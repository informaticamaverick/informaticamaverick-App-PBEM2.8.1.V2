package com.example.myapplication.presentation.features.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.dao.ChatSummary
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.core.domain.model.CompanyClient
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.example.myapplication.presentation.components.DropdownItemData
import com.example.myapplication.presentation.components.FilterSortItem
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.MaverickIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChatThread(
    val chatId: String,
    val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val lastMessage: String,
    val lastTimestamp: Long,
    val isOnline: Boolean,
    val isVerified: Boolean,
    val branchId: String?, // 🔥 Mi identidad (Sucursal)
    val otherBranchId: String?, // 🔥 Identidad del interlocutor
    val categoryId: String?
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val providerRepository: com.example.myapplication.core.data.repository.ProviderRepository,
    private val shortcutRepository: ShortcutRepository,
    private val coordinator: AppActionCoordinator,
    private val auth: FirebaseAuth
) : ViewModel() {

    val searchQuery: StateFlow<String> = coordinator.globalSearchQuery

    private val _totalChatsCount = MutableStateFlow(0)
    val totalChatsCount: StateFlow<Int> = _totalChatsCount.asStateFlow()

    // Expone el mapa de no leídos de forma reactiva (SSOT)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val unreadCountsMap: StateFlow<Map<String, Int>> = userRepository.userProfile
        .flatMapLatest { user ->
            val uid = user?.id ?: auth.currentUser?.uid ?: ""
            if (uid.isEmpty()) flowOf(emptyList())
            else chatRepository.getUnreadCountsPerChat(uid)
        }
        .map { list -> list.associate { it.chatId to it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    val shortcuts: StateFlow<List<FilterSortItem>> = shortcutRepository.getShortcutsByContext("chat")
        .map { list ->
            list.mapNotNull { shortcut ->
                BeDictionary.Filters[shortcut.targetId]?.let { data ->
                    FilterSortItem(
                        id = data.id,
                        label = data.label,
                        emoji = data.emoji ?: "🔹",
                        icon = data.icon,
                        color = data.color,
                        section = data.section
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun manageShortcut(id: String, add: Boolean) {
        viewModelScope.launch {
            if (add) shortcutRepository.addShortcut("chat", id, "filter")
            else shortcutRepository.removeShortcut("chat", id, "filter")
        }
    }

    private val _activeSortCriteria = MutableStateFlow<List<String>>(emptyList())
    val activeSortCriteria: StateFlow<List<String>> = _activeSortCriteria.asStateFlow()

    fun toggleFilter(id: String) {
        val currentFilters = coordinator.activeFilters.value.toMutableSet()
        if (id.startsWith("sort_")) {
            setSortOrder(id)
            return
        }
        if (id == "CLEAR_ALL") {
            currentFilters.clear()
            _activeSortCriteria.value = emptyList()
        } else {
            if (!currentFilters.remove(id)) {
                currentFilters.add(id)
            }
        }
        coordinator.updateFilters(currentFilters)
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _refreshEvent = MutableSharedFlow<String>()
    val refreshEvent = _refreshEvent.asSharedFlow()

    fun refreshAll() {
        viewModelScope.launch {
            val user = userRepository.getUserOnce()
            val uid = user?.id ?: auth.currentUser?.uid ?: ""
            if (uid.isEmpty()) return@launch

            _isRefreshing.value = true
            _refreshEvent.emit("Buscando tus conversaciones...")
            
            try {
                // 1. Sincronizar SOLO lo relativo a mensajes (Categorías para etiquetas)
                categoryRepository.syncWithFirebase()
                
                // 2. Reiniciar escucha de chats (Fuerza descarga de mensajes pendientes en Firebase)
                chatRepository.stopGlobalListening()
                chatRepository.startGlobalListening(uid)
                
                // Pequeño delay para que el usuario vea la animación
                kotlinx.coroutines.delay(1200)
                _refreshEvent.emit("Actualización completa ✅")
            } catch (e: Exception) {
                Log.e("ChatListViewModel", "Error en refresh global: ${e.message}")
                _refreshEvent.emit("Error al sincronizar ❌")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun setSortOrder(sortId: String?) {
        if (sortId == null) {
            _activeSortCriteria.value = emptyList()
            return
        }
        val current = _activeSortCriteria.value.toMutableList()
        if (current.contains(sortId)) {
            current.remove(sortId)
        } else {
            current.add(sortId)
        }
        _activeSortCriteria.value = current
    }

    val selectedPerfilId: StateFlow<String> = coordinator.selectedProfileId
        .map { it ?: "personal" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "personal")

    fun selectPerfil(identityId: String) {
        coordinator.selectProfile(if (identityId == "personal") null else identityId)
        // 🔥 [FIX v8.6] Al cambiar de perfil corporativo, reseteamos el filtro de sucursal
        // Esto evita que un filtro de sucursal previo oculte los chats del nuevo perfil.
        selectBranch(null)
    }

    // 🔥 [ELITE] Proactive Data Sync (Ley #5) - Evita "tirones" sacándolo del combine
    private fun startProactiveSync(summaries: List<ChatSummary>) {
        viewModelScope.launch {
            summaries.forEach { summary ->
                if (summary.userName == "Usuario" || summary.userName == "Cargando..." || summary.userName.isNullOrBlank()) {
                    // 🔥 [FIX v8.4] Sincronización Proactiva SSOT
                    providerRepository.fetchAndSyncProviderDetail(summary.userId)
                }
            }
        }
    }

    // --- GESTIÓN DE IDENTIDAD DUAL (EMPRESA -> SUCURSAL) ---
    private val _selectedBranchId = MutableStateFlow<String?>(null)
    val selectedBranchId = _selectedBranchId.asStateFlow()

    fun selectBranch(branchId: String?) {
        _selectedBranchId.value = branchId
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chattingThreads: StateFlow<Map<String, List<ChatThread>>> = combine(
        userRepository.userProfile.flatMapLatest { user ->
            val uid = user?.id ?: auth.currentUser?.uid ?: ""
            if (uid.isEmpty()) return@flatMapLatest flowOf(emptyList<ChatSummary>())
            chatRepository.startGlobalListening(uid)
            chatRepository.getActiveChatSummaries(uid)
        }.onEach { startProactiveSync(it) }, 
        searchQuery,
        coordinator.activeFilters,
        unreadCountsMap,
        _activeSortCriteria,
        userRepository.userProfile.map { it?.companies ?: emptyList() }.distinctUntilChanged()
    ) { args ->
        val summaries = args[0] as List<ChatSummary>
        val query = args[1] as String
        val activeFilters = args[2] as Set<String>
        val unreadMap = args[3] as Map<String, Int>
        val sortCriteria = args[4] as List<String>
        val companies = args[5] as List<CompanyClient>

        _totalChatsCount.value = summaries.size
        val norm = query.lowercase().trim()
        
        // 🔥 [ELITE v8.7] AGRUPACIÓN MULTI-IDENTIDAD SSOT
        // Creamos un mapa donde cada KEY es un profileId ("personal" o companyId)
        val threadsMap = mutableMapOf<String, MutableList<ChatThread>>()

        summaries.forEach { summary ->
            val sBranchId = summary.branchId?.takeIf { it.isNotBlank() && it != "none" }
            val sCompanyId = summary.companyId?.takeIf { it.isNotBlank() && it != "none" }

            // 1. Clasificación por Identidad (Perfil/Empresa)
            val profileKeys = mutableListOf<String>()
            if (sBranchId == null && sCompanyId == null) {
                profileKeys.add("personal")
            } else {
                // Un chat puede pertenecer a una empresa (vía sucursal o directamente)
                companies.forEach { company ->
                    val belongs = sCompanyId == company.id || company.branches.any { it.id == sBranchId }
                    if (belongs) profileKeys.add(company.id)
                }
            }

            // Si no se pudo clasificar, lo mandamos a personal como fallback
            if (profileKeys.isEmpty()) profileKeys.add("personal")

            // 2. Filtros de Búsqueda y Estado
            val displayName = summary.userName ?: "Cargando..."
            val photoUrl = summary.userPhoto
            val isOnline = summary.isOnline ?: false
            val isVerified = summary.isVerified ?: false

            val passQuery = query.isEmpty() || displayName.lowercase().contains(norm)
            if (!passQuery) return@forEach

            val statusFilters = activeFilters.filter { it.startsWith("filter_chat_") || it.startsWith("filter_") }
            if (statusFilters.isNotEmpty()) {
                val pass = statusFilters.any { filterId ->
                    when (filterId) {
                        "filter_chat_unread" -> (unreadMap[summary.chatId] ?: 0) > 0
                        "filter_chat_online", "filter_online" -> isOnline
                        "filter_chat_business" -> sBranchId != null
                        "filter_chat_pro" -> sBranchId == null
                        "filter_chat_verified" -> isVerified
                        else -> true
                    }
                }
                if (!pass) return@forEach
            }

            val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
            if (catFilters.isNotEmpty()) {
                val category = summary.categoryId
                val pass = catFilters.any { it.equals(category, ignoreCase = true) }
                if (!pass) return@forEach
            }

            val thread = ChatThread(
                chatId = summary.chatId,
                userId = summary.userId,
                displayName = displayName,
                photoUrl = photoUrl,
                lastMessage = summary.lastMessage,
                lastTimestamp = summary.lastTimestamp,
                isOnline = isOnline,
                isVerified = isVerified,
                branchId = sBranchId,
                otherBranchId = summary.remoteBranchId,
                categoryId = summary.categoryId
            )

            // Añadir a cada perfil al que pertenezca
            profileKeys.forEach { key ->
                threadsMap.getOrPut(key) { mutableListOf() }.add(thread)
            }
        }

        // Ordenar cada lista del mapa
        var comparator = compareByDescending<ChatThread> { it.lastTimestamp }
        sortCriteria.forEach { criteria ->
            comparator = when (criteria) {
                "sort_alpha" -> comparator.thenBy { it.displayName }
                "sort_date" -> comparator.thenByDescending { it.lastTimestamp }
                else -> comparator
            }
        }

        threadsMap.mapValues { it.value.sortedWith(comparator) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    // 🔥 [NUEVO v8.6] Conteo de no leídos por identidad para los badges de la UI
    val identityUnreadCounts: StateFlow<Map<String, Int>> = combine(
        chattingThreads,
        unreadCountsMap,
        userRepository.userProfile.map { it?.companies ?: emptyList() }.distinctUntilChanged()
    ) { threadsByProfile, unreadMap, companies ->
        val counts = mutableMapOf<String, Int>()
        
        // 1. Unread Personal
        val personalThreads = threadsByProfile["personal"] ?: emptyList()
        counts["personal"] = personalThreads.sumOf { unreadMap[it.chatId] ?: 0 }
        
        // 2. Unread por Empresa
        companies.forEach { company ->
            val companyThreads = threadsByProfile[company.id] ?: emptyList()
            counts[company.id] = companyThreads.sumOf { unreadMap[it.chatId] ?: 0 }
        }
        
        counts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val filterDropdownItems: StateFlow<List<DropdownItemData>> = combine(
        chattingThreads,
        categoryRepository.allCategories,
        selectedPerfilId
    ) { threadsMap, allCats, currentProfileId ->
        val list = mutableListOf<DropdownItemData>()
        BeDictionary.Filters["filter_chat_unread"]?.let { list.add(it) }
        BeDictionary.Filters["filter_online"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_business"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_pro"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_verified"]?.let { list.add(it) }

        val currentThreads = threadsMap[currentProfileId] ?: emptyList()
        val activeCatNames = currentThreads.mapNotNull { it.categoryId }.distinct()
        activeCatNames.forEach { catName ->
            val catInfo = allCats.find { it.name.equals(catName, ignoreCase = true) }
            list.add(DropdownItemData("cat_$catName", catName, "CATEGORÍAS", catInfo?.icon ?: "📂", MaverickIcons.Filter))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortDropdownItems: StateFlow<List<DropdownItemData>> = flowOf(
        listOfNotNull(
            BeDictionary.Sorts["sort_alpha"],
            BeDictionary.Sorts["sort_date"],
            BeDictionary.Sorts["sort_ranking"]
        )
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedThreads: StateFlow<Map<String, List<ChatThread>>> = combine(
        chattingThreads,
        coordinator.activeFilters,
        selectedPerfilId,
        selectedBranchId // 🔥 [FIX] Escuchamos la sucursal seleccionada
    ) { threadsMap, activeFilters, currentProfileId, branchId ->
        // 1. Filtrado por Identidad Corporativa (Pestaña)
        val allThreads = threadsMap[currentProfileId] ?: emptyList()
        
        // 2. Filtrado por Sucursal (Chip) - Ley #6
        val threads = if (branchId == null) allThreads 
                      else allThreads.filter { it.branchId == branchId }

        val isDateSortActive = activeFilters.contains("sort_date")
        if (!isDateSortActive) mapOf("" to threads)
        else {
            val formatter = SimpleDateFormat("dd MMMM", Locale.getDefault())
            val now = Calendar.getInstance()
            threads.groupBy { 
                val time = Calendar.getInstance().apply { timeInMillis = it.lastTimestamp }
                when {
                    now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR) -> "Hoy"
                    now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - 1 == time.get(Calendar.DAY_OF_YEAR) -> "Ayer"
                    else -> formatter.format(time.time)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive = _isMultiSelectionActive.asStateFlow()

    private val _selectedChatIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedChatIds = _selectedChatIds.asStateFlow()

    fun updateMultiSelection(active: Boolean) {
        _isMultiSelectionActive.value = active
        if (!active) _selectedChatIds.value = emptySet()
    }

    fun toggleSelection(chatId: String) {
        val current = _selectedChatIds.value.toMutableSet()
        if (!current.add(chatId)) current.remove(chatId)
        _selectedChatIds.value = current
    }

    fun selectAll(chatIds: List<String>) {
        _selectedChatIds.value = chatIds.toSet()
    }

    val beActionIds: StateFlow<List<String>> = combine(
        _isMultiSelectionActive,
        _selectedChatIds
    ) { isMulti, selected ->
        val ids = mutableListOf<String>()
        if (isMulti) {
            ids.add("cancel")
            ids.add("divider_v1")
            ids.add("select_all")
            ids.add("divider_v2")
            if (selected.isNotEmpty()) {
                ids.add("delete_multi")
            }
        } else {
            ids.add("goto_direct_budgets")
        }
        ids
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onBeAction(
        actionId: String, 
        onNavigateToBudgets: () -> Unit,
        onShowDeleteConfirm: () -> Unit
    ) {
        when (actionId) {
            "goto_direct_budgets" -> onNavigateToBudgets()
            "select_all" -> {
                val currentThreads = chattingThreads.value[selectedPerfilId.value] ?: emptyList()
                selectAll(currentThreads.map { it.chatId })
            }
            "delete_multi" -> if (_selectedChatIds.value.isNotEmpty()) onShowDeleteConfirm()
            "cancel" -> updateMultiSelection(false)
        }
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            val chatIds = _selectedChatIds.value.toList()
            if (chatIds.isNotEmpty()) {
                chatRepository.deleteConversations(chatIds)
            }
            updateMultiSelection(false)
        }
    }

    fun deleteChatById(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversations(listOf(chatId))
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopGlobalListening()
    }
}

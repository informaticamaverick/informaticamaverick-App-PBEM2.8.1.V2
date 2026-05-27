package com.example.myapplication.presentation.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.dao.ChatSummary
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.data.repository.ChatRepository
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.data.repository.ShortcutRepository
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
    val provider: Provider,
    val lastMessage: String,
    val lastTimestamp: Long
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chattingThreads: StateFlow<List<ChatThread>> = combine(
        userRepository.userProfile.flatMapLatest { user ->
            val uid = user?.id ?: auth.currentUser?.uid ?: ""
            if (uid.isEmpty()) return@flatMapLatest flowOf(emptyList<ChatSummary>())
            chatRepository.startGlobalListening(uid)
            chatRepository.getActiveChatSummaries(uid)
        },
        providerRepository.allProviders,
        searchQuery,
        coordinator.activeFilters,
        unreadCountsMap,
        _activeSortCriteria
    ) { args ->
        val summaries = args[0] as List<ChatSummary>
        val allProviders = args[1] as List<Provider>
        val query = args[2] as String
        val activeFilters = args[3] as Set<String>
        val unreadMap = args[4] as Map<String, Int>
        val sortCriteria = args[5] as List<String>

        _totalChatsCount.value = summaries.size
        val norm = query.lowercase().trim()
        
        val filtered = summaries.mapNotNull { summary ->
            val providerFromRoom = allProviders.find { it.uid == summary.userId }

            val baseProvider = providerFromRoom ?: Provider(
                uid = summary.userId, email = "", phoneNumber = "", displayName = "Cargando...",
                photoUrl = null, name = "Cargando", lastName = "", createdAt = summary.lastTimestamp,
                isOnline = false, isSubscribed = false, isVerified = false, rating = 0f
            )
            val provider = providerRepository.decorateProvider(baseProvider, summary.companyId)

            val passQuery = query.isEmpty() || provider.displayName.lowercase().contains(norm) || (providerFromRoom?.displayName?.lowercase()?.contains(norm) ?: false)
            if (!passQuery) return@mapNotNull null

            val statusFilters = activeFilters.filter { it.startsWith("filter_chat_") || it.startsWith("filter_") }
            if (statusFilters.isNotEmpty()) {
                val pass = statusFilters.any { filterId ->
                    when (filterId) {
                        "filter_chat_unread" -> (unreadMap[summary.chatId] ?: 0) > 0
                        "filter_chat_online", "filter_online" -> provider.isOnline
                        "filter_chat_business" -> summary.companyId != null
                        "filter_chat_pro" -> summary.companyId == null
                        "filter_chat_sub", "filter_sub" -> provider.isSubscribed
                        "filter_chat_fav", "filter_fav" -> provider.isFavorite
                        "filter_chat_verified" -> provider.isVerified
                        "filter_chat_24h", "filter_24h" -> provider.works24h
                        "filter_chat_local", "filter_local" -> provider.hasPhysicalLocation
                        "filter_products" -> provider.doesProduct
                        "filter_services" -> provider.doesService
                        "filter_visits" -> provider.doesHomeVisits
                        "filter_shipping" -> provider.doesShipping
                        "filter_appointments" -> provider.acceptsAppointments
                        else -> true
                    }
                }
                if (!pass) return@mapNotNull null
            }

            val catFilters = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
            if (catFilters.isNotEmpty()) {
                val category = summary.categoryId ?: provider.categories.firstOrNull()
                val pass = catFilters.any { it.equals(category, ignoreCase = true) }
                if (!pass) return@mapNotNull null
            }

            ChatThread(summary.chatId, provider, summary.lastMessage, summary.lastTimestamp)
        }

        var comparator = compareByDescending<ChatThread> { it.lastTimestamp }
        sortCriteria.forEach { criteria ->
            comparator = when (criteria) {
                "sort_alpha" -> comparator.thenBy { it.provider.displayName }
                "sort_ranking" -> comparator.thenByDescending { it.provider.rating }
                "sort_date" -> comparator.thenByDescending { it.lastTimestamp }
                else -> comparator
            }
        }
        filtered.sortedWith(comparator)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterDropdownItems: StateFlow<List<DropdownItemData>> = combine(
        chattingThreads,
        categoryRepository.allCategories
    ) { threads, allCats ->
        val list = mutableListOf<DropdownItemData>()
        BeDictionary.Filters["filter_chat_unread"]?.let { list.add(it) }
        BeDictionary.Filters["filter_online"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_business"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_pro"]?.let { list.add(it) }
        BeDictionary.Filters["filter_products"]?.let { list.add(it) }
        BeDictionary.Filters["filter_services"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_sub"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_fav"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_verified"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_24h"]?.let { list.add(it) }
        BeDictionary.Filters["filter_chat_local"]?.let { list.add(it) }
        BeDictionary.Filters["filter_visits"]?.let { list.add(it) }
        BeDictionary.Filters["filter_shipping"]?.let { list.add(it) }
        BeDictionary.Filters["filter_appointments"]?.let { list.add(it) }

        val activeCatNames = threads.mapNotNull { it.provider.categories.firstOrNull() }.distinct()
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
        coordinator.activeFilters
    ) { threads, activeFilters ->
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
            "select_all" -> selectAll(chattingThreads.value.map { it.chatId })
            "delete_multi" -> if (_selectedChatIds.value.isNotEmpty()) onShowDeleteConfirm()
            "cancel" -> updateMultiSelection(false)
        }
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            val chatIds = _selectedChatIds.value.toList()
            if (chatIds.isNotEmpty()) {
                chatRepository.deleteChats(chatIds)
            }
            updateMultiSelection(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopGlobalListening()
    }
}

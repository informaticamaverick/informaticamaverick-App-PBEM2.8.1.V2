package com.example.myapplication.presentation.global

import com.example.myapplication.core.domain.model.AddressUnico
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.database.TokenManager
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.data.repository.*
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.features.home.CategoryVisuals
import com.example.myapplication.presentation.features.home.SuperCategory
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.BeDictionaryConversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

/** 
 * --- BE BRAIN VIEWMODEL (EL CEREBRO / INTERMEDIARIO) ---
 * Centraliza el estado global y actúa como puente entre los Obreros (ViewModels de cálculo) 
 * y la Interfaz de Usuario.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BeBrainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
   // private val tokenManager: TokenManager,
    private val chatRepository: ChatRepository,
   // private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val categorySeeder: com.example.myapplication.core.data.local.seed.CategorySeeder,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context,
    val coordinator: AppActionCoordinator
) : ViewModel() {

    // ======================================================================================
    // --- SECTOR 0: MONITOREO DE CONECTIVIDAD (RESILIENCIA MAVERICK) ---
    // ======================================================================================
    private val _syncErrorEvent = MutableSharedFlow<String>()
    val syncErrorEvent = _syncErrorEvent.asSharedFlow()

    private fun setupConnectivityListener() {
        // [OPTIMIZACIÓN] El monitoreo ahora reside en el AppActionCoordinator.
        // El Cerebro solo reacciona a los cambios para disparar sincronizaciones.
        viewModelScope.launch {
            coordinator.isOnline.collect { online ->
                if (online && _isOffline.value) {
                    _isOffline.value = false
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "🌐 MODO ONLINE RESTAURADO", Toast.LENGTH_SHORT).show()
                    }
                    syncUserDataInBackground()
                } else if (!online && !_isOffline.value) {
                    _isOffline.value = true
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "🛠️ MODO OFFLINE ACTIVO", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun syncUserDataInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userRepository.refreshUserFromRemote()
                Log.d("BeBrainViewModel", "✅ Sincronización de fondo completada")
            } catch (e: Exception) {
                Log.e("BeBrainViewModel", "❌ Error en sincronización de fondo: ${e.message}")
                if (!_isOffline.value) {
                    _syncErrorEvent.emit(e.message ?: "Error desconocido en la nube")
                }
            }
        }
    }

    // ======================================================================================
    // --- SECTOR 1: NOTIFICACIONES Y ESTADO DE USUARIO ---
    // ======================================================================================
    val totalUnreadCount: StateFlow<Int> = userRepository.userProfile
        .flatMapLatest { user: UserEntity? ->
            if (user != null) chatRepository.getTotalUnreadCount(user.id)
            else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userState: StateFlow<UserEntity?> = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 🔥 [ELITE v8.8] CONTEO DE NO LEÍDOS POR IDENTIDAD (SSOT)
    // Centraliza la lógica de badges para que BeBrain sepa qué perfil tiene actividad.
    val identityUnreadCounts: StateFlow<Map<String, Int>> = userState.flatMapLatest { user ->
        val uid = user?.id ?: authRepository.getCurrentUser()?.uid ?: ""
        if (uid.isEmpty()) flowOf(emptyMap())
        else combine(
            chatRepository.getActiveChatSummaries(uid),
            chatRepository.getUnreadCountsPerChat(uid),
            userRepository.userProfile.map { it?.companies ?: emptyList() }.distinctUntilChanged()
        ) { summaries, unreadCounts, companies ->
            val unreadMap = unreadCounts.associate { it.chatId to it.count }
            val counts = mutableMapOf<String, Int>()
            
            summaries.forEach { summary ->
                val count = unreadMap[summary.chatId] ?: 0
                if (count == 0) return@forEach
                
                val sBranchId = summary.branchId?.takeIf { it.isNotBlank() && it != "none" }
                val sCompanyId = summary.companyId?.takeIf { it.isNotBlank() && it != "none" }

                if (sBranchId == null && sCompanyId == null) {
                    counts["personal"] = (counts["personal"] ?: 0) + count
                } else {
                    companies.forEach { company ->
                        val belongs = sCompanyId == company.id || company.branches.any { it.id == sBranchId }
                        if (belongs) {
                            counts[company.id] = (counts[company.id] ?: 0) + count
                        }
                    }
                }
            }
            counts
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val hasChatNotifications: StateFlow<Boolean> = totalUnreadCount
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- NUEVO: PERFIL ACTIVO (USUARIO O EMPRESA) ---
    val selectedProfileId: StateFlow<String?> = coordinator.selectedProfileId

    /**
     * Nombre que debe mostrar la cabecera (Usuario o Empresa activa).
     */
    val activeProfileName: StateFlow<String> = combine(userState, selectedProfileId) { user, profileId ->
        if (user == null) return@combine "Usuario"
        val domainUser = user.toDomain()
        if (profileId == null) {
            // Perfil Personal: Prioridad DisplayName -> Nombre Completo -> Email
            domainUser.displayName.ifBlank {
                domainUser.fullName.ifBlank {
                    domainUser.email.substringBefore("@")
                }
            }
        } else {
            // Perfil de Empresa / Sucursal
            // Buscamos en todas las sucursales de todas las empresas del usuario
            val branch = user.companies.flatMap { it.branches }.find { it.id == profileId }
            branch?.name ?: user.companies.find { it.id == profileId }?.name
                ?: domainUser.displayName.ifBlank { domainUser.fullName }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    /**
     * Foto procesada (URL o ByteArray/Base64) que debe mostrar la cabecera.
     * [ELITE v5.5]: Prioriza thumbnails y usa caché de decodificación.
     */
    val activeProfilePhoto: StateFlow<Any?> = combine(userState, selectedProfileId) { user, profileId ->
        if (user == null) return@combine null
        
        val source = if (profileId == null) {
            // Perfil Personal: Thumbnail -> PhotoUrl
            user.profileThumbnail?.takeIf { it.isNotBlank() } ?: user.photoUrl
        } else {
            // Perfil de Empresa / Sucursal: Thumbnail -> PhotoUrl Empresa -> PhotoUrl Usuario
            val company = user.companies.find { it.id == profileId || it.branches.any { b -> b.id == profileId } }
            company?.thumbnailBase64?.takeIf { it.isNotBlank() }
                ?: company?.photoUrl 
                ?: user.photoUrl
        }
        ImageUtils.processImageSource(source)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectProfile(profileId: String?) {
        coordinator.selectProfile(profileId)
    }

    // ======================================================================================
    // --- SECTOR 2: ESTADO DE NAVEGACIÓN Y AUTH ---
    // ======================================================================================
    private val _initialNavTarget = MutableStateFlow(InitialNavTarget.CHECKING)
    val initialNavTarget: StateFlow<InitialNavTarget> = _initialNavTarget.asStateFlow()
/**
    private val _isFirstTime = MutableStateFlow(tokenManager.isFirstTime())
*/
    fun performInitialAuthCheck() {
        viewModelScope.launch {
            // [OPTIMIZACIÓN MAVERICK]: Eliminamos delay artificial de 2s. 
            // El chequeo es instantáneo para mejorar el TTFD (Time to Full Display).
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _initialNavTarget.value = InitialNavTarget.LOGIN
            } else {
                // [MANDATO CP]: Solo navegamos, la sincronización se dispara reactivamente 
                // o vía los Obreros específicos para evitar redundancia.

                // Forzamos refresh si Room está vacío para asegurar que ProfileViewModel y otros tengan datos
                val user = userRepository.userProfile.firstOrNull()
                if (user == null) {
                    Log.d("BeBrainViewModel", "🚀 Usuario logueado pero Room vacío. Sincronizando...")
                    userRepository.refreshUserFromRemote()
                } else if (user.personalAddresses.isEmpty()) {
                    _showAddressPopup.value = true
                }
                _initialNavTarget.value = InitialNavTarget.MAIN_SCREEN
            }
        }
    }
/**
    fun completeFirstTime() {
        tokenManager.setFirstTimeCompleted()
        _isFirstTime.value = false
    }
*/
    // ======================================================================================
    // --- SECTOR 3: UBICACIÓN Y CLIMA (Elite SSOT) ---
    // ======================================================================================
    val temperature: StateFlow<String> = coordinator.temperature
    val weatherEmoji: StateFlow<String> = coordinator.weatherEmoji
    val weatherDescription: StateFlow<String> = coordinator.weatherDescription
    
    val locationName: StateFlow<String> = coordinator.activeAddress.map { 
        it?.localidad ?: "Actualizando..."
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Actualizando...")

    val selectedAddressId: StateFlow<String?> = coordinator.selectedAddressId

    val availableAddressInfos: StateFlow<List<AddressUnico>> = coordinator.availableAddressInfos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAddress: StateFlow<AddressUnico?> = coordinator.activeAddress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectAddress(addressId: String) {
        coordinator.selectAddress(addressId)
    }

    fun updateAddressFromGps(address: AddressUnico) {
        coordinator.updateAddressFromGps(address)
    }

    private val _showAddressPopup = MutableStateFlow(true)
    val showAddressPopup: StateFlow<Boolean> = _showAddressPopup.asStateFlow()

    fun dismissAddressPopup() {
        _showAddressPopup.value = false
    }

    // ======================================================================================
    // --- SECTOR 4: HUD Y CONTEXTO (Sincronización Global) ---
    // ======================================================================================
    val currentContext: StateFlow<HUDContext> = coordinator.currentHUDContext
    val isSheetVisible: StateFlow<Boolean> = coordinator.isSheetVisible

    val isBottomBarVisible: StateFlow<Boolean> = combine(currentContext, coordinator.globalSearchQuery, isSheetVisible) { context, query, sheetVisible ->
        val searching = query.isNotEmpty()
        context.requiresBottomBar && !searching && !sheetVisible
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showBe: StateFlow<Boolean> = currentContext.map { it.showBeAssistant }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _showWeatherDetails = MutableStateFlow(false)
    val showWeatherDetails: StateFlow<Boolean> = _showWeatherDetails.asStateFlow()

    private val _showFavoritesPanel = MutableStateFlow(false)
    val showFavoritesPanel: StateFlow<Boolean> = _showFavoritesPanel.asStateFlow()

    private val _isResultadoVisible = MutableStateFlow(false)

    private val _isUIBlocked = MutableStateFlow(false)

    fun toggleWeatherDetails() { _showWeatherDetails.value = !_showWeatherDetails.value }
    fun setWeatherDetailsVisible(visible: Boolean) { _showWeatherDetails.value = visible }
    fun toggleFavoritesPanel() { _showFavoritesPanel.value = !_showFavoritesPanel.value }
    fun setFavoritesPanelVisible(visible: Boolean) { _showFavoritesPanel.value = visible }
    
    fun setResultadoVisible(visible: Boolean) { 
        if (visible) {
            if (!_isUIBlocked.value) _isResultadoVisible.value = true 
        } else {
            _isResultadoVisible.value = false
        }
    }
    fun setUIBlocked(blocked: Boolean) { _isUIBlocked.value = blocked; if (blocked && coordinator.globalSearchQuery.value.isNotEmpty()) _isResultadoVisible.value = false }
    fun setSheetVisible(visible: Boolean) { coordinator.updateSheetVisibility(visible) }
    fun updateHUDContext(context: HUDContext) { coordinator.updateHUDContext(context) }

    // ======================================================================================
    // --- SECTOR 5: BÚSQUEDA Y FILTROS (Elite SSOT) ---
    // ======================================================================================
    val searchQuery: StateFlow<String> = coordinator.globalSearchQuery
    val activeFilters: StateFlow<Set<String>> = coordinator.activeFilters

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    val allCategories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSuperCategory = MutableStateFlow<SuperCategory?>(null)
    val selectedSuperCategory: StateFlow<SuperCategory?> = _selectedSuperCategory.asStateFlow()

    fun selectSuperCategory(superCategory: SuperCategory?) { _selectedSuperCategory.value = superCategory }

    fun updateSearchQuery(query: String) {
        coordinator.updateSearchQuery(query)
        
        // [ELITE PERFORMANCE] La visibilidad se decide por el estado del query debounced
        // para evitar parpadeos y micro-tirones durante la escritura rápida.
        viewModelScope.launch {
            if (query.isNotEmpty() && !_isUIBlocked.value) {
                // Pequeño delay para esperar al frame de animación de la barra
                delay(50) 
                _isResultadoVisible.value = true
            } else if (query.isEmpty()) {
                _isResultadoVisible.value = false
            }
        }
    }

    fun onSearchSubmitted() {
        val query = coordinator.globalSearchQuery.value.trim()
        if (query.isEmpty()) return
        viewModelScope.launch {
            coordinator.submitSearch(query)
        }
    }

    val availableFilters: StateFlow<List<ControlItem>> = currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(
                BeDictionary.Filters["filter_products"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_services"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_24h"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_local"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(
                BeDictionary.Filters["filter_tender_active"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_tender_closed"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_tender_canceled"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_tender_awarded"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> listOf(
                BeDictionary.Filters["filter_budget_pending"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_budget_accepted"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_budget_rejected"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_chat_sub"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_chat_fav"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            HUDContext.CHAT, HUDContext.SEARCH_RESULTS, HUDContext.FAST -> listOf(
                BeDictionary.Filters["filter_chat_sub"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_chat_fav"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_online"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_products"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_services"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_24h"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_shipping"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_visits"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_local"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Filters["filter_appointments"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableSortOptions: StateFlow<List<ControlItem>> = currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(
                BeDictionary.Sorts["sort_alpha"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["view_compact"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(
                BeDictionary.Sorts["sort_alpha"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["sort_date"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["view_compact"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> listOf(
                BeDictionary.Sorts["sort_alpha"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["sort_date"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["sort_price"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            HUDContext.CHAT, HUDContext.SEARCH_RESULTS, HUDContext.FAST -> listOf(
                BeDictionary.Sorts["sort_alpha"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["sort_date"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["sort_distance"]?.toControlItem() ?: ControlItem("", null, "", Color.White),
                BeDictionary.Sorts["sort_ranking"]?.toControlItem() ?: ControlItem("", null, "", Color.White)
            )
            else -> listOf(BeDictionary.Sorts["sort_alpha"]?.toControlItem() ?: ControlItem("", null, "", Color.White))
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

 /**
    val dynamicCategories: StateFlow<List<ControlItem>> = combine(
        currentContext, categoryRepository.allCategories, budgetRepository.allTenders, budgetRepository.allBudgets
    ) { context, allCats, tenders, budgets ->
        val filtered = when (context) {
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                val categoryNamesInTenders = tenders.asSequence().map { it.category.lowercase().trim() }.toSet()
                allCats.filter { it.name.lowercase().trim() in categoryNamesInTenders }
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                val categoryNamesInBudgets = budgets.asSequence().mapNotNull { it.category?.lowercase()?.trim() }.toSet()
                allCats.filter { it.name.lowercase().trim() in categoryNamesInBudgets }
            }
            else -> allCats
        }
        filtered.map { cat -> 
            ControlItem(
                label = cat.name, 
                icon = null, 
                emoji = cat.icon,
                color = Color(CategoryVisuals.getColorFor(cat.superCategory)),
                id = "cat_${cat.name.lowercase().trim()}"
            ) 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
*/


    fun clearFilters() {
        coordinator.updateSearchQuery("")
        _selectedSuperCategory.value = null
        coordinator.updateFilters(emptySet())
        _selectedItemIds.value = emptySet()
        _isMultiSelectionActive.value = false
        _isResultadoVisible.value = false
        if (_beState.value == BeState.NOTIFICATION_READY) _beState.value = BeState.IDLE
        triggerAction("clear_filters")
    }

    fun toggleFilter(filterId: String) {
        val current = coordinator.activeFilters.value.toMutableSet()
        if (!current.add(filterId)) current.remove(filterId)
        coordinator.updateFilters(current)
    }

    // ======================================================================================
    // --- SECTOR 6: ACCIONES Y HERRAMIENTAS (Pequeñas Acciones) ---
    // ======================================================================================
    val actionEvent = coordinator.actionEvent

    private val _currentActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())
    val currentActions: StateFlow<List<BeSmallActionModel>> = _currentActions.asStateFlow()

    private val _customActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())
    private var _currentActionsOwner: HUDContext? = null

    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive: StateFlow<Boolean> = _isMultiSelectionActive.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())

    fun triggerAction(actionId: String) { 
        viewModelScope.launch { 
            when {
                actionId.startsWith("cat_") -> {
                    val catName = actionId.removePrefix("cat_")
                    categoryRepository.allCategories.firstOrNull()?.find { 
                        it.name.lowercase().trim() == catName.lowercase().trim() 
                    }?.let { _categorySelectionEvent.emit(it) }
                    cerrarBeAssistantCompleto()
                }
                actionId.startsWith("select_address_") -> {
                    val addrId = actionId.removePrefix("select_address_")
                    selectAddress(addrId)
                }
            }
            coordinator.triggerAction(actionId)
        } 
    }

    fun setCustomActions(actions: List<BeSmallActionModel>, context: HUDContext? = null) { 
        val targetContext = context ?: currentContext.value
        val currentTopLevel = getTopLevelContext(currentContext.value)
        val targetTopLevel = getTopLevelContext(targetContext)
        
        if (targetContext == currentContext.value || targetTopLevel == currentTopLevel) {
            _customActions.value = actions
            _currentActionsOwner = targetContext
            updateActionsForContext(currentContext.value)
        }
    }

    /**
     * 🔥 [ELITE SSOT] Resuelve IDs de acción enviados por los Obreros.
     * Centraliza la representación visual (Iconos/Colores) en el Cerebro.
     */
    fun setCustomActionIds(ids: List<String>, context: HUDContext? = null) {
        val actions = ids.mapNotNull { id ->
            BeDictionary.Actions[id]?.let { visuals ->
                BeSmallActionModel(
                    id = id,
                    icon = visuals.icon,
                    label = visuals.label,
                    emoji = visuals.emoji,
                    tint = visuals.tint,
                    isDefault = visuals.isDefault
                ) { triggerAction(id) }
            } ?: if (id.startsWith("divider_v")) {
                BeSmallActionModel(id, Icons.Default.VerticalAlignBottom, "") { }
            } else null
        }
        setCustomActions(actions, context)
    }

    fun clearCustomActions(context: HUDContext) {
        if (_currentActionsOwner == context) {
            _customActions.value = emptyList()
            _currentActionsOwner = null
            updateActionsForContext(currentContext.value)
        }
    }

    fun syncMultiSelection(active: Boolean) {
        if (currentContext.value == HUDContext.HOME && !active) { 
            _isMultiSelectionActive.value = false
            _selectedItemIds.value = emptySet()
            return 
        }
        val wasActive = _isMultiSelectionActive.value
        _isMultiSelectionActive.value = active
        // 🔥 [ELITE] BeBrain ya no replica la lista de IDs seleccionados de los obreros.
        // Solo mantiene su propio estado de herramientas visibles.
        if (active && !wasActive) _showBeTools.value = true 
        else if (!active && wasActive) _showBeTools.value = false
        
        updateActionsForContext(currentContext.value)
        updateToolboxKey()
    }

    private fun updateActionsForContext(context: HUDContext) {
        val actions = mutableListOf<BeSmallActionModel>()
        
        when (context) {
            HUDContext.HOME -> {
                actions.add(BeSmallActionModel("sim_chat", Icons.AutoMirrored.Filled.Chat, "Sim Chat", emoji = "💬") { triggerAction("sim_chat") })
                actions.add(BeSmallActionModel("sim_tender", Icons.Default.Gavel, "Sim Licit", emoji = "⚖️") { triggerAction("sim_tender") })
                actions.add(BeSmallActionModel("sim_massive", Icons.Default.PersonAdd, "Sim Prov", emoji = "👥") { triggerAction("sim_massive") })
                actions.add(BeSmallActionModel("migrate_cats", Icons.Default.CloudUpload, "Migrar", emoji = "☁️") { triggerAction("migrate_cats") })
                actions.add(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true) { triggerAction("fast") })
                actions.add(BeSmallActionModel("fav", Icons.Default.Favorite, "Favoritos", emoji = "❤️", isDefault = true) { triggerAction("fav") })
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
            }
            HUDContext.CHAT -> {
                if (_isMultiSelectionActive.value) {
                    actions.add(BeSmallActionModel("cancel", Icons.Default.Close, "CERRAR", tint = Color.Red, isDefault = false) { triggerAction("cancel") })
                    actions.add(BeSmallActionModel("divider_v1", Icons.Default.HorizontalRule, "", isDefault = false)) 
                    actions.add(BeSmallActionModel("select_all", Icons.Default.SelectAll, "TODOS", tint = Color(0xFF2197F5), isDefault = false) { triggerAction("select_all") })
                    actions.add(BeSmallActionModel("divider_v2", Icons.Default.HorizontalRule, "", isDefault = false))
                    actions.add(BeSmallActionModel("delete_multi", Icons.Default.Delete, "ELIMINAR", tint = Color.Red, isDefault = false) { triggerAction("delete_multi") })
                } else {
                    actions.add(BeSmallActionModel("goto_direct_budgets", Icons.AutoMirrored.Filled.Chat, "Presupuestos", emoji = "📩", tint = Color(0xFF2197F5), isDefault = true) { triggerAction("goto_direct_budgets") })
                }
            }
            HUDContext.PROFILE -> {
                // [ELITE SSOT]: Las acciones ahora las inyecta el Worker (ProfileViewModel) vía setCustomActionIds.
            }
            HUDContext.CALENDAR -> {
                actions.add(BeSmallActionModel("goto_history", Icons.Default.History, "Historial", emoji = "📜", tint = Color(0xFFFF9800), isDefault = true) { triggerAction("goto_history") })
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                actions.add(BeSmallActionModel("licit", Icons.Default.Add, "Nueva Lic", emoji = "📄", tint = Color(0xFF2197F5), isDefault = true) { triggerAction("licit") })
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                actions.add(BeSmallActionModel("goto_history", Icons.Default.History, "Historial", emoji = "📜", tint = Color(0xFFFF9800), isDefault = true) { triggerAction("goto_history") })
            }
            HUDContext.SEARCH_RESULTS, HUDContext.FAST -> {
                actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
            }
            else -> {}
        }
        
        val ownerTopLevel = _currentActionsOwner?.let { getTopLevelContext(it) }
        val currentTopLevel = getTopLevelContext(context)

        if (_currentActionsOwner == context || (ownerTopLevel != null && ownerTopLevel == currentTopLevel)) {
            _customActions.value.forEach { customAction ->
                if (actions.none { it.id == customAction.id }) {
                    actions.add(customAction)
                }
            }
        }

        _currentActions.value = actions
    }

    private val _toolboxKey = MutableStateFlow("home_default")
    val toolboxKey: StateFlow<String> = _toolboxKey.asStateFlow()

    private fun updateToolboxKey() { val context = currentContext.value.name.lowercase(); val mode = if (_showBeTools.value) "tools" else "default"; _toolboxKey.value = "${context}_$mode" }

    // ======================================================================================
    // --- SECTOR 7: BE ASSISTANT: COMPORTAMIENTO Y MENSAJERÍA ---
    // ======================================================================================
    private val _beState = MutableStateFlow(BeState.IDLE)
    val beState: StateFlow<BeState> = _beState.asStateFlow()

    private val _beMessages = MutableStateFlow<List<BeMessage>>(emptyList())
    val beMessages: StateFlow<List<BeMessage>> = _beMessages.asStateFlow()

    private val _activeConversationalMessage = MutableStateFlow<BeMessage?>(null)
    val activeConversationalMessage: StateFlow<BeMessage?> = _activeConversationalMessage.asStateFlow()

    private val _currentTipIndex = MutableStateFlow(0)
    val currentTipIndex: StateFlow<Int> = _currentTipIndex.asStateFlow()

    fun syncConversationalMessages(messages: List<BeMessage>) {
        if (messages.isNotEmpty()) {
            _beMessages.value = messages
        }
    }

    fun syncActiveResponse(message: BeMessage?) {
        _activeConversationalMessage.value = message
        if (message != null) {
            _beState.value = BeState.TALKING
        }
    }

    fun clearActiveResponse() {
        _activeConversationalMessage.value = null
        if (_beState.value == BeState.TALKING) {
            _beState.value = BeState.IDLE
        }
    }

    fun setBeState(state: BeState) { _beState.value = state }
    fun nextTip() { if (_currentTipIndex.value < beMessages.value.size - 1) _currentTipIndex.value++ }
    fun prevTip() { if (_currentTipIndex.value > 0) _currentTipIndex.value-- }

    private var behaviorLoopJob: Job? = null

    private fun startBeBrainLoop() {
        behaviorLoopJob?.cancel()
        behaviorLoopJob = viewModelScope.launch {
            while (true) {
                if (_isBeDormido.value) { _beState.value = BeState.IDLE; delay(2000); continue }
                when (_beState.value) {
                    BeState.IDLE -> {
                        delay((6000..15000).random().toLong())
                        if (_beMessages.value.isNotEmpty() && coordinator.globalSearchQuery.value.isEmpty()) {
                            _currentTipIndex.value = _beMessages.value.indices.random()
                            // [MODIFICACIÓN MAVERICK]: Eliminamos el cambio automático a NOTIFICATION_READY 
                            // para que no salte ni se mueva solo. Se queda en IDLE hasta que el usuario interactúe.
                        }
                    }
                    BeState.NOTIFICATION_READY -> { delay(12000); if (_beState.value == BeState.NOTIFICATION_READY) _beState.value = BeState.IDLE }
                    BeState.TALKING -> delay(1000)
                }
            }
        }
    }

    private fun updateBeContextMessages(route: String) {
        val finalMessages = mutableListOf<BeMessage>()

        if (_isOffline.value) {
            finalMessages.add(BeMessage("⚠️", "Sin conexión. Búsquedas limitadas a la base de datos interna 🛠️", null, Color(0xFFEF4444), emotion = BeEmotion.ANGRY))
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        if (currentHour in 21..23 || currentHour in 0..5) finalMessages.add(BeMessage("🌙", "Es tarde. Si tienes una urgencia, usa Maverick FAST.", "PROBAR FAST", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED))
        finalMessages.addAll(when {
            route.contains("home") -> BeDictionaryConversation.HomeMessages
            route.contains("presupuestos") -> BeDictionaryConversation.BudgetMessages
            route.contains("chat") -> BeDictionaryConversation.ChatMessages
            route.contains("calendar") -> BeDictionaryConversation.CalendarMessages
            route.contains("result_busqueda") -> listOf(BeMessage("🔍", "Aquí tienes los prestadores de esta categoría.", null, Color(0xFF22D3EE)))
            route.contains("fast") -> listOf(BeMessage("⚡", "Búsqueda táctica activada. Solo unidades de respuesta inmediata.", null, Color(0xFF22D3EE)))
            else -> BeDictionaryConversation.DefaultMessages
        })
        _beMessages.value = finalMessages
    }

    // ======================================================================================
    // --- SECTOR 8: GESTOS Y EVENTOS DE BE ---
    // ======================================================================================
    private val _isBeDormido = MutableStateFlow(false)
    val isBeDormido: StateFlow<Boolean> = _isBeDormido.asStateFlow()

    private val _showBeTools = MutableStateFlow(false)
    val showBeTools: StateFlow<Boolean> = _showBeTools.asStateFlow()

    private val _resetBePositionTrigger = MutableStateFlow(0)
    val resetBePositionTrigger: StateFlow<Int> = _resetBePositionTrigger.asStateFlow()

    private val _isBubbleMuted = MutableStateFlow(false)
    val isBubbleMuted: StateFlow<Boolean> = _isBubbleMuted.asStateFlow()

    private val _hasNewMessage = MutableStateFlow(false)
    val hasNewMessage: StateFlow<Boolean> = _hasNewMessage.asStateFlow()

    private val _requestKeyboard = MutableStateFlow(false)
    val requestKeyboard = _requestKeyboard.asStateFlow()

    private val _categorySelectionEvent = MutableSharedFlow<CategoryEntity>()

    fun onBeClick() { 
        if (_isBeDormido.value) {
            _isBeDormido.value = false 
        } else {
            setSearchActive(!_isSearchActive.value) 
        }
    }

    fun onBeDoubleClick() { 
        if (_isBeDormido.value) {
            _isBeDormido.value = false
            _resetBePositionTrigger.value += 1 
        } else {
            _isBeDormido.value = true
            cerrarBeAssistantCompleto()
        }
    }

    fun onBeLongClick() {
        if (!_isBeDormido.value) {
            val nextToolsState = !_showBeTools.value
            if (nextToolsState) {
                if (!_isMultiSelectionActive.value) cerrarBeAssistantCompleto()
                else { coordinator.updateSearchQuery(""); _isResultadoVisible.value = false }
                _showBeTools.value = true; updateActionsForContext(currentContext.value)
            } else _showBeTools.value = false
        }
        updateToolboxKey()
    }

    fun setSearchActive(active: Boolean) {
        if (active) {
            _isSearchActive.value = true; _showBeTools.value = false; _beState.value = BeState.IDLE; openKeyboard()
        } else cerrarBeAssistantCompleto()
    }

    fun cerrarBeAssistantCompleto() {
        _isSearchActive.value = false; coordinator.updateSearchQuery(""); _isResultadoVisible.value = false
        _selectedSuperCategory.value = null
        
        _showBeTools.value = false; _isMultiSelectionActive.value = false; _selectedItemIds.value = emptySet()
        _beState.value = BeState.IDLE; closeKeyboard()
    }

    fun toggleBubbleMute() {
        _isBubbleMuted.value = !_isBubbleMuted.value
        _hasNewMessage.value = false
    }

    fun openKeyboard() { _requestKeyboard.value = true }
    fun closeKeyboard() { _requestKeyboard.value = false }

    fun setShowBeTools(visible: Boolean) { _showBeTools.value = visible; if (visible) updateActionsForContext(currentContext.value); updateToolboxKey() }

    // ======================================================================================
    // --- SECTOR 9: RUTAS Y CICLO DE VIDA ---
    // ======================================================================================
    private var _lastRoute: String? = null

    fun onRouteChanged(route: String?) {
        val currentRoute = route ?: return
        _lastRoute = currentRoute
        
        val newContext = when {
            currentRoute == "home" || currentRoute.startsWith("home") -> HUDContext.HOME
            currentRoute == "presupuestos" || currentRoute.startsWith("presupuestos") -> HUDContext.BUDGETS
            currentRoute == "chat_presupuestos_recibidos" || currentRoute == "direct_budgets" -> HUDContext.BUDGETS_DIRECT
            currentRoute.startsWith("chat") -> HUDContext.CHAT
            currentRoute == "calendar" || currentRoute.startsWith("calendar") -> HUDContext.CALENDAR
            currentRoute == "perfil_cliente" || currentRoute.startsWith("perfil_cliente") -> HUDContext.PROFILE
            currentRoute.startsWith("perfil_prestador") -> HUDContext.PROFILE_PRESTADOR
            currentRoute.startsWith("result_busqueda") -> HUDContext.SEARCH_RESULTS
            currentRoute == "crear_licitacion" -> HUDContext.BUDGETS_TENDERS
            currentRoute == "fast" || currentRoute.startsWith("fast") -> HUDContext.FAST
            currentRoute == "promo" || currentRoute.startsWith("promo") -> HUDContext.PROMO
            else -> HUDContext.UNKNOWN
        }

        val oldContext = currentContext.value
        coordinator.updateHUDContext(newContext)

        _showFavoritesPanel.value = false
        _showWeatherDetails.value = false
        _showProviderSimDialog.value = false

        if (oldContext != newContext) {
            if (getTopLevelContext(oldContext) != getTopLevelContext(newContext)) {
                if (_currentActionsOwner != newContext && _currentActionsOwner != getTopLevelContext(newContext)) {
                    _customActions.value = emptyList()
                    _currentActionsOwner = null
                }
            }
            if (_isSearchActive.value) {
                cerrarBeAssistantCompleto()
            }
        }

        _isResultadoVisible.value = false
        _showBeTools.value = false

        updateBeContextMessages(currentRoute)
    }

    fun onEasterEggLinkClick() {
        cerrarBeAssistantCompleto()
        onRouteChanged(_lastRoute)
    }

    override fun onCleared() {
        super.onCleared()
        chatRepository.stopGlobalListening()
        val user = authRepository.getCurrentUser()
        if (user != null) {
            chatRepository.setUserOnline(user.uid, false)
        }
    }

    private fun getTopLevelContext(context: HUDContext): HUDContext {
        return when (context) {
            HUDContext.BUDGETS, 
            HUDContext.BUDGETS_TENDERS, 
            HUDContext.BUDGETS_DIRECT, 
            HUDContext.TENDER_DETAILS -> HUDContext.BUDGETS
            HUDContext.CHAT -> HUDContext.CHAT
            HUDContext.HOME -> HUDContext.HOME
            HUDContext.PROFILE -> HUDContext.PROFILE
            HUDContext.SEARCH_RESULTS -> HUDContext.SEARCH_RESULTS
            HUDContext.FAST -> HUDContext.FAST
            HUDContext.CALENDAR -> HUDContext.CALENDAR
            HUDContext.PROMO -> HUDContext.PROMO
            else -> HUDContext.UNKNOWN
        }
    }

    // ======================================================================================
    // --- SECTOR 10: RESILIENCIA Y CONECTIVIDAD (MAVERICK CORE) ---
    // ======================================================================================
    private val _isOffline = MutableStateFlow(false)

    val isGpsEnabled = coordinator.isGpsEnabled

    private val _showProviderSimDialog = MutableStateFlow(false)
    val showProviderSimDialog: StateFlow<Boolean> = _showProviderSimDialog.asStateFlow()

    fun setShowProviderSimDialog(visible: Boolean) { _showProviderSimDialog.value = visible }

    fun setHUDContext(context: HUDContext) {
        val current = currentContext.value
        if (getTopLevelContext(current) != getTopLevelContext(context)) {
            if (_currentActionsOwner != context && _currentActionsOwner != getTopLevelContext(context)) {
                _customActions.value = emptyList()
                _currentActionsOwner = null
            }
            cerrarBeAssistantCompleto()
            clearFilters()
        }
        
        coordinator.updateHUDContext(context)
        _showFavoritesPanel.value = false
        _showWeatherDetails.value = false
        _showProviderSimDialog.value = false
    }

    init {
        setupConnectivityListener()
        startBeBrainLoop() 
        performInitialAuthCheck() // [PROACTIVO] Verificación de identidad inmediata
        
        // [NUEVO: POLÍTICA ZERO COSTO] Sembrado local desde JSON
        viewModelScope.launch(Dispatchers.IO) {
            val seeded = categorySeeder.seedIfNeeded()
            if (seeded) {
                // Si fue la primera vez, agregamos el acceso directo de Hogar y Mantenimiento
                shortcutRepository.addShortcut(
                    context = "home",
                    targetId = "Hogar y Mantenimiento",
                    type = "supercategory",
                    label = "Hogar y Mantenimiento",
                    icon = "🏠"
                )
                Log.d("BeBrainViewModel", "🚀 Sembrado inicial y shortcut creados con éxito.")
            }
        }

        viewModelScope.launch {
            coordinator.currentHUDContext.collect { context ->
                updateActionsForContext(context)
                updateToolboxKey()
            }
        }
    }
}












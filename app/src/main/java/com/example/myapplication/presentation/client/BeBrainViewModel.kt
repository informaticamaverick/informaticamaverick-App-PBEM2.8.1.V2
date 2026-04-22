package com.example.myapplication.presentation.client

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.components.BeMessage
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.components.BeState
import com.example.myapplication.presentation.components.AddressInfo
import com.example.myapplication.presentation.registry.BeMenuRegistry
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import com.example.myapplication.presentation.components.toLocationOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.local.TokenManager
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.CalendarRepository
import com.example.myapplication.data.local.ChatUnreadCount

// ==========================================================================================
// --- SECCIÓN: ENUMS Y MODELOS DE APOYO (DOMINIO DEL CEREBRO) ---
// ==========================================================================================

/** * --- ENUM DE CONTEXTO DEL HUD --- */
enum class HUDContext {
    HOME, BUDGETS, BUDGETS_TENDERS, BUDGETS_DIRECT, CHAT, CALENDAR, PROMO, TENDER_DETAILS, PROFILE, SEARCH_RESULTS, FAST, UNKNOWN
}

/** * --- ENUM PARA EL ESTADO DE NAVEGACIÓN INICIAL --- */
enum class InitialNavTarget {
    CHECKING, LOGIN, MAIN_SCREEN, PROFILE_EDIT
}

// 🔥 EL MODELO SuperCategory se define en CategoryViewModel.kt para evitar duplicidad 🔥

/** 
 * --- BE BRAIN VIEWMODEL (EL CEREBRO / INTERMEDIARIO) ---
 * Centraliza el estado global y actúa como puente entre los Obreros (ViewModels de cálculo) 
 * y la Interfaz de Usuario.
 * 
 * Este ViewModel NO realiza cálculos pesados, solo sincroniza y expone datos procesados.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BeBrainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager,
    private val chatRepository: ChatRepository,
    private val budgetRepository: BudgetRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    // ======================================================================================
    // --- CHAT Y NOTIFICACIONES GLOBALES ---
    // ======================================================================================
    val totalUnreadCount: StateFlow<Int> = userRepository.userProfile
        .flatMapLatest { user: UserEntity? ->
            if (user != null) chatRepository.getTotalUnreadCount(user.id)
            else kotlinx.coroutines.flow.flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** 🔥 ESTADO DEL PERFIL DE USUARIO (OBSERVADO DIRECTAMENTE DESDE EL REPOSITORIO) 🔥 */
    val userState: StateFlow<UserEntity?> = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val hasChatNotifications: StateFlow<Boolean> = totalUnreadCount
        .map { it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasBudgetNotifications: StateFlow<Boolean> = budgetRepository.allBudgets
        .map { list: List<BudgetEntity> -> list.any { it.status.name == "PENDIENTE" && !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasCalendarNotifications: StateFlow<Boolean> = calendarRepository.allEvents
        .map { list: List<com.example.myapplication.data.local.CalendarEventEntity> -> list.any { it.status.name == "PENDIENTE" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasPromoNotifications: StateFlow<Boolean> = kotlinx.coroutines.flow.flowOf(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val unreadCountsMap: StateFlow<Map<String, Int>> = userRepository.userProfile
        .flatMapLatest { user: UserEntity? ->
            if (user != null) chatRepository.getUnreadCountsPerChat(user.id)
            else kotlinx.coroutines.flow.flowOf(emptyList<ChatUnreadCount>())
        }
        .map { list: List<ChatUnreadCount> -> list.associate { it.chatId to it.count } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private fun startGlobalChatSync(userId: String) {
        chatRepository.startGlobalListening(userId)
        chatRepository.setUserOnline(userId, true)
    }

    private fun stopGlobalChatSync() {
        chatRepository.stopGlobalListening()
    }

    override fun onCleared() {
        super.onCleared()
        stopGlobalChatSync()
        val user = authRepository.getCurrentUser()
        if (user != null) {
            chatRepository.setUserOnline(user.uid, false)
        }
    }

    // ======================================================================================
    // --- 1. ESTADO DE NAVEGACIÓN Y AUTH (DECISIONES ESTRATÉGICAS) ---
    // ======================================================================================
    private val _favoriteProvidersRaw = MutableStateFlow<List<Provider>>(emptyList())
    private val _allBudgetsRaw = MutableStateFlow<List<BudgetEntity>>(emptyList())
    private val _allTendersRaw = MutableStateFlow<List<TenderEntity>>(emptyList())
    private val _allProvidersRaw = MutableStateFlow<List<ServiceDisplayModel>>(emptyList())
    val allProvidersRaw: StateFlow<List<ServiceDisplayModel>> = _allProvidersRaw.asStateFlow()

    private val _initialNavTarget = MutableStateFlow(InitialNavTarget.CHECKING)
    val initialNavTarget: StateFlow<InitialNavTarget> = _initialNavTarget.asStateFlow()

    private val _isFirstTime = MutableStateFlow(tokenManager.isFirstTime())
    val isFirstTime: StateFlow<Boolean> = _isFirstTime.asStateFlow()

    val targetUserName: StateFlow<String> = userState
        .map { it?.displayName ?: "Usuario Maverick" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Usuario Maverick")

    /**
     * ESTRATEGIA: Verificación inicial de sesión y perfil.
     */
    fun performInitialAuthCheck() {
        viewModelScope.launch {
            delay(2000) // Tiempo para que Be "despierte"
            val currentUser = authRepository.getCurrentUser()
            
            if (currentUser == null) {
                _initialNavTarget.value = InitialNavTarget.LOGIN
            } else {
                // Verificamos si tiene perfil en Firestore para decidir si va a Main o ProfileEdit
                val profile = authRepository.getFullUserProfile(currentUser.uid)
                if (profile == null) {
                    _initialNavTarget.value = InitialNavTarget.PROFILE_EDIT
                } else {
                    _initialNavTarget.value = InitialNavTarget.MAIN_SCREEN
                }
            }
        }
    }

    fun completeFirstTime() {
        tokenManager.setFirstTimeCompleted()
        _isFirstTime.value = false
    }

    // ======================================================================================
    // --- 2. GESTIÓN DE CATEGORÍAS Y ORDENAMIENTO ---
    // ======================================================================================
    private val _allCategoriesRaw = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val allCategories: StateFlow<List<CategoryEntity>> = _allCategoriesRaw.asStateFlow()

    private val _sortedCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val sortedCategories: StateFlow<List<CategoryEntity>> = _sortedCategories.asStateFlow()

    private val _superCategories = MutableStateFlow<List<SuperCategory>>(emptyList())
    val superCategories: StateFlow<List<SuperCategory>> = _superCategories.asStateFlow()

    private val _activeSortFilters = MutableStateFlow<Set<String>>(setOf("view_bento", "sort_hot"))
    val activeSortFilters: StateFlow<Set<String>> = _activeSortFilters.asStateFlow()

    private val _selectedSuperCategory = MutableStateFlow<SuperCategory?>(null)
    val selectedSuperCategory: StateFlow<SuperCategory?> = _selectedSuperCategory.asStateFlow()

    // --- ESTADO DE UBICACIÓN Y CLIMA (SINCRONIZADO DESDE UBICACIONCLIMAVIEWMODEL) ---
    private val _temperature = MutableStateFlow("--°C")
    val temperature: StateFlow<String> = _temperature.asStateFlow()

    private val _weatherEmoji = MutableStateFlow("🌤️")
    val weatherEmoji: StateFlow<String> = _weatherEmoji.asStateFlow()

    private val _weatherDescription = MutableStateFlow("Cargando...")
    val weatherDescription: StateFlow<String> = _weatherDescription.asStateFlow()

    private val _locationName = MutableStateFlow("Actualizando...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    // ======================================================================================
    // --- 3. MÉTODOS DE SINCRONIZACIÓN (PUENTE CEREBRO-OBRERO) ---
    // ======================================================================================

    /** 🔥 Sincroniza la lista completa de categorías desde el Obrero */
    fun syncAllCategories(list: List<CategoryEntity>) {
        _allCategoriesRaw.value = list
    }

    fun hydrateCategories(list: List<CategoryEntity>) {
        _allCategoriesRaw.value = list
    }

    fun updateTenders(tenders: List<TenderEntity>) {
        _allTendersRaw.value = tenders
    }

    fun updateBudgets(budgets: List<BudgetEntity>) {
        _allBudgetsRaw.value = budgets
    }

    /** Sincroniza los proveedores unificados desde el Obrero Provider */
    fun syncProviders(providers: List<ServiceDisplayModel>) {
        _allProvidersRaw.value = providers
        // También sincronizamos los favoritos crudos para la búsqueda global si es necesario
        _favoriteProvidersRaw.value = emptyList() // Opcional: Podríamos extraer Providers reales si se requiere
    }

    /** El Cerebro recibe y guarda las categorías ya procesadas por el Obrero */
    fun syncCategories(sorted: List<CategoryEntity>, superCats: List<SuperCategory>) {
        _sortedCategories.value = sorted
        _superCategories.value = superCats
    }

    /** Sincroniza los filtros que el Obrero está aplicando */
    fun syncFilters(filters: Set<String>) {
        _activeSortFilters.value = filters
        _isSuperCategoryView.value = filters.contains("view_bento")
    }

    /** Sincroniza el clima calculado por el Obrero */
    fun syncWeather(temp: String, emoji: String, desc: String, city: String) {
        _temperature.value = temp
        _weatherEmoji.value = emoji
        _weatherDescription.value = desc
        _locationName.value = city
    }

    fun updateProfile(user: UserEntity?) { 
        // Ya no es necesario actualizar manualmente ya que observamos el Repositorio directamente
    }
    
    /** SELECCIÓN DE SUPER CATEGORÍA (ORQUESTADO) */
    fun selectSuperCategory(superCategory: SuperCategory?) { _selectedSuperCategory.value = superCategory }

    // ======================================================================================
    // --- 4. ESTADOS DEL HUD Y VISIBILIDAD ---
    // ======================================================================================
    private val _isSuperCategoryView = MutableStateFlow(true)
    val isSuperCategoryView: StateFlow<Boolean> = _isSuperCategoryView.asStateFlow()

    private val _showWeatherDetails = MutableStateFlow(false)
    val showWeatherDetails: StateFlow<Boolean> = _showWeatherDetails.asStateFlow()

    private val _showFavoritesPanel = MutableStateFlow(false)
    val showFavoritesPanel: StateFlow<Boolean> = _showFavoritesPanel.asStateFlow()

    private val _isBottomBarVisible = MutableStateFlow(true)
    val isBottomBarVisible: StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()

    // Flag para saber si la barra fue ocultada manualmente por una pantalla (ej: Chat)
    // y no por el sistema de búsqueda de Be.
    private val _isBottomBarForcedHidden = MutableStateFlow(false)

    private val _isResultadoVisible = MutableStateFlow(false)
    val isResultadoVisible: StateFlow<Boolean> = _isResultadoVisible.asStateFlow()

    private val _isUIBlocked = MutableStateFlow(false)
    val isUIBlocked: StateFlow<Boolean> = _isUIBlocked.asStateFlow()

    private val _currentContext = MutableStateFlow(HUDContext.HOME)
    val currentContext: StateFlow<HUDContext> = _currentContext.asStateFlow()

    fun toggleWeatherDetails() { _showWeatherDetails.value = !_showWeatherDetails.value }
    fun setWeatherDetailsVisible(visible: Boolean) { _showWeatherDetails.value = visible }
    fun toggleFavoritesPanel() { _showFavoritesPanel.value = !_showFavoritesPanel.value }
    fun setFavoritesPanelVisible(visible: Boolean) { _showFavoritesPanel.value = visible }
    fun setBottomBarVisible(visible: Boolean) { 
        _isBottomBarVisible.value = visible 
        // Si se oculta desde una pantalla, marcamos el flag para que Be no la restaure al "despertar"
        _isBottomBarForcedHidden.value = !visible
    }
    fun setResultadoVisible(visible: Boolean) { if (visible && !_isUIBlocked.value) _isResultadoVisible.value = visible }
    fun setUIBlocked(blocked: Boolean) { _isUIBlocked.value = blocked; if (blocked && _isSearchActive.value) _isResultadoVisible.value = false }
    fun openKeyboard() { _requestKeyboard.value = true }
    fun closeKeyboard() { _requestKeyboard.value = false }
    fun triggerAction(actionId: String) { 
        viewModelScope.launch { 
            // --- SECCIÓN: ORQUESTACIÓN DE ACCIONES ---
            // Las acciones se emiten para que los "Obreros" (otros ViewModels) las procesen.
            if (actionId.startsWith("chat_")) {
                val providerId = actionId.removePrefix("chat_")
                _actionEvent.emit(actionId)
            } else if (actionId.startsWith("talk_")) {
                _actionEvent.emit(actionId)
            } else {
                // Emisión directa (ej: sort_hot, clear_filters, etc.)
                _actionEvent.emit(actionId) 
            }
        } 
    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        val context = _currentContext.value
        if (query.isNotEmpty() && !_isUIBlocked.value && (context == HUDContext.HOME || context == HUDContext.BUDGETS || context == HUDContext.BUDGETS_TENDERS || context == HUDContext.BUDGETS_DIRECT || context == HUDContext.TENDER_DETAILS)) {
            _isResultadoVisible.value = true
            if (context == HUDContext.HOME) _isBottomBarVisible.value = false
        }
    }
    // ======================================================================================
    // --- 5. BE ASSISTANT (BÚSQUEDA Y MENSAJES) ---
    // ======================================================================================
    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showBe = MutableStateFlow(true)
    val showBe: StateFlow<Boolean> = _showBe.asStateFlow()

    private val _isBeDormido = MutableStateFlow(false)
    val isBeDormido: StateFlow<Boolean> = _isBeDormido.asStateFlow()

    private val _beState = MutableStateFlow(BeState.IDLE)
    val beState: StateFlow<BeState> = _beState.asStateFlow()

    private val _currentTipIndex = MutableStateFlow(0)
    val currentTipIndex: StateFlow<Int> = _currentTipIndex.asStateFlow()

    private val _beMessages = MutableStateFlow<List<BeMessage>>(emptyList())
    val beMessages: StateFlow<List<BeMessage>> = _beMessages.asStateFlow()

    // --- ESTADO DE VISIBILIDAD DE LA BURBUJA (BADGE DE CONVERSACIÓN) ---
    private val _isBubbleMuted = MutableStateFlow(false)
    val isBubbleMuted: StateFlow<Boolean> = _isBubbleMuted.asStateFlow()

    private val _hasNewMessage = MutableStateFlow(false)
    val hasNewMessage: StateFlow<Boolean> = _hasNewMessage.asStateFlow()

    private val _resetBePositionTrigger = MutableStateFlow(0)
    val resetBePositionTrigger: StateFlow<Int> = _resetBePositionTrigger.asStateFlow()

    private val _requestKeyboard = MutableStateFlow(false)
    val requestKeyboard = _requestKeyboard.asStateFlow()

    // ======================================================================================
    // --- 5. ACCIONES Y HERRAMIENTAS ---
    // ======================================================================================
    private val _showBeTools = MutableStateFlow(false)
    val showBeTools: StateFlow<Boolean> = _showBeTools.asStateFlow()

    // --- NUEVO: ESTADO PARA DIÁLOGO DE SIMULACIÓN DE PRESTADORES ---
    private val _showProviderSimDialog = MutableStateFlow(false)
    val showProviderSimDialog: StateFlow<Boolean> = _showProviderSimDialog.asStateFlow()

    // 🔥 HERRAMIENTA DE UBICACIÓN CENTRALIZADA EN EL CEREBRO 🔥
    private val _showLocationTool = MutableStateFlow(false)
    val showLocationTool: StateFlow<Boolean> = _showLocationTool.asStateFlow()

    // Guardamos el ID de la dirección seleccionada o un objeto especial para GPS
    private val _selectedAddressId = MutableStateFlow<String?>(null)
    val selectedAddressId: StateFlow<String?> = _selectedAddressId.asStateFlow()

    private val _gpsAddressOverride = MutableStateFlow<AddressInfo?>(null)

    // ======================================================================================
    // --- MAPEADO DE DIRECCIONES (Ssingle Source of Truth) ---
    // ======================================================================================
    
    /**
     * TRABAJO SUCIO (Mapeo): Derivamos la lista de direcciones directamente del estado del usuario.
     * Esto asegura disponibilidad instantánea y coherencia global sin depender de sincronizaciones externas.
     */
    val availableAddressInfos: StateFlow<List<AddressInfo>> = userState.map { user ->
        if (user == null) return@map emptyList<AddressInfo>()
        val list = mutableListOf<AddressInfo>()
        
        // 1. Mapeo de Direcciones Personales
        user.personalAddresses.forEach { addr ->
            list.add(AddressInfo(
                id = addr.id,
                companyOrUserName = user.displayName,
                branchName = addr.label.ifEmpty { "Mi Ubicación" },
                streetAndNumber = "${addr.calle} ${addr.numero}",
                locality = addr.localidad,
                province = addr.provincia,
                country = "Argentina", 
                postalCode = addr.codigoPostal,
                isCompany = false,
                lat = addr.latitude,
                lng = addr.longitude
            ))
        }
        
        // 2. Mapeo de Direcciones de Empresas y sus Sucursales
        user.companies.forEach { company ->
            company.branches.forEach { branch ->
                list.add(AddressInfo(
                    id = branch.id,
                    companyOrUserName = company.name,
                    branchName = branch.name,
                    streetAndNumber = "${branch.address.calle} ${branch.address.numero}",
                    locality = branch.address.localidad,
                    province = branch.address.provincia,
                    country = "Argentina",
                    postalCode = branch.address.codigoPostal,
                    isCompany = true,
                    lat = branch.address.latitude,
                    lng = branch.address.longitude
                ))
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * FUENTE DE VERDAD ÚNICA: Dirección Activa (Modelo de Datos)
     * Combina la selección del usuario con los datos derivados del perfil o GPS.
     */
    val activeAddress: StateFlow<AddressInfo?> = combine(
        _selectedAddressId, _gpsAddressOverride, availableAddressInfos
    ) { selectedId, gpsOverride, allAddresses ->
        // Prioridad 1: Override de GPS Activo
        if (selectedId == "gps_current" && gpsOverride != null) return@combine gpsOverride
        
        // Prioridad 2: Selección del Usuario en la lista persistente
        val found = allAddresses.find { it.id == selectedId }
        if (found != null) return@combine found
        
        // Prioridad 3: Fallback a la primera dirección del perfil (Default)
        allAddresses.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * FUENTE DE VERDAD ÚNICA (VISUAL): LocationOption
     * Derivado directamente de activeAddress para asegurar que la UI (Header/Popups) 
     * siempre muestre lo mismo que los filtros de búsqueda.
     */
    val selectedLocation: StateFlow<LocationOption?> = activeAddress.map { info ->
        info?.toLocationOption()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Selecciona una dirección específica del listado */
    fun selectAddress(addressId: String) {
        _selectedAddressId.value = addressId
        _gpsAddressOverride.value = null
    }

    /** Actualiza la dirección con datos frescos del GPS (Obrero) */
    fun updateAddressFromGps(address: AddressInfo) {
        _gpsAddressOverride.value = address
        _selectedAddressId.value = "gps_current"
    }

    /** Método legacy para compatibilidad durante transición, pronto a deprecado */
    fun syncAvailableAddresses(list: List<AddressInfo>) {
        // Ya no hace nada, usamos availableAddressInfos derivado
    }

    private val _toolboxKey = MutableStateFlow("home_default")
    val toolboxKey: StateFlow<String> = _toolboxKey.asStateFlow()

    private val _actionEvent = MutableSharedFlow<String>()
    val actionEvent = _actionEvent.asSharedFlow()

    private val _currentActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())
    val currentActions: StateFlow<List<BeSmallActionModel>> = _currentActions.asStateFlow()

    private val _customActions = MutableStateFlow<List<BeSmallActionModel>>(emptyList())

    // ======================================================================================
    // --- 6. MULTISELECCIÓN Y FILTROS ---
    // ======================================================================================
    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive: StateFlow<Boolean> = _isMultiSelectionActive.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedItemIds: StateFlow<Set<String>> = _selectedItemIds.asStateFlow()

    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters: StateFlow<Set<String>> = _activeFilters.asStateFlow()

    // --- FILTROS DINÁMICOS POR CONTEXTO ---
    val availableFilters: StateFlow<List<ControlItem>> = _currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(BeMenuRegistry.FILTER_PRODUCTS, BeMenuRegistry.FILTER_SERVICES, BeMenuRegistry.FILTER_24H, BeMenuRegistry.FILTER_LOCAL)
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(BeMenuRegistry.FILTER_TENDER_ACTIVE, BeMenuRegistry.FILTER_TENDER_CLOSED, BeMenuRegistry.FILTER_TENDER_CANCELED, BeMenuRegistry.FILTER_TENDER_AWARDED)
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> listOf(BeMenuRegistry.FILTER_SUBSCRIBED, BeMenuRegistry.FILTER_FAVORITE)
            HUDContext.CHAT, HUDContext.SEARCH_RESULTS, HUDContext.FAST -> listOf(
                BeMenuRegistry.FILTER_SUBSCRIBED,
                BeMenuRegistry.FILTER_FAVORITE,
                BeMenuRegistry.FILTER_ONLINE,
                BeMenuRegistry.FILTER_PRODUCTS,
                BeMenuRegistry.FILTER_SERVICES,
                BeMenuRegistry.FILTER_24H,
                BeMenuRegistry.FILTER_SHIPPING,
                BeMenuRegistry.FILTER_VISITS,
                BeMenuRegistry.FILTER_LOCAL,
                BeMenuRegistry.FILTER_APPOINTMENTS
            )
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val availableSortOptions: StateFlow<List<ControlItem>> = _currentContext.map { context ->
        when (context) {
            HUDContext.HOME -> listOf(BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.VIEW_COMPACT)
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> listOf(BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.SORT_DATE, BeMenuRegistry.VIEW_COMPACT)
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> listOf(BeMenuRegistry.SORT_ALPHA, BeMenuRegistry.SORT_DATE, BeMenuRegistry.SORT_PRICE)
            HUDContext.CHAT, HUDContext.SEARCH_RESULTS, HUDContext.FAST -> listOf(
                BeMenuRegistry.SORT_ALPHA,
                BeMenuRegistry.SORT_DATE,
                BeMenuRegistry.SORT_DISTANCE,
                BeMenuRegistry.SORT_RATING
            )
            else -> listOf(BeMenuRegistry.SORT_ALPHA)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dynamicCategories: StateFlow<List<ControlItem>> = combine(
        _currentContext, _allCategoriesRaw, _allTendersRaw, _allBudgetsRaw
    ) { context, allCats, tenders, budgets ->
        val filtered = when (context) {
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                val categoryNamesInTenders = tenders.map { it.category.lowercase().trim() }.toSet()
                allCats.filter { it.name.lowercase().trim() in categoryNamesInTenders }
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                val categoryNamesInBudgets = budgets.mapNotNull { it.category?.lowercase()?.trim() }.toSet()
                allCats.filter { it.name.lowercase().trim() in categoryNamesInBudgets }
            }
            else -> allCats
        }
        filtered.map { cat -> 
            ControlItem(
                label = cat.name, 
                icon = null, 
                emoji = cat.icon ?: "📁", 
                color = Color(CategoryVisuals.getColorFor(cat.superCategory)),
                id = "cat_${cat.name.lowercase().trim()}"
            ) 
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ======================================================================================
    // --- 7. CONECTIVIDAD, PERMISOS Y RESILIENCIA (EL CEREBRO DE LA APP) ---
    // ======================================================================================
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // --- SECCIÓN: ESTADOS TÉCNICOS DE RED Y CHAT (ESTADO DEL OBRERO TÉCNICO) ---
    private val _isWifiEnabled = MutableStateFlow(true)
    val isWifiEnabled = _isWifiEnabled.asStateFlow()

    private val _isCellularEnabled = MutableStateFlow(true)
    val isCellularEnabled = _isCellularEnabled.asStateFlow()

    private val _isChatConnected = MutableStateFlow(false) // Preparado para futura implementación de Chat
    val isChatConnected = _isChatConnected.asStateFlow()

    // Estados de permisos globales centralizados en el Cerebro (BeBrain)
    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted = _locationPermissionGranted.asStateFlow()

    private val _notificationPermissionGranted = MutableStateFlow(false)
    val notificationPermissionGranted = _notificationPermissionGranted.asStateFlow()

    private val _gpsEnabled = MutableStateFlow(false)
    val gpsEnabled = _gpsEnabled.asStateFlow()

    /**
     * Actualiza el estado de los permisos de ubicación.
     */
    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    /**
     * Actualiza el estado de los permisos de notificación.
     */
    fun setNotificationPermissionGranted(granted: Boolean) {
        _notificationPermissionGranted.value = granted
    }

    /**
     * Actualiza el estado del GPS (hardware).
     */
    fun setGpsEnabled(enabled: Boolean) {
        _gpsEnabled.value = enabled
    }

    // --- SECCIÓN: SINCRONIZACIÓN TÉCNICA (WIFI, RED, CHAT) ---
    
    fun setWifiEnabled(enabled: Boolean) {
        _isWifiEnabled.value = enabled
    }

    fun setCellularEnabled(enabled: Boolean) {
        _isCellularEnabled.value = enabled
    }

    fun setChatConnected(connected: Boolean) {
        _isChatConnected.value = connected
    }

    /**
     * Actualiza el estado de conecitividad global.
     * Dispara reacciones en Be (emociones y mensajes).
     */
    fun setOfflineStatus(offline: Boolean) {
        if (_isOffline.value == offline) return
        _isOffline.value = offline

        if (offline) {
            // --- REACCIÓN INMEDIATA DE BE ---
            _beState.value = BeState.TALKING
            _currentTipIndex.value = 0 // El primer mensaje en offline es la alerta
        } else {
            // --- REACCIÓN AL VOLVER ---
            _beState.value = BeState.NOTIFICATION_READY
        }
        updateBeContextMessages(_lastRoute ?: "home")
    }

    private var _lastRoute: String? = null

    // ======================================================================================
    // --- LÓGICA DE ACTUALIZACIÓN Y LOOP DE NOTIFICACIONES ---
    // ======================================================================================
    init { startBeBrainLoop() }

    private fun startBeBrainLoop() {
        viewModelScope.launch {
            while (true) {
                if (_isBeDormido.value) { _beState.value = BeState.IDLE; delay(2000); continue }
                when (_beState.value) {
                    BeState.IDLE -> {
                        delay((6000..15000).random().toLong())
                        if (_beMessages.value.isNotEmpty() && !_isSearchActive.value) {
                            _currentTipIndex.value = _beMessages.value.indices.random()
                            _beState.value = BeState.NOTIFICATION_READY
                        }
                    }
                    BeState.NOTIFICATION_READY -> { delay(12000); if (_beState.value == BeState.NOTIFICATION_READY) _beState.value = BeState.IDLE }
                    BeState.TALKING -> delay(1000)
                }
            }
        }
    }


    // ======================================================================================
    // --- BE ASSISTANT GESTOS ---
    // ======================================================================================
    
    /**
     * TOQUE SIMPLE EN BE:
     * - Si está dormido: Despierta parcialmente (se muestra completo).
     * - Si está despierto: Activa el modo búsqueda.
     */
    fun onBeClick() { 
        if (_isBeDormido.value) {
            _isBeDormido.value = false 
            // Podríamos agregar un efecto de sonido o vibración aquí
        } else {
            setSearchActive(!_isSearchActive.value) 
        }
    }

    /**
     * DOBLE TOQUE EN BE:
     * - Si está dormido: Despierta y vuelve a su posición original (0,0).
     * - Si está despierto: Entra en modo hibernación (Mitad oculto, transparente).
     */
    fun onBeDoubleClick() { 
        if (_isBeDormido.value) {
            _isBeDormido.value = false
            // Disparamos el trigger para que BeAssistantViewModel resetee la posición a casa
            _resetBePositionTrigger.value += 1 
        } else {
            _isBeDormido.value = true
            cerrarBeAssistantCompleto()
        }
    }

    fun setSearchActive(active: Boolean) {
        if (active) {
            _isSearchActive.value = true; _showBeTools.value = false; _beState.value = BeState.IDLE; openKeyboard()
            // Se elimina _isResultadoVisible.value = true para que no se abra la pantalla BeResultadoScreen
            // ya que ahora los resultados se integrarán en la burbuja BeSearch
            if (!_isUIBlocked.value && (_currentContext.value == HUDContext.HOME || _currentContext.value == HUDContext.SEARCH_RESULTS || _currentContext.value == HUDContext.FAST)) {
                _isBottomBarVisible.value = false
            }
        } else cerrarBeAssistantCompleto()
    }

    fun cerrarBeAssistantCompleto() {
        _isSearchActive.value = false; _searchQuery.value = ""; _isResultadoVisible.value = false
        
        // --- SECCIÓN: RESTAURACIÓN DINÁMICA DE BARRAS ---
        // Restauramos la barra solo si no hay una pantalla (como el chat) que haya pedido ocultarla.
        if (!_isBottomBarForcedHidden.value) {
            _isBottomBarVisible.value = true
        }
        
        _showBeTools.value = false; _isMultiSelectionActive.value = false; _selectedItemIds.value = emptySet()
        _beState.value = BeState.IDLE; closeKeyboard()
    }

    fun toggleBubbleMute() {
        _isBubbleMuted.value = !_isBubbleMuted.value
        // Al silenciar/desilenciar manualmente, quitamos la alerta de mensaje nuevo
        _hasNewMessage.value = false
    }

    /**
     * Marca si hay un mensaje nuevo pendiente de leer (para el efecto de sacudida en el badge)
     */
    fun setHasNewMessage(hasNew: Boolean) {
        _hasNewMessage.value = hasNew
    }

    fun onBeLongClick() {
        if (!_isBeDormido.value) {
            val nextToolsState = !_showBeTools.value
            if (nextToolsState) {
                if (!_isMultiSelectionActive.value) cerrarBeAssistantCompleto()
                else { _isSearchActive.value = false; _searchQuery.value = ""; _isResultadoVisible.value = false }
                _showBeTools.value = true; updateActionsForContext(_currentContext.value)
            } else _showBeTools.value = false
        }
        updateToolboxKey()
    }

    fun setBeState(state: BeState) { _beState.value = state }
    fun nextTip() { if (_currentTipIndex.value < _beMessages.value.size - 1) _currentTipIndex.value++ }
    fun prevTip() { if (_currentTipIndex.value > 0) _currentTipIndex.value-- }
    fun setShowBeTools(visible: Boolean) { _showBeTools.value = visible; if (visible) updateActionsForContext(_currentContext.value); updateToolboxKey() }

    // --- NUEVOS CONTROLES PARA SIMULACIÓN ---
    fun setShowProviderSimDialog(visible: Boolean) { _showProviderSimDialog.value = visible }
    private fun updateToolboxKey() { val context = _currentContext.value.name.lowercase(); val mode = if (_showBeTools.value) "tools" else "default"; _toolboxKey.value = "${context}_${mode}" }

    // ======================================================================================
    // --- SENSOR DE CONTEXTO ---
    // ======================================================================================
    fun onRouteChanged(route: String?) {
        val currentRoute = route ?: return
        _lastRoute = currentRoute
        val newContext = when {
            currentRoute.contains("home") -> HUDContext.HOME
            currentRoute.contains("presupuestos") -> HUDContext.BUDGETS
            currentRoute.contains("chat") -> HUDContext.CHAT
            currentRoute.contains("calendar") -> HUDContext.CALENDAR
            currentRoute.contains("perfil_cliente") -> HUDContext.PROFILE
            currentRoute.contains("result_busqueda") -> HUDContext.SEARCH_RESULTS
            currentRoute.contains("fast") -> HUDContext.FAST
            else -> HUDContext.UNKNOWN
        }
        if (_currentContext.value != newContext) {
            _currentContext.value = newContext; _customActions.value = emptyList()
            
            // --- SECCIÓN: CONTROL DE VISIBILIDAD POR CONTEXTO ---
            // En HUD V5, permitimos que la barra sea visible por defecto. 
            // Reseteamos el flag de ocultación forzada al cambiar de pantalla.
            _isBottomBarForcedHidden.value = false
            _isBottomBarVisible.value = true

            // 🔥 RESET: Cerramos búsqueda al cambiar de pantalla para evitar desincronización
            if (_isSearchActive.value) {
                cerrarBeAssistantCompleto()
            }

            // 🔥 Por defecto, en resultados de búsqueda y FAST, la herramienta de ubicación está ON
            _showLocationTool.value = (newContext == HUDContext.SEARCH_RESULTS || newContext == HUDContext.FAST)
        }
        _showBe.value = !(currentRoute == "login" || currentRoute == "register" || currentRoute == "startup")
        _isResultadoVisible.value = false; _showBeTools.value = false
        updateActionsForContext(newContext); updateBeContextMessages(currentRoute); updateToolboxKey()
    }

    fun setHUDContext(context: HUDContext) {
        if ((_currentContext.value == HUDContext.HOME || _currentContext.value == HUDContext.CHAT || _currentContext.value == HUDContext.CALENDAR) && context != _currentContext.value) return
        if (_currentContext.value != context) { cerrarBeAssistantCompleto(); clearFilters() }
        _currentContext.value = context; updateActionsForContext(context); updateToolboxKey()
    }

    private fun updateActionsForContext(context: HUDContext) {
        val actions = mutableListOf<BeSmallActionModel>()
        if (context == HUDContext.HOME) {
            actions.add(BeSmallActionModel("sim_chat", Icons.AutoMirrored.Filled.Chat, "Sim Chat", emoji = "💬") { triggerAction("sim_chat") })
            actions.add(BeSmallActionModel("sim_tender", Icons.Default.Gavel, "Sim Licit", emoji = "⚖️") { triggerAction("sim_tender") })
            actions.add(BeSmallActionModel("sim_massive", Icons.Default.PersonAdd, "Sim Prov", emoji = "👥") { triggerAction("sim_massive") })
            actions.add(BeSmallActionModel("migrate_cats", Icons.Default.CloudUpload, "Migrar", emoji = "☁️") { triggerAction("migrate_cats") })
            actions.add(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true) { triggerAction("fast") })
            actions.add(BeSmallActionModel("licit", Icons.Default.Gavel, "Licitación", emoji = "⚖️", isDefault = true) { triggerAction("licit") })
            actions.add(BeSmallActionModel("fav", Icons.Default.Favorite, "Favoritos", emoji = "❤️", isDefault = true) { triggerAction("fav") })
            actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
        } else if (context == HUDContext.PROFILE) {
            if (_customActions.value.isNotEmpty()) actions.addAll(_customActions.value.map { if (it.id.contains("divider_v")) it else it.copy(onClick = { triggerAction(it.id) }) })
            else {
                actions.add(BeSmallActionModel("edit_profile", Icons.Default.Edit, "Editar", emoji = "✏️", isDefault = true) { triggerAction("edit_profile") })
                actions.add(BeSmallActionModel("settings_profile", Icons.Default.Settings, "Ajustes", emoji = "⚙️", isDefault = true) { triggerAction("settings_profile") })
            }
        } else if (context == HUDContext.SEARCH_RESULTS || context == HUDContext.FAST) {
            // 🔥 BOTÓN PARA ACTIVAR LA HERRAMIENTA EN EL MENÚ 🔥
            // actions.add(BeSmallActionModel("location_tool", Icons.Default.LocationOn, "Ubicación", emoji = "📍", isDefault = true) { setShowLocationTool(!_showLocationTool.value) })
            actions.add(BeSmallActionModel("share", Icons.Default.Share, "Compartir", emoji = "📤") { })
        } else actions.addAll(_customActions.value)
        _currentActions.value = actions
    }

    private fun updateBeContextMessages(route: String) {
        val finalMessages = mutableListOf<BeMessage>()

        // ======================================================================================
        // --- LÓGICA DE MENSAJES DE CONECTIVIDAD (HUD V5) ---
        // ======================================================================================
        if (_isOffline.value) {
            finalMessages.add(BeMessage("⚠️", "Sin conexión. Búsquedas limitadas a la base de datos interna 🛠️", null, Color(0xFFEF4444), emotion = BeEmotion.ANGRY))
        } else if (_lastRoute != null && !_isOffline.value) {
            // Podríamos mostrar un mensaje de "Volvimos" brevemente, pero por ahora mantenemos el flujo normal
        }

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 21 || currentHour < 6) finalMessages.add(BeMessage("🌙", "Es tarde. Si tienes una urgencia, usa Maverick FAST.", "PROBAR FAST", Color(0xFFF59E0B), emotion = BeEmotion.SURPRISED))
        finalMessages.addAll(when {
            route.contains("home") -> BeDictionary.HomeMessages
            route.contains("presupuestos") -> BeDictionary.BudgetMessages
            route.contains("chat") -> BeDictionary.ChatMessages
            route.contains("calendar") -> BeDictionary.CalendarMessages
            route.contains("result_busqueda") -> listOf(BeMessage("🔍", "Aquí tienes los prestadores de esta categoría.", null, Color(0xFF22D3EE)))
            route.contains("fast") -> listOf(BeMessage("⚡", "Búsqueda táctica activada. Solo unidades de respuesta inmediata.", null, Color(0xFF22D3EE)))
            else -> BeDictionary.DefaultMessages
        })
        _beMessages.value = finalMessages
    }

    fun syncMultiSelection(active: Boolean, selectedIds: Set<String>) {
        if (_currentContext.value == HUDContext.HOME && !active) { _isMultiSelectionActive.value = false; _selectedItemIds.value = emptySet(); return }
        val wasActive = _isMultiSelectionActive.value
        _isMultiSelectionActive.value = active; _selectedItemIds.value = selectedIds
        if (active && !wasActive) _showBeTools.value = true else if (!active && wasActive) _showBeTools.value = false
        updateActionsForContext(_currentContext.value); updateToolboxKey()
    }

    fun setCustomActions(actions: List<BeSmallActionModel>) { if (_currentContext.value != HUDContext.HOME) { _customActions.value = actions; updateActionsForContext(_currentContext.value) } }
    fun toggleMultiSelection() {
        val newState = !_isMultiSelectionActive.value
        _isMultiSelectionActive.value = newState
        if (newState) _showBeTools.value = true else { _selectedItemIds.value = emptySet(); _showBeTools.value = true }
        updateActionsForContext(_currentContext.value); updateToolboxKey()
    }
    fun toggleItemSelection(id: String) { val current = _selectedItemIds.value.toMutableSet(); if (!current.add(id)) current.remove(id); _selectedItemIds.value = current; updateActionsForContext(_currentContext.value) }
    fun selectAllItems(ids: List<String>) { _selectedItemIds.value = ids.toSet(); updateActionsForContext(_currentContext.value) }
    
    /** LIMPIEZA DE FILTROS Y BÚSQUEDA (CEREBRO ORQUESTADOR) */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedSuperCategory.value = null
        _activeFilters.value = emptySet()
        _selectedItemIds.value = emptySet()
        _isMultiSelectionActive.value = false
        // El cerebro decide que si se limpian filtros, Be debe volver a IDLE si no hay nada crítico
        if (_beState.value == BeState.NOTIFICATION_READY) _beState.value = BeState.IDLE
        // Notificamos a los obreros a través de la UI que deben limpiar sus filtros propios
        triggerAction("clear_filters")
    }

    fun clearSpecificFilters(prefixes: List<String>) {
        val current = _activeFilters.value.toMutableSet()
        val toRemove = current.filter { filterId -> prefixes.any { filterId.startsWith(it) } }
        if (toRemove.isNotEmpty()) {
            current.removeAll(toRemove.toSet())
            _activeFilters.value = current
        }
    }

    fun toggleFilter(filterId: String) {
        val current = _activeFilters.value.toMutableSet()
        if (!current.add(filterId)) current.remove(filterId)
        _activeFilters.value = current
    }

    // ======================================================================================
    // --- 8. BÚSQUEDA CONTEXTUAL (PUENTE CEREBRO-OBRERO) ---
    // ======================================================================================
    // El cerebro mantiene la lógica de búsqueda global que combina varios obreros.
    // Procesa los datos crudos que los obreros le sincronizaron.
    val searchResults: StateFlow<SearchResult> = combine(
        _searchQuery,
        _currentContext,
        _selectedSuperCategory,
        _superCategories,
        _sortedCategories,
        _favoriteProvidersRaw,
        _allTendersRaw,
        _allBudgetsRaw,
        _allProvidersRaw
    ) { args: Array<Any?> ->
        val query = args[0] as String
        val context = args[1] as HUDContext
        val selectedSuper = args[2] as SuperCategory?
        val allSuper = args[3] as List<SuperCategory>
        val sortedCats = args[4] as List<CategoryEntity>
        val favorites = args[5] as List<Provider>
        val tenders = args[6] as List<TenderEntity>
        val budgets = args[7] as List<BudgetEntity>
        val providers = args[8] as List<ServiceDisplayModel>

        if (query.isEmpty()) return@combine SearchResult.Empty
        val norm = query.lowercase().trim()

        when (context) {
            HUDContext.HOME -> {
                if (selectedSuper != null) {
                    val normQuery = query.prepareForSearch()
                    val filtered = selectedSuper.items.filter { it.name.wordStartsWithSmart(normQuery) }.sortedBy { it.name.lowercase() }
                    // 🔥 SI NO HAY COINCIDENCIAS, VOLVEMOS A LA LISTA COMPLETA
                    if (filtered.isEmpty()) SearchResult(categories = selectedSuper.items)
                    else SearchResult(categories = filtered)
                } else {
                    val normQuery = query.prepareForSearch()
                    val filteredSuper = allSuper.filter { superCat -> 
                        superCat.title.wordStartsWithSmart(normQuery) || 
                        superCat.items.any { it.name.wordStartsWithSmart(normQuery) } 
                    }
                    // 🔥 SI NO HAY COINCIDENCIAS EN SUPERCATEGORÍAS, VOLVEMOS A LA LISTA COMPLETA POR DEFECTO
                    if (filteredSuper.isEmpty()) SearchResult(superCategories = allSuper, categories = sortedCats, favorites = favorites)
                    else SearchResult(
                        superCategories = filteredSuper, 
                        categories = sortedCats, 
                        favorites = favorites.filter { it.displayName.wordStartsWithSmart(normQuery) }
                    )
                }
            }
            HUDContext.BUDGETS, HUDContext.BUDGETS_TENDERS -> {
                val filtered = tenders.filter { it.title.matchesSmart(norm) }
                // 🔥 FALLBACK A LISTA COMPLETA
                if (filtered.isEmpty()) SearchResult(tenders = tenders)
                else SearchResult(tenders = filtered)
            }
            HUDContext.BUDGETS_DIRECT, HUDContext.TENDER_DETAILS -> {
                val filtered = budgets.filter {
                    it.providerName.matchesSmart(norm) ||
                    it.providerCompanyName?.matchesSmart(norm) == true ||
                    it.grandTotal.toString().startsWith(norm) ||
                    it.budgetId.prepareForSearch().matchesSmart(norm)
                }
                // 🔥 FALLBACK A LISTA COMPLETA
                if (filtered.isEmpty()) SearchResult(budgets = budgets)
                else SearchResult(budgets = filtered)
            }
            HUDContext.SEARCH_RESULTS, HUDContext.FAST -> {
                val filtered = providers.filter { it.title.matchesSmart(norm) || it.subtitle?.matchesSmart(norm) == true }
                // 🔥 FALLBACK A LISTA COMPLETA
                if (filtered.isEmpty()) SearchResult(providers = providers)
                else SearchResult(providers = filtered)
            }
            else -> SearchResult.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResult.Empty)

    /**
     * MODELO ESTRUCTURADO DE RESULTADOS (EL CEREBRO SOLO ENTREGA DATA)
     */
    data class SearchResult(
        val categories: List<CategoryEntity> = emptyList(),
        val superCategories: List<SuperCategory> = emptyList(),
        val favorites: List<Provider> = emptyList(),
        val budgets: List<BudgetEntity> = emptyList(),
        val tenders: List<TenderEntity> = emptyList(),
        val providers: List<ServiceDisplayModel> = emptyList(),
        val genericItems: List<ControlItem> = emptyList()
    ) {
        companion object {
            val Empty = SearchResult()
        }
        fun isEmpty() = categories.isEmpty() && superCategories.isEmpty() && favorites.isEmpty() && 
                       budgets.isEmpty() && tenders.isEmpty() && providers.isEmpty() && genericItems.isEmpty()
    }

    // ======================================================================================
    // --- SECCIÓN: COORDINACIÓN DE EVENTOS ESPECIALES (BE ASSISTANT) ---
    // ======================================================================================

    /**
     * Procesa el envío de la búsqueda (Enter/Lupa).
     */
    fun onSearchSubmitted(interactionViewModel: BeInteractionViewModel) {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) return
        
        // El cerebro le pide al lóbulo frontal (Interaction) que procese la consulta final.
        // Se le pasan los resultados actuales calculados por el cerebro para decidir la reacción.
        interactionViewModel.onSearchSubmitted(
            query = query, 
            resultsFound = !searchResults.value.isEmpty(),
            onComplete = { reaction ->
                onProcessSubmissionComplete(reaction)
            }
        )
    }

    /**
     * Callback invocado por BeInteractionViewModel cuando termina de procesar el Enter.
     * Aquí el cerebro orquestador realiza las acciones visuales finales.
     */
    fun onProcessSubmissionComplete(reaction: BeSearchReaction) {
        viewModelScope.launch {
            // 1. Si Be tiene un mensaje para mostrar, lo ponemos como tip prioritario
            reaction.message?.let { msg ->
                _beMessages.value = listOf(msg)
                _currentTipIndex.value = 0
                setBeState(BeState.TALKING)
                setHasNewMessage(true)
            }
            
            // 2. Limpiamos la barra de búsqueda (Conversation Mode)
            _searchQuery.value = ""
            
            // 3. Si hay un actionId asociado (ej: Easter Egg final), lo disparamos
            reaction.actionId?.let { triggerAction(it) }
            
            // 4. Si hay resultados de búsqueda, nos aseguramos de que sean visibles
            if (!reaction.results.isEmpty()) {
                _isResultadoVisible.value = true
                closeKeyboard()
            }
        }
    }

    /**
     * Cambia el contexto a resultados de búsqueda para profundizar.
     */
    fun searchCategories(query: String) {
        // En HUD V5, la búsqueda profunda cambia el contexto para filtrar proveedores
        _currentContext.value = HUDContext.SEARCH_RESULTS
        // No cerramos la búsqueda para que BeSearch siga visible con los resultados
        closeKeyboard()
    }

    /**
     * Coordina el cierre del evento de Huevo de Pascua.
     */
    fun onEasterEggLinkClick() {
        cerrarBeAssistantCompleto()
        onRouteChanged(_lastRoute) // Restaurar mensajes normales
    }

    // 🔥 EXTENSIONES DE BÚSQUEDA UNIFICADAS (CEREBRO) 🔥
    private fun String.removeAccents(): String {
        val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
    }
    
    private fun String.prepareForSearch(): String = this.removeAccents().lowercase().trim()

    private fun String.wordStartsWithSmart(query: String): Boolean {
        if (query.isEmpty()) return false
        val normQuery = query.prepareForSearch()
        // Split por espacios y paréntesis
        return this.prepareForSearch().split(" ", "(", ")").any { it.startsWith(normQuery) }
    }

    fun String.matchesSmart(query: String): Boolean {
        if (query.isEmpty()) return false
        val normQuery = query.prepareForSearch()
        val textWords = this.prepareForSearch().split(" ", "(", ")").filter { it.isNotEmpty() }
        val queryWords = normQuery.split(" ", "(", ")").filter { it.isNotEmpty() }
        
        return queryWords.all { qw ->
            textWords.any { tw -> tw.startsWith(qw) }
        }
    }
}

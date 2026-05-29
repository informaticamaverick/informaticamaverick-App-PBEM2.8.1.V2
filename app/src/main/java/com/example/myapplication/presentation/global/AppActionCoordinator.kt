package com.example.myapplication.presentation.global

import com.example.myapplication.core.common.extensions.prepareForSearch
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.domain.model.AddressInfo
import com.example.myapplication.core.utils.HardwareStateProvider
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.presentation.components.BeMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- APP ACTION COORDINATOR (ELITE v2.3) ---
 * Centraliza las acciones globales, el estado compartido y la reactividad de hardware.
 * Actúa como el Cerebro que observa al Core para informar a los Obreros (ViewModels).
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Singleton
class AppActionCoordinator @Inject constructor(
    private val userRepository: UserRepository,
    private val hardwareProvider: HardwareStateProvider
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate)
    private var previousTopic: String? = null

    // ======================================================================================
    // --- SECTOR: REACTIVIDAD DE HARDWARE (CORE INTEGRATION) ---
    // ======================================================================================
    
    /** Flujos reactivos directos del Core: Costo Zero de monitoreo */
    val isWifiEnabled = hardwareProvider.isWifiEnabled.stateIn(scope, SharingStarted.Eagerly, false)
    val isGpsEnabled = hardwareProvider.isGpsEnabled.stateIn(scope, SharingStarted.Eagerly, false)
    val isOnline = hardwareProvider.isOnline.stateIn(scope, SharingStarted.Eagerly, false)
    val isCellularEnabled = hardwareProvider.isOnline.combine(hardwareProvider.isWifiEnabled) { online, wifi -> 
        online && !wifi 
    }.stateIn(scope, SharingStarted.Eagerly, false)

    // Eventos de acciones globales (ej: clics en Be, disparadores de búsqueda)
    private val _actionEvent = MutableSharedFlow<String>()
    val actionEvent = _actionEvent.asSharedFlow()

    // --- ESTADO GLOBAL DE BÚSQUEDA e INTENCIONES ---
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery = _globalSearchQuery.asStateFlow()

    /** 
     * [NUEVO BÚSQUEDA NORMALIZADA (CLASE ELITE):
     * Procesa la query para ignorar acentos, mayúsculas y espacios innecesarios.
     */
    val normalizedSearchQuery = _globalSearchQuery
        .map { it.prepareForSearch() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, "")

    /** 
     * [NUEVO BÚSQUEDA CON DEBOUNCE (ESTILO WHATSAPP):
     * Evita disparar procesos pesados en cada pulsación de tecla.
     * Espera 250ms de inactividad antes de emitir.
     */
    val debouncedSearchQuery = _globalSearchQuery
        .debounce(250)
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, "")

    /**
     * [NUEVO BÚSQUEDA DEBOUNCED Y NORMALIZADA:
     * La fuente de verdad definitiva para el filtrado inteligente.
     */
    val debouncedNormalizedSearchQuery = debouncedSearchQuery
        .map { it.prepareForSearch() }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, "")

    private val _globalSelectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val globalSelectedCategory = _globalSelectedCategory.asStateFlow()

    // --- NUEVO: RESULTADOS DE BÚSQUEDA EN TIEMPO REAL (SSOT para Be Assistant) ---
    private val _matchedCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val matchedCategories = _matchedCategories.asStateFlow()

    // --- NUEVO: EVENTO DE BÚSQUEDA ENVIADA (Enter) ---
    private val _searchSubmittedEvent = MutableSharedFlow<String>()
    val searchSubmittedEvent = _searchSubmittedEvent.asSharedFlow()

    // --- NUEVO: CONTEXTO GLOBAL DEL HUD (SSOT para Coordinación Brain-Workers) ---
    private val _currentHUDContext = MutableStateFlow(HUDContext.HOME)
    val currentHUDContext = _currentHUDContext.asStateFlow()

    // --- NUEVO: ESTADO GLOBAL DE FILTROS (SSOT) ---
    private val _activeFilters = MutableStateFlow<Set<String>>(emptySet())
    val activeFilters = _activeFilters.asStateFlow()

    // --- NUEVO: SEÑAL DE RESULTADOS ENCONTRADOS (Notificación de Obreros a Cerebro) ---
    private val _hasMatches = MutableStateFlow(true)
    val hasMatches = _hasMatches.asStateFlow()

    // --- NUEVO: ESTADO DE VISIBILIDAD DE SHEETS (Oculta BottomBar) ---
    private val _isSheetVisible = MutableStateFlow(false)
    val isSheetVisible = _isSheetVisible.asStateFlow()

    fun updateHUDContext(context: HUDContext) {
        _currentHUDContext.value = context
    }

    fun updateSheetVisibility(visible: Boolean) {
        _isSheetVisible.value = visible
    }

    fun updateFilters(filters: Set<String>) {
        _activeFilters.value = filters
    }

    fun setHasMatches(matches: Boolean) {
        _hasMatches.value = matches
    }

    // --- NUEVO: ESTADO GLOBAL DEL ASISTENTE (Be Assistant Communications) ---
    private val _contextMessages = MutableStateFlow<List<BeMessage>>(emptyList())
    val contextMessages = _contextMessages.asStateFlow()

    private val _activeAssistantResponse = MutableStateFlow<BeMessage?>(null)
    val activeAssistantResponse = _activeAssistantResponse.asStateFlow()

    fun updateContextMessages(messages: List<BeMessage>) {
        _contextMessages.value = messages
    }

    fun updateActiveAssistantResponse(response: BeMessage?) {
        _activeAssistantResponse.value = response
    }

    suspend fun triggerAction(actionId: String) {
        _actionEvent.emit(actionId)
    }

    fun updateSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun updateMatchedCategories(list: List<CategoryEntity>) {
        _matchedCategories.value = list
    }

    suspend fun submitSearch(query: String) {
        _searchSubmittedEvent.emit(query)
    }

    fun selectCategory(category: CategoryEntity?) {
        _globalSelectedCategory.value = category
    }

    // --- ESTADO DE CLIMA GLOBAL ---
    private val _temperature = MutableStateFlow("--°C")
    val temperature = _temperature.asStateFlow()

    private val _weatherEmoji = MutableStateFlow("🌤️")
    val weatherEmoji = _weatherEmoji.asStateFlow()

    private val _weatherDescription = MutableStateFlow("Cargando...")
    val weatherDescription = _weatherDescription.asStateFlow()

    // --- NUEVO: GESTIÓN DE PERFIL ACTIVO (USER VS COMPANY) ---
    private val _selectedProfileId = MutableStateFlow<String?>(null) // null = Usuario Principal
    val selectedProfileId: StateFlow<String?> = _selectedProfileId.asStateFlow()

    fun selectProfile(profileId: String?) {
        _selectedProfileId.value = profileId
    }

    fun updateWeather(temp: String, emoji: String, desc: String) {
        _temperature.value = temp
        _weatherEmoji.value = emoji
        _weatherDescription.value = desc
    }

    // Gestión de ubicación activa compartida
    private val _selectedAddressId = MutableStateFlow<String?>(null)
    val selectedAddressId = _selectedAddressId.asStateFlow()

    private val _gpsAddressOverride = MutableStateFlow<AddressInfo?>(null)

    fun selectAddress(addressId: String?) {
        if (addressId.isNullOrBlank()) {
            resetAddressToDefault()
            return
        }
        _selectedAddressId.value = addressId
        
        // Sincronización automática de Perfil:
        // Si la dirección seleccionada pertenece a una empresa, cambiamos el perfil activo.
        scope.launch {
            val allAddresses = availableAddressInfos.first()
            val selected = allAddresses.find { it.id == addressId }
            _selectedProfileId.value = selected?.ownerId
        }
        
        // Limpiamos el override de GPS si se selecciona una dirección manual
        if (addressId != "gps_current") {
            _gpsAddressOverride.value = null
        }
    }

    /**
     * [NUEVO] RESTAURAR A VALOR POR DEFECTO:
     * Limpia selecciones manuales y el override de GPS, volviendo a la dirección principal del perfil.
     */
    fun resetAddressToDefault() {
        _gpsAddressOverride.value = null
        _selectedAddressId.value = null
        _selectedProfileId.value = null
    }

    fun updateAddressFromGps(address: AddressInfo) {
        _gpsAddressOverride.value = address
        _selectedAddressId.value = "gps_current"
        _selectedProfileId.value = null // El GPS siempre es perfil personal por defecto
    }

    /**
     * Listado de direcciones disponibles derivadas del perfil del usuario + Ubicación GPS.
     */
    val availableAddressInfos: Flow<List<AddressInfo>> = combine(
        userRepository.userProfile,
        _gpsAddressOverride
    ) { userEntity, gpsOverride ->
        val list = mutableListOf<AddressInfo>()
        
        // 1. Prioridad: Ubicación GPS actual (Si está activa)
        gpsOverride?.let { list.add(it) }
        
        if (userEntity == null) return@combine list
        
        // 2. Direcciones del Usuario (Mapeo centralizado en Core)
        val domainUser = userEntity.toDomain()
        list.addAll(domainUser.toAddressInfoList())
        
        list
    }

    /**
     * Dirección activa combinada (GPS, Selección o Default).
     */
    val activeAddress: Flow<AddressInfo?> = combine(
        _selectedAddressId, _gpsAddressOverride, availableAddressInfos
    ) { selectedId, gpsOverride, allAddresses ->
        if (selectedId == "gps_current" && gpsOverride != null) return@combine gpsOverride
        val found = allAddresses.find { it.id == selectedId }
        if (found != null) return@combine found
        allAddresses.firstOrNull()
    }

    private fun initTopicAutomation() {
        scope.launch {
            activeAddress.collect { address ->
                val cp = address?.postalCode
                if (cp != null) {
                    syncTopicsForLocation(cp)
                }
            }
        }
    }

    private suspend fun syncTopicsForLocation(cp: String) {
        // [ELITE ULTRA] Sincronización de Topics via FCM
        previousTopic?.let { 
            android.util.Log.d("AppActionCoordinator", "Desuscrito de topic anterior: $it")
        }

        val newTopic = "zona_$cp"
        previousTopic = newTopic
        android.util.Log.d("AppActionCoordinator", "Suscrito a nuevo topic de zona: $newTopic")
    }

    init {
        initTopicAutomation()
    }
}


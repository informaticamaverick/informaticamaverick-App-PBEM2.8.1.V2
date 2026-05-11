package com.example.myapplication.data.repository

import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.client.HUDContext
import com.example.myapplication.presentation.components.AddressInfo
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- APP ACTION COORDINATOR ---
 * Centraliza las acciones globales y el estado compartido (como la ubicación activa)
 * para evitar la inyección circular de ViewModels.
 */
@Singleton
class AppActionCoordinator @Inject constructor(
    userRepository: UserRepository
) {
    // Eventos de acciones globales (ej: clics en Be, disparadores de búsqueda)
    private val _actionEvent = MutableSharedFlow<String>()
    val actionEvent = _actionEvent.asSharedFlow()

    // --- ESTADO GLOBAL DE BÚSQUEDA e INTENCIONES ---
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery = _globalSearchQuery.asStateFlow()

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

    fun updateHUDContext(context: HUDContext) {
        _currentHUDContext.value = context
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

    fun updateWeather(temp: String, emoji: String, desc: String) {
        _temperature.value = temp
        _weatherEmoji.value = emoji
        _weatherDescription.value = desc
    }

    // Gestión de ubicación activa compartida
    private val _selectedAddressId = MutableStateFlow<String?>(null)
    val selectedAddressId = _selectedAddressId.asStateFlow()

    private val _gpsAddressOverride = MutableStateFlow<AddressInfo?>(null)

    fun selectAddress(addressId: String) {
        _selectedAddressId.value = addressId
        _gpsAddressOverride.value = null
    }

    fun updateAddressFromGps(address: AddressInfo) {
        _gpsAddressOverride.value = address
        _selectedAddressId.value = "gps_current"
    }

    /**
     * Listado de direcciones disponibles derivadas del perfil del usuario + Ubicación GPS.
     */
    val availableAddressInfos: Flow<List<AddressInfo>> = combine(
        userRepository.userProfile,
        _gpsAddressOverride
    ) { user, gpsOverride ->
        val list = mutableListOf<AddressInfo>()
        
        // 1. Prioridad: Ubicación GPS actual (Si está activa)
        gpsOverride?.let { list.add(it) }
        
        if (user == null) return@combine list
        
        // 2. Direcciones Personales
        user.personalAddresses.forEach { addr ->
            list.add(AddressInfo(
                id = addr.id,
                companyOrUserName = user.displayName,
                branchName = addr.label.ifEmpty { "Mi Domicilio" },
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
        
        // 3. Direcciones de Empresas
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
}

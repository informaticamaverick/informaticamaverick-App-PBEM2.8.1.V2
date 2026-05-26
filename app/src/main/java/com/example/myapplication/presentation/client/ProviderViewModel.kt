package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.ProviderRepository
import com.example.myapplication.data.repository.AppActionCoordinator
import com.example.myapplication.data.utils.SearchUtils.prepareForSearch
import com.example.myapplication.data.utils.SearchUtils.wordStartsWithSmart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

/**
 * Data class para representar la ubicación del usuario y su código regional.
 */
data class UserLocation(val lat: Double, val lon: Double, val locality: String, val zipCode: String? = null)

/**
 * --- MODELOS DE UI PARA PERFIL DETALLADO ---
 * Mueven la lógica de la Screen al ViewModel para mayor limpieza y persistencia.
 */
data class ProviderActiveProfileInfo(
    val id: Int, // 0 = Personal, 1+ = Empresa
    val photo: String?,
    val title: String,
    val subtitle: String,
    val isVerified: Boolean
)

data class PerfilUIState(
    val activeInfo: ProviderActiveProfileInfo? = null,
    val displayBubbles: List<Pair<Int, String?>> = emptyList(),
    val currentBanner: String? = null,
    val showHoursModal: Boolean = false,
    val hoursContent: String = "",
    val totalPages: Int = 0,
    val currentEmails: List<String> = emptyList(),
    val currentAddresses: List<AddressProvider> = emptyList()
)

/**
 * 🔥 MODELO DE UI PARA LA LISTA DE PROVEEDORES 🔥
 * Representa un item que puede ser un cabezal de sección o un proveedor.
 * Esta estructura permite que la UI solo tenga que "dibujar" la lista plana.
 */
sealed class ProviderUiItem {
    data class Header(val title: String, val emoji: String, val id: String) : ProviderUiItem()
    data class Provider(val service: ServiceDisplayModel) : ProviderUiItem()
}

/**
 * --- VIEWMODEL ROBUSTO PARA PROVEEDORES ---
 * Gestiona la lógica unificada de búsqueda, filtrado y transformación 
 * de Prestadores e Independientes hacia la UI.
 * [OPTIMIZADO]: Incluye filtrado regional por Código Postal (Zip Code) y Proximidad.
 */
@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val repository: ProviderRepository,
    private val coordinator: AppActionCoordinator
) : ViewModel() {

    // --- SECCIÓN: ESTADOS DE CONTROL (Triggers) ---
    val searchQuery: StateFlow<String> = coordinator.globalSearchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * FUENTE DE VERDAD: Derivamos la ubicación directamente del Coordinador global.
     */
    val userLocation: StateFlow<UserLocation?> = coordinator.activeAddress.map { info ->
        info?.let { UserLocation(it.lat ?: 0.0, it.lng ?: 0.0, it.locality, it.postalCode) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- SECCIÓN: FLUJO DE DATOS CRUDO ---
    /**
     * Flujo reactivo de proveedores que implementa la estrategia de ahorro de costos:
     * "Primero Room, luego Firebase solo si es necesario".
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val providers: StateFlow<List<Provider>> = combine(
        _selectedCategory,
        userLocation
    ) { category: String?, location: UserLocation? ->
        category to location?.zipCode
    }.flatMapLatest { (category, zipCode) ->
        flow {
            if (category != null) {
                // Si no hay zipCode, buscamos solo por categoría (sin filtro de zona)
                val zip = zipCode ?: ""
                android.util.Log.d("ProviderVM", "🔄 Buscando proveedores para $category en CP '$zip'")
                emitAll(repository.getProvidersByRegionAndCategory(zip, category))
            } else {
                emitAll(repository.allProviders)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ----------------------------------------------------------------------------------
    // 🔥 SECCIÓN: ESTADO DEL PERFIL DETALLADO (MUDADO DESDE LA SCREEN) 🔥
    // ----------------------------------------------------------------------------------
    private val _perfilUiState = MutableStateFlow(PerfilUIState())
    val perfilUiState: StateFlow<PerfilUIState> = _perfilUiState.asStateFlow()

    /**
     * Actualiza el estado del perfil basado en la página seleccionada en el Pager.
     * Centraliza la lógica de "quién es el perfil activo", las burbujas y el banner dinámico.
     */
    fun updateProfilePage(page: Int, provider: Provider) {
        val companies = provider.companies
        
        // 1. Lógica de Perfil Activo
        val activeInfo = if (page == 0) {
            ProviderActiveProfileInfo(
                id = 0,
                photo = provider.photoUrl,
                title = "${provider.name} ${provider.lastName}",
                subtitle = provider.titulo ?: "Profesional Independiente",
                isVerified = provider.isVerified
            )
        } else {
            val company = companies.getOrNull(page - 1)
            company?.let {
                ProviderActiveProfileInfo(
                    id = page,
                    photo = it.photoUrl,
                    title = it.name,
                    subtitle = it.razonSocial,
                    isVerified = it.isVerified
                )
            }
        }

        // 2. Lógica de "Burbujas" de Navegación
        val all = mutableListOf(0 to provider.photoUrl)
        companies.forEachIndexed { i, c -> all.add((i + 1) to c.photoUrl) }
        
        val activeIdx = page % (all.size.takeIf { it > 0 } ?: 1)
        val resultBubbles = mutableListOf<Pair<Int, String?>>()
        if (all.size > 1) {
            for (i in 1 until all.size) {
                val idx = (activeIdx + i) % all.size
                resultBubbles.add(all[idx])
            }
        }

        // 3. Lógica de Banner Dinámico
        val banner = if (page == 0) provider.bannerImageUrl else companies.getOrNull(page - 1)?.bannerImageUrl

        // 4. Lógica de Horarios: texto estático del branch como valor inicial
        val staticHours = if (page == 0) provider.workingHours 
                          else companies.getOrNull(page - 1)?.branches?.firstOrNull()?.workingHours ?: ""

        // 5. Lógica de Listas Procesadas (Emails y Direcciones)
        val emails = if (page == 0) (listOfNotNull(provider.email) + provider.emails).distinct() 
                     else emptyList()
                     
        val addresses = if (page == 0) (listOfNotNull(provider.address) + provider.addresses).distinctBy { it.id }
                        else companies.getOrNull(page - 1)?.branches?.map { it.address } ?: emptyList()

        _perfilUiState.update { 
            it.copy(
                activeInfo = activeInfo,
                displayBubbles = resultBubbles,
                currentBanner = banner,
                hoursContent = staticHours,
                totalPages = 1 + companies.size,
                currentEmails = emails,
                currentAddresses = addresses
            )
        }

        // 6. Si es página de empresa, cargar horarios reales desde Firestore
        if (page > 0) {
            // Los horarios se guardan en Firestore con providerId = company.id (no branch.id)
            val companyId = companies.getOrNull(page - 1)?.id ?: ""
            if (companyId.isNotBlank()) {
                viewModelScope.launch {
                    val firestoreHours = repository.fetchSchedulesForBranch(companyId)
                    if (firestoreHours.isNotBlank()) {
                        _perfilUiState.update { it.copy(hoursContent = firestoreHours) }
                    }
                }
            }
        }
    }

    /** 6. Gestión de Modales (Preservación de estado) */
    fun toggleHoursModal(show: Boolean) {
        _perfilUiState.update {
            it.copy(showHoursModal = show)
        }
    }

    // ----------------------------------------------------------------------------------
    // SECCIÓN: ESTADO FILTRO PREMIUM (SUSCRIPTOS) Y REFINAMIENTOS
    // ----------------------------------------------------------------------------------
    
    private val _showSubscribedOnly = MutableStateFlow(true)
    val showSubscribedOnly: StateFlow<Boolean> = _showSubscribedOnly.asStateFlow()

    /** Alterna el estado del filtro de suscriptos. */
    fun toggleSubscribedFilter() {
        _showSubscribedOnly.value = !_showSubscribedOnly.value
    }

    private val _activeRefinements = MutableStateFlow<Set<String>>(emptySet())
    val activeRefinements: StateFlow<Set<String>> = _activeRefinements.asStateFlow()

    /** Alterna un filtro de refinamiento específico (ej: "24h") */
    fun toggleRefinement(refId: String) {
        val current = _activeRefinements.value
        _activeRefinements.value = if (current.contains(refId)) {
            current - refId
        } else {
            current + refId
        }
    }

    /** Limpia todos los refinamientos activos */
    fun clearRefinements() {
        _activeRefinements.value = emptySet()
    }

    private val _sortByProximity = MutableStateFlow(true)
    val sortByProximity: StateFlow<Boolean> = _sortByProximity.asStateFlow()

    /** Alterna el estado del ordenamiento por proximidad. */
    fun toggleProximitySort() {
        _sortByProximity.value = !_sortByProximity.value
    }

    /** Calcula la distancia entre dos puntos (Haversine formula). */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // --- SECCIÓN: FLUJOS DE DATOS REACTIVOS ---

    /**
     * Flujo principal de servicios unificados con soporte para búsqueda, filtrado regional y proximidad.
     * [ACTUALIZADO]: Utiliza el flujo 'providers' sincronizado y optimizado para costos.
     */
    val unifiedServices: StateFlow<List<ServiceDisplayModel>> = combine(
        providers,
        searchQuery,
        _selectedCategory,
        _showSubscribedOnly,
        _activeRefinements,
        _sortByProximity,
        userLocation
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val providers = args[0] as List<Provider>
        val query = args[1] as String
        val category = args[2] as String?
        val subscribedOnly = args[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val refinements = args[4] as Set<String>
        val sortByProx = args[5] as Boolean
        val uLoc = args[6] as UserLocation?

        _isLoading.value = true
        
        // 1. Filtrado Base (Categoría + Código Postal MANDATORIO + Búsqueda Inteligente)
        val baseFiltered = providers.filter { provider ->
            // Filtro de Categoría
            val matchesCategory = category == null || provider.categories.contains(category)
            
            // 🔥 FILTRADO POR CÓDIGO POSTAL (Evita listas interminables)
            val providerZipCodes = (provider.addresses.map { it.codigoPostal } + listOfNotNull(provider.address?.codigoPostal)).toSet()
            
            // [MEJORADO]: Si no hay zipCode de usuario, no filtramos por zona (mostramos todo lo que haya)
            // Si hay zipCode, buscamos coincidencia exacta.
            val matchesZipCode = uLoc?.zipCode == null || uLoc.zipCode.isBlank() || providerZipCodes.contains(uLoc.zipCode)

            // Filtro de Búsqueda con Smart Search (UNIFICADO: Acentos, Mayúsculas, Paréntesis)
            val norm = query.prepareForSearch()
            val matchesQuery = query.isEmpty() || 
                provider.displayName.wordStartsWithSmart(norm) ||
                provider.companies.any { it.name.wordStartsWithSmart(norm) } ||
                provider.description.wordStartsWithSmart(norm)
            
            matchesCategory && matchesZipCode && matchesQuery
        }

        // 2. Filtrado por Refinamientos (Badges)
        val refined = if (refinements.isEmpty()) {
            baseFiltered
        } else {
            baseFiltered.filter { provider ->
                refinements.all { refId ->
                    when (refId) {
                        "24h" -> provider.works24h || provider.companies.flatMap { it.branches }.any { it.works24h }
                        "loc" -> provider.hasPhysicalLocation || provider.companies.flatMap { it.branches }.any { it.hasPhysicalLocation }
                        "visit" -> provider.doesHomeVisits || provider.companies.flatMap { it.branches }.any { it.doesHomeVisits }
                        "env" -> provider.doesShipping || provider.companies.flatMap { it.branches }.any { it.doesShipping }
                        "date" -> provider.acceptsAppointments || provider.companies.flatMap { it.branches }.any { it.acceptsAppointments }
                        "serv" -> provider.doesService || provider.companies.flatMap { it.branches }.any { it.doesService }
                        "prod" -> provider.doesProduct || provider.companies.flatMap { it.branches }.any { it.doesProduct }
                        else -> true
                    }
                }
            }
        }

        // 3. Mapeo al modelo de UI (Incluye cálculo de distancia)
        var mapped = refined.map { transformToUnified(it, uLoc) }

        // 🔥 [CORRECCIÓN SOLICITADA]: Respetar siempre el filtro de Suscriptos si está activo
        if (subscribedOnly) {
            mapped = mapped.filter { it.isSubscribed }
        }

        // 4. Lógica de Ordenamiento
        mapped = if (sortByProx && uLoc != null) {
            // Cercanía manda, suscriptos como desempate
            mapped.sortedWith(compareBy<ServiceDisplayModel> { it.distanceKm ?: Double.MAX_VALUE }.thenByDescending { it.isSubscribed })
        } else {
            mapped.sortedByDescending { it.isSubscribed }
        }
        
        _isLoading.value = false
        mapped
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // ----------------------------------------------------------------------------------
    // 🔥 SECCIÓN: ITEMS DE UI PROCESADOS (HEADERS + PROVIDERS) 🔥
    // ----------------------------------------------------------------------------------

    /**
     * Flujo que entrega la lista final procesada para la UI, incluyendo cabezales de proximidad.
     * Esta lógica fue mudada desde la Screen para mantener la UI limpia y centralizar el procesamiento.
     */
    val uiItems: StateFlow<List<ProviderUiItem>> = combine(
        unifiedServices,
        _sortByProximity,
        userLocation
    ) { services, sortByProx, uLoc ->
        // Si no hay ordenamiento por proximidad, devolvemos la lista plana de proveedores
        if (!sortByProx || uLoc == null) {
            return@combine services.map { ProviderUiItem.Provider(it) }
        }

        val result = mutableListOf<ProviderUiItem>()
        // Definición de rangos de proximidad (Mismo criterio que se usaba en la Screen)
        val ranges = listOf(
            Triple(0.0..1.0, "En un radio de 1 km" to "1km", "🚶"),
            Triple(1.0..2.0, "En un radio de 2 km" to "2km", "🚲"),
            Triple(2.0..3.0, "En un radio de 3 km" to "3km", "🛵"),
            Triple(3.0..5.0, "En un radio de 5 km" to "5km", "🚗"),
            Triple(5.0..50.0, "En radio mayor a 5 km" to "plus", "🛣️"),
            Triple(50.0..Double.MAX_VALUE, "Está muy lejos de ${uLoc.locality}" to "far", "😢")
        )

        // Agrupamiento por rangos
        ranges.forEach { (range, data, emoji) ->
            val (label, sectionId) = data
            val inRange = services.filter { (it.distanceKm ?: 9999.0) in range }
            if (inRange.isNotEmpty()) {
                // Añadimos el cabezal de sección
                result.add(ProviderUiItem.Header(label, emoji, sectionId))
                // Añadimos los proveedores que caen en este rango
                inRange.forEach { result.add(ProviderUiItem.Provider(it)) }
            }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Flujo de favoritos unificado. */
    val favoriteServices: StateFlow<List<ServiceDisplayModel>> = repository.favoriteProviders
        .map { list -> list.map { transformToUnified(it, userLocation.value) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- SECCIÓN: LÓGICA DE TRANSFORMACIÓN (MAPPER) ---

    private fun transformToUnified(provider: Provider, userLoc: UserLocation?): ServiceDisplayModel {
        val mainCompany = provider.companies.firstOrNull()
        val allBranches = provider.companies.flatMap { it.branches }

        // --- Cálculo de Distancia ---
        val pLat = provider.address?.latitude ?: allBranches.firstOrNull()?.address?.latitude
        val pLon = provider.address?.longitude ?: allBranches.firstOrNull()?.address?.longitude
        val distance = if (userLoc != null && pLat != null && pLon != null) {
            calculateDistance(userLoc.lat, userLoc.lon, pLat, pLon)
        } else null

        val isCompany = mainCompany != null
        val typeEmoji = if (isCompany) "🏢" else "👨‍🔧"
        val typeLabel = if (isCompany) "Empresa Certificada" else "Profesional Independiente"

        val w24h = provider.works24h || allBranches.any { it.works24h }
        val hasLoc = provider.hasPhysicalLocation || allBranches.any { it.hasPhysicalLocation }
        val hVisits = provider.doesHomeVisits || allBranches.any { it.doesHomeVisits }
        val dServ = provider.doesService || allBranches.any { it.doesService }
        val dProd = provider.doesProduct || allBranches.any { it.doesProduct }
        val dShip = provider.doesShipping || allBranches.any { it.doesShipping }
        val accApp = provider.acceptsAppointments || allBranches.any { it.acceptsAppointments }

        val badges = listOf(
            BadgeDisplayData("24h", "🕒", "Atención 24hs", w24h),
            BadgeDisplayData("loc", "🏪", "Local Físico", hasLoc),
            BadgeDisplayData("visit", "🚚", "Visitas a Domicilio", hVisits),
            BadgeDisplayData("env", "📦", "Realiza Envíos", dShip),
            BadgeDisplayData("date", "📅", "Turnos Online", accApp),
            BadgeDisplayData("serv", "🛠️", "Servicios", dServ),
            BadgeDisplayData("prod", "🛍️", "Venta Productos", dProd)
        )

        return ServiceDisplayModel(
            id = provider.id,
            title = mainCompany?.name ?: provider.displayName,
            subtitle = if (isCompany) "Empresa" else "Independiente",
            photoUrl = mainCompany?.photoUrl ?: provider.photoUrl ?: "",
            rating = (mainCompany?.rating ?: provider.rating).toDouble(),
            isVerified = mainCompany?.isVerified ?: provider.isVerified,
            isOnline = provider.isOnline,
            isFavorite = provider.isFavorite,
            type = if (isCompany) ProviderType.COMPANY else ProviderType.INDIVIDUAL,
            works24h = w24h,
            hasPhysicalLocation = hasLoc,
            doesHomeVisits = hVisits,
            doesService = dServ,
            doesProduct = dProd,
            doesShipping = dShip,
            acceptsAppointments = accApp,
            isSubscribed = provider.isSubscribed,
            categoryId = provider.categories.firstOrNull(),
            categories = provider.categories,
            displayAddress = provider.address?.fullString() ?: allBranches.firstOrNull()?.address?.fullString(),
            branchName = allBranches.firstOrNull()?.name,
            latitude = pLat,
            longitude = pLon,
            distanceKm = distance,
            typeEmoji = typeEmoji,
            typeLabel = typeLabel,
            badgeList = badges,
            companyId = mainCompany?.id
        )
    }

    // --- SECCIÓN: ACCIONES DE LA UI (CONSERVADAS) ---

    fun onSearchQueryChanged(query: String) {
        coordinator.updateSearchQuery(query)
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    // ==================================================================================
    // --- 🔄 SECCIÓN: SINCRONIZACIÓN Y REFRESCADO (PULL-TO-REFRESH) ---
    // ==================================================================================
    /**
     * Realiza un refresco manual de los datos.
     * 1. Limpia errores previos.
     * 2. Activa el estado de carga.
     * 3. Sincroniza con Firebase (esto actualiza Room automáticamente).
     */
    fun refreshData(category: String, zipCode: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Sincronizamos con Firebase (el repositorio se encarga de persistir en Room)
                repository.searchAndSyncProviders(zipCode ?: "", category)
            } catch (e: Exception) {
                _error.value = "Error al refrescar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(providerId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateFavoriteStatus(providerId, !currentStatus)
            } catch (e: Exception) {
                _error.value = "Error al actualizar favoritos"
            }
        }
    }

    fun saveProviderProfile(provider: Provider) {
        viewModelScope.launch {
            try {
                repository.saveProviderProfile(provider)
            } catch (e: Exception) {
                _error.value = "Error al guardar el perfil: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    // --- SECCIÓN: OPERACIONES DE DETALLE (CONSERVADAS) ---

    fun getProviderById(providerId: String): Flow<Provider?> = repository.getProviderById(providerId)

    fun loadProviderDetail(providerId: String) {
        viewModelScope.launch {
            repository.fetchAndSyncProviderDetail(providerId)
        }
    }

    /** Helpers para lógica de negocio en pantallas de detalle */
    fun getAllBranchesForProvider(provider: Provider): List<BranchProvider> {
        return provider.companies.flatMap { it.branches }
    }

    // 🔥 EXTENSIONES DE BÚSQUEDA UNIFICADAS (OBRERO PROVIDER) SE MOVIERON A SearchUtils.kt 🔥
}

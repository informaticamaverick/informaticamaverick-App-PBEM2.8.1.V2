package com.example.myapplication.presentation.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.mapper.ProviderDisplayMapper
import com.example.myapplication.core.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * --- USER LOCATION (SISTEMA FAST) ---
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val locality: String? = null
)

/**
 * --- PROVIDER UI ITEM (LISTA UNIFICADA) ---
 */
sealed class ProviderUiItem {
    data class Header(
        val id: String, 
        val title: String, 
        val subtitle: String? = null,
        val emoji: String = "🔍"
    ) : ProviderUiItem()
    data class Provider(val service: ProviderDisplayModel) : ProviderUiItem()
}

/**
 * --- PROVIDER VIEWMODEL (OBRERO DE RESULTADOS) ---
 * (ELITE v3.2): Implementa leyes de Costo Zero, Shallow Loading e Inmediatez.
 */
@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: com.example.myapplication.data.repository.ShortcutRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val allCategories: StateFlow<List<CategoryEntity>> = categoryRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _showSubscribedOnly = MutableStateFlow(false)
    val showSubscribedOnly: StateFlow<Boolean> = _showSubscribedOnly.asStateFlow()

    private val _sortByProximity = MutableStateFlow(false)
    val sortByProximity: StateFlow<Boolean> = _sortByProximity.asStateFlow()

    private val _activeRefinements = MutableStateFlow<Set<String>>(emptySet())
    val activeRefinements: StateFlow<Set<String>> = _activeRefinements.asStateFlow()

    private val _activeSortCriteria = MutableStateFlow<List<String>>(emptyList())
    val activeSortCriteria: StateFlow<List<String>> = _activeSortCriteria.asStateFlow()

    /**
     * [ELITE] IDs de Prestadores Favoritos (SSOT)
     */
    val favoriteProviderIds: StateFlow<Set<String>> = shortcutRepository.getShortcutsByContext("home")
        .map { list -> list.filter { it.type == "provider" }.map { it.targetId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    /**
     * [ELITE] Lista de Prestadores Favoritos Resueltos
     * Resuelve los shortcuts de tipo "provider" a modelos de visualización completos.
     * Permite que el panel de favoritos funcione independientemente de la categoría seleccionada.
     */
    val favoriteProviders: StateFlow<List<ProviderDisplayModel>> = combine(
        favoriteProviderIds,
        providerRepository.allProviders,
        _userLocation
    ) { ids, all, location ->
        if (ids.isEmpty()) emptyList()
        else {
            all.filter { it.uid in ids }.map { 
                ProviderDisplayMapper.toDisplayModel(it, location) 
            }
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val _selectedProviderId = MutableStateFlow<String?>(null)
    
    /**
     * (ON-DEMAND LOCAL): El perfil completo solo se carga cuando se requiere (Deep Load).
     * [LEY #1]: Transforma al modelo de visualización enriquecido.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedProvider: StateFlow<ProviderDisplayModel?> = _selectedProviderId
        .filterNotNull()
        .flatMapLatest { id ->
            // Observamos cambios en el repo y en la ubicación del usuario
            combine(
                providerRepository.allProviders.map { list -> list.find { it.uid == id } },
                _userLocation
            ) { provider, location ->
                provider?.let { 
                    ProviderDisplayMapper.toDisplayModel(it, location) 
                }
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** 
     * [LEY #4] INMEDIATEZ: Fuente de datos paginada desde Room (Elite Performance).
     * Implementa Mapeo Lazy para evitar procesar toda la lista innecesariamente.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagedProviders: Flow<PagingData<ProviderUiItem>> = combine(
        _selectedCategory,
        userRepository.userProfile.map { user: UserEntity? -> 
            user?.personalAddresses?.firstOrNull()?.codigoPostal 
        }.distinctUntilChanged(),
        _userLocation,
        _showSubscribedOnly,
        _activeRefinements,
        favoriteProviderIds
    ) { args ->
        val category = args[0] as String?
        val zipCode = args[1] as String?
        val location = args[2] as UserLocation?
        val subscribedOnly = args[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val refinements = args[4] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val favIds = args[5] as Set<String>

        if (category == null || zipCode == null) flowOf(PagingData.empty())
        else {
            refreshData(category, zipCode)
            providerRepository.getShallowProvidersPaged(zipCode, category)
                .map { pagingData ->
                    pagingData
                        .flatMap { shallow ->
                            // 1. Mapeo Lazy a Modelo de Visualización (Ley Pareja integrada en Mapper)
                            val displayModels = ProviderDisplayMapper.fromShallow(shallow, location, zipCode)
                            
                            if (displayModels.isEmpty()) {
                                listOf(ProviderDisplayMapper.toDisplayModel(
                                    Provider(
                                        uid = shallow.id, 
                                        displayName = shallow.displayName, 
                                        name = shallow.name, 
                                        lastName = shallow.lastName, 
                                        photoUrl = shallow.photoUrl, 
                                        profileThumbnail = shallow.profileThumbnail, 
                                        rating = shallow.rating, 
                                        categories = shallow.categories, 
                                        isSubscribed = shallow.isSubscribed, 
                                        isVerified = shallow.isVerified, 
                                        isOnline = shallow.isOnline, 
                                        works24h = shallow.works24h, 
                                        hasPhysicalLocation = shallow.hasPhysicalLocation, 
                                        doesHomeVisits = shallow.doesHomeVisits, 
                                        doesShipping = shallow.doesShipping, 
                                        acceptsAppointments = shallow.acceptsAppointments, 
                                        workingHours = shallow.workingHours,
                                        email = "", 
                                        phoneNumber = ""
                                    )
                                ))
                            } else displayModels
                        }
                        .map { it.copy(isFavorite = favIds.contains(it.id)) }
                        .filter { s ->
                            // 2. Filtrado en caliente (Reactive Filtering)
                            val matchesSubscribed = !subscribedOnly || s.isSubscribed
                            val matchesRefinements = refinements.isEmpty() || refinements.all { r -> 
                                when(r) {
                                    "24h" -> s.works24h
                                    "home" -> s.doesHomeVisits
                                    "local" -> s.hasPhysicalLocation
                                    else -> true
                                }
                            }
                            matchesSubscribed && matchesRefinements
                        }
                        .map { ProviderUiItem.Provider(it) }
                        .insertSeparators { before, after ->
                            // 3. Inserción de Cabecera (Ley #1: UI Tonta)
                            if (before == null && after != null) {
                                ProviderUiItem.Header("header_$category", category, "Profesionales en tu zona")
                            } else null
                        }
                }
        }
    }.flatMapLatest { it }
    .cachedIn(viewModelScope)

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun loadFullProfile(providerId: String) {
        _selectedProviderId.value = providerId
        viewModelScope.launch {
            _isLoading.value = true
            providerRepository.loadFullProfile(providerId)
            _isLoading.value = false
        }
    }

    /** 
     * (COSTO ZERO): Sincronización Inteligente.
     * Solo dispara red si es necesario o por petición del usuario.
     */
    private var lastSyncParams: Triple<String, String, List<String>>? = null

    fun refreshData(category: String, zipCode: String?) {
        if (zipCode == null) return
        
        val flags = mutableListOf<String>()
        if (_showSubscribedOnly.value) flags.add("elite")
        _activeRefinements.value.forEach { flags.add(it) }
        
        val currentParams = Triple(category, zipCode, flags.toList())
        if (lastSyncParams == currentParams) {
            Log.d("ProviderViewModel", "🚫 [REFRESH] Ignorando sync redundante para $currentParams")
            return
        }
        lastSyncParams = currentParams

        viewModelScope.launch {
            providerRepository.syncProvidersByRegion(zipCode, category, filterFlags = flags)
        }
    }

    fun forceRefreshData(category: String, zipCode: String?) {
        if (zipCode == null) return
        
        val flags = mutableListOf<String>()
        if (_showSubscribedOnly.value) flags.add("elite")
        _activeRefinements.value.forEach { flags.add(it) }
        
        lastSyncParams = Triple(category, zipCode, flags.toList())

        viewModelScope.launch {
            _isLoading.value = true
            providerRepository.syncProvidersByRegion(zipCode, category, force = true, filterFlags = flags)
            _isLoading.value = false
        }
    }

    fun toggleSubscribedFilter() { _showSubscribedOnly.update { !it } }
    fun toggleProximitySort() { _sortByProximity.update { !it } }
    fun setSortOrder(order: String?) { _activeSortCriteria.value = if (order == null) emptyList() else listOf(order) }
    fun clearRefinements() { _activeRefinements.value = emptySet() }
    fun toggleRefinement(refinement: String) {
        _activeRefinements.update { current ->
            if (current.contains(refinement)) current - refinement else current + refinement
        }
    }

    /**
     * [ELITE] GESTIÓN DE FAVORITOS (SHORTCUTS)
     * Reemplaza al antiguo sistema de toggleFavorite para asegurar persistencia local.
     */
    fun manageShortcut(id: String, type: String, add: Boolean, label: String? = null, icon: String? = null) {
        viewModelScope.launch {
            if (add) {
                shortcutRepository.addShortcut("home", id, type, label, icon)
            } else {
                shortcutRepository.removeShortcut("home", id, type)
            }
        }
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _userLocation.value = UserLocation(lat, lng)
    }
}

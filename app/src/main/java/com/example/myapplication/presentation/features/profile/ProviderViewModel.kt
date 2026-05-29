package com.example.myapplication.presentation.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.mapper.ProviderDisplayMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
 */
@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    private val _shortcuts = MutableStateFlow<List<com.example.myapplication.presentation.components.MaverickFilterItem>>(emptyList())
    val shortcuts: StateFlow<List<com.example.myapplication.presentation.components.MaverickFilterItem>> = _shortcuts.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val _selectedProviderId = MutableStateFlow<String?>(null)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedProvider: StateFlow<Provider?> = _selectedProviderId
        .filterNotNull()
        .flatMapLatest { id ->
            providerRepository.allProviders.map { list -> list.find { it.uid == id } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --- FUENTE DE DATOS: OBSERVABLE DESDE ROOM (SSOT) ---
    val unifiedServices: StateFlow<List<ProviderDisplayModel>> = providerRepository.allProviders
        .map { providers -> 
            providers.map { ProviderDisplayMapper.toDisplayModel(it, userLocation.value) } 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** 
     * UI ITEMS: Procesa filtrado, ordenamiento y agrupamiento para la LazyColumn.
     */
    val uiItems: StateFlow<List<ProviderUiItem>> = combine(
        unifiedServices,
        _selectedCategory,
        _showSubscribedOnly,
        _activeRefinements,
        _sortByProximity,
        _activeSortCriteria,
        _userLocation
    ) { args ->
        val services = args[0] as List<ProviderDisplayModel>
        val category = args[1] as? String
        val subscribedOnly = args[2] as Boolean
        val refinements = args[3] as Set<String>
        val proximity = args[4] as Boolean
        val sort = args[5] as List<String>
        val location = args[6] as? UserLocation
        
        var filtered = services.filter { s ->
            val matchesCategory = category == null || s.categories.contains(category)
            val matchesSubscribed = !subscribedOnly || s.isSubscribed
            val matchesRefinements = refinements.isEmpty() || refinements.any { r -> 
                when(r) {
                    "24h" -> s.works24h
                    "home" -> s.doesHomeVisits
                    "local" -> s.hasPhysicalLocation
                    else -> true
                }
            }
            matchesCategory && matchesSubscribed && matchesRefinements
        }

        // --- ORDENAMIENTO ---
        filtered = when {
            proximity && location != null -> filtered.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            sort.contains("sort_rating") -> filtered.sortedByDescending { it.rating }
            sort.contains("sort_name") -> filtered.sortedBy { it.title.lowercase() }
            else -> filtered
        }

        val items = mutableListOf<ProviderUiItem>()
        if (category != null && filtered.isNotEmpty()) {
            items.add(ProviderUiItem.Header("header_$category", category, "${filtered.size} resultados encontrados"))
        }
        items.addAll(filtered.map { ProviderUiItem.Provider(it) })
        
        items
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Carga el perfil completo del prestador (jerárquico).
     */
    fun loadFullProfile(providerId: String) {
        _selectedProviderId.value = providerId
        viewModelScope.launch {
            _isLoading.value = true
            providerRepository.loadFullProfile(providerId)
            _isLoading.value = false
        }
    }

    /** 
     * Sincronización normal (Costo Zero): Prioriza local.
     */
    fun refreshData(category: String, zipCode: String?) {
        viewModelScope.launch {
            if (zipCode != null) {
                providerRepository.syncProvidersByRegion(zipCode, category)
            }
        }
    }

    /** 
     * Sincronización forzada (Bypass Cache).
     */
    fun forceRefreshData(category: String, zipCode: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            if (zipCode != null) {
                providerRepository.forceSyncProviders(zipCode, category)
            }
            _isLoading.value = false
        }
    }

    fun toggleSubscribedFilter() {
        _showSubscribedOnly.update { !it }
    }

    fun toggleProximitySort() {
        _sortByProximity.update { !it }
    }

    fun setSortOrder(order: String?) {
        if (order == null) {
            _activeSortCriteria.value = emptyList()
        } else {
            _activeSortCriteria.value = listOf(order)
        }
    }

    fun clearRefinements() {
        _activeRefinements.value = emptySet()
    }

    fun toggleRefinement(refinement: String) {
        _activeRefinements.update { current ->
            if (current.contains(refinement)) current - refinement else current + refinement
        }
    }

    fun toggleFavorite(providerId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            providerRepository.updateFavoriteStatus(providerId, isFavorite)
        }
    }

    fun manageShortcut(id: String, add: Boolean) {
        // Delegado a CategoryViewModel o implementado aquí si es específico de prestadores
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _userLocation.value = UserLocation(lat, lng)
    }
}

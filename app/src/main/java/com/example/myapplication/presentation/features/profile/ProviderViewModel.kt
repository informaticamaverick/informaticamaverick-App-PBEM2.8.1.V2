package com.example.myapplication.presentation.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.domain.model.*
import com.example.myapplication.data.model.*
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.example.myapplication.presentation.components.FilterSortItem
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.features.home.CategoryVisuals
import androidx.compose.ui.graphics.Color
import com.example.myapplication.core.common.extensions.prepareForSearch
import com.example.myapplication.core.common.extensions.wordStartsWithSmart
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.presentation.mapper.ProviderDisplayMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserLocation(val lat: Double, val lon: Double, val locality: String, val zipCode: String? = null)

data class ProviderActiveProfileInfo(
    val id: Int, 
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

sealed class ProviderUiItem {
    data class Header(val title: String, val emoji: String, val id: String) : ProviderUiItem()
    data class Provider(val service: ProviderDisplayModel) : ProviderUiItem()
}

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val repository: ProviderRepository,
    private val shortcutRepository: ShortcutRepository,
    private val categoryRepository: CategoryRepository,
    private val coordinator: AppActionCoordinator
) : ViewModel() {

    val searchQuery: StateFlow<String> = coordinator.globalSearchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastSyncTimestamp = 0L

    val shortcuts: StateFlow<List<FilterSortItem>> = shortcutRepository.getShortcutsByContext("results")
        .combine(categoryRepository.allCategories) { savedShortcuts, allCats ->
            savedShortcuts.mapNotNull { shortcut ->
                val globalFilter = BeDictionary.Filters[shortcut.targetId]
                if (globalFilter != null) {
                    FilterSortItem(
                        id = globalFilter.id,
                        label = globalFilter.label,
                        emoji = globalFilter.emoji ?: "🔹",
                        icon = globalFilter.icon,
                        color = globalFilter.color,
                        section = globalFilter.section
                    )
                } else {
                    allCats.find { it.name == shortcut.targetId }?.let { cat ->
                        FilterSortItem(
                            id = cat.name,
                            label = cat.name,
                            emoji = cat.icon,
                            icon = null,
                            color = Color(CategoryVisuals.getColorFor(cat.superCategory)),
                            section = "CATEGORIAS"
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun manageShortcut(id: String, add: Boolean) {
        viewModelScope.launch {
            if (add) shortcutRepository.addShortcut("results", id, "filter")
            else shortcutRepository.removeShortcut("results", id, "filter")
        }
    }

    val userLocation: StateFlow<UserLocation?> = coordinator.activeAddress.map { info ->
        info?.let { UserLocation(it.lat ?: 0.0, it.lng ?: 0.0, it.locality, it.postalCode) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val providers: StateFlow<List<Provider>> = combine(
        _selectedCategory,
        userLocation
    ) { category, location ->
        category to location?.zipCode
    }.flatMapLatest { (category, zipCode) ->
        flow {
            if (category != null) {
                val zip = zipCode ?: ""
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

    private val _perfilUiState = MutableStateFlow(PerfilUIState())
    val perfilUiState: StateFlow<PerfilUIState> = _perfilUiState.asStateFlow()

    fun updateProfilePage(page: Int, provider: Provider) {
        val companies = provider.companies
        val fullName = "${provider.name} ${provider.lastName}".trim()
        val activeInfo = if (page == 0) {
            ProviderActiveProfileInfo(
                id = 0,
                photo = provider.photoUrl,
                title = fullName.ifEmpty { provider.displayName },
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

        val banner = if (page == 0) provider.bannerImageUrl else companies.getOrNull(page - 1)?.bannerImageUrl
        val hours = if (page == 0) provider.workingHours 
                    else companies.getOrNull(page - 1)?.branches?.firstOrNull()?.workingHours ?: ""

        val emails = if (page == 0) (listOfNotNull(provider.email) + provider.emails).distinct() 
                     else emptyList()
                     
        val addresses = if (page == 0) (listOfNotNull(provider.address) + provider.addresses).distinctBy { it.id }
                        else companies.getOrNull(page - 1)?.branches?.map { it.address } ?: emptyList()

        _perfilUiState.update { 
            it.copy(
                activeInfo = activeInfo,
                displayBubbles = resultBubbles,
                currentBanner = banner,
                hoursContent = hours,
                totalPages = 1 + companies.size,
                currentEmails = emails,
                currentAddresses = addresses
            )
        }
    }

    fun toggleHoursModal(show: Boolean) {
        _perfilUiState.update { it.copy(showHoursModal = show) }
    }

    private val _showSubscribedOnly = MutableStateFlow(true)
    val showSubscribedOnly: StateFlow<Boolean> = _showSubscribedOnly.asStateFlow()

    fun toggleSubscribedFilter() {
        _showSubscribedOnly.value = !_showSubscribedOnly.value
    }

    private val _activeRefinements = MutableStateFlow<Set<String>>(emptySet())
    val activeRefinements: StateFlow<Set<String>> = _activeRefinements.asStateFlow()

    fun toggleRefinement(refId: String) {
        val current = _activeRefinements.value
        _activeRefinements.value = if (current.contains(refId)) current - refId else current + refId
    }

    fun clearRefinements() {
        _activeRefinements.value = emptySet()
    }

    private val _sortByProximity = MutableStateFlow(false)
    val sortByProximity: StateFlow<Boolean> = _sortByProximity.asStateFlow()

    fun toggleProximitySort() {
        setSortOrder("toggle_proximity")
    }

    private val _activeSortCriteria = MutableStateFlow<List<String>>(emptyList())
    val activeSortCriteria: StateFlow<List<String>> = _activeSortCriteria.asStateFlow()

    fun setSortOrder(sortId: String?) {
        if (sortId == null) {
            _activeSortCriteria.value = emptyList()
            _sortByProximity.value = false
            return
        }

        val current = _activeSortCriteria.value.toMutableList()
        if (current.contains(sortId)) current.remove(sortId)
        else current.add(sortId)
        _activeSortCriteria.value = current
        _sortByProximity.value = current.any { it == "sort_distance" || it == "toggle_proximity" }
    }

    val unifiedServices: StateFlow<List<ProviderDisplayModel>> = combine(
        providers,
        searchQuery,
        _selectedCategory,
        _showSubscribedOnly,
        _activeRefinements,
        _activeSortCriteria,
        _sortByProximity,
        userLocation
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val providersList = args[0] as List<Provider>
        val query = args[1] as String
        val category = args[2] as String?
        val subscribedOnly = args[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val refinements = args[4] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val sortCriteria = args[5] as List<String>
        val proxEnabled = args[6] as Boolean
        val uLoc = args[7] as UserLocation?
        _isLoading.value = true
        
        val baseFiltered = providersList.filter { provider ->
            val matchesCategory = category == null || provider.categories.contains(category)
            val providerZipCodes = (provider.addresses.map { it.codigoPostal } + listOfNotNull(provider.address?.codigoPostal)).toSet()
            val matchesZipCode = uLoc?.zipCode == null || uLoc.zipCode.isBlank() || providerZipCodes.contains(uLoc.zipCode)
            val norm = query.prepareForSearch()
            val matchesQuery = query.isEmpty() || 
                provider.displayName.wordStartsWithSmart(norm) ||
                provider.companies.any { it.name.wordStartsWithSmart(norm) } ||
                provider.description.wordStartsWithSmart(norm)
            
            matchesCategory && matchesZipCode && matchesQuery
        }

        var refined = if (refinements.isEmpty()) baseFiltered else baseFiltered.filter { provider ->
                refinements.all { refId ->
                    when (refId) {
                        "filter_chat_sub" -> provider.isSubscribed
                        "filter_chat_verified" -> provider.isVerified
                        "filter_chat_online" -> provider.isOnline
                        "filter_chat_24h" -> provider.works24h || provider.companies.any { it.branches.any { b -> b.works24h } }
                        "filter_chat_local" -> provider.hasPhysicalLocation || provider.companies.any { it.branches.any { b -> b.hasPhysicalLocation } }
                        "filter_visits" -> provider.doesHomeVisits || provider.companies.any { it.branches.any { b -> b.doesHomeVisits } }
                        "filter_shipping" -> provider.doesShipping || provider.companies.any { it.branches.any { b -> b.doesShipping } }
                        "filter_appointments" -> provider.acceptsAppointments || provider.companies.any { it.branches.any { b -> b.acceptsAppointments } }
                        "filter_products" -> provider.doesProduct
                        "filter_services" -> provider.doesService
                        "filter_chat_fav" -> provider.isFavorite
                        else -> true
                    }
                }
            }

        if (subscribedOnly) refined = refined.filter { it.isSubscribed }

        val mapped = refined.map { ProviderDisplayMapper.toDisplayModel(it, uLoc?.lat, uLoc?.lon) }
        val comparator = buildChainComparator(proxEnabled, sortCriteria)
        val finalSorted = mapped.sortedWith(comparator)
        
        _isLoading.value = false
        finalSorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun buildChainComparator(proxEnabled: Boolean, criteria: List<String>): Comparator<ProviderDisplayModel> {
        var chain: Comparator<ProviderDisplayModel> = compareByDescending { it.isSubscribed }
        criteria.forEach { id ->
            chain = when (id) {
                "sort_distance", "toggle_proximity" -> chain.thenBy { it.distanceKm ?: Double.MAX_VALUE }
                "sort_ranking" -> chain.thenByDescending { it.rating }
                "sort_alpha", "sort_nombre_asc" -> chain.thenBy { it.title }
                "sort_date" -> chain.thenByDescending { it.createdAt }
                "sort_hot" -> chain.thenByDescending { it.rating }
                else -> chain
            }
        }
        return chain.thenByDescending { it.rating }
    }

    val uiItems: StateFlow<List<ProviderUiItem>> = combine(
        unifiedServices,
        _sortByProximity,
        userLocation
    ) { services, sortByProx, uLoc ->
        if (!sortByProx || uLoc == null) return@combine services.map { ProviderUiItem.Provider(it) }

        val result = mutableListOf<ProviderUiItem>()
        val ranges = listOf(
            Triple(0.0..1.0, "En un radio de 1 km" to "1km", "🚶"),
            Triple(1.0..2.0, "En un radio de 2 km" to "2km", "🚲"),
            Triple(2.0..3.0, "En un radio de 3 km" to "3km", "🛵"),
            Triple(3.0..5.0, "En un radio de 5 km" to "5km", "🚗"),
            Triple(5.0..50.0, "En radio mayor a 5 km" to "plus", "🛣️"),
            Triple(50.0..Double.MAX_VALUE, "Está muy lejos de ${uLoc.locality}" to "far", "😢")
        )

        ranges.forEach { (range, data, emoji) ->
            val (label, sectionId) = data
            val inRange = services.filter { (it.distanceKm ?: 9999.0) in range }
            if (inRange.isNotEmpty()) {
                result.add(ProviderUiItem.Header(label, emoji, sectionId))
                inRange.forEach { result.add(ProviderUiItem.Provider(it)) }
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) = coordinator.updateSearchQuery(query)
    fun onCategorySelected(category: String?) { _selectedCategory.value = category }
    
    fun refreshData(category: String, zipCode: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try { /* ... */ } catch (e: Exception) { _error.value = e.message } finally { _isLoading.value = false }
        }
    }
    
    fun forceRefreshData(category: String, zipCode: String?) {
        val now = System.currentTimeMillis()
        if (now - lastSyncTimestamp < 30_000) return
        viewModelScope.launch {
            _isLoading.value = true
            try { repository.forceSyncProviders(zipCode ?: "", category); lastSyncTimestamp = now } 
            catch (e: Exception) { _error.value = e.message } finally { _isLoading.value = false }
        }
    }
    
    fun toggleFavorite(providerId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            if (currentStatus) {
                shortcutRepository.removeShortcut("provider", providerId, "provider")
            } else {
                shortcutRepository.addShortcut("provider", providerId, "provider")
            }
        }
    }
    
    fun saveProviderProfile(provider: Provider) {
        viewModelScope.launch { try { repository.saveProvider(ProviderEntity.fromDomain(provider)) } catch (e: Exception) { _error.value = e.message } }
    }
    
    fun clearError() { _error.value = null }
    fun getProviderFlowById(providerId: String): Flow<Provider?> = flow { emit(repository.getProviderById(providerId)) }
    fun loadProviderDetail(providerId: String) { viewModelScope.launch { repository.fetchAndSyncProviderDetail(providerId) } }
    fun getAllBranchesForProvider(provider: Provider): List<BranchProvider> = provider.companies.flatMap { it.branches }
}

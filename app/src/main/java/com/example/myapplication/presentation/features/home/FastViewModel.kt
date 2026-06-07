package com.example.myapplication.presentation.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.dao.FastCategoryEntity
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.example.myapplication.data.repository.FastRepository
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.presentation.components.FilterSortItem
import com.example.myapplication.presentation.registry.BeDictionary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL PARA LA PANTALLA FAST (CLEAN RESET) ---
 * Centraliza la lógica de búsqueda en el radar.
 */
@HiltViewModel
class FastViewModel @Inject constructor(
    private val fastRepository: FastRepository,
    private val shortcutRepository: ShortcutRepository,
    private val appActionCoordinator: AppActionCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(FastUIState())
    val uiState: StateFlow<FastUIState> = _uiState.asStateFlow()

    /**
     * Accesos directos dinámicos guardados en Room.
     */
    val shortcuts: StateFlow<List<FilterSortItem>> = shortcutRepository.getShortcutsByContext("fast")
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
            if (add) shortcutRepository.addShortcut("fast", id, "filter")
            else shortcutRepository.removeShortcut("fast", id, "filter")
        }
    }

    /**
     * Historial de categorías usadas recientemente o con más frecuencia.
     */
    val fastHistory: StateFlow<List<FastCategoryEntity>> = fastRepository.getFastHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startSearch(category: CategoryEntity?) {
        val cat = category ?: _uiState.value.selectedCategory
        if (cat == null) return // Necesitamos al menos una categoría previa o actual

        viewModelScope.launch {
            // Guardamos en Room para el historial dinámico
            fastRepository.registerUsage(cat)

            val address = appActionCoordinator.activeAddress.firstOrNull()
          //  val lat = address?.lat ?: -26.8310
          //  val lon = address?.lng ?: -65.2045
            val zipCode = address?.codigoPostal ?: ""

            _uiState.update { it.copy(isSearching = true, searchFinished = false, selectedCategory = cat) }
            //val results = fastRepository.searchHybridEmergency(cat.name, lat, lon, _uiState.value.filters, zipCode)
           // _uiState.update { it.copy(isSearching = false, searchFinished = true, searchResults = results) }
        }
    }

    fun toggleFilter(filterId: String) {
        _uiState.update { state ->
            val newFilters = when (filterId) {
                "filter_online" -> state.filters.copy(isOnline = !state.filters.isOnline)
                "filter_chat_24h" -> state.filters.copy(is24h = !state.filters.is24h)
                "filter_chat_sub" -> state.filters.copy(isSubscribed = !state.filters.isSubscribed)
                "filter_chat_local" -> state.filters.copy(isLocal = !state.filters.isLocal)
                else -> state.filters
            }
            state.copy(filters = newFilters)
        }
    }

    fun selectCategory(category: CategoryEntity) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun resetSearch() {
        _uiState.update { it.copy(searchFinished = false, searchResults = emptyList(), selectedCategory = null) }
    }

    fun updateBeSearchCategories(categories: List<CategoryEntity>) {
        _uiState.update { it.copy(beSearchCategories = categories) }
        // 🔥 NOTIFICAMOS AL MEDIADOR PARA QUE LA BURBUJA GLOBAL REACCIONE 🔥
        appActionCoordinator.updateMatchedCategories(categories)
    }

    fun setBeSearchActive(active: Boolean) {
        _uiState.update { it.copy(isBeSearchActive = active) }
    }
}

data class FastUIState(
    val isSearching: Boolean = false,
    val searchFinished: Boolean = false,
    val searchResults: List<ProviderWithDistance> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val filters: FastFilterState = FastFilterState(),
    val beSearchCategories: List<CategoryEntity> = emptyList(),
    val isBeSearchActive: Boolean = false
)










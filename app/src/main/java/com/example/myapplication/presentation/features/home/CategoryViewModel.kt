package com.example.myapplication.presentation.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.utils.filtroDeTexto
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.data.local.entity.ShortcutEntity
import com.example.myapplication.presentation.global.AppActionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

// ==========================================================================================
// --- SECCIÓN: MODELOS DE DATOS PARA CATEGORÍAS (EL OBRERO) ---
// ==========================================================================================

/**
 * Modelo de datos para representar una agrupación de categorías.
 */
data class SuperCategory(
    val title: String,
    val icon: String,
    val items: List<CategoryEntity> = emptyList(),
    val totalItems: Int = 0,
    val color: Long = 0xFF1A1F26,
    val isFavorite: Boolean = false,
    val hasFavoriteCategories: Boolean = false
)

/**
 * --- CATEGORY VIEWMODEL (EL OBRERO DE CATEGORÍAS) ---
 */
object CategoryVisuals {
    fun getColorFor(superCategory: String?): Long {
        return 0xFF1A1F26
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val coordinator: AppActionCoordinator
) : ViewModel() {

    /** 
     * [LEY #3.2 ON-DEMAND]: Fuente Maestra (Perezosa).
     * Solo se activa bajo demanda cuando el usuario busca, para evitar cargar 500 objetos al inicio.
     */
    private val _loadAllTrigger = MutableStateFlow(false)
    val allCategories: StateFlow<List<CategoryEntity>> = _loadAllTrigger
        .flatMapLatest { shouldLoad ->
            if (shouldLoad) {
                Log.d("CategoryViewModel", "🔍 [LAZY_LOAD] Disparando carga completa de categorías (Modo Búsqueda)")
                repository.allCategories
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = coordinator.globalSearchQuery

    /** 
     * [ELITE METADATOS LIGEROS DE SUPERCATEGORÍAS:
     * Fuente de datos optimizada para las tarjetas Bento (Ley #3.2 On-Demand Local).
     */
    private val superCategoryMetadata: Flow<List<SuperCategory>> = repository.getSuperCategoryMetadata()
        .map { list ->
            list.map { meta ->
                SuperCategory(
                    title = meta.title,
                    icon = meta.icon,
                    totalItems = meta.totalItems,
                    color = meta.color, 
                    isFavorite = false,
                    hasFavoriteCategories = meta.hasFavoriteCategories == 1
                )
            }
        }
        .flowOn(Dispatchers.Default)

    /**
     * [ELITE CARGA BAJO DEMANDA DE CATEGORÍAS (LAZY):
     * Solo carga las categorías reales cuando el usuario selecciona una supercategoría.
     */
    private val _selectedSuperTitle = MutableStateFlow<String?>(null)
    val selectedSuperCategoryItems: StateFlow<List<CategoryEntity>> = _selectedSuperTitle
        .flatMapLatest { title ->
            if (title == null) flowOf(emptyList())
            else {
                Log.d("CategoryViewModel", "📦 [ON_DEMAND] Cargando rubros para supercategoría: $title")
                repository.getCategoriesBySuperCategory(title)
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectSuperCategoryForDetail(title: String?) {
        _selectedSuperTitle.value = title
    }

    private val _activeSortFilters = MutableStateFlow<Set<String>>(setOf("view_bento", "sort_hot"))
    val activeSortFilters = _activeSortFilters.asStateFlow()


    /**
     * [ELITE ESTADO DE BÚSQUEDA ACTIVA (Ley #4 Zero Friction)
     * [FIX: Indica que se está esperando al debounce para evitar parpadeos.
     */
    val isSearching: StateFlow<Boolean> = combine(
        coordinator.normalizedSearchQuery,
        coordinator.debouncedNormalizedSearchQuery
    ) { normalized, debounced ->
        normalized.isNotEmpty() && normalized != debounced
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * [ELITE ACCESOS DIRECTOS (SSOT)
     */
    val homeShortcutIds: StateFlow<Set<String>> = shortcutRepository.getShortcutsByContext("home")
        .map { list -> list.map { it.targetId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _recentOptionIds = MutableStateFlow<List<String>>(listOf("view_bento", "sort_hot", "view_grid", "sort_nombre_asc"))
    val recentOptionIds = _recentOptionIds.asStateFlow()

    private val _hasMatches = MutableStateFlow(true)
    val hasMatches = _hasMatches.asStateFlow()

    /** 
     * [ELIT CATEGORÍAS PROCESADAS (Leyes #3.2 y #4)
     * OPTIMIZACIÓN: Límite inteligente de 80 items para asegurar scroll a 60fps.
     * [FIX: Sincronización perfecta entre tiempo real y debounce para evitar estados inconsistentes.
     */
    val sortedCategories: StateFlow<List<CategoryEntity>> = combine(
        allCategories, 
        coordinator.normalizedSearchQuery,
        coordinator.debouncedNormalizedSearchQuery, 
        _activeSortFilters, 
        homeShortcutIds
    ) { all, realTime, debounced, filters, shortcutIds ->
        // Si el usuario empieza a escribir, activamos el disparador de carga completa
        if (realTime.isNotEmpty() && !_loadAllTrigger.value) {
            _loadAllTrigger.value = true
        }

        // Si el tiempo real es diferente al debounced, estamos en medio de una escritura.

        val source = if (debounced.isEmpty()) {
            _hasMatches.value = true
            all.take(80) 
        } else {
            // [AUDITORÍA]: Filtrado en memoria (Local-First) inmune a acentos y paréntesis
            val matched = all.filter { it.name.filtroDeTexto(debounced) }
            _hasMatches.value = matched.isNotEmpty()
            matched
        }
        
        source.map { it.copy(isFavorite = shortcutIds.contains(it.name)) }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * [ELITE SUPERCATEGORÍAS PROCESADAS (Vista Bento)
     * OPTIMIZACIÓN: Carga Shallow (Metadatos). Los items se cargan On-Demand en el detalle.
     */
    val superCategories: StateFlow<List<SuperCategory>> = combine(
        superCategoryMetadata, 
        _activeSortFilters, 
        homeShortcutIds
    ) { metadata, filters, shortcutIds ->
        val enriched = metadata.map { it.copy(isFavorite = shortcutIds.contains(it.title)) }

        when {
            filters.contains("sort_nombre_asc") -> enriched.sortedBy { it.title.lowercase() }
            filters.contains("sort_nombre_desc") -> enriched.sortedByDescending { it.title.lowercase() }
            filters.contains("sort_random") -> enriched.shuffled()
            filters.contains("sort_hot") -> {
                enriched.sortedWith(
                    compareByDescending<SuperCategory> { it.isFavorite }
                        .thenByDescending { it.hasFavoriteCategories }
                        .thenBy { it.title.lowercase() }
                )
            }
            else -> enriched.sortedBy { it.title.lowercase() }
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * [ELITE ESTADO DE CARGA (Ley #4 Zero Friction)
     * [FIX]: Ahora depende de superCategories, ya que allCategories es perezosa.
     */
    val isInitialLoading: StateFlow<Boolean> = superCategories.map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun manageShortcut(context: String, targetId: String, type: String, isAdd: Boolean, label: String? = null, icon: String? = null) {
        viewModelScope.launch {
            if (isAdd) {
                shortcutRepository.addShortcut(context, targetId, type, label, icon)
            } else {
                shortcutRepository.removeShortcut(context, targetId, type)
            }
        }
    }

    fun getShortcuts(context: String): Flow<List<ShortcutEntity>> {
        return shortcutRepository.getShortcutsByContext(context)
    }

    fun toggleCategoryFavorite(category: CategoryEntity) {
        viewModelScope.launch {
            repository.insertOrUpdate(category.copy(isFavorite = !category.isFavorite))
        }
    }

    fun toggleSortFilter(filterId: String) {
        val current = _activeSortFilters.value.toMutableSet()
        var added = false

        if (filterId.startsWith("view_")) {
            if (!current.contains(filterId)) {
                current.removeAll { it.startsWith("view_") }
                current.add(filterId)
                added = true
            }
        } else if (filterId.startsWith("sort_")) {
            if (current.contains(filterId)) {
                current.remove(filterId)
            } else {
                current.removeAll { it.startsWith("sort_") }
                if (filterId.isNotEmpty()) {
                    current.add(filterId)
                    added = true
                }
            }
        }
        _activeSortFilters.value = current

        if (added) {
            val recents = _recentOptionIds.value.toMutableList()
            recents.remove(filterId)
            recents.add(0, filterId)
            _recentOptionIds.value = recents.take(8)
        }
    }

    fun updateSearchQuery(query: String) {
        coordinator.updateSearchQuery(query)
    }

    fun clearFilters() {
        _activeSortFilters.value = setOf("view_bento", "sort_hot")
        coordinator.updateSearchQuery("")
    }
/**
    fun getCategoryDetail(category: CategoryEntity): String {
        return category.description.ifEmpty { " ${category.description}." }
    }
*/
    init {
        // [POLÍTICA ZERO COSTO]: No se dispara sincronización remota.
    }
}

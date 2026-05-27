package com.example.myapplication.presentation.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.common.extensions.matchesSmart
import com.example.myapplication.core.data.local.entity.CategoryEntity
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
 * Se define aquí por ser el dominio principal del Obrero de Categorías.
 * 
 * [OPTIMIZACIÓN: 'items' es opcional para permitir carga ligera (Bento inicial)
 * y carga pesada (Detalle) sin duplicar clases.
 */
data class SuperCategory(
    val title: String,
    val icon: String,
    val items: List<CategoryEntity> = emptyList(), // Opcional para carga ligera
    val totalItems: Int = 0, // [NUEVO] Contador directo de SQLite
    val color: Long = 0xFF1A1F26,
    val isFavorite: Boolean = false,
    val hasFavoriteCategories: Boolean = false
)

/**
 * --- CATEGORY VIEWMODEL (EL OBRERO DE CATEGORÍAS) ---
 * Encargado del "trabajo sucio": Filtrado, Ordenamiento, Agrupación y Búsqueda de Categorías.
 * Procesa los datos crudos del repositorio para entregarlos listos al Cerebro (BeBrain).
 */
// ======================================================================================
// --- SECCIÓN: CONFIGURACIÓN VISUAL (IDENTIDAD DE MARCA) ---
// ======================================================================================

/** 
 * [OBSOLETO]: Los colores ahora son persistentes en super_categories_table (Room).
 * Se mantiene temporalmente como fallback si fuera necesario, pero la UI prefiere
 * los colores inyectados desde el DAO.
 */
object CategoryVisuals {
    fun getColorFor(superCategory: String?): Long {
        return 0xFF1A1F26 // Gris oscuro genérico (Fallback)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository, // 🔥 [ELITE] Agregado para persistencia de shortcuts
    private val coordinator: AppActionCoordinator
) : ViewModel() {

    // ======================================================================================
    // --- 1. FUENTE DE DATOS: ENRIQUECIMIENTO DINÁMICO DE COLORES ---
    // ======================================================================================
    
    /** 
     * Lista completa de categorías.
     */
    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** 
     * [NUEVO] METADATOS LIGEROS DE SUPERCATEGORÍAS:
     * Fuente de datos optimizada para las tarjetas Bento. 
     * [INTELIGENCIA] Reacciona al query debounced para evitar saturación.
     */
    private val superCategoryMetadata: StateFlow<List<SuperCategory>> = coordinator.debouncedSearchQuery
        .flatMapLatest { repository.getSuperCategoryMetadata() }
        .map { list ->
            list.map { meta ->
                SuperCategory(
                    title = meta.title,
                    icon = meta.icon,
                    totalItems = meta.totalItems,
                    color = meta.color, // 🔥 [ELITE] Color dinámico desde Room
                    isFavorite = false,
                    hasFavoriteCategories = meta.hasFavoriteCategories == 1
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 
     * NUEVO BÚSQUEDA INTELIGENTE (ELITE):
     * Filtra las categorías en memoria usando algoritmos de normalización y matching difuso.
     * Reacciona al query debounced y NORMALIZADO del coordinador para máxima fluidez.
     */
    private val dbSearchResults: StateFlow<List<CategoryEntity>> = combine(
        allCategories,
        coordinator.debouncedNormalizedSearchQuery
    ) { all, normQuery ->
        if (normQuery.isEmpty()) {
            _isSearching.value = false
            emptyList()
        } else {
            _isSearching.value = true
            // Usamos matchesSmart con la query ya normalizada para ahorrar ciclos de CPU
            val results = all.filter { it.name.matchesSmart(normQuery) }
            _isSearching.value = false
            results
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * NUEVO CARGA BAJO DEMANDA DE CATEGORÍAS (LAZY):
     * Solo carga las categorías reales cuando el usuario selecciona una supercategoría.
     */
    private val _selectedSuperTitle = MutableStateFlow<String?>(null)
    val selectedSuperCategoryItems: StateFlow<List<CategoryEntity>> = _selectedSuperTitle
        .flatMapLatest { title ->
            if (title == null) flowOf(emptyList())
            else repository.getCategoriesBySuperCategory(title)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Alterna la carga perezosa de una supercategoría.
     */
    fun selectSuperCategoryForDetail(title: String?) {
        _selectedSuperTitle.value = title
    }

    private val _activeSortFilters = MutableStateFlow<Set<String>>(setOf("view_bento", "sort_hot"))
    val activeSortFilters = _activeSortFilters.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading = _isInitialLoading.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    /**
     * NUEVO LISTADO DE ACCESOS DIRECTOS ACTIVOS (REACTIVO - SSOT)
     */
    private val homeShortcuts: StateFlow<List<ShortcutEntity>> = shortcutRepository.getShortcutsByContext("home")
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val homeShortcutIds: StateFlow<Set<String>> = homeShortcuts
        .map { list -> list.map { it.targetId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    // --- NUEVO: TRACKING DE OPCIONES RECIENTES (HOME ELITE) ---
    private val _recentOptionIds = MutableStateFlow<List<String>>(listOf("view_bento", "sort_hot", "view_grid", "sort_nombre_asc"))
    val recentOptionIds = _recentOptionIds.asStateFlow()

    val searchQuery = coordinator.globalSearchQuery

    private val _hasMatches = MutableStateFlow(true)
    val hasMatches = _hasMatches.asStateFlow()

    // ======================================================================================
    // --- 2. TRABAJO SUCIO: PROCESAMIENTO DE CATEGORÍAS (ORDENAMIENTO Y BÚSQUEDA) ---
    // ======================================================================================

    /** 
     * CATEGORÍAS PROCESADAS: Aplica búsqueda y ordenamiento.
     * OPTIMIZACIÓN: Ahora usa dbSearchResults cuando hay un query activo.
     * [SSOT] Inyecta el estado de favoritos desde la tabla de shortcuts.
     */
    val sortedCategories: StateFlow<List<CategoryEntity>> = combine(
        allCategories, dbSearchResults, _activeSortFilters, coordinator.debouncedSearchQuery, homeShortcutIds
    ) { all, filtered, filters, query, shortcutIds ->
        val source = if (query.isEmpty()) {
            _hasMatches.value = true
            all.sortedBy { it.name.lowercase() }
        } else {
            _hasMatches.value = filtered.isNotEmpty()
            filtered
        }
        
        // Enriquecemos con el estado real de favoritos de shortcuts
        source.map { it.copy(isFavorite = shortcutIds.contains(it.name)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- EXTENSIONES DE BÚSQUEDA (OBRERO) SE MOVIERON A SearchUtils.kt ---

    /**
     * SUPERCATEGORÍAS PROCESADAS: Agrupa las categorías filtradas por su tipo.
     * OPTIMIZACIÓN: Ahora utiliza superCategoryMetadata como base ligera para evitar agrupamiento pesado en memoria.
     * [SSOT] Sincronizado con la base de datos de shortcuts.
     */
    val superCategories: StateFlow<List<SuperCategory>> = combine(
        superCategoryMetadata, 
        dbSearchResults, 
        _activeSortFilters, 
        homeShortcutIds,
        allCategories
    ) { args ->
        val metadata = args[0] as List<SuperCategory>
        val searchResults = args[1] as List<CategoryEntity>
        val filters = args[2] as Set<String>
        val shortcutIds = args[3] as Set<String>
        val allCats = args[4] as List<CategoryEntity>
        
        val isSearching = coordinator.debouncedSearchQuery.value.isNotEmpty()

        // --- SECCIÓN: ENRIQUECIMIENTO Y AGRUPAMIENTO DE BÚSQUEDA ---
        val enriched = metadata.map { meta ->
            val itemsForThisSuper = if (isSearching) {
                searchResults.filter { it.superCategory == meta.title }
            } else {
                emptyList()
            }

            // Calculamos si tiene categorías favoritas basadas en shortcuts
            val hasFavs = allCats.any { it.superCategory == meta.title && shortcutIds.contains(it.name) }

            meta.copy(
                isFavorite = shortcutIds.contains(meta.title),
                hasFavoriteCategories = hasFavs,
                items = itemsForThisSuper.map { it.copy(isFavorite = shortcutIds.contains(it.name)) }
            )
        }

        // --- SECCIÓN: ORDENAMIENTO DE SUPERCATEGORÍAS ---
        val sortedResult = when {
            filters.contains("sort_nombre_asc") -> enriched.sortedBy { it.title.lowercase() }
            filters.contains("sort_nombre_desc") -> enriched.sortedByDescending { it.title.lowercase() }
            filters.contains("sort_random") -> enriched.shuffled()
            
            // 🔥 [NUEVO] LÓGICA DE ORDENAMIENTO "MÁS USADOS / FAVORITOS" 🔥
            filters.contains("sort_hot") -> {
                enriched.sortedWith(
                    compareByDescending<SuperCategory> { it.isFavorite }           // 1. Supercategorías favoritas
                        .thenByDescending { it.hasFavoriteCategories }           // 2. Tienen categorías favoritas
                        .thenBy { it.title.lowercase() }                          // 3. Orden alfabético (A-Z)
                )
            }

            else -> enriched.sortedBy { it.title.lowercase() }
        }
        sortedResult
    }
    .flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ======================================================================================
    // --- 4. ACCIONES Y COMANDOS DEL OBRERO ---
    // ======================================================================================

    /**
     * NUEVO GESTIÓN DE ACCESOS DIRECTOS (PERSISTENCIA)
     */
    fun manageShortcut(context: String, targetId: String, type: String, isAdd: Boolean, label: String? = null, icon: String? = null) {
        viewModelScope.launch {
            if (isAdd) {
                shortcutRepository.addShortcut(context, targetId, type, label, icon)
            } else {
                shortcutRepository.removeShortcut(context, targetId, type)
            }
        }
    }

    /**
     * NUEVO LISTADO DE ACCESOS DIRECTOS ACTIVOS (REACTIVO)
     */
    fun getShortcuts(context: String): Flow<List<ShortcutEntity>> {
        return shortcutRepository.getShortcutsByContext(context)
    }

    /** Alterna el estado de favorito de una categoría */
    fun toggleCategoryFavorite(category: CategoryEntity) {
        viewModelScope.launch {
            repository.insertOrUpdate(category.copy(isFavorite = !category.isFavorite))
        }
    }

    /** Actualiza los filtros de ordenamiento y vista */
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

        // Actualizar Recientes (Solo si se activó una opción nueva)
        if (added) {
            val recents = _recentOptionIds.value.toMutableList()
            recents.remove(filterId)
            recents.add(0, filterId)
            _recentOptionIds.value = recents.take(8)
        }
    }

    /** Actualiza la consulta de búsqueda */
    fun updateSearchQuery(query: String) {
        coordinator.updateSearchQuery(query)
    }

    /** Resetea todos los filtros a su estado inicial */
    fun clearFilters() {
        _activeSortFilters.value = setOf("view_bento", "sort_hot")
        coordinator.updateSearchQuery("")
    }

    /** 
     * [NUEVO] Genera el detalle informativo de la categoría 
     * Conecta el badge de información con la descripción de la base de datos (Room).
     */
    fun getCategoryDetail(category: CategoryEntity): String {
        return category.description.ifEmpty { " ${category.description}." }
    }

    init {
        // [POLÍTICA ZERO COSTO]: No se dispara sincronización remota.
        // La base de datos se siembra localmente en BeBrainViewModel.
        _isInitialLoading.value = false
    }
}











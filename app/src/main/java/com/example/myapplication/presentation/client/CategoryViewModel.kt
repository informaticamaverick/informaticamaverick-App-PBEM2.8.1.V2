package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==========================================================================================
// --- SECCIÓN: MODELOS DE DATOS PARA CATEGORÍAS (EL OBRERO) ---
// ==========================================================================================

/**
 * Modelo de datos para representar una agrupación de categorías.
 * Se define aquí por ser el dominio principal del Obrero de Categorías.
 */
data class SuperCategory(
    val title: String,
    val icon: String,
    val items: List<CategoryEntity>,
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
 * DICCIONARIO MAESTRO DE COLORES MATE/PASTEL
 * Centraliza la identidad visual por Supercategoría.
 */
object CategoryVisuals {
    private val superCategoryColors = mapOf(
        // --- COLORES CÁLIDOS (PASTEL) ---
        "Salud y Medicina" to 0xFFFFD1D1,            // Rosa Suave
        "Bienestar y Terapias Alternativas" to 0xFFE1BEE7, // Lavanda
        "Cuidado Personal y Belleza" to 0xFFF8BBD0,      // Rosa Intenso Pastel
        "Moda y Textil" to 0xFFF3E5F5,                   // Violeta Nube
        "Eventos y Entretenimiento" to 0xFFFFF9C4,       // Crema
        "Gastronomía y Bares" to 0xFFFFCCBC,             // Durazno
        "Mascotas y Veterinaria" to 0xFFF1F8E9,          // Menta Pálido
        "Seguridad y Emergencias" to 0xFFEF9A9A,         // Rojo Mate

        // --- COLORES FRÍOS Y TECNOLÓGICOS (MATE) ---
        "Tecnología y Sistemas" to 0xFFB2EBF2,           // CELESTE
        "Ciencias y Humanidades" to 0xFF80DEEA,          // Cian Suave
        "Turismo y Hotelería" to 0xFFE1F5FE,             // Azul Hielo
        "Servicios Ingenieria" to 0xFFBBDEFB,            // Acero Pastel
        "Limpieza y Saneamiento" to 0xFFCFD8DC,          // Gris Azulado
        "Transporte y Logística" to 0xFFB0BEC5,          // Gris Mate
        "Jardinería y Paisajismo" to 0xFFC8E6C9,         // Verde Musgo Pastel
        "Deporte y Recreación" to 0xFFB2DFDB,            // Turquesa Mate

        // --- COLORES TIERRA Y PROFESIONALES ---
        "Hogar y Mantenimiento" to 0xFFFFE0B2,           // Naranja Arena
        "Construcción y Oficios Pesados" to 0xFFD7CCC8,  // Terracota Suave
        "Servicios Automotores" to 0xFF90A4AE,           // Pizarra
        "Servicios Profesionales y Legales" to 0xFFC5CAE9, // Índigo Pastel
        "Finanzas y Negocios" to 0xFFA5D6A7,             // Esmeralda Mate
        "Marketing, Diseño y Medios" to 0xFFD1C4E9,      // Purpúra Suave
        "Educación y Clases" to 0xFFFFF176,              // Amarillo Sol Mate
        "Cuidado y Asistencia" to 0xFFFFAB91,            // Salmón
        "Agricultura y Ganadería" to 0xFFC5E1A5,         // Oliva Pastel
        "Esoterismo" to 0xFFB39DDB                       // Violeta Místico
    )

    fun getColorFor(superCategory: String?): Long {
        return superCategoryColors[superCategory] ?: 0xFF1A1F26 // Gris oscuro por defecto
    }
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    init {
        // --- AUTO-SINCRONIZACIÓN AL INICIAR EL OBRERO ---
        syncCategoriesWithFirebase()
    }

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

    private val _activeSortFilters = MutableStateFlow<Set<String>>(setOf("view_bento", "sort_hot"))
    val activeSortFilters = _activeSortFilters.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _hasMatches = MutableStateFlow(true)
    val hasMatches = _hasMatches.asStateFlow()

    // ======================================================================================
    // --- 2. TRABAJO SUCIO: PROCESAMIENTO DE CATEGORÍAS (ORDENAMIENTO Y BÚSQUEDA) ---
    // ======================================================================================

    /** 
     * CATEGORÍAS PROCESADAS: Aplica búsqueda (por inicio de palabra, ignorando acentos y mayúsculas) y ordenamiento.
     */
    val sortedCategories: StateFlow<List<CategoryEntity>> = combine(
        allCategories, _activeSortFilters, _searchQuery
    ) { cats, _, query ->
        // --- FILTRADO POR BÚSQUEDA (INICIO DE PALABRA, IGNORANDO ACENTOS) ---
        if (query.isEmpty()) {
            _hasMatches.value = true
            cats.sortedBy { it.name.lowercase() }
        } else {
            val normalizedQuery = query.prepareForSearch()
            
            // Función de ayuda para calcular el peso de la coincidencia (RELEVANCIA)
            fun getMatchWeight(cat: CategoryEntity): Int? {
                // 1. Match en Nombre de Categoría (Prioridad Alta: 0, 1, 2...)
                val nameWords = cat.name.prepareForSearch().split(" ", "(", ")").filter { it.isNotEmpty() }
                val nameMatchIndex = nameWords.indexOfFirst { it.startsWith(normalizedQuery) }
                if (nameMatchIndex != -1) return nameMatchIndex
                
                // 2. Match en Título de SuperCategoría (Prioridad Baja: 100)
                val superWords = (cat.superCategory ?: "").prepareForSearch().split(" ", "(", ")").filter { it.isNotEmpty() }
                if (superWords.any { it.startsWith(normalizedQuery) }) return 100
                
                return null
            }

            val filtered = cats.mapNotNull { cat ->
                val weight = getMatchWeight(cat)
                if (weight != null) cat to weight else null
            }
            .sortedWith(compareBy({ it.second }, { it.first.name.prepareForSearch() }))
            .map { it.first }

            if (filtered.isEmpty()) {
                _hasMatches.value = false
                cats.sortedBy { it.name.lowercase() } // Fallback: Lista completa
            } else {
                _hasMatches.value = true
                filtered
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- EXTENSIONES DE BÚSQUEDA (OBRERO) ---
    private fun String.removeAccents(): String {
        val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
    }

    private fun String.prepareForSearch(): String = this.removeAccents().lowercase().trim()

    private val _superCategoryFavorites = MutableStateFlow<Set<String>>(setOf("Hogar y Mantenimiento"))
    val superCategoryFavorites = _superCategoryFavorites.asStateFlow()

    /**
     * SUPER CATEGORÍAS PROCESADAS: Agrupa las categorías filtradas por su tipo.
     */
    val superCategories: StateFlow<List<SuperCategory>> = combine(
        sortedCategories, _activeSortFilters, _superCategoryFavorites
    ) { cats, filters, superFavs ->
        // --- SECCIÓN: AGRUPACIÓN Y ENRIQUECIMIENTO ---
        val grouped = cats.groupBy { it.superCategory }.map { (title, items) ->
            val superTitle = title ?: "Otros"
            SuperCategory(
                title = superTitle,
                icon = items.firstOrNull()?.superCategoryIcon ?: "📂",
                items = items.sortedBy { it.name.lowercase() }, // Orden alfabético interno de categorías
                // --- ASIGNACIÓN INTERNA DE COLOR POR SUPERCATEGORÍA ---
                color = CategoryVisuals.getColorFor(superTitle),
                isFavorite = superFavs.contains(superTitle),
                hasFavoriteCategories = items.any { it.isFavorite }
            )
        }

        // --- SECCIÓN: ORDENAMIENTO DE SUPERCATEGORÍAS ---
        when {
            filters.contains("sort_nombre_asc") -> grouped.sortedBy { it.title.lowercase() }
            filters.contains("sort_nombre_desc") -> grouped.sortedByDescending { it.title.lowercase() }
            filters.contains("sort_random") -> grouped.shuffled()
            
            // 🔥 [NUEVO] LÓGICA DE ORDENAMIENTO "MÁS USADOS / FAVORITOS" 🔥
            filters.contains("sort_hot") -> {
                grouped.sortedWith(
                    compareByDescending<SuperCategory> { it.isFavorite }           // 1. Supercategorías favoritas
                        .thenByDescending { it.hasFavoriteCategories }           // 2. Tienen categorías favoritas
                        .thenBy { it.title.lowercase() }                          // 3. Orden alfabético (A-Z)
                )
            }

            else -> grouped.sortedBy { it.title.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ======================================================================================
    // --- 4. ACCIONES Y COMANDOS DEL OBRERO ---
    // ======================================================================================

    /** Alterna el estado de favorito de una categoría */
    fun toggleCategoryFavorite(category: CategoryEntity) {
        viewModelScope.launch {
            repository.insertOrUpdate(category.copy(isFavorite = !category.isFavorite))
        }
    }

    /** Alterna el estado de favorito de una supercategoría */
    fun toggleSuperCategoryFavorite(title: String) {
        val current = _superCategoryFavorites.value.toMutableSet()
        if (current.contains(title)) {
            current.remove(title)
        } else {
            current.add(title)
        }
        _superCategoryFavorites.value = current
    }

    /** Actualiza los filtros de ordenamiento y vista */
    fun toggleSortFilter(filterId: String) {
        val current = _activeSortFilters.value.toMutableSet()
        if (filterId.startsWith("view_")) {
            current.removeAll { it.startsWith("view_") }
            current.add(filterId)
        } else if (filterId.startsWith("sort_")) {
            if (current.contains(filterId)) {
                current.remove(filterId)
            } else {
                current.removeAll { it.startsWith("sort_") }
                if (filterId.isNotEmpty()) current.add(filterId)
            }
        }
        _activeSortFilters.value = current
    }

    /** Actualiza la consulta de búsqueda */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Resetea todos los filtros a su estado inicial */
    fun clearFilters() {
        _activeSortFilters.value = setOf("view_bento", "sort_hot")
        _searchQuery.value = ""
    }

    /** 
     * [NUEVO] Genera el detalle informativo de la categoría 
     * Conecta el badge de información con la descripción de la base de datos (Room).
     */
    fun getCategoryDetail(category: CategoryEntity): String {
        return category.description.ifEmpty { " ${category.description}." }
    }

    /** Sincroniza categorías con Firebase Firestore */
    fun syncCategoriesWithFirebase() {
        viewModelScope.launch {
            repository.syncWithFirebase()
        }
    }
}

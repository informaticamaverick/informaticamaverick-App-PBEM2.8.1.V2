/*
package com.example.myapplication.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.CategoriaRapidaEntity
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.datos.repositorios.FastResultadoBusquedaPrestadorRepositorio
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.ui.componentes.FilterSortItem
import com.example.myapplication.core.utilidades.GeoUtils
import com.example.myapplication.core.dominio.mapeadores.PrestadorMappers
import com.example.myapplication.ui.componentes.be.modelos.BeDictionary
import com.example.myapplication.core.dominio.modelos.descubrimiento.ResultadoIndiceBusquedaShallowDominio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL PARA LA PANTALLA FAST RESULTADO BÚSQUEDA PRESTADOR (V2026.RADAR) ---
 * Orquestador táctico para la búsqueda de respuesta inmediata.
 * [ELITE 2026]: Alineado con la arquitectura Deep/Shallow y el motor de descubrimiento atómico.
 * [LEY #9]: Estándar Maverick en Español.
 */
@HiltViewModel
class FastResultadoBusquedaPrestadorViewModel @Inject constructor(
    private val fastRepository: FastResultadoBusquedaPrestadorRepositorio,
    private val categoryRepository: CategoriaRepositorio,
    private val AccesoDirectoRepositorio: AccesoDirectoRepositorio,
    private val coordinacion: CoordinadorAcciones
) : ViewModel() {

    private val _uiState = MutableStateFlow(FastUIState())
    val uiState: StateFlow<FastUIState> = _uiState.asStateFlow()

    /**
     * Accesos directos dinámicos guardados en Room.
     */
    val shortcuts: StateFlow<List<FilterSortItem>> = AccesoDirectoRepositorio.obtenerShortcutsPorContexto("fast")
        .map { list ->
            list.mapNotNull { shortcut ->
                BeDictionary.Filters[shortcut.idDestino]?.let { data ->
                    FilterSortItem(
                        id = data.id,
                        label = data.label,
                        emoji = data.emoji ?: "🔹",
                        color = data.color,
                        section = data.section
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Historial de categorías de emergencia.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val fastHistory: StateFlow<List<CategoriaEntity>> = fastRepository.obtenerHistorialRadar()
        .flatMapLatest { history ->
            if (history.isEmpty()) flowOf(emptyList())
            else {
                combine(history.map { h -> categoryRepository.obtenerPorId(h.id) }) { categories ->
                    categories.filterNotNull()
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE]: Inicia el escaneo táctico del radar.
     * [PROTOCOLO]: Utiliza el motor de descubrimiento atómico de :core.
     */
    fun startSearch(categoria: CategoriaEntity?) {
        val cat = categoria ?: _uiState.value.selectedCategory
        if (cat == null) return

        viewModelScope.launch {
            android.util.Log.d("FAST_VM", "🚀 [RADAR_INICIO] Escaneando zona para: ${cat.nombre}")
            fastRepository.registrarUsoRadar(cat)

            val direccion = coordinacion.direccionActiva.first()
            val lat = direccion?.latitud ?: 0.0
            val lon = direccion?.longitud ?: 0.0
            val cp = direccion?.codigoPostal ?: ""

            _uiState.update { it.copy(isSearching = true, searchFinished = false, selectedCategory = cat) }
            
            // 1. Obtener identidades shallow del repositorio táctico
            val identidades = fastRepository.buscarEnRadar(
                codigoPostal = cp,
                categoria = cat.id, // [ELITE]: Usamos el ID semántico para mayor precisión
                lat = lat,
                lng = lon,
                solo24h = _uiState.value.filters.atiende24h
            )

            // 2. Mapeo y Ordenamiento por Proximidad Real (Ley #4)
            val resultados = identidades.map { shallow ->
                val modeloUi = PrestadorMappers.deShallowAModeloUi(shallow, lat, lon)
                val dist = if (lat != 0.0 && shallow.latitud != 0.0) {
                    GeoUtils.calcularDistanciaKm(lat, lon, shallow.latitud, shallow.longitud)
                } else 0.0

                ProviderWithDistance(
                    service = modeloUi,
                    distanciaKm = dist,
                    estimatedMinutes = GeoUtils.estimarMinutosLlegada(dist),
                    lat = shallow.latitud,
                    lon = shallow.longitud
                )
            }.sortedBy { it.distanciaKm }

            _uiState.update { it.copy(
                isSearching = false, 
                searchFinished = true, 
                searchResults = resultados.take(15) 
            ) }
            
            android.util.Log.d("FAST_VM", "✅ [RADAR_EXITO] Se encontraron ${resultados.size} unidades operativas.")
        }
    }

    fun manageShortcut(id: String, add: Boolean) {
        viewModelScope.launch {
            if (add) AccesoDirectoRepositorio.agregarShortcut("fast", id, "filter")
            else AccesoDirectoRepositorio.eliminarShortcut("fast", id)
        }
    }

    fun toggleFilter(filterId: String) {
        _uiState.update { state ->
            val newFilters = when (filterId) {
                "filter_online" -> state.filters.copy(estaOnline = !state.filters.estaOnline)
                "filter_chat_24h" -> state.filters.copy(atiende24h = !state.filters.atiende24h)
                "filter_chat_sub" -> state.filters.copy(estaSuscrito = !state.filters.estaSuscrito)
                "filter_chat_local" -> state.filters.copy(tieneLocalFisico = !state.filters.tieneLocalFisico)
                else -> state.filters
            }
            state.copy(filters = newFilters)
        }
    }

    fun selectCategory(category: CategoriaEntity) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun resetSearch() {
        _uiState.update { it.copy(searchFinished = false, searchResults = emptyList(), selectedCategory = null) }
    }

    fun setBeSearchActive(active: Boolean) {
        _uiState.update { it.copy(isBeSearchActive = active) }
    }
}

data class FastUIState(
    val isSearching: Boolean = false,
    val searchFinished: Boolean = false,
    val searchResults: List<ProviderWithDistance> = emptyList(),
    val selectedCategory: CategoriaEntity? = null,
    val filters: FastFilterState = FastFilterState(),
    val beSearchCategories: List<CategoriaEntity> = emptyList(),
    val isBeSearchActive: Boolean = false
)

data class ProviderWithDistance(
    val service: PrestadorDominio,
    val distanciaKm: Double,
    val estimatedMinutes: Int,
    val lat: Double,
    val lon: Double
)

data class FastFilterState(
    val estaOnline: Boolean = false,
    val atiende24h: Boolean = false,
    val estaSuscrito: Boolean = false,
    val tieneLocalFisico: Boolean = false
)
*/

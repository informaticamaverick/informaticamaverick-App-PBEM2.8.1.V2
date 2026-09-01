package com.example.myapplication.viewmodel.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.utilidades.filtroDeTexto
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import com.example.myapplication.datos.repositorios.ResultadoBusquedaPrestadorRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.utilidades.GeoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

/**
 * --- VIEWMODEL: URGENCIAS RESULTADOS BÚSQUEDA (v2026.RADAR.PRO) ---
 * [PROPÓSITO]: Orquestar la búsqueda de respuesta inmediata y la visualización del Radar.
 * [LEY #1]: Pantalla Tonta. Centraliza la lógica de escaneo y transformación táctica.
 */
@HiltViewModel
class UrgenciasResultadosBusquedaViewModel @Inject constructor(
    private val searchRepository: ResultadoBusquedaPrestadorRepositorio,
    private val categoryRepository: CategoriaRepositorio,
    private val shortcutRepository: AccesoDirectoRepositorio,
    private val coordinator: CoordinadorAcciones,
    private val consultasRepo: com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UrgenciasResultadosBusquedaUiState())
    val uiState: StateFlow<UrgenciasResultadosBusquedaUiState> = _uiState.asStateFlow()

    private val ecosistemaMaestro: Flow<com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario?> = 
        auth.currentUser?.uid?.let { uid ->
            consultasRepo.obtenerCuentaMaestroUsuarioFlujo(uid)
        } ?: flowOf(null)

    init {
        // 1. Sincronización de Contexto Soberano
        coordinator.direccionActiva
            .onEach { dir -> _uiState.update { it.copy(direccionActiva = dir) } }
            .launchIn(viewModelScope)

        coordinator.estaGpsActivado
            .onEach { gps -> _uiState.update { it.copy(estaGpsActivo = gps) } }
            .launchIn(viewModelScope)

        // Reconstrucción de flujos de identidad sin depender del ViewModel de perfil
        combine(ecosistemaMaestro, coordinator.idPerfilSeleccionado) { cuenta, idPerfil ->
            if (cuenta == null) return@combine
            
            val nombre = if (idPerfil == null) {
                cuenta.usuario.perfil.nombreVisible
            } else {
                val sucursal = cuenta.empresas.flatMap { it.sucursales }.find { it.id == idPerfil }
                sucursal?.nombre ?: cuenta.usuario.perfil.nombreVisible
            }

            val origen = if (idPerfil == null) {
                cuenta.usuario.perfil.urlMiniatura ?: cuenta.usuario.perfil.urlFoto
            } else {
                val empresa = cuenta.empresas.find { it.sucursales.any { s -> s.id == idPerfil } }?.empresa
                empresa?.urlMiniatura ?: empresa?.urlFoto ?: cuenta.usuario.perfil.urlFoto
            }
            val foto = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(origen)

            _uiState.update { it.copy(
                nombrePerfilActivo = nombre,
                fotoPerfil = foto,
                ecosistemaMaestro = cuenta
            ) }
        }.launchIn(viewModelScope)

        // 2. Flujo Reactivo de Rubros (Favoritos vs Búsqueda)
        val favoritosFlow = shortcutRepository.obtenerShortcutsPorContexto("urgencia")
            .flatMapLatest { shortcuts ->
                if (shortcuts.isEmpty()) {
                    categoryRepository.obtenerResumenTodas().map { all ->
                        all.filter { it.id in listOf("HOGAR_CERRAJERO", "LOGISTICA_FLETES", "AUTO_AUXILIO") }
                            .map { com.example.myapplication.core.dominio.mapeadores.CategoriaMappers.deVistaADominio(it) }
                    }
                } else {
                    val idsFavoritos = shortcuts.map { it.idDestino }
                    categoryRepository.obtenerResumenTodas().map { all ->
                        all.filter { it.id in idsFavoritos }
                            .map { com.example.myapplication.core.dominio.mapeadores.CategoriaMappers.deVistaADominio(it) }
                    }
                }
            }

        // 3. Sincronización con la búsqueda del asistente (Be)
        @OptIn(FlowPreview::class)
        coordinator.beBusquedaMotor.consultaNormalizada
            .debounce(200)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                _uiState.update { it.copy(consultaFiltro = query) }
                if (query.isBlank()) {
                    favoritosFlow
                } else {
                    // 🔥 [ELITE FIX]: Usamos filtrado manual (Kotlin) para garantizar paridad con el Home
                    // y evitar fallos del motor FTS en términos cortos.
                    categoryRepository.obtenerResumenTodas().map { all ->
                        all.filter { it.nombre.filtroDeTexto(query) }
                           .map { com.example.myapplication.core.dominio.mapeadores.CategoriaMappers.deVistaADominio(it) }
                    }
                }
            }
            .onEach { list -> 
                _uiState.update { it.copy(rubrosMasUsados = list, isCargandoRubros = false) } 
            }
            .launchIn(viewModelScope)
    }

    /**
     * Flujo de Resultados Paginados (Para ViewMode.LIST)
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val resultadosPaginados: Flow<PagingData<PrestadorDominio>> = _uiState
        .map { Triple(it.rubroSeleccionado?.id, it.direccionActiva?.codigoPostal, it.searchMode) }
        .filter { it.first != null && it.second != null && it.third == SearchMode.RESULTS }
        .distinctUntilChanged()
        .flatMapLatest { (idCat, cp, _) ->
            val lat = _uiState.value.direccionActiva?.latitud ?: 0.0
            val lng = _uiState.value.direccionActiva?.longitud ?: 0.0
            val idConsulta = "urgencias_${cp}_${idCat}"

            searchRepository.obtenerResultadosTacticosFAST(idConsulta, idCat!!, cp!!, lat, lng)
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val resultadosRadar: Flow<List<ProviderUiModel>> = _uiState
        .map { Triple(it.rubroSeleccionado?.id, it.direccionActiva, it.searchMode) }
        .distinctUntilChanged()
        .flatMapLatest { (idCat, dir, mode) ->
            if (idCat == null || dir == null || mode != SearchMode.RESULTS) flowOf(emptyList())
            else {
                val idConsulta = "urgencias_${dir.codigoPostal}_${idCat}"
                combine(
                    searchRepository.obtenerListaRadar(idConsulta, dir.latitud ?: 0.0, dir.longitud ?: 0.0),
                    _uiState.map { it.filtrosActivos }.distinctUntilChanged()
                ) { list, filtros ->
                    transformAPuntosRadar(list, dir.latitud ?: 0.0, dir.longitud ?: 0.0, filtros)
                }
            }
        }

    fun establecerModoVista(modo: ViewMode) {
        _uiState.update { it.copy(viewMode = modo) }
    }

    fun seleccionarRubro(categoria: CategoriaDominio) {
        _uiState.update { it.copy(rubroSeleccionado = categoria) }
        iniciarEscaneo(categoria)
    }

    /**
     * 🔥 [v2026.ADS]: Inicia la animación del radar inmediatamente mientras el Ad carga.
     */
    fun prepararProtocoloDeBusqueda(categoria: CategoriaDominio) {
        _uiState.update { it.copy(
            searchMode = SearchMode.SEARCHING, 
            rubroSeleccionado = categoria 
        ) }
    }

    fun iniciarEscaneo(categoria: CategoriaDominio? = null) {
        val cat = categoria ?: _uiState.value.rubroSeleccionado ?: return
        viewModelScope.launch {
            if (_uiState.value.searchMode != SearchMode.SEARCHING) {
                _uiState.update { it.copy(searchMode = SearchMode.SEARCHING, rubroSeleccionado = cat) }
            }
            kotlinx.coroutines.delay(2500)
            _uiState.update { it.copy(searchMode = SearchMode.RESULTS) }
        }
    }

    fun resetearBusqueda() {
        _uiState.update { it.copy(searchMode = SearchMode.IDLE, rubroSeleccionado = null) }
    }

    fun alternarMenuUbicacion(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarMenuUbicacion = mostrar) }
    }

    fun alternarMenuPerfil(mostrar: Boolean) {
        _uiState.update { it.copy(mostrarMenuPerfil = mostrar) }
    }

    fun alternarFiltroTactico(idFiltro: String) {
        _uiState.update { state ->
            val nuevos = state.filtrosActivos.toMutableSet()
            if (nuevos.contains(idFiltro)) nuevos.remove(idFiltro)
            else nuevos.add(idFiltro)
            state.copy(filtrosActivos = nuevos)
        }
    }

    fun limpiarFiltrosTacticos() {
        _uiState.update { it.copy(filtrosActivos = emptySet()) }
    }

    private fun transformAPuntosRadar(
        prestadores: List<PrestadorDominio>, 
        userLat: Double, 
        userLon: Double,
        filtrosActivos: Set<String> = emptySet()
    ): List<ProviderUiModel> {
        return prestadores.map { p ->
            val pLat = p.latitud ?: 0.0
            val pLon = p.longitud ?: 0.0
            val bearing = calculateBearing(userLat, userLon, pLat, pLon)
            val angleRadians = Math.toRadians(bearing - 90.0)
            
            val distKm = p.distanciaKm ?: 0.0
            val radiusDp = ( (distKm / 5.0).coerceIn(0.1, 1.0) * 120.0).toFloat()
            
            // Lógica de resaltado (Highlight)
            val cumpleFiltros = filtrosActivos.isEmpty() || filtrosActivos.all { f ->
                when(f) {
                    "dist_2km" -> distKm <= 2.0
                    "local" -> p.tieneLocalFisico
                    else -> true
                }
            }

            ProviderUiModel(
                id = p.id,
                name = p.titulo,
                rating = p.reputacion.toString(),
                reviewCount = p.totalReseñas,
                distance = "${"%.1f".format(distKm)} km",
                eta = "${GeoUtils.estimarMinutosLlegada(distKm)} min",
                estPrice = "$ ---",
                category = p.idCategorias.firstOrNull() ?: "",
                isOnline = p.estaOnline,
                badge = if (p.estaVerificado) "VERIFICADO" else "DISPONIBLE",
                imageUrl = p.urlMiniatura ?: p.urlFoto,
                latOffsetDp = (cos(angleRadians) * radiusDp).toFloat(),
                lonOffsetDp = (sin(angleRadians) * radiusDp).toFloat(),
                tags = if (cumpleFiltros) listOf("highlight") else emptyList()
            )
        }
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1); val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)
        return (Math.toDegrees(atan2(sin(deltaLambda) * cos(phi2), cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda))) + 360.0) % 360.0
    }
}

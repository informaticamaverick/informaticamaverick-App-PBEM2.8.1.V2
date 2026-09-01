package com.example.myapplication.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.mapeadores.CategoriaMappers
import com.example.myapplication.datos.repositorios.ResultadoBusquedaPrestadorRepositorio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL: RESULTADO BÚSQUEDA PRESTADOR (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Orquestar la búsqueda táctica y el descubrimiento en cascada.
 * [LEY #1]: Pantalla Tonta. Centraliza la estabilidad de criterios.
 */
@HiltViewModel
class ResultadoBusquedaPrestadorViewModel @Inject constructor(
    private val searchRepository: ResultadoBusquedaPrestadorRepositorio,
    private val categoryRepository: CategoriaRepositorio,
    private val accesoDirectoRepositorio: com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio,
    private val suscripcionTopicDao: com.example.myapplication.core.datos.local.dao.SuscripcionTopicDao,
    private val generadorTopicos: GeneradorTópicosFCM,
    private val repositorioTopic: com.example.myapplication.core.dominio.repository.TopicoRepositorio,
    private val coordinador: CoordinadorAcciones,
    val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor // 🔥 [NEW]
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultadoBusquedaPrestadorUiState())
    val uiState: StateFlow<ResultadoBusquedaPrestadorUiState> = _uiState.asStateFlow()

    init {
        // --- VINCULACIÓN DE SOBERANÍA (Coordinador -> UiState) ---
        viewModelScope.launch {
            coordinador.direccionActiva.collect { direccion ->
                _uiState.update { it.copy(
                    direccionSeleccionada = direccion,
                    codigoPostalActual = direccion?.codigoPostal ?: "",
                    direccionVisible = direccion?.aTextoCorto() ?: "Sin ubicación"
                ) }
            }
        }
        
        viewModelScope.launch {
            coordinador.estaGpsActivado.collect { gps ->
                _uiState.update { it.copy(estaGpsActivo = gps) }
            }
        }

        // 🔥 [ELITE]: Observar Favoritos (Shortcuts) para sincronizar menús táctiles
        viewModelScope.launch {
            accesoDirectoRepositorio.obtenerShortcutsPorContexto("home")
                .map { list -> list.filter { it.tipo == "provider" }.map { it.idDestino }.toSet() }
                .collect { ids ->
                    _uiState.update { it.copy(idsFavoritos = ids) }
                }
        }
    }

    /**
     * Flujo de Resultados con Paging 3 centralizado en el Repositorio.
     * [LEY #14]: Al cambiar filtros, el flujo se invalida y recarga automáticamente.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val resultadosPaginados: Flow<PagingData<PrestadorDominio>> = combine(
        _uiState,
        beBusquedaMotor.consultaNormalizadaDebounced
    ) { state, query -> 
        Triple(state.idRubro, state.codigoPostalActual, state.filtros) to query 
    }
        .filter { it.first.first != null && it.first.second.isNotEmpty() }
        .distinctUntilChanged()
        .flatMapLatest { (info, query) ->
            val (idCat, cp, filtros) = info
            val lat = _uiState.value.direccionSeleccionada?.latitud ?: 0.0
            val lng = _uiState.value.direccionSeleccionada?.longitud ?: 0.0
            
            val idConsulta = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.PRESTADOR, cp, idCat)

            searchRepository.obtenerResultadosDeCategoria(
                idConsulta = idConsulta,
                rubro = idCat!!,
                cp = cp,
                lat = lat,
                lng = lng,
                query = query,
                solo24h = filtros.solo24h,
                soloVerificados = filtros.soloVerificados,
                conEnvio = filtros.conEnvio,
                estaOnline = filtros.estaOnline,
                orden = filtros.orden
            )
        }.cachedIn(viewModelScope)

    fun establecerCategoria(id: String) {
        viewModelScope.launch {
            val rubro = categoryRepository.obtenerPorId(id).firstOrNull()
            _uiState.update { it.copy(
                idRubro = id, 
                rubroInfo = rubro?.let { CategoriaMappers.deEntidadADominio(it) }
            ) }
            
            val cp = _uiState.value.codigoPostalActual
            if (cp.isNotBlank()) {
                val topicoOferta = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cp, id)
                val topicoConcurso = generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.CONCURSO, cp, id)
                
                android.util.Log.d("SEARCH_DISCOVERY", "🛰️ [PERSIST_SYNC] Guardando tópicos en Room: $topicoOferta, $topicoConcurso")
                
                listOf(topicoOferta, topicoConcurso).forEach { topic ->
                    suscripcionTopicDao.insertarSuscripcion(
                        com.example.myapplication.core.datos.local.entidades.SuscripcionTopicEntity(topic, "SEARCH_USER")
                    )
                    repositorioTopic.subscribeToTopic(topic)
                }
            }
        }
    }

    fun alternarFiltro(tipo: String) {
        _uiState.update { actual ->
            val nuevosFiltros = when(tipo) {
                "24h" -> actual.filtros.copy(solo24h = !actual.filtros.solo24h)
                "verificado" -> actual.filtros.copy(soloVerificados = !actual.filtros.soloVerificados)
                "envio" -> actual.filtros.copy(conEnvio = !actual.filtros.conEnvio)
                "online" -> actual.filtros.copy(estaOnline = !actual.filtros.estaOnline)
                else -> actual.filtros
            }
            actual.copy(filtros = nuevosFiltros)
        }
    }

    fun establecerOrden(orden: String) {
        _uiState.update { it.copy(filtros = it.filtros.copy(orden = orden)) }
    }

    fun alternarMenuPerfil(mostrar: Boolean) { _uiState.update { it.copy(mostrarMenuPerfil = mostrar) } }
    fun alternarMenuUbicacion(mostrar: Boolean) { _uiState.update { it.copy(mostrarMenuUbicacion = mostrar) } }
    fun establecerMenuFiltros(menu: String?) { _uiState.update { it.copy(menuFiltrosAbierto = menu) } }

    /**
     * 🔥 [ELITE]: Gestionar el acceso directo (Favorito) desde el menú táctico.
     */
    fun gestionarFavorito(idPrestador: String, agregar: Boolean, etiqueta: String? = null, icono: String? = null) {
        viewModelScope.launch {
            if (agregar) {
                accesoDirectoRepositorio.agregarShortcut("home", idPrestador, "provider", etiqueta, icono)
            } else {
                accesoDirectoRepositorio.eliminarShortcut("home", idPrestador)
            }
        }
    }
}



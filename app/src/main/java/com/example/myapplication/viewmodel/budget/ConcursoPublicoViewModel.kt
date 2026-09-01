package com.example.myapplication.viewmodel.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.ConcursoPublicoResumenDominio
import com.example.myapplication.core.datos.repositorios.ConcursoPublicoRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.dominio.filtros.FiltrosConcursoPublico
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.DropdownItemData
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

/**
 * --- VIEWMODEL DE CONCURSOS PÚBLICOS (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestador de la bandeja de Licitaciones para el cliente ("MIS CONCURSOS").
 * [LEY #9]: Estándar Mav en Español.
 * [LEY #1]: Pantallas Tontas. Expone un único UiState.
 */
@HiltViewModel
class ConcursoPublicoViewModel @Inject constructor(
    private val concursoPublicoRepositorio: ConcursoPublicoRepositorio,
    private val repositorioCategoria: CategoriaRepositorio,
    private val autenticacion: FirebaseAuth,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor,
    val coordinador: CoordinadorAcciones
) : ViewModel() {

    private val _eventosNavegacion = MutableSharedFlow<String>(replay = 0)
    val eventosNavegacion = _eventosNavegacion.asSharedFlow()

    private val _estaCargando = MutableStateFlow(true)
    private val _estaRefrescando = MutableStateFlow(false)
    private val _filtrosActivos = MutableStateFlow(FiltrosConcursoPublico())
    private val _mostrarMenuPerfil = MutableStateFlow(false)
    private val _idsSeleccionados = MutableStateFlow(emptySet<String>())

    val idSoberania = "root_concursos"

    /**
     * 🔥 [ELITE]: Filtros Consolidados para Concursos.
     * Usamos la consulta instantánea para una respuesta táctica ultra-rápida.
     */
    val filtrosEfectivos: StateFlow<FiltrosConcursoPublico> = combine(
        _filtrosActivos,
        beBusquedaMotor.consultaNormalizada // 🔥 [FIX]: Instantáneo (antes debounced)
    ) { manual, textoBe ->
        manual.copy(consulta = textoBe.ifEmpty { manual.consulta })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FiltrosConcursoPublico())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val concursosFlujo = combine(
        coordinador.idPerfilSeleccionado.map { it ?: autenticacion.currentUser?.uid ?: "" },
        filtrosEfectivos,
        repositorioCategoria.todasLasCategorias
    ) { userId, filtros, categorias ->
        Triple(userId, filtros, categorias)
    }.flatMapLatest { (userId, filtros, categorias) ->
        val mapaCategorias = categorias.associateBy { it.id }
        concursoPublicoRepositorio.buscarConcursosPropiosResumen(userId, filtros)
            .map { lista ->
                lista.map { sqlView ->
                    val cat = mapaCategorias[sqlView.concurso.idCategoria]
                    ConcursoPublicoResumenDominio(
                        concursoRaw = sqlView.concurso,
                        nombreCategoria = cat?.nombre ?: sqlView.concurso.idCategoria,
                        iconoCategoria = cat?.icono ?: "📋",
                        totalOfertas = sqlView.totalOfertas,
                        ofertasNoLeidas = sqlView.ofertasNuevas
                    )
                }
            }
            .onEach { lista ->
                coordinador.establecerTieneCoincidencias(filtros.consulta.isEmpty() || lista.isNotEmpty())
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rubrosEnUsoFlujo = coordinador.idPerfilSeleccionado
        .map { it ?: autenticacion.currentUser?.uid ?: "" }
        .flatMapLatest { userId ->
            concursoPublicoRepositorio.obtenerRubrosEnUso(userId)
        }

    private val itemsCategoriaFlujo = combine(
        rubrosEnUsoFlujo,
        repositorioCategoria.todasLasCategorias
    ) { idsEnUso, todasLasCats ->
        val mapa = todasLasCats.associateBy { it.id }
        idsEnUso.mapNotNull { id ->
            mapa[id]?.let { cat ->
                DropdownItemData(
                    id = "cat_${cat.id}",
                    label = cat.nombre,
                    section = "Rubros en uso",
                    emoji = cat.icono
                )
            }
        }.sortedBy { it.label }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todasLasCategoriasFlujo = repositorioCategoria.todasLasCategorias

    val uiState: StateFlow<ConcursoPublicoUiState> = combine(
        concursosFlujo,
        _estaCargando,
        _estaRefrescando,
        filtrosEfectivos,
        itemsCategoriaFlujo,
        todasLasCategoriasFlujo,
        _mostrarMenuPerfil,
        _idsSeleccionados,
        coordinador.idPerfilSeleccionado,
        coordinador.estaMultiseleccionActiva
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val concursos = args[0] as List<ConcursoPublicoResumenDominio>
        val cargando = args[1] as Boolean
        val refrescando = args[2] as Boolean
        val filtros = args[3] as FiltrosConcursoPublico
        val itemsCat = args[4] as List<DropdownItemData>
        val todasLasCats = args[5] as List<com.example.myapplication.core.datos.local.entidades.CategoriaEntity>
        val menuPerfil = args[6] as Boolean
        val seleccionados = args[7] as Set<String>
        val idPerfil = args[8] as String?
        val multiActivo = args[9] as Boolean

        ConcursoPublicoUiState(
            concursos = concursos,
            estaCargando = cargando,
            estaRefrescando = refrescando,
            filtros = filtros,
            itemsCategoria = itemsCat,
            todasLasCategorias = todasLasCats,
            itemsFiltro = listOf(
                BeDictionary.Filters["filter_concurso_activo"]!!,
                BeDictionary.Filters["filter_concurso_cerrado"]!!,
                BeDictionary.Filters["filter_concurso_adjudicado"]!!,
                BeDictionary.Filters["filter_concurso_no_leidos"]!!
            ),
            itemsOrden = listOf(
                BeDictionary.Sorts["sort_date"]!!,
                BeDictionary.Sorts["sort_alpha"]!!,
                BeDictionary.Sorts["sort_concursos_conteo"]!!
            ),
            mostrarMenuPerfil = menuPerfil,
            idPerfilSeleccionado = idPerfil ?: "personal",
            estaMultiseleccion = multiActivo,
            idsSeleccionados = seleccionados,
            totalItems = concursos.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConcursoPublicoUiState())

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            _estaCargando.value = false
        }

        // 🔥 [ELITE REACTIVITY]: Escuchar el Coordinador para limpiar selección si Be cancela.
        viewModelScope.launch {
            coordinador.estaMultiseleccionActiva.collect { activa ->
                if (!activa) {
                    _idsSeleccionados.value = emptySet()
                }
            }
        }
    }

    fun alternarSeleccionItem(id: String) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (!actual.remove(id)) actual.add(id)
        _idsSeleccionados.value = actual
        
        if (actual.isEmpty()) {
            coordinador.actualizarMultiseleccion(false)
            coordinador.actualizarTodoSeleccionado(false)
        } else {
            if (!coordinador.estaMultiseleccionActiva.value) {
                coordinador.actualizarMultiseleccion(true)
            }
            coordinador.actualizarTodoSeleccionado(actual.size >= uiState.value.concursos.size)
        }
    }

    fun seleccionarTodo(ids: List<String>) {
        _idsSeleccionados.value = ids.toSet()
        coordinador.actualizarMultiseleccion(true)
        coordinador.actualizarTodoSeleccionado(true)
    }

    fun deseleccionarTodo() {
        _idsSeleccionados.value = emptySet()
        // 🔥 [v2026.ELITE]: Mantenemos el modo abierto para alternancia infinita.
        coordinador.actualizarTodoSeleccionado(false)
    }

    fun eliminarSeleccionados() {
        viewModelScope.launch {
            val aEliminar = _idsSeleccionados.value.toList()
            aEliminar.forEach { id ->
                concursoPublicoRepositorio.eliminarConcursoLocal(id)
            }
            deseleccionarTodo()
            coordinador.mostrarToast("Concursos eliminados correctamente", TipoBeToast.EXITO)
        }
    }

    fun forzarCierreConcurso(id: String) {
        viewModelScope.launch {
            concursoPublicoRepositorio.obtenerConcursoPorId(id)?.let { concurso ->
                val cerrado = concurso.copy(estado = "CERRADA") 
                concursoPublicoRepositorio.guardarConcursoLocalConMultimedia(cerrado)
                deseleccionarTodo()
                coordinador.mostrarToast("Concurso finalizado correctamente", TipoBeToast.EXITO)
            }
        }
    }

    fun alternarFiltro(id: String) {
        _filtrosActivos.update { current ->
            when {
                id == "CLEAR_ALL" -> FiltrosConcursoPublico(consulta = current.consulta, orden = current.orden)
                id == "filter_concurso_activo" -> current.copy(soloActivos = !current.soloActivos)
                id == "filter_concurso_cerrado" -> current.copy(soloCerrados = !current.soloCerrados)
                id == "filter_concurso_adjudicado" -> current.copy(soloAdjudicados = !current.soloAdjudicados)
                id == "filter_concurso_no_leidos" -> current.copy(soloNoLeidos = !current.soloNoLeidos)
                id.startsWith("sort_") -> current.copy(orden = id)
                id.startsWith("cat_") -> {
                    val catId = id.removePrefix("cat_")
                    val nuevasCats = current.idsCategorias.toMutableSet()
                    if (!nuevasCats.remove(catId)) nuevasCats.add(catId)
                    current.copy(idsCategorias = nuevasCats)
                }
                else -> current
            }
        }
    }

    fun alternarMenuPerfil(visible: Boolean) {
        _mostrarMenuPerfil.value = visible
    }

    fun refrescar() {
        viewModelScope.launch {
            _estaRefrescando.value = true
            kotlinx.coroutines.delay(1000)
            _estaRefrescando.value = false
        }
    }
}

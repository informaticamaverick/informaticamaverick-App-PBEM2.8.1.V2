package com.example.myapplication.viewmodel.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.myapplication.core.datos.repositorios.ArchiveroChatMultimediaRepositorio
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import com.example.myapplication.core.datos.repositorios.ConcursoPublicoRepositorio
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.dominio.modelos.ConcursoPublicoResumenDominio
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.coordinadores.CoordinadorNavegacion
import com.example.myapplication.core.dominio.motores.BeBusquedaMotor
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.uishared.estilos.SharedPalette
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- VIEWMODEL DE PRESUPUESTOS DE CONCURSO (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la vista soberana de presupuestos recibidos en una licitación.
 */
@HiltViewModel
class ConcursoPresupuestosViewModel @Inject constructor(
    private val archiveroRepo: ArchiveroChatMultimediaRepositorio, // 🔥 [NEW] SSOT Unificado
    private val presupuestoRepositorio: PresupuestoRepositorio,
    private val concursoRepositorio: ConcursoPublicoRepositorio,
    private val categoriaRepositorio: CategoriaRepositorio,
    private val auth: FirebaseAuth, // 🔥 [NEW]
    private val coordinator: CoordinadorAcciones, // 🔥 [NEW]
    val beBusquedaMotor: BeBusquedaMotor,
    val navCoordinador: CoordinadorNavegacion
) : ViewModel() {

    private val _idConcurso = MutableStateFlow<String?>(null)
    private val _estaRefrescando = MutableStateFlow(false)
    private val _filtrosActivos = MutableStateFlow<Set<String>>(emptySet())
    private val _idsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    private val _menuFiltrosAbierto = MutableStateFlow<String?>(null)

    val idSoberania = "concurso_presupuestos"

    private val currentLocalIdFlow = coordinator.idPerfilSeleccionado.map { it ?: "personal" }.flatMapLatest { profileId ->
        if (profileId == "personal") {
            auth.currentUser?.uid?.let { flowOf(it) } ?: flowOf("")
        } else {
            flowOf(profileId)
        }
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val concursoInfoFlujo = _idConcurso
        .filterNotNull()
        .flatMapLatest { id ->
            combine(
                concursoRepositorio.todosLosConcursos.map { list -> list.find { it.idConcurso == id } },
                categoriaRepositorio.todasLasCategorias
            ) { concurso, categorias ->
                concurso?.let {
                    val cat = categorias.find { it.id == concurso.idCategoria }
                    com.example.myapplication.core.dominio.modelos.ConcursoPublicoResumenDominio(
                        concursoRaw = it,
                        nombreCategoria = cat?.nombre ?: it.idCategoria,
                        iconoCategoria = cat?.icono ?: "📋",
                        totalOfertas = it.conteoPresupuestos,
                        ofertasNoLeidas = 0 
                    )
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val presupuestosPaginadosFlow = combine(
        currentLocalIdFlow,
        _idConcurso.filterNotNull(),
        beBusquedaMotor.consultaNormalizadaDebounced,
        _filtrosActivos
    ) { localId, idConcurso, query, filters ->
        archiveroRepo.obtenerPresupuestosPaginados(
            idLocal = localId,
            idRemoto = null,
            idConcurso = idConcurso,
            query = query,
            filters = filters
        ).cachedIn(viewModelScope)
    }.flatMapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val presupuestosDisponiblesIds = combine(
        currentLocalIdFlow,
        _idConcurso.filterNotNull(),
        beBusquedaMotor.consultaNormalizadaDebounced,
        _filtrosActivos
    ) { localId, idConcurso, query, filters ->
        archiveroRepo.obtenerPresupuestosIds(
            idLocal = localId,
            idRemoto = null,
            idConcurso = idConcurso,
            query = query,
            filters = filters
        )
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ConcursoPresupuestosUiState> = combine(
        concursoInfoFlujo,
        _estaRefrescando,
        _filtrosActivos,
        _idsSeleccionados,
        _menuFiltrosAbierto,
        coordinator.estaMultiseleccionActiva,
        currentLocalIdFlow.flatMapLatest { archiveroRepo.observarRubrosEnUso(it) },
        presupuestosDisponiblesIds
    ) { args ->
        val concurso = args[0] as ConcursoPublicoResumenDominio?
        val refrescando = args[1] as Boolean
        @Suppress("UNCHECKED_CAST")
        val filtros = args[2] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val seleccionados = args[3] as Set<String>
        val menuAbierto = args[4] as String?
        val multiActiva = args[5] as Boolean
        @Suppress("UNCHECKED_CAST")
        val rubros = args[6] as List<com.example.myapplication.core.datos.local.entidades.CategoriaEntity>
        @Suppress("UNCHECKED_CAST")
        val totalIds = args[7] as List<String>

        ConcursoPresupuestosUiState(
            presupuestosPaginados = presupuestosPaginadosFlow, // 🔥 SSOT estable
            concursoInfo = concurso,
            estaCargando = concurso == null,
            estaRefrescando = refrescando,
            filtrosActivos = filtros,
            itemsFiltro = listOf(
                DropdownItemData("filter_pending", "Pendientes", emoji = "⏳"),
                DropdownItemData("filter_accepted", "Aceptados", emoji = "✅"),
                DropdownItemData("filter_unread", "No leídos", emoji = "🔔")
            ),
            itemsOrden = listOf(
                DropdownItemData("sort_date", "Más Recientes", emoji = "📅"),
                DropdownItemData("sort_amount_asc", "Menor Precio", emoji = "💰"),
                DropdownItemData("sort_amount_desc", "Mayor Precio", emoji = "💎")
            ),
            itemsRubros = rubros.map { DropdownItemData("cat_${it.id}", it.nombre, emoji = it.icono) },
            menuFiltrosAbierto = menuAbierto,
            estaMultiseleccion = multiActiva,
            idsSeleccionados = seleccionados,
            totalItems = totalIds.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConcursoPresupuestosUiState())

    fun inicializar(id: String) {
        _idConcurso.value = id
        registrarHUD()
    }

    private fun registrarHUD() {
        val config = ContextoHUD.CONCURSOS.crearConfiguracionBase(
            mensajes = listOf(MensajeBe("📋", "Gestiona las ofertas recibidas.", null, SharedPalette.ElectricCyan)),
            pistaBusqueda = "BUSCAR EN OFERTAS..."
        ).copy(
            id = idSoberania,
            mostrarBe = true,            
            mostrarHerramientas = false, // 🔥 [FIX]: No mostrar herramientas por defecto
            mostrarBarraNavegacion = false,
            sistema = listOf("teclado") 
        )
        navCoordinador.registrarPantalla(config)
        beBusquedaMotor.establecerEstaBusquedaActiva(false)
    }

    fun alternarFiltro(id: String) {
        _filtrosActivos.update { actual ->
            if (id.startsWith("sort_")) {
                actual.filterNot { it.startsWith("sort_") }.toSet() + id
            } else {
                if (actual.contains(id)) actual - id else actual + id
            }
        }
    }

    fun establecerMenuFiltros(menu: String?) {
        _menuFiltrosAbierto.value = menu
    }

    fun refrescar() {
        viewModelScope.launch {
            _estaRefrescando.value = true
            kotlinx.coroutines.delay(1000.milliseconds)
            _estaRefrescando.value = false
        }
    }

    fun limpiarTodo() {
        _filtrosActivos.value = emptySet()
        beBusquedaMotor.limpiarConsulta()
    }

    init {
        // 🔥 [ELITE REACTIVITY]: Escuchar el Coordinador para limpiar selección si Be cancela.
        viewModelScope.launch {
            coordinator.estaMultiseleccionActiva.collect { activa ->
                if (!activa && uiState.value.idsSeleccionados.isNotEmpty()) {
                    _idsSeleccionados.value = emptySet()
                    registrarHUD() // Restaurar HUD base
                }
            }
        }
    }

    fun alternarSeleccion(id: String) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (id.isNotEmpty()) {
            if (actual.contains(id)) actual.remove(id) else actual.add(id)
        }
        _idsSeleccionados.value = actual
        
        val activa = actual.isNotEmpty()
        coordinator.actualizarMultiseleccion(activa)
        coordinator.actualizarTodoSeleccionado(activa && actual.size >= presupuestosDisponiblesIds.value.size)
        actualizarContratoHUD(activa)
    }

    private fun actualizarContratoHUD(activa: Boolean) {
        navCoordinador.actualizarContratoActual(
            ConfiguracionContextoBe(
                id = idSoberania,
                navegacion = listOf("compare_budgets"), // 🔥 [ELITE]: Isla Izquierda: Comparativa
                edicion = listOf("delete_multi", "select_all", "cancel"), // 🔥 [ELITE]: Isla Derecha: Edición ROG
                mostrarBe = true,
                mostrarHerramientas = activa,
                ocultarOjos = activa,
                mostrarBarraNavegacion = false
            )
        )
    }

    fun seleccionarTodo() {
        val todos = presupuestosDisponiblesIds.value
        if (_idsSeleccionados.value.size >= todos.size && todos.isNotEmpty()) {
            deseleccionarTodo()
        } else {
            _idsSeleccionados.value = todos.toSet()
            alternarSeleccion("") 
        }
    }

    fun deseleccionarTodo() {
        _idsSeleccionados.value = emptySet()
        coordinator.actualizarTodoSeleccionado(false)
        // 🔥 [v2026.ELITE]: No cerramos el modo, solo notificamos el cambio de estado.
    }

    fun eliminarSeleccionados() {
        val ids = _idsSeleccionados.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            ids.forEach { presupuestoRepositorio.eliminarPresupuesto(it) }
            _idsSeleccionados.value = emptySet()
            launch(kotlinx.coroutines.Dispatchers.Main) {
                coordinator.actualizarMultiseleccion(false)
                registrarHUD()
            }
        }
    }

    /**
     * 🔥 [ELITE]: Acciones Soberanas sobre el Concurso (Licitación).
     */
    fun eliminarConcurso() {
        val id = _idConcurso.value ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            concursoRepositorio.eliminarConcursoLocal(id)
        }
    }

    fun terminarConcurso() {
        val id = _idConcurso.value ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Placeholder: Actualizar estado a CERRADA/TERMINADA
            val actual = concursoRepositorio.obtenerConcursoPorId(id)
            if (actual != null) {
                concursoRepositorio.guardarConcursoLocalConMultimedia(
                    actual.copy(estado = "TERMINADA", fechaFin = System.currentTimeMillis())
                )
            }
        }
    }

    fun verDetallesConcurso() {
        // Placeholder para navegación o diálogo
        android.util.Log.d("ConcursoVM", "🔍 Ver detalles del concurso ${_idConcurso.value}")
    }

    override fun onCleared() {
        super.onCleared()
        navCoordinador.removerPantalla(idSoberania)
        beBusquedaMotor.establecerEstaBusquedaActiva(false)
    }
}

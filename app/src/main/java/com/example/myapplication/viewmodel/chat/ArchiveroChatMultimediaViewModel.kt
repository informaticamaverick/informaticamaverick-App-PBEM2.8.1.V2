package com.example.myapplication.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.myapplication.core.datos.repositorios.*
import com.example.myapplication.core.datos.local.dao.*
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.mapeadores.*
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.coordinadores.*
import com.example.myapplication.core.dominio.motores.BeBusquedaMotor
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.core.utilidades.ChatIdHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- VIEWMODEL DE ARCHIVERO MULTIMEDIA (v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar el historial de una conversación (Fotos, Presupuestos, Visitas, Turnos).
 */
@HiltViewModel
class ArchiveroChatMultimediaViewModel @Inject constructor(
    private val archiveroRepo: ArchiveroChatMultimediaRepositorio, // 🔥 [NEW] SSOT Multimedia
    private val chatRepository: ChatMotorSincRepositorio,
    private val budgetDao: PresupuestoFinalDao, // Mantenemos solo para eliminación por ahora
    private val eventoRepositorio: EventoRepositorio,
    private val consultasUserRepo: com.example.myapplication.datos.repositorios.ConsultasUsuarioRepositorio,
    private val consultasPrestadorRepo: com.example.myapplication.datos.repositorios.ArmadorPerfilPrestadorRepositorio,
    private val motorLocal: com.example.myapplication.core.dominio.motores.MotorSincLocal,
    private val auth: FirebaseAuth,
    val beBusquedaMotor: BeBusquedaMotor,
    val coordinador: CoordinadorAcciones,
    val navCoordinador: CoordinadorNavegacion
) : ViewModel() {

    private val _idRemoto = MutableStateFlow<String?>(null)
    private val _idLocal = MutableStateFlow<String?>(null)
    private val _tipoActivo = MutableStateFlow(TipoContenidoMultimedia.PRESUPUESTOS)
    private val _estaRefrescando = MutableStateFlow(false)
    private val _estaCargandoInicial = MutableStateFlow(true)
    private val _menuSelectorAbierto = MutableStateFlow(false)
    private val _idsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    private val _menuFiltrosAbierto = MutableStateFlow<String?>(null)
    private val _filtrosActivos = MutableStateFlow<Set<String>>(emptySet())

    private val _eventosNavegacion = MutableSharedFlow<String>(replay = 0)
    val eventosNavegacion = _eventosNavegacion.asSharedFlow()

    val idSoberania = "archivero_chat_multimedia"

    private val resolvedLocalId = _idLocal.map { local ->
        val miUid = auth.currentUser?.uid ?: ""
        if (local == "personal" || local == null) miUid else local
    }

    private val chatIdFlujo = combine(_idRemoto.filterNotNull(), resolvedLocalId) { remoto, local ->
        ChatIdHelper.generateChatId(local, remoto)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val prestadorFlujo = _idRemoto.flatMapLatest { id ->
        if (id == null || id == "global") flowOf(null)
        else {
            motorLocal.impactarPrestadorShallow(id)
            combine(consultasUserRepo.obtenerUsuarioCompletoFlujo(id), consultasPrestadorRepo.obtenerPerfilPolimorficoFlujo(id)) { u, p ->
                if (u != null) UsuarioMappers.deDominioAPrestadorUi(u.perfil)
                else p
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val rubrosEnUso: StateFlow<List<CategoriaEntity>> = resolvedLocalId.flatMapLatest { localId ->
        archiveroRepo.observarRubrosEnUso(localId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val presupuestosPaginadosFlow = combine(
        resolvedLocalId,
        _idRemoto,
        beBusquedaMotor.consultaNormalizadaDebounced,
        _filtrosActivos
    ) { localId, idRemoto, query, filters ->
        archiveroRepo.obtenerPresupuestosPaginados(
            idLocal = localId, 
            idRemoto = idRemoto, 
            idConcurso = null, 
            query = query, 
            filters = filters
        ).cachedIn(viewModelScope)
    }.flatMapLatest { it }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val imagenesPaginadasFlow = combine(
        resolvedLocalId,
        _idRemoto.map { it ?: "global" },
        beBusquedaMotor.consultaNormalizadaDebounced
    ) { localId, idRemoto, query ->
        archiveroRepo.obtenerImagenesPaginadas(localId, idRemoto, query).cachedIn(viewModelScope)
    }.flatMapLatest { it }

    // --- SECTOR: IDS DISPONIBLES (Para Seleccionar Todo) ---
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val presupuestosDisponiblesIds = combine(
        resolvedLocalId,
        _idRemoto,
        beBusquedaMotor.consultaNormalizadaDebounced,
        _filtrosActivos
    ) { localId, idRemoto, query, filters ->
        archiveroRepo.obtenerPresupuestosIds(
            idLocal = localId, 
            idRemoto = idRemoto, 
            idConcurso = null, 
            query = query, 
            filters = filters
        )
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val imagenesDisponiblesIds = combine(
        resolvedLocalId,
        _idRemoto.map { it ?: "global" },
        beBusquedaMotor.consultaNormalizadaDebounced
    ) { localId, idRemoto, query ->
        archiveroRepo.obtenerImagenesIds(localId, idRemoto, query)
    }.flatMapLatest { it }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val ubicacionesFlujo = combine(chatIdFlujo, beBusquedaMotor.consultaNormalizadaDebounced, _idRemoto) { chatId, consulta, idRemoto ->
        if (idRemoto == "global") flowOf(emptyList<MensajeEntity>())
        else chatRepository.obtenerSoloUbicaciones(chatId).map { it.filter { m -> m.contenido.contains(consulta, ignoreCase = true) } }
    }.flatMapLatest { it }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val eventosFlujo = combine(chatIdFlujo, beBusquedaMotor.consultaNormalizadaDebounced, _idRemoto) { chatId, consulta, idRemoto ->
        if (idRemoto == "global") flowOf(emptyList<EventoDominio>())
        else eventoRepositorio.obtenerPorChat(chatId).map { it.filter { e -> e.titulo.contains(consulta, ignoreCase = true) }.map { e -> EventoMappers.aUiModel(e) } }
    }.flatMapLatest { it }

    val uiState: StateFlow<ArchiveroChatMultimediaUiState> = combine(
        prestadorFlujo, _tipoActivo, _estaRefrescando, _menuSelectorAbierto, _idsSeleccionados, coordinador.estaMultiseleccionActiva, _filtrosActivos, _menuFiltrosAbierto, rubrosEnUso, _estaCargandoInicial,
        presupuestosDisponiblesIds, imagenesDisponiblesIds
    ) { array ->
        val tipo = array[1] as TipoContenidoMultimedia
        val presupuestosIds = array[10] as List<String>
        val imagenesIds = array[11] as List<String>
        
        val total = when (tipo) {
            TipoContenidoMultimedia.PRESUPUESTOS -> presupuestosIds.size
            TipoContenidoMultimedia.IMAGENES -> imagenesIds.size
            else -> 0
        }

        ArchiveroChatMultimediaUiState(
            prestador = array[0] as PrestadorDominio?,
            tipoActivo = tipo,
            presupuestosPaginados = presupuestosPaginadosFlow,
            imagenesPaginadas = imagenesPaginadasFlow,
            ubicaciones = emptyList(), 
            visitas = emptyList(),
            turnos = emptyList(),
            estaRefrescando = array[2] as Boolean,
            menuSelectorAbierto = array[3] as Boolean,
            idsSeleccionados = array[4] as Set<String>,
            estaMultiseleccion = array[5] as Boolean,
            filtrosActivos = array[6] as Set<String>,
            menuFiltrosAbierto = array[7] as String?,
            itemsRubros = (array[8] as List<CategoriaEntity>).map { DropdownItemData("cat_${it.id}", it.nombre, emoji = it.icono) },
            estaCargando = (array[9] as Boolean),
            totalItems = total,
            itemsFiltro = listOf(DropdownItemData("filter_pending", "Pendientes", emoji = "⏳"), DropdownItemData("filter_accepted", "Aceptados", emoji = "✅")),
            itemsOrden = listOf(DropdownItemData("sort_date", "Más Recientes", emoji = "📅"), DropdownItemData("sort_amount_asc", "Menor Precio", emoji = "💰"), DropdownItemData("sort_amount_desc", "Mayor Precio", emoji = "💎"))
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArchiveroChatMultimediaUiState())

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(600.milliseconds)
            _estaCargandoInicial.value = false
        }
        viewModelScope.launch {
            coordinador.estaMultiseleccionActiva.collect { activa ->
                if (!activa && _idsSeleccionados.value.isNotEmpty()) {
                    _idsSeleccionados.value = emptySet()
                    if (navCoordinador.contratoActivo.value.id == idSoberania) registrarHUD()
                }
            }
        }
    }

    fun inicializar(remoto: String, local: String) {
        _idRemoto.value = remoto
        _idLocal.value = local
        registrarHUD()
    }

    fun irAlChat(idRemoto: String) {
        viewModelScope.launch {
            val local = _idLocal.value ?: "personal"
            _eventosNavegacion.emit("NAV_CHAT_${idRemoto}_$local")
        }
    }

    private fun registrarHUD() {
        val config = ConfiguracionContextoBe(
            id = idSoberania,
            mensajes = listOf(MensajeBe("📂", if (_idRemoto.value == "global" || _idRemoto.value == null) "Centro Multimedia Global." else "Centro Multimedia del Chat.", null, SharedPalette.ElectricCyan)),
            pistaBusqueda = "¿BUSCÁS UN ARCHIVO O DOC? 📂📄",
            mostrarBe = true,
            mostrarHerramientas = false, // 🔥 [FIX]: Ocultamos herramientas por defecto
            ocultarHerramientasSistemaBusqueda = true, // 🔥 [NEW]: No mostrar teclado en búsqueda
            mostrarBarraNavegacion = false
        )
        navCoordinador.registrarPantalla(config)
        beBusquedaMotor.establecerEstaBusquedaActiva(false)
    }

    fun alternarFiltro(id: String) {
        _filtrosActivos.update { actual ->
            if (id == "CLEAR_ALL") emptySet()
            else if (id.startsWith("sort_")) actual.filterNot { it.startsWith("sort_") }.toSet() + id
            else if (id.startsWith("cat_")) { val sin = actual.filterNot { it.startsWith("cat_") }.toSet(); if (actual.contains(id)) sin else sin + id }
            else if (actual.contains(id)) actual - id else actual + id
        }
    }

    fun establecerMenuFiltros(menu: String?) { _menuFiltrosAbierto.value = menu }
    fun establecerMenuSelector(abierto: Boolean) { _menuSelectorAbierto.value = abierto }
    fun cambiarTipo(tipo: TipoContenidoMultimedia) { _tipoActivo.value = tipo; _menuSelectorAbierto.value = false; registrarHUD() }
    
    fun alternarSeleccion(id: String) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (id.isNotEmpty()) { if (actual.contains(id)) actual.remove(id) else actual.add(id) }
        _idsSeleccionados.value = actual
        val activa = actual.isNotEmpty()
        coordinador.actualizarMultiseleccion(activa)
        actualizarHUDMultiseleccion(activa)
    }

    private fun actualizarHUDMultiseleccion(activa: Boolean) {
        navCoordinador.actualizarContratoActual(ConfiguracionContextoBe(
            id = idSoberania, 
            navegacion = if (_tipoActivo.value == TipoContenidoMultimedia.PRESUPUESTOS) listOf("compare_budgets") else emptyList(), 
            edicion = listOf("delete_multi", "select_all", "cancel"), 
            mostrarBe = true, 
            mostrarHerramientas = activa, 
            ocultarOjos = activa, 
            ocultarHerramientasSistemaBusqueda = true, // 🔥 [FIX]: No mostrar teclado
            mostrarBarraNavegacion = false
        ))
    }

    fun seleccionarTodo() {
        val todos = when (_tipoActivo.value) {
            TipoContenidoMultimedia.PRESUPUESTOS -> presupuestosDisponiblesIds.value
            TipoContenidoMultimedia.IMAGENES -> imagenesDisponiblesIds.value
            else -> emptyList()
        }
        _idsSeleccionados.value = todos.toSet()
        alternarSeleccion("")
    }

    fun deseleccionarTodo() {
        _idsSeleccionados.value = emptySet()
        coordinador.actualizarTodoSeleccionado(false)
        // 🔥 [v2026.ELITE]: Mantenemos el modo abierto para alternancia.
    }

    fun eliminarSeleccionados() {
        val ids = _idsSeleccionados.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            when (_tipoActivo.value) {
                TipoContenidoMultimedia.PRESUPUESTOS -> ids.forEach { budgetDao.eliminarPorId(it) }
                TipoContenidoMultimedia.IMAGENES -> ids.forEach { chatRepository.eliminarMensaje(it) }
                else -> {}
            }
            _idsSeleccionados.value = emptySet()
            launch(kotlinx.coroutines.Dispatchers.Main) { coordinador.actualizarMultiseleccion(false); registrarHUD() }
        }
    }

    fun refrescar() { viewModelScope.launch { _estaRefrescando.value = true; kotlinx.coroutines.delay(800.milliseconds); _estaRefrescando.value = false } }
    
    fun limpiarTodo() {
        _filtrosActivos.value = emptySet()
        beBusquedaMotor.limpiarConsulta()
    }

    override fun onCleared() {
        super.onCleared()
        navCoordinador.removerPantalla(idSoberania)
        beBusquedaMotor.establecerEstaBusquedaActiva(false)
    }
}

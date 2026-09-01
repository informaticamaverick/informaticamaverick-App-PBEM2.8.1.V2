package com.example.myapplication.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.mapeadores.PresupuestoMappers
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.repositorios.PresupuestoRepositorio
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * --- MODELO DE UI PARA EL ARCHIVERO (v2026.ELITE) ---
 */
data class PresupuestoArchiveroUiModel(
    val id: String,
    val resumen: com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
)

enum class ModoArchivero {
    CHATS,
    CONCURSO
}

/**
 * --- VIEWMODEL DEL ARCHIVERO DE PRESUPUESTOS (v2026.ELITE) ---
 * [PROPÓSITO]: Manejar el historial de presupuestos directos para el chat o de un concurso específico.
 */
@HiltViewModel
class ArchiveroPresupuestoViewModel @Inject constructor(
    private val presupuestoRepositorio: PresupuestoRepositorio,
    private val repositorioCategoria: CategoriaRepositorio,
    private val prestadorDao: IdentidadPrestadorDao, 
    private val chatSincRepo: ChatMotorSincRepositorio, 
    private val autenticacion: FirebaseAuth,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor,
    val coordinador: CoordinadorAcciones,
    val navCoordinador: com.example.myapplication.coordinadores.CoordinadorNavegacion // 🔥 [NEW]
) : ViewModel() {

    private val _modo = MutableStateFlow(ModoArchivero.CHATS)
    val modo = _modo.asStateFlow()

    private val _idCategoriaSeleccionada = MutableStateFlow<String?>(null)
    val idCategoriaSeleccionada = _idCategoriaSeleccionada.asStateFlow()

    private val _ordenReciente = MutableStateFlow(true)
    val ordenReciente = _ordenReciente.asStateFlow()

    private val _idsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val idsSeleccionados = _idsSeleccionados.asStateFlow()

    private val _estaEnModoSeleccion = MutableStateFlow(false)
    val estaEnModoSeleccion = _estaEnModoSeleccion.asStateFlow()

    private val _idConcurso = MutableStateFlow<String?>(null)
    val idConcurso = _idConcurso.asStateFlow()

    /**
     * 🔥 [v2026.ELITE]: ID de soberanía vinculado al slot en la pila del HUD.
     */
    val idSoberania: String = "archivero_presupuestos_global"

    private val uidActual = autenticacion.currentUser?.uid ?: ""

    val rubrosEnUso: StateFlow<List<CategoriaEntity>> = combine(
        presupuestoRepositorio.todosLosPresupuestos,
        repositorioCategoria.todasLasCategorias,
        _modo,
        _idConcurso
    ) { presupuestos, categorias, modoActual, contestId ->
        val presupuestosValidos = presupuestos
            .filter { 
                if (modoActual == ModoArchivero.CONCURSO) it.idConcurso == contestId
                else it.idConcurso == null 
            }
            .filter { it.idCliente == uidActual || it.idPrestador == uidActual }
            
        val categoriasIds = presupuestosValidos
            .mapNotNull { it.idCategoria }
            .distinct()
        
        val rubros = categorias.filter { categoriasIds.contains(it.id) }.toMutableList()
        
        if (presupuestosValidos.any { it.idCategoria == null }) {
            rubros.add(0, CategoriaEntity(
                id = "SIN_RUBRO",
                nombre = "Sin Rubro",
                icono = "⚪",
                idSuperCategoria = "varios"
            ))
        }
        
        rubros
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presupuestosFiltrados: StateFlow<List<PresupuestoArchiveroUiModel>> = combine(
        presupuestoRepositorio.todosLosPresupuestos,
        prestadorDao.obtenerTodos(),
        repositorioCategoria.todasLasCategorias, // 🔥 [NEW]
        beBusquedaMotor.consultaNormalizadaDebounced,
        _idCategoriaSeleccionada,
        _ordenReciente,
        _modo,
        _idConcurso
    ) { array ->
        val presupuestos = array[0] as List<PresupuestoFinalEntity>
        val prestadores = array[1] as List<IdentidadPrestadorEntity>
        val todasCategorias = array[2] as List<CategoriaEntity>
        val busqueda = array[3] as String
        val categoriaId = array[4] as String?
        val reciente = array[5] as Boolean
        val modoActual = array[6] as ModoArchivero
        val contestId = array[7] as String?

        val mapaPrestadores = prestadores.associateBy { it.id }
        val mapaCategorias = todasCategorias.associateBy { it.id }
        
        presupuestos
            .filter { 
                if (modoActual == ModoArchivero.CONCURSO) it.idConcurso == contestId
                else it.idConcurso == null 
            }
            .filter { it.idCliente == uidActual || it.idPrestador == uidActual }
            .filter { p ->
                busqueda.isEmpty() || 
                (p.tituloTrabajo?.contains(busqueda, ignoreCase = true) ?: false) ||
                (p.numeroPresupuesto?.contains(busqueda, ignoreCase = true) ?: false)
            }
            .filter { p ->
                when (categoriaId) {
                    null -> true
                    "SIN_RUBRO" -> p.idCategoria == null
                    else -> p.idCategoria == categoriaId
                }
            }
            .let { lista ->
                if (reciente) lista.sortedByDescending { it.marcaTiempo }
                else lista.sortedBy { it.marcaTiempo }
            }
            .map { p ->
                val metaCat = p.idCategoria?.let { mapaCategorias[it] }
                val resumen = PresupuestoMappers.aResumenDominio(
                    entidad = p,
                    foto = mapaPrestadores[p.idPrestador]?.miniaturaBase64,
                    nombreCat = metaCat?.nombre ?: "Servicio",
                    iconoCat = metaCat?.icono ?: "📋"
                )
                PresupuestoArchiveroUiModel(
                    id = p.idPresupuesto,
                    resumen = resumen
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 🔥 [ELITE REACTIVITY]: Escuchar el Coordinador para limpiar selección si Be cancela.
        viewModelScope.launch {
            coordinador.estaMultiseleccionActiva.collect { activa ->
                if (!activa && _estaEnModoSeleccion.value) {
                    android.util.Log.d("ArchiveroVM", "🧹 [CLEAN] Limpiando selección por orden global de Be.")
                    _estaEnModoSeleccion.value = false
                    _idsSeleccionados.value = emptySet()
                    if (navCoordinador.contratoActivo.value.id == idSoberania) configurarHUD()
                }
            }
        }

        // 🔥 [v2026.ELITE]: Sincronización Reactiva de completitud de lista
        combine(
            _idsSeleccionados,
            presupuestosFiltrados
        ) { seleccionados, lista ->
            val total = lista.size
            seleccionados.isNotEmpty() && total > 0 && seleccionados.size >= total
        }.onEach { completo ->
            coordinador.actualizarTodoSeleccionado(completo)
        }.launchIn(viewModelScope)
    }

    fun cargarPresupuestosDeConcurso(id: String?) {
        _modo.value = ModoArchivero.CONCURSO
        _idConcurso.value = id
        _idCategoriaSeleccionada.value = null
        _estaEnModoSeleccion.value = false
        _idsSeleccionados.value = emptySet()
        configurarHUD()
        actualizarCoordinador()
    }

    fun configurarModoChats() {
        _modo.value = ModoArchivero.CHATS
        _idConcurso.value = null
        _idCategoriaSeleccionada.value = null
        _estaEnModoSeleccion.value = false
        _idsSeleccionados.value = emptySet()
        configurarHUD()
        actualizarCoordinador()
    }

    /**
     * 🔥 [ELITE]: Soberanía HUD del Archivero.
     */
    fun configurarHUD() {
        val modoActual = _modo.value
        val estaMulti = _estaEnModoSeleccion.value
        val pista = if (modoActual == ModoArchivero.CONCURSO) "BUSCAR EN OFERTAS..." else "BUSCAR EN ARCHIVO..."
        
        // 🔥 [FIX]: Activar modo búsqueda para mostrar herramientas de sistema (teclado)
        beBusquedaMotor.establecerEstaBusquedaActiva(true)

        // 🔥 [FIX]: "compare_budgets" solo existe en modo edición (multiselección)
        val edicionAcciones = if (estaMulti) {
            listOf("compare_budgets", "select_all", "delete_multi")
        } else {
            listOf("select_all", "delete_multi")
        }

        navCoordinador.actualizarContratoActual(
            com.example.myapplication.ui.componentes.be.modelos.ConfiguracionContextoBe(
                id = idSoberania, // 🔥 [FIX]: Usamos el ID vinculado para no pisar el tope ajeno
                primarias = emptyList(), // 🔥 [FIX]: Limpiamos primarias para evitar el botón flotando
                edicion = edicionAcciones,
                pistaBusqueda = pista,
                mostrarHerramientas = estaMulti, // 🔥 [FIX]: Solo mostrar herramientas si hay selección
                ocultarHerramientasSistemaBusqueda = true // 🔥 [FIX]: No mostrar teclado
            )
        )
    }

    fun alternarCategoria(id: String) {
        _idCategoriaSeleccionada.value = if (_idCategoriaSeleccionada.value == id) null else id
    }

    fun cambiarOrden(esReciente: Boolean) {
        _ordenReciente.value = esReciente
    }

    fun alternarSeleccion(id: String) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (actual.contains(id)) {
            actual.remove(id)
            if (actual.isEmpty()) _estaEnModoSeleccion.value = false
        } else {
            actual.add(id)
            _estaEnModoSeleccion.value = true
        }
        _idsSeleccionados.value = actual
        configurarHUD()
        actualizarCoordinador()
    }

    fun establecerModoSeleccion(activo: Boolean) {
        _estaEnModoSeleccion.value = activo
        if (!activo) _idsSeleccionados.value = emptySet()
        configurarHUD()
        actualizarCoordinador()
    }

    fun seleccionarTodo(todosLosIds: List<String>) {
        _idsSeleccionados.value = todosLosIds.toSet()
        _estaEnModoSeleccion.value = true
        configurarHUD()
        actualizarCoordinador()
    }

    fun deseleccionarTodo() {
        _idsSeleccionados.value = emptySet()
        // 🔥 [v2026.ELITE]: Mantenemos el modo abierto para alternancia.
        actualizarCoordinador()
    }

    /**
     * 🔥 [ELITE]: Ejecuta la eliminación física de Room y emite aviso al chat (Audit Trail).
     */
    fun eliminarSeleccionados() {
        val ids = _idsSeleccionados.value.toList()
        if (ids.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            android.util.Log.d("ArchiveroVM", "🗑️ [ELIMINAR_START] Cantidad: ${ids.size}")
            
            // 1. Obtener datos antes de borrar para el mensaje de sistema
            val todos = presupuestoRepositorio.todosLosPresupuestos.first()
            val aBorrar = todos.filter { it.idPresupuesto in ids }

            // 2. Borrado físico
            ids.forEach { id -> presupuestoRepositorio.eliminarPresupuesto(id) }

            // 3. Notificación al sistema de Chat (Tombstone Pattern)
            aBorrar.forEach { p ->
                val idOtro = if (p.idCliente == uidActual) p.idPrestador else p.idCliente
                val chatId = com.example.myapplication.core.utilidades.ChatIdHelper.generateChatId(p.idCliente, p.idPrestador)
                
                val label = p.numeroPresupuesto ?: p.tituloTrabajo ?: "N/A"
                chatSincRepo.enviarMensajeSistema(
                    idChat = chatId,
                    receptor = idOtro,
                    texto = "⚠️ Presupuesto eliminado: $label"
                )
            }

            // 4. Limpieza de estado UI
            _idsSeleccionados.value = emptySet()
            _estaEnModoSeleccion.value = false
            
            launch(Dispatchers.Main) {
                configurarHUD()
                actualizarCoordinador()
            }
        }
    }

    private fun actualizarCoordinador() {
        val esMulti = _estaEnModoSeleccion.value
        val todosIds = presupuestosFiltrados.value.map { it.id }
        val esTodoSeleccionado = esMulti && todosIds.isNotEmpty() && _idsSeleccionados.value.size >= todosIds.size

        android.util.Log.d("ArchiveroVM", "📡 [COORD_UPDATE] multi=$esMulti | todoSel=$esTodoSeleccionado")
        coordinador.actualizarMultiseleccion(esMulti)
        coordinador.actualizarTodoSeleccionado(esTodoSeleccionado)
    }

    fun limpiarFiltros() {
        _idCategoriaSeleccionada.value = null
        beBusquedaMotor.limpiarConsulta()
    }
}




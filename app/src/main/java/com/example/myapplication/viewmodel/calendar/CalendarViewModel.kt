package com.example.myapplication.viewmodel.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.ui.componentes.be.modelos.BeDictionary
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.example.myapplication.core.datos.repositorios.CategoriaRepositorio
import com.example.myapplication.core.datos.repositorios.EventoRepositorio
import com.example.myapplication.core.utilidades.filtroDeTexto
import java.util.Calendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DEL CALENDARIO (V2026.FINAL) ---
 * Centraliza la agenda del cliente unificando Visitas, Turnos y Envíos.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val EventoRepositorio: EventoRepositorio,
    private val categoryRepository: CategoriaRepositorio,
    private val prestadorDao: IdentidadPrestadorDao,
    private val beBusquedaMotor: com.example.myapplication.core.dominio.motores.BeBusquedaMotor,
    private val appActionCoordinator: CoordinadorAcciones,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    private val TAG = "CalendarViewModel"

    private val _selectedDate = MutableStateFlow<Long?>(System.currentTimeMillis())
    val selectedDate = _selectedDate.asStateFlow()

    private val _filtrosActivos = MutableStateFlow<Set<String>>(emptySet())
    val filtrosActivos = _filtrosActivos.asStateFlow()

    /**
     * 🔥 [ELITE]: SharedFlow para eventos de UI (mensajes de éxito/error).
     */
    private val _eventosUi = MutableSharedFlow<String>()
    val eventosUi = _eventosUi.asSharedFlow()

    private val _idsSeleccionados = MutableStateFlow<Set<String>>(emptySet())
    val idsSeleccionados = _idsSeleccionados.asStateFlow()

    val idSoberania = "root_calendario"

    init {
        // 🔥 [ELITE REACTIVITY]: Limpiar selección local si el modo se desactiva globalmente
        viewModelScope.launch {
            appActionCoordinator.estaMultiseleccionActiva.collect { activa ->
                if (!activa) {
                    _idsSeleccionados.value = emptySet()
                }
            }
        }
    }

    fun seleccionarFecha(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun alternarFiltro(id: String) {
        _filtrosActivos.update { current ->
            if (id == "CLEAR_ALL") emptySet()
            else if (current.contains(id)) current - id
            else current + id
        }
    }

    private val currentUserUid: String 
        get() = auth.currentUser?.uid ?: ""

    /**
     * Todos los eventos activos y futuros del cliente (Soberanía de Agenda).
     * 🔥 [LEY #14]: Filtrado en la fuente (SQL-First).
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val upcomingEvents: StateFlow<List<com.example.myapplication.core.dominio.modelos.EventoDominio>> = beBusquedaMotor.consultaNormalizadaDebounced
        .flatMapLatest { query ->
            EventoRepositorio.buscarPorCliente(currentUserUid, query)
        }.combine(prestadorDao.obtenerTodos()) { events, providers -> 
            events to providers 
        }.combine(_filtrosActivos) { (events, providers), filtros ->
            Triple(events, providers, filtros)
        }.combine(_selectedDate) { (events, providers, filtros), selDate ->
            val now = System.currentTimeMillis()
            
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val effectiveStart = selDate ?: (now - 900000)

            events.filter { 
                it.fechaInicioUtc >= effectiveStart.coerceAtMost(startOfToday)
            }.filter { event ->
                aplicarFiltrosDeTipoYRubro(event, filtros, providers)
            }.sortedBy { it.fechaInicioUtc }
             .map { com.example.myapplication.core.dominio.mapeadores.EventoMappers.aUiModel(it) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE]: Categorías contextuales basadas en los eventos actuales.
     */
    val categoryDropdownItems: StateFlow<List<DropdownItemData>> = combine(
        EventoRepositorio.obtenerPorCliente(currentUserUid),
        prestadorDao.obtenerTodos(),
        categoryRepository.todasLasCategorias
    ) { events, allProviders, allCats ->
        val providerIdsInEvents = events.map { it.idPropietarioSucursal }.toSet()
        val categoryIdsInEvents = events.mapNotNull { it.idCategoria }.toSet()
        
        val uniqueCategoryIds = mutableSetOf<String>()
        uniqueCategoryIds.addAll(categoryIdsInEvents)
        
        // También incluimos categorías de los prestadores en los eventos
        allProviders.filter { it.id in providerIdsInEvents }
            .forEach { uniqueCategoryIds.addAll(it.idCategorias) }
        
        val categoryMap = allCats.associateBy { it.id }
        uniqueCategoryIds.mapNotNull { id ->
            val meta = categoryMap[id]
            if (meta != null) {
                DropdownItemData(
                    id = "cat_$id",
                    label = meta.nombre,
                    section = "Rubros en agenda",
                    emoji = meta.icono
                )
            } else null
        }.sortedBy { it.label }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE]: Todos los timestamps de inicio de día que tienen al menos un evento.
     * Se usa para marcar puntos en el mini calendario.
     */
    val allEventDates: StateFlow<Set<Long>> = EventoRepositorio.obtenerPorCliente(currentUserUid)
        .map { events ->
            events.map { event ->
                Calendar.getInstance().apply {
                    timeInMillis = event.fechaInicioUtc
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val filterDropdownItems = flowOf(listOf(
        BeDictionary.Filters["filter_event_visit"]!!,
        BeDictionary.Filters["filter_event_appointment"]!!,
        BeDictionary.Filters["filter_event_shipping"]!!
    )).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortDropdownItems = flowOf(listOf(
        BeDictionary.Sorts["sort_date"]!!,
        BeDictionary.Sorts["sort_alpha"]!!
    )).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Agrupamiento por fecha para la lista principal.
     */
    val groupedEvents: StateFlow<Map<String, List<com.example.myapplication.core.dominio.modelos.EventoDominio>>> = upcomingEvents
        .map { events ->
            events.groupBy { it.fechaTexto }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Eventos pasados (vencidos) con soporte para búsqueda y filtros.
     * 🔥 [LEY #14]: Filtrado en la fuente (SQL-First).
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pastEvents: StateFlow<List<com.example.myapplication.core.dominio.modelos.EventoDominio>> = beBusquedaMotor.consultaNormalizadaDebounced
        .flatMapLatest { query ->
            EventoRepositorio.buscarPorCliente(currentUserUid, query)
        }.combine(prestadorDao.obtenerTodos()) { events, providers -> 
            events to providers 
        }.combine(_filtrosActivos) { (events, providers), filtros ->
            val now = System.currentTimeMillis()
            events.filter { it.fechaInicioUtc < now - 900000 }
                .filter { aplicarFiltrosDeTipoYRubro(it, filtros, providers) }
                .sortedByDescending { it.fechaInicioUtc }
                .map { com.example.myapplication.core.dominio.mapeadores.EventoMappers.aUiModel(it) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun aplicarFiltrosDeTipoYRubro(event: EventoEntity, filtros: Set<String>, allProviders: List<IdentidadPrestadorEntity>): Boolean {
        if (filtros.isEmpty()) return true
        
        // 1. Filtro de Tipo (Visita, Turno, etc)
        val filtrosDeTipo = filtros.filter { it.startsWith("filter_event_") }
        val matchesTipo = if (filtrosDeTipo.isEmpty()) true else {
            when (event.tipo) {
                TipoEvento.VISITA_TECNICA -> filtros.contains("filter_event_visit")
                TipoEvento.TURNO_CITA -> filtros.contains("filter_event_appointment")
                TipoEvento.ENVIO_FLETE -> filtros.contains("filter_event_shipping")
                else -> true
            }
        }
        
        if (!matchesTipo) return false

        // 2. Filtro de Rubro (Categoría)
        val activeCategories = filtros.filter { it.startsWith("cat_") }
        if (activeCategories.isNotEmpty()) {
            val eventCatId = event.idCategoria
            if (eventCatId != null && "cat_$eventCatId" in activeCategories) return true
            
            // Fallback: Verificar si el prestador tiene alguna de las categorías
            val provider = allProviders.find { it.id == event.idPropietarioSucursal }
            val providerCats = provider?.idCategorias ?: emptyList()
            if (!providerCats.any { "cat_$it" in activeCategories }) return false
        }
        
        return true
    }

    fun cancelarEvento(id: String) {
        viewModelScope.launch { 
            EventoRepositorio.actualizarEstado(id, EstadoEvento.CANCELADO)
        }
    }

    /**
     * 🔥 [ELITE]: Responder a una propuesta de cita (Turno/Visita).
     */
    fun responderACita(idEvento: String, aceptar: Boolean) {
        viewModelScope.launch {
            try {
                val nuevoEstado = if (aceptar) EstadoEvento.CONFIRMADO else EstadoEvento.CANCELADO
                EventoRepositorio.actualizarEstado(idEvento, nuevoEstado)
                _eventosUi.emit(if (aceptar) "¡Turno confirmado!" else "Propuesta rechazada.")
            } catch (e: Exception) {
                Log.e(TAG, "❌ [ERROR_CALENDAR] Fallo al responder: ${e.message}")
                _eventosUi.emit("Error al procesar la respuesta.")
            }
        }
    }

    /**
     * 🔥 [ELITE]: Cancelar un compromiso ya confirmado.
     */
    fun cancelarCompromiso(idEvento: String) {
        cancelarEvento(idEvento)
    }

    // --- SECCIÓN: MULTISELECCIÓN (v2026.ELITE) ---

    fun alternarSeleccionItem(id: String) {
        val actual = _idsSeleccionados.value.toMutableSet()
        if (!actual.remove(id)) actual.add(id)
        _idsSeleccionados.value = actual
        
        if (actual.isEmpty()) {
            appActionCoordinator.actualizarMultiseleccion(false)
            appActionCoordinator.actualizarTodoSeleccionado(false)
        } else {
            if (!appActionCoordinator.estaMultiseleccionActiva.value) {
                appActionCoordinator.actualizarMultiseleccion(true)
            }
            appActionCoordinator.actualizarTodoSeleccionado(actual.size >= upcomingEvents.value.size)
        }
    }

    fun seleccionarTodo(ids: List<String>) {
        _idsSeleccionados.value = ids.toSet()
        appActionCoordinator.actualizarMultiseleccion(true)
        appActionCoordinator.actualizarTodoSeleccionado(true)
    }

    fun deseleccionarTodo() {
        _idsSeleccionados.value = emptySet()
        // 🔥 [v2026.ELITE]: No cerramos el modo para permitir alternancia
        appActionCoordinator.actualizarTodoSeleccionado(false)
    }

    fun eliminarSeleccionados() {
        viewModelScope.launch {
            val ids = _idsSeleccionados.value.toList()
            EventoRepositorio.eliminarMasivo(ids)
            deseleccionarTodo()
            appActionCoordinator.mostrarToast("Compromisos eliminados", com.example.myapplication.ui.componentes.be.modelos.TipoBeToast.EXITO)
        }
    }
}

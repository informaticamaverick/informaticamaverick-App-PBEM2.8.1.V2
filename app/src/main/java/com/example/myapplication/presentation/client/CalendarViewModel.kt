package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.data.repository.AppActionCoordinator
import com.example.myapplication.data.repository.CalendarRepository
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import com.example.myapplication.presentation.components.BeSmallActionModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * --- VIEWMODEL DEL CALENDARIO (OBRERO MAVERICK) ---
 * Centraliza la lógica de filtrado, búsqueda global y ordenamiento.
 * Sigue la Regla de Oro: Procesa datos para que la UI sea "tonta".
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val categoryRepository: com.example.myapplication.data.repository.CategoryRepository,
    private val appActionCoordinator: AppActionCoordinator
) : ViewModel() {

    init {
        // [LIMPIEZA TEMPORAL] Eliminar eventos de prueba antiguos si el usuario lo desea.
        // O simplemente dejar que los nuevos eventos reales se normalicen.
        // Descomentar la siguiente línea para limpiar la DB al iniciar (solo una vez)
        // viewModelScope.launch { calendarRepository.allEvents.first().forEach { deleteEventPermanently(it.id) } }
    }

    // --- ESTADOS DE FILTRADO LOCAL ---
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    private val _activeFilters = MutableStateFlow(setOf<String>())
    val activeFilters = _activeFilters.asStateFlow()

    // Control para mostrar eventos pasados (vía Pull-to-Refresh)
    private val _showPastEvents = MutableStateFlow(false)
    val showPastEvents = _showPastEvents.asStateFlow()

    /**
     * HISTORIAL DE EVENTOS (ROOM)
     * Obtiene los eventos pasados ordenados de forma descendente.
     * Incluye eventos de hoy que ya pasaron (con margen de 2hs).
     */
    val pastEvents: StateFlow<List<CalendarEventEntity>> = calendarRepository.allEvents
        .map { events ->
            val nowTime = System.currentTimeMillis()
            val twoHoursAgo = nowTime - (2 * 60 * 60 * 1000)
            
            events.filter { event ->
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val timeMillis = try { sdf.parse("${event.date} ${event.time}")?.time ?: 0L } catch(e: Exception) { 0L }
                timeMillis < twoHoursAgo
            }.sortedByDescending { "${it.date} ${it.time}" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * CAJA DE HERRAMIENTAS DE BE (CALENDARIO)
     * Define las herramientas dinámicas para el asistente Be.
     */
    val beActions: StateFlow<List<BeSmallActionModel>> = flowOf(
        listOf(
            BeSmallActionModel(
                id = "goto_history",
                icon = Icons.Default.History,
                label = "Historial",
                emoji = "📜",
                tint = Color(0xFFFF9800), // MaverickOrange
                isDefault = true // Atajo Rápido (Banda Principal)
            )
        )
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * PRÓXIMOS EVENTOS (Destaque Carousel)
     * Detecta el grupo de compromisos más cercanos en el FUTURO.
     */
    val nextEvents: StateFlow<List<CalendarEventEntity>> = calendarRepository.allEvents
        .map { events ->
            val nowTime = System.currentTimeMillis()
            // El carrusel es estricto: solo muestra eventos que NO han pasado (o están pasando)
            val upcoming = events.filter { it.status != VisitStatus.CANCELLED }
                .mapNotNull { event ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val timeMillis = try { sdf.parse("${event.date} ${event.time}")?.time ?: 0L } catch(e: Exception) { 0L }
                    
                    if (timeMillis >= nowTime) {
                        event to timeMillis
                    } else null
                }
                .sortedBy { it.second }

            if (upcoming.isEmpty()) return@map emptyList<CalendarEventEntity>()

            val firstTimeMillis = upcoming.first().second
            upcoming.filter { it.second == firstTimeMillis }.map { it.first }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * FLUJO DE EVENTOS FILTRADOS Y PROCESADOS (SSOT)
     */
    private val _filteredEventsInternal: StateFlow<List<CalendarEventEntity>> = combine(
        calendarRepository.allEvents,
        appActionCoordinator.globalSearchQuery,
        appActionCoordinator.globalSelectedCategory,
        _activeFilters,
        _showPastEvents
    ) { events, searchQuery, globalCategory, filters, showPast ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        var result = events

        // --- 1. FILTRADO POR TIEMPO (Regla del Obrero) ---
        if (!showPast) {
            result = result.filter { it.date >= todayStr }
        }

        // --- 2. BÚSQUEDA GLOBAL (Coordinator SSOT) ---
        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.provider.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true) ||
                (it.categoryName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

        // --- 3. FILTRO POR CATEGORÍA (SSOT) ---
        globalCategory?.let { cat ->
            result = result.filter { it.categoryName == cat.name }
        }

        // --- 4. FILTROS DE ESTADO ---
        val showConfirmed = filters.contains("filter_verif")
        val showPending = filters.contains("filter_fast")
        if (showConfirmed && !showPending) {
            result = result.filter { it.status == VisitStatus.CONFIRMED }
        } else if (showPending && !showConfirmed) {
            result = result.filter { it.status == VisitStatus.PENDING }
        }

        // --- 5. FILTROS DE TIPO ---
        val showVisitas = filters.contains("cat_visita")
        val showTurnos = filters.contains("cat_turno")
        val showEnvios = filters.contains("cat_envio")
        
        if (showVisitas || showTurnos || showEnvios) {
            result = result.filter {
                (showVisitas && it.type == EventType.VISIT) ||
                (showTurnos && it.type == EventType.APPOINTMENT) ||
                (showEnvios && it.type == EventType.SHIPPING)
            }
        }

        // --- 6. ORDENAMIENTO NATURAL (CRÍTICO) ---
        // Normalizamos la hora para un ordenamiento alfabético/numérico consistente
        result.sortedWith(
            compareBy<CalendarEventEntity> { it.date }
                .thenBy { it.time.lowercase().replace("hs", "").trim() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    /**
     * EXCLUSIÓN DE PRÓXIMOS EVENTOS DE LA LISTA PRINCIPAL
     */
    val filteredEvents: StateFlow<List<CalendarEventEntity>> = combine(
        _filteredEventsInternal,
        nextEvents,
        _showPastEvents
    ) { events, nexts, showPast ->
        if (nexts.isNotEmpty() && !showPast) {
            val nextIds = nexts.map { it.id }.toSet()
            events.filter { it.id !in nextIds }
        } else {
            events
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * EVENTOS AGRUPADOS POR FECHA (Para UI de Secciones)
     */
    val groupedEvents: StateFlow<Map<String, List<CalendarEventEntity>>> = filteredEvents
        .map { events -> events.groupBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Categorías dinámicas para el menú Tornado.
     */
    val availableCategories: StateFlow<List<com.example.myapplication.presentation.components.ControlItem>> = calendarRepository.allEvents
        .map { events ->
            events.filter { it.categoryName != null }
                .distinctBy { it.categoryName }
                .map { event ->
                    com.example.myapplication.presentation.components.ControlItem(
                        label = event.categoryName!!,
                        icon = null,
                        emoji = event.categoryEmoji ?: "🏷️",
                        color = Color(event.type.colorLong),
                        id = "cat_${event.categoryName}"
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Mapa de días con su color predominante.
     */
    val daysWithEventColors: StateFlow<Map<String, Long>> = calendarRepository.allEvents
        .map { events ->
            events.filter { it.status != VisitStatus.CANCELLED }
                .groupBy { it.date }
                .mapValues { (_, eventsInDay) ->
                    val types = eventsInDay.map { it.type }
                    when {
                        types.contains(EventType.VISIT) -> EventType.VISIT.colorLong
                        types.contains(EventType.APPOINTMENT) -> EventType.APPOINTMENT.colorLong
                        types.contains(EventType.SHIPPING) -> EventType.SHIPPING.colorLong
                        else -> 0xFF00FFC2
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- ACCIONES DE UI ---

    fun togglePastEvents(show: Boolean) {
        _showPastEvents.value = show
    }

    fun updateSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    fun toggleFilter(filterId: String) {
        viewModelScope.launch {
            val current = _activeFilters.value.toMutableSet()
            val isActive = current.contains(filterId)
            
            if (isActive) current.remove(filterId) else current.add(filterId)
            _activeFilters.value = current

            // --- SINCRONIZACIÓN CON COORDINATOR (SSOT) ---
            if (filterId.startsWith("cat_")) {
                val catName = filterId.removePrefix("cat_")
                // Si estamos activando, buscamos la categoría real para el Coordinator
                if (!isActive) {
                    val category = categoryRepository.allCategories.firstOrNull()?.find { it.name == catName }
                    appActionCoordinator.selectCategory(category)
                } else {
                    appActionCoordinator.selectCategory(null)
                }
            }
        }
    }

    fun clearFilters() {
        _activeFilters.value = emptySet()
        _showPastEvents.value = false
        appActionCoordinator.updateSearchQuery("")
        appActionCoordinator.selectCategory(null)
    }

    fun cancelEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            calendarRepository.cancelEvent(event.id)
        }
    }

    fun deleteEventPermanently(eventId: String) {
        viewModelScope.launch {
            calendarRepository.deleteEvent(eventId)
        }
    }
    
    fun requestReschedule(event: CalendarEventEntity) {
        // Simulación de acción
    }
}

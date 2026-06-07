package com.example.myapplication.presentation.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.CalendarEventEntity
import com.example.myapplication.core.data.local.entity.TenderEntity // Placeholder if needed
import com.example.myapplication.core.data.local.entity.EventType
import com.example.myapplication.core.data.local.entity.VisitStatus
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.example.myapplication.core.data.repository.CalendarRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.data.repository.ShortcutRepository
import com.example.myapplication.presentation.components.DropdownItemData
import com.example.myapplication.presentation.components.FilterSortItem
import com.example.myapplication.presentation.features.home.CategoryVisuals
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.MaverickIcons
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
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository, // 🔥 Inyectamos favoritos
    private val appActionCoordinator: AppActionCoordinator
) : ViewModel() {

    // --- ESTADOS DE FILTRADO LOCAL ---
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    // --- SINCRONIZACIÓN CON COORDINADOR (SSOT) ---
    val activeFilters: StateFlow<Set<String>> = appActionCoordinator.activeFilters

    /**
     * Accesos directos dinámicos guardados en Room.
     */
    val shortcuts: StateFlow<List<FilterSortItem>> = shortcutRepository.getShortcutsByContext("calendar")
        .map { list ->
            list.mapNotNull { shortcut ->
                BeDictionary.Filters[shortcut.targetId]?.let { data ->
                    FilterSortItem(
                        id = data.id,
                        label = data.label,
                        emoji = data.emoji ?: "🔹",
                        icon = data.icon,
                        color = data.color,
                        section = data.section
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun manageShortcut(id: String, add: Boolean) {
        viewModelScope.launch {
            if (add) shortcutRepository.addShortcut("calendar", id, "filter")
            else shortcutRepository.removeShortcut("calendar", id, "filter")
        }
    }

    /**
     * CONTEO TOTAL DE EVENTOS (ROOM)
     */
    val allEventsCount: StateFlow<Int> = calendarRepository.allEvents
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _showPastEvents = MutableStateFlow(false)
    val showPastEvents = _showPastEvents.asStateFlow()

    /**
     * HISTORIAL DE EVENTOS (ROOM)
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
     * [ELITE SSOT] Emitimos solo IDs de comando.
     */
    val beActionIds: StateFlow<List<String>> = flowOf(
        listOf("goto_history")
    ).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * PRÓXIMOS EVENTOS (Destaque Carousel)
     */
    val nextEvents: StateFlow<List<CalendarEventEntity>> = calendarRepository.allEvents
        .map { events ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            val upcoming = events.filter {
                it.status != VisitStatus.CANCELLED && it.date >= todayStr 
            }.sortedBy { "${it.date} ${it.time}" }

            if (upcoming.isEmpty()) return@map emptyList<CalendarEventEntity>()
            val nextDate = upcoming.first().date
            upcoming.filter { it.date == nextDate }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeSortCriteria = MutableStateFlow<List<String>>(emptyList())
    val activeSortCriteria: StateFlow<List<String>> = _activeSortCriteria.asStateFlow()

    /**
     * FLUJO DE EVENTOS FILTRADOS Y PROCESADOS (SSOT)
     */
    private val _filteredEventsInternal: StateFlow<List<CalendarEventEntity>> = combine(
        calendarRepository.allEvents,
        appActionCoordinator.globalSearchQuery,
        appActionCoordinator.globalSelectedCategory,
        activeFilters,
        _showPastEvents,
        _activeSortCriteria,
        appActionCoordinator.selectedProfileId
    ) { args ->
        val events = args[0] as List<CalendarEventEntity>
        val searchQuery = args[1] as String
        val globalCategory = args[2] as? com.example.myapplication.core.data.local.entity.CategoryEntity
        val filters = args[3] as Set<String>
        val showPast = args[4] as Boolean
        val sortCriteria = args[5] as List<String>
        val profileId = args[6] as String?

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        var result = events

        // --- FILTRADO POR PERFIL (SSOT) ---
        // [FIX ELITE]: Por ahora, si no hay campo en la entidad, mostramos todo.
        // El profileId se recibe para futura implementación de filtrado multi-identidad.
        if (profileId != null) {
            // Log.d("CalendarVM", "Filtrando por perfil: $profileId")
        }

        if (!showPast) {
            result = result.filter { it.date >= todayStr }
        }

        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.provider.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true) ||
                (it.categoryName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

        globalCategory?.let { cat: com.example.myapplication.core.data.local.entity.CategoryEntity ->
            result = result.filter { it.categoryName == cat.name }
        }

        val showConfirmed = filters.contains("filter_verif")
        val showPending = filters.contains("filter_fast")
        if (showConfirmed && !showPending) {
            result = result.filter { it.status == VisitStatus.CONFIRMED }
        } else if (showPending && !showConfirmed) {
            result = result.filter { it.status == VisitStatus.PENDING }
        }

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

        // --- FILTRADO POR CATEGORÍA DINÁMICO ---
        val catFilters = filters.filter { it.startsWith("cat_") && it != "cat_visita" && it != "cat_turno" && it != "cat_envio" }
            .map { it.removePrefix("cat_") }
        if (catFilters.isNotEmpty()) {
            result = result.filter { it.categoryName in catFilters }
        }

        // --- ORDENAMIENTO ELITE EN CASCADA ---
        var comparator = compareBy<CalendarEventEntity> { it.date }
            .thenBy { it.time.lowercase().replace("hs", "").trim() }

        if (sortCriteria.isNotEmpty()) {
            sortCriteria.forEachIndexed { index, criteria ->
                val nextComparator = when (criteria) {
                    "sort_alpha" -> compareBy<CalendarEventEntity> { it.title.lowercase() }
                    "sort_date" -> compareBy<CalendarEventEntity> { it.date }.thenBy { it.time.lowercase().replace("hs", "").trim() }
                    else -> null
                }
                
                if (nextComparator != null) {
                    comparator = if (index == 0) nextComparator else comparator.thenComparing(nextComparator)
                }
            }
        }

        result.sortedWith(comparator)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    val groupedEvents: StateFlow<Map<String, List<CalendarEventEntity>>> = combine(
        filteredEvents,
        activeFilters
    ) { events, filters ->
        val isDateSortActive = filters.contains("sort_date") || filters.none { it.startsWith("sort_") }
        if (!isDateSortActive) mapOf("" to events)
        else events.groupBy { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * FILTROS Y ORDENAMIENTO (Mapeo Elite)
     */
    val filterDropdownItems: StateFlow<List<DropdownItemData>> = combine(
        calendarRepository.allEvents,
        categoryRepository.allCategories
    ) { events, allCats ->
        val list = mutableListOf<DropdownItemData>()
        
        // SECCIÓN: ESTADO
        BeDictionary.Filters["filter_verif"]?.let { list.add(it) }
        BeDictionary.Filters["filter_fast"]?.let { list.add(it) }

        // SECCIÓN: TIPO
        BeDictionary.Filters["filter_visits"]?.let { list.add(it.copy(id = "cat_visita", label = "Visitas Técnicas")) }
        BeDictionary.Filters["filter_appointments"]?.let { list.add(it.copy(id = "cat_turno", label = "Turnos / Citas")) }
        BeDictionary.Filters["filter_shipping"]?.let { list.add(it.copy(id = "cat_envio", label = "Envíos / Fletes")) }

        // SECCIÓN: CATEGORÍAS (DINÁMICAS)
        val categoryNamesInEvents = events.mapNotNull { it.categoryName }.distinct()
        categoryNamesInEvents.forEach { catName ->
            val cat = allCats.find { it.name == catName }
            list.add(DropdownItemData("cat_$catName", catName, "RUBROS", cat?.icon ?: "📍", MaverickIcons.Filter))
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sortDropdownItems: StateFlow<List<DropdownItemData>> = flowOf(
        listOfNotNull(
            BeDictionary.Sorts["sort_alpha"],
            BeDictionary.Sorts["sort_date"]
        )
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun toggleFilter(id: String) {
        val currentFilters = appActionCoordinator.activeFilters.value.toMutableSet()

        if (id.startsWith("sort_")) {
            setSortOrder(id)
            return
        }

        if (id == "CLEAR_ALL") {
            currentFilters.clear()
            _activeSortCriteria.value = emptyList()
            appActionCoordinator.selectCategory(null)
            appActionCoordinator.updateSearchQuery("")
        } else {
            if (!currentFilters.remove(id)) {
                currentFilters.add(id)

                // Sincronización con Coordinator si es categoría
                if (id.startsWith("cat_") && id != "cat_visita" && id != "cat_turno" && id != "cat_envio") {
                    val catName = id.removePrefix("cat_")
                    viewModelScope.launch {
                        val category = categoryRepository.allCategories.first().find { it.name == catName }
                        appActionCoordinator.selectCategory(category)
                    }
                }
            } else {
                // Si se quitó un filtro de categoría
                if (id.startsWith("cat_")) {
                    appActionCoordinator.selectCategory(null)
                }
            }
        }

        appActionCoordinator.updateFilters(currentFilters)
    }

    fun setSortOrder(sortId: String?) {
        if (sortId == null) {
            _activeSortCriteria.value = emptyList()
            return
        }

        val current = _activeSortCriteria.value.toMutableList()
        if (current.contains(sortId)) {
            current.remove(sortId)
        } else {
            current.add(sortId)
        }
        _activeSortCriteria.value = current
    }

    fun clearFilters() {
        toggleFilter("CLEAR_ALL")
        _showPastEvents.value = false
    }

    fun cancelEvent(event: CalendarEventEntity) {
        viewModelScope.launch { calendarRepository.cancelEvent(event.id) }
    }

    fun deleteEventPermanently(eventId: String) {
        viewModelScope.launch { calendarRepository.deleteEvent(eventId) }
    }

    fun requestReschedule(event: CalendarEventEntity) {
        // Simulación
    }
}

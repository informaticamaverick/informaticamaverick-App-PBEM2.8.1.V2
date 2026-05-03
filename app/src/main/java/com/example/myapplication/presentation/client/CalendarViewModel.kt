package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.data.repository.AppActionCoordinator
import com.example.myapplication.data.repository.CalendarRepository
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * --- VIEWMODEL DEL CALENDARIO ---
 * Conecta la UI de CalendarScreen con la base de datos Room y centraliza la lógica
 * de filtrado y búsqueda global.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val categoryRepository: com.example.myapplication.data.repository.CategoryRepository,
    appActionCoordinator: AppActionCoordinator,
    // private val chatRepository: ChatRepository // Comentado por política Zero Cost (Firebase)
) : ViewModel() {

    // --- ESTADOS DE FILTRADO LOCAL ---
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    private val _activeFilters = MutableStateFlow(setOf<String>())
    val activeFilters = _activeFilters.asStateFlow()

    // Control para mostrar eventos pasados (vía Pull-to-Refresh)
    private val _showPastEvents = MutableStateFlow(false)
    val showPastEvents = _showPastEvents.asStateFlow()

    /**
     * PRÓXIMO EVENTO (Destaque Google Style)
     */
    val nextEvent: StateFlow<CalendarEventEntity?> = calendarRepository.allEvents
        .map { events ->
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            events.filter { it.status != VisitStatus.CANCELLED }
                .mapNotNull { event ->
                    val time = try { sdf.parse("${event.date} ${event.time}")?.time ?: 0L } catch(e: Exception) { 0L }
                    if (time >= System.currentTimeMillis()) event to time else null
                }
                .sortedBy { it.second }
                .firstOrNull()?.first
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * FLUJO DE EVENTOS FILTRADOS Y PROCESADOS
     */
    val filteredEvents: StateFlow<List<CalendarEventEntity>> = combine(
        calendarRepository.allEvents,
        appActionCoordinator.globalSearchQuery,
        appActionCoordinator.globalSelectedCategory,
        _activeFilters,
        _showPastEvents
    ) { events, searchQuery, globalCategory, filters, showPast ->
        val now = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val todayStr = dateFormat.format(now.time)

        var result = events

        // --- FILTRADO INTELIGENTE POR TIEMPO ---
        if (!showPast) {
            result = result.filter { event ->
                // [MEJORA]: Mostramos TODO el día de HOY, no solo lo que falta.
                // Los eventos pasados de días anteriores se ocultan si !showPast.
                event.date >= todayStr
            }
        }

        // Búsqueda por texto (Global)
        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.provider.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true) ||
                        (it.categoryName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }

        // Filtro por Categoría Global (SSOT)
        globalCategory?.let { cat ->
            result = result.filter { it.categoryName == cat.name }
        }

        // Filtros de Estado
        val showConfirmed = filters.contains("filter_verif")
        val showPending = filters.contains("filter_fast")
        if (showConfirmed && !showPending) result = result.filter { it.status == VisitStatus.CONFIRMED }
        else if (showPending && !showConfirmed) result = result.filter { it.status == VisitStatus.PENDING }

        // Filtros de Tipo (Botones del MoldeBarraMenu)
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

        // Ordenamiento natural por fecha y hora (Soporte Google Style)
        result.sortedWith(compareBy<CalendarEventEntity> { it.date }.thenBy { it.time })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    /**
     * Categorías presentes en los eventos actuales para el menú Tornado.
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
     * Mapa de días con su color predominante de evento (para el calendario)
     */
    val daysWithEventColors: StateFlow<Map<String, Long>> = calendarRepository.allEvents
        .map { events ->
            events.filter { it.status != VisitStatus.CANCELLED }
                .groupBy { it.date }
                .mapValues { (_, eventsInDay) ->
                    // Prioridad de color: Visita > Turno > Envío
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
        val current = _activeFilters.value.toMutableSet()
        if (current.contains(filterId)) current.remove(filterId) else current.add(filterId)
        _activeFilters.value = current
    }

    /**
     * Limpia todos los filtros y resetea la vista a "próximos eventos".
     */
    fun clearFilters() {
        _activeFilters.value = emptySet()
        _showPastEvents.value = false
    }

    /**
     * Cancela un evento (Solo localmente por ahora).
     */
    fun cancelEvent(event: CalendarEventEntity, currentUserId: String) {
        viewModelScope.launch {
            calendarRepository.cancelEvent(event.id)
            // Comentado para evitar Firebase
            // val messageText = "Hola ${event.provider}, me comunico para informarte que he cancelado..."
            // sendAutomatedMessage(currentUserId, event.providerId, messageText)
        }
    }

    /**
     * Inicia el proceso de reprogramación (Solo simulación por ahora).
     */
    fun requestReschedule(event: CalendarEventEntity, currentUserId: String) {
        viewModelScope.launch {
            // Comentado para evitar Firebase
            // val messageText = "Hola ${event.provider}, necesito reprogramar..."
            // sendAutomatedMessage(currentUserId, event.providerId, messageText)
        }
    }

    fun deleteEventPermanently(eventId: String) {
        viewModelScope.launch {
            calendarRepository.deleteEvent(eventId)
        }
    }

    /**
     * Helper para el Chat (Deshabilitado temporalmente)
     */
    /*
    private suspend fun sendAutomatedMessage(senderId: String, receiverId: String, text: String) {
        val chatId = "chat_${senderId}_${receiverId}"
        val message = com.example.myapplication.data.local.MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            type = MessageType.TEXT,
            content = text,
            timestamp = System.currentTimeMillis(),
            status = "SENT"
        )
        chatRepository.sendMessage(message)
    }
    */
}
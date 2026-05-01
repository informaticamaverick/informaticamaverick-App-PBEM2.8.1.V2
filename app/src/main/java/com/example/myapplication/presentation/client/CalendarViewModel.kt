package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.data.repository.AppActionCoordinator
import com.example.myapplication.data.repository.CalendarRepository
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
    appActionCoordinator: AppActionCoordinator,
    // private val chatRepository: ChatRepository // Comentado por política Zero Cost (Firebase)
) : ViewModel() {

    // --- ESTADOS DE FILTRADO LOCAL ---
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate = _selectedDate.asStateFlow()

    private val _activeFilters = MutableStateFlow(setOf<String>())
    val activeFilters = _activeFilters.asStateFlow()

    /**
     * FLUJO DE EVENTOS FILTRADOS Y PROCESADOS
     * Combina:
     * 1. Base de datos (Room)
     * 2. Búsqueda Global (AppActionCoordinator)
     * 3. Fecha seleccionada
     * 4. Filtros tácticos
     */
    val filteredEvents: StateFlow<List<CalendarEventEntity>> = combine(
        calendarRepository.allEvents,
        appActionCoordinator.globalSearchQuery,
        _selectedDate,
        _activeFilters
    ) { events, searchQuery, date, filters ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(date.time)

        var result = events.filter { it.date == selectedDateStr }

        // Búsqueda por texto (Global)
        if (searchQuery.isNotEmpty()) {
            result = result.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.provider.contains(searchQuery, ignoreCase = true) ||
                        it.address.contains(searchQuery, ignoreCase = true)
            }
        }

        // Filtros de Estado
        val showConfirmed = filters.contains("filter_verif")
        val showPending = filters.contains("filter_fast")
        if (showConfirmed && !showPending) result = result.filter { it.status == VisitStatus.CONFIRMED }
        else if (showPending && !showConfirmed) result = result.filter { it.status == VisitStatus.PENDING }

        // Filtros de Tipo
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

        // Ordenamiento
        when {
            filters.contains("sort_precio_desc") -> result.sortedByDescending { it.time }
            filters.contains("sort_nombre_asc") -> result.sortedBy { it.provider }
            else -> result.sortedBy { it.time }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Días que tienen eventos (para marcar en el calendario)
     */
    val daysWithEvents: StateFlow<Set<String>> = calendarRepository.allEvents
        .map { events ->
            events.filter { it.status != VisitStatus.CANCELLED }.map { it.date }.toSet()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- ACCIONES DE UI ---

    fun updateSelectedDate(calendar: Calendar) {
        _selectedDate.value = calendar
    }

    fun toggleFilter(filterId: String) {
        val current = _activeFilters.value.toMutableSet()
        if (current.contains(filterId)) current.remove(filterId) else current.add(filterId)
        _activeFilters.value = current
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
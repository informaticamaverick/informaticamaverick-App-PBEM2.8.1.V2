package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.repository.AvailabilityScheduleFirestoreSync
import com.example.myapplication.prestador.data.repository.AvailabilityScheduleRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val repository: AvailabilityScheduleRepository,
    private val sync: AvailabilityScheduleFirestoreSync,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val providerId: String
        get() = auth.currentUser?.uid ?: ""

    val schedules: StateFlow<List<AvailabilityScheduleEntity>> = repository
        .getActiveSchedulesByProvider(providerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val id = providerId
            if (id.isNotBlank()) {
                sync.pullSchedulesToRoom(id)
            }
        }
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun addSchedule(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        appointmentDuration: Int
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                // Validaciones
                if (dayOfWeek !in 1..7) {
                    _uiState.value = UiState.Error("Día de semana inválido")
                    return@launch
                }

                if (startTime.isBlank() || endTime.isBlank()) {
                    _uiState.value = UiState.Error("Las horas son obligatorias")
                    return@launch
                }

                if (!isValidTime(startTime) || !isValidTime(endTime)) {
                    _uiState.value = UiState.Error("Formato de hora inválido (use HH:mm)")
                    return@launch
                }

                if (!isEndTimeAfterStartTime(startTime, endTime)) {
                    _uiState.value = UiState.Error("La hora de fin debe ser posterior a la hora de inicio")
                    return@launch
                }

                if (appointmentDuration !in listOf(15, 30, 45, 60, 90, 120)) {
                    _uiState.value = UiState.Error("Duración de turno inválida")
                    return@launch
                }

                val schedule = AvailabilityScheduleEntity(
                    id = UUID.randomUUID().toString(),
                    providerId = providerId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    appointmentDuration = appointmentDuration,
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                repository.saveSchedule(schedule)

                val syncResult = sync.upsertSchedule(schedule)
                if (syncResult.isFailure) {
                    _uiState.value = UiState.Error(
                        "Horario guardado localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                    )
                    return@launch
                }

                _uiState.value = UiState.Success("Horario agregado correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar horario")
            }
        }
    }

    fun updateSchedule(schedule: AvailabilityScheduleEntity) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                if (!isValidTime(schedule.startTime) || !isValidTime(schedule.endTime)) {
                    _uiState.value = UiState.Error("Formato de hora inválido")
                    return@launch
                }

                if (!isEndTimeAfterStartTime(schedule.startTime, schedule.endTime)) {
                    _uiState.value = UiState.Error("La hora de fin debe ser posterior a la hora de inicio")
                    return@launch
                }

                val updated = schedule.copy(updatedAt = System.currentTimeMillis())
                repository.updateSchedule(updated)

                val syncResult = sync.upsertSchedule(updated)
                if (syncResult.isFailure) {
                    _uiState.value = UiState.Error(
                        "Horario actualizado localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                    )
                    return@launch
                }

                _uiState.value = UiState.Success("Horario actualizado correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar horario")
            }
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                repository.deleteScheduleById(scheduleId)

                val syncResult = sync.deleteScheduleById(scheduleId)
                if (syncResult.isFailure) {
                    _uiState.value = UiState.Error(
                        "Horario eliminado localmente, pero falló la sincronización: ${syncResult.exceptionOrNull()?.message ?: "Error"}"
                    )
                    return@launch
                }

                _uiState.value = UiState.Success("Horario eliminado correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar horario")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }

    // Helpers de validación
    private fun isValidTime(time: String): Boolean {
        val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
        return regex.matches(time)
    }

    private fun isEndTimeAfterStartTime(startTime: String, endTime: String): Boolean {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

        return endMinutes > startMinutes
    }
}

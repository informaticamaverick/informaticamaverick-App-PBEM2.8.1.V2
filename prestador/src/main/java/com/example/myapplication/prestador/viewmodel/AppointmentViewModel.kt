package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.data.repository.ValidationResult
import com.example.myapplication.prestador.data.repository.AvailabilityScheduleFirestoreSync

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository,
    private val availabilityRepository: com.example.myapplication.prestador.data.repository.AppointmentAvailabilityRepository,
    private val scheduleSync: AvailabilityScheduleFirestoreSync
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _appointment = MutableStateFlow<AppointmentEntity?>(null)
    val appointment: StateFlow<AppointmentEntity?> = _appointment.asStateFlow()

    private val _appointments = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val appointments: StateFlow<List<AppointmentEntity>> = _appointments.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        // Cargar todas las citas al iniciar
        loadAllAppointments()
        viewModelScope.launch { repository.syncAppointmentsFromMessages() }
    }
    
    private fun loadAllAppointments() {
        viewModelScope.launch {
            try {
                repository.getAllAppointments().collect { appointments ->
                    _appointments.value = appointments
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar citas: ${e.message}"
            }
        }
    }

    fun loadAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAppointmentById(appointmentId).collect { appointment ->
                    _appointment.value = appointment
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAppointmentsByProvider(providerId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAppointmentsByProvider(providerId).collect { appointments ->
                    _appointments.value = appointments
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar citas: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun loadAppointmentsByStatus(providerId: String, status: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAppointmentsByStatus(providerId, status).collect { appointments ->
                    _appointments.value = appointments
                    _isLoading.value = false // Desactivar loading después de recibir datos
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar citas: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveAppointment(appointment)
                _successMessage.value = "✅ Cita creada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateAppointment(appointment)
                _successMessage.value = "✅ Cita reprogramada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateAppointmentStatus(appointmentId, status)
                _successMessage.value = when (status.uppercase()) {
                    "CONFIRMED" -> "✅ Cita confirmada"
                    "CANCELLED" -> "❌ Cita cancelada"
                    "COMPLETED" -> "✔️ Cita completada"
                    else -> "Estado actualizado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar estado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteAppointment(appointmentId)
                _successMessage.value = "Cita eliminada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun cancelAppointment(appointmentId: String) {
        updateAppointmentStatus(appointmentId, "cancelled")
    }

    fun confirmAppointment(appointmentId: String) {
        updateAppointmentStatus(appointmentId, "confirmed")
    }

    fun completeAppointment(appointmentId: String) {
        updateAppointmentStatus(appointmentId, "completed")
    }

    fun syncPendingWithFirestore(providerId: String) {
        if (providerId.isBlank()) return
        viewModelScope.launch {
            repository.syncPendingWithFirestore(providerId)
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    private val _availabilitySlots = MutableStateFlow<List<String>>(emptyList())
    val availabilitySlots: StateFlow<List<String>> = _availabilitySlots.asStateFlow()

    private val _availabilityLoading = MutableStateFlow(false)
    val availabilityLoading: StateFlow<Boolean> = _availabilityLoading.asStateFlow()

    private val pulledProviders = mutableSetOf<String>()

    fun loadAvailabilitySlots(providerId: String, date: String, duration: Int = 60) {
        viewModelScope.launch {
            _availabilityLoading.value = true
            try {
                if (providerId.isNotBlank() && pulledProviders.add(providerId)) {
                    val pullResult = scheduleSync.pullSchedulesToRoom(providerId)
                    if (pullResult.isSuccess) {
                        println("🟪 ScheduleSync: pulled ${pullResult.getOrDefault(emptyList()).size} schedules for providerId=$providerId")
                    } else {
                        println("🟥 ScheduleSync: failed for providerId=$providerId -> ${pullResult.exceptionOrNull()?.message}")
                    }
                }

                val slots = availabilityRepository.getAvailableSlots(
                    providerId = providerId,
                    date = date,
                    duration = duration
                )
                println("🟦 AvailabilitySlots: providerId=$providerId date=$date duration=$duration -> ${slots.size} slots")
                _availabilitySlots.value = slots
            } finally {
                _availabilityLoading.value = false
            }
        }
    }

    fun validateAndSave(
        appointment: AppointmentEntity,
        serviceType: ServiceType,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (serviceType == ServiceType.PROFESSIONAL && appointment.providerId.isNotBlank() && pulledProviders.add(appointment.providerId)) {
                val pullResult = scheduleSync.pullSchedulesToRoom(appointment.providerId)
                if (pullResult.isSuccess) {
                    println("🟪 ScheduleSync: pulled ${pullResult.getOrDefault(emptyList()).size} schedules for providerId=${appointment.providerId}")
                } else {
                    println("🟥 ScheduleSync: failed for providerId=${appointment.providerId} -> ${pullResult.exceptionOrNull()?.message}")
                }
            }

            val result = availabilityRepository.isTimeSlotAvailable(
                providerId = appointment.providerId,
                serviceType = serviceType,
                date = appointment.date,
                time = appointment.time,
                duration = appointment.duration,
                rentalSpaceId = appointment.rentalSpaceId,
                scheduleId = appointment.scheduleId
            )
            when (result) {
                is ValidationResult.Available -> {
                    saveAppointment(appointment)
                    onSuccess()
                }
                is ValidationResult.Error -> onError(result.message)
            }
        }
    }
}

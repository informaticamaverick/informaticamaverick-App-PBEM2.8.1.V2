package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.AppointmentDao
import com.example.myapplication.prestador.data.local.dao.AvailabilityScheduleDao
import com.example.myapplication.prestador.data.local.dao.RentalSpaceDao
import com.example.myapplication.prestador.data.model.ServiceType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para validar disponibilidad de appointments según tipo de servicio
 */
@Singleton
class AppointmentAvailabilityRepository @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val availabilityScheduleDao: AvailabilityScheduleDao,
    private val rentalSpaceDao: RentalSpaceDao
) {
    
    /**
     * Valida si una fecha/hora está disponible según el tipo de servicio
     */
    suspend fun isTimeSlotAvailable(
        providerId: String,
        serviceType: ServiceType,
        date: String, // "YYYY-MM-DD"
        time: String, // "HH:mm"
        duration: Int,
        rentalSpaceId: String? = null,
        scheduleId: String? = null
    ): ValidationResult {
        
        return when (serviceType) {
            ServiceType.TECHNICAL, ServiceType.OTHER -> {
                // Servicios técnicos: siempre disponible si el prestador está activo
                // No hay restricciones de horario
                ValidationResult.Available
            }
            
            ServiceType.PROFESSIONAL -> {
                // Profesionales: validar contra availability_schedules
                validateProfessionalAvailability(providerId, date, time, duration, scheduleId)
            }
            
            ServiceType.RENTAL -> {
                // Alquiler: validar que el espacio esté libre en ese horario
                if (rentalSpaceId == null) {
                    return ValidationResult.Error("Debe seleccionar un espacio")
                }
                validateRentalAvailability(providerId, rentalSpaceId, date, time, duration)
            }
        }
    }
    
    /**
     * Valida disponibilidad para profesionales con agenda
     */
    private suspend fun validateProfessionalAvailability(
        providerId: String,
        date: String,
        time: String,
        duration: Int,
        scheduleId: String?
    ): ValidationResult {
        try {
            // Convertir fecha a día de la semana (1=Lunes, 7=Domingo)
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            val dayOfWeek = localDate.dayOfWeek.value
            
            // Obtener horarios de ese día
            val schedules = availabilityScheduleDao.getByProviderIdAndDaySuspend(providerId, dayOfWeek)
            
            if (schedules.isEmpty()) {
                return ValidationResult.Error("No hay horarios configurados para este día")
            }
            
            // Convertir hora a minutos para comparar
            val requestedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val requestedMinutes = requestedTime.hour * 60 + requestedTime.minute
            val endMinutes = requestedMinutes + duration
            
            // Validar que el horario solicitado esté dentro de algún schedule
            val isWithinSchedule = schedules.any { schedule ->
                if (!schedule.isActive) return@any false
                
                val scheduleStart = timeToMinutes(schedule.startTime)
                val scheduleEnd = timeToMinutes(schedule.endTime)
                
                requestedMinutes >= scheduleStart && endMinutes <= scheduleEnd
            }
            
            if (!isWithinSchedule) {
                return ValidationResult.Error("El horario está fuera de la disponibilidad configurada")
            }
            
            // Verificar que no haya otra cita en ese horario
            val existingAppointments = appointmentDao.getByProviderAndDateSuspend(providerId, date)
            val hasConflict = existingAppointments.any { appointment ->
                if (
                    appointment.status.equals("cancelado", ignoreCase = true) ||
                    appointment.status.equals("cancelled", ignoreCase = true)
                ) return@any false
                
                val appointmentStart = timeToMinutes(appointment.time)
                val appointmentEnd = appointmentStart + appointment.duration
                
                // Hay conflicto si los rangos se solapan
                !(endMinutes <= appointmentStart || requestedMinutes >= appointmentEnd)
            }
            
            if (hasConflict) {
                return ValidationResult.Error("Ya hay una cita programada en ese horario")
            }
            
            return ValidationResult.Available
            
        } catch (e: Exception) {
            return ValidationResult.Error("Error al validar disponibilidad: ${e.message}")
        }
    }
    
    /**
     * Valida disponibilidad para espacios de alquiler
     */
    private suspend fun validateRentalAvailability(
        providerId: String,
        rentalSpaceId: String,
        date: String,
        time: String,
        duration: Int
    ): ValidationResult {
        try {
            // Verificar que el espacio existe y está activo
            val space = rentalSpaceDao.getById(rentalSpaceId)
            if (space == null) {
                return ValidationResult.Error("El espacio no existe")
            }
            if (!space.isActive) {
                return ValidationResult.Error("El espacio no está disponible")
            }
            
            // Verificar que no haya otra reserva en ese espacio y horario
            val existingAppointments = appointmentDao.getByProviderAndDateSuspend(providerId, date)
            val hasConflict = existingAppointments.any { appointment ->
                if (
                    appointment.status.equals("cancelado", ignoreCase = true) ||
                    appointment.status.equals("cancelled", ignoreCase = true)
                ) return@any false
                if (appointment.rentalSpaceId != rentalSpaceId) return@any false
                
                val requestedMinutes = timeToMinutes(time)
                val endMinutes = requestedMinutes + duration
                val appointmentStart = timeToMinutes(appointment.time)
                val appointmentEnd = appointmentStart + appointment.duration
                
                // Hay conflicto si los rangos se solapan
                !(endMinutes <= appointmentStart || requestedMinutes >= appointmentEnd)
            }
            
            if (hasConflict) {
                return ValidationResult.Error("El espacio ya está reservado en ese horario")
            }
            
            return ValidationResult.Available
            
        } catch (e: Exception) {
            return ValidationResult.Error("Error al validar disponibilidad: ${e.message}")
        }
    }
    
    /**
     * Convierte tiempo "HH:mm" a minutos desde medianoche
     */
    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
    
    /**
     * Obtiene slots disponibles para un día específico (profesionales)
     */
    suspend fun getAvailableSlots(
        providerId: String,
        date: String,
        duration: Int
    ): List<String> {
        try {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            val dayOfWeek = localDate.dayOfWeek.value
            
            val schedules = availabilityScheduleDao.getByProviderIdAndDaySuspend(providerId, dayOfWeek)
            val existingAppointments = appointmentDao.getByProviderAndDateSuspend(providerId, date)
            
            val availableSlots = mutableListOf<String>()
            
            schedules.filter { it.isActive }.forEach { schedule ->
                val startMinutes = timeToMinutes(schedule.startTime)
                val endMinutes = timeToMinutes(schedule.endTime)
                
                var currentMinutes = startMinutes
                while (currentMinutes + duration <= endMinutes) {
                    val slotTime = minutesToTime(currentMinutes)
                    
                    // Verificar si hay conflicto con citas existentes
                    val hasConflict = existingAppointments.any { appointment ->
                        if (
                            appointment.status.equals("cancelado", ignoreCase = true) ||
                            appointment.status.equals("cancelled", ignoreCase = true)
                        ) return@any false
                        
                        val appointmentStart = timeToMinutes(appointment.time)
                        val appointmentEnd = appointmentStart + appointment.duration
                        val slotEnd = currentMinutes + duration
                        
                        !(slotEnd <= appointmentStart || currentMinutes >= appointmentEnd)
                    }
                    
                    if (!hasConflict) {
                        availableSlots.add(slotTime)
                    }
                    
                    // Avanzar según la duración del appointment del schedule
                    currentMinutes += schedule.appointmentDuration
                }
            }
            
            return availableSlots
            
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    /**
     * Convierte minutos desde medianoche a formato "HH:mm"
     */
    private fun minutesToTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }
}

/**
 * Resultado de validación de disponibilidad
 */
sealed class ValidationResult {
    object Available : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

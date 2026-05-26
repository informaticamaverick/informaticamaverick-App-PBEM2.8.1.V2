package com.example.myapplication.core.data.repository

import android.util.Log
import com.example.myapplication.core.data.local.dao.CalendarDao
import com.example.myapplication.core.data.remote.AppointmentDataMapper
import com.example.myapplication.core.domain.model.Appointment
import com.example.myapplication.core.domain.model.AppointmentStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CITAS (COMPARTIDO) ---
 * Gestiona el agendamiento y coordinación de turnos técnicos.
 * Este repositorio es clave para la sincronización de las agendas entre
 * el Cliente y el Prestador.
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val calendarDao: CalendarDao
) {
    private val appointmentsCollection = firestore.collection("appointments")

    /**
     * Sincroniza las citas de un usuario desde Firestore a Room.
     */
    suspend fun syncAppointments(userId: String, isProvider: Boolean) {
        try {
            val field = if (isProvider) "providerId" else "clientId"
            val snapshot = appointmentsCollection.whereEqualTo(field, userId).get().await()
            
            val events = snapshot.documents.mapNotNull { AppointmentDataMapper.fromFirestore(it) }
            events.forEach { calendarDao.insertEvent(it) }
            Log.d("AppointmentRepository", "Sincronizados ${events.size} eventos para el usuario $userId")
        } catch (e: Exception) {
            Log.e("AppointmentRepository", "Error sincronizando citas: ${e.message}")
        }
    }

    /**
     * Crea una nueva solicitud de cita en Firebase.
     */
    suspend fun createAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = appointmentsCollection.document()
            val appointmentWithId = appointment.copy(id = docRef.id)
            docRef.set(appointmentWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de una cita existente (Aceptar/Rechazar).
     */
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("status", status.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentDateTime(appointmentId: String, date: Long, time: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("requestDate", date, "requestTime", time)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Propone una nueva fecha y hora para una cita existente.
     */
    suspend fun rescheduleAppointment(appointmentId: String, date: Long, time: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update(
                    "requestDate", date,
                    "requestTime", time,
                    "status", AppointmentStatus.RESCHEDULED.name
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

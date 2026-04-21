package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Appointment
import com.example.myapplication.data.model.AppointmentStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val appointmentsCollection = firestore.collection("appointments")

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
                .update(
                    "requestDate", date,
                    "requestTime", time
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

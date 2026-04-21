package com.example.myapplication.prestador.data.repository

import android.R
import com.example.myapplication.prestador.data.local.dao.AppointmentDao
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.Appointment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.annotation.meta.When
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio híbrido para gestionar citas en Firebase y Room
 */
@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val messageDao: com.example.myapplication.prestador.data.local.dao.MessageDao
) {
    private val db = FirebaseFirestore.getInstance()
    private val appointmentsCollection = db.collection("appointments")
    
    // ============ FIREBASE METHODS ============
    
    /**
     * Guarda una nueva cita en Firebase
     */
    suspend fun saveAppointmentToFirebase(appointment: Appointment): Result<String> {
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
     * Obtiene todas las citas del prestador desde Firebase
     */
    suspend fun getAppointmentsFromFirebase(): Result<List<Appointment>> {
        return try {
            val snapshot = appointmentsCollection
                .orderBy("date")
                .orderBy("time")
                .get()
                .await()
            
            val appointments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)
            }
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza el estado de una cita en Firebase
     */
    suspend fun updateAppointmentStatusInFirebase(appointmentId: String, status: String): Result<Unit> {
        return try {
            appointmentsCollection.document(appointmentId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============ ROOM METHODS ============
    
    suspend fun saveAppointment(appointment: AppointmentEntity) {
        println("📍 AppointmentRepository.saveAppointment() - Inicio")
        println("📍 Appointment: ${appointment.id}, ${appointment.clientName}, ${appointment.date}, ${appointment.time}")
        try {
            appointmentDao.insertAppointment(appointment)
            println("📍 AppointmentRepository.saveAppointment() - ✅ Guardado en DAO")
        } catch (e: Exception) {
            println("📍 AppointmentRepository.saveAppointment() - ❌ Error: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateAppointment(appointment: AppointmentEntity) {
        appointmentDao.updateAppontment(appointment)
    }
    
    suspend fun deleteAppointment(appointmentId: String) {
        appointmentDao.deleteAppointmentById(appointmentId)
    }
    
    fun getAppointmentById(appointmentId: String): Flow<AppointmentEntity?> {
        return appointmentDao.getAppointmentByIdFlow(appointmentId)
    }
    
    suspend fun getAppointmentByIdSync(appointmentId: String): AppointmentEntity? {
        return appointmentDao.getAppointmentById(appointmentId)
    }

    suspend fun findAppointmentBySlot(
        providerId: String,
        clientId: String,
        date: String,
        time: String
    ): AppointmentEntity? {
        return appointmentDao.findAppointmentBySlot(providerId, clientId, date, time)
    }
    
    fun getAppointmentsByProvider(providerId: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAllAppointments(providerId)
    }
    
    fun getAppointmentsByStatus(providerId: String, status: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsByStatus(providerId, status)
    }
    
    fun getAllAppointments(): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAllAppointmentsFlow()
    }

    fun getAppointmentsByServiceType(serviceType: String): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAllintmentsByServiceType(serviceType)
    }
    
    suspend fun updateAppointmentStatus(appointmentId: String, status: String) {
        appointmentDao.updateAppointmentStatus(appointmentId, status, System.currentTimeMillis())
    }

    suspend fun syncAppointmentsFromMessages() {
        try {
            val messages = messageDao.getAcceptedAppointmentMessages()
            for (msg in messages) {
                val rawId = msg.appointmentId ?: msg.messageId
                val existing = appointmentDao.getAppointmentById(rawId) ?: continue
                val newStatus = if (msg.appointmentStatus == "ACCEPTED") "confirmed" else "cancelled"
                if (existing.status != newStatus)
                {
                    appointmentDao.updateAppointmentStatus(rawId, newStatus, System.currentTimeMillis())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AppointmenteRepository", "Error sync: ${e.message}")
        }
    }


    suspend fun syncPendingWithFirestore(providerId: String) {
        try {
            val pending = appointmentDao.getPendingAppointmentsSync(providerId)
            android.util.Log.d("SyncDebug", "Citas pendientes: ${pending.size}")
            for (appt in pending) {
                try {
                    val snap = db.collectionGroup("messages")
                        .whereEqualTo("appointmentId", appt.id)
                        .limit(1)
                        .get()
                        .await()
                    if (!snap.isEmpty) {
                        val fsStatus = snap.documents[0].getString("appointmentStatus")
                        android.util.Log.d("SyncDebug", "Cita ${appt.id} -> Firestore status: $fsStatus")
                        when (fsStatus) {
                            "ACCEPTED" -> appointmentDao.updateAppointmentStatus(appt.id, "confirmed",
                                System.currentTimeMillis())
                            "REJECTED" -> appointmentDao.updateAppointmentStatus(appt.id, "cancelled",
                                System.currentTimeMillis())
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("SyncDebug", "Skip ${appt.id}: ${e.message}")
                }
            }

            // Limpieza: si hay múltiples "confirmed" para el mismo cliente+fecha, quedarse con el más reciente
            val allAppts = appointmentDao.getAllAppointmentsSync(providerId)
            val grouped = allAppts.groupBy { "${it.clientId}|${it.date}" }
            for ((_, group) in grouped) {
                val confirmed = group.filter { it.status == "confirmed" }
                if (confirmed.size <= 1) continue
                val sorted = confirmed.sortedByDescending { it.updatedAt }
                for (old in sorted.drop(1)) {
                    appointmentDao.updateAppointmentStatus(old.id, "cancelled", System.currentTimeMillis())
                    android.util.Log.d("SyncDebug", "Cancelada cita duplicada: ${old.id} ${old.time}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncDebug", "Error: ${e.message}")
        }
    }

}
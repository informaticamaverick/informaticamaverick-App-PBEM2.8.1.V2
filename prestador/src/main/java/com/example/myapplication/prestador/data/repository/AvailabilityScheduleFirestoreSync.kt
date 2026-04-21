package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityScheduleFirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: AvailabilityScheduleRepository
) {
    private val coleccion = firestore.collection("availability_schedules")

    suspend fun upsertSchedule(schedule: AvailabilityScheduleEntity): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to schedule.id,
                "providerId" to schedule.providerId,
                "dayOfWeek" to schedule.dayOfWeek,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "appointmentDuration" to schedule.appointmentDuration,
                "isActive" to schedule.isActive,
                "createdAt" to schedule.createdAt,
                "updatedAt" to schedule.updatedAt
            )
            coleccion.document(schedule.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteScheduleById(scheduleId: String): Result<Unit> {
        return try {
            coleccion.document(scheduleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullSchedulesToRoom(providerId: String): Result<List<AvailabilityScheduleEntity>> {
        return try {
            if (providerId.isBlank()) return Result.success(emptyList())

            val snapshot = coleccion
                .whereEqualTo("providerId", providerId)
                .get()
                .await()

            val schedules = snapshot.documents.mapNotNull { doc -> doc.toEntity(providerId) }

            if (schedules.isNotEmpty()) {
                repository.saveSchedules(schedules)
            }

            Result.success(schedules)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toEntity(providerIdFallback: String): AvailabilityScheduleEntity? {
        val id = getString("id") ?: this.id
        val providerId = getString("providerId") ?: providerIdFallback
        val dayOfWeek = (getLong("dayOfWeek") ?: getDouble("dayOfWeek")?.toLong())?.toInt() ?: return null
        val startTime = getString("startTime") ?: return null
        val endTime = getString("endTime") ?: return null
        val appointmentDuration = (getLong("appointmentDuration") ?: getDouble("appointmentDuration")?.toLong())?.toInt() ?: 30
        val isActive = getBoolean("isActive") ?: true
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        val updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()

        return AvailabilityScheduleEntity(
            id = id,
            providerId = providerId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            appointmentDuration = appointmentDuration,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
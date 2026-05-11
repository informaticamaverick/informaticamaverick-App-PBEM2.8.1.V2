package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.BlockedDateEntity
import com.example.myapplication.prestador.data.local.entity.BlockedDateReason
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedDateFirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val repository: BlockedDateRepository
) {
    private val collection = firestore.collection("blocked_dates")

    suspend fun upsert(entity: BlockedDateEntity): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to entity.id,
                "providerId" to entity.providerId,
                "date" to entity.date,
                "label" to entity.label,
                "reason" to entity.reason,
                "isActive" to entity.isActive,
                "createdAt" to entity.createdAt
            )
            collection.document(entity.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteById(id: String): Result<Unit> {
        return try {
            collection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullToRoom(providerId: String): Result<List<BlockedDateEntity>> {
        return try {
            if (providerId.isBlank()) return Result.success(emptyList())

            val snapshot = collection
                .whereEqualTo("providerId", providerId)
                .get()
                .await()

            val entities = snapshot.documents.mapNotNull { it.toEntity(providerId) }
            if (entities.isNotEmpty()) {
                repository.insertAll(entities)
            }

            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun DocumentSnapshot.toEntity(providerIdFallback: String): BlockedDateEntity? {
        val id = getString("id") ?: this.id
        val providerId = getString("providerId") ?: providerIdFallback
        val date = getString("date") ?: return null
        val label = getString("label") ?: ""
        val reason = getString("reason") ?: BlockedDateReason.CUSTOM.name
        val isActive = getBoolean("isActive") ?: true
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()

        return BlockedDateEntity(
            id = id,
            providerId = providerId,
            date = date,
            label = label,
            reason = reason,
            isActive = isActive,
            createdAt = createdAt
        )
    }
}

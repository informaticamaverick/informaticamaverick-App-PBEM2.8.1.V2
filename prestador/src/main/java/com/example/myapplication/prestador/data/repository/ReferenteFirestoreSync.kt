package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferenteFirestoreSync @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val referenteRepository: ReferenteRepository
) {
    private val coleccion = firestore.collection("referentes")

    suspend fun subirReferente(referente: ReferenteEntity): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to referente.id,
                "providerId" to referente.providerId,
                "nombre" to referente.nombre,
                "apellido" to referente.apellido,
                "cargo" to referente.cargo,
                "imageUrl" to referente.imageUrl,
                "empresaId" to referente.empresaId,
                "sucursalId" to referente.sucursalId,
                "activo" to referente.activo,
                "createdAt" to referente.createdAt,
                "updatedAt" to referente.updatedAt
            )
            coleccion.document(referente.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bajarReferentesPorProvider(providerId: String): Result<List<ReferenteEntity>> {
        return try {
            val snapshot = coleccion
                .whereEqualTo("providerId", providerId)
                .whereEqualTo("activo", true)
                .get()
                .await()

            val referentes = snapshot.documents.mapNotNull { doc ->
                try {
                    ReferenteEntity(
                        id = doc.getString("id") ?: doc.id,
                        providerId = doc.getString("providerId") ?: providerId,
                        nombre = doc.getString("nombre") ?: return@mapNotNull null,
                        apellido = doc.getString("apellido"),
                        cargo = doc.getString("cargo"),
                        imageUrl = doc.getString("imageUrl"),
                        empresaId = doc.getString("empresaId"),
                        sucursalId = doc.getString("sucursalId"),
                        activo = doc.getBoolean("activo") ?: true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            referentes.forEach {
                referenteRepository.addReferente(
                    providerId = it.providerId,
                    nombre = it.nombre,
                    apellido = it.apellido,
                    cargo = it.cargo,
                    imageUrl = it.imageUrl,
                    empresaId = it.empresaId,
                    sucursalId = it.sucursalId
                )
            }

            Result.success(referentes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizar(providerId: String): List<ReferenteEntity> {
        val locales = referenteRepository.getReferentesByProvider(providerId)
        if (locales.isNotEmpty()) return locales
        return bajarReferentesPorProvider(providerId).getOrDefault(emptyList())
    }

    suspend fun subirTodos(providerId: String): Result<Unit> {
        return try {
            val referentes = referenteRepository.getReferentesByProvider(providerId)
            referentes.forEach { subirReferente(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
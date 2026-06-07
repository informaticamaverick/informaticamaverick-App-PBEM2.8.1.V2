/**
package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class SolicitudDoc(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val clienteId: String,
    val clienteNombre: String,
    val lat: Double,
    val lng: Double,
    val urgente: Boolean,
    val estado: String,
    val categoria: String,
    val creadoEn: Long
)

@Singleton
class OportunidadesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /** Escucha en tiempo real las solicitudes pendientes de solicitudes_fast */
    fun escucharSolicitudesPendientes(): Flow<List<SolicitudDoc>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("solicitudes_fast")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val docs = snapshot.documents.mapNotNull { doc ->
                    try {
                        SolicitudDoc(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            clienteId = doc.getString("clienteId") ?: "",
                            clienteNombre = doc.getString("clienteNombre") ?: "Cliente",
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0,
                            urgente = doc.getBoolean("urgente") ?: false,
                            estado = "pendiente",
                            categoria = doc.getString("categoria") ?: "",
                            creadoEn = doc.getLong("creadoEn") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(docs)
            }
        awaitClose { listener.remove() }
    }

    /** Obtiene si el prestador trabaja 24hs desde Firestore */
    suspend fun getProviderIs24Hours(prestadorId: String): Boolean {
        return try {
            val doc = firestore.collection("providers").document(prestadorId).get().await()
            doc.getBoolean("is24Hours") ?: false
        } catch (e: Exception) { false }
    }

    /** Verifica si el prestador ya tiene una solicitud fast aceptada en curso */
    suspend fun tieneSolicitudActiva(prestadorId: String): Boolean {
        return try {
            val snapshot = firestore.collection("solicitudes_fast")
                .whereEqualTo("estado", "aceptada")
                .whereEqualTo("prestadorId", prestadorId)
                .get().await()
            !snapshot.isEmpty
        } catch (e: Exception) { false }
    }

    suspend fun aceptarSolicitud(solicitudId: String, prestadorId: String) {
        firestore.collection("solicitudes_fast").document(solicitudId)
            .update(mapOf("estado" to "aceptada", "prestadorId" to prestadorId))
            .await()
    }

    suspend fun completarSolicitud(solicitudId: String) {
        firestore.collection("solicitudes_fast").document(solicitudId)
            .update("estado", "completada")
            .await()
    }

    suspend fun expirarSolicitud(solicitudId: String) {
        try {
            firestore.collection("solicitudes_fast").document(solicitudId)
                .update("estado", "expirada")
                .await()
        } catch (_: Exception) { }
    }

    suspend fun crearSolicitud(
        titulo: String,
        clienteNombre: String,
        clienteId: String,
        lat: Double,
        lng: Double,
        urgente: Boolean,
        categoria: String = ""
    ) {
        firestore.collection("solicitudes_fast").add(
            mapOf(
                "titulo" to titulo,
                "descripcion" to titulo,
                "clienteNombre" to clienteNombre,
                "clienteId" to clienteId,
                "lat" to lat,
                "lng" to lng,
                "urgente" to urgente,
                "estado" to "pendiente",
                "categoria" to categoria,
                "creadoEn" to System.currentTimeMillis()
            )
        ).await()
    }
}
*/
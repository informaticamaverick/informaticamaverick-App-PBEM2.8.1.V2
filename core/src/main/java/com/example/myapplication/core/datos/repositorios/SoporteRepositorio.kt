package com.example.myapplication.core.datos.repositorios

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Autor de un mensaje del lado del usuario (no admin) en el hilo de un ticket. */
const val AUTOR_USUARIO = "usuario"

data class MensajeSoporte(
    val de: String = "",
    val texto: String = "",
    val fecha: Long = 0L,
    val por: String? = null
)

data class TicketSoporte(
    val id: String = "",
    val categoria: String = "",
    val asunto: String = "",
    val mensaje: String = "",
    val estado: String = "abierto",
    val createdAt: Long = 0L,
    val imagen: String? = null,
    val mensajes: List<MensajeSoporte> = emptyList()
)

/**
 * --- BUZÓN DE SOPORTE (v2026) ---
 * Escribe tickets a la colección `soporte` de Firestore, con el mismo esquema
 * que ya lee y responde el panel admin (`HTML Admin/js/soporte.js`): campos
 * planos en el documento (no anidados) y `createdAt` como epoch millis, no
 * server timestamp, porque así es como el JS del admin ordena/muestra los tickets.
 */
@Singleton
class SoporteRepositorio @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Tickets propios en vivo, con las respuestas del admin (`mensajes`) y el
     * `estado` actualizados en tiempo real. Orden por fecha se hace en el
     * cliente (más nuevo primero) para no depender de un índice compuesto
     * `uid == / orderBy(createdAt)` en Firestore.
     */
    fun observarMisTickets(uid: String): Flow<List<TicketSoporte>> = callbackFlow {
        val listener = firestore.collection("soporte")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val tickets = snapshot.documents.map { doc ->
                    val mensajesRaw = doc.get("mensajes") as? List<*> ?: emptyList<Any>()
                    val mensajes = mensajesRaw.mapNotNull { item ->
                        val mapa = item as? Map<*, *> ?: return@mapNotNull null
                        MensajeSoporte(
                            de = mapa["de"] as? String ?: "",
                            texto = mapa["texto"] as? String ?: "",
                            fecha = (mapa["fecha"] as? Number)?.toLong() ?: 0L,
                            por = mapa["por"] as? String
                        )
                    }
                    TicketSoporte(
                        id = doc.id,
                        categoria = doc.getString("categoria") ?: "",
                        asunto = doc.getString("asunto") ?: "",
                        mensaje = doc.getString("mensaje") ?: "",
                        estado = doc.getString("estado") ?: "abierto",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        imagen = doc.getString("imagen"),
                        mensajes = mensajes
                    )
                }.sortedByDescending { it.createdAt }
                trySend(tickets)
            }
        awaitClose { listener.remove() }
    }

    suspend fun crearTicket(
        uid: String,
        nombre: String,
        email: String,
        rol: String,
        categoria: String,
        asunto: String,
        mensaje: String,
        imagenBase64: String? = null
    ): Result<Unit> = try {
        val datos = mutableMapOf<String, Any?>(
            "uid" to uid,
            "nombre" to nombre,
            "email" to email,
            "rol" to rol,
            "categoria" to categoria,
            "asunto" to asunto,
            "mensaje" to mensaje,
            "estado" to "abierto",
            "createdAt" to System.currentTimeMillis(),
            "mensajes" to emptyList<Map<String, Any>>()
        )
        if (imagenBase64 != null) datos["imagen"] = imagenBase64

        firestore.collection("soporte").add(datos).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Agrega una respuesta del usuario al hilo (`mensajes`) de un ticket propio.
     * Mismo mecanismo (`arrayUnion`) que ya usa el panel admin para responder.
     * Las reglas de Firestore ya lo permiten: el dueño del ticket puede actualizar
     * el documento mientras no toque `estado` ni `uid` — no requiere cambios ahí.
     * El llamador debe validar antes que `estado != "resuelto"` (caso cerrado).
     */
    suspend fun agregarMensaje(ticketId: String, texto: String, por: String?): Result<Unit> = try {
        firestore.collection("soporte").document(ticketId).update(
            mapOf(
                "mensajes" to FieldValue.arrayUnion(
                    mapOf(
                        "de" to AUTOR_USUARIO,
                        "texto" to texto,
                        "fecha" to System.currentTimeMillis(),
                        "por" to por
                    )
                ),
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

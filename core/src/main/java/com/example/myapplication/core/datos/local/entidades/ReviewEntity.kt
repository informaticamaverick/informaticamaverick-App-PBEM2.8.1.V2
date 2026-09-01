package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE PERSISTENCIA: PROVIDER REVIEW (ROOM) ---
 * [ELITE v15.0]: Almacena reseñas de prestadores localmente.
 * Sigue la Ley #2: Todo dato de la nube debe impactar en Room.
 */
@Entity(
    tableName = "reviews",
    indices = [
        Index(value = ["targetId"]),
        Index(value = ["reviewerId"])
    ]
)
data class ReviewEntity(
    @PrimaryKey
    val id: String,
    val targetId: String,   // ID del Prestador o Cliente reseñado
    val targetType: String, // PRESTADOR o CLIENTE (Ley de Confianza Mutua)
    val reviewerId: String, // ID del autor (UID Firebase)
    val reviewerName: String,
    val reviewerPhotoUrl: String? = null,
    val rating: Float,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val response: String? = null, // Respuesta del reseñado
    val jobId: String? = null     // Vínculo con trabajo confirmado (Ley #4)
)


































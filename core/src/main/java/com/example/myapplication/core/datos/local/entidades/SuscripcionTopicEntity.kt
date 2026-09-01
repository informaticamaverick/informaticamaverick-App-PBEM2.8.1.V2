package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE SUSCRIPCIÓN A TÓPICOS (HIGIENE DE RED v2026.ELITE) ---
 * [PROPÓSITO]: Persistir los tópicos de FCM a los que la app está suscrita.
 * [LEY #9]: Estándar Mav en Español.
 */
@Entity(tableName = "suscripciones_topic")
data class SuscripcionTopicEntity(
    @PrimaryKey
    val topic: String,                 // Ej: Z_4000, C_4000_plomeria
    val tipo: String,                  // ZONA, CONCURSO, OFERTA, PRESTADOR
    val fechaSuscripcion: Long = System.currentTimeMillis(),
    val estaActiva: Boolean = true     // Permite gestionar bajas lógicas
)


































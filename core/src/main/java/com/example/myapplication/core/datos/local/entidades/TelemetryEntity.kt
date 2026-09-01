package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * --- ENTIDAD DE TELEMETRÍA (ROOM) ---
 * [ELITE v12.0]: Almacena interacciones locales (Likes, Vistas, Clicks)
 * para ser enviadas en lote a Firebase a las 23:30/23:59.
 */
@Entity(tableName = "telemetry")
data class TelemetryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: TelemetryType,
    val targetId: String, // promoId o providerUid
    val value: Int = 1,    // Incremento (ej: +1 like)
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

enum class TelemetryType {
    LIKE,
    VIEW,
    CLICK
}


































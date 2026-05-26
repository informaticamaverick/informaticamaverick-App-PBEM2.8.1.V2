package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- MODELOS DE DATOS DEL CALENDARIO ---
 * Estos Enums y Data Classes estructuran la tabla de la base de datos.
 */

enum class EventType(val label: String, val emoji: String, val colorLong: Long) {
    VISIT("Visita Técnica", "🛠️", 0xFF2197F5),
    APPOINTMENT("Turno / Cita", "📅", 0xFF9B51E0),
    SHIPPING("Envío / Flete", "🚛", 0xFF10B981)
}

enum class VisitStatus {
    CONFIRMED,
    PENDING,
    CANCELLED
}

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val date: String,          // Formato "yyyy-MM-dd"
    val time: String,          // Ej: "10:30"
    val type: EventType,
    val title: String,         // Nombre del servicio o motivo
    val provider: String,      // Nombre del profesional/empresa
    val providerId: String,    // Para vincular con el chat
    val address: String,       // Dirección física
    val status: VisitStatus,
    val categoryName: String? = null, // 🔥 [NUEVO] Rubro del servicio (ej: Plomería)
    val categoryEmoji: String? = null, // 🔥 [NUEVO] Emoji del rubro
    val providerPhotoUrl: String? = null,
    val avatarColorLong: Long = 0xFF161C24 // Color de respaldo si no hay foto
)
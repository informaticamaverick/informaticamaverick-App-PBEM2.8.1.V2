package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- MODELOS DE DATOS DEL CALENDARIO ---
 * Define la estructura de las citas y visitas técnicas que comparten Cliente y Prestador.
 */

/**
 * Define la naturaleza del evento en el calendario.
 */
enum class EventType(val label: String, val emoji: String, val colorLong: Long) {
    VISIT("Visita Técnica", "🛠️", 0xFF2197F5),
    APPOINTMENT("Turno / Cita", "📅", 0xFF9B51E0),
    SHIPPING("Envío / Flete", "🚚", 0xFF10B981)
}

/**
 * Define el estado actual de la visita o turno.
 */
enum class VisitStatus {
    CONFIRMED,
    PENDING,
    CANCELLED
}

/**
 * Entidad de Room para eventos de calendario.
 */
@Entity(
    tableName = "calendar_events",
    indices = [Index(value = ["date"]), Index(value = ["providerId"])]
)
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val date: String,          // Formato "yyyy-MM-dd"
    val time: String,          // Ej: "10:30"
    val type: EventType,
    val title: String,         // Nombre del servicio o motivo
    val provider: String,      // Nombre del profesional/empresa
    val providerId: String,    // ID para vincular con el chat
    val address: String,       // Dirección física del evento
    val status: VisitStatus,
    val categoryName: String? = null,
    val categoryEmoji: String? = null,
    val providerPhotoUrl: String? = null,
    val avatarColorLong: Long = 0xFF161C24
)

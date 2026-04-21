package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de base de datos: Availability Schedule
 * Guarda los horarios de disponibilidad para profesionales con sistema de agenda
 */
@Entity(
    tableName = "availability_schedules",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["providerId"])]
)
data class AvailabilityScheduleEntity(
    @PrimaryKey
    val id: String,
    val providerId: String,
    val dayOfWeek: Int,              // 1=Lunes, 7=Domingo
    val startTime: String,           // Formato "HH:mm" ej: "09:00"
    val endTime: String,             // Formato "HH:mm" ej: "18:00"
    val appointmentDuration: Int,    // Duración de cada turno en minutos (15, 30, 45, 60)
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Helper para convertir día numérico a nombre completo
 */
fun Int.toDayName(): String {
    return when (this) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> "Desconocido"
    }
}

/**
 * Helper para convertir día numérico a abreviatura
 */
fun Int.toDayAbbr(): String {
    return when (this) {
        1 -> "L"
        2 -> "Ma"
        3 -> "Mi"
        4 -> "J"
        5 -> "V"
        6 -> "S"
        7 -> "D"
        else -> "?"
    }
}

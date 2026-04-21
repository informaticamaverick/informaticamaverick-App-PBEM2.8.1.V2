package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para espacios de alquiler (canchas, salones, estudios, etc.)
 * Usado cuando el serviceType del prestador es RENTAL
 */
@Entity(
    tableName = "rental_spaces",
    foreignKeys = [
        ForeignKey(
            entity = ProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["providerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("providerId")]
)
data class RentalSpaceEntity(
    @PrimaryKey
    val id: String,                     // UUID
    val providerId: String,             // FK a ProviderEntity
    val name: String,                   // Ej: "Cancha 1", "Salón Principal"
    val description: String? = null,    // Descripción opcional
    val pricePerHour: Double,           // Precio por hora
    val blockDuration: Int,             // Duración de bloque en minutos (60, 90, 120)
    val isActive: Boolean = true,       // Si está disponible para reservas
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

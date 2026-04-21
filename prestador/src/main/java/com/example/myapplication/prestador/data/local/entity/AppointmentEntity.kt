package com.example.myapplication.prestador.data.local.entity

import android.bluetooth.BluetoothAssignedNumbers
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entidad de base de dats: appointment (cita)
 * Guarda las citas programadas del prestador con sus clientes
 */

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = RentalSpaceEntity::class,
            parentColumns = ["id"],
            childColumns = ["rentalSpaceId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AvailabilityScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["providerId"]),
        Index(value = ["providerId", "date", "time"]),
        Index(value = ["rentalSpaceId"]),
        Index(value = ["scheduleId"])
    ]
)
data class AppointmentEntity(
    @PrimaryKey
    val id: String,
    val clientId: String,
    val clientName: String,
    val providerId: String,
    val service: String,
    val date: String, //Formato "YYYY-MM-DD"
    val time: String, //Formato "HH:MM"
    val duration: Int = 60, //Duración en minutos (default 60)
    val status: String, //pendiente, confirmado, cancelado, completo
    val notes: String = "",
    val proposedBy: String = "provider", //provider o cliente
    val serviceType: String = "TECHNICAL", //TECHNICAL, PROFESSIONAL, RENTAL, OTHER
    val rentalSpaceId: String? = null, //ID del espacio si es tipo RENTAL
    val scheduleId: String? = null, //ID del schedule si es tipo PROFESSIONAL
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val urgencyLevel: String? = null,
    val peopleCount: Int? = null,
    val assignedEmployeeIds: String? = null,
)
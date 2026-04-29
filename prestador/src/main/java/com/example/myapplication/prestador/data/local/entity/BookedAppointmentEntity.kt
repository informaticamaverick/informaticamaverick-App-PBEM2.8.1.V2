package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "booked_appointments")
data class BookedAppointmentEntity(
    @PrimaryKey val id: String,
    val clientId: String,
    val clientName: String,
    val date: String,           // "yyyy-MM-dd"
    val time: String,           // "HH:mm"
    val service: String = "",
    val notes: String = "",
    val status: String = "CONFIRMED",   // CONFIRMED, CANCELLED
    val chatId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

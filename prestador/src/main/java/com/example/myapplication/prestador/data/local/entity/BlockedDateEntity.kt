package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BlockedDateReason { HOLIDAY, CUSTOM }

@Entity(tableName = "blocked_dates")
data class BlockedDateEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val date: String,
    val label: String,
    val reason: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

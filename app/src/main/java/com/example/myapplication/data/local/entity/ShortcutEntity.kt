package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey val id: String,
    val context: String,
    val targetId: String,
    val type: String,
    val label: String? = null,
    val icon: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

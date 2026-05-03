package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD PARA PERSISTENCIA DE CATEGORÍAS FAST ---
 * Almacena el uso de categorías para el acceso rápido y el historial.
 */
@Entity(tableName = "fast_category_usage")
data class FastCategoryEntity(
    @PrimaryKey val name: String,
    val icon: String,
    val superCategory: String,
    val usageCount: Int = 1,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)

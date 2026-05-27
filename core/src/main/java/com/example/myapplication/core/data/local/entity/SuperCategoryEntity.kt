package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE SUPERCATEGORÍA (ROOM) ---
 * Almacena los grupos maestros de servicios con su identidad visual.
 * Permite que los colores y iconos sean dinámicos y persistentes.
 */
@Entity(tableName = "super_categories_table")
data class SuperCategoryEntity(
    @PrimaryKey
    val name: String,
    val icon: String,
    val color: Long, // Color mate/pastel en formato Long (ej: 0xFFFFD1D1)
    val description: String = ""
)

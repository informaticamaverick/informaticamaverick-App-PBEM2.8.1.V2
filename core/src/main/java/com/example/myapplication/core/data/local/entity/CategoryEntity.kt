package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- ENTIDAD DE CATEGORÍA (ROOM) ---
 * Representa un rubro o servicio (ej: "Plomería", "Electricidad").
 * Se agrupan en Supercategorías para la visualización estilo Bento.
 */
@Entity(
    tableName = "categories_table",
    indices = [
        Index(value = ["superCategory"]),
        Index(value = ["name"]) // 🔥 [ELITE] Indexado para búsquedas ultra-rápidas
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val name: String,
    val icon: String,
    val superCategory: String,
    val superCategoryIcon: String = "📂",
    val description: String = "",
    val providerIds: List<String> = emptyList(),
    val isNew: Boolean = false,
    val isNewPrestador: Boolean = false,
    val isAd: Boolean = false,
    val isFavorite: Boolean = false
)

package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories_table")
data class CategoryEntity(
    @PrimaryKey
    val name: String,

    val icon: String,

    val superCategory: String,
    val superCategoryIcon: String = "📂",
    
    // [NUEVO] Detalle descriptivo de la categoría
    val description: String = "",

    // [ARREGLO 2] Usamos List normal para que coincida con tu archivo Converters.kt
    val providerIds: List<String> = emptyList(),

    val isNew: Boolean,
    val isNewPrestador: Boolean,
    val isAd: Boolean,
    // [NUEVO] Estado de favorito para la categoría
    val isFavorite: Boolean = false
)
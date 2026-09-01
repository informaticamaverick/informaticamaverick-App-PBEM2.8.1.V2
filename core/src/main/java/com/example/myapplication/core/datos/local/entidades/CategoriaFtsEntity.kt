package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ÍNDICE DE BÚSQUEDA TÁCTICA: CATEGORÍAS (v2026.ELITE) ---
 * [PROPÓSITO]: Habilitar Full-Text Search (FTS4) para el catálogo de servicios.
 * [LEY #14]: El Embudo. Optimiza el filtrado en la fuente.
 */
@Keep
@Fts4(
    contentEntity = CategoriaEntity::class,
    tokenizer = "unicode61",
    tokenizerArgs = ["remove_diacritics=1"]
)
@Entity(tableName = "categorias_fts")
data class CategoriaFtsEntity(
    val nombre: String,
    val descripcion: String
)

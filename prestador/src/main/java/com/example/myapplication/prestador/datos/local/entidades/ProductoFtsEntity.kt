package com.example.myapplication.prestador.datos.local.entidades

import androidx.room.Entity
import androidx.room.Fts4
import androidx.annotation.Keep

/**
 * --- ÍNDICE DE BÚSQUEDA DE CATÁLOGO - COCINA PRIVADA (v2026.ELITE) ---
 */
@Keep
@Fts4(
    contentEntity = ProductoEntity::class,
    tokenizer = "unicode61",
    tokenizerArgs = ["remove_diacritics=1"]
)
@Entity(tableName = "productos_fts")
data class ProductoFtsEntity(
    val nombre: String,
    val descripcion: String
)

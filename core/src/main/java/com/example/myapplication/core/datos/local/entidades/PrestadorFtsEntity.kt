package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ÍNDICE DE BÚSQUEDA TÁCTICA: PRESTADORES (v2026.ELITE) ---
 */
@Keep
@Fts4(
    contentEntity = IdentidadPrestadorEntity::class,
    tokenizer = "unicode61",
    tokenizerArgs = ["remove_diacritics=1"]
)
@Entity(tableName = "prestadores_fts")
data class PrestadorFtsEntity(
    val nombreVisible: String,
    val biografia: String,
    val especialidades: String?
)

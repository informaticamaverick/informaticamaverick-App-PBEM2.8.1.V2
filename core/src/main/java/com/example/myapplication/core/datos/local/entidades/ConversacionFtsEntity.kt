package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ÍNDICE DE BÚSQUEDA TÁCTICA: CONVERSACIONES (v2026.ELITE) ---
 */
@Keep
@Fts4(
    contentEntity = ConversacionEntity::class,
    tokenizer = "unicode61",
    tokenizerArgs = ["remove_diacritics=1"]
)
@Entity(tableName = "conversaciones_fts")
data class ConversacionFtsEntity(
    val nombreRemoto: String,
    val ultimoMensaje: String
)

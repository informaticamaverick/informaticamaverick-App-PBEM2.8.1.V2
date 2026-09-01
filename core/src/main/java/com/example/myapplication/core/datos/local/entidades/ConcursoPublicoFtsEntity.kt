package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ÍNDICE DE BÚSQUEDA TÁCTICA: CONCURSOS PÚBLICOS (v2026.ELITE) ---
 * [PROPÓSITO]: Habilitar Full-Text Search (FTS4) para búsquedas instantáneas por texto.
 * [LEY #14]: El Embudo. Optimiza el filtrado en la fuente (SQLite Virtual Table).
 */
@Keep
@Fts4(
    contentEntity = ConcursoPublicoEntity::class,
    tokenizer = "unicode61",
    tokenizerArgs = ["remove_diacritics=1"]
)
@Entity(tableName = "concursos_publicos_fts")
data class ConcursoPublicoFtsEntity(
    val titulo: String,
    val descripcion: String
)

package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ENTIDAD FTS DE RECURSOS (v2026.ELITE) ---
 * [PROPÓSITO]: Búsqueda de texto completo para activos físicos.
 */
@Fts4(contentEntity = RecursoEntity::class)
@Entity(tableName = "recursos_fts")
data class RecursoFtsEntity(
    val nombre: String,
    val descripcion: String
)

package com.example.myapplication.core.datos.local.entidades

import androidx.room.Entity
import androidx.room.Fts4

/**
 * --- ENTIDAD FTS DE EQUIPO DE TRABAJO (v2026.ELITE) ---
 * [PROPÓSITO]: Búsqueda de texto completo para personal humano.
 */
@Fts4(contentEntity = EquipoTrabajoEntity::class)
@Entity(tableName = "equipo_trabajo_fts")
data class EquipoTrabajoFtsEntity(
    val nombre: String,
    val apellido: String,
    val cargo: String,
    val detalle: String
)

package com.example.myapplication.core.datos.local.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.core.datos.local.entidades.*

/**
 * --- RELACIÓN DE PRESTADOR COMPLETO (v2026.ELITE) ---
 * [PROPÓSITO]: Agrupar identidad profesional con sus activos personales.
 */
data class PrestadorCompletoRelacionesBD(
    @Embedded val prestador: IdentidadPrestadorEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "idPropietario"
    )
    val direcciones: List<DireccionEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "idPropietario"
    )
    val horario: HorarioEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "targetId"
    )
    val reseñas: List<ReviewEntity>
)


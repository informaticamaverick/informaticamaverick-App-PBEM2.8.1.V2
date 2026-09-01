package com.example.myapplication.core.datos.local.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.core.datos.local.entidades.*

/**
 * --- RELACIÓN DE SUCURSAL COMPLETA (v2026.ELITE) ---
 * [PROPÓSITO]: Ensamblar atómicamente todos los activos de un punto operativo.
 */
data class SucursalCompletaRelacionesBD(
    @Embedded val sucursal: SucursalEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSucursal"
    )
    val direccion: DireccionEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSucursal"
    )
    val horario: HorarioEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSucursal"
    )
    val equipoTrabajo: List<EquipoTrabajoEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSucursal"
    )
    val recursos: List<RecursoEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "targetId"
    )
    val reseñas: List<ReviewEntity>
)


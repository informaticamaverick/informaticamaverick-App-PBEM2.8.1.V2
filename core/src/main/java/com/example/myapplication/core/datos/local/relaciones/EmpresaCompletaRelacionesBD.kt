package com.example.myapplication.core.datos.local.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.core.datos.local.entidades.EmpresaEntity
import com.example.myapplication.core.datos.local.entidades.SucursalEntity

/**
 * --- RELACIÓN DE EMPRESA COMPLETA (v2026.ELITE) ---
 * [PROPÓSITO]: Agrupar la entidad legal con sus puntos de venta.
 */
data class EmpresaCompletaRelacionesBD(
    @Embedded val empresa: EmpresaEntity,

    @Relation(
        entity = SucursalEntity::class,
        parentColumn = "id",
        entityColumn = "idEmpresaPadre"
    )
    val sucursales: List<SucursalCompletaRelacionesBD>
)


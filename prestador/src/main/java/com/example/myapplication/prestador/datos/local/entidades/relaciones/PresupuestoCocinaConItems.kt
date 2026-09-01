package com.example.myapplication.prestador.datos.local.entidades.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity

/**
 * --- RELACIÓN DE COCINA: PRESUPUESTO CON SUS LÍNEAS (v2026.ELITE) ---
 */
data class PresupuestoCocinaConItems(
    @Embedded val cabecera: PresupuestoEntity,
    @Relation(
        parentColumn = "idPresupuesto",
        entityColumn = "idPresupuesto"
    )
    val items: List<ProductoEntity>
)

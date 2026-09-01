package com.example.myapplication.core.datos.local.entidades.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.ProductoFinalEntity
import com.example.myapplication.core.datos.local.entidades.FinanzaFinalEntity

/**
 * --- RELACIÓN SOBERANA: PRESUPUESTO CON ÍTEMS (v2026.SUPREME) ---
 */
data class PresupuestoConItems(
    @Embedded val cabecera: PresupuestoFinalEntity,
    @Relation(
        parentColumn = "idPresupuesto",
        entityColumn = "idPresupuesto"
    )
    val lineas: List<ProductoFinalEntity>,
    @Relation(
        parentColumn = "idPresupuesto",
        entityColumn = "idPresupuesto"
    )
    val finanzas: List<FinanzaFinalEntity>
)

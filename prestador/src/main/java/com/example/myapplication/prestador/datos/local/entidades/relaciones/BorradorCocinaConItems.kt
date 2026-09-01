package com.example.myapplication.prestador.datos.local.entidades.relaciones

import androidx.room.Embedded
import androidx.room.Relation
import com.example.myapplication.prestador.datos.local.entidades.BorradorPresupuestoEntity
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity

/**
 * --- RELACIÓN DE COCINA: BORRADOR CON SUS LÍNEAS (v2026.ELITE) ---
 */
data class BorradorCocinaConItems(
    @Embedded val cabecera: BorradorPresupuestoEntity,
    @Relation(
        parentColumn = "idBorrador",
        entityColumn = "idBorrador"
    )
    val items: List<ProductoEntity>
)

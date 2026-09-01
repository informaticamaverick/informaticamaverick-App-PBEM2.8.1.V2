package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- LÍNEA DE ÍTEM FINAL - SNAPSHOT COMERCIAL (v2026.SUPREME) ---
 * [PROPÓSITO]: Guardar la copia física e inmutable de un ítem al momento de la oferta.
 * [LEY #16]: Tabla Tablita Tablón. Normalización atómica por líneas.
 */
@Keep
@Entity(
    tableName = "productos",
    indices = [
        Index(value = ["idPresupuesto"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = PresupuestoFinalEntity::class,
            parentColumns = ["idPresupuesto"],
            childColumns = ["idPresupuesto"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductoFinalEntity(
    @PrimaryKey(autoGenerate = true) val idLinea: Long = 0,
    val idPresupuesto: String,
    val idOriginal: String? = null, // ID del catálogo si existe
    
    // Snapshot inmutable (v2026.SUPREME)
    val nombreCopiado: String,
    val descripcionCopiada: String = "",
    val cantidad: Int = 1,
    val precioSnapshot: Double = 0.0,
    val precioCostoSnapshot: Double = 0.0, // 🔥 [SUPREME] Para cálculo de margen histórico
    val porcentajeImpuesto: Double = 0.0,
    val porcentajeDescuento: Double = 0.0,
    
    val tipoItem: TipoProductoFinal = TipoProductoFinal.PRODUCTO
)

@Keep
enum class TipoProductoFinal {
    PRODUCTO,
    SERVICIO,
    GASTO
}

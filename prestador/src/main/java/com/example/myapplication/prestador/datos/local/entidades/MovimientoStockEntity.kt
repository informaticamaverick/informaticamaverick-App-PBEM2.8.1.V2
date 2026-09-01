package com.example.myapplication.prestador.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- TIPOS DE MOVIMIENTO DE INVENTARIO ---
 */
@Keep
enum class TipoMovimientoStock {
    COMPRA,
    VENTA,
    AJUSTE,
    DEVOLUCION,
    BAJA_ROTURA
}

/**
 * --- MOVIMIENTO DE STOCK - AUDITORÍA LEGAL (v2026.SUPREME) ---
 * [PROPÓSITO]: Registrar cada cambio físico en el inventario para trazabilidad.
 * [LEY #16]: Tabla Tablita Tablón. Desacoplado del producto para historial.
 */
@Keep
@Entity(
    tableName = "movimientos_stock",
    indices = [
        Index(value = ["idProducto"]),
        Index(value = ["marcaTiempo"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["id"],
            childColumns = ["idProducto"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MovimientoStockEntity(
    @PrimaryKey(autoGenerate = true) val idMovimiento: Long = 0,
    val idProducto: String,
    val cantidad: Int, // Positivo para entrada, Negativo para salida
    val tipo: TipoMovimientoStock,
    val motivo: String? = null,
    val idReferencia: String? = null, // ej: idPresupuesto
    val marcaTiempo: Long = System.currentTimeMillis()
)


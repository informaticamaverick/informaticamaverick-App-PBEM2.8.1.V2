package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- TIPOS DE DESGLOSE FINANCIERO (v2026.SUPREME) ---
 */
@Keep
enum class TipoFinanzaFinal {
    IMPUESTO,
    INTERES,
    DESCUENTO,
    RECARGO
}

/**
 * --- FINANZA FINAL - SNAPSHOT ECONÓMICO (v2026.SUPREME) ---
 * [PROPÓSITO]: Desglosar impuestos, intereses y cargos de un presupuesto inmutable.
 * [LEY #16]: Tabla Tablita Tablón. Normalización atómica financiera.
 */
@Keep
@Entity(
    tableName = "finanzas_final",
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
data class FinanzaFinalEntity(
    @PrimaryKey(autoGenerate = true) val idFinanza: Long = 0,
    val idPresupuesto: String,
    val etiqueta: String, // ej: "IVA 21%", "Recargo Tarjeta"
    val monto: Double,
    val tipo: TipoFinanzaFinal = TipoFinanzaFinal.IMPUESTO
)


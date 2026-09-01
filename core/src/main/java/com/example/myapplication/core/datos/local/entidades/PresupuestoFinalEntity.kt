package com.example.myapplication.core.datos.local.entidades

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- MODELO DE PRESUPUESTO FINAL - DOCUMENTO SOBERANO (v2026.SUPREME) ---
 * [PROPÓSITO]: Representar la cabecera inmutable de un presupuesto enviado/recibido.
 * [LEY #14]: El Embudo. Solo guarda el snapshot final, sin vínculos editables.
 */
@Keep
@Entity(
    tableName = "presupuestos_finales",
    indices = [
        Index(value = ["idConcurso"]),
        Index(value = ["idPrestador"]),
        Index(value = ["idCliente"]),
        Index(value = ["idCategoria"])
    ]
)
data class PresupuestoFinalEntity(
    @PrimaryKey val idPresupuesto: String = "",
    val idCliente: String = "",
    val idPrestador: String = "",
    val idConcurso: String? = null,
    val idCategoria: String? = null,
    val tipo: TipoPresupuesto = TipoPresupuesto.NUEVO,
    
    // Snapshot del prestador (Ley #3: Shallow Identity)
    val nombrePrestador: String = "",
    val nombreEmpresaPrestador: String? = null,
    val urlFotoPrestador: String? = null,
    val urlMiniatura: String? = null,
    
    val numeroPresupuesto: String? = null,
    val tituloTrabajo: String? = null,
    val totalGeneral: Double = 0.0,
    val subtotal: Double = 0.0,
    val subtotalArticulos: Double = 0.0, // 🔥 [ANALYTICS] Solo materiales
    val subtotalServicios: Double = 0.0, // 🔥 [ANALYTICS] Solo mano de obra/HONORARIOS
    val subtotalGastos: Double = 0.0,    // 🔥 [ANALYTICS] Logística/Varios
    val totalImpuestos: Double = 0.0,
    val totalIntereses: Double = 0.0,
    val totalDescuentos: Double = 0.0,
    val totalCostoGeral: Double = 0.0, // 🔥 [SUPREME] Para reportes Be-Profit instantáneos
    val moneda: String = "ARS",
    
    // --- [SUPREME]: Etiquetas Tácticas ---
    val etiquetaManoObra: String = "MANO DE OBRA", // ej: "Honorarios", "Servicios"
    
    // --- [ELITE]: Cláusulas inmutables ---
    val diasValidez: Int = 7,
    val notas: String? = null,
    val tiempoEjecucion: String? = null,
    val infoGarantia: String? = null,
    val metodosPago: String? = null,
    
    // --- [ELITE]: Reputación Snapshot ---
    val reputacionPrestador: Float? = null,
    val trabajosPrestador: Int? = null,
    
    val estado: EstadoPresupuesto = EstadoPresupuesto.PENDIENTE,
    val fechaEmision: Long = System.currentTimeMillis(),
    val fechaVencimiento: Long? = null,
    val leido: Boolean = false,
    val marcaTiempo: Long = System.currentTimeMillis()
)



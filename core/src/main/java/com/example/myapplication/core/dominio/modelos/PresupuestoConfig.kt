package com.example.myapplication.core.dominio.modelos

/**
 * --- CONFIGURACIÓN DE PRESUPUESTOS (V2026.FINAL) ---
 */
data class PresupuestoConfig(
    val validezDias: Int = 7,
    val moneda: String = "ARS",
    val prefijo: String = "PRE",
    val proximoNumero: Int = 1,
    val notaLegal: String = "Este presupuesto tiene carácter informativo.",
    val showArticlesByDefault: Boolean = true,
    val showServicesByDefault: Boolean = true,
    val showFeesByDefault: Boolean = true,
    val showMiscByDefault: Boolean = true,
    val showTaxesByDefault: Boolean = true,
    val showAttachmentsByDefault: Boolean = true,
    val notaObservacionesDefault: String = "",
    val descuentoDefault: Double = 0.0,
    val categoriaDefault: String = "General",
    val lastAcknowledgedMoneda: String = "ARS"
)


































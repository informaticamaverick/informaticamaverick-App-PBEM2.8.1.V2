package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- PLANTILLAS FINANCIERAS (v2026.SUPREME) ---
 */
@Keep
data class ImpuestoTemplate(
    val id: String,
    val nombre: String,
    val porcentaje: Double,
    val esRetencion: Boolean = false
)

object FinanzasMavTemplates {
    
    /**
     * 🇦🇷 Estándares Impositivos Argentina (v2026)
     */
    val argentina = listOf(
        ImpuestoTemplate("AR_IVA_21", "IVA General", 21.0),
        ImpuestoTemplate("AR_IVA_105", "IVA Reducido (Servicios/Bienes)", 10.5),
        ImpuestoTemplate("AR_IIBB_BA", "Ingresos Brutos (Bs.As.)", 3.5),
        ImpuestoTemplate("AR_IIBB_CABA", "Ingresos Brutos (CABA)", 3.0),
        ImpuestoTemplate("AR_PERC_IVA", "Percepción IVA", 1.5, true)
    )

    val interesesFinanciacion = listOf(
        ImpuestoTemplate("INT_3_CUOTAS", "Recargo 3 Cuotas", 15.0),
        ImpuestoTemplate("INT_6_CUOTAS", "Recargo 6 Cuotas", 35.0),
        ImpuestoTemplate("INT_EFECTIVO", "Descuento Efectivo", -10.0)
    )
}

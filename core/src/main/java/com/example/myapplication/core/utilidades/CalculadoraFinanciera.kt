package com.example.myapplication.core.utilidades

import kotlin.math.pow

/**
 * --- CALCULADORA FINANCIERA (V2026.ELITE) ---
 * [PROPÓSITO]: Proveer cálculos de cuotas e intereses bajo estándar bancario (Sistema Francés).
 */
object CalculadoraFinanciera {

    /**
     * Calcula el valor de la cuota mensual usando el Sistema Francés.
     * Fórmula: C = P * [ (i * (1 + i)^n) / ((1 + i)^n - 1) ]
     * @param montoPrincipal El capital original.
     * @param tasaAnual Tasa de Interés Nominal Anual (TNA) en porcentaje (ej: 45.0).
     * @param cuotas Cantidad de pagos.
     */
    fun calcularCuotaFrances(montoPrincipal: Double, tasaAnual: Double, cuotas: Int): Double {
        if (cuotas <= 1) return montoPrincipal
        if (tasaAnual <= 0) return montoPrincipal / cuotas

        val tasaMensual = (tasaAnual / 100.0) / 12.0
        val factor = (1.0 + tasaMensual).pow(cuotas.toDouble())
        
        return montoPrincipal * (tasaMensual * factor) / (factor - 1.0)
    }

    /**
     * Calcula el monto total a pagar (Principal + Intereses).
     */
    fun calcularTotalConInteres(montoPrincipal: Double, tasaAnual: Double, cuotas: Int): Double {
        val cuota = calcularCuotaFrances(montoPrincipal, tasaAnual, cuotas)
        return cuota * cuotas
    }

    /**
     * Genera el texto comercial para la burbuja.
     * Ej: "6 cuotas fijas de $ 12.500"
     */
    fun generarTextoCuotas(cuotas: Int, montoCuota: Double, sinInteres: Boolean): String {
        val prefijo = if (sinInteres) "Cuotas sin interés" else "Cuotas fijas"
        val montoFormateado = String.format(java.util.Locale.getDefault(), "%,.0f", montoCuota)
        return "$cuotas $prefijo de $ $montoFormateado"
    }
}

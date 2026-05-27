package com.example.myapplication.presentation.features.budget

import com.example.myapplication.core.data.local.entity.BudgetEntity
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * --- MODELOS DE DATOS PARA ANÁLISIS DE MERCADO (MAVERICK BI) ---
 * Centralizado para evitar redeclaraciones entre ViewModel y Pantalla.
 */

data class MarketAnalyticsState(
    val items: List<ChartBudgetItem> = emptyList(),
    val avgTotal: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val validCount: Int = 0,
    val isAnalyzing: Boolean = false
)

data class ChartBudgetItem(
    val budget: BudgetEntity,
    val total: Double,
    val mat: Double,
    val lab: Double,
    val tax: Double,
    val isIrrisory: Boolean,
    val isOptimal: Boolean
)

data class RankedBudget(
    val budget: BudgetEntity,
    val score: Double,
    val rating: Float,
    val jobsDone: Int,
    val priceDiffFromAvgPercent: Double,
    val valueForMoneyScore: Double, // Relación Calidad/Precio
    val awards: List<String> // Insignias ganadas
)

/**
 * MOTOR DE INTELIGENCIA DE MERCADO (ESTÁNDAR MAVERICK)
 */
object MaverickAnalyticsEngine {
    
    fun calculateMarketIntelligence(budgets: List<BudgetEntity>, avgTotal: Double, minTotal: Double, maxTotal: Double): List<RankedBudget> {
        if (budgets.isEmpty()) return emptyList()

        return budgets.map { budget ->
            val hash = budget.providerId.hashCode().absoluteValue
            val mockRating = 3.8f + (hash % 13) / 10f 
            val mockJobs = 15 + (hash % 200) 

            val priceScore = when {
                budget.grandTotal <= minTotal -> 1.0
                budget.grandTotal >= maxTotal -> 0.1
                else -> 1.0 - ((budget.grandTotal - minTotal) / (maxTotal - minTotal))
            }

            val ratingScore = (mockRating - 3.5) / 1.5 
            val experienceScore = min(mockJobs / 100.0, 1.0)

            val finalScore = ((priceScore * 0.50) + (ratingScore * 0.35) + (experienceScore * 0.15)) * 10.0
            val valueForMoneyScore = (ratingScore / (budget.grandTotal / avgTotal)).coerceIn(0.0, 10.0)

            val awards = mutableListOf<String>()
            if (budget.grandTotal == minTotal) awards.add("Mejor Precio")
            if (mockRating >= 4.8f) awards.add("Top Rated")
            if (budget.discountAmount > 0) awards.add("Descuento Aplicado")
            if (budget.warrantyInfo?.contains("año", ignoreCase = true) == true) awards.add("Garantía Extendida")
            if (budget.executionTime?.contains("inmediata", ignoreCase = true) == true) awards.add("Ejecución Rápida")
            if (valueForMoneyScore > 1.2) awards.add("Smart Choice")

            val diffPercent = if (avgTotal > 0) ((budget.grandTotal - avgTotal) / avgTotal) * 100 else 0.0

            RankedBudget(budget, finalScore, mockRating, mockJobs, diffPercent, valueForMoneyScore, awards)
        }.sortedByDescending { it.score }
    }
}

package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * --- MODELOS DE PRESUPUESTOS Y LICITACIONES ---
 * Este archivo centraliza la estructura de los presupuestos que el prestador
 * envía y el cliente recibe, así como las licitaciones (pedidos de servicio).
 */

/**
 * Representa un pedido de servicio (Licitación) publicado por un cliente.
 */
@Entity(
    tableName = "tenders",
    indices = [Index(value = ["category"]), Index(value = ["status"])]
)
data class TenderEntity(
    @PrimaryKey
    val tenderId: String,
    val title: String,
    val clientId: String,
    val description: String,
    val category: String,
    val status: String = "ABIERTA", // ABIERTA, CERRADA, ADJUDICADA, CANCELADA
    val isActive: Boolean = true,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = 0,
    val budgetCount: Int = 0,
    val cancellationDate: Long? = null,
    val awardedProviderId: String? = null,
    val awardedProviderName: String? = null,
    val awardedBudgetId: String? = null,
    val awardedProviderPhotoUrl: String? = null,

    // Cláusulas
    val requiresVisit: Boolean = false,
    val requiresPaymentMethod: Boolean = false,
    val requiresWorkGuarantee: Boolean = false,
    val requiresProviderDoc: Boolean = false,

    // Ubicación de la licitación
    val locationAddress: String? = null,
    val locationNumber: String? = null,
    val locationLocality: String? = null,
    val locationPostalCode: String? = null,
    val locationType: String? = null, // PERSONAL o BUSINESS

    // Emisor
    val clientDisplayName: String? = null,
    val companyName: String? = null,
    val branchName: String? = null,

    // Firebase Metadata
    val expiresAt: Long? = null,
    val matchKey: String? = null,
    val imageUrls: List<String> = emptyList()
)

/**
 * Representa un presupuesto formal enviado por un prestador.
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["tenderId"]), Index(value = ["providerId"])]
)
data class BudgetEntity(
    @PrimaryKey
    val budgetId: String,
    val clientId: String,
    val providerId: String,
    val tenderId: String? = null, // null si es presupuesto directo por chat

    val category: String? = null,
    val providerName: String,
    val providerCompanyName: String? = null,
    val providerPhotoUrl: String? = null,

    // Contenido del presupuesto (se guardan como JSON mediante Converters)
    val items: List<BudgetItem> = emptyList(),
    val services: List<BudgetService> = emptyList(),
    val professionalFees: List<BudgetProfessionalFee> = emptyList(),
    val miscExpenses: List<BudgetMiscExpense> = emptyList(),
    val taxes: List<BudgetTax> = emptyList(),
    val imageUrls: List<String> = emptyList(),

    // Totales
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double = 0.0,

    // Condiciones comerciales
    val validityDays: Int = 7,
    val notes: String? = null,
    val paymentMethods: String? = null,
    val warrantyInfo: String? = null,
    val executionTime: String? = null,

    val status: BudgetStatus = BudgetStatus.PENDIENTE,
    val isRead: Boolean = false,
    val dateTimestamp: Long = System.currentTimeMillis()
)

// --- ENUMS Y DATA CLASSES DE APOYO ---

enum class BudgetStatus { PENDIENTE, ACEPTADO, RECHAZADO, PAGADO, VENCIDO }

data class BudgetItem(
    val code: String = "",
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val taxPercentage: Double = 0.0,
    val discountPercentage: Double = 0.0
)

data class BudgetService(
    val code: String = "",
    val description: String,
    val total: Double
)

data class BudgetProfessionalFee(
    val code: String = "",
    val description: String,
    val total: Double
)

data class BudgetMiscExpense(
    val description: String,
    val amount: Double
)

data class BudgetTax(
    val description: String,
    val amount: Double
)

package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SECCIÓN 1: LICITACIONES (Tenders)
 * Esta tabla guarda las peticiones que el cliente publica para buscar servicios.
 */
@Entity(tableName = "tenders")
data class TenderEntity(
    @PrimaryKey
    val tenderId: String,          // ID único generado (usualmente vendrá de Firebase)
    val title: String,             // Título: "Arreglo de techo", "Instalación A/C"
    val clientId: String,          // Tu ID de usuario
    val description: String,       // Detalle de lo que el cliente necesita
    val category: String,          // Categoría: "Plomero", "Electricista"
    val status: String = "ABIERTA", // Estado: ABIERTA, CERRADA, ADJUDICADA, CANCELADA
    val isActive: Boolean = true,  // 🔥 [NUEVO] Indica si la licitación está activa o no( para en prestador) se enviara notificacion al prestador cuando sea false , o llegue a los 100, presupuestos, o cambie el estado a cerrado(vencido) , cancelado o
    val dateTimestamp: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis(), // Fecha de inicio
    val endDate: Long = 0,         // Fecha de fin de la licitación
    val budgetCount: Int = 0,       // Cuántos presupuestos ha recibido esta licitación
    val cancellationDate: Long? = null, // Fecha en la que se canceló
    val awardedProviderId: String? = null, // ID del prestador adjudicado
    val awardedProviderName: String? = null, // Nombre del prestador adjudicado
    val awardedBudgetId: String? = null,    // ID del presupuesto adjudicado

    // --- CLÁUSULAS Y REQUISITOS (Premium) ---
    val requiresVisit: Boolean = false,
    val requiresPaymentMethod: Boolean = false,
    val requiresWorkGuarantee: Boolean = false,
    val requiresProviderDoc: Boolean = false,

    // --- UBICACIÓN ---
    val locationAddress: String? = null,
    val locationNumber: String? = null,
    val locationLocality: String? = null,
    val locationType: String? = null, // PERSONAL o BUSINESS

    // --- MULTIMEDIA ---
    val imageUrls: List<String> = emptyList()
)

/**
 * SECCIÓN 2: PRESUPUESTOS (Budgets)
 * Esta tabla guarda los presupuestos que llegan por chat o por licitación.
 * Usamos "Indices" para que las búsquedas por ID de licitación sean instantáneas.
 */
@Entity(
    tableName = "budgets",
    indices = [Index(value = ["tenderId"]), Index(value = ["providerId"])]
)
data class BudgetEntity(
    @PrimaryKey
    val budgetId: String,          // ID único del presupuesto
    val clientId: String,          // Tu ID de usuario
    val providerId: String,        // ID del prestador que lo envía
    val tenderId: String? = null,  // Si es null, es un presupuesto directo de chat
    
    val category: String? = null,  // 🔥 Agregado: Categoría directa del presupuesto

    // Información del Prestador (Copia local para verla offline rápido)
    val providerName: String,
    val providerCompanyName: String? = null,
    val providerPhotoUrl: String? = null,

    // --- CONTENIDO DINÁMICO (Estas listas se guardarán como JSON) ---
    val items: List<BudgetItem> = emptyList(),
    val services: List<BudgetService> = emptyList(),
    val professionalFees: List<BudgetProfessionalFee> = emptyList(),
    val miscExpenses: List<BudgetMiscExpense> = emptyList(),
    val taxes: List<BudgetTax> = emptyList(),
    val imageUrls: List<String> = emptyList(),

    // --- TOTALES ---
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val grandTotal: Double = 0.0,

    // --- CONDICIONES COMERCIALES ---
    val validityDays: Int = 7,
    val notes: String? = null,
    val paymentMethods: String? = null, // Ej: "Contado", "Transferencia"
    val warrantyInfo: String? = null,   // Ej: "3 meses de garantía"
    val executionTime: String? = null,  // Ej: "Aproximadamente 2 días"

    val status: BudgetStatus = BudgetStatus.PENDIENTE,
    val isRead: Boolean = false, // 🔥 [NUEVO] Indica si el cliente ya abrió el presupuesto
    val dateTimestamp: Long = System.currentTimeMillis()
)

/**
 * SECCIÓN 3: ESTRUCTURAS DE APOYO
 * Estas clases no son tablas, sino la estructura de los datos dentro del presupuesto.
 */

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

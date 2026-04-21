package com.example.myapplication.prestador.ui.presupuesto

import android.net.Uri
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- DATA MODELS ---
data class BudgetItem(
    val id: Long = System.currentTimeMillis(),
    val code: String = "",
    val description: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val taxPercentage: Double = 0.0,
    val discountPercentage: Double = 0.0
)

data class BudgetService(
    val id: Long = System.currentTimeMillis(),
    var code: String = "",
    var description: String = "",
    var total: Double = 0.0
)

data class BudgetProfessionalFee(
    val id: Long = System.currentTimeMillis(),
    var code: String = "",
    var description: String = "",
    var total: Double = 0.0
)

data class BudgetMiscExpense(
    val id: Long = System.currentTimeMillis(),
    var description: String = "",
    var amount: Double = 0.0
)

data class BudgetTax(
    val id: Long = System.currentTimeMillis(),
    var description: String = "",
    var amount: Double = 0.0
)

data class BudgetAttachment(
    val id: Long = System.currentTimeMillis(),
    val uri: Uri? = null,
    var description: String = "",
    val type: AttachmentType = AttachmentType.IMAGE
)

enum class AttachmentType { IMAGE, PDF }

val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val MaverickBlueEnd = Color(0xFF2563EB)
val MaverickBlueStart = Color(0xFF1E40AF)
val MaverickGradient = Brush.linearGradient(colors = listOf(MaverickBlueStart, MaverickBlueEnd))

data class PresupuestoItemDisplay(
    val cantidad: String,
    val descripcion: String,
    val unitario: String,
    val total: String,
    val isSpecial: Boolean = false
)

//Dimensiones A4 (proporción real 210mm × 297mm)
val A4_WIDTH = 450.dp
val A4_HEIGHT = 636.dp  // 450 × (297/210)

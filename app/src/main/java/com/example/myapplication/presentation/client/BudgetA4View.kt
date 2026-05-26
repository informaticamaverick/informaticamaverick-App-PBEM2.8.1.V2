package com.example.myapplication.presentation.client

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetService
import com.example.myapplication.data.local.BudgetProfessionalFee
import com.example.myapplication.data.local.BudgetTax
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.ui.theme.MyApplicationTheme

val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate600 = Color(0xFF475569)
val Slate800 = Color(0xFF1E293B)
val MaverickBlueEnd = Color(0xFF2563EB)
val MaverickBlueStart = Color(0xFF1E40AF)
val MaverickGradient = androidx.compose.ui.graphics.Brush.linearGradient(colors = listOf(MaverickBlueStart, MaverickBlueEnd))

// --- ESTRUCTURA UNIFICADA PARA LA TABLA ---
data class PrintableRow(
    val qty: String,
    val description: String,
    val total: Double
)

/**
 * VISOR DE PRESUPUESTO A4 (MAVERICK FAST) - VERSIÓN REPARADA
 * Se corrigió el scroll de múltiples páginas y se añadieron las condiciones comerciales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetMultiPageScreen(
    budget: BudgetEntity,
    onAccept: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onBack: () -> Unit
) {
    // --- LÓGICA DE ZOOM (Controlado por botones para no romper el scroll) ---
    var scale by remember { mutableFloatStateOf(1f) }

    // --- CONSOLIDACIÓN DE DATOS ---
    val pagedItems = remember(budget) {
        val rows = mutableListOf<PrintableRow>()

        budget.items.forEach { rows.add(PrintableRow(it.quantity.toString(), it.description, it.unitPrice * it.quantity)) }
        budget.services.forEach { rows.add(PrintableRow("-", it.description, it.total)) }
        budget.professionalFees.forEach { rows.add(PrintableRow("-", it.description, it.total)) }
        budget.miscExpenses.forEach { rows.add(PrintableRow("-", it.description, it.amount)) }

        val pages = mutableListOf<List<PrintableRow>>()

        if (rows.isNotEmpty()) {
            // Primera hoja (6 ítems max para dar espacio al encabezado)
            pages.add(rows.take(6))
            val remaining = rows.drop(6).toMutableList()

            while (remaining.isNotEmpty()) {
                // Hojas siguientes aguantan hasta 12 filas para no desbordar
                pages.add(remaining.take(12))
                val nextChunk = remaining.drop(12)
                remaining.clear()
                remaining.addAll(nextChunk)
            }
        } else {
            pages.add(emptyList())
        }
        pages
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DETALLE DE OFERTA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("De: ${budget.providerName}", color = Color.Cyan.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                },
                actions = {
                    // Controles de Zoom seguros
                    IconButton(onClick = { scale = (scale + 0.25f).coerceAtMost(2.5f) }) {
                        Icon(Icons.Default.ZoomIn, "Acercar", tint = Color.White)
                    }
                    IconButton(onClick = { scale = (scale - 0.25f).coerceAtLeast(1f) }) {
                        Icon(Icons.Default.ZoomOut, "Alejar", tint = Color.White)
                    }
                    IconButton(onClick = { /* Exportar a PDF real */ }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            if (budget.status == BudgetStatus.PENDIENTE) {
                Surface(
                    color = Color(0xFF1E293B),
                    tonalElevation = 8.dp,
                    shadowElevation = 20.dp,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onReject(budget.budgetId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("RECHAZAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onAccept(budget.budgetId) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
                        ) {
                            Text("ACEPTAR ESTE", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        // --- LAZY COLUMN CON SCROLL NATIVO Y ZOOM APLICADO AL LAYER ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(pagedItems) { index, pageItems ->
                BudgetA4Page(
                    budget = budget,
                    pageItems = pageItems,
                    pageNumber = index + 1,
                    totalPoints = pagedItems.size,
                    isFirstPage = index == 0,
                    isLastPage = index == pagedItems.size - 1
                )
            }
            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
fun BudgetA4Page(
    budget: BudgetEntity,
    pageItems: List<PrintableRow>,
    pageNumber: Int,
    totalPoints: Int,
    isFirstPage: Boolean,
    isLastPage: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 500.dp)
            .aspectRatio(0.707f), // Proporción A4 estándar
        color = Color.White,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // --- ENCABEZADO DE PÁGINA ---
            if (isFirstPage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Logo y Empresa
                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaverickGradient)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                (budget.providerCompanyName ?: budget.providerName).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate800,
                                letterSpacing = (-0.5).sp,
                                lineHeight = 16.sp
                            )
                            Text(
                                "SERVICIOS PROFESIONALES",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                letterSpacing = 1.5.sp,
                                lineHeight = 11.sp
                            )
                        }
                    }

                    // La "X" con PRESUPUESTO debajo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(2.dp, Slate800, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Slate800)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Slate600, letterSpacing = 0.5.sp)
                    }

                    // Datos
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 3.dp)
                                .background(Slate50)
                                .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("N° ${budget.budgetId.takeLast(8).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Slate800)
                        }
                        // Fecha (usando timestamp del presupuesto)
                        val dateStr = remember(budget.dateTimestamp) {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(budget.dateTimestamp))
                        }
                        Text(dateStr, fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = Slate200)

                // Info del cliente y trabajo
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text("Maximiliano Nanterne", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800, lineHeight = 13.sp)
                        HorizontalDivider(color = Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TRABAJO / PROYECTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text("Proyecto de servicio", fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
                        HorizontalDivider(color = Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ID: ${budget.budgetId.takeLast(8).uppercase()}", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                    Text("PÁGINA $pageNumber DE $totalPoints", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Slate200)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TABLA DE DESGLOSE ---
            Row(modifier = Modifier.fillMaxWidth().background(Slate100).border(0.5.dp, Slate300).padding(8.dp)) {
                Text("CANT", modifier = Modifier.width(40.dp), color = Slate600, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("CONCEPTO / DESCRIPCIÓN", modifier = Modifier.weight(1f), color = Slate600, fontSize = 9.sp, fontWeight = FontWeight.Black)
                Text("TOTAL", modifier = Modifier.width(80.dp), color = Slate600, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End)
            }

            Column(modifier = Modifier.weight(1f).border(0.5.dp, Slate300)) {
                pageItems.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp)) {
                        Text(row.qty, modifier = Modifier.width(40.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        Text(row.description, modifier = Modifier.weight(1f), fontSize = 11.sp, color = Slate600)
                        Text(
                            text = "$ ${String.format("%,.2f", row.total)}",
                            modifier = Modifier.width(80.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.End,
                            color = Slate800
                        )
                    }
                    HorizontalDivider(color = Slate100)
                }
            }

            // --- RESUMEN, TOTALES Y CONDICIONES (SÓLO ÚLTIMA PÁGINA) ---
            if (isLastPage) {

                // CONDICIONES COMERCIALES
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(Slate50).padding(12.dp)) {
                    Text("CONDICIONES COMERCIALES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaverickBlueStart)
                    Spacer(modifier = Modifier.height(4.dp))

                    budget.executionTime?.let { Text("• Ejecución: $it", fontSize = 9.sp, color = Slate600) }
                    budget.warrantyInfo?.let { Text("• Garantía: $it", fontSize = 9.sp, color = Slate600) }
                    budget.paymentMethods?.let { Text("• Forma de pago: $it", fontSize = 9.sp, color = Slate600) }
                    Text("• Validez de oferta: ${budget.validityDays} días", fontSize = 9.sp, color = Slate600)

                    if (!budget.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("NOTAS ADICIONALES:", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(budget.notes, fontSize = 9.sp, color = Slate600, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                Column(modifier = Modifier.align(Alignment.End).width(240.dp).padding(top = 16.dp)) {
                    // Cuadro de Totales (Estilo proveedor)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(1.dp, Slate200, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 11.sp, color = Slate600)
                            Text("$ ${String.format("%,.2f", budget.subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }

                        if (budget.taxAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Impuestos:", fontSize = 11.sp, color = Slate600)
                                Text("$ ${String.format("%,.2f", budget.taxAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            }
                        }

                        if (budget.discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Descuento:", fontSize = 11.sp, color = Slate600)
                                Text("- $ ${String.format("%,.2f", budget.discountAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Slate200)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Slate800)
                            Text("$ ${String.format("%,.2f", budget.grandTotal)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaverickBlueEnd)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("--- DOCUMENTO CONTINÚA EN LA SIGUIENTE PÁGINA ---", textAlign = TextAlign.Center, fontSize = 9.sp, color = Slate300, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("MAVERICK FAST SERVICE • DOCUMENTO GENERADO AUTOMÁTICAMENTE", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 7.sp, color = Slate300)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun BudgetMultiPageScreenPreview() {
    // Generamos suficientes items para forzar múltiples páginas
    val sampleItems = (1..15).map {
        BudgetItem(description = "Insumo Técnico Nivel $it", quantity = 2, unitPrice = 1500.0)
    }
    val sampleServices = listOf(BudgetService(description = "Instalación de Nodos", total = 15000.0))
    val sampleFees = listOf(BudgetProfessionalFee(description = "Certificación de Red", total = 8000.0))

    val subtotal = (1500.0 * 2 * 15) + 15000.0 + 8000.0
    val sampleTaxes = listOf(BudgetTax(description = "IVA (21%)", amount = subtotal * 0.21))

    val sampleBudget = BudgetEntity(
        budgetId = "PRE-2024-TEST-01",
        clientId = "user_123",
        providerId = "prov_maverick",
        providerName = "Maximiliano Nanterne",
        providerCompanyName = "Maverick Informática S.A.",
        items = sampleItems,
        services = sampleServices,
        professionalFees = sampleFees,
        taxes = sampleTaxes,
        subtotal = subtotal,
        taxAmount = sampleTaxes.sumOf { it.amount },
        grandTotal = subtotal + sampleTaxes.sumOf { it.amount },
        notes = "Requiere pago del 50% por adelantado para congelar precio de materiales.",
        paymentMethods = "Transferencia Bancaria / MercadoPago",
        warrantyInfo = "12 meses de garantía escrita.",
        executionTime = "4 a 5 días hábiles.",
        validityDays = 15
    )

    MyApplicationTheme {
        BudgetMultiPageScreen(
            budget = sampleBudget,
            onBack = {}
        )
    }
}

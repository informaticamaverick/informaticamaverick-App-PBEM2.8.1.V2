package com.example.myapplication.uishared.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.core.data.local.entity.*
import com.example.myapplication.uishared.designsystem.MaverickColors
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- CONSTANTES DE DISEÑO A4 (Maverick Standard) ---
val A4_WIDTH = 450.dp
val A4_HEIGHT = 636.dp

/**
 * Modelo ligero para visualización de filas en la tabla.
 */
data class PresupuestoRowDisplay(
    val cantidad: String,
    val descripcion: String,
    val unitario: String,
    val total: String,
    val isSpecial: Boolean = false
)

val MaverickGradient = MaverickColors.MaverickA4Gradient

/**
 * HOJA A4 PURA (SÓLO EL DOCUMENTO)
 * Diseñada para ser incrustada en cualquier visor (Zoom, Paneo, etc.)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetA4Document(
    prestador: ProviderEntity?,
    budget: BudgetEntity,
    clientName: String = "Cliente",
    clientCompany: String? = null,
    clientAddress: String? = null,
    currencySymbol: String = "$",
    locale: Locale = Locale.getDefault(),
    isPreview: Boolean = LocalInspectionMode.current,
    captureLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
) {
    val displayRows = remember(budget, currencySymbol) {
        val rows = mutableListOf<PresupuestoRowDisplay>()
        budget.items.forEach { item ->
            rows.add(PresupuestoRowDisplay(
                cantidad = item.quantity.toString(),
                descripcion = item.description,
                unitario = "$currencySymbol ${String.format(locale, "%,.2f", item.unitPrice)}",
                total = "$currencySymbol ${String.format(locale, "%,.2f", item.unitPrice * item.quantity)}"
            ))
        }
        budget.services.forEach { service ->
            rows.add(PresupuestoRowDisplay("-", service.description, "-", "$currencySymbol ${String.format(locale, "%,.2f", service.total)}", true))
        }
        budget.professionalFees.forEach { fee ->
            rows.add(PresupuestoRowDisplay("-", fee.description, "-", "$currencySymbol ${String.format(locale, "%,.2f", fee.total)}", true))
        }
        budget.miscExpenses.forEach { exp ->
            rows.add(PresupuestoRowDisplay("-", exp.description, "-", "$currencySymbol ${String.format(locale, "%,.2f", exp.amount)}"))
        }
        rows
    }

    Box(
        modifier = Modifier
            .width(A4_WIDTH)
            .height(A4_HEIGHT)
            .shadow(elevation = 12.dp)
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaverickColors.Slate300)
                .drawWithContent {
                    if (isPreview || captureLayer == null) {
                        drawContent()
                    } else {
                        captureLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(captureLayer)
                    }
                }
        ) {
            // Branding Maverick
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaverickColors.MaverickA4Gradient))

            // Secciones del Documento
            if (prestador != null) {
                A4HeaderSection(prestador, budget)
            } else {
                // Fallback header si no hay ProviderEntity completo
                A4SimpleHeader(budget)
            }
            
            HorizontalDivider(color = MaverickColors.Slate200)
            
            if (prestador != null) {
                A4ClientInfoSection(prestador, budget, clientName, clientCompany, clientAddress)
            } else {
                A4SimpleClientInfo(budget, clientName)
            }
            
            A4ItemsTable(displayRows)

            Spacer(modifier = Modifier.weight(1f))

            A4FooterSection(budget, currencySymbol, locale)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun A4SimpleHeader(budget: BudgetEntity) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(MaverickColors.MaverickA4Gradient).padding(6.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text((budget.providerCompanyName ?: budget.providerName).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaverickColors.Slate800, letterSpacing = (-0.5).sp, lineHeight = 16.sp)
                Text("SERVICIOS PROFESIONALES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate400, letterSpacing = 1.5.sp, lineHeight = 11.sp)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
            Box(modifier = Modifier.size(40.dp).border(2.dp, MaverickColors.Slate800, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) { Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = MaverickColors.Slate800) }
            Spacer(modifier = Modifier.height(4.dp))
            Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaverickColors.Slate600, letterSpacing = 0.5.sp)
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.padding(vertical = 3.dp).background(Color(0xFFDBEAFE)).border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("N° ${budget.budgetId.takeLast(8).uppercase()}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF1E40AF))
            }
            Text(currentDate, fontSize = 10.sp, color = MaverickColors.Slate800, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun A4SimpleClientInfo(budget: BudgetEntity, clientName: String) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text((budget.providerCompanyName ?: budget.providerName).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate800, lineHeight = 14.sp)
        HorizontalDivider(color = MaverickColors.Slate200, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate400)
                Text(clientName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate800, lineHeight = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                Text("TRABAJO / PROYECTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate400)
                Text(budget.category ?: "Servicio Profesional", fontSize = 11.sp, color = MaverickColors.Slate800, lineHeight = 14.sp, textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = MaverickColors.Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

/**
 * VISOR DE HOJA A4 CON CONTROLES DE ZOOM Y PANEO (ESTÁNDAR MAVERICK)
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetA4Viewer(
    prestador: ProviderEntity?,
    budget: BudgetEntity,
    onDismiss: () -> Unit,
    clientName: String = "Cliente",
    actions: @Composable (BoxScope.(Float, Offset) -> Unit)? = null
) {
    val captureLayer = rememberGraphicsLayer()
    val isPreview = LocalInspectionMode.current
    val locale = remember { Locale.getDefault() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val initialFitScale = remember(screenWidth) { ((screenWidth - 32.dp) / A4_WIDTH).coerceAtMost(1f) }
        var scale by remember { mutableFloatStateOf(initialFitScale) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF202020))) {
            // Barra superior: Cerrar
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).background(Color.White.copy(alpha = 0.9f), CircleShape).size(48.dp)) {
                    Icon(Icons.Default.Close, "Cerrar", tint = MaverickColors.Slate800)
                }
            }

            // Contenedor interactivo
            Box(modifier = Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(initialFitScale, 4f)
                    offset += pan
                }
            }) {
                Box(modifier = Modifier.align(Alignment.Center).graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)) {
                    BudgetA4Document(prestador, budget, clientName, locale = locale, isPreview = isPreview, captureLayer = captureLayer)
                }
            }

            // Espacio para acciones personalizadas (Botones Enviar / Aceptar / Rechazar)
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2A2A2A)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Controles de Zoom (Fijos en el visor)
                    Row(modifier = Modifier.background(MaverickColors.Slate800.copy(alpha = 0.9f), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { scale = (scale * 0.8f).coerceAtLeast(initialFitScale); offset = Offset.Zero }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                        Text("${(scale / initialFitScale * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 50.dp), textAlign = TextAlign.Center)
                        IconButton(onClick = { scale = (scale * 1.25f).coerceAtMost(4f) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                    }
                    
                    // Slot de acciones (aquí el cliente pondrá sus botones)
                    actions?.invoke(this@Box, scale, offset)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPreviewPDFDialog(
    prestador: ProviderEntity,
    budget: BudgetEntity,
    onDismiss: () -> Unit,
    onEnviar: (() -> Unit)? = null,
    onCapturePng: ((android.graphics.Bitmap) -> Unit)? = null,
    clientName: String = "Cliente",
    showSendButton: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()
    // [TODO] En el futuro captureLayer debería venir desde fuera si queremos el PNG
    
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BudgetA4Viewer(prestador = prestador, budget = budget, onDismiss = onDismiss, clientName = clientName) { scale, offset ->
            if (showSendButton) {
                Button(
                    onClick = { onEnviar?.invoke() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enviar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Contenido visual de la hoja A4 y controles.
 * [OBSOLETO] Se mantiene por compatibilidad momentánea pero se recomienda usar BudgetA4Viewer.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPreviewSheetContent(
    prestador: ProviderEntity,
    budget: BudgetEntity,
    onDismiss: () -> Unit,
    onEnviar: (() -> Unit)? = null,
    onCapturePng: ((android.graphics.Bitmap) -> Unit)? = null,
    currencySymbol: String = "$",
    clientName: String = "Cliente",
    clientCompany: String? = null,
    clientAddress: String? = null,
    showSendButton: Boolean = true
) {
    BudgetA4Viewer(prestador = prestador, budget = budget, onDismiss = onDismiss, clientName = clientName) { _, _ ->
        if (showSendButton) {
            Button(onClick = { onEnviar?.invoke() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))) {
                Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Enviar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun A4HeaderSection(prestador: ProviderEntity, budget: BudgetEntity) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val providerDisplayName = (prestador.companies.firstOrNull()?.name ?: budget.providerCompanyName ?: "${prestador.name} ${prestador.lastName}").uppercase()
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(MaverickColors.MaverickA4Gradient).padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(providerDisplayName, fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaverickColors.Slate800, letterSpacing = (-0.5).sp, lineHeight = 16.sp)
                Text(prestador.categories.firstOrNull() ?: "SERVICIOS PROFESIONALES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate400, letterSpacing = 1.5.sp, lineHeight = 11.sp)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
            Box(modifier = Modifier.size(40.dp).border(2.dp, MaverickColors.Slate800, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = MaverickColors.Slate800)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaverickColors.Slate600, letterSpacing = 0.5.sp)
        }

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.padding(vertical = 3.dp).background(Color(0xFFDBEAFE)).border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("N° ${budget.budgetId.takeLast(8).uppercase()}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF1E40AF))
            }
            Text(currentDate, fontSize = 10.sp, color = MaverickColors.Slate800, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun A4ClientInfoSection(prestador: ProviderEntity, budget: BudgetEntity, clientName: String, clientCompany: String?, clientAddress: String?) {
    val company = prestador.companies.firstOrNull()
    val providerDisplayName = (company?.name ?: budget.providerCompanyName ?: "${prestador.name} ${prestador.lastName}").uppercase()
    val providerFullAddress = company?.branches?.firstOrNull()?.address?.fullString() ?: prestador.address?.fullString() ?: ""

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(providerDisplayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate800, lineHeight = 14.sp)
            if (company?.cuit != null) Text("CUIT ${company.cuit}", fontSize = 9.sp, color = MaverickColors.Slate600)
            if (providerFullAddress.isNotBlank()) Text(providerFullAddress, fontSize = 9.sp, color = MaverickColors.Slate600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!prestador.matricula.isNullOrBlank()) Text("Mat. ${prestador.matricula}", fontSize = 9.sp, color = MaverickColors.Slate600)
        }

        HorizontalDivider(color = MaverickColors.Slate200, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate400)
                Text(clientName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate800, lineHeight = 13.sp)
                if (!clientCompany.isNullOrBlank()) Text(clientCompany, fontSize = 9.sp, color = MaverickColors.Slate600)
                if (!clientAddress.isNullOrBlank()) Text(clientAddress, fontSize = 9.sp, color = MaverickColors.Slate600)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                Text("TRABAJO / PROYECTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate400)
                Text(budget.category ?: "Proyecto de servicio", fontSize = 11.sp, color = MaverickColors.Slate800, lineHeight = 14.sp, textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = MaverickColors.Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun A4ItemsTable(rows: List<PresupuestoRowDisplay>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.border(1.dp, MaverickColors.Slate300)) {
            // Header
            Row(modifier = Modifier.background(MaverickColors.Slate100).height(IntrinsicSize.Min)) {
                A4TableCell("Cant", 0.15f, isHeader = true)
                A4TableCell("Descripción", 0.55f, isHeader = true)
                A4TableCell("Unitario", 0.3f, isHeader = true, alignRight = true)
                A4TableCell("Total", 0.3f, isHeader = true, alignRight = true, isLast = true)
            }
            HorizontalDivider(color = MaverickColors.Slate300)

            // Filas
            rows.forEach { row ->
                val bg = if (row.isSpecial) Color(0xFFEFF6FF) else Color.White
                val weight = if (row.isSpecial) FontWeight.Bold else FontWeight.Normal
                Row(modifier = Modifier.background(bg).height(IntrinsicSize.Min)) {
                    A4TableCell(row.cantidad, 0.15f, color = MaverickColors.Slate600)
                    A4TableCell(row.descripcion, 0.55f, fontWeight = weight)
                    A4TableCell(row.unitario, 0.3f, alignRight = true, color = MaverickColors.Slate600)
                    A4TableCell(row.total, 0.3f, alignRight = true, fontWeight = FontWeight.Bold, isLast = true)
                }
                HorizontalDivider(color = MaverickColors.Slate300)
            }
        }
    }
}

@Composable
fun RowScope.A4TableCell(
    text: String, 
    weight: Float, 
    isHeader: Boolean = false, 
    alignRight: Boolean = false, 
    isLast: Boolean = false, 
    color: Color = Color.Unspecified, 
    fontWeight: FontWeight? = null
) {
    Box(
        modifier = Modifier.weight(weight).fillMaxHeight().then(if (!isLast) Modifier.border(width = 0.5.dp, color = MaverickColors.Slate300.copy(alpha = 0.5f)) else Modifier).padding(6.dp),
        contentAlignment = if (alignRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(text = text, fontSize = if (isHeader) 9.sp else 10.sp, fontWeight = fontWeight ?: (if (isHeader) FontWeight.Bold else FontWeight.Normal), color = if (color == Color.Unspecified) MaverickColors.Slate800 else color, textAlign = if (alignRight) TextAlign.End else TextAlign.Start)
    }
}

@Composable
fun A4FooterSection(budget: BudgetEntity, currencySymbol: String, locale: Locale) {
    Column(modifier = Modifier.fillMaxWidth().background(MaverickColors.Slate50).padding(horizontal = 24.dp, vertical = 16.dp)) {
        
        // Condiciones Comerciales
        if (budget.executionTime != null || budget.warrantyInfo != null || budget.paymentMethods != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text("CONDICIONES COMERCIALES", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaverickColors.MaverickBlueStart)
                budget.executionTime?.let { Text("• Ejecución: $it", fontSize = 9.sp, color = MaverickColors.Slate600) }
                budget.warrantyInfo?.let { Text("• Garantía: $it", fontSize = 9.sp, color = MaverickColors.Slate600) }
                budget.paymentMethods?.let { Text("• Pago: $it", fontSize = 9.sp, color = MaverickColors.Slate600) }
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = MaverickColors.Slate200)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            // Nota Legal
            Text(text = budget.notes ?: "Nota: Los precios están expresados en moneda local.\nVálido por ${budget.validityDays} días.", fontSize = 10.sp, color = MaverickColors.Slate400, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, lineHeight = 14.sp, modifier = Modifier.width(180.dp))

            // Cuadro de Totales
            Column(modifier = Modifier.width(200.dp).shadow(2.dp, RoundedCornerShape(4.dp)).background(Color.White, RoundedCornerShape(4.dp)).border(1.dp, MaverickColors.Slate300, RoundedCornerShape(4.dp)).padding(12.dp)) {
                FooterTotalRow("Subtotal:", "$currencySymbol ${String.format(locale, "%,.2f", budget.subtotal)}")
                
                // Desglose de impuestos sellados
                if (budget.taxes.isNotEmpty()) {
                    budget.taxes.forEach { tax ->
                        FooterTotalRow("${tax.description}:", "+ $currencySymbol ${String.format(locale, "%,.2f", tax.amount)}")
                    }
                } else if (budget.taxAmount > 0) {
                    FooterTotalRow("Impuestos:", "+ $currencySymbol ${String.format(locale, "%,.2f", budget.taxAmount)}")
                }
                
                if (budget.discountAmount > 0) FooterTotalRow("Descuento:", "- $currencySymbol ${String.format(locale, "%,.2f", budget.discountAmount)}")
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaverickColors.Slate200)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaverickColors.Slate800)
                    Text("$currencySymbol ${String.format(locale, "%,.2f", budget.grandTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaverickColors.MaverickBlueEnd)
                }
            }
        }
    }
}

@Composable
fun FooterTotalRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 10.sp, color = MaverickColors.Slate600)
        Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaverickColors.Slate800)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 500, heightDp = 800, name = "Professional A4 Budget Preview")
@Composable
fun PreviewWinnerBudget() {
    MaterialTheme {
        val prestadorTest = ProviderEntity(
            id = "test", email = "soporte@maverick.com", phoneNumber = "123456",
            displayName = "Maverick S.A.", name = "Maximiliano", lastName = "Nanterne",
            categories = listOf("Seguridad"), matricula = "MAT-001",
            createdAt = 0L
        )
        val budgetTest = BudgetEntity(
            budgetId = "PRE-001", clientId = "cli", providerId = "test", providerName = "Maximiliano",
            providerCompanyName = "Maverick Tech",
            items = listOf(BudgetItem(description = "Licencia Pro", quantity = 1, unitPrice = 5000.0)),
            subtotal = 5000.0, grandTotal = 5000.0,
            executionTime = "24 hs",
            paymentMethods = "Efectivo / Transferencia"
        )
        Surface(color = Color(0xFF202020)) {
            BudgetPreviewSheetContent(prestadorTest, budgetTest, onDismiss = {})
        }
    }
}

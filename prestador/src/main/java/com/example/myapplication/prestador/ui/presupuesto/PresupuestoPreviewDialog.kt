package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoConfigViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetPreviewPDFDialog(
    prestador: Provider,
    items: List<BudgetItem>,
    services: List<BudgetService>,
    professionalFees: List<BudgetProfessionalFee>,
    miscExpenses: List<BudgetMiscExpense>,
    taxes: List<BudgetTax>,
    grandTotal: Double,
    subtotal: Double,
    taxAmount: Double,
    discountAmount: Double,
    onDismiss: () -> Unit,
    onEnviar: () -> Unit,
    onEnviarBudget: (() -> Unit)? = null,
    onCapturePng: ((android.graphics.Bitmap) -> Unit)? = null,
    onCompartirPDF: ((android.graphics.Bitmap) -> Unit)? = null,
    onEnviarBudgetConImagen: ((String) -> Unit)? = null,
    showSendButton: Boolean = true,
    showTaxDetail: Boolean = false,
    clientName: String = "",
    clientCompany: String? = null,
    clientAddress: String? = null,
    clienteCompany: String? = null,
    providerName: String = "",
    providerAddress: String = "",
    isProfessional: Boolean = false,
    presupuestoNumero: String = "",
    tituloTrabajo: String = "",
    category: String = "",
    validezDias: Int = 15,
    notaLegal: String = ""
){
    val coroutineScope = rememberCoroutineScope()
    val captureLayer = rememberGraphicsLayer()
    val context = LocalContext.current
    val configViewModel: PresupuestoConfigViewModel = hiltViewModel()
    val presupuestoConfig by configViewModel.config.collectAsState()
    val notaLegalResolved = presupuestoConfig.notaLegal.ifBlank { notaLegal }
    val currencySymbol = when (presupuestoConfig.moneda) {
        "USD" -> "US$"
        else  -> "$"
    }
    //CONVERTIR ITEMS A FORMATO DE DISPLAY

    val displayItems = mutableListOf<PresupuestoItemDisplay>()

    //Agregar articulos
    items.forEach { item ->
        val base = item.unitPrice * item.quantity
        val taxAmount = base * (item.taxPercentage / 100)
        val withTax = base + taxAmount
        val discountAmount = withTax * (item.discountPercentage / 100)
        val total = withTax - discountAmount

        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = item.quantity.toString(),
                descripcion = item.description,
                unitario = "$currencySymbol ${String.format("%,.2f", item.unitPrice)}",
                total = "$currencySymbol ${String.format("%,.2f", total)}"
            )
        )
    }

    // Agregar servicios
    services.forEach { service ->
        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = "-",
                descripcion = service.description,
                unitario = "-",
                total = "$currencySymbol ${String.format("%,.2f", service.total)}",
                isSpecial = true
            )
        )
    }
    
    // Agregar honorarios profesionales
    professionalFees.forEach { fee ->
        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = "-",
                descripcion = fee.description,
                unitario = "-",
                total = "$currencySymbol ${String.format("%,.2f", fee.total)}",
                isSpecial = true
            )
        )
    }

    // Agregar gastos varios
    miscExpenses.forEach { expense ->
        displayItems.add(
            PresupuestoItemDisplay(
                cantidad = "-",
                descripcion = expense.description,
                unitario = "-",
                total = "$currencySymbol ${String.format("%,.2f", expense.amount)}"
            )
        )
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val screenWidth = maxWidth
            val initialFitScale = remember (screenWidth) {
                ((screenWidth - 32.dp) / A4_WIDTH).coerceAtMost(1f)
            }
            // Estados de zoom y paneo
            var scale by remember { mutableFloatStateOf(initialFitScale) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF202020))
                    .padding(bottom = 56.dp)
            ) {
                // Barra superior con botón cerrar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            .size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Slate800)
                    }
                }

                // Contenedor zoomable
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(initialFitScale, 4f)
                                offset += pan
                            }
                        }
                ) {
                    // La hoja de papel A4 (proporción real 210×297mm)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .width(A4_WIDTH)
                            .height(A4_HEIGHT)
                            .shadow(elevation = 12.dp)
                            .background(Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Slate300)
                                .drawWithContent {
                                    captureLayer.record { this@drawWithContent.drawContent() }
                                    drawLayer(captureLayer)
                                }
                        ) {
                            //Franja decorativa
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(MaverickGradient)
                            )

                            // Encabezado
                            A4HeaderSection(prestador, providerName, presupuestoNumero, isProfessional)
                            HorizontalDivider(color = Slate200)

                            // Datos Cliente
                            A4ClientInfoSection(prestador, clientName, clientCompany, clientAddress, providerName, providerAddress, isProfessional, tituloTrabajo, category = category)

                            // Tabla de items
                            A4ItemsTable(displayItems)

                            // Empuja el footer hacia el fondo de la hoja
                            Spacer(modifier = Modifier.weight(1f))

                            //Footer
                            A4FooterSection(
                                subtotal = subtotal,
                                taxAmount = taxAmount,
                                discountAmount = discountAmount,
                                total = grandTotal,
                                taxes = if (showTaxDetail) taxes else emptyList(),
                                validezDias = validezDias,
                                notaLegal = notaLegalResolved,
                                currencySymbol = currencySymbol
                            )
                        }
                    }
                }

                // Barra inferior: zoom + enviar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Controles de zoom
                    Row(
                        modifier = Modifier
                            .background(Slate800.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                scale = (scale * 0.8f).coerceAtLeast(initialFitScale)
                                offset = Offset.Zero
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Alejar", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "${(scale / initialFitScale * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.widthIn(min = 50.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = {
                                scale = (scale * 1.25f).coerceAtMost(4f)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, "Acercar", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                scale = initialFitScale
                                offset = Offset.Zero
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, "Resetear", tint = Color.White)
                        }
                    }

                    //Botón compartir PDF
                    if (onCompartirPDF != null) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val bitmap = captureLayer.toImageBitmap().asAndroidBitmap()
                                    onCompartirPDF(bitmap)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF6B35))
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color(0XFFFF6B65), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Compartir PDF", color = Color(0xFFFF6B35), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    // Botón enviar
                    if (showSendButton) {
                        Button(
                            onClick = {
                                when {
                                    onEnviarBudget != null -> onEnviarBudget()
                                    onCapturePng != null -> {
                                        coroutineScope.launch {
                                            val bitmap = captureLayer.toImageBitmap().asAndroidBitmap()
                                            onCapturePng(bitmap)
                                        }
                                    }
                                    else -> onEnviar()
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6B35)
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Enviar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun A4HeaderSection(
    prestador: Provider,
    providerName: String = "",
    presupuestoNumero: String = "",
    isProfessional: Boolean = false
) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Logo y dirección
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
                    providerName.ifBlank { "${prestador.name} ${prestador.lastName}" }.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate800,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 16.sp
                )
                Text(
                    prestador.categories.firstOrNull() ?: "SERVICIOS",
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
            Text(if (isProfessional) "CONSULTA" else "PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Slate600, letterSpacing = 0.5.sp)
        }

        // Datos
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .padding(vertical = 3.dp)
                    .background(Color(0xFFDBEAFE))
                    .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("N° ${presupuestoNumero.ifBlank { prestador.id.takeLast(8) }}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF1E40AF))
            }
            Text(currentDate, fontSize = 10.sp, color = Slate800, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun A4ClientInfoSection(prestador: Provider,
                        clientName: String = "",
                        clientCompany: String? = null,
                        clientAddress: String? = null,
                        providerName: String = "",
                        providerAddress: String = "",
                        isProfessional: Boolean = false,
                        tituloTrabajo: String = "",
                        providerPhone: String = "",
                        providerEmail: String = "",
                        category: String = "") {
    // Nombre a mostrar: empresa si tiene, sino nombre completo
    val selectedCompany = prestador.companies.firstOrNull()
    val displayName = if (prestador.priorizarEmpresa && selectedCompany != null) {
        selectedCompany.name
    } else {
        providerName.ifBlank { "${prestador.name} ${prestador.lastName}".trim() }
    }
    val displayPhone = providerPhone.ifBlank { prestador.phoneNumber }
    val displayEmail = providerEmail.ifBlank { prestador.email }
    val displayAddress = if (prestador.priorizarEmpresa && selectedCompany != null) {
        selectedCompany.branches.firstOrNull()?.address?.fullString() ?: prestador.address?.fullString() ?: providerAddress
    } else {
        providerAddress.ifBlank { prestador.address?.fullString() ?: "" }
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        // --- Datos del prestador: nombre, CUIT/matrícula, dirección ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                displayName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                lineHeight = 14.sp
            )
            if (prestador.priorizarEmpresa && selectedCompany != null) {
                // Modo empresa: razón social + CUIT + dirección empresa
                val cuit = selectedCompany.cuit.takeIf { it.isNotBlank() } ?: prestador.cuilCuit
                if (cuit != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Business, null, tint = Slate400, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                        Text("CUIT $cuit", fontSize = 9.sp, color = Slate600, lineHeight = 11.sp)
                    }
                }
                if (displayAddress.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Slate400, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                        Text(displayAddress, modifier = Modifier.weight(1f), fontSize = 9.sp, color = Slate600, lineHeight = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else {
                // Modo individual: dirección personal + categoría + matrícula
                if (displayAddress.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Slate400, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                        Text(displayAddress, modifier = Modifier.weight(1f), fontSize = 9.sp, color = Slate600, lineHeight = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (category.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Category, null, tint = Slate400, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                    Text(category.uppercase(), fontSize = 9.sp, color = Slate600, lineHeight = 11.sp)
                }
            }
            if (!prestador.matricula.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Badge, null, tint = Slate400, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                    Text(
                        text = "Mat. ${prestador.matricula}",
                        fontSize = 9.sp,
                        color = Slate600,
                        lineHeight = 11.sp
                    )
                }
            }
            
            // Teléfono y Email del prestador
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (displayPhone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Phone, null, tint = Slate400, modifier = Modifier.size(10.dp))
                        Text(displayPhone, fontSize = 9.sp, color = Slate600)
                    }
                }
                if (displayEmail.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Email, null, tint = Slate400, modifier = Modifier.size(10.dp))
                        Text(displayEmail, fontSize = 9.sp, color = Slate600)
                    }
                }
            }
        }

        HorizontalDivider(color = Slate200, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

        // --- Cliente / destinatario ---
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(clientName.ifBlank { "Cliente" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800, lineHeight = 13.sp)
                if (!clientCompany.isNullOrBlank()) {
                    Text(clientCompany, fontSize = 9.sp, color = Slate600, lineHeight = 11.sp)
                }
                if (!clientAddress.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Slate400, modifier = Modifier.size(10.dp).padding(top = 1.dp))
                        Text(clientAddress, fontSize = 9.sp, color = Slate600, lineHeight = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                Text(if (isProfessional) "CONSULTA / SERVICIO" else "TRABAJO / PROYECTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Text(tituloTrabajo.ifBlank { if (isProfessional) "Servicio profesional" else "Proyecto de servicio" }, fontSize = 11.sp, color = Slate800, lineHeight = 14.sp)
            }
        }
        HorizontalDivider(color = Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun A4ItemsTable(items: List<PresupuestoItemDisplay>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.border(1.dp, Slate300)) {
            // Header
            Row(modifier = Modifier.background(Slate100).height(IntrinsicSize.Min)) {
                A4TableCell("Cant", 0.15f, true)
                A4TableCell("Descripción", 0.55f, true)
                A4TableCell("Unitario", 0.3f, true, alignRight = true)
                A4TableCell("Total", 0.3f, true, alignRight = true, isLast = true)
            }
            HorizontalDivider(color = Slate300)

            // Items
            items.forEach { item ->
                val bg = if (item.isSpecial) Color(0xFFEFF6FF) else Color.White
                val color = if (item.isSpecial) MaverickBlueEnd else Slate800
                val weight = if (item.isSpecial) FontWeight.Bold else FontWeight.Normal

                Row(modifier = Modifier.background(bg).height(IntrinsicSize.Min)) {
                    A4TableCell(if(item.isSpecial) "-" else item.cantidad, 0.15f, color = Slate600)
                    A4TableCell(item.descripcion, 0.55f, color = color, fontWeight = weight)
                    A4TableCell(item.unitario, 0.3f, alignRight = true, color = Slate600)
                    A4TableCell(item.total, 0.3f, alignRight = true, fontWeight = FontWeight.Bold, color = color, isLast = true)
                }
                HorizontalDivider(color = Slate300)
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
    val finalColor = if (color == Color.Unspecified) (if (isHeader) Slate600 else Slate800) else color
    val finalWeight = fontWeight ?: (if (isHeader) FontWeight.Bold else FontWeight.Normal)

    Box(
        modifier = Modifier
            .weight(weight)
            .fillMaxHeight()
            .then(if (!isLast) Modifier.border(width = 0.5.dp, color = Slate300.copy(alpha = 0.5f)) else Modifier)
            .padding(6.dp),
        contentAlignment = if (alignRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 9.sp else 10.sp,
            fontWeight = finalWeight,
            color = finalColor,
            textAlign = if (alignRight) TextAlign.End else TextAlign.Start,
            lineHeight = if (isHeader) 11.sp else 12.sp
        )
    }
}

@Composable
fun A4FooterSection(
    subtotal: Double,
    taxAmount: Double,
    discountAmount: Double,
    total: Double,
    taxes: List<BudgetTax> = emptyList(),
    validezDias: Int = 15,
    notaLegal: String = "",
    currencySymbol: String = "$"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate50)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Nota Legal
            Text(
                text = if (notaLegal.isNotBlank()) notaLegal
                       else "Nota: Los precios están expresados en Pesos Argentinos.\nVálido por $validezDias días.",
                fontSize = 10.sp,
                color = Slate400,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 14.sp,
                modifier = Modifier.width(180.dp)
            )

            // Cuadro de Totales
            Column(
                modifier = Modifier
                    .width(220.dp)
                    .shadow(2.dp, RoundedCornerShape(4.dp))
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, Slate300, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal:", fontSize = 11.sp, color = Slate600)
                    Text("$currencySymbol ${String.format("%,.2f", subtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                }

                if (taxAmount > 0) {
                    if (taxes.isNotEmpty()) {
                        taxes.forEach { tax ->
                            val pct = if (subtotal > 0) tax.amount / subtotal * 100 else 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${tax.description} (${String.format("%.1f", pct)}%)",
                                    fontSize = 10.sp,
                                    color = Slate600,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "+ $currencySymbol ${String.format("%,.2f", tax.amount)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Impuestos:", fontSize = 11.sp, color = Slate600)
                            Text("$currencySymbol ${String.format("%,.2f", taxAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }
                    }
                }

                if (discountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Descuento:", fontSize = 11.sp, color = Slate600)
                        Text("- $currencySymbol ${String.format("%,.2f", discountAmount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate800)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Slate200)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Slate800)
                    Text("$currencySymbol ${String.format("%,.2f", total)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaverickBlueEnd)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 460, heightDp = 680, name = "Vista Previa Presupuesto A4")
@Composable
private fun PreviewBudgetDialog() {
    val prestadorEjemplo = Provider(
        uid = "preview",
        email = "juan@ejemplo.com",
        phoneNumber = "1122334455",
        displayName = "Juan Pérez",
        name = "Juan",
        lastName = "Pérez",
        categories = listOf("Gasista"),
        hasPhysicalLocation = true,
        createdAt = 0L
    )
    val items = listOf(
        PresupuestoItemDisplay("2", "Mano de obra", "$ 5.000", "$ 10.000"),
        PresupuestoItemDisplay("1", "Materiales varios", "$ 8.000", "$ 8.000")
    )
    Column(
        modifier = Modifier
            .width(A4_WIDTH)
            .height(A4_HEIGHT)
            .background(Color.White)
            .border(1.dp, Slate300)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(MaverickGradient))
        A4HeaderSection(prestadorEjemplo, "Juan Pérez", "P-001", false)
        HorizontalDivider(color = Slate200)
        A4ClientInfoSection(
            prestador = prestadorEjemplo,
            clientName = "Franco García",
            clientCompany = null,
            clientAddress = "Av. Corrientes 1234, CABA",
            providerName = "Juan Pérez",
            providerAddress = "Av. Rivadavia 500, CABA",
            isProfessional = false,
            tituloTrabajo = "Instalación de gas",
            category = "Gasista"
        )
        A4ItemsTable(items)
        Spacer(modifier = Modifier.weight(1f))
        A4FooterSection(subtotal = 18000.0, taxAmount = 0.0, discountAmount = 0.0, total = 18000.0, taxes = emptyList())
    }
}
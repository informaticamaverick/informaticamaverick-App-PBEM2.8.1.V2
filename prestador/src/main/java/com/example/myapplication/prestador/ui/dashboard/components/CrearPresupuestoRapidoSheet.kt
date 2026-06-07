package com.example.myapplication.prestador.ui.dashboard.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.presupuesto.*
import com.example.myapplication.prestador.ui.presupuesto.components.ArticleAutoCompleteFields
import com.example.myapplication.prestador.ui.presupuesto.components.DescriptionAutoCompleteField
import com.example.myapplication.prestador.ui.presupuesto.components.FeeAutoCompleteFields
import com.example.myapplication.prestador.ui.presupuesto.components.ServiceAutoCompleteFields
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoConfigViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoRapidoSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val editProfileViewModel: EditProfileViewModel = hiltViewModel()
    val profileState by editProfileViewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider

    val configViewModel: PresupuestoConfigViewModel = hiltViewModel()
    val config by configViewModel.config.collectAsState()

    val presupuestoViewModel: PresupuestoViewModel = hiltViewModel()
    val articleCatalog by presupuestoViewModel.articleCatalog.collectAsState()
    val serviceCatalog by presupuestoViewModel.serviceCatalog.collectAsState()
    val feeCatalog by presupuestoViewModel.feeCatalog.collectAsState()
    val presupuestos by presupuestoViewModel.presupuestos.collectAsState()

    // Catálogo parseado (mismo formato que CrearPresupuestoPrestadorScreen)
    val savedArticleItems = remember(articleCatalog) {
        val json = articleCatalog?.itemsJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 4) BudgetItem(
                id = 0L, code = parts[0], description = parts[1],
                quantity = parts[2].toIntOrNull() ?: 1,
                unitPrice = parts[3].toDoubleOrNull() ?: 0.0,
                taxPercentage = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                discountPercentage = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.description }
    }
    val savedServiceItems = remember(serviceCatalog) {
        val json = serviceCatalog?.serviciosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) BudgetService(id = 0L, code = parts[0], description = parts[1], total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
        }.distinctBy { it.description }
    }
    val savedFeeItems = remember(feeCatalog) {
        val json = feeCatalog?.honorariosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) BudgetProfessionalFee(id = 0L, code = parts[0], description = parts[1], total = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
        }.distinctBy { it.description }
    }
    val suggMisc = remember(presupuestos) {
        presupuestos.flatMap { p ->
            if (p.gastosJson.isBlank()) emptyList()
            else p.gastosJson.split("|").mapNotNull { s ->
                val parts = s.split(";")
                if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0] else null
            }
        }.distinct()
    }

    // Campos principales
    var clienteNombre by remember { mutableStateOf("") }
    var tituloTrabajo by remember { mutableStateOf("") }
    var selectedCategory by remember(provider) {
        mutableStateOf(provider?.categories?.firstOrNull() ?: "")
    }
    var categoryExpanded by remember { mutableStateOf(false) }
    var validezDias by remember(config.validezDias) {
        mutableStateOf(config.validezDias.toString())
    }

    // Secciones
    val articulos = remember { mutableStateListOf<BudgetItem>() }
    val servicios = remember { mutableStateListOf<BudgetService>() }
    val honorarios = remember { mutableStateListOf<BudgetProfessionalFee>() }
    val gastosVarios = remember { mutableStateListOf<BudgetMiscExpense>() }
    val impuestos = remember { mutableStateListOf<BudgetTax>() }

    // Expandidos
    var articulosExpanded by remember { mutableStateOf(true) }
    var serviciosExpanded by remember { mutableStateOf(false) }
    var honorariosExpanded by remember { mutableStateOf(false) }
    var gastosExpanded by remember { mutableStateOf(false) }
    var impuestosExpanded by remember { mutableStateOf(false) }

    // Totales
    val itemsSubtotal = articulos.sumOf { it.unitPrice * it.quantity }
    val servicesSubtotal = servicios.sumOf { it.total }
    val feesSubtotal = honorarios.sumOf { it.total }
    val miscSubtotal = gastosVarios.sumOf { it.amount }
    val taxesSubtotal = impuestos.sumOf { it.amount }
    val subtotal = itemsSubtotal + servicesSubtotal + feesSubtotal + miscSubtotal
    val grandTotal = subtotal + taxesSubtotal

    val currencySymbol = if (config.moneda == "USD") "US$" else "$"
    val accent = Color(0xFFF97316)
    val providerCategories = provider?.categories ?: emptyList()

    var mostrarPreview by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val hasContent = clienteNombre.isNotBlank() &&
            (articulos.any { it.description.isNotBlank() } ||
             servicios.any { it.description.isNotBlank() } ||
             honorarios.any { it.description.isNotBlank() } ||
             gastosVarios.any { it.description.isNotBlank() })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .verticalScroll(rememberScrollState())
        ) {
            // Header dinámico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(accent.copy(alpha = 0.15f), Color.Transparent))
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Description, null, tint = accent, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Presupuesto para",
                            fontSize = 11.sp,
                            color = accent,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = clienteNombre.ifBlank { "—" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila: Categoría + Válido por
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Categoría dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.ifBlank { "Categoría" },
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Category, null, tint = accent, modifier = Modifier.size(18.dp))
                                },
                                trailingIcon = {
                                    Icon(
                                        if (categoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accent,
                                        focusedLabelColor = accent,
                                        cursorColor = accent
                                    )
                                )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                providerCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                cat,
                                                fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal,
                                                color = if (cat == selectedCategory) accent else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = { selectedCategory = cat; categoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Válido por X días
                    OutlinedTextField(
                        value = validezDias,
                        onValueChange = { validezDias = it.filter { c -> c.isDigit() } },
                        label = { Text("Válido por", fontSize = 11.sp) },
                        suffix = { Text("días", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            focusedLabelColor = accent,
                            cursorColor = accent
                        )
                    )
                }

                // Cliente
                OutlinedTextField(
                    value = clienteNombre,
                    onValueChange = { clienteNombre = it },
                    label = { Text("Nombre del cliente") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = accent) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent
                    )
                )

                // Título del trabajo
                OutlinedTextField(
                    value = tituloTrabajo,
                    onValueChange = { tituloTrabajo = it },
                    label = { Text("Nombre del trabajo / proyecto") },
                    leadingIcon = { Icon(Icons.Default.Work, null, tint = accent) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent
                    )
                )

                // ── Artículos ──
                PresupuestoRapidoSection(
                    title = "Artículos",
                    icon = Icons.Default.ShoppingCart,
                    count = articulos.size,
                    expanded = articulosExpanded,
                    onToggle = { articulosExpanded = !articulosExpanded },
                    onAdd = { articulos.add(BudgetItem()); articulosExpanded = true },
                    accent = accent
                ) {
                    ArticleAutoCompleteFields(
                        suggestions = savedArticleItems,
                        onAdd = { articulos.add(it); articulosExpanded = true },
                        items = articulos.toList(),
                        onEdit = { updated ->
                            val i = articulos.indexOfFirst { it.id == updated.id }
                            if (i != -1) articulos[i] = updated
                        },
                        onDelete = { articulos.removeAt(it) }
                    )
                }

                // ── Mano de Obra / Servicios ──
                PresupuestoRapidoSection(
                    title = "Mano de Obra / Servicios",
                    icon = Icons.Default.Build,
                    count = servicios.size,
                    expanded = serviciosExpanded,
                    onToggle = { serviciosExpanded = !serviciosExpanded },
                    onAdd = { servicios.add(BudgetService()); serviciosExpanded = true },
                    accent = accent
                ) {
                    ServiceAutoCompleteFields(
                        suggestions = savedServiceItems,
                        onAdd = { servicios.add(it); serviciosExpanded = true },
                        items = servicios.toList(),
                        onEdit = { updated ->
                            val i = servicios.indexOfFirst { it.id == updated.id }
                            if (i != -1) servicios[i] = updated
                        },
                        onDelete = { servicios.removeAt(it) }
                    )
                }

                // ── Honorarios del Profesional ──
                PresupuestoRapidoSection(
                    title = "Honorarios del Profesional",
                    icon = Icons.Default.Work,
                    count = honorarios.size,
                    expanded = honorariosExpanded,
                    onToggle = { honorariosExpanded = !honorariosExpanded },
                    onAdd = { honorarios.add(BudgetProfessionalFee()); honorariosExpanded = true },
                    accent = accent
                ) {
                    FeeAutoCompleteFields(
                        suggestions = savedFeeItems,
                        onAdd = { honorarios.add(it); honorariosExpanded = true },
                        items = honorarios.toList(),
                        onEdit = { updated ->
                            val i = honorarios.indexOfFirst { it.id == updated.id }
                            if (i != -1) honorarios[i] = updated
                        },
                        onDelete = { honorarios.removeAt(it) }
                    )
                }

                // ── Gastos Varios ──
                PresupuestoRapidoSection(
                    title = "Gastos Varios",
                    icon = Icons.Default.Receipt,
                    count = gastosVarios.size,
                    expanded = gastosExpanded,
                    onToggle = { gastosExpanded = !gastosExpanded },
                    onAdd = { gastosVarios.add(BudgetMiscExpense()); gastosExpanded = true },
                    accent = accent
                ) {
                    DescriptionAutoCompleteField(
                        label = "Buscar gasto...",
                        suggestions = suggMisc,
                        onSelect = { desc ->
                            val prevAmount = presupuestos.flatMap { p ->
                                if (p.gastosJson.isBlank()) emptyList()
                                else p.gastosJson.split("|").mapNotNull { s ->
                                    val parts = s.split(";")
                                    if (parts.getOrNull(0) == desc) parts.getOrNull(1)?.toDoubleOrNull() else null
                                }
                            }.firstOrNull() ?: 0.0
                            gastosVarios.add(BudgetMiscExpense(description = desc, amount = prevAmount))
                            gastosExpanded = true
                        }
                    )
                    gastosVarios.forEachIndexed { i, item ->
                        SimpleLineItemRow(
                            description = item.description,
                            total = item.amount,
                            currencySymbol = currencySymbol,
                            onDescChange = { gastosVarios[i] = item.copy(description = it) },
                            onTotalChange = { gastosVarios[i] = item.copy(amount = it) },
                            onRemove = { gastosVarios.removeAt(i) }
                        )
                    }
                }

                // ── Impuestos ──
                PresupuestoRapidoSection(
                    title = "Impuestos",
                    icon = Icons.Default.AccountBalance,
                    count = impuestos.size,
                    expanded = impuestosExpanded,
                    onToggle = { impuestosExpanded = !impuestosExpanded },
                    onAdd = { impuestos.add(BudgetTax()); impuestosExpanded = true },
                    accent = accent
                ) {
                    impuestos.forEachIndexed { i, item ->
                        SimpleLineItemRow(
                            description = item.description,
                            total = item.amount,
                            currencySymbol = currencySymbol,
                            onDescChange = { impuestos[i] = item.copy(description = it) },
                            onTotalChange = { impuestos[i] = item.copy(amount = it) },
                            onRemove = { impuestos.removeAt(i) }
                        )
                    }
                }

                // Total card
                Spacer(Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "$currencySymbol ${String.format("%,.2f", grandTotal)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = accent
                        )
                    }
                }

                // Botón Vista Previa con degradado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (provider != null && hasContent)
                                Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1)))
                        )
                        .then(
                            if (provider != null && hasContent)
                                Modifier.clickable { mostrarPreview = true }
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Vista Previa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    // Preview + PDF compartir
    if (mostrarPreview && provider != null) {
        val providerName = when {
            provider.companies.isNotEmpty() && provider.companies.first().name.isNotBlank() -> provider.companies.first().name
            else -> "${provider.name.orEmpty()} ${provider.lastName.orEmpty()}".trim()
        }
        BudgetPreviewPDFDialog(
            prestador = provider,
            items = articulos.toList(),
            services = servicios.toList(),
            professionalFees = honorarios.toList(),
            miscExpenses = gastosVarios.toList(),
            taxes = impuestos.toList(),
            grandTotal = grandTotal,
            subtotal = subtotal,
            taxAmount = taxesSubtotal,
            discountAmount = 0.0,
            onDismiss = { mostrarPreview = false },
            onEnviar = { mostrarPreview = false; onDismiss() },
            clientName = clienteNombre,
            providerName = providerName,
            tituloTrabajo = tituloTrabajo,
            category = selectedCategory,
            validezDias = validezDias.toIntOrNull() ?: config.validezDias,
            showSendButton = false,
            onCompartirPDF = { _ ->
                // Generar PDF desde datos (no desde captura de UI — más confiable)
                generarYCompartirPDF(
                    context = context,
                    clienteNombre = clienteNombre,
                    providerName = providerName,
                    tituloTrabajo = tituloTrabajo,
                    categoria = selectedCategory,
                    validezDias = validezDias.toIntOrNull() ?: config.validezDias,
                    articulos = articulos.toList(),
                    servicios = servicios.toList(),
                    honorarios = honorarios.toList(),
                    gastosVarios = gastosVarios.toList(),
                    impuestos = impuestos.toList(),
                    grandTotal = grandTotal,
                    currencySymbol = currencySymbol
                )
            }
        )
    }
}

// ── Sección colapsable reutilizable ──
@Composable
private fun PresupuestoRapidoSection(
    title: String,
    icon: ImageVector,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (count > 0) accent.copy(alpha = 0.6f) else Color.Transparent
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (count > 0) accent.copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Borde izquierdo activo
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header de sección
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggle)
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de sección
                    Icon(
                        icon,
                        null,
                        tint = if (count > 0) accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Badge con conteo
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$count",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    // Botón +
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(accent)
                            .clickable(onClick = onAdd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Contenido expandible
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 12.dp)
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        content = content
                    )
                }
            }
        }
    }
}

// ── Row de Artículo (descripción + cantidad + precio) ──
@Composable
private fun PresupuestoRapidoItemRow(
    item: BudgetItem,
    currencySymbol: String,
    onUpdate: (BudgetItem) -> Unit,
    onRemove: () -> Unit
) {
    val accent = Color(0xFFF97316)
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Artículo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                IconButton(onClick = onRemove, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                }
            }
            OutlinedTextField(
                value = item.description,
                onValueChange = { onUpdate(item.copy(description = it)) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cantidad
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(0.4f)) {
                    IconButton(
                        onClick = { if (item.quantity > 1) onUpdate(item.copy(quantity = item.quantity - 1)) },
                        modifier = Modifier.size(32.dp)
                    ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp)) }
                    Text("${item.quantity}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { onUpdate(item.copy(quantity = item.quantity + 1)) },
                        modifier = Modifier.size(32.dp)
                    ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
                }
                // Precio
                OutlinedTextField(
                    value = if (item.unitPrice == 0.0) "" else item.unitPrice.toString(),
                    onValueChange = { onUpdate(item.copy(unitPrice = it.toDoubleOrNull() ?: 0.0)) },
                    label = { Text("Precio $currencySymbol") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent
                    )
                )
            }
            val itemTotal = item.unitPrice * item.quantity
            if (itemTotal > 0) {
                Text(
                    "Subtotal: $currencySymbol ${String.format("%,.2f", itemTotal)}",
                    fontSize = 11.sp, color = accent, fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ── Row simple para Servicios / Honorarios / Gastos / Impuestos ──
@Composable
private fun SimpleLineItemRow(
    description: String,
    total: Double,
    currencySymbol: String,
    onDescChange: (String) -> Unit,
    onTotalChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val accent = Color(0xFFF97316)
            OutlinedTextField(
                value = description,
                onValueChange = onDescChange,
                label = { Text("Descripción") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent
                )
            )
            OutlinedTextField(
                value = if (total == 0.0) "" else total.toString(),
                onValueChange = { onTotalChange(it.toDoubleOrNull() ?: 0.0) },
                label = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(100.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent
                )
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun generarYCompartirPDF(
    context: Context,
    clienteNombre: String,
    providerName: String,
    tituloTrabajo: String,
    categoria: String,
    validezDias: Int,
    articulos: List<BudgetItem>,
    servicios: List<BudgetService>,
    honorarios: List<BudgetProfessionalFee>,
    gastosVarios: List<BudgetMiscExpense>,
    impuestos: List<BudgetTax>,
    grandTotal: Double,
    currencySymbol: String
) {
    try {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val colRight = pageWidth - margin
        val orange = AndroidColor.parseColor("#F97316")
        val darkGray = AndroidColor.parseColor("#1F2937")
        val medGray = AndroidColor.parseColor("#6B7280")
        val lightGray = AndroidColor.parseColor("#E5E7EB")

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        fun paintText(color: Int = darkGray, size: Float = 11f, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) = Paint().apply {
            this.color = color
            textSize = size
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = align
            isAntiAlias = true
        }

        fun paintFill(color: Int) = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        var y = 0f

        // Header banner
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 72f, paintFill(orange))
        canvas.drawText(providerName, margin, 32f, paintText(AndroidColor.WHITE, 18f, bold = true))
        canvas.drawText("PRESUPUESTO", margin, 56f, paintText(AndroidColor.WHITE, 11f))
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        canvas.drawText(dateStr, colRight, 44f, paintText(AndroidColor.WHITE, 11f, align = Paint.Align.RIGHT))
        y = 88f

        // Client + job info
        canvas.drawText("Cliente:", margin, y, paintText(medGray, 10f))
        canvas.drawText(clienteNombre.ifBlank { "—" }, margin + 48f, y, paintText(darkGray, 10f, bold = true))
        y += 18f
        canvas.drawText("Trabajo:", margin, y, paintText(medGray, 10f))
        canvas.drawText(tituloTrabajo.ifBlank { "—" }, margin + 48f, y, paintText(darkGray, 10f))
        y += 18f
        canvas.drawText("Categoría:", margin, y, paintText(medGray, 10f))
        canvas.drawText(categoria.ifBlank { "—" }, margin + 56f, y, paintText(darkGray, 10f))
        canvas.drawText("Validez: $validezDias días", colRight, y, paintText(medGray, 10f, align = Paint.Align.RIGHT))
        y += 20f

        // Divider
        canvas.drawRect(margin, y, colRight, y + 1f, paintFill(lightGray))
        y += 14f

        // Helper: draw a section
        fun drawSection(title: String, rows: List<Pair<String, String>>) {
            if (rows.isEmpty()) return
            // Section title
            canvas.drawRect(margin, y, colRight, y + 20f, paintFill(0xFFFFF7ED.toInt()))
            canvas.drawRect(margin, y, margin + 3f, y + 20f, paintFill(orange))
            canvas.drawText(title.uppercase(), margin + 10f, y + 14f, paintText(orange, 10f, bold = true))
            var localY = y + 24f
            rows.forEach { (desc, amount) ->
                canvas.drawText(desc.take(70), margin + 6f, localY, paintText(darkGray, 9.5f))
                canvas.drawText(amount, colRight, localY, paintText(darkGray, 9.5f, align = Paint.Align.RIGHT))
                localY += 16f
                canvas.drawRect(margin + 6f, localY - 2f, colRight, localY - 1f, paintFill(lightGray))
            }
            // Update outer y — we mutate via captured reference trick with array
            @Suppress("UNUSED_EXPRESSION")
            localY
            // We cannot assign to captured `y` directly; use a mutable cell approach below
        }

        // Artículos
        if (articulos.isNotEmpty()) {
            canvas.drawRect(margin, y, colRight, y + 20f, paintFill(0xFFFFF7ED.toInt()))
            canvas.drawRect(margin, y, margin + 3f, y + 20f, paintFill(orange))
            canvas.drawText("ARTÍCULOS", margin + 10f, y + 14f, paintText(orange, 10f, bold = true))
            y += 24f
            articulos.forEach { item ->
                val subtotal = item.unitPrice * item.quantity
                val label = "${item.description.take(50)} (x${item.quantity} × $currencySymbol${String.format("%.2f", item.unitPrice)})"
                canvas.drawText(label, margin + 6f, y, paintText(darkGray, 9.5f))
                canvas.drawText("$currencySymbol${String.format("%.2f", subtotal)}", colRight, y, paintText(darkGray, 9.5f, align = Paint.Align.RIGHT))
                y += 16f
                canvas.drawRect(margin + 6f, y - 2f, colRight, y - 1f, paintFill(lightGray))
            }
            y += 6f
        }

        // Servicios
        if (servicios.isNotEmpty()) {
            canvas.drawRect(margin, y, colRight, y + 20f, paintFill(0xFFFFF7ED.toInt()))
            canvas.drawRect(margin, y, margin + 3f, y + 20f, paintFill(orange))
            canvas.drawText("SERVICIOS", margin + 10f, y + 14f, paintText(orange, 10f, bold = true))
            y += 24f
            servicios.forEach { svc ->
                canvas.drawText(svc.description.take(65), margin + 6f, y, paintText(darkGray, 9.5f))
                canvas.drawText("$currencySymbol${String.format("%.2f", svc.total)}", colRight, y, paintText(darkGray, 9.5f, align = Paint.Align.RIGHT))
                y += 16f
                canvas.drawRect(margin + 6f, y - 2f, colRight, y - 1f, paintFill(lightGray))
            }
            y += 6f
        }

        // Honorarios
        if (honorarios.isNotEmpty()) {
            canvas.drawRect(margin, y, colRight, y + 20f, paintFill(0xFFFFF7ED.toInt()))
            canvas.drawRect(margin, y, margin + 3f, y + 20f, paintFill(orange))
            canvas.drawText("HONORARIOS", margin + 10f, y + 14f, paintText(orange, 10f, bold = true))
            y += 24f
            honorarios.forEach { fee ->
                canvas.drawText(fee.description.take(65), margin + 6f, y, paintText(darkGray, 9.5f))
                canvas.drawText("$currencySymbol${String.format("%.2f", fee.total)}", colRight, y, paintText(darkGray, 9.5f, align = Paint.Align.RIGHT))
                y += 16f
                canvas.drawRect(margin + 6f, y - 2f, colRight, y - 1f, paintFill(lightGray))
            }
            y += 6f
        }

        // Gastos Varios
        if (gastosVarios.isNotEmpty()) {
            canvas.drawRect(margin, y, colRight, y + 20f, paintFill(0xFFFFF7ED.toInt()))
            canvas.drawRect(margin, y, margin + 3f, y + 20f, paintFill(orange))
            canvas.drawText("GASTOS VARIOS", margin + 10f, y + 14f, paintText(orange, 10f, bold = true))
            y += 24f
            gastosVarios.forEach { gasto ->
                canvas.drawText(gasto.description.take(65), margin + 6f, y, paintText(darkGray, 9.5f))
                canvas.drawText("$currencySymbol${String.format("%.2f", gasto.amount)}", colRight, y, paintText(darkGray, 9.5f, align = Paint.Align.RIGHT))
                y += 16f
                canvas.drawRect(margin + 6f, y - 2f, colRight, y - 1f, paintFill(lightGray))
            }
            y += 6f
        }

        // Impuestos
        if (impuestos.isNotEmpty()) {
            canvas.drawRect(margin, y, colRight, y + 20f, paintFill(0xFFFFF7ED.toInt()))
            canvas.drawRect(margin, y, margin + 3f, y + 20f, paintFill(orange))
            canvas.drawText("IMPUESTOS / TASAS", margin + 10f, y + 14f, paintText(orange, 10f, bold = true))
            y += 24f
            impuestos.forEach { tax ->
                canvas.drawText(tax.description.take(65), margin + 6f, y, paintText(darkGray, 9.5f))
                canvas.drawText("$currencySymbol${String.format("%.2f", tax.amount)}", colRight, y, paintText(darkGray, 9.5f, align = Paint.Align.RIGHT))
                y += 16f
                canvas.drawRect(margin + 6f, y - 2f, colRight, y - 1f, paintFill(lightGray))
            }
            y += 6f
        }

        // Total banner
        y += 8f
        canvas.drawRect(margin, y, colRight, y + 36f, paintFill(orange))
        canvas.drawText("TOTAL", margin + 12f, y + 24f, paintText(AndroidColor.WHITE, 14f, bold = true))
        canvas.drawText("$currencySymbol${String.format("%.2f", grandTotal)}", colRight - 12f, y + 24f, paintText(AndroidColor.WHITE, 16f, bold = true, align = Paint.Align.RIGHT))
        y += 48f

        // Footer
        canvas.drawText("Documento generado por PBEM", pageWidth / 2f, pageHeight - 20f, paintText(medGray, 9f, align = Paint.Align.CENTER))

        pdfDoc.finishPage(page)

        val dir = File(context.cacheDir, "presupuestos").also { it.mkdirs() }
        val file = File(dir, "presupuesto_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Presupuesto - $clienteNombre")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Compartir presupuesto").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al generar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

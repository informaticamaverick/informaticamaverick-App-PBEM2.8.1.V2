package com.example.myapplication.prestador.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
private val Accent = Color(0xFFF97316)
private val AccentDark = Color(0xFFEA580C)
private val BgSheet = Color(0xFFFAFAFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarCatalogoSheet(onDismiss: () -> Unit) {
    val viewModel: PresupuestoViewModel = hiltViewModel()

    val articleCatalog by viewModel.articleCatalog.collectAsState()
    val serviceCatalog by viewModel.serviceCatalog.collectAsState()
    val feeCatalog     by viewModel.feeCatalog.collectAsState()

    val articulos = remember(articleCatalog) {
        val json = articleCatalog?.itemsJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val p = s.split(";")
            if (p.size >= 4) BudgetItem(
                code = p[0], description = p[1],
                quantity = p[2].toIntOrNull() ?: 1,
                unitPrice = p[3].toDoubleOrNull() ?: 0.0,
                taxPercentage = p.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                discountPercentage = p.getOrNull(5)?.toDoubleOrNull() ?: 0.0
            ) else null
        }
    }

    val servicios = remember(serviceCatalog) {
        val json = serviceCatalog?.serviciosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val p = s.split(";")
            if (p.size >= 3) BudgetService(code = p[0], description = p[1], total = p[2].toDoubleOrNull() ?: 0.0)
            else null
        }
    }

    val honorarios = remember(feeCatalog) {
        val json = feeCatalog?.honorariosJson ?: ""
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val p = s.split(";")
            if (p.size >= 3) BudgetProfessionalFee(code = p[0], description = p[1], total = p[2].toDoubleOrNull() ?: 0.0)
            else null
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Artículos", "Servicios", "Honorarios")

    // Estados de diálogo
    var showAddEditArticle   by remember { mutableStateOf(false) }
    var editingArticle       by remember { mutableStateOf<BudgetItem?>(null) }
    var showAddEditService   by remember { mutableStateOf(false) }
    var editingService       by remember { mutableStateOf<BudgetService?>(null) }
    var showAddEditFee       by remember { mutableStateOf(false) }
    var editingFee           by remember { mutableStateOf<BudgetProfessionalFee?>(null) }
    var showDeleteConfirm    by remember { mutableStateOf(false) }
    var deleteAction         by remember { mutableStateOf<(() -> Unit)?>(null) }
    var deleteLabel          by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgSheet,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Inventory2, null, tint = Accent, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Mi Catálogo", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    color = Color(0xFF1F2937), modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFF6B7280))
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Accent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Accent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp)
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Contenido del tab
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 420.dp)) {
                when (selectedTab) {
                    0 -> CatalogoTabContent(
                        items = articulos,
                        emptyText = "No hay artículos en el catálogo",
                        onAdd = { editingArticle = null; showAddEditArticle = true },
                        itemContent = { item ->
                            CatalogoItemRow(
                                title = item.description,
                                subtitle = "Código: ${item.code.ifBlank { "—" }}  •  Precio: $${String.format("%.2f", item.unitPrice)}  •  Cant: ${item.quantity}",
                                onEdit = { editingArticle = item; showAddEditArticle = true },
                                onDelete = {
                                    deleteLabel = item.description
                                    deleteAction = { viewModel.deleteArticleFromCatalog(item.description) }
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    )
                    1 -> CatalogoTabContent(
                        items = servicios,
                        emptyText = "No hay servicios en el catálogo",
                        onAdd = { editingService = null; showAddEditService = true },
                        itemContent = { svc ->
                            CatalogoItemRow(
                                title = svc.description,
                                subtitle = "Código: ${svc.code.ifBlank { "—" }}  •  Total: $${String.format("%.2f", svc.total)}",
                                onEdit = { editingService = svc; showAddEditService = true },
                                onDelete = {
                                    deleteLabel = svc.description
                                    deleteAction = { viewModel.deleteServiceFromCatalog(svc.description) }
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    )
                    2 -> CatalogoTabContent(
                        items = honorarios,
                        emptyText = "No hay honorarios en el catálogo",
                        onAdd = { editingFee = null; showAddEditFee = true },
                        itemContent = { fee ->
                            CatalogoItemRow(
                                title = fee.description,
                                subtitle = "Código: ${fee.code.ifBlank { "—" }}  •  Total: $${String.format("%.2f", fee.total)}",
                                onEdit = { editingFee = fee; showAddEditFee = true },
                                onDelete = {
                                    deleteLabel = fee.description
                                    deleteAction = { viewModel.deleteFeeFromCatalog(fee.description) }
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    // ── Diálogo Artículo ──────────────────────────────────────────────────────
    if (showAddEditArticle) {
        ArticuloCatalogoDialog(
            initial = editingArticle,
            onDismiss = { showAddEditArticle = false },
            onSave = { item ->
                if (editingArticle != null) {
                    viewModel.updateArticleInCatalog(editingArticle!!.description, item)
                } else {
                    viewModel.saveArticleToSuggestions(item)
                }
                showAddEditArticle = false
            }
        )
    }

    // ── Diálogo Servicio ──────────────────────────────────────────────────────
    if (showAddEditService) {
        ServicioCatalogoDialog(
            initial = editingService,
            onDismiss = { showAddEditService = false },
            onSave = { svc ->
                if (editingService != null) {
                    viewModel.updateServiceInCatalog(editingService!!.description, svc)
                } else {
                    viewModel.saveServiceToSuggestions(svc)
                }
                showAddEditService = false
            }
        )
    }

    // ── Diálogo Honorario ─────────────────────────────────────────────────────
    if (showAddEditFee) {
        HonorarioCatalogoDialog(
            initial = editingFee,
            onDismiss = { showAddEditFee = false },
            onSave = { fee ->
                if (editingFee != null) {
                    viewModel.updateFeeInCatalog(editingFee!!.description, fee)
                } else {
                    viewModel.saveProfessionalFeeToSuggestions(fee)
                }
                showAddEditFee = false
            }
        )
    }

    // ── Confirmación eliminar ─────────────────────────────────────────────────
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFEF4444)) },
            title = { Text("Eliminar item") },
            text = { Text("¿Eliminar \"$deleteLabel\" del catálogo?") },
            confirmButton = {
                TextButton(onClick = { deleteAction?.invoke(); showDeleteConfirm = false }) {
                    Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

// ─── Componente genérico de tab ───────────────────────────────────────────────
@Composable
private fun <T> CatalogoTabContent(
    items: List<T>,
    emptyText: String,
    onAdd: () -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Inbox, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text(emptyText, color = Color(0xFF9CA3AF), fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 72.dp, top = 4.dp)
            ) {
                items(items) { item -> itemContent(item) }
            }
        }

        // FAB agregar
        FloatingActionButton(
            onClick = onAdd,
            containerColor = Accent,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, "Agregar", modifier = Modifier.size(22.dp))
        }
    }
}

// ─── Fila de item del catálogo ────────────────────────────────────────────────
@Composable
private fun CatalogoItemRow(
    title: String,
    subtitle: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(Accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, null, tint = Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1F2937))
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF6B7280))
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, null, tint = Accent, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─── Diálogos de edición ──────────────────────────────────────────────────────

@Composable
private fun ArticuloCatalogoDialog(
    initial: BudgetItem?,
    onDismiss: () -> Unit,
    onSave: (BudgetItem) -> Unit
) {
    var codigo      by remember { mutableStateOf(initial?.code ?: "") }
    var descripcion by remember { mutableStateOf(initial?.description ?: "") }
    var precio      by remember { mutableStateOf(initial?.unitPrice?.toString() ?: "") }
    var cantidad    by remember { mutableStateOf(initial?.quantity?.toString() ?: "1") }
    var impuesto    by remember { mutableStateOf(initial?.taxPercentage?.toString() ?: "0") }
    var descuento   by remember { mutableStateOf(initial?.discountPercentage?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial != null) "Editar artículo" else "Nuevo artículo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CatalogoField("Código (opcional)", codigo, { codigo = it })
                CatalogoField("Descripción *", descripcion, { descripcion = it })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatalogoField("Precio unit.", precio, { precio = it }, KeyboardType.Decimal, Modifier.weight(1f))
                    CatalogoField("Cantidad", cantidad, { cantidad = it }, KeyboardType.Number, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatalogoField("% Impuesto", impuesto, { impuesto = it }, KeyboardType.Decimal, Modifier.weight(1f))
                    CatalogoField("% Descuento", descuento, { descuento = it }, KeyboardType.Decimal, Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descripcion.isNotBlank()) onSave(
                        BudgetItem(
                            code = codigo, description = descripcion,
                            unitPrice = precio.toDoubleOrNull() ?: 0.0,
                            quantity = cantidad.toIntOrNull() ?: 1,
                            taxPercentage = impuesto.toDoubleOrNull() ?: 0.0,
                            discountPercentage = descuento.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = descripcion.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ServicioCatalogoDialog(
    initial: BudgetService?,
    onDismiss: () -> Unit,
    onSave: (BudgetService) -> Unit
) {
    var codigo      by remember { mutableStateOf(initial?.code ?: "") }
    var descripcion by remember { mutableStateOf(initial?.description ?: "") }
    var total       by remember { mutableStateOf(initial?.total?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial != null) "Editar servicio" else "Nuevo servicio", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CatalogoField("Código (opcional)", codigo, { codigo = it })
                CatalogoField("Descripción *", descripcion, { descripcion = it })
                CatalogoField("Precio total", total, { total = it }, KeyboardType.Decimal)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descripcion.isNotBlank()) onSave(
                        BudgetService(code = codigo, description = descripcion, total = total.toDoubleOrNull() ?: 0.0)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = descripcion.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun HonorarioCatalogoDialog(
    initial: BudgetProfessionalFee?,
    onDismiss: () -> Unit,
    onSave: (BudgetProfessionalFee) -> Unit
) {
    var codigo      by remember { mutableStateOf(initial?.code ?: "") }
    var descripcion by remember { mutableStateOf(initial?.description ?: "") }
    var total       by remember { mutableStateOf(initial?.total?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial != null) "Editar honorario" else "Nuevo honorario", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CatalogoField("Código (opcional)", codigo, { codigo = it })
                CatalogoField("Descripción *", descripcion, { descripcion = it })
                CatalogoField("Total", total, { total = it }, KeyboardType.Decimal)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descripcion.isNotBlank()) onSave(
                        BudgetProfessionalFee(code = codigo, description = descripcion, total = total.toDoubleOrNull() ?: 0.0)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                enabled = descripcion.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun CatalogoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Accent,
            focusedLabelColor = Accent,
            cursorColor = Accent
        )
    )
}
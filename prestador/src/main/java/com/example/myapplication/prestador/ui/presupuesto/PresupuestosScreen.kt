package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myapplication.prestador.ui.presupuesto.BudgetPreviewPDFDialog
//import com.example.myapplication.prestador.data.PPrestadorProfileFalso
//import com.example.myapplication.prestador.utils.toPrestadorProfileFalso
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetTax
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import com.example.myapplication.prestador.ui.presupuesto.BudgetMiscExpense
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState


// Enums para estado del presupuesto
enum class PresupuestoEstado(val displayName: String, val color: Color) {
    PENDIENTE("Pendiente", Color(0xFFFFA726)),
    ACEPTADO("Aceptado", Color(0xFF66BB6A)),
    RECHAZADO("Rechazado", Color(0xFFEF5350)),
    ENVIADO("Enviado", Color(0xFF42A5F5))
}

// Data class para presupuesto
data class Presupuesto(
    val id: String,
    val numeroPresupuesto: String,
    val clienteNombre: String,
    val fecha: LocalDate,
    val monto: Double,
    val estado: PresupuestoEstado,
    val descripcion: String = ""
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreen(
    onBack: () -> Unit = {},
    onCrearNuevo: () -> Unit = {},
    onVerDetalle: (Presupuesto) -> Unit = {},
    onNavigateToConfig: () -> Unit = {},
    showTopBar: Boolean = true,
    viewModel: com.example.myapplication.prestador.viewmodel.presupuesto.PresupuestoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val colors = getPrestadorColors()
    val editProfileViewModel: EditProfileViewModel = hiltViewModel()
    val profileState by editProfileViewModel.profileState.collectAsState()
    val businessEntity by editProfileViewModel.businessEntity.collectAsState()
    val isProfessional = (profileState as? ProfileState.Success)?.provider?.serviceType
        .equals("PROFESSIONAL", ignoreCase = true)

    // Estado para el filtro
    var filtroEstado by remember { mutableStateOf<PresupuestoEstado?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var presupuestoSeleccionado by remember { mutableStateOf<Presupuesto?>(null) }
    var clienteParaPreview by remember { mutableStateOf<com.example.myapplication.prestador.data.local.entity.ClienteEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // DATOS REALES DE LA BD
    val presupuestosDB by viewModel.presupuestos.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    
    // SIMPLIFICADO: Sin cargar clientes por ahora
    // val clientesDB by viewModel.clientes.collectAsState()
    
    // Convertir PresupuestoEntity a Presupuesto para la UI
    // mapNotNull con try/catch por-item: un entity inválido no oculta toda la lista
    val presupuestos = presupuestosDB.mapNotNull { entity ->
        try {
            val fecha = if (entity.fecha.isNotBlank())
                LocalDate.parse(entity.fecha)
            else
                LocalDate.now()
            Presupuesto(
                id = entity.id,
                numeroPresupuesto = entity.numeroPresupuesto,
                clienteNombre = "Cliente",
                fecha = fecha,
                monto = entity.total,
                estado = when(entity.estado) {
                    "Aceptado" -> PresupuestoEstado.ACEPTADO
                    "Rechazado" -> PresupuestoEstado.RECHAZADO
                    "Enviado" -> PresupuestoEstado.ENVIADO
                    else -> PresupuestoEstado.PENDIENTE
                },
                descripcion = entity.notas.ifEmpty { "Sin descripción" }
            )
        } catch (e: Exception) {
            null // skip entidades con fecha inválida en lugar de vaciar toda la lista
        }
    }

    // Filtrar presupuestos
    val presupuestosFiltrados = if (filtroEstado != null) {
        presupuestos.filter { it.estado == filtroEstado }
    } else {
        presupuestos
    }

    // Diálogo de confirmación de borrado
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (isProfessional) "Eliminar consultas" else "Eliminar presupuestos") },
            text = { Text(if (isProfessional) "¿Eliminar ${selectedIds.size} consulta(s) seleccionada(s)?" else "¿Eliminar ${selectedIds.size} presupuesto(s) seleccionado(s)?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSelected(); showDeleteDialog = false }) {
                    Text("Eliminar", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        floatingActionButton = {
            if (!isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = onCrearNuevo,
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(if (isProfessional) "Nueva Consulta" else "Nuevo Presupuesto") }
                )
            }
        },
        bottomBar = {
            if (isSelectionMode) {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Eliminar ${selectedIds.size} seleccionado(s)", fontSize = 16.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.backgroundColor)
        ) {
            // ── HEADER estilo Inicio ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.primaryOrange,
                                colors.primaryOrange.copy(alpha = 0.85f)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 28.dp)
            ) {
                Column {
                    // Fila título + acciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (isSelectionMode) viewModel.clearSelection() else onBack() }) {
                                Icon(
                                    if (isSelectionMode) Icons.Default.Close else Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isSelectionMode) "${selectedIds.size} seleccionados" else if (isProfessional) "Consultas" else "Presupuestos",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Botón filtro
                        Row {
                            IconButton(onClick = onNavigateToConfig) {
                                Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                            }

                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = Color.White)
                            }
                            if (filtroEstado != null) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.White, CircleShape)
                                        .align(Alignment.TopEnd)
                                )
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = { filtroEstado = null; showFilterMenu = false },
                                    leadingIcon = { if (filtroEstado == null) Icon(Icons.Default.Check, null) }
                                )
                                HorizontalDivider()
                                PresupuestoEstado.values().forEach { estado ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    Modifier.size(12.dp).clip(CircleShape)
                                                        .background(estado.color)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(estado.displayName)
                                            }
                                        },
                                        onClick = { filtroEstado = estado; showFilterMenu = false },
                                        leadingIcon = {
                                            if (filtroEstado == estado) Icon(
                                                Icons.Default.Check,
                                                null
                                            )
                                        }
                                    )
                                }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Stats cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total presupuestos
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("TOTAL", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f))
                                Spacer(Modifier.height(4.dp))
                                Text("${presupuestos.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("presupuestos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        // Total facturado
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("FACTURADO", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "$ ${"%,.0f".format(presupuestos.sumOf { it.monto })}",
                                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                                Text("total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── CONTENIDO ────────────────────────────────────────────────
            if (presupuestosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Description, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = colors.textSecondary
                        )
                        Text(
                            if (filtroEstado != null) "No hay presupuestos ${filtroEstado?.displayName?.lowercase()}"
                            else "No hay presupuestos",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(presupuestosFiltrados) { presupuesto ->
                        PresupuestoCard(
                            presupuesto = presupuesto,
                            isSelectionMode = isSelectionMode,
                            isSelected = presupuesto.id in selectedIds,
                            onToggleSelect = { viewModel.toggleSelection(presupuesto.id) },
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleSelection(presupuesto.id)
                                } else {
                                    // Abrir vista previa al tocar la card
                                    presupuestoSeleccionado = presupuesto
                                    showPreviewDialog = true
                                }
                            },
                            onVerPreview = {
                                presupuestoSeleccionado = presupuesto
                                showPreviewDialog = true
                            },
                            onCambiarEstado = { nuevoEstado ->
                                viewModel.updateEstado(presupuesto.id, nuevoEstado)
                            },
                            onDelete = {
                                val entity = presupuestosDB.find { it.id == presupuesto.id }
                                if (entity != null) viewModel.deletePresupuesto(entity)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de vista previa
    LaunchedEffect(presupuestoSeleccionado?.id) {
        val entity = presupuestosDB.find { it.id == presupuestoSeleccionado?.id }
        val clienteId = entity?.clienteId
        clienteParaPreview = if (!clienteId.isNullOrBlank())
            viewModel.getClienteById(clienteId) else null
    }

    if (showPreviewDialog && presupuestoSeleccionado != null) {
        val entity = presupuestosDB.find { it.id == presupuestoSeleccionado!!.id }
        val prestador = (profileState as? ProfileState.Success)?.provider

        if (prestador != null && entity != null) {
            // Deserializar artículos
            val realItems = entity.itemsJson
                .takeIf { it.isNotBlank() }
                ?.split("|")
                ?.mapNotNull { s ->
                    val p = s.split(";")
                    if (p.size >= 4) BudgetItem(
                        code = p[0], description = p[1],
                        quantity = p[2].toIntOrNull() ?: 1,
                        unitPrice = p[3].toDoubleOrNull() ?: 0.0,
                        taxPercentage = p.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
                        discountPercentage = p.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                    ) else null
                } ?: emptyList()

            // Deserializar servicios / mano de obra
            val realServices = entity.serviciosJson
                .takeIf { it.isNotBlank() }
                ?.split("|")
                ?.mapNotNull { s ->
                    val p = s.split(";")
                    if (p.size >= 2) BudgetService(
                        code = p[0], description = p[1],
                        total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    ) else null
                } ?: emptyList()

            // Deserializar honorarios
            val realFees = entity.honorariosJson
                .takeIf { it.isNotBlank() }
                ?.split("|")
                ?.mapNotNull { s ->
                    val p = s.split(";")
                    if (p.size >= 2) BudgetProfessionalFee(
                        code = p[0], description = p[1],
                        total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                    ) else null
                } ?: emptyList()

            // Deserializar gastos varios
            val realMisc = entity.gastosJson
                .takeIf { it.isNotBlank() }
                ?.split("|")
                ?.mapNotNull { s ->
                    val p = s.split(";")
                    if (p.size >= 2) BudgetMiscExpense(
                        description = p[0],
                        amount = p[1].toDoubleOrNull() ?: 0.0
                    ) else null
                } ?: emptyList()

            // Deserializar impuestos
            val realTaxes = entity.impuestosJson
                .takeIf { it.isNotBlank() }
                ?.split("|")
                ?.mapNotNull { s ->
                    val p = s.split(";")
                    if (p.size >= 2) BudgetTax(
                        description = p[0],
                        amount = p[1].toDoubleOrNull() ?: 0.0
                    ) else null
                } ?: emptyList()

            val subtotalAmount = entity.subtotal
            val taxAmount = entity.impuestos

            BudgetPreviewPDFDialog(
                prestador = prestador,
                items = realItems,
                services = realServices,
                professionalFees = realFees,
                miscExpenses = realMisc,
                taxes = realTaxes,
                grandTotal = presupuestoSeleccionado!!.monto,
                subtotal = subtotalAmount,
                taxAmount = taxAmount,
                discountAmount = 0.0,
                onDismiss = { showPreviewDialog = false },
                onEnviar = { showPreviewDialog = false },
                clientName = clienteParaPreview?.nombre ?: "",
                clientAddress = clienteParaPreview?.direccion,
                providerName = prestador.displayName,
                providerAddress = prestador.direccionLocal ?: "",
                isProfessional = isProfessional,
                presupuestoNumero = presupuestoSeleccionado!!.numeroPresupuesto,
                tituloTrabajo = entity.tituloTrabajo,
                category = entity.categorias
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PresupuestoCard(
    presupuesto: Presupuesto,
    onClick: () -> Unit,
    onVerPreview: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onCambiarEstado: ((String) -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {}
){
    val colors = getPrestadorColors()
    var showEstadoMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSelect() else onClick() },
                onLongClick = { onToggleSelect() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colors.primaryOrange.copy(alpha = 0.12f) else colors.surfaceColor
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Franja naranja borde izquierdo (igual que inicio)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) Color(0xFFEF4444)
                        else presupuesto.estado.color,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Número y Estado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = presupuesto.numeroPresupuesto,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        // Estado chip — tap para cambiar estado
                        Box {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = presupuesto.estado.color.copy(alpha = 0.15f),
                                modifier = Modifier.clickable(enabled = onCambiarEstado != null && !isSelectionMode) {
                                    showEstadoMenu = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = presupuesto.estado.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = presupuesto.estado.color
                                    )
                                    if (onCambiarEstado != null && !isSelectionMode) {
                                        Icon(
                                            Icons.Default.ArrowDropDown, null,
                                            modifier = Modifier.size(14.dp),
                                            tint = presupuesto.estado.color
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showEstadoMenu,
                                onDismissRequest = { showEstadoMenu = false }
                            ) {
                                listOf("Pendiente", "Enviado", "Aceptado", "Rechazado").forEach { estado ->
                                    DropdownMenuItem(
                                        text = { Text(estado) },
                                        onClick = {
                                            onCambiarEstado?.invoke(estado)
                                            showEstadoMenu = false
                                        },
                                        leadingIcon = {
                                            val color = when(estado) {
                                                "Aceptado" -> PresupuestoEstado.ACEPTADO.color
                                                "Rechazado" -> PresupuestoEstado.RECHAZADO.color
                                                "Enviado" -> PresupuestoEstado.ENVIADO.color
                                                else -> PresupuestoEstado.PENDIENTE.color
                                            }
                                            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                                        }
                                    )
                                }
                            }
                        }
                    }
                    // Cliente
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(15.dp), tint = colors.textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(presupuesto.clienteNombre, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    // Footer: fecha + monto + botón preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(13.dp), tint = colors.textSecondary)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    presupuesto.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                "$ ${String.format("%,.2f", presupuesto.monto)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.primaryOrange
                            )
                        }
                        IconButton(onClick = onVerPreview) {
                            Icon(Icons.Default.Visibility, "Ver Vista Previa", tint = colors.primaryOrange)
                        }
                    }
                }
            }
        }
    }
}

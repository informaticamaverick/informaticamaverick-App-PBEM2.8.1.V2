package com.example.myapplication.prestador.ui.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.prestador.ui.presupuesto.BudgetPreviewPDFDialog
import com.example.myapplication.prestador.ui.presupuesto.BudgetItem
import com.example.myapplication.prestador.ui.presupuesto.BudgetTax
import com.example.myapplication.prestador.ui.presupuesto.BudgetService
import com.example.myapplication.prestador.ui.presupuesto.BudgetProfessionalFee
import com.example.myapplication.prestador.ui.presupuesto.BudgetMiscExpense
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState

enum class PresupuestoEstado(val displayName: String, val color: Color) {
    PENDIENTE("Pendiente", Color(0xFFFFA726)),
    ACEPTADO("Aceptado", Color(0xFF66BB6A)),
    RECHAZADO("Rechazado", Color(0xFFEF5350)),
    ENVIADO("Enviado", Color(0xFF42A5F5))
}

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
    val isProfessional = (profileState as? ProfileState.Success)?.provider?.serviceType
        .equals("PROFESSIONAL", ignoreCase = true)

    var filtroEstado by remember { mutableStateOf<PresupuestoEstado?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var presupuestoSeleccionado by remember { mutableStateOf<Presupuesto?>(null) }
    var clienteParaPreview by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val presupuestosDB by viewModel.presupuestos.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    val presupuestos = presupuestosDB.mapNotNull { entity ->
        try {
            val fecha = if (entity.fecha.isNotBlank()) LocalDate.parse(entity.fecha) else LocalDate.now()
            Presupuesto(
                id = entity.id,
                numeroPresupuesto = entity.numeroPresupuesto,
                clienteNombre = "Cliente",
                fecha = fecha,
                monto = entity.total,
                estado = when (entity.estado) {
                    "Aceptado" -> PresupuestoEstado.ACEPTADO
                    "Rechazado" -> PresupuestoEstado.RECHAZADO
                    "Enviado" -> PresupuestoEstado.ENVIADO
                    else -> PresupuestoEstado.PENDIENTE
                },
                descripcion = entity.notas.ifEmpty { "Sin descripcion" }
            )
        } catch (e: Exception) { null }
    }

    val presupuestosFiltrados = if (filtroEstado != null)
        presupuestos.filter { it.estado == filtroEstado }
    else presupuestos

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(if (isProfessional) "Eliminar consultas" else "Eliminar presupuestos") },
            text = { Text(if (isProfessional) "Eliminar ${selectedIds.size} consulta(s) seleccionada(s)?" else "Eliminar ${selectedIds.size} presupuesto(s) seleccionado(s)?") },
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
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Eliminar ${selectedIds.size} seleccionado(s)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = onCrearNuevo,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFFF7043), Color(0xFFFF9E80))),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                Text(
                                    if (isProfessional) "Nueva Consulta" else "Nuevo Presupuesto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
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
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFFFF7043),
                                0.45f to Color(0xFFFF9E80),
                                1.0f to Color(0xFFFFCCBC)
                            )
                        )
                    )
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 20.dp)
            ) {
                Column {
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
                                    tint = Color(0xFF3D1100)
                                )
                            }
                            Text(
                                text = if (isSelectionMode) "${selectedIds.size} seleccionados"
                                       else if (isProfessional) "Consultas" else "Presupuestos",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3D1100)
                            )
                        }
                        IconButton(onClick = onNavigateToConfig) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuracion", tint = Color(0xFF3D1100))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Stats pills
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.45f)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, null, tint = Color(0xFF5D2000), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${presupuestos.size} ${if (isProfessional) "consultas" else "presupuestos"}",
                                    fontSize = 11.sp, color = Color(0xFF5D2000), fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.45f)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Pending, null, tint = Color(0xFF5D2000), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${presupuestos.count { it.estado == PresupuestoEstado.PENDIENTE }} pendientes",
                                    fontSize = 11.sp, color = Color(0xFF5D2000), fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.45f)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF5D2000), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "$ ${"%,.0f".format(presupuestos.sumOf { it.monto })}",
                                    fontSize = 11.sp, color = Color(0xFF5D2000), fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // FILTROS HORIZONTALES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val todosSelected = filtroEstado == null
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (todosSelected) colors.primaryOrange else colors.surfaceColor,
                    modifier = Modifier.clickable { filtroEstado = null }
                ) {
                    Text(
                        "Todos",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (todosSelected) Color.White else colors.textSecondary
                    )
                }
                PresupuestoEstado.entries.forEach { estado ->
                    val selected = filtroEstado == estado
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) estado.color else colors.surfaceColor,
                        modifier = Modifier.clickable { filtroEstado = if (selected) null else estado }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!selected) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(estado.color))
                            }
                            Text(
                                estado.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selected) Color.White else colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // CONTENIDO
            if (presupuestosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(colors.primaryOrange.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Description, contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = colors.primaryOrange.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            if (filtroEstado != null)
                                "No hay ${if (isProfessional) "consultas" else "presupuestos"} ${filtroEstado?.displayName?.lowercase()}"
                            else
                                "Aun no tenes ${if (isProfessional) "consultas" else "presupuestos"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            if (isProfessional) "Crea tu primera consulta con el boton de abajo"
                            else "Crea tu primer presupuesto con el boton de abajo",
                            style = MaterialTheme.typography.bodyMedium,
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

    LaunchedEffect(presupuestoSeleccionado?.id) {
        val entity = presupuestosDB.find { it.id == presupuestoSeleccionado?.id }
        val clienteId = entity?.clienteId
        clienteParaPreview = if (!clienteId.isNullOrBlank()) viewModel.getClienteById(clienteId) else null
    }

    if (showPreviewDialog && presupuestoSeleccionado != null) {
        val entity = presupuestosDB.find { it.id == presupuestoSeleccionado!!.id }
        val prestador = (profileState as? ProfileState.Success)?.provider

        if (prestador != null && entity != null) {
            val realItems = entity.itemsJson.takeIf { it.isNotBlank() }?.split("|")?.mapNotNull { s ->
                val p = s.split(";")
                if (p.size >= 4) BudgetItem(code = p[0], description = p[1], quantity = p[2].toIntOrNull() ?: 1, unitPrice = p[3].toDoubleOrNull() ?: 0.0, taxPercentage = p.getOrNull(4)?.toDoubleOrNull() ?: 0.0, discountPercentage = p.getOrNull(5)?.toDoubleOrNull() ?: 0.0) else null
            } ?: emptyList()

            val realServices = entity.serviciosJson.takeIf { it.isNotBlank() }?.split("|")?.mapNotNull { s ->
                val p = s.split(";")
                if (p.size >= 2) BudgetService(code = p[0], description = p[1], total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
            } ?: emptyList()

            val realFees = entity.honorariosJson.takeIf { it.isNotBlank() }?.split("|")?.mapNotNull { s ->
                val p = s.split(";")
                if (p.size >= 2) BudgetProfessionalFee(code = p[0], description = p[1], total = p.getOrNull(2)?.toDoubleOrNull() ?: 0.0) else null
            } ?: emptyList()

            val realMisc = entity.gastosJson.takeIf { it.isNotBlank() }?.split("|")?.mapNotNull { s ->
                val p = s.split(";")
                if (p.size >= 2) BudgetMiscExpense(description = p[0], amount = p[1].toDoubleOrNull() ?: 0.0) else null
            } ?: emptyList()

            val realTaxes = entity.impuestosJson.takeIf { it.isNotBlank() }?.split("|")?.mapNotNull { s ->
                val p = s.split(";")
                if (p.size >= 2) BudgetTax(description = p[0], amount = p[1].toDoubleOrNull() ?: 0.0) else null
            } ?: emptyList()

            BudgetPreviewPDFDialog(
                prestador = prestador,
                items = realItems,
                services = realServices,
                professionalFees = realFees,
                miscExpenses = realMisc,
                taxes = realTaxes,
                grandTotal = presupuestoSeleccionado!!.monto,
                subtotal = entity.subtotal,
                taxAmount = entity.impuestos,
                discountAmount = 0.0,
                onDismiss = { showPreviewDialog = false },
                onEnviar = { showPreviewDialog = false },
                clientName = clienteParaPreview?.name ?: "",
                clientAddress = clienteParaPreview?.mainAddress?.fullString(),
                providerName = prestador.displayName,
                providerAddress = prestador.address?.fullString() ?: "",
                isProfessional = isProfessional,
                presupuestoNumero = presupuestoSeleccionado!!.numeroPresupuesto,
                tituloTrabajo = entity.tituloTrabajo,
                category = entity.categorias
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
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
) {
    val colors = getPrestadorColors()
    var showEstadoMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) Color(0xFFEF4444) else presupuesto.estado.color,
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
                                            val color = when (estado) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(15.dp), tint = colors.textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(presupuesto.clienteNombre, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
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

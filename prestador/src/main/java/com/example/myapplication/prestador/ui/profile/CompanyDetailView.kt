package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.BranchProvider
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.EmployeeProvider
import com.example.myapplication.prestador.ui.components.AddressBottomSheet
import com.example.myapplication.prestador.ui.components.AddressProviderCard
import com.example.myapplication.prestador.ui.components.BeActionsBar
import com.example.myapplication.prestador.ui.components.PrestadorAction
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.utils.errorCuitMensaje
import com.example.myapplication.prestador.utils.formatearCuit
import kotlinx.coroutines.delay
import java.util.*

@Composable
internal fun BranchSection(
    index: Int,
    branch: BranchProvider,
    colors: PrestadorColors,
    isEditMode: Boolean = false,
    onUpdateBranch: (BranchProvider) -> Unit = {},
    onNavigateToCalendarioConfig: (ownerId: String, ownerName: String) -> Unit = { _, _ -> }
) {
    val branchName = branch.name.ifBlank { if (index == 0) "Casa Central" else "Sucursal ${index + 1}" }
    var showAddressSheet by remember { mutableStateOf(false) }
    var showNuevoMiembroSheet by remember { mutableStateOf(false) }
    var miembroAEditar by remember { mutableStateOf<EmployeeProvider?>(null) }

    // Badge Sede
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (index == 0) Color(0xFF8B5CF6) else colors.textSecondary.copy(alpha = 0.6f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Icon(if (index == 0) Icons.Default.Home else Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(if (index == 0) "CASA CENTRAL" else "SUCURSAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
    Spacer(Modifier.height(8.dp))

    // Nombre sucursal + botón Horarios
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(branchName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            if (index == 0) {
                Text("Sede Principal", fontSize = 12.sp, color = Color(0xFF8B5CF6))
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNavigateToCalendarioConfig(branch.id, branchName) }
                .background(Color(0xFF8B5CF6).copy(alpha = 0.10f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
            Text("Config", fontSize = 9.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold)
        }
    }
    Spacer(Modifier.height(12.dp))

    // ── DIRECCIÓN ÚNICA (Ley SSOT)
    val context = LocalContext.current
    if (branch.address.fullString().isNotBlank()) {
        AddressProviderCard(
            address = branch.address,
            isEditMode = isEditMode,
            onEdit = { showAddressSheet = true },
            onDelete = { /* Las sucursales deben tener dirección */ },
            onOpenMaps = {
                val lat = branch.address.latitude
                val lng = branch.address.longitude
                if (lat != 0.0 || lng != 0.0) {
                    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            }
        )
    } else if (isEditMode) {
        OutlinedButton(
            onClick = { showAddressSheet = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.AddLocation, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Establecer Ubicación", fontSize = 13.sp)
        }
    }

    if (showAddressSheet) {
        AddressBottomSheet(
            initial = branch.address,
            onDismiss = { showAddressSheet = false },
            onSave = { newAddr -> onUpdateBranch(branch.copy(address = newAddr)); showAddressSheet = false }
        )
    }
    
    // Chips de servicios activos
    val activeServices = buildList<Pair<ImageVector, String>> {
        if (branch.doesService)          add(Icons.Default.Build         to "Servicios")
        if (branch.doesProduct)          add(Icons.Default.ShoppingBag   to "Productos")
        if (branch.acceptsAppointments)  add(Icons.Default.CalendarMonth  to "Turnos")
        if (branch.doesHomeVisits)       add(Icons.Default.DirectionsCar  to "A domicilio")
        if (branch.doesShipping)         add(Icons.Default.LocalShipping  to "Envíos")
        if (branch.works24h)             add(Icons.Default.Warning        to "24hs")
        if (branch.hasPhysicalLocation)  add(Icons.Default.Store          to "Local")
    }
    if (activeServices.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("SERVICIOS DE ESTA SEDE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            activeServices.forEach { (icon, label) ->
                AssistChip(onClick = {}, label = { Text(label, fontSize = 11.sp) }, leadingIcon = { Icon(icon, null, Modifier.size(12.dp), tint = Color(0xFF8B5CF6)) })
            }
        }
    }

    if (branch.team.isNotEmpty() || isEditMode) {
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("EQUIPO DE TRABAJO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, modifier = Modifier.weight(1f))
            if (isEditMode) {
                IconButton(onClick = { miembroAEditar = null; showNuevoMiembroSheet = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PersonAdd, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (branch.team.isEmpty()) {
            Text("Sin miembros de equipo", fontSize = 12.sp, color = colors.textSecondary)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(branch.team) { emp ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                        Box {
                            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFF8B5CF6).copy(alpha = 0.1f)) {
                                Icon(Icons.Default.Person, null, Modifier.padding(10.dp), Color(0xFF8B5CF6))
                            }
                            if (isEditMode) {
                                IconButton(
                                    onClick = { onUpdateBranch(branch.copy(team = branch.team.filter { it.id != emp.id })) },
                                    modifier = Modifier.size(18.dp).align(Alignment.TopEnd).background(Color.Red, CircleShape)
                                ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp)) }
                            }
                        }
                        Text(emp.name, fontSize = 10.sp, color = colors.textPrimary, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(emp.position, fontSize = 8.sp, color = colors.textSecondary, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }

    if (showNuevoMiembroSheet) {
        NuevoMiembroBottomSheet(
            initial = miembroAEditar,
            colors = colors,
            onDismiss = { showNuevoMiembroSheet = false },
            onAceptar = { nuevo ->
                val updatedTeam = if (miembroAEditar != null) branch.team.map { if (it.id == nuevo.id) nuevo else it } else branch.team + nuevo
                onUpdateBranch(branch.copy(team = updatedTeam))
                showNuevoMiembroSheet = false
            }
        )
    }
}

@Composable
internal fun CompanyDetailView(
    company: CompanyProvider,
    providerImageUrl: String?,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    colors: PrestadorColors,
    otherAvatars: List<Pair<String?, () -> Unit>> = emptyList(),
    onNavigateToCalendarioConfig: (ownerId: String, ownerName: String) -> Unit = { _, _ -> },
    onUpdateBranch: (BranchProvider) -> Unit = {},
    onUpdateAllBranches: (List<BranchProvider>) -> Unit = {},
    onAddBranch: (BranchProvider, Boolean) -> Unit = { _, _ -> },
    onUpdateCompany: (CompanyProvider) -> Unit = {},
    onDeleteCompany: () -> Unit = {},
    onEditCompanyPhoto: () -> Unit = {},
    bloqueada: Boolean = false,
    onSettings: () -> Unit = {},
    allCategories: List<com.example.myapplication.core.data.local.entity.CategoryEntity> = emptyList(),
    usedCategories: List<String> = emptyList() // [LEY PAREJA]: Categorías ya usadas por otras empresas
) {
    var isCompanyEditMode by remember { mutableStateOf(false) }
    var showAddSucursalSheet by remember { mutableStateOf(false) }
    var showDeleteEmpresaDialog by remember { mutableStateOf(false) }

    // [LEY PAREJA]: Filtramos el catálogo global para excluir servicios ya tomados
    val availableCategories = remember(allCategories, usedCategories, company.categories) {
        allCategories.filter { it.name !in usedCategories || it.name in company.categories }
    }

    var editNombreComercial by remember { mutableStateOf(company.name) }
    var editRazonSocial by remember { mutableStateOf(company.razonSocial) }
    var editCuit by remember { mutableStateOf(company.cuit) }
    var editEmailCorp by remember { mutableStateOf(company.email) }

    var errorNombreComercial by remember { mutableStateOf<String?>(null) }
    var errorCuit by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var discardNavigateBack by remember { mutableStateOf(false) }
    var showSavedCheck by remember { mutableStateOf(false) }

    fun validateCompany(): Boolean {
        errorNombreComercial = if (editNombreComercial.isBlank()) "Obligatorio" else null
        errorCuit = errorCuitMensaje(editCuit)
        errorEmail = if (editEmailCorp.isNotBlank() && !editEmailCorp.contains("@")) "Email inválido" else null
        return errorNombreComercial == null && errorCuit == null && errorEmail == null
    }

    val listState = rememberLazyListState()
    val headerMaxHeight = 160.dp
    val headerMinHeight = 80.dp
    val density = LocalDensity.current
    val maxScrollPx = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0) (listState.firstVisibleItemScrollOffset.toFloat() / maxScrollPx).coerceIn(0f, 1f) else 1f
        }
    }
    val headerHeight by animateDpAsState(targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction, label = "h")
    val avatarSize by animateDpAsState(targetValue = 90.dp - (40.dp * collapseFraction), label = "a")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
            item { Spacer(Modifier.height(headerMaxHeight)) }

            item {
                ProfileSectionCard(Icons.Default.Business, "Datos del Negocio", Color(0xFF8B5CF6), colors) {
                    if (isCompanyEditMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(value = editNombreComercial, onValueChange = { editNombreComercial = it; errorNombreComercial = null }, label = { Text("Nombre Comercial") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), isError = errorNombreComercial != null)
                            OutlinedTextField(value = editRazonSocial, onValueChange = { editRazonSocial = it }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = editCuit, onValueChange = { editCuit = it }, label = { Text("CUIT") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), isError = errorCuit != null)
                            OutlinedTextField(value = editEmailCorp, onValueChange = { editEmailCorp = it; errorEmail = null }, label = { Text("Email Corporativo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), isError = errorEmail != null)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileInfoRow("🏬", "Comercial", company.name, colors)
                            ProfileInfoRow("🏢", "Razón Social", company.razonSocial, colors)
                            ProfileInfoRow("🆔", "CUIT", company.cuit, colors)
                            ProfileInfoRow("📧", "Email", company.email, colors)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text("RUBROS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    if (isCompanyEditMode) {
                        CategoriasSelector(
                            categoriasJson = try { org.json.JSONArray(company.categories).toString() } catch(_: Exception) { "[]" },
                            onCategoriasActualizadas = { json ->
                                val list = try {
                                    val arr = org.json.JSONArray(json)
                                    (0 until arr.length()).map { arr.getString(it) }
                                } catch (e: Exception) { emptyList<String>() }
                                onUpdateCompany(company.copy(categories = list))
                            },
                            allCategories = availableCategories // Usamos las filtradas
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            company.categories.forEach { cat -> SuggestionChip(onClick = {}, label = { Text(cat) }) }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("SUCURSALES (${company.branches.size}/2)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    Spacer(Modifier.weight(1f))
                    if (isCompanyEditMode) IconButton(onClick = { showAddSucursalSheet = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, null, tint = Color(0xFF8B5CF6)) }
                }
            }

            if (company.branches.isNotEmpty()) item {
                BranchesPager(branches = company.branches, colors = colors, isEditMode = isCompanyEditMode, onUpdateBranch = onUpdateBranch, onNavigateToCalendarioConfig = { _, name -> onNavigateToCalendarioConfig(company.id, name) })
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(headerHeight).align(Alignment.TopStart).zIndex(10f).background(colors.backgroundColor)) {
            IconButton(onClick = { if (isCompanyEditMode) { discardNavigateBack = true; showDiscardDialog = true } else onBack() }, modifier = Modifier.statusBarsPadding().padding(8.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.textPrimary) }
            Row(modifier = Modifier.align(Alignment.Center).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(avatarSize).clip(CircleShape).border(2.dp, Color(0xFF8B5CF6), CircleShape).then(if (isCompanyEditMode) Modifier.clickable { onEditCompanyPhoto() } else Modifier)) {
                    AsyncImage(model = company.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    if (isCompanyEditMode) Box(Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, null, tint = Color.White) }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).graphicsLayer { alpha = (1f - collapseFraction * 2f).coerceIn(0f, 1f) }) {
                    Text(company.name.ifBlank { "Empresa" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, maxLines = 1)
                    Text(company.razonSocial, fontSize = 12.sp, color = colors.textSecondary, maxLines = 1)
                }
            }
            Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                otherAvatars.forEach { (url, onClick) -> AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).border(1.dp, colors.border, CircleShape).clickable { onClick() }, contentScale = ContentScale.Crop) }
                AsyncImage(model = providerImageUrl, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, colors.primaryOrange, CircleShape).clickable { onBack() }, contentScale = ContentScale.Crop)
            }
        }

        BeActionsBar(
            visible = true,
            actions = if (!isCompanyEditMode) listOf(PrestadorAction("edit", Icons.Default.Edit, "Editar", colors.primaryOrange) { isCompanyEditMode = true })
            else listOf(
                PrestadorAction("cancel", Icons.Default.Close, "Cancelar", Color.Red) { isCompanyEditMode = false },
                PrestadorAction("save", Icons.Default.Save, "Guardar", Color(0xFF4CAF50)) { if (validateCompany()) { onUpdateCompany(company.copy(name = editNombreComercial.trim(), razonSocial = editRazonSocial.trim(), cuit = editCuit.trim(), email = editEmailCorp.trim())); showSavedCheck = true } },
                PrestadorAction("delete", Icons.Default.Delete, "Borrar", Color.Red) { showDeleteEmpresaDialog = true }
            ),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp)
        )

        if (showAddSucursalSheet) {
            if (company.branches.size >= 2) AlertDialog(onDismissRequest = { showAddSucursalSheet = false }, title = { Text("Límite de Sucursales") }, text = { Text("Máximo 2 sucursales por empresa.") }, confirmButton = { TextButton(onClick = { showAddSucursalSheet = false }) { Text("OK") } })
            else SucursalBottomSheet(colors = colors, onDismiss = { showAddSucursalSheet = false }, onAceptar = { branch, _ -> onAddBranch(branch, company.branches.isEmpty()); showAddSucursalSheet = false })
        }
        if (showDeleteEmpresaDialog) AlertDialog(onDismissRequest = { showDeleteEmpresaDialog = false }, title = { Text("Eliminar Empresa") }, text = { Text("¿Estás seguro?") }, confirmButton = { Button(onClick = { onDeleteCompany(); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar") } }, dismissButton = { TextButton(onClick = { showDeleteEmpresaDialog = false }) { Text("Cancelar") } })
        AnimatedVisibility(visible = showSavedCheck, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp).zIndex(99f)) {
            LaunchedEffect(Unit) { delay(2000); showSavedCheck = false; isCompanyEditMode = false }
            SavedToast(colors)
        }
        if (bloqueada) Box(modifier = Modifier.fillMaxSize().background(colors.backgroundColor.copy(0.8f)).zIndex(5f), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(20.dp), color = colors.surfaceColor, shadowElevation = 8.dp, modifier = Modifier.padding(32.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.Lock, null, tint = colors.primaryOrange, modifier = Modifier.size(48.dp))
                    Text("Perfil de empresa desactivado", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
                    Text("Activalo en Ajustes para editar.", textAlign = TextAlign.Center, fontSize = 13.sp, color = colors.textSecondary)
                    Button(onClick = onSettings, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)) { Text("Ir a Ajustes") }
                }
            }
        }
    }
    if (showDiscardDialog) DiscardChangesDialog(onConfirm = { showDiscardDialog = false; isCompanyEditMode = false; if (discardNavigateBack) onBack() }, onDismiss = { showDiscardDialog = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SucursalBottomSheet(colors: PrestadorColors, onDismiss: () -> Unit, onAceptar: (BranchProvider, Boolean) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf<AddressUnico?>(null) }
    var showAddressSheet by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surfaceColor) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Nueva Sucursal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(12.dp))
            if (direccion != null) Text(direccion!!.fullString(), fontSize = 12.sp, color = colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
            OutlinedButton(onClick = { showAddressSheet = true }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Icon(Icons.Default.AddLocation, null); Text("Elegir Dirección") }
            Button(onClick = { onAceptar(BranchProvider(name = nombre.trim(), address = direccion ?: AddressUnico()), false) }, enabled = nombre.isNotBlank() && direccion != null, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("Crear") }
        }
    }
    if (showAddressSheet) AddressBottomSheet(initial = direccion ?: AddressUnico(), onDismiss = { showAddressSheet = false }, onSave = { direccion = it; showAddressSheet = false })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BranchesPager(branches: List<BranchProvider>, colors: PrestadorColors, isEditMode: Boolean, onUpdateBranch: (BranchProvider) -> Unit, onNavigateToCalendarioConfig: (String, String) -> Unit) {
    val state = rememberPagerState { branches.size }
    Column {
        HorizontalPager(state = state) { page ->
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceColor), border = BorderStroke(1.dp, colors.border)) {
                Column(Modifier.padding(16.dp)) { BranchSection(index = page, branch = branches[page], colors = colors, isEditMode = isEditMode, onUpdateBranch = onUpdateBranch, onNavigateToCalendarioConfig = onNavigateToCalendarioConfig) }
            }
        }
        if (branches.size > 1) Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            branches.indices.forEach { i -> Box(modifier = Modifier.padding(2.dp).size(6.dp).clip(CircleShape).background(if (state.currentPage == i) Color(0xFF8B5CF6) else colors.textSecondary.copy(0.3f))) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoMiembroBottomSheet(initial: EmployeeProvider?, colors: PrestadorColors, onDismiss: () -> Unit, onAceptar: (EmployeeProvider) -> Unit) {
    var nombre by remember { mutableStateOf(initial?.name ?: "") }
    var cargo by remember { mutableStateOf(initial?.position ?: "") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surfaceColor) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(if (initial == null) "Nuevo Miembro" else "Editar Miembro", fontWeight = FontWeight.Bold)
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = cargo, onValueChange = { cargo = it }, label = { Text("Cargo/Puesto") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp))
            Button(onClick = { onAceptar(EmployeeProvider(id = initial?.id ?: UUID.randomUUID().toString(), name = nombre.trim(), position = cargo.trim())) }, enabled = nombre.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("Aceptar") }
        }
    }
}

package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.data.model.EmployeeProvider
import com.example.myapplication.prestador.ui.components.AddressBottomSheet
import com.example.myapplication.prestador.ui.components.AddressProviderCard
import com.example.myapplication.prestador.ui.components.BeActionsBar
import com.example.myapplication.prestador.ui.components.PrestadorAction
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.utils.formatearCuit
import com.example.myapplication.prestador.utils.errorCuitMensaje
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange

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
    var miembroAEditar by remember { mutableStateOf<com.example.myapplication.prestador.data.model.EmployeeProvider?>(null) }

    // Badge Casa Central
    if (index == 0) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF8B5CF6)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("CASA CENTRAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
    }

    // Nombre sucursal + botón Horarios
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(branchName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            if (index == 0) {
                Text("Casa Central seleccionada", fontSize = 12.sp, color = Color(0xFF8B5CF6))
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
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Horarios de Atención",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Horarios",
                fontSize = 9.sp,
                color = Color(0xFF8B5CF6),
                fontWeight = FontWeight.SemiBold,
                lineHeight = 11.sp
            )
        }
    }
    Spacer(Modifier.height(12.dp))

    // ── DIRECCIÓN con AddressProviderCard (reutiliza el mismo componente del perfil)
    val context = androidx.compose.ui.platform.LocalContext.current
    if (branch.address.fullString().isNotBlank()) {
        AddressProviderCard(
            address = branch.address,
            isEditMode = isEditMode,
            onEdit = { showAddressSheet = true },
            onDelete = {
                onUpdateBranch(
                    branch.copy(address = com.example.myapplication.prestador.data.model.AddressProvider())
                )
            },
            onOpenMaps = {
                val lat = branch.address.latitude
                val lng = branch.address.longitude
                if (lat != null && lng != null && (lat != 0.0 || lng != 0.0)) {
                    val uri = android.net.Uri.parse("geo:$lat,$lng?q=$lat,$lng(${branch.address.label.ifBlank { branchName }})")
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            }
        )
    } else if (isEditMode) {
        OutlinedButton(
            onClick = { showAddressSheet = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B5CF6)),
            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Agregar ubicación", fontSize = 13.sp)
        }
    }

    if (showAddressSheet) {
        AddressBottomSheet(
            initial = branch.address,
            onDismiss = { showAddressSheet = false },
            onSave = { newAddr ->
                onUpdateBranch(branch.copy(address = newAddr))
                showAddressSheet = false
            }
        )
    }
    
    // Chips de servicios activos de esta sede
    val activeServices = buildList<Pair<ImageVector, String>> {
        if (branch.doesService)          add(Icons.Default.Build         to "Realiza servicios")
        if (branch.doesProduct)          add(Icons.Default.ShoppingBag   to "Vende productos")
        if (branch.acceptsAppointments)  add(Icons.Default.CalendarMonth  to "Acepta turnos")
        if (branch.doesHomeVisits)       add(Icons.Default.DirectionsCar  to "A domicilio")
        if (branch.doesShipping)         add(Icons.Default.LocalShipping  to "Realiza envíos")
        if (branch.works24h)             add(Icons.Default.Warning        to "Urgencias 24hs")
        if (branch.hasPhysicalLocation)  add(Icons.Default.Store          to "Atención en local")
    }
    if (activeServices.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text(
            "SERVICIOS DE ESTA SEDE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            activeServices.forEach { (icon, label) ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(icon, null, Modifier.size(13.dp), tint = Color(0xFF8B5CF6))
                        Spacer(Modifier.width(5.dp))
                        Text(label, fontSize = 11.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (branch.employees.isNotEmpty() || isEditMode) {
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "EQUIPO DE TRABAJO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            if (isEditMode) {
                IconButton(
                    onClick = {
                        miembroAEditar = null
                        showNuevoMiembroSheet = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = "Agregar miembro",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (branch.employees.isEmpty()) {
            Text(
                "Sin miembros de equipo",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(branch.employees) { emp ->
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                                    .then(
                                        if (isEditMode) Modifier.clickable {
                                            miembroAEditar = emp
                                            showNuevoMiembroSheet = true
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!emp.photoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        emp.photoUrl, null,
                                        Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, null,
                                        Modifier.size(24.dp), Color(0xFF8B5CF6))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                emp.name, fontSize = 10.sp, color = colors.textPrimary,
                                textAlign = TextAlign.Center, maxLines = 1
                            )
                            if (emp.position.isNotBlank()) {
                                Text(
                                    emp.position, fontSize = 9.sp, color = colors.textSecondary,
                                    textAlign = TextAlign.Center, maxLines = 1
                                )
                            }
                        }
                        //Botón borrar sobre el avatar
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                                    .clickable {
                                        onUpdateBranch(
                                            branch.copy(employees = branch.employees.filter { it.id != emp.id })
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, Modifier.size(10.dp), tint = Color.White)
                            }
                        }
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
            onAceptar = { nuevoMiembro ->
                val updateEmployees = if (miembroAEditar != null) {
                    branch.employees.map { if (it.id == nuevoMiembro.id) nuevoMiembro else it }
                } else {
                    branch.employees + nuevoMiembro
                }
                onUpdateBranch(branch.copy(employees = updateEmployees))
                showNuevoMiembroSheet = false
            }
        )
    }


    if (branch.galleryImages.isNotEmpty() || isEditMode) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val branchGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? ->
            uri?.let {
                val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(context, it, maxWidth = 800, maxHeight = 800, quality = 75)
                if (bytes != null) {
                    val base64 = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)
                    onUpdateBranch(branch.copy(galleryImages = branch.galleryImages + base64))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("GALERIA DE ESTA SEDE", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = colors.textSecondary, letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            if (isEditMode) {
                IconButton(
                    onClick = { branchGalleryLauncher.launch("image/*") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(20.dp), tint = Color(0xFFF59E0B))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (branch.galleryImages.isEmpty()) {
            Text("Sin fotos de esta sede", fontSize = 12.sp, color = colors.textSecondary)

        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(branch.galleryImages) { imageData ->
                    val model: Any = if (imageData.startsWith("http")) imageData
                    else try {
                        val b = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size) ?:  imageData
                    } catch (e: Exception) { imageData }
                    Box{
                        AsyncImage(model, null, Modifier.size(80.dp).clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop)
                        if (isEditMode) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                                    .clickable{
                                        onUpdateBranch(branch.copy(galleryImages = branch.galleryImages.filter { it != imageData }))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, Modifier.size(10.dp), tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BranchInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    colors: PrestadorColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center

        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = iconColor)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = iconColor, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, color = colors.textPrimary, lineHeight = 20.sp)
        }
    }
}

// ── VISTA DETALLE DE EMPRESA ─────────────────────────────────────────────────
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
    onEditCompanyBanner: () -> Unit = {},
    bloqueada: Boolean = false,
    onSettings: () -> Unit = {},
) {
    var isCompanyEditMode by remember { mutableStateOf(false) }
    var showEmpresaEditSheet by remember { mutableStateOf(false) }
    var showAddSucursalSheet by remember { mutableStateOf(false) }
    var showDeleteEmpresaDialog by remember { mutableStateOf(false) }

    var editNombreComercial by remember { mutableStateOf("") }
    var editRazonSocial by remember { mutableStateOf("") }
    var editCuit by remember { mutableStateOf(TextFieldValue("")) }
    var editEmailCorp by remember { mutableStateOf("") }

    //Errores de validación
    var errorNombreComercial by remember { mutableStateOf<String?>(null) }
    var errorCuit by remember { mutableStateOf<String?>(null) }
    var errorEmail by remember { mutableStateOf<String?>(null) }
    var errorPhoto by remember { mutableStateOf(false) }

    //Diálogo de cambios sin guardar
    var showDiscardDialog by remember { mutableStateOf(false) }
    var discardNavigateBack by remember { mutableStateOf(false) }
    var showSavedCheck by remember { mutableStateOf(false) }

    fun validateCompany(): Boolean {
        errorNombreComercial = if (editNombreComercial.isBlank()) "El nombre es obligatorio" else null
        errorCuit = errorCuitMensaje(editCuit.text)
        errorEmail = if (!editEmailCorp.contains("@")) "Email inválido" else null
        errorPhoto = company.photoUrl.isNullOrEmpty()
        return errorNombreComercial == null && errorCuit == null && errorEmail == null && !errorPhoto
    }

    LaunchedEffect(isCompanyEditMode) {
        if (isCompanyEditMode) {
            editNombreComercial = company.name
            editRazonSocial = company.razonSocial
            editCuit = TextFieldValue(formatearCuit(company.cuit))
            editEmailCorp = company.email
        }
    }

    val listState = rememberLazyListState()
    val headerMaxHeight = 330.dp
    val headerMinHeight = 140.dp
    val density = LocalDensity.current
    val maxScrollPx = with(density) { (headerMaxHeight - headerMinHeight).toPx() }
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0)
                (listState.firstVisibleItemScrollOffset.toFloat() / maxScrollPx).coerceIn(0f, 1f)
            else 1f
        }
    }
    val headerHeight by animateDpAsState(
        targetValue = headerMaxHeight - (headerMaxHeight - headerMinHeight) * collapseFraction,
        label = "headerHeight"
    )
    val avatarSize by animateDpAsState(
        targetValue = 90.dp - (35.dp * collapseFraction),
        label = "avatarSize"
    )

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── HEADER EMPRESA (espaciador para el overlay) ───────────────────
        item { Spacer(Modifier.height(headerMaxHeight)) }

        item { Spacer(Modifier.height(16.dp)) }

        // ── DATOS DEL NEGOCIO ────────────────────────────────────────────
        item {
            val firstBranch = company.branches.firstOrNull()
            var showServiciosDialog by remember { mutableStateOf(false) }
            var editDoesService by remember(firstBranch) { mutableStateOf(firstBranch?.doesService ?: false) }
            var editDoesProduct by remember(firstBranch) { mutableStateOf(firstBranch?.doesProduct ?: false) }
            var editWorks24h by remember(firstBranch) { mutableStateOf(firstBranch?.works24h ?: false) }
            var editHasPhysical by remember(firstBranch) { mutableStateOf(firstBranch?.hasPhysicalLocation ?: false) }
            var editDoesHomeVisits by remember(firstBranch) { mutableStateOf(firstBranch?.doesHomeVisits ?: false) }
            var editDoesShipping by remember(firstBranch) { mutableStateOf(firstBranch?.doesShipping ?: false) }
            var editAcceptsAppointments by remember(firstBranch) { mutableStateOf(firstBranch?.acceptsAppointments ?: false) }

            if (showServiciosDialog) {
                AlertDialog(
                    onDismissRequest = { showServiciosDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                val updatedBranches = company.branches.map { b ->
                                    b.copy(
                                        doesService = editDoesService,
                                        doesProduct = editDoesProduct,
                                        works24h = editWorks24h,
                                        hasPhysicalLocation = editHasPhysical,
                                        doesHomeVisits = editDoesHomeVisits,
                                        doesShipping = editDoesShipping,
                                        acceptsAppointments = editAcceptsAppointments
                                    )
                                }
                                onUpdateAllBranches(updatedBranches)
                                showServiciosDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) { Text("Guardar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showServiciosDialog = false }) { Text("Cancelar") }
                    },
                    title = { Text("Servicios del negocio", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BranchServicioSwitch("Realiza servicios", Icons.Default.Build, editDoesService) { editDoesService = it }
                            BranchServicioSwitch("Vende productos", Icons.Default.ShoppingBag, editDoesProduct) { editDoesProduct = it }
                            BranchServicioSwitch("Acepta turnos", Icons.Default.CalendarMonth, editAcceptsAppointments) { editAcceptsAppointments = it }
                            BranchServicioSwitch("Visitas a domicilio", Icons.Default.DirectionsCar, editDoesHomeVisits) { editDoesHomeVisits = it }
                            BranchServicioSwitch("Realiza envíos", Icons.Default.LocalShipping, editDoesShipping) { editDoesShipping = it }
                            BranchServicioSwitch("Urgencias 24hs", Icons.Default.Warning, editWorks24h) { editWorks24h = it }
                            BranchServicioSwitch("Atención en local", Icons.Default.Store, editHasPhysical) { editHasPhysical = it }
                        }
                    }
                )
            }

            ProfileSectionCard(
                Icons.Default.Business,
                "Datos del Negocio",
                Color(0xFF8B5CF6),
                colors,
            ) {

                if (isCompanyEditMode) {
                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.4f),
                        focusedLabelColor = Color(0xFF8B5CF6),
                        unfocusedLabelColor = colors.textSecondary,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = Color(0xFF8B5CF6)
                    )
                    @Composable fun changedColors(changed: Boolean) = if (changed)
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF9800),
                            unfocusedBorderColor = Color(0xFFFF9800),
                            focusedLabelColor = Color(0xFFFF9800),
                            unfocusedLabelColor = Color(0xFFFF9800),
                            cursorColor = Color(0xFF8B5CF6),
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    else fieldColors
                    if (errorPhoto) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF5252).copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                            Text("Agregá un logo de empresa", fontSize = 11.sp, color = Color(0xFFFF5252))
                        }
                    }
                        OutlinedTextField(
                            value = editNombreComercial,
                            onValueChange = { editNombreComercial = it; errorNombreComercial = null },
                            label = { Text("Nombre Comercial") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = changedColors(editNombreComercial != company.name),
                            shape = RoundedCornerShape(12.dp),
                            isError = errorNombreComercial != null,
                            supportingText = errorNombreComercial?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } }
                        )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editRazonSocial,
                        onValueChange = { editRazonSocial = it },
                        label = { Text("Razón Social") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = changedColors(editRazonSocial != company.razonSocial),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editCuit,
                        onValueChange = { nuevo ->
                            val soloDigitos = nuevo.text.filter { it.isDigit() }.take(11)
                            val formateado = formatearCuit(soloDigitos)
                            val digitosAntesCursor = nuevo.text.take(nuevo.selection.start).count {
                                it.isDigit() }
                            var conteo = 0
                            var nuevoCursor = formateado.length
                            for (i in formateado.indices) {
                                if (formateado[i].isDigit()) conteo++
                                if (conteo == digitosAntesCursor) {
                                    nuevoCursor = i + 1; break
                                }
                            }
                            editCuit = TextFieldValue(formateado, TextRange(nuevoCursor))
                            errorCuit = null
                        },
                        label = { Text("CUIT") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = changedColors(editCuit.text != company.cuit),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorCuit != null,
                        supportingText = errorCuit?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } }
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editEmailCorp,
                        onValueChange = { editEmailCorp = it; errorEmail = null },
                        label = { Text("Email Corporativo") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = changedColors(editEmailCorp != company.email),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorEmail != null,
                        supportingText = errorEmail?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } }
                    )
                    Spacer(Modifier.height(8.dp))
                } else {
                    if (company.name.isNotBlank()) {
                        ProfileInfoRow("🏬", "Nombre Comercial", company.name, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (company.razonSocial.isNotBlank()) {
                        ProfileInfoRow("🏢", "Razón Social", company.razonSocial, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (company.cuit.isNotBlank()) {
                        ProfileInfoRow("🆔", "CUIT", company.cuit, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (company.email.isNotBlank()) {
                        ProfileInfoRow("📧", "Email Corporativo", company.email, colors)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (company.description.isNotBlank()) {
                        Text(company.description, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Servicios activos con etiquetas
                val activeServices = buildList<Pair<ImageVector, String>> {
                    if (editDoesService)         add(Icons.Default.Build         to "Realiza servicios")
                    if (editDoesProduct)         add(Icons.Default.ShoppingBag   to "Vende productos")
                    if (editAcceptsAppointments) add(Icons.Default.CalendarMonth  to "Acepta turnos")
                    if (editDoesHomeVisits)      add(Icons.Default.DirectionsCar  to "Visitas a domicilio")
                    if (editDoesShipping)        add(Icons.Default.LocalShipping  to "Realiza envíos")
                    if (editWorks24h)            add(Icons.Default.Warning        to "Urgencias 24hs")
                    if (editHasPhysical)         add(Icons.Default.Store          to "Atención en local")
                }
                if (activeServices.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "SERVICIOS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeServices.forEach { (icon, label) ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(icon, null, Modifier.size(13.dp), tint = Color(0xFF8B5CF6))
                                    Spacer(Modifier.width(5.dp))
                                    Text(label, fontSize = 11.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = { showServiciosDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configurar servicios",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        // ── SUCURSALES ───────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SUCURSALES (${company.branches.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = colors.textSecondary
                )
                Spacer(Modifier.weight(1f))
                if (isCompanyEditMode) {
                    IconButton(
                        onClick = { showAddSucursalSheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar sucursal",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        if (company.branches.isNotEmpty()) {
            item {
                BranchesPager(
                    branches = company.branches,
                    colors = colors,
                    isEditMode = isCompanyEditMode,
                    onUpdateBranch = onUpdateBranch,
                    // Siempre usamos company.id como owner_id — los horarios pertenecen a la empresa, no a la sucursal
                    onNavigateToCalendarioConfig = { _, branchName ->
                        onNavigateToCalendarioConfig(company.id, branchName)
                    }
                )
            }
        }
    } // cierra LazyColumn

    // ── HEADER COLAPSABLE (overlay) ───────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .align(Alignment.TopStart)
            .zIndex(10f)
    ) {
        // Banner llena todo el header
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isCompanyEditMode) Modifier.clickable { onEditCompanyBanner() } else Modifier)
        ) {
            val bannerModel = company.bannerImageUrl?.takeIf { it.isNotEmpty() }
            when {
                bannerModel != null && bannerModel.startsWith("http") ->
                    AsyncImage(bannerModel, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                bannerModel != null -> {
                    val bmp = remember(bannerModel) {
                        try {
                            val b = android.util.Base64.decode(bannerModel, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bmp != null) Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else BannerGradientCompany(colors)
                }
                else -> BannerGradientCompany(colors)
            }
            // Fade inferior
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, colors.backgroundColor.copy(alpha = 0.85f)),
                        startY = 100f
                    )
                )
            )
            // Botón volver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(36.dp), CircleShape, Color.Black.copy(alpha = 0.35f)) {
                    IconButton(onClick = {
                        if (isCompanyEditMode) {
                            discardNavigateBack = true
                            showDiscardDialog = true
                        } else {
                            onBack()
                        }
                    }, Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Logo + Nombre empresa (centrado verticalmente, igual que perfil prestador)
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFF8B5CF6), CircleShape)
                    .then(if (isCompanyEditMode) Modifier.clickable { onEditCompanyPhoto() } else Modifier)
            ) {
                val logoUrl = company.photoUrl
                when {
                    !logoUrl.isNullOrEmpty() && logoUrl.startsWith("http") ->
                        AsyncImage(logoUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    !logoUrl.isNullOrEmpty() -> {
                        val bmp = remember(logoUrl) {
                            try {
                                val b = android.util.Base64.decode(logoUrl, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                            } catch (e: Exception) { null }
                        }
                        if (bmp != null) {
                            Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.fillMaxSize().background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Business, null, Modifier.size(48.dp), tint = Color(0xFF8B5CF6).copy(alpha = 0.6f))
                            }
                        }
                    }
                    else -> {
                        Box(Modifier.fillMaxSize().background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Business, null, Modifier.size(48.dp), tint = Color(0xFF8B5CF6).copy(alpha = 0.6f))
                        }
                    }
                }
                if (isCompanyEditMode) {
                    Box(
                        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(28.dp), tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = (1f - collapseFraction * 2f).coerceIn(0f, 1f) }
            ) {
                Text(
                    company.name.ifBlank { "Empresa" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (company.razonSocial.isNotBlank()) {
                    Text(company.razonSocial, fontSize = 13.sp, color = colors.textSecondary, maxLines = 1)
                }
            }
        }

        // Avatares de toggle: prestador personal + otras empresas (bottom-end)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Primero las otras empresas (si hay)
            otherAvatars.forEach { (photoUrl, onClick) ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .clickable { onClick() }
                ) {
                    ProfilePhoto(imageUrl = photoUrl, colors = colors, isCompany = true)
                }
            }
            // Luego el avatar del prestador personal (volver)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { onBack() }
            ) {
                ProfilePhoto(imageUrl = providerImageUrl, colors = colors, isCompany = true)
            }
        }
    }

    // ── ACTIONS BAR ──────────────────────────────────────────────────────
    BeActionsBar(
        visible = true,
        actions = if (!isCompanyEditMode) {
            listOf(
                PrestadorAction("edit", Icons.Default.Edit, "Editar", tint = colors.primaryOrange) { isCompanyEditMode = true }
            )
        } else {
            listOf(
                PrestadorAction("cancel", Icons.Default.Close, "Cancelar", tint = Color(0xFFFF5252)) { isCompanyEditMode = false },
                PrestadorAction("divider_1", Icons.Default.Edit, ""),
                PrestadorAction("save", Icons.Default.Save, "Guardar", tint = colors.primaryOrange) {
                    if (validateCompany()) {
                        onUpdateCompany(
                            company.copy(
                            name = editNombreComercial.trim(),
                            razonSocial = editRazonSocial.trim(),
                            cuit = editCuit.text.trim(),
                            email = editEmailCorp.trim()
                        )
                        )
                        showSavedCheck = true
                    }
                },
                PrestadorAction("delete", Icons.Default.Delete, "Eliminar", tint = Color(0xFFFF5252)) { showDeleteEmpresaDialog = true }
            )
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 24.dp)
    )

    // ── SHEET AGREGAR SUCURSAL ────────────────────────────────────────────
    if (showAddSucursalSheet) {
        SucursalBottomSheet(
            colors = colors,
            onDismiss = { showAddSucursalSheet = false },
            onAceptar = { branch, esCasaCentral ->
                onAddBranch(branch, esCasaCentral)
                showAddSucursalSheet = false
            }
        )
    }

    // ── SHEET EDITAR EMPRESA ──────────────────────────────────────────────
    if (showEmpresaEditSheet) {
        EmpresaEditBottomSheet(
            company = company,
            colors = colors,
            onDismiss = { showEmpresaEditSheet = false },
            onGuardar = { updatedCompany ->
                onUpdateCompany(updatedCompany)
                showEmpresaEditSheet = false
            }
        )
    }

        if (showDeleteEmpresaDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteEmpresaDialog = false },
                title = { Text("Eliminar empresa", fontWeight = FontWeight.Bold)},
                text = { Text("¿Estás seguro? Se eliminaran todas las sucursales y datos de la empresa") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteEmpresaDialog = false
                            onDeleteCompany()
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteEmpresaDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // TOAST GUARDADO EMPRESA
        androidx.compose.animation.AnimatedVisibility(
            visible = showSavedCheck,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .zIndex(99f),
            enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut()
        ) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSavedCheck = false
                isCompanyEditMode = false
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceElevated,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFFFF5722))
                                ),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "¡Empresa guardada!",
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Los cambios se aplicaron correctamente",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        //Overlay empresa bloqueada
        if (bloqueada) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.backgroundColor.copy(alpha = 0.82f))
                    .zIndex(5f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceColor.copy(alpha = 0.95f),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(colors.primaryOrange.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Empresa desactivada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Para usar el perfil de empresa activá el modo empresa desde Ajustes.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Settings, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ir a Ajustes")
                        }
                    }
                }
            }
        }

        } // cierra Box

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("¿Descartar cambios?", fontWeight = FontWeight.Bold) },
            text = { Text("Tenés cambios sin guardar. ¿Querés descartarlos?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    isCompanyEditMode = false
                    if (discardNavigateBack) onBack()
                }) {
                    Text("Descartar", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false; discardNavigateBack = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }
}

// ── BOTTOM SHEET AGREGAR SUCURSAL ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SucursalBottomSheet(
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onAceptar: (BranchProvider, Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var esCasaCentral by remember { mutableStateOf(false) }
    var direccion by remember { mutableStateOf<com.example.myapplication.prestador.data.model.AddressProvider?>(null) }
    var showAddressSheet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Store, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Editar Sucursal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFFF5252))
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de Sucursal") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Store, null, tint = Color(0xFF8B5CF6)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.4f),
                    focusedLabelColor = Color(0xFF8B5CF6),
                    unfocusedLabelColor = colors.textSecondary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { esCasaCentral = !esCasaCentral }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = esCasaCentral,
                    onCheckedChange = { esCasaCentral = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF8B5CF6),
                        uncheckedColor = colors.textSecondary
                    )
                )
                Text("Es Casa Central / Sede Principal", color = colors.textPrimary, fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Dirección seleccionada
            if (direccion != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = Color(0xFF8B5CF6))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        direccion!!.fullString().ifBlank { "Dirección seleccionada" },
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { showAddressSheet = true }) {
                        Text("Cambiar", fontSize = 11.sp)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showAddressSheet = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B5CF6)),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar ubicación", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val addr = direccion
                    onAceptar(
                        BranchProvider(
                            name = nombre.trim(),
                            address = addr ?: com.example.myapplication.prestador.data.model.AddressProvider()
                        ),
                        esCasaCentral
                    )
                },
                enabled = nombre.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFF4CAF50).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aceptar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAddressSheet) {
        AddressBottomSheet(
            initial = direccion ?: com.example.myapplication.prestador.data.model.AddressProvider(),
            onDismiss = { showAddressSheet = false },
            onSave = { addr ->
                direccion = addr
                showAddressSheet = false
            }
        )
    }
}

// ── BOTTOM SHEET EDITAR EMPRESA ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmpresaEditBottomSheet(
    company: CompanyProvider,
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onGuardar: (CompanyProvider) -> Unit
) {
    var nombreComercial by remember { mutableStateOf(company.name) }
    var razonSocial by remember { mutableStateOf(company.razonSocial) }
    var cuit by remember { mutableStateOf(company.cuit) }
    var emailCorporativo by remember { mutableStateOf(company.email) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Business, null, tint = colors.primaryOrange, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Datos de Empresa",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFFF5252))
                }
            }

            Spacer(Modifier.height(16.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.4f),
                focusedLabelColor = colors.primaryOrange,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primaryOrange
            )

            OutlinedTextField(
                value = nombreComercial,
                onValueChange = { nombreComercial = it },
                label = { Text("Nombre Comercial") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = razonSocial,
                onValueChange = { razonSocial = it },
                label = { Text("Razón Social") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = cuit,
                onValueChange = { cuit = it.filter { c -> c.isDigit() || c == '-' } },
                label = { Text("CUIT") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = emailCorporativo,
                onValueChange = { emailCorporativo = it },
                label = { Text("Email Corporativo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onGuardar(
                        company.copy(
                            name = nombreComercial.trim(),
                            razonSocial = razonSocial.trim(),
                            cuit = cuit.trim(),
                            email = emailCorporativo.trim()
                        )
                    )
                },
                enabled = nombreComercial.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange,
                    disabledContainerColor = colors.primaryOrange.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BranchesPager(
    branches: List<BranchProvider>,
    colors: PrestadorColors,
    isEditMode: Boolean = false,
    onUpdateBranch: (BranchProvider) -> Unit = {},
    onNavigateToCalendarioConfig: (ownerId: String, ownerName: String) -> Unit = { _, _ -> }
) {
    val pagerState = rememberPagerState(pageCount = { branches.size })

    Column {
        // Pager de sucursales
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    BranchSection(index = page, branch = branches[page], colors = colors, isEditMode = isEditMode, onUpdateBranch = onUpdateBranch, onNavigateToCalendarioConfig = onNavigateToCalendarioConfig)
                }
            }
        }

        // Indicadores de puntos (solo si hay más de 1)
        if (branches.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                branches.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF8B5CF6)
                                else colors.textSecondary.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BannerGradientCompany(colors: PrestadorColors) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(
                    Color(0xFF8B5CF6).copy(alpha = 0.8f),
                    Color(0xFF6D28D9).copy(alpha = 0.5f),
                    colors.backgroundColor
                )
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NuevoMiembroBottomSheet(
    initial: com.example.myapplication.prestador.data.model.EmployeeProvider?,
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onAceptar: (com.example.myapplication.prestador.data.model.EmployeeProvider) -> Unit
) {
    var nombre by remember { mutableStateOf(initial?.name ?: "") }
    var apellido by remember { mutableStateOf(initial?.lastName ?: "") }
    var cargo by remember { mutableStateOf(initial?.position ?: "") }
    var photoUrl by remember { mutableStateOf(initial?.photoUrl) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? ->
        uri?.let {
            val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(context, it, maxWidth = 200, maxHeight = 200, quality = 75)
            if (bytes != null) photoUrl = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)
        }
    }


    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF8B5CF6),
        unfocusedBorderColor = colors.border,
        focusedLabelColor = Color(0xFF8B5CF6),
        unfocusedLabelColor = colors.textSecondary,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        cursorColor = Color(0xFF8B5CF6)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (initial != null) "Editar Miembro" else "Nuevo Miembro",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = colors.textPrimary, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFFF5252))
                }
            }
            Spacer(Modifier.height(16.dp))

            //Foto
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
                    .clickable { photoLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!photoUrl.isNullOrEmpty()) {
                    val model: Any = if (photoUrl!!.startsWith("http")) photoUrl!!
                    else try {
                        val b = android.util.Base64.decode(photoUrl!!, android.util.Base64.DEFAULT)
                        android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size) ?: photoUrl!!
                    } catch (e: Exception) { photoUrl!! }
                    AsyncImage(model, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, null, Modifier.size(40.dp), tint = Color(0xFF8B5CF6))
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhotoCamera, null, Modifier.size(16.dp), tint = Color.White)
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onAceptar(
                        com.example.myapplication.prestador.data.model.EmployeeProvider(
                            id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                            name = nombre.trim(),
                            lastName = apellido.trim(),
                            position = cargo.trim(),
                            photoUrl = photoUrl
                        )
                    )
                },
                enabled = nombre.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6),
                    disabledContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aceptar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
internal fun BranchServicioSwitch(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(label, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }



}


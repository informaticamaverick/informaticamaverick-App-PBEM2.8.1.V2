package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.prestador.ui.components.AddressBottomSheet
import com.example.myapplication.prestador.ui.components.AddressProviderCard
import com.example.myapplication.prestador.ui.components.BeActionsBar
import com.example.myapplication.prestador.ui.components.PrestadorAction
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.calendar.AvailabilityViewModel
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState
import com.example.myapplication.prestador.viewmodel.profile.UpdateState
import org.json.JSONArray

/**
 * --- PROFILE SCREEN (STATELESS UI - LEY #1) ---
 * Esta pantalla es puramente visual. Delegue toda la lógica de negocio, 
 * cálculos de GPS y validaciones al EditProfileViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCalendarioConfig: () -> Unit = {},
    onNavigateToCalendarioConfigEntity: (ownerId: String, ownerName: String) -> Unit = { _, _ -> },
    viewModel: EditProfileViewModel = hiltViewModel(),
    scheduleVm: AvailabilityViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val editUiState by viewModel.editUiState.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val profileProgress by viewModel.profileProgress.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val companyError by viewModel.companyError.collectAsStateWithLifecycle()
    val servicios by viewModel.servicios.collectAsStateWithLifecycle()
    val horarios by scheduleVm.schedules.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // --- ESTADOS DE CONTROL ---
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showEmpresaSheet by remember { mutableStateOf(false) }
    var showPriorizarDialog by remember { mutableStateOf(false) }
    var showSavedCheck by remember { mutableStateOf(false) }
    
    var selectedCompanyId by remember { mutableStateOf<String?>(null) }
    var editingAddress by remember { mutableStateOf<AddressUnico?>(null) }
    var empresaPendiente by remember { mutableStateOf<com.example.myapplication.core.domain.model.CompanyProvider?>(null) }

    /**
     * [LEY #2]: REFRESH LOCAL-FIRST
     * Al volver a la pantalla (OnResume), aseguramos que los datos de Room estén frescos.
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFromRoom()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    /**
     * [LEY #7]: FEEDBACK TÁCTICO
     * Observamos el resultado del guardado para mostrar éxito o error.
     */
    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is UpdateState.Success -> {
                viewModel.resetUpdateState()
                showSavedCheck = true // Feedback visual de guardado
                kotlinx.coroutines.delay(2000)
                viewModel.setEditMode(false) // Salimos de edición solo al guardar con éxito
            }
            is UpdateState.Error -> {
                viewModel.resetUpdateState()
                snackbarHostState.showSnackbar("Error SSOT: ${s.message}")
            }
            else -> Unit
        }
    }

    LaunchedEffect(companyError) {
        companyError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCompanyError()
        }
    }

    // --- GESTORES MULTIMEDIA ---
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    val companyPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val provider = (profileState as? ProfileState.Success)?.provider
            val company = provider?.companies?.find { c -> c.id == selectedCompanyId } ?: provider?.companies?.firstOrNull()
            if (company != null) viewModel.addCompany(company, uri)
        }
    }

    // --- LÓGICA DE HEADER COLAPSABLE (UI) ---
    val topBarAlpha by animateFloatAsState(
        targetValue = if (listState.firstVisibleItemIndex > 0) 1f
        else (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f),
        label = "topBarAlpha"
    )
    val headerMaxHeight = 160.dp 
    val headerMinHeight = 80.dp
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
        targetValue = 90.dp - (40.dp * collapseFraction),
        label = "avatarSize"
    )

    Scaffold(
        containerColor = colors.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Barra superior dinámica que aparece al scroll
            Surface(
                modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = topBarAlpha },
                color = colors.surfaceColor,
                shadowElevation = if (topBarAlpha > 0.01f) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isEditMode) {
                            // SI HAY CAMBIOS, PREGUNTAMOS. SI NO, SALIMOS DIRECTO.
                            if (editUiState.isDirty) showDiscardDialog = true
                            else viewModel.setEditMode(false)
                        } else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primaryOrange)
                    }
                    Text(
                        if (isEditMode) "Editando Perfil" else "Mi Perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    ) { padding ->
        // ESTADO DE CARGA SHIMMER
        if (profileState is ProfileState.Loading) {
            ProfileSkeletonScreen(padding = padding, colors = colors)
            return@Scaffold
        }

        val provider = (profileState as? ProfileState.Success)?.provider
        if (provider == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primaryOrange)
            }
            return@Scaffold
        }

        // --- VISTA DETALLE EMPRESA (Si se seleccionó una) ---
        val selectedCompany = provider.companies.find { it.id == selectedCompanyId }
        if (selectedCompany != null) {
            // [LEY PAREJA]: Recolectamos categorías ya usadas por otras empresas
            val usedCategories = provider.companies
                .filter { it.id != selectedCompany.id }
                .flatMap { it.categories }

            CompanyDetailView(
                company = selectedCompany,
                providerImageUrl = provider.imageUrl,
                paddingValues = padding,
                onBack = { selectedCompanyId = null },
                colors = colors,
                otherAvatars = provider.companies
                    .filter { it.id != selectedCompany.id }
                    .map { c -> Pair(c.photoUrl) { selectedCompanyId = c.id } },
                onNavigateToCalendarioConfig = onNavigateToCalendarioConfigEntity,
                onUpdateBranch = { b -> viewModel.addCompany(selectedCompany.copy(branches = selectedCompany.branches.map { if (it.id == b.id) b else it })) },
                onUpdateAllBranches = { bList -> viewModel.addCompany(selectedCompany.copy(branches = bList)) },
                onUpdateCompany = { c -> viewModel.addCompany(c) },
                onDeleteCompany = { viewModel.removeCompany(selectedCompany.id); selectedCompanyId = null },
                onEditCompanyPhoto = { companyPhotoLauncher.launch("image/*") },
                bloqueada = !provider.priorizarEmpresa,
                onSettings = onSettings,
                allCategories = servicios,
                usedCategories = usedCategories // Pasamos las categorías prohibidas
            )
            return@Scaffold
        }

        // ACCIONES FLOTANTES DINÁMICAS
        val beActions = if (!isEditMode) {
            listOf(
                PrestadorAction("settings", Icons.Default.Settings, "Ajustes", tint = colors.primaryOrange, onClick = onSettings),
                PrestadorAction("edit", Icons.Default.Edit, "Editar", tint = colors.primaryOrange) { viewModel.setEditMode(true) },
                PrestadorAction("logout", Icons.Default.ExitToApp, "Salir", tint = Color(0xFFFF5252)) { showLogoutDialog = true }
            )
        } else {
            listOf(
                PrestadorAction("empresa", Icons.Default.Business, "Empresas", tint = colors.primaryOrange) { showEmpresaSheet = true },
                PrestadorAction("cancel", Icons.Default.Close, "Descartar", tint = Color(0xFFFF5252)) {
                    if (editUiState.isDirty) showDiscardDialog = true else viewModel.setEditMode(false)
                },
                PrestadorAction("save", Icons.Default.Save, "Guardar", tint = if (editUiState.isDirty) Color(0xFF4CAF50) else colors.textSecondary) {
                    if (editUiState.isDirty) viewModel.saveProfileChanges()
                }
            )
        }

        val pullState = rememberPullToRefreshState()
        PullToRefreshBox(
            state = pullState,
            isRefreshing = false, 
            onRefresh = { viewModel.refreshProfile() },
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = false,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = colors.surfaceColor,
                    color = colors.primaryOrange
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item { Spacer(Modifier.height(headerMaxHeight)) }
                    
                    // AVISO MODO EMPRESA (Informativo - Ley #6)
                    if (provider.priorizarEmpresa && provider.companies.isNotEmpty()) {
                        item { BusinessModeBanner(colors) }
                    }

                    if (isEditMode) {
                        item { ProfileCompletionCard(profileProgress, colors) }
                    }

                    // --- SECCIÓN: DATOS PERSONALES ---
                    item { 
                        PersonalDataSection(
                            isEditMode = isEditMode,
                            uiState = editUiState,
                            provider = provider,
                            colors = colors,
                            onUpdateState = { viewModel.updateEditState(it) }
                        )
                    }

                    // --- SECCIÓN: SOBRE MÍ ---
                    item { 
                        AboutMeSection(
                            isEditMode = isEditMode,
                            description = if (isEditMode) editUiState.description else provider.description,
                            colors = colors,
                            onUpdateDescription = { text -> viewModel.updateEditState { s -> s.copy(description = text) } }
                        )
                    }

                    // --- SECCIÓN: HORARIOS ---
                    item {
                        ScheduleSection(
                            isEditMode = isEditMode && !provider.priorizarEmpresa,
                            horario = if (isEditMode) editUiState.localHorario else provider.workingHours,
                            horariosDetallados = horarios,
                            colors = colors,
                            onUpdateHorario = { viewModel.updateEditState { s -> s.copy(localHorario = it) } },
                            onNavigateToConfig = { onNavigateToCalendarioConfig() },
                            isLocked = provider.priorizarEmpresa
                        )
                    }

                    // --- SECCIÓN: CAPACIDADES (FLAGS) ---
                    item {
                        ServicesToggleSection(
                            isEditMode = isEditMode && !provider.priorizarEmpresa,
                            uiState = editUiState,
                            provider = provider,
                            onUpdateState = { viewModel.updateEditState(it) },
                            colors = colors,
                            isLocked = provider.priorizarEmpresa
                        )
                    }

                    // --- SECCIÓN: UBICACIONES (SSOT) ---
                    item {
                        AddressesSection(
                            isEditMode = isEditMode && !provider.priorizarEmpresa,
                            addresses = if (isEditMode) editUiState.addresses else provider.addresses,
                            mainAddress = if (isEditMode) editUiState.address else provider.address,
                            colors = colors,
                            onAddClick = { editingAddress = null; showAddressDialog = true },
                            onEditClick = { addr -> editingAddress = addr; showAddressDialog = true },
                            onDeleteClick = { addr -> 
                                if (isEditMode) {
                                    viewModel.updateEditState { s -> s.copy(addresses = s.addresses.filter { it.id != addr.id }) }
                                } else {
                                    viewModel.removeAdditionalAddress(addr.id)
                                }
                            },
                            isLocked = provider.priorizarEmpresa
                        )
                    }

                    // --- SECCIÓN: RUBROS (CATEGORÍAS) ---
                    item {
                        CategoriesSection(
                            isEditMode = isEditMode && !provider.priorizarEmpresa,
                            categories = if (isEditMode) editUiState.categories else provider.categories,
                            allCategories = servicios,
                            colors = colors,
                            onUpdateCategories = { list -> viewModel.updateEditState { it.copy(categories = list) } },
                            isLocked = provider.priorizarEmpresa
                        )
                    }
                }

                // HEADER COLAPSABLE (Diseño Elite - Sin Banners)
                Box(modifier = Modifier.fillMaxWidth().height(headerHeight).align(Alignment.TopStart).zIndex(10f)) {
                    ProfileBannerHeaderView(
                        name = if (isEditMode) editUiState.displayName.ifBlank { "${editUiState.name} ${editUiState.lastName}".trim() } 
                               else provider.displayName.ifBlank { "${provider.name} ${provider.lastName}".trim() },
                        profesion = provider.profesion ?: "Prestador",
                        imageUrl = provider.photoUrl,
                        rating = provider.rating,
                        isSubscribed = provider.isSubscribed,
                        isVerified = provider.isVerified,
                        paddingValues = padding,
                        onBack = onBack,
                        companyAvatars = provider.companies.map { c -> Pair(c.photoUrl) { selectedCompanyId = c.id } },
                        colors = colors,
                        isEditMode = isEditMode,
                        onEditPhoto = { photoLauncher.launch("image/*") },
                        collapseFraction = collapseFraction,
                        avatarSizeDp = avatarSize
                    )
                }

                BeActionsBar(
                    visible = true,
                    actions = beActions,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp)
                )

                // FEEDBACK DE GUARDADO
                if (showSavedCheck) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        SavedToast(colors)
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS Y HOJAS ---
    if (showAddressDialog) {
        AddressBottomSheet(
            initial = editingAddress ?: AddressUnico(),
            onDismiss = { showAddressDialog = false },
            onSave = { addr -> 
                if (isEditMode) {
                    viewModel.updateEditState { s -> 
                        val updated = if (editingAddress != null) s.addresses.map { if (it.id == addr.id) addr else it }
                                      else s.addresses + addr
                        s.copy(addresses = updated)
                    }
                } else {
                    viewModel.saveAdditionalAddress(addr)
                }
                showAddressDialog = false 
            }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(onConfirm = { showLogoutDialog = false; onLogout() }, onDismiss = { showLogoutDialog = false })
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onConfirm = { showDiscardDialog = false; viewModel.setEditMode(false) },
            onDismiss = { showDiscardDialog = false }
        )
    }

    if (showEmpresaSheet) {
        EmpresaBottomSheet(
            colors = colors,
            onDismiss = { showEmpresaSheet = false },
            onAceptar = { empresa ->
                empresaPendiente = empresa
                showEmpresaSheet = false
                showPriorizarDialog = true
            }
        )
    }

    if (showPriorizarDialog && empresaPendiente != null) {
        PriorizarEmpresaDialog(
            onConfirm = { 
                viewModel.addCompany(empresaPendiente!!, priorizarEmpresa = true)
                empresaPendiente = null
                showPriorizarDialog = false 
            },
            onDismiss = { 
                viewModel.addCompany(empresaPendiente!!, priorizarEmpresa = false)
                empresaPendiente = null
                showPriorizarDialog = false 
            }
        )
    }
}

// ==========================================
// SUB-COMPONENTES (UI PURA)
// ==========================================

@Composable
private fun BusinessModeBanner(colors: com.example.myapplication.prestador.ui.theme.PrestadorColors) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = BorderStroke(1.dp, Color(0xFFFFCC80))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Business, null, tint = Color(0xFFF57C00), modifier = Modifier.size(24.dp))
            Column {
                Text("Modo Empresa Activo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFF57C00))
                Text("Tu perfil personal está oculto para clientes. Solo se ven tus empresas.", fontSize = 11.sp, color = Color(0xFF795548), lineHeight = 15.sp)
            }
        }
    }
}

@Composable
private fun ProfileCompletionCard(progress: Float, colors: com.example.myapplication.prestador.ui.theme.PrestadorColors) {
    val progressAnim by animateFloatAsState(targetValue = progress, label = "progress")
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Completud del perfil", fontWeight = FontWeight.Bold, color = colors.textPrimary, fontSize = 14.sp)
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = colors.primaryOrange, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (progress >= 1f) Color(0xFF10B981) else colors.primaryOrange,
                trackColor = colors.border
            )
        }
    }
}

@Composable
private fun PersonalDataSection(
    isEditMode: Boolean,
    uiState: com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState,
    provider: com.example.myapplication.core.domain.model.Provider,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUpdateState: ((com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState) -> com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState) -> Unit
) {
    ProfileSectionCard(
        icon = Icons.Default.Work,
        title = "Datos Personales",
        iconColor = colors.primaryOrange,
        colors = colors
    ) {
        if (isEditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = { onUpdateState { s -> s.copy(displayName = it) } },
                    label = { Text("Nombre para mostrar (SSOT)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { onUpdateState { s -> s.copy(name = it) } },
                        label = { Text("Nombre") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = uiState.lastName,
                        onValueChange = { onUpdateState { s -> s.copy(lastName = it) } },
                        label = { Text("Apellido") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                OutlinedTextField(
                    value = uiState.cuilCuit,
                    onValueChange = { onUpdateState { s -> s.copy(cuilCuit = it.filter { c -> c.isDigit() }.take(11)) } },
                    label = { Text("DNI / CUIT") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { onUpdateState { s -> s.copy(phoneNumber = it) } },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = uiState.profesion,
                    onValueChange = { onUpdateState { s -> s.copy(profesion = it) } },
                    label = { Text("Profesión") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileInfoRow("👤", "Nombre", "${provider.name} ${provider.lastName}", colors)
                ProfileInfoRow("🆔", "DNI/CUIT", provider.cuilCuit ?: "No cargado", colors)
                ProfileInfoRow("📞", "Teléfono", provider.phoneNumber, colors)
                ProfileInfoRow("📧", "Email", provider.email, colors)
                if (provider.profesion != null) ProfileInfoRow("👨‍🔧", "Profesión", provider.profesion!!, colors)
            }
        }
    }
}

@Composable
private fun AboutMeSection(
    isEditMode: Boolean,
    description: String,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUpdateDescription: (String) -> Unit
) {
    ProfileSectionCard(Icons.Default.Info, "Sobre mí", Color(0xFF3B82F6), colors) {
        if (isEditMode) {
            OutlinedTextField(
                value = description,
                onValueChange = onUpdateDescription,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Text(description.ifBlank { "Sin descripción" }, fontSize = 14.sp, color = colors.textSecondary, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ScheduleSection(
    isEditMode: Boolean,
    horario: String,
    horariosDetallados: List<com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity>,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUpdateHorario: (String) -> Unit,
    onNavigateToConfig: () -> Unit,
    isLocked: Boolean = false
) {
    ProfileSectionCard(
        icon = Icons.Default.AccessTime,
        title = "Horario de Atención",
        iconColor = Color(0xFF8B5CF6),
        colors = colors,
        actionButton = {
            if (!isLocked) {
                IconButton(onClick = onNavigateToConfig) {
                    Icon(Icons.Default.Settings, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                }
            } else {
                Icon(Icons.Default.Lock, "Bloqueado", tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    ) {
        if (isLocked && isEditMode) {
            LockedSectionInfo(colors)
        } else if (isEditMode) {
            OutlinedTextField(
                value = horario,
                onValueChange = onUpdateHorario,
                label = { Text("Horario (Ej: 09:00-18:00)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("HH:MM-HH:MM") },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Este horario se muestra en la tarjeta de búsqueda.", fontSize = 11.sp, color = colors.textSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(horario.ifBlank { "Sin horario configurado" }, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                if (horariosDetallados.isNotEmpty()) {
                    HorizontalDivider(Modifier.padding(vertical = 4.dp), color = colors.border.copy(alpha = 0.5f))
                    horariosDetallados.forEach { h ->
                        Text("${h.dayOfWeek}: ${h.startTime} - ${h.endTime}", fontSize = 12.sp, color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicesToggleSection(
    isEditMode: Boolean,
    uiState: com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState,
    provider: com.example.myapplication.core.domain.model.Provider,
    onUpdateState: ((com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState) -> com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    isLocked: Boolean = false
) {
    ProfileSectionCard(
        icon = Icons.Default.Build, 
        title = "Servicios y Capacidades", 
        iconColor = Color(0xFF3B82F6), 
        colors = colors,
        actionButton = if (isLocked) ({
            Icon(Icons.Default.Lock, "Bloqueado", tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }) else null
    ) {
        if (isLocked && isEditMode) {
            LockedSectionInfo(colors)
        } else if (isEditMode) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SwitchRowLegacy("Brindo Servicios Técnicos", uiState.doesService) { onUpdateState { s -> s.copy(doesService = it) } }
                SwitchRowLegacy("Vendo Productos", uiState.doesProduct) { onUpdateState { s -> s.copy(doesProduct = it) } }
                SwitchRowLegacy("Urgencias 24hs", uiState.works24h) { onUpdateState { s -> s.copy(works24h = it) } }
                SwitchRowLegacy("Tengo Local/Taller Físico", uiState.hasPhysicalLocation) { onUpdateState { s -> s.copy(hasPhysicalLocation = it) } }
                SwitchRowLegacy("Voy a Domicilio", uiState.doesHomeVisits) { onUpdateState { s -> s.copy(doesHomeVisits = it) } }
                SwitchRowLegacy("Hago Envíos", uiState.doesShipping) { onUpdateState { s -> s.copy(doesShipping = it) } }
                SwitchRowLegacy("Acepto Turnos", uiState.acceptsAppointments) { onUpdateState { s -> s.copy(acceptsAppointments = it) } }
            }
        } else {
            val flags = buildList {
                if (provider.doesService) add(Icons.Default.Build to "Servicios")
                if (provider.doesProduct) add(Icons.Default.ShoppingBag to "Productos")
                if (provider.works24h) add(Icons.Default.Warning to "24hs")
                if (provider.hasPhysicalLocation) add(Icons.Default.Store to "Local")
                if (provider.doesHomeVisits) add(Icons.Default.DirectionsCar to "Domicilio")
                if (provider.doesShipping) add(Icons.Default.LocalShipping to "Envíos")
            }
            if (flags.isEmpty()) {
                Text("Sin servicios configurados", fontSize = 13.sp, color = colors.textSecondary)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    flags.forEach { (icon, label) ->
                        AssistChip(
                            onClick = {},
                            label = { Text(label) },
                            leadingIcon = { Icon(icon, null, Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressesSection(
    isEditMode: Boolean,
    addresses: List<AddressUnico>,
    mainAddress: AddressUnico?,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onAddClick: () -> Unit,
    onEditClick: (AddressUnico) -> Unit,
    onDeleteClick: (AddressUnico) -> Unit,
    isLocked: Boolean = false
) {
    ProfileSectionCard(
        icon = Icons.Default.LocationOn,
        title = "Ubicaciones",
        iconColor = Color(0xFFEF4444),
        colors = colors,
        actionButton = if (isLocked) ({
            Icon(Icons.Default.Lock, "Bloqueado", tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }) else if (isEditMode) ({
            IconButton(onClick = onAddClick) { Icon(Icons.Default.Add, null, tint = Color(0xFFEF4444)) }
        }) else null
    ) {
        if (isLocked && isEditMode) {
            LockedSectionInfo(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Dirección Principal (SSOT)
                mainAddress?.let { addr ->
                    Text("DIRECCIÓN PRINCIPAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    AddressProviderCard(
                        address = addr,
                        isEditMode = isEditMode,
                        onEdit = { onEditClick(addr) },
                        onDelete = { /* No permitir borrar la principal */ },
                        onOpenMaps = { }
                    )
                }

                // Direcciones Adicionales
                val additional = addresses.filter { it.id != mainAddress?.id }
                if (additional.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("DIRECCIONES ADICIONALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                    additional.forEach { addr ->
                        AddressProviderCard(
                            address = addr,
                            isEditMode = isEditMode,
                            onEdit = { onEditClick(addr) },
                            onDelete = { onDeleteClick(addr) },
                            onOpenMaps = { }
                        )
                    }
                }
                
                if (mainAddress == null && additional.isEmpty()) {
                    Text("Sin direcciones registradas", fontSize = 13.sp, color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun CategoriesSection(
    isEditMode: Boolean,
    categories: List<String>,
    allCategories: List<com.example.myapplication.core.data.local.entity.CategoryEntity>,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    onUpdateCategories: (List<String>) -> Unit,
    isLocked: Boolean = false
) {
    ProfileSectionCard(
        icon = Icons.Default.Category, 
        title = "Categorías", 
        iconColor = Color(0xFF00897B), 
        colors = colors,
        actionButton = if (isLocked) ({
            Icon(Icons.Default.Lock, "Bloqueado", tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
        }) else null
    ) {
        if (isLocked && isEditMode) {
            LockedSectionInfo(colors)
        } else if (isEditMode) {
            CategoriasSelector(
                categoriasJson = JSONArray(categories).toString(),
                onCategoriasActualizadas = { json ->
                    val list = try {
                        val arr = JSONArray(json)
                        (0 until arr.length()).map { arr.getString(it) }
                    } catch (e: Exception) { emptyList<String>() }
                    onUpdateCategories(list)
                },
                allCategories = allCategories
            )
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    SuggestionChip(onClick = {}, label = { Text(cat) })
                }
            }
        }
    }
}

@Composable
private fun LockedSectionInfo(colors: com.example.myapplication.prestador.ui.theme.PrestadorColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.border.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Business, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Text(
            "Estos datos comerciales están bloqueados porque tienes el Modo Empresa activo. Adminístralos desde el perfil de tu empresa.",
            fontSize = 11.sp,
            color = colors.textSecondary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun SwitchRowLegacy(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ProfileSkeletonScreen(padding: PaddingValues, colors: com.example.myapplication.prestador.ui.theme.PrestadorColors) {
    // Implementación del shimmer (Simulado para brevedad)
    Box(Modifier.fillMaxSize().padding(padding).background(colors.backgroundColor)) {
        CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.primaryOrange)
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Datos Personales")
fun PreviewPersonalDataSection() {
    val colors = getPrestadorColors()
    PersonalDataSection(
        isEditMode = false,
        uiState = com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState(),
        provider = com.example.myapplication.core.domain.model.Provider(
            uid = "123",
            displayName = "Juan Perez",
            name = "Juan",
            lastName = "Perez",
            email = "juan@test.com",
            phoneNumber = "12345678",
            profesion = "Plomero Elite"
        ),
        colors = colors,
        onUpdateState = {}
    )
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Servicios - Modo Personal")
fun PreviewServicesSection() {
    val colors = getPrestadorColors()
    ServicesToggleSection(
        isEditMode = true,
        uiState = com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState(doesHomeVisits = true, works24h = true),
        provider = com.example.myapplication.core.domain.model.Provider(uid = "123", displayName = "Juan", name = "Juan", lastName = "Perez", email = "", phoneNumber = ""),
        colors = colors,
        onUpdateState = {},
        isLocked = false
    )
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Servicios - Bloqueado por Empresa")
fun PreviewServicesSectionLocked() {
    val colors = getPrestadorColors()
    ServicesToggleSection(
        isEditMode = true,
        uiState = com.example.myapplication.prestador.viewmodel.profile.EditProfileUiState(),
        provider = com.example.myapplication.core.domain.model.Provider(uid = "123", displayName = "Juan", name = "Juan", lastName = "Perez", email = "", phoneNumber = ""),
        colors = colors,
        onUpdateState = {},
        isLocked = true
    )
}

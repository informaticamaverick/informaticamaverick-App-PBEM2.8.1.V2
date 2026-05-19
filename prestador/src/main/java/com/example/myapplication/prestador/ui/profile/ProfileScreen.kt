package com.example.myapplication.prestador.ui.profile

import android.text.Layout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState
import com.example.myapplication.prestador.viewmodel.calendar.AvailabilityViewModel
import com.example.myapplication.prestador.data.local.entity.toDayAbbr
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Snackbar
import com.example.myapplication.prestador.viewmodel.profile.UpdateState
import androidx.compose.material.icons.filled.Save
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.ui.components.AddressBottomSheet
import com.example.myapplication.prestador.ui.components.AddressProviderCard
import com.example.myapplication.prestador.ui.components.BeActionsBar
import com.example.myapplication.prestador.ui.components.PrestadorAction
import com.example.myapplication.prestador.ui.profile.CategoriasSelector
import org.w3c.dom.Text


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onNavigateToCalendarioConfig: () -> Unit = {},
    onNavigateToCalendarioConfigEntity: (ownerId: String, ownerName: String) -> Unit = { _, _ -> },
    viewModel: EditProfileViewModel = hiltViewModel(),
    scheduleVm: AvailabilityViewModel = hiltViewModel()
){
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider
    val refreshTick by viewModel.refreshTick.collectAsState()
    val horarios by scheduleVm.schedules.collectAsState()
    val listState = rememberLazyListState()
    val isEditMode by viewModel.isEditMode.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    LaunchedEffect(refreshTick) {
        if (refreshTick > 0) isRefreshing = false
    }
    val updateState by viewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val servicios by viewModel.servicios.collectAsState()
    val loadingServicios by viewModel.loadingServicios.collectAsState()

    //Form fields para modo edición
    var editName by remember { mutableStateOf("") }
    var editApellido by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editDniCuit by remember { mutableStateOf("") }
    var editProfesion by remember { mutableStateOf("") }
    var editMatricula by remember { mutableStateOf("") }
    var editTieneMatricula by remember { mutableStateOf(false) }
    var editDescription by remember { mutableStateOf("") }
    var editCategorias by remember { mutableStateOf("[]") }
    var showCompanyView by remember { mutableStateOf(false) }
    var showEmpresaSheet by remember { mutableStateOf(false) }
    var empresaPendiente by remember { mutableStateOf<CompanyProvider?>(null) }
    var showPriorizarDialog by remember { mutableStateOf(false) }

    //Inicializar campos al entrar en modo edición
    LaunchedEffect(isEditMode) {
        if (isEditMode && provider != null) {
            editName = provider.name
            editApellido = provider.apellido ?: ""
            editPhone = provider.phone
            editEmail = provider.email
            editDniCuit = provider.dniCuit ?: ""
            editProfesion = provider.profesion ?: ""
            editMatricula = provider.matricula ?: ""
            editTieneMatricula = provider.tieneMatricula
            editDescription = provider.description ?: ""
            editCategorias = try { org.json.JSONArray(provider.categories).toString() } catch (e: Exception) { "[]" }
        }
    }

    // Observar resultado del guardado
    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is UpdateState.Success -> {
                viewModel.setEditMode(false)
                viewModel.resetUpdateState()
                scope.launch { snackbarHostState.showSnackbar("Perfil actualizado ✓") }
            }
            is UpdateState.Error -> {
                viewModel.resetUpdateState()
                scope.launch { snackbarHostState.showSnackbar("Error: ${s.message}") }
            }
            else -> Unit
        }
    }

    //Pickers de imagen
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? ->
        uri?.let { viewModel.uploadBannerPhoto(it)}
    }

    val galleryImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? ->
        uri?.let { viewModel.addGalleryImage(it) }
    }

    val companyPhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri: Uri? ->
        uri?.let {
            val company = (viewModel.profileState.value as?
                    ProfileState.Success)?.provider?.companies?.firstOrNull()
            if (company != null) viewModel.addCompany(company, uri)
        }
    }
    val companyBannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.updateCompanyBannerPhoto(it) }
    }

    // Inicializar showCompanyView solo la primera vez que provider carga
    LaunchedEffect(provider?.id) {
        if (provider != null && !showCompanyView) {
            showCompanyView = provider.hasCompanyProfile &&
                provider.priorizarEmpresa == true &&
                provider.companies.isNotEmpty()
        }
    }
    var showServicioProviderDialog by remember { mutableStateOf(false) }
    var showHorariosDialog by remember { mutableStateOf(false) }
    var editProviderDoesService by remember { mutableStateOf(provider?.doesService ?: false) }
    var editProviderDoesProduct by remember { mutableStateOf(provider?.doesProduct ?: false) }

    var editProviderWorks24h by remember { mutableStateOf(provider?.atencionUrgencias ?: false) }
    var editProviderTurnosLocal by remember { mutableStateOf(provider?.turnosEnLocal ?: false) }
    var editProviderVaDomicilio by remember { mutableStateOf(provider?.vaDomicilio ?: false) }
    var editProviderEnvios by remember { mutableStateOf(provider?.envios ?: false) }
    var editProviderAcceptsTurnos by remember { mutableStateOf(provider?.acceptsAppointments ?: false) }

    // Carga el perfil desde Firestore al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    // Sincronizar toggles solo la primera vez que provider carga (Room es inmediato)
    // providerInitialized evita que el refresh de Firebase en background pise cambios del usuario
    val providerInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(provider) {
        if (provider != null && !providerInitialized.value) {
            providerInitialized.value = true
            editProviderDoesService = provider.doesService
            editProviderDoesProduct = provider.doesProduct
            editProviderWorks24h = provider.atencionUrgencias
            editProviderTurnosLocal = provider.turnosEnLocal
            editProviderVaDomicilio = provider.vaDomicilio
            editProviderEnvios = provider.envios
            editProviderAcceptsTurnos = provider.acceptsAppointments
        }
    }
    // Local/taller
    var localDireccion by remember(provider) { mutableStateOf(provider?.direccionLocal ?: "") }
    var localProvincia by remember(provider) { mutableStateOf(provider?.provinciaLocal ?: "") }
    var localCp by remember(provider) { mutableStateOf(provider?.codigoPostalLocal ?: "") }
    var localHorario by remember(provider) { mutableStateOf(provider?.horarioLocal ?: "") }

    val topBarAlpha by animateFloatAsState(
        targetValue = if (listState.firstVisibleItemIndex > 0) 1f
                      else (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f),
        label = "topBarAlpha"
    )

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

    Scaffold(
        containerColor = colors.backgroundColor,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colors.primaryOrange,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = {},
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = topBarAlpha },
                color = colors.surfaceColor,
                shadowElevation = if (topBarAlpha > 0.01f) 4.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.primaryOrange)
                    }
                    Text(
                        "Mi Perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(Modifier.weight(1f))

                    if (!isEditMode) {
                        IconButton(onClick = { viewModel.setEditMode(true)}) {
                            Icon(Icons.Default.Edit, null, tint = colors.primaryOrange)
                        }
                    } else {
                        IconButton(onClick = { viewModel.setEditMode(false)}) {
                            Icon(Icons.Default.Close, null, tint = colors.textSecondary)
                        }
                        IconButton(onClick = {
                            viewModel.updateProfile(
                                name = editName,
                                apellido = editApellido,
                                email = editEmail,
                                phone = editPhone,
                                dniCuit = editDniCuit,
                                profesion = editProfesion,
                                tieneMatricula = editTieneMatricula,
                                matricula = if (editTieneMatricula) editMatricula else null,
                                description = editDescription
                            )
                        }){
                            Icon(Icons.Default.Check, null, tint = colors.primaryOrange)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (provider == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primaryOrange)
            }
            return@Scaffold
        }

        val firstCompany = provider.companies.firstOrNull()

        if (showCompanyView && firstCompany != null) {
            CompanyDetailView(
                company = firstCompany,
                providerImageUrl = provider.imageUrl,
                paddingValues = padding,
                onBack = { showCompanyView = false },
                colors = colors,
                onNavigateToCalendarioConfig = onNavigateToCalendarioConfigEntity,
                onUpdateBranch = { updatedBranch ->
                    val updatedCompany = firstCompany.copy(
                        branches = firstCompany.branches.map {
                            if (it.id == updatedBranch.id) updatedBranch else it
                        }
                    )
                    viewModel.addCompany(updatedCompany)
                },
                onUpdateAllBranches = { updatedBranches ->
                    val updatedCompany = firstCompany.copy(branches = updatedBranches)
                    viewModel.addCompany(updatedCompany)
                },
                onAddBranch = { newBranch, esCasaCentral ->
                    val updatedBranches = if (esCasaCentral) {
                        listOf(newBranch) + firstCompany.branches
                    } else {
                        firstCompany.branches + newBranch
                    }
                    viewModel.addCompany(firstCompany.copy(branches = updatedBranches))
                },
                onUpdateCompany = { updatedCompany ->
                    viewModel.addCompany(updatedCompany)
                },
                onDeleteCompany = {
                    viewModel.removeCompany(firstCompany.id)
                },
                onEditCompanyPhoto = { companyPhotoLauncher.launch("image/*") },
                onEditCompanyBanner = { companyBannerLauncher.launch("image/*") },
            )
            return@Scaffold
        }

        val beActions = if (!isEditMode) {
            listOf(
                PrestadorAction("edit", Icons.Default.Edit, "Editar", tint = colors.primaryOrange) { viewModel.setEditMode(true) },
                PrestadorAction("divider_1", Icons.Default.Edit, ""),
                PrestadorAction("settings", Icons.Default.Settings, "Ajustes", tint = colors.primaryOrange, onClick = onSettings)
            )
        } else {
            listOf(
                PrestadorAction("cancel", Icons.Default.Close, "Cancelar", tint = Color(0xFFFF5252)) { viewModel.setEditMode(false) },
                PrestadorAction("divider_1", Icons.Default.Edit, ""),
                PrestadorAction("save", Icons.Default.Save, "Guardar", tint = colors.primaryOrange) {
                    viewModel.updateProfile(
                        name = editName,
                        apellido = editApellido,
                        email = editEmail,
                        phone = editPhone,
                        dniCuit = editDniCuit,
                        profesion = editProfesion,
                        tieneMatricula = editTieneMatricula,
                        matricula = if (editTieneMatricula) editMatricula else null,
                        description = editDescription
                    )
                },
                PrestadorAction("divider_2", Icons.Default.Edit, ""),
                PrestadorAction("empresa", Icons.Default.Business, "Empresa", tint = colors.primaryOrange) { showEmpresaSheet = true }
            )
        }

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refreshProfile()
            },
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
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
            // ── 1. ESPACIADO PARA HEADER COLAPSABLE ──────────────────────
            item { Spacer(Modifier.height(headerMaxHeight)) }

            item { Spacer(Modifier.height(16.dp)) }

            // ── 2. DATOS PROFESIONALES ───────────────────────────────────
            item {
                ProfileSectionCard(
                    icon = Icons.Default.Work,
                    title = "Datos Personales",
                    iconColor = colors.primaryOrange,
                    colors = colors,
                    actionButton = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showHorariosDialog = true }
                                .background(Color(0xFF7C3AED).copy(alpha = 0.10f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Horarios de Atención",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Horarios",
                                fontSize = 9.sp,
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 11.sp
                            )
                        }
                    }
                ) {
                    if (isEditMode) {
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.primaryOrange,
                            unfocusedLabelColor = colors.textSecondary,
                            cursorColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                        val fieldShape = RoundedCornerShape(10.dp)
                        val textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        val labelStyle = @Composable { label: String ->
                            Text(label, fontSize = 11.sp)
                        }

                        // Fila 1: Nombre + Apellido
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { labelStyle("Nombre") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = textStyle
                            )
                            OutlinedTextField(
                                value = editApellido,
                                onValueChange = { editApellido = it },
                                label = { labelStyle("Apellido") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = textStyle
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // Fila 2: DNI/CUIT + Teléfono
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editDniCuit,
                                onValueChange = { editDniCuit = it.filter { c -> c.isDigit() } },
                                label = { labelStyle("DNI / CUIT") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = textStyle,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it.filter { c -> c.isDigit() || c == '+' || c == '-' || c == ' ' } },
                                label = { labelStyle("Teléfono") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = textStyle,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // Fila 3: Email (solo lectura + Cambiar) + Profesión
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Email — solo lectura con botón Cambiar
                            var showEmailDialog by remember { mutableStateOf(false) }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = colors.surfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Email", fontSize = 10.sp, color = colors.textSecondary)
                                        Text(
                                            text = editEmail.ifBlank { "—" },
                                            fontSize = 13.sp,
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    TextButton(
                                        onClick = { showEmailDialog = true },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Cambiar", fontSize = 11.sp, color = colors.primaryOrange)
                                    }
                                }
                            }
                            if (showEmailDialog) {
                                CambiarEmailDialog(onDismiss = { showEmailDialog = false })
                            }
                            OutlinedTextField(
                                value = editProfesion,
                                onValueChange = { editProfesion = it.uppercase().filter { c -> c.isLetterOrDigit() || c == ' ' } },
                                label = { labelStyle("Profesión") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors,
                                textStyle = textStyle,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Ascii,
                                    capitalization = KeyboardCapitalization.Characters
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        // Toggle + campo matrícula en la misma fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Toggle "Tengo matrícula"
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.surfaceElevated)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Matrícula",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Switch(
                                    checked = editTieneMatricula,
                                    onCheckedChange = { editTieneMatricula = it },
                                    modifier = Modifier.scale(0.75f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.primaryOrange,
                                        uncheckedThumbColor = colors.textSecondary,
                                        uncheckedTrackColor = colors.border
                                    )
                                )
                            }
                            // Campo matrícula aparece si toggle activo
                            androidx.compose.animation.AnimatedVisibility(
                                visible = editTieneMatricula,
                                modifier = Modifier.weight(1f),
                                enter = androidx.compose.animation.expandHorizontally() + androidx.compose.animation.fadeIn(),
                                exit = androidx.compose.animation.shrinkHorizontally() + androidx.compose.animation.fadeOut()
                            ) {
                                OutlinedTextField(
                                    value = editMatricula,
                                    onValueChange = { editMatricula = it.uppercase() },
                                    label = { labelStyle("Nº Matrícula") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = fieldShape,
                                    colors = fieldColors,
                                    textStyle = textStyle,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Ascii,
                                        capitalization = KeyboardCapitalization.Characters
                                    )
                                )
                            }
                        }
                    } else {
                        if (!provider.profesion.isNullOrBlank()) {
                            ProfileInfoRow("🎓", "Profesión", provider.profesion!!, colors)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (!provider.dniCuit.isNullOrBlank()) {
                            ProfileInfoRow("🆔", "DNI / CUIT", provider.dniCuit!!, colors)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (provider.tieneMatricula && !provider.matricula.isNullOrBlank()) {
                            ProfileInfoRow("📜", "Matrícula", provider.matricula!!, colors)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (provider.email.isNotBlank()) {
                            ProfileInfoRow("📧", "Email", provider.email, colors)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (provider.phone.isNotBlank()) {
                            ProfileInfoRow("📞", "Teléfono", provider.phone, colors)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            // ── 3. SOBRE MÍ ──────────────────────────────────────────────
            if (!provider.description.isNullOrBlank() || isEditMode) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.Info, "Sobre mí", Color(0xFF3B82F6), colors) {
                        if (isEditMode) {
                            OutlinedTextField(
                                value = editDescription,
                                onValueChange = { editDescription = it },
                                label = { Text("Descripción", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = colors.border,
                                    focusedLabelColor = Color(0xFF3B82F6),
                                    unfocusedLabelColor = colors.textSecondary,
                                    cursorColor = Color(0xFF3B82F6),
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            )
                        } else {
                            Text(
                                provider.description!!,
                                fontSize = 14.sp,
                                color = colors.textSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // ── 3b. MIS DIRECCIONES ──────────────────────────────────────
            item {
                val mapsContext = androidx.compose.ui.platform.LocalContext.current
                var showAddressDialog by remember { mutableStateOf(false) }
                var editingAddress by remember { mutableStateOf<AddressProvider?>(null) }

                Spacer(Modifier.height(12.dp))
                ProfileSectionCard(
                    icon = Icons.Default.LocationOn,
                    title = "Mis Direcciones",
                    iconColor = colors.primaryOrange,
                    colors = colors,
                    actionButton = {
                        if (isEditMode) {
                            IconButton(onClick = {
                                editingAddress = null
                                showAddressDialog = true
                            }) {
                                Icon(Icons.Default.Add, null, tint = colors.primaryOrange)
                            }
                        }
                    }
                ) {
                    if (provider.addresses.isEmpty()) {
                        Text(
                            "Sin direcciones registradas",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        provider.addresses.forEachIndexed { index, addr ->
                            AddressProviderCard(
                                address = addr,
                                isEditMode = isEditMode,
                                onEdit = {
                                    editingAddress = addr
                                    showAddressDialog = true
                                },
                                onDelete = {
                                    viewModel.removeAdditionalAddress(addr.id)
                                },
                                onOpenMaps = {
                                    val query = buildString {
                                        append(addr.calle)
                                        if (addr.numero.isNotBlank()) append(" ${addr.numero}")
                                        if (addr.localidad.isNotBlank()) append(", ${addr.localidad}")
                                        if (addr.provincia.isNotBlank()) append(", ${addr.provincia}")
                                    }
                                    val encodedQuery = android.net.Uri.encode(query)
                                    val mapUri = android.net.Uri.parse(
                                        if (addr.latitude != null && addr.longitude != null && (addr.latitude != 0.0 || addr.longitude != 0.0))
                                            "geo:${addr.latitude},${addr.longitude}?q=$encodedQuery"
                                        else "geo:0,0?q=$encodedQuery"
                                    )
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW, mapUri
                                    )
                                    mapsContext.startActivity(intent)
                                }
                            )
                            if (index < provider.addresses.lastIndex) {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }

                if (showAddressDialog) {
                    AddressBottomSheet(
                        initial = editingAddress ?: AddressProvider(),
                        onDismiss = { showAddressDialog = false },
                        onSave = { addr ->
                            viewModel.saveAdditionalAddress(addr)
                            showAddressDialog = false
                        }
                    )
                }
            }


            // ── 4. CATEGORÍAS ────────────────────────────────────────────
            item {
                Spacer(Modifier.height(12.dp))
                if (isEditMode) {
                    ProfileSectionCard(Icons.Default.Category, "Categorías", Color(0xFF00897B), colors) {
                        CategoriasSelector(
                            categoriasJson = editCategorias,
                            onCategoriasActualizadas = { json ->
                                editCategorias = json
                                viewModel.updateCategorias(json)
                            },
                            serviciosFirebase = servicios
                        )
                    }
                } else if (provider.categories.isNotEmpty()) {
                    ProfileSectionCard(Icons.Default.Category, "Categorías", Color(0xFF00897B), colors) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            provider.categories.forEach { cat ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF00897B).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFF00897B).copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        cat,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        color = Color(0xFF00897B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            //-5 Servicios activos
            val allServices = listOf(
                Triple(Icons.Default.Build,         "Realiza servicios",
                    editProviderDoesService),
                Triple(Icons.Default.ShoppingBag,   "Vende productos",
                    editProviderDoesProduct),
                Triple(Icons.Default.CalendarMonth, "Acepta turnos",
                    editProviderAcceptsTurnos),
                Triple(Icons.Default.DirectionsCar, "Atención a domicilio",
                    editProviderVaDomicilio),
                Triple(Icons.Default.LocalShipping, "Realiza envíos",
                    editProviderEnvios),
                Triple(Icons.Default.Warning,       "Urgencias 24hs",
                    editProviderWorks24h),
                Triple(Icons.Default.Store,         "Atención en local",
                    editProviderTurnosLocal),
                Triple(Icons.Default.Group,         "Trabaja con equipo",
                    provider.trabajaConOtros)
            )
            val activeServices = allServices.filter { it.third }
            item {
                Spacer(Modifier.height(12.dp))
                if (showServicioProviderDialog) {
                    AlertDialog(
                        onDismissRequest = { showServicioProviderDialog = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        doesService = editProviderDoesService,
                                        doesProduct = editProviderDoesProduct,
                                        atencionUrgencias = editProviderWorks24h,
                                        turnosEnLocal = editProviderTurnosLocal,
                                        vaDomicilio = editProviderVaDomicilio,
                                        envios = editProviderEnvios,
                                        acceptsAppointments = editProviderAcceptsTurnos)
                                    showServicioProviderDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                            ) { Text("Guardar")}
                        },
                        dismissButton = {
                            TextButton(onClick = { showServicioProviderDialog = false }) { Text("Cancelar") }
                        },
                        title = { Text("Servicios que ofrezco", fontWeight = FontWeight.Bold)},
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                BranchServicioSwitch("Realiza servicios",
                                    Icons.Default.Build,         editProviderDoesService)    {
                                    editProviderDoesService = it }
                                BranchServicioSwitch("Vende productos",
                                    Icons.Default.ShoppingBag,   editProviderDoesProduct)    {
                                    editProviderDoesProduct = it }
                                BranchServicioSwitch("Acepta turnos",
                                    Icons.Default.CalendarMonth, editProviderAcceptsTurnos)  {
                                    editProviderAcceptsTurnos = it }
                                BranchServicioSwitch("Visitas a domicilio",
                                    Icons.Default.DirectionsCar, editProviderVaDomicilio)    {
                                    editProviderVaDomicilio = it }
                                BranchServicioSwitch("Realiza envíos",
                                    Icons.Default.LocalShipping, editProviderEnvios)         {
                                    editProviderEnvios = it }
                                BranchServicioSwitch("Urgencias 24hs",
                                    Icons.Default.Warning,       editProviderWorks24h)       {
                                    editProviderWorks24h = it }
                                BranchServicioSwitch("Atención en local",
                                    Icons.Default.Store,         editProviderTurnosLocal)    {
                                    editProviderTurnosLocal = it }
                            }
                        }
                    )
                }
                // ── POPUP HORARIOS DE ATENCIÓN ───────────────────────────
                if (showHorariosDialog) {
                    AlertDialog(
                        onDismissRequest = { showHorariosDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Horarios de Atención", fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (horarios.isEmpty()) {
                                    Text(
                                        "Sin horarios configurados",
                                        fontSize = 13.sp,
                                        color = colors.textSecondary
                                    )
                                } else {
                                    val agrupados = horarios.groupBy {
                                        Triple(it.startTime, it.endTime, it.appointmentDuration)
                                    }
                                    agrupados.forEach { (_, grupo) ->
                                        val dias = grupo.sortedBy { it.dayOfWeek }
                                            .distinctBy { it.dayOfWeek }
                                            .joinToString(" · ") { it.dayOfWeek.toDayAbbr() }
                                        val primero = grupo.first()
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF7C3AED).copy(alpha = 0.07f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Schedule,
                                                    null,
                                                    Modifier.size(16.dp),
                                                    tint = Color(0xFF7C3AED)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        dias,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF7C3AED)
                                                    )
                                                    Text(
                                                        "${primero.startTime} - ${primero.endTime}  ·  ${primero.appointmentDuration} min",
                                                        fontSize = 12.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showHorariosDialog = false
                                    onNavigateToCalendarioConfig()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Icon(Icons.Default.Settings, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Configurar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showHorariosDialog = false }) { Text("Cerrar") }
                        }
                    )
                }
                ProfileSectionCard(Icons.Default.Build, "Servicios", Color(0xFF3B82F6), colors) {
                    if (activeServices.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeServices.forEach { (icon, label, _) ->
                                Surface(
                                    
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(icon, null, Modifier.size(14.dp), tint = Color(0xFF3B82F6))
                                        Spacer(Modifier.width(5.dp))
                                        Text(label, fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    } else {
                        Text("No hay servicios configurados", fontSize = 13.sp, color = colors.textSecondary)
                        Spacer(Modifier.height(8.dp))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { showServicioProviderDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6)),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Settings, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Configurar", fontSize = 12.sp)
                        }
                    }
                }
            }

            // ── 6. LOCAL / TALLER ────────────────────────────────────────
            if (editProviderTurnosLocal) {
                item {
                    Spacer(Modifier.height(12.dp))
                    val localScope = rememberCoroutineScope()
                    val localContext = androidx.compose.ui.platform.LocalContext.current
                    var localEditando by remember { mutableStateOf(false) }
                    var localGeoLoading by remember { mutableStateOf(false) }
                    var localLocalidad by remember(provider) { mutableStateOf(provider?.addresses?.find { it.id == "local" }?.localidad ?: provider?.address?.localidad ?: "") }
                    var localCalle by remember(provider) { mutableStateOf(provider?.addresses?.find { it.id == "local" }?.calle ?: provider?.address?.calle ?: "") }
                    var localNumero by remember(provider) { mutableStateOf(provider?.addresses?.find { it.id == "local" }?.numero ?: provider?.address?.numero ?: "") }
                    var mostrarSugerenciasProvincia by remember { mutableStateOf(false) }
                    var mostrarSugerenciasLocalidad by remember { mutableStateOf(false) }
                    var geocodedLat by remember { mutableStateOf<Double?>(null) }
                    var geocodedLng by remember { mutableStateOf<Double?>(null) }

                    val provinciasFiltradas = if (localProvincia.isBlank()) emptyList()
                        else PROVINCIAS_ARGENTINA.filter { it.contains(localProvincia.trim(), ignoreCase = true) }
                    val localidadesDeProvincia = LOCALIDADES_POR_PROVINCIA.entries
                        .firstOrNull { it.key.equals(localProvincia.trim(), ignoreCase = true) }?.value ?: emptyList()
                    val localidadesFiltradas = if (localLocalidad.isBlank()) localidadesDeProvincia
                        else localidadesDeProvincia.filter { it.nombre.contains(localLocalidad.trim(), ignoreCase = true) }

                    val locationPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) {
                            localScope.launch {
                                localGeoLoading = true
                                try {
                                    val fusedClient = com.google.android.gms.location.LocationServices
                                        .getFusedLocationProviderClient(localContext)
                                    @Suppress("MissingPermission")
                                    var loc = fusedClient.lastLocation.await()
                                    if (loc == null) {
                                        loc = fusedClient.getCurrentLocation(
                                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                            null
                                        ).await()
                                    }
                                    if (loc != null) {
                                        val geocoder = android.location.Geocoder(localContext, java.util.Locale.getDefault())
                                        val addrs: List<android.location.Address>? =
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                                                    geocoder.getFromLocation(loc.latitude, loc.longitude, 1, object : android.location.Geocoder.GeocodeListener {
                                                        override fun onGeocode(results: MutableList<android.location.Address>) { cont.resumeWith(Result.success(results)) }
                                                        override fun onError(errorMessage: String?) { cont.resumeWith(Result.success(emptyList())) }
                                                    })
                                                }
                                            } else {
                                                @Suppress("DEPRECATION")
                                                geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                                            }
                                        if (!addrs.isNullOrEmpty()) {
                                            val a = addrs[0]
                                            if (!a.thoroughfare.isNullOrBlank()) localCalle = a.thoroughfare!!
                                            if (!a.subThoroughfare.isNullOrBlank()) localNumero = a.subThoroughfare!!
                                            if (!a.locality.isNullOrBlank()) localLocalidad = a.locality!!
                                            if (!a.adminArea.isNullOrBlank()) localProvincia = a.adminArea!!
                                            if (!a.postalCode.isNullOrBlank()) localCp = a.postalCode!!
                                            geocodedLat = loc.latitude
                                            geocodedLng = loc.longitude
                                        }
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    localGeoLoading = false
                                }
                            }
                        }
                    }

                    ProfileSectionCard(
                        icon = Icons.Default.Store,
                        title = "Local / Taller",
                        iconColor = Color(0xFFE53935),
                        colors = colors,
                        actionButton = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showHorariosDialog = true }
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.10f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Horarios de Atención",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Horarios",
                                    fontSize = 9.sp,
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    ) {
                        if (!localEditando) {
                            // Modo lectura
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val dir = listOfNotNull(
                                    buildString {
                                        if (localCalle.isNotBlank()) append(localCalle.trim())
                                        if (localNumero.isNotBlank()) append(" ${localNumero.trim()}")
                                    }.ifBlank { null },
                                    localLocalidad.ifBlank { null },
                                    localProvincia.ifBlank { null },
                                    localCp.takeIf { it.isNotBlank() }?.let { "CP $it" }
                                ).joinToString(", ")
                                if (dir.isNotBlank()) {
                                    ProfileInfoRow("📍", "Dirección", dir, colors)
                                    Spacer(Modifier.height(4.dp))
                                }
                                if (localHorario.isNotBlank()) {
                                    ProfileInfoRow("🕐", "Horario", localHorario, colors)
                                    Spacer(Modifier.height(4.dp))
                                }
                                if (dir.isBlank() && localHorario.isBlank()) {
                                    Text("Sin datos cargados", fontSize = 13.sp, color = colors.textSecondary)
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(
                                    onClick = { localEditando = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Editar", fontSize = 12.sp)
                                }
                            }
                        } else {
                            // Modo edición
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                // Provincia con autocomplete
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField(
                                        value = localProvincia,
                                        onValueChange = { localProvincia = it; mostrarSugerenciasProvincia = true },
                                        label = "Provincia",
                                        leadingIcon = Icons.Default.Place
                                    )
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = mostrarSugerenciasProvincia && provinciasFiltradas.isNotEmpty(),
                                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                                    ) {
                                        Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                                            Column {
                                                provinciasFiltradas.take(5).forEach { prov ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                localProvincia = prov
                                                                mostrarSugerenciasProvincia = false
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                                    ) { Text(prov, fontSize = 13.sp) }
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                    }
                                }

                                // Localidad con autocomplete (autocompleta CP)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField(
                                        value = localLocalidad,
                                        onValueChange = { localLocalidad = it; mostrarSugerenciasLocalidad = true },
                                        label = "Localidad",
                                        leadingIcon = Icons.Default.LocationCity
                                    )
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = mostrarSugerenciasLocalidad && localidadesFiltradas.isNotEmpty(),
                                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                                    ) {
                                        Surface(shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp), shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                                            Column {
                                                localidadesFiltradas.take(5).forEach { loc ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                localLocalidad = loc.nombre
                                                                localCp = loc.codigoPostal
                                                                mostrarSugerenciasLocalidad = false
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(loc.nombre, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                                        Text(loc.codigoPostal, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                    }
                                }

                                // CP
                                com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField(
                                    value = localCp,
                                    onValueChange = { localCp = it },
                                    label = "Código Postal",
                                    leadingIcon = Icons.Default.PinDrop,
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Text
                                )

                                // Calle con botón GPS
                                OutlinedTextField(
                                    value = localCalle,
                                    onValueChange = { localCalle = it },
                                    label = { Text("Calle") },
                                    leadingIcon = { Icon(Icons.Default.EditRoad, null, tint = colors.textSecondary) },
                                    trailingIcon = {
                                        if (localGeoLoading) {
                                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primaryOrange)
                                        } else {
                                            IconButton(onClick = {
                                                locationPermLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                            }) {
                                                Icon(Icons.Default.MyLocation, "Detectar ubicación", tint = colors.primaryOrange)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primaryOrange,
                                        focusedLabelColor = colors.primaryOrange,
                                        unfocusedBorderColor = colors.border
                                    )
                                )

                                // Número
                                com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField(
                                    value = localNumero,
                                    onValueChange = { localNumero = it },
                                    label = "Número",
                                    leadingIcon = Icons.Default.Numbers,
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )

                                // Botón geocodificar
                                OutlinedButton(
                                    onClick = {
                                        localScope.launch {
                                            localGeoLoading = true
                                            try {
                                                val geocoder = android.location.Geocoder(localContext, java.util.Locale.getDefault())
                                                val query = "$localCalle $localNumero, $localLocalidad, $localProvincia, Argentina"
                                                val results: List<android.location.Address>? =
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                                        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                                                            geocoder.getFromLocationName(query, 1, object : android.location.Geocoder.GeocodeListener {
                                                                override fun onGeocode(r: MutableList<android.location.Address>) { cont.resumeWith(Result.success(r)) }
                                                                override fun onError(errorMessage: String?) { cont.resumeWith(Result.success(emptyList())) }
                                                            })
                                                        }
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        geocoder.getFromLocationName(query, 1)
                                                    }
                                                if (!results.isNullOrEmpty()) {
                                                    val r = results[0]
                                                    geocodedLat = r.latitude
                                                    geocodedLng = r.longitude
                                                    if (!r.postalCode.isNullOrBlank() && localCp.isBlank())
                                                        localCp = r.postalCode!!
                                                }
                                            } catch (_: Exception) {
                                            } finally {
                                                localGeoLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, colors.primaryOrange)
                                ) {
                                    Icon(Icons.Default.MyLocation, null, Modifier.size(16.dp), tint = colors.primaryOrange)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        if (geocodedLat != null) "✓ Ubicación confirmada" else "Confirmar ubicación en mapa",
                                        fontSize = 13.sp,
                                        color = if (geocodedLat != null) Color(0xFF4CAF50) else colors.primaryOrange
                                    )
                                }

                                // Horario
                                HorarioSelectorField(
                                    horario = localHorario,
                                    onHorarioChange = { localHorario = it }
                                )

                                // Botones Cancelar / Guardar
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { localEditando = false },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                                        border = BorderStroke(1.dp, colors.border)
                                    ) { Text("Cancelar", fontSize = 12.sp) }
                                    Button(
                                        onClick = {
                                            localScope.launch {
                                                localGeoLoading = true
                                                try {
                                                    val fullCalle = "$localCalle $localNumero".trim()
                                                    viewModel.updateProfile(
                                                        turnosEnLocal = true,
                                                        direccionLocal = fullCalle,
                                                        provinciaLocal = localProvincia,
                                                        codigoPostalLocal = localCp,
                                                        horarioLocal = localHorario,
                                                        latitud = geocodedLat,
                                                        longitud = geocodedLng
                                                    )
                                                    localEditando = false
                                                } finally {
                                                    localGeoLoading = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        enabled = !localGeoLoading
                                    ) {
                                        if (localGeoLoading) {
                                            CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text("Guardar", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 6. GALERÍA ───────────────────────────────────────────────
            if (provider.galleryImages.isNotEmpty() || isEditMode) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.PhotoLibrary, "Galería de trabajos", Color(0xFFF59E0B), colors) {
                        if (isEditMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { galleryImageLauncher.launch("image/*")},
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Agregar foto", fontSize = 13.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (provider.galleryImages.isEmpty()) {
                            Text(
                                "Sin fotos de trabajos aún",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(provider.galleryImages) { imageData ->
                                    val model: Any = if (imageData.startsWith("http")) imageData
                                    else try {
                                        val bytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: imageData
                                    } catch (e: Exception) { imageData }
                                    Box {
                                        AsyncImage(
                                            model = model,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (isEditMode) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF5252))
                                                    .clickable {
                                                        viewModel.removeGalleryImage(imageData)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Close, null, Modifier.size(12.dp), tint = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 7. ESPACIADO FINAL ───────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
        } // cierra LazyColumn

        // ── HEADER COLAPSABLE (overlay) ───────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .align(Alignment.TopStart)
                .zIndex(10f)
        ) {
            ProfileBannerHeaderView(
                name = "${provider.name} ${provider.apellido}".trim(),
                profesion = provider.profesion ?: "",
                imageUrl = provider.imageUrl,
                bannerImageUrl = provider.bannerImageUrl,
                rating = provider.rating,
                isSubscribed = provider.suscripto,
                isVerified = provider.verificado,
                paddingValues = padding,
                onBack = onBack,
                toggleImageUrl = firstCompany?.photoUrl,
                onToggle = if (firstCompany != null) ({ showCompanyView = true }) else null,
                colors = colors,
                isEditMode = isEditMode,
                onEditPhoto = { photoLauncher.launch("image/*") },
                onEditBanner = { bannerLauncher.launch("image/*") },
                collapseFraction = collapseFraction,
                avatarSizeDp = avatarSize
            )
        }

        BeActionsBar(
            visible = true,
            actions = beActions,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
        )

        // ── SHEET CREAR EMPRESA ──────────────────────────────────────────
        if (showEmpresaSheet) {
            EmpresaBottomSheet(
                colors = colors,
                onDismiss = { showEmpresaSheet = false },
                onAceptar = { nuevaEmpresa ->
                    empresaPendiente = nuevaEmpresa
                    showEmpresaSheet = false
                    showPriorizarDialog = true
                }
            )
        }
            if (showPriorizarDialog && empresaPendiente != null) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.addCompany(empresaPendiente!!)
                        empresaPendiente = null
                        showPriorizarDialog = false
                    },
                    title = {Text("Perfil de empresa", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Querés mostrar la empresa como tu perfil principal?")},
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.addCompany(empresaPendiente!!, priorizarEmpresa = true)
                                empresaPendiente = null
                                showPriorizarDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                        ) { Text("Sí")}
                    },
                    dismissButton = {
                            TextButton(
                                onClick = {
                                    viewModel.addCompany(empresaPendiente!!, priorizarEmpresa = false)
                                    empresaPendiente = null
                                    showPriorizarDialog = false
                                }
                            ) { Text("No por ahora") }
                    }
                )
            }

        } // cierra Box
        } // cierra PullToRefreshBox
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CambiarEmailDialog(onDismiss: () -> Unit) {
        val colors = getPrestadorColors()
        var newEmail by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var success by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Cambiar Email", fontWeight = FontWeight.Bold) },
            text = {
                if (success) {
                    Text("Email actualizado correctamente.", color = Color(0xFF10B981))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Nuevo email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña actual") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        if (error != null) {
                            Text(error!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (success) {
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                } else {
                    Button(
                        onClick = {
                            if (newEmail.isBlank() || password.isBlank()) {
                                error = "Completá todos los campos"
                                return@Button
                            }
                            isLoading = true
                            error = null
                            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            val credential = com.google.firebase.auth.EmailAuthProvider
                                .getCredential(user?.email ?: "", password)
                            user?.reauthenticate(credential)?.addOnCompleteListener { reauth ->
                                if (reauth.isSuccessful) {
                                    user.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener { update ->
                                        isLoading = false
                                        if (update.isSuccessful) success = true
                                        else error = update.exception?.message ?: "Error al actualizar"
                                    }
                                } else {
                                    isLoading = false
                                    error = "Contraseña incorrecta"
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Confirmar")
                    }
                }
            },
            dismissButton = {
                if (!success) TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        )
}


package com.example.myapplication.prestador.ui.profile

import android.text.Layout
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Arrangement
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
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.viewmodel.AvailabilityViewModel
import com.example.myapplication.prestador.data.local.entity.toDayAbbr

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSettings: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel(),
    scheduleVm: AvailabilityViewModel = hiltViewModel()
){
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider
    val horarios by scheduleVm.schedules.collectAsState()
    val listState = rememberLazyListState()
    var showCompanyView by remember { mutableStateOf(false) }
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
    var horarioToEdit by remember { mutableStateOf<com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity?>(null) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var showDeleteScheduleDialog by remember { mutableStateOf<List<com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity>?>(null) }
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
        targetValue = (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f),
        label = "topBarAlpha"
    )

    Scaffold(
        containerColor = colors.backgroundColor,
        bottomBar = {
            ProfileBottomBar(
                onEdit = onEditProfile,
                onSettings = onSettings,
                colors = colors
            )
        },
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
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Default.Edit, null, tint = colors.primaryOrange)
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
                }
            )
            return@Scaffold
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── 1. HEADER BANNER ─────────────────────────────────────────
            item {
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
                    onEdit = onEditProfile,
                    toggleImageUrl = firstCompany?.photoUrl,
                    onToggle = if (firstCompany != null) ({ showCompanyView = true }) else null,
                    colors = colors
                )
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ── 2. DATOS PROFESIONALES ───────────────────────────────────
            item {
                ProfileSectionCard(
                    icon = Icons.Default.Work,
                    title = "Datos Profesionales",
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
                    // Direcciones
                    val todasDirecciones = (listOfNotNull(provider.address)
                            +
                            provider.addresses.filter { it.id != "principal" })
                        .distinctBy { it.fullString() }

                    todasDirecciones.forEachIndexed { index, addr ->
                        ProfileInfoRow(
                            "📍",
                            if (index == 0) "Dirección" else "Otra dirección",
                            addr.fullString(),
                            colors
                        )
                        if (index < todasDirecciones.lastIndex) Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // ── 3. SOBRE MÍ ──────────────────────────────────────────────
            if (!provider.description.isNullOrBlank()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.Info, "Sobre mí", Color(0xFF3B82F6), colors) {
                        Text(
                            provider.description!!,
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // ── 4. CATEGORÍAS ────────────────────────────────────────────
            if (provider.categories.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
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
                                        envios = editProviderEnvios
                                    )
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
                                                // Editar
                                                IconButton(
                                                    onClick = {
                                                        horarioToEdit = primero
                                                        showAddScheduleDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        "Editar",
                                                        tint = Color(0xFF7C3AED),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                // Eliminar
                                                IconButton(
                                                    onClick = { showDeleteScheduleDialog = grupo },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        "Eliminar",
                                                        tint = Color(0xFFFF5252),
                                                        modifier = Modifier.size(16.dp)
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
                                    horarioToEdit = null
                                    showAddScheduleDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Agregar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showHorariosDialog = false }) { Text("Cerrar") }
                        }
                    )
                }
                // Dialog agregar/editar horario
                if (showAddScheduleDialog) {
                    AddScheduleDialog(
                        schedule = horarioToEdit,
                        onDismiss = {
                            showAddScheduleDialog = false
                            horarioToEdit = null
                        },
                        onConfirm = { days, startTime, endTime, duration, worksByAppointment, scheduleType ->
                            if (horarioToEdit != null) {
                                scheduleVm.updateSchedule(
                                    horarioToEdit!!.copy(
                                        dayOfWeek = days.first(),
                                        startTime = startTime,
                                        endTime = endTime,
                                        appointmentDuration = duration,
                                        worksByAppointment = worksByAppointment,
                                        scheduleType = scheduleType
                                    )
                                )
                            } else {
                                days.forEach { day ->
                                    scheduleVm.addSchedule(day, startTime, endTime, duration, worksByAppointment, scheduleType)
                                }
                            }
                            showAddScheduleDialog = false
                            horarioToEdit = null
                        },
                        colors = colors,
                        hasPhysicalLocation = editProviderTurnosLocal
                    )
                }
                // Dialog confirmar eliminación
                showDeleteScheduleDialog?.let { grupo ->
                    AlertDialog(
                        onDismissRequest = { showDeleteScheduleDialog = null },
                        title = { Text("Eliminar horario") },
                        text = { Text("¿Eliminás este horario?") },
                        confirmButton = {
                            TextButton(onClick = {
                                grupo.forEach { scheduleVm.deleteSchedule(it.id) }
                                showDeleteScheduleDialog = null
                            }) {
                                Text("Eliminar", color = Color(0xFFFF5252))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteScheduleDialog = null }) { Text("Cancelar") }
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

                    ProfileSectionCard(Icons.Default.Store, "Local / Taller", Color(0xFFE53935), colors) {
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
            if (provider.galleryImages.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    ProfileSectionCard(Icons.Default.PhotoLibrary, "Galería de trabajos", Color(0xFFF59E0B), colors) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(provider.galleryImages) { imageData ->
                                val model: Any = if (imageData.startsWith("http")) imageData
                                else try {
                                    val bytes = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: imageData
                                } catch (e: Exception) { imageData }
                                AsyncImage(
                                    model = model,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // ── 7. ESPACIADO FINAL ───────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── HEADER CON BANNER ────────────────────────────────────────────────────────
@Composable
private fun ProfileBannerHeaderView(
    name: String,
    profesion: String,
    imageUrl: String?,
    bannerImageUrl: String?,
    rating: Float,
    isSubscribed: Boolean,
    isVerified: Boolean,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    toggleImageUrl: String? = null,
    onToggle: (() -> Unit)? = null,
    colors: PrestadorColors
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        // Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            val bannerModel: Any? = bannerImageUrl?.takeIf { it.isNotEmpty() }
            when {
                bannerModel != null && (bannerModel as String).startsWith("http") -> {
                    AsyncImage(
                        model = bannerModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                bannerModel != null -> {
                    val bmp = remember(bannerModel) {
                        try {
                            val b = android.util.Base64.decode(bannerModel as String, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bmp != null) {
                        Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        BannerGradient(colors)
                    }
                }
                else -> BannerGradient(colors)
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
            // Botón volver (solo back, sin lápiz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(36.dp), CircleShape, Color.Black.copy(alpha = 0.35f)) {
                    IconButton(onClick = onBack, Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Foto de perfil (solapando banner)
        Box(
            modifier = Modifier
                .padding(top = 105.dp, start = 20.dp)
                .size(90.dp)
                .clip(CircleShape)
                .border(3.dp, colors.primaryOrange, CircleShape)
        ) {
            ProfilePhoto(imageUrl = imageUrl, colors = colors)
        }

        // Nombre + Profesión + Badges (abajo)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name.ifEmpty { "Prestador" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                if (isVerified) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Verified, null, Modifier.size(18.dp), Color(0xFF10B981))
                }
            }
            if (profesion.isNotEmpty()) {
                Text(profesion, fontSize = 13.sp, color = colors.textSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceColor,
                    border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f))
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, Modifier.size(12.dp), Color(0xFFFBBF24))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            String.format("%.1f", if (rating == 0f) 5.0f else rating),
                            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary
                        )
                    }
                }
                if (isSubscribed) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFBBF24).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WorkspacePremium, null, Modifier.size(12.dp), Color(0xFFFBBF24))
                            Spacer(Modifier.width(3.dp))
                            Text("Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                        }
                    }
                }
            }
        }

        // Botón toggle empresa (bottom-end) — solo si hay empresa
        if (onToggle != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { onToggle() }
            ) {
                ProfilePhoto(imageUrl = toggleImageUrl, colors = colors)
            }
        }
    }
}

@Composable
private fun BannerGradient(colors: PrestadorColors) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(
                    colors.primaryOrange.copy(alpha = 0.8f),
                    Color(0xFFFF5722).copy(alpha = 0.5f),
                    colors.backgroundColor
                )
            )
        )
    )
}

@Composable
private fun ProfilePhoto(imageUrl: String?, colors: PrestadorColors, isCompany: Boolean = false) {
    when {
        !imageUrl.isNullOrEmpty() && imageUrl.startsWith("http") -> {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        !imageUrl.isNullOrEmpty() -> {
            val bmp = remember(imageUrl) {
                try {
                    val b = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                } catch (e: Exception) { null }
            }
            if (bmp != null) {
                Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                PhotoPlaceholder(colors, isCompany)
            }
        }
        else -> PhotoPlaceholder(colors, isCompany)
    }
}

@Composable
private fun PhotoPlaceholder(colors: PrestadorColors, isCompany: Boolean = false) {
    Box(
        Modifier.fillMaxSize().background(colors.primaryOrange.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (isCompany) Icons.Default.Business else Icons.Default.Person,
            null,
            Modifier.size(28.dp),
            tint = colors.primaryOrange.copy(alpha = 0.5f)
        )
    }
}

// ── CARD SECCIÓN GENÉRICA ────────────────────────────────────────────────────
@Composable
private fun ProfileSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    colors: PrestadorColors,
    actionButton: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, Modifier.size(20.dp), iconColor)
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                if (actionButton != null) {
                    Spacer(Modifier.weight(1f))
                    actionButton()
                }
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

// ── FILA DE DATO CON EMOJI ───────────────────────────────────────────────────
@Composable
private fun ProfileInfoRow(
    emoji: String,
    label: String,
    value: String,
    colors: PrestadorColors
) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 14.sp, modifier = Modifier.width(26.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = colors.textPrimary)
        }
    }
}

// ── CARD EMPRESA CON SUCURSALES Y EQUIPO ─────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEmpresaCard(company: CompanyProvider, colors: PrestadorColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header empresa
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, null, Modifier.size(20.dp), Color(0xFF8B5CF6))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    company.name.ifBlank { "Empresa" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            if (company.razonSocial.isNotBlank()) {
                ProfileInfoRow("🏢", "Razón Social", company.razonSocial, colors)
                Spacer(Modifier.height(8.dp))
            }
            if (company.cuit.isNotBlank()) {
                ProfileInfoRow("🆔", "CUIT", company.cuit, colors)
                Spacer(Modifier.height(8.dp))
            }
            if (company.description.isNotBlank()) {
                Text(company.description, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
            }

            // Rubros empresa
            if (company.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    company.categories.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f))
                        ) {
                            Text(
                                cat,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Sucursales
            company.branches.forEachIndexed { index, branch ->
                BranchSection(index = index, branch = branch, colors = colors)
            }
        }
    }
}

@Composable
private fun BranchSection(index: Int, branch: BranchProvider, colors: PrestadorColors, onUpdateBranch: (BranchProvider) -> Unit = {}) {
    val branchName = branch.name.ifBlank { if (index == 0) "Casa Central" else "Sucursal ${index + 1}" }
    val dir = branch.address.fullString()

    // Título
    Text(branchName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    Spacer(Modifier.height(12.dp))

    if (dir.isNotBlank()) {
        BranchInfoRow(
            icon = Icons.Default.LocationOn,
            iconColor = Color(0xFFE53935),
            label = "Dirección",
            value = dir,
            colors = colors
        )
    }
    if (branch.workingHours.isNotBlank()) {
        if (dir.isNotBlank()) Spacer(Modifier.height(12.dp))
        BranchInfoRow(
            icon = Icons.Default.Schedule,
            iconColor = Color(0xFF8B5CF6),
            label = "Horario",
            value = branch.workingHours,
            colors = colors
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

    if (branch.employees.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("EQUIPO DE TRABAJO", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color =  colors.textSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(branch.employees) { emp ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!emp.photoUrl.isNullOrEmpty()) {
                            AsyncImage(emp.photoUrl, null,
                                Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {

                            Icon(Icons.Default.Person, null, Modifier.size(24.dp), Color(0xFF8B5CF6))

                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(emp.name, fontSize = 10.sp, color = colors.textPrimary,
                        textAlign = TextAlign.Center, maxLines = 1)
                    if(emp.position.isNotBlank()) {

                        Text(emp.position, fontSize = 9.sp, color = colors.textSecondary,
                            textAlign = TextAlign.Center, maxLines = 1)

                    }
                }
            }
        }
    }
    if (branch.galleryImages.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("GALERÍA DE ESTA SEDE", fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = colors.textSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(branch.galleryImages) { imageData ->
                val model: Any = if (imageData.startsWith("http")) imageData
                else try {
                    val b = android.util.Base64.decode(imageData, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size) ?: imageData
                } catch (e: Exception) { imageData }
                AsyncImage(model, null, Modifier.size(80.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop)
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
private fun CompanyDetailView(
    company: CompanyProvider,
    providerImageUrl: String?,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    colors: PrestadorColors,
    onUpdateBranch: (BranchProvider) -> Unit = {},
    onUpdateAllBranches: (List<BranchProvider>) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── HEADER EMPRESA ───────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(295.dp)
            ) {
                // Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
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
                            .padding(top = paddingValues.calculateTopPadding())
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Surface(Modifier.size(36.dp), CircleShape, Color.Black.copy(alpha = 0.35f)) {
                            IconButton(onClick = onBack, Modifier.fillMaxSize()) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Logo empresa (solapando banner)
                Box(
                    modifier = Modifier
                        .padding(top = 148.dp, start = 20.dp)
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFF8B5CF6), CircleShape)
                ) {
                    val logoUrl = company.photoUrl
                    when {
                        !logoUrl.isNullOrEmpty() && logoUrl.startsWith("http") -> {
                            AsyncImage(logoUrl, null, Modifier.fillMaxWidth(), contentScale = ContentScale.Crop)
                        }
                        !logoUrl.isNullOrEmpty() -> {
                            val bmp = remember(logoUrl) {
                                try {
                                    val b = android.util.Base64.decode(logoUrl, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                                } catch (e: Exception) { null }
                            }
                            if (bmp != null ) {
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
                }

                // Nombre empresa y razón social
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 10.dp)
                ) {
                    Text(
                        company.name.ifBlank { "Empresa" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (company.razonSocial.isNotBlank()) {
                        Text(company.razonSocial, fontSize = 13.sp, color = colors.textSecondary)
                    }
                }

                // Botón toggle: foto del prestador (volver a perfil personal)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .clickable { onBack() }
                ) {
                    ProfilePhoto(imageUrl = providerImageUrl, colors = colors, isCompany = true)
                }
            }
        }

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

            ProfileSectionCard(Icons.Default.Business, "Datos del Negocio", Color(0xFF8B5CF6), colors) {
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
                if (company.description.isNotBlank()) {
                    Text(company.description, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                    Spacer(Modifier.height(8.dp))
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
        if (company.branches.isNotEmpty()) {
            item {
                BranchesPager(branches = company.branches, colors = colors, onUpdateBranch = onUpdateBranch)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BranchesPager(
    branches: List<BranchProvider>,
    colors: PrestadorColors,
    onUpdateBranch: (BranchProvider) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { branches.size })

    Column {
        // Tabs con nombre de cada sucursal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            branches.forEachIndexed { index, branch ->
                val isSelected = pagerState.currentPage == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* handled by pager */},
                    shape = RoundedCornerShape(10.dp),
                    color = if(isSelected) Color(0xFF8B5CF6) else colors.surfaceColor,
                    border = if(isSelected) BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f)) else null
                ) {
                    if (index == 0) {
                        Text(
                            text = "Casa Central",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Sucursal",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = branch.name.ifBlank { "Sucursal ${index + 1}" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else colors.textSecondary.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

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
                    BranchSection(index = page, branch = branches[page], colors = colors, onUpdateBranch = onUpdateBranch)
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

// ── BARRA INFERIOR DE ACCIONES ───────────────────────────────────────────────
@Composable
private fun ProfileBottomBar(
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    colors: PrestadorColors
) {
    Surface(
        color = colors.surfaceColor,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileBottomBarButton(
                icon = Icons.Default.Edit,
                label = "EDITAR",
                tint = colors.primaryOrange,
                onClick = onEdit
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(colors.textSecondary.copy(alpha = 0.2f))
            )
            ProfileBottomBarButton(
                icon = Icons.Default.Settings,
                label = "AJUSTES",
                tint = colors.textPrimary,
                onClick = onSettings
            )
        }
    }
}

@Composable
private fun ProfileBottomBarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BranchServicioSwitch(
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

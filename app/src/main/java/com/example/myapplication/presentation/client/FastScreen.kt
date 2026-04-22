package com.example.myapplication.presentation.client

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

// ==========================================================================================
// --- PANTALLA FAST (STATEFUL - CONECTADA AL OBRERO UNIFICADO) ---
// ==========================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FastScreen(
    navController: NavHostController,
    bottomPadding: PaddingValues, // 🔥 Padding del HUD para evitar solapamientos
    profileViewModel: ProfileViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    // 🔥 LÓGICA MIGRADA AL OBRERO UNIFICADO
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userState by beViewModel.userState.collectAsStateWithLifecycle()

    // --- SECCIÓN: DATOS DE PROVEEDORES (CEREBRO) ---
    val unifiedServices by providerViewModel.unifiedServices.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    // --- SECCIÓN: CONEXIÓN AL CEREBRO (FUENTE DE VERDAD UBICACIÓN) ---
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val availableAddresses by beViewModel.availableAddressInfos.collectAsStateWithLifecycle()
    val weatherDesc by beViewModel.weatherDescription.collectAsStateWithLifecycle()
    
    // Estados de búsqueda Fast ahora en el Obrero
    val isSearching by ubicacionObrero.isSearchingFast.collectAsStateWithLifecycle()
    val searchFinished by ubicacionObrero.searchFinishedFast.collectAsStateWithLifecycle()
    val searchResults by ubicacionObrero.searchResultsFast.collectAsStateWithLifecycle()

    // Efecto de limpieza/reinicio al cambiar ubicación
    LaunchedEffect(activeAddress) {
        if (isSearching || searchFinished) {
            ubicacionObrero.resetBusquedaFast()
        }
    }

    FastScreenContent(
        navController = navController,
        bottomPadding = bottomPadding,
        userState = userState,
        allServices = unifiedServices,
        allCategories = categories,
        weatherDescription = weatherDesc,
        userLat = activeAddress?.lat ?: -26.8310,
        userLon = activeAddress?.lng ?: -65.2045,
        isSearching = isSearching,
        searchFinished = searchFinished,
        searchResults = searchResults,
        onStartSearch = { category, lat, lon ->
            ubicacionObrero.ejecutarBusquedaEmergenciaFast(category, unifiedServices, lat, lon)
        },
        onResetSearch = { ubicacionObrero.resetBusquedaFast() },
        // --- PROPS DE UBICACIÓN ---
        activeAddress = activeAddress,
        availableAddresses = availableAddresses,
        onAddressSelected = { addr -> beViewModel.selectAddress(addr.id) },
        onUpdateGps = { beViewModel.triggerAction("refresh_gps") },
        beViewModel = beViewModel
    )

    // ==================================================================================
    // --- 🧠 ESCUCHA DE ACCIONES DEL CEREBRO (ORQUESTACIÓN) ---
    // ==================================================================================
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "refresh_gps" -> {
                    ubicacionObrero.ejecutarCalculoUbicacionGps(context) { _, _, loc, calle, num, cp, lat, lng ->
                        val freshGpsAddress = AddressInfo(
                            id = "gps_current",
                            companyOrUserName = "Mi Ubicación",
                            branchName = "GPS Tracker",
                            streetAndNumber = if (calle.isNotBlank()) "$calle $num".trim() else "Ubicación detectada",
                            locality = loc,
                            postalCode = cp,
                            isCompany = false,
                            lat = lat,
                            lng = lng
                        )
                        beViewModel.updateAddressFromGps(freshGpsAddress)
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- PANTALLA FAST (STATELESS - UI LIMPIA SIN CABECERA DE DIRECCIÓN) ---
// ==========================================================================================

@Composable
fun FastScreenContent(
    navController: NavHostController,
    bottomPadding: PaddingValues, // 🔥 Para elevar la UI sobre las herramientas de Be
    userState: UserEntity?,
    allServices: List<ServiceDisplayModel>,
    allCategories: List<CategoryEntity>,
    weatherDescription: String,
    userLat: Double,
    userLon: Double,
    isSearching: Boolean,
    searchFinished: Boolean,
    searchResults: List<ProviderWithDistance>,
    onStartSearch: (CategoryEntity?, Double, Double) -> Unit,
    onResetSearch: () -> Unit,
    // --- NUEVAS PROPS PARA EL FLOATING TOOL ---
    activeAddress: AddressInfo?,
    availableAddresses: List<AddressInfo>,
    onAddressSelected: (AddressInfo) -> Unit,
    onUpdateGps: () -> Unit,
    beViewModel: BeBrainViewModel
) {
    var showManualSearchSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var selectedProviderOnRadar by remember { mutableStateOf<ProviderWithDistance?>(null) }
    var isBottomSheetExpanded by remember { mutableStateOf(false) }

    // --- ESTADO: VISIBILIDAD DE LA TARJETA DE UBICACIÓN ---
    var isLocationCardVisible by remember { mutableStateOf(true) }
    var isLocationExpanded by remember { mutableStateOf(false) }

    // Escuchar el evento de la lupa desde el BeBrainViewModel (Igual que en ResultBusqueda)
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            if (actionId == "toggle_location_card") {
                isLocationCardVisible = !isLocationCardVisible
            }
        }
    }

    // --- LÓGICA CONTEXTUAL ---
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isNightTime = currentHour >= 21 || currentHour < 6
    val isRaining = remember(weatherDescription) {
        weatherDescription.contains("lluvia", true) || weatherDescription.contains("tormenta", true)
    }

    var showContextAlert by remember { mutableStateOf(isNightTime || isRaining) }

    val topCategories = remember(allCategories) {
        allCategories.filter { it.name in listOf("Electricidad", "Plomería", "Fletes", "Cerrajería") }.take(4)
    }

    LaunchedEffect(topCategories) {
        if (selectedCategory == null && topCategories.isNotEmpty()) {
            selectedCategory = topCategories.first()
        }
    }

    LaunchedEffect(isSearching, searchFinished) {
        if (!searchFinished) isBottomSheetExpanded = false
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {

        // 1. FONDO: MAPA TÁCTICO
        TacticalMapBackground(
            isSearching = isSearching,
            searchFinished = searchFinished,
            results = searchResults,
            onProviderClick = { selectedProviderOnRadar = it }
        )

        // 2. HUD SUPERIOR: SOLO ALERTA CONTEXTUAL (Direcciones ahora en Be Tools)
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp).statusBarsPadding().padding(horizontal = 16.dp)) {
            AnimatedVisibility(
                visible = showContextAlert && (isNightTime || isRaining),
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ContextualWarningBanner(
                    isNight = isNightTime,
                    isRaining = isRaining,
                    weatherDesc = weatherDescription,
                    onDismiss = { showContextAlert = false }
                )
            }
        }

        // 3. CAPA PUBLICITARIA
        if (isSearching) {
            GoogleAdPopup()
        }

        // 4. POPUP DE INTERACCIÓN DEL RADAR
        AnimatedVisibility(
            visible = selectedProviderOnRadar != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut(tween(200)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center).zIndex(50f)
        ) {
            selectedProviderOnRadar?.let { providerData ->
                InteractiveRadarPopup(
                    data = providerData,
                    onClose = { selectedProviderOnRadar = null },
                    onChatClick = {
                        val providerId = providerData.service.id
                        selectedProviderOnRadar = null
                        navController.navigate("chat/$providerId") { launchSingleTop = true }
                    },
                    onProfileClick = {
                        val providerId = providerData.service.id
                        selectedProviderOnRadar = null
                        navController.navigate("perfil_prestador/$providerId") { launchSingleTop = true }
                    }
                )
            }
        }

        // 5. BOTTOM PANEL (CONFIG O RESULTADOS) - ELEVADO POR ARRIBA DEL HUD
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottomPadding).padding(bottom = 16.dp)) {
            if (searchFinished) {
                FastResultsPanel(
                    results = searchResults,
                    selectedCategory = selectedCategory,
                    isExpanded = isBottomSheetExpanded,
                    onToggleExpand = { isBottomSheetExpanded = !isBottomSheetExpanded },
                    onReset = {
                        onResetSearch()
                        selectedProviderOnRadar = null
                    },
                    onChatClick = { providerId ->
                        navController.navigate("chat/$providerId") { launchSingleTop = true }
                    },
                    onNavigateToNormalSearch = { catName ->
                        navController.navigate("result_busqueda/${Uri.encode(catName)}") { launchSingleTop = true }
                    }
                )
            } else if (!isSearching) {
                FastConfigBottomSheet(
                    selectedCategory = selectedCategory,
                    topCategories = topCategories,
                    onCategorySelect = { selectedCategory = it },
                    onOpenManualSearch = { showManualSearchSheet = true },
                    onStartSearch = { onStartSearch(selectedCategory, userLat, userLon) }
                )
            }
        }

        if (showManualSearchSheet) {
            ManualCategorySearchSheet(
                allCategories = allCategories,
                onDismiss = { showManualSearchSheet = false },
                onCategorySelected = {
                    selectedCategory = it
                    showManualSearchSheet = false
                }
            )
        }

        // ==================================================================================
        // --- 📍 NUEVO: FLOATING LOCATION TOOL (POSICIONADO EN LA PARTE SUPERIOR) ---
        // ==================================================================================
        
        // Scrim local para cuando está expandido
        AnimatedVisibility(
            visible = isLocationExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isLocationExpanded = false }
            )
        }

        // Componente Selector Flotante (Posicionado arriba a la izquierda para visibilidad táctica)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 80.dp) // Debajo de las alertas contextuales
                .width(220.dp) 
                .height(85.dp) 
        ) {
            ResultBusquedaLocationTool(
                activeAddress = activeAddress,
                availableAddresses = availableAddresses,
                user = userState,
                onAddressSelected = onAddressSelected,
                onUpdateGps = onUpdateGps,
                isExpanded = isLocationExpanded,
                onToggleExpand = { isLocationExpanded = it },
                isVisible = isLocationCardVisible
            )
        }
    }
}

// ==========================================================================================
// --- ALERTAS Y POPUPS CONTEXTUALES ---
// ==========================================================================================

@Composable
fun ContextualWarningBanner(
    isNight: Boolean,
    isRaining: Boolean,
    weatherDesc: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1F26).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.5f)),
        shadowElevation = 10.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFFACC15), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("AVISO DEL SISTEMA FAST", color = Color(0xFFFACC15), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))
                if (isNight) {
                    Text("🌙 Horario Nocturno: Solo buscaremos prestadores activos que cuenten con servicio de urgencias 24hs.", color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                }
                if (isRaining) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("🌧️ Clima Adverso ($weatherDesc): Es posible que las reparaciones externas se vean demoradas.", color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
            MaverickTacticalButton(
                onClick = onDismiss,
                size = 28.dp,
                modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun GoogleAdPopup() {
    Dialog(onDismissRequest = { }, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Relájate y mira estas ofertas mientras buscamos al mejor profesional para ti ☕",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 20.dp, start = 8.dp, end = 8.dp)
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF7F7F7),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column {
                    AsyncImage(
                        model = "https://picsum.photos/seed/ad/600/300",
                        contentDescription = "Ad",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("YouTube Premium", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        Text("Prueba 1 mes gratis.", fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1967D2))) {
                            Text("OBTENER OFERTA", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- MAPA RADAR INTERACTIVO ---
// ==========================================================================================

@Composable
fun TacticalMapBackground(
    isSearching: Boolean,
    searchFinished: Boolean,
    results: List<ProviderWithDistance>,
    onProviderClick: (ProviderWithDistance) -> Unit
) {
    val gridColor = if (isSearching) Color(0xFF22D3EE).copy(0.1f) else Color(0xFF1A1F26)

    Box(modifier = Modifier.fillMaxSize().drawBehind {
        val step = 40.dp.toPx()
        for (x in 0..size.width.toInt() step step.toInt()) {
            drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
        }
        for (y in 0..size.height.toInt() step step.toInt()) {
            drawLine(gridColor, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
        }
    }) {
        Box(modifier = Modifier.align(Alignment.Center).offset(y = (-50).dp)) {

            if (isSearching) {
                RadarPulse(delay = 0)
                RadarPulse(delay = 1000)
                RadarPulse(delay = 2000)
            } else if (searchFinished) {
                Box(modifier = Modifier.size(150.dp).border(1.dp, Color(0xFF22D3EE).copy(0.2f), CircleShape).align(Alignment.Center))
                Box(modifier = Modifier.size(280.dp).border(1.dp, Color(0xFF22D3EE).copy(0.1f), CircleShape).align(Alignment.Center))

                results.forEachIndexed { index, data ->
                    val angle = (index * (360 / results.size.coerceAtLeast(1))) * (Math.PI / 180)
                    val radius = 70f + (data.distanceKm * 8).toFloat().coerceAtMost(70f)
                    val offsetX = (cos(angle) * radius).dp
                    val offsetY = (sin(angle) * radius).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offsetX, y = offsetY)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onProviderClick(data) }
                            .zIndex(10f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(2.dp, Color(0xFF00FFC2)),
                            modifier = Modifier.size(54.dp),
                            shadowElevation = 10.dp
                        ) {
                            AsyncImage(
                                model = data.service.photoUrl,
                                contentDescription = "Avatar ${data.service.title}",
                                contentScale = ContentScale.Crop,
                                fallback = rememberVectorPainter(Icons.Default.Person)
                            )
                        }
                        Surface(
                            color = Color.Black.copy(0.8f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.2f)),
                            modifier = Modifier.padding(top = 4.dp).offset(y = (-8).dp)
                        ) {
                            Text(
                                "${String.format(Locale.getDefault(), "%.1f", data.distanceKm)}km",
                                color = Color(0xFF22D3EE),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.size(40.dp).align(Alignment.Center),
                shape = CircleShape,
                color = Color(0xFF22D3EE),
                border = BorderStroke(4.dp, Color(0xFF05070A)),
                shadowElevation = 15.dp
            ) {
                Icon(Icons.Default.Navigation, null, modifier = Modifier.padding(8.dp), tint = Color(0xFF05070A))
            }
        }
    }
}

// ==========================================================================================
// --- POPUP INTERACTIVO DEL RADAR ---
// ==========================================================================================

@Composable
fun InteractiveRadarPopup(
    data: ProviderWithDistance,
    onClose: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width(300.dp).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { },
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            border = BorderStroke(1.5.dp, Color(0xFF22D3EE).copy(alpha = 0.5f)),
            shadowElevation = 24.dp
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1F26), Color(0xFF05070A))))
            ) {
                Box(modifier = Modifier.matchParentSize().blur(20.dp).background(Color(0xFF22D3EE).copy(alpha = 0.05f)))

                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFF10B981).copy(0.2f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color(0xFF10B981).copy(0.5f))) {
                            Text("A ${data.estimatedMinutes} min", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        MaverickTacticalButton(
                            onClick = onClose,
                            size = 24.dp,
                            accentColor = Color.White.copy(alpha = 0.3f)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AsyncImage(
                        model = data.service.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, Color.White.copy(0.2f), CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Default.Person)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(data.service.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        if (data.service.isVerified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(Icons.Filled.Verified, null, tint = Color(0xFF9B51E0), modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(data.service.subtitle ?: "Profesional Independiente", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("%.1f".format(data.service.rating), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onProfileClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.1f))
                        ) {
                            Text("PERFIL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onChatClick,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2197F5))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("CHATEAR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- PANELES BOTTOM SHEET ---
// ==========================================================================================

@Composable
fun FastResultsPanel(
    results: List<ProviderWithDistance>,
    selectedCategory: CategoryEntity?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReset: () -> Unit,
    onChatClick: (String) -> Unit,
    onNavigateToNormalSearch: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggleExpand() }
            ) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.Gray, CircleShape).align(Alignment.CenterHorizontally))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("RESULTADOS FAST", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("${results.size} prestadores en alerta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(if (isExpanded) "Ocultar lista" else "Toca para ver lista", color = Color(0xFF22D3EE), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, null, tint = Color.Gray, modifier = Modifier.padding(end = 16.dp))
                        
                        MaverickTacticalButton(
                            onClick = onReset,
                            size = 36.dp,
                            accentColor = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = Color.White.copy(0.05f))

                if (results.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFFACC15), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No hay unidades de emergencia", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { onNavigateToNormalSearch(selectedCategory?.name ?: "Hogar") },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("IR A BÚSQUEDA ESTÁNDAR", fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.6f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { item ->
                            Box {
                                PrestadorCardV3(
                                    provider = item.service,
                                    onClick = { onChatClick(item.service.id) },
                                    onChatClick = { onChatClick(item.service.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Surface(
                                    color = Color(0xFF05070A).copy(0.9f),
                                    shape = RoundedCornerShape(bottomStart = 16.dp, topEnd = 16.dp),
                                    border = BorderStroke(1.dp, Color(0xFF22D3EE)),
                                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 2.dp)
                                ) {
                                    Text("A ${String.format(Locale.getDefault(), "%.1f", item.distanceKm)}km", color = Color(0xFF22D3EE), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarPulse(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val scale by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 4f, animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)), label = "scale")
    val alpha by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0f, animationSpec = infiniteRepeatable(tween(3000, delayMillis = delay, easing = LinearEasing)), label = "alpha")
    Box(modifier = Modifier.size(150.dp).graphicsLayer { scaleX = scale; scaleY = scale }.alpha(alpha).border(2.dp, Color(0xFF22D3EE).copy(0.4f), CircleShape))
}

@Composable
fun FastConfigBottomSheet(
    selectedCategory: CategoryEntity?,
    topCategories: List<CategoryEntity>,
    onCategorySelect: (CategoryEntity) -> Unit,
    onOpenManualSearch: () -> Unit,
    onStartSearch: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        shadowElevation = 24.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Maverick FAST", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE))
                    Text("Busca el servicio de emergencia", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    onClick = onOpenManualSearch,
                    shape = CircleShape,
                    color = Color.White.copy(0.05f),
                    border = BorderStroke(1.dp, Color(0xFF2197F5))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF2197F5), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Búsqueda Manual", color = Color(0xFF2197F5), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("MÁS UTILIZADOS", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val displayList = if (selectedCategory != null && !topCategories.contains(selectedCategory)) {
                    listOf(selectedCategory) + topCategories.take(3)
                } else {
                    topCategories
                }
                items(displayList) { cat ->
                    val isSelected = cat.name == selectedCategory?.name
                    val catColor = Color(CategoryVisuals.getColorFor(cat.superCategory))
                    Surface(
                        onClick = { onCategorySelect(cat) },
                        modifier = Modifier.size(width = 80.dp, height = 90.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) catColor.copy(alpha = 0.2f) else Color.White.copy(0.03f),
                        border = BorderStroke(1.dp, if (isSelected) catColor else Color.White.copy(0.1f))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(cat.icon, fontSize = 28.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(cat.name.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onStartSearch,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
            ) {
                Icon(Icons.Default.Bolt, null, tint = Color(0xFF05070A))
                Spacer(Modifier.width(8.dp))
                Text("SOLICITAR ASISTENCIA AHORA", color = Color(0xFF05070A), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualCategorySearchSheet(
    allCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCategorySelected: (CategoryEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, allCategories) {
        allCategories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0A0E14),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text("Selecciona una Categoría", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Escribe el oficio...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF22D3EE)
                )
            )

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered) { category ->
                    CompactCategoryCard(item = category, onClick = { onCategorySelected(category) })
                }
            }
        }
    }
}

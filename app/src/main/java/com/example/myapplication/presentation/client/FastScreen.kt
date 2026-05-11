package com.example.myapplication.presentation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.repository.FastFilterState
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.presentation.util.NotificationHelper
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Locale
import kotlin.math.*

// ==========================================================================================
// --- PANTALLA FAST (CLEAN RESET) ---
// ==========================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FastScreen(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    fastViewModel: FastViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by fastViewModel.uiState.collectAsStateWithLifecycle()
    val userState by beViewModel.userState.collectAsStateWithLifecycle()
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val availableAddressInfos by beViewModel.availableAddressInfos.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val sortedCategories by categoryViewModel.sortedCategories.collectAsStateWithLifecycle()

    val beSearchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()
    val isBeSearchActive by beViewModel.isSearchActive.collectAsStateWithLifecycle()
    val fastHistory by fastViewModel.fastHistory.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // [REGLA DE ORO] El onRouteChanged ahora se centraliza en AppNavigation
        // beViewModel.onRouteChanged("fast")
    }

    LaunchedEffect(isBeSearchActive) {
        fastViewModel.setBeSearchActive(isBeSearchActive)
    }

    LaunchedEffect(sortedCategories, beSearchQuery) {
        // Solo enviamos categorías filtradas si hay una búsqueda activa
        if (beSearchQuery.isNotEmpty()) {
            fastViewModel.updateBeSearchCategories(sortedCategories)
        } else {
            fastViewModel.updateBeSearchCategories(emptyList())
        }
    }

    LaunchedEffect(activeAddress) {
        // Al cambiar de dirección, si hay una búsqueda activa o terminada, 
        // la refrescamos automáticamente para la nueva zona.
        uiState.selectedCategory?.let { category ->
            fastViewModel.startSearch(category)
        } ?: run {
            if (uiState.isSearching || uiState.searchFinished) {
                fastViewModel.resetSearch()
            }
        }
    }

    // --- ESCUCHA DE ACCIONES DEL CEREBRO (GPS / BURBUJA) ---
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            if (actionId == "refresh_gps") {
                if (ubicacionViewModel.isGpsHabilitado(context)) {
                    ubicacionViewModel.ejecutarCalculoUbicacionGps(context) { pais, prov, loc, calle, num, cp, lat, lng ->
                        val freshGpsAddress = AddressInfo(
                            id = "gps_current",
                            companyOrUserName = "Mi Ubicación",
                            branchName = "GPS Tracker",
                            streetAndNumber = if (calle.isNotBlank()) "$calle $num".trim() else "Ubicación detectada",
                            locality = loc,
                            province = prov,
                            country = pais,
                            postalCode = cp,
                            isCompany = false,
                            lat = lat,
                            lng = lng
                        )
                        beViewModel.updateAddressFromGps(freshGpsAddress)
                    }
                } else {
                    Toast.makeText(context, "⚠️ El GPS está desactivado.", Toast.LENGTH_SHORT).show()
                }
            } else if (actionId.startsWith("cat_")) {
                // 🔥 ACCIÓN DISPARADA DESDE LA BURBUJA TOP DE BE 🔥
                val catName = actionId.removePrefix("cat_")
                allCategories.find { it.name.lowercase().trim() == catName.lowercase().trim() }?.let { category ->
                    fastViewModel.startSearch(category)
                }
            }
        }
    }

    FastScreenContent(
        navController = navController,
        bottomPadding = bottomPadding,
        isSearching = uiState.isSearching,
        searchFinished = uiState.searchFinished,
        searchResults = uiState.searchResults,
        activeAddress = activeAddress,
        availableAddresses = availableAddressInfos,
        user = userState,
        allCategories = allCategories,
        fastHistory = fastHistory,
        selectedCategory = uiState.selectedCategory,
        isBeSearchActive = uiState.isBeSearchActive,
        beSearchCategories = uiState.beSearchCategories,
        filters = uiState.filters,
        onAddressSelected = { addr -> beViewModel.selectAddress(addr.id) },
        onUpdateGps = { beViewModel.triggerAction("refresh_gps") },
        onCategoryClick = { category -> 
            fastViewModel.selectCategory(category)
        },
        onToggleFilter = { id -> fastViewModel.toggleFilter(id) },
        onResetSearch = { fastViewModel.resetSearch() },
        onStartSearch = { cat -> fastViewModel.startSearch(cat) }
    )
}

@Composable
fun FastScreenContent(
    navController: NavHostController,
    bottomPadding: PaddingValues,
    isSearching: Boolean,
    searchFinished: Boolean,
    searchResults: List<ProviderWithDistance>,
    activeAddress: AddressInfo?,
    availableAddresses: List<AddressInfo>,
    user: UserEntity?,
    allCategories: List<CategoryEntity>,
    fastHistory: List<com.example.myapplication.data.local.FastCategoryEntity>,
    selectedCategory: CategoryEntity?,
    isBeSearchActive: Boolean,
    beSearchCategories: List<CategoryEntity>,
    filters: FastFilterState,
    onAddressSelected: (AddressInfo) -> Unit,
    onUpdateGps: () -> Unit,
    onCategoryClick: (CategoryEntity) -> Unit,
    onToggleFilter: (String) -> Unit,
    onResetSearch: () -> Unit,
    onStartSearch: (CategoryEntity?) -> Unit
) {
    var selectedProviderOnRadar by remember { mutableStateOf<ProviderWithDistance?>(null) }
    var radarScale by remember { mutableFloatStateOf(1f) }

    // --- ESTADOS PARA PUBLICIDAD Y NOTIFICACIONES ---
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    var showAd by remember { mutableStateOf(false) }

    val cyberSheetMinHeight = 120.dp
    val cyberSheetExpandedHeight = 320.dp
    val currentCyberSheetHeight by animateDpAsState(
        targetValue = if (!isSearching && !searchFinished && !isBeSearchActive) cyberSheetExpandedHeight else cyberSheetMinHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cyberSheetHeight"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {

        // 1. FONDO: MAPA TÁCTICO
        TacticalMapBackground(
            isSearching = isSearching,
            searchFinished = searchFinished,
            results = searchResults,
            userLat = activeAddress?.lat ?: -26.8310,
            userLon = activeAddress?.lng ?: -65.2045,
            scale = radarScale,
            onScaleChange = { radarScale = (radarScale * it).coerceIn(0.5f, 3f) },
            onProviderClick = { selectedProviderOnRadar = it }
        )

        // 2. HUD SUPERIOR
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(999f)
        ) {
            BarraCabezera(
                title = "Servicios de Urgencia",
                subtitle = "Prestadores 24 Horas",
                emoji = "⚡",
                onBack = { navController.popBackStack() },
                onInfoClick = { /* Info */ },
                backgroundBrush = MaverickColors.RogHorizontalGradient
            )
        }

        // --- BRÚJULA MODERNA (Posicionada debajo de la cabecera a la derecha) ---
        ModernCompass(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 130.dp, end = 16.dp)
                .zIndex(100f)
        )

        // 3. CYBER SHEET (CONTENEDOR INFERIOR)
        CyberSheet(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(30f)
                .heightIn(min = currentCyberSheetHeight)
        ) {
            Text(
                text = "SISTEMA DE RESPUESTA RÁPIDA",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedProviderOnRadar != null) {
                val service = selectedProviderOnRadar!!.service
                val filterItems = remember(service) {
                    listOf(
                        ControlItem("Online", Icons.Default.Public, "🌐", Color(0xFF00FFC2), "online") to service.isOnline,
                        ControlItem("24hs", Icons.Default.AccessTimeFilled, "🕒", Color(0xFFFF9800), "24h") to service.works24h,
                        ControlItem("Visitas", Icons.Default.LocalShipping, "🚚", Color(0xFF2197F5), "visit") to service.doesHomeVisits,
                        ControlItem("Local", Icons.Default.Storefront, "🏪", Color(0xFF4CAF50), "loc") to service.hasPhysicalLocation,
                        ControlItem("Verificado", Icons.Default.Verified, "✅", Color(0xFF22D3EE), "verif") to service.isVerified
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filterItems.forEach { (item, isSelected) ->
                        CompactItemButton(
                            item = item,
                            isSelected = isSelected,
                            onClick = { /* Info */ }
                        )
                    }
                }
            } else {
                // --- SECCIÓN: FILTROS TÁCTICOS DE BÚSQUEDA ---
                // Se muestran cuando no hay un prestador seleccionado para ajustar la búsqueda global.
                val quickFilters = remember {
                    listOf(
                        ControlItem("Online", Icons.Default.Public, "🌐", Color(0xFF00FFC2), "online"),
                        ControlItem("24hs", Icons.Default.AccessTimeFilled, "🕒", Color(0xFFFF9800), "24h"),
                        ControlItem("Suscrito", Icons.Default.Verified, "✅", Color(0xFF22D3EE), "sub"),
                        ControlItem("Local", Icons.Default.Storefront, "🏪", Color(0xFF4CAF50), "loc")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickFilters.forEach { item ->
                        val isSelected = when(item.id) {
                            "online" -> filters.isOnline
                            "24h" -> filters.is24h
                            "sub" -> filters.isSubscribed
                            "loc" -> filters.isLocal
                            else -> false
                        }
                        CompactItemButton(
                            item = item,
                            isSelected = isSelected,
                            onClick = { onToggleFilter(item.id) }
                        )
                    }
                }
            }

            if (!isSearching && !searchFinished && !isBeSearchActive) {
                Spacer(Modifier.height(16.dp))

                // 4. CATEGORÍAS TÁCTICAS (DINÁMICAS DESDE ROOM)
                val emergencyCategories = remember(fastHistory, allCategories) {
                    if (fastHistory.isNotEmpty()) {
                        fastHistory.map { history ->
                            CategoryEntity(
                                name = history.name,
                                icon = history.icon,
                                superCategory = history.superCategory,
                                description = "",
                                isNew = false,
                                isNewPrestador = false,
                                isAd = false
                            )
                        }
                    } else {
                        // Fallback inicial
                        val targets = listOf("Cerrajeria", "Auxilio Mecanico", "Fletes")
                        allCategories.filter { cat -> targets.any { it.equals(cat.name, ignoreCase = true) } }
                    }
                }

                Text(
                    text = "SERVICIOS DE EMERGENCIA",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    emergencyCategories.forEach { category ->
                        MiniCompactCategoryCard(
                            item = category,
                            isSelected = selectedCategory?.name == category.name,
                            onClick = { onCategoryClick(category) }
                        )
                    }
                }
            }

            // 5. NUEVA FILA DE RESULTADOS DE BÚSQUEDA DEL ASISTENTE BE
            AnimatedVisibility(
                visible = isBeSearchActive && beSearchCategories.isNotEmpty() && !isSearching && !searchFinished,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "RESULTADOS DE BÚSQUEDA",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        beSearchCategories.forEach { category ->
                            MiniCompactCategoryCard(
                                item = category,
                                isSelected = selectedCategory?.name == category.name,
                                onClick = { onCategoryClick(category) }
                            )
                        }
                    }
                }
            }
        }

        // 4. POPUP DE INTERACCIÓN DEL RADAR
        AnimatedVisibility(
            visible = selectedProviderOnRadar != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut(tween(200)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center).zIndex(50f)
        ) {
            selectedProviderOnRadar?.let { providerData ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedProviderOnRadar = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        MaverickTacticalButton(
                            onClick = { selectedProviderOnRadar = null },
                            size = 32.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        PrestadorCardV3(
                            provider = providerData.service,
                            cardWidth = 280.dp,
                            isCompact = false,
                            onClick = {
                                selectedProviderOnRadar = null
                                navController.navigate("perfil_prestador/${providerData.service.id}")
                            },
                            onChatClick = {
                                selectedProviderOnRadar = null
                                val service = providerData.service
                                navController.navigate("chat?providerId=${service.id}&companyId=${service.companyId ?: ""}&categoryId=${service.categoryId ?: ""}")
                            }
                        )
                        
                        Surface(
                            modifier = Modifier.padding(top = 12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                        ) {
                            Text(
                                "LLEGADA ESTIMADA: ${providerData.estimatedMinutes} MIN",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. FLOATING LOCATION TOOL & SEARCH BUTTON
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp)
                .fillMaxWidth()
                .zIndex(45f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.width(250.dp).height(80.dp).padding(bottom = 10.dp)) {
                LocationSelector(
                    user = user,
                    currentLocation = activeAddress?.toLocationOption()
                        ?: LocationOption.Gps(address = "Buscando...", locality = "Ubicación"),
                    onRefresh = onUpdateGps,
                    onLocationSelected = { option ->
                        val targetId = when (option) {
                            is LocationOption.Gps -> "gps_current"
                            is LocationOption.Personal -> option.id
                            is LocationOption.Business -> option.id
                        }
                        val matched = availableAddresses.find { it.id == targetId }
                        matched?.let { onAddressSelected(it) }
                    },
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1A1A24).copy(alpha = 0.9f), Color(0xFF0D0D12).copy(alpha = 1f))
                    )
                )
            }

            // BOTÓN DE ACCIÓN: BUSCAR O LIMPIAR
            MaverickTacticalButton(
                onClick = {
                    if (isSearching || searchFinished) {
                        onResetSearch()
                    } else {
                        // Activamos la publicidad antes de iniciar la búsqueda Maverick
                        showAd = true
                    }
                },
                modifier = Modifier.size(76.dp).padding(bottom= 10.dp),
                accentColor = if (isSearching || searchFinished) Color(0xFFEF4444) else Color(0xFF22D3EE)
            ) {
                Icon(
                    imageVector = if (isSearching || searchFinished) Icons.Default.Close else Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )

            }

        }

        // --- 🛡️ SECCIÓN: PUBLICIDAD INTERSTITIAL (FLUJO PREMIUM) ---
        GoogleVerticalInterstitialAd(
            show = showAd,
            onDismiss = {
                showAd = false
                
                // Disparamos la búsqueda real después de la publicidad
                onStartSearch(null)
                
                // Feedback visual mediante notificación Toast Maverick
                notificationHelper.showNotification(
                    "📡 Escaneo en Proceso",
                    "Buscando '${selectedCategory?.name ?: "servicios"}' cerca de ti..."
                )
            }
        )
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
    userLat: Double,
    userLon: Double,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    onProviderClick: (ProviderWithDistance) -> Unit
) {
    val gridColor = if (isSearching) Color(0xFF22D3EE).copy(0.1f) else Color(0xFF1A1F26)
    val orbitColor = Color(0xFF22D3EE).copy(alpha = 0.15f)
    val density = LocalDensity.current

    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        onScaleChange(zoomChange)
        offset += offsetChange
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .transformable(state = transformState)
        .drawBehind {
            val step = 40.dp.toPx()
            for (x in 0..size.width.toInt() step step.toInt()) {
                drawLine(gridColor, Offset(x.toFloat() + offset.x, 0f), Offset(x.toFloat() + offset.x, size.height), 1f)
            }
            for (y in 0..size.height.toInt() step step.toInt()) {
                drawLine(gridColor, Offset(0f, y.toFloat() + offset.y), Offset(size.width, y.toFloat() + offset.y), 1f)
            }
        }
    ) {
        Box(modifier = Modifier
            .align(Alignment.Center)
            .offset(y = (-50).dp)
            .graphicsLayer {
                translationX = offset.x
                translationY = offset.y
                scaleX = scale
                scaleY = scale
            }
        ) {

            if (isSearching) {
                RadarPulse(delay = 0)
                RadarPulse(delay = 1000)
                RadarPulse(delay = 2000)
            } else if (searchFinished) {
                // --- DIBUJO DE ÓRBITAS TÁCTICAS (Distancia Real) ---
                Canvas(modifier = Modifier.size(300.dp).align(Alignment.Center)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Órbita 1: 1km (Radio 60dp aprox)
                    drawCircle(orbitColor, radius = 60.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                    
                    // Órbita 2: 3km (Radio 100dp aprox)
                    drawCircle(orbitColor, radius = 100.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                    
                    // Órbita 3: 5km (Radio 140dp aprox)
                    drawCircle(orbitColor, radius = 140.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                }

                // Etiquetas de órbita
                OrbitLabel("1km", 60.dp, Alignment.TopCenter)
                OrbitLabel("3km", 100.dp, Alignment.TopCenter)
                OrbitLabel("5km", 140.dp, Alignment.TopCenter)

                results.forEach { data ->
                    // --- CÁLCULO DE POSICIÓN GEOGRÁFICA (Rumbo y Distancia Escalada) ---
                    val bearing = calculateBearing(userLat, userLon, data.lat, data.lon)
                    // Ajuste de ángulo: 0° en geografía es Norte (Arriba), en Compose es Este (Derecha)
                    val angleRadians = Math.toRadians(bearing - 90.0)
                    
                    // Escalamiento de radio basado en órbitas
                    val radiusPx = with(density) { getRadiusForDistance(data.distanceKm).dp.toPx() }
                    
                    val offsetX = (cos(angleRadians) * radiusPx / 2.2).toFloat() // Factor de ajuste visual
                    val offsetY = (sin(angleRadians) * radiusPx / 2.2).toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offsetX.dp, y = offsetY.dp)
                            .graphicsLayer {
                                // Mantenemos los avatares sin escala para que no se deformen al hacer zoom
                                scaleX = 1f / scale
                                scaleY = 1f / scale
                            }
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
                                String.format(Locale.getDefault(), "%.1fkm", data.distanceKm),
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

@Composable
fun OrbitLabel(text: String, radius: androidx.compose.ui.unit.Dp, alignment: Alignment) {
    Box(modifier = Modifier.size(radius * 2 + 20.dp)) {
        Text(
            text = text,
            color = Color(0xFF22D3EE).copy(alpha = 0.4f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(alignment).offset(y = 4.dp)
        )
    }
}

/**
 * Mapea la distancia real en KM a un radio en DP para el radar.
 */
fun getRadiusForDistance(distanceKm: Double): Double {
    return when {
        distanceKm <= 1.0 -> 60.0 * distanceKm
        distanceKm <= 3.0 -> 60.0 + (distanceKm - 1.0) * (100.0 - 60.0) / 2.0
        distanceKm <= 5.0 -> 100.0 + (distanceKm - 3.0) * (140.0 - 100.0) / 2.0
        else -> 140.0 + (distanceKm - 5.0) * 10.0 // Crecimiento lento fuera de 5km
    }.coerceAtMost(250.0)
}

/**
 * Calcula el Rumbo (Bearing) entre dos coordenadas en grados (0-360).
 */
fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val phi1 = Math.toRadians(lat1)
    val phi2 = Math.toRadians(lat2)
    val deltaLambda = Math.toRadians(lon2 - lon1)

    val y = sin(deltaLambda) * cos(phi2)
    val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
    
    val theta = atan2(y, x)
    return (Math.toDegrees(theta) + 360.0) % 360.0
}

@Composable
fun ModernCompass(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(60.dp)
            .drawBehind {
                val cyan = Color(0xFF22D3EE)
                val center = Offset(size.width / 2, size.height / 1)
                val radius = size.width / 2

                // Círculo exterior
                drawCircle(
                    color = cyan.copy(alpha = 0.2f),
                    radius = radius,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Marcadores cardinales (Norte siempre arriba)
                val markers = listOf("N", "E", "S", "O")
                markers.forEachIndexed { index, label ->
                    val angle = Math.toRadians(index * 90.0 - 90.0)
                    val x = center.x + cos(angle).toFloat() * (radius - 8.dp.toPx())
                    val y = center.y + sin(angle).toFloat() * (radius - 8.dp.toPx())
                    
                    // Nota: En dibujo manual es complejo poner texto, usamos puntos
                    drawCircle(
                        color = if (label == "N") Color.Red else cyan,
                        radius = if (label == "N") 2.dp.toPx() else 1.5.dp.toPx(),
                        center = Offset(x,y)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("N", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFF22D3EE), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun CyberSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cornerSize = 12.dp
    val strokeWidth = 2.dp
    val cyanColor = Color(0xFF22D3EE) // MaverickCyan

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp) // Altura base
            .clip(CutCornerShape(topStart = cornerSize, topEnd = cornerSize))
            .background(CPCyberColors.DeepVoid.copy(alpha = 0.95f))
            .drawBehind {
                val cornerSizePx = cornerSize.toPx()
                val strokeWidthPx = strokeWidth.toPx()
                
                // Path que define el borde superior y los cortes
                val path = Path().apply {
                    // Empezamos un poco abajo en el lateral izquierdo
                   // moveTo(0f, cornerSizePx + 30.dp.toPx())
                    lineTo(0f, cornerSizePx)
                    lineTo(cornerSizePx, 0f)
                    lineTo(size.width - cornerSizePx, 0f)
                    lineTo(size.width, cornerSizePx)
                    //lineTo(size.width, cornerSizePx + 30.dp.toPx())
                }
                
                drawPath(
                    path = path,
                    color = cyanColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )

                // Efecto de resplandor sutil en la parte superior
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(cyanColor.copy(alpha = 0.1f), Color.Transparent),
                        startY = 0f,
                        endY = 50.dp.toPx()
                    ),
                    alpha = 0.5f
                )
            }
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp) // Padding inferior para el asistente y cards
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
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
// ==========================================================================================
// --- 🖼️ SECCIÓN: VISTA PREVIA (COMPOSE PREVIEW) ---
// ==========================================================================================

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewFastScreen() {
    val navController = rememberNavController()
    MyApplicationTheme {
        FastScreenContent(
            navController = navController,
            bottomPadding = PaddingValues(0.dp),
            isSearching = false,
            searchFinished = false,
            searchResults = emptyList(),
            activeAddress = null,
            availableAddresses = emptyList(),
            user = null,
            allCategories = emptyList(),
            fastHistory = emptyList(),
            selectedCategory = null,
            isBeSearchActive = false,
            beSearchCategories = emptyList(),
            filters = FastFilterState(),
            onAddressSelected = {},
            onUpdateGps = {},
            onCategoryClick = {},
            onToggleFilter = {},
            onResetSearch = {},
            onStartSearch = {}
        )
    }
}

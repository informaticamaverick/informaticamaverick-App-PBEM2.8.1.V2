package com.example.myapplication.presentation.features.home

import com.example.myapplication.core.domain.model.AddressInfo
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
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.MaverickTacticalButton
import com.example.myapplication.core.notifications.NotificationHelper
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
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
    val availableAddresses by beViewModel.availableAddressInfos.collectAsStateWithLifecycle()
    val activeProfileName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val activeProfilePhoto by beViewModel.activeProfilePhoto.collectAsStateWithLifecycle()
    val selectedProfileId by beViewModel.selectedProfileId.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val sortedCategories by categoryViewModel.sortedCategories.collectAsStateWithLifecycle()

    val beSearchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()
    val isBeSearchActive by beViewModel.isSearchActive.collectAsStateWithLifecycle()
    val isGpsActive by beViewModel.isGpsEnabled.collectAsStateWithLifecycle()
    val fastHistory by fastViewModel.fastHistory.collectAsStateWithLifecycle()
    val shortcuts by fastViewModel.shortcuts.collectAsStateWithLifecycle()

    LaunchedEffect(isBeSearchActive) {
        fastViewModel.setBeSearchActive(isBeSearchActive)
    }

    LaunchedEffect(sortedCategories, beSearchQuery) {
        if (beSearchQuery.isNotEmpty()) {
            fastViewModel.updateBeSearchCategories(sortedCategories)
        } else {
            fastViewModel.updateBeSearchCategories(emptyList())
        }
    }

    LaunchedEffect(activeAddress) {
        uiState.selectedCategory?.let { category ->
            fastViewModel.startSearch(category)
        } ?: run {
            if (uiState.isSearching || uiState.searchFinished) {
                fastViewModel.resetSearch()
            }
        }
    }

    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            if (actionId == "refresh_gps") {
                if (ubicacionViewModel.isGpsEnabled.value) {
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
        availableAddresses = availableAddresses,
        user = userState,
        activeProfileName = activeProfileName,
        activeProfilePhoto = activeProfilePhoto,
        selectedProfileId = selectedProfileId,
        isGpsActive = isGpsActive,
        allCategories = allCategories,
        fastHistory = fastHistory,
        selectedCategory = uiState.selectedCategory,
        isBeSearchActive = uiState.isBeSearchActive,
        beSearchCategories = uiState.beSearchCategories,
        filters = uiState.filters,
        shortcuts = shortcuts,
        onAddressSelected = { addr -> beViewModel.selectAddress(addr.id) },
        onUpdateGps = { beViewModel.triggerAction("refresh_gps") },
        onGpsToggle = { ubicacionViewModel.toggleGps(context) },
        onProfileSelected = { beViewModel.selectProfile(it) },
        onLogout = { beViewModel.triggerAction("logout") },
        onCategoryClick = { category -> 
            fastViewModel.selectCategory(category)
        },
        onToggleFilter = { id -> fastViewModel.toggleFilter(id) },
        onManageShortcuts = { id, add -> fastViewModel.manageShortcut(id, add) },
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
    activeProfileName: String,
    activeProfilePhoto: Any?,
    selectedProfileId: String?,
    isGpsActive: Boolean,
    allCategories: List<CategoryEntity>,
    fastHistory: List<com.example.myapplication.core.data.local.dao.FastCategoryEntity>,
    selectedCategory: CategoryEntity?,
    isBeSearchActive: Boolean,
    beSearchCategories: List<CategoryEntity>,
    filters: FastFilterState,
    shortcuts: List<FilterSortItem> = emptyList(),
    onAddressSelected: (AddressInfo) -> Unit,
    onUpdateGps: () -> Unit,
    onGpsToggle: () -> Unit = {},
    onProfileSelected: (String?) -> Unit,
    onLogout: () -> Unit,
    onCategoryClick: (CategoryEntity) -> Unit,
    onToggleFilter: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit = { _, _ -> },
    onResetSearch: () -> Unit,
    onStartSearch: (CategoryEntity?) -> Unit
) {
    var selectedProviderOnRadar by remember { mutableStateOf<ProviderWithDistance?>(null) }
    var radarScale by remember { mutableFloatStateOf(1f) }
    var showLocationPopup by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }

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
            
            // --- NUEVO: TARJETA DE CONTEXTO MAVERICK ---
            MoldePremiumContextCard(
                user = user,
                activeProfileName = activeProfileName,
                activeProfilePhoto = activeProfilePhoto,
                mainAddress = activeAddress?.streetAndNumber ?: "SELECCIONAR UBICACIÓN",
                localityInfo = activeAddress?.locality ?: "ESCANEANDO...",
                description = if (activeAddress?.id == "gps_current") "GPS_LIVE" else if (activeAddress?.isCompany == true) "NETWORK_HQ" else "STATION_HOME",
                isGpsActive = isGpsActive,
                onUserClick = { showProfilePopup = true },
                onLocationClick = { showLocationPopup = true },
                onGpsToggle = onUpdateGps,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        ModernCompass(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 130.dp, end = 16.dp)
                .zIndex(100f)
        )

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
                        BeDictionary.Filters["filter_online"]?.let { FilterSortItem(it.id, it.label, it.emoji ?: "🌐", color = Color(0xFF00FFC2)) } to service.isOnline,
                        BeDictionary.Filters["filter_chat_24h"]?.let { FilterSortItem(it.id, "24HS", it.emoji ?: "🕒", color = Color(0xFFFF9800)) } to service.works24h,
                        BeDictionary.Filters["filter_visits"]?.let { FilterSortItem(it.id, it.label, it.emoji ?: "🚚", color = Color(0xFF2197F5)) } to service.doesHomeVisits,
                        BeDictionary.Filters["filter_chat_local"]?.let { FilterSortItem(it.id, it.label, it.emoji ?: "🏪", color = Color(0xFF4CAF50)) } to service.hasPhysicalLocation,
                        BeDictionary.Filters["filter_chat_verified"]?.let { FilterSortItem(it.id, it.label, it.emoji ?: "🛡️", color = Color(0xFF22D3EE)) } to service.isVerified
                    ).mapNotNull { (item, isSelected) -> if (item != null) item to isSelected else null }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    filterItems.forEach { (item, isSelected) ->
                       // FilterChipSmall(
                         //   item = item,
                         //   isSelected = isSelected,
                         //   onClick = { /* Info */ },
                         //   onLongClick = { /* Info */ },

                       // )
                    }
                }
            } else {
                MoldePremiumFilterCard(
                    label = "Filtrar por",
                    dropdownItems = remember {
                        listOf(
                            BeDictionary.Filters["filter_online"],
                            BeDictionary.Filters["filter_chat_24h"],
                            BeDictionary.Filters["filter_chat_sub"],
                            BeDictionary.Filters["filter_chat_local"]
                        ).filterNotNull()
                    },
                    shortcutItems = shortcuts,
                    activeFilters = buildSet {
                        if (filters.isOnline) add("filter_online")
                        if (filters.is24h) add("filter_chat_24h")
                        if (filters.isSubscribed) add("filter_chat_sub")
                        if (filters.isLocal) add("filter_chat_local")
                    },
                    onToggle = onToggleFilter,
                    onManageShortcuts = onManageShortcuts
                )
            }

            if (!isSearching && !searchFinished && !isBeSearchActive) {
                Spacer(Modifier.height(16.dp))

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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp)
                .fillMaxWidth()
                .zIndex(45f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.width(16.dp)) // Espacio para equilibrar el botón

            MaverickTacticalButton(
                onClick = {
                    if (isSearching || searchFinished) {
                        onResetSearch()
                    } else {
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

        // --- DIÁLOGOS DE CONTROL ---
        LocationDialog(
            show = showLocationPopup,
            availableAddresses = availableAddresses,
            activeAddress = activeAddress,
            isGpsSystemEnabled = isGpsActive,
            onRefresh = onUpdateGps,
            onGpsToggle = onGpsToggle,
            onLocationSelected = { addr ->
                onAddressSelected(addr)
                showLocationPopup = false
            },
            onDismiss = { showLocationPopup = false }
        )

        if (user != null) {
            ProfileDialog(
                show = showProfilePopup,
                user = user.toDomain(),
                isPersonalProfile = selectedProfileId == null,
                selectedProfileId = selectedProfileId,
                onProfileSelected = { id -> 
                    onProfileSelected(id)
                    showProfilePopup = false 
                },
                navController = navController,
                onLogout = { 
                    onLogout()
                    showProfilePopup = false 
                },
                onDismiss = { showProfilePopup = false }
            )
        }

        GoogleVerticalInterstitialAd(
            show = showAd,
            onDismiss = {
                showAd = false
                onStartSearch(null)
                notificationHelper.showNotification(
                    "📡 Escaneo en Proceso",
                    "Buscando cerca de ti..."
                )
            }
        )
    }
}

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
                Canvas(modifier = Modifier.size(300.dp).align(Alignment.Center)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    drawCircle(orbitColor, radius = 60.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(orbitColor, radius = 100.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(orbitColor, radius = 140.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
                }

                OrbitLabel("1km", 60.dp, Alignment.TopCenter)
                OrbitLabel("3km", 100.dp, Alignment.TopCenter)
                OrbitLabel("5km", 140.dp, Alignment.TopCenter)

                results.forEach { data ->
                    val bearing = calculateBearing(userLat, userLon, data.lat, data.lon)
                    val angleRadians = Math.toRadians(bearing - 90.0)
                    val radiusPx = with(density) { getRadiusForDistance(data.distanceKm).dp.toPx() }
                    val offsetX = (cos(angleRadians) * radiusPx / 2.2).toFloat() 
                    val offsetY = (sin(angleRadians) * radiusPx / 2.2).toFloat()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offsetX.dp, y = offsetY.dp)
                            .graphicsLayer {
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
                                //String.format(Locale.getDefault(), "%.1fkm", data.distanceKm),
                                text = "${data.distanceKm}km",
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

fun getRadiusForDistance(distanceKm: Double): Double {
    return when {
        distanceKm <= 1.0 -> 60.0 * distanceKm
        distanceKm <= 3.0 -> 60.0 + (distanceKm - 1.0) * (100.0 - 60.0) / 2.0
        distanceKm <= 5.0 -> 100.0 + (distanceKm - 3.0) * (140.0 - 100.0) / 2.0
        else -> 140.0 + (distanceKm - 5.0) * 10.0 
    }.coerceAtMost(250.0)
}

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
                drawCircle(
                    color = cyan.copy(alpha = 0.2f),
                    radius = radius,
                    style = Stroke(width = 1.dp.toPx())
                )
                val markers = listOf("N", "E", "S", "O")
                markers.forEachIndexed { index, label ->
                    val angle = Math.toRadians(index * 90.0 - 90.0)
                    val x = center.x + cos(angle).toFloat() * (radius - 8.dp.toPx())
                    val y = center.y + sin(angle).toFloat() * (radius - 8.dp.toPx())
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
    val cyanColor = Color(0xFF22D3EE) 

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp) 
            .clip(CutCornerShape(topStart = cornerSize, topEnd = cornerSize))
            .background(CPCyberColors.DeepVoid.copy(alpha = 0.95f))
            .drawBehind {
                val cornerSizePx = cornerSize.toPx()
                val strokeWidthPx = strokeWidth.toPx()
                val path = Path().apply {
                    lineTo(0f, cornerSizePx)
                    lineTo(cornerSizePx, 0f)
                    lineTo(size.width - cornerSizePx, 0f)
                    lineTo(size.width, cornerSizePx)
                }
                drawPath(
                    path = path,
                    color = cyanColor,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(cyanColor.copy(alpha = 0.1f), Color.Transparent),
                        startY = 0f,
                        endY = 50.dp.toPx()
                    ),
                    alpha = 0.5f
                )
            }
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 100.dp) 
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


/**
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
            user = null,
            allCategories = emptyList(),
            fastHistory = emptyList(),
            selectedCategory = null,
            isBeSearchActive = false,
            beSearchCategories = emptyList(),
            filters = FastFilterState(),
            shortcuts = emptyList(),
            onAddressSelected = {},
            onUpdateGps = {},
            onCategoryClick = {},
            onToggleFilter = {},
            onManageShortcuts = { _, _ -> },
            onResetSearch = {},
            onProfileSelected = {},
            onLogout = {},
            onStartSearch = {},
            selectedProfileId = null,
            isGpsActive = false,
            activeProfileName = "",
            activeProfilePhoto = null,
            onUserClick = {},
            onLocationClick = {}
        )
    }
}

*/
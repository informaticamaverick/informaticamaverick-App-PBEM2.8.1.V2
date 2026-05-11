package com.example.myapplication.presentation.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import android.widget.Toast
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.profile.ProfileViewModel
import kotlin.collections.isNotEmpty

// ==================================================================================
// --- 🏠 SECCIÓN 1: ORQUESTADOR DE PANTALLA (INTERMEDIARIO BEBRAIN) ---
// ==================================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController,
    onLogoutRoot: () -> Unit = {}, // 🔥 NUEVO: Callback para navegación raíz
    profileViewModel: ProfileViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    promoViewModel: PromoViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel(),
   // interactionViewModel: BeInteractionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ==================================================================================
    // --- 🛠️ SUBSECCIÓN: TRABAJO SUCIO (COLECTA DE LOS OBREROS) ---
    // ==================================================================================
    
    // Obrero 1: Ubicación y Clima
    val latitude by ubicacionObrero.latitude.collectAsStateWithLifecycle()
    val longitude by ubicacionObrero.longitude.collectAsStateWithLifecycle()

    // Obrero 2: Categorías
    val allRawCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    // ==================================================================================
    // --- 🧠 SUBSECCIÓN: SINCRONIZACIÓN OBREROS -> CEREBRO (INTERMEDIARIO) ---
    // ==================================================================================
    // 🔥 ESCUCHA DE ACCIONES DEL CEREBRO PARA EL OBRERO (ORQUESTACIÓN) 🔥
    LaunchedEffect(Unit) {
        // [REGLA DE ORO] Ya no llamamos a onRouteChanged ni setHUDContext aquí.
        // La navegación central (AppNavigation) se encarga de la sincronización inicial.

        beViewModel.actionEvent.collect { actionId ->
            when {
                actionId.startsWith("sort_") || actionId.startsWith("view_") -> {
                    categoryViewModel.toggleSortFilter(actionId)
                }
                actionId == "clear_filters" -> {
                    categoryViewModel.clearFilters()
                }
                actionId.startsWith("chat_") -> {
                    val providerId = actionId.removePrefix("chat_")
                    navController.navigate("chat?providerId=$providerId")
                }
                actionId.startsWith("talk_") -> {
                }
            }
        }
    }
    // ==================================================================================
    // --- 📊 SUBSECCIÓN: ESTADOS MAESTROS PARA LA UI (DESDE EL CEREBRO) ---
    // ==================================================================================
    val temperature by beViewModel.temperature.collectAsStateWithLifecycle()
    val weatherEmoji by beViewModel.weatherEmoji.collectAsStateWithLifecycle()
    val weatherDescription by beViewModel.weatherDescription.collectAsStateWithLifecycle()
    val cityName by beViewModel.locationName.collectAsStateWithLifecycle()

    // --- OTROS DATOS MAESTROS ---
    val unifiedServices by providerViewModel.unifiedServices.collectAsStateWithLifecycle()
    val favorites by providerViewModel.favoriteServices.collectAsStateWithLifecycle()
    val userState by profileViewModel.userState.collectAsStateWithLifecycle()
    
    // --- NUEVO: ESTADO DEL POPUP DE DIRECCIÓN ---
    val showAddressPopup by beViewModel.showAddressPopup.collectAsStateWithLifecycle()
    val hasAddresses = userState?.personalAddresses?.isNotEmpty() == true
    
    // Mostramos el popup si el usuario NO tiene direcciones y el flag de "una sola vez" es true
    val shouldDisplayPopup = !hasAddresses && showAddressPopup && userState != null

    // --- BANNERS DINÁMICOS (Optimizado: remember para evitar parpadeo y efectos de expansión) ---
    val activeFilters by promoViewModel.activeFilters.collectAsStateWithLifecycle()
    val bannerItems = remember(allRawCategories, unifiedServices, activeFilters) {
        promoViewModel.generateHomeBanners(allRawCategories, unifiedServices, activeFilters)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            ubicacionObrero.ejecutarCalculoUbicacionGps(context)
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            // 🔥 GPS INTELIGENTE (HUD V7): 
            // Solo disparamos la detección inicial si el usuario NO tiene nada seleccionado aún.
            // Esto evita que la ubicación se "resetee" al volver al Inicio.
            if (beViewModel.selectedAddressId.value == null) {
                ubicacionObrero.ejecutarCalculoUbicacionGps(context) { pais, prov, loc, calle, num, cp, lat, lng ->
                    val gpsLoc = AddressInfo(
                        id = "gps_current",
                        companyOrUserName = "Mi Ubicación",
                        branchName = "GPS Tracker",
                        streetAndNumber = if (calle.isNotBlank()) "$calle $num".trim() else loc,
                        locality = loc,
                        province = prov,
                        country = pais,
                        postalCode = cp,
                        lat = lat,
                        lng = lng,
                        isCompany = false
                    )
                    beViewModel.updateAddressFromGps(gpsLoc)
                }
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) ubicacionObrero.fetchWeather(lat = latitude!!, lon = longitude!!)
    }

    HomeScreenContent(
        navController = navController,
        userState = userState,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        onRefreshLocation = {
            if (ubicacionObrero.isGpsHabilitado(context)) {
                ubicacionObrero.ejecutarCalculoUbicacionGps(context) { pais, provincia, localidad, calle, numero, cp, lat, lng ->
                    val gpsLoc = AddressInfo(
                        id = "gps_current",
                        companyOrUserName = "Mi Ubicación",
                        branchName = "GPS Tracker",
                        streetAndNumber = if (calle.isNotBlank()) "$calle $numero".trim() else localidad,
                        locality = localidad,
                        province = provincia,
                        country = pais,
                        postalCode = cp,
                        lat = lat,
                        lng = lng,
                        isCompany = false
                    )
                    beViewModel.updateAddressFromGps(gpsLoc)
                }
            } else {
                Toast.makeText(context, "⚠️ El GPS está desactivado. Actívalo para actualizar tu ubicación.", Toast.LENGTH_SHORT).show()
            }
        },
        bannerItems = bannerItems,
        favoriteProviders = favorites,
        onLogout = { 
            profileViewModel.logout()
            onLogoutRoot() // USAMOS EL CALLBACK RAÍZ
        },
        beViewModel = beViewModel,
        categoryViewModel = categoryViewModel // El contenido pide al obrero directamente vía eventos
    )

    // ==================================================================================
    // --- 🚨 SECCIÓN: POPUP DE DIRECCIÓN MAVERICK (GLASS V5) ---
    // ==================================================================================
    if (shouldDisplayPopup) {
        ModernAddressPopup(
            onDismiss = { beViewModel.dismissAddressPopup() },
            onGoToProfile = {
                beViewModel.dismissAddressPopup()
                // Usamos la ruta definida en Screen.PerfilCliente
                navController.navigate(Screen.PerfilCliente.route) {
                    launchSingleTop = true
                }
            }
        )
    }
}

/**
 * --- COMPONENTE: MODERN ADDRESS POPUP ---
 * Estilo: Cyberpunk / Glassmorphism Maverick
 */
@Composable
fun ModernAddressPopup(
    onDismiss: () -> Unit,
    onGoToProfile: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaverickColors.AbsoluteBlack.copy(alpha = 0.85f))
                .border(1.dp, MaverickColors.GeminiBrush, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono Animado / Emoji Maverick
                Text("📍", fontSize = 48.sp)
                
                Text(
                    text = "¡Personaliza tu Experiencia!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = "Detectamos que aún no tienes una dirección guardada. Configúrala para que Maverick encuentre los mejores prestadores cerca de ti.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Acción: Ir al Perfil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaverickColors.GeminiBrush)
                        .clickable { onGoToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Configurar Dirección",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón Secundario: Quizás más tarde
                Text(
                    text = "Quizás más tarde",
                    color = MaverickColors.ElectricCyan,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { onDismiss() }
                )
            }
        }
    }
}

// ==================================================================================
// --- 🏠 SECCIÓN 2: CONTENIDO REACTIVO (UI CONSUME DEL CEREBRO) ---
// ==================================================================================
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    userState: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    onRefreshLocation: () -> Unit,
    bannerItems: List<AccordionBanner>,
    favoriteProviders: List<ServiceDisplayModel>,
    onLogout: () -> Unit,
    beViewModel: BeBrainViewModel,
    categoryViewModel: CategoryViewModel
) {
    // 🔥 CONSUMO EXCLUSIVO DEL OBRERO DE CATEGORÍAS (SSOT) 🔥
    val superCategories by categoryViewModel.superCategories.collectAsStateWithLifecycle()
    val sortedIndividualCategories by categoryViewModel.sortedCategories.collectAsStateWithLifecycle()
    val activeSortFilters by categoryViewModel.activeSortFilters.collectAsStateWithLifecycle()
    val isSuperCategoryView = remember(activeSortFilters) { activeSortFilters.contains("view_bento") }
    val showWeatherDetails by beViewModel.showWeatherDetails.collectAsStateWithLifecycle()
    val showFavoritesPanel by beViewModel.showFavoritesPanel.collectAsStateWithLifecycle()
    val isSearchActive by beViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState() // Usamos LazyGridState compatible
    val individualGridState = rememberLazyGridState()
    val listState = rememberLazyListState() // Para la lista de resultados expandidos
    val isScrolling by remember { derivedStateOf { gridState.isScrollInProgress || individualGridState.isScrollInProgress || listState.isScrollInProgress } }

    LaunchedEffect(activeSortFilters, isSearchActive) {
        // --- REPOSICIONAMIENTO AUTOMÁTICO (Optimizado: Solo en cambios estructurales, no en cada tecla) ---
        if (!isSearchActive) {
            listState.scrollToItem(0)
            gridState.scrollToItem(0)
            individualGridState.scrollToItem(0)
        }
    }

    // Sincronización de la búsqueda: Eliminamos el LaunchedEffect que llamaba a categoryViewModel.updateSearchQuery(searchQuery)
    // porque CategoryViewModel ya observa al Coordinator directamente (SSOT).

    Scaffold(containerColor = MaverickColors.StealthGray) { paddingValues ->
        // [SECCIÓN: FONDO DE PANTALLA] - Se utiliza Stealth Gray como base sólida
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp))
        ) {

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(150.dp))

                AnimatedVisibility(
                    visible = bannerItems.isNotEmpty() && !isSearchActive,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        PremiumLensCarouselV3(
                            items = bannerItems,
                            isPaused = isScrolling || showWeatherDetails || isSearchActive,
                            onItemClick = { banner ->
                                if (banner.service != null) navController.navigate("perfil_prestador/${banner.service.id}")
                                                                else if (banner.originalCategory != null) navController.navigate("result_busqueda/${Uri.encode(banner.originalCategory.name)}")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // ==================================================================================
                // --- 🛠️ SECCIÓN: EXPLORAR SERVICIOS (CONTENIDO DENTRO DE MOLDEBARRAMENU) ---
                // ==================================================================================
                MoldeBarraMenu(
                    labelCountMain = "BUSCA Y EXPLORA SERVICIOS!",
                    labelCountSub = "",
                    showCountBox = false, // Ocultamos la caja de conteo para el Home
                    modifier = Modifier.fillMaxWidth().weight(1f), // Ocupa todo el ancho y el resto del alto
                    customActions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // --- 🔘 SECCIÓN: BOTONES CON LABELS ---
                            // 1. Favoritos (Con emoji de fuego pegado al texto)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BotonMasUsados(
                                    isActive = activeSortFilters.contains("sort_hot"), 
                                    onClick = { beViewModel.triggerAction("sort_hot") }
                                )
                            }
                            // 2. Ordenar AZ
                            val alphaState = when {
                                activeSortFilters.contains("sort_nombre_asc") -> "asc"
                                activeSortFilters.contains("sort_nombre_desc") -> "desc"
                                else -> "none"
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BotonAlfabetico(
                                    orderState = alphaState,
                                    onStateChange = { newState ->
                                        val filterId = when (newState) {
                                            "asc" -> "sort_nombre_asc"
                                            "desc" -> "sort_nombre_desc"
                                            else -> if (alphaState == "asc") "sort_nombre_asc" else "sort_nombre_desc"
                                        }
                                        beViewModel.triggerAction(filterId)
                                    }
                                )

                            }

                            // 3. Compacto
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BotonVista(
                                    isBentoView = isSuperCategoryView, 
                                    isActive = !isSuperCategoryView, 
                                    onToggleView = { beViewModel.triggerAction(if (isSuperCategoryView) "view_grid" else "view_bento") }
                                )

                            }
                        }
                    }
                )
{
                    // --- CONTENIDO DE CATEGORÍAS (AHORA DENTRO DEL MOLDE) ---
                    if (isSuperCategoryView) {
                        if (isSearchActive && searchQuery.isNotEmpty()) {
                            // --- ESTADO BÚSQUEDA: Lista (LazyColumn) con tarjetas expandidas de ancho completo ---
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 2.dp, start = 4.dp, end = 4.dp, bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(items = superCategories, key = { it.title }) { superCat ->
                                    ExpandedBentoSuperCategoryCard(
                                        superCategory = superCat,
                                        onCategoryClick = { category -> navController.navigate("result_busqueda/${Uri.encode(category.name)}") },
                                        onToggleCategoryFavorite = { category -> categoryViewModel.toggleCategoryFavorite(category) }
                                    )
                                }
                            }
                        } else {
                            // --- ESTADO NORMAL: Grid (LazyVerticalGrid) con 2 por fila ---
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                state = gridState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 2.dp, start = 2.dp, end = 2.dp, bottom = 120.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(items = superCategories, key = { it.title }) { superCat ->
                                    BentoSuperCategoryCard(
                                        superCategory = superCat, 
                                        emoji = superCat.icon, 
                                        height = 145.dp,
                                        onClick = { beViewModel.selectSuperCategory(superCat) },
                                        onToggleFavorite = { categoryViewModel.toggleSuperCategoryFavorite(superCat.title) }
                                    )
                                }
                            }
                        }
                    } else {
                        // --- VISTA COMPACTA (INDIVIDUAL) ---
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            state = individualGridState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 2.dp, start = 4.dp, end = 4.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(items = sortedIndividualCategories, key = { it.name }) { category ->
                                CompactCategoryCard(
                                    item = category, 
                                    onClick = { navController.navigate("result_busqueda/${Uri.encode(category.name)}") },
                                    onToggleFavorite = { categoryViewModel.toggleCategoryFavorite(category) }
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().zIndex(10f)) {
                val currentLoc by beViewModel.selectedLocation.collectAsStateWithLifecycle()
                TopHeaderSection(
                    navController = navController,
                    user = userState,
                    temperature = temperature,
                    weatherEmoji = weatherEmoji,
                    weatherDescription = weatherDescription,
                    cityName = cityName,
                    currentLocationState = currentLoc ?: LocationOption.Gps(
                        address = cityName,
                        locality = "Ubicación Actual"
                    ),
                    onWeatherClick = { beViewModel.toggleWeatherDetails() },
                    onRefreshLocation = { onRefreshLocation() },
                    onLocationSelected = { loc ->
                        // Extraemos el ID del objeto LocationOption (Personal/Business) para activar la dirección real
                        val targetId = when (loc) {
                            is LocationOption.Personal -> loc.id
                            is LocationOption.Business -> loc.id
                            is LocationOption.Gps -> "gps_current"
                        }
                        
                        if (targetId.isNotEmpty()) {
                            // Activamos la dirección en el Cerebro (Fuente de Verdad Única)
                            beViewModel.selectAddress(targetId)
                        }
                    },
                    onLogout = onLogout,
                    beViewModel = beViewModel,
                    onResultClick = { result ->
                        when (result) {
                            is CategoryEntity -> navController.navigate("result_busqueda/${Uri.encode(result.name)}")
                        }
                    }
                )
            }

            if (showWeatherDetails) {
                Box(modifier = Modifier.fillMaxSize().zIndex(15f).background(Color.Black.copy(alpha = 0.5f)).clickable { beViewModel.setWeatherDetailsVisible(false) })
            }
            AnimatedVisibility(
                visible = showWeatherDetails, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 60.dp).zIndex(20f)
            ) { WeatherExpandedCard(temperature, weatherEmoji, weatherDescription, cityName, emptyList()) }

            if (showFavoritesPanel) {
                Box(modifier = Modifier.fillMaxSize().zIndex(11f).background(Color.Black.copy(alpha = 0.65f)).clickable(null, null) { beViewModel.setFavoritesPanelVisible(false) })
            }
            AnimatedVisibility(visible = showFavoritesPanel, enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }, modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)) {
                FavoritesPanel(navController, favoriteProviders) { beViewModel.setFavoritesPanelVisible(false) }
            }

            SuperCategoryDetailsPanel(
                beViewModel = beViewModel, 
                categoryViewModel = categoryViewModel,
                onCategoryClick = { categoryName -> navController.navigate("result_busqueda/${Uri.encode(categoryName)}") }
            )
        }
    }
}

// --- SECCIÓN: PANEL DE FAVORITOS (UNIFICADO) ---
// ==================================================================================
@Composable
fun FavoritesPanel(
    navController: NavHostController, 
    favorites: List<ServiceDisplayModel>, 
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(320.dp), 
        color = MaverickColors.StealthGray, // Sincronizado con el fondo de la Home
        tonalElevation = 16.dp, 
        shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mis Favoritos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onClose, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }
            HorizontalDivider(color = Color.White.copy(0.1f))
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (favorites.isEmpty()) {
                    item { Text("No tienes favoritos guardados.", modifier = Modifier.fillMaxWidth().padding(32.dp), color = Color.Gray, textAlign = TextAlign.Center) }
                } else {
                    items(items = favorites, key = { it.id }) { service ->
                        PrestadorCardV3(
                            provider = service,
                            isCompact = false,
                            onClick = {
                                onClose()
                                navController.navigate("perfil_prestador/${service.id}")
                            },
                            onChatClick = {
                                onClose()
                                navController.navigate("chat?providerId=${service.id}")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

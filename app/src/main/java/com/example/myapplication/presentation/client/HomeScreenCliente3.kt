package com.example.myapplication.presentation.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.CyberMaverickSleekTitle
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlin.collections.isNotEmpty

// ==================================================================================
// --- 🏠 SECCIÓN 1: ORQUESTADOR DE PANTALLA (INTERMEDIARIO BEBRAIN) ---
// ==================================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    promoViewModel: PromoViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel(),
    interactionViewModel: BeInteractionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ==================================================================================
    // --- 🛠️ SUBSECCIÓN: TRABAJO SUCIO (COLECTA DE LOS OBREROS) ---
    // ==================================================================================
    
    // Obrero 1: Ubicación y Clima
    val obreroTemp by ubicacionObrero.temperature.collectAsStateWithLifecycle()
    val obreroEmoji by ubicacionObrero.weatherEmoji.collectAsStateWithLifecycle()
    val obreroDesc by ubicacionObrero.weatherDescription.collectAsStateWithLifecycle()
    val obreroCity by ubicacionObrero.locationName.collectAsStateWithLifecycle()
    val latitude by ubicacionObrero.latitude.collectAsStateWithLifecycle()
    val longitude by ubicacionObrero.longitude.collectAsStateWithLifecycle()

    // Obrero 2: Categorías
    val obreroSortedCats by categoryViewModel.sortedCategories.collectAsStateWithLifecycle()
    val obreroSuperCats by categoryViewModel.superCategories.collectAsStateWithLifecycle()
    val obreroCatFilters by categoryViewModel.activeSortFilters.collectAsStateWithLifecycle()
    val allRawCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    // ==================================================================================
    // --- 🧠 SUBSECCIÓN: SINCRONIZACIÓN OBREROS -> CEREBRO (INTERMEDIARIO) ---
    // ==================================================================================
    
    // Sincronización Clima/Ubicación
    LaunchedEffect(obreroTemp, obreroEmoji, obreroDesc, obreroCity) {
        beViewModel.syncWeather(obreroTemp, obreroEmoji, obreroDesc, obreroCity)
    }

    // Sincronización Categorías
    LaunchedEffect(obreroSortedCats, obreroSuperCats) {
        beViewModel.syncCategories(obreroSortedCats, obreroSuperCats)
    }
    LaunchedEffect(obreroCatFilters) {
        beViewModel.syncFilters(obreroCatFilters)
    }
    LaunchedEffect(allRawCategories) {
        beViewModel.syncAllCategories(allRawCategories)
    }

    // 🔥 NUEVO: ESCUCHA DE ACCIONES DEL CEREBRO PARA EL OBRERO (ORQUESTACIÓN) 🔥
    LaunchedEffect(Unit) {
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
                    val index = actionId.removePrefix("talk_").toIntOrNull()
                    if (index != null) {
                        interactionViewModel.restoreFromHistory(index)
                    }
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
    
    // --- BANNERS DINÁMICOS ---
    val bannerItems by promoViewModel.getHomeBanners(allRawCategories, unifiedServices).collectAsStateWithLifecycle(initialValue = emptyList())

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            ubicacionObrero.ejecutarCalculoUbicacionGps(context)
        }
    }

    LaunchedEffect(userState) { if (userState != null) beViewModel.updateProfile(userState) }

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
        },
        bannerItems = bannerItems,
        favoriteProviders = favorites,
        onLogout = { profileViewModel.logout(); navController.navigate(Screen.Login.route) { popUpTo(0) } },
        beViewModel = beViewModel,
        interactionViewModel = interactionViewModel,
        categoryViewModel = categoryViewModel // El contenido pide al obrero directamente vía eventos
    )
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
    interactionViewModel: BeInteractionViewModel,
    categoryViewModel: CategoryViewModel
) {
    // 🔥 CONSUMO EXCLUSIVO DEL CEREBRO (Sincronizado) 🔥
    val superCategories by beViewModel.superCategories.collectAsStateWithLifecycle()
    val sortedIndividualCategories by beViewModel.sortedCategories.collectAsStateWithLifecycle()
    val activeSortFilters by beViewModel.activeSortFilters.collectAsStateWithLifecycle()
    val isSuperCategoryView by beViewModel.isSuperCategoryView.collectAsStateWithLifecycle()
    val searchResults by beViewModel.searchResults.collectAsStateWithLifecycle()

    val availableSorts by beViewModel.availableSortOptions.collectAsStateWithLifecycle()
    val availableFilters by beViewModel.availableFilters.collectAsStateWithLifecycle()
    val dynamicCategories by beViewModel.dynamicCategories.collectAsStateWithLifecycle()

    val unifiedServices by beViewModel.allProvidersRaw.collectAsStateWithLifecycle()
    // Sincronizar InteractionViewModel con los datos del Cerebro
    LaunchedEffect(availableSorts, availableFilters, dynamicCategories, searchResults, unifiedServices) {
        interactionViewModel.syncResources(
            filters = availableFilters,
            sorts = availableSorts,
            categories = dynamicCategories,
            results = searchResults,
            chats = unifiedServices
        )
    }

    val showWeatherDetails by beViewModel.showWeatherDetails.collectAsStateWithLifecycle()
    val showFavoritesPanel by beViewModel.showFavoritesPanel.collectAsStateWithLifecycle()
    val isSearchActive by beViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()

    val gridState = rememberLazyGridState() // Usamos LazyGridState compatible
    val individualGridState = rememberLazyGridState()
    val listState = rememberLazyListState() // Para la lista de resultados expandidos
    val isScrolling by remember { derivedStateOf { gridState.isScrollInProgress || individualGridState.isScrollInProgress || listState.isScrollInProgress } }

    LaunchedEffect(activeSortFilters, isSuperCategoryView, isSearchActive, searchQuery) {
        // --- REPOSICIONAMIENTO AUTOMÁTICO ---
        // Siempre posicionamos arriba al cambiar la búsqueda o resultados
        listState.animateScrollToItem(0)
        gridState.animateScrollToItem(0)
        individualGridState.animateScrollToItem(0)
    }

    // Sincronización de la búsqueda de Be hacia el Obrero de Categorías e InteractionViewModel
    val hasMatches by categoryViewModel.hasMatches.collectAsStateWithLifecycle()
    LaunchedEffect(searchQuery, searchResults, hasMatches) {
        categoryViewModel.updateSearchQuery(searchQuery)
        interactionViewModel.processSearchQuery(searchQuery, hasMatches)
        interactionViewModel.updateResults(searchResults, HUDContext.HOME)
    }

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
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        PremiumLensCarouselV3(
                            items = bannerItems,
                            isPaused = isScrolling || showWeatherDetails || isSearchActive,
                            onItemClick = { banner ->
                                if (banner.service != null) navController.navigate("perfil_prestador/${banner.service.id}")
                                else if (banner.originalCategory != null) navController.navigate("result_busqueda/${banner.originalCategory.name}")
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
                                /**
                                Spacer(modifier = Modifier.height(0.2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                   // Text("🔥", fontSize = 10.sp)
                                    Text(
                                        text = "Fav",
                                        color = Color.White.copy(alpha = 0.9f), 
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                **/
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
                                                                      /**
                                )
                                Spacer(modifier = Modifier.height(0.2.dp))
                                Text(
                                    text = "AZ",
                                    color = Color.White.copy(alpha = 0.9f), 
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                **/
                            }

                            // 3. Compacto
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                BotonVista(
                                    isBentoView = isSuperCategoryView, 
                                    isActive = !isSuperCategoryView, 
                                    onToggleView = { beViewModel.triggerAction(if (isSuperCategoryView) "view_grid" else "view_bento") }
                                )
                                /**
                                Spacer(modifier = Modifier.height(0.2.dp))
                                Text(
                                    text = "Comp",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                **/
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
                                        onCategoryClick = { category -> navController.navigate("result_busqueda/${category.name}") },
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
                                    onClick = { navController.navigate("result_busqueda/${category.name}") },
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
                    interactionViewModel = interactionViewModel,
                    onResultClick = { result ->
                        when (result) {
                            is CategoryEntity -> navController.navigate("result_busqueda/${result.name}")
                            // is SuperCategory -> beViewModel.selectSuperCategory(result)
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
                FavoritesPanel(navController, favoriteProviders, { beViewModel.setFavoritesPanelVisible(false) })
            }

            SuperCategoryDetailsPanel(
                beViewModel = beViewModel, 
                categoryViewModel = categoryViewModel,
                onCategoryClick = { categoryName -> navController.navigate("result_busqueda/$categoryName") }
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

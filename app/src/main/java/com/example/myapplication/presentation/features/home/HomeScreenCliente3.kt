package com.example.myapplication.presentation.features.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.data.model.ProviderDisplayModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.features.profile.ProfileViewModel
import com.example.myapplication.presentation.features.profile.ProviderViewModel
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.registry.BeDictionary
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

// ==================================================================================
// --- 🏠 SECCIÓN 1: ORQUESTADOR DE PANTALLA (INTERMEDIARIO BEBRAIN) ---
// ==================================================================================
/**
 * HomeScreenComplete: El Orquestador de Elite.
 * Se encarga de la recolección de flujos de los ViewModels y la navegación de alto nivel.
 * Sigue el protocolo de comunicación Cerebro-Obrero.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenComplete(
    navController: NavHostController,
    onLogoutRoot: () -> Unit = {}, 
    profileViewModel: ProfileViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    promoViewModel: PromoViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val allRawCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    // --- ESCUCHA DE ACCIONES DEL CEREBRO (Elite SSOT) ---
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
            }
        }
    }

    // --- SUSCRIPCIÓN A FAVORITOS (PERSISTIDOS EN SHORTCUTS) ---
    val favoriteShortcuts by categoryViewModel.getShortcuts("provider").collectAsStateWithLifecycle(emptyList())
    val favoriteIds = remember(favoriteShortcuts) { favoriteShortcuts.map { it.targetId }.toSet() }
    
    val allServices by providerViewModel.unifiedServices.collectAsStateWithLifecycle()
    val favorites = remember(allServices, favoriteIds) { allServices.filter { it.id in favoriteIds } }

    val userState by profileViewModel.userState.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val showAddressPopup by beViewModel.showAddressPopup.collectAsStateWithLifecycle()
    val hasAddresses = userState?.personalAddresses?.isNotEmpty() == true
    val shouldDisplayPopup = (!hasAddresses) && showAddressPopup && (userState != null)

    val activeFilters by promoViewModel.activeFilters.collectAsStateWithLifecycle()
    val bannerItems = remember(allRawCategories, allServices, activeFilters) {
        promoViewModel.generateHomeBanners(allRawCategories, allServices, activeFilters)
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

    HomeScreenContent(
        navController = navController,
        bannerItems = bannerItems,
        favoriteProviders = favorites,
        onLogout = {
            profileViewModel.logout()
            onLogoutRoot()
        },
        beViewModel = beViewModel,
        categoryViewModel = categoryViewModel,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                //categoryViewModel.syncCategoriesWithFirebase()
                delay(1000)
                isRefreshing = false
          }
        },
        ubicacionObrero = ubicacionObrero
    )
    // --- SECCIÓN: POPUPS DE SISTEMA (RESILIENCIA) ---
    var syncError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        beViewModel.syncErrorEvent.collectLatest { error ->
            syncError = error
        }
    }

    PopUpEmergenteMolde(
        isVisible = syncError != null,
        onDismissRequest = { syncError = null },
        title = "Sincronización Limitada",
        subtitle = "Modo Offline Activo",
        emoji = "🔄",
        accentColor = Color(0xFFF59E0B) // Naranja Maverick
    ) {
        PopUpSectionHeader(text = "Estado de Red", emoji = "📡")
        Text(
            text = "No pudimos sincronizar tus datos más recientes con la nube. La aplicación seguirá funcionando con la información guardada localmente.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(16.dp))
        PopUpDetailSection(emoji = "⚠️", label = "Detalle Técnico", value = syncError ?: "Error desconocido")

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "ENTENDIDO",
            onClick = { syncError = null },
            backgroundColor = Color(0xFFF59E0B)
        )
    }

    if (shouldDisplayPopup) {
        ModernAddressPopup(
            onDismiss = { beViewModel.dismissAddressPopup() },
            onGoToProfile = {
                beViewModel.dismissAddressPopup()
                navController.navigate("perfil_cliente") { launchSingleTop = true }
            }
        )
    }
}

// ==================================================================================
// --- 🏠 SECCIÓN 2: MEDIADOR DE ESTADO (CONEXIÓN CEREBRO-UI) ---
// ==================================================================================
/**
 * HomeScreenContent: Puente de estados.
 * Extrae los valores de los Flow y los pasa como parámetros simples a la UI Stateless.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContent(
    navController: NavHostController,
    bannerItems: List<AccordionBanner>,
    favoriteProviders: List<ProviderDisplayModel>,
    onLogout: () -> Unit,
    beViewModel: BeBrainViewModel,
    categoryViewModel: CategoryViewModel,
    isRefreshing: Boolean,
    isLoadingCategories: Boolean = false,
    onRefresh: () -> Unit,
    ubicacionObrero: UbicacionClimaViewModel
) {
    val superCategories by categoryViewModel.superCategories.collectAsStateWithLifecycle()
    val sortedIndividualCategories by categoryViewModel.sortedCategories.collectAsStateWithLifecycle()
    val activeSortFilters by categoryViewModel.activeSortFilters.collectAsStateWithLifecycle()

    val temperature by beViewModel.temperature.collectAsStateWithLifecycle()
    val weatherEmoji by beViewModel.weatherEmoji.collectAsStateWithLifecycle()
    val weatherDescription by beViewModel.weatherDescription.collectAsStateWithLifecycle()
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val cityName by beViewModel.locationName.collectAsStateWithLifecycle()
    val showWeatherDetails by beViewModel.showWeatherDetails.collectAsStateWithLifecycle()
    val showFavoritesPanel by beViewModel.showFavoritesPanel.collectAsStateWithLifecycle()
    val searching by beViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()

    // --- [ELITE] ESTADO DE TRANSICIÓN DE BÚSQUEDA ---
    var searchAnimationSettled by remember { mutableStateOf(false) }
    LaunchedEffect(searching) {
        if (searching) {
            delay(450) // Tiempo para que la barra de Be se despliegue suavemente
            searchAnimationSettled = true
        } else {
            searchAnimationSettled = false
        }
    }

    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val activeName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val activePhoto by beViewModel.activeProfilePhotoUrl.collectAsStateWithLifecycle()
    val selectedProfileId by beViewModel.selectedProfileId.collectAsStateWithLifecycle()
    val selectedSuperCategory by beViewModel.selectedSuperCategory.collectAsStateWithLifecycle()
    val isInitialLoading by categoryViewModel.isInitialLoading.collectAsStateWithLifecycle()
    val isSearchingCategories by categoryViewModel.isSearching.collectAsStateWithLifecycle()

    // --- SUSCRIPCIÓN A RECIENTES (ELITE) ---
    val homeShortcuts by categoryViewModel.getShortcuts("home").collectAsStateWithLifecycle(emptyList())
    val shortcutIds = remember(homeShortcuts) { homeShortcuts.map { it.targetId }.toSet() }

    val context = LocalContext.current

    val isSuperCategoryView = activeSortFilters.contains("view_bento")

    val dropdownItems = remember(isSuperCategoryView) {
        listOfNotNull(
            BeDictionary.Sorts["sort_hot"],
            BeDictionary.Sorts["sort_nombre_asc"],
            BeDictionary.Sorts["sort_random"],
            BeDictionary.Sorts[if (isSuperCategoryView) "view_grid" else "view_bento"]
        )
    }

    HomeScreenContentStateless(
        navController = navController,
        bannerItems = bannerItems,
        favoriteProviders = favoriteProviders,
        superCategories = superCategories,
        sortedIndividualCategories = sortedIndividualCategories,
        activeSortFilters = activeSortFilters,
        dropdownItems = dropdownItems,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        isSuperCategoryView = activeSortFilters.contains("view_bento"),
        showWeatherDetails = showWeatherDetails,
        showFavoritesPanel = showFavoritesPanel,
        searching = searching,
        searchQuery = searchQuery,
        userFromBrain = userFromBrain,
        activeName = activeName,
        activePhoto = activePhoto,
        selectedProfileId = selectedProfileId,
        activeAddress = activeAddress,
        selectedSuperCategory = selectedSuperCategory,
        isRefreshing = isRefreshing,
        isLoadingCategories = isInitialLoading || isSearchingCategories,
        onRefresh = onRefresh,
        onLogout = onLogout,
        onTriggerAction = { beViewModel.triggerAction(it) },
        onSelectSuperCategory = { beViewModel.selectSuperCategory(it) },
        onToggleSuperCategoryFavorite = { title -> 
            val isAdd = !shortcutIds.contains(title)
            categoryViewModel.manageShortcut("home", title, "supercategory", isAdd, title, "📂") 
        },
        onToggleCategoryFavorite = { category -> 
            val isAdd = !shortcutIds.contains(category.name)
            categoryViewModel.manageShortcut("home", category.name, "category", isAdd, category.name, category.icon)
        },
        onSetWeatherDetailsVisible = { beViewModel.setWeatherDetailsVisible(it) },
        onSetFavoritesPanelVisible = { beViewModel.setFavoritesPanelVisible(it) },
        onRefreshLocation = { ubicacionObrero.ejecutarCalculoUbicacionGps(context) },
        onProfileSelected = { beViewModel.selectProfile(it) },
        onWeatherClick = { beViewModel.toggleWeatherDetails() },
        shortcutIds = shortcutIds,
        superCategoryFavorites = shortcutIds, // Usamos la misma fuente de verdad
        onManageShortcut = { id, type, add, label, icon -> categoryViewModel.manageShortcut("home", id, type, add, label, icon) },
        beViewModel = beViewModel,
        categoryViewModel = categoryViewModel,
        searchAnimationSettled = searchAnimationSettled
    )
}

// ==================================================================================
// --- 🏠 SECCIÓN 3: UI PURA (STATELESS SCREEN) ---
// ==================================================================================
/**
 * HomeScreenContentStateless: La representación visual "tonta".
 * No conoce ViewModels. Solo reacciona a parámetros y emite eventos.
 * Diseñada para permitir Previews instantáneas y pruebas de diseño.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenContentStateless(
    navController: NavHostController,
    bannerItems: List<AccordionBanner>,
    favoriteProviders: List<ProviderDisplayModel>,
    superCategories: List<SuperCategory>,
    sortedIndividualCategories: List<CategoryEntity>,
    activeSortFilters: Set<String>,
    dropdownItems: List<DropdownItemData> = emptyList(),
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    isSuperCategoryView: Boolean,
    showWeatherDetails: Boolean,
    showFavoritesPanel: Boolean,
    searching: Boolean,
    searchQuery: String,
    userFromBrain: UserEntity?,
    activeName: String,
    activePhoto: String?,
    selectedProfileId: String?,
    activeAddress: AddressInfo?,
    selectedSuperCategory: SuperCategory?,
    isRefreshing: Boolean,
    isLoadingCategories: Boolean = false,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onTriggerAction: (String) -> Unit,
    onSelectSuperCategory: (SuperCategory?) -> Unit,
    onToggleSuperCategoryFavorite: (String) -> Unit,
    onToggleCategoryFavorite: (CategoryEntity) -> Unit,
    onSetWeatherDetailsVisible: (Boolean) -> Unit,
    onSetFavoritesPanelVisible: (Boolean) -> Unit,
    onRefreshLocation: () -> Unit,
    onProfileSelected: (String?) -> Unit,
    shortcutIds: Set<String> = emptySet(),
    superCategoryFavorites: Set<String> = emptySet(),
    onManageShortcut: (String, String, Boolean, String?, String?) -> Unit = { _, _, _, _, _ -> },
    beViewModel: BeBrainViewModel,
    categoryViewModel: CategoryViewModel,
    searchAnimationSettled: Boolean,
    onWeatherClick: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 180f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return if (scrollAccumulator >= 180f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val hideFraction = remember {
        derivedStateOf { (scrollAccumulator / 180f).coerceIn(0f, 1f) }
    }

    val gridState = rememberLazyGridState()
    val individualGridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val scrollState = rememberLazyListState()
    val isScrolling by remember { derivedStateOf { gridState.isScrollInProgress || individualGridState.isScrollInProgress || listState.isScrollInProgress || scrollState.isScrollInProgress } }

    LaunchedEffect(activeSortFilters, searching) {
        if (!searching) {
            listState.scrollToItem(0)
            gridState.scrollToItem(0)
            individualGridState.scrollToItem(0)
            scrollState.scrollToItem(0)
        }
    }

    Scaffold(containerColor = MaverickColors.V2TechSurface) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp))
                .nestedScroll(nestedScrollConnection)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        color = MaverickColors.ElectricCyan,
                        containerColor = MaverickColors.ROG_Dark_Bg.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopHeaderSectionContentV2(
                        navController = navController,
                        user = userFromBrain,
                        activeName = activeName,
                        activePhoto = activePhoto,
                        isPersonalProfile = selectedProfileId == null,
                        selectedProfileId = selectedProfileId,
                        temperature = temperature,
                        weatherEmoji = weatherEmoji,
                        weatherDescription = weatherDescription,
                        activeAddress = activeAddress ?: AddressInfo(id = "searching", companyOrUserName = "...", branchName = "...", streetAndNumber = "...", locality = cityName, postalCode = "", isCompany = false, lat = 0.0, lng = 0.0),
                        onWeatherClick = onWeatherClick,
                        onRefreshLocation = onRefreshLocation,
                        onLocationSelected = { },
                        onProfileSelected = onProfileSelected,
                        onLogout = onLogout,
                        userFromBrain = userFromBrain,
                        showWeatherDialog = showWeatherDetails,
                        cityName = cityName,
                        onSetWeatherDetailsVisible = onSetWeatherDetailsVisible
                    )

                    AnimatedVisibility(
                        visible = bannerItems.isNotEmpty() && !searching,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 12.dp * (1f - hideFraction.value))
                                .graphicsLayer {
                                    alpha = 1f - hideFraction.value
                                    translationY = -20.dp.toPx() * hideFraction.value
                                }
                                .height(150.dp * (1f - hideFraction.value))
                        ) {
                            PremiumLensCarouselV3(
                                items = bannerItems,
                                isPaused = isScrolling || showWeatherDetails || searching,
                                onItemClick = { banner ->
                                    if (banner.service != null) {
                                        navController.navigate("perfil_prestador/${banner.service.id}")
                                    } else {
                                        banner.originalCategory?.let {
                                            val encodedName = Uri.encode(it.name)
                                            if (encodedName.isNotEmpty()) {
                                                navController.navigate("result_busqueda/$encodedName")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    ListaMoldeV2(
                        modifier = Modifier.fillMaxSize(),
                        titulo = if (searching && searchQuery.isNotEmpty() && sortedIndividualCategories.isNotEmpty()) "Encontré estos servicios" else "Busca y explora servicios",
                        subtitulo = "Módulo de Exploración",
                        emoji = null,
                        compactInfo = "Explorar",
                        filtrosActivos = emptyList(),
                        state = scrollState,
                        containerColor = MaverickColors.EliteSurface,
                        acciones = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                var showMenu by remember { mutableStateOf(false) }

                                Box(contentAlignment = Alignment.Center) {
                                    BotonCabeceraAccion(
                                        onClick = { showMenu = !showMenu },
                                        icon = if (showMenu) Icons.Rounded.Close else Icons.Rounded.Menu,
                                        color = if (showMenu) MaverickColors.DeepRed else Color.White
                                    )
                                   MoldeEliteBottomSheetV2(
                                        visible = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        items = dropdownItems,
                                        activeFilters = activeSortFilters,
                                        shortcutIds = emptySet<String>(), 
                                        onToggle = { actionId: String ->
                                            onTriggerAction(actionId)
                                            showMenu = false
                                        },
                                        onManageShortcuts = { _: String, _: Boolean -> }
                                    )
                                }
                            }
                        }
                    ) {
                        val columns = if (isSuperCategoryView) 2 else 3

                        if (isLoadingCategories || (searching && !searchAnimationSettled)) {
                            items(4) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(columns) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (isSuperCategoryView) SuperCategoryCardShimmer() else CategoryCardShimmer()
                                        }
                                    }
                                }
                            }
                        } else if (isSuperCategoryView) {
                            if (searching && searchQuery.isNotEmpty()) {
                                val searchRows = sortedIndividualCategories.chunked(columns)
                                items(searchRows) { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                        rowItems.forEach { category ->
                                            Box(modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 5.dp, horizontal = 2.dp)
                                            ) {
                                                CompactCategoryCard(
                                                    item = category,
                                                    onClick = {
                                                        val encodedName = Uri.encode(category.name)
                                                        if (encodedName.isNotEmpty()) {
                                                            navController.navigate("result_busqueda/$encodedName")
                                                        }
                                                    },
                                                    onToggleFavorite = { onToggleCategoryFavorite(category) },
                                                    isShortcut = shortcutIds.contains(category.name),
                                                    onManageShortcut = { isAdd, label, icon ->
                                                        onManageShortcut(category.name, "category", isAdd, label, icon)
                                                    },
                                                    showSuperCategoryLabel = true,
                                                    isSuperCategoryFavorite = superCategoryFavorites.contains(category.superCategory)
                                                )
                                            }
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                val superCatRows = superCategories.chunked(columns)
                                items(superCatRows) { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                        rowItems.forEach { superCat ->
                                            Box(modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp, vertical = 3.dp)
                                            ) {
                                                BentoSuperCategoryCard(
                                                    superCategory = superCat,
                                                    emoji = superCat.icon,
                                                    height = 130.dp,
                                                    onClick = { onSelectSuperCategory(superCat) },
                                                    onToggleFavorite = { onToggleSuperCategoryFavorite(superCat.title) },
                                                    isShortcut = shortcutIds.contains(superCat.title),
                                                    onManageShortcut = { isAdd, label, icon -> 
                                                        onManageShortcut(superCat.title, "supercategory", isAdd, label, icon)
                                                    }
                                                )
                                            }
                                        }
                                        repeat(columns - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            val individualRows = sortedIndividualCategories.chunked(columns)
                            items(individualRows) { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                    rowItems.forEach { category ->
                                        Box(modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 5.dp, horizontal = 2.dp)
                                        ) {
                                            CompactCategoryCard(
                                                item = category,
                                                onClick = {
                                                    val encodedName = Uri.encode(category.name)
                                                    if (encodedName.isNotEmpty()) {
                                                        navController.navigate("result_busqueda/$encodedName")
                                                    }
                                                },
                                                onToggleFavorite = { onToggleCategoryFavorite(category) },
                                                isShortcut = shortcutIds.contains(category.name),
                                                onManageShortcut = { isAdd, label, icon ->
                                                    onManageShortcut(category.name, "category", isAdd, label, icon)
                                                },
                                                showSuperCategoryLabel = searching && searchQuery.isNotEmpty(),
                                                isSuperCategoryFavorite = superCategoryFavorites.contains(category.superCategory)
                                            )
                                        }
                                    }
                                    repeat(columns - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showFavoritesPanel) {
                Box(modifier = Modifier.fillMaxSize().zIndex(11f).background(Color.Black.copy(alpha = 0.65f)).clickable(null, null) { onSetFavoritesPanelVisible(false) })
            }
            AnimatedVisibility(visible = showFavoritesPanel, enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }, modifier = Modifier.align(Alignment.CenterEnd).zIndex(12f)) {
                FavoritesPanel(navController, favoriteProviders) { onSetFavoritesPanelVisible(false) }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).zIndex(50f)) {
                SuperCategoryDetailsPanel(
                    beViewModel = beViewModel,
                    categoryViewModel = categoryViewModel,
                    onCategoryClick = { categoryName ->
                        val encodedName = Uri.encode(categoryName)
                        if (encodedName.isNotEmpty()) {
                            navController.navigate("result_busqueda/$encodedName")
                        }
                    }
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN 4: COMPONENTES AUXILIARES Y PREVIEWS ---
// ==================================================================================

@Composable
fun FavoritesPanel(
    navController: NavHostController,
    favorites: List<ProviderDisplayModel>,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxHeight().width(320.dp),
        color = MaverickColors.StealthGray,
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

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFF07050E)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenPreview() {
    val sampleCategories = listOf(
        CategoryEntity(name = "Limpieza", icon = "🧹", superCategory = "Hogar", isNew = true, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Plomería", icon = "🪠", superCategory = "Hogar", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Electricidad", icon = "⚡", superCategory = "Hogar", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Peluquería", icon = "✂️", superCategory = "Belleza", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Masajes", icon = "💆", superCategory = "Belleza", isNew = false, isNewPrestador = false, isAd = false)
    )

    // Simulamos una supercategoría con color dinámico (Rosa Suave)
    val sampleSuperCategory = SuperCategory(
        title = "Hogar y Construcción", 
        icon = "🏠", 
        totalItems = 3, 
        isFavorite = true,
        color = 0xFFFFD1D1
    )
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            SuperCategoryDetailsPanelContent(
                selectedSuperCategory = sampleSuperCategory,
                items = sampleCategories.take(3),
                searchQuery = "",
                onClose = {},
                onCategoryClick = {}
            )
        }
    }
}

package com.example.myapplication.presentation.features.home

import com.example.myapplication.core.domain.model.AddressUnico
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
import com.example.myapplication.presentation.features.profile.UserViewModel
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
    userViewModel: UserViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    promoViewModel: PromoViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel()
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
    val allServices by providerViewModel.favoriteProviders.collectAsStateWithLifecycle()
    val favorites = allServices

    val userState by userViewModel.userState.collectAsStateWithLifecycle()

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
                    val gpsLoc = AddressUnico(
                        id = "gps_current",
                        ownerName = "Mi Ubicación",
                        label = "GPS Tracker",
                        calle = calle,
                        numero = num,
                        localidad = loc,
                        provincia = prov,
                        pais = pais,
                        codigoPostal = cp,
                        latitude = lat,
                        longitude = lng,
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
            userViewModel.logout()
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
    val activePhoto by beViewModel.activeProfilePhoto.collectAsStateWithLifecycle()
    val selectedProfileId by beViewModel.selectedProfileId.collectAsStateWithLifecycle()
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

    HomeScreenClienteV4(
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
        onGpsToggle = { ubicacionObrero.toggleGps(context) },
        onProfileSelected = { beViewModel.selectProfile(it) },
        onWeatherClick = { beViewModel.toggleWeatherDetails() },
        onLocationSelected = { beViewModel.selectAddress(it.id) },
        shortcutIds = shortcutIds,
        superCategoryFavorites = shortcutIds,
        onManageShortcut = { id, type, add, label, icon -> categoryViewModel.manageShortcut("home", id, type, add, label, icon) },
        searchAnimationSettled = searchAnimationSettled,
        detailsPanel = {
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
    )
}


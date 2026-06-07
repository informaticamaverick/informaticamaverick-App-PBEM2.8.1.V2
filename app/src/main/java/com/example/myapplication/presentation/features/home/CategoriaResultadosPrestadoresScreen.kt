package com.example.myapplication.presentation.features.home

import com.example.myapplication.core.domain.model.AddressUnico
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.features.profile.ProviderViewModel
import com.example.myapplication.presentation.features.profile.ProviderUiItem
import com.example.myapplication.presentation.features.profile.UserLocation
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==================================================================================
// --- 🛰️ SECCIÓN 1: ORQUESTADOR DE RESULTADOS (MODO ELITE) ---
// ==================================================================================
/**
 * ResultBusquedaCategoriaScreen: Orquestador de la pantalla de resultados.
 */
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String, String?, String?) -> Unit,
    onNavigateToChat: (ProviderDisplayModel, String?, String?) -> Unit, // 🔥 [ELITE] Ahora acepta clientCompanyId (deprecated) y branchId
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val userState by beViewModel.userState.collectAsStateWithLifecycle()
    val activeProfilePhoto by beViewModel.activeProfilePhoto.collectAsStateWithLifecycle()
    val activeProfileName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val selectedProfileId by beViewModel.selectedProfileId.collectAsStateWithLifecycle()

    val pagedItems = providerViewModel.pagedProviders.collectAsLazyPagingItems()
    
    val showSubscribedOnly by providerViewModel.showSubscribedOnly.collectAsStateWithLifecycle()
    val activeRefinements by providerViewModel.activeRefinements.collectAsStateWithLifecycle()
    val sortByProximity by providerViewModel.sortByProximity.collectAsStateWithLifecycle()
    val userLocation by providerViewModel.userLocation.collectAsStateWithLifecycle()

    val favoriteIds by providerViewModel.favoriteProviderIds.collectAsStateWithLifecycle()
    val activeSortCriteria by providerViewModel.activeSortCriteria.collectAsStateWithLifecycle()
    val isGpsEnabled by ubicacionViewModel.isGpsEnabled.collectAsStateWithLifecycle()

    // --- ESTADO DE FILTROS ELEVADO (COORDINACIÓN DE COLAPSO) ---
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    // --- SINCRONIZACIÓN DE CONTEXTO ELITE ---
    LaunchedEffect(Unit) {
        beViewModel.updateHUDContext(HUDContext.SEARCH_RESULTS)
        beViewModel.setShowBeTools(true)
    }

    // --- SINCRONIZACIÓN TÁCTICA UNIFICADA ---
    LaunchedEffect(categoryName, activeAddress) {
        val decodedName = Uri.decode(categoryName)
        providerViewModel.onCategorySelected(decodedName) 

        activeAddress?.let { addr ->
            providerViewModel.refreshData(decodedName, addr.codigoPostal)
        } ?: run {
            // Si no hay dirección, intentamos cargar localmente lo que haya (o esperar al rescate manual)
            providerViewModel.refreshData(decodedName, null)
        }
    }
    
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            // [MANDATO ELITE]: Pull-to-refresh ahora usa la sincronización forzada con throttling
            val decodedName = Uri.decode(categoryName)
            providerViewModel.forceRefreshData(decodedName, activeAddress?.codigoPostal)
            pagedItems.refresh()

            delay(1000)
            isRefreshing = false
        }
    }

    // --- ESCUCHA DE ACCIONES DEL CEREBRO ---
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when {
                actionId == "toggle_subscribed" -> providerViewModel.toggleSubscribedFilter()
                actionId == "toggle_proximity" -> providerViewModel.toggleProximitySort()
                actionId.startsWith("sort_") -> providerViewModel.setSortOrder(actionId)
                actionId == "clear_refinements" -> {
                    providerViewModel.clearRefinements()
                    providerViewModel.setSortOrder(null)
                }
                actionId == "refresh_gps" -> {
                    if (ubicacionViewModel.isGpsEnabled.value) {
                        ubicacionViewModel.ejecutarCalculoUbicacionGps(context) { pais, prov, loc, calle, num, cp, lat, lng ->
                            val freshGpsAddress = AddressUnico(
                                id = "gps_current",
                                ownerName = "Mi Ubicación",
                                label = "GPS Tracker",
                                calle = calle,
                                numero = num,
                                localidad = loc,
                                provincia = prov,
                                pais = pais,
                                codigoPostal = cp,
                                isCompany = false,
                                latitude = lat,
                                longitude = lng
                            )
                            beViewModel.updateAddressFromGps(freshGpsAddress)
                        }
                    } else {
                        Toast.makeText(context, "⚠️ El GPS está desactivado", Toast.LENGTH_SHORT).show()
                    }
                }
                actionId.startsWith("refinement_") -> {
                    providerViewModel.toggleRefinement(actionId.removePrefix("refinement_"))
                }
                actionId.startsWith("manual_zip_") -> {
                    val manualZip = actionId.removePrefix("manual_zip_")
                    val manualAddress = AddressUnico(
                        id = "manual_entry",
                        ownerName = "Ubicación Manual",
                        label = "Rescate Táctico",
                        calle = "Zona CP",
                        numero = manualZip,
                        localidad = "Cargada por usuario",
                        codigoPostal = manualZip,
                        isCompany = false,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                    beViewModel.updateAddressFromGps(manualAddress)
                }
            }
        }
    }

    ResultBusquedaCategoriaContent(
        pagedItems = pagedItems,
        allCategories = allCategories,
        categoryName = Uri.decode(categoryName),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        showSubscribedOnly = showSubscribedOnly,
        activeRefinements = activeRefinements,
        sortByProximity = sortByProximity,
        activeSortId = activeSortCriteria,
        shortcuts = emptyList(), 
        favoriteIds = favoriteIds,
        userLocation = userLocation,
        user = userState,
        activeProfileName = activeProfileName,
        activeProfilePhoto = activeProfilePhoto,
        activeAddress = activeAddress,
        selectedProfileId = selectedProfileId,
        onBack = onBack,
        onNavigateToProviderProfile = onNavigateToProviderProfile,
        onNavigateToChat = onNavigateToChat,
        onTriggerAction = { beViewModel.triggerAction(it) },
        onGpsToggle = { ubicacionViewModel.toggleGps(context) },
        onProfileSelected = { beViewModel.selectProfile(it) },
        isGpsSystemEnabled = isGpsEnabled,
        onManageShortcuts = { id, add -> 
            val provider = pagedItems.itemSnapshotList.items.filterIsInstance<ProviderUiItem.Provider>().find { it.service.id == id }?.service
            providerViewModel.manageShortcut(id, "provider", add, provider?.title, provider?.photoUrl?.toString()) 
        },
        isFilterSheetOpen = isFilterSheetOpen,
        onFilterSheetVisibilityChange = { isFilterSheetOpen = it }
    )
}

// ==================================================================================
// --- 🛰️ SECCIÓN 2: MEDIADOR DE UI (STATELESS CONTENT) ---
// ==================================================================================
/**
 * ResultBusquedaCategoriaContent: La representación visual "Elite".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBusquedaCategoriaContent(
    pagedItems: androidx.paging.compose.LazyPagingItems<ProviderUiItem>, 
    allCategories: List<CategoryEntity>,
    categoryName: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    showSubscribedOnly: Boolean,
    activeRefinements: Set<String>,
    sortByProximity: Boolean,
    activeSortId: List<String>, 
    shortcuts: List<FilterSortItem> = emptyList(),
    favoriteIds: Set<String> = emptySet(),
    userLocation: UserLocation?,
    user: UserEntity?,
    activeProfileName: String,
    activeProfilePhoto: Any?,
    activeAddress: AddressUnico?,
    selectedProfileId: String? = null,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String, String?, String?) -> Unit,
    onNavigateToChat: (ProviderDisplayModel, String?, String?) -> Unit, // 🔥 [ELITE] Ahora acepta clientCompanyId (deprecated) y branchId
    onTriggerAction: (String) -> Unit,
    onGpsToggle: () -> Unit,
    onProfileSelected: (String?) -> Unit = {},
    isGpsSystemEnabled: Boolean = true,
    onManageShortcuts: (String, Boolean) -> Unit = { _, _ -> },
    isFilterSheetOpen: Boolean = false,
    onFilterSheetVisibilityChange: (Boolean) -> Unit = {}
) {
    val listState = rememberLazyGridState()

    // --- LÓGICA DE SCROLL Y COLAPSO DE TARJETAS (Elite V5 - Double Phase) ---
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    val autoCollapseFraction by animateFloatAsState(
        targetValue = if (isFilterSheetOpen) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "autoCollapse"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isFilterSheetOpen) return Offset.Zero 
                val delta = available.y
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 400f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return if (scrollAccumulator >= 400f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val filtersHideFraction by remember {
        derivedStateOf { maxOf(scrollAccumulator / 80f, autoCollapseFraction).coerceIn(0f, 1f) }
    }
    val contextHideFraction by remember {
        derivedStateOf { maxOf((scrollAccumulator - 80f) / 80f, autoCollapseFraction).coerceIn(0f, 1f) }
    }
    val collapseFraction by remember {
        derivedStateOf { maxOf((scrollAccumulator - 160f) / 240f, autoCollapseFraction).coerceIn(0f, 1f) }
    }
    
    var showProfileDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showManualZipPopUp by remember { mutableStateOf(false) }

    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }
    val categoryColor = remember(selectedCategory) {
        if (selectedCategory != null) Color(CategoryVisuals.getColorFor(selectedCategory.superCategory)) 
        else MaverickColors.NeonCyan
    }

    val filterDropdownItems: List<DropdownItemData> = remember {
        listOfNotNull(
            BeDictionary.Filters["filter_chat_sub"]?.copy(label = "Suscriptos VIP"),
            BeDictionary.Filters["filter_chat_verified"]?.copy(label = "Verificados"),
            BeDictionary.Filters["filter_chat_online"]?.copy(label = "Online Ahora"),
            BeDictionary.Filters["filter_chat_24h"]?.copy(label = "Servicio 24hs"),
            BeDictionary.Filters["filter_chat_local"]?.copy(label = "Local Comercial"),
            BeDictionary.Filters["filter_visits"]?.copy(label = "Visitas Técnicas"),
            BeDictionary.Filters["filter_shipping"]?.copy(label = "Realiza Envíos"),
            BeDictionary.Filters["filter_appointments"]?.copy(label = "Brinda Turnos"),
            BeDictionary.Filters["filter_services"]?.copy(label = "Servicios Técnicos"),
            BeDictionary.Filters["filter_products"]?.copy(label = "Venta Productos"),
            BeDictionary.Filters["filter_chat_fav"]?.copy(label = "Mis Favoritos")
        )
    }

    val sortDropdownItems: List<DropdownItemData> = remember {
        listOfNotNull(
            BeDictionary.Sorts["sort_distance"]?.copy(label = "Por Cercanía"),
            BeDictionary.Sorts["sort_ranking"]?.copy(label = "Mejor Ranking"),
            BeDictionary.Sorts["sort_alpha"]?.copy(label = "Orden Alfabético"),
            BeDictionary.Sorts["sort_date"]?.copy(label = "Más Recientes")
        )
    }

    Scaffold(
        containerColor = MaverickColors.StealthGray,
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            BarraCabezera(
                title = categoryName,
                subtitle = "Servicios en ${userLocation?.locality ?: "tu zona"}",
                emoji = selectedCategory?.icon ?: "🔍",
                collapseFraction = collapseFraction,
                onBack = onBack,
                accentColor = categoryColor,
                onInfoClick = { },
                infoTitle = "RESULTADOS DE BÚSQUEDA",
                infoDescription = "Aquí puedes encontrar a los mejores prestadores de la zona filtrados por cercanía, ranking y servicios específicos."
            )
        }
    ) { paddingValues ->
        val safePadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
        )

        Box(modifier = Modifier.fillMaxSize().padding(safePadding)) {
            val pullToRefreshState = rememberPullToRefreshState()
            
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaverickColors.StealthGray,
                        color = categoryColor
                    )
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                   // Spacer(modifier = Modifier.height(8.dp * (1f - filtersHideFraction)))
                    
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { 
                            alpha = 1f - filtersHideFraction
                            translationY = -20.dp.toPx() * filtersHideFraction 
                        }
                        .height(106.dp * (1f - filtersHideFraction)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoldePremiumFilterCard(
                        label = "Filtrar por",
                        dropdownItems = filterDropdownItems,
                        shortcutItems = shortcuts,
                        activeFilters = buildSet {
                            if (showSubscribedOnly) add("toggle_subscribed")
                            addAll(activeRefinements)
                        },
                        onToggle = { actionId ->
                            if (actionId == "toggle_subscribed") {
                                onTriggerAction(actionId)
                            } else {
                                onTriggerAction("refinement_$actionId")
                            }
                        },
                        onManageShortcuts = onManageShortcuts,
                        modifier = Modifier.weight(1f),
                        isSheetVisible = isFilterSheetOpen,
                        onSheetVisibilityChange = onFilterSheetVisibilityChange
                    )

                    MoldePremiumSortCard(
                        label = "Ordenar por",
                        dropdownItems = sortDropdownItems,
                        shortcutItems = emptyList(),
                        activeSorts = activeSortId,
                        onToggle = { actionId ->
                            onTriggerAction(actionId)
                        },
                        onManageShortcuts = { _, _ -> }
                    )
                }

                    // --- 2. TARJETA DE CONTEXTO (USUARIO + UBICACIÓN REFINADA) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                alpha = 1f - contextHideFraction
                                translationY = -20.dp.toPx() * contextHideFraction
                            }
                            .height(64.dp * (1f - contextHideFraction))
                    ) {
                        MoldePremiumContextCard(
                            user = user,
                            activeProfileName = activeProfileName,
                            activeProfilePhoto = activeProfilePhoto,
                            mainAddress = activeAddress?.streetAndNumber ?: (userLocation?.locality ?: "Buscando..."),
                            localityInfo = activeAddress?.let { "${it.localidad}, CP ${it.codigoPostal}" } ?: "",
                            description = activeAddress?.let { addr ->
                                if (addr.ownerName?.isNotBlank() == true && addr.ownerName != "Mi Ubicación") {
                                    buildString {
                                        append(addr.ownerName)
                                        if (addr.label.isNotBlank()) append(" - ${addr.label}")
                                    }
                                } else if (addr.id == "gps_current") "Mi Ubicación" else null
                            },
                            isGpsActive = activeAddress?.id == "gps_current",
                            onUserClick = { showProfileDialog = true },
                            onLocationClick = { showLocationDialog = true },
                            onGpsToggle = onGpsToggle
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp * (1f - contextHideFraction)))

                    // --- 4. LISTA DE RESULTADOS ELITE (PAGINADA) ---
                    val hasActiveFilters = activeRefinements.isNotEmpty() || !showSubscribedOnly || !sortByProximity
                    
                    ListaGridMoldeV2(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        titulo = "PROFESIONALES / EMPRESAS",
                        subtitulo = "LISTA DE RESULTADOS DE $categoryName",
                        itemCount = pagedItems.itemCount,
                        state = listState,
                        containerColor = MaverickColors.StealthGray,
                        accentColor = categoryColor,
                        columns = GridCells.Fixed(2),
                        customMaxHeaderHeight = 64.dp,
                        acciones = { fraction ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (hasActiveFilters) {
                                    BotonCabeceraAccion(
                                        onClick = {
                                            onTriggerAction("clear_refinements")
                                            if (!showSubscribedOnly) onTriggerAction("toggle_subscribed")
                                            if (!sortByProximity) onTriggerAction("toggle_proximity")
                                        },
                                        icon = Icons.Default.FilterAltOff,
                                        color = MaverickColors.MagentaNeon,
                                        collapseFraction = fraction
                                    )
                                }

                                BotonesCabecera.Filtro(
                                    collapseFraction = fraction,
                                    isActive = activeRefinements.isNotEmpty(),
                                    onClick = { onFilterSheetVisibilityChange(!isFilterSheetOpen) }
                                )
                            }
                        }
                    ) {
                        // [ELITE PERFORMANCE] Solo mostramos Shimmer si Room está realmente vacío
                        if (pagedItems.loadState.refresh is LoadState.Loading && pagedItems.itemCount == 0) {
                            items(6) {
                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    ProviderCardShimmer()
                                }
                            }
                        } else if (pagedItems.itemCount == 0 && pagedItems.loadState.refresh !is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyStateMessage(
                                    locality = userLocation?.locality,
                                    onManualZipClick = { showManualZipPopUp = true }
                                )
                            }
                        }

                        // [ELITE PERFORMANCE] Keys estables vía itemKey para evitar re-composición masiva
                        items(
                            count = pagedItems.itemCount,
                            key = pagedItems.itemKey { item -> 
                                when(item) {
                                    is ProviderUiItem.Header -> "h_${item.id}"
                                    is ProviderUiItem.Provider -> "p_${item.service.id}"
                                }
                            },
                            span = { index ->
                                val item = pagedItems[index]
                                if (item is ProviderUiItem.Header) GridItemSpan(maxLineSpan)
                                else GridItemSpan(1)
                            }
                        ) { index ->
                            pagedItems[index]?.let { item ->
                                when(item) {
                                    is ProviderUiItem.Header -> {
                                        ProximityDivider(
                                            text = item.title, 
                                            emoji = item.emoji, 
                                            color = categoryColor
                                        )
                                    }
                                    is ProviderUiItem.Provider -> {
                                        PrestadorBusinessCard(
                                            provider = item.service,
                                            user = user,
                                            onClick = { onNavigateToProviderProfile(item.service.providerId, item.service.companyId, item.service.branchId) },
                                            onChatClick = { sender -> 
                                                // NAVEGACIÓN INTELIGENTE: Pasar remitente si se seleccionó
                                                val clientCompanyId = if (sender != null && sender.id != "personal") sender.id else null
                                                val clientBranchId = if (sender != null && sender.id != "personal") sender.branchId else null
                                                onNavigateToChat(item.service, clientCompanyId, clientBranchId)
                                            },
                                            isShortcut = favoriteIds.contains(item.service.id),
                                            onManageShortcut = { isAdd -> 
                                                onManageShortcuts(item.service.id, isAdd)
                                            },
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Shimmer de carga al final (Append)
                        if (pagedItems.loadState.append is LoadState.Loading) {
                            items(2) {
                                ProviderCardShimmer()
                            }
                        }
                    }
                }
            }
        }
    }

    // --- POPUPS DE CONTEXTO ---
    if (showProfileDialog && user != null) {
        ProfileDialog(
            show = showProfileDialog,
            user = user.toDomain(),
            isPersonalProfile = selectedProfileId == null,
            selectedProfileId = selectedProfileId,
            onProfileSelected = { onProfileSelected(it) },
            navController = androidx.navigation.compose.rememberNavController(),
            onLogout = { showProfileDialog = false },
            onDismiss = { showProfileDialog = false }
        )
    }

    if (showLocationDialog) {
        LocationDialog(
            show = showLocationDialog,
            availableAddresses = user?.personalAddresses ?: emptyList(),
            activeAddress = activeAddress,
            selectedProfileId = selectedProfileId,
            isGpsSystemEnabled = isGpsSystemEnabled,
            onRefresh = { onRefresh() },
            onGpsToggle = onGpsToggle,
            onLocationSelected = { addr ->
                onTriggerAction("select_address_${addr.id}")
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false }
        )
    }

    // --- POPUP DE RESCATE MANUAL DE CP ---
    if (showManualZipPopUp) {
        ManualZipRescuePopUp(
            onZipSelected = { manualZip ->
                onTriggerAction("manual_zip_$manualZip")
                showManualZipPopUp = false
            },
            onDismiss = { showManualZipPopUp = false }
        )
    }
}

// ==================================================================================
// --- 🛰️ SECCIÓN 3: COMPONENTES AUXILIARES Y PREVIEWS ---
// ==================================================================================

@Composable
fun ManualZipRescuePopUp(
    onZipSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var zipCode by remember { mutableStateOf("") }
    val commonZips = listOf("T4000", "C1000", "X5000", "B7600", "M5500", "S2000")
    
    PopUpEmergenteMolde(
        isVisible = true,
        onDismissRequest = onDismiss,
        title = "UBICACIÓN MANUAL",
        subtitle = "RESCATE TÁCTICO CP",
        emoji = "📍",
        accentColor = MaverickColors.MagentaNeon,
        actions = {
            PrimaryButton(
                text = "BUSCAR",
                onClick = { if (zipCode.length >= 4) onZipSelected(zipCode) },
                modifier = Modifier.width(100.dp),
                enabled = zipCode.length >= 4
            )
        }
    ) {
        Text(
            text = "Lamentamos las molestias. Si no pudimos detectar tu ubicación, por favor ingresa tu código postal manualmente (ej: T4000) para encontrar los mejores servicios en tu zona.",
            color = Color.Gray,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        
        Spacer(Modifier.height(16.dp))
        
        CustomTextField(
            value = zipCode,
            onValueChange = { zipCode = it.uppercase().take(8) },
            placeholder = "Ej: T4000",
            icon = Icons.Default.LocationOn,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(12.dp))
        
        Text(
            text = "SUGERENCIAS COMUNES:",
            color = Color.White.copy(0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(8.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(commonZips) { zip ->
                Surface(
                    onClick = { zipCode = zip },
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f))
                ) {
                    Text(
                        text = zip,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaverickColors.NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProximityDivider(text: String, emoji: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, border = BorderStroke(1.dp, color.copy(0.2f)), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(text = emoji, fontSize = 18.sp) }
        }
        Spacer(Modifier.width(12.dp))
        Text(text = text.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.4f), Color.Transparent))))
    }
}

@Composable
fun EmptyStateMessage(
    locality: String?,
    onManualZipClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🌪️", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No hay prestadores en ${locality ?: "tu zona"}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Lamentamos los inconvenientes. Si tu ubicación es incorrecta o no ha sido detectada, puedes ingresarla manualmente para ver los resultados de tu área.",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "CONFIGURAR UBICACIÓN MANUAL",
            onClick = onManualZipClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    // Nota: El preview necesita un mock de LazyPagingItems o envolverlo
}



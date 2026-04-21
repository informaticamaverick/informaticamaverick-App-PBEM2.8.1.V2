package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.data.model.ProviderType
import com.example.myapplication.presentation.components.Utilidades.*
import com.example.myapplication.presentation.components.MenuFiltros
import com.example.myapplication.presentation.components.BotonFiltroSuscritosPremium
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.components.BotonVista
import com.example.myapplication.presentation.components.PrestadorCardV3
import com.example.myapplication.presentation.components.MoldeBarraMenu
import com.example.myapplication.presentation.components.LocationPopup
import com.example.myapplication.presentation.components.AddressInfo
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import com.example.myapplication.presentation.components.BarraCabezera
import com.example.myapplication.presentation.components.FavoritePinBadge
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MaverickBlue = Color(0xFF2197F5)

// ==================================================================================
// --- SECCIÓN: PANTALLA RESULTADOS DE BÚSQUEDA (SINCRONIZACIÓN OBRERO-CEREBRO) ---
// ==================================================================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultBusquedaCategoriaScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    ubicacionViewModel: UbicacionClimaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // ==================================================================================
    // --- 🛠️ SUBSECCIÓN: SINCRONIZACIÓN OBREROS -> CEREBRO (INTERMEDIARIO) ---
    // ==================================================================================
    val userState by beViewModel.userState.collectAsStateWithLifecycle()
    val availableAddressInfos by ubicacionViewModel.availableAddressInfos.collectAsStateWithLifecycle()
    val activeAddressFromObrero by ubicacionViewModel.activeAddress.collectAsStateWithLifecycle()

    // 🔥 Sincronizamos el Perfil del Cerebro al Obrero de Ubicación para procesar direcciones
    LaunchedEffect(userState) {
        ubicacionViewModel.updateAddressList(userState)
    }

    // 🔥 Sincronizamos las direcciones procesadas del Obrero al Cerebro
    LaunchedEffect(availableAddressInfos) {
        beViewModel.syncAvailableAddresses(availableAddressInfos)
    }

    // ==================================================================================
    // --- 📍 SECCIÓN: SINCRONIZACIÓN DE UBICACIÓN (OBRERO -> CEREBRO) ---
    // ==================================================================================
    // Sincronizamos la ubicación ACTIVA del Obrero al Cerebro para actualizar el estado global.
    // Esto asegura que los proveedores se carguen correctamente para la ubicación seleccionada.
    LaunchedEffect(activeAddressFromObrero) {
        activeAddressFromObrero?.let { addr ->
            // 1. Sincronizamos el objeto LocationOption para clima y visualización
            beViewModel.syncLocation(addr.toLocationOption()) 
            
            // 2. 🔥 Sincronizamos el estado de la dirección activa en el Cerebro para mantener coherencia global
            if (addr.id == "gps_current") {
                beViewModel.updateAddressFromGps(addr)
            } else {
                beViewModel.selectAddress(addr.id)
            }
        }
    }

    // 🔥 Sincronizamos los proveedores del Obrero al Cerebro (Puente)
    val unifiedServices by providerViewModel.unifiedServices.collectAsStateWithLifecycle()
    LaunchedEffect(unifiedServices) {
        beViewModel.syncProviders(unifiedServices)
    }

    // 🔥 Sincronizamos las categorías del Obrero al Cerebro (Puente)
    val allRawCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    LaunchedEffect(allRawCategories) {
        beViewModel.syncAllCategories(allRawCategories)
    }

    // ==================================================================================
    // --- 🧠 SUBSECCIÓN: ESTADOS MAESTROS PARA LA UI (DESDE EL CEREBRO) ---
    // ==================================================================================
    val allCategories by beViewModel.allCategories.collectAsStateWithLifecycle()
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()
    val activeAddress by beViewModel.activeAddress.collectAsStateWithLifecycle()
    val availableAddressInfosBrain by beViewModel.availableAddressInfos.collectAsStateWithLifecycle()

    // ==================================================================================
    // --- 📊 SUBSECCIÓN: TRABAJO SUCIO DE PRESTADORES (DEL OBRERO PROVIDER) ---
    // ==================================================================================
    val uiItems by providerViewModel.uiItems.collectAsStateWithLifecycle()
    val isLoading by providerViewModel.isLoading.collectAsStateWithLifecycle()
    val showSubscribedOnly by providerViewModel.showSubscribedOnly.collectAsStateWithLifecycle()
    val activeRefinements by providerViewModel.activeRefinements.collectAsStateWithLifecycle()
    val sortByProximity by providerViewModel.sortByProximity.collectAsStateWithLifecycle()
    val userLocation by providerViewModel.userLocation.collectAsStateWithLifecycle()

    // EFECTO DE REFRESCADO: Cada vez que el Cerebro cambia la ubicación, informamos al Obrero
    LaunchedEffect(activeAddress) {
        activeAddress?.let { addr ->
            // 1. Actualizamos la posición en el Obrero para cálculos de distancia
            providerViewModel.setUserLocation(
                lat = addr.lat,
                lon = addr.lng,
                locality = addr.locality,
                zipCode = addr.postalCode
            )
            // 2. 🔥 FILTRO ACTIVO: Forzamos la recarga de datos con el nuevo código postal
            providerViewModel.refreshData(categoryName, addr.postalCode)
        }
    }

    LaunchedEffect(categoryName) { providerViewModel.onCategorySelected(categoryName) }
    
    // Sincronización de la búsqueda de Be hacia el Obrero de Proveedores
    LaunchedEffect(searchQuery) { providerViewModel.onSearchQueryChanged(searchQuery) }

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            // ==================================================================================
            // --- 🔄 SECCIÓN: SINCRONIZACIÓN DE DATOS (FIREBASE -> ROOM -> UI) ---
            // ==================================================================================
            
            // 1. Sincronizamos categorías globales
            categoryViewModel.syncCategoriesWithFirebase()
            
            // 2. Sincronizamos prestadores para la categoría y ubicación actual
            activeAddress?.let { addr ->
                providerViewModel.refreshData(categoryName, addr.postalCode)
            }
            
            // 3. Esperamos a que el Obrero (ViewModel) termine la carga pesada
            // Damos un pequeño margen para que el estado de carga se propague
            delay(300) 
            while(providerViewModel.isLoading.value) {
                delay(200)
            }
            
            // 4. Finalizamos animación
            delay(400) // Delay estético para suavidad "Instagram-style"
            isRefreshing = false
        }
    }

    // 🔥 NUEVO: ESCUCHA DE ACCIONES DEL CEREBRO PARA EL OBRERO PROVIDER (ORQUESTACIÓN) 🔥
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "toggle_subscribed" -> providerViewModel.toggleSubscribedFilter()
                "toggle_proximity" -> providerViewModel.toggleProximitySort()
                "clear_refinements" -> providerViewModel.clearRefinements()
                else -> if (actionId.startsWith("refinement_")) {
                    providerViewModel.toggleRefinement(actionId.removePrefix("refinement_"))
                }
            }
        }
    }

    ResultBusquedaCategoriaContent(
        uiItems = uiItems,
        allCategories = allCategories,
        categoryName = categoryName,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        showSubscribedOnly = showSubscribedOnly,
        activeRefinements = activeRefinements,
        sortByProximity = sortByProximity,
        userLocation = userLocation,
        onToggleSubscribed = { beViewModel.triggerAction("toggle_subscribed") },
        onToggleProximity = { beViewModel.triggerAction("toggle_proximity") },
        onToggleRefinement = { id -> beViewModel.triggerAction("refinement_$id") },
        onClearRefinements = { beViewModel.triggerAction("clear_refinements") },
        onBack = onBack,
        onNavigateToProviderProfile = onNavigateToProviderProfile,
        onNavigateToChat = onNavigateToChat,
        // --- PROPS DE UBICACIÓN ---
        activeAddress = activeAddress,
        availableAddresses = availableAddressInfosBrain,
        user = userState,
        onAddressSelected = { addr ->
            // Actualizamos en el Obrero que a su vez sincroniza con el Cerebro
            ubicacionViewModel.selectAddress(addr.id)
        },
        // ✅ CORRECCIÓN: El botón de GPS ahora notifica al Cerebro para actualizar el contexto
        onUpdateGps = { beViewModel.triggerAction("refresh_gps") },
        beViewModel = beViewModel // Pasamos el beViewModel para escuchar acciones
    )

    // ==================================================================================
    // --- 🧠 ESCUCHA DE ACCIONES DEL CEREBRO (ORQUESTACIÓN) ---
    // ==================================================================================
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "refresh_gps" -> {
                    // El Cerebro ordena al Obrero de Ubicación ejecutar el cálculo
                    ubicacionViewModel.ejecutarCalculoUbicacionGps(context)
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN: CONTENIDO DE LA PANTALLA (UI CONSUME DEL CEREBRO/OBREROS) ---
// ==================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBusquedaCategoriaContent(
    uiItems: List<ProviderUiItem>, 
    allCategories: List<CategoryEntity>,
    categoryName: String,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    showSubscribedOnly: Boolean,
    activeRefinements: Set<String>,
    sortByProximity: Boolean,
    userLocation: UserLocation?,
    onToggleSubscribed: () -> Unit,
    onToggleProximity: () -> Unit,
    onToggleRefinement: (String) -> Unit,
    onClearRefinements: () -> Unit,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    // --- NUEVAS PROPS PARA EL FLOATING TOOL ---
    activeAddress: AddressInfo?,
    availableAddresses: List<AddressInfo>,
    user: UserEntity?,
    onAddressSelected: (AddressInfo) -> Unit,
    onUpdateGps: () -> Unit,
    beViewModel: BeBrainViewModel // Necesario para el trigger de la lupa
) {
    // ------------------------------------------------------------------------------

    var isBentoView by remember { mutableStateOf(false) }
    var isLocationExpanded by remember { mutableStateOf(false) }
    
    // ==================================================================================
    // --- 🎨 ESTADO: VISIBILIDAD DE LA TARJETA DE UBICACIÓN ---
    // Controlado externamente por el botón de la lupa en MoldeBarraMenu
    // ==================================================================================
    var isLocationCardVisible by remember { mutableStateOf(true) }

    // Escuchar el evento de la lupa desde el BeBrainViewModel
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            if (actionId == "toggle_location_card") {
                isLocationCardVisible = !isLocationCardVisible
            }
        }
    }
    
    // ==================================================================================
    // --- 🚫 SECCIÓN: CONTROL DE APERTURA DEL POPUP (SOLICITUD USUARIO) ---
    // ==================================================================================
    // Se ha comentado la auto-expansión para que el popup de ubicación NO se abra solo al entrar.
    // Ahora solo se mostrará cuando el usuario toque la tarjeta de ubicación.
    /*
    LaunchedEffect(Unit) {
        if (activeAddress == null) {
            delay(500)
            isLocationExpanded = true
        }
    }
    */

    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }
    val categoryColor = remember(selectedCategory) {
        if (selectedCategory != null) CategoryVisuals.getColorFor(selectedCategory.superCategory).let { Color(it) } else MaverickBlue
    }

    val refinementOptions = remember {
        listOf(
            ControlItem("Trabaja 24hs", Icons.Default.AccessTimeFilled, "🕒", Color(0xFFFF9800), "24h"),
            ControlItem("Local Comercial", Icons.Default.Storefront, "🏪", Color(0xFF4CAF50), "loc"),
            ControlItem("Visitas Técnicas", Icons.Default.LocalShipping, "🚚", Color(0xFF2197F5), "visit"),
            ControlItem("Realiza Envíos", Icons.Default.LocalShipping, "📦", Color(0xFF00BCD4), "env"),
            ControlItem("Brinda Turnos", Icons.Default.EventAvailable, "📅", Color(0xFF9C27B0), "date"),
            ControlItem("Servicios Técnicos", Icons.Default.Build, "🛠️", Color(0xFFFFC107), "serv"),
            ControlItem("Venta Productos", Icons.Default.ShoppingBag, "🛍️", Color(0xFFE91E63), "prod")
        )
    }

    val gridState = rememberLazyGridState()

    // Cálculos para el colapso de la cabecera basados en el scroll
    val firstVisibleItemScrollOffset = gridState.firstVisibleItemScrollOffset
    val firstVisibleItemIndex = gridState.firstVisibleItemIndex

    // Fracción de colapso: 0.0 (estático/arriba) a 1.0 (scrolled)
    val collapseFraction = remember(firstVisibleItemIndex, firstVisibleItemScrollOffset) {
        if (firstVisibleItemIndex > 0) 1f
        else (firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
    }

    Scaffold(
        containerColor = MaverickColors.StealthGray,
        topBar = {
            // ==================================================================================
            // --- SECCIÓN: NUEVA CABECERA MAVERICK V4 (DINÁMICA & PROTEGIDA) ---
            // ==================================================================================
            BarraCabezera(
                title = categoryName,
                subtitle = "Servicios en ${userLocation?.locality ?: "tu zona"}",
                emoji = selectedCategory?.icon ?: "🔍",
                collapseFraction = collapseFraction, // <--- PASAMOS LA FRACCIÓN DE SCROLL
                onBack = onBack,
                onInfoClick = { /* Diálogo explicativo */ },
                accentColor = categoryColor
            )
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val safePadding = remember(paddingValues, layoutDirection) {
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection).coerceAtLeast(0.dp),
                top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                end = paddingValues.calculateEndPadding(layoutDirection).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )
        }
        Box(modifier = Modifier.fillMaxSize().padding(safePadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- ESPACIADOR DE AIRE ---
                Spacer(modifier = Modifier.height(10.dp))

                // ==================================================================================
                // --- SECCIÓN: BARRA DE HERRAMIENTAS Y CONTENIDO (LISTA DE PRESTADORES) ---
                // ==================================================================================
                MoldeBarraMenu(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    itemCount = uiItems.count { it is ProviderUiItem.Provider },
                    showSubscribedOnly = showSubscribedOnly,
                    onToggleSubscribed = onToggleSubscribed,
                    sortByProximity = sortByProximity,
                    onToggleProximity = onToggleProximity,
                    isBentoView = isBentoView,
                    onToggleView = { isBentoView = !isBentoView },
                    activeRefinements = activeRefinements,
                    refinementOptions = refinementOptions,
                    onToggleRefinement = onToggleRefinement,
                    onClearRefinements = onClearRefinements
                ) {
                    // [MODIFICACIÓN]: Gestión de carga optimizada para Pull-to-Refresh
                    // Si es carga inicial (sin datos y cargando), mostramos el spinner central.
                    // Si ya hay datos o estamos en un 'Refresco', mantenemos el PullToRefreshBox activo.
                    if (isLoading && uiItems.isEmpty() && !isRefreshing) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaverickBlue)
                        }
                    } else {
                        // El contenido (las tarjetas) ahora se renderiza DENTRO del MoldeBarraMenu
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = onRefresh,
                            state = rememberPullToRefreshState(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ProviderListContent(
                                uiItems = uiItems,
                                isBentoView = isBentoView,
                                userLoc = userLocation,
                                categoryColor = categoryColor,
                                onNavigateToProviderProfile = onNavigateToProviderProfile,
                                onNavigateToChat = onNavigateToChat,
                                gridState = gridState
                            )
                        }
                    }
                }
            }

            // ==================================================================================
            // --- 📍 NUEVO: FLOATING LOCATION TOOL (DECOUPLED) ---
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

            // Componente Selector Flotante (Posicionado abajo a la izquierda, junto al asistente)
            // ==================================================================================
            // --- 📏 AJUSTE: ANCHO Y ALTO PERSONALIZADO PARA RESULTADOS DE BÚSQUEDA ---
            // Se ajusta el ancho a 220dp y el alto a 72dp para coincidir con el asistente.
            // ==================================================================================
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 1.dp, bottom = 12.dp)
                    .width(220.dp) // Ancho ajustado según solicitud
                    .height(85.dp) // Alto ajustado para igualar barra de herramientas del asistente
            ) {
                ResultBusquedaLocationTool(
                    activeAddress = activeAddress,
                    availableAddresses = availableAddresses,
                    user = user,
                    onAddressSelected = onAddressSelected,
                    onUpdateGps = onUpdateGps,
                    isExpanded = isLocationExpanded,
                    onToggleExpand = { isLocationExpanded = it },
                    isVisible = isLocationCardVisible // Pasamos visibilidad
                )
            }


        }
    }
}
//=============================================================================
// --- 📍 SECCIÓN: COMPONENTES DE UBICACIÓN FLOTANTE (ADAPTACIÓN V5) ---
// ==================================================================================

/** Extension para compatibilidad con el Popup original */
private fun AddressInfo.toLocationOption(): LocationOption {
    return if (this.id == "gps_current") {
        LocationOption.Gps(
            address = this.streetAndNumber,
            locality = this.locality,
            lat = this.lat,
            lng = this.lng
        )
    } else if (this.isCompany) {
        LocationOption.Business(
            companyName = this.companyOrUserName,
            branchName = this.branchName,
            address = this.streetAndNumber,
            number = "",
            locality = this.locality
        )
    } else {
        LocationOption.Personal(
            address = this.streetAndNumber,
            number = "",
            locality = this.locality
        )
    }
}

@Composable
fun ResultBusquedaLocationTool(
    activeAddress: AddressInfo?,
    availableAddresses: List<AddressInfo>,
    user: UserEntity?,
    onAddressSelected: (AddressInfo) -> Unit,
    onUpdateGps: () -> Unit,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit,
    isVisible: Boolean // Propiedad para controlar visibilidad externa
) {
    val brush = Brush.verticalGradient(
        listOf(Color(0xFF1A1A24).copy(alpha = 0.9f), Color(0xFF0D0D12).copy(alpha = 1f))
    )

    val currentLocation = activeAddress?.toLocationOption() 
        ?: LocationOption.Gps(address = "Buscando...", locality = "Ubicación")

    // Lógica de líneas (reutilizada de LocationSelector para consistencia visual)
    val (linea1, linea2, linea3) = when (currentLocation) {
        is LocationOption.Gps -> Triple(
            "UBICACIÓN ACTUAL", 
            currentLocation.address.ifBlank { "Buscando..." }, 
            "${currentLocation.locality}, ${currentLocation.province}".trim().removeSuffix(",")
        )
        is LocationOption.Personal -> Triple("MI CASA / PERSONAL", "${currentLocation.address} ${currentLocation.number}", currentLocation.locality)
        is LocationOption.Business -> Triple(currentLocation.companyName.uppercase(), currentLocation.branchName, "${currentLocation.address} ${currentLocation.number}")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ==================================================================================
        // --- 🃏 TARJETA DE INFORMACIÓN DE UBICACIÓN ---
        // Se oculta/muestra mediante animación basada en 'isVisible' (controlada por la lupa).
        // ==================================================================================
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
                    .clickable { onToggleExpand(true) }
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(start = 12.dp, top = 4.dp, bottom = 6.dp, end = 40.dp), // Espacio para el botón de GPS
                contentAlignment = Alignment.CenterStart
            ) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                    Text(text = linea1, fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF22D3EE), letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = linea2, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = linea3, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        // ==================================================================================
        // --- 🔘 BOTÓN DE ACTUALIZACIÓN GPS (RESTAURADO) ---
        // Este botón mantiene su función original de actualizar el GPS.
        // Solo es visible si la tarjeta está visible.
        // ==================================================================================
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = (4).dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D1117).copy(alpha = 0.8f))
                    .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f), CircleShape)
                    .clickable { onUpdateGps() }, // Restaurada función original de GPS
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation, 
                    contentDescription = "Refresh GPS", 
                    tint = Color(0xFF22D3EE), 
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (isExpanded) {
        Dialog(
            onDismissRequest = { onToggleExpand(false) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch {
                    delay(300)
                    onToggleExpand(false)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.6f))
                    .clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        LocationPopup(
                            user = user,
                            onClose = { closeWithAnimation() },
                            onRefresh = { onUpdateGps(); closeWithAnimation() },
                            onLocationSelected = { loc ->
                                // Buscamos la dirección que coincida con la opción seleccionada
                                val matched = availableAddresses.find { it.toLocationOption() == loc }
                                matched?.let { onAddressSelected(it) }
                                closeWithAnimation()
                            },
                            currentLocation = currentLocation
                        )
                    }
                }
            }
        }
    }
}

/** HELPER: Envuelve los botones originales para añadir la etiqueta inferior */
@Composable
fun ActionColumnWithLabel(
    label: String, 
    active: Boolean, 
    spacing: Dp = 4.dp,
    fontSize: TextUnit = 7.sp,
    content: @Composable () -> Unit
) {
    val color = if (active) Color.White else Color.Gray
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        content()
        Spacer(Modifier.height(spacing))
        Text(text = label, fontSize = fontSize, fontWeight = FontWeight.Bold, color = color)
    }
}

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE SEPARACIÓN GEOGRÁFICA ---
// ==================================================================================

@Composable
fun ProximityDivider(text: String, emoji: String, color: Color, isExpanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrow")
    Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 12.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, border = BorderStroke(1.dp, color.copy(0.2f)), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(text = emoji, fontSize = 18.sp) }
        }
        Spacer(Modifier.width(12.dp))
        Text(text = text.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.4f), Color.Transparent))))
        Spacer(Modifier.width(12.dp))
        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(20.dp).rotate(rotation))
    }
}

// ==================================================================================
// --- SECCIÓN: GRILLA DE PRESTADORES ---
// ==================================================================================
@Composable
fun ProviderListContent(
    uiItems: List<ProviderUiItem>, 
    isBentoView: Boolean, 
    userLoc: UserLocation?, 
    categoryColor: Color, 
    onNavigateToProviderProfile: (String) -> Unit, 
    onNavigateToChat: (String) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState // <--- AÑADIDO ESTADO
) {
    if (uiItems.isEmpty()) { EmptyStateMessage(userLoc?.locality) } else {
        val expandedCards = remember(isBentoView) { mutableStateMapOf<String, Boolean>() }
        val collapsedSections = remember { mutableStateMapOf<String, Boolean>() }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), 
            state = gridState, // <--- CONECTAMOS EL ESTADO
            contentPadding = PaddingValues(top = 6.dp, start = 2.dp, end = 2.dp, bottom = 80.dp), 
            horizontalArrangement = Arrangement.spacedBy(8.dp), 
            verticalArrangement = Arrangement.spacedBy(8.dp), 
            modifier = Modifier.fillMaxSize()
        ) {
            uiItems.forEach { item ->
                when (item) {
                    is ProviderUiItem.Header -> { item(span = { GridItemSpan(3) }, key = item.id) {
                        val isExpanded = !(collapsedSections[item.id] ?: false)
                        ProximityDivider(item.title, item.emoji, categoryColor, isExpanded) { collapsedSections[item.id] = isExpanded }
                    } }
                    is ProviderUiItem.Provider -> {
                        val sectionId = findSectionIdForItem(item, uiItems)
                        if (!(collapsedSections[sectionId] ?: false)) {
                            item(key = item.service.id) {
                                val isExp = expandedCards[item.service.id] ?: false
                                val compact = isBentoView && !isExp
                                PrestadorCardV3(provider = item.service, isCompact = compact, onClick = { if (compact) expandedCards[item.service.id] = true else onNavigateToProviderProfile(item.service.id) }, onChatClick = { onNavigateToChat(item.service.id) }, modifier = Modifier.padding(bottom = 1.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun findSectionIdForItem(item: ProviderUiItem.Provider, allItems: List<ProviderUiItem>): String {
    val index = allItems.indexOf(item); if (index == -1) return "none"
    for (i in index downTo 0) { if (allItems[i] is ProviderUiItem.Header) return (allItems[i] as ProviderUiItem.Header).id }
    return "none"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultHeaderSection(cat: CategoryEntity?, name: String, onBack: () -> Unit) {
    val headerIconBoxSize = 40.dp
    val headerEmojiSize = 22.sp
    val headerIconTextGap = 16.dp
    val headerTitleFontSize = 16.sp
    val headerSubtitleFontSize = 11.sp
    val headerBottomLineHeight = 1.dp

    val color = if (cat != null) CategoryVisuals.getColorFor(cat.superCategory).let { Color(it) } else MaverickBlue
    Surface(color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Column {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                        cat?.let { 
                            Box(contentAlignment = Alignment.TopEnd) {
                                Surface(
                                    modifier = Modifier.size(headerIconBoxSize), 
                                    shape = RoundedCornerShape(12.dp), 
                                    color = color.copy(0.15f), 
                                    border = BorderStroke(1.dp, color.copy(0.4f))
                                ) { 
                                    Box(contentAlignment = Alignment.Center) { 
                                        Text(it.icon, fontSize = headerEmojiSize) 
                                    } 
                                }
                                
                                // PIN DE FAVORITO EN EL HEADER
                                if (it.isFavorite) {
                                    FavoritePinBadge(
                                        isFavorite = true,
                                        modifier = Modifier.offset(x = 10.dp, y = (-8).dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(headerIconTextGap)) 
                        }
                        Column { 
                            Text(name.uppercase(), fontWeight = FontWeight.Black, color = Color.White, fontSize = headerTitleFontSize, letterSpacing = 1.2.sp)
                            cat?.let { 
                                Text("Servicios en tu zona", fontSize = headerSubtitleFontSize, color = color.copy(0.9f), fontWeight = FontWeight.Bold) 
                            } 
                        } 
                    } 
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }, 
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            Box(Modifier.fillMaxWidth().height(headerBottomLineHeight).background(Brush.horizontalGradient(listOf(Color.Transparent, color.copy(0.6f), Color.Transparent))))
        }
    }
}

@Composable
fun EmptyStateMessage(locality: String?) {
    // [MODIFICACIÓN]: Agregamos verticalScroll para permitir que el gesto de Pull-to-Refresh
    // sea detectado incluso cuando no hay elementos en la lista.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp), 
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌪️", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No hay prestadores en ${locality ?: "tu zona"}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Intenta buscando en otra categoría o ajustando los filtros.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResultBusquedaCategoriaScreenPreview() {
    MyApplicationTheme {
        val sampleCategories = listOf(
            CategoryEntity(
                name = "Informatica",
                icon = "💻",
                superCategory = "Tecnología y Sistemas",
                superCategoryIcon = "💻",
                providerIds = emptyList(),
                isNew = false,
                isNewPrestador = false,
                isAd = false
            )
        )

        val sampleProviders = listOf(
            ProviderUiItem.Header("Destacados", "✨", "header_1"),
            ProviderUiItem.Provider(
                ServiceDisplayModel(
                    id = "1",
                    title = "Maverick Tech",
                    subtitle = "Ingeniero de Software",
                    photoUrl = "https://picsum.photos/200",
                    rating = 5.0,
                    isVerified = true,
                    isOnline = true,
                    type = ProviderType.INDIVIDUAL,
                    works24h = true,
                    isSubscribed = true,
                    displayAddress = "San Miguel de Tucumán",
                    typeEmoji = "👨‍🔧",
                    typeLabel = "Profesional"
                )
            ),
            ProviderUiItem.Header("Cerca de ti", "📍", "header_2"),
            ProviderUiItem.Provider(
                ServiceDisplayModel(
                    id = "2",
                    title = "Soporte Express",
                    subtitle = "Técnico en PC",
                    photoUrl = "https://picsum.photos/201",
                    rating = 4.5,
                    isVerified = false,
                    isOnline = false,
                    type = ProviderType.INDIVIDUAL,
                    works24h = false,
                    isSubscribed = false,
                    displayAddress = "Barrio Norte",
                    typeEmoji = "🛠️",
                    typeLabel = "Técnico"
                )
            )
        )

        ResultBusquedaCategoriaContent(
            uiItems = sampleProviders,
            allCategories = sampleCategories,
            categoryName = "Informatica",
            isLoading = false,
            isRefreshing = false,
            onRefresh = {},
            showSubscribedOnly = false,
            activeRefinements = emptySet(),
            sortByProximity = false,
            userLocation = UserLocation(-26.82414, -65.22260, "San Miguel de Tucumán", "4000"),
            onToggleSubscribed = {},
            onToggleProximity = {},
            onToggleRefinement = {},
            onClearRefinements = {},
            onBack = {},
            onNavigateToProviderProfile = {},
            onNavigateToChat = {},
            // --- AÑADIDO PARA QUE EL PREVIEW COMPILE ---
            activeAddress = null,
            availableAddresses = emptyList(),
            user = null,
            onAddressSelected = {},
            onUpdateGps = {},
            beViewModel = hiltViewModel() // O un mock
        )
    }
}

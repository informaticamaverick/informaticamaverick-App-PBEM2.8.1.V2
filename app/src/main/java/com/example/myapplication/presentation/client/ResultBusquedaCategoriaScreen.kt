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
import com.example.myapplication.presentation.components.BotonFiltroSuscritosPremium
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.components.BotonVista
import com.example.myapplication.presentation.components.PrestadorCardV3
import android.widget.Toast
import com.example.myapplication.presentation.components.MoldeBarraMenu
import com.example.myapplication.presentation.components.LocationPopup
import com.example.myapplication.presentation.components.LocationSelector
import com.example.myapplication.presentation.components.AddressInfo
import com.example.myapplication.presentation.components.toLocationOption
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import com.example.myapplication.presentation.components.BarraCabezera
import com.example.myapplication.presentation.components.FavoritePinBadge
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
    onNavigateToChat: (ServiceDisplayModel) -> Unit,
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

    // ==================================================================================
    // --- 🧠 SUBSECCIÓN: ESTADOS MAESTROS PARA LA UI (DESDE EL CEREBRO) ---
    // ==================================================================================
    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
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
            // Ya no es necesario, el Obrero observa al Coordinador directamente.

            // 2. 🔥 FILTRO ACTIVO: Forzamos la recarga de datos con el nuevo código postal
            providerViewModel.refreshData(categoryName, addr.postalCode)
        }
    }

    LaunchedEffect(categoryName) { providerViewModel.onCategorySelected(categoryName) }
    
    // Sincronización de la búsqueda de Be hacia el Obrero de Proveedores
    // Ya no es necesario, el Obrero observa al Coordinador directamente.

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
            delay(300) 
            while(providerViewModel.isLoading.value) {
                delay(200)
            }
            
            // 4. Finalizamos animación
            delay(400)
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
            // Actualizamos en el Cerebro, que es la fuente de verdad única
            beViewModel.selectAddress(addr.id)
        },
        // ✅ CORRECCIÓN: El botón de GPS ahora notifica al Cerebro para actualizar el contexto
        onUpdateGps = { beViewModel.triggerAction("refresh_gps") },
        beBrainActionEvent = beViewModel.actionEvent // Pasamos el flujo de acciones en lugar del ViewModel completo
    )

    // ==================================================================================
    // --- 🧠 ESCUCHA DE ACCIONES DEL CEREBRO (ORQUESTACIÓN DE GPS) ---
    // ==================================================================================
    LaunchedEffect(Unit) {
        beViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "refresh_gps" -> {
                    // --- 🛰️ SECCIÓN: VALIDACIÓN Y EJECUCIÓN GPS ---
                    if (ubicacionViewModel.isGpsHabilitado(context)) {
                        // El Cerebro ordena al Obrero de Ubicación ejecutar el cálculo
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
                            // Notificamos al Cerebro para actualizar la fuente de verdad global
                            beViewModel.updateAddressFromGps(freshGpsAddress)
                        }
                    } else {
                        // Notificación de advertencia si el sensor está apagado
                        Toast.makeText(context, "⚠️ El GPS está desactivado. Actívalo para actualizar tu ubicación.", Toast.LENGTH_SHORT).show()
                    }
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
    onNavigateToChat: (ServiceDisplayModel) -> Unit,
    // --- NUEVAS PROPS PARA EL FLOATING TOOL ---
    activeAddress: AddressInfo?,
    availableAddresses: List<AddressInfo>,
    user: UserEntity?,
    onAddressSelected: (AddressInfo) -> Unit,
    onUpdateGps: () -> Unit,
    beBrainActionEvent: Flow<String> // Flujo de acciones decoupled para permitir Previews
) {
    // ------------------------------------------------------------------------------

    var isBentoView by remember { mutableStateOf(false) }
    var isLocationExpanded by remember { mutableStateOf(false) }
    
    // ==================================================================================
    // --- 🎨 ESTADO: VISIBILIDAD DE LA TARJETA DE UBICACIÓN ---
    // Controlado externamente por el botón de la lupa en MoldeBarraMenu
    // ==================================================================================
    var isLocationCardVisible by remember { mutableStateOf(true) }

    // Escuchar el evento de la lupa desde el flujo de acciones
    LaunchedEffect(Unit) {
        beBrainActionEvent.collect { actionId ->
            if (actionId == "toggle_location_card") {
                isLocationCardVisible = !isLocationCardVisible
            }
        }
    }


    val selectedCategory = remember(allCategories, categoryName) {
        allCategories.find { it.name.equals(categoryName, ignoreCase = true) }
    }
    val categoryColor = remember(selectedCategory) {
        if (selectedCategory != null) Color(CategoryVisuals.getColorFor(selectedCategory.superCategory)) else MaverickBlue
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
            // Se ajusta el ancho a 280dp y el alto a 80dp para coincidir con el asistente.
            // ==================================================================================
            AnimatedVisibility(
                visible = isLocationCardVisible,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 1.dp)
                    .width(280.dp) // Ancho ajustado según solicitud
                    .height(80.dp) // Alto ajustado para igualar barra de herramientas del asistente
            ) {
                // USAMOS EL MOLDE INDEPENDIENTE DE TarjetasHomeScreenCabecera.kt
                LocationSelector(
                    user = user,
                    currentLocation = activeAddress?.toLocationOption() 
                        ?: LocationOption.Gps(address = "Buscando...", locality = "Ubicación"),
                    onRefresh = onUpdateGps,
                    onLocationSelected = { option ->
                        // Sincronizamos con el Cerebro usando el ID real
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


        }
    }
}
// ==================================================================================
// --- SECCIÓN: COMPONENTES DE SEPARACIÓN GEOGRÁFICA ---
// ==================================================================================

@Composable
fun ProximityDivider(text: String, emoji: String, color: Color, isExpanded: Boolean, onToggle: () -> Unit) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "arrow")
    Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 12.dp, horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
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
    onNavigateToChat: (ServiceDisplayModel) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState // <--- AÑADIDO ESTADO
) {
    if (uiItems.isEmpty()) { EmptyStateMessage(userLoc?.locality) } else {
        val expandedCards = remember(isBentoView) { mutableStateMapOf<String, Boolean>() }
        val collapsedSections = remember { mutableStateMapOf<String, Boolean>() }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), 
            state = gridState, // <--- CONECTAMOS EL ESTADO
            contentPadding = PaddingValues(top = 6.dp, start = 1.dp, end = 2.dp, bottom = 50.dp),
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
                                PrestadorCardV3(
                                    provider = item.service, 
                                    isCompact = compact, 
                                    onClick = { 
                                        if (compact) expandedCards[item.service.id] = true 
                                        else onNavigateToProviderProfile(item.service.id) 
                                    }, 
                                    onChatClick = { onNavigateToChat(item.service) }, 
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
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
            beBrainActionEvent = emptyFlow() // Preview amigable
        )
    }
}

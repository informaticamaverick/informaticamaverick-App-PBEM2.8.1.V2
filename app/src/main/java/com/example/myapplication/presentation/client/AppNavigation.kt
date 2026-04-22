package com.example.myapplication.presentation.client

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoDarkGlassBackground
import com.example.myapplication.presentation.components.Utilidades.geminiGradientBrush
import com.example.myapplication.presentation.components.Utilidades.shakeClick
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ==================================================================================
// --- SECCIÓN: RUTAS DE NAVEGACIÓN ---
// ==================================================================================
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Inicio")
    object Presupuestos : Screen("presupuestos", "Presupuestos")
    object Chat : Screen("chat?providerId={providerId}", "Chat")
    object Calendar : Screen("calendar", "Calendario")
    object Promo : Screen("promo", "Promociones")
    object CrearLicitacion : Screen("crear_licitacion", "Crear Licitación")
    object PerfilPrestador : Screen("perfil_prestador/{providerId}", "Perfil del Prestador")
    object PerfilCliente : Screen("perfil_cliente", "Mi Perfil")
    object ResultBusqueda : Screen("result_busqueda/{category}", "Resultados de Búsqueda")
    object Fast : Screen("fast", "Maverick FAST")
    object Login : Screen("login", "Iniciar Sesión")
}

/**
 * --- APP NAVIGATION (COORDINADOR GLOBAL) ---
 * Gestiona el flujo entre el Cerebro (BeBrain) y el Obrero (UbicacionClima).
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    initialTarget: String? = null,
    hudViewModel: BeBrainViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    simulationViewModel: SimulationViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel(),
    beAssistantViewModel: BeAssistantViewModel = hiltViewModel()
) {
    // --- ESTADOS DEL CEREBRO (UI Y HUD) ---
    val showBe by hudViewModel.showBe.collectAsStateWithLifecycle()
    val isSearchActive by hudViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQueries by hudViewModel.searchQuery.collectAsStateWithLifecycle()
    val beMessages by hudViewModel.beMessages.collectAsStateWithLifecycle()
    val currentActions by hudViewModel.currentActions.collectAsStateWithLifecycle()
    val isBottomBarVisible by hudViewModel.isBottomBarVisible.collectAsStateWithLifecycle()
    val beState by hudViewModel.beState.collectAsStateWithLifecycle()
    val currentTipIndex by hudViewModel.currentTipIndex.collectAsStateWithLifecycle()
    val isDormido by hudViewModel.isBeDormido.collectAsStateWithLifecycle()
    val showBeTools by hudViewModel.showBeTools.collectAsStateWithLifecycle()
    val requestKeyboard by hudViewModel.requestKeyboard.collectAsStateWithLifecycle() 
    val resetBeTrigger by hudViewModel.resetBePositionTrigger.collectAsStateWithLifecycle()
    val isMultiSelectionActive by hudViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val toolboxKey by hudViewModel.toolboxKey.collectAsStateWithLifecycle()
    val showLocationTool by hudViewModel.showLocationTool.collectAsStateWithLifecycle()
    val user by hudViewModel.userState.collectAsStateWithLifecycle()

    // --- ESTADOS DE PROVEEDORES Y CATEGORÍAS ---
    val favorites by providerViewModel.favoriteServices.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    // --- ESTADOS DEL COREÓGRAFO (BE ASSISTANT VM) ---
    val beOffsetX by beAssistantViewModel.offsetX.collectAsStateWithLifecycle()
    val beOffsetY by beAssistantViewModel.offsetY.collectAsStateWithLifecycle()
    val isBeDragging by beAssistantViewModel.isDragging.collectAsStateWithLifecycle()
    val dynamicBeBottomPadding by beAssistantViewModel.beBottomPadding.collectAsStateWithLifecycle()

    // 🔥 SINCRONIZACIÓN CEREBRO -> OBRERO 🔥
    // Cuando el usuario cambia en el cerebro, el obrero debe re-mapear las direcciones.
    LaunchedEffect(user) {
        ubicacionObrero.updateAddressList(user)
    }

    // 🔥 SINCRONIZACIÓN CEREBRO -> COREÓGRAFO 🔥
    // Sincronizamos la mirada de Be con su estado emocional (Brain) y si está durmiendo.
    LaunchedEffect(beState, isDormido) {
        beAssistantViewModel.updateMirada(beState, isDormido)
    }

    // Sincronizamos el padding inferior dinámico de Be según la visibilidad de las barras.
    LaunchedEffect(isBottomBarVisible, isSearchActive) {
        beAssistantViewModel.updateLayout(isBottomBarVisible, isSearchActive)
    }

    // Sincronizamos la estabilidad del toolbar cuando cambia la toolbox.
    LaunchedEffect(toolboxKey) {
        beAssistantViewModel.notifyToolboxChanged()
    }

    // 🔥 NUEVO: OBRERO DE INTERACCIÓN REACTIVA (BE) 🔥
    val beInteractionViewModel: BeInteractionViewModel = hiltViewModel()
    val searchReaction by beInteractionViewModel.currentReaction.collectAsStateWithLifecycle()
    val searchMenuOptions by beInteractionViewModel.searchMenuOptions.collectAsStateWithLifecycle()
    val selectedOptionIds by beInteractionViewModel.selectedOptionIds.collectAsStateWithLifecycle()

    // --- SINCRONIZACIÓN DE RECURSOS PARA LA BÚSQUEDA REACTIVA ---
    val availableFilters by hudViewModel.availableFilters.collectAsStateWithLifecycle()
    val availableSorts by hudViewModel.availableSortOptions.collectAsStateWithLifecycle()
    val dynamicCategories by hudViewModel.dynamicCategories.collectAsStateWithLifecycle()

    LaunchedEffect(availableFilters, availableSorts, dynamicCategories) {
        beInteractionViewModel.syncResources(
            filters = availableFilters,
            sorts = availableSorts,
            categories = dynamicCategories
        )
    }

    // 🔥 CONEXIÓN CEREBRO -> LÓBULO FRONTAL 🔥
    LaunchedEffect(hudViewModel) {
        beInteractionViewModel.setBeBrain(hudViewModel)
    }

    AppNavigationContent(
        initialTarget = initialTarget,
        beViewModel = hudViewModel,
        beAssistantViewModel = beAssistantViewModel, // Pasamos el coreógrafo
        beInteractionViewModel = beInteractionViewModel, // Pasamos el nuevo ViewModel
        searchReaction = searchReaction,
        searchMenuOptions = searchMenuOptions,
        selectedOptionIds = selectedOptionIds,
        showBe = showBe,
        isSearchActive = isSearchActive,
        searchQuery = searchQueries, 
        beMessages = beMessages,
        beState = beState,
        currentTipIndex = currentTipIndex,
        isDormido = isDormido,
        showBeTools = showBeTools,
        requestKeyboard = requestKeyboard, 
        currentActions = currentActions,
        isBottomBarVisible = isBottomBarVisible,
        resetBePositionTrigger = resetBeTrigger, 
        isMultiSelectionActive = isMultiSelectionActive, 
        toolboxKey = toolboxKey, 
        showLocationTool = showLocationTool,
        // Estados de animación de Be
        beOffsetX = beOffsetX,
        beOffsetY = beOffsetY,
        isBeDragging = isBeDragging,
        dynamicBeBottomPadding = dynamicBeBottomPadding,
        onRouteChanged = { hudViewModel.onRouteChanged(it) },
        onBeClick = { hudViewModel.onBeClick() },
        onBeLongClick = { hudViewModel.onBeLongClick() },
        onBeDoubleClick = { hudViewModel.onBeDoubleClick() },
        onSearchQueryChange = { 
            hudViewModel.updateSearchQuery(it)
            beInteractionViewModel.processSearchQuery(it) // Notificamos al obrero de interacción
        },
        onSimulateFiveDirectBudgets = { simulationViewModel.simulateFiveDirectBudgetsToChat() },
        onSimulateTenderResponses = { simulationViewModel.simulateTenderResponsesForEachActive() },
        onSimulateMassiveProviders = { cats, zip, count -> simulationViewModel.simulateMassiveProviders(cats, zip, count) },
        onMigrateCategories = { simulationViewModel.uploadCategoriesToFirestore() },
        favorites = favorites, 
        allCategories = categories
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigationContent(
    initialTarget: String? = null,
    beViewModel: BeBrainViewModel,
    beAssistantViewModel: BeAssistantViewModel, // NUEVO
    beInteractionViewModel: BeInteractionViewModel, // NUEVO
    searchReaction: BeSearchReaction?, // NUEVO
    searchMenuOptions: List<ControlItem>, // NUEVO
    selectedOptionIds: Set<String>, // NUEVO
    showBe: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    beMessages: List<BeMessage>,
    beState: BeState,
    currentTipIndex: Int,
    isDormido: Boolean,
    showBeTools: Boolean,
    requestKeyboard: Boolean, 
    currentActions: List<BeSmallActionModel>,
    favorites: List<ServiceDisplayModel> = emptyList(),
    allCategories: List<CategoryEntity> = emptyList(),
    isBottomBarVisible: Boolean,
    resetBePositionTrigger: Int, 
    isMultiSelectionActive: Boolean, 
    toolboxKey: String, 
    showLocationTool: Boolean,
    // Estados de animación de Be
    beOffsetX: Float,
    beOffsetY: Float,
    isBeDragging: Boolean,
    dynamicBeBottomPadding: Dp,
    onRouteChanged: (String?) -> Unit,
    onBeClick: () -> Unit,
    onBeLongClick: () -> Unit,
    onBeDoubleClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSimulateFiveDirectBudgets: () -> Unit = {},
    onSimulateTenderResponses: () -> Unit = {},
    onSimulateMassiveProviders: (List<String>, String, Int) -> Unit = { _, _, _ -> },
    onMigrateCategories: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- SECCIÓN: REDIRECCIÓN INICIAL ---
    LaunchedEffect(initialTarget) {
        if (initialTarget == "profile") {
            navController.navigate(Screen.PerfilCliente.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(beViewModel.actionEvent) {
        beViewModel.actionEvent.collectLatest { actionId ->
            when (actionId) {
                "fast" -> navController.navigate(Screen.Fast.route) { launchSingleTop = true }
                "licit" -> navController.navigate(Screen.CrearLicitacion.route) { launchSingleTop = true }
                "fav" -> beViewModel.toggleFavoritesPanel()
                "sim_chat" -> onSimulateFiveDirectBudgets()
                "sim_tender" -> onSimulateTenderResponses()
                "sim_massive" -> beViewModel.setShowProviderSimDialog(true)
                "migrate_cats" -> onMigrateCategories()
            }
        }
    }

    var isLocationExpanded by remember { mutableStateOf(false) }

    // ==================================================================================
    // --- 🚫 SECCIÓN: CONTROL DE AUTO-EXPANSIÓN DE UBICACIÓN ---
    // ==================================================================================
    // Se ha eliminado la auto-expansión en la ruta de resultados de búsqueda para
    // evitar que el popup se abra solo al entrar.
    LaunchedEffect(currentRoute) { 
        onRouteChanged(currentRoute)
    }
    val showFavoritesPanel by beViewModel.showFavoritesPanel.collectAsStateWithLifecycle()
    val showProviderSimDialog by beViewModel.showProviderSimDialog.collectAsStateWithLifecycle()

    LaunchedEffect(resetBePositionTrigger) {
        if (resetBePositionTrigger > 0) {
            beAssistantViewModel.resetPosition()
        }
    }

    AppNavigationStateless(
        navController = navController,
        currentRoute = currentRoute,
        showBe = showBe,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        beMessages = beMessages,
        beState = beState,
        currentTipIndex = currentTipIndex,
        isDormido = isDormido,
        showBeTools = showBeTools,
        requestKeyboard = requestKeyboard,
        currentActions = currentActions,
        favorites = favorites,
        allCategories = allCategories,
        isBottomBarVisible = isBottomBarVisible,
        resetBePositionTrigger = resetBePositionTrigger,
        isMultiSelectionActive = isMultiSelectionActive,
        toolboxKey = toolboxKey,
        showLocationTool = showLocationTool,
        isLocationExpanded = isLocationExpanded,
        showFavoritesPanel = showFavoritesPanel,
        showProviderSimDialog = showProviderSimDialog,
        // Estados de animación de Be
        beOffsetX = beOffsetX,
        beOffsetY = beOffsetY,
        isBeDragging = isBeDragging,
        dynamicBeBottomPadding = dynamicBeBottomPadding,
        onUpdateBePosition = { x, y -> beAssistantViewModel.updateOffset(x, y) },
        onSetBeDragging = { beAssistantViewModel.setDragging(it) },
        onBeClick = onBeClick,
        onBeLongClick = onBeLongClick,
        onBeDoubleClick = onBeDoubleClick,
        onSearchQueryChange = onSearchQueryChange,
        onSearchSubmitted = { 
            // El Cerebro procesa el texto llamando al Obrero para determinar la reacción
            beViewModel.onSearchSubmitted(beInteractionViewModel)
        },
        onToggleLocationExpand = { isLocationExpanded = it },
        onDismissSimDialog = { beViewModel.setShowProviderSimDialog(false) },
        onConfirmSimDialog = { cats, zip, count -> beViewModel.setShowProviderSimDialog(false); onSimulateMassiveProviders(cats, zip, count) },
        onMigrateCategories = onMigrateCategories,
        onSetBeState = { beViewModel.setBeState(it) },
        onNextTip = { beViewModel.nextTip() },
        onPrevTip = { beViewModel.prevTip() },
        onBubbleActionClick = { 
            // Si el mensaje actual es el del Huevo de Pascua (detectado por el icono ❤️)
            if (beMessages.getOrNull(currentTipIndex)?.icon == "❤️") {
                beInteractionViewModel.resetEasterEgg() // El Obrero limpia su estado
                beViewModel.onEasterEggLinkClick()     // El Cerebro limpia la UI
            }
        },
        beViewModel = beViewModel,
        beInteractionViewModel = beInteractionViewModel, // Pasamos el nuevo ViewModel
        searchReaction = searchReaction, // Pasamos la reacción
        searchMenuOptions = searchMenuOptions, // Pasamos las opciones
        selectedOptionIds = selectedOptionIds // Pasamos los IDs seleccionados
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigationStateless(
    navController: NavHostController,
    currentRoute: String?,
    showBe: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    beMessages: List<BeMessage>,
    beState: BeState,
    currentTipIndex: Int,
    isDormido: Boolean,
    showBeTools: Boolean,
    requestKeyboard: Boolean,
    currentActions: List<BeSmallActionModel>,
    favorites: List<ServiceDisplayModel> = emptyList(),
    allCategories: List<CategoryEntity> = emptyList(),
    isBottomBarVisible: Boolean,
    resetBePositionTrigger: Int,
    isMultiSelectionActive: Boolean,
    toolboxKey: String,
    showLocationTool: Boolean,
    isLocationExpanded: Boolean,
    showFavoritesPanel: Boolean,
    showProviderSimDialog: Boolean,
    // Estados de animación de Be
    beOffsetX: Float = 0f,
    beOffsetY: Float = 0f,
    isBeDragging: Boolean = false,
    dynamicBeBottomPadding: Dp = 0.dp,
    onUpdateBePosition: (Float, Float) -> Unit = { _, _ -> },
    onSetBeDragging: (Boolean) -> Unit = {},
    onBeClick: () -> Unit,
    onBeLongClick: () -> Unit,
    onBeDoubleClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmitted: () -> Unit = {},
    onToggleLocationExpand: (Boolean) -> Unit,
    onDismissSimDialog: () -> Unit,
    onConfirmSimDialog: (List<String>, String, Int) -> Unit,
    onMigrateCategories: () -> Unit = {},
    onSetBeState: (BeState) -> Unit,
    onNextTip: () -> Unit,
    onPrevTip: () -> Unit,
    onBubbleActionClick: () -> Unit = {},
    beViewModel: BeBrainViewModel? = null,
    beInteractionViewModel: BeInteractionViewModel? = null, // NUEVO
    searchReaction: BeSearchReaction? = null, // NUEVO
    searchMenuOptions: List<ControlItem> = emptyList(), // NUEVO
    selectedOptionIds: Set<String> = emptySet(), // NUEVO
    navHostContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        // ==========================================================================================
        // --- 🛠️ SECCIÓN: CONFIGURACIÓN DE ANIMACIONES DE NAVEGACIÓN (MAVERICK STYLE) ---
        // ==========================================================================================
        val navItems = listOf(Screen.Home, Screen.Presupuestos, Screen.Chat, Screen.Calendar, Screen.Promo)

        // --- TRANSICIÓN DE ENTRADA: DESLIZAMIENTO LATERAL SEGÚN ÍNDICE ---
        val mainEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
            val initialIndex = getRouteIndex(initialState.destination.route, navItems)
            val targetIndex = getRouteIndex(targetState.destination.route, navItems)
            
            if (initialIndex != -1 && targetIndex != -1) {
                if (targetIndex > initialIndex) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = EaseInOutQuart))
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = EaseInOutQuart))
                }
            } else {
                fadeIn(tween(400))
            }
        }

        // --- TRANSICIÓN DE SALIDA: DESLIZAMIENTO LATERAL SEGÚN ÍNDICE ---
        val mainExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
            val initialIndex = getRouteIndex(initialState.destination.route, navItems)
            val targetIndex = getRouteIndex(targetState.destination.route, navItems)
            
            if (initialIndex != -1 && targetIndex != -1) {
                if (targetIndex > initialIndex) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500, easing = EaseInOutQuart))
                } else {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500, easing = EaseInOutQuart))
                }
            } else {
                fadeOut(tween(400))
            }
        }

        // ==========================================================================================
        // --- 🌌 NAVHOST Y ESTRUCTURA DE PANTALLAS ---
        // ==========================================================================================
        Box(modifier = Modifier.fillMaxSize().background(MaverickColors.ROG_Dark_Bg)) {
            NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.fillMaxSize()) {
                // HOME: Ahora usa transiciones explícitas para evitar el "pop" visual al volver
                composable(
                    route = Screen.Home.route, 
                    enterTransition = mainEnterTransition, 
                    exitTransition = mainExitTransition
                ) {
                    HomeScreenComplete(navController = navController, beViewModel = beViewModel ?: hiltViewModel())
                }
                
                composable(route = Screen.Chat.route, arguments = listOf(navArgument("providerId") { type = NavType.StringType; nullable = true; defaultValue = null }), enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId")
                    // ==========================================================================================
                    // --- 🛠️ SECCIÓN: NAVEGACIÓN A CHAT ---
                    // Pasamos la lambda onInConversationChange para que el ChatScreen pueda avisar al Cerebro (BeBrain)
                    // cuando debe ocultar o mostrar la barra de navegación inferior (Lista vs Conversación).
                    // ==========================================================================================
                    ChatScreen(
                        onBack = { navController.popBackStack() }, 
                        initialProviderId = providerId,
                        onInConversationChange = { isInConversation ->
                            beViewModel?.setBottomBarVisible(!isInConversation)
                        }
                    )
                }
                
                composable(route = Screen.Calendar.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { CalendarScreen(onBack = { navController.popBackStack() }) }
                
                composable(route = Screen.Promo.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { PromoScreen(navController = navController, onBack = { navController.popBackStack() }) }
                
                composable(route = Screen.CrearLicitacion.route) { CrearLicScreen(onBack = { navController.popBackStack() }) }
                
                composable(route = Screen.PerfilCliente.route) { PerfilUsuarioScreen(onNavigateBack = { navController.popBackStack() }, onLogout = { }, beViewModel = beViewModel ?: hiltViewModel()) }
                
                composable(route = Screen.ResultBusqueda.route, arguments = listOf(navArgument("category") { type = NavType.StringType })) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    ResultBusquedaCategoriaScreen(categoryName = category, onBack = { navController.popBackStack() }, onNavigateToProviderProfile = { pid -> navController.navigate("perfil_prestador/$pid") }, onNavigateToChat = { pid -> navController.navigate("chat?providerId=$pid") }, beViewModel = beViewModel ?: hiltViewModel())
                }
                
                composable(route = Screen.PerfilPrestador.route, arguments = listOf(navArgument("providerId") { type = NavType.StringType })) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                    PerfilPrestadorCliente(providerId = providerId, onBack = { navController.popBackStack() })
                }
                
                composable(route = Screen.Fast.route) { FastScreen(navController = navController, bottomPadding = innerPadding) }
                
                composable(
                    route = Screen.Presupuestos.route, 
                    enterTransition = mainEnterTransition, 
                    exitTransition = mainExitTransition
                ) {
                    PresupuestosScreen(
                        hiltViewModel(),
                        hiltViewModel(),
                        beViewModel ?: hiltViewModel(),
                        beInteractionViewModel ?: hiltViewModel(),
                        { pid -> navController.navigate("chat?providerId=$pid") },
                        { navController.popBackStack() },
                        innerPadding
                    )
                }
            }
        }
    }
) {
    val navItems = listOf(Screen.Home, Screen.Presupuestos, Screen.Chat, Screen.Calendar, Screen.Promo)
    
    // --- SECCIÓN: CÁLCULO DE VISIBILIDAD DE BARRA INFERIOR ---
    // Se elimina la exclusión explícita del ChatRoute para permitir que la barra se muestre 
    // en la lista de conversaciones. La visibilidad ahora depende dinámicamente de beViewModel.
    val isMainRoute = currentRoute?.split("?")?.first() in navItems.map { it.route.split("?").first() }
    val shouldShowBottomBar = isMainRoute && isBottomBarVisible

    Box(modifier = Modifier.fillMaxSize()) {
        AppHUDShell(
            shouldShowBottomBar = shouldShowBottomBar,
            bottomBar = {
                // ==========================================================================================
                // --- REQUERIMIENTO: ANIMACIÓN DE ENTRADA/SALIDA (SLIDE BOTTOM) ---
                // La barra desaparece hacia abajo cuando la búsqueda está activa.
                // ==========================================================================================
                AnimatedVisibility(
                    visible = shouldShowBottomBar && !isSearchActive,
                    enter = slideInVertically(
                        initialOffsetY = { it }, 
                        animationSpec = tween(durationMillis = 400)
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it }, 
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut()
                ) {
                    AppBottomNavigationBar(navController, navItems, currentRoute, beViewModel)
                }
            }
        ) { innerPadding ->
            navHostContent(innerPadding)
        }

        /** 
         * BeResultadoScreen deshabilitada en favor de BeSearchBubble integrada en BeAssistant.
         * Se conserva el archivo pero se quita la llamada.
         if (beViewModel != null) {
            BeResultadoScreen(
                viewModel = beViewModel,
                onClose = onCloseResultado,
                onProviderClick = onProviderClick,
                allCategories = allCategories,
                modifier = Modifier.zIndex(400f), 
                onCategoryClick = onCategoryClick,
                onSuperCategoryClick = onSuperCategoryClick
            )
        }
        **/

        AnimatedVisibility(
            visible = showBe,
            modifier = Modifier.zIndex(500f) 
        ) {
            val beVerticalBias by animateFloatAsState(
                targetValue = when {
                    isSearchActive -> -1f
                    isDormido -> 0f // Modo hibernación: Mitad de la pantalla
                    else -> 1f
                }, 
                label = "v_bias"
            )
            
            // 🔥 DETERMINAMOS EL PADDING REAL: 
            // Si la barra de navegación no se muestra, el padding debe ser 0 para que Be "caiga" al borde.
            val targetBePadding = if (shouldShowBottomBar && !isSearchActive) dynamicBeBottomPadding else 0.dp

            val isBubbleMuted by beViewModel?.isBubbleMuted?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
            val hasNewMessage by beViewModel?.hasNewMessage?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            ) {
                BeAssistantSearchFab(
                    modifier = Modifier.align(BiasAlignment(horizontalBias = 1f, verticalBias = beVerticalBias)),
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    searchReaction = searchReaction,
                    contextMessages = beMessages,
                    state = beState,
                    currentTipIndex = currentTipIndex,
                    onSearchQueryChange = onSearchQueryChange,
                    onSearchSubmitted = onSearchSubmitted,
                    isDormido = isDormido,
                    currentActions = currentActions,
                    showSmallActions = showBeTools,
                    requestKeyboard = requestKeyboard,
                    resetTrigger = resetBePositionTrigger,
                    isMultiSelectionActive = isMultiSelectionActive,
                    shouldShowBottomBar = shouldShowBottomBar,
                    toolboxKey = toolboxKey,
                    isLocationExpanded = isLocationExpanded,
                    onToggleLocationExpand = onToggleLocationExpand,
                    offsetX = beOffsetX,
                    offsetY = beOffsetY,
                    isDragging = isBeDragging,
                    onUpdatePosition = onUpdateBePosition,
                    onSetDragging = onSetBeDragging,
                    isBubbleMuted = isBubbleMuted,
                    hasNewMessage = hasNewMessage,
                    onToggleBubbleMute = { beViewModel?.toggleBubbleMute() },
                    onReactionActionClick = { actionId ->
                        beViewModel?.triggerAction(actionId)
                        if (actionId.startsWith("filter_") || actionId.startsWith("cat_")) {
                            beViewModel?.toggleFilter(actionId)
                        }
                        beInteractionViewModel?.clearReaction()
                        beViewModel?.updateSearchQuery("")
                    },
                    onReactionCloseClick = { beInteractionViewModel?.clearReaction() },
                    locationToolContent = {
                        AnimatedVisibility(
                            visible = showLocationTool && currentRoute?.contains("fast") == true,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            // Contenido
                        }
                    },
                    onToggleSearch = onBeClick,
                    onToggleActions = onBeLongClick,
                    onToggleSleep = onBeDoubleClick,
                    onSetState = onSetBeState,
                    onNextTip = onNextTip,
                    onPrevTip = onPrevTip,
                    beBottomPadding = targetBePadding, // 🔥 PASAMOS EL PADDING CALCULADO
                    onBubbleActionClick = onBubbleActionClick,
                    searchMenuOptions = searchMenuOptions,
                    onMenuOptionClick = { optionId -> beInteractionViewModel?.onMenuOptionClick(optionId) },
                    selectedOptionIds = selectedOptionIds
                )
            }
        }

        if (showFavoritesPanel) {
            Box(modifier = Modifier.fillMaxSize().zIndex(600f).background(Color.Black.copy(alpha = 0.65f)).clickable { beViewModel?.setFavoritesPanelVisible(false) })
            AnimatedVisibility(visible = showFavoritesPanel, enter = slideInHorizontally { it }, exit = slideOutHorizontally { it }, modifier = Modifier.align(Alignment.CenterEnd).zIndex(610f)) {
                FavoritesPanel(navController, favorites) { beViewModel?.setFavoritesPanelVisible(false) }
            }
        }

        if (showProviderSimDialog) {
            ProviderSimulationDialog(
                allCategories = allCategories,
                onDismiss = onDismissSimDialog,
                onConfirm = onConfirmSimDialog,
                onMigrateCategories = onMigrateCategories
            )
        }
    }
}

@Composable
fun AppHUDShell(
    shouldShowBottomBar: Boolean,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
        Scaffold(bottomBar = { if (shouldShowBottomBar) bottomBar() }, containerColor = Color.Transparent) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    navController: NavHostController, 
    allItems: List<Screen>, 
    currentRoute: String?,
    beViewModel: BeBrainViewModel? = null // Recibimos el cerebro para las notificaciones
) {
    // ==========================================================================================
    // --- SECCIÓN 1: CONFIGURACIÓN DE DIMENSIONES Y FORMA (ESTILO MAVERICK CUT) ---
    // ==========================================================================================
    val navBarHeight = 62.dp // Altura reducida de 76.dp a 62.dp
    val navigationInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding = navigationInsets.calculateBottomPadding()
    
    // --- ESTADOS DE NOTIFICACIÓN DESDE EL CEREBRO ---
    val hasChatNotif by beViewModel?.hasChatNotifications?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    //val hasBudgetNotif by beViewModel?.hasBudgetNotifications?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    //val hasCalendarNotif by beViewModel?.hasCalendarNotifications?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
   // val hasPromoNotif by beViewModel?.hasPromoNotifications?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    // Forma con cortes en las esquinas superiores (estilo MoldeBarraMenu)
    val barShape = CutCornerShape(topStart = 12.dp, topEnd = 12.dp)
    val geminiBrush = geminiGradientBrush()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(navBarHeight + bottomPadding)
            .zIndex(950f)
            .pointerInput(Unit) {
                detectTapGestures { /* Bloqueo de interacciones */ }
            }
            .drawWithCache {
                val path = Path()
                val strokeWidth = 1.dp.toPx()
                val cornerSize = 12.dp.toPx()
                
                // Pre-calculamos el path para evitar crearlo en cada frame
                path.reset()
                path.moveTo(0f, cornerSize)
                path.lineTo(cornerSize, 0f)
                path.lineTo(size.width - cornerSize, 0f)
                path.lineTo(size.width, cornerSize)

                onDrawWithContent {
                    drawContent()
                    // 1. BORDE SUPERIOR PERSONALIZADO
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.2f),
                        style = Stroke(width = strokeWidth)
                    )
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // ==========================================================================================
        // --- SECCIÓN 2: FONDO Y CONTENEDOR DE LA BARRA ---
        // ==========================================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(barShape) // Aplicamos el corte superior a la visual
                .background(MaverickColors.AbsoluteBlack)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(navBarHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            allItems.forEach { screen ->
                val isSelected = currentRoute?.startsWith(screen.route.split("?").first()) == true
                val scope = rememberCoroutineScope()
                
                // --- LÓGICA DE BADGE: DETERMINAR SI ESTE TAB TIENE NOTIFICACIONES ---
                val hasNotification = when (screen) {
                    is Screen.Chat -> hasChatNotif
                    //is Screen.Presupuestos -> hasBudgetNotif
                    //is Screen.Calendar -> hasCalendarNotif
                    //is Screen.Promo -> hasPromoNotif
                    else -> false
                }

                // ==========================================================================================
                // 🛠️ SECCIÓN 3: CONFIGURACIÓN DE ANIMACIONES Y BOTONES
                // ==========================================================================================
                
                // --- EFECTO DE ANCHO DINÁMICO ---
                val animatedWidth by animateDpAsState(
                    targetValue = if (isSelected) 100.dp else 52.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "nav_width"
                )
                
                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(48.dp) // Altura del botón ajustada ligeramente para la barra más baja
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            brush = if (isSelected) geminiBrush else SolidColor(Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(BentoDarkGlassBackground.copy(alpha = if (isSelected) 0.85f else 0.2f))
                        .shakeClick {
                            if (!isSelected) {
                                scope.launch {
                                    // ==========================================================================================
                                    // --- 🛠️ SECCIÓN: LÓGICA DE NAVEGACIÓN OPTIMIZADA ---
                                    // Se asegura de mantener el estado y realizar transiciones limpias.
                                    // CORRECCIÓN: Si es Chat, navegamos a la ruta base para evitar el placeholder {providerId}
                                    // ==========================================================================================
                                    val destination = if (screen is Screen.Chat) "chat" else screen.route
                                    
                                    navController.navigate(destination) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // --- CONTENIDO: EMOJI (ACTIVO) O ICONO (INACTIVO) + CYAN BADGE ---
                    Box(contentAlignment = Alignment.TopEnd) {
                        if (isSelected) {
                            Text(
                                text = getEmojiForScreen(screen),
                                fontSize = 24.sp // Ajustado de 26 a 24 para armonía visual con la barra más baja
                            )
                        } else {
                            Icon(
                                imageVector = when (screen) {
                                    is Screen.Home -> Icons.Filled.Home
                                    is Screen.Presupuestos -> Icons.Filled.AttachMoney
                                    is Screen.Chat -> Icons.AutoMirrored.Filled.Chat
                                    is Screen.Calendar -> Icons.Filled.CalendarToday
                                    is Screen.Promo -> Icons.Filled.LocalFireDepartment
                                    else -> Icons.Filled.Home
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp), // SE MANTIENE EL TAMAÑO DE ICONO
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        // --- CYAN NOTIFICATION BADGE (MAVERICK HUD V5) ---
                        if (hasNotification) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color(0xFF22D3EE), CircleShape) // Cyber Cyan
                                    .border(1.5.dp, Color.Black, CircleShape) // Contraste contra el fondo
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getRouteIndex(route: String?, navItems: List<Screen>): Int {
    if (route == null) return -1
    val baseRoute = route.substringBefore("?").substringBefore("/")
    return navItems.indexOfFirst { it.route.substringBefore("?").substringBefore("/") == baseRoute }
}

fun getEmojiForScreen(screen: Screen): String = when (screen) {
    Screen.Home -> "🏠"; Screen.Presupuestos -> "💰"; Screen.Chat -> "💬"; Screen.Calendar -> "📅"; Screen.Promo -> "🔥"; else -> ""
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AppNavigationPreview() {
    MyApplicationTheme {
        val navController = rememberNavController()
        AppNavigationStateless(
            navController = navController,
            currentRoute = Screen.Home.route,
            showBe = true,
            isSearchActive = false,
            searchQuery = "",
            beMessages = BeDictionary.HomeMessages,
            beState = BeState.IDLE,
            currentTipIndex = 0,
            isDormido = false,
            showBeTools = false,
            requestKeyboard = false,
            currentActions = listOf(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true)),
            favorites = emptyList(),
            allCategories = listOf(
                //CategoryEntity(name = "Informatica", icon = "💻", color = 0xFF22D3EE, superCategory = "Tecnología", superCategoryIcon = "📂", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false),
                //CategoryEntity(name = "Electricidad", icon = "⚡", color = 0xFFFACC15, superCategory = "Hogar", superCategoryIcon = "📂", providerIds = emptyList(), imageUrl = null, isNew = false, isNewPrestador = false, isAd = false)
            ),
            isBottomBarVisible = true,
            resetBePositionTrigger = 0,
            isMultiSelectionActive = false,
            toolboxKey = "home_default",
            showLocationTool = false,
            isLocationExpanded = false,
            showFavoritesPanel = false,
            showProviderSimDialog = false,
            onBeClick = {},
            onBeLongClick = {},
            onBeDoubleClick = {},
            onSearchQueryChange = {},
            onDismissSimDialog = {},
            onConfirmSimDialog = { _, _, _ -> },
            onSetBeState = {},
            onNextTip = {},
            onPrevTip = {},
            onToggleLocationExpand = {},
            beViewModel = null,
            navHostContent = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Home Screen Content", color = Color.White)
                }
            }
        )
    }
}

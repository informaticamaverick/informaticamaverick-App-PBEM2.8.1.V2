package com.example.myapplication.presentation.features.home

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.components.MaverickColors.BentoDarkGlassBackground
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.example.myapplication.presentation.features.chat.*
import com.example.myapplication.presentation.features.calendar.*
import com.example.myapplication.presentation.features.profile.*
import com.example.myapplication.presentation.features.budget.*
import com.example.myapplication.presentation.registry.BeDictionaryConversation
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager

// ==================================================================================
// --- SECCIÓN: RUTAS DE NAVEGACIÓN ---
// ==================================================================================
sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Inicio")
    object Presupuestos : Screen("presupuestos", "Presupuestos")
    object Chat : Screen("chat?providerId={providerId}&companyId={companyId}&categoryId={categoryId}", "Chat") {
        fun createRoute(providerId: String? = null, companyId: String? = null, categoryId: String? = null) = 
            "chat?providerId=${providerId ?: ""}&companyId=${companyId ?: ""}&categoryId=${categoryId ?: ""}"
    }
    object Calendar : Screen("calendar", "Calendario")
    object Promo : Screen("promo", "Promociones")
    object PerfilPrestador : Screen("perfil_prestador/{providerId}", "Perfil del Prestador")
    object PerfilCliente : Screen("perfil_cliente", "Mi Perfil")
    object ResultBusqueda : Screen("result_busqueda/{category}", "Resultados de Búsqueda")
    object Fast : Screen("fast", "Maverick FAST")
    object Configuracion : Screen("config_user", "Ajustes")
}

/**
 * --- APP NAVIGATION (COORDINADOR GLOBAL v2.3) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    initialTarget: String? = null,
    onLogoutRequest: () -> Unit = {}, 
    hudViewModel: BeBrainViewModel = hiltViewModel(),
    providerViewModel: ProviderViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    simulationViewModel: SimulationViewModel = hiltViewModel(),
    beAssistantViewModel: BeAssistantViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionClimaViewModel = hiltViewModel()
) {
    val coordinator = hudViewModel.coordinator

    val showBe by hudViewModel.showBe.collectAsStateWithLifecycle()
    val isSearchActive by hudViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQueries by hudViewModel.searchQuery.collectAsStateWithLifecycle()
    val beMessages by hudViewModel.beMessages.collectAsStateWithLifecycle()
    val activeConversationalMessage by hudViewModel.activeConversationalMessage.collectAsStateWithLifecycle()
    val currentActions by hudViewModel.currentActions.collectAsStateWithLifecycle()
    val isBottomBarVisible by hudViewModel.isBottomBarVisible.collectAsStateWithLifecycle()
    val beState by hudViewModel.beState.collectAsStateWithLifecycle()
    val currentTipIndex by hudViewModel.currentTipIndex.collectAsStateWithLifecycle()
    val isDormido by hudViewModel.isBeDormido.collectAsStateWithLifecycle()
    val showBeTools by hudViewModel.showBeTools.collectAsStateWithLifecycle()
    val requestKeyboard by hudViewModel.requestKeyboard.collectAsStateWithLifecycle() 
    val resetBePositionTrigger by hudViewModel.resetBePositionTrigger.collectAsStateWithLifecycle()
    val isMultiSelectionActive by hudViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val toolboxKey by hudViewModel.toolboxKey.collectAsStateWithLifecycle()

    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    val isBubbleMuted by hudViewModel.isBubbleMuted.collectAsStateWithLifecycle()
    val hasNewMessage by hudViewModel.hasNewMessage.collectAsStateWithLifecycle()

    val beOffsetX by beAssistantViewModel.offsetX.collectAsStateWithLifecycle()
    val beOffsetY by beAssistantViewModel.offsetY.collectAsStateWithLifecycle()
    val isBeDragging by beAssistantViewModel.isDragging.collectAsStateWithLifecycle()
    val dynamicBeBottomPadding by beAssistantViewModel.beBottomPadding.collectAsStateWithLifecycle()
    val isToolbarStable by beAssistantViewModel.isToolbarStable.collectAsStateWithLifecycle()

    LaunchedEffect(toolboxKey) {
        beAssistantViewModel.notifyToolboxChanged()
    }

    LaunchedEffect(showBe) {
        if (!showBe) {
            beAssistantViewModel.resetPosition()
        }
    }

    LaunchedEffect(isBottomBarVisible, isSearchActive) {
        beAssistantViewModel.updateLayout(isBottomBarVisible, isSearchActive)
    }

    AppNavigationContent(
        initialTarget = initialTarget,
        onLogoutRequest = onLogoutRequest, 
        beViewModel = hudViewModel,
        coordinator = hudViewModel.coordinator, 
        beAssistantViewModel = beAssistantViewModel, 
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
        resetBePositionTrigger = resetBePositionTrigger,
        isMultiSelectionActive = isMultiSelectionActive,
        toolboxKey = toolboxKey,
        beOffsetX = beOffsetX,
        beOffsetY = beOffsetY,
        isBeDragging = isBeDragging,
        dynamicBeBottomPadding = dynamicBeBottomPadding,
        isToolbarStable = isToolbarStable, 
        onRouteChanged = { hudViewModel.onRouteChanged(it) },
        onBeClick = { hudViewModel.onBeClick() },
        onBeLongClick = { hudViewModel.onBeLongClick() },
        onBeDoubleClick = { hudViewModel.onBeDoubleClick() },
        onSearchQueryChange = {
            coordinator.updateSearchQuery(it)
            hudViewModel.setResultadoVisible(it.isNotEmpty()) 
        },
        onSimulateFiveDirectBudgets = { simulationViewModel.simulateFiveDirectBudgetsToChat() },
        onSimulateTenderResponses = { simulationViewModel.simulateTenderResponsesForEachActive() },
        onSimulateMassiveProviders = { cats, zip, count -> simulationViewModel.simulateMassiveProviders(cats, zip, count) },
        onMigrateCategories = { simulationViewModel.uploadCategoriesToFirestore() },
        activeConversationalMessage = activeConversationalMessage, 
        allCategories = categories,
        isBubbleMuted = isBubbleMuted,
        hasNewMessage = hasNewMessage,
        providerViewModel = providerViewModel,
        categoryViewModel = categoryViewModel,
        profileViewModel = profileViewModel,
        ubicacionObrero = ubicacionObrero
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigationContent(
    initialTarget: String? = null,
    onLogoutRequest: () -> Unit = {}, 
    beViewModel: BeBrainViewModel,
    coordinator: AppActionCoordinator, 
    beAssistantViewModel: BeAssistantViewModel, 
    showBe: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    beMessages: List<BeMessage>,
    beState: BeState,
    currentTipIndex: Int,
    isDormido: Boolean,
    showBeTools: Boolean,
    activeConversationalMessage: BeMessage?, 
    requestKeyboard: Boolean, 
    currentActions: List<BeSmallActionModel>,
    favorites: List<ProviderDisplayModel> = emptyList(),
    allCategories: List<CategoryEntity> = emptyList(),
    isBubbleMuted: Boolean = false,
    hasNewMessage: Boolean = false,
    isBottomBarVisible: Boolean,
    resetBePositionTrigger: Int, 
    isMultiSelectionActive: Boolean, 
    toolboxKey: String, 
    beOffsetX: Float,
    beOffsetY: Float,
    isBeDragging: Boolean,
    dynamicBeBottomPadding: Dp,
    isToolbarStable: Boolean = true, 
    onRouteChanged: (String?) -> Unit,
    onBeClick: () -> Unit,
    onBeLongClick: () -> Unit,
    onBeDoubleClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSimulateFiveDirectBudgets: () -> Unit = {},
    onSimulateTenderResponses: () -> Unit = {},
    onSimulateMassiveProviders: (List<String>, String, Int) -> Unit = { _, _, _ -> },
    onMigrateCategories: () -> Unit = {},
    providerViewModel: ProviderViewModel,
    categoryViewModel: CategoryViewModel,
    profileViewModel: ProfileViewModel,
    ubicacionObrero: UbicacionClimaViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(initialTarget) {
        if (initialTarget == "profile") {
            navController.navigate(Screen.PerfilCliente.route) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    val showFavoritesPanel by beViewModel.showFavoritesPanel.collectAsStateWithLifecycle()
    val showProviderSimDialog by beViewModel.showProviderSimDialog.collectAsStateWithLifecycle()


    LaunchedEffect(beViewModel.actionEvent) {
        beViewModel.actionEvent.collectLatest { actionId ->
            when (actionId) {
                "fast" -> navController.navigate(Screen.Fast.route) { launchSingleTop = true }
                "fav" -> beViewModel.toggleFavoritesPanel()
                "settings_profile" -> navController.navigate(Screen.Configuracion.route)
                "sim_chat" -> onSimulateFiveDirectBudgets()
                "sim_tender" -> onSimulateTenderResponses()
                "sim_massive" -> beViewModel.setShowProviderSimDialog(true)
                "migrate_cats" -> onMigrateCategories()
            }
        }
    }

    LaunchedEffect(currentRoute) {
        onRouteChanged(currentRoute)
    }

    LaunchedEffect(resetBePositionTrigger) {
        if (resetBePositionTrigger > 0) {
            beAssistantViewModel.resetPosition()
        }
    }

    AppNavigationStateless(
        navController = navController,
        currentRoute = currentRoute,
        onLogoutRequest = onLogoutRequest, 
        coordinator = coordinator, 
        showBe = showBe,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        beMessages = beMessages,
        activeConversationalMessage = activeConversationalMessage, 
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
        showFavoritesPanel = showFavoritesPanel,
        showProviderSimDialog = showProviderSimDialog,
        isBubbleMuted = isBubbleMuted,
        hasNewMessage = hasNewMessage,
        isToolbarStable = isToolbarStable, 
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
        onSearchSubmitted = { beViewModel.onSearchSubmitted() },
        onDismissSimDialog = { beViewModel.setShowProviderSimDialog(false) },
        onConfirmSimDialog = { cats, zip, count -> beViewModel.setShowProviderSimDialog(false); onSimulateMassiveProviders(cats, zip, count) },
        onMigrateCategories = onMigrateCategories,
        onSetBeState = { beViewModel.setBeState(it) },
        onNextTip = { beViewModel.nextTip() },
        onPrevTip = { beViewModel.prevTip() },
        onBubbleActionClick = {
            if (beMessages.getOrNull(currentTipIndex)?.icon == "❤️") {
                beViewModel.onEasterEggLinkClick()     
            }
        },
        beViewModel = beViewModel,
        categoryViewModel = categoryViewModel,
        providerViewModel = providerViewModel,
        profileViewModel = profileViewModel,
        ubicacionObrero = ubicacionObrero
        )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigationStateless(
    navController: NavHostController,
    currentRoute: String?,
    onLogoutRequest: () -> Unit = {}, 
    coordinator: AppActionCoordinator? = null, 
    showBe: Boolean,
    isSearchActive: Boolean,
    searchQuery: String,
    beMessages: List<BeMessage>,
    beState: BeState,
    currentTipIndex: Int,
    isDormido: Boolean,
    showBeTools: Boolean,
    activeConversationalMessage: BeMessage?, 
    requestKeyboard: Boolean,
    currentActions: List<BeSmallActionModel>,
    favorites: List<ProviderDisplayModel> = emptyList(),
    allCategories: List<CategoryEntity> = emptyList(),
    isBottomBarVisible: Boolean,
    resetBePositionTrigger: Int,
    isMultiSelectionActive: Boolean,
    toolboxKey: String,
    showFavoritesPanel: Boolean,
    showProviderSimDialog: Boolean,
    isBubbleMuted: Boolean = false,
    hasNewMessage: Boolean = false,
    isToolbarStable: Boolean = true, 
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
    onDismissSimDialog: () -> Unit,
    onConfirmSimDialog: (List<String>, String, Int) -> Unit,
    onMigrateCategories: () -> Unit = {},
    onSetBeState: (BeState) -> Unit,
    onNextTip: () -> Unit,
    onPrevTip: () -> Unit,
    onBubbleActionClick: () -> Unit = {},
    beViewModel: BeBrainViewModel? = null,
    categoryViewModel: CategoryViewModel? = null,
    providerViewModel: ProviderViewModel? = null,
    profileViewModel: ProfileViewModel? = null,
    ubicacionObrero: UbicacionClimaViewModel? = null,
    navHostContent: @Composable (PaddingValues) -> Unit = { innerPadding ->
        val navItems = listOf(
            Screen.Home, 
            Screen.Presupuestos, 
            Screen.Chat, 
            Screen.Calendar, 
            Screen.Promo
        )

        val mainEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
            val initialIndex = getRouteIndex(initialState.destination.route, navItems)
            val targetIndex = getRouteIndex(targetState.destination.route, navItems)

            if (initialIndex != -1 && targetIndex != -1) {
                if (targetIndex > initialIndex) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400, easing = FastOutSlowInEasing))
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400, easing = FastOutSlowInEasing))
                }
            } else {
                fadeIn(tween(300))
            }
        }
        val mainExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
            val initialIndex = getRouteIndex(initialState.destination.route, navItems)
            val targetIndex = getRouteIndex(targetState.destination.route, navItems)

            if (initialIndex != -1 && targetIndex != -1) {
                if (targetIndex > initialIndex) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400, easing = FastOutSlowInEasing))
                } else {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400, easing = FastOutSlowInEasing))
                }
            } else {
                fadeOut(tween(300))
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(MaverickColors.ROG_Dark_Bg)) {
            NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.fillMaxSize()) {
                composable(
                    route = Screen.Home.route,
                    enterTransition = mainEnterTransition,
                    exitTransition = mainExitTransition
                ) {
                    HomeScreenComplete(
                        navController = navController, 
                        beViewModel = beViewModel ?: hiltViewModel(),
                        categoryViewModel = categoryViewModel ?: hiltViewModel(),
                        providerViewModel = providerViewModel ?: hiltViewModel(),
                        profileViewModel = profileViewModel ?: hiltViewModel(),
                        ubicacionObrero = ubicacionObrero ?: hiltViewModel(),
                        onLogoutRoot = onLogoutRequest 
                    )
                }

                composable(
                    route = Screen.Chat.route,
                    arguments = listOf(
                        navArgument("providerId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("companyId") { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("categoryId") { type = NavType.StringType; nullable = true; defaultValue = null }
                    ),
                    enterTransition = mainEnterTransition,
                    exitTransition = mainExitTransition
                ) { backStackEntry ->
                    val providerId = backStackEntry.arguments?.getString("providerId")
                    val companyId = backStackEntry.arguments?.getString("companyId")
                    val categoryId = backStackEntry.arguments?.getString("categoryId")
                    
                    ChatScreen(
                        onBack = { navController.popBackStack() }, 
                        initialProviderId = providerId,
                        initialCompanyId = companyId,
                        initialCategoryId = categoryId,
                        navController = navController, 
                        beBrainViewModel = beViewModel ?: hiltViewModel(), 
                        onInConversationChange = { isInConversation ->
                            if (isInConversation) {
                                beViewModel?.setHUDContext(HUDContext.CHAT_CONVERSATION)
                            } else {
                                beViewModel?.setHUDContext(HUDContext.CHAT)
                            }
                        }
                    )
                }

                composable(route = Screen.Calendar.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { 
                    CalendarScreen(
                        onBack = { navController.popBackStack() },
                        onChatClick = { pid -> navController.navigate("chat?providerId=$pid") },
                        onNavigateToProfile = { pid -> navController.navigate("perfil_prestador/$pid") }
                    ) 
                }

                composable(route = Screen.Promo.route, enterTransition = mainEnterTransition, exitTransition = mainExitTransition) { 
                    PromoScreen(
                        navController = navController, 
                        onBack = { navController.popBackStack() },
                        beViewModel = beViewModel ?: hiltViewModel()
                    ) 
                }

                composable(
                    route = Screen.PerfilCliente.route,
                    enterTransition = mainEnterTransition,
                    exitTransition = mainExitTransition
                ) { 
                    PerfilUsuarioScreen(
                        onNavigateBack = { navController.popBackStack() }, 
                        onLogout = onLogoutRequest, 
                        beViewModel = beViewModel ?: hiltViewModel()
                    ) 
                }

                composable(
                    route = Screen.Configuracion.route,
                    enterTransition = mainEnterTransition,
                    exitTransition = mainExitTransition
                ) {
                    ConfigUserScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onAccountDeleted = { onLogoutRequest() }
                    )
                }

                composable(
                    route = Screen.ResultBusqueda.route, 
                    arguments = listOf(navArgument("category") { type = NavType.StringType }),
                    enterTransition = mainEnterTransition,
                    exitTransition = mainExitTransition
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    ResultBusquedaCategoriaScreen(
                        categoryName = category, 
                        onBack = { navController.popBackStack() }, 
                        onNavigateToProviderProfile = { pid -> navController.navigate("perfil_prestador/$pid") }, 
                        onNavigateToChat = { service -> 
                            navController.navigate("chat?providerId=${service.id}&companyId=${service.companyId ?: ""}&categoryId=${service.categoryId ?: ""}") 
                        }, 
                        beViewModel = beViewModel ?: hiltViewModel()
                    )
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
                        viewModel = hiltViewModel(),
                        categoryViewModel = categoryViewModel ?: hiltViewModel(),
                        beBrainViewModel = beViewModel ?: hiltViewModel(),
                        onChatClick = { pid, _ -> navController.navigate("chat?providerId=$pid") },
                        onBack = { navController.popBackStack() },
                        bottomPadding = innerPadding
                    )
                }
            }
        }
    }
) {
    val navItems = listOf(Screen.Home, Screen.Presupuestos, Screen.Chat, Screen.Calendar, Screen.Promo)

    Box(modifier = Modifier.fillMaxSize()) {
        AppHUDShell(
            shouldShowBottomBar = isBottomBarVisible,
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible && !isSearchActive,
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
        AnimatedVisibility(
            visible = showBe,
            modifier = Modifier.zIndex(1100f), 
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(400)),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(300))
        ) {
            val beVerticalBias by animateFloatAsState(
                targetValue = when {
                    isSearchActive -> -1f
                    isDormido -> 0f 
                    else -> 1f
                },
                label = "v_bias"
            )

            val targetBePadding = if (isBottomBarVisible && !isSearchActive) dynamicBeBottomPadding else 0.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            ) {
                BeAssistantSearchFab(
                    modifier = Modifier.align(BiasAlignment(horizontalBias = 1f, verticalBias = beVerticalBias)),
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    activeConversationalMessage = activeConversationalMessage, 
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
                    shouldShowBottomBar = isBottomBarVisible,
                    toolboxKey = toolboxKey,
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
                        beViewModel?.clearActiveResponse()
                        coordinator?.updateSearchQuery("")
                    },
                    onReactionCloseClick = { beViewModel?.clearActiveResponse() },
                    onToggleSearch = onBeClick,
                    onToggleActions = onBeLongClick,
                    onToggleSleep = onBeDoubleClick,
                    onSetState = onSetBeState,
                    onNextTip = onNextTip,
                    onPrevTip = onPrevTip,
                    beBottomPadding = targetBePadding, 
                    onBubbleActionClick = onBubbleActionClick,
                    onMenuOptionClick = { optionId -> },
                    isToolbarStable = isToolbarStable,
                    beBrainViewModel = beViewModel
                    )
            }
        }

        AnimatedVisibility(
            visible = showFavoritesPanel,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .zIndex(610f)
        ) {
            FavoritesPanel(navController, favorites) { beViewModel?.setFavoritesPanelVisible(false) }
        }

        if (showFavoritesPanel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(600f)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable { beViewModel?.setFavoritesPanelVisible(false) }
            )
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
    beViewModel: BeBrainViewModel? = null 
) {
    val navBarHeight = 62.dp 
    val navigationInsets = WindowInsets.navigationBars.asPaddingValues()
    val bottomPadding = navigationInsets.calculateBottomPadding()

    val hasChatNotif by beViewModel?.hasChatNotifications?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    val barShape = CutCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val geminiBrush = geminiGradientBrush()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(navBarHeight + bottomPadding)
            .zIndex(950f)
            .pointerInput(Unit) {
                detectTapGestures { }
            }
            .drawWithCache {
                val path = Path()
                val shadowPath = Path()
                val strokeWidth = 1.dp.toPx()
                val cornerSize = 16.dp.toPx()
                val shadowHeight = 24.dp.toPx()

                // Gradiente optimizado para que inicie y termine como una línea muy fina (baja opacidad)
                val borderGradient = Brush.horizontalGradient(
                    0.0f to CPCyberColors.MaverickCyan.copy(alpha = 0.05f),
                    0.15f to CPCyberColors.MaverickCyan,
                    0.85f to CPCyberColors.MaverickCyan,
                    1.0f to CPCyberColors.MaverickCyan.copy(alpha = 0.05f)
                )

                // 1. Path del Borde Superior
                path.reset()
                path.moveTo(0f, cornerSize)
                path.lineTo(cornerSize, 0f)
                path.lineTo(size.width - cornerSize, 0f)
                path.lineTo(size.width, cornerSize)

                // 2. Path de la Sombra Proyectada (Sigue el corte)
                shadowPath.reset()
                shadowPath.moveTo(0f, cornerSize)
                shadowPath.lineTo(cornerSize, 0f)
                shadowPath.lineTo(size.width - cornerSize, 0f)
                shadowPath.lineTo(size.width, cornerSize)
                // Extendemos hacia arriba para el degradado
                shadowPath.lineTo(size.width, -shadowHeight)
                shadowPath.lineTo(0f, -shadowHeight)
                shadowPath.close()

                onDrawWithContent {
                    // --- 0. EFECTO 3D: SOMBRA PROYECTADA ---
                    // Dibujamos una neblina oscura que emerge de los cortes hacia arriba
                    drawPath(
                        path = shadowPath,
                        brush = Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                            startY = 0f,
                            endY = -shadowHeight
                        )
                    )

                    drawContent()
                    
                    // 1. Borde Principal
                    drawPath(
                        path = path,
                        brush = borderGradient,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    // 2. Glow Tenue (Coherente con el gradiente de la línea)
                    drawPath(
                        path = path,
                        brush = borderGradient,
                        style = Stroke(
                            width = strokeWidth * 2.5f,
                            cap = StrokeCap.Round
                        ),
                        alpha = 0.15f
                    )
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(barShape) 
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

                val hasNotification = when (screen) {
                    is Screen.Chat -> hasChatNotif
                    else -> false
                }

                val animatedWidth by animateDpAsState(
                    targetValue = if (isSelected) 100.dp else 52.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "nav_width"
                )

                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(48.dp) 
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            brush = if (isSelected) geminiBrush else SolidColor(Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(BentoDarkGlassBackground.copy(alpha = if (isSelected) 0.85f else 0.2f))
                        .shakeClick {
                            scope.launch {
                                val targetBase = screen.route.split("?").first().split("/").first()
                                val currentBase = currentRoute?.split("?")?.first()?.split("/")?.first()

                                if (targetBase == currentBase) {
                                    beViewModel?.cerrarBeAssistantCompleto()
                                    beViewModel?.clearFilters()
                                    return@launch
                                }

                                val newContext = when (targetBase) {
                                    "home" -> HUDContext.HOME
                                    "presupuestos" -> HUDContext.BUDGETS
                                    "chat" -> HUDContext.CHAT
                                    "calendar" -> HUDContext.CALENDAR
                                    "promo" -> HUDContext.PROMO
                                    else -> HUDContext.UNKNOWN
                                }
                                beViewModel?.setHUDContext(newContext)

                                val destination = if (screen is Screen.Chat) "chat" else screen.route
                                
                                navController.navigate(destination) {
                                    val startId = navController.graph.findStartDestination().id
                                    popUpTo(startId) {
                                        saveState = true 
                                    }
                                    launchSingleTop = true
                                    restoreState = true 
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        if (isSelected) {
                            Text(
                                text = getEmojiForScreen(screen),
                                fontSize = 24.sp 
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
                                modifier = Modifier.size(24.dp), 
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        if (hasNotification) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color(0xFF22D3EE), CircleShape) 
                                    .border(1.5.dp, Color.Black, CircleShape) 
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
            coordinator = null,
            showBe = true,
            isSearchActive = false,
            searchQuery = "",
            beMessages = BeDictionaryConversation.HomeMessages,
            beState = BeState.IDLE,
            currentTipIndex = 0,
            isDormido = false,
            showBeTools = false,
            activeConversationalMessage = null,
            requestKeyboard = false,
            currentActions = listOf(BeSmallActionModel("fast", Icons.Default.FlashOn, "Fast", emoji = "⚡", isDefault = true)),
            favorites = emptyList(),
            allCategories = listOf(
            ),
            isBottomBarVisible = true,
            resetBePositionTrigger = 0,
            isMultiSelectionActive = false,
            toolboxKey = "home_default",
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
            beViewModel = null,
            navHostContent = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Home Screen Content", color = Color.White)
                }
            }
        )
    }
}


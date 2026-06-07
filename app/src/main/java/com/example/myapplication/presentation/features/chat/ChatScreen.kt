package com.example.myapplication.presentation.features.chat

// === IMPORTS ===
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.presentation.features.budget.PresupuestoAdministradorSheet
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.MaverickTypography
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getAppColors
import com.example.myapplication.presentation.features.profile.UserViewModel
import com.example.myapplication.presentation.features.profile.UserUiState
import com.example.myapplication.core.ChatIdHelper
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.presentation.features.home.Screen
import com.example.myapplication.presentation.features.budget.BudgetViewModel
import com.example.myapplication.uishared.components.BudgetA4Viewer
import com.example.myapplication.presentation.components.PerfilEmpresa
import com.example.myapplication.core.domain.model.toEntity
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.utils.ImageUtils
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    initialBranchId: String? = null,   // 🔥 Contexto del Prestador
    initialCategoryId: String? = null,
    initialClientBranchId: String? = null, // 🔥 Contexto del Cliente (Mi Identidad)
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    chatListViewModel: ChatListViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    // --- SUSCRIPCIÓN A DATOS (SSOT) ---
    val chattingThreads by chatListViewModel.chattingThreads.collectAsStateWithLifecycle()
    val selectedPerfilId by chatListViewModel.selectedPerfilId.collectAsStateWithLifecycle()
    val profileState by userViewModel.uiState.collectAsStateWithLifecycle()
    val unreadCountsMap by chatListViewModel.unreadCountsMap.collectAsStateWithLifecycle()
    val allCategories by beBrainViewModel.allCategories.collectAsStateWithLifecycle()
    val shortcuts by chatListViewModel.shortcuts.collectAsStateWithLifecycle()
    val identityUnreadCounts by chatListViewModel.identityUnreadCounts.collectAsStateWithLifecycle()

    // 🔥 [FIX v8.7] Sincronización de Identidad de Nivel 1 (Corporativa)
    // Resolvemos el ID corporativo para que las pestañas de la UI no se rompan
    val effectiveSelectedPerfilId = remember(selectedPerfilId, initialClientBranchId, profileState.companies) {
        val resolvedId = when {
            initialClientBranchId == "{clientBranchId}" || initialClientBranchId.isNullOrBlank() -> "personal"
            else -> initialClientBranchId
        }

        if (resolvedId == "personal") selectedPerfilId
        else {
            // Si venimos de búsqueda con un branchId, debemos encontrar su empresa madre para la pestaña
            profileState.companies.find { it.branches.any { b -> b.id == resolvedId } }?.id ?: selectedPerfilId
        }
    }

    // --- ESTADOS DE UI ---
    val isMultiSelectMode by chatListViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedIds by chatListViewModel.selectedChatIds.collectAsStateWithLifecycle()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var chatToDeleteId by remember { mutableStateOf<String?>(null) }
    var showPresupuestosSheet by remember { mutableStateOf(false) }

    // --- ESTADO DE FILTROS ELEVADO (COORDINACIÓN DE COLAPSO) ---
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    // Estados para previsualización de presupuesto
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerForA4 by remember { mutableStateOf<Provider?>(null) }

    // --- FILTRADO DE PRESUPUESTOS DINÁMICO ---
    var showBudgetsForThisProviderOnly by remember { mutableStateOf(false) }
    var activeProviderId by remember { 
        mutableStateOf(if (initialProviderId == "{providerId}") null else initialProviderId)
    }
    var activeBranchId by remember { mutableStateOf(initialBranchId) }   // 🔥 [NUEVO]

    val budgetsForChatAdmin by budgetViewModel.filteredDirectBudgets.collectAsStateWithLifecycle()
    val filteredBudgetsForSheet = if (showBudgetsForThisProviderOnly && activeProviderId != null) {
        budgetsForChatAdmin.filter { it.providerId == activeProviderId }
    } else {
        budgetsForChatAdmin
    }

    // --- SINCRONIZACIÓN CON EL CEREBRO ---
    val beActionIds by chatListViewModel.beActionIds.collectAsStateWithLifecycle()

    // 🔥 [FIX v8.6] Sincronización Atómica de Identidad desde Navegación
    // Esto asegura que si venimos de un "Enviar como...", la pestaña correcta se active.
    LaunchedEffect(initialClientBranchId, profileState.companies) {
        val resolvedId = when {
            initialClientBranchId == "{clientBranchId}" || initialClientBranchId.isNullOrBlank() -> "personal"
            else -> initialClientBranchId
        }
        
        // Resolvemos el nivel corporativo (Empresa o Personal) para la pestaña
        val targetCorporateId = if (resolvedId == "personal") "personal" else {
             profileState.companies.find { it.branches.any { b -> b.id == resolvedId } }?.id ?: resolvedId
        }
        
        chatListViewModel.selectPerfil(targetCorporateId)
        if (resolvedId != "personal" && resolvedId != targetCorporateId) {
            chatListViewModel.selectBranch(resolvedId)
        }
    }

    LaunchedEffect(isMultiSelectMode, selectedIds, beActionIds) {
        beBrainViewModel.syncMultiSelection(isMultiSelectMode)
        beBrainViewModel.setCustomActionIds(beActionIds, HUDContext.CHAT)
    }

    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("chat")
        beBrainViewModel.actionEvent.collect { actionId ->
            chatListViewModel.onBeAction(
                actionId = actionId,
                onNavigateToBudgets = { 
                    showBudgetsForThisProviderOnly = false
                    showPresupuestosSheet = true 
                },
                onShowDeleteConfirm = { 
                    chatToDeleteId = null
                    showDeleteConfirmDialog = true 
                }
            )
        }
    }

    LaunchedEffect(budgetForA4Preview) {
        if (budgetForA4Preview != null) {
            providerForA4 = budgetViewModel.getProviderById(budgetForA4Preview!!.providerId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            //beBrainViewModel.syncMultiSelection(false, emptySet())
            beBrainViewModel.clearCustomActions(HUDContext.CHAT)
        }
    }

    val filterDropdownItems by chatListViewModel.filterDropdownItems.collectAsStateWithLifecycle()
    val sortDropdownItems by chatListViewModel.sortDropdownItems.collectAsStateWithLifecycle()
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    
    val isRefreshing by chatListViewModel.isRefreshing.collectAsStateWithLifecycle()
    
    // 🔥 [NUEVO v8.9] Gestión de Notificaciones de Refresh
    var refreshToastMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        chatListViewModel.refreshEvent.collect { message ->
            refreshToastMessage = message
            if (message.contains("✅") || message.contains("❌")) {
                kotlinx.coroutines.delay(2500)
                refreshToastMessage = null
            }
        }
    }

    ChatScreenContent(
        allThreadsMap = chattingThreads,
        profileState = profileState,
        unreadCountsMap = unreadCountsMap,
        identityUnreadCounts = identityUnreadCounts,
        allCategories = allCategories,
        shortcuts = shortcuts,
        onBack = onBack,
        initialProviderId = activeProviderId,
        initialBranchId = activeBranchId,
        initialCategoryId = initialCategoryId,
        initialClientBranchId = initialClientBranchId,
        navController = navController,
        onInConversationChange = { inConv ->
            onInConversationChange(inConv)
            if (!inConv) activeProviderId = null
        },
        beBrainViewModel = beBrainViewModel,
        chatListViewModel = chatListViewModel,
        selectedPerfilId = effectiveSelectedPerfilId, // 🔥 [FIX v6.1]
        isMultiSelectMode = isMultiSelectMode,
        selectedIds = selectedIds,
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onConfirmDelete = { 
            if (isMultiSelectMode) {
                chatListViewModel.deleteSelectedChats()
            } else {
                chatToDeleteId?.let { 
                    chatListViewModel.deleteChatById(it)
                }
            }
            showDeleteConfirmDialog = false
            chatToDeleteId = null
        },
        onShowDeleteConfirm = { id ->
            chatToDeleteId = id
            showDeleteConfirmDialog = true
        },
        showPresupuestosSheet = showPresupuestosSheet,
        onClosePresupuestosSheet = { 
            showPresupuestosSheet = false
            showBudgetsForThisProviderOnly = false
        },
        onOpenPresupuestosSheet = { forThisProviderOnly ->
            showBudgetsForThisProviderOnly = forThisProviderOnly
            showPresupuestosSheet = true
        },
        filterDropdownItems = filterDropdownItems,
        sortDropdownItems = sortDropdownItems,
        activeFilters = activeFilters,
        onFilterToggle = { chatListViewModel.toggleFilter(it) },
        budgetsForChatAdmin = filteredBudgetsForSheet,
        budgetForA4Preview = budgetForA4Preview,
        onOpenBudgetPreview = { budgetForA4Preview = it },
        onCloseBudgetPreview = { budgetForA4Preview = null },
        providerForA4 = providerForA4,
        onAcceptBudget = { budgetViewModel.acceptBudget(it) },
        onRejectBudget = { budgetViewModel.rejectBudget(it) },
        isFilterSheetOpen = isFilterSheetOpen,
        onFilterSheetVisibilityChange = { isFilterSheetOpen = it },
        isRefreshing = isRefreshing,
        onRefresh = { chatListViewModel.refreshAll() },
        refreshToastMessage = refreshToastMessage // 🔥 [NEW]
    )
}

@Composable
fun ChatScreenContent(
    allThreadsMap: Map<String, List<ChatThread>>,
    profileState: UserUiState,
    unreadCountsMap: Map<String, Int>,
    identityUnreadCounts: Map<String, Int> = emptyMap(),
    allCategories: List<CategoryEntity> = emptyList(),
    shortcuts: List<FilterSortItem> = emptyList(),
    onBack: () -> Unit,
    initialProviderId: String? = null,
    initialBranchId: String? = null, 
    initialCategoryId: String? = null,
    initialClientBranchId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    beBrainViewModel: BeBrainViewModel? = null,
    chatListViewModel: ChatListViewModel? = null,
    selectedPerfilId: String = "personal",
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    showDeleteConfirmDialog: Boolean = false,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onShowDeleteConfirm: (String?) -> Unit = {},
    showPresupuestosSheet: Boolean = false,
    onClosePresupuestosSheet: () -> Unit = {},
    onOpenPresupuestosSheet: (Boolean) -> Unit = {},
    filterDropdownItems: List<DropdownItemData> = emptyList(),
    sortDropdownItems: List<DropdownItemData> = emptyList(),
    activeFilters: Set<String> = emptySet(),
    onFilterToggle: (String) -> Unit = {},
    budgetsForChatAdmin: List<BudgetEntity> = emptyList(),
    budgetForA4Preview: BudgetEntity? = null,
    onOpenBudgetPreview: (BudgetEntity) -> Unit = {},
    onCloseBudgetPreview: () -> Unit = {},
    providerForA4: Provider? = null,
    onAcceptBudget: (BudgetEntity) -> Unit = {},
    onRejectBudget: (BudgetEntity) -> Unit = {},
    isFilterSheetOpen: Boolean = false,
    onFilterSheetVisibilityChange: (Boolean) -> Unit = {},
    isRefreshing: Boolean = false, 
    onRefresh: () -> Unit = {},
    refreshToastMessage: String? = null // 🔥 [NEW]
) {
    val appColors = getAppColors()
    
    // 🔥 [FIX v8.6] CONTEXTO DE CONVERSACIÓN (SIN REDUNDANCIA)
    var activeChatId by remember { mutableStateOf<String?>(null) }
    var activeProviderId by remember(initialProviderId) { 
        mutableStateOf(if (initialProviderId == "{providerId}" || initialProviderId.isNullOrBlank()) null else initialProviderId)
    }
    var activeBranchId by remember(initialBranchId) { 
        mutableStateOf(if (initialBranchId == "{branchId}" || initialBranchId.isNullOrBlank()) null else initialBranchId)
    }
    var activeLocalBranchId by remember(initialClientBranchId) { 
        mutableStateOf(if (initialClientBranchId == "{clientBranchId}" || initialClientBranchId.isNullOrBlank()) null else initialClientBranchId)
    }

    LaunchedEffect(activeProviderId) {
        onInConversationChange(activeProviderId != null)
    }

    BackHandler {
        if (activeProviderId != null) {
            activeProviderId = null
            activeChatId = null
        } else onBack()
    }

    val currentUserId = profileState.uid

    if (profileState.isLoading || profileState.uid.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = appColors.accentBlue)
        }
    } else {
        if (activeProviderId == null) {
            ChatListContent(
                threadsMap = allThreadsMap,
                profileState = profileState, 
                unreadCountsMap = unreadCountsMap,
                identityUnreadCounts = identityUnreadCounts,
                shortcuts = shortcuts,
                onChatClick = { thread -> 
                    activeChatId = thread.chatId
                    activeProviderId = thread.userId
                    activeBranchId = thread.otherBranchId
                    activeLocalBranchId = thread.branchId
                },
                onBack = onBack,
                selectedPerfilId = selectedPerfilId,
                navController = navController,
                beBrainViewModel = beBrainViewModel,
                chatListViewModel = chatListViewModel,
                isMultiSelectMode = isMultiSelectMode,
                selectedIds = selectedIds,
                isFilterSheetOpen = isFilterSheetOpen,
                onFilterSheetVisibilityChange = onFilterSheetVisibilityChange,
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                refreshToastMessage = refreshToastMessage
            )
        } else {
            // Resolución de ID: Prioridad al ID del hilo, fallback a generación simétrica
            val chatId = activeChatId ?: ChatIdHelper.generateChatId(
                uid1 = currentUserId, 
                uid2 = activeProviderId!!, 
                b1 = activeLocalBranchId ?: if (selectedPerfilId == "personal") null else selectedPerfilId,
                b2 = activeBranchId
            )
            
            val chatViewModel: ChatViewModel = hiltViewModel(key = chatId)
            val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
            val effectiveProvider = chatUiState.activeProvider

            LaunchedEffect(activeProviderId, activeBranchId, allCategories, selectedPerfilId, activeLocalBranchId) {
                if (activeProviderId != null) {
                    // Si no tenemos clientCompanyId, lo buscamos en el perfil
                    val currentIdentityId = activeLocalBranchId ?: if (selectedPerfilId == "personal") null else selectedPerfilId
                    val myCompanyId = profileState.companies.find { it.id == currentIdentityId || it.branches.any { b -> b.id == currentIdentityId } }?.id
                        ?: if (currentIdentityId != "personal") currentIdentityId else null

                    chatViewModel.initialize(
                        chatId = chatId,
                        branchId = activeBranchId,
                        clientCompanyId = myCompanyId,
                        clientBranchId = if (currentIdentityId == "personal") null else currentIdentityId,
                        categoryId = initialCategoryId,
                        initialProvider = null,
                        categories = allCategories
                    )
                }
            }
            
            if (effectiveProvider != null) {
                if (beBrainViewModel != null) {
                    ChatConversationScreen(
                        provider = effectiveProvider,
                        viewModel = chatViewModel,
                        chatId = chatId,
                        onBack = { activeProviderId = null },
                        appColors = appColors,
                        onNavigateToCalendar = { navController?.navigate(Screen.Calendar.route) },
                        onShowBudgets = { onOpenPresupuestosSheet(true) },
                        onShowCalendar = { navController?.navigate(Screen.Calendar.route) },
                        onShowSearch = { beBrainViewModel.setSearchActive(true) },
                        onDeleteChat = { onShowDeleteConfirm(chatId) },
                        onBlockProvider = { /* TODO */ },
                        onReportProvider = { /* TODO */ },
                        onToggleFavorite = { /* TODO */ },
                        isFavorite = false,
                        beBrainViewModel = beBrainViewModel,
                        ubicacionViewModel = hiltViewModel()
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = appColors.accentBlue)
                }
            }
        }

        PresupuestoAdministradorSheet(
            isVisible = showPresupuestosSheet,
            onClose = onClosePresupuestosSheet,
            count = budgetsForChatAdmin.size,
            title = "PRESUPUESTOS EN CHATS",
            helperText = "ADMINISTRADOR DE",
            tenderName = "HISTORIAL DE MENSAJES",
            budgets = budgetsForChatAdmin,
            onBudgetClick = { budget -> onOpenBudgetPreview(budget) }
        )

        if (budgetForA4Preview != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = onCloseBudgetPreview,
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BudgetA4Viewer(
                    prestador = providerForA4?.toEntity(),
                    budget = budgetForA4Preview,
                    onDismiss = onCloseBudgetPreview,
                    clientName = profileState.displayName
                ) { _, _ ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (budgetForA4Preview.status == com.example.myapplication.core.data.local.entity.BudgetStatus.PENDIENTE) {
                            OutlinedButton(
                                onClick = { onRejectBudget(budgetForA4Preview); onCloseBudgetPreview() },
                                modifier = Modifier.height(42.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("RECHAZAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onAcceptBudget(budgetForA4Preview); onCloseBudgetPreview() },
                                modifier = Modifier.height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
                            ) {
                                Text("ACEPTAR", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        } else {
                            com.example.myapplication.presentation.components.StatusPillPremium(budgetForA4Preview.status.name)
                        }
                    }
                }
            }
        }

        if (showDeleteConfirmDialog) {
            val text = if (isMultiSelectMode) {
                "¿Estás seguro de que deseas eliminar las ${selectedIds.size} conversaciones seleccionadas?"
            } else {
                "¿Estás seguro de que deseas eliminar esta conversación?"
            }
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                title = { Text("Eliminar chat") },
                text = { Text(text) },
                confirmButton = { TextButton(onClick = { onConfirmDelete(); activeProviderId = null }) { Text("Eliminar", color = Color.Red) } },
                dismissButton = { TextButton(onClick = onDismissDeleteDialog) { Text("Cancelar") } },
                containerColor = appColors.surfaceColor, 
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListContent(
    threadsMap: Map<String, List<ChatThread>>,
    profileState: UserUiState,
    unreadCountsMap: Map<String, Int>,
    identityUnreadCounts: Map<String, Int> = emptyMap(),
    shortcuts: List<FilterSortItem> = emptyList(),
    onChatClick: (ChatThread) -> Unit,
    onBack: () -> Unit,
    selectedPerfilId: String = "personal",
    navController: NavHostController? = null,
    beBrainViewModel: BeBrainViewModel? = null,
    chatListViewModel: ChatListViewModel? = null,
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    isFilterSheetOpen: Boolean = false,
    onFilterSheetVisibilityChange: (Boolean) -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    refreshToastMessage: String? = null
) {
    val filterDropdownItems by chatListViewModel?.filterDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val sortDropdownItems by chatListViewModel?.sortDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val activeFilters by beBrainViewModel?.activeFilters?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) }
    val activeSortCriteria by chatListViewModel?.activeSortCriteria?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val selectedBranchId by chatListViewModel?.selectedBranchId?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }
    
    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    // 🔥 [ELITE v9.0] PRE-PROCESAMIENTO DE IMÁGENES (Evita flood de HWUI)
    val companyPhotos = remember(profileState.companies) {
        profileState.companies.associate { it.id to ImageUtils.processImageSource(it.thumbnailBase64 ?: it.photoUrl) }
    }

    // --- GESTIÓN DE MULTI-IDENTIDAD NIVEL 1 (CORPORATIVO: PERFIL/EMPRESAS) ---
    val identities = remember(profileState.companies, profileState.displayName, identityUnreadCounts, companyPhotos) {
        val list = mutableListOf<PerfilEmpresa>()
        
        // 1. Identidad Personal (Chats personales)
        val personalUnread = identityUnreadCounts["personal"] ?: 0
        
        list.add(PerfilEmpresa(
            id = "personal",
            nombre = "Mi Perfil",
            iniciales = "YO",
            emoji = "👤",
            photoUrl = profileState.profileThumbnail.takeIf { it.isNotBlank() } ?: profileState.photoUrl.ifEmpty { null },
            colorAcento = MaverickColors.ElectricCyan,
            unreadCount = personalUnread 
        ))
        
        // 2. Identidades de Empresa (Agrupador Corporativo)
        profileState.companies.forEach { company ->
            val companyUnread = identityUnreadCounts[company.id] ?: 0

            list.add(PerfilEmpresa(
                id = company.id,
                nombre = company.name,
                iniciales = company.name.take(2).uppercase(),
                photoUrl = companyPhotos[company.id],
                colorAcento = MaverickColors.NeonCyan,
                unreadCount = companyUnread
            ))
        }
        list
    }

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
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 330f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return if (scrollAccumulator >= 330f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val cardsHideFraction = maxOf(scrollAccumulator / 80f, autoCollapseFraction).coerceIn(0f, 1f)
    val collapseFraction = maxOf((scrollAccumulator - 80f) / 250f, autoCollapseFraction).coerceIn(0f, 1f)

    Box(
        modifier = Modifier.fillMaxSize().background(MaverickColors.V2TechSurface).nestedScroll(nestedScrollConnection)
    ) {
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    val visuals = BeDictionary.Contexts["chat"]!!
                    BarraCabezera(
                        title = visuals.title,
                        subtitle = visuals.subtitle,
                        emoji = visuals.emoji,
                        onBack = onBack,
                        collapseFraction = collapseFraction,
                        accentColor = visuals.accentColor
                    )
                }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp * (1f - cardsHideFraction)))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .graphicsLayer { alpha = 1f - cardsHideFraction; translationY = -20.dp.toPx() * cardsHideFraction }
                                .height(106.dp * (1f - cardsHideFraction)),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MoldePremiumFilterCard(
                                label = "Filtrar por",
                                dropdownItems = filterDropdownItems,
                                shortcutItems = shortcuts,
                                activeFilters = activeFilters,
                                onToggle = { chatListViewModel?.toggleFilter(it) },
                                onManageShortcuts = { id, add -> chatListViewModel?.manageShortcut(id, add) },
                                modifier = Modifier.weight(1f),
                                isSheetVisible = isFilterSheetOpen,
                                onSheetVisibilityChange = onFilterSheetVisibilityChange
                            )

                            MoldePremiumSortCard(
                                label = "Ordenar por",
                                dropdownItems = sortDropdownItems,
                                shortcutItems = emptyList(),
                                activeSorts = activeSortCriteria,
                                onToggle = { chatListViewModel?.toggleFilter(it) },
                                onManageShortcuts = { _, _ -> }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp * (1f - cardsHideFraction)))
                    
                    // --- NIVEL 2: SELECTOR DE SUCURSALES (ETIQUETAS M3) ---
                    val activeCompany = profileState.companies.find { it.id == selectedPerfilId }
                    val groupedThreads by chatListViewModel?.groupedThreads?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyMap()) }
                    val totalItemCount = groupedThreads.values.sumOf { it.size }

                    ListaMoldeV2(
                        modifier = Modifier.weight(1f),
                        titulo = "BANDEJA DE ENTRADA",
                        subtitulo = identities.find { it.id == selectedPerfilId }?.nombre,
                        itemCount = totalItemCount,
                        perfiles = identities,
                        initialPerfilId = selectedPerfilId,
                        onPerfilSelected = { 
                            if (selectedPerfilId != it.id) {
                                chatListViewModel?.selectPerfil(it.id)
                                chatListViewModel?.selectBranch(null) // Reset branch al cambiar empresa
                            }
                        },
                        customMaxHeaderHeight = 64.dp,
                        acciones = { fraction ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                AnimatedVisibility(
                                    visible = activeFilters.isNotEmpty() || selectedBranchId != null,
                                    enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                                    exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
                                ) {
                                    BotonesCabecera.Limpiar(collapseFraction = fraction) {
                                        chatListViewModel?.toggleFilter("CLEAR_ALL")
                                        chatListViewModel?.selectBranch(null)
                                    }
                                }
                                
                                BotonesCabecera.Filtro(
                                    collapseFraction = fraction,
                                    isActive = activeFilters.isNotEmpty(),
                                    onClick = { onFilterSheetVisibilityChange(!isFilterSheetOpen) }
                                )
                            }
                        }
                    ) { perfil ->
                        // 🔥 [ELITE v8.8] ANIMATED CONTENT + SKELETON
                        item {
                            AnimatedContent(
                                targetState = isRefreshing && totalItemCount == 0,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith 
                                    fadeOut(animationSpec = tween(300)) using 
                                    SizeTransform(clip = false)
                                },
                                label = "list_content_transition"
                            ) { loading ->
                                if (loading) {
                                    Column {
                                        repeat(5) { ChatThreadSkeleton() }
                                    }
                                }
                            }
                        }

                        // 🔥 [NUEVO] Cabecera de Sucursales M3
                        if (activeCompany != null && activeCompany.branches.isNotEmpty()) {
                            item {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                        .animateContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    item {
                                        BranchSelectorChip(
                                            label = "TODOS",
                                            isSelected = selectedBranchId == null,
                                            onClick = { chatListViewModel?.selectBranch(null) }
                                        )
                                    }
                                    items(activeCompany.branches) { branch ->
                                        BranchSelectorChip(
                                            label = branch.name.uppercase(),
                                            isSelected = selectedBranchId == branch.id,
                                            onClick = { chatListViewModel?.selectBranch(branch.id) }
                                        )
                                    }
                                }
                            }
                        }

                        groupedThreads.forEach { (header, threads) ->
                            if (header.isNotEmpty()) {
                                item {
                                    BurbujaCabeceraLista(
                                        text = header,
                                        icon = MaverickIcons.Calendar,
                                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                                    )
                                }
                            }

                            items(threads, key = { it.chatId }) { thread ->
                                val threadId = thread.chatId
                                val isSelected = selectedIds.contains(threadId)
                                Box(modifier = Modifier.padding(horizontal = 8.dp).animateItem()) {
                                    UnifiedChatListItem(
                                        thread = thread,
                                        unreadCount = unreadCountsMap[threadId] ?: 0,
                                        isSelected = isSelected,
                                        isMultiSelectMode = isMultiSelectMode,
                                        onClick = { if (isMultiSelectMode) chatListViewModel?.toggleSelection(threadId) else onChatClick(thread) },
                                        onLongClick = { if (!isMultiSelectMode) chatListViewModel?.updateMultiSelection(true); chatListViewModel?.toggleSelection(threadId) },
                                        onAvatarClick = { navController?.navigate("perfil_prestador/${thread.userId}") }
                                    )
                                }
                            }
                        }
                        
                        if (totalItemCount == 0 && !isRefreshing) {
                            item {
                                if (activeFilters.isNotEmpty()) {
                                    EmptyFiltersState(
                                        activeFilters = activeFilters,
                                        filterDropdownItems = filterDropdownItems,
                                        sortDropdownItems = sortDropdownItems,
                                        onClearFilters = { beBrainViewModel?.clearFilters() }
                                    )
                                } else {
                                    EmptyChatPlaceholder(
                                        message = if (perfil?.id == "personal") "No tienes chats personales" 
                                                 else "No hay chats para ${perfil?.nombre ?: "esta empresa"}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔥 [NUEVO v8.9] MAVERICK REFRESH TOAST (Google Elite Style)
        AnimatedVisibility(
            visible = refreshToastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp).zIndex(10f)
        ) {
            refreshToastMessage?.let { msg ->
                Surface(
                    color = MaverickColors.V2TechSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaverickColors.NeonCyan.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (msg.contains("...")) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaverickColors.NeonCyan
                            )
                        }
                        Text(
                            text = msg.uppercase(),
                            style = MaverickTypography.HeaderSubtitle.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BranchSelectorChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaverickColors.NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaverickColors.NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) MaverickColors.NeonCyan else Color.Gray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun EmptyChatPlaceholder(message: String = "No tienes conversaciones activas") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp), 
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = MaverickIcons.Message, 
                        contentDescription = null, 
                        tint = Color.Gray.copy(alpha = 0.3f), 
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tus chats con prestadores aparecerán aquí.", 
                color = Color.Gray.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val sampleProfileState = UserUiState(
        uid = "user_demo_66",
        displayName = "Demo User",
        isLoading = false
    )
    val sampleUnreadCounts = mapOf("c1" to 3)

    MyApplicationTheme {
        ChatScreenContent(
            allThreadsMap = emptyMap(),
            profileState = sampleProfileState,
            unreadCountsMap = sampleUnreadCounts,
            onBack = {}
        )
    }
}

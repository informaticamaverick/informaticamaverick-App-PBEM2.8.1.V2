package com.example.myapplication.presentation.features.chat

// === IMPORTS ===
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.presentation.components.BarraCabezera
import com.example.myapplication.presentation.components.ListaMoldeV2
import com.example.myapplication.presentation.components.BotonCabeceraAccion
import com.example.myapplication.presentation.components.UnifiedChatListItem
import com.example.myapplication.presentation.components.EmptyFiltersState
import com.example.myapplication.presentation.components.MoldePremiumFilterCard
import com.example.myapplication.presentation.components.MoldePremiumSortCard
import com.example.myapplication.presentation.components.DropdownItemData
import com.example.myapplication.presentation.components.FilterSortItem
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
import com.example.myapplication.core.domain.model.toEntity
import com.example.myapplication.core.domain.model.Provider
import androidx.compose.ui.tooling.preview.Preview
//import com.example.myapplication.presentation.features.budget.BudgetFilterSortItem

// ==================================================================================
// --- SECCIÓN 1: CONSTANTES Y ESTILOS ---
// ==================================================================================

// ==================================================================================
// --- SECCIÓN 2: ORQUESTADOR DE PANTALLA ---
// ==================================================================================

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    initialCompanyId: String? = null,
    initialCategoryId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    chatListViewModel: ChatListViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    // --- SUSCRIPCIÓN A DATOS (SSOT) ---
    val chattingThreads by chatListViewModel.chattingThreads.collectAsStateWithLifecycle()
    val profileState by userViewModel.uiState.collectAsStateWithLifecycle()
    // Obtenemos los conteos de forma reactiva a través del ViewModel
    val unreadCountsMap by chatListViewModel.unreadCountsMap.collectAsStateWithLifecycle()
    val allCategories by beBrainViewModel.allCategories.collectAsStateWithLifecycle()
    val shortcuts by chatListViewModel.shortcuts.collectAsStateWithLifecycle()

    // --- ESTADOS DE UI ---
    val isMultiSelectMode by chatListViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedIds by chatListViewModel.selectedChatIds.collectAsStateWithLifecycle()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPresupuestosSheet by remember { mutableStateOf(false) }

    // --- ESTADO DE FILTROS ELEVADO (COORDINACIÓN DE COLAPSO) ---
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    // Estados para previsualización de presupuesto
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerForA4 by remember { mutableStateOf<Provider?>(null) }

    // --- SINCRONIZACIÓN CON EL CEREBRO ---
    val beActionIds by chatListViewModel.beActionIds.collectAsStateWithLifecycle()

    LaunchedEffect(isMultiSelectMode, selectedIds, beActionIds) {
        beBrainViewModel.syncMultiSelection(isMultiSelectMode)
        beBrainViewModel.setCustomActionIds(beActionIds, HUDContext.CHAT)
    }

    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("chat")
        beBrainViewModel.actionEvent.collect { actionId ->
            chatListViewModel.onBeAction(
                actionId = actionId,
                onNavigateToBudgets = { showPresupuestosSheet = true },
                onShowDeleteConfirm = { showDeleteConfirmDialog = true }
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
            beBrainViewModel.syncMultiSelection(false, emptySet())
            beBrainViewModel.clearCustomActions(HUDContext.CHAT)
        }
    }

    val filterDropdownItems by chatListViewModel.filterDropdownItems.collectAsStateWithLifecycle()
    val sortDropdownItems by chatListViewModel.sortDropdownItems.collectAsStateWithLifecycle()
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    
    // Suponiendo que tenemos una forma de obtener presupuestos globales para el administrador
    // Por ahora usamos una lista vacía o placeholder si el VM no lo tiene implementado para chat.
    val budgetsForChatAdmin by budgetViewModel.filteredDirectBudgets.collectAsStateWithLifecycle()

    ChatScreenContent(
        allThreads = chattingThreads,
        profileState = profileState,
        unreadCountsMap = unreadCountsMap,
        allCategories = allCategories,
        shortcuts = shortcuts,
        onBack = onBack,
        initialProviderId = initialProviderId,
        initialCompanyId = initialCompanyId,
        initialCategoryId = initialCategoryId,
        navController = navController,
        onInConversationChange = onInConversationChange,
        beBrainViewModel = beBrainViewModel,
        chatListViewModel = chatListViewModel,
        isMultiSelectMode = isMultiSelectMode,
        selectedIds = selectedIds,
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onConfirmDelete = { 
            chatListViewModel.deleteSelectedChats()
            showDeleteConfirmDialog = false
        },
        showPresupuestosSheet = showPresupuestosSheet,
        onClosePresupuestosSheet = { showPresupuestosSheet = false },
        filterDropdownItems = filterDropdownItems,
        sortDropdownItems = sortDropdownItems,
        activeFilters = activeFilters,
        onFilterToggle = { chatListViewModel.toggleFilter(it) },
        budgetsForChatAdmin = budgetsForChatAdmin,
        budgetForA4Preview = budgetForA4Preview,
        onOpenBudgetPreview = { budgetForA4Preview = it },
        onCloseBudgetPreview = { budgetForA4Preview = null },
        providerForA4 = providerForA4,
        onAcceptBudget = { budgetViewModel.acceptBudget(it) },
        onRejectBudget = { budgetViewModel.rejectBudget(it) },
        isFilterSheetOpen = isFilterSheetOpen,
        onFilterSheetVisibilityChange = { isFilterSheetOpen = it }
    )
}

// ==================================================================================
// --- SECCIÓN 3: CONTENIDO DE PANTALLA ---
// ==================================================================================


@Composable
fun ChatScreenContent(
    allThreads: List<ChatThread>,
    profileState: UserUiState,
    unreadCountsMap: Map<String, Int>,
    allCategories: List<CategoryEntity> = emptyList(),
    shortcuts: List<FilterSortItem> = emptyList(),
    onBack: () -> Unit,
    initialProviderId: String? = null,
    initialCompanyId: String? = null,
    initialCategoryId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    beBrainViewModel: BeBrainViewModel? = null,
    chatListViewModel: ChatListViewModel? = null,
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    showDeleteConfirmDialog: Boolean = false,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    showPresupuestosSheet: Boolean = false,
    onClosePresupuestosSheet: () -> Unit = {},
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
    onFilterSheetVisibilityChange: (Boolean) -> Unit = {}
) {
    val appColors = getAppColors()
    
    var activeProviderId by remember { 
        mutableStateOf(if (initialProviderId == "{providerId}") null else initialProviderId)
    }

   // val fallbackProvider by if (activeProviderId != null && allThreads.none { it.provider.uid == activeProviderId }) {
        //chatListViewModel?.getProviderById(activeProviderId!!)?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    //} else {
     //   remember { mutableStateOf(null) }
    //}

    LaunchedEffect(activeProviderId) {
        onInConversationChange(activeProviderId != null)
    }

    BackHandler {
        if (activeProviderId != null) activeProviderId = null
        else onBack()
    }

    val currentUserId = profileState.uid

    if (profileState.isLoading || profileState.uid.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = appColors.accentBlue)
        }
    } else {
        if (activeProviderId == null) {
            ChatListContent(
                threadsList = allThreads,
                unreadCountsMap = unreadCountsMap,
                shortcuts = shortcuts,
                onChatClick = { thread -> activeProviderId = thread.provider.uid },
                onBack = onBack,
                navController = navController,
                beBrainViewModel = beBrainViewModel,
                chatListViewModel = chatListViewModel,
                isMultiSelectMode = isMultiSelectMode,
                selectedIds = selectedIds,
                isFilterSheetOpen = isFilterSheetOpen,
                onFilterSheetVisibilityChange = onFilterSheetVisibilityChange
            )
        } else {
            // Resolver el proveedor (desde la lista local o fetch remoto vía ChatViewModel)
            val chatId = ChatIdHelper.generateChatId(currentUserId, activeProviderId!!)
            val chatViewModel: ChatViewModel = hiltViewModel(key = chatId)
            val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()

            val providerFromList = allThreads.find { it.provider.uid == activeProviderId }?.provider
            val effectiveProvider = providerFromList ?: chatUiState.activeProvider

            LaunchedEffect(activeProviderId, allCategories) {
                if (activeProviderId != null) {
                    val chatId = ChatIdHelper.generateChatId(currentUserId, activeProviderId!!)
                    chatViewModel.initialize(
                        chatId = chatId,
                        companyId = initialCompanyId,
                        categoryId = initialCategoryId,
                        initialProvider = providerFromList, 
                        categories = allCategories
                    )
                }
            }
            
            if (effectiveProvider != null) {
                if (beBrainViewModel != null) {
                    ChatConversationScreen(
                        provider = effectiveProvider,
                        viewModel = chatViewModel,
                        onBack = { activeProviderId = null },
                        appColors = appColors,
                        onNavigateToCalendar = { navController?.navigate(Screen.Calendar.route) },
                        beBrainViewModel = beBrainViewModel,
                        ubicacionViewModel = hiltViewModel()
                    )
                }
            } else {
                // Estado de carga del perfil del prestador
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
          //  filterOptions = filterDropdownItems.map {
          //      BudgetFilterSortItem(
          //          it.id,
          //          it.label,
          //          it.emoji ?: "🔹"
          //      )
            //},
           // sortOptions = sortDropdownItems.map { BudgetFilterSortItem(it.id, it.label, it.emoji ?: "🔹") },
           // selectedFilter = activeFilters.find { !it.startsWith("sort_") },
           // selectedSort = activeFilters.find { it.startsWith("sort_") },
           // onFilterSelect = onFilterToggle,
           // onSortSelect = onFilterToggle,
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
                    clientName = profileState.displayName ?: "Cliente"
                ) { _, _ ->
                    // Acciones del Cliente: Aceptar / Rechazar
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
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                title = { Text("Eliminar chats") },
                text = { Text("¿Estás seguro de que deseas eliminar las ${selectedIds.size} conversaciones seleccionadas?") },
                confirmButton = { TextButton(onClick = onConfirmDelete) { Text("Eliminar", color = Color.Red) } },
                dismissButton = { TextButton(onClick = onDismissDeleteDialog) { Text("Cancelar") } },
                containerColor = appColors.surfaceColor, 
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

// ==================================================================================
// --- SECCIÓN 4: LISTA DE CHATS (MODO ELITE) ---
// ==================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListContent(
    threadsList: List<ChatThread>,
    unreadCountsMap: Map<String, Int>,
    shortcuts: List<FilterSortItem> = emptyList(),
    onChatClick: (ChatThread) -> Unit,
    onBack: () -> Unit,
    navController: NavHostController? = null,
    beBrainViewModel: BeBrainViewModel? = null,
    chatListViewModel: ChatListViewModel? = null,
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    isFilterSheetOpen: Boolean = false,
    onFilterSheetVisibilityChange: (Boolean) -> Unit = {}
) {
   // val refreshState = rememberPullToRefreshState()
   // val scope = rememberCoroutineScope()
   // var isRefreshing by remember { mutableStateOf(false) }

    // --- SUSCRIPCIÓN A ESTADOS DEL VM (Elite) ---
    val filterDropdownItems by chatListViewModel?.filterDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val sortDropdownItems by chatListViewModel?.sortDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val activeFilters by beBrainViewModel?.activeFilters?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) }

    // Estado de expansión de grupos
    // Removed grouping logic for now

    val activeSortCriteria by chatListViewModel?.activeSortCriteria?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    
    // --- LÓGICA DE SCROLL (Fase 1 + 2) ---
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    // 🔥 [ELITE] Lógica de Colapso Automático por Menú de Filtros
    val autoCollapseFraction by animateFloatAsState(
        targetValue = if (isFilterSheetOpen) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "autoCollapse"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isFilterSheetOpen) return Offset.Zero // 🔥 Bloquear scroll manual si el menú está abierto
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
                //Spacer(modifier = Modifier.height(16.dp))
                ListaMoldeV2(
                    modifier = Modifier.weight(1f),
                    titulo = "BANDEJA DE ENTRADA",
                    compactInfo = "${threadsList.size} CONVERSACIONES",
                    itemCount = threadsList.size,
                    customMaxHeaderHeight = 50.dp,
                    acciones = {
                        if (activeFilters.isNotEmpty()) {
                            BotonCabeceraAccion(
                                onClick = { chatListViewModel?.toggleFilter("CLEAR_ALL") },
                                icon = MaverickIcons.Filter,
                                color = MaverickColors.MagentaNeon
                            )
                        }
                    }
                ) {
                    items(threadsList, key = { it.chatId }) { thread ->
                        val threadId = thread.chatId
                        val isSelected = selectedIds.contains(threadId)
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            UnifiedChatListItem(
                                thread = thread,
                                unreadCount = unreadCountsMap[threadId] ?: 0,
                                isSelected = isSelected,
                                isMultiSelectMode = isMultiSelectMode,
                                onClick = { if (isMultiSelectMode) chatListViewModel?.toggleSelection(threadId) else onChatClick(thread) },
                                onLongClick = { if (!isMultiSelectMode) chatListViewModel?.updateMultiSelection(true); chatListViewModel?.toggleSelection(threadId) },
                                onAvatarClick = { navController?.navigate("perfil_prestador/${thread.provider.uid}") }
                            )
                        }
                    }
                    
                    if (threadsList.isEmpty()) {
                        item {
                            if (activeFilters.isNotEmpty()) {
                                EmptyFiltersState(
                                    activeFilters = activeFilters,
                                    filterDropdownItems = filterDropdownItems,
                                    sortDropdownItems = sortDropdownItems,
                                    onClearFilters = { beBrainViewModel?.clearFilters() }
                                )
                            } else {
                                EmptyChatPlaceholder()
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN 5: OTROS COMPONENTES ---
// ==================================================================================

@Composable
fun EmptyChatPlaceholder() {
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
                text = "No tienes conversaciones activas", 
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
            allThreads = emptyList(),
            profileState = sampleProfileState,
            unreadCountsMap = sampleUnreadCounts,
            onBack = {}
        )
    }
}

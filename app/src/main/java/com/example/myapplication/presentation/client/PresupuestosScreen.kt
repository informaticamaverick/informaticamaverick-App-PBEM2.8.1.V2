package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.firestore.FirebaseFirestore

import com.example.myapplication.presentation.client.CategoryVisuals
// =================================================================================
// --- CONSTANTES DE DISEÑO MACO ---
// =================================================================================
private val DarkBackground = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusFinished = Color(0xFF34D399)

enum class BudgetTabMode {
    LICITACIONES, DIRECTOS
}

@Composable
fun PresupuestosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(), 
    beInteractionViewModel: BeInteractionViewModel = hiltViewModel(),
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    // 🔥 ESPECIALISTA (BudgetViewModel) es ahora el dueño de los datos filtrados
    val tenders by viewModel.filteredTenders.collectAsStateWithLifecycle()
    val directBudgets by viewModel.filteredDirectBudgets.collectAsStateWithLifecycle()
    val overlayBudgets by viewModel.filteredOverlayBudgets.collectAsStateWithLifecycle()
    val hasMatches by viewModel.hasMatches.collectAsStateWithLifecycle()

    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    val dynamicCategories by beBrainViewModel.dynamicCategories.collectAsStateWithLifecycle()
    val availableFilters by beBrainViewModel.availableFilters.collectAsStateWithLifecycle()
    val availableSortOptions by beBrainViewModel.availableSortOptions.collectAsStateWithLifecycle()

    // 🔥 Delegación de Estados de UI
    val isSearchActive by beBrainViewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()
    val isMultiSelectionActive by viewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    // 🔥 Acciones Inyectadas y Datos Raw para Be
    val budgetActions by viewModel.beActions.collectAsStateWithLifecycle()
    val allTenders by viewModel.allTenders.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()

    // Estados locales para manejo de UI condicional (Dialogs)
    var tenderForAnalytics by remember { mutableStateOf<Pair<TenderEntity, List<BudgetEntity>>?>(null) }
    var tenderForDetails by remember { mutableStateOf<TenderEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteContextMessage by remember { mutableStateOf("") }
    var onConfirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // ==========================================================
    // --- SECCIÓN: SINCRONIZACIÓN DE BÚSQUEDA Y ASISTENTE (Be) ---
    // ==========================================================
    LaunchedEffect(searchQuery, hasMatches) {
        viewModel.setSearchQuery(searchQuery)
        // Informamos al lóbulo frontal (InteractionViewModel) sobre el estado de la búsqueda
        // para que pueda disparar respuestas conversacionales si no hay resultados.
        beInteractionViewModel.processSearchQuery(searchQuery, hasMatches)
    }

    // 🔥 Sincronización de Filtros (Fase 3)
    LaunchedEffect(activeFilters) {
        viewModel.setFilters(activeFilters)
    }

    // 🔥 Sincronización de Datos Raw para Be (Fase 3 - Necesario para filtros dinámicos)
    LaunchedEffect(allTenders, allBudgets, categories) {
        beBrainViewModel.updateTenders(allTenders)
        beBrainViewModel.updateBudgets(allBudgets)
        beBrainViewModel.hydrateCategories(categories)
    }

    // 🔥 Sincronización de Multiselección (Fase 3 - Habilita herramientas de Be)
    LaunchedEffect(isMultiSelectionActive, selectedItemIds) {
        beBrainViewModel.syncMultiSelection(isMultiSelectionActive, selectedItemIds)
    }

    // Inyectar Acciones en Be (Fase 3)
    // Se "hidratan" las acciones con triggerAction para que BeBrain emita los eventos
    LaunchedEffect(budgetActions) {
        val hydratedActions = budgetActions.map { action ->
            action.copy(onClick = { beBrainViewModel.triggerAction(action.id) })
        }
        beBrainViewModel.setCustomActions(hydratedActions)
    }

    // Capturar Eventos y Delegar (Fase 3)
    val hudContext by viewModel.currentHUDContext.collectAsStateWithLifecycle()
    val currentSelectedIds by rememberUpdatedState(selectedItemIds)

    // Calculamos qué IDs están actualmente visibles para la acción "Seleccionar Todo"
    val currentVisibleIds = remember(hudContext, tenders, directBudgets, overlayBudgets) {
        when (hudContext) {
            HUDContext.BUDGETS_TENDERS -> tenders.map { it.tenderId }
            HUDContext.TENDER_DETAILS -> overlayBudgets.map { it.budgetId }
            else -> directBudgets.map { it.budgetId }
        }
    }
    val currentIdsToSelect by rememberUpdatedState(currentVisibleIds)

    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "select_all" -> viewModel.selectAll(currentIdsToSelect)
                "mark_as_read" -> viewModel.markAsRead(currentSelectedIds)
                "compare_selected" -> {
                    val selectedBudgets = allBudgets.filter { it.budgetId in currentSelectedIds }
                    if (selectedBudgets.isNotEmpty()) {
                        val dummyTender = TenderEntity("direct_comparison", "Comparativa de Presupuestos", "", "", "General")
                        tenderForAnalytics = dummyTender to selectedBudgets
                    }
                }
                "view_tender_details" -> {
                    val tenderId = currentSelectedIds.firstOrNull()
                    tenderForDetails = tenders.find { it.tenderId == tenderId }
                    viewModel.updateMultiSelection(false)
                }
                "delete_multi" -> {
                    deleteContextMessage = if (hudContext == HUDContext.BUDGETS_TENDERS) {
                        "¿Deseas eliminar las licitaciones seleccionadas? Esta acción no se puede deshacer."
                    } else {
                        "¿Deseas eliminar los presupuestos seleccionados? Esta acción no se puede deshacer."
                    }
                    onConfirmDeleteAction = {
                        if (hudContext == HUDContext.BUDGETS_TENDERS) {
                            viewModel.deleteTenders(currentSelectedIds)
                        } else {
                            viewModel.deleteBudgets(currentSelectedIds)
                        }
                    }
                    showDeleteConfirmDialog = true
                }
                "cancel" -> viewModel.updateMultiSelection(false)
            }
        }
    }

    // 🔥 NUEVO: Limpieza al destruir la pantalla
    DisposableEffect(Unit) {
        onDispose {
            // Avisamos al cerebro que esta pantalla ya no controla las acciones
            beBrainViewModel.setCustomActions(emptyList())
            beBrainViewModel.syncMultiSelection(false, emptySet())
        }
    }

    PresupuestosScreenContent(
        tenders = tenders,
        directBudgets = directBudgets,
        categories = categories,
        activeFilters = activeFilters,
        dynamicCategories = dynamicCategories,
        refinementFilters = availableFilters,
        sortOptions = availableSortOptions,
        onFilterToggle = { beBrainViewModel.toggleFilter(it) },
        onClearFilters = { beBrainViewModel.clearSpecificFilters(listOf("filter_", "cat_")) },
        onClearSort = { beBrainViewModel.clearSpecificFilters(listOf("sort_", "view_")) },
        onSetContext = {
            beBrainViewModel.setHUDContext(it)
            viewModel.setContext(it)
        },
        getBudgetsForTender = { tenderId -> viewModel.getFilteredBudgetsForTender(tenderId) },
        onChatClick = onChatClick,
        onBack = onBack,
        onAcceptBudget = { budget -> viewModel.acceptBudget(budget) },
        onClearSelect = { viewModel.updateMultiSelection(false) },
        onRejectBudget = { budget -> viewModel.rejectBudget(budget) },
        onDeleteTenders = { ids -> viewModel.deleteTenders(ids) },
        onDeleteBudgets = { ids -> viewModel.deleteBudgets(ids) },
        onMarkAsRead = { id -> viewModel.markAsRead(setOf(id)) },
        bottomPadding = bottomPadding,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        onCloseBeAssistant = { beBrainViewModel.cerrarBeAssistantCompleto() },
        isMultiSelectionActive = isMultiSelectionActive,
        selectedItemIds = selectedItemIds,
        onToggleItemSelection = { viewModel.toggleSelection(it) },
        onSelectAllItems = { ids -> viewModel.selectAll(ids) },
        onToggleMultiSelection = { viewModel.updateMultiSelection(!isMultiSelectionActive) },
        beBrainActionEvent = beBrainViewModel.actionEvent,
        onBeLongClick = { beBrainViewModel.onBeLongClick() },
        tenderForAnalytics = tenderForAnalytics,
        tenderForDetails = tenderForDetails,
        onCloseAnalytics = { tenderForAnalytics = null },
        onCloseTenderDetails = { tenderForDetails = null },
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        deleteContextMessage = deleteContextMessage,
        onConfirmDeleteAction = onConfirmDeleteAction,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onRequestDeleteConfirmation = { message, action ->
            deleteContextMessage = message
            onConfirmDeleteAction = action
            showDeleteConfirmDialog = true
        },
        onAnalyticsRequest = { tender, budgets -> tenderForAnalytics = tender to budgets },
        onUpdateTenderStatus = { tenderId, newStatus -> viewModel.updateTenderStatus(tenderId, newStatus) },
        onTenderSelected = { viewModel.setSelectedTenderId(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
    directBudgets: List<BudgetEntity>,
    categories: List<CategoryEntity>,
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onSetContext: (HUDContext) -> Unit,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    onDeleteTenders: (Set<String>) -> Unit = {},
    onDeleteBudgets: (Set<String>) -> Unit = {},
    onMarkAsRead: (String) -> Unit = {},
    onClearSelect: () -> Unit = {},
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onCloseBeAssistant: () -> Unit = {},
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onSelectAllItems: (List<String>) -> Unit = {},
    bottomPadding: PaddingValues,
    beBrainActionEvent: SharedFlow<String>,
    onToggleMultiSelection: () -> Unit = {},
    onBeLongClick: () -> Unit = {},
    tenderForAnalytics: Pair<TenderEntity, List<BudgetEntity>>?,
    tenderForDetails: TenderEntity?,
    onCloseAnalytics: () -> Unit,
    onCloseTenderDetails: () -> Unit,
    showDeleteConfirmDialog: Boolean,
    deleteContextMessage: String,
    onConfirmDeleteAction: (() -> Unit)?,
    onDismissDeleteDialog: () -> Unit,
    onRequestDeleteConfirmation: (String, () -> Unit) -> Unit,
    onAnalyticsRequest: (TenderEntity, List<BudgetEntity>) -> Unit,
    onUpdateTenderStatus: (String, String) -> Unit,
    onTenderSelected: (String?) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(BudgetTabMode.LICITACIONES) }
    
    val tenderListState = rememberLazyListState()
    val directListState = rememberLazyGridState()

    // Volver arriba cuando cambian los datos o filtros
    LaunchedEffect(tenders) {
        if (tenders.isNotEmpty()) tenderListState.animateScrollToItem(0)
    }
    LaunchedEffect(directBudgets) {
        if (directBudgets.isNotEmpty()) directListState.animateScrollToItem(0)
    }

    LaunchedEffect(pagerState.currentPage) {
        currentTab = if (pagerState.currentPage == 0) BudgetTabMode.LICITACIONES else BudgetTabMode.DIRECTOS
        onSetContext(if (currentTab == BudgetTabMode.LICITACIONES) HUDContext.BUDGETS_TENDERS else HUDContext.BUDGETS_DIRECT)
    }

    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }

    MaverickBackgroundStrix {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Dejamos el topBar vacío para manejar la cabecera dentro del flujo del Column
                // Esto asegura que el Spacer y el MoldeBarraMenu se posicionen correctamente debajo.
            }
        ) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())) {
                
                // ==========================================================
                // --- SECCIÓN: CABECERA DINÁMICA CON NAVEGACIÓN TÉCNICA ---
                // ==========================================================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize() // Animamos el cambio de altura suavemente
                ) {
                    Column {
                        // La cabecera refleja el estado activo
                        BarraCabezera(
                            title = if (currentTab == BudgetTabMode.LICITACIONES) "LICITACIONES" else "PRESUPUESTOS",
                            subtitle = if (currentTab == BudgetTabMode.LICITACIONES) "Gestión de Concursos" else "Solicitudes Directas",
                            emoji = if (currentTab == BudgetTabMode.LICITACIONES) "⚖️" else "📩",
                            onBack = onBack,
                            onInfoClick = { /* Acción de información */ },
                            accentColor = MaverickBlue
                        )
                    }

                    // BURBUJA DE NAVEGACIÓN (Estilo Perfil + Indicadores Técnicos)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .statusBarsPadding()
                            .padding(bottom = 28.dp, end = 105.dp) // Posicionamiento quirúrgico
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) + 
                                 scaleIn(initialScale = 0.8f, animationSpec = tween(300))).togetherWith(
                                 fadeOut(animationSpec = tween(200)) + 
                                 scaleOut(targetScale = 0.8f, animationSpec = tween(200))
                                )
                            },
                            label = "BubbleNavAnim"
                        ) { targetTab ->
                            BudgetBubbleTab(
                                icon = if (targetTab == BudgetTabMode.LICITACIONES) "📩" else "⚖️",
                                isToRight = targetTab == BudgetTabMode.LICITACIONES,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(if (targetTab == BudgetTabMode.LICITACIONES) 1 else 0)
                                    }
                                }
                            )
                        }
                    }
                }

                // --- ESPACIADOR DE AIRE ---
                // Ahora sí separa la cabecera de la barra de menú
                Spacer(modifier = Modifier.height(18.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    // --- INTEGRACIÓN DE MoldeBarraMenu PARA FILTROS Y ESTADÍSTICAS ---
                    MoldeBarraMenu(
                        itemCount = if (page == 0) tenders.size else directBudgets.size,
                        labelCountMain = if (page == 0) "LICITACIONES" else "DIRECTOS",
                        labelCountSub = "Encontramos estos",
                        showSubscribedOnly = false, // No aplica directamente aquí pero mantenemos interfaz
                        onToggleSubscribed = { /* No implementado en este contexto */ },
                        sortByProximity = activeFilters.any { it.startsWith("sort_") },
                        onToggleProximity = { onClearSort() },
                        isBentoView = false,
                        onToggleView = { /* Vista fija en esta pantalla */ },
                        activeRefinements = activeFilters,
                        refinementOptions = refinementFilters,
                        sortOptions = sortOptions, // PASAMOS LAS OPCIONES DE ORDENAMIENTO
                        onToggleRefinement = onFilterToggle,
                        onClearRefinements = onClearFilters,
                        onClearSort = onClearSort,
                        showSuscritos = false, // Ocultar filtros irrelevantes en Presupuestos
                        showCercania = false,  // Ocultar filtros irrelevantes en Presupuestos
                        showVista = false,     // Ocultar filtros irrelevantes en Presupuestos
                        modifier = Modifier.fillMaxSize(),
                        // ==========================================================
                        // --- SECCIÓN: ACCIONES PERSONALIZADAS (SIN ENGRANAJE) ---
                        // --- Se quita el engranaje y se exponen los botones a la izquierda del filtro ---
                        // ==========================================================
                        customActions = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                // --- BOTONES DE ORDENAMIENTO (EXTRAÍDOS DEL POPUP) ---
                                sortOptions.forEach { item ->
                                    val isSelected = activeFilters.contains(item.id)
                                    // Implementación táctica con forma personalizada (CutCorner)
                                    MaverickTacticalButton(
                                        isActive = isSelected,
                                        accentColor = item.color,
                                        onClick = { onFilterToggle(item.id) }
                                    ) {
                                        Text(text = item.emoji, fontSize = 16.sp)
                                    }
                                    Spacer(Modifier.width(6.dp))
                                }

                                if (sortOptions.isNotEmpty()) {
                                    M3VerticalDivider(
                                        modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    )
                                }

                                // --- BOTÓN DE FILTRO (MANTENIDO) ---
                                MenuFiltros(
                                    activeFilters = activeFilters,
                                    dynamicCategories = emptyList(),
                                    refinementFilters = refinementFilters,
                                    onAction = onFilterToggle,
                                    onApply = {},
                                    onClearFilters = onClearFilters
                                )
                            }
                        }
                    ) {
                        // CONTENIDO DEL PANEL (Lista o Grilla según la página)
                        if (page == 0) {
                            LazyColumn(
                                state = tenderListState,
                                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp)
                            ) {
                                items(tenders, key = { it.tenderId }) { tender ->
                                    val budgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
                                    val budgets by budgetsFlow.collectAsStateWithLifecycle(emptyList())
                                    val unreadCount = budgets.count { !it.isRead }
                                    val isSelected = selectedItemIds.contains(tender.tenderId)
                                    val categoryInfo = categories.find { it.name.equals(tender.category, ignoreCase = true) }

                                    // --- ENRIQUECIMIENTO TÁCTICO: Color de Supercategoría y Foto de Adjudicado ---
                                    val superColor = Color(CategoryVisuals.getColorFor(categoryInfo?.superCategory))
                                    val awardedPhoto = budgets.find { it.budgetId == tender.awardedBudgetId }?.providerPhotoUrl

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        LicitacionFolderPremium(
                                            title = tender.title,
                                            tenderId = tender.tenderId,
                                            status = tender.status,
                                            startDate = tender.startDate,
                                            endDate = tender.endDate,
                                            budgetCount = budgets.size,
                                            unreadCount = unreadCount,
                                            isSelected = isSelected,
                                            awardedProviderName = tender.awardedProviderName,
                                            awardedBudgetId = tender.awardedBudgetId,
                                            awardedProviderPhotoUrl = awardedPhoto,
                                            category = tender.category,
                                            categoryIcon = categoryInfo?.icon ?: "📋",
                                            categoryColor = Color(CategoryVisuals.getColorFor(tender.category)),
                                            supercategoryColor = superColor,
                                            onClick = {
                                                if (isMultiSelectionActive) {
                                                    onToggleItemSelection(tender.tenderId)
                                                } else {
                                                    onCloseBeAssistant()
                                                    selectedTenderForSheet = tender
                                                    onTenderSelected(tender.tenderId)
                                                }
                                            },
                                            onLongClick = {
                                                if (!isMultiSelectionActive) {
                                                    onToggleMultiSelection()
                                                }
                                                onToggleItemSelection(tender.tenderId)
                                            },
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        } else {
                            // Fase 4: Unificación de Grillas
                            BudgetGridContent(
                                state = directListState,
                                budgets = directBudgets,
                                isMultiSelectionActive = isMultiSelectionActive,
                                selectedItemIds = selectedItemIds,
                                onToggleItemSelection = onToggleItemSelection,
                                onBudgetClick = { budget ->
                                    if (isMultiSelectionActive) {
                                        onToggleItemSelection(budget.budgetId)
                                    } else {
                                        onMarkAsRead(budget.budgetId)
                                        budgetForA4Preview = budget
                                    }
                                },
                                onChatClick = onChatClick,
                                onToggleMultiSelection = onToggleMultiSelection,
                                onAvatarClick = { budget -> providerProfileToShow = budget }
                            )
                        }
                    }
                }
            }
        }

        ResultadoLicitacionOverlay(
            selectedTender = selectedTenderForSheet,
            onClose = {
                selectedTenderForSheet = null
                onTenderSelected(null)
                onSetContext(HUDContext.BUDGETS_TENDERS)
            },
            beBrainActionEvent = beBrainActionEvent,
            onSetContext = { nuevoContexto ->
                onSetContext(nuevoContexto)
            },
            getBudgetsForTender = getBudgetsForTender,
            activeFilters = activeFilters,
            dynamicCategories = dynamicCategories,
            refinementFilters = refinementFilters,
            sortOptions = sortOptions,
            onFilterToggle = onFilterToggle,
            onClearFilters = onClearFilters,
            onClearSort = onClearSort,
            onBudgetClick = { budget ->
                if (isMultiSelectionActive) {
                    onToggleItemSelection(budget.budgetId)
                } else {
                    onMarkAsRead(budget.budgetId)
                    budgetForA4Preview = budget
                }
            },
            onChatClick = onChatClick,
            onAvatarClick = { budget -> providerProfileToShow = budget },
            isMultiSelectionActive = isMultiSelectionActive,
            selectedItemIds = selectedItemIds,
            onToggleItemSelection = onToggleItemSelection,
            onToggleMultiSelection = onToggleMultiSelection,
            onAnalyticsClick = onAnalyticsRequest,
            onDeleteBudgets = onDeleteBudgets,
            onMarkAsReadMulti = { ids -> ids.forEach { id -> onMarkAsRead(id) } },
            showDeleteConfirmDialog = onRequestDeleteConfirmation,
            isAssistantActive = isSearchActive
        )

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                title = { Text("Confirmar Eliminación", fontWeight = FontWeight.Bold) },
                text = { Text(deleteContextMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        onConfirmDeleteAction?.invoke()
                        onDismissDeleteDialog()
                    }) {
                        Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeleteDialog) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                },
                containerColor = CardSurface,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }

        if (tenderForAnalytics != null) {
            Dialog(
                onDismissRequest = onCloseAnalytics,
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                BudgetComparisonAnalytics(
                    tender = tenderForAnalytics.first,
                    budgets = tenderForAnalytics.second,
                    onBack = onCloseAnalytics,
                    onViewBudgetDetail = { selectedId ->
                        val foundBudget = tenderForAnalytics.second.find { it.budgetId == selectedId }
                            ?: directBudgets.find { it.budgetId == selectedId }
                        if (foundBudget != null) {
                            onMarkAsRead(foundBudget.budgetId)
                            budgetForA4Preview = foundBudget
                        }
                        onCloseAnalytics()
                    }
                )
            }
        }

        if (tenderForDetails != null) {
            TenderDetailPopup(
                tender = tenderForDetails!!,
                onClose = onCloseTenderDetails,
                onUpdateStatus = { newStatus ->
                    onUpdateTenderStatus(tenderForDetails!!.tenderId, newStatus)
                    onCloseTenderDetails()
                }
            )
        }

        if (budgetForA4Preview != null) {
            Dialog(onDismissRequest = { budgetForA4Preview = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                BudgetMultiPageScreen(
                    budget = budgetForA4Preview!!,
                    onBack = { budgetForA4Preview = null },
                    onAccept = { _ -> onAcceptBudget(budgetForA4Preview!!); budgetForA4Preview = null },
                    onReject = { _ -> onRejectBudget(budgetForA4Preview!!); budgetForA4Preview = null }
                )
            }
        }

        if (providerProfileToShow != null) {
            ModalBottomSheet(onDismissRequest = { providerProfileToShow = null }, containerColor = CardSurface) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = providerProfileToShow?.providerPhotoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaverickBlue, CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Default.Person)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(providerProfileToShow?.providerName ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(providerProfileToShow?.providerCompanyName ?: "Profesional", color = MaverickBlue, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue), shape = RoundedCornerShape(12.dp)) {
                        Text("VER PERFIL COMPLETO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =================================================================================
// --- COMPONENTES DE UI REUTILIZADOS ---
// =================================================================================

@Composable
fun SimpleStatsHeader(label: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (label == "LICITACIONES") "Licitaciones" else "Presupuestos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = count.toString(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedBudgetHeaderTabs(
    currentTab: BudgetTabMode,
    onTabSelected: (BudgetTabMode) -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val centerX = screenWidth / 2
    val licTargetCenter = if (currentTab == BudgetTabMode.LICITACIONES) centerX else 0.dp
    val dirTargetCenter = if (currentTab == BudgetTabMode.DIRECTOS) centerX else screenWidth
    val licOffset by animateDpAsState(targetValue = licTargetCenter, label = "licX")
    val dirOffset by animateDpAsState(targetValue = dirTargetCenter, label = "dirX")

    Box(
        modifier = Modifier.fillMaxWidth().height(95.dp).background(MaverickColors.ROG_Dark_Bg).zIndex(10f),
        contentAlignment = Alignment.CenterStart
    ) {
        TabItemMaverick(
            label = "Licitaciones",
            description = "Concursos públicos",
            icon = "⚖️",
            isActive = currentTab == BudgetTabMode.LICITACIONES,
            isLicitacion = true,
            modifier = Modifier.offset(x = licOffset),
            onClick = { onTabSelected(BudgetTabMode.LICITACIONES) }
        )
        TabItemMaverick(
            label = "Presupuesto Directos",
            description = "Presupuestos de Chats",
            icon = "📩",
            isActive = currentTab == BudgetTabMode.DIRECTOS,
            isLicitacion = false,
            modifier = Modifier.offset(x = dirOffset),
            onClick = { onTabSelected(BudgetTabMode.DIRECTOS) }
        )
        DividerPremium(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun TabItemMaverick(
    label: String,
    description: String,
    icon: String,
    isActive: Boolean,
    isLicitacion: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val circleSize by animateDpAsState(if (isActive) 56.dp else 40.dp, label = "size")
    Box(
        modifier = modifier.size(0.dp).wrapContentSize(unbounded = true, align = Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentSize(unbounded = true).shakeClick { onClick() }
        ) {
            if (isActive && isLicitacion) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 16.dp)) {
                    Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(description, color = MaverickBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            IconCircleMaverick(icon, isActive, circleSize)
            if (isActive && !isLicitacion) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(start = 16.dp)) {
                    Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text(description, color = MaverickBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun IconCircleMaverick(icon: String, isActive: Boolean, size: Dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(if (isActive) MaverickBlue.copy(0.15f) else Color.White.copy(0.05f))
            .border(1.5.dp, if (isActive) MaverickBlue else Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = (size.value * 0.45f).sp, modifier = Modifier.alpha(if (isActive) 1f else 0.3f))
    }
}

// =================================================================================
// --- NUEVOS COMPONENTES DE NAVEGACIÓN EN CABECERA ---
// =================================================================================

/**
 * Burbuja de navegación minimalista para alternar entre secciones.
 * Incluye flechas indicadoras de dirección (estilo dinámico) y animaciones.
 * Diseño "Vívido": Fondo sólido semi-transparente y bordes definidos.
 */
@Composable
fun BudgetBubbleTab(
    icon: String,
    isToRight: Boolean, // Controla la dirección de la flecha indicadora
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shakeClick { onClick() }
            .background(Color.Black.copy(alpha = 0.75f), CircleShape) // Fondo sólido Cyber
            .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape) // Borde técnico nítido
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Flecha a la izquierda: Indica retorno a Licitaciones
        if (!isToRight) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Ir a Licitaciones",
                tint = MaverickBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Icono de la sección destino
        Text(
            text = icon,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 1.dp)
        )

        // Flecha a la derecha: Indica avance a Presupuestos Directos
        if (isToRight) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ir a Directos",
                tint = MaverickBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PresupuestosScreenPreview() {
    MyApplicationTheme {
        val sampleCategories = listOf(
            CategoryEntity(name = "Informatica", icon = "💻", superCategory = "Tecnología", isNew = false, isNewPrestador = false, isAd = false),
            CategoryEntity(name = "Plomería", icon = "🪠", superCategory = "Hogar", isNew = false, isNewPrestador = false, isAd = false)
        )
        val sampleTenders = listOf(
            TenderEntity("t1", "Reparación de Notebook", "u1", "La pantalla parpadea", "Informatica", "ABIERTA"),
            TenderEntity("t2", "Instalación de Grifería", "u1", "Cambiar juego de baño", "Plomería", "CERRADA")
        )
        val sampleBudgets = listOf(
            BudgetEntity("b1", "u1", "p1", "t1", "Maximiliano Nanterne", "Maverick Tech", "https://picsum.photos/seed/maverick/200/200", grandTotal = 1500.0, status = BudgetStatus.PENDIENTE),
            BudgetEntity("b2", "u1", "p2", "t1", "Juan Perez", "Soporte Ya", null, grandTotal = 1200.0, status = BudgetStatus.PENDIENTE)
        )

        PresupuestosScreenContent(
            tenders = sampleTenders,
            directBudgets = emptyList(),
            categories = sampleCategories,
            activeFilters = emptySet(),
            dynamicCategories = emptyList(),
            refinementFilters = emptyList(),
            sortOptions = emptyList(),
            onFilterToggle = {},
            onClearFilters = {},
            onClearSort = {},
            onSetContext = {},
            getBudgetsForTender = { MutableStateFlow(sampleBudgets) },
            onChatClick = {},
            onBack = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            onDeleteTenders = {},
            onDeleteBudgets = {},
            onMarkAsRead = {},
            onClearSelect = {},
            isSearchActive = false,
            searchQuery = "",
            onCloseBeAssistant = {},
            isMultiSelectionActive = false,
            selectedItemIds = emptySet(),
            onToggleItemSelection = {},
            onSelectAllItems = {},
            bottomPadding = PaddingValues(0.dp),
            beBrainActionEvent = remember { MutableSharedFlow<String>() },
            onToggleMultiSelection = {},
            onBeLongClick = {},
            tenderForAnalytics = null,
            tenderForDetails = null,
            onCloseAnalytics = {},
            onCloseTenderDetails = {},
            showDeleteConfirmDialog = false,
            deleteContextMessage = "",
            onConfirmDeleteAction = {},
            onDismissDeleteDialog = {},
            onRequestDeleteConfirmation = { _, _ -> },
            onAnalyticsRequest = { _, _ -> },
            onUpdateTenderStatus = { _, _ -> },
            onTenderSelected = {}
        )
    }
}

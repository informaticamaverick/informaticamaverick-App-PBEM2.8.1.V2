package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.*
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.repository.AppActionCoordinator
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalConfiguration
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetService
import com.example.myapplication.data.local.BudgetProfessionalFee
import com.example.myapplication.data.local.BudgetTax
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.layout.FlowRow as OptInFlowRow

// --- CONSTANTES DE DISEÑO ---
private val MaverickBlue = Color(0xFF2197F5)
private val CardSurface = Color(0xFF161C24)

@Composable
fun PresupuestosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    // 🔥 REGLA DE ORO: Inyectamos el Maestro de Intenciones (AppActionCoordinator)
    // Nota: Coordinator es un Singleton @Inject, no un ViewModel.
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    // Suscribirnos a la búsqueda global del Coordinator si es necesario
    // val searchQuery by coordinator.globalSearchQuery.collectAsStateWithLifecycle()

    // --- SUSCRIPCIÓN A DATOS (PANTALLA TONTA) ---
    val tenders by viewModel.filteredTenders.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    val dynamicCategories by beBrainViewModel.dynamicCategories.collectAsStateWithLifecycle()
    val availableFilters by beBrainViewModel.availableFilters.collectAsStateWithLifecycle()
    val availableSortOptions by beBrainViewModel.availableSortOptions.collectAsStateWithLifecycle()
    
    // Estados de UI
    val isSearchActive by beBrainViewModel.isSearchActive.collectAsStateWithLifecycle()
    val isMultiSelectionActive by viewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    // 🔥 REGLA DE ORO 4: Sincronización del Contexto de Be
    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("presupuestos")
        beBrainViewModel.setHUDContext(HUDContext.BUDGETS_TENDERS) // Sincronizamos Cerebro
        viewModel.setContext(HUDContext.BUDGETS_TENDERS) // Sincronizamos Obrero
    }

    // --- SECCIÓN: SINCRONIZACIÓN DE BÚSQUEDA Y ASISTENTE (Be) ---
    val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()
    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    // 🔥 Sincronización de Filtros
    LaunchedEffect(activeFilters) {
        viewModel.setFilters(activeFilters)
    }

    // 🔥 Sincronización de Multiselección (Fase 3 - Habilita herramientas de Be)
    LaunchedEffect(isMultiSelectionActive, selectedItemIds) {
        beBrainViewModel.syncMultiSelection(isMultiSelectionActive, selectedItemIds)
    }

    // 🔥 LIMPIEZA AL SALIR DE LA PANTALLA
    DisposableEffect(Unit) {
        onDispose {
            beBrainViewModel.setCustomActions(emptyList())
            beBrainViewModel.syncMultiSelection(false, emptySet())
        }
    }

    // Estados para Dialogs y Navegación Interna
    var tenderForDetails by remember { mutableStateOf<TenderEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteContextMessage by remember { mutableStateOf("") }
    var onConfirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // --- SECCIÓN: ANALÍTICAS DE COMPARACIÓN ---
    var showAnalyticsOverlay by remember { mutableStateOf(false) }
    var budgetsToAnalyze by remember { mutableStateOf<List<BudgetEntity>>(emptyList()) }
    var tenderForAnalytics by remember { mutableStateOf<TenderEntity?>(null) }

    // --- SECCIÓN: ACCIONES DE BE (HUD) ---
    val hudContext by viewModel.currentHUDContext.collectAsStateWithLifecycle()
    val budgetActions by viewModel.beActions.collectAsStateWithLifecycle()

    LaunchedEffect(budgetActions, hudContext) {
        val baseActions = budgetActions.map { action ->
            action.copy(onClick = { beBrainViewModel.triggerAction(action.id) })
        }
        val finalActions = baseActions + listOfNotNull(
            if (hudContext == HUDContext.BUDGETS_TENDERS) {
                BeSmallActionModel(
                    id = "licit",
                    icon = Icons.Default.Add,
                    label = "Nueva Lic",
                    emoji = "📄",
                    tint = MaverickBlue,
                    isDefault = true,
                    onClick = { beBrainViewModel.triggerAction("licit") }
                )
            } else null
        )
        beBrainViewModel.setCustomActions(finalActions)
    }

    // --- SECCIÓN: CAPTURA DE EVENTOS DEL CEREBRO (BeBrain) ---
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "select_all" -> viewModel.selectAll(tenders.map { it.tenderId })
                "view_tender_details" -> {
                    val tenderId = selectedItemIds.firstOrNull()
                    tenderForDetails = tenders.find { it.tenderId == tenderId }
                    viewModel.updateMultiSelection(false)
                }
                "compare_selected" -> {
                    // Acción táctica: Comparamos solo los presupuestos seleccionados (usualmente dentro de una licitación)
                    val selectedBudgets = allBudgets.filter { it.budgetId in selectedItemIds }
                    if (selectedBudgets.isNotEmpty()) {
                        budgetsToAnalyze = selectedBudgets
                        // Buscamos la licitación asociada al primer presupuesto para el contexto visual
                        tenderForAnalytics = tenders.find { it.tenderId == selectedBudgets.first().tenderId }
                        showAnalyticsOverlay = true
                    }
                }
                "compare_all" -> {
                    // Acción desde TENDER_DETAILS: Comparamos todo lo recibido para esa licitación
                    val currentTenderId = viewModel.selectedIds.value.firstOrNull() // En este contexto, selectedIds tiene el ID de la licitación abierta
                    val tender = tenders.find { it.tenderId == currentTenderId }
                    val budgetsForThisTender = allBudgets.filter { it.tenderId == currentTenderId }
                    
                    if (tender != null && budgetsForThisTender.isNotEmpty()) {
                        tenderForAnalytics = tender
                        budgetsToAnalyze = budgetsForThisTender
                        showAnalyticsOverlay = true
                    }
                }
                "delete_multi" -> {
                    deleteContextMessage = "¿Deseas eliminar las licitaciones seleccionadas?"
                    onConfirmDeleteAction = { viewModel.deleteTenders(selectedItemIds) }
                    showDeleteConfirmDialog = true
                }
                "cancel" -> viewModel.updateMultiSelection(false)
            }
        }
    }

    PresupuestosScreenContent(
        tenders = tenders,
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
        getBudgetsForTender = { viewModel.getFilteredBudgetsForTender(it) },
        onChatClick = onChatClick,
        onBack = onBack,
        bottomPadding = bottomPadding, // Pasamos el padding recibido
        isSearchActive = isSearchActive,
        isMultiSelectionActive = isMultiSelectionActive,
        selectedItemIds = selectedItemIds,
        onToggleItemSelection = { viewModel.toggleSelection(it) },
        onToggleMultiSelection = { viewModel.updateMultiSelection(!isMultiSelectionActive) },
        beBrainActionEvent = beBrainViewModel.actionEvent,
        tenderForDetails = tenderForDetails,
        onCloseTenderDetails = { tenderForDetails = null },
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        deleteContextMessage = deleteContextMessage,
        onConfirmDeleteAction = onConfirmDeleteAction,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onUpdateTenderStatus = { id, status -> viewModel.updateTenderStatus(id, status) },
        onTenderSelected = { viewModel.setSelectedTenderId(it) },
        onMarkAsRead = { id -> viewModel.markAsRead(setOf(id)) },
        onDeleteBudgets = { ids -> viewModel.deleteBudgets(ids) },
        showAnalyticsOverlay = showAnalyticsOverlay,
        onCloseAnalytics = { showAnalyticsOverlay = false },
        tenderForAnalytics = tenderForAnalytics,
        budgetsToAnalyze = budgetsToAnalyze,
        onOpenBudgetPreview = { /* Lógica para abrir desde analytics si es necesario */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
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
    bottomPadding: PaddingValues, // Re-agregado
    isSearchActive: Boolean,
    isMultiSelectionActive: Boolean,
    selectedItemIds: Set<String>,
    onToggleItemSelection: (String) -> Unit,
    onToggleMultiSelection: () -> Unit,
    beBrainActionEvent: SharedFlow<String>,
    tenderForDetails: TenderEntity?,
    onCloseTenderDetails: () -> Unit,
    showDeleteConfirmDialog: Boolean,
    deleteContextMessage: String,
    onConfirmDeleteAction: (() -> Unit)?,
    onDismissDeleteDialog: () -> Unit,
    onUpdateTenderStatus: (String, String) -> Unit,
    onTenderSelected: (String?) -> Unit,
    onMarkAsRead: (String) -> Unit,
    onDeleteBudgets: (Set<String>) -> Unit,
    // Props para Analytics
    showAnalyticsOverlay: Boolean,
    onCloseAnalytics: () -> Unit,
    tenderForAnalytics: TenderEntity?,
    budgetsToAnalyze: List<BudgetEntity>,
    onOpenBudgetPreview: (BudgetEntity) -> Unit
) {
    val tenderListState = rememberLazyListState()
    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }

    MaverickBackgroundStrix {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding()) // Aplicar padding
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                
                // --- SECCIÓN: CABECERA TÉCNICA ---
                BarraCabezera(
                    title = "LICITACIONES",
                    subtitle = "Gestión de Concursos",
                    emoji = "⚖️",
                    onBack = onBack,
                    onInfoClick = { },
                    accentColor = MaverickBlue
                )
                Spacer(modifier = Modifier.height(18.dp))

                // --- SECCIÓN: MENÚ DE FILTROS Y LISTA ---
                MoldeBarraMenu(
                    itemCount = tenders.size,
                    labelCountMain = "LICITACIONES",
                    labelCountSub = "Activas",
                    activeRefinements = activeFilters,
                    refinementOptions = refinementFilters,
                    sortOptions = sortOptions,
                    onToggleRefinement = onFilterToggle,
                    onClearRefinements = onClearFilters,
                    onClearSort = onClearSort,
                    showSuscritos = false,
                    showCercania = false,
                    showVista = false,
                    modifier = Modifier.fillMaxSize(),
                    customActions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            sortOptions.forEach { item ->
                                MaverickTacticalButton(
                                    isActive = activeFilters.contains(item.id),
                                    accentColor = item.color,
                                    onClick = { onFilterToggle(item.id) }
                                ) { Text(item.emoji) }
                                Spacer(Modifier.width(6.dp))
                            }
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
                    LazyColumn(
                        state = tenderListState, 
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
                        modifier = Modifier.fillMaxSize() // Estabilidad para evitar que se muevan
                    ) {
                        items(tenders, key = { it.tenderId }) { tender ->
                            val budgets by getBudgetsForTender(tender.tenderId).collectAsStateWithLifecycle(emptyList())
                            val categoryInfo = categories.find { it.name.equals(tender.category, ignoreCase = true) }

                            LicitacionFolderPremium(
                                title = tender.title,
                                category = tender.category,
                                categoryIcon = categoryInfo?.icon ?: "📋",
                                tenderId = tender.tenderId,
                                status = tender.status,
                                startDate = tender.startDate,
                                endDate = tender.endDate,
                                budgetCount = budgets.size,
                                unreadCount = 0,
                                isSelected = selectedItemIds.contains(tender.tenderId),
                                onClick = {
                                    if (isMultiSelectionActive) onToggleItemSelection(tender.tenderId)
                                    else { 
                                        selectedTenderForSheet = tender 
                                        onTenderSelected(tender.tenderId) 
                                        onSetContext(HUDContext.BUDGETS_TENDERS)
                                    }
                                },
                                onLongClick = {
                                    if (!isMultiSelectionActive) onToggleMultiSelection()
                                    onToggleItemSelection(tender.tenderId)
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // --- SECCIÓN: OVERLAY DE RESULTADOS (SOLUCIÓN AL BUG DE NAVEGACIÓN) ---
        ResultadoLicitacionOverlay(
            selectedTender = selectedTenderForSheet,
            onClose = {
                selectedTenderForSheet = null
                onTenderSelected(null)
                onSetContext(HUDContext.BUDGETS_TENDERS)
            },
            beBrainActionEvent = beBrainActionEvent,
            onSetContext = { nuevoContexto -> onSetContext(nuevoContexto) },
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
            onAnalyticsClick = { _, _ -> /* Implementar analíticas si es necesario */ },
            onDeleteBudgets = onDeleteBudgets,
            onMarkAsReadMulti = { ids -> ids.forEach { id -> onMarkAsRead(id) } },
            showDeleteConfirmDialog = { _, _ -> /* Reutilizar dialog global si es necesario */ },
            isAssistantActive = isSearchActive
        )

        // --- SECCIÓN: DIALOGS GLOBALES ---
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                confirmButton = { TextButton(onClick = { onConfirmDeleteAction?.invoke(); onDismissDeleteDialog() }) { Text("ELIMINAR", color = Color.Red) } },
                dismissButton = { TextButton(onClick = onDismissDeleteDialog) { Text("CANCELAR") } },
                title = { Text("Confirmar") },
                text = { Text(deleteContextMessage) },
                containerColor = CardSurface,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }

        if (tenderForDetails != null) {
            TenderDetailPopup(
                tender = tenderForDetails, 
                onClose = onCloseTenderDetails, 
                onUpdateStatus = { onUpdateTenderStatus(tenderForDetails.tenderId, it); onCloseTenderDetails() },
                onContactProvider = onChatClick
            )
        }

        // --- OVERLAY DE ANALÍTICAS (COMPARATIVA) ---
        if (showAnalyticsOverlay && tenderForAnalytics != null) {
            Dialog(
                onDismissRequest = onCloseAnalytics,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                BudgetComparisonAnalytics(
                    tender = tenderForAnalytics,
                    budgets = budgetsToAnalyze,
                    onBack = onCloseAnalytics,
                    onViewBudgetDetail = { bId ->
                        // Si el usuario quiere ver el detalle desde el analytics
                        onCloseAnalytics()
                        val budget = budgetsToAnalyze.find { it.budgetId == bId }
                        if (budget != null) budgetForA4Preview = budget
                    }
                )
            }
        }
        
        // Visor A4
        if (budgetForA4Preview != null) {
            Dialog(onDismissRequest = { budgetForA4Preview = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                BudgetMultiPageScreen(
                    budget = budgetForA4Preview!!, 
                    onBack = { budgetForA4Preview = null }
                )
            }
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
            BudgetEntity("b1", "u1", "p1", "t1", "Maximiliano Nanterne", "Maverick Tech", null, grandTotal = 1500.0)
        )

        PresupuestosScreenContent(
            tenders = sampleTenders,
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
            bottomPadding = PaddingValues(0.dp), // Agregado
            isSearchActive = false,
            isMultiSelectionActive = false,
            selectedItemIds = emptySet(),
            onToggleItemSelection = {},
            onToggleMultiSelection = {},
            beBrainActionEvent = remember { MutableSharedFlow() },
            tenderForDetails = null,
            onCloseTenderDetails = {},
            showDeleteConfirmDialog = false,
            deleteContextMessage = "",
            onConfirmDeleteAction = {},
            onDismissDeleteDialog = {},
            onUpdateTenderStatus = { _, _ -> },
            onTenderSelected = {},
            onMarkAsRead = {},
            onDeleteBudgets = {},
            showAnalyticsOverlay = false,
            onCloseAnalytics = {},
            tenderForAnalytics = null,
            budgetsToAnalyze = emptyList(),
            onOpenBudgetPreview = {}
        )
    }
}

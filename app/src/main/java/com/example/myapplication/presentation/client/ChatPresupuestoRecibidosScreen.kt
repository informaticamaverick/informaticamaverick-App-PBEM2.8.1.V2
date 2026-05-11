package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.local.BudgetStatus
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

// =================================================================================
// --- CONSTANTES DE DISEÑO ---
// =================================================================================
private val MaverickBlue = Color(0xFF2197F5)
private val CardSurface = Color(0xFF161C24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPresupuestoRecibidosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    onChatClick: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val directBudgets by viewModel.filteredDirectBudgets.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    val availableFilters by beBrainViewModel.availableFilters.collectAsStateWithLifecycle()
    val availableSortOptions by beBrainViewModel.availableSortOptions.collectAsStateWithLifecycle()
    val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()
    val isMultiSelectionActive by viewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // [REGLA DE ORO] Sincronizamos solo el Obrero y el Contexto de Be.
        viewModel.setContext(HUDContext.BUDGETS_DIRECT) 
        beBrainViewModel.onRouteChanged("direct_budgets")
    }

    val budgetActions by viewModel.beActions.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()

    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteContextMessage by remember { mutableStateOf("") }
    var onConfirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // --- SECCIÓN: ANALÍTICAS DE COMPARACIÓN ---
    var showAnalyticsOverlay by remember { mutableStateOf(false) }
    var budgetsToAnalyze by remember { mutableStateOf<List<BudgetEntity>>(emptyList()) }

    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    LaunchedEffect(activeFilters) {
        viewModel.setFilters(activeFilters)
    }

    LaunchedEffect(isMultiSelectionActive, selectedItemIds) {
        beBrainViewModel.syncMultiSelection(isMultiSelectionActive, selectedItemIds)
    }

    val hudContext by viewModel.currentHUDContext.collectAsStateWithLifecycle()
    val currentSelectedIds by rememberUpdatedState(selectedItemIds)

    LaunchedEffect(budgetActions, hudContext, isMultiSelectionActive) {
        if (isMultiSelectionActive) {
            val baseActions = budgetActions.map { action ->
                action.copy(onClick = { beBrainViewModel.triggerAction(action.id) })
            }
            beBrainViewModel.setCustomActions(baseActions, HUDContext.BUDGETS_DIRECT)
        } else {
            beBrainViewModel.clearCustomActions(HUDContext.BUDGETS_DIRECT)
        }
    }

    val currentIdsToSelect by rememberUpdatedState(directBudgets.map { it.budgetId })

    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "select_all" -> viewModel.selectAll(currentIdsToSelect)
                "mark_as_read" -> viewModel.markAsRead(currentSelectedIds)
                "compare_selected" -> {
                    // Acción táctica: Comparamos presupuestos directos seleccionados
                    val selected = allBudgets.filter { it.budgetId in selectedItemIds }
                    if (selected.isNotEmpty()) {
                        budgetsToAnalyze = selected
                        showAnalyticsOverlay = true
                    }
                }
                "delete_multi" -> {
                    deleteContextMessage = "¿Deseas eliminar los presupuestos seleccionados?"
                    onConfirmDeleteAction = { viewModel.deleteBudgets(currentSelectedIds) }
                    showDeleteConfirmDialog = true
                }
                "cancel" -> viewModel.updateMultiSelection(false)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            beBrainViewModel.clearCustomActions(HUDContext.BUDGETS_DIRECT)
            beBrainViewModel.syncMultiSelection(false, emptySet())
        }
    }

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } }) {
        ChatPresupuestoRecibidosScreenContent(
            directBudgets = directBudgets,
            categories = categories, // PASAR CATEGORIAS AL CONTENIDO
            activeFilters = activeFilters,
            refinementFilters = availableFilters,
            sortOptions = availableSortOptions,
            onFilterToggle = { beBrainViewModel.toggleFilter(it) },
            onClearFilters = { beBrainViewModel.clearSpecificFilters(listOf("filter_", "cat_")) },
            onClearSort = { beBrainViewModel.clearSpecificFilters(listOf("sort_", "view_")) },
            onChatClick = onChatClick,
            onBack = onBack,
            onAcceptBudget = { budget -> viewModel.acceptBudget(budget) },
            onRejectBudget = { budget -> viewModel.rejectBudget(budget) },
            onMarkAsRead = { id -> viewModel.markAsRead(setOf(id)) },
            bottomPadding = bottomPadding,
            isMultiSelectionActive = isMultiSelectionActive,
            selectedItemIds = selectedItemIds,
            onToggleItemSelection = { viewModel.toggleSelection(it) },
            onToggleMultiSelection = { viewModel.updateMultiSelection(!isMultiSelectionActive) },
            budgetForA4Preview = budgetForA4Preview,
            onCloseBudgetA4Preview = { budgetForA4Preview = null },
            onBudgetClick = { budget ->
                if (isMultiSelectionActive) viewModel.toggleSelection(budget.budgetId)
                else {
                    viewModel.markAsRead(setOf(budget.budgetId))
                    budgetForA4Preview = budget
                }
            },
            providerProfileToShow = providerProfileToShow,
            onCloseProviderProfile = { providerProfileToShow = null },
            onAvatarClick = { budget -> providerProfileToShow = budget },
            showDeleteConfirmDialog = showDeleteConfirmDialog,
            deleteContextMessage = deleteContextMessage,
            onConfirmDeleteAction = onConfirmDeleteAction,
            onDismissDeleteDialog = { showDeleteConfirmDialog = false },
            showAnalyticsOverlay = showAnalyticsOverlay,
            onCloseAnalytics = { showAnalyticsOverlay = false },
            budgetsToAnalyze = budgetsToAnalyze
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPresupuestoRecibidosScreenContent(
    directBudgets: List<BudgetEntity>,
    categories: List<CategoryEntity> = emptyList(), // NUEVO: RECIBIR CATEGORIAS
    activeFilters: Set<String>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onChatClick: (String, String?) -> Unit,
    onBack: () -> Unit,
    onAcceptBudget: (BudgetEntity) -> Unit,
    onRejectBudget: (BudgetEntity) -> Unit,
    onMarkAsRead: (String) -> Unit,
    isMultiSelectionActive: Boolean,
    selectedItemIds: Set<String>,
    onToggleItemSelection: (String) -> Unit,
    bottomPadding: PaddingValues,
    onToggleMultiSelection: () -> Unit,
    budgetForA4Preview: BudgetEntity?,
    onCloseBudgetA4Preview: () -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit,
    providerProfileToShow: BudgetEntity?,
    onCloseProviderProfile: () -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    showDeleteConfirmDialog: Boolean,
    deleteContextMessage: String,
    onConfirmDeleteAction: (() -> Unit)?,
    onDismissDeleteDialog: () -> Unit,
    // Props para Analytics
    showAnalyticsOverlay: Boolean,
    onCloseAnalytics: () -> Unit,
    budgetsToAnalyze: List<BudgetEntity>
) {
    val directListState = rememberLazyGridState()

    MaverickBackgroundStrix {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding())
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {

                BarraCabezera(
                    title = "PRESUPUESTOS",
                    subtitle = "Solicitudes Directas",
                    emoji = "📩",
                    onBack = onBack,
                    onInfoClick = { },
                    accentColor = MaverickBlue
                )
                
                Spacer(modifier = Modifier.height(18.dp))

                MoldeBarraMenu(
                    itemCount = directBudgets.size,
                    labelCountMain = "DIRECTOS",
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
                    val genericTender = remember(directBudgets) {
                        TenderEntity(
                            tenderId = "direct_budgets",
                            title = "Presupuestos Directos",
                            clientId = directBudgets.firstOrNull()?.clientId ?: "",
                            description = "Presupuestos recibidos directamente por chat",
                            category = "Varios",
                            status = "DIRECTO"
                        )
                    }
                    BudgetGridContent(
                        state = directListState,
                        tender = genericTender,
                        budgets = directBudgets,
                        categories = categories, // PASAMOS LAS CATEGORÍAS REALES DEL OBRERO
                        isMultiSelectionActive = isMultiSelectionActive,
                        selectedItemIds = selectedItemIds,
                        onToggleItemSelection = onToggleItemSelection,
                        onBudgetClick = { budget ->
                            onBudgetClick(budget)
                            onMarkAsRead(budget.budgetId)
                        },
                        onChatClick = onChatClick,
                        onToggleMultiSelection = onToggleMultiSelection,
                        onAvatarClick = onAvatarClick
                    )
                }
            }
        }

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

        if (budgetForA4Preview != null) {
            Dialog(onDismissRequest = onCloseBudgetA4Preview, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                BudgetMultiPageScreen(
                    budget = budgetForA4Preview,
                    onBack = onCloseBudgetA4Preview,
                    onAccept = { _ -> onAcceptBudget(budgetForA4Preview); onCloseBudgetA4Preview() },
                    onReject = { _ -> onRejectBudget(budgetForA4Preview); onCloseBudgetA4Preview() }
                )
            }
        }

        if (providerProfileToShow != null) {
            ModalBottomSheet(onDismissRequest = onCloseProviderProfile, containerColor = CardSurface) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = providerProfileToShow.providerPhotoUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaverickBlue, CircleShape),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Default.Person)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(providerProfileToShow.providerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(providerProfileToShow.providerCompanyName ?: "Profesional", color = MaverickBlue, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue), shape = RoundedCornerShape(12.dp)) {
                        Text("VER PERFIL COMPLETO", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- OVERLAY DE ANALÍTICAS (COMPARATIVA) ---
        if (showAnalyticsOverlay && budgetsToAnalyze.isNotEmpty()) {
            Dialog(
                onDismissRequest = onCloseAnalytics,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                // Creamos un Tender mock para la comparativa de presupuestos directos
                val mockTender = TenderEntity(
                    tenderId = "direct_comparison",
                    title = "Comparativa de Presupuestos Directos",
                    clientId = budgetsToAnalyze.first().clientId,
                    description = "Análisis de múltiples ofertas recibidas por chat.",
                    category = budgetsToAnalyze.first().category ?: "Varios",
                    status = "DIRECTO"
                )
                BudgetComparisonAnalytics(
                    tender = mockTender,
                    budgets = budgetsToAnalyze,
                    onBack = onCloseAnalytics,
                    onViewBudgetDetail = { bId ->
                        onCloseAnalytics()
                        val budget = budgetsToAnalyze.find { it.budgetId == bId }
                        if (budget != null) {
                            // Se podría disparar el visor A4 aquí si fuera necesario
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun ChatPresupuestoRecibidosScreenPreview() {
    MyApplicationTheme {
        val sampleBudgets = listOf(
            BudgetEntity("b1", "u1", "p1", "t1", "Maximiliano Nanterne", "Maverick Tech", null, grandTotal = 1500.0)
        )

        ChatPresupuestoRecibidosScreenContent(
            directBudgets = sampleBudgets,
            activeFilters = emptySet(),
            refinementFilters = emptyList(),
            sortOptions = emptyList(),
            onFilterToggle = {},
            onClearFilters = {},
            onClearSort = {},
            onChatClick = { _, _ -> },
            onBack = {},
            onAcceptBudget = {},
            onRejectBudget = {},
            onMarkAsRead = {},
            bottomPadding = PaddingValues(0.dp),
            isMultiSelectionActive = false,
            selectedItemIds = emptySet(),
            onToggleItemSelection = {},
            onToggleMultiSelection = {},
            budgetForA4Preview = null,
            onCloseBudgetA4Preview = {},
            onBudgetClick = {},
            providerProfileToShow = null,
            onCloseProviderProfile = {},
            onAvatarClick = {},
            showDeleteConfirmDialog = false,
            deleteContextMessage = "",
            onConfirmDeleteAction = {},
            onDismissDeleteDialog = {},
            showAnalyticsOverlay = false,
            onCloseAnalytics = {},
            budgetsToAnalyze = emptyList()
        )
    }
}

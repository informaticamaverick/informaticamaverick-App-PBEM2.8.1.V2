
/**
package com.example.myapplication.presentation.features.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.BudgetStatus
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.features.budget.BudgetViewModel
import com.example.myapplication.presentation.features.home.CategoryViewModel
import com.example.myapplication.presentation.features.budget.BudgetMultiPageScreen
import com.example.myapplication.presentation.features.budget.BudgetComparisonAnalytics
import com.example.myapplication.presentation.components.budgetGridListItems
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

// =================================================================================
// --- CONSTANTES DE DISEÑO ---
// =================================================================================
private val MaverickBlue = Color(0xFF2197F5)
private val CardSurface = Color(0xFF161C24)

/**
 * --- 🏗️ COMPONENTE: PRESUPUESTOS RECIBIDOS SHEET (ELITE HUD) ---
 * Versión reestructurada como hoja emergente para integración fluida en ChatScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosRecibidosSheet(
    isVisible: Boolean,
    onClose: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    onChatClick: (String, String?) -> Unit = { _, _ -> },
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

    // 🔥 SINCRONIZACIÓN DE CONTEXTO: Informamos al Cerebro sobre el cambio de ruta/contexto
    LaunchedEffect(isVisible) {
        if (isVisible) {
            viewModel.setContext(HUDContext.BUDGETS_DIRECT)
            beBrainViewModel.onRouteChanged("direct_budgets")
        }
        beBrainViewModel.setSheetVisible(isVisible)
    }

    val budgetActionIds by viewModel.beActionIds.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle(emptyList())

    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerProfileToShow by remember { mutableStateOf<BudgetEntity?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteContextMessage by remember { mutableStateOf("") }
    var onConfirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

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

    LaunchedEffect(budgetActionIds, hudContext, isMultiSelectionActive, isVisible) {
        if (isVisible) {
            beBrainViewModel.setCustomActionIds(budgetActionIds, HUDContext.BUDGETS_DIRECT)
        }
    }

    val currentIdsToSelect by rememberUpdatedState(directBudgets.map { it.budgetId })

    LaunchedEffect(isVisible) {
        if (isVisible) {
            beBrainViewModel.actionEvent.collect { actionId ->
                when (actionId) {
                    "select_all" -> viewModel.selectAll(currentIdsToSelect)
                    "mark_as_read" -> viewModel.markAsRead(currentSelectedIds)
                    "compare_selected" -> {
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
    }

    SheetEmergenteVertical(
        isVisible = isVisible,
        onClose = { 
            onClose()
            beBrainViewModel.onRouteChanged("chat")
        },
        title = "PRESUPUESTOS",
        helperText = "ADMINISTRADOR DE",
        emoji = "📩",
        topOffset = 150.dp, 
        initialAnchorIsFull = true, 
        isScrollable = false, 
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "TOTAL",
                    style = CyberTypography.MonospaceData.copy(
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = directBudgets.size.toString().padStart(2, '0'),
                    style = CyberTypography.TitleTech.copy(
                        color = MaverickBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                )
            }
        }
    ) {
        PresupuestosRecibidosSheetContent(
            directBudgets = directBudgets,
            categories = categories,
            activeFilters = activeFilters,
            refinementFilters = availableFilters,
            sortOptions = availableSortOptions,
            onFilterToggle = { beBrainViewModel.toggleFilter(it) },
            onClearFilters = { beBrainViewModel.clearSpecificFilters(listOf("filter_", "cat_")) },
            onClearSort = { beBrainViewModel.clearSpecificFilters(listOf("sort_", "view_")) },
            onChatClick = onChatClick,
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
fun PresupuestosRecibidosSheetContent(
    directBudgets: List<BudgetEntity>,
    categories: List<CategoryEntity> = emptyList(),
    activeFilters: Set<String>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onChatClick: (String, String?) -> Unit,
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
    showAnalyticsOverlay: Boolean,
    onCloseAnalytics: () -> Unit,
    budgetsToAnalyze: List<BudgetEntity>
) {
    val directListState = rememberLazyListState()

    // 🔥 [ELITE] Reset scroll to top when direct budgets change
    LaunchedEffect(directBudgets) {
        if (directBudgets.isNotEmpty()) {
            directListState.animateScrollToItem(0)
        }
    }

    // 🔥 [ELITE] Mapping filters to DropdownItemData
    val filterItems = remember(refinementFilters) {
        refinementFilters.map { DropdownItemData(it.id, it.label, emoji = it.emoji) }
    }
    val sortItems = remember(sortOptions) {
        sortOptions.map { DropdownItemData(it.id, it.label, emoji = it.emoji) }
    }

    val mappedRecentFilters = remember(filterItems, activeFilters) {
        // Para esta pantalla simplificada, usamos los primeros elementos como "recientes" para que nunca esté vacío
        filterItems.map { FilterSortItem(it.id, it.label, it.emoji ?: "🔹") }.take(5)
    }

    val mappedRecentSorts = remember(sortItems, activeFilters) {
        sortItems.map { FilterSortItem(it.id, it.label, it.emoji ?: "🔹") }
    }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding.calculateBottomPadding())) {
        
        Spacer(modifier = Modifier.height(4.dp))

        // --- FILTROS PREMIUM (Sincronización Elite) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoldePremiumFilterCard(
                label = "Filtrar por",
                dropdownItems = filterItems,
                recentItems = mappedRecentFilters,
                activeFilters = activeFilters,
                onToggle = onFilterToggle,
                modifier = Modifier.weight(1f)
            )
            MoldePremiumSortCard(
                label = "Ordenar por",
                dropdownItems = sortItems,
                recentItems = mappedRecentSorts,
                activeSort = activeFilters.find { it.startsWith("sort_") },
                onSortSelect = onFilterToggle,
                modifier = Modifier.weight(1f)
            )
        }

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

        val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
        
        Box(modifier = Modifier.weight(1f)) {
            ListaMoldeV2(
                titulo = "PRESUPUESTOS RECIBIDOS",
                subtitulo = "ADMINISTRACIÓN TÁCTICA",
                emoji = "📩",
                compactInfo = "Directos",
                itemCount = directBudgets.size,
                accentColor = MaverickBlue,
                state = directListState,
                acciones = {
                    if (activeFilters.isNotEmpty()) {
                        BotonCabeceraAccion(
                            onClick = { onClearFilters() },
                            icon = Icons.Default.FilterAltOff,
                            color = MaverickColors.MagentaNeon
                        )
                    }
                }
            ) {
                budgetGridListItems(
                    tender = genericTender,
                    budgets = directBudgets,
                    categories = categories,
                    isMultiSelectionActive = isMultiSelectionActive,
                    selectedItemIds = selectedItemIds,
                    onToggleItemSelection = onToggleItemSelection,
                    onBudgetClick = { budget ->
                        onBudgetClick(budget)
                        onMarkAsRead(budget.budgetId)
                    },
                    onChatClick = onChatClick,
                    onToggleMultiSelection = onToggleMultiSelection,
                    onAvatarClick = onAvatarClick,
                    expandedStates = expandedStates
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
                budget = budgetForA4Preview!!,
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
                    model = providerProfileToShow!!.providerPhotoUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaverickBlue, CircleShape),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Default.Person)
                )
                Spacer(Modifier.height(12.dp))
                Text(providerProfileToShow!!.providerName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text(providerProfileToShow!!.providerCompanyName ?: "Profesional", color = MaverickBlue, fontSize = 14.sp)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue), shape = RoundedCornerShape(12.dp)) {
                    Text("VER PERFIL COMPLETO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAnalyticsOverlay && budgetsToAnalyze.isNotEmpty()) {
        Dialog(
            onDismissRequest = onCloseAnalytics,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
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
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PresupuestosRecibidosSheetPreview() {
    MyApplicationTheme {
        val sampleBudgets = listOf(
            BudgetEntity("b1", "u1", "p1", "t1", "Maximiliano Nanterne", "Maverick Tech", null, grandTotal = 1500.0)
        )

        PresupuestosRecibidosSheetContent(
            directBudgets = sampleBudgets,
            activeFilters = emptySet(),
            refinementFilters = emptyList(),
            sortOptions = emptyList(),
            onFilterToggle = {},
            onClearFilters = {},
            onClearSort = {},
            onChatClick = { _, _ -> },
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
**/

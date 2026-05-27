package com.example.myapplication.presentation.features.budget

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.features.home.CategoryViewModel
import com.example.myapplication.presentation.features.home.CategoryVisuals
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.uishared.components.BudgetA4Viewer
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.domain.model.toEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// === SECCIÓN: CONSTANTES DE DISEÑO ===
private val DarkBg = MaverickColors.V2TechSurface
private val MaverickBlue = Color(0xFF00F0FF) // Cyan Brillante
private val CardSurface = MaverickColors.EliteSurface

// === SECCIÓN: COMPONENTE PRINCIPAL (CEREBRO DE PANTALLA) ===

@Composable
fun PresupuestosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    onChatClick: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    // --- SUSCRIPCIÓN A DATOS (Elite SSOT) ---
    val tenders by viewModel.filteredTenders.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val activeFilters by beBrainViewModel.activeFilters.collectAsStateWithLifecycle()
    val allBudgetsData by viewModel.allBudgets.collectAsStateWithLifecycle(emptyList())
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()

    // --- SUSCRIPCIÓN A FILTROS MAPEADOS EN VM (Elite) ---
    val filterDropdownItems by viewModel.filterDropdownItems.collectAsStateWithLifecycle()
    val sortDropdownItems by viewModel.sortDropdownItems.collectAsStateWithLifecycle()
    val activeSortCriteria by viewModel.activeSortCriteria.collectAsStateWithLifecycle()
    
    // Optimización de Flujo de Datos: Uso de estadísticas pre-calculadas en ViewModel
    val budgetStatsMap by viewModel.budgetStats.collectAsStateWithLifecycle()

    // Estados de UI y Multiselección
    val isMultiSelectionActive by viewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    // Estados para Sheets y Overlays (Centralizados para Sincronización de Be)
    var showCrearLicitacionSheet by remember { mutableStateOf(false) }
    var selectedTenderForSheet by remember { mutableStateOf<TenderEntity?>(null) }
    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var tenderForDetails by remember { mutableStateOf<TenderEntity?>(null) }
    
    // Estados para Analíticas
    var showAnalyticsOverlay by remember { mutableStateOf(false) }
    var budgetsToAnalyze by remember { mutableStateOf<List<BudgetEntity>>(emptyList()) }
    var tenderForAnalytics by remember { mutableStateOf<TenderEntity?>(null) }

    // Estados para Diálogos
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteContextMessage by remember { mutableStateOf("") }
    var onConfirmDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // 🔥 REGLA DE ORO: Sincronización del Contexto de Be
    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("presupuestos")
    }

    // 🔥 Sincronización de Multiselección (Elite SSOT)
    // El Cerebro solo reacciona al estado activo para el HUD, no replica los IDs.
    LaunchedEffect(isMultiSelectionActive) {
        beBrainViewModel.syncMultiSelection(isMultiSelectionActive)
    }

    // 🔥 Sincronización de Visibilidad de Sheets (Para ocultar BottomBar y Be Assistant)
    LaunchedEffect(selectedTenderForSheet, showCrearLicitacionSheet) {
        val isAnySheetVisible = selectedTenderForSheet != null || showCrearLicitacionSheet
        beBrainViewModel.setSheetVisible(isAnySheetVisible)
    }

    // 🔥 LIMPIEZA AL SALIR (HUD V5.1)
    DisposableEffect(Unit) {
        onDispose {
            beBrainViewModel.clearCustomActions(HUDContext.BUDGETS_TENDERS)
            beBrainViewModel.syncMultiSelection(false)
        }
    }

    // --- SECCIÓN: ACCIONES DE BE (HUD DINÁMICO POR IDS) ---
    val hudContext by viewModel.currentHUDContext.collectAsStateWithLifecycle()
    val budgetActionIds by viewModel.beActionIds.collectAsStateWithLifecycle()

    LaunchedEffect(budgetActionIds, hudContext) {
        beBrainViewModel.setCustomActionIds(budgetActionIds, HUDContext.BUDGETS_TENDERS)
    }

    // --- SECCIÓN: CAPTURA DE EVENTOS DEL CEREBRO (BeBrain) ---
    LaunchedEffect(beBrainViewModel) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "licit" -> {
                    showCrearLicitacionSheet = true
                    beBrainViewModel.cerrarBeAssistantCompleto()
                }
                "select_all" -> viewModel.selectAll(tenders.map { it.tenderId })
                "view_tender_details" -> {
                    val tenderId = selectedItemIds.firstOrNull()
                    tenders.find { it.tenderId == tenderId }?.let {
                        tenderForDetails = it
                    }
                    viewModel.updateMultiSelection(false)
                }
                "compare_selected" -> {
                    val selectedBudgets = allBudgetsData.filter { it.budgetId in selectedItemIds }
                    if (selectedBudgets.isNotEmpty()) {
                        budgetsToAnalyze = selectedBudgets
                        tenderForAnalytics = tenders.find { it.tenderId == selectedBudgets.first().tenderId }
                        showAnalyticsOverlay = true
                    }
                }
                "compare_all" -> {
                    val currentTenderId = viewModel.selectedIds.value.firstOrNull() 
                    val tender = tenders.find { it.tenderId == currentTenderId }
                    val budgetsForThisTender = allBudgetsData.filter { it.tenderId == currentTenderId }
                    
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

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val budgetsForSelectedTender by remember(selectedTenderForSheet) {
        if (selectedTenderForSheet != null) viewModel.getFilteredBudgetsForTender(selectedTenderForSheet!!.tenderId)
        else MutableStateFlow(emptyList())
    }.collectAsStateWithLifecycle()

    var providerForA4 by remember { mutableStateOf<Provider?>(null) }
    
    LaunchedEffect(budgetForA4Preview) {
        if (budgetForA4Preview != null) {
            providerForA4 = viewModel.getProviderById(budgetForA4Preview!!.providerId)
        }
    }

    PresupuestosScreenContent(
        tenders = tenders,
        categories = categories,
        activeFilters = activeFilters,
        activeSorts = activeSortCriteria,
        filterDropdownItems = filterDropdownItems,
        sortDropdownItems = sortDropdownItems,
        shortcuts = shortcuts,
        onFilterToggle = { id -> 
            when (id) {
                "CLEAR_STATUS" -> viewModel.clearSpecificFilters(listOf("filter_tender_"))
                "CLEAR_CATEGORIES" -> viewModel.clearSpecificFilters(listOf("cat_"))
                "CLEAR_ALL" -> viewModel.toggleFilter("CLEAR_ALL")
                else -> viewModel.toggleFilter(id)
            }
        },
        onManageShortcuts = { id, add -> viewModel.manageShortcut(id, add) },
        onChatClick = onChatClick,
        onBack = onBack,
        bottomPadding = bottomPadding, 
        isMultiSelectionActive = isMultiSelectionActive,
        selectedItemIds = selectedItemIds,
        onToggleItemSelection = { viewModel.toggleSelection(it) },
        onToggleMultiSelection = { viewModel.updateMultiSelection(!isMultiSelectionActive) },
        tenderForDetails = tenderForDetails,
        onViewTenderDetails = { tenderForDetails = it },
        onCloseTenderDetails = { tenderForDetails = null },
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        deleteContextMessage = deleteContextMessage,
        onConfirmDeleteAction = onConfirmDeleteAction,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onUpdateTenderStatus = { id, status -> viewModel.updateTenderStatus(id, status) },
        showAnalyticsOverlay = showAnalyticsOverlay,
        onCloseAnalytics = { showAnalyticsOverlay = false },
        tenderForAnalytics = tenderForAnalytics,
        budgetsToAnalyze = budgetsToAnalyze,
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            scope.launch {
                delay(1500)
                isRefreshing = false
            }
        },
        selectedTenderForSheet = selectedTenderForSheet,
        onTenderClick = { tender ->
            if (isMultiSelectionActive) viewModel.toggleSelection(tender.tenderId)
            else {
                selectedTenderForSheet = tender
                viewModel.setSelectedTenderId(tender.tenderId)
            }
        },
        onCloseTenderSheet = {
            selectedTenderForSheet = null
            viewModel.setSelectedTenderId(null)
            viewModel.setContext(HUDContext.BUDGETS)
        },
        budgetForA4Preview = budgetForA4Preview,
        onOpenBudgetPreview = { budgetForA4Preview = it },
        onCloseBudgetPreview = { budgetForA4Preview = null },
        providerForA4 = providerForA4,
        onAcceptBudget = { viewModel.acceptBudget(it) },
        onRejectBudget = { viewModel.rejectBudget(it) },
        budgetStatsMap = budgetStatsMap,
        budgetsForSelectedTender = budgetsForSelectedTender
    )

    CrearLicitacionSheet(
        isVisible = showCrearLicitacionSheet,
        onClose = { 
            showCrearLicitacionSheet = false
            beBrainViewModel.onRouteChanged("presupuestos") 
        },
        onSuccess = {
            showCrearLicitacionSheet = false
            beBrainViewModel.onRouteChanged("presupuestos")
        },
        beViewModel = beBrainViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresupuestosScreenContent(
    tenders: List<TenderEntity>,
    categories: List<CategoryEntity>,
    activeFilters: Set<String>,
    activeSorts: List<String> = emptyList(),
    filterDropdownItems: List<DropdownItemData> = emptyList(),
    sortDropdownItems: List<DropdownItemData> = emptyList(),
    shortcuts: List<FilterSortItem> = emptyList(),
    onFilterToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit = { _, _ -> },
    onChatClick: (String, String?) -> Unit,
    onBack: () -> Unit,
    bottomPadding: PaddingValues, 
    isMultiSelectionActive: Boolean,
    selectedItemIds: Set<String>,
    onToggleItemSelection: (String) -> Unit,
    onToggleMultiSelection: () -> Unit,
    tenderForDetails: TenderEntity?,
    onViewTenderDetails: (TenderEntity) -> Unit,
    onCloseTenderDetails: () -> Unit,
    showDeleteConfirmDialog: Boolean,
    deleteContextMessage: String,
    onConfirmDeleteAction: (() -> Unit)?,
    onDismissDeleteDialog: () -> Unit,
    onUpdateTenderStatus: (String, String) -> Unit,
    showAnalyticsOverlay: Boolean,
    onCloseAnalytics: () -> Unit,
    tenderForAnalytics: TenderEntity?,
    budgetsToAnalyze: List<BudgetEntity>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    selectedTenderForSheet: TenderEntity?,
    onTenderClick: (TenderEntity) -> Unit,
    onCloseTenderSheet: () -> Unit,
    budgetForA4Preview: BudgetEntity?,
    onOpenBudgetPreview: (BudgetEntity) -> Unit,
    onCloseBudgetPreview: () -> Unit,
    providerForA4: Provider? = null,
    onAcceptBudget: (BudgetEntity) -> Unit = {},
    onRejectBudget: (BudgetEntity) -> Unit = {},
    budgetStatsMap: Map<String, TenderStats> = emptyMap(),
    budgetsForSelectedTender: List<BudgetEntity> = emptyList()
) {
    val tenderListState = rememberLazyListState()
    val refreshState = rememberPullToRefreshState()
    
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    // 🔥 [ELITE] Reset scroll to top when tenders list changes (Filter/Sort/Update)
    LaunchedEffect(tenders) {
        if (tenders.isNotEmpty()) {
            tenderListState.animateScrollToItem(0)
        }
    }

    val isDateSortActive = activeFilters.contains("sort_date")

    // --- LÓGICA DE AGRUPACIÓN POR FECHAS ---
    val groupedTenders = remember(tenders, isDateSortActive) {
        if (!isDateSortActive) mapOf("" to tenders)
        else {
            val formatter = SimpleDateFormat("dd MMMM", Locale.getDefault())
            val now = Calendar.getInstance()
            tenders.groupBy { 
                val time = Calendar.getInstance().apply { timeInMillis = it.startDate }
                when {
                    now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR) -> "Hoy"
                    now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - 1 == time.get(Calendar.DAY_OF_YEAR) -> "Ayer"
                    else -> formatter.format(time.time)
                }
            }
        }
    }

    // Estado de expansión de grupos
    val expandedGroups = remember { mutableStateMapOf<String, Boolean>().apply {
        groupedTenders.keys.forEach { put(it, true) } 
    } }

    // --- LÓGICA DE CABECERA REACTIVA ---
    val currentVisibleDate by remember {
        derivedStateOf {
            if (!isDateSortActive) "${tenders.size} LICITACIONES ENCONTRADAS"
            else {
                val firstVisibleIndex = tenderListState.firstVisibleItemIndex
                var count = 0
                var foundDate = ""
                for ((date, items) in groupedTenders) {
                    if (date.isNotEmpty()) count++
                    if (firstVisibleIndex < count + items.size) {
                        foundDate = date.ifEmpty { "Licitaciones" }
                        break
                    }
                    if (expandedGroups[date] == true) count += items.size
                }
                foundDate.ifEmpty { "CONCURSOS" }.uppercase()
            }
        }
    }

    // --- LÓGICA DE SCROLL Y COLAPSO DE TARJETAS (Elite V5 - Double Phase) ---
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // [Fase 1 + 2] Acumulamos hasta que todo colapse (Filtros: 180dp + Header: 250dp = 430dp)
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 330f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll

                // Si estamos colapsados al máximo y scrolleamos hacia abajo (delta < 0), dejamos que la lista consuma
                return if (scrollAccumulator >= 330f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val cardsHideFraction = remember {
        // Fase 1: Los filtros se ocultan en los primeros 180dp
        derivedStateOf { (scrollAccumulator / 80f).coerceIn(0f, 1f) }
    }

    val collapseFraction = remember {
        // Fase 2: La cabecera colapsa después de los filtros (de 180dp a 430dp)
        derivedStateOf { ((scrollAccumulator - 80f) / 250f).coerceIn(0f, 1f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg) 
            .nestedScroll(nestedScrollConnection)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = bottomPadding.calculateBottomPadding()), 
            topBar = {
                val visuals = BeDictionary.Contexts["presupuestos"]!!
                BarraCabezera(
                    title = visuals.title,
                    subtitle = visuals.subtitle,
                    emoji = visuals.emoji,
                    onBack = onBack,
                    onInfoClick = { },
                    accentColor = visuals.accentColor,
                    collapseFraction = collapseFraction.value,
                    infoTitle = "SISTEMA DE LICITACIONES",
                    infoDescription = "Aquí puedes gestionar tus concursos abiertos, comparar presupuestos de diferentes profesionales y adjudicar el proyecto al mejor postor."
                )
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = refreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = refreshState,
                        isRefreshing = isRefreshing,
                        color = MaverickBlue,
                        containerColor = MaverickColors.ROG_Dark_Bg.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                    // Spacer dinámico: se reduce a 0 junto con los filtros para unión perfecta
                    Spacer(modifier = Modifier.height(8.dp * (1f - cardsHideFraction.value)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                alpha = 1f - cardsHideFraction.value
                                translationY = -20.dp.toPx() * cardsHideFraction.value
                            }
                            .height(90.dp * (1f - cardsHideFraction.value)),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoldePremiumFilterCard(
                            label = "Filtrar por",
                            dropdownItems = filterDropdownItems,
                            shortcutItems = shortcuts,
                            activeFilters = activeFilters,
                            onToggle = onFilterToggle,
                            onManageShortcuts = onManageShortcuts,
                            onExpandChanged = { isFilterMenuExpanded = it },
                            modifier = Modifier.weight(1f)
                        )

                        AnimatedVisibility(
                            visible = !isFilterMenuExpanded,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut()
                        ) {
                            MoldePremiumSortCard(
                                label = "Ordenar por",
                                dropdownItems = sortDropdownItems,
                                shortcutItems = emptyList(),
                                activeSorts = activeSorts,
                                onToggle = onFilterToggle,
                                onManageShortcuts = { _, _ -> }
                            )
                        }
                    }
                }
                    Spacer(modifier = Modifier.height(8.dp * (1f - cardsHideFraction.value)))
                ListaMoldeV2(
                    modifier = Modifier.weight(1f),
                    titulo = "CONCURSOS PÚBLICOS",
                    subtitulo = null, // Minimalismo Elite
                    compactInfo = currentVisibleDate,
                    itemCount = tenders.size,
                    customMaxHeaderHeight = 44.dp,
                    customMinHeaderHeight = 40.dp,
                    accentColor = MaverickBlue,
                    acciones = {
                        // Botón Limpiar Filtros - Estilo Elite (Sincronizado con Categorías)
                        if (activeFilters.isNotEmpty()) {
                            BotonCabeceraAccion(
                                onClick = { onFilterToggle("CLEAR_ALL") },
                                icon = Icons.Default.FilterAlt,
                                color = MaverickColors.MagentaNeon
                            )
                        }
                    }
                ) {
                    if (tenders.isEmpty() && activeFilters.isNotEmpty()) {
                        item {
                            EmptyFiltersState(
                                activeFilters = activeFilters,
                                filterDropdownItems = filterDropdownItems,
                                sortDropdownItems = sortDropdownItems,
                                onClearFilters = { onFilterToggle("CLEAR_ALL") }
                            )
                        }
                    } else {
                        groupedTenders.forEach { (date, items) ->
                            if (date.isNotEmpty()) {
                                item(key = "sep_$date") {
                                    SeparadorFechaPremium(
                                        fecha = date,
                                        isExpanded = expandedGroups[date] ?: true,
                                        onToggle = { expandedGroups[date] = !(expandedGroups[date] ?: true) }
                                    )
                                }
                            }

                            if (expandedGroups[date] ?: true) {
                                items(items, key = { it.tenderId }) { tender ->
                                    val stats = budgetStatsMap[tender.tenderId] ?: TenderStats(0, 0)
                                    val categoryInfo = categories.find { it.name.equals(tender.category, ignoreCase = true) }
                                    val superCatColor = remember(categoryInfo) {
                                        Color(CategoryVisuals.getColorFor(categoryInfo?.superCategory))
                                    }

                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                        LicitacionFolderPremium(
                                            title = tender.title,
                                            category = tender.category,
                                            categoryIcon = categoryInfo?.icon ?: "📋",
                                            supercategoryColor = superCatColor,
                                            tenderId = tender.tenderId,
                                            status = tender.status,
                                            startDate = tender.startDate,
                                            endDate = tender.endDate,
                                            budgetCount = stats.totalCount,
                                            unreadCount = stats.unreadCount,
                                            isSelected = selectedItemIds.contains(tender.tenderId),
                                            awardedProviderName = tender.awardedProviderName,
                                            awardedBudgetId = tender.awardedBudgetId,
                                            awardedProviderPhotoUrl = tender.awardedProviderPhotoUrl,
                                            onViewDetails = { onViewTenderDetails(tender) },
                                            onClick = { onTenderClick(tender) },
                                            onLongClick = {
                                                if (!isMultiSelectionActive) onToggleMultiSelection()
                                                onToggleItemSelection(tender.tenderId)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        }

        PresupuestoAdministradorSheet(
            isVisible = selectedTenderForSheet != null,
            onClose = onCloseTenderSheet,
            count = budgetsForSelectedTender.size,
            title = "PRESUPUESTOS EN LICITACIONES",
            helperText = "ADMINISTRADOR DE",
            tenderName = selectedTenderForSheet?.title ?: "LICITACIÓN",
            filterOptions = filterDropdownItems.map { BudgetFilterSortItem(it.id, it.label, it.emoji ?: "🔹") },
            sortOptions = sortDropdownItems.map { BudgetFilterSortItem(it.id, it.label, it.emoji ?: "🔹") },
            selectedFilter = activeFilters.find { !it.startsWith("sort_") },
            selectedSort = activeFilters.find { it.startsWith("sort_") },
            onFilterSelect = onFilterToggle,
            onSortSelect = onFilterToggle,
            budgets = budgetsForSelectedTender,
            onBudgetClick = { budget ->
                onOpenBudgetPreview(budget)
            }
        )

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
            val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
            val creationDate = dateFormat.format(Date(tenderForDetails.dateTimestamp))
            val endDate = if (tenderForDetails.endDate > 0) dateFormat.format(Date(tenderForDetails.endDate)) else "No definida"
            var showCancelWarning by remember { mutableStateOf(false) }

            PopUpEmergenteMolde(
                isVisible = true,
                onDismissRequest = onCloseTenderDetails,
                title = tenderForDetails.title,
                subtitle = "📋 DETALLES TÉCNICOS",
                accentColor = MaverickBlue,
                headerExtra = {
                    StatusPillPremium(tenderForDetails.status)
                    Surface(
                        color = Color.White.copy(0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Text(
                            text = "🏷️ ${tenderForDetails.category.uppercase()}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) {
                // --- SECCIÓN: DESCRIPCIÓN ---
                PopUpSectionHeader("Memoria Descriptiva", emoji = "📝")
                PopUpDetailSection("📄", "Detalle", tenderForDetails.description)
                Spacer(Modifier.height(20.dp))

                // --- SECCIÓN: UBICACIÓN ---
                if (tenderForDetails.locationAddress != null) {
                    PopUpSectionHeader("Ubicación", emoji = "📍")
                    PopUpDetailSection(
                        "🗺️", 
                        "Dirección",
                        "${tenderForDetails.locationAddress} ${tenderForDetails.locationNumber ?: ""}, ${tenderForDetails.locationLocality ?: ""}"
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // --- GALERÍA DE IMÁGENES ---
                PopUpImageGallery(tenderForDetails.imageUrls)
                Spacer(Modifier.height(20.dp))

                // --- REQUISITOS FORMALES ---
                if (tenderForDetails.requiresVisit || tenderForDetails.requiresPaymentMethod || tenderForDetails.requiresWorkGuarantee || tenderForDetails.requiresProviderDoc) {
                    PopUpSectionHeader("Cláusulas y Requisitos", emoji = "⚖️")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (tenderForDetails.requiresVisit) PopUpRequirementChip("🛠️ Visita Técnica Obra")
                        if (tenderForDetails.requiresPaymentMethod) PopUpRequirementChip("💳 Método de Pago")
                        if (tenderForDetails.requiresWorkGuarantee) PopUpRequirementChip("🛡️ Garantía de Obra")
                        if (tenderForDetails.requiresProviderDoc) PopUpRequirementChip("📄 Documentación Legal")
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // --- FECHAS CRÍTICAS ---
                PopUpSectionHeader("Cronograma", emoji = "📅")
                Surface(
                    color = Color.Black.copy(0.3f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PopUpDateItem("Inicio", creationDate)
                        Box(Modifier.width(1.dp).height(30.dp).background(Color.White.copy(0.1f)))
                        PopUpDateItem("Cierre", endDate)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- SECCIÓN DE ADJUDICACIÓN ---
                val awardedName = tenderForDetails.awardedProviderName
                if (tenderForDetails.status == "ADJUDICADA" && awardedName != null) {
                    PopUpSectionHeader("Resultado de Licitación", emoji = "🏆")
                    PopUpProviderCard(
                        name = awardedName,
                        photoUrl = tenderForDetails.awardedProviderPhotoUrl,
                        companyName = "Prestador Adjudicado",
                        onChatClick = { onChatClick(tenderForDetails.awardedProviderId ?: "", null) }
                    )
                }

                // --- BOTONES DE ACCIÓN ---
                if (tenderForDetails.status == "ABIERTA") {
                    Button(
                        onClick = { showCancelWarning = true },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f)),
                        border = BorderStroke(1.dp, Color.Red.copy(0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("❌ CANCELAR LICITACIÓN", color = Color.Red, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }

            if (showCancelWarning) {
                AlertDialog(
                    onDismissRequest = { showCancelWarning = false },
                    containerColor = Color(0xFF1A1C1E),
                    icon = { Icon(Icons.Default.Warning, null, tint = Color.Yellow, modifier = Modifier.size(40.dp)) },
                    title = { Text("¿Terminar de manera abrupta?", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = { Text("Estás por CANCELAR esta licitación de forma forzosa.", color = Color.Gray, fontSize = 14.sp) },
                    confirmButton = {
                        Button(onClick = { onUpdateTenderStatus(tenderForDetails.tenderId, "CANCELADA"); onCloseTenderDetails() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("SÍ, CANCELAR", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = { TextButton(onClick = { showCancelWarning = false }) { Text("VOLVER", color = Color.Gray) } }
                )
            }
        }

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
                        onCloseAnalytics()
                        val budget = budgetsToAnalyze.find { it.budgetId == bId }
                        if (budget != null) onOpenBudgetPreview(budget)
                    }
                )
            }
        }
        
        if (budgetForA4Preview != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Dialog(onDismissRequest = onCloseBudgetPreview, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                BudgetA4Viewer(
                    prestador = providerForA4?.toEntity(), 
                    budget = budgetForA4Preview, 
                    onDismiss = onCloseBudgetPreview,
                    clientName = "Cliente"
                ) { _, _ ->
                    // Acciones del Cliente: Aceptar / Rechazar
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (budgetForA4Preview.status == com.example.myapplication.core.data.local.entity.BudgetStatus.PENDIENTE) {
                            OutlinedButton(
                                onClick = { onRejectBudget(budgetForA4Preview); onCloseBudgetPreview() },
                                modifier = Modifier.height(42.dp),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
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
                            StatusPillPremium(budgetForA4Preview.status.name)
                        }
                    }
                }
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
            activeSorts = emptyList(),
            filterDropdownItems = emptyList(),
            sortDropdownItems = emptyList(),
            shortcuts = emptyList(),
            onFilterToggle = { _ -> },
            onManageShortcuts = { _, _ -> },
            onChatClick = { _, _ -> },
            onBack = {},
            bottomPadding = PaddingValues(0.dp), 
            isMultiSelectionActive = false,
            selectedItemIds = emptySet(),
            onToggleItemSelection = {},
            onToggleMultiSelection = {},
            tenderForDetails = null,
            onViewTenderDetails = {},
            onCloseTenderDetails = {},
            showDeleteConfirmDialog = false,
            deleteContextMessage = "",
            onConfirmDeleteAction = {},
            onDismissDeleteDialog = {},
            onUpdateTenderStatus = { _, _ -> },
            showAnalyticsOverlay = false,
            onCloseAnalytics = {},
            tenderForAnalytics = null,
            budgetsToAnalyze = emptyList(),
            isRefreshing = false,
            onRefresh = {},
            selectedTenderForSheet = null,
            onTenderClick = {},
            onCloseTenderSheet = {},
            budgetForA4Preview = null,
            onOpenBudgetPreview = {},
            onCloseBudgetPreview = {},
            budgetStatsMap = emptyMap(),
            budgetsForSelectedTender = sampleBudgets
        )
    }
}

package com.example.myapplication.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.TenderEntity
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.presentation.client.HUDContext
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

private val DarkBackground = Color(0xFF05070A)

@Composable
fun ResultadoLicitacionOverlay(
    selectedTender: TenderEntity?,
    onClose: () -> Unit,
    beBrainActionEvent: SharedFlow<String>,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String, String?) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    isMultiSelectionActive: Boolean,
    selectedItemIds: Set<String>,
    onToggleItemSelection: (String) -> Unit,
    onToggleMultiSelection: () -> Unit,
    onAnalyticsClick: (TenderEntity, List<BudgetEntity>) -> Unit,
    onDeleteBudgets: (Set<String>) -> Unit,
    onMarkAsReadMulti: (Set<String>) -> Unit = {},
    onSetContext: (HUDContext) -> Unit,
    showDeleteConfirmDialog: (String, () -> Unit) -> Unit,
    isAssistantActive: Boolean = false // NUEVO: Estado para desplazar la UI
) {
    // Backup para mantener la UI estable durante la animación de salida
    var lastSelectedTenderForExit by remember { mutableStateOf<TenderEntity?>(null) }
    if (selectedTender != null) {
        lastSelectedTenderForExit = selectedTender
    }

    LaunchedEffect(selectedTender) {
        if (selectedTender != null) {
            onSetContext(HUDContext.TENDER_DETAILS)
        }
    }

    // Al cerrar, debemos restaurar el contexto (usando DisposableEffect)
    DisposableEffect(selectedTender) {
        onDispose {
            if (selectedTender == null) {
                // Solo si realmente se cerró, volvemos al estado de lista
                onSetContext(HUDContext.BUDGETS_TENDERS)
            }
        }
    }

    AnimatedVisibility(
        visible = selectedTender != null,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(450, easing = FastOutSlowInEasing)) + fadeOut(),
        modifier = Modifier.zIndex(50f)
    ) {
        lastSelectedTenderForExit?.let { tender ->
            BackHandler(enabled = isMultiSelectionActive || selectedTender != null) {
                if (isMultiSelectionActive) {
                    onToggleMultiSelection()
                } else {
                    onClose()
                }
            }

            val tenderBudgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
            val budgets by tenderBudgetsFlow.collectAsStateWithLifecycle(emptyList())

            LaunchedEffect(tender, budgets, selectedItemIds) {
                beBrainActionEvent.collect { actionId: String ->
                    when (actionId) {
                        "compare_all" -> {
                            onAnalyticsClick(tender, budgets.sortedBy { it.providerName.lowercase(Locale.getDefault()) })
                        }
                        "compare_selected" -> {
                            val selectedBudgets = budgets.filter { it.budgetId in selectedItemIds }
                            if (selectedBudgets.isNotEmpty()) {
                                onAnalyticsClick(tender, selectedBudgets.sortedBy { it.providerName.lowercase(Locale.getDefault()) })
                            }
                        }
                        "delete_selected" -> {
                            showDeleteConfirmDialog("¿Deseas eliminar las ofertas seleccionadas de esta licitación?") {
                                onDeleteBudgets(selectedItemIds)
                            }
                        }
                        "select_all" -> {
                            // select_all se maneja en PresupuestosScreen para sincronizar con el ViewModel
                        }
                        "mark_as_read" -> {
                            if (selectedItemIds.isNotEmpty()) {
                                onMarkAsReadMulti(selectedItemIds)
                            }
                        }
                    }
                }
            }

            // Lógica de búsqueda y ordenamiento centralizada en el ViewModel
            // No filtramos localmente para mantener la "Única Fuente de Verdad"
            val sortedAndFilteredBudgets = budgets


            ComparisonSheetEdgeToEdge(
                tender = tender,
                budgets = sortedAndFilteredBudgets,
                activeFilters = activeFilters,
                dynamicCategories = dynamicCategories,
                refinementFilters = refinementFilters,
                sortOptions = sortOptions,
                onFilterToggle = onFilterToggle,
                onClearFilters = onClearFilters,
                onClearSort = onClearSort,
                onBack = {
                    if (isMultiSelectionActive) {
                        onToggleMultiSelection()
                    } else {
                        onClose()
                    }
                },
                onBudgetClick = onBudgetClick,
                onChatClick = onChatClick,
                onAvatarClick = { onAvatarClick(it) },
                isMultiSelectionActive = isMultiSelectionActive,
                selectedItemIds = selectedItemIds,
                onToggleItemSelection = onToggleItemSelection,
                onToggleMultiSelection = onToggleMultiSelection,
                onAnalyticsClick = onAnalyticsClick,
                isAssistantActive = isAssistantActive
            )
        }
    }
}

@Composable
fun ComparisonSheetEdgeToEdge(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    activeFilters: Set<String>,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem>,
    sortOptions: List<ControlItem>,
    onFilterToggle: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClearSort: () -> Unit,
    onBack: () -> Unit,
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String, String?) -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onToggleMultiSelection: () -> Unit = {},
    onAnalyticsClick: (TenderEntity, List<BudgetEntity>) -> Unit = { _, _ -> },
    isAssistantActive: Boolean = false
) {
    // ==========================================================================================
    // --- SECCIÓN 1: ESTRUCTURA PRINCIPAL (MOLDE BARRA MENÚ) ---
    // Se configura para mostrar el título de la licitación y su categoría.
    // Se oculta la caja de conteo para usar el estilo de texto elegante.
    // ==========================================================================================
    
    // --- ESTRUCTURA PRINCIPAL DEL OVERLAY ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // [SECCIÓN: DESPLAZAMIENTO DINÁMICO]
        // Si el asistente (BeBrain) está activo, bajamos toda la interfaz 95.dp para no tapar la barra de búsqueda.
        if (isAssistantActive) {
            Spacer(modifier = Modifier.height(170.dp))
        } else {
            Spacer(modifier = Modifier.statusBarsPadding())
        }
        
        // ==========================================================================================
        // --- SECCIÓN 1: CONTENEDOR TÁCTICO (MOLDE BARRA MENÚ) ---
        // Este componente envuelve tanto la cabecera personalizada como el contenido de la grilla.
        // ==========================================================================================
        MoldeBarraMenu(
            modifier = Modifier.weight(1f),
            showCountBox = false, // Usamos estilo elegante de texto en lugar de caja de conteo
            labelCountMain = tender.title.uppercase(), // Título de la licitación
            labelCountSub = "${tender.category} 🏷️", // Categoría como subtítulo
            showSuscritos = false,
            showCercania = false,
            showVista = false,
            activeRefinements = activeFilters,
            refinementOptions = refinementFilters,
            sortOptions = sortOptions,
            onToggleRefinement = onFilterToggle,
            onClearRefinements = onClearFilters,
            onClearSort = onClearSort,
            customActions = {
                // --- ACCIONES DE CABECERA: FILTROS Y CIERRE ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // Menú de Filtros Táctico
                    MenuFiltros(
                        activeFilters = activeFilters,
                        dynamicCategories = dynamicCategories,
                        refinementFilters = refinementFilters,
                        onAction = onFilterToggle,
                        onApply = {},
                        onClearFilters = onClearFilters
                    )

                    // Menú de Ordenamiento (si aplica)
                    if (sortOptions.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        MenuOrdenamiento(
                            activeFilters = activeFilters,
                            sortOptions = sortOptions,
                            onAction = onFilterToggle,
                            onApply = {},
                            onClearFilters = onClearSort
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Botón de cierre "X" Estilo Maverick Tactical
                    MaverickTacticalButton(
                        isActive = false,
                        accentColor = Color.Red.copy(alpha = 0.7f),
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        ) {
            // ==========================================================================================
            // --- SECCIÓN: CABECERA DE REQUISITOS (SOLO PARA LICITACIONES) ---
            // ==========================================================================================
            if (tender.requiresVisit || tender.requiresWorkGuarantee || tender.requiresProviderDoc) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tender.requiresVisit) RequirementBadge("Visita", Icons.Default.Build)
                    if (tender.requiresWorkGuarantee) RequirementBadge("Garantía", Icons.Default.Build)
                    if (tender.requiresProviderDoc) RequirementBadge("Docs", Icons.Default.Build)
                }
            }

            // ==========================================================================================
            // --- SECCIÓN 2: PANEL DE CONTENIDO (GRID DE PRESUPUESTOS) ---
            // El contenido ahora vive dentro del Molde para heredar el fondo Glass y bordes ROG.
            // ==========================================================================================
            DividerPremium() // Separador visual entre cabecera y contenido
            
            BudgetGridContent(
                tender = tender,
                budgets = budgets,
                isMultiSelectionActive = isMultiSelectionActive,
                selectedItemIds = selectedItemIds,
                onToggleItemSelection = onToggleItemSelection,
                onBudgetClick = onBudgetClick,
                onChatClick = onChatClick,
                onToggleMultiSelection = onToggleMultiSelection,
                onAvatarClick = onAvatarClick
            )
        }
    }
}


@Composable
fun RequirementBadge(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun BudgetGridContent(
    state: LazyGridState = rememberLazyGridState(),
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String, String?) -> Unit,
    onToggleMultiSelection: () -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin ofertas registradas", color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    } else {
        val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")) }
        
        // Agrupar por fecha y ordenar dentro de cada grupo (no leídos primero)
        val groupedBudgets = remember(budgets) {
            budgets.groupBy {
                dateFormatter.format(Date(it.dateTimestamp))
            }.mapValues { entry ->
                // Ordenar: isRead = false primero, luego por timestamp descendente
                entry.value.sortedWith(compareBy<BudgetEntity> { it.isRead }.thenByDescending { it.dateTimestamp })
            }.toList().sortedByDescending { it.second.first().dateTimestamp }
        }

        val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

        LazyVerticalGrid(
            state = state,
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groupedBudgets.forEach { (dateText, budgetsInDate) ->
                val isExpanded = expandedStates[dateText] ?: true
                
                // Header de Fecha con Divider Premium y Flecha
                item(span = { GridItemSpan(maxLineSpan) }, key = "header_$dateText") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { 
                                expandedStates[dateText] = !isExpanded 
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DividerPremium(modifier = Modifier.weight(1f))
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateText.uppercase(),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        DividerPremium(modifier = Modifier.weight(1f))
                    }
                }

                if (isExpanded) {
                    items(budgetsInDate, key = { it.budgetId }) { budget ->
                        TarjetaPresupuestoPremium(
                            providerName = budget.providerName,
                            companyName = budget.providerCompanyName ?: "Independiente",
                            amount = budget.grandTotal,
                            budgetId = budget.budgetId,
                            photoUrl = budget.providerPhotoUrl,
                            isOnline = true,
                            isSubscribed = true,
                            isSelected = selectedItemIds.contains(budget.budgetId),
                            isRead = budget.isRead,
                            isMultiSelectionActive = isMultiSelectionActive,
                            onViewClick = { onBudgetClick(budget) },
                            onChatClick = { onChatClick(budget.providerId, budget.category ?: tender.category) },
                            onAvatarClick = { onAvatarClick(budget) },
                            onLongClick = {
                                if (!isMultiSelectionActive) {
                                    onToggleMultiSelection()
                                }
                                onToggleItemSelection(budget.budgetId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DividerPremium (modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun ResultadoLicitacionOverlayPreview() {
    val sampleTender = TenderEntity(
        tenderId = "tender_123",
        title = "Instalación de Aire Acondicionado",
        clientId = "client_abc",
        description = "Se requiere instalar un aire acondicionado split de 3000 frigorías.",
        category = "Climatización",
        dateTimestamp = System.currentTimeMillis()
    )

    val sampleBudgets = listOf(
        BudgetEntity(
            budgetId = "b1",
            clientId = "client_abc",
            providerId = "p1",
            tenderId = "tender_123",
            providerName = "Juan Clima",
            providerCompanyName = "Servicios JC",
            grandTotal = 4500.0,
            isRead = false,
            dateTimestamp = System.currentTimeMillis()
        ),
        BudgetEntity(
            budgetId = "b2",
            clientId = "client_abc",
            providerId = "p2",
            tenderId = "tender_123",
            providerName = "Marta Frío",
            providerCompanyName = "Marta Services",
            grandTotal = 4200.0,
            isRead = true,
            dateTimestamp = System.currentTimeMillis() - 86400000
        )
    )

    val sampleControls = listOf(
        ControlItem("Económico", null, "💰", Color.Green),
        ControlItem("Rápido", null, "⚡", Color.Yellow),
        ControlItem("Garantía", null, "🛡️", Color.Blue)
    )

    MyApplicationTheme(darkTheme = true) {
        ResultadoLicitacionOverlay(
            selectedTender = sampleTender,
            onClose = {},
            beBrainActionEvent = MutableSharedFlow(),
            getBudgetsForTender = { _ -> MutableStateFlow(sampleBudgets) },
            activeFilters = emptySet(),
            dynamicCategories = sampleControls,
            refinementFilters = sampleControls,
            sortOptions = sampleControls,
            onFilterToggle = {},
            onClearFilters = {},
            onClearSort = {},
            onBudgetClick = {},
            onChatClick = { _, _ -> },
            onAvatarClick = {},
            isMultiSelectionActive = false,
            selectedItemIds = emptySet(),
            onToggleItemSelection = {},
            onToggleMultiSelection = {},
            onAnalyticsClick = { _, _ -> },
            onDeleteBudgets = {},
            onMarkAsReadMulti = {},
            onSetContext = {},
            showDeleteConfirmDialog = { _, _ -> }
        )
    }
}

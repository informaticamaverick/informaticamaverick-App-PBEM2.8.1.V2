
/**
package com.example.myapplication.presentation.client

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.entity.BudgetStatus
import com.example.myapplication.core.data.local.entity.TenderEntity
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.features.budget.BudgetViewModel
import com.example.myapplication.presentation.features.home.CategoryViewModel
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.features.budget.BudgetComparisonAnalytics
import com.example.myapplication.uishared.components.BudgetPreviewPDFDialog
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

// =================================================================================
// --- CONSTANTES DE DISEÑO ---
// =================================================================================
private val MaverickBlue = Color(0xFF2197F5)
private val CardSurface = Color(0xFF161C24)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPresupuestoRecibidosScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    onChatClick: (String) -> Unit = {},
    onBack: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val directBudgets by viewModel.filteredDirectBudgets.collectAsStateWithLifecycle()
    val categories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val searchQuery by beBrainViewModel.searchQuery.collectAsStateWithLifecycle()
    val isMultiSelectionActive by viewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedItemIds by viewModel.selectedIds.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        beBrainViewModel.coordinator.updateHUDContext(HUDContext.BUDGETS_DIRECT) 
    }

    val budgetActionIds by viewModel.beActionIds.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle(emptyList())

    var budgetForA4Preview by remember { mutableStateOf<BudgetEntity?>(null) }
    var providerForA4Preview by remember { mutableStateOf<Provider?>(null) }
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

    val hudContext by viewModel.currentHUDContext.collectAsStateWithLifecycle()
    val currentSelectedIds by rememberUpdatedState(selectedItemIds)

    LaunchedEffect(budgetActionIds, hudContext) {
        beBrainViewModel.setCustomActionIds(budgetActionIds)
    }

    val currentIdsToSelect by rememberUpdatedState(directBudgets.map { it.budgetId })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
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

    DisposableEffect(Unit) {
        onDispose {
            beBrainViewModel.clearCustomActions(HUDContext.BUDGETS_DIRECT)
            beBrainViewModel.syncMultiSelection(false, emptySet())
        }
    }

    Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } }) {
        ChatPresupuestoRecibidosScreenContent(
            directBudgets = directBudgets,
            categories = categories,
            onChatClick = onChatClick,
            onBack = onBack,
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
                    coroutineScope.launch {
                        providerForA4Preview = viewModel.getProviderById(budget.providerId)
                    }
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
            budgetsToAnalyze = budgetsToAnalyze,
            providerForA4Preview = providerForA4Preview
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPresupuestoRecibidosScreenContent(
    directBudgets: List<BudgetEntity>,
    categories: List<CategoryEntity>,
    onChatClick: (String) -> Unit,
    onBack: () -> Unit,
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
    budgetsToAnalyze: List<BudgetEntity>,
    providerForA4Preview: Provider? = null
) {
    val directListState = rememberLazyListState()

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

                val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
                
                ListaMoldeV2(
                    titulo = "PRESUPUESTOS",
                    subtitulo = "ADMINISTRACIÓN DIRECTA",
                    emoji = "📩",
                    compactInfo = "Recibidos",
                    itemCount = directBudgets.size,
                    accentColor = MaverickBlue,
                    state = directListState
                ) {
                    budgetGridListItems(
                        tender = TenderEntity(tenderId = "direct", title = "Directo", clientId = "", description = "", category = "Varios"),
                        budgets = directBudgets,
                        categories = categories,
                        isMultiSelectionActive = isMultiSelectionActive,
                        selectedItemIds = selectedItemIds,
                        onToggleItemSelection = onToggleItemSelection,
                        onBudgetClick = { budget ->
                            onBudgetClick(budget)
                            onMarkAsRead(budget.budgetId)
                        },
                        onChatClick = { providerId, _ -> onChatClick(providerId) },
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

        if (budgetForA4Preview != null && providerForA4Preview != null) {
            BudgetPreviewPDFDialog(
                prestador = ProviderEntity(
                    id = providerForA4Preview.uid,
                    email = providerForA4Preview.email,
                    phoneNumber = providerForA4Preview.phoneNumber,
                    displayName = providerForA4Preview.displayName,
                    name = providerForA4Preview.name,
                    lastName = providerForA4Preview.lastName,
                    matricula = providerForA4Preview.matricula,
                    profesion = providerForA4Preview.profesion,
                    categories = providerForA4Preview.categories,
                    createdAt = 0L
                ),
                budget = budgetForA4Preview,
                onDismiss = onCloseBudgetA4Preview,
                onEnviar = null,
                showSendButton = false
            )
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
                    onViewBudgetDetail = { _ ->
                        onCloseAnalytics()
                        // val budget = budgetsToAnalyze.find { it.budgetId == bId }
                        // Visor A4
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun ChatPresupuestoRecibidosScreenPreview() {
    MyApplicationTheme {
        val sampleBudgets = listOf(
            BudgetEntity("b1", "u1", "p1", "t1", "Maximiliano Nanterne", "Maverick Tech", null, grandTotal = 1500.0)
        )

        ChatPresupuestoRecibidosScreenContent(
            directBudgets = sampleBudgets,
            categories = emptyList(),
            onChatClick = {},
            onBack = {},
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
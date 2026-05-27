
/**
package com.example.myapplication.presentation.components

import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.presentation.features.auth.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.TenderEntity

import androidx.compose.ui.platform.LocalConfiguration
import com.example.myapplication.presentation.designsystem.components.MaverickTacticalButton
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

private val MaverickBlue = Color(0xFF2197F5)

@Composable
fun ResultadoLicitacionOverlay(
    selectedTender: TenderEntity?,
    onClose: () -> Unit,
    beBrainActionEvent: SharedFlow<String>,
    getBudgetsForTender: (String) -> StateFlow<List<BudgetEntity>>,
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
    categories: List<CategoryEntity> = emptyList()
) {
    // Backup para mantener la UI estable durante la animación de salida
    var lastSelectedTenderForExit by remember { mutableStateOf<TenderEntity?>(null) }
    
    val budgetGridState = rememberLazyListState()

    // Actualizamos el backup solo si hay una nueva licitación seleccionada
    if (selectedTender != null) {
        lastSelectedTenderForExit = selectedTender
    }

    LaunchedEffect(selectedTender) {
        if (selectedTender != null) {
            onSetContext(HUDContext.TENDER_DETAILS)
        }
    }

    lastSelectedTenderForExit?.let { tender ->

        val tenderBudgetsFlow = remember(tender.tenderId) { getBudgetsForTender(tender.tenderId) }
        val budgets by tenderBudgetsFlow.collectAsStateWithLifecycle(emptyList())

        val currentLocale = LocalConfiguration.current.locales[0]
        LaunchedEffect(tender, budgets, selectedItemIds, currentLocale) {
            beBrainActionEvent.collect { actionId: String ->
                when (actionId) {
                    "compare_all" -> {
                        onAnalyticsClick(tender, budgets.sortedBy { it.providerName.lowercase(currentLocale) })
                    }
                    "compare_selected" -> {
                        val selectedBudgets = budgets.filter { it.budgetId in selectedItemIds }
                        if (selectedBudgets.isNotEmpty()) {
                            onAnalyticsClick(tender, selectedBudgets.sortedBy { it.providerName.lowercase(currentLocale) })
                        }
                    }
                    "delete_selected" -> {
                        showDeleteConfirmDialog("¿Deseas eliminar las ofertas seleccionadas de esta licitación?") {
                            onDeleteBudgets(selectedItemIds)
                        }
                    }
                    "mark_as_read" -> {
                        if (selectedItemIds.isNotEmpty()) {
                            onMarkAsReadMulti(selectedItemIds)
                        }
                    }
                }
            }
        }

        // [NUEVO] ESTRUCTURA ELITE HUD BASADA EN SHEET EMERGENTE VERTICAL
        SheetEmergenteVertical(
            isVisible = selectedTender != null,
            onClose = onClose,
            title = tender.title,
            helperText = "Licitación ${tender.category}",
            emoji = "📩",
            topOffset = 150.dp,
            showActions = true,
            isScrollable = false, // 🔥 DESACTIVAMOS EL SCROLL PARA USAR LazyVerticalGrid INTERNO
            onAnimationFinished = {
                if (selectedTender == null) {
                    // Solo cuando la animación termina y no hay nada seleccionado, limpiamos backup
                    lastSelectedTenderForExit = null
                }
            },
            actions = {
                MaverickTacticalButton(
                    isActive = false,
                    accentColor = Color.Yellow,
                    onClick = { onAnalyticsClick(tender, budgets) }
                ) { Text("📊", fontSize = 16.sp) }
            }
        ) {
            // --- SECCIÓN: INFO DE TIEMPO Y REQUISITOS (Sincronización con LicitacionFolderPremium) ---
            val locale = LocalConfiguration.current.locales[0]
            val df = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateInfoRowEmoji("📅", "INICIO", df.format(Date(tender.startDate)))
                    DateInfoRowEmoji("🏁", "CIERRE", df.format(Date(tender.endDate)))
                }
                
                // Requisitos Rápidos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (tender.requiresVisit) RequirementBadge("Visita", Icons.Default.Build)
                    if (tender.requiresWorkGuarantee) RequirementBadge("Garantía", Icons.Default.Build)
                    if (tender.requiresProviderDoc) RequirementBadge("Docs", Icons.Default.Build)
                }
            }

            val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

            ListaMoldeV2(
                titulo = "OFERTAS RECIBIDAS",
                subtitulo = "ADMINISTRACIÓN TÁCTICA",
                emoji = "📩",
                compactInfo = "Licitación",
                itemCount = budgets.size,
                accentColor = MaverickBlue,
                customMaxHeaderHeight = 42.dp,
                state = budgetGridState // Compartimos el estado de scroll
            ) {
                budgetGridListItems(
                    tender = tender,
                    budgets = budgets,
                    categories = categories,
                    isMultiSelectionActive = isMultiSelectionActive,
                    selectedItemIds = selectedItemIds,
                    onToggleItemSelection = onToggleItemSelection,
                    onBudgetClick = onBudgetClick,
                    onChatClick = onChatClick,
                    onToggleMultiSelection = onToggleMultiSelection,
                    onAvatarClick = onAvatarClick,
                    expandedStates = expandedStates,
                    locale = locale
                )
            }
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

/**
 * 🔥 [ELITE HUD] EXTENSIÓN PARA LazyListScope
 * Permite aplanar la grilla de presupuestos dentro de una LazyColumn (ListaMoldeV2)
 * evitando el error de anidamiento de componentes con scroll vertical.
 */
fun LazyListScope.budgetGridListItems(
    tender: TenderEntity,
    budgets: List<BudgetEntity>,
    categories: List<CategoryEntity> = emptyList(),
    isMultiSelectionActive: Boolean = false,
    selectedItemIds: Set<String> = emptySet(),
    onToggleItemSelection: (String) -> Unit = {},
    onBudgetClick: (BudgetEntity) -> Unit,
    onChatClick: (String, String?) -> Unit,
    onToggleMultiSelection: () -> Unit,
    onAvatarClick: (BudgetEntity) -> Unit,
    expandedStates: MutableMap<String, Boolean>, // 🔥 Estado de expansión compartido
    locale: Locale
) {
    if (budgets.isEmpty()) {
        item {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("Sin ofertas registradas", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Formateador de fecha
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", locale)
    
    // Agrupar por fecha y ordenar
    val groupedBudgets = budgets.groupBy {
        dateFormatter.format(Date(it.dateTimestamp))
    }.mapValues { entry ->
        entry.value.sortedWith(compareBy<BudgetEntity> { it.isRead }.thenByDescending { it.dateTimestamp })
    }.toList().sortedByDescending { it.second.first().dateTimestamp }

    groupedBudgets.forEach { (dateText, budgetsInDate) ->
        val isExpanded = expandedStates[dateText] ?: true
        
        // Header de Fecha
        item(key = "header_$dateText") {
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
            // Aplanamos la grilla en filas de 3
            val chunkedBudgets = budgetsInDate.chunked(3)
            items(chunkedBudgets.size, key = { "row_${dateText}_$it" }) { rowIndex ->
                val rowBudgets = chunkedBudgets[rowIndex]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowBudgets.forEach { budget ->
                        val catIcon = categories.find { it.name.equals(budget.category, ignoreCase = true) }?.icon ?: "📋"
                        
                        TarjetaPresupuestoA4Document(
                            modifier = Modifier.weight(1f).height(180.dp),
                            budget = budget,
                            isSelected = selectedItemIds.contains(budget.budgetId),
                            isMultiSelectionActive = isMultiSelectionActive,
                            isInsideTender = true,
                            categoryEmoji = catIcon,
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
                    
                    // Rellenar espacios vacíos en la última fila para mantener el alineamiento
                    repeat(3 - rowBudgets.size) {
                        Spacer(modifier = Modifier.weight(1f))
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

    MyApplicationTheme(darkTheme = true) {
        ResultadoLicitacionOverlay(
            selectedTender = sampleTender,
            onClose = {},
            beBrainActionEvent = MutableSharedFlow(),
            getBudgetsForTender = { _ -> MutableStateFlow(sampleBudgets) },
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
**/









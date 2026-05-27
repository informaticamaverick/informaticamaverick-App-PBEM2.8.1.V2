package com.example.myapplication.presentation.features.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.lerp
import com.example.myapplication.presentation.components.TarjetaPresupuestoA4Document
import androidx.compose.ui.platform.LocalConfiguration
import com.example.myapplication.presentation.components.SheetEmergenteVertical
import com.example.myapplication.presentation.components.SheetCloseButton
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.DepthDividerVertical
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.ElevatedDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.shakeClick

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: SHEET ADMINISTRADOR DE PRESUPUESTOS (ELITE HUD) ---
 * ==========================================================================================
 * Panel unificado para la gestión de presupuestos en Licitaciones y Mensajes.
 * Cumple con el protocolo Maverick Elite SSOT y leyes de PasosIniciales.md.
 */
@Composable
fun PresupuestoAdministradorSheet(
    isVisible: Boolean,
    onClose: () -> Unit,
    count: Int,
    title: String = "PRESUPUESTOS EN LICITACIONES",
    helperText: String = "ADMINISTRADOR DE",
    tenderName: String = "HISTORIAL DE MENSAJES",
    filterOptions: List<BudgetFilterSortItem>,
    sortOptions: List<BudgetFilterSortItem>,
    selectedFilter: String?,
    selectedSort: String?,
    onFilterSelect: (String) -> Unit,
    onSortSelect: (String) -> Unit,
    budgets: List<com.example.myapplication.core.data.local.entity.BudgetEntity>, 
    onBudgetClick: (com.example.myapplication.core.data.local.entity.BudgetEntity) -> Unit
) {
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 150f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return Offset(0f, consumed)
            }
        }
    }
    
    // Fracción para el efecto de colapso de la cabecera
    val collapseFraction = (scrollAccumulator / 150f).coerceIn(0f, 1f)

    SheetEmergenteVertical(
        isVisible = isVisible,
        onClose = onClose,
        title = title,
        helperText = helperText,
        showEmoji = false,
        topOffset = 60.dp, 
        showTitle = false, 
        showHelperText = false, 
        showActions = false,
        isDraggable = true,
        isScrollable = false, 
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            
            // --- 1. CABECERA PERSONALIZADA ELITE (MAVERICK HUD) ---
            HeaderAdministrador(
                count = count,
                title = title,
                helperText = helperText,
                tenderName = tenderName,
                onClose = onClose,
                filterOptions = filterOptions,
                sortOptions = sortOptions,
                selectedFilter = selectedFilter,
                selectedSort = selectedSort,
                onFilterSelect = onFilterSelect,
                onSortSelect = onSortSelect,
                collapseFraction = collapseFraction
            )

            // --- 2. LISTA DE PRESUPUESTOS (GRILLA 3 COLUMNAS) ---
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(budgets) { budget ->
                        TarjetaPresupuestoA4Document(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            budget = budget,
                            onViewClick = { onBudgetClick(budget) },
                            onAvatarClick = { /* Opcional: Navegar a perfil */ }
                        )
                    }
                }
                
                // --- SOMBRA TÉCNICA (Overlay fijo al tope para efecto 3D) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(15.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                        .zIndex(10f)
                )
            }
        }
    }
}

/** 
 * SECCIÓN: Cabecera Elite con anatomía multi-fila y colapsable
 */
@Composable
private fun HeaderAdministrador(
    count: Int,
    title: String,
    helperText: String,
    tenderName: String,
    onClose: () -> Unit,
    filterOptions: List<BudgetFilterSortItem>,
    sortOptions: List<BudgetFilterSortItem>,
    selectedFilter: String?,
    selectedSort: String?,
    onFilterSelect: (String) -> Unit,
    onSortSelect: (String) -> Unit,
    collapseFraction: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.95f))
            .zIndex(5f)
    ) {
        // --- FILA 1: BRANDING & CONTROLES ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📄", fontSize = 28.sp)
            
            DepthDividerVertical(
                modifier = Modifier.padding(horizontal = 12.dp).height(30.dp),
                thickness = 1.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = helperText.uppercase(),
                    style = CyberTypography.MonospaceData.copy(
                        color = MaverickColors.ElectricPurple.copy(alpha = 0.8f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )
                AutoSizeText(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = tenderName.uppercase(),
                    style = CyberTypography.MonospaceData.copy(
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            SheetCloseButton(onClick = onClose)
        }

        // --- DIVIDER PROFUNDO ---
        DepthDividerHorizontal(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
            shadowColor = Color.Black.copy(alpha = 0.8f),
            highlightColor = Color.White.copy(alpha = 0.1f)
        )

        // --- FILA 2: STATS & ACCIONES (COLAPSABLE) ---
        if (collapseFraction < 0.8f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .graphicsLayer { 
                        alpha = 1f - collapseFraction 
                        translationY = -20.dp.toPx() * collapseFraction
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contador estilo M3
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "PRESUPUESTOS",
                        style = CyberTypography.MonospaceData.copy(fontSize = 6.sp, color = Color.Gray)
                    )
                    Surface(
                        color = MaverickColors.ElectricCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaverickColors.ElectricCyan.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = CyberTypography.MonospaceData.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Acciones: Filtros y Ordenamiento
                FiltrosVisiblesSection(
                    filterOptions = filterOptions,
                    sortOptions = sortOptions,
                    selectedFilter = selectedFilter,
                    selectedSort = selectedSort,
                    onFilterSelect = onFilterSelect,
                    onSortSelect = onSortSelect
                )
            }
        }

        // --- SEPARADOR 3D FINAL ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            ElevatedDividerHorizontal(
                shadowStartColor = Color.Black.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FiltrosVisiblesSection(
    filterOptions: List<BudgetFilterSortItem>,
    sortOptions: List<BudgetFilterSortItem>,
    selectedFilter: String?,
    selectedSort: String?,
    onFilterSelect: (String) -> Unit,
    onSortSelect: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- FILTROS ---
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filterOptions.forEach { item ->
                FilterActionChip(
                    item = item,
                    isSelected = selectedFilter == item.id,
                    onClick = { onFilterSelect(item.id) }
                )
            }
        }

        // --- ORDENAMIENTO ---
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sortOptions.forEach { item ->
                FilterActionChip(
                    item = item,
                    isSelected = selectedSort == item.id,
                    onClick = { onSortSelect(item.id) }
                )
            }
        }
    }
}

/**
 * Chip de acción con animación de vibración (shakeClick)
 */
@Composable
private fun FilterActionChip(
    item: BudgetFilterSortItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = if (isSelected) MaverickColors.ElectricCyan else Color.White.copy(alpha = 0.1f)
    
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .shakeClick { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = item.emoji, fontSize = 14.sp)
            Text(
                text = item.label.uppercase(),
                style = CyberTypography.MonospaceData.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}


// === DATA MODELS & UTILS ===

data class BudgetFilterSortItem(
    val id: String,
    val label: String,
    val emoji: String
)

// Eliminado BudgetPlaceholder a favor de BudgetEntity

// === PREVIEW ===

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PresupuestoAdministradorSheetPreview() {
    val filters = listOf(
        BudgetFilterSortItem("all", "Todos", "📂"),
        BudgetFilterSortItem("pending", "Pendientes", "⏳"),
        BudgetFilterSortItem("approved", "Aprobados", "✅")
    )
    
    val sorts = listOf(
        BudgetFilterSortItem("recent", "Recientes", "📅"),
        BudgetFilterSortItem("price", "Precio", "💰")
    )

    val budgets = List(12) { i ->
        com.example.myapplication.core.data.local.entity.BudgetEntity(
            budgetId = i.toString(),
            clientId = "cli",
            providerId = "prov",
            providerName = "Presupuesto #$i",
            grandTotal = (i + 1) * 1500.0
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {
        // En preview forzamos el contenido base para asegurar visibilidad
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderAdministrador(
                count = 12,
                title = "PRESUPUESTOS EN LICITACIONES",
                helperText = "ADMINISTRADOR DE",
                tenderName = "Licitación: Fachada Edificio Central",
                onClose = {},
                filterOptions = filters,
                sortOptions = sorts,
                selectedFilter = "all",
                selectedSort = "recent",
                onFilterSelect = {},
                onSortSelect = {},
                collapseFraction = 0f
            )

            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(budgets) { budget ->
                        TarjetaPresupuestoA4Document(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            budget = budget,
                            onViewClick = { }
                        )
                    }
                }
            }
        }
    }
}

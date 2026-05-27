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
import com.example.myapplication.presentation.components.TarjetaPresupuestoA4Document
import com.example.myapplication.presentation.components.SheetCloseButton
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.DepthDividerVertical
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.ElevatedDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.shakeClick
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import com.example.myapplication.presentation.designsystem.components.DepthDividerVertical
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke

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
    budgets: List<com.example.myapplication.core.data.local.entity.BudgetEntity>, 
    onBudgetClick: (com.example.myapplication.core.data.local.entity.BudgetEntity) -> Unit,
    topOffset: Dp = 60.dp
) {
    val density = LocalDensity.current
    var screenHeight by remember { mutableFloatStateOf(0f) }
    
    // Estados de anclaje (Elite SSOT)
    val hiddenAnchor = screenHeight
    val partialAnchor = screenHeight * 0.4f
    val fullAnchor = with(density) { topOffset.toPx() }

    val animatableOffset = remember { Animatable(3000f) }
    var isInitialized by remember { mutableStateOf(false) }

    // --- ESTABILIZACIÓN MAVERICK ---
    var lastVisible by remember { mutableStateOf(isVisible) }
    var isActuallyAnimatingOut by remember { mutableStateOf(false) }
    val shouldBeComposed = isVisible || isActuallyAnimatingOut || (lastVisible && !isVisible)

    LaunchedEffect(isVisible, screenHeight) {
        if (screenHeight > 0) {
            if (!isInitialized) {
                animatableOffset.snapTo(screenHeight)
                isInitialized = true
            }

            if (lastVisible && !isVisible) isActuallyAnimatingOut = true
            if (!lastVisible && isVisible) isActuallyAnimatingOut = false
            lastVisible = isVisible

            val target = if (isVisible) partialAnchor else hiddenAnchor
            
            if (animatableOffset.value != target) {
                animatableOffset.animateTo(
                    targetValue = target,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
                if (animatableOffset.value >= hiddenAnchor && !isVisible) {
                    isActuallyAnimatingOut = false
                }
            }
        }
    }

    if (shouldBeComposed) {
        Box(
            modifier = Modifier.fillMaxSize().onSizeChanged {
                if (it.height > 0 && it.height.toFloat() != screenHeight) {
                    screenHeight = it.height.toFloat()
                }
            }
        ) {
            // --- 🎭 CAPA 1: FONDO OSCURO ---
            if (isInitialized) {
                val alpha = remember(animatableOffset.value, screenHeight, fullAnchor, hiddenAnchor) {
                    if (screenHeight == 0f || hiddenAnchor == fullAnchor) 0f
                    else ((hiddenAnchor - animatableOffset.value) / (hiddenAnchor - fullAnchor) * 0.85f).coerceIn(0f, 0.85f)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaverickColors.ROG_Dark_Bg.copy(alpha = alpha))
                        .pointerInput(Unit) { detectTapGestures { onClose() } }
                )
            }

            // --- 🏗️ CAPA 2: CONTENIDO ELITE ---
            if (isInitialized) {
                val scope = rememberCoroutineScope()
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
                
                val collapseFraction = (scrollAccumulator / 150f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(0, animatableOffset.value.roundToInt()) }
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    animatableOffset.snapTo((animatableOffset.value + delta).coerceIn(fullAnchor, hiddenAnchor))
                                }
                            },
                            onDragStopped = {
                                val target = if (animatableOffset.value < (partialAnchor + fullAnchor) / 2) fullAnchor
                                            else if (animatableOffset.value < (hiddenAnchor + partialAnchor) / 2) partialAnchor
                                            else { onClose(); hiddenAnchor }
                                scope.launch {
                                    animatableOffset.animateTo(target, spring(stiffness = Spring.StiffnessLow))
                                }
                            }
                        )
                ) {
                    EliteHudContainerInternal(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(with(density) { (screenHeight - fullAnchor).toDp() })
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)
                        ) {
                            HeaderAdministrador(
                                count = count,
                                title = title,
                                helperText = helperText,
                                tenderName = tenderName,
                                onClose = onClose,
                                collapseFraction = collapseFraction
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(3),
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(budgets) { budget ->
                                        TarjetaPresupuestoA4Document(
                                            modifier = Modifier.fillMaxWidth().height(180.dp),
                                            budget = budget,
                                            onViewClick = { onBudgetClick(budget) }
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(15.dp)
                                        .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)))
                                        .zIndex(10f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** COMPONENTE INTERNO: Contenedor Maestro Elite HUD (Independizado) */
@Composable
private fun EliteHudContainerInternal(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.98f))
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val cutSize = 10.dp.toPx()
                val borderGradient = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, MaverickColors.ElectricCyan, MaverickColors.ElectricPurple, MaverickColors.ElectricCyan, Color.Transparent)
                )
                val basePath = Path().apply {
                    moveTo(0f, cutSize); lineTo(cutSize, 0f); lineTo(size.width - cutSize, 0f); lineTo(size.width, cutSize)
                }
                drawPath(path = basePath, brush = borderGradient, style = Stroke(width = strokeWidth))
                drawPath(path = basePath, color = MaverickColors.ElectricCyan.copy(alpha = 0.15f), style = Stroke(width = strokeWidth * 2.5f))
                drawPath(path = basePath, brush = borderGradient, style = Stroke(width = strokeWidth * 5f), alpha = 0.08f)
            }
    ) {
        content()
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
    collapseFraction: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                )
            )
            .drawBehind {
                // RIM LIGHTING: Brillo sutil en el borde superior
                drawLine(
                    color = MaverickColors.ElectricCyan.copy(alpha = 0.4f),
                    start = Offset(0f, 0.5.dp.toPx()),
                    end = Offset(size.width, 0.5.dp.toPx()),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
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
                        color = Color.Gray.copy(alpha = 0.8f),
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

            // --- CONTADOR DE PRESUPUESTOS (ELITE) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "RESULT",
                    style = CyberTypography.MonospaceData.copy(
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }

            DepthDividerVertical(
                modifier = Modifier.padding(horizontal = 12.dp).height(24.dp),
                thickness = 1.dp
            )

            SheetCloseButton(onClick = onClose)
        }

        // --- DIVIDER PROFUNDO ---
        DepthDividerHorizontal(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
            shadowColor = Color.Black.copy(alpha = 0.8f),
            highlightColor = Color.White.copy(alpha = 0.1f)
        )

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





// === DATA MODELS & UTILS ===

// Eliminado BudgetPlaceholder a favor de BudgetEntity

// === PREVIEW ===

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PresupuestoAdministradorSheetPreview() {
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

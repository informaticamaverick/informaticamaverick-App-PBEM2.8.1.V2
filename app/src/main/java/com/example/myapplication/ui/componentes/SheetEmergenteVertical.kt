package com.example.myapplication.ui.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.componentes.sistema.AutoSizeText
//import com.example.myapplication.ui.componentes.sistema.CPCyberColors
import com.example.myapplication.uishared.estilos.CyberTypography
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.sistema.shakeClick
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: SHEET EMERGENTE VERTICAL (ELITE HUD v2026.FINAL) ---
 * ==========================================================================================
 * [OPTIMIZACIÓN ELITE]: Se ha resuelto el conflicto de gestos separando el área de
 * arrastre (Cabecera) del área de scroll (Contenido) e implementando Nested Scroll.
 */
@Composable
fun SheetEmergenteVertical(
    isVisible: Boolean,
    onClose: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    emoji: String? = null,
    topOffset: Dp = 150.dp,
    showEmoji: Boolean = true,
    showHelperText: Boolean = true,
    showTitle: Boolean = true,
    showActions: Boolean = true,
    isDraggable: Boolean = true,
    isScrollable: Boolean = true,
    initialAnchorIsFull: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    onAnimationFinished: () -> Unit = {},
    onEntryFinished: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    var screenHeight by remember { mutableFloatStateOf(0f) }

    val hiddenAnchor by remember { derivedStateOf { screenHeight } }
    val partialAnchor by remember { derivedStateOf { screenHeight * 0.4f } }
    val fullAnchor by remember { derivedStateOf { with(density) { topOffset.toPx() } } }

    val animatableOffset = remember { Animatable(3000f) }
    var isInitialized by remember { mutableStateOf(false) }
    var lastVisible by remember { mutableStateOf(isVisible) }
    var isActuallyAnimatingOut by remember { mutableStateOf(false) }
    val shouldBeComposed = isVisible || isActuallyAnimatingOut || (lastVisible && !isVisible)

    val scope = rememberCoroutineScope()

    // --- [ELITE] NESTED SCROLL CONNECTION ---
    // Permite que el scroll interno "empuje" la sheet hacia abajo cuando llega al tope.
    val nestedScrollConnection = remember(screenHeight, fullAnchor) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // Si el usuario desliza hacia abajo y no estamos en el anclaje oculto
                if (delta < 0 && animatableOffset.value > fullAnchor) {
                    val newOffset = (animatableOffset.value - delta).coerceIn(fullAnchor, hiddenAnchor)
                    scope.launch { animatableOffset.snapTo(newOffset) }
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (animatableOffset.value > fullAnchor) {
                    val target = if (animatableOffset.value < (partialAnchor + fullAnchor) / 2) fullAnchor
                                else if (animatableOffset.value < (hiddenAnchor + partialAnchor) / 2) partialAnchor
                                else { onClose(); hiddenAnchor }
                    animatableOffset.animateTo(target, spring(stiffness = Spring.StiffnessLow))
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(isVisible, screenHeight) {
        if (screenHeight > 0) {
            if (!isInitialized) {
                kotlinx.coroutines.delay(100)
                animatableOffset.snapTo(screenHeight)
                isInitialized = true
            }

            if (lastVisible && !isVisible) isActuallyAnimatingOut = true
            if (!lastVisible && isVisible) isActuallyAnimatingOut = false
            lastVisible = isVisible

            val target = if (isVisible) {
                if (initialAnchorIsFull) fullAnchor else partialAnchor
            } else hiddenAnchor

            if (animatableOffset.value != target) {
                animatableOffset.animateTo(target, tween(500, easing = FastOutSlowInEasing))
                if (isVisible && animatableOffset.value == target) onEntryFinished()
                if (animatableOffset.value >= hiddenAnchor && !isVisible) {
                    isActuallyAnimatingOut = false
                    onAnimationFinished()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().onSizeChanged {
            if (it.height > 0 && it.height.toFloat() != screenHeight) screenHeight = it.height.toFloat()
        })

        if (shouldBeComposed && isInitialized) {
            val alpha = remember(animatableOffset.value, screenHeight, fullAnchor, hiddenAnchor) {
                if (screenHeight == 0f || hiddenAnchor == fullAnchor) 0f
                else ((hiddenAnchor - animatableOffset.value) / (hiddenAnchor - fullAnchor) * 0.85f).coerceIn(0f, 0.85f)
            }
            Box(modifier = Modifier.fillMaxSize().background(SharedPalette.ROG_Dark_Bg.copy(alpha = alpha)).pointerInput(Unit) { detectTapGestures { onClose() } })
        }

        if (shouldBeComposed && isInitialized) {
            Box(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, animatableOffset.value.roundToInt()) }
                    .nestedScroll(nestedScrollConnection) // 🔥 [ELITE] Activamos la coordinación de scroll
            ) {
                EliteHudContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { (screenHeight - fullAnchor).toDp() })
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // --- 1. CABECERA (ZONA DE ARRASTRE EXCLUSIVA) ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)))
                                .then(
                                    if (isDraggable && screenHeight > 0) {
                                        Modifier.draggable(
                                            orientation = Orientation.Vertical,
                                            state = rememberDraggableState { delta ->
                                                scope.launch { animatableOffset.snapTo((animatableOffset.value + delta).coerceIn(fullAnchor, hiddenAnchor)) }
                                            },
                                            onDragStopped = {
                                                val target = if (animatableOffset.value < (partialAnchor + fullAnchor) / 2) fullAnchor
                                                            else if (animatableOffset.value < (hiddenAnchor + partialAnchor) / 2) partialAnchor
                                                            else { onClose(); hiddenAnchor }
                                                scope.launch { animatableOffset.animateTo(target, spring(stiffness = Spring.StiffnessLow)) }
                                            }
                                        )
                                    } else Modifier
                                )
                                .drawBehind {
                                    drawLine(SharedPalette.ElectricCyan.copy(alpha = 0.4f), Offset(0f, 0.5.dp.toPx()), Offset(size.width, 0.5.dp.toPx()), 1.2.dp.toPx())
                                    drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showEmoji && emoji != null) {
                                    Text(text = emoji, style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp), modifier = Modifier.padding(end = 16.dp))
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                    if (showHelperText && helperText != null) {
                                        Text(text = helperText.uppercase(), style = CyberTypography.MonospaceData.copy(color = Color.Gray.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp))
                                    }
                                    if (showTitle) {
                                        AutoSizeText(text = title.uppercase(), style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp), maxLines = 1)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    if (showActions) actions()
                                    SheetCloseButton(onClick = onClose)
                                }
                            }
                        }

                        // --- 2. ÁREA DE CONTENIDO (ZONA DE SCROLL) ---
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Column(
                                modifier = Modifier.fillMaxSize().then(
                                    if (isScrollable) Modifier.verticalScroll(rememberScrollState())
                                    else Modifier
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), content = { content() })
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(15.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent))).zIndex(10f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EliteHudContainer(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.clip(CutCornerShape(topStart = 10.dp, topEnd = 10.dp)).background(SharedPalette.ROG_Dark_Bg.copy(alpha = 0.98f)).drawBehind {
        val strokeWidth = 1.dp.toPx(); val cutSize = 10.dp.toPx()
        val borderGradient = Brush.horizontalGradient(listOf(Color.Transparent, SharedPalette.ElectricCyan, SharedPalette.ElectricPurple, SharedPalette.ElectricCyan, Color.Transparent))
        val basePath = Path().apply { moveTo(0f, cutSize); lineTo(cutSize, 0f); lineTo(size.width - cutSize, 0f); lineTo(size.width, cutSize) }
        drawPath(path = basePath, brush = borderGradient, style = Stroke(width = strokeWidth))
        drawPath(path = basePath, color = SharedPalette.ElectricCyan.copy(alpha = 0.15f), style = Stroke(width = strokeWidth * 2.5f))
        drawPath(path = basePath, brush = borderGradient, style = Stroke(width = strokeWidth * 5f), alpha = 0.08f)
    }) { content() }
}

@Composable
fun SheetCloseButton(onClick: () -> Unit) {
    Box(modifier = Modifier.size(32.dp).border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), CircleShape).background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape).shakeClick { onClick() }, contentAlignment = Alignment.Center) {
        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SheetActionButton(icon: String, label: String, onClick: () -> Unit, active: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.size(34.dp).background(if (active) SharedPalette.ElectricCyan.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), CircleShape).border(1.dp, if (active) SharedPalette.ElectricCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), CircleShape).clickable { onClick() }, contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 18.sp)
        }
        Text(text = label.uppercase(), style = CyberTypography.MonospaceData.copy(fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = if (active) SharedPalette.ElectricCyan else Color.Gray.copy(alpha = 0.6f)), modifier = Modifier.padding(top = 2.dp))
    }
}


































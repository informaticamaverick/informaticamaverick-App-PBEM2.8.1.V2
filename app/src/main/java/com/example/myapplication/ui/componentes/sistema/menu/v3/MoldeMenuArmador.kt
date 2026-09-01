package com.example.myapplication.ui.componentes.sistema.menu.v3

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.PBEMTheme

/**
 * --- 🏗️ ARMADOR DE MENÚ ELITE V3 (v2026.ELITE - SUPREME) ---
 * [PROPÓSITO]: Esqueleto inteligente para menús emergentes tácticos.
 * [LEY #10]: Maneja la animación, forma (Bubble) y gravedad del Popup.
 * [SUPREME]: Soporta centrado en pantalla con flecha dinámica inteligente.
 */

class BubbleShapeV3(
    private val arrowOffset: Dp = 24.dp,
    private val isArrowBottom: Boolean = false
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val arrowWidth = with(density) { 16.dp.toPx() }
        val arrowHeight = with(density) { 12.dp.toPx() } 
        val cornerRadius = with(density) { 8.dp.toPx() } 
        val offset = with(density) { arrowOffset.toPx() }.coerceIn(cornerRadius, size.width - cornerRadius - arrowWidth)

        val path = Path().apply {
            if (!isArrowBottom) {
                moveTo(cornerRadius, arrowHeight)
                lineTo(offset - with(density) { 4.dp.toPx() }, arrowHeight)
                lineTo(offset + arrowWidth / 2, 0f)
                lineTo(offset + arrowWidth + with(density) { 4.dp.toPx() }, arrowHeight)
                lineTo(size.width - cornerRadius, arrowHeight)
                quadraticTo(size.width, arrowHeight, size.width, arrowHeight + cornerRadius)
                lineTo(size.width, size.height - cornerRadius)
                quadraticTo(size.width, size.height, size.width - cornerRadius, size.height)
                lineTo(cornerRadius, size.height)
                quadraticTo(0f, size.height, 0f, size.height - cornerRadius)
                lineTo(0f, arrowHeight + cornerRadius)
                quadraticTo(0f, arrowHeight, cornerRadius, arrowHeight)
            } else {
                moveTo(cornerRadius, 0f)
                lineTo(size.width - cornerRadius, 0f)
                quadraticTo(size.width, 0f, size.width, cornerRadius)
                lineTo(size.width, size.height - arrowHeight - cornerRadius)
                quadraticTo(size.width, size.height - arrowHeight, size.width - cornerRadius, size.height - arrowHeight)
                lineTo(offset + arrowWidth + with(density) { 4.dp.toPx() }, size.height - arrowHeight)
                lineTo(offset + arrowWidth / 2, size.height)
                lineTo(offset - with(density) { 4.dp.toPx() }, size.height - arrowHeight)
                lineTo(cornerRadius, size.height - arrowHeight)
                quadraticTo(0f, size.height - arrowHeight, 0f, size.height - arrowHeight - cornerRadius)
                lineTo(0f, cornerRadius)
                quadraticTo(0f, 0f, cornerRadius, 0f)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun MoldeMenuArmadorV3(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    arrowOffset: Dp = 24.dp,
    isArrowBottom: Boolean = false,
    anchoMaximo: Dp = 320.dp,
    alignment: Alignment = Alignment.TopStart,
    verticalOffset: Dp = 0.dp,
    horizontalOffset: Dp = 0.dp, 
    isCenteredOnScreen: Boolean = false,
    autoArrow: Boolean = isCenteredOnScreen, // 🔥 [v2026.SUPREME]: Control inteligente de la flecha
    content: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "MenuScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "MenuAlpha"
    )

    if (expanded || alpha > 0.01f) {
        val density = LocalDensity.current
        var actualArrowOffset by remember { mutableStateOf(arrowOffset) }
        
        // Mantener sincronizado si cambia el parámetro manual y autoArrow es false
        LaunchedEffect(arrowOffset, autoArrow) {
            if (!autoArrow) actualArrowOffset = arrowOffset
        }

        val positionProvider = remember(alignment, horizontalOffset, verticalOffset, isArrowBottom, isCenteredOnScreen, autoArrow) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    val hOffsetPx = with(density) { horizontalOffset.roundToPx() }
                    val vOffsetPx = with(density) { verticalOffset.roundToPx() }
                    val margin = with(density) { 8.dp.roundToPx() }

                    val x = if (isCenteredOnScreen) {
                        (windowSize.width - popupContentSize.width) / 2
                    } else {
                        when (alignment) {
                            Alignment.TopStart, Alignment.BottomStart -> anchorBounds.left
                            Alignment.TopEnd, Alignment.BottomEnd -> anchorBounds.right - popupContentSize.width
                            Alignment.Center, Alignment.TopCenter, Alignment.BottomCenter -> 
                                anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                            else -> anchorBounds.left
                        }
                    }

                    // 🔥 [v2026.ELITE]: Re-alineación vertical basada en el anclaje real (Soluciona "Separados")
                    val y = when (alignment) {
                        Alignment.TopStart, Alignment.TopEnd, Alignment.TopCenter -> {
                            if (isArrowBottom) anchorBounds.top - popupContentSize.height - margin - vOffsetPx
                            else anchorBounds.top + margin + vOffsetPx
                        }
                        Alignment.BottomStart, Alignment.BottomEnd, Alignment.BottomCenter -> {
                            if (isArrowBottom) anchorBounds.bottom - popupContentSize.height - margin - vOffsetPx
                            else anchorBounds.bottom + margin + vOffsetPx
                        }
                        else -> {
                            if (isArrowBottom) anchorBounds.top - popupContentSize.height - margin - vOffsetPx
                            else anchorBounds.bottom + margin + vOffsetPx
                        }
                    }

                    // 🔥 [SUPREME]: Cálculo dinámico de la flecha si autoArrow está activo
                    if (autoArrow) {
                        val anchorCenterX = anchorBounds.left + anchorBounds.width / 2
                        val finalX = x + hOffsetPx
                        val arrowOffsetPx = (anchorCenterX - finalX).toFloat()
                        actualArrowOffset = with(density) { arrowOffsetPx.toDp() }
                    }

                    return IntOffset(x + hOffsetPx, y)
                }
            }
        }

        Popup(
            popupPositionProvider = positionProvider,
            properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
            onDismissRequest = onDismissRequest
        ) {
            Box(
                modifier = modifier
                    .widthIn(min = 210.dp, max = anchoMaximo) 
                    .padding(horizontal = 4.dp) 
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        transformOrigin = TransformOrigin(
                            pivotFractionX = pivotByOffset(actualArrowOffset, size.width, density),
                            pivotFractionY = if (isArrowBottom) 1f else 0f
                        )
                    }
            ) {
                val shape = BubbleShapeV3(actualArrowOffset, isArrowBottom)
                val gradientFondo = Brush.verticalGradient(
                    listOf(Color(0xFF0C0D0F), SharedPalette.VantaBlack)
                )
                val borderBrush = Brush.linearGradient(
                    listOf(SharedPalette.ElectricCyan, SharedPalette.V2Cyan.copy(alpha = 0.6f))
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().asFrameworkPaint().apply {
                                    setShadowLayer(24f, 0f, 12f, Color.Black.copy(alpha = 0.9f).toArgb())
                                }
                                canvas.drawPath(shape.createOutline(size, layoutDirection, density).let { (it as Outline.Generic).path }, Paint().apply { this.asFrameworkPaint().set(paint) })
                                paint.setShadowLayer(14f, 0f, 0f, SharedPalette.ElectricCyan.copy(alpha = 0.35f).toArgb())
                                canvas.drawPath(shape.createOutline(size, layoutDirection, density).let { (it as Outline.Generic).path }, Paint().apply { this.asFrameworkPaint().set(paint) })
                            }
                        }
                        .background(gradientFondo, shape)
                        .border(BorderStroke(0.7.dp, borderBrush), shape),
                    color = Color.Transparent,
                    shape = shape
                ) {
                    Column(
                        modifier = Modifier
                            .width(IntrinsicSize.Max)
                            .padding(
                                top = if (isArrowBottom) 12.dp else 24.dp, 
                                bottom = if (isArrowBottom) 24.dp else 12.dp,
                                start = 14.dp, 
                                end = 14.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(0.dp) 
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

private fun pivotByOffset(offset: Dp, width: Float, density: Density): Float {
    val offsetPx = with(density) { offset.toPx() }
    return (offsetPx / width).coerceIn(0f, 1f)
}

@Preview(name = "Menú V3 - Estilo Asistente", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMenuAsistenteStyle() {
    PBEMTheme {
        Column(
            modifier = Modifier.padding(40.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(240.dp, 120.dp).background(Color.White.copy(alpha = 0.05f))) {
                MoldeMenuArmadorV3(
                    expanded = true,
                    onDismissRequest = {},
                    arrowOffset = 24.dp,
                    isArrowBottom = false,
                    alignment = Alignment.TopStart
                ) {
                    Text("MENU PREVIEW", color = Color.White)
                }
            }
        }
    }
}

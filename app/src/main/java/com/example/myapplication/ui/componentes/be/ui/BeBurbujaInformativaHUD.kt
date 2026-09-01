package com.example.myapplication.ui.componentes.be.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- BE BURBUJA INFORMATIVA HUD (v2026.ELITE.PRO) ---
 * [PROPÓSITO]: Extensión visual del asistente Be para mostrar mensajes, logs o resultados.
 * [DISEÑO]: Full-width con flecha superior izquierda apuntando a los ojos de Be.
 * [LEY #10]: Anatomía Táctica Elite (Tactical Black + Neon Cyan).
 */
@Composable
fun BeBurbujaInformativaHUD(
    visible: Boolean,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    arrowOffset: Dp = 32.dp,
    colorBorde: Color = SharedPalette.ElectricCyan,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "BubbleScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "BubbleAlpha"
    )

    if (visible || alpha > 0.01f) {
        val density = LocalDensity.current
        
        Box(modifier = modifier.fillMaxWidth()) {
            val positionProvider = remember(arrowOffset) {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val x = 0 
                        val y = anchorBounds.top
                        return IntOffset(x, y)
                    }
                }
            }

            Popup(
                popupPositionProvider = positionProvider,
                onDismissRequest = onDismissRequest
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            transformOrigin = TransformOrigin(0.1f, 0f) 
                        }
                        .padding(horizontal = 16.dp)
                ) {
                    val shape = rememberBeBubbleShape(arrowOffset, density)
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                this.shadowElevation = 12.dp.toPx()
                                this.shape = shape
                                this.clip = true
                            },
                        color = Color.Transparent 
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF0A1016), Color(0xFF020408))
                                    )
                                )
                                .drawBehind {
                                    val arrowW = 16.dp.toPx()
                                    val arrowH = 10.dp.toPx()
                                    val radius = 10.dp.toPx() // 🔥 Ajustado
                                    val off = arrowOffset.toPx()

                                    val path = Path().apply {
                                        moveTo(off + arrowW, arrowH)
                                        lineTo(size.width - radius, arrowH)
                                        quadraticTo(size.width, arrowH, size.width, arrowH + radius)
                                        lineTo(size.width, size.height - radius)
                                        quadraticTo(size.width, size.height, size.width - radius, size.height)
                                        lineTo(radius, size.height)
                                        quadraticTo(0f, size.height, 0f, size.height - radius)
                                        lineTo(0f, arrowH + radius)
                                        quadraticTo(0f, arrowH, radius, arrowH)
                                        lineTo(off, arrowH)
                                        lineTo(off + arrowW / 2, 0f)
                                        lineTo(off + arrowW, arrowH)
                                        close()
                                    }
                                    
                                    drawPath(
                                        path = path,
                                        color = colorBorde.copy(alpha = 0.8f),
                                        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 22.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                            ) {
                                contenido()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberBeBubbleShape(arrowOffset: Dp, density: Density): Shape {
    return GenericShape { size, _ ->
        val arrowW = with(density) { 16.dp.toPx() }
        val arrowH = with(density) { 10.dp.toPx() }
        val radius = with(density) { 10.dp.toPx() } // 🔥 Ajustado para paridad con Asistente
        val off = with(density) { arrowOffset.toPx() }

        moveTo(off + arrowW, arrowH)
        lineTo(size.width - radius, arrowH)
        quadraticTo(size.width, arrowH, size.width, arrowH + radius)
        lineTo(size.width, size.height - radius)
        quadraticTo(size.width, size.height, size.width - radius, size.height)
        lineTo(radius, size.height)
        quadraticTo(0f, size.height, 0f, size.height - radius)
        lineTo(0f, arrowH + radius)
        quadraticTo(0f, arrowH, radius, arrowH)
        lineTo(off, arrowH)
        lineTo(off + arrowW / 2, 0f)
        lineTo(off + arrowW, arrowH)
        close()
    }
}

@Preview(name = "Burbuja Be - Ejemplo Mensaje", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewBeBurbujaInformativa() {
    PBEMTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(top = 20.dp)) {
            BeBurbujaInformativaHUD(visible = true) {
                Text(
                    text = "IDENTIDAD DETECTADA",
                    color = SharedPalette.ElectricCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Iniciando protocolo de escaneo satelital en tu zona. Buscando unidades de respuesta inmediata...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

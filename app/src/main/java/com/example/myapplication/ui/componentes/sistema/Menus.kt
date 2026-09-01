package com.example.myapplication.ui.componentes.sistema

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.ui.componentes.be.modelos.EmocionBe
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.sistema.shakeClick
import com.example.myapplication.ui.componentes.be.ui.BeAssistantEyes

/**
 * --- MOLDES DE MENÚS SISTEMA (v2026.ELITE - SANEADO) ---
 * [PROPÓSITO]: Colección de componentes de menú heredados y tácticos que aún están en uso.
 * [LEY #13]: Estratificación Atómica. Los menús nuevos deben ir a v3.
 */

/**
 * MenuTacticoBe: Menú emergente con la identidad visual del asistente Be.
 * Utilizado actualmente en TarjetaPrestador para acciones contextuales.
 */
@Composable
fun MenuTacticoBe(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    touchOffset: Offset? = null,
    emotion: EmocionBe = EmocionBe.NORMAL,
    actionLabel: String = "ACCIONES",
    actionIconEmoji: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopupScale"
    )

    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) (-10).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "PopupOffset"
    )

    if (isVisible || scale > 0.01f) {
        val popupWidth = 92.dp
        val circleSize = 72.dp
        val arrowHeight = 12.dp
        val gap = (-4).dp
        val buttonHeight = 36.dp
        val buttonSpacer = 8.dp
        val popupHeight = buttonHeight + buttonSpacer + circleSize + arrowHeight + gap
        val density = LocalDensity.current
        val arrowCurvePx = with(density) { 4.dp.toPx() }

        val finalOffset = density.run {
            if (touchOffset != null) {
                val pxWidth = popupWidth.toPx()
                val pxHeight = popupHeight.toPx()
                val pxExtraOffset = 24.dp.toPx()

                IntOffset(
                    x = (touchOffset.x - pxWidth / 2).toInt(),
                    y = (touchOffset.y - pxHeight - pxExtraOffset).toInt() + animatedOffsetY.toPx().toInt()
                )
            } else {
                IntOffset(0, (-100).dp.toPx().toInt() + animatedOffsetY.toPx().toInt())
            }
        }

        Popup(
            alignment = Alignment.TopStart,
            offset = finalOffset,
            properties = PopupProperties(
                focusable = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismissRequest
        ) {
            Box(
                modifier = modifier
                    .size(popupWidth, popupHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale.coerceIn(0f, 1f)
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(24.dp, arrowHeight)
                        .shadow(
                            elevation = 8.dp,
                            shape = GenericShape { size, _ ->
                                moveTo(0f, 0f)
                                quadraticTo(size.width / 2f, arrowCurvePx, size.width, 0f)
                                lineTo(size.width / 2f, size.height)
                                close()
                            },
                            clip = false,
                            spotColor = Color.Black
                        )
                ) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        quadraticTo(size.width / 2f, 4.dp.toPx(), size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    drawPath(path, Color(0xFF393B40))
                    val innerPath = Path().apply {
                        moveTo(size.width * 0.35f, size.height * 0.45f)
                        lineTo(size.width * 0.65f, size.height * 0.45f)
                        lineTo(size.width / 2f, size.height * 0.9f)
                        close()
                    }
                    drawPath(innerPath, SharedPalette.NeonCyan)
                    drawPath(path = path, color = Color(0xFF1E293B).copy(alpha = 0.8f), style = Stroke(width = 1.5.dp.toPx()))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onAction,
                        modifier = Modifier.height(buttonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D1B20), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (actionIconEmoji != null) { Text(text = actionIconEmoji, fontSize = 12.sp) }
                            Text(text = actionLabel.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(buttonSpacer))
                    Box(modifier = Modifier.size(circleSize).clip(CircleShape).shakeClick { onAction() }, contentAlignment = Alignment.Center) {
                        BeAssistantEyes(size = circleSize, emocion = emotion)
                    }
                }
            }
        }
    }
}

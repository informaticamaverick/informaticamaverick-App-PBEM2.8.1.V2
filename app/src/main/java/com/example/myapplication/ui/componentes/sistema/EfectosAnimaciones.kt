package com.example.myapplication.ui.componentes.sistema

import com.example.myapplication.uishared.estilos.SharedPalette
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ==========================================================================================
// --- SECCIÓN 2: CONFIGURACIÓN VISUAL (GEMINI CYBERPUNK) ---
// ==========================================================================================
/**
 * Función unificada para generar el gradiente animado de Gemini.
 * Combina la paleta de 4 colores con un desplazamiento fluido.
 * * @param isAnimated Controla si el gradiente debe desplazarse automáticamente.
 */
@Composable
fun geminiGradientBrush(isAnimated: Boolean = true): Brush {
    // Paleta completa de colores de Gemini (Actualizada con Cian/Celeste solicitado)
    val colors = listOf(
        SharedPalette.GeminiCyan, // Cian (Celeste) - Acento principal
        SharedPalette.GeminiPurple, // Púrpura
        SharedPalette.CyberPink, // Rosa
        SharedPalette.GeminiCyan  // Volvemos a Cian para cerrar el ciclo de animación suave
    )

    val offset = if (isAnimated) {
        val infiniteTransition = rememberInfiniteTransition(label = "geminiAnim")
        val animatedValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2000f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )
        animatedValue
    } else {
        0f
    }

    return Brush.linearGradient(
        colors = colors,
        // Se ajustan los offsets para que el degradado cubra bien el área y se mueva suavemente
        start = Offset(offset - 1000f, offset - 1000f),
        end = Offset(offset, offset ),
        tileMode = TileMode.Mirror
    )
}

@Composable
fun Modifier.rotateOnExpansion(isExpanded: Boolean): Modifier {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    return this.rotate(rotation)
}

/**
 * MODIFICADOR SHAKE (EFECTO DE TEMBLOR)
 */
fun Modifier.shakeClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    this.graphicsLayer {
        rotationZ = rotation.value
    }
    .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled
    ) {
        scope.launch {
            // Secuencia de sacudida: 15 -> -15 -> 0
            rotation.animateTo(15f, tween(50, easing = LinearEasing))
            rotation.animateTo(-15f, tween(50, easing = LinearEasing))
            rotation.animateTo(0f, tween(50, easing = LinearEasing))
        }
        onClick()
    }
}

/**
 * Wrapper Premium que aplica el efecto visual de Gemini.
 */
@Composable
fun GeminiCyberWrapper(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    borderThickness: Dp = 1.2.dp,
    isAnimated: Boolean = true,
    showGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val geminiBrush = geminiGradientBrush(isAnimated = isAnimated)
    val cyberBackground = SharedPalette.CyberBackground

    Box(modifier = modifier.padding(12.dp)) {
        if (showGlow) {
            Box(modifier = Modifier.matchParentSize().graphicsLayer { alpha = 0.15f }.blur(25.dp).background(geminiBrush, RoundedCornerShape(cornerRadius)))
            Box(modifier = Modifier.matchParentSize().graphicsLayer { alpha = 0.35f }.blur(8.dp).background(geminiBrush, RoundedCornerShape(cornerRadius)))
        }

        Box(modifier = Modifier.fillMaxWidth().background(geminiBrush, RoundedCornerShape(cornerRadius)).padding(borderThickness)) {
            Surface(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(cornerRadius - borderThickness)), color = cyberBackground) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent))))
                    content()
                }
            }
        }
    }
}


































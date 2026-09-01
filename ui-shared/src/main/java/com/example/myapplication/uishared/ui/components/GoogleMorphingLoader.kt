package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GoogleMorphingLoader: Cargador inspirado en el estilo de Google (Dots to Line/Shape).
 * Utiliza transformaciones de trazo y color para una transición fluida "Elite".
 */
@Composable
fun GoogleMorphingLoader(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "google_morph")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val strokePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1333
                0f at 0
                0.5f at 666
                1f at 1333
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "stroke_phase"
    )

    // Colores Google / Elite
    val colors = listOf(
        Color(0xFF4285F4), // Blue
        Color(0xFFEA4335), // Red
        Color(0xFFFBBC05), // Yellow
        Color(0xFF34A853), // Green
        Color(0xFF00E5FF) // ElectricCyan Fallback
    )
    
    // Animación de color por pasos
    val colorIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = colors.size,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = colors.size * 1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "color"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = if (strokePhase < 0.5f) {
                strokePhase * 2f * 270f
            } else {
                (1f - (strokePhase - 0.5f) * 2f) * 270f
            }.coerceAtLeast(10f)

            val startAngle = if (strokePhase < 0.5f) {
                0f
            } else {
                (strokePhase - 0.5f) * 2f * 270f
            }

            rotate(rotation) {
                drawArc(
                    color = colors[colorIndex % colors.size],
                    startAngle = startAngle - 90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}


































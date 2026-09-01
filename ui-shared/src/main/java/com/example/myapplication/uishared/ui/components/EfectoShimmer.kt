package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * --- EFECTO SHIMMER MAVERICK (v2026.ELITE) ---
 * PROPÓSITO: Proporcionar un efecto de carga animado consistente.
 * LEY #9: Nomenclatura en español.
 */
fun Modifier.shimmerApp(): Modifier = composed {
    val transicionInfinita = rememberInfiniteTransition(label = "shimmerApp")
    val desplazamientoAnimado by transicionInfinita.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerDesplazamiento"
    )

    val coloresShimmer = listOf(
        Color.White.copy(alpha = 0.03f),
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.03f),
    )

    val pincel = Brush.linearGradient(
        colors = coloresShimmer,
        start = Offset(desplazamientoAnimado - 200f, desplazamientoAnimado - 200f),
        end = Offset(desplazamientoAnimado, desplazamientoAnimado)
    )

    background(pincel)
}

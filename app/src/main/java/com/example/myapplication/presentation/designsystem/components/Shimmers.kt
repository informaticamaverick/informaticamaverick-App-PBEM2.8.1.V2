package com.example.myapplication.presentation.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * MODIFICADOR SHIMMER (EFECTO SKELETON)
 * Crea un gradiente animado que simula la carga de contenido.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.02f),
        Color.White.copy(alpha = 0.12f),
        Color.White.copy(alpha = 0.02f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    background(brush)
}

/**
 * SKELETON PARA TARJETA DE CATEGORÍA COMPACTA
 */
@Composable
fun CategoryCardShimmer() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF1A1F26).copy(alpha = 0.5f)
    ) {
        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
    }
}

/**
 * SKELETON PARA TARJETA BENTO DE SUPERCATEGORÍA
 */
@Composable
fun SuperCategoryCardShimmer() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(22.dp)),
        color = Color(0xFF1A1F26).copy(alpha = 0.5f)
    ) {
        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
    }
}

/**
 * SKELETON PARA TARJETA DE PRESTADOR
 */
@Composable
fun ProviderCardShimmer() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = Color(0xFF1A1F26).copy(alpha = 0.5f)
    ) {
        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
    }
}

/**
 * GRILLA DE SKELETONS PARA LA HOME
 */
@Composable
fun HomeBentoShimmerGrid(columns: Int = 2) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(columns) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (columns == 2) SuperCategoryCardShimmer() else CategoryCardShimmer()
                    }
                }
            }
        }
    }
}

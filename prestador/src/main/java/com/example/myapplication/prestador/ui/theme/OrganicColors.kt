package com.example.myapplication.prestador.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * 🔮 CYBERPUNK 2077 - NEON TECH SYSTEM
 * 
 * Paleta de colores inspirada en estética cyberpunk
 * Neones brillantes, fondos oscuros, bordes iluminados
 */
data class OrganicColors(
    val isDark: Boolean,
    
    // Colores Base Cyberpunk
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    
    // Texto con tinte neón
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    
    // Colores Neón Principales
    val primaryOrange: Color = Color(0xFFFF006E), // Magenta neón
    val primaryOrangeLight: Color = Color(0xFFFF0A7B),
    val primaryOrangeDark: Color = Color(0xFFCC0058),
    
    // Gradientes Neón
    val primaryGradient: Brush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF006E), Color(0xFF8338EC), Color(0xFF00F5FF))
    ),
    val primaryVerticalGradient: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFF006E), Color(0xFF8338EC))
    ),
    
    // Neones de Acento
    val yellow: Color = Color(0xFFFBFF00), // Amarillo neón
    val violet: Color = Color(0xFF8338EC), // Púrpura eléctrico
    val cyan: Color = Color(0xFF00F5FF), // Cyan brillante
    val green: Color = Color(0xFF00FF41), // Verde Matrix
    val red: Color = Color(0xFFFF006E), // Rojo neón
    val blue: Color = Color(0xFF00D4FF), // Azul neón
    
    // Estados Neón
    val success: Color = Color(0xFF00FF41),
    val warning: Color = Color(0xFFFBFF00),
    val error: Color = Color(0xFFFF006E),
    val info: Color = Color(0xFF00F5FF),
    
    // Efectos de Brillo Cyberpunk
    val glassBackground: Color,
    val glassBorder: Color,
    val glassHighlight: Color
)

/**
 * 🌃 DARK CYBERPUNK - Night City Mode
 * Fondo ultra oscuro con neones intensos
 */
val DarkPalette = OrganicColors(
    isDark = true,
    
    // Fondos oscuros profundos
    background = Color(0xFF000000), // Negro puro
    surface = Color(0xFF0A0A0F), // Casi negro
    surfaceVariant = Color(0xFF121218), // Gris oscuro
    border = Color(0xFF00F5FF).copy(alpha = 0.3f), // Borde cyan neón
    
    // Texto con tinte cyan
    textPrimary = Color(0xFFE0FFFF), // Blanco azulado
    textSecondary = Color(0xFF00F5FF), // Cyan neón
    textTertiary = Color(0xFF666B7A), // Gris azulado
    
    // Efectos Neón
    glassBackground = Color(0xFF0A0A0F).copy(alpha = 0.7f),
    glassBorder = Color(0xFF00F5FF), // Cyan brillante
    glassHighlight = Color(0xFF00F5FF).copy(alpha = 0.4f)
)

/**
 * ☀️ LIGHT CYBERPUNK - Daylight Mode
 * Fondos claros con neones sobre blanco
 */
val LightPalette = OrganicColors(
    isDark = false,
    
    // Fondos claros
    background = Color(0xFFF0F0F5), // Gris muy claro
    surface = Color(0xFFFFFFFF), // Blanco puro
    surfaceVariant = Color(0xFFE8E8F0), // Gris claro
    border = Color(0xFF8338EC).copy(alpha = 0.4f), // Púrpura neón
    
    // Texto oscuro
    textPrimary = Color(0xFF0A0A0F), // Casi negro
    textSecondary = Color(0xFF4A4A68), // Gris medio
    textTertiary = Color(0xFF8E8E9E), // Gris claro
    
    // Efectos sutiles
    glassBackground = Color(0xFFFFFFFF).copy(alpha = 0.8f),
    glassBorder = Color(0xFF8338EC), // Púrpura brillante
    glassHighlight = Color(0xFF00D4FF).copy(alpha = 0.2f)
)

/**
 * 💫 EFECTOS DE RESPLANDOR NEÓN
 */
object GradientBackgrounds {
    val cyanGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFF00F5FF).copy(alpha = 0.3f),
            Color.Transparent
        )
    )
    
    val magentaGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFF006E).copy(alpha = 0.25f),
            Color.Transparent
        )
    )
    
    val cyberpunkGlow = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFF006E).copy(alpha = 0.15f),
            Color(0xFF8338EC).copy(alpha = 0.15f),
            Color(0xFF00F5FF).copy(alpha = 0.15f)
        )
    )
}

/**
 * EXTENSIONES ÚTILES
 */
fun OrganicColors.surfaceElevated(level: Int = 1): Color {
    return when {
        isDark -> surface.copy(alpha = 0.05f * level + 1f)
        else -> Color.White.copy(alpha = 1f - (0.05f * level))
    }
}

fun OrganicColors.onPrimary(): Color {
    return Color.White
}

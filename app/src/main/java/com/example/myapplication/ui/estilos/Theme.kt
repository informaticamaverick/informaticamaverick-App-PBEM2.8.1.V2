package com.example.myapplication.ui.estilos

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Define tus colores para MODO OSCURO
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),       // Azul brillante
    secondary = Color(0xFF8B5CF6),     // Púrpura
    tertiary = Color(0xFFFBBF24),      // Amarillo
    background = Color(0xFF050508),    // 🔥 [PBEM] VantaBlack base para evitar flash blanco
    surface = Color(0xFF161C24),       // Gris oscuro para tarjetas
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF1E293B),
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFEF4444),         // Rojo
    onError = Color.White,
    outline = Color(0xFF334155),       // Gris oscuro para bordes
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8) // Gris claro para texto secundario
)

// 2. Define tus colores para MODO CLARO
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3B82F6),       // Azul
    secondary = Color(0xFF8B5CF6),     // Púrpura
    tertiary = Color(0xFFFBBF24),      // Amarillo
    background = Color(0xFFF8FAFC),    // Gris muy claro
    surface = Color.White,             // Blanco para tarjetas
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1E293B), // Gris oscuro para texto
    onSurface = Color(0xFF1E293B),
    error = Color(0xFFEF4444),         // Rojo
    onError = Color.White,
    outline = Color(0xFFE2E8F0),       // Gris claro para bordes
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B) // Gris medio para texto secundario
)

@Composable
fun ClienteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta el modo del sistema automáticamente
    // Dynamic color es para Android 12+
    dynamicColor: Boolean = false, // Deshabilitado para usar nuestros colores personalizados
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Configuración de la barra de estado (Ajuste PBEM para API 35)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                window.statusBarColor = colorScheme.background.toArgb() 
            }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Estructura de datos para mantener los colores de la app
data class AppColors(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textPrimaryColor: Color,
    val textSecondaryColor: Color,
    val dividerColor: Color,
    val accentBlue: Color,
    val accentYellow: Color,
    val accentRed: Color,
    val accentGreen: Color
)

// Función helper para acceder fácilmente a los colores del tema
@Composable
fun getThemeColors(): AppColors {
    val colorScheme = MaterialTheme.colorScheme
    return AppColors(
        backgroundColor = colorScheme.background,
        surfaceColor = colorScheme.surface,
        textPrimaryColor = colorScheme.onSurface,
        textSecondaryColor = colorScheme.onSurfaceVariant,
        dividerColor = colorScheme.outline,
        accentBlue = colorScheme.primary,
        accentYellow = colorScheme.tertiary,
        accentRed = colorScheme.error,
        accentGreen = Color(0xFF10B981)
    )
}





































@Composable
fun PBEMTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) = 
    ClienteTheme(darkTheme, false, content)


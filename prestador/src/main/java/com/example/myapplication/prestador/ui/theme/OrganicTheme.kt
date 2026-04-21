package com.example.myapplication.prestador.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/**
 * COMPOSITION LOCAL para acceder a los colores en cualquier composable
 * 
 * Uso: val colors = LocalOrganicColors.current
 */
val LocalOrganicColors = staticCompositionLocalOf { LightPalette }

/**
 * THEME PROVIDER PRINCIPAL
 * 
 * Envuelve toda la aplicación y provee el tema seleccionado
 * Soporta cambio dinámico entre modo claro y oscuro
 */
@Composable
fun OrganicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Seleccionar paleta según el tema
    val colors = if (darkTheme) DarkPalette else LightPalette
    
    // ColorScheme de Material 3 para compatibilidad
    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primaryOrange,
            onPrimary = colors.onPrimary(),
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            error = colors.error,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = colors.primaryOrange,
            onPrimary = colors.onPrimary(),
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            error = colors.error,
            onError = Color.White
        )
    }

    // Proveer colores personalizados y Material Theme
    CompositionLocalProvider(LocalOrganicColors provides colors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * HELPER FUNCTION para obtener los colores actuales
 * 
 * Uso en cualquier @Composable:
 * val colors = getOrganicColors()
 */
@Composable
fun getOrganicColors(): OrganicColors {
    return LocalOrganicColors.current
}

/**
 * ESTADO DEL TEMA GLOBAL
 * Para manejar el cambio de tema en toda la app
 */
class ThemeState {
    private var _isDarkTheme = mutableStateOf(false)
    var isDarkTheme: Boolean
        get() = _isDarkTheme.value
        set(value) { _isDarkTheme.value = value }
    
    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }
}

/**
 * REMEMBER THEME STATE
 * Para mantener el estado del tema entre recomposiciones
 */
@Composable
fun rememberThemeState(): ThemeState {
    return remember { ThemeState() }
}

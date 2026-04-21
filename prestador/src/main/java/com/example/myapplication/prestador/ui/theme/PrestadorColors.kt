package com.example.myapplication.prestador.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

data class PrestadorColors(
    // Colores principales (naranja no cambia)
    val primaryOrange: Color,
    val primaryOrangeDark: Color,
    val primaryOrangeLight: Color,
    
    // Backgrounds
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceElevated: Color,
    
    // Textos
    val textPrimary: Color,
    val textSecondary: Color,
    
    // Bordes y divisores
    val border: Color,
    val divider: Color,
    
    // Chips
    val chipBackground: Color,
    val chipText: Color,
    
    // Estados
    val error: Color,
    val success: Color
)

@Composable
fun getPrestadorColors(darkTheme: Boolean = isSystemInDarkTheme()): PrestadorColors {
    return if (darkTheme) {
        // Modo Oscuro
        PrestadorColors(
            primaryOrange = PrestadorOrange,
            primaryOrangeDark = PrestadorOrangeDark,
            primaryOrangeLight = OrangeLight,
            
            backgroundColor = BackgroundDark,
            surfaceColor = SurfaceDark,
            surfaceElevated = SurfaceDarkElevated,
            
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            
            border = BorderDark,
            divider = DividerDark,
            
            chipBackground = ChipBackgroundDark,
            chipText = ChipTextDark,
            
            error = ErrorRed,
            success = Color(0xFF10B981)
        )
    } else {
        // Modo Claro
        PrestadorColors(
            primaryOrange = PrestadorOrange,
            primaryOrangeDark = PrestadorOrangeDark,
            primaryOrangeLight = OrangeLight,
            
            backgroundColor = BackgroundLight,
            surfaceColor = SurfaceWhite,
            surfaceElevated = SurfaceGray,
            
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryGray,
            
            border = BorderGray,
            divider = DividerLight,
            
            chipBackground = ChipBackground,
            chipText = ChipText,
            
            error = ErrorRed,
            success = Color(0xFF10B981)
        )
    }
}

package com.example.myapplication.uishared.estilos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SHARED PALETTE - DEFINICIÓN UNIFICADA Y ORDENADA
 */
object SharedPalette {

    // ==========================================================================================
    // --- 🎨 SECCIÓN: COLORES SÓLIDOS (Ordenados de Oscuro a Claro) ---
    // ==========================================================================================

    val AbsoluteBlack = Color(0xFF000000) 
    val V2VantaBlack = Color(0xFF020204)
    val V2DeepVoid = Color(0xFF030205)
    val VoidGray = Color(0xFF050505)
    val VantaBlack = Color(0xFF050508) 
    val V2TechSurface = Color(0xFF07050E)
    val NightSky = Color(0xFF070719)
    val CardBg = Color(0xFF0A0A0A)
    val TechSurface = Color(0xFF0A0A0F)
    val CyberBackground = Color(0xFF0A0E14)
    val DeepSpace = Color(0xFF0D0221)
    val ROG_Dark_Bg = Color(0xFF0D0D12)
    val DeepCityBlue = Color(0xFF0F1538)
    val StealthGray = Color(0xFF12121A)

    val EliteMainBackground = Color(0xFF1C222B) 
    val EliteSurface = Color(0xFF12151A)        

    val AsEditorBg = Color(0xFF1E1F22)
    val AsCaretLine = Color(0xFF26282E)
    val AsSidebarBg = Color(0xFF2B2D30)
    val AsSelectionBg = Color(0xFF35373C)
    val AsPanelBorder = Color(0xFF393B40)

    val DeepPurple = Color(0xFF3E065F)
    val TechPurple = Color(0xFF4A00E0)
    val ElectricPurple = Color(0xFF7000FF)
    val RogMagenta = Color(0xFF7E1254)
    val SoftViolet = Color(0xFF8A2BE2)
    val ElectricViolet = Color(0xFF8F00FF)
    val GeminiPurple = Color(0xFF9B51E0)
    val GeminiAccent = Color(0xFFA78BFA)

    val CyberPinkV3 = Color(0xFFD90077)
    val MagentaNeon = Color(0xFFE91E63) 
    val NeonMagenta = Color(0xFFFF0055)
    val CyberPink = Color(0xFFFF00FF)

    val Garnet = Color(0xFF8B0000)
    val RogCrimson = Color(0xFFFF0044)
    val DeepRed = Color(0xFFFF0033)
    val ErrorRed = Color(0xFFFF0032)
    val CyberRed = Color(0xFFEF4444)
    val WarningRed = Color(0xFFFF2A2A)
    val OverdriveOrange = Color(0xFFFF5F00)
    val GoldPremium = Color(0xFFFFD700)

    val GeminiCyan = Color(0xFF22D3EE)
    val V2Cyan = Color(0xFF00E5FF)
    val ElectricCyan = Color(0xFF00F0FF) 
    val NeonCyan = Color(0xFF00FFFF)
    val SuccessGreen = Color(0xFF00FF66)
    val AcidGreen = Color(0xFF00FF00)

    val TextMuted = Color(0xFF94A3B8)
    val ROG_Text_Main = Color(0xFFE2E2E8)
    val TextMain = Color(0xFFE2E8F0)

    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFCBD5E1)
    val Slate400 = Color(0xFF94A3B8)
    val Slate600 = Color(0xFF475569)
    val Slate800 = Color(0xFF1E293B)

    val GlassSurfaceV2 = Color(0xFFFFFFFF).copy(alpha = 0.02f)
    val GlassWhite = Color.White.copy(alpha = 0.05f) 
    val GlassBorder = Color.White.copy(alpha = 0.1f)
    val BorderColor = Color(0x1AFFFFFF) 
    val BentoDarkGlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)
    val GlassSurfaceElite = Color(0x0AFFFFFF)
    val CyanOpaque = Color(0xFF00FFFF).copy(alpha = 0.04f)
    val MagentaOpaque = Color(0xFFFF00FF).copy(alpha = 0.06f)
    val BentoDarkGlassBackground = Color(0xFF12121A).copy(alpha = 0.65f)
    val GlassSurfaceCP = Color(0xFF0A0814).copy(alpha = 0.7f)
    val BottomSheetMatteColor = Color(0xFF0A0A0F).copy(alpha = 0.95f)
    val GlassOverlayRog = Color(0xCC0A0A12)
    val CyanAccent = Color(0xFF00FFFF).copy(alpha = 0.6f)
    val MagentaAccent = Color(0xFFFF00FF).copy(alpha = 0.6f)

    val DarkBg = Color(0xFF020408)
    val CardSurface = Color(0xFF111827)

    // --- COLORES ADICIONALES (EX-MAVERICK) ---
    val SurfaceDark = Color(0xFF0D0F14)
    val SurfaceDarkElevated = Color(0xFF141822)
    val AccentIndigo = Color(0xFF6366F1)
    val EmeraldSuccess = Color(0xFF10B981)
    val BorderCyanSoft = Color(0xFF00F2FE).copy(alpha = 0.25f)
    val TextSubtle = Color(0xFF9CA3AF)

    // ==========================================================================================
    // --- 🌊 SECCIÓN: LISTA DE COLORES Y PINCELES (Brushes / Gradients) ---
    // ==========================================================================================

    val GeminiBrush = Brush.linearGradient(
        colors = listOf(NeonCyan, GeminiAccent, CyberPink)
    )

    val RogGradient = Brush.horizontalGradient(
        colors = listOf(NeonMagenta, TechPurple)
    )

    val BlueEnd = Color(0xFF2563EB)
    val BlueStart = Color(0xFF1E40AF)
    val A4Gradient = Brush.linearGradient(
        colors = listOf(BlueStart, BlueEnd)
    )

    val RogVerticalGradient = Brush.verticalGradient(
        colors = listOf(RogMagenta, DeepPurple, VantaBlack)
    )
    
    val RogHorizontalGradient = Brush.horizontalGradient(
        colors = listOf(RogMagenta, DeepPurple, VantaBlack)
    )

    val BentoGlassBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.12f),
            Color.White.copy(alpha = 0.03f),
            Color.Black.copy(alpha = 0.3f)
        )
    )

    val BentoBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.2f)
        )
    )
}

object SharedRogTheme {
    val DeepPurple = SharedPalette.DeepPurple
    val RogMagenta = SharedPalette.RogMagenta
    val Cyan = SharedPalette.ElectricCyan
    val VantaBlack = SharedPalette.VantaBlack
    val GlassOverlay = SharedPalette.GlassOverlayRog
}

object SharedCyberColors {
    val NightSkyBlue = SharedPalette.NightSky
    val DeepCityBlue = SharedPalette.DeepCityBlue
    val NeonMagenta = SharedPalette.NeonMagenta
    val CyberPink = SharedPalette.CyberPinkV3
    val ElectricCyan = SharedPalette.ElectricCyan
    val TechPurple = SharedPalette.TechPurple
    val WarningRed = SharedPalette.WarningRed
    val GlassOverlay = SharedPalette.GlassWhite
    val AbsoluteBlack = SharedPalette.AbsoluteBlack
    val DeepVoid = SharedPalette.VantaBlack
    val RogDarkGray = SharedPalette.StealthGray
    val SuccessGreen = SharedPalette.SuccessGreen
    val TechSurface = SharedPalette.TechSurface
    val GlassSurface = SharedPalette.GlassSurfaceCP
    val SoftViolet = SharedPalette.SoftViolet
    val MaverickCyan = SharedPalette.ElectricCyan // Alias
    val appCyan = SharedPalette.ElectricCyan // Alias
}

object SharedCyberColorsV2 {
    val AbsoluteBlack = SharedPalette.AbsoluteBlack
    val VoidGray = SharedPalette.VoidGray
    val CyanOpaque = SharedPalette.CyanOpaque
    val CyanAccent = SharedPalette.CyanAccent
    val MagentaOpaque = SharedPalette.MagentaOpaque
    val MagentaAccent = SharedPalette.MagentaAccent
    val GlassSurface = SharedPalette.GlassSurfaceV2
}

object SharedEliteCyberColors {
    val VantaBlack = SharedPalette.V2VantaBlack
    val RogCrimson = SharedPalette.RogCrimson
    val Cyan = SharedPalette.V2Cyan
    val TechSurface = SharedPalette.TechSurface
    val GlassSurface = SharedPalette.GlassSurfaceElite
}

// ALIAS PARA COMPATIBILIDAD
typealias AppPalette = SharedPalette
typealias RogTheme = SharedRogTheme
typealias CyberColorsV3 = SharedCyberColors
typealias CyberColorsV2 = SharedCyberColorsV2
typealias EliteCyberColors = SharedEliteCyberColors
typealias CPCyberColors = SharedCyberColors

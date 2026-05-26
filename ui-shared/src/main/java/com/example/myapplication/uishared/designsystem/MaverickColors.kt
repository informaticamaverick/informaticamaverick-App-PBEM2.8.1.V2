package com.example.myapplication.uishared.designsystem

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
 * MAVERICK COLORS - DEFINICIÓN UNIFICADA Y ORDENADA
 * Estilo: Cyberpunk / ROG / Stealth
 */
object MaverickColors {

    // ==========================================================================================
    // --- 🎨 SECCIÓN: COLORES SÓLIDOS (Ordenados de Oscuro a Claro) ---
    // ==========================================================================================

    // --- NIVEL 0: BASES Y NEGROS PROFUNDOS ---
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

    // --- NUEVOS TONOS ELITE MATTE ---
    val EliteMainBackground = Color(0xFF1C222B) 
    val EliteSurface = Color(0xFF12151A)        

    // --- NIVEL 1: TONOS DE INTERFAZ (IDE NEW UI) ---
    val AsEditorBg = Color(0xFF1E1F22)
    val AsCaretLine = Color(0xFF26282E)
    val AsSidebarBg = Color(0xFF2B2D30)
    val AsSelectionBg = Color(0xFF35373C)
    val AsPanelBorder = Color(0xFF393B40)

    // --- NIVEL 2: PÚRPURAS Y VIOLETAS ---
    val DeepPurple = Color(0xFF3E065F)
    val TechPurple = Color(0xFF4A00E0)
    val ElectricPurple = Color(0xFF7000FF)
    val RogMagenta = Color(0xFF7E1254)
    val SoftViolet = Color(0xFF8A2BE2)
    val ElectricViolet = Color(0xFF8F00FF)
    val GeminiPurple = Color(0xFF9B51E0)
    val GeminiAccent = Color(0xFFA78BFA)

    // --- NIVEL 3: MAGENTAS Y NEONES ---
    val CyberPinkV3 = Color(0xFFD90077)
    val MagentaNeon = Color(0xFFE91E63) 
    val NeonMagenta = Color(0xFFFF0055)
    val CyberPink = Color(0xFFFF00FF)

    // --- NIVEL 4: ROJOS, NARANJAS Y ORO ---
    val Garnet = Color(0xFF8B0000)
    val RogCrimson = Color(0xFFFF0044)
    val DeepRed = Color(0xFFFF0033)
    val ErrorRed = Color(0xFFFF0032)
    val CyberRed = Color(0xFFEF4444)
    val WarningRed = Color(0xFFFF2A2A)
    val OverdriveOrange = Color(0xFFFF5F00)
    val GoldPremium = Color(0xFFFFD700)

    // --- NIVEL 5: CIANOS Y VERDES (NEONES) ---
    val GeminiCyan = Color(0xFF22D3EE)
    val V2MaverickCyan = Color(0xFF00E5FF)
    val ElectricCyan = Color(0xFF00F0FF) 
    val NeonCyan = Color(0xFF00FFFF)
    val SuccessGreen = Color(0xFF00FF66)
    val AcidGreen = Color(0xFF00FF00)

    // --- NIVEL 6: TEXTOS ---
    val TextMuted = Color(0xFF94A3B8)
    val ROG_Text_Main = Color(0xFFE2E2E8)
    val TextMain = Color(0xFFE2E8F0)

    // --- NIVEL 7: ESCALA SLATE (MAVERICK STANDARD) ---
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFCBD5E1)
    val Slate400 = Color(0xFF94A3B8)
    val Slate600 = Color(0xFF475569)
    val Slate800 = Color(0xFF1E293B)

    // --- NIVEL 8: OPACIDADES Y EFECTOS GLASS ---
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

    // ==========================================================================================
    // --- 🌊 SECCIÓN: LISTA DE COLORES Y PINCELES (Brushes / Gradients) ---
    // ==========================================================================================

    // Gemini
    val GeminiBrush = Brush.linearGradient(
        colors = listOf(NeonCyan, GeminiAccent, CyberPink)
    )

    // Rog Standard (Horizontal)
    val RogGradient = Brush.horizontalGradient(
        colors = listOf(NeonMagenta, TechPurple)
    )

    // Maverick Standard A4 Gradient
    val MaverickBlueEnd = Color(0xFF2563EB)
    val MaverickBlueStart = Color(0xFF1E40AF)
    val MaverickA4Gradient = Brush.linearGradient(
        colors = listOf(MaverickBlueStart, MaverickBlueEnd)
    )

    // Bento Style
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

object RogTheme {
    val DeepPurple = MaverickColors.DeepPurple
    val RogMagenta = MaverickColors.RogMagenta
    val MaverickCyan = MaverickColors.ElectricCyan
    val VantaBlack = MaverickColors.VantaBlack
    val GlassOverlay = MaverickColors.GlassOverlayRog
}

object CyberColorsV3 {
    val NightSkyBlue = MaverickColors.NightSky
    val DeepCityBlue = MaverickColors.DeepCityBlue
    val NeonMagenta = MaverickColors.NeonMagenta
    val CyberPink = MaverickColors.CyberPinkV3
    val ElectricCyan = MaverickColors.ElectricCyan
    val TechPurple = MaverickColors.TechPurple
    val WarningRed = MaverickColors.WarningRed
    val GlassOverlay = MaverickColors.GlassWhite
    val AbsoluteBlack = MaverickColors.AbsoluteBlack
    val DeepVoid = MaverickColors.VantaBlack
    val RogDarkGray = MaverickColors.StealthGray
    val SuccessGreen = MaverickColors.SuccessGreen
}

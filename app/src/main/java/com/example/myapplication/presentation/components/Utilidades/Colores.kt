package com.example.myapplication.presentation.components.Utilidades

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
 * 
 * Reglas aplicadas:
 * 1. Orden por escala de colores (Luminosidad: Negro primero).
 * 2. Eliminación de duplicados por código de color.
 * 3. Prefijo V2 para nombres idénticos con distinto código.
 * 4. Separación de colores sólidos y funciones de lista (Brushes).
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

    // --- NIVEL 7: OPACIDADES Y EFECTOS GLASS ---
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

    // Rog Variantes (Vertical y Horizontal con 3 colores)
    val RogVerticalGradient = Brush.verticalGradient(
        colors = listOf(RogMagenta, DeepPurple, VantaBlack)
    )
    
    val RogHorizontalGradient = Brush.horizontalGradient(
        colors = listOf(RogMagenta, DeepPurple, VantaBlack)
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

// ==========================================================================================
// --- ⚙️ COMPATIBILIDAD (Aliases para evitar errores de compilación) ---
// ==========================================================================================

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

object CyberColorsV2 {
    val AbsoluteBlack = MaverickColors.AbsoluteBlack
    val VoidGray = MaverickColors.VoidGray
    val CyanOpaque = MaverickColors.CyanOpaque
    val CyanAccent = MaverickColors.CyanAccent
    val MagentaOpaque = MaverickColors.MagentaOpaque
    val MagentaAccent = MaverickColors.MagentaAccent
    val GlassSurface = MaverickColors.GlassSurfaceV2
}

object EliteCyberColors {
    val VantaBlack = MaverickColors.V2VantaBlack
    val RogCrimson = MaverickColors.RogCrimson
    val MaverickCyan = MaverickColors.V2MaverickCyan
    val TechSurface = MaverickColors.TechSurface
    val GlassSurface = MaverickColors.GlassSurfaceElite
}

object CPCyberColors {
    val DeepVoid = MaverickColors.V2DeepVoid
    val TechSurface = MaverickColors.V2TechSurface
    val MaverickCyan = MaverickColors.ElectricCyan
    val ElectricPurple = MaverickColors.ElectricPurple
    val SoftViolet = MaverickColors.SoftViolet
    val GlassSurface = MaverickColors.GlassSurfaceCP
}

// ==========================================================================================
// --- 🎨 PREVIEW: DICCIONARIO DE COLORES MAVERICK ---
// ==========================================================================================

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 400,
    heightDp = 1800
)
@Composable
fun PreviewMaverickColorPalette() {
    val colorCategories = listOf(
        "CORE & DARK" to listOf(
            "Absolute Black" to MaverickColors.AbsoluteBlack,
            "V2 Vanta Black" to MaverickColors.V2VantaBlack,
            "Night Sky" to MaverickColors.NightSky,
            "Rog Dark" to MaverickColors.ROG_Dark_Bg,
            "Stealth Gray" to MaverickColors.StealthGray
        ),
        "UI & IDE" to listOf(
            "AS Editor" to MaverickColors.AsEditorBg,
            "AS Sidebar" to MaverickColors.AsSidebarBg,
            "AS Panel" to MaverickColors.AsPanelBorder,
            "Deep City Blue" to MaverickColors.DeepCityBlue
        ),
        "NEONS & ACCENTS" to listOf(
            "Electric Cyan" to MaverickColors.ElectricCyan,
            "Neon Cyan" to MaverickColors.NeonCyan,
            "V2 Maverick Cyan" to MaverickColors.V2MaverickCyan,
            "Neon Magenta" to MaverickColors.NeonMagenta,
            "Cyber Pink" to MaverickColors.CyberPink,
            "Electric Violet" to MaverickColors.ElectricViolet
        ),
        "STATUS" to listOf(
            "Deep Red" to MaverickColors.DeepRed,
            "Warning" to MaverickColors.WarningRed,
            "Rog Crimson" to MaverickColors.RogCrimson,
            "Success" to MaverickColors.SuccessGreen,
            "Acid Green" to MaverickColors.AcidGreen,
            "Gold" to MaverickColors.GoldPremium
        )
    )

    val brushes = listOf(
        "Gemini Brush" to MaverickColors.GeminiBrush,
        "Rog Standard (H)" to MaverickColors.RogGradient,
        "Rog Vertical (3 colors)" to MaverickColors.RogVerticalGradient,
        "Rog Horizontal (3 colors)" to MaverickColors.RogHorizontalGradient,
        "Bento Glass" to MaverickColors.BentoGlassBrush,
        "Bento Border" to MaverickColors.BentoBorderBrush
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            "MAVERICK UNIFIED PALETTE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        colorCategories.forEach { (catName, colorList) ->
            Text(
                catName,
                color = MaverickColors.ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(colorList) { (name, color) ->
                    ColorSwatch(name, color)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "LISTAS DE COLORES & BRUSHES",
            color = MaverickColors.ElectricCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            brushes.forEach { (name, brush) ->
                BrushSwatch(name, brush)
            }
        }
    }
}

@Composable
fun ColorSwatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(color, RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        )
        Text(
            name,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
fun BrushSwatch(name: String, brush: Brush) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 120.dp, height = 40.dp)
                .background(brush, RoundedCornerShape(6.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        )
        Text(
            name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

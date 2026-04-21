package com.example.myapplication.presentation.components.Utilidades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================================================================
// --- 🏷️ ETIQUETAS PREMIUM MAVERICK ---
// ==========================================================================================

/**
 * Etiqueta Base Premium con efecto de elevación y cristal.
 */
@Composable
fun MaverickTag(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    backgroundColor: Color = MaverickColors.BentoDarkGlassBackground, // Fondo de la tarjeta
    accentColor: Color = MaverickColors.NeonCyan, // Color del borde y detalle
    useGlow: Boolean = true, // Si es true usa sombra de color (neón), si es false usa sombra negra estándar
    textColor: Color = Color.White, // Color del texto
    cornerRadius: Dp = 12.dp, // Redondeo de esquinas
    paddingVertical: Dp = 8.dp, // Espaciado vertical interno
    paddingHorizontal: Dp = 16.dp, // Espaciado horizontal interno
    fontSize: TextUnit = 13.sp, // Tamaño de la fuente
    elevation: Dp = 10.dp // Intensidad de la sombra/elevación
) {
    val shadowColor = if (useGlow) accentColor else Color.Black
    
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation, 
                shape = RoundedCornerShape(cornerRadius), 
                spotColor = shadowColor, // Sombra dinámica o neutra
                ambientColor = shadowColor
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .background(MaverickColors.BentoGlassBrush) // Capa de brillo premium
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(cornerRadius))
            .padding(vertical = paddingVertical, horizontal = paddingHorizontal),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (emoji != null) {
                Text(text = emoji, fontSize = (fontSize.value + 2).sp)
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = accentColor, 
                    modifier = Modifier.size((fontSize.value + 4).dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Variantes de Etiquetas Premium por concepto
 */
object EtiquetasPremium {
    
    // --- VARIANTES CON GLOW (NEÓN) ---
    @Composable
    fun PromoGlow(text: String = "OFERTA") = MaverickTag(
        text = text, emoji = "🔥", accentColor = MaverickColors.DeepRed, useGlow = true
    )

    @Composable
    fun NuevoGlow(text: String = "NUEVO") = MaverickTag(
        text = text, emoji = "✨", accentColor = MaverickColors.AcidGreen, useGlow = true
    )

    // --- VARIANTES SLEEK (SIN GLOW, SOMBRA NEGRA) ---
    @Composable
    fun PromoSleek(text: String = "OFERTA") = MaverickTag(
        text = text, 
        emoji = "🔥", 
        accentColor = MaverickColors.DeepRed, 
        useGlow = false, 
        backgroundColor = Color(0xFF121212) // Fondo más oscuro y sólido
    )

    @Composable
    fun NuevoSleek(text: String = "NUEVO") = MaverickTag(
        text = text, 
        emoji = "✨", 
        accentColor = MaverickColors.AcidGreen, 
        useGlow = false,
        backgroundColor = Color(0xFF121212)
    )

    @Composable
    fun Destacado(text: String) = MaverickTag(
        text = text,
        emoji = "💎",
        accentColor = MaverickColors.GeminiAccent,
        useGlow = true
    )

    @Composable
    fun InfoSleek(text: String) = MaverickTag(
        text = text,
        icon = MaverickIcons.Info,
        accentColor = MaverickColors.NeonCyan,
        useGlow = false
    )

    // --- VARIANTES MODERNAS (RECT + BORDES COLOR) ---
    @Composable
    fun ModernTech(text: String, accentColor: Color = MaverickColors.NeonCyan) = MaverickModernRectTag(
        text = text,
        accentColor = accentColor
    )

    @Composable
    fun ModernStatus(text: String, color: Color) = MaverickModernRectTag(
        text = text,
        accentColor = color
    )

    // --- VARIANTES RIBBON (CORTE TIJERA) ---
    @Composable
    fun TicketPremium(text: String, emoji: String = "🎟️") = MaverickRibbonTag(
        text = text,
        emoji = emoji,
        backgroundColor = MaverickColors.DeepSpace,
        accentColor = MaverickColors.GoldPremium
    )

    @Composable
    fun BadgePremium(text: String, emoji: String = "🥇") = MaverickRibbonTag(
        text = text,
        emoji = emoji,
        backgroundColor = Color(0xFF1A1A1A),
        accentColor = MaverickColors.ElectricViolet
    )
}

// ==========================================================================================
// --- ✂️ ETIQUETAS TIPO RIBBON (CORTE TIJERA) ---
// ==========================================================================================

/**
 * Etiqueta rectangular con un corte de "tijera" (V-notch) en el lado derecho.
 * Estilo premium sólido sin efectos de neón.
 */
@Composable
fun MaverickRibbonTag(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    backgroundColor: Color = Color(0xFF1E1E2E), // Color sólido de fondo
    accentColor: Color = Color.White, // Color del borde izquierdo y texto
    textColor: Color = Color.White, // Color del texto
    height: Dp = 34.dp, // Altura de la etiqueta
    fontSize: TextUnit = 12.sp, // Tamaño de letra
    paddingHorizontal: Dp = 16.dp, // Padding horizontal (antes del corte)
    notchSize: Dp = 12.dp // Tamaño del corte de tijera
) {
    val density = LocalDensity.current
    val notchPx = with(density) { notchSize.toPx() }

    // Definición de la forma con el corte a la derecha
    val ribbonShape = remember(notchPx) {
        GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - notchPx, size.height / 2f) // Punta hacia adentro (2 puntas resultantes)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    }

    Box(
        modifier = modifier
            .height(height) // Altura controlada
            .shadow(4.dp, ribbonShape) // Sombra negra estándar que sigue la forma
            .clip(ribbonShape)
            .background(backgroundColor)
            .border(
                width = 1.dp, 
                brush = Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.5f), Color.Transparent)), 
                shape = ribbonShape
            )
            .padding(start = paddingHorizontal, end = paddingHorizontal + notchSize),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = (fontSize.value + 2).sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================================================================
// --- 📏 ETIQUETAS MODERNAS RECTANGULARES (GLASS + BORDES COLOR) ---
// ==========================================================================================

/**
 * Etiqueta rectangular con bordes muy poco redondeados (sólo puntas).
 * Fondo gris claro casi transparente y bordes de color sólido.
 */
@Composable
fun MaverickModernRectTag(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = MaverickColors.NeonCyan, // Color del borde
    textColor: Color = Color.White,
    backgroundColor: Color = Color(0xFFE2E8F0).copy(alpha = 0.08f), // Gris claro casi transparente
    cornerRadius: Dp = 3.dp, // Bordes mínimamente redondeados
    fontSize: TextUnit = 11.sp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(1.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(cornerRadius))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = (fontSize.value + 2).sp)
                Spacer(modifier = Modifier.width(6.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size((fontSize.value + 3).dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text.uppercase(),
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==========================================================================================
// --- 📱 ETIQUETAS ESTILO M3 (ANDROID 16) ---
// ==========================================================================================

/**
 * Etiqueta estilo Material 3 Expressive.
 * Más minimalista pero con colores sólidos y formas suaves.
 */
@Composable
fun M3MaverickTag(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaverickColors.GlassWhite,
    contentColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp)) // Forma de píldora
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==========================================================================================
// --- 🖼️ PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PreviewMaverickTags() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Etiquetas Modernas (Rect + Borde Color):", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EtiquetasPremium.ModernTech("SYSTEM: ACTIVE")
            EtiquetasPremium.ModernStatus("ONLINE", MaverickColors.AcidGreen)
            EtiquetasPremium.ModernStatus("ALERTA", MaverickColors.DeepRed)
        }

        Text("Etiquetas Sleek (Sombra Negra):", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EtiquetasPremium.NuevoSleek()
            EtiquetasPremium.PromoSleek()
        }

        Text("Etiquetas Ribbon (Corte Tijera):", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            EtiquetasPremium.TicketPremium("Socio VIP")
            EtiquetasPremium.BadgePremium("Level Up")
        }

        Text("Etiquetas Glow (Con efecto Neón):", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            EtiquetasPremium.NuevoGlow()
            EtiquetasPremium.PromoGlow()
        }
        
        Text("Etiquetas Estándar M3:", color = Color.Gray)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            M3MaverickTag("Categoría", containerColor = MaverickColors.DeepSpace)
            M3MaverickTag("Pendiente", containerColor = MaverickColors.GoldPremium.copy(alpha = 0.2f), contentColor = MaverickColors.GoldPremium)
        }
    }
}

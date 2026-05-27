package com.example.myapplication.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import com.example.myapplication.presentation.registry.MaverickIcons

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

    // --- VARIANTES ELITE MODERNAS ---
    @Composable
    fun GlassElite(text: String) = EliteFrostedGlassBubble(text = text, icon = MaverickIcons.Info)

    @Composable
    fun DirtyGlass(text: String) = DirtyGlassBentoBubble(text = text)

    @Composable
    fun Depth3D(text: String) = Premium3DBubble(text = text)

    @Composable
    fun DirtyGlass3D(text: String) = SplitBento3DBubble(text = text, icon = Icons.Rounded.Edit)

    @Composable
    fun Holographic(text: String) = HolographicEliteBubble(text = text)

    @Composable
    fun Extreme3D(text: String) = Extreme3DBubble(text = text)
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

@Preview(showBackground = true, backgroundColor = 0xFF393B40)
@Composable
fun PreviewMaverickTags() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).background(Color(0xFF393B40)),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Burbujas Elite Modernas (3D & Glass):", color = MaverickColors.ElectricCyan, fontWeight = FontWeight.Bold)
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetasPremium.GlassElite("Elite Glass")
                EtiquetasPremium.DirtyGlass("Dirty Glass")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetasPremium.Depth3D("3D DEPTH")
                EtiquetasPremium.DirtyGlass3D("Split Bento")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EtiquetasPremium.Holographic("Holographic Elite")
                EtiquetasPremium.Extreme3D("3D MAX")
            }
        }

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

        Text("Burbujas Estilo iOS:", color = Color.Gray)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IOSStylePill(text = "Fecha: 24 May")
                IOSStylePillDark(text = "Modo Oscuro")
            }
            IOSStyleGlassPill(text = "Efecto Glassmorphism Premium")
        }
    }
}

// ==========================================================================================
// --- 💎 ETIQUETAS PREMIUM ELITE (NUEVAS TENDENCIAS) ---
// ==========================================================================================

/**
 * Burbuja con efecto de Vidrio Empañado (Frosted Glass / Glassmorphism).
 * Optimizado para fondos oscuros con máxima nitidez.
 * Simula el efecto de iOS usando capas de brillo y bordes sutiles.
 */
@Composable
fun EliteFrostedGlassBubble(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isDarkTheme: Boolean = true
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val glassBrush = if (isDarkTheme) MaverickColors.BentoGlassBrush else Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.7f))
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp, 
                shape = CircleShape, 
                ambientColor = Color.Black, 
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .background(glassBrush, CircleShape)
            .border(
                width = 1.dp, 
                brush = MaverickColors.BentoBorderBrush, 
                shape = CircleShape
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Burbuja con efecto de "Vidrio Sucio" / Frosted Bento.
 * Utiliza tonos grises claros, alta opacidad de blanco y un borde sutil 
 * para simular la textura mate y el blur del bento moderno.
 */
@Composable
fun DirtyGlassBentoBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    DirtyGlassBentoContainer(modifier = modifier) {
        Text(
            text = text,
            color = Color(0xFF334155), // Texto gris oscuro para contraste sobre fondo claro
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Contenedor base con efecto de "Vidrio Sucio" / Frosted Bento.
 * Permite contenido personalizado dentro de la estética Dirty Glass.
 */
@Composable
fun DirtyGlassBentoContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            // Capa base: Gris azulado muy claro (efecto "vidrio sucio")
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE2E8F0).copy(alpha = 0.85f), // Gris bento claro
                        Color(0xFFCBD5E1).copy(alpha = 0.75f)  // Gris ligeramente más oscuro
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            // Capa de brillo superior (especular)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            // Borde premium sutil
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Burbuja estilo Bento "Vidrio Sucio" con efecto 3D (sombra base) y diseño dividido.
 * Incluye un divisor vertical y una flecha hacia abajo en el lado derecho.
 */
@Composable
fun SplitBento3DBubble(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val backgroundColor = Color(0xFFE2E8F0)
    val textColor = Color(0xFF334155)
    
    Box(
        modifier = modifier
            // Sombra negra en la base (proyectada hacia abajo)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black,
                ambientColor = Color.Black.copy(alpha = 0.5f)
            )
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            // Sombra interna en la base para el efecto 3D
            .drawWithContent {
                drawContent()
                val thickness = 3.dp.toPx()
                drawLine(
                    color = Color.Black.copy(alpha = 0.15f),
                    start = Offset(0f, size.height - thickness),
                    end = Offset(size.width, size.height - thickness),
                    strokeWidth = thickness
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Lado Izquierdo: Icono + Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divisor Vertical sutil
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(textColor.copy(alpha = 0.1f))
            )

            // Lado Derecho: Flecha hacia abajo
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Burbuja con efecto 3D Real (Skeuomorphism Moderno).
 * Usa luces y sombras (biselado) para un efecto de relieve táctil.
 */
@Composable
fun Premium3DBubble(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaverickColors.NeonCyan
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF32323A), Color(0xFF121218))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .drawWithContent {
                drawContent()
                // Brillo superior (Efecto Bisel luz)
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(25f, 4f),
                    end = Offset(size.width - 25f, 4f),
                    strokeWidth = 1.5.dp.toPx()
                )
                // Sombra inferior (Profundidad)
                drawLine(
                    color = Color.Black.copy(alpha = 0.6f),
                    start = Offset(25f, size.height - 4f),
                    end = Offset(size.width - 25f, size.height - 4f),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.15f), Color.Transparent, accentColor.copy(alpha = 0.3f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
    }
}

/**
 * Burbuja Holográfica Elite con degradado iridiscente y brillo tipo cristal.
 */
@Composable
fun HolographicEliteBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = MaverickColors.GeminiAccent.copy(alpha = 0.4f))
            .background(MaverickColors.GeminiBrush, RoundedCornerShape(12.dp))
            // Capa de brillo diagonal
            .background(
                brush = Brush.linearGradient(
                    0.0f to Color.White.copy(alpha = 0.35f),
                    0.4f to Color.Transparent,
                    1.0f to Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 500f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp
        )
    }
}

/**
 * Burbuja con efecto 3D EXTREMO (Skeuomorphism marcado).
 * Posee sombras proyectadas y luces internas para un efecto "botón" físico.
 */
@Composable
fun Extreme3DBubble(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaverickColors.StealthGray
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(percent = 50),
                spotColor = Color.Black,
                ambientColor = Color.Black
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.8f), color)
                ),
                shape = RoundedCornerShape(percent = 50)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                ),
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

// ==========================================================================================
// --- 🫧 ETIQUETAS TIPO BURBUJA (ESTILO iOS) ---
// ==========================================================================================

/**
 * Burbuja flotante estilo iOS (Fechas/Etiquetas)
 * Usa sombras personalizadas y bordes para simular el relieve.
 */
@Composable
fun IOSStylePill(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x99FFFFFF), // Semi-transparente claro
    borderColor: Color = Color(0x66FFFFFF),    // Borde más claro para el bisel
    textColor: Color = Color.DarkGray,
    content: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(percent = 50), // Forma de píldora
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor), // Bisel sutil
        shadowElevation = 4.dp, // Sombra de elevación
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            if (content != null) {
                content()
            } else {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Variante de IOSStylePill optimizada para temas oscuros.
 */
@Composable
fun IOSStylePillDark(text: String) {
    IOSStylePill(
        text = text,
        backgroundColor = Color(0x661E293B), // Fondo oscuro traslúcido
        borderColor = Color(0x33FFFFFF),    // Brillo sutil arriba
        textColor = Color.White
    )
}

/**
 * Burbuja flotante estilo iOS con detalle de sombra en borde inferior (Glassmorphism).
 * El color en modo oscuro se ha optimizado a blanco ultra translúcido (0x2EFFFFFF)
 * para capturar la luz del fondo perfectamente y resaltar con máxima nitidez.
 */
@Composable
fun IOSStyleGlassPill(
    text: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    // Definimos los colores óptimos para modo Claro vs Oscuro
    val backgroundColor = if (isDarkTheme) {
        Color(0x2EFFFFFF) // Blanco translúcido luminoso para máxima visibilidad en modo oscuro
    } else {
        Color(0xE6FFFFFF) // Blanco espeso translúcido para modo claro
    }

    val borderColor = if (isDarkTheme) {
        Color(0x4DFFFFFF) // Brillo sutil blanco en los bordes
    } else {
        Color(0x1F000000) // Borde fino oscuro en modo claro
    }

    val textColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)

    Box(
        modifier = modifier
            // Sombra exterior proyectada hacia abajo
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = Color(0x1A000000),
                spotColor = if (isDarkTheme) Color(0x7F000000) else Color(0x26000000)
            )
            .background(color = backgroundColor, shape = CircleShape)
            .border(width = 1.dp, color = borderColor, shape = CircleShape)
            // Agregamos una sombra interna únicamente en el borde inferior para el efecto 3D
            .drawWithContent {
                drawContent()
                
                // Dibujamos un arco o línea de sombra en la base del elemento para crear profundidad
                val strokeThickness = 2.dp.toPx()
                val bottomShadowColor = if (isDarkTheme) {
                    Color(0x59000000) // Sombra inferior pronunciada oscura para resaltar en Dark Mode
                } else {
                    Color(0x12000000) // Sombra inferior suave para Light Mode
                }

                // Dibujar línea inferior sutilizada de sombra interna
                drawLine(
                    color = bottomShadowColor,
                    start = Offset(x = size.width * 0.15f, y = size.height - strokeThickness),
                    end = Offset(x = size.width * 0.85f, y = size.height - strokeThickness),
                    strokeWidth = strokeThickness
                )
            }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        )
    }
}










package com.example.myapplication.presentation.designsystem.components

import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.home.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================================================================
// --- 🌌 PREMIUM CYBER-ROG BACKGROUNDS (VERSION ELITE) ---
// ==========================================================================================

/**
 * 1. BG ROG PREMIUM SLASH
 * Fiel al estilo Republic of Gamers.
 * Fondo ultra profundo con cortes diagonales agresivos, glow magenta y micro-texturas técnicas.
 */
@Composable
fun BgRogPremiumSlash(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaverickColors.ROG_Dark_Bg)
            .drawBehind {
                // 1. Nebula Glow Magenta (Arriba Izquierda)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MaverickColors.NeonMagenta.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.width * 1.2f
                    )
                )

                // 2. Slash Diagonal Estilo Chasis
                val pathSlash = Path().apply {
                    moveTo(size.width * 0.3f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.5f)
                    lineTo(0f, size.height * 0.85f)
                    lineTo(0f, size.height * 0.6f)
                    close()
                }
                drawPath(
                    path = pathSlash,
                    brush = Brush.linearGradient(
                        colors = listOf(MaverickColors.V2DeepVoid, Color.Transparent),
                        start = Offset(size.width, 0f),
                        end = Offset(0f, size.height)
                    )
                )

                // 3. Líneas de Acento "Cyber Laser"
                val laserBrush = Brush.linearGradient(
                    colors = listOf(MaverickColors.NeonMagenta, MaverickColors.ElectricViolet)
                )

                drawLine(
                    brush = laserBrush,
                    start = Offset(size.width, size.height * 0.4f),
                    end = Offset(0f, size.height * 0.8f),
                    strokeWidth = 2.dp.toPx()
                )

                // Glow del Laser
                drawLine(
                    color = MaverickColors.NeonMagenta.copy(alpha = 0.2f),
                    start = Offset(size.width, size.height * 0.4f),
                    end = Offset(0f, size.height * 0.8f),
                    strokeWidth = 10.dp.toPx()
                )

                // 4. Micro-patrón ROG (Puntos)
                val spacing = 24.dp.toPx()
                for (x in (size.width * 0.6f).toInt()..size.width.toInt() step spacing.toInt()) {
                    for (y in 0..(size.height * 0.4f).toInt() step spacing.toInt()) {
                        drawCircle(
                            color = MaverickColors.NeonCyan.copy(alpha = 0.08f),
                            radius = 1.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }
            }
    ) { content() }
}

/**
 * 2. BG CYBER CITY PIXEL-GLASS
 * Inspirado en paisajes urbanos cyberpunk.
 * Efecto de profundidad con luces verticales y scanlines CRT.
 */
@Composable
fun BgCyberCityGlass(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaverickColors.AbsoluteBlack)
            .drawBehind {
                // 1. Resplandor de horizonte magenta
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaverickColors.NeonMagenta.copy(alpha = 0.12f)),
                        startY = size.height * 0.5f
                    )
                )

                // 2. Luces de edificios (Verticales Abstractas)
                val random = java.util.Random(1337)
                repeat(20) {
                    val w = (20 + random.nextInt(40)).dp.toPx()
                    val h = (size.height * 0.15f + random.nextFloat() * size.height * 0.4f)
                    val x = random.nextFloat() * size.width
                    val y = size.height - h

                    val accentColor = if (random.nextBoolean()) MaverickColors.ElectricCyan else MaverickColors.CyberPink

                    // Cuerpo de luz
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        topLeft = Offset(x, y),
                        size = Size(w, h)
                    )

                    // "Borde de neon" en la cima
                    drawRect(
                        color = accentColor.copy(alpha = 0.25f),
                        topLeft = Offset(x, y),
                        size = Size(w, 2.dp.toPx())
                    )
                }

                // 3. Scanlines CRT (Efecto Retro-Futurista)
                for (y in 0..size.height.toInt() step 8) {
                    drawLine(
                        color = Color.Black.copy(alpha = 0.35f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1.5f
                    )
                }

                // 4. Glitch / Luz Lateral Cyan
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MaverickColors.ElectricCyan.copy(alpha = 0.06f), Color.Transparent),
                        startX = 0f,
                        endX = size.width * 0.15f
                    )
                )
            }
    ) {
        // Overlay de Cristal Ahumado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.03f), Color.Transparent, Color.Black.copy(alpha = 0.1f))
                ))
        ) {
            content()
        }
    }
}


/**
 * Fondo para cabeceras con estilo ROG.
 */
@Composable
fun CyberMaverickHeaderBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = MaverickColors.NeonCyan,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaverickColors.V2DeepVoid, MaverickColors.ROG_Dark_Bg)
                )
            )
            .drawBehind {
                // Línea de acento inferior brillante
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor, Color.Transparent)
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            content()
        }
    }
}
/**
 * FONDO DE PANTALLA COMPLETA: STRIX GLASS
 * Estilo degradado diagonal con líneas sutiles y capa de cristal.
 */
@Composable
fun MaverickBackgroundStrix(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Capa de Arte (Fondo degradado ROG)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(RogTheme.RogMagenta, RogTheme.DeepPurple, RogTheme.VantaBlack),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
        )

        // 2. Líneas técnicas decorativas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 100f
            for (i in -10..20) {
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = Offset(i * spacing, 0f),
                    end = Offset(i * spacing + 500f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // 3. CAPA DE CRISTAL (Glassmorphism)
        // Esto difumina lo de atrás y oscurece para que tus textos se vean perfectos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RogTheme.GlassOverlay)
                .blur(10.dp) // Solo funciona en Android 12+, para versiones anteriores se usa el color sólido transparente
        )

        // 4. Contenido de la App
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * CABECERA HORIZONTAL: NEON HEADER
 * Para títulos de secciones, con borde inferior de neón.
 */
@Composable
fun MaverickHeader(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(RogTheme.VantaBlack)
    ) {
        // Fondo con brillo cian muy sutil
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RogTheme.MaverickCyan.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.5f
                )
            )
        }

        // Capa Glassmorphism interna
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )

        // Textos
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = subtitle.uppercase(),
                color = RogTheme.MaverickCyan,
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Línea de Neón inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, RogTheme.RogMagenta, RogTheme.MaverickCyan)
                    )
                )
        )
    }
}

/**
 * 3. FONDOS DE PANTALLA COMPLETA (BACKGROUNDS)
 */

@Composable
fun BgStrixElite(content: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo base con el degradado de la imagen ROG
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(MaverickColors.RogMagenta, MaverickColors.DeepPurple, MaverickColors.VantaBlack),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
        )

        // Líneas técnicas diagonales (Strix Lines)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 80f
            for (i in -10..30) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(i * spacing, 0f),
                    end = Offset(i * spacing + 400f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // CAPA GLASS (Efecto cristal para legibilidad)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaverickColors.GlassOverlayRog)
        )

        // Contenedor de contenido
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * 4. CABECERAS DE SECCIÓN (HEADERS)
 */

// Cabecera: ESTADO DEL HARDWARE
@Composable
fun HeaderHardware() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp))
            .background(MaverickColors.VantaBlack)
    ) {
        // Brillo magenta sutil de fondo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MaverickColors.RogMagenta.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 400f
                )
            )
        }

        // Línea de Neón Inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(MaverickColors.RogMagenta, MaverickColors.ElectricCyan)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "ESTADO DEL", style = MaverickTypography.HeaderSubtitle)
            Text(text = "HARDWARE", style = MaverickTypography.HeaderTitle)
        }
    }
}

// Cabecera: MAVERICK CLOUD
@Composable
fun HeaderMaverickCloud() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(MaverickColors.VantaBlack)
    ) {
        // Grid de Pixeles (Inspirado en el 8-bit City)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40f
            for (x in 0..size.width.toInt() step step.toInt()) {
                for (y in 0..size.height.toInt() step step.toInt()) {
                    drawCircle(MaverickColors.ElectricCyan.copy(alpha = 0.05f), 1.dp.toPx(), Offset(x.toFloat(), y.toFloat()))
                }
            }
        }

        // Capa de cristal sutil
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "CONEXIÓN", style = MaverickTypography.HeaderSubtitle.copy(color = MaverickColors.RogMagenta))
                Text(text = "MAVERICK CLOUD", style = MaverickTypography.HeaderTitle)
            }

            // Icono minimalista
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, MaverickColors.ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawPath(
                        path = Path().apply {
                            moveTo(size.width / 2, 0f)
                            lineTo(size.width, size.height / 2)
                            lineTo(size.width / 2, size.height)
                            lineTo(0f, size.height / 2)
                            close()
                        },
                        color = MaverickColors.ElectricCyan
                    )
                }
            }
        }
    }
}

/**
 * 5. EJEMPLO DE USO (PANTALLA COMPLETA DARK MODE)
 */
@Composable
fun MaverickMainScreen() {
    BgStrixElite {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderHardware()

            Spacer(modifier = Modifier.height(20.dp))

            // Ejemplo de tarjeta con Glassmorphism
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, MaverickColors.BorderColor, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "ESTA TARJETA TIENE EFECTO CRISTAL SOBRE EL FONDO ROG. EL TEXTO ES TOTALMENTE LEGIBLE.",
                    style = MaverickTypography.BodyText
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HeaderMaverickCloud()
        }
    }
}
/**
 * FONDO DE PANTALLA COMPLETA (DARK MODE + GLASS)
 */
@Composable
fun MaverickScreenContainer(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaverickColors.V2DeepVoid)
    ) {
        // Efecto de cristal superior (Glass overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * CABECERA: ESTADO DEL HARDWARE
 */
@Composable
fun HardwareStatusHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaverickColors.CardBg)
            .border(1.dp, MaverickColors.BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ESTADO DEL",
                style = MaverickStyle.SubtitleStyle.copy(color = MaverickColors.ElectricCyan)
            )
            Text(
                text = "HARDWARE",
                style = MaverickStyle.OrbitronLike
            )
        }

        // Indicador de neón en la esquina
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(8.dp)
                .background(MaverickColors.ElectricCyan, RoundedCornerShape(50))
        )
    }
}

/**
 * CABECERA: CONEXIÓN MAVERICK CLOUD
 */
@Composable
fun MaverickCloudHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaverickColors.CardBg)
            .border(1.dp, MaverickColors.BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CONEXIÓN",
                style = MaverickStyle.SubtitleStyle
            )
            Text(
                text = "MAVERICK CLOUD",
                style = MaverickStyle.OrbitronLike
            )
        }
    }
}

/**
 * TARJETA DARK MODE (Cuerpo)
 */
@Composable
fun DarkModeCard() {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .width(260.dp)
            .fillMaxHeight(0.6f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaverickColors.CardBg)
            .border(1.dp, MaverickColors.BorderColor, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            // Línea magenta horizontal de la imagen
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(MaverickColors.MagentaNeon)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "DARK\nMODE",
                style = MaverickStyle.OrbitronLike.copy(fontSize = 28.sp, lineHeight = 32.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OPTIMIZADO PARA MAVERICK",
                style = MaverickStyle.SubtitleStyle.copy(color = MaverickColors.ElectricCyan)
            )
        }
    }
}

// ==========================================================================================
// --- 🎨 PREVIEWS ---
// ==========================================================================================

@Preview(name = "1. ROG Slash Premium", showBackground = true)
@Composable
fun PreviewBgRogPremiumSlash() {
    BgRogPremiumSlash {
        Text(
            "ROG ELITE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 40.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview(name = "2. Cyber City Glass", showBackground = true)
@Composable
fun PreviewBgCyberCityGlass() {
    BgCyberCityGlass {
        Text(
            "NIGHT CITY",
            color = MaverickColors.NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview(name = "3. Strix Elite", showBackground = true)
@Composable
fun PreviewBgStrixElite() {
    BgStrixElite {
        Text(
            "STRIX ELITE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview(name = "4. Header Hardware", showBackground = true)
@Composable
fun PreviewHeaderHardware() {
    HeaderHardware()
}

@Preview(name = "5. Header Maverick Cloud", showBackground = true)
@Composable
fun PreviewHeaderMaverickCloud() {
    HeaderMaverickCloud()
}
@Preview(name = "6. Cyber Maverick Header", showBackground = true)
@Composable
fun PreviewCyberMaverickHeaderBackground() {
    CyberMaverickHeaderBackground {
        Text("DASHBOARD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Preview(name = "7. Maverick Strix Glass", showBackground = true)
@Composable
fun PreviewMaverickBackgroundStrix() {
    MaverickBackgroundStrix {
        Text(
            "STRIX DESIGN",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview(name = "8. Maverick Neon Header", showBackground = true)
@Composable
fun PreviewMaverickHeader() {
    Column {
        MaverickHeader(title = "Hardware", subtitle = "Explorar")
    }
}

@Preview(name = "9. Main Screen Example", showBackground = true)
@Composable
fun PreviewMaverickMainScreen() {
    MaverickMainScreen()
}

@Preview(name = "10. Maverick Screen Container", showBackground = true)
@Composable
fun PreviewMaverickScreenContainer() {
    MaverickScreenContainer {
        HardwareStatusHeader()
        Spacer(modifier = Modifier.height(8.dp))
        MaverickCloudHeader()
        Spacer(modifier = Modifier.height(8.dp))
        DarkModeCard()
    }
}

@Preview(name = "11. Hardware Status Header", showBackground = true)
@Composable
fun PreviewHardwareStatusHeader() {
    Box(modifier = Modifier.background(Color.Black).padding(16.dp)) {
        HardwareStatusHeader()
    }
}

@Preview(name = "12. Maverick Cloud Header", showBackground = true)
@Composable
fun PreviewMaverickCloudHeader() {
    Box(modifier = Modifier.background(Color.Black).padding(16.dp)) {
        MaverickCloudHeader()
    }
}

@Preview(name = "13. Dark Mode Card", showBackground = true)
@Composable
fun PreviewDarkModeCard() {
    Box(modifier = Modifier.background(Color.Black).fillMaxSize().padding(16.dp)) {
        DarkModeCard()
    }
}










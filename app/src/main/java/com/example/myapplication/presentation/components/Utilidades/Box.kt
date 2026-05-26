package com.example.myapplication.presentation.components.Utilidades

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ==========================================================================================
// --- 🏷️ VARIACIONES DE HEADERS (Cabeceras) ---
// ==========================================================================================

/** 1. Header ROG Angular: Corte diagonal masivo en la derecha con gradiente. */
@Composable
fun HeaderRogAngular(
    title: String,
    subtitle: String,
    colors: List<Color> = listOf(CyberColorsV3.NeonMagenta, CyberColorsV3.TechPurple),
    onBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(CutCornerShape(bottomEnd = 40.dp))
            .background(
                brush = Brush.horizontalGradient(colors = colors)
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Column {
                Text(title.uppercase(), style = CyberTypography.TitleTech.copy(fontSize = 19.sp))
                Text(subtitle.uppercase(), style = CyberTypography.MonospaceData.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

/** 2. Header Pixel Glitch: Simula efecto de desalineación RGB estilo retro/pixel. */
@Composable
fun HeaderPixelGlitch(title: String) {
    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
        Text(title.uppercase(), style = CyberTypography.TitleTech.copy(color = CyberColorsV3.ElectricCyan), modifier = Modifier.offset(x = (-2).dp, y = 2.dp))
        Text(title.uppercase(), style = CyberTypography.TitleTech.copy(color = CyberColorsV3.NeonMagenta), modifier = Modifier.offset(x = 2.dp, y = (-2).dp))
        Text(title.uppercase(), style = CyberTypography.TitleTech.copy(color = Color.White))
    }
}

/** 3. Header Tech Line: Minimalista, con línea cyan gruesa a la izquierda. */
@Composable
fun HeaderTechLine(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(50.dp).background(CyberColorsV3.DeepCityBlue),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(CyberColorsV3.ElectricCyan))
        Spacer(Modifier.width(16.dp))
        Text(title.uppercase(), style = CyberTypography.BodyCyber, color = Color.White)
    }
}

/** 4. Header Neon Frame: Borde brillante hueco, perfecto para botones gigantes o secciones clave. */
@Composable
fun HeaderNeonFrame(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .border(2.dp, CyberColorsV3.NeonMagenta, RoundedCornerShape(8.dp))
            .background(CyberColorsV3.NeonMagenta.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = CyberTypography.TitleTech, color = CyberColorsV3.NeonMagenta)
    }
}

/** 5. Header Data Block: Estilo pixel art, bloques pequeños decorativos. */
@Composable
fun HeaderDataBlock(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { Box(modifier = Modifier.size(12.dp).background(CyberColorsV3.WarningRed)) }
        }
        Spacer(Modifier.width(12.dp))
        Text(title, style = CyberTypography.MonospaceData, color = Color.White)
        Spacer(Modifier.weight(1f))
        Box(modifier = Modifier.height(2.dp).weight(2f).background(CyberColorsV3.WarningRed.copy(alpha = 0.3f)))
    }
}

/**
 * Header Estilo "Tech Slice": Corte diagonal en la esquina superior derecha.
 */
@Composable
fun CyberMaverickTechHeader(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00FFFF),
    content: @Composable BoxScope.() -> Unit
) {
    val deepPurple = Color(0xFF1B0B3B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .drawBehind {
                val cornerCut = 20.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width - cornerCut, 0f)
                    lineTo(size.width, cornerCut)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = path, color = deepPurple)
                
                drawLine(
                    color = accentColor,
                    start = Offset(size.width - cornerCut, 0f),
                    end = Offset(size.width, cornerCut),
                    strokeWidth = 2.dp.toPx()
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.padding(start = 20.dp)) { content() }
    }
}

/**
 * Header Estilo "Sleek Bar": Minimalista con degradado lateral sutil.
 */
@Composable
fun CyberMaverickSleekTitle(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00FFFF),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                )
            )
            .drawBehind {
                drawLine(
                    color = accentColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(modifier = Modifier.padding(start = 16.dp)) { content() }
    }
}

@Composable
fun CyberMaverickNeonBox(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00FFFF),
    backgroundColor: Color = Color(0xFF0D0221),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                drawRect(color = backgroundColor)
                val neonColor = accentColor.copy(alpha = 0.3f)
                drawRect(
                    color = neonColor,
                    style = Stroke(width = 1.dp.toPx())
                )
                val L = 15.dp.toPx()
                val sw = 3.dp.toPx()
                // Top-Left Corner
                drawLine(accentColor, Offset(0f, 0f), Offset(L, 0f), sw)
                drawLine(accentColor, Offset(0f, 0f), Offset(0f, L), sw)
                // Top-Right Corner
                drawLine(accentColor, Offset(size.width, 0f), Offset(size.width - L, 0f), sw)
                drawLine(accentColor, Offset(size.width, 0f), Offset(size.width, L), sw)
                // Bottom-Left Corner
                drawLine(accentColor, Offset(0f, size.height), Offset(L, size.height), sw)
                drawLine(accentColor, Offset(0f, size.height), Offset(0f, size.height - L), sw)
                // Bottom-Right Corner
                drawLine(accentColor, Offset(size.width, size.height), Offset(size.width - L, size.height), sw)
                drawLine(accentColor, Offset(size.width, size.height), Offset(size.width, size.height - L), sw)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

/**
 * Header Estilo "Neon Box": Marco de neón completo para títulos destacados.
 */
@Composable
fun CyberMaverickNeonBoxHeader(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00FFFF),
    content: @Composable BoxScope.() -> Unit
) {
    CyberMaverickNeonBox(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp),
        accentColor = accentColor,
        content = {
            Box(modifier = Modifier.align(Alignment.Center)) {
                content()
            }
        }
    )
}

/**
 * Header de sección minimalista con barra lateral.
 */
@Composable
fun CyberV2SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color = CyberColorsV2.CyanAccent
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.1f), Color.Transparent)
                )
            )
            .drawBehind {
                drawLine(
                    color = accentColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 6.dp.toPx()
                )
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title.uppercase(),
            color = Color.White,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun HeaderAuroraCP(title: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)).background(CPCyberColors.TechSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(brush = Brush.radialGradient(listOf(CPCyberColors.ElectricPurple.copy(alpha=0.3f), Color.Transparent)), center = Offset(0f, 0f), radius = size.height * 1.5f)
            drawCircle(brush = Brush.radialGradient(listOf(CPCyberColors.MaverickCyan.copy(alpha=0.2f), Color.Transparent)), center = Offset(size.width, size.height), radius = size.height * 1.5f)
        }
        Text(title.uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
    }
}

@Composable
fun HeaderSlashCP(title: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)).background(CPCyberColors.TechSurface),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(size.width * 0.6f, 0f); lineTo(size.width, 0f); lineTo(size.width, size.height)
                lineTo(size.width * 0.5f, size.height); close()
            }
            drawPath(path, CPCyberColors.ElectricPurple.copy(alpha = 0.1f))
            drawLine(CPCyberColors.MaverickCyan, Offset(size.width * 0.6f, 0f), Offset(size.width * 0.5f, size.height), strokeWidth = 4.dp.toPx())
        }
        Text(title.uppercase(), color = CPCyberColors.MaverickCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(start = 24.dp))
    }
}

@Composable
fun HeaderGlassCP(title: String) {
    Box(modifier = Modifier.fillMaxWidth().height(90.dp).background(CPCyberColors.DeepVoid).padding(8.dp)) {
        Box(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(CPCyberColors.GlassSurface)
                .border(1.dp, CPCyberColors.MaverickCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .drawBehind {
                    drawLine(brush = Brush.horizontalGradient(listOf(CPCyberColors.ElectricPurple, CPCyberColors.MaverickCyan)), start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 6.dp.toPx())
                },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(title.uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, modifier = Modifier.padding(start = 24.dp))
        }
    }
}

@Composable
fun HeaderTechFrameCP(title: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)).background(CPCyberColors.TechSurface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val len = 20.dp.toPx(); val sw = 3.dp.toPx()
            drawLine(CPCyberColors.MaverickCyan, Offset(16f, 16f), Offset(16f + len, 16f), sw)
            drawLine(CPCyberColors.MaverickCyan, Offset(16f, 16f), Offset(16f, 16f + len), sw)
            drawLine(CPCyberColors.ElectricPurple, Offset(size.width - 16f, size.height - 16f), Offset(size.width - 16f - len, size.height - 16f), sw)
            drawLine(CPCyberColors.ElectricPurple, Offset(size.width - 16f, size.height - 16f), Offset(size.width - 16f, size.height - 16f - len), sw)
        }
        Text(title.uppercase(), color = Color(0xFFE0E0E0), fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
    }
}

@Composable
fun HeaderDataStreamCP(title: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(16.dp)).background(CPCyberColors.TechSurface),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(brush = Brush.verticalGradient(listOf(CPCyberColors.ElectricPurple, CPCyberColors.MaverickCyan)), topLeft = Offset(0f, 0f), size = Size(6.dp.toPx(), size.height))
        }
        Text(title.uppercase(), color = CPCyberColors.SoftViolet, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, modifier = Modifier.padding(start = 24.dp))
    }
}

// ==========================================================================================
// --- 📦 CONTENT BOXES (Cajas de Contenido) ---
// ==========================================================================================

/** 1. Box Cyber Chamfer: Esquinas cortadas (ROG style) con borde morado. */
@Composable
fun BoxCyberChamfer(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(CyberColorsV3.DeepCityBlue)
            .border(1.dp, CyberColorsV3.TechPurple, CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(20.dp)
    ) { content() }
}

/** 2. Box Holographic Grid: Fondo semitransparente con rejilla interna. */
@Composable
fun BoxHoloGrid(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .background(CyberColorsV3.GlassOverlay)
            .drawBehind {
                val step = 15.dp.toPx()
                for(y in 0..size.height.toInt() step step.toInt()) {
                    drawLine(CyberColorsV3.ElectricCyan.copy(alpha = 0.05f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                }
            }
            .border(1.dp, CyberColorsV3.ElectricCyan.copy(alpha = 0.3f))
            .padding(16.dp)
    ) { content() }
}

/** 3. Box Alert Pixel: Borde rojo discontinuo, simulando un área de advertencia. */
@Composable
fun BoxAlertPixel(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .background(CyberColorsV3.WarningRed.copy(alpha = 0.05f))
            .drawBehind {
                drawRect(
                    color = CyberColorsV3.WarningRed,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                )
            }
            .padding(16.dp)
    ) { content() }
}

/** 4. Box Neon Ribbon: Caja oscura con una "cinta" de luz neón en la parte superior. */
@Composable
fun BoxNeonRibbon(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .background(CyberColorsV3.NightSkyBlue)
            .drawBehind {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(CyberColorsV3.ElectricCyan, CyberColorsV3.NeonMagenta)),
                    size = Size(size.width, 4.dp.toPx())
                )
            }
            .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) { content() }
}

/** 5. Box Wireframe Target: Esquinas con mirillas (crosshairs) de francotirador/tech. */
@Composable
fun BoxWireframeTarget(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .background(Color.Transparent)
            .drawBehind {
                val length = 20.dp.toPx()
                val stroke = 2.dp.toPx()
                val color = CyberColorsV3.CyberPink

                drawLine(color, Offset(0f, 0f), Offset(length, 0f), stroke)
                drawLine(color, Offset(0f, 0f), Offset(0f, length), stroke)
                drawLine(color, Offset(size.width, 0f), Offset(size.width - length, 0f), stroke)
                drawLine(color, Offset(size.width, 0f), Offset(size.width, length), stroke)
                drawLine(color, Offset(0f, size.height), Offset(length, size.height), stroke)
                drawLine(color, Offset(0f, size.height), Offset(0f, size.height - length), stroke)
                drawLine(color, Offset(size.width, size.height), Offset(size.width - length, size.height), stroke)
                drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - length), stroke)
            }
            .padding(16.dp)
    ) { content() }
}

/**
 * Tarjeta translúcida sobre el fondo negro.
 */
@Composable
fun CyberV2SurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(CyberColorsV2.GlassSurface)
            .border(1.dp, CyberColorsV2.CyanOpaque)
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * ESTRUCTURAS BENTO PREMIUM
 */

@Composable
fun BentoCardPremium(
    title: String,
    modifier: Modifier = Modifier,
    headerEmoji: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black)
            .clip(RoundedCornerShape(28.dp))
            .background(MaverickColors.BentoDarkGlassBackground)
            .background(MaverickColors.BentoGlassBrush)
            .border(1.2.dp, MaverickColors.BentoBorderBrush, RoundedCornerShape(28.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            SectionHeaderWithDivider(text = title, emoji = headerEmoji)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun BentoCardPremium2(
    title: String,
    modifier: Modifier = Modifier,
    headerEmoji: String? = null,
    initialExpanded: Boolean = true,
    accentColor: Color = MaverickColors.GeminiAccent,
    topCornerRadius: Dp = 8.dp,
    bottomCornerRadius: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ArrowRotation"
    )

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 1.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (headerEmoji != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = headerEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.9f), Color.Transparent)
                            )
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = title.uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir/Colapsar",
                    tint = accentColor,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation)
                )
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(tween(200)),
            exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(tween(200))
        ) {
            val cardShape = RoundedCornerShape(
                topStart = topCornerRadius,
                topEnd = topCornerRadius,
                bottomStart = bottomCornerRadius,
                bottomEnd = bottomCornerRadius
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = cardShape,
                        ambientColor = Color.Black,
                        spotColor = accentColor.copy(alpha = 0.15f)
                    )
                    .clip(cardShape)
                    .background(Color(0xFF8A8A9E).copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun BentoBottomSheetContent(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String = "✨",
    showPrimaryButton: Boolean = false,
    primaryButtonText: String = "Aceptar",
    primaryButtonEmoji: String = "✅",
    primaryButtonColor: Color = Color(0xFF22C55E),
    onPrimaryButtonClick: () -> Unit = {},
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffsetBtn = remember { Animatable(0f) }
    val shakeOffsetClose = remember { Animatable(0f) }

    suspend fun triggerShake(animatable: Animatable<Float, AnimationVector1D>) {
        repeat(4) {
            animatable.animateTo(10f, tween(50, easing = LinearEasing))
            animatable.animateTo(-10f, tween(50, easing = LinearEasing))
        }
        animatable.animateTo(0f, tween(50, easing = LinearEasing))
    }

    val contentAlpha = remember { Animatable(0f) }
    val translateY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { contentAlpha.animateTo(1f, tween(600)) }
        launch { translateY.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .shadow(40.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color(0xFF0F0F12))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = contentAlpha.value
                        translationY = translateY.value
                    }
            ) {
                content()
            }
            if (showPrimaryButton) {
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.offset(x = shakeOffsetBtn.value.dp)) {
                    BentoActionButton(
                        text = primaryButtonText,
                        emoji = primaryButtonEmoji,
                        color = primaryButtonColor,
                        onClick = {
                            coroutineScope.launch {
                                triggerShake(shakeOffsetBtn)
                                onPrimaryButtonClick()
                            }
                        }
                    )
                }
            }
            bottomContent?.let {
                Spacer(modifier = Modifier.height(16.dp))
                it()
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .offset(y = 10.dp, x = shakeOffsetClose.value.dp)
        ) {
            CarcasaAccionBento(
                emoji = "❌",
                label = "",
                accentColor = Color(0xFFEF4444),
                onClick = {
                    coroutineScope.launch {
                        triggerShake(shakeOffsetClose)
                        kotlinx.coroutines.delay(250)
                        onClose()
                    }
                },
                size = 42.dp,
                emojiSize = 20.sp
            )
        }
    }
}

// ==========================================================================================
// --- 💬 CHAT BUBBLES (Burbujas de Chat) ---
// ==========================================================================================

/** 1. Chat User (Strix): Gradiente magenta, alineado a la derecha, esquina inferior derecha cortada. */
@Composable
fun ChatBubbleUser(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(CutCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 0.dp))
                .background(Brush.horizontalGradient(listOf(CyberColorsV3.TechPurple, CyberColorsV3.NeonMagenta)))
                .padding(12.dp)
        ) {
            Text(text, style = CyberTypography.BodyCyber, color = Color.White)
        }
    }
}

/** 2. Chat System (Terminal): Transparente, borde cyan, texto monoespaciado. */
@Composable
fun ChatBubbleSystem(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(1.dp, CyberColorsV3.ElectricCyan, RoundedCornerShape(4.dp))
                .background(CyberColorsV3.ElectricCyan.copy(alpha = 0.05f))
                .padding(12.dp)
        ) {
            Text("> $text", style = CyberTypography.MonospaceData)
        }
    }
}

/** 3. Chat NPC/Glitch: Bloque cuadrado brutalista, indicador de color rojo. */
@Composable
fun ChatBubbleNPC(name: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(CyberColorsV3.DeepCityBlue)
                .padding(12.dp)
        ) {
            Text(name.uppercase(), style = CyberTypography.MonospaceData, color = CyberColorsV3.WarningRed, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = CyberTypography.BodyCyber, color = Color.LightGray)
        }
    }
}

/** 1. Chat: ROG Elite (Cola Arriba). Esquina superior izquierda cortada para la "cola". */
@Composable
fun ChatBubbleRogElite(text: String) {
    Column(modifier = Modifier.padding(start = 16.dp)) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(CutCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(Brush.linearGradient(listOf(CyberColorsV3.TechPurple, CyberColorsV3.NeonMagenta)))
                .padding(12.dp)
        ) {
            Text(text, style = CyberTypography.BodyCyber, color = Color.White)
        }
    }
}

/** 2. Chat: Ghost Signal (Cola Arriba). Borde cyan con "cola" geométrica sutil. */
@Composable
fun ChatBubbleGhost(text: String) {
    Column(modifier = Modifier.padding(start = 24.dp)) {
        Box(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .drawBehind {
                    val path = Path().apply {
                        moveTo(10.dp.toPx(), 0f)
                        lineTo(20.dp.toPx(), (-10).dp.toPx())
                        lineTo(30.dp.toPx(), 0f)
                    }
                    drawPath(path, CyberColorsV3.ElectricCyan.copy(alpha = 0.1f))
                }
                .border(1.dp, CyberColorsV3.ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .background(CyberColorsV3.ElectricCyan.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(text, style = CyberTypography.MonospaceData, fontSize = 13.sp)
        }
    }
}

/** 3. Chat: Brute Block (Cola Arriba). Estilo pixelado cuadrado, cola de bloque. */
@Composable
fun ChatBubbleBrute(text: String) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        Box(Modifier.offset(x = 12.dp).size(8.dp).background(CyberColorsV3.WarningRed))
        Box(
            modifier = Modifier
                .widthIn(max = 250.dp)
                .background(CyberColorsV3.RogDarkGray)
                .border(1.dp, CyberColorsV3.WarningRed.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            Text(text, style = CyberTypography.BodyCyber, color = Color.White)
        }
    }
}

// ==========================================================================================
// --- 🔍 SEARCH BARS (Barras de Búsqueda) ---
// ==========================================================================================

/** 1. Search Bar: Cyber-Cut. Con corte diagonal ROG y borde neón. */
@Composable
fun SearchBarCyberCut(placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .background(CyberColorsV3.RogDarkGray)
            .border(1.dp, CyberColorsV3.NeonMagenta.copy(alpha = 0.4f), CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(placeholder, style = CyberTypography.BodyCyber.copy(fontSize = 14.sp, color = Color.Gray))
    }
}

/** 2. Search Bar: Stealth Glass. Efecto de cristal oscuro y borde cyan minimalista. */
@Composable
fun SearchBarStealthGlass(placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(4.dp))
            .drawBehind {
                drawLine(
                    CyberColorsV3.ElectricCyan.copy(alpha = 0.6f),
                    Offset(0f, size.height),
                    Offset(size.width * 0.3f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(placeholder, style = CyberTypography.MonospaceData.copy(fontSize = 13.sp, color = CyberColorsV3.ElectricCyan.copy(alpha = 0.5f)))
    }
}

/** 3. Search Bar: Pixel Scan. Estilo retro-pixel con indicador de escaneo. */
@Composable
fun SearchBarPixelScan(placeholder: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(2.dp, CyberColorsV3.WarningRed.copy(alpha = 0.2f), RectangleShape)
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(CyberColorsV3.WarningRed))
            Spacer(Modifier.width(12.dp))
            Text(placeholder.uppercase(), style = CyberTypography.MonospaceData.copy(color = Color.White))
        }
    }
}

@Preview(name = "Headers Variation", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewHeaders() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderRogAngular(title = "Rog Angular", subtitle = "Subtítulo de prueba")
        HeaderPixelGlitch(title = "Pixel Glitch")
        HeaderTechLine(title = "Tech Line")
        HeaderNeonFrame(title = "Neon Frame")
        HeaderDataBlock(title = "Data Block")
    }
}

@Preview(name = "Cyber Maverick Headers", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewCyberMaverickHeaders() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CyberMaverickTechHeader { Text("Tech Slice Header", color = Color.White) }
        CyberMaverickSleekTitle { Text("Sleek Bar Title", color = Color.White) }
        CyberMaverickNeonBoxHeader { Text("Neon Box Header", color = Color.White) }
        CyberV2SectionHeader(title = "V2 Section Header")
    }
}

@Preview(name = "CP Headers", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewCPHeaders() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderAuroraCP(title = "Aurora CP")
        HeaderSlashCP(title = "Slash CP")
        HeaderGlassCP(title = "Glass CP")
        HeaderTechFrameCP(title = "Tech Frame CP")
        HeaderDataStreamCP(title = "Data Stream CP")
    }
}

@Preview(name = "Content Boxes", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewContentBoxes() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BoxCyberChamfer(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Cyber Chamfer Box", color = Color.White)
        }
        BoxHoloGrid(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Holo Grid Box", color = Color.White)
        }
        BoxAlertPixel(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Alert Pixel Box", color = Color.White)
        }
        BoxNeonRibbon(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Neon Ribbon Box", color = Color.White)
        }
        BoxWireframeTarget(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            Text("Wireframe Target Box", color = Color.White)
        }
    }
}

@Preview(name = "Chat Bubbles", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewChatBubbles() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChatBubbleUser(text = "Hola, esta es una prueba de usuario.")
        ChatBubbleSystem(text = "Sistema inicializado correctamente.")
        ChatBubbleNPC(name = "Mave-Rick", text = "Bienvenido a la red, Maverick.")
        ChatBubbleRogElite(text = "Mensaje estilo ROG Elite.")
        ChatBubbleGhost(text = "Señal fantasma detectada...")
        ChatBubbleBrute(text = "Bloque de datos brutos.")
    }
}

@Preview(name = "Search Bars", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewSearchBars() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchBarCyberCut(placeholder = "Buscar en el sistema...")
        SearchBarStealthGlass(placeholder = "Filtrar datos...")
        SearchBarPixelScan(placeholder = "ESCANEAR...")
    }
}

@Preview(name = "Bento Cards", showBackground = true, backgroundColor = 0xFF0D0221)
@Composable
fun PreviewBentoCards() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BentoCardPremium(title = "Bento Premium", headerEmoji = "💎") {
            Text("Contenido de la tarjeta Bento Premium", color = Color.White)
        }
        BentoCardPremium2(title = "Bento Expandible", headerEmoji = "⚙️") {
            Text("Este contenido se puede colapsar.", color = Color.White)
        }
    }
}

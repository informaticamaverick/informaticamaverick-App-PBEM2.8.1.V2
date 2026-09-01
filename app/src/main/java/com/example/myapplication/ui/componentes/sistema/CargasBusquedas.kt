package com.example.myapplication.ui.componentes.sistema

import androidx.compose.animation.core.FastOutSlowInEasing
import com.example.myapplication.uishared.estilos.SharedPalette
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI

/**
 * Colores definidos para el sistema de cuadrantes (El Reloj)
 */
val ElectricPurple = Color(0xFFB026FF) // 12:00
val ElectricGreen = Color(0xFF39FF14)  // 03:00
val NeonCyan = Color(0xFF00FFFF)       // 06:00
val CyberRed = Color(0xFFFF003C)       // 09:00


// ==========================================================================================
// --- 1. CARGA CIRCULAR ESTILO GEMINI ---
// ==========================================================================================

@Composable
fun GeminiCircleLoading(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_circle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ), label = "rotation"
    )

    val brush = geminiGradientBrush(isAnimated = true)

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = brush,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        // Brillo interno
        Box(
            modifier = Modifier
                .size(size * 0.6f)
                .blur(8.dp)
                .background(brush, CircleShape)
                .alpha(0.3f)
        )
    }
}

// ==========================================================================================
// --- 2. BARRA DE CARGA CONSOLA PROGRESIVA (CARGANDO MÓDULOS) ---
// ==========================================================================================

@Composable
fun ConsoleProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = "CARGANDO MÓDULOS..."
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "> $label",
                color = SharedPalette.AcidGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "[${(animatedProgress * 100).toInt()}%]",
                color = SharedPalette.AcidGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                .border(1.dp, SharedPalette.AcidGreen.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                .padding(2.dp)
        ) {
            // Barra de progreso segmentada
            Canvas(modifier = Modifier.fillMaxSize()) {
                val segments = 25
                val gap = 2.dp.toPx()
                val availableWidth = size.width - (segments - 1) * gap
                val segWidth = availableWidth / segments
                
                for (i in 0 until segments) {
                    val progressThreshold = i.toFloat() / segments
                    if (animatedProgress > progressThreshold) {
                        drawRect(
                            color = SharedPalette.AcidGreen,
                            topLeft = Offset(i * (segWidth + gap), 0f),
                            size = Size(segWidth, size.height)
                        )
                    }
                }
            }
            
            // Efecto de brillo de escaneo
            val infiniteTransition = rememberInfiniteTransition(label = "scan")
            val scanOffset by infiniteTransition.animateFloat(
                initialValue = -0.2f, targetValue = 1.2f,
                animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "scan"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.2f)
                    .graphicsLayer { translationX = scanOffset * size.width }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, SharedPalette.AcidGreen.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
            )
        }
    }
}


// ==========================================================================================
// --- 5. TARJETA DE CARGA "SINCRONIZANDO" ---
// ==========================================================================================

@Composable
fun SyncCard(
    modifier: Modifier = Modifier,
    mainText: String = "SINCRONIZANDO...",
    subText: String = "Conectando con la red PBEM"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = modifier
            .background(Color(0xFF05070A).copy(alpha = 0.9f), RoundedCornerShape(28.dp))
            .border(1.dp, SharedPalette.NeonCyan.copy(alpha = glowAlpha), RoundedCornerShape(28.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GeminiCircleLoading(size = 48.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = mainText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subText,
                color = SharedPalette.NeonCyan.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StandardLoading(
    modifier: Modifier = Modifier,
    color: Color = SharedPalette.NeonCyan
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = color.copy(alpha = 0.1f),
        strokeWidth = 4.dp,
        strokeCap = StrokeCap.Round
    )
}
/**
// ==========================================================================================
// --- 6. COMETA CYBERPUNK (ANIMACIÓN PREMIUM DE ALTO RENDIMIENTO) ---
// ==========================================================================================

/**
 * COMETA CYBERPUNK: Una joya de animación que combina geometría sagrada con estética glitch.
 * - Núcleo metamórfico (Círculo grande <-> Cuadrado pequeño).
 * - Órbita cromática basada en posición (Violeta 12h, Cian 6h).
 * - Cometa con estela de luz (Haz neón).
 */
@Composable
fun CometaCyberPunk(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = "LINK"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cometa_master")

    // 1. Animación de Rotación (El Cometa)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ), label = "orbital_rot"
    )

    // 2. Animación de Metamorfosis (Núcleo)
    // Cuando el valor es 1f -> Círculo Grande. Cuando es 0f -> Cuadrado Pequeño.
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "morph"
    )

    // Colores de referencia
    val colorTop = SharedPalette.ElectricPurple // 12:00
    val colorBottom = SharedPalette.NeonCyan   // 06:00

    // Interpolación de color basada en la rotación actual para el cometa
    // Usamos el seno para que oscile suavemente entre los dos polos cromáticos
    // 0 grados es las 3h, restamos 90 para que sea las 12h
    val angleRad = Math.toRadians(rotation.toDouble() - 90.0)
    val colorFraction = (Math.sin(angleRad).toFloat() + 1f) / 2f
    val currentColor = lerp(colorTop, colorBottom, colorFraction)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            // Capa de Brillo ambiental (Glow)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .blur(30.dp)
                    .background(currentColor.copy(alpha = 0.2f), CircleShape)
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val orbitalRadius = size.width * 0.4f

                // --- A. DIBUJO DEL NÚCLEO METAMÓRFICO ---
                // El tamaño oscila y la forma cambia de cuadrado a círculo
                val coreSize = 14.dp.toPx() + (10.dp.toPx() * morphProgress)
                val radiusPx = (coreSize / 2) * morphProgress // Esquina redondeada = Círculo

                withTransform({
                    rotate(rotation * -0.5f, Offset(cx, cy)) // Rotación inversa lenta
                }) {
                    // Sombra/Brillo del núcleo
                    drawRoundRect(
                        color = currentColor.copy(alpha = 0.3f),
                        topLeft = Offset(cx - coreSize / 2, cy - coreSize / 2),
                        size = Size(coreSize, coreSize),
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )
                    // Borde del núcleo
                    drawRoundRect(
                        color = currentColor,
                        topLeft = Offset(cx - coreSize / 2, cy - coreSize / 2),
                        size = Size(coreSize, coreSize),
                        cornerRadius = CornerRadius(radiusPx, radiusPx),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // --- B. DIBUJO DE LA ESTELA (HAZ DE LUZ) ---
                // Un arco que cambia de color dinámicamente
                drawArc(
                    brush = Brush.sweepGradient(
                        0.8f to Color.Transparent,
                        0.95f to currentColor.copy(alpha = 0.6f),
                        1f to currentColor,
                        center = Offset(cx, cy)
                    ),
                    startAngle = rotation - 100f, // Estela más larga
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(cx - orbitalRadius, cy - orbitalRadius),
                    size = Size(orbitalRadius * 2, orbitalRadius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // --- C. DIBUJO DE LA CABEZA DEL COMETA ---
                val cometaRad = 6.dp.toPx()
                val cometaX = cx + orbitalRadius * Math.cos(angleRad).toFloat()
                val cometaY = cy + orbitalRadius * Math.sin(angleRad).toFloat()

                // Glow de impacto del cometa
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(currentColor, Color.Transparent)
                    ),
                    radius = cometaRad * 2.5f,
                    center = Offset(cometaX, cometaY)
                )
                
                // Cabeza de núcleo blanco (estilo estrella)
                drawCircle(
                    color = Color.White,
                    radius = cometaRad * 0.5f,
                    center = Offset(cometaX, cometaY)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Texto Estilo Cyberpunk
        Text(
            text = label,
            style = AppStyles.SectionHeader.copy(
                color = currentColor,
                fontSize = 14.sp,
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Black
            )
        )
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = CyberTypography.MonospaceData.copy(
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        )
    }
}
**/
// ==========================================================================================
// --- 7. BARRA DE CARGA PREMIUM CYBERPUNK (STREAK & GLITCH) ---
// ==========================================================================================

/**
 * Componente de carga Premium con estética Cyberpunk/ROG.
 * Incluye barra de progreso segmentada, texto dinámico "///..." y efectos de brillo.
 */
@Composable
fun CyberLoadingBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String = "SYSTEM",
    accentColor: Color = SharedPalette.NeonCyan,
    showBeIcon: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "cyber_loading")
    
    // Animación de pulso para el brillo
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // Animación de los slashes "///"
    val slashCount = 15
    val activeSlashes = (animatedProgress * slashCount).toInt()
    val slashText = "/".repeat(activeSlashes) + ".".repeat(slashCount - activeSlashes)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBeIcon) {
                    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                        // Mini versión de Be Body simplificada
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(accentColor, size.minDimension / 2.5f, style = Stroke(2.dp.toPx()))
                            drawCircle(accentColor.copy(alpha = pulseAlpha), size.minDimension / 5f)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Text(
                    text = "LOADING $label $slashText",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = accentColor.copy(alpha = pulseAlpha),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Contenedor de la barra
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            // Relleno de la barra con gradiente y efecto de segmentación
            Canvas(modifier = Modifier.fillMaxSize().padding(1.dp)) {
                val barWidth = size.width * animatedProgress
                
                // Fondo neón sutil
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(accentColor.copy(alpha = 0.1f), accentColor.copy(alpha = 0.4f))
                    ),
                    size = Size(barWidth, size.height)
                )

                // Segmentos brillantes
                val segments = 20
                val gap = 3.dp.toPx()
                val segWidth = (size.width - (segments - 1) * gap) / segments
                
                for (i in 0 until segments) {
                    val startX = i * (segWidth + gap)
                    if (startX + segWidth <= barWidth) {
                        drawRect(
                            color = accentColor,
                            topLeft = Offset(startX, 0f),
                            size = Size(segWidth, size.height)
                        )
                        
                        // Brillo superior de cada segmento
                        drawRect(
                            color = Color.White.copy(alpha = 0.5f),
                            topLeft = Offset(startX, 0f),
                            size = Size(segWidth, 2.dp.toPx())
                        )
                    }
                }
            }

            // Efecto de escaneo (Glare) que recorre la barra
            val glareOffset by infiniteTransition.animateFloat(
                initialValue = -0.5f, targetValue = 1.5f,
                animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
                label = "glare"
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.15f)
                    .graphicsLayer { translationX = glareOffset * size.width }
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )
        }
    }
}

@Composable
fun CometaCyberPunk(
    progress: Float, // Valor de 0.0f a 1.0f
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_pulse")
    val textMeasurer = rememberTextMeasurer()

    // 1. Animación de Rotación Orbital
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ), label = "orbit"
    )

    // 2. Animación de Metamorfosis del Núcleo
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "morph"
    )

    // Determinar el color actual basado en el ángulo
    val currentColor = getOrbitColor(rotation)

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val orbitalRadius = size.width * 0.35f

            // --- A. DIBUJO DEL NÚCLEO METAMÓRFICO ---
            val coreMinSize = 45.dp.toPx()
            val coreMaxSize = 75.dp.toPx()
            val currentCoreSize = coreMinSize + ((coreMaxSize - coreMinSize) * morphProgress)
            val cornerRadiusPx = (currentCoreSize / 2f) * morphProgress

            withTransform({
                rotate(-rotation * 0.2f, Offset(cx, cy))
            }) {
                // Fondo con Glow
                drawRoundRect(
                    color = currentColor.copy(alpha = 0.15f),
                    topLeft = Offset(cx - currentCoreSize / 2, cy - currentCoreSize / 2),
                    size = Size(currentCoreSize, currentCoreSize),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
                // Borde Neón
                drawRoundRect(
                    color = currentColor,
                    topLeft = Offset(cx - currentCoreSize / 2, cy - currentCoreSize / 2),
                    size = Size(currentCoreSize, currentCoreSize),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // --- B. DIBUJO DEL PORCENTAJE (CENTRO) ---
            val percentageText = "${(progress * 100).toInt()}%"
            val textLayoutResult = textMeasurer.measure(
                text = percentageText,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    cx - textLayoutResult.size.width / 2,
                    cy - textLayoutResult.size.height / 2
                )
            )

            // --- C. DIBUJO DE LA ESTELA (HAZ DE LUZ) ---
            withTransform({
                rotate(rotation, Offset(cx, cy))
            }) {
                // Mejora: La estela del cometa usando ConicGradient (SweepGradient en Compose)
                val tailLengthDegrees = 180f
                
                // En Compose, SweepGradient acepta una lista de colores y paradas (offsets).
                // Simulamos el comportamiento del HTML5 Canvas.
                // 1.0f (la cabeza) es el currentColor.
                // Luego bajamos la intensidad.
                val tailBrush = Brush.sweepGradient(
                    0.0f to Color.Transparent,
                    (1f - (tailLengthDegrees / 360f)) to Color.Transparent,
                    (1f - (tailLengthDegrees / 360f) * 0.15f) to currentColor.copy(alpha = 0.7f),
                    1.0f to currentColor,
                    center = Offset(cx, cy)
                )

                // El arco de la estela con Shadow (usando Layer para glow)
                drawArc(
                    brush = tailBrush,
                    startAngle = 0f,
                    sweepAngle = 360f, // Dibujamos el circulo completo para que el gradiente se vea bien
                    useCenter = false,
                    topLeft = Offset(cx - orbitalRadius, cy - orbitalRadius),
                    size = Size(orbitalRadius * 2, orbitalRadius * 2),
                    style = Stroke(
                        width = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // --- D. CABEZA DEL COMETA ---
                val cometX = orbitalRadius
                val cometY = 0f
                val cometPos = Offset(cx + cometX, cy + cometY)

                // Brillo de impacto (Glow Radial)
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to currentColor,
                        0.5f to currentColor.copy(alpha = 0.5f),
                        1f to Color.Transparent,
                        center = cometPos,
                        radius = 18.dp.toPx()
                    ),
                    center = cometPos,
                    radius = 18.dp.toPx()
                )

                // Núcleo blanco caliente
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = cometPos
                )
            }
        }
    }
}

/**
 * Lógica de interpolación de colores estilo "Reloj Cyberpunk"
 */
fun getOrbitColor(angle: Float): Color {
    val deg = ((angle % 360f) + 360f) % 360f
    return when {
        deg < 90f -> lerp(ElectricGreen, NeonCyan, deg / 90f)
        deg < 180f -> lerp(NeonCyan, CyberRed, (deg - 90f) / 90f)
        deg < 270f -> lerp(CyberRed, ElectricPurple, (deg - 180f) / 90f)
        else -> lerp(ElectricPurple, ElectricGreen, (deg - 270f) / 90f)
    }
}

/**
 * Función auxiliar para mezclar colores en Compose
 */
fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================

@Preview(name = "Cometa Cyberpunk Premium", showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewCometaCyberPunk() {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            progress = (progress + 0.005f) % 1f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070A)),
        contentAlignment = Alignment.Center
    ) {
        CometaCyberPunk(
            progress = progress,
            modifier = Modifier.size(250.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PreviewNuevasCargas() {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while(progress < 1f) {
            delay(100)
            progress += 0.01f
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PREMIUM LOADING COMPONENTS", color = SharedPalette.NeonCyan, fontWeight = FontWeight.Black)

        CometaCyberPunk(progress = progress)

        SyncCard()
        
        CyberLoadingBar(progress = progress, label = "SUBSYSTEMS")
        
        CyberLoadingBar(
            progress = progress, 
            label = "ROG_PROTOCOL", 
            accentColor = SharedPalette.NeonMagenta
        )

        ConsoleProgressBar(progress = progress)
                
    }
}

































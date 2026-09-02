package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.InformacionClima
import com.example.myapplication.core.dominio.modelos.PronosticoDia
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

internal enum class EstadoClima { DESPEJADO, PARCIAL, NIEBLA, LLOVIZNA, LLUVIA, NIEVE, TORMENTA, OTRO }

internal fun estadoClimaDesdeEmoji(emoji: String): EstadoClima = when (emoji) {
    "☀️" -> EstadoClima.DESPEJADO
    "🌤️" -> EstadoClima.PARCIAL
    "🌫️" -> EstadoClima.NIEBLA
    "🌦️" -> EstadoClima.LLOVIZNA
    "🌧️" -> EstadoClima.LLUVIA
    "❄️" -> EstadoClima.NIEVE
    "⛈️" -> EstadoClima.TORMENTA
    else -> EstadoClima.OTRO
}

private data class ConfigClimatica(
    val colorInicio: Color,
    val colorFin: Color,
    val sol: Boolean,
    val nubes: Int,
    val lluvia: Int,
    val nieve: Int,
    val niebla: Boolean,
    val tormenta: Boolean
)

private fun configClimatica(estado: EstadoClima): ConfigClimatica = when (estado) {
    EstadoClima.DESPEJADO -> ConfigClimatica(Color(0xFFFF7043), Color(0xFFFFA26B), sol = true, nubes = 0, lluvia = 0, nieve = 0, niebla = false, tormenta = false)
    EstadoClima.PARCIAL -> ConfigClimatica(Color(0xFFFF9466), Color(0xFFFFC299), sol = true, nubes = 3, lluvia = 0, nieve = 0, niebla = false, tormenta = false)
    EstadoClima.NIEBLA -> ConfigClimatica(Color(0xFF8B93A0), Color(0xFFB9C0CA), sol = false, nubes = 0, lluvia = 0, nieve = 0, niebla = true, tormenta = false)
    EstadoClima.LLOVIZNA -> ConfigClimatica(Color(0xFF6E90B8), Color(0xFF96AFCB), sol = false, nubes = 2, lluvia = 14, nieve = 0, niebla = false, tormenta = false)
    EstadoClima.LLUVIA -> ConfigClimatica(Color(0xFF3F5C86), Color(0xFF6786AD), sol = false, nubes = 2, lluvia = 28, nieve = 0, niebla = false, tormenta = false)
    EstadoClima.NIEVE -> ConfigClimatica(Color(0xFF6A9BC3), Color(0xFF9BC2DE), sol = false, nubes = 2, lluvia = 0, nieve = 22, niebla = false, tormenta = false)
    EstadoClima.TORMENTA -> ConfigClimatica(Color(0xFF262C46), Color(0xFF454E72), sol = false, nubes = 3, lluvia = 30, nieve = 0, niebla = false, tormenta = true)
    EstadoClima.OTRO -> ConfigClimatica(Color(0xFFF97316), Color(0xFFFDBA74), sol = true, nubes = 0, lluvia = 0, nieve = 0, niebla = false, tormenta = false)
}

private data class GotaLluvia(val leftFraction: Float, val delayMs: Int, val durationMs: Int)
private data class Copo(val leftFraction: Float, val delayMs: Int, val durationMs: Int, val tamano: Dp)
private data class Nube(val topFraction: Float, val leftFraction: Float, val durationMs: Int)

private fun generarGotas(cantidad: Int, tormenta: Boolean): List<GotaLluvia> = List(cantidad) { i ->
    GotaLluvia(
        leftFraction = (i * 37 % 100) / 100f,
        delayMs = (i * 13 % 20) * 100,
        durationMs = if (tormenta) 500 + (i % 4) * 80 else 800 + (i % 5) * 120
    )
}

private fun generarCopos(cantidad: Int): List<Copo> = List(cantidad) { i ->
    Copo(
        leftFraction = (i * 29 % 100) / 100f,
        delayMs = (i * 17 % 30) * 100,
        durationMs = 3000 + (i % 5) * 600,
        tamano = (4 + (i % 3) * 2).dp
    )
}

private fun generarNubes(cantidad: Int): List<Nube> = List(cantidad) { i ->
    Nube(
        topFraction = (8 + i * 22 % 35) / 100f,
        leftFraction = (4 + i * 31 % 55) / 100f,
        durationMs = 6000 + (i % 3) * 2000
    )
}

@Composable
internal fun EscenaClimatica(estado: EstadoClima, modifier: Modifier = Modifier) {
    val config = remember(estado) { configClimatica(estado) }
    val gotas = remember(estado) { generarGotas(config.lluvia, config.tormenta) }
    val copos = remember(estado) { generarCopos(config.nieve) }
    val nubes = remember(estado) { generarNubes(config.nubes) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(config.colorInicio, config.colorFin)))
    ) {
        val anchoTotal = maxWidth
        val altoTotal = maxHeight

        nubes.forEach { nube -> NubeFlotante(nube, anchoTotal, altoTotal) }
        if (config.niebla) BandasNiebla(anchoTotal, altoTotal)
        gotas.forEach { gota -> GotaAnimada(gota, anchoTotal, altoTotal) }
        copos.forEach { copo -> CopoAnimado(copo, anchoTotal, altoTotal) }
        if (config.tormenta) DestelloRelampago()
        if (config.sol) RayosDeSol(altoTotal)
    }
}

@Composable
private fun NubeFlotante(nube: Nube, ancho: Dp, alto: Dp) {
    val infinite = rememberInfiniteTransition(label = "nube")
    val desplazamiento by infinite.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(nube.durationMs, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "nubeDesplazamiento"
    )
    Box(
        modifier = Modifier
            .offset(x = ancho * nube.leftFraction + 16.dp * desplazamiento, y = alto * nube.topFraction)
            .size(width = 64.dp, height = 34.dp)
    ) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.55f))
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f))
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .offset(x = 32.dp, y = 5.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f))
        )
    }
}

@Composable
private fun BandasNiebla(ancho: Dp, alto: Dp) {
    val bandas = remember { listOf(Triple(0.20f, 7000, 0.40f), Triple(0.48f, 9000, 0.30f), Triple(0.75f, 5500, 0.35f)) }
    bandas.forEachIndexed { index, (topFrac, duracion, opacidad) ->
        val infinite = rememberInfiniteTransition(label = "niebla_$index")
        val desplazamiento by infinite.animateFloat(
            initialValue = -0.25f, targetValue = 0.25f,
            animationSpec = infiniteRepeatable(tween(duracion, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "nieblaDesplazamiento_$index"
        )
        Box(
            modifier = Modifier
                .offset(x = ancho * desplazamiento, y = alto * topFrac)
                .width(ancho * 1.5f)
                .height(16.dp)
                .background(Color.White.copy(alpha = opacidad), RoundedCornerShape(20.dp))
        )
    }
}

@Composable
private fun GotaAnimada(gota: GotaLluvia, ancho: Dp, alto: Dp) {
    val infinite = rememberInfiniteTransition(label = "gota")
    val progreso by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(gota.durationMs, easing = LinearEasing),
            initialStartOffset = StartOffset(gota.delayMs)
        ), label = "gotaProgreso"
    )
    val recorrido = alto + 40.dp
    val offsetY = (-20).dp + recorrido * progreso
    val alfa = if (progreso < 0.15f) progreso / 0.15f else (1f - progreso).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .offset(x = ancho * gota.leftFraction, y = offsetY)
            .size(width = 2.dp, height = 16.dp)
            .rotate(12f)
            .alpha(alfa)
            .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(2.dp))
    )
}

@Composable
private fun CopoAnimado(copo: Copo, ancho: Dp, alto: Dp) {
    val infinite = rememberInfiniteTransition(label = "copo")
    val progreso by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(copo.durationMs, easing = LinearEasing),
            initialStartOffset = StartOffset(copo.delayMs)
        ), label = "copoProgreso"
    )
    val recorrido = alto + 40.dp
    val offsetY = (-20).dp + recorrido * progreso
    val vaiven = (sin(progreso * Math.PI.toFloat() * 2) * 10).dp
    val alfa = if (progreso < 0.15f) progreso / 0.15f else (1f - progreso).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .offset(x = ancho * copo.leftFraction + vaiven, y = offsetY)
            .size(copo.tamano)
            .alpha(alfa)
            .background(Color.White.copy(alpha = 0.85f), CircleShape)
    )
}

@Composable
private fun DestelloRelampago() {
    val infinite = rememberInfiniteTransition(label = "relampago")
    val alfa by infinite.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 3600
                0.9f at 3680
                0.1f at 3720
                0.75f at 3780
                0f at 3840
                0f at 4000
            }
        ), label = "relampagoAlfa"
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = alfa))
    )
}

@Composable
private fun RayosDeSol(alto: Dp) {
    val infinite = rememberInfiniteTransition(label = "rayos")
    val rotacion by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "rayosRotacion"
    )
    Box(
        modifier = Modifier
            .offset(x = (-16).dp, y = alto / 2 - 45.dp)
            .size(90.dp)
            .alpha(0.22f)
            .rotate(rotacion)
    ) {
        repeat(4) { i ->
            Box(
                Modifier
                    .align(Alignment.Center)
                    .rotate(i * 45f)
                    .width(3.dp)
                    .height(90.dp)
                    .background(Color.White)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastSheet(
    weather: InformacionClima,
    onDismiss: () -> Unit,
) {
    val colors = GestionTurnosTheme
    val estadoActual = remember(weather.emojiClima) { estadoClimaDesdeEmoji(weather.emojiClima) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.CardBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── HEADER ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pronóstico del tiempo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.TextPrimary
                    )
                    Text(
                        text = weather.nombreCiudad,
                        fontSize = 13.sp,
                        color = colors.TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = colors.TextMuted)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── CLIMA ACTUAL (hero animado según estado) ─────────────────
            Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                EscenaClimatica(estado = estadoActual, modifier = Modifier.matchParentSize())

                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    val infiniteEmoji = rememberInfiniteTransition(label = "emoji")
                    val escala by infiniteEmoji.animateFloat(
                        initialValue = 1f, targetValue = 1.14f,
                        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = "emojiEscala"
                    )
                    val balanceo by infiniteEmoji.animateFloat(
                        initialValue = -4f, targetValue = 4f,
                        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
                        label = "emojiBalanceo"
                    )

                    Text(
                        text = weather.emojiClima,
                        fontSize = 58.sp,
                        modifier = Modifier
                            .scale(escala)
                            .rotate(balanceo)
                    )
                    Column {
                        Text(
                            text = weather.temperatura,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = weather.descripcionClima,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("💧 ${weather.humedad}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                            Text("💨 ${weather.velocidadViento}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Próximos 7 días",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            // ── PRONÓSTICO 7 DÍAS (entrada escalonada) ───────────────────
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(weather.pronostico) { index, day ->
                    DayForecastCard(day = day, index = index)
                }
            }
        }
    }
}

@Composable
private fun DayForecastCard(day: PronosticoDia, index: Int) {
    val colors = GestionTurnosTheme
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * 80).milliseconds)
        visible = true
    }
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 30.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "offsetY_$index"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha_$index"
    )
    val infiniteAnim = rememberInfiniteTransition(label = "day_$index")
    val emojiScale by infiniteAnim.animateFloat(
        initialValue = 1f, targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            tween(1000 + (index * 150), easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "emojiScale_$index"
    )

    Surface(
        modifier = Modifier
            .width(80.dp)
            .offset { androidx.compose.ui.unit.IntOffset(0, offsetY.toPx().toInt()) }
            .graphicsLayer { this.alpha = alpha },
        shape = RoundedCornerShape(18.dp),
        color = colors.SurfaceInput,
        border = BorderStroke(1.dp, colors.BorderGlass)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(day.nombreDia, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.TextPrimary)
            Text(day.emoji, fontSize = 28.sp, modifier = Modifier.scale(emojiScale))
            Text(day.tempMax, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = colors.BrandOrange)
            Text(day.tempMin, fontSize = 11.sp, color = colors.TextMuted)
        }
    }
}

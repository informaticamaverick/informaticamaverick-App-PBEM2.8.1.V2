package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// --- MODELOS DE DATOS PARA LA ANIMACIÓN ---
// ======================================================================================
// 1. ANIMACIÓN DE BE DURMIENDO ZZZ
// ======================================================================================
data class SheepState(
    val id: Long = System.nanoTime(),
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val scale: Float = 0.8f + (Math.random().toFloat() * 0.4f)
)

data class ZzzState(
    val id: Long = System.nanoTime(),
    val x: Float,
    val y: Float,
    val size: Float = 12f + (Math.random().toFloat() * 8f),
    val phase: Float = Math.random().toFloat() * 2f * PI.toFloat()
)

@Composable
fun BeSleepingScreen() {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "BeBreath")

    // Animación de respiración de Be
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )

    // Animación de flotación
    val floatY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )

    // Estados para las listas de objetos animados con tipos explícitos
    var sheeps by remember { mutableStateOf(listOf<SheepState>()) }
    var zzzList by remember { mutableStateOf(listOf<ZzzState>()) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(
                0.0f to Color(0xFF0F172A),
                1.0f to Color(0xFF020408)
            )),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val beXPx = widthPx * 0.8f
        val beYPx = heightPx * 0.5f

        // Corrutina para generar ovejas y Zzz
        LaunchedEffect(widthPx, heightPx) {
            launch {
                while (true) {
                    delay(2500)
                    val newSheep = SheepState(
                        startX = widthPx * 0.1f,
                        startY = beYPx + 100f,
                        targetX = widthPx * 0.9f
                    )
                    sheeps = sheeps + newSheep
                }
            }
            launch {
                while (true) {
                    delay(1200)
                    val newZzz = ZzzState(x = beXPx - 100f, y = beYPx - 50f)
                    zzzList = zzzList + newZzz
                }
            }
        }

        // 1. DIBUJAR A BE EN EL CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBeNative(
                drawScope = this,
                x = beXPx,
                y = beYPx + floatY.dp.toPx(),
                scale = 3f * breathScale
            )
        }

        // 2. ELEMENTOS DE OVERLAY (Zzz y Ovejas)
        zzzList.forEach { zzz ->
            ZzzComponent(zzz) { zzzList = zzzList.filter { it.id != zzz.id } }
        }

        sheeps.forEach { sheep ->
            SheepComponent(sheep, centerY = beYPx) { sheeps = sheeps.filter { it.id != sheep.id } }
        }

        Text(
            text = "BE IS SLEEPING...",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun ZzzComponent(state: ZzzState, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val anim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        anim.animateTo(1f, animationSpec = tween(3000, easing = LinearEasing))
        onDismiss()
    }

    val xOffset = with(density) { (state.x - (anim.value * 150f)).toDp() }
    val yOffset = with(density) { (state.y - (anim.value * 200f)).toDp() }
    val alpha = 1f - anim.value

    Text(
        text = "Z",
        color = Color(0xFF22D3EE).copy(alpha = alpha),
        fontSize = (state.size + (anim.value * 10)).sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .alpha(alpha)
    )
}

@Composable
fun SheepComponent(state: SheepState, centerY: Float, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(4000, easing = LinearEasing))
        onDismiss()
    }

    val t = progress.value
    val xPx = state.startX + (state.targetX - state.startX) * t
    val maxHeight = 300f
    val yPx = centerY + 100f - (4 * maxHeight * t * (1 - t))

    val alpha = if (t < 0.2f) t / 0.2f else if (t > 0.8f) 1f - (t - 0.8f) / 0.2f else 1f

    Box(
        modifier = Modifier
            .offset(with(density) { xPx.toDp() }, with(density) { yPx.toDp() })
            .scale(state.scale)
            .alpha(alpha)
    ) {
        Text("☁️", fontSize = 30.sp)
        Text("🐑", fontSize = 20.sp, modifier = Modifier.offset(x = 8.dp, y = 8.dp))
    }
}

fun drawBeNative(drawScope: androidx.compose.ui.graphics.drawscope.DrawScope, x: Float, y: Float, scale: Float) {
    with(drawScope) {
        translate(left = x, top = y) {
            drawScope.scale(scale, pivot = Offset.Zero) {
                translate(-50f, -50f) {
                    drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                    drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF22D3EE), Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)

                    drawCircle(Color(0xFF0A0E14), 36f, Offset(50f, 50f))
                    drawCircle(Color(0xFF2197F5).copy(alpha = 0.2f), 32f, Offset(50f, 50f))
                    drawCircle(Color(0xFF2197F5), 32f, Offset(50f, 50f), style = Stroke(6f))

                    drawArc(
                        color = Color.White.copy(alpha = 0.5f),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(4f, cap = StrokeCap.Round),
                        topLeft = Offset(24f, 24f),
                        size = Size(52f, 52f)
                    )

                    val eyePathLeft = Path().apply {
                        moveTo(32f, 52f)
                        quadraticTo(40f, 58f, 48f, 52f)
                    }
                    val eyePathRight = Path().apply {
                        moveTo(52f, 52f)
                        quadraticTo(60f, 58f, 68f, 52f)
                    }

                    drawPath(eyePathLeft, Color.White, style = Stroke(4.5f, cap = StrokeCap.Round))
                    drawPath(eyePathRight, Color.White, style = Stroke(4.5f, cap = StrokeCap.Round))
                }
            }
        }
    }
}

// ======================================================================================
// 1. ANIMACIÓN DE BE BUSCANDO
// ======================================================================================
@Composable
fun BeSearchingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "BeSearch")

    // Animación de balanceo lateral (investigación)
    val swayX by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "sway"
    )

    // Cronómetro para la lógica de lectura (ojos) y paso de páginas
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "timer"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(
                0.0f to Color(0xFF1E293B),
                1.0f to Color(0xFF020408)
            )),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2 - 50.dp.toPx()

            // 1. Dibujar a Be al fondo
            drawBeSearching(this, centerX + swayX.dp.toPx(), centerY, 3f, time)

            // 2. Dibujar el Libro abierto al frente
            drawOpenBook(this, centerX, centerY + 80.dp.toPx(), 1.2f, time)

            // 3. Dibujar las Manos sujetando el libro
            drawHandsSearching(this, centerX, centerY + 95.dp.toPx(), 1.2f)
        }
    }
}

fun drawBeSearching(drawScope: DrawScope, x: Float, y: Float, scale: Float, time: Float) {
    with(drawScope) {
        translate(left = x, top = y) {
            scale(scale, pivot = Offset.Zero) {
                translate(-50f, -50f) {
                    // Brazo robótico original
                    drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                    drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF22D3EE), Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)

                    // Lente
                    drawCircle(Color(0xFF0A0E14), 36f, Offset(50f, 50f))
                    drawCircle(Color(0xFF2197F5).copy(alpha = 0.2f), 32f, Offset(50f, 50f))
                    drawCircle(Color(0xFF2197F5), 32f, Offset(50f, 50f), style = Stroke(6f))

                    // Lógica de Ojos Leyendo
                    val readDuration = 700f
                    val readProgress = (time % readDuration) / readDuration
                    val lineNum = (time % (readDuration * 5) / readDuration).toInt()

                    val eyeX = -10f + (readProgress * 20f)
                    val eyeY = -4f + (lineNum * 2.5f)

                    // Ojos blancos
                    val eyePaint = Paint().apply {
                        color = Color.White
                        isAntiAlias = true
                    }

                    // Dibujar ojos (elipses)
                    drawOval(Color.White, Offset(40f + eyeX - 7f, 50f + eyeY - 9f), Size(14f, 18f))
                    drawOval(Color.White, Offset(60f + eyeX - 7f, 50f + eyeY - 9f), Size(14f, 18f))

                    // Pupilas negras enfocadas
                    drawCircle(Color.Black, 3f, Offset(40f + eyeX, 51f + eyeY))
                    drawCircle(Color.Black, 3f, Offset(60f + eyeX, 51f + eyeY))
                }
            }
        }
    }
}

fun drawOpenBook(drawScope: DrawScope, x: Float, y: Float, scale: Float, time: Float) {
    with(drawScope) {
        translate(left = x, top = y) {
            scale(scale, pivot = Offset.Zero) {
                val bookW = 140f
                val bookH = 100f

                // Cubierta
                val coverPath = Path().apply {
                    addRoundRect(RoundRect(Rect(-bookW - 5f, -bookH/2 - 5f, bookW + 5f, bookH/2 + 10f), cornerRadius = CornerRadius(6f)))
                }
                drawPath(coverPath, Color(0xFF451A03))

                // Páginas (Lado izquierdo)
                val leftPage = Path().apply {
                    moveTo(-bookW, -bookH/2)
                    quadraticTo(-bookW/2, -bookH/2 - 10f, 0f, -bookH/2)
                    lineTo(0f, bookH/2)
                    quadraticTo(-bookW/2, bookH/2 + 10f, -bookW, bookH/2)
                    close()
                }
                drawPath(leftPage, Color(0xFFFEF3C7))

                // Páginas (Lado derecho)
                val rightPage = Path().apply {
                    moveTo(0f, -bookH/2)
                    quadraticTo(bookW/2, -bookH/2 - 10f, bookW, -bookH/2)
                    lineTo(bookW, bookH/2)
                    quadraticTo(bookW/2, bookH/2 + 10f, 0f, bookH/2)
                    close()
                }
                drawPath(rightPage, Color(0xFFFFFBEB))

                // Texto simulado
                for (i in 0..10) {
                    val yPos = -bookH/2 + 15f + (i * 8f)
                    drawLine(Color(0xFF78350F).copy(alpha = 0.3f), Offset(-bookW + 20f, yPos), Offset(-20f, yPos), 2f)
                    drawLine(Color(0xFF78350F).copy(alpha = 0.3f), Offset(20f, yPos), Offset(bookW - 20f, yPos), 2f)
                }

                // Animación de pasar página
                val flipCycle = (time * 2.5f) % (PI.toFloat() * 2000f) / 1000f
                if (flipCycle < PI) {
                    val angle = flipCycle
                    val edgeX = cos(angle) * bookW
                    val edgeYHeight = -sin(angle) * 40f

                    val flipPath = Path().apply {
                        moveTo(0f, -bookH/2)
                        lineTo(edgeX, -bookH/2 + edgeYHeight)
                        lineTo(edgeX, bookH/2 + edgeYHeight)
                        lineTo(0f, bookH/2)
                        close()
                    }
                    drawPath(flipPath, Color(0xFFFEFCE8))
                }
            }
        }
    }
}

fun drawHandsSearching(drawScope: DrawScope, x: Float, y: Float, scale: Float) {
    with(drawScope) {
        translate(left = x, top = y) {
            scale(scale, pivot = Offset.Zero) {
                // Dibujar manos como elipses blancas
                drawOval(Color.White, Offset(-145f, -10f), Size(50f, 35f))
                drawOval(Color.White, Offset(95f, -10f), Size(50f, 35f))
            }
        }
    }
}
// ======================================================================================
// 1. PREVIEWS DE LA ANIMACIÓN
// ======================================================================================
@Preview(showBackground = true)
@Composable
fun BeSleepingScreenPreview() {
    MyApplicationTheme {
        BeSleepingScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun BeSearchingScreenPreview() {
    MyApplicationTheme {
        BeSearchingScreen()
    }
}










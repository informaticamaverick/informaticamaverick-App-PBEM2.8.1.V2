package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ==========================================================================================
// --- 1. PANTALLA DE CARGA COMPLETA "BE INVESTIGATOR" ---
// Bloquea la pantalla entera (Ideal para transiciones largas o procesos pesados)
// ==========================================================================================
@Composable
fun LoadingBeAssistantScreen(
    mainText: String = "Buscando Prestadores...",
    subText: String = "Analizando base de datos local",
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    val infiniteTransition = rememberInfiniteTransition(label = "be_investigator")

    // --- ANIMACIÓN DE VUELO (RUTA DE BÚSQUEDA MISION) ---
    val beOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                -screenWidth * 0.3f at 700
                -screenWidth * 0.28f at 1400
                screenWidth * 0.3f at 2450
                screenWidth * 0.28f at 3150
                -screenWidth * 0.25f at 4200
                -screenWidth * 0.22f at 4900
                screenWidth * 0.25f at 5600
                screenWidth * 0.22f at 6300
                0f at 7000
            }
        ), label = "fly_x"
    )

    val beOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                screenHeight * 0.25f at 700
                screenHeight * 0.22f at 1400
                -screenHeight * 0.25f at 2450
                -screenHeight * 0.22f at 3150
                -screenHeight * 0.3f at 4200
                -screenHeight * 0.28f at 4900
                screenHeight * 0.3f at 5600
                screenHeight * 0.28f at 6300
                0f at 7000
            }
        ), label = "fly_y"
    )

    val beRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                0f at 0
                -20f at 700
                -10f at 1400
                25f at 2450
                15f at 3150
                -30f at 4200
                -15f at 4900
                35f at 5600
                15f at 6300
                0f at 7000
            }
        ), label = "fly_rot"
    )

    val beScale by infiniteTransition.animateFloat(
        initialValue = 1.2f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 7000
                1.2f at 0
                0.9f at 700
                0.9f at 1400
                1.1f at 2450
                1.1f at 3150
                0.8f at 4200
                0.8f at 4900
                1.3f at 5600
                1.3f at 6300
                1.2f at 7000
            }
        ), label = "fly_scale"
    )

    val beamRotation by infiniteTransition.animateFloat(
        initialValue = -35f, targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "beam_sweep"
    )

    val pupilX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0; -3f at 200; 3f at 400; 4f at 800; -2f at 1000; -4f at 1400; 2f at 1600; 0f at 2000
            }
        ), label = "pupil_x"
    )
    val pupilY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0; 3f at 200; 3f at 400; -2f at 800; -4f at 1000; 2f at 1400; 4f at 1600; 0f at 2000
            }
        ), label = "pupil_y"
    )

    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, animationSpec = tween(100), label = "blink")

    val textGradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "text_grad"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF020408))
            .pointerInput(Unit) {}, // Atrapa los clics para no interactuar con el fondo
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(colors = listOf(Color.Transparent, Color(0xFF020408).copy(alpha = 0.8f)), radius = 1500f))
        )

        // --- HUD / TEXTO INFORMATIVO ---
        Box(
            modifier = Modifier
                .background(Color(0xFF05070A).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFF2197F5).copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(horizontal = 40.dp, vertical = 20.dp)
                .zIndex(50f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val textBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFFE91E63), Color(0xFF2197F5)),
                    start = Offset(textGradientOffset - 500f, 0f),
                    end = Offset(textGradientOffset, 0f)
                )
                Text(mainText.uppercase(), style = TextStyle(brush = textBrush), fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(subText, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }

        // --- PERSONAJE BE INVESTIGADOR EN VUELO ---
        Box(modifier = Modifier.fillMaxSize().zIndex(100f), contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        translationX = beOffsetX.dp.toPx()
                        translationY = beOffsetY.dp.toPx()
                        scaleX = beScale
                        scaleY = beScale
                        rotationZ = beRotation
                        shadowElevation = 30f
                        ambientShadowColor = Color.Black
                        spotShadowColor = Color.Black
                    }
            ) {
                // Cálculo de píxeles (toPx) antes de withTransform para evitar el error de receiver
                val cx = 50.dp.toPx()
                val cy = 45.dp.toPx()
                val beamLeftX = (-150).dp.toPx()
                val beamRightX = 250.dp.toPx()
                val beamBottomY = 600.dp.toPx()
                val gradEndY = 500.dp.toPx()

                withTransform({
                    translate(left = cx, top = cy)
                    rotate(beamRotation)
                    translate(left = -cx, top = -cy)
                }) {
                    val beamPath = Path().apply {
                        moveTo(cx, cy)
                        lineTo(beamLeftX, beamBottomY)
                        lineTo(beamRightX, beamBottomY)
                        close()
                    }
                    drawPath(
                        path = beamPath,
                        brush = Brush.verticalGradient(listOf(Color(0xFF2197F5).copy(alpha = 0.5f), Color(0xFF9B51E0).copy(alpha = 0.15f), Color.Transparent), startY = cy, endY = gradEndY),
                        blendMode = BlendMode.Screen
                    )
                }

                // Renderizado del cuerpo de BE
                scale(size.width / 100f, size.height / 100f) {
                    drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(68f, 68f), Offset(92f, 92f), 16f, StrokeCap.Round)
                    drawLine(Color(0xFF1E293B), Offset(65f, 65f), Offset(90f, 90f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF22D3EE), Offset(70f, 70f), Offset(85f, 85f), 14f, StrokeCap.Round)
                    drawLine(Color(0xFF0891B2), Offset(73f, 73f), Offset(82f, 82f), 6f, StrokeCap.Round)

                    drawCircle(Color(0xFF0A0E14), 36f, Offset(45f, 45f))
                    drawCircle(Color(0xFF2197F5).copy(alpha = 0.2f), 32f, Offset(45f, 45f))
                    drawCircle(Color(0xFF2197F5), 32f, Offset(45f, 45f), style = Stroke(6f))

                    drawArc(color = Color.White.copy(alpha = 0.7f), startAngle = 180f, sweepAngle = 90f, useCenter = false, style = Stroke(4f, cap = StrokeCap.Round), topLeft = Offset(13f, 13f), size = Size(64f, 64f))

                    drawOval(Color.White, Offset(24f, 45f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 4.5f * eyeScaleY, Offset(34f + pupilX, 45f + pupilY))
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(35f + pupilX, 44f + pupilY))

                    drawOval(Color.White, Offset(50f, 45f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 4.5f * eyeScaleY, Offset(60f + pupilX, 45f + pupilY))
                    drawCircle(Color.White, 1.5f * eyeScaleY, Offset(61f + pupilX, 44f + pupilY))
                }
            }
        }
    }
}


// ==========================================================================================
// --- 2. MINI ANIMACIÓN SHERLOCK BE + SABUESO (DIRECCIÓN ALTERNADA Y DISEÑO REALISTA) ---
// Ideal para colocar dentro de Cards en LazyColumns mientras cargan los datos.
// ==========================================================================================

/**
@Composable
fun MiniLoadingSherlockBe(
    modifier: Modifier = Modifier,
    text: String = "Buscando resultados...",
    subText: String = "Investigando opciones",
    index: Int = 0 // Controla el desfase de tiempo y la dirección (Pares -> Derecha, Impares -> Izquierda)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sherlock_anim_$index")

    // Lógica de Dirección: Impares se invierten visualmente y caminan hacia la izquierda
    val isMovingRight = index % 2 == 0
    val delayOffset = index * 900 // Desfase en milisegundos

    // Colores dinámicos para darle variedad a los rayos de luz
    val accentColors = listOf(Color(0xFF2197F5), Color(0xFF9B51E0), Color(0xFF10B981), Color(0xFFF43F5E))
    val beamColor = accentColors[index % accentColors.size]

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        // Ciclo de progreso base (0f -> 1f)
        val walkProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delayOffset)
            ),
            label = "walk_progress"
        )

        // Interpolación del movimiento X
        val startX = -120f
        val endX = maxWidthPx + 150f
        val currentX = if (isMovingRight) {
            androidx.compose.ui.util.lerp(startX, endX, walkProgress)
        } else {
            androidx.compose.ui.util.lerp(endX, startX, walkProgress)
        }

        // Rebotes
        val beBounce by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 4f,
            animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse, StartOffset(delayOffset)), label = "be_bounce"
        )

        val dogBounce by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 3f,
            animationSpec = infiniteRepeatable(tween(200, easing = FastOutSlowInEasing), RepeatMode.Reverse, StartOffset(delayOffset)), label = "dog_bounce"
        )

        // UI de Textos (Fijos arriba a la izquierda/derecha según dirección)
        Column(
            modifier = Modifier.padding(16.dp).zIndex(20f).fillMaxWidth(),
            horizontalAlignment = if (isMovingRight) Alignment.Start else Alignment.End
        ) {
            Text(text.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = beamColor, letterSpacing = 1.sp)
            Text(subText, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
        }

        // Renderizado Vectorial Nativo
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomStart)
        ) {
            // Piso sutil
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, size.height - 15.dp.toPx()),
                end = Offset(size.width, size.height - 15.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )

            // Contenedor General Trasladado
            withTransform({
                translate(left = currentX, top = size.height - 50.dp.toPx())

                // Efecto Espejo si camina a la izquierda
                if (!isMovingRight) {
                    scale(scaleX = -1f, scaleY = 1f, pivot = Offset(0f, 0f))
                }

                // Escala general del dibujo para que entre bien en tarjetas pequeñas
                scale(1.2f, pivot = Offset(0f, 0f))
            }) {

                // ==========================================
                // 1. BE SHERLOCK (Va atrás, empujado al centro: X=0)
                // ==========================================
                withTransform({
                    translate(left = 0f, top = -beBounce)
                }) {
                    // Haz de luz de la Lupa (Con color temático)
                    val beamPath = Path().apply {
                        moveTo(25f, -10f)
                        lineTo(85f, 10f)
                        lineTo(85f, -20f)
                        close()
                    }
                    drawPath(beamPath, Brush.horizontalGradient(listOf(beamColor.copy(alpha=0.35f), Color.Transparent), startX = 25f, endX = 85f))

                    // Mango original de Lupa
                    drawLine(Color(0xFF1E293B), Offset(10f, 0f), Offset(0f, 15f), 8f, StrokeCap.Round)
                    drawLine(Color(0xFF2197F5), Offset(10f, 0f), Offset(3f, 11f), 4f, StrokeCap.Round)

                    // Lente de Be (Cyan Original siempre)
                    drawCircle(Color(0xFF0A0E14), 16f, Offset(15f, -10f))
                    drawCircle(Color(0xFF2197F5).copy(0.2f), 13f, Offset(15f, -10f))
                    drawCircle(Color(0xFF2197F5), 13f, Offset(15f, -10f), style = Stroke(3f))

                    // Ojos (Mirando hacia el perro a la derecha)
                    drawOval(Color.White, Offset(9f, -14f), Size(5f, 8f))
                    drawCircle(Color.Black, 1.5f, Offset(12f, -10f))
                    drawOval(Color.White, Offset(17f, -14f), Size(5f, 8f))
                    drawCircle(Color.Black, 1.5f, Offset(20f, -10f))

                    // Sombrero Deerstalker Detallado
                    // Domo
                    val hatDome = Path().apply {
                        moveTo(3f, -22f)
                        cubicTo(3f, -34f, 27f, -34f, 27f, -22f)
                        close()
                    }
                    drawPath(hatDome, Color(0xFF8B5A2B))
                    drawPath(hatDome, Color(0xFF5D4037), style = Stroke(1.5f))
                    // Viseras (Frontal y Trasera)
                    drawPath(Path().apply { moveTo(27f, -22f); quadraticBezierTo(33f, -22f, 34f, -18f) }, Color(0xFF8B5A2B), style = Stroke(3.5f, cap = StrokeCap.Round))
                    drawPath(Path().apply { moveTo(3f, -22f); quadraticBezierTo(-3f, -22f, -4f, -18f) }, Color(0xFF8B5A2B), style = Stroke(3.5f, cap = StrokeCap.Round))
                    // Orejeras atadas arriba
                    drawPath(Path().apply { moveTo(9f, -22f); cubicTo(9f, -30f, 15f, -30f, 15f, -32f) }, Color(0xFF5D4037), style = Stroke(2f, cap = StrokeCap.Round))
                    drawPath(Path().apply { moveTo(21f, -22f); cubicTo(21f, -30f, 15f, -30f, 15f, -32f) }, Color(0xFF5D4037), style = Stroke(2f, cap = StrokeCap.Round))
                    drawCircle(Color(0xFF3E2723), 2f, Offset(15f, -32f))

                    // Pipa Calabash
                    val pipeStem = Path().apply { moveTo(22f, -3f); quadraticBezierTo(30f, 4f, 37f, 2f) }
                    drawPath(pipeStem, Color(0xFF3E2723), style = Stroke(2.5f, cap = StrokeCap.Round))
                    drawRoundRect(Color(0xFF5D4037), Offset(35f, -6f), Size(7f, 9f), CornerRadius(2f))
                    drawCircle(Color(0xFFEF4444).copy(0.8f), 1.5f, Offset(38.5f, -6f))

                    // Humo Animado (Math trick)
                    val smokeTime = (walkProgress * 300) % 20f
                    val smokeAlpha = (1f - (smokeTime / 20f)).coerceIn(0f, 1f)
                    drawCircle(Color.White.copy(alpha = smokeAlpha), 1.5f + (smokeTime / 5f), Offset(39f, -10f - smokeTime))
                }

                // ==========================================
                // 2. EL SABUESO (Va adelante en X = +45)
                // ==========================================
                withTransform({
                    translate(left = 45f, top = -dogBounce + 15f)
                }) {
                    // Cálculo de ciclo de patas (velocidad de la caminata)
                    val cycle = walkProgress * Math.PI * 24
                    val legSwing1 = (sin(cycle) * 5f).toFloat()
                    val legSwing2 = (cos(cycle) * 5f).toFloat()

                    // Cola (meneo rápido)
                    val tailWag = (sin(walkProgress * Math.PI * 48) * 15f).toFloat()
                    withTransform({
                        translate(5f, 5f)
                        rotate(tailWag - 20f)
                        translate(-5f, -5f)
                    }) {
                        drawLine(Color(0xFF8D6E63), Offset(5f, 5f), Offset(-5f, -2f), 4f, StrokeCap.Round)
                    }

                    // Pata Trasera Derecha (Oscura, inversa)
                    withTransform({ translate(13f, 12f); rotate(-legSwing1); translate(-13f, -12f) }) {
                        drawLine(Color(0xFF3E2723), Offset(13f, 12f), Offset(13f, 20f), 3f, StrokeCap.Round)
                    }
                    // Pata Delantera Derecha (Oscura, inversa)
                    withTransform({ translate(28f, 12f); rotate(-legSwing1); translate(-28f, -12f) }) {
                        drawLine(Color(0xFF3E2723), Offset(28f, 12f), Offset(28f, 20f), 3f, StrokeCap.Round)
                    }

                    // Cuerpo del perro
                    drawRoundRect(Color(0xFF8D6E63), Offset(5f, 0f), Size(30f, 14f), CornerRadius(8f))

                    // Pata Trasera Izquierda (Clara, normal)
                    withTransform({ translate(8f, 12f); rotate(legSwing1); translate(-8f, -12f) }) {
                        drawLine(Color(0xFF5D4037), Offset(8f, 12f), Offset(8f, 20f), 3.5f, StrokeCap.Round)
                    }
                    // Pata Delantera Izquierda (Clara, normal)
                    withTransform({ translate(23f, 12f); rotate(legSwing1); translate(-23f, -12f) }) {
                        drawLine(Color(0xFF5D4037), Offset(23f, 12f), Offset(23f, 20f), 3.5f, StrokeCap.Round)
                    }

                    // Cabeza (Inclinada hacia abajo olfateando)
                    withTransform({
                        translate(30f, 6f)
                        rotate(25f)
                    }) {
                        val headPath = Path().apply { moveTo(0f, -6f); lineTo(16f, -2f); cubicTo(20f, 0f, 20f, 6f, 16f, 8f); lineTo(0f, 6f); close() }
                        drawPath(headPath, Color(0xFF8D6E63))
                        drawCircle(Color(0xFF111111), 2f, Offset(17f, 6f)) // Nariz
                        drawCircle(Color(0xFF111111), 1f, Offset(6f, 0f)) // Ojo

                        // Oreja caída
                        val earPath = Path().apply { moveTo(2f, -4f); cubicTo(8f, -4f, 10f, 4f, 8f, 16f); cubicTo(6f, 22f, -2f, 20f, 0f, 10f); close() }
                        drawPath(earPath, Color(0xFF5D4037))
                    }
                }
            }
        }
    }
}
**/

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF020408, widthDp = 360, heightDp = 800)
@Composable
fun LoadingBeAssistantPreview() {
    MyApplicationTheme {
        LoadingBeAssistantScreen(
            mainText = "Sincronizando...",
            subText = "Conectando con la red Maverick"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun MiniLoadingSherlockPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Ejemplo de Múltiples Tarjetas Alternando
            repeat(4) { index ->
                Surface(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF161C24),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shadowElevation = 15.dp
                ){}

                /**

                {
                    MiniLoadingSherlockBe(
                        text = if(index % 2 == 0) "Analizando..." else "Verificando...",
                        subText = "Rastreando base de datos",
                        index = index // <-- El índice controla la dirección y el desfase
                    )
                }
                **/

            }
        }
    }
}










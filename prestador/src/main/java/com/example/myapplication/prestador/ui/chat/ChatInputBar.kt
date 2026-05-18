package com.example.myapplication.prestador.ui.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.data.model.ServiceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    onCancelAudio: () -> Unit,
    isRecording: Boolean,
    recordingTime: Int,
    replyingToContent: String? = null,
    replyingToSender: String? = null,
    onCancelReply: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val coroutineScope = rememberCoroutineScope()

    // Estados para cancelar grabación con animación
    var dragOffsetX by remember { mutableStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) } // Tapa abierta
    var deleteTriggered by remember { mutableStateOf(false) } // En vuelo
    var isDraggingMic by remember { mutableStateOf(false) } // Nuevo: detecta si estamos arrastrando

    // Valores animados para el vuelo del micrófono
    val micTranslationX = remember { Animatable(0f) }
    val micTranslationY = remember { Animatable(0f) }
    val micRotation = remember { Animatable(0f) }
    val micScale = remember { Animatable(1f) }
    val micAlpha = remember { Animatable(1f) }

    // Umbral de cancelación (en píxeles)
    val cancelThreshold = with(density) { -120.dp.toPx() }

    // Estados para gestos de deslizar (legacy, pueden eliminarse después)
    var isDraggingToCancel by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }
    var micSwallowed by remember { mutableStateOf(false) }
    var trashBounce by remember { mutableStateOf(false) }

    // Funciones para manejar la grabación con animación
    fun startRecording() {
        deleteTriggered = false
        isDeleting = false
        coroutineScope.launch {
            micTranslationX.snapTo(0f)
            micTranslationY.snapTo(0f)
            micRotation.snapTo(0f)
            micScale.snapTo(1f)
            micAlpha.snapTo(1f)
        }
    }

    fun stopRecording() {
        dragOffsetX = 0f
        isDeleting = false
    }

    fun cancelRecordingAnimation() {
        deleteTriggered = true
        // Capturar la posición actual del drag
        val currentDragPosition = dragOffsetX

        coroutineScope.launch {
            // Inicializar las animaciones desde donde está el mic actualmente
            micTranslationX.snapTo(currentDragPosition)
            micTranslationY.snapTo(0f)

            // CÁLCULO CORRECTO:
            // El Row tiene: tacho (lado izq) | Spacer(1f) | texto(2f) | Spacer(1f) | mic (lado der)
            // Total weights = 4f, el mic está en el extremo derecho
            //
            // Posición del tacho desde el borde izquierdo:
            // - 16dp padding del Box (línea 388)
            // - 8dp padding del tacho (línea 403)
            // - 16dp centro del tacho (32dp / 2, sin scale porque aún no escaló)
            // = 40dp desde el borde izquierdo de la pantalla
            //
            // Posición del mic: está en el borde derecho menos paddings
            // - screenWidth - 16dp (padding del Box) - 28dp (mitad del mic 56dp/2)
            //
            // Distancia a recorrer = posición_mic - posición_tacho
            // = (screenWidth - 44dp) - 40dp = screenWidth - 84dp
            // Como usamos offset negativo: -(screenWidth - 84dp)

            val trashCenterFromLeft = with(density) { (16.dp + 8.dp + 16.dp).toPx() }
            val micCenterFromRight = with(density) { (16.dp + 28.dp).toPx() } // padding + radio del mic

            // Distancia total que debe recorrer el mic hacia la izquierda (negativa)
            val trashPositionX = -(screenWidthPx - trashCenterFromLeft - micCenterFromRight)

            launch {
                micTranslationX.animateTo(
                    targetValue = trashPositionX,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
            }
            launch {
                // Arco parabólico más suave y natural, sin saltitos
                micTranslationY.animateTo(
                    targetValue = with(density) { 0.dp.toPx() }, // Caer a la altura del tacho
                    animationSpec = keyframes {
                        durationMillis = 1000
                        0f at 0 with FastOutSlowInEasing // Inicio suave
                        with(density) { -180.dp.toPx() } at 450 with FastOutSlowInEasing // Sube más lento
                        with(density) { -120.dp.toPx() } at 650 with FastOutSlowInEasing // Empieza a bajar
                        with(density) { -50.dp.toPx() } at 850 with FastOutSlowInEasing // Sigue bajando gradualmente
                        with(density) { 0.dp.toPx() } at 1000 with FastOutSlowInEasing // Cae suavemente
                    }
                )
            }
            launch {
                micRotation.animateTo(
                    targetValue = -360f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing)
                )
            }
            launch {
                micScale.animateTo(
                    targetValue = 0.3f,
                    animationSpec = keyframes {
                        durationMillis = 1000
                        1.2f at 120 with FastOutSlowInEasing // Crece un poco
                        0.8f at 600 with FastOutSlowInEasing // Se mantiene
                        0.3f at 1000 // Se achica al final
                    }
                )
            }
            launch {
                micAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 1000
                        1f at 800 with FastOutSlowInEasing // Se mantiene visible
                        0f at 1000 // Desaparece rápido al final
                    }
                )
            }

            delay(1050)
            isDeleting = false
            delay(200)
            onCancelAudio()
            stopRecording()
            deleteTriggered = false

            micTranslationX.snapTo(0f)
            micTranslationY.snapTo(0f)
            micRotation.snapTo(0f)
            micScale.snapTo(1f)
            micAlpha.snapTo(1f)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Barra de preview de respuesta ──────────────────────────────────────
        AnimatedVisibility(
            visible = replyingToContent != null && !isRecording,
            enter = androidx.compose.animation.expandVertically() + fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2197F5))
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = replyingToSender ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2197F5),
                        maxLines = 1
                    )
                    Text(
                        text = replyingToContent ?: "",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar respuesta",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, clip = false)
            .background(colors.surfaceColor)
            .drawWithContent {
                drawContent()
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        val isTextEmpty = messageText.trim().isEmpty()

        // Crossfade entre UI normal y UI de grabación
        Crossfade(
            targetState = isRecording,
            label = "recordingState",
            modifier = Modifier.align(Alignment.BottomStart)
        ) { recording ->
            if (!recording) {
                // UI NORMAL de input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp,
                            end = if (isTextEmpty) 56.dp else 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de adjuntar
                    IconButton(
                        onClick = onAttachClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Adjuntar",
                            tint = Color(0xFFF97316)
                        )
                    }

                    // Campo de texto con botones integrados
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF97316).copy(alpha = 0.0f),
                                        Color(0xFFF97316).copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.15f),
                                        Color(0xFFF97316).copy(alpha = 0.3f),
                                        Color(0xFFF97316).copy(alpha = 0.0f)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            // Campo de texto
                            TextField(
                                value = messageText,
                                onValueChange = onMessageTextChange,
                                placeholder = { Text("Escribe un mensaje...") },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                maxLines = 4
                            )

                            // Botón de cámara (solo visible cuando no hay texto)
                            AnimatedVisibility(
                                visible = isTextEmpty,
                                enter = fadeIn(tween(200)) + scaleIn(
                                    tween(200),
                                    initialScale = 0.8f
                                ),
                                exit = fadeOut(tween(200)) + scaleOut(
                                    tween(200),
                                    targetScale = 0.8f
                                )
                            ) {
                                IconButton(
                                    onClick = onCameraClick,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Cámara",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Botón de enviar (solo cuando hay texto; el mic está fuera del Crossfade)
                            if (!isTextEmpty) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = Color(0xFFF97316),
                                            shape = CircleShape
                                        )
                                        .clickable { onSendMessage() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Enviar",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // UI DE GRABACIÓN (slide to cancel con animación de vuelo)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 72.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Tacho de basura animado (aparece al deslizar)
                        AnimatedVisibility(
                            visible = dragOffsetX < -20,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .scale(if (isDeleting) 1.2f else 1f)
                            ) {
                                TrashCanIcon(isLidOpen = isDeleting, isRed = isDeleting)
                            }
                        }

                        // Texto de tiempo y "Desliza para cancelar"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .weight(1f)
                                .alpha((1f - (dragOffsetX.absoluteValue / 200f)).coerceIn(0f, 1f))
                        ) {
                            // Punto rojo parpadeante
                            val infiniteTransition = rememberInfiniteTransition()
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.3f,
                                animationSpec = infiniteRepeatable(
                                    tween(800),
                                    RepeatMode.Reverse
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red.copy(alpha = alpha))
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val mins = recordingTime / 60
                            val secs = recordingTime % 60
                            Text(
                                text = "${mins}:${secs.toString().padStart(2, '0')}",
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Light
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                " Desliza para cancelar",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
            // ── Botón mic PERSISTENTE fuera del Crossfade ──────────────────────
            // Un único pointer input que no se destruye al cambiar el estado de grabación
            if (isTextEmpty || isRecording) {
                val currentTx = if (deleteTriggered) micTranslationX.value else dragOffsetX
                val currentTy = if (deleteTriggered) micTranslationY.value else 0f
                val currentRot = if (deleteTriggered) micRotation.value else 0f
                val currentScale = if (deleteTriggered) micScale.value
                else if (isRecording) 1.5f else 1f
                val currentAlpha = if (deleteTriggered) micAlpha.value
                else if (isRecording && dragOffsetX < -50) 0.6f else 1f

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .offset { IntOffset(currentTx.roundToInt(), currentTy.roundToInt()) }
                        .rotate(currentRot)
                        .scale(currentScale)
                        .alpha(currentAlpha)
                        .size(if (isRecording) 56.dp else 40.dp)
                        .background(Color(0xFFF97316), CircleShape)
                        .pointerInput(isRecording) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (!isRecording) {
                                    // Tap para iniciar grabación
                                    val up = waitForUpOrCancellation()
                                    if (up != null && !deleteTriggered) {
                                        startRecording()
                                        onMicClick()
                                    }
                                } else {
                                    // Modo grabación: drag izquierda para cancelar
                                    if (!deleteTriggered) {
                                        do {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull() ?: break
                                            if (!change.pressed) break
                                            val delta = change.position.x - down.position.x
                                            if (delta < 0) {
                                                dragOffsetX = delta.coerceAtLeast(-200f)
                                                isDeleting = dragOffsetX < -80f
                                            }
                                            if (dragOffsetX < -140f && !deleteTriggered) {
                                                cancelRecordingAnimation()
                                                break
                                            }
                                        } while (true)

                                        if (!deleteTriggered) {
                                            // Soltó sin cancelar → detener y enviar
                                            onMicClick()
                                            dragOffsetX = 0f
                                            isDeleting = false
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isRecording) "Grabando" else "Grabar audio",
                        tint = Color.White,
                        modifier = Modifier.size(if (isRecording) 28.dp else 22.dp)
                    )
                }
            }
        }
    } // cierra Box interno
    } // cierra Column wrapper
}

// Menú flotante de adjuntos con burbujas
@Composable
fun AttachmentOptionsMenu(
    serviceType: ServiceType,
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onCameraClick: () -> Unit,
    onLocationClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onScheduleVisitClick: () -> Unit,
    onScheduleLocalClick: () -> Unit
) {
    // Menú de burbujas flotantes
    Column(
        modifier = Modifier.padding(start = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Presupuesto / Consulta (según tipo)
        AttachmentBubble(
            icon = Icons.Default.Description,
            label = if (serviceType == ServiceType.PROFESSIONAL) "Consulta" else "Presupuesto",
            color = Color(0xFFF97316),
            onClick = onDocumentClick
        )

        // Ubicación
        AttachmentBubble(
            icon = Icons.Default.LocationOn,
            label = "Ubicación",
            color = Color(0xFF10B981),
            onClick = onLocationClick
        )

        // Visita Técnica
        AttachmentBubble(
            icon = Icons.Default.CalendarToday,
            label = "Visita Técnica",
            color = Color(0xFF8B5CF6),
            onClick = onScheduleVisitClick
        )

        // Turno en local
        AttachmentBubble(
            icon = Icons.Default.Store,
            label = "Turno en Local",
            color = Color(0xFF6366f1),
            onClick = onScheduleLocalClick
        )


        // Imágenes
        AttachmentBubble(
            icon = Icons.Default.Image,
            label = "Imágenes",
            color = Color(0xFF3B82F6),
            onClick = onImageClick
        )
    }
}

@Composable
fun AttachmentBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ícono circular
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Etiqueta
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937)
            )
        }
    }
}

@Composable
fun TrashCanIcon(isLidOpen: Boolean, isRed: Boolean) {
    val color = if (isRed) Color(0xFFEF4444) else Color(0xFF94A3B8) // Rojo o gris
    val lidRotation by animateFloatAsState(
        targetValue = if (isLidOpen) -35f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val lidTranslateY by animateFloatAsState(
        targetValue = if (isLidOpen) -4f else 0f
    )

    Canvas(modifier = Modifier.size(32.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Cuerpo del tacho
        val bodyPath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.3f)
            lineTo(size.width * 0.3f, size.height * 0.85f)
            quadraticBezierTo(size.width * 0.32f, size.height * 0.95f, size.width * 0.5f, size.height * 0.95f)
            quadraticBezierTo(size.width * 0.68f, size.height * 0.95f, size.width * 0.7f, size.height * 0.85f)
            lineTo(size.width * 0.75f, size.height * 0.3f)
        }
        drawPath(bodyPath, color, style = stroke)

        // Líneas verticales del cuerpo
        drawLine(color, Offset(size.width * 0.42f, size.height * 0.45f), Offset(size.width * 0.42f, size.height * 0.75f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.58f, size.height * 0.45f), Offset(size.width * 0.58f, size.height * 0.75f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)

        // Tapa (animada) - Usando transformaciones de canvas correctamente
        val pivotX = size.width * 0.15f
        val pivotY = size.height * 0.3f

        // Guardar estado del canvas
        drawContext.canvas.save()

        // Trasladar al punto de pivote
        drawContext.canvas.translate(pivotX, pivotY + lidTranslateY)

        // Rotar alrededor del origen (que ahora es el punto de pivote)
        drawContext.canvas.rotate(lidRotation)

        // Trasladar de vuelta
        drawContext.canvas.translate(-pivotX, -pivotY)

        // Dibujar la tapa
        // Línea horizontal tapa
        drawLine(
            color,
            start = Offset(size.width * 0.15f, size.height * 0.3f),
            end = Offset(size.width * 0.85f, size.height * 0.3f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Manija tapa
        val handlePath = Path().apply {
            moveTo(size.width * 0.4f, size.height * 0.3f)
            lineTo(size.width * 0.4f, size.height * 0.22f)
            quadraticBezierTo(size.width * 0.4f, size.height * 0.18f, size.width * 0.5f, size.height * 0.18f)
            quadraticBezierTo(size.width * 0.6f, size.height * 0.18f, size.width * 0.6f, size.height * 0.22f)
            lineTo(size.width * 0.6f, size.height * 0.3f)
        }
        drawPath(handlePath, color, style = stroke)

        // Restaurar estado del canvas
        drawContext.canvas.restore()
    }
}





@Composable
fun SwipeToReply(
    onReply: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val threshold = 120f
    val iconAlpha by animateFloatAsState(
        targetValue = (offsetX.value / threshold).coerceIn(0f, 1f),
        label = "iconAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetX.value >= threshold) {
                                onReply()
                            }
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    if (dragAmount > 0 || offsetX.value > 0) {
                        coroutineScope.launch {
                            val newVal = (offsetX.value + dragAmount).coerceIn(0f, threshold * 1.2f)
                            offsetX.snapTo(newVal)
                        }
                    }
                }
            }
    ) {
        // Reply icon revealed on swipe
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Responder",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = iconAlpha),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .size(24.dp)
        )
        // Message content shifts right
        Box(modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }) {
            content()
        }
    }
}

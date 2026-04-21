package com.example.myapplication.presentation.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.isOn
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.TableInfo
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.concurrent.TaskRunner
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


// --- 1. HEADER CON ESTILO GLASS ---
@Composable
fun ChatHeader(
    providerName: String,
    providerPhoto: String?,
    isOnline: Boolean,
    onBack: () -> Unit,
    appColors: AppColors,
    isProviderTyping: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.8f), // Fondo oscuro profundo
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
            }

            ProviderPhoto(
                photoData = providerPhoto,
                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(0.1f)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(providerName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (isProviderTyping) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF00FFFF),
                            modifier = Modifier.size(11.dp)
                        )
                        Text("escribiendo...", fontSize = 11.sp, color = Color(0xFF00FFFF))
                    }
                } else if (isOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).background(Color(0xFF00FFFF), CircleShape))
                        Text("En linea", fontSize = 11.sp, color = Color(0xFF00FFFF))
                    }
                }
            }

            IconButton(onClick = { /* Acción de llamada */ }) {
                Icon(Icons.Default.Call, null, tint = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}


// --- 2. BARRA DE ENTRADA (MÁXIMA TECNOLOGÍA) ---
@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    appColors: AppColors,
    onAttachMenuToggle: () -> Unit,
    onCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onCancelAudio: () -> Unit,
    isRecordingAudio: Boolean
) {
    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx() }
    val coroutineScope = rememberCoroutineScope()

    var recordingTime by remember { mutableIntStateOf(0) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteTriggered by remember { mutableStateOf(false) }

    // Animatables para el vuelo del micrófono al tacho
    val micTranslationX = remember { Animatable(0f) }
    val micTranslationY = remember { Animatable(0f) }
    val micRotation = remember { Animatable(0f) }
    val micScale = remember { Animatable(1f) }
    val micAlpha = remember { Animatable(1f) }

    // Timer de grabación
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingTime = 0
            while (isRecordingAudio) { delay(1000); recordingTime++ }
        } else {
            dragOffsetX = 0f
        }
    }

    fun resetMicState() {
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

    fun cancelRecordingAnimation() {
        isDeleting = true
        deleteTriggered = true
        val currentDrag = dragOffsetX
        val trashCenterFromLeft = with(density) { (16.dp + 8.dp + 16.dp).toPx() }
        val micCenterFromRight = with(density) { (16.dp + 28.dp).toPx() }
        val trashPositionX = -(screenWidthPx - trashCenterFromLeft - micCenterFromRight)
        val up180 = with(density) { -180.dp.toPx() }
        val up120 = with(density) { -120.dp.toPx() }
        val up50  = with(density) { -50.dp.toPx() }

        coroutineScope.launch {
            micTranslationX.snapTo(currentDrag)
            micTranslationY.snapTo(0f)

            coroutineScope.launch {
                micTranslationX.animateTo(trashPositionX, tween(1000, easing = FastOutSlowInEasing))
            }
            coroutineScope.launch {
                micTranslationY.animateTo(0f, keyframes {
                    durationMillis = 1000
                    0f at 0 with FastOutSlowInEasing
                    up180 at 450 with FastOutSlowInEasing
                    up120 at 650 with FastOutSlowInEasing
                    up50  at 850 with FastOutSlowInEasing
                    0f at 1000 with FastOutSlowInEasing
                })
            }
            coroutineScope.launch {
                micRotation.animateTo(-360f, tween(1000, easing = FastOutSlowInEasing))
            }
            coroutineScope.launch {
                micScale.animateTo(0.3f, keyframes {
                    durationMillis = 1000
                    1.2f at 120 with FastOutSlowInEasing
                    0.8f at 600 with FastOutSlowInEasing
                    0.3f at 1000
                })
            }
            coroutineScope.launch {
                micAlpha.animateTo(0f, keyframes {
                    durationMillis = 1000
                    1f at 800 with FastOutSlowInEasing
                    0f at 1000
                })
            }
            delay(1050)
            onCancelAudio()
            delay(200)
            resetMicState()
        }
    }

    // Valores actuales del mic (vuelo o estado normal)
    val currentTx = if (deleteTriggered) micTranslationX.value else dragOffsetX
    val currentTy = if (deleteTriggered) micTranslationY.value else 0f
    val currentRot = if (deleteTriggered) micRotation.value else 0f
    val currentScale = if (deleteTriggered) micScale.value else if (isRecordingAudio) 1.3f else 1f
    val currentAlpha = if (deleteTriggered) micAlpha.value else 1f

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false }
        ) {
            Crossfade(targetState = isRecordingAudio, label = "recordingCrossfade") { recording ->
                if (!recording) {
                    // MODO NORMAL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onAttachMenuToggle) {
                            Icon(Icons.Default.Add, null, tint = appColors.accentBlue)
                        }
                        TextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Escribe algo...", color = Color.Gray, fontSize = 15.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White
                            ),
                            maxLines = 4
                        )
                        if (inputText.isEmpty()) {
                            IconButton(onClick = onCameraClick) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.Gray)
                            }
                        }
                        if (inputText.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(appColors.accentBlue)
                                    .clickable { onSendMessage(inputText) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                } else {
                    // MODO GRABACIÓN con tacho y "desliza para cancelar"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 72.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Tacho de basura animado
                            AnimatedVisibility(
                                visible = dragOffsetX < -20,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                Box(modifier = Modifier
                                    .padding(start = 8.dp)
                                    .scale(if (isDeleting) 1.2f else 1f)
                                ) {
                                    ClientTrashCanIcon(isLidOpen = isDeleting, accentColor = appColors.accentBlue)
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Punto rojo + tiempo + desliza para cancelar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(2f)
                                    .alpha(((1f - (kotlin.math.abs(dragOffsetX) / 200f)).coerceIn(0f, 1f)))
                            ) {
                                // Punto rojo parpadeante
                                val infiniteTransition = rememberInfiniteTransition(label = "blink")
                                val blinkAlpha by infiniteTransition.animateFloat(
                                    initialValue = 1f, targetValue = 0.2f,
                                    animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                                    label = "blinkAlpha"
                                )
                                Box(
                                    modifier = Modifier.size(10.dp).clip(CircleShape)
                                        .background(Color.Red.copy(alpha = blinkAlpha))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val mins = recordingTime / 60
                                val secs = recordingTime % 60
                                Text(
                                    text = "${mins}:${secs.toString().padStart(2, '0')}",
                                    color = appColors.textPrimaryColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Text(" Desliza para cancelar", color = Color.Gray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Botón mic persistente (fuera del Crossfade para mantener gestos)
            if (inputText.isEmpty() || isRecordingAudio) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp, bottom = 4.dp, top = 4.dp)
                        .offset { IntOffset(currentTx.roundToInt(), currentTy.roundToInt()) }
                        .rotate(currentRot)
                        .scale(currentScale)
                        .alpha(currentAlpha)
                        .size(if (isRecordingAudio) 52.dp else 44.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingAudio) Color.Red else appColors.accentBlue)
                        .pointerInput(isRecordingAudio) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (!isRecordingAudio) {
                                    // Esperar si es long press para iniciar grabación
                                    var isLong = false
                                    val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                                    val upOrCancel = withTimeoutOrNull(longPressTimeout) {
                                        waitForUpOrCancellation()
                                    }
                                    if (upOrCancel == null) {
                                        isLong = true
                                        resetMicState()
                                        onAudioClick()
                                    } else {
                                        if (inputText.isNotEmpty()) onSendMessage(inputText)
                                    }
                                } else {
                                    // Modo grabación: detectar drag hacia la izquierda
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
                                            // Soltó sin cancelar → enviar
                                            if (dragOffsetX > -140f) onAudioClick()
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
                        imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecordingAudio) "Detener" else "Grabar",
                        tint = Color.White,
                        modifier = Modifier.size(if (isRecordingAudio) 26.dp else 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientTrashCanIcon(isLidOpen: Boolean, accentColor: Color) {
    val color = if (isLidOpen) Color(0xFFEF4444) else accentColor.copy(alpha = 0.7f)
    val lidRotation by animateFloatAsState(
        targetValue = if (isLidOpen) -35f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "lidRot"
    )
    val lidTranslateY by animateFloatAsState(
        targetValue = if (isLidOpen) -4f else 0f,
        label = "lidY"
    )
    Canvas(modifier = Modifier.size(32.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val bodyPath = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.3f)
            lineTo(size.width * 0.3f, size.height * 0.85f)
            quadraticBezierTo(size.width * 0.32f, size.height * 0.95f, size.width * 0.5f, size.height * 0.95f)
            quadraticBezierTo(size.width * 0.68f, size.height * 0.95f, size.width * 0.7f, size.height * 0.85f)
            lineTo(size.width * 0.75f, size.height * 0.3f)
        }
        drawPath(bodyPath, color, style = stroke)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.45f), androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.75f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, androidx.compose.ui.geometry.Offset(size.width * 0.58f, size.height * 0.45f), androidx.compose.ui.geometry.Offset(size.width * 0.58f, size.height * 0.75f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        val pivotX = size.width * 0.15f
        val pivotY = size.height * 0.3f
        drawContext.canvas.save()
        drawContext.canvas.translate(pivotX, pivotY + lidTranslateY)
        drawContext.canvas.rotate(lidRotation)
        drawContext.canvas.translate(-pivotX, -pivotY)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.3f), end = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.3f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        val handlePath = Path().apply {
            moveTo(size.width * 0.4f, size.height * 0.3f)
            lineTo(size.width * 0.4f, size.height * 0.22f)
            quadraticBezierTo(size.width * 0.4f, size.height * 0.18f, size.width * 0.5f, size.height * 0.18f)
            quadraticBezierTo(size.width * 0.6f, size.height * 0.18f, size.width * 0.6f, size.height * 0.22f)
            lineTo(size.width * 0.6f, size.height * 0.3f)
        }
        drawPath(handlePath, color, style = stroke)
        drawContext.canvas.restore()
    }
}

// --- 3. MENÚ DE ADJUNTOS TIPO "FLOATING GLASS" ---
@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onLocationClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
        color = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AttachmentIcon(Icons.Default.Image, "Imagen", Color(0xFF8B5CF6), onImageClick)
            AttachmentIcon(Icons.Default.LocationOn, "Mapa", Color(0xFF10B981), onLocationClick)
            AttachmentIcon(Icons.Default.CalendarMonth, "Cita", Color(0xFF3B82F6), onScheduleClick)
            AttachmentIcon(Icons.Default.Description, "Cotizar", Color(0xFF4F46E5), onInviteClick)
        }
    }
}

@Composable
fun AttachmentIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

// --- 4. DIÁLOGOS DE AGENDAMIENTO REAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

    // Aquí podrías integrar un real DatePickerDialog de Android. Por ahora, estética moderna:
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text("Programar Visita", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecciona una fecha y hora para que el prestador visite tu domicilio.", color = Color.Gray, fontSize = 13.sp)

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Instrucciones o Notas", color = Color.Cyan) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Cyan
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm("15/02/2026", "10:30", notes) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Text("Enviar Propuesta", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
        }
    )
}

// --- 5. TARJETA DE PRESUPUESTO ESTILO CYBER ---
@Composable
fun TarjetaPresupuestoChat(
    title: String,
    amount: String,
    status: String,
    isFromMe: Boolean,
    appColors: AppColors,
    onClick: () -> Unit
) {
    val neonColor = Color(0xFF00FF9F) // Verde Neón

    Surface(
        modifier = Modifier
            .width(260.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, neonColor.copy(alpha = 0.3f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(
                Brush.horizontalGradient(listOf(neonColor, Color.Transparent))
            ))

            Column(modifier = Modifier.padding(16.dp)) {
                Text("PRESUPUESTO RECIBIDO", color = neonColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(amount, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)

                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = neonColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "VER DETALLES",
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = neonColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- BURBUJAS DE MENSAJE ---
@Composable
fun MessageBubble(
    message: MessageEntity,
    appColors: AppColors,
    currentUserId: String = "currentUser",
    onBudgetClick: (String) -> Unit = {}
) {
    val isFromMe = message.senderId ==
            currentUserId
    val context = LocalContext.current

    when (message.type) {

        MessageType.LOCATION -> {
            val lat = message.latitude
            val lng = message.longitude
            val mapsUrl = if (lat != null &&
                lng != null)

                "https://www.google.com/maps?q=$lat,$lng" else
                null

            Surface(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .widthIn(max = 280.dp)
                    .clickable {
                        mapsUrl?.let {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                        }
                    },
                shape =
                    RoundedCornerShape(16.dp),
                color = if (isFromMe)
                    appColors.accentBlue else
                    appColors.surfaceColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    if (lat != null && lng != null) {
                        //Calcula el tile OSM correspondiente a las coordenadas
                        val zoom = 15
                        val n = 1 shl zoom
                        val xTile = ((lng + 180.0) / 360.0 * n).toInt()
                        val latRad = lat * Math.PI / 180.0
                        val yTile = ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n).toInt()
                        val tileUrl = "https://tile.openstreetmap.org/$zoom/$xTile/$yTile.png"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            SubcomposeAsyncImage(model = ImageRequest.Builder(context)
                                .data(tileUrl)
                                .addHeader("User-Agent", "MyApplication/1.0 Android")
                                .crossfade(true)
                                .build(),
                                contentDescription = "Mapa", modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = {
                                    Box(
                                        modifier = Modifier.fillMaxSize()
                                            .background(Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.LocationOn, null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(40.dp))
                                    }
                                }
                            ) //Pin rojo siempre visible sbre el mapa
                            Icon(Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(44.dp).offset(y = (-8).dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(40.dp))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()

                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement
                        = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.LocationOn,
                            contentDescription
                            = null,
                            tint = if
                                           (isFromMe) Color.White else Color(0xFF10B981),
                            modifier =
                                Modifier.size(18.dp)
                        )
                        Column(modifier =
                            Modifier.weight(1f)) {
                            Text(
                                text =
                                    "Ubicación compartida",
                                fontSize =
                                    13.sp,
                                fontWeight =
                                    FontWeight.SemiBold,
                                color = if
                                                (isFromMe) Color.White else
                                    appColors.textPrimaryColor
                            )
                            Text(
                                text = "Toca para abrir en Maps",
                                fontSize =
                                    11.sp,
                                color = if
                                                (isFromMe) Color.White.copy(0.75f) else
                                    appColors.textSecondaryColor
                            )
                        }
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 10.sp,
                            color = if
                                            (isFromMe) Color.White.copy(0.7f) else
                                appColors.textSecondaryColor
                        )
                    }
                }
            }
        }

        else -> {
            val bubbleColor = if (isFromMe) appColors.accentBlue else appColors.surfaceColor
            val textColor = if (isFromMe) Color.White
            else appColors.textPrimaryColor
            val timeColor = if (isFromMe) Color.White.copy(alpha = 0.7f) else appColors.textSecondaryColor

            val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

            val isRead = message.isRead || message.status == "READ"
            val isDelivered = message.status == "DELIVERED" || isRead
            val tickColor = if (isRead) Color(0xFF53BDEB) else timeColor

            Surface(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .widthIn(max = 280.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromMe) 16.dp else 4.dp,
                    bottomEnd = if (isFromMe) 4.dp else 16.dp
                ),
                color = bubbleColor,
                shadowElevation = 2.dp

            ) {
                Column(modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 8.dp,
                    bottom = 6.dp
                )
                ) {
                    Text(
                        text = message.content,
                        color = textColor,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = timeColor
                        )
                        if (isFromMe) {
                            if (isDelivered) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = tickColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = null,
                                    tint = tickColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AudioMessageBubble(
    audioPath: String,
    duration: Int,
    timestamp: Long,
    appColors: AppColors,
    isFromMe: Boolean,
    senderAvatarUrl: String? = null
) {
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var audioDuration by remember { mutableIntStateOf(if (duration > 0) duration * 1000 else 0) }

    DisposableEffect(audioPath) { onDispose { mediaPlayer?.release() } }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { currentPosition = it.currentPosition }
            delay(100)
        }
    }

    fun togglePlay() {
        android.util.Log.d("AudioBubble", "audioPath = $audioPath")
        if (isPlaying) {
            mediaPlayer?.pause(); isPlaying = false
        } else {
            if (mediaPlayer == null) {
                try {
                    val mp = MediaPlayer()
                    mp.setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    mp.setDataSource(audioPath)
                    mp.setOnPreparedListener { player ->
                        audioDuration = player.duration
                        player.start()
                        isPlaying = true
                    }
                    mp.setOnCompletionListener {
                        isPlaying = false; currentPosition = 0
                    }
                    mp.setOnErrorListener { _, what, extra ->
                        android.util.Log.e("AudioBubble", "Error: what=$what extra=$extra url=$audioPath")
                        isPlaying = false; true
                    }
                    mp.prepareAsync()
                    mediaPlayer = mp
                } catch (e: Exception) {
                    android.util.Log.e("AudioBubble", "Exception: ${e.message}")
                    e.printStackTrace()
                }
            } else { mediaPlayer?.start();
                isPlaying = true
            }


        }
    }

    fun formatMs(ms: Int): String {
        val s = ms / 1000
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromMe) {
            Box(modifier = Modifier.size(44.dp)) {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = appColors.accentBlue.copy(alpha = 0.2f)) {
                    if (senderAvatarUrl != null) {
                        AsyncImage(model = senderAvatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = appColors.accentBlue, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Box(modifier = Modifier.size(18.dp).align(Alignment.BottomEnd).background(appColors.surfaceColor, CircleShape).border(1.5.dp, appColors.backgroundColor, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Mic, null, tint = appColors.textPrimaryColor, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        Surface(
            modifier = Modifier.widthIn(min = 220.dp, max = 290.dp),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isFromMe) 16.dp else 4.dp, bottomEnd = if (isFromMe) 4.dp else 16.dp),
            color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp).clickable { togglePlay() }
                    )
                    Canvas(modifier = Modifier.weight(1f).height(20.dp)) {
                        val dotCount = 28
                        val dotR = 2.5.dp.toPx()
                        val scrubR = 5.5.dp.toPx()
                        val spacing = size.width / dotCount
                        val progress = if (audioDuration > 0) currentPosition.toFloat() / audioDuration else 0f
                        val sx = (progress * size.width).coerceIn(0f, size.width)
                        repeat(dotCount) { i ->
                            val x = i * spacing + spacing / 2f
                            drawCircle(color = if (x < sx) Color.White else Color.White.copy(alpha = 0.35f), radius = dotR, center = Offset(x, size.height / 2f))
                        }
                        drawCircle(Color.White, scrubR, Offset(sx.coerceIn(scrubR, size.width - scrubR), size.height / 2f))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(formatMs(currentPosition), fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)), fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                        if (isFromMe) Icon(Icons.Default.Done, null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderPhoto(
    photoData: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val placeholder = rememberVectorPainter(Icons.Default.Person)

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(photoData)
            .crossfade(true)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        error = placeholder,
        placeholder = placeholder
    )
}


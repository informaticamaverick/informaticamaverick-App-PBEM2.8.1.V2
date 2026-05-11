package com.example.myapplication.prestador.ui.chat

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Tag
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.prestador.R
import com.example.myapplication.prestador.data.model.Message
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.RequestPage
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.modifier.modifierLocalProvider
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.work.WorkRequest
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.RequestPage

@Composable
fun QuotedMessage(
    replyToSenderName: String?,
    replyToContent: String?,
    colors: PrestadorColors,
    modifier: Modifier = Modifier
) {
    if (replyToSenderName == null || replyToContent == null) return

    val maverickBlue = Color(0xFF2197F5)
    
    Surface(
        color = Color.Black.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(maverickBlue)
            )
            
            Spacer(Modifier.width(10.dp))
            
            Column {
                Text(
                    text = replyToSenderName,
                    fontSize = 12.sp,
                    color = maverickBlue,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = replyToContent,
                    fontSize = 13.sp,
                    color = colors.textPrimary.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    senderAvatarUrl: String? = null,
    onVerPresupuesto: (() -> Unit)? = null,
    onImageClick: ((String?) -> Unit)? = null,
    onAccept: ((messageId: String) -> Unit)? = null,
    onReject: ((messageId: String, reason: String?) -> Unit)? = null,
    onCreateBudgetFromRequest: (() -> Unit)? = null,
    clientName: String = ""
){
    val colors = getPrestadorColors()
    val bubbleColor = if (message.type == Message.MessageType.BUDGET ||
        message.type == Message.MessageType.APPOINTMENT ||
        message.type == Message.MessageType.CALENDAR_INVITE ||
        message.type == Message.MessageType.APPOINTMENT_REQUEST ||
        message.type == Message.MessageType.APPOINTMENT_RECEIPT ||
        message.type == Message.MessageType.RESCHEDULE_NOTICE ||
        message.type == Message.MessageType.COMPLETION_NOTICE ||
        message.type == Message.MessageType.CANCELLATION_NOTICE ||
        message.type == Message.MessageType.BUDGET_REQUEST) {
        Color.Transparent
    } else if (isFromCurrentUser) {
        colors.primaryOrange
    } else {
        colors.surfaceElevated
    }



    val textColor = if (isFromCurrentUser) {
        Color.White
    } else {
        colors.textPrimary
    }

    // Audio: renderizado especial con avatar fuera de la burbuja (estilo WhatsApp)
    if (message.type == Message.MessageType.AUDIO) {
        AudioMessageBubbleWA(
            message = message,
            isFromCurrentUser = isFromCurrentUser,
            senderAvatarUrl = senderAvatarUrl
        )
        return
    }

    Surface(
        modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = if (message.type == Message.MessageType.APPOINTMENT) 0.dp else 2.dp
        ) {
            Column(
                modifier = if (message.type == Message.MessageType.BUDGET ||
                    message.type == Message.MessageType.APPOINTMENT ||
                    message.type == Message.MessageType.CALENDAR_INVITE ||
                    message.type == Message.MessageType.APPOINTMENT_REQUEST ||
                    message.type == Message.MessageType.APPOINTMENT_RECEIPT ||
                    message.type == Message.MessageType.RESCHEDULE_NOTICE ||
                    message.type == Message.MessageType.COMPLETION_NOTICE ||
                    message.type == Message.MessageType.CANCELLATION_NOTICE) Modifier.padding(8.dp) else Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 4.dp
                    )
            ) {
                if (message.replyToId != null) {
                    QuotedMessage(
                        replyToSenderName = message.replyToSenderName,
                        replyToContent = message.replyToContent,
                        colors = colors
                    )
                }
                when (message.type) {
                    Message.MessageType.TEXT -> {
                        // Estilo WhatsApp: texto y hora en la misma línea
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = message.text ?: "",
                                color = textColor,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = formatMessageTime(message.timestamp),
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                                if (isFromCurrentUser) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    val tickColor = if (message.isRead)
                                        Color(0xFF53BDEB) else textColor.copy(alpha = 0.7f)
                                    when { !message.isSynced -> Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )

                                        message.isRead || message.isDelivered -> Icon(
                                            Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = tickColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        else -> Icon(
                                            Icons.Default.Done,
                                            contentDescription = null,
                                            tint = textColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Message.MessageType.IMAGE -> {
                        ImageMessageContent(
                            imageUrl = message.imageUrl,
                            text = message.text,
                            onImageClick = { onImageClick?.invoke(message.imageUrl)}
                        )
                    }
                    Message.MessageType.LOCATION -> {
                        LocationMessageContent(
                            latitude = message.latitude ?: 0.0,
                            longitude = message.longitude ?: 0.0
                        )
                    }
                    Message.MessageType.DOCUMENT -> {
                        DocumentMessageContent(
                            fileName = message.fileName ?: "Archivo",
                            fileSize = message.fileSize ?: 0
                        )
                    }
                    Message.MessageType.BUDGET -> {
                        BudgetMessageContent(message = message, onVerPresupuesto = onVerPresupuesto)
                    }
                    Message.MessageType.AUDIO -> { /* handled above with early return */ }
                    Message.MessageType.APPOINTMENT -> { /* legacy — not rendered */ }
                    Message.MessageType.CALENDAR_INVITE -> {
                        CalendarInviteBubble(
                            startDate = message.calendarStartDate ?: "",
                            endDate = message.calendarEndDate ?: "",
                            isFromCurrentUser = isFromCurrentUser
                        )
                    }
                    Message.MessageType.APPOINTMENT_REQUEST -> {
                        AppointmentRequestBubble(
                            date = message.appointmentDate ?: "",
                            time = message.appointmentTime ?: "",
                            title = message.appointmentTitle ?: "",
                            rawContent = message.text,
                            status = message.appointmentStatus,
                            isFromCurrentUser = isFromCurrentUser,
                            onAccept = onAccept?.let { cb -> { serviceTitle -> cb(serviceTitle) } },
                            onReject = onReject?.let { cb -> { reason -> cb(message.id, reason) } }
                        )
                    }
                    Message.MessageType.APPOINTMENT_RECEIPT -> {
                        AppointmentReceiptBubble(
                            date = message.appointmentDate ?: "",
                            time = message.appointmentTime ?: "",
                            service = message.receiptService ?: "",
                            providerName = message.receiptProviderName ?: "",
                            isTechnician = message.receiptIsTechnician,
                            profession = message.receiptProfession,
                            address = message.receiptAddress,
                            code = message.receiptCode,
                            prioritizeCompany = message.receiptPrioritizeCompany,
                            isFromCurrentUser = isFromCurrentUser,
                            appointmentType = message.appointmentType ?: "TECHNICAL_VISIT",
                            category = message.categoryId
                        )
                    }
                    Message.MessageType.RESCHEDULE_NOTICE -> {
                        RescheduleNoticeBubble(
                            originalDate = message.appointmentDate ?: "",
                            originalTime = message.appointmentTime ?: "",
                            isFromCurrentUser = isFromCurrentUser
                        )
                    }
                    Message.MessageType.COMPLETION_NOTICE -> {
                        CompletionNoticeBubble(
                            originalDate = message.appointmentDate ?: "",
                            originalTime = message.appointmentTime ?: "",
                            isFromCurrentUser = isFromCurrentUser
                        )
                    }
                    Message.MessageType.CANCELLATION_NOTICE -> {
                        CancellationNoticeBubble(
                            originalDate = message.appointmentDate ?: "",
                            originalTime = message.appointmentTime ?: "",
                            reason = message.rejectionReason ?: "",
                            isFromCurrentUser = isFromCurrentUser
                        )
                    }
                    Message.MessageType.BUDGET_REQUEST -> {
                        BudgetRequestBubble(
                            message = message,
                            clientName = clientName,
                            onCreateBudget = { onCreateBudgetFromRequest?.invoke() }
                        )
                    }
                    Message.MessageType.VISIT,
                    Message.MessageType.TENDER,
                    Message.MessageType.SYSTEM -> {
                        Text(
                            text = message.text ?: "",
                            color = textColor,
                            fontSize = 14.sp
                        )
                    }
                }

                // Timestamp y estado (excepto para AUDIO y BUDGET que tienen su propio layout)
                if (message.type != Message.MessageType.AUDIO && message.type != Message.MessageType.BUDGET && message.type != Message.MessageType.TEXT &&
                    message.type != Message.MessageType.CALENDAR_INVITE &&
                    message.type != Message.MessageType.APPOINTMENT_REQUEST &&
                    message.type != Message.MessageType.APPOINTMENT_RECEIPT &&
                    message.type != Message.MessageType.RESCHEDULE_NOTICE &&
                    message.type != Message.MessageType.COMPLETION_NOTICE &&
                    message.type != Message.MessageType.CANCELLATION_NOTICE &&
                    message.type != Message.MessageType.BUDGET_REQUEST) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatMessageTime(message.timestamp),
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )

                        if (isFromCurrentUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Done,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
}

// Contenido de mensaje de texto
@Composable
fun TextMessageContent(
    text: String,
    textColor: Color
) {
    // Detectar URLs en el texto
    val urlPattern = Regex("(https?://[^\\s]+)")
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        urlPattern.findAll(text).forEach { match ->
            // Texto antes del URL
            append(text.substring(lastIndex, match.range.first))
            
            // URL con estilo
            pushStringAnnotation(tag = "URL", annotation = match.value)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF60A5FA),
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(match.value)
            }
            pop()
            
            lastIndex = match.range.last + 1
        }
        // Texto restante
        append(text.substring(lastIndex))
    }
    
    val context = LocalContext.current
    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle(
            color = textColor,
            fontSize = 15.sp
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                    context.startActivity(intent)
                }
        }
    )
}

// Contenido de mensaje con imagen
@Composable
fun ImageMessageContent(
    imageUrl: String?,
    text: String?,
    onImageClick: () -> Unit = {}
) {
    Column {
        if (imageUrl != null) {
            // Resuelve el modelo: local path → file:// URI, URL remota o Base64
            val model = remember(imageUrl) {
                when {
                    imageUrl.startsWith("/") -> "file://$imageUrl"
                    imageUrl.startsWith("file://") -> imageUrl
                    imageUrl.startsWith("http") -> imageUrl
                    imageUrl != "[Imagen]" && imageUrl.isNotEmpty() -> {
                        try {
                            android.util.Base64.decode(imageUrl, android.util.Base64.NO_WRAP)
                        } catch (e: Exception) { null }
                    }
                    else -> null
                }
            }

            if (model != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(model)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Text("Imagen no disponible", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                }
            }
        }

        if (!text.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}

// Burbuja de audio estilo WhatsApp (con avatar + dots progress)
@Composable
fun AudioMessageBubbleWA(
    message: com.example.myapplication.prestador.data.model.Message,
    isFromCurrentUser: Boolean,
    senderAvatarUrl: String? = null
) {
    val audioUrl = message.audioUrl
    val duration = message.audioDuration ?: 0
    val timestamp = message.timestamp
    val colors = getPrestadorColors()
    val contentColor = if (isFromCurrentUser) Color.White else colors.textPrimary
    val contentColorSecondary = if (isFromCurrentUser) Color.White.copy(0.75f) else colors.textSecondary
    val waveformActive = if (isFromCurrentUser) Color.White else colors.textPrimary
    val waveformInactive = if (isFromCurrentUser) Color.White.copy(0.35f) else colors.textPrimary.copy(0.35f)

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var audioDuration by remember { mutableStateOf(if (duration > 0) duration * 1000 else 0) }

    DisposableEffect(audioUrl) { onDispose { mediaPlayer?.release() } }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { currentPosition = it.currentPosition }
            delay(100)
        }
    }

    fun togglePlay() {
        if (isPlaying) { mediaPlayer?.pause(); isPlaying = false }
        else {
            if (mediaPlayer == null && audioUrl != null) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(audioUrl)
                        prepare()
                        audioDuration = this.duration
                        setOnCompletionListener { isPlaying = false; currentPosition = 0 }
                        start()
                    }
                    isPlaying = true
                } catch (e: Exception) { e.printStackTrace() }
            } else { mediaPlayer?.start(); isPlaying = true }
        }
    }

    fun formatMs(ms: Int): String {
        val s = ms / 1000
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar con badge mic (solo mensajes recibidos)
        if (!isFromCurrentUser) {
            Box(modifier = Modifier.size(44.dp)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = colors.primaryOrange.copy(alpha = 0.2f)
                ) {
                    if (senderAvatarUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(senderAvatarUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(androidx.compose.material.icons.Icons.Default.Person, null, tint = colors.primaryOrange, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color(0xFF2A2F32), androidx.compose.foundation.shape.CircleShape)
                        .border(1.5.dp, Color(0xFF111B21), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Mic, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        // Burbuja
        Surface(
            modifier = Modifier.widthIn(min = 220.dp, max = 290.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            ),
            color = if (isFromCurrentUser) colors.primaryOrange else colors.surfaceElevated,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                if (message.replyToId != null) {
                    QuotedMessage(
                        replyToSenderName = message.replyToSenderName,
                        replyToContent = message.replyToContent,
                        colors = colors,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) androidx.compose.material.icons.Icons.Default.Pause else androidx.compose.material.icons.Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(28.dp).clickable { togglePlay() }
                    )
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clickable {
                                // seek on tap handled via pointerInput below
                            }
                    ) {
                        val dotCount = 28
                        val dotR = 2.5.dp.toPx()
                        val scrubR = 5.5.dp.toPx()
                        val spacing = size.width / dotCount
                        val progress = if (audioDuration > 0) currentPosition.toFloat() / audioDuration else 0f
                        val sx = (progress * size.width).coerceIn(0f, size.width)
                        repeat(dotCount) { i ->
                            val x = i * spacing + spacing / 2f
                            drawCircle(
                                color = if (x < sx) waveformActive else waveformInactive,
                                radius = dotR,
                                center = Offset(x, size.height / 2f)
                            )
                        }
                        drawCircle(waveformActive, scrubR, Offset(sx.coerceIn(scrubR, size.width - scrubR), size.height / 2f))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatMs(currentPosition), fontSize = 11.sp, color = contentColorSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)),
                            fontSize = 11.sp,
                            color = contentColorSecondary
                        )
                        if (isFromCurrentUser) {
                            Icon(androidx.compose.material.icons.Icons.Default.Done, null, tint = contentColorSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// Contenido de mensaje de ubicación
@Composable
fun LocationMessageContent(
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current
    val colors = getPrestadorColors()

    // Generar link de Google Maps
    val mapsUrl = "https://www.google.com/maps?q=$latitude,$longitude"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                context.startActivity(intent)
            }
    ) {
        //Preview del mapa (OpenStreetMap tiles)
        val zoom = 15
        val n = 1 shl zoom
        val xTile = ((longitude + 180.0) / 360.0 * n).toInt()
        val latRad = latitude * Math.PI / 180.0
        val yTile = ((1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * n).toInt()
        val tileUrl = "https://tile.openstreetmap.org/$zoom/$xTile/$yTile.png"

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            coil.compose.SubcomposeAsyncImage(
                model = coil.request.ImageRequest.Builder(context)
                    .data(tileUrl)
                    .addHeader("User-Agent", "MyApplication/1.0 Android")
                    .crossfade(true)
                    .build(),
                contentDescription = "Mapa",
                modifier = Modifier.fillMaxSize(),
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
            )
            // Pin rojo siempre visible sobre el mapa
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(44.dp).offset(y = (-8).dp)
            )
        }
        // Footer con icono + texto
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    colors.surfaceElevated,
                    RoundedCornerShape(
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = "Ubicación compartida",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Toca para abrir en Maps",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}




// Contenido de mensaje de documento
@Composable
fun DocumentMessageContent(
    fileName: String,
    fileSize: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF97316)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = formatFileSize(fileSize),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        
        IconButton(onClick = { /* TODO: Descargar */ }) {
            Icon(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Descargar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Funciones auxiliares
private fun formatMessageTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatAudioDuration(millis: Int): String {
    val seconds = millis / 1000
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

// Indicador de "escribiendo..." con animación de puntos
@Composable
fun TypingIndicator(
    userName: String
) {
    val colors = getPrestadorColors()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 200.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 4.dp,
                bottomEnd = 16.dp
            ),
            color = colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryOrange
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "escribiendo",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    // Animación de puntos
                    TypingDots()
                }
            }
        }
    }
}

@Composable
fun TypingDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            var alpha by remember { mutableStateOf(0.3f) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay((index * 200).toLong())
                    alpha = 1f
                    delay(300)
                    alpha = 0.3f
                    delay(300)
                }
            }

            Text(
                text = "•",
                fontSize = 16.sp,
                color = Color.Gray.copy(alpha = alpha)
            )
        }
    }
}


// --- BUDGET MESSAGE ---
@Composable
fun BudgetMessageContent(
    message: com.example.myapplication.prestador.data.model.Message,
    onVerPresupuesto: (() -> Unit)? = null
) {
    val Orange = Color(0xFFFF6B35)
    val SlateLight = Color(0xFFF8FAFC)
    val SlateBorder = Color(0xFFE2E8F0)
    val SlateText = Color(0xFF475569)
    val SlateDark = Color(0xFF1E293B)

    // Deserializar todos los items del presupuesto
    fun parseItems(json: String?, sep1: Char = '|', sep2: Char = ';'): List<Pair<String, String>> {
        if (json.isNullOrBlank()) return emptyList()
        return json.split(sep1).mapNotNull { s ->
            val p = s.split(sep2)
            when {
                p.size >= 4 -> Pair(p[1], "$ ${String.format("%,.2f", (p[2].toIntOrNull() ?: 1) * (p[3].toDoubleOrNull() ?: 0.0))}")
                p.size >= 3 -> Pair(p[1], "$ ${String.format("%,.2f", p[2].toDoubleOrNull() ?: 0.0)}")
                p.size >= 2 -> Pair(p[0], "$ ${String.format("%,.2f", p[1].toDoubleOrNull() ?: 0.0)}")
                else -> null
            }
        }
    }

    val allLines = buildList {
        addAll(parseItems(message.budgetItemsJson))
        addAll(parseItems(message.budgetServiciosJson))
        addAll(parseItems(message.budgetHonorariosJson))
        addAll(parseItems(message.budgetGastosJson))
        addAll(parseItems(message.budgetImpuestosJson))
    }

    Column(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
    ) {
        // Header naranja
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Orange)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("📋", fontSize = 16.sp)
                Text("PRESUPUESTO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(message.budgetNumero ?: "", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
        }

        // Líneas de items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateLight)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (allLines.isEmpty()) {
                Text("Sin ítems", fontSize = 11.sp, color = SlateText)
            } else {
                allLines.take(4).forEach { (desc, total) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            desc.take(22) + if (desc.length > 22) "…" else "",
                            fontSize = 11.sp, color = SlateDark,
                            modifier = Modifier.weight(1f)
                        )
                        Text(total, fontSize = 11.sp, color = SlateDark, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (allLines.size > 4) {
                    Text("+ ${allLines.size - 4} ítems más…", fontSize = 10.sp, color = SlateText)
                }
            }
        }

        HorizontalDivider(color = SlateBorder)

        // Footer con total
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TOTAL", fontSize = 9.sp, color = SlateText, fontWeight = FontWeight.Bold)
                Text(
                    "$ ${String.format("%,.2f", message.budgetTotal ?: 0.0)}",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Orange
                )
            }
            if ((message.budgetValidezDias ?: 0) > 0) {
                Text(
                    "Válido ${message.budgetValidezDias} días",
                    fontSize = 10.sp, color = SlateText
                )
            }
        }

        if (!message.budgetNotas.isNullOrBlank()) {
            HorizontalDivider(color = SlateBorder)
            Text(
                message.budgetNotas,
                fontSize = 10.sp, color = SlateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Botón "Ver presupuesto"
        HorizontalDivider(color = SlateBorder)
        TextButton(
            onClick = { onVerPresupuesto?.invoke() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = Orange)
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Ver presupuesto", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ImageZoomDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 6f)
        offset += panChange
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val imageBitmap = remember(imageUrl) {
                try {
                    val bytes = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } catch (e: Exception) { null }
            }
            if (imageBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(transformableState),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(imageUrl).build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(transformableState),
                    contentScale = ContentScale.Fit
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Burbuja para CALENDAR_INVITE (lado del prestador).
 * Muestra un resumen de la disponibilidad enviada al cliente.
 */
@Composable
fun CalendarInviteBubble(
    startDate: String,
    endDate: String,
    isFromCurrentUser: Boolean
) {
    val colors = getPrestadorColors()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFromCurrentUser) colors.primaryOrange.copy(alpha = 0.15f) else colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.4f)),
        modifier = Modifier.widthIn(min = 200.dp, max = 280.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Calendario enviado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Del $startDate al $endDate",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "El cliente puede elegir un turno disponible",
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

/**
 * Burbuja para APPOINTMENT_REQUEST (lado del prestador).
 * El cliente solicitó un turno → el prestador puede Aceptar o Rechazar.
 * Cuando ya tiene estado ACCEPTED/REJECTED muestra el badge correspondiente.
 */
@Composable
fun AppointmentRequestBubble(
    date: String,
    time: String,
    title: String = "",
    rawContent: String? = null,
    status: Message.AppointmentProposalStatus?,
    isFromCurrentUser: Boolean,
    onAccept: ((serviceTitle: String) -> Unit)? = null,
    onReject: ((reason: String?) -> Unit)? = null
) {
    val colors = getPrestadorColors()
    var showRejectDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var serviceTitle by remember { mutableStateOf(title.ifBlank { "Servicio técnico" }) }

    // Parsear contenido pipe-separated: "Solicitud de turno|DíaNombre|HoraInicio|HoraFin"
    val contentParts = remember(rawContent) {
        rawContent?.takeIf { it.contains("|") }?.split("|") ?: emptyList()
    }
    val dayName = contentParts.getOrNull(1)?.takeIf { it.isNotBlank() && !it.contains("-") }
    val endTime = contentParts.getOrNull(3)?.takeIf { it.isNotBlank() }

    val borderColor = when (status) {
        Message.AppointmentProposalStatus.ACCEPTED -> Color(0xFF4CAF50)
        Message.AppointmentProposalStatus.REJECTED -> Color(0xFFF44336)
        else -> colors.primaryOrange.copy(alpha = 0.6f)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.widthIn(min = 220.dp, max = 300.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Solicitud de turno",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = colors.primaryOrange.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Servicio / título
            if (title.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.Build,
                    label = "Servicio",
                    value = title,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Fecha
            if (date.isNotBlank()) {
                val displayDate = if (dayName != null) "$dayName, $date" else date
                ReceiptInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha",
                    value = displayDate,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Hora
            if (time.isNotBlank()) {
                val displayTime = if (endTime != null) "$time – $endTime" else time
                ReceiptInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Hora",
                    value = displayTime,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Badge de estado o botones
            when (status) {
                Message.AppointmentProposalStatus.ACCEPTED -> {
                    HorizontalDivider(color = Color(0xFF4CAF50).copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aceptado",
                            color = Color(0xFF4CAF50),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Message.AppointmentProposalStatus.REJECTED -> {
                    HorizontalDivider(color = Color(0xFFF44336).copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rechazado",
                            color = Color(0xFFF44336),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    HorizontalDivider(color = colors.primaryOrange.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aceptar", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            border = BorderStroke(1.dp, Color(0xFFF44336)),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFF44336))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rechazar", fontSize = 12.sp, color = Color(0xFFF44336))
                        }
                    }
                }
            }
        }
    }

    // Dialog para confirmar y editar servicio
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar turno") },
            text = {
                Column {
                    Text("Servicio / Motivo:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = serviceTitle,
                        onValueChange = { serviceTitle = it },
                        placeholder = { Text("Ej: Instalación de gas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAccept?.invoke(serviceTitle.ifBlank { "Servicio técnico" })
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog para rechazar
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Rechazar solicitud") },
            text = {
                Column {
                    Text("Podés indicar un motivo (opcional):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Ej: No tengo disponibilidad ese día") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject?.invoke(rejectReason.ifBlank { null })
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Comprobante de turno confirmado (APPOINTMENT_RECEIPT)
 * Se envía automáticamente cuando el prestador acepta un turno.
 * Muestra: Servicio, Profesional, Fecha, Hora, Domicilio/Consultorio, Código.
 */

@Composable
fun AppointmentReceiptBubble(
    date: String,
    time: String,
    service: String,
    providerName: String,
    isTechnician: Boolean,
    profession: String? = null,
    address: String? = null,
    code: String? = null,
    prioritizeCompany: Boolean = false,
    isFromCurrentUser: Boolean,
    appointmentType: String = "TECHNICAL_VISIT",
    category: String? = null
) {
    val colors = getPrestadorColors()
    val isLocal = appointmentType == "LOCAL_APPOINTMENT"
    val accentGreen = Color(0xFF4CAF50)
    val accentBlue = Color(0xFF2197F5)
    val accentColor = if (isLocal) accentBlue else accentGreen
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = Modifier.widthIn(min = 200.dp, max = 300.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTechnician) "Visita técnica confirmada" else "Turno confirmado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Categoría
            if (!category.isNullOrBlank()) {
                ReceiptInfoRow(icon = Icons.Default.Tag, label = "Categoría", value = category, colors = colors)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Servicio
            if (service.isNotBlank()) {
                ReceiptInfoRow(icon = Icons.Default.Build, label = "Servicio", value = service, colors = colors)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Profesional o Empresa según configuración
            if (providerName.isNotBlank()) {
                if (prioritizeCompany) {
                    // Mostrar como Empresa
                    ReceiptInfoRow(icon = Icons.Default.Business, label = "Empresa", value = providerName, colors = colors)
                } else {
                    // Mostrar como Profesional independiente
                    val profValue = if (!profession.isNullOrBlank()) "$providerName · $profession" else providerName
                    ReceiptInfoRow(icon = Icons.Default.Person, label = "Profesional", value = profValue, colors = colors)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Fecha y hora
            if (time.isNotBlank()) {
                ReceiptInfoRow(icon = Icons.Default.AccessTime, label = "Hora", value = time, colors = colors)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Domicilio
            if (!address.isNullOrBlank()) {
                val context = LocalContext.current
                ReceiptInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = if (isTechnician) "Domicilio" else "Dirección",
                    value = address,
                    colors = colors,
                    clickable = true,
                    onClick = {
                        val uri = android.net.Uri.parse(
                            "https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode(address)}"
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Código
            if (!code.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                HorizontalDivider(color = accentColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = code,
                        fontSize = 11.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    colors: PrestadorColors,
    clickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (clickable) Color(0xFF1976D2) else colors.primaryOrange,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(
            modifier = if (clickable) Modifier.clickable { onClick() } else Modifier
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = if (clickable) Color(0xFF1976D2) else colors.textPrimary,
                lineHeight = 16.sp,
                textDecoration = if (clickable) androidx.compose.ui.text.style.TextDecoration.Underline else androidx.compose.ui.text.style.TextDecoration.None
            )
        }
    }
}

@Composable
fun RescheduleNoticeBubble(
    originalDate: String,
    originalTime: String,
    isFromCurrentUser: Boolean
) {
    val colors = getPrestadorColors()

    val formattedDate = remember(originalDate) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val displaySdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            displaySdf.format(sdf.parse(originalDate)!!)
        } catch (e: Exception) { originalDate }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFromCurrentUser) colors.primaryOrange.copy(alpha = 0.12f) else colors.surfaceElevated,
        border = BorderStroke(1.dp, Color(0xFFFF6B35).copy(alpha = 0.7f)),
        modifier = Modifier.widthIn(min = 200.dp, max = 300.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tu turno será reprogramado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFFF6B35).copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            if (formattedDate.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha original",
                    value = formattedDate,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (originalTime.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Hora original",
                    value = originalTime,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFFF6B35).copy(alpha = 0.08f)
            ) {
                Text(
                    text = "El prestador necesita cambiar la fecha. Pronto recibirás nuevos horarios disponibles para elegir.",
                    fontSize = 11.sp,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun CancellationNoticeBubble(
    originalDate: String,
    originalTime: String,
    reason: String,
    isFromCurrentUser: Boolean
) {
    val colors = getPrestadorColors()
    val formattedDate = remember(originalDate) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val displaySdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            displaySdf.format(sdf.parse(originalDate)!!)
        } catch (e: Exception) { originalDate }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFromCurrentUser) Color(0xFFEF4444).copy(alpha = 0.08f) else colors.surfaceElevated,
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
        modifier = Modifier.widthIn(min = 200.dp, max = 300.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Turno cancelado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFEF4444).copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            if (formattedDate.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha",
                    value = formattedDate,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (originalTime.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Hora",
                    value = originalTime,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (reason.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Motivo: $reason",
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionNoticeBubble(
    originalDate: String,
    originalTime: String,
    isFromCurrentUser: Boolean
) {
    val colors = getPrestadorColors()
    val formattedDate = remember(originalDate) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val displaySdf = java.text.SimpleDateFormat("dd//MM/yyyy", java.util.Locale.getDefault())
            displaySdf.format(sdf.parse(originalDate)!!)
        } catch (e: Exception) { originalDate }
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isFromCurrentUser) Color(0xFF059669).copy(alpha = 0.08f) else colors.surfaceElevated,
        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.6f)),
        modifier = Modifier.widthIn(min = 200.dp, max = 300.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Turno completado",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFF059669).copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            if (formattedDate.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha",
                    value = formattedDate,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (originalTime.isNotBlank()) {
                ReceiptInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Hora",
                    value = originalTime,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF059669).copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "El cliente recibirá una solicitud de calificación.",
                        fontSize = 11.sp,
                        color = Color(0xFF059669),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }

}

@Composable
fun BudgetRequestBubble(
    message: Message,
    clientName: String,
    onCreateBudget: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = colors.surfaceVariant,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.7f)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RequestPage,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.padding(4.dp).size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Solicitud de presupuesto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.primary
                        )
                        if (clientName.isNotBlank()) {
                            Text(
                                text = clientName,
                                fontSize = 11.sp,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = colors.primary.copy(alpha = 0.2f)
                )

                // Descripción
                if (!message.budgetRequestDescription.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.budgetRequestDescription,
                            fontSize = 13.sp,
                            color = colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Dirección
                if (!message.budgetRequestClientAddress.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.budgetRequestClientAddress,
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Botón - siempre visible
                Button(
                    onClick = onCreateBudget,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Crear Presupuesto", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Timestamp
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(message.timestamp)),
                    fontSize = 10.sp,
                    color = colors.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

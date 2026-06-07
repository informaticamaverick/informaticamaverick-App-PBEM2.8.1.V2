package com.example.myapplication.presentation.components

import android.os.Build
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.features.chat.ChatThread
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getThemeColors
import com.example.myapplication.presentation.designsystem.components.MaverickTypography
import com.example.myapplication.presentation.registry.MaverickIcons
import kotlinx.coroutines.delay
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.text.BasicTextField
import java.text.SimpleDateFormat
import java.util.*

// --- SECCIÓN: INTERACCIÓN Y ANIMACIÓN ---

/**
 * SwipeToReplyWrapper: Contenedor que permite deslizar un mensaje para responder.
 * Inspirado en Telegram/WhatsApp.
 */
@Composable
fun SwipeToReplyWrapper(
    onReply: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxSwipe = 150f
    val haptic = LocalHapticFeedback.current
    var isTriggered by remember { mutableStateOf(false) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "swipe_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val nextX = (offsetX + dragAmount).coerceIn(0f, maxSwipe)
                        offsetX = nextX
                        
                        if (offsetX >= maxSwipe * 0.7f && !isTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isTriggered = true
                        } else if (offsetX < maxSwipe * 0.7f && isTriggered) {
                            isTriggered = false
                        }
                    },
                    onDragEnd = {
                        if (isTriggered) onReply()
                        offsetX = 0f
                        isTriggered = false
                    },
                    onDragCancel = {
                        offsetX = 0f
                        isTriggered = false
                    }
                )
            }
    ) {
        // Icono de respuesta con animación de escala y opacidad
        val progress = (offsetX / (maxSwipe * 0.7f)).coerceIn(0f, 1f)
        val iconScale = if (isTriggered) 1.2f else progress
        val iconAlpha = progress

        if (offsetX > 10f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        alpha = iconAlpha
                    }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    tint = if (isTriggered) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(modifier = Modifier.offset { IntOffset(animatedOffsetX.toInt(), 0) }) {
            content()
        }
    }
}

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

@Composable
fun ReplyPreviewBar(
    message: MessageEntity,
    onCancel: () -> Unit,
    colors: AppColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceColor,
        border = BorderStroke(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF22D3EE))
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = if (message.senderId == "current") "Tú" else "Prestador",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF22D3EE),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// --- SECCIÓN: INDICADORES ---

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            BouncingDot(delay = index * 200)
        }
    }
}

@Composable
private fun BouncingDot(delay: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dy by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dy"
    )

    Box(
        modifier = Modifier
            .size(6.dp)
            .offset { IntOffset(0, dy.dp.roundToPx()) }
            .clip(CircleShape)
            .background(Color(0xFF22D3EE).copy(alpha = 0.6f))
    )
}

// --- SECCIÓN: CABECERAS ---

@Composable
fun TelegramStyleChatHeader(
    title: String,
    photoUrl: Any?,
    isOnline: Boolean,
    onBack: () -> Unit,
    appColors: AppColors,
    isFilterActive: Boolean = false,
    collapseFraction: Float = 0f,
    onInfoClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val height = lerp(80.dp, 64.dp, collapseFraction)
    val avatarSize = lerp(48.dp, 36.dp, collapseFraction)
    val titleSize = lerp(18.sp, 15.sp, collapseFraction)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        color = appColors.surfaceColor.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }

            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.2f)),
                    contentScale = ContentScale.Crop
                )
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(appColors.surfaceColor)
                            .padding(2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF22C55E)))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable { onInfoClick() }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = titleSize),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isOnline) "En línea" else "Visto por última vez recientemente",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOnline) Color(0xFF22D3EE) else Color.Gray
                )
            }

            Row {
                IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, null, tint = Color.White) }
                IconButton(onClick = onDeleteClick) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
            }
        }
    }
}

@Composable
fun ElitePillAction(
    text: String,
    emoji: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun EliteHeaderAction(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
    }
}

fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}

@Composable
fun ChatHeader(
    title: String,
    photoUrl: Any?,
    isOnline: Boolean,
    onBack: () -> Unit,
    appColors: AppColors,
    isFilterActive: Boolean = false,
    onInfoClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onBudgetClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    isPersonalProfile: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        tonalElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }

                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop
                    )
                    if (isOnline) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(appColors.surfaceColor)
                                .padding(2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF22C55E)))
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .clickable { onInfoClick() }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isOnline) "Online" else "Desconectado",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOnline) Color(0xFF22D3EE) else Color.Gray
                    )
                }

                Row {
                    IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, null, tint = Color.White) }
                    IconButton(onClick = onMoreClick) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                }
            }
            
            // Acciones Rápidas (Elite)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EliteHeaderAction("📅", "Agenda", onCalendarClick)
                EliteHeaderAction("💰", "Presupuesto", onBudgetClick)
                EliteHeaderAction("📍", "Dirección", onLocationClick)
                EliteHeaderAction("📞", "Llamar", {})
            }
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}

// --- SECCIÓN: ENTRADA ---

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit,
    appColors: AppColors,
    onAttachmentClick: () -> Unit,
    onMicClick: () -> Unit,
    onLocationClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    isRecording: Boolean = false,
    recordingTime: Int = 0
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.surfaceColor,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = onAttachmentClick) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF22D3EE))
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Grabando... ${recordingTime}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            cursorBrush = SolidColor(Color(0xFF22D3EE)),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (value.isEmpty()) {
                                        Text("Escribe un mensaje...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        IconButton(onClick = onCameraClick, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.Gray)
                        }
                    }
                }
            }

            val isNotEmpty = value.isNotBlank()
            IconButton(
                onClick = { if (isNotEmpty) onSend(value) else onMicClick() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isNotEmpty) Color(0xFF22D3EE) else Color.Transparent)
            ) {
                Icon(
                    imageVector = if (isNotEmpty) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isNotEmpty) Color(0xFF0F172A) else Color(0xFF22D3EE)
                )
            }
        }
    }
}

@Composable
fun AttachmentOptionsMenu(
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onLocation: () -> Unit,
    onBudget: () -> Unit,
    onAppointment: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                AttachmentItem("Galería", "🖼️", Color(0xFF818CF8), onGallery)
                AttachmentItem("Cámara", "📸", Color(0xFFF472B6), onCamera)
                AttachmentItem("Ubicación", "📍", Color(0xFF4ADE80), onLocation)
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                AttachmentItem("Presupuesto", "💰", Color(0xFFFBBF24), onBudget)
                AttachmentItem("Agenda", "📅", Color(0xFF22D3EE), onAppointment)
                AttachmentItem("Documento", "📄", Color(0xFF94A3B8), {})
            }
        }
    }
}

@Composable
fun AttachmentItem(label: String, emoji: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

@Composable
fun DateSeparator(timestamp: Long, colors: AppColors) {
    val locale = LocalConfiguration.current.locales[0]
    val dateText = remember(timestamp) {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM", locale)
        sdf.format(Date(timestamp)).replaceFirstChar { it.uppercase() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- SECCIÓN: MENSAJES ---

@Composable
fun QuotedMessage(
    sender: String?,
    content: String?,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        color = Color.Black.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF22D3EE))
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = sender ?: "Usuario",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF22D3EE),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = content ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    appColors: AppColors,
    isFromMe: Boolean,
    onReply: () -> Unit = {},
    budget: BudgetEntity? = null,
    categoryEmoji: String? = null,
    allCategories: List<CategoryEntity> = emptyList(),
    onAcceptBudget: () -> Unit = {},
    onRejectBudget: () -> Unit = {},
    onBudgetClick: (BudgetEntity) -> Unit = {},
    onAcceptAppointment: (String) -> Unit = {},
    onRejectAppointment: () -> Unit = {},
    onRescheduleAppointment: () -> Unit = {},
    isRepliable: Boolean = true,
    onImageClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {},
    isEnabled: Boolean = true
) {
    val bubbleColor = if (isFromMe) Color(0xFF1E293B) else Color(0xFF334155)
    val alignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isFromMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val locale = LocalConfiguration.current.locales[0]

    SwipeToReplyWrapper(onReply = onReply) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = alignment
        ) {
            Column(horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start) {
                Surface(
                    color = bubbleColor,
                    shape = shape,
                    modifier = Modifier.widthIn(max = 280.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Si es respuesta a otro mensaje
                        if (message.replyToId != null) {
                            QuotedMessage(
                                sender = message.replyToSenderName,
                                content = message.replyToContent,
                                appColors = appColors
                            )
                        }

                        when (message.type) {
                            MessageType.TEXT -> {
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            MessageType.IMAGE -> {
                                ImageMessageBubble(message, appColors, isFromMe, onClick = onImageClick)
                            }
                            MessageType.LOCATION -> {
                                LocationMessageBubble(message, appColors, isFromMe, onClick = {
                                    message.locationAddress?.let { onAddressClick(it) }
                                })
                            }
                            MessageType.AUDIO -> {
                                AudioMessageBubble(message, appColors, isFromMe)
                            }
                            MessageType.BUDGET_REQUEST -> {
                                BudgetRequestBubble(
                                    message = message,
                                    isMe = isFromMe,
                                    appColors = appColors
                                )
                            }
                            MessageType.BUDGET -> {
                                if (budget != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    BudgetBubble(
                                        message = message,
                                        budget = budget,
                                        isMe = isFromMe,
                                        appColors = appColors,
                                        onClick = { onBudgetClick(budget) }
                                    )
                                }
                            }
                            MessageType.VISIT -> {
                                TechnicalVisitProposalBubble(
                                    message = message,
                                    isMe = isFromMe,
                                    appColors = appColors,
                                    onAccept = { onAcceptAppointment(message.id) },
                                    onReject = onRejectAppointment
                                )
                            }
                            MessageType.CALENDAR_INVITE -> {
                                CalendarInviteBubble(
                                    message = message,
                                    isMe = isFromMe,
                                    appColors = appColors,
                                    isEnabled = isEnabled,
                                    onClick = onCalendarClick
                                )
                            }
                            MessageType.SYSTEM -> {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = message.content,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            else -> {
                                Text(text = message.content, color = Color.Gray)
                            }
                        }

                        Row(
                            modifier = Modifier.align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeStr = remember(message.timestamp) {
                                SimpleDateFormat("HH:mm", locale).format(Date(message.timestamp))
                            }
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = Color.Gray
                            )
                            if (isFromMe) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (message.isRead) MaverickIcons.DoneAll else MaverickIcons.Update,
                                    contentDescription = null,
                                    tint = if (message.isRead) Color(0xFF22D3EE) else Color.Gray,
                                    modifier = Modifier.size(12.dp)
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
fun EnhancedMessageBubble(
    message: MessageEntity,
    isFromMe: Boolean,
    appColors: AppColors,
    onReply: () -> Unit
) {
    MessageBubble(
        message = message,
        appColors = appColors,
        isFromMe = isFromMe,
        onReply = onReply
    )
}




@Composable
fun ChatThreadSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.03f))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifiedChatListItem(
    thread: ChatThread,
    unreadCount: Int,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier 
) {
    val backgroundColor = if (isSelected) Color(0xFF22D3EE).copy(alpha = 0.08f) else Color.Transparent
    val profileImage = remember(thread.photoUrl) { ImageUtils.processImageSource(thread.photoUrl) }
    
    val locale = LocalConfiguration.current.locales[0]
    val timeStr = remember(thread.lastTimestamp) {
        val sdf = SimpleDateFormat("HH:mm", locale)
        sdf.format(Date(thread.lastTimestamp))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. AVATAR CON GLOW DE ESTADO (Elite Telegram Style)
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clickable { onAvatarClick() }
                        .border(
                            width = 1.dp,
                            brush = if (thread.isOnline) 
                                Brush.sweepGradient(listOf(Color(0xFF22D3EE), Color(0xFF818CF8), Color(0xFF22D3EE)))
                                else SolidColor(Color.White.copy(alpha = 0.1f)),
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    color = Color.Black
                ) {
                    if (profileImage != null) {
                        AsyncImage(
                            model = profileImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(MaverickIcons.Person, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                
                // Indicador Online vibrante
                if (thread.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .padding(2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFF4ADE80)))
                    }
                }

                if (isMultiSelectMode) {
                    ChatSelectionIndicator(
                        isSelected = isSelected,
                        modifier = Modifier.size(20.dp).offset(x = 4.dp, y = 4.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // 2. INFO DEL CHAT (Cuerpo Central)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = thread.displayName.uppercase(),
                        style = MaverickTypography.HeaderTitle.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (unreadCount > 0) Color.White else Color.White.copy(alpha = 0.9f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (thread.isVerified) {
                        Icon(
                            imageVector = MaverickIcons.Verified,
                            contentDescription = "Verificado",
                            tint = Color(0xFF22D3EE),
                            modifier = Modifier.padding(start = 4.dp).size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = thread.lastMessage,
                    style = MaverickTypography.BodyText.copy(
                        fontSize = 12.sp,
                        color = if (unreadCount > 0) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. METADATOS (Derecha - Hora y Badge)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight().padding(vertical = 2.dp)
            ) {
                Text(
                    text = timeStr,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 10.sp,
                        color = if (unreadCount > 0) Color(0xFF22D3EE) else Color.Gray,
                        fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                )

                if (unreadCount > 0) {
                    Surface(
                        color = Color(0xFF22D3EE),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                } else {
                    // Indicador de "Visto" sutil (Double Check Style)
                    Icon(
                        imageVector = MaverickIcons.DoneAll,
                        contentDescription = "Leído",
                        tint = Color(0xFF22D3EE).copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatSelectionIndicator(isSelected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (isSelected) Color(0xFF22D3EE) else Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(Icons.Default.Check, null, tint = Color(0xFF0F172A), modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun ImageZoomDialog(
    message: MessageEntity,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val imageSource = remember(message.imageUrl, message.imageLocalPath) {
                ImageUtils.processImageSource(message.imageLocalPath ?: message.imageUrl)
            }
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageSource)
                    .crossfade(true)
                    .build(),
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

            // Close button overlay
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MessageBubblePreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MessageBubble(
                message = MessageEntity(
                    id = "m1", chatId = "c1", senderId = "s1", receiverId = "r1", 
                    type = MessageType.TEXT, content = "Hola, ¿cómo estás?",
                    timestamp = System.currentTimeMillis(), status = "SENT"
                ),
                appColors = getThemeColors(),
                isFromMe = false
            )
            MessageBubble(
                message = MessageEntity(
                    id = "m2", chatId = "c1", senderId = "current", receiverId = "r1", 
                    type = MessageType.TEXT, content = "Todo bien, ¿y tú?",
                    timestamp = System.currentTimeMillis(), isRead = true, status = "READ"
                ),
                appColors = getThemeColors(),
                isFromMe = true
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MediaBubblesPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MessageBubble(
                message = MessageEntity(
                    id = "m3", chatId = "c1", senderId = "s1", receiverId = "r1",
                    type = MessageType.LOCATION, content = "", locationAddress = "Av. Siempre Viva 742",
                    timestamp = System.currentTimeMillis(), status = "SENT"
                ),
                appColors = getThemeColors(),
                isFromMe = false
            )
            Spacer(Modifier.height(8.dp))
            MessageBubble(
                message = MessageEntity(
                    id = "m4", chatId = "c1", senderId = "current", receiverId = "r1",
                    type = MessageType.AUDIO, content = "", durationSeconds = 45,
                    timestamp = System.currentTimeMillis(), status = "SENT"
                ),
                appColors = getThemeColors(),
                isFromMe = true
            )
        }
    }
}

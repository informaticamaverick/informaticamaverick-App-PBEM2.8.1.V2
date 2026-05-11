package com.example.myapplication.presentation.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
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
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.presentation.client.ChatThread
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getThemeColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// --- SECCIÓN: INTERACCIÓN PREMIUM (SWIPE TO REPLY) ---

@Composable
fun SwipeToReplyWrapper(
    onReply: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxOffset = 150f
    val threshold = 120f
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SwipeReplyAnimation"
    )
    
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX >= threshold) onReply()
                        offsetX = 0f
                        hasTriggeredHaptic = false
                    },
                    onDragCancel = {
                        offsetX = 0f
                        hasTriggeredHaptic = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        // Solo deslizar hacia la derecha (valor positivo)
                        val newOffset = (offsetX + dragAmount).coerceIn(0f, maxOffset)
                        offsetX = newOffset
                        
                        if (offsetX >= threshold && !hasTriggeredHaptic) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasTriggeredHaptic = true
                        }
                    }
                )
            }
    ) {
        // Icono de Respuesta Detrás
        val iconAlpha = (offsetX / threshold).coerceIn(0f, 1f)
        val iconScale = lerp(0.5f, 1.2f, iconAlpha)
        
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(40.dp)
                .graphicsLayer {
                    alpha = iconAlpha
                    scaleX = iconScale
                    scaleY = iconScale
                }
                .background(Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = null,
                tint = Color(0xFF22D3EE),
                modifier = Modifier.size(20.dp)
            )
        }

        // Contenido Principal (Burbuja)
        Box(
            modifier = Modifier.offset { IntOffset(animatedOffsetX.toInt(), 0) }
        ) {
            content()
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}

@Composable
fun ReplyPreviewBar(
    message: MessageEntity,
    onCancelReply: () -> Unit,
    appColors: AppColors
) {
    val maverickBlue = Color(0xFF2197F5)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = appColors.surfaceColor.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de acento vertical
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(maverickBlue)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Respondiendo a...",
                    style = MaterialTheme.typography.labelSmall,
                    color = maverickBlue,
                    fontWeight = FontWeight.Black
                )
                
                val previewText = when (message.type) {
                    MessageType.IMAGE -> "🖼️ Imagen"
                    MessageType.AUDIO -> "🎤 Audio"
                    MessageType.BUDGET -> "📄 Presupuesto"
                    MessageType.LOCATION -> "📍 Ubicación"
                    MessageType.VISIT -> "🗓️ Turno"
                    else -> message.content
                }
                
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textPrimaryColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(
                onClick = onCancelReply,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancelar",
                    tint = appColors.textSecondaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// --- SECCIÓN: CABECERA Y ENTRADA (DUMB COMPONENTS) ---

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
        color = appColors.surfaceColor,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = appColors.textPrimaryColor)
            }

            Spacer(Modifier.width(4.dp))

            // Avatar del Prestador
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = appColors.backgroundColor
                ) {
                    if (providerPhoto != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(providerPhoto)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = appColors.textSecondaryColor)
                        }
                    }
                }
                
                // Indicador Online
                if (isOnline) {
                    Surface(
                        modifier = Modifier.size(12.dp).border(2.dp, appColors.surfaceColor, CircleShape),
                        shape = CircleShape,
                        color = Color(0xFF10B981)
                    ) {}
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = providerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (isProviderTyping) {
                    Text(
                        text = "escribiendo...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                } else if (isOnline) {
                    Text(
                        text = "En línea",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondaryColor
                    )
                }
            }

            IconButton(onClick = { /* Info del prestador */ }) {
                Icon(Icons.Default.Info, contentDescription = "Información", tint = appColors.textSecondaryColor)
            }
        }
    }
}

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
    isRecordingAudio: Boolean = false
) {
    Surface(
        color = appColors.surfaceColor,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecordingAudio) {
                IconButton(onClick = onAttachMenuToggle) {
                    Text("📎", fontSize = 24.sp)
                }
                
                IconButton(onClick = onCameraClick) {
                    Icon(Icons.Default.PhotoCamera, null, tint = appColors.textSecondaryColor)
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = appColors.backgroundColor,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, appColors.textSecondaryColor.copy(alpha = 0.2f))
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        placeholder = { Text("Escribe un mensaje...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = appColors.accentBlue
                        ),
                        maxLines = 4
                    )
                }
            } else {
                // UI de Grabación de Audio
                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mic, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Grabando audio...", color = appColors.textPrimaryColor, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onCancelAudio) {
                        Text("CANCELAR", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isBlank() && !isRecordingAudio) appColors.backgroundColor else appColors.accentBlue)
                    .clickable {
                        if (inputText.isNotBlank()) onSendMessage(inputText)
                        else onAudioClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else if (isRecordingAudio) Icons.Default.Done else Icons.Default.Mic,
                    contentDescription = "Enviar",
                    tint = if (inputText.isBlank() && !isRecordingAudio) appColors.textSecondaryColor else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AttachmentOptionsMenu(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onLocationClick: () -> Unit,
    onInviteClick: () -> Unit,
    onBudgetRequestClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E293B),
        tonalElevation = 16.dp,
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AttachmentItem("🖼️", "Galería", Color(0xFF3B82F6), onImageClick)
                AttachmentItem("📍", "Ubicación", Color(0xFF10B981), onLocationClick)
                AttachmentItem("📝", "Solicitud", Color(0xFF2197F5), onBudgetRequestClick)
                AttachmentItem("⚖️", "Invitar", Color(0xFF8B5CF6), onInviteClick)
            }
        }
    }
}

@Composable
private fun AttachmentItem(emoji: String, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = color.copy(alpha = 0.2f)) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 24.sp)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DateSeparator(timestamp: Long, appColors: AppColors) {
    val dateText = remember(timestamp) {
        val now = Calendar.getInstance()
        val msgDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        
        when {
            now.get(Calendar.YEAR) == msgDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == msgDate.get(Calendar.DAY_OF_YEAR) -> "Hoy"
            
            now.get(Calendar.YEAR) == msgDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) - 1 == msgDate.get(Calendar.DAY_OF_YEAR) -> "Ayer"
            
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Surface(
            color = appColors.surfaceColor.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = dateText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textSecondaryColor
            )
        }
    }
}

// --- SECCIÓN: BURBUJAS BASE (TEXTO, IMAGEN, AUDIO, UBICACIÓN) ---

@Composable
fun QuotedMessage(
    replyToSenderName: String?,
    replyToContent: String?,
    appColors: AppColors,
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
                    color = appColors.textPrimaryColor.copy(alpha = 0.7f),
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
    budget: BudgetEntity? = null,
    categoryEmoji: String? = null,
    allCategories: List<CategoryEntity> = emptyList(), // 🔥 [NUEVO] Para resolución dinámica
    onReply: () -> Unit = {}, // 🔥 [NUEVO]
    onImageClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {},
    onAcceptAppointment: () -> Unit = {},
    onRejectAppointment: () -> Unit = {},
    isEnabled: Boolean = true
) {
    when (message.type) {
        MessageType.TEXT -> EnhancedMessageBubble(message, isFromMe, appColors, onReply = onReply)
        MessageType.IMAGE -> ImageMessageBubble(message, appColors, isFromMe, onReply = onReply, onImageClick = onImageClick)
        MessageType.LOCATION -> LocationMessageBubble(message, appColors, isFromMe, onReply = onReply)
        MessageType.VISIT -> {
            val isTechnical = message.appointmentType == "TECHNICAL_VISIT"
            if (isTechnical) {
                TechnicalVisitProposalBubble(
                    message = message, 
                    isMe = isFromMe, 
                    appColors = appColors, 
                    categoryEmoji = categoryEmoji,
                    onReply = onReply,
                    onAccept = onAcceptAppointment, 
                    onReject = onRejectAppointment
                )
            } else {
                LocalAppointmentProposalBubble(
                    message = message, 
                    isMe = isFromMe, 
                    appColors = appColors, 
                    categoryEmoji = categoryEmoji,
                    onReply = onReply,
                    onAccept = onAcceptAppointment, 
                    onReject = onRejectAppointment
                )
            }
        }
        MessageType.BUDGET -> BudgetBubble(
            message = message, 
            budget = budget, 
            isMe = isFromMe, 
            appColors = appColors, 
            categoryEmoji = categoryEmoji,
            onReply = onReply, // 🔥 [NUEVO]
            onClick = {}
        )
        MessageType.BUDGET_REQUEST -> BudgetRequestBubble(
            message = message, 
            isMe = isFromMe, 
            appColors = appColors,
            onReply = onReply // 🔥 [NUEVO]
        )
        MessageType.CALENDAR_INVITE -> CalendarInviteBubble(
            message = message, 
            isMe = isFromMe, 
            appColors = appColors, 
            isEnabled = isEnabled,
            onReply = onReply,
            onClick = onCalendarClick
        )
        MessageType.AUDIO -> {
            AudioMessageBubble(
                message = message,
                appColors = appColors, 
                isFromMe = isFromMe,
                onReply = onReply
            )
        }
        MessageType.APPOINTMENT_RECEIPT -> {
            if (message.receiptIsTechnician == true) {
                TechnicalVisitReceiptBubble(
                    message = message, 
                    isMe = isFromMe, 
                    appColors = appColors, 
                    allCategories = allCategories, 
                    onReply = onReply,
                    onCalendarClick = onCalendarClick, 
                    onAddressClick = onAddressClick
                )
            } else {
                StandardAppointmentReceiptBubble(
                    message = message, 
                    isMe = isFromMe, 
                    appColors = appColors, 
                    allCategories = allCategories, 
                    onReply = onReply,
                    onCalendarClick = onCalendarClick, 
                    onAddressClick = onAddressClick
                )
            }
        }
        else -> EnhancedMessageBubble(message, isFromMe, appColors, onReply = onReply)
    }
}

@Composable
fun EnhancedMessageBubble(message: MessageEntity, isMe: Boolean, appColors: AppColors, senderPhotoUrl: String? = null, onReply: () -> Unit = {}) {
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)
    
    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(horizontal = if (isMe) 8.dp else 0.dp)
            ) {
                if (!isMe && senderPhotoUrl != null) {
                    AsyncImage(
                        model = senderPhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(CircleShape).padding(end = 4.dp)
                    )
                }
                
                Surface(
                    color = if (isMe) appColors.accentBlue else appColors.surfaceColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp, 
                        topEnd = 16.dp, 
                        bottomStart = if (isMe) 16.dp else 2.dp, 
                        bottomEnd = if (isMe) 2.dp else 16.dp
                    ),
                    border = BorderStroke(0.5.dp, borderColor),
                    tonalElevation = if (isMe) 0.dp else 2.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Column {
                            if (message.replyToId != null) {
                                QuotedMessage(
                                    replyToSenderName = message.replyToSenderName,
                                    replyToContent = message.replyToContent,
                                    appColors = appColors
                                )
                            }
                            Text(
                                text = message.content,
                                color = if (isMe) Color.White else appColors.textPrimaryColor,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 4.dp, end = 40.dp) // Espacio para la hora
                            )
                        }
                        
                        Row(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = (if (isMe) Color.White else appColors.textSecondaryColor).copy(alpha = 0.6f)
                            )
                            if (isMe) {
                                Spacer(Modifier.width(2.dp))
                                Icon(
                                    imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = null,
                                    tint = if (message.isRead) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.6f),
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
fun ImageMessageBubble(message: MessageEntity, appColors: AppColors, isFromMe: Boolean, onReply: () -> Unit = {}, onImageClick: () -> Unit) {
    val model = remember(message.content, message.imageUrl) {
        val path = message.imageUrl
        when {
            !path.isNullOrBlank() -> if (path.startsWith("/")) "file://$path" else path
            message.content.startsWith("http") -> message.content
            else -> null
        }
    }
    val borderColor = if (isFromMe) Color(0xFF10B981) else Color(0xFF22D3EE)

    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isFromMe) 8.dp else 0.dp), 
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = appColors.surfaceColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp, 
                    bottomStart = if (isFromMe) 16.dp else 2.dp, 
                    bottomEnd = if (isFromMe) 2.dp else 16.dp
                ),
                border = BorderStroke(0.5.dp, borderColor),
                tonalElevation = 2.dp,
                modifier = Modifier.widthIn(max = 240.dp).clickable { onImageClick() }
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    if (message.replyToId != null) {
                        QuotedMessage(
                            replyToSenderName = message.replyToSenderName,
                            replyToContent = message.replyToContent,
                            appColors = appColors,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    if (model != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(model)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagen",
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.Gray.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, null, tint = appColors.textSecondaryColor)
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textSecondaryColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationMessageBubble(message: MessageEntity, appColors: AppColors, isFromMe: Boolean, onReply: () -> Unit = {}) {
    val context = LocalContext.current
    val lat = message.latitude ?: 0.0
    val lng = message.longitude ?: 0.0
    val emeraldColor = Color(0xFF10B981) // Esmeralda Maverick
    val borderColor = if (isFromMe) Color(0xFF10B981) else Color(0xFF22D3EE)
    
    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isFromMe) 8.dp else 0.dp), 
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = appColors.surfaceColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp, 
                    bottomStart = if (isFromMe) 16.dp else 2.dp, 
                    bottomEnd = if (isFromMe) 2.dp else 16.dp
                ),
                border = BorderStroke(0.5.dp, borderColor),
                tonalElevation = 2.dp,
                modifier = Modifier.widthIn(max = 260.dp).clickable {
                    val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    context.startActivity(mapIntent)
                }
            ) {
                Column {
                    if (message.replyToId != null) {
                        QuotedMessage(
                            replyToSenderName = message.replyToSenderName,
                            replyToContent = message.replyToContent,
                            appColors = appColors,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    // Header PREMIUM Ubicación
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(emeraldColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Ubicación Compartida", 
                            fontWeight = FontWeight.Black, 
                            color = Color.White, 
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Column(modifier = Modifier.padding(12.dp)) {
                        // DIRECCIÓN PRIMERO CON EMOJI
                        Row(verticalAlignment = Alignment.Top) {
                            Text("📍", fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = message.locationAddress ?: "Coordenadas GPS", 
                                fontSize = 13.sp, 
                                color = Color.White,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))

                        // TARJETA VER EN MAPA ABAJO CON EMOJI DE MAPA A COLOR
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, emeraldColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🗺️", fontSize = 32.sp) // Emoji de mapa a color
                                Spacer(Modifier.height(4.dp))
                                Text("VER EN MAPA", color = emeraldColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                            modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textSecondaryColor.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioMessageBubble(
    message: MessageEntity,
    appColors: AppColors,
    isFromMe: Boolean,
    onReply: () -> Unit = {}
) {
    val audioPath =
        if (message.content == "[Audio]" && !message.imageUrl.isNullOrBlank()) message.imageUrl else message.content
    val duration = message.durationSeconds ?: 0
    val timestamp = message.timestamp
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPosition by remember { mutableIntStateOf(0) }
    val borderColor = if (isFromMe) Color(0xFF10B981) else Color(0xFF22D3EE)

    val displayDuration = if (duration > 0) duration else 0
    val minutes = displayDuration / 60
    val seconds = displayDuration % 60
    val durationText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    DisposableEffect(audioPath) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { currentPosition = it.currentPosition }
            delay(200)
        }
    }

    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isFromMe) 8.dp else 0.dp),
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isFromMe) appColors.accentBlue else appColors.surfaceColor,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = if (isFromMe) 24.dp else 4.dp,
                    bottomEnd = if (isFromMe) 4.dp else 24.dp
                ),
                border = BorderStroke(0.5.dp, borderColor),
                tonalElevation = 2.dp,
                modifier = Modifier.widthIn(min = 200.dp, max = 260.dp)
            ) {
                Column {
                    if (message.replyToId != null) {
                        QuotedMessage(
                            replyToSenderName = message.replyToSenderName,
                            replyToContent = message.replyToContent,
                            appColors = appColors,
                            modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                } else {
                                    if (mediaPlayer == null && audioPath != null) {
                                        try {
                                            mediaPlayer = MediaPlayer().apply {
                                                setDataSource(audioPath)
                                                prepare()
                                                setOnCompletionListener {
                                                    isPlaying = false
                                                    currentPosition = 0
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    mediaPlayer?.start()
                                    isPlaying = true
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isFromMe) Color.White else Color(0xFF22D3EE)
                            )
                        }

                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            LinearProgressIndicator(
                                progress = { if (isPlaying && mediaPlayer != null && mediaPlayer!!.duration > 0) currentPosition.toFloat() / mediaPlayer!!.duration else 0f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = if (isFromMe) Color.White else Color(0xFF22D3EE),
                                trackColor = (if (isFromMe) Color.White else appColors.textSecondaryColor).copy(
                                    alpha = 0.2f
                                ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    durationText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor
                                )
                                Text(
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                        Date(
                                            timestamp
                                        )
                                    ),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor
                                )
                            }
                        }

                        Icon(
                            Icons.Default.Mic,
                            null,
                            tint = (if (isFromMe) Color.White else appColors.textSecondaryColor).copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SECCIÓN: OTROS COMPONENTES ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnifiedChatListItem(
    thread: ChatThread,
        unreadCount: Int,
        isSelected: Boolean,
        isMultiSelectMode: Boolean,
        onClick: () -> Unit,
        onLongClick: () -> Unit,
        onAvatarClick: () -> Unit
    ) {
        val provider = thread.provider
        val backgroundColor = if (isSelected) Color(0xFF1E293B) else Color.Transparent

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clickable { onAvatarClick() },
                        shape = CircleShape,
                        color = Color.Gray.copy(alpha = 0.2f)
                    ) {
                        val photoUrl = provider.photoUrl

                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.Gray.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    if (isMultiSelectMode) {
                        SelectionIndicator(
                            isSelected = isSelected,
                            modifier = Modifier.size(20.dp).offset(x = 4.dp, y = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val displayName = provider.displayName

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (provider.isVerified) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                null,
                                tint = Color(0xFF2197F5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (thread.lastMessage.isNotEmpty()) {
                        Text(
                            text = thread.lastMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                if (unreadCount > 0) {
                    Surface(
                        color = Color(0xFF2197F5),
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = unreadCount.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale *= zoomChange
            offset += offsetChange
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.95f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = if (scale > 1f) 1f else 2f
                                    offset = Offset.Zero
                                }
                            )
                        }
                ) {
                    AsyncImage(
                        model = message.imageUrl ?: message.content,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = maxOf(1f, scale),
                                scaleY = maxOf(1f, scale),
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = state),
                        contentScale = ContentScale.Fit
                    )

                    // Botón de cierre superior
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .statusBarsPadding()
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }
                }
            }
        }
    }


// --- SECCIÓN DE VISTAS PREVIAS (PREVIEWS) ---

@Preview(showBackground = true, name = "Mensajes de Texto")
@Composable
fun MessageBubblePreview() {
    val appColors = getThemeColors()
    val message = MessageEntity(
        id = "1",
        chatId = "c1",
        senderId = "p1",
        receiverId = "user1",
        type = MessageType.TEXT,
        content = "Hola, ¿cómo estás? Te envío el presupuesto solicitado para la reparación.",
        timestamp = System.currentTimeMillis()
    )
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MessageBubble(message = message, appColors = appColors, isFromMe = false)
            MessageBubble(message = message.copy(senderId = "user1", receiverId = "p1", content = "¡Hola! Perfecto, lo reviso ahora mismo."), appColors = appColors, isFromMe = true)
        }
    }
}

@Preview(showBackground = true, name = "Burbujas Multimedia")
@Composable
fun MediaBubblesPreview() {
    val appColors = getThemeColors()
    val now = System.currentTimeMillis()
    
    val imageMsg = MessageEntity(
        id = "m1",
        chatId = "c1",
        senderId = "u1",
        receiverId = "p1",
        type = MessageType.IMAGE,
        content = "Imagen adjunta",
        imageUrl = "https://picsum.photos/400/300",
        timestamp = now
    )
    
    val audioMsg = MessageEntity(
        id = "m2",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.AUDIO,
        content = "[Audio]",
        durationSeconds = 42,
        timestamp = now
    )
    
    val locationMsg = MessageEntity(
        id = "m3",
        chatId = "c1",
        senderId = "u1",
        receiverId = "p1",
        type = MessageType.LOCATION,
        content = "Ubicación compartida",
        latitude = -26.8241,
        longitude = -65.2226,
        locationAddress = "Av. Sarmiento 1100, San Miguel de Tucumán",
        timestamp = now
    )

    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImageMessageBubble(message = imageMsg, appColors = appColors, isFromMe = true, onImageClick = {})
            AudioMessageBubble(
                message = audioMsg,
                appColors = appColors, 
                isFromMe = false
            )
            LocationMessageBubble(message = locationMsg, appColors = appColors, isFromMe = true)
        }
    }
}

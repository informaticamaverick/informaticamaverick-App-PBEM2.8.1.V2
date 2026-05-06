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
import androidx.compose.foundation.gestures.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.local.BudgetEntity
import com.example.myapplication.data.local.BudgetItem
import com.example.myapplication.data.local.BudgetService
import com.example.myapplication.data.local.BudgetProfessionalFee
import com.example.myapplication.data.local.BudgetMiscExpense
import com.example.myapplication.data.local.BudgetTax
import com.example.myapplication.data.model.MessageType
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.client.ChatThread
import com.example.myapplication.presentation.util.ChatIdHelper
import com.example.myapplication.ui.theme.AppColors
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.getThemeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- HELPERS ---

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

@Composable
fun EnhancedMessageBubble(message: MessageEntity, isMe: Boolean, appColors: AppColors, senderPhotoUrl: String? = null) {
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)
    
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


@Composable
fun BudgetBubble(message: MessageEntity, budget: com.example.myapplication.data.local.BudgetEntity?, isMe: Boolean, appColors: AppColors, onClick: () -> Unit) {
    val orange = Color(0xFFFF6B35)
    val slateLight = Color(0xFFF8FAFC)
    val slateBorder = Color(0xFFE2E8F0)
    val slateText = Color(0xFF475569)
    val slateDark = Color(0xFF1E293B)
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 0.dp), 
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp, 
                    bottomStart = if (isMe) 16.dp else 2.dp, 
                    bottomEnd = if (isMe) 2.dp else 16.dp
                ))
                .background(Color.White)
                .border(0.5.dp, borderColor, RoundedCornerShape(
                    topStart = 16.dp, 
                    topEnd = 16.dp, 
                    bottomStart = if (isMe) 16.dp else 2.dp, 
                    bottomEnd = if (isMe) 2.dp else 16.dp
                ))
                .clickable { onClick() }
        ) {
            // Header naranja PREMIUM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(orange.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📋", fontSize = 18.sp)
                    Column {
                        Text(
                            text = budget?.providerCompanyName ?: "PRESUPUESTO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!budget?.category.isNullOrBlank()) {
                            Text(
                                text = budget!!.category!!.uppercase(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = budget?.budgetId?.takeLast(8) ?: (message.relatedId?.takeLast(8) ?: ""), 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Líneas de items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(slateLight)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (budget == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = orange
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Sincronizando...", fontSize = 11.sp, color = slateText)
                        }
                    }
                } else {
                    val allLines = mutableListOf<Pair<String, String>>()
                    budget.items.forEach { allLines.add(it.description to "$ ${String.format(Locale.US, "%,.2f", it.unitPrice * it.quantity)}") }
                    budget.services.forEach { allLines.add(it.description to "$ ${String.format(Locale.US, "%,.2f", it.total)}") }
                    
                    if (allLines.isEmpty()) {
                        Text("Detalles en el presupuesto formal", fontSize = 11.sp, color = slateText)
                    } else {
                        allLines.take(4).forEach { (desc, total) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    desc.take(22) + if (desc.length > 22) "…" else "",
                                    fontSize = 11.sp, color = slateDark,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(total, fontSize = 11.sp, color = slateDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (allLines.size > 4) {
                            Text("+ ${allLines.size - 4} ítems más…", fontSize = 10.sp, color = slateText)
                        }
                    }
                }
            }

            HorizontalDivider(color = slateBorder)

            // Footer con total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL", fontSize = 9.sp, color = slateText, fontWeight = FontWeight.Bold)
                    Text(
                        "$ ${String.format(Locale.US, "%,.2f", budget?.grandTotal ?: 0.0)}",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = orange
                    )
                }
                if ((budget?.validityDays ?: 0) > 0) {
                    Surface(color = orange.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "Válido ${budget?.validityDays} días",
                            fontSize = 9.sp, fontWeight = FontWeight.Bold, color = orange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Botón "Ver presupuesto"
            HorizontalDivider(color = slateBorder)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = orange),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("VER PRESUPUESTO", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = slateText.copy(alpha = 0.4f),
                    modifier = Modifier.padding(end = 8.dp)
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
    budget: com.example.myapplication.data.local.BudgetEntity? = null,
    onImageClick: () -> Unit = {},
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {},
    isEnabled: Boolean = true // 🔥 [NUEVO] Para habilitar/deshabilitar acciones
) {
    when (message.type) {
        MessageType.IMAGE -> ImageMessageBubble(message, appColors, isFromMe, onImageClick)
        MessageType.LOCATION -> LocationMessageBubble(message, appColors, isFromMe)
        MessageType.VISIT -> AppointmentBubble(message, isFromMe, appColors)
        MessageType.BUDGET -> BudgetBubble(message, budget, isFromMe, appColors, onClick = {})
        MessageType.BUDGET_REQUEST -> BudgetRequestBubble(message, isFromMe, appColors)
        MessageType.CALENDAR_INVITE -> CalendarInviteBubble(message, isFromMe, appColors, isEnabled, onCalendarClick)
        MessageType.AUDIO -> {
            val audioPath = if (message.content == "[Audio]" && !message.imageUrl.isNullOrBlank()) message.imageUrl else message.content
            AudioMessageBubble(
                audioPath = audioPath,
                duration = message.durationSeconds ?: 0, 
                timestamp = message.timestamp, 
                appColors = appColors, 
                isFromMe = isFromMe
            )
        }
        MessageType.APPOINTMENT_RECEIPT -> {
            if (message.receiptIsTechnician == true) {
                TechnicalVisitReceiptBubble(message, isFromMe, appColors, onCalendarClick, onAddressClick)
            } else {
                StandardAppointmentReceiptBubble(message, isFromMe, appColors, onCalendarClick, onAddressClick)
            }
        }
        else -> EnhancedMessageBubble(message, isFromMe, appColors)
    }
}

@Composable
fun ImageMessageBubble(message: MessageEntity, appColors: AppColors, isFromMe: Boolean, onImageClick: () -> Unit) {
    val model = remember(message.content, message.imageUrl) {
        val path = message.imageUrl
        when {
            !path.isNullOrBlank() -> if (path.startsWith("/")) "file://$path" else path
            message.content.startsWith("http") -> message.content
            else -> null
        }
    }
    val borderColor = if (isFromMe) Color(0xFF10B981) else Color(0xFF22D3EE)

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

@Composable
fun LocationMessageBubble(message: MessageEntity, appColors: AppColors, isFromMe: Boolean) {
    val context = LocalContext.current
    val lat = message.latitude ?: 0.0
    val lng = message.longitude ?: 0.0
    val emeraldColor = Color(0xFF10B981) // Esmeralda Maverick
    val borderColor = if (isFromMe) Color(0xFF10B981) else Color(0xFF22D3EE)
    
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

@Composable
fun AudioMessageBubble(
    audioPath: String?,
    duration: Int,
    timestamp: Long,
    appColors: AppColors,
    isFromMe: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
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
                                } catch (e: Exception) { e.printStackTrace() }
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
                        trackColor = (if (isFromMe) Color.White else appColors.textSecondaryColor).copy(alpha = 0.2f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(durationText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor)
                        Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp)), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isFromMe) Color.White.copy(alpha = 0.8f) else appColors.textSecondaryColor)
                    }
                }

                Icon(Icons.Default.Mic, null, tint = (if (isFromMe) Color.White else appColors.textSecondaryColor).copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun AppointmentBubble(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    providerPhotoUrl: String? = null,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val status = message.appointmentStatus ?: "PENDING"
    val isTechnicalVisit = message.appointmentType == "TECHNICAL_VISIT"
    
    // Identidad Visual Unificada
    val (headerColor, headerEmoji, headerTitle) = if (isTechnicalVisit) {
        Triple(Color(0xFF10B981), "🧰", "Propuesta de Visita")
    } else {
        Triple(Color(0xFF2197F5), "🗓️", "Propuesta de Turno")
    }

    val statusColor = when(status) {
        "ACCEPTED" -> Color(0xFF10B981)
        "REJECTED" -> Color(0xFFEF4444)
        else -> headerColor
    }
    
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 0.dp), 
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = appColors.surfaceColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (isMe) 16.dp else 2.dp, 
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column {
                // Header con Identidad Unificada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(headerEmoji, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = headerTitle, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White, 
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isMe && providerPhotoUrl != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                            AsyncImage(
                                model = providerPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Propuesta de prestador", fontSize = 11.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Card de Datos de la Cita
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = headerColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                // 🔥 FORMATEO dd/MM/yyyy
                                val displayDate = try {
                                    val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    message.appointmentDate?.let { dateStr ->
                                        if (dateStr.contains("-") && dateStr.length == 10) {
                                            inputFmt.parse(dateStr)?.let { outputFmt.format(it) }
                                        } else dateStr
                                    } ?: message.appointmentDate
                                } catch (e: Exception) { message.appointmentDate }
                                
                                Text(displayDate ?: "A convenir", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = headerColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(message.appointmentTime ?: "A convenir", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    if (message.content.isNotBlank() && !message.content.startsWith("Cita en:")) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = message.content, 
                            fontSize = 12.sp, 
                            color = appColors.textSecondaryColor, 
                            fontStyle = FontStyle.Italic,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    if (status == "PENDING" && !isMe && onAccept != null && onReject != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("ACEPTAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(38.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("RECHAZAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when(status) {
                                    "ACCEPTED" -> "✓ ACEPTADA"
                                    "REJECTED" -> "✕ RECHAZADA"
                                    else -> "⏳ PENDIENTE"
                                },
                                color = statusColor,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        modifier = Modifier.align(Alignment.End).padding(top = 8.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondaryColor.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarInviteBubble(
    message: MessageEntity, 
    isMe: Boolean, 
    appColors: AppColors, 
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val isTechnicalVisit = message.appointmentType == "TECHNICAL_VISIT"
    
    // Identidad Visual Unificada
    val (headerColor, headerEmoji, headerTitle) = if (isTechnicalVisit) {
        Triple(Color(0xFF10B981), "🧰", "Visita Técnica")
    } else {
        Triple(Color(0xFF2197F5), "🗓️", "Turno en Local")
    }
    
    val accentCyan = Color(0xFF22D3EE)
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 0.dp), 
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = appColors.surfaceColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (isMe) 16.dp else 2.dp, 
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.widthIn(max = 280.dp).then(
                if (isEnabled) Modifier.clickable { onClick() } else Modifier
            ),
            shadowElevation = 4.dp
        ) {
            Column {
                // Header con Identidad Unificada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(headerEmoji, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = headerTitle,
                            fontWeight = FontWeight.Black, 
                            color = Color.White, 
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isEnabled) "AGENDA DISPONIBLE" else "CITA AGENDADA",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEnabled) accentCyan else Color.Gray,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEnabled) {
                            if (isTechnicalVisit) 
                                "El prestador compartió su agenda para realizar la visita en tu domicilio." 
                                else "El prestador compartió su agenda para que reserves un turno en su local."
                        } else {
                            "Ya has seleccionado un horario para esta propuesta."
                        },
                        fontSize = 13.sp, 
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onClick,
                        enabled = isEnabled,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEnabled) headerColor else Color.Gray.copy(alpha = 0.2f),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isEnabled) {
                            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("ELEGIR DÍA Y HORA", fontSize = 12.sp, fontWeight = FontWeight.Black)
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Text("TURNO RESERVADO", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                        }
                    }
                    
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        modifier = Modifier.align(Alignment.End).padding(top = 10.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondaryColor.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPROBANTES DE TURNO (RECEIPTS)
// ==========================================

@Composable
fun TechnicalVisitReceiptBubble(
    message: MessageEntity, 
    isMe: Boolean, 
    appColors: AppColors,
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {}
) {
    val statusConfirmed = Color(0xFF10B981) // Verde Maverick
    val maverickBlue = Color(0xFF2197F5)
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)
    var showSecurityPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 0.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = appColors.surfaceColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (isMe) 16.dp else 2.dp, 
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 8.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column {
                // Header PREMIUM Visita Técnica (Verde)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(statusConfirmed.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🧰", fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "VISITA TÉCNICA CONFIRMADA",
                        fontWeight = FontWeight.Black,
                        color = statusConfirmed,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DETALLES VISITA TÉCNICA",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("Agendado en tu Calendario", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Tarjeta Fecha y Hora
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onCalendarClick() },
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FECHA", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                                // 🔥 FORMATEO dd/MM/yyyy o Respetar Formato del Prestador
                                val displayDate = remember(message.appointmentDate) {
                                    try {
                                        val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        message.appointmentDate?.let { dateStr ->
                                            if (dateStr.contains("-") && dateStr.length == 10) {
                                                inputFmt.parse(dateStr)?.let { outputFmt.format(it) }
                                            } else dateStr
                                        } ?: "--/--/--"
                                    } catch (e: Exception) { message.appointmentDate ?: "--/--/--" }
                                }
                                Text(displayDate, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HORA", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                                Text("${message.appointmentTime ?: "--:--"} HS", fontSize = 14.sp, color = maverickBlue, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val providerName = message.receiptProviderName ?: "El prestador"
                    Text(
                        text = "📍 $providerName se va a dirigir a esta dirección:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { message.receiptAddress?.let { onAddressClick(it) } },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, maverickBlue.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = maverickBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = message.receiptAddress ?: "Ubicación confirmada",
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Código de Validación
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CÓDIGO DE VALIDACIÓN", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                            Text(
                                text = message.receiptCode ?: "PENDIENTE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = statusConfirmed,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                        
                        IconButton(
                            onClick = { showSecurityPopup = true },
                            modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                        }
                    }

                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        modifier = Modifier.align(Alignment.End).padding(top = 12.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondaryColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }

    if (showSecurityPopup) {
        SecurityRecommendationsPopup(onDismiss = { showSecurityPopup = false })
    }
}

@Composable
fun StandardAppointmentReceiptBubble(
    message: MessageEntity, 
    isMe: Boolean, 
    appColors: AppColors,
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {}
) {
    val maverickBlue = Color(0xFF2197F5)
    val statusConfirmed = Color(0xFF10B981)
    val borderColor = if (isMe) Color(0xFF10B981) else Color(0xFF22D3EE)
    var showSecurityPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 0.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = appColors.surfaceColor,
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (isMe) 16.dp else 2.dp, 
                bottomEnd = if (isMe) 2.dp else 16.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 8.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column {
                // Header PREMIUM Turno Confirmado (Azul)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(maverickBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🗓️", fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "TURNO CONFIRMADO",
                        fontWeight = FontWeight.Black,
                        color = maverickBlue,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DETALLE DE TURNO",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("Agendado en tu Calendario", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onCalendarClick() },
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FECHA", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                                // 🔥 FORMATEO dd/MM/yyyy o Respetar Formato del Prestador
                                val displayDate = remember(message.appointmentDate) {
                                    try {
                                        val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        message.appointmentDate?.let { dateStr ->
                                            if (dateStr.contains("-") && dateStr.length == 10) {
                                                inputFmt.parse(dateStr)?.let { outputFmt.format(it) }
                                            } else dateStr
                                        } ?: "--/--/--"
                                    } catch (e: Exception) { message.appointmentDate ?: "--/--/--" }
                                }
                                Text(displayDate, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("HORA", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                                Text("${message.appointmentTime ?: "--:--"} HS", fontSize = 14.sp, color = maverickBlue, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    val providerName = message.receiptProviderName ?: "El prestador"
                    Text(
                        text = "🏢 $providerName te va a esperar en:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { message.receiptAddress?.let { onAddressClick(it) } },
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.5.dp, maverickBlue.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = maverickBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = message.receiptAddress ?: "Ubicación confirmada",
                                fontSize = 12.sp,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CÓDIGO DE VALIDACIÓN", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Black)
                            Text(
                                text = message.receiptCode ?: "PENDIENTE",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = statusConfirmed,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                        
                        IconButton(
                            onClick = { showSecurityPopup = true },
                            modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                        ) {
                            Text("⚠️", fontSize = 16.sp)
                        }
                    }

                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        modifier = Modifier.align(Alignment.End).padding(top = 12.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondaryColor.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }

    if (showSecurityPopup) {
        SecurityRecommendationsPopup(onDismiss = { showSecurityPopup = false })
    }
}

@Composable
fun SecurityRecommendationsPopup(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛡️", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Text("Seguridad Maverick", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SecurityItem("👤", "Verifica la identidad del prestador pidiendo su DNI al llegar.")
                SecurityItem("🔑", "No compartas el código de validación hasta que el trabajo haya comenzado o el prestador esté presente.")
                SecurityItem("🏠", "Si es una visita técnica, asegúrate de estar acompañado si es posible.")
                SecurityItem("📞", "Cualquier irregularidad, repórtala de inmediato a través del botón de ayuda.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2197F5))
            ) {
                Text("ENTENDIDO", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF1E293B),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun SecurityItem(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (dd/mm/aaaa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Hora (hh:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(date, time, notes) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
                            modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray.copy(alpha = 0.5f))
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
                        Icon(Icons.Default.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(16.dp))
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

@Preview(showBackground = true, name = "Mensajes de Texto y Presupuesto")
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
            
            val budget = BudgetEntity(
                budgetId = "b1",
                clientId = "u1",
                providerId = "p1",
                providerName = "Juan Perez",
                providerCompanyName = "Maverick Tech Solutions",
                category = "Refrigeración",
                items = listOf(
                    BudgetItem(description = "Compresor 1/4 HP", quantity = 1, unitPrice = 45000.0),
                    BudgetItem(description = "Carga de Gas R134", quantity = 1, unitPrice = 12000.0)
                ),
                services = listOf(
                    BudgetService(description = "Mano de obra especializada", total = 15000.0)
                ),
                subtotal = 72000.0,
                grandTotal = 87120.0,
                validityDays = 5,
                status = com.example.myapplication.data.local.BudgetStatus.PENDIENTE,
                dateTimestamp = System.currentTimeMillis()
            )
            BudgetBubble(message = message.copy(type = MessageType.BUDGET), budget = budget, isMe = false, appColors = appColors, onClick = {})
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
                audioPath = audioMsg.imageUrl, 
                duration = audioMsg.durationSeconds ?: 0, 
                timestamp = audioMsg.timestamp, 
                appColors = appColors, 
                isFromMe = false
            )
            LocationMessageBubble(message = locationMsg, appColors = appColors, isFromMe = true)
        }
    }
}

@Preview(showBackground = true, name = "Burbujas de Turnos y Citas")
@Composable
fun TurnoBubblesPreview() {
    val appColors = getThemeColors()
    val now = System.currentTimeMillis()
    
    val appointmentMsg = MessageEntity(
        id = "t1",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.VISIT,
        content = "Te propongo realizar la visita técnica este día.",
        appointmentDate = "2024-12-25",
        appointmentTime = "10:30",
        appointmentStatus = "PENDING",
        appointmentType = "TECHNICAL_VISIT",
        timestamp = now
    )
    
    val inviteMsg = MessageEntity(
        id = "t2",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.CALENDAR_INVITE,
        content = "Elegí el horario que mejor te quede.",
        appointmentType = "IN_STORE",
        timestamp = now
    )
    
    val receiptMsgVisita = MessageEntity(
        id = "t3",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.APPOINTMENT_RECEIPT,
        content = "¡Listo! Visita confirmada.",
        appointmentDate = "Mié 06/05/2026",
        appointmentTime = "10:00",
        receiptProviderName = "Maverick Refrigeración",
        receiptAddress = "B. Matienzo 1339",
        receiptCode = "#VIS-20260506-001",
        receiptIsTechnician = true,
        timestamp = now
    )
    
    val receiptMsgTurno = MessageEntity(
        id = "t4",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.APPOINTMENT_RECEIPT,
        content = "¡Listo! Turno confirmado.",
        appointmentDate = "Jue 07/05/2026",
        appointmentTime = "16:30",
        receiptProviderName = "Maverick Refrigeración",
        receiptAddress = "Calle Junín 450, Tucumán",
        receiptCode = "#TRN-20260507-001",
        receiptIsTechnician = false,
        timestamp = now
    )

    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AppointmentBubble(message = appointmentMsg, isMe = false, appColors = appColors, onAccept = {}, onReject = {})
            CalendarInviteBubble(message = inviteMsg, isMe = false, appColors = appColors, onClick = {})
            TechnicalVisitReceiptBubble(message = receiptMsgVisita, isMe = false, appColors = appColors)
            StandardAppointmentReceiptBubble(message = receiptMsgTurno, isMe = false, appColors = appColors)
        }
    }
}

package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getThemeColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImageMessageBubble(
    message: MessageEntity,
    appColors: AppColors,
    isMe: Boolean,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .clip(shape)
            .background(if (isMe) Color(0xFF1E293B) else Color(0xFF334155))
            .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color.Black.copy(alpha = 0.2f)
            ) {
                val imageSource = remember(message.imageUrl, message.imageLocalPath) {
                    ImageUtils.processImageSource(message.imageLocalPath ?: message.imageUrl)
                }
                
                if (imageSource != null) {
                    AsyncImage(
                        model = imageSource,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Este archivo ya no está en la nube (Ley #8)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Micro-timestamp overlay (WhatsApp Elite Style)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                val timeStr = remember(message.timestamp) {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                }
                Text(
                    text = timeStr,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (message.content.isNotEmpty() && message.content != "[Imagen]") {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun ImageMessageBubblePreview() {
    val appColors = getThemeColors()
    val message = MessageEntity(
        id = "img1",
        chatId = "c1",
        senderId = "user1",
        receiverId = "p1",
        type = MessageType.IMAGE,
        content = "Look at this image!",
        imageUrl = "https://example.com/image.jpg",
        timestamp = System.currentTimeMillis()
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ImageMessageBubble(
                message = message,
                appColors = appColors,
                isMe = true
            )
        }
    }
}

package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getThemeColors

@Composable
fun AudioMessageBubble(
    message: MessageEntity,
    appColors: AppColors,
    isMe: Boolean,
    onPlay: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val isAvailable = remember(message.audioLocalPath, message.imageUrl) {
        val path = message.audioLocalPath ?: message.imageUrl
        path != null && (path.startsWith("/") || path.startsWith("http") || path.length > 500)
    }

    Box(
        modifier = Modifier
            .width(240.dp)
            .clip(shape)
            .background(if (isMe) Color(0xFF1E293B) else Color(0xFF334155))
            .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
            .padding(8.dp)
    ) {
        if (!isAvailable) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Audio no disponible (Ley #8)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play Button with circular progress (WhatsApp Elite)
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(42.dp),
                        color = Color(0xFF22D3EE),
                        strokeWidth = 2.dp,
                        trackColor = Color.White.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    IconButton(
                        onClick = { 
                            isPlaying = !isPlaying 
                            onPlay()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    // Waveform Placeholder (Elite visual)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(15) { index ->
                            val height = remember { (4..12).random().dp }
                            val color = if (progress > (index / 15f)) Color(0xFF22D3EE) else Color.Gray.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(height)
                                    .background(color, CircleShape)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val duration = message.durationSeconds ?: 0
                        val minutes = duration / 60
                        val seconds = duration % 60
                        Text(
                            text = "$minutes:${seconds.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color.Gray
                        )
                        Icon(
                            imageVector = Icons.Default.Mic, 
                            contentDescription = null, 
                            tint = Color.Gray, 
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun AudioMessageBubblePreview() {
    val appColors = getThemeColors()
    val message = MessageEntity(
        id = "audio1",
        chatId = "c1",
        senderId = "user1",
        receiverId = "p1",
        type = MessageType.AUDIO,
        content = "",
        durationSeconds = 45,
        timestamp = System.currentTimeMillis()
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            AudioMessageBubble(
                message = message,
                appColors = appColors,
                isMe = true
            )
        }
    }
}

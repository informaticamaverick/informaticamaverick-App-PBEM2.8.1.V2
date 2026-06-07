package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getThemeColors

@Composable
fun LocationMessageBubble(
    message: MessageEntity,
    appColors: AppColors,
    isMe: Boolean,
    onClick: () -> Unit = {}
) {
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .width(240.dp)
            .clip(shape)
            .background(if (isMe) Color(0xFF1E293B) else Color(0xFF334155))
            .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        // Mapa Static Preview (WhatsApp Style)
        val lat = message.latitude ?: 0.0
        val lng = message.longitude ?: 0.0
        val zoom = 15
        val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?center=$lat,$lng&zoom=$zoom&size=400x250&markers=color:red%7C$lat,$lng&key=YOUR_API_KEY_HERE"
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray)
        ) {
            AsyncImage(
                model = staticMapUrl,
                contentDescription = "Vista del mapa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Icono central decorativo si la imagen falla o carga
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                tint = Color(0xFF22D3EE).copy(alpha = 0.8f)
            )
        }

        Spacer(Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn, 
                contentDescription = null, 
                tint = Color(0xFF22D3EE), 
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    text = "Ubicación compartida",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF22D3EE)
                )
                Text(
                    text = message.locationAddress ?: "Sin dirección",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LocationMessageBubblePreview() {
    val appColors = getThemeColors()
    val message = MessageEntity(
        id = "loc1",
        chatId = "c1",
        senderId = "user1",
        receiverId = "p1",
        type = MessageType.LOCATION,
        content = "",
        locationAddress = "Av. Siempre Viva 742",
        timestamp = System.currentTimeMillis()
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LocationMessageBubble(
                message = message,
                appColors = appColors,
                isMe = true
            )
        }
    }
}

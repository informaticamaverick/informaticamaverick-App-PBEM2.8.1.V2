package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- RASTREADOR DE ACCIONES PENDIENTES (V2026.7) ---
 * [ELITE]: Botón flotante para navegación rápida a tareas sin resolver en el chat.
 */
@Composable
fun RastreadorAccionesChatMav(
    cantidad: Int,
    alHacerClick: () -> Unit,
    colorAcento: Color
) {
    Surface(
        modifier = Modifier
            .padding(top = 80.dp, end = 16.dp)
            .height(40.dp)
            .clickable { alHacerClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorAcento.copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(colorAcento, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cantidad.toString(),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "ACCIONES PENDIENTES",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = colorAcento,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


































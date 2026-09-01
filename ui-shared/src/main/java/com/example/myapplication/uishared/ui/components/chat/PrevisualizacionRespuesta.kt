package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.datos.local.entidades.TipoMensaje

/**
 * --- BARRA DE PREVISUALIZACIÓN DE RESPUESTA MAVERICK (V2026.7) ---
 */
@Composable
fun PrevisualizacionRespuesta(
    mensaje: MensajeEntity,
    alCancelar: () -> Unit,
    colorAcento: Color,
    colorFondo: Color,
    esMio: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(),
        color = colorFondo.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(colorAcento)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = if (esMio) "Tú" else "Prestador",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = colorAcento
                )
                Text(
                    text = when (mensaje.tipo) {
                        TipoMensaje.IMAGEN -> "📷 Imagen"
                        TipoMensaje.AUDIO -> "🎵 Nota de voz"
                        TipoMensaje.UBICACION -> "📍 Ubicación"
                        TipoMensaje.PRESUPUESTO -> "💰 Presupuesto"
                        else -> mensaje.contenido
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = alCancelar) {
                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}



































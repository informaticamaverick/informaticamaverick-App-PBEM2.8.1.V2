package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE AUDIO MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaAudio(
    duracion: String,
    reproduciendo: Boolean,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    progreso: Float = 0f,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    alHacerClickPlay: () -> Unit = {},
    alHacerSwipeRespuesta: (() -> Unit)? = null // 🔥 [NEW]
) {
    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondo,
        colorContenido = colorContenido,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado,
        nombreRespuesta = nombreRespuesta,
        contenidoRespuesta = contenidoRespuesta,
        alHacerSwipeRespuesta = alHacerSwipeRespuesta
    ) {
        Row(
            modifier = Modifier
                .width(230.dp)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorContenido.copy(alpha = 0.1f))
                    .clickable { alHacerClickPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (reproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (esMio) Color(0xFF4FC3F7) else colorContenido,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                WaveformElite(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    color = if (esMio) Color(0xFF4FC3F7) else colorContenido,
                    progreso = progreso
                )
                
                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = duracion,
                    fontSize = 11.sp,
                    color = colorContenido.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WaveformElite(modifier: Modifier, color: Color, progreso: Float) {
    Canvas(modifier = modifier) {
        val ancho = size.width
        val alto = size.height
        val anchoBarra = 2.dp.toPx()
        val espacio = 1.5.dp.toPx()
        val cantidad = (ancho / (anchoBarra + espacio)).toInt()

        for (i in 0 until cantidad) {
            val factor = when (i % 8) {
                0 -> 0.3f; 1 -> 0.7f; 2 -> 0.5f; 3 -> 0.9f; 4 -> 0.4f; 5 -> 0.8f; 6 -> 0.6f; else -> 0.2f
            }
            val altoBarra = alto * factor
            val x = i * (anchoBarra + espacio)
            val yInicio = (alto - altoBarra) / 2
            
            val resaltado = (i.toFloat() / cantidad) < progreso

            drawLine(
                color = color.copy(alpha = if (resaltado) 1f else 0.3f),
                start = Offset(x, yInicio),
                end = Offset(x, yInicio + altoBarra),
                strokeWidth = anchoBarra,
                cap = StrokeCap.Round
            )
        }
    }
}


































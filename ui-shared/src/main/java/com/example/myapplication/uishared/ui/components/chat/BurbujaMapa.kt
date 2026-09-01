package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE MAPA MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaMapa(
    latitud: Double,
    longitud: Double,
    direccion: String?,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    alHacerClick: (() -> Unit)? = null,
    alHacerClickHerramienta: (() -> Unit)? = null
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
        alHacerClick = alHacerClick,
        margenInterno = PaddingValues(0.dp),
        contenidoExtra = {
            // --- 🚀 BADGE TÁCTICO (Estilo Telegram) ---
            if (!esMio && alHacerClickHerramienta != null) {
                Surface(
                    onClick = alHacerClickHerramienta,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🧰", fontSize = 18.sp)
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Ubicación compartida",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = colorContenido
                )
                if (direccion != null) {
                    Text(
                        text = direccion,
                        fontSize = 12.sp,
                        color = colorContenido.copy(alpha = 0.7f),
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}


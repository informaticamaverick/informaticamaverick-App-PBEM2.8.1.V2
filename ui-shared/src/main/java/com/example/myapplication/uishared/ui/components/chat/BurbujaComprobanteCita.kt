package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE COMPROBANTE DE CITA MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaComprobanteCita(
    titulo: String,
    fecha: String?,
    hora: String?,
    direccion: String?,
    codigo: String?,
    idCategoria: String? = null, // 🔥 [ELITE]: Clave Semántica
    nombreCategoria: String? = null,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    alHacerSwipeRespuesta: (() -> Unit)? = null
) {
    val colorAcento = Color(0xFF34D399) 
    val colorFondoBurbuja = Color(0xFF064E3B) 
    val colorFondoCabecera = Color(0xFF04382A) 
    
    val emoji = if (titulo.contains("Visita", ignoreCase = true)) "🧰" else "🗓️"

    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondoBurbuja,
        colorContenido = Color.White,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado,
        alHacerSwipeRespuesta = alHacerSwipeRespuesta,
        margenInterno = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.width(280.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorFondoCabecera)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = nombreCategoria?.uppercase() ?: titulo.uppercase(), 
                            fontWeight = FontWeight.Black, 
                            fontSize = 15.sp, 
                            color = colorAcento
                        )
                        Text(
                            "CITA CONFIRMADA", 
                            fontSize = 9.sp, 
                            color = colorAcento.copy(alpha = 0.7f), 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DatoComprobanteItem("FECHA", fecha ?: "--", Color.White)
                        Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
                        DatoComprobanteItem("HORA", hora ?: "--", Color.White)
                    }
                }

                if (!direccion.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = colorAcento.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(direccion, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                if (!codigo.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    SeparadorTicketComprobante(Color.White)
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CÓDIGO DE VERIFICACIÓN", 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = codigo, 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.ExtraBold, 
                            color = colorAcento,
                            letterSpacing = 6.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeparadorTicketComprobante(color: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().height(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                pathEffect = pathEffect,
                strokeWidth = 1.dp.toPx()
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(14.dp).offset(x = (-12).dp).background(Color.Black.copy(alpha = 0.15f), CircleShape))
            Box(modifier = Modifier.size(14.dp).offset(x = (12).dp).background(Color.Black.copy(alpha = 0.15f), CircleShape))
        }
    }
}

@Composable
private fun DatoComprobanteItem(label: String, value: String, contentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 8.sp, color = contentColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, color = contentColor, fontWeight = FontWeight.Black)
    }
}


































package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
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

/**
 * --- BURBUJA DE PRESUPUESTO MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaPresupuesto(
    titulo: String,
    total: String,
    estado: String,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    nombreCategoria: String? = null,
    alHacerClick: (() -> Unit)? = null,
    alHacerSwipeRespuesta: (() -> Unit)? = null // 🔥 [NEW]
) {
    val colorFondoBurbuja = Color(0xFF0D1418)
    val colorAcento = Color(0xFFF97316) 
    val colorFondoCabecera = Color(0xFF2C1C16) 
    
    val tituloFinal = "PRESUPUESTO ELITE"
    val subtituloFinal = nombreCategoria?.uppercase() ?: "PROPUESTA COMERCIAL"

    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = if (esMio) colorFondo else colorFondoBurbuja,
        colorContenido = Color.White,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado,
        nombreRespuesta = nombreRespuesta,
        contenidoRespuesta = contenidoRespuesta,
        alHacerSwipeRespuesta = alHacerSwipeRespuesta,
        margenInterno = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clickable { alHacerClick?.invoke() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(colorFondoCabecera.copy(alpha = 0.9f), colorFondoCabecera)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, null, tint = colorAcento, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tituloFinal, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 15.sp, 
                            color = colorAcento,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = subtituloFinal, 
                            fontSize = 10.sp, 
                            color = Color.White.copy(alpha = 0.6f), 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = titulo, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 13.sp, 
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = total, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 22.sp, 
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                            
                            Surface(
                                color = colorAcento.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = estado.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colorAcento
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { alHacerClick?.invoke() },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorAcento),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("VER DETALLES", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}


































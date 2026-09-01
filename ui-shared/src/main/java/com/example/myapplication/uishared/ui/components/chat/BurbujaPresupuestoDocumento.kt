package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE PRESUPUESTO ESTILO DOCUMENTO (v2026.ELITE — Opción C) ---
 * PROPÓSITO: Mismo lenguaje "Neon Glow" que Turno/Visita, con acento de color según
 * el estado del presupuesto (pendiente/aceptado/rechazado) y colita tipo burbuja de chat.
 */
@Composable
fun BurbujaPresupuestoDocumento(
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
    miniaturaBase64: String? = null,
    alVer: () -> Unit = {},
    alGuardar: () -> Unit = {},
    alHacerSwipeRespuesta: (() -> Unit)? = null
) {
    val colorFondoBurbuja = Color(0xFF07060D)
    val estadoUpper = estado.uppercase()

    val (colorAcento, emoji, textoEstado) = when (estadoUpper) {
        "ACEPTADO", "ACCEPTED", "CONFIRMADO" -> Triple(Color(0xFF10B981), "✅", "ACEPTADO")
        "RECHAZADO", "REJECTED", "CANCELADO" -> Triple(Color(0xFFF43F5E), "❌", "RECHAZADO")
        else -> Triple(Color(0xFFF59E0B), "⏳", "PENDIENTE")
    }

    val formaExterior = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (esMio) 20.dp else 6.dp,
        bottomEnd = if (esMio) 6.dp else 20.dp
    )
    val formaInterior = RoundedCornerShape(
        topStart = 18.5.dp,
        topEnd = 18.5.dp,
        bottomStart = if (esMio) 18.5.dp else 5.dp,
        bottomEnd = if (esMio) 5.dp else 18.5.dp
    )

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
        Box(modifier = Modifier.width(266.dp)) {

            Box(
                modifier = Modifier
                    .align(if (esMio) Alignment.TopStart else Alignment.TopEnd)
                    .width(260.dp)
                    .clip(formaExterior)
                    .background(colorAcento.copy(alpha = 0.5f))
                    .padding(1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(formaInterior)
                        .background(Brush.verticalGradient(listOf(Color(0xFF0B0A14), colorFondoBurbuja)))
                ) {

                    // --- HEADER: ícono de estado + título + badge de estado ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colorAcento.copy(alpha = 0.1f))
                                .border(0.5.dp, colorAcento.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PRESUPUESTO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                            Text(
                                text = titulo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = textoEstado,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = colorAcento,
                            modifier = Modifier
                                .background(colorAcento.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.06f))
                    )

                    // --- MONTO ---
                    Column(
                        modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "MONTO",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                        Text(
                            text = total,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    DashedDivider(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    // --- ACCIONES: VER / GUARDAR ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = alVer,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Visibility, null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("VER", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = alGuardar,
                            modifier = Modifier.weight(1.3f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
                        ) {
                            Icon(Icons.Default.Download, null, tint = Color.Black, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("GUARDAR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }
            }

            // --- COLITA: mismo mecanismo visual que ya tienen las burbujas de texto simples ---
            Canvas(
                modifier = Modifier
                    .align(if (esMio) Alignment.BottomEnd else Alignment.BottomStart)
                    .size(width = 10.dp, height = 8.dp)
            ) {
                val camino = Path().apply {
                    if (esMio) {
                        moveTo(0f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                    } else {
                        moveTo(size.width, 0f)
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height)
                    }
                    close()
                }
                drawPath(path = camino, color = colorAcento.copy(alpha = 0.5f))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020617)
@Composable
private fun PreviewBurbujaPresupuestoDocumento() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BurbujaPresupuestoDocumento(
            titulo = "Reparación instalación de gas",
            total = "$ 15.000,00",
            estado = "PENDIENTE",
            esMio = true,
            marcaTiempo = System.currentTimeMillis(),
            colorFondo = Color(0xFFF97316),
            colorContenido = Color.White
        )

        BurbujaPresupuestoDocumento(
            titulo = "Servicio de pintura completo",
            total = "$ 45.000,00",
            estado = "ACEPTADO",
            esMio = false,
            marcaTiempo = System.currentTimeMillis(),
            colorFondo = Color(0xFF22D3EE),
            colorContenido = Color.White
        )
    }
}

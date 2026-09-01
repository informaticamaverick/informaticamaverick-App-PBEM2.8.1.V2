package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.core.datos.local.entidades.MensajeEntity

/**
 * --- BURBUJA DE CITA ELITE (V2026.7) ---
 * Unifica Turnos, Citas y Visitas Técnicas.
 */
@Composable
fun BurbujaCitaElite(
    tipo: TipoCitaElite,
    estado: String = "PENDIENTE",
    fecha: String? = null,
    hora: String? = null,
    direccion: String? = null,
    idCategoria: String? = null, 
    nombreCategoria: String? = null,
    codigoVerificacion: String? = null,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    alAceptar: () -> Unit = {},
    alRechazar: () -> Unit = {},
    alVerCalendario: () -> Unit = {},
    alHacerClickCuerpo: () -> Unit = {},
    alHacerSwipeRespuesta: (() -> Unit)? = null,
    nombreRecurso: String? = null,
    nombreProfesional: String? = null,
    equipoTrabajo: List<String>? = null,
    motivoRechazo: String? = null
) {
    val (colorAcento, colorFondoBurbuja, colorFondoCabecera, titulo, subLabel, emoji) = when (tipo) {
        TipoCitaElite.LOCAL -> Sextuple(
            Color(0xFFA78BFA), 
            Color(0xFF0D1418), 
            Color(0xFF231B32), 
            "TURNO EN LOCAL", 
            "ATENCIÓN EN ESTABLECIMIENTO", 
            "🗓️"
        )
        TipoCitaElite.VISITA_TECNICA -> Sextuple(
            Color(0xFF00E5FF), 
            Color(0xFF0D1418), 
            Color(0xFF16272C), 
            "VISITA TÉCNICA", 
            "PROPUESTA DE SERVICIO", 
            "🧰"
        )
        TipoCitaElite.ENVIO -> Sextuple(
            Color(0xFFFACC15), 
            Color(0xFF0D1418), 
            Color(0xFF282315), 
            "ENVÍO DE PRODUCTO", 
            "LOGÍSTICA Y DESPACHO", 
            "📦"
        )
    }

    val esCerrado = fecha != null && hora != null
    val esPendiente = estado == "PENDIENTE" || estado == "PENDING"
    val esAceptado = estado == "ACEPTADO" || estado == "ACCEPTED"
    
    val tituloFinal = titulo.uppercase()
    val subtituloFinal = buildString {
        append(nombreCategoria?.uppercase() ?: "SERVICIO")
        append(" · ")
        append(if (esCerrado) "TURNO FIJO" else "TURNO ABIERTO")
    }
    
    val colorFondoEfectivo = if (esPendiente || esAceptado) colorFondoBurbuja else colorFondo
    val colorContenidoEfectivo = if (colorFondoEfectivo != colorFondo) Color.White else colorContenido

    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondoEfectivo,
        colorContenido = colorContenidoEfectivo,
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
                .clickable { alHacerClickCuerpo() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colorFondoCabecera.copy(alpha = 0.8f),
                                colorFondoCabecera
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tituloFinal, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 15.sp, 
                            color = colorAcento,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtituloFinal, 
                            fontSize = 9.sp, 
                            color = colorContenidoEfectivo.copy(alpha = 0.5f), 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    if (!esPendiente) {
                        Surface(
                            color = obtenerColorEstado(estado).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = obtenerTextoEstado(estado).uppercase(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = obtenerColorEstado(estado)
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FECHA", fontSize = 8.sp, color = colorContenidoEfectivo.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            Text(
                                text = fecha ?: "PENDIENTE", 
                                fontSize = 14.sp, 
                                color = colorContenidoEfectivo, 
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = if (fecha == null) FontStyle.Italic else FontStyle.Normal
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(colorContenidoEfectivo.copy(alpha = 0.1f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HORA", fontSize = 8.sp, color = colorContenidoEfectivo.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            Text(
                                text = hora ?: "--:--", 
                                fontSize = 14.sp, 
                                color = colorContenidoEfectivo, 
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = if (hora == null) FontStyle.Italic else FontStyle.Normal
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(10.dp))

                val direccionAMostrar = if (direccion.isNullOrBlank() || direccion.contains("convenir", ignoreCase = true)) {
                    if (tipo == TipoCitaElite.LOCAL) "Dirección del Local" else "A convenir con el cliente"
                } else direccion
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = colorAcento, modifier = Modifier.size(18.dp))
                        Column {
                            Text("DIRECCIÓN", fontSize = 8.sp, color = colorContenidoEfectivo.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                            Text(
                                text = direccionAMostrar, 
                                fontSize = 11.sp, 
                                color = colorContenidoEfectivo, 
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (nombreRecurso != null || equipoTrabajo?.isNotEmpty() == true || nombreProfesional != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.03f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            nombreRecurso?.let {
                                FilaDatoCita(Icons.Default.Dashboard, "RECURSO", it, colorContenidoEfectivo)
                            }
                            
                            equipoTrabajo?.let { team ->
                                if (team.isNotEmpty()) {
                                    if (nombreRecurso != null) Spacer(Modifier.height(8.dp))
                                    FilaDatoCita(Icons.Default.Groups, "EQUIPO", team.joinToString(", "), colorContenidoEfectivo)
                                }
                            } ?: nombreProfesional?.let {
                                if (nombreRecurso != null) Spacer(Modifier.height(8.dp))
                                FilaDatoCita(Icons.Default.Badge, "RESPONSABLE", it, colorContenidoEfectivo)
                            }
                        }
                    }
                }

                if ((estado == "RECHAZADO" || estado == "REJECTED") && motivoRechazo != null) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Red.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "MOTIVO: $motivoRechazo",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 10.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!esMio && esPendiente) {
                    Spacer(Modifier.height(16.dp))
                    if (esCerrado) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = alAceptar,
                                modifier = Modifier.weight(1.2f).height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorAcento),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (tipo == TipoCitaElite.LOCAL) "RESERVAR" else "ACEPTAR", 
                                    fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                            OutlinedButton(
                                onClick = alRechazar,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorContenidoEfectivo),
                                border = BorderStroke(1.5.dp, colorContenidoEfectivo.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("RECHAZAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        Button(
                            onClick = alVerCalendario,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorAcento),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("VER CALENDARIO", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                }

                if (codigoVerificacion != null && esAceptado) {
                    Spacer(Modifier.height(12.dp))
                    SeparadorTicket(colorContenidoEfectivo)
                    
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("CÓDIGO DE SEGURIDAD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colorContenidoEfectivo.copy(alpha = 0.4f))
                        Text(text = codigoVerificacion, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = colorAcento, letterSpacing = 6.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeparadorTicket(color: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().height(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            drawLine(color = color.copy(alpha = 0.3f), start = Offset(0f, 0f), end = Offset(size.width, 0f), pathEffect = pathEffect, strokeWidth = 1.dp.toPx())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(12.dp).offset(x = (-10).dp).background(Color.Black.copy(alpha = 0.1f), CircleShape))
            Box(modifier = Modifier.size(12.dp).offset(x = (10).dp).background(Color.Black.copy(alpha = 0.1f), CircleShape))
        }
    }
}

@Composable
private fun FilaDatoCita(icon: ImageVector, etiqueta: String, valor: String, colorContenido: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = colorContenido.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
        Column {
            Text(etiqueta, fontSize = 8.sp, color = colorContenido.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            Text(text = valor, fontSize = 12.sp, color = colorContenido, fontWeight = FontWeight.Medium)
        }
    }
}

private fun obtenerColorEstado(estado: String) = when (estado) {
    "ACCEPTED", "ACEPTADO" -> Color(0xFF10B981)
    "REJECTED", "RECHAZADO" -> Color(0xFFEF4444)
    "COMPLETED", "COMPLETADO" -> Color(0xFF3B82F6)
    "CANCELLED", "CANCELADO" -> Color(0xFF6B7280)
    else -> Color.Gray
}

private fun obtenerTextoEstado(estado: String) = when (estado) {
    "ACCEPTED", "ACEPTADO" -> "Confirmado"
    "REJECTED", "RECHAZADO" -> "Rechazado"
    "COMPLETED", "COMPLETADO" -> "Finalizado"
    "CANCELLED", "CANCELADO" -> "Cancelado"
    else -> estado
}

enum class TipoCitaElite {
    LOCAL, VISITA_TECNICA, ENVIO
}

private data class Sextuple<A, B, C, D, E, F>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F)

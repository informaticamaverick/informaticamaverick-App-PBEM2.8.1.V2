package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette
import java.util.*

/**
 * --- BURBUJA DE TURNO EN LOCAL (ELITE v2026.FINAL) ---
 */
@Composable
fun BurbujaTurnoLocal(
    estado: String = "PENDIENTE",
    fecha: String?,
    hora: String?,
    direccion: String?,
    idCategoria: String? = null, 
    nombreCategoria: String? = null,
    iconoCategoria: String? = "🔧",
    codigoVerificacion: String? = null,
    idRecurso: String? = null,
    nombreRecurso: String? = null,
    detalleRecurso: String? = null,
    precioRecurso: Double? = null,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    esAgendaAbierta: Boolean = false,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    alAceptar: () -> Unit = {},
    alRechazar: () -> Unit = {},
    alVerCalendario: () -> Unit = {},
    alHacerClickCuerpo: () -> Unit = {},
    alHacerClickMapa: (String) -> Unit = {},
    alHacerSwipeRespuesta: (() -> Unit)? = null
) {
    val colorAcento = Color(0xFFA855F7) 
    val colorFondoCabecera = Color(0xFF231B32)
    val colorFondoBurbuja = Color(0xFF0D1418) 

    val estadoUpper = estado.uppercase()
    val esConfirmado = estadoUpper == "CONFIRMADO" || estadoUpper == "ACEPTADO" || estadoUpper == "ACCEPTED"

    val colorFondoEfectivo = colorFondoBurbuja
    val colorContenidoEfectivo = Color.White

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
        margenInterno = PaddingValues(0.dp),
        contenidoExtra = {
            Column(
                horizontalAlignment = if (esMio) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndicadorEstadoExterno(estado = estadoUpper, esMio = esMio)
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable { alVerCalendario() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Herramientas",
                        tint = colorAcento,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(colorFondoCabecera.copy(alpha = 0.95f), colorFondoCabecera)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🗓️", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TARJETA DE TURNO",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = colorAcento,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "#" + (codigoVerificacion ?: "---"),
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = nombreCategoria?.uppercase() ?: "SERVICIO EN LOCAL",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val (mesTag, diaNum) = remember(fecha) { parseFecha(fecha) }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorAcento.copy(alpha = 0.1f))
                                    .border(1.dp, colorAcento.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = mesTag, fontSize = 7.sp, fontWeight = FontWeight.Black, color = colorAcento)
                                    Text(text = diaNum, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("FECHA", fontSize = 7.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                Text(fecha ?: "TBD", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("HORARIO", fontSize = 7.sp, color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                Text(hora ?: "--:--", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1)
                            }
                        }
                    }
                }

                Surface(
                    onClick = { if (!direccion.isNullOrBlank()) alHacerClickMapa("geo:0,0?q=" + direccion) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF020617).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(colorAcento.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Place, null, tint = colorAcento, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DIRECCIÓN DEL ESTABLECIMIENTO",
                                fontSize = 8.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = direccion ?: "Consultar al prestador",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = colorAcento.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("DETALLES DEL SERVICIO / RECURSO", fontSize = 7.sp, color = Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.Black)
                            Text(
                                text = nombreRecurso ?: "GENERAL",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (!detalleRecurso.isNullOrBlank()) {
                                Text(detalleRecurso, fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (precioRecurso != null && precioRecurso > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("COSTO ESTIMADO", fontSize = 7.sp, color = Color(0xFF10B981).copy(alpha = 0.6f), fontWeight = FontWeight.Black)
                                Text(
                                    text = "$ " + String.format(Locale.getDefault(), "%,.0f", precioRecurso),
                                    fontSize = 14.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                DashedDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                if (!esMio && (estadoUpper == "PENDIENTE" || estadoUpper == "PENDING")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = alRechazar,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("RECHAZAR", fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        }
                        Button(
                            onClick = alAceptar,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
                        ) {
                            Text(
                                text = if (esAgendaAbierta) "ELEGIR HORARIO" else "CONFIRMAR CITA", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black, 
                                color = Color.Black, 
                                maxLines = 1
                            )
                        }
                    }
                } else if (esConfirmado) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = codigoVerificacion ?: "CONF-0000",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = colorAcento,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "PRESENTA ESTE CÓDIGO AL LLEGAR AL LOCAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (esMio && (estadoUpper == "PENDIENTE" || estadoUpper == "PENDING")) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colorAcento.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, null, tint = colorAcento, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "ESPERANDO CONFIRMACIÓN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colorAcento,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxWidth().height(thickness)) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            strokeWidth = thickness.toPx()
        )
    }
}

@Composable
private fun IndicadorEstadoExterno(estado: String, esMio: Boolean) {
    val (color, texto, emoji) = when (estado.uppercase()) {
        "PENDIENTE", "PENDING" -> Triple(Color(0xFFF59E0B), "PENDIENTE", "⏳")
        "CONFIRMADO", "ACEPTADO", "ACCEPTED" -> Triple(Color(0xFF10B981), "CONFIRMADO", "✅")
        "CANCELADO", "RECHAZADO", "REJECTED" -> Triple(Color(0xFFF43F5E), "CANCELADO", "❌")
        else -> Triple(Color.Gray, estado, "🔹")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (esMio) {
            Text(
                text = texto,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = color,
                modifier = Modifier
                    .background(color.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f))
                .border(0.5.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 14.sp)
        }

        if (!esMio) {
            Text(
                text = texto,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = color,
                modifier = Modifier
                    .background(color.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

private fun parseFecha(fecha: String?): Pair<String, String> {
    if (fecha.isNullOrBlank()) return "---" to "--"
    
    if (fecha.contains("-") && fecha.length >= 10) {
        try {
            val parts = fecha.split("-")
            val dia = parts[2]
            val mesInt = parts[1].toIntOrNull() ?: 1
            val meses = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
            return meses.getOrElse(mesInt - 1) { "MES" } to dia
        } catch (e: Exception) { }
    }
    
    val parts = fecha.split(" ")
    if (parts.size >= 2) {
        val p1 = parts[0]
        val p2 = parts[1]
        if (p1.any { it.isLetter() }) return p1.uppercase().take(3) to p2
        return p2.uppercase().take(3) to p1
    }
    
    return fecha.take(3).uppercase() to fecha.filter { it.isDigit() }.take(2).ifBlank { "--" }
}

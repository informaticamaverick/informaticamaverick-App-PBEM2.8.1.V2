package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.myapplication.core.datos.local.entidades.TipoAvisoEstado

/**
 * --- BURBUJA DE AVISO DE ESTADO MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaAvisoEstado(
    tipoEstado: TipoAvisoEstado,
    mensaje: String,
    motivo: String? = null,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true
) {
    val (icono, colorAcento, titulo) = when (tipoEstado) {
        TipoAvisoEstado.COMPLETADO -> Triple(Icons.Default.CheckCircle, Color(0xFF10B981), "TRABAJO FINALIZADO")
        TipoAvisoEstado.CANCELADO -> Triple(Icons.Default.Cancel, Color(0xFFEF4444), "TURNO CANCELADO")
        TipoAvisoEstado.REPROGRAMADO -> Triple(Icons.Default.Info, Color(0xFFF97316), "REPROGRAMACIÓN")
    }

    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondo,
        colorContenido = colorContenido,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado
    ) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .padding(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(titulo, fontWeight = FontWeight.Black, fontSize = 12.sp, color = colorAcento)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensaje,
                fontSize = 13.sp,
                color = colorContenido,
                lineHeight = 18.sp
            )
            if (!motivo.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = colorAcento.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Motivo: $motivo",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 11.sp,
                        color = colorContenido.copy(alpha = 0.8f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}


































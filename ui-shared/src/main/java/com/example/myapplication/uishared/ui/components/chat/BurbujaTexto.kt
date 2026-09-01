package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ID_PLACEHOLDER_HORA = "hora_ticks"

/**
 * --- BURBUJA DE TEXTO MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaTexto(
    texto: String,
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
    alHacerSwipeRespuesta: (() -> Unit)? = null
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
        alHacerSwipeRespuesta = alHacerSwipeRespuesta,
        // [WHATSAPP-STYLE] la hora/ticks se arman ACÁ ADENTRO, incrustados al final del texto
        // (no en una fila propia debajo) — así la burbuja se ajusta al ancho real del texto en
        // vez de ensancharse o dejar un renglón vacío solo para la hora.
        mostrarFilaHora = false
    ) {
        val horaTexto = remember(marcaTiempo) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(marcaTiempo))
        }

        val textoAnotado = remember(texto, horaTexto) {
            buildAnnotatedString {
                append(texto)
                append("  ")
                appendInlineContent(ID_PLACEHOLDER_HORA, "[$horaTexto]")
            }
        }

        val anchoPlaceholder = if (esMio) 40.sp else 28.sp
        val inlineContent = remember(horaTexto, esMio, estaLeido, estaEntregado, estaSincronizado, colorContenido) {
            mapOf(
                ID_PLACEHOLDER_HORA to InlineTextContent(
                    Placeholder(
                        width = anchoPlaceholder,
                        height = 18.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = horaTexto,
                            fontSize = 10.sp,
                            color = colorContenido.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                        if (esMio) {
                            Spacer(Modifier.size(2.dp))
                            val colorTick = if (estaLeido) Color(0xFF4FC3F7) else colorContenido.copy(alpha = 0.4f)
                            when {
                                !estaSincronizado -> Icon(Icons.Default.Schedule, null, tint = colorTick, modifier = Modifier.size(10.dp))
                                estaLeido || estaEntregado -> Icon(Icons.Default.DoneAll, null, tint = colorTick, modifier = Modifier.size(12.dp))
                                else -> Icon(Icons.Default.Done, null, tint = colorTick, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            )
        }

        Text(
            text = textoAnotado,
            inlineContent = inlineContent,
            color = colorContenido,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

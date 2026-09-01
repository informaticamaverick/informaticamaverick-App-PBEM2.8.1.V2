package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * --- CONTENEDOR DESLIZABLE PARA RESPUESTA (V2026.7) ---
 * [ELITE]: Implementa el gesto de deslizar para responder con feedback háptico.
 */
@Composable
fun ContenedorRespuesta(
    alResponder: () -> Unit,
    alHacerLongClick: () -> Unit = {},
    colorAcento: Color,
    esDestacado: Boolean = false, // 🔥 [NEW v2026.ELITE]
    contenido: @Composable () -> Unit
) {
    var desplazamientoX by remember { mutableFloatStateOf(0f) }
    val maximoDesplazamiento = 150f
    val retroalimentacion = LocalHapticFeedback.current
    var activado by remember { mutableStateOf(false) }

    // --- ANIMACIÓN DE RESALTADO (TIPO TELEGRAM/WHATSAPP) ---
    val colorResaltadoAnimado by animateColorAsState(
        targetValue = if (esDestacado) colorAcento.copy(alpha = 0.25f) else Color.Transparent,
        animationSpec = if (esDestacado) {
            tween(durationMillis = 300)
        } else {
            tween(durationMillis = 800, delayMillis = 200)
        },
        label = "animacionResaltado"
    )

    val desplazamientoAnimadoX by animateFloatAsState(
        targetValue = desplazamientoX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "animacionDesplazamiento"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResaltadoAnimado) // 🔥 [ELITE] Aplicar el color de resaltado
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { 
                        retroalimentacion.performHapticFeedback(HapticFeedbackType.LongPress)
                        alHacerLongClick() 
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { cambio, cantidad ->
                        cambio.consume()
                        val siguienteX = (desplazamientoX + cantidad).coerceIn(0f, maximoDesplazamiento)
                        desplazamientoX = siguienteX
                        
                        if (desplazamientoX >= maximoDesplazamiento * 0.7f && !activado) {
                            retroalimentacion.performHapticFeedback(HapticFeedbackType.LongPress)
                            activado = true
                        } else if (desplazamientoX < maximoDesplazamiento * 0.7f && activado) {
                            activado = false
                        }
                    },
                    onDragEnd = {
                        if (activado) alResponder()
                        desplazamientoX = 0f
                        activado = false
                    },
                    onDragCancel = {
                        desplazamientoX = 0f
                        activado = false
                    }
                )
            }
    ) {
        val progreso = (desplazamientoX / (maximoDesplazamiento * 0.7f)).coerceIn(0f, 1f)
        val escalaIcono = if (activado) 1.2f else progreso
        val alfaIcono = progreso

        if (desplazamientoX > 10f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .graphicsLayer {
                        scaleX = escalaIcono
                        scaleY = escalaIcono
                        alpha = alfaIcono
                    }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    tint = if (activado) colorAcento else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Box(modifier = Modifier.offset { IntOffset(desplazamientoAnimadoX.toInt(), 0) }) {
            contenido()
        }
    }
}


































package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BARRA DE ENTRADA DE MENSAJES MAVERICK (V2026.7) ---
 * ELITE: Entrada universal con grabadora de audio integrada y animaciones premium.
 */
@Composable
fun BarraEntradaMensaje(
    modifier: Modifier = Modifier,
    valor: String,
    alCambiarValor: (String) -> Unit,
    alEnviar: (String) -> Unit,
    colorAcento: Color,
    alHacerClickAdjunto: () -> Unit,
    alHacerClickMic: () -> Unit,
    alHacerClickCamara: () -> Unit = {},
    alHacerClickEmoji: () -> Unit = {},
    estaGrabando: Boolean = false,
    tiempoGrabacion: Int = 0,
    alCancelarAudio: () -> Unit = {},
    menuAdjuntosAbierto: Boolean = false
) {
    val retroalimentacion = LocalHapticFeedback.current
    var desplazamientoDragX by remember { mutableFloatStateOf(0f) }
    
    val escalaMic by animateFloatAsState(if (estaGrabando) 1.4f else 1f, label = "escalaMic")
    val rotacionClip by animateFloatAsState(if (menuAdjuntosAbierto) 45f else 0f, label = "rotClip")
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp) 
            .imePadding(),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!estaGrabando) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 2.dp)
                    .size(46.dp),
                shape = CircleShape,
                color = if (menuAdjuntosAbierto) colorAcento.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                onClick = alHacerClickAdjunto
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (menuAdjuntosAbierto) Icons.Default.Close else Icons.Default.AttachFile, 
                        contentDescription = null, 
                        tint = if (menuAdjuntosAbierto) colorAcento else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotacionClip }
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 46.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1C242F), 
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), // 🔥 Más brillante
            shadowElevation = 8.dp // 🔥 Más profundidad
        ) {
            if (estaGrabando) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val transicionInfinita = rememberInfiniteTransition(label = "parpadeoMic")
                    val alfaParpadeo by transicionInfinita.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                        label = "alfaMic"
                    )
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF5252).copy(alpha = alfaParpadeo)))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%02d:%02d", tiempoGrabacion / 60, tiempoGrabacion % 60),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Desliza para cancelar",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(14.dp).padding(start = 4.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    BasicTextField(
                        value = valor,
                        onValueChange = alCambiarValor,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        cursorBrush = SolidColor(colorAcento),
                        decorationBox = { inner ->
                            Box {
                                if (valor.isEmpty()) Text(
                                    "Escribe un mensaje...", 
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 16.sp
                                )
                                inner()
                            }
                        }
                    )
                    
                    if (valor.isEmpty()) {
                        IconButton(onClick = alHacerClickCamara) {
                            Icon(
                                Icons.Default.CameraAlt, 
                                null, 
                                tint = Color.White.copy(alpha = 0.4f), 
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(onClick = alHacerClickEmoji) {
                        Icon(
                            Icons.Default.SentimentSatisfiedAlt, 
                            null, 
                            tint = Color.White.copy(alpha = 0.4f), 
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        val noEstaVacio = valor.isNotBlank()
        
        // 🔥 [ELITE] Botón de Acción Principal (Mic / Send) con Transformación Animada
        Surface(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .size(48.dp) // Un poco más grande para el estilo premium
                .scale(escalaMic)
                .pointerInput(estaGrabando) {
                    if (estaGrabando) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (desplazamientoDragX < -100f) alCancelarAudio()
                                else alHacerClickMic()
                                desplazamientoDragX = 0f
                            }
                        ) { cambio, cantidad ->
                            cambio.consume()
                            desplazamientoDragX += cantidad
                        }
                    }
                },
            onClick = { 
                if (noEstaVacio) alEnviar(valor) else alHacerClickMic()
                retroalimentacion.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            shape = CircleShape,
            color = if (noEstaVacio || estaGrabando) colorAcento else Color.White.copy(alpha = 0.08f),
            shadowElevation = if (noEstaVacio || estaGrabando) 6.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = noEstaVacio || estaGrabando,
                    transitionSpec = {
                        (scaleIn(animationSpec = spring(Spring.DampingRatioMediumBouncy)) + fadeIn())
                            .togetherWith(scaleOut() + fadeOut())
                    },
                    label = "iconoAccionTransition"
                ) { isPrimaryAction ->
                    if (isPrimaryAction) {
                        Icon(
                            imageVector = if (estaGrabando) Icons.Default.Done else Icons.AutoMirrored.Filled.Send, 
                            null, 
                            tint = Color.Black, 
                            modifier = Modifier.size(24.dp).let { if (!estaGrabando) it.offset(x = 1.dp) else it }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}



































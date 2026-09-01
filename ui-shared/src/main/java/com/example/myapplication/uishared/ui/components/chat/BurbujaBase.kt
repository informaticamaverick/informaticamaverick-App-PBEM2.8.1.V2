package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- MOLDE BASE PARA BURBUJAS DE CHAT (Standard 2026) ---
 */
@Composable
fun BurbujaBase(
    esMio: Boolean,
    marcaTiempo: Long,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    colorFondo: Color,
    colorContenido: Color,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    margenInterno: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    // [WHATSAPP-STYLE] BurbujaTexto arma la hora/ticks incrustados adentro de su propio Text
    // (inlineContent) para que quede en el mismo renglón que el texto — cuando hace eso, no
    // queremos que BurbujaBase agregue OTRA fila de hora abajo (quedaría duplicada).
    mostrarFilaHora: Boolean = true,
    alHacerClick: (() -> Unit)? = null,
    alHacerSwipeRespuesta: (() -> Unit)? = null,
    contenidoExtra: @Composable (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    val radioBase = 6.dp 
    val radioCola = 2.dp

    val formaBurbuja = RoundedCornerShape(
        topStart = radioBase,
        topEnd = radioBase,
        bottomStart = if (esMio) radioBase else radioCola,
        bottomEnd = if (esMio) radioCola else radioBase
    )

    val pincelFondo = remember(colorFondo, esMio) {
        if (esMio) {
            Brush.linearGradient(
                colors = listOf(colorFondo, colorFondo.copy(alpha = 0.9f)),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        } else {
            Brush.linearGradient(
                colors = listOf(colorFondo.copy(alpha = 0.95f), colorFondo),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        }
    }

    var esVisible by remember { mutableStateOf(value = false) }
    LaunchedEffect(Unit) { esVisible = true }

    val escala by animateFloatAsState(
        targetValue = if (esVisible) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "escalaBurbuja"
    )
    val alfa by animateFloatAsState(
        targetValue = if (esVisible) 1f else 0f,
        animationSpec = tween(150),
        label = "alfaBurbuja"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
                this.alpha = alfa
            },
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (esMio && contenidoExtra != null) {
            Box(modifier = Modifier.padding(end = 8.dp)) { contenidoExtra() }
        }

        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .drawBehind {
                    val tailPath = Path().apply {
                        if (esMio) {
                            moveTo(size.width, size.height - 8.dp.toPx())
                            lineTo(size.width + 5.dp.toPx(), size.height)
                            lineTo(size.width - 10.dp.toPx(), size.height)
                            close()
                        } else {
                            moveTo(0f, size.height - 8.dp.toPx())
                            lineTo(-5.dp.toPx(), size.height)
                            lineTo(10.dp.toPx(), size.height)
                            close()
                        }
                    }
                    drawPath(path = tailPath, brush = pincelFondo)
                }
                .shadow(
                    elevation = 3.dp,
                    shape = formaBurbuja,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = colorFondo.copy(alpha = 0.2f)
                )
                .let { m -> 
                    if (alHacerSwipeRespuesta != null) {
                        m.pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                if ((dragAmount > 50f) && !esMio) alHacerSwipeRespuesta()
                                if ((dragAmount < -50f) && esMio) alHacerSwipeRespuesta()
                                change.consume()
                            }
                        }
                    } else m
                }
                .let { if (alHacerClick != null) it.clickable { alHacerClick() } else it },
            shape = formaBurbuja,
            color = Color.Transparent 
        ) {
            Box(
                modifier = Modifier
                    .background(pincelFondo)
                    .padding(margenInterno)
            ) {
                Column {
                    if ((nombreRespuesta != null) && (contenidoRespuesta != null)) {
                        VistaPreviaRespuesta(
                            remitente = nombreRespuesta,
                            contenido = contenidoRespuesta,
                            colorContenido = colorContenido
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    Column(modifier = Modifier.padding(bottom = 2.dp)) {
                        contenido()
                    }

                    if (mostrarFilaHora) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(marcaTiempo)),
                                fontSize = 10.sp,
                                color = colorContenido.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Medium
                            )
                            if (esMio) {
                                val colorTick = if (estaLeido) Color(0xFF4FC3F7) else colorContenido.copy(alpha = 0.4f)
                                when {
                                    !estaSincronizado -> Icon(Icons.Default.Schedule, null, tint = colorTick, modifier = Modifier.size(10.dp))
                                    estaLeido || estaEntregado -> Icon(Icons.Default.DoneAll, null, tint = colorTick, modifier = Modifier.size(13.dp))
                                    else -> Icon(Icons.Default.Done, null, tint = colorTick, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!esMio && contenidoExtra != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) { contenidoExtra() }
        }
    }
}

@Composable
fun VistaPreviaRespuesta(
    remitente: String,
    contenido: String,
    colorContenido: Color
) {
    Surface(
        color = Color.Black.copy(alpha = 0.2f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.5.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2197F5), Color(0xFF64B5F6))
                        )
                    )
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = remitente,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2197F5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = contenido,
                    fontSize = 12.sp,
                    color = colorContenido.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

package com.example.myapplication.ui.componentes.be.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithCache
import com.example.myapplication.ui.componentes.sistema.geminiGradientBrush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.componentes.be.modelos.TipoBeToast
import com.example.myapplication.ui.estilos.PBEMTheme

/**
 * --- BE TOAST (ELITE MAVERICK UI v2026) ---
 * [PROPÓSITO]: Diseño de alta gama con curvas orgánicas, efecto de volumen
 * y profundidad tonal. Basado en estándares de diseño de Apple y Telegram.
 */
private object EliteBePalette {
    // Talking (Mensaje propio del asistente)
    val SurfaceTalking = listOf(Color(0xFF2B5278), Color(0xFF1E3A56))
    val BorderTalking = Color(0xFF64B5F6).copy(alpha = 0.4f)

    // Info
    val SurfaceInfo = listOf(Color(0xFF182533), Color(0xFF111D29))
    val BorderInfo = Color(0xFF7EB5E6).copy(alpha = 0.3f)
    
    // Processing
    val SurfaceProcessing = listOf(Color(0xFF1E252D), Color(0xFF14191F))
    val BorderProcessing = Color(0xFFE1E9F1).copy(alpha = 0.2f)
    
    // Success
    val SurfaceSuccess = listOf(Color(0xFF1A2E25), Color(0xFF12241D))
    val BorderSuccess = Color(0xFF84DC98).copy(alpha = 0.4f)
    
    // Error
    val SurfaceError = listOf(Color(0xFF331E20), Color(0xFF251416))
    val BorderError = Color(0xFFE58585).copy(alpha = 0.4f)
}

@Composable
fun BeToast(
    mensaje: String,
    tipo: TipoBeToast,
    modifier: Modifier = Modifier,
    soloContenido: Boolean = false
) {
    val isProcessing = tipo == TipoBeToast.PROCESANDO
    val geminiBrush = geminiGradientBrush()

    val (gradientColors, borderColor) = when (tipo) {
        TipoBeToast.HABLANDO -> Pair(EliteBePalette.SurfaceTalking, EliteBePalette.BorderTalking)
        TipoBeToast.INFO -> Pair(EliteBePalette.SurfaceInfo, EliteBePalette.BorderInfo)
        TipoBeToast.PROCESANDO -> Pair(EliteBePalette.SurfaceProcessing, EliteBePalette.BorderProcessing)
        TipoBeToast.EXITO -> Pair(EliteBePalette.SurfaceSuccess, EliteBePalette.BorderSuccess)
        TipoBeToast.ERROR -> Pair(EliteBePalette.SurfaceError, EliteBePalette.BorderError)
    }

    // Animación de Morphing de Colores (Fondo y Borde)
    val animatedTopColor by animateColorAsState(targetValue = gradientColors[0], animationSpec = tween(500), label = "top_bg")
    val animatedBottomColor by animateColorAsState(targetValue = gradientColors[1], animationSpec = tween(500), label = "bottom_bg")
    val animatedBorderColor by animateColorAsState(targetValue = borderColor, animationSpec = tween(500), label = "border")

    if (soloContenido) {
        val emoji = when (tipo) {
            TipoBeToast.HABLANDO -> "💬"
            TipoBeToast.INFO -> "ℹ"
            TipoBeToast.EXITO -> "✓"
            TipoBeToast.ERROR -> "✕"
            else -> null
        }
        BeToastContent(mensaje, tipo, emoji, modifier)
    } else {
        // --- BURBUJA ELITE ---
        Surface(
            modifier = modifier
                .wrapContentSize()
                .padding(vertical = 4.dp)
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
            color = Color.Transparent, // Fondo manejado por el gradiente
            shape = EliteChatBubbleShape(),
            shadowElevation = 8.dp 
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (isProcessing) Modifier.background(geminiBrush)
                        else Modifier.background(Brush.verticalGradient(listOf(animatedTopColor, animatedBottomColor)))
                    )
                    .graphicsLayer { clip = true; shape = EliteChatBubbleShape() }
                    .drawWithCache {
                        // Efecto de Rim Light (Luz de borde superior)
                        onDrawWithContent {
                            drawContent()
                            drawPath(
                                path = Path().apply {
                                    moveTo(0f, size.height)
                                    lineTo(0f, 20f)
                                    quadraticTo(0f, 0f, 20f, 0f)
                                    lineTo(size.width - 40f, 0f)
                                },
                                color = Color.White.copy(alpha = 0.12f),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                    .padding(1.dp) // Espacio para el borde
            ) {
                // El borde real se dibuja aquí para que siga la forma exacta
                Surface(
                    color = Color.Transparent,
                    shape = EliteChatBubbleShape(),
                    border = BorderStroke(
                        width = if (isProcessing) 1.5.dp else 1.dp, 
                        brush = if (isProcessing) geminiBrush else SolidColor(animatedBorderColor)
                    )
                ) {
                    AnimatedContent(
                        targetState = Pair(mensaje, tipo),
                        transitionSpec = {
                            (fadeIn(tween(300, 100)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300, 100)))
                                .togetherWith(fadeOut(tween(150)))
                        },
                        label = "text_morph"
                    ) { (targetMensaje, targetTipo) ->
                        val emoji = when (targetTipo) {
                            TipoBeToast.HABLANDO -> "💬"
                            TipoBeToast.INFO -> "ℹ"
                            TipoBeToast.EXITO -> "✓"
                            TipoBeToast.ERROR -> "✕"
                            else -> null
                        }
                        BeToastContent(targetMensaje, targetTipo, emoji)
                    }
                }
            }
        }
    }
}

/**
 * --- ELITE CHAT BUBBLE SHAPE ---
 * Diseño tipo tarjeta cuadrada con esquinas de 6dp para un look corporativo.
 * La cola es un triángulo recto posicionado en la parte superior.
 */
class EliteChatBubbleShape(
    private val cornerRadiusDp: Float = 6f,
    private val tailWidthDp: Float = 10f,
    private val tailHeightDp: Float = 12f
) : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val d = density.density
        val r = cornerRadiusDp * d
        val tw = tailWidthDp * d
        val th = tailHeightDp * d

        val w = size.width - tw
        val h = size.height
        
        // 🔥 [ELITE]: Cola posicionada más arriba (~35% de la altura)
        val tailCenterY = h * 0.35f
        val tailTopY = tailCenterY - (th / 2f)
        val tailBottomY = tailCenterY + (th / 2f)

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            quadraticTo(w, 0f, w, r) // Top Right del cuerpo
            
            lineTo(w, tailTopY)
            
            // LA COLA: Triángulo recto táctico
            lineTo(w + tw, tailCenterY) 
            lineTo(w, tailBottomY)         
            
            lineTo(w, h - r)
            quadraticTo(w, h, w - r, h) // Bottom Right
            
            lineTo(r, h)
            quadraticTo(0f, h, 0f, h - r) // Bottom Left
            
            lineTo(0f, r)
            quadraticTo(0f, 0f, r, 0f) // Top Left
            
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun BeToastContent(
    mensaje: String,
    tipo: TipoBeToast,
    iconEmoji: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(start = 14.dp, top = 12.dp, end = 22.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (tipo == TipoBeToast.PROCESANDO) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 3.dp,
                color = Color.White
            )
        } else if (iconEmoji != null) {
            Text(
                text = iconEmoji,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        Text(
            text = mensaje, 
            color = Color.White,
            fontSize = 15.sp, 
            fontWeight = FontWeight.ExtraBold, // Peso máximo para resaltar
            letterSpacing = (-0.3).sp, // Kerning profesional
            lineHeight = 20.sp
        )
    }
}

@Preview(name = "Elite - Hablando", showBackground = true, backgroundColor = 0xFF0B1324)
@Composable
fun PreviewBeToastElite() {
    PBEMTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            BeToast(mensaje = "Analizando tu solicitud, dame un momento...", tipo = TipoBeToast.HABLANDO)
        }
    }
}

@Preview(name = "Elite - Éxito", showBackground = true, backgroundColor = 0xFF0B1324)
@Composable
fun PreviewBeToastSuccessElite() {
    PBEMTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            BeToast(mensaje = "¡Configuración guardada!", tipo = TipoBeToast.EXITO)
        }
    }
}

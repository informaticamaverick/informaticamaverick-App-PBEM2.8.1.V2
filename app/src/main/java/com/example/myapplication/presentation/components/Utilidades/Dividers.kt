package com.example.myapplication.presentation.components.Utilidades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
/**
 * Divider básico estilo Material 3 (Android 16 style)
 */
@Composable
fun M3MaverickDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaverickColors.TextMuted.copy(alpha = 0.2f)
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color
    )
}

/**
 * Divider vertical básico estilo Material 3
 */
@Composable
fun M3VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaverickColors.TextMuted.copy(alpha = 0.2f)
) {
    VerticalDivider(
        modifier = modifier.width(thickness),
        color = color
    )
}

/**
 * Divider Horizontal Premium con gradiente y brillo central opcional.
 */
@Composable
fun PremiumHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    accentColor: Color = Color.White,
    glow: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (glow) thickness + 2.dp else thickness)
            .drawBehind {
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.8f),
                        accentColor.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
                
                // Línea principal
                drawLine(
                    brush = brush,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = thickness.toPx(),
                    cap = StrokeCap.Round
                )
                
                // Brillo (Glow)
                if (glow) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent),
                            center = center,
                            radius = size.width / 2
                        ),
                        alpha = 0.4f
                    )
                }
            }
    )
}

/**
 * Divider Vertical Premium con gradiente.
 */
@Composable
fun PremiumVerticalDivider(
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    thickness: Dp = 1.dp,
    accentColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .width(thickness)
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = 0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Divider estilo "Gemini" con el degradado característico de la app.
 */
@Composable
fun GeminiDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(MaverickColors.GeminiBrush)
    )
}

/**
 * Divider de sección con texto integrado y estilo premium.
 * Ahora tiene la capacidad de albergar contenido inferior (como filas de etiquetas).
 */
@Composable
fun SectionHeaderWithDivider(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    textColor: Color = Color.White,
    dividerColor: Color = Color.White.copy(alpha = 0.3f),
    content: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji != null) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(dividerColor, Color.Transparent)
                        )
                    )
            )
        }
        
        // Si hay contenido (ej: Fila de etiquetas), se muestra debajo del header
        content?.invoke()
    }
}

/**
 * Componente Visual para Divisor Premium (Gradiente Maverick)
 */
@Composable
fun PremiumDividerV3(color: Color, verticalPadding: Dp = 3.dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        color.copy(0.5f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Componente Visual para Divisor Vertical Premium (Gradiente Maverick)
 * [NUEVO]: Utilizado para separar grupos de botones en barras de herramientas.
 */
@Composable
fun PremiumVerticalDividerV3(color: Color, horizontalPadding: Dp = 4.dp, height: Dp = 24.dp) {
    Box(
        Modifier
            .padding(horizontal = horizontalPadding)
            .width(1.2.dp)
            .height(height)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        color.copy(0.7f),
                        Color.Transparent
                    )
                )
            )
    )
}




/**
 * Divider Horizontal con terminaciones en "V" apuntando hacia afuera
 *
 * @param length El largo total del divider.
 * @param thickness El grosor de las líneas.
 * @param forkSize El tamaño de las ramificaciones en las puntas.
 * @param color El color del divider.
 */
@Composable
fun HorizontalForkDivider(
    modifier: Modifier = Modifier,
    length: Dp = 150.dp,
    thickness: Dp = 2.dp,
    forkSize: Dp = 12.dp,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier.size(width = length, height = forkSize * 2)) {
        val strokeWidthPx = thickness.toPx()
        val forkPx = forkSize.toPx()
        val centerY = size.height / 2

        // Línea principal horizontal
        drawLine(
            color = color,
            start = Offset(x = forkPx, y = centerY),
            end = Offset(x = size.width - forkPx, y = centerY),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        // Punta Izquierda (Arriba y Abajo)
        drawLine(
            color = color,
            start = Offset(x = forkPx, y = centerY),
            end = Offset(x = 0f, y = centerY - forkPx),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(x = forkPx, y = centerY),
            end = Offset(x = 0f, y = centerY + forkPx),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        // Punta Derecha (Arriba y Abajo)
        drawLine(
            color = color,
            start = Offset(x = size.width - forkPx, y = centerY),
            end = Offset(x = size.width, y = centerY - forkPx),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(x = size.width - forkPx, y = centerY),
            end = Offset(x = size.width, y = centerY + forkPx),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Divider Vertical con terminaciones en "V" apuntando hacia afuera
 *
 * @param length El largo (alto) total del divider.
 * @param thickness El grosor de las líneas.
 * @param forkSize El tamaño de las ramificaciones en las puntas.
 * @param color El color del divider.
 */
@Composable
fun VerticalForkDivider(
    modifier: Modifier = Modifier,
    length: Dp = 150.dp,
    thickness: Dp = 1.dp,
    forkSize: Dp = 12.dp,
    color: Color = Color.Black
) {
    Canvas(modifier = modifier.size(width = forkSize * 2, height = length)) {
        val strokeWidthPx = thickness.toPx()
        val forkPx = forkSize.toPx()
        val centerX = size.width / 2

        // Línea principal vertical
        drawLine(
            color = color,
            start = Offset(x = centerX, y = forkPx),
            end = Offset(x = centerX, y = size.height - forkPx),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        // Punta Superior (Izquierda y Derecha)
        drawLine(
            color = color,
            start = Offset(x = centerX, y = forkPx),
            end = Offset(x = centerX - forkPx, y = 0f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(x = centerX, y = forkPx),
            end = Offset(x = centerX + forkPx, y = 0f),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        // Punta Inferior (Izquierda y Derecha)
        drawLine(
            color = color,
            start = Offset(x = centerX, y = size.height - forkPx),
            end = Offset(x = centerX - forkPx, y = size.height),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(x = centerX, y = size.height - forkPx),
            end = Offset(x = centerX + forkPx, y = size.height),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
    }
}



// ==========================================================================================
// --- 🖼️ PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PreviewMaverickDividers() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("M3 Basic Divider:", color = Color.Gray)
        M3MaverickDivider()

        Text("Premium Glow (Red):", color = Color.Gray)
        PremiumHorizontalDivider(accentColor = MaverickColors.DeepRed)

        Text("Premium Glow (Neon Cyan):", color = Color.Gray)
        PremiumHorizontalDivider(accentColor = MaverickColors.NeonCyan)

        Text("Gemini Divider:", color = Color.Gray)
        GeminiDivider()

        Text("Section Header Premium (Acid Green):", color = Color.Gray)
        SectionHeaderWithDivider(
            text = "ESTADÍSTICAS",
            emoji = "📊",
            textColor = MaverickColors.AcidGreen,
            dividerColor = MaverickColors.AcidGreen.copy(alpha = 0.4f)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Vertical Dividers: ", color = Color.Gray)
            M3VerticalDivider(modifier = Modifier.height(30.dp))
            Spacer(Modifier.width(20.dp))
            PremiumVerticalDivider(accentColor = MaverickColors.CyberPink, height = 40.dp)
        }
    }
}
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CustomDividersPreview() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ejemplo Vertical
        VerticalForkDivider(
            length = 200.dp,
            thickness = 2.dp,
            forkSize = 14.dp,
            color = Color.Black
        )

        // Ejemplo Horizontal y variaciones
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalForkDivider(
                length = 180.dp,
                thickness = 2.dp,
                forkSize = 14.dp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Ejemplo cambiando colores y grosores usando las variables
            HorizontalForkDivider(
                length = 120.dp,
                thickness = 4.dp,
                forkSize = 8.dp,
                color = Color(0xFF3F51B5) // Indigo
            )
        }
    }
}
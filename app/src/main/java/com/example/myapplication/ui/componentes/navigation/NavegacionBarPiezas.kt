package com.example.myapplication.ui.componentes.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.componentes.sistema.geminiGradientBrush
import com.example.myapplication.ui.componentes.sistema.shakeClick
import com.example.myapplication.ui.pantallas.home.Screen
import com.example.myapplication.ui.pantallas.home.getEmojiForScreen
import com.example.myapplication.uishared.estilos.CPCyberColors
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * NavegacionBarPiezas.kt
 * Propósito: Átomos visuales (piezas) del sistema de navegación V3.
 * Funcionamiento: Componentes stateless optimizados para rendimiento.
 * Relación: Utilizados por NavegacionBarScreen.kt.
 */

// ==================================================================================
// --- PIEZA 1: FONDO NAVEGACIÓN V3 ---
// ==================================================================================

@Composable
fun PiezaFondoNavV3(
    modifier: Modifier = Modifier,
    alturaBase: androidx.compose.ui.unit.Dp = 62.dp,
    paddingInferior: androidx.compose.ui.unit.Dp = 0.dp,
    contenido: @Composable BoxScope.() -> Unit
) {
    val barShape = CutCornerShape(topStart = 16.dp, topEnd = 16.dp)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alturaBase + paddingInferior)
            .drawWithCache {
                val path = Path()
                val shadowPath = Path()
                val strokeWidth = 1.dp.toPx()
                val cornerSize = 16.dp.toPx()
                val shadowHeight = 24.dp.toPx()

                val borderGradient = Brush.horizontalGradient(
                    0.0f to CPCyberColors.MaverickCyan.copy(alpha = 0.05f),
                    0.15f to CPCyberColors.MaverickCyan,
                    0.85f to CPCyberColors.MaverickCyan,
                    1.0f to CPCyberColors.MaverickCyan.copy(alpha = 0.05f)
                )

                path.reset()
                path.moveTo(0f, cornerSize)
                path.lineTo(cornerSize, 0f)
                path.lineTo(size.width - cornerSize, 0f)
                path.lineTo(size.width, cornerSize)

                shadowPath.reset()
                shadowPath.moveTo(0f, cornerSize)
                shadowPath.lineTo(cornerSize, 0f)
                shadowPath.lineTo(size.width - cornerSize, 0f)
                shadowPath.lineTo(size.width, cornerSize)
                shadowPath.lineTo(size.width, -shadowHeight)
                shadowPath.lineTo(0f, -shadowHeight)
                shadowPath.close()

                onDrawWithContent {
                    drawPath(
                        path = shadowPath,
                        brush = Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                            startY = 0f,
                            endY = -shadowHeight
                        )
                    )
                    drawContent()
                    drawPath(
                        path = path,
                        brush = borderGradient,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = path,
                        brush = borderGradient,
                        style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round),
                        alpha = 0.15f
                    )
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(barShape)
                .background(SharedPalette.AbsoluteBlack)
        )
        contenido()
    }
}

// ==================================================================================
// --- PIEZA 2: ICONO DESTINO V3 (PILL ANIMADA) ---
// ==================================================================================

@Composable
fun PiezaIconoDestinoV3(
    modifier: Modifier = Modifier,
    screen: Screen,
    estaSeleccionado: Boolean,
    tieneNotificacion: Boolean = false,
    alHacerClick: () -> Unit
) {
    val geminiBrush = geminiGradientBrush()
    val animatedWidth by animateDpAsState(
        targetValue = if (estaSeleccionado) 100.dp else 52.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "WidthPillNav"
    )

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(48.dp)
            .border(
                width = if (estaSeleccionado) 2.dp else 1.dp,
                brush = if (estaSeleccionado) geminiBrush else SolidColor(Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SharedPalette.BentoDarkGlassBackground.copy(alpha = if (estaSeleccionado) 0.85f else 0.2f))
            .shakeClick { alHacerClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            if (estaSeleccionado) {
                Text(
                    text = getEmojiForScreen(screen),
                    fontSize = 24.sp
                )
            } else {
                Icon(
                    imageVector = getIconForScreen(screen),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }

            if (tieneNotificacion) {
                PiezaBadgeNotifV3()
            }
        }
    }
}

// ==================================================================================
// --- PIEZA 3: BADGE DE NOTIFICACIÓN V3 ---
// ==================================================================================

@Composable
fun PiezaBadgeNotifV3(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(10.dp)
            .offset(x = 4.dp, y = (-4).dp)
            .background(SharedPalette.ElectricCyan, CircleShape)
            .border(1.5.dp, Color.Black, CircleShape)
    )
}

/**
 * Utilidad privada para mapear iconos por defecto.
 */
private fun getIconForScreen(screen: Screen): ImageVector = when (screen) {
    Screen.Home -> Icons.Filled.Home
    Screen.Concursos -> Icons.Filled.AttachMoney
    Screen.Chat -> Icons.AutoMirrored.Filled.Chat
    Screen.Calendar -> Icons.Filled.CalendarToday
    Screen.Promo -> Icons.Filled.LocalFireDepartment
    else -> Icons.Filled.Home
}

// ==================================================================================
// --- PREVIEWS (LEY #10: SCREEN ANATOMY) ---
// ==================================================================================

@Preview(name = "Piezas Nav V3 - Vista General", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewNavegacionBarPiezas() {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PIEZAS NAVEGACIÓN V3", color = Color.Gray)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PiezaIconoDestinoV3(screen = Screen.Home, estaSeleccionado = true, alHacerClick = {})
            PiezaIconoDestinoV3(screen = Screen.Chat, estaSeleccionado = false, tieneNotificacion = true, alHacerClick = {})
            PiezaIconoDestinoV3(screen = Screen.Calendar, estaSeleccionado = false, alHacerClick = {})
        }
        
        PiezaFondoNavV3 {
            Text("CONTENEDOR FONDO", color = Color.White, modifier = Modifier.align(Alignment.Center))
        }
    }
}

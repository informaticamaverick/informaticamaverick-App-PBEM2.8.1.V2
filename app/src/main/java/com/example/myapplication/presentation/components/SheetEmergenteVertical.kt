package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.components.Utilidades.AutoSizeText
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.shakeClick

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: SHEET EMERGENTE VERTICAL (BENTO CYBER PREMIUM) ---
 * ==========================================================================================
 */
@Composable
fun SheetEmergenteVertical(
    isVisible: Boolean,
    onClose: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    emoji: String? = null,
    topOffset: Dp = 16.dp,
    showEmoji: Boolean = true,
    showHelperText: Boolean = true,
    showTitle: Boolean = true,
    showActions: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    // [SECCIÓN: CONTENEDOR MAESTRO PARA CAPAS]
    Box(modifier = Modifier.fillMaxSize()) {

        // --- 🎭 CAPA 1: FONDO OSCURO TÁCTICO (FADE IN/OUT) ---
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Aplicamos el padding superior para NO tapar al asistente (BeBrain) ni el buscador
                    .padding(top = topOffset.coerceAtLeast(0.dp))
                    // [SECCIÓN: FONDO ROG DARK] - Color de la paleta Maverick
                    .background(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() }
            )
        }

        // --- 🏗️ CAPA 2: EL CONTENIDO DE LA SHEET (SLIDE IN/OUT) ---
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut()
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(top = topOffset.coerceAtLeast(0.dp))
                    .padding(horizontal = 0.dp)
            ) {
                // --- 1. CABECERA TÁCTICA (EFECTO STRIX APLICADO) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(CutCornerShape(10.dp))
                        // [SECCIÓN: FONDO STRIX] - Gradiente ROG y Capa de Cristal
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaverickColors.RogMagenta,
                                    MaverickColors.DeepPurple,
                                    MaverickColors.VantaBlack
                                )
                            )
                        )
                        .background(MaverickColors.GlassOverlayRog)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val cornerSize = 10.dp.toPx()
                            
                            // 1. EFECTO DIVIDER PREMIUM SUPERIOR
                            val cyanElectric = MaverickColors.ElectricCyan
                            val topBrush = Brush.horizontalGradient(
                                colors = listOf(cyanElectric.copy(alpha = 0.5f), cyanElectric, cyanElectric.copy(alpha = 0.5f))
                            )
                            
                            val topPath = Path().apply {
                                moveTo(0f, cornerSize)
                                lineTo(cornerSize, 0f)
                                lineTo(size.width - cornerSize, 0f)
                                lineTo(size.width, cornerSize)
                            }
                            drawPath(path = topPath, brush = topBrush, style = Stroke(width = strokeWidth))

                            // 2. EFECTO DIVIDER PREMIUM INFERIOR
                            val lightGray = Color(0xFFE0E0E0)
                            val bottomBrush = Brush.horizontalGradient(
                                colors = listOf(lightGray.copy(alpha = 0.4f), lightGray.copy(alpha = 0.7f), lightGray.copy(alpha = 0.4f))
                            )
                            
                            val bottomPath = Path().apply {
                                moveTo(size.width, size.height - cornerSize)
                                lineTo(size.width - cornerSize, size.height)
                                lineTo(cornerSize, size.height)
                                lineTo(0f, size.height - cornerSize)
                            }
                            drawPath(path = bottomPath, brush = bottomBrush, style = Stroke(width = strokeWidth))
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showEmoji && emoji != null) {
                            Text(
                                text = emoji, 
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            if (showHelperText && helperText != null) {
                                Text(
                                    text = helperText.uppercase(),
                                    color = Color.Gray.copy(alpha = 0.9f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            if (showTitle) {
                                AutoSizeText(
                                    text = title.uppercase(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    maxLines = 2 
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            if (showActions) {
                                actions()
                            }
                            SheetCloseButton(onClick = onClose)
                        }
                    }
                }
/**
                // --- 1.5 INDICADOR Y ESPACIADOR TÁCTICO (BAJANDO LA SECCIÓN DE TARJETAS) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp), // Se aumentó la altura para bajar más la sección de tarjetas
                    contentAlignment = Alignment.TopCenter
                ) {
                    // [SECCIÓN: INDICADOR V CON OFFSET] - Chevron posicionado justo debajo del encabezado
                    Canvas(
                        modifier = Modifier
                            .size(30.dp, 12.dp)
                            .offset(y = 2.dp) // Offset negativo para "engancharlo" visualmente al borde del encabezado
                    ) {
                        val strokeWidth = 2.dp.toPx()
                        val vWidth = 12.dp.toPx()
                        val vHeight = 8.dp.toPx()
                        val lightGray = Color(0xFFE0E0E0)
                        val vBrush = Brush.horizontalGradient(
                            colors = listOf(
                                lightGray.copy(alpha = 0.3f),
                                lightGray.copy(alpha = 0.6f),
                                lightGray.copy(alpha = 0.3f)
                            )
                        )
                        
                        val vPath = Path().apply {
                            moveTo(size.width / 2 - vWidth, 0f)
                            lineTo(size.width / 2, vHeight)
                            lineTo(size.width / 2 + vWidth, 0f)
                        }
                        drawPath(path = vPath, brush = vBrush, style = Stroke(width = strokeWidth))
                    }
                }
**/
                // --- 2. ÁREA DE CONTENIDO (EFECTO STRIX APLICADO) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(CutCornerShape(10.dp))
                        // [SECCIÓN: FONDO STRIX] - Gradiente ROG y Capa de Cristal
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaverickColors.RogMagenta,
                                    MaverickColors.DeepPurple,
                                    MaverickColors.VantaBlack
                                )
                            )
                        )
                        .background(MaverickColors.GlassOverlayRog)
                        .drawBehind {
                            val strokeWidth = 1.5.dp.toPx()
                            val cornerSize = 10.dp.toPx()
                            val grayColor = Color.Gray.copy(alpha = 0.4f)
                            val contentTopPath = Path().apply {
                                moveTo(0f, cornerSize)
                                lineTo(cornerSize, 0f)
                                lineTo(size.width - cornerSize, 0f)
                                lineTo(size.width, cornerSize)
                            }
                            drawPath(path = contentTopPath, color = grayColor, style = Stroke(width = strokeWidth))
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        content = content
                    )
                }
            }
        }
    }
}

/**
 * HELPER: Botón de cierre estilo círculo táctico rojo con etiqueta
 */
@Composable
fun SheetCloseButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp) // --- 📐 TAMAÑO REDUCIDO SEGÚN IMAGEN ---
                .border(width = 1.2.dp, color = Color(0xFFEF4444).copy(alpha = 0.7f), shape = CircleShape)
                .background(color = Color(0xFFEF4444).copy(alpha = 0.15f), shape = CircleShape)
                .shakeClick { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
            )
        }
/**
        Spacer(modifier = Modifier.height(0.5.dp)) // --- 📏 DISTANCIA CRÍTICA ---

        Text(
            text = "CERRAR",
            color = Color(0xFFEF4444),
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        **/
    }
}

/**
 * HELPER: Botón de acción circular premium
 */
@Composable
fun SheetActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    active: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (active) MaverickColors.ElectricCyan.copy(alpha = 0.1f)
                    else Color.White.copy(alpha = 0.05f),
                    CircleShape
                )
                .border(
                    width = 1.dp,
                    color = if (active) MaverickColors.ElectricCyan.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }

/**
        Spacer(modifier = Modifier.height(0.5.dp))
        Text(
            text = label.uppercase(),
            color = if (active) Color.White else Color.Gray,
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
        **/
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun SheetEmergenteVerticalPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        SheetEmergenteVertical(
            isVisible = true,
            onClose = {},
            title = "Panel Táctico de Monitoreo de Sistemas",
            helperText = "Filtros de sistema",
            emoji = "🛰️",
            showEmoji = true,
            showHelperText = true,
            showTitle = true,
            showActions = true,
            actions = {
                SheetActionButton(icon = "⚙️", label = "Ajustes", onClick = {})
                SheetActionButton(icon = "🌪️", label = "Filtro", onClick = {}, active = true)
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("TARJETA DE EJEMPLO", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.presentation.designsystem.components.shakeClick
import com.example.myapplication.presentation.designsystem.components.CyberMaverickNeonBox
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex


// ==========================================================================================
// --- 0. CONFIGURACIÓN Y CONSTANTES ---
// ==========================================================================================

enum class BubbleTailPosition {
    TOP_RIGHT,
    BOTTOM_RIGHT
}

// Valores de Diseño Premium
val ElectricCyanColor = Color(0xFF00F0FF)

// ==========================================================================================
// --- 1. MICRO-COMPONENTES ---
// ==========================================================================================

data class ControlItemLite(
    val label: String,
    val emoji: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val color: Color,
    val id: String = label.lowercase()
)

@Composable
fun ActionChip(
    item: ControlItemLite,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = item.color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, item.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.emoji != null) {
                Text(item.emoji, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
            } else if (item.icon != null) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(16.dp).padding(end = 6.dp))
            }
            Text(
                text = item.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ==========================================================================================
// --- 2. COMPONENTE: COLA DE BURBUJA TECH (TRIÁNGULO) ---
// ==========================================================================================

@Composable
private fun TechTriangleTail(
    position: BubbleTailPosition,
    modifier: Modifier = Modifier,
    color: Color = ElectricCyanColor
) {
    Canvas(modifier = modifier.size(24.dp, 18.dp)) {
        val path = Path().apply {
            if (position == BubbleTailPosition.TOP_RIGHT) {
                // Triángulo que nace de la burbuja y apunta hacia arriba/derecha
                moveTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width, 0f)
            } else {
                // Triángulo que nace de la burbuja y apunta hacia abajo/derecha
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
            }
            close()
        }
        drawPath(path, color.copy(alpha = 0.5f))
    }
}

// ==========================================================================================
// --- 3. BURBUJA SUPERIOR (MODO BÚSQUEDA ACTIVA) ---
// ==========================================================================================

@Composable
fun BeTopBubble(
    isVisible: Boolean,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = ElectricCyanColor,
    backgroundColor: Color = Color(0xFF0D0221),
    content: @Composable ColumnScope.() -> Unit
) {
    // Forzamos ElectricCyan si el color es gris ( NotFound fallback )
    val finalBorderColor = if (borderColor == Color.Gray) ElectricCyanColor else borderColor

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut(tween(300)) + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp) // Quitamos padding horizontal para ocupar todo el ancho
                .pointerInput(Unit) { detectTapGestures { } } // Bloquea toques al fondo
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                // COLA TRIANGULAR
                TechTriangleTail(
                    position = BubbleTailPosition.TOP_RIGHT,
                    modifier = Modifier.offset(x = (-70).dp, y = 2.dp),
                    color = finalBorderColor.copy(alpha = 0.5f)
                )

                // CUERPO NEON BOX
                CyberMaverickNeonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 80.dp), // <--- MODIFICAR: ALTURA MÍNIMA BASE
                    accentColor = finalBorderColor,
                    backgroundColor = backgroundColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            // MODIFICAR: PADDING INTERNO (Controla los márgenes dentro de la caja)
                            .padding(horizontal = 14.dp, vertical = 10.dp) 
                            .animateContentSize()
                    ) {
                        content()
                    }
                }
            }

            // BOTÓN CERRAR (Solo X Roja con Shake)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color(0xFFEF4444), // Rojo Eléctrico / Cyber
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 24.dp)
                    .size(24.dp)
                    .shakeClick { onCloseClick() }
                    .zIndex(100f)
            )
        }
    }
}

// ==========================================================================================
// --- 4. BURBUJA INFERIOR (MODO TIPS / IDLE) ---
// ==========================================================================================

@Composable
fun BeBottomBubble(
    isVisible: Boolean,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    messages: List<BeMessage> = emptyList(),
    pagerState: PagerState? = null,
    onActionClick: () -> Unit = {},
    borderColor: Color = ElectricCyanColor,
    backgroundColor: Color = Color(0xFF0D0221)
) {
    // Si no hay mensajes, no renderizamos nada
    if (messages.isEmpty()) return

    // Fallback por si no pasan un PagerState (útil para previews simples)
    val finalPagerState = pagerState ?: rememberPagerState(pageCount = { messages.size })

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            transformOrigin = TransformOrigin(1f, 1f),
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)
        ) + fadeIn(),
        exit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut()
    ) {
        // ==========================================================================================
        // --- CONTENEDOR PRINCIPAL: ALINEACIÓN INFERIOR PARA CRECIMIENTO HACIA ARRIBA ---
        // ==========================================================================================
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .pointerInput(Unit) { detectTapGestures { } },
            contentAlignment = Alignment.BottomEnd // <--- ANCLA INFERIOR: La burbuja crece hacia ARRIBA
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // CUERPO NEON BOX
                    CyberMaverickNeonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 80.dp) // <--- MODIFICAR: ALTURA MÍNIMA BASE
                            .wrapContentHeight(),
                        accentColor = if (messages.size > 0) {
                            val msg = messages[finalPagerState.currentPage % messages.size]
                            if (msg.bubbleColor == Color.Gray) ElectricCyanColor else msg.bubbleColor
                        } else borderColor,
                        backgroundColor = backgroundColor
                    ) {
                        // USAMOS PAGER PARA EL CONTENIDO
                        HorizontalPager(
                            state = finalPagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val msg = messages[page % messages.size]
                            val resolvedBorderColor = if (msg.bubbleColor == Color.Gray) ElectricCyanColor else msg.bubbleColor

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // MODIFICAR: PADDING INTERNO (Controla los márgenes dentro de cada página del Pager)
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .animateContentSize()
                            ) {
                                // CABECERA REDISEÑADA: Emoji pequeño, Título y Contador
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(end = 24.dp) // Espacio para la X
                                ) {
                                    Text(msg.icon, fontSize = 14.sp) // Emoji más pequeño
                                    Spacer(Modifier.width(8.dp)) // MODIFICAR: ESPACIO ENTRE EMOJI Y TÍTULO
                                    BubbleM3Typography.Title("Be Tip", color = resolvedBorderColor)
                                    
                                    if (messages.size > 1) {
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = "${(finalPagerState.currentPage % messages.size) + 1} / ${messages.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.Gray,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp)) // MODIFICAR: ESPACIO ENTRE CABECERA Y TEXTO
                                BubbleM3Typography.Body(msg.text)

                                if (msg.actionText != null) {
                                    Spacer(Modifier.height(12.dp)) // MODIFICAR: ESPACIO SOBRE EL BOTÓN DE ACCIÓN
                                    Button(
                                        onClick = onActionClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = resolvedBorderColor),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(msg.actionText, color = msg.textColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // BOTÓN CERRAR (Solo X Roja con Shake)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 14.dp)
                            .size(24.dp)
                            .shakeClick { onCloseClick() }
                            .zIndex(100f)
                    )
                }

                // COLA TRIANGULAR
                TechTriangleTail(
                    position = BubbleTailPosition.BOTTOM_RIGHT,
                    modifier = Modifier.offset(x = (-70).dp, y = (-8).dp),
                    color = if (messages.size > 0) {
                        val msg = messages[finalPagerState.currentPage % messages.size]
                        if (msg.bubbleColor == Color.Gray) ElectricCyanColor else msg.bubbleColor
                    } else borderColor
                )
            }
        }
    }
}

// ==========================================================================================
// --- 5. ESTILOS DE TEXTO M3 PARA BURBUJAS ---
// ==========================================================================================

object BubbleM3Typography {
    @Composable
    fun Title(text: String, color: Color = Color.White) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            ),
            color = color
        )
    }

    @Composable
    fun Body(text: String, textAlign: TextAlign = TextAlign.Start) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp
            ),
            color = Color.White,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================================================================
// --- 6. PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun BeTopBubblePreview() {
    Surface(color = Color(0xFF0D0D12), modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(top = 100.dp)) {
            BeTopBubble(
                isVisible = true,
                onCloseClick = {}
            ) {
                BubbleM3Typography.Title("Asistente de Búsqueda", color = ElectricCyanColor)
                Spacer(Modifier.height(8.dp))
                BubbleM3Typography.Body("He encontrado 5 resultados para tu búsqueda de 'laptops gamer'.")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun BeBottomBubblePreview() {
    Surface(color = Color(0xFF0D0D12), modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            BeBottomBubble(
                isVisible = true,
                onCloseClick = {},
                messages = listOf(
                    BeMessage("✨", "Prueba de tip infinito 1", null, ElectricCyanColor),
                    BeMessage("🚀", "Prueba de tip infinito 2", "VAMOS", Color.Magenta)
                )
            )
        }
    }
}










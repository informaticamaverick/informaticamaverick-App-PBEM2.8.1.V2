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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.shakeClick
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: SHEET EMERGENTE VERTICAL (ELITE HUD MAVERICK) ---
 * ==========================================================================================
 * Molde premium adaptable para paneles emergentes con soporte para arrastre ajustable.
 */
@Composable
fun SheetEmergenteVertical(
    isVisible: Boolean,
    onClose: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    emoji: String? = null,
    topOffset: Dp = 150.dp,
    showEmoji: Boolean = true,
    showHelperText: Boolean = true,
    showTitle: Boolean = true,
    showActions: Boolean = true,
    isDraggable: Boolean = true, // 🔥 NUEVO: Control de arrastre
    isScrollable: Boolean = true, // 🔥 NUEVO: Control de scroll interno
    initialAnchorIsFull: Boolean = false, // 🔥 NUEVO: Control de subida inicial
    actions: @Composable RowScope.() -> Unit = {},
    onAnimationFinished: () -> Unit = {}, // 🔥 NUEVO: Callback de fin de animación de salida
    onEntryFinished: () -> Unit = {}, // 🔥 [ELITE] Callback de fin de animación de entrada
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    var screenHeight by remember { mutableFloatStateOf(0f) }
    
    // Estados de anclaje (en píxeles desde el tope)
    val hiddenAnchor = screenHeight
    val partialAnchor = screenHeight * 0.4f // 60% visible
    val fullAnchor = with(density) { topOffset.toPx() }

    // Offset actual animado usando Animatable para control total
    val animatableOffset = remember { Animatable(3000f) }
    var isInitialized by remember { mutableStateOf(false) }

    // --- 🛠️ ESTABILIZACIÓN MAVERICK (FIX GHOST SHEET) ---
    var lastVisible by remember { mutableStateOf(isVisible) }
    var isActuallyAnimatingOut by remember { mutableStateOf(false) }

    // [SSOT VISIBILIDAD] Determinamos si la sheet debe estar en la composición.
    // Combinamos el estado actual, el backup de animación y la detección instantánea de salida (evita el gap de 1 frame).
    val shouldBeComposed = isVisible || isActuallyAnimatingOut || (lastVisible && !isVisible)

    // Sincronización con isVisible y screenHeight
    LaunchedEffect(isVisible, screenHeight) {
        if (screenHeight > 0) {
            // Si es la primera vez que conocemos el alto, posicionamos en el fondo sin animar
            if (!isInitialized) {
                // Forzamos el snap al alto real capturado
                animatableOffset.snapTo(screenHeight)
                isInitialized = true
                android.util.Log.d("MAVERICK_HUD", "[$title] HUD Initialized at screenHeight: $screenHeight")
            }

            // Detectamos si estamos iniciando una animación de salida deliberada
            if (lastVisible && !isVisible) {
                isActuallyAnimatingOut = true
                android.util.Log.d("MAVERICK_HUD", "[$title] Starting deliberate close animation")
            }
            
            // Si se vuelve a poner visible mientras animaba hacia afuera, cancelamos el flag de salida
            if (!lastVisible && isVisible) {
                isActuallyAnimatingOut = false
            }

            lastVisible = isVisible

            val target = if (isVisible) {
                if (initialAnchorIsFull) fullAnchor else partialAnchor
            } else hiddenAnchor
            
            // Solo animamos si el valor actual es diferente al objetivo
            if (animatableOffset.value != target) {
                android.util.Log.d("MAVERICK_HUD", "[$title] Animating: ${animatableOffset.value} -> $target (isVisible=$isVisible)")
                animatableOffset.animateTo(
                    targetValue = target,
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                )
                
                // --- [ELITE] NOTIFICACIÓN DE ESTADO FINAL DE ANIMACIÓN ---
                if (isVisible && animatableOffset.value == target) {
                    android.util.Log.d("MAVERICK_HUD", "[$title] Entry animation finished")
                    onEntryFinished()
                }

                // Si la animación terminó y estamos en el anclaje oculto, notificamos
                if (animatableOffset.value >= hiddenAnchor && !isVisible) {
                    isActuallyAnimatingOut = false
                    android.util.Log.d("MAVERICK_HUD", "[$title] Animation finished, cleaning up")
                    onAnimationFinished()
                }
            }
        }
    }

    // [SECCIÓN: CONTENEDOR MAESTRO PARA CAPAS]
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Inicializador silencioso para capturar el screenHeight sin dibujar nada
        // Se mantiene SIEMPRE en la composición para reaccionar a cambios de pantalla/teclado
        Box(modifier = Modifier.fillMaxSize().onSizeChanged { 
            if (it.height > 0 && it.height.toFloat() != screenHeight) {
                screenHeight = it.height.toFloat() 
            }
        })

        // --- 🎭 CAPA 1: FONDO OSCURO TÁCTICO (FADE IN/OUT) ---
        if (shouldBeComposed && isInitialized) {
            val alpha = remember(animatableOffset.value, screenHeight, fullAnchor, hiddenAnchor) {
                if (screenHeight == 0f || hiddenAnchor == fullAnchor) 0f
                else ((hiddenAnchor - animatableOffset.value) / (hiddenAnchor - fullAnchor) * 0.85f).coerceIn(0f, 0.85f)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaverickColors.ROG_Dark_Bg.copy(alpha = alpha))
                    .pointerInput(Unit) {
                        // BLOQUEO TOTAL: Detectamos taps para cerrar, pero el pointerInput consume el resto
                        // evitando que los eventos de scroll o touch lleguen a la capa inferior.
                        detectTapGestures { onClose() }
                    }
            )
        }

        // --- 🏗️ CAPA 2: EL CONTENIDO DE LA SHEET (DRAGGABLE) ---
        if (shouldBeComposed && isInitialized) {
            val scope = rememberCoroutineScope()
            Box(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, animatableOffset.value.roundToInt()) }
                    .then(
                        if (isDraggable && screenHeight > 0) {
                            Modifier.draggable(
                                orientation = Orientation.Vertical,
                                state = rememberDraggableState { delta ->
                                    scope.launch {
                                        animatableOffset.snapTo((animatableOffset.value + delta).coerceIn(fullAnchor, hiddenAnchor))
                                    }
                                },
                                onDragStopped = {
                                    val target = if (animatableOffset.value < (partialAnchor + fullAnchor) / 2) fullAnchor
                                                else if (animatableOffset.value < (hiddenAnchor + partialAnchor) / 2) partialAnchor
                                                else { onClose(); hiddenAnchor }
                                    
                                    scope.launch {
                                        animatableOffset.animateTo(
                                            targetValue = target,
                                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                                        )
                                    }
                                }
                            )
                        } else Modifier
                    )
            ) {
                EliteHudContainer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = with(density) { (screenHeight - partialAnchor).toDp() })
                        .height(with(density) { (screenHeight - fullAnchor).toDp() })
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // --- 1. CABECERA UNIFICADA TÁCTICA (ELITE HUD GLASS) ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                                    )
                                )
                                .drawBehind {
                                    // RIM LIGHTING: Brillo sutil en el borde superior
                                    drawLine(
                                        color = MaverickColors.ElectricCyan.copy(alpha = 0.4f),
                                        start = Offset(0f, 0.5.dp.toPx()),
                                        end = Offset(size.width, 0.5.dp.toPx()),
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                    
                                    // BORDE INFERIOR DE CONTRASTE (Diferenciación de la lista)
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.15f),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Handle de arrastre integrado
                               // EliteHudDragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (showEmoji && emoji != null) {
                                        Text(
                                            text = emoji, 
                                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (showHelperText && helperText != null) {
                                            Text(
                                                text = helperText.uppercase(),
                                                style = CyberTypography.MonospaceData.copy(
                                                    color = MaverickColors.ElectricPurple.copy(alpha = 0.7f),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.2.sp
                                                )
                                            )
                                        }
                                        if (showTitle) {
                                            AutoSizeText(
                                                text = title.uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = 18.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 1.2.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        if (showActions) {
                                            actions()
                                        }
                                        SheetCloseButton(onClick = onClose)
                                    }
                                }
                            }
                        }

                        // --- 2. ÁREA DE CONTENIDO UNIFICADA (BLUEPRINT + SHADOW OVERLAY) ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            // CONTENIDO CON SCROLL
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (isScrollable) Modifier.verticalScroll(rememberScrollState()) 
                                        else Modifier
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 32.dp), // Solo padding inferior para que el final no quede pegado
                                    content = { content() }
                                )
                            }

                            // --- SOMBRA TÉCNICA (Overlay fijo al tope del contenido) ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(15.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                                        )
                                    )
                                    .zIndex(10f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** COMPONENTE: Contenedor Maestro Elite HUD */
@Composable
private fun EliteHudContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.98f)) // Glassmorphism 2.0
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val cutSize = 10.dp.toPx()

                // 1. GRADIENTE MAVERICK HORIZONTAL
                val borderGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaverickColors.ElectricCyan,
                        MaverickColors.ElectricPurple,
                        MaverickColors.ElectricCyan,
                        Color.Transparent
                    )
                )

                // 2. DIBUJAR BASE SUPERIOR (Cortes y tapa horizontal)
                val basePath = Path().apply {
                    moveTo(0f, cutSize)
                    lineTo(cutSize, 0f)
                    lineTo(size.width - cutSize, 0f)
                    lineTo(size.width, cutSize)
                }
                
                // 3. Borde Principal
                drawPath(path = basePath, brush = borderGradient, style = Stroke(width = strokeWidth))

                // 4. Glow Tenue Adaptativo
                drawPath(path = basePath, color = MaverickColors.ElectricCyan.copy(alpha = 0.15f), style = Stroke(width = strokeWidth * 2.5f))
                
                // Glow ambiental superior (Aura central)
                drawPath(path = basePath, brush = borderGradient, style = Stroke(width = strokeWidth * 5f), alpha = 0.08f)
            }
    ) {
        content()
    }
}

/** COMPONENTE: Handle de Arrastre Estilo Minimalista Moderno
@Composable
private fun EliteHudDragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .size(36.dp, 4.dp)
            .background(Color.White.copy(alpha = 0.2f), CircleShape)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
    )
}
*/



/** COMPONENTE: Brackets tácticos en las esquinas
@Composable
private fun TacticalBrackets() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineLen = 14.dp.toPx()
        val thickness = 1.2.dp.toPx()
        val color = MaverickColors.ElectricCyan.copy(alpha = 0.4f)
        val offset = 6.dp.toPx() // Un poco más de margen para que no choquen con el corte
        
        // Esquina Superior Izquierda (Ajustada al corte de 10dp)
        val startX = 10.dp.toPx().coerceAtLeast(offset)
        drawLine(color, Offset(startX, offset), Offset(startX + lineLen, offset), thickness)
        drawLine(color, Offset(offset, startX), Offset(offset, startX + lineLen), thickness)
        
        // Esquina Superior Derecha
        val endX = size.width - 10.dp.toPx().coerceAtMost(size.width - offset)
        drawLine(color, Offset(endX, offset), Offset(endX - lineLen, offset), thickness)
        drawLine(color, Offset(size.width - offset, startX), Offset(size.width - offset, startX + lineLen), thickness)

        // Esquina Inferior Izquierda
        drawLine(color, Offset(offset, size.height - offset), Offset(offset + lineLen, size.height - offset), thickness)
        drawLine(color, Offset(offset, size.height - offset), Offset(offset, size.height - offset - lineLen), thickness)

        // Esquina Inferior Derecha
        drawLine(color, Offset(size.width - offset, size.height - offset), Offset(size.width - offset - lineLen, size.height - offset), thickness)
        drawLine(color, Offset(size.width - offset, size.height - offset), Offset(size.width - offset, size.height - offset - lineLen), thickness)
    }
}
*/
/**
 * HELPER: Botón de cierre estilo círculo táctico rojo
 */
@Composable
fun SheetCloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(width = 1.dp, color = Color(0xFFEF4444).copy(alpha = 0.5f), shape = CircleShape)
            .background(color = Color(0xFFEF4444).copy(alpha = 0.1f), shape = CircleShape)
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * HELPER: Botón de acción circular premium para la Sheet
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
                    color = if (active) MaverickColors.ElectricCyan.copy(alpha = 0.4f)
                            else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }
        
        Text(
            text = label.uppercase(),
            style = CyberTypography.MonospaceData.copy(
                fontSize = 6.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) MaverickColors.ElectricCyan else Color.Gray.copy(alpha = 0.6f)
            ),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun SheetEmergenteVerticalPreview() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF05070A))) {
        // Usamos una versión simplificada para el preview que no dependa de screenHeight animado
        // para asegurar que se vea algo en la herramienta de renderizado.
        EliteHudContainer(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(400.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              //  EliteHudDragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️", fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SISTEMAS V2.4", style = CyberTypography.MonospaceData.copy(fontSize = 9.sp, color = MaverickColors.ElectricPurple))
                        Text("MÓDULO CONFIG", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black))
                    }
                    SheetCloseButton(onClick = {})
                }

                Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("CONTENIDO DE PRUEBA HUD", color = Color.White.copy(alpha = 0.5f))
                    //TacticalBrackets()
                }
            }
        }
    }
}










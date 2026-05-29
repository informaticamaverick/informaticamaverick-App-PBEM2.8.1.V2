package com.example.myapplication.presentation.designsystem.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeCap
import com.example.myapplication.presentation.components.BeEmotion

// ==========================================================================================
// --- SECCIÓN: ARBOLES DE DIRECTORIO Y MENÚS ---
// ==========================================================================================

data class FileNode(
    val name: String,
    val isDirectory: Boolean,
    val children: List<FileNode> = emptyList(),
    val icon: ImageVector? = null,
    val tint: Color? = null, // Para el color gris/cyan
    val alpha: Float = 1f    // Para el efecto "apagado"
)

@Composable
fun DirectoryTree(
    nodes: List<FileNode>,
    modifier: Modifier = Modifier,
    onNodeClick: (FileNode) -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        nodes.forEach { node ->
            TacticalFileNodeItem(node, onNodeClick = onNodeClick)
        }
    }
}

@Composable
private fun TacticalFileNodeItem(
    node: FileNode,
    level: Int = 0,
    onNodeClick: (FileNode) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "rot")
    val alphaAnim by animateFloatAsState(if (isExpanded) 1f else 0.7f, label = "alpha")
    val finalAlpha = alphaAnim * node.alpha

    val accentColor = node.tint ?: if (node.isDirectory) MaverickColors.NeonCyan else MaverickColors.TextMuted

    Column(modifier = Modifier.alpha(finalAlpha)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (node.isDirectory) isExpanded = !isExpanded
                    onNodeClick(node)
                }
                .padding(start = (level * 20).dp, top = 6.dp, bottom = 6.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Guía visual (línea vertical de jerarquía)
            if (level > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .padding(start = 8.dp)
                        .background(MaverickColors.TextMuted.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            if (node.isDirectory) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation),
                    tint = accentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).alpha(alphaAnim),
                    tint = accentColor
                )
            } else {
                // Punto de conexión para hojas
                Canvas(modifier = Modifier.size(14.dp)) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.5f),
                        radius = 2.dp.toPx(),
                        center = center
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = node.icon ?: Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = node.name.uppercase(),
                    color = if (node.isDirectory) Color.White else MaverickColors.TextMain,
                    fontSize = 11.sp,
                    fontWeight = if (node.isDirectory) FontWeight.ExtraBold else FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (!node.isDirectory) {
                    Text(
                        text = "ENTRY_NODE",
                        color = accentColor.copy(alpha = 0.4f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = node.isDirectory && isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                node.children.forEach { child ->
                    TacticalFileNodeItem(child, level + 1, onNodeClick)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun DirectoryTreePreview() {
    val sampleTree = listOf(
        FileNode(
            "app", true, listOf(
                FileNode("src", true, listOf(
                    FileNode("main", true, listOf(
                        FileNode("java", true),
                        FileNode("res", true)
                    ))
                )),
                FileNode("build.gradle", false)
            )
        ),
        FileNode("gradle", true),
        FileNode("settings.gradle", false)
    )

    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DirectoryTree(nodes = sampleTree)
        }
    }
}

// ==========================================================================================
// --- SECCIÓN: MENU CP (HÍBRIDO ROG/GHOST) ---
// ==========================================================================================

/**
 * MenuCP: Contenedor de menú estilo Cyberpunk.
 * Mezcla la geometría de ChatBubbleRogElite con el estilo visual de ChatBubbleGhost.
 */
@Composable
fun MenuCP(
    isVisible: Boolean,
    title: String,
    headerEmoji: String = "",
    headerColor: Color = CyberColorsV3.ElectricCyan,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = CutCornerShape(topStart = 0.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = modifier.padding(start = 24.dp, top = 12.dp)) {
            Box(
                modifier = Modifier
                    .widthIn(min = 240.dp)
                    .drawBehind {
                        val path = Path().apply {
                            moveTo(10.dp.toPx(), 0f)
                            lineTo(20.dp.toPx(), (-10).dp.toPx())
                            lineTo(30.dp.toPx(), 0f)
                            close()
                        }
                        drawPath(path, headerColor.copy(alpha = 0.1f))
                    }
                    .background(
                        color = MaverickColors.CyberBackground,
                        shape = shape
                    )
                    .border(
                        width = 1.dp,
                        color = headerColor.copy(alpha = 0.3f),
                        shape = shape
                    )
            ) {
                Column(
                    modifier = Modifier
                        .clip(shape)
                        .padding(12.dp)
                ) {
                    // HEADER CON TÍTULO Y BOTÓN DE CIERRE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (headerEmoji.isNotEmpty()) {
                                Text(headerEmoji, modifier = Modifier.padding(end = 8.dp))
                            }
                            Text(
                                text = title.uppercase(),
                                color = headerColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        // BOTÓN DE CIERRE (X) - Centrado a la derecha de la cabecera
                      //  BtnCancelStealth(
                       //     onClick = onDismiss
                       // )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = headerColor.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    content()
                }
            }
        }
    }
}


/**
 * MenuTacticoShortcut: Menú emergente personalizado con forma circular (Elite Style).
 * Diseñado para la gestión rápida de favoritos (Shortcuts) con estética minimalista y moderna.
 * Se abre dinámicamente sobre el punto de presión si se proporciona un offset.
 */
@Composable
fun MenuTacticoShortcut(
    isVisible: Boolean,
    isShortcut: Boolean,
    onDismissRequest: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    touchOffset: Offset? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopupScale"
    )

    // Offset vertical para la animación de "salida" hacia arriba
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) (-10).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "PopupOffset"
    )

    if (isVisible || scale > 0.01f) {
        val popupWidth = 84.dp
        val circleSize = 84.dp
        val arrowHeight = 12.dp // Aumentado ligeramente
        val gap = 6.dp // Espacio entre el círculo y la flecha
        val popupHeight = circleSize + arrowHeight + gap
        val density = LocalDensity.current

        // Calculamos la posición final basándonos en el touchOffset si existe
        val finalOffset = density.run {
            if (touchOffset != null) {
                val pxWidth = popupWidth.toPx()
                val pxHeight = popupHeight.toPx()
                val pxExtraOffset = 12.dp.toPx() // Aumentado para evitar que el dedo tape el menú

                IntOffset(
                    x = (touchOffset.x - pxWidth / 2).toInt(),
                    y = (touchOffset.y - pxHeight - pxExtraOffset).toInt() + animatedOffsetY.toPx().toInt()
                )
            } else {
                IntOffset(0, (-80).dp.toPx().toInt() + animatedOffsetY.toPx().toInt())
            }
        }

        Popup(
            alignment = if (touchOffset != null) Alignment.TopStart else Alignment.TopCenter,
            offset = finalOffset,
            properties = PopupProperties(
                focusable = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismissRequest
        ) {
            Box(
                modifier = modifier
                    .size(popupWidth, popupHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale.coerceIn(0f, 1f)
                        transformOrigin = TransformOrigin(0.5f, 1f) // Expande desde la punta de la flecha
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // --- FLECHA INFERIOR REFINADA (Curva y con Borde) ---
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(24.dp, arrowHeight)
                        .graphicsLayer {
                            // Sombra para la flecha
                            shadowElevation = 16.dp.toPx()
                            shape = GenericShape { size, _ ->
                                val path = Path().apply {
                                    moveTo(0f, 0f)
                                    // Base curva siguiendo la estética del círculo
                                    quadraticTo(size.width / 2f, 4.dp.toPx(), size.width, 0f)
                                    lineTo(size.width / 2f, size.height)
                                    close()
                                }
                                addPath(path)
                            }
                        }
                ) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        quadraticTo(size.width / 2f, 4.dp.toPx(), size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }
                    // Relleno Mate
                    drawPath(path, Color(0xFF1D1B20))
                    // Borde idéntico al del círculo
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.12f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // --- FONDO CIRCULAR MATE CON SOMBRA 3D PROFUNDA (Imagen Ref) ---
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            clip = false,
                            spotColor = Color.Black.copy(alpha = 0.8f)
                        )
                        .clip(CircleShape)
                        .background(Color(0xFF1D1B20)) // Color sólido mate profundo
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .shakeClick {
                            onAction()
                            onDismissRequest()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // --- ICONO "+" / "-" 3D VOLUMÉTRICO ---
                        Canvas(modifier = Modifier.size(36.dp)) {
                            val strokeWidth = 8.dp.toPx()
                            val colorPlusTop = MaverickColors.GeminiAccent
                            val colorPlusSide = MaverickColors.GeminiAccent.copy(alpha = 0.6f)
                            val colorMinusTop = MaverickColors.WarningRed
                            val colorMinusSide = MaverickColors.WarningRed.copy(alpha = 0.6f)

                            if (!isShortcut) {
                                // Dibujamos el "+" con efecto de volumen (Grosor y puntas redondeadas)
                                // Sombra/Lado para efecto 3D
                                drawLine(
                                    color = colorPlusSide,
                                    start = Offset(0f, size.height / 2 + 2.dp.toPx()),
                                    end = Offset(size.width, size.height / 2 + 2.dp.toPx()),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                // Frente
                                drawLine(
                                    brush = Brush.verticalGradient(listOf(colorPlusTop, colorPlusTop.copy(alpha = 0.8f))),
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                // Vertical Sombra/Lado
                                drawLine(
                                    color = colorPlusSide,
                                    start = Offset(size.width / 2 + 2.dp.toPx(), 0f),
                                    end = Offset(size.width / 2 + 2.dp.toPx(), size.height),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                // Vertical Frente
                                drawLine(
                                    brush = Brush.horizontalGradient(listOf(colorPlusTop, colorPlusTop.copy(alpha = 0.8f))),
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            } else {
                                // Dibujamos un "-" grueso con efecto 3D
                                drawLine(
                                    color = colorMinusSide,
                                    start = Offset(4.dp.toPx(), size.height / 2 + 2.dp.toPx()),
                                    end = Offset(size.width - 4.dp.toPx(), size.height / 2 + 2.dp.toPx()),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    brush = Brush.verticalGradient(listOf(colorMinusTop, colorMinusTop.copy(alpha = 0.8f))),
                                    start = Offset(4.dp.toPx(), size.height / 2),
                                    end = Offset(size.width - 4.dp.toPx(), size.height / 2),
                                    strokeWidth = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isShortcut) "QUITAR" else "AGREGAR",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * BeMenuItem: Modelo de datos para las opciones de los menús de Be.
 */
data class BeMenuItem(
    val label: String,
    val icon: ImageVector,
    val tint: Color = Color.White,
    val onClick: () -> Unit
)

/**
 * MenuTacticoBe: Menú emergente con la identidad visual del asistente Be.
 * Actúa como el "Trigger" o ancla para otros menús (Vertical u Horizontal).
 */
@Composable
fun MenuTacticoBe(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    touchOffset: Offset? = null,
    emotion: BeEmotion = BeEmotion.NORMAL,
    actionLabel: String = "ACCIONES",
    actionIconEmoji: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopupScale"
    )

    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) (-10).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "PopupOffset"
    )

    if (isVisible || scale > 0.01f) {
        val popupWidth = 92.dp
        val circleSize = 72.dp
        val arrowHeight = 12.dp
        val gap = (-4).dp
        val buttonHeight = 36.dp
        val buttonSpacer = 8.dp
        val popupHeight = buttonHeight + buttonSpacer + circleSize + arrowHeight + gap
        val density = LocalDensity.current
        val arrowCurvePx = with(density) { 4.dp.toPx() }

        val finalOffset = density.run {
            if (touchOffset != null) {
                val pxWidth = popupWidth.toPx()
                val pxHeight = popupHeight.toPx()
                val pxExtraOffset = 24.dp.toPx()

                IntOffset(
                    x = (touchOffset.x - pxWidth / 2).toInt(),
                    y = (touchOffset.y - pxHeight - pxExtraOffset).toInt() + animatedOffsetY.toPx().toInt()
                )
            } else {
                IntOffset(0, (-100).dp.toPx().toInt() + animatedOffsetY.toPx().toInt())
            }
        }

        Popup(
            alignment = Alignment.TopStart,
            offset = finalOffset,
            properties = PopupProperties(
                focusable = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismissRequest
        ) {
            Box(
                modifier = modifier
                    .size(popupWidth, popupHeight)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale.coerceIn(0f, 1f)
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // --- FLECHA INFERIOR REFINADA (Con Sombra 3D) ---
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(24.dp, arrowHeight)
                        .shadow(
                            elevation = 8.dp,
                            shape = GenericShape { size, _ ->
                                moveTo(0f, 0f)
                                quadraticTo(size.width / 2f, arrowCurvePx, size.width, 0f)
                                lineTo(size.width / 2f, size.height)
                                close()
                            },
                            clip = false,
                            spotColor = Color.Black
                        )
                ) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        quadraticTo(size.width / 2f, 4.dp.toPx(), size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }

                    drawPath(path, Color(0xFF393B40))

                    val innerPath = Path().apply {
                        moveTo(size.width * 0.35f, size.height * 0.45f)
                        lineTo(size.width * 0.65f, size.height * 0.45f)
                        lineTo(size.width / 2f, size.height * 0.9f)
                        close()
                    }
                    drawPath(innerPath, MaverickColors.NeonCyan)

                    drawPath(
                        path = path,
                        color = Color(0xFF1E293B).copy(alpha = 0.8f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // --- BOTÓN M3 MODERNO OSCURO (MATE) ---
                    Button(
                        onClick = {
                            onAction()
                        },
                        modifier = Modifier.height(buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D1B20),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        ),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (actionIconEmoji != null) {
                                Text(text = actionIconEmoji, fontSize = 12.sp)
                            }
                            Text(
                                text = actionLabel.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(buttonSpacer))

                    // --- CUERPO CIRCULAR (CARA DE BE) ---
                    Box(
                        modifier = Modifier
                            .size(circleSize)
                            .clip(CircleShape)
                            .shakeClick {
                                onAction()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFF0A0E14),
                                radius = size.width * 0.38f,
                                center = center
                            )

                            drawCircle(
                                color = MaverickColors.NeonCyan,
                                radius = size.width * 0.34f,
                                center = center,
                                style = Stroke(width = size.width * 0.035f)
                            )

                            drawArc(
                                color = Color.White.copy(alpha = 0.2f),
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = false,
                                style = Stroke(width = size.width * 0.025f, cap = StrokeCap.Round),
                                topLeft = Offset(size.width * 0.22f, size.height * 0.22f),
                                size = Size(size.width * 0.56f, size.height * 0.56f)
                            )

                            val eyeScaleY = if (emotion == BeEmotion.SLEEPING) 0.1f else 1f

                            if (emotion == BeEmotion.HAPPY) {
                                drawPath(
                                    Path().apply {
                                        moveTo(size.width * 0.33f, size.height * 0.5f)
                                        quadraticTo(size.width * 0.40f, size.height * 0.38f, size.width * 0.47f, size.height * 0.5f)
                                    },
                                    color = Color.White,
                                    style = Stroke(width = size.width * 0.05f, cap = StrokeCap.Round)
                                )
                                drawPath(
                                    Path().apply {
                                        moveTo(size.width * 0.53f, size.height * 0.5f)
                                        quadraticTo(size.width * 0.60f, size.height * 0.38f, size.width * 0.67f, size.height * 0.5f)
                                    },
                                    color = Color.White,
                                    style = Stroke(width = size.width * 0.05f, cap = StrokeCap.Round)
                                )
                            } else {
                                drawOval(
                                    color = Color.White,
                                    topLeft = Offset(size.width * 0.31f, size.height * 0.5f - (size.height * 0.11f * eyeScaleY)),
                                    size = Size(size.width * 0.15f, size.height * 0.22f * eyeScaleY)
                                )
                                drawOval(
                                    color = Color.White,
                                    topLeft = Offset(size.width * 0.54f, size.height * 0.5f - (size.height * 0.11f * eyeScaleY)),
                                    size = Size(size.width * 0.15f, size.height * 0.22f * eyeScaleY)
                                )

                                if (emotion != BeEmotion.SLEEPING) {
                                    val pupilRadius = if (emotion == BeEmotion.SURPRISED) size.width * 0.025f else size.width * 0.045f
                                    drawCircle(Color(0xFF05070A), pupilRadius * eyeScaleY, Offset(size.width * 0.385f, size.height * 0.5f))
                                    drawCircle(Color.White, size.width * 0.012f * eyeScaleY, Offset(size.width * 0.395f, size.height * 0.485f))
                                    drawCircle(Color(0xFF05070A), pupilRadius * eyeScaleY, Offset(size.width * 0.615f, size.height * 0.5f))
                                    drawCircle(Color.White, size.width * 0.012f * eyeScaleY, Offset(size.width * 0.625f, size.height * 0.485f))
                                }

                                when (emotion) {
                                    BeEmotion.ANGRY -> {
                                        drawLine(MaverickColors.NeonCyan, Offset(size.width * 0.28f, size.height * 0.36f), Offset(size.width * 0.46f, size.height * 0.42f), strokeWidth = size.width * 0.05f, cap = StrokeCap.Round)
                                        drawLine(MaverickColors.NeonCyan, Offset(size.width * 0.72f, size.height * 0.36f), Offset(size.width * 0.54f, size.height * 0.42f), strokeWidth = size.width * 0.05f, cap = StrokeCap.Round)
                                    }
                                    BeEmotion.SURPRISED -> {
                                        drawArc(MaverickColors.NeonCyan, 180f, 180f, false, Offset(size.width * 0.32f, size.height * 0.32f), Size(size.width * 0.16f, size.height * 0.10f), style = Stroke(size.width * 0.035f, cap = StrokeCap.Round))
                                        drawArc(MaverickColors.NeonCyan, 180f, 180f, false, Offset(size.width * 0.52f, size.height * 0.32f), Size(size.width * 0.16f, size.height * 0.10f), style = Stroke(size.width * 0.035f, cap = StrokeCap.Round))
                                    }
                                    BeEmotion.SAD -> {
                                        drawLine(MaverickColors.NeonCyan.copy(alpha = 0.8f), Offset(size.width * 0.30f, size.height * 0.42f), Offset(size.width * 0.44f, size.height * 0.38f), strokeWidth = size.width * 0.04f, cap = StrokeCap.Round)
                                        drawLine(MaverickColors.NeonCyan.copy(alpha = 0.8f), Offset(size.width * 0.70f, size.height * 0.42f), Offset(size.width * 0.56f, size.height * 0.38f), strokeWidth = size.width * 0.04f, cap = StrokeCap.Round)
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ConcavePillShape: Forma de píldora con un recorte cóncavo en un lateral.
 * Se usa para que el primer ítem del menú "abrace" visualmente la cabeza de Be.
 */
class ConcavePillShape(
    private val isOnRightSide: Boolean,
    private val cutoutRadius: Float = 36f // Ajustar según el tamaño de Be
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val r = size.height / 2f
            if (!isOnRightSide) {
                // El menú está a la DERECHA de Be -> Recorte en la IZQUIERDA
                moveTo(0f, 0f)
                // Arco cóncavo (hacia adentro)
                arcTo(
                    rect = Rect(-cutoutRadius, -cutoutRadius * 0.2f, cutoutRadius, size.height + cutoutRadius * 0.2f),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                lineTo(size.width - r, 0f)
                arcTo(Rect(size.width - 2 * r, 0f, size.width, size.height), -90f, 180f, false)
                close()
            } else {
                // El menú está a la IZQUIERDA de Be -> Recorte en la DERECHA
                moveTo(r, 0f)
                lineTo(size.width, 0f)
                arcTo(
                    rect = Rect(size.width - cutoutRadius, -cutoutRadius * 0.2f, size.width + cutoutRadius, size.height + cutoutRadius * 0.2f),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                lineTo(r, size.height)
                arcTo(Rect(0f, 0f, 2 * r, size.height), 90f, 180f, false)
                close()
            }
        }
        return Outline.Generic(path)
    }
}


/**
 * MenuHerramientasBe: Barra de herramientas horizontal para acciones rápidas.
 * Estilo minimalista con elevación 3D profunda.
 */
@Composable
fun MenuHerramientasBe(
    isVisible: Boolean,
    items: List<BeMenuItem>,
    onDismissRequest: () -> Unit,
    anchorOffset: Offset?,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "ToolBarScale"
    )

    if (isVisible || scale > 0.01f) {
        val density = LocalDensity.current
        val menuOffset = density.run {
            if (anchorOffset != null) {
                IntOffset(
                    (anchorOffset.x - (items.size * 48.dp.toPx() / 2)).toInt(),
                    (anchorOffset.y - 100.dp.toPx()).toInt()
                )
            } else {
                IntOffset(0, 0)
            }
        }

        Popup(
            alignment = Alignment.TopStart,
            offset = menuOffset,
            onDismissRequest = onDismissRequest
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp), // Más redondeado estilo Android 16
                color = Color(0xFF1D1B20), // MATE PROFUNDO
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = scale
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    }
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color.Black.copy(alpha = 0.95f)
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        IconButton(
                            onClick = {
                                item.onClick()
                                onDismissRequest()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = item.tint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * BotonMensajeBe: (OBSOLETO) Reemplazo del Snackbar M3 para un encaje perfecto con Be.
 * Se mantiene por compatibilidad hasta que se migren todos los usos.
 */
@Composable
fun BotonMensajeBe(
    modifier: Modifier = Modifier,
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    concaveSide: Alignment.Horizontal? = null
) {
    val shape = remember(concaveSide) {
        GenericShape { size, _ ->
            val r = size.height / 2f
            val cutoutRadius = size.height * 0.85f

            if (concaveSide == Alignment.Start) {
                // LADO IZQUIERDO CÓNCAVO
                moveTo(0f, 0f)
                arcTo(
                    rect = Rect(-cutoutRadius * 0.6f, -cutoutRadius * 0.1f, cutoutRadius * 0.6f, size.height + cutoutRadius * 0.1f),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                lineTo(size.width - r, 0f)
                arcTo(Rect(size.width - 2 * r, 0f, size.width, size.height), -90f, 180f, false)
                lineTo(cutoutRadius * 0.3f, size.height)
                close()
            } else if (concaveSide == Alignment.End) {
                // LADO DERECHO CÓNCAVO
                moveTo(r, 0f)
                lineTo(size.width, 0f)
                arcTo(
                    rect = Rect(size.width - cutoutRadius * 0.6f, -cutoutRadius * 0.1f, size.width + cutoutRadius * 0.6f, size.height + cutoutRadius * 0.1f),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = -180f,
                    forceMoveTo = false
                )
                lineTo(r, size.height)
                arcTo(Rect(0f, 0f, 2 * r, size.height), 90f, 180f, false)
                close()
            } else {
                addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r, r)))
            }
        }
    }

    Surface(
        modifier = modifier
            .shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.5f))
            .border(0.5.dp, Color.White.copy(alpha = 0.25f), shape)
            .clip(shape),
        color = Color(0xFF1D1B20),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = if (concaveSide == Alignment.Start) 24.dp else 16.dp,
                    end = if (concaveSide == Alignment.End) 24.dp else 16.dp,
                    top = 4.dp,
                    bottom = 4.dp
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.weight(1f),
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (actionLabel != null) {
                TextButton(
                    onClick = onAction,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaverickColors.GeminiAccent
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = actionLabel.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuVerticalAddBePreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.size(400.dp, 600.dp), contentAlignment = Alignment.Center) {
            // Simulamos el menú sin Popup para la preview
            Column(
                modifier = Modifier
                    .width(180.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val items = listOf(
                    BeMenuItem("Document", Icons.Default.Folder, MaverickColors.NeonCyan) {},
                    BeMenuItem("Message", Icons.AutoMirrored.Filled.InsertDriveFile, MaverickColors.GeminiAccent) {},
                    BeMenuItem("Folder", Icons.Default.Folder, MaverickColors.WarningRed) {}
                )
                items.forEachIndexed { index, item ->
                    val isFirst = index == 0
                    val shape = if (isFirst) ConcavePillShape(isOnRightSide = true) else CircleShape
                    Surface(
                        onClick = {},
                        shape = shape,
                        color = Color(0xFF1D1B20),
                        modifier = Modifier
                            .shadow(
                                elevation = 16.dp,
                                shape = shape,
                                spotColor = Color.Black.copy(alpha = 0.9f)
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.12f), shape)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = if (isFirst) 24.dp else 20.dp,
                                top = 12.dp,
                                bottom = 12.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.tint,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = item.label.uppercase(),
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuHerramientasBePreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.size(300.dp, 150.dp), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF1D1B20),
                modifier = Modifier
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color.Black.copy(alpha = 0.95f)
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val items = listOf(
                        BeMenuItem("Add", Icons.Default.Folder, Color.White) {},
                        BeMenuItem("Delete", Icons.Default.Folder, MaverickColors.WarningRed) {},
                        BeMenuItem("Edit", Icons.Default.Folder, MaverickColors.NeonCyan) {}
                    )
                    items.forEach { item ->
                        IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = item.tint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista previa puramente visual sin Popup para asegurar el renderizado
 */
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuTacticoBeVisualPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .padding(20.dp)
                .size(100.dp, 120.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Contenido manual simplificado para la preview
            val circleSize = 72.dp
            val arrowHeight = 12.dp

            // Flecha
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(24.dp, arrowHeight)
            ) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    quadraticTo(size.width / 2f, 4.dp.toPx(), size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                }
                // Base
                drawPath(path, Color(0xFF393B40))
                // Interior Cyan
                val innerPath = Path().apply {
                    moveTo(size.width * 0.35f, size.height * 0.45f)
                    lineTo(size.width * 0.65f, size.height * 0.45f)
                    lineTo(size.width / 2f, size.height * 0.9f)
                    close()
                }
                drawPath(innerPath, MaverickColors.NeonCyan)
                // Borde
                drawPath(path, Color(0xFF1E293B), style = Stroke(2.dp.toPx()))
            }

            // Cara de Be
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // REPLICACIÓN EXACTA DE BeAssistant.kt
                    drawCircle(
                        color = Color(0xFF0A0E14),
                        radius = size.width * 0.38f,
                        center = center
                    )
                    drawCircle(
                        color = MaverickColors.NeonCyan,
                        radius = size.width * 0.34f,
                        center = center,
                        style = Stroke(width = size.width * 0.035f)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.2f),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = size.width * 0.025f, cap = StrokeCap.Round),
                        topLeft = Offset(size.width * 0.22f, size.height * 0.22f),
                        size = Size(size.width * 0.56f, size.height * 0.56f)
                    )
                    drawOval(Color.White, Offset(size.width * 0.31f, size.height * 0.5f - size.height * 0.11f), Size(size.width * 0.15f, size.height * 0.22f))
                    drawOval(Color.White, Offset(size.width * 0.54f, size.height * 0.5f - size.height * 0.11f), Size(size.width * 0.15f, size.height * 0.22f))
                    drawCircle(Color(0xFF05070A), size.width * 0.045f, Offset(size.width * 0.385f, size.height * 0.5f))
                    drawCircle(Color.White, size.width * 0.012f, Offset(size.width * 0.395f, size.height * 0.485f))
                    drawCircle(Color(0xFF05070A), size.width * 0.045f, Offset(size.width * 0.615f, size.height * 0.5f))
                    drawCircle(Color.White, size.width * 0.012f, Offset(size.width * 0.625f, size.height * 0.485f))
                }
            }
        }
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuTacticoShortcutPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            MenuTacticoShortcut(
                isVisible = true,
                isShortcut = false,
                onDismissRequest = {},
                onAction = {},
                touchOffset = Offset(200f, 400f) // Ejemplo de offset
            )
        }
    }
}

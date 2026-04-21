package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.drawscope.StrokeCap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.client.BeSearchReaction
import androidx.compose.ui.window.Popup
import com.example.myapplication.presentation.components.Utilidades.CyberColorsV3
import com.example.myapplication.presentation.components.Utilidades.MenuCP
import com.example.myapplication.presentation.components.Utilidades.CyberTypography
import com.example.myapplication.presentation.components.Utilidades.MaverickColors//.BentoDarkGlassBackground
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoDarkGlassBackground
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoGlassBrush
//import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoGlassBrush
import com.example.myapplication.presentation.registry.BeDictionary
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// ==========================================================================================
// --- 1. MODELOS DE ESTADO (Centralizados en BeBrainViewModel) ---
// ==========================================================================================
enum class BeState {
    IDLE,               // Reposo
    NOTIFICATION_READY, // Alerta (Badge)
    TALKING             // Mostrando burbuja
}
enum class BeEmotion {
    NORMAL, HAPPY, SURPRISED, ANGRY, THINKING, SLEEPING, BLUSHING, SAD
}
data class BeMessage(
    val icon: String,
    val text: String,
    val actionText: String? = null,
    val bubbleColor: Color,
    val textColor: Color = Color(0xFF05070A),
    val emotion: BeEmotion = BeEmotion.NORMAL,
    val isCentered: Boolean = false
)

// --- Paleta ROG Local para evitar conflictos si no están en scope ---
private val LocalROG_Red = Color(0xFFFF0032)
private val LocalROG_Cyan = Color(0xFF00FFFF)
private val LocalDarkBackground = Color(0xFF010409)

// ==========================================================================================
// --- 2. COMPONENTE PRINCIPAL: BE ASSISTANT ---
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BeAssistantSearchFab(
    modifier: Modifier = Modifier,
    // Estados sincronizados con BeBrainViewModel
    isSearchActive: Boolean = false,
    searchQuery: String = "", 
    contextMessages: List<BeMessage> = emptyList(),
    isDormido: Boolean = false,
    currentActions: List<BeSmallActionModel> = emptyList(),
    showSmallActions: Boolean = false,
    requestKeyboard: Boolean = false,
    isMultiSelectionActive: Boolean = false, 
    shouldShowBottomBar: Boolean = true, 
    toolboxKey: String = "default", 
    // 🔥 NUEVOS ESTADOS CENTRALIZADOS (HUB)
    state: BeState = BeState.IDLE,
    currentTipIndex: Int = 0,
    // 🔥 ESTADOS PARA LA HERRAMIENTA DE UBICACIÓN
    isLocationExpanded: Boolean = false, // Indica si la tarjeta de ubicación está expandida
    onToggleLocationExpand: (Boolean) -> Unit = {}, // Callback para cerrar al tocar fuera
    locationToolContent: @Composable (() -> Unit)? = null,
    // Callbacks de acción
    onSearchQueryChange: (String) -> Unit = {},
    onSearchStateChange: (Boolean) -> Unit = {},
    onSearchSubmitted: () -> Unit = {}, // 🔥 Callback para Easter Egg
    onBubbleActionClick: () -> Unit = {},
    onToggleSearch: () -> Unit = {},
    onToggleActions: () -> Unit = {},
    onToggleSearchLongClick: () -> Unit = {}, // 🔥 Callback opcional
    onToggleSleep: () -> Unit = {},
    onNextTip: () -> Unit = {},
    onPrevTip: () -> Unit = {},
    onSetState: (BeState) -> Unit = {},
    resetTrigger: Int = 0,
    // 🔥 NUEVO: Reacción de búsqueda desde BeInteractionViewModel
    searchReaction: BeSearchReaction? = null,
    onReactionActionClick: (String) -> Unit = {},
    onReactionResultClick: (Any) -> Unit = {},
    onReactionCloseClick: () -> Unit = {},
    // 🔥 NUEVO: Callback para el menú de opciones
    onMenuOptionClick: (String) -> Unit = {},
    searchMenuOptions: List<ControlItem> = emptyList(),
    selectedOptionIds: Set<String> = emptySet(),
    // 🔥 NUEVOS: Silenciado de burbuja y aviso de mensaje
    isBubbleMuted: Boolean = false,
    hasNewMessage: Boolean = false,
    onToggleBubbleMute: () -> Unit = {},
    // 🔥 PADDING DINÁMICO (Para resting position sobre Nav Bar)
    beBottomPadding: Dp = 0.dp,
    // 🔥 PARÁMETROS DE COREÓGRAFO (BeAssistantViewModel)
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    isDragging: Boolean = false,
    pupilX: Float = 0f,
    pupilY: Float = 0f,
    isBlinking: Boolean = false,
    isToolbarStable: Boolean = true, // 🔥 AGREGADO AQUÍ
    onUpdatePosition: (Float, Float) -> Unit = { _, _ -> },
    onSetDragging: (Boolean) -> Unit = {}
) {


// --- COORDENADAS DE ARRASTRE PERSISTENTES -----------------------------------------------------------------
    // 🔥 PADDING DINÁMICO ANIMADO (Para sincronización con Nav Bar)
    val animatedBeBottomPadding by animateDpAsState(
        targetValue = beBottomPadding,
        animationSpec = tween(durationMillis = 400), // Sincronizado con Nav Bar Enter
        label = "BeBottomPaddingAnimation"
    )

    val isDraggedToLeft = if (isSearchActive) false else offsetX < -120f
    
    // TAMAÑO DEL ASISTENTE: Modifica el valor de 64.dp para hacerlo más grande de manera proporcional
    val assistantSize by animateDpAsState(targetValue = if (isDormido) 48.dp else 64.dp, label = "AssistantSize")
    
    val alpha by animateFloatAsState(if (isDormido) 0.5f else 1f, label = "AlphaDormido")
// --- TECLADO Y BÚSQUEDA --------------------------------------------------------------------------------------
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 🏠 EFECTO DE REGRESO A CASA
    LaunchedEffect(resetTrigger) {
        if (resetTrigger > 0) {
            onUpdatePosition(0f, 0f)
        }
    }

    // --- EFECTO PARA SOLICITUD EXPLÍCITA DE TECLADO ---
    LaunchedEffect(requestKeyboard) {
        if (requestKeyboard) {
            delay(300)
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // --- EFECTO PARA GESTIÓN DE FOCO Y ESTADOS SEGÚN BÚSQUEDA ---
    LaunchedEffect(isSearchActive) {
        if (!isSearchActive) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
        if (isSearchActive && state == BeState.TALKING) onSetState(BeState.IDLE)
    }

    // Optimizamos las animaciones infinitas para reducir la carga en el RenderThread
    val infiniteTransition = rememberInfiniteTransition(label = "be_animations")
    
    val floatY by infiniteTransition.animateFloat(
        initialValue = -2f, 
        targetValue = 2f, 
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing), 
            repeatMode = RepeatMode.Reverse
        ), 
        label = "float_y"
    )
    
    // Multiplicador para suavizar la detención de la flotación cuando la búsqueda está activa
    val floatMultiplier by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "FloatMultiplier"
    )
    
    val wiggleRotation by infiniteTransition.animateFloat(
        initialValue = -12f, 
        targetValue = 12f, 
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing), // Ralentizado de 150 a 300ms
            repeatMode = RepeatMode.Reverse
        ), 
        label = "wiggle"
    )
    
    // 🔥 EFECTO DE SACUDIDA (Badges)
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing), // Ralentizado de 100 a 200ms
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val badgeScale by animateFloatAsState(
        targetValue = if (state == BeState.NOTIFICATION_READY || (state == BeState.TALKING && contextMessages.getOrNull(currentTipIndex)?.icon == "❤️")) 1f else 0f, 
        label = "badge_scale"
    )
    
    val floatingAuraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, 
        targetValue = 0.4f, // Reducido de 0.6f a 0.4f
        animationSpec = infiniteRepeatable(
            animation = tween(1500), // Ralentizado de 1000 a 1500ms
            repeatMode = RepeatMode.Reverse
        ), 
        label = "aura"
    )
    // Vuelo de Be
    val flyUpPx by animateFloatAsState(targetValue = if (isSearchActive) -offsetY else 0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow), label = "fly_up")
    val flySidePx by animateFloatAsState(targetValue = if (isSearchActive) -offsetX else 0f, animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow), label = "fly_side")

    // MOVIMIENTO Ocular
    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state) {
        while (true) {
            when (state) {
                BeState.IDLE -> { targetPupilX = (-2..2).random().toFloat(); targetPupilY = (-3..3).random().toFloat(); delay((2000..4000).random().toLong()) }
                // ==========================================================================================
                // --- SECCIÓN: LÓGICA DE MIRADA HACIA LA NOTIFICACIÓN (IZQUIERDA) ---
                // ==========================================================================================
                // Se cambia targetPupilX a -2.5f para que Be mire hacia la izquierda cuando aparece el badge.
                BeState.NOTIFICATION_READY -> { targetPupilX = -2.5f; targetPupilY = -3f; delay(1500); targetPupilX = 0f; targetPupilY = 0f; delay(1000) }
                BeState.TALKING -> {
                    // --- SECCIÓN: MIRADA ESPECIAL PARA SONROJO ---
                    if (contextMessages.getOrNull(currentTipIndex)?.emotion == BeEmotion.BLUSHING) {
                        targetPupilX = 0f; targetPupilY = 4f
                    } else {
                        targetPupilX = 0f; targetPupilY = 0f
                    }
                    delay(1000)
                }
            }
        }
    }
    val pupilX by animateFloatAsState(targetValue = targetPupilX, animationSpec = tween(400), label = "pupilX")
    val pupilY by animateFloatAsState(targetValue = targetPupilY, animationSpec = tween(400), label = "pupilY")
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay((2500..7000).random().toLong()); isBlinking = true; delay(150); isBlinking = false } }
    val eyeScaleY by animateFloatAsState(targetValue = if (isBlinking) 0.1f else 1f, tween(120), label = "blink")

    // ==========================================================================================
    // --- RENDERIZADO PRINCIPAL ---
    // ==========================================================================================
    Box(
        modifier = Modifier.fillMaxSize() 
            .zIndex(if (isDragging || state == BeState.TALKING || isSearchActive || isLocationExpanded) 200f else 100f),
        contentAlignment = if (isSearchActive) Alignment.TopEnd else Alignment.BottomEnd
    ) {
        // --- CAPA 0: SCRIM GLOBAL (CIERRE POR TOQUE EXTERNO) ---
        AnimatedVisibility(
            visible = showSmallActions || isLocationExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // 🔥 CORRECCIÓN: El Scrim solo es visible (oscurece) cuando la ubicación está expandida
                    .background(if (isLocationExpanded) Color.Black.copy(alpha = 0.45f) else Color.Transparent)
                    .then(
                        if (!isMultiSelectionActive) {
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (showSmallActions) onToggleActions() // Cerrar panel herramientas
                                if (isLocationExpanded) onToggleLocationExpand(false) // Cerrar tarjeta ubicación
                            }
                        } else {
                            Modifier
                        }
                    )
            )
        }

        // --- SECCIÓN: CAPA DE FONDO INMERSIVO PARA BÚSQUEDA ---
        AnimatedVisibility(
            visible = isSearchActive,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            enter = fadeIn(tween(400)) + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut(tween(400)) + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            val backgroundShape = CutCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(backgroundShape)
                    .background(MaverickColors.ROG_Dark_Bg)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val cutSize = 10.dp.toPx()
                        
                        val path = Path().apply {
                            moveTo(0f, size.height - cutSize)
                            lineTo(cutSize, size.height)
                            lineTo(size.width - cutSize, size.height)
                            lineTo(size.width, size.height - cutSize)
                        }
                        
                        drawPath(
                            path = path,
                            color = Color.LightGray.copy(alpha = 0.6f),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                Spacer(
                    modifier = Modifier
                        .statusBarsPadding()
                        .height(95.dp) // Altura reducida para compactar el diseño
                        .fillMaxWidth()
                )
            }
        }

        // --- CAPA 3: BE SEARCH (MODO COMPACTO) ---
        // Se posiciona justo debajo de la barra de búsqueda y ocupa todo el ancho.
        // Se mueve aquí para que quede POR DEBAJO de la SearchBar y de Be en el orden del Z-Index natural de Compose
        if (isSearchActive && searchReaction != null && !isBubbleMuted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(150f) // Menor que la SearchBar y Be
            ) {
                BeSearchBubble(
                    isVisible = true,
                    reaction = searchReaction,
                    onActionClick = { id ->
                        onReactionActionClick(id ?: searchReaction.actionId ?: "")
                    },
                    onResultClick = onReactionResultClick,
                    onCloseClick = onReactionCloseClick // Pasamos el callback de cierre
                )
            }
        }

        // ==========================================================================================
        // --- SECCIÓN 1: CAPA DE HERRAMIENTAS Y CONTROLES DE BASE ---
        // --- Ajustamos el padding dinámico para que Be "caiga" al borde si no hay Nav Bar ---
        // ==========================================================================================
        if (!isDormido) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = animatedBeBottomPadding.coerceAtLeast(0.dp))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.CenterEnd
            ) {
                // --- SECCIÓN: HERRAMIENTAS MODO NORMAL ---
                val derivedToolboxKey = "${toolboxKey}_${isMultiSelectionActive}"

                BeSmallActionsBuilder(
                    isVisible = showSmallActions && !isSearchActive,
                    actions = currentActions,
                    shouldShowBottomBar = shouldShowBottomBar,
                    toolboxKey = derivedToolboxKey,
                    leadingContent = null,
                    isToolbarStable = isToolbarStable
                )

                BeDefaultActionsBand(
                    isVisible = !showSmallActions && !isSearchActive,
                    actions = currentActions,
                    shouldShowBottomBar = shouldShowBottomBar,
                    toolboxKey = derivedToolboxKey,
                    leadingContent = locationToolContent,
                    isToolbarStable = isToolbarStable
                )

                // --- SECCIÓN: CONTROLES MODO BÚSQUEDA (CON ANIMACIÓN DE ENTRADA/SALIDA) ---
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(400)),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300)),
                    label = "SearchControlsAnimation"
                ) {
                    // ==========================================================================================
                    // --- MODO BÚSQUEDA: PANEL DE CONTROL PREMIUM (STYLE M3) ---
                    // Rediseño con estética minimalista, botones circulares y mayor espaciado.
                    // Los colores se ajustan a una paleta de grises y blancos para un look más sofisticado.
                    // ==========================================================================================
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .wrapContentWidth()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF121212).copy(alpha = 0.9f), Color(0xFF121212))
                                ),
                                shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 32.dp, end = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp), // Mayor separación entre controles
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // --- BOTÓN 1: ABRIR TECLADO (PREMIUM GREY) ---
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2C))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                    .clickable { 
                                        // 1. Notificar al ViewModel si es necesario
                                        onBubbleActionClick() 
                                        // 2. Acción de UI: Forzar foco y teclado
                                        focusRequester.requestFocus()
                                        keyboardController?.show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = "Teclado",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // --- BOTÓN 2: CERRAR (PREMIUM WHITE) ---
                            Box(
                                modifier = Modifier
                                    .size(52.dp) // Mismo tamaño exacto
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF424242)) // Gris medio
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                    .clickable { onToggleSearch() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CAPA 2: ASISTENTE BE (CONTROL MÓVIL) ---
        Row(
            modifier = Modifier
                .align(if (isSearchActive) Alignment.TopEnd else Alignment.BottomEnd)
                .then(
                    if (isSearchActive) {
                        Modifier
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp)
                            .offset(y = (-2).dp) // Posición elevada
                    } else Modifier
                )
                .padding(bottom = if (isSearchActive) 0.dp else animatedBeBottomPadding.coerceAtLeast(0.dp))
                .offset { IntOffset((offsetX + flySidePx).roundToInt(), (offsetY + flyUpPx).roundToInt()) }
                .zIndex(300f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (isSearchActive) {
                // --- SECCIÓN: BARRA DE BÚSQUEDA ASUS ROG ---
                SearchBarComponent(
                    modifier = Modifier.weight(1f),
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearchSubmitted, // 🔥 Callback para Easter Egg
                    focusRequester = focusRequester,
                    onClearText = { onSearchQueryChange("") },
                    onCloseBar = {
                        onSearchQueryChange("") // Limpiar query al cerrar desde la barra
                        onToggleSearch()
                    },
                    menuOptions = searchMenuOptions,
                    onMenuOptionClick = onMenuOptionClick,
                    selectedOptionIds = selectedOptionIds
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Box(
                modifier = Modifier
                    .size(80.dp) // Aumentado de 72.dp para acomodar el nuevo tamaño del asistente
                    .pointerInput(isDormido, isSearchActive) {
                        detectTapGestures(
                            onTap = { 
                            if (isDormido) onToggleSleep()
                            else {
                                if (isSearchActive) onSearchQueryChange("") // Limpiar al cerrar
                                onSetState(BeState.IDLE)
                                onToggleSearch()
                            }
                        },
                            onLongPress = { if (!isDormido && !isSearchActive) onToggleActions() },
                            onDoubleTap = { onToggleSleep() }
                        )
                    }
                    .pointerInput(isSearchActive) {
                        detectDragGestures(
                            onDragStart = { if (!isSearchActive) onSetDragging(true) },
                            onDragEnd = { if (!isSearchActive) onSetDragging(false) },
                            onDrag = { change, dragAmount ->
                                if (!isSearchActive) {
                                    change.consume()
                                    onUpdatePosition(offsetX + dragAmount.x, offsetY + dragAmount.y)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val currentMessage = if (contextMessages.isNotEmpty()) contextMessages.getOrNull(currentTipIndex) else null
                val currentEmotion = if (state != BeState.IDLE) currentMessage?.emotion ?: BeEmotion.NORMAL else BeEmotion.NORMAL

                Box(
                    modifier = Modifier
                        .offset(y = (floatY * floatMultiplier).dp)
                        .size(assistantSize)
                        .scale(if (isDormido) 0.8f else 1f)
                        .alpha(alpha)
                        .drawBehind {
                            // --- SECCIÓN: RESPLANDOR NEÓN (Sincronizado con Startup) ---
                            // Movido a drawBehind para evitar recrear el pincel y objetos de dibujo
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(LocalROG_Cyan.copy(alpha = 0.2f), Color.Transparent)
                                ),
                                radius = size.width * 0.9f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDormido) { Text("💤", fontSize = 18.sp) }
                    else {
                        if (isDragging) { 
                            Box(
                                modifier = Modifier
                                    .offset(x = 5.dp, y = 5.dp)
                                    .size(54.dp)
                                    .scale(1.2f)
                                    .alpha(floatingAuraAlpha)
                                    .background(LocalROG_Cyan, CircleShape)
                                    .blur(8.dp)
                            ) 
                        }
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            this.scale(size.width / 100f, size.height / 100f, pivot = Offset.Zero) {
                                // --- SECCIÓN: MANGO DE LA LUPA ---
                                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                                drawLine(LocalROG_Cyan, Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)
                                drawLine(LocalROG_Cyan.copy(alpha = 0.4f), Offset(76f, 76f), Offset(89f, 89f), 6f, StrokeCap.Round)

                                // --- SECCIÓN: CABEZA DE ASISTENTE (LENTE) ---
                                // Casco Base de Be
                                drawCircle(Color(0xFF0A0E14), 38f, Offset(50f, 50f))

                                // Anillo Neón
                                drawCircle(
                                    color = LocalROG_Cyan,
                                    radius = 34f,
                                    center = Offset(50f, 50f),
                                    style = Stroke(width = 3.5f)
                                )

                                // Brillo visor
                                drawArc(
                                    color = Color.White.copy(alpha = 0.2f),
                                    startAngle = 180f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    style = Stroke(width = 2.5f, cap = StrokeCap.Round),
                                    topLeft = Offset(22f, 22f),
                                    size = Size(56f, 56f)
                                )

                                if (currentEmotion == BeEmotion.HAPPY) {
                                    drawPath(Path().apply { moveTo(33f, 50f); quadraticTo(40f, 38f, 47f, 50f) }, color = Color.White, style = Stroke(5f, cap = StrokeCap.Round))
                                    drawPath(Path().apply { moveTo(53f, 50f); quadraticTo(60f, 38f, 67f, 50f) }, color = Color.White, style = Stroke(5f, cap = StrokeCap.Round))
                                } else {
                                    // Ojos
                                    drawOval(
                                        color = Color.White,
                                        topLeft = Offset(31f, 50f - (11f * eyeScaleY)),
                                        size = Size(15f, 22f * eyeScaleY)
                                    )
                                    drawOval(
                                        color = Color.White,
                                        topLeft = Offset(54f, 50f - (11f * eyeScaleY)),
                                        size = Size(15f, 22f * eyeScaleY)
                                    )
                                    
                                    val pupilRadius = if (currentEmotion == BeEmotion.SURPRISED) 2.5f else 4.5f
                                    drawCircle(Color(0xFF05070A), pupilRadius * eyeScaleY, Offset(38.5f + pupilX, 50f + pupilY))
                                    drawCircle(Color.White, 1.2f * eyeScaleY, Offset(39.5f + pupilX, 48.5f + pupilY))
                                    drawCircle(Color(0xFF05070A), pupilRadius * eyeScaleY, Offset(61.5f + pupilX, 50f + pupilY))
                                    drawCircle(Color.White, 1.2f * eyeScaleY, Offset(62.5f + pupilX, 48.5f + pupilY))
                                    
                                    // --- SECCIÓN: MEJILLAS SONROJADAS (EASTER EGG) ---
                                    if (currentEmotion == BeEmotion.BLUSHING) {
                                        drawCircle(Color(0xFFFFB6C1).copy(alpha = 0.6f), 8f, Offset(35f, 65f))
                                        drawCircle(Color(0xFFFFB6C1).copy(alpha = 0.6f), 8f, Offset(65f, 65f))
                                    }

                                    if (currentEmotion == BeEmotion.ANGRY) {
                                        drawLine(LocalROG_Cyan, Offset(28f, 36f), Offset(46f, 42f), strokeWidth = 5f, cap = StrokeCap.Round)
                                        drawLine(LocalROG_Cyan, Offset(72f, 36f), Offset(54f, 42f), strokeWidth = 5f, cap = StrokeCap.Round)
                                    } else if (currentEmotion == BeEmotion.SURPRISED) {
                                        drawArc(LocalROG_Cyan, 180f, 180f, false, Offset(32f, 32f), Size(16f, 10f), style = Stroke(3.5f, cap = StrokeCap.Round))
                                        drawArc(LocalROG_Cyan, 180f, 180f, false, Offset(52f, 32f), Size(16f, 10f), style = Stroke(3.5f, cap = StrokeCap.Round))
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================================================================
                // --- SECCIÓN: BADGE DE CORAZÓN (EASTER EGG) ---
                // ==========================================================================================
                if (badgeScale > 0.01f && currentMessage != null && !isSearchActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart) // 🔥 IZQUIERDA
                            .offset(x = (-8).dp, y = (-16).dp) // 🔥 MÁS ALTO (Alineado con el scrim de BeBuild)
                            .wrapContentSize(unbounded = true)
                            .graphicsLayer {
                                scaleX = badgeScale
                                scaleY = badgeScale
                                rotationZ = wiggleRotation
                            }
                            .zIndex(20f)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(BentoDarkGlassBackground)
                            .background(BentoGlassBrush)
                            .border(1.5.dp, currentMessage.bubbleColor.copy(alpha = 0.6f), CircleShape)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onSetState(BeState.TALKING)
                            }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentMessage.icon, fontSize = 16.sp)
                    }
                }

                // ==========================================================================================
                // --- SECCIÓN: BURBUJA DE CONTEXTO / COMIC (MODO NORMAL) ---
                // Solo se muestra si NO estamos en búsqueda activa.
                // ==========================================================================================
                if (!isSearchActive) {
                    BeAssistantBubble(
                        isVisible = state == BeState.TALKING && contextMessages.isNotEmpty(),
                        messages = contextMessages,
                        currentIndex = currentTipIndex,
                        onCloseClick = { onSetState(BeState.IDLE) },
                        onPageSelected = { index ->
                            if (index > currentTipIndex) onNextTip()
                            else if (index < currentTipIndex) onPrevTip()
                        },
                        onActionClick = { onSetState(BeState.IDLE); onBubbleActionClick() }
                    )
                }

                // ==========================================================================================
                // --- SECCIÓN: BADGE DE CONVERSACIÓN MAVERICK (SOBRE EL ASISTENTE) ---
                // ==========================================================================================
                if (isSearchActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-6).dp, y = 1.dp) // Corner inferior izquierdo de Be
                            .size(24.dp) // Más compacto
                            .graphicsLayer {
                                // Sacudida si hay mensaje nuevo y está silenciado
                                rotationZ = if (hasNewMessage && isBubbleMuted) shakeRotation else 0f
                            }
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF0A0E14).copy(alpha = 0.9f))
                            .border(
                                1.5.dp,
                                if (isBubbleMuted) Color.Gray.copy(alpha = 0.6f) else LocalROG_Cyan.copy(alpha = 0.7f),
                                CircleShape
                            )
                            .clickable { onToggleBubbleMute() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBubbleMuted) "😶" else "💬", 
                            fontSize = 14.sp,
                            modifier = Modifier.alpha(if (isBubbleMuted) 0.5f else 1f)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN: COMPONENTES AUXILIARES ---
// ==========================================================================================

/**
 * SearchBarComponent - Estilo ASUS ROG Phone x Material 3 (ACTUALIZADO)
 * @param query Texto actual de búsqueda
 * @param onQueryChange Callback al cambiar el texto
 * @param onSearch Callback al presionar Intro
 * @param focusRequester Requester para manejar el foco del teclado
 * @param onClearText Acción para la 'X' de limpiar texto
 * @param onCloseBar Acción para la 'X' principal de cerrar la barra
 * @param onMenuClick Acción para el selector de categoría (FAB divider)
 */
@Composable
fun SearchBarComponent(
    modifier: Modifier = Modifier, // 🔥 MODIFIER PRIMERO
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit = {}, // 🔥 Callback para Easter Egg
    focusRequester: FocusRequester,
    onClearText: () -> Unit,
    onCloseBar: () -> Unit,
    onMenuOptionClick: (String) -> Unit = {},
    menuOptions: List<ControlItem> = emptyList(),
    selectedOptionIds: Set<String> = emptySet()
) {
    // --- SECCIÓN: ESTADOS LOCALES ---
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current // 🔥 CONTROLADOR TECLADO

    // --- SECCIÓN: CONFIGURACIÓN VISUAL ---
    // Conservamos el gradiente original del borde (Cian a Rojo)
    val borderBrush = Brush.horizontalGradient(
        listOf(LocalROG_Cyan, LocalROG_Red)
    )

    // CAMBIO: Forma Cyber-Cut según @Box.kt (Corte diagonal ROG)
    val barShape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp) // Altura ajustada a SearchBarCyberCut
            .shadow(12.dp, barShape, ambientColor = LocalROG_Cyan, spotColor = LocalROG_Red)
            .border(1.5.dp, borderBrush, barShape) // Borde neón conservado
            .clip(barShape)
            .background(MaverickColors.ROG_Dark_Bg) // Fondo cambiado a RogDarkGray
            .padding(start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- SECCIÓN: SELECTOR DE CATEGORÍA CON MENÚ ---
            Box {
                Row(
                    modifier = Modifier
                        .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                        .clickable { expanded = !expanded }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = LocalROG_Cyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // --- NUEVO MENÚ CP (ESTILO CYBERPUNK) ---
                if (expanded) {
                    Popup(
                        onDismissRequest = { expanded = false },
                        offset = IntOffset(x = 0, y = 140)
                    ) {
                        MenuCP(
                            isVisible = true,
                            title = "MÓDULOS DE ASISTENTE",
                            headerEmoji = "🤖",
                            headerColor = CyberColorsV3.ElectricCyan,
                            onDismiss = { expanded = false }
                        ) {
                            if (menuOptions.isNotEmpty()) {
                                menuOptions.forEach { option ->
                                    val isSelected = selectedOptionIds.contains(option.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onMenuOptionClick(option.id)
                                                expanded = false
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (option.emoji != null) {
                                            Text(option.emoji, fontSize = 18.sp)
                                        } else {
                                            Icon(
                                                imageVector = option.icon ?: Icons.Default.Search,
                                                contentDescription = null,
                                                tint = option.color,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = option.label.uppercase(),
                                            color = if (isSelected) LocalROG_Cyan else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            style = CyberTypography.BodyCyber
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Seleccionado",
                                                tint = LocalROG_Cyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .padding(vertical = 18.dp, horizontal = 4.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.6f)
            )

            // --- SECCIÓN: INPUT DE TEXTO (TIPOGRAFÍA CYBER) ---
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                textStyle = CyberTypography.BodyCyber.copy(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(LocalROG_Cyan),
                singleLine = true, // 🔥 OBLIGATORIO PARA IME ACTION
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search // 🔥 CAMBIA ICONO A LUPA/BUSCAR
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        keyboardController?.hide() // 🔥 OCULTAR TECLADO AL BUSCAR
                        onSearch() 
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = "BUSCA CON BE...",
                                style = CyberTypography.BodyCyber.copy(
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // --- SECCIÓN: ACCIONES (LIMPIAR Y CERRAR) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // X pequeña para limpiar texto
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = onClearText,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

            }
        }
    }
}

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF010409)
@Composable
fun SearchBarComponentPreview() {
    var query by remember { mutableStateOf("") }
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).background(LocalDarkBackground)) {
            SearchBarComponent(
                query = query,
                onQueryChange = { query = it },
                focusRequester = remember { FocusRequester() },
                onClearText = { query = "" },
                onCloseBar = {},
                menuOptions = emptyList(),
                onMenuOptionClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF010409)
@Composable
fun BeAssistantSearchFabIdlePreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            BeAssistantSearchFab(
                state = BeState.IDLE,
                contextMessages = BeDictionary.HomeMessages
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF010409)
@Composable
fun BeAssistantSearchFabTalkingPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            BeAssistantSearchFab(
                state = BeState.TALKING,
                contextMessages = BeDictionary.HomeMessages,
                currentTipIndex = 0
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF010409)
@Composable
fun BeAssistantSearchFabSearchActivePreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            BeAssistantSearchFab(
                isSearchActive = true,
                searchQuery = "Searching with Be..."
            )
        }
    }
}

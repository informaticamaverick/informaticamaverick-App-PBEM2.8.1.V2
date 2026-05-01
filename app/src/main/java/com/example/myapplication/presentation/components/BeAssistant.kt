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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
//import com.example.myapplication.presentation.client.BeSearchReaction
import androidx.compose.ui.window.Popup
import com.example.myapplication.presentation.components.Utilidades.CyberColorsV3
import com.example.myapplication.presentation.components.Utilidades.MenuCP
import com.example.myapplication.presentation.components.Utilidades.CyberTypography
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoDarkGlassBackground
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoGlassBrush
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
    val isCentered: Boolean = false,
    val categories: List<com.example.myapplication.data.local.CategoryEntity> = emptyList() // NUEVO: Categorías para búsqueda rápida
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
    activeConversationalMessage: BeMessage? = null, // NUEVO
    contextMessages: List<BeMessage> = emptyList(),
    isDormido: Boolean = false,
    currentActions: List<BeSmallActionModel> = emptyList(),
    showSmallActions: Boolean = false,
    requestKeyboard: Boolean = false,
    isMultiSelectionActive: Boolean = false, 
    shouldShowBottomBar: Boolean = true, 
    toolboxKey: String = "default", 
    // NUEVOS ESTADOS CENTRALIZADOS (HUB)
    state: BeState = BeState.IDLE,
    currentTipIndex: Int = 0,
    // Callbacks de acción
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmitted: () -> Unit = {}, // Callback para Easter Egg
    onBubbleActionClick: () -> Unit = {},
    onToggleSearch: () -> Unit = {},
    onToggleActions: () -> Unit = {},
    onToggleSleep: () -> Unit = {},
    onNextTip: () -> Unit = {},
    onPrevTip: () -> Unit = {},
    onSetState: (BeState) -> Unit = {},
    resetTrigger: Int = 0,
    // NUEVO: Reacción de búsqueda desde BeInteractionViewModel
    //searchReaction: BeSearchReaction? = null,
    onReactionActionClick: (String) -> Unit = {},
    onReactionResultClick: (Any) -> Unit = {},
    onReactionCloseClick: () -> Unit = {},
    // NUEVO: Callback para el menú de opciones
    onMenuOptionClick: (String) -> Unit = {},
    searchMenuOptions: List<ControlItem> = emptyList(),
    selectedOptionIds: Set<String> = emptySet(),
    // NUEVOS: Silenciado de burbuja y aviso de mensaje
    isBubbleMuted: Boolean = false,
    hasNewMessage: Boolean = false,
    onToggleBubbleMute: () -> Unit = {},
    // PADDING DINÁMICO (Para resting position sobre Nav Bar)
    beBottomPadding: Dp = 0.dp,
    // PARÁMETROS DE COREÓGRAFO (BeAssistantViewModel)
    offsetX: Float = 0f,
    offsetY: Float = 0f,
    isDragging: Boolean = false,
    isToolbarStable: Boolean = true,
    onUpdatePosition: (Float, Float) -> Unit = { _, _ -> },
    onSetDragging: (Boolean) -> Unit = {}
) {

    // 🔥 PAGER STATE PARA TIPS INFINITOS (USAMOS INT.MAX_VALUE / 2 COMO OFFSET INICIAL)
    val totalTips = contextMessages.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(
        initialPage = (Int.MAX_VALUE / 2) - (Int.MAX_VALUE / 2 % totalTips),
        pageCount = { if (contextMessages.isEmpty()) 0 else Int.MAX_VALUE }
    )

    // 🔥 PADDING DINÁMICO ANIMADO (Para sincronización con Nav Bar)
    val animatedBeBottomPadding by animateDpAsState(
        targetValue = beBottomPadding,
        animationSpec = tween(durationMillis = 400), // Sincronizado con Nav Bar Enter
        label = "BeBottomPaddingAnimation"
    )

    // --- SECCIÓN: HIBERNACIÓN ESTILO WHATSAPP ---
    // Si está dormido, se desplaza hacia la derecha para mostrar solo la mitad (24dp de 48dp)
    val hibernationXOffset by animateDpAsState(
        targetValue = if (isDormido) 24.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "HibernationXOffset"
    )

    // TAMAÑO DEL ASISTENTE: Aumentado a 72dp cuando está despierto para feedback visual
    val assistantSize by animateDpAsState(targetValue = if (isDormido) 48.dp else 72.dp, label = "AssistantSize")
    
    val alpha by animateFloatAsState(if (isDormido) 0.4f else 1f, label = "AlphaDormido") // Más transparente en hibernación

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
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wiggle"
    )

    // 🔥 EFECTO DE SACUDIDA (Badges)
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
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
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), 
        label = "aura"
    )

    // Multiplicador para anular la flotación y el aura en hibernación
    val hibernationMultiplier by animateFloatAsState(
        targetValue = if (isDormido) 0f else 1f,
        animationSpec = tween(500),
        label = "HibernationMultiplier"
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
                BeState.NOTIFICATION_READY -> { targetPupilX = -2.5f; targetPupilY = -3f; delay(1500); targetPupilX = 0f; targetPupilY = 0f; delay(1000) }
                BeState.TALKING -> {
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
    
    // --- SECCIÓN: EFECTO DE OJOS CERRADOS EN HIBERNACIÓN ---
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { while (true) { delay((2500..7000).random().toLong()); isBlinking = true; delay(150); isBlinking = false } }
    val eyeScaleY by animateFloatAsState(
        targetValue = if (isDormido || isBlinking) 0.1f else 1f, 
        animationSpec = tween(120), 
        label = "blink"
    )

    // ==========================================================================================
    // --- RENDERIZADO PRINCIPAL ---
    // ==========================================================================================
    Box(
        modifier = modifier.fillMaxSize()
            .zIndex(if (isDragging || state == BeState.TALKING || isSearchActive) 200f else 100f),
        contentAlignment = if (isSearchActive) Alignment.TopEnd else Alignment.BottomEnd
    ) {
        // --- CAPA 0: SCRIM GLOBAL (CIERRE POR TOQUE EXTERNO) ---
        AnimatedVisibility(
            visible = showSmallActions,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .then(
                        if (!isMultiSelectionActive) {
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (showSmallActions) onToggleActions() // Cerrar panel herramientas
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
        // ==========================================================================================
        // --- SECCIÓN: BURBUJAS DE DIÁLOGO CÓMIC (NUEVO V5) ---
        // ==========================================================================================

        // 1. BURBUJA SUPERIOR (MODO BÚSQUEDA ACTIVA)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 108.dp, end = 4.dp, start = 4.dp) // Debajo de la barra de búsqueda y Be
                .zIndex(400f)
        ) {
            BeTopBubble(
                isVisible = isSearchActive && activeConversationalMessage != null,
                onCloseClick = onReactionCloseClick, // Usamos el callback correcto para cerrar la reacción/respuesta
                borderColor = activeConversationalMessage?.bubbleColor ?: ElectricCyanColor
            ) {
                activeConversationalMessage?.let { msg ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(msg.icon, fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        BubbleM3Typography.Title("Be Assistant", color = msg.bubbleColor)
                    }
                    Spacer(Modifier.height(8.dp))
                    BubbleM3Typography.Body(msg.text)
                    
                    if (msg.actionText != null) {
                        Spacer(Modifier.height(12.dp))
                        ActionChip(
                            item = ControlItemLite(
                                label = msg.actionText,
                                color = msg.bubbleColor
                            ),
                            onClick = onBubbleActionClick
                        )
                    }

                    // --- SECCIÓN: CATEGORÍAS ENCONTRADAS (MODO FAST) ---
                    if (msg.categories.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    msg.categories.forEach { category ->
                                        ActionChip(
                                            item = ControlItemLite(
                                                label = category.name,
                                                emoji = category.icon,
                                                color = msg.bubbleColor
                                            ),
                                            onClick = { 
                                                // Emitimos la acción de la categoría seleccionada
                                                onReactionActionClick("cat_${category.name.lowercase().trim()}") 
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. BURBUJA INFERIOR (MODO IDLE / TIPS)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = animatedBeBottomPadding + 45.dp, end = 4.dp, start = 4.dp)
                .zIndex(400f)
        ) {
            BeBottomBubble(
                isVisible = !isSearchActive && state == BeState.TALKING && contextMessages.isNotEmpty(),
                onCloseClick = { onSetState(BeState.IDLE) },
                messages = contextMessages,
                pagerState = pagerState,
                onActionClick = onBubbleActionClick
            )
        }

        // --- CAPA 3: BE SEARCH (MODO COMPACTO) ---
        // ==========================================================================================
        // --- SECCIÓN 1: CAPA DE HERRAMIENTAS Y CONTROLES DE BASE ---
        // ==========================================================================================
        if (!isDormido) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = animatedBeBottomPadding.coerceAtLeast(0.dp))
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.CenterEnd
            ) {
                // --- SECCIÓN: ORQUESTACIÓN DE ACCIONES TÁCTICAS (HUD V7) ---
                // Si estamos en Chat y la multiselección está activa, inyectamos las herramientas aquí directamente
                // para asegurar que el flujo visual sea consistente con el diseño Maverick.
                val chatMultiSelectActions = remember {
                    listOf(
                        BeSmallActionModel(
                            id = "chat_cancel",
                            icon = Icons.Default.Close,
                            label = "CERRAR",
                            tint = Color.Red
                        ),
                        BeSmallActionModel(id = "divider_v1", icon = Icons.Default.Remove, label = ""),
                        BeSmallActionModel(
                            id = "chat_select_all",
                            icon = Icons.Default.SelectAll,
                            label = "TODOS",
                            tint = Color(0xFF2197F5) // MaverickBlue
                        ),
                        BeSmallActionModel(id = "divider_v2", icon = Icons.Default.Remove, label = ""),
                        BeSmallActionModel(
                            id = "chat_delete_multi",
                            icon = Icons.Default.Delete,
                            label = "ELIMINAR",
                            tint = Color.Red
                        )
                    )
                }

                val finalActions = remember(currentActions, isMultiSelectionActive, toolboxKey) {
                    if (isMultiSelectionActive && (toolboxKey.startsWith("chat") || toolboxKey.startsWith("conversaciones"))) {
                        chatMultiSelectActions.map { action ->
                            action.copy(onClick = { onReactionActionClick(action.id) })
                        }
                    } else {
                        currentActions
                    }
                }

                val derivedToolboxKey = "${toolboxKey}_${isMultiSelectionActive}"

                BeSmallActionsBuilder(
                    isVisible = showSmallActions && !isSearchActive,
                    actions = finalActions,
                    shouldShowBottomBar = shouldShowBottomBar,
                    toolboxKey = derivedToolboxKey,
                    isToolbarStable = isToolbarStable
                )

                BeDefaultActionsBand(
                    isVisible = !showSmallActions && !isSearchActive,
                    actions = finalActions,
                    shouldShowBottomBar = shouldShowBottomBar,
                    toolboxKey = derivedToolboxKey,
                    isToolbarStable = isToolbarStable
                )

                // --- SECCIÓN: CONTROLES MODO BÚSQUEDA (CON ANIMACIÓN DE ENTRADA/SALIDA) ---
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(400)),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300)),
                    label = "SearchControlsAnimation"
                ) {
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .wrapContentWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF121212).copy(alpha = 0.9f), Color(0xFF121212))
                                ),
                                shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 32.dp, end = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                                        onBubbleActionClick() 
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
                                    .size(52.dp)
                                    .shadow(4.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFF424242))
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
                            .offset(y = (-2).dp)
                    } else Modifier
                )
                .padding(bottom = if (isSearchActive) 0.dp else animatedBeBottomPadding.coerceAtLeast(0.dp))
                .offset { 
                    IntOffset(
                        (offsetX + flySidePx + hibernationXOffset.toPx()).roundToInt(), 
                        (offsetY + flyUpPx).roundToInt() 
                    ) 
                }
                .zIndex(300f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (isSearchActive) {
                SearchBarComponent(
                    modifier = Modifier.weight(1f),
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearchSubmitted,
                    focusRequester = focusRequester,
                    onClearText = { onSearchQueryChange("") },
                    onCloseBar = {
                        onSearchQueryChange("")
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
                    .size(80.dp)
                    .pointerInput(isDormido, isSearchActive) {
                        detectTapGestures(
                            onTap = { 
                                if (isDormido) onToggleSleep()
                                else {
                                    if (isSearchActive) onSearchQueryChange("")
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
                        .offset(y = (floatY * floatMultiplier * hibernationMultiplier).dp)
                        .size(assistantSize)
                        .scale(if (isDormido) 0.8f else 1f)
                        .alpha(alpha)
                        .drawBehind {
                            if (hibernationMultiplier > 0.1f) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(LocalROG_Cyan.copy(alpha = 0.2f * hibernationMultiplier), Color.Transparent)
                                    ),
                                    radius = size.width * 0.9f
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDragging) { 
                        Box(
                            modifier = Modifier
                                .offset(x = 5.dp, y = 5.dp)
                                .size(54.dp)
                                .scale(1.2f)
                                .alpha(floatingAuraAlpha * hibernationMultiplier)
                                .background(LocalROG_Cyan, CircleShape)
                                .blur(8.dp)
                        ) 
                    }
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        this.scale(size.width / 100f, size.height / 100f, pivot = Offset.Zero) {
                            // MANGO DE LA LUPA
                            drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                            drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                            drawLine(LocalROG_Cyan, Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)
                            drawLine(LocalROG_Cyan.copy(alpha = 0.4f), Offset(76f, 76f), Offset(89f, 89f), 6f, StrokeCap.Round)

                            // CABEZA DE ASISTENTE (LENTE)
                            drawCircle(Color(0xFF0A0E14), 38f, Offset(50f, 50f))
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
                                
                                // MEJILLAS SONROJADAS (EASTER EGG)
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

                // --- BADGE DE CORAZÓN (EASTER EGG) ---
                if (badgeScale > 0.01f && currentMessage != null && !isSearchActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-8).dp, y = (-16).dp)
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

                // --- BADGE DE CONVERSACIÓN MAVERICK ---
                if (isSearchActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-6).dp, y = 1.dp)
                            .size(24.dp)
                            .graphicsLayer {
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

@Composable
fun SearchBarComponent(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit = {},
    focusRequester: FocusRequester,
    onClearText: () -> Unit,
    onCloseBar: () -> Unit,
    onMenuOptionClick: (String) -> Unit = {},
    menuOptions: List<ControlItem> = emptyList(),
    selectedOptionIds: Set<String> = emptySet()
) {
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val borderBrush = Brush.horizontalGradient(listOf(LocalROG_Cyan, LocalROG_Red))
    val barShape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(12.dp, barShape, ambientColor = LocalROG_Cyan, spotColor = LocalROG_Red)
            .border(1.5.dp, borderBrush, barShape)
            .clip(barShape)
            .background(MaverickColors.ROG_Dark_Bg)
            .padding(start = 12.dp, end = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
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

            VerticalDivider(
                modifier = Modifier
                    .padding(vertical = 18.dp, horizontal = 4.dp)
                    .width(1.dp),
                color = Color.White.copy(alpha = 0.6f)
            )

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
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { 
                        keyboardController?.hide()
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

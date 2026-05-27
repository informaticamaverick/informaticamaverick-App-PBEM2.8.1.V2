package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.positionInWindow
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.MenuTacticoBe
import com.example.myapplication.presentation.designsystem.components.BeMenuItem
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.DepthDividerThemedVertical
import com.example.myapplication.presentation.designsystem.components.shakeClick
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material3.LocalTextStyle
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.myapplication.presentation.components.BeEmotion

// ==========================================================================================
// --- CONSTANTES DE DISEÑO ELITE (UNIFICADAS) ---
// ==========================================================================================
private val BorderColor = MaverickColors.BentoDarkGlassBorder
private val WhiteAccent = Color.White
private val TextGray = MaverickColors.TextMuted
private val MenuBg = MaverickColors.ROG_Dark_Bg
private val ActionBtnBg = MaverickColors.AsSidebarBg

// ==========================================================================================
// --- MODELOS DE DATOS ---
// ==========================================================================================

data class DropdownItemData(
    val id: String, 
    val label: String,
    val section: String? = null,
    val emoji: String? = null,
    val icon: ImageVector? = null,
    val color: Color = Color.White // 🔥 ELITE: Color de identidad neón
) {
    /**
     * 🔥 ELITE: Convierte datos de dropdown a items de control para el HUD.
     */
    fun toControlItem(): ControlItem {
        return ControlItem(
            label = this.label,
            icon = this.icon,
            emoji = this.emoji ?: "🔹",
            color = this.color,
            id = this.id
        )
    }
}

data class FilterSortItem(
    val id: String,
    val label: String,
    val emoji: String,
    val icon: ImageVector? = null,
    val color: Color = Color.White,
    val section: String? = null // 🔥 ELITE: Identificador de grupo para lógica visual
)

// ==========================================================================================
// --- MOLDES BASE (UI ATOMS) ---
// ==========================================================================================

@Composable
fun MoldePremiumCardBase(
    modifier: Modifier = Modifier,
    label: String? = null,
    height: Dp? = 90.dp,
    innerPadding: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    isHighlighted: Boolean = false,
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable () -> Unit
) {
    // --- ESTADOS DE INTERACCIÓN M3 ---
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // --- ANIMACIONES DE ELEVACIÓN Y TONALIDAD ---
    val elevation by animateDpAsState(
        targetValue = when {
            isHighlighted -> 32.dp
            isPressed -> 12.dp
            else -> 6.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "elevation"
    )

    val borderColor = remember(isPressed, isHighlighted) {
        when {
            isHighlighted -> Color.White.copy(alpha = 0.3f)
            isPressed -> Color.White.copy(alpha = 0.3f)
            else -> Color.White.copy(alpha = 0.12f)
        }
    }

    Column(
        modifier = modifier
            .then(if (height != null) Modifier.height(height) else Modifier)
            .graphicsLayer {
                val scale = if (isPressed) 0.98f else 1f
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onClick()
                    }
                } else Modifier
            )
    ) {
        // --- ETIQUETA "HELPER" EXTERNA (ARRIBA DEL BORDE) ---
        if (label != null) {
            Text(
                text = label.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .padding(start = 4.dp, bottom = 0.dp)
            )
        }

        // --- CONTENEDOR PRINCIPAL CON BORDE (Estilo Modern Dark) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (label != null) Modifier.weight(1f) else Modifier.fillMaxHeight())
                .shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaverickColors.ROG_Dark_Bg.copy(alpha = 0.9f),
                            MaverickColors.VantaBlack
                        )
                    ),
                    shape = shape
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                )
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 1: MOLDE TARJETA DE FILTROS (V2 SHORTCUTS) ---
// ==========================================================================================

/**
 * MoldePremiumFilterCard: Tarjeta de filtrado con accesos directos dinámicos.
 */
@Composable
fun MoldePremiumFilterCard(
    label: String,
    dropdownItems: List<DropdownItemData>,
    shortcutItems: List<FilterSortItem>,
    activeFilters: Set<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit, // (itemId, isAdd)
    modifier: Modifier = Modifier,
    isExpandedExternally: Boolean = false,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(false) }
    var cardWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // 🔥 ELITE: Sincronización con delay para el efecto "primero expande, luego aparece menú"
    LaunchedEffect(expanded) {
        onExpandChanged(expanded)
        if (expanded) {
            delay(150) // Delay corto para permitir que la tarjeta "se expanda" visualmente
            menuVisible = true
        } else {
            menuVisible = false
        }
    }

    // 🔥 ELITE: Forma dinámica para unión perfecta con el menú
    val cardShape = animateHorizontalEdgeShape(expanded)

    Box(modifier = modifier
        .zIndex(1f) // Mantener tarjeta sobre el menú para el efecto de "aparece desde atrás"
        .onGloballyPositioned {
            cardWidth = with(density) { it.size.width.toDp() }
        }
    ) {
        var showTacticalMenu by remember { mutableStateOf(false) }
        var touchOffset by remember { mutableStateOf(Offset.Zero) }

        MoldePremiumCardBase(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            touchOffset = offset
                            showTacticalMenu = true
                        }
                    )
                },
            label = label,
            height = 106.dp,
            isHighlighted = expanded,
            shape = cardShape
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- CUERPO: LISTA HORIZONTAL DE ACCESOS DIRECTOS Y SELECCIONADOS ---
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shortcutItems.isEmpty() && activeFilters.isEmpty()) {
                        Text(
                            text = "MANTÉN PARA AÑADIR FAVORITOS",
                            color = Color.White.copy(alpha = 0.2f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Calculamos los IDs de shortcuts una vez
                    val shortcutIds = remember(shortcutItems) { shortcutItems.map { it.id }.toSet() }

                    // 1. Mostrar Accesos Directos (Favoritos)
                    shortcutItems.forEach { item ->
                        val isSelected = activeFilters.contains(item.id)
                        FilterChipSmall(
                            item = item,
                            isSelected = isSelected,
                            isShortcut = true,
                            onClick = { onToggle(item.id) },
                            onManageShortcuts = onManageShortcuts
                        )
                    }

                    // 2. Mostrar items ACTIVOS que NO están en los accesos directos (Seleccionados del menú)
                    dropdownItems.filter {
                        activeFilters.contains(it.id) && !shortcutIds.contains(it.id)
                    }.forEach { dropdownItem ->
                         FilterChipSmall(
                            item = FilterSortItem(
                                id = dropdownItem.id,
                                label = dropdownItem.label,
                                emoji = dropdownItem.emoji ?: "🔹",
                                icon = dropdownItem.icon,
                                color = dropdownItem.color,
                                section = dropdownItem.section
                            ),
                            isSelected = true,
                            isShortcut = false,
                            onClick = { onToggle(dropdownItem.id) },
                            onManageShortcuts = onManageShortcuts
                        )
                    }
                }

                // --- BOTÓN DE MENÚ A LA DERECHA (Convertible en X) ---
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (expanded) MaverickColors.DeepRed.copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (expanded) MaverickColors.DeepRed else Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .clickable { expanded = !expanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) MaverickIcons.Close else MaverickIcons.ExpandMore,
                        contentDescription = null,
                        tint = if (expanded) MaverickColors.DeepRed else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- MENÚ TÁCTICO PARA LA TARJETA COMPLETA (MODO IMPOSTOR) ---
        var showActionMenu by remember { mutableStateOf(false) }

        MenuTacticoBe(
            isVisible = showTacticalMenu,
            onDismissRequest = {
                showTacticalMenu = false
                showActionMenu = false
            },
            onAction = { 
                showActionMenu = !showActionMenu
                showTacticalMenu = false
            },
            touchOffset = touchOffset,
            emotion = BeEmotion.NORMAL,
            actionLabel = "VER ACCIONES",
            actionIconEmoji = "⚡"
        )


        // --- MENÚ ELITE DROPDOWN ---
        val shortcutIds = remember(shortcutItems) { shortcutItems.map { it.id }.toSet() }
        MoldeEliteDropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { expanded = false },
            items = dropdownItems,
            activeFilters = activeFilters,
            shortcutIds = shortcutIds,
            onToggle = onToggle,
            onManageShortcuts = onManageShortcuts,
            width = cardWidth
        )
    }
}

/**
 * Helper para animar la forma de la tarjeta (Esquinas inferiores se vuelven rectas al expandir)
 */
@Composable
private fun animateHorizontalEdgeShape(expanded: Boolean): Shape {
    val cornerAnim by animateDpAsState(
        targetValue = if (expanded) 2.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cornerAnim"
    )
    return RoundedCornerShape(
        topStart = 8.dp,
        topEnd = 8.dp,
        bottomStart = cornerAnim,
        bottomEnd = cornerAnim
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterChipSmall(
    item: FilterSortItem,
    isSelected: Boolean,
    isShortcut: Boolean,
    onClick: () -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit
) {
    val isCategory = item.section?.uppercase() == "CATEGORIAS"
    val haptic = LocalHapticFeedback.current
    var showTacticalMenu by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    // --- 🚀 OPTIMIZACIÓN ELITE: Animación de Shake Única para el Emoji ---
    val animatableRotation = remember { Animatable(0f) }
    
    LaunchedEffect(isSelected) {
        if (isSelected) {
            animatableRotation.animateTo(15f, tween(80, easing = LinearEasing))
            animatableRotation.animateTo(-15f, tween(80, easing = LinearEasing))
            animatableRotation.animateTo(0f, tween(80, easing = LinearEasing))
        } else {
            animatableRotation.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .width(58.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        touchOffset = offset
                        showTacticalMenu = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { onClick() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CutCornerShape(4.dp))
                .background(
                    if (isSelected) item.color.copy(alpha = 0.15f)
                    else Color.Black.copy(alpha = 0.4f)
                )
                .border(
                    width = if (isSelected) 1.5.dp else 0.8.dp,
                    color = if (isSelected) item.color else Color.White.copy(alpha = 0.12f),
                    shape = CutCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                // MODO ACTIVO: Emoji a color con brillo
                Text(
                    text = item.emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = animatableRotation.value },
                    style = TextStyle(
                        shadow = Shadow(
                            color = item.color,
                            offset = Offset(0f, 0f),
                            blurRadius = 15f
                        )
                    )
                )
            } else {
                // MODO DESACTIVADO: Lógica diferenciada
                if (isCategory) {
                    // Categoría: Emoji en tono gris (Grayscale)
                    Text(
                        text = item.emoji,
                        fontSize = 18.sp,
                        modifier = Modifier.graphicsLayer {
                            alpha = 0.5f
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        }
                    )
                } else {
                    // Otros: Icono gris monocromático
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        // Rescate: emoji en tono gris
                        Text(
                            text = item.emoji,
                            fontSize = 18.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.3f
                                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            }
                        )
                    }
                }
            }

            // 1. EL IMPOSTOR BE (DISPARADOR)
            MenuTacticoBe(
                isVisible = showTacticalMenu,
                onDismissRequest = {
                    showTacticalMenu = false
                    showActionMenu = false
                },
                onAction = { 
                    onManageShortcuts(item.id, !isShortcut)
                    showTacticalMenu = false
                    showActionMenu = false
                },
                touchOffset = touchOffset,
                emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
                actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
                actionIconEmoji = "📌"
            )


        }

        Text(
            text = item.label.uppercase(),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = LocalTextStyle.current
        )
    }
}

// ==========================================================================================
// --- SECCIÓN 2: MOLDE TARJETA DE ORDENAMIENTO (GRID 2x2) ---
// ==========================================================================================

@Composable
fun MoldePremiumSortCard(
    label: String,
    dropdownItems: List<DropdownItemData>,
    shortcutItems: List<FilterSortItem>,
    activeSorts: List<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // --- CÁLCULO DE ANCHO ELITE (Máximo 2 columnas visibles) ---
    val columnCount = if (dropdownItems.size <= 2) 1 else 2
    // 42dp (botón) + 4dp (espaciado entre cols) + paddings (aprox 8dp totales)
    val dynamicWidth = (columnCount * 42 + (columnCount - 1) * 4 + 10).dp

    MoldePremiumCardBase(
        modifier = modifier.width(dynamicWidth),
        label = label,
        height = 106.dp,
        innerPadding = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center, // 🔥 Centrado horizontal para simetría
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dividimos los items en grupos de 2 para crear las 2 filas dentro del scroll horizontal
            val chunkedItems = dropdownItems.chunked(2)

            chunkedItems.forEachIndexed { index, pair ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    pair.forEach { item ->
                        val isSelected = activeSorts.contains(item.id)
                        SortGridItemSmall(
                            item = item,
                            isSelected = isSelected,
                            onClick = { onToggle(item.id) }
                        )
                    }
                    // Si el grupo solo tiene 1 item, añadimos un espacio para mantener la alineación 2x2
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.size(42.dp))
                    }
                }
                // Añadimos espacio entre columnas solo si no es la última
                if (index < chunkedItems.size - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
fun SortGridItemSmall(
    item: DropdownItemData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // --- 🚀 OPTIMIZACIÓN ELITE: Animación de Shake Única para el Emoji ---
    val animatableRotation = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            animatableRotation.animateTo(15f, tween(80, easing = LinearEasing))
            animatableRotation.animateTo(-15f, tween(80, easing = LinearEasing))
            animatableRotation.animateTo(0f, tween(80, easing = LinearEasing))
        } else {
            animatableRotation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .size(42.dp) // 🔥 ELITE: Tamaño estándar sin label
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) item.color.copy(alpha = 0.15f)
                else Color.Black.copy(alpha = 0.4f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) item.color else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // MODO ACTIVO: Emoji a color con brillo y shake
            if (item.emoji != null) {
                Text(
                    text = item.emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = animatableRotation.value },
                    style = TextStyle(
                        shadow = Shadow(
                            color = item.color,
                            offset = Offset(0f, 0f),
                            blurRadius = 15f
                        )
                    )
                )
            } else {
                Icon(
                    imageVector = item.icon ?: MaverickIcons.Sort,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = animatableRotation.value }
                )
            }
        } else {
            // MODO DESACTIVADO: Icono/Emoji gris
            if (item.emoji != null) {
                Text(
                    text = item.emoji,
                    fontSize = 18.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = 0.3f
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    }
                )
            } else {
                Icon(
                    imageVector = item.icon ?: MaverickIcons.Sort,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 3: MENÚ ELITE DE FILTROS (2 COLUMNAS + GESTIÓN SHORTCUTS) ---
// ==========================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoldeEliteDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<DropdownItemData>,
    activeFilters: Set<String>,
    shortcutIds: Set<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit,
    width: Dp = 360.dp
) {
    // 🔥 ELITE: Sincronización de visibilidad para permitir animación de salida
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) isVisible = true
    }

    // Estado para colapsar/expandir secciones
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    if (isVisible || expanded) {
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(0, with(LocalDensity.current) { 108.dp.roundToPx() }), // 🔥 2dp de separación
            properties = PopupProperties(
                focusable = false,
                dismissOnClickOutside = false
            ),
            onDismissRequest = { onDismissRequest() }
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(200))
            ) {
                // Side effect to hide popup after exit animation
                DisposableEffect(Unit) {
                    onDispose {
                        if (!expanded) isVisible = false
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(width)
                        .zIndex(0f)
                ) {
                    // --- BODY ---
                    Column(
                        modifier = Modifier
                            .shadow(
                                elevation = 40.dp,
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                                spotColor = Color.Black,
                                ambientColor = Color.Black
                            )
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaverickColors.ROG_Dark_Bg,
                                        MaverickColors.AbsoluteBlack
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        // --- SECCIÓN: EXPLORAR TODO EL MENÚ ---
                        val sections = items.groupBy { it.section }

                        Column(
                            modifier = Modifier
                                .heightIn(max = 450.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            sections.forEach { (section, sectionItems) ->
                                val sectionKey = section ?: "OTRO"
                                val isSectionExpanded = expandedSections.getOrDefault(sectionKey, true)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (section != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    expandedSections[sectionKey] = !isSectionExpanded
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = section.uppercase(),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp
                                            )

                                            DepthDividerHorizontal(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 8.dp),
                                                thickness = 0.8.dp,
                                                shadowColor = Color.Black,
                                                highlightColor = Color.White.copy(alpha = 0.08f)
                                            )

                                            // 🔥 ELITE: Contenedor circular para la flecha
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isSectionExpanded) MaverickIcons.ExpandLess else MaverickIcons.ExpandMore,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = isSectionExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            // Grilla de 3 columnas para items de sección (Más anchas)
                                            val chunkedItems = sectionItems.chunked(3)
                                            chunkedItems.forEach { row ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    row.forEach { item ->
                                                        EliteMenuButton(
                                                            item = item,
                                                            isSelected = activeFilters.contains(item.id),
                                                            isShortcut = shortcutIds.contains(item.id),
                                                            onClick = { onToggle(item.id) },
                                                            onManageShortcuts = onManageShortcuts,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    repeat(3 - row.size) {
                                                        Spacer(Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EliteMenuButton(
    item: DropdownItemData,
    isSelected: Boolean,
    isShortcut: Boolean,
    onClick: () -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isCategory = item.section?.uppercase() == "CATEGORIAS"
    var showTacticalMenu by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    // Lógica de color y brillo
    val buttonColor = if (isCategory && !isSelected) Color.Gray else item.color
    val borderColor = if (isSelected) buttonColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
    val bgColor = if (isSelected) buttonColor.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        touchOffset = offset
                        showTacticalMenu = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 8.dp, vertical = 6.dp) // Más ancho y espaciado
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icono / Emoji
            Box(
                modifier = Modifier.size(26.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    // MODO ACTIVO: Emoji a color (si existe) o Icono con color
                    if (item.emoji != null) {
                        Text(item.emoji, fontSize = 18.sp)
                    } else {
                        Icon(
                            imageVector = item.icon ?: MaverickIcons.Filter,
                            contentDescription = null,
                            tint = item.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // MODO DESACTIVADO: Lógica diferenciada
                    val isCategory = item.section?.uppercase() == "CATEGORIAS"
                    if (isCategory) {
                        // Categoría: Emoji en tono gris (Grayscale)
                        Text(
                            text = item.emoji ?: "🔹",
                            fontSize = 18.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.5f
                                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            }
                        )
                    } else {
                        // Otros: Icono gris monocromático
                        Icon(
                            imageVector = item.icon ?: MaverickIcons.Filter,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Divisor profundo vertical
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label.uppercase(),
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
            }
        }

        // 1. EL IMPOSTOR BE (DISPARADOR)
        MenuTacticoBe(
            isVisible = showTacticalMenu,
            onDismissRequest = {
                showTacticalMenu = false
                showActionMenu = false
            },
            onAction = { 
                onManageShortcuts(item.id, !isShortcut)
                showTacticalMenu = false
                showActionMenu = false
            },
            touchOffset = touchOffset,
            emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
            actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
            actionIconEmoji = "📌"
        )


    }
}


// ==========================================================================================
// --- OTROS COMPONENTES ---
// ==========================================================================================

@Composable
fun MoldePremiumContextCard(
    modifier: Modifier = Modifier,
    user: com.example.myapplication.core.data.local.entity.UserEntity?,
    activeProfileName: String,
    activeProfilePhotoUrl: String?,
    mainAddress: String,
    localityInfo: String,
    description: String? = null, // Sucursal o Descripción personalizada
    isGpsActive: Boolean,
    onUserClick: () -> Unit,
    onLocationClick: () -> Unit,
    onGpsToggle: () -> Unit

) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp), // Un poco más alta para albergar los helpers
        horizontalArrangement = Arrangement.spacedBy(2.dp) // "Pegadas" con micro-separación visual
    ) {
        // --- TARJETA 1: PERFIL (LADO IZQUIERDO) ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 2.dp, bottomEnd = 2.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.95f), MaverickColors.VantaBlack)
                    ),
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 2.dp, bottomEnd = 2.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 2.dp, bottomEnd = 2.dp)
                )
                .clickable { onUserClick() }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    if (activeProfilePhotoUrl != null) {
                        // Dummy photo logic - In real app use AsyncImage
                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)))
                    } else {
                        Icon(MaverickIcons.Person, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp).align(Alignment.Center))
                    }
                }
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = if (user == null) "PERFIL INVITADO" else "PERFIL ACTIVO",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 7.sp
                    )
                    Text(
                        text = activeProfileName.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // --- TARJETA 2: DIRECCIÓN (PROTAGONISTA - LADO DERECHO) ---
        Box(
            modifier = Modifier
                .weight(1.8f) // PROTAGONISTA: Más ancha que el perfil
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.95f), MaverickColors.VantaBlack)
                    ),
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                )
                .clickable { onLocationClick() }
                .padding(start = 12.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Helper Superior: Sucursal o Descripción
                    if (!description.isNullOrBlank()) {
                        Text(
                            text = description.uppercase(),
                            color = MaverickColors.NeonCyan.copy(alpha = 0.6f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 7.sp
                        )
                    }

                    // Dirección Principal (Protagonista)
                    Text(
                        text = mainAddress.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 11.sp
                    )

                    // Helper Inferior: Localidad + CP
                    Text(
                        text = localityInfo.uppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 7.sp
                    )
                }

                // --- BOTÓN GPS (ESTILO M3 BOX) ---
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isGpsActive) MaverickColors.NeonCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable { onGpsToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isGpsActive) MaverickIcons.GpsOn else MaverickIcons.GpsOff,
                            contentDescription = null,
                            tint = if (isGpsActive) MaverickColors.NeonCyan else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isGpsActive) "ON" else "OFF",
                            color = if (isGpsActive) MaverickColors.NeonCyan else Color.White.copy(alpha = 0.2f),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 6.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- PREVIEW ELITE ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TarjetasModuloFiltrosPreview() {
    val mockFilters = listOf(
        FilterSortItem("1", "Suscritos", "⭐", icon = MaverickIcons.Check, color = Color.Yellow),
        FilterSortItem("2", "Cercanía", "📍", icon = MaverickIcons.Location, color = Color.Red),
        FilterSortItem("3", "24hs", "🕒", icon = MaverickIcons.Timer, color = Color.Green)
    )

    val mockDropdown = listOf(
        DropdownItemData("1", "Suscritos", "ESTADO", "⭐", MaverickIcons.Check, color = Color.Yellow),
        DropdownItemData("2", "Cercanía", "ESTADO", "📍", MaverickIcons.Location, color = Color.Red),
        DropdownItemData("3", "24hs", "SERVICIOS", "🕒", MaverickIcons.Timer, color = Color.Green),
        DropdownItemData("4", "Urgencias", "SERVICIOS", "🔥", MaverickIcons.Warning, color = Color.Red)
    )

    var isFilterExpanded by remember { mutableStateOf(false) }

    // 🔥 ELITE: Animación de peso para el efecto "expandir a la derecha"
    val filterWeight by animateFloatAsState(
        targetValue = if (isFilterExpanded) 4f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "weightAnim"
    )

    MyApplicationTheme {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                MoldePremiumFilterCard(
                    label = "Filtrar por",
                    dropdownItems = mockDropdown,
                    shortcutItems = mockFilters,
                    activeFilters = setOf("1"),
                    onToggle = {},
                    onManageShortcuts = { _, _ -> },
                    onExpandChanged = { isFilterExpanded = it },
                    modifier = Modifier.weight(filterWeight)
                )
                MoldePremiumSortCard(
                    label = "Ordenar por",
                    dropdownItems = mockDropdown,
                    shortcutItems = emptyList(),
                    activeSorts = listOf("1"),
                    onToggle = {},
                    onManageShortcuts = { _, _ -> },
                    modifier = if (isFilterExpanded) Modifier.width(0.dp).alpha(0f) else Modifier
                )
            }

            MoldePremiumContextCard(
                user = null,
                activeProfileName = "Invitado",
                activeProfilePhotoUrl = null,
                mainAddress = "Calle Falsa 123, Sector B",
                localityInfo = "Tucumán, CP 4000",
                description = "Mi Ubicación",
                isGpsActive = true,
                onUserClick = {},
                onLocationClick = {},
                onGpsToggle = {}
            )
        }
    }
}

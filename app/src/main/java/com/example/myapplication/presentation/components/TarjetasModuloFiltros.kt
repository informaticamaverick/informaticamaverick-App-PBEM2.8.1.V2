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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.MenuTacticoBe
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

// ==========================================================================================
// --- MODELOS DE DATOS ---
// ==========================================================================================

/**
 * MaverickFilterItem: Modelo unificado para filtros, ordenamiento y accesos directos.
 */
data class MaverickFilterItem(
    val id: String,
    val label: String,
    val section: String? = null,
    val emoji: String? = null,
    val icon: ImageVector? = null,
    val color: Color = Color.White
) {
    /**
     * 🔥 ELITE: Convierte datos de filtro a items de control para el HUD.
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

// Retrocompatibilidad con nombres antiguos
typealias DropdownItemData = MaverickFilterItem
typealias FilterSortItem = MaverickFilterItem

// ==========================================================================================
// --- MOLDES BASE (UI ATOMS) ---
// ==========================================================================================

@Composable
private fun HeaderActionButtonV2(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    onClick: () -> Unit,
    backgroundColor: Color = Color.White.copy(0.05f),
    borderColor: Color = Color.White.copy(0.1f),
    contentColor: Color = Color.White.copy(alpha = 0.8f)
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(28.dp),
        color = backgroundColor,
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            } else if (emoji != null) {
                Text(emoji, fontSize = 14.sp)
            }
        }
    }
}

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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

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
        if (label != null) {
            Text(
                text = label.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = if (label.length > 9) 8.sp else 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = if (label.length > 9) 0.5.sp else 1.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (label != null) Modifier.weight(1f) else Modifier.fillMaxHeight())
                .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.9f), MaverickColors.VantaBlack)
                    ),
                    shape = shape
                )
                .border(width = 1.dp, color = borderColor, shape = shape)
                .padding(innerPadding)
        ) {
            content()
        }
        
        Spacer(modifier = Modifier.height(14.dp))
    }
}

// ==========================================================================================
// --- SECCIÓN 1: MOLDE TARJETA DE FILTROS ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoldePremiumFilterCard(
    label: String,
    dropdownItems: List<MaverickFilterItem>,
    shortcutItems: List<MaverickFilterItem>,
    activeFilters: Set<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isSheetVisible: Boolean = false,
    onSheetVisibilityChange: (Boolean) -> Unit = {}
) {
    Box(modifier = modifier.zIndex(1f)) {
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
            isHighlighted = isSheetVisible,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                    val shortcutIds = remember(shortcutItems) { shortcutItems.map { it.id }.toSet() }

                    shortcutItems.forEach { item ->
                        FilterChipSmall(
                            item = item,
                            isSelected = activeFilters.contains(item.id),
                            isShortcut = true,
                            onClick = { onToggle(item.id) },
                            onManageShortcuts = onManageShortcuts
                        )
                    }

                    dropdownItems.filter {
                        activeFilters.contains(it.id) && !shortcutIds.contains(it.id)
                    }.forEach { item ->
                         FilterChipSmall(
                            item = item,
                            isSelected = true,
                            isShortcut = false,
                            onClick = { onToggle(item.id) },
                            onManageShortcuts = onManageShortcuts
                        )
                    }
                }

                HeaderActionButtonV2(
                    icon = if (isSheetVisible) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    onClick = { onSheetVisibilityChange(!isSheetVisible) },
                    backgroundColor = if (isSheetVisible) MaverickColors.DeepRed.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f),
                    borderColor = if (isSheetVisible) MaverickColors.DeepRed.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                    contentColor = if (isSheetVisible) MaverickColors.DeepRed else Color.White.copy(alpha = 0.8f)
                )
            }
        }

        MenuTacticoBe(
            isVisible = showTacticalMenu,
            onDismissRequest = { showTacticalMenu = false },
            onAction = { showTacticalMenu = false },
            touchOffset = touchOffset,
            emotion = BeEmotion.NORMAL,
            actionLabel = "ACCIONES DE FILTRO",
            actionIconEmoji = "⚡"
        )

        MoldeEliteBottomSheetV2(
            visible = isSheetVisible,
            onDismissRequest = { onSheetVisibilityChange(false) },
            items = dropdownItems,
            activeFilters = activeFilters,
            shortcutIds = shortcutItems.map { it.id }.toSet(),
            onToggle = onToggle,
            onManageShortcuts = onManageShortcuts
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterChipSmall(
    item: MaverickFilterItem,
    isSelected: Boolean,
    isShortcut: Boolean,
    onClick: () -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showTacticalMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

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
                .background(if (isSelected) item.color.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.4f))
                .border(
                    width = if (isSelected) 1.5.dp else 0.8.dp,
                    color = if (isSelected) item.color else Color.White.copy(alpha = 0.12f),
                    shape = CutCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            val isCategory = item.section?.uppercase() == "CATEGORIAS"
            if (isSelected) {
                Text(
                    text = item.emoji ?: "🔹",
                    fontSize = 22.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = animatableRotation.value },
                    style = TextStyle(shadow = Shadow(color = item.color, offset = Offset(0f, 0f), blurRadius = 15f))
                )
            } else {
                if (isCategory) {
                    Text(
                        text = item.emoji ?: "🔹",
                        fontSize = 20.sp,
                        modifier = Modifier.graphicsLayer {
                            alpha = 0.5f
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        }
                    )
                } else if (item.icon != null) {
                    Icon(item.icon, null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = item.emoji ?: "🔹",
                        fontSize = 20.sp,
                        modifier = Modifier.graphicsLayer {
                            alpha = 0.3f
                            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        }
                    )
                }
            }

            MenuTacticoBe(
                isVisible = showTacticalMenu,
                onDismissRequest = { showTacticalMenu = false },
                onAction = { 
                    onManageShortcuts(item.id, !isShortcut)
                    showTacticalMenu = false
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
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================================================================
// --- SECCIÓN 2: MOLDE TARJETA DE ORDENAMIENTO ---
// ==========================================================================================

@Composable
fun MoldePremiumSortCard(
    label: String,
    dropdownItems: List<MaverickFilterItem>,
    shortcutItems: List<MaverickFilterItem> = emptyList(),
    activeSorts: List<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val itemsToShow = dropdownItems.take(4)
    val columnCount = if (itemsToShow.size <= 2) 1 else 2
    val finalLabel = if (columnCount == 1) "ORDENAR" else label.uppercase()
    val dynamicWidth = if (columnCount == 1) 68.dp else 102.dp

    MoldePremiumCardBase(
        modifier = modifier.width(dynamicWidth),
        label = finalLabel,
        height = 106.dp,
        innerPadding = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsToShow.chunked(2).forEach { pair ->
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    pair.forEach { item ->
                        SortGridItemSmall(
                            item = item,
                            isSelected = activeSorts.contains(item.id),
                            onClick = { onToggle(item.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SortGridItemSmall(
    item: MaverickFilterItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
            .width(42.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) item.color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) item.color else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            if (item.emoji != null) {
                Text(
                    text = item.emoji,
                    fontSize = 22.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = animatableRotation.value },
                    style = TextStyle(shadow = Shadow(color = item.color, offset = Offset(0f, 0f), blurRadius = 15f))
                )
            } else {
                Icon(
                    imageVector = item.icon ?: MaverickIcons.Sort,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = animatableRotation.value }
                )
            }
        } else {
            if (item.emoji != null) {
                Text(
                    text = item.emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer {
                        alpha = 0.4f
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                    }
                )
            } else {
                Icon(
                    imageVector = item.icon ?: MaverickIcons.Sort,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 3: MENÚ ELITE DE FILTROS (BOTTOM SHEET) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoldeEliteBottomSheetV2(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    items: List<MaverickFilterItem>,
    activeFilters: Set<String>,
    shortcutIds: Set<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = Color.Transparent, // 🔥 Transparente para manejar el fondo nosotros
            scrimColor = Color.Black.copy(alpha = 0.5f), // 🔥 Scrim más profundo para depth
            dragHandle = null, // 🔥 Quitamos el dragHandle nativo para integrarlo en nuestra estructura
            shape = CutCornerShape(topStart = 8.dp, topEnd = 8.dp),
            tonalElevation = 0.dp
        ) {
            // Contenedor principal con altura máxima absoluta (Incluyendo Cabecera y Lista)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp) // 🔥 Límite de crecimiento absoluto
                    .clip(CutCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(MaverickColors.AbsoluteBlack)
            ) {
                // 1. CABECERA TÁCTICA (Integrada)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 1.6.dp.toPx()
                            val path = Path().apply {
                                moveTo(0f, size.height)
                                lineTo(0f, 8.dp.toPx())
                                lineTo(8.dp.toPx(), 0f)
                                lineTo(size.width - 8.dp.toPx(), 0f)
                                lineTo(size.width, 8.dp.toPx())
                                lineTo(size.width, size.height)
                            }

                            // Borde base en escala de grises
                            val borderGradient = Brush.horizontalGradient(
                                0.0f to Color.Black,
                                0.2f to Color.DarkGray,
                                0.5f to Color.Gray,
                                0.8f to Color.DarkGray,
                                1.0f to Color.Black
                            )

                            drawPath(
                                path = path,
                                brush = borderGradient,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // HIGHLIGHT SUPERIOR
                            val topRimPath = Path().apply {
                                moveTo(0f, 8.dp.toPx())
                                lineTo(8.dp.toPx(), 0f)
                                lineTo(size.width - 8.dp.toPx(), 0f)
                                lineTo(size.width, 8.dp.toPx())
                            }
                            drawPath(
                                path = topRimPath,
                                color = Color.White.copy(alpha = 0.25f),
                                style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round)
                            )
                        }
                ) {
                    // --- SOMBRA EXTERIOR SUPERIOR ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                                )
                            )
                    )

                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(
                            text = "MANTÉN PARA FAVORITOS",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activeFilters.isNotEmpty()) {
                                HeaderActionButtonV2(
                                    icon = Icons.Default.FilterAltOff,
                                    onClick = { onToggle("CLEAR_ALL") },
                                    backgroundColor = MaverickColors.DeepRed.copy(alpha = 0.1f),
                                    borderColor = MaverickColors.DeepRed.copy(alpha = 0.3f),
                                    contentColor = MaverickColors.DeepRed
                                )
                            }
                            HeaderActionButtonV2(
                                icon = Icons.Default.ArrowDownward,
                                onClick = onDismissRequest,
                                backgroundColor = Color.White.copy(alpha = 0.05f),
                                borderColor = Color.White.copy(alpha = 0.1f),
                                contentColor = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                    DepthDividerHorizontal(
                        thickness = 0.8.dp, 
                        shadowColor = Color.Black.copy(alpha = 0.9f), 
                        highlightColor = Color.White.copy(alpha = 0.08f)
                    )
                }

                // 2. CUERPO DE FILTROS (Scrollable)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false) // 🔥 Solo crece si hay items, pero respeta el límite
                        .background(MaverickColors.EliteSurface)
                        .drawBehind {
                            val strokeWidth = 1.2.dp.toPx()
                            val borderColor = Color.Gray.copy(alpha = 0.2f)
                            drawLine(borderColor, Offset(0f, 0f), Offset(0f, size.height), strokeWidth)
                            drawLine(borderColor, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth)
                        }
                ) {
                    MoldeEliteBottomSheetContent(items, activeFilters, shortcutIds, onToggle, onManageShortcuts)
                }
            }
        }
    }
}

@Composable
private fun MoldeEliteBottomSheetContent(
    items: List<MaverickFilterItem>,
    activeFilters: Set<String>,
    shortcutIds: Set<String>,
    onToggle: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit
) {
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp).padding(bottom = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.groupBy { it.section }.forEach { (section, sectionItems) ->
                val sectionKey = section ?: "OTRO"
                val isExpanded = expandedSections.getOrDefault(sectionKey, true)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (section != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { expandedSections[sectionKey] = !isExpanded },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = section.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            DepthDividerHorizontal(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), thickness = 0.8.dp)
                            Icon(
                                imageVector = if (isExpanded) MaverickIcons.ExpandLess else MaverickIcons.ExpandMore,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sectionItems.chunked(3).forEach { row ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    if (row.size < 3) repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
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
    item: MaverickFilterItem,
    isSelected: Boolean,
    isShortcut: Boolean,
    onClick: () -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showTacticalMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

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

    val isCategory = item.section?.uppercase() == "CATEGORIAS"
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
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(26.dp).graphicsLayer { rotationZ = animatableRotation.value }, contentAlignment = Alignment.Center) {
                if (isSelected) {
                    if (item.emoji != null) Text(item.emoji, fontSize = 18.sp)
                    else Icon(item.icon ?: MaverickIcons.Filter, null, tint = item.color, modifier = Modifier.size(20.dp))
                } else {
                    if (isCategory) {
                        Text(
                            text = item.emoji ?: "🔹",
                            fontSize = 18.sp,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.5f
                                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                            }
                        )
                    } else {
                        Icon(item.icon ?: MaverickIcons.Filter, null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
            }
            Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.08f)))
            Text(
                text = item.label.uppercase(),
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        MenuTacticoBe(
            isVisible = showTacticalMenu,
            onDismissRequest = { showTacticalMenu = false },
            onAction = { 
                onManageShortcuts(item.id, !isShortcut)
                showTacticalMenu = false
            },
            touchOffset = touchOffset,
            emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
            actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
            actionIconEmoji = "📌"
        )
    }
}

// ==========================================================================================
// --- CONTEXT CARD ---
// ==========================================================================================

@Composable
fun MoldePremiumContextCard(
    modifier: Modifier = Modifier,
    user: com.example.myapplication.core.data.local.entity.UserEntity?,
    activeProfileName: String,
    activeProfilePhotoUrl: String?,
    mainAddress: String,
    localityInfo: String,
    description: String? = null,
    isGpsActive: Boolean,
    onUserClick: () -> Unit,
    onLocationClick: () -> Unit,
    onGpsToggle: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth().height(64.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        // PERFIL
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 2.dp, bottomEnd = 2.dp))
                .background(
                    brush = Brush.verticalGradient(listOf(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.95f), MaverickColors.VantaBlack)),
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 2.dp, bottomEnd = 2.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 2.dp, bottomEnd = 2.dp))
                .clickable { onUserClick() }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)).border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)) {
                    if (activeProfilePhotoUrl != null) Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)))
                    else Icon(MaverickIcons.Person, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp).align(Alignment.Center))
                }
                Column(verticalArrangement = Arrangement.Center) {
                    Text(text = if (user == null) "PERFIL INVITADO" else "PERFIL ACTIVO", color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(text = activeProfileName.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        }

        // DIRECCIÓN
        Box(
            modifier = Modifier
                .weight(1.8f)
                .fillMaxHeight()
                .shadow(4.dp, RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .background(
                    brush = Brush.verticalGradient(listOf(MaverickColors.ROG_Dark_Bg.copy(alpha = 0.95f), MaverickColors.VantaBlack)),
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 10.dp, bottomEnd = 10.dp))
                .clickable { onLocationClick() }
                .padding(start = 12.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    if (!description.isNullOrBlank()) Text(text = description.uppercase(), color = MaverickColors.NeonCyan.copy(alpha = 0.6f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(text = mainAddress.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = localityInfo.uppercase(), color = Color.White.copy(alpha = 0.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.padding(start = 4.dp).size(44.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isGpsActive) MaverickColors.NeonCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .clickable { onGpsToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = if (isGpsActive) MaverickIcons.GpsOn else MaverickIcons.GpsOff, null, tint = if (isGpsActive) MaverickColors.NeonCyan else Color.White.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
                        Text(text = if (isGpsActive) "ON" else "OFF", color = if (isGpsActive) MaverickColors.NeonCyan else Color.White.copy(alpha = 0.2f), fontSize = 6.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- PREVIEW ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun FilterBottomSheetPreview() {
    val mockItems = listOf(
        MaverickFilterItem("1", "Suscritos", "ESTADO", "⭐", MaverickIcons.Check, color = Color.Yellow),
        MaverickFilterItem("2", "Cercanía", "ESTADO", "📍", MaverickIcons.Location, color = Color.Red),
        MaverickFilterItem("5", "Digital", "CATEGORIAS", "💻", color = Color.Cyan)
    )

    MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxWidth(), color = MaverickColors.ROG_Dark_Bg, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
            MoldeEliteBottomSheetContent(mockItems, setOf("1", "5"), setOf("1", "2"), {}, { _, _ -> })
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TarjetasModuloFiltrosPreview() {
    val mockItems = listOf(
        MaverickFilterItem("1", "Suscritos", "ESTADO", "⭐", MaverickIcons.Check, color = Color.Yellow),
        MaverickFilterItem("2", "Cercanía", "ESTADO", "📍", MaverickIcons.Location, color = Color.Red),
        MaverickFilterItem("3", "24hs", "SERVICIOS", "🕒", MaverickIcons.Timer, color = Color.Green),
        MaverickFilterItem("4", "Urgencias", "SERVICIOS", "🔥", MaverickIcons.Warning, color = Color.Red)
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
                    dropdownItems = mockItems,
                    shortcutItems = mockItems.take(3),
                    activeFilters = setOf("1"),
                    onToggle = {},
                    onManageShortcuts = { _, _ -> },
                    modifier = Modifier.weight(1f)
                )
                MoldePremiumSortCard(
                    label = "Ordenar por",
                    dropdownItems = mockItems,
                    shortcutItems = emptyList(),
                    activeSorts = listOf("1"),
                    onToggle = {},
                    onManageShortcuts = { _, _ -> },
                    modifier = Modifier.width(102.dp)
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

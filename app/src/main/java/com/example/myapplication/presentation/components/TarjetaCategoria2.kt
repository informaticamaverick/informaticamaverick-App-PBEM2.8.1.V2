package com.example.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.presentation.features.home.CategoryVisuals
import com.example.myapplication.presentation.features.home.SuperCategory
import com.example.myapplication.presentation.designsystem.components.MenuTacticoBe

import com.example.myapplication.presentation.designsystem.components.BeMenuItem
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.components.BeEmotion

// ==========================================================================================
// ------------------- PIN DE FAVORITOS (REUTILIZABLE) -----------------------------------
// ==========================================================================================
@Composable
fun FavoritePinBadge(
    isFavorite: Boolean, 
    modifier: Modifier = Modifier,
    isIndirect: Boolean = false
) {
    if (isFavorite || isIndirect) {
        Box(
            modifier = modifier
                .size(32.dp)
                .background(
                    if (isFavorite) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📌",
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer {
                    if (isIndirect && !isFavorite) {
                        // Aplicamos un filtro gris si es indirecto
                        alpha = 0.5f
                    }
                },
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

// ==========================================================================================
// ------------------------ NUEVA TARJETA CATEGORIA VERTICAL BENTO GLASS ---
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactCategoryCard(
    item: CategoryEntity, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {}
) {
    val baseColor = Color(CategoryVisuals.getColorFor(item.superCategory))
    val haptic = LocalHapticFeedback.current
    // ==========================================================================================
    // SECCIÓN: ESTADOS PARA EL CONTROL DE MENÚS Y BADGES
    // ==========================================================================================
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp) // Altura equilibrada
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        touchOffset = offset
                        showContextMenu = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        // --- 1. TARJETA BASE ---
        Card(
            shape = RoundedCornerShape(6.dp), // Esquinas profesionales
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // FONDO: Saturado Mate (Top) -> Negro Mate (Bottom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 1f),
                                    Color(0xFF080A0F) // Negro mate profundo
                                ),
                                startY = 100f,
                                endY = 550f
                            )
                        )
                )
                // CAPA DE DIFUMINADO SUPERIOR (Neblina de color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.73f)
                        .blur(5.dp)
                        .background(baseColor.copy(alpha = 0.9f))
                )
                // ==========================================================================================
                // SECCIÓN: EMOJI CENTRAL (SOMBRA Y PRINCIPAL)
                // ==========================================================================================
                // EMOJI CENTRAL: SOMBRA PROYECTADA (Silueta negra desfasada para efecto real)
                Text(
                    text = item.icon,
                    fontSize = 100.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-14).dp) // Desplazado hacia abajo respecto al original (-30)
                        .graphicsLayer {
                            alpha = 0.9f
                            // Convertimos el emoji en una silueta negra
                            colorFilter = ColorFilter.tint(Color.Black)
                        }
                        .blur(2.dp) // Desenfoque mínimo para realismo
                )

                // EMOJI CENTRAL: PRINCIPAL
                Text(
                    text = item.icon,
                    fontSize = 100.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-20).dp)
                )

                // ==========================================================================================
                // SECCIÓN: BADGES (NOTIFICACIÓN, FAVORITO E INFORMACIÓN) - SUPERPUESTOS AL EMOJI
                // ==========================================================================================
                // 1. BADGE DE NOTIFICACIÓN (Extremo Izquierdo Superior)
                if (item.isNew || item.isNewPrestador || item.isAd) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(2.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = Color(0xFFFF9800).copy(alpha = 0.9f), // Naranja para notificaciones
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🔔", fontSize = 14.sp)
                            }
                        }
                    }
                }

                // 2. BADGE DE FAVORITO (Extremo Derecho Superior)
                if (item.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                    ) {
                        FavoritePinBadge(
                            isFavorite = true
                        )
                    }
                }

                // 3. BOTÓN DE INFORMACIÓN (REUBICADO AL BORDE INFERIOR DERECHO DEL ICONO)
                var showInfoMenu by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 1.dp, y = (-40).dp)
                ) {
                    Surface(
                        onClick = { showInfoMenu = !showInfoMenu },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Color(0xFF1A1C1E), // FONDO OPACO (No transparente)
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "ℹ️", fontSize = 14.sp)
                        }
                    }

                    // Menú desplegable con la descripción de la categoría
                    DropdownMenu(
                        expanded = showInfoMenu,
                        onDismissRequest = { showInfoMenu = false },
                        modifier = Modifier
                            .width(220.dp)
                            .background(Color(0xFF1A1C1E))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.description.ifEmpty { "Explora los servicios disponibles en ${item.name}." },
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // ==========================================================================================
                // SECCIÓN: INFERIOR - TEXTO Y BADGE INFORMATIVO (REDiseño para Máximo Espacio)
                // ==========================================================================================
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(53.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. DIVIDER HORIZONTAL DE PROFUNDIDAD
                    DepthDividerHorizontal(
                        shadowColor = Color.Black.copy(alpha = 0.5f),
                        highlightColor = Color.White.copy(alpha = 0.25f)
                    )

                    // 2. CONTENEDOR DE CONTENIDO (TEXTO + BADGE CON DIVIDER VERTICAL)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- SUBSECCIÓN: NOMBRE DE LA CATEGORÍA ---
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 2.dp, end = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AutoSizeText(
                                text = item.name.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp, // Tamaño base ajustado
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.1.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }

        // --- MENU CONTEXTUAL PARA FAVORITOS (MENU TACTICO BE) ---
        MenuTacticoBe(
            isVisible = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            onAction = {
                onManageShortcut(!isShortcut)
                onToggleFavorite()
                showContextMenu = false
            },
            touchOffset = touchOffset,
            emotion = if (isShortcut) BeEmotion.HAPPY else BeEmotion.NORMAL,
            actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
            actionIconEmoji = "📌"
        )
    }
}

// ==========================================================================================
// ------------------------ NUEVA TARJETA CATEGORIA MINI BENTO GLASS (FAST SCREEN) ---
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniCompactCategoryCard(
    item: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val baseColor = Color(CategoryVisuals.getColorFor(item.superCategory))

    Box(
        modifier = Modifier
            .size(width = 85.dp, height = 110.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, if (isSelected) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // FONDO: Saturado Mate (Top) -> Negro Mate (Bottom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    if (isSelected) baseColor else baseColor.copy(alpha = 0.6f),
                                    Color(0xFF080A0F)
                                ),
                                startY = 0f,
                                endY = 300f
                            )
                        )
                )
                // CAPA DE DIFUMINADO SUPERIOR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                        .blur(4.dp)
                        .background(baseColor.copy(alpha = if (isSelected) 0.8f else 0.4f))
                )

                // EMOJI CENTRAL: SOMBRA
                Text(
                    text = item.icon,
                    fontSize = 44.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-10).dp)
                        .graphicsLayer {
                            alpha = 0.8f
                            colorFilter = ColorFilter.tint(Color.Black)
                        }
                        .blur(1.5.dp)
                )

                // EMOJI CENTRAL: PRINCIPAL
                Text(
                    text = item.icon,
                    fontSize = 44.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-12).dp)
                )

                // SECCIÓN INFERIOR: TEXTO
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = if (isSelected) 0.6f else 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoSizeText(
                            text = item.name.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isSelected) Color.White else Color.Gray,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// ------------------- TARJETA CATEGORIA HORIZONTAL--------------------------------
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactCategoryCardHorizontal(
    item: CategoryEntity, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {}
) {
    val baseColor = Color(CategoryVisuals.getColorFor(item.superCategory))
    val haptic = LocalHapticFeedback.current
    // ==========================================================================================
    // SECCIÓN: ESTADOS PARA EL CONTROL DE MENÚS Y BADGES
    // ==========================================================================================
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        touchOffset = offset
                        showContextMenu = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // EFECTO OVERLAY (Brillo de cristal)
                Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache {
                    val gradient = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) }
                })

                // DEGRADADO OSCURO
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.99f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            startX = 100f,
                            endX = 600f
                        )
                    )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SECCIÓN IZQUIERDA (Texto)
                    Box(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = item.name.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // DIVIDER INFERIOR DE TEXTO
                            Box(modifier = Modifier.width(30.dp).height(2.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                        }
                    }

                    // DIVIDER VERTICAL + BADGE INFORMATIVO CENTRADO
                    Box(
                        modifier = Modifier.fillMaxHeight().width(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Línea del Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(1f)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent)
                                    )
                                )
                        )
                        // Badge Informativo: Conectado con la descripción de la base de datos
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                        ) {
                            Text(text = "✨", fontSize = 14.sp, modifier = Modifier.graphicsLayer { alpha = 0.85f })
                        }
                    }
                    // SECCIÓN DERECHA (Icono)
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        // SOMBRA PROYECTADA (Silueta negra desfasada para efecto real)
                        Text(
                            text = item.icon,
                            fontSize = 64.sp,
                            modifier = Modifier
                                .offset(y = 4.dp) // Desfase hacia abajo
                                .graphicsLayer {
                                    alpha = 0.6f
                                    colorFilter = ColorFilter.tint(Color.Black)
                                }
                                .blur(2.dp)
                        )

                        // EMOJI PRINCIPAL
                        Text(
                            text = item.icon,
                            fontSize = 64.sp
                        )
                    }
                }
            }
        }

        // ==========================================================================================
        // SECCIÓN: BADGES SUPERIORES (NOTIFICACIÓN Y FAVORITO)
        // ==========================================================================================
        // 1. BADGE DE NOTIFICACIÓN (Extremo Izquierdo Superior)
        if (item.isNew || item.isNewPrestador || item.isAd) {
            Box(modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp, start = 8.dp)) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF9800).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔔", fontSize = 14.sp)
                    }
                }
            }
        }

        // 2. BADGE DE FAVORITO (Extremo Derecho Superior)
        if (item.isFavorite) {
            FavoritePinBadge(
                isFavorite = true,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 8.dp)
            )
        }

        // --- MENU CONTEXTUAL PARA FAVORITOS (MENU TACTICO BE) ---
        MenuTacticoBe(
            isVisible = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            onAction = {
                onManageShortcut(!isShortcut)
                onToggleFavorite()
                showContextMenu = false
            },
            touchOffset = touchOffset,
            emotion = if (isShortcut) BeEmotion.HAPPY else BeEmotion.NORMAL,
            actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
            actionIconEmoji = "📌"
        )
    }
}

// ==========================================================================================
// ------------------- TARJETA ESTILO BENTO PARA SUPERCATEGORÍAS (Extraída de HomeScreenCliente3)--------------------------------
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSuperCategoryCard(
    superCategory: SuperCategory, 
    emoji: String, 
    height: Dp, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // SECCIÓN: Contenedor para permitir que el pin sobresalga (efecto Badge)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(top = 8.dp, start = 2.dp, end = 2.dp) // Sincronización Maverick: Margen simétrico para centrado perfecto
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // --- SOMBRA 3D PERSONALIZADA (Estilo LicitacionFolder - Bottom & Sides only) ---
                    val shadowColor = Color.Black.copy(alpha = if (isPressed) 1f else 1f)
                    val shadowRadius = if (isPressed) 12.dp.toPx() else 8.dp.toPx()
                    val offsetY = if (isPressed) 6.dp.toPx() else 4.dp.toPx()

                    drawIntoCanvas { canvas ->
                        val paint = Paint().asFrameworkPaint().apply {
                            color = shadowColor.toArgb()
                            setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb())
                        }
                        canvas.nativeCanvas.drawRoundRect(
                            0f,
                            offsetY, // Empezamos desde abajo para que no se vea arriba
                            size.width,
                            size.height,
                            8.dp.toPx(), // Radio de esquina
                            8.dp.toPx(),
                            paint
                        )
                    }
                }
                .graphicsLayer {
                    val scale = if (isPressed) 0.98f else 1f
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            touchOffset = offset
                            showContextMenu = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onTap = { onClick() }
                    )
                },
            // SECCIÓN: Esquinas equilibradas y bordes reforzados (Sincronizado con LicitacionFolder)
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)), 
            color = Color(0xFF1A1C1E)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo con degradado
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(0.85f)
                            )
                        )
                    ))


                // Iconos internos difuminados (Optimizado: Reemplazado LazyVerticalGrid por Row/Column estático)
                Box(modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 16.dp) // Reducido levemente para mejorar performance
                    .alpha(0.25f)) {
                //    val bgItems = superCategory.items.take(4) // Máximo 4 para el fondo
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                          //  bgItems.getOrNull(0)?.let { Text(it.icon, fontSize = 60.sp) }
                          //  bgItems.getOrNull(1)?.let { Text(it.icon, fontSize = 60.sp) }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                           // bgItems.getOrNull(2)?.let { Text(it.icon, fontSize = 60.sp) }
                           // bgItems.getOrNull(3)?.let { Text(it.icon, fontSize = 60.sp) }
                        }
                    }
                }

                // SECCIÓN: Contenido Principal (Icono, Servicios y Título) agrupados para reducir espacio
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp) // Reducido para mayor compacidad
                ) {
                    // ==========================================================================================
                    // SECCIÓN: FILA SUPERIOR (EMOJI CON SOMBRA Y CONTADOR DE SERVICIOS)
                    // ==========================================================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom, // Pegados hacia abajo
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // CONTENEDOR DE EMOJI: Superpone la sombra detrás del emoji principal
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // EMOJI (SOMBRA): Silueta negra con desenfoque, posicionada detrás
                            Text(
                                text = emoji,
                                fontSize = 48.sp, // Tamaño ajustado para compacidad
                                modifier = Modifier
                                    .offset(x = 3.dp, y = 3.dp)
                                    .graphicsLayer {
                                        alpha = 0.9f
                                        colorFilter = ColorFilter.tint(Color.Black)
                                    }
                                    .blur(3.dp)
                            )

                            // EMOJI (COLOR): El emoji principal al frente
                            Text(
                                text = emoji,
                                fontSize = 48.sp,
                                modifier = Modifier.alpha(1f)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // CANTIDAD DE SERVICIOS: Columna alineada al lado del emoji
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.Bottom // Pegados hacia abajo


                        ) {
                            Surface(
                                color = Color.Gray.copy(alpha = 0.15f), // Color gris en la caja
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.35f))
                            ) {
                                // [OPTIMIZACIÓN]: Usamos totalItems (contador directo de SQLite) 
                                // en lugar de contar la lista de items en memoria.
                                val countToShow = if (superCategory.totalItems > 0) superCategory.totalItems else superCategory.items.size
                                Text(
                                    text = countToShow.toString(),
                                    color = Color.White, // El numero debe ser blanco
                                    fontSize = 18.sp, // Numero mas chico (era 22)
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "SERVICIOS",
                                    color = Color.Gray.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp,
                                        lineHeight = 8.sp
                                    )
                                )
                                Text(
                                    text = "PROFESIONES",
                                    color = Color.Gray.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp,
                                        lineHeight = 8.sp
                                    )
                                )
                            }
                        }
                    }
                    
                    // DIVIDER DE PROFUNDIDAD (Separador de Secciones)
                    DepthDividerHorizontal(
                        shadowColor = Color.Black.copy(alpha = 0.5f),
                        highlightColor = Color.White.copy(alpha = 0.05f)
                    )
                    
                    // Título de la Supercategoría (Detalle) - AutoAjustable a 2 líneas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoSizeText(
                            text = superCategory.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 16.sp // Interlineado mas compacto
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // --- MENU CONTEXTUAL PARA SUPERCATEGORÍAS (MENU TACTICO BE) ---
                MenuTacticoBe(
                    isVisible = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    onAction = {
                        onManageShortcut(!isShortcut)
                        onToggleFavorite()
                        showContextMenu = false
                    },
                    touchOffset = touchOffset,
                    emotion = if (isShortcut) BeEmotion.HAPPY else BeEmotion.NORMAL,
                    actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
                    actionIconEmoji = "📌"
                )
            }
        }

        // SECCIÓN: Pin de favorito (📌) con OFFSET para sobresalir del borde superior derecho
        FavoritePinBadge(
            isFavorite = superCategory.isFavorite,
            isIndirect = superCategory.hasFavoriteCategories,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (4).dp)
        )
    }
}


// ==========================================================================================
// ------------------- TARJETA EXPANDIDA PARA RESULTADOS DE BÚSQUEDA ------------------------
// ==========================================================================================
@Composable
fun ExpandedBentoSuperCategoryCard(
    superCategory: SuperCategory,
    onCategoryClick: (CategoryEntity) -> Unit,
    onToggleCategoryFavorite: (CategoryEntity) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(12.dp, RoundedCornerShape(6.dp)),
        // SECCIÓN: Esquinas menos redondeadas según requerimiento
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con degradado (efecto bento)
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(0.85f)
                        )
                    )
                ))
            
            Column(modifier = Modifier.padding(6.dp)) {
                // HEADER: Nombre a la izquierda, Icono a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge de favorito en el header
                        Text(text = superCategory.icon, fontSize = 28.sp)

                        if (superCategory.isFavorite || superCategory.hasFavoriteCategories) Spacer(modifier = Modifier.width(8.dp))

                        AutoSizeText(
                            text = superCategory.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 2
                        )
                    }
                    FavoritePinBadge(
                        isFavorite = superCategory.isFavorite,
                        isIndirect = superCategory.hasFavoriteCategories,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                // LISTA HORIZONTAL con 3 items visibles aprox
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = superCategory.items, key = { it.name }) { category ->
                        // Ajustamos ancho a ~110.dp para que quepan 3 aprox en pantalla
                        Box(modifier = Modifier.width(125.dp)) {
                            CompactCategoryCard(
                                item = category,
                                onClick = { onCategoryClick(category) },
                                onToggleFavorite = { onToggleCategoryFavorite(category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

//==========================================================================================
// ------------------- PREVIEWS--------------------------------
// ==========================================================================================


@Preview(showBackground = true)
@Composable
fun CompactCategoryCardhHorizontalPreview() {
    MyApplicationTheme {
        val sampleItem = CategoryEntity(
            name = "Peluquería",
            icon = "✂️",
            superCategory = "Cuidado Personal y Moda",
            isNew = true,
            isNewPrestador = true,
            isAd = true
        )
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            CompactCategoryCardHorizontal(
                item = sampleItem,
                onClick = {}
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun CompactCategoryCardPreview() {
    val sampleItem = CategoryEntity(
        name = "Electricidad",
        icon = "⚡",
        superCategory = "Hogar",
        isNew = true,
        isNewPrestador = true,
        isAd = true
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).width(160.dp)) {
            CompactCategoryCard(
                item = sampleItem,
                onClick = {}
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BentoSuperCategoryCardPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Limpieza",
            icon = "🧹",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Plomería",
            icon = "🪠",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Electricidad",
            icon = "⚡",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Carpintería",
            icon = "🪚",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        )
    )
    val sampleSuperCat = SuperCategory(
        title = "Hogar y Construcción",
        icon = "🏠",
        items = sampleCategories,
        isFavorite = true,
        hasFavoriteCategories = true
    )
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp)
        ) {
            BentoSuperCategoryCard(
                superCategory = sampleSuperCat,
                emoji = "🏠",
                height = 130.dp,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandedBentoSuperCategoryCardPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Limpieza",
            icon = "🧹",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = true,
            isNewPrestador = true,
            isAd = true
        ),
        CategoryEntity(
            name = "Plomería",
            icon = "🪠",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Electricidad",
            icon = "⚡",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        )
    )
    val sampleSuperCat = SuperCategory(
        title = "Hogar y Construcción",
        icon = "🏠",
        items = sampleCategories,
        isFavorite = true,
        hasFavoriteCategories = true
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ExpandedBentoSuperCategoryCard(
                superCategory = sampleSuperCat,
                onCategoryClick = {}
            )
        }
    }
}










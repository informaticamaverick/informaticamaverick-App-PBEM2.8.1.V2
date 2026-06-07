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
import androidx.compose.ui.graphics.drawscope.DrawScope
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
    onManageShortcut: (Boolean, String?, String?) -> Unit = { _, _, _ -> },
    showSuperCategoryLabel: Boolean = false,
    isSuperCategoryFavorite: Boolean = false
) {
    val baseColor = Color(CategoryVisuals.getColorFor(item.superCategory))
    val haptic = LocalHapticFeedback.current
    
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp)
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
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CategoryCardBackground(baseColor)
                CategoryCardEmoji(item.icon)
                CategoryCardBadges(item, showSuperCategoryLabel, isSuperCategoryFavorite)
                CategoryCardInfoButton(item)
                CategoryCardFooter(item.name)
            }
        }

        MenuTacticoBe(
            isVisible = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            onAction = {
                onManageShortcut(!isShortcut, item.name, item.icon)
                onToggleFavorite()
                showContextMenu = false
            },
            touchOffset = touchOffset,
            emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
            actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
            actionIconEmoji = "📌"
        )
    }
}

@Composable
private fun CategoryCardBackground(baseColor: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(baseColor, Color(0xFF080A0F)), startY = 100f, endY = 550f)))
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.73f).blur(5.dp).background(baseColor.copy(alpha = 0.9f)))
    }
}

@Composable
private fun BoxScope.CategoryCardEmoji(icon: String) {
    Box(modifier = Modifier.fillMaxSize().offset(y = (-10).dp), contentAlignment = Alignment.Center) {
        Text(text = icon, fontSize = 100.sp, modifier = Modifier.offset(y = 6.dp).graphicsLayer { alpha = 0.8f; colorFilter = ColorFilter.tint(Color.Black) }.blur(3.dp))
        Text(text = icon, fontSize = 100.sp)
    }
}

@Composable
private fun BoxScope.CategoryCardBadges(item: CategoryEntity, showSuperCategoryLabel: Boolean, isSuperCategoryFavorite: Boolean) {
    if (showSuperCategoryLabel) {
        SuperCategoryLabel(item, isSuperCategoryFavorite)
    } else {
        if (item.isNew || item.isNewPrestador || item.isAd) NotificationBadge(Modifier.align(Alignment.TopStart))
        if (item.isFavorite) FavoritePinBadge(isFavorite = true, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp))
    }
}

@Composable
private fun SuperCategoryLabel(item: CategoryEntity, isFavorite: Boolean) {
    Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(bottomEnd = 8.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))) {
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = item.superCategoryIcon, fontSize = 14.sp)
            Box(modifier = Modifier.height(20.dp).width(1.dp).background(Color.White.copy(alpha = 0.3f)))
            Column(verticalArrangement = Arrangement.spacedBy((-14).dp)) {
                Text(text = "SE ENCUENTRA EN", color = Color.White.copy(alpha = 0.5f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = item.superCategory.uppercase(), color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    if (isFavorite) Text(text = "📌", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun NotificationBadge(modifier: Modifier) {
    Box(modifier = modifier.padding(2.dp)) {
        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = Color(0xFFFF9800).copy(alpha = 0.9f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)), shadowElevation = 4.dp) {
            Box(contentAlignment = Alignment.Center) { Text(text = "🔔", fontSize = 14.sp) }
        }
    }
}

@Composable
private fun BoxScope.CategoryCardInfoButton(item: CategoryEntity) {
    var showInfoMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 1.dp, y = (-40).dp)) {
        Surface(onClick = { showInfoMenu = !showInfoMenu }, modifier = Modifier.size(28.dp), shape = CircleShape, color = Color(0xFF1A1C1E), shadowElevation = 4.dp, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))) {
            Box(contentAlignment = Alignment.Center) { Text(text = "ℹ️", fontSize = 14.sp) }
        }
        DropdownMenu(expanded = showInfoMenu, onDismissRequest = { showInfoMenu = false }, modifier = Modifier.width(220.dp).background(Color(0xFF1A1C1E)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))) {
            Box(modifier = Modifier.padding(12.dp)) {
                Text(text = item.description.ifEmpty { "Explora los servicios disponibles en ${item.name}." }, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun BoxScope.CategoryCardFooter(name: String) {
    Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(53.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        DepthDividerHorizontal(shadowColor = Color.Black.copy(alpha = 0.5f), highlightColor = Color.White.copy(alpha = 0.25f))
        Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
            AutoSizeText(text = name.uppercase(), modifier = Modifier.fillMaxWidth(), color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp), textAlign = TextAlign.Center, maxLines = 3)
        }
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
    Box(modifier = Modifier.size(width = 85.dp, height = 110.dp).combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) {
        Card(shape = RoundedCornerShape(6.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), border = BorderStroke(1.dp, if (isSelected) Color(0xFF22D3EE) else Color.White.copy(alpha = 0.15f)), elevation = CardDefaults.cardElevation(if (isSelected) 8.dp else 2.dp), modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(if (isSelected) baseColor else baseColor.copy(alpha = 0.6f), Color(0xFF080A0F)), startY = 0f, endY = 300f)))
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f).blur(4.dp).background(baseColor.copy(alpha = if (isSelected) 0.8f else 0.4f)))
                Text(text = item.icon, fontSize = 44.sp, modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp).graphicsLayer { alpha = 0.8f; colorFilter = ColorFilter.tint(Color.Black) }.blur(1.5.dp))
                Text(text = item.icon, fontSize = 44.sp, modifier = Modifier.align(Alignment.Center).offset(y = (-12).dp))
                Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(color = Color.White.copy(alpha = if (isSelected) 0.6f else 0.3f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 1.dp))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                        AutoSizeText(text = item.name.uppercase(), modifier = Modifier.fillMaxWidth(), color = if (isSelected) Color.White else Color.Gray, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp), textAlign = TextAlign.Center, maxLines = 2)
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// ------------------- TARJETA ESTILO BENTO PARA SUPERCATEGORÍAS --------------------------------
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
    onManageShortcut: (Boolean, String?, String?) -> Unit = { _, _, _ -> }
) {
    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(modifier = Modifier.fillMaxWidth().height(height).padding(top = 8.dp, start = 2.dp, end = 2.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize()
                .drawBehind { drawBentoCardShadow(isPressed) }
                .graphicsLayer { val scale = if (isPressed) 0.98f else 1f; scaleX = scale; scaleY = scale }
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { offset -> touchOffset = offset; showContextMenu = true; haptic.performHapticFeedback(HapticFeedbackType.LongPress) }, onTap = { onClick() })
                },
            shape = RoundedCornerShape(8.dp), border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)), color = Color(0xFF1A1C1E)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BentoCardBackground()
                Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BentoCardHeader(emoji, superCategory)
                    Spacer(modifier = Modifier.height(4.dp))
                    DepthDividerHorizontal(shadowColor = Color.Black.copy(alpha = 0.5f), highlightColor = Color.White.copy(alpha = 0.05f))
                    BentoCardFooter(superCategory.title)
                }
                MenuTacticoBe(
                    isVisible = showContextMenu, onDismissRequest = { showContextMenu = false },
                    onAction = { onManageShortcut(!isShortcut, superCategory.title, superCategory.icon); onToggleFavorite(); showContextMenu = false },
                    touchOffset = touchOffset, emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
                    actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO", actionIconEmoji = "📌"
                )
            }
        }
        FavoritePinBadge(isFavorite = superCategory.isFavorite, isIndirect = superCategory.hasFavoriteCategories, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = (4).dp))
    }
}

@Composable
private fun BentoCardBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))
        Box(modifier = Modifier.fillMaxSize().blur(radius = 16.dp).alpha(0.15f).background(Color.White.copy(alpha = 0.05f)))
    }
}

@Composable
private fun BentoCardHeader(emoji: String, superCategory: SuperCategory) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = 48.sp, modifier = Modifier.offset(x = 3.dp, y = 3.dp).graphicsLayer { alpha = 0.9f; colorFilter = ColorFilter.tint(Color.Black) }.blur(3.dp))
            Text(text = emoji, fontSize = 48.sp)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
            Surface(color = Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.35f))) {
                val countToShow = if (superCategory.totalItems > 0) superCategory.totalItems else superCategory.items.size
                Text(text = countToShow.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                Text(text = "SERVICIOS", color = Color.Gray.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp))
                Text(text = "PROFESIONES", color = Color.Gray.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp))
            }
        }
    }
}

@Composable
private fun ColumnScope.BentoCardFooter(title: String) {
    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        AutoSizeText(text = title, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 16.sp), textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.fillMaxWidth())
    }
}

private fun DrawScope.drawBentoCardShadow(isPressed: Boolean) {
    val shadowColor = Color.Black
    val shadowRadius = if (isPressed) 12.dp.toPx() else 8.dp.toPx()
    val offsetY = if (isPressed) 6.dp.toPx() else 4.dp.toPx()
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply { color = shadowColor.toArgb(); setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb()) }
        canvas.nativeCanvas.drawRoundRect(0f, offsetY, size.width, size.height, 8.dp.toPx(), 8.dp.toPx(), paint)
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
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).shadow(12.dp, RoundedCornerShape(6.dp)), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.85f)))))
            Column(modifier = Modifier.padding(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = superCategory.icon, fontSize = 28.sp)
                        if (superCategory.isFavorite || superCategory.hasFavoriteCategories) Spacer(modifier = Modifier.width(8.dp))
                        AutoSizeText(text = superCategory.title, color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), maxLines = 2)
                    }
                    FavoritePinBadge(isFavorite = superCategory.isFavorite, isIndirect = superCategory.hasFavoriteCategories)
                }
                Spacer(modifier = Modifier.height(8.dp)); HorizontalDivider(color = Color.White.copy(alpha = 0.2f)); Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = superCategory.items, key = { it.name }) { category ->
                        Box(modifier = Modifier.width(125.dp)) {
                            CompactCategoryCard(item = category, onClick = { onCategoryClick(category) }, onToggleFavorite = { onToggleCategoryFavorite(category) }, onManageShortcut = { _, _, _ -> })
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompactCategoryCardPreview() {
    val sampleItem = CategoryEntity(name = "Electricidad", icon = "⚡", superCategory = "Hogar", isNew = true, isNewPrestador = true, isAd = true)
    MyApplicationTheme { Box(modifier = Modifier.padding(16.dp).width(160.dp)) { CompactCategoryCard(item = sampleItem, onClick = {}, onManageShortcut = { _, _, _ -> }) } }
}

@Preview(showBackground = true)
@Composable
fun CompactCategoryCardWithSearchLabelPreview() {
    val sampleItem = CategoryEntity(name = "Plomería", icon = "🪠", superCategory = "Hogar y Mantenimiento", superCategoryIcon = "🏠", isFavorite = false)
    MyApplicationTheme { Box(modifier = Modifier.padding(16.dp).width(180.dp)) { CompactCategoryCard(item = sampleItem, onClick = {}, onManageShortcut = { _, _, _ -> }, showSuperCategoryLabel = true, isSuperCategoryFavorite = true) } }
}

@Preview(showBackground = true)
@Composable
fun BentoSuperCategoryCardPreview() {
    val sampleSuperCat = SuperCategory(title = "Hogar y Construcción", icon = "🏠", isFavorite = true, hasFavoriteCategories = true)
    MyApplicationTheme { Box(modifier = Modifier.padding(16.dp).width(300.dp)) { BentoSuperCategoryCard(superCategory = sampleSuperCat, emoji = "🏠", height = 130.dp, onClick = {}, onManageShortcut = { _, _, _ -> }) } }
}

@Preview(showBackground = true)
@Composable
fun ExpandedBentoSuperCategoryCardPreview() {
    val sampleSuperCat = SuperCategory(title = "Hogar y Construcción", icon = "🏠", items = emptyList(), isFavorite = true, hasFavoriteCategories = true)
    MyApplicationTheme { Box(modifier = Modifier.padding(16.dp)) { ExpandedBentoSuperCategoryCard(superCategory = sampleSuperCat, onCategoryClick = {}) } }
}

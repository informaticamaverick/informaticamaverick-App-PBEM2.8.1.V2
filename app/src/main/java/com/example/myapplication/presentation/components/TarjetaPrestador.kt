package com.example.myapplication.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.model.BadgeDisplayData
import com.example.myapplication.data.model.ProviderType
import com.example.myapplication.data.model.ProviderDisplayModel

import com.example.myapplication.presentation.designsystem.components.BentoActionButton
import com.example.myapplication.presentation.designsystem.components.PremiumDividerV3
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

/**
 * Clase de utilidad para representar los datos de un Badge con estado activo/inactivo.
 * [PLAN DE ACCIÓN]: Mantenida para compatibilidad interna de componentes UI.
 */
data class BadgeItem(
    val id: String,
    val icon: String,          
    val inactiveIcon: ImageVector, 
    val label: String,         
    val isActive: Boolean      
)

// ==========================================================================================
// ---------- SECCIÓN: HELPER DE ICONOS INACTIVOS (PLAN DE ACCIÓN) --------------------------
// ==========================================================================================

/**
 * Mapeo estático de iconos para evitar instanciación o lógica pesada en el scroll.
 */
private fun getInactiveIconForId(id: String): ImageVector {
    return when (id) {
        "24h" -> Icons.Default.AccessTimeFilled
        "loc" -> Icons.Default.Storefront
        "visit", "env" -> Icons.Default.LocalShipping
        "date" -> Icons.Default.EventAvailable
        "serv" -> Icons.Default.Build
        "prod" -> Icons.Default.ShoppingBag
        else -> Icons.Default.Build
    }
}

// ==========================================================================================
// ---------- SECCIÓN: COMPONENTES AUXILIARES V3 ------------------------------------------
// ==========================================================================================

/**
 * Badge Individual para V3 con lógica de tachado (si es false).
 * [CORRECCIÓN CRÍTICA LAG]: Se eliminó el DropdownMenu interno. 
 * Ahora es un componente ligero puramente visual.
 */
@Composable
fun BadgeIconV3(
    item: BadgeDisplayData,
    badgeSize: Dp = 26.dp,
    emojiSize: TextUnit = 14.sp
) {
    // El icono inactivo se obtiene de forma directa (sin lógica de negocio)
    val inactiveIcon = remember(item.id) { getInactiveIconForId(item.id) }

    Box(
        modifier = Modifier
            .size(badgeSize)
            .background(Color.White.copy(0.05f), CircleShape)
            .border(
                0.5.dp,
                if (item.isActive) Color.White.copy(0.3f) else Color.White.copy(0.1f),
                CircleShape
            )
            .drawWithContent {
                drawContent()
                if (!item.isActive) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.6f),
                        start = Offset(x = size.width * 0.2f, y = size.height * 0.2f),
                        end = Offset(x = size.width * 0.8f, y = size.height * 0.8f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (item.isActive) {
            Text(text = item.icon, fontSize = emojiSize)
        } else {
            Icon(
                imageVector = inactiveIcon,
                contentDescription = null,
                tint = Color.Gray.copy(0.4f),
                modifier = Modifier.size(badgeSize * 0.5f) 
            )
        }
    }
}

// ==========================================================================================
// ---------- TARJETA PRESTADOR V3 (PREMIUM COMPACT) - [UNIFICADA] --------------------------
// ==========================================================================================

/**
 * PRESTADOR CARD V3: La nueva carcaza premium unificada con estilo Material 3.
 * [OPTIMIZADA]: Cumple con el Plan de Acción de "La pantalla solo dibuja".
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrestadorCardV3(
    provider: ProviderDisplayModel,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    // --- VARIABLES DE TAMAÑO DE LETRA ---
    titleFontSize: TextUnit = 12.sp,
    addressFontSize: TextUnit = 9.sp,
    branchFontSize: TextUnit = 11.sp,
    rankingFontSize: TextUnit = 10.sp,
    buttonFontSize: TextUnit = 10.sp,
    
    // --- VARIABLES DE TAMAÑO DE EMOJI/ICONOS ---
    statusEmojiSize: TextUnit = 12.sp,
    badgeEmojiSize: TextUnit = 14.sp,
    buttonEmojiSize: TextUnit = 12.sp,
    badgeContainerSize: Dp = 26.dp,
    avatarSize: Dp = 46.dp,
    
    // --- VARIABLES DE ESPACIOS Y DIMENSIONES ---
    cardWidth: Dp = 135.dp,
    mainPadding: Dp = 6.dp,
    verticalPaddingBetweenSections: Dp = 6.dp,
    buttonHeight: Dp = 30.dp,
    horizontalSpacingAvatarInfo: Dp = 8.dp
) {
    // --- SECCIÓN: ANIMACIÓN DE ALTURA ---
    // [OPTIMIZACIÓN]: Se usa un valor estático si no es necesario animar para reducir carga en scroll
    // [MODIFICACIÓN]: Se aumentó el tamaño de 56.dp a 74.dp para evitar que el avatar se encoja en modo compacto
    val animatedHeight by animateDpAsState(
        targetValue = if (isCompact) 74.dp else 235.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CardHeightAnimation"
    )

    // --- SECCIÓN: PALETA DE COLORES Y CONFIGURACIÓN ---
    val maverickBlue = Color(0xFF22D3EE)
    val haptic = LocalHapticFeedback.current

    val maverickPurple = Color(0xFF9B51E0)
    val darkCardBg = Color(0xFF1A1F26)
    val darkBottomBg = Color(0xFF0A0E14)
    val backgroundBrush = Brush.verticalGradient(listOf(darkCardBg, darkBottomBg))

    Surface(
        modifier = modifier
            .width(cardWidth) 
            .height(animatedHeight)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.background(backgroundBrush)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(mainPadding)
            ) {
                // --- SECCIÓN: CABECERA (Avatar M3 + Nombre + Ranking) ---
                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(avatarSize)) {
                        // [OPTIMIZACIÓN COIL]: Se añade control de tamaño para evitar lag en scroll
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(provider.photoUrl)
                                .crossfade(true)
                                .size(150, 150) // Limita el tamaño de decodificación
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (provider.isOnline) {
                            Box(modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset((-3).dp, (-3).dp)
                                .size(12.dp)
                                .background(Color(0xFF34D399), CircleShape)
                                .border(1.5.dp, darkCardBg, CircleShape))
                        }
                        if (provider.isSubscribed) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = maverickBlue, 
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(3.dp, 3.dp)
                                    .size(18.dp)
                                    .background(darkCardBg, CircleShape)
                                    .padding(1.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(horizontalSpacingAvatarInfo))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = provider.title, 
                            color = Color.White,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp
                        )

                        // --- Fila de Estado: [LEER DEL MODELO] Pre-calculado ---
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Text(text = provider.typeEmoji, fontSize = statusEmojiSize)

                            Spacer(Modifier.width(16.dp))

                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                            Text(
                                text = " ${"%.1f".format(provider.rating)}", 
                                color = Color.White,
                                fontSize = rankingFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!isCompact) {
                    PremiumDividerV3(maverickPurple, verticalPadding = verticalPaddingBetweenSections)

                    // --- SECCIÓN: INFO (Sucursal/Ubicación + Dirección) ---
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 1.dp)) {
                        Text(
                            text = provider.branchName ?: "Dirección", 
                            color = maverickBlue,
                            fontSize = branchFontSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                           modifier = Modifier.padding(top = 0.dp),
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = provider.displayAddress ?: "", 
                            color = Color.Gray,
                            fontSize = addressFontSize,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    PremiumDividerV3(maverickPurple, verticalPadding = verticalPaddingBetweenSections)

                    // --- SECCIÓN: FOOTER (Badges Expandidos + Botón Chat Bento) ---
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // --- Grid de Badges: [LEER DEL MODELO] Pre-calculado ---
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            provider.badgeList.chunked(4).forEach { rowBadges ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowBadges.forEach { badgeData -> 
                                        BadgeIconV3(badgeData, badgeSize = badgeContainerSize, emojiSize = badgeEmojiSize) 
                                    }
                                }
                            }
                        }

                        // --- Botón de Mensaje: BentoActionButton Premium ---
                        BentoActionButton(
                            text = "MENSAJE",
                            emoji = "💬",
                            color = maverickBlue,
                            onClick = onChatClick,
                            fontSize = buttonFontSize,
                            emojiSize = buttonEmojiSize,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(buttonHeight)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// ---------- PREVIEWS ACTUALIZADOS ---------------------------------------------------------
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorCardV3Preview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrestadorCardV3(
                provider = ProviderDisplayModel(
                    id = "1",
                    title = "Maverick Tech S.A.",
                    subtitle = "Software & Hardware",
                    photoUrl = "https://picsum.photos/seed/maverick/200/200",
                    rating = 4.95,
                    isVerified = true,
                    isOnline = true,
                    type = ProviderType.COMPANY,
                    typeEmoji = "🏢",
                    typeLabel = "Empresa Certificada",
                    badgeList = listOf(
                        BadgeDisplayData("24h", "🕒", "Atención 24hs", true),
                        BadgeDisplayData("loc", "🏪", "Local Físico", true),
                        BadgeDisplayData("visit", "🚚", "Visitas a Domicilio", true)
                    ),
                    isSubscribed = true,
                    branchName = "Casa Central",
                    displayAddress = "Av. Aconquija 2000, Yerba Buena"
                ),
                onClick = {},
                onChatClick = {},

            )
        }
    }
}











package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.data.model.BadgeDisplayData
import com.example.myapplication.data.model.ProviderType
import com.example.myapplication.data.model.ProviderDisplayModel

import com.example.myapplication.presentation.designsystem.components.BentoActionButton
import com.example.myapplication.presentation.designsystem.components.PremiumDividerV3
import com.example.myapplication.presentation.designsystem.components.MenuTacticoBe
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.asCompact
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.core.data.local.entity.UserEntity

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
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {},
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
    val animatedHeight by animateDpAsState(
        targetValue = if (isCompact) 74.dp else 235.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CardHeightAnimation"
    )

    // --- SECCIÓN: PALETA DE COLORES Y CONFIGURACIÓN ---
    val maverickBlue = Color(0xFF22D3EE)
    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

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
                    onLongPress = { offset ->
                        touchOffset = offset
                        showContextMenu = true
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
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(provider.photoUrl)
                                .crossfade(true)
                                .size(150, 150)
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp
                        )

                        // [ELITE] Tag de Identidad (Empresa vs Sucursal vs Personal)
                        val identityLabel = when {
                            provider.branchId != null -> "📍 Sucursal: ${provider.branchName}"
                            provider.companyId != null -> "🏢 Empresa"
                            else -> "👨‍🔧 Profesional"
                        }
                        Text(
                            text = identityLabel,
                            color = maverickBlue,
                            fontSize = (addressFontSize.value + 1).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
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

                    Column(modifier = Modifier.fillMaxWidth().padding(top = 1.dp)) {
                        Text(
                            text = provider.displayAddress ?: "Ubicación",
                            color = Color.Gray,
                            fontSize = addressFontSize,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    PremiumDividerV3(maverickPurple, verticalPadding = verticalPaddingBetweenSections)

                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
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

            // --- MENU TACTICO BE (AL ESTILO SUPERCATEGORIA) ---
            MenuTacticoBe(
                isVisible = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                onAction = {
                    onManageShortcut(!isShortcut)
                    showContextMenu = false
                },
                touchOffset = touchOffset,
                emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
                actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
                actionIconEmoji = "📌"
            )
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
                isShortcut = false,
                onManageShortcut = {}
            )
        }
    }
}

// ==========================================================================================
// ---------- SECCIÓN: COLORES ESTILO TAILWIND ----------------------------------------------
// ==========================================================================================
private val Slate50 = Color(0xFFF8FAFC)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate400 = Color(0xFF94A3B8)
private val Slate500 = Color(0xFF64748B)
private val Slate600 = Color(0xFF475569)
private val Slate800 = Color(0xFF1E293B)
private val Blue50 = Color(0xFFEFF6FF)
private val Blue100 = Color(0xFFDBEAFE)
private val Blue500 = Color(0xFF3B82F6)
private val Blue600 = Color(0xFF2563EB)
private val Yellow400 = Color(0xFFFACC15)
private val Green500 = Color(0xFF22C55E)

// ==========================================================================================
// ---------- MODELOS DE DATOS (DETALLE PRESTADOR) ------------------------------------------
// ==========================================================================================
data class ServiceItem(val name: String, val price: String, val time: String)
data class ProviderStats(val jobs: Int, val rating: Double, val responseTime: String)
data class ProviderDetail(
    val name: String,
    val role: String,
    val location: String,
    val isVerified: Boolean,
    val avatar: String,
    val cover: String,
    val stats: ProviderStats,
    val about: String,
    val skills: List<String>,
    val services: List<ServiceItem>
)

// ==========================================================================================
// ---------- COMPONENTE: SERVICE CARD SCREEN (DETALLE PREMIUM) -----------------------------
// ==========================================================================================

/**
 * Pantalla de detalle del prestador con estilo "Elena Rodriguez".
 * Mejora: Utiliza Glassmorphism y animaciones de entrada.
 */
@Composable
fun ServiceCardScreen() {
    // Datos simulados (Elena Rodríguez Style)
    val provider = remember {
        ProviderDetail(
            name = "Elena Rodríguez",
            role = "Especialista en Diseño de Interiores",
            location = "Madrid, Centro",
            isVerified = true,
            avatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=256&auto=format&fit=crop",
            cover = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?q=80&w=800&auto=format&fit=crop",
            stats = ProviderStats(142, 4.9, "< 1h"),
            about = "Transformo espacios vacíos en hogares llenos de vida. Con más de 5 años de experiencia, me especializo en diseño minimalista y sostenible, adaptándome a tu presupuesto.",
            skills = listOf("Minimalismo", "Render 3D", "Sostenibilidad", "Gestión de Obra"),
            services = listOf(
                ServiceItem("Asesoría Online", "Desde 50€", "1 hora"),
                ServiceItem("Proyecto Completo", "Personalizado", "2-4 semanas"),
                ServiceItem("Home Staging", "Desde 300€", "1 semana")
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Cabecera: Portada y Avatar
                HeaderSection(provider)

                // 2. Contenido Principal
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    
                    Text(
                        text = provider.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = provider.role,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue600,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = Slate500, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = provider.location, fontSize = 14.sp, color = Slate500)
                    }

                    // 3. Stats Gamificados
                    StatsSection(provider.stats)

                    // 4. Botones de Acción
                    ActionButtonsSection()

                    // 5. Tabs de Información
                    var activeTab by remember { mutableIntStateOf(0) }
                    TabSection(activeTab) { activeTab = it }

                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = activeTab == 0,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                    ) {
                        ProfileTabContent(provider)
                    }
                    
                    AnimatedVisibility(
                        visible = activeTab == 1,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 50 })
                    ) {
                        ServicesTabContent(provider)
                    }

                    // 6. Footer Verificación
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Green500, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Identidad verificada • Plataforma Segura", 
                            fontSize = 11.sp, 
                            color = Slate400, 
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(provider: ProviderDetail) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        // Portada
        Box(modifier = Modifier.fillMaxWidth().height(144.dp)) {
            AsyncImage(
                model = provider.cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
            
            IconButton(
                onClick = { },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Avatar Flotante
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
        ) {
            AsyncImage(
                model = provider.avatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            )
            if (provider.isVerified) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(28.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun StatsSection(stats: ProviderStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .background(Slate50, RoundedCornerShape(16.dp))
            .border(1.dp, Slate100, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItemDetail(icon = Icons.Default.Build, iconTint = Slate400, value = stats.jobs.toString(), label = "Trabajos")
        VerticalDivider(modifier = Modifier.height(30.dp), color = Slate200.copy(0.5f))
        StatItemDetail(icon = Icons.Default.Star, iconTint = Yellow400, value = stats.rating.toString(), label = "Rating")
        VerticalDivider(modifier = Modifier.height(30.dp), color = Slate200.copy(0.5f))
        StatItemDetail(icon = Icons.Default.AccessTime, iconTint = Slate400, value = stats.responseTime, label = "Respuesta")
    }
}

@Composable
private fun StatItemDetail(icon: ImageVector, iconTint: Color, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Slate800)
        Text(text = label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun ActionButtonsSection() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { },
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Contactar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        
        Button(
            onClick = { },
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Slate100, contentColor = Slate800),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Agendar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TabSection(activeTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate100, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        val tabs = listOf("Perfil", "Servicios")
        tabs.forEachIndexed { index, title ->
            val isSelected = activeTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) Slate800 else Slate500
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileTabContent(provider: ProviderDetail) {
    Column {
        Text(
            text = provider.about,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = Slate600
        )
        
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = "ESPECIALIDADES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Slate400,
            letterSpacing = 1.sp
        )
        
        Spacer(Modifier.height(10.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            provider.skills.forEach { skill ->
                Surface(
                    color = Blue50,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Blue100)
                ) {
                    Text(
                        text = skill,
                        color = Blue600,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServicesTabContent(provider: ProviderDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        provider.services.forEach { service ->
            Surface(
                onClick = { },
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate100)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = service.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        Text(text = service.time, fontSize = 12.sp, color = Slate500)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = service.price, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Blue600)
                        Icon(Icons.Default.ChevronRight, null, tint = Slate400, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceCardPreview() {
    MyApplicationTheme {
        ServiceCardScreen()
    }
}

// ==========================================================================================
// ---------- PRESTADOR CARD V4 (PREMIUM LIGHT - ELENA STYLE) -----------------------------
// ==========================================================================================

/**
 * PRESTADOR CARD V4: La evolución definitiva que une la lógica de V3 con el estilo Elena.
 * DISEÑO: Light-theme, Slate/Blue palette, tipografía premium y layout Bento.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrestadorCardV4(
    provider: ProviderDisplayModel,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {},
    isCompact: Boolean = false,
    
    // --- VARIABLES DE TAMAÑO ---
    cardWidth: Dp = 155.dp, // Un poco más ancha para el estilo premium
    avatarSize: Dp = 58.dp,
    badgeSize: Dp = 24.dp
) {
    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    
    // Animación de altura similar a V3 pero con valores ajustados al nuevo diseño
    val animatedHeight by animateDpAsState(
        targetValue = if (isCompact) 85.dp else 275.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CardHeightV4Animation"
    )

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(animatedHeight)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Slate100)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // --- CABECERA: Avatar con Glow y Online Status ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(provider.photoUrl)
                                .crossfade(true)
                                .size(150, 150)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                                .border(2.dp, Blue50, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Indicador Online (Elena Style)
                        if (provider.isOnline) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(14.dp)
                                    .background(Green500, CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    if (!isCompact) {
                        Spacer(Modifier.width(10.dp))
                        Column {
                            // Rating Pill Compacto
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Blue50)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, tint = Yellow400, modifier = Modifier.size(10.dp))
                                Text(
                                    text = "%.1f".format(provider.rating),
                                    color = Blue600,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- TÍTULOS Y SUBTÍTULOS ---
                Text(
                    text = provider.title,
                    color = Slate800,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (isCompact) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                
                Text(
                    text = provider.typeLabel,
                    color = Blue600,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                if (!isCompact) {
                    Spacer(Modifier.height(10.dp))
                    
                    // --- SECCIÓN BENTO: Ubicación y Badges ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Ubicación traslúcida
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate50)
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Slate400, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = provider.displayAddress ?: "Ubicación",
                                    color = Slate600,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Grid de Badges Estilo Elena
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            provider.badgeList.take(4).forEach { badge ->
                                Box(
                                    modifier = Modifier
                                        .size(badgeSize)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (badge.isActive) Blue50 else Slate50)
                                        .border(0.5.dp, if (badge.isActive) Blue100 else Slate200, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (badge.isActive) badge.icon else "•",
                                        fontSize = 12.sp,
                                        color = if (badge.isActive) Blue600 else Slate400
                                    )
                                }
                            }
                        }
                    }

                    // --- BOTÓN DE ACCIÓN PREMIUM ---
                    Button(
                        onClick = onChatClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("MENSAJE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // --- MENU TACTICO BE ---
            MenuTacticoBe(
                isVisible = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                onAction = {
                    onManageShortcut(!isShortcut)
                    showContextMenu = false
                },
                touchOffset = touchOffset,
                emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
                actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
                actionIconEmoji = "📌"
            )
        }
    }
}

// ==========================================================================================
// ---------- PREVIEW V4 --------------------------------------------------------------------
// ==========================================================================================

// ==========================================================================================
// ---------- PRESTADOR BUSINESS CARD (V5 - PROFESSIONAL) -----------------------------------
// ==========================================================================================

    /**
 * PRESTADOR BUSINESS CARD V5: Una tarjeta de presentación profesional (Business Card).
 * DISEÑO: Layout horizontal, estilo "Maverick Dark Premium".
 * OPTIMIZACIÓN: Ultra-compacta, conectada al SSOT y funcional.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrestadorBusinessCard(
    provider: ProviderDisplayModel,
    user: UserEntity?, // 🔥 [ELITE] Necesario para selección de remitente
    onClick: () -> Unit,
    onChatClick: (SenderProfile?) -> Unit, // 🔥 [ELITE] Ahora pasa el remitente seleccionado
    modifier: Modifier = Modifier,
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {}
) {
    val maverickBlue = Color(0xFF22D3EE)
    val maverickPurple = Color(0xFF9B51E0)
    val darkCardBg = Color(0xFF1A1F26)

    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    // --- NUEVO: GESTIÓN DE REMITENTE INTELIGENTE ---
    var showSenderSelector by remember { mutableStateOf(false) }
    val senderProfiles = remember(user) { getAvailableSenderProfiles(user) }

    fun handleChatAction() {
        if (senderProfiles.size > 1) {
            showSenderSelector = true
        } else {
            onChatClick(senderProfiles.firstOrNull())
        }
    }

    ElevatedCard(
        modifier = modifier
            .width(170.dp) 
            .height(110.dp) // Altura ajustada
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
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp), 
        colors = CardDefaults.elevatedCardColors(
            containerColor = darkCardBg,
            contentColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(listOf(maverickBlue.copy(0.1f), Color.Transparent)),
                    radius = size.maxDimension * 0.8f,
                    center = Offset(size.width * 0.9f, size.height * 0.1f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp) 
            ) {
                // --- SECCIÓN SUPERIOR: AVATAR + INFO ---
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top 
                ) {
                    // --- IZQUIERDA: AVATAR ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        Brush.sweepGradient(listOf(maverickBlue, maverickPurple, maverickBlue)),
                                        CircleShape
                                    )
                                    .padding(1.5.dp)
                                    .background(darkCardBg, CircleShape)
                            )
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(provider.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.size(46.dp).clip(CircleShape).clickable { onClick() },
                                contentScale = ContentScale.Crop
                            )

                            // [ELITE] Badge de "+X sucursales" (Ley Pareja)
                            if (provider.nearbyBranchesCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp)
                                        .background(Color(0xFF22D3EE), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+${provider.nearbyBranchesCount}",
                                        color = Color.Black,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            
                            // --- TILDE DE VERIFICACIÓN (SSOT) ---
                            if (provider.isVerified) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Verificado",
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .size(16.dp)
                                        .background(darkCardBg, CircleShape)
                                        .padding(1.dp)
                                )
                            }
                        }
                        
                        // Rating Mini
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(8.dp))
                            Text(
                                text = " ${"%.1f".format(provider.rating)}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = TextStyle().asCompact()
                            )
                        }
                    }

                    // --- DERECHA: INFO Y DIRECCIÓN (CON AUTO-AJUSTE DINÁMICO) ---
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp) // Espaciado mínimo entre líneas
                    ) {
                        AutoSizeText(
                            text = provider.title,
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ).asCompact()
                        )
                        
                        Text(
                            text = provider.typeLabel.uppercase(),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = maverickBlue,
                            letterSpacing = 0.5.sp,
                            style = TextStyle().asCompact()
                        )
                        Spacer(Modifier.height(2.dp))

                        HorizontalDivider(
                            thickness = 0.5.dp, 
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(2.dp))
                        
                        Text(
                            text = "DIRECCIÓN",
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            color = maverickBlue.copy(0.8f),
                            style = TextStyle().asCompact()
                        )
                        
                        AutoSizeText(
                            text = provider.branchName ?: "CASA CENTRAL",
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ).asCompact()
                        )

                        AutoSizeText(
                            text = provider.displayAddress ?: "Ubicación no disponible",
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            ).asCompact()
                        )
                    }
                }

                // --- SOCALO MINI CON MENÚ DE INFO ---
                HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.08f))
                Spacer(Modifier.height(3.dp))

                var showBadgesMenu by remember { mutableStateOf(false) }

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- IZQUIERDA: Fila de Badges (Clickable) ---
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showBadgesMenu = true },
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mostramos todos los badges (activos e inactivos)
                            provider.badgeList.forEach { badge ->
                                BadgeIconBoxV5Mini(
                                    emoji = if (badge.isActive) badge.icon else null,
                                    icon = if (!badge.isActive) getInactiveIconForId(badge.id) else null,
                                    isActive = badge.isActive
                                )
                            }
                        }
                    }

                    // --- MENÚ EMERGENTE DE INFORMACIÓN (MATERIAL 3) ---
                    DropdownMenu(
                        expanded = showBadgesMenu,
                        onDismissRequest = { showBadgesMenu = false },
                        modifier = Modifier
                            .background(darkCardBg)
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = "SERVICIOS Y CAPACIDADES",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = maverickBlue,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        
                        provider.badgeList.forEach { badge ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    if (badge.isActive) maverickBlue.copy(0.1f) else Color.White.copy(0.05f),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (badge.isActive) {
                                                Text(text = badge.icon, fontSize = 16.sp)
                                            } else {
                                                Icon(
                                                    imageVector = getInactiveIconForId(badge.id),
                                                    contentDescription = null,
                                                    tint = Color.Gray.copy(0.4f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = badge.label,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (badge.isActive) "ACTIVO" else "NO DISPONIBLE",
                                                color = if (badge.isActive) Color(0xFF22C55E) else Color.Gray,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                },
                                onClick = { showBadgesMenu = false }
                            )
                        }
                    }
                }
            }

            // --- BOTÓN DE MENSAJE FLOTANTE (ESTILO FAB - M3) ---
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(y = (-23).dp) // Posicionado exactamente sobre el divisor
                    .clickable { handleChatAction() }
                    .shadow(18.dp, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
                color = darkCardBg,
                //shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                //border = BorderStroke(0.5.dp, maverickBlue.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(text = "📨", fontSize = 16.sp, style = TextStyle().asCompact())
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = maverickBlue,
                        modifier = Modifier.size(10.dp)
                    )
                }

                // --- MENÚ DE SELECCIÓN DE REMITENTE ---
                SenderSelectionMenu(
                    expanded = showSenderSelector,
                    onDismissRequest = { showSenderSelector = false },
                    profiles = senderProfiles,
                    onProfileSelected = { sender ->
                        onChatClick(sender)
                        showSenderSelector = false
                    }
                )
            }

            // --- MENU TACTICO BE ---
            MenuTacticoBe(
                isVisible = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                onAction = {
                    onManageShortcut(!isShortcut)
                    showContextMenu = false
                },
                touchOffset = touchOffset,
                emotion = if (isShortcut) BeEmotion.SAD else BeEmotion.HAPPY,
                actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
                actionIconEmoji = "📌"
            )
        }
    }
}

/**
 * Versión Mini del Badge Box para la tarjeta compacta.
 */
@Composable
private fun BadgeIconBoxV5Mini(
    emoji: String? = null,
    icon: ImageVector? = null,
    isActive: Boolean = true
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && emoji != null) {
                Text(
                    text = emoji, 
                    fontSize = 11.sp,
                    style = TextStyle().asCompact()
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}





@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorBusinessCardPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E14))
                .padding(24.dp), 
            contentAlignment = Alignment.Center
        ) {
            PrestadorBusinessCard(
                provider = ProviderDisplayModel(
                    id = "5",
                    title = "Dr. Steve Smith",
                    subtitle = "General Physician",
                    photoUrl = "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=256&auto=format&fit=crop",
                    rating = 4.9,
                    isVerified = true,
                    isOnline = true,
                    type = ProviderType.INDIVIDUAL,
                    typeEmoji = "👨‍⚕️",
                    typeLabel = "Medical Specialist",
                    badgeList = listOf(
                        BadgeDisplayData("serv", "🛠️", "Brinda Servicio", true),
                        BadgeDisplayData("prod", "📦", "Vende Productos", false),
                        BadgeDisplayData("24h", "🕒", "Atención 24hs", true),
                        BadgeDisplayData("loc", "🏪", "Local Físico", true),
                        BadgeDisplayData("visit", "🚚", "Visitas a Domicilio", true),
                        BadgeDisplayData("env", "📦", "Envíos", false),
                        BadgeDisplayData("date", "📅", "Turnos Online", true)
                    ),
                    isSubscribed = true,
                    branchName = "Central Clinic",
                    displayAddress = "77 Your Street Address, NY",
                    statusText = "ABIERTO AHORA ✅"
                ),
                onClick = {},
                user = null,
                onChatClick = { }
                //sten onChatClick = { }
            )
        }
    }
}



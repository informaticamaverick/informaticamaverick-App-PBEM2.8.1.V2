package com.example.myapplication.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.componentes.sistema.menu.SenderProfile
import com.example.myapplication.ui.componentes.sistema.menu.SenderSelectionMenu
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.PerfilPrestadorInsignia
import com.example.myapplication.core.dominio.modelos.TipoPrestador
import com.example.myapplication.ui.componentes.sistema.BentoActionButton
import com.example.myapplication.ui.componentes.sistema.PremiumDividerV3
import com.example.myapplication.ui.componentes.sistema.MenuTacticoBe
import com.example.myapplication.ui.componentes.sistema.AutoSizeText
import com.example.myapplication.uishared.estilos.asCompact
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.ui.componentes.be.modelos.EmocionBe

import com.example.myapplication.core.utilidades.ImageUtils

/**
 * Clase de utilidad para representar los datos de un Badge con estado activo/inactivo.

//data class BadgeItem(
    val id: String,
    val icon: String,
    val inactiveIcon: ImageVector,
    val label: String,
    val isActive: Boolean
)
**/
// ==========================================================================================
// ---------- SECCIÓN: HELPER DE ICONOS INACTIVOS --------------------------
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
// ---------- SECCIÓN: COMPONENTES AUXILIARES V3 ------------------------------------------
// ==========================================================================================

/**
 * Badge Individual para V3 con lógica de tachado (si es false).
 */
@Composable
fun BadgeIconV3(
    item: PerfilPrestadorInsignia,
    badgeSize: Dp = 26.dp,
    emojiSize: TextUnit = 14.sp
) {
    val inactiveIcon = remember(item.id) { getInactiveIconForId(item.id) }

    Box(
        modifier = Modifier
            .size(badgeSize)
            .background(Color.White.copy(0.05f), CircleShape)
            .border(
                0.5.dp,
                if (item.estaActiva) Color.White.copy(0.3f) else Color.White.copy(0.1f),
                CircleShape
            )
            .drawWithContent {
                drawContent()
                if (!item.estaActiva) {
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
        if (item.estaActiva) {
            Text(text = item.icono, fontSize = emojiSize)
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
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrestadorCardV3(
    provider: PrestadorDominio,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {},
    isCompact: Boolean = false,
    // --- VARIABLES DE TAMAÑO DE LETRA ---
    titleFontSize: TextUnit = 12.sp,
    addressFontSize: TextUnit = 9.sp,
   // branchFontSize: TextUnit = 11.sp,
    rankingFontSize: TextUnit = 10.sp,
    buttonFontSize: TextUnit = 10.sp,
    
    // --- VARIABLES DE TAMAÑO DE EMOJI/ICONOS ---
   // statusEmojiSize: TextUnit = 12.sp,
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
    val animatedHeight by animateDpAsState(
        targetValue = if (isCompact) 74.dp else 235.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "CardHeightAnimation"
    )

    val appBlue = Color(0xFF22D3EE)
    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    val appPurple = Color(0xFF9B51E0)
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
                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoldeBurbujaPerfilV3(
                        perfil = PerfilIdentidadV3(
                            id = provider.id,
                            nombre = provider.titulo,
                            iniciales = provider.titulo.take(2).uppercase(),
                            photoUrl = ImageUtils.processImageSource(provider.urlFoto ?: provider.urlMiniatura),
                            estaEnLinea = provider.estaOnline,
                            esSuscripto = provider.estaSuscrito,
                            estaVerificado = provider.estaVerificado
                        ),
                        tamanoBase = avatarSize,
                        mostrarBadges = true
                    )

                    Spacer(Modifier.width(horizontalSpacingAvatarInfo))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = provider.titulo, 
                            color = Color.White,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp
                        )

                        val identityLabel = provider.subtitulo ?: "Profesional"
                        Text(
                            text = identityLabel,
                            color = appBlue,
                            fontSize = (addressFontSize.value + 1).sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                            Text(
                                text = " ${"%.1f".format(provider.reputacion)}", 
                                color = Color.White,
                                fontSize = rankingFontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!isCompact) {
                    PremiumDividerV3(appPurple, verticalPadding = verticalPaddingBetweenSections)

                    Column(modifier = Modifier.fillMaxWidth().padding(top = 1.dp)) {
                        Text(
                            text = provider.direccionVisible ?: "Ubicación",
                            color = Color.Gray,
                            fontSize = addressFontSize,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 10.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }

                    PremiumDividerV3(appPurple, verticalPadding = verticalPaddingBetweenSections)

                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            provider.insignias.chunked(4).forEach { rowBadges ->
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
                            color = appBlue,
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

            MenuTacticoBe(
                isVisible = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                onAction = {
                    onManageShortcut(!isShortcut)
                    showContextMenu = false
                },
                touchOffset = touchOffset,
                emotion = if (isShortcut) EmocionBe.TRISTE else EmocionBe.FELIZ,
                actionLabel = if (isShortcut) "QUITAR FAVORITO" else "AGREGAR FAVORITO",
                actionIconEmoji = "📌"
            )
        }
    }
}



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


/**
 * PRESTADOR BUSINESS CARD V5: Una tarjeta de presentación profesional (Business Card).
 * [ELITE V5.2]: Rediseño total para 2 columnas, interacción centrada en áreas.
 * - Tap Card: Chat con prestador.
 * - Tap Avatar: Perfil del prestador.
 * - Tap Badges: Menú descriptivo.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrestadorBusinessCard(
    provider: PrestadorDominio,
    user: CuentaMaestroUsuario?,
    onAvatarClick: () -> Unit,
    onChatClick: (SenderProfile?) -> Unit,
    modifier: Modifier = Modifier,
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean) -> Unit = {},
    accentColor: Color = Color(0xFF22D3EE),
    idPerfilActivo: String? = null // [NEW] Inyección de soberanía desde el contexto
) {
    val appBlue = accentColor
    val appPurple = Color(0xFF9B51E0)
    val darkCardBg = Color(0xFF0F1520) 

    val haptic = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }

    var showSenderSelector by remember { mutableStateOf(false) }
    val senderProfiles = remember(user) { getAvailableSenderProfiles(user) }

    fun handleChatAction() {
        if (!idPerfilActivo.isNullOrBlank()) {
            // [ELITE]: Si hay un perfil inyectado, resolvemos y saltamos el menú
            val resolved = if (idPerfilActivo == "personal" || idPerfilActivo == user?.usuario?.perfil?.id) {
                senderProfiles.find { it.id == "personal" }
            } else {
                senderProfiles.find { it.branchId == idPerfilActivo || it.id == idPerfilActivo }
            }
            onChatClick(resolved ?: senderProfiles.firstOrNull())
        } else if (senderProfiles.size > 1) {
            showSenderSelector = true
        } else {
            onChatClick(senderProfiles.firstOrNull())
        }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth() // Dinámico para 2 columnas
            .height(115.dp)
            .combinedClickable(
                onClick = { handleChatAction() },
                onLongClick = {
                    // El offset aproximado para el menú contextual en combinedClickable es más difícil,
                    // pero para el estándar Maverick usamos una posición central si no hay offset.
                    touchOffset = Offset.Zero 
                    showContextMenu = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = darkCardBg,
            contentColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Glow Effect
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(listOf(appBlue.copy(0.06f), Color.Transparent)),
                    radius = size.maxDimension * 0.7f,
                    center = Offset(size.width * 0.15f, size.height * 0.2f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    // --- IZQUIERDA: AVATAR (Tap -> Perfil) ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        MoldeBurbujaPerfilV3(
                            perfil = PerfilIdentidadV3(
                                id = provider.id,
                                nombre = provider.titulo,
                                iniciales = provider.titulo.take(2).uppercase(),
                                photoUrl = ImageUtils.processImageSource(provider.urlMiniatura ?: provider.urlFoto),
                                estaEnLinea = provider.estaOnline,
                                estaVerificado = provider.estaVerificado,
                                esSuscripto = provider.estaSuscrito
                            ),
                            tamanoBase = 48.dp,
                            modifier = Modifier.clickable { onAvatarClick() }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                            Text(
                                text = " ${"%.1f".format(provider.reputacion)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = TextStyle().asCompact()
                            )
                        }
                    }

                    // --- DERECHA: INFO ---
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        AutoSizeText(
                            text = provider.titulo,
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            ).asCompact()
                        )

                        Text(
                            text = (provider.subtitulo ?: "Profesional").uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = appBlue,
                            letterSpacing = 0.5.sp,
                            style = TextStyle().asCompact()
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "DIRECCIÓN",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = appBlue.copy(0.7f),
                            style = TextStyle().asCompact()
                        )

                        AutoSizeText(
                            text = provider.nombreSucursal ?: "CASA CENTRAL",
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ).asCompact()
                        )

                        AutoSizeText(
                            text = provider.direccionVisible ?: "Ubicación no disponible",
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            ).asCompact()
                        )
                    }
                }

                // --- SOCALO MINI DE BADGES (Tap -> Menú) ---
                var showBadgesMenu by remember { mutableStateOf(false) }

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clickable { showBadgesMenu = true }, // Badges -> Menú
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        provider.insignias.take(7).forEach { badge ->
                            BadgeIconBoxV5Mini(
                                emoji = if (badge.estaActiva) badge.icono else null,
                                icon = if (!badge.estaActiva) getInactiveIconForId(badge.id) else null,
                                isActive = badge.estaActiva
                            )
                        }
                    }

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
                                color = appBlue,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        provider.insignias.forEach { badge ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    if (badge.estaActiva) appBlue.copy(0.1f) else Color.White.copy(0.05f),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (badge.estaActiva) {
                                                Text(text = badge.icono, fontSize = 16.sp)
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
                                                text = badge.etiqueta,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (badge.estaActiva) "ACTIVO" else "NO DISPONIBLE",
                                                color = if (badge.estaActiva) Color(0xFF22C55E) else Color.Gray,
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

            // --- SELECCIÓN DE REMITENTE (FAB OCULTO) ---
            SenderSelectionMenu(
                expanded = showSenderSelector,
                onDismissRequest = { showSenderSelector = false },
                profiles = senderProfiles,
                onProfileSelected = { sender ->
                    onChatClick(sender)
                    showSenderSelector = false
                }
            )

            // --- MENU TACTICO BE ---
            MenuTacticoBe(
                isVisible = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                onAction = {
                    onManageShortcut(!isShortcut)
                    showContextMenu = false
                },
                touchOffset = touchOffset,
                emotion = if (isShortcut) EmocionBe.TRISTE else EmocionBe.FELIZ,
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

/**
 * Helper para obtener perfiles de remitente (SSOT).
 */
private fun getAvailableSenderProfiles(user: CuentaMaestroUsuario?): List<SenderProfile> {
    if (user == null) return emptyList()
    val profiles = mutableListOf(SenderProfile("personal", null, user.usuario.perfil.nombreVisible, null, user.usuario.perfil.urlFoto))
    user.empresas.forEach { company ->
        company.sucursales.forEach { branch ->
            profiles.add(SenderProfile(
                id = company.empresa.id,
                branchId = branch.sucursal.id,
                name = branch.sucursal.nombre,
                subName = company.empresa.nombre,
                photoUrl = company.empresa.urlMiniatura ?: company.empresa.urlFoto
            ))
        }
    }
    return profiles
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorBusinessCardPreview() {
    ClienteTheme {
        Box(
            modifier = Modifier
                //.fillMaxSize()
                .background(Color(0xFF0A0E14))
                .padding(16.dp), 
            contentAlignment = Alignment.Center
        ) {
            PrestadorBusinessCard(
                provider = PrestadorDominio(
                    id = "5",
                    titulo = "Dr. Steve Smith",
                    subtitulo = "General Physician",
                    urlFoto = "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=256&auto=format&fit=crop",
                    reputacion = 4.9f,
                    estaVerificado = true,
                    estaOnline = true,
                    tipo = TipoPrestador.INDIVIDUAL,
                    insignias = listOf(
                        PerfilPrestadorInsignia("serv", "🛠️", "Brinda Servicio", true),
                        PerfilPrestadorInsignia("prod", "📦", "Vende Productos", false),
                        PerfilPrestadorInsignia("24h", "🕒", "Atención 24hs", true),
                        PerfilPrestadorInsignia("loc", "🏪", "Local Físico", true),
                        PerfilPrestadorInsignia("visit", "🏠", "A Domicilio", true),
                        PerfilPrestadorInsignia("env", "🚚", "Envíos", false),
                        PerfilPrestadorInsignia("date", "📅", "Turnos Online", true)
                    ),
                    estaSuscrito = true,
                    nombreSucursal = "Central Clinic",
                    direccionVisible = "77 Your Street Address, NY",
                    textoEstado = "ABIERTO AHORA ✅"
                ),
                onAvatarClick = {},
                user = null,
                onChatClick = { }
            )
        }
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun PrestadorCardV3Preview() {
    ClienteTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            PrestadorCardV3(
                provider = PrestadorDominio(
                    id = "1",
                    titulo = "app Tech S.A.",
                    subtitulo = "Software & Hardware",
                    urlFoto = "https://picsum.photos/seed/app/200/200",
                    reputacion = 4.95f,
                    estaVerificado = true,
                    estaOnline = true,
                    tipo = TipoPrestador.EMPRESA,
                    insignias = listOf(
                        PerfilPrestadorInsignia("24h", "🕒", "Atención 24hs", true),
                        PerfilPrestadorInsignia("loc", "🏪", "Local Físico", true),
                        PerfilPrestadorInsignia("visit", "🏠", "A Domicilio", true)
                    ),
                    estaSuscrito = true,
                    nombreSucursal = "Casa Central",
                    direccionVisible = "Av. Aconquija 2000, Yerba Buena"
                ),
                onClick = {},
                onChatClick = {},
                isShortcut = false,
                onManageShortcut = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceCardPreview() {
    ClienteTheme {
        ServiceCardScreen()
    }
}

// ==========================================================================================
// ---------- SECCIÓN: TARJETA DE PRESUPUESTO ULTRA PREMIUM (v2026.ELITE) -------------------
// ==========================================================================================

/**
 * Modelo de datos optimizado para la tarjeta A4 Ultra Premium.
 */
data class PresupuestoA4Entity(
    val id: String = "",
    val nombrePrestador: String = "PROVEEDOR",
    val urlFotoPrestador: String? = null,
    val fotoMiniaturaDocumento: String? = null,
    val categoria: String = "SERVICIO",
    val emojiCategoria: String = "📋",
    val estado: String = "ENVIADO", // ENVIADO, EN REVISIÓN, APROBADO
    val totalGeneral: Double = 45000.0,
    val leido: Boolean = true
)

/**
 * --- TARJETA DE PRESUPUESTO A4 (Improved Style) ---
 * [LEY #10]: Diseño de alto impacto con soporte para selección y multiselección.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuestoA4(
    modifier: Modifier = Modifier,
    presupuesto: PresupuestoA4Entity,
    estaSeleccionado: Boolean = false,
    esMultiseleccionActiva: Boolean = false,
    alHacerClick: () -> Unit = {},
    alHacerClickChat: () -> Unit = {},
    alHacerLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val locale = LocalConfiguration.current.locales[0]

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (estaSeleccionado) 0.98f else 1f,
        label = "scale_animation"
    )

    val colorBorde = if (estaSeleccionado) Color(0xFF3B82F6) else Color.Transparent

    Surface(
        modifier = modifier
            .scale(scale)
            .aspectRatio(1f / 1.414f)
            .drawBehind {
                val shadowColor = Color.Black.copy(alpha = if (isPressed) 0.8f else 0.6f)
                val shadowRadius = if (isPressed) 12.dp.toPx() else 25.dp.toPx()
                val offsetY = if (isPressed) 6.dp.toPx() else 12.dp.toPx()
                drawIntoCanvas { canvas ->
                    @Suppress("DEPRECATION")
                    val paint = Paint().asFrameworkPaint().apply {
                        color = Color.Transparent.toArgb()
                        setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb())
                    }
                    canvas.nativeCanvas.drawRoundRect(
                        0f, offsetY, size.width, size.height,
                        6.dp.toPx(), 6.dp.toPx(), paint
                    )
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (esMultiseleccionActiva) alHacerLongClick() else alHacerClick() },
                onLongClick = alHacerLongClick
            ),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(if (estaSeleccionado) 2.dp else 0.dp, colorBorde)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(presupuesto.fotoMiniaturaDocumento ?: "https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=400&auto=format&fit=crop")
                    .crossfade(true)
                    .build(),
                contentDescription = "Miniatura Documento A4",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
                    .align(Alignment.TopCenter)
            )

            if (estaSeleccionado) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF3B82F6).copy(alpha = 0.15f)))
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(22.dp)
                        .background(Color(0xFF3B82F6), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Column(
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = presupuesto.emojiCategoria, fontSize = 9.sp)
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.White.copy(alpha = 0.3f)))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = presupuesto.categoria.uppercase(locale),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val colorEstado = if (presupuesto.estado == "ENVIADO") Color(0xFF3B82F6) else Color(0xFFF59E0B)
                Box(
                    modifier = Modifier
                        .background(colorEstado.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .border(1.dp, colorEstado, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = presupuesto.estado.uppercase(locale),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.33f)
                    .background(Brush.verticalGradient(colors = listOf(Color(0xFF0F172A).copy(alpha = 0.75f), Color.Black.copy(alpha = 0.95f))))
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text("$", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = String.format(locale, "%,.0f", presupuesto.totalGeneral),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            MoldeBurbujaPerfilV3(
                                perfil = PerfilIdentidadV3(
                                    id = presupuesto.id,
                                    nombre = presupuesto.nombrePrestador,
                                    iniciales = presupuesto.nombrePrestador.take(2).uppercase(),
                                    photoUrl = presupuesto.urlFotoPrestador,
                                    estaVerificado = true,
                                    esSuscripto = true
                                ),
                                tamanoBase = 24.dp,
                                mostrarBadges = false
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = presupuesto.nombrePrestador.uppercase(locale),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.2f)))
                        Box(
                            modifier = Modifier.size(26.dp).clickable { alHacerClickChat() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💬", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewTarjetaPresupuestoA4() {
    ClienteTheme {
        Box(modifier = Modifier.padding(20.dp).width(150.dp)) {
            TarjetaPresupuestoA4(
                presupuesto = PresupuestoA4Entity(
                    nombrePrestador = "PBEM Informática",
                    totalGeneral = 11000.0,
                    categoria = "Servicio Técnico"
                )
            )
        }
    }
}


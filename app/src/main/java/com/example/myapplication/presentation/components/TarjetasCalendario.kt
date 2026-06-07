package com.example.myapplication.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.CalendarEventEntity
import com.example.myapplication.core.data.local.entity.EventType
import com.example.myapplication.core.data.local.entity.VisitStatus
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.components.M3VerticalDivider
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PALETA MAVERICK ELITE ---
 */
private val DarkBg = Color(0xFF020408)
private val SurfaceCard = Color(0xFF0F1520)
private val MaverickBlue = Color(0xFF2E91FF)
private val CyberNeon = Color(0xFF00FFC2)
private val StatusOk = Color(0xFF00E676)
private val StatusWait = Color(0xFFFFA000)
private val ActionRed = Color(0xFFFF4081)

// ==========================================================================================
// --- TARJETAS PRINCIPALES DEL CALENDARIO ---
// ==========================================================================================

/**
 * --- TARJETA DE EVENTO MODERNA ---
 * Diseño refinado con Glassmorphism y tipografía Cyberpunk
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernEventCard(
    event: CalendarEventEntity,
    isPast: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    val isCancelled = event.status == VisitStatus.CANCELLED
    val accent = Color(event.type.colorLong)
    val typeEmoji = when (event.type) {
        EventType.VISIT -> "🧰"
        EventType.APPOINTMENT -> "📅"
        EventType.SHIPPING -> "🚛"
    }
    
    // [ELITE] Lógica de Atenuación Contextual
    val contentAlpha = if (isPast) 0.4f else 1.0f
    val finalAccent = if (isPast) Color.Gray else accent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceCard.copy(alpha = if (isPast) 0.5f else 1.0f),
        border = BorderStroke(1.dp, Color.White.copy(if (isPast) 0.02f else 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sección Hora
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                Text(
                    event.time.replace("hs", "").trim(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = (if (isCancelled) Color.Gray else Color.White).copy(alpha = contentAlpha),
                    letterSpacing = (-0.5).sp
                )
                Text("HS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = finalAccent.copy(0.7f * contentAlpha))
            }

            M3VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 12.dp), color = Color.White.copy(0.1f * contentAlpha))

            // Info Central
            Column(modifier = Modifier.weight(1f)) {
                // Fila de Etiquetas (Tipo + Categoría)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tipo con Emoji
                    Text(
                        text = "$typeEmoji ${event.type.label.uppercase()}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = finalAccent.copy(alpha = contentAlpha),
                        letterSpacing = 0.5.sp
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Categoría (Centrada a la derecha del tipo)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(event.categoryEmoji ?: "📍", fontSize = 10.sp, modifier = Modifier.graphicsLayer(alpha = contentAlpha))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            (event.categoryName ?: "SERVICIO").uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f * contentAlpha),
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Text(
                    text = event.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                )
                
                Text(
                    text = event.provider,
                    fontSize = 12.sp,
                    color = Color.White.copy(0.5f * contentAlpha),
                    fontWeight = FontWeight.Medium
                )
            }

            // Acción Rápida (Mensaje Emoji Redondo Superpuesto)
            Surface(
                modifier = Modifier
                    .offset(x = 12.dp)
                    .size(46.dp)
                    .align(Alignment.CenterVertically), // Centrado verticalmente
                shape = CircleShape,
                color = if (isPast) Color.Gray.copy(0.1f) else MaverickBlue.copy(0.15f),
                border = BorderStroke(1.dp, if (isPast) Color.Gray.copy(0.3f) else MaverickBlue.copy(0.4f)),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().clickable { onMessageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📨",
                        fontSize = 20.sp,
                        modifier = Modifier.graphicsLayer(alpha = contentAlpha)
                    )
                }
            }
        }
    }
}

/**
 * --- CAROUSEL DE PRÓXIMOS COMPROMISOS ---
 * Maneja múltiples eventos simultáneos o cercanos con autoplay.
 * Si no hay eventos, muestra un banner de estado vacío.
 */
@Composable
fun NextCommitmentCarousel(
    events: List<CalendarEventEntity>,
    onClick: (CalendarEventEntity) -> Unit,
    onMessageClick: (String) -> Unit
) {
    if (events.isEmpty()) {
        EmptyCommitmentBanner()
        return
    }

    val pagerState = rememberPagerState { events.size }
    
    // Lógica de Autoplay (solo si hay más de uno)
    if (events.size > 1) {
        LaunchedEffect(pagerState) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                val nextPage = (pagerState.currentPage + 1) % events.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            NextCommitmentBanner(
                event = events[page],
                onClick = { onClick(events[page]) },
                onMessageClick = onMessageClick
            )
        }
        
        // Indicadores de página (Dots)
        if (events.size > 1) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(events.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaverickBlue else Color.White.copy(0.2f)
                    val width by animateDpAsState(if (pagerState.currentPage == iteration) 12.dp else 6.dp, label = "")
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(width, 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * --- BANNER DE ESTADO VACÍO (PRÓXIMOS) ---
 * Diseño Elite con Glassmorphism y tipografía de alto impacto.
 */
@Composable
fun EmptyCommitmentBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.01f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        // Fondo decorativo sutil
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomEnd)
                .offset(20.dp, 20.dp)
                .background(Brush.radialGradient(listOf(MaverickBlue.copy(0.05f), Color.Transparent)))
        )

        Row(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono 3D / Emoji
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(0.05f),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("☕", fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AGENDA DESPEJADA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaverickBlue,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "No tienes compromisos próximos.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "¡Es un buen momento para planificar algo nuevo!",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

/**
 * --- BANNER DE PRÓXIMO COMPROMISO ---
 * Estilo "Hero" con organización optimizada:
 * Izquierda: Emoji Categoría + Hora Grande
 * Derecha: Detalles jerárquicos + Acción Cyber
 */
@Composable
fun NextCommitmentBanner(
    event: CalendarEventEntity,
    onClick: () -> Unit,
    onMessageClick: (String) -> Unit
) {
    val typeColor = Color(event.type.colorLong)
    val typeEmoji = when (event.type) {
        EventType.VISIT -> "🧰"
        EventType.APPOINTMENT -> "📅"
        EventType.SHIPPING -> "🚛"
    }

    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(typeColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "PRÓXIMO COMPROMISO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(0.6f),
                letterSpacing = 1.2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            color = typeColor.copy(0.08f),
            border = BorderStroke(1.dp, typeColor.copy(0.2f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Glow decorativo de fondo
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.TopEnd)
                        .offset(60.dp, (-60).dp)
                        .background(Brush.radialGradient(listOf(typeColor.copy(0.12f), Color.Transparent)))
                )

                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                    // SECCIÓN IZQUIERDA: EMOJI + HORA
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(0.05f),
                            border = BorderStroke(1.dp, typeColor.copy(0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(event.categoryEmoji ?: "📍", fontSize = 32.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = event.time.replace("hs", "").trim(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "HS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = typeColor.copy(0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // SECCIÓN DERECHA: INFO JERÁRQUICA
                    Column(modifier = Modifier.weight(1f)) {
                        // 1. Categoría
                        Text(
                            (event.categoryName ?: "SERVICIO").uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaverickBlue,
                            letterSpacing = 1.5.sp
                        )

                        // 2. Tipo de Evento (Con Emoji y Color)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(typeEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                event.type.label.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = typeColor
                            )
                        }

                        // 3. Título del Evento
                        Text(
                            text = event.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // 4. Prestador
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(MaverickIcons.Business, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(event.provider, fontSize = 12.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold)
                        }

                        // 5. Dirección
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(MaverickIcons.Location, null, modifier = Modifier.size(14.dp), tint = Color.Gray.copy(0.6f))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                event.address,
                                fontSize = 11.sp,
                                color = Color.White.copy(0.5f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Acción Rápida (Mensaje Emoji Redondo Superpuesto)
                    Surface(
                        modifier = Modifier
                            .offset(x = 16.dp)
                            .size(54.dp)
                            .align(Alignment.CenterVertically), // Centrado verticalmente
                        shape = CircleShape,
                        color = typeColor.copy(0.15f),
                        border = BorderStroke(1.dp, typeColor.copy(0.4f)),
                        shadowElevation = 10.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clickable { onMessageClick(event.providerId) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📨",
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * --- ITEM DE HISTORIAL ---
 */
@Composable
fun HistoryEventItem(event: CalendarEventEntity) {
    val accent = Color(event.type.colorLong)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(DarkBg.copy(0.5f), shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar o Emoji
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(accent.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (event.providerPhotoUrl != null) {
                AsyncImage(
                    model = event.providerPhotoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = event.categoryEmoji ?: event.type.emoji,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.provider,
                fontSize = 12.sp,
                color = Color.LightGray
            )
            Text(
                text = "${event.date} • ${event.time} hs",
                fontSize = 11.sp,
                color = MaverickBlue,
                fontWeight = FontWeight.Bold
            )
        }

        // Estado (Badge pequeño)
        Box(
            modifier = Modifier
                .background(
                    color = when(event.status) {
                        VisitStatus.CONFIRMED -> StatusOk
                        VisitStatus.CANCELLED -> ActionRed
                        else -> Color.Gray
                    }.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = event.status.name,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = when(event.status) {
                    VisitStatus.CONFIRMED -> StatusOk
                    VisitStatus.CANCELLED -> ActionRed
                    else -> Color.Gray
                }
            )
        }
    }
}

// ==========================================================================================
// --- POPUP PROFESIONAL DE DETALLES ---
// ==========================================================================================

/**
 * --- POPUP DE DETALLES DE EVENTO PREMIUM ---
 * Rediseño completo para una experiencia más profesional y rica en datos.
 */
@Composable
fun EventDetailPopup(
    event: CalendarEventEntity,
    onDismiss: () -> Unit,
    onChatClick: (String) -> Unit,
    onRescheduleClick: (CalendarEventEntity) -> Unit,
    onCancelClick: (CalendarEventEntity) -> Unit,
    onProviderClick: (String) -> Unit
) {
    val context = LocalContext.current
    val accent = Color(event.type.colorLong)
    val isCancelled = event.status == VisitStatus.CANCELLED

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(32.dp))
                .border(
                    BorderStroke(1.dp, Brush.verticalGradient(listOf(accent.copy(0.3f), Color.Transparent))),
                    RoundedCornerShape(32.dp)
                ),
            color = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                // --- CABECERA (GLASS) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(accent.copy(0.15f), Color.Transparent)
                            )
                        )
                ) {
                    // Botón Cerrar (X)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(40.dp)
                            .background(Color.White.copy(0.05f), CircleShape)
                            .border(1.dp, Color.White.copy(0.1f), CircleShape)
                    ) {
                        Icon(MaverickIcons.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Black.copy(0.4f),
                            border = BorderStroke(1.dp, accent.copy(0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(event.categoryEmoji ?: "📍", fontSize = 36.sp)
                            }
                        }
                        
                        Spacer(Modifier.width(20.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = (event.categoryName ?: "SERVICIO").uppercase(),
                                color = MaverickBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = event.title,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 26.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    
                    // --- ESTADO + ID ---
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadgePremium(event.status)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "REF: #${event.id.takeLast(6).uppercase()}",
                            color = Color.White.copy(0.15f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- CRONOGRAMA (CARD) ---
                    InfoCard(label = "⏰ PROGRAMACIÓN") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DateTimeItem("FECHA", event.date, MaverickIcons.Calendar)
                            Box(Modifier.width(1.dp).height(30.dp).background(Color.White.copy(0.05f)))
                            DateTimeItem("HORARIO", event.time, MaverickIcons.Clock)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- UBICACIÓN (CARD) ---
                    InfoCard(label = "📍 UBICACIÓN") {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).background(CyberNeon.copy(0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(MaverickIcons.Map, null, tint = CyberNeon, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.address, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
                                TextButton(
                                    onClick = {
                                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(event.address)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        context.startActivity(intent)
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("ABRIR EN MAPAS", color = CyberNeon, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- PRESTADOR (CARD) ---
                    InfoCard(label = "👤 PROFESIONAL") {
                        Row(
                            modifier = Modifier.padding(12.dp).clickable { onProviderClick(event.providerId) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(52.dp)) {
                                if (event.providerPhotoUrl != null) {
                                    AsyncImage(
                                        model = event.providerPhotoUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(event.avatarColorLong).copy(0.2f), CircleShape).border(1.dp, Color(event.avatarColorLong), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(event.provider.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.provider, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("Ver perfil profesional", color = MaverickBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { onChatClick(event.providerId) },
                                modifier = Modifier.background(MaverickBlue, CircleShape).size(40.dp)
                            ) {
                                Icon(MaverickIcons.Message, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // --- BOTONES DE ACCIÓN ---
                    if (!isCancelled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { onCancelClick(event) },
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, ActionRed.copy(0.3f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionRed)
                            ) {
                                Text("ANULAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }

                            Button(
                                onClick = { onRescheduleClick(event) },
                                modifier = Modifier.weight(1.5f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accent)
                            ) {
                                Icon(MaverickIcons.Update, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("REPROGRAMAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onRescheduleClick(event) },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue)
                        ) {
                            Text("SOLICITAR NUEVA CITA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label, 
            color = Color.White.copy(0.3f), 
            fontSize = 9.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            color = Color.White.copy(0.03f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.06f)),
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

// ==========================================================================================
// --- COMPONENTES ATÓMICOS DE DISEÑO ---
// ==========================================================================================

@Composable
private fun InfoSection(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(accentColor.copy(0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
            if (actionLabel != null && onAction != null) {
                TextButton(
                    onClick = onAction,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = actionLabel,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTimeItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatusBadgePremium(status: VisitStatus) {
    val (color, text, icon) = when(status) {
        VisitStatus.CONFIRMED -> Triple(StatusOk, "CONFIRMADO", MaverickIcons.Check)
        VisitStatus.PENDING -> Triple(StatusWait, "PENDIENTE", MaverickIcons.Timer)
        VisitStatus.CANCELLED -> Triple(ActionRed, "ANULADO", MaverickIcons.Close)
    }

    Surface(
        color = color.copy(0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun EventStatusPill(status: String) {
    val color = when(status) {
        "ABIERTA" -> CyberNeon
        "ADJUDICADA" -> StatusOk
        "CANCELADA" -> ActionRed
        else -> Color.Gray
    }

    Surface(
        color = color.copy(0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    events: List<CalendarEventEntity>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "HISTORIAL DE ACTIVIDADES",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(16.dp),
                color = MaverickBlue,
                letterSpacing = 1.sp
            )

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NO HAY EVENTOS PASADOS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        HistoryEventItem(event = event)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernDateHeader(
    date: String,
    isToday: Boolean,
    isCollapsed: Boolean,
    onToggle: () -> Unit
) {
    val dayFormat = SimpleDateFormat("d", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMMM", Locale("es", "ES"))
    val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale("es", "ES"))
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val dateObj = try { isoFormat.parse(date) } catch (_: Exception) { null }

    val dayText = dateObj?.let { dayFormat.format(it) } ?: "0"
    val monthText = dateObj?.let { monthFormat.format(it) } ?: ""
    val dayOfWeekText = dateObj?.let { dayOfWeekFormat.format(it) } ?: ""

    val rotation by animateFloatAsState(if (isCollapsed) -90f else 0f, label = "rotate")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // --- CÁPSULA TÁCTICA FLOTANTE (Elite M3 Style) ---
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable { onToggle() },
            color = Color.Black.copy(alpha = 0.85f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        if (isToday) CyberNeon.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                        if (isToday) CyberNeon else MaverickBlue.copy(alpha = 0.4f),
                        if (isToday) CyberNeon.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Indicador de "Hoy" o Punto Táctico
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isToday) CyberNeon else Color.White.copy(alpha = 0.3f))
                        .drawBehind {
                            if (isToday) {
                                drawCircle(
                                    color = CyberNeon,
                                    radius = size.width * 1.5f,
                                    alpha = 0.3f
                                )
                            }
                        }
                )

                // Texto de Fecha (Simplificado y Moderno)
                val displayDate = if (isToday) {
                    "HOY, $dayText DE ${monthText.uppercase()}"
                } else {
                    "${dayOfWeekText.uppercase()} $dayText"
                }

                Text(
                    text = displayDate,
                    style = CyberTypography.MonospaceData.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        color = if (isToday) CyberNeon else Color.White
                    )
                )

                // Indicador de Colapso
                Icon(
                    imageVector = MaverickIcons.ChevronDown,
                    contentDescription = null,
                    tint = if (isToday) CyberNeon else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }
    }
}

@Composable
fun CalendarPopup(
    selectedDate: Calendar,
    daysWithEventColors: Map<String, Long>,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    var displayMonth by remember { mutableStateOf(selectedDate.clone() as Calendar) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, MaverickBlue.copy(0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                CalendarHeaderPro(
                    currentDate = displayMonth,
                    onPreviousMonth = { displayMonth = (displayMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) } },
                    onNextMonth = { displayMonth = (displayMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeekDaysHeaderPro()
                CalendarGridPro(
                    currentDate = displayMonth,
                    selectedDate = selectedDate,
                    daysWithEventColors = daysWithEventColors,
                    dateFormat = dateFormat,
                    onDayClick = { day ->
                        val newDate = (displayMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        onDateSelected(newDate)
                    }
                )
            }
        }
    }
}

@Composable
fun EventContextMenu(onDismiss: () -> Unit, onAction: (String) -> Unit) {
    Popup(alignment = Alignment.Center, onDismissRequest = onDismiss, properties = androidx.compose.ui.window.PopupProperties(focusable = true)) {
        Surface(
            modifier = Modifier.width(220.dp),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceCard,
            border = BorderStroke(1.dp, Color.White.copy(0.1f)),
            shadowElevation = 20.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                ContextItem(label = "VER DETALLE", emoji = "🔍", onClick = { onAction("detail") })
                ContextItem(label = "ANULAR CITA", emoji = "⚠️", color = ActionRed, onClick = { onAction("cancel") })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.05f))
                ContextItem(label = "ELIMINAR", emoji = "🗑️", color = Color.Gray, onClick = { onAction("delete") })
            }
        }
    }
}

@Composable
fun ContextItem(label: String, emoji: String, color: Color = Color.White, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
fun EmptyStateCalendar() {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("☕", fontSize = 60.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text("AGENDA DESPEJADA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("No hay eventos que coincidan.", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun CalendarHeaderPro(
    currentDate: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val months = listOf("ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE")
    Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.05f), RoundedCornerShape(16.dp)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPreviousMonth) { Icon(MaverickIcons.ChevronLeft, null, tint = MaverickBlue) }
        Text("${months[currentDate[Calendar.MONTH]]} ${currentDate[Calendar.YEAR]}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
        IconButton(onClick = onNextMonth) { Icon(MaverickIcons.ChevronRight, null, tint = MaverickBlue) }
    }
}

@Composable
fun WeekDaysHeaderPro() {
    val days = listOf("DO", "LU", "MA", "MI", "JU", "VI", "SA")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        days.forEach { Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray) }
    }
}

@Composable
fun CalendarGridPro(currentDate: Calendar, selectedDate: Calendar, daysWithEventColors: Map<String, Long>, dateFormat: SimpleDateFormat, onDayClick: (Int) -> Unit) {
    val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDay = Calendar.getInstance().apply { time = currentDate.time; set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val today = Calendar.getInstance()
    Column {
        var day = 1
        val rows = (firstDay + daysInMonth + 6) / 7
        repeat(rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cell = it * 7 + col
                    if (cell < firstDay || day > daysInMonth) { Box(modifier = Modifier.weight(1f).aspectRatio(1f)) }
                    else {
                        val d = day
                        val dateToCheck = (currentDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, d) }
                        val dateStr = dateFormat.format(dateToCheck.time)
                        DayCellPro(d, isSameDay(dateToCheck, selectedDate), isSameDay(dateToCheck, today), daysWithEventColors.containsKey(dateStr), Color(daysWithEventColors[dateStr] ?: 0L)) { onDayClick(d) }
                        day++
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCellPro(day: Int, isSelected: Boolean, isToday: Boolean, hasEvent: Boolean, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp).clip(CircleShape).background(if (isSelected) MaverickBlue else if (isToday) Color.White.copy(0.1f) else Color.Transparent).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day.toString(), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium, color = if (isSelected) Color.White else Color.Gray)
            if (hasEvent) Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Color.White else color))
        }
    }
}

@Composable
fun CancelVisitConfirmModal(event: CalendarEventEntity, onConfirm: (CalendarEventEntity) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = SurfaceCard, border = BorderStroke(1.dp, ActionRed.copy(0.3f))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(MaverickIcons.Warning, null, modifier = Modifier.size(48.dp), tint = ActionRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text("¿ANULAR COMPROMISO?", fontWeight = FontWeight.Black, color = Color.White)
                Text("Esta acción notificará al profesional.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onConfirm(event) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = ActionRed)) { Text("SÍ, ANULAR") }
                TextButton(onClick = onDismiss) { Text("MANTENER", color = Color.White) }
            }
        }
    }
}

fun isSameDay(d1: Calendar, d2: Calendar) = d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR) && d1.get(Calendar.MONTH) == d2.get(Calendar.MONTH) && d1.get(Calendar.DAY_OF_MONTH) == d2.get(Calendar.DAY_OF_MONTH)

// ==========================================================================================
// --- PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun EventCardsPreview() {
    MyApplicationTheme {
        val dummy = CalendarEventEntity(
            id = "1",
            date = "2024-05-10",
            time = "10:30 hs",
            type = EventType.VISIT,
            title = "Mantenimiento Aire Acondicionado",
            provider = "Seba Climatización",
            providerId = "p1",
            address = "Av. Santa Fe 1234, CABA",
            status = VisitStatus.CONFIRMED,
            categoryName = "Climatización",
            categoryEmoji = "❄️"
        )
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NextCommitmentBanner(event = dummy, onClick = {}, onMessageClick = {})
            ModernEventCard(event = dummy, onClick = {}, onLongClick = {}, onMessageClick = {})
            ModernEventCard(event = dummy.copy(id="2", time="08:00 hs"), isPast = true, onClick = {}, onLongClick = {}, onMessageClick = {})
            HistoryEventItem(event = dummy.copy(status = VisitStatus.CANCELLED))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun EventDetailPopupPreview() {
    MyApplicationTheme {
        val dummy = CalendarEventEntity(
            id = "1",
            date = "Viernes, 10 de Mayo",
            time = "10:30 hs",
            type = EventType.VISIT,
            title = "Reparación de Tablero Eléctrico",
            provider = "Ing. Marcos Tech",
            providerId = "p1",
            address = "Calle Falsa 123, Planta Baja B",
            status = VisitStatus.CONFIRMED,
            categoryName = "Electricidad",
            categoryEmoji = "⚡"
        )
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EventDetailPopup(
                event = dummy,
                onDismiss = {},
                onChatClick = {},
                onRescheduleClick = {},
                onCancelClick = {},
                onProviderClick = {}
            )
        }
    }
}










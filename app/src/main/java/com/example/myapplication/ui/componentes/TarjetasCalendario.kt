package com.example.myapplication.ui.componentes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.uishared.estilos.AppIcons
import com.example.myapplication.uishared.estilos.CyberTypography
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.ui.components.MoldeMultiSeleccion
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- EXTENSIONES DE APOYO MAV ---
 */
private val TipoEvento.label: String get() = when(this) {
    TipoEvento.VISITA_TECNICA -> "Visita Técnica"
    TipoEvento.TURNO_CITA -> "Turno en Local"
    TipoEvento.ENVIO_FLETE -> "Envío de Producto"
    TipoEvento.BLOQUEO_ADMIN -> "Bloqueo"
}

private val TipoEvento.colorLong: Long get() = when(this) {
    TipoEvento.VISITA_TECNICA -> 0xFF2E91FF
    TipoEvento.TURNO_CITA -> 0xFF00FFC2
    TipoEvento.ENVIO_FLETE -> 0xFFFFA000
    TipoEvento.BLOQUEO_ADMIN -> 0xFF757575
}

private val TipoEvento.emoji: String get() = when(this) {
    TipoEvento.VISITA_TECNICA -> "🛠️"
    TipoEvento.TURNO_CITA -> "📅"
    TipoEvento.ENVIO_FLETE -> "🚚"
    TipoEvento.BLOQUEO_ADMIN -> "🚫"
}

/**
 * --- PALETA Elite ---
 */
private val DarkBg = Color(0xFF020408)
private val SurfaceCard = Color(0xFF0F1520)
private val appBlue = Color(0xFF2E91FF)
private val CyberNeon = Color(0xFF00FFC2)
private val StatusOk = Color(0xFF00E676)
private val StatusWait = Color(0xFFFFA000)
private val ActionRed = Color(0xFFFF4081)

/**
 * --- UTILIDADES DE FORMATEO ELITE v11.0 ---
 * [ELITE] Se movió a funciones de extensión o locales para observar el Locale de Compose.
 */
private fun formatTimePro(timestamp: Long, locale: Locale): String =
    SimpleDateFormat("HH:mm", locale).format(Date(timestamp))

private fun formatDatePro(timestamp: Long, locale: Locale): String =
    SimpleDateFormat("dd/MM/yyyy", locale).format(Date(timestamp))

// ==========================================================================================
// --- TARJETAS PRINCIPALES DEL CALENDARIO ---
// ==========================================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernEventCard(
    event: com.example.myapplication.core.dominio.modelos.EventoDominio,
    isPast: Boolean = false,
    hasConflict: Boolean = false,
    estaSeleccionado: Boolean = false,
    modoMultiseleccionActivo: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val timeStr = event.horaTexto

    val isCancelled = event.estado == EstadoEvento.CANCELADO
    val accent = Color(event.colorAcentoHex)
    val typeEmoji = event.emojiTipo

    val contentAlpha = if (isPast) 0.4f else 1.0f
    val finalAccent = if (isPast) Color.Gray else accent

    MoldeMultiSeleccion(
        estaSeleccionado = estaSeleccionado,
        modoMultiseleccionActivo = modoMultiseleccionActivo,
        colorAcento = accent,
        radioCurvatura = 12.dp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp) 
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            shape = RoundedCornerShape(12.dp), 
            color = SurfaceCard.copy(alpha = if (isPast) 0.5f else 1.0f),
            border = BorderStroke(1.dp, if (hasConflict) ActionRed.copy(alpha = 0.5f) else if (estaSeleccionado) accent else Color.White.copy(if (isPast) 0.02f else 0.05f)),
            shadowElevation = if (isPast) 0.dp else 2.dp 
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sección Avatar
                Box(modifier = Modifier.size(40.dp)) { 
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, finalAccent.copy(alpha = 0.3f))
                    ) {
                        if (!event.urlFotoParticipante.isNullOrBlank()) {
                            AsyncImage(
                                model = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(event.urlFotoParticipante),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = (event.nombreParticipante).take(1).uppercase(),
                                    color = finalAccent,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Info Central
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (hasConflict) {
                            Text(text = "⚠️ SOLAPAMIENTO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ActionRed, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = "$typeEmoji ${event.tipo.label.uppercase()}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = finalAccent.copy(alpha = contentAlpha),
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Sobre de mensaje alineado con el emoji/tipo
                        Surface(
                            modifier = Modifier.size(24.dp).clickable { onMessageClick() },
                            shape = CircleShape,
                            color = appBlue.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, appBlue.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📨", fontSize = 12.sp)
                            }
                        }
                    }

                    Text(
                        text = event.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = contentAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.nombreParticipante,
                            fontSize = 11.sp,
                            color = Color.White.copy(0.6f * contentAlpha),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (event.direccion.isNotBlank() && event.direccion != "Ver detalles en Chat") {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(AppIcons.Location, null, modifier = Modifier.size(10.dp), tint = Color.Gray.copy(0.5f))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = event.direccion,
                                fontSize = 9.sp,
                                color = Color.White.copy(0.4f * contentAlpha),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Sección Hora en una sola línea (Más grande)
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.wrapContentWidth()) {
                    Text(
                        text = timeStr.replace(" hs", ""),
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Black,
                        color = (if (isCancelled) Color.Gray else if (hasConflict) ActionRed else Color.White).copy(alpha = contentAlpha),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "HS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = (if (hasConflict) ActionRed else finalAccent).copy(0.7f * contentAlpha)
                    )
                }
            }
        }
    }
}

@Composable
fun NextCommitmentCarousel(
    events: List<com.example.myapplication.core.dominio.modelos.EventoDominio>,
    onClick: (com.example.myapplication.core.dominio.modelos.EventoDominio) -> Unit,
    onMessageClick: (String) -> Unit
) {
    if (events.isEmpty()) {
        EmptyCommitmentBanner()
        return
    }
    val pagerState = rememberPagerState { events.size }
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
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            NextCommitmentBanner(event = events[page], onClick = { onClick(events[page]) }, onMessageClick = onMessageClick)
        }
        if (events.size > 1) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                repeat(events.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) appBlue else Color.White.copy(0.2f)
                    val width by animateDpAsState(if (pagerState.currentPage == iteration) 12.dp else 6.dp, label = "")
                    Box(modifier = Modifier.padding(horizontal = 3.dp).clip(CircleShape).background(color).size(width, 6.dp))
                }
            }
        }
    }
}

@Composable
fun NextCommitmentBanner(
    event: com.example.myapplication.core.dominio.modelos.EventoDominio,
    onClick: () -> Unit,
    onMessageClick: (String) -> Unit
) {
    val timeStr = event.horaTexto

    val typeColor = Color(event.colorAcentoHex)
    val typeEmoji = event.emojiTipo

    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(typeColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text("PRÓXIMO COMPROMISO", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White.copy(0.6f), letterSpacing = 1.2.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), color = typeColor.copy(0.08f), border = BorderStroke(1.dp, typeColor.copy(0.2f))) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(60.dp, (-60).dp).background(Brush.radialGradient(listOf(typeColor.copy(0.12f), Color.Transparent))))
                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp).padding(0.dp)) {
                        Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, typeColor.copy(0.3f))) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!event.urlFotoParticipante.isNullOrBlank()) {
                                    AsyncImage(
                                        model = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(event.urlFotoParticipante),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("📍", fontSize = 32.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = timeStr.replace(" hs", ""), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = (-1).sp)
                        Text(text = "HS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = typeColor.copy(0.8f))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text((event.tipo.label).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = appBlue, letterSpacing = 1.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(typeEmoji, fontSize = 12.sp); Spacer(modifier = Modifier.width(6.dp))
                            Text(event.tipo.label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = typeColor)

                            // TODO: Re-añadir nombreRecurso si se agrega al UiModel
                        }
                        Text(text = event.titulo, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(AppIcons.Business, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(event.nombreParticipante, fontSize = 12.sp, color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(AppIcons.Location, null, modifier = Modifier.size(14.dp), tint = Color.Gray.copy(0.6f))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(event.direccion, fontSize = 11.sp, color = Color.White.copy(0.5f), maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
                        }
                    }
                    Surface(modifier = Modifier.offset(x = 16.dp).size(54.dp).align(Alignment.CenterVertically), shape = CircleShape, color = typeColor.copy(0.15f), border = BorderStroke(1.dp, typeColor.copy(0.4f)), shadowElevation = 10.dp) {
                        Box(modifier = Modifier.fillMaxSize().clickable { onMessageClick(event.idParticipante) }, contentAlignment = Alignment.Center) { Text(text = "📨", fontSize = 24.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryEventItem(event: com.example.myapplication.core.dominio.modelos.EventoDominio) {
    val dateStr = event.fechaTexto
    val timeStr = event.horaTexto

    val accent = Color(event.colorAcentoHex)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).background(DarkBg.copy(0.5f), shape = RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).background(accent.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            if (event.urlFotoParticipante != null) { AsyncImage(model = event.urlFotoParticipante, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop) }
            else { Text(text = event.emojiTipo, fontSize = 20.sp) }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = event.nombreParticipante, fontSize = 12.sp, color = Color.LightGray)
            Text(text = "$dateStr • $timeStr", fontSize = 11.sp, color = appBlue, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.background(color = when(event.estado) { EstadoEvento.CONFIRMADO -> StatusOk; EstadoEvento.CANCELADO -> ActionRed; else -> Color.Gray }.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(text = event.estado.name, fontSize = 9.sp, fontWeight = FontWeight.Black, color = when(event.estado) { EstadoEvento.CONFIRMADO -> StatusOk; EstadoEvento.CANCELADO -> ActionRed; else -> Color.Gray })
        }
    }
}

@Composable
fun EventDetailPopup(
    event: com.example.myapplication.core.dominio.modelos.EventoDominio,
    onDismiss: () -> Unit,
    onChatClick: (String) -> Unit,
    onRescheduleClick: (com.example.myapplication.core.dominio.modelos.EventoDominio) -> Unit,
    onCancelClick: (com.example.myapplication.core.dominio.modelos.EventoDominio) -> Unit,
    onProviderClick: (String) -> Unit,
    currentLat: Double? = null,
    currentLon: Double? = null
) {
    val context = LocalContext.current
    val dateStr = event.fechaTexto
    val timeStr = event.horaTexto

    val accent = Color(event.colorAcentoHex)
    val isCancelled = event.estado == EstadoEvento.CANCELADO

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).clip(RoundedCornerShape(32.dp)).border(BorderStroke(1.dp, Brush.verticalGradient(listOf(accent.copy(0.3f), Color.Transparent))), RoundedCornerShape(32.dp)),
            color = SurfaceCard
        ) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Brush.verticalGradient(listOf(accent.copy(0.15f), Color.Transparent)))) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(40.dp).background(Color.White.copy(0.05f), CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape)) { Icon(AppIcons.Close, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    Row(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(20.dp), color = Color.Black.copy(0.4f), border = BorderStroke(1.dp, accent.copy(0.4f))) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!event.urlFotoParticipante.isNullOrBlank()) {
                                    AsyncImage(
                                        model = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(event.urlFotoParticipante),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("📍", fontSize = 36.sp)
                                }
                            }
                        }
                        Spacer(Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = (event.tipo.label).uppercase(), color = appBlue, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                            Text(text = event.titulo, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, lineHeight = 26.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None)
                        }
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadgePremium(event.estado)
                    }

                    Spacer(Modifier.height(24.dp))
                    InfoCard(label = "⏰ PROGRAMACIÓN") {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            DateTimeItem("FECHA", dateStr, AppIcons.Calendar)
                            Box(Modifier.width(1.dp).height(30.dp).background(Color.White.copy(0.05f)))
                            DateTimeItem("HORARIO", timeStr, AppIcons.Clock)
                        }
                    }

                    if (event.direccion.isNotBlank() && event.direccion != "Ver detalles en Chat") {
                        Spacer(Modifier.height(16.dp))
                        InfoCard(label = "📍 UBICACIÓN") {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                                Box(modifier = Modifier.size(36.dp).background(CyberNeon.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(AppIcons.Map, null, tint = CyberNeon, modifier = Modifier.size(18.dp)) }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(event.direccion, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp)
                                    TextButton(onClick = { 
                                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(event.direccion)}")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent) 
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "No se pudo abrir el mapa", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(30.dp)) { Text("ABRIR EN MAPAS", color = CyberNeon, fontSize = 11.sp, fontWeight = FontWeight.Black) }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    InfoCard(label = "👤 PARTICIPANTE") {
                        Row(modifier = Modifier.padding(12.dp).clickable { onProviderClick(event.idParticipante) }, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(52.dp)) {
                                if (event.urlFotoParticipante != null) {
                                    AsyncImage(
                                        model = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(event.urlFotoParticipante),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().background(accent.copy(0.2f), CircleShape).border(1.dp, accent, CircleShape), contentAlignment = Alignment.Center) { Text((event.nombreParticipante).take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold) }
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.nombreParticipante, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("Ver perfil", color = appBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onChatClick(event.idParticipante) }, modifier = Modifier.background(appBlue, CircleShape).size(40.dp)) { Icon(AppIcons.Message, null, tint = Color.Black, modifier = Modifier.size(20.dp)) }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    if (!isCancelled) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { onCancelClick(event) }, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, ActionRed.copy(0.3f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = ActionRed)) { Text("ANULAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                            Button(onClick = { onRescheduleClick(event) }, modifier = Modifier.weight(1.5f).height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accent)) { Icon(AppIcons.Update, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("REPROGRAMAR", fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                        }
                    } else {
                        Button(onClick = { onRescheduleClick(event) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = appBlue)) { Text("SOLICITAR NUEVA CITA", fontWeight = FontWeight.Black, letterSpacing = 1.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryBottomSheet(
    events: List<com.example.myapplication.core.dominio.modelos.EventoDominio>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = SurfaceCard, contentColor = Color.White, dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(text = "HISTORIAL DE ACTIVIDADES", fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(16.dp), color = appBlue, letterSpacing = 1.sp)
            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("NO HAY EVENTOS PASADOS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(events, key = { it.id }) { event -> HistoryEventItem(event = event) }
                }
            }
        }
    }
}

@Composable
fun EmptyCommitmentBanner() {
    val typeColor = appBlue
    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp, 16.dp).clip(RoundedCornerShape(4.dp)).background(typeColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ESTADO DE AGENDA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White.copy(0.6f), letterSpacing = 1.2.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            color = typeColor.copy(0.08f),
            border = BorderStroke(1.dp, typeColor.copy(0.2f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(160.dp).align(Alignment.BottomEnd).offset(60.dp, 60.dp).background(Brush.radialGradient(listOf(typeColor.copy(0.12f), Color.Transparent))))
                Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(12.dp), color = Color.White.copy(0.05f), border = BorderStroke(1.dp, typeColor.copy(0.3f))) {
                        Box(contentAlignment = Alignment.Center) { Text("☕", fontSize = 28.sp) }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AGENDA DESPEJADA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = typeColor, letterSpacing = 1.5.sp)
                        Text("No tienes compromisos próximos.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("¡Es un buen momento para planificar algo nuevo!", fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, content: @Composable () -> Unit) {
    Column {
        Text(text = label, color = Color.White.copy(0.3f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Surface(color = Color.White.copy(0.03f), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(0.06f)), modifier = Modifier.fillMaxWidth(), content = content)
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
private fun StatusBadgePremium(status: EstadoEvento) {
    val (color, text, icon) = when(status) {
        EstadoEvento.CONFIRMADO -> Triple(StatusOk, "CONFIRMADO", AppIcons.Check)
        EstadoEvento.SOLICITADO -> Triple(StatusWait, "PENDIENTE", AppIcons.Timer)
        EstadoEvento.CANCELADO, EstadoEvento.REPROGRAMADO -> Triple(ActionRed, "ANULADO", AppIcons.Close)
        EstadoEvento.COMPLETADO -> Triple(CyberNeon, "COMPLETADO", AppIcons.Check)
        else -> Triple(Color.Gray, status.name, AppIcons.Timer)
    }
    Surface(color = color.copy(0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(0.2f))) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

fun isSameDay(d1: Calendar, d2: Calendar) = d1.get(Calendar.YEAR) == d2.get(Calendar.YEAR) && d1.get(Calendar.MONTH) == d2.get(Calendar.MONTH) && d1.get(Calendar.DAY_OF_MONTH) == d2.get(Calendar.DAY_OF_MONTH)

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun EventCardsPreview() {
    ClienteTheme {
        val dummy = com.example.myapplication.core.dominio.modelos.EventoDominio(
            id = "1",
            titulo = "Mantenimiento Aire Acondicionado",
            descripcion = "Limpieza de filtros y carga de gas",
            fechaTexto = "Lunes, 28 de Julio",
            horaTexto = "14:30 hs",
            tipo = TipoEvento.VISITA_TECNICA,
            estado = EstadoEvento.CONFIRMADO,
            direccion = "Av. Santa Fe 1234, CABA",
            colorAcentoHex = 0xFF00E5FF,
            emojiTipo = "🧰",
            idParticipante = "p1",
            nombreParticipante = "Seba Climatización",
            marcaTiempoUtc = System.currentTimeMillis()
        )
        Column(modifier = Modifier.padding(10.dp).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NextCommitmentBanner(event = dummy, onClick = {}, onMessageClick = {})
            ModernEventCard(event = dummy, onClick = {}, onLongClick = {}, onMessageClick = {})
            HistoryEventItem(event = dummy.copy(estado = EstadoEvento.CANCELADO))
        }
    }
}

package com.example.myapplication.prestador.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.NotificacionesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onNavigateBack: () -> Unit,
    onAccion: (String) -> Unit = {},
    viewModel: NotificacionesViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val notificaciones by viewModel.notificaciones.collectAsStateWithLifecycle()
    val filtroTipo by viewModel.filtroTipo.collectAsStateWithLifecycle()
    val soloNoLeidas by viewModel.soloNoLeidas.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()

    val grupos = remember(notificaciones) { agruparPorFecha(notificaciones) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // ── HEADER ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(colors.primaryOrange, colors.primaryOrangeDark)
                    ),
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .padding(top = 48.dp, start = 8.dp, end = 16.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificaciones",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (unreadCount > 0) "$unreadCount sin leer" else "Todo al día ✓",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                if (unreadCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.marcarTodasLeidas() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.DoneAll,
                                    contentDescription = "Marcar todas leídas",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = if (unreadCount > 99) "99+" else "$unreadCount",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── FILTROS — Tipo ────────────────────────────────────────
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FiltroChip(
                    label = "🔔 Todos",
                    selected = filtroTipo == null,
                    onClick = { viewModel.setFiltroTipo(null) },
                    colors = colors
                )
            }
            items(TipoNotificacion.entries) { tipo ->
                FiltroChip(
                    label = "${tipo.emoji} ${tipo.label}",
                    selected = filtroTipo == tipo,
                    onClick = { viewModel.setFiltroTipo(if (filtroTipo == tipo) null else tipo) },
                    colors = colors
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── TOGGLE Solo no leídas ─────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(colors.surfaceColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(colors.primaryOrange, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Solo no leídas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }
            Switch(
                checked = soloNoLeidas,
                onCheckedChange = { viewModel.toggleSoloNoLeidas() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryOrange,
                    uncheckedTrackColor = colors.border
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── LISTA ─────────────────────────────────────────────────
        if (notificaciones.isEmpty()) {
            EmptyState(colors)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                grupos.forEach { (grupo, items) ->
                    item {
                        GrupoHeader(label = grupo, colors = colors)
                    }
                    items(items, key = { it.id }) { notif ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            NotificacionCard(
                                notif = notif,
                                onTap = {
                                    viewModel.marcarLeida(notif.id)
                                    notif.accionRoute?.let { onAccion(it) }
                                },
                                onEliminar = { viewModel.eliminar(notif.id) },
                                colors = colors
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── EMPTY STATE ───────────────────────────────────────────────────
@Composable
private fun EmptyState(colors: PrestadorColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                colors.primaryOrange.copy(alpha = 0.15f),
                                colors.primaryOrange.copy(alpha = 0.04f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(48.dp)
                )
            }
            Text(
                "Sin notificaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                "Estás al día con todo ✓",
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
    }
}

// ── HEADER DE GRUPO ───────────────────────────────────────────────
@Composable
private fun GrupoHeader(label: String, colors: PrestadorColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.width(8.dp))
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = colors.divider,
            thickness = 0.8.dp
        )
    }
}

// ── CHIP DE FILTRO ────────────────────────────────────────────────
@Composable
private fun FiltroChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: PrestadorColors
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) colors.primaryOrange else colors.surfaceColor,
        border = if (selected) null else BorderStroke(1.dp, colors.border),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else colors.textSecondary
        )
    }
}

// ── CARD DE NOTIFICACIÓN ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificacionCard(
    notif: NotificacionItem,
    onTap: () -> Unit,
    onEliminar: () -> Unit,
    colors: PrestadorColors
) {
    val accentColor = tipoColor(notif.tipo)

    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "dot"
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onEliminar(); true } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.error)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Eliminar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    ) {
        Surface(
            onClick = onTap,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (!notif.leida) colors.primaryOrange.copy(alpha = 0.05f) else colors.surfaceColor,
            border = BorderStroke(
                width = if (!notif.leida) 1.dp else 0.8.dp,
                color = if (!notif.leida) colors.primaryOrange.copy(alpha = 0.25f) else colors.border
            ),
            shadowElevation = if (!notif.leida) 2.dp else 0.dp
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Acento lateral izquierdo para no leídas
                if (!notif.leida) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                accentColor,
                                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                            )
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Ícono con fondo del color del tipo
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(notif.tipo.emoji, fontSize = 22.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                notif.titulo,
                                fontSize = 14.sp,
                                fontWeight = if (!notif.leida) FontWeight.Bold else FontWeight.SemiBold,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (!notif.leida) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .background(
                                            colors.primaryOrange.copy(alpha = dotAlpha),
                                            CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            notif.mensaje,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            lineHeight = 19.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        // Footer: badge tipo + tiempo relativo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = accentColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "${notif.tipo.emoji} ${notif.tipo.label}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                tiempoRelativo(notif.fechaMs),
                                fontSize = 11.sp,
                                color = colors.textSecondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── HELPERS ───────────────────────────────────────────────────────

private fun tipoColor(tipo: TipoNotificacion): Color = when (tipo) {
    TipoNotificacion.MENSAJE     -> Color(0xFF3B82F6)
    TipoNotificacion.CITA        -> Color(0xFF10B981)
    TipoNotificacion.PRESUPUESTO -> Color(0xFFF59E0B)
    TipoNotificacion.SOLICITUD   -> Color(0xFFF97316)
    TipoNotificacion.SISTEMA     -> Color(0xFF8B5CF6)
}

private fun tiempoRelativo(ms: Long): String {
    val diff    = System.currentTimeMillis() - ms
    val minutos = diff / 60_000
    val horas   = diff / 3_600_000
    val dias    = diff / 86_400_000
    return when {
        minutos < 1   -> "ahora"
        minutos < 60  -> "hace ${minutos}m"
        horas   < 24  -> "hace ${horas}h"
        dias    == 1L -> "ayer"
        dias    < 7   -> "hace ${dias} días"
        else          -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(ms))
    }
}

private fun agruparPorFecha(items: List<NotificacionItem>): List<Pair<String, List<NotificacionItem>>> {
    val ahora     = System.currentTimeMillis()
    val unDia     = 86_400_000L
    val unaSemana = 7 * unDia

    val hoy        = items.filter { ahora - it.fechaMs < unDia }
    val ayer       = items.filter { ahora - it.fechaMs in unDia until (2 * unDia) }
    val semana     = items.filter { ahora - it.fechaMs in (2 * unDia) until unaSemana }
    val anteriores = items.filter { ahora - it.fechaMs >= unaSemana }

    return buildList {
        if (hoy.isNotEmpty())        add("HOY"         to hoy)
        if (ayer.isNotEmpty())       add("AYER"        to ayer)
        if (semana.isNotEmpty())     add("ESTA SEMANA" to semana)
        if (anteriores.isNotEmpty()) add("ANTERIORES"  to anteriores)
    }
}


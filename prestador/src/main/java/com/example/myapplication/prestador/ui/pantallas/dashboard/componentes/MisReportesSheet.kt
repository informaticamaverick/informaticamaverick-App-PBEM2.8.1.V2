package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.datos.repositorios.MensajeSoporte
import com.example.myapplication.core.datos.repositorios.TicketSoporte
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.viewmodel.dashboard.MisReportesViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReportesSheet(
    onDismiss: () -> Unit,
    viewModel: MisReportesViewModel = hiltViewModel()
) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722), primaryOrangeDark = Color(0xFFF4511E), primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712), surfaceColor = Color(0xFF0F172A), surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC), textSecondary = Color(0xFF94A3B8), border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B), chipBackground = Color(0xFF1E293B), chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444), success = Color(0xFF10B981)
    )
    val accent = colors.primaryOrange
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tickets by viewModel.tickets.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textSecondary.copy(alpha = 0.3f))
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Forum, null, tint = accent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Mis reportes", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text("Seguí el estado de tus consultas", fontSize = 12.sp, color = colors.textSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                if (tickets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Inbox, null, tint = colors.textSecondary, modifier = Modifier.size(40.dp))
                            Text("Todavía no enviaste ningún reporte", fontSize = 14.sp, color = colors.textSecondary)
                        }
                    }
                } else {
                    tickets.forEach { ticket ->
                        TicketCard(ticket = ticket, accentColor = accent, viewModel = viewModel)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

data class EstadoInfo(val etiqueta: String, val color: Color)

fun estadoInfo(estado: String): EstadoInfo = when (estado) {
    "en_proceso" -> EstadoInfo("En proceso", Color(0xFF2563EB))
    "resuelto" -> EstadoInfo("Resuelto", Color(0xFF16A34A))
    else -> EstadoInfo("Abierto", Color(0xFFF59E0B))
}

fun formatearFecha(millis: Long): String {
    if (millis <= 0L) return ""
    return SimpleDateFormat("dd/MM HH:mm", Locale("es", "AR")).format(Date(millis))
}

@Composable
private fun TicketCard(ticket: TicketSoporte, accentColor: Color, viewModel: MisReportesViewModel) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722), primaryOrangeDark = Color(0xFFF4511E), primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712), surfaceColor = Color(0xFF0F172A), surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC), textSecondary = Color(0xFF94A3B8), border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B), chipBackground = Color(0xFF1E293B), chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444), success = Color(0xFF10B981)
    )
    var expandido by remember { mutableStateOf(false) }
    val estado = estadoInfo(ticket.estado)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.backgroundColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ticket.asunto.ifBlank { ticket.categoria }, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(formatearFecha(ticket.createdAt), fontSize = 11.sp, color = colors.textSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(estado.color.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(estado.etiqueta, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = estado.color)
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = colors.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (!expandido) {
                Spacer(Modifier.height(6.dp))
                Text(
                    ticket.mensaje,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(visible = expandido, enter = expandVertically(), exit = shrinkVertically()) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))
                    Spacer(Modifier.height(10.dp))
                    HiloBurbuja(
                        esTuya = true,
                        texto = ticket.mensaje,
                        fecha = formatearFecha(ticket.createdAt),
                        accentColor = accentColor
                    )
                    ticket.mensajes.forEach { msj ->
                        Spacer(Modifier.height(8.dp))
                        HiloBurbuja(
                            esTuya = msj.de != "soporte",
                            texto = msj.texto,
                            fecha = formatearFecha(msj.fecha),
                            accentColor = accentColor
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    if (ticket.estado == "resuelto") {
                        Text(
                            "Caso cerrado — el equipo dio esta consulta por resuelta.",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    } else {
                        RespuestaBox(ticketId = ticket.id, accentColor = accentColor, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun RespuestaBox(ticketId: String, accentColor: Color, viewModel: MisReportesViewModel) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722), primaryOrangeDark = Color(0xFFF4511E), primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712), surfaceColor = Color(0xFF0F172A), surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC), textSecondary = Color(0xFF94A3B8), border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B), chipBackground = Color(0xFF1E293B), chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444), success = Color(0xFF10B981)
    )
    var texto by remember(ticketId) { mutableStateOf("") }
    var enviando by remember(ticketId) { mutableStateOf(false) }
    var error by remember(ticketId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = texto,
            onValueChange = { if (it.length <= 500) texto = it },
            placeholder = { Text("Escribir una respuesta...", fontSize = 12.sp, color = colors.textSecondary) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.3f),
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = accentColor
            )
        )
        IconButton(
            onClick = {
                val texto2 = texto.trim()
                if (texto2.isEmpty() || enviando) return@IconButton
                enviando = true
                error = null
                scope.launch {
                    viewModel.responder(ticketId, texto2)
                        .onSuccess { texto = ""; enviando = false }
                        .onFailure { error = "No se pudo enviar, probá de nuevo"; enviando = false }
                }
            },
            enabled = !enviando && texto.isNotBlank(),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = if (texto.isNotBlank()) 1f else 0.3f))
        ) {
            if (enviando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
    if (error != null) {
        Spacer(Modifier.height(4.dp))
        Text(error ?: "", fontSize = 11.sp, color = Color(0xFFEF4444))
    }
}

@Composable
private fun HiloBurbuja(esTuya: Boolean, texto: String, fecha: String, accentColor: Color) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722), primaryOrangeDark = Color(0xFFF4511E), primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712), surfaceColor = Color(0xFF0F172A), surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC), textSecondary = Color(0xFF94A3B8), border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B), chipBackground = Color(0xFF1E293B), chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444), success = Color(0xFF10B981)
    )
    Column(
        horizontalAlignment = if (esTuya) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (esTuya) "Vos" else "Soporte",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (esTuya) colors.textSecondary else accentColor
        )
        Spacer(Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = if (esTuya) 12.dp else 2.dp, bottomEnd = if (esTuya) 2.dp else 12.dp))
                .background(if (esTuya) colors.textSecondary.copy(alpha = 0.1f) else accentColor.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(texto, fontSize = 13.sp, color = colors.textPrimary)
        }
        if (fecha.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(fecha, fontSize = 10.sp, color = colors.textSecondary.copy(alpha = 0.7f))
        }
    }
}

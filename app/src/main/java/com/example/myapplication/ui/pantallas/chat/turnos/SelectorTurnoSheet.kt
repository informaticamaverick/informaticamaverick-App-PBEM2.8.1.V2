package com.example.myapplication.ui.pantallas.chat.turnos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTurnoSheet(
    mensaje: MensajeEntity,
    alCerrar: () -> Unit,
    alConfirmar: (LocalDate, BloqueHorario, idRecurso: String) -> Unit,
    viewModel: UsuarioSelectorTurnoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(mensaje.id) {
        viewModel.inicializar(mensaje)
    }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = Color(0xFFF8FAFC), 
        modifier = Modifier.fillMaxSize(),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        SelectorTurnoContent(
            state = state,
            onFechaSelect = { viewModel.seleccionarFecha(it) },
            onBloqueSelect = { id, b -> viewModel.seleccionarBloque(id, b) },
            onConfirmar = alConfirmar
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectorTurnoContent(
    state: SelectorTurnoUiState,
    onFechaSelect: (LocalDate) -> Unit,
    onBloqueSelect: (String, BloqueHorario) -> Unit,
    onConfirmar: (LocalDate, BloqueHorario, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF3B82F6))
                    Spacer(Modifier.width(12.dp))
                    val hoyText = if (state.fechaSeleccionada == LocalDate.now()) "Hoy, " else ""
                    Text(
                        text = hoyText + state.fechaSeleccionada.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + state.fechaSeleccionada.dayOfMonth + " " + state.fechaSeleccionada.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "OPERATIVO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF166534)
                    )
                }
            }
        }

        if (state.esAgendaAbierta) {
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.fechasDisponibles) { fecha ->
                    val sel = fecha == state.fechaSeleccionada
                    FilterChip(
                        selected = sel,
                        onClick = { onFechaSelect(fecha) },
                        label = { Text(fecha.dayOfMonth.toString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF3B82F6),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(state.recursosDisponibles) { recurso ->
                RecursoSchedulingCard(
                    recurso = recurso,
                    bloqueSeleccionado = if (state.idRecursoSeleccionado == recurso.id) state.bloqueSeleccionado else null,
                    onBloqueSelect = { onBloqueSelect(recurso.id, it) }
                )
            }
        }

        Button(
            onClick = {
                state.bloqueSeleccionado?.let { 
                    onConfirmar(state.fechaSeleccionada, it, state.idRecursoSeleccionado!!) 
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 8.dp),
            enabled = state.puedeConfirmar,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Text("RESERVAR TURNO", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun RecursoSchedulingCard(
    recurso: RecursoConSlots,
    bloqueSeleccionado: BloqueHorario?,
    onBloqueSelect: (BloqueHorario) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(recurso.nombre, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text(recurso.especialidad.ifBlank { "Personal asignado" }, color = Color(0xFF3B82F6), fontSize = 13.sp)
                }
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Turnos: ${recurso.duracionMinutos} min",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(Modifier.height(16.dp))

            val rows = recurso.slots.chunked(3)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { slot ->
                        val sel = slot.horaTexto == bloqueSeleccionado?.horaTexto
                        val occupied = slot.estaOcupado
                        
                        Surface(
                            onClick = { if (!occupied) onBloqueSelect(slot) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (sel) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (sel) Color(0xFF3B82F6) else Color(0xFFBFDBFE)
                            ),
                            enabled = !occupied
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = slot.horaTexto,
                                    color = if (occupied) Color(0xFFCBD5E1) else Color(0xFF1D4ED8),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    textDecoration = if (occupied) TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

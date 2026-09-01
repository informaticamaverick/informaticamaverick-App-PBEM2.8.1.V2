package com.example.myapplication.prestador.ui.pantallas.chat.wizard.componentes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.EquipoTrabajoDominio
import com.example.myapplication.core.dominio.modelos.RecursoDominio
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario
import com.example.myapplication.prestador.viewmodel.chat.wizard.ModoAgendaTurno
import com.example.myapplication.uishared.estilos.SharedPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoConfiguracionTurno(
    modo: ModoAgendaTurno,
    recursos: List<RecursoDominio>,
    recursoSeleccionado: RecursoDominio?,
    onRecursoSelect: (RecursoDominio) -> Unit,
    equipo: List<EquipoTrabajoDominio>,
    personalAsignado: EquipoTrabajoDominio?,
    onPersonalSelect: (EquipoTrabajoDominio?) -> Unit,
    fechaTexto: String,
    onAbrirCalendario: () -> Unit,
    bloques: List<BloqueHorario>,
    estaCargando: Boolean,
    horaSeleccionada: String,
    onHoraSelect: (String) -> Unit,
    onCambiarModo: (ModoAgendaTurno) -> Unit,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit
) {
    val colorAcento = Color(0xFFA855F7)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SELECTOR DE MODO (TÁCTICO) ---
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = modo == ModoAgendaTurno.CERRADA,
                onClick = { onCambiarModo(ModoAgendaTurno.CERRADA) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("HORARIO CERRADO", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            SegmentedButton(
                selected = modo == ModoAgendaTurno.ABIERTA,
                onClick = { onCambiarModo(ModoAgendaTurno.ABIERTA) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("AGENDA ABIERTA", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // --- SECCIÓN: RECURSO ---
        SeccionWizard(titulo = "ESPACIO / RECURSO", icono = Icons.Default.MeetingRoom, colorAcento = colorAcento) {
            LazyRow(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recursos) { res ->
                    val esSeleccionado = recursoSeleccionado?.id == res.id
                    FilterChip(
                        selected = esSeleccionado,
                        onClick = { onRecursoSelect(res) },
                        label = { Text(res.nombre) },
                        leadingIcon = if (esSeleccionado) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) } } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorAcento.copy(alpha = 0.2f),
                            selectedLabelColor = colorAcento
                        )
                    )
                }
            }
        }

        // --- SECCIÓN: PERSONAL (OPCIONAL) ---
        SeccionWizard(titulo = "PERSONAL TÉCNICO (OPCIONAL)", icono = Icons.Default.Badge, colorAcento = colorAcento) {
            LazyRow(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FilterChip(
                        selected = personalAsignado == null,
                        onClick = { onPersonalSelect(null) },
                        label = { Text("Sin asignar") }
                    )
                }
                items(equipo) { per ->
                    val esSeleccionado = personalAsignado?.id == per.id
                    FilterChip(
                        selected = esSeleccionado,
                        onClick = { onPersonalSelect(per) },
                        label = { Text(per.nombre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF10B981)
                        )
                    )
                }
            }
        }

        // --- SECCIÓN DINÁMICA: TIEMPO ---
        AnimatedContent(targetState = modo, label = "modo_tiempo") { targetModo ->
            if (targetModo == ModoAgendaTurno.CERRADA) {
                ContenidoModoCerrado(
                    fechaTexto = fechaTexto,
                    onAbrirCalendario = onAbrirCalendario,
                    bloques = bloques,
                    estaCargando = estaCargando,
                    horaSeleccionada = horaSeleccionada,
                    onHoraSelect = onHoraSelect
                )
            } else {
                ContenidoModoAbierto()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ATRÁS", color = Color.White)
            }
            Button(
                onClick = onConfirmar,
                modifier = Modifier.weight(2f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento),
                enabled = if (modo == ModoAgendaTurno.CERRADA) horaSeleccionada.isNotBlank() else true
            ) {
                Text("ENVIAR PROPUESTA", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ContenidoModoCerrado(
    fechaTexto: String,
    onAbrirCalendario: () -> Unit,
    bloques: List<BloqueHorario>,
    estaCargando: Boolean,
    horaSeleccionada: String,
    onHoraSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Selector de Fecha
        Surface(
            onClick = onAbrirCalendario,
            shape = RoundedCornerShape(16.dp),
            color = SharedPalette.EliteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFFA855F7))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("DÍA SELECCIONADO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(fechaTexto, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // Selector de Hora
        Text("BLOQUE HORARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
        
        if (estaCargando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp))
        } else if (bloques.isEmpty()) {
            Text("Sin disponibilidad para este día.", color = Color.Red, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(20.dp))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.heightIn(max = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bloques) { bloque ->
                    val seleccionado = horaSeleccionada == bloque.horaTexto
                    val habilitado = !bloque.estaOcupado
                    Surface(
                        onClick = { if (habilitado) onHoraSelect(bloque.horaTexto) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (seleccionado) Color(0xFFA855F7) else if (!habilitado) Color.White.copy(alpha = 0.02f) else SharedPalette.EliteSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (seleccionado) Color(0xFFA855F7) else Color.White.copy(alpha = 0.08f)),
                        enabled = habilitado
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(bloque.horaTexto, color = if (seleccionado) Color.Black else if (!habilitado) Color.White.copy(alpha = 0.2f) else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContenidoModoAbierto() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("MODO: AGENDA ABIERTA", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF10B981))
            }
            Text(
                "Se enviarán al cliente todas tus franjas horarias disponibles para los próximos 7 días. El cliente podrá elegir el horario que más le convenga.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun PreviewPasoConfiguracionTurno() {
    PasoConfiguracionTurno(
        modo = ModoAgendaTurno.CERRADA,
        recursos = listOf(RecursoDominio(nombre = "Box Principal"), RecursoDominio(nombre = "Consultorio A")),
        recursoSeleccionado = RecursoDominio(nombre = "Box Principal"),
        onRecursoSelect = {},
        equipo = listOf(EquipoTrabajoDominio(nombre = "Carlos", apellido = "Gómez")),
        personalAsignado = null,
        onPersonalSelect = {},
        fechaTexto = "LUNES 14 DE AGOSTO",
        onAbrirCalendario = {},
        bloques = listOf(
            BloqueHorario("08:00", false, 0, 0),
            BloqueHorario("08:30", true, 0, 0),
            BloqueHorario("09:00", false, 0, 0)
        ),
        estaCargando = false,
        horaSeleccionada = "08:00",
        onHoraSelect = {},
        onCambiarModo = {},
        onVolver = {},
        onConfirmar = {}
    )
}

package com.example.myapplication.prestador.ui.pantallas.chat.wizard.componentes

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.EquipoTrabajoDominio
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario
import com.example.myapplication.prestador.viewmodel.chat.wizard.ModoAgendaTurno
import com.example.myapplication.uishared.estilos.SharedPalette
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoConfiguracionVisita(
    modo: ModoAgendaTurno,
    direccionesDestino: List<MensajeEntity>,
    direccionSeleccionada: MensajeEntity?,
    onDireccionSelect: (MensajeEntity) -> Unit,
    equipo: List<EquipoTrabajoDominio>,
    equipoSeleccionadoIds: Set<String>,
    onToggleTecnico: (String) -> Unit,
    presupuestos: List<PresupuestoResumenDominio>,
    presupuestoSeleccionado: PresupuestoResumenDominio?,
    onPresupuestoSelect: (PresupuestoResumenDominio?) -> Unit,
    fechaTexto: String,
    onAbrirCalendario: () -> Unit,
    bloques: List<BloqueHorario>,
    estaCargando: Boolean,
    horaSeleccionada: String,
    onHoraSelect: (String) -> Unit,
    costoEstimado: Double,
    onCambiarModo: (ModoAgendaTurno) -> Unit,
    onVolver: () -> Unit,
    onConfirmar: () -> Unit
) {
    val colorAcento = Color(0xFF00E5FF)
    var expandedDirecciones by remember { mutableStateOf(false) }
    var expandedPresupuestos by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SELECTOR MODO
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = modo == ModoAgendaTurno.CERRADA,
                onClick = { onCambiarModo(ModoAgendaTurno.CERRADA) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("VISITA FIJA", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
            SegmentedButton(
                selected = modo == ModoAgendaTurno.ABIERTA,
                onClick = { onCambiarModo(ModoAgendaTurno.ABIERTA) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("PROPONER AGENDA", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // DESTINO
        SeccionWizard(titulo = "UBICACIÓN DE DESTINO", icono = Icons.Default.Place, colorAcento = colorAcento) {
            Box(modifier = Modifier.clickable { if (direccionesDestino.size > 1) expandedDirecciones = true }.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = colorAcento, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(direccionSeleccionada?.direccionTexto ?: "Consultar al cliente", color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (costoEstimado > 0) {
                            Text("Traslado est.: $ ${String.format(Locale.getDefault(), "%.0f", costoEstimado)}", color = colorAcento, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    if (direccionesDestino.size > 1) Icon(Icons.Default.ExpandMore, null, tint = Color.Gray)
                }
                
                DropdownMenu(expanded = expandedDirecciones, onDismissRequest = { expandedDirecciones = false }, modifier = Modifier.background(SharedPalette.EliteSurface).border(1.dp, Color.White.copy(0.1f))) {
                    direccionesDestino.forEach { msg ->
                        DropdownMenuItem(
                            text = { Text(msg.direccionTexto ?: "Ubicación chat", color = Color.White) },
                            onClick = { onDireccionSelect(msg); expandedDirecciones = false }
                        )
                    }
                }
            }
        }

        // EQUIPO
        SeccionWizard(titulo = "EQUIPO TÉCNICO", icono = Icons.Default.Groups, colorAcento = colorAcento) {
            LazyRow(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(equipo) { emp ->
                    val sel = equipoSeleccionadoIds.contains(emp.id)
                    FilterChip(
                        selected = sel,
                        onClick = { onToggleTecnico(emp.id) },
                        label = { Text(emp.nombre) },
                        leadingIcon = if (sel) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) } } else null
                    )
                }
            }
        }

        // PRESUPUESTO
        SeccionWizard(titulo = "VINCULAR PRESUPUESTO (OPCIONAL)", icono = Icons.Default.Description, colorAcento = Color(0xFFFACC15)) {
            Box(modifier = Modifier.clickable { expandedPresupuestos = true }.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, null, tint = Color(0xFFFACC15))
                    Spacer(Modifier.width(12.dp))
                    Text(presupuestoSeleccionado?.tituloTrabajo ?: "Vincular presupuesto...", color = if (presupuestoSeleccionado != null) Color.White else Color.Gray, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ExpandMore, null, tint = Color.Gray)
                }
                DropdownMenu(expanded = expandedPresupuestos, onDismissRequest = { expandedPresupuestos = false }, modifier = Modifier.background(SharedPalette.EliteSurface)) {
                    DropdownMenuItem(text = { Text("Ninguno", color = Color.White) }, onClick = { onPresupuestoSelect(null); expandedPresupuestos = false })
                    presupuestos.forEach { pre ->
                        DropdownMenuItem(
                            text = { Text(pre.tituloTrabajo ?: "Presupuesto", color = Color.White) },
                            onClick = { onPresupuestoSelect(pre); expandedPresupuestos = false }
                        )
                    }
                }
            }
        }

        // TIEMPO
        AnimatedContent(targetState = modo, label = "modo_visita") { targetModo ->
            if (targetModo == ModoAgendaTurno.CERRADA) {
                ContenidoModoCerradoVisita(fechaTexto, onAbrirCalendario, bloques, estaCargando, horaSeleccionada, onHoraSelect)
            } else {
                ContenidoModoAbiertoVisita()
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onVolver, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("ATRÁS", color = Color.White)
            }
            Button(onClick = onConfirmar, modifier = Modifier.weight(2f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = colorAcento), enabled = if (modo == ModoAgendaTurno.CERRADA) horaSeleccionada.isNotBlank() else true) {
                Text("ENVIAR PROPUESTA", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

@Composable
private fun ContenidoModoCerradoVisita(
    fechaTexto: String,
    onAbrirCalendario: () -> Unit,
    bloques: List<BloqueHorario>,
    estaCargando: Boolean,
    horaSeleccionada: String,
    onHoraSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(onClick = onAbrirCalendario, shape = RoundedCornerShape(16.dp), color = SharedPalette.EliteSurface, border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF00E5FF))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("FECHA DE VISITA", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(fechaTexto, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
        
        if (estaCargando) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.heightIn(max = 240.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bloques) { bloque ->
                    val sel = horaSeleccionada == bloque.horaTexto
                    val ok = !bloque.estaOcupado
                    Surface(onClick = { if (ok) onHoraSelect(bloque.horaTexto) }, shape = RoundedCornerShape(12.dp), color = if (sel) Color(0xFF00E5FF) else if (!ok) Color.White.copy(0.02f) else SharedPalette.EliteSurface, border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) Color(0xFF00E5FF) else Color.White.copy(0.08f)), enabled = ok) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(bloque.horaTexto, color = if (sel) Color.Black else if (!ok) Color.White.copy(0.2f) else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContenidoModoAbiertoVisita() {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.05f)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.2f)), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("AGENDA DE CAMPO ABIERTA", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF00E5FF))
            Text("El cliente recibirá tus próximos huecos libres de 1 hora para elegir el momento de la visita.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun PreviewPasoConfiguracionVisita() {
    PasoConfiguracionVisita(
        modo = ModoAgendaTurno.CERRADA,
        direccionesDestino = emptyList(),
        direccionSeleccionada = null,
        onDireccionSelect = {},
        equipo = listOf(EquipoTrabajoDominio(nombre = "Pedro", apellido = "Técnico")),
        equipoSeleccionadoIds = setOf(),
        onToggleTecnico = {},
        presupuestos = emptyList(),
        presupuestoSeleccionado = null,
        onPresupuestoSelect = {},
        fechaTexto = "VIERNES 15 DE AGOSTO",
        onAbrirCalendario = {},
        bloques = listOf(BloqueHorario("09:00", false, 0, 0), BloqueHorario("10:00", true, 0, 0)),
        estaCargando = false,
        horaSeleccionada = "09:00",
        onHoraSelect = {},
        costoEstimado = 1250.0,
        onCambiarModo = {},
        onVolver = {},
        onConfirmar = {}
    )
}

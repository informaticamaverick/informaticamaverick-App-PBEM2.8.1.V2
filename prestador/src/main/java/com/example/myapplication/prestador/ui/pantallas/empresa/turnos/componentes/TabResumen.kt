package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.pantallas.empresa.componentes.MetricCard
import com.example.myapplication.prestador.ui.pantallas.empresa.componentes.SelectorFecha
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.empresa.turnos.TipoFiltroResumen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class SlotTurnoSimulado(
    val hora: String,
    val ocupado: Boolean
)

/**
 * --- PESTAÑA DE RESUMEN: GESTIÓN DE TURNOS (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TabResumen(
    ocupacion: Int,
    staffActivo: Int,
    recursosListos: Int,
    filtroActual: TipoFiltroResumen,
    idStaffExpandido: String?,
    fechaSeleccionada: LocalDate,
    inventario: List<InventarioActivoDominio>,
    onFiltroChange: (TipoFiltroResumen) -> Unit,
    onStaffClick: (String) -> Unit,
    onFechaChange: (LocalDate) -> Unit,
    onAbrirCalendario: () -> Unit,
    obtenerSlots: (InventarioActivoDominio) -> List<SlotTurnoSimulado>
) {
    val colors = getPrestadorColors()

    val itemsFiltrados = remember(inventario, filtroActual) {
        when(filtroActual) {
            TipoFiltroResumen.RECURSOS -> inventario.filter { it.tipo == TipoActivo.RECURSO }
            TipoFiltroResumen.STAFF -> inventario.filter { it.tipo == TipoActivo.EQUIPO }
            TipoFiltroResumen.OCUPACION -> inventario.filter { obtenerSlots(it).any { s -> s.ocupado } }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SelectorFecha(
                seleccionada = fechaSeleccionada,
                onFechaSelect = onFechaChange,
                onCalendarioClick = onAbrirCalendario
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "OCUPACIÓN",
                    value = "$ocupacion%",
                    icon = Icons.Default.Percent,
                    color = Color(0xFF10B981),
                    seleccionado = filtroActual == TipoFiltroResumen.OCUPACION,
                    onClick = { onFiltroChange(TipoFiltroResumen.OCUPACION) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "STAFF",
                    value = staffActivo.toString(),
                    icon = Icons.Default.Group,
                    color = Color(0xFF3B82F6),
                    seleccionado = filtroActual == TipoFiltroResumen.STAFF,
                    onClick = { onFiltroChange(TipoFiltroResumen.STAFF) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "RECURSOS",
                    value = recursosListos.toString(),
                    icon = Icons.Default.MeetingRoom,
                    color = colors.primaryOrange,
                    seleccionado = filtroActual == TipoFiltroResumen.RECURSOS,
                    onClick = { onFiltroChange(TipoFiltroResumen.RECURSOS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val esHoy = fechaSeleccionada.isEqual(LocalDate.now())
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (esHoy) Icons.Outlined.Info else Icons.Default.Event,
                        null,
                        tint = colors.primaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (esHoy) "Agenda en vivo para hoy. Sincronización en tiempo real."
                              else "Agenda para el ${fechaSeleccionada.dayOfMonth} de ${fechaSeleccionada.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            val etiqueta = when(filtroActual) {
                TipoFiltroResumen.OCUPACION -> "ACTIVOS CON ACTIVIDAD"
                TipoFiltroResumen.STAFF -> "EQUIPO DE TRABAJO"
                TipoFiltroResumen.RECURSOS -> "RECURSOS REGISTRADOS"
            }
            Text(
                etiqueta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = colors.textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        items(itemsFiltrados, key = { it.id }) { activo ->
            val staffVinculado = if (activo.tipo == TipoActivo.RECURSO) {
                inventario.filter { it.tipo == TipoActivo.EQUIPO && it.idRecursoVinculado == activo.id }
            } else emptyList()

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                EstadoActivoCard(
                    activo = activo, 
                    slots = obtenerSlots(activo),
                    esHoy = fechaSeleccionada.isEqual(LocalDate.now()),
                    staffVinculado = staffVinculado,
                    estaExpandido = idStaffExpandido == activo.id,
                    onExpandClick = { onStaffClick(activo.id) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EstadoActivoCard(
    activo: InventarioActivoDominio,
    slots: List<SlotTurnoSimulado>,
    esHoy: Boolean = true,
    staffVinculado: List<InventarioActivoDominio> = emptyList(),
    estaExpandido: Boolean = false,
    onExpandClick: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    val ahoraStr = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    val slotActual = if (esHoy) slots.find { it.hora <= ahoraStr } ?: slots.firstOrNull() else null
    val estaOcupado = slotActual?.ocupado ?: false
    val estaHabilitado = activo.habilitado
    val tieneHorario = slots.isNotEmpty()

    val colorEstado = if (!estaHabilitado) Color.Gray 
                     else if (!tieneHorario) Color.LightGray
                     else if (esHoy && estaOcupado) Color.Red 
                     else Color(0xFF10B981)

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        onClick = if (activo.tipo == TipoActivo.EQUIPO) onExpandClick else ({})
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(colorEstado, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(activo.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                }
                
                Surface(color = colorEstado.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (!estaHabilitado) "OFF" 
                              else if (!tieneHorario) "SIN TURNO"
                              else if (esHoy && estaOcupado) "OCUPADO" 
                              else if (esHoy) "LIBRE"
                              else "DISPONIBLE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = colorEstado
                    )
                }
            }
            
            Text(
                text = if (activo.tipo == TipoActivo.EQUIPO) "Cargo: ${activo.subTitulo}" else "Categoría: ${activo.subTitulo}",
                fontSize = 11.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(start = 18.dp, top = 2.dp)
            )

            if (activo.tipo == TipoActivo.RECURSO && staffVinculado.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.padding(start = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Asignado: ${staffVinculado.joinToString { it.nombre }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (slots.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(colors.border.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                } else {
                    slots.take(12).forEach { slot ->
                        Box(
                            modifier = Modifier.weight(1f).height(4.dp).background(
                                if (slot.ocupado) Color.Red.copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                        )
                    }
                }
            }

            if (activo.tipo == TipoActivo.EQUIPO && estaExpandido) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                Text("BLOQUES OCUPADOS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = colors.textSecondary)
                Spacer(Modifier.height(8.dp))
                
                val slotsOcupados = slots.filter { it.ocupado }
                if (slotsOcupados.isEmpty()) {
                    Text("Sin turnos tomados para esta fecha", fontSize = 11.sp, color = Color(0xFF10B981))
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slotsOcupados.forEach { slot ->
                            Surface(
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = slot.hora,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme
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
    val colors = GestionTurnosTheme

    val itemsFiltrados = remember(inventario, filtroActual) {
        when(filtroActual) {
            TipoFiltroResumen.RECURSOS -> inventario.filter { it.tipo == TipoActivo.RECURSO }
            TipoFiltroResumen.STAFF -> inventario.filter { it.tipo == TipoActivo.EQUIPO }
            TipoFiltroResumen.OCUPACION -> inventario.filter { obtenerSlots(it).any { s -> s.ocupado } }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.DarkBg),
        contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    label = "OCUPACIÓN",
                    value = "$ocupacion%",
                    icon = Icons.Default.Percent,
                    color = colors.AccentEmerald,
                    seleccionado = filtroActual == TipoFiltroResumen.OCUPACION,
                    onClick = { onFiltroChange(TipoFiltroResumen.OCUPACION) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "STAFF",
                    value = staffActivo.toString(),
                    icon = Icons.Default.Group,
                    color = colors.AccentViolet,
                    seleccionado = filtroActual == TipoFiltroResumen.STAFF,
                    onClick = { onFiltroChange(TipoFiltroResumen.STAFF) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "RECURSOS",
                    value = recursosListos.toString(),
                    icon = Icons.Default.MeetingRoom,
                    color = colors.BrandOrange,
                    seleccionado = filtroActual == TipoFiltroResumen.RECURSOS,
                    onClick = { onFiltroChange(TipoFiltroResumen.RECURSOS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val esHoy = fechaSeleccionada.isEqual(LocalDate.now())
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (esHoy) Icons.Outlined.Info else Icons.Default.Event,
                    null,
                    tint = colors.BrandOrange,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (esHoy) "Agenda en vivo para hoy. Sincronización en tiempo real."
                          else "Agenda para el ${fechaSeleccionada.dayOfMonth} de ${fechaSeleccionada.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}.",
                    fontSize = 11.sp,
                    color = colors.TextSecondary,
                    lineHeight = 16.sp
                )
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
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = colors.TextMuted,
                letterSpacing = 0.8.sp,
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
    val colors = GestionTurnosTheme
    val ahoraStr = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    val slotActual = if (esHoy) slots.find { it.hora <= ahoraStr } ?: slots.firstOrNull() else null
    val estaOcupado = slotActual?.ocupado ?: false
    val estaHabilitado = activo.habilitado
    val tieneHorario = slots.isNotEmpty()

    val colorEstado = if (!estaHabilitado) colors.TextMuted
                     else if (!tieneHorario) colors.TextMuted.copy(alpha = 0.6f)
                     else if (esHoy && estaOcupado) colors.AccentRose
                     else colors.AccentEmerald

    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(10.dp),
        color = colors.CardBg,
        border = BorderStroke(1.dp, colors.BorderGlass),
        onClick = if (activo.tipo == TipoActivo.EQUIPO) onExpandClick else ({})
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(colorEstado, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(9.dp))
                    Text(activo.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.TextPrimary)
                }

                Surface(color = colorEstado.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
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
                fontSize = 10.sp,
                color = colors.TextSecondary,
                modifier = Modifier.padding(start = 17.dp, top = 5.dp)
            )

            if (activo.tipo == TipoActivo.RECURSO && staffVinculado.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.padding(start = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = colors.AccentViolet, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Asignado: ${staffVinculado.joinToString { it.nombre }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.AccentViolet
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (slots.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(colors.BorderGlass, RoundedCornerShape(3.dp)))
                } else {
                    slots.take(12).forEach { slot ->
                        Box(
                            modifier = Modifier.weight(1f).height(5.dp).background(
                                if (slot.ocupado) colors.AccentRose.copy(alpha = 0.6f) else colors.AccentEmerald.copy(alpha = 0.3f),
                                RoundedCornerShape(3.dp)
                            )
                        )
                    }
                }
            }

            if (activo.tipo == TipoActivo.EQUIPO && estaExpandido) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = colors.BorderGlass)
                Spacer(Modifier.height(12.dp))
                Text("BLOQUES OCUPADOS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = colors.TextMuted, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(8.dp))

                val slotsOcupados = slots.filter { it.ocupado }
                if (slotsOcupados.isEmpty()) {
                    Text("Sin turnos tomados para esta fecha", fontSize = 11.sp, color = colors.AccentEmerald)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slotsOcupados.forEach { slot ->
                            Surface(
                                color = colors.AccentRose.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, colors.AccentRose.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = slot.hora,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.AccentRose
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

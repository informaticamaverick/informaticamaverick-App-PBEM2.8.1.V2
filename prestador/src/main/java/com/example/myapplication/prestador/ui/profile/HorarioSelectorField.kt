package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

private val DIAS = listOf("Lun", "Mar", "M" + '\u00ED' + "e", "Jue", "Vie", "S" + '\u00E1' + "b", "Dom")
private val DIAS_INICIAL = listOf("L", "M", "X", "J", "V", "S", "D")

private data class HorarioParsed(
    val dias: Set<String> = emptySet(),
    val desde: String = "08:00",
    val hasta: String = "18:00"
)

private fun parseHorario(s: String): HorarioParsed {
    if (s.isBlank()) return HorarioParsed()
    return try {
        val parts = s.trim().split(" ")
        val dias = parts[0].split(",").map { it.trim() }.toSet()
        val times = if (parts.size > 1) parts[1].split("-") else listOf("08:00", "18:00")
        HorarioParsed(
            dias = dias,
            desde = times.getOrElse(0) { "08:00" },
            hasta = times.getOrElse(1) { "18:00" }
        )
    } catch (e: Exception) { HorarioParsed() }
}

private fun serializeHorario(h: HorarioParsed): String {
    if (h.dias.isEmpty()) return ""
    val diasOrdenados = DIAS.filter { it in h.dias }.joinToString(",")
    return "$diasOrdenados ${h.desde}-${h.hasta}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTimePickerDialog(
    titulo: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val colors = getPrestadorColors()
    val parts = initialTime.split(":").mapNotNull { it.toIntOrNull() }
    val state = rememberTimePickerState(
        initialHour = parts.getOrElse(0) { 8 },
        initialMinute = parts.getOrElse(1) { 0 },
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceColor,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colors.primaryOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                HorizontalDivider(color = colors.primaryOrange.copy(alpha = 0.3f))

                // TimePicker con colores de la app
                CompositionLocalProvider(
                    LocalContentColor provides colors.textPrimary
                ) {
                    TimePicker(
                        state = state,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = colors.backgroundColor,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = colors.textPrimary,
                            selectorColor = colors.primaryOrange,
                            containerColor = colors.surfaceColor,
                            timeSelectorSelectedContainerColor = colors.primaryOrange,
                            timeSelectorUnselectedContainerColor = colors.primaryOrange.copy(alpha = 0.1f),
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = colors.textPrimary
                        )
                    )
                }

                HorizontalDivider(color = colors.primaryOrange.copy(alpha = 0.3f))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", color = colors.textSecondary)
                    }
                    Button(
                        onClick = {
                            onConfirm("%02d:%02d".format(state.hour, state.minute))
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioSelectorField(
    horario: String,
    onHorarioChange: (String) -> Unit,
    label: String = "Horario de atenci" + '\u00F3' + "n",
    modifier: Modifier = Modifier
) {
    val colors = getPrestadorColors()
    var parsed by remember(horario) { mutableStateOf(parseHorario(horario)) }
    var showPickerDesde by remember { mutableStateOf(false) }
    var showPickerHasta by remember { mutableStateOf(false) }

    fun update(nuevo: HorarioParsed) {
        parsed = nuevo
        onHorarioChange(serializeHorario(nuevo))
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // Label
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Schedule, contentDescription = null,
                tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)
        }

        // D" + 'i' + "as — chips compactos
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            DIAS.forEachIndexed { index, dia ->
                val seleccionado = dia in parsed.dias
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            if (seleccionado) colors.primaryOrange
                            else colors.primaryOrange.copy(alpha = 0.08f)
                        )
                        .clickable {
                            val nuevos = if (seleccionado) parsed.dias - dia else parsed.dias + dia
                            update(parsed.copy(dias = nuevos))
                        }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DIAS_INICIAL[index],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (seleccionado) Color.White else colors.primaryOrange
                    )
                }
            }
        }

        // Desde / Hasta
        if (parsed.dias.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showPickerDesde = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null,
                        tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Desde  ${parsed.desde}", fontSize = 12.sp, color = colors.textPrimary)
                }

                Text("—", color = colors.textSecondary, fontSize = 14.sp)

                OutlinedButton(
                    onClick = { showPickerHasta = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null,
                        tint = colors.primaryOrange, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hasta  ${parsed.hasta}", fontSize = 12.sp, color = colors.textPrimary)
                }
            }

            // Resumen
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colors.primaryOrange.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = serializeHorario(parsed),
                    fontSize = 12.sp,
                    color = colors.primaryOrange,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }

    if (showPickerDesde) {
        AppTimePickerDialog(
            titulo = "Horario de inicio",
            initialTime = parsed.desde,
            onDismiss = { showPickerDesde = false },
            onConfirm = { t -> update(parsed.copy(desde = t)); showPickerDesde = false }
        )
    }

    if (showPickerHasta) {
        AppTimePickerDialog(
            titulo = "Horario de cierre",
            initialTime = parsed.hasta,
            onDismiss = { showPickerHasta = false },
            onConfirm = { t -> update(parsed.copy(hasta = t)); showPickerHasta = false }
        )
    }
}
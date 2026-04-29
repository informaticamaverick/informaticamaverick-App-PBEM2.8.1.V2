package com.example.myapplication.prestador.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.toDayName
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.AvailabilityViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendCalendarDialog(
    providerId: String,
    onDismiss: () -> Unit,
    onSend: (startDate: String, endDate: String, availabilityJson: String, bookedSlotsJson: String) -> Unit,
    availabilityViewModel: AvailabilityViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val schedules by availabilityViewModel.schedules.collectAsState()

    var showRangePicker by remember { mutableStateOf(false) }
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val rangePicker = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= today
        }
    )

    val startDate = rangePicker.selectedStartDateMillis?.let { sdf.format(Date(it)) } ?: ""
    val endDate   = rangePicker.selectedEndDateMillis?.let   { sdf.format(Date(it)) } ?: ""

    val rangeColors = DatePickerDefaults.colors(
        selectedDayContainerColor    = colors.primaryOrange,
        selectedDayContentColor      = Color.White,
        dayInSelectionRangeContainerColor = colors.primaryOrange.copy(alpha = 0.15f),
        dayInSelectionRangeContentColor   = colors.primaryOrange,
        todayDateBorderColor         = colors.primaryOrange,
        todayContentColor            = colors.primaryOrange,
        headlineContentColor         = colors.primaryOrange,
        selectedYearContainerColor   = colors.primaryOrange,
        currentYearContentColor      = colors.primaryOrange
    )

    if (showRangePicker) {
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { showRangePicker = false },
                    enabled = rangePicker.selectedStartDateMillis != null && rangePicker.selectedEndDateMillis != null
                ) { Text("Aceptar", color = colors.primaryOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        ) {
            DateRangePicker(
                state = rangePicker,
                colors = rangeColors,
                showModeToggle = false,
                title = {
                    Text(
                        text = "Seleccioná el rango de fechas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                    )
                },
                headline = {
                    val start = rangePicker.selectedStartDateMillis?.let { sdf.format(Date(it)) } ?: "—"
                    val end   = rangePicker.selectedEndDateMillis?.let   { sdf.format(Date(it)) } ?: "—"
                    Text(
                        text = "$start  →  $end",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryOrange,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                },
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }


    fun buildAvailabilityJson(
        list: List<AvailabilityScheduleEntity>,
        startMillis: Long,
        endMillis: Long
    ): String {
        val scheduleByDay = list.associateBy { it.dayOfWeek }
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val array = JSONArray()

        val cursor = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }

        while (!cursor.after(endCal)) {
            // Calendar
            val entityDay = (cursor.get(Calendar.DAY_OF_WEEK) +5) %7 + 1
            scheduleByDay[entityDay]?.let { s ->
                val obj = JSONObject()
                obj.put("date", dateSdf.format(cursor.time))
                obj.put("dayOfWeek", s.dayOfWeek)
                obj.put("startTime", s.startTime)
                obj.put("endTime", s.endTime)
                obj.put("durationMinutes", s.appointmentDuration)
                array.put(obj)
            }
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        return array.toString()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enviar disponibilidad",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                if (schedules.isEmpty()) {
                    Text(
                        text = "No tenés horarios activos configurados. Andá a Disponibilidad para agregar horarios.",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                } else {
                    Text(
                        text = "Horarios activos:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(schedules.sortedBy { it.dayOfWeek }) { schedule ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.primaryOrange.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = schedule.dayOfWeek.toDayName(),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${schedule.startTime}–${schedule.endTime}",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${schedule.appointmentDuration} min",
                                        fontSize = 11.sp,
                                        color = colors.primaryOrange
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Rango de fechas a compartir:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { showRangePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                    border = BorderStroke(
                        1.dp,
                        if (startDate.isNotBlank()) colors.primaryOrange
                        else colors.textSecondary.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (startDate.isBlank()) "Seleccionar rango"
                               else "$startDate  →  $endDate",
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startMs = rangePicker.selectedStartDateMillis ?: return@Button
                    val endMs   = rangePicker.selectedEndDateMillis   ?: return@Button
                    onSend(
                        startDate,
                        endDate,
                        buildAvailabilityJson(schedules, startMs, endMs),
                        "[]"
                    )
                },
                enabled = schedules.isNotEmpty() && startDate.isNotBlank() && endDate.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
            ) {
                Text("Enviar calendario")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

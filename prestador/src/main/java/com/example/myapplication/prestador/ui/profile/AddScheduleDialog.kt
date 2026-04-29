package com.example.myapplication.prestador.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.toDayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    schedule: AvailabilityScheduleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (days: List<Int>, startTime: String, endTime: String, duration: Int, worksByAppointment: Boolean) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    var selectedDays by remember { mutableStateOf(setOf(schedule?.dayOfWeek ?: 1)) }
    var worksByAppointment by remember { mutableStateOf(schedule?.worksByAppointment ?: true) }

    var customDurationText by remember {
        mutableStateOf(schedule?.appointmentDuration?.takeIf { it > 0 }?.toString() ?: "")
    }

    val startInitialHour = schedule?.startTime?.split(":")?.get(0)?.toIntOrNull() ?: 9
    val startInitialMinute = schedule?.startTime?.split(":")?.get(1)?.toIntOrNull() ?: 0
    val endInitialHour = schedule?.endTime?.split(":")?.get(0)?.toIntOrNull() ?: 18
    val endInitialMinute = schedule?.endTime?.split(":")?.get(1)?.toIntOrNull() ?: 0

    val startTimeState = rememberTimePickerState(
        initialHour = startInitialHour,
        initialMinute = startInitialMinute,
        is24Hour = true
    )
    val endTimeState = rememberTimePickerState(
        initialHour = endInitialHour,
        initialMinute = endInitialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.backgroundColor,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (schedule != null) "Editar Disponibilidad" else "Nueva Disponibilidad",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Dia de la semana", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..7).chunked(4).forEach { row ->
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { day ->
                                DayChip(
                                    day = day,
                                    isSelected = selectedDays.contains(day),
                                    onClick = {
                                        selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                                    },
                                    colors = colors
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Franja horaria", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Desde", fontSize = 12.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TimeInput(
                            state = startTimeState,
                            colors = TimePickerDefaults.colors(
                                timeSelectorSelectedContainerColor = colors.primaryOrange,
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContainerColor = colors.textSecondary.copy(alpha = 0.1f),
                                timeSelectorUnselectedContentColor = colors.textPrimary
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Hasta", fontSize = 12.sp, color = colors.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        TimeInput(
                            state = endTimeState,
                            colors = TimePickerDefaults.colors(
                                timeSelectorSelectedContainerColor = colors.primaryOrange,
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContainerColor = colors.textSecondary.copy(alpha = 0.1f),
                                timeSelectorUnselectedContentColor = colors.textPrimary
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = colors.textSecondary.copy(alpha = 0.07f), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Trabajas con turnos?", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                            Text(
                                text = if (worksByAppointment) "Activo - turnos con duracion fija" else "Desactivado - atencion libre",
                                fontSize = 12.sp, color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = worksByAppointment,
                            onCheckedChange = { worksByAppointment = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primaryOrange)
                        )
                    }
                }
                AnimatedVisibility(visible = worksByAppointment) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "Duracion de cada turno", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customDurationText,
                                onValueChange = { value ->
                                    val filtered = value.filter { it.isDigit() }.take(3)
                                    customDurationText = filtered
                                },
                                placeholder = { Text("0", color = colors.textSecondary) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(90.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.4f)
                                )
                            )
                            Text("minutos", fontSize = 14.sp, color = colors.textSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val startTime = "${startTimeState.hour.toString().padStart(2,'0')}:${startTimeState.minute.toString().padStart(2,'0')}"
                            val endTime = "${endTimeState.hour.toString().padStart(2,'0')}:${endTimeState.minute.toString().padStart(2,'0')}"
                            val duration = if (!worksByAppointment) 0
                                           else customDurationText.toIntOrNull() ?: 0
                            onConfirm(selectedDays.sorted(), startTime, endTime, duration, worksByAppointment)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) {
                        Text(if (schedule != null) "Guardar" else "Agregar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayChip(
    day: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val dayName = day.toDayName().take(3)
    val backgroundColor = if (isSelected) colors.primaryOrange else Color.Transparent
    val textColor = if (isSelected) Color.White else colors.textPrimary
    val borderColor = if (isSelected) colors.primaryOrange else colors.textSecondary.copy(alpha = 0.3f)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, borderColor, RoundedCornerShape(8.dp)).background(backgroundColor).clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Text(text = dayName, modifier = Modifier.padding(vertical = 8.dp), fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.toDayName

@Composable
fun AddScheduleDialog(
    schedule: AvailabilityScheduleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (days: List<Int>, startTime: String, endTime: String, duration: Int) -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    var selectedDays by remember { mutableStateOf(setOf(schedule?.dayOfWeek ?: 1)) }
    var startHour by remember { mutableStateOf(schedule?.startTime?.split(":")?.get(0) ?: "09") }
    var startMinute by remember { mutableStateOf(schedule?.startTime?.split(":")?.get(1) ?: "00") }
    var endHour by remember { mutableStateOf(schedule?.endTime?.split(":")?.get(0) ?: "18") }
    var endMinute by remember { mutableStateOf(schedule?.endTime?.split(":")?.get(1) ?: "00") }
    var selectedDuration by remember { mutableStateOf(schedule?.appointmentDuration ?: 30) }
    
    val durations = listOf(15, 30, 45, 60, 90, 120)
    val hours = (0..23).map { it.toString().padStart(2, '0') }
    val minutes = listOf("00", "15", "30", "45")
    
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
            ) {
                // Header
                Text(
                    text = if (schedule != null) "Editar Horario" else "Agregar Horario",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Day selector
                Text(
                    text = "Día de la semana",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..7).chunked(4).forEach { row ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { day ->
                                DayChip(
                                    day = day,
                                    isSelected = selectedDays.contains(day),
                                    onClick = {
                                        selectedDays = if (selectedDays.contains(day)) {
                                            selectedDays - day
                                        } else {
                                            selectedDays + day
                                        }
                                    },
                                    colors = colors
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Time pickers
                Text(
                    text = "Horario",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start time
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Desde",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimeDropdown(
                                value = startHour,
                                options = hours,
                                onValueChange = { startHour = it },
                                modifier = Modifier.weight(1f),
                                colors = colors
                            )
                            Text(":", fontSize = 20.sp, color = colors.textPrimary)
                            TimeDropdown(
                                value = startMinute,
                                options = minutes,
                                onValueChange = { startMinute = it },
                                modifier = Modifier.weight(1f),
                                colors = colors
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    // End time
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hasta",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TimeDropdown(
                                value = endHour,
                                options = hours,
                                onValueChange = { endHour = it },
                                modifier = Modifier.weight(1f),
                                colors = colors
                            )
                            Text(":", fontSize = 20.sp, color = colors.textPrimary)
                            TimeDropdown(
                                value = endMinute,
                                options = minutes,
                                onValueChange = { endMinute = it },
                                modifier = Modifier.weight(1f),
                                colors = colors
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Duration selector
                Text(
                    text = "Duración de cada turno",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    durations.forEach { duration ->
                        DurationChip(
                            duration = duration,
                            isSelected = selectedDuration == duration,
                            onClick = { selectedDuration = duration },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            val startTime = "$startHour:$startMinute"
                            val endTime = "$endHour:$endMinute"
                            onConfirm(selectedDays.sorted(), startTime, endTime, selectedDuration)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        )
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Text(
            text = dayName,
            modifier = Modifier.padding(vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun TimeDropdown(
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = colors.textSecondary.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(12.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DurationChip(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) colors.primaryOrange else Color.Transparent
    val textColor = if (isSelected) Color.White else colors.textPrimary
    val borderColor = if (isSelected) colors.primaryOrange else colors.textSecondary.copy(alpha = 0.3f)
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = duration.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "min",
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

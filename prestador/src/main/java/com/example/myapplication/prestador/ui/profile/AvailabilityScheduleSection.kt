package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.toDayName
import com.example.myapplication.prestador.data.local.entity.toDayAbbr
import com.example.myapplication.prestador.viewmodel.AvailabilityViewModel

@Composable
fun AvailabilityScheduleSection(
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    viewModel: AvailabilityViewModel = hiltViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<AvailabilityScheduleEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<List<AvailabilityScheduleEntity>?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Handle UI state
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AvailabilityViewModel.UiState.Success -> {
                showAddDialog = false
                scheduleToEdit = null
                showDeleteDialog = null
                successMessage = state.message
                showSuccessMessage = true
                viewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = false
            }
            is AvailabilityViewModel.UiState.Error -> {
                showErrorMessage = state.message
                viewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showErrorMessage = null
            }
            else -> {}
        }
    }
    
    Column {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Horarios de Atención",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configurá tu disponibilidad semanal",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
            
            OutlinedButton(
                onClick = {
                    scheduleToEdit = null
                    showAddDialog = true
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primaryOrange
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar")
            }
        }
        
        // Success/Error messages
        if (showSuccessMessage) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = successMessage,
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        showErrorMessage?.let { error ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color(0xFFFF5252).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Schedule list grouped by day
        if (schedules.isEmpty()) {
            // Empty state
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = colors.textSecondary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin horarios configurados",
                        color = colors.textSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agregá tus horarios de atención",
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            //Agrupa por franja hraria
            val schedulesGroupedByTime = schedules
                .groupBy { Triple(it.startTime, it.endTime, it.appointmentDuration) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                schedulesGroupedByTime.forEach { (_, groupSchedules) ->
                    ScheduleSumaryCard(
                        schedules = groupSchedules,
                        onEdit = {
                            scheduleToEdit = groupSchedules.first()
                            showAddDialog = true
                        },
                        onDelete = {
                            showDeleteDialog = groupSchedules
                        },
                        colors = colors
                    )
                }
            }
        }
    }
    
    // Add/Edit Dialog
    if (showAddDialog) {
        AddScheduleDialog(
            schedule = scheduleToEdit,
            onDismiss = {
                showAddDialog = false
                scheduleToEdit = null
            },
            onConfirm = { days, startTime, endTime, duration ->
                if (scheduleToEdit != null) {
                    viewModel.updateSchedule(
                        scheduleToEdit!!.copy(
                            dayOfWeek = days.first(),
                            startTime = startTime,
                            endTime = endTime,
                            appointmentDuration = duration
                        )
                    )
                } else {
                    days.forEach { day ->
                        viewModel.addSchedule(day, startTime, endTime, duration)
                    }
                }
            },
            colors = colors
        )
    }
    
    // Delete confirmation dialog
    showDeleteDialog?.let { group ->
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = null },
            title = { Text("Eliminar horario")},
            text = { Text("¿Estás seguro que querés eñiminar este horario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        group.forEach {viewModel.deleteSchedule(it.id) }
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ScheduleSumaryCard(
    schedules:
    List<AvailabilityScheduleEntity>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors
) {
    val dias = schedules.sortedBy {
        it.dayOfWeek
    }.joinToString(" · ") {
        it.dayOfWeek.toDayAbbr()
    }
    val first = schedules.first()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = colors.primaryOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dias,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryOrange
                )
                Text(
                    text = "${first.startTime} - ${first.endTime}  ·  Turnos ${first.appointmentDuration} min",
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    "Editar",
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

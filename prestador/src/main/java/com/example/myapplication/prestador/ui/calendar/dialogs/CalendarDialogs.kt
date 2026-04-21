package com.example.myapplication.prestador.ui.calendar.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.ui.calendar.Appointment
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.utils.ServiceTypeConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun CancelAppointmentDialog(
    serviceTypeConfig: com.example.myapplication.prestador.utils.ServiceTypeConfig,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = getPrestadorColors()
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceColor,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de alerta
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFEF4444)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título
                Text(
                    text = "¿${serviceTypeConfig.cancelAction}?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = "Esta acción eliminará la ${serviceTypeConfig.appointmentName} programada. ¿Estás seguro de que quieres continuar?",
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Confirmar
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Sí, ${serviceTypeConfig.cancelAction.lowercase()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Botón Volver
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.surfaceElevated,
                            contentColor = colors.textSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Volver atrás",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


// --- COLORES DEL TEMA (App Prestador) ---
val OrangePrimary = Color(0xFFFF6B35)  // Color principal naranja
val OrangeLight = Color(0xFFFF9F66)    // Naranja claro
val OrangeBackground = Color(0xFFFFF8F3)  // Fondo claro
val Gray800 = Color(0xFF1F2937)
val Gray500 = Color(0xFF6B7280)
val Gray400 = Color(0xFF9CA3AF)
val Green100 = Color(0xFFDCFCE7)
val Green600 = Color(0xFF16A34A)

// Componente de animación de éxito
@Composable
fun PropuestaEnviadaView(
    onDismiss: () -> Unit
) {
    val colors = getPrestadorColors()
    // Temporizador de 2 segundos
    LaunchedEffect(Unit) {
        delay(2000)
        onDismiss()
    }

    // Animación de rebote
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceColor.copy(alpha = 0.95f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Icono animado
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(80.dp)
                    .background(Green100, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Éxito",
                    tint = Green600,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Cita Reprogramada",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "La cita ha sido actualizada correctamente.\nVolviendo al calendario...",
                color = colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// Diálogo para reprogramar una cita
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String) -> Unit
) {
    val colors = getPrestadorColors()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Estados para fecha y hora
    var newDate by remember { mutableStateOf(appointment.date) }
    var newTime by remember { mutableStateOf(appointment.time) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // Estados para mostrar pickers personalizados
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Función para confirmar
    fun confirmReschedule() {
        isSaving = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            isSaving = false
            showSuccess = true
        }, 500)
    }
    
    // Solo mostrar Dialog si no está mostrando éxito
    if (!showSuccess) {
        // Dialog popup centrado
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reprogramar Cita",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Información del cliente
                Text(
                    text = "Cliente: ${appointment.clientName}",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = "Servicio: ${appointment.service}",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Input: Nueva Fecha
                Text(
                    text = "NUEVA FECHA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newDate,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, null, tint = OrangePrimary)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledContainerColor = colors.surfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "Seleccionado: ${newDate.split("-").reversed().joinToString("/")}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Input: Nueva Hora
                Text(
                    text = "NUEVA HORA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newTime,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    trailingIcon = {
                        Text("🕐", fontSize = 20.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE2E8F0),
                        disabledContainerColor = colors.surfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón Confirmar
                Button(
                    onClick = { confirmReschedule() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaving,
                    elevation = ButtonDefaults.buttonElevation(10.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Confirmar Cambio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    }
    
    // Picker de Fecha Personalizado
    if (showDatePicker && !showSuccess) {
        CustomDatePickerDialog(
            initialDate = newDate,
            onDateSelected = { selectedDate ->
                newDate = selectedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Picker de Hora Personalizado
    if (showTimePicker && !showSuccess) {
        CustomTimePickerDialog(
            initialTime = newTime,
            onTimeSelected = { selectedTime ->
                newTime = selectedTime
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
    
    // Animación de éxito (se muestra encima de todo)
    if (showSuccess) {
        PropuestaEnviadaView(
            onDismiss = {
                showSuccess = false
                onConfirm(newDate, newTime)
            }
        )
    }
}

// Picker de Fecha Personalizado con diseño moderno
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    initialDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = getPrestadorColors()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            val parts = initialDate.split("-")
            val calendar = Calendar.getInstance()
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH) + 1
                        val day = calendar.get(Calendar.DAY_OF_MONTH)
                        val dateStr = String.format("%04d-%02d-%02d", year, month, day)
                        onDateSelected(dateStr)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary
                )
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = colors.textSecondary)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = colors.surfaceColor,
            selectedDayContainerColor = OrangePrimary,
            todayDateBorderColor = OrangePrimary,
            todayContentColor = OrangePrimary
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = OrangePrimary,
                todayDateBorderColor = OrangePrimary,
                todayContentColor = OrangePrimary,
                selectedDayContentColor = Color.White
            )
        )
    }
}

// Picker de Hora Personalizado con diseño moderno
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = getPrestadorColors()
    val timeParts = initialTime.split(":")
    val initialHour = if (timeParts.isNotEmpty()) timeParts[0].toIntOrNull() ?: 9 else 9
    val initialMinute = if (timeParts.size > 1) timeParts[1].toIntOrNull() ?: 0 else 0
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar Hora",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = colors.surfaceElevated,
                        selectorColor = OrangePrimary,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = colors.textPrimary,
                        timeSelectorSelectedContainerColor = OrangePrimary,
                        timeSelectorUnselectedContainerColor = colors.surfaceElevated,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = colors.textPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = colors.textSecondary)
                    }
                    
                    Button(
                        onClick = {
                            val hour = timePickerState.hour.toString().padStart(2, '0')
                            val minute = timePickerState.minute.toString().padStart(2, '0')
                            onTimeSelected("$hour:$minute")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary
                        )
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}


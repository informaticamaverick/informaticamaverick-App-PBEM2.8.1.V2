package com.example.myapplication.prestador.ui.appointments

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.ConversationEntity
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Diálogo adaptativo para crear appointments según el tipo de servicio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentDialog(
    serviceType: ServiceType,
    onDismiss: () -> Unit,
    onConfirm: (
        clientName: String,
        service: String,
        date: String,
        time: String,
        duration: Int,
        rentalSpaceId: String?,
        scheduleId: String?,
        notes: String,
        assignedEmployeeIds: String?,
        peopleCount: Int?
    ) -> Unit,
    colors: PrestadorColors,
    availableSpaces: List<Pair<String, String>> = emptyList(),
    availableSlots: List<String> = emptyList(),
    isSlotsLoading: Boolean = false,
    onRequestSlots: ((date: String, duration: Int) -> Unit)? = null,
    slotsRequestKey: Any? = null,
    availableEmployees: List<Pair<String, String>> = emptyList(),
    initialClientName: String = "",
    initialService: String = "",
    initialDate: String = "",
    initialTime: String = "09:00",
    initialDuration: Int = 60,
    initialNotes: String = "",
    initialPeopleCount: Int = 1,
    isEditMode: Boolean = false,
    clienteSugeridos: List<ConversationEntity> = emptyList(),
    onBuscarClientes: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val config = getServiceTypeConfig(serviceType)

    var clientName by remember { mutableStateOf(initialClientName) }
    var selectedClientId by remember { mutableStateOf<String?>(null) }
    var showClientSelector by remember { mutableStateOf(false) }
    var clientSearchQuery by remember { mutableStateOf(initialClientName) }
    var service by remember { mutableStateOf(initialService) }

    // Estados para date/time pickers
    val calendar = Calendar.getInstance()
    if (initialDate.isNotEmpty()) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = sdf.parse(initialDate) ?: calendar.time
        } catch (_: Exception) { calendar.add(Calendar.DAY_OF_YEAR, 1) }
    } else {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    var selectedDateMillis by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedTime by remember {
        mutableStateOf(
            if (serviceType == ServiceType.PROFESSIONAL && !isEditMode) "" else initialTime
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Formatear fecha para mostrar y para enviar
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val selectedDate = dateFormatter.format(Date(selectedDateMillis))
    val displayDate = displayDateFormatter.format(Date(selectedDateMillis))

    var duration by remember { mutableStateOf(initialDuration) }
    var selectedSpaceId by remember { mutableStateOf<String?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf(initialNotes) }
    var selectedEmployeeIds by remember { mutableStateOf(setOf<String>()) }
    var peopleCount by remember { mutableStateOf(initialPeopleCount) }

    // Cargar/recargar turnos disponibles cuando cambia la fecha/duración o el providerId (via slotsRequestKey)
    LaunchedEffect(serviceType, selectedDate, duration, slotsRequestKey) {
        if (serviceType == ServiceType.PROFESSIONAL) {
            onRequestSlots?.invoke(selectedDate, duration)
        }
    }

    var lastProfessionalDate by remember { mutableStateOf(selectedDate) }
    LaunchedEffect(serviceType, selectedDate) {
        if (serviceType == ServiceType.PROFESSIONAL && selectedDate != lastProfessionalDate) {
            selectedSlot = null
            selectedTime = ""
            lastProfessionalDate = selectedDate
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceColor,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 12.dp,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header compacto con icono y tipo de servicio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = if (isEditMode) "Editar Cita" else config.createDialogTitle,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            // Badge del tipo de servicio compacto
                            Text(
                                text = when(serviceType) {
                                    ServiceType.TECHNICAL -> "Técnico"
                                    ServiceType.PROFESSIONAL -> "Profesional"
                                    ServiceType.RENTAL -> "Alquiler"
                                    ServiceType.OTHER -> "Otro"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when(serviceType) {
                                    ServiceType.TECHNICAL -> Color(0xFF6366F1)
                                    ServiceType.PROFESSIONAL -> Color(0xFF10B981)
                                    ServiceType.RENTAL -> Color(0xFFF59E0B)
                                    ServiceType.OTHER -> Color(0xFF8B5CF6)
                                },
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.textSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campos comunes en card compacta
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceElevated,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Selector de Cliente con búsqueda
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { 
                                    clientName = it
                                    showClientSelector = it.isNotEmpty()
                                    clientSearchQuery = it
                                },
                                placeholder = { Text("Buscar o ingresar cliente", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = colors.primaryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = if (selectedClientId != null) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Cliente seleccionado",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.border.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            // Dropdown con clientes sugeridos
                            if (showClientSelector && clientSearchQuery.isNotEmpty() && clienteSugeridos.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.surfaceColor,
                                    shadowElevation = 4.dp,
                                    tonalElevation = 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier.heightIn(max = 200.dp)
                                    ) {
                                        clienteSugeridos.take(5).forEach { cliente ->
                                            ClienteListItem(
                                                cliente = cliente,
                                                colors = colors,
                                                onClick = {
                                                    selectedClientId = cliente.userId
                                                    clientName = cliente.userName
                                                    showClientSelector = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Servicio
                        OutlinedTextField(
                            value = service,
                            onValueChange = { service = it },
                            placeholder = { Text(config.descriptionPlaceholder, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (serviceType == ServiceType.PROFESSIONAL) Icons.Default.Description else Icons.Default.Build,
                                    contentDescription = null,
                                    tint = colors.primaryOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primaryOrange,
                                unfocusedBorderColor = colors.border.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // UI específica según serviceType
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surfaceElevated,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (serviceType) {
                            ServiceType.TECHNICAL, ServiceType.OTHER -> {
                                // Fecha con picker
                                OutlinedTextField(
                                    value = displayDate,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Seleccionar fecha", fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primaryOrange,
                                        unfocusedBorderColor = colors.border.copy(alpha = 0.5f),
                                        disabledTextColor = colors.textPrimary,
                                        disabledBorderColor = colors.border.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    enabled = false
                                )
                                
                                // Hora con picker
                                OutlinedTextField(
                                    value = selectedTime,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Seleccionar hora", fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showTimePicker = true },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.primaryOrange,
                                        unfocusedBorderColor = colors.border.copy(alpha = 0.5f),
                                        disabledTextColor = colors.textPrimary,
                                        disabledBorderColor = colors.border.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    enabled = false
                                )

                                // Empleados que asistirán
                                if (availableEmployees.isNotEmpty()) {
                                    HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Group, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                                            Text("EQUIPO QUE ASISTIRÁ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 0.5.sp)
                                        }
                                        availableEmployees.forEach { (id, name) ->
                                            FilterChip(
                                                selected = selectedEmployeeIds.contains(id),
                                                onClick = {
                                                    selectedEmployeeIds = if (selectedEmployeeIds.contains(id)) {
                                                        selectedEmployeeIds - id
                                                    } else {
                                                        selectedEmployeeIds + id
                                                    }
                                                },
                                                label = { Text(name, fontWeight = FontWeight.Medium) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = colors.primaryOrange,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            
                            ServiceType.PROFESSIONAL -> {
                                // Fecha
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "FECHA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    OutlinedTextField(
                                        value = displayDate,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text("Seleccionar fecha", fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.CalendarToday, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showDatePicker = true },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.primaryOrange,
                                            disabledTextColor = colors.textPrimary,
                                            disabledBorderColor = colors.border.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = false
                                    )
                                }
                                
                                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                                
                                // Turnos disponibles
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "TURNOS DISPONIBLES",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    
                                    when {
                                        isSlotsLoading -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = colors.primaryOrange
                                                )
                                                Text(
                                                    text = "Cargando turnos...",
                                                    fontSize = 13.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                        availableSlots.isEmpty() -> {
                                            Text(
                                                text = "No hay turnos disponibles para esta fecha",
                                                fontSize = 13.sp,
                                                color = colors.textSecondary,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        else -> {
                                            val columns = 3
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                availableSlots.chunked(columns).forEach { rowSlots ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        for (i in 0 until columns) {
                                                            val slot = rowSlots.getOrNull(i)
                                                            if (slot != null) {
                                                                FilterChip(
                                                                    selected = selectedSlot == slot,
                                                                    onClick = { selectedSlot = slot; selectedTime = slot },
                                                                    label = {
                                                                        Text(
                                                                            text = slot,
                                                                            fontWeight = FontWeight.Medium,
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            textAlign = TextAlign.Center,
                                                                            maxLines = 1,
                                                                            softWrap = false
                                                                        )
                                                                    },
                                                                    modifier = Modifier.weight(1f),
                                                                    colors = FilterChipDefaults.filterChipColors(
                                                                        selectedContainerColor = colors.primaryOrange,
                                                                        selectedLabelColor = Color.White
                                                                    )
                                                                )
                                                            } else {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            ServiceType.RENTAL -> {
                                // Espacios disponibles
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Store,
                                            contentDescription = null,
                                            tint = colors.primaryOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "ESPACIO",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    
                                    if (availableSpaces.isEmpty()) {
                                        Text(
                                            text = "No hay espacios disponibles",
                                            fontSize = 13.sp,
                                            color = colors.textSecondary,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            availableSpaces.forEach { (id, name) ->
                                                FilterChip(
                                                    selected = selectedSpaceId == id,
                                                    onClick = { selectedSpaceId = id },
                                                    label = { Text(name, fontWeight = FontWeight.Medium) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = colors.primaryOrange,
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))

                                // Cantidad de personas
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Group, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                                        Text("CANTIDAD DE PERSONAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 0.5.sp)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        IconButton(onClick = { if (peopleCount > 1) peopleCount-- }) {
                                            Icon(Icons.Default.Remove, null, tint = colors.primaryOrange)
                                        }
                                        Text("$peopleCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                        IconButton(onClick = { peopleCount++ }) {
                                            Icon(Icons.Default.Add, null, tint = colors.primaryOrange)
                                        }
                                    }
                                }

                                HorizontalDivider(color = colors.border.copy(alpha = 0.3f))
                                
                                // Fecha
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = displayDate,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text("Seleccionar fecha", fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.CalendarToday, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showDatePicker = true },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.primaryOrange,
                                            disabledTextColor = colors.textPrimary,
                                            disabledBorderColor = colors.border.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = false
                                    )
                                }
                                
                                // Hora
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = selectedTime,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = { Text("Seleccionar hora", fontSize = 14.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.AccessTime, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showTimePicker = true },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colors.primaryOrange,
                                            disabledTextColor = colors.textPrimary,
                                            disabledBorderColor = colors.border.copy(alpha = 0.5f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Notas opcionales - compactas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Notas adicionales (opcional)", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        unfocusedBorderColor = colors.border.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Botones compactos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Button(
                        onClick = {
                            // Validar que todos los campos requeridos estén completos
                            if (clientName.isNotBlank() && service.isNotBlank() && selectedTime.isNotBlank()) {
                                // Debug: imprimir valores
                                println("CreateAppointment - Cliente: $clientName")
                                println("CreateAppointment - Servicio: $service")
                                println("CreateAppointment - Fecha: $selectedDate")
                                println("CreateAppointment - Hora: $selectedTime")
                                println("CreateAppointment - SpaceId: $selectedSpaceId")
                                
                                onConfirm(
                                    clientName,
                                    service,
                                    selectedDate,
                                    selectedTime,
                                    duration,
                                    selectedSpaceId,
                                    null,
                                    notes,
                                    if (selectedEmployeeIds.isEmpty()) null else selectedEmployeeIds.joinToString(","),
                                    if (serviceType == ServiceType.RENTAL) peopleCount else null
                                )
                                onDismiss() // Cerrar el diálogo después de confirmar
                            }
                        },
                        enabled = clientName.isNotBlank() && service.isNotBlank() && selectedTime.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditMode) "Guardar cambios" else config.createAction,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // DatePicker con diseño personalizado
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePicker = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar", color = colors.textSecondary, fontWeight = FontWeight.Medium)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = colors.surfaceColor
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = colors.surfaceColor,
                    titleContentColor = colors.textPrimary,
                    headlineContentColor = colors.textPrimary,
                    weekdayContentColor = colors.textSecondary,
                    subheadContentColor = colors.textSecondary,
                    navigationContentColor = colors.primaryOrange,
                    yearContentColor = colors.textPrimary,
                    currentYearContentColor = colors.primaryOrange,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = colors.primaryOrange,
                    dayContentColor = colors.textPrimary,
                    disabledDayContentColor = colors.textSecondary.copy(alpha = 0.4f),
                    selectedDayContainerColor = colors.primaryOrange,
                    selectedDayContentColor = Color.White,
                    todayContentColor = colors.primaryOrange,
                    todayDateBorderColor = colors.primaryOrange,
                    dayInSelectionRangeContentColor = colors.textPrimary,
                    dayInSelectionRangeContainerColor = colors.primaryOrange.copy(alpha = 0.2f)
                ),
                title = {
                    Text(
                        text = "Seleccionar fecha",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    }
    
    // TimePicker personalizado con Material3
    if (showTimePicker) {
        val currentTime = selectedTime.split(":").let { parts ->
            if (parts.size == 2) Pair(parts[0].toIntOrNull() ?: 9, parts[1].toIntOrNull() ?: 0)
            else Pair(9, 0)
        }
        
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.first,
            initialMinute = currentTime.second,
            is24Hour = true
        )
        
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceColor,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título
                    Text(
                        text = "Seleccionar hora",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    
                    // TimePicker
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = colors.surfaceElevated,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = colors.textPrimary,
                            selectorColor = colors.primaryOrange,
                            containerColor = colors.surfaceColor,
                            periodSelectorBorderColor = colors.border,
                            periodSelectorSelectedContainerColor = colors.primaryOrange,
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = colors.textSecondary,
                            timeSelectorSelectedContainerColor = colors.primaryOrange.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = colors.surfaceElevated,
                            timeSelectorSelectedContentColor = colors.primaryOrange,
                            timeSelectorUnselectedContentColor = colors.textPrimary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTimePicker = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", color = colors.textSecondary, fontWeight = FontWeight.Medium)
                        }
                        
                        Button(
                            onClick = {
                                selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aceptar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente para mostrar un cliente en la lista de sugerencias
 */
@Composable
fun ClienteListItem(
    cliente: ConversationEntity,
    colors: PrestadorColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar con iniciales
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = colors.primaryOrange.copy(alpha = 0.15f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val partes = cliente.userName.trim().split(" ")
                    val iniciales = if (partes.size >= 2)
                        "${partes[0].first()}${partes[1].first()}"
                    else
                        cliente.userName.take(2).uppercase()
                    Text(
                        text = iniciales,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryOrange
                    )
                }
            }

            // Información del cliente
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = cliente.userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (!cliente.jobDescription.isNullOrBlank()) {
                    Text(
                        text = cliente.jobDescription,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Ícono de selección
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    // Divider
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 12.dp),
        thickness = 0.5.dp,
        color = colors.border.copy(alpha = 0.3f)
    )
}

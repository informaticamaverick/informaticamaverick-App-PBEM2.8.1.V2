package com.example.myapplication.prestador.ui.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import com.example.myapplication.prestador.data.local.entity.BlockedDateEntity
import com.example.myapplication.prestador.data.local.entity.BlockedDateReason
import com.example.myapplication.prestador.data.local.entity.ScheduleType
import com.example.myapplication.prestador.data.local.entity.toDayAbbr
import com.example.myapplication.prestador.ui.profile.AddScheduleDialog
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.AvailabilityViewModel
import com.example.myapplication.prestador.viewmodel.BlockedDateViewModel
import com.example.myapplication.prestador.viewmodel.ProviderViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarioConfigScreen(
    onBack: () -> Unit,
    onGoToEditProfile: () -> Unit = {},
    providerViewModel: ProviderViewModel = hiltViewModel(),
    availabilityViewModel: AvailabilityViewModel = hiltViewModel(),
    blockedDateViewModel: BlockedDateViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val provider by providerViewModel.provider.collectAsState()
    val allSchedules by availabilityViewModel.schedules.collectAsState()
    val uiState by availabilityViewModel.uiState.collectAsState()
    val blockedUiState by blockedDateViewModel.uiState.collectAsState()
    val blockedDates by blockedDateViewModel.blockedDates.collectAsState()
    val holidays by blockedDateViewModel.argentineHolidays.collectAsState()

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (!uid.isNullOrBlank()) {
            providerViewModel.loadProvider(uid)
        }
    }

    val tieneEmpresa = provider?.tieneEmpresa ?: false
    val hasLocalFisico = provider?.hasPhysicalLocation ?: false

    val visitSchedules = allSchedules.filter {
        it.scheduleType == ScheduleType.TECHNICAL_VISIT.name
    }
    val localSchedules = allSchedules.filter {
        it.scheduleType == ScheduleType.LOCAL_APPOINTMENT.name
    }

    var showAddVisita by remember { mutableStateOf(false) }
    var editVisita by remember { mutableStateOf<AvailabilityScheduleEntity?>(null) }
    var deleteVisitaGroup by remember { mutableStateOf<List<AvailabilityScheduleEntity>?>(null) }

    var showAddLocal by remember { mutableStateOf(false) }
    var editLocal by remember { mutableStateOf<AvailabilityScheduleEntity?>(null) }
    var deleteLocalGroup by remember { mutableStateOf<List<AvailabilityScheduleEntity>?>(null) }

    var showSuccessMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val s = uiState) {
            is AvailabilityViewModel.UiState.Success -> {
                showAddVisita = false
                editVisita = null
                deleteVisitaGroup = null
                showAddLocal = false
                editLocal = null
                deleteLocalGroup = null
                showSuccessMessage = s.message
                availabilityViewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = ""
            }
            is AvailabilityViewModel.UiState.Error -> {
                showErrorMessage = s.message
                availabilityViewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showErrorMessage = ""
            }
            else -> Unit
        }
    }

    LaunchedEffect(blockedUiState) {
        when (val s = blockedUiState) {
            is BlockedDateViewModel.UiState.Success -> {
                showSuccessMessage = s.msg
                blockedDateViewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = ""
            }
            is BlockedDateViewModel.UiState.Error -> {
                showErrorMessage = s.msg
                blockedDateViewModel.resetState()
                kotlinx.coroutines.delay(3000)
                showErrorMessage = ""
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary)
            }
            Text(
                "Horarios de atención",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        HorizontalDivider(color = colors.divider)

        if (showSuccessMessage.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(showSuccessMessage, color = Color(0xFF4CAF50), fontSize = 13.sp)
                }
            }
        }
        if (showErrorMessage.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFFFF5252).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(showErrorMessage, color = Color(0xFFFF5252), fontSize = 13.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HorarioSection(
                title = "VISITAS TÉCNICAS",
                icon = Icons.Default.DirectionsCar,
                subtitle = "Días y horarios para visitas técnicas",
                schedules = visitSchedules,
                onAdd = { showAddVisita = true },
                onEdit = { group ->
                    editVisita = group.first()
                    showAddVisita = true
                },
                onDelete = { group -> deleteVisitaGroup = group },
                colors = colors
            )

            if (hasLocalFisico && !tieneEmpresa) {
                HorarioSection(
                    title = "TURNO EN LOCAL",
                    icon = Icons.Default.Store,
                    subtitle = "Días y horarios para turnos en tu local",
                    schedules = localSchedules,
                    onAdd = { showAddLocal = true },
                    onEdit = { group ->
                        editLocal = group.first()
                        showAddLocal = true
                    },
                    onDelete = { group -> deleteLocalGroup = group },
                    colors = colors
                )
            }

            if (tieneEmpresa) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primaryOrange.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Turnos en local", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text(
                                "Los horarios de tus sucursales se configuran en Editar Perfil → Sucursales",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                        TextButton(onClick = onGoToEditProfile) {
                            Text("Ir", color = colors.primaryOrange, fontSize = 13.sp)
                        }
                    }
                }
            }

            BlockedDatesSection(
                blockedDates = blockedDates,
                holidays = holidays,
                blockedDateViewModel = blockedDateViewModel,
                colors = colors
            )
        }
    }

    if (showAddVisita) {
        AddScheduleDialog(
            schedule = editVisita,
            hasPhysicalLocation = false,
            showAppointmentToggle = false,
            colors = colors,
            onDismiss = { showAddVisita = false; editVisita = null },
            onConfirm = { days, start, end, duration, _, _ ->
                if (editVisita != null) {
                    availabilityViewModel.updateSchedule(
                        editVisita!!.copy(
                            dayOfWeek = days.first(),
                            startTime = start,
                            endTime = end,
                            appointmentDuration = duration,
                            worksByAppointment = true,
                            scheduleType = ScheduleType.TECHNICAL_VISIT.name
                        )
                    )
                } else {
                    days.forEach { day ->
                        availabilityViewModel.addSchedule(
                            day,
                            start,
                            end,
                            duration,
                            true,
                            ScheduleType.TECHNICAL_VISIT.name
                        )
                    }
                }
            }
        )
    }

    if (showAddLocal) {
        AddScheduleDialog(
            schedule = editLocal,
            hasPhysicalLocation = false,
            colors = colors,
            onDismiss = { showAddLocal = false; editLocal = null },
            onConfirm = { days, start, end, duration, worksByAppointment, _ ->
                if (editLocal != null) {
                    availabilityViewModel.updateSchedule(
                        editLocal!!.copy(
                            dayOfWeek = days.first(),
                            startTime = start,
                            endTime = end,
                            appointmentDuration = duration,
                            worksByAppointment = worksByAppointment,
                            scheduleType = ScheduleType.LOCAL_APPOINTMENT.name
                        )
                    )
                } else {
                    days.forEach { day ->
                        availabilityViewModel.addSchedule(
                            day,
                            start,
                            end,
                            duration,
                            worksByAppointment,
                            ScheduleType.LOCAL_APPOINTMENT.name
                        )
                    }
                }
            }
        )
    }

    deleteVisitaGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { deleteVisitaGroup = null },
            containerColor = colors.surfaceColor,
            title = { Text("Eliminar horario", color = colors.textPrimary) },
            text = { Text("¿Estás seguro que querés eliminar este horario?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    group.forEach { availabilityViewModel.deleteSchedule(it.id) }
                    deleteVisitaGroup = null
                }) {
                    Text("Eliminar", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteVisitaGroup = null }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }

    deleteLocalGroup?.let { group ->
        AlertDialog(
            onDismissRequest = { deleteLocalGroup = null },
            containerColor = colors.surfaceColor,
            title = { Text("Eliminar horario", color = colors.textPrimary) },
            text = { Text("¿Estás seguro que querés eliminar este horario?", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    group.forEach { availabilityViewModel.deleteSchedule(it.id) }
                    deleteLocalGroup = null
                }) {
                    Text("Eliminar", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteLocalGroup = null }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockedDatesSection(
    blockedDates: List<BlockedDateEntity>,
    holidays: List<BlockedDateViewModel.HolidayItem>,
    blockedDateViewModel: BlockedDateViewModel,
    colors: PrestadorColors
) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val customDates = remember(blockedDates) {
        blockedDates.filter { it.reason == BlockedDateReason.CUSTOM.name }
    }

    var holidaysExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var customLabel by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colors.primaryOrange.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.EventBusy, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("DÍAS BLOQUEADOS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text("Fechas en que no atendés", fontSize = 11.sp, color = colors.textSecondary)
                }
            }

            HorizontalDivider(color = colors.divider)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { holidaysExpanded = !holidaysExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Feriados $currentYear",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = if (holidaysExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }

                AnimatedVisibility(
                    visible = holidaysExpanded,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200))
                ) {
                    if (holidays.isEmpty()) {
                        Text(
                            text = "No se pudieron cargar los feriados todavía.",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    } else {
                        val rows = holidays.chunked(2)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rows.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { holiday ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.primaryOrange.copy(alpha = 0.05f),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = formatBlockedDate(holiday.date, "dd/MM"),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.primaryOrange
                                                )
                                                Text(
                                                    text = holiday.label,
                                                    fontSize = 11.sp,
                                                    color = colors.textPrimary,
                                                    maxLines = 2,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Switch(
                                                    checked = blockedDateViewModel.isDateBlocked(holiday.date),
                                                    onCheckedChange = { blockedDateViewModel.toggleHoliday(holiday) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.White,
                                                        checkedTrackColor = colors.primaryOrange
                                                    ),
                                                    modifier = Modifier.height(24.dp)
                                                )
                                            }
                                        }
                                    }
                                    // Celda vacía si la fila tiene solo 1 elemento
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                } // AnimatedVisibility
            }

            HorizontalDivider(color = colors.divider)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Personalizados",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    OutlinedButton(
                        onClick = {
                            selectedDate = ""
                            customLabel = ""
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar", fontSize = 13.sp)
                    }
                }

                if (customDates.isEmpty()) {
                    Text(
                        text = "Todavía no agregaste días personalizados.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        customDates.forEach { blocked ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.primaryOrange.copy(alpha = 0.05f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formatBlockedDate(blocked.date, "dd/MM/yyyy"),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primaryOrange
                                        )
                                        Text(
                                            text = blocked.label.ifBlank { "Día bloqueado" },
                                            fontSize = 13.sp,
                                            color = colors.textPrimary
                                        )
                                    }
                                    IconButton(onClick = { blockedDateViewModel.deleteBlocked(blocked.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF5252))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = colors.surfaceColor,
            title = { Text("Agregar día bloqueado", color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (selectedDate.isBlank()) "Seleccionar fecha" else formatBlockedDate(selectedDate, "dd/MM/yyyy"),
                            fontSize = 13.sp
                        )
                    }
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Etiqueta (opcional)") },
                        placeholder = { Text("Vacaciones") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            unfocusedBorderColor = colors.divider,
                            focusedLabelColor = colors.primaryOrange,
                            cursorColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        blockedDateViewModel.addCustomDate(selectedDate, customLabel.trim())
                        showAddDialog = false
                    },
                    enabled = selectedDate.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }

    if (showDatePicker) {
        val today = remember {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val initialDateMillis = remember(selectedDate) {
            parseBlockedDateMillis(selectedDate) ?: today
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= today
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = blockedDateFromMillis(millis)
                        }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Aceptar", color = colors.primaryOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = colors.surfaceColor,
                selectedDayContainerColor = colors.primaryOrange,
                selectedDayContentColor = Color.White,
                todayDateBorderColor = colors.primaryOrange,
                todayContentColor = colors.primaryOrange,
                selectedYearContainerColor = colors.primaryOrange,
                currentYearContentColor = colors.primaryOrange
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    selectedDayContainerColor = colors.primaryOrange,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = colors.primaryOrange,
                    todayContentColor = colors.primaryOrange,
                    selectedYearContainerColor = colors.primaryOrange,
                    currentYearContentColor = colors.primaryOrange
                )
            )
        }
    }
}

@Composable
private fun HorarioSection(
    title: String,
    icon: ImageVector,
    subtitle: String,
    schedules: List<AvailabilityScheduleEntity>,
    onAdd: () -> Unit,
    onEdit: (List<AvailabilityScheduleEntity>) -> Unit,
    onDelete: (List<AvailabilityScheduleEntity>) -> Unit,
    colors: PrestadorColors
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(colors.primaryOrange.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
                OutlinedButton(
                    onClick = onAdd,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primaryOrange),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Agregar", fontSize = 13.sp)
                }
            }

            HorizontalDivider(color = colors.divider)

            if (schedules.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = colors.textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("Sin horarios configurados", color = colors.textSecondary, fontSize = 13.sp)
                    Text("Tocá Agregar para configurar", color = colors.textSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            } else {
                val grouped = schedules.groupBy {
                    Triple(it.startTime, it.endTime, it.appointmentDuration)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    grouped.forEach { (_, group) ->
                        val first = group.first()
                        val dias = group
                            .sortedBy { it.dayOfWeek }
                            .distinctBy { it.dayOfWeek }
                            .joinToString(" · ") { it.dayOfWeek.toDayAbbr() }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.primaryOrange.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Schedule, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dias, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.primaryOrange)
                                    Text(
                                        "${first.startTime} - ${first.endTime}" +
                                            if (first.worksByAppointment) "  ·  Turnos ${first.appointmentDuration} min" else "  ·  Sin turnos",
                                        fontSize = 13.sp,
                                        color = colors.textPrimary
                                    )
                                }
                                IconButton(onClick = { onEdit(group) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Edit, "Editar", tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDelete(group) }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatBlockedDate(date: String, outputPattern: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }
        val formatter = SimpleDateFormat(outputPattern, Locale.getDefault())
        val parsedDate = parser.parse(date) ?: return date
        formatter.format(parsedDate)
    } catch (_: Exception) {
        date
    }
}

private fun blockedDateFromMillis(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
        Locale.getDefault(),
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

private fun parseBlockedDateMillis(date: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { isLenient = false }.parse(date)?.time
    } catch (_: Exception) {
        null
    }
}

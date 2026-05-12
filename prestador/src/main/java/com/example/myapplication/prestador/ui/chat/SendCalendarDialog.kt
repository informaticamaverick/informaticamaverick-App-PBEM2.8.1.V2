package com.example.myapplication.prestador.ui.chat

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Store
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
import com.example.myapplication.prestador.viewmodel.BlockedDateViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendCalendarDialog(
    providerId: String,
    onDismiss: () -> Unit,
    onSend: (startDate: String, endDate: String, availabilityJson: String, bookedSlotsJson: String, appointmentType: String, providerAddress: String?, serviceCategory: String) -> Unit,
    hasPhysicalLocation: Boolean = false,
    tieneEmpresa: Boolean = false,
    initialAppointmentType: String = "TECHNICAL_VISIT",
    showTypePicker: Boolean = true,
    availabilityViewModel: AvailabilityViewModel = hiltViewModel(),
    blockedDateViewModel: BlockedDateViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val schedules by availabilityViewModel.schedules.collectAsState()
    val blockedDatesActive by blockedDateViewModel.blockedDates.collectAsState()
    val profileState by editProfileViewModel.profileState.collectAsState()

    // Carga el perfil fresco desde Firestore cada vez que se abre el dialog
    LaunchedEffect(Unit) {
        editProfileViewModel.loadProfile()
    }

    // Fuente única de verdad: leemos del ViewModel directamente
    val provider = (profileState as? com.example.myapplication.prestador.viewmodel.ProfileState.Success)?.provider
    val localActivo = provider?.hasPhysicalLocation ?: false
    val empresaActiva = provider?.hasCompanyProfile ?: false
    val canUseLocalAppointment = localActivo || empresaActiva

    // Prioridad: local propio > empresa
    val mostrarBranchesEmpresa = !localActivo && empresaActiva
    val branches = if (mostrarBranchesEmpresa) provider?.companies?.flatMap { it.branches } ?: emptyList()
    else emptyList()
    val localAddress = if (localActivo)
        provider?.addresses?.find { it.id == "local" }?.fullString() ?: provider?.address?.fullString()
    else null

    var appointmentType by remember { mutableStateOf(initialAppointmentType) }
    LaunchedEffect(canUseLocalAppointment) {
        if (!canUseLocalAppointment) appointmentType = "TECHNICAL_VISIT"
    }

    var selectedAddress by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(appointmentType, localActivo, mostrarBranchesEmpresa) {
        selectedAddress = when {
            appointmentType != "LOCAL_APPOINTMENT" -> null
            localActivo -> localAddress
            mostrarBranchesEmpresa -> branches.firstOrNull()?.address?.fullString()
            else -> null
        }
    }

    val providerCategories = remember(provider) {
        provider?.categories?.filter { it.isNotBlank() }?.distinct() ?: emptyList()
    }
    var selectedCategory by remember(providerCategories) {
        mutableStateOf(providerCategories.firstOrNull() ?: "")
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val filteredSchedules = remember(schedules, appointmentType) {
        val exact = schedules.filter { it.scheduleType == appointmentType }
        if (exact.isEmpty() && appointmentType == com.example.myapplication.prestador.data.local.entity.ScheduleType.LOCAL_APPOINTMENT.name)
            schedules.filter { it.scheduleType == com.example.myapplication.prestador.data.local.entity.ScheduleType.TECHNICAL_VISIT.name }
        else exact
    }
    val blockedSet = remember(blockedDatesActive) {
        blockedDatesActive.map { it.date }.toSet()
    }

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
        endMillis: Long,
        blockedDates: Set<String>
    ): String {
        val scheduleByDay = list.associateBy { it.dayOfWeek }
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val array = JSONArray()

        val cursor = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }

        while (!cursor.after(endCal)) {
            val entityDay = (cursor.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
            val dateStr = dateSdf.format(cursor.time)
            if (dateStr !in blockedDates) {
                scheduleByDay[entityDay]?.let { s ->
                    val obj = JSONObject()
                    obj.put("date", dateStr)
                    obj.put("dayOfWeek", s.dayOfWeek)
                    obj.put("startTime", s.startTime)
                    obj.put("endTime", s.endTime)
                    obj.put("durationMinutes", s.appointmentDuration)
                    array.put(obj)
                }
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
                    imageVector = if (appointmentType == "LOCAL_APPOINTMENT")
                        Icons.Default.Store
                    else
                        Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (appointmentType == "LOCAL_APPOINTMENT")
                        Color(0xFF6366f1)
                    else
                        Color(0xFF8B5CF6),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (appointmentType == "LOCAL_APPOINTMENT")
                        "Disponibilidad · Turno en Local"
                    else
                        "Disponibilidad · Visita Técnica",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column {
                // Selector de Categoría
                if (providerCategories.isNotEmpty()) {
                    val accentColor = if (appointmentType == "LOCAL_APPOINTMENT")
                        Color(0xFF6366f1) else Color(0xFF8B5CF6)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(accentColor, shape = RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Categoría del servicio",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = accentColor.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, if (categoryDropdownExpanded) accentColor else accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { categoryDropdownExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = selectedCategory.ifBlank { "Seleccionar categoría" },
                                    fontSize = 13.sp,
                                    color = if (selectedCategory.isBlank()) colors.textSecondary else colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (categoryDropdownExpanded)
                                        Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            providerCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (cat == selectedCategory) accentColor else accentColor.copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                cat,
                                                fontSize = 13.sp,
                                                fontWeight = if (cat == selectedCategory) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (cat == selectedCategory) accentColor else colors.textPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }


                // Selector de Tipo de Cita
                if (showTypePicker) {
                    Text(
                        text = "Tipo de servicio",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = appointmentType == "TECHNICAL_VISIT",
                            onClick = { appointmentType = "TECHNICAL_VISIT" },
                            label = { Text("Visita técnica", fontSize = 11.sp)},
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                        if (canUseLocalAppointment) {
                            FilterChip(
                                selected = appointmentType == "LOCAL_APPOINTMENT",
                                onClick = { appointmentType = "LOCAL_APPOINTMENT"},
                                label = { Text("Turno en local", fontSize = 11.sp)},
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primaryOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (appointmentType == "LOCAL_APPOINTMENT") {
                    Spacer(modifier = Modifier.height(6.dp))
                    if (mostrarBranchesEmpresa) {
                        Text(
                            text = "Seleccioná la ubicación del turno",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (branches.isEmpty()) {
                            Text("No tenés sucursales configuradas.", color = Color.Red, fontSize = 11.sp)
                        } else {
                            branches.forEach { branch ->
                                val fullAddr = branch.address.fullString()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        selectedAddress = fullAddr }.padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedAddress == fullAddr,
                                        onClick = { selectedAddress = fullAddr },
                                        colors = RadioButtonDefaults.colors(selectedColor = colors.primaryOrange)
                                    )
                                    Column {
                                        Text(branch.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                        Text(fullAddr, fontSize = 11.sp, color = colors.textSecondary)
                                    }
                                }
                            }
                        }
                    } else {
                        //Modo local/taller personal
                        Text(
                            text = "Ubicación del local/taller:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (localAddress.isNullOrBlank()) {
                            Text("No tenés dirección de local configurada.", color = Color.Red, fontSize = 11.sp)
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.primaryOrange.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Store, null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(localAddress, fontSize = 13.sp, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

        if (filteredSchedules.isEmpty()) {
                        Text(
                            text = "No tenés horarios de ${if (appointmentType == "TECHNICAL_VISIT") "Visita Técnica" else "Turno en Local"} configurados.\nAndá a Disponibilidad para agregar horarios.",
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
                            items(filteredSchedules.sortedBy { it.dayOfWeek }) { schedule ->
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
                        buildAvailabilityJson(filteredSchedules, startMs, endMs, blockedSet),
                        "[]",
                        appointmentType,
                        if (appointmentType == "LOCAL_APPOINTMENT") selectedAddress else null,
                        selectedCategory
                    )
                },
                enabled = filteredSchedules.isNotEmpty() && startDate.isNotBlank() && endDate.isNotBlank() && (appointmentType == "TECHNICAL_VISIT" || selectedAddress != null),
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

package com.example.myapplication.prestador.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import com.example.myapplication.prestador.viewmodel.chat.ChatViewModel
import com.example.myapplication.prestador.viewmodel.calendar.CalendarViewModel
import com.example.myapplication.prestador.data.local.entity.BookedAppointmentEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication.prestador.ui.calendar.dialogs.*
import com.example.myapplication.prestador.utils.ServiceTypeConfig
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.viewmodel.empresa.EmpleadosViewModel


// Modelo de datos para las citas del prestador
data class Appointment(
    val id: String,
    val clientId: String, // ID del cliente para navegación
    val chatId: String = "", // ID de la conversación para Reprogramar
    val date: String, // Formato "yyyy-MM-dd"
    val time: String, // Ej: "10:30"
    val service: String,
    val clientName: String,
    val status: AppointmentStatus,
    val avatarColor: Color
)

enum class AppointmentStatus {
    CONFIRMED,    // Confirmada
    PENDING,      // Pendiente
    CANCELLED,    // Cancelada
    COMPLETED,    // Completada
    RESCHEDULED   // Reprogramada (pendiente de nuevo horario)
}


//pantalla principal del calendario para el prestador

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PrestadorCalendarScreen(
    onNavigateToChat: (clientId: String, clientName: String, newDate: String, newTime: String, appointmentId: String) -> Unit,
    onNavigateToPresupuesto: (appointmentId: String) -> Unit = {},
    onBack: () -> Unit = {},
    triggerCreateDialog: Boolean = false,
    onCreateDialogHandled: () -> Unit = {},
    editProfileViewModel: EditProfileViewModel = hiltViewModel(),
    empleadosViewModel: EmpleadosViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (client: String) -> Unit = {}
) {
    val colors = getPrestadorColors()
    val context = LocalContext.current

    val todasConversaciones by chatViewModel.conversations.collectAsState()
    var clienteBusqueda by remember { mutableStateOf("") }
    val clientesFiltrados = remember(todasConversaciones, clienteBusqueda) {
        if (clienteBusqueda.isBlank()) emptyList()
        else todasConversaciones.filter {
            it.userName.contains(clienteBusqueda, ignoreCase = true)
        }
    }
    // Obtener estado del perfil
    val profileState by editProfileViewModel.profileState.collectAsState()
    
    // Obtener configuración según tipo de servicio del provider (REACTIVO)
    val serviceTypeConfig by remember {
        derivedStateOf {
            when (profileState) {
                is com.example.myapplication.prestador.viewmodel.profile.ProfileState.Success -> {
                    val provider = (profileState as com.example.myapplication.prestador.viewmodel.profile.ProfileState.Success).provider
                    println("🔥 CALENDAR: ServiceType cambió a ${provider.serviceType}")
                    getServiceTypeConfig(ServiceType.fromString(provider.serviceType))
                }
                else -> {
                    println("⚠️ CALENDAR: ProfileState = $profileState")
                    getServiceTypeConfig(ServiceType.PROFESSIONAL) // Default mientras carga
                }
            }
        }
    }
    
    // Paginación infinita para deslizar entre meses
    val indiceInicial = 5000
    val pagerState = rememberPagerState(
        initialPage = indiceInicial,
        pageCount = { 10000 }
    )
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para la fecha actual mostrada (sincronizada con el pager)
    var currentDate by remember {
        mutableStateOf(Calendar.getInstance())
    }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Sincronizar currentDate con la página del pager
    LaunchedEffect(pagerState.currentPage) {
        val mesesDiferencia = pagerState.currentPage - indiceInicial
        val newDate = Calendar.getInstance()
        newDate.add(Calendar.MONTH, mesesDiferencia)
        currentDate = newDate
    }
    
    // Turnos confirmados desde Room
    val bookedAppointments by calendarViewModel.allAppointments.collectAsState()
    val appointments = remember(bookedAppointments) {
        bookedAppointments.map { entity ->
            Appointment(
                id = entity.id,
                clientId = entity.clientId,
                chatId = entity.chatId,
                date = entity.date,
                time = entity.time,
                service = entity.service.ifBlank { entity.notes.ifBlank { "Turno reservado" } },
                clientName = entity.clientName,
                status = when (entity.status) {
                    "CANCELLED"   -> AppointmentStatus.CANCELLED
                    "RESCHEDULED" -> AppointmentStatus.RESCHEDULED
                    else          -> AppointmentStatus.CONFIRMED
                },
                avatarColor = Color(
                    (entity.clientId.hashCode().toLong() and 0xFFFFFFFF) or 0xFF000000
                )
            )
        }
    }
    
    //Formato de fecha para la comparacion
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    //Filtrar citas del dia seleccionado
    val selectedDateStr = dateFormat.format(selectedDate.time)
    
    // Estado para controlar si la lista de citas está expandida (calendario minimizado)
    var isExpanded by remember { mutableStateOf(true) }
    
    // Estado para el modal de edición (deshabilitado — citas eliminadas)
    var showEditDialog by remember { mutableStateOf(false) }

    // Estado para el modal de crear cita
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScopeSnackbar = rememberCoroutineScope()

    // Recibir trigger externo del FAB del dashboard
    LaunchedEffect(triggerCreateDialog) {
        if (triggerCreateDialog) {
            showCreateDialog = true
            onCreateDialogHandled()
        }
    }

    // Obtener providerId del perfil
    val providerId = remember(profileState) {
        when (profileState) {
            is com.example.myapplication.prestador.viewmodel.profile.ProfileState.Success ->
                (profileState as com.example.myapplication.prestador.viewmodel.profile.ProfileState.Success).provider.id
            else -> ""
        }
    }

    // Obtener serviceType actual
    val currentServiceType = remember(profileState) {
        when (profileState) {
            is com.example.myapplication.prestador.viewmodel.profile.ProfileState.Success ->
                ServiceType.fromString((profileState as com.example.myapplication.prestador.viewmodel.profile.ProfileState.Success).provider.serviceType)
            else -> ServiceType.PROFESSIONAL
        }
    }

    //Filtrar citas del dia seleccionado
    val appointmentsForSelectedDay = appointments.filter { it.date == selectedDateStr }
    //dias que tienen citas
    val daysWithAppointments = appointments.filter { it.status != AppointmentStatus.CANCELLED }.map { it.date }.toSet()

    //Cargar empleados para TECHNICAL
    val empleadosState by empleadosViewModel.uiState.collectAsState()
    val availableEmployees = remember(empleadosState) {
        when (val state = empleadosState) {
            is com.example.myapplication.prestador.viewmodel.empresa.EmpleadosUiState.Success ->
                state.empleados.filter { it.activo }.map { it.id to it.nombreCompleto() }
            else -> emptyList()
        }
    }
    
    // Stats para el header
    val today = dateFormat.format(Calendar.getInstance().time)
    val citasHoy = appointments.filter { it.date == today && it.status != AppointmentStatus.CANCELLED }.size
    val citasEsteMes = appointments.filter {
        val cal = Calendar.getInstance()
        it.date.startsWith("${cal.get(Calendar.YEAR)}-${String.format("%02d", cal.get(Calendar.MONTH) + 1)}")
            && it.status != AppointmentStatus.CANCELLED
    }.size

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(colors.backgroundColor)
        ) {
            // ── HEADER estilo Inicio ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.primaryOrange,
                                colors.primaryOrange.copy(alpha = 0.85f)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .statusBarsPadding()
                    .padding(start = 4.dp, end = 16.dp, bottom = 14.dp)
            ) {
                Column {
                    // Fila: back + título
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                        Text(
                            text = serviceTypeConfig.calendarTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Stats chips compactas
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Today, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text("Hoy: $citasHoy", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text("Este mes: $citasEsteMes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            // Header del calendario con navegación por flechas y mes/año
            CalendarHeader(
                currentDate = currentDate,
                onPreviousMonth = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onNextMonth = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )
            
            // Contenido del calendario que se puede minimizar
            AnimatedVisibility(visible = !isExpanded) {
                Column {
                    // HorizontalPager para deslizar entre meses
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) { page ->
                        // Calcular el mes basado en la página
                        val mesesDiferencia = page - indiceInicial
                        val fechaMes = Calendar.getInstance()
                        fechaMes.add(Calendar.MONTH, mesesDiferencia)
                        
                        // Renderizar el grid del mes
                        CalendarGrid(
                            currentDate = fechaMes,
                            selectedDate = selectedDate,
                            daysWithAppointments = daysWithAppointments,
                            onDateSelected = { newDate ->
                                selectedDate = newDate
                                isExpanded = true // Minimizar calendario automáticamente
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Lista de citas (todas del tipo, el componente filtra internamente)
            AppointmentsList(
                appointments = appointmentsForSelectedDay,
                selectedDate = selectedDate,
                isExpanded = isExpanded,
                serviceTypeConfig = serviceTypeConfig,
                onExpandClick = { isExpanded = !isExpanded },
                onNavigateToClientePerfil = onNavigateToClientePerfil,
                onGoToChat = { clientId, clientName ->
                    onNavigateToChat(clientId, clientName, "", "", "")
                },
                onCancel = { id, chatId, date, time, reason ->
                    calendarViewModel.cancelAppointment(id)
                    chatViewModel.sendCancellationNotice(chatId, date, time, reason)
                },
                onComplete = { id, chatId, date, time ->
                    calendarViewModel.completeAppointment(id)
                    chatViewModel.sendCompletionNotice(chatId, date, time)
                },
                onReschedule = { id, chatId, date, time, clientId, clientName ->
                    calendarViewModel.rescheduleAppointment(id)
                    chatViewModel.sendRescheduleNotice(chatId, date, time)
                    onNavigateToChat(clientId, clientName, date, time, "RESCHEDULE")
                }
            )
        }
    }

}

//Header del calendario con navegacion de mes

@Composable
fun CalendarHeader(
    currentDate: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val colors = getPrestadorColors()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Mes anterior",
                tint = Color(0xFFFF6B35)
            )
        }

        Text(
            text = monthFormat.format(currentDate.time).capitalize(Locale.getDefault()),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Mes siguiente",
                tint = Color(0xFFFF6B35)
            )
        }
    }
}

@Composable
fun CalendarGrid(
    currentDate: Calendar,
    selectedDate: Calendar,
    daysWithAppointments: Set<String>,
    onDateSelected: (Calendar) -> Unit
) {
    val colors = getPrestadorColors()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    //DIAS DE LA SEMANA
    val daysOfWeek = listOf("D", "L", "M", "M", "J", "V", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        //Header de dias de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de días
        val daysInMonth = getDaysInMonth(currentDate)
        val rows = daysInMonth.chunked(7)

        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { dayInfo ->
                    DayCell(
                        dayInfo = dayInfo,
                        selectedDate = selectedDate,
                        daysWithAppointments = daysWithAppointments,
                        dateFormat = dateFormat,
                        onDateSelected = onDateSelected
                    )
                }
                // Rellenar espacios vacíos al final
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Data class para información del día
 */
data class DayInfo(
    val date: Calendar,
    val dayNumber: Int,
    val isCurrentMonth: Boolean
)

/**
 * Obtiene todos los días del mes incluyendo días del mes anterior/siguiente
 */
fun getDaysInMonth(date: Calendar): List<DayInfo> {
    val days = mutableListOf<DayInfo>()
    val calendar = date.clone() as Calendar
    
    // Ir al primer día del mes
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    
    // Agregar días del mes anterior
    val prevMonth = calendar.clone() as Calendar
    prevMonth.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    for (i in (daysInPrevMonth - firstDayOfWeek + 1)..daysInPrevMonth) {
        val day = prevMonth.clone() as Calendar
        day.set(Calendar.DAY_OF_MONTH, i)
        days.add(DayInfo(day, i, false))
    }
    
    // Agregar días del mes actual
    val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..daysInCurrentMonth) {
        val day = calendar.clone() as Calendar
        day.set(Calendar.DAY_OF_MONTH, i)
        days.add(DayInfo(day, i, true))
    }
    
    // Agregar días del mes siguiente
    val nextMonth = calendar.clone() as Calendar
    nextMonth.add(Calendar.MONTH, 1)
    val remainingDays = 42 - days.size // 6 semanas completas
    for (i in 1..remainingDays) {
        val day = nextMonth.clone() as Calendar
        day.set(Calendar.DAY_OF_MONTH, i)
        days.add(DayInfo(day, i, false))
    }
    
    return days
}

/**
 * Celda individual del día en el calendario
 */
@Composable
fun RowScope.DayCell(
    dayInfo: DayInfo,
    selectedDate: Calendar,
    daysWithAppointments: Set<String>,
    dateFormat: SimpleDateFormat,
    onDateSelected: (Calendar) -> Unit
) {
    val colors = getPrestadorColors()
    val isSelected = dateFormat.format(dayInfo.date.time) == dateFormat.format(selectedDate.time)
    val hasAppointments = daysWithAppointments.contains(dateFormat.format(dayInfo.date.time))
    val isToday = dateFormat.format(dayInfo.date.time) == dateFormat.format(Calendar.getInstance().time)
    
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> Color(0xFFFF6B35)
                    isToday -> colors.primaryOrangeLight
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = dayInfo.isCurrentMonth) {
                onDateSelected(dayInfo.date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayInfo.dayNumber.toString(),
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    !dayInfo.isCurrentMonth -> colors.textSecondary.copy(alpha = 0.4f)
                    isToday -> Color(0xFFFF6B35)
                    else -> colors.textPrimary
                }
            )
            
            if (hasAppointments && dayInfo.isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFFFF6B35))
                )
            }
        }
    }
}

//lista de citas del dia seleccionado

@Composable
fun AppointmentsList(
    appointments: List<Appointment>,
    selectedDate: Calendar,
    isExpanded: Boolean,
    serviceTypeConfig: com.example.myapplication.prestador.utils.ServiceTypeConfig,
    onExpandClick: () -> Unit,
    onNavigateToClientePerfil: (clientId: String) -> Unit = {},
    onGoToChat: (clientId: String, clientName: String) -> Unit = { _, _ -> },
    onCancel: (id: String, chatId: String, date: String, time: String, reason: String) -> Unit = { _, _, _, _, _ -> },
    onComplete: (id: String, chatId: String, date: String, time: String) -> Unit = { _, _, _, _ -> },
    onReschedule: (id: String, chatId: String, date: String, time: String, clientId: String, clientName: String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val colors = getPrestadorColors()
    val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale.getDefault())
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    // Separar en próximas (pending/confirmed/rescheduled con fecha >= hoy) e historial (completed/cancelled o pasadas)
    val upcoming = appointments.filter {
        (it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.RESCHEDULED) && it.date >= today
    }.sortedBy { it.date + it.time }

    val history = appointments.filter {
        it.status == AppointmentStatus.COMPLETED || it.status == AppointmentStatus.CANCELLED ||
        ((it.status == AppointmentStatus.PENDING || it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.RESCHEDULED) && it.date < today)
    }.sortedByDescending { it.date + it.time }

    var selectedTab by remember { mutableStateOf(0) }
    val displayList = if (selectedTab == 0) upcoming else history

    // Estado para controlar qué cita está expandida (solo una a la vez)
    var expandedAppointmentId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header con fecha del día seleccionado y toggle expandir
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${selectedDate.get(Calendar.DAY_OF_MONTH)} de ${monthNames[selectedDate.get(Calendar.MONTH)]}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = if (appointments.isEmpty()) {
                        "Sin ${serviceTypeConfig.appointmentsName}"
                    } else {
                        "${appointments.size} ${if (appointments.size == 1) serviceTypeConfig.appointmentName else serviceTypeConfig.appointmentsName}"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
            }
            Text(
                text = if (isExpanded) "Minimizar" else "Expandir",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35),
                modifier = Modifier.clickable { onExpandClick() }
            )
        }

        // Tabs Próximas / Historial
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Próximas" to upcoming.size, "Historial" to history.size).forEachIndexed { index, (label, count) ->
                val isSelected = selectedTab == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index; expandedAppointmentId = null },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(0xFFFF6B35) else colors.surfaceElevated
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else colors.textSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(alpha = 0.3f) else colors.border
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * direction } + fadeIn(tween(250))) togetherWith
                (slideOutHorizontally { -it * direction } + fadeOut(tween(200)))
            },
            label = "tab_content"
        ) { tab ->
            val list = if (tab == 0) upcoming else history
            if (list.isEmpty()) {
                // sin citas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(2.dp, colors.border, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (tab == 0) "📅" else "🗂️", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (tab == 0)
                                "No hay ${serviceTypeConfig.appointmentsName} próximas"
                            else
                                "Sin historial de ${serviceTypeConfig.appointmentsName}",
                            fontSize = 14.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(list) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            isExpanded = expandedAppointmentId == appointment.id,
                            onToggleExpand = {
                                expandedAppointmentId = if (expandedAppointmentId == appointment.id) null else appointment.id
                            },
                            serviceTypeConfig = serviceTypeConfig,
                            onNavigateToClientePerfil = onNavigateToClientePerfil,
                            onGoToChat = onGoToChat,
                            onCancel = onCancel,
                            onComplete = onComplete,
                            onReschedule = onReschedule
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual de cita
 */
@Composable
fun AppointmentCard(
    appointment: Appointment,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    serviceTypeConfig: ServiceTypeConfig,
    onNavigateToClientePerfil: (clientId: String) -> Unit = {},
    onGoToChat: (clientId: String, clientName: String) -> Unit = { _, _ -> },
    onCancel: (id: String, chatId: String, date: String, time: String, reason: String) -> Unit = { _, _, _, _, _ -> },
    onComplete: (id: String, chatId: String, date: String, time: String) -> Unit = { _, _, _, _ -> },
    onReschedule: (id: String, chatId: String, date: String, time: String, clientId: String, clientName: String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val colors = getPrestadorColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar del cliente
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(appointment.avatarColor)
                        .clickable { onNavigateToClientePerfil(appointment.clientId) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appointment.clientName.first().uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Información de la cita
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.clientName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appointment.service,
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🕐",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.time,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                
                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (appointment.status) {
                        AppointmentStatus.CONFIRMED    -> Color(0xFF10B981).copy(alpha = 0.1f)
                        AppointmentStatus.PENDING      -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                        AppointmentStatus.CANCELLED    -> Color(0xFFEF4444).copy(alpha = 0.1f)
                        AppointmentStatus.COMPLETED    -> Color(0xFF6366F1).copy(alpha = 0.1f)
                        AppointmentStatus.RESCHEDULED  -> Color(0xFF059669).copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = when (appointment.status) {
                            AppointmentStatus.CONFIRMED   -> serviceTypeConfig.confirmedStatus
                            AppointmentStatus.PENDING     -> serviceTypeConfig.pendingStatus
                            AppointmentStatus.CANCELLED   -> serviceTypeConfig.cancelledStatus
                            AppointmentStatus.COMPLETED   -> "Completada"
                            AppointmentStatus.RESCHEDULED -> "Reprogramando"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (appointment.status) {
                            AppointmentStatus.CONFIRMED   -> Color(0xFF10B981)
                            AppointmentStatus.PENDING     -> Color(0xFFF59E0B)
                            AppointmentStatus.CANCELLED   -> Color(0xFFEF4444)
                            AppointmentStatus.COMPLETED   -> Color(0xFF6366F1)
                            AppointmentStatus.RESCHEDULED -> Color(0xFF059669)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Botones de acción (solo si está expandida y no está cancelada ni completada)
            AnimatedVisibility(
                visible = isExpanded && appointment.status != AppointmentStatus.CANCELLED && appointment.status != AppointmentStatus.COMPLETED,
                enter = expandVertically(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(300))
            ) {
                Column {
                    HorizontalDivider(color = colors.surfaceElevated)

                    var showCancelDialog by remember { mutableStateOf(false) }
                    var cancelReason by remember { mutableStateOf("") }
                    var showCompleteDialog by remember { mutableStateOf(false) }
                    var showRescheduleDialog by remember { mutableStateOf(false) }

                    //Botones de acción
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val btnPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        val btnModifier = Modifier.weight(1f).height(36.dp)

                        //Completar
                        Button(
                            onClick = { showCompleteDialog = true },
                            modifier = btnModifier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = btnPadding
                        ) {
                            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Completar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        //Reprogramar
                        Button(
                            onClick = { showRescheduleDialog = true },
                            modifier = btnModifier,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = btnPadding
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Reprogramar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        //Cancelar
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = btnModifier,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = btnPadding
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Cancelar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showCancelDialog) {
                        Dialog(onDismissRequest = { showCancelDialog = false }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surfaceColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    // Header rojo
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color(0xFFEF4444),
                                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                            )
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Cancelar turno",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    // Contenido
                                    Column(modifier = Modifier.padding(20.dp)) {

                                        // Info del turno
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = appointment.clientName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = colors.textPrimary
                                                    )
                                                    Text(
                                                        text = "${appointment.date}  •  ${appointment.time}",
                                                        fontSize = 12.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        //Motivos rapidos
                                        val motivos = listOf("Horario no disponible", "Problema personal", "Fuerza mayor", "Otro")
                                        Text("Motivo (obligatorio):", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                            color = colors.textPrimary)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        motivos.forEach { motivo ->
                                            val selected = cancelReason == motivo
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (selected) Color(0xFFEF4444) else Color(
                                                    0xFFEF4444
                                                ).copy(alpha = 0.08f),
                                                border = BorderStroke(
                                                    1.dp,
                                                    Color(0xFFEF4444).copy(alpha = if (selected) 1f else 0.3f)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clickable { cancelReason = motivo }
                                            ) {
                                                Text(
                                                    text = motivo,
                                                    fontSize = 13.sp,
                                                    color = if (selected) Color.White else Color(
                                                        0xFFEF4444
                                                    ),
                                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                    modifier = Modifier.padding(
                                                        horizontal = 14.dp,
                                                        vertical = 8.dp
                                                    )
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))


                                        Text(
                                            text = "Esta acción no se puede deshacer. El cliente recibirá un mensaje automático notificando la cancelación.",
                                            fontSize = 13.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 18.sp
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Botones
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedButton(
                                                onClick = { showCancelDialog = false },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, colors.border),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Volver", color = colors.textPrimary)
                                            }
                                            Button(
                                                onClick = {
                                                    if (cancelReason.isNotEmpty()) {
                                                        onCancel(appointment.id, appointment.chatId, appointment.date, appointment.time, cancelReason)
                                                        cancelReason = ""
                                                        showCancelDialog = false
                                                    }
                                                },
                                                enabled = cancelReason.isNotEmpty(),
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text("Sí, cancelar", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Dialog Completar
                    if (showCompleteDialog) {
                        Dialog(onDismissRequest = { showCompleteDialog = false }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surfaceColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF6366F1), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Done, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Completar turno", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF6366F1).copy(alpha = 0.1f),
                                            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(appointment.clientName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                                    Text("${appointment.date}  •  ${appointment.time}", fontSize = 12.sp, color = colors.textSecondary)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Al marcar como completado se notificará al cliente. Esta acción no se puede deshacer.", fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedButton(
                                                onClick = { showCompleteDialog = false },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, colors.border),
                                                shape = RoundedCornerShape(10.dp)
                                            ) { Text("Volver", color = colors.textPrimary) }
                                            Button(
                                                onClick = {
                                                    onComplete(appointment.id, appointment.chatId, appointment.date, appointment.time)
                                                    showCompleteDialog = false
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) { Text("Sí, completar", color = Color.White, fontWeight = FontWeight.Bold) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Dialog Reprogramar
                    if (showRescheduleDialog) {
                        Dialog(onDismissRequest = { showRescheduleDialog = false }) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = colors.surfaceColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFF6B35), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Reprogramar turno", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    }
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFF6B35).copy(alpha = 0.1f),
                                            border = BorderStroke(1.dp, Color(0xFFFF6B35).copy(alpha = 0.3f))
                                        ) {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFFF6B35), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(appointment.clientName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                                    Text("${appointment.date}  •  ${appointment.time}", fontSize = 12.sp, color = colors.textSecondary)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text("Se enviará al cliente un mensaje para que elija una nueva fecha. El estado del turno pasará a Reprogramando.", fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedButton(
                                                onClick = { showRescheduleDialog = false },
                                                modifier = Modifier.weight(1f),
                                                border = BorderStroke(1.dp, colors.border),
                                                shape = RoundedCornerShape(10.dp)
                                            ) { Text("Volver", color = colors.textPrimary) }
                                            Button(
                                                onClick = {
                                                    onReschedule(appointment.id, appointment.chatId, appointment.date, appointment.time, appointment.clientId, appointment.clientName)
                                                    showRescheduleDialog = false
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) { Text("Sí, reprogramar", color = Color.White, fontWeight = FontWeight.Bold) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Función helper para generar color único basado en clientId
private fun generateColorFromId(clientId: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFFEC4899), // Pink
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Amber
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFFEF4444), // Red
        Color(0xFF14B8A6)  // Teal
    )
    val hash = clientId.hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}

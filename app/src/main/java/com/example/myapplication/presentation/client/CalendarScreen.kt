package com.example.myapplication.presentation.client

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

// ==========================================================================================
// --- CONSTANTES VISUALES MAVERICK PRO ---
// ==========================================================================================
private val DarkBg = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val NeonCyber = Color(0xFF00FFC2)
private val StatusConfirmed = Color(0xFF10B981)
private val StatusPending = Color(0xFFF59E0B)
private val ErrorRed = Color(0xFFF43F5E)

// ==========================================================================================
// --- PANTALLA PRINCIPAL DEL CALENDARIO (STATEFUL / MVVM) ---
// ==========================================================================================

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onChatClick: (String) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
) {
    val filteredEvents by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val daysWithEvents by viewModel.daysWithEvents.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    val userState by profileViewModel.userState.collectAsStateWithLifecycle()
    val currentUserId = userState?.email ?: "user_demo_66"

    // --- 1. SINCRONIZACIÓN CON EL ASISTENTE BE ---
    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("calendar")
    }

    // --- 2. CONFIGURACIÓN DE LA CAJA DE HERRAMIENTAS (TOOLBOX) ---
    val calendarActions = remember {
        listOf(
            BeSmallActionModel(
                id = "add_event",
                icon = Icons.Default.Add,
                label = "Nuevo",
                emoji = "➕",
                isDefault = true
            ),
            BeSmallActionModel(
                id = "clean_history",
                icon = Icons.Default.DeleteSweep,
                label = "Limpiar",
                emoji = "🧹",
                isDefault = false
            ),
            BeSmallActionModel(
                id = "refresh_calendar",
                icon = Icons.Default.Refresh,
                label = "Refrescar",
                emoji = "🔄",
                isDefault = false
            )
        )
    }

    DisposableEffect(Unit) {
        beBrainViewModel.setCustomActions(calendarActions)
        onDispose {
            beBrainViewModel.setCustomActions(emptyList())
        }
    }

    // --- 3. ESCUCHA DE EVENTOS DE ACCIÓN (BE ACTIONS) ---
    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "add_event" -> {
                    // Implementar lógica para añadir evento a futuro
                }
                "clean_history" -> {
                    // Implementar lógica para limpiar historial a futuro
                }
                "refresh_calendar" -> {
                    // Acción de refresco
                }
            }
        }
    }

    CalendarScreenContent(
        filteredEvents = filteredEvents,
        daysWithEvents = daysWithEvents,
        selectedDate = selectedDate,
        onBack = onBack,
        onChatClick = onChatClick,
        onDateChange = { viewModel.updateSelectedDate(it) },
        onCancelEvent = { event ->
            viewModel.cancelEvent(event, currentUserId)
        },
        onRescheduleEvent = { event ->
            viewModel.requestReschedule(event, currentUserId)
        }
    )
}

// ==========================================================================================
// --- CONTENIDO STATELESS ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenContent(
    filteredEvents: List<CalendarEventEntity>,
    daysWithEvents: Set<String>,
    selectedDate: Calendar,
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    onDateChange: (Calendar) -> Unit,
    onCancelEvent: (CalendarEventEntity) -> Unit,
    onRescheduleEvent: (CalendarEventEntity) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- ESTADOS DEL CALENDARIO ---
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    var isCalendarExpanded by remember { mutableStateOf(false) } // Por defecto arranca oculto

    // --- ESTADOS DE MODALES ---
    var selectedEvent by remember { mutableStateOf<CalendarEventEntity?>(null) } // Abre el Modal de Detalles
    var eventToCancel by remember { mutableStateOf<CalendarEventEntity?>(null) } // Abre el Modal de Confirmación

    // --- 🏗️ SECCIÓN: LÓGICA DE ANIMACIÓN DE CABECERA (SCROLL) ---
    val scrollState = rememberScrollState()
    val collapseFraction by remember {
        derivedStateOf {
            (scrollState.value.toFloat() / 250f).coerceIn(0f, 1f)
        }
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // ==========================================================================================
                // --- 🏗️ SECCIÓN: CABECERA DINÁMICA MAVERICK (REEMPLAZO DE TOPAPPBAR) ---
                // ==========================================================================================
                BarraCabezera(
                    title = "Mi Agenda",
                    subtitle = "Control de visitas y turnos",
                    emoji = "📅",
                    onBack = onBack,
                    onInfoClick = { isCalendarExpanded = !isCalendarExpanded },
                    collapseFraction = collapseFraction,
                    accentColor = Color(0xFF2197F5)
                )
            }
        ) { paddingValues ->
            val safePadding = PaddingValues(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current).coerceAtLeast(0.dp),
                top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(safePadding)
                    .verticalScroll(scrollState)
            ) {
                // --- 1. WIDGET DEL CALENDARIO GLASS ---
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // Header con botón de Minimizar integrado
                        CalendarHeaderPro(
                            currentDate = currentDate,
                            isExpanded = isCalendarExpanded,
                            onToggleExpand = { isCalendarExpanded = !isCalendarExpanded },
                            onPreviousMonth = { currentDate = Calendar.getInstance().apply { time = currentDate.time; add(Calendar.MONTH, -1) } },
                            onNextMonth = { currentDate = Calendar.getInstance().apply { time = currentDate.time; add(Calendar.MONTH, 1) } }
                        )

                        // Grilla del calendario Animada (Ocultable)
                        AnimatedVisibility(
                            visible = isCalendarExpanded,
                            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                WeekDaysHeaderPro()
                                Spacer(modifier = Modifier.height(12.dp))

                                CalendarGridPro(
                                    currentDate = currentDate,
                                    selectedDate = selectedDate,
                                    daysWithEvents = daysWithEvents,
                                    events = filteredEvents,
                                    dateFormat = dateFormat,
                                    onDayClick = { day ->
                                        onDateChange(Calendar.getInstance().apply { time = currentDate.time; set(Calendar.DAY_OF_MONTH, day) })
                                    }
                                )
                            }
                        }
                    }
                }

                // --- 2. LISTA DE EVENTOS ---
                EventsListPro(
                    selectedDate = selectedDate,
                    events = filteredEvents,
                    onEventClick = { event -> selectedEvent = event } // Abre el Modal de Detalles
                )

                Spacer(modifier = Modifier.height(120.dp)) // Espacio para el FAB
            }

        }

        // =========================================================================
        // --- MODALES Y POPUPS ---
        // =========================================================================

        // 1. MODAL DE DETALLES DEL EVENTO
        if (selectedEvent != null) {
            EventDetailsModal(
                event = selectedEvent!!,
                onDismiss = { selectedEvent = null },
                onChatClick = {
                    selectedEvent = null
                    onChatClick(it)
                },
                onRescheduleClick = { event ->
                    // Llama a la función que actualiza Room y envía el mensaje de chat
                    onRescheduleEvent(event)
                    selectedEvent = null
                    onChatClick(event.providerId)
                },
                onCancelClick = { event ->
                    // Cierra detalles y abre confirmación de cancelación
                    eventToCancel = event
                    selectedEvent = null
                }
            )
        }

        // 2. MODAL DE CONFIRMACIÓN DE CANCELACIÓN
        if (eventToCancel != null) {
            CancelVisitConfirmModal(
                event = eventToCancel!!,
                onConfirm = { event ->
                    // Ejecuta la cancelación real a través de Room y Chat
                    onCancelEvent(event)
                    eventToCancel = null
                },
                onDismiss = { eventToCancel = null }
            )
        }

    }
}

// ==========================================================================================
// --- COMPONENTES DEL CALENDARIO PRO ---
// ==========================================================================================

@Composable
fun CalendarHeaderPro(
    currentDate: Calendar,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chevronRotate")

    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) { Icon(Icons.Default.KeyboardArrowLeft, null, tint = MaverickBlue) }

        // Área central clickeable para colapsar/expandir el calendario
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Sin onda de ripple para que sea sutil
                    onClick = onToggleExpand
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "HOY ${currentDate.get(Calendar.DAY_OF_MONTH)} de ${monthNames[currentDate.get(Calendar.MONTH)]} / ${currentDate.get(Calendar.YEAR)}".uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Ocultar" else "Mostrar",
                tint = MaverickBlue,
                modifier = Modifier.size(20.dp).rotate(rotation)
            )
        }

        IconButton(onClick = onNextMonth) { Icon(Icons.Default.KeyboardArrowRight, null, tint = MaverickBlue) }
    }
}

@Composable
fun WeekDaysHeaderPro() {
    val weekDays = listOf("Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        weekDays.forEach { day -> Text(text = day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray) }
    }
}

@Composable
fun CalendarGridPro(
    currentDate: Calendar,
    selectedDate: Calendar,
    daysWithEvents: Set<String>,
    events: List<CalendarEventEntity>,
    dateFormat: SimpleDateFormat,
    onDayClick: (Int) -> Unit
) {
    val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = Calendar.getInstance().apply { time = currentDate.time; set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val today = Calendar.getInstance()

    Column {
        var dayCounter = 1
        val rows = (firstDayOfMonth + daysInMonth + 6) / 7

        repeat(rows) { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                repeat(7) { dayOfWeek ->
                    val cellIndex = week * 7 + dayOfWeek
                    if (cellIndex < firstDayOfMonth || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = dayCounter
                        val dateToCheck = Calendar.getInstance().apply { time = currentDate.time; set(Calendar.DAY_OF_MONTH, day) }
                        val dateStr = dateFormat.format(dateToCheck.time)

                        val isSelected = isSameDay(dateToCheck, selectedDate)
                        val isToday = isSameDay(dateToCheck, today)
                        val hasEvent = daysWithEvents.contains(dateStr)

                        // Mapea el color del primer evento desde la entidad Room
                        val dotColor = if (hasEvent) {
                            events.firstOrNull { it.date == dateStr && it.status != VisitStatus.CANCELLED }?.let { Color(it.type.colorLong) } ?: NeonCyber
                        } else Color.Transparent

                        DayCellPro(day, isSelected, isToday, hasEvent, dotColor) { onDayClick(day) }
                        dayCounter++
                    }
                }
            }
            if (week < rows - 1) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun RowScope.DayCellPro(day: Int, isSelected: Boolean, isToday: Boolean, hasEvent: Boolean, dotColor: Color, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaverickBlue else if (isToday) Color.White.copy(alpha = 0.05f) else Color.Transparent
    val textColor = if (isSelected || isToday) Color.White else Color.Gray
    val fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium

    Box(
        modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(CircleShape)
            .background(bgColor).border(if (isToday && !isSelected) 1.dp else 0.dp, if (isToday && !isSelected) MaverickBlue else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = day.toString(), fontSize = 14.sp, fontWeight = fontWeight, color = textColor)
            if (hasEvent) {
                Spacer(modifier = Modifier.height(2.dp))
                // Indicador de evento coloreado según el tipo
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) DarkBg else dotColor))
            }
        }
    }
}

// ==========================================================================================
// --- LISTA Y TARJETAS DE EVENTOS ---
// ==========================================================================================

@Composable
fun EventsListPro(
    selectedDate: Calendar,
    events: List<CalendarEventEntity>,
    onEventClick: (CalendarEventEntity) -> Unit
) {
    val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Eventos del ${selectedDate.get(Calendar.DAY_OF_MONTH)} de ${monthNames[selectedDate.get(Calendar.MONTH)]}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Text("${events.size} Registros", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }

        if (events.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                events.forEach { event ->
                    EventCardPro(event = event, onClick = { onEventClick(event) })
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Coffee, null, modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Agenda Libre", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.5f))
                    Text("No hay eventos ni turnos este día.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EventCardPro(
    event: CalendarEventEntity,
    onClick: () -> Unit
) {
    val isCancelled = event.status == VisitStatus.CANCELLED
    val cardAlpha = if (isCancelled) 0.4f else 1f
    val eventColor = Color(event.type.colorLong)

    Surface(
        modifier = Modifier.fillMaxWidth().alpha(cardAlpha).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = CardSurface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(eventColor).align(Alignment.CenterStart).zIndex(10f))

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

                // HORARIO
                Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = event.time, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isCancelled) Color.Gray else eventColor)
                    Text("HRS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
                }

                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 12.dp))
                Spacer(modifier = Modifier.width(16.dp))

                // INFO DEL SERVICIO
                Column(modifier = Modifier.weight(1f)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Surface(color = eventColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, eventColor.copy(alpha=0.3f))) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(event.type.emoji, fontSize = 8.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(event.type.label.uppercase(), color = eventColor, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        val (statusText, statusColor) = when (event.status) {
                            VisitStatus.CONFIRMED -> "CONFIRMADO" to StatusConfirmed
                            VisitStatus.PENDING -> "PENDIENTE" to StatusPending
                            VisitStatus.CANCELLED -> "CANCELADO" to ErrorRed
                        }
                        Text(statusText, fontSize = 8.sp, fontWeight = FontWeight.Black, color = statusColor, letterSpacing = 1.sp, modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, textDecoration = if(isCancelled) TextDecoration.LineThrough else TextDecoration.None, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    // Dirección
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(event.address, fontSize = 11.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    // Provider Avatar
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (event.providerPhotoUrl != null) {
                            AsyncImage(
                                model = event.providerPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp).clip(CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.size(20.dp).background(Color(event.avatarColorLong), CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(event.provider.first().toString(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(event.provider, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- MODALES FLOTANTES ---
// ==========================================================================================

@Composable
fun EventDetailsModal(
    event: CalendarEventEntity,
    onDismiss: () -> Unit,
    onChatClick: (String) -> Unit,
    onRescheduleClick: (CalendarEventEntity) -> Unit,
    onCancelClick: (CalendarEventEntity) -> Unit
) {
    val eventColor = Color(event.type.colorLong)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).clickable(onClick = onDismiss, indication = null, interactionSource = remember { MutableInteractionSource() }), contentAlignment = Alignment.Center) {

            Surface(
                modifier = Modifier.fillMaxWidth(0.9f).clickable(onClick = {}, indication = null, interactionSource = remember { MutableInteractionSource() }),
                shape = RoundedCornerShape(32.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, eventColor.copy(alpha = 0.4f)),
                shadowElevation = 24.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = eventColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, eventColor.copy(alpha=0.3f))) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(event.type.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(event.type.label.uppercase(), color = eventColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }

                        val (statusText, statusColor) = when (event.status) {
                            VisitStatus.CONFIRMED -> "CONFIRMADO" to StatusConfirmed
                            VisitStatus.PENDING -> "PENDIENTE" to StatusPending
                            VisitStatus.CANCELLED -> "CANCELADO" to ErrorRed
                        }
                        Text(statusText, fontSize = 10.sp, fontWeight = FontWeight.Black, color = statusColor, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(text = event.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White, lineHeight = 28.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(modifier = Modifier.weight(1f), color = Color.White.copy(0.05f), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("DÍA", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                Text(event.date, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        Surface(modifier = Modifier.weight(1f), color = Color.White.copy(0.05f), shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("HORARIO", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                Text("${event.time} HS", fontSize = 14.sp, color = eventColor, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(Color.White.copy(0.05f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Dirección", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(event.address, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (event.providerPhotoUrl != null) {
                                AsyncImage(
                                    model = event.providerPhotoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.size(36.dp).background(Color(event.avatarColorLong), CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Handyman, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Prestador / Profesional", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(event.provider, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            MaverickTacticalButton(
                                onClick = { onChatClick(event.providerId) },
                                size = 36.dp,
                                accentColor = MaverickBlue
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Message, null, tint = MaverickBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (event.status != VisitStatus.CANCELLED) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onCancelClick(event) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f), contentColor = ErrorRed)
                            ) {
                                Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }

                            Button(
                                onClick = { onRescheduleClick(event) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = eventColor, contentColor = Color.White)
                            ) {
                                Text("REPROGRAMAR", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    } else {
                        Surface(modifier = Modifier.fillMaxWidth(), color = ErrorRed.copy(0.1f), shape = RoundedCornerShape(12.dp)) {
                            Text("Este evento ha sido cancelado", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CancelVisitConfirmModal(
    event: CalendarEventEntity,
    onConfirm: (CalendarEventEntity) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).zIndex(300f), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(32.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                shadowElevation = 20.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(64.dp).background(ErrorRed.copy(alpha = 0.1f), CircleShape).border(2.dp, ErrorRed.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.WarningAmber, null, modifier = Modifier.size(32.dp), tint = ErrorRed)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¿Anular ${event.type.label}?", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Se cancelará el evento y se enviará un mensaje automático a ${event.provider} informándole. ¿Deseas continuar?", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = { onConfirm(event) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                        Text("SÍ, ANULAR Y AVISAR", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text("MANTENER CITA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun isSameDay(date1: Calendar, date2: Calendar): Boolean {
    return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) && date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) && date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH)
}

// ==========================================================================================
// --- PREVIEW ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun CalendarScreenPreview() {
    MyApplicationTheme {
        // Fix for Preview issue: Cannot create an instance of class CalendarViewModel.
        // Instead of calling CalendarScreen (which uses hiltViewModel),
        // directly call CalendarScreenContent and provide dummy data.
        val dummyEvents = listOf(
            CalendarEventEntity(
                id = "1",
                title = "Revisión de Sistema",
                // Removed 'description' as it's not a parameter in CalendarEventEntity
                date = "2023-11-15",
                time = "10:00",
                type = EventType.VISIT,
                status = VisitStatus.CONFIRMED,
                provider = "Tech Solutions Inc.",
                providerId = "tech_solutions_1",
                address = "Calle Falsa 123",
                providerPhotoUrl = null,
                avatarColorLong = 0xFF42A5F5 // Blue
            ),
            CalendarEventEntity(
                id = "2",
                title = "Consulta Médica",
                // Removed 'description' as it's not a parameter in CalendarEventEntity
                date = "2023-11-15",
                time = "14:30",
                type = EventType.APPOINTMENT,
                status = VisitStatus.PENDING,
                provider = "Dr. John Smith",
                providerId = "dr_smith_2",
                address = "Av. Siempre Viva 742",
                providerPhotoUrl = null,
                avatarColorLong = 0xFF66BB6A // Green
            ),
            CalendarEventEntity(
                id = "3",
                title = "Entrega de Paquete",
                // Removed 'description' as it's not a parameter in CalendarEventEntity
                date = "2023-11-16",
                time = "09:00",
                type = EventType.SHIPPING,
                status = VisitStatus.CONFIRMED,
                provider = "Envios Express",
                providerId = "envios_express_3",
                address = "Ruta 40 Km 10",
                providerPhotoUrl = null,
                avatarColorLong = 0xFFFFA726 // Orange
            )
        )

        CalendarScreenContent(
            filteredEvents = dummyEvents,
            daysWithEvents = dummyEvents.map { it.date }.toSet(),
            selectedDate = Calendar.getInstance(),
            onBack = {},
            onChatClick = {},
            onDateChange = {},
            onCancelEvent = {},
            onRescheduleEvent = {}
        )
    }
}

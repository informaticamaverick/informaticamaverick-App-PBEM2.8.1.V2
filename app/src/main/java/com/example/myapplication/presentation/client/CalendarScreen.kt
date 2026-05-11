package com.example.myapplication.presentation.client

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.components.Utilidades.M3VerticalDivider
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- CONSTANTES DE DISEÑO MAVERICK ELITE ---
 * Paleta de colores inspirada en Cyberpunk / M3 Dark Mode
 */
private val DarkBg = Color(0xFF020408)
private val MaverickBlue = Color(0xFF2E91FF)
private val CyberNeon = Color(0xFF00FFC2)
private val StatusOk = Color(0xFF00E676)
private val StatusWait = Color(0xFFFFA000)

// ==========================================================================================
// --- PANTALLA PRINCIPAL (STATEFUL) ---
// ==========================================================================================

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onChatClick: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel()
) {
    val filteredEvents by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val groupedEvents by viewModel.groupedEvents.collectAsStateWithLifecycle()
    val daysWithEventColors by viewModel.daysWithEventColors.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeFilters by viewModel.activeFilters.collectAsStateWithLifecycle()
    val showPastEvents by viewModel.showPastEvents.collectAsStateWithLifecycle()
    val nextEvents by viewModel.nextEvents.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    
    // [NUEVO] Estados para el Historial de Be
    val pastEvents by viewModel.pastEvents.collectAsStateWithLifecycle()
    var showHistorySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // [REGLA DE ORO] Sincronizar contexto con Be
        beBrainViewModel.onRouteChanged("calendar")
        beBrainViewModel.coordinator.updateSearchQuery("")
        beBrainViewModel.coordinator.selectCategory(null)
    }

    // --- SECCIÓN: SINCRONIZACIÓN CON EL ASISTENTE BE ---
    // [REGLA DE ORO] El botón "Historial" ya es nativo del contexto en el Cerebro.
    // Solo registraríamos acciones aquí si fueran dinámicas del Obrero.
    /*
    LaunchedEffect(beActions, beActions.size) {
        if (beActions.isNotEmpty()) {
            beBrainViewModel.setCustomActions(beActions, HUDContext.CALENDAR)
        }
    }
    */

    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            if (actionId == "goto_history") {
                showHistorySheet = true
            }
        }
    }

    // 🔥 LIMPIEZA: Al salir de la pantalla, reseteamos el estado de Be (HUD V5.1)
    DisposableEffect(Unit) {
        onDispose {
            beBrainViewModel.clearCustomActions(HUDContext.CALENDAR)
        }
    }

    CalendarScreenContent(
        filteredEvents = filteredEvents,
        groupedEvents = groupedEvents,
        daysWithEventColors = daysWithEventColors,
        selectedDate = selectedDate,
        activeFilters = activeFilters,
        showPastEvents = showPastEvents,
        nextEvents = nextEvents,
        availableCategories = availableCategories,
        onBack = onBack,
        onChatClick = onChatClick,
        onNavigateToProfile = { pid -> 
            beBrainViewModel.coordinator.updateSearchQuery("")
            onNavigateToProfile(pid)
        },
        onDateChange = { viewModel.updateSelectedDate(it) },
        onToggleFilter = { viewModel.toggleFilter(it) },
        onClearFilters = { viewModel.clearFilters() },
        onCancelEvent = { viewModel.cancelEvent(it) },
        onRescheduleEvent = { viewModel.requestReschedule(it) },
        onDeleteEvent = { viewModel.deleteEventPermanently(it.id) },
        // Props del Historial
        showHistorySheet = showHistorySheet,
        onDismissHistory = { showHistorySheet = false },
        pastEvents = pastEvents
    )
}

// ==========================================================================================
// --- CONTENIDO ESTRUCTURAL (STATELESS) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreenContent(
    filteredEvents: List<CalendarEventEntity>,
    groupedEvents: Map<String, List<CalendarEventEntity>>,
    daysWithEventColors: Map<String, Long>,
    selectedDate: Calendar,
    activeFilters: Set<String>,
    showPastEvents: Boolean,
    nextEvents: List<CalendarEventEntity>,
    availableCategories: List<ControlItem>,
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onDateChange: (Calendar) -> Unit,
    onToggleFilter: (String) -> Unit,
    onClearFilters: () -> Unit,
    onCancelEvent: (CalendarEventEntity) -> Unit,
    onRescheduleEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    // [NUEVO] Props del Historial
    showHistorySheet: Boolean = false,
    onDismissHistory: () -> Unit = {},
    pastEvents: List<CalendarEventEntity> = emptyList()
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val collapsedStates = remember { mutableStateMapOf<String, Boolean>() }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Estados de UI para Diálogos
    var showCalendarPopup by remember { mutableStateOf(false) }
    var selectedEventDetail by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var eventToCancel by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var contextEvent by remember { mutableStateOf<CalendarEventEntity?>(null) }

    // Factor de colapso para animar la cabecera
    val headerCollapse by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                BarraCabezera(
                    title = if (showPastEvents) "HISTORIAL" else "AGENDA",
                    subtitle = if (showPastEvents) "Registro completo de actividades" else "Eventos y compromisos activos",
                    emoji = "🗓️",
                    onBack = onBack,
                    onInfoClick = {}, 
                    collapseFraction = headerCollapse,
                    accentColor = MaverickBlue
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding())) {
                
                // --- 1. BARRA DE MENÚ Y FILTROS ---
                MoldeBarraMenu(
                    modifier = Modifier.weight(1f),
                    itemCount = filteredEvents.size,
                    labelCountMain = "EVENTOS",
                    labelCountSub = "Agenda Maverick",
                    showSuscritos = false,
                    showCercania = false,
                    showVista = false,
                    customActions = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Filtros de Categoría Rápidos
                            listOf(
                                Triple("cat_visita", "🛠️", MaverickBlue),
                                Triple("cat_turno", "📅", Color(0xFF9B51E0)),
                                Triple("cat_envio", "🚛", StatusOk)
                            ).forEach { (id, emoji, color) ->
                                val label = id.split("_").last().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                ActionColumnWithLabel(label = label, active = activeFilters.contains(id)) {
                                    MaverickTacticalButton(
                                        onClick = { onToggleFilter(id) },
                                        isActive = activeFilters.contains(id),
                                        accentColor = color
                                    ) { Text(emoji, fontSize = 18.sp) }
                                }
                            }

                            M3VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(0.1f))

                            // Botón de Agenda (Popup)
                            ActionColumnWithLabel(label = "Calendario", active = false) {
                                MaverickTacticalButton(onClick = { showCalendarPopup = true }, accentColor = CyberNeon) {
                                    Text("🗓️", fontSize = 18.sp)
                                }
                            }

                            M3VerticalDivider(modifier = Modifier.height(24.dp), color = Color.White.copy(0.1f))

                            // Filtros Avanzados
                            MenuFiltros(
                                activeFilters = activeFilters,
                                dynamicCategories = availableCategories,
                                refinementFilters = listOf(
                                    ControlItem("Confirmados", Icons.Default.Verified, "✅", StatusOk, "filter_verif"),
                                    ControlItem("Pendientes", Icons.Default.AccessTime, "⏳", StatusWait, "filter_fast")
                                ),
                                onAction = { onToggleFilter(it) },
                                onApply = {},
                                onClearFilters = onClearFilters
                            )
                        }
                    }
                ) {
                    // --- 2. LISTA INTEGRAL (NEXT COMMITMENT + COLLAPSIBLE GROUPS) ---
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // --- 🌟 SECCIÓN: PRÓXIMO COMPROMISO (BANNER M3) ---
                        if (nextEvents.isNotEmpty() && !showPastEvents) {
                            item(key = "next_commitment") {
                                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                    NextCommitmentCarousel(
                                        events = nextEvents,
                                        onClick = { selectedEventDetail = it },
                                        onMessageClick = { onChatClick(it) }
                                    )
                                }
                            }
                        }

                        if (groupedEvents.isEmpty()) {
                            item { 
                                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    EmptyStateCalendar() 
                                }
                            }
                        }

                        // --- 📅 SECCIONES COLAPSABLES POR FECHA ---
                        val allDates = (groupedEvents.keys + nextEvents.map { it.date } + todayStr).distinct().sorted()
                        
                        allDates.forEach { date ->
                            val events = groupedEvents[date] ?: emptyList()
                            val isToday = date == todayStr
                            
                            // Si no es hoy y no hay eventos, saltar
                            if (!isToday && events.isEmpty()) return@forEach

                            val isCollapsed = collapsedStates[date] ?: false

                            stickyHeader(key = "header_$date") {
                                ModernDateHeader(
                                    date = date,
                                    isToday = isToday,
                                    isCollapsed = isCollapsed,
                                    onToggle = { collapsedStates[date] = !isCollapsed }
                                )
                            }

                            if (!isCollapsed) {
                                if (isToday) {
                                    val nowTime = System.currentTimeMillis()
                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    
                                    val todayEventsSorted = events.sortedBy { it.time }
                                    val upcomingToday = todayEventsSorted.filter { event ->
                                        val timeMillis = try { sdf.parse("${event.date} ${event.time}")?.time ?: 0L } catch(e: Exception) { 0L }
                                        timeMillis >= nowTime
                                    }
                                    
                                    if (upcomingToday.isEmpty() && nextEvents.none { it.date == todayStr }) {
                                        item {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                                                color = Color.White.copy(0.02f),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, Color.White.copy(0.05f))
                                            ) {
                                                Text(
                                                    "Has completado tu agenda de hoy.\nPuedes ver eventos pasados en el Historial.",
                                                    modifier = Modifier.padding(20.dp),
                                                    color = Color.Gray,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }

                                    items(todayEventsSorted, key = { it.id }) { event ->
                                        val timeMillis = try { sdf.parse("${event.date} ${event.time}")?.time ?: 0L } catch(e: Exception) { 0L }
                                        val isPast = timeMillis < nowTime

                                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                            ModernEventCard(
                                                event = event,
                                                isPast = isPast,
                                                onClick = { selectedEventDetail = event },
                                                onLongClick = { contextEvent = event },
                                                onMessageClick = { onChatClick(event.providerId) }
                                            )
                                        }
                                    }
                                } else {
                                    items(events, key = { it.id }) { event ->
                                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                            ModernEventCard(
                                                event = event,
                                                onClick = { selectedEventDetail = event },
                                                onLongClick = { contextEvent = event },
                                                onMessageClick = { onChatClick(event.providerId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- POPUPS Y MODALES ---
        if (showCalendarPopup) {
            CalendarPopup(
                selectedDate = selectedDate,
                daysWithEventColors = daysWithEventColors,
                onDismiss = { showCalendarPopup = false },
                onDateSelected = { date ->
                    onDateChange(date)
                    showCalendarPopup = false
                    coroutineScope.launch {
                        val dStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
                        val idx = calculateScrollToIndex(dStr, groupedEvents, collapsedStates)
                        if (idx != -1) listState.animateScrollToItem(idx)
                    }
                }
            )
        }

        if (contextEvent != null) {
            EventContextMenu(
                onDismiss = { contextEvent = null },
                onAction = { action ->
                    when(action) {
                        "detail" -> selectedEventDetail = contextEvent
                        "cancel" -> eventToCancel = contextEvent
                        "delete" -> onDeleteEvent(contextEvent!!)
                    }
                    contextEvent = null
                }
            )
        }

        if (selectedEventDetail != null) {
            EventDetailPopup(
                event = selectedEventDetail!!,
                onDismiss = { selectedEventDetail = null },
                onChatClick = { onChatClick(it); selectedEventDetail = null },
                onRescheduleClick = { onRescheduleEvent(it); selectedEventDetail = null },
                onCancelClick = { eventToCancel = it; selectedEventDetail = null },
                onProviderClick = { onNavigateToProfile(it); selectedEventDetail = null }
            )
        }

        if (eventToCancel != null) {
            CancelVisitConfirmModal(
                event = eventToCancel!!,
                onConfirm = { onCancelEvent(it); eventToCancel = null },
                onDismiss = { eventToCancel = null }
            )
        }

        // --- SECCIÓN: HISTORIAL DE EVENTOS (BOTTOM SHEET) ---
        if (showHistorySheet) {
            HistoryBottomSheet(
                events = pastEvents,
                onDismiss = onDismissHistory
            )
        }
    }
}

// --- FUNCIONES AUXILIARES ---

private fun calculateScrollToIndex(dateStr: String, grouped: Map<String, List<CalendarEventEntity>>, collapsed: Map<String, Boolean>): Int {
    var currentIdx = 0
    val sortedDates = grouped.keys.sorted()
    sortedDates.forEach { date ->
        if (date == dateStr) return currentIdx
        currentIdx += 1 // Header
        if (!(collapsed[date] ?: false)) {
            currentIdx += grouped[date]?.size ?: 0
        }
    }
    return -1
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun CalendarScreenPreview() {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val sampleEvents = listOf(
        CalendarEventEntity(
            id = "1",
            date = today,
            time = "10:00",
            type = EventType.VISIT,
            title = "Visita Técnica Aire Acondicionado",
            provider = "Juan Pérez",
            providerId = "p1",
            address = "Calle Falsa 123",
            status = VisitStatus.CONFIRMED,
            categoryName = "Climatización",
            categoryEmoji = "❄️"
        ),
        CalendarEventEntity(
            id = "2",
            date = today,
            time = "14:30",
            type = EventType.APPOINTMENT,
            title = "Turno Odontología",
            provider = "Dra. García",
            providerId = "p2",
            address = "Av. Siempre Viva 742",
            status = VisitStatus.PENDING,
            categoryName = "Salud",
            categoryEmoji = "🦷"
        )
    )

    MyApplicationTheme {
        CalendarScreenContent(
            filteredEvents = sampleEvents,
            groupedEvents = mapOf(today to sampleEvents),
            daysWithEventColors = mapOf(today to 0xFF2E91FF),
            selectedDate = Calendar.getInstance(),
            activeFilters = emptySet(),
            showPastEvents = false,
            nextEvents = sampleEvents.take(1),
            availableCategories = listOf(
                ControlItem("Visitas", Icons.Default.Build, "🛠️", Color(0xFF2E91FF), "cat_visita"),
                ControlItem("Turnos", Icons.Default.CalendarToday, "📅", Color(0xFF9B51E0), "cat_turno")
            ),
            onBack = {},
            onChatClick = {},
            onNavigateToProfile = {},
            onDateChange = {},
            onToggleFilter = {},
            onClearFilters = {},
            onCancelEvent = {},
            onRescheduleEvent = {},
            onDeleteEvent = {}
        )
    }
}

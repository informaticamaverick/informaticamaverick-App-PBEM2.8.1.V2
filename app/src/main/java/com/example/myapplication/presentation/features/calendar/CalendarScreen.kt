package com.example.myapplication.presentation.features.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.data.local.entity.CalendarEventEntity
import com.example.myapplication.core.data.local.entity.EventType
import com.example.myapplication.core.data.local.entity.VisitStatus
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.global.HUDContext
import com.example.myapplication.presentation.registry.BeDictionary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- CONSTANTES DE DISEÑO MAVERICK ELITE ---
 */
private val DarkBg = Color(0xFF020408)

// ==========================================================================================
// --- 📅 PANTALLA PRINCIPAL: CALENDARIO (SMART ORCHESTRATOR) ---
// ==========================================================================================

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onChatClick: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
) {
    // --- SUSCRIPCIÓN A LOS OBREROS (SSOT) ---
    val filteredEvents by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val groupedEvents by viewModel.groupedEvents.collectAsStateWithLifecycle()
    val daysWithEventColors by viewModel.daysWithEventColors.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeFilters by viewModel.activeFilters.collectAsStateWithLifecycle()
    val showPastEvents by viewModel.showPastEvents.collectAsStateWithLifecycle()
    val nextEvents by viewModel.nextEvents.collectAsStateWithLifecycle()
    val allEventsCount by viewModel.allEventsCount.collectAsStateWithLifecycle()
    val shortcuts by viewModel.shortcuts.collectAsStateWithLifecycle()
    
    // --- NUEVOS COMPONENTES ELITE ---
    val filterDropdownItems by viewModel.filterDropdownItems.collectAsStateWithLifecycle()
    val sortDropdownItems by viewModel.sortDropdownItems.collectAsStateWithLifecycle()
    val activeSortCriteria by viewModel.activeSortCriteria.collectAsStateWithLifecycle()
    val beActionIds by viewModel.beActionIds.collectAsStateWithLifecycle()

    // Estados para el Historial (Integración HUD)
    val pastEvents by viewModel.pastEvents.collectAsStateWithLifecycle()
    var showHistorySheet by remember { mutableStateOf(false) }

    // 🔥 SINCRONIZACIÓN DE HUD
    LaunchedEffect(showHistorySheet) {
        beBrainViewModel.setSheetVisible(showHistorySheet)
    }

    LaunchedEffect(beActionIds) {
        beBrainViewModel.setCustomActionIds(beActionIds, HUDContext.CALENDAR)
    }

    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("calendar")
        beBrainViewModel.coordinator.updateSearchQuery("")
        
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                "goto_history" -> showHistorySheet = true
                "clear_filters" -> viewModel.clearFilters()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            beBrainViewModel.clearCustomActions(HUDContext.CALENDAR)
        }
    }

    CalendarScreenContent(
        filteredEvents = filteredEvents,
        allEventsCount = allEventsCount,
        groupedEvents = groupedEvents,
        daysWithEventColors = daysWithEventColors,
        selectedDate = selectedDate,
        activeFilters = activeFilters,
        showPastEvents = showPastEvents,
        nextEvents = nextEvents,
        shortcuts = shortcuts,
        filterDropdownItems = filterDropdownItems,
        sortDropdownItems = sortDropdownItems,
        activeSorts = activeSortCriteria,
        onBack = onBack,
        onChatClick = onChatClick,
        onNavigateToProfile = { pid -> 
            beBrainViewModel.coordinator.updateSearchQuery("")
            onNavigateToProfile(pid)
        },
        onDateChange = { viewModel.updateSelectedDate(it) },
        onToggleFilter = { viewModel.toggleFilter(it) },
        onManageShortcuts = { id, add -> viewModel.manageShortcut(id, add) },
        onClearFilters = { viewModel.clearFilters() },
        onCancelEvent = { viewModel.cancelEvent(it) },
        onRescheduleEvent = { viewModel.requestReschedule(it) },
        onDeleteEvent = { viewModel.deleteEventPermanently(it.id) },
        showHistorySheet = showHistorySheet,
        onDismissHistory = { showHistorySheet = false },
        pastEvents = pastEvents,
    )
}

// ==========================================================================================
// --- 🎨 CONTENIDO ESTRUCTURAL (STATELESS UI) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreenContent(
    filteredEvents: List<CalendarEventEntity>,
    allEventsCount: Int,
    groupedEvents: Map<String, List<CalendarEventEntity>>,
    daysWithEventColors: Map<String, Long>,
    selectedDate: Calendar,
    activeFilters: Set<String>,
    showPastEvents: Boolean,
    nextEvents: List<CalendarEventEntity>,
    shortcuts: List<FilterSortItem> = emptyList(),
    filterDropdownItems: List<DropdownItemData>,
    sortDropdownItems: List<DropdownItemData>,
    activeSorts: List<String> = emptyList(),
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onDateChange: (Calendar) -> Unit,
    onToggleFilter: (String) -> Unit,
    onManageShortcuts: (String, Boolean) -> Unit = { _, _ -> },
    onClearFilters: () -> Unit,
    onCancelEvent: (CalendarEventEntity) -> Unit,
    onRescheduleEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit,
    showHistorySheet: Boolean = false,
    onDismissHistory: () -> Unit = {},
    pastEvents: List<CalendarEventEntity> = emptyList()
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    val collapsedStates = remember { mutableStateMapOf<String, Boolean>() }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // --- ESTADOS DE UI ---
    var showCalendarPopup by remember { mutableStateOf(false) }
    var selectedEventDetail by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var eventToCancel by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var contextEvent by remember { mutableStateOf<CalendarEventEntity?>(null) }

    // --- LÓGICA DE SCROLL Y COLAPSO ---
    var scrollAccumulator by remember { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 430f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return if (scrollAccumulator >= 430f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val cardsHideFraction = (scrollAccumulator / 180f).coerceIn(0f, 1f)
    val collapseFraction = ((scrollAccumulator - 180f) / 250f).coerceIn(0f, 1f)

    // --- CABECERA DINÁMICA ---
    val isDateSortActive = activeFilters.contains("sort_date") || activeFilters.none { it.startsWith("sort_") }
    val currentVisibleLabel by remember(filteredEvents.size, isDateSortActive, groupedEvents) {
        derivedStateOf {
            if (!isDateSortActive) "${filteredEvents.size} COMPROMISOS"
            else {
                val firstVisibleIndex = listState.firstVisibleItemIndex
                var count = 0
                var foundDate = ""
                for ((date, events) in groupedEvents) {
                    if (date.isNotEmpty()) count++ 
                    if (firstVisibleIndex < count + events.size) {
                        foundDate = date
                        break
                    }
                    if (collapsedStates[date] != true) count += events.size
                }
                foundDate.ifEmpty { "AGENDA" }.uppercase()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBg).nestedScroll(nestedScrollConnection)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                val visuals = BeDictionary.Contexts["calendar"]!!
                BarraCabezera(
                    title = if (showPastEvents) "HISTORIAL" else visuals.title,
                    subtitle = if (showPastEvents) "Registro completo de actividades" else visuals.subtitle,
                    emoji = visuals.emoji,
                    onBack = onBack,
                    collapseFraction = collapseFraction,
                    accentColor = visuals.accentColor
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
                if (!showPastEvents) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 8.dp * (1f - cardsHideFraction))
                            .graphicsLayer { alpha = 1f - cardsHideFraction; translationY = -10.dp.toPx() * cardsHideFraction }
                            .height(130.dp * (1f - cardsHideFraction))
                    ) {
                        NextCommitmentCarousel(
                            events = nextEvents,
                            onClick = { selectedEventDetail = it },
                            onMessageClick = onChatClick
                        )
                    }
                }

                // --- 2. MODULO DE FILTROS PREMIUM ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 4.dp * (1f - cardsHideFraction))
                        .graphicsLayer { alpha = 1f - cardsHideFraction; translationY = -20.dp.toPx() * cardsHideFraction }
                        .height(100.dp * (1f - cardsHideFraction)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoldePremiumFilterCard(
                        label = "Filtrar por",
                        dropdownItems = filterDropdownItems,
                        shortcutItems = shortcuts,
                        activeFilters = activeFilters,
                        onToggle = onToggleFilter,
                        onManageShortcuts = onManageShortcuts,
                        onExpandChanged = { isFilterMenuExpanded = it },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedVisibility(
                        visible = !isFilterMenuExpanded,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        MoldePremiumSortCard(
                            label = "Ordenar por",
                            dropdownItems = sortDropdownItems,
                            shortcutItems = emptyList(),
                            activeSorts = activeSorts,
                            onToggle = onToggleFilter,
                            onManageShortcuts = { _, _ -> }
                        )
                    }
                }

                // --- 3. LISTADO DE EVENTOS ELITE ---
                ListaMoldeV2(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    titulo = if (showPastEvents) "HISTORIAL" else "PRÓXIMOS COMPROMISOS",
                    subtitulo = "SISTEMA MAVERICK",
                    compactInfo = currentVisibleLabel,
                    itemCount = allEventsCount,
                    acciones = {
                        if (activeFilters.isNotEmpty()) {
                            BotonesCabecera.Limpiar(onClearFilters)
                        }
                    }
                ) {
                    if (groupedEvents.isEmpty() && nextEvents.isEmpty()) {
                        item { Box(modifier = Modifier.padding(horizontal = 8.dp)) { EmptyStateCalendar() } }
                    }

                    val allDates = (groupedEvents.keys + todayStr).distinct().sorted()

                    allDates.forEach { date ->
                        val events = groupedEvents[date] ?: emptyList()
                        val isToday = date == todayStr
                        if (events.isEmpty()) return@forEach

                        val isCollapsed = collapsedStates[date] ?: false

                        stickyHeader(key = "header_$date") {
                            SeparadorFechaPremium(
                                fecha = if (isToday) "Hoy, $date" else date,
                                isExpanded = !isCollapsed,
                                onToggle = { collapsedStates[date] = !isCollapsed }
                            )
                        }

                        if (!isCollapsed) {
                            items(events, key = { it.id }) { event ->
                                val nowTime = System.currentTimeMillis()
                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val timeMillis = try { sdf.parse("${event.date} ${event.time}")?.time ?: 0L } catch(_: Exception) { 0L }
                                val isPast = timeMillis < nowTime

                                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    ModernEventCard(
                                        event = event,
                                        isPast = isPast,
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

        // --- OVERLAYS ---
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

        SheetEmergenteVertical(
            isVisible = showHistorySheet,
            onClose = onDismissHistory,
            title = "Historial de Actividades",
            helperText = "Registro completo de eventos",
            emoji = "📜",
            topOffset = 150.dp,
            isScrollable = false
        ) {
            if (pastEvents.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay eventos pasados registrados", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
                    items(pastEvents, key = { it.id }) { event ->
                        ModernEventCard(
                            event = event,
                            isPast = true,
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

// --- FUNCIONES AUXILIARES ---

private fun calculateScrollToIndex(dateStr: String, grouped: Map<String, List<CalendarEventEntity>>, collapsed: Map<String, Boolean>): Int {
    var currentIdx = 0
    val sortedDates = grouped.keys.sorted()
    sortedDates.forEach { date ->
        if (date == dateStr) return currentIdx
        currentIdx += 1
        if (collapsed[date] != true) {
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
            id = "1", date = today, time = "10:00", type = EventType.VISIT,
            title = "Visita Técnica Aire Acondicionado", provider = "Juan Pérez", providerId = "p1",
            address = "Calle Falsa 123", status = VisitStatus.CONFIRMED, categoryName = "Climatización", categoryEmoji = "❄️"
        )
    )

    MyApplicationTheme {
        CalendarScreenContent(
            filteredEvents = sampleEvents, allEventsCount = 10, groupedEvents = mapOf(today to sampleEvents),
            daysWithEventColors = mapOf(today to 0xFF2E91FF), selectedDate = Calendar.getInstance(),
            activeFilters = emptySet(), showPastEvents = false, nextEvents = sampleEvents,
            filterDropdownItems = emptyList(), sortDropdownItems = emptyList(),
            activeSorts = emptyList(),
            onBack = {}, onChatClick = {}, onNavigateToProfile = {}, onDateChange = {},
            onToggleFilter = {}, onClearFilters = {}, onCancelEvent = {},
            onRescheduleEvent = {}, onDeleteEvent = {}
        )
    }
}

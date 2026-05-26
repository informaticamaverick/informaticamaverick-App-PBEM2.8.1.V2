package com.example.myapplication.presentation.client

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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
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
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val filteredEvents by viewModel.filteredEvents.collectAsStateWithLifecycle()
    val daysWithEventColors by viewModel.daysWithEventColors.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeFilters by viewModel.activeFilters.collectAsStateWithLifecycle()
    val showPastEvents by viewModel.showPastEvents.collectAsStateWithLifecycle()
    val nextEvent by viewModel.nextEvent.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()

    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    val userState by beBrainViewModel.userState.collectAsStateWithLifecycle()
    val currentUserId = userState?.id ?: "guest"

    // --- 1. SINCRONIZACIÓN CON EL ASISTENTE BE ---
    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("calendar")
        // [PASO CRÍTICO] Limpiar búsqueda global al entrar para evitar que la agenda aparezca vacía 
        // por un filtro de búsqueda residual de otra pantalla (ej: Home).
        beBrainViewModel.coordinator.updateSearchQuery("")
        // También limpiamos la categoría seleccionada para empezar de cero
        beBrainViewModel.coordinator.selectCategory(null)
    }

    CalendarScreenContent(
        filteredEvents = filteredEvents,
        daysWithEventColors = daysWithEventColors,
        selectedDate = selectedDate,
        activeFilters = activeFilters,
        showPastEvents = showPastEvents,
        nextEvent = nextEvent,
        availableCategories = availableCategories,
        onBack = onBack,
        onChatClick = onChatClick,
        onDateChange = { viewModel.updateSelectedDate(it) },
        onToggleFilter = { id ->
            if (id.startsWith("cat_")) {
                val catName = id.removePrefix("cat_")
                val category = allCategories.find { it.name == catName }
                if (category != null) {
                    beBrainViewModel.coordinator.selectCategory(
                        if (activeFilters.contains(id)) null else category
                    )
                }
            }
            viewModel.toggleFilter(id)
        },
        onTogglePastEvents = { viewModel.togglePastEvents(it) },
        onClearFilters = {
            viewModel.clearFilters()
            beBrainViewModel.coordinator.updateSearchQuery("")
            beBrainViewModel.coordinator.selectCategory(null)
        },
        onCancelEvent = { event ->
            viewModel.cancelEvent(event, currentUserId)
        },
        onRescheduleEvent = { event ->
            viewModel.requestReschedule(event, currentUserId)
        },
        onDeleteEvent = { event ->
            viewModel.deleteEventPermanently(event.id)
        }
    )
}

// ==========================================================================================
// --- CONTENIDO STATELESS ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreenContent(
    filteredEvents: List<CalendarEventEntity>,
    daysWithEventColors: Map<String, Long>,
    selectedDate: Calendar,
    activeFilters: Set<String>,
    showPastEvents: Boolean,
    nextEvent: CalendarEventEntity?,
    availableCategories: List<ControlItem> = emptyList(), // 🔥 [NUEVO] Categorías dinámicas
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    onDateChange: (Calendar) -> Unit,
    onToggleFilter: (String) -> Unit,
    onTogglePastEvents: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onCancelEvent: (CalendarEventEntity) -> Unit,
    onRescheduleEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (CalendarEventEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Pull to Refresh state (M3)
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    // --- ESTADOS DE MODALES ---
    var showCalendarPopup by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<CalendarEventEntity?>(null) } 
    var eventToCancel by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var eventContextItem by remember { mutableStateOf<CalendarEventEntity?>(null) }

    // --- 🏗️ SECCIÓN: LÓGICA DE ANIMACIÓN DE CABECERA ---
    val scrollFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset.toFloat() / 300f).coerceIn(0f, 1f)
        }
    }

    // Agrupación de eventos por fecha
    val groupedEvents = remember(filteredEvents) {
        filteredEvents.groupBy { it.date }
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Calendar.getInstance().time)

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                BarraCabezera(
                    title = if (showPastEvents) "Agenda Completa" else "Mi Agenda",
                    subtitle = if (showPastEvents) "Incluye eventos pasados" else "Control de visitas y turnos",
                    emoji = "📅",
                    onBack = onBack,
                    onInfoClick = { /* Info */ },
                    collapseFraction = scrollFraction,
                    accentColor = MaverickBlue
                )
            }
        ) { paddingValues ->
            val safePadding = PaddingValues(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current).coerceAtLeast(0.dp),
                top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current).coerceAtLeast(0.dp),
                bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
            )

            Column(modifier = Modifier.fillMaxSize().padding(safePadding)) {
                
                // ==========================================================================================
                // --- 🏗️ SECCIÓN: MOLDE DE BARRA DE MENU (CON FILTROS Y CALENDARIO) ---
                // ==========================================================================================
                MoldeBarraMenu(
                    itemCount = filteredEvents.size,
                    labelCountMain = "EVENTOS",
                    labelCountSub = "Agenda Maverick",
                    showSuscritos = false,
                    showCercania = false,
                    showVista = false,
                    customActions = {
                        // BOTONES DE FILTRO DE TIPO
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            
                            val filterOptions = listOf(
                                Triple("cat_visita", "🛠️", "Visitas"),
                                Triple("cat_turno", "📅", "Turnos"),
                                Triple("cat_envio", "🚛", "Envíos")
                            )

                            filterOptions.forEach { (id, emoji, label) ->
                                ActionColumnWithLabel(label = label, active = activeFilters.contains(id)) {
                                    MaverickTacticalButton(
                                        onClick = { onToggleFilter(id) },
                                        isActive = activeFilters.contains(id),
                                        accentColor = if (id == "cat_visita") MaverickBlue else if (id == "cat_turno") Color(0xFF9B51E0) else Color(0xFF10B981)
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                    }
                                }
                            }

                            M3VerticalDivider(modifier = Modifier.height(30.dp).padding(horizontal = 4.dp), color = Color.White.copy(alpha = 0.1f))

                            // BOTÓN DE CALENDARIO (POPUP)
                            ActionColumnWithLabel(label = "Agenda", active = false) {
                                MaverickTacticalButton(
                                    onClick = { showCalendarPopup = true },
                                    accentColor = NeonCyber
                                ) {
                                    Text("🗓️", fontSize = 18.sp)
                                }
                            }

                            M3VerticalDivider(modifier = Modifier.height(30.dp).padding(horizontal = 4.dp), color = Color.White.copy(alpha = 0.1f))

                            // FILTRO TORNADO (DYNAMIC FILTERS)
                            MenuFiltros(
                                activeFilters = activeFilters,
                                dynamicCategories = availableCategories, // 🔥 Conectamos categorías dinámicas
                                refinementFilters = listOf(
                                    ControlItem("Confirmados", Icons.Default.Verified, "✅", StatusConfirmed, "filter_verif"),
                                    ControlItem("Pendientes", Icons.Default.AccessTime, "⏳", StatusPending, "filter_fast")
                                ),
                                onAction = { onToggleFilter(it) },
                                onApply = {},
                                onClearFilters = onClearFilters
                            )
                        }
                    }
                ) {
                    // --- LISTA DE EVENTOS CON PULL TO REFRESH (M3) ---
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            coroutineScope.launch {
                                isRefreshing = true
                                onTogglePastEvents(!showPastEvents)
                                delay(1200)
                                isRefreshing = false
                            }
                        },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            // --- 🌟 SECCIÓN: PRÓXIMO COMPROMISO (GOOGLE STYLE) ---
                            if (nextEvent != null && !showPastEvents) {
                                item {
                                    NextEventBanner(
                                        event = nextEvent,
                                        onClick = { selectedEvent = nextEvent }
                                    )
                                }
                            }

                            if (groupedEvents.isEmpty()) {
                                item { EmptyStateCalendar() }
                            }

                            groupedEvents.forEach { (date, events) ->
                                stickyHeader {
                                    DateHeaderPro(date, isToday = date == todayStr)
                                }

                                items(events, key = { it.id }) { event ->
                                    EventCardPremium(
                                        event = event,
                                        onClick = { selectedEvent = event },
                                        onLongClick = { eventContextItem = event },
                                        onMessageClick = { onChatClick(event.providerId) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // --- MODALES Y POPUPS ---
        // =========================================================================

        // 1. POPUP DE CALENDARIO MODERNO
        if (showCalendarPopup) {
            CalendarPopup(
                selectedDate = selectedDate,
                daysWithEventColors = daysWithEventColors,
                onDateSelected = { date ->
                    onDateChange(date)
                    showCalendarPopup = false
                    // Scroll a la fecha si existe en la lista
                    coroutineScope.launch {
                        val dateStr = dateFormat.format(date.time)
                        
                        // Calculamos el índice del header o del primer item de esa fecha
                        var targetIndex = -1
                        var currentIdx = 0
                        
                        // Necesitamos una lista ordenada de las fechas presentes en groupedEvents
                        val sortedDates = groupedEvents.keys.sorted()
                        
                        sortedDates.forEach { groupedDate ->
                            if (groupedDate == dateStr) {
                                targetIndex = currentIdx
                                return@forEach
                            }
                            currentIdx += 1 // Header (stickyHeader)
                            currentIdx += groupedEvents[groupedDate]?.size ?: 0 // Items
                        }

                        if (targetIndex != -1) {
                            listState.animateScrollToItem(targetIndex)
                        } else {
                            // Si la fecha no está en la lista (ej: no hay eventos ese día)
                        }
                    }
                },
                onDismiss = { showCalendarPopup = false }
            )
        }

        // 2. MENU CONTEXTUAL (PRESIÓN LARGA)
        if (eventContextItem != null) {
            EventContextMenu(
                onDismiss = { eventContextItem = null },
                onAction = { action ->
                    when (action) {
                        "detail" -> selectedEvent = eventContextItem
                        "cancel" -> eventToCancel = eventContextItem
                        "delete" -> onDeleteEvent(eventContextItem!!)
                    }
                    eventContextItem = null
                }
            )
        }

        // 3. MODAL DE DETALLES
        if (selectedEvent != null) {
            EventDetailsModal(
                event = selectedEvent!!,
                onDismiss = { selectedEvent = null },
                onChatClick = {
                    selectedEvent = null
                    onChatClick(it)
                },
                onRescheduleClick = { event ->
                    onRescheduleEvent(event)
                    selectedEvent = null
                    onChatClick(event.providerId)
                },
                onCancelClick = { event ->
                    eventToCancel = event
                    selectedEvent = null
                }
            )
        }

        // 4. CONFIRMACIÓN CANCELACIÓN
        if (eventToCancel != null) {
            CancelVisitConfirmModal(
                event = eventToCancel!!,
                onConfirm = { event ->
                    onCancelEvent(event)
                    eventToCancel = null
                },
                onDismiss = { eventToCancel = null }
            )
        }
    }
}

// ==========================================================================================
// --- COMPONENTES ESPECÍFICOS V2 ---
// ==========================================================================================

@Composable
fun DateHeaderPro(date: String, isToday: Boolean = false) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    
    val dateObj = try { inputFormat.parse(date) } catch (_: Exception) { null }
    val formattedDate = dateObj?.let { outputFormat.format(it).uppercase() } ?: date

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg) // Fondo sólido para evitar superposición transparente
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (isToday) 6.dp else 4.dp, 18.dp)
                    .clip(CircleShape)
                    .background(if (isToday) NeonCyber else MaverickBlue)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isToday) "HOY - $formattedDate" else formattedDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isToday) NeonCyber else Color.White,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
fun NextEventBanner(event: CalendarEventEntity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaverickBlue.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaverickBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.radialGradient(listOf(MaverickBlue.copy(alpha = 0.3f), Color.Transparent)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(event.categoryEmoji ?: "⚡", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (event.categoryName ?: "ENFOQUE TÁCTICO").uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = MaverickBlue,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = event.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Próximo a las ${event.time} con ${event.provider}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaverickBlue.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventCardPremium(
    event: CalendarEventEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    val isCancelled = event.status == VisitStatus.CANCELLED
    val eventColor = Color(event.type.colorLong)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // --- 🌈 GLOW LATERAL COMPLETO ---
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(eventColor, eventColor.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
                    .align(Alignment.CenterStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp), 
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- 🕒 SECCIÓN IZQUIERDA: HORA OPTIMIZADA ---
                Column(
                    modifier = Modifier.width(65.dp), 
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Limpiamos la hora por si trae "hs" incluido
                    val cleanTime = event.time.lowercase().replace("hs", "").trim()
                    Text(
                        text = cleanTime.ifEmpty { "--:--" }, 
                        fontSize = 22.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if (isCancelled) Color.Gray else Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "HS", 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if (isCancelled) Color.Gray.copy(alpha = 0.5f) else MaverickBlue,
                        letterSpacing = 1.sp
                    )
                }

                M3VerticalDivider(
                    modifier = Modifier.height(50.dp).padding(horizontal = 10.dp), 
                    color = Color.White.copy(alpha = 0.12f)
                )

                // --- 📝 SECCIÓN CENTRAL: INFO ---
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val displayEmoji = event.categoryEmoji ?: event.type.emoji
                            val displayTypeLabel = (event.categoryName ?: event.type.label).uppercase()
                            
                            // Si el label es un UUID o muy largo, usamos el label del tipo por defecto
                            val safeTypeLabel = if (displayTypeLabel.length > 20 || displayTypeLabel.contains("-")) {
                                event.type.label.uppercase()
                            } else {
                                displayTypeLabel
                            }

                            Text(displayEmoji, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = safeTypeLabel, 
                                color = eventColor, 
                                fontSize = 8.sp, 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = 0.5.sp
                            )
                        }
                        
                        // SERVICIO / CATEGORÍA (Ej: Plomero, Electricista)
                        // Mostramos el badge de categoría solo si tenemos un nombre válido (no UUID)
                        val catNameRaw = event.categoryName ?: event.title.split("|").firstOrNull()
                        val isCatNameValid = catNameRaw != null && catNameRaw.length < 25 && !catNameRaw.contains("-")
                        
                        if (isCatNameValid) {
                            Surface(
                                color = eventColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, eventColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = catNameRaw.uppercase(),
                                    color = eventColor,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    
                    // Título Limpio (Evita IDs al inicio)
                    val cleanTitle = if (event.title.contains("|")) {
                        event.title.split("|").lastOrNull()?.trim() ?: event.title
                    } else if (event.title.length > 30 && event.title.contains("-")) {
                        event.type.label // Fallback si el título es un UUID
                    } else {
                        event.title
                    }

                    Text(
                        text = cleanTitle, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis, 
                        textDecoration = if(isCancelled) TextDecoration.LineThrough else TextDecoration.None
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // DIRECCIÓN (Prioridad visual sobre el nombre)
                    Text(
                        text = event.address, 
                        fontSize = 10.sp, 
                        color = Color.Gray, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )

                    // PRESTADOR CON AVATAR AL LADO DEL NOMBRE
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        if (event.providerPhotoUrl != null) {
                            AsyncImage(
                                model = event.providerPhotoUrl, 
                                contentDescription = null, 
                                modifier = Modifier.size(20.dp).clip(CircleShape).border(0.5.dp, Color.White.copy(0.2f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(event.avatarColorLong)), 
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = event.provider.take(1).uppercase(), 
                                    fontSize = 9.sp, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.provider, 
                            fontSize = 12.sp, 
                            color = Color.White.copy(alpha = 0.8f), 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // --- 💬 ACCIÓN RÁPIDA: MENSAJE ---
                MaverickTacticalButton(
                    onClick = onMessageClick,
                    size = 44.dp,
                    accentColor = MaverickBlue
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message, 
                        contentDescription = "Chat", 
                        tint = MaverickBlue, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CalendarPopup(
    selectedDate: Calendar,
    daysWithEventColors: Map<String, Long>,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    var internalDate by remember { mutableStateOf((selectedDate.clone() as Calendar)) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = CardSurface,
            border = BorderStroke(1.dp, MaverickBlue.copy(alpha = 0.3f)),
            shadowElevation = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                CalendarHeaderPro(
                    currentDate = internalDate,
                    isExpanded = true,
                    onToggleExpand = {},
                    onPreviousMonth = { internalDate = (internalDate.clone() as Calendar).apply { add(Calendar.MONTH, -1) } },
                    onNextMonth = { internalDate = (internalDate.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeekDaysHeaderPro()
                Spacer(modifier = Modifier.height(12.dp))
                CalendarGridPro(
                    currentDate = internalDate,
                    selectedDate = selectedDate,
                    daysWithEventColors = daysWithEventColors,
                    dateFormat = dateFormat,
                    onDayClick = { day ->
                        val newDate = (internalDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        onDateSelected(newDate)
                    }
                )
            }
        }
    }
}

@Composable
fun EventContextMenu(
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ContextItem(label = "Ver Detalle", emoji = "🔍", onClick = { onAction("detail") })
                ContextItem(label = "Anular Cita", emoji = "⚠️", color = ErrorRed, onClick = { onAction("cancel") })
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.05f))
                ContextItem(label = "Eliminar Registro", emoji = "🗑️", color = Color.Gray, onClick = { onAction("delete") })
            }
        }
    }
}

@Composable
fun ContextItem(label: String, emoji: String, color: Color = Color.White, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyStateCalendar() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("☕", fontSize = 60.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("AGENDA DESPEJADA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("No hay eventos programados con estos filtros.", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// ==========================================================================================
// --- REUTILIZACIÓN DE COMPONENTES ORIGINALES ---
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggleExpand).padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${monthNames[currentDate.get(Calendar.MONTH)]} ${currentDate.get(Calendar.YEAR)}".uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
            if (isExpanded) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = MaverickBlue, modifier = Modifier.size(20.dp).rotate(rotation))
            }
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
    daysWithEventColors: Map<String, Long>,
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
                        val eventColorLong = daysWithEventColors[dateStr]
                        val hasEvent = eventColorLong != null

                        val dotColor = if (hasEvent) Color(eventColorLong) else Color.Transparent

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
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) DarkBg else dotColor))
            }
        }
    }
}

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
                                Text(event.categoryEmoji ?: event.type.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text((event.categoryName ?: event.type.label).uppercase(), color = eventColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
                                AsyncImage(model = event.providerPhotoUrl, contentDescription = null, modifier = Modifier.size(36.dp).clip(CircleShape).border(1.dp, Color.White.copy(0.2f), CircleShape), contentScale = ContentScale.Crop)
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
                            MaverickTacticalButton(onClick = { onChatClick(event.providerId) }, size = 36.dp, accentColor = MaverickBlue) {
                                Icon(Icons.AutoMirrored.Filled.Message, null, tint = MaverickBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    if (event.status != VisitStatus.CANCELLED) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { onCancelClick(event) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f), contentColor = ErrorRed)) {
                                Text("CANCELAR", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                            Button(onClick = { onRescheduleClick(event) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = eventColor, contentColor = Color.White)) {
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
            Surface(modifier = Modifier.fillMaxWidth(0.85f), shape = RoundedCornerShape(32.dp), color = CardSurface, border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)), shadowElevation = 20.dp) {
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
        val dummyEvents = listOf(
            CalendarEventEntity("1", "2023-11-15", "10:00", EventType.VISIT, "Revisión de Sistema", "Tech Solutions Inc.", "tech_solutions_1", "Calle Falsa 123", VisitStatus.CONFIRMED, "Tecnología", "💻", null, 0xFF42A5F5L),
            CalendarEventEntity("2", "2023-11-15", "14:30", EventType.APPOINTMENT, "Consulta Médica", "Dr. John Smith", "dr_smith_2", "Av. Siempre Viva 742", VisitStatus.PENDING, "Salud", "🏥", null, 0xFF66BB6AL),
            CalendarEventEntity("3", "2023-11-16", "09:00", EventType.SHIPPING, "Entrega de Paquete", "Envios Express", "envios_express_3", "Ruta 40 Km 10", VisitStatus.CONFIRMED, "Logística", "📦", null, 0xFFFFA726L)
        )

        CalendarScreenContent(
            filteredEvents = dummyEvents,
            daysWithEventColors = dummyEvents.associate { it.date to it.type.colorLong },
            selectedDate = Calendar.getInstance(),
            activeFilters = emptySet(),
            showPastEvents = false,
            nextEvent = dummyEvents.first(),
            availableCategories = listOf(
                ControlItem("Tecnología", null, "💻", Color(EventType.VISIT.colorLong), "cat_Tecnología"),
                ControlItem("Salud", null, "🏥", Color(EventType.APPOINTMENT.colorLong), "cat_Salud")
            ),
            onBack = {},
            onChatClick = {},
            onDateChange = {},
            onToggleFilter = {},
            onTogglePastEvents = {},
            onCancelEvent = {},
            onRescheduleEvent = {},
            onDeleteEvent = {},
            onClearFilters = {}
        )
    }
}

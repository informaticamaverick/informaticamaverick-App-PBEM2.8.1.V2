package com.example.myapplication.ui.pantallas.calendar

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.layout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.estilos.AppIcons
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorListaPantallaCompleta
import com.example.myapplication.ui.componentes.sistema.menu.v3.*
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MoldeMenuArmadorV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuSectionHeaderV3
import com.example.myapplication.ui.componentes.sistema.EsqueletoConcurso
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.cabecera.ColumnaTituloSeccionV3
import com.example.myapplication.ui.componentes.sistema.cabecera.EmojiImpactoV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPantallas
import com.example.myapplication.ui.componentes.sistema.menu.MiniCalendarioElite
import com.example.myapplication.viewmodel.calendar.CalendarViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val DarkBg = Color(0xFF020408)

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onChatClick: (String) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
    userViewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    BeCerebroViewModel: BeCerebroViewModel = hiltViewModel()
) {
    val groupedEvents by viewModel.groupedEvents.collectAsStateWithLifecycle()
    val pastEvents by viewModel.pastEvents.collectAsStateWithLifecycle()
    val perfiles by userViewModel.identidadesSoberanas.collectAsStateWithLifecycle()
    val selectedPerfilId by BeCerebroViewModel.coordinador.idPerfilSeleccionado.collectAsStateWithLifecycle()
    val filtrosActivos by viewModel.filtrosActivos.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val allEventDates by viewModel.allEventDates.collectAsStateWithLifecycle()
    
    val idsSeleccionados by viewModel.idsSeleccionados.collectAsStateWithLifecycle()
    val estaMultiseleccion by BeCerebroViewModel.coordinador.estaMultiseleccionActiva.collectAsStateWithLifecycle()

    val filterItems by viewModel.filterDropdownItems.collectAsStateWithLifecycle()
    val sortItems by viewModel.sortDropdownItems.collectAsStateWithLifecycle()
    val categoryItems by viewModel.categoryDropdownItems.collectAsStateWithLifecycle()

    var mostrarMenuPerfil by remember { mutableStateOf(false) }
    var mostrarMenuCalendario by remember { mutableStateOf(false) }
    var menuFiltrosAbierto by remember { mutableStateOf<String?>(null) }
    
    var estaRefrescando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var mostrarHistorial by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val beConfig = remember {
        ContextoHUD.CALENDARIO.crearConfiguracionBase(
            edicion = listOf("select_all", "delete_multi"),
            mensajes = emptyList(),
            pistaBusqueda = "¿BUSCÁS UN COMPROMISO? 🗓️✅"
        )
    }

    DisposableEffect(Unit) {
        BeCerebroViewModel.navCoordinador.reiniciarContextoHUD(ContextoHUD.CALENDARIO)
        BeCerebroViewModel.navCoordinador.registrarPantalla(beConfig)

        onDispose {
            BeCerebroViewModel.navCoordinador.removerPantalla(beConfig.id)
            viewModel.deseleccionarTodo() // 🔥 [SANEAMIENTO]
        }
    }

    // 🔥 [ELITE SOBERANÍA]: Sincronización del contrato Be basado en Multiselección
    LaunchedEffect(estaMultiseleccion, idsSeleccionados) {
        val config = ContextoHUD.CALENDARIO.crearConfiguracionBase(
            edicion = if (estaMultiseleccion) listOf("delete_multi", "select_all", "cancel") else listOf("select_all", "delete_multi"),
            mensajes = emptyList(),
            pistaBusqueda = "¿BUSCÁS UN COMPROMISO? 🗓️✅"
        ).copy(
            ocultarOjos = estaMultiseleccion
        )
        BeCerebroViewModel.navCoordinador.actualizarContratoActual(config)
    }

    LaunchedEffect(Unit) {
        BeCerebroViewModel.actionEvent.collect { idAccion ->
            // 🔥 [LEY #12]: Soberanía por Contrato.
            if (BeCerebroViewModel.navCoordinador.contratoActivo.value.id != "root_calendario") return@collect

            when (idAccion) {
                "goto_history" -> mostrarHistorial = true
                "select_all" -> {
                    val actual = idsSeleccionados.size
                    val total = groupedEvents.values.flatten().size
                    if (actual >= total && total > 0) {
                        viewModel.deseleccionarTodo()
                    } else {
                        viewModel.seleccionarTodo(groupedEvents.values.flatten().map { it.id })
                    }
                }
                "delete_multi" -> if (estaMultiseleccion && idsSeleccionados.isNotEmpty()) showDeleteConfirmDialog = true
            }
        }
    }


    CalendarScreenContent(
        groupedEvents = groupedEvents,
        perfiles = perfiles,
        selectedPerfilId = selectedPerfilId ?: "personal",
        onPerfilSelected = { userViewModel.seleccionarPerfil(if (it.id == "personal") null else it.id) },
        filtrosActivos = filtrosActivos,
        filterItems = filterItems,
        sortItems = sortItems,
        categoryItems = categoryItems,
        onBack = onBack,
        onChatClick = onChatClick,
        mostrarMenuPerfil = mostrarMenuPerfil,
        onToggleMenuPerfil = { mostrarMenuPerfil = it },
        mostrarMenuCalendario = mostrarMenuCalendario,
        onToggleMenuCalendario = { mostrarMenuCalendario = it },
        menuFiltrosAbierto = menuFiltrosAbierto,
        onToggleMenuFiltros = { menuFiltrosAbierto = it },
        selectedDate = selectedDate,
        allEventDates = allEventDates,
        onDateSelected = { viewModel.seleccionarFecha(it) },
        onAlternarFiltro = { viewModel.alternarFiltro(it) },
        idsSeleccionados = idsSeleccionados,
        estaMultiseleccion = estaMultiseleccion,
        alAlternarSeleccion = { viewModel.alternarSeleccionItem(it) },
        BeCerebroViewModel = BeCerebroViewModel,
        estaRefrescando = estaRefrescando,
        alRefrescar = {
            estaRefrescando = true
            scope.launch {
                kotlinx.coroutines.delay(1500)
                estaRefrescando = false
            }
        }
    )

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar compromisos") },
            text = { Text("¿Estás seguro de que deseas eliminar los ${idsSeleccionados.size} compromisos seleccionados? Esta acción es permanente.") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.eliminarSeleccionados()
                    showDeleteConfirmDialog = false 
                }) { 
                    Text("Eliminar", color = Color.Red) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { 
                    Text("Cancelar") 
                }
            }
        )
    }

    if (mostrarHistorial) {
        HistoryBottomSheet(
            events = pastEvents,
            onDismiss = { mostrarHistorial = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreenContent(
    groupedEvents: Map<String, List<com.example.myapplication.core.dominio.modelos.EventoDominio>>,
    perfiles: List<PerfilIdentidadV3> = emptyList(),
    selectedPerfilId: String = "personal",
    onPerfilSelected: (PerfilIdentidadV3) -> Unit = {},
    filtrosActivos: Set<String> = emptySet(),
    filterItems: List<DropdownItemData> = emptyList(),
    sortItems: List<DropdownItemData> = emptyList(),
    categoryItems: List<DropdownItemData> = emptyList(),
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    mostrarMenuPerfil: Boolean = false,
    onToggleMenuPerfil: (Boolean) -> Unit = {},
    mostrarMenuCalendario: Boolean = false,
    onToggleMenuCalendario: (Boolean) -> Unit = {},
    menuFiltrosAbierto: String? = null,
    onToggleMenuFiltros: (String?) -> Unit = {},
    selectedDate: Long? = null,
    allEventDates: Set<Long> = emptySet(),
    onDateSelected: (Long) -> Unit = {},
    onAlternarFiltro: (String) -> Unit = {},
    idsSeleccionados: Set<String> = emptySet(),
    estaMultiseleccion: Boolean = false,
    alAlternarSeleccion: (String) -> Unit = {},
    BeCerebroViewModel: BeCerebroViewModel? = null,
    estaRefrescando: Boolean = false,
    alRefrescar: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val collapsedStates = remember { mutableStateMapOf<String, Boolean>() }

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

    val collapseFraction = remember { derivedStateOf { ((scrollAccumulator - 180f) / 250f).coerceIn(0f, 1f) } }
    val barraFiltrosHideFraction = remember { derivedStateOf { (scrollAccumulator / 80f).coerceIn(0f, 1f) } }

    LaunchedEffect(selectedDate) {
        if (selectedDate != null) {
            val targetDateText = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault())
                .format(Date(selectedDate)).replaceFirstChar { it.uppercase() }
            
            val sortedKeys = groupedEvents.keys.sorted()
            val index = sortedKeys.indexOf(targetDateText)
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg).nestedScroll(nestedScrollConnection)) {
        val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()
        
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = estaRefrescando,
            onRefresh = alRefrescar,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    MoldeCabeceraSuperiorPantallas(
                        fraccionColapso = collapseFraction.value,
                        slotIzquierdo = { BotonBackCabeceraV3(onClick = onBack) },
                        slotCentral = {
                            ColumnaTituloSeccionV3(
                                titulo = "Mi Agenda",
                                subtitulo = "Próximos Compromisos",
                                fraccionColapso = collapseFraction.value
                            )
                        },
                        slotDerecho = {
                            EmojiImpactoV3(
                                emoji = "📅",
                                fraccionColapso = collapseFraction.value
                            )
                        }
                    )
                }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
                    
                    Box(modifier = Modifier.fillMaxWidth().animateContentSize().graphicsLayer {
                        alpha = 1f - barraFiltrosHideFraction.value
                        translationY = -10.dp.toPx() * barraFiltrosHideFraction.value
                    }.layout { measurable, constraints -> 
                        val placeable = measurable.measure(constraints)
                        val h = (placeable.height * (1f - barraFiltrosHideFraction.value)).toInt()
                        layout(placeable.width, h) { placeable.placeRelative(0, 0) }
                    }) {
                        BarraFiltrosV3(
                            filtrosActivos = filtrosActivos.mapNotNull { id ->
                                val filter = BeDictionary.Filters[id] ?: BeDictionary.Sorts[id]
                                if (filter != null) ModeloBurbujaFiltro(id, filter.label, filter.emoji ?: "🔹")
                                else if (id.startsWith("cat_")) ModeloBurbujaFiltro(id, id.removePrefix("cat_"), "📋")
                                else null
                            },
                            alHacerClickMenu = { type -> onToggleMenuFiltros(if (menuFiltrosAbierto == type) null else type) },
                            alEliminarFiltro = { onAlternarFiltro(it) },
                            alLimpiarTodo = { onAlternarFiltro("CLEAR_ALL") },
                            mostrarMenuFiltros = menuFiltrosAbierto == "filtros",
                            mostrarMenuOrdenar = menuFiltrosAbierto == "ordenar",
                            mostrarMenuCategorias = menuFiltrosAbierto == "categorias",
                            mostrarCategorias = true,
                            idsFiltrosSeleccionados = filtrosActivos,
                            alAlternarFiltro = { id -> onAlternarFiltro(id) },
                            alCerrarMenu = { onToggleMenuFiltros(null) },
                            itemsFiltro = filterItems,
                            itemsOrden = sortItems,
                            itemsCategoria = categoryItems,
                            estaCentrado = true,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // 🔥 [ELITE v2026]: Implementación del Nuevo ArmadorListaPantallaCompleta
                    ArmadorListaPantallaCompleta(
                        modifier = Modifier.fillMaxSize(),
                        titulo = "MI AGENDA",
                        subtitulo = "Próximos Compromisos",
                        cantidadItems = groupedEvents.values.sumOf { it.size },
                        perfiles = perfiles,
                        idPerfilInicial = selectedPerfilId,
                        alSeleccionarPerfil = { p: PerfilIdentidadV3 -> onPerfilSelected(p) },
                        colorAcento = SharedPalette.ElectricCyan,
                        slotCentral = {
                            Row(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .clickable { onToggleMenuCalendario(true) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val dateLabel = remember(selectedDate) {
                                    if (selectedDate == null) "HOY"
                                    else {
                                        val cal1 = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                        val cal2 = Calendar.getInstance() 
                                        val isToday = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && 
                                                      cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

                                        if (isToday) "HOY"
                                        else SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(selectedDate)).uppercase()
                                    }
                                }

                                TextCompactoAutoFit(
                                    text = dateLabel,
                                    color = Color.White,
                                    maxFontSize = 15.sp,
                                    minFontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    style = AppTypography.HeaderTitle.copy(
                                        letterSpacing = 1.sp
                                    )
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SharedPalette.ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )

                                MoldeMenuArmadorV3(
                                    expanded = mostrarMenuCalendario,
                                    onDismissRequest = { onToggleMenuCalendario(false) }
                                ) {
                                    MenuSectionHeaderV3("SELECCIONAR FECHA")
                                    MiniCalendarioElite(
                                        eventDates = allEventDates,
                                        selectedDate = selectedDate ?: System.currentTimeMillis(),
                                        onDateSelected = { 
                                            onDateSelected(it)
                                            onToggleMenuCalendario(false)
                                        }
                                    )
                                }
                            }
                        },
                        menuPerfil = { iden ->
                            MenuPerfilContenido(
                                identidades = perfiles,
                                idPerfilActivo = iden,
                                alSeleccionar = { p -> 
                                    onPerfilSelected(p)
                                    onToggleMenuPerfil(false)
                                }
                            )
                        }
                    ) { _ ->
                        val sortedDates = groupedEvents.keys.sorted()
                        
                        if (groupedEvents.isEmpty()) {
                            if (estaRefrescando) {
                                items(5) { EsqueletoConcurso() }
                            } else {
                                item { EmptyStateCalendar() }
                            }
                        }

                        sortedDates.forEach { date ->
                            val events = groupedEvents[date] ?: emptyList()
                            val isCollapsed = collapsedStates[date] ?: false

                            stickyHeader {
                                SeparadorFechaPremium(
                                    fecha = date,
                                    isExpanded = !isCollapsed,
                                    onToggle = { collapsedStates[date] = !isCollapsed }
                                )
                            }

                            if (!isCollapsed) {
                                items(events, key = { it.id }) { event ->
                                    val now = System.currentTimeMillis()
                                    ModernEventCard(
                                        event = event,
                                        isPast = event.marcaTiempoUtc < now,
                                        estaSeleccionado = idsSeleccionados.contains(event.id),
                                        modoMultiseleccionActivo = estaMultiseleccion,
                                        onClick = { 
                                            if (estaMultiseleccion) alAlternarSeleccion(event.id)
                                        },
                                        onLongClick = { alAlternarSeleccion(event.id) },
                                        onMessageClick = { onChatClick(event.idParticipante) }
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

@Composable
fun EmptyStateCalendar() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
            // --- 1. TÍTULO EXPLICATIVO ---
            TextCompacto(
                text = "ORGANIZA TU SEMANA",
                color = SharedPalette.ElectricCyan.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // --- 2. TARJETA DE EJEMPLO (Visual Mock) ---
            Box(modifier = Modifier.graphicsLayer { alpha = 0.35f }) {
                ModernEventCard(
                    event = com.example.myapplication.core.dominio.modelos.EventoDominio(
                        id = "mock_event",
                        titulo = "Ejemplo: Visita Técnica de Plomería",
                        descripcion = "Revisión de filtración en cocina",
                        fechaTexto = "Lunes, 10 de Agosto",
                        horaTexto = "14:30 hs",
                        marcaTiempoUtc = System.currentTimeMillis() + 86400000,
                        idParticipante = "p1",
                        nombreParticipante = "Juan Plomero",
                        estado = EstadoEvento.CONFIRMADO,
                        tipo = TipoEvento.VISITA_TECNICA,
                        direccion = "Tu dirección aquí",
                        colorAcentoHex = 0xFF22D3EE,
                        emojiTipo = "🛠️"
                    ),
                    onClick = { },
                    onLongClick = { },
                    onMessageClick = { }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- 3. MENSAJE DE ACCIÓN ---
            TextCompacto(
                text = "AGENDA SIN COMPROMISOS",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "Aquí verás tus próximas citas, visitas técnicas y entregas programadas. Mantén el control de tus proyectos y nunca pierdas un compromiso importante.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. GUÍA DEL ASISTENTE ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.Message, null, tint = SharedPalette.AcidGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                TextCompacto(
                    text = "Coordina una cita con un profesional desde el chat",
                    color = SharedPalette.AcidGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun PreviewCalendarVacioElite() {
    ClienteTheme(darkTheme = true) {
        CalendarScreenContent(
            groupedEvents = emptyMap(),
            onBack = {},
            onChatClick = {}
        )
    }
}

// ==================================================================================
// --- 🎨 PREVIEWS ELITE ---
// ==================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun PreviewCalendarElite() {
    val mockEvents = mapOf(
        "Lunes, 10 de Agosto" to listOf(
            com.example.myapplication.core.dominio.modelos.EventoDominio(
                id = "1", titulo = "Reparación Aire Acondicionado", 
                descripcion = "Carga de gas y limpieza de filtros",
                fechaTexto = "Lunes, 10 de Agosto",
                horaTexto = "14:30 hs",
                marcaTiempoUtc = System.currentTimeMillis() + 86400000,
                idParticipante = "p1", nombreParticipante = "Juan Técnico",
                estado = EstadoEvento.CONFIRMADO,
                tipo = TipoEvento.VISITA_TECNICA,
                direccion = "Av. Siempre Viva 742",
                colorAcentoHex = 0xFF22D3EE,
                emojiTipo = "🛠️"
            )
        )
    )

    ClienteTheme(darkTheme = true) {
        CalendarScreenContent(
            groupedEvents = mockEvents,
            onBack = {},
            onChatClick = {}
        )
    }
}

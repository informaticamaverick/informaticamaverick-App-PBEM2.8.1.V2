package com.example.myapplication.prestador.ui.pantallas.empresa.turnos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes.*
import com.example.myapplication.prestador.viewmodel.empresa.turnos.GestionTurnosUiState
import com.example.myapplication.prestador.viewmodel.empresa.turnos.GestionTurnosViewModel
import com.example.myapplication.prestador.viewmodel.empresa.turnos.TipoFiltroResumen

// --- PALETA OSCURA (mismo criterio que Presupuesto/Catálogo/Concursos) ---
internal object GestionTurnosTheme {
    val DarkBg = Color(0xFF030712)
    val CardBg = Color(0xFF0F172A)
    val SurfaceInput = Color(0xFF020617)
    val BorderGlass = Color(0x1AFFFFFF)
    val BrandOrange = Color(0xFFF97316)
    val AccentCyan = Color(0xFF06B6D4)
    val AccentEmerald = Color(0xFF10B981)
    val AccentAmber = Color(0xFFF59E0B)
    val AccentViolet = Color(0xFF8B5CF6)
    val AccentRose = Color(0xFFF43F5E)
    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

/**
 * --- PANTALLA: GESTIÓN DE TURNOS (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionTurnosScreen(
    onBack: () -> Unit,
    onNavigateToHorarios: (String, String) -> Unit,
    viewModel: GestionTurnosViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = GestionTurnosTheme
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMensaje) {
        state.snackbarMensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarSnackbar()
        }
    }

    if (state.mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.fechaSeleccionada.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.toggleDatePicker(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val fecha = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.cambiarFecha(fecha)
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleDatePicker(false) }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GESTIÓN DE TURNOS", fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 0.4.sp, color = colors.TextPrimary)
                        Text(state.sucursalSeleccionada?.titulo ?: "MAVERICK BUSINESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.BrandOrange)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.TextPrimary)
                    }
                },
                actions = {
                    if (!state.estaCargando) {
                        Surface(
                            onClick = { viewModel.confirmarCambiosGlobales() },
                            color = colors.BrandOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colors.BrandOrange.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = colors.BrandOrange)
                                Spacer(Modifier.width(6.dp))
                                Text("Guardar", fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.BrandOrange)
                            }
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 12.dp), color = colors.BrandOrange, strokeWidth = 2.dp)
                    }

                    if (state.sucursales.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Business, null, tint = colors.BrandOrange)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            state.sucursales.forEach { suc ->
                                DropdownMenuItem(
                                    text = { Text(suc.titulo) },
                                    onClick = { viewModel.seleccionarSucursal(suc); expanded = false }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.CardBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.DarkBg
    ) { padding ->
        GestionTurnosLienzo(
            state = state,
            onTabSelect = { viewModel.seleccionarTab(it) },
            onBusquedaChange = { viewModel.actualizarBusqueda(it) },
            onToggleActivo = { viewModel.alternarActivo(it) },
            onEliminar = { viewModel.eliminarActivo(it) },
            onNuevoRecurso = { viewModel.abrirEditorRecurso(null) },
            onEditarRecurso = { viewModel.abrirEditorRecurso(it) },
            onAsignarEquipo = { viewModel.abrirEditorEquipo(null) },
            onEditarEquipo = { viewModel.abrirEditorEquipo(it) },
            onConfigurarHorario = { id, name -> onNavigateToHorarios(id, name) },
            onFechaChange = { viewModel.cambiarFecha(it) },
            onAbrirCalendario = { viewModel.toggleDatePicker(true) },
            onFiltroResumenChange = { viewModel.seleccionarFiltroResumen(it) },
            onStaffClick = { viewModel.toggleExpansionStaff(it) },
            obtenerSlots = { viewModel.obtenerSlotsTurno(it) },
            modifier = Modifier.padding(padding)
        )
    }

    if (state.mostrarEditorRecurso) {
        EditorRecursoSheet(
            entidad = state.recursoEnEdicion,
            onDismiss = { viewModel.cerrarEditores() },
            onConfirm = { viewModel.guardarRecurso(it) }
        )
    }

    if (state.mostrarEditorEquipo) {
        EditorEquipoSheet(
            entidad = state.equipoEnEdicion,
            recursosDisponibles = state.inventario,
            onDismiss = { viewModel.cerrarEditores() },
            onConfirm = { viewModel.guardarEquipo(it) }
        )
    }
}

@Composable
private fun EliteTabItem(
    label: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = seleccionado,
        onClick = onClick,
        text = { Text(label, fontSize = 9.sp, fontWeight = if (seleccionado) FontWeight.Black else FontWeight.Bold) },
        icon = { Icon(icono, null, modifier = Modifier.size(18.dp)) },
        selectedContentColor = GestionTurnosTheme.BrandOrange,
        unselectedContentColor = GestionTurnosTheme.TextMuted
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GestionTurnosLienzo(
    state: GestionTurnosUiState,
    onTabSelect: (Int) -> Unit,
    onBusquedaChange: (String) -> Unit,
    onToggleActivo: (InventarioActivoDominio) -> Unit,
    onEliminar: (InventarioActivoDominio) -> Unit,
    onNuevoRecurso: () -> Unit,
    onEditarRecurso: (String) -> Unit,
    onAsignarEquipo: () -> Unit,
    onEditarEquipo: (String) -> Unit,
    onConfigurarHorario: (String, String) -> Unit,
    onFechaChange: (java.time.LocalDate) -> Unit,
    onAbrirCalendario: () -> Unit,
    onFiltroResumenChange: (TipoFiltroResumen) -> Unit,
    onStaffClick: (String) -> Unit,
    obtenerSlots: (InventarioActivoDominio) -> List<SlotTurnoSimulado>,
    modifier: Modifier = Modifier
) {
    val colors = GestionTurnosTheme

    Column(modifier = modifier.fillMaxSize().background(colors.DarkBg)) {
        TabRow(
            selectedTabIndex = state.tabSeleccionada,
            containerColor = colors.CardBg,
            contentColor = colors.BrandOrange,
            divider = { HorizontalDivider(color = colors.BorderGlass) }
        ) {
            EliteTabItem("RESUMEN", Icons.Outlined.Dashboard, state.tabSeleccionada == 0) { onTabSelect(0) }
            EliteTabItem("ESPACIOS", Icons.Outlined.MeetingRoom, state.tabSeleccionada == 1) { onTabSelect(1) }
            EliteTabItem("EQUIPO", Icons.Outlined.Badge, state.tabSeleccionada == 2) { onTabSelect(2) }
            EliteTabItem("HORARIOS", Icons.Outlined.Schedule, state.tabSeleccionada == 3) { onTabSelect(3) }
        }

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = state.tabSeleccionada,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f) togetherWith
                    fadeOut(animationSpec = tween(180))
                },
                label = "tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> TabResumen(
                        ocupacion = state.ocupacionHoy,
                        staffActivo = state.staffActivo,
                        recursosListos = state.recursosListos,
                        filtroActual = state.filtroResumen,
                        idStaffExpandido = state.idStaffExpandido,
                        fechaSeleccionada = state.fechaSeleccionada,
                        inventario = state.inventario,
                        onFiltroChange = onFiltroResumenChange,
                        onStaffClick = onStaffClick,
                        onFechaChange = onFechaChange,
                        onAbrirCalendario = onAbrirCalendario,
                        obtenerSlots = obtenerSlots
                    )
                    1 -> TabRecursos(
                        recursos = state.inventario,
                        onNuevoRecurso = onNuevoRecurso,
                        onEditarRecurso = onEditarRecurso,
                        onToggleActivo = onToggleActivo,
                        onEliminar = onEliminar
                    )
                    2 -> TabEquipo(
                        equipo = state.inventario,
                        onAsignarPersonal = onAsignarEquipo,
                        onEditarPersonal = onEditarEquipo,
                        onToggleActivo = onToggleActivo,
                        onEliminar = onEliminar
                    )
                    3 -> TabHorarios(
                        inventario = state.inventario,
                        onConfigurarHorario = onConfigurarHorario
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF030712)
@Composable
fun PreviewGestionTurnos() {
    val mockState = GestionTurnosUiState(
        tabSeleccionada = 0,
        inventario = listOf(
            InventarioActivoDominio("1", "Recurso Alpha", com.example.myapplication.core.dominio.modelos.TipoActivo.RECURSO, true, "SALA", "Sector A"),
            InventarioActivoDominio("2", "Colaborador Beta", com.example.myapplication.core.dominio.modelos.TipoActivo.EQUIPO, true, "Técnico", "Staff")
        )
    )
    GestionTurnosLienzo(
        state = mockState,
        onTabSelect = {},
        onBusquedaChange = {},
        onToggleActivo = {},
        onEliminar = {},
        onNuevoRecurso = {},
        onEditarRecurso = {},
        onAsignarEquipo = {},
        onEditarEquipo = {},
        onConfigurarHorario = { _, _ -> },
        onFechaChange = {},
        onAbrirCalendario = {},
        onFiltroResumenChange = {},
        onStaffClick = {},
        obtenerSlots = { emptyList() }
    )
}

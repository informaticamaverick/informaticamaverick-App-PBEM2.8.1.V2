package com.example.myapplication.prestador.ui.pantallas.empresa.visitas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.pantallas.empresa.componentes.MetricCard
import com.example.myapplication.prestador.ui.pantallas.empresa.componentes.SelectorFecha
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes.EditorEquipoSheet
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes.SlotTurnoSimulado
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.empresa.visitas.GestionVisitasUiState
import com.example.myapplication.prestador.viewmodel.empresa.visitas.GestionVisitasViewModel

/**
 * --- PANTALLA: GESTIÓN DE VISITAS TÉCNICAS (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionVisitasScreen(
    onBack: () -> Unit,
    onNavigateToHorarios: (String, String) -> Unit,
    viewModel: GestionVisitasViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = getPrestadorColors()
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
                        Text("VISITAS TÉCNICAS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(state.sucursalSeleccionada?.titulo ?: "AGENDA DE CAMPO", fontSize = 11.sp, color = colors.primaryOrange)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.textPrimary)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { viewModel.confirmarCambiosGlobales() },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = colors.primaryOrange.copy(alpha = 0.1f), contentColor = colors.primaryOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Confirmar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surfaceColor)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.backgroundColor
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = state.tabSeleccionada,
                containerColor = colors.surfaceColor,
                contentColor = colors.primaryOrange
            ) {
                Tab(
                    selected = state.tabSeleccionada == 0,
                    onClick = { viewModel.seleccionarTab(0) },
                    text = { Text("AGENDA", fontSize = 10.sp, fontWeight = FontWeight.Black) },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, null, modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = state.tabSeleccionada == 1,
                    onClick = { viewModel.seleccionarTab(1) },
                    text = { Text("TÉCNICOS", fontSize = 10.sp, fontWeight = FontWeight.Black) },
                    icon = { Icon(Icons.Outlined.Badge, null, modifier = Modifier.size(20.dp)) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.tabSeleccionada,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f) togetherWith
                        fadeOut(animationSpec = tween(180))
                    },
                    label = "tab_visitas"
                ) { tab ->
                    when (tab) {
                        0 -> AgendaVisitasTab(state, viewModel)
                        1 -> TecnicosVisitasTab(state, viewModel)
                    }
                }
            }
        }
    }

    if (state.mostrarEditorTecnico) {
        EditorEquipoSheet(
            entidad = state.tecnicoEnEdicion,
            onDismiss = { viewModel.cerrarEditor() },
            onConfirm = { viewModel.guardarTecnico(it) }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AgendaVisitasTab(state: GestionVisitasUiState, viewModel: GestionVisitasViewModel) {
    val colors = getPrestadorColors()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            SelectorFecha(
                seleccionada = state.fechaSeleccionada,
                onFechaSelect = { viewModel.cambiarFecha(it) },
                onCalendarioClick = { viewModel.toggleDatePicker(true) }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = colors.primaryOrange)
                    Spacer(Modifier.width(12.dp))
                    Text("Aquí puedes visualizar la carga de trabajo del personal de campo para la fecha seleccionada.", fontSize = 12.sp, color = colors.textSecondary)
                }
            }
        }

        items(state.tecnicos) { tecnico ->
            val slots = viewModel.obtenerSlotsTurno(tecnico)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(tecnico.nombre, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(tecnico.subTitulo, fontSize = 11.sp, color = colors.textSecondary)
                    
                    Spacer(Modifier.height(12.dp))
                    
                    val ocupados = slots.filter { it.ocupado }
                    if (ocupados.isEmpty()) {
                        Text("Sin visitas programadas", fontSize = 11.sp, color = Color(0xFF10B981))
                    } else {
                        ocupados.forEach { slot ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color.Red, CircleShape))
                                Spacer(Modifier.width(8.dp))
                                Text("${slot.hora} hs — Visita Técnica Confirmada", fontSize = 12.sp, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TecnicosVisitasTab(state: GestionVisitasUiState, viewModel: GestionVisitasViewModel) {
    val colors = getPrestadorColors()

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("PERSONAL DE CAMPO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                Button(onClick = { viewModel.abrirEditor(null) }) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Nuevo Técnico")
                }
            }
        }

        items(state.tecnicos) { tecnico ->
            Card(
                onClick = { viewModel.abrirEditor(tecnico.id) },
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = colors.primaryOrange)
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tecnico.nombre, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(tecnico.subTitulo, fontSize = 11.sp, color = colors.textSecondary)
                    }
                    Switch(checked = tecnico.habilitado, onCheckedChange = { viewModel.alternarHabilitacion(tecnico) })
                }
            }
        }
    }
}

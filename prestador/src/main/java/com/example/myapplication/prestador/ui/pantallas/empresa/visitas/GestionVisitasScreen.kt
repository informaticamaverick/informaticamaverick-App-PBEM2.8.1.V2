package com.example.myapplication.prestador.ui.pantallas.empresa.visitas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.pantallas.empresa.componentes.SelectorFecha
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes.EditorEquipoSheet
import com.example.myapplication.prestador.viewmodel.empresa.visitas.GestionVisitasUiState
import com.example.myapplication.prestador.viewmodel.empresa.visitas.GestionVisitasViewModel

/**
 * --- PANTALLA: GESTIÓN DE VISITAS TÉCNICAS (v2026.SUPREME) ---
 * Migrada al mismo tema oscuro que Gestión de Turnos (mockup aprobado 27/08).
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
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("VISITAS TÉCNICAS", fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 0.4.sp, color = colors.TextPrimary)
                            Text(state.sucursalSeleccionada?.titulo ?: "AGENDA DE CAMPO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.BrandOrange)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.TextPrimary)
                        }
                    },
                    actions = {
                        Surface(
                            onClick = { viewModel.confirmarCambiosGlobales() },
                            color = colors.BrandOrange.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, null, tint = colors.BrandOrange, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Confirmar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.BrandOrange)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.CardBg)
                )
                TabRow(
                    selectedTabIndex = state.tabSeleccionada,
                    containerColor = colors.CardBg,
                    contentColor = colors.BrandOrange,
                    divider = { HorizontalDivider(color = colors.BorderGlass) }
                ) {
                    Tab(
                        selected = state.tabSeleccionada == 0,
                        onClick = { viewModel.seleccionarTab(0) },
                        selectedContentColor = colors.BrandOrange,
                        unselectedContentColor = colors.TextMuted,
                        text = { Text("AGENDA", fontSize = 10.sp, fontWeight = FontWeight.Black) },
                        icon = { Icon(Icons.AutoMirrored.Outlined.Assignment, null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = state.tabSeleccionada == 1,
                        onClick = { viewModel.seleccionarTab(1) },
                        selectedContentColor = colors.BrandOrange,
                        unselectedContentColor = colors.TextMuted,
                        text = { Text("TÉCNICOS", fontSize = 10.sp, fontWeight = FontWeight.Black) },
                        icon = { Icon(Icons.Outlined.Badge, null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.DarkBg
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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

    if (state.mostrarEditorTecnico) {
        EditorEquipoSheet(
            entidad = state.tecnicoEnEdicion,
            onDismiss = { viewModel.cerrarEditor() },
            onConfirm = { viewModel.guardarTecnico(it) }
        )
    }
}

/** Alterna acento entre naranja/cian/violeta según el id, mismo criterio que TabEquipo. */
private fun colorTecnico(id: String, colors: GestionTurnosTheme): Color = when (Math.floorMod(id.hashCode(), 3)) {
    0 -> colors.BrandOrange
    1 -> colors.AccentCyan
    else -> colors.AccentViolet
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AgendaVisitasTab(state: GestionVisitasUiState, viewModel: GestionVisitasViewModel) {
    val colors = GestionTurnosTheme

    Box(modifier = Modifier.fillMaxSize().background(colors.DarkBg)) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SelectorFecha(
                    seleccionada = state.fechaSeleccionada,
                    onFechaSelect = { viewModel.cambiarFecha(it) },
                    onCalendarioClick = { viewModel.toggleDatePicker(true) }
                )
            }

            item {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.BrandOrange.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, colors.BrandOrange.copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = colors.BrandOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Agenda de Campo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.TextPrimary)
                            Text("Visualizá la carga de trabajo del personal técnico para la fecha seleccionada.", fontSize = 10.sp, color = colors.TextSecondary)
                        }
                    }

                    Text(
                        "TÉCNICOS EN CAMPO (${state.tecnicos.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        color = colors.TextMuted
                    )
                }
            }

            items(state.tecnicos, key = { it.id }) { tecnico ->
                val slots = viewModel.obtenerSlotsTurno(tecnico)
                val acento = colorTecnico(tecnico.id, colors)

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    color = colors.CardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.BorderGlass)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(acento.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                Text(tecnico.nombre.take(1).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = acento)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(tecnico.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.TextPrimary)
                                Text(tecnico.subTitulo, fontSize = 11.sp, color = colors.TextSecondary)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = colors.BorderGlass)
                        Spacer(Modifier.height(12.dp))

                        val ocupados = slots.filter { it.ocupado }
                        if (ocupados.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = colors.AccentEmerald, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sin visitas programadas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.AccentEmerald)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ocupados.forEach { slot ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(colors.AccentRose, CircleShape))
                                        Spacer(Modifier.width(8.dp))
                                        Text("${slot.hora} hs — Visita Técnica Confirmada", fontSize = 12.sp, color = colors.TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TecnicosVisitasTab(state: GestionVisitasUiState, viewModel: GestionVisitasViewModel) {
    val colors = GestionTurnosTheme
    val activos = state.tecnicos.count { it.habilitado }

    Box(modifier = Modifier.fillMaxSize().background(colors.DarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("PERSONAL DE CAMPO", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp, color = colors.TextSecondary)
                    Row(
                        modifier = Modifier
                            .background(colors.AccentEmerald.copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(colors.AccentEmerald, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("$activos ACTIVOS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = colors.AccentEmerald)
                    }
                }
            }

            items(state.tecnicos, key = { it.id }) { tecnico ->
                val acento = colorTecnico(tecnico.id, colors)

                Surface(
                    onClick = { viewModel.abrirEditor(tecnico.id) },
                    modifier = Modifier.fillMaxWidth().let { if (!tecnico.habilitado) it.alpha(0.6f) else it },
                    color = colors.CardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.BorderGlass)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(acento.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text(tecnico.nombre.take(1).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = acento)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(color = acento.copy(alpha = 0.12f), shape = RoundedCornerShape(5.dp)) {
                                Text(
                                    tecnico.especialidad.ifBlank { "GENERAL" }.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = acento,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                            Text(tecnico.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.TextPrimary)
                            Text(tecnico.subTitulo, fontSize = 11.sp, color = colors.TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (tecnico.habilitado) "ACTIVO" else "INACTIVO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (tecnico.habilitado) colors.AccentEmerald else colors.TextMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Switch(
                                checked = tecnico.habilitado,
                                onCheckedChange = { viewModel.alternarHabilitacion(tecnico) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.AccentEmerald)
                            )
                        }
                    }
                }
            }
        }

        Surface(
            onClick = { viewModel.abrirEditor(null) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            color = colors.AccentEmerald,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("NUEVO TÉCNICO", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

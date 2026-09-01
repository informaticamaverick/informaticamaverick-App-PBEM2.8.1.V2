package com.example.myapplication.prestador.ui.pantallas.chat.wizard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.utilidades.CalendarUtils
import com.example.myapplication.prestador.ui.pantallas.chat.wizard.componentes.*
import com.example.myapplication.prestador.viewmodel.chat.wizard.ModoAgendaTurno
import com.example.myapplication.prestador.viewmodel.chat.wizard.PasoWizard
import com.example.myapplication.prestador.viewmodel.chat.wizard.WizardTurnoViewModel
import com.example.myapplication.uishared.estilos.SharedPalette
import java.util.*

/**
 * --- WIZARD DE PROPUESTA DE TURNO (v2026.SUPREME) ---
 * [PROPÓSITO]: Reemplaza el selector simple por una experiencia guiada por pasos.
 * [LEY #10]: UI Tonta con navegación interna.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardTurnoSheet(
    idSucursal: String?,
    nombreCliente: String,
    urlFotoCliente: String?,
    categoriaActual: String?,
    iconoActual: String?,
    alCerrar: () -> Unit,
    alConfirmarCerrado: (fecha: String, hora: String, direccion: String, idRecurso: String?, nombreRecurso: String?) -> Unit,
    alConfirmarAbierto: (direccion: String, idRecurso: String?, nombreRecurso: String?) -> Unit,
    viewModel: WizardTurnoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.inicializar(idSucursal, nombreCliente, urlFotoCliente, categoriaActual, iconoActual)
    }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = SharedPalette.EliteMainBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.1f)) },
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            AnimatedContent(
                targetState = state.pasoActual,
                transitionSpec = {
                    if (targetState == PasoWizard.CONFIGURACION) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "wizard_steps"
            ) { paso ->
                when (paso) {
                    PasoWizard.IDENTIDAD -> PasoIdentidad(
                        nombrePrestador = state.nombrePrestador,
                        categoria = state.categoriaServicio,
                        iconoCategoria = state.iconoCategoria,
                        direcciones = state.direccionesDisponibles,
                        direccionSeleccionada = state.direccionSeleccionada,
                        onDireccionSelect = { viewModel.seleccionarDireccion(it) },
                        nombreCliente = state.nombreCliente,
                        urlFotoCliente = state.urlFotoCliente,
                        onContinuar = { viewModel.irAPasoConfiguracion() }
                    )
                    PasoWizard.CONFIGURACION -> PasoConfiguracionTurno(
                        modo = state.modoAgenda,
                        recursos = state.recursosDisponibles,
                        recursoSeleccionado = state.recursoSeleccionado,
                        onRecursoSelect = { viewModel.seleccionarRecurso(it, idSucursal ?: "") },
                        equipo = state.equipoDisponible,
                        personalAsignado = state.personalAsignado,
                        onPersonalSelect = { viewModel.seleccionarPersonal(it) },
                        fechaTexto = state.fechaTexto,
                        onAbrirCalendario = { mostrarDatePicker = true },
                        bloques = state.bloquesDisponibles,
                        estaCargando = state.estaCargando,
                        horaSeleccionada = state.horaSeleccionada,
                        onHoraSelect = { viewModel.seleccionarHora(it) },
                        onCambiarModo = { viewModel.cambiarModoAgenda(it) },
                        onVolver = { viewModel.volverAIdentidad() },
                        onConfirmar = {
                            val dir = state.direccionSeleccionada?.let { it.etiqueta.ifBlank { it.aTextoCorto() } } ?: ""
                            if (state.modoAgenda == ModoAgendaTurno.CERRADA) {
                                alConfirmarCerrado(
                                    CalendarUtils.formatIsoDate(state.fechaSeleccionadaMillis),
                                    state.horaSeleccionada,
                                    dir,
                                    state.recursoSeleccionado?.id,
                                    state.recursoSeleccionado?.nombre
                                )
                            } else {
                                alConfirmarAbierto(
                                    dir,
                                    state.recursoSeleccionado?.id,
                                    state.recursoSeleccionado?.nombre
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.fechaSeleccionadaMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= System.currentTimeMillis() - 86400000
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.establecerFecha(it, idSucursal ?: "") }
                    mostrarDatePicker = false
                }) { Text("ACEPTAR", color = Color(0xFFA855F7), fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("CANCELAR", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(containerColor = SharedPalette.EliteSurface)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

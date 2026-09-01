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
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.utilidades.CalendarUtils
import com.example.myapplication.prestador.ui.pantallas.chat.wizard.componentes.*
import com.example.myapplication.prestador.viewmodel.chat.wizard.ModoAgendaTurno
import com.example.myapplication.prestador.viewmodel.chat.wizard.PasoWizard
import com.example.myapplication.prestador.viewmodel.chat.wizard.WizardVisitaViewModel
import com.example.myapplication.uishared.estilos.SharedPalette
import java.util.*

/**
 * --- WIZARD DE PROPUESTA DE VISITA TÉCNICA (v2026.SUPREME) ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardVisitaSheet(
    idSucursal: String?,
    nombreCliente: String,
    urlFotoCliente: String?,
    categoriaActual: String?,
    iconoActual: String?,
    ubicacionesChat: List<MensajeEntity>,
    presupuestosChat: List<PresupuestoResumenDominio>,
    direccionInicial: MensajeEntity? = null,
    alCerrar: () -> Unit,
    alConfirmarCerrado: (fecha: String, hora: String, direccion: String, equipoIds: List<String>, idPresupuesto: String?) -> Unit,
    alConfirmarAbierto: (direccion: String, equipoIds: List<String>, idPresupuesto: String?) -> Unit,
    viewModel: WizardVisitaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.inicializar(idSucursal, nombreCliente, urlFotoCliente, categoriaActual, iconoActual, ubicacionesChat, presupuestosChat, direccionInicial)
    }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = SharedPalette.EliteMainBackground,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.1f)) },
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            AnimatedContent(
                targetState = state.pasoActual,
                transitionSpec = {
                    if (targetState == PasoWizard.CONFIGURACION) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "wizard_visita_steps"
            ) { paso ->
                when (paso) {
                    PasoWizard.IDENTIDAD -> PasoIdentidad(
                        nombrePrestador = state.nombrePrestador,
                        categoria = state.categoriaServicio,
                        iconoCategoria = state.iconoCategoria,
                        direcciones = state.direccionesOrigen,
                        direccionSeleccionada = state.direccionOrigenSeleccionada,
                        onDireccionSelect = { viewModel.seleccionarDireccionOrigen(it) },
                        nombreCliente = state.nombreCliente,
                        urlFotoCliente = state.urlFotoCliente,
                        onContinuar = { viewModel.irAPasoConfiguracion() }
                    )
                    PasoWizard.CONFIGURACION -> PasoConfiguracionVisita(
                        modo = state.modoAgenda,
                        direccionesDestino = state.direccionesDestinoDisponibles,
                        direccionSeleccionada = state.direccionDestinoSeleccionada,
                        onDireccionSelect = { viewModel.seleccionarDireccionDestino(it, idSucursal ?: "") },
                        equipo = state.equipoDisponible,
                        equipoSeleccionadoIds = state.equipoSeleccionadoIds,
                        onToggleTecnico = { viewModel.toggleTecnico(it) },
                        presupuestos = state.presupuestosDisponibles,
                        presupuestoSeleccionado = state.presupuestoSeleccionado,
                        onPresupuestoSelect = { viewModel.seleccionarPresupuesto(it) },
                        fechaTexto = state.fechaTexto,
                        onAbrirCalendario = { mostrarDatePicker = true },
                        bloques = state.bloquesDisponibles,
                        estaCargando = state.estaCargando,
                        horaSeleccionada = state.horaSeleccionada,
                        onHoraSelect = { viewModel.seleccionarHora(it) },
                        costoEstimado = state.costoTrasladoEstimado,
                        onCambiarModo = { viewModel.toggleModo(it) },
                        onVolver = { viewModel.volverAIdentidad() },
                        onConfirmar = {
                            val dest = state.direccionDestinoSeleccionada?.direccionTexto ?: ""
                            if (state.modoAgenda == ModoAgendaTurno.CERRADA) {
                                alConfirmarCerrado(
                                    CalendarUtils.formatIsoDate(state.fechaSeleccionadaMillis),
                                    state.horaSeleccionada,
                                    dest,
                                    state.equipoSeleccionadoIds.toList(),
                                    state.presupuestoSeleccionado?.idPresupuesto
                                )
                            } else {
                                alConfirmarAbierto(
                                    dest,
                                    state.equipoSeleccionadoIds.toList(),
                                    state.presupuestoSeleccionado?.idPresupuesto
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
                }) { Text("ACEPTAR", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black) }
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

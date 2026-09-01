package com.example.myapplication.ui.pantallas.chat.turnos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

/**
 * --- VIEWMODEL: SELECTOR DE TURNO (v2026.SUPREME) ---
 */
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class UsuarioSelectorTurnoViewModel @Inject constructor(
    private val calculadora: CalculadoraDisponibilidad
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectorTurnoUiState())
    val uiState: StateFlow<SelectorTurnoUiState> = _uiState.asStateFlow()

    fun inicializar(mensaje: MensajeEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            
            try {
                val esAbierto = mensaje.subtipoOperativo == "AGENDA_ABIERTA"
                val hoy = LocalDate.now()
                val fechas = (0..6).map { hoy.plusDays(it.toLong()) }

                _uiState.update { state ->
                    state.copy(
                        estaCargando = false,
                        nombrePrestador = mensaje.nombreRecurso ?: "Maverick Prestador",
                        direccionPrestador = mensaje.direccionTexto ?: "Consultar al local",
                        esAgendaAbierta = esAbierto,
                        fechasDisponibles = fechas,
                        fechaSeleccionada = hoy
                    )
                }
                
                if (esAbierto) {
                    procesarAgendaAbierta(mensaje.contenido, hoy)
                } else {
                    procesarAgendaCerrada(mensaje)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(estaCargando = false, error = "Fallo al cargar agenda") }
            }
        }
    }

    private fun procesarAgendaAbierta(jsonStr: String, fecha: LocalDate) {
        try {
            val json = JSONObject(jsonStr)
            val recursosIds = json.optJSONArray("recursos") ?: return
            
            // Simulación de carga de slots basada en reglas
            // En una versión real, esto consultaría a un endpoint de disponibilidad real-time
            val mockRecursos = mutableListOf<RecursoConSlots>()
            for (i in 0 until recursosIds.length()) {
                val id = recursosIds.getString(i)
                val slots = calculadora.generarBloquesParaDia(
                    fechaIso = fecha.toString(),
                    rangos = emptyList(), // Aquí irían los rangos del JSON
                    eventosExistentes = emptyList()
                )
                // Para el MVP, generamos slots simulados
                val slotsSimulados = generarSlotsSimulados(fecha)
                
                mockRecursos.add(RecursoConSlots(
                    id = id,
                    nombre = "Opción ${i + 1}",
                    slots = slotsSimulados
                ))
            }
            _uiState.update { it.copy(recursosDisponibles = mockRecursos) }
        } catch (e: Exception) {}
    }

    private fun procesarAgendaCerrada(mensaje: MensajeEntity) {
        val bloque = BloqueHorario(
            horaTexto = mensaje.horaCita ?: "--:--",
            estaOcupado = false,
            inicioUtc = 0,
            finUtc = 0
        )
        val recurso = RecursoConSlots(
            id = mensaje.idReferencia ?: "fijo",
            nombre = mensaje.nombreRecurso ?: "Turno Propuesto",
            slots = listOf(bloque)
        )
        _uiState.update { it.copy(
            recursosDisponibles = listOf(recurso),
            idRecursoSeleccionado = recurso.id,
            bloqueSeleccionado = bloque,
            puedeConfirmar = true
        ) }
    }

    fun seleccionarFecha(fecha: LocalDate) {
        _uiState.update { it.copy(fechaSeleccionada = fecha, bloqueSeleccionado = null, puedeConfirmar = false) }
        // Si es abierta, recalcular slots para ese día
        if (_uiState.value.esAgendaAbierta) {
            // Recargar slots simulados para la nueva fecha
            val actualizados = _uiState.value.recursosDisponibles.map { 
                it.copy(slots = generarSlotsSimulados(fecha))
            }
            _uiState.update { it.copy(recursosDisponibles = actualizados) }
        }
    }

    fun seleccionarBloque(idRecurso: String, bloque: BloqueHorario) {
        _uiState.update { it.copy(
            idRecursoSeleccionado = idRecurso,
            bloqueSeleccionado = bloque,
            puedeConfirmar = true
        ) }
    }

    private fun generarSlotsSimulados(fecha: LocalDate): List<BloqueHorario> {
        val slots = mutableListOf<BloqueHorario>()
        val base = listOf("08:00", "09:00", "10:00", "11:00", "15:00", "16:00", "17:00")
        base.forEach { h ->
            slots.add(BloqueHorario(h, Random().nextBoolean(), 0, 0))
        }
        return slots.sortedBy { it.horaTexto }
    }
}


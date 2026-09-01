package com.example.myapplication.prestador.viewmodel.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import com.example.myapplication.prestador.datos.repositorios.PrestadorCalendarioRepositorio
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * --- VIEWMODEL DE FECHAS BLOQUEADAS (V2026.12) ---
 * Gestiona días no laborables y feriados usando el motor de Eventos.
 * [LEY #9]: Nomenclatura en Español.
 */
@HiltViewModel
class FechasBloqueadasViewModel @Inject constructor(
    private val calendarioRepository: PrestadorCalendarioRepositorio,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val idPrestador get() = savedStateHandle.get<String>("owner_id") ?: auth.currentUser?.uid ?: ""

    /**
     * Flujo de fechas bloqueadas (Eventos tipo BLOQUEO_ADMIN).
     */
    val fechasBloqueadas: StateFlow<List<EventoEntity>> = calendarioRepository
        .obtenerTodosLosEventos(idPrestador)
        .map { lista -> lista.filter { it.tipo == TipoEvento.BLOQUEO_ADMIN && it.estado != EstadoEvento.CANCELADO } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _estadoUi = MutableStateFlow<EstadoBloqueoUi>(EstadoBloqueoUi.Reposo)
    val estadoUi: StateFlow<EstadoBloqueoUi> = _estadoUi.asStateFlow()

    /**
     * Bloquea un día completo.
     */
    fun bloquearDiaCompleto(fechaIso: String, motivo: String) {
        viewModelScope.launch {
            if (idPrestador.isBlank()) return@launch

            _estadoUi.value = EstadoBloqueoUi.Cargando
            try {
                val partes = fechaIso.split("-")
                val cal = Calendar.getInstance().apply {
                    set(partes[0].toInt(), partes[1].toInt() - 1, partes[2].toInt(), 0, 0, 0)
                }
                val inicio = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                val fin = cal.timeInMillis

                val nuevoBloqueo = EventoEntity(
                    id = UUID.randomUUID().toString(),
                    idPropietarioSucursal = idPrestador,
                    idSucursal = idPrestador, // Asumido
                    idCliente = "SISTEMA",
                    idChat = "BLOQUEO_ADMIN",
                    tipo = TipoEvento.BLOQUEO_ADMIN,
                    estado = EstadoEvento.CONFIRMADO,
                    fechaInicioUtc = inicio,
                    fechaFinUtc = fin,
                    titulo = motivo,
                    descripcion = "Bloqueo manual de agenda"
                )

                calendarioRepository.agendarEventoInterno(nuevoBloqueo)
                _estadoUi.value = EstadoBloqueoUi.Exito("Día bloqueado: $motivo")
            } catch (e: Exception) {
                _estadoUi.value = EstadoBloqueoUi.Error(e.message ?: "Error al bloquear")
            }
        }
    }

    /**
     * Desbloquea un evento de tipo bloqueo.
     */
    fun eliminarBloqueo(idEvento: String) {
        viewModelScope.launch {
            calendarioRepository.actualizarEstado(idEvento, EstadoEvento.CANCELADO)
        }
    }

    sealed interface EstadoBloqueoUi {
        object Reposo : EstadoBloqueoUi
        object Cargando : EstadoBloqueoUi
        data class Exito(val mensaje: String) : EstadoBloqueoUi
        data class Error(val mensaje: String) : EstadoBloqueoUi
    }
}

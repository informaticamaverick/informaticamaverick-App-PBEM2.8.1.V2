package com.example.myapplication.prestador.viewmodel.chat.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.example.myapplication.prestador.datos.repositorios.PrestadorCalendarioRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * --- VIEWMODEL: WIZARD DE PROPUESTA DE TURNO (v2026.SUPREME) ---
 */
@HiltViewModel
class WizardTurnoViewModel @Inject constructor(
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val calendarioRepo: PrestadorCalendarioRepositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(WizardTurnoUiState())
    val uiState: StateFlow<WizardTurnoUiState> = _uiState.asStateFlow()

    private val formatterFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale.getDefault())

    /**
     * Inicializa el Wizard con los datos del contexto del chat.
     */
    fun inicializar(
        idSucursal: String?,
        nombreCliente: String,
        urlFotoCliente: String?,
        categoriaActual: String?,
        iconoActual: String?
    ) {
        viewModelScope.launch {
            val borrador = gestorBorrador.borrador.value ?: return@launch
            
            val sucursales = borrador.empresas.flatMap { it.sucursales }
            val sucEncontrada = if (idSucursal != null) sucursales.find { it.id == idSucursal } else null
            
            val direcciones = if (sucEncontrada != null) {
                listOfNotNull(sucEncontrada.direccion)
            } else {
                borrador.prestador.direcciones
            }

            val recursos = sucEncontrada?.recursos ?: emptyList()
            val equipo = sucEncontrada?.equipoTrabajo ?: emptyList()

            _uiState.update { state ->
                state.copy(
                    nombrePrestador = sucEncontrada?.nombre ?: borrador.prestador.perfil.titulo,
                    categoriaServicio = categoriaActual ?: "Servicio General",
                    iconoCategoria = iconoActual ?: "🗓️",
                    direccionesDisponibles = direcciones,
                    direccionSeleccionada = direcciones.firstOrNull(),
                    nombreCliente = nombreCliente,
                    urlFotoCliente = urlFotoCliente,
                    recursosDisponibles = recursos,
                    equipoDisponible = equipo,
                    recursoSeleccionado = recursos.firstOrNull(),
                    fechaTexto = formatterFecha.format(Date(state.fechaSeleccionadaMillis)).uppercase()
                )
            }
            
            if (recursos.isNotEmpty() && idSucursal != null) {
                recalcularDisponibilidad(idSucursal, recursos.first().id, _uiState.value.fechaSeleccionadaMillis)
            }
        }
    }

    fun irAPasoConfiguracion() {
        _uiState.update { it.copy(pasoActual = PasoWizard.CONFIGURACION) }
    }

    fun volverAIdentidad() {
        _uiState.update { it.copy(pasoActual = PasoWizard.IDENTIDAD) }
    }

    fun seleccionarDireccion(direccion: DireccionDominio) {
        _uiState.update { it.copy(direccionSeleccionada = direccion) }
    }

    fun cambiarModoAgenda(modo: ModoAgendaTurno) {
        _uiState.update { it.copy(modoAgenda = modo) }
    }

    fun establecerFecha(millis: Long, idSucursal: String) {
        _uiState.update { it.copy(
            fechaSeleccionadaMillis = millis,
            fechaTexto = formatterFecha.format(Date(millis)).uppercase(),
            horaSeleccionada = "" 
        ) }
        val idRecurso = _uiState.value.recursoSeleccionado?.id
        recalcularDisponibilidad(idSucursal, idRecurso, millis)
    }

    fun seleccionarRecurso(recurso: RecursoDominio, idSucursal: String) {
        _uiState.update { it.copy(recursoSeleccionado = recurso, horaSeleccionada = "") }
        recalcularDisponibilidad(idSucursal, recurso.id, _uiState.value.fechaSeleccionadaMillis)
    }

    fun seleccionarPersonal(personal: EquipoTrabajoDominio?) {
        _uiState.update { it.copy(personalAsignado = personal) }
    }

    fun seleccionarHora(hora: String) {
        _uiState.update { it.copy(horaSeleccionada = hora) }
    }

    private fun recalcularDisponibilidad(idSucursal: String, idRecurso: String?, fecha: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true) }
            try {
                val slots = calendarioRepo.calcularDisponibilidadReal(idSucursal, idRecurso, fecha)
                _uiState.update { it.copy(bloquesDisponibles = slots, estaCargando = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(bloquesDisponibles = emptyList(), estaCargando = false) }
            }
        }
    }

    fun toggleRecursoPermitido(id: String) {
        _uiState.update { state ->
            val nuevos = if (state.recursosPermitidosIds.contains(id)) state.recursosPermitidosIds - id
                         else state.recursosPermitidosIds + id
            state.copy(recursosPermitidosIds = nuevos)
        }
    }

    fun togglePersonalPermitido(id: String) {
        _uiState.update { state ->
            val nuevos = if (state.personalPermitidoIds.contains(id)) state.personalPermitidoIds - id
                         else state.personalPermitidoIds + id
            state.copy(personalPermitidoIds = nuevos)
        }
    }
}


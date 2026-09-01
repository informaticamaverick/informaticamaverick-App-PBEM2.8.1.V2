package com.example.myapplication.prestador.viewmodel.chat.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.core.utilidades.GeoUtils
import com.example.myapplication.prestador.datos.gestores.BorradorPerfilPrestadorGestor
import com.example.myapplication.prestador.datos.repositorios.PrestadorCalendarioRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * --- VIEWMODEL: WIZARD DE PROPUESTA DE VISITA TÉCNICA (v2026.SUPREME) ---
 */
@HiltViewModel
class WizardVisitaViewModel @Inject constructor(
    private val gestorBorrador: BorradorPerfilPrestadorGestor,
    private val calendarioRepo: PrestadorCalendarioRepositorio,
    private val direccionDao: com.example.myapplication.core.datos.local.dao.DireccionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WizardVisitaUiState())
    val uiState: StateFlow<WizardVisitaUiState> = _uiState.asStateFlow()

    private val formatterFecha = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale.getDefault())

    fun inicializar(
        idSucursal: String?,
        nombreCliente: String,
        urlFotoCliente: String?,
        categoriaActual: String?,
        iconoActual: String?,
        ubicacionesChat: List<MensajeEntity>,
        presupuestosChat: List<PresupuestoResumenDominio>,
        direccionInicial: MensajeEntity? = null
    ) {
        viewModelScope.launch {
            val borrador = gestorBorrador.borrador.value ?: return@launch
            val sucursales = borrador.empresas.flatMap { it.sucursales }
            val sucEncontrada = if (idSucursal != null) sucursales.find { it.id == idSucursal } else null
            
            val direcciones = if (sucEncontrada != null) listOfNotNull(sucEncontrada.direccion) else borrador.prestador.direcciones
            val equipo = sucEncontrada?.equipoTrabajo ?: emptyList()

            _uiState.update { state ->
                state.copy(
                    nombrePrestador = sucEncontrada?.nombre ?: borrador.prestador.perfil.titulo,
                    categoriaServicio = categoriaActual ?: "Servicio Técnico",
                    iconoCategoria = iconoActual ?: "🧰",
                    direccionesOrigen = direcciones,
                    direccionOrigenSeleccionada = direcciones.firstOrNull(),
                    nombreCliente = nombreCliente,
                    urlFotoCliente = urlFotoCliente,
                    direccionesDestinoDisponibles = ubicacionesChat,
                    direccionDestinoSeleccionada = direccionInicial ?: ubicacionesChat.firstOrNull(),
                    presupuestosDisponibles = presupuestosChat,
                    equipoDisponible = equipo,
                    fechaTexto = formatterFecha.format(Date(state.fechaSeleccionadaMillis)).uppercase()
                )
            }
            
            if (idSucursal != null) {
                recalcularDisponibilidad(idSucursal, null, _uiState.value.fechaSeleccionadaMillis)
                _uiState.value.direccionDestinoSeleccionada?.let { 
                    estimarGastos(it.latitud ?: 0.0, it.longitud ?: 0.0, idSucursal)
                }
            }
        }
    }

    fun irAPasoConfiguracion() { _uiState.update { it.copy(pasoActual = PasoWizard.CONFIGURACION) } }
    fun volverAIdentidad() { _uiState.update { it.copy(pasoActual = PasoWizard.IDENTIDAD) } }

    fun seleccionarDireccionOrigen(dir: DireccionDominio) { 
        _uiState.update { it.copy(direccionOrigenSeleccionada = dir) } 
    }

    fun seleccionarDireccionDestino(msg: MensajeEntity, idSucursal: String) {
        _uiState.update { it.copy(direccionDestinoSeleccionada = msg) }
        estimarGastos(msg.latitud ?: 0.0, msg.longitud ?: 0.0, idSucursal)
    }

    fun toggleModo(modo: ModoAgendaTurno) { _uiState.update { it.copy(modoAgenda = modo) } }

    fun establecerFecha(millis: Long, idSucursal: String) {
        _uiState.update { it.copy(
            fechaSeleccionadaMillis = millis,
            fechaTexto = formatterFecha.format(Date(millis)).uppercase(),
            horaSeleccionada = ""
        ) }
        recalcularDisponibilidad(idSucursal, null, millis)
    }

    fun seleccionarHora(hora: String) { _uiState.update { it.copy(horaSeleccionada = hora) } }

    fun toggleTecnico(id: String) {
        _uiState.update { state ->
            val nuevos = if (state.equipoSeleccionadoIds.contains(id)) state.equipoSeleccionadoIds - id
                         else state.equipoSeleccionadoIds + id
            state.copy(equipoSeleccionadoIds = nuevos)
        }
    }

    fun seleccionarPresupuesto(pre: PresupuestoResumenDominio?) {
        _uiState.update { it.copy(presupuestoSeleccionado = pre) }
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

    private fun estimarGastos(lat: Double, lng: Double, idSucursal: String) {
        viewModelScope.launch {
            val dirSucursal = direccionDao.obtenerPorIdSync(idSucursal)
            if (dirSucursal != null) {
                val dist = GeoUtils.calcularDistanciaKm(dirSucursal.latitud, dirSucursal.longitud, lat, lng)
                _uiState.update { it.copy(costoTrasladoEstimado = 500.0 + (dist * 150.0)) }
            }
        }
    }
}


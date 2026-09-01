package com.example.myapplication.prestador.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.SucursalDao
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad.BloqueHorario
import com.example.myapplication.core.utilidades.GeoUtils
import com.example.myapplication.prestador.datos.repositorios.PrestadorCalendarioRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * --- PAN DE MIGA (BREADCRUMB) ---
 * [TÍTULO]: ViewModel de Gestión de Eventos (App Naranja)
 * [PROPÓSITO]: Orquestar la toma de turnos y propuestas de visitas técnicas.
 * [FUNCIONAMIENTO INTERNO]: Consume datos masticados del Repositorio de Calendario.
 * [RELACIÓN]: Se comunica con las BottomSheets de Nueva Visita y Nuevo Turno.
 */
@HiltViewModel
class GestionEventosViewModel @Inject constructor(
    private val calendarioRepositorio: PrestadorCalendarioRepositorio,
    private val sucursalDao: SucursalDao,
    private val direccionDao: com.example.myapplication.core.datos.local.dao.DireccionDao
) : ViewModel() {

    private val _bloquesDisponibles = MutableStateFlow<List<BloqueHorario>>(emptyList())
    val bloquesDisponibles: StateFlow<List<BloqueHorario>> = _bloquesDisponibles.asStateFlow()

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando.asStateFlow()

    private val _costoEstimadoTraslado = MutableStateFlow(0.0)
    val costoEstimadoTraslado: StateFlow<Double> = _costoEstimadoTraslado.asStateFlow()

    private val _fechaSeleccionadaMillis = MutableStateFlow(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis)
    val fechaSeleccionadaMillis: StateFlow<Long> = _fechaSeleccionadaMillis.asStateFlow()

    val fechaFormateada: StateFlow<String> = _fechaSeleccionadaMillis.map {
        SimpleDateFormat("EEEE d 'de' MMMM, yyyy", java.util.Locale.getDefault()).format(java.util.Date(it)).uppercase()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /**
     * Establece una nueva fecha y dispara el recálculo automático en el repositorio.
     */
    fun establecerFecha(millis: Long, idSucursal: String, idRecurso: String? = null) {
        _fechaSeleccionadaMillis.value = millis
        viewModelScope.launch {
            _estaCargando.value = true
            try {
                _bloquesDisponibles.value = calendarioRepositorio.calcularDisponibilidadReal(
                    idSucursal = idSucursal,
                    idRecurso = idRecurso,
                    fechaMillis = millis
                )
            } catch (e: Exception) {
                _bloquesDisponibles.value = emptyList()
            } finally {
                _estaCargando.value = false
            }
        }
    }

    /**
     * Estima el costo de traslado basado en la distancia entre sucursal y cliente.
     */
    fun estimarGastosVisita(latCliente: Double, lngCliente: Double, idSucursal: String) {
        viewModelScope.launch {
            val dirSucursal = direccionDao.obtenerPorIdSync(idSucursal)
            if (dirSucursal != null) {
                val distancia = GeoUtils.calcularDistanciaKm(
                    dirSucursal.latitud, dirSucursal.longitud, latCliente, lngCliente
                )
                // Tarifa Elite: $500 base + $150 por km
                _costoEstimadoTraslado.value = 500.0 + (distancia * 150.0)
            }
        }
    }
}


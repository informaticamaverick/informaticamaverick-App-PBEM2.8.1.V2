package com.example.myapplication.viewmodel.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.datos.repositorios.ClimaRepositorio
import com.example.myapplication.core.dominio.modelos.InformacionClima
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- CLIMA VIEWMODEL (EL OBRERO DE CABECERA v1.0) ---
 * [PROPÓSITO]: Proveer datos de clima de forma ultra-eficiente a la cabecera.
 * [LEY #2]: Costo Zero. Evita recomposiciones por cambios menores.
 */
@HiltViewModel
class ClimaViewModel @Inject constructor(
    private val coordinator: CoordinadorAcciones,
    private val climaRepositorio: ClimaRepositorio
) : ViewModel() {

    private val _climaActivo = MutableStateFlow<InformacionClima?>(null)
    
    // --- ESTADOS PARA LA UI (FORMATEADOS) ---
    
    val temperatura: StateFlow<String> = _climaActivo
        .map { it?.temperatura ?: "--°C" }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "--°C")

    val emojiClima: StateFlow<String> = _climaActivo
        .map { it?.emojiClima ?: "🌤️" }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "🌤️")

    val descripcionClima: StateFlow<String> = _climaActivo
        .map { it?.descripcionClima ?: "Sincronizando..." }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    private val _mostrarDetalles = MutableStateFlow(false)
    val mostrarDetalles = _mostrarDetalles.asStateFlow()

    private val _senalRefresco = MutableSharedFlow<Unit>(replay = 0)

    init {
        // Observación soberana de la ubicación para actualizar el clima
        coordinator.direccionActiva.onEach { address ->
            if (address != null) {
                actualizarClima(address.latitud, address.longitud, address.codigoPostal)
            }
        }.launchIn(viewModelScope)

        // Escuchar señal de refresco manual
        _senalRefresco.onEach {
            coordinator.direccionActiva.first()?.let { address ->
                actualizarClima(address.latitud, address.longitud, address.codigoPostal, forzar = true)
            }
        }.launchIn(viewModelScope)
    }

    private fun actualizarClima(lat: Double, lon: Double, cp: String?, forzar: Boolean = false) {
        viewModelScope.launch {
            try {
                val data = climaRepositorio.obtenerClimaActual(lat, lon, cp, forzar)
                _climaActivo.value = data
            } catch (e: Exception) {
                Log.e("ClimaVM", "❌ Error al obtener clima: ${e.message}")
            }
        }
    }

    /**
     * 🔥 [ELITE]: Forzar la actualización del clima (Bypass cache táctica).
     */
    fun refrescarClima() {
        viewModelScope.launch {
            _senalRefresco.emit(Unit)
        }
    }

    fun alternarDetalles() {
        _mostrarDetalles.value = !_mostrarDetalles.value
    }

    fun establecerVisibilidadDetalles(visible: Boolean) {
        _mostrarDetalles.value = visible
    }
}



package com.example.myapplication.prestador.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.ClimaRepositorio
import com.example.myapplication.core.datos.repositorios.GestorUbicacionGps
import com.example.myapplication.core.dominio.modelos.InformacionClima
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE CLIMA (V2026.FINAL) ---
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: ClimaRepositorio,
    private val gestorUbicacion: GestorUbicacionGps
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _state.value = WeatherState.Loading
            try {
                // [FIX]: estaba hardcodeado a Buenos Aires ("para demo, GPS real en fase 2") —
                // ahora usa el mismo GestorUbicacionGps que ya usa Editar Perfil para detectar
                // GPS. Si falla (sin permiso, sin señal) cae al mismo default de Buenos Aires
                // que ya tenía, en vez de romper la tarjeta de clima.
                val ubicacion = gestorUbicacion.detectarUbicacionActual()
                val lat = ubicacion?.latitud?.takeIf { it != 0.0 } ?: -34.6037
                val lng = ubicacion?.longitud?.takeIf { it != 0.0 } ?: -58.3816
                val data = repository.obtenerClimaActual(lat, lng, nombreCiudad = ubicacion?.localidad)
                if (data != null) {
                    _state.value = WeatherState.Success(data)
                } else {
                    _state.value = WeatherState.Error("No se pudo obtener el clima")
                }
            } catch (e: Exception) {
                _state.value = WeatherState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    sealed interface WeatherState {
        object Loading : WeatherState
        data class Success(val data: InformacionClima) : WeatherState
        data class Error(val message: String) : WeatherState
    }
}
















































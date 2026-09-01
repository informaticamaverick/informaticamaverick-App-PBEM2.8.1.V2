package com.example.myapplication.prestador.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.ClimaRepositorio
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
    private val repository: ClimaRepositorio
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
                // Hardcodigod para demo o obtener de GPS real en fase 2
                val data = repository.obtenerClimaActual(-34.6037, -58.3816)
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
















































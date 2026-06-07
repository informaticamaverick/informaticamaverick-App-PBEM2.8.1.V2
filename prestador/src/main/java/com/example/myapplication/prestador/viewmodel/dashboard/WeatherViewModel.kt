package com.example.myapplication.prestador.viewmodel.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.WeatherData
import com.example.myapplication.core.data.repository.WeatherRepository
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    sealed class WeatherState {
        object Loading : WeatherState()
        data class Success(val data: WeatherData) : WeatherState()
        object Error : WeatherState()
    }

    private val _state = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    private val  fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init { loadWeather() }

    @SuppressLint("MissingPermission")
    fun loadWeather() {
        viewModelScope.launch {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let { loc ->
                            viewModelScope.launch {
                                val cityName = getCityName(loc.latitude, loc.longitude)
                                val data = weatherRepository.getCurrentWeather(loc.latitude, loc.longitude)
                                _state.value = if (data != null)
                                    WeatherState.Success(data.copy(cityName = cityName))
                                else WeatherState.Error
                            }
                        } ?: run { _state.value = WeatherState.Error }
                    }
                    .addOnFailureListener { _state.value = WeatherState.Error }
            } catch (e: Exception) {
                _state.value = WeatherState.Error
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: "Tu ubicación"
        } catch (e: Exception) { " Tu ubicación" }
    }
}
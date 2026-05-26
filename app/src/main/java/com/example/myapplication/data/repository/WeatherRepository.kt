package com.example.myapplication.data.repository

import com.example.myapplication.core.data.remote.weather.WeatherApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CLIMA (APP CLIENTE) ---
 * Obtiene datos meteorológicos para mostrar en la cabecera.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApiService
) {
    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherData? {
        return try {
            val response = weatherApi.getCurrentWeather(lat, lon)
            if (response.isSuccessful) {
                val data = response.body()
                data?.let {
                    WeatherData(
                        temperature = "${it.current.temperature_2m.toInt()}°C",
                        weatherEmoji = "☀️",
                        weatherDescription = "Despejado",
                        windSpeed = "${it.current.windspeed_10m.toInt()} km/h",
                        humidity = "${it.current.relativehumidity_2m}%",
                        cityName = "Lat: ${String.format("%.2f", lat)}"
                    )
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

data class WeatherData(
    val temperature: String,
    val weatherEmoji: String,
    val weatherDescription: String,
    val windSpeed: String,
    val humidity: String,
    val cityName: String
)

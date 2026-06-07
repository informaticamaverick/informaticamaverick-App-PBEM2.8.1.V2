package com.example.myapplication.core.data.repository

import com.example.myapplication.core.data.remote.api.WeatherApiService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CLIMA (COMPARTIDO) ---
 * Obtiene datos meteorológicos para mostrar en la cabecera de las apps.
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
                    val dailyForecast = it.daily.time.mapIndexed { index, time ->
                        DayForecast(
                            dayName = getDayName(time),
                            emoji = getWeatherEmoji(it.daily.weathercode[index]),
                            tempMax = "${it.daily.temperature_2m_max[index].toInt()}°",
                            tempMin = "${it.daily.temperature_2m_min[index].toInt()}°"
                        )
                    }

                    WeatherData(
                        temperature = "${it.current.temperature_2m.toInt()}°C",
                        weatherEmoji = getWeatherEmoji(it.current.weathercode),
                        weatherDescription = getWeatherDescription(it.current.weathercode),
                        windSpeed = "${it.current.windspeed_10m.toInt()} km/h",
                        humidity = "${it.current.relativehumidity_2m}%",
                        cityName = "Lat: ${String.format(java.util.Locale.US, "%.2f", lat)}",
                        forecast = dailyForecast
                    )
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getDayName(dateStr: String): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = sdf.parse(dateStr)
            val displaySdf = java.text.SimpleDateFormat("EEE", java.util.Locale.forLanguageTag("es-AR"))
            val day = displaySdf.format(date!!)
            day.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Hoy"
        }
    }

    private fun getWeatherEmoji(code: Int): String = when (code) {
        0 -> "☀️" // Despejado
        1, 2 -> "🌤️" // Mayormente despejado
        3 -> "☁️" // Nublado
        45, 48 -> "🌫️" // Niebla
        51, 53, 55 -> "🌦️" // Llovizna
        61, 63 -> "🌧️" // Lluvia
        65 -> "⛈️" // Lluvia fuerte
        71, 73, 75 -> "❄️" // Nieve
        77 -> "🌨️" // Granizo
        80, 81, 82 -> "💧" // Chubascos
        85, 86 -> "❄️" // Chubascos de nieve
        95 -> "⚡" // Tormenta
        96, 99 -> "🌩️" // Tormenta fuerte
        else -> "🌡️"
    }

    private fun getWeatherDescription(code: Int): String = when (code) {
        0 -> "Cielo despejado"
        1 -> "Principalmente despejado"
        2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla y escarcha"
        51 -> "Llovizna ligera"
        53 -> "Llovizna moderada"
        55 -> "Llovizna densa"
        61 -> "Lluvia ligera"
        63 -> "Lluvia moderada"
        65 -> "Lluvia fuerte"
        71 -> "Nevada ligera"
        73 -> "Nevada moderada"
        75 -> "Nevada fuerte"
        77 -> "Granizo"
        80 -> "Chubascos ligeros"
        81 -> "Chubascos moderados"
        82 -> "Chubascos violentos"
        85 -> "Chubascos de nieve ligeros"
        86 -> "Chubascos de nieve fuertes"
        95 -> "Tormenta"
        96 -> "Tormenta con granizo ligero"
        99 -> "Tormenta con granizo fuerte"
        else -> "Condiciones variables"
    }
}

data class WeatherData(
    val temperature: String,
    val weatherEmoji: String,
    val weatherDescription: String,
    val windSpeed: String,
    val humidity: String,
    val cityName: String,
    val forecast: List<DayForecast> = emptyList()
)

data class DayForecast(
    val dayName: String,
    val emoji: String,
    val tempMax: String,
    val tempMin: String
)

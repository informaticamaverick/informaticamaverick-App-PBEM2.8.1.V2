package com.example.myapplication.core.data.remote.weather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * --- INTERFAZ API DE CLIMA (OPEN-METEO) ---
 */
interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weathercode,windspeed_10m,relativehumidity_2m",
        @Query("daily") daily: String = "weathercode,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7
    ): Response<OpenMeteoResponse>
}

data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeather,
    val daily: DailyForecast
)

data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val weathercode: Int,
    val windspeed_10m: Double,
    val relativehumidity_2m: Int
)

data class DailyForecast(
    val time: List<String>,
    val weathercode: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

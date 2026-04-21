package com.example.myapplication.data.model

data class OpenMeteoResponse(
    val current_weather: CurrentWeather,
    val hourly: HourlyWeather? = null,
    val daily: DailyWeather? = null
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val windspeed: Double? = null,
    val winddirection: Int? = null
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relativehumidity_2m: List<Int>? = null,
    val windspeed_10m: List<Double>? = null
)

data class DailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val weathercode: List<Int>,
    val precipitation_sum: List<Double>? = null,
    val windspeed_10m_max: List<Double>? = null
)

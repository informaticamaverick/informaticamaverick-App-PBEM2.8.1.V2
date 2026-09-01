package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELOS DE CLIMA (V2026.FINAL) ---
 */
data class InformacionClima(
    val nombreCiudad: String = "Buenos Aires",
    val temperatura: String = "20°C",
    val emojiClima: String = "☀️",
    val descripcionClima: String = "Despejado",
    val humedad: String = "50%",
    val velocidadViento: String = "10 km/h",
    val pronostico: List<PronosticoDia> = emptyList()
)

data class PronosticoDia(
    val nombreDia: String,
    val emoji: String,
    val tempMax: String,
    val tempMin: String
)


































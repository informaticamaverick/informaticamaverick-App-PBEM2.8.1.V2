package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.remoto.api.WeatherApiService
import com.example.myapplication.core.dominio.modelos.InformacionClima
import com.example.myapplication.core.dominio.modelos.PronosticoDia
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CLIMA (Atómico - v2026.ELITE) ---
 * 
 * [PROPÓSITO]: Proveer datos meteorológicos en tiempo real para mejorar la toma 
 * de decisiones tanto del cliente como del prestador.
 */
@Singleton
class ClimaRepositorio @Inject constructor(
    private val weatherApi: WeatherApiService
) {
    // --- SECTOR: CACHÉ TÁCTICA (Ley de Costo Zero) ---
    private var cacheClima: InformacionClima? = null
    private var ultimoCP: String? = null
    private var ultimaLat: Double = 0.0
    private var ultimaLng: Double = 0.0
    private var ultimaMarcaTiempo: Long = 0L

    private val UMBRAL_DISTANCIA_KM = 10.0 // 🔥 [ELITE] Umbral reducido para más precisión
    private val UMBRAL_TIEMPO_MS = 2 * 60 * 60 * 1000 // 2 Horas

    /**
     * Obtiene el clima actual basado en coordenadas con lógica de caché inteligente.
     * [OPTIMIZACIÓN]: Prioriza el Código Postal (CP) para evitar llamadas redundantes.
     */
    suspend fun obtenerClimaActual(
        latitud: Double,
        longitud: Double,
        codigoPostal: String? = null,
        forzar: Boolean = false,
        nombreCiudad: String? = null
    ): InformacionClima? {
        val ahora = System.currentTimeMillis()
        
        if (!forzar) {
            cacheClima?.let { clima ->
                val tiempoTranscurrido = ahora - ultimaMarcaTiempo
                
                // 1. Validar por CP (Más eficiente)
                if (codigoPostal != null && codigoPostal == ultimoCP && tiempoTranscurrido < UMBRAL_TIEMPO_MS) {
                    android.util.Log.d("WeatherRepo", "❄️ [CACHE_HIT] Clima por CP: $codigoPostal")
                    return clima
                }

                // 2. Validar por Distancia (Fallback si no hay CP)
                val distancia = CalculadoraGeografica.calcularDistanciaKm(latitud, longitud, ultimaLat, ultimaLng)
                if (distancia < UMBRAL_DISTANCIA_KM && tiempoTranscurrido < UMBRAL_TIEMPO_MS) {
                    android.util.Log.d("WeatherRepo", "❄️ [CACHE_HIT] Clima por Distancia (${"%.2f".format(distancia)}km)")
                    return clima
                }
            }
        }

        return try {
            android.util.Log.d("WeatherRepo", "🌐 [API_FETCH] Consultando clima nuevo.")
            val response = weatherApi.getCurrentWeather(
                latitude = latitud,
                longitude = longitud,
                current = "temperature_2m,weathercode,windspeed_10m,relativehumidity_2m",
                daily = "weathercode,temperature_2m_max,temperature_2m_min",
                timezone = "auto",
                forecastDays = 7
            )
            
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    val nuevoClima = InformacionClima(
                        // [FIX]: nunca se pasaba — la tarjeta de clima siempre mostraba el
                        // default "Buenos Aires" del modelo, sin importar la ubicación real
                        // usada para pedir la temperatura (Open-Meteo no devuelve nombre de
                        // ciudad, hay que traerlo de la geocodificación inversa del GPS).
                        nombreCiudad = nombreCiudad?.takeIf { it.isNotBlank() } ?: "Buenos Aires",
                        temperatura = "${data.current.temperature_2m.toInt()}°C",
                        emojiClima = mapCodeToEmoji(data.current.weathercode),
                        descripcionClima = mapCodeToDescription(data.current.weathercode),
                        humedad = "${data.current.relativehumidity_2m}%",
                        velocidadViento = "${data.current.windspeed_10m} km/h",
                        pronostico = data.daily.time.indices.map { i ->
                            PronosticoDia(
                                nombreDia = data.daily.time[i],
                                emoji = mapCodeToEmoji(data.daily.weathercode[i]),
                                tempMax = "${data.daily.temperature_2m_max[i].toInt()}°",
                                tempMin = "${data.daily.temperature_2m_min[i].toInt()}°"
                            )
                        }
                    )
                    
                    cacheClima = nuevoClima
                    ultimoCP = codigoPostal
                    ultimaLat = latitud
                    ultimaLng = longitud
                    ultimaMarcaTiempo = ahora
                    
                    nuevoClima
                }
            } else null
        } catch (e: Exception) { null }
    }

    private fun mapCodeToEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2, 3 -> "🌤️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌦️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }

    private fun mapCodeToDescription(code: Int): String = when (code) {
        0 -> "Despejado"
        1, 2, 3 -> "Parcialmente Nublado"
        45, 48 -> "Niebla"
        51, 53, 55 -> "Llovizna"
        61, 63, 65 -> "Lluvia"
        71, 73, 75 -> "Nieve"
        80, 81, 82 -> "Chubascos"
        95, 96, 99 -> "Tormenta"
        else -> "Estable"
    }
}



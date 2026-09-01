package com.example.myapplication.core.utilidades

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import com.example.myapplication.core.dominio.ubicacion.CalculadoraGeografica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

/**
 * --- GEO UTILS (PREMIUM ENTERPRISE EDITION) ---
 * Centraliza la inteligencia geográfica.
 * [v2026.ELITE]: Optimizaciones agresivas para captura de datos por GPS.
 * [LEY #9]: Estándar Maverick en Español.
 */
object GeoUtils {

    private const val RADIO_TIERRA_KM = 6371.0
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /**
     * Genera un Geohash (Standard Base32) para una coordenada.
     */
    fun calcularGeohash(lat: Double, lng: Double, precision: Int = 9): String {
        return CalculadoraGeografica.generarGeohash(lat, lng, precision)
    }

    /**
     * Verifica si el sensor GPS del dispositivo está encendido.
     */
    fun estaGpsHabilitado(contexto: Context): Boolean {
        val administradorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            administradorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Calcula la distancia en KM entre dos puntos usando la fórmula de Haversine.
     */
    fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return RADIO_TIERRA_KM * c
    }

    /**
     * Estima los minutos de llegada basados en la distancia.
     */
    fun estimarMinutosLlegada(distanciaKm: Double, multiplicadorVelocidad: Double = 5.0): Int {
        return (distanciaKm * multiplicadorVelocidad).toInt().coerceAtLeast(3)
    }

    /**
     * Verifica si una ubicación manual está dentro del rango aceptable del GPS.
     */
    fun verificarUbicacionGps(latReal: Double, lngReal: Double, latManual: Double, lngManual: Double): Boolean {
        val distancia = calcularDistanciaKm(latReal, lngReal, latManual, lngManual) * 1000 
        return distancia <= 100 
    }

    /**
     * [REVERSE GEOCODING]: Obtiene Direccion a partir de coordenadas.
     */
    suspend fun obtenerDireccionDesdeCoordenadas(
        contexto: Context,
        lat: Double,
        lng: Double
    ): DireccionDominio? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(contexto, Locale.getDefault())
            @Suppress("DEPRECATION")
            val resultados = geocoder.getFromLocation(lat, lng, 1)

            if (!resultados.isNullOrEmpty()) {
                val a = resultados[0]
                construirDireccion(a, lat, lng)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * [FORWARD GEOCODING]: Traduce texto a coordenadas y desglose Direccion.
     */
    suspend fun obtenerDireccionDesdeTexto(
        contexto: Context,
        textoDireccion: String
    ): DireccionDominio? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(contexto, Locale.getDefault())
            @Suppress("DEPRECATION")
            val resultados = geocoder.getFromLocationName(textoDireccion, 1)

            if (!resultados.isNullOrEmpty()) {
                val r = resultados[0]
                construirDireccion(r, r.latitude, r.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 🔥 [BUILDER ELITE]: Inteligencia de extracción de datos para Direccion.
     */
    private fun construirDireccion(a: Address, lat: Double, lng: Double): DireccionDominio {
        val provincia = a.adminArea ?: ""
        val zip = NormalizadorDirecciones.limpiarCodigoPostal(a.postalCode ?: "")
        val localidad = a.locality ?: a.subAdminArea ?: a.adminArea ?: a.featureName ?: ""
        
        var numeroCalle = a.subThoroughfare ?: ""
        if (numeroCalle.isBlank()) {
            numeroCalle = a.featureName?.filter { it.isDigit() } ?: ""
        }

        var nombreCalle = a.thoroughfare ?: ""
        if (nombreCalle.isBlank()) {
            val lineaCompleta = a.getAddressLine(0) ?: ""
            if (numeroCalle.isNotBlank() && lineaCompleta.contains(numeroCalle)) {
                nombreCalle = lineaCompleta.split(",").firstOrNull()?.replace(numeroCalle, "")?.trim() ?: ""
            } else {
                nombreCalle = lineaCompleta.split(",").firstOrNull()?.trim() ?: ""
            }
        }

        if (nombreCalle.isBlank() && !a.featureName.isNullOrBlank() && !a.featureName.any { it.isDigit() }) {
            nombreCalle = a.featureName
        }

        return DireccionDominio(
            calle = nombreCalle,
            numero = numeroCalle,
            localidad = localidad,
            provincia = provincia,
            pais = a.countryName ?: "Argentina",
            codigoPostal = zip,
            latitud = lat,
            longitud = lng,
            geohash = calcularGeohash(lat, lng),
            estaVerificadaGps = true,
            etiqueta = "Ubicación Detectada"
        )
    }

    /**
     * [SUV RESCUE]: Resuelve una entrada de texto compleja (Link, Coordenadas o Dirección).
     */
    suspend fun resolverEntradaUbicacion(contexto: Context, entrada: String): DireccionDominio? {
        val limpia = entrada.trim()
        val regexCoordenadas = Regex("""(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)""")
        val match = regexCoordenadas.find(limpia)
        if (match != null) {
            val lat = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val lng = match.groupValues[2].toDoubleOrNull() ?: 0.0
            return obtenerDireccionDesdeCoordenadas(contexto, lat, lng)
        }

        if (limpia.startsWith("http")) {
            val urlResuelta = if (limpia.contains("maps.app.goo.gl")) resolverUrlCorta(limpia) else limpia
            val coordenadas = extraerCoordenadasDesdeUrl(urlResuelta ?: limpia)
            if (coordenadas != null) {
                return obtenerDireccionDesdeCoordenadas(contexto, coordenadas.first, coordenadas.second)
            }
        }
        return obtenerDireccionDesdeTexto(contexto, limpia)
    }

    private suspend fun resolverUrlCorta(urlCorta: String): String? = withContext(Dispatchers.IO) {
        try {
            val conexion = java.net.URL(urlCorta).openConnection() as java.net.HttpURLConnection
            conexion.instanceFollowRedirects = false
            conexion.connect()
            val urlExpandida = conexion.getHeaderField("Location")
            conexion.disconnect()
            urlExpandida
        } catch (e: Exception) {
            null
        }
    }

    private fun extraerCoordenadasDesdeUrl(url: String): Pair<Double, Double>? {
        val matchAt = Regex("""@(-?\d+\.\d+),(-?\d+\.\d+)""").find(url)
        if (matchAt != null) {
            val lat = matchAt.groupValues[1].toDoubleOrNull()
            val lng = matchAt.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
        val matchD = Regex("""!3d(-?\d+\.\d+)!4d(-?\d+\.\d+)""").find(url)
        if (matchD != null) {
            val lat = matchD.groupValues[1].toDoubleOrNull()
            val lng = matchD.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) return lat to lng
        }
        return null
    }
}

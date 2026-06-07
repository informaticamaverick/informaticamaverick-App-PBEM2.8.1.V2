package com.example.myapplication.core.utils

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import com.example.myapplication.core.domain.model.AddressUnico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

/**
 * --- MAVERICK GEO UTILS (PREMIUM ENTERPRISE EDITION) ---
 * Centraliza la inteligencia geográfica del ecosistema Maverick.
 */
object MaverickGeoUtils {

    private const val EARTH_RADIUS_KM = 6371.0
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /**
     * Genera un Geohash (Standard Base32) para una coordenada.
     * [ELITE]: Permite búsquedas por radio y proximidad en Firestore sin queries pesadas.
     * @param precision Longitud del hash (por defecto 9 para ~4.8m de precisión).
     */
    fun computeGeohash(lat: Double, lng: Double, precision: Int = 9): String {
        val latRange = doubleArrayOf(-90.0, 90.0)
        val lngRange = doubleArrayOf(-180.0, 180.0)
        var isEven = true
        var bit = 0
        var ch = 0
        val geohash = StringBuilder()

        while (geohash.length < precision) {
            val mid: Double
            if (isEven) {
                mid = (lngRange[0] + lngRange[1]) / 2
                if (lng > mid) {
                    ch = ch or (1 shl (4 - bit))
                    lngRange[0] = mid
                } else {
                    lngRange[1] = mid
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2
                if (lat > mid) {
                    ch = ch or (1 shl (4 - bit))
                    latRange[0] = mid
                } else {
                    latRange[1] = mid
                }
            }

            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                geohash.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }
        return geohash.toString()
    }

    /**
     * Mapa de Provincias para normalización de CPA (Código Postal Argentino).
     * El CPA premium requiere la letra de la provincia (ISO 3166-2:AR).
     */
    private val PROVINCE_MAP = mapOf(
        "buenos aires" to "B", "ciudad autonoma de buenos aires" to "C", "caba" to "C",
        "catamarca" to "K", "chaco" to "H", "chubut" to "U", "cordoba" to "X",
        "corrientes" to "W", "entre rios" to "E", "formosa" to "P", "jujuy" to "Y",
        "la pampa" to "L", "la rioja" to "F", "mendoza" to "M", "misiones" to "N",
        "neuquen" to "Q", "rio negro" to "R", "salta" to "A", "san juan" to "J",
        "san luis" to "D", "santa cruz" to "Z", "santa fe" to "S", "santiago del estero" to "G",
        "tierra del fuego" to "V", "tucuman" to "T"
    )

    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Estima el tiempo de llegada basado en la distancia.
     */
    fun estimateArrivalMinutes(distanceKm: Double, speedMultiplier: Double = 5.0): Int {
        return (distanceKm * speedMultiplier).toInt().coerceAtLeast(3)
    }

    /**
     * Verifica si el sensor GPS del dispositivo está encendido.
     */
    fun isGpsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * [REVERSE GEOCODING]: Obtiene AddressUnico a partir de coordenadas.
     */
    suspend fun getAddressFromCoordinates(
        context: Context,
        lat: Double,
        lng: Double
    ): AddressUnico? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val province = a.adminArea ?: ""
                val cleanZip = normalizeCPA(province, a.postalCode ?: "")
                val locality = a.locality ?: a.subLocality ?: a.subAdminArea ?: ""

                // Intento robusto de obtener el número de la dirección
                val streetNumber = a.subThoroughfare ?: a.featureName?.filter { it.isDigit() } ?: ""
                
                // Intento robusto de obtener la calle (a veces thoroughfare es null)
                var streetName = a.thoroughfare ?: ""
                if (streetName.isBlank() && !a.featureName.isNullOrBlank() && !a.featureName.any { it.isDigit() }) {
                    streetName = a.featureName
                }
                
                // Fallback final: Parsear la primera línea de la dirección si sigue vacía
                if (streetName.isBlank()) {
                    val fullLine = a.getAddressLine(0) ?: ""
                    streetName = fullLine.split(",").firstOrNull()?.replace(streetNumber, "")?.trim() ?: ""
                }

                AddressUnico(
                    calle = streetName,
                    numero = streetNumber,
                    localidad = locality,
                    provincia = province,
                    pais = a.countryName ?: "Argentina",
                    codigoPostal = cleanZip,
                    latitude = lat,
                    longitude = lng,
                    label = "Ubicación Detectada"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * [FORWARD GEOCODING]: Traduce texto a coordenadas y desglose AddressUnico.
     */
    suspend fun getAddressFromText(
        context: Context,
        addressText: String
    ): AddressUnico? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(addressText, 1)

            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val province = a.adminArea ?: ""
                val cleanZip = normalizeCPA(province, a.postalCode ?: "")
                val locality = a.locality ?: a.subLocality ?: a.subAdminArea ?: ""
                val streetNumber = a.subThoroughfare ?: a.featureName?.filter { it.isDigit() } ?: ""
                var streetName = a.thoroughfare ?: ""
                
                if (streetName.isBlank() && !a.featureName.isNullOrBlank() && !a.featureName.any { it.isDigit() }) {
                    streetName = a.featureName
                }

                if (streetName.isBlank()) {
                    val fullLine = a.getAddressLine(0) ?: ""
                    streetName = fullLine.split(",").firstOrNull()?.replace(streetNumber, "")?.trim() ?: ""
                }

                AddressUnico(
                    calle = streetName,
                    numero = streetNumber,
                    localidad = locality,
                    provincia = province,
                    pais = a.countryName ?: "Argentina",
                    codigoPostal = cleanZip,
                    latitude = a.latitude,
                    longitude = a.longitude,
                    label = "Búsqueda"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Normaliza el código postal al formato CPA (Letra + 4 dígitos).
     * Ejemplo: "4000" en "Tucumán" -> "T4000"
     */
    private fun normalizeCPA(province: String, rawZip: String): String {
        if (rawZip.isBlank()) return ""

        // Si ya tiene el formato CPA (Letra + Números), lo devolvemos limpio
        if (rawZip.length >= 5 && rawZip[0].isLetter() && rawZip[1].isDigit()) {
            return rawZip.uppercase()
        }

        // Si son solo números, buscamos la letra de la provincia
        val provinceKey = province.lowercase(Locale.ROOT).trim()
        val letter = PROVINCE_MAP[provinceKey]
        return if (letter != null && rawZip.all { it.isDigit() }) {
            "$letter$rawZip"
        } else {
            rawZip.uppercase()
        }
    }
}

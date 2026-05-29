package com.example.myapplication.core.utils

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import com.example.myapplication.core.domain.model.AddressClient
import com.example.myapplication.core.domain.model.AddressProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*

/**
 * --- MAVERICK GEO UTILS (PREMIUM ENTERPRISE EDITION) ---
 * Centraliza la inteligencia geográfica del ecosistema Maverick.
 * Utiliza el motor de Google Maps (Geocoder) con lógica de discriminación
 * avanzada para Argentina (CPA, Localidades, Departamentos).
 */
object MaverickGeoUtils {

    private const val EARTH_RADIUS_KM = 6371.0

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
     * [REVERSE GEOCODING]: Obtiene dirección PREMIUM a partir de coordenadas.
     * Implementa lógica de discriminación de CPA (Ej: T4000) y jerarquía administrativa.
     */
    suspend fun getAddressFromCoordinates(
        context: Context,
        lat: Double,
        lng: Double
    ): AddressClient? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val province = a.adminArea ?: ""
                val rawZip = a.postalCode ?: ""
                
                // Normalización de CPA (Premium Argentina)
                val cleanZip = normalizeCPA(province, rawZip)
                
                // Discriminación inteligente Ciudad/Localidad/Barrio
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

                AddressClient(
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
     * [FORWARD GEOCODING]: Traduce texto a coordenadas y desglose PREMIUM.
     */
    suspend fun getAddressFromText(
        context: Context,
        addressText: String
    ): AddressClient? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(addressText, 1)

            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val province = a.adminArea ?: ""
                val rawZip = a.postalCode ?: ""
                val locality = a.locality ?: a.subLocality ?: a.subAdminArea ?: ""
                
                // Normalización de CPA
                val cleanZip = normalizeCPA(province, rawZip)

                // Lógica robusta para calle y número (igual que en Reverse Geocoding)
                val streetNumber = a.subThoroughfare ?: a.featureName?.filter { it.isDigit() } ?: ""
                var streetName = a.thoroughfare ?: ""
                
                if (streetName.isBlank() && !a.featureName.isNullOrBlank() && !a.featureName.any { it.isDigit() }) {
                    streetName = a.featureName
                }

                if (streetName.isBlank()) {
                    val fullLine = a.getAddressLine(0) ?: ""
                    streetName = fullLine.split(",").firstOrNull()?.replace(streetNumber, "")?.trim() ?: ""
                }

                AddressClient(
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
     * Convierte un AddressClient a AddressProvider para interoperabilidad.
     */
    fun clientToProvider(a: AddressClient): AddressProvider = AddressProvider(
        id = a.id, calle = a.calle, numero = a.numero, localidad = a.localidad,
        provincia = a.provincia, pais = a.pais, codigoPostal = a.codigoPostal,
        latitude = a.latitude, longitude = a.longitude, label = a.label
    )

    /**
     * Convierte un AddressProvider a AddressClient para interoperabilidad.
     */
    fun providerToClient(a: AddressProvider): AddressClient = AddressClient(
        id = a.id, calle = a.calle, numero = a.numero, localidad = a.localidad,
        provincia = a.provincia, pais = a.pais, codigoPostal = a.codigoPostal,
        latitude = a.latitude, longitude = a.longitude, label = a.label
    )

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


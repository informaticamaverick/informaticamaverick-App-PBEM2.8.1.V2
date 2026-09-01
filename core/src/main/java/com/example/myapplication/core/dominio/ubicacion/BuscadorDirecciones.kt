package com.example.myapplication.core.dominio.ubicacion

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * --- BUSCADOR DE DIRECCIONES (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Traducir entre coordenadas y direcciones legibles (Geocoder).
 * [LEY #17]: Protocolo de Bautizo.
 */
object BuscadorDirecciones {

    /**
     * Obtiene una dirección completa a partir de latitud y longitud.
     */
    suspend fun obtenerDesdeCoordenadas(
        contexto: Context,
        lat: Double,
        lng: Double
    ): DireccionDominio? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(contexto, Locale.getDefault())
            @Suppress("DEPRECATION")
            val resultados = geocoder.getFromLocation(lat, lng, 1)

            if (!resultados.isNullOrEmpty()) {
                mapearAddressADireccion(resultados[0], lat, lng)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Traduce un texto a coordenadas y desglose de dirección.
     */
    suspend fun obtenerDesdeTexto(
        contexto: Context,
        direccionTexto: String
    ): DireccionDominio? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(contexto, Locale.getDefault())
            @Suppress("DEPRECATION")
            val resultados = geocoder.getFromLocationName(direccionTexto, 1)

            if (!resultados.isNullOrEmpty()) {
                val r = resultados[0]
                mapearAddressADireccion(r, r.latitude, r.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resuelve una entrada compleja (Link, Coordenadas o Texto).
     */
    suspend fun resolverEntradaUbicacion(contexto: Context, entrada: String): DireccionDominio? {
        val limpia = entrada.trim()
        val regexCoordenadas = Regex("""(-?\d+\.\d+)\s*,\s*(-?\d+\.\d+)""")
        val match = regexCoordenadas.find(limpia)
        
        if (match != null) {
            val lat = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val lng = match.groupValues[2].toDoubleOrNull() ?: 0.0
            return obtenerDesdeCoordenadas(contexto, lat, lng)
        }

        // [PENDIENTE]: Lógica de resolución de links de Google Maps si es necesario
        return obtenerDesdeTexto(contexto, limpia)
    }

    private fun mapearAddressADireccion(a: Address, lat: Double, lng: Double): DireccionDominio {
        val zip = NormalizadorDirecciones.limpiarCodigoPostal(a.postalCode ?: "")
        val localidad = a.locality ?: a.subAdminArea ?: a.adminArea ?: a.featureName ?: ""
        
        var numeroCalle = a.subThoroughfare ?: ""
        if (numeroCalle.isBlank()) {
            numeroCalle = a.featureName?.filter { it.isDigit() } ?: ""
        }

        var nombreCalle = a.thoroughfare ?: ""
        if (nombreCalle.isBlank()) {
            val lineaCompleta = a.getAddressLine(0) ?: ""
            nombreCalle = if (numeroCalle.isNotBlank() && lineaCompleta.contains(numeroCalle)) {
                lineaCompleta.split(",").firstOrNull()?.replace(numeroCalle, "")?.trim() ?: ""
            } else {
                lineaCompleta.split(",").firstOrNull()?.trim() ?: ""
            }
        }

        return DireccionDominio(
            calle = nombreCalle,
            numero = numeroCalle,
            localidad = localidad,
            provincia = a.adminArea ?: "",
            pais = a.countryName ?: "Argentina",
            codigoPostal = zip,
            latitud = lat,
            longitud = lng,
            geohash = CalculadoraGeografica.generarGeohash(lat, lng),
            estaVerificadaGps = true,
            etiqueta = "Ubicación Detectada"
        )
    }
}

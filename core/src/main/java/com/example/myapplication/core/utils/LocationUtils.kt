package com.example.myapplication.core.utils

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.example.myapplication.core.domain.model.AddressClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * --- LOCATION UTILS (COMPARTIDO) ---
 * Centraliza la lógica de geocodificación (Texto <-> Coordenadas).
 * Evita la redundancia de código en los ViewModels y garantiza que todas las direcciones
 * se normalicen bajo el mismo estándar de calidad.
 */
object LocationUtils {

    /**
     * Obtiene una dirección desglosada a partir de coordenadas GPS.
     */
    suspend fun getAddressFromCoordinates(
        context: Context,
        lat: Double,
        lng: Double
    ): AddressClient? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Implementación moderna (API 33+)
                val results = mutableListOf<android.location.Address>()
                // Nota: getFromLocation con callback es asíncrono, pero aquí usamos el wrapper síncrono por simplicidad en IO
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1)
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(lat, lng, 1)
            }

            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                AddressClient(
                    calle = a.thoroughfare ?: "",
                    numero = a.subThoroughfare ?: "",
                    localidad = a.locality ?: a.subAdminArea ?: "",
                    provincia = a.adminArea ?: "",
                    pais = a.countryName ?: "Argentina",
                    codigoPostal = a.postalCode ?: "",
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
     * Obtiene coordenadas y desglose a partir de un texto de dirección.
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
                AddressClient(
                    calle = a.thoroughfare ?: "",
                    numero = a.subThoroughfare ?: "",
                    localidad = a.locality ?: a.subAdminArea ?: "",
                    provincia = a.adminArea ?: "",
                    pais = a.countryName ?: "Argentina",
                    codigoPostal = a.postalCode ?: "",
                    latitude = a.latitude,
                    longitude = a.longitude,
                    label = "Búsqueda"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

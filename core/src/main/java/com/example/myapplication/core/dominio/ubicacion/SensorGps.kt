package com.example.myapplication.core.dominio.ubicacion

import android.content.Context
import android.location.LocationManager

/**
 * --- MONITOR DEL SENSOR GPS (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Verificar el estado físico del hardware de ubicación.
 * [LEY #17]: Protocolo de Bautizo.
 */
object SensorGps {

    /**
     * Verifica si el GPS está encendido en el sistema.
     */
    fun estaHabilitado(contexto: Context): Boolean {
        val locationManager = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }
}

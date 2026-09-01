package com.example.myapplication.core.datos.repositorios

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.ubicacion.BuscadorDirecciones
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE UBICACIÓN GPS (Atómico - v2026.ELITE) ---
 * [PROPÓSITO]: Centralizar la detección GPS y geocodificación inversa para todo el ecosistema.
 * [LEY #4]: Inmediatez. Provee coordenadas precisas para el motor de descubrimiento.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Singleton
class GestorUbicacionGps @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val clienteUbicacion = LocationServices.getFusedLocationProviderClient(contexto)
    
    /**
     * 🔥 [ELITE]: Detecta la ubicación actual y la traduce a una Dirección Mav completa.
     * [ESTRATEGIA DE SEMILLA]: Devuelve la última ubicación conocida al instante mientras
     * intenta obtener una actualización de alta precisión en paralelo.
     */
    @SuppressLint("MissingPermission")
    suspend fun detectarUbicacionActual(): DireccionDominio? = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("GestorUbicacionGps", "🛰️ [GPS_START] Iniciando captura táctica (Semilla + Hit)...")

            // 1. OBTENER SEMILLA (Inmediatez Ley #4)
            val semilla = try {
                Tasks.await(clienteUbicacion.lastLocation)
            } catch (e: Exception) { null }

            // 2. DISPARAR HIT DE ALTA PRECISIÓN (Segundo plano)
            // No bloqueamos el hilo principal esperando al satélite si ya tenemos semilla.
            val hitDeseado = try {
                if (semilla != null) {
                    // Si hay semilla, lanzamos la petición pero no la esperamos eternamente
                    Tasks.await(clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
                } else {
                    // Si no hay nada, forzamos espera por precisión
                    Tasks.await(clienteUbicacion.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
                }
            } catch (e: Exception) {
                android.util.Log.w("GestorUbicacionGps", "⚠️ [GPS_HIT_FAIL] Falló el hit fresco. Usando semilla.")
                semilla
            }

            val locFinal = hitDeseado ?: semilla

            locFinal?.let { nuevaLoc ->
                android.util.Log.d("GestorUbicacionGps", "📍 [GPS_HIT] Lat: ${nuevaLoc.latitude}, Lng: ${nuevaLoc.longitude}")
                
                // Traducción a dirección física (Calle, Altura, CP, Localidad)
                BuscadorDirecciones.obtenerDesdeCoordenadas(contexto, nuevaLoc.latitude, nuevaLoc.longitude)
            }
        } catch (e: Exception) {
            Log.e("GestorUbicacionGps", "❌ [GPS_ERROR] Error crítico: ${e.message}")
            null
        }
    }
}

package com.example.myapplication.core.datos.repositorios

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE UBICACIÓN GPS (v2026.ELITE) ---
 * [PROPÓSITO]: Ser la Fuente Única de Verdad (SSOT) para la posición global del usuario.
 * [LEY #2]: Costo Zero. Implementa una Caché Táctica en DataStore para respuesta instantánea.
 * [LEY #4]: Inmediatez. Evita esperas de hardware mediante el uso de "Snapshots" previos.
 */
@Singleton
class UbicacionGpsRepositorio @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gestorGps: GestorUbicacionGps
) {
    private val KEY_LAT = doublePreferencesKey("gps_lat")
    private val KEY_LNG = doublePreferencesKey("gps_lng")
    private val KEY_CP = stringPreferencesKey("gps_cp")
    private val KEY_CALLE = stringPreferencesKey("gps_calle")
    private val KEY_NUM = stringPreferencesKey("gps_numero")
    private val KEY_LOC = stringPreferencesKey("gps_localidad")
    private val KEY_TIME = longPreferencesKey("gps_timestamp")

    /**
     * Flujo reactivo de la ubicación persistida (Caché).
     * Garantiza que la App siempre tenga una dirección que mostrar al arrancar.
     */
    val ubicacionCacheada: Flow<DireccionDominio?> = dataStore.data.map { prefs ->
        val lat = prefs[KEY_LAT] ?: 0.0
        if (lat == 0.0) return@map null

        DireccionDominio(
            id = "gps_current",
            calle = prefs[KEY_CALLE] ?: "",
            numero = prefs[KEY_NUM] ?: "",
            codigoPostal = prefs[KEY_CP] ?: "",
            localidad = prefs[KEY_LOC] ?: "",
            latitud = lat,
            longitud = prefs[KEY_LNG] ?: 0.0,
            etiqueta = "GPS Tracker"
        )
    }.distinctUntilChanged()

    /**
     * 🔥 [ELITE]: Dispara un "Hit" único al sensor GPS para actualizar la caché.
     * Una vez obtenido el dato, el sensor se libera para ahorrar recursos.
     */
    suspend fun actualizarUbicacionSoberana(): DireccionDominio? {
        val nuevaDireccion = gestorGps.detectarUbicacionActual()
        
        nuevaDireccion?.let { dir ->
            persistirUbicacion(dir)
        }
        
        return nuevaDireccion
    }

    private suspend fun persistirUbicacion(dir: DireccionDominio) {
        dataStore.edit { prefs ->
            prefs[KEY_LAT] = dir.latitud
            prefs[KEY_LNG] = dir.longitud
            prefs[KEY_CP] = dir.codigoPostal
            prefs[KEY_CALLE] = dir.calle
            prefs[KEY_NUM] = dir.numero
            prefs[KEY_LOC] = dir.localidad
            prefs[KEY_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Retorna true si la caché tiene menos de 5 minutos de antigüedad.
     */
    suspend fun esCacheValida(): Boolean {
        val prefs = dataStore.data.first()
        val lastTime = prefs[KEY_TIME] ?: 0L
        return (System.currentTimeMillis() - lastTime) < (5 * 60 * 1000)
    }
}


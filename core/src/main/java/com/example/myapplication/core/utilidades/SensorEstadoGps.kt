package com.example.myapplication.core.utilidades

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- SENSOR DE ESTADO GPS (app CORE ELITE) ---
 * [PROPÓSITO]: Provee flujos reactivos en tiempo real sobre el estado del hardware (GPS, Internet).
 * [LEY #9]: Estándar Maverick en Español. Una sola fuente de verdad para los sensores del sistema.
 */
@Singleton
class SensorEstadoGps @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val contexto: Context
) {
    private val administradorConectividad = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val administradorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Flujo reactivo del estado de conexión a Internet.
     */
    val estaEnLinea: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }

        val solicitud = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        administradorConectividad.registerNetworkCallback(solicitud, callback)

        // Estado inicial
        val actual = administradorConectividad.activeNetwork?.let {
            administradorConectividad.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        trySend(actual)

        awaitClose { administradorConectividad.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /**
     * Flujo reactivo del estado del sensor GPS (Habilitado/Deshabilitado).
     */
    val estaGpsHabilitado: Flow<Boolean> = callbackFlow {
        val receptor = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    trySend(administradorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER))
                }
            }
        }

        contexto.registerReceiver(receptor, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        
        // Estado inicial
        trySend(administradorUbicacion.isProviderEnabled(LocationManager.GPS_PROVIDER))

        awaitClose { contexto.unregisterReceiver(receptor) }
    }.distinctUntilChanged()

    /**
     * Flujo reactivo de WiFi habilitado.
     */
    val estaWifiHabilitado: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            }
        }
        administradorConectividad.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
        awaitClose { administradorConectividad.unregisterNetworkCallback(callback) }
    }.onStart {
        val actual = administradorConectividad.activeNetwork?.let {
            administradorConectividad.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } ?: false
        emit(actual)
    }.distinctUntilChanged()
}

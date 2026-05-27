package com.example.myapplication.core.utils

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
 * --- HARDWARE STATE PROVIDER (MAVERICK CORE ELITE) ---
 * Provee flujos reactivos en tiempo real sobre el estado del hardware (GPS, Internet).
 * Implementa la Ley de SSOT: Una sola fuente de verdad para los sensores del sistema.
 */
@Singleton
class HardwareStateProvider @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Flujo reactivo del estado de conexión a Internet.
     */
    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Estado inicial
        val current = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        trySend(current)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    /**
     * Flujo reactivo del estado del sensor GPS (Habilitado/Deshabilitado).
     */
    val isGpsEnabled: Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    trySend(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        
        // Estado inicial
        trySend(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))

        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    /**
     * Flujo reactivo de WiFi habilitado.
     */
    val isWifiEnabled: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            }
        }
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.onStart {
        val current = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } ?: false
        emit(current)
    }.distinctUntilChanged()
}

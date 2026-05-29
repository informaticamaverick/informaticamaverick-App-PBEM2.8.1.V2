package com.example.myapplication.presentation.features.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.core.domain.model.AddressInfo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.location.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.myapplication.core.data.repository.WeatherRepository
import com.example.myapplication.presentation.global.AppActionCoordinator
import javax.inject.Inject

/**
 * --- UBICACION CLIMA VIEWMODEL (EL OBRERO UNIFICADO v2.3) ---
 * Este ViewModel es ahora STATELESS. Delega la persistencia del estado de hardware
 * y la ubicación activa al Core y al Coordinator.
 * Su única responsabilidad es disparar acciones de UI (Geocodificación, GPS, Clima).
 */
@HiltViewModel
class UbicacionClimaViewModel @Inject constructor(
    private val coordinator: AppActionCoordinator,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    // ======================================================================================
    // --- 1. ESTADOS REACTIVOS (DERIVADOS DEL COORDINADOR/CORE) ---
    // ======================================================================================
    
    val isWifiEnabled = coordinator.isWifiEnabled
    val isGpsEnabled = coordinator.isGpsEnabled
    val isOnline = coordinator.isOnline
    val isCellularEnabled = coordinator.isCellularEnabled

    /** La dirección activa fluye desde el SSOT en el Coordinator */
    val activeAddress: StateFlow<AddressInfo?> = coordinator.activeAddress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isCargando = MutableStateFlow(false)
    val isCargando = _isCargando.asStateFlow()

    // ======================================================================================
    // --- 2. ACCIONES TÁCTICAS (GEOPROCESAMIENTO) ---
    // ======================================================================================

    /**
     * [NUEVO] TOGGLE INTELIGENTE DE GPS:
     * Si el GPS está activo, lo apaga y vuelve al domicilio por defecto.
     * Si está apagado, intenta disparar el cálculo de ubicación.
     */
    fun toggleGps(context: Context) {
        val currentAddress = activeAddress.value
        if (currentAddress?.id == "gps_current") {
            // Apagamos GPS: Volvemos al default y notificamos
            coordinator.resetAddressToDefault()
            Toast.makeText(context, "🏠 Volviendo a dirección predeterminada", Toast.LENGTH_SHORT).show()
        } else {
            // Intentamos activar GPS
            ejecutarCalculoUbicacionGps(context)
        }
    }

    @SuppressLint("MissingPermission")
    fun ejecutarCalculoUbicacionGps(
        context: Context,
        onResultado: (
            pais: String,
            provincia: String,
            localidad: String,
            calle: String,
            numero: String,
            cp: String,
            lat: Double,
            lng: Double
        ) -> Unit = { _, _, _, _, _, _, _, _ -> }
    ) {
        // 1. Validar hardware del sistema
        if (!MaverickGeoUtils.isGpsEnabled(context)) {
            Toast.makeText(context, "📍 El GPS está desactivado en el sistema", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Validar permisos de Android
        val fineLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocation != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "🚫 No hay permisos de ubicación concedidos", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _isCargando.value = true
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                var location: Location? = fusedClient.lastLocation.await()
                
                if (location == null) {
                    // Si lastLocation falla, intentamos una petición activa (Elite Rescue)
                    location = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                }

                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    
                    val address = MaverickGeoUtils.getAddressFromCoordinates(context, lat, lng)
                    address?.let { a ->
                        val freshGpsAddress = AddressInfo(
                            id = "gps_current",
                            companyOrUserName = "Mi Ubicación",
                            branchName = "GPS Tracker",
                            streetAndNumber = if (a.calle.isNotBlank()) "${a.calle} ${a.numero}".trim() else "Ubicación detectada",
                            locality = a.localidad,
                            province = a.provincia,
                            country = a.pais,
                            postalCode = a.codigoPostal,
                            isCompany = false,
                            lat = a.latitude,
                            lng = a.longitude
                        )
                        coordinator.updateAddressFromGps(freshGpsAddress)
                        Toast.makeText(context, "🛰️ Ubicación GPS activada: ${freshGpsAddress.locality}", Toast.LENGTH_SHORT).show()

                        onResultado(a.pais, a.provincia, a.localidad, a.calle, a.numero, a.codigoPostal, a.latitude, a.longitude)
                    }
                } else {
                    Toast.makeText(context, "❌ No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "⚠️ Error al procesar GPS: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                _isCargando.value = false
            }
        }
    }

    /**
     * Busca una dirección completa basándose en coordenadas específicas (Latitud/Longitud).
     */
    fun ejecutarBusquedaPorCoordenadas(
        context: Context,
        lat: Double,
        lng: Double,
        onResultado: (
            pais: String,
            provincia: String,
            localidad: String,
            calle: String,
            numero: String,
            cp: String,
            lat: Double,
            lng: Double
        ) -> Unit
    ) {
        viewModelScope.launch {
            _isCargando.value = true
            MaverickGeoUtils.getAddressFromCoordinates(context, lat, lng)?.let { a ->
                onResultado(a.pais, a.provincia, a.localidad, a.calle, a.numero, a.codigoPostal, a.latitude, a.longitude)
            }
            _isCargando.value = false
        }
    }

    /** Traduce un texto (calle y altura) a coordenadas y desglose completo */
    fun ejecutarBusquedaPorTexto(
        context: Context,
        direccionTexto: String,
        onResultado: (
            pais: String,
            provincia: String,
            localidad: String,
            calle: String,
            numero: String,
            cp: String,
            lat: Double,
            lng: Double
        ) -> Unit
    ) {
        viewModelScope.launch {
            _isCargando.value = true
            try {
                MaverickGeoUtils.getAddressFromText(context, direccionTexto)?.let { a ->
                    // Si el geocoder no detectó el número pero está en el texto original,
                    // podríamos intentar un parseo extra aquí si fuera necesario,
                    // pero MaverickGeoUtils ya fue mejorado.
                    onResultado(a.pais, a.provincia, a.localidad, a.calle, a.numero, a.codigoPostal, a.latitude, a.longitude)
                }
            } catch (e: Exception) {
                // Log o manejo de error silencioso
            } finally {
                _isCargando.value = false
            }
        }
    }

    // ======================================================================================
    // --- 3. GESTIÓN DEL CLIMA (SINCRONIZADA) ---
    // ======================================================================================

    val temperature = coordinator.temperature
    val weatherEmoji = coordinator.weatherEmoji
    val weatherDescription = coordinator.weatherDescription

    /**
     * Obtiene el clima actual basado en coordenadas.
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val data = weatherRepository.getCurrentWeather(lat, lon)
                data?.let {
                    // SINCRONIZACIÓN GLOBAL VIA COORDINATOR (SSOT)
                    coordinator.updateWeather(it.temperature, it.weatherEmoji, it.weatherDescription)
                }
            } catch (e: Exception) {
                coordinator.updateWeather("24°C", "☀️", "Despejado")
            }
        }
    }

    init {
        // Observación automática para el clima al cambiar de nodo activo
        viewModelScope.launch {
            coordinator.activeAddress.collect { address ->
                if (address != null) {
                    fetchWeather(address.lat, address.lng)
                }
            }
        }
    }
}


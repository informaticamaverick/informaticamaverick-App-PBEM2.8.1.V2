package com.example.myapplication.presentation.client

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.data.remote.WeatherApi
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.presentation.components.AddressInfo
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.*

// ==========================================================================================
// --- SECCIÓN: MODELOS DE DATOS PARA EL OBRERO (UBICACIÓN Y CLIMA) ---
// ==========================================================================================

/** 
 * --- MODELO DE UBICACIÓN GLOBAL ACTUALIZADO --- 
 * Se centraliza en el Obrero para manejar el dominio geográfico de la App.
 */
sealed class LocationOption {
    data class Gps(
        val address: String,        // Calle + Altura (formateado para mostrar)
        val street: String = "",    // Solo el nombre de la calle
        val number: String = "",    // Altura / Número
        val locality: String,       // Ciudad / Localidad
        val province: String = "",  // Provincia / Estado
        val country: String = "",   // País
        val postalCode: String = "", // Código Postal / Zip Code
        val lat: Double = 0.0,      // Latitud real
        val lng: Double = 0.0,      // Longitud real
        val id: String = "gps_current" // ID persistente para sincronización
    ) : LocationOption()
    
    data class Personal(
        val address: String, 
        val number: String, 
        val locality: String,
        val province: String = "",
        val country: String = "",
        val postalCode: String = "",
        val id: String = "" // ID de la dirección en Room/Firebase
    ) : LocationOption()
    
    data class Business(
        val companyName: String, 
        val branchName: String, 
        val address: String, 
        val number: String, 
        val locality: String,
        val province: String = "",
        val country: String = "",
        val postalCode: String = "",
        val id: String = "" // ID de la sucursal
    ) : LocationOption()
}

/** Modelo para resultados de búsqueda con metadatos de distancia Maverick */
data class ProviderWithDistance(
    val service: ServiceDisplayModel, 
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

/**
 * --- UBICACION CLIMA VIEWMODEL (EL OBRERO UNIFICADO) ---
 * Este ViewModel realiza el "trabajo sucio": Geocodificación, GPS, Clima y Cálculos de Distancia.
 * Libera al Cerebro (BeBrain) de procesos intensivos de datos.
 */
@HiltViewModel
class UbicacionClimaViewModel @Inject constructor() : ViewModel() {

    // ======================================================================================
    // --- 1. ESTADOS DE PROCESAMIENTO ---
    // ======================================================================================
    private val _isCargando = MutableStateFlow(false)
    val isCargando = _isCargando.asStateFlow()

    // --- Lista de direcciones procesadas y formateadas para la UI ---
    private val _userAddressesRaw = MutableStateFlow<List<AddressInfo>>(emptyList())
    val availableAddressInfos: StateFlow<List<AddressInfo>> = _userAddressesRaw.asStateFlow()

    /** 
     * TRABAJO SUCIO: Mapea el UserEntity a una lista de AddressInfo enriquecida.
     * Se encarga de iterar por todas las sucursales y direcciones personales.
     */
    fun updateAddressList(user: UserEntity?) {
        if (user == null) {
            _userAddressesRaw.value = emptyList()
            return
        }
        val list = mutableListOf<AddressInfo>()
        
        // 1. Mapeo de Direcciones Personales
        user.personalAddresses.forEach { addr ->
            list.add(AddressInfo(
                id = addr.id,
                companyOrUserName = user.displayName,
                branchName = addr.label.ifEmpty { "Mi Ubicación" },
                streetAndNumber = "${addr.calle} ${addr.numero}",
                locality = addr.localidad,
                province = addr.provincia,
                country = "Argentina", 
                postalCode = addr.codigoPostal,
                isCompany = false,
                lat = addr.latitude,
                lng = addr.longitude
            ))
        }
        
        // 2. Mapeo de Direcciones de Empresas y sus Sucursales
        user.companies.forEach { company ->
            company.branches.forEach { branch ->
                list.add(AddressInfo(
                    id = branch.id,
                    companyOrUserName = company.name,
                    branchName = branch.name,
                    streetAndNumber = "${branch.address.calle} ${branch.address.numero}",
                    locality = branch.address.localidad,
                    province = branch.address.provincia,
                    country = "Argentina",
                    postalCode = branch.address.codigoPostal,
                    isCompany = true,
                    lat = branch.address.latitude,
                    lng = branch.address.longitude
                ))
            }
        }
        _userAddressesRaw.value = list
    }

    // ======================================================================================
    // --- 2. TRABAJO SUCIO: COORDENADAS A DIRECCIÓN (REVERSE GEOCODING) ---
    // ======================================================================================

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
        viewModelScope.launch {
            _isCargando.value = true
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedClient.lastLocation.await()

                if (location != null) {
                    _latitude.value = location.latitude
                    _longitude.value = location.longitude
                    obtenerDireccionDesdeCoordenadas(context, location.latitude, location.longitude) { pais, prov, loc, calle, num, cp, lat, lng ->
                        _locationName.value = if (calle.isNotBlank()) "$calle $num".trim() else loc
                        
                        onResultado(pais, prov, loc, calle, num, cp, lat, lng)
                    }
                }
            } catch (e: Exception) {
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
            obtenerDireccionDesdeCoordenadas(context, lat, lng, onResultado)
            _isCargando.value = false
        }
    }

    /**
     * Lógica interna para desglosar una coordenada en componentes de dirección (Geocoder).
     */
    private suspend fun obtenerDireccionDesdeCoordenadas(
        context: Context,
        lat: Double,
        lng: Double,
        callback: (String, String, String, String, String, String, Double, Double) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)

            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                withContext(Dispatchers.Main) {
                    callback(
                        addr.countryName ?: "",
                        addr.adminArea ?: "",
                        addr.locality ?: addr.subAdminArea ?: "",
                        addr.thoroughfare ?: "",
                        addr.subThoroughfare ?: "",
                        addr.postalCode ?: "",
                        lat,
                        lng
                    )
                }
            }
        } catch (e: Exception) { }
    }

    // ======================================================================================
    // --- 3. TRABAJO SUCIO: DIRECCIÓN A COORDENADAS (FORWARD GEOCODING) ---
    // ======================================================================================

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
        viewModelScope.launch(Dispatchers.IO) {
            _isCargando.value = true
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(direccionTexto, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    withContext(Dispatchers.Main) {
                        onResultado(
                            addr.countryName ?: "",
                            addr.adminArea ?: "",
                            addr.locality ?: addr.subAdminArea ?: "",
                            addr.thoroughfare ?: "",
                            addr.subThoroughfare ?: "",
                            addr.postalCode ?: "",
                            addr.latitude,
                            addr.longitude
                        )
                    }
                }
            } catch (e: Exception) {
            } finally {
                _isCargando.value = false
            }
        }
    }

    /** Solo calcula Lat/Lng para una dirección de texto */
    fun ejecutarCalculoInversoDireccion(
        context: Context, 
        direccionTexto: String, 
        onCoordenadasListas: (lat: Double, lng: Double) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCargando.value = true
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(direccionTexto, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    onCoordenadasListas(addr.latitude, addr.longitude)
                }
            } catch (e: Exception) {
            } finally {
                _isCargando.value = false
            }
        }
    }

    // ======================================================================================
    // --- 4. TRABAJO SUCIO: CÁLCULOS MATEMÁTICOS (DISTANCIAS RADAR) ---
    // ======================================================================================

    /**
     * Calcula la distancia entre dos puntos geográficos usando la fórmula de Haversine.
     */
    fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // ======================================================================================
    // --- 5. GESTIÓN DEL CLIMA (UNIFICADO) ---
    // ======================================================================================
    
    private val _temperature = MutableStateFlow("--°C")
    val temperature: StateFlow<String> = _temperature.asStateFlow()

    private val _weatherEmoji = MutableStateFlow("🌤️")
    val weatherEmoji: StateFlow<String> = _weatherEmoji.asStateFlow()

    private val _weatherDescription = MutableStateFlow("Cargando...")
    val weatherDescription: StateFlow<String> = _weatherDescription.asStateFlow()

    private val _windSpeed = MutableStateFlow("-- km/h")
    val windSpeed: StateFlow<String> = _windSpeed.asStateFlow()

    private val _humidity = MutableStateFlow("-- %")
    val humidity: StateFlow<String> = _humidity.asStateFlow()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherApi::class.java)

    /**
     * Obtiene el clima actual basado en coordenadas.
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = weatherApi.getWeather(
                    lat = lat,
                    lon = lon,
                    hourly = "temperature_2m,relativehumidity_2m,windspeed_10m",
                    daily = "temperature_2m_max,temperature_2m_min,weathercode,precipitation_sum,windspeed_10m_max"
                )
                _temperature.value = "${response.current_weather.temperature.toInt()}°C"
                _weatherEmoji.value = getWeatherEmoji(response.current_weather.weathercode)
                _weatherDescription.value = getWeatherDescription(response.current_weather.weathercode)
                _windSpeed.value = "${response.current_weather.windspeed?.toInt() ?: 0} km/h"

                val currentHumidity = response.hourly?.relativehumidity_2m?.firstOrNull()
                _humidity.value = "${currentHumidity ?: 0}%"

            } catch (e: Exception) {
                // Valores por defecto en caso de error
                _temperature.value = "24°C"; _weatherEmoji.value = "☀️"; _weatherDescription.value = "Despejado"
            }
        }
    }

    private fun getWeatherEmoji(code: Int): String = when (code) {
        0 -> "☀️"; 1, 2, 3 -> "⛅"; 45, 48 -> "🌫️"; 51, 53, 55 -> "🌦️"; 61, 63, 65 -> "🌧️"; 71, 73, 75 -> "❄️"; 95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }

    private fun getWeatherDescription(code: Int): String = when (code) {
        0 -> "Despejado"; 1 -> "Mayormente despejado"; 2 -> "Parcialmente nublado"; 3 -> "Nublado"; 45, 48 -> "Niebla"
        51 -> "Llovizna ligera"; 53 -> "Llovizna moderada"; 55 -> "Llovizna densa"; 61 -> "Lluvia ligera"
        63 -> "Lluvia moderada"; 65 -> "Lluvia intensa"; 71 -> "Nevada ligera"; 73 -> "Nevada moderada"
        75 -> "Nevada intensa"; 95 -> "Tormenta"; 96, 99 -> "Tormenta con granizo"; else -> "Clima variable"
    }

    // ======================================================================================
    // --- 6. ESTADOS DE UBICACIÓN PERSISTENTES ---
    // ======================================================================================
    private val _locationName = MutableStateFlow("Actualizando...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    // ======================================================================================
    // --- 7. LÓGICA TÁCTICA FAST (BÚSQUEDA DE EMERGENCIA EN EL OBRERO) ---
    // ======================================================================================
    private val _isSearchingFast = MutableStateFlow(false)
    val isSearchingFast: StateFlow<Boolean> = _isSearchingFast.asStateFlow()

    private val _searchFinishedFast = MutableStateFlow(false)
    val searchFinishedFast: StateFlow<Boolean> = _searchFinishedFast.asStateFlow()

    private val _searchResultsFast = MutableStateFlow<List<ProviderWithDistance>>(emptyList())
    val searchResultsFast: StateFlow<List<ProviderWithDistance>> = _searchResultsFast.asStateFlow()

    /**
     * Motor táctico Maverick FAST: Filtra prestadores por cercanía y disponibilidad inmediata.
     */
    fun ejecutarBusquedaEmergenciaFast(
        category: CategoryEntity?, 
        allServices: List<ServiceDisplayModel>, 
        userLat: Double, 
        userLon: Double
    ) {
        if (category == null) return
        viewModelScope.launch {
            _isSearchingFast.value = true
            _searchFinishedFast.value = false
            _searchResultsFast.value = emptyList()
            delay(4000)

            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val isNightTime = currentHour >= 21 || currentHour < 6

            val filteredList = allServices
                .filter { it.categories.any { cat -> cat.equals(category.name, ignoreCase = true) } }
                .filter { service ->
                    if (isNightTime) service.works24h && service.isSubscribed
                    else (service.isOnline || service.works24h) && service.isSubscribed
                }
                .map { service ->
                    val pLat = service.latitude ?: (userLat + kotlin.random.Random.nextDouble(-0.03, 0.03))
                    val pLon = service.longitude ?: (userLon + kotlin.random.Random.nextDouble(-0.03, 0.03))
                    val distance = calcularDistanciaKm(userLat, userLon, pLat, pLon)
                    ProviderWithDistance(service, distance, (distance * 4.0).toInt().coerceAtLeast(2), pLat, pLon)
                }
                .sortedBy { it.distanceKm }
                .take(5)

            _searchResultsFast.value = filteredList
            _isSearchingFast.value = false
            _searchFinishedFast.value = true
        }
    }

    fun resetBusquedaFast() {
        _isSearchingFast.value = false
        _searchFinishedFast.value = false
        _searchResultsFast.value = emptyList()
    }
}

package com.example.myapplication.viewmodel.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.ui.componentes.be.modelos.TipoBeToast
import com.example.myapplication.core.datos.repositorios.UbicacionGpsRepositorio
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.utilidades.GeoUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- UBICACION GPS OBRERO (EL ORQUESTADOR TÁCTICO v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar disparos de GPS, permisos y feedback visual de Be.
 * [LEY #12]: Be como Portavoz. El obrero notifica estados al HUD global.
 */
@HiltViewModel
class UbicacionGpsObrero @Inject constructor(
    private val coordinador: CoordinadorAcciones,
    private val repositorioGps: UbicacionGpsRepositorio,
    private val accesoDirectoRepositorio: AccesoDirectoRepositorio
) : ViewModel() {

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando = _estaCargando.asStateFlow()

    private val _mostrarDialogoDireccion = MutableStateFlow(false)
    val mostrarDialogoDireccion: StateFlow<Boolean> = _mostrarDialogoDireccion.asStateFlow()
    fun establecerMostrarDialogoDireccion(visible: Boolean) { _mostrarDialogoDireccion.value = visible }

    val estaGpsActivado = coordinador.modoGpsActivo // 🔥 [ELITE]: Ahora bindeado al modo de la App
    
    // 🔥 [ELITE]: Observamos la dirección activa con Auto-Warmup y Soberanía de Hardware
    val direccionActiva: StateFlow<DireccionDominio?> = coordinador.direccionActiva
        .onEach { dir ->
            // SOBERANÍA: Si el modo App está prendido pero no hay coordenadas válidas, forzamos el rastreo.
            if (coordinador.modoGpsActivo.value && (dir == null || dir.latitud == 0.0 || dir.id != "gps_current")) {
                Log.d("UbiGpsObrero", "🛰️ [HARDWARE_SYNC] Modo GPS activo detectado. Forzando actualización...")
                viewModelScope.launch {
                    coordinador.dispararAccion("refresh_gps")
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    init {
        // Asegurar auto-warmup al iniciar el ViewModel si el modo ya está activo
        viewModelScope.launch {
            coordinador.modoGpsActivo.collect { enabled ->
                if (enabled) {
                    val actual = coordinador.direccionActiva.first()
                    if (actual == null || actual.latitud == 0.0) {
                        Log.d("UbiGpsObrero", "🛰️ [INIT_WARMUP] Modo GPS ya estaba activo. Disparando...")
                        coordinador.dispararAccion("refresh_gps")
                    }
                }
            }
        }
    }

    /**
     * Selecciona una dirección y orquesta la sincronización de red.
     */
    fun seleccionarDireccion(idDireccion: String) {
        coordinador.seleccionarDireccion(idDireccion)
        viewModelScope.launch {
            val direcciones = coordinador.informacionDireccionesDisponibles.first()
            val seleccionada = direcciones.find { it.id == idDireccion }
            
            seleccionada?.let {
                val etiqueta = if (it.id == "gps_current") "Ubicación Satelital" else it.etiqueta.ifBlank { it.calle }
                coordinador.mostrarToast("Zona activa: $etiqueta", TipoBeToast.EXITO)
            }

            // Sincronización proactiva de tópicos para la zona
            seleccionada?.codigoPostal?.let { cp ->
                val misFavoritos = accesoDirectoRepositorio.obtenerShortcutsPorContexto("home").first()
                    .filter { it.tipo == "category" }
                    .map { it.idDestino }
                coordinador.sincronizarEcosistemaRed(cp, misFavoritos)
            }
        }
    }

    /**
     * Alterna el modo GPS.
     */
    fun toggleGps(context: Context) {
        viewModelScope.launch {
            val modoActual = coordinador.modoGpsActivo.value
            if (modoActual) {
                coordinador.mostrarToast("Regresando a zona predeterminada", TipoBeToast.INFO)
                coordinador.alternarModoGps(false)
            } else {
                coordinador.alternarModoGps(true)
                coordinador.mostrarToast("Conectando con satélites", TipoBeToast.PROCESANDO)
                ejecutarCalculoUbicacionGps(context, mostrarAvisos = true)
            }
        }
    }

    /**
     * 🔥 [ELITE]: Captura y retorna la ubicación actual para formularios reactivos.
     */
    suspend fun capturarUbicacionGps(): DireccionDominio? {
        _estaCargando.value = true
        return try {
            repositorioGps.actualizarUbicacionSoberana()
        } finally {
            _estaCargando.value = false
        }
    }

    /**
     * Dispara la detección física de ubicación.
     */
    @SuppressLint("MissingPermission")
    fun ejecutarCalculoUbicacionGps(context: Context, mostrarAvisos: Boolean = false) {
        // 1. Validar Permisos
        val fineLocation = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocation != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (mostrarAvisos) coordinador.mostrarToast("🚫 Sin permisos de ubicación", TipoBeToast.ERROR)
            return
        }

        // 2. Validar Hardware
        if (!GeoUtils.estaGpsHabilitado(context)) {
            if (mostrarAvisos) coordinador.mostrarToast("📍 Activa el GPS del equipo", TipoBeToast.INFO)
            return
        }

        viewModelScope.launch {
            _estaCargando.value = true
            if (mostrarAvisos) coordinador.mostrarToast("Actualizando tu ubicación...", TipoBeToast.PROCESANDO)
            
            try {
                val dir = repositorioGps.actualizarUbicacionSoberana()
                dir?.let {
                    coordinador.actualizarDireccionDesdeGps(it)
                    if (mostrarAvisos) coordinador.mostrarToast("Ubicación Encontrada", TipoBeToast.EXITO)
                }
            } catch (e: Exception) {
                Log.e("UbiGpsObrero", "❌ Error GPS: ${e.message}")
                coordinador.mostrarToast("Error de rastreo", TipoBeToast.ERROR)
            } finally {
                _estaCargando.value = false
            }
        }
    }
}



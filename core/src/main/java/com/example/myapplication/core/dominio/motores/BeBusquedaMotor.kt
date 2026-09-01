package com.example.myapplication.core.dominio.motores

import com.example.myapplication.core.utilidades.normalizeFull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- MOTOR DE BÚSQUEDA BE (v2026.ELITE) ---
 * [PROPÓSITO]: Centralizar la inteligencia de búsqueda, normalización y debouncing.
 * [LEY #14]: El Embudo. Filtra en la fuente a través de contratos normalizados.
 * [UBICACIÓN]: Módulo :core para disponibilidad universal (Cliente/Prestador).
 */
@OptIn(FlowPreview::class)
@Singleton
class BeBusquedaMotor @Inject constructor() {
    
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _estaBusquedaActiva = MutableStateFlow(false)
    val estaBusquedaActiva = _estaBusquedaActiva.asStateFlow()

    private val _consultaCruda = MutableStateFlow("")
    val consultaCruda = _consultaCruda.asStateFlow()

    /**
     * Consulta normalizada en tiempo real (sin debouncing).
     */
    val consultaNormalizada: StateFlow<String> = _consultaCruda
        .map { it.normalizeFull() }
        .stateIn(alcance, SharingStarted.Eagerly, "")

    /**
     * 🔥 [ELITE]: Consulta optimizada para la base de datos (con debouncing).
     * Es la fuente principal para que los ViewModels disparen sus consultas SQL.
     */
    val consultaNormalizadaDebounced: StateFlow<String> = consultaNormalizada
        .debounce(300.milliseconds)
        .distinctUntilChanged()
        .stateIn(alcance, SharingStarted.Eagerly, "")

    fun establecerEstaBusquedaActiva(activa: Boolean) {
        _estaBusquedaActiva.value = activa
        if (!activa) limpiarConsulta()
    }

    fun actualizarConsulta(nueva: String) {
        _consultaCruda.value = nueva
    }

    fun limpiarConsulta() {
        _consultaCruda.value = ""
    }
}

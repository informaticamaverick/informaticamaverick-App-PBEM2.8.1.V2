package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.dominio.modelos.PresupuestoConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * --- REPOSITORIO DE CONFIGURACIÓN (Atómico) ---
 */
@Singleton
class ConfiguracionRepositorio @Inject constructor() {
    val modoTema: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    val notifMensajes: Flow<Boolean> = flowOf(true)
    val notifPresupuestos: Flow<Boolean> = flowOf(true)
    val notifPedidos: Flow<Boolean> = flowOf(true)

    private val _presupuestoConfig = MutableStateFlow(PresupuestoConfig())
    val presupuestoConfig: Flow<PresupuestoConfig> = _presupuestoConfig.asStateFlow()

    suspend fun establecerModoTema(modo: ThemeMode) { }
    suspend fun establecerNotifMensajes(habilitado: Boolean) { }
    suspend fun establecerNotifPresupuestos(habilitado: Boolean) { }
    suspend fun establecerNotifPedidos(habilitado: Boolean) { }

    suspend fun guardarConfigPresupuesto(config: PresupuestoConfig) {
        _presupuestoConfig.value = config
    }
}



































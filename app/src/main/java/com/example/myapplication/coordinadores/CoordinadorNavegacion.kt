package com.example.myapplication.coordinadores

import android.util.Log
import com.example.myapplication.ui.componentes.be.modelos.ConfiguracionContextoBe
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- COORDINADOR DE NAVEGACIÓN Y SOBERANÍA (v2026.ELITE) ---
 * [PROPÓSITO]: Único dueño de la visibilidad de la interfaz (Be, Barra Nav, Escáner).
 * [FUNCIONAMIENTO]: Mantiene un mapa de registros soberanos. La última pantalla/hoja 
 * registrada dicta el estado visual de la aplicación.
 * [LEY #12]: Soberanía por Contrato.
 */
@Singleton
class CoordinadorNavegacion @Inject constructor() {

    private val alcance = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate)

    // Mapa de registros: ID -> Configuración
    // Usamos LinkedHashMap para garantizar que el último insertado sea el tope visual.
    private val _registroSoberano = MutableStateFlow<Map<String, ConfiguracionContextoBe>>(emptyMap())

    /**
     * Contrato Activo: Siempre emite la última configuración registrada en el mapa.
     * Si el mapa está vacío, devuelve la configuración base del Inicio.
     */
    val contratoActivo: StateFlow<ConfiguracionContextoBe> = _registroSoberano
        .map { map -> 
            map.values.lastOrNull() ?: ContextoHUD.INICIO.crearConfiguracionBase()
        }
        .stateIn(alcance, SharingStarted.Eagerly, ContextoHUD.INICIO.crearConfiguracionBase())

    // --- ESTADOS GLOBALES DE INTERFAZ ---
    private val _estaHojaVisible = MutableStateFlow(false)
    val estaHojaVisible = _estaHojaVisible.asStateFlow()

    private val _estaMenuLateralAbierto = MutableStateFlow(false)
    val estaMenuLateralAbierto = _estaMenuLateralAbierto.asStateFlow()

    fun actualizarVisibilidadHoja(visible: Boolean) { _estaHojaVisible.value = visible }
    fun establecerEstaMenuLateralAbierto(abierto: Boolean) { _estaMenuLateralAbierto.value = abierto }

    /**
     * 🔥 [ELITE]: Reinicia el sistema de soberanía con un contexto raíz.
     * Limpia todos los registros previos.
     */
    fun reiniciarContextoHUD(contexto: ContextoHUD) {
        android.util.Log.d("MAV_HUD", "🌐 [HUD_RESET] Reiniciando a contexto raíz: $contexto")
        _registroSoberano.value = linkedMapOf(
            "root_${contexto.name}" to contexto.crearConfiguracionBase()
        )
    }

    /**
     * 🔥 [ELITE]: Registra una pantalla u hoja en el sistema de soberanía.
     */
    fun registrarPantalla(config: ConfiguracionContextoBe) {
        android.util.Log.d("MAV_HUD", "📥 [REGISTRAR] id=${config.id} | Be=${config.mostrarBe} | Nav=${config.mostrarBarraNavegacion}")
        _registroSoberano.update { map ->
            val nuevoMapa = LinkedHashMap(map)
            nuevoMapa.remove(config.id)
            nuevoMapa[config.id] = config
            nuevoMapa
        }
    }

    /**
     * 🔥 [ELITE]: Remueve una pantalla u hoja del sistema.
     * Al removerse, la configuración anterior en el mapa toma el control automáticamente.
     */
    fun removerPantalla(id: String) {
        android.util.Log.d("MAV_HUD", "📤 [REMOVER] id=$id")
        _registroSoberano.update { map ->
            val nuevoMapa = LinkedHashMap(map)
            nuevoMapa.remove(id)
            nuevoMapa
        }
    }

    /**
     * Actualiza la configuración de un contrato existente o del tope actual.
     */
    fun actualizarContratoActual(config: ConfiguracionContextoBe) {
        _registroSoberano.update { map ->
            val nuevoMapa = LinkedHashMap(map)
            val idActualizar = if (config.id == "default") nuevoMapa.keys.lastOrNull() else config.id
            if (idActualizar != null && nuevoMapa.containsKey(idActualizar)) {
                nuevoMapa[idActualizar] = config.copy(id = idActualizar)
            } else {
                nuevoMapa[config.id] = config
            }
            nuevoMapa
        }
    }

    /**
     * Limpia todos los registros excepto el base (si existe).
     * Útil para el Cierre Maestro.
     */
    fun limpiarCierreMaestro() {
        _registroSoberano.update { map ->
            if (map.size > 1) {
                val primerEntrada = map.entries.first()
                linkedMapOf(primerEntrada.key to primerEntrada.value)
            } else map
        }
    }
}

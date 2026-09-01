package com.example.myapplication.coordinadores

import android.util.Log
import com.example.myapplication.core.datos.repositorios.AccesoDirectoRepositorio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE INICIO (EL INICIADOR ELITE) ---
 * [LEY #9]: Estándar en Español.
 * Gestiona la inicialización fría de la aplicación: accesos directos por defecto.
 * El sembrado de categorías ocurre ahora automáticamente en Room.
 */
@Singleton
class GestorInicio @Inject constructor(
    private val repositorioShortcuts: AccesoDirectoRepositorio
) {
    private val alcance = CoroutineScope(Dispatchers.IO)

    /**
     * Ejecuta las tareas de inicio de forma asíncrona.
     * [LEY #5: Background Warm-up]
     */
    fun realizarInicioIncial() {
        alcance.launch {
            Log.d("GestorInicio", "🚀 [STARTUP_WARMUP] Verificando configuración de accesos directos...")
            
            // Configuramos los shortcuts por defecto para una experiencia inmediata
            repositorioShortcuts.agregarShortcut(
                contexto = "home",
                idDestino = "Hogar y Mantenimiento",
                tipo = "supercategory",
                etiqueta = "Hogar y Mantenimiento",
                icono = "🏠"
            )

            // --- SECTOR URGENCIAS (RADAR FAST v2026) ---
            repositorioShortcuts.agregarShortcut("urgencia", "HOGAR_CERRAJERO", "category", "Cerrajero", "🔑")
            repositorioShortcuts.agregarShortcut("urgencia", "LOGISTICA_FLETES", "category", "Fletes", "🛻")
            repositorioShortcuts.agregarShortcut("urgencia", "AUTO_AUXILIO", "category", "Auxilio", "🆘")
        }
    }
}



































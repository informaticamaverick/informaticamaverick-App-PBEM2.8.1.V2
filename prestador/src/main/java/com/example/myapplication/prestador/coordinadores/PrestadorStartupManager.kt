package com.example.myapplication.prestador.coordinadores

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- PRESTADOR STARTUP MANAGER (EL INICIADOR ELITE) ---
 * Gestiona la inicialización fría de la App Prestador.
 * El sembrado de categorías ocurre ahora automáticamente en Room (Big League).
 */
@Singleton
class PrestadorStartupManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun performInitialStartup() {
        scope.launch {
            Log.d("PrestadorStartup", "🚀 [STARTUP_WARMUP] Sistema de datos gestionado por Room.")
        }
    }
}














































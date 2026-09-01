package com.example.myapplication.core.servicios.publicidad

import android.content.Context
import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * --- BE ADS MANAGER (MAVERICK v1.0) ---
 * Centraliza la inicialización y configuración de Google Ads (AdMob).
 * Implementa el "Kill Switch" de seguridad para desactivar anuncios instantáneamente.
 */
object BeAdsManager {
    private const val TAG = "BeAdsManager"
    
    // --- KILL SWITCH DE SEGURIDAD ---
    // Cambiar a false para desactivar TODO el sistema de anuncios de Google
    var isAdSystemEnabled = true
        private set

    // --- IDS DE PRUEBA OFICIALES ---
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    const val TEST_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"

    private var isInitialized = false

    /**
     * Inicializa el SDK de Mobile Ads de forma asíncrona.
     * [LEY #5: Background Warm-up] Diferido para evitar Jank en el arranque.
     */
    fun initialize(context: Context) {
        if (!isAdSystemEnabled) {
            Log.d(TAG, "🚫 [ADS_DISABLED] El sistema de anuncios está desactivado por configuración.")
            return
        }

        if (isInitialized) return

        CoroutineScope(Dispatchers.IO).launch {
            // [LEY #5: Background Warm-up] Diferido optimizado (500ms)
            kotlinx.coroutines.delay(500)

            // [LEY #7: Trazabilidad Hormiga] Auditoría táctica del entorno
            Log.d(TAG, "🚀 [ADS_INIT_START] Verificando estabilidad del entorno GMS...")
            
            try {
                // Configuración de Dispositivos de Prueba
                val configuration = RequestConfiguration.Builder().build()
                MobileAds.setRequestConfiguration(configuration)

                MobileAds.initialize(context) { status ->
                    isInitialized = true
                    val mediationStatus = status.adapterStatusMap.values.joinToString { "${it.description}: ${it.initializationState}" }
                    Log.d(TAG, "✅ [ADS_INIT_SUCCESS] SDK Inicializado. Estados: $mediationStatus")
                }
            } catch (e: Throwable) {
                Log.e(TAG, "❌ [ADS_INIT_FATAL] Error crítico de sistema en GMS: ${e.message}")
            }
        }
    }

    /**
     * Permite activar o desactivar el sistema de anuncios dinámicamente (Elite Control).
     */
    fun setAdSystemEnabled(enabled: Boolean) {
        isAdSystemEnabled = enabled
        Log.w(TAG, "⚠️ [ADS_CONTROL] Sistema de anuncios ${if (enabled) "ACTIVADO" else "DESACTIVADO"}")
    }

    /**
     * 🔥 [ELITE]: Genera una configuración de carga de anuncios nativos estándar.
     * [v2026.FINAL]: Añadido soporte para Video Ads con Auto-play silenciado.
     */
    fun buildNativeAdOptions(): com.google.android.gms.ads.nativead.NativeAdOptions {
        val videoOptions = com.google.android.gms.ads.VideoOptions.Builder()
            .setStartMuted(true) 
            .build()

        return com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
            .setAdChoicesPlacement(com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setRequestMultipleImages(false)
            .setVideoOptions(videoOptions)
            .build()
    }

    /**
     * Extrae el Activity desde un contexto, necesario para el AdLoader de Google.
     */
    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}


































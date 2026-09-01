package com.example.myapplication.core.utilidades

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE AUDIO MAVERICK (V2026.7) ---
 * [ELITE SSOT]: Único gestor de grabación y reproducción para todo el ecosistema.
 * [LEY #2]: Optimización de recursos (Costo Zero).
 * [LEY #8]: Gestión de archivos efímeros.
 */
@Singleton
class AudioManager @Inject constructor() {

    private val TAG = "AudioManager"
    private var grabador: MediaRecorder? = null
    private var reproductor: MediaPlayer? = null
    private var rutaArchivoActual: String? = null
    
    private val alcanceGrabacion = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var tareaCronometro: Job? = null

    private val _estaGrabando = MutableStateFlow(false)
    val estaGrabando: StateFlow<Boolean> = _estaGrabando.asStateFlow()

    private val _tiempoTranscurrido = MutableStateFlow(0)
    val tiempoTranscurrido: StateFlow<Int> = _tiempoTranscurrido.asStateFlow()

    private val _estaReproduciendo = MutableStateFlow(false)
    val estaReproduciendo: StateFlow<Boolean> = _estaReproduciendo.asStateFlow()

    private val _idAudioActual = MutableStateFlow<String?>(null)
    val idAudioActual: StateFlow<String?> = _idAudioActual.asStateFlow()

    private val _progresoReproduccion = MutableStateFlow(0f)
    val progresoReproduccion: StateFlow<Float> = _progresoReproduccion.asStateFlow()

    private var tareaProgreso: Job? = null

    /**
     * Inicia la grabación de un audio en el directorio de caché.
     */
    fun iniciarGrabacion(contexto: Context) {
        if (_estaGrabando.value) return

        try {
            val archivo = File(contexto.cacheDir, "grabacion_mav_${System.currentTimeMillis()}.m4a")
            rutaArchivoActual = archivo.absolutePath
            
            grabador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(contexto)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            grabador?.apply {
                AudioUtils.configureEliteRecorder(this)
                setOutputFile(rutaArchivoActual)
                prepare()
                start()
            }

            _estaGrabando.value = true
            iniciarCronometro()
            Log.d(TAG, "🎙️ [AUDIO_START] Grabación iniciada en: $rutaArchivoActual")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [AUDIO_ERROR] Fallo crítico al iniciar grabación: ${e.message}")
            e.printStackTrace()
            liberarGrabador()
        }
    }

    /**
     * Detiene la grabación y devuelve el archivo resultante.
     */
    fun detenerGrabacion(): File? {
        if (!_estaGrabando.value) return null

        try {
            grabador?.apply {
                stop()
                release()
            }
            grabador = null
            _estaGrabando.value = false
            detenerCronometro()

            val archivo = rutaArchivoActual?.let { File(it) }
            Log.d(TAG, "✅ [AUDIO_STOP] Grabación finalizada. Tamaño: ${archivo?.length() ?: 0} bytes")
            return archivo
        } catch (e: Exception) {
            Log.e(TAG, "❌ [AUDIO_ERROR] Error al detener grabador: ${e.message}")
            liberarGrabador()
            return null
        }
    }

    /**
     * Cancela la grabación actual y elimina el archivo temporal.
     */
    fun cancelarGrabacion() {
        liberarGrabador()
        rutaArchivoActual?.let { 
            val f = File(it)
            if (f.exists()) f.delete()
        }
        rutaArchivoActual = null
        Log.d(TAG, "🗑️ [AUDIO_CANCEL] Grabación descartada.")
    }

    /**
     * Reproduce un audio desde una URL o ruta local.
     */
    fun reproducirAudio(id: String, url: String? = null, rutaLocal: String? = null) {
        try {
            if (_estaReproduciendo.value && _idAudioActual.value == id) {
                detenerReproduccion()
                return
            }

            detenerReproduccion()

            val fuente = rutaLocal ?: url ?: return

            reproductor = MediaPlayer().apply {
                setDataSource(fuente)
                prepareAsync()
                setOnPreparedListener { 
                    start()
                    _estaReproduciendo.value = true
                    _idAudioActual.value = id
                    iniciarSeguimientoProgreso()
                }
                setOnCompletionListener {
                    _estaReproduciendo.value = false
                    _idAudioActual.value = null
                    detenerSeguimientoProgreso()
                    liberarReproductor()
                }
                setOnErrorListener { _, _, _ ->
                    detenerSeguimientoProgreso()
                    liberarReproductor()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [PLAY_ERROR] No se pudo reproducir: ${e.message}")
        }
    }

    fun detenerReproduccion() {
        try {
            reproductor?.let { player ->
                if (player.isPlaying) player.stop()
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ [AUDIO_RELEASE_ERR] Error al soltar reproductor: ${e.message}")
        }
        reproductor = null
        _estaReproduciendo.value = false
        _idAudioActual.value = null
        detenerSeguimientoProgreso()
    }

    private fun iniciarSeguimientoProgreso() {
        tareaProgreso?.cancel()
        tareaProgreso = alcanceGrabacion.launch {
            while (isActive && _estaReproduciendo.value) {
                reproductor?.let { player ->
                    try {
                        if (player.isPlaying && player.duration > 0) {
                            _progresoReproduccion.value = (player.currentPosition.toFloat() / player.duration.toFloat()).coerceIn(0f, 1f)
                        }
                    } catch (_: Exception) {}
                }
                delay(100)
            }
        }
    }

    private fun detenerSeguimientoProgreso() {
        tareaProgreso?.cancel()
        tareaProgreso = null
        _progresoReproduccion.value = 0f
    }

    private fun iniciarCronometro() {
        tareaCronometro?.cancel()
        _tiempoTranscurrido.value = 0
        tareaCronometro = alcanceGrabacion.launch {
            while (isActive && _estaGrabando.value) {
                delay(1000)
                _tiempoTranscurrido.value += 1
                // Límite de seguridad: 5 minutos
                if (_tiempoTranscurrido.value >= 300) {
                    detenerGrabacion()
                    break
                }
            }
        }
    }

    private fun detenerCronometro() {
        tareaCronometro?.cancel()
        tareaCronometro = null
    }

    private fun liberarGrabador() {
        try { grabador?.release() } catch (e: Exception) {}
        grabador = null
        _estaGrabando.value = false
        detenerCronometro()
    }

    private fun liberarReproductor() {
        try { reproductor?.release() } catch (e: Exception) {}
        reproductor = null
        _estaReproduciendo.value = false
        _idAudioActual.value = null
        detenerSeguimientoProgreso()
    }
}


































package com.example.myapplication.core.utilidades

import android.media.MediaRecorder

/**
 * --- AUDIO UTILS (COMPARTIDO) ---
 * Centraliza la configuración maestra de grabación para el ecoSISTEMA.
 * Asegura calidad "Elite" con el mínimo peso posible (estándar AAC).
 */
object AudioUtils {

    /**
     * Configura un MediaRecorder con los parámetros optimizados para Elite.
     * Formato: MPEG_4 | Codificador: AAC | Bitrate: 32kbps | Frecuencia: 16kHz
     */
    fun configureEliteRecorder(recorder: MediaRecorder) {
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioChannels(1)
            setAudioSamplingRate(16000)
            setAudioEncodingBitRate(32000)
        }
    }

    /**
     * Helper para obtener la extensión de archivo recomendada.
     */
    fun getRecommendedExtension(): String = ".m4a"
}



































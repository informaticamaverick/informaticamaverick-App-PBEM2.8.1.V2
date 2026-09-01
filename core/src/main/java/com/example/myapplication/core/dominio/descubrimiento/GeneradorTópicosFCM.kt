package com.example.myapplication.core.dominio.descubrimiento

import android.util.Log
import com.example.myapplication.core.dominio.ubicacion.NormalizadorDirecciones
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GENERADOR DE TÓPICOS FCM (v2026.ELITE) ---
 * [RESPONSABILIDAD]: Construir las llaves de suscripción para Firebase Cloud Messaging.
 * [LEY #17]: Protocolo de Bautizo.
 */
@Singleton
class GeneradorTópicosFCM @Inject constructor() {

    /**
     * Genera el nombre de un Tópico Maestro para red o búsqueda.
     * Formato: PREFIJO_CP_CATEGORIA
     */
    fun generarTópicoMaestro(prefijo: String, codigoPostal: String, categoria: String? = null): String {
        val cpLimpio = NormalizadorDirecciones.limpiarCodigoPostal(codigoPostal)
        if (cpLimpio.isEmpty()) return ""
        
        val base = "${prefijo}_$cpLimpio"
        val topico = if (categoria.isNullOrBlank()) base 
                     else "${base}_${NormalizadorTópicos.normalizar(categoria)}"
        
        Log.d("FCM_GEN", "🛰️ [TÓPICO_CONSTRUIDO] $topico | CP: $cpLimpio")
        return topico
    }
}

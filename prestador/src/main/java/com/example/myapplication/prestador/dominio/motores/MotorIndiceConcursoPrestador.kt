package com.example.myapplication.prestador.dominio.motores

import com.example.myapplication.core.dominio.motores.MotorTopicos
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE ÍNDICE DE CONCURSOS (Prestador - v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar las suscripciones del prestador a licitaciones públicas.
 * [LEY #9]: Estándar Maverick. Escucha de oportunidades de trabajo.
 */
@Singleton
class MotorIndiceConcursoPrestador @Inject constructor(
    private val motorTopicos: MotorTopicos,
    private val generadorTopicos: GeneradorTópicosFCM
) {

    /**
     * 🔥 [ELITE]: Suscribe al prestador a los concursos de su zona y rubros.
     * Genera tópicos con prefijo 'C' (Concurso).
     */
    suspend fun sincronizarOportunidadesDeTrabajo(cp: String, categorias: List<String>) {
        try {
            Log.d("MOTOR_CONCURSO_PREST", "📡 [SYNC] Sincronizando hilos de trabajo para CP: $cp")
            
            val topicsConcursos = categorias.map { rubro ->
                generadorTopicos.generarTópicoMaestro(
                    ProtocoloPrefijos.CONCURSO, 
                    cp, 
                    rubro
                )
            }.toSet()
            
            motorTopicos.sincronizarSuscripciones(topicsConcursos, origen = "CONCURSO_PRESTADOR")
            Log.d("MOTOR_CONCURSO_PREST", "✅ [SYNC_OK] Suscrito a ${topicsConcursos.size} canales de trabajo.")
            
        } catch (e: Exception) {
            Log.e("MOTOR_CONCURSO_PREST", "❌ [SYNC_ERR] Error al sincronizar: ${e.message}")
        }
    }
}


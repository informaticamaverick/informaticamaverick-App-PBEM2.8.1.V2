package com.example.myapplication.dominio.motores

import com.example.myapplication.core.dominio.motores.MotorTopicos
import com.example.myapplication.core.dominio.descubrimiento.GeneradorTópicosFCM
import com.example.myapplication.core.dominio.descubrimiento.ProtocoloPrefijos
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- MOTOR DE ÍNDICE DE PROMOCIONES (Azul - v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar las suscripciones del usuario a hilos de ofertas (Promociones).
 * [LEY #9]: Estándar Maverick. Suscriptor proactivo del cliente.
 */
@Singleton
class MotorIndicePromocionesUsuario @Inject constructor(
    private val motorTopicos: MotorTopicos,
    private val generadorTopicos: GeneradorTópicosFCM
) {

    /**
     * 🔥 [ELITE]: Suscribe al usuario a las promociones de su zona y rubros de interés.
     * Genera tópicos con prefijo 'H' (Historia/Oferta).
     */
    suspend fun sincronizarOfertasDeInteres(cp: String, categorias: List<String>) {
        try {
            Log.d("MOTOR_PROMO_USER", "📡 [SYNC] Sincronizando hilos de ofertas para CP: $cp")
            
            val topicsNuevos = categorias.map { rubro ->
                generadorTopicos.generarTópicoMaestro(
                    ProtocoloPrefijos.OFERTA, 
                    cp, 
                    rubro
                )
            }.toMutableSet()
            
            // 🔥 [ELITE FIX]: Incluir también el hilo global de ofertas de la zona
            topicsNuevos.add(generadorTopicos.generarTópicoMaestro(ProtocoloPrefijos.OFERTA, cp))
            
            motorTopicos.sincronizarSuscripciones(topicsNuevos, origen = "PROMO_USUARIO")
            Log.d("MOTOR_PROMO_USER", "✅ [SYNC_OK] Suscrito a ${topicsNuevos.size} canales de ofertas.")
            
        } catch (e: Exception) {
            Log.e("MOTOR_PROMO_USER", "❌ [SYNC_ERR] Error al sincronizar tópicos: ${e.message}")
        }
    }
}


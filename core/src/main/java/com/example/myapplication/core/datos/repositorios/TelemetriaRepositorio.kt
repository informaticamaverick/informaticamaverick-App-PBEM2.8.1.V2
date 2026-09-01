package com.example.myapplication.core.datos.repositorios

import com.example.myapplication.core.datos.local.dao.TelemetryDao
import com.example.myapplication.core.datos.local.entidades.TelemetryType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE TELEMETRÍA MAVERICK (Atómico) ---
 * [ELITE v2026]: Servicio técnico compartido para sincronización de interacciones.
 */
@Singleton
class TelemetriaRepositorio @Inject constructor(
    private val telemetryDao: TelemetryDao,
    private val firestore: FirebaseFirestore
) {
    /**
     * Sincroniza eventos acumulados en Room hacia Firestore (Metodología Costo Zero).
     * [v2026.ELITE]: Implementa pre-agregación local para ahorrar escrituras en Spark Plan.
     */
    suspend fun sincronizarTelemetriaPendiente() {
        val pendientes = telemetryDao.obtenerPendientes()
        if (pendientes.isEmpty()) return

        try {
            // 1. Agrupamos por targetId y tipo para enviar un solo incremento total
            val lotes = pendientes.groupBy { it.targetId to it.type }
            
            lotes.forEach { (clave, eventos) ->
                val (targetId, tipo) = clave
                val valorTotal = eventos.sumOf { it.value }
                
                // [LEY #2]: Determinamos la colección técnica
                val coleccion = when (tipo) {
                    TelemetryType.LIKE, TelemetryType.VIEW -> "metricas"
                    TelemetryType.CLICK -> "telemetria_clicks"
                }

                val docRef = firestore.collection(coleccion).document(targetId)
                
                // Actualización atómica en la nube
                val campo = when (tipo) {
                    TelemetryType.LIKE -> "likes"
                    TelemetryType.VIEW -> "vistas"
                    TelemetryType.CLICK -> "clicks"
                }
                
                docRef.set(
                    mapOf(campo to com.google.firebase.firestore.FieldValue.increment(valorTotal.toLong())),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
            }
            
            // 2. Marcamos como sincronizados localmente
            val ids = pendientes.map { it.id }
            telemetryDao.marcarComoSincronizados(ids)
            
            android.util.Log.d("TelemetryRepo", "✅ [TELEMETRY_SYNC_OK] ${pendientes.size} eventos agrupados y subidos.")
        } catch (e: Exception) {
            android.util.Log.e("TelemetryRepo", "❌ [TELEMETRY_SYNC_ERR] ${e.message}")
        }
    }
}




































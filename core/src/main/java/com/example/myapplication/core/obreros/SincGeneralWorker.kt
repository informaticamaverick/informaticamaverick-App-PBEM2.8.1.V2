package com.example.myapplication.core.obreros

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.core.datos.repositorios.PromocionRepositorio
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * --- OBRERO DE SINCRONIZACIÓN GENERAL (v2026.ELITE) ---
 * [PROPÓSITO]: Ejecutar tareas tácticas comunes (Telemetría, Presencia) 
 * que no pertenecen a la lógica de negocio privada de las apps.
 */
@HiltWorker
class SincGeneralWorker @AssistedInject constructor(
    @Assisted contexto: Context,
    @Assisted parametros: WorkerParameters,
    private val repositorioPromocion: PromocionRepositorio,
    private val repositorioChat: ChatMotorSincRepositorio
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val tipo = inputData.getString(CLAVE_TIPO_SYNC) ?: return Result.failure()

        Log.d("SincGeneralWorker", "🚀 [INICIO_SINC_GRAL] Tarea: $tipo")

        return try {
            when (tipo) {
                TIPO_TELEMETRIA -> {
                    repositorioPromocion.sincronizarTelemetriaPendiente()
                }
                TIPO_OBSERVAR_CHAT -> {
                    // Lógica de señalización general
                }
            }
            
            Log.d("SincGeneralWorker", "✅ [EXITO_SINC_GRAL] Tarea '$tipo' finalizada.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SincGeneralWorker", "❌ [ERROR_SINC_GRAL] Fallo en background ($tipo): ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val CLAVE_TIPO_SYNC = "sync_type"
        const val CLAVE_ID_CHAT = "chat_id"
        const val TIPO_TELEMETRIA = "telemetry"
        const val TIPO_OBSERVAR_CHAT = "chat_observe"
    }
}



































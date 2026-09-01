package com.example.myapplication.prestador.obreros

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.myapplication.core.datos.repositorios.SincronizadorRemotoPrestador
import com.example.myapplication.prestador.datos.repositorios.PerfilPrestadorDeepRepositorio
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * --- OBRERO UNIFICADO DE SINCRONIZACIÓN (PRO - v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar tanto la subida (PUSH) como la descarga (PULL) de datos 
 * en segundo plano de forma garantizada.
 */
@HiltWorker
class SincPrestadorWorker @AssistedInject constructor(
    @Assisted contexto: Context,
    @Assisted parametros: WorkerParameters,
    private val repoRemoto: SincronizadorRemotoPrestador,
    private val repoDeep: PerfilPrestadorDeepRepositorio
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val uid = inputData.getString(CLAVE_UID) ?: return Result.failure()
        val tipo = inputData.getString(CLAVE_TIPO) ?: TIPO_PUSH

        return try {
            when (tipo) {
                TIPO_PUSH -> {
                    Log.d("SincWorker", "📤 [PUSH_START] Subiendo jerarquía profesional para: $uid")
                    repoRemoto.subirEcosistemaCompleto(uid)
                }
                TIPO_PULL -> {
                    Log.d("SincWorker", "📥 [PULL_START] Descargando jerarquía profesional para: $uid")
                    repoDeep.descargarEcosistemaCompleto(uid)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SincWorker", "❌ [ERROR_SINC] Fallo en background ($tipo): ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val CLAVE_UID = "uid"
        const val CLAVE_TIPO = "tipo_sinc"
        const val TIPO_PUSH = "PUSH"
        const val TIPO_PULL = "PULL"
    }
}

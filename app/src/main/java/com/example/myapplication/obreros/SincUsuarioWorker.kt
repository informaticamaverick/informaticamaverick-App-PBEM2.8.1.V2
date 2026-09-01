package com.example.myapplication.obreros

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.myapplication.datos.repositorios.SincUsuarioRepositorio
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

/**
 * --- OBRERO UNIFICADO DE SINCRONIZACIÓN USUARIO (US - v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar tanto la subida (PUSH) como la descarga (PULL) de datos 
 * del cliente en segundo plano de forma garantizada.
 */
@HiltWorker
class SincUsuarioWorker @AssistedInject constructor(
    @Assisted contexto: Context,
    @Assisted parametros: WorkerParameters,
    private val sincRepositorio: SincUsuarioRepositorio
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val uid = inputData.getString(CLAVE_UID) ?: return Result.failure()
        val tipo = inputData.getString(CLAVE_TIPO) ?: TIPO_PUSH

        return try {
            when (tipo) {
                TIPO_PUSH -> {
                    Log.d("SincUsuarioWorker", "📤 [PUSH_START] Subiendo perfil cliente para: $uid")
                    sincRepositorio.subirPerfilUsuarioCompleto(uid)
                }
                TIPO_PULL -> {
                    Log.d("SincUsuarioWorker", "📥 [PULL_START] Descargando jerarquía cliente para: $uid")
                    sincRepositorio.descargarPerfilUsuarioCompleto(uid)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("SincUsuarioWorker", "❌ [ERROR_SINC] Fallo en background ($tipo): ${e.message}")
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

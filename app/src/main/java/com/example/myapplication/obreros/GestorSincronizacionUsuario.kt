package com.example.myapplication.obreros

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE SINCRONIZACIÓN USUARIO (ELITE) ---
 * [PROPÓSITO]: Centralizar la planificación de tareas de fondo para la App Azul.
 */
@Singleton
class GestorSincronizacionUsuario @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val workManager = WorkManager.getInstance(contexto)

    /**
     * Encola la subida de cambios locales del cliente a la nube.
     */
    fun encolarSincronizacionPush(uid: String) {
        val datos = Data.Builder()
            .putString(SincUsuarioWorker.CLAVE_UID, uid)
            .putString(SincUsuarioWorker.CLAVE_TIPO, SincUsuarioWorker.TIPO_PUSH)
            .build()

        val peticion = OneTimeWorkRequestBuilder<SincUsuarioWorker>()
            .setInputData(datos)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork("push_cliente_$uid", ExistingWorkPolicy.KEEP, peticion)
    }

    /**
     * Encola la descarga completa del perfil del cliente desde la nube.
     */
    fun encolarRestauracionPull(uid: String) {
        val datos = Data.Builder()
            .putString(SincUsuarioWorker.CLAVE_UID, uid)
            .putString(SincUsuarioWorker.CLAVE_TIPO, SincUsuarioWorker.TIPO_PULL)
            .build()

        val peticion = OneTimeWorkRequestBuilder<SincUsuarioWorker>()
            .setInputData(datos)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork("pull_cliente_$uid", ExistingWorkPolicy.KEEP, peticion)
    }
}

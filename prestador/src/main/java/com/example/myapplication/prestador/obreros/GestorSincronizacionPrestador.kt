package com.example.myapplication.prestador.obreros

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE SINCRONIZACIÓN PRESTADOR (ELITE) ---
 * [PROPÓSITO]: Centralizar la planificación de tareas de fondo para la App Naranja.
 */
@Singleton
class GestorSincronizacionPrestador @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val workManager = WorkManager.getInstance(contexto)

    /**
     * Encola la subida de cambios locales a la nube.
     */
    fun encolarSincronizacionPush(uid: String) {
        val datos = Data.Builder()
            .putString(SincPrestadorWorker.CLAVE_UID, uid)
            .putString(SincPrestadorWorker.CLAVE_TIPO, SincPrestadorWorker.TIPO_PUSH)
            .build()

        val peticion = OneTimeWorkRequestBuilder<SincPrestadorWorker>()
            .setInputData(datos)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        workManager.enqueueUniqueWork("push_prestador_$uid", ExistingWorkPolicy.KEEP, peticion)
    }

    /**
     * Encola la descarga completa del ecosistema desde la nube.
     */
    fun encolarRestauracionPull(uid: String) {
        val datos = Data.Builder()
            .putString(SincPrestadorWorker.CLAVE_UID, uid)
            .putString(SincPrestadorWorker.CLAVE_TIPO, SincPrestadorWorker.TIPO_PULL)
            .build()

        val peticion = OneTimeWorkRequestBuilder<SincPrestadorWorker>()
            .setInputData(datos)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork("pull_prestador_$uid", ExistingWorkPolicy.KEEP, peticion)
    }
}

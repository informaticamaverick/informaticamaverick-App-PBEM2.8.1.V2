package com.example.myapplication.core.obreros

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- GESTOR DE SINCRONIZACIÓN GENERAL (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar las tareas de fondo transversales a todo el ecosistema 
 * (Telemetría, Señalización, Presencia).
 * [LEY #9]: Idioma Español. Independencia de Negocio.
 */
@Singleton
class GestorSincronizacionGeneral @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val workManager = WorkManager.getInstance(contexto)

    /**
     * Planifica la subida de telemetría (Likes/Views) en lote para ambas apps.
     */
    fun encolarSincronizacionTelemetria(inmediato: Boolean = false) {
        val datos = Data.Builder()
            .putString(SincGeneralWorker.CLAVE_TIPO_SYNC, SincGeneralWorker.TIPO_TELEMETRIA)
            .build()

        if (inmediato) {
            val peticion = OneTimeWorkRequestBuilder<SincGeneralWorker>()
                .setInputData(datos)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            workManager.enqueue(peticion)
        } else {
            val peticion = PeriodicWorkRequestBuilder<SincGeneralWorker>(12, java.util.concurrent.TimeUnit.HOURS)
                .setInputData(datos)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            workManager.enqueueUniquePeriodicWork("telemetry_sync_periodic", ExistingPeriodicWorkPolicy.KEEP, peticion)
        }
    }

    /**
     * Planifica la observación de un chat en segundo plano (Señalización).
     */
    fun encolarObservacionChat(idChat: String) {
        val datos = Data.Builder()
            .putString(SincGeneralWorker.CLAVE_TIPO_SYNC, SincGeneralWorker.TIPO_OBSERVAR_CHAT)
            .putString(SincGeneralWorker.CLAVE_ID_CHAT, idChat)
            .build()

        val peticion = OneTimeWorkRequestBuilder<SincGeneralWorker>()
            .setInputData(datos)
            .build()

        workManager.enqueue(peticion)
    }
}


































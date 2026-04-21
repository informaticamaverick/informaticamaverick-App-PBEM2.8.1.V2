package com.example.myapplication.prestador.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.prestador.worker.PresupuestoCleanupWorker
import java.util.concurrent.TimeUnit

object PresupuestoCleanupScheduler {

    fun scheduleDelete(context: Context, presupuestoId: String, delayDays: Long = 5) {
        val data = Data.Builder()
            .putString(PresupuestoCleanupWorker.KEY_PRESUPUESTO_ID, presupuestoId)
            .build()

        val request = OneTimeWorkRequestBuilder<PresupuestoCleanupWorker>()
            .setInitialDelay(delayDays, TimeUnit.DAYS)
            .setInputData(data)
            .addTag("cleanup_$presupuestoId")
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancel(context: Context, presupuestoId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("cleanup_$presupuestoId")
    }
}

package com.example.myapplication.prestador.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.prestador.data.repository.PresupuestoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PresupuestoCleanupWorker @AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val presupuestoRepository: PresupuestoRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val presupuestoId = inputData.getString(KEY_PRESUPUESTO_ID) ?:
        return Result.failure()
        val presupuesto = presupuestoRepository.getPresupuestoById(presupuestoId) ?:
        return Result.success()
        presupuestoRepository.deletePresupuesto(presupuesto)
        return Result.success()
    }

    companion object {
        const val KEY_PRESUPUESTO_ID = "presupuesto_id"
    }
}
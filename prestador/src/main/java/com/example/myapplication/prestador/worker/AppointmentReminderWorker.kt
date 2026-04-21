package com.example.myapplication.prestador.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.prestador.utils.NotificationHelper

class AppointmentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val clientName = inputData.getString(KEY_CLIENT_NAME) ?: return Result.failure()
        val service = inputData.getString(KEY_SERVICE) ?: return Result.failure()
        val date = inputData.getString(KEY_DATE) ?: return Result.failure()
        val time = inputData.getString(KEY_TIME) ?: return Result.failure()
        val hoursUntil = inputData.getInt(KEY_HOURS_UNTIL, 24)

        NotificationHelper(applicationContext).showReminderNotification(
            clientName = clientName,
            service = service,
            date = date,
            time = time,
            hoursUntil = hoursUntil
        )
        return Result.success()
    }

    companion object {
        const val KEY_CLIENT_NAME = "client_name"
        const val KEY_SERVICE = "service"
        const val KEY_DATE = "date"
        const val KEY_TIME = "time"
        const val KEY_HOURS_UNTIL = "hours_until"
    }
}

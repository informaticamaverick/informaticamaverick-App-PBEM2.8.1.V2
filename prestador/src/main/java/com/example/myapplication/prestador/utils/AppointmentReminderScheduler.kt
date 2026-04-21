package com.example.myapplication.prestador.utils

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.worker.AppointmentReminderWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AppointmentReminderScheduler {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * Programa recordatorios de 24h y 1h antes de la cita.
     * Cancela los anteriores si ya existían para esta cita.
     */
    fun schedule(context: Context, appointment: AppointmentEntity) {
        val appointmentMillis = parseAppointmentMillis(appointment.date, appointment.time)
            ?: return

        val now = System.currentTimeMillis()
        val wm = WorkManager.getInstance(context)

        // Cancelar recordatorios anteriores para esta cita
        wm.cancelAllWorkByTag("reminder_${appointment.id}")

        listOf(24 to TimeUnit.HOURS.toMillis(24), 1 to TimeUnit.HOURS.toMillis(1))
            .forEach { (hours, offset) ->
                val delay = appointmentMillis - offset - now
                if (delay > 0) {
                    val data = Data.Builder()
                        .putString(AppointmentReminderWorker.KEY_CLIENT_NAME, appointment.clientName)
                        .putString(AppointmentReminderWorker.KEY_SERVICE, appointment.service)
                        .putString(AppointmentReminderWorker.KEY_DATE, appointment.date)
                        .putString(AppointmentReminderWorker.KEY_TIME, appointment.time)
                        .putInt(AppointmentReminderWorker.KEY_HOURS_UNTIL, hours)
                        .build()

                    val request = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("reminder_${appointment.id}")
                        .build()

                    wm.enqueue(request)
                }
            }
    }

    /**
     * Cancela los recordatorios de una cita (cuando se cancela o completa)
     */
    fun cancel(context: Context, appointmentId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$appointmentId")
    }

    private fun parseAppointmentMillis(date: String, time: String): Long? {
        return try {
            sdf.parse("$date $time")?.time
        } catch (e: Exception) {
            null
        }
    }
}

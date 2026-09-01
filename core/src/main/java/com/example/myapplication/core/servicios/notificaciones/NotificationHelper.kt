/*
package com.example.myapplication.core.servicios.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.core.R

/**
 * --- NOTIFICATION HELPER (PROTOCOLO 2026) ---
 * Gestiona los canales y el envío de notificaciones locales para el ecosistema Maverick.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID_MESSAGES = "maverick_messages"
        const val CHANNEL_ID_TENDERS = "maverick_tenders"
        const val CHANNEL_ID_AGENDA = "maverick_agenda"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_MESSAGES, "Mensajes", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_ID_TENDERS, "Licitaciones", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_ID_AGENDA, "Agenda", NotificationManager.IMPORTANCE_DEFAULT)
            )
            notificationManager.createNotificationChannels(channels)
        }
    }

    fun showNotification(title: String, message: String, channelId: String = CHANNEL_ID_MESSAGES) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun showAgendaNotification(title: String, message: String) {
        showNotification(title, message, CHANNEL_ID_AGENDA)
    }
}
*/



































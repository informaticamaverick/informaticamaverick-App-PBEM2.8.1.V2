/*
package com.example.myapplication.core.utilidades

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- NOTIFICADOR DE CHAT MAVERICK (v2026.ELITE) ---
 * [PROPÓSITO]: Disparar notificaciones locales de Android cuando llega un mensaje nuevo.
 * [LEY #4]: Inmediatez. El usuario debe saber que tiene un mensaje aunque la app esté en fondo.
 */
@Singleton
class ChatNotifierMav @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "chat_notifications"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensajes de Chat",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes en Maverick"
                enableLights(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun mostrarNotificacion(chatId: String, remitente: String, mensaje: String) {
        // Intent para abrir la app en el chat específico
        // Nota: El intent real dependerá de la estructura de MainActivity de cada app.
        // Usamos un intent genérico que sea capturado por el router.
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            putExtra("chatId", chatId)
            putExtra("target_type", "message")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(remitente)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(chatId.hashCode(), notification)
    }
}
*/



































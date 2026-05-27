package com.example.myapplication.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

/**
 * --- AYUDANTE DE NOTIFICACIONES (COMPARTIDO) ---
 * Esta clase centraliza la creación de canales y el envío de alertas visuales.
 * Es fundamental para notificar al usuario sobre nuevos mensajes de chat,
 * presupuestos recibidos o recordatorios de agenda.
 * 
 * NOTA: Dado que :core no conoce el MainActivity, el Intent de apertura debe ser
 * configurado dinámicamente o apuntar a una acción genérica.
 */
class NotificationHelper(private val context: Context) {

    private val CHAT_CHANNEL_ID = "chat_channel_id"
    private val CHAT_CHANNEL_NAME = "Mensajes de Chat"
    
    private val AGENDA_CHANNEL_ID = "agenda_channel_id"
    private val AGENDA_CHANNEL_NAME = "Recordatorios de Agenda"

    init {
        createNotificationChannels()
    }

    /**
     * Registra los canales de notificación requeridos por Android 8.0+.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            
            // Canal para Chat
            val chatChannel = NotificationChannel(
                CHAT_CHANNEL_ID,
                CHAT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes recibidos"
                enableVibration(true)
            }
            manager.createNotificationChannel(chatChannel)

            // Canal para Agenda/Turnos
            val agendaChannel = NotificationChannel(
                AGENDA_CHANNEL_ID,
                AGENDA_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de citas y visitas técnicas"
                enableVibration(true)
            }
            manager.createNotificationChannel(agendaChannel)
        }
    }

    /**
     * Muestra una notificación push en el dispositivo.
     */
    @SuppressLint("MissingPermission")
    fun showNotification(title: String, message: String, channelId: String = CHAT_CHANNEL_ID) {
        // Validación de permisos para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // Icono genérico por defecto
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            NotificationManagerCompat.from(context).notify(Random.nextInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Atajo para notificaciones de la agenda.
     */
    fun showAgendaNotification(title: String, message: String) {
        showNotification(title, message, AGENDA_CHANNEL_ID)
    }
}

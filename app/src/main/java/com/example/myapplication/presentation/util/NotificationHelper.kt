package com.example.myapplication.presentation.util

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
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import kotlin.random.Random

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "chat_channel_id"
    private val CHANNEL_NAME = "Mensajes de Chat"
    
    private val AGENDA_CHANNEL_ID = "agenda_channel_id"
    private val AGENDA_CHANNEL_NAME = "Recordatorios de Agenda"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal de Chat
            val chatChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes recibidos"
                enableVibration(true)
            }
            manager.createNotificationChannel(chatChannel)

            // Canal de Agenda
            val agendaChannel = NotificationChannel(
                AGENDA_CHANNEL_ID,
                AGENDA_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de citas y visitas técnicas"
                enableVibration(true)
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            manager.createNotificationChannel(agendaChannel)
        }
    }

    /**
     * Muestra la notificación.
     */
    @SuppressLint("MissingPermission")
    fun showNotification(title: String, message: String, channelId: String = CHANNEL_ID) {
        // ... (rest of verification logic remains same)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            NotificationManagerCompat.from(context).notify(Random.nextInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun showAgendaNotification(title: String, message: String) {
        showNotification(title, message, AGENDA_CHANNEL_ID)
    }

    @SuppressLint("MissingPermission")
    fun showChatNotification(
        senderId: String,
        senderName: String,
        msgType: String,
        appointmentStatus: String? = null,
        appointmentTitle: String? = null
    ) {
        val title = senderName
        val content = when (msgType) {
            "TEXT" -> "Nuevo mensaje de texto"
            "IMAGE" -> "Imagen recibida"
            "AUDIO" -> "Audio recibido"
            "LOCATION" -> "Ubicación recibida"
            "VIDEO" -> "Video recibido"
            "BUDGET" -> "Nuevo presupuesto"
            "APPOINTMENT", "VISIT" -> {
                val status = when (appointmentStatus) {
                    "PENDING" -> "Pendiente"
                    "ACCEPTED" -> "Aceptada"
                    "REJECTED" -> "Rechazada"
                    "CANCELLED" -> "Cancelada"
                    "COMPLETED" -> "Completada"
                    else -> appointmentStatus ?: ""
                }
                if (status.isNotEmpty()) "Cita $status: ${appointmentTitle ?: ""}"
                else "Nueva cita: ${appointmentTitle ?: ""}"
            }
            else -> "Nuevo mensaje"
        }
        showNotification(title, content)
    }
}


/**
package com.example.myapplication.presentation.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.MainActivity // Asegúrate de importar tu MainActivity
import com.example.myapplication.R
import kotlin.random.Random

/**
 * NOTIFICATION HELPER
 * Clase encargada de gestionar y mostrar las notificaciones del sistema.
 * Se usará tanto para notificaciones locales (Bot) como remotas (Firebase).
 */
class NotificationHelper(private val context: Context) {

    // ID y Nombre del canal (Obligatorio para Android 8.0+)
    private val CHANNEL_ID = "chat_channel_id"
    private val CHANNEL_NAME = "Mensajes de Chat"

    init {
        createNotificationChannel()
    }

    // 1. Crear el canal (Solo se ejecuta en Android O o superior)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // HIGH hace que suene y aparezca arriba
            ).apply {
                description = "Notificaciones de nuevos mensajes recibidos"
                enableVibration(true)
            }

            // Registrar el canal en el sistema
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // 2. Mostrar la notificación
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification(title: String, message: String) {
        // Intent: Qué pasa cuando toco la notificación (Abrir la App)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE // Requerido en Android 12+
        )

        // Construir la UI de la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ⚠️ Asegúrate de tener un icono aquí (o usa R.drawable.iconapp si tienes uno propio)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Conectar el click
            .setAutoCancel(true) // Se borra al tocarla
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Se ve en pantalla bloqueada

        // Mostrarla
        try {
            // Usamos un ID aleatorio para que no se reemplacen entre sí si llegan muchas
            val notificationId = Random.nextInt()
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // En Android 13+ si el usuario no dio permiso, esto falla silenciosamente (lo manejaremos luego)
            e.printStackTrace()
        }
    }
}**/
package com.example.myapplication.prestador.utils

import android.Manifest
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

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_CHAT_ID  = "chat_messages"
        const val CHANNEL_CITAS_ID = "citas_channel"
        const val NOTIFICATION_ID_BASE = 10000
    }

    init { createChannels() }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_CHAT_ID, "Mensajes de chat",
                        NotificationManager.IMPORTANCE_HIGH).apply {
                        enableVibration(true); setShowBadge(true)
                    }
                )
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL_CITAS_ID, "Solicitudes y citas",
                        NotificationManager.IMPORTANCE_HIGH).apply {
                        enableVibration(true); setShowBadge(true)
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (e: AssertionError) {
                // LayoutLib en Compose Preview arroja AssertionError si el servicio no está soportado
                e.printStackTrace()
            }
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun notify(id: Int, notification: android.app.Notification) {
        if (!hasPermission()) return
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: Exception) {
            android.util.Log.e("NotifHelper", "Error: ${e.message}")
        }
    }

    private fun buildPendingIntent(userId: String, userName: String, notifId: Int): PendingIntent {
        val intent = Intent(context,
            Class.forName("com.example.myapplication.prestador.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_chat", true)
            putExtra("user_id", userId)
            putExtra("user_name", userName)
        }
        return PendingIntent.getActivity(context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    /**
     * Notificación unificada para todos los tipos de mensaje del chat.
     */
    fun showChatNotification(
        senderId: String,
        senderName: String,
        msgType: String,
        appointmentStatus: String? = null,
        appointmentTitle: String? = null
    ) {
        val notifId = NOTIFICATION_ID_BASE + senderId.hashCode()
        val pendingIntent = buildPendingIntent(senderId, senderName, notifId)

        val (title, body, channel) = when (msgType) {
            "AUDIO"    -> Triple(senderName, "🎵 Te envió un audio", CHANNEL_CHAT_ID)
            "IMAGE"    -> Triple(senderName, "📷 Te envió una imagen", CHANNEL_CHAT_ID)
            "LOCATION" -> Triple(senderName, "📍 Compartió su ubicación", CHANNEL_CHAT_ID)
            "BUDGET"   -> Triple("💰 Nuevo presupuesto", "$senderName respondió un presupuesto", CHANNEL_CHAT_ID)
            "VISIT", "APPOINTMENT" -> {
                val appt = appointmentTitle?.let { "\"$it\"" } ?: "una cita"
                when (appointmentStatus) {
                    "PENDING"   -> Triple("📅 Solicitud de cita", "$senderName solicitó $appt", CHANNEL_CITAS_ID)
                    "CONFIRMED" -> Triple("✅ Cita confirmada",   "$senderName confirmó $appt",  CHANNEL_CITAS_ID)
                    "REJECTED"  -> Triple("❌ Cita rechazada",    "$senderName rechazó $appt",   CHANNEL_CITAS_ID)
                    "CANCELLED" -> Triple("🚫 Cita cancelada",    "$senderName canceló $appt",   CHANNEL_CITAS_ID)
                    else        -> Triple("📅 Cita actualizada",  "$senderName actualizó $appt", CHANNEL_CITAS_ID)
                }
            }
            else -> Triple(senderName, "💬 Nuevo mensaje", CHANNEL_CHAT_ID)
        }

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 250, 250, 250))
            .build()

        notify(notifId, notification)
        android.util.Log.d("NotifHelper", "📬 $title — $body")
    }

    // ── Métodos existentes mantenidos ─────────────────────────────────────────

    fun showAppointmentConfirmedNotification(clientName: String, date: String, time: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_CITAS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("✅ Cita Confirmada")
            .setContentText("$clientName confirmó su cita para el $date a las $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true).build()
        notify(NOTIFICATION_ID_BASE + 1, notification)
    }

    fun showReminderNotification(clientName: String, service: String,
                                 date: String, time: String, hoursUntil: Int) {
        val title = if (hoursUntil == 1) "⏰ Cita en 1 hora" else "📅 Cita mañana"
        val text  = "$service con $clientName — $date a las $time"
        val notification = NotificationCompat.Builder(context, CHANNEL_CITAS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250)).build()
        notify(NOTIFICATION_ID_BASE + 2000 + clientName.hashCode() + hoursUntil, notification)
    }

    fun showSolicitudFastNotification(titulo: String, clienteNombre: String, distanciaKm: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_CITAS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚡ Nueva urgencia Fast: $titulo")
            .setContentText("Cliente: $clienteNombre · %.1f km".format(distanciaKm))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM).setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 200, 400)).build()
        notify(NOTIFICATION_ID_BASE + 9000 + titulo.hashCode(), notification)
    }

    fun showPresupuestoAceptadoNotification(clientName: String, total: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_CHAT_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("💰 Presupuesto aceptado")
            .setContentText("$clientName aceptó el presupuesto por $${"%.2f".format(total)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build()
        notify(NOTIFICATION_ID_BASE + 3, notification)
    }

    fun cancelNotification(notificationId: Int) =
        NotificationManagerCompat.from(context).cancel(notificationId)

    fun hasNotificationPermission(): Boolean = hasPermission()
}

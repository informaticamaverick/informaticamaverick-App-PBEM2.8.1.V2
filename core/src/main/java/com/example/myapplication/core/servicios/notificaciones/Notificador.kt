package com.example.myapplication.core.servicios.notificaciones

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
 * --- NOTIFICADOR MAESTRO (v2026.ELITE) ---
 */
@Singleton
class Notificador @Inject constructor(
    @ApplicationContext private val contexto: Context
) {
    private val gestorNotificaciones = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CANAL_MENSAJES = "mensajes"
        const val CANAL_CONCURSOS = "concursos"
        const val CANAL_AGENDA = "agenda"
        const val CANAL_SISTEMA = "sistema"
    }

    init {
        crearCanales()
    }

    private fun crearCanales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canales = listOf(
                NotificationChannel(CANAL_MENSAJES, "Mensajes", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Conversaciones con prestadores y clientes"
                },
                NotificationChannel(CANAL_CONCURSOS, "Oportunidades", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Nuevos concursos y presupuestos"
                },
                NotificationChannel(CANAL_AGENDA, "Agenda y Turnos", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Recordatorios de compromisos"
                },
                NotificationChannel(CANAL_SISTEMA, "Alertas del Sistema", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Notificaciones de mantenimiento y seguridad"
                }
            )
            gestorNotificaciones.createNotificationChannels(canales)
        }
    }

    fun mostrarAvisoChat(idChat: String, remitente: String, mensaje: String) {
        val intent = contexto.packageManager.getLaunchIntentForPackage(contexto.packageName)?.apply {
            putExtra("chatId", idChat)
            putExtra("target_type", "message")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pi = PendingIntent.getActivity(
            contexto, idChat.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val aviso = NotificationCompat.Builder(contexto, CANAL_MENSAJES)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(remitente)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        gestorNotificaciones.notify(idChat.hashCode(), aviso)
    }
    


    fun mostrarAvisoGeneral(titulo: String, mensaje: String, idRef: String? = null, tipo: String = "SISTEMA") {
        val canalId = when(tipo) {
            "CONCURSO", "PRESUPUESTO" -> CANAL_CONCURSOS
            "AGENDA" -> CANAL_AGENDA
            else -> CANAL_SISTEMA
        }

        val intent = contexto.packageManager.getLaunchIntentForPackage(contexto.packageName)?.apply {
            idRef?.let { putExtra("idReferencia", it) }
            putExtra("target_type", tipo.lowercase())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pi = PendingIntent.getActivity(
            contexto, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val aviso = NotificationCompat.Builder(contexto, canalId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        gestorNotificaciones.notify(System.currentTimeMillis().toInt(), aviso)
    }
}

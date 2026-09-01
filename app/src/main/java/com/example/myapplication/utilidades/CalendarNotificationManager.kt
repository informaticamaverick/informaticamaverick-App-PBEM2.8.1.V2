/*
package com.example.myapplication.utilidades

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import java.util.*

class CalendarNotificationManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleEventNotification(event: EventoEntity) {
        val eventTime = event.fechaInicioUtc
        
        // Si no hay tiempo definido o el evento ya pasó, no agendamos nada
        if (eventTime <= 0L || eventTime < System.currentTimeMillis()) return

        // 1. Recordatorio 15 minutos antes
        val reminderTime = eventTime - 15 * 60 * 1000
        if (reminderTime > System.currentTimeMillis()) {
            setAlarm(
                event.id.hashCode(),
                reminderTime,
                "Próximo: ${event.titulo}",
                "En 15 minutos: ${event.nombreSucursal ?: "Servicio"} en ${event.direccion}"
            )
        }

        // 2. Notificación al momento de inicio
        setAlarm(
            event.id.hashCode() + 1,
            eventTime,
            "Ahora: ${event.titulo}",
            "Iniciando compromiso con ${event.nombreSucursal ?: "Servicio"}"
        )
    }

    private fun setAlarm(requestCode: Int, triggerTime: Long, title: String, message: String) {
        val intent = Intent(context, CalendarNotificationReceiver::class.java).apply {
            putExtra("EVENT_TITLE", title)
            putExtra("EVENT_MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelEventNotification(event: EventoEntity) {
        val intent = Intent(context, CalendarNotificationReceiver::class.java)
        
        val p1 = PendingIntent.getBroadcast(context, event.id.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        p1?.let { alarmManager.cancel(it) }

        val p2 = PendingIntent.getBroadcast(context, event.id.hashCode() + 1, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        p2?.let { alarmManager.cancel(it) }
    }
}
*/






























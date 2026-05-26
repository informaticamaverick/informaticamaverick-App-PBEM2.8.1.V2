package com.example.myapplication.presentation.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.myapplication.data.local.CalendarEventEntity
import java.text.SimpleDateFormat
import java.util.*

class CalendarNotificationManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleEventNotification(event: CalendarEventEntity) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val eventTime = try {
            sdf.parse("${event.date} ${event.time}")?.time ?: return
        } catch (e: Exception) {
            return
        }

        // Si el evento ya pasó, no agendamos nada
        if (eventTime < System.currentTimeMillis()) return

        // 1. Recordatorio 15 minutos antes
        val reminderTime = eventTime - 15 * 60 * 1000
        if (reminderTime > System.currentTimeMillis()) {
            setAlarm(
                event.id.hashCode(),
                reminderTime,
                "Próximo: ${event.title}",
                "En 15 minutos: ${event.provider} en ${event.address}"
            )
        }

        // 2. Notificación al momento de inicio
        setAlarm(
            event.id.hashCode() + 1,
            eventTime,
            "Ahora: ${event.title}",
            "Iniciando compromiso con ${event.provider}"
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

    fun cancelEventNotification(event: CalendarEventEntity) {
        val intent = Intent(context, CalendarNotificationReceiver::class.java)
        
        val p1 = PendingIntent.getBroadcast(context, event.id.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        p1?.let { alarmManager.cancel(it) }

        val p2 = PendingIntent.getBroadcast(context, event.id.hashCode() + 1, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        p2?.let { alarmManager.cancel(it) }
    }
}

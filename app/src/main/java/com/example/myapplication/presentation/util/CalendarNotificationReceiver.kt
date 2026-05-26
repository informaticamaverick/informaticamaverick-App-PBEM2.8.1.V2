package com.example.myapplication.presentation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CalendarNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EVENT_TITLE") ?: "Recordatorio de Agenda"
        val message = intent.getStringExtra("EVENT_MESSAGE") ?: "Tienes un compromiso próximamente."
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, message)
    }
}

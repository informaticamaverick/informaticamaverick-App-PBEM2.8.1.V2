package com.example.myapplication.data.repository

import com.example.myapplication.data.local.CalendarDao
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.presentation.util.CalendarNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
    @ApplicationContext private val context: android.content.Context
) {
    private val notificationManager = CalendarNotificationManager(context)

    // Flujo en tiempo real de todos los eventos
    val allEvents: Flow<List<CalendarEventEntity>> = calendarDao.getAllEvents()

    suspend fun addEvent(event: CalendarEventEntity) {
        calendarDao.insertEvent(event)
        notificationManager.scheduleEventNotification(event)
    }

    suspend fun cancelEvent(eventId: String) {
        calendarDao.updateEventStatus(eventId, VisitStatus.CANCELLED)
        // Opcional: Buscar el evento y cancelar notificación si es necesario
    }

    suspend fun deleteEvent(eventId: String) {
        calendarDao.deleteEvent(eventId)
    }
}
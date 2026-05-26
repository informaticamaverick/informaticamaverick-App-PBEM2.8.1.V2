package com.example.myapplication.core.data.repository

import android.util.Log
import com.example.myapplication.core.data.local.dao.CalendarDao
import com.example.myapplication.core.data.local.dao.CategoryDao
import com.example.myapplication.core.data.local.entity.CalendarEventEntity
import com.example.myapplication.core.data.local.entity.VisitStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE CALENDARIO (COMPARTIDO) ---
 * Centraliza la gestión de visitas técnicas y citas confirmadas.
 * Sincroniza los eventos con la base de datos local para acceso offline.
 */
@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
    private val categoryDao: CategoryDao,
    private val firestore: FirebaseFirestore
) {

    /**
     * Observable de todos los eventos del calendario local.
     */
    val allEvents: Flow<List<CalendarEventEntity>> = calendarDao.getAllEvents()

    /**
     * Agrega un nuevo evento al calendario local.
     */
    suspend fun addEvent(event: CalendarEventEntity) {
        calendarDao.insertEvent(event)
    }

    /**
     * Sincroniza y guarda inteligentemente un evento (ej: desde un comprobante de chat).
     */
    suspend fun saveSmartEvent(
        id: String,
        rawDate: String,
        rawTime: String,
        title: String,
        providerId: String,
        providerName: String? = null,
        providerPhotoUrl: String? = null,
        categoryId: String? = null,
        status: VisitStatus = VisitStatus.CONFIRMED
    ) {
        try {
            val event = CalendarEventEntity(
                id = if (id.startsWith("evt_")) id else "evt_$id",
                date = rawDate,
                time = rawTime,
                type = com.example.myapplication.core.data.local.entity.EventType.APPOINTMENT,
                title = title,
                provider = providerName ?: "Cargando...",
                providerId = providerId,
                address = "Ver detalles en Chat",
                status = status,
                providerPhotoUrl = providerPhotoUrl,
                categoryName = categoryId // En este contexto usamos categoryId como nombre si aplica
            )
            calendarDao.insertEvent(event)
            Log.d("CalendarRepository", "Evento Maverick guardado: ${event.title}")

        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error en saveSmartEvent: ${e.message}")
        }
    }

    suspend fun cancelEvent(eventId: String) {
        calendarDao.updateEventStatus(eventId, VisitStatus.CANCELLED)
    }

    suspend fun deleteEvent(eventId: String) {
        calendarDao.deleteEvent(eventId)
    }
}

package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.CalendarDao
import com.example.myapplication.data.local.CalendarEventEntity
import com.example.myapplication.data.local.CategoryDao
import com.example.myapplication.data.local.EventType
import com.example.myapplication.data.local.VisitStatus
import com.example.myapplication.presentation.util.CalendarNotificationManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarDao: CalendarDao,
    private val categoryDao: CategoryDao,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: android.content.Context
) {
    private val notificationManager = CalendarNotificationManager(context)

    // Flujo en tiempo real de todos los eventos
    val allEvents: Flow<List<CalendarEventEntity>> = calendarDao.getAllEvents()

    suspend fun addEvent(event: CalendarEventEntity) {
        calendarDao.insertEvent(event)
        notificationManager.scheduleEventNotification(event)
    }

    /**
     * --- MAVERICK SMART SAVE ---
     * Procesa, normaliza y guarda un evento en el calendario local.
     * Centraliza la inteligencia para que los ViewModels sean "tontos".
     */
    suspend fun saveSmartEvent(
        id: String,
        rawDate: String,
        rawTime: String,
        title: String,
        providerId: String,
        providerName: String? = null,
        providerPhotoUrl: String? = null,
        address: String? = null,
        categoryId: String? = null,
        appointmentType: String? = null,
        isTechnician: Boolean? = null
    ) {
        try {
            // 1. NORMALIZACIÓN DE FECHA Y HORA (Regresa a Maverick)
            val normalizedDate = unformatDate(rawDate)
            val normalizedTime = normalizeTime(rawTime)

            // 2. DETERMINAR TIPO DE EVENTO
            val eventType = when {
                appointmentType == "TECHNICAL_VISIT" -> EventType.VISIT
                appointmentType == "LOCAL_APPOINTMENT" -> EventType.APPOINTMENT
                isTechnician == true -> EventType.VISIT
                title.contains("técnica", ignoreCase = true) -> EventType.VISIT
                title.contains("envío", ignoreCase = true) || title.contains("flete", ignoreCase = true) -> EventType.SHIPPING
                else -> EventType.APPOINTMENT
            }

            // 3. RECUPERAR METADATOS DE CATEGORÍA (Zero Cost)
            val category = categoryId?.let { categoryDao.getCategoryByName(it) }
            val resolvedCategoryName = category?.name ?: "Servicio"
            val resolvedCategoryEmoji = category?.icon ?: "📍"

            // 4. RECUPERAR INFO FALTANTE DEL PROVEEDOR (Sync Silencioso)
            var finalProviderName = providerName
            var finalPhotoUrl = providerPhotoUrl

            if (finalProviderName == null || finalPhotoUrl == null) {
                try {
                    val provDoc = firestore.collection("providers").document(providerId).get().await()
                    if (provDoc.exists()) {
                        if (finalProviderName == null) {
                            val perfil = provDoc.get("perfil") as? Map<*, *>
                            finalProviderName = (perfil?.get("nombre") as? String) ?: provDoc.getString("displayName") ?: provDoc.getString("nombre")
                        }
                        if (finalPhotoUrl == null) {
                            finalPhotoUrl = provDoc.getString("photoUrl")
                        }
                    }
                } catch (e: Exception) {
                    Log.w("CalendarRepository", "No se pudo sincronizar perfil del proveedor $providerId")
                }
            }

            // 5. CONSTRUIR ENTIDAD FINAL
            val event = CalendarEventEntity(
                id = if (id.startsWith("evt_")) id else "evt_$id",
                date = normalizedDate,
                time = normalizedTime,
                type = eventType,
                title = title.split("|").lastOrNull()?.trim() ?: title,
                provider = finalProviderName ?: "Prestador",
                providerId = providerId,
                address = address ?: "Ver detalles en Chat",
                status = VisitStatus.CONFIRMED,
                categoryName = resolvedCategoryName,
                categoryEmoji = resolvedCategoryEmoji,
                providerPhotoUrl = finalPhotoUrl,
                avatarColorLong = eventType.colorLong
            )

            // 6. PERSISTIR Y NOTIFICAR
            calendarDao.insertEvent(event)
            notificationManager.scheduleEventNotification(event)
            Log.d("CalendarRepository", "✅ Evento Maverick guardado: ${event.title} ($normalizedDate $normalizedTime)")

        } catch (e: Exception) {
            Log.e("CalendarRepository", "❌ Error en saveSmartEvent: ${e.message}")
        }
    }

    suspend fun cancelEvent(eventId: String) {
        calendarDao.updateEventStatus(eventId, VisitStatus.CANCELLED)
    }

    suspend fun deleteEvent(eventId: String) {
        calendarDao.deleteEvent(eventId)
    }

    /**
     * NORMALIZACIÓN DE FECHA: "Dom 12/05/2024" -> "2024-05-12"
     */
    private fun unformatDate(date: String): String {
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return date
        
        val formats = listOf(
            SimpleDateFormat("EEE dd/MM/yyyy", Locale("es", "ES")),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
        )
        
        val output = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val clean = date.replace("hs", "").trim()
        
        for (f in formats) {
            try {
                val d = f.parse(clean)
                if (d != null) return output.format(d)
            } catch (e: Exception) { continue }
        }
        return date
    }

    /**
     * NORMALIZACIÓN DE HORA: "10:30 hs" -> "10:30"
     */
    private fun normalizeTime(time: String): String {
        if (time.isBlank()) return "00:00"
        val clean = time.lowercase().replace("hs", "").replace(".", "").trim()
        
        val patterns = listOf("HH:mm", "H:mm", "hh:mm a", "h:mm a")
        val out = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, if (p.contains("a")) Locale.US else Locale.getDefault())
                val d = sdf.parse(clean)
                if (d != null) return out.format(d)
            } catch (e: Exception) { continue }
        }
        return clean.take(5).filter { it.isDigit() || it == ':' }
    }
}

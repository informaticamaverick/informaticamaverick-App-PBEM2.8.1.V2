package com.example.myapplication.core.data.remote

import android.util.Log
import com.example.myapplication.core.data.local.entity.CalendarEventEntity
import com.example.myapplication.core.data.local.entity.EventType
import com.example.myapplication.core.data.local.entity.VisitStatus
import com.google.firebase.firestore.DocumentSnapshot

/**
 * --- APPOINTMENT DATA MAPPER (COMPARTIDO) ---
 * Centraliza la conversión de Turnos y Citas desde Firestore a eventos de Calendario.
 */
object AppointmentDataMapper {

    fun fromFirestore(doc: DocumentSnapshot): CalendarEventEntity? {
        if (!doc.exists()) return null
        return try {
            val data = doc.data ?: return null
            CalendarEventEntity(
                id = doc.id,
                date = data["requestDateStr"] as? String ?: data["date"] as? String ?: "",
                time = data["requestTime"] as? String ?: data["time"] as? String ?: "",
                type = try { EventType.valueOf(data["type"] as? String ?: "APPOINTMENT") } catch(e: Exception) { EventType.APPOINTMENT },
                title = data["serviceName"] as? String ?: data["title"] as? String ?: "Turno",
                provider = data["providerName"] as? String ?: data["provider"] as? String ?: "Profesional",
                providerId = data["providerId"] as? String ?: "",
                address = data["address"] as? String ?: "A convenir",
                status = try { VisitStatus.valueOf(data["status"] as? String ?: "PENDING") } catch(e: Exception) { VisitStatus.PENDING },
                categoryName = data["categoryName"] as? String ?: data["categoria"] as? String,
                categoryEmoji = data["categoryEmoji"] as? String ?: data["emoji"] as? String,
                providerPhotoUrl = data["providerPhotoUrl"] as? String ?: data["photoUrl"] as? String,
                avatarColorLong = (data["avatarColorLong"] as? Number)?.toLong() ?: 0xFF161C24
            )
        } catch (e: Exception) {
            Log.e("AppointmentDataMapper", "Error mapeando Turno ${doc.id}: ${e.message}")
            null
        }
    }
}

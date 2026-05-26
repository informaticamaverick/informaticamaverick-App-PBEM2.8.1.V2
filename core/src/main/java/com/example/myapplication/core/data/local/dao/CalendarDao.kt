package com.example.myapplication.core.data.local.dao

import androidx.room.*
import com.example.myapplication.core.data.local.entity.CalendarEventEntity
import com.example.myapplication.core.data.local.entity.VisitStatus
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA CALENDARIO (COMPARTIDO) ---
 */
@Dao
interface CalendarDao {

    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Query("UPDATE calendar_events SET status = :status WHERE id = :eventId")
    suspend fun updateEventStatus(eventId: String, status: VisitStatus)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("SELECT * FROM calendar_events WHERE date = :date")
    suspend fun getEventsByDate(date: String): List<CalendarEventEntity>
}

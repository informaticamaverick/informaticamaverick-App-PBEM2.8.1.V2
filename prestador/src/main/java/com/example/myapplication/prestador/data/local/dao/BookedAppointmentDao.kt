package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.BookedAppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookedAppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: BookedAppointmentEntity)

    @Query("SELECT * FROM booked_appointments ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<BookedAppointmentEntity>>

    @Query("SELECT * FROM booked_appointments WHERE date = :date ORDER BY time ASC")
    fun getAppointmentsForDate(date: String): Flow<List<BookedAppointmentEntity>>

    @Query("UPDATE booked_appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM booked_appointments WHERE id = :id")
    suspend fun deleteById(id: String)
}

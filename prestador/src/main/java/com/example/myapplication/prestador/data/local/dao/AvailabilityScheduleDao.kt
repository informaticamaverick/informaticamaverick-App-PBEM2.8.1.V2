package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos con availability_schedules
 */
@Dao
interface AvailabilityScheduleDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: AvailabilityScheduleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<AvailabilityScheduleEntity>)
    
    @Update
    suspend fun updateSchedule(schedule: AvailabilityScheduleEntity)
    
    @Delete
    suspend fun deleteSchedule(schedule: AvailabilityScheduleEntity)
    
    @Query("DELETE FROM availability_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: String)
    
    @Query("DELETE FROM availability_schedules WHERE providerId = :providerId")
    suspend fun deleteAllSchedulesByProvider(providerId: String)
    
    @Query("SELECT * FROM availability_schedules WHERE providerId = :providerId AND isActive = 1 ORDER BY dayOfWeek, startTime")
    fun getActiveSchedulesByProvider(providerId: String): Flow<List<AvailabilityScheduleEntity>>
    
    @Query("SELECT * FROM availability_schedules WHERE providerId = :providerId ORDER BY dayOfWeek, startTime")
    fun getAllSchedulesByProvider(providerId: String): Flow<List<AvailabilityScheduleEntity>>
    
    @Query("SELECT * FROM availability_schedules WHERE providerId = :providerId AND dayOfWeek = :dayOfWeek AND isActive = 1")
    fun getSchedulesByDay(providerId: String, dayOfWeek: Int): Flow<List<AvailabilityScheduleEntity>>
    
    @Query("SELECT * FROM availability_schedules WHERE providerId = :providerId AND dayOfWeek = :dayOfWeek")
    suspend fun getByProviderIdAndDaySuspend(providerId: String, dayOfWeek: Int): List<AvailabilityScheduleEntity>
    
    @Query("SELECT * FROM availability_schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): AvailabilityScheduleEntity?
    
    @Query("SELECT COUNT(*) FROM availability_schedules WHERE providerId = :providerId AND isActive = 1")
    suspend fun countActiveSchedules(providerId: String): Int
    
    @Query("UPDATE availability_schedules SET isActive = :isActive WHERE id = :scheduleId")
    suspend fun updateScheduleStatus(scheduleId: String, isActive: Boolean)
}

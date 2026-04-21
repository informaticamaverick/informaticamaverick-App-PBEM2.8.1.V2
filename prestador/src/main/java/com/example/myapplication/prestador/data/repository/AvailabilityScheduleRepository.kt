package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.AvailabilityScheduleDao
import com.example.myapplication.prestador.data.local.entity.AvailabilityScheduleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityScheduleRepository @Inject constructor(
    private val scheduleDao: AvailabilityScheduleDao
) {
    
    suspend fun saveSchedule(schedule: AvailabilityScheduleEntity) {
        scheduleDao.insertSchedule(schedule)
    }
    
    suspend fun saveSchedules(schedules: List<AvailabilityScheduleEntity>) {
        scheduleDao.insertSchedules(schedules)
    }
    
    suspend fun updateSchedule(schedule: AvailabilityScheduleEntity) {
        scheduleDao.updateSchedule(schedule)
    }
    
    suspend fun deleteSchedule(schedule: AvailabilityScheduleEntity) {
        scheduleDao.deleteSchedule(schedule)
    }
    
    suspend fun deleteScheduleById(scheduleId: String) {
        scheduleDao.deleteScheduleById(scheduleId)
    }
    
    suspend fun deleteAllSchedulesByProvider(providerId: String) {
        scheduleDao.deleteAllSchedulesByProvider(providerId)
    }
    
    fun getActiveSchedulesByProvider(providerId: String): Flow<List<AvailabilityScheduleEntity>> {
        return scheduleDao.getActiveSchedulesByProvider(providerId)
    }
    
    fun getAllSchedulesByProvider(providerId: String): Flow<List<AvailabilityScheduleEntity>> {
        return scheduleDao.getAllSchedulesByProvider(providerId)
    }
    
    fun getSchedulesByDay(providerId: String, dayOfWeek: Int): Flow<List<AvailabilityScheduleEntity>> {
        return scheduleDao.getSchedulesByDay(providerId, dayOfWeek)
    }
    
    suspend fun getScheduleById(scheduleId: String): AvailabilityScheduleEntity? {
        return scheduleDao.getScheduleById(scheduleId)
    }
    
    suspend fun countActiveSchedules(providerId: String): Int {
        return scheduleDao.countActiveSchedules(providerId)
    }
    
    suspend fun updateScheduleStatus(scheduleId: String, isActive: Boolean) {
        scheduleDao.updateScheduleStatus(scheduleId, isActive)
    }
}

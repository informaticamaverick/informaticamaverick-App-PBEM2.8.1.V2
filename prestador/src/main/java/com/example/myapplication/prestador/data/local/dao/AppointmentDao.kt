package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la tabla de appintments (citas)
 * define todas las operaciones CRUD para gestionar citas
 */
@Dao
interface AppointmentDao {
    /**
     * INSETAR una nueva cita
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    /**
     * INSERTAR multiples citas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)

    /**
     * ACTUALIZAR una cita existente
     */
    @Update
    suspend fun updateAppontment(appointment: AppointmentEntity)

    /**
     * ELIMINAR una cita
     */
    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    /**
     * ELIMINNAR cita por ID
     */
    @Query("DELETE FROM appointments WHERE id = :appointmentId")
    suspend fun deleteAppointmentById(appointmentId: String)

    /**OBTENER todas las citas del prestador
     * ordenadas por fecha y hora
     */
    @Query("SELECT * FROM appointments WHERE providerId = :providerId ORDER BY date ASC, time ASC")
    fun getAllAppointments(providerId: String): Flow<List<AppointmentEntity>>

    /**
     * OBTENER vita por ID
     */
    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: String): AppointmentEntity?

    @Query(
        "SELECT * FROM appointments " +
            "WHERE providerId = :providerId AND clientId = :clientId AND date = :date AND time = :time " +
            "ORDER BY updatedAt DESC LIMIT 1"
    )
    suspend fun findAppointmentBySlot(
        providerId: String,
        clientId: String,
        date: String,
        time: String
    ): AppointmentEntity?

    /**
     * OBTENER cita por ID (observando cambios)
     */
    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    fun getAppointmentByIdFlow(appointmentId: String): Flow<AppointmentEntity?>

    /**
     * OBTENER citas por Estado
     */
    @Query("SELECT * FROM appointments WHERE providerId = :providerId AND status = :status ORDER BY date ASC, time ASC")
    fun getAppointmentsByStatus(providerId: String, status: String): Flow<List<AppointmentEntity>>
    
    /**
     * OBTENER TODAS las citas (sin filtro de provider)
     */
    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    fun getAllAppointmentsFlow(): Flow<List<AppointmentEntity>>

    /**
     * OBTENER citas de un cliente especifico
     */
    @Query("SELECT * FROM appointments WHERE clientId= :clientId ORDER BY date ASC, time ASC")
    fun getAppointmentsByClient(clientId: String): Flow<List<AppointmentEntity>>

    /**
     * OBTENER citas de una fecha especifica
     */
    @Query("SELECT * FROM appointments WHERE providerId = :providerId AND date = :date ORDER BY time ASC")
    fun getAppointmentsByDate(providerId: String, date: String): Flow<List<AppointmentEntity>>

    /**
     * OBTENER citas de una fecha especifica (suspend para validación)
     */
    @Query("SELECT * FROM appointments WHERE providerId = :providerId AND date = :date ORDER BY time ASC")
    suspend fun getByProviderAndDateSuspend(providerId: String, date: String): List<AppointmentEntity>
    
    /**
     * OBTENER citas pendientes
     */
    @Query("SELECT * FROM appointments WHERE providerId= :providerId AND (status = 'pending' OR status = 'confirmed') ORDER BY date ASC, time ASC")
    fun getpendingAppointments(providerId: String): Flow<List<AppointmentEntity>>

    /**
     * ACTUALIZAR el estado de una cita
     */
    @Query("UPDATE appointments SET status = :status, updatedAt = :updatedAt WHERE id = :appointmentId")
    suspend fun updateAppointmentStatus(appointmentId: String, status: String, updatedAt: Long)

    /**
     * CONTAR citas por estados
     */
    @Query("SELECT COUNT(*) FROM appointments WHERE providerId = :providerId AND status = :status")
    suspend fun countAppointmentsByStatus(providerId: String, status: String): Int

    /**
     * ELIMINAR citas completadas o canceladas antiguas
     * Útil para limpiar la BD perdiodicamente
     */
    @Query("DELETE FROM appointments WHERE (status = 'completed' OR status = 'cancelled') AND createdAt < :beforeTimestamp")
    suspend fun deleteOldAppointments(beforeTimestamp: Long)

    /**
     * OBTENER todas las citas filtradas por serviceType
     */
    @Query("SELECT * FROM appointments WHERE serviceType = :serviceType ORDER BY date ASC, time ASC")
    fun getAllintmentsByServiceType(serviceType: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE providerId = :providerId AND status = 'pending'")
    suspend fun getPendingAppointmentsSync(providerId: String): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE providerId = :providerId")
    suspend fun getAllAppointmentsSync(providerId: String): List<AppointmentEntity>

}

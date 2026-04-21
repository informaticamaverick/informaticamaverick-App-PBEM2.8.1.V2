package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoDao {
    
    @Query("SELECT * FROM presupuestos WHERE estado != '__catalog__' ORDER BY fecha DESC")
    fun getAllPresupuestos(): Flow<List<PresupuestoEntity>>
    
    @Query("SELECT * FROM presupuestos WHERE estado = :estado ORDER BY fecha DESC")
    fun getPresupuestosByEstado(estado: String): Flow<List<PresupuestoEntity>>
    
    @Query("SELECT * FROM presupuestos WHERE id = :presupuestoId")
    suspend fun getPresupuestoById(presupuestoId: String): PresupuestoEntity?

    @Query("SELECT * FROM presupuestos WHERE id = :presupuestoId")
    fun getPresupuestoByIdAsFlow(presupuestoId: String): Flow<PresupuestoEntity?>
    
    @Query("SELECT * FROM presupuestos WHERE clienteId = :clienteId ORDER BY fecha DESC")
    fun getPresupuestosByCliente(clienteId: String): Flow<List<PresupuestoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuesto(presupuesto: PresupuestoEntity)
    
    @Update
    suspend fun updatePresupuesto(presupuesto: PresupuestoEntity)
    
    @Delete
    suspend fun deletePresupuesto(presupuesto: PresupuestoEntity)
    
    @Query("DELETE FROM presupuestos WHERE id = :presupuestoId")
    suspend fun deletePresupuestoById(presupuestoId: String)
    
    @Query("SELECT COUNT(*) FROM presupuestos WHERE estado = :estado")
    suspend fun countByEstado(estado: String): Int

    @Query("SELECT * FROM presupuestos WHERE appointmentId = :appointmentId ORDER BY fecha DESC")
    fun getPresupuestosByAppointment(appointmentId: String): Flow<List<PresupuestoEntity>>

    @Query("SELECT * FROM presupuestos WHERE appointmentId = :appointmentId ORDER BY fecha DESC LIMIT 1")
    suspend fun getLatestPresupuestoForAppointment(appointmentId: String): PresupuestoEntity?

    @Query("SELECT SUM(total) FROM presupuestos WHERE estado = 'Aceptado' AND prestadorId = :prestadorId")
    suspend fun getTotalGanancias(prestadorId: String): Double?

    @Query("SELECT SUM(total) FROM presupuestos WHERE estado = 'Aceptado' AND prestadorId = :prestadorId AND fecha >= :fromDate")
    suspend fun getGananciasDesde(prestadorId: String, fromDate: String): Double?

    @Query("UPDATE presupuestos SET estado = :estado, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEstado(id: String, estado: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE presupuestos SET firestoreId = :firestoreId, syncedAt = :syncedAt WHERE id = :id")
    suspend fun updateSyncInfo(id: String, firestoreId: String, syncedAt: Long)
}

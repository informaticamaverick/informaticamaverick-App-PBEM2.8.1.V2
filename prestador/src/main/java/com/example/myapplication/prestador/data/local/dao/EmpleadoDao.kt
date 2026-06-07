package com.example.myapplication.prestador.data.local.dao

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en el modelo Provider del módulo :core.
 * Los empleados se gestionan dentro de la jerarquía de BranchProvider -> EmployeeProvider.
 */

/*
import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpleadoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(empleado: EmpleadoEntity)
    
    @Update
    suspend fun update(empleado: EmpleadoEntity)
    
    @Delete
    suspend fun delete(empleado: EmpleadoEntity)
    
    @Query("UPDATE empleados SET activo = 0, updatedAt = :timestamp WHERE id = :empleadoId")
    suspend fun markAsInactive(empleadoId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM empleados WHERE prestadorId = :prestadorId AND activo = 1 ORDER BY nombre ASC")
    fun getEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>>
    
    @Query("SELECT * FROM empleados WHERE prestadorId = :prestadorId ORDER BY activo DESC, nombre ASC")
    fun getAllEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>>
    
    @Query("SELECT * FROM empleados WHERE id = :empleadoId")
    suspend fun getEmpleadoById(empleadoId: String): EmpleadoEntity?
    
    @Query("SELECT COUNT(*) FROM empleados WHERE prestadorId = :prestadorId AND activo = 1")
    suspend fun countActiveEmpleados(prestadorId: String): Int
    
    @Query("SELECT COUNT(*) FROM empleados WHERE prestadorId = :prestadorId AND dni = :dni AND activo = 1 AND id != :excludeId")
    suspend fun existsEmpleadoWithDni(prestadorId: String, dni: String, excludeId: String = ""): Int
}
*/

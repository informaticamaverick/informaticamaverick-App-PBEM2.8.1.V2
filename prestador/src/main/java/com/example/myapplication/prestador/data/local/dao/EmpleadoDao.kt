package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de Empleados
 */
@Dao
interface EmpleadoDao {
    
    /**
     * Insertar un nuevo empleado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(empleado: EmpleadoEntity)
    
    /**
     * Actualizar empleado existente
     */
    @Update
    suspend fun update(empleado: EmpleadoEntity)
    
    /**
     * Eliminar empleado (físicamente)
     */
    @Delete
    suspend fun delete(empleado: EmpleadoEntity)
    
    /**
     * Soft delete: marcar empleado como inactivo
     */
    @Query("UPDATE empleados SET activo = 0, updatedAt = :timestamp WHERE id = :empleadoId")
    suspend fun markAsInactive(empleadoId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Obtener todos los empleados ACTIVOS de un prestador (Flow para observar cambios)
     */
    @Query("SELECT * FROM empleados WHERE prestadorId = :prestadorId AND activo = 1 ORDER BY nombre ASC")
    fun getEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>>
    
    /**
     * Obtener todos los empleados de un prestador (incluyendo inactivos)
     */
    @Query("SELECT * FROM empleados WHERE prestadorId = :prestadorId ORDER BY activo DESC, nombre ASC")
    fun getAllEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>>
    
    /**
     * Obtener empleado por ID (una vez, no Flow)
     */
    @Query("SELECT * FROM empleados WHERE id = :empleadoId")
    suspend fun getEmpleadoById(empleadoId: String): EmpleadoEntity?
    
    /**
     * Contar empleados activos de un prestador
     */
    @Query("SELECT COUNT(*) FROM empleados WHERE prestadorId = :prestadorId AND activo = 1")
    suspend fun countActiveEmpleados(prestadorId: String): Int
    
    /**
     * Verificar si existe un empleado con ese DNI para el prestador
     */
    @Query("SELECT COUNT(*) FROM empleados WHERE prestadorId = :prestadorId AND dni = :dni AND activo = 1 AND id != :excludeId")
    suspend fun existsEmpleadoWithDni(prestadorId: String, dni: String, excludeId: String = ""): Int
}

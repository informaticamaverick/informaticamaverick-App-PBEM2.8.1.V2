package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.EmpleadoDao
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * REPOSITORY: Empleados
 * Gestiona operaciones CRUD de empleados del prestador
 */
@Singleton
class EmpleadoRepository @Inject constructor(
    private val empleadoDao: EmpleadoDao
) {
    
    /**
     * Obtener empleados activos del prestador (observable con Flow)
     */
    fun getEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>> {
        return empleadoDao.getEmpleadosByPrestadorId(prestadorId)
    }
    
    /**
     * Obtener todos los empleados (activos e inactivos)
     */
    fun getAllEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>> {
        return empleadoDao.getAllEmpleadosByPrestadorId(prestadorId)
    }
    
    /**
     * Obtener empleado por ID
     */
    suspend fun getEmpleadoById(empleadoId: String): EmpleadoEntity? {
        return empleadoDao.getEmpleadoById(empleadoId)
    }
    
    /**
     * Agregar nuevo empleado
     */
    suspend fun addEmpleado(
        prestadorId: String,
        nombre: String,
        apellido: String,
        dni: String
    ): Result<EmpleadoEntity> {
        return try {
            // Validar DNI
            if (dni.isBlank() || dni.length < 7) {
                return Result.failure(Exception("DNI inválido"))
            }
            
            // Verificar si ya existe un empleado con ese DNI
            val exists = empleadoDao.existsEmpleadoWithDni(prestadorId, dni)
            if (exists > 0) {
                return Result.failure(Exception("Ya existe un empleado con ese DNI"))
            }
            
            val empleado = EmpleadoEntity(
                id = UUID.randomUUID().toString(),
                prestadorId = prestadorId,
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                dni = dni.trim(),
                activo = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            empleadoDao.insert(empleado)
            Result.success(empleado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar empleado existente
     */
    suspend fun updateEmpleado(
        empleadoId: String,
        nombre: String,
        apellido: String,
        dni: String
    ): Result<EmpleadoEntity> {
        return try {
            val empleado = empleadoDao.getEmpleadoById(empleadoId)
                ?: return Result.failure(Exception("Empleado no encontrado"))
            
            // Validar DNI
            if (dni.isBlank() || dni.length < 7) {
                return Result.failure(Exception("DNI inválido"))
            }
            
            // Verificar si ya existe otro empleado con ese DNI
            val exists = empleadoDao.existsEmpleadoWithDni(
                empleado.prestadorId, 
                dni, 
                excludeId = empleadoId
            )
            if (exists > 0) {
                return Result.failure(Exception("Ya existe otro empleado con ese DNI"))
            }
            
            val updatedEmpleado = empleado.copy(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                dni = dni.trim(),
                updatedAt = System.currentTimeMillis()
            )
            
            empleadoDao.update(updatedEmpleado)
            Result.success(updatedEmpleado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar empleado (soft delete)
     */
    suspend fun deleteEmpleado(empleadoId: String): Result<Unit> {
        return try {
            empleadoDao.markAsInactive(empleadoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Contar empleados activos
     */
    suspend fun countActiveEmpleados(prestadorId: String): Int {
        return empleadoDao.countActiveEmpleados(prestadorId)
    }
}

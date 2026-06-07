package com.example.myapplication.prestador.data.repository

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en el repositorio ProviderRepository 
 * del módulo :core. Los empleados ahora se gestionan dentro de la jerarquía de Provider.
 */

/*
import com.example.myapplication.prestador.data.local.dao.EmpleadoDao
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmpleadoRepository @Inject constructor(
    private val empleadoDao: EmpleadoDao
) {
    fun getEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>> {
        return empleadoDao.getEmpleadosByPrestadorId(prestadorId)
    }
    
    fun getAllEmpleadosByPrestadorId(prestadorId: String): Flow<List<EmpleadoEntity>> {
        return empleadoDao.getAllEmpleadosByPrestadorId(prestadorId)
    }
    
    suspend fun getEmpleadoById(empleadoId: String): EmpleadoEntity? {
        return empleadoDao.getEmpleadoById(empleadoId)
    }
    
    suspend fun addEmpleado(
        prestadorId: String,
        nombre: String,
        apellido: String,
        dni: String
    ): Result<EmpleadoEntity> {
        return try {
            if (dni.isBlank() || dni.length < 7) {
                return Result.failure(Exception("DNI inválido"))
            }
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
    
    suspend fun updateEmpleado(
        empleadoId: String,
        nombre: String,
        apellido: String,
        dni: String
    ): Result<EmpleadoEntity> {
        return try {
            val empleado = empleadoDao.getEmpleadoById(empleadoId)
                ?: return Result.failure(Exception("Empleado no encontrado"))
            if (dni.isBlank() || dni.length < 7) {
                return Result.failure(Exception("DNI inválido"))
            }
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
    
    suspend fun deleteEmpleado(empleadoId: String): Result<Unit> {
        return try {
            empleadoDao.markAsInactive(empleadoId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun countActiveEmpleados(prestadorId: String): Int {
        return empleadoDao.countActiveEmpleados(prestadorId)
    }
}
*/

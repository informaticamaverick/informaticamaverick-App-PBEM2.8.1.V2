package com.example.myapplication.prestador.data.repository

import com.example.myapplication.prestador.data.local.dao.RentalSpaceDao
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gestionar espacios de alquiler
 */
@Singleton
class RentalSpaceRepository @Inject constructor(
    private val rentalSpaceDao: RentalSpaceDao
) {
    
    /**
     * Obtener todos los espacios de un prestador
     */
    fun getAllSpacesByProviderId(providerId: String): Flow<List<RentalSpaceEntity>> {
        return rentalSpaceDao.getAllByProviderId(providerId)
    }
    
    /**
     * Obtener solo espacios activos
     */
    fun getActiveSpacesByProviderId(providerId: String): Flow<List<RentalSpaceEntity>> {
        return rentalSpaceDao.getActiveByProviderId(providerId)
    }
    
    /**
     * Obtener un espacio por ID
     */
    suspend fun getSpaceById(id: String): RentalSpaceEntity? {
        return rentalSpaceDao.getById(id)
    }
    
    /**
     * Insertar un nuevo espacio
     */
    suspend fun insertSpace(space: RentalSpaceEntity) {
        rentalSpaceDao.insert(space)
    }
    
    /**
     * Actualizar un espacio
     */
    suspend fun updateSpace(space: RentalSpaceEntity) {
        rentalSpaceDao.update(space.copy(updatedAt = System.currentTimeMillis()))
    }
    
    /**
     * Eliminar un espacio
     */
    suspend fun deleteSpace(space: RentalSpaceEntity) {
        rentalSpaceDao.delete(space)
    }
    
    /**
     * Contar espacios de un prestador
     */
    fun countSpacesByProviderId(providerId: String): Flow<Int> {
        return rentalSpaceDao.countByProviderId(providerId)
    }
    
    /**
     * Activar/desactivar un espacio
     */
    suspend fun updateSpaceActiveStatus(id: String, isActive: Boolean) {
        rentalSpaceDao.updateActiveStatus(id, isActive)
    }
}

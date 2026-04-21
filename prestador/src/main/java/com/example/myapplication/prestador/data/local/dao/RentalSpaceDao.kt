package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para gestionar espacios de alquiler
 */
@Dao
interface RentalSpaceDao {
    
    /**
     * Obtener todos los espacios de alquiler de un prestador
     */
    @Query("SELECT * FROM rental_spaces WHERE providerId = :providerId ORDER BY name ASC")
    fun getAllByProviderId(providerId: String): Flow<List<RentalSpaceEntity>>
    
    /**
     * Obtener solo espacios activos de un prestador
     */
    @Query("SELECT * FROM rental_spaces WHERE providerId = :providerId AND isActive = 1 ORDER BY name ASC")
    fun getActiveByProviderId(providerId: String): Flow<List<RentalSpaceEntity>>
    
    /**
     * Obtener un espacio por ID
     */
    @Query("SELECT * FROM rental_spaces WHERE id = :id")
    suspend fun getById(id: String): RentalSpaceEntity?
    
    /**
     * Insertar un nuevo espacio
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(space: RentalSpaceEntity)
    
    /**
     * Actualizar un espacio existente
     */
    @Update
    suspend fun update(space: RentalSpaceEntity)
    
    /**
     * Eliminar un espacio
     */
    @Delete
    suspend fun delete(space: RentalSpaceEntity)
    
    /**
     * Eliminar todos los espacios de un prestador
     */
    @Query("DELETE FROM rental_spaces WHERE providerId = :providerId")
    suspend fun deleteAllByProviderId(providerId: String)
    
    /**
     * Contar espacios de un prestador
     */
    @Query("SELECT COUNT(*) FROM rental_spaces WHERE providerId = :providerId")
    fun countByProviderId(providerId: String): Flow<Int>
    
    /**
     * Activar/desactivar un espacio
     */
    @Query("UPDATE rental_spaces SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())
}

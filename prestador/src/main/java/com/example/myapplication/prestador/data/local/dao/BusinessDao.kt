package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la tabl ade Business (negocio)
 */

@Dao
interface BusinessDao {
    /**
     * INSERTAR un nuevo negocio
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    /**
     * INSERTAR múltiples negocios
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessEntity>)

    /**
     * ACTUALIZAR negocio existente
     */

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    /**
     * ELIMINAR negocio
     */
    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)

    /**
     * OBTENER negocio por ID del prestador
     */
    @Query("SELECT * FROM business WHERE providerId = :providerId LIMIT 1")
    fun getBusinessByProviderId(providerId: String): Flow<BusinessEntity?>

    /**
     * OBTENER negocio por ID del prestador (una Vez)
     */
    @Query("SELECT * FROM business WHERE providerId = :providerId LIMIT 1")
    suspend fun getBusinessByProviderIdOnce(providerId: String): BusinessEntity?

    /**
     * OBTENER negocio por ID
     */
    @Query("SELECT * FROM business WHERE id = :businessId")
    fun getBusinessById(businessId: String): Flow<BusinessEntity?>

    /**
     * OBTENER todos los negocios
     */
    @Query("SELECT * FROM business ORDER BY nombreNegocio ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    /**
     * OBTENER negocios por ID del prestador
     */
    @Query("SELECT * FROM business WHERE providerId = :providerId")
    fun getBusinessesByProvider(providerId: String): Flow<List<BusinessEntity>>

    /**
     * BUSCAR negocios por nombre
     */
    @Query("SELECT * FROM business WHERE nombreNegocio LIKE :name")
    fun searchBusinessesByName(name: String): Flow<List<BusinessEntity>>

    /**
     * OBTENER negocio por CUIT
     */
    @Query("SELECT * FROM business WHERE cuitNegocio = :cuit")
    fun getBusinessByCuit(cuit: String): Flow<BusinessEntity?>

    /**
     * VERIFICAR si existe un negocio
     */
    @Query("SELECT EXISTS(SELECT 1 FROM business WHERE id = :businessId)")
    suspend fun businessExists(businessId: String): Boolean

    /**
     * CONTAR total de negocios
     */
    @Query("SELECT COUNT(*) FROM business")
    suspend fun countBusinesses(): Int

    /**
     * ACTUALIZAR timestamp
     */
    @Query("UPDATE business SET updatedAt = :timestamp WHERE id = :businessId")
    suspend fun updateBusinessTimestamp(businessId: String, timestamp: Long)

    /**
     * ELIMINAR negocio por ID
     */
    @Query("DELETE FROM business WHERE id = :businessId")
    suspend fun deleteBusinessById(businessId: String)

    /**
     * ELIMINAR todos los negocios
     */
    @Query("DELETE FROM business")
    suspend fun deleteAllBusinesses()

    /**
     * ELIMINAR negocio por ID del prestador
     */
    @Query("DELETE FROM business WHERE providerId = :providerId")
    suspend fun deleteBusinessByProviderId(providerId: String)
}
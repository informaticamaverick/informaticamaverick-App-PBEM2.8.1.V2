package com.example.myapplication.prestador.data.local.dao

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en los modelos del módulo :core.
 * Se recomienda usar el repositorio central com.example.myapplication.core.data.repository.ProviderRepository
 * y gestionar la persistencia a través de com.example.myapplication.core.data.local.dao.ProviderDao.
 */

/*
import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: BusinessEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinesses(businesses: List<BusinessEntity>)

    @Update
    suspend fun updateBusiness(business: BusinessEntity)

    @Delete
    suspend fun deleteBusiness(business: BusinessEntity)

    @Query("SELECT * FROM business WHERE providerId = :providerId LIMIT 1")
    fun getBusinessByProviderId(providerId: String): Flow<BusinessEntity?>

    @Query("SELECT * FROM business WHERE providerId = :providerId LIMIT 1")
    suspend fun getBusinessByProviderIdOnce(providerId: String): BusinessEntity?

    @Query("SELECT * FROM business WHERE id = :businessId")
    fun getBusinessById(businessId: String): Flow<BusinessEntity?>

    @Query("SELECT * FROM business ORDER BY nombreNegocio ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM business WHERE providerId = :providerId")
    fun getBusinessesByProvider(providerId: String): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM business WHERE nombreNegocio LIKE :name")
    fun searchBusinessesByName(name: String): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM business WHERE cuitNegocio = :cuit")
    fun getBusinessByCuit(cuit: String): Flow<BusinessEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM business WHERE id = :businessId)")
    suspend fun businessExists(businessId: String): Boolean

    @Query("SELECT COUNT(*) FROM business")
    suspend fun countBusinesses(): Int

    @Query("UPDATE business SET updatedAt = :timestamp WHERE id = :businessId")
    suspend fun updateBusinessTimestamp(businessId: String, timestamp: Long)

    @Query("DELETE FROM business WHERE id = :businessId")
    suspend fun deleteBusinessById(businessId: String)

    @Query("DELETE FROM business")
    suspend fun deleteAllBusinesses()

    @Query("DELETE FROM business WHERE providerId = :providerId")
    suspend fun deleteBusinessByProviderId(providerId: String)
}
*/

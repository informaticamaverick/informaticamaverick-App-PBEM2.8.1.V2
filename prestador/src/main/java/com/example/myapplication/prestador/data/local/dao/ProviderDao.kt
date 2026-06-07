/**
package com.example.myapplication.prestador.data.local.dao

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en los DAOs del módulo :core.
 * Se recomienda usar com.example.myapplication.core.data.local.dao.ProviderDao
 * para todas las operaciones de persistencia del prestador.
 */

/*
import androidx.room.*
import com.example.myapplication.core.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Delete
    suspend fun deleteProvider(provider: ProviderEntity)
    
    @Query("SELECT * FROM provider_profile WHERE id = :id")
    fun getProviderById(id: String): Flow<ProviderEntity?>

    @Query("SELECT * FROM provider_profile WHERE id = :id")
    suspend fun getProviderByIdOnce(id: String): ProviderEntity?

    @Query("SELECT * FROM provider_profile ORDER BY createdAt DESC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM provider_profile WHERE id = :id)")
    suspend fun providerExists(id: String): Boolean

    @Query("UPDATE provider_profile SET photoUrl = :imageUrl WHERE id = :id")
    suspend fun updateProviderImage(id: String, imageUrl: String)

    @Query("UPDATE provider_profile SET rating = :rating WHERE id = :id")
    suspend fun updateProviderRating(id: String, rating: Float)

    @Query("DELETE FROM provider_profile WHERE id = :id")
    suspend fun deleteProviderById(id: String)

    @Query("SELECT * FROM provider_profile WHERE name LIKE :query ORDER BY name ASC")
    fun searchProviders(query: String): Flow<List<ProviderEntity>>
}
*/
*/

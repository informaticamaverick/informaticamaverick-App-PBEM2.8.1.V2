package com.example.myapplication.prestador.data.local.dao

import androidx.room.*
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) para la tabla de Providers
 */

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
    
    @Query("SELECT * FROM providers WHERE id = :id")
    fun getProviderById(id: String): Flow<ProviderEntity?>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderByIdOnce(id: String): ProviderEntity?

    @Query("SELECT * FROM providers ORDER BY createdAt DESC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM providers WHERE id = :id)")
    suspend fun providerExists(id: String): Boolean

    @Query("UPDATE providers SET photoUrl = :imageUrl WHERE id = :id")
    suspend fun updateProviderImage(id: String, imageUrl: String)

    @Query("UPDATE providers SET rating = :rating WHERE id = :id")
    suspend fun updateProviderRating(id: String, rating: Float)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProviderById(id: String)

    @Query("SELECT * FROM providers WHERE name LIKE :query ORDER BY name ASC")
    fun searchProviders(query: String): Flow<List<ProviderEntity>>
}

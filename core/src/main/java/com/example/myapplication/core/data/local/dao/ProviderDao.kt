package com.example.myapplication.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.core.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

/**
 * --- DAO PARA PROVEEDORES (COMPARTIDO) ---
 * Gestiona el acceso local a los perfiles de prestadores.
 * Ambas apps lo usan: el Cliente para buscar profesionales y el Prestador 
 * para gestionar su propia información.
 */
@Dao
interface ProviderDao {

    // =========================================================================
    // === SECCIÓN: COMÚN (OPERACIONES BASE) ===
    // =========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Query("DELETE FROM provider_profile WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM provider_profile")
    suspend fun clearAllProviders()

    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    suspend fun getProviderById(providerId: String): ProviderEntity?

    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    fun getProviderFlowById(providerId: String): Flow<ProviderEntity?>

    // =========================================================================
    // === SECCIÓN: APP CLIENTE (LECTURA / BÚSQUEDA) ===
    // =========================================================================

    /**
     * Obtiene todos los prestadores guardados localmente.
     */
    @Query("SELECT * FROM provider_profile")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    /**
     * Obtiene solo los prestadores marcados como favoritos por el usuario.
     */
    @Query("SELECT * FROM provider_profile WHERE isFavorite = 1")
    fun getFavoriteProviders(): Flow<List<ProviderEntity>>

    /**
     * Búsqueda por categoría. 
     */
    @Query("SELECT * FROM provider_profile WHERE categories LIKE '%' || :category || '%'")
    suspend fun getProvidersByCategory(category: String): List<ProviderEntity>

    /**
     * Filtra prestadores por Código Postal y Categoría (Estrategia Costo Cero).
     */
    @Query("SELECT * FROM provider_profile WHERE categories LIKE '%' || :category || '%' AND (addresses LIKE '%' || :zipCode || '%' OR address LIKE '%' || :zipCode || '%')")
    fun getProvidersByFilter(zipCode: String, category: String): Flow<List<ProviderEntity>>

    /**
     * Actualiza si un prestador es favorito o no.
     */
    @Query("UPDATE provider_profile SET isFavorite = :isFavorite WHERE id = :providerId")
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean)

    // =========================================================================
    // === SECCIÓN: APP PRESTADOR (ESCRITURA / PERFIL PROPIO) ===
    // =========================================================================

    @Query("UPDATE provider_profile SET photoUrl = :imageUrl WHERE id = :id")
    suspend fun updateImage(id: String, imageUrl: String)
}

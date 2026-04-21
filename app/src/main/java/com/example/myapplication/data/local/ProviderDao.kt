package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * --- DATA ACCESS OBJECT (DAO) PARA PROVEEDORES ---
 * [ACTUALIZADO] Soporte para flujos individuales por ID y búsqueda dentro de la lista de categorías.
 */
@Dao
interface ProviderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Query("SELECT * FROM provider_profile")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM provider_profile WHERE isFavorite = 1")
    fun getFavoriteProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    suspend fun getProviderById(providerId: String): ProviderEntity?

    /**
     * Búsqueda por categoría.
     * Como "categories" se guarda como una lista serializada, usamos LIKE para búsqueda parcial.
     */
    @Query("SELECT * FROM provider_profile WHERE categories LIKE '%' || :category || '%'")
    suspend fun getProvidersByCategory(category: String): List<ProviderEntity>

    /**
     * Obtiene un flujo de datos de un proveedor específico para observar cambios en tiempo real.
     */
    @Query("SELECT * FROM provider_profile WHERE id = :providerId")
    fun getProviderFlowById(providerId: String): Flow<ProviderEntity?>

    @Query("UPDATE provider_profile SET isFavorite = :isFavorite WHERE id = :providerId")
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean)

    /**
     * Obtiene prestadores filtrados por código postal y categoría.
     * [MEJORADO] Busca tanto en la lista serializada de direcciones como en el objeto de dirección principal.
     * Esto asegura que la estrategia de ahorro de costos detecte los datos locales correctamente.
     */
    @Query("SELECT * FROM provider_profile WHERE categories LIKE '%' || :category || '%' AND (addresses LIKE '%' || :zipCode || '%' OR address LIKE '%' || :zipCode || '%')")
    fun getProvidersByFilter(zipCode: String, category: String): Flow<List<ProviderEntity>>

    @Query("DELETE FROM provider_profile")
    suspend fun clearAllProviders()
}

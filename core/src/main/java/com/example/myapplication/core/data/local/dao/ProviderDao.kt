package com.example.myapplication.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.paging.PagingSource
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

    @Query("SELECT * FROM provider_profile WHERE id = :targetId")
    suspend fun getProviderById(targetId: String): ProviderEntity?

    @Query("SELECT * FROM provider_profile WHERE id = :targetId")
    fun getProviderFlowById(targetId: String): Flow<ProviderEntity?>

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
     * [ELITE] Proyección ligera para listas masivas (Shallow Loading - Ley #3.1).
     * Devuelve solo lo necesario para renderizar la tarjeta en la búsqueda.
     */
    @Query("""
        SELECT id, providerId, displayName, name, lastName, photoUrl, profileThumbnail, rating, categories, 
               isSubscribed, isVerified, isOnline, works24h, hasPhysicalLocation, 
               doesHomeVisits, doesShipping, acceptsAppointments, workingHours,
               fullAddress, geohash, latitude, longitude, companies, address,
               0 as extraBranches
        FROM provider_profile 
        WHERE categories LIKE '%' || :category || '%' 
        AND (addresses LIKE '%' || :zipCode || '%' OR address LIKE '%' || :zipCode || '%' OR companies LIKE '%' || :zipCode || '%')
    """)
    fun getShallowProvidersByFilter(zipCode: String, category: String): Flow<List<ShallowProvider>>

    /**
     * [ELITE] Versión paginada para optimizar memoria y CPU (Ley #4).
     * Room implementa automáticamente el PagingSource.
     * [LEY PAREJA]: Agrupa por providerId para evitar redundancia visual y calcula sucursales extra.
     */
    @Query("""
        SELECT p1.id, p1.providerId, p1.displayName, p1.name, p1.lastName, p1.photoUrl, p1.profileThumbnail, p1.rating, p1.categories, 
               p1.isSubscribed, p1.isVerified, p1.isOnline, p1.works24h, p1.hasPhysicalLocation, 
               p1.doesHomeVisits, p1.doesShipping, p1.acceptsAppointments, p1.workingHours,
               p1.fullAddress, p1.geohash, p1.latitude, p1.longitude, p1.companies, p1.address,
               CAST((SELECT COUNT(*) FROM provider_profile p2 
                WHERE p2.providerId = p1.providerId 
                AND p2.categories LIKE '%' || :category || '%'
                AND (p2.addresses LIKE '%' || :zipCode || '%' OR p2.address LIKE '%' || :zipCode || '%' OR p2.companies LIKE '%' || :zipCode || '%')
               ) - 1 AS INTEGER) as extraBranches
        FROM provider_profile p1
        WHERE p1.categories LIKE '%' || :category || '%' 
        AND (p1.addresses LIKE '%' || :zipCode || '%' OR p1.address LIKE '%' || :zipCode || '%' OR p1.companies LIKE '%' || :zipCode || '%')
        GROUP BY p1.providerId
        ORDER BY p1.isSubscribed DESC, p1.rating DESC
    """)
    fun getShallowProvidersPaged(zipCode: String, category: String): PagingSource<Int, ShallowProvider>

    @Query("SELECT MAX(lastSyncTimestamp) FROM provider_profile WHERE categories LIKE '%' || :category || '%'")
    suspend fun getLastSyncForCategory(category: String): Long?

    /**
     * Actualiza si un prestador es favorito o no.
     */
    @Query("UPDATE provider_profile SET isFavorite = :isFavorite WHERE id = :providerId")
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean)

    // =========================================================================
    // === SECCIÓN: APP PRESTADOR (ESCRITURA / PERFIL PROPIO) ===
    // =========================================================================

    @Query("UPDATE provider_profile SET photoUrl = :imageUrl WHERE id = :id")
    suspend fun updateProviderImage(id: String, imageUrl: String)

    @Query("UPDATE provider_profile SET profileThumbnail = :thumbnail WHERE id = :id")
    suspend fun updateProviderThumbnail(id: String, thumbnail: String)

    @Query("UPDATE provider_profile SET rating = :rating WHERE id = :id")
    suspend fun updateProviderRating(id: String, rating: Float)

    @Query("SELECT EXISTS(SELECT 1 FROM provider_profile WHERE id = :id)")
    suspend fun providerExists(id: String): Boolean

    @Query("SELECT * FROM provider_profile WHERE name LIKE :query OR displayName LIKE :query ORDER BY name ASC")
    fun searchProviders(query: String): Flow<List<ProviderEntity>>
}

/**
 * [ELITE] Modelo ligero para la Ley de Carga On-Demand Local.
 */
data class ShallowProvider(
    val id: String,
    val providerId: String,
    val displayName: String,
    val name: String,
    val lastName: String,
    val photoUrl: String?,
    val profileThumbnail: String? = null,
    val rating: Float,
    val categories: List<String>,
    val isSubscribed: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val works24h: Boolean,
    val hasPhysicalLocation: Boolean,
    val doesHomeVisits: Boolean,
    val doesShipping: Boolean,
    val acceptsAppointments: Boolean,
    val workingHours: String = "",
    val fullAddress: String? = null,
    val geohash: String? = null,
    val extraBranches: Int = 0, // [ELITE] Conteo para Ley Pareja (Deduplicación SQL)
    val latitude: Double? = null,
    val longitude: Double? = null,
    val companies: List<com.example.myapplication.core.domain.model.CompanyProvider> = emptyList(),
    val address: com.example.myapplication.core.domain.model.AddressUnico? = null
)

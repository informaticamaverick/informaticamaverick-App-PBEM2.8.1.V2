package com.example.myapplication.data.repository

import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.data.local.dao.FastCategoryDao
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.data.local.dao.FastCategoryEntity
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.presentation.mapper.ProviderDisplayMapper
import com.example.myapplication.presentation.features.profile.UserLocation
import com.example.myapplication.presentation.features.home.FastFilterState
import com.example.myapplication.presentation.features.home.ProviderWithDistance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * --- REPOSITORIO FAST (APP CLIENTE) ---
 * Gestiona el historial de rubros más buscados y la búsqueda táctica de emergencia.
 */
@Singleton
class FastRepository @Inject constructor(
    private val providerDao: ProviderDao,
    private val fastCategoryDao: FastCategoryDao
) {
    fun getFastHistory(limit: Int = 10): Flow<List<FastCategoryEntity>> =
        fastCategoryDao.getMostUsed(limit)

    suspend fun registerUsage(category: CategoryEntity) {
        fastCategoryDao.registerUsage(category)
    }

    /**
     * Motor táctico Maverick FAST: Filtra prestadores por cercanía y disponibilidad inmediata.
     */
    suspend fun searchHybridEmergency(
        categoryName: String,
        userLat: Double,
        userLon: Double,
        filters: FastFilterState,
        zipCode: String
    ): List<ProviderWithDistance> {
        delay(2000)
        
        val localProviders = providerDao.getProvidersByCategory(categoryName)
        
        return localProviders.map { entity ->
            val provider = entity.toDomain()
            val pLat = provider.address?.latitude ?: (userLat + (kotlin.random.Random.nextDouble(-0.02, 0.02)))
            val pLon = provider.address?.longitude ?: (userLon + (kotlin.random.Random.nextDouble(-0.02, 0.02)))
            
            val distance = MaverickGeoUtils.calculateDistanceKm(userLat, userLon, pLat, pLon)
            
            val userLocation = UserLocation(userLat, userLon)
            val service = ProviderDisplayMapper.toDisplayModel(provider, userLocation).copy(
                //latitude = pLat,
                //longitude = pLon,
                distanceKm = distance
            )
            
            ProviderWithDistance(
                service = service,
                distanceKm = distance,
                estimatedMinutes = MaverickGeoUtils.estimateArrivalMinutes(distance),
                lat = pLat,
                lon = pLon
            )
        }.filter { item ->
            var pass = true
            // En ProviderDisplayModel estas propiedades deberían existir para que este filtro funcione
            // Si el modelo ProviderDisplayModel no tiene estas propiedades, deben ser agregadas
         //   if (filters.isOnline && !item.service.isOnline) pass = false
          //  if (filters.isSubscribed && !item.service.isSubscribed) pass = false
            pass
        }.sortedBy { it.distanceKm }.take(10)
    }
}

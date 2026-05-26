package com.example.myapplication.data.repository

import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.data.local.dao.FastCategoryDao
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.data.local.entity.FastCategoryEntity
import com.example.myapplication.data.model.ProviderDisplayModel
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
            
            val distance = calcularDistanciaKm(userLat, userLon, pLat, pLon)
            
            val service = ProviderDisplayModel(
                id = provider.uid,
                title = provider.displayName,
                photoUrl = provider.photoUrl ?: "",
                rating = provider.rating.toDouble(),
                isVerified = provider.isVerified,
                isOnline = provider.isOnline,
                isSubscribed = provider.isSubscribed,
                type = if (provider.hasCompanyProfile) com.example.myapplication.data.model.ProviderType.COMPANY else com.example.myapplication.data.model.ProviderType.INDIVIDUAL,
                categories = provider.categories,
                latitude = pLat,
                longitude = pLon,
                distanceKm = distance
            )
            
            ProviderWithDistance(
                service = service,
                distanceKm = distance,
                estimatedMinutes = (distance * 5).toInt().coerceAtLeast(3),
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

    private fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.ProviderEntity
import com.example.myapplication.data.local.FastCategoryDao
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.ProviderType
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.data.repository.util.ProviderMapper
import com.example.myapplication.presentation.client.ProviderWithDistance
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class FastRepository @Inject constructor(
    private val providerDao: ProviderDao,
    private val fastCategoryDao: FastCategoryDao,
    private val firestore: FirebaseFirestore
) {
    /**
     * Obtiene el historial de categorías más usadas o recientes.
     */
    fun getFastHistory(limit: Int = 10) = fastCategoryDao.getMostUsed(limit)

    /**
     * Registra el uso de una categoría para persistencia.
     */
    suspend fun registerUsage(category: com.example.myapplication.data.local.CategoryEntity) {
        fastCategoryDao.registerUsage(category)
    }

    /**
     * Búsqueda Híbrida de Emergencia.
     * 1. Busca en Room (Locales/Suscritos con ubicación fija).
     * 2. Busca en Firebase (Online/24hs en tiempo real).
     */
    suspend fun searchHybridEmergency(
        category: String,
        userLat: Double,
        userLon: Double,
        filters: FastFilterState,
        zipCode: String = ""
    ): List<ProviderWithDistance> {
        val results = mutableListOf<ProviderWithDistance>()

        // --- 1. BÚSQUEDA LOCAL (ROOM) ---
        val localEntities = providerDao.getProvidersByCategory(category)
        localEntities.forEach { entity ->
            val provider = entity.toDomain()
            
            // Filtros básicos de Room + CP si aplica
            if (filters.isSubscribed && !provider.isSubscribed) return@forEach
            if (filters.isLocal && !provider.hasPhysicalLocation) return@forEach
            if (filters.isOnline && !provider.isOnline) return@forEach
            if (filters.is24h && !provider.works24h) return@forEach
            
            // Si el CP no coincide y es un local fijo, podríamos filtrar, 
            // pero para emergencias priorizamos distancia real.
            
            val unified = transformToUnified(provider)
            val distance = calculateDistanceKm(userLat, userLon, entity.address?.latitude ?: 0.0, entity.address?.longitude ?: 0.0)
            
            results.add(ProviderWithDistance(
                service = unified,
                distanceKm = distance,
                estimatedMinutes = (distance * 10).toInt().coerceIn(5, 60),
                lat = entity.address?.latitude ?: 0.0,
                lon = entity.address?.longitude ?: 0.0
            ))
        }

        // --- 2. BÚSQUEDA REMOTA (FIREBASE) ---
        // Nota: Implementación simplificada. En producción usar Geofire o índices compuestos.
        try {
            var query = firestore.collection("providers")
                .whereArrayContains("categories", category)
            
            if (filters.isOnline) query = query.whereEqualTo("isOnline", true)
            if (filters.is24h) query = query.whereEqualTo("works24h", true)
            if (filters.isSubscribed) query = query.whereEqualTo("isSubscribed", true)
            // Filtro opcional por CP en Firebase si no hay GPS preciso
            // if (zipCode.isNotEmpty()) query = query.whereEqualTo("address.postalCode", zipCode)

            val snapshot = query.get().await()
            val remoteEntities = mutableListOf<ProviderEntity>()
            
            snapshot.documents.forEach { doc ->
                // 🔥 [ZERO COST SYNC] Convertimos y guardamos en Room 🔥
                val entity = ProviderMapper.fromFirestore(doc)
                if (entity != null) {
                    remoteEntities.add(entity)
                    
                    val p = entity.toDomain()
                    if (results.any { it.service.id == p.uid }) return@forEach // Evitar duplicados

                    val distance = calculateDistanceKm(userLat, userLon, p.address?.latitude ?: 0.0, p.address?.longitude ?: 0.0)
                    
                    // Filtro de radio (ej: 20km para emergencias)
                    if (distance > 20.0) return@forEach

                    results.add(ProviderWithDistance(
                        service = transformToUnified(p),
                        distanceKm = distance,
                        estimatedMinutes = (distance * 8).toInt().coerceIn(5, 45),
                        lat = p.address?.latitude ?: 0.0,
                        lon = p.address?.longitude ?: 0.0
                    ))
                }
            }
            
            if (remoteEntities.isNotEmpty()) {
                providerDao.insertAll(remoteEntities)
                Log.d("FastRepository", "✅ ${remoteEntities.size} prestadores sincronizados en Room desde FAST")
            }
        } catch (e: Exception) {
            // Log error
        }

        return results.sortedBy { it.distanceKm }
    }

    private fun transformToUnified(provider: Provider): ServiceDisplayModel {
        val isCompany = provider.companies.isNotEmpty()
        val mainCompany = provider.companies.firstOrNull()
        val allBranches = provider.companies.flatMap { it.branches }
        val firstBranch = allBranches.firstOrNull()

        // Badge Logic matching ProviderViewModel
        val w24h = provider.works24h || allBranches.any { it.works24h }
        val hasLoc = provider.hasPhysicalLocation || allBranches.any { it.hasPhysicalLocation }
        val hVisits = provider.doesHomeVisits || allBranches.any { it.doesHomeVisits }
        val dServ = provider.doesService || allBranches.any { it.doesService }
        val dProd = provider.doesProduct || allBranches.any { it.doesProduct }
        val dShip = provider.doesShipping || allBranches.any { it.doesShipping }
        val accApp = provider.acceptsAppointments || allBranches.any { it.acceptsAppointments }

        val badges = listOf(
            com.example.myapplication.data.model.BadgeDisplayData("24h", "🕒", "Atención 24hs", w24h),
            com.example.myapplication.data.model.BadgeDisplayData("loc", "🏪", "Local Físico", hasLoc),
            com.example.myapplication.data.model.BadgeDisplayData("visit", "🚚", "Visitas a Domicilio", hVisits),
            com.example.myapplication.data.model.BadgeDisplayData("env", "📦", "Realiza Envíos", dShip),
            com.example.myapplication.data.model.BadgeDisplayData("date", "📅", "Turnos Online", accApp),
            com.example.myapplication.data.model.BadgeDisplayData("serv", "🛠️", "Servicios", dServ),
            com.example.myapplication.data.model.BadgeDisplayData("prod", "🛍️", "Venta Productos", dProd)
        )

        return ServiceDisplayModel(
            id = provider.uid,
            title = mainCompany?.name ?: provider.displayName,
            subtitle = if (isCompany) "Empresa" else "Independiente",
            photoUrl = mainCompany?.photoUrl ?: provider.photoUrl ?: "",
            rating = (mainCompany?.rating ?: provider.rating).toDouble(),
            isVerified = mainCompany?.isVerified ?: provider.isVerified,
            isOnline = provider.isOnline,
            isSubscribed = provider.isSubscribed,
            works24h = w24h,
            hasPhysicalLocation = hasLoc,
            doesHomeVisits = hVisits,
            doesService = dServ,
            doesProduct = dProd,
            doesShipping = dShip,
            acceptsAppointments = accApp,
            type = if (isCompany) ProviderType.COMPANY else ProviderType.INDIVIDUAL,
            categories = provider.categories,
            displayAddress = provider.address?.fullString() ?: firstBranch?.address?.fullString(),
            branchName = firstBranch?.name,
            typeEmoji = if (isCompany) "🏢" else "👨‍🔧",
            typeLabel = if (isCompany) "Empresa Certificada" else "Profesional Independiente",
            badgeList = badges
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}

data class FastFilterState(
    val isOnline: Boolean = false,
    val isSubscribed: Boolean = false,
    val is24h: Boolean = false,
    val isLocal: Boolean = false
)

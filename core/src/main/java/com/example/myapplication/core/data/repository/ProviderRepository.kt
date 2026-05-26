package com.example.myapplication.core.data.repository

import android.util.Log
import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.data.remote.ProviderDataMapper
import com.example.myapplication.core.domain.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PRESTADORES (COMPARTIDO) ---
 * Gestiona el catálogo de profesionales. Implementa la estrategia "Costo Cero"
 * que prioriza los datos locales de Room antes de realizar consultas a Firestore.
 */
@Singleton
class ProviderRepository @Inject constructor(
    private val providerDao: ProviderDao,
    private val firestore: FirebaseFirestore
) {
    private val tag = "ProviderRepository"

    // =========================================================================
    // === SECCIÓN: APP USUARIO (LECTURA / BÚSQUEDA) ===
    // =========================================================================

    /**
     * Observable de todos los prestadores conocidos localmente.
     */
    val allProviders: Flow<List<Provider>> = providerDao.getAllProviders().map { entities ->
        entities.map { it.toDomain() }
    }

    /**
     * Observable de todos los prestadores favoritos.
     */
    val favoriteProviders: Flow<List<Provider>> = providerDao.getFavoriteProviders().map { entities ->
        entities.map { it.toDomain() }
    }

    /**
     * Obtiene prestadores por categoría (Costo Cero).
     */
    suspend fun getProvidersByCategory(category: String): List<Provider> =
        providerDao.getProvidersByCategory(category).map { it.toDomain() }

    suspend fun getProviderById(providerId: String): Provider? =
        providerDao.getProviderById(providerId)?.toDomain()

    fun getProvidersByRegionAndCategory(zipCode: String, category: String): Flow<List<Provider>> =
        providerDao.getProvidersByFilter(zipCode, category).map { entities ->
            entities.map { it.toDomain() }
        }

    fun decorateProvider(provider: Provider, companyId: String?): Provider {
        if (companyId == null) return provider
        val company = provider.companies.find { it.id == companyId } ?: return provider
        
        return provider.copy(
            displayName = company.name,
            photoUrl = company.photoUrl ?: provider.photoUrl,
            hasCompanyProfile = true
        )
    }

    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean) {
        providerDao.updateFavoriteStatus(providerId, isFavorite)
    }

    // =========================================================================
    // === SECCIÓN: APP PRESTADOR (ESCRITURA / SINCRONIZACIÓN) ===
    // =========================================================================
    
    suspend fun saveProvider(provider: ProviderEntity) = providerDao.insertProvider(provider)

    suspend fun updateProvider(provider: ProviderEntity) = providerDao.insertProvider(provider)

    suspend fun deleteProvider(id: String) = providerDao.deleteById(id)
    
    /**
     * [CORE SSoT]: Sincronización completa delegada.
     */
    suspend fun syncProviderComplete(provider: Provider) {
        // Implementar lógica de escritura si es necesaria
    }

    // =========================================================================
    // === SECCIÓN: COMÚN (SISTEMA SYNC) ===
    // =========================================================================

    /**
     * Busca y sincroniza un prestador específico desde Firebase.
     */
    suspend fun fetchAndSyncProviderDetail(providerId: String) {
        try {
            val doc = firestore.collection("providers").document(providerId)
                .get(Source.SERVER).await()
            
            if (doc.exists()) {
                val entity = ProviderDataMapper.fromFirestore(doc)
                entity?.let {
                    providerDao.insertProvider(it)
                    Log.d(tag, "Perfil de prestador sincronizado: $providerId")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error sincronizando detalle del prestador: ${e.message}")
        }
    }

    /**
     * Realiza una búsqueda de prestadores por zona y rubro en Firebase.
     */
    suspend fun syncProvidersByRegion(zipCode: String, category: String) {
        try {
            val snapshot = firestore.collection("providers")
                .whereArrayContains("servicios", category)
                .whereEqualTo("ubicacion.codigoPostal", zipCode)
                .get().await()

            val remoteProviders = snapshot.documents.mapNotNull { doc ->
                ProviderDataMapper.fromFirestore(doc)
            }

            if (remoteProviders.isNotEmpty()) {
                providerDao.insertAll(remoteProviders)
                Log.d(tag, "Se encontraron y guardaron ${remoteProviders.size} prestadores.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error en búsqueda regional: ${e.message}")
        }
    }

    /**
     * Carga el perfil completo jerárquico desde Firestore y lo persiste en Room.
     * [ON-DEMAND]: Solo llamar cuando el usuario entra en el perfil específico.
     */
    suspend fun loadFullProfile(providerId: String) {
        try {
            val doc = firestore.collection("providers").document(providerId).get(Source.SERVER).await()
            if (!doc.exists()) return

            // Mapeo básico + profundización
            val entity = ProviderDataMapper.fromFirestore(doc) ?: return
            
            // Sincronización de Subcolecciones (Companies)
            val companiesSnapshot = doc.reference.collection("companies").get().await()
            val companies = companiesSnapshot.documents.map { compDoc ->
                com.example.myapplication.core.domain.model.CompanyProvider(
                    id = compDoc.id,
                    name = compDoc.getString("name") ?: compDoc.getString("nombre") ?: "",
                    razonSocial = compDoc.getString("razonSocial") ?: "",
                    cuit = compDoc.getString("cuit") ?: "",
                    description = compDoc.getString("description") ?: "",
                    isVerified = compDoc.getBoolean("isVerified") ?: false,
                    photoUrl = compDoc.getString("photoUrl"),
                    rating = (compDoc.getDouble("rating") ?: 0.0).toFloat()
                )
            }

            // Actualizar la entidad con las empresas mapeadas
            val fullEntity = entity.copy(companies = companies)

            providerDao.insertProvider(fullEntity)
            Log.d(tag, "✅ Perfil completo cargado para: $providerId con ${companies.size} empresas")

        } catch (e: Exception) {
            Log.e(tag, "❌ Error en carga jerárquica: ${e.message}")
        }
    }

    suspend fun forceSyncProviders(zipCode: String, category: String) {
        syncProvidersByRegion(zipCode, category)
    }
}

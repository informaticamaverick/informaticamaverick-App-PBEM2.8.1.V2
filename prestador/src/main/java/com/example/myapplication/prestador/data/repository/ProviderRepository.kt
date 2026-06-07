
/**
package com.example.myapplication.prestador.data.repository

/*
 * ARCHIVO EN DESUSO
 * Motivo: Se ha centralizado la fuente de verdad en los repositorios del módulo :core.
 * Se recomienda usar com.example.myapplication.core.data.repository.ProviderRepository
 * para todas las operaciones de persistencia y sincronización de prestadores.
 */

/*
import android.content.Context
import android.util.Log
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.domain.model.Provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providerDao: ProviderDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    private val TAG = "ProviderRepository"

    fun getProviderById(id: String): Flow<ProviderEntity?> = providerDao.getProviderById(id)
    
    suspend fun getProviderByIdOnce(id: String): ProviderEntity? = providerDao.getProviderByIdOnce(id)
    
    suspend fun saveProvider(provider: ProviderEntity) = providerDao.insertProvider(provider)
    
    suspend fun updateProvider(provider: ProviderEntity) = providerDao.updateProvider(provider)
    
    suspend fun updateProviderImage(id: String, imageUrl: String) = providerDao.updateProviderImage(id, imageUrl)

    suspend fun deleteProvider(id: String) = providerDao.deleteProviderById(id)

    suspend fun syncProviderWithFirebase(provider: Provider) {
        Log.d(TAG, "Sync delegada al módulo :core")
    }

    suspend fun updateBannerOnFirestore(userId: String, base64: String) {
        Log.w(TAG, "Funcionalidad de banners deshabilitada por directiva SSOT")
    }

    suspend fun fetchAndUpdateFromFirestore(uid: String) {
    }

    suspend fun loadFullProfileFromFirestore(userId: String): ProviderEntity? {
        return null
    }

    fun searchProviders(query: String): Flow<List<ProviderEntity>> = providerDao.searchProviders("%$query%")
    fun getAllProviders(): Flow<List<ProviderEntity>> = providerDao.getAllProviders()
    suspend fun providerExists(id: String): Boolean = providerDao.providerExists(id)

    suspend fun actualizarModoEmpresa(uid: String, priorizarEmpresa: Boolean) {
    }

    suspend fun actualizarVisibilidadPerfil(uid: String, priorizarEmpresa: Boolean, companyIds: List<String>) {
    }
}
*/
*/

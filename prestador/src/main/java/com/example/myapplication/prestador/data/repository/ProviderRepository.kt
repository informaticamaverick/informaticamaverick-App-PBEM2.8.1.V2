package com.example.myapplication.prestador.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.FilterQueryProvider
import com.example.myapplication.prestador.data.local.dao.ProviderDao
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.model.Provider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * REPOSITORY para Providers
 * 
 * [ACTUALIZADO] Sincronización robusta replicada del molde UserRepository (App Cliente).
 * Gestiona la persistencia local en Room y la sincronización jerárquica en Firestore.
 */
@Singleton
class ProviderRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providerDao: ProviderDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val TAG = "ProviderRepository"

    // ─── SECCIÓN: CONSULTAS LOCALES (ROOM) ──────────────────────────────────

    fun getProviderById(id: String): Flow<ProviderEntity?> = providerDao.getProviderById(id)
    
    suspend fun getProviderByIdOnce(id: String): ProviderEntity? = providerDao.getProviderByIdOnce(id)
    
    suspend fun saveProvider(provider: ProviderEntity) = providerDao.insertProvider(provider)
    
    suspend fun updateProvider(provider: ProviderEntity) = providerDao.updateProvider(provider)
    
    suspend fun updateProviderImage(id: String, imageUrl: String) = providerDao.updateProviderImage(id, imageUrl)

    suspend fun deleteProvider(id: String) = providerDao.deleteProviderById(id)

    // ─── SECCIÓN: SINCRONIZACIÓN FIRESTORE (REPLICADO DEL MOLDE CLIENTE) ────

    /**
     * Sincronización PROFUNDA con Estructura Jerárquica.
     * Reclica el flujo de UserRepository.kt de la App Cliente para asegurar consistencia de datos.
     */
    suspend fun syncProviderWithFirebase(provider: Provider) {
        val uid = auth.currentUser?.uid ?: provider.uid
        if (uid.isBlank()) return

        try {
            // =========================================================================
            // PASO 1: ACTUALIZACIÓN LOCAL INMEDIATA
            // =========================================================================
            val entity = ProviderEntity.fromDomain(provider)
            providerDao.insertProvider(entity)
            Log.d(TAG, "🏠 [LOCAL] Perfil guardado en Room.")

            // =========================================================================
            // PASO 2: SINCRONIZACIÓN REMOTA ATÓMICA (WriteBatch)
            // =========================================================================
            Log.d(TAG, "⏳ [REMOTO] Iniciando sincronización por lote para: $uid")
            val batch = firestore.batch()

            // A. Documento Principal en Firestore (providers/{uid})
            val providerDocRef = firestore.collection("providers").document(uid)
            
            val perfilMap = mapOf(
                "nombre" to provider.name,
                "apellido" to provider.lastName,
                "email" to provider.email,
                "telefono" to provider.phoneNumber,
                "description" to provider.description,
                "imageUrl" to provider.photoUrl,
                "bannerUrl" to provider.bannerImageUrl,
                "dniCuit" to provider.cuilCuit,
                "matricula" to provider.matricula,
                "profesion" to provider.profesion,
                "rating" to provider.rating
            )

            val providerMap = linkedMapOf<String, Any?>(
                "uid" to uid,
                "email" to provider.email,
                "displayName" to provider.displayName,
                "nombre" to provider.name,
                "apellido" to provider.lastName,
                "phoneNumber" to provider.phoneNumber,
                "description" to provider.description,
                "photoUrl" to provider.photoUrl,
                "bannerImageUrl" to provider.bannerImageUrl,
                "galleryImages" to provider.galleryImages,
                "perfil" to perfilMap,
                "hasCompanyProfile" to provider.hasCompanyProfile,
                "empresa.priorizarEmpresa" to provider.priorizarEmpresa,
                "isOnline" to provider.isOnline,
                "isSubscribed" to true, // Provisorio hasta que se desarrolle la logica del pago
                "isVerified" to provider.isVerified,
                "rating" to provider.rating,
                "servicios" to provider.categories,
                "serviceType" to provider.serviceType,
                "createdAt" to provider.createdAt,
                "updatedAt" to System.currentTimeMillis(),
                "doesService" to provider.doesService,
                "doesProduct" to provider.doesProduct,
                "atencionUrgencias" to provider.works24h,
                "vaDomicilio" to provider.doesHomeVisits,
                "envios" to provider.doesShipping,
                "turnosEnLocal" to provider.hasPhysicalLocation,
                "local" to mapOf(
                    "turnosEnLocal" to provider.hasPhysicalLocation,
                    "horarioLocal" to provider.workingHours
                )
            )

            // ─── SECCIÓN: GENERACIÓN DE LISTA DE TÓPICOS (MatchKeys) ──────────────────────
            // 1. Recopilamos todos los códigos postales únicos (Personal + Empresas/Sucursales)
            val allPostalCodes = (
                listOfNotNull(provider.address?.codigoPostal) + 
                provider.addresses.map { it.codigoPostal } + 
                provider.companies.flatMap { it.branches.map { b -> b.address.codigoPostal } }
            ).filter { it.isNotBlank() }.distinct()

            // 2. Recopilamos todas las categorías únicas (Personales y de Empresas)
            val allCats = (
                provider.categories + 
                provider.companies.flatMap { it.categories }
            ).filter { it.isNotBlank() }.distinct()

            // 3. Generamos la matriz de tópicos (CP x Categoría) asegurando datos válidos
            if (allPostalCodes.isNotEmpty() && allCats.isNotEmpty()) {
                val topicList = mutableListOf<String>()
                allPostalCodes.forEach { cp ->
                    val cleanCp = normalizeForTopic(cp)
                    allCats.forEach { cat ->
                        val cleanCat = normalizeForTopic(cat)
                        if (cleanCp.isNotBlank() && cleanCat.isNotBlank()) {
                            topicList.add("tender_${cleanCp}_$cleanCat")
                        }
                    }
                }
                
                if (topicList.isNotEmpty()) {
                    providerMap["fcmTopics"] = topicList.distinct()
                    providerMap["matchKeys"] = topicList.distinct() // Replicamos por compatibilidad
                    Log.d(TAG, "📡 [TOPICS] Generados ${topicList.size} matchKeys para Firebase.")
                }
            }
            // ─────────────────────────────────────────────────────────────────────────────

            provider.address?.let { addr ->
                providerMap["latitud"] = addr.latitude
                providerMap["longitud"] = addr.longitude
                providerMap["provincia"] = addr.provincia
                providerMap["codigoPostal"] = addr.codigoPostal
                
                providerMap["ubicacion"] = mapOf(
                    "calle" to addr.calle,
                    "direccion" to addr.fullString(),
                    "localidad" to addr.localidad,
                    "provincia" to addr.provincia,
                    "codigoPostal" to addr.codigoPostal,
                    "latitude" to addr.latitude,
                    "longitude" to addr.longitude
                )
            }
            
            batch.set(providerDocRef, providerMap, SetOptions.merge())

            // B. Subcolección: addresses
            provider.addresses.forEach { address ->
                val addrRef = providerDocRef.collection("addresses").document(address.id)
                batch.set(addrRef, address, SetOptions.merge())
            }

            // C. Subcolección: companies (Empresas -> Sucursales -> Empleados)
            provider.companies.forEach { company ->
                val companyDocRef = providerDocRef.collection("companies").document(company.id)
                val companyMap = mapOf(
                    "id" to company.id,
                    "name" to company.name,
                    "razonSocial" to company.razonSocial,
                    "cuit" to company.cuit,
                    "description" to company.description,
                    "photoUrl" to company.photoUrl,
                    "rating" to company.rating,
                    "categories" to company.categories,
                    "verificado" to company.isVerified
                )
                batch.set(companyDocRef, companyMap, SetOptions.merge())

                company.branches.forEach { branch ->
                    val branchDocRef = companyDocRef.collection("branches").document(branch.id)
                    val branchMap = mapOf(
                        "id" to branch.id,
                        "name" to branch.name,
                        "nombre" to branch.name,
                        "address" to branch.address?.let { addr ->
                            mapOf(
                                "id" to addr.id,
                                "calle" to addr.calle,
                                "numero" to addr.numero,
                                "localidad" to addr.localidad,
                                "provincia" to addr.provincia,
                                "pais" to addr.pais,
                                "codigoPostal" to addr.codigoPostal,
                                "latitude" to addr.latitude,
                                "longitude" to addr.longitude,
                                "direccion" to addr.fullString()
                            )
                        },
                        "horario" to branch.workingHours,
                        "workingHours" to branch.workingHours,
                        "galleryImages" to branch.galleryImages,
                        "rating" to branch.rating,
                        "doesService" to branch.doesService,
                        "doesProduct" to branch.doesProduct,
                        "works24h" to branch.works24h,
                        "hasPhysicalLocation" to branch.hasPhysicalLocation,
                        "doesHomeVisits" to branch.doesHomeVisits,
                        "doesShipping" to branch.doesShipping,
                        "acceptsAppointments" to branch.acceptsAppointments
                    )
                    batch.set(branchDocRef, branchMap, SetOptions.merge())

                    branch.employees.forEach { employee ->
                        val empRef = branchDocRef.collection("employees").document(employee.id)
                        batch.set(empRef, employee, SetOptions.merge())
                    }
                }
            }

            // EJECUCIÓN ATÓMICA DEL LOTE
            batch.commit().await()
            Log.d(TAG, "✅ [REMOTO] Sincronización exitosa con WriteBatch.")

        } catch (e: Exception) {
            Log.e(TAG, "❌ [FALLO] Error en sincronización: ${e.message}")
            throw e
        }
    }

    suspend fun deleteAddressFromFirebase(addressId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers")
            .document(uid)
            .collection("addresses")
            .document(addressId)
            .delete()
            .await()
        Log.d(TAG, "✅ [REMOTO] Dirección $addressId eliminada de Firestore.")
    }

    // ─── OTROS MÉTODOS DE REPOSITORIO ──────────────────────────────────────

    suspend fun deleteBranchFromFirebase(companyId: String, branchId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers")
            .document(uid)
            .collection("companies")
            .document(companyId)
            .collection("branches")
            .document(branchId)
            .delete()
            .await()
        Log.d(TAG, "✅ [REMOTO] Sucursal $branchId eliminada de Firestore.")
    }

    suspend fun deleteEmployeeFromFirebase(companyId: String, branchId: String, employeeId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers")
            .document(uid)
            .collection("companies")
            .document(companyId)
            .collection("branches")
            .document(branchId)
            .collection("employees")
            .document(employeeId)
            .delete()
            .await()
        Log.d(TAG, "✅ [REMOTO] Empleado $employeeId eliminado de Firestore.")
    }

    suspend fun deleteCompanyFromFirebase(companyId: String) {
        val uid = auth.currentUser?.uid ?: return
        val companyRef = firestore.collection("providers")
            .document(uid)
            .collection("companies")
            .document(companyId)

        // Borrar todas las branches de la empresa (Firestore no las borra automáticamente)
        val branches = companyRef.collection("branches").get().await()
        for (branch in branches.documents) {
            branch.reference.delete().await()
        }

        companyRef.delete().await()
        Log.d(TAG, "✅ [REMOTO] Empresa $companyId y sus branches eliminadas de Firestore.")
    }

    fun searchProviders(query: String): Flow<List<ProviderEntity>> = providerDao.searchProviders("%$query%")
    fun getAllProviders(): Flow<List<ProviderEntity>> = providerDao.getAllProviders()
    suspend fun providerExists(id: String): Boolean = providerDao.providerExists(id)

    // ─── SECCIÓN: UTILIDADES DE NORMALIZACIÓN (PRIVADAS) ────────────────────

    private fun normalizeForTopic(input: String): String {
        val normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
        val accentRemoved = "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
        return accentRemoved
            .replace(" ", "_")
            .replace("(", "")
            .replace(")", "")
            .replace(Regex("[^a-zA-Z0-9-_.~%]"), "")
            .lowercase()
    }
}

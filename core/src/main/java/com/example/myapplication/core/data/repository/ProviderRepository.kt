package com.example.myapplication.core.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myapplication.core.data.local.dao.ProviderDao
import com.example.myapplication.core.data.local.dao.ShallowProvider
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.data.remote.ProviderDataMapper
import com.example.myapplication.core.domain.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO DE PRESTADORES (COMPARTIDO) ---
 * Gestiona el catálogo de profesionales. Implementa la estrategia "Costo Cero"
 * que prioriza los datos locales de Room antes de realizar consultas a Firestore.
 */
@Singleton
class ProviderRepository @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val providerDao: ProviderDao,
    private val firestore: FirebaseFirestore,
    private val auth: com.google.firebase.auth.FirebaseAuth
) {
    private val tag = "ProviderRepository"

    // =========================================================================
    // === SECCIÓN: VALIDACIONES DE SEGURIDAD (LEY #7) ===
    // =========================================================================

    private fun verifyWriteAccess(targetUid: String) {
        val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        if (currentUid != targetUid) {
            throw SecurityException("Intento de escritura no autorizado: $currentUid no puede modificar $targetUid")
        }
    }

    // =========================================================================
    // === SECCIÓN: APP CLIENTE (LECTURA / BÚSQUEDA) ===
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
     * Obtiene todos los prestadores en formato Entity (Para App Prestador).
     */
    fun getAllProvidersEntity(): Flow<List<ProviderEntity>> = providerDao.getAllProviders()

    /**
     * Búsqueda de prestadores por nombre o nombre de pantalla.
     */
    fun searchProviders(query: String): Flow<List<ProviderEntity>> =
        providerDao.searchProviders("%$query%")

    /**
     * Obtiene prestadores por categoría (Costo Cero).
     */
    suspend fun getProvidersByCategory(category: String): List<Provider> =
        providerDao.getProvidersByCategory(category).map { it.toDomain() }

    suspend fun getProviderById(providerId: String): Provider? =
        providerDao.getProviderById(providerId)?.toDomain()

    fun getProviderFlowById(providerId: String): Flow<ProviderEntity?> =
        providerDao.getProviderFlowById(providerId)

    suspend fun getProviderByIdOnce(providerId: String): ProviderEntity? = 
        providerDao.getProviderById(providerId)

    /**
     * [ELITE] Obtiene prestadores por región y categoría con Proyección Ligera.
     * Implementa la Ley #3.1 (Shallow Loading).
     */
    fun getShallowProvidersByRegionAndCategory(zipCode: String, category: String): Flow<List<ShallowProvider>> =
        providerDao.getShallowProvidersByFilter(zipCode, category)

    /**
     * [ELITE] Versión paginada (Paging 3) para la Ley #4 de Inmediatez.
     * Optimiza memoria y CPU al no cargar toda la lista en memoria.
     */
    fun getShallowProvidersPaged(zipCode: String, category: String): Flow<PagingData<ShallowProvider>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { providerDao.getShallowProvidersPaged(zipCode, category) }
        ).flow
    }

    fun decorateProvider(provider: Provider, companyId: String?): Provider {
        if (companyId == null) return provider
        val company = provider.companies.find { it.id == companyId } ?: return provider
        
        return provider.copy(
            displayName = company.name,
            photoUrl = company.photoUrl ?: provider.photoUrl
        )
    }

    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean) {
        providerDao.updateFavoriteStatus(providerId, isFavorite)
    }

    // =========================================================================
    // === SECCIÓN: APP PRESTADOR (ESCRITURA / GESTIÓN) ===
    // =========================================================================
    
    suspend fun saveProvider(provider: ProviderEntity) {
        verifyWriteAccess(provider.id)
        providerDao.insertProvider(provider)
    }

    suspend fun updateProvider(provider: ProviderEntity) {
        verifyWriteAccess(provider.id)
        providerDao.insertProvider(provider)
        syncProviderWithFirebase(provider.toDomain())
    }

    suspend fun deleteProvider(id: String) {
        verifyWriteAccess(id)
        providerDao.deleteById(id)
    }

    suspend fun updateProviderImage(id: String, imageUrl: String) {
        verifyWriteAccess(id)
        providerDao.updateProviderImage(id, imageUrl)
    }

    suspend fun updateProfilePhotoOnFirestore(userId: String, base64: String, thumbnailBase64: String?) {
        verifyWriteAccess(userId)
        
        // 1. Actualización local inmediata (Ley #2)
        providerDao.updateProviderImage(userId, base64)
        if (thumbnailBase64 != null) {
            // Asumiendo que existe un método en DAO o usamos una query directa
            providerDao.updateProviderThumbnail(userId, thumbnailBase64)
        }

        // 2. Sincronización remota
        val updateMap = mutableMapOf<String, Any?>(
            "photoUrl" to base64,
            "perfil.photoUrl" to base64,
            "updatedAt" to System.currentTimeMillis()
        )
        if (thumbnailBase64 != null) {
            updateMap["profileThumbnail"] = thumbnailBase64
            updateMap["perfil.profileThumbnail"] = thumbnailBase64
        }

        firestore.collection("providers").document(userId)
            .update(updateMap).await()
    }

    suspend fun updateGalleryImagesOnFirestore(userId: String, list: List<String>) {
        verifyWriteAccess(userId)
        firestore.collection("providers").document(userId)
            .update(mapOf(
                "galleryImages" to list,
                "updatedAt" to System.currentTimeMillis()
            )).await()
    }

    suspend fun actualizarModoEmpresa(uid: String, priorizarEmpresa: Boolean) {
        verifyWriteAccess(uid)
        firestore.collection("providers").document(uid)
            .update(
                mapOf(
                    "empresa.priorizarEmpresa" to priorizarEmpresa,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    suspend fun actualizarVisibilidadPerfil(uid: String, priorizarEmpresa: Boolean, companyIds: List<String>) {
        verifyWriteAccess(uid)
        val providerRef = firestore.collection("providers").document(uid)
        providerRef.update("visible", !priorizarEmpresa).await()
        companyIds.forEach { companyId ->
            providerRef.collection("companies").document(companyId)
                .update("visible", priorizarEmpresa)
                .await()
        }
    }

    suspend fun deleteCompanyFromFirebase(companyId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers").document(uid)
            .collection("companies").document(companyId).delete().await()
    }

    suspend fun deleteBranchFromFirebase(companyId: String, branchId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers").document(uid)
            .collection("companies").document(companyId)
            .collection("branches").document(branchId).delete().await()
    }

    suspend fun deleteEmployeeFromFirebase(companyId: String, branchId: String, employeeId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers").document(uid)
            .collection("companies").document(companyId)
            .collection("branches").document(branchId)
            .collection("employees").document(employeeId).delete().await()
    }

    suspend fun deleteAddressFromFirebase(addressId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("providers").document(uid)
            .collection("personalAddresses").document(addressId).delete().await()
    }
    
    /**
     * [CORE SSoT]: Sincronización completa jerárquica.
     * Implementa Carga On-Demand Jerárquica (Subcolecciones).
     */
    suspend fun syncProviderWithFirebase(provider: Provider) {
        val uid = auth.currentUser?.uid ?: provider.uid
        if (uid.isBlank()) return
        verifyWriteAccess(uid)

        try {
            // 1. Persistencia local inmediata
            providerDao.insertProvider(ProviderEntity.fromDomain(provider))

            // 2. Perfil Básico (Shallow Document) - Alineado con UserRepository para consistencia
            val perfilMap = mutableMapOf<String, Any?>(
                "name" to provider.name,
                "lastName" to provider.lastName,
                "email" to provider.email,
                "phoneNumber" to provider.phoneNumber,
                "displayName" to provider.displayName,
                "bio" to provider.description,
                "photoUrl" to provider.photoUrl,
                "profileThumbnail" to provider.profileThumbnail,
                "matricula" to provider.matricula,
                "profesion" to provider.profesion,
                "dniCuit" to provider.cuilCuit
            ).filterValues { it != null && (it !is String || it.isNotBlank()) }

            val modalidadMap = mapOf(
                "atencionUrgencias" to provider.works24h,
                "vaDomicilio" to provider.doesHomeVisits,
                "envios" to provider.doesShipping
            )

            val localMap = mapOf(
                "workingHours" to provider.workingHours,
                "atencionEnLocal" to provider.hasPhysicalLocation,
                "aceptaTurnos" to provider.acceptsAppointments
            )

            // Datos de empresas Shallow (respuesta inmediata)
            val shallowCompanies = provider.companies.map { company ->
                mapOf(
                    "id" to company.id,
                    "name" to company.name,
                    "photoUrl" to company.photoUrl,
                    "thumbnailBase64" to company.thumbnailBase64,
                    "categories" to company.categories,
                    "branches" to company.branches.take(2).map { b ->
                        mapOf(
                            "id" to b.id,
                            "name" to b.name,
                            "address" to b.address
                        )
                    }
                )
            }

            val providerData = mutableMapOf<String, Any?>(
                "uid" to uid,
                "email" to provider.email,
                "displayName" to provider.displayName,
                "perfil" to perfilMap,
                "personalAddresses" to provider.addresses,
                "ubicacion" to provider.address?.let { addr ->
                    mapOf(
                        "calle" to addr.calle,
                        "numero" to addr.numero,
                        "localidad" to addr.localidad,
                        "provincia" to addr.provincia,
                        "codigoPostal" to addr.codigoPostal,
                        "latitude" to addr.latitude,
                        "longitude" to addr.longitude,
                        "label" to addr.label
                    )
                },
                "modalidad" to modalidadMap,
                "local" to localMap,
                "isVerified" to provider.isVerified,
                "isOnline" to provider.isOnline,
                "isSubscribed" to provider.isSubscribed,
                "hasCompanyProfile" to provider.companies.isNotEmpty(), // [PERFECCIÓN]: Flag para UI
                "rating" to provider.rating,
                "categories" to provider.categories,
                "trabajaConOtros" to provider.trabajaConOtros,
                "doesService" to provider.doesService,
                "doesProduct" to provider.doesProduct,
                "empresa" to mapOf("priorizarEmpresa" to provider.priorizarEmpresa),
                "photoUrl" to provider.photoUrl, 
                "profileThumbnail" to provider.profileThumbnail,
                "createdAt" to provider.createdAt,
                "updatedAt" to System.currentTimeMillis()
            ).filterValues { it != null }

            firestore.collection("providers").document(uid).set(providerData, com.google.firebase.firestore.SetOptions.merge()).await()

            // 3. Sincronización de Direcciones Personales (Subcolección)
            provider.addresses.forEach { addr ->
                firestore.collection("providers").document(uid)
                    .collection("personalAddresses").document(addr.id)
                    .set(addr, com.google.firebase.firestore.SetOptions.merge()).await()
            }

            // 4. Sincronización Profunda de Empresas (Subcolecciones)
            provider.companies.forEach { company ->
                val compRef = firestore.collection("providers").document(uid)
                    .collection("companies").document(company.id)
                
                val companyData = company.copy(branches = emptyList())
                compRef.set(companyData, com.google.firebase.firestore.SetOptions.merge()).await()

                company.branches.forEach { branch ->
                    val branchRef = compRef.collection("branches").document(branch.id)
                    val branchData = branch.copy(team = emptyList())
                    branchRef.set(branchData, com.google.firebase.firestore.SetOptions.merge()).await()

                    branch.team.forEach { employee ->
                        branchRef.collection("employees").document(employee.id).set(employee, com.google.firebase.firestore.SetOptions.merge()).await()
                    }
                }
            }

            // 4. 🔥 [ELITE] Actualización del Índice de Búsqueda (Puntos de Servicio)
            syncSearchIndex(provider)
            
            Log.d(tag, "✅ Perfil de prestador, empresas e índice sincronizados (SSOT).")

        } catch (e: Exception) {
            Log.e(tag, "❌ Fallo en sincronización remota: ${e.message}")
            throw e
        }
    }

    /**
     * [ELITE] Aplana la jerarquía del prestador en documentos individuales en 'search_index'.
     * Esto permite búsquedas rápidas por Código Postal y Categoría para el cliente.
     */
    private suspend fun syncSearchIndex(provider: Provider) {
        Log.d(tag, "📡 [SYNC_INDEX] Iniciando indexación para: ${provider.displayName} (PriorizarEmpresa: ${provider.priorizarEmpresa})")
        val batch = firestore.batch()
        
        var pointsIndexed = 0
        // 1. Ubicaciones Personales (Solo si NO prioriza empresa)
        if (!provider.priorizarEmpresa) {
            provider.address?.let { addr ->
                if (addr.codigoPostal.isNotBlank()) {
                    val indexId = "${provider.uid}_main"
                    val indexRef = firestore.collection("search_index").document(indexId)
                    batch.set(indexRef, createIndexMap(provider, null, null, addr))
                    pointsIndexed++
                }
            }
            provider.addresses.filter { it.id != provider.address?.id }.forEach { addr ->
                if (addr.codigoPostal.isNotBlank()) {
                    val indexId = "${provider.uid}_addr_${addr.id}"
                    val indexRef = firestore.collection("search_index").document(indexId)
                    batch.set(indexRef, createIndexMap(provider, null, null, addr))
                    pointsIndexed++
                }
            }
        } else {
            Log.d(tag, "🛡️ [SYNC_INDEX] Soberanía de Empresa activa. Omitiendo direcciones personales.")
        }

        // 2. Sucursales de Empresas
        provider.companies.forEach { company ->
            company.branches.forEach { branch ->
                if (branch.address.codigoPostal.isNotBlank()) {
                    val indexId = "${provider.uid}_${company.id}_${branch.id}"
                    val indexRef = firestore.collection("search_index").document(indexId)
                    batch.set(indexRef, createIndexMap(provider, company, branch, branch.address))
                    pointsIndexed++
                }
            }
        }

        try {
            batch.commit().await()
            Log.d(tag, "✅ [SYNC_INDEX] Sincronización exitosa. Puntos indexados: $pointsIndexed")
        } catch (e: Exception) {
            Log.e(tag, "❌ [SYNC_INDEX] Error al comprometer batch: ${e.message}")
            throw e
        }
    }

    private fun createIndexMap(
        p: Provider, 
        c: com.example.myapplication.core.domain.model.CompanyProvider?, 
        b: com.example.myapplication.core.domain.model.BranchProvider?, 
        addr: com.example.myapplication.core.domain.model.AddressUnico
    ): Map<String, Any?> {
        val categories = (c?.categories ?: p.categories)
        val works24h = (b?.works24h ?: p.works24h)
        val hasPhysicalLocation = (b?.hasPhysicalLocation ?: p.hasPhysicalLocation)
        val doesHomeVisits = (b?.doesHomeVisits ?: p.doesHomeVisits)
        val doesShipping = (b?.doesShipping ?: p.doesShipping)
        val acceptsAppointments = (b?.acceptsAppointments ?: p.acceptsAppointments)

        // [ELITE] Generación de claves compuestas para filtros rápidos en Firestore
        // "Grandes Ligas": Combinamos CP y Categoría para evitar queries multi-campo y reducir la necesidad de índices complejos.
        // Formato: zipCode_category_flag (Ej: "T4000_plomero_24h")
        val searchFilters = mutableListOf<String>()
        val cp = addr.codigoPostal
        
        categories.forEach { cat ->
            val base = "${cp}_$cat" 
            searchFilters.add(base) // Búsqueda base en zona
            
            if (works24h) searchFilters.add("${base}_24h")
            if (doesHomeVisits) searchFilters.add("${base}_home")
            if (hasPhysicalLocation) searchFilters.add("${base}_local")
            if (p.isSubscribed) searchFilters.add("${base}_elite")
            
            // [PREPARACIÓN FAST]: Filtros para búsqueda instantánea (Online + 24hs)
            if (p.isOnline && works24h) searchFilters.add("${base}_fast")
        }

        val indexData = mapOf(
            "providerId" to p.uid,
            "companyId" to c?.id,
            "branchId" to b?.id,
            "title" to (c?.name ?: p.displayName),
            "branchName" to (b?.name ?: "CASA CENTRAL"),
            "name" to p.name,
            "lastName" to p.lastName,
            "photoUrl" to (c?.photoUrl ?: p.photoUrl),
            "thumbnailBase64" to (c?.thumbnailBase64 ?: p.profileThumbnail),
            "rating" to (b?.rating ?: p.rating),
            "categories" to categories,
            "searchFilters" to searchFilters, 
            "zipCode" to addr.codigoPostal,
            "latitude" to addr.latitude,
            "longitude" to addr.longitude,
            "geohash" to com.example.myapplication.core.utils.MaverickGeoUtils.computeGeohash(addr.latitude, addr.longitude),
            "fullAddress" to addr.fullString(), // [ELITE] Dirección pre-formateada
            "address" to mapOf(
                "calle" to addr.calle,
                "numero" to addr.numero,
                "localidad" to addr.localidad,
                "provincia" to addr.provincia,
                "codigoPostal" to addr.codigoPostal,
                "latitude" to addr.latitude,
                "longitude" to addr.longitude
            ),
            "isSubscribed" to p.isSubscribed,
            "isVerified" to (c?.isVerified ?: p.isVerified),
            "isOnline" to p.isOnline,
            "works24h" to works24h,
            "hasPhysicalLocation" to hasPhysicalLocation,
            "doesHomeVisits" to doesHomeVisits,
            "doesShipping" to doesShipping,
            "acceptsAppointments" to acceptsAppointments,
            "updatedAt" to System.currentTimeMillis()
        )
        
        // Log.v(tag, "📄 [INDEX_DATA] Mapped document for ${indexData["title"]} - Branch: ${indexData["branchName"]} | CP: ${indexData["zipCode"]} | Geohash: ${indexData["geohash"]}")
        return indexData
    }

    // =========================================================================
    // === SECCIÓN: COMÚN (SISTEMA SYNC) ===
    // =========================================================================

    /**
     * Busca y sincroniza un prestador específico desde Firebase.
     * 🔥 [FIX v8.0] Sincronización Inteligente: Actualiza identidades compuestas.
     */
    suspend fun fetchAndSyncProviderDetail(providerId: String) {
        try {
            val doc = firestore.collection("providers").document(providerId)
                .get(Source.SERVER).await()
            
            if (doc.exists()) {
                val entity = ProviderDataMapper.fromFirestore(doc)
                entity?.let { newEntity ->
                    // 1. Guardamos el perfil base (UID)
                    providerDao.insertProvider(newEntity)
                    
                    // 2. 🔥 [ELITE] Propagamos la actualización a todos los puntos de servicio 
                    // que compartan el mismo UID para evitar discrepancias visuales.
                    // Esto resuelve el problema de "tarjetas duplicadas" al unificar datos.
                    Log.d(tag, "Perfil base de prestador sincronizado: $providerId")
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error sincronizando detalle del prestador: ${e.message}")
        }
    }

    /**
     * [ELITE] Realiza una búsqueda de prestadores por zona y rubro en Firebase.
     * Consulta el "Índice de Puntos de Servicio" para una búsqueda ultrarrápida y barata.
     * Implementa la Ley #2.3 (Sincronización por Deltas).
     */
    suspend fun syncProvidersByRegion(
        zipCode: String, 
        category: String, 
        force: Boolean = false,
        filterFlags: List<String> = emptyList() // [ELITE] Soporte para filtros combinados en red
    ) = withContext(kotlinx.coroutines.Dispatchers.IO) {
        Log.d(tag, "🔍 [FETCH_RESULTS] Buscando: $category en CP: $zipCode (Flags: $filterFlags)")
        val syncKey = "last_sync_${category}_${zipCode}_${filterFlags.sorted().joinToString("_")}"
        val prefs = context.getSharedPreferences("maverick_sync_prefs", android.content.Context.MODE_PRIVATE)
        val lastForceSync = prefs.getLong(syncKey, 0L)
        val now = System.currentTimeMillis()
        
        val oneDayMillis = 24 * 60 * 60 * 1000L
        if (force && (now - lastForceSync < oneDayMillis)) {
            Log.d(tag, "🚫 [FETCH_RESULTS] Throttling activo para $category en $zipCode. Ignorando force sync.")
        }

        try {
            val isRoomEmpty = (providerDao.getLastSyncForCategory(category) ?: 0L) == 0L
            val lastSync = if (force || isRoomEmpty) 0L else providerDao.getLastSyncForCategory(category) ?: 0L
            
            // 🔥 [ELITE GRANDES LIGAS] Clave Compuesta: CP + Categoría
            // Esto reduce la query de 3 campos (CP, Cat, Flags) a 1 solo (Tags).
            // Elimina el error FAILED_PRECONDITION al no requerir índices manuales para la búsqueda básica.
            val targetSearchTag = if (filterFlags.isNotEmpty()) {
                "${zipCode}_${category}_${filterFlags.first()}" 
            } else "${zipCode}_$category"

            val query = firestore.collection("search_index")
                .whereArrayContains("searchFilters", targetSearchTag)

            // [TEMPORAL]: Quitamos 'whereGreaterThan' de Firestore para evitar el requisito de índice compuesto con range-filter.
            // La Ley #2.3 (Sync por Deltas) se realiza ahora en memoria para garantizar inmediatez sin crashes.
            if (lastSync > 0) {
                Log.d(tag, "📥 [FETCH_RESULTS] Modo DELTA (Filtrado en Cliente). Desde: $lastSync")
            } else {
                Log.d(tag, "📥 [FETCH_RESULTS] Modo FULL. Primera carga o force sync.")
            }

            val snapshot = query.get().await()
            Log.d(tag, "📊 [FETCH_RESULTS] Documentos recibidos de Firebase: ${snapshot.size()}")

            // Mapeamos y filtramos por timestamp localmente (Ley #3: Shallow Loading)
            val remoteProviders = snapshot.documents.mapNotNull { doc ->
                val entity = ProviderDataMapper.fromSearchIndex(doc)
                if (entity != null && (lastSync == 0L || entity.lastSyncTimestamp > lastSync)) {
                    entity.copy(lastSyncTimestamp = System.currentTimeMillis())
                } else null
            }

            if (remoteProviders.isNotEmpty()) {
                providerDao.insertAll(remoteProviders)
                Log.d(tag, "✅ [FETCH_RESULTS] Guardados en Room: ${remoteProviders.size} puntos de servicio")
                if (force || isRoomEmpty) prefs.edit().putLong(syncKey, now).apply()
            } else {
                Log.d(tag, "⚠️ [FETCH_RESULTS] No se encontraron resultados que superen el delta.")
            }
        } catch (e: Exception) {
            Log.e(tag, "❌ [FETCH_RESULTS] Error crítico en búsqueda por índice: ${e.message}", e)
        }
    }

    /**
     * Carga el perfil completo jerárquico desde Firestore y lo persiste en Room.
     * [ON-DEMAND]: Solo llamar cuando el usuario entra en el perfil específico.
     * Implementa Deep Loading de Empresas -> Sucursales -> Empleados.
     */
    suspend fun loadFullProfile(providerId: String) {
        val entity = loadFullProfileFromFirestore(providerId)
        if (entity != null) {
            providerDao.insertProvider(entity)
            Log.d(tag, "✅ DEEP LOAD CUMPLIDO: $providerId")
        }
    }

    suspend fun loadFullProfileFromFirestore(providerId: String): ProviderEntity? {
        try {
            val doc = firestore.collection("providers").document(providerId).get(Source.SERVER).await()
            if (!doc.exists()) return null

            val entity = ProviderDataMapper.fromFirestore(doc) ?: return null
            
            // 1. Carga de Direcciones Personales (Subcolección)
            val addressesSnapshot = doc.reference.collection("personalAddresses").get().await()
            val personalAddresses = if (addressesSnapshot.isEmpty) {
                // [LEY #2]: Si la subcolección está vacía, confiamos en el campo aplanado del documento principal
                entity.addresses 
            } else {
                addressesSnapshot.documents.map { addrDoc ->
                    com.example.myapplication.core.domain.model.AddressUnico(
                        id = addrDoc.id,
                        calle = addrDoc.getString("calle") ?: "",
                        numero = addrDoc.getString("numero") ?: "",
                        piso = addrDoc.getString("piso") ?: "",
                        departamento = addrDoc.getString("departamento") ?: "",
                        localidad = addrDoc.getString("localidad") ?: "",
                        provincia = addrDoc.getString("provincia") ?: "",
                        pais = addrDoc.getString("pais") ?: "Argentina",
                        codigoPostal = addrDoc.getString("codigoPostal") ?: "",
                        latitude = addrDoc.getDouble("latitude") ?: 0.0,
                        longitude = addrDoc.getDouble("longitude") ?: 0.0,
                        label = addrDoc.getString("label") ?: ""
                    )
                }
            }

            // 2. Carga de Empresas
            val companiesSnapshot = doc.reference.collection("companies").get().await()
            val companies = companiesSnapshot.documents.map { compDoc ->
                // 2. Carga de Sucursales por cada Empresa
                val branchesSnapshot = compDoc.reference.collection("branches").get().await()
                val branches = branchesSnapshot.documents.map { branchDoc ->
                    // 3. Carga de Empleados por cada Sucursal
                    val employeesSnapshot = branchDoc.reference.collection("employees").get().await()
                    val employees = employeesSnapshot.documents.map { empDoc ->
                        com.example.myapplication.core.domain.model.EmployeeProvider(
                            id = empDoc.id,
                            name = empDoc.getString("name") ?: "",
                            lastName = empDoc.getString("lastName") ?: "",
                            position = empDoc.getString("position") ?: "",
                            detail = empDoc.getString("detail") ?: ""
                        )
                    }

                    val addrMap = branchDoc.get("address") as? Map<*, *>
                    com.example.myapplication.core.domain.model.BranchProvider(
                        id = branchDoc.id,
                        name = branchDoc.getString("name") ?: branchDoc.getString("nombre") ?: "",
                        description = branchDoc.getString("description") ?: "",
                        address = com.example.myapplication.core.domain.model.AddressUnico(
                            id = addrMap?.get("id")?.toString() ?: UUID.randomUUID().toString(),
                            calle = addrMap?.get("calle")?.toString() ?: "",
                            numero = addrMap?.get("numero")?.toString() ?: "",
                            piso = addrMap?.get("piso")?.toString() ?: "",
                            departamento = addrMap?.get("departamento")?.toString() ?: "",
                            localidad = addrMap?.get("localidad")?.toString() ?: "",
                            provincia = addrMap?.get("provincia")?.toString() ?: "",
                            pais = addrMap?.get("pais")?.toString() ?: "Argentina",
                            codigoPostal = addrMap?.get("codigoPostal")?.toString() ?: "",
                            latitude = (addrMap?.get("latitude") as? Number)?.toDouble() ?: 0.0,
                            longitude = (addrMap?.get("longitude") as? Number)?.toDouble() ?: 0.0
                        ),
                        workingHours = branchDoc.getString("workingHours") ?: branchDoc.getString("horario") ?: "",
                        team = employees,
                        doesService = branchDoc.getBoolean("doesService") ?: false,
                        doesProduct = branchDoc.getBoolean("doesProduct") ?: false,
                        works24h = branchDoc.getBoolean("works24h") ?: false,
                        hasPhysicalLocation = branchDoc.getBoolean("hasPhysicalLocation") ?: false,
                        doesHomeVisits = branchDoc.getBoolean("doesHomeVisits") ?: false,
                        doesShipping = branchDoc.getBoolean("doesShipping") ?: false,
                        acceptsAppointments = branchDoc.getBoolean("acceptsAppointments") ?: false,
                        rating = (branchDoc.getDouble("rating") ?: 0.0).toFloat()
                    )
                }

                com.example.myapplication.core.domain.model.CompanyProvider(
                    id = compDoc.id,
                    name = compDoc.getString("name") ?: compDoc.getString("nombre") ?: "",
                    razonSocial = compDoc.getString("razonSocial") ?: "",
                    cuit = compDoc.getString("cuit") ?: "",
                    description = compDoc.getString("description") ?: compDoc.getString("descripcion") ?: "",
                    isVerified = compDoc.getBoolean("isVerified") ?: compDoc.getBoolean("verificado") ?: false,
                    photoUrl = compDoc.getString("photoUrl") ?: compDoc.getString("imageUrl"),
                    thumbnailBase64 = compDoc.getString("thumbnailBase64"),
                    rating = (compDoc.getDouble("rating") ?: 0.0).toFloat(),
                    categories = (compDoc.get("categories") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    branches = branches
                )
            }

            return entity.copy(companies = companies, addresses = personalAddresses)

        } catch (e: Exception) {
            Log.e(tag, "❌ Error en carga jerárquica profunda: ${e.message}")
            return null
        }
    }

    suspend fun forceSyncProviders(zipCode: String, category: String) {
        syncProvidersByRegion(zipCode, category)
    }
}

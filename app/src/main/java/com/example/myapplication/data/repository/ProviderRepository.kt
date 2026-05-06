package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.ProviderEntity
import com.example.myapplication.data.model.*
import com.example.myapplication.data.repository.util.ProviderMapper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * --- REPOSITORIO PARA PROVEEDORES ---
 * [RESTRUCTURADO] Gestiona la comunicación entre la fuente de datos (DAO) y la lógica de negocio.
 * Sincroniza datos desde Firebase Firestore a Room.
 */
@Singleton
class ProviderRepository @Inject constructor(
    private val providerDao: ProviderDao,
    private val firestore: FirebaseFirestore
) {

    // --- SECCIÓN: CONSULTAS DE LISTADOS ---

    /**
     * Obtiene todos los proveedores registrados en la base de datos local.
     */
    val allProviders: Flow<List<Provider>> = providerDao.getAllProviders().map { entities ->
        entities.map { it.toDomain() }
    }

    /**
     * Obtiene solo los proveedores marcados como favoritos.
     */
    val favoriteProviders: Flow<List<Provider>> = providerDao.getFavoriteProviders().map { entities ->
        entities.map { it.toDomain() }
    }

    // --- SECCIÓN: CONSULTAS INDIVIDUALES Y FILTROS ---

    /**
     * Obtiene un proveedor específico por su ID.
     */
    fun getProviderById(providerId: String): Flow<Provider?> {
        return providerDao.getProviderFlowById(providerId).map { it?.toDomain() }
    }

    /**
     * Carga el perfil completo de un prestador desde Firestore (incluye subcollections
     * de empresas, sucursales y empleados) y actualiza Room.
     * Usar al abrir la pantalla de detalle para datos frescos con priorizarEmpresa.
     */
    suspend fun fetchAndSyncProviderDetail(providerId: String) {
        try {
            val doc = firestore.collection("providers").document(providerId)
                .get(Source.SERVER).await()
            if (!doc.exists()) return

            // --- 1. Mapeo Base usando Mapper ---
            val baseEntity = ProviderMapper.fromFirestore(doc) ?: return

            // --- 2. Carga Profunda de Subcolecciones (Companies -> Branches -> Employees) ---
            val companiesSnapshot = doc.reference.collection("companies").get().await()
            val companiesList = mutableListOf<CompanyProvider>()
            for (compDoc in companiesSnapshot.documents) {
                val branchesSnapshot = compDoc.reference.collection("branches").get().await()
                val branchesList = mutableListOf<BranchProvider>()
                for (branchDoc in branchesSnapshot.documents) {
                    val employeesSnapshot = branchDoc.reference.collection("employees").get().await()
                    val employeesList = employeesSnapshot.documents.map { empDoc ->
                        EmployeeProvider(
                            id = empDoc.id,
                            name = empDoc.getString("name") ?: "",
                            lastName = empDoc.getString("lastName") ?: "",
                            position = empDoc.getString("position") ?: "",
                            detail = empDoc.getString("detail") ?: "",
                            photoUrl = empDoc.getString("photoUrl")
                        )
                    }
                    val addrMap = branchDoc.get("address") as? Map<*, *>
                    val branchAddr = AddressProvider(
                        id = addrMap?.get("id") as? String ?: UUID.randomUUID().toString(),
                        calle = addrMap?.get("calle") as? String ?: "",
                        numero = addrMap?.get("numero") as? String ?: "",
                        localidad = addrMap?.get("localidad") as? String ?: "",
                        provincia = addrMap?.get("provincia") as? String ?: "",
                        pais = addrMap?.get("pais") as? String ?: "Argentina",
                        codigoPostal = addrMap?.get("codigoPostal") as? String ?: "",
                        latitude = addrMap?.get("latitude") as? Double,
                        longitude = addrMap?.get("longitude") as? Double
                    )
                    branchesList.add(BranchProvider(
                        id = branchDoc.id,
                        name = branchDoc.getString("nombre") ?: branchDoc.getString("name") ?: "",
                        address = branchAddr,
                        workingHours = branchDoc.getString("horario") ?: branchDoc.getString("workingHours") ?: "",
                        employees = employeesList,
                        galleryImages = (branchDoc.get("galleryImages") as? List<*>)?.map { it.toString() } ?: emptyList(),
                        doesService = branchDoc.getBoolean("doesService") ?: false,
                        doesProduct = branchDoc.getBoolean("doesProduct") ?: false,
                        works24h = branchDoc.getBoolean("works24h") ?: false,
                        hasPhysicalLocation = branchDoc.getBoolean("hasPhysicalLocation") ?: false,
                        doesHomeVisits = branchDoc.getBoolean("doesHomeVisits") ?: false,
                        doesShipping = branchDoc.getBoolean("doesShipping") ?: false,
                        acceptsAppointments = branchDoc.getBoolean("acceptsAppointments") ?: false,
                        rating = (branchDoc.getDouble("rating") ?: 0.0).toFloat()
                    ))
                }
                companiesList.add(CompanyProvider(
                    id = compDoc.id,
                    name = compDoc.getString("nombreNegocio") ?: compDoc.getString("name") ?: "",
                    razonSocial = compDoc.getString("razonSocial") ?: "",
                    cuit = compDoc.getString("cuitNegocio") ?: compDoc.getString("cuit") ?: "",
                    description = compDoc.getString("descripcion") ?: compDoc.getString("description") ?: "",
                    rating = (compDoc.getDouble("rating") ?: 0.0).toFloat(),
                    photoUrl = compDoc.getString("photoUrl"),
                    bannerImageUrl = compDoc.getString("bannerImageUrl"),
                    categories = (compDoc.get("categories") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    isVerified = compDoc.getBoolean("verificado") ?: false,
                    branches = branchesList
                ))
            }

            // --- 3. Consolidación Final ---
            val finalEntity = baseEntity.copy(
                companies = companiesList,
                hasCompanyProfile = doc.getBoolean("hasCompanyProfile") ?: companiesList.isNotEmpty()
            )
            
            providerDao.insertAll(listOf(finalEntity))
            Log.d("ProviderRepo", "✅ Perfil profundo de ${doc.id} sincronizado")
        } catch (e: Exception) {
            Log.e("ProviderRepo", "Error fetching provider detail: ${e.message}")
        }
    }

    /**
     * Obtiene una lista de proveedores filtrada localmente por categoría.
     */
    suspend fun getProvidersByCategory(category: String): List<Provider> {
        return providerDao.getProvidersByCategory(category).map { it.toDomain() }
    }

    /**
     * Obtiene un flujo de proveedores filtrado por categoría y término de búsqueda.
     */
    fun getFilteredProviders(category: String, query: String): Flow<List<Provider>> {
        return providerDao.getProvidersByFilter(category, "%$query%").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Obtiene un flujo de proveedores filtrados por región (Código Postal) y categoría.
     * [ZERO COST]: Realiza la búsqueda en Room para entrega inmediata.
     */
    fun getProvidersByRegionAndCategory(zipCode: String, category: String): Flow<List<Provider>> {
        // En Maverick, la política es primero mostrar lo que hay en Room.
        // El ViewModel se encargará de disparar el refresco remoto si es necesario.
        return providerDao.getProvidersByCategoryFlow(category).map { entities ->
            var list = entities.map { it.toDomain() }
            
            // Filtro por código postal si se proporciona
            if (zipCode.isNotEmpty()) {
                list = list.filter { p ->
                    val zips = (p.addresses.map { it.codigoPostal } + listOfNotNull(p.address?.codigoPostal)).toSet()
                    zips.contains(zipCode)
                }
            }
            list
        }
    }

    /**
     * Busca en Firebase Firestore y sincroniza los resultados en Room.
     * [ACTUALIZADO] Mapeo usando Mapper Unificado.
     */
    suspend fun searchAndSyncProviders(zipCode: String, category: String) {
        val trimmedZip = zipCode.trim()
        val trimmedCategory = category.trim()
        try {
            Log.d("ProviderRepo", "📡 Consultando Firestore: '$trimmedCategory' en CP: '$trimmedZip'")

            // Intentamos primero la query combinada (requiere índice compuesto en Firestore)
            // Si falla (índice inexistente) o no hay resultados, hacemos fallback solo por categoría
            var snapshot = try {
                firestore.collection("providers")
                    .whereArrayContains("servicios", trimmedCategory)
                    .whereEqualTo("ubicacion.codigoPostal", trimmedZip)
                    .get().await()
                    .also { Log.d("ProviderRepo", "📥 Query combinada: ${it.size()} docs") }
            } catch (e: Exception) {
                Log.w("ProviderRepo", "⚠️ Query combinada falló (índice?): ${e.message}. Fallback solo por categoría.")
                null
            }

            // Fallback: buscar solo por categoría y filtrar CP en memoria
            if (snapshot == null || snapshot.isEmpty) {
                Log.d("ProviderRepo", "🔄 Fallback: buscando solo por categoría '$trimmedCategory'")
                snapshot = firestore.collection("providers")
                    .whereArrayContains("servicios", trimmedCategory)
                    .get().await()
                Log.d("ProviderRepo", "📥 Fallback devolvió ${snapshot.size()} docs")
            }

            Log.d("ProviderRepo", "📥 Firestore devolvió ${snapshot.size()} documentos")

            // 2. Mapeo usando Mapper Unificado
            val remoteProviders: List<ProviderEntity> = snapshot.documents.mapNotNull { doc ->
                ProviderMapper.fromFirestore(doc)
            }

            // 3. Guardar en Room (Disparará automáticamente los Flows en la UI)
            if (remoteProviders.isNotEmpty()) {
                providerDao.insertAll(remoteProviders)
                Log.d("ProviderRepo", "✅ ${remoteProviders.size} prestadores persistidos en Room")
            } else {
                Log.d("ProviderRepo", "ℹ️ No se encontraron nuevos resultados en Firestore para los filtros aplicados.")
            }

        } catch (e: Exception) {
            Log.e("ProviderRepo", "❌ Error en sincronización remota: ${e.message}")
        }
    }

    // --- SECCIÓN: OPERACIONES DE ACTUALIZACIÓN ---

    /**
     * Guarda o actualiza el perfil completo de un prestador.
     */
    suspend fun saveProviderProfile(provider: Provider) {
        val entity = ProviderEntity.fromDomain(provider)
        providerDao.insertAll(listOf(entity))
    }

    /**
     * Actualiza únicamente el estado de favorito de un proveedor.
     */
    suspend fun updateFavoriteStatus(providerId: String, isFavorite: Boolean) {
        providerDao.updateFavoriteStatus(providerId, isFavorite)
    }

    /**
     * Limpia la base de datos local de prestadores.
     */
    suspend fun clearProviders() {
        providerDao.clearAllProviders()
    }
}

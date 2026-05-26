package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.ProviderEntity
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.BranchProvider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.EmployeeProvider
import com.example.myapplication.data.model.Provider
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
     * Obtiene un proveedor específico por su ID en tiempo real (Flow).
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

            val perfil = doc.get("perfil") as? Map<*, *>
            val ubicacion = doc.get("ubicacion") as? Map<*, *>
            val empresaMap = doc.get("empresa") as? Map<*, *>

            // Leer companies subcollection
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

            val priorizarEmpresa = (empresaMap?.get("priorizarEmpresa") as? Boolean)
                ?: (doc.getBoolean("priorizarEmpresa") ?: false)

            val existing = providerDao.getProviderFlowById(providerId).first()
            val entity = (existing ?: ProviderEntity(
                id = doc.id,
                email = (perfil?.get("email") as? String) ?: "",
                displayName = (perfil?.get("nombre") as? String) ?: "",
                name = (perfil?.get("nombre") as? String) ?: "",
                lastName = (perfil?.get("apellido") as? String) ?: "",
                phoneNumber = (perfil?.get("telefono") as? String) ?: "",
                rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                categories = (doc.get("servicios") as? List<*>)?.map { it.toString() } ?: emptyList(),
                isSubscribed = doc.getBoolean("isSubscribed") ?: false,
                isVerified = doc.getBoolean("verificado") ?: false,
                photoUrl = (perfil?.get("imageUrl") as? String) ?: doc.getString("photoUrl"),
                bannerImageUrl = (perfil?.get("bannerUrl") as? String) ?: doc.getString("bannerImageUrl"),
                address = AddressProvider(
                    codigoPostal = ubicacion?.get("codigoPostal")?.toString() ?: "",
                    calle = (ubicacion?.get("direccion") ?: ubicacion?.get("calle"))?.toString() ?: "",
                    localidad = ubicacion?.get("localidad")?.toString() ?: "",
                    provincia = ubicacion?.get("provincia")?.toString() ?: "",
                    pais = ubicacion?.get("pais")?.toString() ?: "Argentina"
                ),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )).copy(
                companies = companiesList,
                hasCompanyProfile = doc.getBoolean("hasCompanyProfile") ?: companiesList.isNotEmpty(),
                priorizarEmpresa = priorizarEmpresa
            )
            providerDao.insertAll(listOf(entity))
        } catch (e: Exception) {
            Log.e("ProviderRepo", "Error fetching provider detail: ${e.message}")
        }
    }


    /**
     * Busca prestadores que pertenezcan a una categoría específica.
     */
    suspend fun getProvidersByCategory(category: String): List<Provider> {
        return providerDao.getProvidersByCategory(category).map { it.toDomain() }
    }

    /**
     * Obtiene prestadores filtrados localmente por código postal y categoría.
     */
    fun getFilteredProviders(zipCode: String, category: String): Flow<List<Provider>> {
        return providerDao.getProvidersByFilter(zipCode, category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // --- SECCIÓN: CONSULTAS LOCALES (Ahorro de Costos) ---

    /**
     * [NUEVO] Verifica si existen datos locales para una categoría y CP.
     * Si no hay, dispara la sincronización con Firebase.
     * Implementado según el Plan de Acción para minimizar costos de Firestore.
     */
    suspend fun getProvidersByRegionAndCategory(zipCode: String, category: String): Flow<List<Provider>> {
        // Cache check: si ya hay datos locales para esta categoría, evitamos llamar a Firestore.
        // Usamos solo la categoría (sin zip) porque el fallback de Firestore puede traer
        // prestadores de otras zonas y queremos mostrarlos igual.
        val localData = providerDao.getProvidersByCategory(category)

        if (localData.isEmpty()) {
            Log.d("ProviderRepo", "🔍 Local vacío para $category/${zipCode.ifBlank { "*" }}. Sincronizando desde Firebase...")
            searchAndSyncProviders(zipCode, category)
        } else {
            Log.d("ProviderRepo", "✅ Usando datos locales para $category (${localData.size} encontrados)")
        }

        // Retornamos TODOS los prestadores de esta categoría en Room,
        // sin filtrar por CP para no excluir resultados del fallback de Firestore.
        return providerDao.getProvidersByCategoryFlow(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // --- SECCIÓN: SINCRONIZACIÓN REMOTA (FIRESTORE) ---

    /**
     * Busca en Firebase Firestore y sincroniza los resultados en Room.
     * [ACTUALIZADO] Mapeo de sub-objetos (Ubicación y Empresas) con mayor robustez.
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
                    .whereEqualTo("visible", true)
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
                    .whereEqualTo("visible", true)
                    .whereArrayContains("servicios", trimmedCategory)
                    .get().await()
                Log.d("ProviderRepo", "📥 Fallback devolvió ${snapshot.size()} docs")
            }

            Log.d("ProviderRepo", "📥 Firestore devolvió ${snapshot.size()} documentos")

            // 2. Mapeo de Documentos a Entidades de Room (Estructura Compleja)
            val remoteProviders = snapshot.documents.mapNotNull { doc ->
                try {
                    val perfil = doc.get("perfil") as? Map<*, *> ?: emptyMap<String, Any>()
                    val ubicacion = doc.get("ubicacion") as? Map<*, *>

                    // --- MAPEO DE EMPRESAS (Sincronización de sub-entidades) ---
                    val companiesRaw = doc.get("empresas") as? List<Map<String, Any>> ?: emptyList()
                    val mappedCompanies = companiesRaw.map { c ->
                        CompanyProvider(
                            name = c["nombre"] as? String ?: "",
                            razonSocial = c["razonSocial"] as? String ?: "",
                            isVerified = c["verificado"] as? Boolean ?: false,
                            photoUrl = c["imageUrl"] as? String ?: c["photoUrl"] as? String,
                            rating = (c["rating"] as? Number)?.toFloat() ?: 0f
                        )
                    }

                    // Reconstruimos el modelo Provider con los datos de Firebase
                    // Buscamos campos tanto en la raíz como dentro de 'perfil' para mayor compatibilidad
                    val provider = Provider(
                        uid = doc.id,
                        email = (perfil["email"] as? String) ?: doc.getString("email") ?: "",
                        displayName = (perfil["nombre"] as? String) ?: doc.getString("nombre") ?: doc.getString("displayName") ?: "",
                        name = (perfil["nombre"] as? String) ?: doc.getString("nombre") ?: "",
                        lastName = (perfil["apellido"] as? String) ?: doc.getString("apellido") ?: "",
                        phoneNumber = (perfil["telefono"] as? String) ?: doc.getString("telefono") ?: "",
                        matricula = (perfil["matricula"] as? String) ?: doc.getString("matricula"),
                        cuilCuit = (perfil["dniCuit"] as? String) ?: doc.getString("cuilCuit"),
                        rating = doc.getDouble("rating")?.toFloat() ?: (perfil["rating"] as? Number)?.toFloat() ?: 0f,
                        isSubscribed = (doc.getBoolean("isSubscribed") ?: false) || (doc.getBoolean("suscripto") ?: false),
                        isVerified = doc.getBoolean("verificado") ?: doc.getBoolean("isVerified") ?: false,
                        
                        // --- CARACTERÍSTICAS REPLICADAS ---
                        doesService = doc.getBoolean("doesService") ?: false,
                        doesProduct = doc.getBoolean("doesProduct") ?: false,
                        works24h = doc.getBoolean("atencionUrgencias") ?: doc.getBoolean("works24h") ?: false,
                        doesHomeVisits = doc.getBoolean("vaDomicilio") ?: doc.getBoolean("doesHomeVisits") ?: false,
                        doesShipping = doc.getBoolean("envios") ?: doc.getBoolean("doesShipping") ?: false,
                        acceptsAppointments = doc.getBoolean("turnosEnLocal") ?: doc.getBoolean("acceptsAppointments") ?: false,
                        hasPhysicalLocation = doc.getBoolean("turnosEnLocal") ?: doc.getBoolean("hasPhysicalLocation") ?: false,

                        categories = (doc.get("servicios") as? List<*>)?.map { it.toString() } ?: emptyList(),
                        companies = mappedCompanies,
                        photoUrl = (perfil["imageUrl"] as? String) ?: (perfil["imageBase64"] as? String) ?: doc.getString("photoUrl"),
                        bannerImageUrl = (perfil["bannerUrl"] as? String) ?: doc.getString("bannerImageUrl"),
                        createdAt = doc.getLong("updatedAt") ?: doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        address = AddressProvider(
                            codigoPostal = ubicacion?.get("codigoPostal")?.toString() ?: zipCode,
                            calle = (ubicacion?.get("direccion") ?: ubicacion?.get("calle"))?.toString() ?: "",
                            localidad = ubicacion?.get("localidad")?.toString() ?: "",
                            provincia = doc.getString("provincia") ?: ubicacion?.get("provincia")?.toString() ?: "",
                            pais = ubicacion?.get("pais")?.toString() ?: "Argentina",
                            latitude = (ubicacion?.get("latitude") ?: ubicacion?.get("latitud")) as? Double,
                            longitude = (ubicacion?.get("longitude") ?: ubicacion?.get("longitud")) as? Double
                        )
                    )
                    ProviderEntity.fromDomain(provider)
                } catch (e: Exception) {
                    Log.e("ProviderRepo", "❌ Error mapeando documento ${doc.id}: ${e.message}")
                    null
                }
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

    // --- SECCIÓN: HORARIOS DESDE FIRESTORE ---
    /**
     * Lee los horarios de disponibilidad de una sucursal desde Firestore
     * y los formatea como texto legible para el cliente.
     */
    suspend fun fetchSchedulesForBranch(branchId: String): String {
        if (branchId.isBlank()) return ""
        return try {
            val snapshot = firestore.collection("availability_schedules")
                .whereEqualTo("providerId", branchId)
                .whereEqualTo("isActive", true)
                .get().await()

            if (snapshot.isEmpty) return ""

            val dayNames = mapOf(
                1 to "Lunes", 2 to "Martes", 3 to "Miércoles",
                4 to "Jueves", 5 to "Viernes", 6 to "Sábado", 7 to "Domingo"
            )

            // Agrupar por día y formatear
            snapshot.documents
                .mapNotNull { doc ->
                    val day = (doc.getLong("dayOfWeek") ?: return@mapNotNull null).toInt()
                    val start = doc.getString("startTime") ?: return@mapNotNull null
                    val end = doc.getString("endTime") ?: return@mapNotNull null
                    day to "$start - $end"
                }
                .groupBy({ it.first }, { it.second })
                .entries
                .sortedBy { it.key }
                .joinToString("\n") { (day, slots) ->
                    "${dayNames[day] ?: "Día $day"}: ${slots.joinToString(", ")}"
                }
        } catch (e: Exception) {
            Log.e("ProviderRepo", "Error cargando horarios de sucursal: ${e.message}")
            ""
        }
    }


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

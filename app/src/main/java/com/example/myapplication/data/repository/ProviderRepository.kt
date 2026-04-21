package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.local.ProviderDao
import com.example.myapplication.data.local.ProviderEntity
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
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
        val localData = providerDao.getProvidersByFilter(zipCode, category).first()

        if (localData.isEmpty()) {
            Log.d("ProviderRepo", "🔍 Local vacío para $category/$zipCode. Sincronizando desde Firebase...")
            searchAndSyncProviders(zipCode, category)
        } else {
            Log.d("ProviderRepo", "✅ Usando datos locales para $category/$zipCode (${localData.size} encontrados)")
        }

        return providerDao.getProvidersByFilter(zipCode, category).map { entities ->
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

            // 1. Consulta optimizada: solo traemos lo que coincide con el CP y la Categoría
            // Intentamos buscar por String (CP con letras o ceros a la izquierda)
            var snapshot = firestore.collection("providers")
                .whereArrayContains("servicios", trimmedCategory)
                .whereEqualTo("ubicacion.codigoPostal", trimmedZip)
                .get()
                .await()
            
            // Si no hay resultados y el CP es puramente numérico, intentamos como Number
            if (snapshot.isEmpty && trimmedZip.all { it.isDigit() }) {
                Log.d("ProviderRepo", "ℹ️ Sin resultados por String CP, reintentando como Number...")
                snapshot = firestore.collection("providers")
                    .whereArrayContains("servicios", trimmedCategory)
                    .whereEqualTo("ubicacion.codigoPostal", trimmedZip.toLong())
                    .get()
                    .await()
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
                        isSubscribed = doc.getBoolean("suscripto") ?: doc.getBoolean("isSubscribed") ?: false,
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

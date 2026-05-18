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
import com.google.firebase.firestore.Source

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
                "empresa" to mapOf(
                    "tieneEmpresa" to provider.hasCompanyProfile,
                    "priorizarEmpresa" to provider.priorizarEmpresa
                ),
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
                "turnosEnLocal" to provider.hasPhysicalLocation
                // Nota: el mapa "local" (con dirección, horario, turnosEnLocal) solo se escribe
                // desde EditProfileViewModel.updateProfile para evitar sobreescribir datos locales.
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
                    "nombre" to company.name, // Replicado para compatibilidad
                    "nombreNegocio" to company.name, // Replicado para compatibilidad
                    "razonSocial" to company.razonSocial,
                    "cuit" to company.cuit,
                    "cuitNegocio" to company.cuit, // Replicado para compatibilidad
                    "description" to company.description,
                    "descripcion" to company.description, // Replicado para compatibilidad
                    "photoUrl" to company.photoUrl,
                    "imageUrl" to company.photoUrl, // Replicado para compatibilidad
                    "bannerImageUrl" to company.bannerImageUrl,
                    "bannerUrl" to company.bannerImageUrl, // Replicado para compatibilidad
                    "email" to company.email,
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
            // Escribir local.turnosEnLocal por separado (update con dot-notation)
            // batch.set con merge no hace deep-merge en sub-mapas anidados
            firestore.collection("providers").document(uid)
                .update("local.turnosEnLocal", provider.hasPhysicalLocation)
                .await()

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

    /**
     * Obtiene datos del provider desde Firestore y actualiza roon.
     * Si Firestore fall, Room ya tiene los datos del último login.
     */
    suspend fun fetchAndUpdateFromFirestore(uid: String) {
        try {
            val doc = firestore.collection("providers").document(uid).get().await()
            if (!doc.exists()) return

            val perfil = doc.get("perfil") as? Map<String, Any>
            val nombre = perfil?.get("nombre") as? String ?: doc.getString("nombre") ?: ""
            val apellido = doc.getString("apellido") ?: ""
            val tipo = doc.getString("serviceType") ?: "TECHNICAL"
            val imagen = perfil?.get("imageBase64") as? String
                ?: doc.getString("imageBase64")
                ?: doc.getString("imageUrl")

            val existing = providerDao.getProviderByIdOnce(uid)
            val updated = existing?.copy(
                name = nombre, lastName = apellido,
                serviceType = tipo, photoUrl = imagen
            ) ?: ProviderEntity(
                id = uid, email = doc.getString("email") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: "",
                displayName = nombre, name = nombre, lastName = apellido,
                serviceType = tipo, photoUrl = imagen,
                createdAt = System.currentTimeMillis()
            )
            providerDao.insertProvider(updated)
            Log.d(TAG, "✅ Provider actualizado en Room desde Firestore")
        } catch (e: Exception) {
            Log.d(TAG, "⚠️ fetchAndUpdateFromFirestore: ${e.message} — usando Room")
        }
    }

    //Sección: MéTODOS MOVIDOS DESDE EditProfileViewModel

    // ─── SECCIÓN: MÉTODOS MOVIDOS DESDE EditProfileViewModel

    suspend fun updateProfilePhotoOnFirestore(userId: String, base64:
    String) {
        firestore.collection("providers").document(userId)
            .update(mapOf("perfil.imageUrl" to base64, "updatedAt" to
                    System.currentTimeMillis()))
            .await()
    }

    suspend fun updateBannerOnFirestore(userId: String, base64: String) {
        // "providers" corregido (era "providerss" con typo)
        firestore.collection("providers").document(userId)
            .update(mapOf("perfil.bannerImageUrl" to base64, "updatedAt" to
                    System.currentTimeMillis()))
            .await()
    }

    suspend fun updateGalleryImagesOnFirestore(userId: String, images:
    List<String>) {
        firestore.collection("providers").document(userId)
            .set(mapOf("galleryImages" to images), SetOptions.merge())
            .await()
    }

    /**
     * Carga el perfil completo desde Firestore (incluye addresses,
    companies,
     * branches y employees), guarda en Room y lo retorna.
     */
    suspend fun loadFullProfileFromFirestore(userId: String):
            ProviderEntity? {
        val doc =
            firestore.collection("providers").document(userId).get(Source.SERVER).await()
        if (!doc.exists()) return null

        val perfil = doc.get("perfil") as? Map<String, Any>
        val ubicacion = doc.get("ubicacion") as? Map<String, Any>
        val localMap = doc.get("local") as? Map<String, Any>
        val empresa = doc.get("empresa") as? Map<String, Any>
        val modalidad = doc.get("modalidad") as? Map<String, Any>

        fun str(map: Map<String, Any>?, key: String) = map?.get(key) as?
                String ?: doc.getString(key)
        fun bool(map: Map<String, Any>?, key: String, default: Boolean =
            false) =
            map?.get(key) as? Boolean ?: doc.getBoolean(key) ?: default

        // 1. Direcciones
        val addressesList = mutableListOf<com.example.myapplication.prestador.data.model.AddressProvider>()
        val addressesSnapshot =
            doc.reference.collection("addresses").get().await()
        if (!addressesSnapshot.isEmpty) {
            addressesSnapshot.documents.forEach { addrDoc ->
                addressesList.add(com.example.myapplication.prestador.data.model.AddressProvider(id = addrDoc.id,
                    calle = addrDoc.getString("calle") ?: "",
                    numero = addrDoc.getString("numero") ?: "",
                    localidad = addrDoc.getString("localidad") ?: "",
                    provincia = addrDoc.getString("provincia") ?: "",
                    pais = addrDoc.getString("pais") ?: "Argentina",
                    codigoPostal = addrDoc.getString("codigoPostal") ?: "",
                    latitude = addrDoc.getDouble("latitude"),
                    longitude = addrDoc.getDouble("longitude")
                ))
            }
        } else if (ubicacion != null) {
            addressesList.add(com.example.myapplication.prestador.data.model
                .AddressProvider(
                    id = "default",
                    calle = str(ubicacion, "direccion") ?: "",
                    numero = "",
                    localidad = "",
                    provincia = str(ubicacion, "provincia") ?: "",
                    pais = str(ubicacion, "pais") ?: "Argentina",
                    codigoPostal = str(ubicacion, "codigoPostal") ?: ""
                ))
        }
        if (bool(localMap, "turnosEnLocal")) {
            val localCalle = str(localMap, "direccionLocal") ?: ""
            val localProvincia = str(localMap, "provinciaLocal") ?: ""
            val localCp = str(localMap, "codigoPostalLocal") ?: ""
            if (localCalle.isNotBlank() || localProvincia.isNotBlank()) {
                addressesList.removeIf { it.id == "local" }
                addressesList.add(com.example.myapplication.prestador.data.model.AddressProvider(id = "local", calle = localCalle, numero = "",
                    localidad = "", provincia = localProvincia,
                    pais = "Argentina", codigoPostal = localCp
                ))
            }
        }

        // 2. Empresas / Sucursales / Empleados
        val companiesList = mutableListOf<com.example.myapplication.prestador.data.model.CompanyProvider>()
        val companiesSnapshot =
            doc.reference.collection("companies").get().await()
        for (compDoc in companiesSnapshot.documents) {
            val branchesList = mutableListOf<com.example.myapplication.prestador.data.model.BranchProvider>()
            val branchesSnapshot =
                compDoc.reference.collection("branches").get().await()
            for (branchDoc in branchesSnapshot.documents) {
                val employeesList = mutableListOf<com.example.myapplication.
                prestador.data.model.EmployeeProvider>()

                branchDoc.reference.collection("employees").get().await().documents.forEach {
                        empDoc ->
                    employeesList.add(com.example.myapplication.prestador.data.model.EmployeeProvider(id = empDoc.id,
                        name = empDoc.getString("name") ?: "",
                        lastName = empDoc.getString("lastName") ?: "",
                        position = empDoc.getString("position") ?: "",
                        detail = empDoc.getString("detail") ?: "",
                        photoUrl = empDoc.getString("photoUrl")
                    ))
                }
                val branchAddrMap = branchDoc.get("address") as? Map<String,
                        Any>
                val branchAddr = if (branchAddrMap != null) {

                    com.example.myapplication.prestador.data.model.AddressProvider(
                        id = branchAddrMap["id"] as? String ?:
                        UUID.randomUUID().toString(),
                        calle = branchAddrMap["calle"] as? String ?: "",
                        numero = branchAddrMap["numero"] as? String ?: "",
                        localidad = branchAddrMap["localidad"] as? String ?:
                        "",
                        provincia = branchAddrMap["provincia"] as? String ?:
                        "",
                        pais = branchAddrMap["pais"] as? String ?:
                        "Argentina",
                        codigoPostal = branchAddrMap["codigoPostal"] as?
                                String ?: "",
                        latitude = branchAddrMap["latitude"] as? Double,
                        longitude = branchAddrMap["longitude"] as? Double
                    )
                } else {

                    com.example.myapplication.prestador.data.model.AddressProvider(
                        id = branchDoc.getString("direccionId") ?:
                        UUID.randomUUID().toString(),
                        calle = branchDoc.getString("direccionId") ?: "",
                        provincia = branchDoc.getString("provincia") ?: "",
                        codigoPostal = branchDoc.getString("codigoPostal")
                            ?: ""
                    )
                }
                branchesList.add(com.example.myapplication.prestador.data.model.BranchProvider(id = branchDoc.id,
                    name = branchDoc.getString("nombre") ?:
                    branchDoc.getString("name") ?: "",
                    address = branchAddr,
                    workingHours = branchDoc.getString("horario") ?:
                    branchDoc.getString("workingHours") ?: "",
                    employees = employeesList,
                    galleryImages = (branchDoc.get("galleryImages") as?
                            List<*>)?.map { it.toString() } ?: emptyList(),
                    doesService = branchDoc.getBoolean("doesService") ?:
                    false,
                    doesProduct = branchDoc.getBoolean("doesProduct") ?:
                    false,
                    works24h = branchDoc.getBoolean("works24h") ?: false,
                    hasPhysicalLocation =
                        branchDoc.getBoolean("hasPhysicalLocation") ?: false,
                    doesHomeVisits = branchDoc.getBoolean("doesHomeVisits")
                        ?: false,
                    doesShipping = branchDoc.getBoolean("doesShipping") ?:
                    false,
                    acceptsAppointments =
                        branchDoc.getBoolean("acceptsAppointments") ?: false,
                    rating = (branchDoc.getDouble("rating") ?:
                    0.0).toFloat()
                ))
            }
            companiesList.add(com.example.myapplication.prestador.data.model
                .CompanyProvider(
                    id = compDoc.id,
                    name = compDoc.getString("nombreNegocio") ?:
                    compDoc.getString("name") ?: "",
                    razonSocial = compDoc.getString("razonSocial") ?: "",
                    cuit = compDoc.getString("cuitNegocio") ?:
                    compDoc.getString("cuit") ?: "",
                    email = compDoc.getString("email") ?: "",
                    description = compDoc.getString("descripcion") ?:
                    compDoc.getString("description") ?: "",
                    rating = (compDoc.getDouble("rating") ?: 0.0).toFloat(),
                    photoUrl = compDoc.getString("photoUrl"),
                    categories = (compDoc.get("categories") as? List<*>)?.map {
                        it.toString() } ?: emptyList(),
                    isVerified = compDoc.getBoolean("verificado") ?: false,
                    branches = branchesList
                ))
        }

        val savedServiceType =
            providerDao.getProviderByIdOnce(userId)?.serviceType
        val provider = ProviderEntity(
            id = userId,
            name = str(perfil, "nombre") ?: "",
            lastName = str(perfil, "apellido") ?: "",
            displayName = "${str(perfil, "nombre")} ${str(perfil,
                "apellido")}".trim(),
            email = str(perfil, "email") ?: "",
            emails = (doc.get("emails") as? List<*>)?.map { it.toString() }
                ?: listOfNotNull(str(perfil, "email")),
            phoneNumber = str(perfil, "telefono") ?: "",
            photoUrl = str(perfil, "imageUrl")?.takeIf { it.isNotBlank() }
                ?: str(perfil, "imageBase64")?.takeIf { it.isNotBlank() }
                ?: doc.getString("imageUrl")?.takeIf { it.isNotBlank() }
                ?: doc.getString("imageBase64")?.takeIf { it.isNotBlank() },
            description = str(perfil, "description") ?: "",
            cuilCuit = str(perfil, "dniCuit"),
            profesion = str(perfil, "profesion"),
            matricula = str(perfil, "matricula"),
            addresses = addressesList,
            address = addressesList.firstOrNull(),
            companies = companiesList,
            hasCompanyProfile = bool(empresa, "tieneEmpresa"),
            priorizarEmpresa = bool(empresa, "priorizarEmpresa") ||
                    (doc.getBoolean("priorizarEmpresa") ?: false),
            works24h = doc.getBoolean("atencionUrgencias") ?:
            bool(modalidad, "atencionUrgencias"),
            doesHomeVisits = doc.getBoolean("vaDomicilio") ?:
            bool(modalidad, "vaDomicilio"),
            hasPhysicalLocation = (localMap?.get("turnosEnLocal") as?
                    Boolean) ?: doc.getBoolean("turnosEnLocal") ?: false,
            doesShipping = doc.getBoolean("envios") ?: bool(modalidad,
                "envios"),
            acceptsAppointments = doc.getBoolean("acceptsAppointments") ?:
            bool(localMap, "turnosEnLocal"),
            trabajaConOtros = doc.getBoolean("trabajaConOtros") ?:
            bool(empresa, "trabajaConOtros"),
            isVerified = doc.getBoolean("verificado") ?:
            doc.getBoolean("isVerified") ?: false,
            isSubscribed = true,
            doesService = doc.getBoolean("doesService") ?: false,
            doesProduct = doc.getBoolean("doesProduct") ?: false,
            rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
            categories = (doc.get("servicios") as? List<*>)?.map {
                it.toString() } ?: emptyList(),
            createdAt = doc.getLong("createdAt") ?:
            System.currentTimeMillis(),
            workingHours = str(localMap, "horarioLocal") ?: "",
            galleryImages = (doc.get("galleryImages") as? List<*>)?.map {
                it.toString() } ?: emptyList(),
            serviceType = savedServiceType ?: "TECHNICAL"
        )

        providerDao.insertProvider(provider)
        // Temporal: mantener isSubscribed=true en Firestore hasta que haya lógica de pagos

        firestore.collection("providers").document(userId).update("isSubscribed",
            true).await()
        Log.d(TAG, "✅ Perfil completo cargado desde Firestore y guardado en Room")
            return provider
    }

}

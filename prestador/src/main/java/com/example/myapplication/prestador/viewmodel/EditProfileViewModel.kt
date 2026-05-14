package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.model.PrestadorProfileMode
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.example.myapplication.prestador.utils.ServiceTypeConfig
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.data.model.EmployeeProvider
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.example.myapplication.prestador.data.repository.ServiciosRepository

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val providerRepository: ProviderRepository,
    // private val businessRepository: BusinessRepository,
    // private val companiesFirestoreSync: CompaniesFirestoreSync,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val serviciosRepository: ServiciosRepository,
    // private val sucursalRepository: SucursalRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _servicios = MutableStateFlow<List<ServicioFirebase>>(emptyList())
    val servicios: StateFlow<List<ServicioFirebase>> = _servicios

    private val _loadingServicios = MutableStateFlow(false)
    val loadingServicios: StateFlow<Boolean> = _loadingServicios

    init {
        viewModelScope.launch {
            _loadingServicios.value = true
            try {
                _servicios.value = serviciosRepository.getServicios()
            } catch (e: Exception) {
                // Si falla Firebase, queda lista vaciaa
            } finally {
                _loadingServicios.value = false
            }
        }
    }
    
    // Estado del modo de visualización del perfil
    private val _profileMode = MutableStateFlow(PrestadorProfileMode.PERSONAL)
    val profileMode: StateFlow<PrestadorProfileMode> = _profileMode.asStateFlow()
    
    // ID del business del prestador (si tiene empresa)
    private val _businessId = MutableStateFlow<String?>(null)
    val businessId: StateFlow<String?> = _businessId.asStateFlow()

    private val _galleryImages = MutableStateFlow("[]")
    val galleryImages: StateFlow<String> = _galleryImages.asStateFlow()

    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    private val _bussinesEntity = MutableStateFlow<BusinessEntity?>(null)
    val businessEntity: StateFlow<BusinessEntity?> = _bussinesEntity.asStateFlow()
    
    // Configuración de tipo de servicio
    private val _serviceTypeConfig = MutableStateFlow(getServiceTypeConfig(ServiceType.TECHNICAL))
    val serviceTypeConfig: StateFlow<ServiceTypeConfig> = _serviceTypeConfig.asStateFlow()

    // Tick que incrementa cada vez que un refresh de Firebase completa (para pull-to-refresh)
    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    //Edit Mode
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun setEditMode(enabled: Boolean) { _isEditMode.value = enabled }
    fun toogleEditMode() { _isEditMode.value = !_isEditMode.value}


    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _photoUploadState.value = PhotoUploadState.Loading
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")

                // Comprimir a 150x150 y convertir a Base64
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                val resized = android.graphics.Bitmap.createScaledBitmap(originalBitmap, 150, 150, true)
                val outputStream = java.io.ByteArrayOutputStream()
                resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                val base64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)

                // Guardar Base64 en Firestore usando el campo correcto
                firestore.collection("providers")
                    .document(userId)
                    .update(mapOf(
                        "perfil.imageUrl" to base64,
                        "updatedAt" to System.currentTimeMillis()
                    ))
                    .await()

                // Actualizar Room directamente con el método dedicado
                providerRepository.updateProviderImage(userId, base64)

                // Refrescar UI con el estado actualizado
                val currentProvider = providerRepository.getProviderByIdOnce(userId)
                if (currentProvider != null) {
                    _profileState.value = ProfileState.Success(currentProvider)
                }
                _photoUploadState.value = PhotoUploadState.Success(base64)

            } catch (e: Exception) {
                _photoUploadState.value = PhotoUploadState.Error(e.message ?: "Error al subir foto")
            }
        }
    }
    fun uploadCompanyPhoto(uri: Uri, companyId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid?: return@launch

                //Mismo metodo que el chat
                val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(context, uri, maxWidth = 400, maxHeight = 400, quality = 75)
                    ?: return@launch
                val base64 = com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(bytes)

                //GUARDAR EN ROOM
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
                val updateCompanies = current.companies.map { company ->
                    if ( company.id == companyId) company.copy(photoUrl = base64)
                    else company
                }
                val updatedProvider = current.copy(companies = updateCompanies)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                _profileState.value = ProfileState.Success(updatedProvider)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileViewModel", "Error subiendo foto empresa: ${e.message}")
            }
        }
    }

    /**
     * [NUEVO] Sube el Banner de la Empresa
     */
    fun uploadCompanyBannerPhoto(uri: Uri, companyId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(context, uri, maxWidth = 800, maxHeight = 300, quality = 80)
                    ?: return@launch
                val base64 = com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(bytes)

                // GUARDAR EN ROOM Y FIREBASE
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
                val updateCompanies = current.companies.map { company ->
                    if (company.id == companyId) company.copy(bannerImageUrl = base64)
                    else company
                }
                val updatedProvider = current.copy(companies = updateCompanies)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                _profileState.value = ProfileState.Success(updatedProvider)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileViewModel", "Error subiendo banner empresa: ${e.message}")
            }
        }
    }


    fun uploadBannerPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(
                    context, uri, maxWidth = 800, maxHeight = 300, quality = 80
                ) ?: return@launch
                val base64 = com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(bytes)
                firestore.collection("providerss")
                    .document(userId)
                    .update(mapOf(
                        "perfil.bannerImageUrl" to base64,
                        "updateAt" to System.currentTimeMillis()
                    ))
                    .await()
                val current = (profileState.value as?
                        ProfileState.Success)?.provider ?: return@launch
                val updated = current.copy(bannerImageUrl = base64)
                providerRepository.saveProvider(updated)
                _profileState.value = ProfileState.Success(updated)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileViewModel", "Error subiendo banner: ${e.message}")
            }
        }
    }

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                _profileState.value = ProfileState.Error("Usuario no autenticado")
                return@launch
            }
            // 1. Mostrar datos de Room inmediatamente (sin flash de Loading)
            val cached = providerRepository.getProviderByIdOnce(userId)
            if (cached != null) {
                _profileState.value = ProfileState.Success(cached)
            } else {
                _profileState.value = ProfileState.Loading
            }
            // 2. Refrescar desde Firebase en segundo plano
            try {
                loadFromFirebase(userId)
            } catch (e: Exception) {
                if (_profileState.value is ProfileState.Loading) {
                    _profileState.value = ProfileState.Error(e.message ?: "Error al cargar perfil")
                }
            }
        }
    }

    /** Llamado desde pull-to-refresh: recarga directo de Firebase sin mostrar loading global. */
    fun refreshProfile() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: run {
                _refreshTick.value++
                return@launch
            }
            try {
                loadFromFirebase(userId)
            } catch (_: Exception) {
            } finally {
                _refreshTick.value++
            }
        }
    }

    private suspend fun loadFromFirebase(userId: String) {
        try {
            val doc = firestore.collection("providers").document(userId).get(Source.SERVER).await()
            if (doc.exists()) {
                // Leer grupos anidados
                val perfil = doc.get("perfil") as? Map<String, Any>
                val ubicacion = doc.get("ubicacion") as? Map<String, Any>
                val localMap = doc.get("local") as? Map<String, Any>
                val empresa = doc.get("empresa") as? Map<String, Any>
                val modalidad = doc.get("modalidad") as? Map<String, Any>

                fun str(map: Map<String, Any>?, key: String) =
                    map?.get(key) as? String ?: doc.getString(key)

                fun bool(map: Map<String, Any>?, key: String, default: Boolean = false) =
                    map?.get(key) as? Boolean ?: doc.getBoolean(key) ?: default

                // --- RECONSTRUIR JERARQUÍA ---
                
                // 1. Direcciones
                val addressesList = mutableListOf<AddressProvider>()
                val addressesSnapshot = doc.reference.collection("addresses").get().await()
                if (!addressesSnapshot.isEmpty) {
                    addressesSnapshot.documents.forEach { addrDoc ->
                        addressesList.add(AddressProvider(
                            id = addrDoc.id,
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
                    addressesList.add(AddressProvider(
                        id = "default",
                        calle = str(ubicacion, "direccion") ?: "",
                        numero = "",
                        localidad = "",
                        provincia = str(ubicacion, "provincia") ?: "",
                        pais = str(ubicacion, "pais") ?: "Argentina",
                        codigoPostal = str(ubicacion, "codigoPostal") ?: ""
                    ))
                }

                // Agregar dirección del local/taller si turnosEnLocal está activo
                if (bool(localMap, "turnosEnLocal")) {
                    val localCalle = str(localMap, "direccionLocal") ?: ""
                    val localProvincia = str(localMap, "provinciaLocal") ?: ""
                    val localCp = str(localMap, "codigoPostalLocal") ?: ""
                    if (localCalle.isNotBlank() || localProvincia.isNotBlank()) {
                        addressesList.removeIf { it.id == "local" }
                        addressesList.add(AddressProvider(
                            id = "local",
                            calle = localCalle,
                            numero = "",
                            localidad = "",
                            provincia = localProvincia,
                            pais = "Argentina",
                            codigoPostal = localCp
                        ))
                    }
                }

                // 2. Empresas y Sucursales
                val companiesList = mutableListOf<CompanyProvider>()
                val companiesSnapshot = doc.reference.collection("companies").get().await()
                
                for (compDoc in companiesSnapshot.documents) {
                    val branchesList = mutableListOf<BranchProvider>()
                    val branchesSnapshot = compDoc.reference.collection("branches").get().await()
                    
                    for (branchDoc in branchesSnapshot.documents) {
                        val employeesList = mutableListOf<EmployeeProvider>()
                        val employeesSnapshot = branchDoc.reference.collection("employees").get().await()
                        for (empDoc in employeesSnapshot.documents) {
                            employeesList.add(EmployeeProvider(
                                id = empDoc.id,
                                name = empDoc.getString("name") ?: "",
                                lastName = empDoc.getString("lastName") ?: "",
                                position = empDoc.getString("position") ?: "",
                                detail = empDoc.getString("detail") ?: "",
                                photoUrl = empDoc.getString("photoUrl")
                            ))
                        }

                        val branchAddrMap = branchDoc.get("address") as? Map<String, Any>
                        val branchAddr = if (branchAddrMap != null) {
                            AddressProvider(
                                id = branchAddrMap["id"] as? String ?: UUID.randomUUID().toString(),
                                calle = branchAddrMap["calle"] as? String ?: "",
                                numero = branchAddrMap["numero"] as? String ?: "",
                                localidad = branchAddrMap["localidad"] as? String ?: "",
                                provincia = branchAddrMap["provincia"] as? String ?: "",
                                pais = branchAddrMap["pais"] as? String ?: "Argentina",
                                codigoPostal = branchAddrMap["codigoPostal"] as? String ?: "",
                                latitude = branchAddrMap["latitude"] as? Double,
                                longitude = branchAddrMap["longitude"] as? Double
                            )
                        } else {
                            AddressProvider(
                                id = branchDoc.getString("direccionId") ?: UUID.randomUUID().toString(),
                                calle = branchDoc.getString("direccionId") ?: "",
                                provincia = branchDoc.getString("provincia") ?: "",
                                codigoPostal = branchDoc.getString("codigoPostal") ?: ""
                            )
                        }
                        
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
                        email = compDoc.getString("email") ?: "",
                        description = compDoc.getString("descripcion") ?: compDoc.getString("description") ?: "",
                        rating = (compDoc.getDouble("rating") ?: 0.0).toFloat(),
                        photoUrl = compDoc.getString("photoUrl"),
                        categories = (compDoc.get("categories") as? List<*>)?.map { it.toString() } ?: emptyList(),
                        isVerified = compDoc.getBoolean("verificado") ?: false,
                        branches = branchesList
                    ))
                }

                // Preservar service type de room
                val savedServiceType = providerRepository.getProviderByIdOnce(userId)?.serviceType
                val provider = ProviderEntity(
                    id = userId,
                    name = str(perfil, "nombre") ?: "",
                    lastName = str(perfil, "apellido") ?: "",
                    displayName = "${str(perfil, "nombre")} ${str(perfil, "apellido")}".trim(),
                    email = str(perfil, "email") ?: "",
                    emails = (doc.get("emails") as? List<*>)?.map { it.toString() } ?: listOfNotNull(str(perfil, "email")),
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
                    priorizarEmpresa = bool(empresa, "priorizarEmpresa") || (doc.getBoolean("priorizarEmpresa") ?: false),

                    works24h = doc.getBoolean("atencionUrgencias") ?: bool(modalidad, "atencionUrgencias"),
                    doesHomeVisits = doc.getBoolean("vaDomicilio") ?: bool(modalidad, "vaDomicilio"),
                    hasPhysicalLocation = (localMap?.get("turnosEnLocal") as? Boolean)
                        ?: doc.getBoolean("turnosEnLocal")
                        ?: false,
                    doesShipping = doc.getBoolean("envios") ?: bool(modalidad, "envios"),
                    acceptsAppointments = doc.getBoolean("acceptsAppointments") ?: bool(localMap, "turnosEnLocal"),
                        trabajaConOtros = doc.getBoolean("trabajaConOtros") ?: bool(empresa, "trabajaConOtros"),
                    
                    isVerified = doc.getBoolean("verificado") ?: doc.getBoolean("isVerified") ?: false,


                //***********************************************************************************************
                    //isSubscribed = doc.getBoolean("suscripto") ?: doc.getBoolean("isSubscribed") ?: false,

                    // --- MODIFICACIÓN PARA PRUEBAS: Default suscripción true ---
                    isSubscribed = true,
                    // --- FIN MODIFICACIÓN ---

               //******************************************************************************************************
                    doesService = doc.getBoolean("doesService") ?: false,
                    doesProduct = doc.getBoolean("doesProduct") ?: false,

                    rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
                    categories = (doc.get("servicios") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    workingHours = str(localMap, "horarioLocal") ?: "",
                    galleryImages = (doc.get("galleryImages") as? List<*>)?.map { it.toString() } ?: emptyList(),
                    serviceType = savedServiceType ?: "TECHNICAL"  //Room nunca sobreescribe lo que ya se guardo en room
                )
                
                // ORDEN CRÍTICO: 1. Guardar Provider (SSOT incluye jerarquía completa)
                providerRepository.saveProvider(provider)
                //forzar isSubscribed = true en Firebase hasta que haya logica de pago
                firestore.collection("providers").document(userId)
                    .update("isSubscribed", true).await()
                


                _galleryImages.value = org.json.JSONArray(provider.galleryImages).toString()
                _profileState.value = ProfileState.Success(provider)
                
                // Sincronizar el modo con tieneEmpresa
                _profileMode.value = if (provider.hasCompanyProfile) {
                    PrestadorProfileMode.EMPRESA
                } else {
                    PrestadorProfileMode.PERSONAL
                }
                
                // Actualizar configuración de tipo de servicio
                _serviceTypeConfig.value = getServiceTypeConfig(
                    ServiceType.fromString(doc.getString("serviceType") ?: "TECHNICAL")
                )
                
                // Actualizar businessId y businessEntity para compatibilidad
                if (provider.hasCompanyProfile) {
                    val firstComp = provider.companies.firstOrNull()
                    if (firstComp != null) {
                        _businessId.value = firstComp.id
                        _bussinesEntity.value = BusinessEntity(
                            id = firstComp.id,
                            providerId = userId,
                            nombreNegocio = firstComp.name,
                            razonSocial = firstComp.razonSocial,
                            cuitNegocio = firstComp.cuit,
                            direccion = firstComp.branches.firstOrNull()?.address?.fullString() ?: "",
                            codigoPostal = firstComp.branches.firstOrNull()?.address?.codigoPostal ?: "",
                            createdAt = System.currentTimeMillis()
                        )
                    }
                }
            } else {
                _profileState.value = ProfileState.Error("Perfil no encontrado")
            }
        } catch (e: Exception) {
            _profileState.value = ProfileState.Error(e.message ?: "Error al cargar desde Firebase")
            e.printStackTrace()
        }
    }

    fun updateProfile(
        name: String? = null,
        apellido: String? = null,
        email: String? = null,
        phone: String? = null,
        description: String? = null,
        address: String? = null,
        dniCuit: String? = null,
        profesion: String? = null,
        tieneMatricula: Boolean? = null,
        matricula: String? = null,
        provincia: String? = null,
        codigoPostal: String? = null,
        pais: String? = null,
        atencionUrgencias: Boolean? = null,
        vaDomicilio: Boolean? = null,
        turnosEnLocal: Boolean? = null,
        direccionLocal: String? = null,
        provinciaLocal: String? = null,
        codigoPostalLocal: String? = null,
        tieneEmpresa: Boolean? = null,
        trabajaConOtros: Boolean? = null,
        envios: Boolean? = null,
        nombreEmpresa: String? = null,
        cuitEmpresa: String? = null,
        direccionEmpresa: String? = null,
        serviceType: String? = null,
        horarioLocal: String? = null,
        doesService: Boolean? = null,
        doesProduct: Boolean? = null,
        categorias: String? = null,
        latitud: Double? = null,
        longitud: Double? = null,
        priorizarEmpresa: Boolean? = null,
        acceptsAppointments: Boolean? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                
                val currentProvider = providerRepository.getProviderByIdOnce(userId)
                    ?: run {
                        loadFromFirebase(userId)
                        providerRepository.getProviderByIdOnce(userId)
                            ?: throw Exception("Perfil no encontrado")
                    }
                
                val updatedProvider = currentProvider.copy(
                    name = name ?: currentProvider.name,
                    lastName = apellido ?: currentProvider.lastName,
                    displayName = if (name != null || apellido != null) {
                        "${name ?: currentProvider.name} ${apellido ?: currentProvider.lastName}".trim()
                    } else currentProvider.displayName,
                    email = email ?: currentProvider.email,
                    phoneNumber = phone ?: currentProvider.phoneNumber,
                    description = description ?: currentProvider.description,
                    cuilCuit = dniCuit ?: currentProvider.cuilCuit,
                    profesion = profesion ?: currentProvider.profesion,
                    matricula = matricula ?: currentProvider.matricula,
                    works24h = atencionUrgencias ?: currentProvider.works24h,
                    doesHomeVisits = vaDomicilio ?: currentProvider.doesHomeVisits,
                    hasPhysicalLocation = turnosEnLocal ?: currentProvider.hasPhysicalLocation,
                    doesShipping = envios ?: currentProvider.doesShipping,
                    acceptsAppointments = acceptsAppointments ?: currentProvider.acceptsAppointments,
                    hasCompanyProfile = tieneEmpresa ?: currentProvider.hasCompanyProfile,
                    trabajaConOtros = trabajaConOtros ?: currentProvider.trabajaConOtros,
                    doesService = doesService ?: currentProvider.doesService,
                    doesProduct = doesProduct ?: currentProvider.doesProduct,
                    workingHours = horarioLocal ?: currentProvider.workingHours,
                    companies = currentProvider.companies,
                    addresses = currentProvider.addresses,
                    address = currentProvider.address,
                    photoUrl = currentProvider.photoUrl,
                    bannerImageUrl = currentProvider.bannerImageUrl,
                    galleryImages = currentProvider.galleryImages,
                    isSubscribed = currentProvider.isSubscribed,
                    isVerified = currentProvider.isVerified,
                    rating = currentProvider.rating,
                    categories = categorias?.let { 
                        val arr = org.json.JSONArray(it)
                        (0 until arr.length()).map { i -> arr.getString(i) }
                    } ?: currentProvider.categories,
                    serviceType = serviceType ?: currentProvider.serviceType
                )
                
                // Asegurar que el registro padre exista antes de cualquier operación dependiente
                providerRepository.saveProvider(updatedProvider)
                
                // 🔥 [NUEVO] Sincronización de Topics de FCM (Costo Cero)
                syncTopics(
                    cp = (codigoPostal ?: updatedProvider.address?.codigoPostal)?.ifBlank { null },
                    categories = updatedProvider.categories,
                    isSubscribed = updatedProvider.isSubscribed
                )
                
                // Actualizar en Firebase
                val updateData = hashMapOf<String, Any>("updatedAt" to System.currentTimeMillis())
                if (name != null) updateData["perfil.nombre"] = name
                if (apellido != null) updateData["perfil.apellido"] = apellido
                if (email != null) updateData["perfil.email"] = email
                if (phone != null) updateData["perfil.telefono"] = phone
                if (description != null) updateData["perfil.description"] = description
                if (dniCuit != null) updateData["perfil.dniCuit"] = dniCuit
                if (profesion != null) updateData["perfil.profesion"] = profesion
                if (tieneMatricula != null) updateData["perfil.tieneMatricula"] = tieneMatricula
                if (matricula != null) updateData["perfil.matricula"] = matricula

                if (!address.isNullOrBlank()) {
                    updateData["ubicacion.direccion"] = address
                    updateData["ubicacion.calle"] = address // Replicado de App Cliente
                }
                if (!codigoPostal.isNullOrBlank()) {
                    updateData["ubicacion.codigoPostal"] = codigoPostal
                    updateData["codigoPostal"] = codigoPostal
                }
                if (!provincia.isNullOrBlank()) {
                    updateData["ubicacion.provincia"] = provincia
                    updateData["provincia"] = provincia
                }
                if (pais != null) updateData["ubicacion.pais"] = pais
                if (latitud != null) {
                    updateData["ubicacion.latitud"] = latitud
                    updateData["ubicacion.latitude"] = latitud // Replicado de App Cliente
                    updateData["latitud"] = latitud
                }
                if (longitud != null) {
                    updateData["ubicacion.longitud"] = longitud
                    updateData["ubicacion.longitude"] = longitud // Replicado de App Cliente
                    updateData["longitud"] = longitud
                }

                if (turnosEnLocal != null) {
                    updateData["local.turnosEnLocal"] = turnosEnLocal
                    updateData["turnosEnLocal"] = turnosEnLocal
                }
                updateData["isSubscribed"] = true // Mock o lógica de suscripción si aplica

                // --- REPLICACIÓN DE CAMPOS DE APP CLIENTE EN RAÍZ ---
                if (name != null) updateData["nombre"] = name
                if (apellido != null) updateData["apellido"] = apellido
                if (email != null) updateData["email"] = email
                if (phone != null) updateData["telefono"] = phone
                if (description != null) updateData["description"] = description
                if (atencionUrgencias != null) updateData["isVerified"] = true // Ejemplo de consistencia
                if (direccionLocal != null) updateData["local.direccionLocal"] = direccionLocal
                if (provinciaLocal != null) updateData["local.provinciaLocal"] = provinciaLocal
                if (codigoPostalLocal != null) updateData["local.codigoPostalLocal"] = codigoPostalLocal
                if (horarioLocal != null) updateData["local.horarioLocal"] = horarioLocal

                if (tieneEmpresa != null) updateData["empresa.tieneEmpresa"] = tieneEmpresa
                if (priorizarEmpresa != null) updateData["empresa.priorizarEmpresa"] = priorizarEmpresa
                if (nombreEmpresa != null) {
                    updateData["empresa.nombreEmpresa"] = nombreEmpresa
                    updateData["empresa.razonSocial"] = nombreEmpresa
                }
                if (cuitEmpresa != null) updateData["empresa.cuitEmpresa"] = cuitEmpresa
                if (direccionEmpresa != null) updateData["empresa.direccionEmpresa"] = direccionEmpresa
                if (trabajaConOtros != null) updateData["empresa.trabajaConOtros"] = trabajaConOtros

                if (atencionUrgencias != null) updateData["atencionUrgencias"] = atencionUrgencias
                if (vaDomicilio != null) updateData["vaDomicilio"] = vaDomicilio
                if (envios != null) updateData["envios"] = envios
                if (acceptsAppointments != null) updateData["acceptsAppointments"] = acceptsAppointments
                if (doesService != null) updateData["doesService"] = doesService
                if (doesProduct != null) updateData["doesProduct"] = doesProduct

                if (email != null) {
                    updateData["emails"] = listOf(email).filter { it.isNotBlank() }
                }

                if (serviceType != null) updateData["serviceType"] = serviceType
                if (categorias != null) {
                    val arr = org.json.JSONArray(categorias)
                    val list = (0 until arr.length()).map { arr.getString(it) }
                    updateData["servicios"] = list
                }

                // --- SINCRONIZACIÓN SSOT ---
                // Sincronizar en Firebase y Room usando el Repositorio Central
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                
                /*
                // Gestionar BusinessEntity (Obsoleto: syncProviderWithFirebase maneja la jerarquía)
                if (updatedProvider.hasCompanyProfile && !nombreEmpresa.isNullOrBlank()) {
                    val businesses = businessRepository.getBusinessesByProvider(userId).first()
                    val existingBusiness = businesses.firstOrNull()
                    if (existingBusiness != null) {
                        val updatedBusiness = existingBusiness.copy(
                            nombreNegocio = nombreEmpresa,
                            razonSocial = nombreEmpresa,
                            cuitNegocio = cuitEmpresa ?: existingBusiness.cuitNegocio,
                            direccion = direccionEmpresa ?: existingBusiness.direccion,
                            codigoPostal = existingBusiness.codigoPostal,
                            updatedAt = System.currentTimeMillis()
                        )
                        businessRepository.updateBusiness(updatedBusiness)
                        companiesFirestoreSync.subirCompany(updatedBusiness, userId)
                        _businessId.value = existingBusiness.id
                        _bussinesEntity.value = updatedBusiness
                    } else {
                        val newBusinessId = UUID.randomUUID().toString()
                        val newBusiness = BusinessEntity(
                            id = newBusinessId,
                            providerId = userId,
                            nombreNegocio = nombreEmpresa,
                            razonSocial = nombreEmpresa,
                            cuitNegocio = cuitEmpresa ?: "",
                            direccion = direccionEmpresa ?: "",
                            codigoPostal = "", // TODO: agregar código postal de empresa
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        businessRepository.saveBusiness(newBusiness)
                        companiesFirestoreSync.subirCompany(newBusiness, userId)
                        _businessId.value = newBusinessId
                        _bussinesEntity.value = newBusiness
                    }
                } else if (tieneEmpresa == false) {
                    _businessId.value = null
                }
                */
                
                _updateState.value = UpdateState.Success
                _profileState.value = ProfileState.Success(updatedProvider)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Error al actualizar perfil")
                e.printStackTrace()
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
    
    fun toggleProfileMode() {
        _profileMode.update { currentMode ->
            if (currentMode == PrestadorProfileMode.PERSONAL) {
                PrestadorProfileMode.EMPRESA
            } else {
                PrestadorProfileMode.PERSONAL
            }
        }
    }

    fun updateImagenesProductos(json: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val updatedCompanies = current.companies.map { company ->
            // Si tiene múltiples empresas, aquí se podría filtrar por ID.
            // Por simplicidad, actualizamos la primera si existe.
            company.copy(branches = company.branches.map { branch ->
                branch.copy(galleryImages = try {
                    val arr = org.json.JSONArray(json)
                    (0 until arr.length()).map { arr.getString(it) }
                } catch (e: Exception) { branch.galleryImages })
            })
        }
        val updatedProvider = current.copy(companies = updatedCompanies)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    fun updateGalleryImages(json: String) {
        _galleryImages.value = json
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val arr = org.json.JSONArray(json)
                val list = (0 until arr.length()).map { arr.getString(it) }
                firestore.collection("providers")
                    .document(userId)
                    .set(mapOf("galleryImages" to list), SetOptions.merge())
                    .await()
                val current = (profileState.value as? ProfileState.Success)?.provider
                if (current != null) {
                    val updated = current.copy(galleryImages = list)
                    providerRepository.saveProvider(updated)
                    _profileState.value = ProfileState.Success(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addGalleryImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(
                    context, uri, maxWidth = 800, maxHeight = 800, quality = 75
                ) ?: return@launch
                val base64 = com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(bytes)
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
                val updatedList = current.galleryImages + base64
                updateGalleryImages(org.json.JSONArray(updatedList).toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeGalleryImage(imageData: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val updateList = current.galleryImages.filter { it != imageData }
        updateGalleryImages(org.json.JSONArray(updateList).toString())
    }

    fun updateCategorias(json: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val list = try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { current.categories }
        
        val updatedProvider = current.copy(categories = list)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    fun updateHorarioCasaCentral(horario: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val updatedCompanies = current.companies.map { company ->
            company.copy(branches = company.branches.mapIndexed { index, branch ->
                if (index == 0) branch.copy(workingHours = horario) else branch
            })
        }
        val updatedProvider = current.copy(companies = updatedCompanies)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    // --- MÉTODOS JERÁRQUICOS SSOT ---

    fun addEmployee(employee: EmployeeProvider) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        // Por defecto agregamos a la primera sucursal de la primera empresa (Casa Central)
        val updatedCompanies = current.companies.toMutableList()
        if (updatedCompanies.isEmpty()) {
            updatedCompanies.add(CompanyProvider(name = current.nombreEmpresa ?: "Empresa Principal"))
        }
        
        val firstCompany = updatedCompanies[0]
        val updatedBranches = firstCompany.branches.toMutableList()
        if (updatedBranches.isEmpty()) {
            updatedBranches.add(BranchProvider(name = "Casa Central"))
        }
        
        val firstBranch = updatedBranches[0]
        updatedBranches[0] = firstBranch.copy(employees = firstBranch.employees + employee)
        updatedCompanies[0] = firstCompany.copy(branches = updatedBranches)
        
        val updatedProvider = current.copy(companies = updatedCompanies)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    fun removeEmployee(employeeId: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        var foundCompanyId: String? = null
        var foundBranchId: String? = null
        current.companies.forEach { company ->
            company.branches.forEach { branch ->
                if (branch.employees.any { it.id == employeeId }) {
                    foundCompanyId = company.id
                    foundBranchId = branch.id
                }
            }
        }
        val updatedCompanies = current.companies.map { company ->
            company.copy(branches = company.branches.map { branch ->
                branch.copy(employees = branch.employees.filter { it.id != employeeId })
            })
        }
        val updatedProvider = current.copy(companies = updatedCompanies)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            if (foundCompanyId != null && foundBranchId != null) {
                providerRepository.deleteEmployeeFromFirebase(foundCompanyId!!, foundBranchId!!, employeeId)
            }
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    fun saveAdditionalAddress(address: AddressProvider) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val updatedAddresses = current.addresses.filter { it.id != address.id } + address
        // Actualizar también el campo `address` (singular) para que la UI lo muestre después de guardar
        val updatedProvider = current.copy(addresses = updatedAddresses, address = address)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    fun removeAdditionalAddress(addressId: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val updatedAddresses = current.addresses.filter { it.id != addressId }
        val updatedProvider = current.copy(addresses = updatedAddresses)
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            providerRepository.deleteAddressFromFirebase(addressId)
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    fun updateCompanyBannerPhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
                val company = current.companies.firstOrNull() ?: return@launch
                val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(
                    context, uri, maxWidth = 1200, maxHeight = 400, quality = 80
                )
                val base64 = bytes?.let { com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(it) } ?: return@launch
                val updated = company.copy(bannerImageUrl = base64)
                val updatedCompanies = current.companies.map { if (it.id == updated.id) updated else it }
                val updatedProvider = current.copy(companies = updatedCompanies)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                _profileState.value = ProfileState.Success(updatedProvider)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileViewModel", "Error banner empresa: ${e.message}")
            }
        }
    }


    fun addCompany(company: CompanyProvider, photoUri: android.net.Uri? = null) {
        viewModelScope.launch {
            try {
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch

                // Procesar foto en el mismo coroutine para evitar race condition
                val finalCompany = if (photoUri != null) {
                    val bytes = com.example.myapplication.prestador.utils.ImageUtils.compressImageToWebP(
                        context, photoUri, maxWidth = 400, maxHeight = 400, quality = 75
                    )
                    if (bytes != null) company.copy(photoUrl = com.example.myapplication.prestador.utils.ImageUtils.bytesToBase64(bytes))
                    else company
                } else company

                // Reemplazar si ya existe el mismo ID, si no agregar
                val updatedCompanies = if (current.companies.any { it.id == finalCompany.id }) {
                    current.companies.map { if (it.id == finalCompany.id) finalCompany else it }
                } else {
                    current.companies + finalCompany
                }

                val updatedProvider = current.copy(companies = updatedCompanies, hasCompanyProfile = true)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                _profileState.value = ProfileState.Success(updatedProvider)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileViewModel", "Error guardando empresa: ${e.message}")
            }
        }
    }



    fun removeCompany(companyId: String) {
        val current = (profileState.value as? ProfileState.Success)?.provider ?: return
        val updatedCompanies = current.companies.filter { it.id != companyId }
        val updatedProvider = current.copy(
            companies = updatedCompanies,
            hasCompanyProfile = updatedCompanies.isNotEmpty()
        )
        viewModelScope.launch {
            providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
            providerRepository.deleteCompanyFromFirebase(companyId)
            _profileState.value = ProfileState.Success(updatedProvider)
        }
    }

    /**
     * ── SECCIÓN: NOTIFICACIONES POR TEMA (FCM Topics) ─────────────────────────────────────────
     * Suscribe al prestador a los temas de licitaciones según su CP y Rubros.
     * Solo si es usuario Premium (isSubscribed).
     */
    fun syncTopics(cp: String?, categories: List<String>, isSubscribed: Boolean) {
        if (cp.isNullOrBlank() || categories.isEmpty()) {
            Log.w("FCM_TOPIC", "No se puede sincronizar topics: CP o Categorías vacíos. CP: $cp, Cats: $categories")
            return
        }

        val fcm = FirebaseMessaging.getInstance()
        // 🔥 [VALIDACIÓN DE FLUJO] Normalización idéntica a la App Cliente
        val cleanCp = cp.normalizeForTopic()

        Log.d("FCM_FLOW", "Sincronizando Topics para Prestador - CP: $cleanCp (Premium: $isSubscribed)")

        categories.forEach { cat ->
            val cleanCat = cat.normalizeForTopic()
            val topicName = "tender_${cleanCp}_$cleanCat"
            
            Log.d("FCM_FLOW", "Procesando Tópico: $topicName")

            // ─── SECCIÓN: LÓGICA DE SUSCRIPCIÓN (Premium Incentives) ─────────────────────
            // Mantenemos la suscripción activa si es premium.
            if (isSubscribed) {
                fcm.subscribeToTopic(topicName)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("FCM_TOPIC", "✅ Suscrito con éxito a: $topicName")
                        } else {
                            Log.e("FCM_TOPIC", "❌ Error al suscribirse a $topicName: ${task.exception?.message}")
                        }
                    }
            } else {
                // Si no es premium, nos desuscribimos para no recibir los mensajes 
                // del topic, ya que el servicio maneja la lógica de upsell.
                fcm.unsubscribeFromTopic(topicName)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("FCM_TOPIC", "📴 Desuscrito de: $topicName")
                        }
                    }
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val provider: ProviderEntity) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

sealed class PhotoUploadState {
    object Idle : PhotoUploadState()
    object Loading : PhotoUploadState()
    data class Success(val url: String) : PhotoUploadState()
    data class Error(val message: String) : PhotoUploadState()
}

/**
 * --- EXTENSIONES DE NORMALIZACIÓN PARA TÓPICOS ---
 */
private fun String.removeAccents(): String {
    val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
}

fun String.normalizeForTopic(): String {
    return this.removeAccents()
        .replace(" ", "_")
        .replace("(", "")
        .replace(")", "")
        .replace(Regex("[^a-zA-Z0-9-_.~%]"), "") // Solo caracteres permitidos por FCM
        .lowercase()
}

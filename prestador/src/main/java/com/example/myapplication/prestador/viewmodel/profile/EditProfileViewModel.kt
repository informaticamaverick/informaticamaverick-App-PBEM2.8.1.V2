package com.example.myapplication.prestador.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.core.utils.normalizeForTopic
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.model.PrestadorProfileMode
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.example.myapplication.prestador.utils.ServiceTypeConfig
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import com.example.myapplication.core.domain.model.AddressProvider
import com.example.myapplication.core.domain.model.BranchProvider
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.EmployeeProvider
import com.example.myapplication.prestador.data.model.ServicioFirebase
import com.example.myapplication.prestador.data.repository.ServiciosRepository

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val providerRepository: ProviderRepository,
    // private val businessRepository: BusinessRepository,
    // private val companiesFirestoreSync: CompaniesFirestoreSync,
    private val auth: FirebaseAuth,
    private val serviciosRepository: ServiciosRepository,
    // private val sucursalRepository: SucursalRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private var loadProfileJob: Job? = null

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _servicios = MutableStateFlow<List<ServicioFirebase>>(emptyList())
    val servicios: StateFlow<List<ServicioFirebase>> = _servicios

    private val _loadingServicios = MutableStateFlow(false)
    val loadingServicios: StateFlow<Boolean> = _loadingServicios
    
    private var isLoadingProfile = false

    init {
        // Cargar servicios en background sin bloquear la UI
        viewModelScope.launch {
            _loadingServicios.value = true
            try {
                _servicios.value = serviciosRepository.getServicios()
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Error cargando servicios: ${e.message}")
                // Si falla Firebase, queda lista vacia
            } finally {
                _loadingServicios.value = false
            }
        }
        // Cargar perfil solo una vez
        loadProfile()
    }
    
    // Estado del modo de visualizaci?n del perfil
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
    
    // Configuraci?n de tipo de servicio
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

    private val _companyError = MutableStateFlow<String?>(null)
    val companyError: StateFlow<String?> = _companyError.asStateFlow()

    fun clearCompanyError() { _companyError.value = null }

    fun toggleModoEmpresa(activarEmpresa: Boolean) {
        viewModelScope.launch {
            val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
            val uid = auth.currentUser?.uid ?: return@launch

            // Cancelar carga en background para evitar que sobreescriba el estado
            loadProfileJob?.cancel()

            // Actualizar estado local y Room PRIMERO (fuente de verdad)
            val updated = current.copy(priorizarEmpresa = activarEmpresa)
            _profileState.value = ProfileState.Success(updated)
            providerRepository.saveProvider(updated)

            // Firebase: fire-and-forget (no revertir en error para no trabar el switch)
            try {
                providerRepository.actualizarModoEmpresa(uid, activarEmpresa)
                val companyIds = current.companies.map { it.id }
                providerRepository.actualizarVisibilidadPerfil(uid, activarEmpresa, companyIds)
            } catch (e: Exception) {
                android.util.Log.e("EditProfileVM", "Error Firebase toggleModoEmpresa: ${e.message}")
            }
        }
    }

    fun refreshFromRoom() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val cached = providerRepository.getProviderByIdOnce(userId) ?: return@launch
            _profileState.value = ProfileState.Success(cached)
        }
    }




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
                providerRepository.updateProfilePhotoOnFirestore(userId, base64)

                // Actualizar Room directamente con el m?todo dedicado
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
                val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(context, uri, maxWidth = 400, maxHeight = 400, quality = 75)
                    ?: return@launch
                val base64 = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)

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

                val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(context, uri, maxWidth = 800, maxHeight = 300, quality = 80)
                    ?: return@launch
                val base64 = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)

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
                val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(
                    context, uri, maxWidth = 800, maxHeight = 300, quality = 80
                ) ?: return@launch
                val base64 = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)
                // GUARDAR EN ROOM Y FIREBASE (typo "providerss" corregido en el repositorio)
                providerRepository.updateBannerOnFirestore(userId, base64)
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


    fun loadProfile() {
        // Evitar llamadas múltiples simultáneas (verificar Y setear ANTES de lanzar coroutine)
        synchronized(this) {
            if (isLoadingProfile) {
                Log.d("EditProfileViewModel", "⏭️ Ya está cargando, ignorando llamada")
                return
            }
            isLoadingProfile = true
        }
        
        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: run {
                    Log.e("EditProfileViewModel", "❌ Usuario no autenticado")
                    _profileState.value = ProfileState.Error("Usuario no autenticado")
                    return@launch
                }
                Log.d("EditProfileViewModel", "📥 Cargando perfil para userId: $userId")
                
                // 1. Mostrar datos de Room inmediatamente (sin flash de Loading)
                val cached = providerRepository.getProviderByIdOnce(userId)
                if (cached != null) {
                    Log.d("EditProfileViewModel", "✅ Perfil encontrado en Room")
                    _profileState.value = ProfileState.Success(cached)
                } else {
                    Log.d("EditProfileViewModel", "⏳ No hay perfil en Room, mostrando Loading")
                    _profileState.value = ProfileState.Loading
                }
                
                // 2. Refrescar desde Firebase en segundo plano con timeout
                withContext(Dispatchers.IO) {
                    try {
                        Log.d("EditProfileViewModel", "🔄 Cargando desde Firebase...")
                        withTimeout(10_000) { // 10 segundos timeout
                            loadFromFirebase(userId)
                        }
                        Log.d("EditProfileViewModel", "✅ Firebase completado")
                    } catch (e: TimeoutCancellationException) {
                        Log.w("EditProfileViewModel", "⏱️ Timeout cargando desde Firebase (10s)")
                        // No es error crítico, ya tenemos datos de Room
                    } catch (e: Exception) {
                        Log.e("EditProfileViewModel", "❌ Error cargando desde Firebase: ${e.message}", e)
                        if (_profileState.value is ProfileState.Loading) {
                            _profileState.value = ProfileState.Error(e.message ?: "Error al cargar perfil")
                        }
                    }
                }
            } finally {
                synchronized(this@EditProfileViewModel) {
                    isLoadingProfile = false
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
            Log.d("EditProfileViewModel", "🌐 Llamando loadFullProfileFromFirestore...")
            val provider = providerRepository.loadFullProfileFromFirestore(userId)
            if (provider != null) {
                Log.d("EditProfileViewModel", "✅ Perfil cargado desde Firebase: ${provider.displayName}")

                // Actualizar en el Main thread para garantizar que la UI se entere
                withContext(Dispatchers.Main) {
                    _galleryImages.value = org.json.JSONArray(provider.galleryImages).toString()
                    _profileState.value = ProfileState.Success(provider)
                    Log.d("EditProfileViewModel", "✅ Estado actualizado a Success en Main thread")
                    
                    // Sincronizar el modo con tieneEmpresa
                    _profileMode.value = if (provider.hasCompanyProfile) {
                        PrestadorProfileMode.EMPRESA
                    } else {
                        PrestadorProfileMode.PERSONAL
                    }
                    
                        // Actualizar configuraci?n de tipo de servicio
                        _serviceTypeConfig.value = getServiceTypeConfig(
                            ServiceType.fromString(provider.serviceType ?: "TECHNICAL")
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
                    }
            } else {
                    withContext(Dispatchers.Main) {
                        _profileState.value = ProfileState.Error("Perfil no encontrado")
                    }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                    _profileState.value = ProfileState.Error(e.message ?: "Error al cargar desde Firebase")
            }
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
            Log.d("EditProfileViewModel", "🟢 updateProfile llamado: name=$name, email=$email, phone=$phone, profesion=$profesion")
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
                
                // Asegurar que el registro padre exista antes de cualquier operaci?n dependiente
                providerRepository.saveProvider(updatedProvider)
                
                // ?? [NUEVO] Sincronizaci?n de Topics de FCM (Costo Cero)
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
                updateData["isSubscribed"] = true // Mock o l?gica de suscripci?n si aplica

                // --- REPLICACI?N DE CAMPOS DE APP CLIENTE EN RA?Z ---
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

                // Sincronizaci?n SSOT: Room + Firebase atómico
                Log.d("EditProfileViewModel", "🟡 Sincronizando con Firebase y Room...")
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())

                // ✅ VERIFICACIÓN: leer de vuelta de Room y comparar campos clave
                val savedInRoom = providerRepository.getProviderByIdOnce(userId)
                if (savedInRoom == null) {
                    Log.e("EditProfileViewModel", "❌ VERIFICACIÓN ROOM: registro no encontrado después de guardar!")
                } else {
                    val roomOk = savedInRoom.name == updatedProvider.name &&
                        savedInRoom.email == updatedProvider.email &&
                        savedInRoom.phoneNumber == updatedProvider.phoneNumber &&
                        savedInRoom.profesion == updatedProvider.profesion
                    if (roomOk) {
                        Log.d("EditProfileViewModel", "✅ VERIFICACIÓN ROOM: OK → name=${savedInRoom.name}, email=${savedInRoom.email}, phone=${savedInRoom.phoneNumber}")
                    } else {
                        Log.w("EditProfileViewModel", "⚠️ VERIFICACIÓN ROOM: discrepancia detectada!")
                        Log.w("EditProfileViewModel", "   Esperado → name=${updatedProvider.name}, email=${updatedProvider.email}, phone=${updatedProvider.phoneNumber}, profesion=${updatedProvider.profesion}")
                        Log.w("EditProfileViewModel", "   Room     → name=${savedInRoom.name}, email=${savedInRoom.email}, phone=${savedInRoom.phoneNumber}, profesion=${savedInRoom.profesion}")
                    }
                }

                Log.d("EditProfileViewModel", "✅ updateProfile completado exitosamente")
                
                /*
                // Gestionar BusinessEntity (Obsoleto: syncProviderWithFirebase maneja la jerarqu?a)
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
                            codigoPostal = "", // TODO: agregar c?digo postal de empresa
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

    // -- CAMBIAR CONTRASE?A ----------------------------------------------------
    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: run {
            _passwordChangeState.value = PasswordChangeState.Error("Usuario no autenticado")
            return
        }
        val email = user.email ?: run {
            _passwordChangeState.value = PasswordChangeState.Error("No se pudo obtener el email")
            return
        }
        viewModelScope.launch {
            _passwordChangeState.value = PasswordChangeState.Loading
            try {
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(newPassword).await()
                _passwordChangeState.value = PasswordChangeState.Success
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                _passwordChangeState.value = PasswordChangeState.Error("La contraseña actual es incorrecta")
            } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                _passwordChangeState.value = PasswordChangeState.Error("La contraseña nueva es muy débil")
            } catch (e: Exception) {
                _passwordChangeState.value = PasswordChangeState.Error(e.message ?: "Error al cambiar contraseña")
            }
        }
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState.Idle
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
            // Si tiene m?ltiples empresas, aqu? se podr?a filtrar por ID.
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
                providerRepository.updateGalleryImagesOnFirestore(userId, list)
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
                val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(
                    context, uri, maxWidth = 800, maxHeight = 800, quality = 75
                ) ?: return@launch
                val base64 = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)
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

    // --- M?TODOS JER?RQUICOS SSOT ---

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
        // Actualizar tambi?n el campo `address` (singular) para que la UI lo muestre despu?s de guardar
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

    fun updateCompanyBannerPhoto(uri: android.net.Uri, companyId: String? = null) {
        viewModelScope.launch {
            try {
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
                val company = (if (companyId != null) current.companies.find { it.id == companyId } else null)
                    ?: current.companies.firstOrNull() ?: return@launch
                val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(
                    context, uri, maxWidth = 1200, maxHeight = 400, quality = 80
                )
                val base64 = bytes?.let { com.example.myapplication.core.utils.ImageUtils.bytesToBase64(it) } ?: return@launch
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


    fun addCompany(company: CompanyProvider, photoUri: android.net.Uri? = null, priorizarEmpresa: Boolean? = null) {
        viewModelScope.launch {
            try {
                val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch

                // Procesar foto en el mismo coroutine para evitar race condition
                val finalCompany = if (photoUri != null) {
                    val bytes = com.example.myapplication.core.utils.ImageUtils.compressImageToWebP(
                        context, photoUri, maxWidth = 400, maxHeight = 400, quality = 75
                    )
                    if (bytes != null) company.copy(photoUrl = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes))
                    else company
                } else company

                // Reemplazar si ya existe el mismo ID, si no agregar
                val isUpdate = current.companies.any { it.id == finalCompany.id }

                if (!isUpdate) {
                    //MÁXIMO 3 empresas
                    if (current.companies.size >= 3) {
                        _companyError.value = "Solo podés tener hasta 3 empresa de distintas categorias"
                        return@launch
                    }
                    //Categorías únicas entre empresas
                    val categoriasExistentes = current.companies.flatMap { it.categories }.toSet()
                    val categoriasDuplicadas = finalCompany.categories.filter { it in categoriasExistentes }
                    if ( categoriasDuplicadas.isNotEmpty()) {
                        _companyError.value = "ya tenés una empresa con la categoría ${categoriasDuplicadas.first()}"
                        return@launch
                    }
                }

                val updatedCompanies = if (isUpdate) {
                    current.companies.map { if(it.id == finalCompany.id) finalCompany else it }
                } else {
                    current.companies + finalCompany
                }

                val updatedProvider = current.copy(
                    companies = updatedCompanies,
                    hasCompanyProfile = true,
                    priorizarEmpresa = priorizarEmpresa ?: current.priorizarEmpresa
                )
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
     * -- SECCIÓN: NOTIFICACIONES POR TEMA (FCM Topics) -----------------------------------------
     * Suscribe al prestador a los temas de licitaciones según su CP y Rubros.
     * Solo si es usuario Premium (isSubscribed).
     */
    fun syncTopics(cp: String?, categories: List<String>, isSubscribed: Boolean) {
        if (cp.isNullOrBlank() || categories.isEmpty()) {
            Log.w("FCM_TOPIC", "No se puede sincronizar topics: CP o Categorías vacíos. CP: $cp, Cats: $categories")
            return
        }

        val fcm = FirebaseMessaging.getInstance()
        // [VALIDACIÓN DE FLUJO] Normalización idéntica a la App Cliente
        val cleanCp = cp.normalizeForTopic()

        Log.d("FCM_FLOW", "Sincronizando Topics para Prestador - CP: $cleanCp (Premium: $isSubscribed)")

        categories.forEach { cat ->
            val cleanCat = cat.normalizeForTopic()
            val topicName = "tender_${cleanCp}_$cleanCat"
            
            Log.d("FCM_FLOW", "Procesando Tópico: $topicName")

            // --- SECCIÓN: LÓGICA DE SUSCRIPCIÓN (Premium Incentives) ---------------------
            // Mantenemos la suscripci?n activa si es premium.
            if (isSubscribed) {
                fcm.subscribeToTopic(topicName)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("FCM_TOPIC", "? Suscrito con ?xito a: $topicName")
                        } else {
                            Log.e("FCM_TOPIC", "? Error al suscribirse a $topicName: ${task.exception?.message}")
                        }
                    }
            } else {
                // Si no es premium, nos desuscribimos para no recibir los mensajes 
                // del topic, ya que el servicio maneja la l?gica de upsell.
                fcm.unsubscribeFromTopic(topicName)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("FCM_TOPIC", "?? Desuscrito de: $topicName")
                        }
                    }
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: return@launch
                providerRepository.deleteProvider(uid)
                auth.currentUser?.delete()?.await()
                auth.signOut()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al eliminar la cuenta")
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

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

/**
 * --- EXTENSIONES DE NORMALIZACI?N PARA T?PICOS ---
 */
private fun String.removeAccents(): String {
    val normalized = java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
    return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalized, "")
}




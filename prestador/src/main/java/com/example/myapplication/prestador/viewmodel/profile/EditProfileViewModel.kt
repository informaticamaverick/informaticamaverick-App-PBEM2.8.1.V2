package com.example.myapplication.prestador.viewmodel.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.ProviderRepository
import com.example.myapplication.core.data.repository.CategoryRepository
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.domain.model.toEntity
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.core.utils.normalizeForTopic
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.prestador.data.model.PrestadorProfileMode
import com.example.myapplication.prestador.utils.ServiceTypeConfig
import com.example.myapplication.prestador.utils.getServiceTypeConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject

/**
 * --- EDIT PROFILE VIEWMODEL (EL OBRERO MAESTRO) ---
 * [ELITE v3.5]: Centraliza toda la lógica de negocio, cálculos de GPS y sincronización SSOT.
 * Sigue la Ley #1: La UI es tonta, este ViewModel hace el trabajo sucio.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providerRepository: ProviderRepository,
    private val auth: FirebaseAuth,
    private val categoryRepository: CategoryRepository // Reemplaza ServiciosRepository (Ley #6)
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _editUiState = MutableStateFlow(EditProfileUiState())
    val editUiState: StateFlow<EditProfileUiState> = _editUiState.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _profileProgress = MutableStateFlow(0f)
    val profileProgress: StateFlow<Float> = _profileProgress.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _servicios = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val servicios: StateFlow<List<CategoryEntity>> = _servicios.asStateFlow()

    private var loadProfileJob: Job? = null
    private var initialEditProvider: Provider? = null

    private val _companyError = MutableStateFlow<String?>(null)
    val companyError: StateFlow<String?> = _companyError.asStateFlow()

    init {
        loadCatalogAndProfile()
    }

    private fun loadCatalogAndProfile() {
        viewModelScope.launch {
            // [LEY #6]: Soberanía Local de Catálogos - Mapeo en hilo de fondo
            categoryRepository.allCategories
                .flowOn(Dispatchers.Default)
                .collect { list ->
                    _servicios.value = list
                }
        }
        loadProfile()
    }

    /**
     * --- GESTIÓN DEL MODO EDICIÓN ---
     * @param enabled true para entrar a edición, false para salir.
     * Al entrar, se crea un 'Draft' del perfil actual en el UiState.
     * [LEY #2]: Los datos iniciales vienen de Room (SSOT local).
     */
    fun setEditMode(enabled: Boolean) {
        if (enabled) {
            val current = (profileState.value as? ProfileState.Success)?.provider
            current?.let { p ->
                _editUiState.value = EditProfileUiState(
                    name = p.name,
                    lastName = p.lastName,
                    displayName = p.displayName,
                    phoneNumber = p.phoneNumber,
                    email = p.email,
                    emails = p.emails,
                    cuilCuit = p.cuilCuit ?: "",
                    profesion = p.profesion ?: "",
                    matricula = p.matricula ?: "",
                    hasMatricula = !p.matricula.isNullOrBlank(),
                    description = p.description,
                    categories = p.categories,
                    addresses = p.addresses,
                    works24h = p.works24h,
                    hasPhysicalLocation = p.hasPhysicalLocation,
                    doesHomeVisits = p.doesHomeVisits,
                    doesShipping = p.doesShipping,
                    acceptsAppointments = p.acceptsAppointments,
                    trabajaConOtros = p.trabajaConOtros,
                    doesService = p.doesService,
                    doesProduct = p.doesProduct,
                    address = p.address ?: AddressUnico(),
                    localCalle = p.address?.calle ?: "",
                    localNumero = p.address?.numero ?: "",
                    localLocalidad = p.address?.localidad ?: "",
                    localProvincia = p.address?.provincia ?: "",
                    localCp = p.address?.codigoPostal ?: "",
                    localHorario = p.workingHours,
                    isDirty = false // Se inicializa en limpio
                )
            }
        } else {
            // [ELITE]: Limpieza de estados al salir sin guardar
            _editUiState.update { it.copy(isDirty = false) }
        }
        _isEditMode.value = enabled
    }

    /**
     * --- ACTUALIZACIÓN DEL DRAFT ---
     * @param transform función de transformación para el estado actual.
     * Cada vez que el usuario teclea o cambia un switch, marcamos isDirty = true.
     * [SIMPLICIDAD]: No comparamos con Firebase ni Room, solo detectamos interacción.
     */
    fun updateEditState(transform: (EditProfileUiState) -> EditProfileUiState) {
        _editUiState.update { current ->
            val newState = transform(current)
            newState.copy(isDirty = true) // Cualquier cambio marca el estado como sucio
        }
        calculateProgress()
    }

    /**
     * [ELITE GPS]: Obtiene la dirección a partir de coordenadas GPS.
     */
    fun obtenerDireccionDesdeGps(lat: Double, lng: Double, onResult: (AddressUnico?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addr = MaverickGeoUtils.getAddressFromCoordinates(context, lat, lng)
                withContext(Dispatchers.Main) { onResult(addr) }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error GPS: ${e.message}")
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    /**
     * [LEY #1]: Lógica de GPS delegada al ViewModel usando MaverickGeoUtils.
     */
    fun geocodificarLocal() {
        val state = _editUiState.value
        val query = "${state.localCalle} ${state.localNumero}, ${state.localLocalidad}, ${state.localProvincia}, Argentina"
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // [ELITE GEO]: Búsqueda de texto optimizada en el CORE
                val addr = MaverickGeoUtils.getAddressFromText(context, query)
                if (addr != null) {
                    withContext(Dispatchers.Main) {
                        updateEditState { it.copy(
                            localCalle = addr.calle,
                            localNumero = addr.numero,
                            localLocalidad = addr.localidad,
                            localProvincia = addr.provincia,
                            localCp = addr.codigoPostal,
                            address = addr.copy(label = "LOCAL")
                        ) }
                    }
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error geocoding local: ${e.message}")
            }
        }
    }

    /**
     * Geocodificación genérica para cualquier dirección (Usado por AddressBottomSheet).
     */
    fun geocodificarAddress(street: String, number: String, city: String, province: String, onResult: (AddressUnico?) -> Unit) {
        val query = "$street $number, $city, $province, Argentina"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addr = MaverickGeoUtils.getAddressFromText(context, query)
                withContext(Dispatchers.Main) {
                    onResult(addr)
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error geocoding address: ${e.message}")
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }


    /**
     * --- GUARDADO INTEGRAL SSOT ---
     * 1. Toma el UiState (Draft).
     * 2. Impacta en Room (Feedback instantáneo).
     * 3. Sincroniza con Firebase (Carga On-Demand).
     * [LEY #2 & #7]: Garantiza que el usuario vea sus cambios aunque no haya internet.
     */
    fun saveProfileChanges() {
        val s = _editUiState.value
        Log.d("EditProfileVM", "💾 [SAVE_PROFILE] Guardando perfil para: ${s.displayName}")

        viewModelScope.launch {
            val current = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
            _updateState.value = UpdateState.Loading

            // 1. Unificación de dirección principal (combina draft con coordenadas previas)
            val updatedMainAddress = s.address.copy(
                calle = s.localCalle.trim(),
                numero = s.localNumero.trim(),
                localidad = s.localLocalidad.trim(),
                provincia = s.localProvincia.trim(),
                codigoPostal = s.localCp.trim()
            )

            // 2. Construcción del objeto Provider según SSOT del Core
            val updatedProvider = current.copy(
                name = s.name.trim(),
                lastName = s.lastName.trim(),
                displayName = s.displayName.trim().ifBlank { "${s.name} ${s.lastName}".trim() },
                phoneNumber = s.phoneNumber.trim(),
                email = s.email.trim(),
                emails = s.emails,
                cuilCuit = s.cuilCuit.trim(),
                profesion = s.profesion.trim(),
                matricula = if (s.hasMatricula) s.matricula.trim() else "",
                description = s.description.trim(),
                categories = s.categories,
                addresses = s.addresses,
                address = updatedMainAddress,
                works24h = s.works24h,
                hasPhysicalLocation = s.hasPhysicalLocation,
                doesHomeVisits = s.doesHomeVisits,
                doesShipping = s.doesShipping,
                acceptsAppointments = s.acceptsAppointments,
                trabajaConOtros = s.trabajaConOtros,
                doesService = s.doesService,
                doesProduct = s.doesProduct,
                workingHours = s.localHorario
            )

            try {
                // 3. PERSISTENCIA ROOM (Prioridad Máxima)
                providerRepository.saveProvider(updatedProvider.toEntity())
                
                // 4. SINCRONIZACIÓN FIREBASE (Segundo Plano)
                providerRepository.syncProviderWithFirebase(updatedProvider)
                
                // 5. ACTUALIZACIÓN DE ESTADO Y RESET DE DIRTY
                _profileState.value = ProfileState.Success(updatedProvider)
                _editUiState.update { it.copy(isDirty = false) } // Ya no está sucio tras guardar
                _updateState.value = UpdateState.Success
            } catch (e: Exception) {
                Log.e("EditProfileVM", "❌ Error Crítico: ${e.message}")
                _updateState.value = UpdateState.Error(e.message ?: "Fallo al sincronizar")
            }
        }
    }

    fun loadProfile() {
        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            
            // 1. Carga instantánea de Room (Ley #2)
            val cached = providerRepository.getProviderByIdOnce(userId)
            if (cached != null) {
                val domain = cached.toDomain()
                _profileState.value = ProfileState.Success(domain)
                calculateProgress()
            } else {
                _profileState.value = ProfileState.Loading
            }

            // 2. Sincronización en segundo plano (Ley #5)
            try {
                withTimeout(8000L) {
                    val remote = providerRepository.loadFullProfileFromFirestore(userId)
                    if (remote != null) {
                        withContext(Dispatchers.Main) {
                            val domain = remote.toDomain()
                            _profileState.value = ProfileState.Success(domain)
                            if (!_isEditMode.value) {
                                // No pisar el draft si el usuario ya está editando (Blindaje)
                                calculateProgress()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("EditProfileVM", "Timeout sync: ${e.message}")
            }
        }
    }

    fun refreshFromRoom() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            providerRepository.getProviderByIdOnce(userId)?.let {
                _profileState.value = ProfileState.Success(it.toDomain())
            }
        }
    }

    fun refreshProfile() {
        loadProfile()
    }

    fun calculateProgress() {
        val p = if (_isEditMode.value) {
            val s = _editUiState.value
            Provider(uid="", displayName="", name=s.name, lastName=s.lastName, email="", phoneNumber=s.phoneNumber, categories=s.categories, description=s.description, profesion=s.profesion)
        } else {
            (profileState.value as? ProfileState.Success)?.provider ?: return
        }

        val fields = listOf(
            p.name.isNotBlank(),
            p.lastName.isNotBlank(),
            p.phoneNumber.isNotBlank(),
            p.photoUrl?.isNotBlank() == true,
            p.description.isNotBlank(),
            p.profesion?.isNotBlank() == true,
            p.categories.isNotEmpty(),
            p.address != null || p.companies.isNotEmpty()
        )
        _profileProgress.value = fields.count { it } / fields.size.toFloat()
    }

    fun validateProfile(phone: String, dniCuit: String, categoriesJson: String, photoUrl: String?): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()
        if (phone.length < 7) errors["phone"] = "Teléfono inválido"
        val cuitError = com.example.myapplication.prestador.utils.errorCuitMensaje(dniCuit)
        if (cuitError != null) errors["dniCuit"] = cuitError
        if (photoUrl.isNullOrBlank()) errors["photo"] = "Falta foto"
        return errors
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val bytes = com.example.myapplication.core.utils.ImageUtils.compressElite(context, uri)
                val thumbBase64 = com.example.myapplication.core.utils.ImageUtils.generateThumbnailBase64(context, uri)
                
                if (bytes != null) {
                    val base64 = com.example.myapplication.core.utils.ImageUtils.bytesToBase64(bytes)
                    providerRepository.updateProfilePhotoOnFirestore(userId, base64, thumbBase64)
                    refreshProfile()
                }
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error foto: ${e.message}")
            }
        }
    }

    /**
     * --- GESTIÓN DE EMPRESAS (ELITE DELEGATION) ---
     * @param company Objeto empresa a añadir o actualizar.
     * @param photoUri URI de la imagen si se está subiendo una nueva.
     * @param priorizarEmpresa Flag para activar el Modo Empresa (Soberanía de Empresa).
     * [LEY #6]: Si es la primera empresa, clonamos los datos comerciales del prestador
     * para evitar fatiga y asegurar consistencia en la transición.
     */
    fun addCompany(company: CompanyProvider, photoUri: Uri? = null, priorizarEmpresa: Boolean? = null) {
        viewModelScope.launch {
            val provider = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
            
            var updatedCompany = company
            
            // 1. [ELITE CLONING]: Si es la primera empresa y está vacía de datos comerciales, clonamos
            if (provider.companies.isEmpty() && updatedCompany.categories.isEmpty()) {
                Log.d("EditProfileVM", "🧬 [IDENTITY_DELEGATION] Clonando datos comerciales del prestador a la nueva empresa.")
                
                // Creamos una sucursal inicial (Casa Central) con los datos del prestador
                val initialBranch = com.example.myapplication.core.domain.model.BranchProvider(
                    name = "Casa Central",
                    address = provider.address ?: AddressUnico(),
                    workingHours = provider.workingHours,
                    doesService = provider.doesService,
                    doesProduct = provider.doesProduct,
                    works24h = provider.works24h,
                    hasPhysicalLocation = provider.hasPhysicalLocation,
                    doesHomeVisits = provider.doesHomeVisits,
                    doesShipping = provider.doesShipping,
                    acceptsAppointments = provider.acceptsAppointments
                )
                
                updatedCompany = updatedCompany.copy(
                    categories = provider.categories,
                    branches = listOf(initialBranch)
                )
            }

            // 2. Procesamiento de imagen (Thumbnail Elite)
            if (photoUri != null) {
                val bytes = ImageUtils.compressElite(context, photoUri)
                val thumb = ImageUtils.generateThumbnailBase64(context, photoUri)
                if (bytes != null) {
                    updatedCompany = updatedCompany.copy(
                        photoUrl = ImageUtils.bytesToBase64(bytes),
                        thumbnailBase64 = thumb
                    )
                }
            }

            val updatedCompanies = provider.companies.filter { it.id != updatedCompany.id } + updatedCompany
            
            // 3. Activación automática de PriorizarEmpresa si es la primera
            val finalPriorizar = priorizarEmpresa ?: (provider.companies.isEmpty() || provider.priorizarEmpresa)

            val updatedProvider = provider.copy(
                companies = updatedCompanies,
                priorizarEmpresa = finalPriorizar
            )
            
            // 4. Persistencia SSOT
            providerRepository.saveProvider(updatedProvider.toEntity())
            providerRepository.syncProviderWithFirebase(updatedProvider)
            loadProfile()
        }
    }

    /**
     * --- ELIMINACIÓN DE EMPRESA ---
     * [LEY #6]: Si no quedan más empresas, desactivamos el Modo Empresa automáticamente
     * para que el prestador vuelva a ser visible como profesional independiente.
     */
    fun removeCompany(companyId: String) {
        viewModelScope.launch {
            val provider = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
            val updatedCompanies = provider.companies.filter { it.id != companyId }
            
            // Si no quedan empresas, priorizarEmpresa debe ser false
            val finalPriorizar = if (updatedCompanies.isEmpty()) false else provider.priorizarEmpresa
            
            val updatedProvider = provider.copy(
                companies = updatedCompanies,
                priorizarEmpresa = finalPriorizar
            )
            
            providerRepository.saveProvider(updatedProvider.toEntity())
            providerRepository.deleteCompanyFromFirebase(companyId)
            
            // Forzamos sync para actualizar el Search Index (revertir a personal si aplica)
            providerRepository.syncProviderWithFirebase(updatedProvider)
            loadProfile()
        }
    }

    fun removeAdditionalAddress(addressId: String) {
        viewModelScope.launch {
            val provider = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
            val updatedAddresses = provider.addresses.filter { it.id != addressId }
            val updatedProvider = provider.copy(addresses = updatedAddresses)
            providerRepository.saveProvider(updatedProvider.toEntity())
            providerRepository.deleteAddressFromFirebase(addressId)
            loadProfile()
        }
    }

    fun saveAdditionalAddress(address: AddressUnico) {
        viewModelScope.launch {
            val provider = (profileState.value as? ProfileState.Success)?.provider ?: return@launch
            val updatedAddresses = provider.addresses.filter { it.id != address.id } + address
            val updatedProvider = provider.copy(addresses = updatedAddresses)
            providerRepository.saveProvider(updatedProvider.toEntity())
            providerRepository.syncProviderWithFirebase(updatedProvider)
            loadProfile()
        }
    }

    fun resetUpdateState() { _updateState.value = UpdateState.Idle }
    fun clearCompanyError() { _companyError.value = null }

    fun syncTopics(cp: String, categories: List<String>, isSubscribed: Boolean) {
        val fcm = FirebaseMessaging.getInstance()
        categories.forEach { cat ->
            val topic = "tender_${cp.normalizeForTopic()}_${cat.normalizeForTopic()}"
            if (isSubscribed) {
                fcm.subscribeToTopic(topic)
            } else {
                fcm.unsubscribeFromTopic(topic)
            }
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            // Lógica delegada a Auth del Core
        }
    }

    fun toggleModoEmpresa(enabled: Boolean) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            providerRepository.actualizarModoEmpresa(userId, enabled)
            // Sincronización inmediata SSOT
            providerRepository.actualizarVisibilidadPerfil(userId, enabled, emptyList()) 
            loadProfile()
        }
    }

    fun sendPasswordResetEmail(email: String, onSent: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSent()
            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error reset email: ${e.message}")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.currentUser?.delete()?.await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al borrar cuenta")
            }
        }
    }
    
    fun resetPasswordChangeState() {}
}

/**
 * --- UI STATE (STATELESS) ---
 */
/**
 * --- EDIT PROFILE UI STATE (MAVERICK ELITE) ---
 * Centraliza todos los campos de edición para un manejo Stateless de la UI.
 */
data class EditProfileUiState(
    val name: String = "",
    val lastName: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val emails: List<String> = emptyList(),
    val cuilCuit: String = "",
    val profesion: String = "",
    val matricula: String = "",
    val hasMatricula: Boolean = false,
    val description: String = "",
    val categories: List<String> = emptyList(),
    val addresses: List<AddressUnico> = emptyList(), // Lista de direcciones en el draft
    
    // --- FLAGS DE SERVICIO (SIMETRÍA CORE) ---
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val trabajaConOtros: Boolean = false,
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    
    // --- DATOS DE LOCAL / TALLER ---
    val localCalle: String = "",
    val localNumero: String = "",
    val localLocalidad: String = "",
    val localProvincia: String = "",
    val localCp: String = "",
    val localHorario: String = "",
    
    // --- ESTADOS DE LÓGICA ---
    val address: AddressUnico = AddressUnico(),
    val isDirty: Boolean = false,
    val errors: Map<String, String?> = emptyMap()
)

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val provider: Provider) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}

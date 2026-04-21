package com.example.myapplication.presentation.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.repository.UserRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Clear
import com.example.myapplication.presentation.components.BeSmallActionModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * --- PROFILE VIEWMODEL (EL CEREBRO UNIFICADO) ---
 * 
 * Este ViewModel es ahora la UNICA FUENTE DE VERDAD para el perfil del usuario.
 * Gestiona tanto el estado global (Identity) como el estado temporal de edición.
 * Se ha unificado con ProfileSharedViewModel siguiendo las mejores prácticas.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // ==================================================================================
    // 1. ESTADO GLOBAL (Single Source of Truth) - Viene de Room
    // ==================================================================================
    val userState: StateFlow<UserEntity?> = userRepository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // ==================================================================================
    // 2. ESTADO DE EDICIÓN (UI State) - Temporal para formularios
    // ==================================================================================
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Cargamos datos iniciales de Room al UI State para los formularios
        loadUserProfileIntoUiState()

        // LÓGICA DE OPTIMIZACIÓN DE COSTOS (Firestore):
        // Solo refrescamos desde remoto si Room nos dice que los datos no están sincronizados.
        viewModelScope.launch {
            val user = userRepository.userProfile.firstOrNull()
            if (user == null || !user.isSynced) {
                Log.d("ProfileViewModel", "🚀 Datos no sincronizados o nuevos. Iniciando refresh remoto...")
                refreshData()
            } else {
                Log.d("ProfileViewModel", "⚡ Datos sincronizados en Room. Omitiendo refresh remoto inicial.")
            }
        }
    }

    /** 
     * --- SECCIÓN: ACTUALIZACIÓN INTELIGENTE (Pull-to-Refresh) ---
     * Refresca los datos del usuario. 
     * - Si se detecta que los datos locales están desincronizados, prioriza Firebase.
     * - Si están sincronizados, notifica que se está usando la copia local optimizada
     *   a menos que el usuario fuerce la carga remota.
     */
    fun refreshData(forceRemote: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val currentUser = userRepository.userProfile.firstOrNull()
                val isSynced = currentUser?.isSynced ?: false

                if (!isSynced || forceRemote) {
                    // ESCENARIO A: Sincronización con la Nube (Firebase)
                    Log.d("ProfileViewModel", "🔄 [REMOTO] Iniciando sincronización con Firebase...")
                    userRepository.refreshUserFromRemote()
                    _uiState.update { it.copy(successMessage = "✨ Perfil sincronizado con la nube") }
                } else {
                    // ESCENARIO B: Optimización Local (Room)
                    Log.d("ProfileViewModel", "⚡ [LOCAL] Datos ya sincronizados. Usando copia local.")
                    _uiState.update { it.copy(successMessage = "⚡ Perfil actualizado (Copia Local)") }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "❌ Error al refrescar datos: ${e.message}")
                val errorMsg = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> "🚫 Error de permisos en el servidor"
                    e.message?.contains("network") == true -> "🌐 Sin conexión a internet"
                    else -> "⚠️ Error: ${e.message}"
                }
                _uiState.update { it.copy(error = errorMsg) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    // ==================================================================================
// 2.1. FLUJO DE ACCIONES DINÁMICAS (Pattern similar a BudgetViewModel)
// ==================================================================================
    val beActions: StateFlow<List<BeSmallActionModel>> = _uiState
        .map { state ->
            val actions = mutableListOf<BeSmallActionModel>()

            if (state.isEditMode) {
                // MODO EDICIÓN: Be ofrece Cancelar, Divider, Guardar, Agregar Empresa, Agregar Ubicación
                // Se marcan como isDefault = true para que aparezcan por defecto en la barra del asistente
                actions.add(BeSmallActionModel("cancel_edit", Icons.Default.Close, "CANCELAR", emoji = "✖️", isDefault = true))
                actions.add(BeSmallActionModel("divider_v_edit", Icons.Default.Clear, "DIVIDER", isDefault = true))
                actions.add(BeSmallActionModel("save_profile", Icons.Default.Save, "GUARDAR", emoji = "💾", isDefault = true))
                actions.add(BeSmallActionModel("add_company", Icons.Default.Business, "EMPRESA", emoji = "🏢", isDefault = true))
                actions.add(BeSmallActionModel("add_location", Icons.Default.LocationOn, "UBICACIÓN", emoji = "📍", isDefault = true))
            } else {
                // MODO LECTURA (Por defecto al entrar): Be ofrece Editar, Divider, Compartir, Ajustes
                // Se marcan como isDefault = true para que aparezcan por defecto sin necesidad de mantener presionado
                actions.add(BeSmallActionModel("edit_profile", Icons.Default.Edit, "EDITAR", emoji = "✏️", isDefault = true))
                actions.add(BeSmallActionModel("divider_v_read", Icons.Default.Clear, "DIVIDER", isDefault = true))
                actions.add(BeSmallActionModel("share_profile", Icons.Default.Share, "COMPARTIR", emoji = "📤", isDefault = true))
                actions.add(BeSmallActionModel("settings_profile", Icons.Default.Settings, "AJUSTES", emoji = "⚙️", isDefault = true))
            }
            actions
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )



    /**  ESTAS FUNCIONES SON PARA ACTUALIZAR LOS DATOS DE USUARIO LAS DEBE LLAMAR PERFILSCREEN  */
    // ==================================================================================
    // 3. HANDLERS DE FORMULARIO (Actualización de uiState)
    // ==================================================================================
    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled) }
    }
        /**
        _uiState.update { it.copy(isEditMode = enabled) }
        if (enabled) {
            // Al editar, mandamos acciones de "Guardar" y "Cancelar"
            _beActions.value = listOf(
                BeSmallActionModel("save_profile", Icons.Default.Save, "Guardar", emoji = "💾"),
                BeSmallActionModel("cancel_edit", Icons.Default.Close, "Cancelar", emoji = "✖️")
            )
        } else {
            // Al terminar, volvemos a las acciones por defecto
            _beActions.value = listOf(
                BeSmallActionModel("edit_profile", Icons.Default.Edit, "Editar", emoji = "✏️"),
                BeSmallActionModel("share_profile", Icons.Default.Share, "Compartir", emoji = "🔗")
            )
        }
    }    **/


    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value, error = null) }
    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, error = null) }
    fun onPhoneNumberChange(value: String) = _uiState.update { it.copy(phoneNumber = value, error = null) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    // Handlers para CompleteProfileScreen
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value, error = null) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value, error = null) }
    fun onStateChange(value: String) = _uiState.update { it.copy(state = value, error = null) }
    fun onZipCodeChange(value: String) = _uiState.update { it.copy(zipCode = value, error = null) }

    // --- MANEJO DE DIRECCIONES ---
    fun updatePersonalAddresses(newList: List<AddressClient>) = _uiState.update { it.copy(personalAddresses = newList) }
    
    // --- MANEJO DE CONTACTOS ADICIONALES ---
    fun updateAdditionalEmails(newList: List<String>) = _uiState.update { it.copy(additionalEmails = newList) }
    fun updateAdditionalPhones(newList: List<String>) = _uiState.update { it.copy(additionalPhones = newList) }

    // --- MANEJO DE EMPRESAS ---
    fun onHasCompanyProfileChange(value: Boolean) = _uiState.update { it.copy(isEmpresa = value) }
    fun updateCompanies(newList: List<CompanyClient>) = _uiState.update { it.copy(companies = newList) }

    // ==================================================================================
    // 4. LÓGICA DE PERSISTENCIA Y SEGURIDAD
    // ==================================================================================

    /** 
     * SECCIÓN 4: LÓGICA DE PERSISTENCIA Y SEGURIDAD
     * Guarda el perfil completo usando el Repositorio (Single Source of Truth)
     * Basado en la nueva estructura unificada de UserEntity y Sync Jerárquico.
     */
    fun saveProfile() {
        if (!validateInputs()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val s = _uiState.value

                // --- 4.1. CONSTRUCCIÓN DE DIRECCIONES ---
                // Si hay datos en los campos temporales del formulario, creamos un objeto AddressClient
                val updatedAddresses = if (s.address.isNotBlank() || s.city.isNotBlank()) {
                    val newAddr = AddressClient(
                        calle = s.address,
                        localidad = s.city,
                        provincia = s.state,
                        codigoPostal = s.zipCode,
                        label = "Principal"
                    )
                    // Evitamos duplicados básicos
                    if (s.personalAddresses.none { it.calle == newAddr.calle && it.localidad == newAddr.localidad }) {
                        s.personalAddresses + newAddr
                    } else {
                        s.personalAddresses
                    }
                } else {
                    s.personalAddresses
                }

                // --- 4.2. VALIDACIÓN MAVERICK: DIRECCIÓN OBLIGATORIA CON CÓDIGO POSTAL ---
                val hasValidAddress = updatedAddresses.any { it.codigoPostal.isNotBlank() }
                if (!hasValidAddress) {
                    _uiState.update { it.copy(
                        isLoading = false, 
                        error = "CRITICAL_ERROR: Debes cargar al menos una dirección con Código Postal para continuar." 
                    ) }
                    return@launch
                }

                // --- 4.3. CREACIÓN DE ENTIDAD DE USUARIO (UserEntity) ---
                val entity = UserEntity(
                    id = auth.currentUser?.uid ?: "",
                    email = s.email,
                    name = s.name,
                    lastName = s.lastName,
                    displayName = s.displayName,
                    phoneNumber = s.phoneNumber,
                    bio = s.bio,
                    photoUrl = if (s.photoUrl.isNotBlank()) s.photoUrl else null,
                    bannerImageUrl = if (s.coverPhotoUrl.isNotBlank()) s.coverPhotoUrl else null,
                    galleryImages = s.galleryImages,
                    additionalEmails = s.additionalEmails,
                    additionalPhones = s.additionalPhones,
                    personalAddresses = updatedAddresses,
                    hasCompanyProfile = s.isEmpresa,
                    companies = s.companies,
                    isOnline = s.isOnline,
                    isSubscribed = s.isSubscribed,
                    isVerified = s.isVerified,
                    notificationsEnabled = s.notificationsEnabled,
                    isPublicProfile = s.isPublicProfile,
                    isProfileComplete = true,
                    rating = s.rating,
                    favoriteProviderIds = s.favoriteProviderIds,
                    latitude = s.latitude,
                    longitude = s.longitude
                )

                // --- 4.4. PERSISTENCIA Y SINCRONIZACIÓN (Deep Sync) ---
                // Usamos syncUserWithFirebase para activar la subida jerárquica y compresión
                userRepository.syncUserWithFirebase(entity.toDomain())
                
                // Forzamos un refresco local para asegurar que uiState tenga las URLs finales de Firebase
                // userRepository.refreshUserFromRemote() // Opcional: syncUserWithFirebase ya actualiza Room

                _uiState.update { currentState ->
                    mapUserToUiState(entity, currentState).copy(
                        isLoading = false,
                        isComplete = true,
                        isEditMode = false,
                        successMessage = "✓ Perfil y multimedia sincronizados con éxito"
                    )
                }
                
                // Opcional: Refrescar desde remoto en segundo plano para obtener URLs finales de Storage si las hubo
                viewModelScope.launch {
                    userRepository.refreshUserFromRemote()
                }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") }
            }
        }
    }

    /** Actualiza la foto de perfil usando el flujo de Sincronización Jerárquica */
    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Obtenemos el usuario actual del estado global
                val currentUser = userState.value?.toDomain() ?: return@launch
                val updatedUser = currentUser.copy(photoUrl = uri.toString())
                
                // Sincroniza: comprime, sube a storage jerárquicamente y actualiza DB
                userRepository.syncUserWithFirebase(updatedUser)
                
                _uiState.update { it.copy(isLoading = false, photoUrl = uri.toString(), successMessage = "✓ Foto de perfil actualizada") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** Actualiza la foto de banner usando el flujo de Sincronización Jerárquica */
    fun updateBannerPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = userState.value?.toDomain() ?: return@launch
                val updatedUser = currentUser.copy(bannerImageUrl = uri.toString())
                
                // Sincroniza jerárquicamente
                userRepository.syncUserWithFirebase(updatedUser)

                _uiState.update { it.copy(isLoading = false, coverPhotoUrl = uri.toString(), successMessage = "✓ Banner actualizado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ==================================================================================
    // 5. UBICACIÓN Y GEOLOCALIZACIÓN
    // ==================================================================================
    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    _uiState.update { it.copy(isLoading = false, error = "Permisos de ubicación denegados") }
                    return@launch
                }
                client.lastLocation.addOnSuccessListener { loc ->
                    loc?.let { viewModelScope.launch { fetchAddress(context, it.latitude, it.longitude) } }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun fetchAddress(ctx: Context, lat: Double, lon: Double) {
        try {
            val geo = Geocoder(ctx, Locale.getDefault())
            val addrs = geo.getFromLocation(lat, lon, 1)
            if (!addrs.isNullOrEmpty()) {
                val a = addrs[0]
                val newAddress = AddressClient(
                    calle = a.thoroughfare ?: "",
                    numero = a.subThoroughfare ?: "",
                    localidad = a.locality ?: "",
                    provincia = a.adminArea ?: "",
                    pais = a.countryName ?: "",
                    codigoPostal = a.postalCode ?: "",
                    latitude = lat,
                    longitude = lon,
                    label = "Ubicación Actual"
                )
                // Agregamos la nueva dirección a la lista existente
                val currentList = _uiState.value.personalAddresses.toMutableList()
                currentList.add(newAddress)
                _uiState.update { it.copy(isLoading = false, personalAddresses = currentList) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "Error al obtener dirección") }
        }
    }

    // ==================================================================================
    // 6. UTILIDADES Y CARGA DE DATOS
    // ==================================================================================

    /** Mapea el perfil persistido al estado de edición de la UI */
    private fun loadUserProfileIntoUiState() {
        viewModelScope.launch {
            userRepository.userProfile.collect { user ->
                user?.let { u ->
                    _uiState.update { currentState ->
                        // Si estamos en modo edición, evitamos sobreescribir lo que el usuario está escribiendo
                        // a menos que sea el primer cargado o un cambio externo real.
                        if (currentState.isEditMode && currentState.uid == u.id) {
                            currentState
                        } else {
                            mapUserToUiState(u, currentState)
                        }
                    }
                }
            }
        }
    }

    private fun updateUiStateFromUser(u: UserEntity) {
        _uiState.update { currentState ->
            mapUserToUiState(u, currentState)
        }
    }

    private fun mapUserToUiState(u: UserEntity, currentState: ProfileUiState): ProfileUiState {
        return currentState.copy(
            uid = u.id,
            displayName = u.displayName,
            name = u.name,
            lastName = u.lastName,
            email = u.email,
            phoneNumber = u.phoneNumber,
            bio = u.bio,
            photoUrl = u.photoUrl ?: "",
            coverPhotoUrl = u.bannerImageUrl ?: "",
            personalAddresses = u.personalAddresses,
            additionalEmails = u.additionalEmails,
            additionalPhones = u.additionalPhones,
            galleryImages = u.galleryImages,
            isEmpresa = u.hasCompanyProfile,
            companies = u.companies,
            isOnline = u.isOnline,
            isSubscribed = u.isSubscribed,
            isVerified = u.isVerified,
            notificationsEnabled = u.notificationsEnabled,
            isPublicProfile = u.isPublicProfile,
            rating = u.rating,
            favoriteProviderIds = u.favoriteProviderIds,
            latitude = u.latitude,
            longitude = u.longitude
        )
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.clearLocalUser()
        }
    }

    private fun validateInputs(): Boolean {
        val s = _uiState.value
        if (s.phoneNumber.isNotBlank() && s.phoneNumber.length < 8) {
            _uiState.update { it.copy(error = "El número de teléfono debe tener al menos 8 dígitos") }
            return false
        }
        return true
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
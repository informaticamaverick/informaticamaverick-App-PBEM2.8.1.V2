package com.example.myapplication.presentation.features.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.domain.model.AddressClient
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.core.data.repository.UserRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.presentation.global.AppActionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.core.utils.MaverickGeoUtils
import com.example.myapplication.presentation.global.HUDContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * --- USER VIEWMODEL (EL CEREBRO DE IDENTIDAD) ---
 * 
 * Este ViewModel es ahora la UNICA FUENTE DE VERDAD para la identidad del usuario.
 * Gestiona tanto el estado global como el de edición bajo las leyes de "Costo Zero".
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val coordinator: AppActionCoordinator,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
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
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUserProfileIntoUiState()
        Log.d("UserViewModel", "⚡ Inicializado. Sincronización delegada al Cerebro Global.")

        viewModelScope.launch {
            coordinator.actionEvent.collect { actionId ->
                when (actionId) {
                    "edit_profile" -> setEditMode(true)
                    "save_profile" -> saveProfile()
                    "cancel_edit" -> setEditMode(false)
                }
            }
        }
    }

    fun refreshData(forceRemote: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val currentUser = userRepository.userProfile.firstOrNull()
                val isSynced = currentUser?.isSynced ?: false

                if (!isSynced || forceRemote) {
                    userRepository.refreshUserFromRemote()
                    _uiState.update { it.copy(successMessage = "✨ Perfil sincronizado con la nube") }
                } else {
                    _uiState.update { it.copy(successMessage = "⚡ Perfil actualizado (Copia Local)") }
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ Error al refrescar datos: ${e.message}")
                _uiState.update { it.copy(error = "⚠️ Error: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    val actionIds: StateFlow<List<String>> = _uiState
        .map { state ->
            if (state.isEditMode) {
                listOf("cancel_edit", "divider_v_edit", "save_profile", "add_company")
            } else {
                listOf("edit_profile", "divider_v_read", "share", "settings_profile")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun setEditMode(enabled: Boolean) {
        _uiState.update { it.copy(isEditMode = enabled) }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }
    fun onLastNameChange(value: String) = _uiState.update { it.copy(lastName = value, error = null) }
    fun onDisplayNameChange(value: String) = _uiState.update { it.copy(displayName = value, error = null) }
    fun onPhoneNumberChange(value: String) = _uiState.update { it.copy(phoneNumber = value, error = null) }
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value, error = null) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value, error = null) }
    fun onStateChange(value: String) = _uiState.update { it.copy(state = value, error = null) }
    fun onZipCodeChange(value: String) = _uiState.update { it.copy(zipCode = value, error = null) }

    fun updatePersonalAddresses(newList: List<AddressClient>) {
        _uiState.update { it.copy(personalAddresses = newList) }
        viewModelScope.launch {
            try {
                val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
                currentUser?.let {
                    val updatedUser = it.copy(personalAddresses = newList)
                    userRepository.syncUserWithFirebase(updatedUser)
                }
            } catch (_: Exception) {}
        }
    }
    
    fun updateAdditionalEmails(newList: List<String>) = _uiState.update { it.copy(additionalEmails = newList) }
    fun updateAdditionalPhones(newList: List<String>) = _uiState.update { it.copy(additionalPhones = newList) }

    fun onHasCompanyProfileChange(value: Boolean) = _uiState.update { it.copy(isEmpresa = value) }
    
    /** 
     * --- MAVERICK ELITE: Límite de 2 empresas ---
     */
    fun updateCompanies(newList: List<CompanyClient>) {
        if (newList.size > 2) {
            _uiState.update { it.copy(error = "Límite alcanzado: Máximo 2 empresas permitidas.") }
            return
        }
        _uiState.update { it.copy(companies = newList) }
    }

    /** 
     * --- MAVERICK ELITE: Límite de 3 sucursales por empresa ---
     */
    fun updateBranches(companyId: String, newBranches: List<CompanyClient>) {
        // Lógica de validación de sucursales delegada a la UI o aquí
    }

    fun saveProfile() {
        if (!validateInputs()) return
        
        // --- VALIDACIÓN DE LÍMITES ELITE ---
        val s = _uiState.value
        if (s.companies.size > 2) {
            _uiState.update { it.copy(error = "Máximo 2 empresas permitidas.") }
            return
        }
        if (s.companies.any { it.branches.size > 3 }) {
            _uiState.update { it.copy(error = "Máximo 3 sucursales permitidas por empresa.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val updatedAddresses = if (s.address.isNotBlank() || s.city.isNotBlank()) {
                    val newAddr = AddressClient(
                        calle = s.address,
                        localidad = s.city,
                        provincia = s.state,
                        codigoPostal = s.zipCode,
                        label = "Principal"
                    )
                    if (s.personalAddresses.none { it.calle == newAddr.calle && it.localidad == newAddr.localidad }) {
                        s.personalAddresses + newAddr
                    } else s.personalAddresses
                } else s.personalAddresses

                val hasValidAddress = updatedAddresses.any { it.codigoPostal.isNotBlank() }
                if (!hasValidAddress) {
                    _uiState.update { it.copy(isLoading = false, error = "Debes cargar al menos una dirección con Código Postal.") }
                    return@launch
                }

                val entity = UserEntity(
                    id = auth.currentUser?.uid ?: "",
                    email = s.email,
                    name = s.name,
                    lastName = s.lastName,
                    displayName = s.displayName,
                    phoneNumber = s.phoneNumber,
                    bio = s.bio,
                    photoUrl = s.photoUrl.ifBlank { null },
                    bannerImageUrl = s.coverPhotoUrl.ifBlank { null },
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

                userRepository.syncUserWithFirebase(entity.toDomain())
                
                _uiState.update { currentState ->
                    mapUserToUiState(entity, currentState).copy(
                        isLoading = false,
                        isComplete = true,
                        isEditMode = false,
                        successMessage = "✓ Identidad sincronizada (Costo Zero)"
                    )
                }
                
                viewModelScope.launch { userRepository.refreshUserFromRemote() }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error al guardar") }
            }
        }
    }

    /** --- [COSTO ZERO] Compresión agresiva WebP --- */
    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val compressedBytes = ImageUtils.compressImageToWebP(context, uri, quality = 60)
                val base64 = compressedBytes?.let { ImageUtils.bytesToBase64(it) } ?: uri.toString()

                val currentUser = userState.value?.toDomain() ?: return@launch
                val updatedUser = currentUser.copy(photoUrl = base64)
                
                userRepository.syncUserWithFirebase(updatedUser)
                _uiState.update { it.copy(isLoading = false, photoUrl = base64, successMessage = "✓ Avatar optimizado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** --- [COSTO ZERO] Compresión agresiva WebP --- */
    fun updateBannerPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val compressedBytes = ImageUtils.compressImageToWebP(context, uri, quality = 60, maxWidth = 800)
                val base64 = compressedBytes?.let { ImageUtils.bytesToBase64(it) } ?: uri.toString()

                val currentUser = userState.value?.toDomain() ?: return@launch
                val updatedUser = currentUser.copy(bannerImageUrl = base64)
                
                userRepository.syncUserWithFirebase(updatedUser)
                _uiState.update { it.copy(isLoading = false, coverPhotoUrl = base64, successMessage = "✓ Banner optimizado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    _uiState.update { it.copy(isLoading = false, error = "Permisos denegados") }
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

    private fun fetchAddress(ctx: Context, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                MaverickGeoUtils.getAddressFromCoordinates(ctx, lat, lon)?.let { newAddress ->
                    val currentList = _uiState.value.personalAddresses.toMutableList()
                    if (currentList.none { it.calle == newAddress.calle && it.numero == newAddress.numero }) {
                        currentList.add(newAddress)
                    }
                    _uiState.update { it.copy(personalAddresses = currentList) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Error al obtener dirección") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadUserProfileIntoUiState() {
        viewModelScope.launch {
            userRepository.userProfile.collect { user ->
                if (user != null) {
                    _uiState.update { currentState ->
                        if (currentState.isEditMode && currentState.uid == user.id) currentState
                        else mapUserToUiState(user, currentState)
                    }
                } else if (auth.currentUser != null) {
                    userRepository.refreshUserFromRemote()
                }
            }
        }
    }

    private fun mapUserToUiState(u: UserEntity, currentState: UserUiState): UserUiState {
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
            auth.signOut()
        }
    }

    fun deleteFullAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                userRepository.deleteAccount()
                auth.currentUser?.delete()?.await()
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al eliminar cuenta.") }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val s = _uiState.value
        if (s.phoneNumber.isNotBlank() && s.phoneNumber.length < 8) {
            _uiState.update { it.copy(error = "Teléfono inválido.") }
            return false
        }
        return true
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

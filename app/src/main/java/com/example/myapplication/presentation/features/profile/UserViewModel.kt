package com.example.myapplication.presentation.features.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.domain.model.*
import com.example.myapplication.core.utils.ImageUtils
import com.example.myapplication.presentation.global.AppActionCoordinator
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val coordinator: AppActionCoordinator,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val userState: StateFlow<UserEntity?> = userRepository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadUserProfileIntoUiState()
        
        coordinator.selectedProfileId
            .onEach { id -> _uiState.update { it.copy(selectedProfileId = id) } }
            .launchIn(viewModelScope)

        coordinator.actionEvent
            .onEach { actionId ->
                when (actionId) {
                    "edit_profile" -> setEditMode(true)
                }
            }
            .launchIn(viewModelScope)
    }

    val actionIds: StateFlow<List<String>> = _uiState.map { s ->
        if (s.isEditMode) listOf("cancel_edit", "save_profile")
        else listOf("edit_profile", "add_company")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.refreshUserFromRemote()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun toggleEditMode() {
        if (_uiState.value.isEditMode) {
            saveProfile()
        } else {
            setEditMode(true)
        }
    }

    fun setEditMode(edit: Boolean) {
        _uiState.update { it.copy(isEditMode = edit) }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onLastNameChange(v: String) = _uiState.update { it.copy(lastName = v) }
    fun onDisplayNameChange(v: String) = _uiState.update { it.copy(displayName = v) }
    fun onPhoneNumberChange(v: String) = _uiState.update { it.copy(phoneNumber = v) }
    fun onBioChange(v: String) = _uiState.update { it.copy(bio = v) }

    fun updateAdditionalEmails(newList: List<String>) {
        _uiState.update { it.copy(additionalEmails = newList) }
        viewModelScope.launch {
            val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
            currentUser?.let {
                userRepository.syncUserWithFirebase(it.copy(additionalEmails = newList))
            }
        }
    }

    fun updateAdditionalPhones(newList: List<String>) {
        _uiState.update { it.copy(additionalPhones = newList) }
        viewModelScope.launch {
            val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
            currentUser?.let {
                userRepository.syncUserWithFirebase(it.copy(additionalPhones = newList))
            }
        }
    }

    fun updateCompanies(newList: List<CompanyClient>) {
        _uiState.update { it.copy(companies = newList) }
        viewModelScope.launch {
            val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
            currentUser?.let {
                userRepository.syncUserWithFirebase(it.copy(companies = newList))
            }
        }
    }

    fun updatePersonalAddresses(newList: List<AddressUnico>) {
        Log.d("UserViewModel", "📍 [UPDATE_ADDRESSES] Actualizando lista local: ${newList.size} direcciones.")
        _uiState.update { it.copy(personalAddresses = newList) }
        viewModelScope.launch {
            try {
                // [ELITE] Reconstruimos el usuario desde el estado actual para la sincronización "Hormiga"
                val s = _uiState.value
                val userToSync = User(
                    uid = s.uid,
                    email = s.email,
                    displayName = s.displayName,
                    name = s.name,
                    lastName = s.lastName,
                    phoneNumber = s.phoneNumber,
                    bio = s.bio,
                    photoUrl = s.photoUrl,
                    profileThumbnail = s.profileThumbnail,
                    additionalEmails = s.additionalEmails,
                    additionalPhones = s.additionalPhones,
                    personalAddresses = newList, // Usamos la lista actualizada
                    companies = s.companies,
                    isOnline = s.isOnline,
                    isSubscribed = s.isSubscribed,
                    isVerified = s.isVerified,
                    isProfileComplete = true,
                    rating = s.rating,
                    favoriteProviderIds = s.favoriteProviderIds,
                    latitude = s.latitude,
                    longitude = s.longitude
                )

                userRepository.syncUserWithFirebase(userToSync)
                Log.d("UserViewModel", "✅ [UPDATE_ADDRESSES] Sincronización proactiva con Firebase exitosa.")
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ [UPDATE_ADDRESSES] Error en sincronización: ${e.message}")
            }
        }
    }

    /**
     * Guarda el perfil bajo la nueva anatomía (Sin banners, con thumbnails).
     */
    fun saveProfile() {
        val s = _uiState.value
        Log.d("UserViewModel", "💾 [SAVE_PROFILE] Iniciando guardado integral para: ${s.displayName}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // [ELITE] Sincronizamos usando la lista consolidada de direcciones (SSOT)
                val user = User(
                    uid = auth.currentUser?.uid ?: "",
                    email = s.email,
                    name = s.name,
                    lastName = s.lastName,
                    displayName = s.displayName,
                    phoneNumber = s.phoneNumber,
                    bio = s.bio,
                    photoUrl = s.photoUrl,
                    profileThumbnail = s.profileThumbnail,
                    additionalEmails = s.additionalEmails,
                    additionalPhones = s.additionalPhones,
                    personalAddresses = s.personalAddresses, // Lista maestra
                    companies = s.companies,
                    isOnline = s.isOnline,
                    isSubscribed = s.isSubscribed,
                    isVerified = s.isVerified,
                    isProfileComplete = true,
                    rating = s.rating,
                    favoriteProviderIds = s.favoriteProviderIds,
                    latitude = s.latitude,
                    longitude = s.longitude
                )

                Log.d("UserViewModel", "💾 [SAVE_PROFILE] Direcciones a guardar: ${user.personalAddresses.size}")
                user.personalAddresses.forEach { Log.d("UserViewModel", "   - 📍 ${it.calle} ${it.numero} (ID: ${it.id})") }

                val isLocal = user.photoUrl?.startsWith("/") == true || user.photoUrl?.startsWith("content://") == true
                userRepository.syncUserWithFirebase(
                    user = user,
                    remotePhotoUrl = if (isLocal) null else user.photoUrl
                )
                _uiState.update { it.copy(isLoading = false, isEditMode = false, successMessage = "✓ Perfil guardado (Elite)") }
                
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ [SAVE_PROFILE] Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /** 
     * [LEY #3 & #6]: Genera automáticamente un thumbnail y aplica Room Shielding.
     * Guarda la imagen física localmente y persiste la ruta en Room.
     */
    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Imagen Principal (Elite Compression 80%)
                val photoBytes = ImageUtils.compressElite(context, uri)
                
                // 2. Room Shielding: Guardar a archivo local
                val fileName = "profile_${auth.currentUser?.uid ?: "unknown"}"
                val localPath = photoBytes?.let { ImageUtils.saveBytesToFile(context, it, fileName) }
                
                // 3. Preparar Base64 solo para Sync con Firebase
                val photoBase64 = photoBytes?.let { ImageUtils.bytesToBase64(it) } ?: uri.toString()

                // 4. Thumbnail (Muy pequeña para carga Shallow - Ley #3.1)
                val thumbBase64 = ImageUtils.generateThumbnailBase64(context, uri)

                Log.d("UserViewModel", "📸 [PHOTO_UPDATE] Generando metadatos. Thumbnail: ${thumbBase64?.length ?: 0} chars.")

                _uiState.update { it.copy(
                    photoUrl = localPath ?: uri.toString(), 
                    profileThumbnail = thumbBase64 ?: "",
                    isLoading = false
                ) }

                // 5. [SSOT] Persistencia inmediata en Room y Firebase
                val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
                currentUser?.let {
                    val updatedUser = it.copy(
                        photoUrl = localPath ?: uri.toString(),
                        profileThumbnail = thumbBase64
                    )
                    userRepository.syncUserWithFirebase(
                        user = updatedUser,
                        remotePhotoUrl = photoBase64
                    )
                    Log.d("UserViewModel", "✅ [PHOTO_UPDATE] Sincronización proactiva completada.")
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ [PHOTO_UPDATE] Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * [ELITE v5.2]: Guarda o actualiza una empresa procesando su imagen si es necesario.
     */
    fun saveCompany(company: CompanyClient, newPhotoUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                var updatedCompany = company
                if (newPhotoUri != null) {
                    val photoBytes = ImageUtils.compressElite(context, newPhotoUri)
                    val fileName = "company_${company.id}_${System.currentTimeMillis()}"
                    val localPath = photoBytes?.let { ImageUtils.saveBytesToFile(context, it, fileName) }
                    val photoBase64 = photoBytes?.let { ImageUtils.bytesToBase64(it) } ?: newPhotoUri.toString()
                    val thumbBase64 = ImageUtils.generateThumbnailBase64(context, newPhotoUri)
                    
                    updatedCompany = company.copy(photoUrl = localPath ?: photoBase64, thumbnailBase64 = thumbBase64)
                }

                val currentCompanies = _uiState.value.companies.toMutableList()
                val idx = currentCompanies.indexOfFirst { it.id == company.id }
                if (idx != -1) currentCompanies[idx] = updatedCompany else currentCompanies.add(updatedCompany)
                
                _uiState.update { it.copy(companies = currentCompanies, isLoading = false) }
                
                val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
                currentUser?.let {
                    userRepository.syncUserWithFirebase(it.copy(companies = currentCompanies))
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ [SAVE_COMPANY] Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * [ELITE v5.2]: Guarda o actualiza un representante procesando su imagen si es necesario.
     */
    fun saveRepresentative(company: CompanyClient, branch: BranchClient, representative: RepresentativeClient, newPhotoUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                var updatedRep = representative
                if (newPhotoUri != null) {
                    val photoBytes = ImageUtils.compressElite(context, newPhotoUri)
                    val fileName = "rep_${representative.id}_${System.currentTimeMillis()}"
                    val localPath = photoBytes?.let { ImageUtils.saveBytesToFile(context, it, fileName) }
                    val photoBase64 = photoBytes?.let { ImageUtils.bytesToBase64(it) } ?: newPhotoUri.toString()
                    val thumbBase64 = ImageUtils.generateThumbnailBase64(context, newPhotoUri)
                    
                    updatedRep = representative.copy(photoUrl = localPath ?: photoBase64, thumbnailBase64 = thumbBase64)
                }

                val updatedCompanies = _uiState.value.companies.map { c ->
                    if (company.id == c.id) {
                        val updatedBranches = c.branches.map { b ->
                            if (branch.id == b.id) {
                                val currentReps = b.representatives.toMutableList()
                                val ridx = currentReps.indexOfFirst { it.id == representative.id }
                                if (ridx != -1) currentReps[ridx] = updatedRep else currentReps.add(updatedRep)
                                b.copy(representatives = currentReps)
                            } else b
                        }
                        c.copy(branches = updatedBranches)
                    } else c
                }

                _uiState.update { it.copy(companies = updatedCompanies, isLoading = false) }
                
                val currentUser = userRepository.userProfile.firstOrNull()?.toDomain()
                currentUser?.let {
                    userRepository.syncUserWithFirebase(it.copy(companies = updatedCompanies))
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "❌ [SAVE_REPRESENTATIVE] Error: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadUserProfileIntoUiState() {
        viewModelScope.launch {
            userRepository.userProfile.collect { user ->
                user?.let { u ->
                    _uiState.update { currentState ->
                        if (currentState.isEditMode && currentState.uid == u.id) currentState
                        else mapUserToUiState(u, currentState)
                    }
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
            profileThumbnail = u.profileThumbnail ?: "",
            personalAddresses = u.personalAddresses,
            additionalEmails = u.additionalEmails,
            additionalPhones = u.additionalPhones,
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

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

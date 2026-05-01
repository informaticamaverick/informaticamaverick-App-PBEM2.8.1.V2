package com.example.myapplication.presentation.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository, // Agregado para forzar sincronización post-login
    @ApplicationContext private val context: Context 
) : ViewModel() {

    // ======================================================================================
    // --- SECCIÓN: ESTADOS TÉCNICOS (EL OBRERO SENSANDO LA REALIDAD) ---
    // ======================================================================================
    private val _isWifiEnabled = MutableStateFlow(false)
    val isWifiEnabled = _isWifiEnabled.asStateFlow()

    private val _isCellularEnabled = MutableStateFlow(false)
    val isCellularEnabled = _isCellularEnabled.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(false)
    val isGpsEnabled = _isGpsEnabled.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()

    /** 
     * TRABAJO SUCIO TÉCNICO: Monitoreo de Hardware 
     * Esta función debe ser llamada desde la UI (ej. StartupScreen) para actualizar los estados.
     */
    fun refreshHardwareStatus() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        _isWifiEnabled.value = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        _isCellularEnabled.value = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        _isGpsEnabled.value = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        
        checkOnlineStatus()
    }

    fun updateWifiStatus(enabled: Boolean) { _isWifiEnabled.value = enabled; checkOnlineStatus() }
    fun updateCellularStatus(enabled: Boolean) { _isCellularEnabled.value = enabled; checkOnlineStatus() }
    fun updateGpsStatus(enabled: Boolean) { _isGpsEnabled.value = enabled }
    
    private fun checkOnlineStatus() {
        _isOnline.value = _isWifiEnabled.value || _isCellularEnabled.value
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _hasProfile = MutableStateFlow(false)
    val hasProfile: StateFlow<Boolean> = _hasProfile.asStateFlow()

    // ======================================================================================
    // --- NUEVO ESTADO: DESTINO DE NAVEGACIÓN POST-LOGIN ---
    // ======================================================================================
    private val _navigationTarget = MutableStateFlow<String?>(null)
    val navigationTarget: StateFlow<String?> = _navigationTarget.asStateFlow()

    private var timerJob: Job? = null

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    // ======================================================================================
    // --- SECCIÓN: INICIO DE SESIÓN ---
    // ======================================================================================

    fun login() {
        if (!validateInputs()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithEmailAndPassword(
                _uiState.value.email,
                _uiState.value.password
            )

            result.onSuccess { user ->
                _userName.value = user.displayName

                Log.d("LoginViewModel", "Usuario logueado: ${user.uid}, nombre: ${user.displayName}")

                // --- CHEQUEO DE PERFIL Y DIRECCIÓN ---
                // Ahora delegamos la decisión de navegación final al Cerebro o la centralizamos aquí
                // pero informamos éxito al UI state.
                checkProfileAndNavigate(user.uid)

            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al iniciar sesión"
                    )
                }
            }
        }
    }


    fun signInWithGoogle() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    fun onSignInCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithGoogle(idToken)

            result.onSuccess { (user, googleProfile) ->
                _userName.value = user.displayName ?: "Usuario"

                // --- INICIALIZACIÓN DE USUARIO NUEVO (Si no existe en Room/Firestore) ---
                // El refreshUserFromRemote en checkProfileAndNavigate nos dirá si ya existe.
                // Pero si es un registro limpio, inicializamos con los datos enriquecidos.
                
                checkProfileAndNavigate(user.uid, user, googleProfile)

            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Error al iniciar sesión con Google"
                    )
                }
            }
        }
    }

    /**
     * Lógica centralizada para decidir a dónde enviar al usuario tras el login.
     * AHORA FUERZA LA DESCARGA DE DATOS DESDE FIREBASE.
     */
    private suspend fun checkProfileAndNavigate(
        uid: String, 
        userBase: com.example.myapplication.data.model.User? = null,
        googleProfile: Map<String, Any?>? = null
    ) {
        try {
            // --- 1. SINCRONIZACIÓN FORZADA ---
            // Descargamos los datos completos de Firestore a Room antes de verificar el estado
            Log.d("LoginViewModel", "🔄 Iniciando descarga de perfil para $uid")
            userRepository.refreshUserFromRemote()

            // 2. Verificamos si existe el perfil en Firestore
            val profileExists = authRepository.checkUserProfileExists(uid)
            _hasProfile.value = profileExists

            if (!profileExists && userBase != null) {
                // Si el perfil no existe en Firestore, lo inicializamos localmente y sincronizamos
                Log.d("LoginViewModel", "🆕 Inicializando nuevo usuario de Google...")
                userRepository.initializeNewUserFromGoogle(userBase, googleProfile)
                _hasProfile.value = true
            }

            // --- 3. DECISIÓN DE NAVEGACIÓN (MAVERICK V5) ---
            // Ya NO forzamos perfil_cliente_edit si no tiene dirección.
            // Siempre vamos a la main_screen, y el Cerebro decidirá si mostrar el popup de dirección.
            _navigationTarget.value = "main_screen"

            _uiState.update {
                it.copy(isLoading = false, isLoginSuccess = true)
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "❌ Error en checkProfileAndNavigate: ${e.message}")
            _uiState.update { it.copy(isLoading = false, error = "Error al verificar perfil") }
        }
    }

    // ======================================================================================
    // --- SECCIÓN: REESTRUCTURACIÓN MAVERICK (NUEVAS FUNCIONES) ---
    // ======================================================================================

    fun toggleRegisterWithEmail() {
        _uiState.update { it.copy(isRegisteringWithEmail = !it.isRegisteringWithEmail) }
    }

    fun onVerificationCodeChange(code: String) {
        _uiState.update { it.copy(verificationCode = code) }
    }

    fun sendVerificationCode() {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Simulación de envío de código (En un flujo real se integraría con el backend/Auth)
            delay(1500) 
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    isVerificationSent = true,
                    isTimerRunning = true,
                    timerValue = 300 // Reset a 5 minutos
                ) 
            }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerValue > 0) {
                delay(1000)
                _uiState.update { it.copy(timerValue = it.timerValue - 1) }
            }
            _uiState.update { it.copy(isTimerRunning = false) }
        }
    }

    fun verifyCodeAndContinue() {
        if (_uiState.value.verificationCode.length < 4) {
            _uiState.update { it.copy(error = "Código inválido") }
            return
        }
        
        // Aquí iría la lógica de validación del código
        login() // Por ahora procedemos al login tras la "verificación"
    }

    // ======================================================================================
    // --- SECCIÓN: RECUPERACIÓN DE CONTRASEÑA ---
    // ======================================================================================
    fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _uiState.update { it.copy(error = "Por favor ingresa tu email") }
            return
        }

        if (!email.contains("@")) {
            _uiState.update { it.copy(error = "Email inválido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.sendPasswordResetEmail(email)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        passwordResetEmailSent = true
                    )
                }
            }.onFailure { error ->
                val errorMessage = when {
                    error.message?.contains("user-not-found") == true ->
                        "No existe una cuenta con este email"
                    error.message?.contains("invalid-email") == true ->
                        "Email inválido"
                    error.message?.contains("too-many-requests") == true ->
                        "Demasiados intentos. Intenta más tarde"
                    else ->
                        "Error al enviar el correo: ${error.message}"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isEmpty() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Ingresa un email válido") }
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return false
        }

        return true
    }

    // ======================================================================================
    // --- SECCIÓN: PERSISTENCIA Y OFFLINE-FIRST ---
    // ======================================================================================

    fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _userName.value = user.displayName ?: "Usuario"
                
                // --- AL ABRIR LA APP: Chequeo de dirección obligatoria ---
                checkProfileAndNavigate(user.uid)
            }
        }
    }

    fun consumeNavigationTarget() {
        _navigationTarget.value = null
    }
}
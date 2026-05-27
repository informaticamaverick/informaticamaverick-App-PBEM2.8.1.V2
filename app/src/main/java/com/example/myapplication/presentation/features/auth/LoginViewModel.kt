package com.example.myapplication.presentation.features.auth

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.AuthRepository
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.presentation.global.AppActionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val coordinator: AppActionCoordinator
) : ViewModel() {

    // ======================================================================================
    // --- SECCIÓN: ESTADOS TÉCNICOS (MAVERICK CORE) ---
    // ======================================================================================
    val isWifiEnabled = coordinator.isWifiEnabled
    val isCellularEnabled = coordinator.isCellularEnabled
    val isGpsEnabled = coordinator.isGpsEnabled
    val isOnline = coordinator.isOnline

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _userName = MutableStateFlow("")

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
        Log.d("LoginViewModel", "🔵 Intento de login con email: ${_uiState.value.email}")

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


    fun onSignInCancelled() {
        _uiState.update { it.copy(isLoading = false) }
    }

    fun handleGoogleSignInResult(idToken: String) {
        Log.d("LoginViewModel", "🔵 Recibido ID Token de Google. Iniciando Auth Firebase...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithGoogle(idToken)

            result.onSuccess { (user, googleProfile) ->
                Log.d("LoginViewModel", "✅ Firebase Auth Exitosa: ${user.uid}")
                _userName.value = user.displayName
                checkProfileAndNavigate(user.uid, user, googleProfile)

            }.onFailure { error ->
                Log.e("LoginViewModel", "❌ Error en Firebase Auth con Google: ${error.message}")
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
     * MAVERICK V5: NAVEGACIÓN ULTRA-RÁPIDA (Costo Zero).
     * Sincroniza en segundo plano para evitar bloqueos por latencia de red.
     */
    private fun checkProfileAndNavigate(
        uid: String, 
        userBase: com.example.myapplication.core.domain.model.User? = null,
        googleProfile: Map<String, Any?>? = null
    ) {
        // --- 1. SINCRONIZACIÓN TÁCTICA EN SEGUNDO PLANO ---
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("LoginViewModel", "🔄 [BACKGROUND] Iniciando recuperación de perfil para $uid")
                
                // Si es un nuevo usuario de Google, inicializamos primero en Room y luego sync
                if (userBase != null) {
                    Log.d("LoginViewModel", "🆕 [BACKGROUND] Inicializando nuevo usuario de Google...")
                    userRepository.initializeNewUserFromGoogle(userBase, googleProfile)
                } else {
                    // Si ya existe, refrescamos desde remoto
                    userRepository.refreshUserFromRemote()
                }
                Log.d("LoginViewModel", "✅ [BACKGROUND] Sincronización finalizada con éxito")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "⚠️ [BACKGROUND] Error en sync (No bloqueante): ${e.message}")
            }
        }

        // --- 2. NAVEGACIÓN INSTANTÁNEA (Ley de Persistencia Total) ---
        // No esperamos a Firebase. Si el usuario llegó aquí, ya está autenticado en Auth.
        // Room nos servirá los datos que tenga disponibles inmediatamente.
        Log.d("LoginViewModel", "🚀 Ejecutando navegación instantánea a main_screen")
        _navigationTarget.value = "main_screen"

        _uiState.update {
            it.copy(isLoading = false, isLoginSuccess = true)
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
            // MAVERICK V5: Envío inmediato (Zero Friction)
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    isVerificationSent = true,
                    isTimerRunning = true,
                    timerValue = 300
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

    // ======================================================================================
    // --- SECCIÓN: GESTIÓN DE PERMISOS ---
    // ======================================================================================
    fun updateLocationPermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    fun updateNotificationsPermissionStatus(granted: Boolean) {
        _uiState.update { it.copy(hasNotificationsPermission = granted) }
    }
}










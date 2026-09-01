package com.example.myapplication.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datos.repositorios.UsuarioAutenticacionRepositorio
import com.example.myapplication.datos.repositorios.SincUsuarioRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * --- ESTADO DEL LOGIN USUARIO (UI) ---
 */
data class EstadoUiLoginUsuario(
    val email: String = "",
    val password: String = "",
    val estaCargando: Boolean = false,
    val exitoLogin: Boolean = false,
    val error: String? = null,
    val verificationCode: String = "",
    val esVerificacionEnviada: Boolean = false,
    val tienePermisoUbicacion: Boolean = false,
    val tienePermisoNotificaciones: Boolean = false,
    val esRegistroEmail: Boolean = false 
)

/**
 * --- VIEWMODEL DE LOGIN CLIENTE (v2026.ELITE) ---
 * [LEY #9]: Nombres Completos en Español.
 */
@HiltViewModel
class UsuarioLoginViewModel @Inject constructor(
    private val authRepository: UsuarioAutenticacionRepositorio,
    private val sincRepo: SincUsuarioRepositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadoUiLoginUsuario())
    val uiState: StateFlow<EstadoUiLoginUsuario> = _uiState.asStateFlow()

    private val _objetivoNavegacion = MutableStateFlow<String?>(null)
    val navigationTarget: StateFlow<String?> = _objetivoNavegacion.asStateFlow()

    // --- SECTOR: UI UPDATES ---

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }
    fun onVerificationCodeChange(code: String) = _uiState.update { it.copy(verificationCode = code) }
    fun setCustomError(error: String) = _uiState.update { it.copy(error = error) }
    fun onSignInCancelled() = _uiState.update { it.copy(estaCargando = false) }
    fun toggleRegisterWithEmail() = _uiState.update { it.copy(esRegistroEmail = !it.esRegistroEmail) }

    fun updateLocationPermissionStatus(concedido: Boolean) {
        _uiState.update { it.copy(tienePermisoUbicacion = concedido) }
    }

    fun updateNotificationsPermissionStatus(concedido: Boolean) {
        _uiState.update { it.copy(tienePermisoNotificaciones = concedido) }
    }

    // --- SECTOR: ACCIONES ---

    fun verificarUsuarioActual() {
        authRepository.obtenerUsuarioActual()?.let { usuario ->
            viewModelScope.launch { finalizarAcceso(usuario) }
        }
    }

    fun iniciarSesion() {
        if (!validarEntradas()) return
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true, error = null) }
            authRepository.iniciarSesionConEmailClave(_uiState.value.email, _uiState.value.password)
                .onSuccess { usuario ->
                    finalizarAcceso(usuario)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(estaCargando = false, error = error.message ?: "Error de acceso") }
                }
        }
    }

    fun manejarResultadoLoginGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true, error = null) }
            authRepository.iniciarSesionConGoogle(idToken)
                .onSuccess { usuario ->
                    finalizarAcceso(usuario)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(estaCargando = false, error = error.message ?: "Error Google") }
                }
        }
    }

    private suspend fun finalizarAcceso(usuario: com.google.firebase.auth.FirebaseUser) {
        try {
            android.util.Log.d("UsuarioLoginVM", "🌱 [SEED_SYNC] Delegando preparación de Room...")
            sincRepo.finalizarAcceso(usuario)
            withContext(Dispatchers.Main) {
                _objetivoNavegacion.value = "home"
                _uiState.update { it.copy(estaCargando = false, exitoLogin = true) }
            }
        } catch (e: Exception) {
            android.util.Log.e("UsuarioLoginVM", "❌ [LOGIN_ERROR] ${e.message}")
            withContext(Dispatchers.Main) {
                _objetivoNavegacion.value = "home"
                _uiState.update { it.copy(estaCargando = false, exitoLogin = true, error = "Modo Emergencia") }
            }
        }
    }

    fun recuperarClave(email: String) {
        if (email.isEmpty()) {
            _uiState.update { it.copy(error = "Ingresa tu email") }
            return
        }
        viewModelScope.launch {
            authRepository.enviarCorreoRecuperacionClave(email)
            _uiState.update { it.copy(error = "Instrucciones enviadas") }
        }
    }

    private fun validarEntradas(): Boolean {
        if (_uiState.value.email.isEmpty() || !_uiState.value.email.contains("@")) {
            _uiState.update { it.copy(error = "Email no válido") }
            return false
        }
        if (_uiState.value.password.length < 6) {
            _uiState.update { it.copy(error = "Mínimo 6 caracteres") }
            return false
        }
        return true
    }

    fun consumeNavigationTarget() { _objetivoNavegacion.value = null }
}


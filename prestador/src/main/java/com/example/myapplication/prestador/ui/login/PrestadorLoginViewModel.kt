package com.example.myapplication.prestador.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging




@HiltViewModel
class PrestadorLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _hasProfile = MutableStateFlow(false)
    val hasProfile: StateFlow<Boolean> = _hasProfile

    private val _passwordResetEmailSent = MutableStateFlow(false)
    val passwordResetEmailSent: StateFlow<Boolean> = _passwordResetEmailSent

    fun login(email: String, password: String) {
        // Validar campos
        if (!validateInputs(email, password)) return

        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val result = authRepository.signInWithEmailAndPassword(email, password)
                
                result.onSuccess { user ->
                    // Verificar si el usuario tiene perfil completo
                    saveFcmToken(user.uid)
                    val profileExists = authRepository.checkUserProfileExists(user.uid)
                    _hasProfile.value = profileExists
                    _loginState.value = LoginState.Success
                }.onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun signInWithGoogle() {
        _loginState.value = LoginState.Loading
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val result = authRepository.signInWithGoogle(idToken)
                
                result.onSuccess { user ->
                    // Verificar si el usuario tiene perfil completo
                    saveFcmToken(user.uid)
                    val profileExists = authRepository.checkUserProfileExists(user.uid)
                    _hasProfile.value = profileExists
                    _loginState.value = LoginState.Success
                }.onFailure { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Error al iniciar sesión con Google")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error al iniciar sesión con Google")
            }
        }
    }

    fun resetPassword(email: String) {
        // Validar email vacío
        if (email.isEmpty()) {
            _loginState.value = LoginState.Error("Por favor ingresa tu email")
            return
        }

        // Validar formato de email
        if (!email.contains("@")) {
            _loginState.value = LoginState.Error("Email inválido")
            return
        }

        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val result = authRepository.sendPasswordResetEmail(email)

                result.onSuccess {
                    _loginState.value = LoginState.Idle
                    _passwordResetEmailSent.value = true
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
                    _loginState.value = LoginState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || !email.contains("@")) {
            _loginState.value = LoginState.Error("Ingresa un email válido")
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            _loginState.value = LoginState.Error("La contraseña debe tener al menos 6 caracteres")
            return false
        }

        return true
    }

    fun resetPasswordEmailSentFlag() {
        _passwordResetEmailSent.value = false
    }

    private fun saveFcmToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            // Usar update() en lugar de set(merge) para NO crear el documento si no existe.
            // Si el usuario nuevo aún no completó el registro, el documento no existe
            // y set(merge) lo creaba con solo fcmToken → checkUserProfileExists retornaba true
            // → saltaba el Register y mandaba al Dashboard.
            FirebaseFirestore.getInstance()
                .collection("providers")
                .document(uid)
                .update("fcmToken", token)
                .addOnFailureListener { /* ignorar: el doc no existe aún para usuarios nuevos */ }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
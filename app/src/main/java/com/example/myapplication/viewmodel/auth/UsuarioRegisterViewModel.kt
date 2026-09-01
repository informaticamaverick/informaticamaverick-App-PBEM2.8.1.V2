package com.example.myapplication.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datos.repositorios.UsuarioAutenticacionRepositorio
import com.example.myapplication.datos.repositorios.SincUsuarioRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- ESTADO DEL REGISTRO USUARIO (UI) ---
 */
data class EstadoUiRegisterUsuario(
    val email: String = "",
    val password: String = "",
    val estaCargando: Boolean = false,
    val exitoRegistro: Boolean = false,
    val error: String? = null
)

/**
 * --- VIEWMODEL DE REGISTRO CLIENTE (v2026.ELITE) ---
 * [LEY #9]: Nombres Completos en Español.
 */
@HiltViewModel
class UsuarioRegisterViewModel @Inject constructor(
    private val authRepository: UsuarioAutenticacionRepositorio,
    private val sincRepo: SincUsuarioRepositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadoUiRegisterUsuario())
    val uiState: StateFlow<EstadoUiRegisterUsuario> = _uiState.asStateFlow()

    // --- SECTOR: UI UPDATES ---

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }
    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, error = null) }

    // --- SECTOR: ACCIONES ---

    fun registrarse(email: String, clave: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(estaCargando = true, error = null) }
            authRepository.registrarseConEmailClave(email, clave)
                .onSuccess { usuario ->
                    // El registro es atómico; creamos semilla y lanzamos PULL Deep en background global
                    sincRepo.finalizarAcceso(usuario)
                    _uiState.update { it.copy(estaCargando = false, exitoRegistro = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(estaCargando = false, error = error.message ?: "Error de registro") }
                }
        }
    }
    
    fun resetState() {
        _uiState.value = EstadoUiRegisterUsuario()
    }
}


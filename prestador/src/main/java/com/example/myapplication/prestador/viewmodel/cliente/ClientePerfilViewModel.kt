package com.example.myapplication.prestador.viewmodel.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.repository.UserRepository
import com.example.myapplication.core.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientePerfilUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: User = User()
)

@HiltViewModel
class ClientePerfilViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        loadProfileShallow()
    }

    /**
     * LEY #3: Carga Shallow (Datos básicos necesarios para renderizar la pantalla)
     */
    private fun loadProfileShallow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userRepository.getUserById(clientId).collect { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, profile = user)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No se encontró el perfil del cliente"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }

    /**
     * LEY #3: Carga Deep (Solo si la UI necesita información adicional pesada)
     */
    fun loadProfileDeep() {
        // Implementación futura: Si el modelo User no trae todo (ej. historial completo de transacciones),
        // aquí llamaríamos a un método especializado del repositorio.
    }

    fun refreshProfile() {
        viewModelScope.launch {
            userRepository.refreshUserFromRemote()
            _refreshTick.value++
        }
    }
}
/*
// ARCHIVO ANTERIOR (EN DESUSO)
import com.example.myapplication.prestador.data.model.ClienteProfile
import com.example.myapplication.prestador.data.repository.ClienteRepository

data class ClientePerfilUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profile: ClienteProfile = ClienteProfile()
)

@HiltViewModel
class ClientePerfilViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    private val _refreshTick = MutableStateFlow(0)
    val refreshTick: StateFlow<Int> = _refreshTick.asStateFlow()

    init {
        observeClienteProfile()
    }

    private fun observeClienteProfile() {
        viewModelScope.launch {
            try {
                clienteRepository.observerClienteProfile(clientId).collect {
                    profile ->
                    if (profile != null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, profile = profile)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No se encontró el perfil del cliente"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }
    fun refreshProfile() {
        viewModelScope.launch {
            observeClienteProfile()
            _refreshTick.value++
        }
    }
}
*/

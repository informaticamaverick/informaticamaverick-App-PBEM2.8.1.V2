package com.example.myapplication.prestador.viewmodel.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.model.ClienteProfile
import com.example.myapplication.prestador.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.asStateFlow

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
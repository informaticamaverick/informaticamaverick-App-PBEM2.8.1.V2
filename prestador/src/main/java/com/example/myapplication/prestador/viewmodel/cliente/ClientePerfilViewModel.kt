package com.example.myapplication.prestador.viewmodel.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.model.ClienteProfile
import com.example.myapplication.prestador.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val clientId: String =
        checkNotNull(savedStateHandle["clientId"])

    private val _uiState = MutableStateFlow(ClientePerfilUiState())
    val uiState: StateFlow<ClientePerfilUiState> = _uiState.asStateFlow()

    init {
        loadClienteProfile()
    }

    private fun loadClienteProfile() {
        viewModelScope.launch {
            try {
                val profile =
                    clienteRepository.fetchClienteProfile(clientId)
                _uiState.value = _uiState.value.copy(isLoading = false,
                    profile = profile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el perfil: ${e.message}"
                )
            }
        }
    }
}
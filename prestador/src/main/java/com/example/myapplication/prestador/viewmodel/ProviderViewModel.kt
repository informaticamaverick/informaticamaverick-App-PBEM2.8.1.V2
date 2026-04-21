package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderViewModel @Inject constructor(
    private val repository: ProviderRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _provider = MutableStateFlow<ProviderEntity?>(null)
    val provider: StateFlow<ProviderEntity?> = _provider.asStateFlow()

    private val _providers = MutableStateFlow<List<ProviderEntity>>(emptyList())
    val providers: StateFlow<List<ProviderEntity>> = _providers.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadProvider(providerId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getProviderById(providerId).collect { provider ->
                    _provider.value = provider
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar prestador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllProviders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllProviders().collect { providers ->
                    _providers.value = providers
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar prestadores: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveProvider(provider)
                _successMessage.value = "Prestador guardado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar prestador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateProvider(provider)
                _successMessage.value = "Prestador actualizado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar prestador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteProvider(providerId)
                _successMessage.value = "Prestador eliminado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar prestador: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProviders(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.searchProviders(query).collect { providers ->
                    _providers.value = providers
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al buscar prestadores: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ClienteEntity
import com.example.myapplication.prestador.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteViewModel @Inject constructor(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cliente = MutableStateFlow<ClienteEntity?>(null)
    val cliente: StateFlow<ClienteEntity?> = _cliente.asStateFlow()

    private val _clientes = MutableStateFlow<List<ClienteEntity>>(emptyList())
    val clientes: StateFlow<List<ClienteEntity>> = _clientes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getClienteById(clienteId).collect { cliente ->
                    _cliente.value = cliente
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar cliente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAllClientes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllClientes().collect { clientes ->
                    _clientes.value = clientes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar clientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.saveCliente(cliente)
                _successMessage.value = "Cliente guardado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar cliente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCliente(cliente: ClienteEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateCliente(cliente)
                _successMessage.value = "Cliente actualizado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar cliente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteCliente(clienteId)
                _successMessage.value = "Cliente eliminado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar cliente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchClientes(nombre: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.searchClientesByNombre(nombre).collect { clientes ->
                    _clientes.value = clientes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al buscar clientes: ${e.message}"
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
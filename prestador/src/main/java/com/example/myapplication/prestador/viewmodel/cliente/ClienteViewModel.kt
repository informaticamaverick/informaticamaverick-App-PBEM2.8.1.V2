package com.example.myapplication.prestador.viewmodel.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.core.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClienteViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cliente = MutableStateFlow<User?>(null)
    val cliente: StateFlow<User?> = _cliente.asStateFlow()

    private val _clientes = MutableStateFlow<List<User>>(emptyList())
    val clientes: StateFlow<List<User>> = _clientes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getUserById(clienteId).collect { cliente ->
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
                repository.getAllUsers().collect { clientes ->
                    _clientes.value = clientes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar clientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCliente(cliente: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.syncUserWithFirebase(cliente)
                _successMessage.value = "Cliente guardado exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar cliente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCliente(clienteId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // El repositorio de usuarios no debería permitir borrar a otros de Firebase
                // Pero podemos limpiar el cache local si fuera necesario.
                // repository.clearLocalUser() // Esto limpia el perfil propio
                _successMessage.value = "Funcionalidad de borrado limitada por seguridad"
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
                repository.searchUsers(nombre).collect { clientes ->
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

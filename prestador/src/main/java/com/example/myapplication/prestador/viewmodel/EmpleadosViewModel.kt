package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import com.example.myapplication.prestador.data.repository.EmpleadoRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados para la UI de empleados
 */
sealed class EmpleadosUiState {
    object Loading : EmpleadosUiState()
    data class Success(val empleados: List<EmpleadoEntity>) : EmpleadosUiState()
    data class Error(val message: String) : EmpleadosUiState()
}

sealed class EmpleadoActionState {
    object Idle : EmpleadoActionState()
    object Loading : EmpleadoActionState()
    data class Success(val message: String) : EmpleadoActionState()
    data class Error(val message: String) : EmpleadoActionState()
}

/**
 * ViewModel para gestionar empleados del prestador
 */
@HiltViewModel
class EmpleadosViewModel @Inject constructor(
    private val empleadoRepository: EmpleadoRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<EmpleadosUiState>(EmpleadosUiState.Loading)
    val uiState: StateFlow<EmpleadosUiState> = _uiState.asStateFlow()
    
    private val _actionState = MutableStateFlow<EmpleadoActionState>(EmpleadoActionState.Idle)
    val actionState: StateFlow<EmpleadoActionState> = _actionState.asStateFlow()
    
    init {
        loadEmpleados()
    }
    
    /**
     * Cargar empleados del prestador actual
     */
    fun loadEmpleados() {
        viewModelScope.launch {
            try {
                val prestadorId = auth.currentUser?.uid 
                    ?: throw Exception("Usuario no autenticado")
                
                empleadoRepository.getEmpleadosByPrestadorId(prestadorId).collect { empleados ->
                    _uiState.value = EmpleadosUiState.Success(empleados)
                }
            } catch (e: Exception) {
                _uiState.value = EmpleadosUiState.Error(e.message ?: "Error al cargar empleados")
            }
        }
    }
    
    /**
     * Agregar nuevo empleado
     */
    fun addEmpleado(nombre: String, apellido: String, dni: String) {
        viewModelScope.launch {
            _actionState.value = EmpleadoActionState.Loading
            
            try {
                val prestadorId = auth.currentUser?.uid 
                    ?: throw Exception("Usuario no autenticado")
                
                // Validaciones
                if (nombre.isBlank()) {
                    _actionState.value = EmpleadoActionState.Error("El nombre es requerido")
                    return@launch
                }
                if (apellido.isBlank()) {
                    _actionState.value = EmpleadoActionState.Error("El apellido es requerido")
                    return@launch
                }
                if (dni.isBlank()) {
                    _actionState.value = EmpleadoActionState.Error("El DNI es requerido")
                    return@launch
                }
                if (dni.length < 7 || dni.length > 8) {
                    _actionState.value = EmpleadoActionState.Error("DNI debe tener 7 u 8 dígitos")
                    return@launch
                }
                if (!dni.all { it.isDigit() }) {
                    _actionState.value = EmpleadoActionState.Error("DNI debe contener solo números")
                    return@launch
                }
                
                val result = empleadoRepository.addEmpleado(
                    prestadorId = prestadorId,
                    nombre = nombre,
                    apellido = apellido,
                    dni = dni
                )
                
                if (result.isSuccess) {
                    _actionState.value = EmpleadoActionState.Success("Empleado agregado exitosamente")
                } else {
                    _actionState.value = EmpleadoActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al agregar empleado"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = EmpleadoActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Actualizar empleado existente
     */
    fun updateEmpleado(empleadoId: String, nombre: String, apellido: String, dni: String) {
        viewModelScope.launch {
            _actionState.value = EmpleadoActionState.Loading
            
            try {
                // Validaciones
                if (nombre.isBlank()) {
                    _actionState.value = EmpleadoActionState.Error("El nombre es requerido")
                    return@launch
                }
                if (apellido.isBlank()) {
                    _actionState.value = EmpleadoActionState.Error("El apellido es requerido")
                    return@launch
                }
                if (dni.isBlank()) {
                    _actionState.value = EmpleadoActionState.Error("El DNI es requerido")
                    return@launch
                }
                if (dni.length < 7 || dni.length > 8) {
                    _actionState.value = EmpleadoActionState.Error("DNI debe tener 7 u 8 dígitos")
                    return@launch
                }
                if (!dni.all { it.isDigit() }) {
                    _actionState.value = EmpleadoActionState.Error("DNI debe contener solo números")
                    return@launch
                }
                
                val result = empleadoRepository.updateEmpleado(
                    empleadoId = empleadoId,
                    nombre = nombre,
                    apellido = apellido,
                    dni = dni
                )
                
                if (result.isSuccess) {
                    _actionState.value = EmpleadoActionState.Success("Empleado actualizado exitosamente")
                } else {
                    _actionState.value = EmpleadoActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al actualizar empleado"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = EmpleadoActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Eliminar empleado
     */
    fun deleteEmpleado(empleadoId: String) {
        viewModelScope.launch {
            _actionState.value = EmpleadoActionState.Loading
            
            try {
                val result = empleadoRepository.deleteEmpleado(empleadoId)
                
                if (result.isSuccess) {
                    _actionState.value = EmpleadoActionState.Success("Empleado eliminado exitosamente")
                } else {
                    _actionState.value = EmpleadoActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al eliminar empleado"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = EmpleadoActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Resetear estado de acción
     */
    fun resetActionState() {
        _actionState.value = EmpleadoActionState.Idle
    }
}

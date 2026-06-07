package com.example.myapplication.prestador.viewmodel.empresa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.domain.model.EmployeeProvider
import com.example.myapplication.core.data.repository.ProviderRepository
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
    data class Success(val empleados: List<EmployeeProvider>) : EmpleadosUiState()
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
    private val providerRepository: ProviderRepository,
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
                
                providerRepository.getProviderFlowById(prestadorId).collect { provider ->
                    val allEmployees = provider?.companies?.flatMap { company ->
                        company.branches.flatMap { branch -> branch.team }
                    } ?: emptyList()
                    _uiState.value = EmpleadosUiState.Success(allEmployees)
                }
            } catch (e: Exception) {
                _uiState.value = EmpleadosUiState.Error(e.message ?: "Error al cargar empleados")
            }
        }
    }
    
    /**
     * Agregar nuevo empleado
     */
    fun addEmpleado(nombre: String, apellido: String, position: String) {
        viewModelScope.launch {
            _actionState.value = EmpleadoActionState.Loading
            
            try {
                val prestadorId = auth.currentUser?.uid 
                    ?: throw Exception("Usuario no autenticado")
                
                val currentProvider = providerRepository.getProviderByIdOnce(prestadorId)
                    ?: throw Exception("Perfil no encontrado")

                val newEmployee = EmployeeProvider(
                    name = nombre,
                    lastName = apellido,
                    position = position
                )

                // Agregamos a la primera sucursal de la primera empresa por defecto (Casa Central)
                val updatedCompanies = currentProvider.companies.toMutableList()
                if (updatedCompanies.isEmpty()) throw Exception("Debe crear una empresa primero")
                
                val firstComp = updatedCompanies[0]
                val updatedBranches = firstComp.branches.toMutableList()
                if (updatedBranches.isEmpty()) throw Exception("Debe crear una sucursal primero")
                
                val firstBranch = updatedBranches[0]
                updatedBranches[0] = firstBranch.copy(team = firstBranch.team + newEmployee)
                updatedCompanies[0] = firstComp.copy(branches = updatedBranches)

                providerRepository.syncProviderWithFirebase(currentProvider.copy(companies = updatedCompanies).toDomain())
                _actionState.value = EmpleadoActionState.Success("Empleado agregado exitosamente")
            } catch (e: Exception) {
                _actionState.value = EmpleadoActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Actualizar empleado existente
     */
    fun updateEmpleado(employeeId: String, nombre: String, apellido: String, position: String) {
        viewModelScope.launch {
            _actionState.value = EmpleadoActionState.Loading
            
            try {
                val prestadorId = auth.currentUser?.uid ?: return@launch
                val currentProvider = providerRepository.getProviderByIdOnce(prestadorId)
                    ?: return@launch

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.map { branch ->
                        branch.copy(team = branch.team.map { employee ->
                            if (employee.id == employeeId) {
                                employee.copy(name = nombre, lastName = apellido, position = position)
                            } else employee
                        })
                    })
                }

                providerRepository.syncProviderWithFirebase(currentProvider.copy(companies = updatedCompanies).toDomain())
                _actionState.value = EmpleadoActionState.Success("Empleado actualizado exitosamente")
            } catch (e: Exception) {
                _actionState.value = EmpleadoActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    /**
     * Eliminar empleado
     */
    fun deleteEmpleado(employeeId: String) {
        viewModelScope.launch {
            _actionState.value = EmpleadoActionState.Loading
            
            try {
                val prestadorId = auth.currentUser?.uid ?: return@launch
                val currentProvider = providerRepository.getProviderByIdOnce(prestadorId)
                    ?: return@launch

                var foundCompanyId: String? = null
                var foundBranchId: String? = null
                currentProvider.companies.forEach { company ->
                    company.branches.forEach { branch ->
                        if (branch.team.any { it.id == employeeId }) {
                            foundCompanyId = company.id
                            foundBranchId = branch.id
                        }
                    }
                }

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.map { branch ->
                        branch.copy(team = branch.team.filter { it.id != employeeId })
                    })
                }

                providerRepository.syncProviderWithFirebase(currentProvider.copy(companies = updatedCompanies).toDomain())
                if (foundCompanyId != null && foundBranchId != null) {
                    providerRepository.deleteEmployeeFromFirebase(foundCompanyId!!, foundBranchId!!, employeeId)
                }
                _actionState.value = EmpleadoActionState.Success("Empleado eliminado exitosamente")
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

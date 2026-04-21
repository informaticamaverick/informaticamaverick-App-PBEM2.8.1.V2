package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ProviderEntity
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.BranchProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.data.model.EmployeeProvider
import com.example.myapplication.prestador.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * VIEWMODEL para la gestión de Sucursales y Equipos del Prestador.
 * [REFACTORED] Ahora utiliza ProviderRepository como única fuente de verdad (SSOT).
 * Las sucursales y empleados se gestionan dentro de la jerarquía de ProviderEntity.
 */
@HiltViewModel
class SucursalesViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val providerId: String
        get() = auth.currentUser?.uid ?: ""

    // Observamos el Provider para obtener la jerarquía completa
    private val _provider = MutableStateFlow<ProviderEntity?>(null)

    // businessId reactivo: ID de la primera empresa del prestador (o null)
    val businessId: StateFlow<String?> = _provider
        .map { it?.companies?.firstOrNull()?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Sucursales reactivas desde el Provider
    val sucursales: StateFlow<List<BranchProvider>> = _provider
        .map { it?.companies?.flatMap { comp -> comp.branches } ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadProviderHierarchy()
    }

    private fun loadProviderHierarchy() {
        if (providerId.isBlank()) return
        viewModelScope.launch {
            providerRepository.getProviderById(providerId).collect { provider ->
                _provider.value = provider
            }
        }
    }

    // --- MANTENEMOS LOS ESTADOS DE UI POR COMPATIBILIDAD ---
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    /**
     * Agrega una sucursal a la primera empresa del prestador.
     */
    fun addSucursal(
        nombre: String,
        provincia: String?,
        localidad: String?,
        calle: String?,
        numero: String?,
        cp: String?,
        horario: String?
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: throw Exception("Perfil no encontrado")
                
                if (currentProvider.companies.isEmpty()) {
                    throw Exception("Debe crear una empresa primero")
                }

                val newBranch = BranchProvider(
                    id = UUID.randomUUID().toString(),
                    name = nombre,
                    workingHours = horario ?: "",
                    address = AddressProvider(
                        id = UUID.randomUUID().toString(),
                        calle = calle ?: "",
                        numero = numero ?: "",
                        localidad = localidad ?: "",
                        provincia = provincia ?: "",
                        codigoPostal = cp ?: ""
                    ),
                    hasPhysicalLocation = true
                )

                // Actualizar jerarquía (agregamos a la primera empresa por defecto)
                val updatedCompanies = currentProvider.companies.toMutableList()
                val firstComp = updatedCompanies[0]
                updatedCompanies[0] = firstComp.copy(
                    branches = firstComp.branches + newBranch
                )

                val updatedProvider = currentProvider.copy(companies = updatedCompanies)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())

                _uiState.value = UiState.Success("Sucursal agregada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar sucursal")
            }
        }
    }

    /**
     * Actualiza los datos de una sucursal existente.
     */
    fun updateBranch(updatedBranch: BranchProvider) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: throw Exception("Perfil no encontrado")

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.map { branch ->
                        if (branch.id == updatedBranch.id) updatedBranch else branch
                    })
                }

                val updatedProvider = currentProvider.copy(companies = updatedCompanies)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                
                _uiState.value = UiState.Success("Sucursal actualizada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar sucursal")
            }
        }
    }

    /**
     * Elimina una sucursal por ID.
     */
    fun deleteSucursal(sucursalId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: throw Exception("Perfil no encontrado")

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.filter { it.id != sucursalId })
                }

                val updatedProvider = currentProvider.copy(companies = updatedCompanies)
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())

                _uiState.value = UiState.Success("Sucursal eliminada correctamente")
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar sucursal")
            }
        }
    }

    /**
     * Gestiona el equipo de trabajo de una sucursal.
     */
    fun agregarMiembroEquipo(sucursalId: String, nombre: String, apellido: String?, cargo: String?, imageUrl: String? = null) {
        viewModelScope.launch {
            try {
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: return@launch

                val newEmployee = EmployeeProvider(
                    id = UUID.randomUUID().toString(),
                    name = nombre,
                    lastName = apellido ?: "",
                    position = cargo ?: "",
                    photoUrl = imageUrl
                )

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.map { branch ->
                        if (branch.id == sucursalId) {
                            branch.copy(employees = branch.employees + newEmployee)
                        } else branch
                    })
                }

                providerRepository.syncProviderWithFirebase(currentProvider.copy(companies = updatedCompanies).toDomain())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al agregar miembro")
            }
        }
    }

    /**
     * Elimina un miembro del equipo.
     */
    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            try {
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: return@launch

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.map { branch ->
                        branch.copy(employees = branch.employees.filter { it.id != employeeId })
                    })
                }

                providerRepository.syncProviderWithFirebase(currentProvider.copy(companies = updatedCompanies).toDomain())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al eliminar miembro")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
    
    /** No-op: businessId ahora es reactivo y se actualiza automáticamente. */
    fun refreshBusinessId() = Unit

    /**
     * Actualiza el encargado de una sucursal (primer empleado de la lista por convención o lógica similar)
     * O simplemente actualiza los datos si ya existe.
     * En este modelo jerárquico, el "encargado" es solo un empleado.
     */
    fun updateManager(sucursalId: String, nombre: String, apellido: String?, cargo: String?, imageUrl: String?) {
        viewModelScope.launch {
            try {
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: return@launch

                val updatedCompanies = currentProvider.companies.map { company ->
                    company.copy(branches = company.branches.map { branch ->
                        if (branch.id == sucursalId) {
                            // Si no hay empleados, agregamos uno. Si hay, actualizamos el primero.
                            val updatedEmployees = if (branch.employees.isEmpty()) {
                                listOf(EmployeeProvider(name = nombre, lastName = apellido ?: "", position = cargo ?: "Encargado", photoUrl = imageUrl))
                            } else {
                                val first = branch.employees[0]
                                listOf(first.copy(name = nombre, lastName = apellido ?: "", position = cargo ?: first.position, photoUrl = imageUrl)) + branch.employees.drop(1)
                            }
                            branch.copy(employees = updatedEmployees)
                        } else branch
                    })
                }

                providerRepository.syncProviderWithFirebase(currentProvider.copy(companies = updatedCompanies).toDomain())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error al actualizar encargado")
            }
        }
    }
     
    // @Deprecated("Utilizar agregarMiembroEquipo con la nueva estructura")
    // fun desactivarMiembroEquipo(referenteId: String) { ... }

    // @Deprecated("Las direcciones ahora se gestionan dentro de BranchProvider")
    // fun guardarDireccionSucursal(...) { ... }
}

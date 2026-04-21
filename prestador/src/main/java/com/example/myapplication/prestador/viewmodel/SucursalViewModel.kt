package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
// import com.example.myapplication.prestador.data.local.entity.SucursalEntity
// import com.example.myapplication.prestador.data.repository.SucursalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * [REDUNDANTE] - Este ViewModel ha sido neutralizado en favor de SucursalesViewModel (plural).
 * La gestión de sucursales ahora se realiza a través de la jerarquía de ProviderEntity
 * centralizada en ProviderRepository (SSOT).
 */
@Deprecated("Usar SucursalesViewModel para la gestión jerárquica de sucursales")
@HiltViewModel
class SucursalViewModel @Inject constructor(
    // private val repository: SucursalRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Comentado para evitar errores de compilación con repositorios obsoletos
    /*
    private val _sucursal = MutableStateFlow<SucursalEntity?>(null)
    val sucursal: StateFlow<SucursalEntity?> = _sucursal.asStateFlow()

    private val _sucursales = MutableStateFlow<List<SucursalEntity>>(emptyList())
    val sucursales: StateFlow<List<SucursalEntity>> = _sucursales.asStateFlow()
    */

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // =========================================================================
    // SECCIÓN: LÓGICA NEUTRALIZADA
    // Las operaciones de sucursales ahora residen en SucursalesViewModel
    // =========================================================================

    fun loadSucursal(sucursalId: String) {
        // Operación delegada a SucursalesViewModel (Observación de ProviderEntity)
    }

    fun loadSucursalesByBusiness(businessId: String) {
        // Operación delegada a SucursalesViewModel
    }

    fun loadActiveSucursales(businessId: String) {
        // Operación delegada a SucursalesViewModel
    }

    /*
    fun saveSucursal(sucursal: SucursalEntity) { }
    fun updateSucursal(sucursal: SucursalEntity) { }
    fun deleteSucursal(sucursalId: String) { }
    fun updateSucursalStatus(sucursalId: String, isActive: Boolean) { }
    fun searchSucursales(name: String) { }
    */

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

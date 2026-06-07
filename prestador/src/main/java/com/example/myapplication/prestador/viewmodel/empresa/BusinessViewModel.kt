package com.example.myapplication.prestador.viewmodel.empresa

import android.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * VIEWMODEL para la gestión de Empresas del Prestador.
 * [REFACTORED] Ahora utiliza ProviderRepository como única fuente de verdad (SSOT).
 * Toda la informacián de empresas se extrae y guarda dentro del ProviderEntity jerárquico.
 */
@HiltViewModel
class BusinessViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val providerId: String
        get() = auth.currentUser?.uid ?: ""

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Observamos el ProviderEntity completo para extraer las empresas
    private val _provider = MutableStateFlow<ProviderEntity?>(null)
    
    // Lista de empresas extraída del Provider
    private val _businesses = MutableStateFlow<List<CompanyProvider>>(emptyList())
    val businesses: StateFlow<List<CompanyProvider>> = _businesses.asStateFlow()

    // Empresa seleccionada (para edición)
    private val _selectedBusiness = MutableStateFlow<CompanyProvider?>(null)
    val business: StateFlow<CompanyProvider?> = _selectedBusiness.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    init {
        loadBusinesses()
    }

    /**
     * Carga todas las empresas asociadas al perfil del prestador.
     */
    fun loadBusinesses() {
        if (providerId.isBlank()) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                providerRepository.getProviderFlowById(providerId).collect { provider ->
                    _provider.value = provider
                    _businesses.value = provider?.companies ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar empresas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga una empresa específica por ID desde el estado local.
     */
    fun loadBusiness(businessId: String) {
        val found = _businesses.value.find { it.id == businessId }
        _selectedBusiness.value = found
    }

    /**
     * Guarda o actualiza una empresa en el perfil jerárquico del prestador.
     */
    fun saveBusiness(company: CompanyProvider) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: throw Exception("No se encontró el perfil del prestador")

                // Actualizar la lista de empresas
                val updatedCompanies = currentProvider.companies.toMutableList()
                val index = updatedCompanies.indexOfFirst { it.id == company.id }
                
                if (index != -1) {
                    updatedCompanies[index] = company
                } else {
                    updatedCompanies.add(company)
                }

                val updatedProvider = currentProvider.copy(
                    companies = updatedCompanies
                )
                //Solo sincornizar si hubo cambios reales
                if (updatedProvider.companies == currentProvider.companies)
                {
                    _successMessage.value = "Sin cambios"
                    return@launch
                }
                //Sincronizacion SSOT: Room + Firebase
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())

                // Sincronización SSOT: Room + Firebase
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                
                _successMessage.value = "Empresa guardada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar empresa: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una empresa del perfil jerárquico.
     */
    fun deleteBusiness(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentProvider = providerRepository.getProviderByIdOnce(providerId)
                    ?: throw Exception("No se encontró el perfil del prestador")

                val updatedCompanies = currentProvider.companies.filter { it.id != businessId }
                val updatedProvider = currentProvider.copy(
                    companies = updatedCompanies
                )

                // Sincronización SSOT
                providerRepository.syncProviderWithFirebase(updatedProvider.toDomain())
                
                _successMessage.value = "Empresa eliminada exitosamente"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar empresa: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- MÉTODOS OBSOLETOS O REDIRIGIDOS ---
    @Deprecated("Utilizar loadBusinesses", ReplaceWith("loadBusinesses()"))
    fun loadBusinessesByProvider(providerId: String) = loadBusinesses()

    @Deprecated("Utilizar saveBusiness con CompanyProvider")
    fun updateBusiness(company: CompanyProvider) = saveBusiness(company)

    fun clearMessage() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}

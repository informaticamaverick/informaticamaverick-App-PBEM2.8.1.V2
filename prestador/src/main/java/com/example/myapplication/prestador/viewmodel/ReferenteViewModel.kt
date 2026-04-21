package com.example.myapplication.prestador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import com.example.myapplication.prestador.data.repository.ReferenteRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ReferentesUiState {
    object Loading : ReferentesUiState()
    data class Success(val referentes: List<ReferenteEntity>) : ReferentesUiState()
    data class Error(val message: String) : ReferentesUiState()
}

sealed class ReferenteActionState {
    object Idle : ReferenteActionState()
    object Loading : ReferenteActionState()
    data class Success(val message: String) : ReferenteActionState()
    data class Error(val message: String) : ReferenteActionState()
}

@HiltViewModel
class ReferenteViewModel @Inject constructor(
    private val referenteRepository: ReferenteRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReferentesUiState>(ReferentesUiState.Loading)
    val uiState: StateFlow<ReferentesUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ReferenteActionState>(ReferenteActionState.Idle)
    val actionState: StateFlow<ReferenteActionState> = _actionState.asStateFlow()

    /**
     * Carga los referentes del prestador actual (todos).
     */
    fun loadReferentesByProvider() {
        viewModelScope.launch {
            _uiState.value = ReferentesUiState.Loading
            try {
                val providerId = auth.currentUser?.uid
                    ?: throw Exception("Usuario no autenticado")
                val referentes = referenteRepository.getReferentesByProvider(providerId)
                _uiState.value = ReferentesUiState.Success(referentes)
            } catch (e: Exception) {
                _uiState.value = ReferentesUiState.Error(e.message ?: "Error al cargar referentes")
            }
        }
    }

    /**
     * Carga los referentes de una empresa específica.
     */
    fun loadReferentesByEmpresa(empresaId: String) {
        viewModelScope.launch {
            _uiState.value = ReferentesUiState.Loading
            try {
                val referentes = referenteRepository.getReferentesByEmpresa(empresaId)
                _uiState.value = ReferentesUiState.Success(referentes)
            } catch (e: Exception) {
                _uiState.value = ReferentesUiState.Error(e.message ?: "Error al cargar referentes de empresa")
            }
        }
    }

    /**
     * Carga los referentes de una sucursal específica.
     */
    fun loadReferentesBySucursal(sucursalId: String) {
        viewModelScope.launch {
            _uiState.value = ReferentesUiState.Loading
            try {
                val referentes = referenteRepository.getReferentesBySucursal(sucursalId)
                _uiState.value = ReferentesUiState.Success(referentes)
            } catch (e: Exception) {
                _uiState.value = ReferentesUiState.Error(e.message ?: "Error al cargar referentes de sucursal")
            }
        }
    }

    /**
     * Agrega un nuevo referente al prestador actual.
     */
    fun addReferente(
        nombre: String,
        apellido: String? = null,
        cargo: String? = null,
        imageUrl: String? = null,
        empresaId: String? = null,
        sucursalId: String? = null
    ) {
        viewModelScope.launch {
            _actionState.value = ReferenteActionState.Loading
            try {
                val providerId = auth.currentUser?.uid
                    ?: throw Exception("Usuario no autenticado")

                val result = referenteRepository.addReferente(
                    providerId = providerId,
                    nombre = nombre,
                    apellido = apellido,
                    cargo = cargo,
                    imageUrl = imageUrl,
                    empresaId = empresaId,
                    sucursalId = sucursalId
                )

                if (result.isSuccess) {
                    _actionState.value = ReferenteActionState.Success("Referente agregado exitosamente")
                    // Recargar lista
                    if (empresaId != null) loadReferentesByEmpresa(empresaId)
                    else if (sucursalId != null) loadReferentesBySucursal(sucursalId)
                    else loadReferentesByProvider()
                } else {
                    _actionState.value = ReferenteActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al agregar referente"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ReferenteActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Actualiza un referente existente.
     */
    fun updateReferente(referente: ReferenteEntity) {
        viewModelScope.launch {
            _actionState.value = ReferenteActionState.Loading
            try {
                val result = referenteRepository.updateReferente(referente)
                if (result.isSuccess) {
                    _actionState.value = ReferenteActionState.Success("Referente actualizado exitosamente")
                    // Recargar lista según contexto
                    when {
                        referente.empresaId != null -> loadReferentesByEmpresa(referente.empresaId)
                        referente.sucursalId != null -> loadReferentesBySucursal(referente.sucursalId)
                        else -> loadReferentesByProvider()
                    }
                } else {
                    _actionState.value = ReferenteActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al actualizar referente"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ReferenteActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Desactiva (soft delete) un referente.
     */
    fun desactivarReferente(referenteId: String, empresaId: String? = null, sucursalId: String? = null) {
        viewModelScope.launch {
            _actionState.value = ReferenteActionState.Loading
            try {
                val result = referenteRepository.desactivarReferente(referenteId)
                if (result.isSuccess) {
                    _actionState.value = ReferenteActionState.Success("Referente eliminado")
                    when {
                        empresaId != null -> loadReferentesByEmpresa(empresaId)
                        sucursalId != null -> loadReferentesBySucursal(sucursalId)
                        else -> loadReferentesByProvider()
                    }
                } else {
                    _actionState.value = ReferenteActionState.Error(
                        result.exceptionOrNull()?.message ?: "Error al desactivar referente"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ReferenteActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ReferenteActionState.Idle
    }
}

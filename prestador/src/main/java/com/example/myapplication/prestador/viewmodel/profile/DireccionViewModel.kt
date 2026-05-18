package com.example.myapplication.prestador.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.repository.DireccionFirestoreSync
import com.example.myapplication.prestador.data.repository.DireccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ARCHIVO REDUNDANTE - CONSOLIDADO EN [EditProfileViewModel] y [ProviderRepository]
 * 
 * SECCIÓN: NOTA DE MIGRACIÓN
 * Este ViewModel ha quedado obsoleto tras la implementación del Single Source of Truth (SSOT).
 * Las direcciones ya no se gestionan como entidades independientes en una tabla plana, 
 * sino como parte de la jerarquía de [ProviderEntity].
 * 
 * Reemplazos:
 * - Carga: Ahora se realiza automáticamente al cargar el perfil en EditProfileViewModel.
 * - Guardado: Usar 'viewModel.saveAdditionalAddress(AddressProvider)' en EditProfileViewModel.
 * - Sincronización: Delegada a 'ProviderRepository.syncProviderWithFirebase'.
 */

sealed class DireccionUiState {
    object Idle : DireccionUiState()
    object Loading : DireccionUiState()
    data class Success(val direccion: DireccionEntity?) : DireccionUiState()
    data class Error(val message: String) : DireccionUiState()
}

sealed class DireccionActionState {
    object Idle : DireccionActionState()
    object Loading : DireccionActionState()
    data class Success(val message: String) : DireccionActionState()
    data class Error(val message: String) : DireccionActionState()
}

@HiltViewModel
class DireccionViewModel @Inject constructor(
    private val direccionRepository: DireccionRepository,
    private val sync: DireccionFirestoreSync
) : ViewModel() {

    private val _uiState = MutableStateFlow<DireccionUiState>(DireccionUiState.Idle)
    val uiState: StateFlow<DireccionUiState> = _uiState.asStateFlow()

    private val _consultorioState = MutableStateFlow<DireccionUiState>(DireccionUiState.Idle)
    val consultorioState: StateFlow<DireccionUiState> = _consultorioState.asStateFlow()

    private val _actionState = MutableStateFlow<DireccionActionState>(DireccionActionState.Idle)
    val actionState: StateFlow<DireccionActionState> = _actionState.asStateFlow()

    /* 
    // =========================================================================
    // SECCIÓN: CÓDIGO COMENTADO (OBSOLETO)
    // El "trabajo sucio" ahora es gestionado por EditProfileViewModel
    // =========================================================================

    /**
     * Carga la dirección asociada a una entidad (prestador, empresa o sucursal).
     */
    fun loadDireccion(referenciaId: String, referenciaTipo: String) {
        viewModelScope.launch {
            _uiState.value = DireccionUiState.Loading
            try {
                // val direccion = sync.sincronizar(referenciaId, referenciaTipo)
                // _uiState.value = DireccionUiState.Success(direccion)
            } catch (e: Exception) {
                _uiState.value = DireccionUiState.Error(e.message ?: "Error al cargar dirección")
            }
        }
    }


    fun loadConsultorioDireccion(referenciaId: String) {
        viewModelScope.launch {
            _consultorioState.value = DireccionUiState.Loading
            try {
                // val direccion = sync.sincronizar(referenciaId, "CONSULTORIO")
                // _consultorioState.value = DireccionUiState.Success(direccion)
            } catch (e: Exception) {
                _consultorioState.value = DireccionUiState.Error(e.message ?: "Error")
            }
        }
    }

    /**
     * Guarda o actualiza la dirección de una entidad (upsert).
     */
    fun guardarDireccion(
        referenciaId: String,
        referenciaTipo: String,
        pais: String = "Argentina",
        provincia: String? = null,
        localidad: String? = null,
        codigoPostal: String? = null,
        calle: String? = null,
        numero: String? = null,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        viewModelScope.launch {
            _actionState.value = DireccionActionState.Loading
            try {
                // Lógica movida a EditProfileViewModel.saveAdditionalAddress
                // utilizando el modelo jerárquico Provider -> AddressProvider
                _actionState.value = DireccionActionState.Error("Usar EditProfileViewModel para guardar")
            } catch (e: Exception) {
                _actionState.value = DireccionActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Elimina la dirección de una entidad.
     */
    fun eliminarDireccion(direccion: DireccionEntity) {
        viewModelScope.launch {
            _actionState.value = DireccionActionState.Loading
            try {
                // Lógica movida a EditProfileViewModel.removeAdditionalAddress
                _actionState.value = DireccionActionState.Error("Usar EditProfileViewModel para eliminar")
            } catch (e: Exception) {
                _actionState.value = DireccionActionState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    */

    fun resetActionState() {
        _actionState.value = DireccionActionState.Idle
    }
}

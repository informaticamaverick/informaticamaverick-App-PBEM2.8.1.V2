package com.example.myapplication.prestador.viewmodel.cliente

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.dao.DireccionDao
import com.example.myapplication.core.datos.local.dao.IdentidadUsuarioDao
import com.example.myapplication.core.datos.local.entidades.DireccionEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.core.dominio.motores.MotorSincLocal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class ClientePerfilUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val identidad: IdentidadUsuarioEntity? = null,
    val direcciones: List<DireccionEntity> = emptyList(),
    val estaDetectandoGps: Boolean = false
)

@HiltViewModel
class ClientePerfilViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val contexto: android.content.Context,
    savedStateHandle: SavedStateHandle,
    private val motorLocal: MotorSincLocal,
    private val usuarioDao: IdentidadUsuarioDao,
    private val direccionDao: DireccionDao
) : ViewModel() {

    private val clientId: String = savedStateHandle["clientId"] ?: ""

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ClientePerfilUiState> = if (clientId.isBlank()) {
        MutableStateFlow(ClientePerfilUiState(isLoading = false, error = "ID de cliente no especificado"))
    } else {
        combine(
            usuarioDao.obtenerPorId(clientId),
            direccionDao.obtenerPorPropietario(clientId)
        ) { identidad, dirs ->
            ClientePerfilUiState(
                isLoading = false,
                identidad = identidad,
                direcciones = dirs
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientePerfilUiState())
    }

    init {
        if (clientId.isNotBlank()) refreshProfile()
    }

    fun refreshProfile() {
        viewModelScope.launch {
            motorLocal.impactarUsuarioDeep(clientId)
        }
    }
}
















































package com.example.myapplication.prestador.ui.pantallas.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.datos.indices.busqueda.IndiceBusquedaPrestadorRepositorio
import com.example.myapplication.core.datos.local.dao.IdentidadPrestadorDao
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceConfig(
    val hasPhysicalStore: Boolean = false,
    val is24Hours: Boolean = false,
    val hasHomeVisits: Boolean = false,
    val hasStoreAppointments: Boolean = false,
    val services: List<String> = emptyList()
)

@HiltViewModel
class ServiceConfigViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val repoIndice: IndiceBusquedaPrestadorRepositorio,
    private val prestadorDao: IdentidadPrestadorDao
) : ViewModel() {

    private val _configState = MutableStateFlow<ServiceConfig?>(null)
    val configState: StateFlow<ServiceConfig?> = _configState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadCurrentConfig() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val perfil = prestadorDao.obtenerPorId(userId).firstOrNull()
                if (perfil != null) {
                    val config = ServiceConfig(
                        hasPhysicalStore = false, // TODO: Obtener de dirección principal si existe
                        is24Hours = perfil.atiende24Horas,
                        hasHomeVisits = perfil.visitaADomicilio,
                        hasStoreAppointments = perfil.brindaTurnos,
                        services = perfil.idCategorias
                    )
                    _configState.value = config
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar configuración: ${e.message}"
            }
        }
    }

    fun saveConfiguration(
        hasPhysicalStore: Boolean,
        is24Hours: Boolean,
        hasHomeVisits: Boolean,
        hasStoreAppointments: Boolean,
        services: List<String>
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val actual = prestadorDao.obtenerPorId(userId).firstOrNull() ?: IdentidadPrestadorEntity(id = userId)
                
                val actualizada = actual.copy(
                    atiende24Horas = is24Hours,
                    visitaADomicilio = hasHomeVisits,
                    brindaTurnos = hasStoreAppointments,
                    idCategorias = services,
                    ultimaSincronizacion = System.currentTimeMillis()
                )

                prestadorDao.insertar(actualizada)
                repoIndice.sincronizarTodoElDescubrimiento(userId)

                _configState.value = ServiceConfig(
                    hasPhysicalStore, is24Hours, hasHomeVisits, hasStoreAppointments, services
                )
                _isLoading.value = false
                
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }
}
















































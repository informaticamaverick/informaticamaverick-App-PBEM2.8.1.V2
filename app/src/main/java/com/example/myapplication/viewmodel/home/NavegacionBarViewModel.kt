package com.example.myapplication.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.coordinadores.CoordinadorAcciones
import com.example.myapplication.core.datos.repositorios.AlertasSoberanasRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NavegacionBarViewModel.kt
 * Propósito: Coordinador táctico de la barra de navegación.
 * Funcionamiento: Gestiona la visibilidad, el contexto HUD y las alertas de notificación.
 * Relación: Escucha el Coordinador Global y el Repositorio de Alertas Soberanas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NavegacionBarViewModel @Inject constructor(
    private val actionCoordinator: CoordinadorAcciones,
    private val navCoordinator: com.example.myapplication.coordinadores.CoordinadorNavegacion, // 🔥 [NEW]
    private val alertasRepo: AlertasSoberanasRepositorio
) : ViewModel() {

    // --- ESTADOS DE VISIBILIDAD Y CONTEXTO ---
    // [v2026.ELITE]: La visibilidad ahora es dictada por el contrato soberano activo.
    val esBarraInferiorVisible = navCoordinator.contratoActivo.map { it.mostrarBarraNavegacion }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // --- ESTADOS DE ALERTAS (NOTIFICACIONES) ---
    // [ELITE]: Consumimos del Repositorio Soberano unificado
    val alertasNavegacion = actionCoordinator.idPerfilSeleccionado
        .flatMapLatest { idPerfil ->
            if (idPerfil == null) flowOf(AlertasSoberanasRepositorio.AlertasGlobales())
            else alertasRepo.observarAlertasParaIdentidad(idPerfil)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertasSoberanasRepositorio.AlertasGlobales())

    /**
     * Notifica un cambio de ruta (Solo logs y analíticas).
     * [ELITE]: Al cambiar de destino principal, reseteamos el estado de multiselección (UX Estándar).
     */
    fun alCambiarRuta(route: String?) {
        android.util.Log.d("NavegacionBarVM", "🚀 [ROUTE_CHANGE] Route: $route")
        // 🔥 [UX RESET]: Navegar entre pestañas de la barra inferior limpia contextos locales.
        actionCoordinator.actualizarMultiseleccion(false)
    }

    /**
     * Dispara una acción global a través del coordinador.
     */
    fun dispararAccion(actionId: String) {
        viewModelScope.launch {
            actionCoordinator.dispararAccion(actionId)
        }
    }
}

package com.example.myapplication.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datos.repositorios.AjustesNotificacion
import com.example.myapplication.datos.repositorios.UsuarioConfiguracionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- USUARIO CONFIGURACIÓN VIEWMODEL (ELITE v1.0) ---
 * [LEY #9]: Estándar en Español.
 * Obrero encargado de la gestión de preferencias del usuario.
 */
@HiltViewModel
class UsuarioConfiguracionViewModel @Inject constructor(
    private val repositorio: UsuarioConfiguracionRepositorio
) : ViewModel() {

    val ajustes: StateFlow<AjustesNotificacion> = repositorio.ajustesNotificacion
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AjustesNotificacion()
        )

    fun actualizarNotifChat(habilitada: Boolean) {
        viewModelScope.launch { repositorio.actualizarNotifChat(habilitada) }
    }

    fun actualizarNotifCalendario(habilitada: Boolean) {
        viewModelScope.launch { repositorio.actualizarNotifCalendario(habilitada) }
    }

    fun actualizarNotifLicitaciones(habilitada: Boolean) {
        viewModelScope.launch { repositorio.actualizarNotifLicitaciones(habilitada) }
    }

    fun actualizarNotifPromociones(habilitada: Boolean) {
        viewModelScope.launch { repositorio.actualizarNotifPromociones(habilitada) }
    }
}



































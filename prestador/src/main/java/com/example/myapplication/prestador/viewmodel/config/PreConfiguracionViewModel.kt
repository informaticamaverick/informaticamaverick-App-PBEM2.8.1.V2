package com.example.myapplication.prestador.viewmodel.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.repositorios.ConfiguracionRepositorio
import com.example.myapplication.core.datos.repositorios.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE CONFIGURACIÓN PRESTADOR (ELITE v2026.FINAL) ---
 */
@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: ConfiguracionRepositorio
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repository.modoTema
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val notifMessages: StateFlow<Boolean> = repository.notifMensajes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notifPresupuestos: StateFlow<Boolean> = repository.notifPresupuestos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notifPedidos: StateFlow<Boolean> = repository.notifPedidos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(modo: ThemeMode) {
        viewModelScope.launch { repository.establecerModoTema(modo) }
    }

    fun setNotifMessages(enabled: Boolean) {
        viewModelScope.launch { repository.establecerNotifMensajes(enabled) }
    }

    fun setNotifPresupuestos(enabled: Boolean) {
        viewModelScope.launch { repository.establecerNotifPresupuestos(enabled) }
    }

    fun setNotifPedidos(enabled: Boolean) {
        viewModelScope.launch { repository.establecerNotifPedidos(enabled) }
    }
}

/** Legacy alias */
typealias PreConfiguracionViewModel = AppSettingsViewModel















































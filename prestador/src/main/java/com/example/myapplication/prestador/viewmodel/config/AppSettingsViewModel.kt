package com.example.myapplication.prestador.viewmodel.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.repository.AppSettingsRepository
import com.example.myapplication.prestador.data.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: AppSettingsRepository
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            ThemeMode.SYSTEM)

    val notifMessages: StateFlow<Boolean> = repository.notifMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notifPresupuestos: StateFlow<Boolean> = repository.notifPresupuestos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notifPedidos: StateFlow<Boolean> = repository.notifPedidos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setNotifMessages(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifMessages(enabled) }
    }

    fun setNotifPresupuestos(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifPresupuestos(enabled) }
    }

    fun setNotifPedidos(enabled: Boolean) {
        viewModelScope.launch { repository.setNotifPedidos(enabled) }
    }
}

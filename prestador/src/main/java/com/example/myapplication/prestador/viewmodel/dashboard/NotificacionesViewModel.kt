package com.example.myapplication.prestador.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.prestador.data.model.NotificacionItem
import com.example.myapplication.prestador.data.model.TipoNotificacion
import com.example.myapplication.prestador.data.repository.NotificacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificacionesViewModel @Inject constructor(
    private val repo: NotificacionRepository) : ViewModel() {
        private val _filtroTipo = MutableStateFlow<TipoNotificacion?>(null)
    val filtroTipo: StateFlow<TipoNotificacion?> = _filtroTipo.asStateFlow()

    private val _soloNoLeidas = MutableStateFlow(false)
    val soloNoLeidas: StateFlow<Boolean> = _soloNoLeidas.asStateFlow()

    val unreadCount: StateFlow<Int> = repo.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notificaciones: StateFlow<List<NotificacionItem>> = combine(_filtroTipo, _soloNoLeidas) {
        tipo, soloNoLeidas ->
        Pair(tipo, soloNoLeidas)
    }.flatMapLatest { (tipo, soloNoLeidas) ->
        when{
            tipo != null -> repo.getByTipoFlow(tipo)
            soloNoLeidas -> repo.getUnreadFlow()
            else -> repo.getAllFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    fun setFiltroTipo(tipo: TipoNotificacion?)
    { _filtroTipo.value = tipo }
    fun toggleSoloNoLeidas() {
        _soloNoLeidas.value = !_soloNoLeidas.value }
    fun marcarLeida(id: Long) =
        viewModelScope.launch { repo.marcarLeida(id) }
    fun marcarTodasLeidas() = viewModelScope.launch {
            repo.marcarTodasLeidas() }
    fun eliminar(id: Long) =
        viewModelScope.launch { repo.eliminar(id) }
    }
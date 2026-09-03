package com.example.myapplication.prestador.viewmodel.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.datos.local.entidades.ConversacionEntity
import com.example.myapplication.core.datos.repositorios.ChatMotorSincRepositorio
import com.example.myapplication.prestador.datos.repositorios.PrestadorAutenticacionRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * --- VIEWMODEL DE LISTA DE CHATS (PRO - v2026.ELITE) ---
 * [PROPÓSITO]: Gestionar la bandeja de entrada del profesional, permitiendo 
 * alternar entre perfiles personales y corporativos.
 * [LEY #9]: Idioma Español.
 */
@HiltViewModel
class PrestadorListaChatsViewModel @Inject constructor(
    private val chatRepository: ChatMotorSincRepositorio,
    private val authRepository: PrestadorAutenticacionRepositorio
) : ViewModel() {

    // [FIX]: "TODAS" (agrupar todas las sucursales de una empresa) necesita filtrar por
    // varios ids a la vez, no uno solo — cada conversación queda etiquetada con el id de
    // UNA sucursal puntual, nunca con el id de la empresa.
    private val _idsIdentidadActiva = MutableStateFlow<List<String>>(emptyList())
    val idsIdentidadActiva = _idsIdentidadActiva.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val conversaciones: StateFlow<List<ConversacionEntity>> = _idsIdentidadActiva
        .filter { it.isNotEmpty() }
        .flatMapLatest { ids ->
            chatRepository.obtenerConversaciones(ids)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * 🔥 [ELITE]: Establece qué bandeja(s) estamos visualizando.
     */
    fun establecerBandeja(ids: List<String>) {
        if (_idsIdentidadActiva.value == ids) return
        android.util.Log.d("ListaChatsVM", "📥 [BANDEJA_SET] Cambiando a identidades: $ids")
        _idsIdentidadActiva.value = ids
    }

    fun establecerBandeja(idIdentidad: String) = establecerBandeja(listOf(idIdentidad))

    /**
     * 🔥 [ELITE]: Obtiene el conteo de mensajes no leídos global para el profesional.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val totalNoLeidos: StateFlow<Int> = authRepository.observarUsuarioActual()
        .flatMapLatest { usuario ->
            if (usuario == null) flowOf(0)
            else chatRepository.obtenerConteoNoLeidosGlobal(usuario.uid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun refrescarBandeja() {
        val ids = _idsIdentidadActiva.value.takeIf { it.isNotEmpty() } ?: return
        viewModelScope.launch {
            // Aquí podríamos disparar un pull shallow si fuera necesario
            android.util.Log.d("ListaChatsVM", "🔄 [REFRESH] Refrescando bandeja para: $ids")
        }
    }
}















































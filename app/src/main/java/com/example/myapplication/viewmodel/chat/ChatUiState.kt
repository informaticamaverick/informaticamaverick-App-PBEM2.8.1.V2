package com.example.myapplication.viewmodel.chat

import androidx.paging.PagingData
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.uishared.ui.components.chat.ItemPaginacionChat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * [LEY #4 DE INMEDIATEZ] - UI STATE SOBERANO (v2026.FINAL)
 * 
 * PROPÓSITO: Centralizar el estado de la conversación activa.
 */
data class ChatUiState(
    val idChatActivo: String = "",
    val idIdentidadLocal: String = "",
    val idIdentidadRemota: String = "",
    val pagingMessages: Flow<PagingData<ItemPaginacionChat>> = emptyFlow(),
    val isRecording: Boolean = false,
    val isCargando: Boolean = false,
    val activeProvider: PrestadorDominio? = null,
    val isProviderOnline: Boolean = false,
    val activePromo: Promocion? = null,
    val replyingToMessage: MensajeEntity? = null,
    val bookingUiState: BookingUiState = BookingUiState()
)

data class BookingUiState(
    val availableResources: List<String> = emptyList(),
    val selectedResource: String? = null,
    val slots: List<String> = emptyList(),
    val selectedTime: String? = null
)

sealed class ChatUiEvent {
    object MessageSent : ChatUiEvent()
}


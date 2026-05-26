package com.example.myapplication.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.ChatSummary
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.ProviderRepository
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.components.BeSmallActionModel
import com.example.myapplication.presentation.util.ChatIdHelper
import com.example.myapplication.data.repository.AppActionCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Representa un hilo de conversación unificado para la UI del cliente.
 */
data class ChatThread(
    val chatId: String,
    val provider: Provider,
    val companyId: String?,
    val categoryId: String?,
    val lastMessage: String,
    val lastTimestamp: Long
)

/**
 * VIEWMODEL PARA LA LISTA DE CHATS
 * Gestiona la visibilidad de conversaciones activas, multiselección y eliminación.
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val providerRepository: ProviderRepository,
    private val coordinator: AppActionCoordinator,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val currentUserId = auth.currentUser?.uid ?: ""

    init {
        // [PASO CRÍTICO] Activar escucha global de todos los chats para notificaciones
        if (currentUserId.isNotEmpty()) {
            chatRepository.startGlobalListening(currentUserId)
        }
    }

    // --- BÚSQUEDA UNIFICADA (MAESTRO DE INTENCIONES) ---
    val searchQuery: StateFlow<String> = coordinator.globalSearchQuery

    // 1. Obtener los resúmenes de chats activos
    private val chatSummaries: Flow<List<ChatSummary>> = chatRepository.getActiveChatSummaries(currentUserId)

    // 2. Combinar con la lista global de proveedores y búsqueda global para filtrar
    val chattingThreads: StateFlow<List<ChatThread>> = combine(
        chatSummaries,
        providerRepository.allProviders,
        searchQuery
    ) { summaries, allProviders, query ->
        val norm = query.lowercase().trim()
        
        summaries.mapNotNull { summary ->
            val provider = allProviders.find { it.uid == summary.userId } ?: return@mapNotNull null
            
            // Filtro de búsqueda
            val matchesQuery = query.isEmpty() || 
                provider.displayName.lowercase().contains(norm) ||
                provider.companies.any { it.name.lowercase().contains(norm) }
            
            if (!matchesQuery) return@mapNotNull null
            
            ChatThread(
                chatId = summary.chatId,
                provider = provider,
                companyId = summary.companyId,
                categoryId = summary.categoryId,
                lastMessage = summary.lastMessage,
                lastTimestamp = summary.lastTimestamp
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mantener chattingProviders por compatibilidad o multiselección (aunque debería ser chattingThreads)
    val chattingProviders: StateFlow<List<Provider>> = chattingThreads.map { threads ->
        threads.map { it.provider }.distinctBy { it.uid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- MULTISELECCIÓN ---
    private val _isMultiSelectionActive = MutableStateFlow(false)
    val isMultiSelectionActive = _isMultiSelectionActive.asStateFlow()

    private val _selectedProviderIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedProviderIds = _selectedProviderIds.asStateFlow()

    fun updateMultiSelection(active: Boolean) {
        _isMultiSelectionActive.value = active
        if (!active) _selectedProviderIds.value = emptySet()
    }

    fun toggleSelection(providerId: String) {
        val current = _selectedProviderIds.value.toMutableSet()
        if (!current.add(providerId)) current.remove(providerId)
        _selectedProviderIds.value = current
    }

    fun selectAll(providerIds: List<String>) {
        _selectedProviderIds.value = providerIds.toSet()
    }

    // --- ACCIONES DE BE ---
    val beActions = _isMultiSelectionActive.map { active ->
        if (active) {
            listOf(
                BeSmallActionModel(
                    id = "cancel",
                    icon = Icons.Default.Close,
                    label = "Cancelar",
                    emoji = "✖️",
                    tint = Color.Gray
                ),
                // Divider Vertical Táctico
                BeSmallActionModel(
                    id = "divider_v_chat",
                    icon = Icons.Default.Close, // Dummy, BeActionsBar detecta id prefix "divider_v"
                    label = "",
                    isVisible = true
                ),
                BeSmallActionModel(
                    id = "select_all",
                    icon = Icons.Default.SelectAll,
                    label = "Todo",
                    emoji = "✅",
                    tint = Color(0xFF2197F5)
                ),
                BeSmallActionModel(
                    id = "delete_multi",
                    icon = Icons.Default.Delete,
                    label = "Eliminar",
                    emoji = "🗑️",
                    tint = Color.Red
                )
            )
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACCIONES ---
    fun getProviderById(providerId: String): Flow<Provider?> {
        return providerRepository.getProviderById(providerId)
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            val providerIds = _selectedProviderIds.value.toList()
            val chatIds = providerIds.map { providerId ->
                ChatIdHelper.generateChat(currentUserId, providerId)
            }
            chatRepository.deleteChats(chatIds)
            updateMultiSelection(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Liberar recursos de escucha global
        chatRepository.stopGlobalListening()
    }
}

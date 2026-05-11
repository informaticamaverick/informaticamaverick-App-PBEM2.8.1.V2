package com.example.myapplication.presentation.client

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.ChatSummary
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.repository.ChatRepository
import com.example.myapplication.data.repository.ProviderRepository
import com.example.myapplication.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.components.BeSmallActionModel
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
    private val userRepository: UserRepository,
    private val coordinator: AppActionCoordinator,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val syncingProviderIds = mutableSetOf<String>() // 🔥 Control de sincronización única

    // --- BÚSQUEDA UNIFICADA (MAESTRO DE INTENCIONES) ---
    val searchQuery: StateFlow<String> = coordinator.globalSearchQuery

    // 2. Combinar con la lista global de proveedores y búsqueda global para filtrar
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val chattingThreads: StateFlow<List<ChatThread>> = userRepository.userProfile
        .flatMapLatest { user ->
            val uid = user?.id ?: ""
            if (uid.isEmpty()) return@flatMapLatest flowOf(emptyList<ChatSummary>())
            
            // [PASO CRÍTICO] Activar escucha global si no está activa
            chatRepository.startGlobalListening(uid)
            
            chatRepository.getActiveChatSummaries(uid)
        }
        .combine(providerRepository.allProviders) { summaries, allProviders ->
            Pair(summaries, allProviders)
        }
        .combine(searchQuery) { (summaries, allProviders), query ->
            val norm = query.lowercase().trim()
            
            summaries.mapNotNull { summary ->
                // [REGLA DE ORO] Intentamos encontrar al proveedor en Room
                val providerFromRoom = allProviders.find { it.uid == summary.userId }
                
                // 🔥 [MEJORA] Si no existe o falta la empresa específica del chat, sincronizamos profundamente
                val needsSync = (providerFromRoom == null) || 
                               (summary.companyId != null && providerFromRoom.companies.none { it.id == summary.companyId })

                if (needsSync) {
                    // 🔥 DISPARAMOS SINCRONIZACIÓN SILENCIOSA 🔥
                    if (!syncingProviderIds.contains(summary.userId)) {
                        syncingProviderIds.add(summary.userId)
                        viewModelScope.launch {
                            try {
                                Log.d("ChatListVM", "🔄 Iniciando sincronización silenciosa para ${summary.userId}")
                                providerRepository.fetchAndSyncProviderDetail(summary.userId)
                                Log.d("ChatListVM", "✅ Sincronización silenciosa completada para ${summary.userId}")
                            } catch (e: Exception) {
                                Log.e("ChatListVM", "❌ Error en sincronización silenciosa para ${summary.userId}: ${e.message}")
                            } finally {
                                syncingProviderIds.remove(summary.userId)
                            }
                        }
                    }
                }

                // [REGLA DE ORO] Usamos el Repositorio para decorar el proveedor (Empresa vs Persona)
                val baseProvider = providerFromRoom ?: Provider(
                    uid = summary.userId,
                    email = "",
                    phoneNumber = "",
                    displayName = "Cargando...",
                    photoUrl = null,
                    name = "Cargando",
                    lastName = "",
                    createdAt = summary.lastTimestamp,
                    isOnline = false,
                    isSubscribed = false,
                    isVerified = false,
                    rating = 0f
                )
                
                val provider = providerRepository.decorateProvider(baseProvider, summary.companyId)
                
                // Filtro de búsqueda (Usando el nombre final que se mostrará)
                val matchesQuery = query.isEmpty() || 
                    provider.displayName.lowercase().contains(norm) ||
                    (providerFromRoom?.displayName?.lowercase()?.contains(norm) ?: false)
                
                if (!matchesQuery) return@mapNotNull null
                
                ChatThread(
                    chatId = summary.chatId,
                    provider = provider,
                    lastMessage = summary.lastMessage,
                    lastTimestamp = summary.lastTimestamp
                )
            }
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

    fun toggleSelection(chatId: String) {
        val current = _selectedProviderIds.value.toMutableSet()
        if (!current.add(chatId)) current.remove(chatId)
        _selectedProviderIds.value = current
    }

    fun selectAll(chatIds: List<String>) {
        _selectedProviderIds.value = chatIds.toSet()
    }

    // --- ACCIONES DE BE ---
    /**
     * Define las herramientas dinámicas para el asistente Be basadas en el estado del Obrero.
     * Sigue la Regla de Oro: El ViewModel decide qué herramientas ofrecer.
     */
    val beActions: StateFlow<List<BeSmallActionModel>> = _isMultiSelectionActive.map { active ->
        if (active) {
            listOf(
                BeSmallActionModel(
                    id = "cancel",
                    icon = Icons.Default.Close,
                    label = "Cancelar",
                    emoji = "✖️",
                    tint = Color.Gray,
                    isDefault = true
                ),
                BeSmallActionModel(
                    id = "select_all",
                    icon = Icons.Default.SelectAll,
                    label = "Todo",
                    emoji = "✅",
                    tint = Color(0xFF2197F5),
                    isDefault = true
                ),
                BeSmallActionModel(
                    id = "delete_multi",
                    icon = Icons.Default.Delete,
                    label = "Eliminar",
                    emoji = "🗑️",
                    tint = Color.Red,
                    isDefault = true
                )
            )
        } else {
            listOf(
                BeSmallActionModel(
                    id = "goto_direct_budgets",
                    icon = Icons.AutoMirrored.Filled.Message,
                    label = "Presupuestos",
                    emoji = "📩",
                    tint = Color(0xFF2197F5), // MaverickBlue
                    isDefault = true
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Maneja las acciones disparadas desde el HUD de Be.
     * Centraliza la respuesta a intenciones del usuario.
     */
    fun onBeAction(
        actionId: String, 
        onNavigateToBudgets: () -> Unit,
        onShowDeleteConfirm: () -> Unit
    ) {
        when (actionId) {
            "goto_direct_budgets" -> onNavigateToBudgets()
            "select_all" -> selectAll(chattingThreads.value.map { it.chatId })
            "delete_multi" -> if (_selectedProviderIds.value.isNotEmpty()) onShowDeleteConfirm()
            "cancel" -> updateMultiSelection(false)
        }
    }

    // --- ACCIONES ---
    fun getProviderById(providerId: String): Flow<Provider?> {
        return providerRepository.getProviderById(providerId)
    }

    fun deleteSelectedChats() {
        viewModelScope.launch {
            val chatIds = _selectedProviderIds.value.toList()
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

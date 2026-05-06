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
                // IMPORTANTE: companyId puede venir en el summary si se guardó en algún mensaje del hilo
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
                                // Mantenemos en el set un tiempo breve para evitar spam, pero permitimos re-intento rápido si Room cambia
                                syncingProviderIds.remove(summary.userId)
                            }
                        }
                    }
                }

                // Determinar el nombre y la foto a mostrar con prioridad SSOT
                // Si el chat tiene un companyId asociado, buscamos esa empresa en el objeto Provider
                val company = summary.companyId?.let { cid ->
                    providerFromRoom?.companies?.find { it.id == cid }
                }

                val displayName = company?.name ?: providerFromRoom?.displayName ?: "Cargando..."
                val photoUrl = company?.photoUrl ?: providerFromRoom?.photoUrl

                // Si no está en Room todavía o el nombre sigue siendo placeholder, 
                // creamos un objeto Provider mínimo pero con los datos que queremos mostrar (SSOT)
                val provider = providerFromRoom?.copy(
                    displayName = displayName,
                    photoUrl = photoUrl
                ) ?: Provider(
                    uid = summary.userId,
                    email = "",
                    phoneNumber = "",
                    displayName = displayName,
                    photoUrl = photoUrl,
                    name = "Cargando",
                    lastName = "",
                    createdAt = summary.lastTimestamp,
                    isOnline = false,
                    isSubscribed = false,
                    isVerified = false,
                    rating = 0f
                )
                
                // Filtro de búsqueda (Usando el nombre final que se mostrará)
                val matchesQuery = query.isEmpty() || 
                    displayName.lowercase().contains(norm) ||
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

    fun toggleSelection(chatId: String) {
        val current = _selectedProviderIds.value.toMutableSet()
        if (!current.add(chatId)) current.remove(chatId)
        _selectedProviderIds.value = current
    }

    fun selectAll(chatIds: List<String>) {
        _selectedProviderIds.value = chatIds.toSet()
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

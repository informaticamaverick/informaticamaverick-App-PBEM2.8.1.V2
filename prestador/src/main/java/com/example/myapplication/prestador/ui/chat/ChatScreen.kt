package com.example.myapplication.prestador.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.chat.ChatViewModel
import com.example.myapplication.prestador.viewmodel.chat.InboxType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

/**
 * --- CHAT SCREEN (PRESTADOR) ---
 * [ELITE v10.0]: Pantalla principal que orquesta la lista de conversaciones y el detalle.
 * Implementa un flujo MVI con estados reactivos desde el ChatViewModel.
 */

enum class ChatFilterState {
    ALL, NOTIFICATIONS_ON, VISIBLE,
    DATE_RANGE, LOCKED, UNREAD
}

enum class SortMode {
    ALPHABETICAL, RECENT
}

@Composable
fun PrestadorChatScreen(
    onBack: () -> Unit = {},
    onInConversationChange: (Boolean) -> Unit = {},
    initialChatUserId: String? = null,
    autoOpenCalendarDialog: Boolean = false,
    rescheduleDate: String = "",
    rescheduleTime: String = "",
    chatViewModel: ChatViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (clientId: String) -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val providerId = currentUser?.uid ?: ""

    // Sincronización inicial del perfil y conversaciones (Ley #5)
    LaunchedEffect(providerId) {
        if (providerId.isNotEmpty()) {
            chatViewModel.syncConversations()
            chatViewModel.loadProviderProfile(providerId)
        }
    }

    val realConversationList by chatViewModel.conversations.collectAsStateWithLifecycle()
    val selectedInbox by chatViewModel.selectedInbox.collectAsStateWithLifecycle()
    val hasCompanyInbox by chatViewModel.hasCompanyInbox.collectAsStateWithLifecycle()
    val providerPhotoUrl by chatViewModel.providerPhotoUrl.collectAsStateWithLifecycle()
    val companyPhotoUrl by chatViewModel.companyPhotoUrl.collectAsStateWithLifecycle()
    val companyName by chatViewModel.companyName.collectAsStateWithLifecycle()
    val providerProfile by chatViewModel.providerProfile.collectAsStateWithLifecycle()
    val activeCompanyId by chatViewModel.activeCompanyId.collectAsStateWithLifecycle()
    val activeBranchId by chatViewModel.activeBranchId.collectAsStateWithLifecycle()
    val liveClientPhoto by chatViewModel.liveClientPhotos.collectAsStateWithLifecycle()

    var activeChatUserId by remember { mutableStateOf<String?>(initialChatUserId) }
    var activeChatConvId by remember { mutableStateOf<String?>(null) }
    var activeChatBranchId by remember { mutableStateOf<String?>(null) }
    
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentFilter by remember { mutableStateOf(ChatFilterState.ALL) }
    var sortMode by remember { mutableStateOf(SortMode.RECENT) }
    var isDeletionMode by remember { mutableStateOf(false) }
    var selectedChatsForDeletion by remember { mutableStateOf(setOf<String>()) }
    
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    // Avisamos al Dashboard sobre el estado de la navegación
    LaunchedEffect(activeChatUserId) {
        delay(320)
        onInConversationChange(activeChatUserId != null)
    }

    LaunchedEffect(activeChatUserId) {
        activeChatUserId?.let { chatViewModel.observeClientPhoto(it) }
    }

    BackHandler {
        when {
            activeChatUserId != null -> { 
                activeChatUserId = null
                activeChatConvId = null
                activeChatBranchId = null 
            }
            isSearchActive -> { 
                isSearchActive = false
                searchQuery = "" 
            }
            isDeletionMode -> { 
                isDeletionMode = false
                selectedChatsForDeletion = emptySet() 
            }
            else -> onBack()
        }
    }

    Crossfade(targetState = activeChatUserId, animationSpec = tween(300), label = "chat_transition") { chatUserId ->
        if (chatUserId == null) {
            ChatListScreen(
                conversations = realConversationList,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                currentFilter = currentFilter,
                sortMode = sortMode,
                isDeletionMode = isDeletionMode,
                selectedChatsForDeletion = selectedChatsForDeletion,
                onSearchActiveChange = { isSearchActive = it },
                onSearchQueryChange = { searchQuery = it },
                onFilterChange = { currentFilter = it },
                onSortModeChange = { sortMode = it },
                onDeletionModeChange = { isDeletionMode = it },
                onChatSelectionChange = { selectedChatsForDeletion = it },
                onChatClick = { userId, convId -> 
                    val thread = realConversationList.find { it.conversationId == convId }
                    activeChatUserId = userId
                    activeChatConvId = convId
                    activeChatBranchId = thread?.branchId
                },
                onBack = onBack,
                onShowNotificationDialog = { showNotificationDialog = true },
                onShowVisibilityDialog = { showVisibilityDialog = true },
                onShowDateRangeDialog = { showDateRangeDialog = true },
                onShowLockDialog = { showLockDialog = true },
                onDeleteSelected = { userIds ->
                    chatViewModel.deleteConversations(userIds)
                    selectedChatsForDeletion = emptySet()
                    isDeletionMode = false
                },
                onRequestDeleteConfirmation = { showConfirmDeleteDialog = true },
                selectedInbox = selectedInbox,
                hasCompanyInbox = hasCompanyInbox,
                providerPhotoUrl = providerPhotoUrl,
                companyName = companyName ?: "",
                onInboxChange = { type, companyId, branchId -> chatViewModel.selectInbox(type, companyId, branchId) },
                companyPhotoUrl = companyPhotoUrl,
                providerCompanies = providerProfile?.companies ?: emptyList(),
                activeCompanyId = activeCompanyId,
                activeBranchId = activeBranchId,
                onRefresh = { chatViewModel.syncConversations() } // 🔥 SOLUCIONADO: Pasamos el parámetro requerido
            )
        } else {
            val userName = realConversationList.firstOrNull { it.userId == chatUserId }?.userName ?: "Usuario"
            val userPhotoUrl = liveClientPhoto[chatUserId] ?: realConversationList.firstOrNull { it.userId == chatUserId }?.userAvatarUrl
            
            ChatConversationScreen(
                userId = chatUserId,
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                providerId = providerId,
                conversationId = activeChatConvId,
                branchId = activeChatBranchId,
                onBack = { 
                    activeChatUserId = null
                    activeChatConvId = null
                    activeChatBranchId = null 
                },
                onNavigateToClientePerfil = { onNavigateToClientePerfil(chatUserId) },
                autoOpenCalendarDialog = autoOpenCalendarDialog,
                rescheduleDate = rescheduleDate,
                rescheduleTime = rescheduleTime
            )
        }
    }

    // --- DIÁLOGOS DE CONFIGURACIÓN ---
    if (showNotificationDialog) {
        NotificationSettingsDialog(
            onDismiss = { showNotificationDialog = false },
            onConfirm = { showNotificationDialog = false }
        )
    }
    if (showVisibilityDialog) {
        VisibilitySettingsDialog(
            onDismiss = { showVisibilityDialog = false },
            onConfirm = { showVisibilityDialog = false }
        )
    }
    if (showDateRangeDialog) {
        DateRangeDialog(
            onDismiss = { showDateRangeDialog = false },
            onConfirm = { showDateRangeDialog = false }
        )
    }
    if (showLockDialog) {
        LockSettingsDialog(
            onDismiss = { showLockDialog = false },
            onConfirm = { showLockDialog = false }
        )
    }

    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("Eliminar conversaciones") },
            text = { Text("¿Eliminás ${selectedChatsForDeletion.size} conversación(es)? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatViewModel.deleteConversations(selectedChatsForDeletion)
                        selectedChatsForDeletion = emptySet()
                        isDeletionMode = false
                        showConfirmDeleteDialog = false
                    }
                ) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

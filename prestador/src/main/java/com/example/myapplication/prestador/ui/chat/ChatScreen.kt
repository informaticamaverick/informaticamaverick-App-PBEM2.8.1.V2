package com.example.myapplication.prestador.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.ChatData
//import com.example.myapplication.prestador.viewmodel.chat.ChatSimulationViewModel
import com.example.myapplication.prestador.viewmodel.chat.ChatViewModel
import com.example.myapplication.prestador.viewmodel.chat.InboxType
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

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
    onNavigateToPresupuesto: () -> Unit = {},
    initialChatUserId: String? = null,
    autoOpenCalendarDialog: Boolean = false,
    rescheduleDate: String = "",
    rescheduleTime: String = "",
    chatViewModel: ChatViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (clientId: String) -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val providerId = currentUser?.uid ?: ""

    LaunchedEffect(providerId) {
        if (providerId.isNotEmpty()) {
            chatViewModel.syncConversations()
            chatViewModel.loadProviderProfile(providerId)
            chatViewModel.refreshProviderProfile(providerId)
        }
    }

    val realConversations by chatViewModel.conversations.collectAsState()
    val selectedInbox by chatViewModel.selectedInbox.collectAsState()
    val hasCompanyInbox by chatViewModel.hasCompanyInbox.collectAsState()
    val providerPhotoUrl by chatViewModel.providerPhotoUrl.collectAsState()
    val companyPhotoUrl by chatViewModel.companyPhotoUrl.collectAsState()
    val companyName by chatViewModel.companyName.collectAsState()
    val realConversationList = remember(realConversations) {
        realConversations.map { entity ->
            ChatData.Conversation(
                userId = entity.userId,
                userName = entity.userName,
                lastMessage = entity.lastMessage ?: "",
                timestamp = entity.lastMessageTimestamp,
                unreadCount = entity.unreadCount,
                notificationsEnabled = entity.notificationsEnabled,
                isVisible = entity.isVisible,
                isLocked = entity.isLocked,
                conversationId = entity.conversationId,
                userAvatarUrl = entity.userAvatarUrl
            )
        }
    }


    LaunchedEffect(realConversationList.size) {
        println("PrestadorChatScreen - ${realConversationList.size} conversaciones reales")
    }
    LaunchedEffect(initialChatUserId) {
        println("Initial chat userId: $initialChatUserId")
    }

    var activeChatUserId by remember { mutableStateOf<String?>(initialChatUserId) }
    
    // Eliminado: Dependencia de chatSimulationViewModel para navegación automática.
    // En el futuro, se debe implementar un mecanismo basado en notificaciones reales 
    // manejado por chatViewModel o un servicio de notificaciones.

    var inputText by remember { mutableStateOf("") }
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

    LaunchedEffect(activeChatUserId) {
        onInConversationChange(activeChatUserId != null)
    }

    BackHandler {
        when {
            activeChatUserId != null -> { activeChatUserId = null; inputText = "" }
            isSearchActive -> { isSearchActive = false; searchQuery = "" }
            isDeletionMode -> { isDeletionMode = false; selectedChatsForDeletion = emptySet() }
            else -> onBack()
        }
    }

    Crossfade(targetState = activeChatUserId, animationSpec = tween(300)) { chatUserId ->
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
                onChatClick = { userId -> activeChatUserId = userId },
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
                onInboxChange = { chatViewModel.selectInbox(it) },
                companyPhotoUrl = companyPhotoUrl,
            )
        } else {
            val userName = realConversations.firstOrNull { it.userId == chatUserId }?.userName ?: "Usuario"
            val userPhotoUrl = realConversations.firstOrNull { it.userId == chatUserId }?.userAvatarUrl
            ChatConversationScreen(
                userId = chatUserId,
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                providerId = providerId,
                onBack = { activeChatUserId = null; inputText = "" },
                onNavigateToPresupuesto = onNavigateToPresupuesto,
                onNavigateToClientePerfil = { onNavigateToClientePerfil(chatUserId)},
                autoOpenCalendarDialog = autoOpenCalendarDialog,
                rescheduleDate = rescheduleDate,
                rescheduleTime = rescheduleTime
            )
        }
    }

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
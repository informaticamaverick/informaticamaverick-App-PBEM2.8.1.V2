package com.example.myapplication.prestador.ui.pantallas.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.utilidades.GeneradorIdChat
import com.example.myapplication.prestador.viewmodel.chat.PrestadorListaChatsViewModel
import com.example.myapplication.prestador.viewmodel.chat.PrestadorChatViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- CHAT SCREEN (PRESTADOR v2026.50) ---
 * 
 * [PROPÓSITO]: Orquestar la comunicación entre el profesional y sus clientes, 
 * manejando la soberanía de identidades (Personal vs Empresa).
 */

enum class ChatFilterState {
    ALL, NOTIFICATIONS_ON, VISIBLE,
    DATE_RANGE, LOCKED, UNREAD, PENDING_ACTIONS
}

enum class SortMode {
    ALPHABETICAL, RECENT
}

@Composable
fun PrestadorChatScreen(
    onBack: () -> Unit = {},
    onInConversationChange: (Boolean) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    initialChatUserId: String? = null,
    chatViewModel: PrestadorChatViewModel = hiltViewModel(),
    listaChatsViewModel: PrestadorListaChatsViewModel = hiltViewModel(),
    identidadViewModel: PerfilPrestadorDeepViewModel = hiltViewModel(),
    onNavigateToClientePerfil: (clientId: String) -> Unit = {},
    onNavigateToCreateBudget: (clientId: String) -> Unit = {} // 🔥 [NEW]
) {
    val estadoUi by chatViewModel.uiState.collectAsStateWithLifecycle()
    val conversaciones by listaChatsViewModel.conversaciones.collectAsStateWithLifecycle()
    val stateDeep by identidadViewModel.state.collectAsStateWithLifecycle()
    val maestro = stateDeep.ecosistema

    var idUsuarioChatActivo by remember { mutableStateOf(initialChatUserId) }
    var idChatActivo by remember { mutableStateOf<String?>(null) }

    // --- CABLEADO DE SOBERANÍA ---
    LaunchedEffect(maestro) {
        maestro?.let {
            val idLocal = it.cuenta.idPerfilActivo ?: it.cuenta.id
            listaChatsViewModel.establecerBandeja(idLocal)
        }
    }

    LaunchedEffect(idUsuarioChatActivo) {
        onInConversationChange(idUsuarioChatActivo != null)
    }

    BackHandler {
        if (idUsuarioChatActivo != null) {
            idUsuarioChatActivo = null
            idChatActivo = null
        } else {
            onBack()
        }
    }

    Crossfade(targetState = idUsuarioChatActivo, animationSpec = tween(300), label = "chat_transition") { userId ->
        if (userId == null) {
            val maestroActual = maestro // 🔥 [FIX]: Local variable for smart cast
            ChatListScreen(
                conversations = conversaciones,
                isSearchActive = false,
                searchQuery = "",
                currentFilter = ChatFilterState.ALL,
                sortMode = SortMode.RECENT,
                isDeletionMode = false,
                selectedChatsForDeletion = emptySet(),
                onSearchActiveChange = {},
                onSearchQueryChange = {},
                onFilterChange = {},
                onSortModeChange = {},
                onDeletionModeChange = {},
                onChatSelectionChange = {},
                onChatClick = { idRemoto, chatId ->
                    idUsuarioChatActivo = idRemoto
                    idChatActivo = chatId
                    chatViewModel.cargarHilo(chatId, idRemoto)
                    chatViewModel.marcarLeido(chatId)
                },
                onBack = onBack,
                onShowNotificationDialog = {},
                onShowVisibilityDialog = {},
                onShowDateRangeDialog = {},
                onShowLockDialog = {},
                onRequestDeleteConfirmation = {},
                onDeleteSelected = {},
                selectedInbox = if (maestroActual?.cuenta?.priorizarEmpresa == true) com.example.myapplication.prestador.viewmodel.chat.InboxType.EMPRESA else com.example.myapplication.prestador.viewmodel.chat.InboxType.PERSONAL,
                hasCompanyInbox = (maestroActual?.empresas?.size ?: 0) > 0,
                providerPhotoUrl = maestroActual?.prestador?.perfil?.urlFoto?.toString(),
                companyPhotoUrl = maestroActual?.empresas?.firstOrNull()?.empresa?.urlFoto,
                companyName = maestroActual?.empresas?.firstOrNull()?.empresa?.nombre ?: "",
                onInboxChange = { type, idEmp, idSuc -> 
                    val idDestino = idSuc ?: idEmp ?: maestroActual?.cuenta?.id
                    idDestino?.let { listaChatsViewModel.establecerBandeja(it) }
                },
                providerCompanies = maestroActual?.empresas?.map { empComp ->
                    com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deEmpresaAModeloUi(empComp)
                } ?: emptyList(),
                activeCompanyId = maestroActual?.empresas?.find { e -> 
                    e.empresa.id == maestroActual.cuenta.idPerfilActivo || e.sucursales.any { s -> s.sucursal.id == maestroActual.cuenta.idPerfilActivo } 
                }?.empresa?.id,
                activeBranchId = maestroActual?.empresas?.flatMap { it.sucursales }?.find { it.sucursal.id == maestroActual.cuenta.idPerfilActivo }?.sucursal?.id,
                onRefresh = { 
                    listaChatsViewModel.refrescarBandeja()
                },
                companyBranches = maestroActual?.empresas?.find { e -> 
                    e.empresa.id == maestroActual.cuenta.idPerfilActivo || e.sucursales.any { s -> s.sucursal.id == maestroActual.cuenta.idPerfilActivo } 
                }?.sucursales?.map { sucComp ->
                    com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deSucursalAModeloUi(sucComp)
                } ?: emptyList()
            )
        } else {
            // Detalle de conversación
            val conversacion = conversaciones.find { it.idChat == idChatActivo }
            
            ChatConversationScreen(
                userId = userId,
                userName = conversacion?.nombreRemoto ?: "Cliente",
                userPhotoUrl = conversacion?.fotoRemotaUrl,
                providerId = maestro?.cuenta?.idPerfilActivo ?: maestro?.cuenta?.id ?: "",
                onBack = { idUsuarioChatActivo = null },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToCreateBudget = onNavigateToCreateBudget
            )
        }
    }
}


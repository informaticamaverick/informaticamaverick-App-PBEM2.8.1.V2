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

    // [FIX]: el buscador ya filtraba de verdad adentro de ChatListScreen, pero acá se le
    // pasaban siempre valores fijos (isSearchActive=false, onSearchQueryChange={}) — tocar la
    // lupa o escribir no hacía nada porque nada actualizaba un estado real.
    var busquedaActiva by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    // --- CABLEADO DE SOBERANÍA ---
    // [FIX]: se separa "qué pestaña/sucursal estoy mirando" (estado local de UI, lo controla
    // el selector) de "qué ids se le piden al ViewModel" — antes ambas cosas dependían del
    // mismo idPerfilActivo, así que "TODAS" (agrupar las sucursales de la empresa) terminaba
    // pidiendo el id de la EMPRESA, que ninguna conversación usa nunca, y no traía nada.
    var inboxSeleccionado by remember { mutableStateOf(com.example.myapplication.prestador.viewmodel.chat.InboxType.PERSONAL) }
    var empresaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var sucursalSeleccionadaId by remember { mutableStateOf<String?>(null) }

    // [FIX]: "maestro" es un objeto nuevo cada vez que algo del ecosistema se sincroniza
    // (no solo el chat) — al re-ejecutarse con cada emisión, esto pisaba SIEMPRE la bandeja
    // activa con idPerfilActivo, aunque el usuario ya hubiera elegido Personal/Empresa a mano
    // con el selector. Ahora solo fija la bandeja inicial una vez; el selector manual manda después.
    var bandejaInicializada by remember { mutableStateOf(false) }
    LaunchedEffect(maestro) {
        if (!bandejaInicializada) {
            maestro?.let {
                val idActivo = it.cuenta.idPerfilActivo
                val empresaConSucursalActiva = idActivo?.let { id -> it.empresas.find { e -> e.sucursales.any { s -> s.sucursal.id == id } } }
                if (it.cuenta.priorizarEmpresa && idActivo != null && empresaConSucursalActiva != null) {
                    inboxSeleccionado = com.example.myapplication.prestador.viewmodel.chat.InboxType.EMPRESA
                    empresaSeleccionadaId = empresaConSucursalActiva.empresa.id
                    sucursalSeleccionadaId = idActivo
                    listaChatsViewModel.establecerBandeja(idActivo)
                } else {
                    inboxSeleccionado = com.example.myapplication.prestador.viewmodel.chat.InboxType.PERSONAL
                    listaChatsViewModel.establecerBandeja(it.cuenta.id)
                }
                bandejaInicializada = true
            }
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
                isSearchActive = busquedaActiva,
                searchQuery = textoBusqueda,
                currentFilter = ChatFilterState.ALL,
                sortMode = SortMode.RECENT,
                isDeletionMode = false,
                selectedChatsForDeletion = emptySet(),
                onSearchActiveChange = { activa ->
                    busquedaActiva = activa
                    if (!activa) textoBusqueda = ""
                },
                onSearchQueryChange = { textoBusqueda = it },
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
                selectedInbox = inboxSeleccionado,
                hasCompanyInbox = (maestroActual?.empresas?.size ?: 0) > 0,
                providerPhotoUrl = maestroActual?.prestador?.perfil?.urlFoto?.toString(),
                companyPhotoUrl = maestroActual?.empresas?.firstOrNull()?.empresa?.urlFoto,
                companyName = maestroActual?.empresas?.firstOrNull()?.empresa?.nombre ?: "",
                onInboxChange = { type, idEmp, idSuc ->
                    inboxSeleccionado = type
                    empresaSeleccionadaId = idEmp
                    sucursalSeleccionadaId = idSuc
                    when {
                        type == com.example.myapplication.prestador.viewmodel.chat.InboxType.PERSONAL ->
                            maestroActual?.cuenta?.id?.let { listaChatsViewModel.establecerBandeja(it) }
                        idSuc != null -> listaChatsViewModel.establecerBandeja(idSuc)
                        idEmp != null -> {
                            // "TODAS": agrupar las conversaciones de TODAS las sucursales de esta empresa.
                            val idsSucursales = maestroActual?.empresas?.find { it.empresa.id == idEmp }
                                ?.sucursales?.map { it.sucursal.id } ?: emptyList()
                            if (idsSucursales.isNotEmpty()) listaChatsViewModel.establecerBandeja(idsSucursales)
                        }
                    }
                },
                providerCompanies = maestroActual?.empresas?.map { empComp ->
                    com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deEmpresaAModeloUi(empComp)
                } ?: emptyList(),
                activeCompanyId = empresaSeleccionadaId,
                activeBranchId = sucursalSeleccionadaId,
                onRefresh = {
                    listaChatsViewModel.refrescarBandeja()
                },
                companyBranches = maestroActual?.empresas?.find { it.empresa.id == empresaSeleccionadaId }
                    ?.sucursales?.map { sucComp ->
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


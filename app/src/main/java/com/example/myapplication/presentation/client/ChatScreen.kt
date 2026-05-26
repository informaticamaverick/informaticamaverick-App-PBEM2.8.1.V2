package com.example.myapplication.presentation.client

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.presentation.components.*
import com.example.myapplication.ui.theme.*
import com.example.myapplication.presentation.profile.ProfileViewModel
import com.example.myapplication.presentation.util.ChatIdHelper
import androidx.compose.ui.tooling.preview.Preview

/**
 * GRAN ORQUESTADOR DE CHAT
 * Maneja la transición entre la Lista de Chats y la Conversación Activa.
 */
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    initialProviderId: String? = null,
    initialCompanyId: String? = null,
    initialCategoryId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    beBrainViewModel: BeBrainViewModel = hiltViewModel(),
    chatListViewModel: ChatListViewModel = hiltViewModel()
) {
    // [REGLA DE ORO] Suscripción a Datos Procesados (UI)
    // Usamos variables de estado sin delegar para garantizar acceso a la referencia estable en corrutinas
    val chattingThreadsState = chatListViewModel.chattingThreads.collectAsStateWithLifecycle()
    val chattingThreads by chattingThreadsState
    
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val unreadCountsMap by beBrainViewModel.unreadCountsMap.collectAsStateWithLifecycle()

    // Estados de multiselección y acciones de Be
    val isMultiSelectMode by chatListViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    
    val selectedIdsState = chatListViewModel.selectedProviderIds.collectAsStateWithLifecycle()
    val selectedIds by selectedIdsState

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Sincronización de Multiselección con el Cerebro (BeBrain)
    LaunchedEffect(isMultiSelectMode, selectedIds) {
        beBrainViewModel.syncMultiSelection(isMultiSelectMode, selectedIds)
    }

    // [PASO CRÍTICO] Sincronización del Contexto de Be y Acciones Dinámicas
    LaunchedEffect(Unit) {
        beBrainViewModel.onRouteChanged("chat_list")
        // Registramos el botón de Presupuestos Recibidos por defecto al iniciar
        val chatActions = listOf(
            BeSmallActionModel(
                id = "goto_direct_budgets",
                icon = Icons.AutoMirrored.Filled.Message,
                label = "Presupuestos",
                emoji = "📩",
                tint = Color(0xFF2197F5), // MaverickBlue
                isDefault = true,
                onClick = { beBrainViewModel.triggerAction("goto_direct_budgets") }
            )
        )
        beBrainViewModel.setCustomActions(chatActions)
    }

    // --- SECCIÓN: ACCIONES DINÁMICAS DE BE (TOOLBOX) ---
    // Dependiendo de si estamos en multiselección o no, inyectamos diferentes herramientas
    LaunchedEffect(isMultiSelectMode) {
        if (isMultiSelectMode) {
            val multiActions = listOf(
                BeSmallActionModel(
                    id = "cancel",
                    icon = Icons.Default.Close,
                    label = "Cancelar",
                    emoji = "❌",
                    isDefault = true,
                    onClick = { beBrainViewModel.triggerAction("cancel") }
                ),
                BeSmallActionModel(
                    id = "select_all",
                    icon = Icons.Default.SelectAll,
                    label = "Todo",
                    emoji = "✅",
                    isDefault = true,
                    onClick = { beBrainViewModel.triggerAction("select_all") }
                ),
                BeSmallActionModel(
                    id = "delete_multi",
                    icon = Icons.Default.Delete,
                    label = "Eliminar",
                    emoji = "🗑️",
                    tint = Color.Red,
                    isDefault = true,
                    onClick = { beBrainViewModel.triggerAction("delete_multi") }
                )
            )
            beBrainViewModel.setCustomActions(multiActions)
        } else {
            // Restauramos las acciones por defecto del chat (Presupuestos Recibidos)
            val chatActions = listOf(
                BeSmallActionModel(
                    id = "goto_direct_budgets",
                    icon = Icons.AutoMirrored.Filled.Message,
                    label = "Presupuestos",
                    emoji = "📩",
                    tint = Color(0xFF2197F5), // MaverickBlue
                    isDefault = true,
                    onClick = { beBrainViewModel.triggerAction("goto_direct_budgets") }
                )
            )
            beBrainViewModel.setCustomActions(chatActions)
        }
    }

    // Capturar Acciones de BeBrain y delegar al ViewModel de Lista (OBRERO)
    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            when (actionId) {
                // SECCIÓN: Navegación desde el HUD de Be
                "goto_direct_budgets" -> navController?.navigate("chat_presupuestos_recibidos")

                // SECCIÓN: Acciones de multiselección de chats
                // Usamos .value para asegurar que obtenemos el dato más fresco del flujo
                "select_all" -> chatListViewModel.selectAll(chattingThreadsState.value.map { it.chatId })
                "delete_multi" -> if (selectedIdsState.value.isNotEmpty()) showDeleteConfirmDialog = true
                "cancel" -> chatListViewModel.updateMultiSelection(false)
                else -> { /* Manejar otras acciones si es necesario */ }
            }
        }
    }

    // 🔥 NUEVO: Limpieza al destruir la pantalla o al salir de multiselección
    DisposableEffect(Unit) {
        onDispose {
            // Avisamos al cerebro que esta pantalla ya no controla las acciones personalizadas
            // Esto es crucial para que la barra de Be se limpie al salir de la pantalla
            beBrainViewModel.setCustomActions(emptyList())
            beBrainViewModel.syncMultiSelection(false, emptySet()) // También reseteamos la multiselección
        }
    }

    ChatScreenContent(
        allThreads = chattingThreads,
        profileState = profileState,
        unreadCountsMap = unreadCountsMap,
        onBack = onBack,
        initialProviderId = initialProviderId,
        initialCompanyId = initialCompanyId,
        initialCategoryId = initialCategoryId,
        navController = navController,
        onInConversationChange = onInConversationChange,
        beBrainViewModel = beBrainViewModel,
        chatListViewModel = chatListViewModel,
        isMultiSelectMode = isMultiSelectMode,
        selectedIds = selectedIds,
        showDeleteConfirmDialog = showDeleteConfirmDialog,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onConfirmDelete = { 
            chatListViewModel.deleteSelectedChats()
            showDeleteConfirmDialog = false
        },
        onRouteChanged = { beBrainViewModel.onRouteChanged("chat_list") }
    )
}

@Composable
fun ChatScreenContent(
    allThreads: List<ChatThread>,
    profileState: com.example.myapplication.presentation.profile.ProfileUiState,
    unreadCountsMap: Map<String, Int>,
    onBack: () -> Unit,
    initialProviderId: String? = null,
    initialCompanyId: String? = null,
    initialCategoryId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    beBrainViewModel: BeBrainViewModel? = null,
    chatListViewModel: ChatListViewModel? = null,
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    showDeleteConfirmDialog: Boolean = false,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onRouteChanged: () -> Unit
) {
    val appColors = getAppColors()
    
    // Estado de navegación local
    var activeProviderId by remember { 
        mutableStateOf(if (initialProviderId == "{providerId}") null else initialProviderId) 
    }
    var activeCompanyId by remember {
        mutableStateOf(if (initialCompanyId == "{companyId}") null else initialCompanyId)
    }
    var activeCategoryId by remember {
        mutableStateOf(if (initialCategoryId == "{categoryId}") null else initialCategoryId)
    }

    // --- SECCIÓN: CARGA DE PROVEEDOR FALLBACK ---
    // Si el providerId no está en la lista de 'chattingThreads' (porque es un chat nuevo),
    // lo buscamos a través del ViewModel (OBRERO).
    val fallbackProvider by if (activeProviderId != null && allThreads.none { it.provider.uid == activeProviderId }) {
        chatListViewModel?.getProviderById(activeProviderId!!)?.collectAsStateWithLifecycle(initialValue = null) ?: remember { mutableStateOf(null) }
    } else {
        remember { mutableStateOf(null) }
    }

    // Efecto de visibilidad de la barra inferior
    LaunchedEffect(activeProviderId) {
        onInConversationChange(activeProviderId != null)
    }

    BackHandler {
        if (activeProviderId != null) {
            activeProviderId = null
            activeCompanyId = null
            activeCategoryId = null
        } else onBack()
    }

    if (profileState.isLoading || profileState.uid.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = appColors.accentBlue)
        }
    } else {
        val currentUserId = profileState.uid

        if (activeProviderId == null) {
            ChatListContent(
                threadsList = allThreads,
                currentUserId = currentUserId,
                unreadCountsMap = unreadCountsMap,
                onChatClick = { thread -> 
                    activeProviderId = thread.provider.uid
                    activeCompanyId = thread.companyId
                    activeCategoryId = thread.categoryId
                },
                onBack = onBack,
                appColors = appColors,
                navController = navController,
                onRouteChanged = onRouteChanged,
                beBrainViewModel = beBrainViewModel,
                chatListViewModel = chatListViewModel,
                isMultiSelectMode = isMultiSelectMode,
                selectedIds = selectedIds
            )
        } else {
            val provider = allThreads.find { it.provider.uid == activeProviderId }?.provider ?: fallbackProvider

            if (provider != null) {
                // [REGLA DE ORO] Generamos el chatId consistente
                // Para chats de empresa: solo usamos companyId como participante (sin provider.uid)
                val chatId = if (activeCompanyId != null)
                    ChatIdHelper.generateChat(currentUserId, activeCompanyId!!)
                else
                    ChatIdHelper.generateChat(currentUserId, provider.uid)
                
                // Usamos hiltViewModel con una key para que cada chat tenga su propia instancia
                val chatViewModel: ChatViewModel = hiltViewModel(key = chatId)
                
                // [PASO CRÍTICO] Inicializamos el ViewModel con el chatId generado
                LaunchedEffect(chatId) {
                    chatViewModel.initialize(
                        chatId = chatId,
                        companyId = activeCompanyId,
                        categoryId = activeCategoryId
                    )
                }
                
                if (beBrainViewModel != null) {
                    ChatConversationScreen(
                        provider = provider,
                        companyId = activeCompanyId,
                        categoryId = activeCategoryId,
                        viewModel = chatViewModel,
                        onBack = { 
                            activeProviderId = null
                            activeCompanyId = null
                            activeCategoryId = null
                        },
                        appColors = appColors,
                        beBrainViewModel = beBrainViewModel
                    )
                }
            }
        }

        // --- SECCIÓN: DIÁLOGOS GLOBALES DE CHAT ---
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = onDismissDeleteDialog,
                title = { Text("Eliminar chats") },
                text = { Text("¿Estás seguro de que deseas eliminar las ${selectedIds.size} conversaciones seleccionadas? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = onConfirmDelete) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDeleteDialog) {
                        Text("Cancelar")
                    }
                },
                containerColor = appColors.surfaceColor, // Corregido: surfaceColor en lugar de cardSurface
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

/**
 * UI PURA: Lista de Chats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListContent(
    threadsList: List<ChatThread>,
    currentUserId: String,
    unreadCountsMap: Map<String, Int>,
    onChatClick: (ChatThread) -> Unit,
    onBack: () -> Unit,
    appColors: AppColors,
    navController: NavHostController? = null,
    onRouteChanged: () -> Unit,
    beBrainViewModel: BeBrainViewModel? = null,
    chatListViewModel: ChatListViewModel? = null,
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet()
) {
    val listState = rememberLazyListState()

    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(Unit) {
        onRouteChanged()
    }

    val activeFilters by beBrainViewModel?.activeFilters?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptySet()) }
    val availableFilters by beBrainViewModel?.availableFilters?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }

    Box(modifier = Modifier.fillMaxSize().background(appColors.backgroundColor)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                BarraCabezera(
                    title = "Mensajes",
                    subtitle = "Bandeja de Entrada",
                    emoji = "💬",
                    onBack = onBack,
                    collapseFraction = collapseFraction,
                    accentColor = Color(0xFF2197F5),
                    onInfoClick = {}
                )
            }
        ) { paddingValues ->
            MoldeBarraMenu(
                modifier = Modifier.padding(paddingValues),
                itemCount = threadsList.size,
                labelCountMain = "CHATS",
                labelCountSub = "Conversaciones",
                showSuscritos = false,
                showCercania = false,
                showVista = false,
                customActions = {
                    // --- SECCIÓN: ACCIONES DE CABECERA (PANTALLA TONTA) ---
                    // Solo mantenemos el botón de filtros según requerimiento.
                    // Las acciones de multiselección ahora viven en el asistente Be.
                    MenuFiltros(
                        activeFilters = activeFilters,
                        dynamicCategories = emptyList(),
                        refinementFilters = availableFilters,
                        onAction = { beBrainViewModel?.toggleFilter(it) },
                        onApply = {},
                        onClearFilters = { beBrainViewModel?.clearFilters() }
                    )
                },
                content = {
                    if (threadsList.isEmpty()) {
                        EmptyChatPlaceholder()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(threadsList, key = { it.chatId }) { thread ->
                                val threadId = thread.chatId
                                val isSelected = selectedIds.contains(threadId)

                                UnifiedChatListItem(
                                    thread = thread,
                                    unreadCount = unreadCountsMap[threadId] ?: 0,
                                    isSelected = isSelected,
                                    isMultiSelectMode = isMultiSelectMode,
                                    onClick = { 
                                        if (isMultiSelectMode) chatListViewModel?.toggleSelection(threadId)
                                        else onChatClick(thread)
                                    },
                                    onLongClick = {
                                        if (!isMultiSelectMode) chatListViewModel?.updateMultiSelection(true)
                                        chatListViewModel?.toggleSelection(threadId)
                                    },
                                    onAvatarClick = { navController?.navigate("perfil_prestador/${thread.provider.uid}") }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Filled.Message, null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("No tienes conversaciones activas", color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val sampleProvider = com.example.myapplication.data.model.fake.PrestadorSampleDataFalso.generateMaverickProvider().toDomain()
    val sampleThread = ChatThread(
        chatId = "c1",
        provider = sampleProvider,
        companyId = null,
        categoryId = "Sistemas",
        lastMessage = "Hola!",
        lastTimestamp = System.currentTimeMillis()
    )
    val sampleThreads = listOf(sampleThread)
    val sampleProfileState = com.example.myapplication.presentation.profile.ProfileUiState(
        uid = "user_demo_66",
        displayName = "Demo User",
        isLoading = false
    )
    val sampleUnreadCounts = mapOf("c1" to 3)

    MyApplicationTheme {
        ChatScreenContent(
            allThreads = sampleThreads,
            profileState = sampleProfileState,
            unreadCountsMap = sampleUnreadCounts,
            onBack = {},
            onRouteChanged = {}
        )
    }
}

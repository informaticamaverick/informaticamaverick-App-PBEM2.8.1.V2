package com.example.myapplication.ui.pantallas.chat

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.uishared.estilos.AppIcons
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorListaPantallaCompleta
import com.example.myapplication.ui.componentes.sistema.menu.v3.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.ui.estilos.getThemeColors
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.example.myapplication.core.utilidades.ChatIdHelper
import com.example.myapplication.ui.componentes.be.vm.*
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.viewmodel.budget.UsuarioPresupuestoViewModel
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Viewer
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.ui.componentes.sistema.lista.BurbujaCabeceraLista
import com.example.myapplication.uishared.ui.components.chat.ElementoListaChat
import com.example.myapplication.ui.componentes.sistema.ShimmerCabeceraChat
import com.example.myapplication.ui.componentes.sistema.ListaShimmerChat
import com.example.myapplication.ui.componentes.sistema.efectoShimmer
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.cabecera.ColumnaTituloSeccionV3
import com.example.myapplication.ui.componentes.sistema.cabecera.EmojiImpactoV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPantallas
import com.example.myapplication.ui.pantallas.home.Screen
import com.example.myapplication.core.dominio.modelos.ConversacionHiloMDominio
import com.example.myapplication.viewmodel.chat.ChatViewModel
import com.example.myapplication.viewmodel.chat.UsuarioListaChatsViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import com.example.myapplication.viewmodel.chat.ArchiveroPresupuestoViewModel
import com.example.myapplication.viewmodel.budget.PresupuestoAnalyticsViewModel

/**
 * [LEY #1]: PANTALLA TONTA (Stateless UI)
 * 
 * PROPÓSITO: Bandeja de entrada de mensajería unificada (Multiperfil).
 */
@Composable
fun ChatPantalla(
    onBack: () -> Unit,
    idRemoto: String? = null,
    idLocal: String? = null,
    initialPromoId: String? = null,
    navController: NavHostController? = null,
    onInConversationChange: (Boolean) -> Unit = {},
    userViewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    beBrainViewModel: BeCerebroViewModel = hiltViewModel(),
    listaChatsViewModel: UsuarioListaChatsViewModel = hiltViewModel(),
    budgetViewModel: UsuarioPresupuestoViewModel = hiltViewModel(),
    archiveroViewModel: ArchiveroPresupuestoViewModel = hiltViewModel(),
    analyticsViewModel: PresupuestoAnalyticsViewModel = hiltViewModel()
) {
    val hilosPorSeccion by listaChatsViewModel.chattingThreads.collectAsStateWithLifecycle()
    val idPerfilSeleccionado by listaChatsViewModel.selectedPerfilId.collectAsStateWithLifecycle()
    val conteoNoLeidos by listaChatsViewModel.unreadCountsMap.collectAsStateWithLifecycle()
    val identidadesSoberanas by userViewModel.identidadesSoberanas.collectAsStateWithLifecycle()
    val accountState by userViewModel.ecosistemaMaestro.collectAsStateWithLifecycle()

    val isMultiSelectMode by listaChatsViewModel.isMultiSelectionActive.collectAsStateWithLifecycle()
    val selectedIds by listaChatsViewModel.selectedChatIds.collectAsStateWithLifecycle()
    
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var chatToDeleteId by remember { mutableStateOf<String?>(null) }
    var showPresupuestosSheet by remember { mutableStateOf(false) }
    var budgetForA4Preview by remember { mutableStateOf<PresupuestoConItems?>(null) }
    var showBudgetsForThisProviderOnly by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    var activeChatId by remember { mutableStateOf<String?>(null) }

    var activeRemotoId by remember(idRemoto) {
        mutableStateOf(if (idRemoto.isNullOrBlank()) null else idRemoto)
    }

    var activeLocalId by remember(idLocal) {
        mutableStateOf(if (idLocal.isNullOrBlank()) null else idLocal)
    }

    val budgetsForChatAdmin by budgetViewModel.presupuestosDirectos.collectAsStateWithLifecycle()
    val budgetDetail by budgetViewModel.presupuestoSeleccionado.collectAsStateWithLifecycle()

    LaunchedEffect(budgetDetail) {
        if (budgetDetail != null) budgetForA4Preview = budgetDetail
    }

    LaunchedEffect(activeRemotoId) {
        onInConversationChange(activeRemotoId != null)
    }

    val isSearchActive by beBrainViewModel.beBusquedaMotor.estaBusquedaActiva.collectAsStateWithLifecycle()
    val favoriteIds by listaChatsViewModel.favoriteIds.collectAsStateWithLifecycle()

    // 🔥 [LEY #12]: Sincronización con el HUD de Be y Gestión de Islas
    LaunchedEffect(isMultiSelectMode, selectedIds, favoriteIds, activeRemotoId) {
        if (activeRemotoId == null) {
            // Solo actualizamos el contrato si estamos en la LISTA de chats
            val sonTodosFavoritos = selectedIds.isNotEmpty() && 
                                    selectedIds.all { idChat ->
                                        // En chat, el idChat suele contener el id del prestador o derivarse de él.
                                        // Buscamos el hilo para obtener el idUsuarioRemoto real.
                                        val idRemoto = hilosPorSeccion.values.flatten().find { it.idChat == idChat }?.idUsuarioRemoto
                                        favoriteIds.contains(idRemoto)
                                    }

            val accionFavorito = if (sonTodosFavoritos) "remove_fav_multi" else "add_fav_multi"

            val beConfig = ContextoHUD.CHAT.crearConfiguracionBase(
                navegacion = if (isMultiSelectMode) listOf(accionFavorito) else emptyList(),
                edicion = if (isMultiSelectMode) listOf("select_all", "cancel", "delete_multi") else emptyList(),
                mensajes = emptyList(),
                pistaBusqueda = "¿BUSCÁS UN CHAT O CONTACTO? 💬🤝"
            ).copy(
                ocultarOjos = isMultiSelectMode,
                mostrarHerramientas = true
            )
            
            beBrainViewModel.navCoordinador.actualizarContratoActual(beConfig)
            beBrainViewModel.coordinador.actualizarMultiseleccion(isMultiSelectMode)
        }
    }

    val isRefreshing by listaChatsViewModel.isRefreshing.collectAsStateWithLifecycle()

    // 🔥 [FIX]: Captura de estado actualizado para el colector de eventos soberanos
    val remotoIdActual by rememberUpdatedState(activeRemotoId)
    val localIdActual by rememberUpdatedState(activeLocalId)

    LaunchedEffect(beBrainViewModel) {
        beBrainViewModel.actionEvent.collect { actionId ->
            // 🔥 [LEY #12]: Soberanía por Contrato.
            val idSoberanaActual = beBrainViewModel.navCoordinador.contratoActivo.value.id
            if (idSoberanaActual != "root_chat" && idSoberanaActual != "visor_a4_elite") return@collect

            android.util.Log.v("ChatPantalla", "🎯 [ACTION_RECEIVED] ID: $actionId | currentRemote: $remotoIdActual")
            when (actionId) {
                "archivo_chat" -> {
                    val rId = remotoIdActual ?: "global" 
                    val lId = localIdActual ?: "personal"
                    android.util.Log.d("ChatPantalla", "📁 [ARCHIVE_ACTION] Navigating to Multimedia. Remote: $rId | Local: $lId")
                    if (rId == "global") {
                        activeRemotoId = null // 🔥 [v2026.ELITE]: Limpiar chat activo para regresar a la lista
                    }
                    navController?.navigate(Screen.ArchiveroChatMultimedia.createRoute(rId, lId))
                }
                "close_all_sheets" -> {
                    showPresupuestosSheet = false
                }
                "select_all" -> {
                    val actual = selectedIds.size
                    val total = hilosPorSeccion.values.flatten().size
                    if (actual >= total && total > 0) {
                        listaChatsViewModel.deseleccionarTodo()
                    } else {
                        listaChatsViewModel.seleccionarTodo()
                    }
                }
                "compare_budgets" -> {
                    // Se maneja desde el archivero multimedia ahora
                }
                "delete_multi" -> {
                    showDeleteConfirmDialog = true
                }
                "add_fav_multi" -> {
                    listaChatsViewModel.agregarSeleccionadasAFavoritos()
                }
                "remove_fav_multi" -> {
                    listaChatsViewModel.quitarSeleccionadasDeFavoritos()
                }
            }
        }
    }

    ChatContenidoPrincipal(
        hilosPorSeccion = hilosPorSeccion, accountState = accountState, unreadCountsMap = conteoNoLeidos, identityUnreadCounts = emptyMap(),
        perfiles = identidadesSoberanas,
        onBack = onBack, idRemoto = activeRemotoId,
        idLocal = activeLocalId, initialPromoId = initialPromoId, navController = navController,
        onActiveChatChange = { hilo -> activeChatId = hilo?.idChat; activeRemotoId = hilo?.idUsuarioRemoto; activeLocalId = hilo?.idIdentidadLocal },
        onInConversationChange = { inConv -> onInConversationChange(inConv); if (!inConv) activeRemotoId = null },
        beBrainViewModel = beBrainViewModel, listaChatsViewModel = listaChatsViewModel, selectedPerfilId = idPerfilSeleccionado,
        isMultiSelectMode = isMultiSelectMode, selectedIds = selectedIds, showDeleteConfirmDialog = showDeleteConfirmDialog,
        onDismissDeleteDialog = { showDeleteConfirmDialog = false },
        onConfirmDelete = { 
            if (isMultiSelectMode) {
                listaChatsViewModel.deleteSelectedChats()
            } else {
                chatToDeleteId?.let { listaChatsViewModel.deleteChatById(it) }
            }
            showDeleteConfirmDialog = false
            chatToDeleteId = null 
        },
        onShowDeleteConfirm = { id -> chatToDeleteId = id; showDeleteConfirmDialog = true },
        showPresupuestosSheet = showPresupuestosSheet,
        onClosePresupuestosSheet = {
            showPresupuestosSheet = false
            showBudgetsForThisProviderOnly = false
            beBrainViewModel.navCoordinador.actualizarVisibilidadHoja(false) // 🔥 [FIX]
            beBrainViewModel.coordinador.ejecutarCierreMaestro()
        },
        onConfirmBudgetAccept = { onBack() },
        onOpenPresupuestosSheet = {
            showBudgetsForThisProviderOnly = it
            showPresupuestosSheet = true
            beBrainViewModel.navCoordinador.actualizarVisibilidadHoja(true) // 🔥 [FIX]
        },
        budgetsForChatAdmin = if (showBudgetsForThisProviderOnly && activeRemotoId != null) budgetsForChatAdmin.filter { it.idPrestador == activeRemotoId } else budgetsForChatAdmin,
        budgetForA4Preview = budgetForA4Preview, onOpenBudgetPreview = { budgetId -> budgetViewModel.cargarDetallePresupuesto(budgetId) }, onCloseBudgetPreview = { budgetForA4Preview = null },
        onAcceptBudget = { budgetViewModel.aceptarPresupuesto(it) }, onRejectBudget = { budgetViewModel.rechazarPresupuesto(it) },
        isRefreshing = isRefreshing,
        onRefresh = { listaChatsViewModel.refreshAll() },
        budgetViewModel = budgetViewModel,
        showAnalytics = showAnalytics,
        analyticsViewModel = analyticsViewModel,
        onCloseAnalytics = { showAnalytics = false },
        activeRemotoId = activeRemotoId
    )
}

@Composable
fun ChatContenidoPrincipal(
    hilosPorSeccion: Map<String, List<ConversacionHiloMDominio>>,
    accountState: CuentaMaestroUsuario?,
    unreadCountsMap: Map<String, Int>,
    identityUnreadCounts: Map<String, Int> = emptyMap(),
    perfiles: List<PerfilIdentidadV3> = emptyList(),
    onBack: () -> Unit,
    idRemoto: String? = null,
    idLocal: String? = null,
    initialPromoId: String? = null,
    navController: NavHostController? = null,
    onActiveChatChange: (ConversacionHiloMDominio?) -> Unit = {},
    onInConversationChange: (Boolean) -> Unit = {},
    beBrainViewModel: BeCerebroViewModel? = null, // 🔥 [FIXED]
    listaChatsViewModel: UsuarioListaChatsViewModel? = null,
    selectedPerfilId: String = "personal",
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    showDeleteConfirmDialog: Boolean = false,
    onDismissDeleteDialog: () -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onShowDeleteConfirm: (String?) -> Unit = {},
    showPresupuestosSheet: Boolean = false,
    onClosePresupuestosSheet: () -> Unit = {},
    onConfirmBudgetAccept: () -> Unit = {},
    onOpenPresupuestosSheet: (Boolean) -> Unit = {},
    budgetsForChatAdmin: List<PresupuestoResumenDominio> = emptyList(),
    budgetForA4Preview: PresupuestoConItems? = null,
    onOpenBudgetPreview: (String) -> Unit = {},
    onCloseBudgetPreview: () -> Unit = {},
    onAcceptBudget: (PresupuestoFinalEntity) -> Unit = {},
    onRejectBudget: (PresupuestoFinalEntity) -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    budgetViewModel: UsuarioPresupuestoViewModel? = null,
    showAnalytics: Boolean = false,
    analyticsViewModel: PresupuestoAnalyticsViewModel? = null,
    onCloseAnalytics: () -> Unit = {},
    activeRemotoId: String? = null // 🔥 [NEW]
) {
    val appColors = getThemeColors()
    var activeChatId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(activeRemotoId) { onInConversationChange(activeRemotoId != null) }
    
    // 🔥 [FIX]: Usamos activeRemotoId (estado local) en lugar de idRemoto (argumento estático)
    BackHandler { 
        if (activeRemotoId != null) {
            android.util.Log.d("MAV_NAV", "🔙 [CHAT_CONTENIDO] Cerrando conversación local")
            onActiveChatChange(null) 
        } else {
            android.util.Log.d("MAV_NAV", "🔙 [CHAT_CONTENIDO] Ejecutando popBackStack")
            onBack() 
        }
    }

    if (idRemoto == null) {
        ChatListaUI(
            hilosPorSeccion = hilosPorSeccion, accountState = accountState, unreadCountsMap = unreadCountsMap,
            identityUnreadCounts = identityUnreadCounts, perfiles = perfiles, onChatClick = { onActiveChatChange(it) },
            onBack = onBack, selectedPerfilId = selectedPerfilId, navController = navController,
            beBrainViewModel = beBrainViewModel, listaChatsViewModel = listaChatsViewModel,
            isMultiSelectMode = isMultiSelectMode, selectedIds = selectedIds,
            isRefreshing = isRefreshing, onRefresh = onRefresh,
            showPresupuestosSheet = showPresupuestosSheet,
            showAnalytics = showAnalytics,
            activeRemotoId = activeRemotoId
        )
    } else {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val resolvedLocalId = if (idLocal == null || idLocal == "personal") currentUid else idLocal

        val chatId = activeChatId ?: ChatIdHelper.generateChatId(idRemoto, resolvedLocalId)
        val viewModel: ChatViewModel = hiltViewModel(key = chatId)

        LaunchedEffect(chatId, idRemoto, resolvedLocalId) {
            viewModel.inicializar(
                chatId = chatId,
                idLocal = idLocal,
                idRemota = idRemoto,
                initialPromoId = initialPromoId
            )
        }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        if (uiState.activeProvider != null) {
            ChatConversacionPantalla(
                identidadRemota = uiState.activeProvider!!,
                viewModel = viewModel,
                beBrainViewModel = beBrainViewModel ?: hiltViewModel(),
                onBack = { onActiveChatChange(null) },
                navController = navController,
                onBudgetClick = onOpenBudgetPreview,
                onShowSearch = { }
            )
        } else {
            ChatConversationSkeleton(onBack = { onActiveChatChange(null) })
        }
    }

    if (budgetForA4Preview != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val budget = budgetForA4Preview
        // 🔥 [ELITE]: Reclamo temporal de soberanía para el visor A4
        val beConfigVisor = remember { 
            ConfiguracionContextoBe(
                id = "visor_a4_elite",
                mostrarHerramientas = false 
            )
        }

        DisposableEffect(Unit) {
            beBrainViewModel?.navCoordinador?.registrarPantalla(beConfigVisor)
            onDispose {
                beBrainViewModel?.navCoordinador?.removerPantalla(beConfigVisor.id)
            }
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = onCloseBudgetPreview,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            PlanillaPresupuestoA4Viewer(
                prestador = null,
                relacion = budget,
                alCerrar = onCloseBudgetPreview,
                nombreCliente = accountState?.usuario?.perfil?.nombreVisible ?: "Yo",
                empresaCliente = accountState?.empresas?.firstOrNull()?.empresa?.nombre,
                direccionCliente = accountState?.usuario?.direcciones?.firstOrNull()?.aTextoCompleto()
            ) { _, _ ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (budget.cabecera.estado == com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto.PENDIENTE) {
                        OutlinedButton(
                            onClick = { onRejectBudget(budget.cabecera); onCloseBudgetPreview() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("RECHAZAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onAcceptBudget(budget.cabecera); onCloseBudgetPreview(); onConfirmBudgetAccept() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ACEPTAR PRESUPUESTO", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            StatusPillPremium(budget.cabecera.estado.name)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        val text = if (isMultiSelectMode) "¿Estás seguro de que deseas eliminar las ${selectedIds.size} conversaciones seleccionadas?" else "¿Estás seguro de que deseas eliminar esta conversación?"
        AlertDialog(onDismissRequest = onDismissDeleteDialog, title = { Text("Eliminar chat") }, text = { Text(text) }, confirmButton = { TextButton(onClick = { onConfirmDelete(); onActiveChatChange(null) }) { Text("Eliminar", color = Color.Red) } }, dismissButton = { TextButton(onClick = onDismissDeleteDialog) { Text("Cancelar") } }, containerColor = appColors.surfaceColor, titleContentColor = Color.White, textContentColor = Color.LightGray)
    }

    if (showAnalytics && analyticsViewModel != null) {
        val presupuestosAnaliticos by analyticsViewModel.presupuestosAnaliticos.collectAsStateWithLifecycle()
        val concursoVirtualAnalytics by analyticsViewModel.concursoVirtual.collectAsStateWithLifecycle()

        concursoVirtualAnalytics?.let { t ->
            androidx.compose.ui.window.Dialog(
                onDismissRequest = onCloseAnalytics,
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    com.example.myapplication.ui.pantallas.budget.BudgetComparisonAnalytics(
                        tender = t,
                        budgets = presupuestosAnaliticos,
                        onBack = onCloseAnalytics,
                        onViewBudgetDetail = { idPresupuesto ->
                            onOpenBudgetPreview(idPresupuesto)
                            // 🔥 [FIX]: No cerramos las analíticas para permitir regresar a ellas 
                            // tras cerrar la vista previa del presupuesto.
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun ChatListaUI(
    hilosPorSeccion: Map<String, List<ConversacionHiloMDominio>>,
    accountState: CuentaMaestroUsuario?,
    unreadCountsMap: Map<String, Int>,
    identityUnreadCounts: Map<String, Int> = emptyMap(),
    perfiles: List<PerfilIdentidadV3> = emptyList(),
    onChatClick: (ConversacionHiloMDominio) -> Unit,
    onBack: () -> Unit,
    selectedPerfilId: String = "personal",
    navController: NavHostController? = null,
    beBrainViewModel: BeCerebroViewModel? = null, // 🔥 [FIXED NAME]
    listaChatsViewModel: UsuarioListaChatsViewModel? = null,
    isMultiSelectMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    showPresupuestosSheet: Boolean = false,
    showAnalytics: Boolean = false,
    activeRemotoId: String? = null
) {
    val filtrosActivos by (listaChatsViewModel?.filtrosActivos ?: flowOf(emptySet<String>())).collectAsStateWithLifecycle(emptySet())
    val categoryItems by listaChatsViewModel?.categoryDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val filterItems by listaChatsViewModel?.filterDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val sortItems by listaChatsViewModel?.sortDropdownItems?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }

    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    val beConfig = remember {
        ContextoHUD.CHAT.crearConfiguracionBase(
            edicion = listOf("select_all", "delete_multi"),
            mensajes = emptyList(),
            pistaBusqueda = "¿BUSCÁS UN CHAT O CONTACTO? 💬🤝"
        )
    }

    // 🔥 [LEY #12]: Soberanía de contexto HUD
    DisposableEffect(activeRemotoId) {
        if (activeRemotoId == null) {
            beBrainViewModel?.navCoordinador?.reiniciarContextoHUD(ContextoHUD.CHAT)
            beBrainViewModel?.navCoordinador?.registrarPantalla(beConfig)
        } else {
            // Cuando hay idRemoto, ChatConversacionPantalla tomará el control
        }
        onDispose {
            if (activeRemotoId == null) {
                beBrainViewModel?.navCoordinador?.removerPantalla(beConfig.id)
                listaChatsViewModel?.deseleccionarTodo() // 🔥 [SANEAMIENTO]
            }
        }
    }

    // Eliminamos el LaunchedEffect de restauración manual (Ley #12 v2026.ELITE)

    val identities = remember(perfiles, identityUnreadCounts, accountState) {
        perfiles.map { iden ->
            val unreadId = if (iden.id == "personal") accountState?.cuenta?.id else iden.id
            iden.copy(conteoNoLeidos = identityUnreadCounts[unreadId] ?: 0)
        }
    }

    var scrollAccumulator by remember { mutableFloatStateOf(0f) }
    var menuFiltrosAbierto by remember { mutableStateOf<String?>(null) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val nScroll = (scrollAccumulator - delta).coerceIn(0f, 330f)
                val consumed = scrollAccumulator - nScroll
                scrollAccumulator = nScroll
                return if (scrollAccumulator >= 330f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val cardsHideFraction = remember { derivedStateOf { (scrollAccumulator / 80f).coerceIn(0f, 1f) } }
    val collapseFraction = remember { derivedStateOf { ((scrollAccumulator - 80f) / 250f).coerceIn(0f, 1f) } }
    val hasAnyFilter = filtrosActivos.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize().background(SharedPalette.V2TechSurface).nestedScroll(nestedScrollConnection)) {
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, state = pullToRefreshState, modifier = Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    val visuals = BeDictionary.Contexts["chat"]!!
                    MoldeCabeceraSuperiorPantallas(
                        fraccionColapso = collapseFraction.value,
                        slotIzquierdo = { BotonBackCabeceraV3(onClick = onBack) },
                        slotCentral = {
                            ColumnaTituloSeccionV3(
                                titulo = visuals.title,
                                subtitulo = "Sistema de Mensajería",
                                fraccionColapso = collapseFraction.value
                            )
                        },
                        slotDerecho = {
                            EmojiImpactoV3(
                                emoji = visuals.emoji,
                                fraccionColapso = collapseFraction.value
                            )
                        }
                    )
                }
            ) { paddingValues ->
                Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {

                    Box(modifier = Modifier.fillMaxWidth().animateContentSize().graphicsLayer {
                        if (!hasAnyFilter) { alpha = 1f - cardsHideFraction.value; translationY = -10.dp.toPx() * cardsHideFraction.value }
                    }.then(if (!hasAnyFilter) { Modifier.layout { measurable, constraints -> val placeable = measurable.measure(constraints); val h = (placeable.height * (1f - cardsHideFraction.value)).toInt(); layout(placeable.width, h) { placeable.placeRelative(0, 0) } } } else Modifier)) {
                        BarraFiltrosV3(
                            filtrosActivos = filtrosActivos.mapNotNull { id ->
                                val filter = BeDictionary.Filters[id] ?: BeDictionary.Sorts[id]
                                if (filter != null) ModeloBurbujaFiltro(id, filter.label, filter.emoji ?: "🔹")
                                else if (id.startsWith("cat_")) {
                                    val item = categoryItems.find { it.id == id }
                                    ModeloBurbujaFiltro(id, item?.label ?: id.removePrefix("cat_"), item?.emoji ?: "📋")
                                }
                                else null
                            },
                            alHacerClickMenu = { type -> menuFiltrosAbierto = if (menuFiltrosAbierto == type) null else type },
                            alEliminarFiltro = { listaChatsViewModel?.toggleFilter(it) },
                            alLimpiarTodo = { listaChatsViewModel?.toggleFilter("CLEAR_ALL") },
                            mostrarMenuFiltros = menuFiltrosAbierto == "filtros",
                            mostrarMenuOrdenar = menuFiltrosAbierto == "ordenar",
                            mostrarMenuCategorias = menuFiltrosAbierto == "categorias",
                            idsFiltrosSeleccionados = filtrosActivos,
                            alAlternarFiltro = { listaChatsViewModel?.toggleFilter(it) },
                            alCerrarMenu = { menuFiltrosAbierto = null },
                            itemsCategoria = categoryItems,
                            itemsFiltro = filterItems,
                            itemsOrden = sortItems,
                            estaCentrado = true,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val totalItemCount by (listaChatsViewModel?.filteredChatsCount ?: MutableStateFlow(0)).collectAsStateWithLifecycle()

                    ArmadorListaPantallaCompleta(
                        modifier = Modifier.fillMaxSize(),
                        titulo = "BANDEJA ENTRADA",
                        subtitulo = "Mi Perfil",
                        cantidadItems = totalItemCount,
                        perfiles = identities,
                        idPerfilInicial = selectedPerfilId,
                        alSeleccionarPerfil = { p: PerfilIdentidadV3 ->
                            listaChatsViewModel?.selectPerfil(p.id)
                            listaChatsViewModel?.selectBranch(null)
                        },
                        colorAcento = SharedPalette.ElectricCyan,
                        menuPerfil = { iden ->
                            MenuPerfilContenido(
                                identidades = identities,
                                idPerfilActivo = iden,
                                alSeleccionar = { p ->
                                    listaChatsViewModel?.selectPerfil(p.id)
                                    listaChatsViewModel?.selectBranch(null)
                                }
                            )
                        }
                    ) { _ ->
                        if (totalItemCount == 0) {
                            if (isRefreshing) {
                                repeat(8) { item { com.example.myapplication.ui.componentes.sistema.ShimmerElementoChat() } }
                            } else {
                                item { EmptyStateChats() }
                            }
                        } else {
                            hilosPorSeccion.forEach { (header, hilos) ->
                                if (header.isNotEmpty()) {
                                    item { BurbujaCabeceraLista(text = header, icon = AppIcons.Calendar, modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) }
                                }
                                items(hilos, key = { it.idChat }) { hilo ->
                                    val isSelected = selectedIds.contains(hilo.idChat)
                                    Box(modifier = Modifier.padding(horizontal = 8.dp).animateItem()) {
                                        ElementoListaChat(
                                            idChat = hilo.idChat,
                                            nombreVisible = hilo.nombreVisible,
                                            ultimoMensaje = hilo.ultimoMensaje,
                                            marcaTiempo = hilo.marcaTiempoUltimo,
                                            urlFoto = hilo.urlFoto,
                                            urlMiniatura = hilo.urlMiniatura,
                                            estaOnline = hilo.estaOnline,
                                            estaVerificado = hilo.estaVerificado,
                                            conteoNoLeidos = unreadCountsMap[hilo.idChat] ?: 0,
                                            accionesPendientes = 0, // [DEPRECATED v2026]
                                            estaSeleccionado = isSelected,
                                            modoMultiseleccion = isMultiSelectMode,
                                            alHacerClick = { if (isMultiSelectMode) listaChatsViewModel?.toggleSelection(hilo.idChat) else onChatClick(hilo) },
                                            alHacerLongClick = { if (!isMultiSelectMode) listaChatsViewModel?.updateMultiSelection(true); listaChatsViewModel?.toggleSelection(hilo.idChat) },
                                            alHacerClickAvatar = {
                                                val route = Screen.PerfilPrestador.createRoute(
                                                    providerId = hilo.idUsuarioRemoto,
                                                    companyId = null,
                                                    branchId = hilo.idSucursalRemota
                                                )
                                                navController?.navigate(route)
                                            },
                                            colorAcento = SharedPalette.ElectricCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatConversationSkeleton(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            Surface(color = Color(0xFF0F172A), shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonBackCabeceraV3(onClick = onBack)
                    ShimmerCabeceraChat()
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .efectoShimmer()
            )
        },
        containerColor = Color(0xFF050508)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            ListaShimmerChat()
        }
    }
}

@Composable
fun EmptyStateChats() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
            TextCompacto(
                text = "COMIENZA UNA CONVERSACIÓN",
                color = SharedPalette.NeonCyan.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 2.sp),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Box(modifier = Modifier.graphicsLayer { alpha = 0.35f }) {
                ElementoListaChat(
                    idChat = "mock_chat",
                    nombreVisible = "Ejemplo: Juan Técnico",
                    ultimoMensaje = "Hola, he visto tu concurso y puedo ayudarte...",
                    marcaTiempo = System.currentTimeMillis(),
                    urlFoto = null,
                    urlMiniatura = null,
                    estaOnline = true,
                    estaVerificado = true,
                    conteoNoLeidos = 1,
                    accionesPendientes = 0,
                    estaSeleccionado = false,
                    modoMultiseleccion = false,
                    alHacerClick = { },
                    alHacerLongClick = { },
                    alHacerClickAvatar = { },
                    colorAcento = SharedPalette.ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            TextCompacto(
                text = "BANDEJA DE ENTRADA VACÍA",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Aquí aparecerán tus conversaciones con los profesionales. Podrás coordinar visitas, recibir presupuestos y realizar el seguimiento de tus proyectos en tiempo real.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcons.Search, null, tint = SharedPalette.AcidGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                TextCompacto(
                    text = "Busca profesionales en el mapa para iniciar un chat",
                    color = SharedPalette.AcidGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

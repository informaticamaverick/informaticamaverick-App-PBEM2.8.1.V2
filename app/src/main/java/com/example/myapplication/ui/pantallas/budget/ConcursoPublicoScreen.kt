package com.example.myapplication.ui.pantallas.budget

import androidx.activity.ComponentActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.viewmodel.home.CategoryViewModel
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.*
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorListaPantallaCompleta
import com.example.myapplication.ui.componentes.sistema.cabecera.ColumnaTituloSeccionV3
import com.example.myapplication.ui.componentes.sistema.cabecera.EmojiImpactoV3
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPantallas
import com.example.myapplication.ui.componentes.sistema.EsqueletoConcurso
import com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Viewer
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.sistema.contexto.ArmadorFiltrosV3
import com.example.myapplication.viewmodel.budget.ConcursoPublicoViewModel
import com.example.myapplication.viewmodel.budget.UsuarioPresupuestoViewModel
import com.example.myapplication.viewmodel.chat.ArchiveroPresupuestoViewModel
import com.example.myapplication.viewmodel.budget.PresupuestoAnalyticsViewModel
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.viewmodel.budget.ConcursoPublicoUiState
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * --- PANTALLA DE CONCURSOS PÚBLICOS (v2026.ELITE) ---
 * [LEY #13]: Soberanía del Coordinador.
 * [LEY #9]: Estándar Mav en Español.
 * [LEY #1]: Pantalla Tonta. Consume un único UiState.
 */
@Composable
fun ConcursoPublicoScreen(
    modeloVistaConcursos: ConcursoPublicoViewModel = hiltViewModel(),
    modeloVistaPresupuestos: UsuarioPresupuestoViewModel = hiltViewModel(),
    modeloVistaUsuario: ArmadorUsuarioViewModel = hiltViewModel(),
    beBrainViewModel: BeCerebroViewModel = hiltViewModel(),
    analyticsViewModel: PresupuestoAnalyticsViewModel = hiltViewModel(),
    alHacerClickChat: (String, String?) -> Unit = { _, _ -> },
    alNavegarANuevoConcurso: () -> Unit = {},
    alNavegarAPresupuestosConcurso: (String) -> Unit = {}, // 🔥 [NEW]
    alRegresar: () -> Unit,
    rellenoInferior: PaddingValues = PaddingValues(0.dp)
) {
    val state by modeloVistaConcursos.uiState.collectAsStateWithLifecycle()
    
    var presupuestoParaVistaPrevia by remember { mutableStateOf<PresupuestoConItems?>(null) }
    var concursoParaDetalles by remember { mutableStateOf<ConcursoPublicoEntity?>(null) }

    var mostrarAnaliticas by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showForceCloseConfirmDialog by remember { mutableStateOf(false) }

    val budgetDetail by modeloVistaPresupuestos.presupuestoSeleccionado.collectAsStateWithLifecycle()
    LaunchedEffect(budgetDetail) {
        if (budgetDetail != null) {
            presupuestoParaVistaPrevia = budgetDetail
        }
    }

    LaunchedEffect(Unit) {
        modeloVistaConcursos.eventosNavegacion.collect { evento ->
            when {
                evento == "NAV_NUEVO_CONCURSO" -> alNavegarANuevoConcurso()
                evento.startsWith("NAV_DETALLES_") -> {
                    val id = evento.removePrefix("NAV_DETALLES_")
                    state.concursos.find { it.idConcurso == id }?.let { resumen ->
                        concursoParaDetalles = resumen.concursoRaw
                    }
                }
                evento.startsWith("NAV_FORCE_CLOSE_") -> {
                    showForceCloseConfirmDialog = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        beBrainViewModel.actionEvent.collect { actionId ->
            // 🔥 [LEY #12]: Soberanía por Contrato.
            if (beBrainViewModel.navCoordinador.contratoActivo.value.id != "root_concursos") return@collect

            when (actionId) {
                "concurso_nuevo" -> alNavegarANuevoConcurso()
                "refresh_concursos" -> modeloVistaConcursos.refrescar()
                "clear_filters" -> modeloVistaConcursos.alternarFiltro("CLEAR_ALL")
                "select_all" -> {
                    val actual = state.idsSeleccionados.size
                    val total = state.totalItems
                    if (actual >= total && total > 0) {
                        modeloVistaConcursos.deseleccionarTodo()
                    } else {
                        modeloVistaConcursos.seleccionarTodo(state.concursos.map { it.idConcurso })
                    }
                }
                "view_details" -> {
                    val id = state.idsSeleccionados.firstOrNull()
                    id?.let { idConcurso ->
                        state.concursos.find { it.idConcurso == idConcurso }?.let { resumen ->
                            concursoParaDetalles = resumen.concursoRaw
                        }
                    }
                }
                "force_close" -> {
                    if (state.idsSeleccionados.isNotEmpty()) {
                        showForceCloseConfirmDialog = true
                    }
                }
                "delete_multi" -> {
                    if (state.estaMultiseleccion && state.idsSeleccionados.isNotEmpty()) {
                        showDeleteConfirmDialog = true
                    }
                }
            }
        }
    }

    // 🔥 [ELITE SOBERANÍA]: Sincronización del contrato Be basado en Multiselección
    LaunchedEffect(state.estaMultiseleccion, state.idsSeleccionados) {
        val config = ContextoHUD.CONCURSOS.crearConfiguracionBase(
            navegacion = if (state.estaMultiseleccion) listOf("view_details", "force_close") else emptyList(), // 🔥 [ELITE]: Isla Izquierda
            edicion = if (state.estaMultiseleccion) listOf("delete_multi", "select_all","cancel" ) else listOf("select_all", "delete_multi"), // 🔥 [ELITE]: Isla Derecha (Reorganizada)
            mensajes = emptyList(),
            pistaBusqueda = "...¿BUSCAS UN PRESUPUESTO?"
        ).copy(
            accionesDeshabilitadas = if (state.idsSeleccionados.size != 1) listOf("view_details", "force_close") else emptyList(),
            ocultarOjos = state.estaMultiseleccion
        )
        beBrainViewModel.navCoordinador.actualizarContratoActual(config)
    }

    val beConfig = remember {
        ContextoHUD.CONCURSOS.crearConfiguracionBase(
            edicion = listOf("select_all", "delete_multi"),
            mensajes = emptyList(),
            pistaBusqueda = "¿BUSCÁS UNA LICITACIÓN? ⚖️✨"
        )
    }

    DisposableEffect(Unit) {
        beBrainViewModel.navCoordinador.reiniciarContextoHUD(ContextoHUD.CONCURSOS)
        beBrainViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose { 
            beBrainViewModel.navCoordinador.removerPantalla(beConfig.id)
            modeloVistaConcursos.deseleccionarTodo() // 🔥 [SANEAMIENTO]
        }
    }
    
    val perfiles by modeloVistaUsuario.identidadesSoberanas.collectAsStateWithLifecycle()

    ConcursoPublicoScreenContent(
        state = state,
        perfiles = perfiles,
        alSeleccionarPerfil = { modeloVistaUsuario.seleccionarPerfil(if (it.id == "personal") null else it.id) },
        alRegresar = alRegresar, 
        rellenoInferior = rellenoInferior,
        alVerDetallesConcurso = { concursoParaDetalles = it }, 
        alCerrarDetallesConcurso = { concursoParaDetalles = null },
        alRefrescar = { modeloVistaVista -> modeloVistaVista.refrescar() },
        alHacerClickConcurso = { contest -> 
            alNavegarAPresupuestosConcurso(contest.idConcurso)
        },
        presupuestoParaVistaPrevia = presupuestoParaVistaPrevia, 
        alCerrarVistaPreviaPresupuesto = { presupuestoParaVistaPrevia = null },
        alAceptarPresupuesto = { modeloVistaPresupuestos.aceptarPresupuesto(it) }, 
        alHacerClickChat = alHacerClickChat,
        modeloVista = modeloVistaConcursos
    )

    // --- 📺 GESTIÓN DE PUBLICIDAD DE VIDEO (UBICACIÓN GLOBAL) ---
    val borradorViewModel: com.example.myapplication.viewmodel.budget.BorradorConcursoViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    val mostrarAds by borradorViewModel.mostrarPublicidad.collectAsStateWithLifecycle()
    com.example.myapplication.uishared.ui.components.RewardedInterstitialVideoAd(
        show = mostrarAds,
        onRewardEarned = { /* El premio es la publicación */ },
        onDismiss = { borradorViewModel.cerrarPublicidad() }
    )

    if (mostrarAnaliticas) {
        val presupuestosAnaliticos by analyticsViewModel.presupuestosAnaliticos.collectAsStateWithLifecycle()
        val concursoVirtualAnalytics by analyticsViewModel.concursoVirtual.collectAsStateWithLifecycle()

        concursoVirtualAnalytics?.let { t ->
            Dialog(
                onDismissRequest = { mostrarAnaliticas = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    com.example.myapplication.ui.pantallas.budget.BudgetComparisonAnalytics(
                        tender = t,
                        budgets = presupuestosAnaliticos,
                        onBack = { mostrarAnaliticas = false },
                        onViewBudgetDetail = { idPresupuesto ->
                            modeloVistaPresupuestos.cargarDetallePresupuesto(idPresupuesto)
                            mostrarAnaliticas = false
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar ofertas") },
            text = { Text("¿Estás seguro de que deseas eliminar las ofertas seleccionadas? Esta acción dejará un aviso en el sistema.") },
            confirmButton = {
                TextButton(onClick = { 
                    modeloVistaConcursos.eliminarSeleccionados()
                    showDeleteConfirmDialog = false 
                }) { 
                    Text("Eliminar", color = Color.Red) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { 
                    Text("Cancelar") 
                }
            }
        )
    }

    if (showForceCloseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showForceCloseConfirmDialog = false },
            title = { Text("Finalizar concurso") },
            text = { Text("¿Deseas cerrar esta licitación de forma permanente? No se podrán recibir más ofertas.") },
            confirmButton = {
                TextButton(onClick = { 
                    val id = state.idsSeleccionados.firstOrNull()
                    id?.let { modeloVistaConcursos.forzarCierreConcurso(it) }
                    showForceCloseConfirmDialog = false 
                }) { 
                    Text("Finalizar", color = SharedPalette.RogCrimson) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showForceCloseConfirmDialog = false }) { 
                    Text("Volver") 
                }
            }
        )
    }

    if (concursoParaDetalles != null) {
        SheetEmergenteVertical(
            isVisible = true,
            onClose = { concursoParaDetalles = null },
            title = concursoParaDetalles?.titulo ?: "Detalles",
            emoji = "📝",
            helperText = "Información del Proyecto"
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = concursoParaDetalles?.descripcion ?: "", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                // Más detalles aquí
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcursoPublicoScreenContent(
    state: ConcursoPublicoUiState,
    perfiles: List<PerfilIdentidadV3>, 
    alSeleccionarPerfil: (PerfilIdentidadV3) -> Unit,
    alRegresar: () -> Unit, 
    rellenoInferior: PaddingValues,
    alVerDetallesConcurso: (ConcursoPublicoEntity) -> Unit, 
    alCerrarDetallesConcurso: () -> Unit,
    alRefrescar: (ConcursoPublicoViewModel) -> Unit, 
    alHacerClickConcurso: (ConcursoPublicoEntity) -> Unit,
    presupuestoParaVistaPrevia: PresupuestoConItems?, 
    alCerrarVistaPreviaPresupuesto: () -> Unit,
    alAceptarPresupuesto: (PresupuestoFinalEntity) -> Unit, 
    alHacerClickChat: (String, String?) -> Unit = { _, _ -> },
    modeloVista: ConcursoPublicoViewModel? = null
) {
    val estadoPull = rememberPullToRefreshState()
    var menuFiltrosAbierto by remember { mutableStateOf<String?>(null) }
    
    var acumuladorScroll by remember { mutableFloatStateOf(0f) }
    val conexionScrollAnidado = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val nScroll = (acumuladorScroll - delta).coerceIn(0f, 330f)
                val consumido = acumuladorScroll - nScroll
                acumuladorScroll = nScroll
                return if (acumuladorScroll >= 330f && delta < 0) Offset.Zero else Offset(0f, consumido)
            }
        }
    }
    
    val fraccionColapso = remember { derivedStateOf { (acumuladorScroll / 330f).coerceIn(0f, 1f) } }
    val fraccionOcultarBarraFiltros = remember { derivedStateOf { (acumuladorScroll / 80f).coerceIn(0f, 1f) } }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(conexionScrollAnidado)) {
        Scaffold(
            containerColor = Color.Transparent, 
            modifier = Modifier
                .padding(bottom = rellenoInferior.calculateBottomPadding()), 
            topBar = {
                MoldeCabeceraSuperiorPantallas(
                    fraccionColapso = fraccionColapso.value,
                    slotIzquierdo = {
                        BotonBackCabeceraV3(onClick = alRegresar)
                    },
                    slotCentral = {
                        ColumnaTituloSeccionV3(
                            titulo = "Mis Cotizaciones",
                            subtitulo = "Administrador de cotizaciones",
                            fraccionColapso = fraccionColapso.value
                        )
                    },
                    slotDerecho = {
                        EmojiImpactoV3(
                            emoji = "💼",
                            fraccionColapso = fraccionColapso.value
                        )
                    }
                )
            }
    ) { relleno ->
        Column(modifier = Modifier.fillMaxSize().padding(top = relleno.calculateTopPadding())) {
            
            Box(modifier = Modifier.fillMaxWidth().animateContentSize(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    dampingRatio = Spring.DampingRatioLowBouncy
                )
            ).graphicsLayer {
                alpha = 1f - fraccionOcultarBarraFiltros.value
                translationY = -10.dp.toPx() * fraccionOcultarBarraFiltros.value
            }.layout { measurable, constraints -> 
                val placeable = measurable.measure(constraints)
                val h = (placeable.height * (1f - fraccionOcultarBarraFiltros.value)).toInt()
                layout(placeable.width, h) { placeable.placeRelative(0, 0) }
            }) {
                val burbujas = remember(state.filtros, state.todasLasCategorias) {
                    ArmadorFiltrosV3.armarBurbujasConcursoPublico(state.filtros, state.todasLasCategorias)
                }

                val idsSeleccionados = remember(state.filtros) {
                    val set = mutableSetOf<String>()
                    if (state.filtros.soloActivos) set.add("filter_concurso_activo")
                    if (state.filtros.soloCerrados) set.add("filter_concurso_cerrado")
                    if (state.filtros.soloAdjudicados) set.add("filter_concurso_adjudicado")
                    if (state.filtros.soloNoLeidos) set.add("filter_concurso_no_leidos")
                    set.add(state.filtros.orden)
                    state.filtros.idsCategorias.forEach { set.add("cat_$it") }
                    set
                }

                BarraFiltrosV3(
                    filtrosActivos = burbujas,
                    alHacerClickMenu = { tipo -> menuFiltrosAbierto = if (menuFiltrosAbierto == tipo) null else tipo },
                    alEliminarFiltro = { modeloVista?.alternarFiltro(it) },
                    alLimpiarTodo = { modeloVista?.alternarFiltro("CLEAR_ALL") },
                    mostrarMenuFiltros = menuFiltrosAbierto == "filtros",
                    mostrarMenuOrdenar = menuFiltrosAbierto == "ordenar",
                    mostrarMenuCategorias = menuFiltrosAbierto == "categorias",
                    idsFiltrosSeleccionados = idsSeleccionados,
                    alAlternarFiltro = { modeloVista?.alternarFiltro(it) },
                    alCerrarMenu = { menuFiltrosAbierto = null },
                    itemsCategoria = state.itemsCategoria, 
                    itemsFiltro = state.itemsFiltro, 
                    itemsOrden = state.itemsOrden,
                    estaCentrado = true,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            PullToRefreshBox(
                isRefreshing = state.estaRefrescando, 
                onRefresh = { modeloVista?.let { alRefrescar(it) } }, 
                state = estadoPull, 
                modifier = Modifier.fillMaxSize()
            ) {
                ArmadorListaPantallaCompleta(
                    modifier = Modifier.fillMaxSize(),
                    titulo = "Mis Concursos",
                    subtitulo = "Licitaciones Públicas Activas",
                    icono = null,
                    cantidadItems = state.concursos.size,
                    perfiles = perfiles,
                    idPerfilInicial = state.idPerfilSeleccionado,
                    alSeleccionarPerfil = { p: PerfilIdentidadV3 -> alSeleccionarPerfil(p) },
                    colorAcento = SharedPalette.ElectricCyan,
                    menuPerfil = { iden ->
                        MenuPerfilContenido(
                            identidades = perfiles,
                            idPerfilActivo = iden,
                            alSeleccionar = { p -> 
                                alSeleccionarPerfil(p)
                                modeloVista?.alternarMenuPerfil(false)
                            }
                        )
                    }
                ) { _ ->
                    if (state.concursos.isEmpty()) { 
                        if (state.estaRefrescando || state.estaCargando) {
                            items(5) { EsqueletoConcurso() }
                        } else {
                            item { EspacioVacioConcursos(state.filtros.estaActivo()) }
                        }
                    } else {
                        items(state.concursos, key = { it.idConcurso }) { resumen ->
                            val concurso = resumen.concursoRaw
                            
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                ConcursoFolderPremium(
                                    titulo = concurso.titulo, 
                                    categoria = resumen.nombreCategoria, 
                                    iconoCategoria = resumen.iconoCategoria, 
                                    colorSupercategoria = Color(0xFF2197F5),
                                    idConcurso = concurso.idConcurso, 
                                    estado = concurso.estado, 
                                    fechaInicio = concurso.fechaInicio, 
                                    fechaFin = concurso.fechaFin, 
                                    conteoPresupuestos = resumen.totalOfertas, 
                                    conteoNoLeidos = resumen.ofertasNoLeidas, 
                                    estaSeleccionado = state.idsSeleccionados.contains(concurso.idConcurso), 
                                    modoMultiseleccionActivo = state.estaMultiseleccion, 
                                    nombrePrestadorAdjudicado = concurso.nombrePrestadorAdjudicado, 
                                    idPresupuestoAdjudicado = concurso.idPresupuestoAdjudicado, 
                                    urlFotoPrestadorAdjudicado = concurso.urlFotoPrestadorAdjudicado, 
                                    alVerDetalles = { alVerDetallesConcurso(concurso) }, 
                                    alVerPresupuestoAdjudicado = { /* TODO: Navegar a A4 */ },
                                    alChatConPrestadorAdjudicado = { _ ->
                                        concurso.idPrestadorAdjudicado?.let { idPrestador ->
                                            alHacerClickChat(idPrestador, concurso.idCategoria)
                                        }
                                    },
                                    alHacerClick = { 
                                        if (state.estaMultiseleccion) modeloVista?.alternarSeleccionItem(concurso.idConcurso)
                                        else alHacerClickConcurso(concurso) 
                                    },
                                    alHacerLongClick = {
                                        modeloVista?.alternarSeleccionItem(concurso.idConcurso)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (presupuestoParaVistaPrevia != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        Dialog(
            onDismissRequest = alCerrarVistaPreviaPresupuesto, 
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            PlanillaPresupuestoA4Viewer(
                prestador = null, // Podríamos obtenerlo del ViewModel si fuera necesario
                relacion = presupuestoParaVistaPrevia, 
                alCerrar = alCerrarVistaPreviaPresupuesto, 
                nombreCliente = "Cliente"
            ) { _, _ ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (presupuestoParaVistaPrevia.cabecera.estado == EstadoPresupuesto.PENDIENTE) {
                        Button(
                            onClick = { alAceptarPresupuesto(presupuestoParaVistaPrevia.cabecera); alCerrarVistaPreviaPresupuesto() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
                            shape = RoundedCornerShape(12.dp)
                        ) { 
                            Text("ACEPTAR PRESUPUESTO", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 12.sp) 
                        }
                    } else { 
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            StatusPillPremium(presupuestoParaVistaPrevia.cabecera.estado.name) 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EspacioVacioConcursos(esBusqueda: Boolean = false) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp)) {
            if (esBusqueda) {
                Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "No se encontraron concursos", color = Color.White, fontWeight = FontWeight.Bold)
            } else {
                // --- 1. TÍTULO EXPLICATIVO ---
                TextCompacto(
                    text = "VISTA PREVIA DE TU PRÓXIMO PROYECTO",
                    color = SharedPalette.ElectricCyan.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(letterSpacing = 2.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // --- 2. TARJETA DE EJEMPLO (Visual Mock) ---
                Box(modifier = Modifier.graphicsLayer { alpha = 0.4f }) {
                    ConcursoFolderPremium(
                        titulo = "Ejemplo: Reparación de Techo",
                        categoria = "TECHISTA",
                        iconoCategoria = "🏠",
                        idConcurso = "mock_template",
                        estado = "ABIERTA",
                        fechaInicio = System.currentTimeMillis(),
                        fechaFin = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L),
                        conteoPresupuestos = 2,
                        conteoNoLeidos = 1,
                        estaSeleccionado = false,
                        alHacerClick = { /* No-op en template */ }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 3. MENSAJE DE ACCIÓN ---
                TextCompacto(
                    text = "AÚN NO TIENES LICITACIONES ACTIVAS",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Aquí aparecerán tus proyectos. Los profesionales de tu zona verán tu solicitud y te enviarán sus presupuestos para que puedas compararlos y elegir al mejor.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. GUÍA DEL ASISTENTE ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddCircle, null, tint = SharedPalette.AcidGreen, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    TextCompacto(
                        text = "Toca el botón '+' en el asistente Be para comenzar",
                        color = SharedPalette.AcidGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

package com.example.myapplication.ui.pantallas.budget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.ui.componentes.sistema.cabecera.*
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorGridPantallaCompleta
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.ui.componentes.StatusPillPremium 
import com.example.myapplication.uishared.ui.components.TarjetaPresupuesto
import com.example.myapplication.uishared.ui.components.TarjetaPresupuestoMiniSkeleton
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.viewmodel.budget.ConcursoPresupuestosViewModel
import com.example.myapplication.viewmodel.budget.ConcursoPresupuestosUiState
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.ui.componentes.be.modelos.BeZIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Settings

/**
 * --- PANTALLA DE PRESUPUESTOS DE CONCURSO (v2026.ELITE) ---
 * [PROPÓSITO]: Vista soberana para gestionar ofertas recibidas en una licitación.
 */
@Composable
fun ConcursoPresupuestoScreen(
    idConcurso: String,
    viewModel: ConcursoPresupuestosViewModel = hiltViewModel(),
    beCerebroVm: com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel = hiltViewModel(),
    analyticsViewModel: com.example.myapplication.viewmodel.budget.PresupuestoAnalyticsViewModel = hiltViewModel(),
    budgetViewModel: com.example.myapplication.viewmodel.budget.UsuarioPresupuestoViewModel = hiltViewModel(),
    alRegresar: () -> Unit,
    alHacerClickChat: (String, String?) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val presupuestos = state.presupuestosPaginados.collectAsLazyPagingItems()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    var showDeleteContestDialog by remember { mutableStateOf(false) }
    var showTerminateContestDialog by remember { mutableStateOf(false) }
    
    val budgetDetail by budgetViewModel.presupuestoSeleccionado.collectAsStateWithLifecycle()
    var budgetForA4Preview by remember { mutableStateOf<com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems?>(null) }
    
    LaunchedEffect(budgetDetail) {
        if (budgetDetail != null) budgetForA4Preview = budgetDetail
    }

    LaunchedEffect(idConcurso) {
        viewModel.inicializar(idConcurso)
    }

    val estaBuscando by viewModel.beBusquedaMotor.estaBusquedaActiva.collectAsStateWithLifecycle()
    androidx.activity.compose.BackHandler(enabled = !estaBuscando) {
        alRegresar()
    }

    LaunchedEffect(Unit) {
        beCerebroVm.actionEvent.collect { actionId ->
            if (beCerebroVm.navCoordinador.contratoActivo.value.id != viewModel.idSoberania) return@collect

            when (actionId) {
                "select_all" -> {
                    val actual = state.idsSeleccionados.size
                    val total = state.totalItems
                    if (actual >= total && total > 0) {
                        viewModel.deseleccionarTodo()
                    } else {
                        viewModel.seleccionarTodo()
                    }
                }
                "delete_multi" -> if (state.idsSeleccionados.isNotEmpty()) showDeleteDialog = true
                "compare_budgets" -> if (state.idsSeleccionados.isNotEmpty()) {
                    analyticsViewModel.inicializarConPresupuestos(state.idsSeleccionados.toList())
                    showAnalytics = true
                }
            }
        }
    }

    ConcursoPresupuestoContent(
        state = state,
        presupuestos = presupuestos,
        alRegresar = alRegresar,
        alRefrescar = { viewModel.refrescar() },
        alAlternarFiltro = { viewModel.alternarFiltro(it) },
        alEstablecerMenuFiltros = { viewModel.establecerMenuFiltros(it) },
        alHacerClickPresupuesto = { id -> budgetViewModel.cargarDetallePresupuesto(id) },
        alHacerClickChat = { idPrestador -> alHacerClickChat(idPrestador, state.concursoInfo?.concursoRaw?.idCategoria) },
        alAlternarSeleccion = { viewModel.alternarSeleccion(it) },
        alLimpiarTodo = { viewModel.limpiarTodo() },
        onVerDetalles = { viewModel.verDetallesConcurso() },
        onTerminar = { showTerminateContestDialog = true },
        onEliminar = { showDeleteContestDialog = true }
    )

    if (showDeleteContestDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteContestDialog = false },
            title = { Text("Eliminar Concurso") },
            text = { Text("¿Deseas eliminar definitivamente este concurso y todas sus ofertas?") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.eliminarConcurso()
                    showDeleteContestDialog = false 
                    alRegresar()
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteContestDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showTerminateContestDialog) {
        AlertDialog(
            onDismissRequest = { showTerminateContestDialog = false },
            title = { Text("Terminar Concurso") },
            text = { Text("¿Deseas finalizar el período de recepción de ofertas para este concurso?") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.terminarConcurso()
                    showTerminateContestDialog = false 
                }) { Text("Finalizar", color = SharedPalette.AcidGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showTerminateContestDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (budgetForA4Preview != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { budgetForA4Preview = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Viewer(
                prestador = null,
                relacion = budgetForA4Preview!!,
                alCerrar = { budgetForA4Preview = null },
                nombreCliente = "Yo"
            ) { _, _ -> }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar ofertas") },
            text = { Text("¿Deseas eliminar las ${state.idsSeleccionados.size} ofertas seleccionadas?") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.eliminarSeleccionados()
                    showDeleteDialog = false 
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showAnalytics) {
        val presupuestosAnaliticos by analyticsViewModel.presupuestosAnaliticos.collectAsStateWithLifecycle()
        val concursoInfo = state.concursoInfo?.concursoRaw

        if (concursoInfo != null) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showAnalytics = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BudgetComparisonAnalytics(
                        tender = concursoInfo,
                        budgets = presupuestosAnaliticos,
                        onBack = { showAnalytics = false },
                        onViewBudgetDetail = { budgetViewModel.cargarDetallePresupuesto(it); showAnalytics = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcursoPresupuestoContent(
    state: ConcursoPresupuestosUiState,
    presupuestos: androidx.paging.compose.LazyPagingItems<PresupuestoResumenDominio>,
    alRegresar: () -> Unit,
    alRefrescar: () -> Unit,
    alAlternarFiltro: (String) -> Unit,
    alEstablecerMenuFiltros: (String?) -> Unit,
    alHacerClickPresupuesto: (String) -> Unit,
    alHacerClickChat: (String) -> Unit,
    alAlternarSeleccion: (String) -> Unit,
    alLimpiarTodo: () -> Unit,
    onVerDetalles: () -> Unit,
    onTerminar: () -> Unit,
    onEliminar: () -> Unit
) {
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
            topBar = {
                CabeceraConcursoSoberanaV3(
                    concurso = state.concursoInfo,
                    fraccionColapso = fraccionColapso.value,
                    alRegresar = alRegresar,
                    onVerDetalles = onVerDetalles,
                    onTerminar = onTerminar,
                    onEliminar = onEliminar
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)
                        )
                        .graphicsLayer {
                            alpha = 1f - fraccionOcultarBarraFiltros.value
                            translationY = -10.dp.toPx() * fraccionOcultarBarraFiltros.value
                        }
                        .layout { measurable, constraints -> 
                            val placeable = measurable.measure(constraints)
                            val h = (placeable.height * (1f - fraccionOcultarBarraFiltros.value)).toInt()
                            layout(placeable.width, h) { placeable.placeRelative(0, 0) }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    val filtrosVisuales = remember(state.filtrosActivos, state.itemsRubros) {
                        state.filtrosActivos.mapNotNull { id ->
                            if (id.startsWith("cat_")) {
                                val rubro = state.itemsRubros.find { it.id == id }
                                com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro(id, rubro?.label ?: id.removePrefix("cat_"), rubro?.emoji ?: "📋")
                            } else {
                                val filter = state.itemsFiltro.find { it.id == id } ?: state.itemsOrden.find { it.id == id }
                                filter?.let { com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro(id, it.label, it.emoji ?: "🔹") }
                            }
                        }
                    }

                    BarraFiltrosV3(
                        filtrosActivos = filtrosVisuales,
                        alHacerClickMenu = alEstablecerMenuFiltros,
                        alEliminarFiltro = alAlternarFiltro,
                        alLimpiarTodo = alLimpiarTodo,
                        mostrarMenuFiltros = state.menuFiltrosAbierto == "filtros",
                        mostrarMenuOrdenar = state.menuFiltrosAbierto == "ordenar",
                        mostrarMenuCategorias = state.menuFiltrosAbierto == "categorias",
                        idsFiltrosSeleccionados = state.filtrosActivos,
                        alAlternarFiltro = alAlternarFiltro,
                        alCerrarMenu = { alEstablecerMenuFiltros(null) },
                        itemsFiltro = state.itemsFiltro,
                        itemsOrden = state.itemsOrden,
                        itemsCategoria = state.itemsRubros,
                        estaCentrado = true
                    )
                }

                ArmadorGridPantallaCompleta(
                    modifier = Modifier.weight(1f),
                    titulo = "OFERTAS RECIBIDAS",
                    subtitulo = "Ofertas Técnicas y Económicas",
                    cantidadItems = presupuestos.itemCount,
                    colorAcento = SharedPalette.ElectricCyan,
                    colorContenedor = SharedPalette.EliteSurface
                ) {
                    if (state.estaCargando && presupuestos.itemCount == 0) {
                        items(6) { TarjetaPresupuestoMiniSkeleton() }
                    } else if (presupuestos.itemCount == 0 && presupuestos.loadState.append is androidx.paging.LoadState.NotLoading && presupuestos.loadState.append.endOfPaginationReached) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                            Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                                Text("Aún no has recibido ofertas", color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    } else {
                        items(count = presupuestos.itemCount, key = presupuestos.itemKey { it.idPresupuesto }) { index ->
                            val p = presupuestos[index]
                            if (p != null) {
                                TarjetaPresupuesto(
                                    presupuesto = p,
                                    estaSeleccionado = state.idsSeleccionados.contains(p.idPresupuesto),
                                    esMultiseleccionActiva = state.estaMultiseleccion,
                                    alHacerClick = { 
                                        if (state.estaMultiseleccion) alAlternarSeleccion(p.idPresupuesto)
                                        else alHacerClickPresupuesto(p.idPresupuesto)
                                    },
                                    alHacerClickChat = { alHacerClickChat(p.idPrestador) },
                                    alHacerLongClick = { alAlternarSeleccion(p.idPresupuesto) },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * --- CABECERA SOBERANA CONCURSO (ELITE v2026) ---
 * [DISEÑO]: Fondo degradado SSOT (V2DeepVoid -> ROG_Dark_Bg).
 */
@Composable
fun CabeceraConcursoSoberanaV3(
    concurso: com.example.myapplication.core.dominio.modelos.ConcursoPublicoResumenDominio?,
    fraccionColapso: Float,
    alRegresar: () -> Unit,
    onVerDetalles: () -> Unit,
    onTerminar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colorAcento = Color(0xFF2197F5) // appBlue Maverick
    val localizacion = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // 1. Barra de Acento Lateral Izquierda (Maverick Folder Style)
                drawRect(
                    brush = Brush.verticalGradient(listOf(colorAcento.copy(alpha = 0.8f), colorAcento)),
                    topLeft = Offset(0f, 0f),
                    size = Size(4.dp.toPx(), size.height)
                )
                // 2. Borde Inferior Neón Cyber Sutil
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(colorAcento.copy(alpha = 0.05f), colorAcento.copy(alpha = 0.6f), colorAcento.copy(alpha = 0.05f))
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(SharedPalette.V2DeepVoid, SharedPalette.ROG_Dark_Bg)
                    )
                )
                .statusBarsPadding()
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- ROW 1: BARRA DE NAVEGACIÓN PRINCIPAL ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotonBackCabeceraV3(onClick = alRegresar)

                    Spacer(Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (concurso != null) {
                            com.example.myapplication.uishared.ui.components.TextCompactoAutoFit(
                                text = "${concurso.iconoCategoria} ${concurso.nombreCategoria.uppercase(localizacion)}",
                                color = colorAcento.copy(alpha = 0.9f),
                                maxFontSize = 9.sp,
                                minFontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                style = TextStyle(letterSpacing = 1.2.sp)
                            )
                            
                            com.example.myapplication.uishared.ui.components.TextCompactoAutoFit(
                                text = concurso.concursoRaw.titulo.uppercase(localizacion),
                                color = Color.White,
                                maxFontSize = 16.sp,
                                minFontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        } else {
                            TextCompacto("CARGANDO...", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (concurso != null) {
                            StatusPillPremium(estado = concurso.concursoRaw.estado)
                        }
                        
                        MenuAccionesConcurso(
                            onVerDetalles = onVerDetalles,
                            onTerminar = onTerminar,
                            onEliminar = onEliminar
                        )
                    }
                }

                // --- ROW 2: DETALLES SECUNDARIOS Y STATS ---
                if (concurso != null) {
                    val alfaExpansible = (1f - fraccionColapso * 2.5f).coerceIn(0f, 1f)
                    
                    if (alfaExpansible > 0f) {
                        Spacer(Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = alfaExpansible
                                    translationY = -8.dp.toPx() * fraccionColapso
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                TextCompacto(
                                    text = "🏷️ PROYECTO",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    style = TextStyle(letterSpacing = 0.5.sp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Box(Modifier.width(1.dp).height(10.dp).background(Color.White.copy(0.2f)))
                                Spacer(Modifier.width(4.dp))
                                TextCompacto(
                                    text = "#${concurso.concursoRaw.idConcurso.takeLast(6).uppercase(localizacion)}",
                                    color = colorAcento.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = Color.Black.copy(0.5f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StatItemSimple(
                                        count = concurso.ofertasNoLeidas,
                                        label = "NUEVAS",
                                        color = SharedPalette.AcidGreen
                                    )
                                    Box(Modifier.width(1.dp).height(10.dp).background(Color.White.copy(0.15f)))
                                    StatItemSimple(
                                        count = concurso.totalOfertas,
                                        label = "TOTAL",
                                        color = colorAcento
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

@Composable
private fun StatItemSimple(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextCompacto(
            text = count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = if (count > 0) color else Color.White.copy(alpha = 0.3f)
        )
        Spacer(Modifier.width(3.dp))
        TextCompacto(
            text = label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            color = if (count > 0) color.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun MenuAccionesConcurso(onVerDetalles: () -> Unit, onTerminar: () -> Unit, onEliminar: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
        DropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false }, 
            modifier = Modifier.background(Color(0xFF161C24))
        ) {
            DropdownMenuItem(
                text = { Text("Ver Detalles", color = Color.White, fontSize = 12.sp) }, 
                onClick = { expanded = false; onVerDetalles() }, 
                leadingIcon = { Icon(Icons.Default.Info, null, tint = SharedPalette.ElectricCyan, modifier = Modifier.size(18.dp)) }
            )
            DropdownMenuItem(
                text = { Text("Terminar Concurso", color = Color.White, fontSize = 12.sp) }, 
                onClick = { expanded = false; onTerminar() }, 
                leadingIcon = { Icon(Icons.Default.TaskAlt, null, tint = SharedPalette.AcidGreen, modifier = Modifier.size(18.dp)) }
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            DropdownMenuItem(
                text = { Text("Eliminar", color = Color.Red, fontSize = 12.sp) }, 
                onClick = { expanded = false; onEliminar() }, 
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
            )
        }
    }
}

@Preview(name = "Concurso Presupuesto - Lista", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewConcursoPresupuestoScreen() {
    PBEMTheme {
        val mockPaginados = kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty<PresupuestoResumenDominio>()).collectAsLazyPagingItems()
        ConcursoPresupuestoContent(
            state = ConcursoPresupuestosUiState(estaCargando = false, filtrosActivos = setOf("sort_date")), 
            presupuestos = mockPaginados, 
            alRegresar = {}, 
            alRefrescar = {}, 
            alAlternarFiltro = {}, 
            alEstablecerMenuFiltros = {}, 
            alHacerClickPresupuesto = {}, 
            alHacerClickChat = {}, 
            alAlternarSeleccion = {}, 
            alLimpiarTodo = {}, 
            onVerDetalles = {}, 
            onTerminar = {}, 
            onEliminar = {}
        )
    }
}

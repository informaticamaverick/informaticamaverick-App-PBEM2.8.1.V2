package com.example.myapplication.ui.pantallas.chat.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.ui.componentes.sistema.cabecera.*
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorGridPantallaCompleta
import com.example.myapplication.ui.componentes.sistema.menu.v3.*
import com.example.myapplication.uishared.ui.components.TarjetaPresupuesto
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.viewmodel.chat.ArchiveroChatMultimediaViewModel
import com.example.myapplication.viewmodel.chat.ArchiveroChatMultimediaUiState
import com.example.myapplication.viewmodel.chat.TipoContenidoMultimedia
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.uishared.ui.components.TarjetaPresupuestoMiniSkeleton
import java.util.Locale

/**
 * --- PANTALLA DE ARCHIVERO MULTIMEDIA DEL CHAT (v2026.ELITE) ---
 * [PROPÓSITO]: Centro soberano para gestionar archivos y documentos de una conversación.
 * [LEY #1]: Pantalla Tonta. Reactiva al UiState.
 */
@Composable
fun ArchiveroChatMultimediaScreen(
    idRemoto: String,
    idLocal: String,
    viewModel: ArchiveroChatMultimediaViewModel = hiltViewModel(),
    beCerebroVm: com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel = hiltViewModel(),
    analyticsViewModel: com.example.myapplication.viewmodel.budget.PresupuestoAnalyticsViewModel = hiltViewModel(),
    budgetViewModel: com.example.myapplication.viewmodel.budget.UsuarioPresupuestoViewModel = hiltViewModel(), // 🔥 [NEW]
    alRegresar: () -> Unit,
    alHacerClickImagen: (String) -> Unit,
    alNavegarAChat: (String, String) -> Unit 
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val presupuestos = state.presupuestosPaginados.collectAsLazyPagingItems()
    val imagenes = state.imagenesPaginadas.collectAsLazyPagingItems()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    
    // --- GESTIÓN DE VISTA PREVIA A4 ---
    val budgetDetail by budgetViewModel.presupuestoSeleccionado.collectAsStateWithLifecycle()
    var budgetForA4Preview by remember { mutableStateOf<com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems?>(null) }
    
    LaunchedEffect(budgetDetail) {
        if (budgetDetail != null) budgetForA4Preview = budgetDetail
    }

    LaunchedEffect(idRemoto, idLocal) {
        viewModel.inicializar(idRemoto, idLocal)
    }

    // 🔥 [v2026.ELITE]: Manejo de retroceso soberano.
    // Solo permitimos el cierre de pantalla si NO hay búsqueda activa (el Coordinador maneja la búsqueda).
    val estaBuscando by viewModel.beBusquedaMotor.estaBusquedaActiva.collectAsStateWithLifecycle()
    androidx.activity.compose.BackHandler(enabled = !estaBuscando) {
        alRegresar()
    }

    // 🔥 [v2026.ELITE]: Recolector de acciones soberanas del Asistente Be
    LaunchedEffect(Unit) {
        viewModel.eventosNavegacion.collect { evento ->
            if (evento.startsWith("NAV_CHAT_")) {
                val parts = evento.removePrefix("NAV_CHAT_").split("_")
                if (parts.size >= 2) alNavegarAChat(parts[0], parts[1])
            }
        }
    }

    LaunchedEffect(Unit) {
        beCerebroVm.actionEvent.collect { actionId ->
            // 🔥 [LEY #12]: Soberanía por Contrato.
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

    ArchiveroChatMultimediaContent(
        state = state,
        presupuestos = presupuestos,
        imagenes = imagenes,
        alRegresar = alRegresar,
        alCambiarTipo = { viewModel.cambiarTipo(it) },
        alEstablecerMenuSelector = { viewModel.establecerMenuSelector(it) },
        alHacerClickPresupuesto = { id -> budgetViewModel.cargarDetallePresupuesto(id) },
        alHacerClickImagen = alHacerClickImagen,
        alHacerClickChat = { pid -> viewModel.irAlChat(pid) },
        alLimpiarTodo = { viewModel.limpiarTodo() },
        alAlternarSeleccion = { viewModel.alternarSeleccion(it) },
        alAlternarFiltro = { viewModel.alternarFiltro(it) },
        alEstablecerMenuFiltros = { viewModel.establecerMenuFiltros(it) }
    )

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
            ) { _, _ ->
                // Botones de acción si fueran necesarios en el visor
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar archivos") },
            text = { Text("¿Deseas eliminar los ${state.idsSeleccionados.size} elementos seleccionados?") },
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
        val tenderVirtual = com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity(
            idConcurso = "virtual",
            titulo = "Comparativa Directa",
            descripcion = "Análisis de presupuestos recibidos en chat",
            idCategoria = ""
        )

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAnalytics = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                com.example.myapplication.ui.pantallas.budget.BudgetComparisonAnalytics(
                    tender = tenderVirtual,
                    budgets = presupuestosAnaliticos,
                    onBack = { showAnalytics = false },
                    onViewBudgetDetail = { budgetViewModel.cargarDetallePresupuesto(it); showAnalytics = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveroChatMultimediaContent(
    state: ArchiveroChatMultimediaUiState,
    presupuestos: androidx.paging.compose.LazyPagingItems<PresupuestoResumenDominio>,
    imagenes: androidx.paging.compose.LazyPagingItems<com.example.myapplication.core.datos.local.entidades.MensajeEntity>,
    alRegresar: () -> Unit,
    alCambiarTipo: (TipoContenidoMultimedia) -> Unit,
    alEstablecerMenuSelector: (Boolean) -> Unit,
    alHacerClickPresupuesto: (String) -> Unit,
    alHacerClickImagen: (String) -> Unit,
    alHacerClickChat: (String) -> Unit,
    alLimpiarTodo: () -> Unit,
    alAlternarSeleccion: (String) -> Unit,
    alAlternarFiltro: (String) -> Unit,
    alEstablecerMenuFiltros: (String?) -> Unit
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
                MoldeCabeceraSuperiorPantallas(
                    fraccionColapso = fraccionColapso.value,
                    slotIzquierdo = { BotonBackCabeceraV3(onClick = alRegresar) },
                    slotCentral = {
                        ColumnaTituloSeccionV3(
                            titulo = if (state.prestador != null) state.prestador.titulo else "Archivo Multimedia",
                            subtitulo = if (state.prestador != null) state.prestador.subtitulo ?: "Global" else "Todas las conversaciones",
                            fraccionColapso = fraccionColapso.value
                        )
                    },
                    slotDerecho = {
                        EmojiImpactoV3(
                            emoji = "📂",
                            fraccionColapso = fraccionColapso.value
                        )
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding())
            ) {
                // --- BARRA DE FILTROS ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium,
                                dampingRatio = Spring.DampingRatioLowBouncy
                            )
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

                // --- CUERPO DE LISTA MULTIMEDIA ---
                ArmadorGridPantallaCompleta(
                    modifier = Modifier.weight(1f),
                    titulo = state.tipoActivo.titulo,
                    subtitulo = state.tipoActivo.subtitulo,
                    colorAcento = SharedPalette.ElectricCyan,
                    accionesIzquierda = {
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { alEstablecerMenuSelector(true) }
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(text = state.tipoActivo.emoji, fontSize = 24.sp)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            MoldeMenuArmadorV3(
                                expanded = state.menuSelectorAbierto,
                                onDismissRequest = { alEstablecerMenuSelector(false) },
                                alignment = Alignment.TopStart,
                                verticalOffset = 42.dp,
                                arrowOffset = 20.dp
                            ) {
                                MenuGrupoV3 {
                                    MenuSectionHeaderV3(text = "SELECCIONAR CATEGORÍA")
                                    TipoContenidoMultimedia.entries.forEach { tipo ->
                                        MenuItemEliteV3(
                                            label = tipo.titulo.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                                            emoji = tipo.emoji,
                                            isSelected = state.tipoActivo == tipo,
                                            onClick = { alCambiarTipo(tipo) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) {
                    renderizarContenidoMultimedia(state, presupuestos, imagenes, alHacerClickPresupuesto, alHacerClickImagen, alHacerClickChat, alAlternarSeleccion)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.renderizarContenidoMultimedia(
    state: ArchiveroChatMultimediaUiState,
    presupuestos: androidx.paging.compose.LazyPagingItems<PresupuestoResumenDominio>,
    imagenes: androidx.paging.compose.LazyPagingItems<com.example.myapplication.core.datos.local.entidades.MensajeEntity>,
    alHacerClickPresupuesto: (String) -> Unit,
    alHacerClickImagen: (String) -> Unit,
    alHacerClickChat: (String) -> Unit,
    alAlternarSeleccion: (String) -> Unit
) {
    if (state.estaCargando) {
        items(6) { TarjetaPresupuestoMiniSkeleton() }
        return
    }

    when (state.tipoActivo) {
        TipoContenidoMultimedia.PRESUPUESTOS -> {
            if (presupuestos.itemCount == 0 && presupuestos.loadState.append is androidx.paging.LoadState.NotLoading && presupuestos.loadState.append.endOfPaginationReached) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    EmptyStateArchivero("No se encontraron presupuestos")
                }
            } else {
                items(
                    count = presupuestos.itemCount,
                    key = presupuestos.itemKey { p: PresupuestoResumenDominio -> p.idPresupuesto }
                ) { index ->
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
        TipoContenidoMultimedia.IMAGENES -> {
            if (imagenes.itemCount == 0 && imagenes.loadState.append is androidx.paging.LoadState.NotLoading && imagenes.loadState.append.endOfPaginationReached) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                    EmptyStateArchivero("No hay imágenes en este archivo")
                }
            } else {
                items(
                    count = imagenes.itemCount,
                    key = imagenes.itemKey { m: com.example.myapplication.core.datos.local.entidades.MensajeEntity -> m.id }
                ) { index ->
                    val msg = imagenes[index]
                    if (msg != null) {
                        Surface(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clickable { alHacerClickImagen(msg.contenido) },
                            color = Color.White.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "📸", fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }
        else -> {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                EmptyStateArchivero("Contenido próximamente disponible")
            }
        }
    }
}

@Composable
private fun EmptyStateArchivero(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mensaje,
            color = Color.White.copy(alpha = 0.3f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(name = "Archivero Multimedia - Galería", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewArchiveroChatMultimediaScreen() {
    PBEMTheme {
        val mockPresupuestos = kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty<PresupuestoResumenDominio>()).collectAsLazyPagingItems()
        val mockImagenes = kotlinx.coroutines.flow.flowOf(androidx.paging.PagingData.empty<com.example.myapplication.core.datos.local.entidades.MensajeEntity>()).collectAsLazyPagingItems()

        ArchiveroChatMultimediaContent(
            state = ArchiveroChatMultimediaUiState(
                prestador = com.example.myapplication.core.dominio.modelos.PrestadorDominio(
                    id = "p1",
                    titulo = "Maverick Soluciones",
                    subtitulo = "Soporte Técnico"
                ),
                tipoActivo = TipoContenidoMultimedia.IMAGENES,
                estaCargando = false
            ),
            presupuestos = mockPresupuestos,
            imagenes = mockImagenes,
            alRegresar = {},
            alCambiarTipo = {},
            alEstablecerMenuSelector = {},
            alHacerClickPresupuesto = {},
            alHacerClickImagen = {},
            alHacerClickChat = {},
            alLimpiarTodo = {},
            alAlternarSeleccion = {},
            alAlternarFiltro = {},
            alEstablecerMenuFiltros = {}
        )
    }
}

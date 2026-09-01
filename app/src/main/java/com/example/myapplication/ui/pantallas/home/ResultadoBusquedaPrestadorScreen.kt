package com.example.myapplication.ui.pantallas.home

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.core.datos.local.entidades.CuentaEntity
import com.example.myapplication.core.datos.local.entidades.IdentidadUsuarioEntity
import com.example.myapplication.core.dominio.modelos.CuentaMaestroUsuario
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.modelos.TipoPrestador
import com.example.myapplication.core.dominio.modelos.UsuarioDominio
import com.example.myapplication.core.dominio.modelos.UsuarioDominioCompleto
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.ui.componentes.be.modelos.ConfiguracionContextoBe
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.ui.componentes.be.modelos.MensajeBe
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.cabecera.ColumnaTituloSeccionV3
import com.example.myapplication.ui.componentes.sistema.cabecera.EmojiImpactoV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPantallas
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro
import com.example.myapplication.ui.componentes.sistema.contexto.ContadorResultadosElite
import com.example.myapplication.ui.componentes.sistema.contexto.MoldeTarjetaPerfilDirec
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEmergenteV3
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorListaPantallaCompleta
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.uishared.ui.components.profile.parts.PrestadorPerfilMocks
import com.example.myapplication.ui.componentes.sistema.GrillaShimmerPrestadores
import com.example.myapplication.viewmodel.home.ResultadoBusquedaPrestadorViewModel
import com.example.myapplication.viewmodel.home.UbicacionGpsObrero
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel
import kotlinx.coroutines.flow.flowOf

/**
 * ResultadoBusquedaPrestadorScreen: Evolución v2026 de la búsqueda por rubro.
 * [ELITE]: Utiliza MoldeCabeceraSuperiorPantallas y MoldeTarjetaPerfilDirec.
 */
@Composable
fun ResultadoBusquedaPrestadorScreen(
    idCategoria: String,
    alVolver: () -> Unit,
    alNavegarAPerfilPrestador: (String, String?, String?) -> Unit,
    alNavegarAChat: (PrestadorDominio, String?, String?) -> Unit,
    viewModel: ResultadoBusquedaPrestadorViewModel = hiltViewModel(),
    brainViewModel: BeCerebroViewModel = hiltViewModel(),
    ubicacionObrero: UbicacionGpsObrero = hiltViewModel(),
    userViewModel: ArmadorUsuarioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resultados = viewModel.resultadosPaginados.collectAsLazyPagingItems()
    
    val accountState by userViewModel.ecosistemaMaestro.collectAsStateWithLifecycle()
    val idPerfilSeleccionado by brainViewModel.coordinador.idPerfilSeleccionado.collectAsStateWithLifecycle()
    val fotoPerfilActivo by userViewModel.fotoPerfilActiva.collectAsStateWithLifecycle()
    val nombrePerfilActivo by userViewModel.nombrePerfilActivo.collectAsStateWithLifecycle()

    val idDecodificado = remember(idCategoria) { Uri.decode(idCategoria) }

    BackHandler(enabled = uiState.mostrarMenuPerfil || uiState.mostrarMenuUbicacion || uiState.menuFiltrosAbierto != null) {
        viewModel.alternarMenuPerfil(false)
        viewModel.alternarMenuUbicacion(false)
        viewModel.establecerMenuFiltros(null)
    }

    val beConfig = remember { 
        com.example.myapplication.ui.componentes.be.modelos.ContextoHUD.RESULTADOS_BUSQUEDA.crearConfiguracionBase(
            mensajes = listOf(com.example.myapplication.ui.componentes.be.modelos.MensajeBe("🔍", "Buscando expertos...", null, Color(0xFF22D3EE))),
            pistaBusqueda = "BUSCA POR NOMBRE O EMPRESA..."
        )
    }

    // 🔥 [v2026.ELITE]: Soberanía Re-Claim al resumir pantalla
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("MAV_HUD", "♻️ [RESULTADOS] ON_RESUME: Re-reclamando soberanía HUD")
                brainViewModel.navCoordinador.registrarPantalla(beConfig)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            brainViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    // 🔥 [ELITE]: Actualización reactiva del mensaje de Be según el rubro cargado
    LaunchedEffect(uiState.rubroInfo) {
        if (uiState.rubroInfo != null) {
            brainViewModel.navCoordinador.actualizarContratoActual(
                beConfig.copy(
                    mensajes = listOf(com.example.myapplication.ui.componentes.be.modelos.MensajeBe("🔍", "Aquí tienes los expertos en ${uiState.rubroInfo?.nombre}.", null, Color(0xFF22D3EE)))
                )
            )
        }
    }

    LaunchedEffect(idDecodificado) {
        viewModel.establecerCategoria(idDecodificado)
        // 🔥 [v2026.ELITE]: Al entrar en resultados por rubro, apagamos el motor de búsqueda global
        // para limpiar el texto previo y cerrar el HUD de búsqueda de la Home.
        brainViewModel.beBusquedaMotor.establecerEstaBusquedaActiva(false)
    }

    ResultadoBusquedaPrestadorContent(
        resultados = resultados,
        categoriaEntity = uiState.rubroInfo?.let { com.example.myapplication.core.dominio.mapeadores.CategoriaMappers.deDominioAEntidad(it) },
        categoryName = uiState.rubroInfo?.nombre ?: idDecodificado,
        onRefresh = { resultados.refresh() },
        solo24h = uiState.filtros.solo24h,
        soloVerificados = uiState.filtros.soloVerificados,
        conEnvio = uiState.filtros.conEnvio,
        ordenActual = uiState.filtros.orden,
        usuario = accountState,
        nombrePerfilActivo = nombrePerfilActivo,
        fotoPerfilActivo = fotoPerfilActivo,
        direccionActiva = uiState.direccionSeleccionada,
        estaGpsActivado = uiState.estaGpsActivo,
        isCargandoUbicacion = uiState.isCargandoUbicacion,
        onBack = { 
            android.util.Log.d("MAV_NAV", "🔙 [RESULTADOS] Click en volver")
            alVolver() 
        },
        onNavigateToProviderProfile = alNavegarAPerfilPrestador,
        onNavigateToChat = { provider, cid, bid ->
            android.util.Log.d("MAV_NAV", "💬 [RESULTADOS] Navegando a Chat: ${provider.id}")
            alNavegarAChat(provider, cid, bid)
        },
        onGpsToggle = { ubicacionObrero.toggleGps(context) },
        alSeleccionarDireccion = { ubicacionObrero.seleccionarDireccion(it.id) },
        alSeleccionarPerfil = { userId, branchId ->
            if (branchId == null && userId == accountState?.usuario?.perfil?.id) userViewModel.seleccionarPerfil(null)
            else userViewModel.seleccionarPerfil(branchId ?: userId)
        },
        onFilterToggle = { viewModel.alternarFiltro(it) },
        onSortSelect = { viewModel.establecerOrden(it) },
        onClearFilters = {
            if (uiState.filtros.solo24h) viewModel.alternarFiltro("24h")
            if (uiState.filtros.soloVerificados) viewModel.alternarFiltro("verificado")
            if (uiState.filtros.conEnvio) viewModel.alternarFiltro("envio")
            viewModel.establecerOrden("reciente")
        },
        idPerfilActivo = idPerfilSeleccionado,
        mostrarMenuPerfil = uiState.mostrarMenuPerfil,
        mostrarMenuUbicacion = uiState.mostrarMenuUbicacion,
        menuFiltrosAbierto = uiState.menuFiltrosAbierto,
        onMenuToggle = { type, show ->
            when(type) {
                "profile" -> viewModel.alternarMenuPerfil(show)
                "location" -> viewModel.alternarMenuUbicacion(show)
                else -> viewModel.establecerMenuFiltros(if (show) type else null)
            }
        },
        onDismissMenus = {
            viewModel.alternarMenuPerfil(false)
            viewModel.alternarMenuUbicacion(false)
            viewModel.establecerMenuFiltros(null)
        },
        idsFavoritos = uiState.idsFavoritos,
        onManageShortcut = { id, add, label, icon -> viewModel.gestionarFavorito(id, add, label, icon) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadoBusquedaPrestadorContent(
    resultados: LazyPagingItems<PrestadorDominio>,
    categoriaEntity: CategoriaEntity?,
    categoryName: String,
    onRefresh: () -> Unit,
    solo24h: Boolean,
    soloVerificados: Boolean,
    conEnvio: Boolean,
    ordenActual: String,
    usuario: CuentaMaestroUsuario?,
    nombrePerfilActivo: String,
    fotoPerfilActivo: Any?,
    direccionActiva: DireccionDominio?,
    estaGpsActivado: Boolean,
    isCargandoUbicacion: Boolean = false,
    onBack: () -> Unit,
    onNavigateToProviderProfile: (String, String?, String?) -> Unit,
    onNavigateToChat: (PrestadorDominio, String?, String?) -> Unit,
    onGpsToggle: () -> Unit,
    alSeleccionarDireccion: (DireccionDominio) -> Unit,
    alSeleccionarPerfil: (String, String?) -> Unit,
    onFilterToggle: (String) -> Unit,
    onSortSelect: (String) -> Unit,
    onClearFilters: () -> Unit,
    idPerfilActivo: String? = null,
    mostrarMenuPerfil: Boolean,
    mostrarMenuUbicacion: Boolean,
    menuFiltrosAbierto: String?,
    onMenuToggle: (String, Boolean) -> Unit,
    onDismissMenus: () -> Unit,
    idsFavoritos: Set<String> = emptySet(),
    onManageShortcut: (String, Boolean, String?, String?) -> Unit = { _, _, _, _ -> }
) {
    val listState = rememberLazyGridState()
    val categoryColor = SharedPalette.ElectricCyan
    val isRefreshing = resultados.loadState.refresh is LoadState.Loading

    var scrollAccumulator by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newScroll = (scrollAccumulator - delta).coerceIn(0f, 330f)
                val consumed = scrollAccumulator - newScroll
                scrollAccumulator = newScroll
                return if (scrollAccumulator >= 330f && delta < 0) Offset.Zero else Offset(0f, consumed)
            }
        }
    }

    val collapseFraction = remember { derivedStateOf { (scrollAccumulator / 330f).coerceIn(0f, 1f) } }
    val contextHideFraction = remember { derivedStateOf { (scrollAccumulator / 80f).coerceIn(0f, 1f) } }

    Scaffold(
        containerColor = SharedPalette.V2TechSurface,
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            MoldeCabeceraSuperiorPantallas(
                fraccionColapso = collapseFraction.value,
                slotIzquierdo = { BotonBackCabeceraV3(onClick = onBack) },
                slotCentral = {
                    ColumnaTituloSeccionV3(
                        titulo = categoryName,
                        subtitulo = "RUBRO: ${categoriaEntity?.id ?: "BÚSQUEDA"}",
                        fraccionColapso = collapseFraction.value
                    )
                },
                slotDerecho = {
                    EmojiImpactoV3(
                        emoji = categoriaEntity?.icono ?: "🔍",
                        fraccionColapso = collapseFraction.value
                    )
                }
            )
        }
    ) { paddingValues ->
        val pullToRefreshState = rememberPullToRefreshState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // --- 1. TARJETA DE CONTEXTO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .graphicsLayer {
                        alpha = 1f - contextHideFraction.value
                        translationY = -10.dp.toPx() * contextHideFraction.value
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val h = (placeable.height * (1f - contextHideFraction.value)).toInt()
                        layout(placeable.width, h) { placeable.placeRelative(0, 0) }
                    }
            ) {
                MoldeTarjetaPerfilDirec(
                    usuario = usuario,
                    nombrePerfilActivo = nombrePerfilActivo,
                    fotoPerfilActivo = fotoPerfilActivo,
                    direccionActiva = direccionActiva,
                    estaGpsActivo = estaGpsActivado && direccionActiva?.id == "gps_current",
                    isCargandoUbicacion = isCargandoUbicacion,
                    alHacerClickPerfil = { onMenuToggle("profile", !mostrarMenuPerfil) },
                    alHacerClickUbicacion = { onMenuToggle("location", !mostrarMenuUbicacion) },
                    alAlternarGps = onGpsToggle,
                    alSeleccionarDireccion = alSeleccionarDireccion,
                    alSeleccionarPerfil = alSeleccionarPerfil,
                    mostrarMenuPerfil = mostrarMenuPerfil,
                    mostrarMenuUbicacion = mostrarMenuUbicacion,
                    alOcultarMenu = onDismissMenus,
                    esBusquedaManual = false,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                )
            }

            // --- 2. BARRA DE FILTROS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = 1f - contextHideFraction.value
                        translationY = -5.dp.toPx() * contextHideFraction.value
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val h = (placeable.height * (1f - contextHideFraction.value)).toInt()
                        layout(placeable.width, h) { placeable.placeRelative(0, 0) }
                    }
            ) {
                BarraFiltrosV3(
                    idsFiltrosSeleccionados = run {
                        val set = mutableSetOf<String>()
                        if (solo24h) set.add("24h")
                        if (soloVerificados) set.add("verificado")
                        if (conEnvio) set.add("envio")
                        if (ordenActual != "reciente") set.add(ordenActual)
                        set
                    },
                    alHacerClickMenu = { type: String -> onMenuToggle(type, menuFiltrosAbierto != type) },
                    alAlternarFiltro = onFilterToggle,
                    alEliminarFiltro = { onFilterToggle(it) },
                    alLimpiarTodo = onClearFilters,
                    mostrarMenuFiltros = menuFiltrosAbierto == "filtros",
                    mostrarMenuOrdenar = menuFiltrosAbierto == "ordenar",
                    alCerrarMenu = onDismissMenus,
                    filtrosActivos = run {
                        val list = mutableListOf<ModeloBurbujaFiltro>()
                        if (solo24h) list.add(ModeloBurbujaFiltro("24h", "24h", "🕒"))
                        if (soloVerificados) list.add(ModeloBurbujaFiltro("verificado", "Verificado", "✅"))
                        if (conEnvio) list.add(ModeloBurbujaFiltro("envio", "Envío", "🚚"))
                        list.toList()
                    },
                    mostrarCategorias = false,
                    estaCentrado = true,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // --- 3. LISTA DE RESULTADOS (PULL TO REFRESH INTEGRADO) ---
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                indicator = { /* [LEY #12]: Feedback delegado a ToastLogs de Be */ },
                modifier = Modifier.weight(1f)
            ) {
                // 🔥 [ELITE v2026]: Implementación del Nuevo ArmadorGridPantallaCompleta
                com.example.myapplication.ui.componentes.sistema.lista.ArmadorGridPantallaCompleta(
                    modifier = Modifier.fillMaxSize(),
                    titulo = "RESULTADOS EN TU ZONA",
                    subtitulo = "Descubrimiento",
                    cantidadItems = if (isRefreshing && resultados.itemCount == 0) null else resultados.itemCount,
                    colorAcento = categoryColor,
                    columnas = GridCells.Fixed(2),
                    estadoGrid = listState
                ) {
                    if (isRefreshing && resultados.itemCount == 0) {
                        // MODO CARGA INICIAL: Skeletons dentro del grid
                        items(6) {
                            com.example.myapplication.ui.componentes.sistema.ShimmerPrestadorBusinessCard()
                        }
                    } else if (resultados.itemCount == 0 && !isRefreshing) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No se encontraron expertos con estos filtros.", color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(
                            count = resultados.itemCount,
                            key = resultados.itemKey { it.id }
                        ) { index ->
                            resultados[index]?.let { item ->
                                val esFavorito = idsFavoritos.contains(item.id)
                                PrestadorBusinessCard(
                                    provider = item,
                                    user = usuario,
                                    onAvatarClick = { onNavigateToProviderProfile(item.id, item.idEmpresa, if (item.tipo == TipoPrestador.SUCURSAL) item.id else null) },
                                    onChatClick = { sender ->
                                        val clientCompanyId = if (sender != null && sender.id != "personal") sender.id else null
                                        val clientBranchId = if (sender != null && sender.id != "personal") sender.branchId else null
                                        onNavigateToChat(item, clientCompanyId, clientBranchId)
                                    },
                                    accentColor = categoryColor,
                                    idPerfilActivo = idPerfilActivo,
                                    isShortcut = esFavorito,
                                    onManageShortcut = { agregar ->
                                        onManageShortcut(item.id, agregar, item.titulo, item.urlMiniatura?.toString())
                                    },
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

// ==================================================================================
// --- 🎨 PREVIEW ELITE ---
// ==================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun PreviewResultadoBusquedaPrestador() {
    val mockCategoria = CategoriaEntity(
        id = "SALUD_PEDIATRA",
        nombre = "Pediatría",
        icono = "👶",
        idSuperCategoria = "SALUD",
        descripcion = "Médicos especialistas en el cuidado de niños."
    )

    val mockDireccion = DireccionDominio(
        id = "1",
        calle = "Av. Principal",
        numero = "100",
        localidad = "Ciudad PBEM",
        etiqueta = "Hogar"
    )

    val mockUsuario = CuentaMaestroUsuario(
        cuenta = CuentaEntity(id = "user123", correoGoogle = "user@gmail.com"),
        usuario = UsuarioDominioCompleto(
            perfil = UsuarioDominio(id = "user123", nombreVisible = "Max"),
            direcciones = emptyList()
        )
    )

    val mockResultados = flowOf(
        PagingData.from(
            listOf(
                PrestadorPerfilMocks.elenaRodriguez,
                PrestadorPerfilMocks.empresaTech.copy(titulo = "Consultorio Infantil")
            )
        )
    ).collectAsLazyPagingItems()

    ClienteTheme(darkTheme = true) {
        ResultadoBusquedaPrestadorContent(
            resultados = mockResultados,
            categoriaEntity = mockCategoria,
            categoryName = "Pediatría",
            onRefresh = {},
            solo24h = false,
            soloVerificados = true,
            conEnvio = false,
            ordenActual = "reciente",
            usuario = mockUsuario,
            nombrePerfilActivo = "Max",
            fotoPerfilActivo = null,
            direccionActiva = mockDireccion,
            estaGpsActivado = true,
            onBack = {},
            onNavigateToProviderProfile = { _, _, _ -> },
            onNavigateToChat = { _, _, _ -> },
            onGpsToggle = {},
            alSeleccionarDireccion = {},
            alSeleccionarPerfil = { _, _ -> },
            onFilterToggle = {},
            onSortSelect = {},
            onClearFilters = {},
            idPerfilActivo = null,
            mostrarMenuPerfil = false,
            mostrarMenuUbicacion = false,
            menuFiltrosAbierto = null,
            onMenuToggle = { _, _ -> },
            onDismissMenus = {},
            idsFavoritos = emptySet(),
            onManageShortcut = { _, _, _, _ -> }
        )
    }
}



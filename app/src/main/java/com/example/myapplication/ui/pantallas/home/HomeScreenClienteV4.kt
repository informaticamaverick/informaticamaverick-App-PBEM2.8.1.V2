package com.example.myapplication.ui.pantallas.home

import android.util.Log
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.componentes.be.modelos.BeZIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorHome
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPerfil
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorUbicacion
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorClima
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuClimaV3
import com.example.myapplication.ui.componentes.sistema.menu.v3.MenuUbicacionV3
import com.example.myapplication.core.dominio.modelos.*
import com.example.myapplication.ui.componentes.sistema.lista.ArmadorListaPantallaCompleta
import com.example.myapplication.ui.componentes.sistema.lista.BotonAccionV3
import com.example.myapplication.ui.componentes.*
import com.example.myapplication.ui.componentes.sistema.*
import com.example.myapplication.uishared.ui.components.CarruselPromocionesV3
import com.example.myapplication.uishared.ui.modelos.AccordionBanner
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.viewmodel.home.HomeScreenUiState

/**
 * --- HOME SCREEN CLIENTE V4 (v2026.ELITE) ---
 * [PROPÓSITO]: La representación visual "tonta" de la pantalla de inicio.
 * [LEY #1]: Pantalla Tonta. Consume un objeto de estado atómico (HomeScreenUiState).
 * [LEY #9]: Estándar Mav en Español.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenClienteV4(
    navController: NavHostController,
    state: HomeScreenUiState,
    alRefrescar: () -> Unit,
    alSeleccionarSuperCategoria: (SuperCategoriaDominio?) -> Unit,
    alAlternarFavoritoSuperCategoria: (String) -> Unit,
    alAlternarFavoritoCategoria: (CategoriaDominio) -> Unit,
    alEstablecerVisibilidadDetallesClima: (Boolean) -> Unit,
    alAlternarGps: () -> Unit,
    alHacerClickClima: () -> Unit,
    alSeleccionarUbicacion: (DireccionDominio) -> Unit,
    alEstablecerVisibilidadMenuUbicacion: (Boolean) -> Unit,
    alGestionarAccesoDirecto: (id: String, tipo: String, agregar: Boolean, etiqueta: String?, icono: String?) -> Unit,
    alAlternarMenu: (Boolean) -> Unit, 
    alAlternarSeleccionItem: (String) -> Unit,
    alAlternarFiltroOrden: (String) -> Unit, 
    alHacerClickInfoCategoria: (CategoriaDominio?) -> Unit, 
    alHacerClickCategoria: (String) -> Unit, 
    alCerrarBusqueda: () -> Unit,
    alCerrarFavoritos: () -> Unit,
    panelDetalles: @Composable () -> Unit,
    panelFavoritos: @Composable () -> Unit,
    panelMenu: @Composable () -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val estadoScroll = rememberLazyListState()
    val estaHaciendoScroll by remember { derivedStateOf { estadoScroll.isScrollInProgress } }

    val HUDActivo = state.estaBuscando || state.categoriaState.estaHojaVisible

    // 🔥 [v2026.ELITE]: Manejo soberano del botón Atrás para cerrar búsqueda
    androidx.activity.compose.BackHandler(enabled = state.estaBuscando) {
        alCerrarBusqueda()
    }

    val ocultarCarrusel = state.estaBuscando && !state.categoriaState.estaHojaVisible

    Scaffold(
        containerColor = SharedPalette.V2TechSurface 
    ) { paddingValues ->
        val dummy = paddingValues // Para evitar error de parámetro no usado
        Box(modifier = Modifier.fillMaxSize()) {
            // --- CAPA 1: CONTENIDO PRINCIPAL ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                PullToRefreshBox(
                    isRefreshing = state.estaRefrescando,
                    onRefresh = alRefrescar,
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MoldeCabeceraSuperiorHome(
                            slotIzquierdo = {
                                MoldeCabeceraSuperiorPerfil(
                                    nombre = state.nombrePerfilActivo,
                                    foto = state.fotoPerfilActivo,
                                    esPersonal = state.idPerfilSeleccionado == null,
                                    estaVerificado = state.estaVerificado,
                                    esSuscripto = state.esSuscripto,
                                    conteoNoLeidos = state.conteoNoLeidos,
                                    onClick = { alAlternarMenu(true) }
                                )
                            },
                            slotCentral = {
                                Box {
                                    MoldeCabeceraSuperiorUbicacion(
                                        direccion = state.direccionActiva,
                                        onClick = { alEstablecerVisibilidadMenuUbicacion(true) }
                                    )

                                    MenuUbicacionV3(
                                        expanded = state.mostrarMenuUbicacion,
                                        onDismissRequest = { alEstablecerVisibilidadMenuUbicacion(false) },
                                        direccionActiva = state.direccionActiva,
                                        direccionGpsActual = null, 
                                        estaGpsActivo = state.estaGpsActivado,
                                        isCargando = state.estaCargandoUbicacion, 
                                        direccionesDisponibles = state.direccionesDisponibles,
                                        alAlternarGps = alAlternarGps,
                                        alSeleccionarDireccion = { 
                                            alSeleccionarUbicacion(it)
                                            alEstablecerVisibilidadMenuUbicacion(false)
                                        },
                                        alignment = Alignment.BottomCenter, 
                                        isCenteredOnScreen = true, 
                                        verticalOffset = (-10).dp 
                                    )
                                }
                            },
                            slotDerecho = {
                                Box {
                                    MoldeCabeceraSuperiorClima(
                                        temperatura = state.temperatura,
                                        emoji = state.emojiClima,
                                        descripcion = state.descripcionClima,
                                        onClick = alHacerClickClima
                                    )

                                    val mensajeContexto = remember(state.temperatura, state.descripcionClima) {
                                        val valorTemp = state.temperatura.replace("°C", "").trim().toIntOrNull() ?: 20
                                        val estaLloviendo = state.descripcionClima.contains("lluvia", ignoreCase = true) || state.descripcionClima.contains("tormenta", ignoreCase = true)

                                        val hora = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

                                        when {
                                            estaLloviendo -> "¡No olvides el paraguas! ☔"
                                            valorTemp > 30 -> "Día caluroso, ¡hidrátate! 🥤"
                                            valorTemp < 15 -> "Está fresco, ¡abrígate! 🧣"
                                            hora in 6..11 -> "¡Buenos días! ☀️"
                                            hora in 12..18 -> "¡Buenas tardes! ☕"
                                            else -> "¡Buenas noches! 🌙"
                                        }
                                    }

                                    MenuClimaV3(
                                        expanded = state.mostrarDetallesClima,
                                        onDismissRequest = { alEstablecerVisibilidadDetallesClima(false) },
                                        temperatura = state.temperatura,
                                        emoji = state.emojiClima,
                                        descripcion = state.descripcionClima,
                                        nombreCiudad = state.nombreCiudad,
                                        mensajeContexto = mensajeContexto
                                    )
                                }
                            }
                        )

                        if (!ocultarCarrusel) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(
                                        animationSpec = spring(
                                            stiffness = Spring.StiffnessLow,
                                            dampingRatio = Spring.DampingRatioLowBouncy
                                        )
                                    )
                            ) {
                                if (state.itemsBanner.isEmpty()) {
                                    BannerCarouselSkeleton()
                                } else {
                                    CarruselPromocionesV3(
                                        items = state.itemsBanner,
                                        isPaused = estaHaciendoScroll || state.mostrarDetallesClima,
                                        onItemClick = { banner ->
                                            val pid = banner.providerId
                                            if (pid != null) {
                                                navController.navigate(Screen.PerfilPrestador.createRoute(pid))
                                            } else {
                                                banner.originalCategory?.let {
                                                    navController.navigate(Screen.ResultBusqueda.createRoute(it.id))
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }

                        ArmadorListaPantallaCompleta(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            titulo = if (HUDActivo && state.consultaBusqueda.isNotEmpty() && state.categoriaState.categoriasPlanas.isNotEmpty()) "Encontré estos servicios" else "Busca y explora servicios",
                            subtitulo = "Módulo de Exploración",
                            estadoLista = estadoScroll,
                            colorContenedor = SharedPalette.EliteSurface,
                            acciones = { proveedorColapso ->
                                var showMenu by remember { mutableStateOf(false) }
                                Box {
                                    BotonAccionV3(
                                        alHacerClick = { showMenu = !showMenu },
                                        icono = if (showMenu) Icons.Rounded.Close else Icons.Rounded.Menu,
                                        colorIcono = if (showMenu) SharedPalette.DeepRed else Color.White,
                                        proveedorColapso = proveedorColapso
                                    )

                                    com.example.myapplication.ui.componentes.sistema.menu.v3.MoldeMenuArmadorV3(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        alignment = Alignment.TopEnd,
                                        verticalOffset = 22.dp,
                                        horizontalOffset = 42.dp, 
                                        arrowOffset = 240.dp 
                                    ) {
                                        com.example.myapplication.ui.componentes.sistema.menu.v3.MenuGrupoV3 {
                                            com.example.myapplication.ui.componentes.sistema.menu.v3.MenuSectionHeaderV3(text = "ORDENAR POR")
                                            val itemsOrden = listOf(
                                                DropdownItemData(id = "sort_favorites", label = "Favoritos Primero", emoji = "⭐"),
                                                DropdownItemData(id = "sort_alpha_asc", label = "Nombre A - Z", emoji = "🔤"),
                                                DropdownItemData(id = "sort_alpha_desc", label = "Nombre Z - A", emoji = "🔡")
                                            )
                                            com.example.myapplication.ui.componentes.sistema.menu.v3.MenuOrdenContenido(
                                                items = itemsOrden,
                                                idsSeleccionados = state.categoriaState.filtrosOrden,
                                                alAlternar = { 
                                                    alAlternarFiltroOrden(it)
                                                    showMenu = false 
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { _ ->
                            val mostrarSuperCategorias = (state.categoriaState.superCategoriaSeleccionada == null && !HUDActivo) || (HUDActivo && state.consultaBusqueda.isEmpty())
                            val columnas = if (state.categoriaState.filtrosOrden.contains("view_bento") && state.consultaBusqueda.isEmpty()) 2 else 3

                            Log.v("HOME_AUDIT", "🎨 [RENDER_LIST] Super: $mostrarSuperCategorias | HUD: $HUDActivo | Query: '${state.consultaBusqueda}' | Rows: ${if (mostrarSuperCategorias) state.categoriaState.superCategoriasFiltradas.size else state.categoriaState.categoriasFiltradas.size}")

                            if (state.categoriaState.estaCargando || (HUDActivo && state.consultaBusqueda.isNotEmpty() && !state.animacionBusquedaFinalizada && state.categoriaState.categoriasPlanas.isEmpty())) {
                                items(4) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        repeat(columnas) { Box(modifier = Modifier.weight(1f)) { if (mostrarSuperCategorias) ShimmerTarjetaSuperCategoria() else ShimmerTarjetaCategoria() } }
                                    }
                                }
                            } else if (mostrarSuperCategorias) {
                                items(state.categoriaState.superCategoriasFiltradas) { fila ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                        fila.elementos.forEach { superCat ->
                                            Box(modifier = Modifier.weight(1f).padding(horizontal = 2.dp, vertical = 3.dp)) {
                                                BentoSuperCategoryCard(
                                                    superCategory = superCat,
                                                    emoji = superCat.icono,
                                                    height = 130.dp,
                                                    onClick = { 
                                                        if (state.modoMultiseleccion) alAlternarSeleccionItem(superCat.id)
                                                        else alSeleccionarSuperCategoria(superCat) 
                                                    },
                                                    onToggleFavorite = { alAlternarFavoritoSuperCategoria(superCat.id) },
                                                    isShortcut = state.categoriaState.idsFavoritos.contains(superCat.id),
                                                    onManageShortcut = { agregar, etiqueta, icono -> alGestionarAccesoDirecto(superCat.id, "supercategory", agregar, etiqueta, icono) },
                                                    estaSeleccionado = state.idsSeleccionados.contains(superCat.id),
                                                    modoMultiseleccionActivo = state.modoMultiseleccion,
                                                    alHacerLongClick = { alAlternarSeleccionItem(superCat.id) }
                                                )
                                            }
                                        }
                                        repeat(columnas - fila.elementos.size) { Spacer(modifier = Modifier.weight(1f)) }
                                    }
                                }
                            } else {
                                items(state.categoriaState.categoriasFiltradas) { fila ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                                        fila.elementos.forEach { category ->
                                            Box(modifier = Modifier.weight(1f).padding(vertical = 5.dp, horizontal = 2.dp)) {
                                                CompactCategoryCard(
                                                    item = category,
                                                    onClick = {
                                                        if (state.modoMultiseleccion) alAlternarSeleccionItem(category.id)
                                                        else alHacerClickCategoria(category.id)
                                                    },
                                                    onToggleFavorite = { alAlternarFavoritoCategoria(category) },
                                                    onInfoClick = { alHacerClickInfoCategoria(it) }, 
                                                    isInfoVisible = state.categoriaState.categoriaParaDetalle?.id == category.id,
                                                    onDismissInfo = { alHacerClickInfoCategoria(null) },
                                                    isShortcut = state.categoriaState.idsFavoritos.contains(category.id),
                                                    onManageShortcut = { agregar, etiqueta, icono -> alGestionarAccesoDirecto(category.id, "category", agregar, etiqueta, icono) },
                                                    showSuperCategoryLabel = HUDActivo && state.consultaBusqueda.isNotEmpty(),
                                                    isSuperCategoryFavorite = state.categoriaState.idsFavoritos.contains(category.idSuperCategoria),
                                                    estaSeleccionado = state.idsSeleccionados.contains(category.id),
                                                    modoMultiseleccionActivo = state.modoMultiseleccion,
                                                    alHacerLongClick = { alAlternarSeleccionItem(category.id) }
                                                )
                                            }
                                        }
                                        repeat(columnas - fila.elementos.size) { Spacer(modifier = Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- CAPA 2: OVERLAYS Y SHEETS (FULL SCREEN / TRANSPARENT) ---
            // 🔥 [v2026.ELITE]: Scrim para Favoritos (Cierre al tocar afuera)
            AnimatedVisibility(
                visible = state.estaPanelFavoritosAbierto,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.zIndex(BeZIndex.PANELES + 700f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { alCerrarFavoritos() }
                )
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd).zIndex(BeZIndex.PANELES + 800f)) { panelFavoritos() }
            Box(modifier = Modifier.align(Alignment.BottomCenter).zIndex(BeZIndex.PANELES)) { panelDetalles() }

            // 🔥 [v2026.ELITE]: Scrim para Menú Lateral con Animación
            AnimatedVisibility(
                visible = state.estaMenuLateralAbierto,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)),
                modifier = Modifier.zIndex(BeZIndex.PANELES + 500f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { alAlternarMenu(false) }
                )
            }

            Box(modifier = Modifier.fillMaxSize().zIndex(BeZIndex.PANELES + 600f)) {
                AnimatedVisibility(
                    visible = state.estaMenuLateralAbierto,
                    enter = slideInHorizontally(animationSpec = tween(300), initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(animationSpec = tween(300), targetOffsetX = { -it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) { panelMenu() }
            }
        }
    }
}

@Composable
fun BannerCarouselSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, 
        targetValue = 0.6f, 
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing), 
            repeatMode = RepeatMode.Reverse
        ), 
        label = "alpha"
    )
    Surface(
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 16.dp), 
        color = SharedPalette.V2TechSurface.copy(alpha = 0.5f), 
        shape = RoundedCornerShape(16.dp), 
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent, Color.White.copy(alpha = 0.1f)))))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(name = "Home Screen V4", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewHomeScreenClienteV4() {
    PBEMTheme {
        HomeScreenClienteV4(
            navController = rememberNavController(),
            state = HomeScreenUiState(),
            alRefrescar = {},
            alSeleccionarSuperCategoria = {},
            alAlternarFavoritoSuperCategoria = {},
            alAlternarFavoritoCategoria = {},
            alEstablecerVisibilidadDetallesClima = {},
            alAlternarGps = {},
            alHacerClickClima = {},
            alSeleccionarUbicacion = {},
            alEstablecerVisibilidadMenuUbicacion = {},
            alGestionarAccesoDirecto = { _, _, _, _, _ -> },
            alAlternarMenu = {},
            alAlternarSeleccionItem = {},
            alAlternarFiltroOrden = {}, 
            alHacerClickInfoCategoria = {}, 
            alHacerClickCategoria = {},
            alCerrarBusqueda = {},
            alCerrarFavoritos = {},
            panelDetalles = {},
            panelFavoritos = {},
            panelMenu = {}
        )
    }
}

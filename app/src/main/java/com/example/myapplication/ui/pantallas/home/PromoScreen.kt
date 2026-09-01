package com.example.myapplication.ui.pantallas.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import com.example.myapplication.ui.componentes.sistema.cabecera.BotonBackCabeceraV3
import com.example.myapplication.ui.componentes.sistema.cabecera.ColumnaTituloSeccionV3
import com.example.myapplication.ui.componentes.sistema.cabecera.EmojiImpactoV3
import com.example.myapplication.ui.componentes.sistema.cabecera.MoldeCabeceraSuperiorPantallas
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.core.dominio.modelos.PromocionDominio
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.ui.componentes.sistema.contexto.BarraFiltrosV3
import com.example.myapplication.ui.componentes.sistema.contexto.ModeloBurbujaFiltro
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.InstagramPromoCard
import com.example.myapplication.uishared.ui.components.InstagramPromoSkeleton
import com.example.myapplication.uishared.ui.components.InstagramStoriesRow
import com.example.myapplication.uishared.ui.components.PromoCommentsSheet
import com.example.myapplication.uishared.ui.components.InstagramNativeAdCard
import com.example.myapplication.viewmodel.home.PromoItem
import com.example.myapplication.viewmodel.home.PromoViewModel

/**
 * --- PROMO SCREEN (ELITE v2026.OPTIMIZED) ---
 * [PROPÓSITO]: Implementar el descubrimiento de ofertas con filtros soberanos.
 * [LEY #12]: Soberanía por Contrato. Be está en modo pasivo.
 */
@Composable
fun PromoScreen(
    navController: NavController,
    beViewModel: BeCerebroViewModel,
    viewModel: PromoViewModel = hiltViewModel(),
) {
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val pagingItems = viewModel.feedPagingData.collectAsLazyPagingItems()

    val filtrosActivos by viewModel.filtrosActivos.collectAsStateWithLifecycle()
    val itemsFiltro by viewModel.itemsFiltro.collectAsStateWithLifecycle()
    val itemsOrden by viewModel.itemsOrden.collectAsStateWithLifecycle()
    val itemsCategoria by viewModel.itemsCategoria.collectAsStateWithLifecycle()

    val currentUserPhoto by viewModel.currentUserPhoto.collectAsStateWithLifecycle()

    var idPromoMostrarComentarios by remember { mutableStateOf<String?>(null) }
    var menuFiltrosAbierto by remember { mutableStateOf<String?>(null) }

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
    val filterHideFraction = remember { derivedStateOf { (scrollAccumulator / 80f).coerceIn(0f, 1f) } }

    val beConfig = remember { 
        ContextoHUD.PROMO.crearConfiguracionBase().copy(
            ocultarOjos = true
        )
    }

    DisposableEffect(Unit) {
        beViewModel.navCoordinador.reiniciarContextoHUD(ContextoHUD.PROMO)
        beViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose { 
            beViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    Scaffold(
        containerColor = SharedPalette.DarkBg,
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            MoldeCabeceraSuperiorPantallas(
                fraccionColapso = collapseFraction.value,
                slotIzquierdo = { BotonBackCabeceraV3(onClick = { navController.popBackStack() }) },
                slotCentral = {
                    ColumnaTituloSeccionV3(
                        titulo = "Descubrir",
                        subtitulo = "Ofertas en tu Zona",
                        fraccionColapso = collapseFraction.value
                    )
                },
                slotDerecho = {
                    EmojiImpactoV3(
                        emoji = "🔥",
                        fraccionColapso = collapseFraction.value
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            Box(modifier = Modifier.fillMaxWidth().animateContentSize().graphicsLayer {
                alpha = 1f - filterHideFraction.value
                translationY = -10.dp.toPx() * filterHideFraction.value
            }.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val h = (placeable.height * (1f - filterHideFraction.value)).toInt()
                layout(placeable.width, h) { placeable.placeRelative(0, 0) }
            }) {
                val filtrosActivosVisuales = remember(filtrosActivos, itemsFiltro, itemsOrden, itemsCategoria) {
                    filtrosActivos.mapNotNull { id ->
                        val item = itemsFiltro.find { it.id == id } 
                            ?: itemsOrden.find { it.id == id }
                            ?: itemsCategoria.find { it.id == id }
                        item?.let { ModeloBurbujaFiltro(it.id, it.label, it.emoji ?: "✨") }
                    }
                }

                BarraFiltrosV3(
                    filtrosActivos = filtrosActivosVisuales,
                    alHacerClickMenu = { tipo: String -> menuFiltrosAbierto = if (menuFiltrosAbierto == tipo) null else tipo },
                    alEliminarFiltro = { id -> viewModel.alternarFiltro(id) },
                    alLimpiarTodo = { viewModel.alternarFiltro("CLEAR_ALL") },
                    mostrarMenuFiltros = menuFiltrosAbierto == "filtros",
                    mostrarMenuOrdenar = menuFiltrosAbierto == "ordenar",
                    mostrarMenuCategorias = menuFiltrosAbierto == "categorias",
                    idsFiltrosSeleccionados = filtrosActivos,
                    alAlternarFiltro = { id -> viewModel.alternarFiltro(id) },
                    alCerrarMenu = { menuFiltrosAbierto = null },
                    itemsCategoria = itemsCategoria,
                    itemsFiltro = itemsFiltro,
                    itemsOrden = itemsOrden,
                    estaCentrado = true
                )
            }

            PromoScreenContent(
                modifier = Modifier.weight(1f),
                historias = stories,
                itemsPaginadosFeed = pagingItems,
                navController = navController,
                alDarLike = { viewModel.toggleLike(it) },
                alHacerClickComentario = { idPromoMostrarComentarios = it }
            )
        }

        if (idPromoMostrarComentarios != null) {
            val comentarios by viewModel.getComments(idPromoMostrarComentarios!!).collectAsStateWithLifecycle(initialValue = emptyList())
            PromoCommentsSheet(
                onDismiss = { idPromoMostrarComentarios = null },
                comments = comentarios,
                onSendComment = { texto -> viewModel.addComment(idPromoMostrarComentarios!!, texto) },
                currentUserPhoto = currentUserPhoto
            )
        }
    }
}

@Composable
fun PromoScreenContent(
    modifier: Modifier = Modifier,
    historias: List<PromocionDominio>,
    itemsPaginadosFeed: androidx.paging.compose.LazyPagingItems<PromoItem>,
    navController: NavController,
    alDarLike: (String) -> Unit,
    alHacerClickComentario: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (historias.isNotEmpty()) {
            item {
                InstagramStoriesRow(
                    stories = historias,
                    onStoryClick = { /* Ver Historia */ }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        }

        items(itemsPaginadosFeed.itemCount) { index ->
            val item = itemsPaginadosFeed[index]
            if (item != null) {
                key(when (item) {
                    is PromoItem.RealPromo -> "promo_${item.promotion.id}"
                    is PromoItem.GoogleAd -> "ad_${item.id}"
                    is PromoItem.GoogleNativeAd -> "native_ad_${item.nativeAd.hashCode()}"
                }) {
                    when (item) {
                        is PromoItem.RealPromo -> {
                            InstagramPromoCard(
                                promotion = item.promotion,
                                onLike = { alDarLike(item.promotion.id) },
                                onCommentClick = { alHacerClickComentario(item.promotion.id) },
                                onProviderClick = { navController.navigate("perfil_prestador/${item.promotion.idPrestador}") },
                                onContactClick = { 
                                    navController.navigate("chat?providerId=${item.promotion.idPrestador}&promoId=${item.promotion.id}") 
                                }
                            )
                        }
                        is PromoItem.GoogleNativeAd -> {
                            InstagramNativeAdCard(nativeAd = item.nativeAd)
                        }
                        is PromoItem.GoogleAd -> {
                            item.nativeAd?.let { InstagramNativeAdCard(nativeAd = it) }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (itemsPaginadosFeed.loadState.append is androidx.paging.LoadState.Loading) {
            items(3) { InstagramPromoSkeleton() }
        }
    }
}

@Composable
fun PromoEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Campaign,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        androidx.compose.material3.Text("Sin promociones", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
        androidx.compose.material3.Text(
            "Es posible que no existan promociones activas para la zona o filtros seleccionados.",
            color = Color.Gray, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun PromoScreenPreview() {
    val mockPromos = listOf<PromoItem>(
        PromoItem.RealPromo(
            PromocionDominio(
                id = "1", idPrestador = "p1", titulo = "Oferta Limpieza",
                descripcion = "50% de descuento en la primera limpieza de hogar.",
                urlImagen = null, nombrePrestador = "Limpiezas Maverick",
                urlMiniaturaPrestador = null, reputacion = 4.5f, estaVerificado = true,
                tiempoRelativo = "Expira en 2h", etiquetaOferta = "50% OFF",
                esHistoria = false, leGustaAlUsuario = true, conteoLikes = 120, esNuevo = true
            )
        )
    )

    val pagingData = flowOf(PagingData.from(mockPromos)).collectAsLazyPagingItems()

    ClienteTheme(darkTheme = true) {
        PromoScreenContent(
            historias = emptyList(),
            itemsPaginadosFeed = pagingData,
            navController = androidx.navigation.compose.rememberNavController(),
            alDarLike = {},
            alHacerClickComentario = {}
        )
    }
}

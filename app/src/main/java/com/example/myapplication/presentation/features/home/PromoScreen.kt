package com.example.myapplication.presentation.features.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.data.model.ProviderDisplayModel
import com.example.myapplication.presentation.components.*
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.features.profile.ProviderViewModel
import com.example.myapplication.presentation.global.BeBrainViewModel
import kotlin.random.Random

// ==========================================================================================
// --- CONSTANTES VISUALES MAVERICK PRO ULTRA ---
// ==========================================================================================
private val DarkBg = Color(0xFF020408)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = MaverickColors.ElectricCyan // Unificado con Sistema Maverick Elite
private val DiscountRed = Color(0xFFE91E63)
private val AdYellow = Color(0xFFFFC107)

// ==========================================================================================
// --- SECCIÓN 1: ENUMS Y MODELOS DE DATOS ---
// ==========================================================================================

enum class PromoType(val label: String, val icon: String, val color: Color) {
    PRODUCT("PRODUCTO", "🛍️", Color(0xFFF59E0B)),
    SERVICE("SERVICIO", "🛠️", Color(0xFF3B82F6))
}

enum class PromoTag(val label: String) {
    HOT_SALE("HOT SALE"),
    TWO_FOR_ONE("2x1"),
    FREE_SHIPPING("ENVÍO GRATIS"),
    INSTALLMENTS("CUOTAS SIN INTERÉS"),
    NEW_ARRIVAL("NUEVO INGRESO")
}

data class Promotion(
    val id: String,
    val type: PromoType,
    val tag: PromoTag?,
    val imageUrls: List<String>,
    val providerImageUrl: Any?,
    val providerName: String,
    val description: String,
    val providerId: String,
    val categories: List<String>,
    val rating: Float,
    val likes: Int,
    var isLiked: Boolean,
    val discount: Int? = null
)

data class ProviderPromotions(
    val service: ProviderDisplayModel, // 🔥 [UNIFICADO]: Usamos el modelo global
    val promotions: List<Promotion>
)

sealed interface PromoListItem {
    data class ProviderPromoItem(val providerPromotions: ProviderPromotions) : PromoListItem
    data class AdItem(
        val id: String,
        val title: String,
        val description: String,
        val imageUrl: String,
        val cta: String
    ) : PromoListItem
}

// ==========================================================================================
// --- SECCIÓN 2: PANTALLA PRINCIPAL (STATEFUL ORCHESTRATOR) ---
// ==========================================================================================

@Composable
fun PromoScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: ProviderViewModel = hiltViewModel(),
    beViewModel: BeBrainViewModel = hiltViewModel(),
    bottomPadding: PaddingValues = PaddingValues(bottom = 80.dp)
) {
    // --- SINCRONIZACIÓN DE CONTEXTO (Elite SSOT) ---
    // Notificamos al Cerebro sobre el cambio de ruta para que Be Assistant actualice su comportamiento.
    LaunchedEffect(Unit) {
        beViewModel.onRouteChanged("promo")
    }

    // --- SUSCRIPCIÓN A LOS OBREROS (SSOT) ---
    // Consumimos el flujo unificado de servicios filtrados por la búsqueda global del asistente.
    //val unifiedServices by viewModel.unifiedServices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
/**
    PromoScreenContent(
       // services = unifiedServices,
        isLoading = isLoading,
        onBack = onBack,
        navController = navController,
        bottomPadding = bottomPadding,
        beViewModel = beViewModel
    )
    */
}

// ==========================================================================================
// --- SECCIÓN 3: CONTENIDO STATELESS (UI) ---
// ==========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoScreenContent(
    services: List<ProviderDisplayModel>,
    isLoading: Boolean,
    onBack: () -> Unit,
    navController: NavHostController,
    bottomPadding: PaddingValues,
    beViewModel: BeBrainViewModel
) {
    // --- ESTADOS GLOBALES (Sincronizados con Be Assistant) ---
    val searchQuery by beViewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilters by beViewModel.activeFilters.collectAsStateWithLifecycle()

    var viewedFavorites by remember { mutableStateOf(setOf<String>()) }
    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedAd by remember { mutableStateOf<PromoListItem.AdItem?>(null) }

    // --- 🏗️ SECCIÓN: LÓGICA DE ANIMACIÓN DE CABECERA (SCROLL) ---
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset.toFloat() / 250f).coerceIn(0f, 1f)
        }
    }

    // --- LÓGICA DE MAPEO (Servicios -> Promociones y Anuncios) ---
    val (listItems, _) = remember(services) {
        val providersWithPromos = mutableListOf<ProviderPromotions>()
        val categoriesSet = mutableSetOf<String>()

        // Solo prestadores suscritos aparecen en el feed de promociones
        val subscribedServices = services.filter { it.isSubscribed }.shuffled()

        subscribedServices.forEachIndexed { index, service ->
            categoriesSet.addAll(service.categories)

            val mockImages = if (index % 2 == 0) {
                listOf("https://picsum.photos/seed/${service.id}1/800/800", "https://picsum.photos/seed/${service.id}2/800/800")
            } else {
                listOf("https://picsum.photos/seed/${service.id}3/800/600")
            }

            val promoList = listOf(
                Promotion(
                    id = "promo_${service.id}",
                    type = if (Random.nextBoolean()) PromoType.PRODUCT else PromoType.SERVICE,
                    tag = if (Random.nextBoolean()) PromoTag.entries.random() else null,
                    imageUrls = mockImages,
                    providerImageUrl = service.photoUrl,
                    providerName = service.title,
                    description = "Aprovecha nuestras ofertas exclusivas en ${service.categories.firstOrNull() ?: "servicios"}.",
                    providerId = service.id,
                    rating = service.rating.toFloat(),
                    likes = (50..500).random(),
                    isLiked = service.isFavorite,
                    discount = if (index % 2 == 0) (10..50).random() else null,
                    categories = service.categories
                )
            )
            providersWithPromos.add(ProviderPromotions(service, promoList))
        }

        val finalItems = mutableListOf<PromoListItem>()
        val adTemplates = listOf(
            PromoListItem.AdItem("ad1", "Seguros Pro", "Asegura tu equipo de trabajo.", "https://picsum.photos/seed/ad1/800/600", "Cotizar"),
            PromoListItem.AdItem("ad2", "Herramientas", "30% OFF en toda la línea.", "https://picsum.photos/seed/ad2/800/600", "Ver"),
        )

        var adCounter = 0
        providersWithPromos.forEachIndexed { index, item ->
            finalItems.add(PromoListItem.ProviderPromoItem(item))
            if ((index + 1) % 3 == 0) {
                finalItems.add(adTemplates[adCounter % adTemplates.size].copy(id = "ad_${System.currentTimeMillis()}_$adCounter"))
                adCounter++
            }
        }

        val cats = categoriesSet.map { catName ->
            ControlItem(label = catName, icon = null, emoji = "🏷️", color = MaverickBlue, id = "cat_${catName.lowercase()}")
        }
        finalItems to cats
    }

    // Estado local para interacción reactiva con los likes de la promo
    val promosState = remember(listItems) {
        mutableStateMapOf<String, Promotion>().apply {
            listItems.forEach { if (it is PromoListItem.ProviderPromoItem) it.providerPromotions.promotions.forEach { p -> put(p.id, p) } }
        }
    }

    // --- LÓGICA DE FILTRADO (Categorías de la Burbuja) ---
    val filteredListItems = remember(activeFilters, searchQuery, listItems) {
        listItems.mapNotNull { item ->
            when (item) {
                is PromoListItem.AdItem -> item
                is PromoListItem.ProviderPromoItem -> {
                    val matchingPromos = item.providerPromotions.promotions.filter { promo ->
                        val selectedCats = activeFilters.filter { it.startsWith("cat_") }.map { it.removePrefix("cat_") }
                        val catMatch = selectedCats.isEmpty() || promo.categories.any { it.lowercase() in selectedCats }
                        // Nota: La búsqueda global ya filtra los 'services' que entran a este Composable,
                        // por lo que searchMatch aquí es un refuerzo o puede ser ignorado si confiamos en el Obrero.
                        val searchMatch = searchQuery.isEmpty() || promo.providerName.contains(searchQuery, ignoreCase = true)
                        catMatch && searchMatch
                    }
                    if (matchingPromos.isNotEmpty()) PromoListItem.ProviderPromoItem(item.providerPromotions.copy(promotions = matchingPromos)) else null
                }
            }
        }
    }

    val favoritePromotions = remember(listItems) {
        listItems.mapNotNull { if (it is PromoListItem.ProviderPromoItem && it.providerPromotions.service.isFavorite) it.providerPromotions else null }
    }

    // --- RENDERIZADO UI ---
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // ==========================================================================================
                // --- 🏗️ SECCIÓN: CABECERA DINÁMICA MAVERICK (REEMPLAZO DE TOPAPPBAR) ---
                // ==========================================================================================
                BarraCabezera(
                    title = "Ofertas",
                    subtitle = "Descuentos Maverick",
                    emoji = "🛍️",
                    onBack = onBack,
                    onInfoClick = { /* Diálogo de información */ },
                    collapseFraction = collapseFraction,
                    accentColor = MaverickBlue
                )
            }
        ) { paddingValues ->
            val layoutDirection = LocalLayoutDirection.current
            val safePadding = remember(paddingValues, layoutDirection) {
                PaddingValues(
                    start = paddingValues.calculateStartPadding(layoutDirection).coerceAtLeast(0.dp),
                    top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                    end = paddingValues.calculateEndPadding(layoutDirection).coerceAtLeast(0.dp),
                    bottom = paddingValues.calculateBottomPadding().coerceAtLeast(0.dp)
                )
            }
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaverickBlue) }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = safePadding.calculateTopPadding(),
                        bottom = bottomPadding.calculateBottomPadding() + safePadding.calculateBottomPadding() + 16.dp
                    )
                ) {
                    // 1. Stories de Favoritos
                    if (favoritePromotions.isNotEmpty()) {
                        item {
                            ProviderStoriesRow(
                                providers = favoritePromotions,
                                viewedProviderIds = viewedFavorites,
                                onStoryClick = { promo ->
                                    viewedFavorites = viewedFavorites + promo.service.id
                                    navController.navigate("chat?providerId=${promo.service.id}")
                                }
                            )
                        }
                    }

                    // 2. Feed de Promociones
                    items(filteredListItems, key = {
                        when (it) {
                            is PromoListItem.AdItem -> it.id
                            is PromoListItem.ProviderPromoItem -> it.providerPromotions.service.id
                        }
                    }) { item ->
                        when (item) {
                            is PromoListItem.AdItem -> AdBannerCard(item) { selectedAd = item }
                            is PromoListItem.ProviderPromoItem -> {
                                item.providerPromotions.promotions.forEach { promo ->
                                    PromoCard(
                                        promotion = promosState[promo.id] ?: promo,
                                        onLike = {
                                            val p = promosState[promo.id]
                                            if (p != null) {
                                                promosState[promo.id] = p.copy(
                                                    isLiked = !p.isLiked, 
                                                    likes = if (p.isLiked) p.likes - 1 else p.likes + 1
                                                )
                                            }
                                        },
                                        onImageClick = { fullscreenImageUrl = it },
                                        onContact = {
                                            navController.navigate("chat?providerId=${promo.providerId}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- OVERLAYS ---
        if (fullscreenImageUrl != null) {
            FullscreenImageDialog(fullscreenImageUrl!!) { fullscreenImageUrl = null }
        }

        if (selectedAd != null) {
            AdDetailDialog(selectedAd!!) { selectedAd = null }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 4: COMPONENTES DE TARJETA ---
// ==========================================================================================

@Composable
fun ProviderStoriesRow(
    providers: List<ProviderPromotions>,
    viewedProviderIds: Set<String>,
    onStoryClick: (ProviderPromotions) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text("Ofertas de tus Favoritos", color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(providers) { promo ->
                val isViewed = promo.service.id in viewedProviderIds
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp).clickable { onStoryClick(promo) }) {
                    val brush = if (isViewed) SolidColor(Color.Gray) else geminiGradientBrush()
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(brush).padding(3.dp)) {
                        AsyncImage(model = promo.service.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape).background(CardSurface))
                    }
                    Text(promo.service.title, color = if (isViewed) Color.Gray else Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun PromoCard(
    promotion: Promotion,
    onLike: () -> Unit,
    onImageClick: (String) -> Unit,
    onContact: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column {
            // Header del Proveedor
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = promotion.providerImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBg),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(promotion.providerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = AdYellow, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(promotion.rating.toString(), color = Color.Gray, fontSize = 12.sp)
                    }
                }
                promotion.tag?.let { tag ->
                    Surface(
                        color = MaverickBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaverickBlue.copy(alpha = 0.5f))
                    ) {
                        Text(tag.label, color = MaverickBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Imagen Pager
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val pagerState = rememberPagerState { promotion.imageUrls.size }
                HorizontalPager(state = pagerState) { page ->
                    AsyncImage(
                        model = promotion.imageUrls[page],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clickable { onImageClick(promotion.imageUrls[page]) }
                    )
                }

                // Badge de Descuento
                if (promotion.discount != null) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).clip(RoundedCornerShape(12.dp)).background(DiscountRed).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("-${promotion.discount}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }

                // Indicador de Pager
                if (promotion.imageUrls.size > 1) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(promotion.imageUrls.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                        }
                    }
                }
            }

            // Acciones y Descripción
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike) {
                        Icon(if (promotion.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (promotion.isLiked) DiscountRed else Color.White)
                    }
                    Text("${promotion.likes}", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = onContact) {
                        Icon(Icons.AutoMirrored.Filled.Message, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onContact,
                        colors = ButtonDefaults.buttonColors(containerColor = MaverickBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("ME INTERESA", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(promotion.description, color = Color.LightGray, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun AdBannerCard(ad: PromoListItem.AdItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AdYellow)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(color = Color.Black, shape = RoundedCornerShape(4.dp)) {
                    Text("ANUNCIO", color = AdYellow, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(ad.title, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(ad.description, color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            AsyncImage(model = ad.imageUrl, contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 5: DIÁLOGOS Y UTILIDADES ---
// ==========================================================================================

@Composable
fun FullscreenImageDialog(url: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun AdDetailDialog(ad: PromoListItem.AdItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = CardSurface)) {
            Column {
                AsyncImage(model = ad.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("ANUNCIO", color = AdYellow, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(ad.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text(ad.description, color = Color.Gray, fontSize = 14.sp)
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 24.dp), colors = ButtonDefaults.buttonColors(containerColor = AdYellow)) {
                        Text(ad.cta.uppercase(), color = Color.Black)
                    }
                }
            }
        }
    }
}

fun geminiGradientBrush() = Brush.linearGradient(listOf(Color(0xFF4285F4), Color(0xFF9B72CB), Color(0xFFD96570)))












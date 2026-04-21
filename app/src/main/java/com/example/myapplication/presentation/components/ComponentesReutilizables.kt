package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.data.model.ServiceDisplayModel
import com.example.myapplication.presentation.client.CategoryVisuals
import com.example.myapplication.presentation.components.Utilidades.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

// ==========================================================================================
// --- SECCIÓN 3: COMPONENTES BÁSICOS (UI ATOMS) ---
// ==========================================================================================

/**
 * Componente que muestra una etiqueta con el nombre de la categoría, su color e ícono.
 */
@Composable
fun ServiceTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: String? = null
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!icon.isNullOrEmpty()) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = text.formatearTexto(),
                style = MaterialTheme.typography.labelSmall,
                color = if (color.luminance() > 0.4f) Color.Black else Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Sobrecarga de ServiceTag que acepta una CategoryEntity completa.
 */
@Composable
fun ServiceTag(category: CategoryEntity, modifier: Modifier = Modifier) {
    ServiceTag(
        text = category.name,
        color = Color(CategoryVisuals.getColorFor(category.superCategory)),
        icon = category.icon,
        modifier = modifier
    )
}

// ==========================================================================================
// --- SECCIÓN 4: BANNERS Y CARRUSELES ---
// ==========================================================================================

enum class BannerType(val label: String) {
    GOOGLE_AD("SPONSORED"),
    PROMO("PROMOCIÓN"),
    NEW_CATEGORY("NUEVA CATEGORÍA"),
    NEW_PROVIDER("NUEVOS PRESTADORES"),
    PRODUCT_SALE("VENTA DE PRODUCTO"),
    SERVICE_SALE("SERVICIO DESTACADO")
}

data class AccordionBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val type: BannerType,
    val originalCategory: CategoryEntity? = null,
    val isNew: Boolean = false,
    val imageUrl: String? = null,
    val discount: Int? = null,
    val provider: Provider? = null, // Mantenido para compatibilidad
    val service: ServiceDisplayModel? = null // 🔥 [UNIFICADO]: Nuevo campo para modelo unificado
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumLensCarousel(
    items: List<AccordionBanner>,
    isPaused: Boolean = false,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 4000L
) {
    if (items.isEmpty()) return

    val infiniteCount = Int.MAX_VALUE
    val initialPage = infiniteCount / 2 - (infiniteCount / 2 % items.size.coerceAtLeast(1))
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }

    LaunchedEffect(isPaused, items) {
        if (!isPaused && items.size > 1) {
            while (true) {
                delay(autoplayDelay)
                if (!isPaused) {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth().background(MaverickColors.ROG_Dark_Bg)) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(300.dp),
            pageSpacing = 4.dp,
            contentPadding = PaddingValues(start = 10.dp, end = 64.dp),
            modifier = Modifier.fillMaxWidth().height(140.dp)
        ) { index ->
            val actualIndex = index % items.size
            val item = items[actualIndex]

            val pageOffset by remember {
                derivedStateOf {
                    ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
                }
            }

            Box(modifier = Modifier.graphicsLayer {
                val scale = lerp(start = 0.9f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                scaleX = scale
                scaleY = scale
                alpha = lerp(start = 0.5f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
            }) {
                when (item.type) {
                    BannerType.GOOGLE_AD -> AdBannerItem(item = item)
                    BannerType.PROMO -> PromotionBannerItem(item = item, onClick = { onItemClick(item) })
                    else -> PremiumBannerItem(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun PremiumBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = item.color),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
                
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.4f), Color.Transparent), startX = 0f, endX = 600f)))

                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(0.85f).fillMaxHeight().padding(start = 10.dp, top = 20.dp, bottom = 16.dp), contentAlignment = Alignment.CenterStart) {
                        Column { 
                            AutoSizeText(text = item.title.uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp))
                            Text(text = item.subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.width(40.dp).height(3.dp).background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))) 
                        }
                    }
                    Box(modifier = Modifier.weight(0.35f).fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
                        Text(text = item.icon, fontSize = 100.sp, modifier = Modifier.offset(x = 20.dp))
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).zIndex(1f)) {
            if (item.isNew || item.type == BannerType.NEW_CATEGORY || item.type == BannerType.NEW_PROVIDER) {
                val labelText = when (item.type) {
                    BannerType.NEW_CATEGORY -> "🚀 NUEVA CATEGORÍA"
                    BannerType.NEW_PROVIDER -> "👥 NUEVOS PRESTADORES"
                    else -> "✨ NUEVO"
                }
                Surface(color = Color(0xFFFFD600), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopStart)) {
                    Text(text = labelText, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun PromotionBannerItem(item: AccordionBanner, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()


    ) {
        Card(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp).clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = item.color),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (item.imageUrl != null) AsyncImage(model = item.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.4f)
                Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent), startX = 0f, endX = 500f)))

                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(0.65f).fillMaxHeight().padding(start = 16.dp, top = 20.dp, bottom = 4.dp), contentAlignment = Alignment.CenterStart) {
                        Column {
                            AutoSizeText(text = item.title.uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
                            Text(text = item.subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

                            Spacer(modifier = Modifier.weight(1f))

                            val displayPhotoUrl = item.service?.photoUrl ?: item.provider?.photoUrl
                            val displayTitle = (item.service?.title ?: item.provider?.displayName)?.formatearTexto()
                            val isVerified = item.service?.isVerified ?: item.provider?.isVerified ?: false

                            if (displayPhotoUrl != null) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(end = 10.dp)) {
                                    AsyncImage(model = displayPhotoUrl, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape).border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape), contentScale = ContentScale.Crop)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isVerified) { Icon(Icons.Default.Verified, null, tint = Color(0xFF2197F5), modifier = Modifier.size(12.dp)); Spacer(modifier = Modifier.width(4.dp)) }
                                    Text(text = displayTitle ?: "", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(0.35f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text(text = item.icon, fontSize = 80.sp, modifier = Modifier.offset(y = (-8).dp))
                    }
                }
            }
        }
/**
        if (item.discount != null) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).zIndex(1f)) {
                Surface(modifier = Modifier.align(Alignment.TopStart), color = Color(0xFFE91E63), shape = RoundedCornerShape(8.dp)) {
                    Text(text = "${item.discount}% OFF", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
        **/
    }
}

// ==========================================================================================
// --- SECCIÓN 5: CARRUSEL V3 (CARRUSEL DE PROMOCIONES MEJORADO) ---
// ==========================================================================================

/**
 * PremiumLensCarouselV3: Carrusel de alto impacto visual para promociones y anuncios.
 * Implementa animaciones suaves, tarjetas más anchas y un diseño optimizado para M3.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumLensCarouselV3(
    items: List<AccordionBanner>,
    isPaused: Boolean = false,
    onItemClick: (AccordionBanner) -> Unit,
    modifier: Modifier = Modifier,
    autoplayDelay: Long = 5000L
) {
    if (items.isEmpty()) return

    // Configuración del Pager Infinito para scroll continuo
    val infiniteCount = Int.MAX_VALUE
    val initialPage = infiniteCount / 2 - (infiniteCount / 2 % items.size.coerceAtLeast(1))
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }

    // Efecto de autodesplazamiento con tiempo aumentado para lectura cómoda
    LaunchedEffect(isPaused, items) {
        if (!isPaused && items.size > 1) {
            while (true) {
                delay(autoplayDelay)
                if (!isPaused) {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth().background(Color.Transparent).padding(vertical = 12.dp)) {
        HorizontalPager(
            state = pagerState,
            // Tarjetas más anchas (340.dp en lugar de 300.dp)
            pageSize = PageSize.Fixed(340.dp),
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 20.dp),
            modifier = Modifier.fillMaxWidth().height(170.dp)
            ) { index ->
            val actualIndex = index % items.size
            val item = items[actualIndex]

            // Cálculo de offset optimizado
            val pageOffset by remember {
                derivedStateOf {
                    ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
                }
            }
            
            Box(modifier = Modifier
                .graphicsLayer {
                    // Animación de escala sutil tipo "lente"
                    val scale = lerp(
                        start = 0.92f, 
                        stop = 1f, 
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                    scaleX = scale
                    scaleY = scale
                    // Desvanecimiento suave para tarjetas laterales
                    alpha = lerp(
                        start = 0.6f, 
                        stop = 1f, 
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
            ) {
                BannerItemV3(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

/**
 * BannerItemV3: Tarjeta rectangular con esquinas redondeadas y etiquetas integradas.
 * El diseño ubica las etiquetas entre el texto y la imagen/icono.
 */
@Composable
fun BannerItemV3(item: AccordionBanner, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp), // Esquinas redondeadas rectangulares
        colors = CardDefaults.cardColors(containerColor = item.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo con opacidad baja para no tapar el texto
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )
            }
            
            // Degradado para legibilidad del texto
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 800f
                    )
                )
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna de textos (Izquierda)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AutoSizeText(
                        text = item.title.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        maxLines = 1
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = item.subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    // Área de etiquetas (Centro entre texto e ícono)
                    if (item.isNew || item.discount != null || item.type != BannerType.SERVICE_SALE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (item.isNew) {
                                LabelTagV3(text = "NUEVO", color = Color(0xFFFFD600), textColor = Color.Black)
                            }
                            if (item.discount != null) {
                                LabelTagV3(text = "${item.discount}% OFF", color = Color(0xFFE91E63), textColor = Color.White)
                            }
                            // Si es un tipo especial (Categoría/Prestador nuevo)
                            when (item.type) {
                                BannerType.NEW_CATEGORY -> LabelTagV3("CATEGORÍA", Color.Cyan, Color.Black)
                                BannerType.NEW_PROVIDER -> LabelTagV3("PRESTADOR", Color.Green, Color.Black)
                                else -> {}
                            }
                        }
                    }
                }

                // Ícono / Emoji (Derecha)
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.icon,
                        fontSize = 60.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * LabelTagV3: Etiqueta cuadrada con esquinas redondeadas para el interior de la tarjeta.
 */
@Composable
fun LabelTagV3(text: String, color: Color, textColor: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ==========================================================================================
// --- SECCIÓN 6: PREVIEWS ---
// ==========================================================================================
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PremiumLensCarouselV3Preview() {
    val sampleItems = listOf(
        AccordionBanner(
            id = "1",
            title = "Limpieza de Hogar",
            subtitle = "Los mejores profesionales para tu casa",
            icon = "🧹",
            color = Color(0xFF2197F5),
            type = BannerType.SERVICE_SALE,
            isNew = true
        ),
        AccordionBanner(
            id = "2",
            title = "Oferta Plomería",
            subtitle = "Descuento especial por tiempo limitado",
            icon = "🪠",
            color = Color(0xFFE91E63),
            type = BannerType.PROMO,
            discount = 25
        )
    )

    MyApplicationTheme {
        Box(modifier = Modifier.padding(vertical = 16.dp)) {
            PremiumLensCarouselV3(
                items = sampleItems,
                onItemClick = {}
            )
        }
    }
}


package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.servicios.publicidad.BeAdsManager
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.ui.modelos.AccordionBanner
import com.example.myapplication.uishared.ui.modelos.BannerType
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- CARRUSEL PROMOCIONES V3 (PREMIUM UNIVERSAL) ---
 * Inspirado en apps de grandes ligas (Netflix, Instagram, Airbnb).
 * Implementa:
 * - Autoplay Infinito y Fluido.
 * - Animación de Enfoque Aggressive (Scaling + Alpha).
 * - Diseño Horizontal Elite compatible con Google Ads.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarruselPromocionesV3(
    items: List<AccordionBanner>,
    modifier: Modifier = Modifier,
    isPaused: Boolean = false,
    autoplayDelay: Long = 5000L,
    onItemClick: (AccordionBanner) -> Unit
) {
    if (items.isEmpty()) return

    // Configuración del Pager Infinito
    val infiniteCount = if (items.size > 1) 10000 else items.size
    val initialPage = if (items.size > 1) (infiniteCount / 2) - ((infiniteCount / 2) % items.size) else 0
    val pagerState = rememberPagerState(initialPage = initialPage) { infiniteCount }

    // Efecto de autodesplazamiento (Costo Zero & Dinamismo)
    LaunchedEffect(items, isPaused) {
        if (!isPaused && items.size > 1) {
            while (true) {
                delay(autoplayDelay.milliseconds)
                try {
                    // [OPTIMIZACIÓN]: Suavizamos la animación y usamos un easing más fluido
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1,
                        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
                    )
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fill,
            pageSpacing = 0.dp,
            contentPadding = PaddingValues(horizontal = 24.dp), 
            modifier = Modifier.fillMaxWidth().height(160.dp),
            // 🔥 [ELITE]: Limitar el número de páginas cacheadas para liberar RAM al inicio
            beyondViewportPageCount = 0
        ) { index ->
            val actualIndex = index % items.size
            val item = items[actualIndex]

            // [OPTIMIZACIÓN]: Usar remember con pagerState para evitar recomposiciones innecesarias
            val pageOffset = remember(pagerState) {
                derivedStateOf {
                    ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction).absoluteValue
                }
            }.value
            
            Box(modifier = Modifier
                .padding(horizontal = 4.dp)
                .graphicsLayer {
                    // Animación Premium optimizada (menos cálculos por frame)
                    val progress = (1f - pageOffset.coerceIn(0f, 1f))
                    val scale = 0.90f + (0.10f * progress)
                    scaleX = scale
                    scaleY = scale
                    alpha = 0.6f + (0.4f * progress)
                    
                    // Efecto Parallax suavizado
                    translationX = (pagerState.currentPage - index + pagerState.currentPageOffsetFraction) * 15f
                }
            ) {
                when {
                    item.nativeAd != null -> {
                        NativeCarouselAdCard(
                            nativeAd = item.nativeAd!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    item.type == BannerType.GOOGLE_AD -> {
                        // [ELITE v14.5]: Carga de Native Ad profesional para el carrusel con Fallback realista
                        AdMobCarouselNativeAd(
                            adUnitId = BeAdsManager.TEST_NATIVE_ID,
                            fallbackImage = item.imageUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    item.promotion != null -> {
                        HorizontalPremiumPromoCard(
                            promotion = item.promotion!!,
                            onClick = { onItemClick(item) }
                        )
                    }
                    else -> {
                        // Tarjeta genérica para novedades (Centralizada en ui-shared)
                        GenericBannerCardV3(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta Genérica V3 para Novedades (Dinamismo Visual)
 */
@Composable
fun GenericBannerCardV3(item: AccordionBanner, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = item.color),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = ImageUtils.processImageSource(item.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )
            }
            
            // Gradiente dinámico para legibilidad
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                    startX = 0f,
                    endX = 500f
                ))
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.subtitle.uppercase(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
                Text(
                    text = item.icon,
                    fontSize = 54.sp
                )
            }
        }
    }
}


































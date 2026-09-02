package com.example.myapplication.prestador.ui.pantallas.promocion

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.promocion.PrestadorPromocionFeedViewModel
import com.example.myapplication.prestador.viewmodel.promocion.PromocionFeedItem
import com.example.myapplication.uishared.ui.components.InstagramPromoCard
import com.example.myapplication.uishared.ui.components.InstagramStoriesRow
import com.example.myapplication.uishared.ui.components.InstagramPromoSkeleton
import com.example.myapplication.uishared.ui.components.InstagramNativeAdCard
import com.example.myapplication.uishared.ui.components.AdMobNativeAd

// --- PALETA OSCURA (misma que Inicio/Mensajes/Concursos/Mis Publicaciones---
private object FeedThemeColors {
        val DarkBg = Color(0xFF030712)
        val CardBg = Color(0xFF0F172A)
        val CardBorder = Color(0xFF334155).copy(alpha = 0.7f)
        val HeaderBg = Color(0xFF020617).copy(alpha = 0.95f)
        val Divider = Color(0xFF1E293B)
        val BrandOrange = Color(0xFFFF5722)
        val TextPrimary = Color(0xFFF8FAFC)
        val TextSecondary = Color(0xFF94A3B8)
}

/**
 * --- PANTALLA DE FEED DE PROMOCIONES (PRESTADOR v2026.FINAL) ---
 * [ELITE]: Ahora con soporte para Google Ads (Video/Multimedia) integrados orgánicamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocionFeedScreen(
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: PrestadorPromocionFeedViewModel = hiltViewModel(),
    scrollState: LazyListState = rememberLazyListState()
) {
    val historias by viewModel.historias.collectAsStateWithLifecycle()
    val feedItems by viewModel.feedItems.collectAsStateWithLifecycle()
    val estaCargando by viewModel.estaCargando.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refrescar()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FeedThemeColors.HeaderBg,
                border = BorderStroke(1.dp, FeedThemeColors.Divider)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(FeedThemeColors.CardBg, RoundedCornerShape(8.dp))
                            .border(1.dp, FeedThemeColors.CardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = FeedThemeColors.TextPrimary, modifier = Modifier.size(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text("DESCUBRIR", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = FeedThemeColors.TextPrimary, letterSpacing = 0.5.sp)
                        Text("Mercado Profesional", fontSize = 11.sp, color = FeedThemeColors.TextSecondary)
                    }
                }
            }
        },
        containerColor = FeedThemeColors.DarkBg
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = estaCargando,
            onRefresh = { viewModel.refrescar() },
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp, start = 32.dp, end = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BusinessCenter, null, tint = FeedThemeColors.BrandOrange.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("SHOPPING", fontWeight = FontWeight.Black, fontSize = 16.sp, color = FeedThemeColors.TextPrimary, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Proveedores destacados y herramientas para potenciar tu productividad.",
                            color = FeedThemeColors.TextSecondary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                if (historias.isNotEmpty()) {
                    item {
                        InstagramStoriesRow(
                            stories = historias,
                            onStoryClick = { /* Ver historia */ }
                        )
                    }
                }

                if (estaCargando && feedItems.isEmpty()) {
                    items(3) { InstagramPromoSkeleton() }
                }

                items(feedItems) { item ->
                    when (item) {
                        is PromocionFeedItem.Promo -> {
                            InstagramPromoCard(
                                promotion = item.uiModel,
                                onLike = { viewModel.toggleLike(item.uiModel.id) },
                                onCommentClick = { /* Ver comentarios */ },
                                onProviderClick = { onNavigateToProfile(item.uiModel.idPrestador) },
                                onContactClick = { /* Contactar */ }
                            )
                        }
                        is PromocionFeedItem.Ad -> {
                            InstagramNativeAdCard(nativeAd = item.nativeAd)
                        }
                        is PromocionFeedItem.FallbackAd -> {
                            AdMobNativeAd(modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

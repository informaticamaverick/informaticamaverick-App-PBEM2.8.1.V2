package com.example.myapplication.prestador.ui.pantallas.promocion

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
    val colores = getPrestadorColors()
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
                color = colores.primaryOrange,
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(colores.primaryOrange, Color(0xFFEA580C))
                            )
                        )
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("DESCUBRIR", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White, letterSpacing = 1.sp)
                        Text("Mercado Profesional", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        },
        containerColor = colores.backgroundColor
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = estaCargando,
            onRefresh = { viewModel.refrescar() },
            modifier = Modifier.padding(paddingValues).fillMaxSize()
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
                        Icon(Icons.Default.BusinessCenter, null, tint = colores.primaryOrange.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("SHOPPING", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Proveedores destacados y herramientas para potenciar tu productividad.",
                            color = Color.Gray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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

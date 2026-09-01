package com.example.myapplication.uishared.ui.components

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.myapplication.core.servicios.publicidad.BeAdsManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.AdLoader
import kotlinx.coroutines.delay

/**
 * --- ADMOB BANNER COMPONENT (MAVERICK v14.0) ---
 */
@Composable
fun AdMobBanner(
    adUnitId: String = BeAdsManager.TEST_BANNER_ID,
    modifier: Modifier = Modifier,
    adSizeOverride: AdSize? = null,
    onAdLoaded: () -> Unit = {},
    onAdFailed: (LoadAdError) -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isPreview = LocalInspectionMode.current
    
    if (!BeAdsManager.isAdSystemEnabled || isPreview) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1C1E)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isPreview) "VISTA PREVIA DE PUBLICIDAD" else "PROMO ESPACIO RESERVADO",
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    var adLoaded by remember { mutableStateOf(false) }
    var currentAdSize by remember { mutableStateOf<AdSize?>(adSizeOverride) }

    LaunchedEffect(activity, adSizeOverride) {
        if (adSizeOverride == null) {
            activity?.let {
                val displayMetrics = it.resources.displayMetrics
                val adWidthPixels = displayMetrics.widthPixels.toFloat()
                val density = displayMetrics.density
                val adWidth = (adWidthPixels / density).toInt()
                currentAdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(it, adWidth)
            }
        }
    }

    val adSize = currentAdSize ?: AdSize.BANNER

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adSize.getHeightInPixels(context).let { (it / context.resources.displayMetrics.density).dp }.coerceAtLeast(60.dp)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    val adView = AdView(ctx).apply {
                        setAdSize(adSize)
                        setAdUnitId(adUnitId)
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                adLoaded = true
                                onAdLoaded()
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                super.onAdFailedToLoad(error)
                                onAdFailed(error)
                            }
                        }
                    }
                    addView(adView)
                    adView.loadAd(AdRequest.Builder().build())
                }
            }
        )

        if (!adLoaded) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF1A1C1E)),
                contentAlignment = Alignment.Center
            ) {
                Text("CARGANDO PUBLICIDAD...", color = Color.DarkGray, fontSize = 9.sp)
            }
        }
    }
}

/**
 * --- INSTAGRAM NATIVE AD CARD (ELITE 2026 - FULL WIDTH) ---
 * Rediseño profesional: Ocupa todo el ancho, sin colas de burbuja, estilo post real.
 */
@Composable
fun InstagramNativeAdCard(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier
) {
    // Contenedor principal
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                val nativeAdView = NativeAdView(context)
                
                // 🔥 [CORRECCIÓN]: Todo el layout DEBE estar dentro de nativeAdView
                val root = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // 1. Header Row
                val header = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    val p = (12 * context.resources.displayMetrics.density).toInt()
                    setPadding(p, p, p, p)
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }
                
                val iconView = android.widget.ImageView(context).apply {
                    val size = (38 * context.resources.displayMetrics.density).toInt()
                    layoutParams = android.widget.LinearLayout.LayoutParams(size, size)
                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                }
                nativeAdView.iconView = iconView
                
                val textContainer = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding((10 * context.resources.displayMetrics.density).toInt(), 0, 0, 0)
                }
                
                val headline = android.widget.TextView(context).apply {
                    setTextColor(android.graphics.Color.WHITE)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    textSize = 14f
                }
                nativeAdView.headlineView = headline
                
                val advertised = android.widget.TextView(context).apply {
                    text = "Patrocinado"
                    setTextColor(android.graphics.Color.GRAY)
                    textSize = 10f
                }
                
                textContainer.addView(headline)
                textContainer.addView(advertised)
                header.addView(iconView)
                header.addView(textContainer)
                root.addView(header)

                // 2. Media View
                val mediaView = MediaView(context).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                nativeAdView.mediaView = mediaView
                root.addView(mediaView)

                // 3. Info & CTA Area
                val infoContainer = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    val p = (16 * context.resources.displayMetrics.density).toInt()
                    setPadding(p, p, p, p)
                }

                val body = android.widget.TextView(context).apply {
                    setTextColor(android.graphics.Color.LTGRAY)
                    textSize = 13f
                    maxLines = 3
                    setPadding(0, 0, 0, (16 * context.resources.displayMetrics.density).toInt())
                }
                nativeAdView.bodyView = body
                
                val cta = android.widget.Button(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setTextColor(android.graphics.Color.parseColor("#00E5FF"))
                    val shape = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = 12 * context.resources.displayMetrics.density
                        setStroke((1 * context.resources.displayMetrics.density).toInt(), android.graphics.Color.parseColor("#00E5FF"))
                    }
                    background = shape
                    val h = (48 * context.resources.displayMetrics.density).toInt()
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        h
                    )
                }
                nativeAdView.callToActionView = cta

                infoContainer.addView(body)
                infoContainer.addView(cta)
                root.addView(infoContainer)

                // IMPORTANTE: Primero añadir el root a la nativeAdView
                nativeAdView.addView(root)
                nativeAdView
            },
            update = { view ->
                // Mapeo de datos dinámicos
                (view.headlineView as? TextView)?.text = nativeAd.headline
                (view.bodyView as? TextView)?.text = nativeAd.body
                (view.callToActionView as? android.widget.Button)?.text = nativeAd.callToAction ?: "MÁS INFO"
                
                nativeAd.icon?.let {
                    (view.iconView as? android.widget.ImageView)?.setImageDrawable(it.drawable)
                }

                view.setNativeAd(nativeAd)
            }
        )
    }
}

/**
 * --- NATIVE CAROUSEL AD CARD (ELITE v14.5 - HIGH PERFORMANCE) ---
 * Rediseño profesional: Gradientes Cyber, bordes neón suaves y tipografía optimizada.
 */
@Composable
fun NativeCarouselAdCard(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier,
    fallbackImage: Any? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520)),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.Transparent)))
    ) {
        AndroidView(
            factory = { ctx ->
                val nativeAdView = NativeAdView(ctx)
                val composeView = ComposeView(ctx)
                nativeAdView.addView(composeView)
                
                // Registro de componentes para interactividad (Requerido por Google)
                nativeAdView.headlineView = composeView
                nativeAdView.mediaView = MediaView(ctx) // Placeholder técnico
                
                composeView.setContent {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Fondo con gradiente sutil
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(size.width * 0.4f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width, size.height)
                                lineTo(size.width * 0.3f, size.height)
                                close()
                            }
                            drawPath(path, Color.White.copy(alpha = 0.02f))
                        }

                        Row(modifier = Modifier.fillMaxSize()) {
                            // 1. Media Area (Visual Engine)
                            val ratioAspecto = remember(nativeAd.mediaContent) {
                                val ratio = nativeAd.mediaContent?.aspectRatio ?: 1.33f
                                if (ratio > 0f) ratio else 1.33f
                            }
                            Box(modifier = Modifier.weight(0.42f).fillMaxHeight().aspectRatio(ratioAspecto)) {
                                AndroidView(
                                    factory = { c -> 
                                        MediaView(c).apply {
                                            nativeAdView.mediaView = this
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Overlay de Fallback (Si no carga el MediaView de Google o es Test Ad)
                                val finalFallback = fallbackImage ?: nativeAd.images.firstOrNull()?.drawable ?: "https://picsum.photos/seed/ad/400/300"
                                
                                if (nativeAd.mediaContent == null || nativeAd.mediaContent?.hasVideoContent() == false) {
                                    AsyncImage(
                                        model = finalFallback,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Badge de "ANUNCIO" estilo Elite
                                Surface(
                                    color = Color(0xFFFFC107).copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = "AD",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // 2. Info Area (Marketing Engine)
                            Column(
                                modifier = Modifier
                                    .weight(0.58f)
                                    .padding(12.dp)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = nativeAd.headline ?: "Sugerencia Maverick",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = nativeAd.body ?: "Descubre servicios exclusivos cerca de ti.",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // CTA Button: El trigger de conversión
                                Surface(
                                    modifier = Modifier.fillMaxWidth().height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
                                ) {
                                    AndroidView(
                                        factory = { c ->
                                            TextView(c).apply {
                                                text = nativeAd.callToAction?.uppercase() ?: "MÁS INFO"
                                                setTextColor(android.graphics.Color.parseColor("#00E5FF"))
                                                textSize = 10f
                                                gravity = android.view.Gravity.CENTER
                                                setTypeface(null, android.graphics.Typeface.BOLD)
                                                nativeAdView.callToActionView = this
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
                nativeAdView
            },
            update = { view ->
                view.setNativeAd(nativeAd)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * --- SMART NATIVE CAROUSEL AD (MAVERICK v14.5) ---
 * Orquestador especializado para el carrusel de Home.
 * Maneja la carga en segundo plano y el skeleton placeholder.
 */
@Composable
fun AdMobCarouselNativeAd(
    adUnitId: String = BeAdsManager.TEST_NATIVE_ID,
    modifier: Modifier = Modifier,
    fallbackImage: Any? = null
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    if (isPreview || !BeAdsManager.isAdSystemEnabled) {
        NativeCarouselPlaceholder(modifier = modifier)
        return
    }

    LaunchedEffect(adUnitId) {
        try {
            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { ad ->
                    nativeAd = ad
                    isLoading = false
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdMobCarouselNativeAd", "Fallo al cargar: ${error.message}")
                        isLoading = false
                    }
                })
                .withNativeAdOptions(BeAdsManager.buildNativeAdOptions())
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            isLoading = false
        }
    }

    if (isLoading) {
        NativeCarouselPlaceholder(modifier = modifier)
    } else {
        nativeAd?.let {
            NativeCarouselAdCard(nativeAd = it, modifier = modifier, fallbackImage = fallbackImage)
        } ?: NativeCarouselPlaceholder(modifier = modifier)
    }
}

/**
 * --- NATIVE CAROUSEL PLACEHOLDER (SKELETON v14.5) ---
 */
@Composable
fun NativeCarouselPlaceholder(modifier: Modifier = Modifier) {
    val skeletonColor = Color.White.copy(alpha = 0.05f)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.42f).fillMaxHeight().background(skeletonColor), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF00E5FF).copy(alpha = 0.3f), strokeWidth = 2.dp)
            }
            Column(modifier = Modifier.weight(0.58f).padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Box(modifier = Modifier.size(100.dp, 12.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
                    Spacer(Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
                }
                Box(modifier = Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(8.dp)).background(skeletonColor))
            }
        }
    }
}

/**
 * --- SMART NATIVE AD LOADER (ELITE 2026) ---
 * Este componente orquesta la carga real de un NativeAd desde AdMob.
 * Mientras carga, muestra el placeholder nativo.
 */
@Composable
fun AdMobNativeAd(
    adUnitId: String = BeAdsManager.TEST_NATIVE_ID,
    modifier: Modifier = Modifier,
    fallbackImage: Any? = null,
    preLoadedAd: NativeAd? = null // 🔥 [ELITE]: Soporte para anuncios pre-cargados
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    
    // Si viene pre-cargado, lo mostramos inmediatamente
    if (preLoadedAd != null) {
        InstagramNativeAdCard(nativeAd = preLoadedAd, modifier = modifier)
        return
    }

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // En Preview o si el sistema está desactivado, mostramos el placeholder
    if (isPreview || !BeAdsManager.isAdSystemEnabled) {
        NativeAdPlaceholder(modifier = modifier, fallbackImage = fallbackImage)
        return
    }

    LaunchedEffect(adUnitId) {
        try {
            val activity = BeAdsManager.findActivity(context) ?: return@LaunchedEffect
            val adLoader = AdLoader.Builder(activity, adUnitId)
                .forNativeAd { ad ->
                    nativeAd = ad
                    isLoading = false
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdMobNativeAd", "❌ [ADS_LOAD_FAIL] Código: ${error.code} | Mensaje: ${error.message}")
                        isLoading = false
                    }
                })
                .withNativeAdOptions(BeAdsManager.buildNativeAdOptions())
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            isLoading = false
        }
    }

    if (isLoading) {
        NativeAdPlaceholder(modifier = modifier, fallbackImage = fallbackImage)
    } else {
        nativeAd?.let {
            InstagramNativeAdCard(nativeAd = it, modifier = modifier)
        } ?: NativeAdPlaceholder(modifier = modifier, fallbackImage = fallbackImage)
    }
}

/**
 * --- NATIVE AD PLACEHOLDER (ELITE SKELETON) ---
 * Imita la estructura de un post de Instagram para una carga fluida.
 */
@Composable
fun NativeAdPlaceholder(
    modifier: Modifier = Modifier,
    fallbackImage: Any? = null
) {
    val skeletonColor = Color.White.copy(alpha = 0.08f)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        // Header Skeleton
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(skeletonColor)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Box(modifier = Modifier.size(120.dp, 10.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.size(70.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            }
        }
        
        // Media Skeleton (Cuadrado Perfecto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(skeletonColor),
            contentAlignment = Alignment.Center
        ) {
            if (fallbackImage != null) {
                AsyncImage(
                    model = fallbackImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.4f),
                    contentScale = ContentScale.Crop
                )
            }

            CircularProgressIndicator(
                color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Info Skeleton
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(180.dp, 12.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(8.dp).clip(RoundedCornerShape(2.dp)).background(skeletonColor))
            
            Spacer(Modifier.height(24.dp))
            
            // CTA Button Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(skeletonColor)
            )
        }
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
    }
}

/**
 * --- REWARDED INTERSTITIAL VIDEO AD ---
 */
@Composable
fun RewardedInterstitialVideoAd(
    show: Boolean,
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit,
    adUnitId: String = BeAdsManager.TEST_REWARDED_INTERSTITIAL_ID
) {
    if (!show) return

    val context = LocalContext.current
    val activity = context as? Activity
    val isPreview = LocalInspectionMode.current

    LaunchedEffect(show) {
        if (show && !isPreview) {
            android.util.Log.d("AdMobElite", "🔍 [ADS_REQUEST] Cargando video recompensado...")
            RewardedInterstitialAd.load(
                context,
                adUnitId,
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        android.util.Log.d("AdMobElite", "✅ [ADS_LOAD_SUCCESS] Video cargado. Configurando callbacks.")
                        
                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                android.util.Log.d("AdMobElite", "🚪 [ADS_DISMISSED] El usuario cerró el video.")
                                onDismiss()
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                android.util.Log.e("AdMobElite", "❌ [ADS_SHOW_FAIL] No se pudo mostrar: ${error.message}")
                                onDismiss()
                            }
                        }

                        activity?.let {
                            android.util.Log.d("AdMobElite", "🎬 [ADS_SHOW] Mostrando video ahora.")
                            ad.show(it) { reward ->
                                android.util.Log.d("AdMobElite", "🏆 [ADS_REWARD] Recompensa obtenida: ${reward.amount} ${reward.type}")
                                onRewardEarned()
                            }
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        android.util.Log.e("AdMobElite", "❌ [ADS_LOAD_FAIL] Fallo al cargar video: ${error.message}")
                        onDismiss()
                    }
                }
            )
        }
    }

    // 🔥 [ELITE] v2026: Diálogo de carga eliminado para inmediatez absoluta.
    // El feedback se gestiona vía Be Toast en el ViewModel.
}

/*
/**
 * --- GOOGLE INTERSTITIAL AD (v14.0 UNIVERSAL - MOCK VERSION) ---
 * @deprecated Esta versión es manual y local. Usar implementaciones reales de AdMob para producción.
 */
@Composable
fun GoogleVerticalInterstitialAd(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    adUnitId: String = BeAdsManager.TEST_INTERSTITIAL_ID
) {
    if (!show) return

    var timeLeft by remember { mutableIntStateOf(10) }
    var isClosable by remember { mutableStateOf(false) }

    val adData = remember {
        listOf(
            Triple("https://images.unsplash.com/photo-1599305090598-fe179d501227?q=80&w=1080", "Transforma tu Hogar con Inteligencia", "Descubre la nueva línea de dispositivos BeSmart."),
            Triple("https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=1080", "Maverick Pro: Herramientas de Elite", "Suscripción premium para profesionales exigentes."),
            Triple("https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=1080", "Aumenta tus Ventas en un 40%", "Nuevas estrategias de marketing digital.")
        ).random()
    }

    LaunchedEffect(show) {
        if (show) {
            timeLeft = 10
            isClosable = false
            while (timeLeft > 0) {
                delay(1000L)
                timeLeft--
            }
            isClosable = true
        }
    }

    Dialog(onDismissRequest = { if (isClosable) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.98f))) {
            AsyncImage(model = adData.first, contentDescription = null, modifier = Modifier.fillMaxSize().alpha(0.15f), contentScale = ContentScale.Crop)
            Card(modifier = Modifier.fillMaxHeight(0.88f).fillMaxWidth(0.92f).align(Alignment.Center), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
                            AsyncImage(model = adData.first, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            Surface(modifier = Modifier.align(Alignment.Center).size(64.dp), shape = CircleShape, color = Color.Black.copy(alpha = 0.6f), border = BorderStroke(2.dp, Color.White)) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.padding(16.dp).size(32.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = Color(0xFF1A73E8)) {
                                    Icon(Icons.Default.AdsClick, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(text = "Maverick Ecosystem", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = adData.second, fontWeight = FontWeight.Black, color = Color(0xFF1A73E8), textAlign = TextAlign.Center, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = adData.third, textAlign = TextAlign.Center, color = Color.DarkGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = { onDismiss() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))) {
                                Text("PROBAR AHORA", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Surface(color = Color(0xFFFFC107), shape = RoundedCornerShape(bottomEnd = 16.dp), modifier = Modifier.align(Alignment.TopStart)) {
                        Text(text = "ANUNCIO PATROCINADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 24.dp, end = 16.dp)) {
                if (isClosable) {
                    FilledIconButton(onClick = onDismiss, modifier = Modifier.size(44.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White.copy(0.2f), contentColor = Color.White)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { timeLeft.toFloat() / 10f }, modifier = Modifier.size(44.dp), color = Color.White, strokeWidth = 3.dp)
                        Text(text = timeLeft.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
*/

// ==========================================================================================
// --- PREVIEWS (MAVERICK ELITE 2026) ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun AdMobBannerPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("ADAPTIVE BANNER (FALLBACK)", color = Color.White, fontSize = 10.sp)
        Spacer(Modifier.height(8.dp))
        AdMobBanner()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun InstagramNativeAdCardPreview() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // [ELITE REDESIGN]: Sin colas, sin márgenes, estilo post real.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.Gray))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Anunciante Elite", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Patrocinado", color = Color.Gray, fontSize = 10.sp)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.DarkGray))
            Column(modifier = Modifier.padding(16.dp)) {
                Text("¡Publicidad Estilo Instagram!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Este es el nuevo diseño que integra anuncios de forma profesional y orgánica.", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))) {
                    Text("MÁS INFORMACIÓN", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun RewardedInterstitialVideoAdPreview() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF00E5FF))
            Spacer(Modifier.height(24.dp))
            Text("PROCESANDO PUBLICACIÓN...", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}


































package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ==========================================================================================
// --- SECCIÓN 1: BANNERS HORIZONTALES (PARA CARRUSELES) ---
// ==========================================================================================

/**
 * Componente que representa un banner publicitario horizontal.
 */
@Composable
fun AdBannerItem(item: AccordionBanner) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.3f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdsClick,
                    contentDescription = "Ads",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            Surface(
                color = Color(0xFFFFC107),
                shape = RoundedCornerShape(bottomEnd = 12.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = "ANUNCIO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 2: ANUNCIOS PANTALLA COMPLETA (INTERSTITIALS) ---
// ==========================================================================================

/**
 * Componente de Anuncio Vertical Premium con apariencia real.
 */
@Composable
fun GoogleVerticalInterstitialAd(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
) {
    if (!show) return

    var timeLeft by remember { mutableIntStateOf(10) }
    var isClosable by remember { mutableStateOf(false) }

    // Simulación de carga de anuncio real con imágenes de alta calidad
    val adData = remember {
        listOf(
            Triple("https://images.unsplash.com/photo-1599305090598-fe179d501227?q=80&w=1080", "Transforma tu Hogar con Inteligencia", "Descubre la nueva línea de dispositivos BeSmart. Tecnología que entiende tu ritmo."),
            Triple("https://images.unsplash.com/photo-1523275335684-37898b6baf30?q=80&w=1080", "Maverick Pro: Herramientas de Elite", "Suscripción premium para profesionales exigentes. Duplica tus licitaciones hoy."),
            Triple("https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=1080", "Aumenta tus Ventas en un 40%", "Nuevas estrategias de marketing digital para prestadores locales.")
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

    Dialog(
        onDismissRequest = { if (isClosable) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = isClosable,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.98f))
        ) {
            // Fondo con efecto blur (simulado con imagen de fondo oscura)
            AsyncImage(
                model = adData.first,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.15f),
                contentScale = ContentScale.Crop
            )

            Card(
                modifier = Modifier
                    .fillMaxHeight(0.88f)
                    .fillMaxWidth(0.92f)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(20.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Imagen Principal del Anuncio
                        Box(modifier = Modifier.weight(1.2f).fillMaxWidth()) {
                            AsyncImage(
                                model = adData.first,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Botón de Play Flotante (Simula Video)
                            Surface(
                                modifier = Modifier.align(Alignment.Center).size(64.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(16.dp).size(32.dp)
                                )
                            }

                            // Badge de Tiempo en video
                            Surface(
                                color = Color.Black.copy(0.7f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                            ) {
                                Text("0:15", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp))
                            }
                        }

                        // Contenido de Texto
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1A73E8)
                                ) {
                                    Icon(Icons.Default.AdsClick, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Be Ecosystem",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = adData.second,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A73E8),
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = adData.third,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            Button(
                                onClick = { /* Simular clic */ },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                            ) {
                                Text("PROBAR AHORA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    }
                    
                    // Badge de Anuncio
                    Surface(
                        color = Color(0xFFFFC107),
                        shape = RoundedCornerShape(bottomEnd = 16.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "ANUNCIO PATROCINADO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Controles de Cierre
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
            ) {
                if (isClosable) {
                    FilledIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { timeLeft.toFloat() / 10f },
                            modifier = Modifier.size(44.dp),
                            color = Color.White,
                            strokeWidth = 3.dp,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = timeLeft.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// --- SECCIÓN 3: PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun AdBannerItemPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).size(width = 300.dp, height = 120.dp)) {
            AdBannerItem(
                item = AccordionBanner(
                    id = "ad",
                    title = "Publicidad Premium",
                    subtitle = "Anuncio patrocinado por Google AdMob",
                    icon = "📢",
                    color = Color.White,
                    type = BannerType.GOOGLE_AD
                )
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun GoogleVerticalInterstitialAdPreview() {
    MyApplicationTheme {
        var showAd by remember { mutableStateOf(true) }
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { showAd = true }) {
                Text("Simular Interstitial")
            }
            
            GoogleVerticalInterstitialAd(
                show = showAd,
                onDismiss = { showAd = false }
            )
        }
    }
}










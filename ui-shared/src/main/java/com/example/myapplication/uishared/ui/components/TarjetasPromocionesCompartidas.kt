package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.mapeadores.PromocionMappers
import com.example.myapplication.core.dominio.modelos.PromocionComentario
import com.google.android.gms.ads.nativead.NativeAd
import com.example.myapplication.core.dominio.modelos.Promocion
import com.example.myapplication.core.dominio.modelos.PromocionDominio
import com.example.myapplication.core.dominio.modelos.TipoPromocion
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.estilos.SharedPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * MODIFICADOR SHAKE (EFECTO DE TEMBLOR) LOCAL PARA COMPONENTES COMPARTIDOS
 */
fun Modifier.shakeClick(onClick: () -> Unit): Modifier = composed {
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }

    this.graphicsLayer {
        rotationZ = rotation.value
    }
    .clickable(
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
        indication = null
    ) {
        scope.launch {
            rotation.animateTo(15f, tween(50, easing = androidx.compose.animation.core.LinearEasing))
            rotation.animateTo(-15f, tween(50, easing = androidx.compose.animation.core.LinearEasing))
            rotation.animateTo(0f, androidx.compose.animation.core.tween(50, easing = androidx.compose.animation.core.LinearEasing))
        }
        onClick()
    }
}

/**
 * --- HORIZONTAL PREMIUM PROMO CARD (ELITE v14.0) ---
 * Diseñada específicamente para el carrusel de la pantalla de inicio.
 * Optimiza el espacio horizontal y prioriza la legibilidad y el impacto visual.
 * [LEY #9]: Variables en español.
 */
@Composable
fun HorizontalPremiumPromoCard(
    promotion: PromocionDominio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appBlue = SharedPalette.ElectricCyan
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SharedPalette.EliteSurface),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Imagen (Lado Izquierdo)
            Box(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
                AsyncImage(
                    model = ImageUtils.processImageSource(promotion.urlImagen ?: "https://picsum.photos/400"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                promotion.etiquetaOferta?.let { label ->
                    Surface(
                        color = SharedPalette.RogCrimson,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 2. Información (Lado Derecho)
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageUtils.processImageSource(promotion.urlMiniaturaPrestador),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = promotion.nombrePrestador,
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = promotion.titulo,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (promotion.descripcion.isNotEmpty()) {
                        Text(
                            text = promotion.descripcion,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 13.sp
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    promotion.etiquetaOferta?.let { label ->
                        Text(
                            text = label,
                            color = appBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = appBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * --- INSTAGRAM PROMO CARD (FEED STYLE - SHARED) ---
 * [LEY #9]: Variables en español.
 */
@Composable
fun InstagramPromoCard(
    promotion: PromocionDominio,
    onLike: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onProviderClick: () -> Unit = {},
    onContactClick: () -> Unit = {},
    isClickable: Boolean = true,
    isCompact: Boolean = false
) {
    var isHeartVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val appBlue = SharedPalette.ElectricCyan
    val darkBg = SharedPalette.DarkBg

    if (isCompact) {
        // --- VERSIÓN COMPACTA PARA MENSAJES DE CHAT ---
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = isClickable) { onProviderClick() },
            colors = CardDefaults.cardColors(containerColor = SharedPalette.EliteSurface),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    if (promotion.urlImagen != null) {
                        AsyncImage(
                            model = ImageUtils.processImageSource(promotion.urlImagen),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(darkBg), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Campaign, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                        }
                    }
                    promotion.etiquetaOferta?.let { label ->
                        BadgeOverlay(label, SharedPalette.RogCrimson)
                    }
                }
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(promotion.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(promotion.nombrePrestador, color = appBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = onContactClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = appBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, appBlue.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "VER DETALLES",
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = appBlue
                        )
                    }
                }
            }
        }
    } else {
        // --- VERSIÓN FULL REFINADA (INSTAGRAM 2026 STYLE) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(darkBg)
        ) {
            // 1. Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageUtils.processImageSource(promotion.urlMiniaturaPrestador),
                    contentDescription = null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, InstagramGradient, CircleShape)
                        .clickable { onProviderClick() },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f).clickable { onProviderClick() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = promotion.nombrePrestador.ifBlank { "Prestador Elite" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (promotion.estaVerificado) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = appBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = promotion.tiempoRelativo,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = { /* TODO: Opciones */ }) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.7f))
                }
            }

            // 2. Media Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        if (isClickable) {
                            detectTapGestures(onDoubleTap = {
                                if (!promotion.leGustaAlUsuario) onLike()
                                isHeartVisible = true
                                scope.launch { delay(800.milliseconds); isHeartVisible = false }
                            })
                        }
                    }
            ) {
                AsyncImage(
                    model = ImageUtils.processImageSource(promotion.urlImagen ?: "https://picsum.photos/800"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                promotion.etiquetaOferta?.let { label ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SharedPalette.RogCrimson)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isHeartVisible,
                    enter = scaleIn(tween(300)) + fadeIn(),
                    exit = scaleOut(tween(300)) + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(Icons.Default.Favorite, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(100.dp))
                }
            }

            // 3. Actions Barra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.shakeClick { onLike() }
                ) {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (promotion.leGustaAlUsuario) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (promotion.leGustaAlUsuario) SharedPalette.RogCrimson else Color.White
                        )
                    }
                    Text(
                        text = promotion.conteoLikes.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.shakeClick { onCommentClick() }
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White, modifier = Modifier.padding(12.dp).clickable { onCommentClick() })
                    Text(
                        text = "0", 
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    onClick = onContactClick,
                    shape = RoundedCornerShape(20.dp),
                    color = appBlue,
                    modifier = Modifier.shakeClick { onContactClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Text(
                            "CONTACTAR",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 4. Pie de Foto
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                Text(
                    text = promotion.titulo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (promotion.descripcion.isNotEmpty()) {
                    Text(
                        text = promotion.descripcion,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.BadgeOverlay(text: String, color: Color) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

@Composable
fun ServiceCompletionBubble(
    isFromMe: Boolean,
    evidenceUrl: String? = null,
    onRateClick: () -> Unit = {}
) {
    val appBlue = SharedPalette.ElectricCyan

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glow"
    )

    ElevatedCard(
        modifier = Modifier
            .width(280.dp)
            .padding(vertical = 4.dp)
            .border(1.dp, Brush.linearGradient(listOf(appBlue.copy(alpha = glowAlpha), Color.Transparent)), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF16161D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = appBlue.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CheckCircle, null, tint = appBlue, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("¡TRABAJO FINALIZADO!", fontWeight = FontWeight.Black, fontSize = 12.sp, color = appBlue, letterSpacing = 1.sp)
                    Text("Resumen del servicio", fontSize = 10.sp, color = Color.Gray)
                }
            }

            if (evidenceUrl != null) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = evidenceUrl,
                    contentDescription = "Evidencia",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))
            
            if (!isFromMe) {
                Button(
                    onClick = onRateClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appBlue),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CALIFICAR AHORA", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    Text(
                        "Esperando calificación...",
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * --- PROMO COMMENTS SHEET (ELITE v12.0 - INSTAGRAM STYLE) ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoCommentsSheet(
    onDismiss: () -> Unit,
    comments: List<PromocionComentario>,
    onSendComment: (String) -> Unit,
    currentUserPhoto: String? = null
) {
    var text by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SharedPalette.EliteSurface,
        contentColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxHeight(0.85f),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SharedPalette.EliteSurface)
                        .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding() + 8.dp)
                ) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val emojis = listOf("❤️", "🙌", "🔥", "👏", "😢", "😍", "😮", "😂")
                        emojis.forEach { emoji ->
                            Text(
                                text = emoji, 
                                fontSize = 22.sp,
                                modifier = Modifier.clickable { text += emoji }.padding(6.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = ImageUtils.processImageSource(currentUserPhoto),
                            contentDescription = null,
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.Gray.copy(0.2f)),
                            contentScale = ContentScale.Crop
                        )
                        
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(SharedPalette.ElectricCyan),
                            decorationBox = { innerTextField ->
                                if (text.isEmpty()) Text("Únete a la conversación...", color = Color.Gray, fontSize = 14.sp)
                                innerTextField()
                            }
                        )

                        AnimatedVisibility(visible = text.isNotBlank()) {
                            Text(
                                "Publicar",
                                color = SharedPalette.ElectricCyan,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.clickable {
                                    onSendComment(text)
                                    text = ""
                                    focusManager.clearFocus()
                                }.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                Text(
                    "Comentarios",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("Aún no hay comentarios.", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        items(comments) { CommentItemRow(it) }
                    }
                }
            }
        }
    }
}

/**
 * --- COMMENT ITEM ROW ---
 */
@Composable
private fun CommentItemRow(comment: PromocionComentario) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = ImageUtils.processImageSource(comment.urlFotoUsuario),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(0.1f)),
            contentScale = ContentScale.Crop
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.nombreUsuario,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "1h", 
                    fontSize = 11.sp, 
                    color = Color.Gray.copy(alpha = 0.8f)
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = comment.texto,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.95f),
                lineHeight = 18.sp
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Responder", 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "0", 
                fontSize = 10.sp, 
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * --- COMPONENTES DE HISTORIAS (SHARED) ---
 */

@Composable
fun InstagramStoriesRow(
    stories: List<PromocionDominio>,
    onStoryClick: (PromocionDominio) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stories) { story ->
            StoryItem(story, onStoryClick)
        }
    }
}

@Composable
fun StoryItem(story: PromocionDominio, onClick: (PromocionDominio) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "borderScale"
    )
    
    val adIcon = (story.nativeAd as? NativeAd)?.icon?.drawable

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick(story) }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = borderScale
                    scaleY = borderScale
                }
                .clip(CircleShape)
                .background(if (story.esPublicidad) Brush.linearGradient(listOf(Color(0xFF22D3EE), Color(0xFF0EA5E9))) else InstagramGradient)
                .padding(3.dp)
                .clip(CircleShape)
                .background(SharedPalette.DarkBg)
                .padding(2.dp)
        ) {
            AsyncImage(
                model = if (story.esPublicidad) adIcon ?: Icons.Default.Campaign else ImageUtils.processImageSource(story.urlMiniaturaPrestador),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            
            if (story.estaVerificado || story.esPublicidad) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 2.dp, end = 2.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(1.dp)
                        .clip(CircleShape)
                        .background(if (story.esPublicidad) Color(0xFF22D3EE) else SharedPalette.ElectricCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (story.esPublicidad) Icons.Default.Campaign else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (story.esPublicidad) Color.Black else Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (story.esPublicidad) "ANUNCIO" else story.nombrePrestador,
            color = if (story.esPublicidad) Color(0xFF22D3EE) else Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val InstagramGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF9CE34), Color(0xFFEE2A7B), Color(0xFF6228D7))
)

// ==========================================================================================
// --- PREVIEWS (SHARED) ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun InstagramPromoCardPreview() {
    val mockPromo = Promocion(
        id = "preview_1",
        idPrestador = "p1",
        nombrePrestador = "app Studio",
        urlFotoPrestador = "https://picsum.photos/200",
        titulo = "Diseño UI/UX con 40% OFF",
        descripcion = "Transformamos tu idea en una app de élite.",
        tipo = TipoPromocion.PROMOCION,
        urlImagenes = listOf("https://picsum.photos/800/800"),
        porcentajeDescuento = 40,
        conteoLikes = 1240,
        leGustaAlUsuario = true
    )

    Box(modifier = Modifier.fillMaxWidth().background(SharedPalette.DarkBg)) {
        InstagramPromoCard(promotion = PromocionMappers.aUiModel(mockPromo))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
fun InstagramStoriesRowPreview() {
    val mockStories = listOf(
        Promocion(id = "s1", idPrestador = "p1", nombrePrestador = "Tu Negocio", urlFotoPrestador = "https://picsum.photos/100"),
        Promocion(id = "s2", idPrestador = "p2", nombrePrestador = "Elite Tech", urlFotoPrestador = "https://picsum.photos/101")
    )

    Box(modifier = Modifier.fillMaxWidth().background(SharedPalette.DarkBg)) {
        InstagramStoriesRow(stories = mockStories.map { PromocionMappers.aUiModel(it) }, onStoryClick = {})
    }
}


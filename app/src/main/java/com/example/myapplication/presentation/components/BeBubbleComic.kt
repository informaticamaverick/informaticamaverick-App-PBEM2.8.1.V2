package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.presentation.components.Utilidades.SectionHeaderWithDivider
import com.example.myapplication.presentation.components.Utilidades.EtiquetasPremium
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.CyberColorsV3
import com.example.myapplication.presentation.components.Utilidades.CyberTypography
import com.example.myapplication.presentation.components.Utilidades.BtnCancelStealth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.presentation.client.BeBrainViewModel.SearchResult
import com.example.myapplication.presentation.client.BeSearchReaction
import com.example.myapplication.presentation.client.SuperCategory
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.presentation.client.BubbleSection
import com.example.myapplication.presentation.components.ControlItem
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoBorderBrush
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoGlassBrush

// ==========================================================================================
// --- MICRO-COMPONENTES (ESTILO MAVERICK M3) ---
// ==========================================================================================

@Composable
fun CategoryExplorationChip(
    item: ControlItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A1A24).copy(alpha = 0.8f),
        border = BorderStroke(1.dp, item.color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(item.emoji ?: "📂", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ActionChip(
    item: ControlItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp), // Más redondeado estilo M3
        color = item.color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, item.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.emoji != null) {
                Text(item.emoji, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
            } else if (item.icon != null) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(16.dp).padding(end = 6.dp))
            }
            Text(
                text = item.label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun BubbleSectionContent(
    section: BubbleSection,
    onResultClick: (Any) -> Unit,
    onActionClick: (String?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (section) {
            is BubbleSection.Categories -> {
                Text("CATEGORÍAS", color = CyberColorsV3.ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(section.items) { cat ->
                        Box(modifier = Modifier.width(130.dp)) {
                            CompactCategoryCard(item = cat, onClick = { onResultClick(cat) })
                        }
                    }
                }
            }
            is BubbleSection.SuperCategories -> {
                Text("GRUPOS", color = CyberColorsV3.ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.items.forEach { superCat ->
                        BentoSuperCategoryCard(
                            superCategory = superCat,
                            emoji = superCat.icon,
                            height = 100.dp,
                            onClick = { onResultClick(superCat) }
                        )
                    }
                }
            }
            is BubbleSection.Favorites -> {
                Text("MIS FAVORITOS", color = CyberColorsV3.ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            is BubbleSection.Budgets -> {
                Text("PRESUPUESTOS", color = CyberColorsV3.ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            is BubbleSection.Tenders -> {
                Text("LICITACIONES", color = CyberColorsV3.ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
            is BubbleSection.Providers -> {
                Text("PRESTADORES", color = CyberColorsV3.ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.items.forEach { provider ->
                        Text(provider.title, color = Color.White)
                    }
                }
            }
            is BubbleSection.Filters -> {
                val isExploration = (LocalContext.current as? androidx.activity.ComponentActivity)?.let { false } ?: false // Fallback
                // Nota: Idealmente pasamos isCategoryExploration a BubbleSectionContent, 
                // pero como no queremos cambiar la firma de la sellada ahora, usamos el contexto de la reacción si es posible.
                // Sin embargo, la forma más limpia es chequear el título o pasar un flag.
                
                Text(section.title.uppercase(), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                
                if (section.title.contains("categorías", ignoreCase = true)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(section.items) { cat ->
                            CategoryExplorationChip(item = cat, onClick = { onActionClick(cat.id) })
                        }
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(section.items) { filter ->
                            ActionChip(item = filter, onClick = { onActionClick(filter.id) })
                        }
                    }
                }
            }
            is BubbleSection.Generic -> {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(section.items) { item ->
                        ActionChip(item = item, onClick = { onActionClick(item.id) })
                    }
                }
            }
            BubbleSection.SortOptions -> {
            }
        }
    }
}

// ==========================================================================================
// --- COMPONENTE: COLA DE BURBUJA COMIC ---
// ==========================================================================================

@Composable
private fun ComicBubbleTail(
    isTop: Boolean,
    modifier: Modifier = Modifier,
    borderBrush: Brush = BentoBorderBrush,
    backgroundColor: Color = Color(0xFF0A0A0F).copy(alpha = 0.99f),
    isSearchStyle: Boolean = false
) {
    val tailWidth = if (isSearchStyle) 64.dp else 32.dp // Más ancha para búsqueda
    val tailHeight = 22.dp

    Canvas(
        modifier = modifier
            .size(tailWidth, tailHeight)
            .graphicsLayer {
                if (!isSearchStyle && isTop) rotationX = 180f
            }
    ) {
        val path = Path().apply {
            if (isSearchStyle) {
                // Cola para búsqueda (apuntando hacia ARRIBA)
                // El lado derecho debe ser recto siguiendo la línea de la burbuja
                moveTo(0f, size.height) // Base Izquierda
                quadraticTo(
                    size.width * 0.4f, size.height * 0.1f,
                    size.width, 0f // Punta Superior Derecha (Donde estarían los ojos)
                )
                lineTo(size.width, size.height) // Baja recto por la derecha
                close()
            } else {
                // Estilo clásico
                moveTo(size.width * 0.2f, 0f)
                quadraticTo(
                    size.width * 0.4f, size.height * 0.05f,
                    size.width * 0.85f, size.height
                )
                quadraticTo(
                    size.width * 0.75f, size.height * 0.25f,
                    size.width, 0f
                )
                close()
            }
        }

        // 1. Relleno con el mismo color que el cuerpo de la burbuja
        drawPath(path = path, color = backgroundColor)

        // 2. Borde (Evitando la línea de unión con la burbuja)
        if (isSearchStyle) {
            val strokePath = Path().apply {
                moveTo(0f, size.height)
                quadraticTo(
                    size.width * 0.4f, size.height * 0.1f,
                    size.width, 0f
                )
                // Lado derecho recto (opcional si quieres borde ahí)
                lineTo(size.width, size.height)
            }
            drawPath(
                path = strokePath,
                brush = borderBrush,
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        } else {
            drawPath(
                path = path,
                brush = borderBrush,
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

// ==========================================================================================
// --- 1. BURBUJA DE ASISTENTE (NOTIFICACIONES / BUDGETS) ---
// ==========================================================================================

/**
 * Burbuja principal para notificaciones, tips y mensajes del asistente.
 * Optimizada para posicionarse sobre el asistente (inferior derecha).
 */
@Composable

fun BoxScope.BeAssistantBubble(
    isVisible: Boolean,
    messages: List<BeMessage>,
    currentIndex: Int,
    onCloseClick: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onActionClick: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = currentIndex) { messages.size }

    LaunchedEffect(currentIndex) {
        if (pagerState.currentPage != currentIndex && currentIndex < messages.size) {
            pagerState.animateScrollToPage(currentIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageSelected(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = (-14).dp, y = (-120).dp)
            .wrapContentSize(unbounded = true)
            .zIndex(150f)
    ) {
        AnimatedVisibility(
            visible = isVisible && messages.isNotEmpty(),
            enter = scaleIn(
                transformOrigin = TransformOrigin(1f, 1f),
                animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)
            ) + fadeIn(),
            exit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .animateContentSize()
                    .padding(top = 16.dp, end = 16.dp, bottom = 6.dp)
            ) {
                Box(modifier = Modifier.wrapContentSize()) {
                    // Cuerpo de la burbuja
                    Box(
                        modifier = Modifier
                            .widthIn(min = 260.dp, max = 320.dp)
                            .wrapContentHeight()
                            .shadow(40.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF0A0A0F).copy(alpha = 0.99f))
                            .background(BentoGlassBrush)
                            // Borde Maverick: Mix de Glass + Tinte de color del mensaje
                            .border(1.5.dp, BentoBorderBrush, RoundedCornerShape(32.dp))
                            .border(
                                1.dp, 
                                messages.getOrNull(currentIndex)?.bubbleColor?.copy(alpha = 0.3f) ?: Color.Transparent, 
                                RoundedCornerShape(32.dp)
                            )
                    ) {
                        Column {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val msg = messages.getOrNull(page)
                                msg?.let {
                                    Column(modifier = Modifier.padding(24.dp)) {
                                        Row(
                                            modifier = if (it.isCentered) Modifier.fillMaxWidth() else Modifier,
                                            horizontalArrangement = if (it.isCentered) Arrangement.Center else Arrangement.Start,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = it.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                                            Text(
                                                text = "BE ASISTENTE",
                                                color = it.bubbleColor.copy(alpha = 0.9f),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 2.sp,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                        
                                        Spacer(Modifier.height(16.dp))
                                        
                                        Text(
                                            text = it.text,
                                            color = Color.White,
                                            textAlign = if (it.isCentered) TextAlign.Center else TextAlign.Start,
                                            modifier = if (it.isCentered) Modifier.fillMaxWidth() else Modifier,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp
                                            )
                                        )

                                        if (it.actionText != null) {
                                            Spacer(Modifier.height(24.dp))
                                            Button(
                                                onClick = { onActionClick() },
                                                modifier = if (it.isCentered) Modifier.align(Alignment.CenterHorizontally) else Modifier,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = it.bubbleColor.copy(alpha = 0.15f),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, it.bubbleColor.copy(alpha = 0.4f)),
                                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(it.actionText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(Modifier.width(8.dp))
                                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (messages.size > 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.04f))
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1} / ${messages.size}",
                                        color = Color.White.copy(0.4f),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }

                    // Botón cerrar
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-12).dp)
                            .size(38.dp)
                            .shadow(16.dp, CircleShape)
                            .background(Color(0xFFE11D48), CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable { onCloseClick() }
                            .zIndex(10f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, "Cerrar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(1.dp))
                ComicBubbleTail(isTop = false, modifier = Modifier.offset(x = (-18).dp))
            }
        }
    }
}

// ==========================================================================================
// --- 2. BURBUJA DE BÚSQUEDA (MODO BUSQUEDA ABIERTA) ---
// ==========================================================================================

/**
 * Burbuja especializada para el modo búsqueda.
 * Se posiciona debajo de la barra de búsqueda y muestra resultados dinámicos.
 */
@Composable
fun BoxScope.BeSearchBubble(
    isVisible: Boolean,
    reaction: BeSearchReaction?,
    onActionClick: (String?) -> Unit,
    onResultClick: (Any) -> Unit = {},
    onCloseClick: () -> Unit = {} // 🔥 Callback para cerrar la burbuja
) {
    if (reaction == null) return

    var showEasterEggImage by remember { mutableStateOf(false) }

    val message = reaction.message ?: return
    val tags = reaction.tags
    val results = reaction.results
    val subSections = reaction.subSections

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxBubbleHeight = screenHeight * 0.5f

    // ==========================================================================================
    // --- SECCIÓN: OVERLAY SORPRESA HUEVO DE PASCUA ---
    // ==========================================================================================
    if (showEasterEggImage) {
        Dialog(
            onDismissRequest = { showEasterEggImage = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showEasterEggImage = false },
                contentAlignment = Alignment.Center
            ) {
                // Contenedor de la imagen con emojis alrededor
                Box(
                    modifier = Modifier
                        .padding(40.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Emojis superiores
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("❤️", fontSize = 20.sp)
                            Text("😍", fontSize = 20.sp)
                            Text("👨‍👩‍👧‍👦", fontSize = 20.sp)
                            Text("💖", fontSize = 20.sp)
                        }

                        Image(
                            painter = painterResource(id = com.example.myapplication.R.drawable.myeasteregg),
                            contentDescription = "Maverick Developers Family",
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        // Emojis inferiores
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨", fontSize = 20.sp)
                            Text("🥰", fontSize = 20.sp)
                            Text("👪", fontSize = 20.sp)
                            Text("💕", fontSize = 20.sp)
                        }
                    }

                    // Botón cerrar X arriba a la derecha
                    IconButton(
                        onClick = { showEasterEggImage = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // ==========================================================================================
    // --- SECCIÓN: ANIMACIÓN DE ENTRADA (ESTILO POP BUBBLE Bouncy) ---
    // ==========================================================================================
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)) + scaleIn(
            initialScale = 0.5f,
            transformOrigin = TransformOrigin(0.95f, 0f), // Sale desde donde está Be
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + slideInVertically(initialOffsetY = { -40 }),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin(0.95f, 0f)
        ) + shrinkVertically(shrinkTowards = Alignment.Top)
    ) {
        // --- CONTENEDOR MAESTRO: Protege la zona de resultados sin tapar la SearchBar ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp) // Despeje exacto para no cubrir la barra de búsqueda
        ) {
            // Contenedor dinámico que sincroniza el Scrim con el tamaño de la tarjeta
            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {

                // ==========================================================================================
                // --- SECCIÓN: CAPA SCRIM (GHOST ABSOLUTE BLACK) ---
                // ==========================================================================================
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            // Estiramos el fondo un poco más hacia abajo para un fundido elegante
                            scaleY = 1.4f
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        }
                        .background(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Black.copy(alpha = 0.98f),
                                0.7f to Color.Black.copy(alpha = 0.99f),
                                1.0f to Color.Transparent
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures { /* Bloqueo de interacciones */ }
                        }
                )

                // ==========================================================================================
                // --- SECCIÓN: CUERPO DE LA BURBUJA (RESULTADOS) ---
                // ==========================================================================================
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .zIndex(180f)
                ) {
                    // Cuerpo de la burbuja con la cola integrada mediante drawBehind (Estilo ChatBubbleGhost)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = maxBubbleHeight)
                            .zIndex(140f)
                            .padding(top = 10.dp) // Espacio exacto para la cola de 10.dp
                            .drawBehind {
                                // COLA COPIADA EXACTAMENTE DE ChatBubbleGhost
                                val path = Path().apply {
                                    // Posicionamos la cola a la derecha (donde está Be en búsqueda)
                                    // size.width - 40.dp para que coincida con el estilo Ghost pero alineado al asistente
                                    val startX = size.width - 40.dp.toPx()
                                    moveTo(startX, 0f)
                                    lineTo(startX + 10.dp.toPx(), (-10).dp.toPx())
                                    lineTo(startX + 20.dp.toPx(), 0f)
                                }
                                // Rellenamos todo el triángulo con el color ElectricCyan (estilo sólido)
                                drawPath(
                                    path, 
                                    CyberColorsV3.ElectricCyan.copy(alpha = 0.8f)
                                )
                            }
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = CyberColorsV3.ElectricCyan.copy(alpha = 0.5f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberColorsV3.RogDarkGray.copy(alpha = 0.98f))
                            .border(
                                1.5.dp,
                                CyberColorsV3.ElectricCyan.copy(alpha = 0.8f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        // ==========================================================================================
                        // --- SECCIÓN: BOTÓN DE CIERRE (STEALTH X) ---
                        // ==========================================================================================
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .zIndex(200f) // Por encima del contenido de la lista
                        ) {
                            BtnCancelStealth(onClick = onCloseClick)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // --- SECCIÓN 1: MENSAJE PRINCIPAL ---
                            item {
                                Column {
                                    Text(
                                        text = message.text,
                                        color = Color.White.copy(alpha = 0.95f),
                                        textAlign = if (message.isCentered) TextAlign.Center else TextAlign.Start,
                                        modifier = if (message.isCentered) Modifier.fillMaxWidth() else Modifier,
                                        style = CyberTypography.MonospaceData.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )

                                    // Tags principales / Acción rápida (Huevo de Pascua y Mensajes manuales)
                                    if (tags.isNotEmpty() || message.actionText != null) {
                                        Spacer(Modifier.height(18.dp))

                                        val displayTags =
                                            if (tags.isEmpty() && message.actionText != null) {
                                                val actionItem = ControlItem(
                                                    label = message.actionText,
                                                    icon = null,
                                                    emoji = message.icon,
                                                    color = message.bubbleColor,
                                                    id = reaction.actionId ?: ""
                                                )

                                                if (reaction.actionId == "show_easter_egg_image" || reaction.actionId == "easter_egg_final") {
                                                    listOf(
                                                        ControlItem(
                                                            "Mira",
                                                            null,
                                                            "👀",
                                                            Color(0xFF22D3EE),
                                                            "show_easter_egg_image"
                                                        ),
                                                        actionItem
                                                    )
                                                } else {
                                                    listOf(actionItem)
                                                }
                                            } else {
                                                tags
                                            }

                                        LazyRow(
                                            modifier = if (message.isCentered) Modifier.fillMaxWidth() else Modifier,
                                            horizontalArrangement = if (message.isCentered) Arrangement.Center else Arrangement.spacedBy(
                                                10.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            items(displayTags) { tag ->
                                                ActionChip(
                                                    item = tag,
                                                    onClick = {
                                                        if (tag.id == "show_easter_egg_image") {
                                                            showEasterEggImage = true
                                                        } else {
                                                            onActionClick(tag.id)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // --- NUEVA SECCIÓN: RENDERIZADO DINÁMICO DE SECCIONES ORGANIZADAS POR EL VM ---
                            items(reaction.organizedSections) { section ->
                                BubbleSectionContent(
                                    section = section,
                                    onResultClick = onResultClick,
                                    onActionClick = onActionClick
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}

// ==========================================================================================
// PREVIEWS
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0D0D12,
    widthDp = 400,  // Ancho normal
    heightDp = 400)
@Composable
fun BeSearchBubblePreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            BeSearchBubble(
                isVisible = true,
                reaction = BeSearchReaction(
                    message = BeMessage(
                        icon = "🔍",
                        text = "He encontrado varios servicios de computación que podrían interesarte.",
                        bubbleColor = Color(0xFF22D3EE)
                    ),
                    tags = listOf(
                        ControlItem(label = "PC GAMER", icon = null, emoji = "🎮", color = Color.Cyan, id = "1"),
                        ControlItem(label = "LAPTOPS", icon = null, emoji = "💻", color = Color.Magenta, id = "2")
                    )
                ),
                onActionClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D12,

    widthDp = 400,  // Ancho normal
    heightDp = 400
)
@Composable
fun BeAssistantBubblePreview() {
    val messages = listOf(
        BeMessage(
            icon = "✨",
            text = "¡Hola! Soy Be. Tengo 2 notificaciones importantes para tus presupuestos pendientes.",
            actionText = "Ver Detalles",
            bubbleColor = Color(0xFF00FFFF)
        )
    )
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            BeAssistantBubble(
                isVisible = true,
                messages = messages,
                currentIndex = 0,
                onCloseClick = {},
                onPageSelected = {},
                onActionClick = {}
            )
        }
    }
}

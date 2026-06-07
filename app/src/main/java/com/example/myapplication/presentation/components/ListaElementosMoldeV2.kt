package com.example.myapplication.presentation.components

// === IMPORTS ===
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.DepthDividerThemedVertical
import com.example.myapplication.presentation.designsystem.components.IOSStylePill
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.MaverickTypography
import com.example.myapplication.presentation.designsystem.components.MaverickStyles
import com.example.myapplication.presentation.designsystem.components.shakeClick
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

// ==================================================================================
// --- SECCIÓN 0: COMPONENTES ATÓMICOS DE CABECERA ---
// ==================================================================================

/**
 * BurbujaCabeceraLista: Réplica exacta del SeparadorFechaPremium.
 * Estilo "Elite Glass" con anatomía táctica y dividers de profundidad.
 */
@Composable
fun BurbujaCabeceraLista(
    modifier: Modifier = Modifier,
    text: String,
    emoji: String? = null,
    icon: ImageVector? = null,
    backgroundColor: Color = Color(0x661E293B),
    accentColor: Color = Color(0x33FFFFFF)
) {
    IOSStylePill(
        modifier = modifier,
        text = "",
        backgroundColor = backgroundColor,
        borderColor = accentColor,
        textColor = Color.White,        
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                // 1. EMOJI O ICONO
                if (emoji != null) {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Text(text = emoji, fontSize = 10.sp)
                    }
                } else if (icon != null) {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // 2. DIVIDER PEQUEÑO (Si hay emoji/icon)
                if (emoji != null || icon != null) {
                    DepthDividerThemedVertical(
                        modifier = Modifier
                            .height(12.dp)
                            .align(Alignment.CenterVertically)
                    )
                }

                // 3. TEXTO
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(
                        text = text.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }
        }
    )
}

/**
 * BotonCabeceraAccion: Botón minimalista basado en referencia visual.
 * Círculo oscuro, borde fino sutil y acento de color solo en el icono.
 */
@Composable
fun BotonCabeceraAccion(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    color: Color = MaverickColors.NeonCyan,
    collapseFraction: Float = 0f
) {
    val size by animateDpAsState(
        targetValue = if (collapseFraction > 0.7f) 28.dp else 32.dp,
        label = "ButtonSize"
    )
    val iconSize by animateDpAsState(
        targetValue = if (collapseFraction > 0.7f) 14.dp else 16.dp,
        label = "IconSize"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF1E293B).copy(alpha = 0.6f)) // Fondo oscuro minimalista
            .border(0.8.dp, Color.White.copy(alpha = 0.15f), CircleShape) // Borde fino sutil
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color, // Acento solo en el icono
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * BotonToggleEmoji: Botón toggle minimalista.
 */
@Composable
fun BotonToggleEmoji(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onToggle: () -> Unit,
    activeEmoji: String,
    inactiveIcon: ImageVector,
    activeColor: Color = MaverickColors.AcidGreen,
    collapseFraction: Float = 0f
) {
    val size by animateDpAsState(
        targetValue = if (collapseFraction > 0.7f) 28.dp else 32.dp,
        label = "ButtonSize"
    )
    val contentSize by animateFloatAsState(
        targetValue = if (collapseFraction > 0.7f) 14f else 16f,
        label = "ContentSize"
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .border(
                width = 0.8.dp,
                color = if (isActive) activeColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                shape = CircleShape
            )
            .shakeClick { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Text(text = activeEmoji, fontSize = contentSize.sp)
        } else {
            Icon(
                modifier = Modifier.size(contentSize.dp),
                imageVector = inactiveIcon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * BotonFlechaAbajo: Botón especializado para despliegues.
 */
@Composable
fun BotonFlechaAbajo(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    collapseFraction: Float = 0f,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    BotonCabeceraAccion(
        modifier = modifier.rotate(rotation),
        onClick = onClick,
        icon = MaverickIcons.ChevronDown,
        color = Color.White.copy(alpha = 0.8f),
        collapseFraction = collapseFraction
    )
}

/**
 * Acciones de Cabecera Predefinidas (Android 16 M3 Style)
 */
object BotonesCabecera {
    @Composable
    fun Nuevo(collapseFraction: Float = 0f, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Add, color = MaverickColors.AcidGreen, collapseFraction = collapseFraction)
    @Composable
    fun Editar(collapseFraction: Float = 0f, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Edit, color = MaverickColors.NeonCyan, collapseFraction = collapseFraction)
    @Composable
    fun Borrar(collapseFraction: Float = 0f, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Delete, color = MaverickColors.DeepRed, collapseFraction = collapseFraction)
    @Composable
    fun Eliminar(collapseFraction: Float = 0f, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Delete, color = MaverickColors.DeepRed, collapseFraction = collapseFraction)
    @Composable
    fun Cerrar(collapseFraction: Float = 0f, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Close, color = Color.Gray, collapseFraction = collapseFraction)
    @Composable
    fun Limpiar(collapseFraction: Float = 0f, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Refresh, color = MaverickColors.MagentaNeon, collapseFraction = collapseFraction)
    @Composable
    fun Filtro(collapseFraction: Float = 0f, isActive: Boolean = false, onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.FilterList, color = if (isActive) MaverickColors.ElectricCyan else Color.White.copy(alpha = 0.6f), collapseFraction = collapseFraction)
}

/**
 * ContadorResultadosElite: Componente atómico M3 Android 16 Style.
 * Muestra el número arriba y el helper abajo, con escalado dinámico.
 */
@Composable
fun ContadorResultadosElite(
    modifier: Modifier = Modifier,
    count: Int,
    collapseFraction: Float = 0f,
    accentColor: Color = MaverickColors.ElectricCyan
) {
    val numberFontSize by animateFloatAsState(
        targetValue = if (collapseFraction < 0.6f) 22f else 14f,
        label = "CounterNumberSize"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = count.toString().padStart(2, '0'),
            style = MaverickTypography.HeaderTitle.copy(
                fontSize = numberFontSize.sp,
                color = accentColor,
                fontWeight = FontWeight.Black,
                lineHeight = (numberFontSize * 0.8f).sp
            )
        )
        Text(
            text = "RESULT",
            style = MaverickTypography.HeaderSubtitle.copy(
                fontSize = 6.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        )
    }
}

/**
 * PerfilEmpresa: Modelo de datos para multi-perfil / multi-empresa.
 */
data class PerfilEmpresa(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val colorAcento: Color = MaverickColors.ElectricCyan,
    val emoji: String? = null,
    val photoUrl: Any? = null, // 🔥 [NUEVO] Soporta URL, URI o Drawable
    val unreadCount: Int = 0
)

/**
 * BurbujaPerfilElite: Representación táctica de una empresa/perfil.
 * Estilo circular con animación de escala y glow adaptativo.
 */
@Composable
fun BurbujaPerfilElite(
    modifier: Modifier = Modifier,
    perfil: PerfilEmpresa,
    isSelected: Boolean,
    collapseFraction: Float = 0f,
    onClick: () -> Unit
) {
    val size by animateDpAsState(
        targetValue = if (isSelected) {
            if (collapseFraction > 0.7f) 32.dp else 36.dp
        } else {
            if (collapseFraction > 0.7f) 24.dp else 28.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ProfileSize"
    )
    
    val borderColor = if (isSelected) perfil.colorAcento else Color.White.copy(alpha = 0.15f)
    val glowAlpha by animateFloatAsState(targetValue = if (isSelected) 0.5f else 0f, label = "ProfileGlow")

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isSelected) perfil.colorAcento.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.4f))
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = borderColor,
                shape = CircleShape
            )
            .drawBehind {
                if (isSelected) {
                    drawCircle(
                        color = perfil.colorAcento,
                        radius = (size.toPx() / 2) + 2.dp.toPx(),
                        alpha = glowAlpha * 0.15f,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .shakeClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (perfil.photoUrl != null) {
            AsyncImage(
                model = perfil.photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isSelected) 1f else 0.6f
            )
        } else if (perfil.emoji != null) {
            Text(text = perfil.emoji, fontSize = if (isSelected) 14.sp else 10.sp)
        } else {
            Text(
                text = perfil.iniciales.uppercase(),
                style = MaverickTypography.HeaderTitle.copy(
                    fontSize = if (isSelected) 11.sp else 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            )
        }

        // 🔥 BADGE DE NO LEÍDOS (Elite Style)
        if (perfil.unreadCount > 0 && collapseFraction < 0.7f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaverickColors.DeepRed)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (perfil.unreadCount > 9) "+" else perfil.unreadCount.toString(),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * SelectorPerfilesElite: Fila horizontal de burbujas de perfil.
 */
@Composable
fun SelectorPerfilesElite(
    modifier: Modifier = Modifier,
    perfiles: List<PerfilEmpresa>,
    perfilSeleccionadoId: String?,
    collapseFraction: Float = 0f,
    onPerfilClick: (PerfilEmpresa) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        perfiles.forEach { perfil ->
            val isSelected = perfil.id == perfilSeleccionadoId
            // Animación de visibilidad: Solo el seleccionado cuando está colapsado
            AnimatedVisibility(
                visible = collapseFraction < 0.8f || isSelected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                BurbujaPerfilElite(
                    perfil = perfil,
                    isSelected = isSelected,
                    collapseFraction = collapseFraction,
                    onClick = { onPerfilClick(perfil) }
                )
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN 1: CONFIGURACIÓN Y ESTADOS ---
// ==================================================================================

/**
 * CabeceraDinamicaMoldeV2: Componente visual premium colapsable.
 * Extraído de ListaMoldeV2 para uso quirúrgico en otras pantallas (ej: Home).
 */
@Composable
fun CabeceraDinamicaMoldeV2(
    modifier: Modifier = Modifier,
    titulo: String,
    subtitulo: String? = null,
    itemCount: Int? = null,
    perfiles: List<PerfilEmpresa> = emptyList(),
    perfilSeleccionadoId: String? = null,
    onPerfilSelected: (PerfilEmpresa) -> Unit = {},
    collapseFraction: Float, // 0f = Expandido, 1f = Colapsado
    height: Dp,
    accentColor: Color = MaverickColors.ElectricCyan,
    backgroundBrush: Brush = Brush.verticalGradient(
        listOf(
            MaverickColors.V2DeepVoid,
            MaverickColors.ROG_Dark_Bg
        )
    ),
    acciones: @Composable (RowScope.() -> Unit)? = null,
    filtros: @Composable (RowScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundBrush)
            .drawBehind {
                val strokeWidth = 1.6.dp.toPx()
                // Path con laterales y cortes (16dp), sin borde inferior
                val path = Path().apply {
                    moveTo(0f, size.height) // Empezar en esquina inferior izquierda
                    lineTo(0f, 16.dp.toPx()) // Subir recto por el lateral izquierdo
                    lineTo(16.dp.toPx(), 0f) // Corte diagonal superior izquierdo
                    lineTo(size.width - 16.dp.toPx(), 0f) // Línea superior
                    lineTo(size.width, 16.dp.toPx()) // Corte diagonal superior derecho
                    lineTo(size.width, size.height) // Bajar recto por el lateral derecho
                }

                // C. LUMINOSIDAD GRADIENTE MAVERICK (Borde neón dinámico sincronizado con BarraCabezera)
                val borderGradient = Brush.horizontalGradient(
                    0.0f to accentColor.copy(alpha = 0.05f),
                    0.2f to accentColor,
                    0.5f to MaverickColors.ElectricCyan,
                    0.8f to accentColor,
                    1.0f to accentColor.copy(alpha = 0.05f)
                )

                // D. LÍNEA SÓLIDA PRINCIPAL
                drawPath(
                    path = path,
                    brush = borderGradient,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )

                // E. GLOW TENUE ADAPTATIVO (Resplandor de borde)
                drawPath(
                    path = path,
                    brush = borderGradient,
                    style = Stroke(
                        width = strokeWidth * 3f,
                        cap = StrokeCap.Round
                    ),
                    alpha = 0.12f
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- SLOT IZQUIERDO: CONTADOR + TÍTULO/SUBTÍTULO (JUSTIFICADO A LA IZQUIERDA) ---
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    if (itemCount != null) {
                        ContadorResultadosElite(count = itemCount, collapseFraction = collapseFraction, accentColor = accentColor)
                        
                        DepthDividerThemedVertical(
                            modifier = Modifier
                                .height(20.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (collapseFraction < 0.85f) {
                            if (subtitulo != null && collapseFraction < 0.6f) {
                                Text(
                                    text = subtitulo.uppercase(),
                                    style = MaverickTypography.HeaderSubtitle.copy(
                                        fontSize = 8.sp,
                                        color = Color.Gray,
                                        letterSpacing = 1.sp
                                    ),
                                    textAlign = TextAlign.Start,
                                    maxLines = 1,
                                    modifier = Modifier.graphicsLayer { alpha = (1f - collapseFraction * 2f).coerceIn(0f, 1f) }
                                )
                            }
                            AutoSizeText(
                                text = titulo.uppercase(),
                                style = MaverickTypography.HeaderTitle.copy(
                                    fontSize = (16 - (2 * collapseFraction)).sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                ),
                                textAlign = TextAlign.Start,
                                maxLines = 1
                            )
                        }
                    }
                }

                // --- SLOT DERECHO: PERFILES Y ACCIONES (CON DIVIDER INTERMEDIO) ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (perfiles.isNotEmpty()) {
                        SelectorPerfilesElite(
                            perfiles = perfiles,
                            perfilSeleccionadoId = perfilSeleccionadoId,
                            collapseFraction = collapseFraction,
                            onPerfilClick = onPerfilSelected
                        )
                        
                        // DIVIDER A LA DERECHA DE LAS BURBUJAS
                        DepthDividerThemedVertical(
                            modifier = Modifier
                                .height(20.dp)
                                .padding(horizontal = 10.dp)
                        )
                    }

                    if (acciones != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            acciones()
                        }
                    }
                }
            }

            // --- NUEVO: SLOT DE FILTROS (Se oculta al colapsar) ---
            if (filtros != null && collapseFraction < 0.4f) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 6.dp)
                        .graphicsLayer {
                            alpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    filtros()
                }
            }
        }

        // --- DIVIDER DE PROFUNDIDAD INFERIOR (Biselado DepthDivider) ---
        DepthDividerHorizontal(
            modifier = Modifier.align(Alignment.BottomCenter),
            thickness = 0.8.dp,
            shadowColor = Color.Black.copy(alpha = 0.8f),
            highlightColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

/**
 * ListaMoldeV2: Contenedor visual evolucionado con cabecera colapsable y adaptable.
 */
@Composable
fun ListaMoldeV2(
    modifier: Modifier = Modifier,
    titulo: String = "SISTEMA DE RESULTADOS",
    subtitulo: String? = null,
    acciones: @Composable (RowScope.(fraction: Float) -> Unit)? = null,
    filtros: @Composable (RowScope.() -> Unit)? = null,
    itemCount: Int? = null,
    perfiles: List<PerfilEmpresa> = emptyList(),
    initialPerfilId: String? = null,
    onPerfilSelected: (PerfilEmpresa) -> Unit = {},
    accentColor: Color = MaverickColors.ElectricCyan,
    customMaxHeaderHeight: Dp = if (filtros != null) 100.dp else 64.dp,
    customMinHeaderHeight: Dp = 40.dp,
    state: LazyListState = rememberLazyListState(),
    containerColor: Color = MaverickColors.EliteSurface,
    content: LazyListScope.(perfil: PerfilEmpresa?) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val maxHeaderHeightPx = with(density) { customMaxHeaderHeight.toPx() }
    val minHeaderHeightPx = with(density) { customMinHeaderHeight.toPx() }
    
    var headerHeightPx by remember { mutableFloatStateOf(maxHeaderHeightPx) }

    // Estado para el Pager de perfiles
    val pagerState = if (perfiles.isNotEmpty()) {
        val initialPage = remember(initialPerfilId) {
            val index = perfiles.indexOfFirst { it.id == initialPerfilId }
            if (index != -1) index else 0
        }
        rememberPagerState(
            initialPage = initialPage,
            pageCount = { perfiles.size }
        )
    } else null

    // Sincronización Pager -> Selección externa
    if (pagerState != null) {
        LaunchedEffect(pagerState.currentPage) {
            onPerfilSelected(perfiles[pagerState.currentPage])
        }
    }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newHeight = headerHeightPx + delta
                val previousHeight = headerHeightPx
                headerHeightPx = newHeight.coerceIn(minHeaderHeightPx, maxHeaderHeightPx)
                val consumed = headerHeightPx - previousHeight
                return Offset(0f, consumed)
            }
        }
    }

    val collapseFraction = if (maxHeaderHeightPx == minHeaderHeightPx) 1f 
                          else ((maxHeaderHeightPx - headerHeightPx) / (maxHeaderHeightPx - minHeaderHeightPx)).coerceIn(0f, 1f)
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(containerColor)
            .nestedScroll(nestedScrollConnection)
    ) {
        if (pagerState != null) {
            // MODO MULTI-EMPRESA CON PAGER
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().zIndex(0f),
                contentPadding = PaddingValues(top = customMaxHeaderHeight + 8.dp, bottom = 20.dp),
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top
            ) { page ->
                LazyColumn(
                    // Cada página tiene su propio scroll para evitar conflictos
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content(perfiles[page])
                }
            }
        } else {
            // MODO ESTÁNDAR
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize().zIndex(0f),
                contentPadding = PaddingValues(top = customMaxHeaderHeight + 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content(null)
            }
        }

        // --- SOMBRA PROYECTADA (Efecto 3D de Elevación) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .offset(y = headerHeightDp)
                .zIndex(1f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.95f), Color.Transparent)
                    )
                )
        )

        CabeceraDinamicaMoldeV2(
            modifier = Modifier.zIndex(2f),
            titulo = titulo,
            subtitulo = subtitulo,
            itemCount = itemCount,
            perfiles = perfiles,
            perfilSeleccionadoId = pagerState?.let { perfiles[it.currentPage].id },
            onPerfilSelected = { perfil ->
                scope.launch {
                    pagerState?.animateScrollToPage(perfiles.indexOf(perfil))
                }
            },
            collapseFraction = collapseFraction,
            height = headerHeightDp,
            accentColor = accentColor,
            acciones = acciones?.let { { it(collapseFraction) } },
            filtros = filtros
        )
    }
}

// ==================================================================================
// --- SECCIÓN 3: PREVIEWS ---
// ==================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun PreviewListaMoldeV2Redisenio() {
    MyApplicationTheme {
        var isToggled by remember { mutableStateOf(false) }
        var isExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
        ) {
            val perfilesMock = listOf(
                PerfilEmpresa("1", "Maverick Corp", "MC", MaverickColors.ElectricCyan),
                PerfilEmpresa("2", "Cyberdyne", "CD", MaverickColors.NeonCyan),
                PerfilEmpresa("3", "Stark Ind", "SI", MaverickColors.AcidGreen)
            )
            var perfilActual by remember { mutableStateOf(perfilesMock[0]) }

            ListaMoldeV2(
                titulo = "Presupuestos de Obra",
                subtitulo = perfilActual.nombre,
                perfiles = perfilesMock,
                onPerfilSelected = { perfilActual = it },
                itemCount = 24,
                acciones = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        BotonToggleEmoji(
                            isActive = isToggled,
                            onToggle = { isToggled = !isToggled },
                            activeEmoji = "🔥",
                            inactiveIcon = MaverickIcons.Info
                        )
                        BotonesCabecera.Limpiar {}
                        BotonesCabecera.Nuevo {}
                        BotonFlechaAbajo(
                            isExpanded = isExpanded,
                            onClick = { isExpanded = !isExpanded }
                        )
                    }
                }
            ) { perfil ->
                items(20) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 16.dp)
                            .background(Color.White.copy(alpha = 0.03f), CutCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), CutCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            "Elemento de ${perfil?.nombre ?: "Sistema"} #${index + 1}",
                            color = Color.White,
                            style = MaverickTypography.BodyText
                        )
                    }
                }
            }
        }
    }
}

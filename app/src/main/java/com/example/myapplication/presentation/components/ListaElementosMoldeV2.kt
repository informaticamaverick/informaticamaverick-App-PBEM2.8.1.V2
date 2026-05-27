package com.example.myapplication.presentation.components

// === IMPORTS ===
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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

) {
    Box(
        modifier = modifier
            .size(32.dp)
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
            modifier = Modifier.size(16.dp)
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

) {
    Box(
        modifier = modifier
            .size(32.dp)
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
            Text(text = activeEmoji, fontSize = 16.sp)
        } else {
            Icon(
                modifier = Modifier.size(16.dp),
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
        color = Color.White.copy(alpha = 0.8f)

    )
}

/**
 * Acciones de Cabecera Predefinidas (Android 16 M3 Style)
 */
object BotonesCabecera {
    @Composable
    fun Nuevo(onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Add, color = MaverickColors.AcidGreen)
    @Composable
    fun Editar(onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Edit, color = MaverickColors.NeonCyan)
    @Composable
    fun Borrar(onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Delete, color = MaverickColors.DeepRed)
    @Composable
    fun Eliminar(onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Delete, color = MaverickColors.DeepRed)
    @Composable
    fun Cerrar(onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Close, color = Color.Gray)
    @Composable
    fun Limpiar(onClick: () -> Unit) = BotonCabeceraAccion(onClick = onClick, icon = MaverickIcons.Refresh, color = MaverickColors.MagentaNeon)
}

/**
 * ContadorResultadosElite: Componente atómico M3 Android 16 Style.
 * Muestra un helper "RESULTADOS" arriba y el número abajo.
 */
@Composable
fun ContadorResultadosElite(
    modifier: Modifier = Modifier,
    count: Int,
    accentColor: Color = MaverickColors.ElectricCyan
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 6.dp)
    ) {
        androidx.compose.material3.Text(
            text = "RESULT",
            style = MaverickTypography.HeaderSubtitle.copy(
                fontSize = 6.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        )
        androidx.compose.material3.Text(
            text = count.toString().padStart(2, '0'),
            style = MaverickTypography.HeaderTitle.copy(
                fontSize = 18.sp,
                color = accentColor,
                fontWeight = FontWeight.Black,
                lineHeight = 13.sp
            )
        )
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
    emoji: String? = null,
    compactInfo: String = "",
    itemCount: Int? = null,
    collapseFraction: Float, // 0f = Expandido, 1f = Colapsado
    height: Dp,
    filtrosActivos: List<String> = emptyList(),
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
                    .padding(horizontal = 8.dp), // Ajustado para dar espacio a los bordes
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- SLOT IZQUIERDO: EMOJI / FILTROS / CONTADOR ---
                Row(
                    modifier = Modifier.weight(1.2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Siempre visible: El contador de resultados
                    if (itemCount != null) {
                        ContadorResultadosElite(count = itemCount, accentColor = accentColor)
                        
                        // Divider solo si hay algo más a la derecha y estamos expandidos
                        if (collapseFraction < 0.6f && (emoji != null || filtrosActivos.isNotEmpty())) {
                            DepthDividerThemedVertical(
                                modifier = Modifier
                                    .height(16.dp)
                                    .padding(horizontal = 4.dp)
                            )
                        } else if (collapseFraction >= 0.6f) {
                            // Espaciador mínimo cuando está contraído
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }

                    // Estos elementos solo se ven cuando la cabecera está expandida
                    if (collapseFraction < 0.6f) {
                        if (emoji != null) {
                            Text(text = emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        
                        filtrosActivos.take(1).forEach { filtro ->
                            BurbujaCabeceraLista(
                                text = filtro, 
                                backgroundColor = Color.Black.copy(alpha = 0.4f),
                                accentColor = MaverickColors.NeonCyan.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // --- CENTRO: TÍTULO DINÁMICO (BLANCO PURO + HELPER GRIS) ---
                Box(
                    modifier = Modifier.weight(3f),
                    contentAlignment = Alignment.Center
                ) {
                    if (collapseFraction < 0.85f) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (subtitulo != null && collapseFraction < 0.4f) {
                                Text(
                                    text = subtitulo.uppercase(),
                                    style = MaverickTypography.HeaderSubtitle.copy(
                                        fontSize = 7.sp,
                                        color = Color.Gray, // HELPER GRIS
                                        letterSpacing = 1.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                            AutoSizeText(
                                text = titulo.uppercase(),
                                style = MaverickTypography.HeaderTitle.copy(
                                    fontSize = 15.sp,
                                    color = Color.White, // BLANCO PURO
                                    letterSpacing = 0.5.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        BurbujaCabeceraLista(
                            text = compactInfo,
                            icon = MaverickIcons.Info,
                            accentColor = MaverickColors.GoldPremium
                        )
                    }
                }

                // --- SLOT DERECHO: ACCIONES ---
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
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
    emoji: String? = null,
    acciones: @Composable (RowScope.() -> Unit)? = null,
    filtros: @Composable (RowScope.() -> Unit)? = null,
    compactInfo: String = "",
    itemCount: Int? = null,
    filtrosActivos: List<String> = emptyList(),
    accentColor: Color = MaverickColors.ElectricCyan,
    customMaxHeaderHeight: Dp = if (filtros != null) 90.dp else 42.dp,
    customMinHeaderHeight: Dp = 40.dp,
    state: LazyListState = rememberLazyListState(),
    containerColor: Color = MaverickColors.EliteSurface,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val maxHeaderHeightPx = with(density) { customMaxHeaderHeight.toPx() }
    val minHeaderHeightPx = with(density) { customMinHeaderHeight.toPx() }
    
    var headerHeightPx by remember { mutableFloatStateOf(maxHeaderHeightPx) }
    
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
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize().zIndex(0f),
            contentPadding = PaddingValues(top = customMaxHeaderHeight + 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
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
            emoji = emoji,
            compactInfo = compactInfo,
            itemCount = itemCount,
            filtrosActivos = filtrosActivos,
            collapseFraction = collapseFraction,
            height = headerHeightDp,
            accentColor = accentColor,
            acciones = acciones,
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
            ListaMoldeV2(
                titulo = "Presupuestos de Obra",
                subtitulo = "Módulo de Ventas",
                emoji = "⚡",
                compactInfo = "Hoy: 20 May",
                itemCount = 24,
                filtrosActivos = listOf("Urgente"),
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
            ) {
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
                            "Elemento de Lista #${index + 1}",
                            color = Color.White,
                            style = MaverickTypography.BodyText
                        )
                    }
                }
            }
        }
    }
}

package com.example.myapplication.ui.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.core.dominio.modelos.SuperCategoriaDominio
import com.example.myapplication.uishared.ui.components.*
import com.example.myapplication.ui.componentes.sistema.DepthDividerHorizontal
import com.example.myapplication.ui.estilos.ClienteTheme
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.ui.componentes.be.modelos.EmocionBe
import com.example.myapplication.ui.componentes.sistema.menu.v3.MoldeMenuArmadorV3

/**
 * TarjetaCategoria2.kt
 * Propósito: Definir las tarjetas de categoría y supercategoría del ecosistema Maverick.
 * LEY #10: Anatomía de Pantalla y Previews.
 * LEY #11: CuatroOjos - Paridad Visual y Bloqueo de Escalado.
 */

@Composable
fun FavoritePinBadge(
    isFavorite: Boolean, 
    modifier: Modifier = Modifier,
    isIndirect: Boolean = false
) {
    if (isFavorite || isIndirect) {
        Box(
            modifier = modifier
                .size(32.dp)
                .background(
                    if (isFavorite) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            TextCompacto(
                text = "📌",
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer {
                    if (isIndirect && !isFavorite) {
                        alpha = 0.5f
                    }
                },
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

// ==========================================================================================
// ------------------------ TARJETA CATEGORIA COMPACTA ELITE V3 ----------------------------
// ==========================================================================================
@Composable
fun CompactCategoryCard(
    item: CategoriaDominio, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    onInfoClick: (CategoriaDominio) -> Unit = {}, 
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean, String?, String?) -> Unit = { _, _, _ -> },
    showSuperCategoryLabel: Boolean = false,
    isSuperCategoryFavorite: Boolean = false,
    estaSeleccionado: Boolean = false,
    modoMultiseleccionActivo: Boolean = false,
    alHacerLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isInfoVisible: Boolean = false, 
    onDismissInfo: () -> Unit = {}    
) {
    BloquearEscalaFuente(minFontScale = 0.9f, maxFontScale = 1.1f) {
        val haptic = LocalHapticFeedback.current
        val interactionSource = remember { MutableInteractionSource() }

        val currentOnClick by rememberUpdatedState(onClick)
        val currentOnLongClick by rememberUpdatedState(alHacerLongClick)

        val colorAcento = remember(item.color) { Color(item.color) }

        MoldeMultiSeleccion(
            estaSeleccionado = estaSeleccionado,
            modoMultiseleccionActivo = modoMultiseleccionActivo,
            colorAcento = colorAcento,
            radioCurvatura = 8.dp,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnLongClick()
                            },
                            onTap = { 
                                if (modoMultiseleccionActivo) {
                                    currentOnLongClick() 
                                } else {
                                    currentOnClick() 
                                }
                            }
                        )
                    }
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SharedPalette.EliteSurface),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // --- 1. CUERPO MATE CON ACENTO SUTIL ---
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            colorAcento.copy(alpha = 0.12f),
                                            SharedPalette.EliteSurface
                                        )
                                    )
                                )
                        )
                        
                        // --- 2. CABECERA TRANSPARENTE (BADGES) ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .align(Alignment.TopEnd),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showSuperCategoryLabel) {
                                TextCompacto(
                                    text = item.superCategoria.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colorAcento.copy(alpha = 0.8f),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            if (item.esNueva) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SharedPalette.ElectricCyan)
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            
                            FavoritePinBadge(
                                isFavorite = isShortcut || isSuperCategoryFavorite,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(Modifier.width(6.dp))
                            
                            Box {
                                CategoryCardInfoButton { onInfoClick(item) }

                                MoldeMenuArmadorV3(
                                    expanded = isInfoVisible,
                                    onDismissRequest = onDismissInfo,
                                    isCenteredOnScreen = true,
                                    autoArrow = true,
                                    verticalOffset = (-8).dp,
                                    anchoMaximo = 260.dp
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        TextCompacto(
                                            text = "SOBRE ESTE RUBRO",
                                            color = SharedPalette.ElectricCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.5.sp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.descripcion.ifEmpty { "Sin descripción disponible." },
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            textAlign = TextAlign.Justify
                                        )
                                    }
                                }
                            }
                        }

                        // --- 3. EMOJI CENTRAL ---
                        CategoryCardEmoji(item.icono)

                        // --- 4. FOOTER DARK GLASS (SIMULACIÓN DE ALTO RENDIMIENTO) ---
                        CategoryCardFooter(item.nombre)
                    }
                }
            }
        }
    }
}

// ==========================================================================================
// ------------------------ TARJETA CATEGORIA TÁCTICA (URGENCIAS) ---------------------------
// ==========================================================================================

/**
 * TarjetaCategoriaTactica: Versión ultra-compacta diseñada para el Radar de Urgencias.
 * [DISEÑO]: Réplica exacta de CompactCategoryCard con divider profundo.
 */
@Composable
fun TarjetaCategoriaTactica(
    item: CategoriaDominio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorAcento = remember(item.color) { Color(item.color) }
    
    Box(
        modifier = modifier
            .width(110.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SharedPalette.EliteSurface),
            border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.4f)), // 🔥 [FIX]: Borde táctico con acento
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // --- 1. FONDO CON ACENTO (PARIDAD ELITE) ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colorAcento.copy(alpha = 0.15f), // 🔥 [FIX]: Intensidad Elite
                                    SharedPalette.EliteSurface
                                )
                            )
                        )
                )

                // --- 2. EMOJI CENTRAL (REESCALADO TÁCTICO) ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 44.dp), 
                    contentAlignment = Alignment.Center
                ) {
                    TextCompacto(
                        text = item.icono,
                        fontSize = 44.sp,
                        modifier = Modifier
                            .offset(y = 4.dp)
                            .graphicsLayer { alpha = 0.3f }
                            .drawWithContent {
                                drawContent()
                                drawRect(color = Color.Black, blendMode = BlendMode.SrcIn)
                            }
                    )
                    TextCompacto(text = item.icono, fontSize = 44.sp)
                }

                // --- 3. FOOTER DARK GLASS CON DIVIDER PROFUNDO ---
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Divider Táctico (Separa el cuerpo del footer)
                    com.example.myapplication.ui.componentes.sistema.DepthDividerHorizontal(
                        shadowColor = Color.Black.copy(alpha = 0.8f),
                        highlightColor = Color.White.copy(alpha = 0.05f)
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextCompacto(
                            text = item.nombre.uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CategoryCardEmoji(icon: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 14.dp, bottom = 48.dp), 
        contentAlignment = Alignment.Center
    ) {
        TextCompacto(
            text = icon,
            fontSize = 72.sp,
            modifier = Modifier
                .offset(y = 6.dp)
                .graphicsLayer { alpha = 0.5f }
                .drawWithContent {
                    drawContent()
                    drawRect(color = Color.Black, blendMode = BlendMode.SrcIn)
                }
        )
        TextCompacto(text = icon, fontSize = 72.sp)
    }
}

@Composable
private fun CategoryCardInfoButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(22.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            TextCompacto("!", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BoxScope.CategoryCardFooter(name: String) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black.copy(alpha = 0.6f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Divider Táctico Profundo (Paridad Elite v2026)
        DepthDividerHorizontal(
            shadowColor = Color.Black.copy(alpha = 0.8f),
            highlightColor = Color.White.copy(alpha = 0.05f)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TextCompactoAutoFit( 
                text = name.uppercase(),
                modifier = Modifier.padding(horizontal = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                softWrap = true
            )
        }
    }
}

// ==========================================================================================
// ------------------------ TARJETA SUPERCATEGORIA BENTO ELITE -----------------------------
// ==========================================================================================
@Composable
fun BentoSuperCategoryCard(
    superCategory: SuperCategoriaDominio, 
    emoji: String, 
    height: Dp,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    isShortcut: Boolean = false,
    onManageShortcut: (Boolean, String?, String?) -> Unit = { _, _, _ -> },
    estaSeleccionado: Boolean = false,
    modoMultiseleccionActivo: Boolean = false,
    alHacerLongClick: () -> Unit = {}
) {
    BloquearEscalaFuente(minFontScale = 0.9f, maxFontScale = 1.1f) {
        val haptic = LocalHapticFeedback.current
        val interactionSource = remember { MutableInteractionSource() }

        val currentOnClick by rememberUpdatedState(onClick)
        val currentOnLongClick by rememberUpdatedState(alHacerLongClick)

        val baseColor = remember(superCategory.color) { Color(superCategory.color) }

        MoldeMultiSeleccion(
            estaSeleccionado = estaSeleccionado,
            modoMultiseleccionActivo = modoMultiseleccionActivo,
            colorAcento = baseColor,
            radioCurvatura = 8.dp,
            mostrarTilde = false, // 🔥 [FIX]: Manually placed in Header for precise alignment
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(top = 8.dp, start = 2.dp, end = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .drawBehind { drawBentoCardShadow(estaSeleccionado) }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentOnLongClick()
                                }, 
                                onTap = { 
                                    if (modoMultiseleccionActivo) {
                                        currentOnLongClick()
                                    } else {
                                        currentOnClick() 
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(8.dp), 
                    border = BorderStroke(1.5.dp, baseColor.copy(alpha = 0.4f)), 
                    color = Color(0xFF1A1C1E)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        BentoCardBackground(baseColor)
                        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            BentoCardHeader(
                                emoji = emoji, 
                                superCategory = superCategory,
                                isFavorite = isShortcut || superCategory.tieneFavoritos,
                                modoMultiseleccionActivo = modoMultiseleccionActivo,
                                estaSeleccionado = estaSeleccionado,
                                colorAcento = baseColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            DepthDividerHorizontal(shadowColor = Color.Black.copy(alpha = 0.5f), highlightColor = Color.White.copy(alpha = 0.05f))
                            BentoCardFooter(superCategory.titulo)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoCardBackground(accentColor: Color = Color.White) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(accentColor.copy(alpha = 0.05f), Color.Black.copy(0.85f)))
        ))
        Box(modifier = Modifier.fillMaxSize().blur(radius = 16.dp).alpha(0.15f).background(accentColor.copy(alpha = 0.1f)))
    }
}

@Composable
private fun BentoCardHeader(
    emoji: String, 
    superCategory: SuperCategoriaDominio,
    isFavorite: Boolean,
    modoMultiseleccionActivo: Boolean = false,
    estaSeleccionado: Boolean = false,
    colorAcento: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.Start
    ) { 
        // --- LADO IZQUIERDO: EMOJI CON GLOW ---
        Box(contentAlignment = Alignment.Center) {
            TextCompacto(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier
                    .offset(x = 3.dp, y = 3.dp)
                    .graphicsLayer { alpha = 0.9f }
                    .drawWithContent {
                        drawContent()
                        drawRect(color = Color.Black, blendMode = BlendMode.SrcIn)
                    }
                    .blur(3.dp)
            )
            TextCompacto(text = emoji, fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- LADO DERECHO: 2 ROWS DE HERRAMIENTAS Y INFO ---
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            // ROW 1: Badges (Budgets), Pin, Expansión / Círculo Selección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

                // Pin de Favorito (SÓLO UNO, en esta fila)
                if (isFavorite) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextCompacto(
                        text = "📌",
                        fontSize = 16.sp,
                        modifier = Modifier.graphicsLayer {
                            val s = if (estaSeleccionado) 1.1f else 1.0f
                            scaleX = s
                            scaleY = s
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Indicador de Acción: Círculo (Multiselección) o Flecha (Expansión)
                if (modoMultiseleccionActivo) {
                    IndicadorSeleccion(
                        estaSeleccionado = estaSeleccionado,
                        colorAcento = colorAcento
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ROW 2: Etiqueta de Rubros
            Surface(
                color = Color.Black.copy(alpha = 0.4f), // 🔥 [FIX]: Fondo más oscuro y discreto
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextCompacto(
                        text = superCategory.totalItems.toString(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    TextCompacto(
                        text = "RUBROS",
                        color = Color.Gray.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.BentoCardFooter(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f), 
        contentAlignment = Alignment.Center
    ) {
        TextCompactoAutoFit(
            text = title.uppercase(), 
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black, 
                fontSize = 14.sp,
                lineHeight = 16.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

private fun DrawScope.drawBentoCardShadow(isPressed: Boolean) {
    val shadowColor = Color.Black
    val shadowRadius = if (isPressed) 12.dp.toPx() else 8.dp.toPx()
    val offsetY = if (isPressed) 6.dp.toPx() else 4.dp.toPx()
    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply { color = shadowColor.toArgb(); setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb()) }
        canvas.nativeCanvas.drawRoundRect(0f, offsetY, size.width, size.height, 8.dp.toPx(), 8.dp.toPx(), paint)
    }
}

// ==========================================================================================
// --- SECCIÓN: PREVIEWS (MAVERICK ELITE 2026) ---
// ==========================================================================================

@Preview(name = "1. Tarjeta Categoría Compacta", showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewCompactCategoryCard() {
    val mock = CategoriaDominio(id = "OBRAS_CONSTRUC", nombre = "Construcción", icono = "🏗️", idSuperCategoria = "OBRAS", superCategoria = "Obras", esNueva = true, color = 0xFF00FFFF)
    ClienteTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp).width(160.dp)) {
            CompactCategoryCard(item = mock, onClick = {}, isShortcut = false)
        }
    }
}

@Preview(name = "2. Tarjeta Supercategoría Bento", showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewBentoSuperCategoryCard() {
    val mock = SuperCategoriaDominio(id = "AUTO", titulo = "Mecánica Automotriz", icono = "🚗", color = 0xFFFF0032, totalItems = 12)
    ClienteTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp).width(200.dp)) {
            BentoSuperCategoryCard(superCategory = mock, emoji = "🚗", height = 130.dp, onClick = {})
        }
    }
}

@Preview(name = "3. Tarjeta Categoría Táctica", showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewTarjetaCategoriaTactica() {
    val mock = CategoriaDominio(id = "AUTO_AUXILIO", nombre = "Auxilio Mecánico", icono = "🆘", idSuperCategoria = "AUTO", superCategoria = "Servicios Automotores", color = 0xFFFF0032)
    ClienteTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            TarjetaCategoriaTactica(item = mock, onClick = {})
        }
    }
}

@Preview(name = "AUDITORÍA: Tarjetas Activas V3", showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewAuditoriaTarjetasActivas() {
    ClienteTheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFF05070A)),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("TARJETAS ACTIVAS ECOSISTEMA", color = Color.Gray, style = AppTypography.HeaderSubtitle)
            
            // Compacta (Sheet y Listas)
            Box(modifier = Modifier.width(140.dp)) {
                CompactCategoryCard(item = CategoriaDominio(id = "NAT_JARDIN", nombre = "Jardinería", icono = "🌿", color = 0xFF00FF00, idSuperCategoria = "NAT", superCategoria = "Naturaleza"), onClick = {})
            }

            // Bento Super (Home)
            BentoSuperCategoryCard(
                superCategory = SuperCategoriaDominio(id = "NAT", titulo = "Naturaleza", icono = "🌲", color = 0xFF006400, totalItems = 8),
                emoji = "🌲",
                height = 120.dp,
                onClick = {}
            )
        }
    }
}

package com.example.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.CategoryEntity
import com.example.myapplication.presentation.client.SuperCategory
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.ui.draw.drawBehind
import com.example.myapplication.presentation.components.Utilidades.AutoSizeText
import com.example.myapplication.presentation.client.CategoryVisuals
// ==========================================================================================
// ------------------- PIN DE FAVORITOS (REUTILIZABLE) -----------------------------------
// ==========================================================================================
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
            Text(
                text = "📌",
                fontSize = 18.sp,
                modifier = Modifier.graphicsLayer {
                    if (isIndirect && !isFavorite) {
                        // Aplicamos un filtro gris si es indirecto
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
// ------------------------ NUEVA TARJETA CATEGORIA VERTICAL BENTO GLASS ---
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactCategoryCard(
    item: CategoryEntity, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    val baseColor = Color(CategoryVisuals.getColorFor(item.superCategory))
    // ==========================================================================================
    // SECCIÓN: ESTADOS PARA EL CONTROL DE MENÚS Y BADGES
    // ==========================================================================================
    var expandedInfoBadge by remember { mutableStateOf(false) }
    var showNotificationMenu by remember { mutableStateOf(false) } // Controla el menú de novedades (Top-Left)
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(195.dp) // Altura equilibrada
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
    ) {
        // --- 1. TARJETA BASE ---
        Card(
            shape = RoundedCornerShape(6.dp), // Esquinas profesionales
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // FONDO: Saturado Mate (Top) -> Negro Mate (Bottom)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 1f),
                                    Color(0xFF080A0F) // Negro mate profundo
                                ),
                                startY = 100f,
                                endY = 550f
                            )
                        )
                )
                // CAPA DE DIFUMINADO SUPERIOR (Neblina de color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.73f)
                        .blur(5.dp)
                        .background(baseColor.copy(alpha = 0.9f))
                )







                // ==========================================================================================
                // SECCIÓN: EMOJI CENTRAL (SOMBRA Y PRINCIPAL)
                // ==========================================================================================
                
                // EMOJI CENTRAL: SOMBRA PROYECTADA (Silueta negra desfasada para efecto real)
                Text(
                    text = item.icon,
                    fontSize = 100.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-14).dp) // Desplazado hacia abajo respecto al original (-30)
                        .graphicsLayer {
                            alpha = 0.9f
                            // Convertimos el emoji en una silueta negra
                            colorFilter = ColorFilter.tint(Color.Black)
                        }
                        .blur(2.dp) // Desenfoque mínimo para realismo
                )

                // EMOJI CENTRAL: PRINCIPAL
                Text(
                    text = item.icon,
                    fontSize = 100.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-20).dp)
                )

                // ==========================================================================================
                // SECCIÓN: BADGES (NOTIFICACIÓN, FAVORITO E INFORMACIÓN) - SUPERPUESTOS AL EMOJI
                // ==========================================================================================
                
                // 1. BADGE DE NOTIFICACIÓN (Extremo Izquierdo Superior)
                // Se muestra si la categoría es nueva, tiene nuevos prestadores o es publicidad
                if (item.isNew || item.isNewPrestador || item.isAd) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(2.dp)
                            .offset(x = 0.dp, y = 0.dp) // ==========================================
                                                        // SECCIÓN: OFFSET PARA MOVER NOTIFICACIÓN
                                                        // ==========================================
                    ) {
                        Surface(
                            onClick = { showNotificationMenu = !showNotificationMenu },
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = Color(0xFFFF9800).copy(alpha = 0.9f), // Naranja para notificaciones
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🔔", fontSize = 14.sp)
                            }
                        }

                        // Menú M3 para mostrar detalles de notificación
                        DropdownMenu(
                            expanded = showNotificationMenu,
                            onDismissRequest = { showNotificationMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF1A1C1E))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            if (item.isNew) {
                                DropdownMenuItem(
                                    text = {
                                        Text("✨ Este es un nuevo servicio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    },
                                    onClick = { showNotificationMenu = false }
                                )
                            }
                            if (item.isNewPrestador) {
                                DropdownMenuItem(
                                    text = {
                                        Text("👥 Hay nuevos prestadores o profesionales para este servicio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    },
                                    onClick = { showNotificationMenu = false }
                                )
                            }
                            if (item.isAd) {
                                DropdownMenuItem(
                                    text = {
                                        Text("📢 Servicio Patrocinado", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    },
                                    onClick = { showNotificationMenu = false }
                                )
                            }
                        }
                    }
                }

                // 2. BADGE DE FAVORITO (Extremo Derecho Superior)
                // Se separó en su propio contenedor para permitir el uso de Offsets individuales
                if (item.isFavorite) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .offset(x = 0.dp, y = 0.dp) // ==========================================
                                                        // SECCIÓN: OFFSET PARA MOVER FAVORITOS
                                                        // ==========================================
                    ) {
                        FavoritePinBadge(
                            isFavorite = true
                        )
                    }
                }

                // 3. BOTÓN DE INFORMACIÓN (REUBICADO AL BORDE INFERIOR DERECHO DEL ICONO)
                var showInfoMenu by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 1.dp, y = (-40).dp) // ==========================================
                                                     // SECCIÓN: OFFSET PARA MOVER INFORMACIÓN
                                                     // (Posicionado relativo al centro del emoji)
                                                     // ==========================================
                ) {
                    Surface(
                        onClick = { showInfoMenu = !showInfoMenu },
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = Color(0xFF1A1C1E), // FONDO OPACO (No transparente)
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "ℹ️", fontSize = 14.sp)
                        }
                    }

                    // Menú desplegable con la descripción de la categoría
                    DropdownMenu(
                        expanded = showInfoMenu,
                        onDismissRequest = { showInfoMenu = false },
                        modifier = Modifier
                            .width(220.dp)
                            .background(Color(0xFF1A1C1E))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.description.ifEmpty { "Explora los servicios disponibles en ${item.name}." },
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                // ==========================================================================================
                // SECCIÓN: INFERIOR - TEXTO Y BADGE INFORMATIVO (REDiseño para Máximo Espacio)
                // ==========================================================================================
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(53.dp), // Incrementado para usar todo el alto posible hasta el divider superior
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. DIVIDER HORIZONTAL (Delimita la zona de imagen de la zona de texto)
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.45f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )

                    // 2. CONTENEDOR DE CONTENIDO (TEXTO + BADGE CON DIVIDER VERTICAL)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- SUBSECCIÓN: NOMBRE DE LA CATEGORÍA ---
                        // Se expande para usar todo el ancho disponible a la izquierda del divider vertical
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(start = 2.dp, end = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AutoSizeText(
                                text = item.name.uppercase(),
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp, // Tamaño base ajustado
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.1.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 3 // Permitimos hasta 3 líneas para evitar cortes de palabras en nombres largos
                            )
                        }


                        }
                    }
                }
            }
        }

        // --- MENU CONTEXTUAL PARA FAVORITOS ---
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier.background(Color(0xFF1A1C1E)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (item.isFavorite) "Quitar de Favoritos" else "Agregar a Favoritos",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "📌", fontSize = 16.sp, modifier = Modifier.alpha(if (item.isFavorite) 1f else 0.4f))
                    }
                },
                onClick = {
                    onToggleFavorite()
                    showContextMenu = false
                }
            )
        }
    }
//}

/**
 * Componente interno: Icono Informativo Minimalista (Sin fondo)
 */
@Composable
fun CategoryBadgeItem(icon: String, tooltip: String, isExpanded: Boolean, onToggle: () -> Unit) {
    Box {
        // SECCIÓN: ICONO INTERACTIVO SIN FONDO
        Box(
            modifier = Modifier
                .size(26.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // Limpieza visual sin ripple para estilo glass/minimal
                    onClick = onToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 14.sp,
                modifier = Modifier.graphicsLayer { alpha = 0.85f }
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onToggle,
            modifier = Modifier
                .width(220.dp) // Ancho ligeramente mayor para descripciones
                .background(Color(0xFF0C0F14))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = tooltip,
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ==========================================================================================
// ------------------- TARJETA CATEGORIA HORIZONTAL--------------------------------
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactCategoryCardHorizontal(
    item: CategoryEntity, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    val baseColor = Color(CategoryVisuals.getColorFor(item.superCategory))
    // ==========================================================================================
    // SECCIÓN: ESTADOS PARA EL CONTROL DE MENÚS Y BADGES
    // ==========================================================================================
    var expandedInfoBadge by remember { mutableStateOf(false) }
    var showNotificationMenu by remember { mutableStateOf(false) } // Controla el menú de novedades (Top-Left)
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = { showContextMenu = true }
            )
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = baseColor),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // EFECTO OVERLAY (Brillo de cristal)
                Box(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.99f).drawWithCache {
                    val gradient = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    );
                    onDrawWithContent { drawContent(); drawRect(gradient, blendMode = BlendMode.Overlay) }
                })

                // DEGRADADO OSCURO
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.99f),
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            startX = 100f,
                            endX = 600f
                        )
                    )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SECCIÓN IZQUIERDA (Texto)
                    Box(
                        modifier = Modifier
                            .weight(0.65f)
                            .fillMaxHeight()
                            .padding(start = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = item.name.uppercase(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // DIVIDER INFERIOR DE TEXTO
                            Box(modifier = Modifier.width(30.dp).height(2.dp).background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(1.dp)))
                        }
                    }

                    // DIVIDER VERTICAL + BADGE INFORMATIVO CENTRADO
                    Box(
                        modifier = Modifier.fillMaxHeight().width(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Línea del Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(1f)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.White.copy(alpha = 0.9f), Color.Transparent)
                                    )
                                )
                        )
                        // Badge Informativo: Conectado con la descripción de la base de datos
                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                        ) {
                            val infoText = remember(item) {
                                item.description.ifEmpty { "Explora los servicios disponibles en ${item.name}." }
                            }
                            CategoryBadgeItem(
                                icon = "✨",
                                tooltip = infoText,
                                isExpanded = expandedInfoBadge,
                                onToggle = { expandedInfoBadge = !expandedInfoBadge }
                            )
                        }
                    }
                    // SECCIÓN DERECHA (Icono)
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        // SOMBRA PROYECTADA (Silueta negra desfasada para efecto real)
                        Text(
                            text = item.icon,
                            fontSize = 64.sp,
                            modifier = Modifier
                                .offset(y = 4.dp) // Desfase hacia abajo
                                .graphicsLayer {
                                    alpha = 0.6f
                                    colorFilter = ColorFilter.tint(Color.Black)
                                }
                                .blur(2.dp)
                        )

                        // EMOJI PRINCIPAL
                        Text(
                            text = item.icon,
                            fontSize = 64.sp
                        )
                    }
                }
            }
        }

        // ==========================================================================================
        // SECCIÓN: BADGES SUPERIORES (NOTIFICACIÓN Y FAVORITO)
        // ==========================================================================================

        // 1. BADGE DE NOTIFICACIÓN (Extremo Izquierdo Superior)
        // Se muestra si la categoría es nueva, tiene nuevos prestadores o es publicidad
        if (item.isNew || item.isNewPrestador || item.isAd) {
            Box(modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp, start = 8.dp)) {
                Surface(
                    onClick = { showNotificationMenu = !showNotificationMenu },
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF9800).copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔔", fontSize = 14.sp)
                    }
                }

                DropdownMenu(
                    expanded = showNotificationMenu,
                    onDismissRequest = { showNotificationMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1A1C1E))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    if (item.isNew) {
                        DropdownMenuItem(
                            text = {
                                Text("✨ Este es un nuevo servicio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            },
                            onClick = { showNotificationMenu = false }
                        )
                    }
                    if (item.isNewPrestador) {
                        DropdownMenuItem(
                            text = {
                                Text("👥 Hay nuevos prestadores o profesionales para este servicio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            },
                            onClick = { showNotificationMenu = false }
                        )
                    }
                    if (item.isAd) {
                        DropdownMenuItem(
                            text = {
                                Text("📢 Servicio Patrocinado", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            },
                            onClick = { showNotificationMenu = false }
                        )
                    }
                }
            }
        }

        // 2. BADGE DE FAVORITO (Extremo Derecho Superior)
        if (item.isFavorite) {
            FavoritePinBadge(
                isFavorite = true,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 8.dp)
            )
        }

        // --- MENU CONTEXTUAL PARA FAVORITOS ---
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier.background(Color(0xFF1A1C1E)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (item.isFavorite) "Quitar de Favoritos" else "Agregar a Favoritos",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "📌", fontSize = 16.sp, modifier = Modifier.alpha(if (item.isFavorite) 1f else 0.4f))
                    }
                },
                onClick = {
                    onToggleFavorite()
                    showContextMenu = false
                }
            )
        }
    }
}
// ==========================================================================================
// ------------------- TARJETA ESTILO BENTO PARA SUPERCATEGORÍAS (Extraída de HomeScreenCliente3)--------------------------------
// ==========================================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSuperCategoryCard(
    superCategory: SuperCategory, 
    emoji: String, 
    height: Dp, 
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    var showContextMenu by remember { mutableStateOf(false) }

    // SECCIÓN: Contenedor para permitir que el pin sobresalga (efecto Badge)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(top = 8.dp, end = 8.dp) // Espacio para que el pin no se corte
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showContextMenu = true }
                ),
            // SECCIÓN: Esquinas equilibradas y bordes reforzados
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.3f)), // Borde más visible
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Fondo con degradado
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(0.85f)
                            )
                        )
                    ))
                // Iconos internos difuminados
                Box(modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 20.dp)
                    .alpha(0.35f)) {
                    LazyVerticalGrid(GridCells.Fixed(2), userScrollEnabled = false) {
                        items(items = superCategory.items, key = { it.name }) { item ->
                            Text(item.icon, fontSize = 70.sp, modifier = Modifier
                                .padding(4.dp)
                                .alpha(0.5f))
                        }
                    }
                }

                // SECCIÓN: Contenido Principal (Icono, Servicios y Título) agrupados para reducir espacio
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp) // Reduce el espacio entre el icono y el título
                ) {
                    // ==========================================================================================
                    // SECCIÓN: FILA SUPERIOR (EMOJI CON SOMBRA Y CONTADOR DE SERVICIOS)
                    // ==========================================================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Start // Colocamos los elementos seguidos para que el texto quede a la derecha
                    ) {
                        // CONTENEDOR DE EMOJI: Superpone la sombra detrás del emoji principal
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(end = 12.dp) // Espaciado entre el emoji y el texto de servicios
                        ) {
                            // EMOJI (SOMBRA): Silueta negra con desenfoque, posicionada detrás
                            Text(
                                text = emoji,
                                fontSize = 55.sp,
                                modifier = Modifier
                                    .offset(x = 4.dp, y = 4.dp) // Desfase para el efecto de profundidad
                                    .graphicsLayer {
                                        alpha = 0.9f
                                        colorFilter = ColorFilter.tint(Color.Black)
                                    }
                                    .blur(4.dp)
                            )

                            // EMOJI (COLOR): El emoji principal al frente
                            Text(
                                text = emoji,
                                fontSize = 55.sp,
                                modifier = Modifier.alpha(1f)
                            )
                        }

                        // CANTIDAD DE SERVICIOS: Ubicada a la derecha del emoji
                        Text(
                            text = "${superCategory.items.size} Servicios",
                            color = Color.Cyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 1.dp) // Alineación visual con la base del emoji
                        )
                    }
                   Spacer(modifier = Modifier.height(12.dp))
                    // Título de la Supercategoría (Detalle) - AutoAjustable a 2 líneas
                    AutoSizeText(
                        text = superCategory.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 18.sp
                        ),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // --- MENU CONTEXTUAL PARA SUPERCATEGORÍAS ---
                DropdownMenu(
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    modifier = Modifier.background(Color(0xFF1A1C1E)).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (superCategory.isFavorite) "Quitar Super-Favorito" else "Hacer Super-Favorito",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "📌", fontSize = 16.sp, modifier = Modifier.alpha(if (superCategory.isFavorite) 1f else 0.4f))
                            }
                        },
                        onClick = {
                            onToggleFavorite()
                            showContextMenu = false
                        }
                    )
                }
            }
        }

        // SECCIÓN: Pin de favorito (📌) con OFFSET para sobresalir del borde superior derecho
        FavoritePinBadge(
            isFavorite = superCategory.isFavorite,
            isIndirect = superCategory.hasFavoriteCategories,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (4).dp)
        )
    }
}


// ==========================================================================================
// ------------------- TARJETA EXPANDIDA PARA RESULTADOS DE BÚSQUEDA ------------------------
// ==========================================================================================
@Composable
fun ExpandedBentoSuperCategoryCard(
    superCategory: SuperCategory,
    onCategoryClick: (CategoryEntity) -> Unit,
    onToggleCategoryFavorite: (CategoryEntity) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(12.dp, RoundedCornerShape(6.dp)),
        // SECCIÓN: Esquinas menos redondeadas según requerimiento
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo con degradado (efecto bento)
            Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(0.85f)
                        )
                    )
                ))
            
            Column(modifier = Modifier.padding(6.dp)) {
                // HEADER: Nombre a la izquierda, Icono a la derecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge de favorito en el header
                        Text(text = superCategory.icon, fontSize = 28.sp)

                        if (superCategory.isFavorite || superCategory.hasFavoriteCategories) Spacer(modifier = Modifier.width(8.dp))

                        AutoSizeText(
                            text = superCategory.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 2
                        )
                    }
                    FavoritePinBadge(
                        isFavorite = superCategory.isFavorite,
                        isIndirect = superCategory.hasFavoriteCategories,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))

                // LISTA HORIZONTAL con 3 items visibles aprox
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = superCategory.items, key = { it.name }) { category ->
                        // Ajustamos ancho a ~110.dp para que quepan 3 aprox en pantalla
                        Box(modifier = Modifier.width(125.dp)) {
                            CompactCategoryCard(
                                item = category,
                                onClick = { onCategoryClick(category) },
                                onToggleFavorite = { onToggleCategoryFavorite(category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

//==========================================================================================
// ------------------- PREVIEWS--------------------------------
// ==========================================================================================


@Preview(showBackground = true)
@Composable
fun CompactCategoryCardhHorizontalPreview() {
    MyApplicationTheme {
        val sampleItem = CategoryEntity(
            name = "Peluquería",
            icon = "✂️",
            superCategory = "Cuidado Personal y Moda",
            isNew = true,
            isNewPrestador = true,
            isAd = true
        )
        Box(modifier = Modifier.padding(16.dp).fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            CompactCategoryCardHorizontal(
                item = sampleItem,
                onClick = {}
            )
        }
    }
}




@Preview(showBackground = true)
@Composable
fun CompactCategoryCardPreview() {
    val sampleItem = CategoryEntity(
        name = "Electricidad",
        icon = "⚡",
        superCategory = "Hogar",
        isNew = true,
        isNewPrestador = true,
        isAd = true
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp).width(160.dp)) {
            CompactCategoryCard(
                item = sampleItem,
                onClick = {}
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BentoSuperCategoryCardPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Limpieza",
            icon = "🧹",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Plomería",
            icon = "🪠",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Electricidad",
            icon = "⚡",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Carpintería",
            icon = "🪚",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        )
    )
    val sampleSuperCat = SuperCategory(
        title = "Hogar y Construcción",
        icon = "🏠",
        items = sampleCategories,
        isFavorite = true,
        hasFavoriteCategories = true
    )
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp)
        ) {
            BentoSuperCategoryCard(
                superCategory = sampleSuperCat,
                emoji = "🏠",
                height = 200.dp,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpandedBentoSuperCategoryCardPreview() {
    val sampleCategories = listOf(
        CategoryEntity(
            name = "Limpieza",
            icon = "🧹",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = true,
            isNewPrestador = true,
            isAd = true
        ),
        CategoryEntity(
            name = "Plomería",
            icon = "🪠",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        ),
        CategoryEntity(
            name = "Electricidad",
            icon = "⚡",
            superCategory = "Hogar",
            superCategoryIcon = "🏠",
            providerIds = emptyList(),
            isNew = false,
            isNewPrestador = false,
            isAd = false
        )
    )
    val sampleSuperCat = SuperCategory(
        title = "Hogar y Construcción",
        icon = "🏠",
        items = sampleCategories,
        isFavorite = true,
        hasFavoriteCategories = true
    )
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ExpandedBentoSuperCategoryCard(
                superCategory = sampleSuperCat,
                onCategoryClick = {}
            )
        }
    }
}
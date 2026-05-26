package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.presentation.designsystem.components.MaverickTacticalButton
import com.example.myapplication.presentation.designsystem.components.shakeClick
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

// ==========================================================================================
// --- MODELOS DE DATOS Y COMPONENTES BASE ---
// ==========================================================================================

/**
 * ControlItem: Modelo de datos para elementos de filtrado y ordenamiento.
 */
data class ControlItem(
    val label: String,
    val icon: ImageVector?,
    val emoji: String?,
    val color: Color,
    val id: String = label.lowercase()
)

/**
 * CompactItemButton: Botón minimalista utilizado en los menús tácticos.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactItemButton(
    item: ControlItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    overlayEmoji: String? = null,
    overlayAlignment: Alignment = Alignment.BottomEnd
) {
    // --- 📐 SECCIÓN: CONFIGURACIÓN DE FORMA TÁCTICA (CASI CUADRADA) ---
    val tacticalShape = CutCornerShape(4.dp)

    Column(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isSelected) item.color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), tacticalShape)
                .border(if (isSelected) 1.5.dp else 0.8.dp, if (isSelected) item.color else Color.White.copy(alpha = 0.15f), tacticalShape)
                .shakeClick { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = item.emoji ?: "", 
                    fontSize = 24.sp, 
                    style = TextStyle(shadow = Shadow(color = item.color, offset = androidx.compose.ui.geometry.Offset(0f, 0f), blurRadius = 25f))
                )
            } else {
                item.icon?.let { Icon(it, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp)) }
                    ?: run { Text(item.emoji ?: "", fontSize = 20.sp, modifier = Modifier.alpha(0.6f)) }
            }

            overlayEmoji?.let { emoji ->
                Text(
                    text = emoji, fontSize = 11.sp, color = Color.White,
                    modifier = Modifier
                        .align(overlayAlignment)
                        .offset(x = 6.dp, y = if (overlayAlignment == Alignment.TopEnd) (-6).dp else 6.dp)
                        .graphicsLayer { shadowElevation = 10f }
                )
            }
        }
        // --- SECCIÓN: ETIQUETA DE TEXTO (CONFIGURACIÓN DE 2 LÍNEAS) ---
        // Se ajusta el interlineado (lineHeight) y se elimina el padding de la fuente (includeFontPadding)
        // para que las 2 líneas estén "pegadas" con una distancia aproximada de 1dp.
        Text(
            text = item.label,
            fontSize = 9.sp,
            lineHeight = 10.sp, // Interlineado ajustado para 1dp de separación visual
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false // Elimina espacio extra para que las líneas queden pegadas
                )
            ),
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.White else Color.LightGray,
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==========================================================================================
// --- COMPONENTE PRINCIPAL: MENU FILTROS ---
// ==========================================================================================

/**
 * MenuFiltros: Componente táctico para la gestión de filtros y categorías.
 */
@Composable
fun MenuFiltros(
    activeFilters: Set<String>,
    modifier: Modifier = Modifier,
    dynamicCategories: List<ControlItem>,
    refinementFilters: List<ControlItem> = emptyList(),
    onAction: (String) -> Unit,
    onApply: () -> Unit,
    onClearFilters: () -> Unit,

    showProductService: Boolean = false 
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 🔥 CORRECCIÓN CRÍTICA: Se amplió la lógica para mostrar la X. 
    // Ahora detecta cualquier filtro activo que no sea de ordenamiento (sort_) o vista (view_).
    val hasSpecificFilters = activeFilters.any { 
        !it.startsWith("sort_") && !it.startsWith("view_") 
    }

    // 1. Animación de rotación para el icono (Tornado en este caso)
    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "RotationFiltros"
    )

    // 2. Animación de escala con Rebote para el panel (Réplica exacta)
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(200)
        },
        label = "ScaleFiltros"
    )

    // Gradiente premium para el fondo del panel
    val cardBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1F26), Color(0xFF0A0E14))
    )

    // CONTENEDOR PRINCIPAL
    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 🔥 BOTÓN X (Limpiar): Brota desde detrás del Tornado hacia la izquierda
        AnimatedVisibility(
            visible = hasSpecificFilters,
            enter = fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { it })
        ) {
            MaverickTacticalButton(
                onClick = { onClearFilters() },
                modifier = Modifier.padding(end = 40.dp),
                accentColor = Color(0xFFEF4444)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFEF4444)

                )
            }
        }

        // --- ICONO DISPARADOR (Tornado 🌪️) ---
        MaverickTacticalButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.graphicsLayer { rotationZ = iconRotation }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🌪️", fontSize = 20.sp) // --- 📐 TAMAÑO AJUSTADO ---
               // Text(
                //    text = "FILTRAR",
                //    fontSize = 6.sp,
                 //   fontWeight = FontWeight.Black,
                 //   color = Color.White.copy(alpha = 0.7f)
               // )
            }
        }

        // --- 2. PANEL POPUP (FILTROS CON ESTILO GHOST) ---
        if (isExpanded || scale > 0.01f) {
            // AJUSTE DE POSICIÓN: offset(x, y) donde y controla la altura vertical
            val verticalOffset = 105 // --- 🆙 Aumenta para bajar, disminuye para subir ---
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, verticalOffset),
                properties = PopupProperties(focusable = true, dismissOnClickOutside = true),
                onDismissRequest = { isExpanded = false }
            ) {
                // Caja con estilo de burbuja Ghost
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa casi todo el ancho
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = scale.coerceIn(0f, 1f)
                        }
                ) {
                    // ==========================================================
                    // --- SECCIÓN: COLA DE LA BURBUJA (AJUSTE DE POSICIÓN) ---
                    // ==========================================================
                    
                    // AJUSTE DINÁMICO: Modifica tailHorizontalOffset para apuntar al Tornado (🌪️)
                    val tailHorizontalOffset = 195.dp // --- ↔️ Mueve a la derecha (+) o izquierda (-) ---
                    val tailVerticalOffset = 2.dp     // --- ↕️ Mueve abajo (+) o arriba (-) para pegar al card ---
                    val tailWidth = 60.dp             // --- ↔️ Ancho de la cola ---
                    val tailHeight = 14.dp            // --- ↕️ Alto de la cola ---
                    
                    Canvas(
                        modifier = Modifier
                            .size(width = tailWidth, height = tailHeight)
                            .offset(x = tailHorizontalOffset, y = tailVerticalOffset) // Aplicación del offset pedido
                    ) {
                        val path = Path().apply {
                            // Triángulo estilizado centrado en su propio Canvas
                            moveTo(size.width * 0.30f, size.height)
                            lineTo(size.width * 0.50f, 0f)
                            lineTo(size.width * 0.70f, size.height)
                            close()
                        }
                        
                        // 1. Relleno: Coincide con el fondo del Card (Gris Oscuro)
                        drawPath(path, Color(0xFF00FFFF))
                        
                        // 2. Borde Neón: Coincide con el estilo del Card (Cyan)
                        drawPath(
                            path, 
                            Color(0xFF00FFFF).copy(alpha = 0.7f), 
                            style = Stroke(width = 2.5f)
                        )
                    }

                    // ==========================================================
                    // --- SECCIÓN: CUERPO DEL PANEL (CARD) ---
                    // ==========================================================
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00FFFF).copy(alpha = 0.6f), CutCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F26)),
                        shape = CutCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // --- HEADER ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AJUSTA MAS LA BUSQUEDA",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.5.sp
                                )


                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // --- SECCIÓN UNIFICADA (FILA ÚNICA) ---
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showProductService) {
                                    CompactItemButton(
                                        item = ControlItem("Productos", Icons.Default.ShoppingBag, "🛍️", Color(0xFF22D3EE), "filter_products"),
                                        isSelected = activeFilters.contains("filter_products"),
                                        onClick = { onAction("filter_products") }
                                    )
                                    CompactItemButton(
                                        item = ControlItem("Servicios", Icons.Default.Build, "🔧", Color(0xFFF59E0B), "filter_services"),
                                        isSelected = activeFilters.contains("filter_services"),
                                        onClick = { onAction("filter_services") }
                                    )
                                    VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 4.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
                                }

                                val allFilters = dynamicCategories + refinementFilters
                                allFilters.forEach { item ->
                                    CompactItemButton(
                                        item = item,
                                        isSelected = activeFilters.contains(item.id),
                                        onClick = { onAction(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista previa específica para visualizar el Popup de Filtros expandido con diseño Ghost.
 */
@Preview(showBackground = true, backgroundColor = 0xFF05070A, device = "spec:width=411dp,height=891dp")
@Composable
fun MenuFiltrosGhostExpandedPreview() {
    // Usamos CompositionLocal para forzar la expansión del menú en la preview
    MyApplicationTheme {
        var activeFilters by remember { mutableStateOf(setOf("filter_products", "cat_plomeria", "24h")) }
        
        val sampleCategories = listOf(
            ControlItem("Plomería", null, "🔧", Color(0xFF2197F5), "cat_plomeria"),
            ControlItem("Electricidad", null, "⚡", Color(0xFFFFEB3B), "cat_electricidad"),
            ControlItem("Pintura", null, "🎨", Color(0xFFFF4081), "cat_pintura")
        )
        
        val sampleRefinements = listOf(
            ControlItem("Suscrito", Icons.Default.Verified, "✅", Color(0xFF9B51E0), "filter_sub"),
            ControlItem("24hs", Icons.Default.AccessTimeFilled, "⏳", Color(0xFFFF9800), "Atension 24h")
        )

        // Contenedor que simula un estado donde el popup ya está abierto
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
            // Este wrapper simula el estado interno del componente
            val isExpanded = remember { mutableStateOf(true) }
            
            // Reutilizamos MenuFiltros, pero nota que controlamos isExpanded internamente en el componente original
            // Para la preview, si no podemos acceder al estado interno, visualizamos el componente completo
            MenuFiltros(
                activeFilters = activeFilters,
                dynamicCategories = sampleCategories,
                refinementFilters = sampleRefinements,
                showProductService = true,
                onAction = { id ->
                    val current = activeFilters.toMutableSet()
                    if (!current.add(id)) current.remove(id)
                    activeFilters = current
                },
                onApply = { },
                onClearFilters = { activeFilters = emptySet() }
            )
        }
    }
}
/**
 * Vista previa de CompactItemButton mostrando diferentes estados.
 */
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun CompactItemButtonPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val sampleItem = ControlItem(
                label = "Servicios",
                icon = Icons.Default.Build,
                emoji = "🔧",
                color = Color(0xFFF59E0B),
                id = "filter_services"
            )

            // Estado no seleccionado
            CompactItemButton(
                item = sampleItem,
                isSelected = false,
                onClick = { }
            )

            // Estado seleccionado
            CompactItemButton(
                item = sampleItem,
                isSelected = true,
                onClick = { }
            )

            // Con emoji superpuesto
            CompactItemButton(
                item = sampleItem,
                isSelected = false,
                onClick = { },
                overlayEmoji = "✨"
            )
        }
    }
}










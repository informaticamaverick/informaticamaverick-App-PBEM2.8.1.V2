package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import com.example.myapplication.presentation.designsystem.components.MaverickTacticalButton
import com.example.myapplication.presentation.designsystem.components.shakeClick
import com.example.myapplication.presentation.designsystem.components.BotonVista
import com.example.myapplication.presentation.designsystem.components.BotonFiltroSuscritosPremium

// ====================================================================================
// ================= SECCIÓN: BOTONES INDIVIDUALES IMPLEMENTADOS ======================
// ====================================================================================

// ---------------------------------------------------------
// 2. BOTÓN ALFABÉTICO (Ascendente, Descendente, Apagado)
// ---------------------------------------------------------
@Composable
fun BotonAlfabetico(
    orderState: String, // "none", "asc", "desc"
    onStateChange: (String) -> Unit
) {
    val isActive = orderState != "none"
    val emoji = when (orderState) {
        "asc" -> "🅰️"
        "desc" -> "🆉"
        else -> "🔤"
    }

    MaverickTacticalButton(
        isActive = isActive,
        accentColor = Color(0xFF2197F5),
        onClick = {
            val nextState = when (orderState) {
                "none" -> "asc"
                "asc" -> "desc"
                else -> "none"
            }
            onStateChange(nextState)
        }
    ) {
        Text(text = emoji, fontSize = 16.sp)
    }
}

// ---------------------------------------------------------
// 3. BOTÓN DE PRECIO (Menor a Mayor, Mayor a Menor, Apagado)
// ---------------------------------------------------------
@Composable
fun BotonPrecio(
    orderState: String, // "none", "asc", "desc"
    onStateChange: (String) -> Unit
) {
    val isActive = orderState != "none"
    val emoji = when (orderState) {
        "asc" -> "💸" 
        "desc" -> "💰" 
        else -> "💲"
    }

    MaverickTacticalButton(
        isActive = isActive,
        accentColor = Color(0xFF2197F5),
        onClick = {
            val nextState = when (orderState) {
                "none" -> "asc"
                "asc" -> "desc"
                else -> "none"
            }
            onStateChange(nextState)
        }
    ) {
        Text(text = emoji, fontSize = 16.sp)
    }
}

// ---------------------------------------------------------
// 4. BOTÓN DE FECHA (Click normal y Long Press)
// ---------------------------------------------------------
@Composable
fun BotonFecha(
    dateState: String, // "none", "recent", "oldest", "custom"
    onStateChange: (String) -> Unit,
    onDateRangeSelected: (String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val isActive = dateState != "none"

    val emoji = when (dateState) {
        "recent" -> "🆕"
        "oldest" -> "🕰️"
        "custom" -> "🎯"
        else -> "📅"
    }

    // 🔥 NOTA: Aquí se usa MaverickTacticalButton
    MaverickTacticalButton(
        isActive = isActive,
        accentColor = Color(0xFF2197F5),
        onClick = {
            val nextState = when (dateState) {
                "none", "custom" -> "recent"
                "recent" -> "oldest"
                else -> "none"
            }
            onStateChange(nextState)
        }
    ) {
        Text(text = emoji, fontSize = 20.sp)

        // Puntito rojo si hay filtro custom
        if (dateState == "custom") {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }

    // Modal del Calendario
    if (showDialog) {
        DialogFiltroFechas(
            onDismiss = { showDialog = false },
            onAccept = { inicio, fin ->
                showDialog = false
                onStateChange("custom")
                onDateRangeSelected(inicio, fin)
            }
        )
    }
}

// ====================================================================================
// ================= SECCIÓN: DIÁLOGO DEL CALENDARIO ==================================
// ====================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogFiltroFechas(
    onDismiss: () -> Unit,
    onAccept: (String, String) -> Unit
) {
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F26)),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filtrar por Fechas", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = startDate, onValueChange = { startDate = it },
                    label = { Text("Fecha Inicio", fontSize = 12.sp) }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF2197F5), unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = endDate, onValueChange = { endDate = it },
                    label = { Text("Fecha Fin", fontSize = 12.sp) }, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF2197F5), unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAccept(startDate, endDate) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2197F5))
                    ) { Text("Aplicar", color = Color.White) }
                }
            }
        }
    }
}
/**
 * 5. BOTÓN MÁS USADOS (Ordenamiento)
 * Al activarse, ordena la lista priorizando los elementos con mayor frecuencia de uso.
 */
@Composable
fun BotonMasUsados(
    isActive: Boolean,
    onClick: () -> Unit
) {
    MaverickTacticalButton(
        isActive = isActive,
        accentColor = Color(0xFF2197F5),
        onClick = onClick
    ) {
        Text(text = "🔥", fontSize = 20.sp)
    }
}

/**
 * 6. BOTÓN FAVORITOS (Filtro)
 * Al activarse, filtra la lista para mostrar únicamente los elementos marcados como favoritos.
 */
@Composable
fun BotonFavoritos(
    isActive: Boolean,
    onClick: () -> Unit
) {
    MaverickTacticalButton(
        isActive = isActive,
        accentColor = Color(0xFF2197F5),
        onClick = onClick
    ) {
        Text(text = "⭐", fontSize = 20.sp)
    }
}

/**
 * 7. BOTÓN SUSCRITO (Filtro Booleano)
 * Filtra la lista según la variable booleana de suscripción del usuario/item.
 */
@Composable
fun BotonSuscrito(
    isSubscribedOnly: Boolean,
    onClick: () -> Unit
) {
    MaverickTacticalButton(
        isActive = isSubscribedOnly,
        accentColor = Color(0xFF2197F5),
        onClick = onClick
    ) {
        Text(text = "💎", fontSize = 20.sp)
    }
}


/**
 * MenuOrdenamiento: Componente táctico para la gestión de ordenamientos.
 */
@Composable
fun MenuOrdenamiento(
    activeFilters: Set<String>,
    sortOptions: List<ControlItem> = emptyList(), // 🔥 NUEVO: Opciones dinámicas desde el ViewModel
    onAction: (String) -> Unit,
    onApply: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    // 🔥 MANTENIDOS PARA COMPATIBILIDAD CON CARRUSEL DE HOMESCREEN
    showNombre: Boolean = false,
    showRank: Boolean = false,
    showViewModes: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }

    // 🔥 CORRECCIÓN: Solo muestra la X si hay filtros de tipo 'sort_' o 'view_'
    val hasSortFilters = activeFilters.any { it.startsWith("sort_") || it.startsWith("view_") }

    // 1. Rotación de la tuerca
    val gearRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "GearRotation"
    )

    // 2. Escala con Rebote (Spring)
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = if (isExpanded) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        } else {
            tween(200)
        },
        label = "ScaleOrdenamiento"
    )

    val cardBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1F26), Color(0xFF0A0E14))
    )

    // 🔥 CORRECCIÓN: Quitamos fillMaxWidth() para que no tape el texto de HomeScreen
    Box(
       modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 🔥 BOTÓN X (Limpiar): Brota y se esconde detrás del engranaje
        // Lo ponemos ANTES en el código para que el Engranaje se dibuje ENCIMA (Z-index natural)
        AnimatedVisibility(
            visible = hasSortFilters,
            enter = fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { it })
        ) {
            // El paddingEnd asegura que la X se detenga a la izquierda del engranaje
            MaverickTacticalButton(
                onClick = { onClearFilters() },
                modifier = Modifier.padding(end = 40.dp),
                accentColor = Color(0xFFEF4444)
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(24.dp), tint = Color(0xFFEF4444))
            }
        }

        // --- BOTÓN ENGRANAJE (Ancla absoluta) ---
        // Al estar al final del Box y alineado al End, nunca se moverá
        MaverickTacticalButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.graphicsLayer { rotationZ = gearRotation }
        ) {
            Text(text = "⚙️", fontSize = 20.sp)
        }

        // --- EL POPUP QUE "BROTA" ---
        if (isExpanded || scale > 0.01f) {
            Popup(alignment = Alignment.TopEnd, offset = IntOffset(-50, 115), properties = PopupProperties(focusable = true, dismissOnClickOutside = true), onDismissRequest = { isExpanded = false }) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(280.dp).graphicsLayer { scaleX = scale; scaleY = scale; alpha = scale.coerceIn(0f, 1f); transformOrigin = TransformOrigin(1f, 0f) }) {
                    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp)).border(width = 1.dp, color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp))) {
                        Box(modifier = Modifier.background(cardBackground)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "ORDENAR POR", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.5.sp)
                                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.White.copy(alpha = 0.9f))
                                    MaverickTacticalButton(
                                        onClick = { isExpanded = false; onApply() },
                                        size = 36.dp,
                                        accentColor = Color(0xFF10B981)
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                    }
                                }
                                // ... (Sigue el contenido de CompactItemButton igual que antes)

                                Spacer(modifier = Modifier.height(10.dp))

                                // --- BLOQUE DINÁMICO: OPCIONES DESDE EL VIEWMODEL ---
                                if (sortOptions.isNotEmpty()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        sortOptions.forEach { item ->
                                            CompactItemButton(
                                                item = item,
                                                isSelected = activeFilters.contains(item.id),
                                                onClick = { onAction(item.id) }
                                            )
                                        }
                                    }
                                }

                                // --- BLOQUE MANTENIDO: PARA HOMESCREEN (CARRUSEL) ---
                                if (showNombre || showRank || showViewModes) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        if (showNombre) {
                                            val isAsc = activeFilters.contains("sort_nombre_asc")
                                            val isDesc = activeFilters.contains("sort_nombre_desc")
                                            CompactItemButton(
                                                item = ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_nombre"),
                                                isSelected = isAsc || isDesc,
                                                onClick = { onAction(if (isAsc) "sort_nombre_desc" else if (isDesc) "" else "sort_nombre_asc") }
                                            )
                                        }
                                        if (showRank) {
                                            val isRAsc = activeFilters.contains("sort_rank_asc")
                                            val isRDesc = activeFilters.contains("sort_rank_desc")
                                            CompactItemButton(
                                                item = ControlItem("Rank", Icons.Default.Star, "⭐", Color(0xFF9B51E0), "sort_rank"),
                                                isSelected = isRAsc || isRDesc,
                                                onClick = { onAction(if (isRAsc) "sort_rank_desc" else if (isRDesc) "" else "sort_rank_asc") }
                                            )
                                        }
                                        if (showViewModes) {
                                            CompactItemButton(
                                                item = ControlItem("Grupos", Icons.Default.GridView, "🍱", Color(0xFF2197F5), "view_bento"),
                                                isSelected = activeFilters.contains("view_bento"),
                                                onClick = { onAction("view_bento") }
                                            )
                                            CompactItemButton(
                                                item = ControlItem("Grilla", Icons.Default.Dashboard, "📱", Color(0xFF9B51E0), "view_grid"),
                                                isSelected = activeFilters.contains("view_grid"),
                                                onClick = { onAction("view_grid") }
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
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun MenuOrdenamientoPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            MenuOrdenamiento(
                activeFilters = setOf("sort_alpha"),
                sortOptions = listOf(ControlItem("Nombre", Icons.Default.SortByAlpha, "ABC", Color(0xFF2197F5), "sort_alpha")),
                onAction = {},
                onApply = {},
                onClearFilters = {}
            )
        }
    }
}










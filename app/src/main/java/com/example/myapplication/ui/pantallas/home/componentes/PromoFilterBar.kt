
/*
package com.example.myapplication.viewmodel.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.TipoCategoriaPromo
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.core.datos.local.entidades.CategoriaEntity
import com.example.myapplication.ui.componentes.be.modelos.SuperCategoria

/**
 * --- PROMO FILTER BAR (ELITE v2026.7) ---
 * [ELITE]: Barra de filtros multi-estado persistente.
 * Es FUNDAMENTAL para la navegación por rubros en el feed de promociones.
 * [LEY #9]: Variables en español y unificación Monolito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoFilterBar(
    tiposSeleccionados: Set<TipoCategoriaPromo>,
    superCategoriasSeleccionadas: Set<String>,
    categoriasSeleccionadas: Set<String>,
    superCategorias: List<SuperCategoria>,
    subCategorias: List<CategoriaEntity>,
    proveedorSubCategorias: (String) -> List<CategoriaEntity>,
    estaCargandoSubCategorias: Boolean,
    esFiltroModificado: Boolean,
    tieneCualquierFiltro: Boolean,
    alAlternarTipo: (TipoCategoriaPromo) -> Unit,
    alAlternarSuperCategoria: (String) -> Unit,
    alNavegarSuperCategoria: (String) -> Unit,
    alAlternarCategoria: (String, String) -> Unit,
    alGuardarFiltros: () -> Unit,
    alLimpiarFiltros: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarMenuTipo by remember { mutableStateOf(false) }
    var mostrarMenuSuper by remember { mutableStateOf(false) }
    var nivelMenuActual by remember { mutableIntStateOf(0) }
    var tituloSuperActiva by remember { mutableStateOf<String?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. FILTRADO POR TIPO (Servicio/Producto)
            item {
                Box {
                    FilterChip(
                        selected = tiposSeleccionados.isNotEmpty(),
                        onClick = { mostrarMenuTipo = true },
                        label = {
                            val etiqueta = if (tiposSeleccionados.size == 1) tiposSeleccionados.first().etiqueta else "TIPOS"
                            Text(text = etiqueta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        },
                        leadingIcon = { Icon(Icons.Default.FilterList, null, Modifier.size(16.dp), tint = SharedPalette.ElectricCyan) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.05f), labelColor = Color.Gray)
                    )
                    DropdownMenu(expanded = mostrarMenuTipo, onDismissRequest = { mostrarMenuTipo = false }, modifier = Modifier.background(Color(0xFF121418))) {
                        FilterMenuItem(
                            label = "Servicios",
                            emoji = "🛠️",
                            state = if (tiposSeleccionados.contains(TipoCategoriaPromo.SERVICIO)) ToggleableState.On else ToggleableState.Off,
                            onClick = { alAlternarTipo(TipoCategoriaPromo.SERVICIO) }
                        )
                        FilterMenuItem(
                            label = "Productos",
                            emoji = "📦",
                            state = if (tiposSeleccionados.contains(TipoCategoriaPromo.PRODUCTO)) ToggleableState.On else ToggleableState.Off,
                            onClick = { alAlternarTipo(TipoCategoriaPromo.PRODUCTO) }
                        )
                    }
                }
            }

            // 2. FILTRADO POR CATEGORÍAS (Bento Drill-down)
            item {
                Box {
                    FilterChip(
                        selected = superCategoriasSeleccionadas.isNotEmpty() || categoriasSeleccionadas.isNotEmpty(),
                        onClick = { 
                            mostrarMenuSuper = true
                            nivelMenuActual = 0
                        },
                        label = { 
                            val total = categoriasSeleccionadas.size
                            val etiqueta = if (total > 0) "$total RUBROS" else "CATEGORÍAS"
                            Text(text = etiqueta, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
                        },
                        leadingIcon = { Icon(Icons.Default.Category, null, Modifier.size(16.dp), tint = SharedPalette.ElectricCyan) },
                        trailingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, Modifier.size(10.dp)) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = Color.White.copy(alpha = 0.05f), labelColor = Color.Gray)
                    )

                    DropdownMenu(
                        expanded = mostrarMenuSuper,
                        onDismissRequest = { mostrarMenuSuper = false },
                        modifier = Modifier.background(Color(0xFF121418)).width(260.dp).heightIn(max = 280.dp)
                    ) {
                        AnimatedContent(
                            targetState = nivelMenuActual,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                                }.using(SizeTransform(clip = false))
                            },
                            label = "CategoryMenuTransition"
                        ) { nivel ->
                            if (nivel == 0) {
                                Column {
                                    superCategorias.forEach { superCat ->
                                        val rubrosHijos = proveedorSubCategorias(superCat.titulo)
                                        val nombresHijos = rubrosHijos.map { it.nombre }
                                        val seleccionadosEnSuper = nombresHijos.filter { categoriasSeleccionadas.contains(it) }
                                        val estadoTri = when {
                                            seleccionadosEnSuper.isEmpty() -> ToggleableState.Off
                                            seleccionadosEnSuper.size == nombresHijos.size && nombresHijos.isNotEmpty() -> ToggleableState.On
                                            else -> ToggleableState.Indeterminate
                                        }
                                        FilterMenuItem(
                                            label = superCat.titulo,
                                            emoji = superCat.icono,
                                            state = estadoTri,
                                            hasSubMenu = true,
                                            onClick = { alAlternarSuperCategoria(superCat.titulo) },
                                            onNavClick = {
                                                tituloSuperActiva = superCat.titulo
                                                alNavegarSuperCategoria(superCat.titulo)
                                                nivelMenuActual = 1
                                            }
                                        )
                                    }
                                }
                            }
else {
                                Column {
                                    DropdownMenuItem(
                                        text = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(14.dp), tint = Color.Gray)
                                                Spacer(Modifier.width(8.dp))
                                                Text("VOLVER", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black) 
                                            }
                                        },
                                        onClick = { nivelMenuActual = 0 }
                                    )
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                    if (estaCargandoSubCategorias) {
                                        repeat(4) { MenuSkeletonItem() }
                                    } else {
                                        subCategorias.forEach { cat ->
                                            FilterMenuItem(
                                                label = cat.nombre,
                                                emoji = cat.icono,
                                                state = if (categoriasSeleccionadas.contains(cat.nombre)) ToggleableState.On else ToggleableState.Off,
                                                onClick = { alAlternarCategoria(cat.nombre, tituloSuperActiva ?: "") }
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

        // --- BOTONES DE CONTROL (SAVE & CLEAR) ---
        AnimatedVisibility(
            visible = esFiltroModificado || tieneCualquierFiltro,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (esFiltroModificado) {
                    FilledIconButton(
                        onClick = alGuardarFiltros,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = SharedPalette.ElectricCyan.copy(alpha = 0.1f),
                            contentColor = SharedPalette.ElectricCyan
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Recordar", modifier = Modifier.size(18.dp))
                    }
                }

                if (tieneCualquierFiltro) {
                    FilledIconButton(
                        onClick = alLimpiarFiltros,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = SharedPalette.RogCrimson.copy(alpha = 0.1f),
                            contentColor = SharedPalette.RogCrimson
                        )
                    ) {
                        Icon(Icons.Default.FilterAltOff, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun FilterMenuItem(
    label: String,
    emoji: String? = null,
    state: ToggleableState = ToggleableState.Off,
    hasSubMenu: Boolean = false,
    onClick: () -> Unit,
    onNavClick: (() -> Unit)? = null
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                TriStateCheckbox(
                    state = state,
                    onClick = onClick,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SharedPalette.ElectricCyan,
                        uncheckedColor = Color.White.copy(alpha = 0.2f),
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.size(24.dp)
                )
                Row(
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (emoji != null) {
                        Text(emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f)))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = label,
                        color = if (state != ToggleableState.Off) Color.White else Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = if (state != ToggleableState.Off) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasSubMenu) {
                        IconButton(onClick = { onNavClick?.invoke() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, Modifier.size(12.dp), tint = Color.White.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        },
        onClick = { if (hasSubMenu && onNavClick != null) onNavClick() else onClick() }
    )
}

@Composable
fun MenuSkeletonItem() {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f)))
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.7f).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.05f)))
    }
}

*/

































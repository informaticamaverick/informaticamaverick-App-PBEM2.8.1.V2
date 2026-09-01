package com.example.myapplication.ui.componentes.sistema.contexto

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.componentes.DropdownItemData
import com.example.myapplication.ui.componentes.sistema.menu.v3.*
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit

/**
 * --- 🛤️ NIVEL 1: BARRA DE FILTROS DUAL (MENÚS + BURBUJAS) ---
 * [ELITE]: Orquestador de filtros y ordenamiento para pantallas de descubrimiento.
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun BarraFiltrosV3(
    modifier: Modifier = Modifier,
    filtrosActivos: List<ModeloBurbujaFiltro>,
    alHacerClickMenu: (String) -> Unit, 
    alEliminarFiltro: (String) -> Unit,
    alLimpiarTodo: () -> Unit,
    mostrarCategorias: Boolean = true,
    mostrarMenuFiltros: Boolean = false,
    mostrarMenuOrdenar: Boolean = false,
    mostrarMenuCategorias: Boolean = false,
    alCerrarMenu: () -> Unit = {},
    idsFiltrosSeleccionados: Set<String> = emptySet(),
    alAlternarFiltro: (String) -> Unit = {},
    itemsCategoria: List<DropdownItemData> = emptyList(),
    itemsFiltro: List<DropdownItemData> = emptyList(),
    itemsOrden: List<DropdownItemData> = emptyList(),
    estaCentrado: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    GatilloMenuV3(
                        etiqueta = "Filtros",
                        icono = Icons.Default.FilterList,
                        alHacerClick = { alHacerClickMenu("filtros") },
                        estaSeleccionado = itemsFiltro.any { idsFiltrosSeleccionados.contains(it.id) },
                        estaMenuAbierto = mostrarMenuFiltros
                    )
                    MoldeMenuArmadorV3(
                        expanded = mostrarMenuFiltros,
                        onDismissRequest = alCerrarMenu,
                        alignment = Alignment.BottomCenter, // 🔥 [FIX]: Alineación inferior para que nazca debajo
                        isCenteredOnScreen = true,
                        verticalOffset = (-14).dp
                    ) {
                        MenuFiltrosContenido(
                            items = itemsFiltro,
                            idsSeleccionados = idsFiltrosSeleccionados,
                            alAlternar = alAlternarFiltro
                        )
                    }
                }
                Box {
                    GatilloMenuV3(
                        etiqueta = "Ordenar",
                        icono = Icons.Default.SwapVert,
                        alHacerClick = { alHacerClickMenu("ordenar") },
                        estaSeleccionado = itemsOrden.any { idsFiltrosSeleccionados.contains(it.id) },
                        estaMenuAbierto = mostrarMenuOrdenar
                    )
                    MoldeMenuArmadorV3(
                        expanded = mostrarMenuOrdenar,
                        onDismissRequest = alCerrarMenu,
                        alignment = Alignment.BottomCenter, // 🔥 [FIX]: Alineación inferior para que nazca debajo
                        isCenteredOnScreen = true,
                        verticalOffset = (-14).dp
                    ) {
                        MenuOrdenContenido(
                            items = itemsOrden,
                            idsSeleccionados = idsFiltrosSeleccionados,
                            alAlternar = alAlternarFiltro
                        )
                    }
                }
                if (mostrarCategorias) {
                    Box {
                        GatilloMenuV3(
                            etiqueta = "Rubros",
                            icono = Icons.Default.Category,
                            alHacerClick = { alHacerClickMenu("categorias") },
                            estaSeleccionado = itemsCategoria.any { idsFiltrosSeleccionados.contains(it.id) },
                            estaMenuAbierto = mostrarMenuCategorias
                        )
                        MoldeMenuArmadorV3(
                            expanded = mostrarMenuCategorias,
                            onDismissRequest = alCerrarMenu,
                            alignment = Alignment.BottomEnd, 
                            isCenteredOnScreen = false,
                            autoArrow = true, // 🔥 [NEW]: Flecha automática aunque esté a la derecha
                            verticalOffset = (-14).dp
                        ) {
                            MenuRubrosContenido(
                                items = itemsCategoria,
                                idsSeleccionados = idsFiltrosSeleccionados,
                                alAlternar = alAlternarFiltro
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = filtrosActivos.isNotEmpty(),
            enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh)) + fadeOut()
        ) {
            val estadoLista = rememberLazyListState()

            LaunchedEffect(filtrosActivos.size) {
                if (filtrosActivos.isNotEmpty()) {
                    estadoLista.animateScrollToItem(filtrosActivos.size - 1)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    state = estadoLista,
                    contentPadding = PaddingValues(start = 16.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filtrosActivos, key = { it.id }) { filtro ->
                        Box(modifier = Modifier.animateItem()) {
                            BurbujaFiltroElite(
                                modelo = filtro,
                                alEliminar = { alEliminarFiltro(filtro.id) }
                            )
                        }
                    }
                }

                // 🔥 [SUPREME]: Botón Limpiar con estilo Premium de Tarjeta
                BotonIconoV3(
                    icono = Icons.Default.DeleteSweep,
                    colorTinte = SharedPalette.ErrorRed,
                    alHacerClick = alLimpiarTodo
                )
            }
        }
    }
}

@Composable
private fun GatilloMenuV3(
    etiqueta: String,
    icono: ImageVector,
    alHacerClick: () -> Unit,
    estaSeleccionado: Boolean = false,
    estaMenuAbierto: Boolean = false 
) {
    val colorAcento = if (estaSeleccionado || estaMenuAbierto) SharedPalette.ElectricCyan else Color.White
    
    Surface(
        onClick = alHacerClick,
        color = if (estaSeleccionado || estaMenuAbierto) colorAcento.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (estaSeleccionado || estaMenuAbierto) colorAcento.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icono, 
                contentDescription = null, 
                tint = colorAcento.copy(alpha = if (estaSeleccionado || estaMenuAbierto) 1f else 0.6f), 
                modifier = Modifier.size(16.dp)
            )
            
            VerticalDivider(
                modifier = Modifier.height(14.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.12f)
            )
            
            TextCompacto(
                text = etiqueta.uppercase(), 
                color = colorAcento, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Black,
                style = TextStyle(letterSpacing = 0.5.sp)
            )

            Icon(
                imageVector = if (estaMenuAbierto) Icons.Default.Close else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (estaMenuAbierto) SharedPalette.RogCrimson else colorAcento.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BotonIconoV3(
    icono: ImageVector,
    colorTinte: Color,
    alHacerClick: () -> Unit
) {
    Surface(
        onClick = alHacerClick,
        modifier = Modifier.size(28.dp), // 🔥 Estilo HeaderActionButton
        color = Color.White.copy(alpha = 0.08f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icono, 
                contentDescription = null, 
                tint = colorTinte, 
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ==========================================================================================
// --- 🧪 SECCIÓN DE PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewBarraFiltrosV3() {
    ClienteTheme {
        var idsSeleccionados by remember { mutableStateOf(setOf("urgente")) }

        val filtros = listOf(
            ModeloBurbujaFiltro("1", "Urgente", "🔥"),
            ModeloBurbujaFiltro("2", "Verificados", "✅", Color(0xFF4ADE80))
        )
        val opciones = listOf(
            DropdownItemData("urgente", "Urgente", emoji = "🔥"),
            DropdownItemData("verificados", "Verificados", emoji = "✅")
        )

        Column(modifier = Modifier.padding(16.dp)) {
            BarraFiltrosV3(
                filtrosActivos = filtros,
                mostrarMenuFiltros = true,
                idsFiltrosSeleccionados = idsSeleccionados,
                itemsFiltro = opciones,
                alAlternarFiltro = { id ->
                    idsSeleccionados = if (idsSeleccionados.contains(id)) idsSeleccionados - id else idsSeleccionados + id
                },
                alHacerClickMenu = { },
                alEliminarFiltro = {},
                alLimpiarTodo = {},
                alCerrarMenu = { }
            )
        }
    }
}

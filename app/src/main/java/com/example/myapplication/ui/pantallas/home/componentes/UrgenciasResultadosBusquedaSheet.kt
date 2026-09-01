package com.example.myapplication.ui.pantallas.home.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.core.dominio.modelos.CategoriaDominio
import com.example.myapplication.ui.componentes.CompactCategoryCard
import com.example.myapplication.ui.componentes.sistema.ShimmerTarjetaCategoriaTactica
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEmergenteV3
import com.example.myapplication.ui.pantallas.home.TacticalTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.PBEMTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * --- URGENCIAS RESULTADOS BUSQUEDA SHEET (v2026.SOLID) ---
 * [PROPÓSITO]: Hoja emergente táctica restructurada modularmente para evitar parches de altura.
 * [LEY #13]: Especialista. Orquesta estados de rubros y filtros.
 */
@Composable
fun UrgenciasResultadosBusquedaSheet(
    isVisible: Boolean,
    onClose: () -> Unit,
    activeFilters: Set<String>,
    rubrosVisibles: List<CategoriaDominio>,
    rubroSeleccionado: CategoriaDominio?, 
    isCargando: Boolean,
    consultaBusqueda: String,
    onToggleFilter: (String) -> Unit,
    onSelectCategory: (CategoriaDominio) -> Unit,
    onClear: () -> Unit,
    interaccionHabilitada: Boolean = true
) {
    val estaBuscandoRubro = consultaBusqueda.isNotEmpty()
    val modoResultadosRadar = rubroSeleccionado != null

    val tituloEstado = when {
        modoResultadosRadar -> "✅ RESULTADO DE LA BÚSQUEDA"
        estaBuscandoRubro -> "🔎 RESULTADOS PARA \"${consultaBusqueda.uppercase()}\""
        else -> "🔥 RUBROS MÁS BUSCADOS"
    }

    MoldeSheetEmergenteV3(
        estaVisible = isVisible,
        alCerrar = onClose,
        showCloseButton = false,
        alturaMaximaFraccion = when {
            modoResultadosRadar -> 0.22f 
            estaBuscandoRubro -> 0.85f   
            else -> 0.40f              // 🔥 [v2026.SOLID]: Espacio real para carrusel de alta visibilidad
        },
        mostrarFondoOscuro = estaBuscandoRubro, 
        paddingInferiorHUD = 4.dp, 
        cabeceraPersonalizada = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tituloEstado,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                if (modoResultadosRadar || estaBuscandoRubro || activeFilters.isNotEmpty()) {
                    TextButton(
                        onClick = onClear,
                        enabled = interaccionHabilitada,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            "LIMPIAR",
                            color = if (interaccionHabilitada) SharedPalette.RogCrimson else Color.Gray.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .imePadding()
                    .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 24.dp), // 🔥 [FIX]: Más aire inferior
                verticalArrangement = Arrangement.spacedBy(10.dp) // 🔥 [REFINED]: Espacio equilibrado
            ) {
                when {
                    modoResultadosRadar -> {
                        EstadoFiltrosTacticos(
                            activeFilters = activeFilters,
                            onToggleFilter = onToggleFilter,
                            interaccionHabilitada = interaccionHabilitada
                        )
                    }
                    estaBuscandoRubro -> {
                        EstadoBusquedaRubrosGrid(
                            rubros = rubrosVisibles,
                            isCargando = isCargando,
                            onSelectCategory = onSelectCategory,
                            interaccionHabilitada = interaccionHabilitada
                        )
                    }
                    else -> {
                        EstadoRubrosSugeridosCarrusel(
                            rubros = rubrosVisibles,
                            isCargando = isCargando,
                            onSelectCategory = onSelectCategory,
                            interaccionHabilitada = interaccionHabilitada
                        )
                    }
                }
            }

            // --- ESCUDO DE BLOQUEO (SHIELD) ---
            if (!interaccionHabilitada) {
                Box(modifier = Modifier.matchParentSize().clickable(enabled = true, onClick = {}))
            }
        }
    }
}

// ==================================================================================
// --- SUB-COMPONENTES DE ESTADO (ESTRUCTURA LIMPIA) ---
// ==================================================================================

@Composable
private fun EstadoFiltrosTacticos(
    activeFilters: Set<String>,
    onToggleFilter: (String) -> Unit,
    interaccionHabilitada: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "CRITERIOS DE CERCANÍA",
            color = TacticalTheme.TextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            TacticalChipElite(
                label = "MENOR A 2KM",
                emoji = "📍",
                isSelected = activeFilters.contains("dist_2km"),
                onClick = { if (interaccionHabilitada) onToggleFilter("dist_2km") }
            )
            TacticalChipElite(
                label = "CON LOCAL FÍSICO",
                emoji = "🏪",
                isSelected = activeFilters.contains("local"),
                onClick = { if (interaccionHabilitada) onToggleFilter("local") }
            )
        }
    }
}

@Composable
private fun EstadoBusquedaRubrosGrid(
    rubros: List<CategoriaDominio>,
    isCargando: Boolean,
    onSelectCategory: (CategoriaDominio) -> Unit,
    interaccionHabilitada: Boolean
) {
    if (isCargando) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(4) { ShimmerTarjetaCategoriaTactica() }
        }
    } else if (rubros.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("SIN COINCIDENCIAS", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 16.dp)
        ) {
            items(rubros) { cat ->
                CompactCategoryCard(
                    item = cat,
                    onClick = { if (interaccionHabilitada) onSelectCategory(cat) }
                )
            }
        }
    }
}

@Composable
private fun EstadoRubrosSugeridosCarrusel(
    rubros: List<CategoriaDominio>,
    isCargando: Boolean,
    onSelectCategory: (CategoriaDominio) -> Unit,
    interaccionHabilitada: Boolean
) {
    if (isCargando) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(4) { ShimmerTarjetaCategoriaTactica() }
        }
    } else if (rubros.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("SIN COINCIDENCIAS", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp), // 🔥 [FIX]: Altura exacta para tarjetas de 132dp de ancho (aspectRatio 0.85)
            contentAlignment = Alignment.CenterEnd
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                contentPadding = PaddingValues(start = 8.dp, end = 16.dp, bottom = 0.dp), // 🔥 [FIX]: Ajuste sin flecha
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(rubros) { cat ->
                    Box(modifier = Modifier.width(132.dp)) { 
                        CompactCategoryCard(
                            item = cat,
                            onClick = { if (interaccionHabilitada) onSelectCategory(cat) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalChipElite(
    label: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) TacticalTheme.Cyan.copy(alpha = 0.15f) else Color(0xFF0F172A).copy(alpha = 0.6f),
        border = BorderStroke(1.dp, if (isSelected) TacticalTheme.Cyan else Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// ==================================================================================
// --- 🧪 SECCIÓN DE PREVIEWS ---
// ==================================================================================

@Preview(name = "Urgencias Sheet - Most Used", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewUrgenciasSheet() {
    PBEMTheme {
        UrgenciasResultadosBusquedaSheet(
            isVisible = true,
            onClose = {},
            activeFilters = setOf("dist_2km"),
            rubrosVisibles = listOf(
                CategoriaDominio(id = "1", nombre = "Cerrajero", icono = "🔑", color = 0xFF22D3EE, idSuperCategoria = "HOGAR", superCategoria = "Hogar"),
                CategoriaDominio(id = "2", nombre = "Fletes", icono = "🛻", color = 0xFF10B981, idSuperCategoria = "LOGISTICA", superCategoria = "Logística"),
                CategoriaDominio(id = "3", nombre = "Auxilio Mecánico", icono = "🆘", color = 0xFFEF4444, idSuperCategoria = "AUTO", superCategoria = "Mecánica Automotriz")
            ),
            rubroSeleccionado = null,
            isCargando = false,
            consultaBusqueda = "",
            onToggleFilter = {},
            onSelectCategory = {},
            onClear = {}
        )
    }
}

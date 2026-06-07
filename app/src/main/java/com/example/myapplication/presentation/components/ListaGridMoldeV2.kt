package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.registry.MaverickIcons

/**
 * ListaGridMoldeV2: Versión evolucionada de ListaMoldeV2 optimizada para cuadrículas (Grids).
 * Utiliza LazyVerticalGrid para soportar layouts de múltiples columnas con cabecera colapsable.
 */
@Composable
fun ListaGridMoldeV2(
    modifier: Modifier = Modifier,
    titulo: String = "SISTEMA DE RESULTADOS",
    subtitulo: String? = null,
    acciones: @Composable (RowScope.(fraction: Float) -> Unit)? = null,
    filtros: @Composable (RowScope.() -> Unit)? = null,
    itemCount: Int? = null,
    perfiles: List<PerfilEmpresa> = emptyList(),
    onPerfilSelected: (PerfilEmpresa) -> Unit = {},
    accentColor: Color = MaverickColors.ElectricCyan,
    customMaxHeaderHeight: Dp = if (filtros != null) 90.dp else 42.dp,
    customMinHeaderHeight: Dp = 40.dp,
    columns: GridCells = GridCells.Fixed(2),
    state: LazyGridState = rememberLazyGridState(),
    containerColor: Color = MaverickColors.EliteSurface,
    content: LazyGridScope.() -> Unit,
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
        LazyVerticalGrid(
            columns = columns,
            state = state,
            modifier = Modifier.fillMaxSize().zIndex(0f),
            contentPadding = PaddingValues(top = customMaxHeaderHeight + 8.dp, bottom = 20.dp, start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            itemCount = itemCount,
            perfiles = perfiles,
            onPerfilSelected = onPerfilSelected,
            collapseFraction = collapseFraction,
            height = headerHeightDp,
            accentColor = accentColor,
            acciones = acciones?.let { { it(collapseFraction) } },
            filtros = filtros
        )
    }
}

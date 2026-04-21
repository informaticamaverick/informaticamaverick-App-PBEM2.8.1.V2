package com.example.myapplication.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import com.example.myapplication.presentation.components.Utilidades.M3VerticalDivider
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.MaverickTacticalButton
import com.example.myapplication.presentation.components.Utilidades.VerticalForkDivider
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Componente Reutilizable: Molde de Barra de Menú y Panel de Contenido.
 * Estilo HeaderGlassCP: Superior redondeado, inferior recto con borde degradado fino.
 * Incluye un contenedor inferior con efecto Glass/Blur para tarjetas y otros elementos.
 */
@Composable
fun MoldeBarraMenu(
    modifier: Modifier = Modifier,
    itemCount: Int = 0,
    showSubscribedOnly: Boolean = false,
    onToggleSubscribed: () -> Unit = {},
    sortByProximity: Boolean = false,
    onToggleProximity: () -> Unit = {},
    isBentoView: Boolean = false,
    onToggleView: () -> Unit = {},
    activeRefinements: Set<String> = emptySet(),
    refinementOptions: List<ControlItem> = emptyList(),
    sortOptions: List<ControlItem> = emptyList(),
    onToggleRefinement: (String) -> Unit = {},
    onClearRefinements: () -> Unit = {},
    onClearSort: () -> Unit = {},
    labelCountMain: String = "PRESTADORES",
    labelCountSub: String = "Encontramos estos",
    showSuscritos: Boolean = true, // NUEVO: Control de visibilidad
    showCercania: Boolean = true,  // NUEVO: Control de visibilidad
    showVista: Boolean = true,     // NUEVO: Control de visibilidad
    showCountBox: Boolean = true,  // NUEVO: Permite ocultar la caja de conteo (para HomeScreen)
    customActions: @Composable (RowScope.() -> Unit)? = null, // NUEVO: Permite inyectar botones personalizados
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ==========================================================================================
        // --- SECCIÓN 1: CABECERA (BARRA DE MENÚ) (EFECTO STRIX APLICADO) ---
        // ==========================================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp) // Altura reducida para un look más moderno y compacto
                .clip(CutCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
                // [SECCIÓN: FONDO STRIX] - Gradiente ROG y Capa de Cristal
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaverickColors.DeepCityBlue,
                            MaverickColors.NightSky,
                           // MaverickColors.NeonCyan
                        )
                    )
                )
                .background(MaverickColors.GlassOverlayRog)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val cornerSize = 10.dp.toPx()
                    val gradient = Brush.horizontalGradient(
                        listOf(CPCyberColors.ElectricPurple, CPCyberColors.MaverickCyan)
                    )

                    // --- CONTORNO SUPERIOR E INFERIOR (Sin líneas verticales laterales) ---
                    val path = Path().apply {
                        // Parte Superior
                        moveTo(0f, cornerSize)
                        lineTo(cornerSize, 0f)
                        lineTo(size.width - cornerSize, 0f)
                        lineTo(size.width, cornerSize)
                        
                        // Parte Inferior (Diagonales y horizontal inferior)
                        moveTo(size.width, size.height - cornerSize)
                        lineTo(size.width - cornerSize, size.height)
                        lineTo(cornerSize, size.height)
                        lineTo(0f, size.height - cornerSize)
                    }

                    drawPath(
                        path = path,
                        brush = gradient,
                        style = Stroke(width = strokeWidth)
                    )
                }
        ) {
            // Capa de superficie sólida (Sin blur, colores vivos)
            Box(
                modifier = Modifier
                    .fillMaxSize()

                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- SECCIÓN IZQUIERDA: CAJA DE CONTEO O TEXTO SIMPLE ---
                    if (showCountBox) {
                        Box(
                            modifier = Modifier
                                .clip(CutCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp))
                                .background(CPCyberColors.MaverickCyan.copy(alpha = 0.12f))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(CPCyberColors.MaverickCyan.copy(alpha = 0.6f), Color.Transparent)
                                    ),
                                    shape = CutCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 10.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(verticalArrangement = Arrangement.spacedBy((-12).dp)) {
                                    Text(text = labelCountSub, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray.copy(alpha = 0.9f))
                                    Text(text = labelCountMain, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                M3VerticalDivider(
                                    modifier = Modifier.height(32.dp).padding(horizontal = 8.dp),
                                    color = CPCyberColors.MaverickCyan.copy(alpha = 0.4f)
                                )
                                Text(text = "$itemCount", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            }
                        }
                    } else {
                        // Variante para HomeScreen: Texto elegante sin caja de conteo con soporte para subtítulo/helper
                        Column(
                            modifier = Modifier.padding(start = 10.dp),
                            //verticalArrangement = Arrangement.Center
                            verticalArrangement = Arrangement.spacedBy((-12).dp)
                        ) {
                            if (labelCountSub.isNotEmpty()) {
                                Text(
                                    text = labelCountSub,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = labelCountMain,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))



                    // --- SECCIÓN DERECHA: ACCIONES (PERSONALIZADAS O POR DEFECTO) ---
                    if (customActions != null) {
                        customActions()
                    } else {
                         if (showSuscritos) {
                            ActionColumnWithLabel(label = "Suscritos", active = showSubscribedOnly) {
                                BotonFiltroSuscritosPremium(showSubscribedOnly, onToggleSubscribed)
                            }
                             Spacer(Modifier.width(8.dp)) }

                         if (showCercania) {
                             ActionColumnWithLabel(label = "Cercanía", active = sortByProximity) {
                                 MaverickTacticalButton(isActive = sortByProximity, accentColor = MaverickColors.AcidGreen, onClick = onToggleProximity) {
                                    Text("📍", fontSize = 20.sp)
                                 }
                             }
                             Spacer(Modifier.width(8.dp))
                         }

                         if (showVista) {
                             ActionColumnWithLabel(label = if (isBentoView) "Expandir" else "Compacto", active = isBentoView) {
                                 BotonVista(isBentoView, isBentoView, onToggleView)
                             }
                            Spacer(Modifier.width(14.dp))
                         }


                         ActionColumnWithLabel(label = "Filtro", active = activeRefinements.any { it.startsWith("filter_") || it.startsWith("cat_") }) {
                            MenuFiltros(
                                activeFilters = activeRefinements,
                                dynamicCategories = emptyList(),
                                refinementFilters = refinementOptions,
                                onAction = onToggleRefinement,
                                onApply = {},
                                onClearFilters = onClearRefinements
                            )
                         }

                        if (sortOptions.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                             ActionColumnWithLabel(label = "Orden", active = activeRefinements.any { it.startsWith("sort_") }) {
                                MenuOrdenamiento(
                                    activeFilters = activeRefinements,
                                    sortOptions = sortOptions,
                                    onAction = onToggleRefinement,
                                    onApply = {},
                                    onClearFilters = onClearSort
                                )
                             }
                        }
                    }
                }
            }
        }

        // ==========================================================================================
        // --- SECCIÓN 2: CONTENEDOR DE CONTENIDO (EFECTO STRIX APLICADO) ---
        // ==========================================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Contenedor real para los elementos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CutCornerShape(10.dp))
                    // [SECCIÓN: FONDO STRIX] - Gradiente Vertical ROG y Capa de Cristal
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaverickColors.NeonCyan,
                                MaverickColors.DeepPurple,
                                MaverickColors.VantaBlack
                            )
                        )
                    )
                    .background(MaverickColors.GlassOverlayRog)
                    // [SECCIÓN: BORDE TÁCTICO] - Margen horizontal superior y recortes en gris
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val cornerSize = 10.dp.toPx()
                        val grayColor = Color.Gray.copy(alpha = 0.9f)

                        val path = Path().apply {
                            // Dibujamos el margen horizontal superior respetando los recortes
                            moveTo(0f, cornerSize)
                            lineTo(cornerSize, 0f)
                            lineTo(size.width - cornerSize, 0f)
                            lineTo(size.width, cornerSize)
                        }
                        drawPath(path = path, color = grayColor, style = Stroke(width = strokeWidth))
                    }
                    .padding(start = 4.dp, end = 4.dp, top = 0.dp, bottom = 10.dp), // Ajuste: Máximo ancho y pegado arriba
                content = content
            )
        }
    }
}

/** HELPER: Envuelve los botones con una etiqueta inferior */
@Composable
fun ActionColumnWithLabel(
    label: String, 
    active: Boolean, 
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
       // Text(
        //    text = label,
        //    fontSize = 7.5.sp,
         //   fontWeight = FontWeight.Bold,
         //   color = if (active) Color.White else Color.Gray.copy(alpha = 0.7f)
        //)
    }

}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun MoldeBarraMenuPreview() {
    MyApplicationTheme {
        val refinementOptions = listOf(
            ControlItem("24hs", Icons.Default.AccessTimeFilled, "🕒", Color(0xFFFF9800), "24h")
        )
        
        Box(modifier = Modifier.padding(16.dp)) {
            MoldeBarraMenu(
                itemCount = 12,
                showSubscribedOnly = true,
                onToggleSubscribed = {},
                sortByProximity = false,
                onToggleProximity = {},
                isBentoView = false,
                onToggleView = {},
                activeRefinements = setOf("24h"),
                refinementOptions = refinementOptions,
                onToggleRefinement = {},
                onClearRefinements = {},
                content = {
                    // Ejemplo de contenido interno
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("TARJETA DE EJEMPLO", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun ActionColumnWithLabelPreview() {
    MyApplicationTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0E14))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionColumnWithLabel(
                label = "Activo",
                active = true
            ) {
                MaverickTacticalButton(
                    onClick = {},
                    accentColor = Color.Cyan
                ) {
                    Text("A", color = Color.White)
                }
            }

            ActionColumnWithLabel(
                label = "Inactivo",
                active = false
            ) {
                MaverickTacticalButton(
                    onClick = {},
                    accentColor = Color.Gray,
                    isActive = false
                ) {
                    Text("I", color = Color.White)
                }
            }
        }
    }
}

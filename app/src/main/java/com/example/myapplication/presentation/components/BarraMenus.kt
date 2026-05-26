
/**
package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.components.M3VerticalDivider
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.MaverickTacticalButton
import com.example.myapplication.presentation.designsystem.components.HeaderHUDElite
import com.example.myapplication.presentation.designsystem.components.CountHUD
import com.example.myapplication.presentation.designsystem.components.HeaderSimpleTitle
import com.example.myapplication.presentation.designsystem.components.HUDActionItem
import com.example.myapplication.presentation.designsystem.components.BotonVista
import com.example.myapplication.presentation.designsystem.components.BotonFiltroSuscritosPremium
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

/**
 * Componente Reutilizable: Molde de Barra de Menú y Panel de Contenido.
 * Estilo ELITE HUD (2025): Refined Glassmorphism, Luminous Adaptive Borders, Obsidian Base.
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
    showSuscritos: Boolean = true,
    showCercania: Boolean = true,
    showVista: Boolean = true,
    showCountBox: Boolean = true,
    customActions: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ==========================================================================================
        // --- SECCIÓN 1: CABECERA ELITE HUD (Compacta & Centrada) ---
        // ==========================================================================================
        HeaderHUDElite {
            // --- SUBSECCIÓN: TÍTULO / COUNT (CENTRADO) ---
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (showCountBox) {
                    CountHUD(
                        count = itemCount,
                        mainLabel = labelCountMain,
                        subLabel = labelCountSub
                    )
                } else {
                    HeaderSimpleTitle(mainLabel = labelCountMain, subLabel = labelCountSub)
                }
            }

            // --- SUBSECCIÓN: ACTION TOOLBAR (DERECHA) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (customActions != null) {
                    customActions()
                } else {
                    if (showSuscritos) {
                        HUDActionItem(label = "VIP", active = showSubscribedOnly) {
                            BotonFiltroSuscritosPremium(showSubscribedOnly, onToggleSubscribed)
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    if (showCercania) {
                        HUDActionItem(label = "GPS", active = sortByProximity) {
                            MaverickTacticalButton(
                                isActive = sortByProximity, 
                                accentColor = MaverickColors.AcidGreen, 
                                onClick = onToggleProximity
                            ) {
                                Text("📍", fontSize = 16.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    if (showVista) {
                        HUDActionItem(label = if (isBentoView) "EXP" else "COM", active = isBentoView) {
                            BotonVista(isBentoView, isBentoView, onToggleView)
                        }
                        Spacer(Modifier.width(12.dp))
                    }

                    HUDActionItem(label = "Filtro", active = activeRefinements.any { it.startsWith("filter_") || it.startsWith("cat_") }) {
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
                        HUDActionItem(label = "Orden", active = activeRefinements.any { it.startsWith("sort_") }) {
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

        // ==========================================================================================
        // --- SECCIÓN 2: CONTENEDOR DE CONTENIDO ---
        // ==========================================================================================
        if (content != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaverickColors.ROG_Dark_Bg)
                    .drawBehind {
                        // Pattern
                        val spacing = 32.dp.toPx()
                        for (x in 0..size.width.toInt() step spacing.toInt()) {
                            for (y in 0..size.height.toInt() step spacing.toInt()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.03f),
                                    radius = 0.8.dp.toPx(),
                                    center = Offset(x.toFloat(), y.toFloat())
                                )
                            }
                        }
                    }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), content = content)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun MoldeBarraMenuPreview() {
    MyApplicationTheme {
        val refinementOptions = listOf(
            ControlItem("24hs", Icons.Default.AccessTimeFilled, "🕒", Color(0xFFFF9800), "24h")
        )
        
        Box(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
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
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SYSTEM MODULE 0$it", color = Color.White.copy(alpha = 0.5f), style = CyberTypography.MonospaceData)
                            }
                        }
                    }
                }
            )
        }
    }
}
**/
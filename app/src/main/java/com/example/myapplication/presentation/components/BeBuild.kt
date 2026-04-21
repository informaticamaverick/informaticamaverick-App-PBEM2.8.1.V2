package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.BranchClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.presentation.components.Utilidades.*
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoBorderBrush
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoDarkGlassBackground
import com.example.myapplication.presentation.components.Utilidades.MaverickColors.BentoGlassBrush
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==================================================================================
// --- MODELOS DE DATOS ---
// ==================================================================================
data class BeSmallActionModel(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val emoji: String? = null,
    val isVisible: Boolean = true,
    val isSelected: Boolean = false,
    val isDefault: Boolean = false,
    val tint: Color = Color.White,
    val onClick: () -> Unit = {}
)

// ==================================================================================
// --- SECCIÓN: MODELO DE DATOS PARA UBICACIÓN (ENRIQUECIDO V3) ---
// ==================================================================================
data class AddressInfo(
    val id: String,
    val companyOrUserName: String,
    val branchName: String,
    val streetAndNumber: String,
    val locality: String,
    val province: String = "",
    val country: String = "",
    val postalCode: String,
    val isCompany: Boolean,
    val lat: Double,
    val lng: Double
)

// ==================================================================================
// --- COMPONENTES DE LA BARRA DE ACCIONES ---
// ==================================================================================

@Composable
fun BeActionsBar(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default",
    showOnlyDefault: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    isToolbarStable: Boolean = true // 🔥 AHORA SE RECIBE DEL COREÓGRAFO (BeAssistantViewModel)
) {
    val filteredActions = remember(actions, showOnlyDefault) {
        actions.filter { it.isVisible && it.isDefault == showOnlyDefault }
    }

    // 🔥 MODIFICACIÓN: La barra es visible si hay acciones o contenido
    val actuallyVisible = isVisible && (filteredActions.isNotEmpty() || leadingContent != null)
    val isProfileContext = toolboxKey.startsWith("profile")

    // ==================================================================================
    // --- SECCIÓN: CONFIGURACIÓN VISUAL DE LA BARRA DINÁMICA (HUD V6.1) ---
    // ==================================================================================
    // Altura ajustada para los iconos (80dp)
    val toolboxHeight = 80.dp 
    val sidePadding = if (isProfileContext) 12.dp else 16.dp
    val spacing = if (showOnlyDefault) 5.dp else 8.dp

    val sharedSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // 🔥 ANIMACIÓN DE ENTRADA DESDE ABAJO (Sincronizada con el Coreógrafo)
    val offsetY by animateIntOffsetAsState(
        targetValue = if (isToolbarStable) IntOffset(0, 0) else IntOffset(0, 100),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ToolbarStableOffset"
    )

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 0.dp) // 🔥 CAMBIADO A 0.DP PARA AJUSTE PERFECTO CON NAV BAR
        .offset { offsetY } // 🔥 SE APLICA EL OFFSET DE ESTABILIDAD
        .zIndex(0.5f), contentAlignment = Alignment.BottomEnd) {

        // ==================================================================================
        // --- SECCIÓN: BARRA DE HERRAMIENTAS DINÁMICA (ESTILO DRAWER DERECHO) ---
        // El fondo ahora pasa por detrás del asistente y llega hasta el borde derecho.
        // Las puntas son menos redondeadas (20dp).
        // Reemplaza al scrim de fondo completo anterior.
        // ==================================================================================
        AnimatedVisibility(
            visible = actuallyVisible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = sharedSpring) + fadeIn(tween(400)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = sharedSpring) + fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .height(toolboxHeight)
                    // SE ELIMINA EL PADDING END AQUÍ PARA QUE EL FONDO LLEGUE AL BORDE (Pasando por detrás de Be)
                    .animateContentSize(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        alignment = Alignment.CenterEnd // 📌 Anclamos el crecimiento al lado derecho
                    )
                    // ==================================================================================
                    // --- SECCIÓN: FONDO CON CORTES DE ESQUINA (10DP) ---
                    // ==================================================================================
                    .background(
                        brush = verticalGradient(listOf(Color.Black.copy(alpha = 0.95f), Color.Black, Color.Black)),
                        shape = CutCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                    )
                    // ==================================================================================
                    // --- SECCIÓN: BARRA SIN CONTENEDOR (SÓLO ICONOS FLOTANTES) ---
                    // Se elimina el fondo y los bordes para que los iconos aparezcan sin contenedor,
                    // según lo solicitado ("solamente tenga los iconos sin ningún tipo de círculo o contenedor").
                    // ==================================================================================
                    .pointerInput(Unit) {
                        detectTapGestures { /* BLOQUEO */ }
                    }
            ) {
                AnimatedContent(
                    targetState = toolboxKey,
                    transitionSpec = {
                        (slideInHorizontally(tween(400)) { it } + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)))
                    },
                    contentAlignment = Alignment.CenterEnd, // 📌 Mantiene el contenido pegado a la derecha durante la transición
                    label = "BeActionsAnimation"
                ) { targetKey ->
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        // --- SECCIÓN: CONTENIDO DE HERRAMIENTAS (LazyRow) ---
                        LazyRow(
                            modifier = Modifier
                                .wrapContentWidth()
                                .fillMaxHeight()
                                .padding(top = 1.dp, end = 80.dp) // Reducimos el end para que encaje mejor
                                .clipToBounds(),
                            contentPadding = PaddingValues(start = 2.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            if (leadingContent != null) {
                                item(key = "leading_tool") {
                                    Box(modifier = Modifier.animateItem()) {
                                        AnimatedVisibility(
                                            visible = true,
                                            enter = if (isToolbarStable) slideInVertically { it } + fadeIn() else fadeIn(),
                                            exit = slideOutVertically { it } + fadeOut()
                                        ) {
                                            leadingContent()
                                        }
                                    }
                                }
                            }

                            items(filteredActions, key = { it.id }) { action ->
                                Box(modifier = Modifier.animateItem()) {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = if (isToolbarStable) slideInVertically { it } + fadeIn() else fadeIn(),
                                        exit = slideOutVertically { it } + fadeOut()
                                    ) {
                                        if (action.id.startsWith("divider_v")) {
                                            PremiumVerticalDivider(modifier = Modifier.padding(horizontal = 2.dp), height = 36.dp)
                                        } else {
                                            BeSmallActionButton(
                                                label = action.label,
                                                onClick = action.onClick,
                                                icon = if (action.emoji == null) action.icon else null,
                                                emoji = action.emoji,
                                                isSelected = action.isSelected,
                                                tint = action.tint,
                                                modifier = Modifier.width(52.dp)
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




@Composable
fun BeSmallActionsBuilder(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default",
    leadingContent: @Composable (() -> Unit)? = null,
    isToolbarStable: Boolean = true
) {
    BeActionsBar(isVisible = isVisible, actions = actions, shouldShowBottomBar = shouldShowBottomBar, toolboxKey = toolboxKey, showOnlyDefault = false, leadingContent = leadingContent, isToolbarStable = isToolbarStable)
}

@Composable
fun BeDefaultActionsBand(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default",
    leadingContent: @Composable (() -> Unit)? = null,
    isToolbarStable: Boolean = true
) {
    BeActionsBar(isVisible = isVisible, actions = actions, shouldShowBottomBar = shouldShowBottomBar, toolboxKey = toolboxKey, showOnlyDefault = true, leadingContent = leadingContent, isToolbarStable = isToolbarStable)
}


@Preview(showBackground = true)
@Composable
fun BeSmallActionsBuilderPreview() {
    val sampleActions = listOf(
        BeSmallActionModel("4", Icons.Default.Share, "Compartir", emoji = "📤", isDefault = true) {},
        BeSmallActionModel("5", Icons.Default.Delete, "Borrar", emoji = "🗑️", tint = Color.Red) {}
    )
    MyApplicationTheme {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(16.dp), contentAlignment = Alignment.BottomStart) {
            BeActionsBar(isVisible = true, actions = sampleActions)
        }
    }
}

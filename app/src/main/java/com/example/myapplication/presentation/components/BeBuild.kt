package com.example.myapplication.presentation.components

import com.example.myapplication.presentation.features.home.*

import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.home.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myapplication.presentation.designsystem.components.*
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

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
    val ownerId: String? = null, // ID del propietario (User o Company)
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

/** 
 * --- EXTENSIÓN UNIFICADA (MAVERICK HUD V7) ---
 * Convierte AddressInfo a LocationOption para coherencia visual y de búsqueda.
 * Esta es la ÚNICA FUENTE DE VERDAD para la conversión.
 */
fun AddressInfo.toLocationOption(): com.example.myapplication.presentation.features.home.LocationOption {
    return if (this.isCompany) {
        com.example.myapplication.presentation.features.home.LocationOption.Business(
            companyName = this.companyOrUserName,
            branchName = this.branchName,
            address = this.streetAndNumber,
            number = "", // El número ya está en streetAndNumber
            locality = this.locality,
            province = this.province,
            country = this.country,
            postalCode = this.postalCode,
            id = this.id // 🔥 IMPORTANTE: Mantenemos el ID real para comparación
        )
    } else if (this.id == "gps_current") {
        com.example.myapplication.presentation.features.home.LocationOption.Gps(
            address = this.streetAndNumber,
            locality = this.locality,
            province = this.province,
            country = this.country,
            postalCode = this.postalCode,
            lat = this.lat,
            lng = this.lng,
            id = "gps_current"
        )
    } else {
        com.example.myapplication.presentation.features.home.LocationOption.Personal(
            address = this.streetAndNumber,
            number = "", // El número ya está en streetAndNumber
            locality = this.locality,
            province = this.province,
            country = this.country,
            postalCode = this.postalCode,
            id = this.id // 🔥 IMPORTANTE: Mantenemos el ID real para comparación
        )
    }
}

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
    isToolbarStable: Boolean = true // 🔥 AHORA SE RECIBE DEL COREÓGRAFO (BeAssistantViewModel)
) {
    val filteredActions = remember(actions, showOnlyDefault) {
        actions.filter { it.isVisible && it.isDefault == showOnlyDefault }
    }

    // 🔥 MODIFICACIÓN: La barra es visible si hay acciones
    val actuallyVisible = isVisible && filteredActions.isNotEmpty()
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

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 0.dp) // 🔥 AJUSTE PERFECTO CON NAV BAR
        .zIndex(0.5f), contentAlignment = Alignment.BottomEnd) {

        // ==================================================================================
        // --- SECCIÓN: BARRA DE HERRAMIENTAS DINÁMICA (ESTILO DRAWER DERECHO) ---
        // ==================================================================================
        AnimatedVisibility(
            visible = actuallyVisible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = sharedSpring) + fadeIn(tween(400)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = sharedSpring) + fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .height(toolboxHeight)
                    .animateContentSize(
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        alignment = Alignment.CenterEnd 
                    )
                    .background(
                        brush = verticalGradient(listOf(Color.Black.copy(alpha = 0.95f), Color.Black, Color.Black)),
                        shape = CutCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { /* BLOQUEO */ }
                    }
            ) {
                AnimatedContent(
                    targetState = toolboxKey,
                    transitionSpec = {
                        (slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(tween(400)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(tween(300)))
                    },
                    contentAlignment = Alignment.CenterEnd,
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
                                .padding(top = 1.dp, end = 80.dp)
                                .clipToBounds(),
                            contentPadding = PaddingValues(start = 2.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            items(filteredActions, key = { it.id }) { action ->
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
                                        modifier = Modifier.width(52.dp).animateItem()
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




@Composable
fun BeSmallActionsBuilder(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default",
    isToolbarStable: Boolean = true
) {
    BeActionsBar(isVisible = isVisible, actions = actions, shouldShowBottomBar = shouldShowBottomBar, toolboxKey = toolboxKey, showOnlyDefault = false, isToolbarStable = isToolbarStable)
}

@Composable
fun BeDefaultActionsBand(
    isVisible: Boolean,
    actions: List<BeSmallActionModel>,
    shouldShowBottomBar: Boolean = true,
    toolboxKey: String = "default",
    isToolbarStable: Boolean = true
) {
    BeActionsBar(isVisible = isVisible, actions = actions, shouldShowBottomBar = shouldShowBottomBar, toolboxKey = toolboxKey, showOnlyDefault = true, isToolbarStable = isToolbarStable)
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










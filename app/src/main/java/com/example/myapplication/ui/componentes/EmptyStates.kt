package com.example.myapplication.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.estilos.CyberTypography
import com.example.myapplication.uishared.estilos.AppIcons

/**
 * ==========================================================================================
 * --- 🛡️ MAVERICK ELITE UI v3.1: ESTADOS VACÍOS Y FEEDBACK TÁCTICO ---
 * ==========================================================================================
 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyFiltersState(
    activeFilters: Set<String>,
    filterDropdownItems: List<DropdownItemData>,
    sortDropdownItems: List<DropdownItemData> = emptyList(),
    categoryItems: List<DropdownItemData> = emptyList(),
    customMessage: String? = null,
    onClearFilters: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 🧊 NÚCLEO VISUAL ---
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SharedPalette.MagentaNeon.copy(alpha = 0.1f), CircleShape)
                .drawBehind {
                    drawCircle(
                        color = SharedPalette.MagentaNeon,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 1.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("🔍", fontSize = 32.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "SIN RESULTADOS",
            style = CyberTypography.TitleTech.copy(
                color = Color.White,
                fontSize = 18.sp,
                letterSpacing = 2.sp
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = customMessage ?: "Tu configuración actual de filtros es demasiado restrictiva:",
            style = CyberTypography.MonospaceData.copy(
                color = Color.Gray,
                fontSize = 12.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        // --- 🫧 BURBUJAS DE FILTROS ACTIVOS (SSOT) ---
        if (activeFilters.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeFilters.forEach { filterId ->
                    val filterInfo = remember(filterId, filterDropdownItems, sortDropdownItems, categoryItems) {
                        filterDropdownItems.find { it.id == filterId }
                            ?: sortDropdownItems.find { it.id == filterId }
                            ?: categoryItems.find { it.id == filterId }
                    }
                    
                    if (filterInfo != null) {
                        Surface(
                            color = SharedPalette.MagentaNeon.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SharedPalette.MagentaNeon.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(filterInfo.emoji ?: "🔹", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = filterInfo.label.uppercase(),
                                    style = CyberTypography.MonospaceData.copy(
                                        color = SharedPalette.MagentaNeon,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }

        // --- ⚡ BOTONES DE ACCIÓN TÁCTICA ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onClearFilters,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedPalette.MagentaNeon.copy(alpha = 0.15f),
                    contentColor = SharedPalette.MagentaNeon
                ),
                border = BorderStroke(1.dp, SharedPalette.MagentaNeon),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = AppIcons.FilterOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "LIMPIEZA TÁCTICA",
                    style = CyberTypography.MonospaceData.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )
            }

            if (secondaryActionLabel != null && onSecondaryAction != null) {
                TextButton(
                    onClick = onSecondaryAction
                ) {
                    Text(
                        text = secondaryActionLabel.uppercase(),
                        style = CyberTypography.MonospaceData.copy(
                            color = SharedPalette.ElectricCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}


































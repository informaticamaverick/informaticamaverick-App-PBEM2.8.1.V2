package com.example.myapplication.presentation.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmptyFiltersState(
    activeFilters: Set<String>,
    filterDropdownItems: List<DropdownItemData>,
    sortDropdownItems: List<DropdownItemData>,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaverickColors.MagentaNeon.copy(alpha = 0.1f), CircleShape)
                .drawBehind {
                    drawCircle(
                        color = MaverickColors.MagentaNeon,
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
            text = "No hay resultados que coincidan con los filtros aplicados:",
            style = CyberTypography.MonospaceData.copy(
                color = Color.Gray,
                fontSize = 12.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            activeFilters.forEach { filterId ->
                val filterInfo = remember(filterId) {
                    filterDropdownItems.find { it.id == filterId }
                        ?: sortDropdownItems.find { it.id == filterId }
                }
                
                if (filterInfo != null) {
                    Surface(
                        color = MaverickColors.MagentaNeon.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaverickColors.MagentaNeon.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(filterInfo.emoji ?: "🔹", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = filterInfo.label.uppercase(),
                                style = CyberTypography.MonospaceData.copy(
                                    color = MaverickColors.MagentaNeon,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onClearFilters,
            colors = ButtonDefaults.buttonColors(containerColor = MaverickColors.MagentaNeon.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaverickColors.MagentaNeon),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(MaverickIcons.FilterOff, null, tint = MaverickColors.MagentaNeon, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "LIMPIEZA TÁCTICA",
                style = CyberTypography.MonospaceData.copy(
                    color = MaverickColors.MagentaNeon,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            )
        }
    }
}

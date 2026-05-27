package com.example.myapplication.presentation.components

// === IMPORTS ===
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.registry.MaverickIcons
import com.example.myapplication.presentation.designsystem.components.DepthDividerThemedVertical
import com.example.myapplication.presentation.designsystem.components.IOSStylePill
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

// ==================================================================================
// --- SECCIÓN 1: COMPONENTE SEPARADOR ---
// ==================================================================================

/**
 * SeparadorFechaPremium: Burbuja moderna para agrupaciones por fecha.
 * Estilo "Elite Glass" con bordes neón y toggle de expansión.
 * SECCIÓN: Centrado absoluto y anatomía táctica.
 */
@Composable
fun SeparadorFechaPremium(
    fecha: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- ANIMACIONES DE ESTADO ---
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ArrowRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // --- BURBUJA ESTILO iOS MODO OSCURO ---
        IOSStylePill(
            text = "", // Contenido personalizado
            backgroundColor = Color(0x661E293B), // Fondo oscuro traslúcido (iOS Dark)
            borderColor = Color(0x33FFFFFF),    // Brillo sutil arriba
            textColor = Color.White,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() },
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    // 1. EMOJI
                    Box(modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)) {
                        Text("📅", fontSize = 12.sp)
                    }

                    // 2. DIVIDER PEQUEÑO CON PROFUNDIDAD
                    DepthDividerThemedVertical(
                        modifier = Modifier
                            .height(14.dp)
                            .align(Alignment.CenterVertically)
                    )

                    // 3. TEXTO DE LA FECHA
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = fecha.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.5.sp
                            )
                        )
                    }

                    // 4. DIVIDER VERTICAL COMPLETO
                    DepthDividerThemedVertical(
                        modifier = Modifier.fillMaxHeight()
                    )

                    // 5. FLECHA DE TOGGLE
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaverickIcons.ChevronDown,
                            contentDescription = "Expandir/Colapsar",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotation)
                        )
                    }
                }
            }
        )
    }
}

// ==================================================================================
// --- SECCIÓN 2: PREVIEWS ---
// ==================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun SeparadorFechaPreview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117)).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SeparadorFechaPremium(fecha = "Hoy, 19 Mayo", isExpanded = true, onToggle = {})
            SeparadorFechaPremium(fecha = "Ayer, 18 Mayo", isExpanded = false, onToggle = {})
        }
    }
}

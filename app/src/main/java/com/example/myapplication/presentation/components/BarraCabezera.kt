package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import com.example.myapplication.presentation.components.Utilidades.CyberTypography
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.shakeClick

/**
 * Barra de Encabezado Maverick V4 (Premium Pro - Dinámica).
 * 
 * Mejoras aplicadas:
 * - Fondo inmersivo que llega hasta la barra de estado.
 * - statusBarsPadding interno para proteger el contenido.
 * - Triángulo de corte en Negro Mate.
 * - Lógica de colapso basada en [collapseFraction].
 */
@Composable
fun BarraCabezera(
    title: String,
    subtitle: String,
    emoji: String,
    onBack: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    collapseFraction: Float = 0f, // 0.0 (Expandido) a 1.0 (Colapsado)
    accentColor: Color = Color.Cyan,
    borderColor: Color = Color.LightGray.copy(alpha = 0.6f),
    backgroundBrush: Brush = MaverickColors.RogHorizontalGradient
) {
    // ==========================================================================================
    // --- 🌌 SECCIÓN: CONTENEDOR INMERSIVO (FONDO HASTA LA STATUS BAR) ---
    // ==========================================================================================
    Box(
        modifier = modifier
            .fillMaxWidth()
            // --- CORTE TRANSPARENTE APLICADO AL CONTENEDOR ---
            .clip(CutCornerShape(bottomEnd = 15.dp, bottomStart = 15.dp))
            .background(backgroundBrush) 
    ) {
        // --- SECCIÓN: CONTENIDO PROTEGIDO (BAJO LA BARRA DE ESTADO) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // Empuja el contenido para no quedar detrás de la barra de estado
                .height(95.dp * (1f - (collapseFraction * 0.45f))) // Altura dinámica directamente en el modifier
        ) {
            // --- EMOJI (ZONA DE FONDO DINÁMICA) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = emoji,
                    fontSize = 90.sp * (1f - (collapseFraction * 0.55f)), // Tamaño de emoji inlined
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(
                            x = 20.dp, 
                            y = (4.dp * (1f - collapseFraction)) // Animación de subida
                        )
                )
            }

            // --- BORDE PINTADO (DRAWBEHIND CON VALORES DIRECTOS) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        
                        val path = Path().apply {
                            // Diagonal Inferior Izquierda (Corte de 10dp directo)
                            moveTo(0f, size.height - 15.dp.toPx())
                            lineTo(15.dp.toPx(), size.height)
                            
                            // Línea Horizontal Inferior
                            lineTo(size.width - 15.dp.toPx(), size.height)
                            
                            // Diagonal Inferior Derecha (Corte de 48dp directo)
                            lineTo(size.width, size.height - 15.dp.toPx())
                        }
                        
                        drawPath(
                            path = path,
                            color = CPCyberColors.ElectricPurple,
                            style = Stroke(width = strokeWidth)
                        )
                    }
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- BOTÓN BACK (TAMAÑO CALCULADO EN EL MODIFIER) ---
                Box(
                    modifier = Modifier
                        .size(46.dp * (1f - (collapseFraction * 0.2f)))
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, borderColor.copy(alpha = 0.3f), CircleShape)
                        .shakeClick { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp * (1f - collapseFraction * 0.2f))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // --- TÍTULOS (ESTILOS Y ANIMACIONES DIRECTAS) ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        style = CyberTypography.TitleTech.copy(
                            color = Color.White,
                            fontSize = 18.sp * (1f - (collapseFraction * 0.15f)), // Tamaño inlined
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    )
                    
                    // Cálculo de visibilidad inlined
                    if ((1f - collapseFraction * 2.5f).coerceIn(0f, 1f) > 0.05f) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .graphicsLayer { 
                                    alpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f) 
                                }
                        ) {
                            Text(
                                text = subtitle,
                                style = CyberTypography.BodyCyber.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.shakeClick { onInfoClick() }) {
                                Text(text = "❗", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Header Dinámico Maverick", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewBarraCabezera() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Expandida
        BarraCabezera(
            title = "Zona Expandida",
            subtitle = "Scroll en 0%",
            emoji = "🛰️",
            collapseFraction = 0f,
            onBack = { },
            onInfoClick = { }
        )
        
        // Mitad de camino
        BarraCabezera(
            title = "Zona Media",
            subtitle = "Scroll en 50%",
            emoji = "🛰️",
            collapseFraction = 0.5f,
            onBack = { },
            onInfoClick = { }
        )

        // Colapsada
        BarraCabezera(
            title = "Zona Colapsada",
            subtitle = "Solo Título",
            emoji = "🛰️",
            collapseFraction = 1f,
            onBack = { },
            onInfoClick = { }
        )
    }
}

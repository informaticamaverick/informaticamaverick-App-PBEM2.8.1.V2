package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- MOLDE DE MULTISELECCIÓN SOBERANO (V2026.ELITE) ---
 * [LEY #13]: Infraestructura visual unificada para todo el ecosistema.
 * [DISEÑO]: Inspirado en Google Photos y Telegram Elite.
 * 
 * PROPÓSITO: Envolver cualquier tarjeta para dotarla de capacidades de selección,
 * animaciones de escala y feedback háptico.
 */
@Composable
fun MoldeMultiSeleccion(
    estaSeleccionado: Boolean,
    modoMultiseleccionActivo: Boolean,
    modifier: Modifier = Modifier,
    colorAcento: Color = SharedPalette.ElectricCyan,
    radioCurvatura: Dp = 16.dp,
    mostrarTilde: Boolean = true,
    contenido: @Composable () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    // 1. Animación de Escala (Reducción sutil para "hundir" la selección)
    val escala by animateFloatAsState(
        targetValue = if (estaSeleccionado) 0.96f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "escalaSeleccion"
    )

    // 2. Animación de Borde
    val anchoBorde by animateDpAsState(
        targetValue = if (estaSeleccionado) 2.5.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "anchoBorde"
    )

    // 3. Disparar Vibración al entrar en modo (Feedback Táctico Ley #11)
    LaunchedEffect(estaSeleccionado) {
        if (estaSeleccionado) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .clip(RoundedCornerShape(radioCurvatura))
            .then(
                if (estaSeleccionado) {
                    Modifier.border(
                        width = anchoBorde,
                        color = colorAcento,
                        shape = RoundedCornerShape(radioCurvatura)
                    )
                } else Modifier
            )
    ) {
        // --- EL COMPONENTE BASE ---
        contenido()

        // --- OVERLAY DE COLOR (TINTE) ---
        AnimatedVisibility(
            visible = estaSeleccionado,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colorAcento.copy(alpha = 0.12f))
            )
        }

        // --- INDICADOR DE TILDE (EL BADGE) ---
        if (mostrarTilde && (modoMultiseleccionActivo || estaSeleccionado)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .zIndex(500f)
            ) {
                IndicadorSeleccion(
                    estaSeleccionado = estaSeleccionado,
                    colorAcento = colorAcento
                )
            }
        }
    }
}

/**
 * --- INDICADOR DE SELECCIÓN SOBERANO (V2026.ELITE) ---
 * Círculo con tilde animada para multiselección.
 */
@Composable
fun IndicadorSeleccion(
    estaSeleccionado: Boolean,
    colorAcento: Color = SharedPalette.ElectricCyan
) {
    val colorFondo by animateColorAsState(
        targetValue = if (estaSeleccionado) colorAcento else Color.Black.copy(alpha = 0.3f),
        label = "colorFondoTilde"
    )
    
    val colorBorde = if (estaSeleccionado) colorAcento else Color.White.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(colorFondo)
            .border(1.dp, colorBorde, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (estaSeleccionado) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Black 
            )
        }
    }
}

// ==========================================================================================
// --- SECCIÓN: PREVIEWS (MAVERICK ELITE 2026) ---
// ==========================================================================================

@Preview(name = "1. Tarjeta Seleccionada", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMoldeSeleccionado() {
    Box(modifier = Modifier.padding(16.dp)) {
        MoldeMultiSeleccion(
            estaSeleccionado = true,
            modoMultiseleccionActivo = true,
            colorAcento = SharedPalette.ElectricCyan
        ) {
            Box(
                modifier = Modifier
                    .size(width = 150.dp, height = 100.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text("CONTENIDO", color = Color.White)
            }
        }
    }
}

@Preview(name = "2. Tarjeta Normal (Modo Multiselección)", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMoldeModoActivo() {
    Box(modifier = Modifier.padding(16.dp)) {
        MoldeMultiSeleccion(
            estaSeleccionado = false,
            modoMultiseleccionActivo = true,
            colorAcento = SharedPalette.ElectricCyan
        ) {
            Box(
                modifier = Modifier
                    .size(width = 150.dp, height = 100.dp)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text("CONTENIDO", color = Color.White)
            }
        }
    }
}

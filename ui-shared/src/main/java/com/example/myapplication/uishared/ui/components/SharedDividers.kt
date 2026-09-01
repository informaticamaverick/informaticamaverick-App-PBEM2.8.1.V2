package com.example.myapplication.uishared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 1. DIVIDER HORIZONTAL INSET (EFECTO BAJO RELIEVE / BISELADO)
 * Usa dos líneas paralelas: una oscura arriba (sombra) y una clara abajo (reflejo de luz).
 * [ELITE SHARED]: Movido a ui-shared para uso cross-module.
 */
@Composable
fun DepthDividerHorizontal(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    shadowColor: Color = Color(0x33000000), // Sombra oscura
    highlightColor: Color = Color(0x77FFFFFF) // Brillo claro
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Línea superior oscura
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .background(shadowColor)
        )
        // Línea inferior clara
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(thickness)
                .background(highlightColor)
        )
    }
}


































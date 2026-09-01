package com.example.myapplication.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * --- 🔘 ÁTOMO: BOTÓN DE ACCIÓN CIRCULAR ELITE ---
 * [PROPÓSITO]: Botón unificado para cerrar sheets o disparar menús.
 * [DISEÑO]: Inspirado en ROG/Instagram Elite.
 */
@Composable
fun BotonAccionCircularElite(
    modifier: Modifier = Modifier,
    estaAbierto: Boolean = false,
    iconoCerrado: ImageVector = Icons.Default.Menu,
    iconoAbierto: ImageVector = Icons.Default.Close,
    alHacerClick: () -> Unit,
    tamanoBase: Dp = 32.dp,
    colorTinte: Color = Color.White
) {
    Surface(
        onClick = alHacerClick,
        modifier = modifier.size(tamanoBase),
        color = Color.White.copy(alpha = 0.08f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (estaAbierto) iconoAbierto else iconoCerrado,
                contentDescription = null,
                tint = if (estaAbierto) Color(0xFFF43F5E) else colorTinte, 
                modifier = Modifier.size((tamanoBase.value * 0.5f).dp)
            )
        }
    }
}

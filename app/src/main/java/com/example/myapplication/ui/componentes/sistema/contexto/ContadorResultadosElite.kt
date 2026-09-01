package com.example.myapplication.ui.componentes.sistema.contexto

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * ContadorResultadosElite: Componente atómico M3 Android 16 Style.
 * Muestra el número arriba y el helper abajo, con escalado dinámico.
 */
@Composable
fun ContadorResultadosElite(
    modifier: Modifier = Modifier,
    count: Int,
    collapseFraction: Float = 0f,
    accentColor: Color = SharedPalette.ElectricCyan
) {
    val numberFontSize by animateFloatAsState(
        targetValue = if (collapseFraction < 0.6f) 30f else 18f, 
        label = "CounterNumberSize"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Text(
            text = count.toString().padStart(2, '0'),
            style = AppTypography.HeaderTitle.copy(
                fontSize = numberFontSize.sp,
                color = accentColor,
                fontWeight = FontWeight.Black,
                lineHeight = (numberFontSize * 0.85f).sp 
            )
        )
        Text(
            text = "RESULT",
            style = AppTypography.HeaderSubtitle.copy(
                fontSize = 5.sp, 
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.offset(y = (-3).dp) 
        )
    }
}

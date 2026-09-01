package com.example.myapplication.ui.componentes.sistema.lista

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.componentes.sistema.DepthDividerThemedVertical
import com.example.myapplication.ui.componentes.sistema.IOSStylePill

/**
 * BurbujaCabeceraLista: Estilo "Elite Glass" con anatomía táctica y dividers de profundidad.
 */
@Composable
fun BurbujaCabeceraLista(
    modifier: Modifier = Modifier,
    text: String,
    emoji: String? = null,
    icon: ImageVector? = null,
    backgroundColor: Color = Color(0x661E293B),
    accentColor: Color = Color(0x33FFFFFF)
) {
    IOSStylePill(
        modifier = modifier,
        text = "",
        backgroundColor = backgroundColor,
        borderColor = accentColor,
        textColor = Color.White,        
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                if (emoji != null) {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Text(text = emoji, fontSize = 10.sp)
                    }
                } else if (icon != null) {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (emoji != null || icon != null) {
                    DepthDividerThemedVertical(
                        modifier = Modifier
                            .height(12.dp)
                            .align(Alignment.CenterVertically)
                    )
                }

                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(
                        text = text.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }
        }
    )
}

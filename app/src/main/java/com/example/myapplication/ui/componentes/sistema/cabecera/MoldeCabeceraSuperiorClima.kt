package com.example.myapplication.ui.componentes.sistema.cabecera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit

/**
 * --- ⛅ PIEZA CABECERA: CLIMA ---
 * [PROPÓSITO]: Bloque de información climática.
 */

/* --- ⛅ PIEZA CABECERA: CLIMA (REDISEÑADA Y PROFESIONAL) --- */

@Composable
fun MoldeCabeceraSuperiorClima(
    temperatura: String,  // Ej: "24°C"
    emoji: String,        // Ej: "⛅"
    descripcion: String,  // Ej: "Soleado despejado"
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.04f)) 
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
            .fillMaxHeight(), // 🔥 [RAÍZ]: Ocupa todo el alto
        contentAlignment = Alignment.CenterEnd 
    ) {
        // 1. EMOJI DE FONDO: Grande, opaco y ubicado en la capa posterior
        TextCompacto(
            text = emoji,
            fontSize = 42.sp, 
            modifier = Modifier
                .graphicsLayer {
                    alpha = 0.9f 
                    translationX = 45f
                    translationY = -8f // 🔥 [AJUSTE]: Mejor centrado
                }
                .align(Alignment.BottomEnd)
        )

        // 2. CONTENIDO FRONTAL: Temperatura con borde de contorno y helper text
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            // Contenedor superpuesto para lograr el contorno negro (Text Stroke)
            Box(contentAlignment = Alignment.CenterEnd) {
                // Capa trasera: Texto en negro dibujado en Stroke (Contorno)
                TextCompacto(
                    text = temperatura,
                    fontSize = 24.sp, // 🔥 [AJUSTE]
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        color = Color.Black,
                        drawStyle = Stroke(width = 6f, join = StrokeJoin.Round)
                    )
                )
                // Capa delantera: Texto principal relleno
                TextCompacto(
                    text = temperatura,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    style = TextStyle(drawStyle = Fill)
                )
            }

            // 3. HELPER TEXT TIPO ANDROID: Texto descriptivo pequeño en gris debajo
            TextCompactoAutoFit(
                text = descripcion.lowercase().replaceFirstChar { it.uppercase() },
                maxFontSize = 9.sp, // 🔥 [REDUCIDO]: Antes 10.sp
                minFontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.offset(y = (-4).dp) 
            )
        }
    }
}



@Preview(name = "Clima Profesional - Día", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMoldeClimaV3Pro() {
    Box(modifier = Modifier.padding(20.dp)) {
        MoldeCabeceraSuperiorClima(
            temperatura = "24°C",
            emoji = "⛅",
            descripcion = "Soleado despejado",
            onClick = {}
        )
    }
}

@Preview(name = "Clima Profesional - Noche", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMoldeClimaV3ProNoche() {
    Box(modifier = Modifier.padding(20.dp)) {
        MoldeCabeceraSuperiorClima(
            temperatura = "18°C",
            emoji = "🌙",
            descripcion = "Cielo despejado",
            onClick = {}
        )
    }
}
package com.example.myapplication.ui.componentes.sistema.cabecera

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit

/**
 * --- 🧪 ÁTOMOS DE CABECERA SUPERIOR ---
 * [PROPÓSITO]: Definir los estilos de texto y elementos indivisibles de la cabecera.
 * [LEY #11]: Uso de textos elásticos para garantizar paridad visual.
 */

@Composable
fun TextoEtiquetaCabeceraV3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SharedPalette.ElectricCyan.copy(alpha = 0.8f)
) {
    TextCompactoAutoFit(
        text = text,
        modifier = modifier,
        color = color,
        maxFontSize = 7.sp,
        minFontSize = 5.sp,
        fontWeight = FontWeight.Black,
        style = AppTypography.HeaderSubtitle.copy(
            letterSpacing = 1.2.sp
        )
    )
}

@Composable
fun TextoValorCabeceraV3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    maxFontSize: TextUnit = 13.sp,
    minFontSize: TextUnit = 9.sp,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 1
) {
    TextCompactoAutoFit(
        text = text,
        modifier = modifier,
        color = color,
        maxFontSize = maxFontSize,
        minFontSize = minFontSize,
        fontWeight = FontWeight.ExtraBold,
        textAlign = textAlign,
        style = AppTypography.HeaderTitle.copy(
            letterSpacing = 0.4.sp
        ),
        maxLines = maxLines
    )
}

/**
 * Átomo de Título Compuesto (Título + Subtítulo)
 * [LEY #10]: Colapso dinámico de jerarquía visual.
 */
@Composable
fun ColumnaTituloSeccionV3(
    titulo: String,
    subtitulo: String?,
    fraccionColapso: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.Start
    ) {
        if (!subtitulo.isNullOrBlank()) {
            TextCompacto(
                text = subtitulo.uppercase(),
                style = AppTypography.HeaderSubtitle.copy(
                    fontSize = 6.sp,
                    color = SharedPalette.ElectricCyan.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.3.sp
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = (1f - fraccionColapso * 3.5f).coerceIn(0f, 1f)
                    translationY = (8f * fraccionColapso) // Sube sutilmente
                }
            )
        }

        TextCompactoAutoFit(
            text = titulo.uppercase(),
            maxFontSize = 20.sp,
            minFontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Black,
            style = AppTypography.HeaderTitle.copy(
                letterSpacing = 0.6.sp,
                lineHeight = 20.sp
            ),
            modifier = Modifier.graphicsLayer {
                // 🔥 [ELITE]: Centrado Dinámico Compensatorio
                // Cuando el subtítulo desaparece (fraccion 1), movemos el título 
                // hacia arriba para que quede perfectamente centrado en la barra colapsada.
                translationY = (-6f * fraccionColapso) 
            }
        )
    }
}

@Composable
fun TextoClimaValorV3(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    TextCompactoAutoFit(
        text = text,
        modifier = modifier,
        color = color,
        maxFontSize = 22.sp,
        minFontSize = 16.sp,
        fontWeight = FontWeight.Black,
        style = AppTypography.HeaderTitle.copy(
            letterSpacing = (-0.5).sp
        )
    )
}

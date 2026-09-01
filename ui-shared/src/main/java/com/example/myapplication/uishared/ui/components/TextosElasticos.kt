package com.example.myapplication.uishared.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * --- 🔠 COMPONENTES DE TEXTO ELITE (v2026.16 REFACTORED) ---
 * [PROPÓSITO]: Unificación soberana de autosize, compactación y protección de escala.
 * Centralizado en ui-shared para consumo global en todo el ecosistema Maverick.
 * [LEY #11]: Garantiza legibilidad y estabilidad visual ante configuraciones del sistema.
 */

/**
 * Estilo base reutilizable para eliminar el font padding y la holgura vertical.
 * Usar como base para cualquier texto que requiera alineación quirúrgica.
 */
val EstiloCompactoBase = TextStyle(
    platformStyle = PlatformTextStyle(
        includeFontPadding = false // Desactiva el relleno invisible predeterminado
    ),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both // Recorta los bordes superior e inferior
    )
)

/**
 * 1. COMPONENTE BASE: Reemplazo directo de `Text` pero ultra-compacto.
 * Asegura que no haya espacios fantasma arriba o abajo del glifo.
 */
@Composable
fun TextCompacto(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = TextStyle.Default,
    textAlign: TextAlign = TextAlign.Start,
    textDecoration: TextDecoration? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        textDecoration = textDecoration,
        // Fusiona el estilo base compacto con las preferencias recibidas
        style = EstiloCompactoBase.merge(style)
    )
}

/**
 * 2. COMPONENTE AUTOSIZE: Se reduce automáticamente si el espacio es insuficiente.
 * Inicia en `maxFontSize` y va reduciéndose hasta `minFontSize` si hay desbordamiento.
 */
@Composable
fun TextCompactoAutoFit(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxFontSize: TextUnit = 13.sp,
    minFontSize: TextUnit = 8.sp,
    stepGranularity: TextUnit = 0.5.sp,
    fontWeight: FontWeight? = null,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    style: TextStyle = TextStyle.Default,
    textAlign: TextAlign = TextAlign.Start,
    textDecoration: TextDecoration? = null,
    softWrap: Boolean = true // 🔥 [ELITE]: Habilitado por defecto para permitir multi-línea inteligente
) {
    var currentFontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }
    var readyToDraw by remember(text, maxFontSize) { mutableStateOf(false) }

    val estiloCombinado = EstiloCompactoBase.merge(style).copy(
        fontSize = currentFontSize,
        color = color,
        fontWeight = fontWeight,
        textAlign = textAlign,
        textDecoration = textDecoration
    )

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            // Solo dibuja cuando ya determinó el tamaño correcto de fuente
            if (readyToDraw) drawContent()
        },
        style = estiloCombinado,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        onTextLayout = { textLayoutResult ->
            // Si el texto excede visualmente el contenedor o el número de líneas, reduce la fuente
            if ((textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > maxLines) && currentFontSize.value > minFontSize.value) {
                val proximoTamano = (currentFontSize.value - stepGranularity.value).coerceAtLeast(minFontSize.value)
                currentFontSize = proximoTamano.sp
            } else {
                readyToDraw = true
            }
        }
    )
}

/**
 * 3. CONTROLADOR DE ESCALA DE FUENTE DEL SISTEMA (Solución al "Descontrol" de UI)
 *
 * @param minFontScale Límite inferior (ej. 0.85f = 85%)
 * @param maxFontScale Límite superior (ej. 1.15f = 115%).
 */
@Composable
fun BloquearEscalaFuente(
    minFontScale: Float = 0.85f,
    maxFontScale: Float = 1.15f,
    content: @Composable () -> Unit
) {
    val densityActual = LocalDensity.current
    
    // Clamp para acotar el tamaño entre valores razonables que no destruyan la UI
    val escalaFuenteProtegida = densityActual.fontScale.coerceIn(minFontScale, maxFontScale)

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = densityActual.density,
            fontScale = escalaFuenteProtegida
        )
    ) {
        content()
    }
}

// --- 🔗 ALIASES DE COMPATIBILIDAD (Mantenidos para transición suave) ---

@Deprecated(
    message = "Usar TextCompactoAutoFit para mayor precisión quirúrgica",
    replaceWith = ReplaceWith("TextCompactoAutoFit(text, modifier, color, style.fontSize, style.fontSize * minScale, style = style, textAlign = textAlign, maxLines = maxLines)")
)
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    minScale: Float = 0.5f
) {
    val maxFs = if (style.fontSize.isSp) style.fontSize else 13.sp
    val minFs = maxFs * minScale
    TextCompactoAutoFit(
        text = text,
        modifier = modifier,
        color = color,
        maxFontSize = maxFs,
        minFontSize = minFs,
        maxLines = maxLines,
        style = style,
        textAlign = textAlign
    )
}

@Deprecated(
    message = "Usar BloquearEscalaFuente para mayor control de rangos dinámicos",
    replaceWith = ReplaceWith("BloquearEscalaFuente(content = content)")
)
@Composable
fun BloquearEscaladoFuente(content: @Composable () -> Unit) {
    BloquearEscalaFuente(minFontScale = 1f, maxFontScale = 1f, content = content)
}

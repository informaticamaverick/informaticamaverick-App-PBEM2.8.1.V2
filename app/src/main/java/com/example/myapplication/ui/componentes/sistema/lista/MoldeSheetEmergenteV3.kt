package com.example.myapplication.ui.componentes.sistema.lista

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.componentes.be.modelos.BeZIndex
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * MoldeSheetEmergenteV3.kt
 * Propósito: Definir el "Cascarón" universal para hojas emergentes (Bottom Sheets).
 * Funcionamiento: Maneja la animación de entrada/salida y el marco visual Elite.
 * Relación: Pieza de infraestructura para desacoplar el contenido de la lógica de presentación.
 * LEY #11 (Elasticidad): Altura adaptable con límites definidos.
 */

@Composable
fun MoldeSheetEmergenteV3(
    modifier: Modifier = Modifier,
    estaVisible: Boolean,
    alCerrar: () -> Unit,
    tituloCabecera: String? = null,
    subtituloCabecera: String? = null, 
    iconoCabecera: String? = null,
    showCloseButton: Boolean = true, // 🔥 [NEW]
    alturaMaximaFraccion: Float = 0.85f,
    colorBordeAcento: Color = SharedPalette.ElectricCyan,
    radioCorteSuperior: Dp = 10.dp,
    mostrarFondoOscuro: Boolean = true,
    paddingInferiorHUD: Dp = 5.dp, 
    paddingSuperiorHUD: Dp = 0.dp, // 🔥 [ELITE]: Espacio para la barra de búsqueda HUD
    cabeceraSoberana: @Composable (() -> Unit)? = null, // 🔥 [RESTORED]: Compatibilidad Raíz
    cabeceraPersonalizada: @Composable (RowScope.() -> Unit)? = null, // 🔥 [RESTORED]: Compatibilidad Raíz
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().zIndex(BeZIndex.SHEETS)) {
        // ... (Scrim logic remains same)
        // --- 1. FONDO OSCURO (SCRIM) ---
        if (mostrarFondoOscuro) {
            val alphaFondo by animateFloatAsState(
                targetValue = if (estaVisible) 0.85f else 0f,
                animationSpec = tween(400),
                label = "AlphaScrimV3"
            )

            if (alphaFondo > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = alphaFondo))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = alCerrar
                        )
                )
            }
        }

        // --- 2. EL PANEL EMERGENTE ---
        AnimatedVisibility(
            visible = estaVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val config = LocalConfiguration.current
            val alturaLimite = config.screenHeightDp.dp * alturaMaximaFraccion

            ContenedorBaseAppV3(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // 🔥 [SOLID]: Permite que la sheet se ajuste al contenido
                    .padding(top = paddingSuperiorHUD) 
                    .heightIn(max = alturaLimite) // 🔥 Solo limita el crecimiento máximo
                    .drawBehind {
                        dibujarBiselSheetEliteV3(size, colorBordeAcento, radioCorteSuperior)
                    }
                    .clickable(enabled = true, onClick = {}),
                colorFondo = SharedPalette.EliteSurface,
                radioCorte = radioCorteSuperior
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight() // 🔥 [SOLID]: La columna interna ahora permite el colapso
                ) {
                    // --- CABECERA ELITE UNIFICADA (RAÍZ DE DISEÑO) ---
                    if (cabeceraSoberana != null) {
                        cabeceraSoberana()
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp) // 🔥 [ELITE]: Altura más compacta
                                .background(SharedPalette.V2VantaBlack)
                                .drawBehind {
                                    val grosorTrazo = 0.8.dp.toPx()
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.12f),
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = grosorTrazo
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp) // 🔥 Padding reducido
                        ) {
                            if (cabeceraPersonalizada != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    cabeceraPersonalizada()
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        if (iconoCabecera != null) {
                                            androidx.compose.material3.Text(
                                                text = iconoCabecera, 
                                                fontSize = 20.sp, // 🔥 Un poco más pequeño
                                                modifier = Modifier.padding(end = 10.dp)
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (tituloCabecera != null) {
                                                com.example.myapplication.ui.componentes.sistema.AutoSizeText(
                                                    text = tituloCabecera.uppercase(),
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 17.sp, // 🔥 [ELITE]: Tamaño exacto ROG
                                                        letterSpacing = 1.sp
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                            if (subtituloCabecera != null) {
                                                androidx.compose.material3.Text(
                                                    text = subtituloCabecera.uppercase(),
                                                    color = colorBordeAcento,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.1.sp
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    
                                    // 🔥 [ELITE]: Botón de Cierre Unificado (Opcional)
                                    if (showCloseButton) {
                                        com.example.myapplication.ui.componentes.BotonAccionCircularElite(
                                            estaAbierto = true,
                                            alHacerClick = alCerrar,
                                            tamanoBase = 26.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- CONTENIDO ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight() // 🔥 [SOLID]: El contenido define la altura real
                    ) {
                        contenido()
                    }
                }
            }
        }
    }
}

/**
 * Lógica de dibujo táctica para la sheet.
 * Replica la forma y el gradiente horizontal de la cabecera Home.
 */
private fun DrawScope.dibujarBiselSheetEliteV3(
    size: Size, 
    colorAcento: Color, 
    radioCorte: Dp
) {
    val anchoTrazo = 1.2.dp.toPx()
    val radioPx = radioCorte.toPx()
    
    // Solo dibujamos la parte superior (Laterales -> Cortes -> Techo)
    val ruta = Path().apply {
        moveTo(0f, size.height) 
        lineTo(0f, radioPx)      
        lineTo(radioPx, 0f)      
        lineTo(size.width - radioPx, 0f) 
        lineTo(size.width, radioPx)      
        lineTo(size.width, size.height)  
    }

    val gradienteBorde = Brush.horizontalGradient(
        0.0f to colorAcento.copy(alpha = 0.02f),
        0.3f to colorAcento,
        0.5f to Color.White, // 🔥 Punto de brillo máximo (Impacto visual)
        0.7f to colorAcento,
        1.0f to colorAcento.copy(alpha = 0.02f)
    )

    // 1. Línea principal de alta definición con punto de brillo máximo (Paridad Home)
    drawPath(
        path = ruta,
        brush = gradienteBorde,
        style = Stroke(width = anchoTrazo, cap = StrokeCap.Round)
    )

    // 2. Resplandor (Glow) ambiental
    drawPath(
        path = ruta,
        brush = gradienteBorde,
        style = Stroke(width = anchoTrazo * 2.5f, cap = StrokeCap.Round),
        alpha = 0.2f // 🔥 Un poco más de presencia para el Cyan
    )
}

/**
 * MoldeSheetEliteV3.kt
 * [RAÍZ ELITE]: Versión estandarizada para hojas de alto nivel comercial.
 * Propósito: Eliminar el parchado de cabeceras individuales usando un único contrato visual.
 */
@Composable
fun MoldeSheetEliteV3(
    modifier: Modifier = Modifier,
    estaVisible: Boolean,
    alCerrar: () -> Unit,
    titulo: String,
    subtitulo: String? = null,
    icono: String? = null, 
    alturaMaximaFraccion: Float = 1f,
    colorAcento: Color = SharedPalette.ElectricCyan,
    paddingSuperiorHUD: Dp = 0.dp, // 🔥 [NEW]: Soporte para integración con HUD Search
    contenido: @Composable ColumnScope.() -> Unit
) {
    MoldeSheetEmergenteV3(
        modifier = modifier,
        estaVisible = estaVisible,
        alCerrar = alCerrar,
        tituloCabecera = titulo,
        subtituloCabecera = subtitulo,
        iconoCabecera = icono,
        alturaMaximaFraccion = alturaMaximaFraccion,
        colorBordeAcento = colorAcento,
        mostrarFondoOscuro = true,
        paddingSuperiorHUD = paddingSuperiorHUD,
        contenido = contenido
    )
}

@Preview(name = "Sheet V3 - Ejemplo Visual Elite", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMoldeSheetEmergenteV3() {
    PBEMTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            MoldeSheetEmergenteV3(
                estaVisible = true,
                alCerrar = {},
                tituloCabecera = "Panel de Control Elite",
                iconoCabecera = "🛡️"
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        "CONTENIDO ATÓMICO V3",
                        color = Color.White,
                        style = com.example.myapplication.uishared.estilos.AppTypography.HeaderTitle
                    )
                }
            }
        }
    }
}

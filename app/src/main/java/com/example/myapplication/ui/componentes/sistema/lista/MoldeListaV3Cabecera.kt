package com.example.myapplication.ui.componentes.sistema.lista

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.ui.componentes.sistema.DepthDividerHorizontal
import com.example.myapplication.ui.componentes.sistema.DepthDividerThemedVertical
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * MoldeListaV3Cabecera.kt
 * Propósito: Cabecera dinámica de alto rendimiento con efectos ROG/Elite.
 * Funcionamiento: Utiliza lectura diferida de estado para animaciones ultra-fluidas.
 * Relación: Pieza central de la anatomía de pantalla PBEM (Ley #10).
 */

@Composable
fun CabeceraAppV3(
    modifier: Modifier = Modifier,
    titulo: String,
    subtitulo: String? = null,
    icono: String? = null, // 🔥 [ELITE]: Icono de Impacto (Derecha por defecto)
    cantidadItems: Int? = null,
    perfiles: List<PerfilIdentidadV3> = emptyList(),
    idPerfilSeleccionado: String? = null,
    alSeleccionarPerfil: (PerfilIdentidadV3) -> Unit = {},
    proveedorColapso: () -> Float = { 0f },
    alturaCabecera: Dp,
    colorAcento: Color = SharedPalette.ElectricCyan,
    fondoPincel: Brush = Brush.verticalGradient(
        listOf(SharedPalette.V2DeepVoid, SharedPalette.ROG_Dark_Bg)
    ),
    slotCentral: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null, // 🔥 [NEW]: Espacio central flexible
    menuPerfil: @Composable (ColumnScope.(idPerfil: String) -> Unit)? = null, // 🔥 [NEW]: Soporte para menú de perfiles
    accionesDerecha: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null,
    accionesIzquierda: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null,
    filtros: @Composable (RowScope.(proveedorColapso: () -> Float) -> Unit)? = null
) {
    // 🔥 [PERFORMANCE] Las animaciones se manejan en la fase de Draw o Layout mediante lambdas
    val fraccionColapso = proveedorColapso()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alturaCabecera)
            .background(fondoPincel)
            .drawBehind {
                dibujarBiselNeonV3(size, colorAcento)
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 2.dp), // 🔥 [ELITE]: Centrado vertical con margen mínimo
                verticalAlignment = Alignment.CenterVertically 
            ) {
                // 1. ICONO DE IMPACTO (IZQUIERDA - LEY #10)
                if (icono != null) {
                    val escalaIcono = (1f + fraccionColapso * 0.4f)
                    Text(
                        text = icono,
                        fontSize = (34 * escalaIcono).sp,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .graphicsLayer {
                                translationY = (12f * fraccionColapso) 
                            }
                    )
                }

                // 2. ACCIONES IZQUIERDA Y PERFILES
                Row(
                    modifier = Modifier.padding(bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (accionesIzquierda != null) {
                        accionesIzquierda(proveedorColapso)
                    }

                    if (perfiles.isNotEmpty()) {
                        perfiles.forEach { perfil ->
                            Box {
                                var mostrarMenuLocal by remember { mutableStateOf(false) }
                                
                                MoldeBurbujaPerfilV3(
                                    perfil = perfil,
                                    tamanoBase = 40.dp,
                                    estaSeleccionado = perfil.id == idPerfilSeleccionado,
                                    modifier = Modifier.clickable { 
                                        if (menuPerfil != null && perfil.id == idPerfilSeleccionado) {
                                            mostrarMenuLocal = true 
                                        } else {
                                            alSeleccionarPerfil(perfil)
                                        }
                                    }
                                )

                                if (mostrarMenuLocal && menuPerfil != null) {
                                    com.example.myapplication.ui.componentes.sistema.menu.v3.MoldeMenuArmadorV3(
                                        expanded = mostrarMenuLocal,
                                        onDismissRequest = { mostrarMenuLocal = false },
                                        alignment = Alignment.TopStart, // 🔥 [ELITE]: Alineación TopStart para caída natural
                                        verticalOffset = 42.dp, // 🔥 [ELITE]: Debajo de la burbuja (40dp + 2dp margen)
                                        arrowOffset = 20.dp
                                    ) {
                                        menuPerfil(idPerfilSeleccionado ?: "")
                                    }
                                }
                            }
                        }
                    }
                }

                if (perfiles.isNotEmpty() || accionesIzquierda != null || (icono != null && fraccionColapso < 0.5f)) {
                    DepthDividerThemedVertical(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(horizontal = 8.dp) // 🔥 [ELITE]: Ajuste de espaciado
                    )
                }

                // 3. TEXTOS (TÍTULO Y SUBTÍTULO) - AUTOAJUSTABLE
                Column(
                    modifier = Modifier
                        .then(if (slotCentral == null) Modifier.weight(1f) else Modifier.wrapContentWidth()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center 
                ) {
                    if (!subtitulo.isNullOrBlank()) {
                        TextCompacto(
                            text = subtitulo.uppercase(),
                            color = colorAcento.copy(alpha = 0.8f),
                            fontSize = 7.sp, 
                            fontWeight = FontWeight.Black,
                            style = TextStyle(letterSpacing = 1.4.sp),
                            maxLines = 1,
                            modifier = Modifier.graphicsLayer {
                                alpha = (1f - fraccionColapso * 3f).coerceIn(0f, 1f)
                                translationY = (10f * fraccionColapso)
                            }
                        )
                    }

                    TextCompactoAutoFit(
                        text = titulo.uppercase(),
                        maxFontSize = 20.sp,
                        minFontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            letterSpacing = 0.5.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 1,
                        modifier = Modifier.graphicsLayer {
                            translationY = (4f * fraccionColapso)
                        }
                    )
                }

                // 🔥 [ELITE]: Slot Central (Espacio para Calendarios o Selectores)
                if (slotCentral != null) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        this@Row.slotCentral(proveedorColapso)
                    }
                }

                // 4. CONTADOR Y ACCIONES DERECHA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    if (cantidadItems != null) {
                        ContadorResultadosV3(
                            cantidad = cantidadItems,
                            proveedorColapso = proveedorColapso,
                            colorAcento = colorAcento
                        )
                    }

                    if (accionesDerecha != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        accionesDerecha(proveedorColapso)
                    }
                }
            }

            // 4. SLOT DE FILTROS (Solo visible si no está colapsado)
            if (filtros != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
                        .graphicsLayer {
                            alpha = (1f - fraccionColapso * 3f).coerceIn(0f, 1f)
                            translationY = (fraccionColapso * -20f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        filtros(proveedorColapso)
                    }
                }
            }
        }


        // Divisor inferior de profundidad
        DepthDividerHorizontal(
            modifier = Modifier.align(Alignment.BottomCenter),
            thickness = 0.8.dp
        )
    }
}

/**
 * Función de dibujo para el bisel neón característico.
 * Extraído a función de extensión para limpieza visual.
 */
private fun DrawScope.dibujarBiselNeonV3(size: Size, colorAcento: Color) {
    val anchoTrazo = 1.6.dp.toPx()
    val radioCorte = 16.dp.toPx()
    
    val ruta = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, radioCorte)
        lineTo(radioCorte, 0f)
        lineTo(size.width - radioCorte, 0f)
        lineTo(size.width, radioCorte)
        lineTo(size.width, size.height)
    }

    val gradienteBorde = Brush.horizontalGradient(
        listOf(
            colorAcento.copy(alpha = 0.1f),
            colorAcento,
            SharedPalette.ElectricCyan,
            colorAcento,
            colorAcento.copy(alpha = 0.1f)
        )
    )

    drawPath(
        path = ruta,
        brush = gradienteBorde,
        style = Stroke(width = anchoTrazo, cap = StrokeCap.Round)
    )

    // Resplandor (Glow)
    drawPath(
        path = ruta,
        brush = gradienteBorde,
        style = Stroke(width = anchoTrazo * 2.5f, cap = StrokeCap.Round),
        alpha = 0.15f
    )
}

// ==================================================================================
// --- PREVIEWS (LEY #10: MODO EDICIÓN) ---
// ==================================================================================

@Preview(name = "Cabecera V3 - Completa", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewCabeceraV3() {
    PBEMTheme {
        Box(modifier = Modifier.padding(10.dp)) {
            CabeceraAppV3(
                titulo = "GESTIÓN DE CONCURSOS",
                subtitulo = "PBEM Corporation",
                cantidadItems = 24,
                alturaCabecera = 80.dp,
                perfiles = listOf(
                    PerfilIdentidadV3("1", "App", "AP", colorAcento = SharedPalette.ElectricCyan)
                ),
                idPerfilSeleccionado = "1",
                filtros = {
                    Text("Filtros Activos: Recientes", color = Color.Gray, fontSize = 10.sp)
                }
            )
        }
    }
}

package com.example.myapplication.ui.componentes.sistema.lista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * MoldeListaV3Estructura.kt
 * Propósito: Definir la base estructural y el contenedor maestro del sistema V3.
 * Funcionamiento: Provee el marco visual (forma, fondo y sombras) para todas las listas.
 * Relación: Es el "Lienzo" (Ley #10) donde se montan las piezas y cabeceras.
 */

/**
 * ContenedorBaseAppV3: El marco visual definitivo del ecosistema.
 * Aplica el corte de esquinas superior y el color de fondo Elite.
 */
@Composable
fun ContenedorBaseAppV3(
    modifier: Modifier = Modifier,
    colorFondo: Color = SharedPalette.EliteSurface,
    radioCorte: Dp = 16.dp,
    contenido: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topStart = radioCorte, topEnd = radioCorte))
            .background(colorFondo),
        content = contenido
    )
}

/**
 * SombraProyectadaV3: Efecto de elevación táctica para la cabecera fija.
 * Se sitúa justo debajo de la cabecera para dar profundidad al scroll.
 */
@Composable
fun SombraProyectadaV3(
    modifier: Modifier = Modifier,
    altura: Dp = 16.dp,
    desplazamientoY: Dp,
    opacidad: Float = 0.8f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(altura)
            .offset(y = desplazamientoY)
            .zIndex(1f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = opacidad),
                        Color.Transparent
                    )
                )
            )
    )
}

// ==================================================================================
// --- PREVIEWS (LEY #10: MODO LECTURA) ---
// ==================================================================================

@Preview(name = "Estructura V3 - Contenedor", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewEstructuraV3() {
    PBEMTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(20.dp)
        ) {
            ContenedorBaseAppV3(
                modifier = Modifier.height(300.dp)
            ) {
                // Simulación de sombra
                SombraProyectadaV3(desplazamientoY = 56.dp)
                
                // Simulación de contenido
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 70.dp, start = 20.dp, end = 20.dp)
                        .background(Color.White.copy(alpha = 0.05f))
                )
            }
        }
    }
}

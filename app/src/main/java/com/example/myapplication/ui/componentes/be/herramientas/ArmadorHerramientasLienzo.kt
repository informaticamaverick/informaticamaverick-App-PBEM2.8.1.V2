package com.example.myapplication.ui.componentes.be.herramientas

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.ui.componentes.be.modelos.*
import com.example.myapplication.ui.componentes.be.ui.BeToast
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.estilos.PBEMTheme

/**
 * --- LIENZO DE HERRAMIENTAS BE (v2026.ELITE) ---
 * [PROPÓSITO]: Orquestar la transición entre el modo LOG (Toast) y el modo ACCIONES.
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun ArmadorHerramientasLienzo(
    estadoUi: EstadoUiBeAsistente,
    modifier: Modifier = Modifier
) {
    val toastActivo = estadoUi.toastActivo

    // 🔥 [ELITE]: Cambio de Row a Box para evitar empuje lateral.
    // Los sectores se superponen tácticamente con prioridad al Log.
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // --- 1. SECTOR ACCIONES (SOBERANÍA TÁCTICA) ---
        // Se desvanece suavemente cuando el Log toma el control.
        AnimatedVisibility(
            visible = toastActivo == null,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.zIndex(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Isla Primaria
                estadoUi.herramientasPrimarias.forEach { acc ->
                    GrupoHerramientaConEtiqueta(acc, SharedPalette.ElectricCyan, CircleShape, esUnico = true)
                }

                // Isla Navegación
                if (estadoUi.herramientasNavegacion.isNotEmpty()) {
                    GrupoHerramientaConEtiquetaMultiple(
                        acciones = estadoUi.herramientasNavegacion,
                        colorActivo = SharedPalette.ElectricCyan
                    )
                }

                // Isla Edición
                if (estadoUi.herramientasEdicion.isNotEmpty()) {
                    GrupoHerramientaConEtiquetaMultiple(
                        acciones = estadoUi.herramientasEdicion,
                        colorActivo = SharedPalette.ElectricCyan
                    )
                }

                // Isla Sistema (Cerca de Be)
                if (estadoUi.herramientasSistema.isNotEmpty()) {
                    GrupoHerramientaConEtiquetaMultiple(
                        acciones = estadoUi.herramientasSistema,
                        colorActivo = Color.White
                    )
                }
            }
        }

        // --- 0. SECTOR LOG (FEEDBACK PROACTIVO) - PRIORIDAD MÁXIMA ---
        // Aparece por encima de las herramientas sin desplazarlas.
        AnimatedVisibility(
            visible = toastActivo != null,
            enter = scaleIn(transformOrigin = TransformOrigin(1f, 0.5f), animationSpec = tween(400)) + fadeIn(),
            exit = scaleOut(transformOrigin = TransformOrigin(1f, 0.5f), animationSpec = tween(300)) + fadeOut(),
            modifier = Modifier.zIndex(2f) // 🔥 Prioridad visual superior
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                IslaHerramientasSupreme(
                    modifier = Modifier.widthIn(max = 260.dp) // 🔥 [ELITE]: Un poco más de ancho para logs largos
                ) {
                    toastActivo?.let {
                        BeToast(mensaje = it.mensaje, tipo = it.tipo, soloContenido = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun GrupoHerramientaConEtiqueta(
    accion: ModeloAccionPequenaBe,
    colorActivo: Color,
    forma: Shape,
    esUnico: Boolean = false
) {
    IslaHerramientasSupreme(
        modifier = Modifier.wrapContentWidth()
    ) {
        BotonHerramientaSupreme(
            accion = accion, 
            colorActivo = colorActivo,
            forma = forma,
            esUnico = esUnico
        )
    }
}

@Composable
private fun GrupoHerramientaConEtiquetaMultiple(
    acciones: List<ModeloAccionPequenaBe>,
    colorActivo: Color
) {
    IslaHerramientasSupreme(
        modifier = Modifier.wrapContentWidth()
    ) {
        acciones.forEachIndexed { i, acc ->
            BotonHerramientaSupreme(
                accion = acc,
                colorActivo = colorActivo,
                forma = obtenerFormaSegmentadaSupreme(i, acciones.size)
            )
        }
    }
}

// ==========================================================================================
// --- SECCIÓN: PREVIEWS (MAVERICK ELITE 2026) ---
// ==========================================================================================

@Preview(name = "1. Modo Normal (Inicio)", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewHerramientasNormal() {
    PBEMTheme {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            ArmadorHerramientasLienzo(
                estadoUi = EstadoUiBeAsistente(
                    herramientasPrimarias = listOf(
                        ModeloAccionPequenaBe("fast", androidx.compose.material.icons.Icons.Default.FlashOn, "Fast", emoji = "⚡"),
                        ModeloAccionPequenaBe("fav", androidx.compose.material.icons.Icons.Default.Favorite, "Favoritos", emoji = "❤️")
                    ),
                    herramientasSistema = listOf(
                        ModeloAccionPequenaBe("teclado", androidx.compose.material.icons.Icons.Default.Keyboard, "Teclado"),
                        ModeloAccionPequenaBe("cerrar_todo", androidx.compose.material.icons.Icons.Default.Close, "Cerrar")
                    )
                )
            )
        }
    }
}

@Preview(name = "2. Modo Multiselección (Supreme)", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewHerramientasSeleccion() {
    PBEMTheme {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            ArmadorHerramientasLienzo(
                estadoUi = EstadoUiBeAsistente(
                    estaMultiseleccion = true,
                    herramientasEdicion = listOf(
                        ModeloAccionPequenaBe("cancel", androidx.compose.material.icons.Icons.Default.Close, "Cancelar"),
                        ModeloAccionPequenaBe("select_all", androidx.compose.material.icons.Icons.Default.DoneAll, "Todo", estaSeleccionado = true),
                        ModeloAccionPequenaBe("delete_multi", androidx.compose.material.icons.Icons.Default.Delete, "Borrar")
                    ),
                    herramientasSistema = listOf(
                        ModeloAccionPequenaBe("teclado", androidx.compose.material.icons.Icons.Default.Keyboard, "Teclado"),
                        ModeloAccionPequenaBe("cerrar_todo", androidx.compose.material.icons.Icons.Default.Close, "Cerrar")
                    )
                )
            )
        }
    }
}

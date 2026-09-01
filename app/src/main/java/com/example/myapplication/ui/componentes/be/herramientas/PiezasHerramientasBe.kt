package com.example.myapplication.ui.componentes.be.herramientas

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.componentes.be.modelos.ModeloAccionPequenaBe
import com.example.myapplication.ui.componentes.sistema.shakeClick
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit

/**
 * --- PIEZAS DE HERRAMIENTAS BE (v2026.ELITE - M3 SEGMENTED) ---
 * [LEY #10]: Átomos del HUD.
 * [LEY #9]: Estándar Mav en Español.
 * [LEY #11]: Textos Elásticos integrados.
 */

/**
 * --- PIEZAS DE HERRAMIENTAS BE (SUPREME v2026) ---
 */

@Composable
fun obtenerFormaSegmentadaSupreme(indice: Int, total: Int, tipo: String = "default"): Shape {
    val radioGrande = 28.dp // 🔥 [ELITE]: Radio M3 para extremos
    val radioPequeno = 4.dp  // 🔥 [ELITE]: Radio mínimo para uniones internas
    
    return when {
        total == 1 -> CircleShape
        tipo == "primaria" -> CircleShape
        indice == 0 -> RoundedCornerShape(topStart = radioGrande, bottomStart = radioGrande, topEnd = radioPequeno, bottomEnd = radioPequeno)
        indice == total - 1 -> RoundedCornerShape(topStart = radioPequeno, bottomStart = radioPequeno, topEnd = radioGrande, bottomEnd = radioGrande)
        else -> RoundedCornerShape(radioPequeno)
    }
}

/**
 * Contenedor de Isla Bento (Supreme Style - Invisible Container)
 * [v2026.ELITE]: Ahora actúa como un simple agrupador táctico sin fondo 
 * para permitir que los botones segmentados luzcan el efecto MD3 Split.
 */
@Composable
fun IslaHerramientasSupreme(
    modifier: Modifier = Modifier,
    contenido: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .height(78.dp) // Altura para Icono + Texto
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp), // 🔥 [MD3 SPLIT EFFECT]
            content = contenido
        )
    }
}

@Composable
fun BotonHerramientaSupreme(
    accion: ModeloAccionPequenaBe,
    colorActivo: Color,
    forma: Shape,
    esUnico: Boolean = false,
    modifier: Modifier = Modifier
) {
    val estaSeleccionado = accion.estaSeleccionado
    val estaHabilitado = accion.estaHabilitado
    
    // --- ESTILO MD3 TONAL / FILLED ---
    val pincelFondo = when {
        !estaHabilitado -> Brush.verticalGradient(listOf(Color(0xFF15161A), Color(0xFF0A0B0E)))
        estaSeleccionado -> Brush.verticalGradient(listOf(colorActivo.copy(alpha = 0.45f), colorActivo.copy(alpha = 0.1f)))
        else -> Brush.verticalGradient(listOf(Color(0xFF1C1E26), Color(0xFF0A0B10)))
    }

    val colorBorde = when {
        estaSeleccionado -> colorActivo
        // 🔥 [ELITE FIX]: Si la acción es de cierre o cancelación, usamos su tinte (Rojo)
        // para el borde, dándole una identidad visual de advertencia/salida.
        accion.id == "cerrar_todo" || accion.id == "cerrar_wizard" || accion.id == "cancel" -> accion.tinte.copy(alpha = 0.7f)
        esUnico -> colorActivo.copy(alpha = 0.4f)
        else -> Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .then(if (esUnico) Modifier.size(width = 62.dp, height = 72.dp) else Modifier.size(width = 54.dp, height = 72.dp))
            .clip(forma)
            .background(pincelFondo)
            .border(width = if(estaSeleccionado) 1.5.dp else 1.dp, color = colorBorde, shape = forma)
            .shakeClick(enabled = estaHabilitado) { 
                android.util.Log.d("HUD", "[ACCION_CLICK] ID: ${accion.id}")
                accion.alHacerClick() 
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            // --- 1. ICONO O EMOJI ---
            Box(modifier = Modifier.graphicsLayer { alpha = if (estaHabilitado) 1f else 0.5f }) {
                if (accion.emoji != null) {
                    Text(text = accion.emoji, fontSize = 22.sp)
                } else {
                    Icon(
                        imageVector = accion.icono,
                        contentDescription = accion.etiqueta,
                        tint = if (estaSeleccionado) Color.White else accion.tinte,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- 2. ETIQUETA INTEGRADA (MD3 STYLE) ---
            if (accion.etiqueta.isNotEmpty()) {
                val colorEtiqueta = when {
                    estaSeleccionado -> Color.White
                    accion.id == "cancel" || accion.id == "delete_multi" -> accion.tinte.copy(alpha = 0.9f)
                    else -> Color.White.copy(alpha = 0.7f)
                }
                TextCompactoAutoFit(
                    text = accion.etiqueta.uppercase(),
                    color = colorEtiqueta,
                    maxFontSize = 7.sp,
                    minFontSize = 5.sp,
                    fontWeight = if(estaSeleccionado) FontWeight.Black else FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
fun DivisorTacticoBe(
    modifier: Modifier = Modifier,
    opacidad: Float = 0.15f
) {
    com.example.myapplication.ui.componentes.sistema.PremiumVerticalDivider(
        modifier = modifier.padding(horizontal = 4.dp),
        height = 32.dp,
        thickness = 1.dp,
        accentColor = Color.White.copy(alpha = opacidad)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewPiezasHerramientasBe() {
    PBEMTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Ejemplo de Isla Supreme (Cápsula Unificada)
            IslaHerramientasSupreme {
                BotonHerramientaSupreme(
                    accion = ModeloAccionPequenaBe("test", Icons.Default.Star, "Todo"),
                    colorActivo = SharedPalette.ElectricCyan,
                    forma = CircleShape
                )
            }
        }
    }
}

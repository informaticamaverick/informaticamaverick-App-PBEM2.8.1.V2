package com.example.myapplication.ui.componentes.be.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale as drawScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.myapplication.ui.componentes.be.modelos.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- CUERPO DEL ASISTENTE BE (v2026.ELITE) ---
 * [PROPÓSITO: Renderizar el asistente, sus ojos, burbujas y herramientas.
 * [LEY #9]: Estándar Mav en Español.
 */

private val CianLocal_Elite = Color(0xFF22D3EE)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FabAsistenteBe(
    modifier: Modifier = Modifier,
    estadoUi: EstadoUiBeAsistente,
    estadoFisico: EstadoFisicoBeAsistente,
    acciones: AccionesAsistenteBe
) {
    val rellenoInferiorAnimado by animateDpAsState(
        targetValue = estadoFisico.rellenoInferior, 
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy),
        label = "relleno_inf"
    )
    
    val desplazamientoHibernacionX by animateDpAsState(targetValue = if (estadoUi.estaDormido) 30.dp else 0.dp, label = "hibernacion")
    
    val escalaOjoY by animateFloatAsState(targetValue = if (estadoUi.estaDormido || estadoFisico.estaParpadeando) 0.1f else 1f, label = "parpadeo")

    // 🔥 [ELITE]: Elevación Soberana para estar sobre la Sheet
    Box(modifier = modifier.fillMaxSize().zIndex(BeZIndex.ASISTENTE_FAB)) {
        
        // --- NÚCLEO DEL ASISTENTE (FAB + BARRA) ---
        // 🔥 [ELITE]: Siempre visible si hay herramientas, aunque la hoja esté abierta.
        // También visible si el contrato soberano dice 'mostrarBe', para ver solo los ojos.
        // Se mantiene visible durante la búsqueda activa para mostrar las herramientas de sistema (Teclado/Cerrar).
        val mostrarNucleo = (estadoUi.herramientasPrimarias.isNotEmpty() || estadoUi.herramientasSistema.isNotEmpty() || 
             estadoUi.herramientasNavegacion.isNotEmpty() || estadoUi.herramientasEdicion.isNotEmpty()) || 
             estadoUi.estaBusquedaActiva || estadoUi.mostrarBe


        AnimatedVisibility(
            visible = mostrarNucleo,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(), 
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(), 
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
                Row(
                    modifier = Modifier
                        .padding(bottom = rellenoInferiorAnimado.coerceAtLeast(0.dp))
                        .offset { IntOffset(desplazamientoHibernacionX.toPx().roundToInt(), 0) }
                        .zIndex(10f), 
                    verticalAlignment = Alignment.Bottom
                ) {
                // --- 1. BARRA DE HERRAMIENTAS / LOG (ARMADOR SOBERANO) ---
                if (!estadoUi.estaDormido) {
                    com.example.myapplication.ui.componentes.be.herramientas.ArmadorHerramientasCaja(
                        estaVisible = true,
                        estadoUi = estadoUi
                    )
                }

                // --- 3. OJOS DEL ASISTENTE ---
                // 🔥 [ELITE]: Higiene Visual Radical. Ocultamos el cuerpo de Be en Multiselección.
                if (!estadoUi.estaBusquedaActiva && !estadoUi.configuracion.ocultarOjos && !estadoUi.estaMultiseleccion) {
                    Box(modifier = Modifier.height(90.dp).width(80.dp).pointerInput(estadoUi.estaDormido, estadoUi.estaBusquedaActiva) {
                        detectTapGestures(
                            onTap = { acciones.alHacerClick() },
                            onLongPress = { acciones.alHacerClickLargo() },
                            onDoubleTap = { acciones.alHacerDobleClick() }
                        )
                    }) {
                        val emocionFinal = when (estadoUi.toastActivo?.tipo) {
                            TipoBeToast.EXITO -> EmocionBe.FELIZ
                            TipoBeToast.ERROR -> EmocionBe.SORPRENDIDO
                            TipoBeToast.HABLANDO -> EmocionBe.NORMAL
                            else -> EmocionBe.NORMAL
                        }

                        BeAssistantEyes(
                            modifier = Modifier.align(Alignment.Center), 
                            size = 80.dp, 
                            emocion = emocionFinal, 
                            eyeScaleY = escalaOjoY, 
                            pupilaX = estadoFisico.pupilaX, 
                            pupilaY = estadoFisico.pupilaY, 
                            estaDormido = estadoUi.estaDormido
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BeAssistantEyes(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    emocion: EmocionBe = EmocionBe.NORMAL,
    eyeScaleY: Float = 1f,
    pupilaX: Float = 0f,
    pupilaY: Float = 0f,
    estaDormido: Boolean = false,
    grosorAnilloDp: Float = 3f,          
    intensidadSombra: Float = 0.90f,     
    opacidadReflejo: Float = 1.0f        
) {
    val cianCyber = Color(0xFF22D3EE)
    val negroMate = Color(0xFF020408)
    val azulNoche = Color(0xFF0B1324)

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val factorEscala = this.size.width / 100f
            drawScale(factorEscala, factorEscala, pivot = Offset.Zero) {
                
                // Sombra y Silueta
                val colorSombra = negroMate.copy(alpha = intensidadSombra)
                drawCircle(color = colorSombra, radius = 38f, center = Offset(50f, 50f))
                drawLine(color = colorSombra, start = Offset(68f, 68f), end = Offset(92f, 92f), strokeWidth = 14f, cap = StrokeCap.Round)

                // Mango
                drawLine(color = azulNoche, start = Offset(68f, 68f), end = Offset(92f, 92f), strokeWidth = 13f, cap = StrokeCap.Round)
                drawLine(color = cianCyber, start = Offset(71f, 71f), end = Offset(89f, 89f), strokeWidth = 9f, cap = StrokeCap.Round)

                // Visor Base
                drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFF182232), Color(0xFF0A0E16), negroMate), center = Offset(35f, 30f), radius = 70f), radius = 38f, center = Offset(50f, 50f))

                // Anillo
                drawCircle(color = cianCyber, radius = 34f, center = Offset(50f, 50f), style = Stroke(width = grosorAnilloDp))

                // Reflejo
                val rutaReflejo = Path().apply { moveTo(22f, 42f); quadraticTo(28f, 20f, 52f, 22f); cubicTo(38f, 25f, 28f, 32f, 22f, 42f); close() }
                drawPath(path = rutaReflejo, brush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.65f * opacidadReflejo), Color.Transparent), start = Offset(20f, 20f), end = Offset(50f, 45f)))

                // Ojos
                if (estaDormido) {
                    val rutaZzzL = Path().apply { moveTo(29f, 50f); quadraticTo(37f, 56f, 45f, 50f) }
                    val rutaZzzR = Path().apply { moveTo(55f, 50f); quadraticTo(63f, 56f, 71f, 50f) }
                    drawPath(rutaZzzL, Color.White, style = Stroke(width = 3f, cap = StrokeCap.Round))
                    drawPath(rutaZzzR, Color.White, style = Stroke(width = 3f, cap = StrokeCap.Round))
                } else if (emocion == EmocionBe.FELIZ) {
                    val rutaFelizL = Path().apply { moveTo(29f, 53f); quadraticTo(37f, 38f, 45f, 53f) }
                    val rutaFelizR = Path().apply { moveTo(55f, 53f); quadraticTo(63f, 38f, 71f, 53f) }
                    drawPath(rutaFelizL, Color.White, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                    drawPath(rutaFelizR, Color.White, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                } else {
                    val radioPupila = if (emocion == EmocionBe.SORPRENDIDO) 2.5f else 4f
                    val altoOjo = 24f * eyeScaleY
                    drawOval(Color.White, Offset(29f, 50f - (altoOjo / 2f)), Size(16f, altoOjo))
                    drawCircle(Color(0xFF05070A), radioPupila * eyeScaleY, Offset(37f + pupilaX, 50f + pupilaY))
                    drawCircle(Color.White, 1.2f, Offset(35f + pupilaX, 47.5f + pupilaY))
                    drawOval(Color.White, Offset(55f, 50f - (altoOjo / 2f)), Size(16f, altoOjo))
                    drawCircle(Color(0xFF05070A), radioPupila * eyeScaleY, Offset(63f + pupilaX, 50f + pupilaY))
                    drawCircle(Color.White, 1.2f, Offset(61f + pupilaX, 47.5f + pupilaY))
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1324)
@Composable
fun PreviewOjosAsistenteBe() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BeAssistantEyes(emocion = EmocionBe.NORMAL)
            BeAssistantEyes(emocion = EmocionBe.FELIZ)
            BeAssistantEyes(emocion = EmocionBe.SORPRENDIDO)
        }
    }
}

data class AccionesAsistenteBe(
    val alHacerClick: () -> Unit,
    val alHacerDobleClick: () -> Unit,
    val alHacerClickLargo: () -> Unit,
    val alCambiarConsultaBusqueda: (String) -> Unit,
    val alEnviarBusqueda: () -> Unit,
    val alHacerClickAccionBurbuja: () -> Unit,
    val alHacerClickAccionReaccion: (String) -> Unit,
    val alCerrarReaccion: () -> Unit
)

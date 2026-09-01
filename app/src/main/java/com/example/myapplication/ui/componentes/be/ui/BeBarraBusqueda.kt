package com.example.myapplication.ui.componentes.be.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.CyberTypography
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.EstiloCompactoBase
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import kotlinx.coroutines.flow.StateFlow

/**
 * --- BE BARRA BUSQUEDA (MORPHING BUBBLE v2026.ELITE.PRO) ---
 * [PROPÓSITO]: Interfaz de búsqueda con transformación dinámica de MD3.
 * [CONTRASTE]: Voz de Be clara (85%), Voz de Usuario opaca (60%).
 * [LEY #9]: Estándar Mav en Español.
 */
@Composable
fun BarraBusquedaTacticaV3(
    modificador: Modifier = Modifier,
    alCambiarConsulta: (String) -> Unit,
    flujoConsulta: StateFlow<String>,
    alBuscar: () -> Unit,
    requeridorFoco: FocusRequester,
    alLimpiarTexto: () -> Unit,
    textoPista: String = "ESCANEANDO RED..."
) {
    val consulta by flujoConsulta.collectAsState()
    var estaEnFoco by remember { mutableStateOf(false) }

    // --- 1. CONFIGURACIÓN DE ANIMACIÓN (MD3 MOTION) ---
    val duracionAnim = 400
    
    val sesgoCola by animateFloatAsState(
        targetValue = if (estaEnFoco) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "sesgo_cola"
    )

    // Transición de Colores (Estilo Menú Táctico)
    val colorBeInicio = Color(0xFF0A1016) // Negro Táctico (Fondo de Menú)
    val colorBeFin = Color(0xFF020408)    // Negro Mate Profundo
    val colorMateUsuario = Color(0xFF1C1C1E)

    val fondoActualA by animateColorAsState(targetValue = if (estaEnFoco) colorMateUsuario else colorBeInicio, animationSpec = tween(duracionAnim))
    val fondoActualB by animateColorAsState(targetValue = if (estaEnFoco) colorMateUsuario else colorBeFin, animationSpec = tween(duracionAnim))

    // Color de Borde dinámico
    val colorBordeBe = SharedPalette.ElectricCyan.copy(alpha = 0.6f)
    val colorBordeUsuario = Color.White.copy(alpha = 0.12f)
    
    val colorBordeFinal by animateColorAsState(
        targetValue = if (estaEnFoco) SharedPalette.ElectricCyan else colorBordeBe,
        animationSpec = tween(duracionAnim)
    )
    val alfaPista by animateFloatAsState(
        targetValue = if (estaEnFoco) 0.2f else 0.85f, // 🔥 Be habla claro, se apaga al escribir
        animationSpec = tween(duracionAnim)
    )
    val alfaInput by animateFloatAsState(
        targetValue = if (estaEnFoco) 0.6f else 1.0f, // 🔥 El input es más discreto que la voz de Be
        animationSpec = tween(duracionAnim)
    )

    // Animación de Vibración de Cola (Solo en modo Be)
    val transicionInfinita = rememberInfiniteTransition(label = "anim_habla")
    val vibracionCola by transicionInfinita.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse)
    )
    val vibracionFinal = if (estaEnFoco) 0f else vibracionCola

    val formaDinamica = FormaBurbujaDinamicaBe(sesgoHaciaDerecha = sesgoCola, vibracionPunta = vibracionFinal)

    // --- 3. CONSTRUCCIÓN DE LA BURBUJA ---
    Box(
        modifier = modificador
            .fillMaxWidth()
            .height(50.dp)
            .onFocusChanged { estaEnFoco = it.isFocused }
            .clip(formaDinamica)
            .background(Brush.verticalGradient(listOf(fondoActualA, fondoActualB)))
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    if (!estaEnFoco) {
                        drawPath(
                            path = Path().apply {
                                moveTo(size.width - 20f, size.height)
                                lineTo(size.width, 14f)
                                quadraticTo(size.width, 0f, size.width - 14f, 0f)
                                lineTo(40f, 0f)
                            },
                            color = Color.White.copy(alpha = 0.08f),
                            style = Stroke(width = 1f)
                        )
                    }
                }
            }
            .border(
                width = if (estaEnFoco) 1.2.dp else 0.8.dp,
                color = colorBordeFinal, // 🔥 Usando el color unificado (Cian)
                shape = formaDinamica
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val paddingInicio by animateDpAsState(targetValue = if (estaEnFoco) 16.dp else 24.dp)
        val paddingFin by animateDpAsState(targetValue = if (estaEnFoco) 24.dp else 12.dp)

        Row(
            modifier = Modifier.fillMaxSize().padding(start = paddingInicio, end = paddingFin),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (consulta.isEmpty()) {
                    TextCompactoAutoFit(
                        text = textoPista.uppercase(),
                        color = Color.White.copy(alpha = alfaPista),
                        maxFontSize = 13.sp,
                        minFontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        style = EstiloCompactoBase.copy(letterSpacing = 1.sp)
                    )
                }
                
                BasicTextField(
                    value = consulta,
                    onValueChange = alCambiarConsulta,
                    modifier = Modifier.fillMaxWidth().focusRequester(requeridorFoco),
                    textStyle = EstiloCompactoBase.merge(CyberTypography.TitleTech).copy(
                        color = Color.White.copy(alpha = alfaInput),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp
                    ),
                    cursorBrush = SolidColor(if (estaEnFoco) SharedPalette.ElectricCyan else Color.White),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { alBuscar() }),
                    singleLine = true
                )
            }
            
            if (consulta.isNotEmpty()) {
                IconButton(onClick = alLimpiarTexto, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpiar",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * --- FORMA BURBUJA DINÁMICA BE (MD3 MOTION) ---
 * Una forma capaz de mover su cola de izquierda a derecha suavemente.
 */
class FormaBurbujaDinamicaBe(
    private val sesgoHaciaDerecha: Float = 0f, 
    private val vibracionPunta: Float = 0f
) : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val d = density.density
        val r = 8f * d
        val ac = 10f * d
        val hc = 10f * d
        val v = vibracionPunta * d

        val w = size.width
        val h = size.height
        
        val centroColaY = h * 0.35f
        val topeColaY = centroColaY - (hc / 2f)
        val baseColaY = centroColaY + (hc / 2f)

        val cuerpoI = ac * (1f - sesgoHaciaDerecha)
        val cuerpoD = w - (ac * sesgoHaciaDerecha)

        val ruta = Path().apply {
            moveTo(cuerpoI + r, 0f)
            lineTo(cuerpoD - r, 0f)
            quadraticTo(cuerpoD, 0f, cuerpoD, r)
            if (sesgoHaciaDerecha > 0.01f) {
                lineTo(cuerpoD, topeColaY)
                lineTo(cuerpoD + (ac * sesgoHaciaDerecha), centroColaY + v)
                lineTo(cuerpoD, baseColaY)
            }
            lineTo(cuerpoD, h - r)
            quadraticTo(cuerpoD, h, cuerpoD - r, h)
            lineTo(cuerpoI + r, h)
            quadraticTo(cuerpoI, h, cuerpoI, h - r)
            if (sesgoHaciaDerecha < 0.99f) {
                lineTo(cuerpoI, baseColaY)
                lineTo(cuerpoI - (ac * (1f - sesgoHaciaDerecha)), centroColaY + v)
                lineTo(cuerpoI, topeColaY)
            }
            lineTo(cuerpoI, r)
            quadraticTo(cuerpoI, 0f, cuerpoI + r, 0f)
            close()
        }
        return Outline.Generic(ruta)
    }
}

@Preview(name = "Burbuja Morphing", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewBarraBusquedaTacticaV3Vacia() {
    val requeridorFoco = remember { FocusRequester() }
    PBEMTheme {
        Box(modifier = Modifier.padding(24.dp)) {
            BarraBusquedaTacticaV3(
                alCambiarConsulta = {},
                flujoConsulta = kotlinx.coroutines.flow.MutableStateFlow(""),
                alBuscar = {},
                requeridorFoco = requeridorFoco,
                alLimpiarTexto = {}
            )
        }
    }
}

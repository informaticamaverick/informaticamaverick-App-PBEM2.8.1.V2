package com.example.myapplication.ui.componentes.sistema.cabecera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import com.example.myapplication.uishared.estilos.CPCyberColors
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.core.dominio.modelos.DireccionDominio

/**
 * --- 🛡️ CARCASA BASE CABECERA CYBER V3 ---
 * [PROPÓSITO]: Proveer el ADN visual (colores, bordes, neones) a todas las cabeceras.
 * [LEY #10]: Centraliza la estética para garantizar paridad en todo el ecosistema.
 */
@Composable
private fun CarcasaCabeceraCyberV3(
    modifier: Modifier = Modifier,
    fraccionColapso: Float = 0f,
    alturaMaxima: androidx.compose.ui.unit.Dp = 70.dp,
    colorFondo: Color = CPCyberColors.DeepVoid, // 🔥 [ELITE] Sólido por defecto
    esHome: Boolean = false, // 🔥 [NEW]: Comportamiento específico para Home
    contenido: @Composable BoxScope.() -> Unit
) {
    val alturaFinal = if (esHome) 70.dp else lerp(alturaMaxima, 56.dp, fraccionColapso)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CutCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(colorFondo) 
            .drawBehind { drawCyberHeaderBorder() }
            .statusBarsPadding()
            .height(alturaFinal) 
            .padding(horizontal = 12.dp)
            .padding(bottom = 0.dp) // 🔥 [AJUSTE]: El padding se maneja internamente
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // 🔥 [RAÍZ]: Centrado para transiciones fluidas
        ) {
            contenido()
        }
    }
}

/**
 * --- 🏗️ ARMADOR CABECERA HOME ---
 * [LAYOUT]: Proporcional (1f : 1.5f : 0.9f).
 * Optimizado para el Dashboard con Perfil, Ubicación y Clima.
 */
@Composable
fun MoldeCabeceraSuperiorHome(
    slotIzquierdo: @Composable BoxScope.() -> Unit,
    slotCentral: @Composable BoxScope.() -> Unit,
    slotDerecho: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    fraccionColapso: Float = 0f
) {
    CarcasaCabeceraCyberV3(
        modifier = modifier,
        fraccionColapso = fraccionColapso,
        alturaMaxima = 70.dp,
        esHome = true 
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically // 🔥 [RAÍZ]: Centrado real equilibrado
        ) {
            Box(
                modifier = Modifier.weight(1.5f), 
                contentAlignment = Alignment.CenterStart
            ) {
                slotIzquierdo()
            }
            Box(
                modifier = Modifier.weight(1.8f), 
                contentAlignment = Alignment.Center
            ) {
                slotCentral()
            }
            Box(
                modifier = Modifier.weight(0.7f), 
                contentAlignment = Alignment.CenterEnd
            ) {
                slotDerecho()
            }
        }
    }
}

/**
 * --- 🏗️ ARMADOR CABECERA PANTALLAS ---
 * [LAYOUT]: Jerárquico (Impacto).
 * Optimizado para navegación con Título dominante y Emoji Gigante.
 */
@Composable
fun MoldeCabeceraSuperiorPantallas(
    slotIzquierdo: @Composable BoxScope.() -> Unit,
    slotCentral: @Composable BoxScope.() -> Unit,
    slotDerecho: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    fraccionColapso: Float = 0f
) {
    CarcasaCabeceraCyberV3(
        modifier = modifier,
        fraccionColapso = fraccionColapso,
        alturaMaxima = 70.dp
    ) {
        val densidad = LocalDensity.current
        // --- DINÁMICA DE POSICIONAMIENTO ELITE ---
        // Empujamos hacia abajo cuando está expandido (fraccion 0) para cercanía al borde,
        // y regresamos al centro absoluto (0dp) cuando está colapsado (fraccion 1).
        val compensacionVerticalPx = with(densidad) { (6f * (1f - fraccionColapso)).dp.toPx() }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .wrapContentSize()
                    .graphicsLayer { translationY = compensacionVerticalPx },
                contentAlignment = Alignment.CenterStart
            ) {
                slotIzquierdo()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 44.dp)
                    .fillMaxWidth()
                    .graphicsLayer { translationY = compensacionVerticalPx },
                contentAlignment = Alignment.CenterStart
            ) {
                slotCentral()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .wrapContentSize()
                    .graphicsLayer { translationY = compensacionVerticalPx },
                contentAlignment = Alignment.CenterEnd
            ) {
                slotDerecho()
            }
        }
    }
}

/**
 * --- 🏗️ ARMADOR CABECERA BÚSQUEDA BE ---
 * [LAYOUT]: Táctico (Busqueda + Identidad).
 * Optimizado para el escaneo inmersivo del asistente.
 */
@Composable
fun MoldeCabeceraBusquedaBeV3(
    slotBusqueda: @Composable BoxScope.() -> Unit,
    slotIdentidad: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    fraccionColapso: Float = 0f
) {
    CarcasaCabeceraCyberV3(
        modifier = modifier,
        fraccionColapso = fraccionColapso,
        alturaMaxima = 70.dp, // 🔥 [FIX] Estandarización de tamaño con Home/Pantallas
        colorFondo = Color(0xFF020408) // Negro Mate Profundo
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier.wrapContentSize().padding(start = 4.dp, end = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                slotIdentidad()
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                slotBusqueda()
            }
        }
    }
}

/**
 * Lógica de dibujo del borde Cyberpunk (Extraída para reutilización)
 */
private fun DrawScope.drawCyberHeaderBorder() {
    val strokeWidth = 1.2.dp.toPx()
    val path = Path().apply {
        moveTo(0f, size.height - 16.dp.toPx())
        lineTo(16.dp.toPx(), size.height)
        lineTo(size.width - 16.dp.toPx(), size.height)
        lineTo(size.width, size.height - 16.dp.toPx())
    }
    
    val borderGradient = Brush.horizontalGradient(
        0.0f to SharedPalette.ElectricCyan.copy(alpha = 0.05f),
        0.15f to SharedPalette.ElectricCyan,
        0.85f to SharedPalette.ElectricCyan,
        1.0f to SharedPalette.ElectricCyan.copy(alpha = 0.05f)
    )

    drawPath(
        path = path,
        brush = borderGradient,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    drawPath(
        path = path,
        brush = borderGradient,
        style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round),
        alpha = 0.15f
    )
}

// ==================================================================================
// --- 🧪 SECCIÓN DE PREVIEWS (DIFERENCIACIÓN TÉCNICA) ---
// ==================================================================================

@Preview(name = "1. VISTA DASHBOARD (HOME)", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewCabeceraHome() {
    MoldeCabeceraSuperiorHome(
        slotIzquierdo = {
            MoldeCabeceraSuperiorPerfil(
                nombre = "MAXI MAVERICK",
                foto = null,
                esPersonal = true,
                onClick = {}
            )
        },
        slotCentral = {
            MoldeCabeceraSuperiorUbicacion(
                direccion = DireccionDominio(localidad = "San Miguel de Tucumán"),
                onClick = {}
            )
        },
        slotDerecho = {
            MoldeCabeceraSuperiorClima(
                temperatura = "20°C",
                emoji = "☀️",
                descripcion = "Despejado",
                onClick = {}
            )
        }
    )
}

@Preview(name = "2. VISTA IMPACTO (PANTALLAS)", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewCabeceraPantallas() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Expandida
        MoldeCabeceraSuperiorPantallas(
            fraccionColapso = 0f,
            slotIzquierdo = { BotonBackCabeceraV3(onClick = {}) },
            slotCentral = { 
                ColumnaTituloSeccionV3(
                    titulo = "Mis Presupuestos",
                    subtitulo = "Gestión de Licitaciones",
                    fraccionColapso = 0f
                )
            },
            slotDerecho = { EmojiImpactoV3(emoji = "💰", fraccionColapso = 0f) }
        )

        // Colapsada
        MoldeCabeceraSuperiorPantallas(
            fraccionColapso = 1f,
            slotIzquierdo = { BotonBackCabeceraV3(onClick = {}) },
            slotCentral = { 
                ColumnaTituloSeccionV3(
                    titulo = "Mis Presupuestos",
                    subtitulo = "Gestión de Licitaciones",
                    fraccionColapso = 1f
                )
            },
            slotDerecho = { EmojiImpactoV3(emoji = "💰", fraccionColapso = 1f) }
        )

        // Búsqueda Be
        MoldeCabeceraBusquedaBeV3(
            slotBusqueda = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.White.copy(alpha = 0.05f), CutCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    androidx.compose.material3.Text("¿BUSCAS UN COMPROMISO?", color = Color.Gray, fontSize = 12.sp)
                }
            },
            slotIdentidad = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Cyan.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text("🤖")
                }
            }
        )
    }
}

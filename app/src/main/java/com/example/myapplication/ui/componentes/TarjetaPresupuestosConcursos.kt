package com.example.myapplication.ui.componentes

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.ui.components.MoldeMultiSeleccion
//import com.example.myapplication.uishared.ui.components.IndicadorSeleccion
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.uishared.ui.components.EstiloCompactoBase

// --- PALETA DE COLORES PREMIUM ---
private val CardSurface = Color(0xFF161C24)
private val CardDark = Color(0xFF0F172A)
private val appBlue = Color(0xFF2197F5)
private val StatusActive = Color(0xFF10B981)
private val StatusWarning = Color(0xFFF59E0B)
private val NeonCyber = Color(0xFF00FFC2)
private val TextGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFF94A3B8)))

/**
 * Indicador de pulsación (Ping) para notificar novedades.
 */
@Composable
fun PingAnimationIndicator(modifier: Modifier = Modifier, color: Color = NeonCyber) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(scale)
                .background(color.copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
        )
    }
}

/**
 * Divisor con profundidad Maverick Premium.
 */
@Composable
fun DepthDividerPremium(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color.Black.copy(alpha = 0.2f))
            .drawBehind {
                drawLine(
                    color = Color.White.copy(alpha = 0.06f),
                    start = Offset(0f, 1.dp.toPx()),
                    end = Offset(size.width, 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
    )
}

@Composable
private fun HeaderActionButton(
    emoji: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(28.dp),
        color = Color.White.copy(0.08f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(0.12f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else if (emoji != null) {
                Text(emoji, fontSize = 14.sp)
            }
        }
    }
}

/**
 * --- TARJETA DE CONCURSO PREMIUM (FOLDER STYLE v2026.ELITE) ---
 * [LEY #11]: Textos Elásticos.
 * [LEY #9]: Nomenclatura en Español.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConcursoFolderPremium(
    titulo: String,
    categoria: String,
    iconoCategoria: String = "📋",
    colorSupercategoria: Color = appBlue,
    idConcurso: String,
    estado: String,
    fechaInicio: Long,
    fechaFin: Long,
    conteoPresupuestos: Int,
    conteoNoLeidos: Int,
    estaSeleccionado: Boolean,
    modoMultiseleccionActivo: Boolean = false, // 🔥 [NEW]
    nombrePrestadorAdjudicado: String? = null,
    idPresupuestoAdjudicado: String? = null,
    urlFotoPrestadorAdjudicado: String? = null,
    miniaturaPrestadorAdjudicado: String? = null,
    alHacerLongClick: () -> Unit = {},
    alVerDetalles: () -> Unit = {},
    alVerPresupuestoAdjudicado: (String) -> Unit = {}, // 🔥 [NEW]
    alChatConPrestadorAdjudicado: (String) -> Unit = {}, // 🔥 [NEW]
    alHacerClick: () -> Unit
) {
    val ahora = System.currentTimeMillis()
    val estaExpirado = ahora > fechaFin && (fechaFin != 0L)
    val estadoEfectivo = if (estaExpirado && (estado == "ABIERTA" || estado == "ACTIVO")) "CERRADA" else estado
    val esAdjudicado = estado == "ADJUDICADA" || nombrePrestadorAdjudicado != null

    val diasRestantes = if (fechaFin > ahora) {
        TimeUnit.MILLISECONDS.toDays(fechaFin - ahora)
    } else 0
    
    val localizacion = LocalConfiguration.current.locales[0]
    val df = remember(localizacion) { SimpleDateFormat("dd/MM/yy", localizacion) }
    val fuenteInteraccion = remember { MutableInteractionSource() }
    val estaPresionado by fuenteInteraccion.collectIsPressedAsState()

    val alfaTonal by animateFloatAsState(
        targetValue = if (estaPresionado) 0.12f else if (estaSeleccionado) 0.08f else 0.02f,
        label = "alfaTonal"
    )
    val colorFondoAnimado = appBlue.copy(alpha = alfaTonal).compositeOver(CardSurface)
    val colorBorde = if (estaSeleccionado || estaPresionado) appBlue else Color.White.copy(alpha = 0.12f)

    MoldeMultiSeleccion(
        estaSeleccionado = estaSeleccionado,
        modoMultiseleccionActivo = modoMultiseleccionActivo,
        colorAcento = appBlue,
        radioCurvatura = 4.dp,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val escala = if (estaPresionado) 0.985f else 1f
                    scaleX = escala; scaleY = escala
                }
                .combinedClickable(interactionSource = fuenteInteraccion, indication = null, onClick = alHacerClick, onLongClick = alHacerLongClick),
            color = colorFondoAnimado,
            shape = RoundedCornerShape(4.dp), // 🔥 [ELITE]: Esquinas rectas
            border = BorderStroke(if (estaSeleccionado || estaPresionado) 1.5.dp else 1.dp, colorBorde),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- HEADER CON BARRA DE COLOR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent)))
                        .drawBehind {
                            drawLine(color = Color.White.copy(alpha = 0.12f), start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                            drawRect(
                                brush = Brush.verticalGradient(listOf(colorSupercategoria.copy(0.8f), colorSupercategoria)),
                                topLeft = Offset(0f, 0f),
                                size = Size(4.dp.toPx(), size.height)
                            )
                        }
                        .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        TextCompacto(text = iconoCategoria, fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        TextCompactoAutoFit(
                            text = categoria.uppercase(localizacion), 
                            color = Color.White.copy(alpha = 0.9f), 
                            maxFontSize = 10.sp, 
                            minFontSize = 8.sp,
                            fontWeight = FontWeight.Black, 
                            style = TextStyle(letterSpacing = 1.2.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            HeaderActionButton(emoji = "❗", onClick = alVerDetalles)
                            HeaderActionButton(icon = Icons.Default.ArrowUpward, onClick = alHacerClick)
                        }
                    }
                }

                // --- CONTENIDO PRINCIPAL ---
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextCompacto(text = "🏷️ PROYECTO", color = Color.White.copy(alpha = 0.5f), fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 0.5.sp))
                                Spacer(Modifier.width(4.dp))
                                Box(Modifier.width(1.dp).height(8.dp).background(Color.White.copy(0.2f)))
                                Spacer(Modifier.width(4.dp))
                                TextCompacto(text = "#${idConcurso.takeLast(6).uppercase(localizacion)}", color = appBlue.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            TextCompactoAutoFit(
                                text = titulo, 
                                color = Color.White, 
                                fontWeight = FontWeight.Black, 
                                maxFontSize = 16.sp, 
                                minFontSize = 12.sp,
                                maxLines = 2, 
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(brush = TextGradient)
                            )
                        }

                        Column(modifier = Modifier.width(100.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPillPremium(estado = estadoEfectivo, modifier = Modifier.wrapContentSize().fillMaxWidth())
                            
                            // Caja Inset de Ofertas
                            Surface(
                                color = Color.Black.copy(0.6f), 
                                shape = RoundedCornerShape(8.dp), 
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    TextCompacto("OFERTAS", fontSize = 7.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(contentAlignment = Alignment.TopEnd) {
                                                TextCompacto(text = conteoNoLeidos.toString(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (conteoNoLeidos > 0) NeonCyber else Color.White.copy(alpha = 0.15f))
                                                if (conteoNoLeidos > 0 && !esAdjudicado) {
                                                    PingAnimationIndicator(modifier = Modifier.offset(x = 6.dp, y = (-2).dp))
                                                }
                                            }
                                            TextCompacto("NUEVAS", fontSize = 6.sp, fontWeight = FontWeight.Black, color = if (conteoNoLeidos > 0) NeonCyber.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f))
                                        }
                                        Box(Modifier.width(0.5.dp).height(20.dp).background(Color.White.copy(0.1f)))
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            TextCompacto(text = conteoPresupuestos.toString(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (conteoPresupuestos > 0) appBlue else Color.White.copy(alpha = 0.15f))
                                            TextCompacto("TOTAL", fontSize = 6.sp, fontWeight = FontWeight.Black, color = if (conteoPresupuestos > 0) appBlue.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(6.dp))
                    DepthDividerPremium()
                    Spacer(Modifier.height(6.dp))

                    // --- LÍNEA DE TIEMPO ELITE (Dashed + Puntos) ---
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // Inicio
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.width(60.dp)) {
                            TextCompacto("INICIO", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.sp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Box(Modifier.size(6.dp).background(Color.Gray, CircleShape).border(2.dp, CardDark, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                TextCompacto(df.format(Date(fechaInicio)), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Línea Dashed + Pill Tiempo
                        Box(modifier = Modifier.weight(1f).height(30.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp).padding(top = 10.dp)) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.1f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            }
                            
                            // Time Pill
                            val pillColor = if (esAdjudicado) Color.DarkGray else StatusWarning
                            Surface(
                                color = CardDark,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                                modifier = Modifier.offset(y = 5.dp)
                            ) {
                                val timeText = if (esAdjudicado) "FINALIZADO" else if (diasRestantes > 0) "FALTAN $diasRestantes DÍAS" else "HOY"
                                TextCompacto(
                                    text = timeText, 
                                    color = if(esAdjudicado) Color.Gray else Color.White, 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Black, 
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Cierre
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(60.dp)) {
                            TextCompacto("CIERRE", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.sp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                TextCompacto(df.format(Date(fechaFin)), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                val dotColor = if (esAdjudicado) Color.Gray else StatusWarning
                                Box(Modifier.size(6.dp).background(dotColor, CircleShape).border(2.dp, CardDark, CircleShape))
                            }
                        }
                    }
                    
                    // --- SECCIÓN ADJUDICADA ELITE ---
                    AnimatedVisibility(
                        visible = esAdjudicado && nombrePrestadorAdjudicado != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Spacer(Modifier.height(16.dp))
                            DepthDividerPremium()
                            Spacer(Modifier.height(12.dp))
                            
                            Surface(
                                color = StatusActive.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp), // 🔥 [ELITE]: Esquinas rectas
                                border = BorderStroke(1.dp, StatusActive.copy(alpha = 0.3f))
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    // Avatar Premium
                                    MoldeBurbujaPerfilV3(
                                        perfil = PerfilIdentidadV3(
                                            id = "winner",
                                            nombre = nombrePrestadorAdjudicado ?: "",
                                            photoUrl = miniaturaPrestadorAdjudicado ?: urlFotoPrestadorAdjudicado,
                                            iniciales = (nombrePrestadorAdjudicado?.take(2) ?: "??").uppercase(),
                                            estaEnLinea = true,
                                            estaVerificado = true
                                        ),
                                        tamanoBase = 42.dp
                                    )
                                    
                                    Spacer(Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        TextCompacto("ADJUDICADO A", color = StatusActive, fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.5.sp))
                                        TextCompactoAutoFit(
                                            text = nombrePrestadorAdjudicado?.uppercase() ?: "", 
                                            color = Color.White, 
                                            maxFontSize = 13.sp, 
                                            minFontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        TextCompacto(
                                            text = "PRESUPUESTO #${idPresupuestoAdjudicado?.takeLast(6)?.uppercase() ?: "----"}", 
                                            color = StatusActive, 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Botones Tácticos
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HeaderActionButton(icon = Icons.Default.Description) {
                                            idPresupuestoAdjudicado?.let { alVerPresupuestoAdjudicado(it) }
                                        }
                                        HeaderActionButton(icon = Icons.AutoMirrored.Filled.Chat) {
                                            idPresupuestoAdjudicado?.let { alChatConPrestadorAdjudicado(it) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fila de información de fecha (Pública para pantallas de detalle).
 */
@Composable
fun DateInfoRowEmoji(emoji: String, etiqueta: String, fecha: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextCompacto(emoji, fontSize = 11.sp)
        Spacer(Modifier.width(6.dp))
        Column {
            TextCompacto(etiqueta, fontSize = 6.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Black)
            TextCompacto(fecha, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusPillPremium(estado: String, modifier: Modifier = Modifier) {
    val localizacion = LocalConfiguration.current.locales[0]
    val estadoMayus = estado.uppercase(localizacion)
    val color = when(estadoMayus) {
        "ACTIVO", "ABIERTA" -> Color(0xFF10B981)
        "ADJUDICADO", "ADJUDICADA" -> Color(0xFF0EA5E9)
        "TERMINADO", "CERRADA" -> Color(0xFFF43F5E)
        else -> Color.Gray
    }
    val esCancelado = estadoMayus == "CANCELADA"
    val colorFinal = if (esCancelado) Color.Gray else color

    Surface(color = colorFinal.copy(0.12f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, colorFinal.copy(0.4f)), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(colorFinal))
            Spacer(Modifier.width(6.dp))
            TextCompacto(estadoMayus, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = colorFinal, style = TextStyle(letterSpacing = 0.5.sp), textDecoration = if (esCancelado) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
        }
    }
}

@Composable
fun BudgetStatusBadge(estado: EstadoPresupuesto) {
    val (color, emoji) = when (estado) {
        EstadoPresupuesto.PENDIENTE -> Color(0xFFFACC15) to "📄"
        EstadoPresupuesto.ACEPTADO -> Color(0xFF10B981) to "✅"
        EstadoPresupuesto.RECHAZADO -> Color(0xFFEF4444) to "❌"
        else -> Color.Gray to "📄"
    }
    Surface(color = color.copy(alpha = 0.2f), shape = CircleShape, border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))) {
        Text(text = emoji, fontSize = 8.sp, modifier = Modifier.padding(2.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuestoA4Summary(
    modifier: Modifier = Modifier,
    summary: PresupuestoResumenDominio,
    estaSeleccionado: Boolean = false,
    esMultiseleccionActiva: Boolean = false,
    alHacerClickVista: () -> Unit = {},
    alHacerLongClick: () -> Unit = {}
) {
    val colorBorde = if (estaSeleccionado) appBlue else Color.White.copy(alpha = 0.1f)
    val fuenteInteraccion = remember { MutableInteractionSource() }
    val estaPresionado by fuenteInteraccion.collectIsPressedAsState()

    Surface(
        modifier = modifier
            .drawBehind {
                val shadowColor = Color.Black.copy(alpha = if (estaPresionado) 1f else 0.8f)
                val shadowRadius = if (estaPresionado) 12.dp.toPx() else 8.dp.toPx()
                val offsetY = if (estaPresionado) 6.dp.toPx() else 4.dp.toPx()
                drawIntoCanvas { canvas ->
                    @Suppress("DEPRECATION")
                    val paint = Paint().asFrameworkPaint().apply {
                        color = shadowColor.toArgb()
                        setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb())
                    }
                    canvas.nativeCanvas.drawRoundRect(0f, offsetY, size.width, size.height, 8.dp.toPx(), 8.dp.toPx(), paint)
                }
            }
            .combinedClickable(interactionSource = fuenteInteraccion, indication = null, onClick = { if (esMultiseleccionActiva) alHacerLongClick() else alHacerClickVista() }, onLongClick = alHacerLongClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.White,
        border = BorderStroke(if (estaSeleccionado) 2.dp else 1.dp, colorBorde)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            EsqueletoPresupuestoA4(modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(if (summary.esLeido) Brush.horizontalGradient(listOf(appBlue, Color(0xFF9B51E0))) else Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF10B981)))))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                Text(text = summary.nombrePrestador, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, maxLines = 1)
                Text(text = "$${summary.totalGeneral}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
            }
            summary.iconoCategoria?.let { emoji ->
                Text(text = emoji, modifier = Modifier.align(Alignment.TopStart).padding(4.dp), fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuestoA4Document(
    modifier: Modifier = Modifier,
    presupuesto: PresupuestoFinalEntity,
    nombrePrestadorVisible: String? = null,
    fotoPrestador: String? = null,
    estaSeleccionado: Boolean = false,
    esMultiseleccionActiva: Boolean = false,
    estaDentroDeConcurso: Boolean = false,
    emojiCategoria: String? = null,
    alHacerClickVista: () -> Unit = {},
    alHacerClickChat: () -> Unit = {},
    alHacerClickAvatar: () -> Unit = {},
    alHacerLongClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val localizacion = LocalConfiguration.current.locales[0]
    val nombreActivo = nombrePrestadorVisible ?: presupuesto.nombrePrestador
    val fotoActiva = fotoPrestador ?: presupuesto.urlFotoPrestador

    MoldeMultiSeleccion(
        estaSeleccionado = estaSeleccionado,
        modoMultiseleccionActivo = esMultiseleccionActiva,
        colorAcento = appBlue,
        radioCurvatura = 8.dp,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = { 
                        if (esMultiseleccionActiva) {
                            alHacerLongClick() 
                        } else alHacerClickVista() 
                    }, 
                    onLongClick = {
                        alHacerLongClick()
                    }
                ),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                EsqueletoPresupuestoA4(modifier = Modifier.fillMaxSize())
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(if (presupuesto.leido) Brush.horizontalGradient(listOf(appBlue, Color(0xFF9B51E0))) else Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF10B981)))))

                Column(modifier = Modifier.align(Alignment.TopStart).padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!estaDentroDeConcurso) {
                        Box(modifier = Modifier.height(22.dp).background(appBlue.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).border(1.dp, appBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emojiCategoria ?: "📋", fontSize = 10.sp)
                                Spacer(Modifier.width(6.dp))
                                Box(modifier = Modifier.width(1.dp).height(10.dp).background(appBlue.copy(alpha = 0.4f)))
                                Spacer(Modifier.width(6.dp))
                                Text(text = (presupuesto.idCategoria ?: "Servicio").uppercase(localizacion), color = appBlue, fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    BudgetStatusBadge(estado = presupuesto.estado)
                }

                Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(78.dp).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(brush = Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.95f)))).border(width = 1.dp, brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).padding(horizontal = 2.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                            Text("$", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonCyber)
                            Text(text = String.format(localizacion, "%,.0f", presupuesto.totalGeneral), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Row(modifier = Modifier.weight(1f).clickable(enabled = !esMultiseleccionActiva) { alHacerClickAvatar() }, verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(fotoActiva).crossfade(true).size(60, 60).build(), contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape).border(0.5.dp, Color.White.copy(0.3f), CircleShape), contentScale = ContentScale.Crop)
                                Spacer(Modifier.width(6.dp))
                                Text(text = nombreActivo.uppercase(localizacion), color = Color.White, style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp), maxLines = 2, modifier = Modifier.weight(1f))
                            }
                            Box(modifier = Modifier.width(1.dp).height(12.dp).background(Color.White.copy(alpha = 0.2f)))
                            Box(modifier = Modifier.size(24.dp).clickable(enabled = !esMultiseleccionActiva) { alHacerClickChat() }, contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EsqueletoPresupuestoA4(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(12.dp)) {
        val ancho = size.width
        drawLine(Color(0xFFE2E8F0), Offset(0.dp.toPx(), 4.dp.toPx()), Offset(ancho * 0.7f, 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(Color(0xFFF1F5F9), Offset(0.dp.toPx(), 10.dp.toPx()), Offset(ancho * 0.5f, 10.dp.toPx()), strokeWidth = 1.5.dp.toPx())
        drawRect(Color(0xFFDBEAFE), Offset(ancho - 25.dp.toPx(), 0f), size = androidx.compose.ui.geometry.Size(25.dp.toPx(), 10.dp.toPx()))
        val tablaY = 35.dp.toPx()
        val filaH = 12.dp.toPx()
        drawRect(Color(0xFFF8FAFC), Offset(0f, tablaY), size = androidx.compose.ui.geometry.Size(ancho, filaH))
        for (i in 1..5) {
            val y = tablaY + (i * filaH)
            drawLine(Color(0xFFF1F5F9), Offset(0f, y), Offset(ancho, y), strokeWidth = 0.5.dp.toPx())
            drawLine(Color(0xFFF1F5F9), Offset(4.dp.toPx(), y + 6.dp.toPx()), Offset(12.dp.toPx(), y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFFF1F5F9), Offset(18.dp.toPx(), y + 6.dp.toPx()), Offset(ancho * 0.6f, y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFFF1F5F9), Offset(ancho - 20.dp.toPx(), y + 6.dp.toPx()), Offset(ancho - 4.dp.toPx(), y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())
        }
        val totalY = tablaY + (7 * filaH)
        drawLine(Color(0xFFE2E8F0), Offset(ancho * 0.6f, totalY), Offset(ancho, totalY), strokeWidth = 1.dp.toPx())
        drawLine(Color(0xFFE2E8F0), Offset(ancho * 0.6f, totalY + 6.dp.toPx()), Offset(ancho, totalY + 6.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

// ==========================================================================================
// --- PREVIEWS (MAVERICK ELITE 2026) ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun ConcursoFolderPremiumPreview() {
    PBEMTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Estado 1: Abierta con ofertas nuevas
            ConcursoFolderPremium(
                titulo = "Remodelación Integral de Cocina y Comedor",
                categoria = "Construcción",
                iconoCategoria = "🏗️",
                idConcurso = "concurso_123",
                estado = "ABIERTA",
                fechaInicio = System.currentTimeMillis() - 86400000 * 2,
                fechaFin = System.currentTimeMillis() + 86400000 * 5,
                conteoPresupuestos = 12,
                conteoNoLeidos = 3,
                estaSeleccionado = false,
                alHacerClick = {}
            )

            // Estado 2: Adjudicada
            ConcursoFolderPremium(
                titulo = "Instalación de Sistema de Climatización AC",
                categoria = "Climatización",
                iconoCategoria = "❄️",
                idConcurso = "concurso_456",
                estado = "ADJUDICADA",
                fechaInicio = System.currentTimeMillis() - 86400000 * 10,
                fechaFin = System.currentTimeMillis() - 86400000 * 2,
                conteoPresupuestos = 8,
                conteoNoLeidos = 0,
                estaSeleccionado = false,
                nombrePrestadorAdjudicado = "Tech Solutions S.R.L.",
                idPresupuestoAdjudicado = "PRE-B902",
                urlFotoPrestadorAdjudicado = "https://images.unsplash.com/photo-1573497019940-1c28c88b4f3e?w=150",
                alHacerClick = {}
            )
        }
    }
}


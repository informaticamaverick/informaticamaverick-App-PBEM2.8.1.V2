package com.example.myapplication.prestador.ui.pantallas.market

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
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import com.example.myapplication.core.dominio.modelos.ConcursoDominio
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import androidx.compose.ui.graphics.vector.ImageVector

// --- PALETA DE COLORES PREMIUM (Adaptada Naranja) ---
private val CardSurface = Color(0xFF161C24)
private val CardDark = Color(0xFF0F172A)
private val AppOrange = Color(0xFFF97316)
private val StatusWarning = Color(0xFFF59E0B)
private val TextGradient = Brush.horizontalGradient(listOf(Color.White, Color(0xFF94A3B8)))

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
 * --- TARJETA DE CONCURSO PÚBLICO (PRESTADOR v2026.ELITE) ---
 * Basada en el molde Folder de la App Azul.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaConcursoPublico(
    concurso: ConcursoDominio,
    alHacerClick: () -> Unit,
    alResponder: () -> Unit,
    alVerDetalles: () -> Unit = {}
) {
    val localizacion = LocalConfiguration.current.locales[0]
    val df = remember(localizacion) { SimpleDateFormat("dd/MM/yy", localizacion) }
    val fuenteInteraccion = remember { MutableInteractionSource() }
    val estaPresionado by fuenteInteraccion.collectIsPressedAsState()

    val esAdjudicado = concurso.estado == "ADJUDICADA"
    
    // Color de la barra basado en la categoría o naranja por defecto
    val colorBarra = AppOrange 

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .graphicsLayer {
                val escala = if (estaPresionado) 0.985f else 1f
                scaleX = escala; scaleY = escala
            }
            .combinedClickable(
                interactionSource = fuenteInteraccion, 
                indication = null, 
                onClick = alHacerClick
            ),
        color = Color.Transparent, // El fondo se maneja con el gradiente de abajo
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.background(CardSurface)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- HEADER CON BARRA DE COLOR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color.Black.copy(0.3f), Color.Transparent)))
                        .drawBehind {
                            drawLine(color = Color.White.copy(alpha = 0.12f), start = Offset(0f, size.height), end = Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                            drawRect(
                                brush = Brush.verticalGradient(listOf(colorBarra.copy(0.8f), colorBarra)),
                                topLeft = Offset(0f, 0f),
                                size = Size(4.dp.toPx(), size.height)
                            )
                        }
                        .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        TextCompacto(text = concurso.iconoCategoria ?: "📋", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        TextCompactoAutoFit(
                            text = (concurso.categoria ?: "LICITACIÓN").uppercase(localizacion), 
                            color = Color.White.copy(alpha = 0.9f), 
                            maxFontSize = 10.sp, 
                            minFontSize = 8.sp,
                            fontWeight = FontWeight.Black, 
                            style = TextStyle(letterSpacing = 1.2.sp),
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            HeaderActionButton(emoji = "🔍", onClick = alVerDetalles)
                        }
                    }
                }

                // --- CONTENIDO PRINCIPAL ---
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextCompacto(text = "🏷️ CONCURSO", color = Color.White.copy(alpha = 0.5f), fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 0.5.sp))
                                Spacer(Modifier.width(4.dp))
                                Box(Modifier.width(1.dp).height(8.dp).background(Color.White.copy(0.2f)))
                                Spacer(Modifier.width(4.dp))
                                TextCompacto(text = "#${concurso.idConcurso.takeLast(6).uppercase(localizacion)}", color = AppOrange.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            TextCompactoAutoFit(
                                text = concurso.titulo, 
                                color = Color.White, 
                                fontWeight = FontWeight.Black, 
                                maxFontSize = 16.sp, 
                                minFontSize = 12.sp,
                                maxLines = 2, 
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(brush = TextGradient)
                            )
                        }

                        StatusPillPremium(estado = concurso.estado, modifier = Modifier.wrapContentSize())
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    DepthDividerPremium()
                    Spacer(Modifier.height(12.dp))

                    // --- LÍNEA DE TIEMPO ELITE ---
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.width(60.dp)) {
                            TextCompacto("INICIO", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.sp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Box(Modifier.size(6.dp).background(Color.Gray, CircleShape).border(2.dp, CardDark, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                TextCompacto(df.format(Date(concurso.marcaTiempo)), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(modifier = Modifier.weight(1f).height(30.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(2.dp).padding(top = 10.dp)) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.1f),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )
                            }
                            
                            Surface(
                                color = CardDark,
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                                modifier = Modifier.offset(y = 5.dp)
                            ) {
                                val timeText = if (esAdjudicado) "FINALIZADO" else concurso.tiempoRestante.uppercase()
                                TextCompacto(
                                    text = timeText, 
                                    color = if(esAdjudicado) Color.Gray else Color.White, 
                                    fontSize = 8.sp, 
                                    fontWeight = FontWeight.Black, 
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(60.dp)) {
                            TextCompacto("CIERRE", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.sp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                val fechaCierre = if (concurso.fechaFin > 0) df.format(Date(concurso.fechaFin)) else "--/--/--"
                                TextCompacto(fechaCierre, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                val dotColor = if (esAdjudicado) Color.Gray else StatusWarning
                                Box(Modifier.size(6.dp).background(dotColor, CircleShape).border(2.dp, CardDark, CircleShape))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    DepthDividerPremium()
                    Spacer(Modifier.height(12.dp))

                    // --- SECCIÓN DEL CLIENTE + BOTÓN RESPONDER ---
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // Avatar Cliente
                        MoldeBurbujaPerfilV3(
                            perfil = PerfilIdentidadV3(
                                id = concurso.idCliente,
                                nombre = concurso.nombreCliente,
                                photoUrl = concurso.urlMiniaturaCliente,
                                iniciales = concurso.nombreCliente.take(2).uppercase(),
                                estaEnLinea = false,
                                estaVerificado = true
                            ),
                            tamanoBase = 40.dp
                        )
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            TextCompacto("PUBLICADO POR", color = Color.White.copy(0.4f), fontSize = 7.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.5.sp))
                            TextCompactoAutoFit(
                                text = concurso.nombreCliente.uppercase(), 
                                color = Color.White, 
                                maxFontSize = 13.sp, 
                                minFontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            TextCompacto(
                                text = concurso.ubicacionResumen, 
                                color = AppOrange, 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Botón Responder Maverick
                        Button(
                            onClick = alResponder,
                            colors = ButtonDefaults.buttonColors(containerColor = AppOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            TextCompacto(
                                text = "RESPONDER", 
                                color = Color.White, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black,
                                style = TextStyle(letterSpacing = 1.sp)
                            )
                        }
                    }
                }
            }
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

    Surface(
        color = colorFinal.copy(0.12f), 
        shape = RoundedCornerShape(4.dp), 
        border = BorderStroke(1.dp, colorFinal.copy(0.4f)), 
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(colorFinal))
            Spacer(Modifier.width(6.dp))
            TextCompacto(estadoMayus, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = colorFinal, style = TextStyle(letterSpacing = 0.5.sp), textDecoration = if (esCancelado) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TarjetaConcursoPublicoPreview() {
    PrestadorTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            TarjetaConcursoPublico(
                concurso = ConcursoDominio(
                    idConcurso = "concurso_123",
                    idCliente = "cli_1",
                    titulo = "Remodelación Integral de Cocina y Comedor",
                    descripcion = "Necesito un profesional para reforma completa.",
                    idCategoria = "CONSTRUCCION",
                    categoria = "Construcción",
                    nombreCliente = "Maxi Nanterne",
                    urlMiniaturaCliente = null,
                    ubicacionResumen = "Zona - 4000",
                    tiempoRestante = "Cierra en 5 días",
                    estado = "ABIERTA",
                    exigeVisita = true,
                    exigeGarantia = false,
                    exigePago = true,
                    exigeDocumentacion = false,
                    urlImagenes = emptyList(),
                    marcaTiempo = System.currentTimeMillis() - 86400000,
                    fechaFin = System.currentTimeMillis() + 86400000 * 5
                ),
                alHacerClick = {},
                alResponder = {}
            )
        }
    }
}

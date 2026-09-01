package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.datos.local.entidades.IdentidadPrestadorEntity
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

private val AppBlue = Color(0xFF3B82F6)
private val NeonCyber = Color(0xFF06B6D4)
private val StatusSuccess = Color(0xFF10B981)
private val StatusPending = Color(0xFFF59E0B)

/**
 * --- TARJETA DE PRESUPUESTO ULTRA PREMIUM (v2026.ELITE) ---
 * [PROPÓSITO]: Representación visual de alto impacto para presupuestos en el archivero.
 * [LEY #10]: Screen Anatomy. A4 Aspect Ratio.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuesto(
    modifier: Modifier = Modifier,
    presupuesto: PresupuestoResumenDominio,
    estaSeleccionado: Boolean = false,
    esMultiseleccionActiva: Boolean = false,
    alHacerClick: () -> Unit = {},
    alHacerClickChat: () -> Unit = {},
    alHacerLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val locale = LocalConfiguration.current.locales[0]
    
    // Resolución de identidad (Prioridad modelo de dominio)
    val nombreFinal = presupuesto.nombrePrestador
    // 🔥 [FIX]: Para la burbuja de perfil usamos la foto del prestador, NO la miniatura del documento.
    val fotoFinalRaw = presupuesto.fotoPrestador
    
    // 🔥 [SUPREME.FIX]: Procesamos la fuente de imagen para asegurar que Base64 o URL funcionen (Ley #3)
    val fotoFinal = remember(fotoFinalRaw) { ImageUtils.processImageSource(fotoFinalRaw) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (estaSeleccionado) 0.98f else 1f,
        label = "scale_animation"
    )

    val colorBorde = if (estaSeleccionado) AppBlue else Color.Transparent

    Surface(
        modifier = modifier
            .scale(scale)
            // Relación de aspecto A4 exacta: Ancho / Alto = 1 / 1.414
            .aspectRatio(1f / 1.414f)
            .drawBehind {
                val shadowColor = Color.Black.copy(alpha = if (isPressed) 0.8f else 0.6f)
                val shadowRadius = if (isPressed) 12.dp.toPx() else 25.dp.toPx()
                val offsetY = if (isPressed) 6.dp.toPx() else 12.dp.toPx()
                drawIntoCanvas { canvas ->
                    @Suppress("DEPRECATION")
                    val paint = Paint().asFrameworkPaint().apply {
                        color = Color.Transparent.toArgb()
                        setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb())
                    }
                    canvas.nativeCanvas.drawRoundRect(
                        0f, offsetY, size.width, size.height,
                        6.dp.toPx(), 6.dp.toPx(), paint
                    )
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (esMultiseleccionActiva) alHacerLongClick() else alHacerClick() },
                onLongClick = alHacerLongClick
            ),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(if (estaSeleccionado) 2.dp else 0.dp, colorBorde)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(presupuesto.urlMiniatura ?: "https://images.unsplash.com/photo-1586281380349-632531db7ed4?q=80&w=400&auto=format&fit=crop")
                    .crossfade(true)
                    .build(),
                contentDescription = "Miniatura Documento A4",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradiente superior para contraste de badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
                    .align(Alignment.TopCenter)
            )

            if (estaSeleccionado) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBlue.copy(alpha = 0.15f))
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(22.dp)
                        .background(AppBlue, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seleccionado",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Badge Categoría
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextCompacto(text = presupuesto.iconoCategoria ?: "📋", fontSize = 9.sp)
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier.width(1.dp).height(10.dp).background(Color.White.copy(alpha = 0.3f)))
                    Spacer(Modifier.width(4.dp))
                    TextCompactoAutoFit(
                        text = (presupuesto.nombreCategoria ?: "SERVICIO").uppercase(locale),
                        color = Color.White,
                        maxFontSize = 8.sp,
                        minFontSize = 6.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(letterSpacing = 1.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Badge Estado
                val colorEstado = when(presupuesto.estado) {
                    EstadoPresupuesto.ACEPTADO -> StatusSuccess
                    EstadoPresupuesto.PENDIENTE -> StatusPending
                    EstadoPresupuesto.RECHAZADO -> Color.Red
                    else -> AppBlue
                }
                Box(
                    modifier = Modifier
                        .background(colorEstado.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .border(1.dp, colorEstado, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    TextCompacto(
                        text = presupuesto.estado.name.uppercase(locale),
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(letterSpacing = 1.sp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.46f) // 🔥 [ELITE]: Sube el fondo para dar más aire
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A).copy(alpha = 0.75f), Color.Black.copy(alpha = 0.95f))
                        )
                    )
                    .drawBehind {
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Título del Trabajo / Número (v2026.ELITE)
                    TextCompactoAutoFit(
                        text = (presupuesto.tituloTrabajo ?: "PRESUPUESTO SIN TÍTULO").uppercase(locale),
                        color = Color.White.copy(alpha = 0.9f),
                        maxFontSize = 10.sp,
                        minFontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                    )

                    if (!presupuesto.numeroPresupuesto.isNullOrBlank()) {
                        TextCompacto(
                            text = "#${presupuesto.numeroPresupuesto}",
                            color = NeonCyber.copy(alpha = 0.8f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Precio Destacado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        TextCompacto("$", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyber)
                        Spacer(Modifier.width(2.dp))
                        TextCompactoAutoFit(
                            text = String.format(locale, "%,.0f", presupuesto.totalGeneral),
                            color = Color.White,
                            maxFontSize = 18.sp, 
                            minFontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp)) // 🔥 [ELITE]: Más aire entre secciones

                    // Tarjeta de Usuario y Chat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Info Prestador (Usando MoldeBurbujaPerfilV3)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                                .clickable { if (!esMultiseleccionActiva) alHacerClickChat() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MoldeBurbujaPerfilV3(
                                perfil = PerfilIdentidadV3(
                                    id = presupuesto.idPrestador,
                                    nombre = nombreFinal,
                                    iniciales = nombreFinal.take(2).uppercase(),
                                    photoUrl = fotoFinal,
                                    estaEnLinea = false, // Datos dinámicos se manejan vía chat
                                    estaVerificado = true,
                                    esSuscripto = presupuesto.estaSuscrito,
                                    colorAcento = AppBlue
                                ),
                                tamanoBase = 32.dp, // 🔥 [ELITE]: Más presencia física
                                mostrarBadges = false
                            )
                            
                            Spacer(Modifier.width(6.dp))
                            TextCompactoAutoFit(
                                text = nombreFinal.uppercase(locale),
                                color = Color.White,
                                maxFontSize = 11.sp, // 🔥 [ELITE]: Un poco más grande
                                minFontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                style = TextStyle(letterSpacing = 0.5.sp, lineHeight = 11.sp),
                                maxLines = 2, // 🔥 [ELITE]: 2 líneas solicitadas
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Divisor vertical
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        )

                        // Botón de Chat Integrado (Sobre de mensaje solicitado)
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = alHacerClickChat
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            TextCompacto(text = "✉️", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
private fun PreviewTarjetaPresupuestoElite() {
    val mockPresupuesto = PresupuestoResumenDominio(
        idPresupuesto = "1",
        numeroPresupuesto = "001",
        tituloTrabajo = "Reparación de Cañería",
        totalGeneral = 11000.0,
        estado = EstadoPresupuesto.PENDIENTE,
        fechaTimestamp = System.currentTimeMillis(),
        esLeido = false,
        idPrestador = "p1",
        nombrePrestador = "PBEM Informática",
        fotoPrestador = null,
        idCategoria = "PLOMERIA",
        nombreCategoria = "Plomería",
        iconoCategoria = "🚰"
    )
    
    Box(modifier = Modifier.padding(20.dp).width(150.dp)) {
        TarjetaPresupuesto(
            presupuesto = mockPresupuesto,
            estaSeleccionado = false
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
private fun PreviewTarjetaPresupuestoSeleccionada() {
    val mockPresupuesto = PresupuestoResumenDominio(
        idPresupuesto = "2",
        numeroPresupuesto = "002",
        tituloTrabajo = "Pintura General",
        totalGeneral = 45000.0,
        estado = EstadoPresupuesto.ACEPTADO,
        fechaTimestamp = System.currentTimeMillis(),
        esLeido = true,
        idPrestador = "p2",
        nombrePrestador = "Pinturas Express",
        fotoPrestador = null,
        idCategoria = "DECORACION",
        nombreCategoria = "Decoración",
        iconoCategoria = "🎨"
    )
    
    Box(modifier = Modifier.padding(20.dp).width(150.dp)) {
        TarjetaPresupuesto(
            presupuesto = mockPresupuesto,
            estaSeleccionado = true,
            esMultiseleccionActiva = true
        )
    }
}

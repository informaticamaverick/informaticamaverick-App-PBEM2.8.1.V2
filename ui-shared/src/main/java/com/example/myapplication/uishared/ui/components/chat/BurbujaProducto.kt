package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.ProductoMensajeDominio
import java.util.Locale

private object ProductoBubbleTheme {
    val BackgroundDark = Color(0xFF0D1418) // Negro Mate Elite
    val SurfaceDarkInner = Color(0xFF020617)
    val BorderGlass = Color(0x1AFFFFFF)

    val BrandOrange = Color(0xFFF97316)
    val BrandOrangeLight = Color(0xFFFB923C)

    val AccentCyan = Color(0xFF06B6D4)
    val AccentEmerald = Color(0xFF10B981)

    val TextPrimary = Color(0xFFF8FAFC)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)
}

/**
 * --- BURBUJA DE PRODUCTO ELITE (v2026.FINAL) ---
 * [PROPÓSITO]: Mostrar ficha comercial simplificada y profesional.
 */
@Composable
fun ProductoChatBubble(
    producto: ProductoMensajeDominio,
    esEntrante: Boolean,
    horaMensaje: String,
    modifier: Modifier = Modifier,
    mostrarBotonComprar: Boolean = true,
    onComprar: (ProductoMensajeDominio) -> Unit = {}
) {
    var showFullScreen by remember { mutableStateOf(false) }

    val bubbleShape = if (esEntrante) {
        RoundedCornerShape(20.dp).copy(topStart = CornerSize(2.dp))
    } else {
        RoundedCornerShape(20.dp).copy(topEnd = CornerSize(2.dp))
    }

    if (showFullScreen) {
        ProductoFullScreenDialog(
            producto = producto,
            onDismiss = { showFullScreen = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (esEntrante) Alignment.Start else Alignment.End
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(16.dp, bubbleShape, spotColor = Color.Black.copy(alpha = 0.5f)),
            shape = bubbleShape,
            color = ProductoBubbleTheme.BackgroundDark,
            border = BorderStroke(1.dp, ProductoBubbleTheme.BorderGlass)
        ) {
            Column(modifier = Modifier.fillMaxWidth().clickable { showFullScreen = true }) {
                // SECCIÓN 1: PORTADA E IMAGEN
                ImagenPortadaProducto(
                    urlImagen = producto.urlImagen,
                    miniaturaBase64 = producto.miniaturaBase64,
                    titulo = producto.titulo,
                    descripcion = producto.descripcion,
                    porcentajeDescuento = producto.porcentajeDescuento,
                    esServicio = producto.esServicio,
                    idCategoria = producto.idCategoria,
                    tipoEnvio = producto.tipoEnvio,
                    envioGratis = producto.envioGratis,
                    costoEnvio = producto.costoEnvio
                )

                // SECCIÓN 2: CUERPO Y PRECIOS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tarjeta de Precio, Financiación y Tags (v2026.PROFESSIONAL)
                    TarjetaMontoYFinanciacionElite(
                        producto = producto
                    )

                    // Botón de Acción Comercial
                    if (mostrarBotonComprar) {
                        val colorBoton = if (producto.estaSolicitado) Color.Gray.copy(alpha = 0.3f) 
                                        else if (producto.esServicio) ProductoBubbleTheme.AccentCyan 
                                        else ProductoBubbleTheme.BrandOrange
                        
                        Button(
                            onClick = { if (!producto.estaSolicitado) onComprar(producto) },
                            enabled = !producto.estaSolicitado,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorBoton,
                                disabledContainerColor = Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Icon(
                                imageVector = if (producto.estaSolicitado) Icons.Default.CheckCircle else if (producto.esServicio) Icons.Default.EditNote else Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = if (producto.estaSolicitado) Color.White.copy(alpha = 0.3f) else Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            @Suppress("SpellCheckingInspection")
                            Text(
                                text = if (producto.estaSolicitado) "PEDIDO PROCESADO" 
                                       else if (producto.esServicio) "CONTRATAR SERVICIO" 
                                       else "COMPRAR AHORA",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (producto.estaSolicitado) Color.White.copy(alpha = 0.3f) else Color.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // FOOTER TIMESTAMP
                FooterSeguridadYHora(hora = horaMensaje, esEntrante = esEntrante)
            }
        }
    }
}

@Composable
private fun ImagenPortadaProducto(
    urlImagen: String,
    miniaturaBase64: String?,
    titulo: String,
    descripcion: String,
    porcentajeDescuento: Int,
    esServicio: Boolean,
    idCategoria: String,
    tipoEnvio: String,
    envioGratis: Boolean,
    costoEnvio: Double?
) {
    var expandirDescripcion by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(ProductoBubbleTheme.SurfaceDarkInner)
    ) {
        val model = remember(urlImagen, miniaturaBase64) {
            val fullImg = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(urlImagen)
            fullImg ?: com.example.myapplication.core.utilidades.ImageUtils.processImageSource(miniaturaBase64)
        }

        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = titulo,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color.White.copy(0.05f)), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (esServicio) Icons.Default.Handyman else Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Color.White.copy(0.1f),
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        // Gradient overlay (Más profundo para mejor legibilidad)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ProductoBubbleTheme.BackgroundDark.copy(alpha = 0.4f),
                            ProductoBubbleTheme.BackgroundDark.copy(alpha = 0.95f)
                        ),
                        startY = 0f
                    )
                )
        )

        // COLUMN RIGHT: TAGS & DISCOUNT (v2026.PROFESSIONAL_COLUMN)
        Column(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Discount Tag
            if (porcentajeDescuento > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ProductoBubbleTheme.AccentEmerald,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "$porcentajeDescuento% OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // 2. Categoria (Emoji dinámico)
            LabelProfessional(
                texto = "${getCategoryEmoji(idCategoria)} ${idCategoria.uppercase()}",
                color = ProductoBubbleTheme.TextPrimary.copy(alpha = 0.9f),
                bgColor = Color.Black.copy(alpha = 0.6f)
            )

            // 3. Producto / Servicio
            LabelProfessional(
                texto = if (esServicio) "🛠️ SERVICIO" else "📦 PRODUCTO",
                color = if (esServicio) ProductoBubbleTheme.AccentCyan else ProductoBubbleTheme.BrandOrangeLight,
                bgColor = Color.Black.copy(alpha = 0.6f)
            )

            // 4. Envío (Emoji inteligente)
            val envioText = when {
                envioGratis -> "🚀 ENVÍO GRATIS"
                (costoEnvio ?: 0.0) > 0 -> "🚚 ENVÍO: $ ${String.format(Locale.getDefault(), "%,.0f", costoEnvio)}"
                tipoEnvio == "CONVENIR" -> "🤝 RETIRO / A CONVENIR"
                else -> "🚚 ENVÍO A CONSULTAR"
            }
            LabelProfessional(
                texto = envioText,
                color = if(envioGratis) ProductoBubbleTheme.AccentEmerald else ProductoBubbleTheme.TextSecondary,
                bgColor = Color.Black.copy(alpha = 0.6f)
            )
        }

        // Info Overlay Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ProductoBubbleTheme.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 28.sp
            )

            if (descripcion.isNotBlank() && descripcion != "0") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.clickable { expandirDescripcion = !expandirDescripcion },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = descripcion,
                        fontSize = 13.sp,
                        color = ProductoBubbleTheme.TextSecondary,
                        maxLines = if (expandirDescripcion) 5 else 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (descripcion.length > 30) {
                        Icon(
                            imageVector = if (expandirDescripcion) Icons.Default.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = ProductoBubbleTheme.TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaMontoYFinanciacionElite(
    producto: ProductoMensajeDominio
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(1.dp, ProductoBubbleTheme.BorderGlass)
    ) {
        val anterior = producto.precioAnterior
        Column(modifier = Modifier.padding(16.dp)) {
            // FILA 1: PRECIOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (anterior != null && anterior > producto.precioActual) {
                        Text(
                            text = "Antes: $ ${String.format(Locale.getDefault(), "%,.0f", anterior)}",
                            fontSize = 12.sp,
                            color = ProductoBubbleTheme.TextMuted,
                            textDecoration = TextDecoration.LineThrough,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$ ${String.format(Locale.getDefault(), "%,.0f", producto.precioActual)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = ProductoBubbleTheme.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Final",
                            fontSize = 10.sp,
                            color = ProductoBubbleTheme.TextMuted,
                            modifier = Modifier.padding(bottom = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Método de Pago Badge
                val (metodoLabel, metodoIcon) = when(producto.metodoPago) {
                    "TARJETA_SIN_INTERES", "TARJETA_INTERES" -> "Tarjeta" to Icons.Default.CreditCard
                    "TRANSFERENCIA" -> "Transferencia" to Icons.Default.AccountBalance
                    else -> "Efectivo" to Icons.Default.Payments
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ProductoBubbleTheme.BorderGlass)
                ) {
                    Row(Modifier.padding(8.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(metodoIcon, null, tint = ProductoBubbleTheme.AccentCyan, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(metodoLabel, fontSize = 9.sp, fontWeight = FontWeight.Black, color = ProductoBubbleTheme.TextSecondary)
                    }
                }
            }

            // FILA 2: CUOTAS / FINANCIACIÓN
            if (producto.cuotasTexto.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.03f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventRepeat, null, tint = ProductoBubbleTheme.AccentEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = producto.cuotasTexto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProductoBubbleTheme.AccentEmerald,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelProfessional(texto: String, color: Color, bgColor: Color = Color.Transparent) {
    Surface(
        color = if(bgColor == Color.Transparent) color.copy(alpha = 0.08f) else bgColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if(bgColor == Color.Transparent) color.copy(alpha = 0.2f) else ProductoBubbleTheme.BorderGlass)
    ) {
        Text(
            text = texto,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "electrónica", "tecnología", "informática (técnico)" -> "💻"
        "hogar", "muebles" -> "🏠"
        "herramientas", "riel din" -> "🔧"
        "energía solar", "energía" -> "☀️"
        "limpieza" -> "🧹"
        "construcción" -> "🏗️"
        "seguridad" -> "🛡️"
        "iluminación" -> "💡"
        else -> "🏷️"
    }
}

/**
 * --- DIÁLOGO FULL SCREEN (INSTAGRAM STYLE) ---
 */
@Composable
private fun ProductoFullScreenDialog(
    producto: ProductoMensajeDominio,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val model = remember(producto.urlImagen, producto.miniaturaBase64) {
                val fullImg = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(producto.urlImagen)
                fullImg ?: com.example.myapplication.core.utilidades.ImageUtils.processImageSource(producto.miniaturaBase64)
            }

            // IMAGEN CENTRAL CON ZOOM/PAN
            if (model != null) {
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

                AsyncImage(
                    model = model,
                    contentDescription = producto.titulo,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset += pan
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            // BOTÓN CERRAR (Top Left)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            // INFO BOTTOM (Instagram Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.8f), Color.Black)
                        )
                    )
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                @Suppress("SpellCheckingInspection")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = producto.marca.uppercase(),
                        color = ProductoBubbleTheme.BrandOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = producto.titulo,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (producto.descripcion.isNotBlank()) {
                        Text(
                            text = producto.descripcion,
                            color = Color.White.copy(0.7f),
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "$ ${String.format(Locale.getDefault(), "%,.0f", producto.precioActual)}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}


@Composable
private fun FooterSeguridadYHora(
    hora: String,
    esEntrante: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = ProductoBubbleTheme.TextMuted,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Oferta vinculante via Maverick",
                fontSize = 8.sp,
                color = ProductoBubbleTheme.TextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = hora,
                fontSize = 9.sp,
                color = ProductoBubbleTheme.TextMuted
            )
            if (!esEntrante) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Leído",
                    tint = ProductoBubbleTheme.AccentCyan,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B141A)
@Composable
fun ProductoChatBubblePreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ProductoChatBubble(
            producto = ProductoMensajeDominio(
                idProducto = "PREVIEW_1",
                titulo = "Inversor Híbrido 5KW Growatt",
                descripcion = "Equipo de alta eficiencia para sistemas solares residenciales. Soporta baterías de litio de última generación.",
                marca = "Growatt",
                idCategoria = "Energía Solar",
                precioActual = 1250000.0,
                precioAnterior = 1500000.0,
                porcentajeDescuento = 15,
                cuotasTexto = "12 cuotas fijas de $ 104.166",
                envioGratis = true,
                esServicio = false
            ),
            esEntrante = true,
            horaMensaje = "12:00"
        )
        
        Spacer(Modifier.height(20.dp))

        ProductoChatBubble(
            producto = ProductoMensajeDominio(
                idProducto = "PREVIEW_2",
                titulo = "Instalación y Puesta en Marcha",
                descripcion = "Mano de obra certificada para montaje de paneles y conexión a red.",
                marca = "Maverick Hunter",
                idCategoria = "Mano de Obra",
                precioActual = 85000.0,
                esServicio = true,
                estaSolicitado = false
            ),
            esEntrante = false,
            horaMensaje = "13:45",
            mostrarBotonComprar = false
        )
    }
}

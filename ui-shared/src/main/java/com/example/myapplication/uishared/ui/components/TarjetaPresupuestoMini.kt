package com.example.myapplication.uishared.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- TARJETA DE PRESUPUESTO MINI VERTICAL (v2026.ELITE) ---
 * PROPÓSITO: Mostrar presupuestos en administradores y cuadrículas.
 * LEY #9: Nomenclatura en español y unificación visual.
 */
@Composable
fun TarjetaPresupuestoMini(
    titulo: String,
    total: String,
    estado: EstadoPresupuesto,
    modifier: Modifier = Modifier,
    miniaturaBase64: String? = null,
    iconoCategoria: String? = null,
    leido: Boolean = true,
    alHacerClick: () -> Unit = {}
) {
    val colorBorde = if (leido) SharedPalette.Slate300.copy(alpha = 0.2f) else SharedPalette.BlueEnd.copy(alpha = 0.6f)
    val radioCorte = 6.dp // 🔥 [ELITE v2026]: Bordes más rectos y técnicos

    val miniaturaBitmap = remember(miniaturaBase64) {
        if (miniaturaBase64.isNullOrBlank()) null else {
            try {
                val bytes = Base64.decode(miniaturaBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        }
    }

    Card(
        modifier = modifier
            .width(165.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(radioCorte))
            .clickable { alHacerClick() },
        shape = RoundedCornerShape(radioCorte),
        colors = CardDefaults.cardColors(containerColor = SharedPalette.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(if (leido) 1.dp else 2.dp, colorBorde)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- 🖼️ FONDO: MINIATURA O ESQUELETO (Full size) ---
            if (miniaturaBitmap != null) {
                Image(
                    bitmap = miniaturaBitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(SharedPalette.Slate50)) {
                    EsqueletoPresupuestoMini(modifier = Modifier.fillMaxSize())
                }
            }

            // --- 🌑 DEGRADADO DE LEGIBILIDAD (Base) ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 300f
                        )
                    )
            )

            // --- 🏆 INDICADORES FLOTANTES (Badge y Categoría) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                iconoCategoria?.let {
                    Surface(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = it, fontSize = 12.sp)
                        }
                    }
                }
                InsigniaEstadoPresupuesto(estado = estado)
            }

            // --- 📊 SECCIÓN DE DATOS PREMIUM (Bottom Panel) ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp, 
                        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .background(
                        color = SharedPalette.CardBg.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(10.dp)
            ) {
                Text(
                    text = titulo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                    modifier = Modifier.height(28.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = total,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }
            
            // --- 🟦 LÍNEA DE ESTADO (Ley #4) ---
            if (!leido) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFF2563EB))
                            )
                        )
                )
            }
        }
    }
}

/**
 * --- SKELETON DE TARJETA DE PRESUPUESTO MINI (v2026.ELITE) ---
 */
@Composable
fun TarjetaPresupuestoMiniSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(165.dp)
            .height(230.dp),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = SharedPalette.EliteSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize().shimmerApp())
    }
}

@Composable
fun InsigniaEstadoPresupuesto(estado: EstadoPresupuesto) {
    val (color, texto) = when (estado) {
        EstadoPresupuesto.PENDIENTE -> SharedPalette.Slate400 to "PEND"
        EstadoPresupuesto.ACEPTADO -> Color(0xFF10B981) to "OK"
        EstadoPresupuesto.RECHAZADO -> SharedPalette.WarningRed to "NO"
        EstadoPresupuesto.PAGADO -> SharedPalette.SuccessGreen to "PAGO"
        EstadoPresupuesto.VENCIDO -> SharedPalette.Slate800 to "VENC"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
fun EsqueletoPresupuestoMini(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val ancho = size.width
        val colorLinea = Color(0xFFE2E8F0)
        val colorAccent = Color(0xFFDBEAFE)

        // Líneas de cabecera
        drawLine(colorLinea, Offset(0f, 5.dp.toPx()), Offset(ancho * 0.6f, 5.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(colorLinea, Offset(0f, 12.dp.toPx()), Offset(ancho * 0.4f, 12.dp.toPx()), strokeWidth = 1.dp.toPx())

        // Cuadrado de logo/distintivo
        drawRect(colorAccent, Offset(ancho - 20.dp.toPx(), 0f), size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 10.dp.toPx()))

        // Filas de tabla
        val inicioY = 35.dp.toPx()
        val alturaFila = 10.dp.toPx()
        for (i in 0..4) {
            val y = inicioY + (i * (alturaFila + 4.dp.toPx()))
            drawLine(colorLinea.copy(alpha = 0.5f), Offset(0f, y), Offset(ancho, y), strokeWidth = 0.5.dp.toPx())
            drawLine(colorLinea, Offset(2.dp.toPx(), y + 5.dp.toPx()), Offset(ancho * 0.7f, y + 5.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(colorLinea, Offset(ancho - 15.dp.toPx(), y + 5.dp.toPx()), Offset(ancho, y + 5.dp.toPx()), strokeWidth = 1.dp.toPx())
        }

        // Bloque de total
        val totalY = inicioY + (5 * (alturaFila + 4.dp.toPx())) + 10.dp.toPx()
        drawLine(colorAccent, Offset(ancho * 0.5f, totalY), Offset(ancho, totalY), strokeWidth = 3.dp.toPx())
    }
}

// ==========================================================================================
// --- PREVIEWS (MAVERICK ELITE 2026) ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF1C222B)
@Composable
private fun PreviewTarjetaPresupuestoMini() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("ESTADOS REALES", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaPresupuestoMini(
                titulo = "Reparación de Cañería Master - Baño Principal",
                total = "$ 85.000",
                estado = EstadoPresupuesto.PENDIENTE,
                iconoCategoria = "🚰",
                leido = false
            )
            
            TarjetaPresupuestoMini(
                titulo = "Instalación Eléctrica Local Comercial",
                total = "$ 240.500",
                estado = EstadoPresupuesto.ACEPTADO,
                iconoCategoria = "⚡",
                miniaturaBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
            )
        }
        
        Text("ESTADO DE CARGA (SKELETON)", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TarjetaPresupuestoMiniSkeleton()
            TarjetaPresupuestoMiniSkeleton()
        }
    }
}


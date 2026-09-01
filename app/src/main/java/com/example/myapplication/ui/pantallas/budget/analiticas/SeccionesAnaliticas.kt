package com.example.myapplication.ui.pantallas.budget.analiticas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.local.entidades.TipoProductoFinal
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.core.dominio.motores.PresupuestoClasificado
import com.example.myapplication.core.dominio.motores.ModeloPresupuestoAnalitico
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.uishared.estilos.SharedPalette
import java.util.Locale

/**
 * --- 🏗️ SECCIONES ANALÍTICAS (ORGANISMOS - v2026.ELITE) ---
 * Título: Secciones Analíticas
 * Propósito: Organismos complejos que agrupan bloques para formar áreas funcionales del panel.
 * [LEY #9]: Estándar Mav en Español.
 */

private val ColorMaterial = Color(0xFF3B82F6)
private val ColorLabor = Color(0xFFA855F7)
private val ColorTax = Color(0xFFF43F5E)
private val GlassPanel = Color(0xFF161C24)
private val appBlue = Color(0xFF2197F5)
private val PremiumGold = Color(0xFFFFD700)
private val SuccessGreen = Color(0xFF10B981)

@Composable
fun SeccionCurvaPreciosMasiva(
    presupuestos: List<PresupuestoConItems>,
    alHacerClickMaximizar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = GlassPanel,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Insights, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    TextCompacto(
                        text = "CURVA DE PRECIOS MASIVA (${presupuestos.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.sp)
                    )
                }
                IconButton(
                    onClick = alHacerClickMaximizar,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(0.1f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Fullscreen, "Pantalla Completa", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                if (presupuestos.isEmpty()) {
                    TextCompacto("Sin datos", color = Color.Gray)
                } else {
                    GraficoBarrasMiniatura(presupuestos)
                }
            }
            Spacer(Modifier.height(8.dp))
            TextCompacto(
                text = "Toca el icono de maximizar para análisis interactivo",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GraficoBarrasMiniatura(presupuestos: List<PresupuestoConItems>) {
    val rawMaxTotal = presupuestos.maxOfOrNull { it.cabecera.totalGeneral }?.toFloat() ?: 0f
    val maxTotal = if (rawMaxTotal > 0f) rawMaxTotal else 1f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val barWidth = size.width / presupuestos.size.coerceAtLeast(1)
        val gap = barWidth * 0.2f
        val actualWidth = barWidth - gap
        
        presupuestos.forEachIndexed { index, relacion ->
            val budget = relacion.cabecera
            val total = budget.totalGeneral.toFloat()
            
            // 🔥 [ANALYTICS v2026.SUPREME] Uso de campos denormalizados para Zero-Lag
            val mat = budget.subtotalArticulos.toFloat()
            val lab = budget.subtotalServicios.toFloat()
            val tax = budget.totalImpuestos.toFloat()
            
            val alpha = if (total > maxTotal * 0.8f) 0.3f else 1f
            val hTotal = (total / maxTotal) * size.height
            val hMat = if (total > 0f) (mat / total) * hTotal else 0f
            val hLab = if (total > 0f) (lab / total) * hTotal else 0f
            val hTax = if (total > 0f) (tax / total) * hTotal else 0f
            
            val x = index * barWidth + gap / 2
            val yTax = size.height - hTotal
            
            drawRect(ColorTax, Offset(x, yTax), Size(actualWidth, hTax), alpha = alpha)
            val yLab = yTax + hTax
            drawRect(ColorLabor, Offset(x, yLab), Size(actualWidth, hLab), alpha = alpha)
            val yMat = yLab + hLab
            drawRect(ColorMaterial, Offset(x, yMat), Size(actualWidth, hMat), alpha = alpha)
        }
    }
}

@Composable
fun SeccionRankingTopElite(
    presupuestosRankeados: List<PresupuestoClasificado>, 
    alHacerClickPrestador: (PresupuestoClasificado) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.EmojiEvents, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            TextCompacto(
                text = "app SCORE: TOP 10", 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Black, 
                color = Color.White, 
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.sp)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presupuestosRankeados.size) { index ->
                val ranked = presupuestosRankeados[index]
                TarjetaRankingPrestador(
                    rank = index + 1, 
                    presupuestoRankeado = ranked, 
                    onClick = { alHacerClickPrestador(ranked) }
                )
            }
        }
    }
}

@Composable
fun TarjetaInsightIA(
    presupuestosRankeados: List<PresupuestoClasificado>
) {
    val mejorBalanceado = presupuestosRankeados.firstOrNull()
    if (mejorBalanceado != null) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF581C87))))
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, null, tint = PremiumGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    TextCompacto(
                        text = "app AI INSIGHT", 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color(0xFF22D3EE), 
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.sp)
                    )
                    Spacer(Modifier.height(4.dp))
                    TextCompacto(
                        text = "La IA de app sugiere aceptar la oferta de ${mejorBalanceado.presupuesto.cabecera.nombrePrestador}. Aunque puede no ser la más barata absoluta, tiene el mejor balance entre precio, experiencia comprobada (${mejorBalanceado.trabajosRealizados} trabajos) y calidad técnica.", 
                        fontSize = 11.sp, 
                        color = Color.LightGray, 
                        maxLines = 5,
                        style = androidx.compose.ui.text.TextStyle(lineHeight = 18.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun SeccionMatrizComparativaTecnica(
    presupuestosAnaliticos: List<ModeloPresupuestoAnalitico>,
    presupuestosRankeados: List<PresupuestoClasificado>,
    alHacerClickPrestador: (ModeloPresupuestoAnalitico) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(Modifier.padding(20.dp)) {
            TextCompacto("MATRIZ DE COMPARACIÓN TÉCNICA", fontSize = 10.sp, fontWeight = FontWeight.Black, color = appBlue)
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column {
                    // Cabecera de Nombres
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextCompacto("CONCEPTO", Modifier.width(110.dp), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        presupuestosAnaliticos.forEach { model ->
                            TextCompacto(
                                text = model.nombrePrestador.split(" ").first().uppercase(), 
                                modifier = Modifier.width(100.dp).clickable { alHacerClickPrestador(model) }, 
                                fontSize = 10.sp, 
                                color = Color.White, 
                                fontWeight = FontWeight.Black, 
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(0.1f))
                    
                    // Métricas
                    val locale = LocalConfiguration.current.locales[0]
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        TextCompacto("MÉTRICA GLOBAL", Modifier.width(110.dp), fontSize = 10.sp, color = Color.Gray)
                        presupuestosAnaliticos.forEach { model ->
                            val score = presupuestosRankeados.find { it.presupuesto.cabecera.idPresupuesto == model.presupuesto.cabecera.idPresupuesto }?.puntaje ?: 0.0
                            TextCompacto(
                                text = "${String.format(locale, "%.1f", score)} / 10", 
                                modifier = Modifier.width(100.dp), 
                                fontSize = 10.sp, 
                                color = appBlue, 
                                textAlign = TextAlign.Center, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    val minTotal = presupuestosAnaliticos.minOfOrNull { it.presupuesto.cabecera.totalGeneral } ?: 0.0
                    FilaPrecioMatriz("TOTAL FINAL", presupuestosAnaliticos.map { it.presupuesto.cabecera }, minTotal, esTotal = true) { it.totalGeneral }
                }
            }
            Spacer(Modifier.height(16.dp))
            TextCompacto("Toca el nombre del proveedor para un análisis multidimensional detallado.", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun HojaDetalleProfundoPrestador(
    budget: PresupuestoConItems,
    rankedInfo: PresupuestoClasificado?,
    marketAvgTotal: Double,
    alVerPresupuestoCompleto: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cabecera = budget.cabecera
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = rankedInfo?.fotoPrestadorAlternativo ?: cabecera.urlFotoPrestador, 
                contentDescription = null, 
                fallback = rememberVectorPainter(Icons.Default.Person), 
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(0.1f), CircleShape), 
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                TextCompacto(
                    text = rankedInfo?.nombrePrestadorAlternativo ?: cabecera.nombreEmpresaPrestador ?: cabecera.nombrePrestador, 
                    color = Color.White, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Black
                )
                if (rankedInfo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Star, null, tint = SharedPalette.GoldPremium, modifier = Modifier.size(16.dp))
                        TextCompacto(
                            text = " ${rankedInfo.reputacion} • ${rankedInfo.trabajosRealizados} Trabajos", 
                            color = Color.LightGray, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Desglose vs Mercado
        Surface(
            color = Color.White.copy(0.03f), 
            shape = RoundedCornerShape(16.dp), 
            border = BorderStroke(1.dp, Color.White.copy(0.05f))
        ) { 
            Column(modifier = Modifier.padding(16.dp)) { 
                TextCompacto(
                    text = "DESGLOSE VS MERCADO", 
                    color = Color.Gray, 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.sp)
                )
                Spacer(Modifier.height(16.dp))
                BarraComparativaMercado("Total Final", cabecera.totalGeneral, marketAvgTotal, esMenorMejor = true) 
            } 
        }
        
        Spacer(Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) { 
            OutlinedButton(
                onClick = onDismiss, 
                modifier = Modifier.weight(1f).height(50.dp), 
                shape = RoundedCornerShape(12.dp), 
                border = BorderStroke(1.dp, Color.White.copy(0.2f))
            ) { 
                TextCompacto("CERRAR", color = Color.White, fontWeight = FontWeight.Bold) 
            }
            Button(
                onClick = { alVerPresupuestoCompleto(cabecera.idPresupuesto) }, 
                modifier = Modifier.weight(1f).height(50.dp), 
                shape = RoundedCornerShape(12.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = appBlue)
            ) { 
                TextCompacto("VER PRESUPUESTO", color = Color.White, fontWeight = FontWeight.Black) 
            } 
        }
    }
}

@Preview(backgroundColor = 0xFF020408, showBackground = true)
@Composable
private fun PreviewSeccionesAnaliticas() {
    val mockBudget = PresupuestoFinalEntity(
        idPresupuesto = "1",
        nombrePrestador = "Maverick Hunter",
        totalGeneral = 42000.0,
        estado = EstadoPresupuesto.ACEPTADO,
        idCategoria = "Tecnología"
    )
    val mockRelacion = PresupuestoConItems(
        cabecera = mockBudget,
        lineas = emptyList(),
        finanzas = emptyList()
    )
    val mockRanked = listOf(
        PresupuestoClasificado(
            presupuesto = mockRelacion,
            puntaje = 9.2,
            reputacion = 4.9f,
            trabajosRealizados = 150,
            reconocimientos = listOf("Mejor Precio"),
            puntajeRelacionPrecioCalidad = 9.5
        )
    )

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SeccionRankingTopElite(presupuestosRankeados = mockRanked, alHacerClickPrestador = {})
        TarjetaInsightIA(presupuestosRankeados = mockRanked)
    }
}


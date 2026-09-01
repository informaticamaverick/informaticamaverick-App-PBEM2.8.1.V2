package com.example.myapplication.ui.pantallas.budget.analiticas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.motores.PresupuestoClasificado
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import java.util.Locale

/**
 * --- 🧱 BLOQUES ANALÍTICOS (MOLÉCULAS - v2026.ELITE) ---
 * Título: Bloques Analíticos
 * Propósito: Moléculas visuales que agrupan átomos para formar componentes comerciales complejos.
 * [LEY #9]: Estándar Mav en Español.
 */

private val appBlue = Color(0xFF2197F5)
private val PremiumGold = Color(0xFFFFD700)
private val SuccessGreen = Color(0xFF10B981)
private val WarningAmber = Color(0xFFF59E0B)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaRankingPrestador(
    rank: Int,
    presupuestoRankeado: PresupuestoClasificado,
    onClick: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val isTop3 = rank <= 3
    val borderColor = when (rank) {
        1 -> PremiumGold
        2 -> Color(0xFF94A3B8)
        3 -> Color(0xFFB45309)
        else -> Color.White.copy(0.1f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.width(260.dp),
        color = Color.White.copy(0.02f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isTop3) 1.5.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextCompacto("#$rank", fontSize = 18.sp, fontWeight = FontWeight.Black, color = borderColor)
                    Spacer(Modifier.width(12.dp))
                    AsyncImage(
                        model = presupuestoRankeado.fotoPrestadorAlternativo ?: presupuestoRankeado.presupuesto.cabecera.urlFotoPrestador,
                        contentDescription = null,
                        fallback = rememberVectorPainter(Icons.Default.Person),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(0.2f)),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    color = appBlue.copy(0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, appBlue)
                ) {
                    TextCompacto(
                        text = String.format(locale, "%.1f", presupuestoRankeado.puntaje),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            TextCompacto(
                text = presupuestoRankeado.nombrePrestadorAlternativo ?: presupuestoRankeado.presupuesto.cabecera.nombrePrestador,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TextCompacto(
                text = presupuestoRankeado.presupuesto.cabecera.nombreEmpresaPrestador ?: "Independiente",
                color = appBlue,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Icon(Icons.Default.Star, null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                TextCompacto("${presupuestoRankeado.reputacion}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                TextCompacto(" • ${presupuestoRankeado.trabajosRealizados} trabajos", color = Color.Gray, fontSize = 10.sp)
            }
            
            if (presupuestoRankeado.reconocimientos.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presupuestoRankeado.reconocimientos.take(2).forEach { award ->
                        val (icon, color) = when(award) {
                            "Mejor Precio" -> Icons.AutoMirrored.Filled.TrendingDown to SuccessGreen
                            "Top Rated" -> Icons.Default.WorkspacePremium to PremiumGold
                            "Smart Choice" -> Icons.Default.Lightbulb to appBlue
                            else -> Icons.Default.CheckCircle to Color.LightGray
                        }
                        Surface(
                            color = color.copy(0.1f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, color.copy(0.3f))
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, null, tint = color, modifier = Modifier.size(8.dp))
                                Spacer(Modifier.width(2.dp))
                                TextCompacto(award, color = color, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(0.05f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                TextCompacto("OFERTA FINAL:", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                TextCompacto("$ ${String.format(locale, "%,.0f", presupuestoRankeado.presupuesto.cabecera.totalGeneral)}", color = SuccessGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun BarraComparativaMercado(
    label: String,
    valor: Double,
    promedio: Double,
    esMenorMejor: Boolean
) {
    val locale = LocalConfiguration.current.locales[0]
    val diff = if (promedio > 0) ((valor - promedio) / promedio) * 100 else 0.0
    val esPositivo = if (esMenorMejor) diff <= 0 else diff >= 0
    val colorDiferencia = if (esPositivo) SuccessGreen else Color(0xFFF43F5E)
    
    val escalaMaxima = maxOf(valor, promedio) * 1.5
    val porcentajeLlenado = if(escalaMaxima > 0) (valor / escalaMaxima).toFloat() else 0f
    val porcentajePromedio = if(escalaMaxima > 0) (promedio / escalaMaxima).toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextCompacto(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            TextCompacto(
                text = "${if(diff > 0) "+" else ""}${String.format(locale, "%.1f", diff)}%",
                color = colorDiferencia,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(modifier = Modifier.fillMaxWidth().height(10.dp)) {
            Box(modifier = Modifier.fillMaxWidth(porcentajePromedio).fillMaxHeight().background(Color.Gray.copy(0.3f), CircleShape))
            Box(modifier = Modifier.fillMaxWidth(porcentajeLlenado).fillMaxHeight().background(colorDiferencia, CircleShape))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextCompacto("Oferta: $${String.format(locale, "%,.0f", valor)}", color = Color.LightGray, fontSize = 10.sp)
            TextCompacto("Promedio: $${String.format(locale, "%,.0f", promedio)}", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun FilaPrecioMatriz(
    etiqueta: String,
    presupuestos: List<PresupuestoFinalEntity>,
    mejorValor: Double,
    esTotal: Boolean = false,
    extractorValor: (PresupuestoFinalEntity) -> Double
) {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = Modifier.padding(vertical = 6.dp).then(if(esTotal) Modifier.padding(vertical = 8.dp) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextCompacto(
            text = etiqueta,
            modifier = Modifier.width(110.dp).padding(start = if (esTotal) 8.dp else 0.dp),
            fontSize = if(esTotal) 10.sp else 11.sp,
            color = if(esTotal) Color.White else Color.LightGray,
            fontWeight = if(esTotal) FontWeight.ExtraBold else FontWeight.Normal
        )
        presupuestos.forEach { p ->
            val valor = extractorValor(p)
            val esMejor = valor == mejorValor && valor > 0
            Surface(
                modifier = Modifier.width(100.dp),
                color = if (esMejor && !esTotal) SuccessGreen.copy(0.1f) else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                TextCompacto(
                    text = "$ ${String.format(locale, "%,.0f", valor)}",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontSize = if(esTotal) 13.sp else 11.sp,
                    color = if (esMejor) SuccessGreen else if(esTotal) Color.White else Color.Gray,
                    fontWeight = if(esMejor || esTotal) FontWeight.Black else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    if (!esTotal) HorizontalDivider(color = Color.White.copy(0.03f))
}

@Preview(backgroundColor = 0xFF020408, showBackground = true)
@Composable
private fun PreviewBloquesAnaliticos() {
    val mockBudget = PresupuestoFinalEntity(
        idPresupuesto = "1",
        nombrePrestador = "Maverick Hunter",
        totalGeneral = 42000.0,
        estado = EstadoPresupuesto.ACEPTADO
    )
    val mockRanked = PresupuestoClasificado(
        presupuesto = PresupuestoConItems(
            cabecera = mockBudget,
            lineas = emptyList(),
            finanzas = emptyList()
        ),
        puntaje = 9.2,
        reputacion = 4.9f,
        trabajosRealizados = 150,
        reconocimientos = listOf("Mejor Precio", "Top Rated"),
        puntajeRelacionPrecioCalidad = 9.5
    )

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextCompacto("RANKING CARD:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        TarjetaRankingPrestador(rank = 1, presupuestoRankeado = mockRanked, onClick = {})
        
        TextCompacto("COMPARISON BAR:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        BarraComparativaMercado(label = "Presupuesto vs Mercado", valor = 42000.0, promedio = 45000.0, esMenorMejor = true)
        
        TextCompacto("MATRIX ROW:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        FilaPrecioMatriz(
            etiqueta = "Materiales", 
            presupuestos = listOf(mockBudget), 
            mejorValor = 42000.0
        ) { it.totalGeneral }
    }
}


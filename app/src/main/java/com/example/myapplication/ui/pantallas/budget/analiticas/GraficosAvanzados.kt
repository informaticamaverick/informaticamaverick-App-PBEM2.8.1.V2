package com.example.myapplication.ui.pantallas.budget.analiticas

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.motores.EstadoAnaliticaMercado
import com.example.myapplication.core.dominio.motores.ElementoGraficoPresupuesto
import com.example.myapplication.uishared.ui.components.TextCompacto
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.ui.estilos.ClienteTheme
import androidx.compose.ui.tooling.preview.Preview

/**
 * --- 📈 GRÁFICOS AVANZADOS (v2026.ELITE) ---
 * Título: Gráficos de Análisis Profundo
 * Propósito: Ofrecer visualizaciones interactivas de mercado en pantalla completa.
 * Funcionamiento Interno: Implementa gráficos de barras superpuestas con modo aislamiento y zoom táctico.
 * Relación: Invocado por el Armador de Analíticas bajo demanda del usuario.
 * [LEY #7]: Trazabilidad Hormiga habilitada.
 * [LEY #9]: Estándar Mav en Español.
 */

private val FondoOscuro = Color(0xFF020408)
private val VerdeExito = Color(0xFF10B981)
private val ColorMaterial = Color(0xFF3B82F6)
private val ColorManoObra = Color(0xFFA855F7)
private val ColorImpuesto = Color(0xFFF43F5E)

@Composable
fun GraficoComparativoHorizontal(
    estadoMercado: EstadoAnaliticaMercado, 
    alCerrar: () -> Unit, 
    alVerPresupuesto: (String) -> Unit
) {
    val alcanceCorrutina = rememberCoroutineScope()
    val estadoLista = rememberLazyListState()
    var modoAislamiento by remember { mutableStateOf("total") }
    var estaOrdenadoPorOptimo by remember { mutableStateOf(false) }
    var idPresupuestoSeleccionado by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d("GRAFICOS_ELITE", "[ANALISIS_HORIZONTAL] Pantalla Completa Activada.")
    }

    val elementosVisualizacion = remember(estadoMercado.elementos, estaOrdenadoPorOptimo) { 
        if (estaOrdenadoPorOptimo) estadoMercado.elementos.sortedWith(compareBy({ it.esIrrisorio }, { !it.esOptimo }, { it.total })) 
        else estadoMercado.elementos 
    }
    
    val valoresVisualizacionValidos = elementosVisualizacion.filter { !it.esIrrisorio }.map { 
        when(modoAislamiento) { 
            "mat" -> it.materiales 
            "lab" -> it.manoObra 
            "tax" -> it.impuestos 
            else -> it.total 
        } 
    }
    
    val valorMaximoCrudo = valoresVisualizacionValidos.maxOrNull() ?: 0.0
    val valorMaximoVisual = if (valorMaximoCrudo > 0.0) valorMaximoCrudo else 1.0
    val valorMinimoVisual = valoresVisualizacionValidos.minOrNull() ?: 0.0
    val valorPromedioVisual = if(valoresVisualizacionValidos.isNotEmpty()) valoresVisualizacionValidos.average() else 0.0
    val techoVisual = valorMaximoVisual * 1.15

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { idPresupuestoSeleccionado = null }, 
        color = FondoOscuro
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF2197F5).copy(0.08f), Color.Transparent), center = Offset(500f, 1500f), radius = 1500f)))
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.displayCutout)) {
            // --- CABECERA DE CONTROL ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF05070A).copy(0.85f))
                    .border(1.dp, Color.White.copy(0.08f))
                    .padding(horizontal = 24.dp, vertical = 8.dp), 
                horizontalArrangement = Arrangement.SpaceBetween, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column { 
                        TextCompacto("ANALIZANDO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray, style = TextStyle(letterSpacing = 1.sp))
                        TextCompacto("${estadoMercado.elementos.size}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White) 
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                    Column { 
                        TextCompacto("PROMEDIO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5), style = TextStyle(letterSpacing = 1.sp))
                        TextCompacto("$ ${(estadoMercado.promedioTotal/1000).toInt()}k", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2197F5)) 
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                    
                    Surface(
                        onClick = { 
                            estaOrdenadoPorOptimo = !estaOrdenadoPorOptimo
                            Log.d("GRAFICOS_ELITE", "[FILTRO] Orden Óptimo: $estaOrdenadoPorOptimo")
                            alcanceCorrutina.launch { estadoLista.animateScrollToItem(0) } 
                        }, 
                        shape = RoundedCornerShape(12.dp), 
                        color = if (estaOrdenadoPorOptimo) VerdeExito else VerdeExito.copy(0.1f), 
                        border = BorderStroke(1.dp, VerdeExito.copy(0.4f)), 
                        modifier = Modifier.padding(start = 8.dp)
                    ) { 
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { 
                            Icon(Icons.Default.TrackChanges, null, tint = if (estaOrdenadoPorOptimo) FondoOscuro else VerdeExito, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(6.dp))
                            TextCompacto(if (estaOrdenadoPorOptimo) "VIENDO ÓPTIMOS" else "AGRUPAR ÓPTIMOS", fontSize = 8.sp, fontWeight = FontWeight.Black, color = if (estaOrdenadoPorOptimo) FondoOscuro else VerdeExito) 
                        } 
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { 
                    TextCompacto("VISTA:", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                    BotonAislamientoAnalitico("Total", Icons.Default.Layers, Color.White, modoAislamiento == "total") { modoAislamiento = "total" }
                    BotonAislamientoAnalitico("Mat", Icons.Default.Square, ColorMaterial, modoAislamiento == "mat") { modoAislamiento = "mat" }
                    BotonAislamientoAnalitico("Obra", Icons.Default.Square, ColorManoObra, modoAislamiento == "lab") { modoAislamiento = "lab" }
                    BotonAislamientoAnalitico("Tasa", Icons.Default.Square, ColorImpuesto, modoAislamiento == "tax") { modoAislamiento = "tax" }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = alCerrar, modifier = Modifier.background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))) { 
                        Icon(Icons.Default.CloseFullscreen, null, tint = Color.Gray) 
                    } 
                }
            }

            // --- ÁREA DE GRÁFICO ---
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                val alturaPx = constraints.maxHeight.toFloat()
                val animacionMax by animateFloatAsState(targetValue = (valorMaximoVisual / techoVisual).toFloat(), animationSpec = tween(600), label = "max")
                val animacionPromedio by animateFloatAsState(targetValue = (valorPromedioVisual / techoVisual).toFloat(), animationSpec = tween(600), label = "avg")
                val animacionMinimo by animateFloatAsState(targetValue = (valorMinimoVisual / techoVisual).toFloat(), animationSpec = tween(600), label = "min")
                
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp)) { 
                    val ancho = size.width; val alto = size.height
                    val efectoTrazo = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    val yMaximo = alto - (alto * animacionMax)
                    drawLine(ColorImpuesto.copy(0.5f), Offset(0f, yMaximo), Offset(ancho, yMaximo), strokeWidth = 2f, pathEffect = efectoTrazo)
                    val yPromedio = alto - (alto * animacionPromedio)
                    drawLine(Color(0xFF2197F5).copy(0.5f), Offset(0f, yPromedio), Offset(ancho, yPromedio), strokeWidth = 2f, pathEffect = efectoTrazo)
                    val yMinimo = alto - (alto * animacionMinimo)
                    drawLine(VerdeExito.copy(0.5f), Offset(0f, yMinimo), Offset(ancho, yMinimo), strokeWidth = 2f, pathEffect = efectoTrazo) 
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp, top = 20.dp)) { 
                    EtiquetaPicoAnalitica("Pico Máx: $${(valorMaximoVisual/1000).toInt()}k", ColorImpuesto, animacionMax, this.maxHeight)
                    EtiquetaPicoAnalitica("Promedio: $${(valorPromedioVisual/1000).toInt()}k", Color(0xFF2197F5), animacionPromedio, this.maxHeight)
                    EtiquetaPicoAnalitica("Pico Mín: $${(valorMinimoVisual/1000).toInt()}k", VerdeExito, animacionMinimo, this.maxHeight) 
                }

                LazyRow(
                    state = estadoLista, 
                    modifier = Modifier.fillMaxSize().padding(bottom = 10.dp, top = 20.dp), 
                    contentPadding = PaddingValues(horizontal = 60.dp), 
                    horizontalArrangement = Arrangement.spacedBy(0.dp), 
                    verticalAlignment = Alignment.Bottom
                ) { 
                    items(elementosVisualizacion, key = { it.presupuesto.cabecera.idPresupuesto }) { item -> 
                        ElementoBarraSuperpuesta(
                            item = item, 
                            modo = modoAislamiento, 
                            techoVisual = techoVisual, 
                            estaSeleccionado = idPresupuestoSeleccionado == item.presupuesto.cabecera.idPresupuesto, 
                            alSeleccionar = { 
                                Log.d("GRAFICOS_ELITE", "[FOCO] Presupuesto Seleccionado: ${item.presupuesto.cabecera.idPresupuesto}")
                                idPresupuestoSeleccionado = item.presupuesto.cabecera.idPresupuesto 
                            }, 
                            alVerPresupuesto = { alVerPresupuesto(item.presupuesto.cabecera.idPresupuesto) }, 
                            alturaMaximaPx = alturaPx
                        ) 
                    } 
                } 
            }
        }
    }
}

@Composable
fun BotonAislamientoAnalitico(etiqueta: String, icono: ImageVector, color: Color, estaActivo: Boolean, alHacerClick: () -> Unit) {
    Surface(
        onClick = alHacerClick, 
        shape = CircleShape, 
        color = if (estaActivo) color.copy(alpha = 0.15f) else Color.White.copy(0.05f), 
        border = BorderStroke(1.dp, if (estaActivo) color else Color.White.copy(0.1f))
    ) { 
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { 
            Icon(icono, null, tint = color, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(4.dp))
            TextCompacto(etiqueta.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White) 
        } 
    }
}

@Composable
fun BoxScope.EtiquetaPicoAnalitica(texto: String, color: Color, porcentaje: Float, alturaContenedor: Dp) {
    val alphaAnim by animateFloatAsState(targetValue = 1f, animationSpec = tween(600), label = "alpha")
    Box(modifier = Modifier.align(Alignment.BottomStart).offset { IntOffset(x = 10.dp.roundToPx(), y = -((porcentaje * alturaContenedor.toPx()).toInt())) }.graphicsLayer { alpha = alphaAnim }.offset(y = (-4).dp).background(Color(0xFF020408).copy(0.8f), RoundedCornerShape(6.dp)).border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) { TextCompacto(texto, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black) }
}

@Composable
fun ElementoBarraSuperpuesta(
    item: ElementoGraficoPresupuesto, 
    modo: String, 
    techoVisual: Double, 
    estaSeleccionado: Boolean, 
    alSeleccionar: () -> Unit, 
    alVerPresupuesto: () -> Unit, 
    alturaMaximaPx: Float
) {
    val ratioAlturaTotal = (item.total / techoVisual).toFloat().coerceIn(0f, 1f)
    val ratioAlturaMat = (item.materiales / techoVisual).toFloat().coerceIn(0f, 1f)
    val ratioAlturaLab = (item.manoObra / techoVisual).toFloat().coerceIn(0f, 1f)
    val ratioAlturaTax = (item.impuestos / techoVisual).toFloat().coerceIn(0f, 1f)
    
    val hTotal by animateFloatAsState(if(modo == "total") ratioAlturaTotal else 0f, tween(500), label = "hTotal")
    val hMat by animateFloatAsState(if(modo == "total" || modo == "mat") ratioAlturaMat else 0f, tween(500), label = "hMat")
    val hLab by animateFloatAsState(if(modo == "total" || modo == "lab") ratioAlturaLab else 0f, tween(500), label = "hLab")
    val hTax by animateFloatAsState(if(modo == "total" || modo == "tax") ratioAlturaTax else 0f, tween(500), label = "hTax")
    
    val wMat by animateDpAsState(if(modo == "mat") 32.dp else 22.dp, label = "wMat")
    val alphaMat by animateFloatAsState(if(modo == "total" || modo == "mat") 1f else 0f, label = "aMat")
    val wLab by animateDpAsState(if(modo == "lab") 32.dp else 14.dp, label = "wLab")
    val alphaLab by animateFloatAsState(if(modo == "total" || modo == "lab") 1f else 0f, label = "aLab")
    val wTax by animateDpAsState(if(modo == "tax") 32.dp else 6.dp, label = "wTax")
    val alphaTax by animateFloatAsState(if(modo == "total" || modo == "tax") 1f else 0f, label = "aTax")
    
    Box(modifier = Modifier.width(52.dp).fillMaxHeight().background(if (item.esOptimo) VerdeExito.copy(alpha = 0.08f) else Color.Transparent).border(width = if (item.esOptimo) 1.dp else 0.dp, color = if (item.esOptimo) VerdeExito.copy(0.2f) else Color.Transparent).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { alSeleccionar() }.zIndex(if (estaSeleccionado) 100f else 0f), contentAlignment = Alignment.BottomCenter) {
        val avatarFallback = rememberVectorPainter(Icons.Default.Person)
        val escalaAvatar by animateFloatAsState(if(estaSeleccionado) 1.3f else 1f, label = "avatarScale")
        val colorBordeAvatar = if(estaSeleccionado) Color(0xFF2197F5) else Color.White.copy(0.1f)
        
        Box(modifier = Modifier.padding(bottom = 5.dp).zIndex(20f)) { 
            AsyncImage(model = item.fotoPrestadorAlternativo ?: item.presupuesto.cabecera.urlFotoPrestador, contentDescription = null, fallback = avatarFallback, error = avatarFallback, modifier = Modifier.size(22.dp).graphicsLayer { scaleX = escalaAvatar; scaleY = escalaAvatar }.clip(CircleShape).border(2.dp, colorBordeAvatar, CircleShape).background(Color(0xFF161C24)), contentScale = ContentScale.Crop)
            if (item.esIrrisorio) { TextCompacto("⚠️", fontSize = 14.sp, modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp)) } 
        }
        
        val alphaBarra = if (item.esIrrisorio) 0.3f else if (estaSeleccionado) 1f else 0.8f
        Box(modifier = Modifier.padding(bottom = 35.dp).fillMaxSize().alpha(alphaBarra), contentAlignment = Alignment.BottomCenter) {
            Box(modifier = Modifier.width(32.dp).fillMaxHeight(hTotal).background(if(estaSeleccionado) Color.White.copy(0.15f) else Color.White.copy(0.03f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).zIndex(1f))
            Box(modifier = Modifier.width(wMat).fillMaxHeight(hMat).alpha(alphaMat).background(ColorMaterial, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(modo == "mat") 10f else 2f))
            Box(modifier = Modifier.width(wLab).fillMaxHeight(hLab).alpha(alphaLab).background(ColorManoObra, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(modo == "lab") 10f else 3f))
            Box(modifier = Modifier.width(wTax).fillMaxHeight(hTax).alpha(alphaTax).background(ColorImpuesto, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).shadow(10.dp).zIndex(if(modo == "tax") 10f else 4f))
        }
        
        if (estaSeleccionado) {
            val densidad = LocalDensity.current
            val rellenoInferiorPx = with(densidad) { 35.dp.toPx() }
            val hoverExtraPx = with(densidad) { 20.dp.toPx() }
            val alturaEstimadaPopupPx = with(densidad) { 200.dp.toPx() }
            val yProyectada = -((hTotal * (alturaMaximaPx - rellenoInferiorPx)) + rellenoInferiorPx + hoverExtraPx)
            val yMaxSegura = -(alturaMaximaPx - alturaEstimadaPopupPx)
            val yOffsetFinal = yProyectada.toFloat().coerceAtLeast(yMaxSegura).toInt()
            
            Popup(alignment = Alignment.BottomCenter, offset = IntOffset(0, yOffsetFinal), properties = PopupProperties(clippingEnabled = false, excludeFromSystemGesture = true)) {
                var animarEntrada by remember { mutableStateOf(false) }; LaunchedEffect(Unit) { animarEntrada = true }
                AnimatedVisibility(visible = animarEntrada, enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 1f)), exit = fadeOut()) {
                    Surface(modifier = Modifier.width(170.dp), color = Color(0xFF0A0E14).copy(alpha = 0.95f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFF2197F5).copy(0.4f)), shadowElevation = 25.dp) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, Color.White.copy(0.1f)), RoundedCornerShape(6.dp)).padding(bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { TextCompacto("Total Final:", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold); TextCompacto("$${(item.total/1000).toInt()}k", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black) }
                            Spacer(Modifier.height(4.dp)); Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) { TextCompacto("Material:", color = ColorMaterial, fontSize = 8.sp); TextCompacto("$${(item.materiales/1000).toInt()}k", color = ColorMaterial, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) { TextCompacto("M. Obra:", color = ColorManoObra, fontSize = 8.sp); TextCompacto("$${(item.manoObra/1000).toInt()}k", color = ColorManoObra, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) { TextCompacto("Tasas:", color = ColorImpuesto, fontSize = 8.sp); TextCompacto("$${(item.impuestos/1000).toInt()}k", color = ColorImpuesto, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.height(6.dp)); if (item.esOptimo) { TextCompacto("ZONA ÓPTIMA", modifier = Modifier.fillMaxWidth().background(VerdeExito.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, VerdeExito.copy(0.2f), RoundedCornerShape(4.dp)).padding(vertical = 2.dp), textAlign = TextAlign.Center, color = VerdeExito, fontSize = 8.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.sp)) }; if (item.esIrrisorio) { TextCompacto("⚠️ ANOMALÍA", modifier = Modifier.fillMaxWidth().background(Color.Red.copy(0.1f), RoundedCornerShape(4.dp)).border(1.dp, Color.Red.copy(0.2f), RoundedCornerShape(4.dp)).padding(vertical = 2.dp), textAlign = TextAlign.Center, color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Black, style = TextStyle(letterSpacing = 1.sp)) }
                            Spacer(Modifier.height(8.dp)); HorizontalDivider(color = Color.White.copy(0.1f)); Spacer(Modifier.height(6.dp)); TextCompacto(item.nombrePrestadorAlternativo ?: item.presupuesto.cabecera.nombrePrestador, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()); TextCompacto(item.presupuesto.cabecera.nombreEmpresaPrestador ?: "Profesional Independiente", color = Color(0xFF22D3EE), fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp)); Button(onClick = alVerPresupuesto, modifier = Modifier.fillMaxWidth().height(32.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) { TextCompacto("DEEP DIVE", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White, style = TextStyle(letterSpacing = 0.5.sp)) }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF020408)
@Composable
private fun PreviewGraficoComparativoHorizontal() {
    val mockBudget = PresupuestoFinalEntity(
        idPresupuesto = "1",
        nombrePrestador = "Maverick Hunter",
        totalGeneral = 42000.0,
        estado = EstadoPresupuesto.ACEPTADO
    )
    val mockElement = ElementoGraficoPresupuesto(
        presupuesto = PresupuestoConItems(
            cabecera = mockBudget,
            lineas = emptyList(),
            finanzas = emptyList()
        ),
        total = 42000.0,
        materiales = 20000.0,
        manoObra = 15000.0,
        impuestos = 7000.0,
        descuentos = 0.0,
        esIrrisorio = false,
        esOptimo = true
    )
    val mockState = EstadoAnaliticaMercado(
        elementos = listOf(mockElement),
        promedioTotal = 45000.0,
        precioMinimo = 42000.0,
        precioMaximo = 50000.0,
        conteoValidos = 1,
        estaAnalizando = false
    )

    ClienteTheme {
        GraficoComparativoHorizontal(
            estadoMercado = mockState,
            alCerrar = {},
            alVerPresupuesto = {}
        )
    }
}


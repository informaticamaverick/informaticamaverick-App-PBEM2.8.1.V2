package com.example.myapplication.prestador.ui.pantallas.presupuesto.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.ui.pantallas.presupuesto.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PrePresupuestoConfigViewModel
import kotlinx.coroutines.delay

/**
 * --- COMPONENTES DE PRESUPUESTO MAVERICK (V2026.12) ---
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoTextoCompacto(
    valor: String,
    onValorCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiqueta: @Composable (() -> Unit)? = null,
    sugerencia: @Composable (() -> Unit)? = null,
    opcionesTeclado: KeyboardOptions = KeyboardOptions.Default,
    estiloTexto: TextStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
) {
    val colores = getPrestadorColors()
    val fuenteInteraccion = remember { MutableInteractionSource() }
    BasicTextField(
        value = valor,
        onValueChange = onValorCambio,
        modifier = modifier,
        textStyle = estiloTexto.copy(color = colores.textPrimary),
        keyboardOptions = opcionesTeclado,
        singleLine = true,
        interactionSource = fuenteInteraccion,
        decorationBox = { campoInterno ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = valor,
                innerTextField = campoInterno,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = fuenteInteraccion,
                label = etiqueta,
                placeholder = sugerencia,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colores.primaryOrange,
                    unfocusedBorderColor = colores.border,
                    focusedLabelColor = colores.primaryOrange,
                    unfocusedLabelColor = colores.textSecondary,
                    cursorColor = colores.primaryOrange,
                    focusedTextColor = colores.textPrimary,
                    unfocusedTextColor = colores.textPrimary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
                        isError = false,
                        interactionSource = fuenteInteraccion,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B35),
                            unfocusedBorderColor = Color(0xFFD1D5DB)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            )
        }
    )
}

@Composable
fun PastillaEstado(estado: EstadoPresupuesto) {
    val (color, etiqueta) = when (estado) {
        EstadoPresupuesto.PENDIENTE -> Color(0xFFF59E0B) to "Pendiente"
        EstadoPresupuesto.ACEPTADO -> Color(0xFF10B981) to "Aceptado"
        EstadoPresupuesto.RECHAZADO -> Color(0xFFEF4444) to "Rechazado"
        EstadoPresupuesto.PAGADO -> Color(0xFF3B82F6) to "Pagado"
        EstadoPresupuesto.VENCIDO -> Color(0xFF6B7280) to "Vencido"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            etiqueta,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FilaDetalleArticulo(item: ArticuloPresupuesto, itemsSugerencia: List<ArticuloPresupuesto> = emptyList(), onActualizar: (ArticuloPresupuesto) -> Unit) {
    val colores = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val base = item.precioUnitario * item.cantidad
    val total = base + (base * item.porcentajeImpuesto / 100) - (base * item.porcentajeDescuento / 100)

    var mostrarSugerencias by remember { mutableStateOf(false) }
    val terminoBusqueda = if (item.codigo.length >= 2) item.codigo else item.descripcion
    val filtrados = if (mostrarSugerencias && terminoBusqueda.length >= 2 && itemsSugerencia.isNotEmpty()) {
        itemsSugerencia.filter { it.descripcion.contains(terminoBusqueda, ignoreCase = true) || it.codigo.contains(terminoBusqueda, ignoreCase = true) }.take(5)
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(modifier = Modifier.weight(0.25f)) {
                Text("CÓDIGO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = item.codigo, onValorCambio = { onActualizar(item.copy(codigo = it)); mostrarSugerencias = it.isNotBlank() }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            }
            Column(modifier = Modifier.weight(0.75f)) {
                Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = item.descripcion, onValorCambio = { onActualizar(item.copy(descripcion = it)); mostrarSugerencias = it.isNotBlank() }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(imeAction = ImeAction.Next))
            }
        }

        if (mostrarSugerencias && filtrados.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F5)), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(8.dp)) {
                Column {
                    filtrados.forEachIndexed { index, sugerencia ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onActualizar(sugerencia.copy(id = item.id)); mostrarSugerencias = false }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (sugerencia.codigo.isNotBlank()) Text(sugerencia.codigo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colores.primaryOrange)
                                    Text(sugerencia.descripcion, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = colores.textPrimary)
                                }
                                Text("Cant: ${sugerencia.cantidad}  •  $simboloMoneda${"%.2f".format(sugerencia.precioUnitario)}", style = MaterialTheme.typography.bodySmall, color = colores.textSecondary)
                            }
                            Icon(Icons.Default.NorthWest, null, tint = colores.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        if (index < filtrados.size - 1) HorizontalDivider(color = colores.border)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CANT.", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = if (item.cantidad == 0) "" else item.cantidad.toString(), onValorCambio = { if (it.all { c -> c.isDigit() }) onActualizar(item.copy(cantidad = it.toIntOrNull() ?: 0)) }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("P. UNIT.", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = if (item.precioUnitario == 0.0) "" else item.precioUnitario.toString(), onValorCambio = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) onActualizar(item.copy(precioUnitario = it.toDoubleOrNull() ?: 0.0)) }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
            }
            Text("Total: $simboloMoneda${"%.2f".format(total)}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = colores.primaryOrange)
        }
    }
}

@Composable
fun FilaDetalleServicio(servicio: ServicioPresupuesto, itemsSugerencia: List<ServicioPresupuesto> = emptyList(), onActualizar: (ServicioPresupuesto) -> Unit) {
    val colores = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    var mostrarSugerencias by remember { mutableStateOf(false) }
    val terminoBusqueda = if (servicio.codigo.length >= 2) servicio.codigo else servicio.descripcion
    val filtrados = if (mostrarSugerencias && terminoBusqueda.length >= 2 && itemsSugerencia.isNotEmpty()) {
        itemsSugerencia.filter { it.descripcion.contains(terminoBusqueda, ignoreCase = true) || it.codigo.contains(terminoBusqueda, ignoreCase = true) }.take(5)
    } else emptyList()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(modifier = Modifier.weight(0.25f)) {
                Text("CÓDIGO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = servicio.codigo, onValorCambio = { onActualizar(servicio.copy(codigo = it)); mostrarSugerencias = it.isNotBlank() }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(imeAction = ImeAction.Next))
            }
            Column(modifier = Modifier.weight(0.75f)) {
                Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = servicio.descripcion, onValorCambio = { onActualizar(servicio.copy(descripcion = it)); mostrarSugerencias = it.isNotBlank() }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(imeAction = ImeAction.Next))
            }
        }

        if (mostrarSugerencias && filtrados.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)), elevation = CardDefaults.cardElevation(4.dp), shape = RoundedCornerShape(8.dp)) {
                Column {
                    filtrados.forEachIndexed { index, sugerencia ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onActualizar(sugerencia.copy(id = servicio.id)); mostrarSugerencias = false }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (sugerencia.codigo.isNotBlank()) Text(sugerencia.codigo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colores.primaryOrange)
                                    Text(sugerencia.descripcion, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, color = colores.textPrimary)
                                }
                                Text("$simboloMoneda${"%.2f".format(sugerencia.total)}", style = MaterialTheme.typography.bodySmall, color = colores.textSecondary)
                            }
                            Icon(Icons.Default.NorthWest, null, tint = colores.primaryOrange, modifier = Modifier.size(16.dp))
                        }
                        if (index < filtrados.size - 1) HorizontalDivider(color = colores.border)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("TOTAL ($)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colores.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = if (servicio.total == 0.0) "" else servicio.total.toString(), onValorCambio = { onActualizar(servicio.copy(total = it.toDoubleOrNull() ?: 0.0)) }, modifier = Modifier.fillMaxWidth().height(40.dp), opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
            }
        }
    }
}

@Composable
fun FilaResumenArticulo(modifier: Modifier = Modifier, item: ArticuloPresupuesto, onEditar: (() -> Unit)? = null, onEliminar: (() -> Unit)? = null) {
    val colores = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val base = item.precioUnitario * item.cantidad
    val total = base + (base * item.porcentajeImpuesto / 100) - (base * item.porcentajeDescuento / 100)
    Row(modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).shadow(2.dp, RoundedCornerShape(8.dp)).background(colores.surfaceColor), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(colores.primaryOrange))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.descripcion.ifBlank { "(Sin desc.)" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall, color = colores.textPrimary, maxLines = 1)
            Text("${item.cantidad} × $simboloMoneda${"%.2f".format(item.precioUnitario)}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = colores.textSecondary)
        }
        Text("$simboloMoneda${"%.2f".format(total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colores.primaryOrange)
        if (onEditar != null || onEliminar != null) {
            IconButton(onClick = { onEditar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, null, tint = colores.primaryOrange, modifier = Modifier.size(14.dp)) }
            IconButton(onClick = { onEliminar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp)) }
        }
    }
}

@Composable
fun FilaResumenServicio(modifier: Modifier = Modifier, servicio: ServicioPresupuesto, onEditar: (() -> Unit)? = null, onEliminar: (() -> Unit)? = null) {
    val colores = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val colorAcento = Color(0xFF3B82F6)
    Row(modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).shadow(2.dp, RoundedCornerShape(8.dp)).background(colores.surfaceColor), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(colorAcento))
        Spacer(modifier = Modifier.width(8.dp))
        Text(servicio.descripcion.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall, color = colores.textPrimary, maxLines = 1)
        Text("$simboloMoneda${"%.2f".format(servicio.total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colorAcento)
        if (onEditar != null || onEliminar != null) {
            IconButton(onClick = { onEditar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, null, tint = colores.primaryOrange, modifier = Modifier.size(14.dp)) }
            IconButton(onClick = { onEliminar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp)) }
        }
    }
}



@Composable
fun FilaResumenImpuesto(modifier: Modifier = Modifier, impuesto: ImpuestoPresupuesto, onEditar: (() -> Unit)? = null, onEliminar: (() -> Unit)? = null) {
    val colores = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val colorAcento = Color(0xFFEF4444)
    Row(modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).shadow(2.dp, RoundedCornerShape(8.dp)).background(colores.surfaceColor), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(colorAcento))
        Spacer(modifier = Modifier.width(8.dp))
        Text(impuesto.descripcion.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall, color = colores.textPrimary, maxLines = 1)
        Text("$simboloMoneda${"%.2f".format(impuesto.monto)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colorAcento)
        if (onEditar != null || onEliminar != null) {
            IconButton(onClick = { onEditar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, null, tint = colores.primaryOrange, modifier = Modifier.size(14.dp)) }
            IconButton(onClick = { onEliminar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp)) }
        }
    }
}

@Composable
fun FilaResumenGasto(modifier: Modifier = Modifier, gasto: GastoVarioPresupuesto, onEditar: (() -> Unit)? = null, onEliminar: (() -> Unit)? = null) {
    val colores = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val colorAcento = Color(0xFF10B981)
    Row(modifier = modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).shadow(2.dp, RoundedCornerShape(8.dp)).background(colores.surfaceColor), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(colorAcento))
        Spacer(modifier = Modifier.width(8.dp))
        Text(gasto.descripcion.ifBlank { "(Sin descripción)" }, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall, color = colores.textPrimary, maxLines = 1)
        Text("$simboloMoneda${"%.2f".format(gasto.monto)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = colorAcento)
        if (onEditar != null || onEliminar != null) {
            IconButton(onClick = { onEditar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, null, tint = colores.primaryOrange, modifier = Modifier.size(14.dp)) }
            IconButton(onClick = { onEliminar?.invoke() }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp)) }
        }
    }
}

@Composable
fun EncabezadoPrestador(prestador: PrestadorDominio, numeroPresupuesto: String, alFiltrar: () -> Unit) {
    val fechaActual = remember { 
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es"))
        sdf.format(java.util.Date())
    }
    val colores = getPrestadorColors()
    val gradiente = Brush.linearGradient(colors = listOf(Color(0xFFFF6B35), Color(0xFFE64A19)))
    val nombreCompleto = prestador.titulo
    val iniciales = nombreCompleto.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercaseChar().toString() }
    Box(modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).background(gradiente).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) { Text(iniciales, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.Start) {
                Text(nombreCompleto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("# $numeroPresupuesto", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    Text("•", color = Color.White.copy(alpha = .6f), style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
                    Text(fechaActual, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
fun <T> SeccionColapsable(
    titulo: String,
    items: List<T>,
    totalSeccion: Double,
    estaExpandida: Boolean,
    alAlternar: () -> Unit,
    alAgregar: () -> Unit,
    ranuraAgregarRapido: (@Composable () -> Unit)? = null,
    mostrarContenidoBase: Boolean = true,
    contenidoItem: @Composable (item: T, index: Int) -> Unit
) {
    val colores = getPrestadorColors()
    val anguloRotacion by animateFloatAsState(targetValue = if (estaExpandida) 180f else 0f, label = "arrowRotation")
    Box(modifier = Modifier.padding(top = 18.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colores.surfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = alAlternar)) {
                    Badge(containerColor = colores.primaryOrange, contentColor = Color.White) { Text(text = "${items.size}", modifier = Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colores.textPrimary)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = if (estaExpandida) "Colapsar" else "Expandir", modifier = Modifier.rotate(anguloRotacion), tint = colores.textPrimary)
                    Spacer(modifier = Modifier.weight(1f))
                }
                AnimatedVisibility(visible = estaExpandida) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colores.border)
                        if (ranuraAgregarRapido != null) { ranuraAgregarRapido(); Spacer(modifier = Modifier.height(8.dp)) }
                        if (mostrarContenidoBase) {
                            if (items.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).border(width = 1.dp, color = colores.border, shape = RoundedCornerShape(8.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.AddCircleOutline, null, tint = colores.textSecondary, modifier = Modifier.size(16.dp))
                                        Text("Sin ítems. Tocá + para agregar.", color = colores.textSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                    }
                                }
                            } else {
                                Column {
                                    items.forEachIndexed { index, item ->
                                        contenidoItem(item, index)
                                        if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = colores.border)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        SmallFloatingActionButton(onClick = alAgregar, modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-18).dp), shape = CircleShape, containerColor = colores.primaryOrange, contentColor = Color.White) { Icon(Icons.Default.Add, null) }
        if (items.isNotEmpty()) {
            Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 12.dp).shadow(4.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(colors = listOf(Color(0xFFFF8C42), Color(0xFFFF4500)))).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(text = "Subtotal: \$${"%.2f".format(totalSeccion)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CamposAutoCompletadoArticulo(
    sugerencias: List<ArticuloPresupuesto>,
    alAgregar: (ArticuloPresupuesto) -> Unit,
    items: List<ArticuloPresupuesto> = emptyList(),
    alEditar: (ArticuloPresupuesto) -> Unit = {},
    alEliminar: (Int) -> Unit = {}
) {
    val colores = getPrestadorColors()
    var textoCodigo by remember { mutableStateOf("") }
    var codigoExpandido by remember { mutableStateOf(false) }
    val solicitanteVista = remember { BringIntoViewRequester() }
    var campoEnFoco by remember { mutableStateOf(false) }
    LaunchedEffect(campoEnFoco) { if (campoEnFoco) { delay(300); solicitanteVista.bringIntoView() } }
    val filtrados = remember(textoCodigo, sugerencias) { if (textoCodigo.isBlank()) emptyList() else sugerencias.filter { it.codigo.contains(textoCodigo, ignoreCase = true) || it.descripcion.contains(textoCodigo, ignoreCase = true) } }

    Row(modifier = Modifier.fillMaxWidth().bringIntoViewRequester(solicitanteVista), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        ExposedDropdownMenuBox(expanded = codigoExpandido && filtrados.isNotEmpty(), onExpandedChange = { codigoExpandido = it }, modifier = Modifier.weight(0.22f)) {
            CampoTextoCompacto(valor = textoCodigo, onValorCambio = { textoCodigo = it; codigoExpandido = true }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth().height(40.dp).onFocusChanged { campoEnFoco = it.isFocused }, etiqueta = { Text("Buscar", style = MaterialTheme.typography.labelSmall) }, sugerencia = { Text("Cód. o desc.", style = MaterialTheme.typography.labelSmall) })
            ExposedDropdownMenu(expanded = codigoExpandido && filtrados.isNotEmpty(), onDismissRequest = { codigoExpandido = false }, modifier = Modifier.widthIn(min = 200.dp)) {
                filtrados.take(5).forEach { item ->
                    DropdownMenuItem(text = { FilaSugerenciaCodigo(codigo = item.codigo, descripcion = item.descripcion, icono = Icons.Default.Inventory2) }, onClick = { alAgregar(item.copy(id = System.currentTimeMillis())); textoCodigo = ""; codigoExpandido = false }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
        Column(modifier = Modifier.weight(0.78f)) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).padding(vertical = 12.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) { Text("Sin ítems. Buscá arriba.", color = colores.textSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
            } else {
                items.forEachIndexed { index, item ->
                    FilaResumenArticulo(item = item, onEditar = { alEditar(item) }, onEliminar = { alEliminar(index) })
                    if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CamposAutoCompletadoServicio(
    sugerencias: List<ServicioPresupuesto>,
    alAgregar: (ServicioPresupuesto) -> Unit,
    items: List<ServicioPresupuesto> = emptyList(),
    alEditar: (ServicioPresupuesto) -> Unit = {},
    alEliminar: (Int) -> Unit = {}
) {
    val colores = getPrestadorColors()
    var textoCodigo by remember { mutableStateOf("") }
    var codigoExpandido by remember { mutableStateOf(false) }
    val solicitanteVista = remember { BringIntoViewRequester() }
    var campoEnFoco by remember { mutableStateOf(false) }
    LaunchedEffect(campoEnFoco) { if (campoEnFoco) { delay(300); solicitanteVista.bringIntoView() } }
    val filtrados = remember(textoCodigo, sugerencias) { if (textoCodigo.isBlank()) emptyList() else sugerencias.filter { it.codigo.contains(textoCodigo, ignoreCase = true) || it.descripcion.contains(textoCodigo, ignoreCase = true) } }

    Row(modifier = Modifier.fillMaxWidth().bringIntoViewRequester(solicitanteVista), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        ExposedDropdownMenuBox(expanded = codigoExpandido && filtrados.isNotEmpty(), onExpandedChange = { codigoExpandido = it }, modifier = Modifier.weight(0.22f)) {
            CampoTextoCompacto(valor = textoCodigo, onValorCambio = { textoCodigo = it; codigoExpandido = true }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth().height(40.dp).onFocusChanged { campoEnFoco = it.isFocused }, etiqueta = { Text("Buscar", style = MaterialTheme.typography.labelSmall) }, sugerencia = { Text("Cód. o desc.", style = MaterialTheme.typography.labelSmall) })
            ExposedDropdownMenu(expanded = codigoExpandido && filtrados.isNotEmpty(), onDismissRequest = { codigoExpandido = false }, modifier = Modifier.widthIn(min = 200.dp)) {
                filtrados.take(5).forEach { item ->
                    DropdownMenuItem(text = { FilaSugerenciaCodigo(codigo = item.codigo, descripcion = item.descripcion, icono = Icons.Default.Build) }, onClick = { alAgregar(item.copy(id = System.currentTimeMillis())); textoCodigo = ""; codigoExpandido = false }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
        Column(modifier = Modifier.weight(0.78f)) {
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).padding(vertical = 12.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) { Text("Sin ítems. Buscá arriba.", color = colores.textSecondary, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
            } else {
                items.forEachIndexed { index, item ->
                    FilaResumenServicio(servicio = item, onEditar = { alEditar(item) }, onEliminar = { alEliminar(index) })
                    if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CampoAutoCompletadoDescripcion(
    etiqueta: String,
    sugerencias: List<String>,
    alSeleccionar: (String) -> Unit
) {
    val colores = getPrestadorColors()
    var texto by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }
    val solicitanteVista = remember { BringIntoViewRequester() }
    var campoEnFoco by remember { mutableStateOf(false) }
    LaunchedEffect(campoEnFoco) { if (campoEnFoco) { delay(300); solicitanteVista.bringIntoView() } }
    val filtrados = remember(texto, sugerencias) { if (texto.isBlank()) emptyList() else sugerencias.filter { it.contains(texto, ignoreCase = true) } }
    Box(modifier = Modifier.fillMaxWidth().bringIntoViewRequester(solicitanteVista)) {
    ExposedDropdownMenuBox(expanded = expandido && filtrados.isNotEmpty(), onExpandedChange = { expandido = it }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = texto, onValueChange = { texto = it; expandido = true },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth().onFocusChanged { campoEnFoco = it.isFocused },
            label = { Text(etiqueta, style = MaterialTheme.typography.labelSmall) }, singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            trailingIcon = if (texto.isNotEmpty()) { { IconButton(onClick = { alSeleccionar(texto); texto = "" }) { Icon(Icons.Default.AddCircle, null, tint = colores.primaryOrange) } } } else null,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colores.primaryOrange, cursorColor = colores.primaryOrange),
            shape = RoundedCornerShape(8.dp))
        ExposedDropdownMenu(expanded = expandido && filtrados.isNotEmpty(), onDismissRequest = { expandido = false }) {
            filtrados.take(5).forEach { desc ->
                DropdownMenuItem(text = { Text(desc, style = MaterialTheme.typography.bodySmall, color = colores.textPrimary) },
                    onClick = { alSeleccionar(desc); texto = ""; expandido = false })
            }
        }
    }
    }
}

@Composable
private fun FilaSugerenciaCodigo(
    codigo: String,
    descripcion: String = "",
    icono: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Tag
) {
    val colores = getPrestadorColors()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(colores.primaryOrange.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Icon(icono, null, tint = colores.primaryOrange, modifier = Modifier.size(18.dp)) }
        Column {
            Text(codigo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colores.textPrimary)
            if (descripcion.isNotEmpty()) Text(descripcion, fontSize = 11.sp, color = colores.textSecondary, maxLines = 1)
        }
    }
}


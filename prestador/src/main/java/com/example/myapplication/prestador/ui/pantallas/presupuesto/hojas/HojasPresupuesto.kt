package com.example.myapplication.prestador.ui.pantallas.presupuesto.hojas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.presupuesto.PrePresupuestoConfigViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.prestador.ui.pantallas.presupuesto.componentes.*

/**
 * --- ENUM DE NAVEGACIÓN DE HOJAS (v2026.ELITE) ---
 */
enum class TipoHojaPresupuesto { Articulo, Servicio, Honorario, Gasto, Impuesto, Secciones, SelectorCliente }

@Composable
fun HojaAgregarArticulo(
    itemAEditar: ArticuloPresupuesto?,
    itemsSugerencia: List<ArticuloPresupuesto> = emptyList(),
    itemsActuales: List<ArticuloPresupuesto> = emptyList(),
    onAgregarItem: (ArticuloPresupuesto) -> Unit,
    onActualizarItem: (ArticuloPresupuesto) -> Unit,
    onEliminarItemActual: ((Int) -> Unit)? = null,
    onGuardarEnCatalogo: ((ArticuloPresupuesto) -> Unit)? = null,
    onCompletar: () -> Unit = {},
    onDescartar: () -> Unit = {}
){
    val colors = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val esModoEdicion = itemAEditar != null
    var itemActual by remember { mutableStateOf(itemAEditar ?: ArticuloPresupuesto(descripcion = "", cantidad = 1, precioUnitario = 0.0)) }

    val montoBase = itemActual.cantidad * itemActual.precioUnitario
    val valorImpuesto = montoBase * (itemActual.porcentajeImpuesto / 100)
    val baseConImpuesto = montoBase + valorImpuesto

    var precioCostoStr by remember { mutableStateOf(if (itemActual.precioCosto > 0) itemActual.precioCosto.toString() else "") }
    var porcentajeImpuestoStr by remember { mutableStateOf(if (itemActual.porcentajeImpuesto > 0) itemActual.porcentajeImpuesto.toString() else "") }
    var montoImpuestoStr by remember { mutableStateOf(if (itemActual.porcentajeImpuesto > 0) "%.2f".format(valorImpuesto) else "") }
    var porcentajeDescuentoStr by remember { mutableStateOf(if (itemActual.porcentajeDescuento > 0) itemActual.porcentajeDescuento.toString() else "") }
    var montoDescuentoStr by remember { mutableStateOf(if (itemActual.porcentajeDescuento > 0) "%.2f".format(baseConImpuesto * itemActual.porcentajeDescuento / 100) else "") }
    var itemPendienteParaGuardar by remember { mutableStateOf<ArticuloPresupuesto?>(null) }

    // 🔥 [ELITE] Sincronizar estados locales cuando cambia el item (ej: desde sugerencias)
    LaunchedEffect(itemActual.idProducto, itemActual.precioUnitario, itemActual.porcentajeImpuesto, itemActual.porcentajeDescuento) {
        if (itemActual.porcentajeImpuesto > 0) {
            porcentajeImpuestoStr = itemActual.porcentajeImpuesto.toString()
            montoImpuestoStr = "%.2f".format(itemActual.cantidad * itemActual.precioUnitario * itemActual.porcentajeImpuesto / 100)
        }
        if (itemActual.porcentajeDescuento > 0) {
            porcentajeDescuentoStr = itemActual.porcentajeDescuento.toString()
            val baseConImp = (itemActual.cantidad * itemActual.precioUnitario) * (1 + itemActual.porcentajeImpuesto / 100)
            montoDescuentoStr = "%.2f".format(baseConImp * itemActual.porcentajeDescuento / 100)
        }
    }

    LaunchedEffect(montoBase) {
         if (montoBase > 0) {
            val pImp = porcentajeImpuestoStr.toDoubleOrNull() ?: 0.0
            if (pImp > 0) montoImpuestoStr = "%.2f".format(montoBase * pImp / 100)
            val pDesc = porcentajeDescuentoStr.toDoubleOrNull() ?: 0.0
            val impAct = montoBase * (pImp / 100)
            val baseActConImp = montoBase + impAct
            if (pDesc > 0) montoDescuentoStr = "%.2f".format(baseActConImp * pDesc / 100)
        }
    }

    Column(modifier = Modifier.fillMaxWidth().background(colors.backgroundColor).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Inventory2, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(if (esModoEdicion) "Editar Artículo" else "Nuevo Artículo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDescartar) { Icon(Icons.Default.Close, "Cerrar", tint = colors.textSecondary) }
        }

        FilaDetalleArticulo(item = itemActual, itemsSugerencia = itemsSugerencia, onActualizar = { 
            itemActual = it 
            if (it.precioCosto > 0) precioCostoStr = it.precioCosto.toString()
        })

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 [NEW v2026.ELITE]: Gestión de Margen e Imagen
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .clickable { /* TODO: Selector de Imagen */ },
                contentAlignment = Alignment.Center
            ) {
                if (itemActual.urlImagen != null) {
                    AsyncImage(model = itemActual.urlImagen, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("COSTO DE ADQUISICIÓN", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                CampoTextoCompacto(
                    valor = precioCostoStr,
                    onValorCambio = {
                        precioCostoStr = it
                        itemActual = itemActual.copy(precioCosto = it.toDoubleOrNull() ?: 0.0)
                    },
                    etiqueta = { Text("Precio de Costo ($)") },
                    opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.border)
        Text("Impuestos y Descuentos", style = MaterialTheme.typography.titleSmall, color = colors.primaryOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CampoTextoCompacto(
                valor = porcentajeImpuestoStr,
                onValorCambio = {
                    porcentajeImpuestoStr = it
                    val p = it.toDoubleOrNull()
                    if (p != null && montoBase > 0) {
                         val montoImp = montoBase * p / 100
                         montoImpuestoStr = "%.2f".format(montoImp)
                         itemActual = itemActual.copy(porcentajeImpuesto = p)
                         val nuevaBaseConImp = montoBase + montoImp
                         val pDescAct = porcentajeDescuentoStr.toDoubleOrNull() ?: 0.0
                         if (pDescAct > 0) montoDescuentoStr = "%.2f".format(nuevaBaseConImp * pDescAct / 100)
                    } else if (it.isEmpty()) {
                        montoImpuestoStr = ""
                        itemActual = itemActual.copy(porcentajeImpuesto = 0.0)
                    }
                },
                etiqueta = { Text("Imp. (%)", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f),
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
            CampoTextoCompacto(
                valor = montoImpuestoStr,
                onValorCambio = {
                    montoImpuestoStr = it
                    val a = it.toDoubleOrNull()
                    if (a != null && montoBase > 0) {
                        val p = (a / montoBase) * 100
                        porcentajeImpuestoStr = "%.2f".format(p)
                        itemActual = itemActual.copy(porcentajeImpuesto = p)
                         val nuevaBaseConImp = montoBase + a
                         val pDescAct = porcentajeDescuentoStr.toDoubleOrNull() ?: 0.0
                         if (pDescAct > 0) montoDescuentoStr = "%.2f".format(nuevaBaseConImp * pDescAct / 100)
                    } else if (it.isEmpty()) {
                        porcentajeImpuestoStr = ""
                        itemActual = itemActual.copy(porcentajeImpuesto = 0.0)
                    }
                },
                etiqueta = { Text("Imp. ($)", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f),
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CampoTextoCompacto(
                valor = porcentajeDescuentoStr,
                onValorCambio = {
                    porcentajeDescuentoStr = it
                    val p = it.toDoubleOrNull()
                    if (p != null && baseConImpuesto > 0) {
                         montoDescuentoStr = "%.2f".format(baseConImpuesto * p / 100)
                         itemActual = itemActual.copy(porcentajeDescuento = p)
                    } else if (it.isEmpty()) {
                        montoDescuentoStr = ""
                        itemActual = itemActual.copy(porcentajeDescuento = 0.0)
                    }
                },
                etiqueta = { Text("Desc. (%)", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f),
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
            CampoTextoCompacto(
                valor = montoDescuentoStr,
                onValorCambio = {
                    montoDescuentoStr = it
                    val a = it.toDoubleOrNull()
                    if (a != null && baseConImpuesto > 0) {
                        val p = (a / baseConImpuesto) * 100
                        porcentajeDescuentoStr = "%.2f".format(p)
                        itemActual = itemActual.copy(porcentajeDescuento = p)
                    } else if (it.isEmpty()) {
                        porcentajeDescuentoStr = ""
                        itemActual = itemActual.copy(porcentajeDescuento = 0.0)
                    }
                },
                etiqueta = { Text("Desc. ($)", style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.weight(1f),
                opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (esModoEdicion) onActualizarItem(itemActual)
                else {
                    val agregado = itemActual
                    onAgregarItem(agregado)
                    if (onGuardarEnCatalogo != null && itemsSugerencia.none { it.descripcion.equals(agregado.descripcion, ignoreCase = true) }) {
                        itemPendienteParaGuardar = agregado
                    }
                    itemActual = ArticuloPresupuesto()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = itemActual.descripcion.isNotBlank() && itemActual.precioUnitario > 0 && itemActual.cantidad > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
        ) {
            Text(if (esModoEdicion) "Guardar Cambios" else "Agregar Artículo", fontWeight = FontWeight.Bold)
        }

        if (itemsActuales.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            Text("Artículos en este presupuesto (${itemsActuales.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
            itemsActuales.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(buildString { if (item.codigo.isNotBlank()) append("[${item.codigo}] "); append(item.descripcion) }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        Text("${item.cantidad} u.  •  $simboloMoneda${"%.2f".format(item.precioUnitario)}  =  $simboloMoneda${"%.2f".format(item.cantidad * item.precioUnitario)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    }
                    IconButton(onClick = { itemActual = item }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = { onEliminarItemActual?.invoke(index) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) }
                }
            }
        }

        itemPendienteParaGuardar?.let { nuevoArt ->
            AlertDialog(
                onDismissRequest = { itemPendienteParaGuardar = null; onCompletar() },
                title = { Text("Artículo nuevo", fontWeight = FontWeight.Bold) },
                text = { Text("\"${nuevoArt.descripcion}\" no está en tu lista.\n¿Deseas guardarlo para usarlo en futuros presupuestos?") },
                confirmButton = {
                    TextButton(onClick = { onGuardarEnCatalogo?.invoke(nuevoArt); itemPendienteParaGuardar = null; onCompletar() }) {
                        Text("Guardar", color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { itemPendienteParaGuardar = null; onCompletar() }) { Text("No, gracias") } }
            )
        }
    }
}

@Composable
fun HojaAgregarServicio(
    itemAEditar: ServicioPresupuesto?,
    onAgregarItem: (ServicioPresupuesto) -> Unit,
    onActualizarItem: (ServicioPresupuesto) -> Unit,
    itemsActuales: List<ServicioPresupuesto> = emptyList(),
    onEliminarItemActual: ((Int) -> Unit)? = null,
    itemsSugerencia: List<ServicioPresupuesto> = emptyList(),
    onGuardarEnCatalogo: ((ServicioPresupuesto) -> Unit)? = null,
    onCompletar: () -> Unit = {},
    onDescartar: () -> Unit = {}
){
    val colors = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val esModoEdicion = itemAEditar != null
    var itemActual by remember { mutableStateOf(itemAEditar ?: ServicioPresupuesto(descripcion = "", total = 0.0)) }
    var itemPendienteParaGuardar by remember { mutableStateOf<ServicioPresupuesto?>(null) }

    Column(modifier = Modifier.fillMaxWidth().background(colors.backgroundColor).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Build, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(if (esModoEdicion) "Editar Servicio" else "Nuevo Servicio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDescartar) { Icon(Icons.Default.Close, null, tint = colors.textSecondary) }
        }
        FilaDetalleServicio(servicio = itemActual, itemsSugerencia = itemsSugerencia, onActualizar = { itemActual = it })
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (esModoEdicion) onActualizarItem(itemActual)
                else {
                    val agregado = itemActual
                    onAgregarItem(agregado)
                    if (onGuardarEnCatalogo != null && itemsSugerencia.none { it.descripcion.equals(agregado.descripcion, ignoreCase = true) }) {
                        itemPendienteParaGuardar = agregado
                    }
                    itemActual = ServicioPresupuesto()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = itemActual.descripcion.isNotBlank() && itemActual.total > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
        ) {
            Text(if (esModoEdicion) "Guardar Cambios" else "Agregar Servicio", fontWeight = FontWeight.Bold)
        }

        if (itemsActuales.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colors.border)
            Text("Servicios en este presupuesto (${itemsActuales.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 8.dp))
            itemsActuales.forEachIndexed { index, item ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.08f)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(buildString { if (item.codigo.isNotBlank()) append("[${item.codigo}] "); append(item.descripcion) }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        Text("$simboloMoneda${"%.2f".format(item.total)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                    }
                    IconButton(onClick = { itemActual = item }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, null, tint = colors.primaryOrange, modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = { onEliminarItemActual?.invoke(index) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) }
                }
            }
        }

        itemPendienteParaGuardar?.let { newItem ->
            AlertDialog(
                onDismissRequest = { itemPendienteParaGuardar = null; onCompletar() },
                title = { Text("Servicio nuevo", fontWeight = FontWeight.Bold) },
                text = { Text("\"${newItem.descripcion}\" no está en tu lista.\n¿Deseas guardarlo para futuros presupuestos?") },
                confirmButton = {
                    TextButton(onClick = { onGuardarEnCatalogo?.invoke(newItem); itemPendienteParaGuardar = null; onCompletar() }) {
                        Text("Guardar", color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { itemPendienteParaGuardar = null; onCompletar() }) { Text("No, gracias") } }
            )
        }
    }
}

@Composable
fun HojaAgregarGasto(
    itemAEditar: GastoVarioPresupuesto?,
    itemsExistentes: List<GastoVarioPresupuesto> = emptyList(),
    onAgregarItem: (List<GastoVarioPresupuesto>) -> Unit,
    onActualizarItem: (GastoVarioPresupuesto) -> Unit,
    onEliminarItem: ((GastoVarioPresupuesto) -> Unit)? = null,
    onDescartar: () -> Unit = {}
){
    val colors = getPrestadorColors()
    val configVm: PrePresupuestoConfigViewModel = hiltViewModel()
    val pConfig by configVm.config.collectAsState()
    val simboloMoneda = if (pConfig.moneda == "USD") "US$" else "$"
    val esModoEdicion = itemAEditar != null

    var descripcion by remember { mutableStateOf(itemAEditar?.descripcion ?: "") }
    var montoStr by remember { mutableStateOf(if ((itemAEditar?.monto ?: 0.0) > 0) itemAEditar!!.monto.toString() else "") }

    Column(modifier = Modifier.fillMaxWidth().background(colors.backgroundColor).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Receipt, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(if (esModoEdicion) "Editar Gasto" else "Agregar Gasto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDescartar) { Icon(Icons.Default.Close, null, tint = colors.textSecondary) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(0.65f)) {
                Text("DESCRIPCIÓN", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = descripcion, onValorCambio = { descripcion = it }, modifier = Modifier.fillMaxWidth().height(40.dp), sugerencia = { Text("...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) }, opcionesTeclado = KeyboardOptions(imeAction = ImeAction.Next))
            }
            Column(modifier = Modifier.weight(0.35f)) {
                Text("IMPORTE ($)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(bottom = 4.dp))
                CampoTextoCompacto(valor = montoStr, onValorCambio = { val itFiltrado = it.filter { c: Char -> c.isDigit() || c == '.' }; montoStr = itFiltrado }, modifier = Modifier.fillMaxWidth().height(40.dp), sugerencia = { Text("$ 0.00", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)) }, opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done))
            }
        }

        Button(
            onClick = {
                val expense = GastoVarioPresupuesto(id = itemAEditar?.id ?: System.currentTimeMillis(), descripcion = descripcion, monto = montoStr.toDoubleOrNull() ?: 0.0)
                if (esModoEdicion) onActualizarItem(expense)
                else { onAgregarItem(listOf(expense)); descripcion = ""; montoStr = "" }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = descripcion.isNotBlank() && (montoStr.toDoubleOrNull() ?: 0.0) > 0,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
        ) {
            Text(if (esModoEdicion) "Guardar Cambios" else "Agregar Gasto", fontWeight = FontWeight.Bold)
        }

        if (!esModoEdicion) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = colors.border)
            Text("Gastos agregados", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
            itemsExistentes.forEach { expense ->
                key(expense.id) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.06f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.descripcion, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("$simboloMoneda ${"%.2f".format(expense.monto)}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                        }
                        IconButton(onClick = { onEliminarItem?.invoke(expense) }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444)) }
                    }
                }
            }
        }
    }
}

@Composable
fun HojaAgregarImpuesto(
    itemAEditar: ImpuestoPresupuesto?,
    subtotal: Double = 0.0,
    onAgregarItem: (List<ImpuestoPresupuesto>) -> Unit,
    onActualizarItem: (ImpuestoPresupuesto) -> Unit,
    onDescartar: () -> Unit = {}
){
    val colors = getPrestadorColors()
    val esModoEdicion = itemAEditar != null
    var descripcion by remember { mutableStateOf(itemAEditar?.descripcion ?: "") }
    var valorStr by remember { mutableStateOf("") }
    var esPorcentaje by remember { mutableStateOf(true) }

    LaunchedEffect(itemAEditar) {
        if (itemAEditar != null) {
            descripcion = itemAEditar.descripcion
            val pctFromDesc = Regex("(\\d+(?:\\.\\d+)?)%").find(itemAEditar.descripcion)?.groupValues?.get(1)?.toDoubleOrNull()
            if (pctFromDesc != null && subtotal > 0) {
                esPorcentaje = true
                valorStr = pctFromDesc.toString()
            } else {
                esPorcentaje = false
                valorStr = if (itemAEditar.monto > 0) itemAEditar.monto.toString() else ""
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().background(colors.backgroundColor).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 20.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(colors.primaryOrange.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Percent, null, tint = colors.primaryOrange, modifier = Modifier.size(18.dp))
            }
            Text(if (esModoEdicion) "Editar Impuesto" else "Impuesto Personalizado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDescartar) { Icon(Icons.Default.Close, null, tint = colors.textSecondary) }
        }

        CampoTextoCompacto(valor = descripcion, onValorCambio = { descripcion = it }, etiqueta = { Text("Descripción", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.fillMaxWidth().height(40.dp))
        
        // 🔥 [SUPREME]: Plantillas Impositivas
        if (!esModoEdicion) {
            Spacer(Modifier.height(12.dp))
            Text("Plantillas Sugeridas (🇦🇷)", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(com.example.myapplication.core.dominio.modelos.FinanzasMavTemplates.argentina) { template ->
                    Surface(
                        onClick = { 
                            descripcion = template.nombre
                            valorStr = template.porcentaje.toString()
                            esPorcentaje = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = colors.primaryOrange.copy(alpha = 0.05f),
                        border = BorderStroke(0.5.dp, colors.primaryOrange.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "${template.nombre} (${template.porcentaje}%)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = colors.primaryOrange
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CampoTextoCompacto(valor = valorStr, onValorCambio = { val itFiltrado = it.filter { c: Char -> c.isDigit() || c == '.' }; valorStr = itFiltrado }, etiqueta = { Text(if (esPorcentaje) "Porcentaje (%)" else "Importe ($)", style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f).height(40.dp), opcionesTeclado = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Row(modifier = Modifier.height(56.dp).border(1.dp, colors.border, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp))) {
                listOf("%" to true, "$" to false).forEachIndexed { idx, (label, isPct) ->
                    val sel = esPorcentaje == isPct
                    Box(modifier = Modifier.fillMaxHeight().background(if (sel) colors.primaryOrange else Color.Transparent).clickable { esPorcentaje = isPct }.width(48.dp), contentAlignment = Alignment.Center) {
                        Text(label, fontWeight = FontWeight.Bold, color = if (sel) Color.White else colors.textSecondary)
                    }
                    if (idx == 0) Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(colors.border))
                }
            }
        }

        val valorIngresado = valorStr.toDoubleOrNull() ?: 0.0
        val montoFinal = if (esPorcentaje) subtotal * valorIngresado / 100.0 else valorIngresado

        if (valorIngresado > 0 && subtotal > 0) {
            val preview = if (esPorcentaje) "${"%.1f".format(valorIngresado)}% de ${"%.2f".format(subtotal)} = ${"%.2f".format(montoFinal)}" else "${"%.2f".format(valorIngresado)} sobre subtotal ${"%.2f".format(subtotal)}"
            Text(preview, style = MaterialTheme.typography.bodySmall, color = colors.primaryOrange, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDescartar, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, colors.border)) { Text("Cancelar", color = colors.textPrimary) }
            Button(onClick = { val autoD = if (esPorcentaje) "$valorStr%" else "$$valorStr"; val tax = ImpuestoPresupuesto(id = itemAEditar?.id ?: System.currentTimeMillis(), descripcion = descripcion.ifBlank { autoD }, monto = montoFinal); if (esModoEdicion) onActualizarItem(tax) else onAgregarItem(listOf(tax)) }, modifier = Modifier.weight(1f), enabled = valorIngresado > 0, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)) { Text(if (esModoEdicion) "Guardar Cambios" else "Guardar Ítem", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun HojaSecciones(
    mostrarArticulos: Boolean,
    mostrarServicios: Boolean,
    mostrarHonorarios: Boolean,
    mostrarVarios: Boolean,
    mostrarImpuestos: Boolean,
    onMostrarArticulosCambio: (Boolean) -> Unit,
    onMostrarServiciosCambio: (Boolean) -> Unit,
    onMostrarHonorariosCambio: (Boolean) -> Unit,
    onMostrarVariosCambio: (Boolean) -> Unit,
    onMostrarImpuestosCambio: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    Column(modifier = Modifier.padding(16.dp).background(colors.backgroundColor)) {
        Text("Configurar Secciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 16.dp))
        ItemSwitchSeccion("Artículos y Materiales", mostrarArticulos, onMostrarArticulosCambio)
        ItemSwitchSeccion("Mano de Obra / Servicios", mostrarServicios, onMostrarServiciosCambio)
        ItemSwitchSeccion("Honorarios Profesionales", mostrarHonorarios, onMostrarHonorariosCambio)
        ItemSwitchSeccion("Gastos Varios", mostrarVarios, onMostrarVariosCambio)
        ItemSwitchSeccion("Impuestos Personalizados", mostrarImpuestos, onMostrarImpuestosCambio)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ItemSwitchSeccion(titulo: String, marcado: Boolean, alCambiar: (Boolean) -> Unit) {
    val colors = getPrestadorColors()
    Row(modifier = Modifier.fillMaxWidth().clickable { alCambiar(!marcado) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Switch(checked = marcado, onCheckedChange = alCambiar, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primaryOrange))
    }
}

@Composable
fun HojaSelectorCliente(
    clientes: List<PrestadorDominio>,
    idClienteSeleccionado: String?,
    onSeleccionarCliente: (PrestadorDominio) -> Unit,
    onCerrar: () -> Unit
) {
    val colors = getPrestadorColors()
    Column(modifier = Modifier.fillMaxHeight(0.7f).padding(16.dp).background(colors.backgroundColor)) {
        Text("Seleccionar Cliente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary, modifier = Modifier.padding(bottom = 16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(clientes) { cliente ->
                val seleccionado = cliente.id == idClienteSeleccionado
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (seleccionado) colors.primaryOrange.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onSeleccionarCliente(cliente) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = if (cliente.urlMiniatura is String) ImageUtils.processImageSource(cliente.urlMiniatura as String) else cliente.urlMiniatura,
                        contentDescription = null, 
                        modifier = Modifier.size(40.dp).clip(CircleShape), 
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(cliente.titulo, color = colors.textPrimary, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
                    Spacer(Modifier.weight(1f))
                    if (seleccionado) Icon(Icons.Default.Check, null, tint = colors.primaryOrange)
                }
            }
        }
    }
}
















































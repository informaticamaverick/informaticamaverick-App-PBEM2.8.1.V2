package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import com.example.myapplication.prestador.datos.local.entidades.ProductoEntity
import com.example.myapplication.core.datos.local.entidades.TipoProducto
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetAgregarItemMobile(
    sugerencias: List<com.example.myapplication.core.dominio.modelos.ProductoDominio>,
    categoriasVigentes: List<String>,
    itemInicial: Any? = null,
    onBusquedaChange: (String) -> Unit,
    onVerCatalogo: () -> Unit,
    onDismiss: () -> Unit,
    onAgregarArticulo: (ArticuloPresupuesto) -> Unit,
    onAgregarServicio: (ServicioPresupuesto) -> Unit,
    onAgregarGasto: (GastoVarioPresupuesto) -> Unit
) {
    // --- ESTADOS ---
    var tipoSeleccionado by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> TipoProducto.PRODUCTO
                is ServicioPresupuesto -> TipoProducto.SERVICIO
                is GastoVarioPresupuesto -> TipoProducto.GASTO
                else -> TipoProducto.PRODUCTO
            }
        )
    }
    var textoBusqueda by remember { mutableStateOf("") }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    var skuManual by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.codigo
                is ServicioPresupuesto -> itemInicial.codigo
                else -> ""
            }
        )
    }

    var concepto by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.descripcion
                is ServicioPresupuesto -> itemInicial.descripcion
                is GastoVarioPresupuesto -> itemInicial.descripcion
                else -> ""
            }
        )
    }

    var especificacion by remember { mutableStateOf("") } // Para el campo "Especificación para el Cliente"

    var cantidad by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.cantidad.toString()
                else -> "1"
            }
        )
    }

    var precioUnitario by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.precioUnitario.toInt().toString()
                is ServicioPresupuesto -> itemInicial.precioUnitario.toInt().toString()
                is GastoVarioPresupuesto -> itemInicial.precioUnitario.toInt().toString()
                else -> "0"
            }
        )
    }

    var porcentajeDescuento by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.porcentajeDescuento.toInt().toString()
                is ServicioPresupuesto -> itemInicial.porcentajeDescuento.toInt().toString()
                is GastoVarioPresupuesto -> itemInicial.porcentajeDescuento.toInt().toString()
                else -> "0"
            }
        )
    }

    var montoDescuentoFijo by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.montoDescuento.toInt().toString()
                is ServicioPresupuesto -> itemInicial.montoDescuento.toInt().toString()
                is GastoVarioPresupuesto -> itemInicial.montoDescuento.toInt().toString()
                else -> "0"
            }
        )
    }

    var porcentajeInteres by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.porcentajeInteres.toInt().toString()
                is ServicioPresupuesto -> itemInicial.porcentajeInteres.toInt().toString()
                is GastoVarioPresupuesto -> itemInicial.porcentajeInteres.toInt().toString()
                else -> "0"
            }
        )
    }

    var montoInteresFijo by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.montoInteres.toInt().toString()
                is ServicioPresupuesto -> itemInicial.montoInteres.toInt().toString()
                is GastoVarioPresupuesto -> itemInicial.montoInteres.toInt().toString()
                else -> "0"
            }
        )
    }

    var precioCosto by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.precioCosto.toInt().toString()
                else -> "0"
            }
        )
    }

    var idProductoVinculado by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.idProducto
                is ServicioPresupuesto -> itemInicial.idProducto
                else -> null
            }
        )
    }

    var urlImagenVinculada by remember {
        mutableStateOf(
            when (itemInicial) {
                is ArticuloPresupuesto -> itemInicial.urlImagen
                is ServicioPresupuesto -> itemInicial.urlImagen
                else -> null
            }
        )
    }

    var stockActual by remember { mutableStateOf("0") }

    // --- CÁLCULOS ---
    val cantidadInt = cantidad.toIntOrNull() ?: 0
    val precioUnitDouble = precioUnitario.toDoubleOrNull() ?: 0.0
    val descuentoDouble = porcentajeDescuento.toDoubleOrNull() ?: 0.0
    val descuentoFijoDouble = montoDescuentoFijo.toDoubleOrNull() ?: 0.0
    val interesDouble = porcentajeInteres.toDoubleOrNull() ?: 0.0
    val interesFijoDouble = montoInteresFijo.toDoubleOrNull() ?: 0.0

    val subtotal = remember(cantidadInt, precioUnitDouble, descuentoDouble, descuentoFijoDouble, interesDouble, interesFijoDouble) {
        CalculadoraPresupuesto.calcularSubtotalItem(
            precioUnitario = precioUnitDouble,
            cantidad = cantidadInt,
            montoDescuento = descuentoFijoDouble,
            porcentajeDescuento = descuentoDouble,
            montoInteres = interesFijoDouble,
            porcentajeInteres = interesDouble
        )
    }

    // Funciones de sincronización
    fun syncDescuentoDesdeMonto(monto: String) {
        montoDescuentoFijo = monto
        val base = precioUnitDouble * cantidadInt
        if (base > 0) {
            val porc = CalculadoraPresupuesto.calcularPorcentajeDesdeMonto(base, monto.toDoubleOrNull() ?: 0.0)
            porcentajeDescuento = if (porc % 1.0 == 0.0) porc.toInt().toString() else String.format(Locale.getDefault(), "%.1f", porc)
        }
    }

    fun syncDescuentoDesdePorcentaje(porc: String) {
        porcentajeDescuento = porc
        val base = precioUnitDouble * cantidadInt
        val monto = CalculadoraPresupuesto.calcularMontoDesdePorcentaje(base, porc.toDoubleOrNull() ?: 0.0)
        montoDescuentoFijo = if (monto % 1.0 == 0.0) monto.toInt().toString() else String.format(Locale.getDefault(), "%.1f", monto)
    }

    fun syncInteresDesdeMonto(monto: String) {
        montoInteresFijo = monto
        val base = precioUnitDouble * cantidadInt
        if (base > 0) {
            val porc = CalculadoraPresupuesto.calcularPorcentajeDesdeMonto(base, monto.toDoubleOrNull() ?: 0.0)
            porcentajeInteres = if (porc % 1.0 == 0.0) porc.toInt().toString() else String.format(Locale.getDefault(), "%.1f", porc)
        }
    }

    fun syncInteresDesdePorcentaje(porc: String) {
        porcentajeInteres = porc
        val base = precioUnitDouble * cantidadInt
        val monto = CalculadoraPresupuesto.calcularMontoDesdePorcentaje(base, porc.toDoubleOrNull() ?: 0.0)
        montoInteresFijo = if (monto % 1.0 == 0.0) monto.toInt().toString() else String.format(Locale.getDefault(), "%.1f", monto)
    }

    // --- LÓGICA ---
    val sugerenciasFiltradas = remember(sugerencias, tipoSeleccionado) {
        sugerencias.filter {
            when (tipoSeleccionado) {
                TipoProducto.PRODUCTO -> !it.esServicio
                TipoProducto.SERVICIO -> it.esServicio
                else -> false
            }
        }
    }

    fun validarYAñadir() {
        val p = precioUnitario.toDoubleOrNull() ?: 0.0
        val c = cantidad.toIntOrNull() ?: 1
        val d = porcentajeDescuento.toDoubleOrNull() ?: 0.0
        val cost = precioCosto.toDoubleOrNull() ?: 0.0

        val descripcionFinal = if (especificacion.isBlank()) concepto else "$concepto - $especificacion"

        when (tipoSeleccionado) {
            TipoProducto.PRODUCTO -> {
                onAgregarArticulo(
                    ArticuloPresupuesto(
                        id = if (itemInicial is ArticuloPresupuesto) itemInicial.id else System.currentTimeMillis(),
                        idProducto = idProductoVinculado ?: UUID.randomUUID().toString(),
                        descripcion = descripcionFinal,
                        codigo = skuManual,
                        precioUnitario = p,
                        precioCosto = cost,
                        cantidad = c,
                        porcentajeDescuento = d,
                        montoDescuento = descuentoFijoDouble,
                        porcentajeInteres = porcentajeInteres.toDoubleOrNull() ?: 0.0,
                        montoInteres = interesFijoDouble,
                        urlImagen = urlImagenVinculada
                    )
                )
            }
            TipoProducto.SERVICIO -> {
                onAgregarServicio(
                    ServicioPresupuesto(
                        id = if (itemInicial is ServicioPresupuesto) itemInicial.id else System.currentTimeMillis(),
                        idProducto = idProductoVinculado ?: UUID.randomUUID().toString(),
                        descripcion = descripcionFinal,
                        codigo = skuManual,
                        precioUnitario = p,
                        porcentajeDescuento = d,
                        montoDescuento = descuentoFijoDouble,
                        porcentajeInteres = porcentajeInteres.toDoubleOrNull() ?: 0.0,
                        montoInteres = interesFijoDouble,
                        total = subtotal
                    )
                )
            }
            TipoProducto.GASTO -> {
                onAgregarGasto(
                    GastoVarioPresupuesto(
                        id = if (itemInicial is GastoVarioPresupuesto) itemInicial.id else System.currentTimeMillis(),
                        descripcion = descripcionFinal,
                        precioUnitario = p,
                        porcentajeDescuento = d,
                        montoDescuento = descuentoFijoDouble,
                        porcentajeInteres = porcentajeInteres.toDoubleOrNull() ?: 0.0,
                        montoInteres = interesFijoDouble,
                        monto = subtotal
                    )
                )
            }
        }
        onDismiss()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E293B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (itemInicial == null) "Añadir al Presupuesto" else "Editar ítem",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Red)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // BUSCADOR
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Plantillas y Catálogo",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF97316)
                )

                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = {
                        textoBusqueda = it
                        onBusquedaChange(it)
                    },
                    placeholder = { Text("Buscar por Nombre, Sku, etc...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
                    trailingIcon = {
                        if (textoBusqueda.isNotEmpty()) {
                            IconButton(onClick = { textoBusqueda = ""; onBusquedaChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFF97316),
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                // RESULTADOS
                if (textoBusqueda.length >= 2 && sugerenciasFiltradas.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(sugerenciasFiltradas) { item ->
                                Surface(
                                    onClick = {
                                        concepto = item.nombre
                                        precioUnitario = item.precio.toInt().toString()
                                        precioCosto = item.precioCosto.toInt().toString()
                                        idProductoVinculado = item.id
                                        skuManual = item.codigo
                                        urlImagenVinculada = item.urlImagen
                                        stockActual = item.stockActual.toString()
                                        textoBusqueda = ""
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = Color(0xFFF97316).copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = item.codigo.ifBlank { "S/C" },
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color(0xFFF97316),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = item.nombre,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = String.format(Locale.getDefault(), "$%.2f", item.precio),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFF97316)
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = if (item.esServicio) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = if (item.esServicio) "Servicio" else "Producto",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                    color = if (item.esServicio) Color(0xFFC4B5FD) else Color(0xFF6EE7B7),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            
                                            // 🔥 [ELITE]: Mostrar Categoría (Emoji + Nombre)
                                            if (item.nombreCategoria != null) {
                                                Text(
                                                    text = "${item.iconoCategoria ?: "📂"} ${item.nombreCategoria}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF06B6D4),
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // FORMULARIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = skuManual,
                    onValueChange = { skuManual = it.uppercase() },
                    label = { Text("Código / Ref.", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFF97316),
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown },
                    modifier = Modifier.weight(1.3f)
                ) {
                    OutlinedTextField(
                        value = when (tipoSeleccionado) {
                            TipoProducto.PRODUCTO -> "Producto / Ítem"
                            TipoProducto.SERVICIO -> "Servicio"
                            TipoProducto.GASTO -> "Gasto Vario"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo *", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF97316),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false },
                        modifier = Modifier.background(Color(0xFF1E293B))
                    ) {
                        TipoProducto.entries.forEach { tipo ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (tipo) {
                                            TipoProducto.PRODUCTO -> "Producto / Ítem"
                                            TipoProducto.SERVICIO -> "Servicio"
                                            TipoProducto.GASTO -> "Gasto Vario"
                                        },
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    tipoSeleccionado = tipo
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = concepto,
                onValueChange = { concepto = it },
                label = { Text("Concepto *", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFF97316),
                    focusedBorderColor = Color(0xFFF97316),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = especificacion,
                onValueChange = { especificacion = it },
                label = { Text("Especificación para el Cliente", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFF97316),
                    focusedBorderColor = Color(0xFFF97316),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            // FILA 1: CANTIDAD Y PRECIO UNITARIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cant.", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                OutlinedTextField(
                    value = precioUnitario,
                    onValueChange = { precioUnitario = it },
                    label = { Text("Precio Unit. ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFF97316),
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // DESCUENTOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = montoDescuentoFijo,
                    onValueChange = { syncDescuentoDesdeMonto(it) },
                    label = { Text("Desc. Fijo ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                OutlinedTextField(
                    value = porcentajeDescuento,
                    onValueChange = { syncDescuentoDesdePorcentaje(it) },
                    label = { Text("Desc. %", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // INTERESES / RECARGOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = montoInteresFijo,
                    onValueChange = { syncInteresDesdeMonto(it) },
                    label = { Text("Recargo Fijo ($)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                OutlinedTextField(
                    value = porcentajeInteres,
                    onValueChange = { syncInteresDesdePorcentaje(it) },
                    label = { Text("Recargo %", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )
            }

            // FILA 2: STOCK (READ ONLY) Y SUBTOTAL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (tipoSeleccionado == TipoProducto.PRODUCTO) stockActual else "N/A",
                    onValueChange = { },
                    label = { Text("Stock Disp.", color = Color.Gray) },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF97316).copy(alpha = 0.15f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SUBTOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF97316)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "$%.2f", subtotal),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text("Cancelar", color = Color.White)
                }

                Button(
                    onClick = { if (concepto.isNotBlank()) validarYAñadir() },
                    enabled = concepto.isNotBlank(),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (itemInicial == null) "Añadir al Presupuesto" else "Guardar Cambios", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}



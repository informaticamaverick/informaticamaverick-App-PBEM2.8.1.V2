package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.TipoPresupuesto
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.prestador.ui.pantallas.presupuesto.componentes.*
import com.example.myapplication.prestador.viewmodel.presupuesto.PrePresupuestoConfigViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PrestadorPresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoRapidoSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val armadorViewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
    val stateDeep by armadorViewModel.state.collectAsStateWithLifecycle()
    val ecosistema = stateDeep.ecosistema
    val provider = ecosistema?.prestador

    val configViewModel: PrePresupuestoConfigViewModel = hiltViewModel()
    val config by configViewModel.config.collectAsState()

    val PresupuestoViewModel: PrestadorPresupuestoViewModel = hiltViewModel()
    val articleCatalog by PresupuestoViewModel.articleCatalog.collectAsState()
    val serviceCatalog by PresupuestoViewModel.serviceCatalog.collectAsState()

    // --- SECTOR: PARSEO DE CATÁLOGOS ---
    val savedArticleItems = remember(articleCatalog) {
        val json = articleCatalog.itemsJson
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 4) ArticuloPresupuesto(
                id = System.currentTimeMillis() + (0..999).random(), 
                codigo = parts[0], descripcion = parts[1],
                cantidad = parts[2].toIntOrNull() ?: 1,
                precioUnitario = parts[3].toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.descripcion }
    }
    val savedServiceItems = remember(serviceCatalog) {
        val json = serviceCatalog.serviciosJson
        if (json.isBlank()) emptyList()
        else json.split("|").mapNotNull { s ->
            val parts = s.split(";")
            if (parts.size >= 2) ServicioPresupuesto(
                id = System.currentTimeMillis() + (0..999).random(), 
                codigo = parts[0], descripcion = parts[1], 
                precioUnitario = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
            ) else null
        }.distinctBy { it.descripcion }
    }
    
    val suggMisc = emptyList<String>()

    // --- SECTOR: ESTADO UI ---
    var clienteNombre by remember { mutableStateOf("") }
    var tituloTrabajo by remember { mutableStateOf("") }
    var selectedCategory by remember(provider) {
        mutableStateOf(provider?.perfil?.idCategorias?.firstOrNull() ?: "GENERAL")
    }
    var validezDias by remember(config.validezDias) {
        mutableStateOf(config.validezDias.toString())
    }

    val articulos = remember { mutableStateListOf<ArticuloPresupuesto>() }
    val servicios = remember { mutableStateListOf<ServicioPresupuesto>() }
    val gastosVarios = remember { mutableStateListOf<GastoVarioPresupuesto>() }
    val impuestos = remember { mutableStateListOf<ImpuestoPresupuesto>() }

    var articulosExpanded by remember { mutableStateOf(true) }
    var serviciosExpanded by remember { mutableStateOf(false) }
    var gastosExpanded by remember { mutableStateOf(false) }

    // Totales
    val itemsSubtotal = articulos.sumOf { it.precioUnitario * it.cantidad }
    val servicesSubtotal = servicios.sumOf { it.precioUnitario }
    val miscSubtotal = gastosVarios.sumOf { it.precioUnitario }
    val taxesSubtotal = impuestos.sumOf { it.monto }
    val subtotal = itemsSubtotal + servicesSubtotal + miscSubtotal
    val grandTotal = subtotal + taxesSubtotal

    val currencySymbol = if (config.moneda == "USD") "US$" else "$"
    val accent = Color(0xFFF97316)
    var mostrarPreview by remember { mutableStateOf(false) }
    val hasContent = articulos.isNotEmpty() || servicios.isNotEmpty() || gastosVarios.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFAFAFA),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Presupuesto Rápido", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1F2937))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color(0xFF6B7280)) }
            }

            // Datos Cliente y Trabajo
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = clienteNombre, onValueChange = { clienteNombre = it }, label = { Text("Nombre del Cliente") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                    OutlinedTextField(value = tituloTrabajo, onValueChange = { tituloTrabajo = it }, label = { Text("Título del Trabajo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                }
            }

            // ── Artículos ──
            SeccionColapsable(
                titulo = "Artículos",
                items = articulos.toList(),
                totalSeccion = itemsSubtotal,
                estaExpandida = articulosExpanded,
                alAlternar = { articulosExpanded = !articulosExpanded },
                alAgregar = { articulos.add(ArticuloPresupuesto(id = System.currentTimeMillis())) },
                ranuraAgregarRapido = {
                    CamposAutoCompletadoArticulo(
                        sugerencias = savedArticleItems,
                        alAgregar = { articulos.add(it.copy(id = System.currentTimeMillis())) },
                        items = articulos.toList(),
                        alEditar = { updated ->
                            val i = articulos.indexOfFirst { it.id == updated.id }
                            if (i != -1) articulos[i] = updated
                        },
                        alEliminar = { articulos.removeAt(it) }
                    )
                }
            ) { item, index ->
                FilaDetalleArticulo(item = item, itemsSugerencia = savedArticleItems, onActualizar = { updated -> articulos[index] = updated })
            }

            // ── Servicios ──
            SeccionColapsable(
                titulo = "Servicios",
                items = servicios.toList(),
                totalSeccion = servicesSubtotal,
                estaExpandida = serviciosExpanded,
                alAlternar = { serviciosExpanded = !serviciosExpanded },
                alAgregar = { servicios.add(ServicioPresupuesto(id = System.currentTimeMillis())) },
                ranuraAgregarRapido = {
                    CamposAutoCompletadoServicio(
                        sugerencias = savedServiceItems,
                        alAgregar = { servicios.add(it.copy(id = System.currentTimeMillis())) },
                        items = servicios.toList(),
                        alEditar = { updated ->
                            val i = servicios.indexOfFirst { it.id == updated.id }
                            if (i != -1) servicios[i] = updated
                        },
                        alEliminar = { servicios.removeAt(it) }
                    )
                }
            ) { item, index ->
                FilaDetalleServicio(servicio = item, itemsSugerencia = savedServiceItems, onActualizar = { updated -> servicios[index] = updated })
            }

            // ── Gastos Varios ──
            SeccionColapsable(
                titulo = "Gastos Varios",
                items = gastosVarios.toList(),
                totalSeccion = miscSubtotal,
                estaExpandida = gastosExpanded,
                alAlternar = { gastosExpanded = !gastosExpanded },
                alAgregar = { gastosVarios.add(GastoVarioPresupuesto(id = System.currentTimeMillis())) },
                ranuraAgregarRapido = {
                    CampoAutoCompletadoDescripcion(
                        etiqueta = "Gasto rápido",
                        sugerencias = suggMisc,
                        alSeleccionar = { desc ->
                            gastosVarios.add(GastoVarioPresupuesto(id = System.currentTimeMillis(), descripcion = desc, monto = 0.0))
                            gastosExpanded = true
                        }
                    )
                }
            ) { item, index ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTextoCompacto(valor = item.descripcion, onValorCambio = { d -> gastosVarios[index] = item.copy(descripcion = d) }, modifier = Modifier.weight(1f))
                    CampoTextoCompacto(valor = if (item.monto == 0.0) "" else item.monto.toString(), onValorCambio = { m -> gastosVarios[index] = item.copy(monto = m.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.width(100.dp))
                    IconButton(onClick = { gastosVarios.removeAt(index) }) { Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                }
            }

            // Total Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total General", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("$currencySymbol ${String.format("%.2f", grandTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = accent)
                }
            }

            // Botón Compartir / Preview
            Button(
                onClick = { mostrarPreview = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                enabled = hasContent && provider != null
            ) {
                Icon(Icons.Default.PictureAsPdf, null)
                Spacer(Modifier.width(8.dp))
                Text("Generar y Previsualizar PDF", fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- SECTOR: PREVIEW & EXPORT (Ley #8) ---
    if (mostrarPreview && provider != null) {
        val prestadorUi = com.example.myapplication.core.dominio.mapeadores.PrestadorMappers.deCompletoAModeloUi(provider)
        
        val h = PresupuestoFinalEntity(
            idPresupuesto = "TEMP_${System.currentTimeMillis()}",
            idPrestador = provider.perfil.id,
            nombrePrestador = prestadorUi.titulo,
            idCategoria = selectedCategory,
            tituloTrabajo = tituloTrabajo,
            subtotal = subtotal,
            totalGeneral = grandTotal,
            totalImpuestos = taxesSubtotal,
            totalDescuentos = 0.0,
            tipo = TipoPresupuesto.RAPIDO,
            marcaTiempo = System.currentTimeMillis()
        )

        val lineas = mutableListOf<ProductoFinalEntity>()
        articulos.forEach { lineas.add(ProductoFinalEntity(idPresupuesto = h.idPresupuesto, nombreCopiado = it.descripcion, cantidad = it.cantidad, precioSnapshot = it.precioUnitario, tipoItem = TipoProductoFinal.PRODUCTO)) }
        servicios.forEach { lineas.add(ProductoFinalEntity(idPresupuesto = h.idPresupuesto, nombreCopiado = it.descripcion, cantidad = 1, precioSnapshot = it.precioUnitario, tipoItem = TipoProductoFinal.SERVICIO)) }
        gastosVarios.forEach { lineas.add(ProductoFinalEntity(idPresupuesto = h.idPresupuesto, nombreCopiado = it.descripcion, cantidad = 1, precioSnapshot = it.precioUnitario, tipoItem = TipoProductoFinal.GASTO)) }

        val wrap = PresupuestoConItems(
            cabecera = h,
            lineas = lineas,
            finanzas = impuestos.map { FinanzaFinalEntity(idPresupuesto = h.idPresupuesto, etiqueta = it.descripcion, monto = it.monto, tipo = TipoFinanzaFinal.IMPUESTO) }
        )

        com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog(
            prestador = prestadorUi,
            relacion = wrap,
            clientName = clienteNombre,
            onDismiss = { mostrarPreview = false },
            showSendButton = false
        )
    }
}



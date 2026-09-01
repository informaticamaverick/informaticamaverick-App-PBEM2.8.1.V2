/*
package com.example.myapplication.prestador.ui.pantallas.chat.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.prestador.viewmodel.profile.ArmadorPrestadorViewModel
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.presupuesto.PrestadorPresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.ProductoViewModel
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.myapplication.prestador.ui.pantallas.presupuesto.hojas.*
import com.example.myapplication.prestador.ui.pantallas.presupuesto.componentes.*
import com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog

/**
 * --- HOJA DE NUEVO PRESUPUESTO (v2026.ELITE) ---
 * PROPÓSITO: Creador de presupuestos atómico para el chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoPresupuestoSheet(
    idUsuario: String,
    nombreUsuario: String,
    idPrestador: String,
    direccionClienteInicial: String? = null,
    onDescartar: () -> Unit,
    viewModelPresupuesto: PrestadorPresupuestoViewModel = hiltViewModel(),
    viewModelProducto: ProductoViewModel = hiltViewModel(),
    identidadViewModel: ArmadorPrestadorViewModel = hiltViewModel(),
    configViewModel: com.example.myapplication.prestador.viewmodel.config.AppSettingsViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val maestro by identidadViewModel.ecosistemaMaestro.collectAsState()
    val identidadUi = remember(maestro) {
        maestro?.prestador?.perfil?.let { p ->
            PrestadorDominio(id = p.id, titulo = p.nombreVisible, urlMiniatura = p.miniaturaBase64 ?: p.urlFotoPerfil)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val articulos = remember { mutableStateListOf<ArticuloPresupuesto>() }
    val servicios = remember { mutableStateListOf<ServicioPresupuesto>() }
    val honorariosProfesionales = remember { mutableStateListOf<HonorarioPresupuesto>() }
    val gastosVarios = remember { mutableStateListOf<GastoVarioPresupuesto>() }
    val impuestos = remember { mutableStateListOf<ImpuestoPresupuesto>() }

    var expandedSection by remember { mutableStateOf<String?>(null) }
    var tipoHoja by remember { mutableStateOf<TipoHojaPresupuesto?>(null) }
    var itemAEditar by remember { mutableStateOf<Any?>(null) }
    var mostrarDialogoVistaPrevia by remember { mutableStateOf(false) }
    var presupuestoPendiente by remember { mutableStateOf<PresupuestoEntity?>(null) }
    
    val listaPresupuestos by viewModelPresupuesto.presupuestos.collectAsState()
    val catalogoArticulos by viewModelProducto.catalogoArticulos.collectAsState()
    val catalogoServicios by viewModelProducto.catalogoServicios.collectAsState()

    var tituloTrabajo by remember { mutableStateOf("") }
    var validez by remember { mutableStateOf("7") }

    val calc = remember(articulos.toList(), servicios.toList(), honorariosProfesionales.toList(), gastosVarios.toList(), impuestos.toList()) {
        CalculadoraPresupuesto.calcularTodo(articulos, servicios, honorariosProfesionales, gastosVarios, impuestos)
    }
    
    val totalGeneral = calc.totalGeneral
    val lazyListState = rememberLazyListState()
    val tieneItems = articulos.isNotEmpty() || servicios.isNotEmpty() || honorariosProfesionales.isNotEmpty() || gastosVarios.isNotEmpty()

    // 🔥 [FIX v2026.ELITE]: Observar errores para evitar crash
    val contexto = LocalContext.current
    LaunchedEffect(Unit) {
        viewModelPresupuesto.error.collect { msg ->
            android.widget.Toast.makeText(contexto, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val itemsSugerencia = remember(catalogoArticulos) { 
        catalogoArticulos.map { 
            ArticuloPresupuesto(
                idProducto = it.id, 
                codigo = it.sku ?: "", 
                descripcion = it.nombre, 
                precioUnitario = it.precioVenta,
                porcentajeImpuesto = it.impuestoDefault,
                porcentajeDescuento = it.descuentoDefault,
                urlImagen = it.urlImagen,
                miniaturaBase64 = it.miniaturaBase64
            ) 
        } 
    }
    val serviciosSugerencia = remember(catalogoServicios) { 
        catalogoServicios.map { 
            ServicioPresupuesto(
                idProducto = it.id, 
                codigo = it.sku ?: "", 
                descripcion = it.nombre, 
                total = it.precioVenta,
                urlImagen = it.urlImagen,
                miniaturaBase64 = it.miniaturaBase64
            ) 
        } 
    }
    val honorariosSugerencia = remember(listaPresupuestos) { listaPresupuestos.flatMap { it.honorarios }.distinctBy { it.descripcion } }

    fun construirPresupuesto(): PresupuestoEntity {
        val timestamp = System.currentTimeMillis()
        val friendlyId = "PR-${(100000..999999).random()}"
        
        return PresupuestoEntity(
            idPresupuesto = "pres_chat_$timestamp",
            numeroPresupuesto = friendlyId,
            idCliente = idUsuario,
            idPrestador = idPrestador,
            nombrePrestador = identidadUi?.titulo ?: "Prestador",
            totalGeneral = totalGeneral,
            subtotal = calc.subtotal,
            estado = EstadoPresupuesto.PENDIENTE,
            tituloTrabajo = tituloTrabajo,
            articulos = articulos.toList(),
            servicios = servicios.toList(),
            honorarios = honorariosProfesionales.toList(),
            gastosVarios = gastosVarios.toList(),
            impuestosDetalle = impuestos.toList(),
            marcaTiempo = System.currentTimeMillis()
        )
    }

    ModalBottomSheet(onDismissRequest = { if (tipoHoja != null) tipoHoja = null else onDescartar() }, containerColor = colors.backgroundColor) {
        if (tipoHoja == null) {
            Column(modifier = Modifier.fillMaxHeight(0.93f)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Presupuesto para $nombreUsuario", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    IconButton(onClick = onDescartar) { Icon(Icons.Default.Close, null) }
                }
                HorizontalDivider(color = colors.border)
                LazyColumn(state = lazyListState, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
                    item { OutlinedTextField(value = tituloTrabajo, onValueChange = { tituloTrabajo = it }, label = { Text("Título del trabajo") }, modifier = Modifier.fillMaxWidth()) }
                    item { SeccionColapsable(titulo = "Artículos", items = articulos, totalSeccion = calc.totalMateriales, estaExpandida = expandedSection == "articulos", alAlternar = { expandedSection = if (expandedSection == "articulos") null else "articulos" }, alAgregar = { itemAEditar = null; tipoHoja = TipoHojaPresupuesto.Articulo }) { _, _ -> } }
                    item { SeccionColapsable(titulo = "Servicios", items = servicios, totalSeccion = servicios.sumOf { it.total }, estaExpandida = expandedSection == "servicios", alAlternar = { expandedSection = if (expandedSection == "servicios") null else "servicios" }, alAgregar = { itemAEditar = null; tipoHoja = TipoHojaPresupuesto.Servicio }) { _, _ -> } }
                    item { SeccionColapsable(titulo = "Honorarios", items = honorariosProfesionales, totalSeccion = honorariosProfesionales.sumOf { it.total }, estaExpandida = expandedSection == "honorarios", alAlternar = { expandedSection = if (expandedSection == "honorarios") null else "honorarios" }, alAgregar = { itemAEditar = null; tipoHoja = TipoHojaPresupuesto.Honorario }) { _, _ -> } }
                }
                Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E293B)) {
                    Row(modifier = Modifier.padding(16.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total", color = Color.Gray, fontSize = 12.sp)
                            Text("$ ${String.format(Locale.getDefault(), "%,.2f", totalGeneral)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Button(onClick = { presupuestoPendiente = construirPresupuesto(); mostrarDialogoVistaPrevia = true }, enabled = tieneItems, colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)) { Text("Vista Previa") }
                    }
                }
            }
        } else {
            when (tipoHoja) {
                TipoHojaPresupuesto.Articulo -> HojaAgregarArticulo(itemAEditar = itemAEditar as? ArticuloPresupuesto, itemsSugerencia = itemsSugerencia, itemsActuales = articulos, onAgregarItem = { articulos.add(it) }, onActualizarItem = { updated -> val i = articulos.indexOfFirst { it.id == updated.id }; if (i != -1) articulos[i] = updated; tipoHoja = null }, onEliminarItemActual = { articulos.removeAt(it) }, onGuardarEnCatalogo = { viewModelProducto.guardarArticuloEnCatalogo(it, idPrestador) }, onDescartar = { tipoHoja = null })
                TipoHojaPresupuesto.Servicio -> HojaAgregarServicio(itemAEditar = itemAEditar as? ServicioPresupuesto, itemsSugerencia = serviciosSugerencia, itemsActuales = servicios, onAgregarItem = { servicios.add(it) }, onActualizarItem = { updated -> val i = servicios.indexOfFirst { it.id == updated.id }; if (i != -1) servicios[i] = updated; tipoHoja = null }, onEliminarItemActual = { servicios.removeAt(it) }, onGuardarEnCatalogo = { viewModelProducto.guardarServicioEnCatalogo(it, idPrestador) }, onDescartar = { tipoHoja = null })
                TipoHojaPresupuesto.Honorario -> HojaAgregarHonorario(itemAEditar = itemAEditar as? HonorarioPresupuesto, itemsSugerencia = honorariosSugerencia, itemsActuales = honorariosProfesionales, onAgregarItem = { honorariosProfesionales.add(it) }, onActualizarItem = { updated -> val i = honorariosProfesionales.indexOfFirst { it.id == updated.id }; if (i != -1) honorariosProfesionales[i] = updated; tipoHoja = null }, onEliminarItemActual = { honorariosProfesionales.removeAt(it) }, onGuardarEnCatalogo = { viewModelProducto.guardarServicioEnCatalogo(ServicioPresupuesto(descripcion = it.descripcion, total = it.total), idPrestador) }, onDescartar = { tipoHoja = null })
                else -> {}
            }
        }
    }

    if (mostrarDialogoVistaPrevia && identidadUi != null) {
        val pres = presupuestoPendiente ?: construirPresupuesto()
        PlanillaPresupuestoA4Dialog(
            prestador = identidadUi, 
            relacion = PresupuestoConItems(cabecera = pres.deEntidadAFinal(), lineas = emptyList(), finanzas = emptyList()), // Simplificado para el mock comentado
            onDismiss = { mostrarDialogoVistaPrevia = false }, 
            onEnviar = { miniatura -> 
                val presConMiniatura = pres.copy(miniaturaPresupuesto = miniatura)
                coroutineScope.launch { 
                    viewModelPresupuesto.enviarPresupuestoReal(presConMiniatura) 
                }
                mostrarDialogoVistaPrevia = false
                onDescartar() 
            }, 
            clientName = nombreUsuario
        )
    }
}
*/



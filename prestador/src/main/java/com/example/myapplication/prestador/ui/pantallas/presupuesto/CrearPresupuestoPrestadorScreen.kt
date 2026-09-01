/*
package com.example.myapplication.prestador.ui.pantallas.presupuesto

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.presupuesto.PrestadorPresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.ProductoViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.PrePresupuestoConfigViewModel
import com.example.myapplication.prestador.viewmodel.profile.ArmadorPrestadorViewModel
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import com.example.myapplication.core.dominio.modelos.PresupuestoConfig
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.prestador.ui.pantallas.presupuesto.hojas.*
import com.example.myapplication.prestador.ui.pantallas.presupuesto.componentes.*
import java.util.UUID

/**
 * --- PANTALLA DE CREACIÓN DE PRESUPUESTOS (V2026.FINAL) ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoPrestadorScreen(
    idCita: String = "",
    idConcurso: String = "",
    onVolver: () -> Unit = {},
    viewModelPresupuesto: PrestadorPresupuestoViewModel = hiltViewModel(),
    viewModelProducto: ProductoViewModel = hiltViewModel(),
    identidadViewModel: ArmadorPrestadorViewModel = hiltViewModel(),
    configViewModel: PrePresupuestoConfigViewModel = hiltViewModel()
) {
    val maestro by identidadViewModel.ecosistemaMaestro.collectAsState()
    val presupuestoConfig by configViewModel.config.collectAsState()
    val presupuestosList by viewModelPresupuesto.presupuestos.collectAsState()
    val catalogoArticulos by viewModelProducto.catalogoArticulos.collectAsState()
    val catalogoServicios by viewModelProducto.catalogoServicios.collectAsState()
    val concursoDetalles by viewModelPresupuesto.detallesConcurso.collectAsState()
    val clientesList by viewModelPresupuesto.clientes.collectAsState()

    LaunchedEffect(idConcurso) { if (idConcurso.isNotBlank()) viewModelPresupuesto.cargarDetallesConcurso(idConcurso) }

    val identidadUi = remember(maestro) {
        maestro?.prestador?.perfil?.let { p ->
            PrestadorDominio(
                id = p.id,
                titulo = p.nombreVisible,
                urlMiniatura = p.miniaturaBase64 ?: p.urlFotoPerfil,
                categorias = p.categorias,
                cuitCuil = p.cuitCuil,
                matricula = p.matricula
            )
        }
    }

    val clientesUi = remember(clientesList) {
        clientesList.map { c: IdentidadUsuarioEntity ->
            PrestadorDominio(
                id = c.id, 
                titulo = c.nombreVisible, 
                urlMiniatura = c.miniaturaBase64 ?: c.urlFotoPerfil
            )
        }
    }

    CrearPresupuestoPrestadorContent(
        idCita = idCita, 
        idConcurso = idConcurso, 
        concurso = concursoDetalles,
        identidad = identidadUi, 
        presupuestoConfig = presupuestoConfig,
        presupuestos = presupuestosList, 
        catalogoArticulos = catalogoArticulos, 
        catalogoServicios = catalogoServicios,
        clientes = clientesUi,
        onVolver = onVolver, 
        onInsertarPresupuesto = { viewModelPresupuesto.insertarPresupuesto(it) }, 
        onEnviarPresupuestoReal = { viewModelPresupuesto.enviarPresupuestoReal(it) },
        onGuardarArticuloEnCatalogo = { item -> identidadUi?.id?.let { viewModelProducto.guardarArticuloEnCatalogo(item, it) } },
        onGuardarServicioEnCatalogo = { item -> identidadUi?.id?.let { viewModelProducto.guardarServicioEnCatalogo(item, it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPresupuestoPrestadorContent(
    idCita: String = "", 
    idConcurso: String = "", 
    concurso: ConcursoPublicoEntity? = null,
    identidad: PrestadorDominio?, 
    presupuestoConfig: PresupuestoConfig,
    presupuestos: List<PresupuestoEntity>, 
    catalogoArticulos: List<ProductoEntity>, 
    catalogoServicios: List<ProductoEntity>,
    clientes: List<PrestadorDominio>,
    onVolver: () -> Unit, 
    onInsertarPresupuesto: (PresupuestoEntity) -> Unit, 
    onEnviarPresupuestoReal: (PresupuestoEntity) -> Unit,
    onGuardarArticuloEnCatalogo: (ArticuloPresupuesto) -> Unit,
    onGuardarServicioEnCatalogo: (ServicioPresupuesto) -> Unit
) {
    val colors = getPrestadorColors()
    val articulos = remember { mutableStateListOf<ArticuloPresupuesto>() }
    val servicios = remember { mutableStateListOf<ServicioPresupuesto>() }
    val honorarios = remember { mutableStateListOf<HonorarioPresupuesto>() }
    val gastos = remember { mutableStateListOf<GastoVarioPresupuesto>() }
    val impuestos = remember { mutableStateListOf<ImpuestoPresupuesto>() }

    val articulosSugeridos = remember(catalogoArticulos) { 
        catalogoArticulos.map { 
            ArticuloPresupuesto(
                idProducto = it.id, 
                codigo = it.sku ?: "", 
                descripcion = it.nombre, 
                precioUnitario = it.precioVenta,
                precioCosto = it.precioCosto,
                porcentajeImpuesto = it.impuestoDefault,
                porcentajeDescuento = it.descuentoDefault,
                urlImagen = it.urlImagen,
                miniaturaBase64 = it.miniaturaBase64
            ) 
        } 
    }
    val serviciosSugeridos = remember(catalogoServicios) { 
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

    var tipoHoja by remember { mutableStateOf<TipoHojaPresupuesto?>(null) }
    var itemAEditar by remember { mutableStateOf<Any?>(null) }
    var mostrarVistaPrevia by remember { mutableStateOf(false) }
    var presupuestoPendiente by remember { mutableStateOf<PresupuestoEntity?>(null) }
    
    var idClienteSeleccionado by remember { mutableStateOf<String?>(null) }
    var clienteNombre by remember { mutableStateOf("Seleccionar Cliente") }

    val calc = remember(articulos.toList(), servicios.toList(), honorarios.toList(), gastos.toList(), impuestos.toList()) {
        CalculadoraPresupuesto.calcularTodo(articulos, servicios, honorarios, gastos, impuestos)
    }

    var mostrarSeccionArticulos by remember { mutableStateOf(true) }
    var mostrarSeccionServicios by remember { mutableStateOf(true) }
    var mostrarSeccionHonorarios by remember { mutableStateOf(false) }
    var mostrarSeccionVarios by remember { mutableStateOf(true) }
    var mostrarSeccionImpuestos by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = { 
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Nuevo Presupuesto Elite", color = Color.White, fontWeight = FontWeight.Bold) }, 
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }, 
                actions = {
                    IconButton(onClick = { tipoHoja = TipoHojaPresupuesto.Secciones }) {
                        Icon(Icons.Default.Tune, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.primaryOrange)
            ) 
        },
        floatingActionButton = {
            if (articulos.isNotEmpty() || servicios.isNotEmpty() || honorarios.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        if (idClienteSeleccionado.isNullOrBlank()) {
                            tipoHoja = TipoHojaPresupuesto.SelectorCliente
                            return@FloatingActionButton
                        }
                        val nuevo = PresupuestoEntity(
                            idPresupuesto = "pres_${UUID.randomUUID()}",
                            idPrestador = identidad?.id ?: "",
                            idCliente = idClienteSeleccionado!!,
                            nombrePrestador = identidad?.titulo ?: "Prestador",
                            totalGeneral = calc.totalGeneral,
                            subtotal = calc.subtotal,
                            articulos = articulos.toList(),
                            servicios = servicios.toList(),
                            honorarios = honorarios.toList(),
                            gastosVarios = gastos.toList(),
                            impuestosDetalle = impuestos.toList(),
                            idConcurso = idConcurso.takeIf { it.isNotBlank() },
                            marcaTiempo = System.currentTimeMillis(),
                            estado = EstadoPresupuesto.PENDIENTE
                        )
                        presupuestoPendiente = nuevo
                        mostrarVistaPrevia = true
                    },
                    containerColor = colors.primaryOrange, contentColor = Color.White
                ) { Icon(Icons.AutoMirrored.Filled.Send, null) }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            item { Spacer(Modifier.height(16.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { tipoHoja = TipoHojaPresupuesto.SelectorCliente },
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = colors.primaryOrange)
                        Spacer(Modifier.width(12.dp))
                        Text(clienteNombre, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (mostrarSeccionArticulos) item {
                SeccionColapsable(
                    titulo = "Artículos / Materiales",
                    items = articulos,
                    totalSeccion = calc.totalMateriales,
                    estaExpandida = true,
                    alAlternar = {},
                    alAgregar = { itemAEditar = null; tipoHoja = TipoHojaPresupuesto.Articulo }
                ) { item, index ->
                    FilaResumenArticulo(item = item, onEditar = { itemAEditar = item; tipoHoja = TipoHojaPresupuesto.Articulo }, onEliminar = { articulos.removeAt(index) })
                }
            }

            if (mostrarSeccionServicios) item {
                SeccionColapsable(
                    titulo = "Mano de Obra",
                    items = servicios,
                    totalSeccion = servicios.sumOf { it.total },
                    estaExpandida = true,
                    alAlternar = {},
                    alAgregar = { itemAEditar = null; tipoHoja = TipoHojaPresupuesto.Servicio }
                ) { item, index ->
                    FilaResumenServicio(servicio = item, onEditar = { itemAEditar = item; tipoHoja = TipoHojaPresupuesto.Servicio }, onEliminar = { servicios.removeAt(index) })
                }
            }

            if (mostrarSeccionHonorarios) item {
                SeccionColapsable(
                    titulo = "Honorarios",
                    items = honorarios,
                    totalSeccion = honorarios.sumOf { it.total },
                    estaExpandida = true,
                    alAlternar = {},
                    alAgregar = { itemAEditar = null; tipoHoja = TipoHojaPresupuesto.Honorario }
                ) { item, index ->
                    FilaResumenHonorario(honorario = item, onEditar = { itemAEditar = item; tipoHoja = TipoHojaPresupuesto.Honorario }, onEliminar = { honorarios.removeAt(index) })
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.primaryOrange.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, colors.primaryOrange.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.End) {
                        Text("RESUMEN TOTAL", style = MaterialTheme.typography.labelSmall, color = colors.primaryOrange, fontWeight = FontWeight.Bold)
                        Text("Subtotal: $ ${"%.2f".format(calc.subtotal)}", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                        Text("Total: $ ${"%.2f".format(calc.totalGeneral)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = colors.textPrimary)
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    if (tipoHoja != null) {
        ModalBottomSheet(onDismissRequest = { tipoHoja = null }) {
            when (tipoHoja) {
                TipoHojaPresupuesto.Articulo -> HojaAgregarArticulo(
                    itemAEditar = itemAEditar as? ArticuloPresupuesto,
                    itemsSugerencia = articulosSugeridos,
                    onAgregarItem = { articulos.add(it); tipoHoja = null },
                    onActualizarItem = { updated -> articulos[articulos.indexOfFirst { it.id == updated.id }] = updated; tipoHoja = null },
                    onGuardarEnCatalogo = onGuardarArticuloEnCatalogo,
                    onDescartar = { tipoHoja = null }
                )
                TipoHojaPresupuesto.Servicio -> HojaAgregarServicio(
                    itemAEditar = itemAEditar as? ServicioPresupuesto,
                    itemsSugerencia = serviciosSugeridos,
                    onAgregarItem = { servicios.add(it); tipoHoja = null },
                    onActualizarItem = { updated -> servicios[servicios.indexOfFirst { it.id == updated.id }] = updated; tipoHoja = null },
                    onGuardarEnCatalogo = onGuardarServicioEnCatalogo,
                    onDescartar = { tipoHoja = null }
                )
                TipoHojaPresupuesto.Honorario -> HojaAgregarHonorario(
                    itemAEditar = itemAEditar as? HonorarioPresupuesto,
                    onAgregarItem = { honorarios.add(it); tipoHoja = null },
                    onActualizarItem = { updated -> honorarios[honorarios.indexOfFirst { it.id == updated.id }] = updated; tipoHoja = null },
                    onDescartar = { tipoHoja = null }
                )
                TipoHojaPresupuesto.Secciones -> HojaSecciones(
                    mostrarArticulos = mostrarSeccionArticulos,
                    mostrarServicios = mostrarSeccionServicios,
                    mostrarHonorarios = mostrarSeccionHonorarios,
                    mostrarVarios = mostrarSeccionVarios,
                    mostrarImpuestos = mostrarSeccionImpuestos,
                    onMostrarArticulosCambio = { mostrarSeccionArticulos = it },
                    onMostrarServiciosCambio = { mostrarSeccionServicios = it },
                    onMostrarHonorariosCambio = { mostrarSeccionHonorarios = it },
                    onMostrarVariosCambio = { mostrarSeccionVarios = it },
                    onMostrarImpuestosCambio = { mostrarSeccionImpuestos = it }
                )
                TipoHojaPresupuesto.SelectorCliente -> HojaSelectorCliente(
                    clientes = clientes,
                    idClienteSeleccionado = idClienteSeleccionado,
                    onSeleccionarCliente = { idClienteSeleccionado = it.id; clienteNombre = it.titulo; tipoHoja = null },
                    onCerrar = { tipoHoja = null }
                )
                else -> {}
            }
        }
    }

    if (mostrarVistaPrevia && presupuestoPendiente != null && identidad != null) {
        com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog(
            prestador = identidad,
            relacion = presupuestoPendiente!!,
            onDismiss = { mostrarVistaPrevia = false },
            onEnviar = {
                onInsertarPresupuesto(presupuestoPendiente!!)
                onEnviarPresupuestoReal(presupuestoPendiente!!)
                mostrarVistaPrevia = false
                onVolver()
            },
            clientName = clienteNombre
        )
    }
}
*/



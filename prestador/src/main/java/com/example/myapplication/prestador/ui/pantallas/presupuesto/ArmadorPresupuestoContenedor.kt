package com.example.myapplication.prestador.ui.pantallas.presupuesto

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.core.dominio.motores.CalculadoraPresupuesto
import com.example.myapplication.prestador.datos.local.entidades.*
import com.example.myapplication.prestador.viewmodel.presupuesto.BorradorPresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.presupuesto.SeccionPresupuesto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmadorPresupuestoMobileLayout(
    borrador: BorradorPresupuestoEntity,
    articulos: List<ArticuloPresupuesto>,
    servicios: List<ServicioPresupuesto>,
    gastosVarios: List<GastoVarioPresupuesto>,
    calculos: CalculadoraPresupuesto.ResultadoCalculo,
    sugerencias: List<com.example.myapplication.core.dominio.modelos.ProductoDominio>,
    perfil: PrestadorDominio?,
    misIdentidades: List<PrestadorDominio>,
    categoriasVigentes: List<String>,
    infoCategoria: Pair<String, String>? = null,
    seccionActual: SeccionPresupuesto,
    datosCliente: com.example.myapplication.core.dominio.modelos.UsuarioDominio?,
    direccionesCliente: List<com.example.myapplication.core.dominio.modelos.DireccionDominio>,
    onCambiarSeccion: (SeccionPresupuesto) -> Unit,
    onVolver: () -> Unit,
    onVerCatalogo: () -> Unit,
    onActualizarBusqueda: (String) -> Unit,
    onAgregarArticulo: (ArticuloPresupuesto, Boolean, () -> Unit) -> Unit,
    onAgregarServicio: (ServicioPresupuesto, Boolean, () -> Unit) -> Unit,
    onAgregarGasto: (GastoVarioPresupuesto, Boolean, () -> Unit) -> Unit,
    onEliminarArticulo: (Long) -> Unit,
    onEliminarServicio: (Long) -> Unit,
    onEliminarGasto: (Long) -> Unit,
    onSeleccionarDireccion: (String) -> Unit,
    onActualizarDireccionManual: (String?, String?, String?, String?, String?, String?, String?) -> Unit,
    onActualizarMetodosPago: (String?) -> Unit,
    onActualizarValidez: (Int) -> Unit,
    onActualizarNotas: (String?) -> Unit,
    onActualizarCategoria: (String) -> Unit,
    onSeleccionarIdentidadEmisora: (String) -> Unit,
    validacionCatalogo: BorradorPresupuestoViewModel.ResultadoValidacionCatalogo,
    onValidarCatalogo: (() -> Unit) -> Unit,
    onSincronizarCatalogo: (List<ProductoEntity>) -> Unit,
    onLimpiarValidacion: () -> Unit,
    mostrarAlertaDuplicado: Boolean,
    onOcultarAlertaDuplicado: () -> Unit,
    onEnviar: (String?) -> Unit
) {
    var materialesExpandido by remember { mutableStateOf(true) }
    var manoObraExpandido by remember { mutableStateOf(true) }
    var gastosExpandido by remember { mutableStateOf(true) }

    var itemAEliminar by remember { mutableStateOf<Any?>(null) }
    var itemEnEdicion by remember { mutableStateOf<Any?>(null) }

    var mostrarDialogoPdf by remember { mutableStateOf(false) }
    var mostrarConfirmacionEnvio by remember { mutableStateOf(false) }
    var mostrarModalAgregarItem by remember { mutableStateOf(false) }
    var mostrarModalDireccion by remember { mutableStateOf(false) }
    
    var alicuotaIvaSeleccionada by remember { mutableStateOf(21.0) }
    var descuentoMonto by remember { mutableStateOf("0") }

    var mensajeToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(mensajeToast) {
        if (mensajeToast != null) {
            kotlinx.coroutines.delay(2000)
            mensajeToast = null
        }
    }

    val totalItems = articulos.size + servicios.size + gastosVarios.size

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            HeaderMobileBar(
                codigoPresupuesto = borrador.idBorrador.takeLast(8).uppercase(),
                onVolver = onVolver,
                onOpenPdf = { mostrarDialogoPdf = true }
            )
        },
        bottomBar = {
            BarraFlotanteInferior(
                calculos = calculos,
                seccionActual = seccionActual,
                onSiguiente = {
                    if (seccionActual == SeccionPresupuesto.ITEMS) {
                        onValidarCatalogo {
                            onCambiarSeccion(SeccionPresupuesto.TOTALES)
                        }
                    } else {
                        val proxima = when(seccionActual) {
                            SeccionPresupuesto.IDENTIDAD -> SeccionPresupuesto.ITEMS
                            SeccionPresupuesto.ITEMS -> SeccionPresupuesto.TOTALES
                            SeccionPresupuesto.TOTALES -> SeccionPresupuesto.TOTALES
                        }
                        onCambiarSeccion(proxima)
                    }
                },
                onEnviar = { 
                    if (totalItems > 0) {
                        mostrarConfirmacionEnvio = true 
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                NavegadorPasosMobile(
                    seccionActual = seccionActual,
                    cantidadItems = totalItems,
                    onCambiarSeccion = onCambiarSeccion
                )

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    AnimatedContent(
                        targetState = seccionActual,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_mobile_anim"
                    ) { seccion ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (seccion) {
                                SeccionPresupuesto.IDENTIDAD -> {
                                    item {
                                        TarjetaDetallesPresupuesto(
                                            numero = borrador.numeroPresupuesto ?: borrador.idBorrador.takeLast(8).uppercase(),
                                            fecha = System.currentTimeMillis(),
                                            idCategoria = borrador.idCategoria ?: "",
                                            nombreCategoria = infoCategoria?.first,
                                            iconoCategoria = infoCategoria?.second,
                                            sugerenciasCategorias = categoriasVigentes,
                                            onCategoriaChange = onActualizarCategoria
                                        )
                                    }
                                    item {
                                        TarjetaEmisorMobile(
                                            perfilActual = perfil,
                                            misIdentidades = misIdentidades,
                                            onSeleccionar = onSeleccionarIdentidadEmisora
                                        )
                                    }
                                    item {
                                        TarjetaClienteMobile(
                                            borrador = borrador,
                                            datosCliente = datosCliente,
                                            idDireccionSeleccionada = borrador.idDireccionCliente,
                                            direccionManual = borrador.direccionManual,
                                            direcciones = direccionesCliente,
                                            onSeleccionarDireccion = onSeleccionarDireccion,
                                            onActualizarDireccionManual = onActualizarDireccionManual,
                                            onCambiarCliente = { },
                                            onManualAddressClick = { mostrarModalDireccion = true }
                                        )
                                    }
                                    item {
                                        TarjetaValidezOferta(
                                            validezOferta = borrador.diasValidez,
                                            onValidezChange = onActualizarValidez
                                        )
                                    }
                                }

                                SeccionPresupuesto.ITEMS -> {
                                    item {
                                        HeaderAccionItems(
                                            cantidadItems = totalItems,
                                            onAgregarClick = { 
                                                itemEnEdicion = null
                                                mostrarModalAgregarItem = true 
                                            }
                                        )
                                    }
                                    
                                    item {
                                        SeccionExpandibleMobile(
                                            titulo = "MATERIALES",
                                            icono = Icons.Default.Inventory2,
                                            colorAcento = Color(0xFFF97316),
                                            conteo = articulos.size,
                                            expandido = materialesExpandido,
                                            onToggle = { materialesExpandido = !materialesExpandido }
                                        ) {
                                            SeccionListadoArticulosMobile(
                                                articulos = articulos,
                                                onEliminar = { item -> itemAEliminar = item },
                                                onEditar = { item -> 
                                                    itemEnEdicion = item
                                                    mostrarModalAgregarItem = true
                                                }
                                            )
                                        }
                                    }

                                    item {
                                        SeccionExpandibleMobile(
                                            titulo = "MANO DE OBRA",
                                            icono = Icons.Default.Build,
                                            colorAcento = Color(0xFF22D3EE),
                                            conteo = servicios.size,
                                            expandido = manoObraExpandido,
                                            onToggle = { manoObraExpandido = !manoObraExpandido }
                                        ) {
                                            SeccionListadoServiciosMobile(
                                                servicios = servicios,
                                                onEliminar = { item -> itemAEliminar = item },
                                                onEditar = { item -> 
                                                    itemEnEdicion = item
                                                    mostrarModalAgregarItem = true
                                                }
                                            )
                                        }
                                    }

                                    item {
                                        SeccionExpandibleMobile(
                                            titulo = "GASTOS VARIOS",
                                            icono = Icons.Default.LocalShipping,
                                            colorAcento = Color(0xFFFBBF24),
                                            conteo = gastosVarios.size,
                                            expandido = gastosExpandido,
                                            onToggle = { gastosExpandido = !gastosExpandido }
                                        ) {
                                            SeccionListadoGastosMobile(
                                                gastos = gastosVarios,
                                                onEliminar = { item -> itemAEliminar = item },
                                                onEditar = { item -> 
                                                    itemEnEdicion = item
                                                    mostrarModalAgregarItem = true
                                                }
                                            )
                                        }
                                    }
                                }

                                SeccionPresupuesto.TOTALES -> {
                                    item {
                                        TarjetaCondicionPagoMobile(
                                            condicionPago = borrador.metodosPago ?: "",
                                            onCondicionPagoChange = onActualizarMetodosPago
                                        )
                                    }
                                    item {
                                        TarjetaAjustesFinancieros(
                                            alicuotaIva = alicuotaIvaSeleccionada,
                                            onCambiarAlicuota = { alicuotaIvaSeleccionada = it },
                                            descuento = descuentoMonto,
                                            onDescuentoChange = { descuentoMonto = it },
                                            observaciones = borrador.notas ?: "",
                                            onObservacionesChange = onActualizarNotas
                                        )
                                    }
                                    item {
                                        TarjetaResumenCalculosMobile(calculos = calculos)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarModalAgregarItem) {
        ModalBottomSheetAgregarItemMobile(
            sugerencias = sugerencias,
            categoriasVigentes = categoriasVigentes,
            itemInicial = itemEnEdicion,
            onBusquedaChange = onActualizarBusqueda,
            onVerCatalogo = onVerCatalogo,
            onDismiss = { 
                mostrarModalAgregarItem = false
                itemEnEdicion = null 
            },
            onAgregarArticulo = { art ->
                onAgregarArticulo(art, itemEnEdicion != null) {
                    mensajeToast = if (itemEnEdicion != null) "Material actualizado" else "Material agregado"
                }
            },
            onAgregarServicio = { svc ->
                onAgregarServicio(svc, itemEnEdicion != null) {
                    mensajeToast = if (itemEnEdicion != null) "Servicio actualizado" else "Servicio agregado"
                }
            },
            onAgregarGasto = { gasto ->
                onAgregarGasto(gasto, itemEnEdicion != null) {
                    mensajeToast = if (itemEnEdicion != null) "Gasto actualizado" else "Gasto extra agregado"
                }
            }
        )
    }

    if (mostrarAlertaDuplicado) {
        AlertDialog(
            onDismissRequest = onOcultarAlertaDuplicado,
            title = { Text("Ítem Duplicado", fontWeight = FontWeight.Black) },
            text = { Text("Este material o servicio ya se encuentra agregado.") },
            confirmButton = {
                TextButton(onClick = onOcultarAlertaDuplicado) { Text("ENTENDIDO") }
            }
        )
    }

    if (mostrarModalDireccion) {
        ModalBottomSheetDireccionManual(
            borrador = borrador,
            onDismiss = { mostrarModalDireccion = false },
            onConfirm = { c, n, p, d, l, pr, cp ->
                onActualizarDireccionManual(c, n, p, d, l, pr, cp)
                mostrarModalDireccion = false
            }
        )
    }

    if (mostrarConfirmacionEnvio) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEnvio = false },
            title = { Text("¿Enviar Presupuesto?", fontWeight = FontWeight.Black) },
            text = { Text("Se enviará la cotización oficial al cliente. Asegúrate de haber revisado todos los ítems.") },
            confirmButton = {
                Button(
                    onClick = {
                        onEnviar(null)
                        mostrarConfirmacionEnvio = false
                        onVolver()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                ) {
                    Text("SÍ, ENVIAR AHORA", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEnvio = false }) { Text("CANCELAR") }
            }
        )
    }

    if (itemAEliminar != null) {
        AlertDialog(
            onDismissRequest = { itemAEliminar = null },
            title = { Text("¿Eliminar ítem?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (val item = itemAEliminar) {
                            is ArticuloPresupuesto -> onEliminarArticulo(item.id)
                            is ServicioPresupuesto -> onEliminarServicio(item.id)
                            is GastoVarioPresupuesto -> onEliminarGasto(item.id)
                        }
                        itemAEliminar = null
                    }
                ) {
                    Text("ELIMINAR", color = Color.Red, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemAEliminar = null }) { Text("CANCELAR") }
            }
        )
    }

    if (mostrarDialogoPdf && perfil != null) {
        val h = PresupuestoFinalEntity(
            idPresupuesto = borrador.idBorrador,
            idCliente = borrador.idBorrador,
            idPrestador = borrador.idPrestador,
            nombrePrestador = perfil.titulo,
            tituloTrabajo = borrador.tituloTrabajo.ifBlank { "Presupuesto de Servicio" },
            idCategoria = borrador.idCategoria,
            subtotal = calculos.subtotal,
            totalGeneral = calculos.totalGeneral,
            metodosPago = borrador.metodosPago,
            diasValidez = borrador.diasValidez,
            notas = borrador.notas,
            marcaTiempo = System.currentTimeMillis()
        )

        val lineas = mutableListOf<ProductoFinalEntity>()
        articulos.forEach { lineas.add(ProductoFinalEntity(idPresupuesto = h.idPresupuesto, nombreCopiado = it.descripcion, cantidad = it.cantidad, precioSnapshot = it.precioUnitario, tipoItem = TipoProductoFinal.PRODUCTO)) }
        servicios.forEach { lineas.add(ProductoFinalEntity(idPresupuesto = h.idPresupuesto, nombreCopiado = it.descripcion, cantidad = 1, precioSnapshot = it.precioUnitario, tipoItem = TipoProductoFinal.SERVICIO)) }
        gastosVarios.forEach { lineas.add(ProductoFinalEntity(idPresupuesto = h.idPresupuesto, nombreCopiado = it.descripcion, cantidad = 1, precioSnapshot = it.precioUnitario, tipoItem = TipoProductoFinal.GASTO)) }

        val wrap = PresupuestoConItems(cabecera = h, lineas = lineas, finanzas = emptyList())

        com.example.myapplication.uishared.ui.components.PlanillaPresupuestoA4Dialog(
            prestador = perfil ?: PrestadorDominio(titulo = "Yo"),
            relacion = wrap,
            clientName = datosCliente?.nombreVisible ?: "Cliente",
            onDismiss = { mostrarDialogoPdf = false },
            showSendButton = false
        )
    }

    if (validacionCatalogo.tieneCambios) {
        DialogoSincronizacionCatalogo(
            resultado = validacionCatalogo,
            onConfirmarSincronizacion = { lista ->
                onSincronizarCatalogo(lista)
                onCambiarSeccion(SeccionPresupuesto.TOTALES)
            },
            onContinuarSinGuardar = {
                onLimpiarValidacion()
                onCambiarSeccion(SeccionPresupuesto.TOTALES)
            },
            onDismiss = onLimpiarValidacion
        )
    }
}

@Composable
private fun DialogoSincronizacionCatalogo(
    resultado: BorradorPresupuestoViewModel.ResultadoValidacionCatalogo,
    onConfirmarSincronizacion: (List<ProductoEntity>) -> Unit,
    onContinuarSinGuardar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sincronización de Catálogo", fontSize = 18.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Se detectaron ítems nuevos o modificados. ¿Deseas actualizar tu catálogo privado?")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmarSincronizacion(resultado.nuevos + resultado.modificados) }) {
                Text("ACTUALIZAR", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onContinuarSinGuardar) { Text("CONTINUAR SIN GUARDAR") }
        }
    )
}


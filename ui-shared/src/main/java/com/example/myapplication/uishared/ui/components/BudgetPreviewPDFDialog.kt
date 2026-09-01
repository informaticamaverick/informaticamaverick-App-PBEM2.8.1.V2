package com.example.myapplication.uishared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import com.example.myapplication.uishared.estilos.SharedPalette
import androidx.compose.ui.graphics.asAndroidBitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.example.myapplication.uishared.ui.components.profile.parts.PrestadorPerfilMocks
import kotlinx.coroutines.launch
import java.util.Locale

// --- CONSTANTES DE DISEÑO A4 (Standard) ---
val A4_WIDTH = 450.dp
val A4_HEIGHT = 636.dp

data class PresupuestoRowDisplay(
    val cantidad: String,
    val descripcion: String,
    val unitario: String,
    val total: String,
    val esEspecial: Boolean = false
)

@Composable
fun BudgetA4Document(
    prestador: PrestadorDominio?,
    presupuesto: PresupuestoConItems,
    nombreCategoria: String? = null,
    nombreCliente: String = "Cliente",
    empresaCliente: String? = null,
    direccionCliente: String? = null,
    simboloMoneda: String = "$",
    idioma: Locale = Locale.getDefault(),
    esVistaPrevia: Boolean = LocalInspectionMode.current,
    capaCaptura: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
) {
    val filasVisualizacion = remember(presupuesto, simboloMoneda) {
        val filas = mutableListOf<PresupuestoRowDisplay>()
        presupuesto.lineas.forEach { item ->
            val esEspecial = item.tipoItem != TipoProductoFinal.PRODUCTO
            filas.add(PresupuestoRowDisplay(
                cantidad = if (esEspecial) "-" else item.cantidad.toString(),
                descripcion = item.nombreCopiado,
                unitario = if (esEspecial) "-" else "$simboloMoneda ${String.format(idioma, "%,.2f", item.precioSnapshot)}",
                total = "$simboloMoneda ${String.format(idioma, "%,.2f", item.precioSnapshot * item.cantidad)}",
                esEspecial = esEspecial
            ))
        }
        filas
    }

    Box(
        modifier = Modifier
            .width(A4_WIDTH)
            .height(A4_HEIGHT)
            .shadow(elevation = 12.dp)
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, SharedPalette.Slate300)
                .drawWithContent {
                    if (esVistaPrevia || capaCaptura == null) {
                        drawContent()
                    } else {
                        capaCaptura.record { this@drawWithContent.drawContent() }
                        drawLayer(capaCaptura)
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(SharedPalette.A4Gradient))

            if (prestador != null) {
                A4HeaderSection(prestador, presupuesto, nombreCategoria)
            } else {
                A4SimpleHeader(presupuesto)
            }

            HorizontalDivider(color = SharedPalette.Slate200)

            if (prestador != null) {
                A4ClientInfoSection(prestador, presupuesto, nombreCategoria, nombreCliente, empresaCliente, direccionCliente)
            } else {
                A4SimpleClientInfo(presupuesto, nombreCategoria, nombreCliente, empresaCliente, direccionCliente)
            }

            A4ItemsTable(filasVisualizacion)

            Spacer(modifier = Modifier.weight(1f))

            A4FooterSection(presupuesto, simboloMoneda, idioma)
        }
    }
}

@Composable
private fun A4SimpleHeader(presupuesto: PresupuestoConItems) {
    val h = presupuesto.cabecera
    val fechaActual = remember {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(java.util.Date())
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(SharedPalette.A4Gradient).padding(6.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text((h.nombreEmpresaPrestador ?: h.nombrePrestador).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Black, color = SharedPalette.Slate800, letterSpacing = (-0.5).sp, lineHeight = 16.sp)
                Text("SERVICIOS PROFESIONALES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate400, letterSpacing = 1.5.sp, lineHeight = 11.sp)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
            Box(modifier = Modifier.size(40.dp).border(2.dp, SharedPalette.Slate800, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) { Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = SharedPalette.Slate800) }
            Spacer(modifier = Modifier.height(4.dp))
            Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = SharedPalette.Slate600, letterSpacing = 0.5.sp)
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.padding(vertical = 3.dp).background(Color(0xFFDBEAFE)).border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("N° ${h.idPresupuesto.takeLast(8).uppercase()}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF1E40AF))
            }
            Text(fechaActual, fontSize = 10.sp, color = SharedPalette.Slate800, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun A4SimpleClientInfo(presupuesto: PresupuestoConItems, nombreCategoria: String?, nombreCliente: String, empresaCliente: String? = null, direccionCliente: String? = null) {
    val h = presupuesto.cabecera
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text((h.nombreEmpresaPrestador ?: h.nombrePrestador).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate800, lineHeight = 14.sp)
        HorizontalDivider(color = SharedPalette.Slate200, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate400)
                Text(nombreCliente, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate800, lineHeight = 13.sp)
                if (!empresaCliente.isNullOrBlank()) Text(empresaCliente, fontSize = 9.sp, color = SharedPalette.Slate600)
                if (!direccionCliente.isNullOrBlank()) Text(direccionCliente, fontSize = 9.sp, color = SharedPalette.Slate600)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                Text("TRABAJO / PROYECTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate400)
                Text(nombreCategoria ?: "Servicio Profesional", fontSize = 11.sp, color = SharedPalette.Slate800, lineHeight = 14.sp, textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = SharedPalette.Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun BudgetA4Viewer(
    prestador: PrestadorDominio?,
    presupuesto: PresupuestoConItems,
    alCerrar: () -> Unit,
    nombreCategoria: String? = null,
    nombreCliente: String = "Cliente",
    empresaCliente: String? = null,
    direccionCliente: String? = null,
    capaCapturaParaExterno: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    acciones: @Composable (BoxScope.(Float, Offset) -> Unit)? = null
) {
    val capaInterna = rememberGraphicsLayer()
    val capaCaptura = capaCapturaParaExterno ?: capaInterna
    val esVistaPrevia = LocalInspectionMode.current
    val idioma = remember { Locale.getDefault() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val anchoPantalla = maxWidth
        val escalaAjusteInicial = remember(anchoPantalla) { ((anchoPantalla - 32.dp) / A4_WIDTH).coerceAtMost(1f) }
        var escala by remember { mutableFloatStateOf(escalaAjusteInicial) }
        var desplazamiento by remember { mutableStateOf(Offset.Zero) }

        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF202020))) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(onClick = alCerrar, modifier = Modifier.align(Alignment.TopEnd).background(Color.White.copy(alpha = 0.9f), CircleShape).size(48.dp)) {
                    Icon(Icons.Default.Close, "Cerrar", tint = SharedPalette.Slate800)
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    escala = (escala * zoom).coerceIn(escalaAjusteInicial, 4f)
                    desplazamiento += pan
                }
            }) {
                Box(modifier = Modifier.align(Alignment.Center).graphicsLayer(scaleX = escala, scaleY = escala, translationX = desplazamiento.x, translationY = desplazamiento.y)) {
                    BudgetA4Document(
                        prestador = prestador,
                        presupuesto = presupuesto,
                        nombreCategoria = nombreCategoria,
                        nombreCliente = nombreCliente,
                        empresaCliente = empresaCliente,
                        direccionCliente = direccionCliente,
                        idioma = idioma,
                        esVistaPrevia = esVistaPrevia,
                        capaCaptura = capaCaptura
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2A2A2A)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.background(SharedPalette.Slate800.copy(alpha = 0.9f), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { escala = (escala * 0.8f).coerceAtLeast(escalaAjusteInicial); desplazamiento = Offset.Zero }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                        Text("${(escala / escalaAjusteInicial * 100).toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 50.dp), textAlign = TextAlign.Center)
                        IconButton(onClick = { escala = (escala * 1.25f).coerceAtMost(4f) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                    }
                    acciones?.invoke(this@Box, escala, desplazamiento)
                }
            }
        }
    }
}

@Composable
fun BudgetPreviewPDFDialog(
    prestador: PrestadorDominio? = null,
    budget: PresupuestoConItems,
    onDismiss: () -> Unit,
    onEnviar: ((String?) -> Unit)? = null,
    nombreCategoria: String? = null,
    clientName: String = "Cliente",
    showSendButton: Boolean = true
) {
    val capaCaptura = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BudgetA4Viewer(
            prestador = prestador, 
            presupuesto = budget, 
            alCerrar = onDismiss, 
            nombreCategoria = nombreCategoria,
            nombreCliente = clientName,
            capaCapturaParaExterno = capaCaptura
        ) { _, _ ->
            if (showSendButton) {
                Button(
                    onClick = { 
                        scope.launch {
                            try {
                                val bitmap = capaCaptura.toImageBitmap().asAndroidBitmap()
                                val outputStream = ByteArrayOutputStream()
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
                                onEnviar?.invoke(base64)
                            } catch (e: Exception) {
                                onEnviar?.invoke(null)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enviar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun A4HeaderSection(prestador: PrestadorDominio, presupuesto: PresupuestoConItems, nombreCategoria: String?) {
    val header = presupuesto.cabecera
    val fechaActual = remember {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(java.util.Date())
    }
    val nombreVisiblePrestador = (prestador.nombreSucursal ?: prestador.titulo).uppercase()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(SharedPalette.A4Gradient).padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Business, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(nombreVisiblePrestador, fontSize = 14.sp, fontWeight = FontWeight.Black, color = SharedPalette.Slate800, letterSpacing = (-0.5).sp, lineHeight = 16.sp)
                Text(nombreCategoria ?: "SERVICIOS PROFESIONALES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate400, letterSpacing = 1.5.sp, lineHeight = 11.sp)
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
            Box(modifier = Modifier.size(40.dp).border(2.dp, SharedPalette.Slate800, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                Text("X", fontSize = 26.sp, fontWeight = FontWeight.Black, color = SharedPalette.Slate800)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("PRESUPUESTO", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = SharedPalette.Slate600, letterSpacing = 0.5.sp)
        }

        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.padding(vertical = 3.dp).background(Color(0xFFDBEAFE)).border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("N° ${header.idPresupuesto.takeLast(8).uppercase()}", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = Color(0xFF1E40AF))
            }
            Text(fechaActual, fontSize = 10.sp, color = SharedPalette.Slate800, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun A4ClientInfoSection(prestador: PrestadorDominio, presupuesto: PresupuestoConItems, nombreCategoria: String?, nombreCliente: String, empresaCliente: String?, direccionCliente: String?) {
    val header = presupuesto.cabecera
    val nombreVisiblePrestador = (prestador.nombreSucursal ?: prestador.titulo).uppercase()
    val direccionCompletaPrestador = prestador.direccionVisible ?: ""

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(nombreVisiblePrestador, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate800, lineHeight = 14.sp)
            if (!prestador.cuitCuil.isNullOrBlank()) Text("CUIT ${prestador.cuitCuil}", fontSize = 9.sp, color = SharedPalette.Slate600)
            if (direccionCompletaPrestador.isNotBlank()) Text(direccionCompletaPrestador, fontSize = 9.sp, color = SharedPalette.Slate600, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!prestador.matricula.isNullOrBlank()) Text("Mat. ${prestador.matricula}", fontSize = 9.sp, color = SharedPalette.Slate600)
        }

        HorizontalDivider(color = SharedPalette.Slate200, thickness = 1.dp, modifier = Modifier.padding(vertical = 6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("CLIENTE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate400)
                Text(nombreCliente, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate800, lineHeight = 13.sp)
                if (!empresaCliente.isNullOrBlank()) Text(empresaCliente, fontSize = 9.sp, color = SharedPalette.Slate600)
                if (!direccionCliente.isNullOrBlank()) Text(direccionCliente, fontSize = 9.sp, color = SharedPalette.Slate600)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End) {
                Text("TRABAJO / PROYECTO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate400)
                Text(nombreCategoria ?: "Proyecto de servicio", fontSize = 11.sp, color = SharedPalette.Slate800, lineHeight = 14.sp, textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = SharedPalette.Slate300, thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun A4ItemsTable(filas: List<PresupuestoRowDisplay>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.border(1.dp, SharedPalette.Slate300)) {
            Row(modifier = Modifier.background(SharedPalette.Slate100).height(IntrinsicSize.Min)) {
                A4TableCell("Cant", 0.15f, esCabecera = true)
                A4TableCell("Descripción", 0.55f, esCabecera = true)
                A4TableCell("Unitario", 0.3f, esCabecera = true, alinearDerecha = true)
                A4TableCell("Total", 0.3f, esCabecera = true, alinearDerecha = true, esUltimo = true)
            }
            HorizontalDivider(color = SharedPalette.Slate300)

            filas.forEach { fila ->
                val fondo = if (fila.esEspecial) Color(0xFFEFF6FF) else Color.White
                val peso = if (fila.esEspecial) FontWeight.Bold else FontWeight.Normal
                Row(modifier = Modifier.background(fondo).height(IntrinsicSize.Min)) {
                    A4TableCell(fila.cantidad, 0.15f, color = SharedPalette.Slate600)
                    A4TableCell(fila.descripcion, 0.55f, pesoFuente = peso)
                    A4TableCell(fila.unitario, 0.3f, alinearDerecha = true, color = SharedPalette.Slate600)
                    A4TableCell(fila.total, 0.3f, alinearDerecha = true, pesoFuente = FontWeight.Bold, esUltimo = true)
                }
                HorizontalDivider(color = SharedPalette.Slate300)
            }
        }
    }
}

@Composable
fun RowScope.A4TableCell(
    texto: String,
    peso: Float,
    esCabecera: Boolean = false,
    alinearDerecha: Boolean = false,
    esUltimo: Boolean = false,
    color: Color = Color.Unspecified,
    pesoFuente: FontWeight? = null
) {
    Box(
        modifier = Modifier.weight(peso).fillMaxHeight().then(if (!esUltimo) Modifier.border(width = 0.5.dp, color = SharedPalette.Slate300.copy(alpha = 0.5f)) else Modifier).padding(6.dp),
        contentAlignment = if (alinearDerecha) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Text(text = texto, fontSize = if (esCabecera) 9.sp else 10.sp, fontWeight = pesoFuente ?: (if (esCabecera) FontWeight.Bold else FontWeight.Normal), color = if (color == Color.Unspecified) SharedPalette.Slate800 else color, textAlign = if (alinearDerecha) TextAlign.End else TextAlign.Start)
    }
}

@Composable
fun A4FooterSection(presupuesto: PresupuestoConItems, simboloMoneda: String, idioma: Locale) {
    val header = presupuesto.cabecera
    Column(modifier = Modifier.fillMaxWidth().background(SharedPalette.Slate50).padding(horizontal = 24.dp, vertical = 16.dp)) {
        if (header.tiempoEjecucion != null || header.infoGarantia != null || header.metodosPago != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text("CONDICIONES COMERCIALES", fontSize = 8.sp, fontWeight = FontWeight.Black, color = SharedPalette.BlueStart)
                header.tiempoEjecucion?.let { Text("• Ejecución: $it", fontSize = 9.sp, color = SharedPalette.Slate600) }
                header.infoGarantia?.let { Text("• Garantía: $it", fontSize = 9.sp, color = SharedPalette.Slate600) }
                header.metodosPago?.let { Text("• Pago: $it", fontSize = 9.sp, color = SharedPalette.Slate600) }
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = SharedPalette.Slate200)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(text = header.notas ?: "Nota: Los precios están expresados en moneda local.\nVálido por ${header.diasValidez} días.", fontSize = 10.sp, color = SharedPalette.Slate400, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, lineHeight = 14.sp, modifier = Modifier.width(180.dp))

            Column(modifier = Modifier.width(200.dp).shadow(2.dp, RoundedCornerShape(4.dp)).background(Color.White, RoundedCornerShape(4.dp)).border(1.dp, SharedPalette.Slate300, RoundedCornerShape(4.dp)).padding(12.dp)) {
                FilaTotalFooter("Subtotal:", "$simboloMoneda ${String.format(idioma, "%,.2f", header.subtotal)}")
                if (presupuesto.finanzas.isNotEmpty()) {
                    presupuesto.finanzas.forEach { tax ->
                        FilaTotalFooter("${tax.etiqueta}:", "+ $simboloMoneda ${String.format(idioma, "%,.2f", tax.monto)}")
                    }
                } else if (header.totalImpuestos > 0) {
                    FilaTotalFooter("Impuestos:", "+ $simboloMoneda ${String.format(idioma, "%,.2f", header.totalImpuestos)}")
                }
                if (header.totalDescuentos > 0) FilaTotalFooter("Descuento:", "- $simboloMoneda ${String.format(idioma, "%,.2f", header.totalDescuentos)}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = SharedPalette.Slate200)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SharedPalette.Slate800)
                    Text("$simboloMoneda ${String.format(idioma, "%,.2f", header.totalGeneral)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SharedPalette.BlueEnd)
                }
            }
        }
    }
}

@Composable
fun FilaTotalFooter(etiqueta: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, fontSize = 10.sp, color = SharedPalette.Slate600)
        Text(valor, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SharedPalette.Slate800)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewBudgetA4Document() {
    val h = PresupuestoFinalEntity(
        idPresupuesto = "presup_test_12345678",
        nombrePrestador = "Elena Rodríguez",
        idCategoria = "INTERIORES",
        subtotal = 50200.0,
        totalImpuestos = 10542.0,
        totalGeneral = 60742.0,
        diasValidez = 15,
        metodosPago = "Transferencia, Efectivo",
        tiempoEjecucion = "10 días hábiles",
        infoGarantia = "6 meses por defectos de fabricación"
    )
    
    val samplePresupuesto = PresupuestoConItems(
        cabecera = h,
        lineas = listOf(
            ProductoFinalEntity(idPresupuesto = "1", nombreCopiado = "Escritorio de Madera", cantidad = 1, precioSnapshot = 15000.0),
            ProductoFinalEntity(idPresupuesto = "1", nombreCopiado = "Silla Ergonómica", cantidad = 2, precioSnapshot = 8500.0, tipoItem = TipoProductoFinal.SERVICIO)
        ),
        finanzas = listOf(
            FinanzaFinalEntity(idPresupuesto = "1", etiqueta = "IVA 21%", monto = 10542.0, tipo = TipoFinanzaFinal.IMPUESTO)
        )
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Gray).padding(16.dp), contentAlignment = Alignment.Center) {
        BudgetA4Document(
            prestador = PrestadorPerfilMocks.elenaRodriguez,
            presupuesto = samplePresupuesto,
            nombreCliente = "Juan Pérez",
            empresaCliente = "Tech Solutions S.A.",
            direccionCliente = "Av. Siempre Viva 742"
        )
    }
}

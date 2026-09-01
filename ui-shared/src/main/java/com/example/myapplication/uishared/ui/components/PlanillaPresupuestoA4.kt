package com.example.myapplication.uishared.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.*
import com.example.myapplication.core.datos.local.entidades.relaciones.PresupuestoConItems
import com.example.myapplication.core.dominio.modelos.PrestadorDominio
import android.util.Base64
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PLANILLA DE PRESUPUESTO A4 (COTIZAPRO EXECUTIVE) ---
 */

// Modelo interno para que la UI sea "tonta" y reciba todo calculado
data class ItemRender(
    val item: ItemPresupuesto,
    val index: Int,
    val esPrimeroDeCategoria: Boolean,
    val esUltimoDeCategoria: Boolean
)

@Composable
fun PlanillaPresupuestoA4(
    prestador: PrestadorDominio?,
    relacion: PresupuestoConItems,
    nombreCategoria: String? = null, // 🔥 [ELITE]
    nombreCliente: String = "Cliente",
    clientCompany: String? = null,
    clientAddress: String? = null,
    capaCaptura: androidx.compose.ui.graphics.layer.GraphicsLayer? = null
) {
    BloquearEscaladoFuente {
        val locale = Locale.getDefault()
        val sdf = remember { SimpleDateFormat("dd/MM/yyyy", locale) }
        val presupuesto = relacion.cabecera
        val lineas = relacion.lineas

        val model = remember(relacion, prestador, nombreCliente, clientCompany, clientAddress, nombreCategoria) {
            val emisorNombre = (prestador?.nombreSucursal ?: prestador?.titulo ?: presupuesto.nombrePrestador).uppercase()
            val vencimientoMillis = presupuesto.marcaTiempo + (presupuesto.diasValidez * 24 * 60 * 60 * 1000L)
            
            Presupuesto(
                numeroPresupuesto = presupuesto.numeroPresupuesto ?: presupuesto.idPresupuesto.takeLast(8).uppercase(),
                fechaEmision = sdf.format(Date(presupuesto.marcaTiempo)),
                fechaVencimiento = sdf.format(Date(vencimientoMillis)),
                diasValidez = presupuesto.diasValidez,
                alcanceProyecto = presupuesto.tituloTrabajo ?: nombreCategoria ?: "Servicio Profesional Integral",
                prestador = PrestadorServicio(
                    nombreEmpresa = emisorNombre,
                    identificacionFiscal = prestador?.cuitCuil ?: "",
                    direccion = prestador?.direccionVisible ?: "",
                    email = prestador?.correo ?: "",
                    telefono = prestador?.numeroTelefono ?: "",
                    urlFoto = prestador?.urlMiniatura ?: prestador?.urlFoto
                ),
                cliente = Cliente(
                    nombreOEmpresa = nombreCliente,
                    identificacionFiscal = clientCompany ?: "", 
                    contacto = "Titular Responsable",
                    direccion = clientAddress ?: "" 
                ),
                items = lineas.map { 
                    ItemPresupuesto(
                        categoria = when(it.tipoItem) {
                            TipoProductoFinal.PRODUCTO -> CategoriaItem.INSUMOS_PRODUCTOS
                            TipoProductoFinal.SERVICIO -> CategoriaItem.MANO_DE_OBRA
                            else -> CategoriaItem.GASTOS_VARIOS
                        },
                        descripcion = it.nombreCopiado,
                        cantidad = it.cantidad,
                        precioUnitario = it.precioSnapshot,
                        porcentajeDescuento = it.porcentajeDescuento,
                        montoDescuentoFijo = 0.0 // No guardado en FinalEntity individualmente
                    )
                }.sortedBy { it.categoria.ordinal },
                porcentajeDescuento = 0.0,
                monedaSimbolo = presupuesto.moneda,
                tipo = presupuesto.tipo
            )
        }

        // --- PROCESAMIENTO DE ÍTEMS (Indexación invisible) ---
        val itemsRender = model.items.mapIndexed { i, item ->
            ItemRender(
                item = item,
                index = i + 1,
                esPrimeroDeCategoria = i == 0 || model.items[i-1].categoria != item.categoria,
                esUltimoDeCategoria = i == model.items.size - 1 || model.items[i+1].categoria != item.categoria
            )
        }

        // --- LÓGICA DE PAGINACIÓN INTELIGENTE ---
        val paginas = mutableListOf<List<ItemRender>>()
        if (itemsRender.isEmpty()) {
            paginas.add(emptyList())
        } else {
            var cursor = 0
            var esPrimera = true
            while (cursor < itemsRender.size) {
                val restante = itemsRender.size - cursor
                val capacidadMaxima = if (esPrimera) 6 else 12 // Más margen de seguridad
                
                val itemsATomar = if (restante <= capacidadMaxima) {
                    // Es la presunta última página. ¿Cabe el Pie de Página?
                    val limiteParaFooter = if (esPrimera) 4 else 7
                    if (restante > limiteParaFooter) {
                        // No caben cómodos con el footer, cortamos antes
                        if (esPrimera) 4 else 7
                    } else {
                        restante
                    }
                } else {
                    capacidadMaxima
                }

                paginas.add(itemsRender.subList(cursor, cursor + itemsATomar))
                cursor += itemsATomar
                esPrimera = false
            }
        }
        
        // Verificación final para espacio del footer
        if (paginas.isNotEmpty() && paginas.last().isNotEmpty()) {
            val limiteSeguro = if (paginas.size == 1) 4 else 7
            if (paginas.last().size > limiteSeguro) {
                paginas.add(emptyList())
            }
        }

        Column(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .drawWithContent {
                    if (capaCaptura == null) {
                        drawContent()
                    } else {
                        capaCaptura.record(
                            density = this,
                            layoutDirection = layoutDirection,
                            size = IntSize(size.width.toInt(), size.height.toInt())
                        ) {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(capaCaptura)
                    }
                },
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            paginas.forEachIndexed { index, itemsDePagina ->
                HojaPresupuestoA4Pagina(
                    presupuesto = model,
                    itemsPagina = itemsDePagina,
                    esPrimera = index == 0,
                    esUltima = index == paginas.size - 1,
                    numeroPagina = index + 1,
                    totalPaginas = paginas.size
                )
            }
        }
    }
}

// --- MODELO DE DATOS INTERNO ---

enum class CategoriaItem(
    val titulo: String,
    val icono: String,
    val colorFondo: Color,
    val colorTexto: Color
) {
    INSUMOS_PRODUCTOS(
        titulo = "Productos, Materiales e Insumos",
        icono = "📦",
        colorFondo = Color(0xFFFFFBEB),
        colorTexto = Color(0xFF78350F)
    ),
    MANO_DE_OBRA(
        titulo = "Mano de Obra y Servicios Profesionales",
        icono = "🛠️",
        colorFondo = Color(0xFFEFF6FF),
        colorTexto = Color(0xFF1E3A8A)
    ),
    GASTOS_VARIOS(
        titulo = "Gastos Varios, Viáticos y Logística",
        icono = "🚗",
        colorFondo = Color(0xFFECFDF5),
        colorTexto = Color(0xFF064E3B)
    )
}

data class ItemPresupuesto(
    val id: String = UUID.randomUUID().toString(),
    val categoria: CategoriaItem = CategoriaItem.MANO_DE_OBRA,
    var descripcion: String,
    var cantidad: Int,
    var precioUnitario: Double,
    var porcentajeDescuento: Double = 0.0,
    var montoDescuentoFijo: Double = 0.0
) {
    val esDescuentoPorcentaje: Boolean get() = montoDescuentoFijo <= 0 && porcentajeDescuento > 0
    val esDescuentoFijo: Boolean get() = montoDescuentoFijo > 0

    val montoDescuentoCalculado: Double get() = if (esDescuentoFijo) {
        montoDescuentoFijo
    } else {
        precioUnitario * (porcentajeDescuento / 100.0)
    }

    val precioConDescuento: Double get() = (precioUnitario - montoDescuentoCalculado).coerceAtLeast(0.0)
    val totalLinea: Double get() = cantidad * precioConDescuento
}

data class PrestadorServicio(
    val nombreEmpresa: String,
    val identificacionFiscal: String,
    val direccion: String,
    val email: String,
    val telefono: String,
    val urlFoto: Any? = null
)

data class Cliente(
    val nombreOEmpresa: String,
    val identificacionFiscal: String,
    val contacto: String,
    val direccion: String
)

data class Presupuesto(
    val numeroPresupuesto: String,
    val fechaEmision: String,
    val fechaVencimiento: String,
    val diasValidez: Int,
    val alcanceProyecto: String,
    val prestador: PrestadorServicio,
    val cliente: Cliente,
    val items: List<ItemPresupuesto>,
    val porcentajeIva: Double = 21.0,
    val porcentajeDescuento: Double = 0.0,
    val porcentajeAnticipo: Double = 50.0,
    val datosBancarios: String = "Banco Maverick • Consultar para transferencia.",
    val monedaSimbolo: String = "$",
    val tipo: TipoPresupuesto = TipoPresupuesto.NUEVO
) {
    val subtotalGeneral: Double get() = items.sumOf { it.totalLinea }
    val montoDescuento: Double get() = subtotalGeneral * (porcentajeDescuento / 100.0)
    val subtotalConDescuento: Double get() = subtotalGeneral - montoDescuento
    val montoIva: Double get() = subtotalConDescuento * (porcentajeIva / 100.0)
    val totalGeneral: Double get() = subtotalConDescuento + montoIva
    val montoAnticipo: Double get() = totalGeneral * (porcentajeAnticipo / 100.0)
    val saldoRestante: Double get() = totalGeneral - montoAnticipo

    fun subtotalPorCategoria(cat: CategoriaItem): Double {
        return items.filter { it.categoria == cat }.sumOf { it.totalLinea }
    }
}

// --- VISTA PRINCIPAL (SIMULACIÓN A4) ---

val EXECUTIVE_A4_WIDTH = 595.dp
val EXECUTIVE_A4_HEIGHT = 842.dp

@Composable
fun HojaPresupuestoA4Pagina(
    presupuesto: Presupuesto,
    itemsPagina: List<ItemRender>,
    esPrimera: Boolean,
    esUltima: Boolean,
    numeroPagina: Int,
    totalPaginas: Int
) {
    Surface(
        modifier = Modifier.size(width = EXECUTIVE_A4_WIDTH, height = EXECUTIVE_A4_HEIGHT),
        shape = RoundedCornerShape(2.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- SECCIÓN 1: IDENTIDAD (Solo en Pág 1) ---
            if (esPrimera) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Card(
                        modifier = Modifier.weight(2.2f).fillMaxHeight(),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (presupuesto.prestador.urlFoto != null) {
                                        AsyncImage(model = presupuesto.prestador.urlFoto, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(text = presupuesto.prestador.nombreEmpresa.take(1), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(text = presupuesto.prestador.nombreEmpresa, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), letterSpacing = (-0.5).sp)
                                    if (presupuesto.prestador.identificacionFiscal.isNotBlank()) {
                                        Text(text = "CUIL/CUIT: ${presupuesto.prestador.identificacionFiscal}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(11.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = acortarDireccion(presupuesto.prestador.direccion), fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MailOutline, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(11.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = presupuesto.prestador.email, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(0.8f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color(0xFFF1F5F9))) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.End) {
                            val etiquetaTipo = when(presupuesto.tipo) {
                                TipoPresupuesto.CONCURSO -> "PRESUPUESTO CONCURSO"
                                TipoPresupuesto.CONVERSACION -> "PRESUPUESTO CHAT"
                                TipoPresupuesto.RAPIDO -> "PRESUPUESTO RÁPIDO"
                                else -> "PRESUPUESTO"
                            }
                            Text(etiquetaTipo, fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D4ED8))
                            Text(text = "N° ${presupuesto.numeroPresupuesto}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        }
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color(0xFFF1F5F9))) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(text = "Emisión:", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(4.dp))
                                    Text(text = presupuesto.fechaEmision, fontSize = 10.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(10.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = "Validez:", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(4.dp))
                                    Text(text = presupuesto.fechaVencimiento, fontSize = 10.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color(0xFFF1F5F9))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                VerticalDivider(modifier = Modifier.height(12.dp).padding(horizontal = 8.dp), color = Color(0xFFCBD5E1))
                                Text("DATOS DEL CLIENTE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                            }
                            Spacer(Modifier.width(24.dp))
                            Text(presupuesto.cliente.nombreOEmpresa.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (!presupuesto.cliente.identificacionFiscal.isNullOrBlank()) {
                                val idFiscal = presupuesto.cliente.identificacionFiscal.replace("CUIT/CUIL:", "", ignoreCase = true).trim()
                                if (idFiscal.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Badge, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("CUIL / CUIT: ", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                        Text(idFiscal, fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(Modifier.width(32.dp))
                            if (!presupuesto.cliente.direccion.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(acortarDireccion(presupuesto.cliente.direccion), fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            // --- SECCIÓN 2: DESGLOSE DE TRABAJO (Ítems) ---
            Column(modifier = Modifier.weight(1f)) {
                // Cabecera de Tabla
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("DESCRIPCIÓN DE ÍTEMS / ENTREGABLES", modifier = Modifier.weight(2.2f), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E40AF))
                    Text("CANT.", modifier = Modifier.weight(0.4f), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E40AF), textAlign = TextAlign.Center)
                    Text("PRECIO U.", modifier = Modifier.weight(0.7f), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E40AF), textAlign = TextAlign.End)
                    Text("TOTAL", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E40AF), textAlign = TextAlign.End)
                }

                itemsPagina.forEach { render ->
                    val item = render.item
                    
                    if (render.esPrimeroDeCategoria) {
                        Surface(color = item.categoria.colorFondo, modifier = Modifier.fillMaxWidth().padding(top = 1.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${item.categoria.icono} ${item.categoria.titulo.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = item.categoria.colorTexto)
                            }
                        }
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(2.2f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                Text(text = item.descripcion, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), lineHeight = 14.sp, platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                if (item.esDescuentoPorcentaje || item.esDescuentoFijo) {
                                    val txt = if (item.esDescuentoPorcentaje) "Descuento ${item.porcentajeDescuento.toInt()}% (-${presupuesto.monedaSimbolo}${String.format("%.2f", item.montoDescuentoCalculado)})" else "Descuento Especial (-${presupuesto.monedaSimbolo}${String.format("%.2f", item.montoDescuentoFijo)})"
                                    Text(text = txt, style = TextStyle(color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp, platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                }
                            }
                            Text(text = "${item.cantidad}", modifier = Modifier.weight(0.4f), fontSize = 12.sp, textAlign = TextAlign.Center, color = Color(0xFF334155))
                            Column(modifier = Modifier.weight(0.7f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                if (item.esDescuentoPorcentaje || item.esDescuentoFijo) {
                                    Text(text = "${presupuesto.monedaSimbolo}${String.format("%.2f", item.precioUnitario)}", style = TextStyle(fontSize = 9.sp, color = Color(0xFF94A3B8), textDecoration = TextDecoration.LineThrough, platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                    Text(text = "${presupuesto.monedaSimbolo}${String.format("%.2f", item.precioConDescuento)}", style = TextStyle(fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                } else {
                                    Text(text = "${presupuesto.monedaSimbolo}${String.format("%.2f", item.precioUnitario)}", style = TextStyle(fontSize = 12.sp, color = Color(0xFF64748B), platformStyle = PlatformTextStyle(includeFontPadding = false)))
                                }
                            }
                            Text(text = "${presupuesto.monedaSimbolo}${String.format("%.2f", item.totalLinea)}", modifier = Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, color = Color(0xFF0F172A))
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                    }

                    if (render.esUltimoDeCategoria) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            val lbl = when(item.categoria) {
                                CategoriaItem.MANO_DE_OBRA -> "Subtotal Mano:"
                                CategoriaItem.INSUMOS_PRODUCTOS -> "Subtotal Productos:"
                                CategoriaItem.GASTOS_VARIOS -> "Subtotal Gastos:"
                            }
                            Text(text = lbl, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color(0xFF64748B))
                            Spacer(Modifier.width(16.dp))
                            Text(text = "${presupuesto.monedaSimbolo} ${String.format("%.2f", presupuesto.subtotalPorCategoria(item.categoria))}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                        }
                    }
                }
            }

            // --- SECCIÓN 3: TOTALES Y CONDICIONES (Solo en la Última Página) ---
            if (esUltima) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text("CONDICIONES Y MÉTODOS DE PAGO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                            Spacer(Modifier.height(4.dp))
                            Text(text = presupuesto.datosBancarios, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
                        }
                        if (presupuesto.alcanceProyecto.isNotBlank()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NOTAS ADICIONALES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))
                                Spacer(Modifier.height(4.dp))
                                Text(text = presupuesto.alcanceProyecto, fontSize = 10.sp, color = Color(0xFF64748B), maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)), border = BorderStroke(1.dp, Color(0xFFDBEAFE))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("CONDICIÓN DE INICIO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E40AF))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Anticipo (${presupuesto.porcentajeAnticipo.toInt()}%):", fontSize = 11.sp, color = Color(0xFF475569))
                                Text(text = "${presupuesto.monedaSimbolo} ${String.format("%,.2f", presupuesto.montoAnticipo)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Saldo restante: ${presupuesto.monedaSimbolo} ${String.format("%,.2f", presupuesto.saldoRestante)}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("RESUMEN CONSOLIDADO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(8.dp))
                                FilaConsolidadoDoc("Subtotal General", presupuesto.subtotalGeneral)
                                FilaConsolidadoDoc("IVA (${presupuesto.porcentajeIva.toInt()}%)", presupuesto.montoIva)
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(modifier = Modifier.fillMaxWidth().border(2.dp, Color(0xFF2563EB), RoundedCornerShape(8.dp)).padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("TOTAL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                        Text(text = "${presupuesto.monedaSimbolo} ${String.format("%,.2f", presupuesto.totalGeneral)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = "Página $numeroPagina de $totalPaginas", fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

private fun acortarDireccion(original: String): String {
    val partes = original.split(",")
    return if (partes.size >= 2) "${partes[0].trim()}, ${partes[1].trim()}" else original
}

@Composable
private fun FilaConsolidadoDoc(label: String, monto: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
        Text("${if(monto < 0) "- " else ""}$ ${String.format("%,.2f", Math.abs(monto))}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun PlanillaPresupuestoA4Viewer(
    prestador: PrestadorDominio?,
    relacion: PresupuestoConItems,
    alCerrar: () -> Unit,
    nombreCategoria: String? = null, // 🔥 [ELITE]
    nombreCliente: String = "Cliente",
    empresaCliente: String? = null,
    direccionCliente: String? = null,
    capaCapturaParaExterno: androidx.compose.ui.graphics.layer.GraphicsLayer? = null,
    acciones: @Composable (BoxScope.(Float, Offset) -> Unit)? = null
) {
    val capaInterna = rememberGraphicsLayer()
    val capaCaptura = capaCapturaParaExterno ?: capaInterna
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val anchoDisponible = this.maxWidth
        val escalaAjusteInicial = remember(anchoDisponible) { ((anchoDisponible - 32.dp) / EXECUTIVE_A4_WIDTH).coerceAtMost(1f) }
        var escala by remember { mutableFloatStateOf(escalaAjusteInicial) }
        var desplazamiento by remember { mutableStateOf(Offset.Zero) }
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF111111))) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Black.copy(alpha = 0.4f), tonalElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = alCerrar, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Vista Previa Profesional", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Formato A4 Executive", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                    Row(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { escala = (escala * 0.8f).coerceAtLeast(escalaAjusteInicial); desplazamiento = Offset.Zero }) { Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        Text("${(escala / escalaAjusteInicial * 100).toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.widthIn(min = 40.dp), textAlign = TextAlign.Center)
                        IconButton(onClick = { escala = (escala * 1.25f).coerceAtMost(4f) }) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth().graphicsLayer(clip = true).pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> escala = (escala * zoom).coerceIn(escalaAjusteInicial, 4f); desplazamiento = desplazamiento.copy(y = desplazamiento.y + pan.y) } }, contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.wrapContentSize(unbounded = true).requiredWidth(EXECUTIVE_A4_WIDTH).graphicsLayer(scaleX = escala, scaleY = escala, translationY = desplazamiento.y)) {
                    PlanillaPresupuestoA4(
                        prestador = prestador, 
                        relacion = relacion, 
                        nombreCategoria = nombreCategoria,
                        nombreCliente = nombreCliente, 
                        clientCompany = empresaCliente, 
                        clientAddress = direccionCliente, 
                        capaCaptura = capaCaptura
                    )
                }
            }
            if (acciones != null) Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.6f)).padding(16.dp)) { acciones(escala, desplazamiento) }
        }
    }
}

@Composable
fun PlanillaPresupuestoA4Dialog(
    prestador: PrestadorDominio,
    relacion: PresupuestoConItems,
    onDismiss: () -> Unit,
    onEnviar: ((String?) -> Unit)? = null,
    nombreCategoria: String? = null, // 🔥 [ELITE]
    clientName: String = "Cliente",
    clientCompany: String? = null,
    clientAddress: String? = null,
    showSendButton: Boolean = true
) {
    val capaCaptura = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        PlanillaPresupuestoA4Viewer(
            prestador = prestador, 
            relacion = relacion, 
            alCerrar = onDismiss, 
            nombreCategoria = nombreCategoria,
            nombreCliente = clientName, 
            empresaCliente = clientCompany, 
            direccionCliente = clientAddress, 
            capaCapturaParaExterno = capaCaptura
        ) { _, _ ->
            if (showSendButton) Button(onClick = { scope.launch { try { val bitmap = capaCaptura.toImageBitmap().asAndroidBitmap(); val outputStream = ByteArrayOutputStream(); bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream); val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT); onEnviar?.invoke(base64) } catch (_: Exception) { onEnviar?.invoke(null) } } }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)), shape = RoundedCornerShape(16.dp)) { Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text("ENVIAR PRESUPUESTO PROFESIONAL", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp) }
        }
    }
}

data class ItemSimpleUI(val descripcion: String, val cantidad: Int, val precioTotal: Double)
data class SeccionCategoriaUI(val titulo: String, val icono: String, val colorFondo: Color, val colorTexto: Color, val subtotalSeccion: Double, val items: List<ItemSimpleUI>)

@Preview(showBackground = true, widthDp = 700, heightDp = 1100)
@Composable
fun PreviewPresupuestoUI() {
    val sample = Presupuesto(numeroPresupuesto = "PROP-2026-904", fechaEmision = "1/8/2026", fechaVencimiento = "16/8/2026", diasValidez = 15, alcanceProyecto = "Proyecto de prueba", prestador = PrestadorServicio("Apex Tech Services S.R.L.", "30-71882910-4", "Torre Corporativa Catalinas", "contacto@apextech.io", "+54 11 5900-2026"), cliente = Cliente("Grupo Financiero Global", "30-66442211-9", "Ing. Mariana Rojas", "Av. Santa Fe 1250"), items = listOf(ItemPresupuesto(categoria = CategoriaItem.MANO_DE_OBRA, descripcion = "Arquitectura", cantidad = 1, precioUnitario = 1200.0)))
    MaterialTheme { Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B)).padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { val itemsPre = sample.items.mapIndexed { i, it -> ItemRender(it, i+1, i==0, i == sample.items.size-1) }; HojaPresupuestoA4Pagina(presupuesto = sample, itemsPagina = itemsPre, esPrimera = true, esUltima = true, numeroPagina = 1, totalPaginas = 1) } }
}


package com.example.myapplication.prestador.ui.pantallas.chat.componentes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.datos.local.entidades.PresupuestoFinalEntity
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.EquipoTrabajoDominio
import com.example.myapplication.core.utilidades.CalendarUtils
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad
import com.example.myapplication.prestador.viewmodel.chat.GestionEventosViewModel
import com.example.myapplication.uishared.estilos.SharedPalette
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- HOJA DE NUEVA VISITA TÉCNICA (v2026.ELITE) ---
 * [LEY #10]: UI Tonta. Consume estados formateados del ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaVisitaTecSheet(
    idSucursal: String,
    nombrePrestador: String,
    nombreCliente: String,
    urlFotoCliente: String? = null,
    categoriaServicio: String? = null,
    iconoCategoria: String? = null,
    listaPresupuestosChat: List<com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio> = emptyList(),
    listaDireccionesChat: List<MensajeEntity> = emptyList(),
    equipoDisponible: List<EquipoTrabajoDominio> = emptyList(),
    direccionInicial: MensajeEntity? = null,
    alCerrar: () -> Unit,
    alCrearEquipo: () -> Unit = {},
    alConfirmar: (fecha: String, hora: String, direccion: String, equipoIds: List<String>, idPresupuesto: String?) -> Unit,
    viewModel: GestionEventosViewModel = hiltViewModel()
) {
    val bloques by viewModel.bloquesDisponibles.collectAsState()
    val cargando by viewModel.estaCargando.collectAsState()
    val gastosEstimados by viewModel.costoEstimadoTraslado.collectAsState()
    val fechaFormateada by viewModel.fechaFormateada.collectAsState()
    val fechaMillis by viewModel.fechaSeleccionadaMillis.collectAsState()

    NuevaVisitaTecSheetContent(
        nombrePrestador = nombrePrestador,
        nombreCliente = nombreCliente,
        urlFotoCliente = urlFotoCliente,
        categoriaServicio = categoriaServicio,
        iconoCategoria = iconoCategoria,
        listaPresupuestosChat = listaPresupuestosChat,
        listaDireccionesChat = listaDireccionesChat,
        equipoDisponible = equipoDisponible,
        direccionInicial = direccionInicial,
        bloquesDisponibles = bloques,
        estaCargando = cargando,
        gastosEstimados = gastosEstimados,
        fechaTexto = fechaFormateada,
        fechaMillis = fechaMillis,
        alCerrar = alCerrar,
        alCrearEquipo = alCrearEquipo,
        alConfirmar = alConfirmar,
        onRecalcularDisponibilidad = { viewModel.establecerFecha(it, idSucursal) },
        onEstimarGastos = { lat, lng ->
            viewModel.estimarGastosVisita(lat, lng, idSucursal)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaVisitaTecSheetContent(
    nombrePrestador: String,
    nombreCliente: String,
    urlFotoCliente: String? = null,
    categoriaServicio: String? = null,
    iconoCategoria: String? = null,
    listaPresupuestosChat: List<com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio> = emptyList(),
    listaDireccionesChat: List<MensajeEntity> = emptyList(),
    equipoDisponible: List<EquipoTrabajoDominio> = emptyList(),
    direccionInicial: MensajeEntity? = null,
    bloquesDisponibles: List<CalculadoraDisponibilidad.BloqueHorario>,
    estaCargando: Boolean,
    gastosEstimados: Double,
    fechaTexto: String,
    fechaMillis: Long,
    alCerrar: () -> Unit,
    alCrearEquipo: () -> Unit,
    alConfirmar: (fecha: String, hora: String, direccion: String, equipoIds: List<String>, idPresupuesto: String?) -> Unit,
    onRecalcularDisponibilidad: (Long) -> Unit,
    onEstimarGastos: (Double, Double) -> Unit
) {
    val colorAcento = Color(0xFF00E5FF) // Cian Técnico
    val colorSuperficie = SharedPalette.EliteSurface
    val colorFondo = SharedPalette.EliteMainBackground

    // --- ESTADOS ---
    var presupuestoSeleccionado by remember { mutableStateOf<com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio?>(null) }
    var mostrarMenuPresupuestos by remember { mutableStateOf(false) }

    var mostrarMenuEquipo by remember { mutableStateOf(false) }
    var mostrarGuiaEquipo by remember { mutableStateOf(false) }

    var direccionSeleccionadaTexto by remember { mutableStateOf(direccionInicial?.direccionTexto ?: "") }
    var latitudSeleccionada by remember { mutableStateOf(direccionInicial?.latitud) }
    var longitudSeleccionada by remember { mutableStateOf(direccionInicial?.longitud) }
    var mostrarMenuDirecciones by remember { mutableStateOf(false) }

    var horaSeleccionada by remember { mutableStateOf("") }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    val equipoSeleccionadoIds = remember { mutableStateListOf<String>() }

    // Gatillo Recálculo y Estimación Inicial
    LaunchedEffect(direccionInicial) {
        direccionInicial?.let { 
            onEstimarGastos(it.latitud ?: 0.0, it.longitud ?: 0.0)
        }
    }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = colorFondo,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.1f)) },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER PROFESIONAL ---
            Text(
                text = "GESTIÓN DE VISITA TÉCNICA",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Text(
                text = "PROTOCOLO DE SERVICIO A DOMICILIO",
                fontSize = 11.sp,
                color = colorAcento,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: DATOS DEL PRESTADOR ---
            SeccionInfoElite(
                titulo = "DATOS DEL PRESTADOR",
                icono = Icons.Default.Engineering,
                colorAcento = colorAcento
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = nombrePrestador.uppercase(),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    categoriaServicio?.let { 
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Text(iconoCategoria ?: "🛠️", fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = it.uppercase(),
                                color = colorAcento.copy(0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN: VÍNCULO COMERCIAL (PRESUPUESTOS) ---
            SeccionInfoElite(
                titulo = "RELACIONAR PRESUPUESTO",
                icono = Icons.Default.Description,
                colorAcento = Color(0xFFFACC15) // Amarillo Oro
            ) {
                Column(
                    modifier = Modifier
                        .clickable { mostrarMenuPresupuestos = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(Color(0xFFFACC15).copy(0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (presupuestoSeleccionado != null) Icons.Default.CheckCircle else Icons.Default.Link,
                                contentDescription = null,
                                tint = if (presupuestoSeleccionado != null) Color(0xFF10B981) else Color(0xFFFACC15),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = presupuestoSeleccionado?.tituloTrabajo ?: "Vincular presupuesto del chat...",
                                color = if (presupuestoSeleccionado != null) Color.White else Color.White.copy(0.4f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (presupuestoSeleccionado != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "ID: ${presupuestoSeleccionado!!.numeroPresupuesto ?: presupuestoSeleccionado!!.idPresupuesto.takeLast(8).uppercase()}",
                                        color = Color(0xFFFACC15),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "TOTAL: $ ${String.format(Locale.getDefault(), "%,.2f", presupuestoSeleccionado!!.totalGeneral)}",
                                        color = Color.White.copy(0.6f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFFFACC15), modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(
                        expanded = mostrarMenuPresupuestos,
                        onDismissRequest = { mostrarMenuPresupuestos = false },
                        modifier = Modifier.background(colorSuperficie).border(1.dp, Color.White.copy(0.1f))
                    ) {
                        if (listaPresupuestosChat.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Aquí aparecen los presupuestos que enviaste", color = Color.Gray, fontSize = 12.sp) },
                                onClick = { mostrarMenuPresupuestos = false }
                            )
                        } else {
                            listaPresupuestosChat.sortedByDescending { it.fechaTimestamp }.forEach { pre ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(pre.tituloTrabajo ?: "Presupuesto sin título", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.weight(1f))
                                                Text("$ ${String.format(Locale.getDefault(), "%,.2f", pre.totalGeneral)}", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("ID: ${pre.numeroPresupuesto ?: pre.idPresupuesto.takeLast(8).uppercase()}", color = Color(0xFFFACC15), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                Spacer(Modifier.width(8.dp))
                                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(pre.fechaTimestamp)), color = Color.Gray, fontSize = 10.sp)
                                            }
                                        }
                                    },
                                    onClick = {
                                        presupuestoSeleccionado = pre
                                        mostrarMenuPresupuestos = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Description, null, tint = Color(0xFFFACC15), modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN: EQUIPO TÉCNICO ---
            SeccionInfoElite(
                titulo = "EQUIPO ASIGNADO",
                icono = Icons.Default.Groups,
                colorAcento = colorAcento
            ) {
                Column(
                    modifier = Modifier
                        .clickable { if (equipoDisponible.isNotEmpty()) mostrarMenuEquipo = true }
                        .padding(16.dp)
                ) {
                    if (equipoDisponible.isEmpty()) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(nombrePrestador, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                Surface(color = colorAcento.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                                    Text("RESPONSABLE ÚNICO", color = colorAcento, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Deseas agregar un equipo de trabajo?",
                                color = colorAcento,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                modifier = Modifier.clickable { mostrarGuiaEquipo = true }
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (equipoSeleccionadoIds.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = if (equipoSeleccionadoIds.isNotEmpty()) Color(0xFF10B981) else colorAcento,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                val textoEquipo = if (equipoSeleccionadoIds.isEmpty()) "Seleccionar personal técnico..." 
                                                 else "${equipoSeleccionadoIds.size} integrantes seleccionados"
                                Text(
                                    text = textoEquipo,
                                    color = if (equipoSeleccionadoIds.isNotEmpty()) Color.White else Color.White.copy(0.4f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                        }

                        DropdownMenu(
                            expanded = mostrarMenuEquipo,
                            onDismissRequest = { mostrarMenuEquipo = false },
                            modifier = Modifier.background(colorSuperficie).border(1.dp, Color.White.copy(0.1f)).width(280.dp)
                        ) {
                            equipoDisponible.forEach { emp ->
                                val seleccionado = equipoSeleccionadoIds.contains(emp.id)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${emp.nombre} ${emp.apellido}", color = Color.White, fontSize = 14.sp)
                                            Spacer(Modifier.weight(1f))
                                            if (emp.cargo.isNotBlank()) {
                                                Text(emp.cargo, color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (seleccionado) equipoSeleccionadoIds.remove(emp.id)
                                        else equipoSeleccionadoIds.add(emp.id)
                                    },
                                    leadingIcon = {
                                        Checkbox(checked = seleccionado, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = colorAcento))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- SECCIÓN: DATOS DEL CLIENTE ---
            SeccionInfoElite(
                titulo = "DATOS DEL CLIENTE",
                icono = Icons.Default.Person,
                colorAcento = Color(0xFF3B82F6)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!urlFotoCliente.isNullOrBlank()) {
                        AsyncImage(
                            model = urlFotoCliente,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.05f)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(40.dp).background(Color(0xFF3B82F6).copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(nombreCliente.take(1).uppercase(), color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(nombreCliente, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN: UBICACIÓN DE DESTINO ---
            SeccionInfoElite(
                titulo = "UBICACIÓN DE VISITA",
                icono = Icons.Default.Place,
                colorAcento = colorAcento
            ) {
                val direccionesFiltradas = listaDireccionesChat
                
                Column(
                    modifier = Modifier
                        .clickable { mostrarMenuDirecciones = true }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).background(colorAcento.copy(0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (direccionSeleccionadaTexto.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Place,
                                contentDescription = null,
                                tint = if (direccionSeleccionadaTexto.isNotBlank()) Color(0xFF10B981) else colorAcento,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = direccionSeleccionadaTexto.ifBlank { "Seleccionar dirección enviada..." },
                            color = if (direccionSeleccionadaTexto.isNotBlank()) Color.White else Color.White.copy(0.4f),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                    }

                    if (gastosEstimados > 0 && direccionSeleccionadaTexto.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(color = colorAcento.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(
                                "GASTOS DE TRASLADO ESTIMADOS: $ ${String.format(Locale.getDefault(), "%,.2f", gastosEstimados)}",
                                color = colorAcento,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = mostrarMenuDirecciones,
                        onDismissRequest = { mostrarMenuDirecciones = false },
                        modifier = Modifier.background(colorSuperficie).border(1.dp, Color.White.copy(0.1f))
                    ) {
                        if (direccionesFiltradas.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay direcciones en el chat", color = Color.Gray, fontSize = 12.sp) },
                                onClick = { mostrarMenuDirecciones = false }
                            )
                        } else {
                            direccionesFiltradas.forEach { msg ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(msg.direccionTexto ?: "Ubicación sin nombre", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(msg.marcaTiempo)), color = Color.Gray, fontSize = 10.sp)
                                        }
                                    },
                                    onClick = {
                                        direccionSeleccionadaTexto = msg.direccionTexto ?: ""
                                        latitudSeleccionada = msg.latitud
                                        longitudSeleccionada = msg.longitud
                                        onEstimarGastos(msg.latitud ?: 0.0, msg.longitud ?: 0.0)
                                        mostrarMenuDirecciones = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = colorAcento, modifier = Modifier.size(18.dp)) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- CONFIGURACIÓN DE TIEMPO ---
            Text("PROGRAMACIÓN DE VISITA", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = { mostrarDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                color = colorSuperficie,
                border = BorderStroke(1.dp, Color.White.copy(0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(colorAcento.copy(0.1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.CalendarMonth, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FECHA SELECCIONADA", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            text = fechaTexto,
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = colorAcento.copy(0.7f), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (estaCargando) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorAcento)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(bloquesDisponibles) { bloque ->
                        val seleccionado = horaSeleccionada == bloque.horaTexto
                        val habilitado = !bloque.estaOcupado
                        Surface(
                            onClick = { if (habilitado) horaSeleccionada = bloque.horaTexto },
                            shape = RoundedCornerShape(12.dp),
                            color = if (seleccionado) colorAcento else if (!habilitado) Color.White.copy(0.02f) else colorSuperficie,
                            border = BorderStroke(1.dp, if (seleccionado) colorAcento else Color.White.copy(0.08f)),
                            enabled = habilitado
                        ) {
                            Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(bloque.horaTexto, color = if (seleccionado) Color.Black else if (!habilitado) Color.White.copy(0.2f) else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val dStr = CalendarUtils.formatIsoDate(fechaMillis)
                    alConfirmar(dStr, horaSeleccionada, direccionSeleccionadaTexto, equipoSeleccionadoIds.toList(), presupuestoSeleccionado?.idPresupuesto)
                },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 16.dp),
                enabled = horaSeleccionada.isNotBlank() && direccionSeleccionadaTexto.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento, contentColor = Color.Black)
            ) {
                Text("ENVIAR PROPUESTA DE VISITA", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- DATE PICKER MODAL ---
    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
                    return utcTimeMillis >= calendar.timeInMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onRecalcularDisponibilidad(it); horaSeleccionada = "" }
                    mostrarDatePicker = false
                }) { Text("SELECCIONAR", color = colorAcento, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("CANCELAR", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(containerColor = colorSuperficie)
        ) { DatePicker(state = datePickerState) }
    }

    if (mostrarGuiaEquipo) {
        GuiaEquipoDialog(
            onDismiss = { mostrarGuiaEquipo = false },
            onCrear = {
                mostrarGuiaEquipo = false
                alCrearEquipo()
            }
        )
    }
}

@Composable
fun GuiaEquipoDialog(
    onDismiss: () -> Unit,
    onCrear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Groups, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(40.dp)) },
        title = {
            Text(
                "GESTIÓN DE EQUIPO",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    "Un equipo de trabajo te permite delegar visitas a tus empleados o colaboradores.",
                    color = Color.White.copy(0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Puedes configurar tu personal desde la sección 'Empresa' dentro de tu Perfil Profesional.",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCrear,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CREAR EQUIPO", fontWeight = FontWeight.Black, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1E1E2E),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun SeccionInfoElite(
    titulo: String,
    icono: ImageVector,
    colorAcento: Color,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, null, tint = colorAcento, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = titulo, fontSize = 10.sp, color = colorAcento, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SharedPalette.EliteSurface,
            border = BorderStroke(1.dp, Color.White.copy(0.08f)),
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Preview
@Composable
private fun PreviewNuevaVisitaTec() {
    val mockBloques = listOf(
        CalculadoraDisponibilidad.BloqueHorario("09:00", false, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("09:30", true, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("10:00", false, 0, 0)
    )
    
    NuevaVisitaTecSheetContent(
        nombrePrestador = "Maverick Hunter",
        nombreCliente = "Juan Pérez",
        bloquesDisponibles = mockBloques,
        estaCargando = false,
        gastosEstimados = 750.0,
        fechaTexto = "JUEVES 30 DE JULIO, 2026",
        fechaMillis = System.currentTimeMillis(),
        alCerrar = {},
        alCrearEquipo = {},
        direccionInicial = null,
        alConfirmar = { _, _, _, _, _ -> },
        onRecalcularDisponibilidad = {},
        onEstimarGastos = { _, _ -> },
        categoriaServicio = "Instalación Eléctrica",
        iconoCategoria = "⚡"
    )
}

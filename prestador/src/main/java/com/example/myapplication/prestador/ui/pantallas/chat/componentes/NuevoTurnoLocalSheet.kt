package com.example.myapplication.prestador.ui.pantallas.chat.componentes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.style.TextDecoration
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.RecursoDominio
import com.example.myapplication.core.utilidades.CalendarUtils
import com.example.myapplication.core.dominio.motores.CalculadoraDisponibilidad
import com.example.myapplication.prestador.viewmodel.chat.GestionEventosViewModel
import com.example.myapplication.uishared.estilos.SharedPalette
import java.util.*

/**
 * --- HOJA DE NUEVO TURNO LOCAL (v2026.ELITE) ---
 * [LEY #10]: UI Tonta. Consume estados formateados del ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoTurnoLocalSheet(
    idSucursal: String,
    nombrePrestador: String,
    nombreCliente: String,
    direccionesPrestador: List<DireccionDominio>,
    recursosDisponibles: List<RecursoDominio>,
    alCerrar: () -> Unit,
    alConfirmar: (fecha: String, hora: String, direccion: String, idRecurso: String?, nombreRecurso: String?, categoria: String?) -> Unit,
    alCrearRecurso: () -> Unit = {},
    urlFotoCliente: String? = null,
    categoriaServicio: String? = null,
    iconoCategoria: String? = null,
    viewModel: GestionEventosViewModel = hiltViewModel()
) {
    val bloques by viewModel.bloquesDisponibles.collectAsState()
    val cargando by viewModel.estaCargando.collectAsState()
    val fechaFormateada by viewModel.fechaFormateada.collectAsState()
    val fechaMillis by viewModel.fechaSeleccionadaMillis.collectAsState()

    NuevoTurnoLocalSheetContent(
        nombrePrestador = nombrePrestador,
        nombreCliente = nombreCliente,
        direccionesPrestador = direccionesPrestador,
        recursosDisponibles = recursosDisponibles,
        bloquesDisponibles = bloques,
        estaCargando = cargando,
        fechaTexto = fechaFormateada,
        fechaMillis = fechaMillis,
        alCerrar = alCerrar,
        alConfirmar = { f, h, d, ir, nr -> alConfirmar(f, h, d, ir, nr, categoriaServicio) },
        alCrearRecurso = alCrearRecurso,
        urlFotoCliente = urlFotoCliente,
        categoriaServicio = categoriaServicio,
        iconoCategoria = iconoCategoria,
        alCambiarFecha = { viewModel.establecerFecha(it, idSucursal) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoTurnoLocalSheetContent(
    nombrePrestador: String,
    nombreCliente: String,
    direccionesPrestador: List<DireccionDominio>,
    recursosDisponibles: List<RecursoDominio>,
    bloquesDisponibles: List<CalculadoraDisponibilidad.BloqueHorario>,
    estaCargando: Boolean,
    fechaTexto: String,
    fechaMillis: Long,
    alCerrar: () -> Unit,
    alConfirmar: (fecha: String, hora: String, direccion: String, idRecurso: String?, nombreRecurso: String?) -> Unit,
    alCrearRecurso: () -> Unit,
    alCambiarFecha: (Long) -> Unit,
    urlFotoCliente: String? = null,
    categoriaServicio: String? = null,
    iconoCategoria: String? = null
) {
    val colorAcento = Color(0xFFA855F7) // Púrpura Elite
    val colorSuperficie = SharedPalette.EliteSurface
    val colorFondo = SharedPalette.EliteMainBackground

    // --- ESTADOS ---
    var direccionSeleccionada by remember {
        mutableStateOf(direccionesPrestador.firstOrNull()?.let { 
            it.etiqueta.ifBlank { it.aTextoCorto() }
        } ?: "")
    }
    var mostrarMenuDirecciones by remember { mutableStateOf(false) }
    var mostrarGuiaRecursos by remember { mutableStateOf(false) }
    
    var horaSeleccionada by remember { mutableStateOf("") }
    var recursoSeleccionado by remember(recursosDisponibles) { mutableStateOf(recursosDisponibles.firstOrNull()) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

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
                text = "GESTIÓN DE TURNO LOCAL",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Text(
                text = "PROTOCOLO DE ATENCIÓN EN SEDE",
                fontSize = 11.sp,
                color = colorAcento,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: DATOS DEL LOCAL ---
            SeccionInfoElite(
                titulo = "DATOS DEL LOCAL",
                icono = Icons.Default.Store,
                colorAcento = colorAcento
            ) {
                Column(
                    modifier = Modifier
                        .clickable(enabled = direccionesPrestador.size > 1) { mostrarMenuDirecciones = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nombrePrestador.uppercase(),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            categoriaServicio?.let { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(iconoCategoria ?: "🔧", fontSize = 12.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = it.uppercase(),
                                        color = colorAcento.copy(0.9f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = direccionSeleccionada,
                                    color = Color.White.copy(0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                        if (direccionesPrestador.size > 1) {
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = mostrarMenuDirecciones,
                        onDismissRequest = { mostrarMenuDirecciones = false },
                        modifier = Modifier.background(colorSuperficie).border(1.dp, Color.White.copy(0.1f))
                    ) {
                        direccionesPrestador.forEach { dir ->
                            val label = dir.etiqueta.ifBlank { dir.aTextoCorto() }
                            DropdownMenuItem(
                                text = { Text(label, color = Color.White, fontSize = 14.sp) },
                                onClick = {
                                    direccionSeleccionada = label
                                    mostrarMenuDirecciones = false
                                },
                                leadingIcon = { Icon(Icons.Default.Place, null, tint = colorAcento, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN: DATOS DEL CLIENTE ---
            SeccionInfoElite(
                titulo = "DATOS DEL CLIENTE",
                icono = Icons.Default.Person,
                colorAcento = Color(0xFF3B82F6) // Azul Profesional
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!urlFotoCliente.isNullOrBlank()) {
                        AsyncImage(
                            model = urlFotoCliente,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.1f)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF3B82F6).copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                nombreCliente.take(1).uppercase(),
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = nombreCliente,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- CONFIGURACIÓN DEL TURNO ---
            Text(
                "CONFIGURACIÓN DEL TURNO",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(0.4f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Fecha (Profesional)
            Surface(
                onClick = { mostrarDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                color = colorSuperficie,
                border = BorderStroke(1.dp, Color.White.copy(0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorAcento.copy(0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FECHA SELECCIONADA", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            text = fechaTexto,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = colorAcento.copy(0.7f), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECURSO
            Text("RECURSO / ESTACIÓN", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            if (recursosDisponibles.isEmpty()) {
                Surface(
                    onClick = { mostrarGuiaRecursos = true },
                    shape = RoundedCornerShape(12.dp),
                    color = colorSuperficie,
                    border = BorderStroke(1.dp, Color.White.copy(0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, null, tint = colorAcento, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "¿Quieres agregar un recurso?",
                            color = colorAcento,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(recursosDisponibles) { res ->
                        val esSeleccionado = recursoSeleccionado?.id == res.id
                        Surface(
                            onClick = { recursoSeleccionado = res; horaSeleccionada = "" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (esSeleccionado) colorAcento.copy(0.15f) else colorSuperficie,
                            border = BorderStroke(1.dp, if (esSeleccionado) colorAcento else Color.White.copy(0.08f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Settings, null, 
                                    tint = if (esSeleccionado) colorAcento else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(res.nombre, color = Color.White, fontSize = 13.sp, fontWeight = if (esSeleccionado) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DISPONIBILIDAD
            Text("HORARIOS DISPONIBLES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            if (estaCargando) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorAcento)
                }
            } else if (bloquesDisponibles.isEmpty()) {
                Surface(
                    color = Color.Red.copy(0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No hay disponibilidad para este día.", 
                        color = Color.Red.copy(0.8f), 
                        fontSize = 13.sp, 
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.heightIn(max = 400.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false // El padre ya tiene scroll
                ) {
                    items(bloquesDisponibles) { bloque ->
                        val seleccionado = horaSeleccionada == bloque.horaTexto
                        val habilitado = !bloque.estaOcupado
                        
                        Surface(
                            onClick = { if (habilitado) horaSeleccionada = bloque.horaTexto },
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                seleccionado -> colorAcento
                                !habilitado -> Color.White.copy(0.02f)
                                else -> colorSuperficie
                            },
                            border = BorderStroke(
                                width = 1.dp, 
                                color = if (seleccionado) colorAcento else Color.White.copy(0.08f)
                            ),
                            enabled = habilitado
                        ) {
                            Box(modifier = Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = bloque.horaTexto, 
                                    color = when {
                                        seleccionado -> Color.Black
                                        !habilitado -> Color.White.copy(0.2f)
                                        else -> Color.White
                                    },
                                    fontWeight = if (seleccionado) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val dStr = CalendarUtils.formatIsoDate(fechaMillis)
                    alConfirmar(dStr, horaSeleccionada, direccionSeleccionada, recursoSeleccionado?.id, recursoSeleccionado?.nombre)
                },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 8.dp),
                enabled = horaSeleccionada.isNotBlank() && !estaCargando,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorAcento, 
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(0.05f),
                    disabledContentColor = Color.White.copy(0.2f)
                )
            ) {
                Text("GENERAR PROPUESTA DE TURNO", fontWeight = FontWeight.Black, fontSize = 14.sp)
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
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    return utcTimeMillis >= calendar.timeInMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        alCambiarFecha(it)
                        horaSeleccionada = "" // Reset hora al cambiar fecha
                    }
                    mostrarDatePicker = false
                }) {
                    Text("SELECCIONAR", color = colorAcento, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = colorSuperficie
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarGuiaRecursos) {
        GuiaRecursoDialog(
            onDismiss = { mostrarGuiaRecursos = false },
            onCrear = {
                mostrarGuiaRecursos = false
                alCrearRecurso()
            }
        )
    }
}

@Composable
fun GuiaRecursoDialog(
    onDismiss: () -> Unit,
    onCrear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.SettingsSuggest, null, tint = Color(0xFFA855F7), modifier = Modifier.size(40.dp)) },
        title = {
            Text(
                "¿QUÉ ES UN RECURSO?",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    "Un recurso representa la unidad donde se realiza el trabajo (Ej: Consultorio 1, Elevador A, Estación de Belleza).",
                    color = Color.White.copy(0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Sirve para gestionar tu agenda de forma automática, evitando que se solapen turnos en el mismo espacio físico.",
                    color = Color.White.copy(0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Para proponer un turno, primero debes configurar tu estructura en el Perfil Profesional.",
                    color = Color(0xFFA855F7),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCrear,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CREAR ESTRUCTURA", fontWeight = FontWeight.Bold)
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
            Text(
                text = titulo,
                fontSize = 10.sp,
                color = colorAcento,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
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
private fun PreviewNuevoTurnoLocal() {
    val mockBloques = listOf(
        CalculadoraDisponibilidad.BloqueHorario("09:00", false, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("09:30", true, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("10:00", false, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("10:30", false, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("11:00", true, 0, 0),
        CalculadoraDisponibilidad.BloqueHorario("11:30", false, 0, 0)
    )

    NuevoTurnoLocalSheetContent(
        nombrePrestador = "Taller Maverick Elite",
        nombreCliente = "Juan Pérez",
        direccionesPrestador = listOf(DireccionDominio(calle = "Av. Siempre Viva", numero = "742", etiqueta = "Sucursal Principal")),
        recursosDisponibles = emptyList(),
        bloquesDisponibles = mockBloques,
        estaCargando = false,
        fechaTexto = "LUNES 28 DE JULIO, 2026",
        fechaMillis = System.currentTimeMillis(),
        alCerrar = {},
        alConfirmar = { _, _, _, _, _ -> },
        alCrearRecurso = {},
        alCambiarFecha = {},
        categoriaServicio = "INFORMÁTICA (TÉCNICO)",
        iconoCategoria = "💻"
    )
}

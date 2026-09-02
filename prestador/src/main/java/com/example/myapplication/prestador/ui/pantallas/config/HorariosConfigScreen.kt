package com.example.myapplication.prestador.ui.pantallas.config

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.core.dominio.modelos.HorarioDominio
import com.example.myapplication.core.dominio.modelos.RangoHorarioDominio
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.viewmodel.calendar.GestionHorariosViewModel
import java.util.UUID

/**
 * Contexto táctico para el selector de hora.
 */
data class PickerContext(
    val dia: Int? = null,
    val indexRango: Int? = null,
    val isStart: Boolean = true,
    val isMasivo: Boolean = false,
    val initialTime: String = "08:00"
)

/**
 * --- CONFIGURACIÓN DE HORARIOS ELITE (v2026.FINAL) ---
 * [PROPÓSITO]: Gestión ágil de disponibilidad con soporte para edición masiva.
 * [LEY #9]: Estándar Mav en Español.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosConfigScreen(
    onBack: () -> Unit,
    disponibilidadViewModel: GestionHorariosViewModel = hiltViewModel()
) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722),
        primaryOrangeDark = Color(0xFFF4511E),
        primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712),
        surfaceColor = Color(0xFF0F172A),
        surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B),
        chipBackground = Color(0xFF1E293B),
        chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444),
        success = Color(0xFF10B981)
    )
    val horarioBase by disponibilidadViewModel.horarioBase.collectAsStateWithLifecycle()
    
    // --- ESTADO DE EDICIÓN MÁSIVA ---
    var diasSeleccionadosMasivo by remember { mutableStateOf(setOf<Int>()) }
    var rangosBorradorMasivo by remember { mutableStateOf(listOf(RangoHorarioDominio("08:00", "18:00"))) }
    
    var pickerContext by remember { mutableStateOf<PickerContext?>(null) }
    val colorAcento = Color(0xFFFF5722)

    var mostrarConfirmacionSalida by remember { mutableStateOf(false) }
    val hayCambios = disponibilidadViewModel.hayCambios() 

    BackHandler(enabled = true) {
        if (hayCambios) mostrarConfirmacionSalida = true else onBack()
    }

    if (mostrarConfirmacionSalida) {
        com.example.myapplication.uishared.ui.components.profile.parts.DialogoSalidaSegura(
            onGuardar = {
                mostrarConfirmacionSalida = false
                // El guardado final se hace en la pantalla de Perfil al sincronizar,
                // pero aquí debemos al menos cerrar la pantalla sabiendo que el borrador tiene los datos.
                onBack()
            },
            onDescartar = {
                mostrarConfirmacionSalida = false
                disponibilidadViewModel.revertirCambios() // Necesitamos esta función
                onBack()
            },
            onCancelar = {
                mostrarConfirmacionSalida = false
            }
        )
    }

    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF020617).copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.surfaceColor, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text("CONFIGURAR HORARIOS", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary, letterSpacing = 0.5.sp)
                        Text("Gestión de disponibilidad", fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
            }
        },
        bottomBar = {
            // --- BOTONES DE CONTROL MAESTRO (Grandes Ligas) ---
            Surface(
                color = colors.backgroundColor,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))
                    ) {
                        Text("CANCELAR TODO", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = {
                            // En esta arquitectura, los cambios ya impactan en Room mediante el ViewModel
                            // pero podríamos forzar una sincronización final aquí si fuera necesario.
                            onBack()
                        },
                        modifier = Modifier.weight(1.5f).height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
                    ) {
                        Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // --- 1. SECCIÓN DE CONFIGURACIÓN RÁPIDA (HERRAMIENTA ELITE) ---
            item {
                ConfiguracionRapidaHorario(
                    diasSeleccionados = diasSeleccionadosMasivo,
                    onDiasChange = { diasSeleccionadosMasivo = it },
                    rangos = rangosBorradorMasivo,
                    onAddRange = { rangosBorradorMasivo = rangosBorradorMasivo + RangoHorarioDominio("09:00", "13:00") },
                    onRemoveRange = { r -> rangosBorradorMasivo = rangosBorradorMasivo - r },
                    onEditTime = { index, isStart -> 
                        val time = if (isStart) rangosBorradorMasivo[index].inicio else rangosBorradorMasivo[index].fin
                        pickerContext = PickerContext(indexRango = index, isStart = isStart, isMasivo = true, initialTime = time)
                    },
                    onApply = { 
                        disponibilidadViewModel.aplicarHorarioADias(diasSeleccionadosMasivo.toList(), rangosBorradorMasivo)
                        diasSeleccionadosMasivo = emptySet()
                    }
                )
            }

            item {
                Text(
                    "VISTA SEMANAL DETALLADA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                    letterSpacing = 1.sp
                )
            }

            // --- 2. LISTA DE DÍAS INDIVIDUALES ---
            items((1..7).toList()) { dia ->
                val rangos = when(dia) {
                    1 -> horarioBase?.lunes ?: emptyList()
                    2 -> horarioBase?.martes ?: emptyList()
                    3 -> horarioBase?.miercoles ?: emptyList()
                    4 -> horarioBase?.jueves ?: emptyList()
                    5 -> horarioBase?.viernes ?: emptyList()
                    6 -> horarioBase?.sabado ?: emptyList()
                    7 -> horarioBase?.domingo ?: emptyList()
                    else -> emptyList()
                }

                DiaHorarioCardElite(
                    dia = dia,
                    rangos = rangos,
                    onAddClick = { 
                        pickerContext = PickerContext(dia = dia, isStart = true, isMasivo = false, initialTime = "08:00")
                    },
                    onEditRange = { index, isStart, time ->
                        pickerContext = PickerContext(dia = dia, indexRango = index, isStart = isStart, isMasivo = false, initialTime = time)
                    },
                    onDeleteRango = { r -> disponibilidadViewModel.eliminarRango(dia, r) },
                    onClear = { disponibilidadViewModel.limpiarDia(dia) },
                    onCopyAll = { 
                        val destino = (1..7).filter { it != dia }
                        disponibilidadViewModel.copiarHorarioADias(dia, destino)
                    }
                )
            }
        }
    }

    // Diálogo de Selección de Hora Único
    if (pickerContext != null) {
        val ctx = pickerContext!!
        val parts = ctx.initialTime.split(":")
        
        MavTimePickerDialog(
            title = if (ctx.isStart) "HORA DE INICIO" else "HORA DE CIERRE",
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            onDismiss = { pickerContext = null },
            onConfirm = { hour, minute ->
                val formatted = "%02d:%02d".format(hour, minute)
                
                if (ctx.isMasivo) {
                    val lista = rangosBorradorMasivo.toMutableList()
                    val old = lista[ctx.indexRango!!]
                    lista[ctx.indexRango] = if (ctx.isStart) old.copy(inicio = formatted) else old.copy(fin = formatted)
                    rangosBorradorMasivo = lista
                } else {
                    // Edición individual (Directa a DB para reactividad)
                    if (ctx.indexRango == null) {
                        if (ctx.isStart) {
                            pickerContext = ctx.copy(isStart = false, initialTime = "18:00", indexRango = -99)
                            disponibilidadViewModel.añadirRango(ctx.dia!!, formatted, "%02d:%02d".format((hour + 1) % 24, minute))
                        }
                    } else if (ctx.indexRango == -99) {
                        // Flujo completado en paso anterior
                    } else {
                        // Editar rango existente
                        val listaDia = when(ctx.dia) {
                            1 -> horarioBase?.lunes ?: emptyList()
                            2 -> horarioBase?.martes ?: emptyList()
                            3 -> horarioBase?.miercoles ?: emptyList()
                            4 -> horarioBase?.jueves ?: emptyList()
                            5 -> horarioBase?.viernes ?: emptyList()
                            6 -> horarioBase?.sabado ?: emptyList()
                            7 -> horarioBase?.domingo ?: emptyList()
                            else -> emptyList()
                        }.toMutableList()
                        
                        if (ctx.indexRango in listaDia.indices) {
                            val old = listaDia[ctx.indexRango]
                            val nuevo = if (ctx.isStart) old.copy(inicio = formatted) else old.copy(fin = formatted)
                            listaDia[ctx.indexRango] = nuevo
                            disponibilidadViewModel.aplicarHorarioADias(listOf(ctx.dia!!), listaDia)
                        }
                    }
                }
                pickerContext = null
            }
        )
    }
}

@Composable
fun ConfiguracionRapidaHorario(
    diasSeleccionados: Set<Int>,
    onDiasChange: (Set<Int>) -> Unit,
    rangos: List<RangoHorarioDominio>,
    onAddRange: () -> Unit,
    onRemoveRange: (RangoHorarioDominio) -> Unit,
    onEditTime: (Int, Boolean) -> Unit,
    onApply: () -> Unit
) {
    val nombresCortos = listOf("", "L", "M", "X", "J", "V", "S", "D")
    val colorAcento = Color(0xFFFF5722)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // --- CABECERA ESTILO IMAGEN ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = colorAcento.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Schedule, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Horario de atención", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                    Text("Selecciona los días y horas disponibles", color = Color.Gray, fontSize = 13.sp)
                }
            }
            
            Spacer(Modifier.height(28.dp))
            
            // --- DÍAS DE LA SEMANA ---
            Text("Días de la semana", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (1..7).forEach { dia ->
                    val isSelected = diasSeleccionados.contains(dia)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colorAcento else Color.White.copy(alpha = 0.05f))
                            .clickable { 
                                onDiasChange(if (isSelected) diasSeleccionados - dia else diasSeleccionados + dia)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(nombresCortos[dia], color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // --- RANGO HORARIO ---
            Text("Rango horario", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            
            rangos.forEachIndexed { index, rango ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeDisplayBox(rango.inicio, "Desde", Modifier.weight(1f)) { onEditTime(index, true) }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(20.dp))
                    TimeDisplayBox(rango.fin, "Hasta", Modifier.weight(1f)) { onEditTime(index, false) }
                    
                    if (rangos.size > 1) {
                        IconButton(onClick = { onRemoveRange(rango) }) {
                            Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Red.copy(alpha = 0.5f))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (rangos.size < 2) {
                TextButton(
                    onClick = onAddRange,
                    modifier = Modifier.align(Alignment.Start),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = colorAcento, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AÑADIR OTRO TRAMO", color = colorAcento, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onApply,
                enabled = diasSeleccionados.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
            ) {
                Text("APLICAR A ${diasSeleccionados.size} DÍAS", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun TimeDisplayBox(time: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(time, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
fun DiaHorarioCardElite(
    dia: Int,
    rangos: List<RangoHorarioDominio>,
    onAddClick: () -> Unit,
    onEditRange: (Int, Boolean, String) -> Unit,
    onDeleteRango: (RangoHorarioDominio) -> Unit,
    onClear: () -> Unit,
    onCopyAll: () -> Unit
) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722),
        primaryOrangeDark = Color(0xFFF4511E),
        primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712),
        surfaceColor = Color(0xFF0F172A),
        surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B),
        chipBackground = Color(0xFF1E293B),
        chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444),
        success = Color(0xFF10B981)
    )
    val nombresDias = listOf("", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val estaAbierto = rangos.isNotEmpty()
    val colorAcento = Color(0xFFFF5722)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (estaAbierto) colors.surfaceColor else colors.surfaceColor.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = if (estaAbierto) 1.dp else 0.5.dp,
            color = if (estaAbierto) colorAcento.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombresDias[dia].uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (estaAbierto) Color.White else Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (estaAbierto) "ABIERTO" else "CERRADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estaAbierto) Color(0xFF4ADE80) else Color.Gray.copy(alpha = 0.5f)
                    )
                }

                if (estaAbierto) {
                    IconButton(onClick = onCopyAll) {
                        Icon(Icons.Default.ContentCopy, null, tint = colorAcento.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.DeleteSweep, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                    }
                }
                
                if (!estaAbierto) {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.AddCircle, null, tint = colorAcento, modifier = Modifier.size(24.dp))
                    }
                }
            }

            if (estaAbierto) {
                Spacer(Modifier.height(12.dp))
                rangos.forEachIndexed { index, rango ->
                    Surface(
                        modifier = Modifier.padding(vertical = 3.dp).fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.03f)
                    ) {
                        Row(modifier = Modifier.padding(12.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = colorAcento)
                            Spacer(Modifier.width(12.dp))
                            
                            Row(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rango.inicio, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { onEditRange(index, true, rango.inicio) }
                                )
                                Text(" — ", color = Color.Gray, fontSize = 14.sp)
                                Text(
                                    text = rango.fin, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { onEditRange(index, false, rango.fin) }
                                )
                            }
                            
                            IconButton(onClick = { onDeleteRango(rango) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
                
                if (rangos.size < 2) {
                    TextButton(onClick = onAddClick, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("+ AÑADIR OTRO TURNO", color = colorAcento, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MavTimePickerDialog(
    title: String,
    initialHour: Int = 8,
    initialMinute: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1A1A24),
            modifier = Modifier.padding(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF5722),
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(24.dp))
                
                // [ELITE]: TimeInput para carga rápida digital
                TimeInput(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color.White.copy(alpha = 0.05f),
                        clockDialSelectedContentColor = Color.Black,
                        clockDialUnselectedContentColor = Color.White,
                        selectorColor = Color(0xFFFF5722),
                        periodSelectorBorderColor = Color(0xFFFF5722),
                        periodSelectorSelectedContainerColor = Color(0xFFFF5722).copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = Color(0xFFFF5722),
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorUnselectedContentColor = Color.White,
                        timeSelectorSelectedContainerColor = Color(0xFFFF5722).copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = Color(0xFFFF5722),
                        timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.05f),
                        timeSelectorUnselectedContentColor = Color.White
                    )
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(state.hour, state.minute) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONFIRMAR", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
















































package com.example.myapplication.prestador.ui.pantallas.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import com.example.myapplication.core.datos.local.entidades.EstadoEvento
import com.example.myapplication.prestador.viewmodel.calendar.CalendarioViewModel
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.BorderGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- PANTALLA DE CALENDARIO ELITE (PRESTADOR v2026.8) ---
 * [ELITE]: Diseño ultra-moderno con modo oscuro y marcado de eventos.
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PrestadorCalendarScreen(
    onBack: () -> Unit = {},
    calendarioViewModel: CalendarioViewModel = hiltViewModel()
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val eventos by calendarioViewModel.todosLosEventos.collectAsState()
    val colors = GestionTurnosTheme

    
    val indiceInicial = 5000
    val pagerState = rememberPagerState(initialPage = indiceInicial, pageCount = { 10000 })
    val coroutineScope = rememberCoroutineScope()
    
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(pagerState.currentPage) {
        val mesesDiferencia = pagerState.currentPage - indiceInicial
        val newDate = Calendar.getInstance()
        newDate.add(Calendar.MONTH, mesesDiferencia)
        currentDate = newDate
    }

    val selectedDateStr = dateFormat.format(selectedDate.time)
    val eventosDelDia = remember(eventos, selectedDateStr) {
        eventos.filter { dateFormat.format(Date(it.marcaTiempoUtc)) == selectedDateStr }
            .sortedBy { it.marcaTiempoUtc }
    }
    
    val diasConEventos = remember(eventos) {
        eventos.filter { it.estado != EstadoEvento.CANCELADO }
            .map { dateFormat.format(Date(it.marcaTiempoUtc)) }
            .toSet()
    }


    Scaffold(
        containerColor = colors.DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.CardBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = colors.TextPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MI AGENDA",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.4.sp,
                            color = colors.TextPrimary
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.getDefault()).format(selectedDate.time)
                                .replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.BrandOrange
                        )
                    }
                    IconButton(onClick = { /* Opciones */ }) {
                        Icon(Icons.Default.MoreVert, null, tint = colors.TextSecondary)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (eventosDelDia.isEmpty()) "Sin compromisos" else "${eventosDelDia.size} eventos hoy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                colors.BrandOrange.copy(alpha = 0.12f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Event,
                            null,
                            tint = colors.BrandOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = colors.BorderGlass)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Nuevo Evento Manual */ },
                containerColor = colors.BrandOrange,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Agregar", tint = Color.Black)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- CALENDARIO CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = colors.CardBg),
                border = BorderStroke(1.dp, colors.BorderGlass),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    CalendarHeader(
                        currentDate = currentDate, 
                        onPreviousMonth = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }, 
                        onNextMonth = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                    )
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    ) { page ->
                        val mesesDiferencia = page - indiceInicial
                        val fechaMes = Calendar.getInstance(); fechaMes.add(Calendar.MONTH, mesesDiferencia)
                        CalendarGrid(
                            currentDate = fechaMes,
                            selectedDate = selectedDate,
                            daysWithEvents = diasConEventos,
                            onDateSelected = { selectedDate = it }
                        )
                    }
                }
            }

            // --- LISTA DE EVENTOS ---
            Text(
                text = "CRONOGRAMA DEL DÍA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = colors.TextMuted,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                letterSpacing = 0.6.sp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (eventosDelDia.isEmpty()) {
                    item {
                        EmptyDayState()
                    }
                }
                items(eventosDelDia, key = { it.id }) { evento ->
                    EventoEliteCard(evento = evento)
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(currentDate: Calendar, onPreviousMonth: () -> Unit, onNextMonth: () -> Unit) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) { 
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.White) 
        }
        Text(
            text = monthFormat.format(currentDate.time).uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
        IconButton(onClick = onNextMonth) { 
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White) 
        }
    }
}

@Composable
fun CalendarGrid(currentDate: Calendar, selectedDate: Calendar, daysWithEvents: Set<String>, onDateSelected: (Calendar) -> Unit) {
    val daysOfWeek = listOf("DOM", "LUN", "MAR", "MIE", "JUE", "VIE", "SAB")
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            daysOfWeek.forEach { day -> 
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                ) 
            }
        }
        
        val daysInMonth = getDaysInMonth(currentDate)
        daysInMonth.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { dayInfo -> 
                    val isSelected = isSameDay(dayInfo.date, selectedDate)
                    val dateStr = dateFormat.format(dayInfo.date.time)
                    val hasEvent = daysWithEvents.contains(dateStr)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFFF97316) else Color.Transparent)
                            .clickable { onDateSelected(dayInfo.date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayInfo.dayNumber.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.White else if (dayInfo.isCurrentMonth) Color.White else Color.White.copy(alpha = 0.2f)
                            )
                            if (hasEvent && dayInfo.isCurrentMonth) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else Color(0xFFF97316))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventoEliteCard(evento: com.example.myapplication.core.dominio.modelos.EventoDominio) {
    val colors = GestionTurnosTheme
    val colorAcento = Color(evento.colorAcentoHex)
    val startTime = evento.horaTexto
    val endTime = evento.horaFinTexto ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2F33)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hora y Barra Lateral
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = startTime.replace(" hs", ""), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                Box(modifier = Modifier.width(2.dp).height(24.dp).background(colorAcento.copy(alpha = 0.3f), CircleShape))
                Text(text = endTime.replace(" hs", ""), fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Icono Tipo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colorAcento.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                val icon: ImageVector = when(evento.tipo) {
                    TipoEvento.VISITA_TECNICA -> Icons.Default.Build
                    TipoEvento.TURNO_CITA -> Icons.Default.Schedule
                    TipoEvento.ENVIO_FLETE -> Icons.Default.LocalShipping
                    TipoEvento.BLOQUEO_ADMIN -> Icons.Default.Block
                }
                Icon(icon, null, tint = colorAcento, modifier = Modifier.size(24.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = evento.descripcion.ifBlank { "Sin detalles adicionales" },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(8.dp))
                
                Surface(
                    color = colorAcento.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = evento.estado.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = colorAcento
                    )
                }
            }
            
            IconButton(onClick = { /* Ver detalles */ }) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun EmptyDayState() {
    val colors = GestionTurnosTheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.EventBusy,
            null,
            modifier = Modifier.size(64.dp),
            tint = colors.TextMuted.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Día libre de compromisos",
            color = colors.TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helpers duplicados para independencia del archivo
private fun getDaysInMonth(date: Calendar): List<DayInfo> {
    val days = mutableListOf<DayInfo>()
    val cal = date.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val offset = cal.get(Calendar.DAY_OF_WEEK) - 1
    cal.add(Calendar.DAY_OF_MONTH, -offset)
    repeat(42) {
        days.add(DayInfo(cal.clone() as Calendar, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) == date.get(Calendar.MONTH)))
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private data class DayInfo(val date: Calendar, val dayNumber: Int, val isCurrentMonth: Boolean)
















































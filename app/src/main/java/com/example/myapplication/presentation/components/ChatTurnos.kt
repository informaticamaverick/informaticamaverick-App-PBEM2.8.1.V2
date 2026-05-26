package com.example.myapplication.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.presentation.components.Utilidades.MaverickColors
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// CONFIGURACIÓN Y MODELOS DE DATOS
// ==========================================
data class DayAvailability(
    val date: Date,
    val startTime: String, // "HH:mm"
    val endTime: String,   // "HH:mm"
    val slotDurationMinutes: Int
)

data class TimeSlot(
    val time: String, // "HH:mm"
    val isOccupied: Boolean
)

// ==========================================
// COMPONENTE PRINCIPAL DEL DIALOG (REDISEÑO MAVERICK)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    message: MessageEntity,
    availableAddresses: List<AddressInfo>,
    onDismissRequest: () -> Unit,
    onAcceptRequest: (String, String, String?, String?, String) -> Unit // Retorna fecha, hora, dirección y label opcional
) {
    // Parsing del JSON de disponibilidad
    val availabilityList = remember(message.availabilityJson) {
        parseAvailabilityJson(message.availabilityJson ?: "[]")
    }
    
    val bookedSlots = remember(message.bookedSlotsJson) {
        parseBookedSlotsJson(message.bookedSlotsJson ?: "[]")
    }

    // Estados
    var selectedDateAvailability by remember { mutableStateOf(availabilityList.firstOrNull()) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var selectedAddress by remember { mutableStateOf<AddressInfo?>(availableAddresses.firstOrNull()) }

    // Tipo de Cita y Dirección del Prestador
    val appointmentType = message.appointmentType ?: "TECHNICAL_VISIT"
    val isTechnicalVisit = appointmentType == "TECHNICAL_VISIT"
    val providerAddress = message.providerAddress

    // Obtener slots del día seleccionado
    val currentSlots = remember(selectedDateAvailability) {
        selectedDateAvailability?.let { avail ->
            generateSlotsFromAvailability(avail, bookedSlots)
        } ?: emptyList()
    }

    // Colores Maverick
    val maverickBlue = Color(0xFF2197F5)
    val maverickCyan = Color(0xFF22D3EE)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
                
                // --- Glow Effect Background ---
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 100.dp, y = (-100).dp)
                        .blur(80.dp)
                        .background(maverickBlue.copy(alpha = 0.15f), CircleShape)
                )

                Column(modifier = Modifier.fillMaxSize()) {

                    // --- Encabezado Táctico ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = maverickBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isTechnicalVisit) Icons.Default.HomeRepairService else Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = maverickCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isTechnicalVisit) "Visita Técnica" else "Turno en Local",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "GESTIÓN DE AGENDA MAVERICK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = maverickCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // --- Cuerpo (Scrollable) ---
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        
                        // 1. Selección de Fecha
                        Text(
                            text = "1. SELECCIONÁ EL DÍA",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availabilityList) { avail ->
                                DayItemV2(
                                    date = avail.date,
                                    isSelected = avail.date == selectedDateAvailability?.date,
                                    onClick = {
                                        selectedDateAvailability = avail
                                        selectedTime = null
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // 2. Selección de Hora
                        Text(
                            text = "2. SELECCIONÁ EL HORARIO",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (currentSlots.isEmpty()) {
                            EmptySlotsMessage()
                        } else {
                            // Usamos LazyRow o un FlowRow si son muchos
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(currentSlots) { slot ->
                                    TimeItemV2(
                                        slot = slot,
                                        isSelected = slot.time == selectedTime,
                                        onClick = {
                                            if (!slot.isOccupied) {
                                                selectedTime = if (selectedTime == slot.time) null else slot.time
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // 3. Selección de Ubicación
                        Text(
                            text = if (isTechnicalVisit) "3. ¿DÓNDE REALIZAMOS EL TRABAJO?" else "3. UBICACIÓN DEL LOCAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (isTechnicalVisit) {
                            if (availableAddresses.isEmpty()) {
                                Text("No tienes direcciones guardadas.", color = Color.Red, fontSize = 12.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    availableAddresses.forEach { addr ->
                                        AddressItemPremium(
                                            address = addr,
                                            isSelected = selectedAddress?.id == addr.id,
                                            onClick = { selectedAddress = addr }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Mostrar dirección del prestador
                            LocationInfoCard(
                                title = "Dirección de atención",
                                address = providerAddress ?: "A convenir con el prestador"
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- Pie de acciones (Glassmorphism) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.weight(1f).height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("CANCELAR", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            }
                            
                            Button(
                                onClick = {
                                    if (selectedDateAvailability != null && selectedTime != null) {
                                        // 🔥 NORMALIZACIÓN: Guardamos en formato ISO (yyyy-MM-dd) para Room y ordenamiento
                                        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val dateStr = isoFormat.format(selectedDateAvailability!!.date)
                                        val finalAddr = if (isTechnicalVisit) selectedAddress?.streetAndNumber else providerAddress
                                        val finalLabel = if (isTechnicalVisit) selectedAddress?.branchName else null
                                        onAcceptRequest(dateStr, selectedTime!!, finalAddr, finalLabel, message.id)
                                    }
                                },
                                enabled = selectedDateAvailability != null && selectedTime != null && (!isTechnicalVisit || selectedAddress != null),
                                modifier = Modifier.weight(1.5f).height(54.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = maverickBlue,
                                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("CONFIRMAR CITA", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItemPremium(
    address: AddressInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val maverickBlue = Color(0xFF2197F5)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) maverickBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, if (isSelected) maverickBlue else Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isSelected) maverickBlue else Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        address.id == "gps_current" -> Icons.Default.MyLocation
                        address.isCompany -> Icons.Default.Business
                        else -> Icons.Default.Home
                    },
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address.branchName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${address.streetAndNumber}, ${address.locality}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = maverickBlue, unselectedColor = Color.Gray)
            )
        }
    }
}

@Composable
fun DayItemV2(date: Date, isSelected: Boolean, onClick: () -> Unit) {
    val dayName = SimpleDateFormat("EEE", Locale("es", "ES")).format(date).uppercase()
    val dayNumber = SimpleDateFormat("d", Locale.getDefault()).format(date)
    val maverickBlue = Color(0xFF2197F5)

    Surface(
        modifier = Modifier
            .width(64.dp)
            .height(84.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) maverickBlue else Color.White.copy(alpha = 0.05f),
        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = dayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dayNumber,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.White else Color.White
            )
        }
    }
}

@Composable
fun TimeItemV2(slot: TimeSlot, isSelected: Boolean, onClick: () -> Unit) {
    val maverickBlue = Color(0xFF2197F5)
    
    Surface(
        modifier = Modifier
            .width(80.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !slot.isOccupied) { onClick() },
        color = when {
            slot.isOccupied -> Color.White.copy(alpha = 0.02f)
            isSelected -> maverickBlue
            else -> Color.White.copy(alpha = 0.08f)
        },
        border = if (isSelected || slot.isOccupied) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = slot.time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    slot.isOccupied -> Color.White.copy(alpha = 0.2f)
                    isSelected -> Color.White
                    else -> Color.White
                },
                textDecoration = if (slot.isOccupied) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

@Composable
fun LocationInfoCard(title: String, address: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF22D3EE), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text(address, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptySlotsMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("No hay horarios disponibles para hoy", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
    }
}

// ==========================================
// FUNCIONES DE PARSEO Y LÓGICA
// ==========================================
fun parseAvailabilityJson(json: String): List<DayAvailability> {
    val list = mutableListOf<DayAvailability>()
    try {
        val array = JSONArray(json)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val dateStr = obj.getString("date")
            val startTimeStr = obj.getString("startTime")
            val endTimeStr = obj.getString("endTime")
            val duration = obj.getInt("durationMinutes")
            
            val date = dateFormatter.parse(dateStr)
            if (date != null) {
                list.add(DayAvailability(
                    date = date,
                    startTime = startTimeStr,
                    endTime = endTimeStr,
                    slotDurationMinutes = duration
                ))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list.sortedBy { it.date }
}

fun parseBookedSlotsJson(json: String): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    try {
        val array = JSONArray(json)
        
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val dateStr = obj.getString("date") // yyyy-MM-dd
            val timeStr = obj.getString("time") // HH:mm
            
            list.add(dateStr to timeStr)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

fun generateSlotsFromAvailability(avail: DayAvailability, booked: List<Pair<String, String>>): List<TimeSlot> {
    val slots = mutableListOf<TimeSlot>()
    val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    val dateKey = dateSdf.format(avail.date)
    
    // Si la duración es 0, no generamos slots (pueden ser horarios de atención general)
    if (avail.slotDurationMinutes <= 0) return emptyList()

    try {
        var current = timeSdf.parse(avail.startTime)
        val end = timeSdf.parse(avail.endTime)
        
        if (current != null && end != null) {
            val calendar = Calendar.getInstance()
            
            while (true) {
                val currentTime = current!!
                calendar.time = currentTime
                val next = Calendar.getInstance().apply {
                    time = currentTime
                    add(Calendar.MINUTE, avail.slotDurationMinutes)
                }.time
                
                if (next.after(end)) break
                
                val currentTimeStr = timeSdf.format(current)
                val isOccupied = booked.any { it.first == dateKey && it.second == currentTimeStr }
                
                slots.add(TimeSlot(time = currentTimeStr, isOccupied = isOccupied))
                current = next
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return slots
}

package com.example.myapplication.presentation.components

import com.example.myapplication.presentation.features.home.*

import com.example.myapplication.presentation.features.auth.*

import com.example.myapplication.presentation.features.home.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.entity.CategoryEntity
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.presentation.designsystem.theme.getThemeColors
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
// FUNCIONES AUXILIARES DE FORMATEO
// ==========================================

/**
 * Limpia el string de hora para evitar duplicados como "10:30 hs HS".
 * Retorna el formato limpio "HH:mm HS".
 */
fun cleanAppointmentTime(rawTime: String?): String {
    if (rawTime.isNullOrBlank()) return "--:--"
    // Eliminar hs, HS, h, H y espacios extra
    val clean = rawTime.replace(Regex("(?i)\\s*hs|\\s*h"), "").trim()
    return "$clean HS"
}

// ==========================================
// COMPONENTE PRINCIPAL DEL DIALOG (REDISEÑO MAVERICK)
// ==========================================
/**
 * PANTALLA TONTA: Diálogo de reserva de turnos.
 * Recibe el estado ya procesado y emite eventos de selección.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    message: MessageEntity,
    availableAddresses: List<AddressInfo>,
    categories: List<CategoryEntity> = emptyList(),
    // [NUEVO] Parámetros de estado para ser stateless
    availableDays: List<DayAvailability>,
    selectedDay: DayAvailability?,
    currentSlots: List<TimeSlot>,
    selectedTime: String?,
    selectedAddress: AddressInfo?,
    onDaySelected: (DayAvailability) -> Unit,
    onTimeSelected: (String) -> Unit,
    onAddressSelected: (AddressInfo) -> Unit,
    onDismissRequest: () -> Unit,
    onAcceptRequest: (String, String, String?, String?) -> Unit // Retorna fecha, hora, dirección y label opcional
) {
    // Tipo de Cita y Dirección del Prestador
    val appointmentType = message.appointmentType ?: "TECHNICAL_VISIT"
    val isTechnicalVisit = appointmentType == "TECHNICAL_VISIT"
    val providerAddress = message.providerAddress

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

                    // --- Encabezado PREMIUM ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(maverickBlue.copy(alpha = 0.15f))
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val catEmoji = remember(message.categoryId, categories) {
                                        categories.find { it.name == message.categoryId }?.icon
                                    }
                                    Text(
                                        text = catEmoji ?: if (isTechnicalVisit) "🧰" else "🗓️",
                                        fontSize = 24.sp
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
                            .padding(24.dp)
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
                            items(availableDays) { avail ->
                                DayItemV2(
                                    date = avail.date,
                                    isSelected = avail.date == selectedDay?.date,
                                    onClick = { onDaySelected(avail) }
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
                                                onTimeSelected(slot.time)
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
                                            onClick = { onAddressSelected(addr) }
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
                                    if (selectedDay != null && selectedTime != null) {
                                        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val dateStr = isoFormat.format(selectedDay.date)
                                        val finalAddr = if (isTechnicalVisit) selectedAddress?.streetAndNumber else providerAddress
                                        val finalLabel = if (isTechnicalVisit) selectedAddress?.branchName else null
                                        onAcceptRequest(dateStr, selectedTime, finalAddr, finalLabel)
                                    }
                                },
                                enabled = selectedDay != null && selectedTime != null && (!isTechnicalVisit || selectedAddress != null),
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
// BURBUJAS DE TURNOS Y CITAS (TURNOS)
// ==========================================

@Composable
fun TechnicalVisitProposalBubble(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    providerPhotoUrl: String? = null,
    categoryEmoji: String? = null,
    onReply: () -> Unit = {}, // 🔥 [NUEVO]
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val status = message.appointmentStatus ?: "PENDING"
    val headerColor = Color(0xFF10B981) // Verde Esmeralda base
    val headerEmoji = "🧰"
    val headerTitle = "Visita Técnica"
    val headerSubtitle = "Propuesta de Visita"

    SwipeToReplyWrapper(onReply = onReply) {
        AppointmentProposalBase(
            message = message,
            isMe = isMe,
            appColors = appColors,
            headerColor = headerColor,
            headerEmoji = headerEmoji,
            headerTitle = headerTitle,
            headerSubtitle = headerSubtitle,
            status = status,
            providerPhotoUrl = providerPhotoUrl,
            categoryEmoji = categoryEmoji,
            onAccept = onAccept,
            onReject = onReject
        )
    }
}

@Composable
fun LocalAppointmentProposalBubble(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    providerPhotoUrl: String? = null,
    categoryEmoji: String? = null,
    onReply: () -> Unit = {}, // 🔥 [NUEVO]
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val status = message.appointmentStatus ?: "PENDING"
    val headerColor = Color(0xFF2197F5) // Azul Maverick base
    val headerEmoji = "🗓️"
    val headerTitle = "Turno en local"
    val headerSubtitle = "Propuesta de Turno"

    SwipeToReplyWrapper(onReply = onReply) {
        AppointmentProposalBase(
            message = message,
            isMe = isMe,
            appColors = appColors,
            headerColor = headerColor,
            headerEmoji = headerEmoji,
            headerTitle = headerTitle,
            headerSubtitle = headerSubtitle,
            status = status,
            providerPhotoUrl = providerPhotoUrl,
            categoryEmoji = categoryEmoji,
            onAccept = onAccept,
            onReject = onReject
        )
    }
}

@Composable
private fun AppointmentProposalBase(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors,
    headerColor: Color,
    headerEmoji: String,
    headerTitle: String,
    headerSubtitle: String,
    status: String,
    providerPhotoUrl: String? = null,
    categoryEmoji: String? = null,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null
) {
    val statusColor = when(status) {
        "ACCEPTED" -> Color(0xFF10B981)
        "REJECTED" -> Color(0xFFEF4444)
        else -> headerColor
    }
    
    // Colores Explícitos para Gradiente Premium (Asegura Oscuro Izquierda -> Claro Derecha)
    val (darkColor, lightColor) = remember(headerColor) {
        when (headerColor) {
            Color(0xFF10B981) -> Color(0xFF064E3B) to Color(0xFF10B981) // Visitas
            Color(0xFF2197F5) -> Color(0xFF1E3A8A) to Color(0xFF2197F5) // Turnos
            else -> headerColor.copy(alpha = 0.9f) to headerColor.copy(alpha = 0.4f)
        }
    }
    
    val headerGradient = Brush.horizontalGradient(colors = listOf(darkColor, lightColor))
    
    val borderColor = if (isMe) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF22D3EE).copy(alpha = 0.5f)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 4.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMe) 20.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                if (message.replyToId != null) {
                    QuotedMessage(
                        replyToSenderName = message.replyToSenderName,
                        replyToContent = message.replyToContent,
                        appColors = appColors,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                // --- HEADER PREMIUM ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(headerEmoji, fontSize = 22.sp) // Emoji más grande
                    
                    // Divider Vertical a la derecha del Emoji
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = headerTitle.uppercase(), 
                            fontWeight = FontWeight.Black, 
                            color = Color.White, 
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = headerSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.padding(14.dp)) {
                    // --- CATEGORIA Y PERFIL ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = headerColor.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(categoryEmoji ?: "📍", fontSize = 16.sp)
                                }
                            }
                            
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f))) // Vertical Divider
                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = (message.categoryId ?: "Servicio").uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = headerColor,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Propuesta de Agenda",
                                    fontSize = 10.sp,
                                    color = appColors.textSecondaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        if (!isMe && providerPhotoUrl != null) {
                            coil.compose.AsyncImage(
                                model = providerPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).clip(CircleShape).border(1.dp, headerColor.copy(alpha = 0.3f), CircleShape)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(Modifier.height(14.dp))

                    // --- DETALLES DE CITA ---
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, tint = headerColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(10.dp))
                                val displayDate = try {
                                    val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    message.appointmentDate?.let { dateStr ->
                                        if (dateStr.contains("-") && dateStr.length == 10) {
                                            inputFmt.parse(dateStr)?.let { outputFmt.format(it) }
                                        } else dateStr
                                    } ?: message.appointmentDate
                                } catch (_: Exception) { message.appointmentDate }
                                
                                Text(displayDate ?: "A convenir", fontSize = 13.sp, color = appColors.textPrimaryColor, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, tint = headerColor, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(cleanAppointmentTime(message.appointmentTime), fontSize = 13.sp, color = appColors.textPrimaryColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    if (message.content.isNotBlank() && !message.content.startsWith("Cita en:")) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = message.content, 
                            fontSize = 12.sp, 
                            color = appColors.textSecondaryColor, 
                            fontStyle = FontStyle.Italic,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    
                    if (status == "PENDING" && !isMe && onAccept != null && onReject != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("ACEPTAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(40.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("RECHAZAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    } else {
                        Surface(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when(status) {
                                    "ACCEPTED" -> "✓ ACEPTADA"
                                    "REJECTED" -> "✕ RECHAZADA"
                                    else -> "⏳ PENDIENTE"
                                },
                                color = statusColor,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
        // --- HORA FUERA DE LA BURBUJA (ESTILO PREMIUM) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, start = if (isMe) 8.dp else 0.dp, end = if (isMe) 0.dp else 8.dp),
            contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.textSecondaryColor.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
                if (isMe) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                        contentDescription = null,
                        tint = if (message.isRead) Color(0xFF22D3EE) else appColors.textSecondaryColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarInviteBubble(
    message: MessageEntity, 
    isMe: Boolean, 
    appColors: AppColors, 
    isEnabled: Boolean = true,
    onReply: () -> Unit = {}, // 🔥 [NUEVO]
    onClick: () -> Unit
) {
    val isTechnicalVisit = message.appointmentType == "TECHNICAL_VISIT"
    
    // Identidad Visual Unificada
    val (headerColor, headerEmoji, headerTitle) = if (isTechnicalVisit) {
        Triple(Color(0xFF10B981), "🧰", "Visita Técnica")
    } else {
        Triple(Color(0xFF2197F5), "🗓️", "Turno en Local")
    }
    
    // Colores Explícitos para Gradiente Premium
    val (darkColor, lightColor) = remember(headerColor) {
        if (isTechnicalVisit) Color(0xFF064E3B) to Color(0xFF10B981)
        else Color(0xFF1E3A8A) to Color(0xFF2197F5)
    }
    
    val headerGradient = Brush.horizontalGradient(colors = listOf(darkColor, lightColor))
    
    val borderColor = if (isMe) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF22D3EE).copy(alpha = 0.5f)

    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 4.dp, vertical = 4.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier.width(280.dp),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ),
                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    if (message.replyToId != null) {
                        QuotedMessage(
                            replyToSenderName = message.replyToSenderName,
                            replyToContent = message.replyToContent,
                            appColors = appColors,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    // Header con Identidad Unificada y Gradiente
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerGradient)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(headerEmoji, fontSize = 22.sp) // Más grande
                        
                        // Divider Vertical
                        Spacer(Modifier.width(10.dp))
                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text = headerTitle.uppercase(),
                                fontWeight = FontWeight.Black, 
                                color = Color.White, 
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isEnabled) "AGENDA DISPONIBLE" else "CITA AGENDADA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) Color.White.copy(alpha = 0.9f) else Color.Gray,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isEnabled) {
                                if (isTechnicalVisit) 
                                    "El prestador compartió su agenda para realizar la visita en tu domicilio." 
                                    else "El prestador compartió su agenda para que reserves un turno en su local."
                            } else {
                                "Ya has seleccionado un horario para esta propuesta."
                            },
                            fontSize = 13.sp, 
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onClick,
                            enabled = isEnabled,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEnabled) headerColor else Color.Gray.copy(alpha = 0.2f),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isEnabled) {
                                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ELEGIR DÍA Y HORA", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            } else {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text("TURNO RESERVADO", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                            }
                        }
                    }
                }
            }
            // --- HORA FUERA DE LA BURBUJA ---
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondaryColor.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// ==========================================
// COMPROBANTES DE TURNO (RECEIPTS)
// ==========================================

@Composable
fun TechnicalVisitReceiptBubble(
    message: MessageEntity, 
    isMe: Boolean, 
    appColors: AppColors,
    allCategories: List<CategoryEntity> = emptyList(), // 🔥 [NUEVO] Para resolver emoji y nombre
    onReply: () -> Unit = {}, // 🔥 [NUEVO]
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {}
) {
    val statusConfirmed = Color(0xFF10B981) // Verde Maverick
    val maverickBlue = Color(0xFF2197F5)
    val isConfirmed = message.appointmentStatus == "ACCEPTED" || message.type == MessageType.APPOINTMENT_RECEIPT
    
    // Gradiente Premium Consistente
    val headerGradient = Brush.horizontalGradient(
        listOf(Color(0xFF064E3B), statusConfirmed)
    )
    
    val borderColor = if (isMe) statusConfirmed.copy(alpha = 0.5f) else Color(0xFF22D3EE).copy(alpha = 0.5f)
    var showSecurityPopup by remember { mutableStateOf(false) }

    // Resolver Categoría desde la lista global usando el ID del mensaje
    val resolvedCategory = remember(message.categoryId, allCategories) {
        allCategories.find { it.name == message.categoryId }
    }

    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 4.dp, vertical = 4.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier.width(300.dp),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ),
                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    if (message.replyToId != null) {
                        QuotedMessage(
                            replyToSenderName = message.replyToSenderName,
                            replyToContent = message.replyToContent,
                            appColors = appColors,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    // Header PREMIUM Visita Técnica (Verde)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerGradient)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧰", fontSize = 22.sp) // Más grande
                            
                            // Divider Vertical
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "VISITA TÉCNICA",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        // --- Círculo Moderno 3D de Estado ---
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .shadow(4.dp, CircleShape, spotColor = Color.Black),
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isConfirmed) "✓" else "⏳",
                                        color = if (isConfirmed) statusConfirmed else Color.Gray,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isConfirmed) "CONFIRMADA" else "PENDIENTE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        // --- DETALLE DE VISITA (JSON) ---
                        if (message.content.isNotBlank() && !message.content.startsWith("Cita en:")) {
                            Text(
                                text = message.content,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // --- CATEGORIA RESOLVIDA ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = statusConfirmed.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(resolvedCategory?.icon ?: "🛠️", fontSize = 16.sp)
                                }
                            }

                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f))) // Vertical Divider
                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = (resolvedCategory?.name ?: message.receiptService ?: message.categoryId ?: "Técnico").uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusConfirmed,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Servicio Técnico Maverick",
                                    fontSize = 10.sp,
                                    color = appColors.textSecondaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📅", fontSize = 16.sp) // Un poco más grande
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.2f))) // Vertical Divider
                            Spacer(Modifier.width(10.dp))
                            Text("Agendado en tu Calendario", fontSize = 11.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))

                        // Tarjeta Fecha y Hora
                        Surface(
                            color = Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onCalendarClick() },
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("FECHA", fontSize = 9.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Black)
                                    val displayDate = remember(message.appointmentDate) {
                                        try {
                                            val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            message.appointmentDate?.let { dateStr ->
                                                if (dateStr.contains("-") && dateStr.length == 10) {
                                                    inputFmt.parse(dateStr)?.let { outputFmt.format(it) }
                                                } else dateStr
                                            } ?: "--/--/--"
                                        } catch (e: Exception) { message.appointmentDate ?: "--/--/--" }
                                    }
                                    Text(displayDate, fontSize = 14.sp, color = appColors.textPrimaryColor, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(appColors.textSecondaryColor.copy(alpha = 0.1f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("HORA", fontSize = 9.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Black)
                                    Text(cleanAppointmentTime(message.appointmentTime), fontSize = 14.sp, color = maverickBlue, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) // Divisor para ubicación
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "📍 El prestador se va a dirigir a:",
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { message.receiptAddress?.let { onAddressClick(it) } },
                            color = Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, maverickBlue.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = maverickBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = message.receiptAddress ?: "Ubicación confirmada",
                                    fontSize = 12.sp,
                                    color = appColors.textPrimaryColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) // Divisor para código
                        Spacer(Modifier.height(20.dp))

                        // Código de Validación
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CÓDIGO DE VALIDACIÓN", fontSize = 9.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Black)
                                Text(
                                    text = message.receiptCode ?: "PENDIENTE",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusConfirmed,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }
                            
                            IconButton(
                                onClick = { showSecurityPopup = true },
                                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
            // --- HORA FUERA DE LA BURBUJA ---
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondaryColor.copy(alpha = 0.4f)
                )
            }
        }
    }

    if (showSecurityPopup) {
        SecurityRecommendationsPopup(onDismiss = { showSecurityPopup = false })
    }
}

@Composable
fun StandardAppointmentReceiptBubble(
    message: MessageEntity, 
    isMe: Boolean, 
    appColors: AppColors,
    allCategories: List<CategoryEntity> = emptyList(), // 🔥 [NUEVO]
    onReply: () -> Unit = {}, // 🔥 [NUEVO]
    onCalendarClick: () -> Unit = {},
    onAddressClick: (String) -> Unit = {}
) {
    val maverickBlue = Color(0xFF2197F5)
    val statusConfirmed = Color(0xFF10B981)
    val isConfirmed = message.appointmentStatus == "ACCEPTED" || message.type == MessageType.APPOINTMENT_RECEIPT
    
    // Gradiente Premium Consistente
    val headerGradient = Brush.horizontalGradient(
        listOf(Color(0xFF1E3A8A), maverickBlue)
    )
    val borderColor = if (isMe) statusConfirmed.copy(alpha = 0.5f) else Color(0xFF22D3EE).copy(alpha = 0.5f)
    var showSecurityPopup by remember { mutableStateOf(false) }

    // Resolver Categoría
    val resolvedCategory = remember(message.categoryId, allCategories) {
        allCategories.find { it.name == message.categoryId }
    }

    SwipeToReplyWrapper(onReply = onReply) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 4.dp, vertical = 4.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Card(
                modifier = Modifier.width(300.dp),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 20.dp
                ),
                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    if (message.replyToId != null) {
                        QuotedMessage(
                            replyToSenderName = message.replyToSenderName,
                            replyToContent = message.replyToContent,
                            appColors = appColors,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    // Header PREMIUM Turno Confirmado (Azul)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(headerGradient)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🗓️", fontSize = 22.sp) // Más grande
                            
                            // Divider Vertical
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f)))
                            Spacer(Modifier.width(10.dp))

                            Text(
                                text = "TURNO EN LOCAL",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        // --- Círculo Moderno 3D de Estado ---
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier
                                    .size(32.dp)
                                    .shadow(4.dp, CircleShape, spotColor = Color.Black),
                                shape = CircleShape,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isConfirmed) "✓" else "⏳",
                                        color = if (isConfirmed) statusConfirmed else Color.Gray,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isConfirmed) "CONFIRMADO" else "PENDIENTE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        // --- DETALLE DE TURNO (JSON) ---
                        if (message.content.isNotBlank() && !message.content.startsWith("Cita en:")) {
                            Text(
                                text = message.content,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // --- CATEGORIA RESOLVIDA ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = maverickBlue.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(resolvedCategory?.icon ?: "💼", fontSize = 16.sp)
                                }
                            }

                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f))) // Vertical Divider
                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = (resolvedCategory?.name ?: message.receiptService ?: message.categoryId ?: "Atención").uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = maverickBlue,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Atención al Cliente Maverick",
                                    fontSize = 10.sp,
                                    color = appColors.textSecondaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        Spacer(Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📅", fontSize = 16.sp) // Un poco más grande
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.2f))) // Vertical Divider
                            Spacer(Modifier.width(10.dp))
                            Text("Agendado en tu Calendario", fontSize = 11.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Bold)
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onCalendarClick() },
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("FECHA", fontSize = 9.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Black)
                                    val displayDate = remember(message.appointmentDate) {
                                        try {
                                            val inputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            message.appointmentDate?.let { dateStr ->
                                                if (dateStr.contains("-") && dateStr.length == 10) {
                                                    inputFmt.parse(dateStr)?.let { outputFmt.format(it) }
                                                } else dateStr
                                            } ?: "--/--/--"
                                        } catch (e: Exception) { message.appointmentDate ?: "--/--/--" }
                                    }
                                    Text(displayDate, fontSize = 14.sp, color = appColors.textPrimaryColor, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.width(1.dp).height(24.dp).background(appColors.textSecondaryColor.copy(alpha = 0.1f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("HORA", fontSize = 9.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Black)
                                    Text(cleanAppointmentTime(message.appointmentTime), fontSize = 14.sp, color = maverickBlue, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) // Divisor para ubicación
                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "🏢 El prestador te va a esperar en:",
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { message.receiptAddress?.let { onAddressClick(it) } },
                            color = Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(0.5.dp, maverickBlue.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = maverickBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = message.receiptAddress ?: "Ubicación confirmada",
                                    fontSize = 12.sp,
                                    color = appColors.textPrimaryColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) // Divisor para código
                        Spacer(Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CÓDIGO DE VALIDACIÓN", fontSize = 9.sp, color = appColors.textSecondaryColor, fontWeight = FontWeight.Black)
                                Text(
                                    text = message.receiptCode ?: "PENDIENTE",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusConfirmed,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                            }
                            
                            IconButton(
                                onClick = { showSecurityPopup = true },
                                modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                            }
                        }
                    }
                }
                // --- HORA FUERA DE LA BURBUJA ---
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondaryColor.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }

    if (showSecurityPopup) {
        SecurityRecommendationsPopup(onDismiss = { showSecurityPopup = false })
    }
}

@Composable
fun SecurityRecommendationsPopup(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛡️", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Text("Seguridad Maverick", fontWeight = FontWeight.Black)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SecurityItem("👤", "Verifica la identidad del prestador pidiendo su DNI al llegar.")
                SecurityItem("🔑", "No compartas el código de validación hasta que el trabajo haya comenzado o el prestador esté presente.")
                SecurityItem("🏠", "Si es una visita técnica, asegúrate de estar acompañado si es posible.")
                SecurityItem("📞", "Cualquier irregularidad, repórtala de inmediato a través del botón de ayuda.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2197F5))
            ) {
                Text("ENTENDIDO", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF1E293B),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun SecurityItem(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
fun ScheduleAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, notes: String) -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (dd/mm/aaaa)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Hora (hh:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(date, time, notes) }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// ==========================================
// SECCIÓN DE VISTAS PREVIAS (PREVIEWS)
// ==========================================

@Preview(showBackground = true, name = "Burbujas de Turnos y Citas")
@Composable
fun TurnoBubblesPreview() {
    val appColors = getThemeColors()
    val now = System.currentTimeMillis()
    
    val appointmentMsg = MessageEntity(
        id = "t1",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.VISIT,
        content = "Te propongo realizar la visita técnica este día.",
        appointmentDate = "2024-12-25",
        appointmentTime = "10:30",
        appointmentStatus = "PENDING",
        appointmentType = "TECHNICAL_VISIT",
        categoryId = "Refrigeración",
        timestamp = now
    )
    
    val inviteMsg = MessageEntity(
        id = "t2",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.CALENDAR_INVITE,
        content = "Elegí el horario que mejor te quede.",
        appointmentType = "IN_STORE",
        timestamp = now
    )
    
    val receiptMsgVisita = MessageEntity(
        id = "t3",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.APPOINTMENT_RECEIPT,
        content = "¡Listo! Visita confirmada.",
        appointmentDate = "Mié 06/05/2026",
        appointmentTime = "10:00",
        receiptProviderName = "Maverick Refrigeración",
        receiptAddress = "B. Matienzo 1339",
        receiptCode = "#VIS-20260506-001",
        receiptIsTechnician = true,
        categoryId = "Técnico",
        timestamp = now
    )
    
    val receiptMsgTurno = MessageEntity(
        id = "t4",
        chatId = "c1",
        senderId = "p1",
        receiverId = "u1",
        type = MessageType.APPOINTMENT_RECEIPT,
        content = "¡Listo! Turno confirmado.",
        appointmentDate = "Jue 07/05/2026",
        appointmentTime = "16:30",
        receiptProviderName = "Maverick Refrigeración",
        receiptAddress = "Calle Junín 450, Tucumán",
        receiptCode = "#TRN-20260507-001",
        receiptIsTechnician = false,
        categoryId = "Consultoría",
        timestamp = now
    )

    val sampleCategories = listOf(
        CategoryEntity(name = "Refrigeración", icon = "❄️", superCategory = "", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Técnico", icon = "🛠️", superCategory = "", isNew = false, isNewPrestador = false, isAd = false),
        CategoryEntity(name = "Consultoría", icon = "💼", superCategory = "", isNew = false, isNewPrestador = false, isAd = false)
    )

    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TechnicalVisitProposalBubble(
                message = appointmentMsg, 
                isMe = false, 
                appColors = appColors, 
                categoryEmoji = "❄️",
                onAccept = {}, 
                onReject = {}
            )
            CalendarInviteBubble(message = inviteMsg, isMe = false, appColors = appColors, onClick = {})
            TechnicalVisitReceiptBubble(
                message = receiptMsgVisita, 
                isMe = false, 
                appColors = appColors,
                allCategories = sampleCategories
            )
            StandardAppointmentReceiptBubble(
                message = receiptMsgTurno, 
                isMe = false, 
                appColors = appColors,
                allCategories = sampleCategories
            )
        }
    }
}










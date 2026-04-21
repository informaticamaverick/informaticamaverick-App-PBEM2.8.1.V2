package com.example.myapplication.prestador.ui.appointments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.AppointmentEntity
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.theme.PrestadorColors

/**
 * Diálogo para mostrar detalles completos de una cita
 * Incluye información específica según el tipo de servicio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailsDialog(
    appointment: AppointmentEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    colors: PrestadorColors,
    rentalSpaceName: String? = null,  // Nombre del espacio si es RENTAL
    scheduleName: String? = null      // Nombre del horario si es PROFESSIONAL
) {
    println("🟣 AppointmentDetailsDialog abierto")
    println("🟣 Cita ID: ${appointment.id}")
    println("🟣 Cliente: ${appointment.clientName}")
    println("🟣 Status: ${appointment.status}")
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colors.backgroundColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header con estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles de la Cita",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.textPrimary
                    )
                    
                    // Badge de estado
                    StatusBadge(status = appointment.status, colors = colors)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Badge de tipo de servicio
                ServiceTypeBadge(
                    serviceType = appointment.serviceType,
                    colors = colors
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Información del cliente
                InfoSection(
                    title = "Cliente",
                    icon = Icons.Default.Person,
                    colors = colors
                ) {
                    InfoRow(label = "Nombre", value = appointment.clientName, colors = colors)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Información del servicio
                InfoSection(
                    title = "Servicio",
                    icon = if (appointment.serviceType.equals("PROFESSIONAL", ignoreCase = true)) Icons.Default.Description else Icons.Default.Build,
                    colors = colors
                ) {
                    InfoRow(label = "Descripción", value = appointment.service, colors = colors)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Información de fecha y hora
                InfoSection(
                    title = "Fecha y Hora",
                    icon = Icons.Default.CalendarToday,
                    colors = colors
                ) {
                    InfoRow(label = "Fecha", value = formatDate(appointment.date), colors = colors)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Hora", value = appointment.time, colors = colors)
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Duración", value = "${appointment.duration} minutos", colors = colors)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Información específica según tipo de servicio
                when (appointment.serviceType) {
                    "PROFESSIONAL" -> {
                        scheduleName?.let {
                            InfoSection(
                                title = "Horario",
                                icon = Icons.Default.Schedule,
                                colors = colors
                            ) {
                                InfoRow(label = "Turno", value = it, colors = colors)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                    "RENTAL" -> {
                        rentalSpaceName?.let {
                            InfoSection(
                                title = "Espacio",
                                icon = Icons.Default.Place,
                                colors = colors
                            ) {
                                InfoRow(label = "Nombre", value = it, colors = colors)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                
                // Notas (si existen)
                if (appointment.notes.isNotEmpty()) {
                    InfoSection(
                        title = "Notas",
                        icon = Icons.Default.Notes,
                        colors = colors
                    ) {
                        Text(
                            text = appointment.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Información adicional
                InfoSection(
                    title = "Información Adicional",
                    icon = Icons.Default.Info,
                    colors = colors
                ) {
                    InfoRow(
                        label = "Propuesta por",
                        value = if (appointment.proposedBy == "provider") "Prestador" else "Cliente",
                        colors = colors
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Cerrar
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.primaryOrange
                        ),
                        border = BorderStroke(1.dp, colors.primaryOrange)
                    ) {
                        Text("Cerrar")
                    }
                    // Botón Reprogramar (solo si no está cancelada o completada)
                    if (appointment.status !in listOf("CANCELLED", "CANCELADO", "COMPLETED", "COMPLETADO")) {
                        Button(
                            onClick = {
                                println("🔴 BOTÓN REPROGRAMAR CLICKEADO!")
                                println("🔴 Status de la cita: ${appointment.status}")
                                onEdit()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primaryOrange
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reprogramar")
                        }
                    } else {
                        println("⚠️ Botón Reprogramar NO se muestra. Status: ${appointment.status}")
                    }
                }
            }
        }
    }
}

/**
 * Badge de estado de la cita
 */
@Composable
private fun StatusBadge(status: String, colors: PrestadorColors) {
    val (text, backgroundColor, textColor) = when (status.uppercase()) {
        "PENDING", "PENDIENTE" -> Triple("Pendiente", Color(0xFFFEF3C7), Color(0xFF92400E))
        "CONFIRMED", "CONFIRMADO" -> Triple("Confirmada", Color(0xFFD1FAE5), Color(0xFF065F46))
        "CANCELLED", "CANCELADO" -> Triple("Cancelada", Color(0xFFFEE2E2), Color(0xFF991B1B))
        "COMPLETED", "COMPLETADO" -> Triple("Completada", Color(0xFFDBEAFE), Color(0xFF1E40AF))
        else -> Triple(status, Color(0xFFF3F4F6), Color(0xFF6B7280))
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

/**
 * Badge de tipo de servicio
 */
@Composable
private fun ServiceTypeBadge(serviceType: String, colors: PrestadorColors) {
    val (text, icon, color) = when (serviceType.uppercase()) {
        "TECHNICAL" -> Triple("Servicio Técnico", Icons.Default.Build, Color(0xFF6366F1))
        "PROFESSIONAL" -> Triple("Profesional con Turnos", Icons.Default.CalendarToday, Color(0xFF10B981))
        "RENTAL" -> Triple("Alquiler de Espacio", Icons.Default.Place, Color(0xFFF59E0B))
        else -> Triple("Otro Servicio", Icons.Default.Category, Color(0xFF6B7280))
    }
    
    Row(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Sección de información con título e icono
 */
@Composable
private fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: PrestadorColors,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primaryOrange,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }
        
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.surfaceColor
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

/**
 * Fila de información (label: value)
 */
@Composable
private fun InfoRow(label: String, value: String, colors: PrestadorColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Formatear fecha de yyyy-MM-dd a formato legible
 */
private fun formatDate(date: String): String {
    return try {
        val parts = date.split("-")
        val day = parts[2]
        val month = parts[1]
        val year = parts[0]
        
        val monthName = when (month) {
            "01" -> "Enero"
            "02" -> "Febrero"
            "03" -> "Marzo"
            "04" -> "Abril"
            "05" -> "Mayo"
            "06" -> "Junio"
            "07" -> "Julio"
            "08" -> "Agosto"
            "09" -> "Septiembre"
            "10" -> "Octubre"
            "11" -> "Noviembre"
            "12" -> "Diciembre"
            else -> month
        }
        
        "$day de $monthName de $year"
    } catch (e: Exception) {
        date
    }
}

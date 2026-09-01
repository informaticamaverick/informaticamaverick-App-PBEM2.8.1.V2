package com.example.myapplication.prestador.ui.pantallas.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showPreview by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFF97316)
                )
                Text(
                    "Configuración de Notificaciones",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Personaliza cómo recibes notificaciones de este chat:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                HorizontalDivider()

                // Activar/Desactivar notificaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Activar notificaciones",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Recibir alertas de nuevos mensajes",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                // Sonido
                AnimatedVisibility(visible = notificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Sonido",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Reproducir sonido de notificación",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                    }
                }

                // Vibración
                AnimatedVisibility(visible = notificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Vibración",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Vibrar al recibir mensaje",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
                        )
                    }
                }

                // Vista previa
                AnimatedVisibility(visible = notificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Vista previa",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Mostrar contenido del mensaje",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = showPreview,
                            onCheckedChange = { showPreview = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(notificationsEnabled)
                    onDismiss()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun VisibilitySettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var showAvatar by remember { mutableStateOf(true) }
    var showLastMessage by remember { mutableStateOf(true) }
    var showTimestamp by remember { mutableStateOf(true) }
    var showBadges by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color(0xFF10B981)
                )
                Text(
                    "Visibilidad de Datos",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Selecciona qué información quieres ver en la lista de chats:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                HorizontalDivider()

                // Mostrar avatar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mostrar avatares",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Ver fotos de perfil",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = showAvatar,
                        onCheckedChange = { showAvatar = it }
                    )
                }

                // Mostrar último mensaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Último mensaje",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Ver preview del mensaje",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = showLastMessage,
                        onCheckedChange = { showLastMessage = it }
                    )
                }

                // Mostrar timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Fecha y hora",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Ver cuándo llegó el mensaje",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = showTimestamp,
                        onCheckedChange = { showTimestamp = it }
                    )
                }

                // Mostrar badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Contador de mensajes",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Ver cantidad de no leídos",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = showBadges,
                        onCheckedChange = { showBadges = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(showAvatar)
                    onDismiss()
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DateRangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("Última semana") }
    val periods = listOf(
        "Hoy",
        "Ayer",
        "Última semana",
        "Último mes",
        "Últimos 3 meses",
        "Último año",
        "Todo el tiempo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B)
                )
                Text(
                    "Filtrar por Período",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    "Selecciona el período de tiempo para filtrar conversaciones:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                periods.forEach { period ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = period,
                            fontSize = 16.sp,
                            fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedPeriod)
                    onDismiss()
                }
            ) {
                Text("Filtrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun LockSettingsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var isLocked by remember { mutableStateOf(false) }
    var requirePassword by remember { mutableStateOf(false) }
    var hideNotifications by remember { mutableStateOf(false) }
    var archiveChat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
                Text(
                    "Privacidad y Seguridad",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Configura opciones de seguridad para este chat:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                HorizontalDivider()

                // Bloquear chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bloquear chat",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            "Impedir que esta persona te contacte",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isLocked,
                        onCheckedChange = { isLocked = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444)
                        )
                    )
                }

                // Requiere contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Requiere contraseña",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Pedir PIN para abrir el chat",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = requirePassword,
                        onCheckedChange = { requirePassword = it }
                    )
                }

                // Ocultar notificaciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Ocultar notificaciones",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "No mostrar contenido en alertas",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = hideNotifications,
                        onCheckedChange = { hideNotifications = it }
                    )
                }

                // Archivar chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Archivar conversación",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Mover a chats archivados",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = archiveChat,
                        onCheckedChange = { archiveChat = it }
                    )
                }

                if (isLocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Al bloquear este chat, ya no podrás recibir mensajes de esta persona.",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(isLocked)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocked) Color(0xFFEF4444) else Color(0xFFF97316)
                )
            ) {
                Text(if (isLocked) "Bloquear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

















































package com.example.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.local.MessageEntity
import com.example.myapplication.data.model.Provider
import com.example.myapplication.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// DIÁLOGO DE SOLICITUD DE PRESUPUESTO
// ==========================================
/**
 * Popup moderno para que el cliente solicite un presupuesto.
 * Permite seleccionar una dirección y describir el problema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetRequestDialog(
    provider: Provider,
    availableAddresses: List<AddressInfo>,
    onDismissRequest: () -> Unit,
    onAcceptRequest: (problem: String, address: String, lat: Double, lng: Double) -> Unit
) {
    var problemText by remember { mutableStateOf("") }
    var selectedAddress by remember { mutableStateOf(availableAddresses.firstOrNull()) }

    // Colores Identidad Maverick
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
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {

                // Efecto de brillo (Glow)
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 80.dp, y = (-80).dp)
                        .blur(60.dp)
                        .background(maverickBlue.copy(alpha = 0.1f), CircleShape)
                )

                Column(modifier = Modifier.fillMaxSize()) {
                    // --- SECCIÓN: CABECERA ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(maverickBlue.copy(alpha = 0.15f))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Solicitud de presupuesto",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = provider.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = maverickCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // --- SECCIÓN: CUERPO (Scrollable) ---
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Selección de Dirección
                        Text(
                            text = "1. SELECCIONÁ TU DIRECCIÓN",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (availableAddresses.isEmpty()) {
                            Text(
                                "No tienes direcciones guardadas. Por favor, añade una en tu perfil.",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
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

                        Spacer(Modifier.height(24.dp))

                        // 2. Descripción del Problema
                        Text(
                            text = "2. DETALLÁ TU NECESIDAD O PROBLEMA",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = problemText,
                            onValueChange = { problemText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            placeholder = { Text("Ej: Mi aire acondicionado no enfría...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = maverickBlue,
                                unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // --- SECCIÓN: PIE DE ACCIONES ---
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
                                    selectedAddress?.let { addr ->
                                        onAcceptRequest(problemText, addr.streetAndNumber, addr.lat, addr.lng)
                                    }
                                },
                                enabled = problemText.isNotBlank() && selectedAddress != null,
                                modifier = Modifier.weight(1.5f).height(54.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = maverickBlue,
                                    disabledContainerColor = Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("ENVIAR SOLICITUD", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// BURBUJA DE SOLICITUD DE PRESUPUESTO
// ==========================================
/**
 * Burbuja de chat para mostrar una solicitud de presupuesto.
 * Sigue el estilo de las burbujas de presupuesto pero con identidad azul.
 */
@Composable
fun BudgetRequestBubble(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors
) {
    val maverickBlue = Color(0xFF2197F5)
    val slateLight = Color(0xFFF8FAFC)
    val slateBorder = Color(0xFFE2E8F0)
    val slateText = Color(0xFF475569)
    val slateDark = Color(0xFF1E293B)
    val borderColor = if (isMe) maverickBlue else Color(0xFF10B981)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (isMe) 8.dp else 0.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 16.dp
                ))
                .background(Color.White)
                .border(0.5.dp, borderColor, RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 16.dp
                ))
        ) {
            // Header Azul PREMIUM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(maverickBlue.copy(alpha = 0.9f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📝", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SOLICITUD DE PRESUPUESTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "CLIENTE MAVERICK",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Cuerpo del mensaje
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(slateLight)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Detalle del problema:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = maverickBlue
                )
                Text(
                    text = message.content,
                    fontSize = 13.sp,
                    color = slateDark,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = slateBorder)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = maverickBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = message.locationAddress ?: "Dirección no especificada",
                        fontSize = 11.sp,
                        color = slateText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Footer con hora
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = slateText.copy(alpha = 0.4f)
                )
            }
        }
    }
}

package com.example.myapplication.presentation.components

import com.example.myapplication.core.domain.model.AddressUnico
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.core.data.local.entity.MessageEntity
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.BudgetStatus
import com.example.myapplication.core.data.local.entity.BudgetItem
import com.example.myapplication.core.data.local.entity.BudgetService
import com.example.myapplication.core.data.local.entity.ProviderEntity
import com.example.myapplication.core.domain.model.Provider
import com.example.myapplication.core.domain.model.MessageType
import com.example.myapplication.presentation.designsystem.theme.AppColors
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.theme.getThemeColors
import com.example.myapplication.uishared.components.BudgetPreviewPDFDialog
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// BURBUJAS DE PRESUPUESTO
// ==========================================

/**
 * Burbuja que muestra el resumen de un presupuesto formal enviado por el prestador.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BudgetBubble(
    message: MessageEntity,
    budget: BudgetEntity?,
    isMe: Boolean,
    appColors: AppColors,
    categoryEmoji: String? = null,
    providerEntity: ProviderEntity? = null, // 🔥 [NUEVO] Para el visor PDF
    onClick: () -> Unit
) {
    val budgetOrange = Color(0xFFFF6B35)
    val budgetAmber = Color(0xFFFFB300)
    val headerGradient = Brush.horizontalGradient(listOf(budgetOrange, budgetAmber))

    var showPdfViewer by remember { mutableStateOf(false) } // 🔥 [NUEVO]

    val borderColor =
        if (isMe) appColors.accentGreen.copy(alpha = 0.5f) else appColors.accentBlue.copy(alpha = 0.5f)

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = if (isMe) 12.dp else 4.dp, vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier
                .width(280.dp)
                .clickable { 
                    if (budget != null && providerEntity != null) showPdfViewer = true
                    else onClick() 
                },
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
                        sender = message.replyToSenderName,
                        content = message.replyToContent,
                        appColors = appColors,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                // --- HEADER: MODERNO Y LIMPIO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📄", fontSize = 18.sp)
                        Text(
                            text = "PRESUPUESTO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#${
                                budget?.budgetId?.takeLast(6) ?: message.relatedId?.takeLast(
                                    6
                                ) ?: "---"
                            }",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // --- CUERPO: ENFOQUE EN CATEGORÍA ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (budget == null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = budgetOrange
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = budgetOrange.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(categoryEmoji ?: "📋", fontSize = 16.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = budget.category?.uppercase() ?: "GENERAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = budgetOrange,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Servicio Profesional Detallado",
                                    fontSize = 12.sp,
                                    color = appColors.textSecondaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                // --- FOOTER: TOTAL Y VIGENCIA ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "TOTAL DEL PRESUPUESTO",
                            fontSize = 9.sp,
                            color = appColors.textSecondaryColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$ ${String.format(Locale.US, "%,.2f", budget?.grandTotal ?: 0.0)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = budgetOrange
                        )
                    }

                    if ((budget?.validityDays ?: 0) > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = budgetOrange.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "${budget?.validityDays} días",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textSecondaryColor
                            )
                        }
                    }
                }

                // --- ACCIÓN ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f))
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { 
                                if (budget != null && providerEntity != null) showPdfViewer = true
                                else onClick() 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = budgetOrange)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.OpenInNew,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "REVISAR DETALLES",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPdfViewer && budget != null && providerEntity != null) {
        BudgetPreviewPDFDialog(
            prestador = providerEntity,
            budget = budget,
            onDismiss = { showPdfViewer = false },
            showSendButton = false
        )
    }
}

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
    availableAddresses: List<AddressUnico>,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📝", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Solicitud de presupuesto",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = provider.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = maverickCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 36.dp)
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
                            placeholder = {
                                Text(
                                    "Ej: Mi aire acondicionado no enfría...",
                                    color = Color.Gray
                                )
                            },
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
                                Text(
                                    "CANCELAR",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    selectedAddress?.let { addr ->
                                        onAcceptRequest(
                                            problemText,
                                            addr.calle,
                                            addr.latitude,
                                            addr.longitude
                                        )
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
@Composable
fun BudgetRequestBubble(
    message: MessageEntity,
    isMe: Boolean,
    appColors: AppColors
) {
    val budgetOrange = Color(0xFFFF6B35)
    val budgetAmber = Color(0xFFFFB300)
    val headerGradient = Brush.horizontalGradient(listOf(budgetOrange, budgetAmber))

    val borderColor =
        if (isMe) appColors.accentGreen.copy(alpha = 0.6f) else appColors.accentBlue.copy(alpha = 0.6f)
    val borderWeight = if (isMe) 1.5.dp else 1.dp

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = if (isMe) 12.dp else 4.dp, vertical = 4.dp),
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
            border = BorderStroke(borderWeight, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                if (message.replyToId != null) {
                    QuotedMessage(
                        sender = message.replyToSenderName,
                        content = message.replyToContent,
                        appColors = appColors,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                // --- HEADER: CONSISTENTE CON PRESUPUESTO ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📝", fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SOLICITUD DE PRESUPUESTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                // --- CUERPO: DETALLE Y LOCALIZACIÓN ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "DETALLE DEL PROBLEMA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = budgetOrange,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = message.content,
                            fontSize = 13.sp,
                            color = appColors.textPrimaryColor,
                            lineHeight = 18.sp
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = budgetOrange.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = budgetOrange,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = message.locationAddress ?: "Dirección no especificada",
                                fontSize = 11.sp,
                                color = appColors.textSecondaryColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // --- FOOTER: HORA ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
/**
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                                Date(
                                    message.timestamp
                                )
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors.textSecondaryColor.copy(alpha = 0.5f)
                        )
                        */
                    }
                }
            }
        }
    }


// ==========================================
// SECCIÓN DE VISTAS PREVIAS (PREVIEWS)
// ==========================================

@Preview(showBackground = true, name = "Burbuja Solicitud Presupuesto")
@Composable
fun BudgetRequestBubblePreview() {
    val appColors = getThemeColors()
    val message = MessageEntity(
        id = "1",
        chatId = "c1",
        senderId = "user1",
        receiverId = "p1",
        type = MessageType.BUDGET_REQUEST,
        content = "Necesito presupuesto para arreglar un lavarropas que no desagota.",
        locationAddress = "Av. Siempre Viva 742",
        timestamp = System.currentTimeMillis()
    )
    MyApplicationTheme {
        Box(
            modifier = Modifier.fillMaxWidth().background(appColors.backgroundColor)
                .padding(16.dp)
        ) {
            BudgetRequestBubble(message = message, isMe = true, appColors = appColors)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Burbuja Presupuesto Formal")
@Composable
fun BudgetBubblePreview() {
        val appColors = getThemeColors()
        val message = MessageEntity(
            id = "1",
            chatId = "c1",
            senderId = "p1",
            receiverId = "user1",
            type = MessageType.BUDGET,
            content = "Presupuesto enviado",
            timestamp = System.currentTimeMillis()
        )
        val budget = BudgetEntity(
            budgetId = "b1",
            clientId = "u1",
            providerId = "p1",
            providerName = "Juan Perez",
            providerCompanyName = "Maverick Tech Solutions",
            category = "Refrigeración",
            items = listOf(
                BudgetItem(description = "Compresor 1/4 HP", quantity = 1, unitPrice = 45000.0),
                BudgetItem(description = "Carga de Gas R134", quantity = 1, unitPrice = 12000.0)
            ),
            services = listOf(
                BudgetService(description = "Mano de obra especializada", total = 15000.0)
            ),
            subtotal = 72000.0,
            grandTotal = 87120.0,
            validityDays = 5,
            //status = com.example.myapplication.data.local.BudgetStatus.PENDIENTE,
            dateTimestamp = System.currentTimeMillis()
        )
        MyApplicationTheme {
            Box(
                modifier = Modifier.fillMaxWidth().background(appColors.backgroundColor)
                    .padding(16.dp)
            ) {
                BudgetBubble(
                    message = message,
                    budget = budget,
                    isMe = false,
                    appColors = appColors,
                    onClick = {})
            }
        }
    }

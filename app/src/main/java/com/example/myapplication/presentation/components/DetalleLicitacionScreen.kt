package com.example.myapplication.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.local.TenderEntity
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val PremiumPink = Color(0xFFE91E63)

@Composable
private fun DetailSectionPremium(emoji: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun RequirementChipPremium(text: String) {
    Surface(
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DateItemPremium(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * --- POPUP DE DETALLES DE LICITACIÓN ---
 * Muestra información extendida de una licitación y permite gestionar su estado.
 */
@Composable
fun TenderDetailPopup(
    tender: TenderEntity,
    onClose: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onContactProvider: (String) -> Unit = {},
    onViewProviderProfile: (String) -> Unit = {},
    onViewAwardedBudget: (String) -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val creationDate = dateFormat.format(Date(tender.dateTimestamp))
    val endDate = if (tender.endDate > 0) dateFormat.format(Date(tender.endDate)) else "No definida"
    
    var showCancelWarning by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    BorderStroke(1.dp, Brush.verticalGradient(listOf(MaverickBlue.copy(0.4f), Color.Transparent))),
                    RoundedCornerShape(24.dp)
                ),
            color = CardSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                // --- CABECERA PREMIUM CON GRADIENTE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(MaverickBlue.copy(0.2f), Color.Transparent)
                            )
                        )
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .background(Color.Red.copy(0.15f), CircleShape)
                            .border(1.dp, Color.Red.copy(0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "📋 DETALLES TÉCNICOS",
                            color = MaverickBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = tender.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 24.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    
                    // --- ESTADO Y CATEGORÍA ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPillPremium(tender.status)
                        
                        Surface(
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.1f))
                        ) {
                            Text(
                                text = "🏷️ ${tender.category.uppercase()}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // --- SECCIÓN: DESCRIPCIÓN ---
                    DetailSectionPremium("📝", "MEMORIA DESCRIPTIVA", tender.description)
                    
                    Spacer(Modifier.height(20.dp))

                    // --- SECCIÓN: UBICACIÓN ---
                    if (tender.locationAddress != null) {
                        DetailSectionPremium(
                            "📍", 
                            "UBICACIÓN", 
                            "${tender.locationAddress} ${tender.locationNumber ?: ""}, ${tender.locationLocality ?: ""}"
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    // --- GALERÍA DE IMÁGENES ---
                    if (tender.imageUrls.isNotEmpty()) {
                        Text(
                            "📸 REGISTRO VISUAL", 
                            color = Color.Gray, 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(tender.imageUrls) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // --- REQUISITOS FORMALES ---
                    if (tender.requiresVisit || tender.requiresPaymentMethod || tender.requiresWorkGuarantee || tender.requiresProviderDoc) {
                        Text(
                            "⚖️ CLÁUSULAS Y REQUISITOS", 
                            color = Color.Gray, 
                            fontSize = 9.sp, 
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        OptInFlowRow {
                            if (tender.requiresVisit) RequirementChipPremium("🛠️ Visita Técnica Obra")
                            if (tender.requiresPaymentMethod) RequirementChipPremium("💳 Método de Pago")
                            if (tender.requiresWorkGuarantee) RequirementChipPremium("🛡️ Garantía de Obra")
                            if (tender.requiresProviderDoc) RequirementChipPremium("📄 Documentación Legal")
                        }
                        Spacer(Modifier.height(20.dp))
                    }

                    // --- FECHAS CRÍTICAS ---
                    Surface(
                        color = Color.Black.copy(0.3f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DateItemPremium("📅 INICIO", creationDate)
                            Box(Modifier.width(1.dp).height(30.dp).background(Color.White.copy(0.1f)))
                            DateItemPremium("🏁 CIERRE", endDate)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- SECCIÓN DE ADJUDICACIÓN (PREMIUM SEPARADA) ---
                    if (tender.status == "ADJUDICADA" && tender.awardedProviderName != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "🏆 RESULTADO DE LICITACIÓN",
                                color = MaverickBlue,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            
                            // TARJETA DEL PRESTADOR
                            Surface(
                                onClick = { onViewProviderProfile(tender.awardedProviderId ?: "") },
                                color = Color.White.copy(0.05f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = tender.awardedProviderPhotoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, MaverickBlue, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PRESTADOR ADJUDICADO", color = MaverickBlue, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        Text(tender.awardedProviderName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    IconButton(
                                        onClick = { onContactProvider(tender.awardedProviderId ?: "") },
                                        modifier = Modifier.background(MaverickBlue, CircleShape).size(32.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // TARJETA DEL PRESUPUESTO
                            Surface(
                                onClick = { onViewAwardedBudget(tender.awardedBudgetId ?: "") },
                                color = MaverickBlue.copy(0.05f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaverickBlue.copy(0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(44.dp).background(MaverickBlue.copy(0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📄", fontSize = 20.sp)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("PRESUPUESTO GANADOR", color = MaverickBlue, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        Text("#${tender.awardedBudgetId?.takeLast(8)?.uppercase()}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Text("VER A4", color = MaverickBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = MaverickBlue, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    // --- BOTONES DE ACCIÓN ---
                    if (tender.status == "ABIERTA") {
                        Button(
                            onClick = { showCancelWarning = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.1f)),
                            border = BorderStroke(1.dp, Color.Red.copy(0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("❌ CANCELAR LICITACIÓN", color = Color.Red, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE ADVERTENCIA DE CANCELACIÓN ---
    if (showCancelWarning) {
        AlertDialog(
            onDismissRequest = { showCancelWarning = false },
            containerColor = Color(0xFF1A1C1E),
            icon = { Icon(Icons.Default.Warning, null, tint = Color.Yellow, modifier = Modifier.size(40.dp)) },
            title = { Text("¿Terminar de manera abrupta?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { 
                Text(
                    "Estás por CANCELAR esta licitación de forma forzosa. " +
                    "Los prestadores que ya enviaron presupuestos serán notificados y ya no se podrán recibir nuevas ofertas. " +
                    "\n\n¿Deseas continuar?",
                    color = Color.Gray,
                    fontSize = 14.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus("CANCELADA")
                        showCancelWarning = false
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("SÍ, CANCELAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelWarning = false }) {
                    Text("VOLVER", color = Color.Gray)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInFlowRow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TenderDetailPopupPreview() {
    val sampleTenderOpen = TenderEntity(
        tenderId = "T-12345678",
        title = "Reparación de Sistema Eléctrico Industrial",
        clientId = "client_01",
        description = "Se requiere la revisión y reparación de tableros eléctricos principales en planta industrial. Incluye verificación de térmicas y cableado de potencia.",
        category = "Electricidad",
        status = "ABIERTA",
        dateTimestamp = System.currentTimeMillis() - 86400000,
        startDate = System.currentTimeMillis() - 86400000,
        endDate = System.currentTimeMillis() + 86400000 * 5,
        requiresVisit = true,
        requiresWorkGuarantee = true,
        locationAddress = "Av. Siempre Viva 742",
        locationLocality = "Springfield",
        imageUrls = listOf("https://picsum.photos/seed/elec1/400/300", "https://picsum.photos/seed/elec2/400/300")
    )

    val sampleTenderAwarded = sampleTenderOpen.copy(
        tenderId = "T-87654321",
        status = "ADJUDICADA",
        awardedProviderId = "prov_maverick",
        awardedProviderName = "Maverick Tech S.A.",
        awardedBudgetId = "BUD-99001122",
        awardedProviderPhotoUrl = "https://picsum.photos/seed/maverick/200/200"
    )

    MyApplicationTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("VISTA: LICITACIÓN ABIERTA", color = Color.White, fontWeight = FontWeight.Bold)
            TenderDetailPopup(
                tender = sampleTenderOpen,
                onClose = {},
                onUpdateStatus = {}
            )
            
            Spacer(Modifier.height(40.dp))
            
            Text("VISTA: LICITACIÓN ADJUDICADA", color = Color.White, fontWeight = FontWeight.Bold)
            TenderDetailPopup(
                tender = sampleTenderAwarded,
                onClose = {},
                onUpdateStatus = {}
            )
        }
    }
}

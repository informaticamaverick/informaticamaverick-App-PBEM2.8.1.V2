package com.example.myapplication.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

// --- PALETA DE COLORES PREMIUM ---
private val DarkBackground = Color(0xFF05070A)
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusActive = Color(0xFF38BDF8)
private val StatusFinished = Color(0xFF34D399)
private val StatusWarning = Color(0xFFF87171)
private val NeonCyber = Color(0xFF00FFC2)
// --- DEFINICIÓN DE COLORES ---
val DarkCardBg = Color(0xFF1A1F26)
val DarkBottomBg = Color(0xFF0A0E14)
/*** Componente Visual del Checkbox Premium*/
@Composable
fun SelectionIndicator(isSelected: Boolean, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaverickBlue else Color.White.copy(alpha = 0.1f), label = ""
    )
    val iconColor by animateColorAsState(
        if (isSelected) Color.White else Color.Transparent, label = ""
    )
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, if (isSelected) MaverickBlue else Color.White.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconColor
            )
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuestoPremium(
    providerName: String,
    companyName: String?,
    amount: Double,
    budgetId: String,
    category: String = "Servicio", // Se conserva para la UI mejorada
    photoUrl: String?,
    isOnline: Boolean = false,
    isSubscribed: Boolean = false,
    isSelected: Boolean = false,
    isRead: Boolean = false,
    isMultiSelectionActive: Boolean = false,
    onViewClick: () -> Unit,
    onChatClick: () -> Unit,
    onAvatarClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaverickBlue else Color.White.copy(alpha = 0.08f)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(DarkCardBg, DarkBottomBg)
    )

    Surface(
        modifier = modifier
            .width(118.dp) // Tamaño optimizado para 3 por fila
            .height(180.dp)
            .combinedClickable(
                onClick = {
                    if (isMultiSelectionActive) {
                        onLongClick()
                    } else {
                        onViewClick()
                    }
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 12.dp else 4.dp
    ) {
        Box(modifier = Modifier.background(backgroundBrush)) {

            // --- INDICADOR DE SELECCIÓN (Conserva tu componente SelectionIndicator) ---
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .zIndex(30f)
                ) {
                    SelectionIndicator(isSelected = true)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                // --- CABECERA: AVATAR + TEXTOS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier
                        .size(40.dp)
                        .clickable { onAvatarClick() }
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(0.1f), CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        // PUNTO VERDE: CORNER IZQUIERDO SUPERIOR
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopStart)
                                    .offset(x = (-2).dp, y = (-2).dp)
                                    .background(StatusActive, CircleShape)
                                    .border(1.5.dp, DarkCardBg, CircleShape)
                                    .zIndex(10f)
                            )
                        }

                        // ICONO SUSCRIPTO: CORNER DERECHO INFERIOR
                        if (isSubscribed) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 2.dp, y = 2.dp)
                                    .background(Color(0xFFF59E0B), CircleShape)
                                    .border(1.5.dp, DarkCardBg, CircleShape)
                                    .zIndex(10f),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(2.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = providerName,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = (companyName ?: "Independiente").uppercase(),
                            color = MaverickBlue,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // --- DIVIDER SUPERIOR ---
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White.copy(0.3f), Color.Transparent)
                            )
                        )
                )

                // --- SECCIÓN CENTRAL: ID/CATEGORIA Y PRECIO ---
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
                    // FILA 1: ID | CATEGORIA
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${budgetId.takeLast(4).uppercase()}",
                            color = Color.Gray,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(modifier = Modifier.width(1.dp).height(8.dp).background(Color.White.copy(0.2f)))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = category.uppercase(),
                            color = MaverickBlue.copy(alpha = 0.8f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(2.dp))

                    // FILA 2: TOTAL Y PRECIO (Alineado a la Izquierda)
                    Text(
                        text = "TOTAL PRESUPUESTO",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 6.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$ ${String.format(Locale.getDefault(), "%,.0f", amount)}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(2.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White.copy(0.5f), Color.Transparent)
                            )
                        )
                )
                Spacer(Modifier.weight(1f))

                // --- BOTONES FINALES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Botón de Chat con Emoji
                    Surface(
                        onClick = onChatClick,
                        modifier = Modifier.size(28.dp),
                        color = Color.White.copy(0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💬", fontSize = 16.sp)
                        }
                    }

                    // Botón VER (Verde StatusActive)
                    Button(
                        onClick = onViewClick,
                        modifier = Modifier.weight(2f).height(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRead) Color.White.copy(0.1f) else StatusActive,
                            contentColor = if (isRead) Color.Gray else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (isRead) "VISTO" else "VER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LicitacionFolderPremium(
    title: String,
    category: String,
    categoryIcon: String = "📋",
    categoryColor: Color = Color.Gray,
    // --- NUEVO: Color de la Supercategoría (Usado para Pestaña y Borde) ---
    supercategoryColor: Color = Color.Gray,
    tenderId: String,
    status: String,
    startDate: Long,
    endDate: Long,
    budgetCount: Int,
    unreadCount: Int,
    isSelected: Boolean,
    awardedProviderName: String? = null,
    awardedBudgetId: String? = null,
    // --- NUEVO: URL de la imagen del proveedor adjudicado ---
    awardedProviderPhotoUrl: String? = null,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
) {
    // --- LÓGICA DE TIEMPO Y AUTO-CIERRE ---
    val now = System.currentTimeMillis()
    val isExpired = now > endDate && endDate != 0L
    val effectiveStatus = if (isExpired && (status == "ABIERTA" || status == "ACTIVO")) "CERRADA" else status
    
    val remainingDays = if (endDate > now) {
        TimeUnit.MILLISECONDS.toDays(endDate - now)
    } else 0
    
    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // --- 1. LÓGICA DE COLOR: SE USA SUPERCATEGORÍA PARA EL BORDE ---
    val borderColor = if (isSelected) MaverickBlue else supercategoryColor.copy(alpha = 0.3f)

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 28.dp, start = 4.dp, end = 4.dp, bottom = 8.dp)
    ) {
        // --- 1. PESTAÑA DEL FOLDER (Categoría - Usa Color de Supercategoría) ---
        Surface(
            modifier = Modifier
                .offset(y = (-28).dp)
                .width(160.dp)
                .height(28.dp)
                .zIndex(1f),
            color = supercategoryColor.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 10.dp),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(categoryIcon, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    // --- COLOR DEL TEXTO: Usa Supercategoría ---
                    color = supercategoryColor,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- 2. CONTADOR DE MENSAJES NUEVOS (Badge Rectangular) ---
        if (unreadCount > 0) {
            Surface(
                modifier = Modifier
                    .offset(x = 155.dp, y = (-28).dp)
                    .zIndex(10f),
                color = NeonCyber,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, DarkBackground)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "$unreadCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkBackground,
                        lineHeight = 11.sp
                    )
                    Text(
                        "NUEVO",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkBackground,
                        lineHeight = 8.sp
                    )
                }
            }
        }

        // --- 3. CUERPO DE LA CARPETA ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            color = CardSurface,
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, bottomStart = 8.dp),
            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
            shadowElevation = if (isSelected) 12.dp else 4.dp
        ) {
            Column(Modifier.padding(10.dp)) {
                
                // --- FILA SUPERIOR: CAJA 1 (Info) y CAJA 2 (Estado/Presupuestos) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // --- CAJA 1: NOMBRE E ID ---
                    Column(verticalArrangement = Arrangement.spacedBy((-10).dp),
                        modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp))

                    {
                        Text(
                            text = "NOMBRE",
                            color = Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            lineHeight = 20.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(Modifier.height(30.dp))
                        
                        Text(
                            text = "CODIGO",
                            color = Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "#${tenderId.takeLast(8).uppercase()}",
                            color = MaverickBlue.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // --- CAJA 2: ESTADO, DÍAS Y PRESUPUESTOS ---
                    Column(horizontalAlignment = Alignment.Start) {
                        StatusPillPremium(effectiveStatus)
                        
                        Spacer(Modifier.height(4.dp))
                        
                        // Días restantes con Emoji ⏳
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⏳", fontSize = 10.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (remainingDays > 0) "Quedan $remainingDays días" else "Finalizado",
                                fontSize = 10.sp,
                                color = if (remainingDays < 3 && remainingDays > 0) StatusWarning else MaverickBlue,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Caja de Presupuestos Recibidos (Formato Modular)
                        Surface(
                            color = Color.Black.copy(0.3f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,

                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PRESUPUESTOS", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Gray, lineHeight = 8.sp)
                                    Text("RECIBIDOS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White, lineHeight = 9.sp)
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(modifier = Modifier.width(1.dp).height(18.dp).background(Color.White.copy(0.1f)))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = budgetCount.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaverickBlue
                                )
                            }
                        }
                    }
                }

                // --- DIVIDER HORIZONTAL ---
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth(),
                    color = Color.White.copy(0.15f),
                    thickness = 0.5.dp
                )

                // --- SECCIÓN INFERIOR DINÁMICA: ADJUDICACIÓN O FECHAS ---
                if (effectiveStatus == "ADJUDICADA" && awardedProviderName != null) {
                    // MODO ADJUDICADA: Imagen/Icono + Nombre + Presupuesto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        // --- IMAGEN DEL PROVEEDOR (Centrado a la izquierda del nombre) ---
                        if (awardedProviderPhotoUrl != null) {
                            AsyncImage(
                                model = awardedProviderPhotoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, StatusActive.copy(alpha = 0.5f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))
                        }

                        //Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                //Text = "", fontSize = 10.sp,
                                text = "🏆 ADJUDICADO A:",
                                fontSize = 7.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = awardedProviderName.uppercase(),
                                fontSize = 11.sp,
                                color = StatusActive,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color.White.copy(0.1f)))
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PRESUPUESTO",
                                fontSize = 7.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "#${awardedBudgetId?.takeLast(4)?.uppercase() ?: "----"}",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (effectiveStatus == "CERRADA" && awardedProviderName == null) {
                    // MODO CERRADA SIN ADJUDICAR
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "SIN ADJUDICAR",
                            color = StatusWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                } else {
                    // MODO NORMAL: FECHAS CON EMOJIS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fecha Inicio 📅
                        Column {
                          Row {
                                Text("📅", fontSize = 10.sp)
                                Text("FECHA INICIO", fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Text(df.format(Date(startDate)), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                           // }
                        }

                        // Fecha Cierre 🏁
                        Column(verticalArrangement = Arrangement.Center) {
                        Row() {
                            // Column(horizontalAlignment = Alignment.End) {
                            Text("🏁", fontSize = 10.sp)
                            Text(
                                "FECHA CIERRE",
                                fontSize = 7.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                                Text(df.format(Date(endDate)), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, date: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(10.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text(date.uppercase(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusPillPremium(status: String) {
    val upperStatus = status.uppercase()
    val color = when(upperStatus) {
        "ACTIVO", "ABIERTA" -> Color(0xFF10B981) // Verde esmeralda
        "ADJUDICADO", "ADJUDICADA" -> Color(0xFF0EA5E9) // Celeste sky
        "TERMINADO", "CERRADA" -> Color(0xFFF43F5E) // Rojo rose
        else -> Color.Gray
    }
    val isCancelled = upperStatus == "CANCELADA"
    val finalColor = if (isCancelled) Color.Gray else color

    Surface(
        color = finalColor.copy(0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, finalColor.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de punto de color
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(finalColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                upperStatus,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = finalColor,
                letterSpacing = 1.sp,
                textDecoration = if (isCancelled) TextDecoration.LineThrough else null
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun TarjetaPresupuestoPremiumPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TarjetaPresupuestoPremium(
                providerName = "Maverick Informática",
                companyName = "Maverick Tech S.A.",
                amount = 25000.0,
                budgetId = "PRE-12345",
                category = "Informatica",
                photoUrl = "https://picsum.photos/seed/maverick/200/200",
                isOnline = true,
                isSubscribed = true,
                isSelected = false,
                isRead = false,
                onViewClick = {},
                onChatClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun LicitacionFolderPremiumPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Caso 1: Licitación Abierta
            LicitacionFolderPremium(
                title = "Reparación de Aire Acondicionado",
                category = "Climatización",
                categoryIcon = "❄️",
                categoryColor = Color(0xFF38BDF8),
                supercategoryColor = Color(0xFF0EA5E9), // Ejemplo Supercategoría
                tenderId = "T-AB-1-ABCD",
                status = "ABIERTA",
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + 86400000 * 5,
                budgetCount = 5,
                unreadCount = 2,
                isSelected = false,
                onClick = {}
            )

            // Caso 2: Licitación Adjudicada
            LicitacionFolderPremium(
                title = "Mantenimiento Preventivo IT",
                category = "Informática",
                categoryIcon = "💻",
                categoryColor = MaverickBlue,
                supercategoryColor = MaverickPurple, // Ejemplo Supercategoría
                tenderId = "T-AD-2-EFGH",
                status = "ADJUDICADA",
                startDate = System.currentTimeMillis() - 86400000 * 10,
                endDate = System.currentTimeMillis() - 86400000 * 2,
                budgetCount = 12,
                unreadCount = 0,
                isSelected = false,
                awardedProviderName = "Maverick Tech S.A.",
                awardedBudgetId = "BUD-9999",
                awardedProviderPhotoUrl = "https://picsum.photos/seed/provider/200/200", // Foto de prueba
                onClick = {}
            )
        }
    }
}

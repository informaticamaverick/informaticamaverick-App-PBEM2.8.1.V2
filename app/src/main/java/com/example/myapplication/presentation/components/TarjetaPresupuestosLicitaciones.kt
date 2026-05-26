package com.example.myapplication.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.BudgetStatus
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// --- PALETA DE COLORES PREMIUM ---
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusActive = Color(0xFF38BDF8)
private val StatusWarning = Color(0xFFF87171)
private val NeonCyber = Color(0xFF00FFC2)
// --- DEFINICIÓN DE COLORES ---
val DarkCardBg = Color(0xFF1A1F26)
val DarkBottomBg = Color(0xFF0A0E14)

/**
 * Forma personalizada para las tarjetas de presupuesto:
 * - Esquinas superiores con corte (beveled) de 5dp.
 * - Esquinas inferiores casi rectas (redondeo mínimo de 2dp).
 */
val BudgetCardShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutSize = with(density) { 5.dp.toPx() }
        val cornerRadius = with(density) { 2.dp.toPx() }
        val path = Path().apply {
            moveTo(cutSize, 0f)
            lineTo(size.width - cutSize, 0f)
            lineTo(size.width, cutSize)
            lineTo(size.width, size.height - cornerRadius)
            arcTo(
                rect = Rect(size.width - 2 * cornerRadius, size.height - 2 * cornerRadius, size.width, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(cornerRadius, size.height)
            arcTo(
                rect = Rect(0f, size.height - 2 * cornerRadius, 2 * cornerRadius, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(0f, cutSize)
            close()
        }
        return Outline.Generic(path)
    }
}

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
    modifier: Modifier = Modifier,
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
    onLongClick: () -> Unit = {}

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

/**
 * --- COMPONENTE: LICITACION FOLDER PREMIUM ---
 * Representa una licitación con estilo "Windows 11" (Ventana).
 */
@Composable
private fun HeaderActionButton(
    emoji: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(28.dp),
        color = Color.White.copy(0.1f),
        shape = CircleShape,
        border = BorderStroke(1.dp, Color.White.copy(0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            } else if (emoji != null) {
                Text(emoji, fontSize = 14.sp)
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
    awardedProviderPhotoUrl: String? = null,
    onLongClick: () -> Unit = {},
    onViewDetails: () -> Unit = {},
    onClick: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isExpired = now > endDate && endDate != 0L
    val effectiveStatus = if (isExpired && (status == "ABIERTA" || status == "ACTIVO")) "CERRADA" else status
    
    val remainingDays = if (endDate > now) {
        TimeUnit.MILLISECONDS.toDays(endDate - now)
    } else 0
    
    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // --- ESTADOS DE INTERACCIÓN M3 ---
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val tonalAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.15f else if (isSelected) 0.08f else 0.02f,
        label = "tonalAlpha"
    )
    val animatedBgColor = MaverickBlue.copy(alpha = tonalAlpha).compositeOver(CardSurface)
    
    val borderColor = if (isSelected || isPressed) MaverickBlue else Color.White.copy(alpha = 0.15f)

    // --- FORMA DE VENTANA WINDOWS 11 (Esquinas más rectas - Premium V2) ---
    val windowShape = RoundedCornerShape(4.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp) // Más espacio para la sombra
            .drawBehind {
                // --- SOMBRA 3D PERSONALIZADA (Bottom & Sides only) ---
                val shadowColor = Color.Black.copy(alpha = if (isPressed) 1f else 1f)
                val shadowRadius = if (isPressed) 12.dp.toPx() else 8.dp.toPx()
                val offsetY = if (isPressed) 6.dp.toPx() else 4.dp.toPx()

                drawIntoCanvas { canvas ->
                    val paint = Paint().asFrameworkPaint().apply {
                        color = shadowColor.toArgb()
                        setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb())
                    }
                    canvas.nativeCanvas.drawRoundRect(
                        0f,
                        offsetY, // Empezamos desde abajo para que no se vea arriba
                        size.width,
                        size.height,
                        4.dp.toPx(),
                        4.dp.toPx(),
                        paint
                    )
                }
            }
            .graphicsLayer {
                val scale = if (isPressed) 0.98f else 1f
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = animatedBgColor,
        shape = windowShape,
        border = BorderStroke(if (isSelected || isPressed) 2.dp else 1.dp, borderColor),
        shadowElevation = 0.dp // Desactivamos la elevación estándar para usar nuestra sombra 3D
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            
            // --- 1. CABECERA ESTILO WINDOWS 11 (Refinamiento Glassmorphism 2.0) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(supercategoryColor.copy(alpha = 0.25f), supercategoryColor.copy(alpha = 0.05f))
                        )
                    )
                    .drawBehind {
                        // RIM LIGHTING: Brillo sutil en el borde superior
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(0f, 0.5.dp.toPx()),
                            end = Offset(size.width, 0.5.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(categoryIcon, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.width(1.dp).height(14.dp).background(Color.White.copy(0.2f)))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = category.uppercase(),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(Modifier.weight(1f))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HeaderActionButton(emoji = "❗", onClick = onViewDetails)
                        HeaderActionButton(icon = Icons.Default.ArrowUpward, onClick = onClick)
                    }
                }
               /**
                // LÍNEA DE ACENTO INFERIOR (Sutil)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.4f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(supercategoryColor.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                )
                */
            }

            // --- DIVIDER DE PROFUNDIDAD PARA LA CABECERA ---
            DepthDividerHorizontal(
                shadowColor = Color.Black.copy(alpha = 0.5f),
                highlightColor = Color.White.copy(alpha = 0.05f)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // --- SECCIÓN CENTRAL: DOS COLUMNAS (INFO VS ESTADO/PRESUPUESTO) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // COLUMNA IZQUIERDA: TÍTULO
                    Column(modifier = Modifier.weight(1f)) {
                        // --- SECCIÓN SUPERIOR: ENCABEZADO TÉCNICO (ETIQUETA E ID) ---
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏷️ NOMBRE DEL PROYECTO",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(Modifier.width(1.dp).height(10.dp).background(Color.White.copy(0.2f)))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "#${tenderId.takeLast(8).uppercase()}",
                                color = MaverickBlue.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // COLUMNA DERECHA: ESTADO Y TARJETA PRESUPUESTOS
                    Column(
                        modifier = Modifier.width(115.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. ESTADO
                        StatusPillPremium(status = effectiveStatus, modifier = Modifier.wrapContentSize() .fillMaxWidth())

                        // 2. TARJETA PRESUPUESTOS
                        Surface(
                            color = Color.Black.copy(0.3f), // Fondo más oscuro premium
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.15f)) // Bordes más resaltados
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "PRESUPUESTOS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 1.sp
                                )
                                HorizontalDivider( color = Color.White.copy(0.2f), thickness = 1.dp)
                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // NUEVOS
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = unreadCount.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (unreadCount > 0) NeonCyber else Color.Gray.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            "NUEVOS",
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (unreadCount > 0) NeonCyber.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f)
                                        )
                                    }
                                    
                                    // RECIBIDOS
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = budgetCount.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (budgetCount > 0) MaverickBlue else Color.Gray.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            "RECIBIDOS",
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (budgetCount > 0) MaverickBlue.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                DepthDividerHorizontal(
                    shadowColor = Color.Black.copy(alpha = 0.4f),
                    highlightColor = Color.White.copy(alpha = 0.03f)
                )
                Spacer(Modifier.height(16.dp))

                // --- SECCIÓN: FECHAS Y TIEMPO (REORGANIZADO) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        DateInfoRowEmoji("📅", "INICIO", df.format(Date(startDate)))
                        DateInfoRowEmoji("🏁", "CIERRE", df.format(Date(endDate)))
                    }

                    // BADGE DE TIEMPO (Temporizador prolijo)
                    Surface(
                        color = if (remainingDays in 1..2) StatusWarning.copy(0.1f) else Color.White.copy(0.05f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (remainingDays in 1..2) StatusWarning.copy(0.3f) else Color.White.copy(0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(if (remainingDays > 0) "⏳" else "🏁", fontSize = 12.sp)
                            Text(
                                text = if (remainingDays > 0) "FALTAN $remainingDays DÍAS" else "FINALIZADO",
                                fontSize = 8.sp,
                                color = if (remainingDays in 1..2) StatusWarning else Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // --- SECCIÓN INFERIOR: ADJUDICACIÓN ---
                if (effectiveStatus == "ADJUDICADA" && awardedProviderName != null) {
                    Spacer(Modifier.height(16.dp))
                    DepthDividerHorizontal(
                        shadowColor = Color.Black.copy(alpha = 0.6f),
                        highlightColor = Color.White.copy(alpha = 0.08f)
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(awardedProviderPhotoUrl)
                                    .crossfade(true)
                                    .size(120, 120) // 🔥 [OPTIMIZACIÓN ELITE] Evita cargar imágenes gigantes en RAM
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, StatusActive, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text("🏆", modifier = Modifier.align(Alignment.TopStart).offset(x = (-4).dp, y = (-4).dp), fontSize = 12.sp)
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PROVEEDOR ADJUDICADO", color = StatusActive, fontSize = 7.sp, fontWeight = FontWeight.Black)
                            Text(awardedProviderName.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PRESUPUESTO", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            Text("#${awardedBudgetId?.takeLast(6)?.uppercase() ?: "----"}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                } else if (effectiveStatus == "CERRADA" && awardedProviderName == null) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StatusWarning.copy(0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚠️ LICITACIÓN SIN ADJUDICAR", color = StatusWarning, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}


@Composable
fun DateInfoRowEmoji(emoji: String, label: String, date: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 12.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 7.sp, color = Color.Gray, fontWeight = FontWeight.Black)
            Text(date, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusPillPremium(
    status: String,
    modifier: Modifier = Modifier
) {
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
        color = finalColor.copy(0.15f), // Más opaco para resaltar
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.2.dp, finalColor.copy(0.5f)), // Borde más sólido y marcado
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Indicador de punto de color
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(finalColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                upperStatus,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold, // Texto más grueso
                color = finalColor,
                letterSpacing = 1.sp,
                textDecoration = if (isCancelled) TextDecoration.LineThrough else null
            )
        }
    }
}


@Composable
fun BudgetStatusBadge(status: BudgetStatus) {
    val (color, emoji) = when (status) {
        BudgetStatus.PENDIENTE -> Color(0xFFFACC15) to "⏳"
        BudgetStatus.ACEPTADO -> Color(0xFF10B981) to "✅"
        BudgetStatus.RECHAZADO -> Color(0xFFEF4444) to "❌"
        else -> Color.Gray to "📄"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = CircleShape,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = emoji,
            fontSize = 8.sp,
            modifier = Modifier.padding(2.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetCard(
    modifier: Modifier = Modifier,
    budget: BudgetEntity,
    isSelected: Boolean = false,
    isMultiSelectionActive: Boolean = false,
    categoryEmoji: String? = null,
    cardWidth: Dp = 118.dp, // Variable para ajustar el ancho de la tarjeta
    onAvatarClick: () -> Unit = {},
    onViewClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onLongClick: () -> Unit = {}

) {
    val borderColor = if (isSelected) MaverickBlue else Color.White.copy(alpha = 0.1f)
    val effectiveEmoji = categoryEmoji ?: "📋"
    
    // El color del borde superior cambia si no está visto (Verde) o si ya se vio (Maverick Gradient)
    val statusColors = if (!budget.isRead) {
        listOf(Color(0xFF10B981), Color(0xFF10B981)) // Verde esmeralda (No visto)
    } else {
        listOf(MaverickBlue, MaverickPurple) // Colores estándar (Visto)
    }

    Card(
        modifier = modifier
            .width(cardWidth) // Usando la variable de ancho
            .padding(2.dp)
            .pointerInput(isMultiSelectionActive) {
                detectTapGestures(
                    onTap = {
                        if (isMultiSelectionActive) onLongClick()
                        else onViewClick() // Abrir presupuesto al tocar la tarjeta
                    },
                    onLongPress = { onLongClick() }
                )
            },
        shape = BudgetCardShape, // Aplicando el corte de 5dp arriba y casi recto abajo
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Decoración superior (Indicador de estado Visto/No Visto)
            // Ajustamos el Box para que siga la forma de la tarjeta (el corte)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(BudgetCardShape) // Para que el gradiente se corte igual que la tarjeta
                    .background(Brush.horizontalGradient(statusColors))
            )

            // --- INDICADOR DE SELECCIÓN ---
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp, top = 4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    SelectionIndicator(isSelected = true)
                }
            }

            // --- INDICADOR DE ESTADO (MANAGER) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp),
                contentAlignment = Alignment.TopStart
            ) {
                BudgetStatusBadge(status = budget.status)
            }

            // Cuerpo: Info del Proveedor (Click -> Perfil)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAvatarClick() }
                    .padding(vertical = 2.dp), // Reducido vertical padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-2).dp) // Reducir espacio entre textos verticalmente
            ) {
                // Imagen con anillo y check
                Box(modifier = Modifier.padding(bottom = 2.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, MaverickBlue.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(budget.providerPhotoUrl)
                                .crossfade(true)
                                .size(100, 100) // 🔥 [OPTIMIZACIÓN ELITE] Imagen optimizada para el Grid
                                .build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Check de verificado
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(12.dp)
                            .background(StatusActive, CircleShape)
                            .border(1.dp, DarkCardBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verificado",
                            tint = Color.Black,
                            modifier = Modifier.size(6.dp)
                        )
                    }
                }

                Text(
                    text = budget.providerName,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Text(
                    text = (budget.providerCompanyName ?: "Independiente").uppercase(),
                    fontSize = 7.sp,
                    color = MaverickBlue,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Divisor (Espaciado reducido)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )

            // Encabezado: Categoría e ID (Alineación optimizada)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de Categoría
                Row(
                    modifier = Modifier
                        .background(MaverickBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, MaverickBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = effectiveEmoji, fontSize = 8.sp)
                    Text(
                        text = (budget.category ?: "Servicio").uppercase(),
                        color = MaverickBlue,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 50.dp)
                    )
                }

                // ID del Presupuesto
                Text(
                    text = "#${budget.budgetId.takeLast(6).uppercase()}",
                    color = Color.Gray,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Monto del Presupuesto (Diseño compacto)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-1).dp) // Reducir espacio vertical entre monto y precio
            ) {
                Text(
                    text = "MONTO TOTAL",
                    color = Color.Gray,
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "$", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f", budget.grandTotal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                if (budget.validityDays > 0) {
                    Text(
                        text = "Válido ${budget.validityDays} días",
                        color = MaverickBlue.copy(alpha = 0.8f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pie: Botón de Acción Principal (Enviar Mensaje)
            Box(modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 4.dp).fillMaxWidth()) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier.fillMaxWidth().height(26.dp), // Reducido un poco el alto
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaverickBlue,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "MENSAJE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// Esta función te permite ver la tarjeta en la ventana de "Design" de Android Studio
@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun BudgetCardPreview() {
    val sampleBudgetRead = BudgetEntity(
        budgetId = "PRE-84920",
        clientId = "user123",
        providerId = "prov456",
        providerName = "Carlos Rodríguez",
        providerCompanyName = "Servicios Integrales S.A.",
        category = "Electricista",
        grandTotal = 45500.0,
        validityDays = 15,
        isRead = true,
        dateTimestamp = System.currentTimeMillis()
    )

    val sampleBudgetUnread = sampleBudgetRead.copy(budgetId = "PRE-84921", isRead = false)

    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("VISTO (Gradiente Azul)", color = Color.White, fontSize = 10.sp)
            BudgetCard(
                budget = sampleBudgetRead,
                categoryEmoji = "⚡",
                onAvatarClick = {},
                onViewClick = {},
                onChatClick = {}
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text("NO VISTO (Verde)", color = Color.White, fontSize = 10.sp)
            BudgetCard(
                budget = sampleBudgetUnread,
                categoryEmoji = "⚡",
                onAvatarClick = {},
                onViewClick = {},
                onChatClick = {}
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

@Preview(showBackground = true, backgroundColor = 0xFFE2E8F0)
@Composable
fun LicitacionFolderPremiumPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Caso 1: Licitación Abierta
            LicitacionFolderPremium(
                title = "Reparación de Aire Acondicionado",
                category = "Climatización",
                categoryIcon = "❄️",
                supercategoryColor = Color(0xFF0EA5E9), 
                tenderId = "T-AB-1-ABCD",
                status = "ABIERTA",
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis() + 86400000 * 5,
                budgetCount = 5,
                unreadCount = 2,
                isSelected = false,
                onViewDetails = {},
                onClick = {}
            )

            // Caso 2: Licitación Adjudicada
            LicitacionFolderPremium(
                title = "Mantenimiento Preventivo IT",
                category = "Informática",
                categoryIcon = "💻",
                supercategoryColor = MaverickPurple, 
                tenderId = "T-AD-2-EFGH",
                status = "ADJUDICADA",
                startDate = System.currentTimeMillis() - 86400000 * 10,
                endDate = System.currentTimeMillis() - 86400000 * 2,
                budgetCount = 12,
                unreadCount = 0,
                isSelected = false,
                awardedProviderName = "Maverick Tech S.A.",
                awardedBudgetId = "BUD-9999",
                awardedProviderPhotoUrl = "https://picsum.photos/seed/provider/200/200",
                onViewDetails = {},
                onClick = {}
            )
        }
    }
}


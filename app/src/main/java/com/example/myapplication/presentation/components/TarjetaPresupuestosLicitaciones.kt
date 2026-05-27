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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.core.data.local.entity.BudgetEntity
import com.example.myapplication.core.data.local.entity.BudgetStatus
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.ui.graphics.vector.ImageVector

// --- PALETA DE COLORES PREMIUM ---
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)
private val MaverickPurple = Color(0xFF9B51E0)
private val StatusActive = Color(0xFF38BDF8)
private val StatusWarning = Color(0xFFF87171)
private val NeonCyber = Color(0xFF00FFC2)


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
    val isExpired = now > endDate && (endDate != 0L)
    val effectiveStatus = if (isExpired && (status == "ABIERTA" || status == "ACTIVO")) "CERRADA" else status
    
    val remainingDays = if (endDate > now) {
        TimeUnit.MILLISECONDS.toDays(endDate - now)
    } else 0
    
    val locale = LocalConfiguration.current.locales[0]
    val df = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    
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
                    @Suppress("DEPRECATION")
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
                        text = category.uppercase(locale),
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
                                text = "#${tenderId.takeLast(8).uppercase(locale)}",
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
                        StatusPillPremium(status = effectiveStatus, modifier = Modifier.wrapContentSize().fillMaxWidth())

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
                                HorizontalDivider(color = Color.White.copy(0.2f), thickness = 1.dp)
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
                                    .size(120, 120)
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
                            Text(awardedProviderName.uppercase(locale), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("PRESUPUESTO", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            Text("#${awardedBudgetId?.takeLast(6)?.uppercase(locale) ?: "----"}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
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
    val locale = LocalConfiguration.current.locales[0]
    val upperStatus = status.uppercase(locale)
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
        BudgetStatus.PENDIENTE -> Color(0xFFFACC15) to "📄"
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


/**
 * --- COMPONENTE: TARJETA PRESUPUESTO A4 (ESTILO GOOGLE DRIVE / PREVIEW) ---
 * Representa el presupuesto como una hoja A4 miniaturizada con un pie de página moderno.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaPresupuestoA4Document(
    modifier: Modifier = Modifier,
    budget: BudgetEntity,
    isSelected: Boolean = false,
    isMultiSelectionActive: Boolean = false,
    isInsideTender: Boolean = false,
    categoryEmoji: String? = null,
    onViewClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val borderColor = if (isSelected) MaverickBlue else Color.White.copy(alpha = 0.1f)
    
    // --- ESTADOS DE INTERACCIÓN ---
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val locale = LocalConfiguration.current.locales[0]

    Surface(
        modifier = modifier
            .drawBehind {
                // --- SOMBRA 3D PERSONALIZADA (Estilo Licitación) ---
                val shadowColor = Color.Black.copy(alpha = if (isPressed) 1f else 0.8f)
                val shadowRadius = if (isPressed) 12.dp.toPx() else 8.dp.toPx()
                val offsetY = if (isPressed) 6.dp.toPx() else 4.dp.toPx()

                drawIntoCanvas { canvas ->
                    @Suppress("DEPRECATION")
                    val paint = Paint().asFrameworkPaint().apply {
                        color = shadowColor.toArgb()
                        setShadowLayer(shadowRadius, 0f, offsetY, shadowColor.toArgb())
                    }
                    canvas.nativeCanvas.drawRoundRect(
                        0f,
                        offsetY,
                        size.width,
                        size.height,
                        8.dp.toPx(),
                        8.dp.toPx(),
                        paint
                    )
                }
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (isMultiSelectionActive) onLongClick()
                    else onViewClick()
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(4.dp),
        color = Color.White, // Fondo de la "Hoja A4"
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = 0.dp // Usamos nuestra sombra 3D personalizada
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // --- 1. FONDO: ESQUELETO DEL DOCUMENTO (SKELETON) ---
            BudgetSkeletonA4(modifier = Modifier.fillMaxSize())

            // --- 2. OVERLAY SUPERIOR: INDICADOR DE ESTADO (VISTO/NO VISTO) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        if (budget.isRead) Brush.horizontalGradient(listOf(MaverickBlue, MaverickPurple))
                        else Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF10B981)))
                    )
            )

            // --- 3. INDICADORES (SELECCIÓN / ESTADO / CATEGORÍA) ---
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    SelectionIndicator(isSelected = true)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // ETIQUETA DE CATEGORÍA (Solo si no es Licitación)
                if (!isInsideTender) {
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .background(MaverickBlue.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .border(1.dp, MaverickBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = categoryEmoji ?: "📋", fontSize = 10.sp)
                            Spacer(Modifier.width(6.dp))
                            // DIVIDER VERTICAL MEJORADO
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(10.dp)
                                    .background(MaverickBlue.copy(alpha = 0.4f))
                            )
                            Spacer(Modifier.width(6.dp))

                            Text(
                                text = (budget.category ?: "Servicio").uppercase(locale),
                                color = MaverickBlue,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // BADGE DE ESTADO
                BudgetStatusBadge(status = budget.status)
            }

            // --- 4. PIE DE PÁGINA (GLASSMORPHISM MAVERICK) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(78.dp) // Un poco más de altura para el gradient
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(horizontal = 2.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // FILA: PRECIO RESALTADO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "$",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyber
                        )
                       // Spacer(Modifier.width(1.dp))
                        Text(
                            text = String.format(locale, "%,.0f", budget.grandTotal),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // FILA: PRESTADOR Y ACCIONES
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Perfil del Prestador
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAvatarClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(budget.providerPhotoUrl)
                                    .crossfade(true)
                                    .size(60, 60)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp) // Un poco más grande
                                    .clip(CircleShape)
                                    .border(0.5.dp, Color.White.copy(0.3f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(Modifier.width(6.dp))
                            
                            AutoSizeText(
                                text = budget.providerName.uppercase(locale),
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 2,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // DIVIDER VERTICAL
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(12.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                        )

                        // BOTÓN CHAT (Icono Email/Chat)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onChatClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Chat",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Representación visual simplificada del documento A4 (Skeleton Lines).
 */
@Composable
private fun BudgetSkeletonA4(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.padding(12.dp)) {
        val width = size.width
        
        // --- CABECERA ---
        // Líneas de texto de cabecera
        drawLine(Color(0xFFE2E8F0), Offset(0.dp.toPx(), 4.dp.toPx()), Offset(width * 0.7f, 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(Color(0xFFF1F5F9), Offset(0.dp.toPx(), 10.dp.toPx()), Offset(width * 0.5f, 10.dp.toPx()), strokeWidth = 1.5.dp.toPx())

        // Cuadrito de ID (Derecha)
        drawRect(Color(0xFFDBEAFE), Offset(width - 25.dp.toPx(), 0f), size = androidx.compose.ui.geometry.Size(25.dp.toPx(), 10.dp.toPx()))

        // --- CUERPO (TABLA) ---
        val tableStartY = 35.dp.toPx()
        val rowHeight = 12.dp.toPx()
        
        // Cabecera de tabla
        drawRect(Color(0xFFF8FAFC), Offset(0f, tableStartY), size = androidx.compose.ui.geometry.Size(width, rowHeight))
        
        // Líneas de filas
        for (i in 1..5) {
            val y = tableStartY + (i * rowHeight)
            drawLine(Color(0xFFF1F5F9), Offset(0f, y), Offset(width, y), strokeWidth = 0.5.dp.toPx())
            // Contenido de fila (Cant, Desc, Total)
            drawLine(Color(0xFFF1F5F9), Offset(4.dp.toPx(), y + 6.dp.toPx()), Offset(12.dp.toPx(), y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFFF1F5F9), Offset(18.dp.toPx(), y + 6.dp.toPx()), Offset(width * 0.6f, y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFFF1F5F9), Offset(width - 20.dp.toPx(), y + 6.dp.toPx()), Offset(width - 4.dp.toPx(), y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())
        }
        
        // --- TOTALES ---
        val totalY = tableStartY + (7 * rowHeight)
        drawLine(Color(0xFFE2E8F0), Offset(width * 0.6f, totalY), Offset(width, totalY), strokeWidth = 1.dp.toPx())
        drawLine(Color(0xFFE2E8F0), Offset(width * 0.6f, totalY + 6.dp.toPx()), Offset(width, totalY + 6.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE2E8F0)
@Composable
fun TarjetaPresupuestoA4DocumentPreview() {
    val sampleBudget = BudgetEntity(
        budgetId = "PRE-55210",
        clientId = "user123",
        providerId = "prov456",
        providerName = "Ing. Marcos Tech",
        providerCompanyName = "Tech Solutions",
        category = "Domótica",
        grandTotal = 125800.0,
        validityDays = 30,
        isRead = false,
        dateTimestamp = System.currentTimeMillis()
    )

    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("CONTEXTO CHAT (Con Categoría)", color = Color.White, fontSize = 10.sp)
            Row(modifier = Modifier.height(180.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TarjetaPresupuestoA4Document(
                    modifier = Modifier.width(118.dp).fillMaxHeight(),
                    budget = sampleBudget,
                    isSelected = false,
                    isInsideTender = false,
                    categoryEmoji = "🏠"
                )
                
                TarjetaPresupuestoA4Document(
                    modifier = Modifier.width(118.dp).fillMaxHeight(),
                    budget = sampleBudget.copy(isRead = true, budgetId = "PRE-55211"),
                    isSelected = true,
                    isInsideTender = false,
                    categoryEmoji = "🏠"
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text("CONTEXTO LICITACIÓN (Sin Categoría)", color = Color.White, fontSize = 10.sp)
            Row(modifier = Modifier.height(180.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TarjetaPresupuestoA4Document(
                    modifier = Modifier.width(118.dp).fillMaxHeight(),
                    budget = sampleBudget.copy(budgetId = "PRE-55212"),
                    isSelected = false,
                    isInsideTender = true
                )
            }
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

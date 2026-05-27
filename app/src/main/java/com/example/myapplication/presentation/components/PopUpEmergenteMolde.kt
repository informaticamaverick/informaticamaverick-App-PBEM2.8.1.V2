package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.presentation.designsystem.components.DepthDividerHorizontal
import com.example.myapplication.presentation.designsystem.components.MaverickColors
import com.example.myapplication.presentation.designsystem.components.CyberTypography
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

// --- COLORES EXTRAÍDOS DE LICITACIÓN DETALLE ---
private val CardSurface = Color(0xFF161C24)
private val MaverickBlue = Color(0xFF2197F5)

/**
 * --- COMPONENTES ATÓMICOS PARA CONTENIDO DE POPUPS ---
 */

@Composable
fun PopUpSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    accentColor: Color = MaverickBlue
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (emoji != null) {
                Text(text = emoji, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
            }
            Text(
                text = text.uppercase(),
                color = accentColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
fun PopUpDetailSection(emoji: String, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label.uppercase(),
                color = Color.Gray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun PopUpRequirementChip(text: String) {
    Surface(
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PopUpDateItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun PopUpImageGallery(imageUrls: List<String>) {
    if (imageUrls.isNotEmpty()) {
        PopUpSectionHeader(text = "Registro Visual", emoji = "📸")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(imageUrls) { url ->
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
    }
}

@Composable
fun PopUpProviderCard(
    name: String,
    photoUrl: String?,
    companyName: String? = null,
    onChatClick: () -> Unit,
    accentColor: Color = MaverickBlue
) {
    Surface(
        onClick = onChatClick,
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, accentColor, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (companyName ?: "Prestador").uppercase(),
                    color = accentColor,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
            IconButton(
                onClick = onChatClick,
                modifier = Modifier.background(accentColor, CircleShape).size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Contenido visual del Popup para permitir renderizado en Previews y desacoplamiento de Dialog.
 */
@Composable
fun PopUpEmergenteMoldeContent(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String? = null,
    accentColor: Color = MaverickBlue,
    showCloseButton: Boolean = true,
    isScrollable: Boolean = true,
    onClose: () -> Unit = {},
    headerExtra: @Composable (RowScope.() -> Unit)? = null, // Slot para pills o estados debajo del título
    actions: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .drawBehind {
                // SOMBRA NEGRA FUERTE EXTERIOR
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(0f, 4f),
                    size = size,
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    alpha = 0.8f
                )
            }
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurface)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(MaverickBlue.copy(alpha = 0.4f), Color.Transparent)
                ),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // --- 1. CABECERA DINÁMICA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Ajustado para permitir contenido extra
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val path = Path().apply {
                            moveTo(0f, size.height)
                            lineTo(0f, 8.dp.toPx())
                            cubicTo(0f, 8.dp.toPx(), 0f, 0f, 8.dp.toPx(), 0f)
                            lineTo(size.width - 8.dp.toPx(), 0f)
                            cubicTo(size.width - 8.dp.toPx(), 0f, size.width, 0f, size.width, 8.dp.toPx())
                            lineTo(size.width, size.height)
                        }
                        val borderGradient = Brush.horizontalGradient(
                            0.0f to accentColor.copy(alpha = 0.05f),
                            0.5f to accentColor.copy(alpha = 0.4f),
                            1.0f to accentColor.copy(alpha = 0.05f)
                        )
                        drawPath(path = path, brush = borderGradient, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (emoji != null) {
                            Text(text = emoji, fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            if (subtitle != null) {
                                Text(
                                    text = subtitle.uppercase(),
                                    style = CyberTypography.MonospaceData.copy(
                                        color = accentColor.copy(alpha = 0.7f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                            AutoSizeText(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                ),
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (actions != null) actions()
                            if (showCloseButton) SheetCloseButton(onClick = onClose)
                        }
                    }

                    // Slot extra para estados o etiquetas debajo del título principal
                    if (headerExtra != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            content = headerExtra
                        )
                    }
                }

                DepthDividerHorizontal(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    thickness = 0.8.dp,
                    shadowColor = Color.Black.copy(alpha = 0.5f),
                    highlightColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // --- 2. CUERPO DEL POPUP ---
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                        .padding(20.dp),
                    content = content
                )
                if (isScrollable) {
                    Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent))).zIndex(1f))
                }
            }
        }
    }
}

/**
 * ==========================================================================================
 * --- 🏗️ COMPONENTE: POPUP EMERGENTE MOLDE (UNIVERSAL MAVERICK) ---
 * ==========================================================================================
 * Molde premium para ventanas emergentes tácticas. 
 * Basado en la anatomía de ListaElementosMoldeV2 para la cabecera.
 * Estilos visuales sincronizados con LicitacionDetallePopUp.
 */
@Composable
fun PopUpEmergenteMolde(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String? = null,
    accentColor: Color = MaverickBlue,
    showCloseButton: Boolean = true,
    isScrollable: Boolean = true,
    actions: @Composable (RowScope.() -> Unit)? = null,
    headerExtra: @Composable (RowScope.() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismissRequest, properties = properties) {
            PopUpEmergenteMoldeContent(
                title = title,
                modifier = modifier,
                subtitle = subtitle,
                emoji = emoji,
                accentColor = accentColor,
                showCloseButton = showCloseButton,
                isScrollable = isScrollable,
                onClose = onDismissRequest,
                headerExtra = headerExtra,
                actions = actions,
                content = content
            )
        }
    }
}

// ==========================================================================================
// --- 🎨 SECCIÓN: PREVIEWS ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PopUpEmergenteMoldePreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            PopUpEmergenteMoldeContent(
                title = "Configuración Elite",
                subtitle = "Sistemas v2.4",
                emoji = "⚙️",
                actions = {
                    SheetActionButton(icon = "💾", label = "Guardar", onClick = {})
                }
            ) {
                PopUpSectionHeader(text = "Detalles Técnicos", emoji = "📋")
                PopUpDetailSection(emoji = "📝", label = "Memoria", value = "Descripción detallada del sistema Maverick Elite.")
                PopUpImageGallery(listOf("https://picsum.photos/seed/1/200/200", "https://picsum.photos/seed/2/200/200"))
                PopUpSectionHeader(text = "Prestador Adjudicado", emoji = "🏆")
                PopUpProviderCard(name = "Maximiliano Nanterne", photoUrl = null, onChatClick = {})
            }
        }
    }
}

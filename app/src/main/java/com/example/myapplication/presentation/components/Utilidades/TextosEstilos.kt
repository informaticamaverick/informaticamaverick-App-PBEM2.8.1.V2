package com.example.myapplication.presentation.components.Utilidades

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapplication.R
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import java.text.Normalizer
import java.util.Locale


/**
 * 2. ESTILO DE LETRA (TYPOGRAPHY)
 * Simulación de la fuente Orbitron/Rajdhani para Android
 */
object MaverickTypography {
    val HeaderTitle = TextStyle(
        fontFamily = FontFamily.SansSerif, // Cambiar por Orbitron si tienes el .ttf
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        letterSpacing = 2.sp,
        color = Color.White
    )

    val HeaderSubtitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 5.sp,
        color = MaverickColors.ElectricCyan
    )

    val BodyText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        color = Color.LightGray
    )
}
/**
 * ESTILO DE LETRA ROG/MAVERICK
 */
object MaverickStyle {
    val OrbitronLike = TextStyle(
        fontFamily = FontFamily.SansSerif, // En Android Studio usar .ttf de 'Orbitron' o 'Rajdhani'
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp,
        color = Color.White
    )

    val SubtitleStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 4.sp,
        color = MaverickColors.MagentaNeon
    )
}


// ==========================================================================================
// --- SECCIÓN 1: UTILIDADES Y EXTENSIONES ---
// ==========================================================================================

/**
 * Función para limpiar textos, arreglar mayúsculas/minúsculas y opcionalmente quitar acentos.
 */
fun String.formatearTexto(quitarAcentos: Boolean = false): String {
    if (this.isBlank()) return this

    // Convierte a minúsculas y capitaliza la primera letra
    val textoFormateado = this.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
    // Si se requiere, elimina acentos y diacríticos
    return if (quitarAcentos) {
        val normalizado = Normalizer.normalize(textoFormateado, Normalizer.Form.NFD)
        "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalizado, "")
    } else {
        textoFormateado
    }
}

// ==========================================================================================
// --- 🔠 SECCIÓN 2: ESTILOS DE TEXTO (Typography Styles) ---
// ==========================================================================================

object CyberTypography {
    // 1. ROG Strix Style: Audaz, gruesa, moderna (Ideal para títulos grandes)
    val TitleTech = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = 3.sp,
        color = Color.White
    )

    // 2. Pixel Data Style: Monoespaciada, técnica, bloques (Ideal para datos y subsistemas)
    val MonospaceData = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp,
        color = MaverickColors.ElectricCyan
    )

    // 3. System Body Style: Limpia, legible pero con toque futurista
    val BodyCyber = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        color = Color.LightGray
    )
}

object MaverickStyles {
    // Estilo para encabezados de sección técnica (Mayúsculas, espaciado ancho)
    val SectionHeader = TextStyle(
        color = MaverickColors.NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp
    )
    
    // Estilo para etiquetas inteligentes con gradiente
    val IntelligentTag = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )

    // Estilo para títulos de resultados de búsqueda
    val ResultTitle = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = MaverickColors.TextMain
    )
}

// ==========================================================================================
// --- 📱 SECCIÓN 3: COMPONENTES DE TEXTO INTELIGENTES ---
// ==========================================================================================

/**
 * Componente de texto inteligente que ajusta su tamaño para encajar en el espacio disponible.
 * SECCIÓN: Mejora de robustez para evitar cortes de palabras y soportar múltiples líneas.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    minScale: Float = 0.4f // Rango de escala más amplio para mayor flexibilidad
) {
    var multiplier by remember(text, maxLines) { mutableFloatStateOf(1f) }
    var readyToDraw by remember(text, multiplier) { mutableStateOf(false) }

    // Aplicamos el multiplicador al tamaño de fuente si es una unidad válida
    val scaledStyle = remember(style, multiplier) {
        if (style.fontSize.isSp) {
            style.copy(
                fontSize = style.fontSize * multiplier,
                lineHeight = if (style.lineHeight.isSp) style.lineHeight * multiplier else style.lineHeight
            )
        } else style
    }

    Text(
        text = text,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        color = color,
        textAlign = textAlign,
        style = scaledStyle,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        softWrap = true, // Asegura que las palabras intenten envolverse antes de cortarse
        onTextLayout = { textLayoutResult ->
            // Si hay desbordamiento (visual o por líneas), reducimos el tamaño
            if ((textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > maxLines) && multiplier > minScale) {
                multiplier *= 0.9f
            } else {
                readyToDraw = true
            }
        }
    )
}

// ==========================================================================================
// --- ⚙️ SECCIÓN 4: ENTRADAS Y CAMPOS DE DATOS ---
// ==========================================================================================

/**
 * CUSTOM TEXT FIELD - Estilo corporativo con soporte para contraseñas
 * (Movido desde CustomTextField.kt)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray
            )
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                        ),
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = if (passwordVisible) Color(0xFF3B82F6) else Color.Gray
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAFC).copy(alpha = 0.9f),
            unfocusedContainerColor = Color(0xFFF8FAFC).copy(alpha = 0.8f),
            focusedBorderColor = Color(0xFF3B82F6),
            unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedTextColor = Color(0xFF1E293B),
            unfocusedTextColor = Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

/**
 * CAJA DE TEXTO EDICION M3 - Estilo Bento/Cyber
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoTextFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    emoji: String? = null,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        placeholder = { Text(placeholder) },
        leadingIcon = if (emoji != null) {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        } else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Black.copy(alpha = 0.4f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
            focusedBorderColor = MaverickColors.GeminiAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = MaverickColors.GeminiAccent,
            unfocusedLabelColor = Color.Gray,
            cursorColor = MaverickColors.GeminiAccent
        )
    )
}

/**
 * Campo de visualización de datos estilo Bento con etiqueta flotante y borde inferior neón.
 */
@Composable
fun BentoDisplayFieldM3(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    accentColor: Color = MaverickColors.NeonCyan,
    containerColor: Color = Color.Black.copy(alpha = 0.2f),
    cornerRadius: Dp = 12.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(containerColor, RoundedCornerShape(cornerRadius))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (emoji != null) {
                        Text(text = emoji, fontSize = 18.sp, modifier = Modifier.padding(bottom = 2.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            accentColor.copy(alpha = 0.7f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                   }

                    Text(
                        text = value,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 1.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.8f),
                                    accentColor.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Text(
                text = label.uppercase(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (-10).dp, x = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = accentColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

// ==========================================================================================
// --- 📦 SECCIÓN 5: COMPONENTES DE LISTA Y NAVEGACIÓN ---
// ==========================================================================================

/**
 * Encabezado colapsable para secciones con contador y flecha rotativa.
 */
@Composable
fun CollapsibleSectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    // La rotación se maneja externamente o mediante el modificador rotateOnExpansion
    // Pero aquí lo integramos para independencia.

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title.uppercase(),
                style = MaverickStyles.SectionHeader
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .size(20.dp)
                .rotate(if (isExpanded) 90f else 0f)
        )
    }
}

// ==========================================================================================
// --- 🎨 PREVIEW: CATÁLOGO DE ESTILOS Y COMPONENTES ---
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
fun PreviewMaverickTypography() {
    MyApplicationTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("MAVERICK DESIGN SYSTEM", style = CyberTypography.TitleTech, color = MaverickColors.NeonCyan)

            // --- SECCIÓN TIPOGRAFÍA ---
            Text("1. TIPOGRAFÍA Y ESTILOS", style = MaverickStyles.SectionHeader)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StylePreviewItem("TitleTech", "MAVERICK OS v2.0", CyberTypography.TitleTech)
                StylePreviewItem("HeaderTitle", "PROJECT MAVERICK", MaverickTypography.HeaderTitle)
                StylePreviewItem("MonospaceData", "LATENCY: 0.04ms", CyberTypography.MonospaceData)
                StylePreviewItem("BodyCyber", "Este es un texto de cuerpo legible.", CyberTypography.BodyCyber)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // --- SECCIÓN COMPONENTES INTELIGENTES ---
            Text("2. COMPONENTES DE TEXTO", style = MaverickStyles.SectionHeader)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AUTOSIZE TEXT (Reducción automática):", color = Color.Gray, fontSize = 10.sp)
                AutoSizeText(
                    text = "Texto muy largo que se ajusta solo a una línea.",
                    style = MaverickStyles.ResultTitle,
                    color = Color.White
                )
                
                Text("COLLAPSIBLE HEADER:", color = Color.Gray, fontSize = 10.sp)
                CollapsibleSectionHeader(title = "Servicios Maverick", count = 12, isExpanded = true, onToggle = {})
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // --- SECCIÓN ENTRADAS ---
            Text("3. CAMPOS DE ENTRADA (INPUTS)", style = MaverickStyles.SectionHeader)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("CUSTOM TEXT FIELD (Light/Business):", color = Color.Gray, fontSize = 10.sp)
                CustomTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "ejemplo@maverick.com",
                    icon = Icons.Default.Email
                )
                
                CustomTextField(
                    value = "password123",
                    onValueChange = {},
                    placeholder = "Contraseña",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )

                Text("BENTO TEXT FIELD (Dark/Cyber):", color = Color.Gray, fontSize = 10.sp)
                BentoTextFieldM3(
                    value = "",
                    onValueChange = {},
                    label = "Nombre de Usuario",
                    emoji = "⚡"
                )

                Text("BENTO DISPLAY FIELD:", color = Color.Gray, fontSize = 10.sp)
                BentoDisplayFieldM3(label = "Status", value = "Online / Encrypted", emoji = "🌐")
            }
        }
    }
}

@Composable
private fun StylePreviewItem(name: String, content: String, style: TextStyle) {
    Column {
        Text(name.uppercase(), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(content, style = style)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

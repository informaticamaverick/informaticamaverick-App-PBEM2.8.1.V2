package com.example.myapplication.ui.componentes.sistema

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.estilos.PBEMTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.myapplication.R
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.estilos.AppBaseStyle
import com.example.myapplication.uishared.estilos.CyberTypography
import com.example.myapplication.uishared.estilos.AppStyles
import java.text.Normalizer
import java.util.Locale


/**
 * 2. ESTILO DE LETRA (TYPOGRAPHY) - Moved to Shared
 */

@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    minScale: Float = 0.4f 
) {
    var multiplier by remember(text, maxLines) { mutableFloatStateOf(1f) }
    var readyToDraw by remember(text, multiplier) { mutableStateOf(false) }

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
        overflow = TextOverflow.Clip, 
        softWrap = true,
        onTextLayout = { textLayoutResult ->
            if ((textLayoutResult.hasVisualOverflow || textLayoutResult.lineCount > maxLines) && multiplier > minScale) {
                multiplier = (multiplier * 0.9f).coerceAtLeast(minScale)
            } else {
                readyToDraw = true
            }
        }
    )
}

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

@Composable
fun StandardTextFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = if (isError) {
            { Icon(Icons.Filled.Error, contentDescription = "Error") }
        } else trailingIcon,
        supportingText = if (isError && errorMessage != null) {
            { Text(text = errorMessage) }
        } else if (helperText != null) {
            { Text(text = helperText) }
        } else null,
        isError = isError,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SharedPalette.ElectricCyan,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
        )
    )
}

@Composable
fun StandardFilledTextFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = if (isError) {
            { Icon(Icons.Filled.Error, contentDescription = "Error") }
        } else trailingIcon,
        supportingText = if (isError && errorMessage != null) {
            { Text(text = errorMessage) }
        } else if (helperText != null) {
            { Text(text = helperText) }
        } else null,
        isError = isError,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    )
}

@Composable
fun StandardPasswordFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    StandardTextFieldM3(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Ocultar" else "Mostrar"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = isError,
        errorMessage = errorMessage
    )
}

@Composable
fun StandardEmailFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Email",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    StandardTextFieldM3(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = "ejemplo@correo.com",
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        isError = isError,
        errorMessage = errorMessage
    )
}

@Composable
fun StandardDisplayFieldM3(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = if (icon != null) { { Icon(icon, null) } } else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalTextFieldM3(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    accentColor: Color = SharedPalette.ElectricCyan,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { 
                Text(
                    text = label, 
                    style = CyberTypography.MonospaceData.copy(fontSize = 12.sp)
                ) 
            },
            placeholder = { 
                Text(
                    text = placeholder, 
                    style = CyberTypography.MonospaceData.copy(fontSize = 14.sp, color = Color.Gray.copy(alpha = 0.5f))
                ) 
            },
            leadingIcon = leadingIcon,
            trailingIcon = if (isError) {
                { Icon(Icons.Filled.Error, contentDescription = "Error", tint = SharedPalette.ErrorRed) }
            } else trailingIcon,
            supportingText = if (isError && errorMessage != null) {
                { Text(text = errorMessage, color = SharedPalette.ErrorRed, style = CyberTypography.MonospaceData.copy(fontSize = 10.sp)) }
            } else if (helperText != null) {
                { Text(text = helperText, style = CyberTypography.MonospaceData.copy(fontSize = 10.sp, color = Color.Gray)) }
            } else null,
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            shape = RoundedCornerShape(12.dp),
            textStyle = CyberTypography.MonospaceData.copy(fontSize = 14.sp, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                errorBorderColor = SharedPalette.ErrorRed,
                focusedLabelColor = accentColor,
                unfocusedLabelColor = Color.Gray,
                errorLabelColor = SharedPalette.ErrorRed,
                cursorColor = accentColor,
                focusedContainerColor = accentColor.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    emoji: String? = null,
    accentColor: Color = SharedPalette.ElectricCyan,
    readOnly: Boolean = false,
    singleLine: Boolean = true
) {
    TacticalTextFieldM3(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        placeholder = placeholder,
        accentColor = accentColor,
        readOnly = readOnly,
        singleLine = singleLine,
        leadingIcon = if (emoji != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                    Text(text = emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(accentColor.copy(alpha = 0.3f)))
                }
            }
        } else null
    )
}


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
            focusedBorderColor = SharedPalette.GeminiAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = SharedPalette.GeminiAccent,
            unfocusedLabelColor = Color.Gray,
            cursorColor = SharedPalette.GeminiAccent
        )
    )
}

@Composable
fun BentoDisplayFieldM3(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    accentColor: Color = SharedPalette.NeonCyan,
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

@Composable
fun CollapsibleSectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
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
                style = AppStyles.SectionHeader
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

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
fun PreviewStandardM3Components() {
    ClienteTheme(darkTheme = false) { 
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("PIXEL STYLE (M3)", style = MaterialTheme.typography.headlineSmall, color = Color.Black, fontWeight = FontWeight.Bold)

            StandardTextFieldM3(
                value = "Odette",
                onValueChange = {},
                label = "First name",
                placeholder = "Enter your name"
            )

            StandardTextFieldM3(
                value = "D'Ambricourt",
                onValueChange = {},
                label = "Last name"
            )

            StandardFilledTextFieldM3(
                value = "",
                onValueChange = {},
                label = "City",
                placeholder = "Search your city...",
                leadingIcon = { Icon(Icons.Default.MyLocation, null) }
            )

            StandardTextFieldM3(
                value = "08",
                onValueChange = {},
                label = "Zip",
                isError = true,
                errorMessage = "Not a valid zip"
            )

            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))

            Text("AUTH & DISPLAY (PIXEL)", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

            StandardEmailFieldM3(
                value = "odette@pixel.com",
                onValueChange = {}
            )

            StandardPasswordFieldM3(
                value = "pixel123",
                onValueChange = {},
                label = "Password"
            )

            StandardDisplayFieldM3(
                label = "Account Status",
                value = "Active / Verified",
                icon = Icons.Default.Check
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D12)
@Composable
fun PreviewAppTypography() {
    ClienteTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("APP DESIGN SYSTEM", style = CyberTypography.TitleTech, color = SharedPalette.NeonCyan)

            Text("1. TIPOGRAFÍA Y ESTILOS", style = AppStyles.SectionHeader)
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StylePreviewItem("TitleTech", "APP OS v2.0", CyberTypography.TitleTech)
                StylePreviewItem("HeaderTitle", "PROJECT", AppTypography.HeaderTitle)
                StylePreviewItem("MonospaceData", "LATENCY: 0.04ms", CyberTypography.MonospaceData)
                StylePreviewItem("BodyCyber", "Este es un texto de cuerpo legible.", CyberTypography.BodyCyber)
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Text("2. COMPONENTES DE TEXTO", style = AppStyles.SectionHeader)
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("AUTOSIZE TEXT (Reducción automática):", color = Color.Gray, fontSize = 10.sp)
                AutoSizeText(
                    text = "Texto muy largo que se ajusta solo a una línea.",
                    style = AppStyles.ResultTitle,
                    color = Color.White
                )
                
                Text("COLLAPSIBLE HEADER:", color = Color.Gray, fontSize = 10.sp)
                CollapsibleSectionHeader(title = "Servicios", count = 12, isExpanded = true, onToggle = {})
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Text("3. CAMPOS DE ENTRADA (INPUTS)", style = AppStyles.SectionHeader)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StandardTextFieldM3(
                    value = "",
                    onValueChange = {},
                    label = "Nombre del Proyecto",
                    placeholder = "Ej: Pintura de Fachada",
                    helperText = "Usa un nombre que identifique el trabajo"
                )

                StandardFilledTextFieldM3(
                    value = "",
                    onValueChange = {},
                    label = "Ubicación",
                    placeholder = "Buscar dirección...",
                    leadingIcon = { Icon(Icons.Filled.MyLocation, null) }
                )

                CustomTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "ejemplo@correo.com",
                    icon = Icons.Default.Email
                )
                
                CustomTextField(
                    value = "password123",
                    onValueChange = {},
                    placeholder = "Contraseña",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )

                BentoTextFieldM3(
                    value = "",
                    onValueChange = {},
                    label = "Nombre de Usuario",
                    emoji = "⚡"
                )

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

// Removed alias

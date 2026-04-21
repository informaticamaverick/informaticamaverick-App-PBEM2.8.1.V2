package com.example.myapplication.prestador.ui.register.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.ui.theme.*
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.data.model.ServiceType
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.prestador.data.model.ServicioFirebase
@Composable
fun RegisterSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onExpandChange: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceColor,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Borde izquierdo de color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Cabecera clickeable
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange() }
                        .padding(bottom = if (expanded) 16.dp else 0.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Colapsar" else "Expandir",
                        tint = color
                    )
                }

                // Contenido animado
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column { content() }
                }
            }
        }
    }
}

// =========================
// COMPONENTES REUTILIZABLES
// =========================

@Composable
fun ProfilePhotoSection(
    imageUri: Uri?,
    onCameraClick: () -> Unit
) {
    val colors = getPrestadorColors()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = colors.textSecondary
                    )
                }
            }
            
            // Botón cámara flotante
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .clickable { onCameraClick() },
                shape = CircleShape,
                shadowElevation = 4.dp,
                color = colors.primaryOrange
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = getPrestadorColors()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = colors.primaryOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Surface(
                shape = CircleShape,
                color = if (isExpanded) colors.primaryOrange.copy(alpha = 0.1f) else Color.Transparent,
                modifier = Modifier.clickable { onToggle() }
            ) {
                Icon(
                    if (isExpanded) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = if (isExpanded) "Guardar" else "Editar",
                    tint = if (isExpanded) colors.primaryOrange else colors.textSecondary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingLabelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector?,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefixText: String? = null,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val colors = getPrestadorColors()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hasText = value.isNotEmpty()
    
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            leadingIcon = {
                if (leadingContent != null) {
                    leadingContent()
                } else if (leadingIcon != null) {
                    Icon(
                        leadingIcon,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            },
            trailingIcon = when {
                trailingContent != null -> trailingContent
                trailingIcon != null -> ({
                    IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                        Icon(trailingIcon, contentDescription = null, tint = colors.textSecondary)
                    }
                })
                else -> null
            },
            label = {
                Text(
                    label,
                    color = if (isFocused) colors.primaryOrange else colors.textSecondary,
                    fontSize = if (isFocused || hasText) 12.sp else 16.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primaryOrange,
                unfocusedBorderColor = if (enabled) colors.border else Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedLabelColor = colors.primaryOrange,
                unfocusedLabelColor = colors.textSecondary,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                disabledTextColor = colors.textPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            singleLine = true,
            prefix = prefixText?.let { { Text(it, color = colors.textSecondary)}},
        )
    }
}

@Composable
fun TooltipBubble(
    text: String,
    onDismiss: () -> Unit
) {
    val colors = getPrestadorColors()
    
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp),
        shadowElevation = 8.dp,
        modifier = Modifier.width(200.dp),
        color = colors.primaryOrange
    ) {
        Box(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceChipsList(
    services: List<String>,
    onRemove: (String) -> Unit
) {
    val colors = getPrestadorColors()
    
    if (services.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "No hay servicios seleccionados",
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = Int.MAX_VALUE
        ) {
            services.forEach { service ->
                ServiceChip(
                    text = service,
                    onRemove = { onRemove(service) }
                )
            }
        }
    }
}

@Composable
fun ServiceChip(
    text: String,
    onRemove: () -> Unit
) {
    val colors = getPrestadorColors()
    
    Surface(
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape(10.dp),
        color = ChipBackground,
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text,
                color = OrangeDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Surface(
                shape = CircleShape,
                color = Color(0x0D000000),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = colors.primaryOrange,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(
            color = colors.divider,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.textPrimary
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.primaryOrange,
                    uncheckedColor = colors.border
                )
            )
        }
    }
}

@Composable
fun BusinessDetailsForm(
    nombreEmpresa: String,
    onNombreEmpresaChange: (String) -> Unit,
    razonSocial: String,
    onRazonSocialChange: (String) -> Unit,
    cuit: String,
    onCuitChange: (String) -> Unit,
    sucursales: List<Sucursal>,
    onSucursalesChange: (List<Sucursal>) -> Unit
) {
    val colors = getPrestadorColors()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            FloatingLabelTextField(
                value = nombreEmpresa,
                onValueChange = onNombreEmpresaChange,
                label = "Nombre de Fantasía",
                leadingIcon = Icons.Default.Business
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            FloatingLabelTextField(
                value = razonSocial,
                onValueChange = onRazonSocialChange,
                label = "Razón Social",
                leadingIcon = Icons.Default.Description
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            FloatingLabelTextField(
                value = cuit,
                onValueChange = onCuitChange,
                label = "CUIT",
                leadingIcon = Icons.Default.Receipt,
                keyboardType = KeyboardType.Number
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sucursales
            sucursales.forEachIndexed { index, sucursal ->
                BranchBlock(
                    title = if (index == 0) "Sucursal Principal" else "Sucursal #${index + 1}",
                    direccion = sucursal.direccion,
                    onDireccionChange = { newDir ->
                        val updated = sucursales.toMutableList()
                        updated[index] = sucursal.copy(direccion = newDir)
                        onSucursalesChange(updated)
                    },
                    codigoPostal = sucursal.codigoPostal,
                    onCodigoPostalChange = { newCp ->
                        val updated = sucursales.toMutableList()
                        updated[index] = sucursal.copy(codigoPostal = newCp)
                        onSucursalesChange(updated)
                    },
                    showDelete = index > 0,
                    onDelete = {
                        val updated = sucursales.toMutableList()
                        updated.removeAt(index)
                        onSucursalesChange(updated)
                    }
                )
                
                if (index < sucursales.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón agregar sucursal
            OutlinedButton(
                onClick = {
                    onSucursalesChange(sucursales + Sucursal("", ""))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primaryOrange
                ),
                border = BorderStroke(1.dp, colors.primaryOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar otra sucursal",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BranchBlock(
    title: String,
    direccion: String,
    onDireccionChange: (String) -> Unit,
    codigoPostal: String,
    onCodigoPostalChange: (String) -> Unit,
    showDelete: Boolean,
    onDelete: () -> Unit
) {
    val colors = getPrestadorColors()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated)
            .drawBehind {
                // Dibujar línea izquierda naranja
                drawRect(
                    color = androidx.compose.ui.graphics.Color(0xFFF97316), // Keep orange for accent
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(12f, size.height)
                )
            }
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = colors.primaryOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            if (showDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar sucursal",
                        tint = colors.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FloatingLabelTextField(
            value = direccion,
            onValueChange = onDireccionChange,
            label = "Dirección",
            leadingIcon = Icons.Default.LocationOn
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingLabelTextField(
            value = codigoPostal,
            onValueChange = onCodigoPostalChange,
            label = "Código Postal",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Number
        )
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ServiceSelectionModal(
    availableServices: List<String>,
    selectedServices: Set<String>,
    onServiceToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = getPrestadorColors()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.surfaceColor,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.primaryOrange)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Servicios",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "${selectedServices.size}/5",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Lista de servicios
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(availableServices) { service ->
                        val isSelected = selectedServices.contains(service)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onServiceToggle(service) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colors.primaryOrange,
                                    uncheckedColor = colors.border
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                service,
                                fontSize = 16.sp,
                                color = colors.textPrimary
                            )
                        }
                        
                        Divider(color = colors.divider)
                    }
                }
                
                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = colors.primaryOrange)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aceptar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Componente para item de sugerencia
@Composable
fun SuggestionItem(
    text: String,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(colors.surfaceColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar",
                tint = colors.primaryOrange,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Divider(
            color = colors.divider,
            thickness = 1.dp
        )
    }
}

// Data class
data class Sucursal(
    val direccion: String,
    val codigoPostal: String
)




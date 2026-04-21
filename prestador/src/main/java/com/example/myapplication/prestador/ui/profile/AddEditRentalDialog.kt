package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import com.example.myapplication.prestador.ui.theme.PrestadorColors

/**
 * Diálogo para agregar o editar un espacio de alquiler
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRentalDialog(
    space: RentalSpaceEntity?,
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, pricePerHour: Double, blockDuration: Int) -> Unit
) {
    var name by remember { mutableStateOf(space?.name ?: "") }
    var description by remember { mutableStateOf(space?.description ?: "") }
    var priceText by remember { mutableStateOf(space?.pricePerHour?.toInt()?.toString() ?: "") }
    var selectedDuration by remember { mutableStateOf(space?.blockDuration ?: 60) }
    
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    
    val isEditing = space != null
    val title = if (isEditing) "Editar Espacio" else "Nuevo Espacio"
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = colors.textSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Campo: Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nombre del espacio") },
                    placeholder = { Text("Ej: Cancha 1, Salón Principal") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Stadium,
                            contentDescription = null,
                            tint = colors.primaryOrange
                        )
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("El nombre es obligatorio", color = Color(0xFFE53935)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Descripción (opcional)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Detalles adicionales del espacio") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Precio por hora
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            priceText = it
                            priceError = it.isEmpty() || it.toIntOrNull() == null || it.toInt() <= 0
                        }
                    },
                    label = { Text("Precio por hora") },
                    placeholder = { Text("Ej: 5000") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = colors.primaryOrange
                        )
                    },
                    isError = priceError,
                    supportingText = if (priceError) {
                        { Text("Ingresá un precio válido", color = Color(0xFFE53935)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryOrange,
                        focusedLabelColor = colors.primaryOrange,
                        cursorColor = colors.primaryOrange
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de duración de bloque
                Text(
                    text = "Duración de cada turno",
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(60, 90, 120).forEach { duration ->
                        DurationChip(
                            duration = duration,
                            selected = selectedDuration == duration,
                            onClick = { selectedDuration = duration },
                            colors = colors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textPrimary
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            // Validar antes de guardar
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            
                            val price = priceText.toDoubleOrNull()
                            if (price == null || price <= 0) {
                                priceError = true
                                return@Button
                            }
                            
                            onSave(
                                name.trim(),
                                description.trim().takeIf { it.isNotBlank() },
                                price,
                                selectedDuration
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        )
                    ) {
                        Text(if (isEditing) "Guardar" else "Crear")
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationChip(
    duration: Int,
    selected: Boolean,
    onClick: () -> Unit,
    colors: PrestadorColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .then(
                if (selected) {
                    Modifier.border(2.dp, colors.primaryOrange, RoundedCornerShape(8.dp))
                } else {
                    Modifier.border(1.dp, colors.textSecondary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                }
            ),
        color = if (selected) colors.primaryOrange.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$duration",
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) colors.primaryOrange else colors.textPrimary
            )
            Text(
                text = "min",
                fontSize = 11.sp,
                color = if (selected) colors.primaryOrange else colors.textSecondary
            )
        }
    }
}

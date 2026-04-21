package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

/**
 * Dialog para agregar o editar un empleado
 */
@Composable
fun AddEmpleadoDialog(
    empleado: EmpleadoEntity? = null,  // null = agregar, no null = editar
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, apellido: String, dni: String) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = empleado != null
    
    var nombre by remember { mutableStateOf(empleado?.nombre ?: "") }
    var apellido by remember { mutableStateOf(empleado?.apellido ?: "") }
    var dni by remember { mutableStateOf(empleado?.dni ?: "") }
    var nombreError by remember { mutableStateOf(false) }
    var apellidoError by remember { mutableStateOf(false) }
    var dniError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEditMode) "Editar Empleado" else "Agregar Empleado",
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Nombre
                FloatingLabelTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = false
                    },
                    label = "Nombre",
                    leadingIcon = Icons.Default.Person
                )
                
                if (nombreError) {
                    Text(
                        text = "El nombre es requerido",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: Apellido
                FloatingLabelTextField(
                    value = apellido,
                    onValueChange = {
                        apellido = it
                        apellidoError = false
                    },
                    label = "Apellido",
                    leadingIcon = Icons.Default.Person
                )
                
                if (apellidoError) {
                    Text(
                        text = "El apellido es requerido",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo: DNI
                FloatingLabelTextField(
                    value = dni,
                    onValueChange = {
                        // Solo permitir números y máximo 8 dígitos
                        if (it.all { char -> char.isDigit() } && it.length <= 8) {
                            dni = it
                            dniError = false
                        }
                    },
                    label = "DNI",
                    leadingIcon = Icons.Default.Badge,
                    keyboardType = KeyboardType.Number
                )
                
                if (dniError) {
                    Text(
                        text = "DNI debe tener 7 u 8 dígitos",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Cancelar
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.textSecondary
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    // Botón Guardar
                    Button(
                        onClick = {
                            // Validar campos
                            var hasError = false
                            
                            if (nombre.isBlank()) {
                                nombreError = true
                                hasError = true
                            }
                            if (apellido.isBlank()) {
                                apellidoError = true
                                hasError = true
                            }
                            if (dni.isBlank() || dni.length < 7) {
                                dniError = true
                                hasError = true
                            }
                            
                            if (!hasError) {
                                onConfirm(nombre, apellido, dni)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryOrange
                        )
                    ) {
                        Text(if (isEditMode) "Actualizar" else "Agregar")
                    }
                }
            }
        }
    }
}

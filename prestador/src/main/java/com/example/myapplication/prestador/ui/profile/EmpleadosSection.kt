package com.example.myapplication.prestador.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.prestador.viewmodel.empresa.EmpleadosViewModel
import com.example.myapplication.prestador.viewmodel.empresa.EmpleadoActionState
import com.example.myapplication.prestador.viewmodel.empresa.EmpleadosUiState
import com.example.myapplication.prestador.data.local.entity.EmpleadoEntity
import com.example.myapplication.prestador.ui.profile.sections.ArchiveroSection
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

/**
 * Sección de Equipo de Trabajo (Empleados)
 * Se muestra en ambos modos: Personal y Empresa
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmpleadosSection(
    trabajaConOtros: Boolean,
    onTrabajaConOtrosChange: (Boolean) -> Unit,
    expanded: Boolean,
    onExpandChange: () -> Unit,
    isProfessional: Boolean = false,
    viewModel: EmpleadosViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var showInlineForm by remember { mutableStateOf(false) }
    var empleadoToEdit by remember { mutableStateOf<EmpleadoEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var nombreError by remember { mutableStateOf(false) }
    var apellidoError by remember { mutableStateOf(false) }
    var dniError by remember { mutableStateOf(false) }

    fun resetForm() {
        nombre = ""; apellido = ""; dni = ""; nombreError = false; apellidoError = false; dniError = false;
        showInlineForm = false; empleadoToEdit = null
    }

    LaunchedEffect(empleadoToEdit) {
        empleadoToEdit?.let {
            nombre = it.nombre
            apellido = it.apellido
            dni = it.dni
        }
    }
    
    // Manejar estado de acción (success/error)
    LaunchedEffect(actionState) {
        when (actionState) {
            is EmpleadoActionState.Success -> {
                resetForm()
                kotlinx.coroutines.delay(2000)
                viewModel.resetActionState()
            }
            else -> {}
        }
    }
    
    ArchiveroSection(
        title = if (isProfessional) "Asistentes / Personal" else "Equipo de Trabajo",
        sectionId = "team",
        icon = Icons.Default.Group,
        color = Color(0xFF9C27B0),
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        // Switch: ¿Trabaja con otras personas?
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTrabajaConOtrosChange(!trabajaConOtros) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "¿Trabaja con otras personas?",
                color = colors.textPrimary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = trabajaConOtros,
                onCheckedChange = onTrabajaConOtrosChange,
                modifier = Modifier.scale(0.85f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryOrange,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                )
            )
        }
        
        // Contenido si trabaja con otros
        AnimatedVisibility(
            visible = trabajaConOtros,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mensaje de estado de acción
                when (actionState) {
                    is EmpleadoActionState.Success -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = (actionState as EmpleadoActionState.Success).message,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    is EmpleadoActionState.Error -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = (actionState as EmpleadoActionState.Error).message,
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    else -> {}
                }
                
                // Lista de empleados
                when (uiState) {
                    is EmpleadosUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = colors.primaryOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    is EmpleadosUiState.Success -> {
                        val empleados = (uiState as EmpleadosUiState.Success).empleados
                        
                        if (empleados.isEmpty() && !showInlineForm) {
                            // Sin empleados
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.PersonAddAlt,
                                        contentDescription = null,
                                        tint = colors.textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No hay empleados registrados",
                                        color = colors.textSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            // Lista de empleados
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                empleados.forEach { empleado ->
                                    EmpleadoCard(
                                        empleado = empleado,
                                        onEdit = {
                                            empleadoToEdit = empleado
                                            showInlineForm = true
                                        },
                                        onDelete = {
                                            showDeleteConfirmation = empleado.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    is EmpleadosUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = (uiState as EmpleadosUiState.Error).message,
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Formulario inline
                LaunchedEffect(showInlineForm) {
                    if (showInlineForm) {
                        delay(350) // esperar que la animación termine
                        bringIntoViewRequester.bringIntoView()
                    }
                }
                AnimatedVisibility(
                    visible = showInlineForm,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .bringIntoViewRequester(bringIntoViewRequester),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceElevated,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (empleadoToEdit != null) "Editar Empleada" else "Nuevo Empleado",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it; nombreError = false },
                                label = { Text("Nombre") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.textSecondary) },
                                isError = nombreError,
                                supportingText = if (nombreError) {{ Text("El nombre es requerido", color = MaterialTheme.colorScheme.error) }}
                                else null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    focusedLabelColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.border
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = apellido,
                                onValueChange = { apellido = it; apellidoError = false },
                                label = { Text("Apellido") },
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.textSecondary) },
                                isError = apellidoError,
                                supportingText = if (apellidoError) {{ Text("El apellido es requerido", color = MaterialTheme.colorScheme.error)}} else null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    focusedLabelColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.border
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = dni,
                                onValueChange = {
                                    if (it.all { c -> c.isDigit() } && it.length <= 8) {
                                        dni = it; dniError = false
                                    }
                                },
                                label = { Text("DNI") },
                                leadingIcon = { Icon(Icons.Default.Badge, null, tint = colors.textSecondary) },
                                isError = dniError,
                                supportingText = if (dniError) {{ Text("DNI debe tener 7 u 8 Digitos", color = MaterialTheme.colorScheme.error) }} else null,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.primaryOrange,
                                    focusedLabelColor = colors.primaryOrange,
                                    unfocusedBorderColor = colors.border
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { resetForm() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                                ) { Text("Cancelar") }

                                Button(
                                    onClick = {
                                        var hasError = false
                                        if (nombre.isBlank()) {
                                            nombreError = true; hasError = true
                                        }
                                        if (apellido.isBlank()) {
                                            apellidoError = true; hasError = true
                                        }
                                        if (dni.isBlank() || dni.length < 7) {
                                            dniError = true; hasError = true
                                        }
                                        if (!hasError) {
                                            if (empleadoToEdit != null) {
                                                viewModel.updateEmpleado(
                                                    empleadoToEdit!!.id,
                                                    nombre,
                                                    apellido,
                                                    dni
                                                )
                                            } else {
                                                viewModel.addEmpleado(nombre, apellido, dni)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                                    enabled = actionState !is EmpleadoActionState.Loading
                                ) { Text(if (empleadoToEdit != null) "Actualizar" else "Agregar") }

                                }
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                if (!showInlineForm) {
                    Button(
                        onClick = { empleadoToEdit = null; showInlineForm = true},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                        enabled = actionState !is EmpleadoActionState.Loading
                    ) {
                        Icon(Icons.Default.PersonAddAlt,
                            contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Empleado")
                    }
                }
            }
        }
    }
    
    
    
    // Dialog de confirmación de eliminación
    showDeleteConfirmation?.let { empleadoId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Eliminar empleado") },
            text = { Text("¿Estás seguro de que deseas eliminar este empleado?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEmpleado(empleadoId)
                        showDeleteConfirmation = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Card de empleado individual
 */
@Composable
private fun EmpleadoCard(
    empleado: EmpleadoEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = getPrestadorColors()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.backgroundColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.primaryOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = colors.primaryOrange,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empleado.nombreCompleto(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "DNI: ${empleado.dni}",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
            
            // Botones
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = colors.primaryOrange
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

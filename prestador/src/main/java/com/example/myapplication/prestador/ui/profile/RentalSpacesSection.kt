package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.RentalSpaceEntity
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.viewmodel.RentalSpacesViewModel

/**
 * Sección para gestionar espacios de alquiler (canchas, salones, etc.)
 */
@Composable
fun RentalSpacesSection(
    colors: PrestadorColors,
    providerId: String? = null,
    viewModel: RentalSpacesViewModel = hiltViewModel()
) {
    // Inicializar providerId en el viewModel
    LaunchedEffect(providerId) {
        providerId?.let { viewModel.setProviderId(it) }
    }
    
    val spaces by viewModel.rentalSpaces.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSpace by remember { mutableStateOf<RentalSpaceEntity?>(null) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header con botón de agregar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Espacios Disponibles",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryOrange
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Agregar", fontSize = 13.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Lista de espacios o mensaje vacío
        if (spaces.isEmpty()) {
            EmptySpacesMessage(colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                spaces.forEach { space ->
                    SpaceCard(
                        space = space,
                        colors = colors,
                        onEdit = { editingSpace = space },
                        onDelete = { viewModel.deleteSpace(space) },
                        onToggleActive = { viewModel.toggleSpaceActiveStatus(space.id, !space.isActive) }
                    )
                }
            }
        }
    }
    
    // Diálogos
    if (showAddDialog) {
        AddEditRentalDialog(
            space = null,
            colors = colors,
            onDismiss = { showAddDialog = false },
            onSave = { name, description, price, duration ->
                viewModel.addSpace(name, description, price, duration)
                showAddDialog = false
            }
        )
    }
    
    editingSpace?.let { space ->
        AddEditRentalDialog(
            space = space,
            colors = colors,
            onDismiss = { editingSpace = null },
            onSave = { name, description, price, duration ->
                viewModel.updateSpace(
                    id = space.id,
                    name = name,
                    description = description,
                    pricePerHour = price,
                    blockDuration = duration,
                    isActive = space.isActive
                )
                editingSpace = null
            }
        )
    }
}

@Composable
private fun EmptySpacesMessage(colors: PrestadorColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.backgroundColor.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Stadium,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No hay espacios configurados",
                color = colors.textSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agregá tus canchas, salones o espacios disponibles",
                color = colors.textSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SpaceCard(
    space: RentalSpaceEntity,
    colors: PrestadorColors,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = space.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (space.isActive) colors.textPrimary else colors.textSecondary
                        )
                        if (!space.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.textSecondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Inactivo",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    space.description?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = colors.textSecondary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Menú de acciones
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info de precio y duración
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$${space.pricePerHour.toInt()}/hora",
                            fontSize = 13.sp,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${space.blockDuration} min",
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
                
                // Switch para activar/desactivar
                Switch(
                    checked = space.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = colors.primaryOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = colors.textSecondary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
    
    // Confirmación de eliminar
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
            },
            title = {
                Text("¿Eliminar ${space.name}?", color = colors.textPrimary)
            },
            text = {
                Text(
                    "Esta acción no se puede deshacer. Los clientes no podrán reservar este espacio.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = colors.textPrimary)
                }
            }
        )
    }
}

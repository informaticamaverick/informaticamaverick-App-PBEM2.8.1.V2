package com.example.myapplication.prestador.ui.presupuesto

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.data.local.entity.PresupuestoEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.PresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosListScreen(
    onBacck: () -> Unit = {},
    viewModel: PresupuestoViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val editProfileViewModel: EditProfileViewModel = hiltViewModel()
    val profileState by editProfileViewModel.profileState.collectAsState()
    val isProfessional = (profileState as? ProfileState.Success)?.provider?.serviceType
        .equals("PROFESSIONAL", ignoreCase = true)
    val presupuestos by viewModel.presupuestos.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val isSelecctionMode by viewModel.isSelectionMode.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog)  {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = { Text(if (isProfessional) "Eliminar consultas" else "Eliminar presupuestos") },
            text = { Text(if (isProfessional) "¿Eliminar ${selectedIds.size} consulta(s) seleccionada(s)?" else "¿Eliminar ${selectedIds.size} presupuesto(s) selecconado(s)?") },
            confirmButton = {
                TextButton(onClick =  {
                    viewModel.deleteSelected()
                    showDeleteDialog = false
                }) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false }) { Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelecctionMode) "${selectedIds.size} seleccionados"
                        else if (isProfessional) "Consultas Guardadas" else "Presupuestos Guardados"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelecctionMode) viewModel.clearSelection() else onBacck()
                    }) {
                        Icon (
                            if (isSelecctionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isSelecctionMode) "Cancelar" else "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surfaceColor)
            )
        },
        bottomBar = {
            if (isSelecctionMode) {
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Eliminar ${selectedIds.size} seleccionado(s)", fontSize = 16.sp)
                }
            }
        }
    ) { padding ->
        if (presupuestos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.textSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        if (isProfessional) "No hay consultas guardadas" else "No hay presupuestos guardados",
                        color = colors.textSecondary,
                        fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(presupuestos) { presupuesto ->
                    PresupuestoCard(
                        presupuesto = presupuesto,
                        colors = colors,
                        isSelectionMode = isSelecctionMode,
                        isSelected = presupuesto.id in selectedIds,
                        onLongClick = {
                            viewModel.toggleSelection(presupuesto.id) },
                        onClick = {
                            if (isSelecctionMode)
                                viewModel.toggleSelection(presupuesto.id)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresupuestoCard(
    presupuesto: PresupuestoEntity,
    colors: com.example.myapplication.prestador.ui.theme.PrestadorColors,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = onLongClick, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
            colors.primaryOrange.copy(alpha = 0.15f) else colors.surfaceColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presupuesto.numeroPresupuesto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                when (presupuesto.estado){
                                    "Pendiente" -> colors.primaryOrange.copy(alpha = 0.2f)
                                    "Aprobado" -> Color(0xFF66BB6A).copy(alpha = 0.2f)
                                    else -> colors.textSecondary.copy(alpha = 0.2f)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = presupuesto.estado,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (presupuesto.estado) {
                                "Pendiente" -> colors.primaryOrange
                                "Aprobado" -> Color(0xFF66BB6A)
                                else -> colors.textSecondary
                            }
                        )
                    }
                }

                Text("Fecha: ${presupuesto.fecha}", fontSize = 14.sp, color = colors.textSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Total:", fontSize = 14.sp, color = colors.textSecondary)
                    Text(
                        text = "$ ${String.format("%,.2f", presupuesto.total)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryOrange
                    )
                }
                if (presupuesto.notas.isNotBlank()) {
                    Text(presupuesto.notas, fontSize = 12.sp, color = colors.textSecondary, maxLines = 2)
                }
            }
        }
    }
}
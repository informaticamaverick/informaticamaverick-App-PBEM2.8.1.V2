package com.example.myapplication.prestador.ui.pantallas.presupuesto

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.datos.local.entidades.PresupuestoEntity
import com.example.myapplication.core.datos.local.entidades.EstadoPresupuesto
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.ui.pantallas.presupuesto.componentes.PastillaEstado
import com.example.myapplication.prestador.viewmodel.presupuesto.PrestadorPresupuestoViewModel
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PresupuestosListScreen(
    onBack: () -> Unit = {},
    viewModel: PrestadorPresupuestoViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val identidadViewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
    val stateDeep by identidadViewModel.state.collectAsStateWithLifecycle()
    val maestro = stateDeep.ecosistema
    
    val esProfesional = remember(maestro) { maestro?.cuenta?.priorizarEmpresa == true || maestro?.empresas?.isNotEmpty() == true }
    
    val presupuestos by viewModel.presupuestos.collectAsState()
    val idsSeleccionados by viewModel.idsSeleccionados.collectAsState()
    val esModoSeleccion by viewModel.esModoSeleccion.collectAsState()

    PresupuestosListContent(
        presupuestos = presupuestos,
        idsSeleccionados = idsSeleccionados,
        esModoSeleccion = esModoSeleccion,
        esProfesional = esProfesional,
        onBack = onBack,
        onAlternarSeleccion = { viewModel.alternarSeleccion(it) },
        onEliminarSeleccionados = { viewModel.eliminarSeleccionados() },
        onLimpiarSeleccion = { viewModel.limpiarSeleccion() },
        colors = colors
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresupuestosListContent(
    presupuestos: List<PresupuestoEntity>,
    idsSeleccionados: Set<String>,
    esModoSeleccion: Boolean,
    esProfesional: Boolean,
    onBack: () -> Unit,
    onAlternarSeleccion: (String) -> Unit,
    onEliminarSeleccionados: () -> Unit,
    onLimpiarSeleccion: () -> Unit,
    colors: PrestadorColors
) {
    Scaffold(
        containerColor = colors.backgroundColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (esModoSeleccion) "${idsSeleccionados.size} seleccionados" 
                        else if (esProfesional) "Mis Consultas" else "Mis Presupuestos", 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = if (esModoSeleccion) onLimpiarSeleccion else onBack) {
                        Icon(if (esModoSeleccion) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (esModoSeleccion) {
                        IconButton(onClick = onEliminarSeleccionados) {
                            Icon(Icons.Default.Delete, null, tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.primaryOrange)
            )
        }
    ) { padding ->
        if (presupuestos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(64.dp), tint = colors.textSecondary.copy(alpha = 0.5f))
                    Text(if (esProfesional) "No hay consultas guardadas" else "No hay presupuestos guardados", color = colors.textSecondary, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(presupuestos, key = { it.idPresupuesto }) { budget ->
                    FilaListaPresupuesto(
                        presupuesto = budget,
                        colors = colors,
                        estaSeleccionado = idsSeleccionados.contains(budget.idPresupuesto),
                        onClick = { if (esModoSeleccion) onAlternarSeleccion(budget.idPresupuesto) },
                        onLongClick = { onAlternarSeleccion(budget.idPresupuesto) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FilaListaPresupuesto(
    presupuesto: PresupuestoEntity,
    colors: PrestadorColors,
    estaSeleccionado: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val fechaStr = remember(presupuesto.marcaTiempo) {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        sdf.format(java.util.Date(presupuesto.marcaTiempo))
    }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = if (estaSeleccionado) colors.primaryOrange.copy(alpha = 0.1f) else colors.surfaceColor),
        border = if (estaSeleccionado) BorderStroke(2.dp, colors.primaryOrange) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(presupuesto.numeroPresupuesto ?: "S/N", fontWeight = FontWeight.Bold, color = colors.primaryOrange)
                Text(fechaStr, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Text(presupuesto.tituloTrabajo ?: "Presupuesto sin título", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                PastillaEstado(presupuesto.estado)
                val totalFormateado = remember(presupuesto.totalGeneral) { String.format(java.util.Locale.getDefault(), "%,.2f", presupuesto.totalGeneral) }
                Text("$ $totalFormateado", fontWeight = FontWeight.Black, fontSize = 18.sp, color = colors.textPrimary)
            }
        }
    }
}


















































package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity
import com.example.myapplication.core.datos.local.entidades.RecursoEntity
import com.example.myapplication.core.datos.local.entidades.VisibilidadRecurso
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo

/**
 * --- EDITORES DE GESTIÓN (v2026.SUPREME) ---
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRecursoSheet(
    entidad: RecursoEntity?,
    onDismiss: () -> Unit,
    onConfirm: (RecursoEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var nombre by remember { mutableStateOf(entidad?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(entidad?.descripcion ?: "") }
    var precioBase by remember { mutableStateOf(entidad?.precioBase?.toString() ?: "0.0") }
    var tipoRecurso by remember { mutableStateOf(entidad?.tipoRecurso ?: "CONSULTORIO") }
    var capacidadMaxima by remember { mutableStateOf(entidad?.capacidadMaxima?.toString() ?: "1") }
    var estaHabilitado by remember { mutableStateOf(entidad?.estaHabilitado ?: true) }
    var requiereHorarioPropio by remember { mutableStateOf(entidad?.requiereHorarioPropio ?: false) }
    var visibilidad by remember { mutableStateOf(entidad?.visibilidad ?: VisibilidadRecurso.PRIVADO) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entidad == null) "Nuevo Recurso Físico" else "Editar Recurso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción / Detalles") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = precioBase,
                    onValueChange = { precioBase = it },
                    label = { Text("Precio Base ($)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = capacidadMaxima,
                    onValueChange = { capacidadMaxima = it },
                    label = { Text("Aforo / Capacidad") },
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = estaHabilitado, onCheckedChange = { estaHabilitado = it })
                    Text("Activo y visible", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = requiereHorarioPropio, onCheckedChange = { requiereHorarioPropio = it })
                    Text("Configurar horario independiente", fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    val final = (entidad ?: RecursoEntity()).copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        precioBase = precioBase.toDoubleOrNull() ?: 0.0,
                        capacidadMaxima = capacidadMaxima.toIntOrNull() ?: 1,
                        estaHabilitado = estaHabilitado,
                        requiereHorarioPropio = requiereHorarioPropio,
                        tipoRecurso = tipoRecurso,
                        visibilidad = visibilidad
                    )
                    onConfirm(final)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GUARDAR RECURSO", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorEquipoSheet(
    entidad: EquipoTrabajoEntity?,
    recursosDisponibles: List<InventarioActivoDominio> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (EquipoTrabajoEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var nombre by remember { mutableStateOf(entidad?.nombre ?: "") }
    var apellido by remember { mutableStateOf(entidad?.apellido ?: "") }
    var cargo by remember { mutableStateOf(entidad?.cargo ?: "") }
    var detalle by remember { mutableStateOf(entidad?.detalle ?: "") }
    var estaHabilitado by remember { mutableStateOf(entidad?.estaHabilitado ?: true) }
    var idRecursoVinculado by remember { mutableStateOf(entidad?.idRecursoVinculado ?: "") }

    var expandedRecursos by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entidad == null) "Nuevo Colaborador" else "Editar Personal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = cargo,
                onValueChange = { cargo = it },
                label = { Text("Cargo / Especialidad") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expandedRecursos,
                onExpandedChange = { expandedRecursos = !expandedRecursos }
            ) {
                val recursoSeleccionado = recursosDisponibles.find { it.id == idRecursoVinculado }
                OutlinedTextField(
                    value = recursoSeleccionado?.nombre ?: "Sin espacio asignado",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Espacio de Trabajo / Recurso") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRecursos) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                ExposedDropdownMenu(
                    expanded = expandedRecursos,
                    onDismissRequest = { expandedRecursos = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ninguno / Flotante") },
                        onClick = { idRecursoVinculado = ""; expandedRecursos = false }
                    )
                    recursosDisponibles.filter { it.tipo == TipoActivo.RECURSO }.forEach { rec ->
                        DropdownMenuItem(
                            text = { Text(rec.nombre) },
                            onClick = { idRecursoVinculado = rec.id; expandedRecursos = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = detalle,
                onValueChange = { detalle = it },
                label = { Text("Detalles Adicionales") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = estaHabilitado, onCheckedChange = { estaHabilitado = it })
                Spacer(Modifier.width(12.dp))
                Text("Habilitado para la agenda", fontSize = 14.sp)
            }

            Button(
                onClick = {
                    val final = (entidad ?: EquipoTrabajoEntity(id = java.util.UUID.randomUUID().toString())).copy(
                        nombre = nombre,
                        apellido = apellido,
                        cargo = cargo,
                        detalle = detalle,
                        estaHabilitado = estaHabilitado,
                        idRecursoVinculado = idRecursoVinculado.ifBlank { null }
                    )
                    onConfirm(final)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CONFIRMAR PERSONAL", fontWeight = FontWeight.Bold)
            }
        }
    }
}

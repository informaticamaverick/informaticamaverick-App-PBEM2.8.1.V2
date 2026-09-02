package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.EquipoTrabajoEntity
import com.example.myapplication.core.datos.local.entidades.RecursoEntity
import com.example.myapplication.core.datos.local.entidades.VisibilidadRecurso
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

/**
 * --- EDITORES DE GESTIÓN (v2026.SUPREME) ---
 */

@Composable
private fun SeccionEditorTurnos(
    titulo: String,
    colorTitulo: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = GestionTurnosTheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.SurfaceInput,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, colors.BorderGlass)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = colorTitulo
            )
            content()
        }
    }
}

@Composable
private fun EncabezadoEditorTurnos(titulo: String, onCerrar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = GestionTurnosTheme.TextPrimary
        )
        IconButton(onClick = onCerrar) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = GestionTurnosTheme.TextMuted)
        }
    }
}

@Composable
private fun campoEditorTurnosColors(accent: Color = GestionTurnosTheme.BrandOrange) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = accent,
    focusedBorderColor = accent,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = accent,
    unfocusedLabelColor = Color.Gray,
    focusedContainerColor = GestionTurnosTheme.CardBg,
    unfocusedContainerColor = GestionTurnosTheme.CardBg
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRecursoSheet(
    entidad: RecursoEntity?,
    onDismiss: () -> Unit,
    onConfirm: (RecursoEntity) -> Unit
) {
    val colors = GestionTurnosTheme
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
        containerColor = colors.CardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.TextMuted) },
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
            EncabezadoEditorTurnos(
                titulo = if (entidad == null) "Nuevo Recurso Físico" else "Editar Recurso",
                onCerrar = onDismiss
            )

            SeccionEditorTurnos(titulo = "DATOS BÁSICOS", colorTitulo = colors.BrandOrange) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = campoEditorTurnosColors()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Detalles", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),

                    shape = RoundedCornerShape(10.dp),
                    colors = campoEditorTurnosColors()
                )
            }

            SeccionEditorTurnos(titulo = "PRECIO Y CAPACIDAD", colorTitulo = colors.AccentEmerald) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = precioBase,
                        onValueChange = { nuevo ->
                            if (nuevo.matches(Regex("^\\d*\\.?\\d*$"))) precioBase = nuevo
                        },
                        label = { Text("Precio Base ($)", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        colors = campoEditorTurnosColors(accent = colors.AccentEmerald)
                    )
                    OutlinedTextField(
                        value = capacidadMaxima,
                        onValueChange = { nuevo ->
                            if (nuevo.all { it.isDigit() }) capacidadMaxima = nuevo
                        },
                        label = { Text("Aforo / Capacidad", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = campoEditorTurnosColors(accent = colors.AccentEmerald)
                    )
                }
            }

            SeccionEditorTurnos(titulo = "CONFIGURACIÓN", colorTitulo = colors.AccentViolet) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = estaHabilitado,
                        onCheckedChange = { estaHabilitado = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.BrandOrange)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Activo y visible", fontSize = 13.sp, color = colors.TextPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = requiereHorarioPropio,
                        onCheckedChange = { requiereHorarioPropio = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.BrandOrange)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Configurar horario independiente", fontSize = 13.sp, color = colors.TextPrimary)
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
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.BrandOrange)
            ) {
                Text("GUARDAR RECURSO", fontWeight = FontWeight.Black, color = Color.Black)
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
    val colors = GestionTurnosTheme
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
        containerColor = colors.CardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.TextMuted) },
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
            EncabezadoEditorTurnos(
                titulo = if (entidad == null) "Nuevo Colaborador" else "Editar Personal",
                onCerrar = onDismiss
            )

            SeccionEditorTurnos(titulo = "DATOS PERSONALES", colorTitulo = colors.AccentViolet) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = campoEditorTurnosColors(accent = colors.AccentViolet)
                    )
                    OutlinedTextField(
                        value = apellido,
                        onValueChange = { apellido = it },
                        label = { Text("Apellido", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = campoEditorTurnosColors(accent = colors.AccentViolet)
                    )
                }
                OutlinedTextField(
                    value = cargo,
                    onValueChange = { cargo = it },
                    label = { Text("Cargo / Especialidad", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = campoEditorTurnosColors(accent = colors.AccentViolet)
                )
            }

            SeccionEditorTurnos(titulo = "ASIGNACIÓN", colorTitulo = colors.BrandOrange) {
                ExposedDropdownMenuBox(
                    expanded = expandedRecursos,
                    onExpandedChange = { expandedRecursos = !expandedRecursos }
                ) {
                    val recursoSeleccionado = recursosDisponibles.find { it.id == idRecursoVinculado }
                    OutlinedTextField(
                        value = recursoSeleccionado?.nombre ?: "Sin espacio asignado",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Espacio de Trabajo / Recurso", color = Color.Gray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRecursos) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = campoEditorTurnosColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRecursos,
                        onDismissRequest = { expandedRecursos = false },
                        modifier = Modifier.background(colors.CardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguno / Flotante", color = Color.White) },
                            onClick = { idRecursoVinculado = ""; expandedRecursos = false }
                        )
                        recursosDisponibles.filter { it.tipo == TipoActivo.RECURSO }.forEach { rec ->
                            DropdownMenuItem(
                                text = { Text(rec.nombre, color = Color.White) },
                                onClick = { idRecursoVinculado = rec.id; expandedRecursos = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = detalle,
                    onValueChange = { detalle = it },
                    label = { Text("Detalles Adicionales", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    colors = campoEditorTurnosColors()
                )
            }

            SeccionEditorTurnos(titulo = "ESTADO", colorTitulo = colors.AccentEmerald) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = estaHabilitado,
                        onCheckedChange = { estaHabilitado = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.AccentEmerald)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Habilitado para la agenda", fontSize = 13.sp, color = colors.TextPrimary)
                }
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
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.AccentEmerald)
            ) {
                Text("CONFIRMAR PERSONAL", fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

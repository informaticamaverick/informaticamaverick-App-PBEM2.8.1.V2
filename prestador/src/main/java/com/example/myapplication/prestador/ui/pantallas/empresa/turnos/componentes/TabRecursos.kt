package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun TabRecursos(
    recursos: List<InventarioActivoDominio>,
    onNuevoRecurso: () -> Unit,
    onEditarRecurso: (String) -> Unit,
    onToggleActivo: (InventarioActivoDominio) -> Unit,
    onEliminar: (InventarioActivoDominio) -> Unit
) {
    val colors = getPrestadorColors()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.primaryOrange.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = colors.primaryOrange)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Recursos Físicos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textPrimary)
                        Text("Define canchas, consultorios o activos de reserva exclusiva.", fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("RECURSOS REGISTRADOS (${recursos.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                Button(onClick = onNuevoRecurso, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nuevo Recurso", fontSize = 12.sp)
                }
            }
        }

        items(recursos.filter { it.tipo == TipoActivo.RECURSO }, key = { it.id }) { recurso ->
            Card(
                onClick = { onEditarRecurso(recurso.id) },
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Surface(color = colors.primaryOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(recurso.categoria, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primaryOrange, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(recurso.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                        }
                        Switch(checked = recurso.habilitado, onCheckedChange = { onToggleActivo(recurso) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primaryOrange))
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Build, null, modifier = Modifier.size(14.dp), tint = colors.textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Text(recurso.equipamiento.ifEmpty { "Sin descripción adicional" }, fontSize = 12.sp, color = colors.textSecondary)
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { onEliminar(recurso) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Delete, "Eliminar", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

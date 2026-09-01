package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun TabEquipo(
    equipo: List<InventarioActivoDominio>,
    onAsignarPersonal: () -> Unit,
    onEditarPersonal: (String) -> Unit,
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PersonSearch, null, tint = Color(0xFFF57F17))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Equipo de Trabajo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textPrimary)
                        Text("Vincular colaboradores o especialistas a los puntos de venta.", fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("COLABORADORES (${equipo.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                Button(onClick = onAsignarPersonal, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Añadir Personal", fontSize = 12.sp)
                }
            }
        }

        if (equipo.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No hay personal asignado a esta sucursal.", fontSize = 12.sp, color = colors.textSecondary)
                }
            }
        } else {
            items(equipo.filter { it.tipo == TipoActivo.EQUIPO }, key = { it.id }) { persona ->
                Card(
                    onClick = { onEditarPersonal(persona.id) },
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primaryOrange.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Text(persona.nombre.take(1).uppercase(), fontWeight = FontWeight.Bold, color = colors.primaryOrange)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(persona.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textPrimary)
                                Text(persona.subTitulo, fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = persona.habilitado, onCheckedChange = { onToggleActivo(persona) }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981)))
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { onEliminar(persona) }) {
                                Icon(Icons.Outlined.PersonRemove, "Quitar", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

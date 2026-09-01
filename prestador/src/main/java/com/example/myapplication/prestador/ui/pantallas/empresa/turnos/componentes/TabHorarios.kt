package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun TabHorarios(
    inventario: List<InventarioActivoDominio>,
    onConfigurarHorario: (id: String, nombre: String) -> Unit
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
                    Icon(Icons.Outlined.Schedule, null, tint = colors.primaryOrange)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Horarios de Disponibilidad", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.textPrimary)
                        Text("Configura las franjas horarias de cada recurso o colaborador.", fontSize = 11.sp, color = colors.textSecondary)
                    }
                }
            }
        }

        item {
            Text("SELECCIONAR ACTIVO PARA CONFIGURAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
        }

        items(inventario, key = { it.id }) { item ->
            Card(
                onClick = { onConfigurarHorario(item.id, item.nombre) },
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.tipo == TipoActivo.EQUIPO) Icons.Outlined.Badge else Icons.Outlined.AccessTime,
                            null,
                            tint = colors.primaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.textPrimary)
                            Text(item.subTitulo, fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary.copy(alpha = 0.5f))
                }
            }
        }
    }
}

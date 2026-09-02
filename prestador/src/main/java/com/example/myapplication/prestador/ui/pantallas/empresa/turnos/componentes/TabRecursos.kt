package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

@Composable
fun TabRecursos(
    recursos: List<InventarioActivoDominio>,
    onNuevoRecurso: () -> Unit,
    onEditarRecurso: (String) -> Unit,
    onToggleActivo: (InventarioActivoDominio) -> Unit,
    onEliminar: (InventarioActivoDominio) -> Unit
) {
    val colors = GestionTurnosTheme

    Box(modifier = Modifier.fillMaxSize().background(colors.DarkBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.BrandOrange.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, colors.BrandOrange.copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, tint = colors.BrandOrange, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Recursos Físicos", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.TextPrimary)
                        Text("Define canchas, consultorios o activos de reserva exclusiva.", fontSize = 10.sp, color = colors.TextSecondary)
                    }
                }
            }

            item {
                Text(
                    "RECURSOS REGISTRADOS (${recursos.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = colors.TextMuted
                )
            }

            items(recursos.filter { it.tipo == TipoActivo.RECURSO }, key = { it.id }) { recurso ->
                Surface(
                    onClick = { onEditarRecurso(recurso.id) },
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.CardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.BorderGlass)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(colors.BrandOrange.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MeetingRoom, null, tint = colors.BrandOrange, modifier = Modifier.size(20.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(color = colors.BrandOrange.copy(alpha = 0.12f), shape = RoundedCornerShape(5.dp)) {
                                    Text(recurso.categoria, fontSize = 9.sp, fontWeight = FontWeight.Black, color = colors.BrandOrange, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(recurso.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.TextPrimary)
                                Spacer(Modifier.height(5.dp))
                                Text(recurso.equipamiento.ifEmpty { "Sin descripción adicional" }, fontSize = 11.sp, color = colors.TextSecondary)
                            }

                            Switch(
                                checked = recurso.habilitado,
                                onCheckedChange = { onToggleActivo(recurso) },
                                colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = colors.BrandOrange)
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = colors.BorderGlass)
                        Spacer(Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onEliminar(recurso) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Outlined.Delete, "Eliminar", tint = colors.AccentRose.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        Surface(
            onClick = onNuevoRecurso,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            color = colors.BrandOrange,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("NUEVO RECURSO", fontSize = 12.sp, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.Black)
            }
        }
    }
}

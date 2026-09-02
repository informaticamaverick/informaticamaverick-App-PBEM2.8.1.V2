package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

@Composable
fun TabEquipo(
    equipo: List<InventarioActivoDominio>,
    onAsignarPersonal: () -> Unit,
    onEditarPersonal: (String) -> Unit,
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
                        .background(colors.AccentViolet.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, colors.AccentViolet.copy(alpha = 0.18f)), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.PersonSearch, null, tint = colors.AccentViolet, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Equipo de Trabajo", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.TextPrimary)
                        Text("Vincular colaboradores o especialistas a los puntos de venta.", fontSize = 10.sp, color = colors.TextSecondary)
                    }
                }
            }

            item {
                Text(
                    "COLABORADORES (${equipo.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp,
                    color = colors.TextMuted
                )
            }

            val personas = equipo.filter { it.tipo == TipoActivo.EQUIPO }
            if (personas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No hay personal asignado a esta sucursal.", fontSize = 12.sp, color = colors.TextSecondary)
                    }
                }
            } else {
                items(personas, key = { it.id }) { persona ->
                    val colorAvatar = if (persona.id.hashCode() % 2 == 0) colors.BrandOrange else colors.AccentCyan
                    Surface(
                        onClick = { onEditarPersonal(persona.id) },
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.CardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.BorderGlass)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(colorAvatar.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                    Text(persona.nombre.take(1).uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = colorAvatar)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(persona.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = colors.TextPrimary)
                                    Text(persona.subTitulo, fontSize = 10.sp, color = colors.TextSecondary)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = persona.habilitado,
                                    onCheckedChange = { onToggleActivo(persona) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.AccentEmerald)
                                )
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = { onEliminar(persona) }) {
                                    Icon(Icons.Outlined.PersonRemove, "Quitar", tint = colors.AccentRose.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Surface(
            onClick = onAsignarPersonal,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            color = colors.AccentEmerald,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PersonAdd, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("AÑADIR PERSONAL", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }
    }
}

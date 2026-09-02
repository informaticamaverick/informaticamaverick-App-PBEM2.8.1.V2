package com.example.myapplication.prestador.ui.pantallas.empresa.turnos.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.HorarioDominio
import com.example.myapplication.core.dominio.modelos.InventarioActivoDominio
import com.example.myapplication.core.dominio.modelos.TipoActivo
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

private fun HorarioDominio?.tieneAlgunRango(): Boolean {
    if (this == null) return false
    return listOf(lunes, martes, miercoles, jueves, viernes, sabado, domingo).any { it.isNotEmpty() }
}

@Composable
fun TabHorarios(
    inventario: List<InventarioActivoDominio>,
    onConfigurarHorario: (id: String, nombre: String) -> Unit
) {
    val colors = GestionTurnosTheme

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.DarkBg),
        contentPadding = PaddingValues(16.dp),
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
                Icon(Icons.Outlined.Schedule, null, tint = colors.BrandOrange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Horarios de Disponibilidad", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.TextPrimary)
                    Text("Configura las franjas horarias de cada recurso o colaborador.", fontSize = 10.sp, color = colors.TextSecondary)
                }
            }
        }

        item {
            Text(
                "SELECCIONAR ACTIVO PARA CONFIGURAR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                color = colors.TextMuted
            )
        }

        items(inventario, key = { it.id }) { item ->
            val configurado = item.horario.tieneAlgunRango()
            val colorEstado = if (configurado) colors.AccentEmerald else colors.AccentAmber

            Surface(
                onClick = { onConfigurarHorario(item.id, item.nombre) },
                modifier = Modifier.fillMaxWidth(),
                color = colors.CardBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (configurado) colors.BorderGlass else colors.AccentAmber.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.BrandOrange.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (item.tipo == TipoActivo.EQUIPO) Icons.Outlined.Badge else Icons.Default.MeetingRoom,
                                null,
                                tint = colors.BrandOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.TextPrimary)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Box(modifier = Modifier.size(5.dp).background(colorEstado, RoundedCornerShape(3.dp)))
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    if (configurado) "Configurado" else "Sin configurar",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorEstado
                                )
                            }
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = colors.TextMuted)
                }
            }
        }
    }
}

package com.example.myapplication.prestador.ui.pantallas.empresa.componentes

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectorFecha(
    seleccionada: LocalDate,
    onFechaSelect: (LocalDate) -> Unit,
    onCalendarioClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = getPrestadorColors()
    val hoy = LocalDate.now()
    val fechas = remember { (-3..10).map { hoy.plusDays(it.toLong()) } }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceColor)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCalendarioClick, modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = colors.primaryOrange)
        }

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(fechas) { fecha ->
                val esSeleccionada = fecha.isEqual(seleccionada)
                val esHoy = fecha.isEqual(hoy)
                
                Surface(
                    onClick = { onFechaSelect(fecha) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (esSeleccionada) colors.primaryOrange else Color.Transparent,
                    border = if (esSeleccionada) null else androidx.compose.foundation.BorderStroke(1.dp, colors.border.copy(alpha = 0.3f)),
                    modifier = Modifier.width(54.dp).height(64.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase().take(1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (esSeleccionada) Color.White else colors.textSecondary
                        )
                        Text(
                            text = fecha.dayOfMonth.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (esSeleccionada) Color.White else colors.textPrimary
                        )
                        if (esHoy && !esSeleccionada) {
                            Box(modifier = Modifier.size(4.dp).background(colors.primaryOrange, CircleShape))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    seleccionado: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = getPrestadorColors()
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) color.copy(alpha = 0.1f) else colors.surfaceColor
        ),
        border = if (seleccionado) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (seleccionado) color else colors.textSecondary)
        }
    }
}

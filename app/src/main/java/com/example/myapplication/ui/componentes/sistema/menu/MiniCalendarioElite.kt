package com.example.myapplication.ui.componentes.sistema.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette
import java.text.SimpleDateFormat
import java.util.*

/**
 * MiniCalendarioElite: Un selector de fechas táctico para el menú de Agenda.
 */
@Composable
fun MiniCalendarioElite(
    modifier: Modifier = Modifier,
    eventDates: Set<Long>, // Timestamps de inicio de día con eventos
    selectedDate: Long = System.currentTimeMillis(),
    onDateSelected: (Long) -> Unit
) {
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val monthName = remember(calendar) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time).uppercase()
    }

    val daysInMonth = remember(calendar) {
        val temp = calendar.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
        val daysCount = temp.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        List(42) { index ->
            if (index < firstDayOfWeek || index >= firstDayOfWeek + daysCount) {
                null
            } else {
                val day = index - firstDayOfWeek + 1
                val dayCal = calendar.clone() as Calendar
                dayCal.set(Calendar.DAY_OF_MONTH, day)
                dayCal.set(Calendar.HOUR_OF_DAY, 0)
                dayCal.set(Calendar.MINUTE, 0)
                dayCal.set(Calendar.SECOND, 0)
                dayCal.set(Calendar.MILLISECOND, 0)
                dayCal.timeInMillis
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CABECERA MES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
            }) {
                Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
            }
            
            Text(
                text = monthName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            
            IconButton(onClick = { 
                calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            }) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White)
            }
        }

        // DÍAS DE LA SEMANA
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // GRILLA DE DÍAS
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            daysInMonth.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { dayTimestamp ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayTimestamp != null) {
                                val isSelected = isSameDay(dayTimestamp, selectedDate)
                                val isToday = dayTimestamp == today
                                val hasEvents = eventDates.any { isSameDay(it, dayTimestamp) }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> SharedPalette.ElectricCyan.copy(alpha = 0.2f)
                                                isToday -> Color.White.copy(alpha = 0.05f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = when {
                                                isSelected -> SharedPalette.ElectricCyan
                                                isToday -> Color.White.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable { onDateSelected(dayTimestamp) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = Calendar.getInstance().apply { timeInMillis = dayTimestamp }.get(Calendar.DAY_OF_MONTH).toString(),
                                            color = if (isSelected) SharedPalette.ElectricCyan else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium
                                        )
                                        if (hasEvents && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(3.dp)
                                                    .clip(CircleShape)
                                                    .background(SharedPalette.ElectricCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

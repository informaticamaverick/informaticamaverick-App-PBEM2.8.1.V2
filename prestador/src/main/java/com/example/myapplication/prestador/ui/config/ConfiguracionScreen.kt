
package com.example.myapplication.prestador.ui.config

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun ConfiguracionScreen(
    onBack: () -> Unit = {},
    onNavigateToCalendario: () -> Unit = {}
) {
    val colors = getPrestadorColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceColor)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = colors.textPrimary
                )
            }
            Text(
                "Configuración",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }

        HorizontalDivider(color = colors.divider)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConfigSectionLabel("Presupuesto", colors)

            ConfigMenuItem(
                icon = Icons.Default.Description,
                title = "Configuración de presupuesto",
                subtitle = "Validez por defecto, nota legal y más",
                colors = colors,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigSectionLabel("Calendario", colors)

            ConfigMenuItem(
                icon = Icons.Default.CalendarMonth,
                title = "Horarios de atención",
                subtitle = "Días, horarios y duración de turnos",
                colors = colors,
                onClick = onNavigateToCalendario
            )
        }
    }
}


@Composable
private fun ConfigSectionLabel(label: String, colors: PrestadorColors) {
    Text(
        label.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = colors.textSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun ConfigMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: PrestadorColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.primaryOrange.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = colors.textSecondary, lineHeight = 16.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        }
    }
}






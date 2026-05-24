
package com.example.myapplication.prestador.ui.config

import android.R
import android.view.Surface
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
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState
import androidx.compose.ui.graphics.Color

@Composable
fun ConfiguracionScreen(
    onBack: () -> Unit = {},
    onNavigateToCalendario: () -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider
    val priorizarEmpresa = provider?.priorizarEmpresa ?: false
    val tieneEmpresa = (provider?.companies?.size ?: 0) > 0

    LaunchedEffect(Unit) {
        if (profileState !is ProfileState.Success) {
            viewModel.loadProfile()
        }
    }

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
                onClick = onNavigateToPresupuestoConfig
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

            Spacer(modifier = Modifier.height(8.dp))

            ConfigSectionLabel("Apariencia", colors)

            ConfigMenuItem(
                icon = Icons.Default.Palette,
                title = "Tema",
                subtitle = "Claro, oscuro o seguir el sistema",
                colors = colors,
                onClick = onNavigateToApariencia
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigSectionLabel("Notificaciones", colors)

            ConfigMenuItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                subtitle = "Mensajes, presupuestos y pedidos",
                colors = colors,
                onClick = onNavigateToNotificaciones
            )
            Spacer(modifier = Modifier.height(8.dp))

            ConfigSectionLabel("Perfil", colors)

            ConfigToggleItem(
                icon = Icons.Default.Business,
                title = "Usar perfil de empresa como principal",
                subtitle = if (priorizarEmpresa)"Tu perfil personal está desactivado"
                else
                "Tu perfil está activo",
                checked = priorizarEmpresa,
                enabled = tieneEmpresa,
                colors = colors,
                onToggle = { viewModel.toggleModoEmpresa(!priorizarEmpresa)}
            )

            if (!tieneEmpresa) {
                Text(
                    "Primero agregá una empresa desde tu perfil",
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
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


@Composable
private fun ConfigToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    colors: PrestadorColors,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        tonalElevation = 1.dp,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = androidx.compose.ui.Modifier
                    .size(40.dp)
                    .background(
                        if (checked) colors.primaryOrange.copy(alpha = 0.15f)
                        else colors.primaryOrange.copy(alpha = 0.1f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) colors.primaryOrange else colors.textSecondary,
                    modifier = androidx.compose.ui.Modifier.size(20.dp)
                )
            }
            Spacer(modifier = androidx.compose.ui.Modifier.width(14.dp))
            Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = if (enabled) colors.textSecondary else colors.textSecondary
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = if (checked) Color(0xFFF57C00) else colors.textSecondary,
                    lineHeight = 16.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) onToggle() },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.primaryOrange,
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.border
                )
            )
        }
    }
}





package com.example.myapplication.prestador.ui.pantallas.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.viewmodel.config.AppSettingsViewModel

/**
 * --- PANTALLA DE CONFIGURACIÓN DE NOTIFICACIONES (V2026.12) ---
 * [UNIFICADO]: Conectada al AppSettingsViewModel unificado.
 */
@Composable
fun NotificacionesConfigScreen(
    onBack: () -> Unit = {},
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val colors = PrestadorColors(
        primaryOrange = Color(0xFFFF5722),
        primaryOrangeDark = Color(0xFFF4511E),
        primaryOrangeLight = Color(0xFFFB923C),
        backgroundColor = Color(0xFF030712),
        surfaceColor = Color(0xFF0F172A),
        surfaceElevated = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        border = Color(0xFF334155).copy(alpha = 0.7f),
        divider = Color(0xFF1E293B),
        chipBackground = Color(0xFF1E293B),
        chipText = Color(0xFFF8FAFC),
        error = Color(0xFFEF4444),
        success = Color(0xFF10B981)
    )
    val notifMessages by viewModel.notifMessages.collectAsState()
    val notifPresupuestos by viewModel.notifPresupuestos.collectAsState()
    val notifPedidos by viewModel.notifPedidos.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(colors.backgroundColor)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary)
            }
            Text("Notificaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
        HorizontalDivider(color = colors.divider)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ACTIVAR / DESACTIVAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 4.dp))

            NotifToggleItem(
                icon = Icons.Default.Chat,
                title = "Mensajes",
                subtitle = "Notificaciones de nuevos mensajes en el chat",
                checked = notifMessages,
                onCheckedChange = { viewModel.setNotifMessages(it) },
                colors = colors
            )
            NotifToggleItem(
                icon = Icons.Default.Description,
                title = "Presupuestos",
                subtitle = "Cuando un cliente responde un presupuesto",
                checked = notifPresupuestos,
                onCheckedChange = { viewModel.setNotifPresupuestos(it) },
                colors = colors
            )
            NotifToggleItem(
                icon = Icons.Default.Work,
                title = "Nuevos pedidos",
                subtitle = "Cuando llega una nueva solicitud de servicio",
                checked = notifPedidos,
                onCheckedChange = { viewModel.setNotifPedidos(it) },
                colors = colors
            )
        }
    }
}

@Composable
private fun NotifToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: PrestadorColors
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(colors.primaryOrange.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = colors.textSecondary, lineHeight = 16.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = colors.primaryOrange, checkedTrackColor = colors.primaryOrange.copy(alpha = 0.4f))
            )
        }
    }
}
















































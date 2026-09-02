package com.example.myapplication.prestador.ui.pantallas.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.myapplication.core.datos.repositorios.ThemeMode
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.viewmodel.config.AppSettingsViewModel

@Composable
fun AparienciaScreen(
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
    val themeMode by viewModel.themeMode.collectAsState()

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
            Text("Apariencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
        HorizontalDivider(color = colors.divider)

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("TEMA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 4.dp))

            ThemeOption(icon = Icons.Default.SettingsBrightness, title = "Seguir sistema", subtitle = "Usa el tema del dispositivo", selected = themeMode == ThemeMode.SYSTEM, onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) }, colors = colors)
            ThemeOption(icon = Icons.Default.LightMode, title = "Claro", subtitle = "Siempre modo claro", selected = themeMode == ThemeMode.LIGHT, onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) }, colors = colors)
            ThemeOption(icon = Icons.Default.DarkMode, title = "Oscuro", subtitle = "Siempre modo oscuro", selected = themeMode == ThemeMode.DARK, onClick = { viewModel.setThemeMode(ThemeMode.DARK) }, colors = colors)
        }
    }
}

@Composable
private fun ThemeOption(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, colors: PrestadorColors) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceColor,
        tonalElevation = if (selected) 2.dp else 1.dp,
        modifier = Modifier.fillMaxWidth().border(if (selected) 2.dp else 1.dp, if (selected) colors.primaryOrange else colors.border, RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(colors.primaryOrange.copy(alpha = if (selected) 0.15f else 0.07f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = colors.textSecondary)
            }
            if (selected) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
        }
    }
}
















































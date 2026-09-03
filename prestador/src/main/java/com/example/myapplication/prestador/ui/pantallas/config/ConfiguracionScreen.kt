package com.example.myapplication.prestador.ui.pantallas.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.uishared.ui.components.profile.parts.DialogoPriorizarEmpresa
import com.example.myapplication.uishared.ui.components.profile.parts.DialogoDesactivarEmpresa

private val IconColorPresupuesto   = Color(0xFF1976D2)
private val IconColorCalendario    = Color(0xFF388E3C)
private val IconColorApariencia    = Color(0xFF7B1FA2)
private val IconColorNotificaciones = Color(0xFFE53935)
private val IconColorPerfil        = Color(0xFFF57C00)
private val IconColorLegal         = Color(0xFF455A64)
private val IconColorInfo          = Color(0xFF00796B)

@Composable
fun ConfiguracionScreen(
    onBack: () -> Unit = {},
    onNavigateToCalendario: () -> Unit = {},
    onNavigateToPresupuestoConfig: () -> Unit = {},
    onNavigateToApariencia: () -> Unit = {},
    onNavigateToNotificaciones: () -> Unit = {},
    onNavigateToTerminos: () -> Unit = {},
    onNavigateToPrivacidad: () -> Unit = {},
    onNavigateToAcercaDe: () -> Unit = {},
    onNavigateToGestionTurnos: () -> Unit = {},
    onNavigateToGestionVisitas: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val stateDeep by viewModel.state.collectAsStateWithLifecycle()
    val maestro = stateDeep.ecosistema
    val priorizarEmpresa = maestro?.cuenta?.priorizarEmpresa ?: false
    val tieneEmpresa = maestro?.empresas?.isNotEmpty() ?: false
    var mostrarConfirmarActivarEmpresa by remember { mutableStateOf(false) }
    var mostrarConfirmarDesactivarEmpresa by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refrescarDatos() }

    Column(modifier = Modifier.fillMaxSize().background(colors.backgroundColor)) {
        Row(modifier = Modifier.fillMaxWidth().background(colors.surfaceColor).statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = colors.textPrimary) }
            Icon(Icons.Default.Settings, null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Configuración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
        HorizontalDivider(color = colors.divider)

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ConfigSection(label = "Trabajo") {
                ConfigGroupItem(icon = Icons.Default.Description, iconColor = IconColorPresupuesto, title = "Presupuestos", subtitle = "Validez, nota legal y más", colors = colors, onClick = onNavigateToPresupuestoConfig)
                HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = colors.divider.copy(alpha = 0.6f))
                ConfigGroupItem(icon = Icons.Default.CalendarMonth, iconColor = IconColorCalendario, title = "Horarios de atención", subtitle = "Días y horarios", colors = colors, onClick = onNavigateToCalendario)
            }

            ConfigSection(label = "Preferencias") {
                ConfigGroupItem(icon = Icons.Default.Palette, iconColor = IconColorApariencia, title = "Apariencia", subtitle = "Claro, oscuro o sistema", colors = colors, onClick = onNavigateToApariencia)
                HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = colors.divider.copy(alpha = 0.6f))
                ConfigGroupItem(icon = Icons.Default.Notifications, iconColor = IconColorNotificaciones, title = "Notificaciones", subtitle = "Mensajes y alertas", colors = colors, onClick = onNavigateToNotificaciones)
            }

            ConfigSection(label = "Mi perfil") {
                ConfigGroupToggle(icon = Icons.Default.Business, iconColor = IconColorPerfil, title = "Modo empresa", subtitle = if (priorizarEmpresa) "Perfil personal desactivado" else if (tieneEmpresa) "Perfil personal activo" else "Necesitás agregar una empresa", checked = priorizarEmpresa, enabled = tieneEmpresa, colors = colors, onToggle = { enabled ->
                    if (enabled) mostrarConfirmarActivarEmpresa = true else mostrarConfirmarDesactivarEmpresa = true
                })
                if (priorizarEmpresa) {
                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = colors.divider.copy(alpha = 0.6f))
                    ConfigGroupItem(
                        icon = Icons.Default.CalendarMonth, 
                        iconColor = IconColorPerfil, 
                        title = "Gestión de Turnos", 
                        subtitle = "Espacios y staff local", 
                        colors = colors, 
                        onClick = onNavigateToGestionTurnos
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = colors.divider.copy(alpha = 0.6f))
                    ConfigGroupItem(
                        icon = Icons.Default.AssignmentInd, 
                        iconColor = IconColorPerfil, 
                        title = "Visitas Técnicas", 
                        subtitle = "Agenda de campo", 
                        colors = colors, 
                        onClick = onNavigateToGestionVisitas
                    )
                }
            }

            if (mostrarConfirmarActivarEmpresa) {
                DialogoPriorizarEmpresa(
                    onConfirm = {
                        mostrarConfirmarActivarEmpresa = false
                        // [FIX]: chat y búsqueda operan a nivel de SUCURSAL, no de empresa.
                        viewModel.alternarSoberania(maestro?.empresas?.firstOrNull()?.sucursales?.firstOrNull()?.sucursal?.id, true)
                    },
                    onDismiss = { mostrarConfirmarActivarEmpresa = false }
                )
            }

            if (mostrarConfirmarDesactivarEmpresa) {
                DialogoDesactivarEmpresa(
                    onConfirm = {
                        mostrarConfirmarDesactivarEmpresa = false
                        viewModel.alternarSoberania(null, false)
                    },
                    onDismiss = { mostrarConfirmarDesactivarEmpresa = false }
                )
            }

            ConfigSection(label = "Legal") {
                ConfigGroupItem(icon = Icons.Default.Gavel, iconColor = IconColorLegal, title = "Términos y condiciones", subtitle = "Condiciones de uso", colors = colors, onClick = onNavigateToTerminos)
                HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = colors.divider.copy(alpha = 0.6f))
                ConfigGroupItem(icon = Icons.Default.PrivacyTip, iconColor = IconColorLegal, title = "Política de privacidad", subtitle = "Protección de datos", colors = colors, onClick = onNavigateToPrivacidad)
            }

            var mostrarDialogCerrarSesion by remember { mutableStateOf(false) }
            ConfigSection(label = "Cuenta") {
                ConfigGroupItem(icon = Icons.AutoMirrored.Filled.Logout, iconColor = Color(0xFFF57C00), title = "Cerrar sesión", subtitle = "Salir de tu cuenta", colors = colors, onClick = { mostrarDialogCerrarSesion = true })
            }

            if (mostrarDialogCerrarSesion) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogCerrarSesion = false},
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = colors.primaryOrange) },
                    title = { Text("¿Cerrar sesión?") },
                    text = { Text("Vas a salir de tu cuenta en este dispositivo.") },
                    confirmButton = {
                        TextButton(onClick = {
                            mostrarDialogCerrarSesion = false
                            viewModel.cerrarSesion()
                            onSignOut()
                        }) { Text("Cerrar sesión", color = colors.primaryOrange) }
                    },
                    dismissButton = { TextButton(onClick = { mostrarDialogCerrarSesion = false }) { Text("Cancelar") } }
                )
            }

            ConfigSection(label = "Información") {
                ConfigGroupItem(icon = Icons.Default.Info, iconColor = IconColorInfo, title = "Acerca de", subtitle = "Versión y equipo", colors = colors, onClick = onNavigateToAcercaDe)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConfigSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = getPrestadorColors()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(horizontal = 4.dp))
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = colors.surfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
            Column(content = content)
        }
    }
}

@Composable
private fun ConfigGroupItem(icon: ImageVector, iconColor: Color, title: String, subtitle: String, colors: PrestadorColors, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp)) }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = colors.textSecondary, lineHeight = 17.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ConfigGroupToggle(icon: ImageVector, iconColor: Color, title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, colors: PrestadorColors, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).background(if (checked) iconColor.copy(alpha = 0.18f) else iconColor.copy(alpha = 0.10f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = if (enabled) iconColor else colors.textSecondary, modifier = Modifier.size(22.dp)) }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            Text(subtitle, fontSize = 12.sp, color = if (checked) iconColor else colors.textSecondary, lineHeight = 17.sp)
        }
        Switch(checked = checked, onCheckedChange = { if (enabled) onToggle(it) }, enabled = enabled, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = iconColor, uncheckedThumbColor = colors.textSecondary, uncheckedTrackColor = colors.border))
    }
}
















































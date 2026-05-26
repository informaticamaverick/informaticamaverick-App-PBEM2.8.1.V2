
package com.example.myapplication.prestador.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.profile.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.profile.ProfileState

// ── Colores de íconos por sección ────────────────────────────────────────────
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
    onSignOut: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val colors = getPrestadorColors()
    val profileState by viewModel.profileState.collectAsState()
    val provider = (profileState as? ProfileState.Success)?.provider
    val priorizarEmpresa = provider?.priorizarEmpresa ?: false
    val tieneEmpresa = (provider?.companies?.size ?: 0) > 0

    LaunchedEffect(Unit) {
        if (profileState !is ProfileState.Success) viewModel.loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
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
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = colors.primaryOrange,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Configuración",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        HorizontalDivider(color = colors.divider)

        // ── Contenido ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Trabajo
            ConfigSection(label = "Trabajo") {
                ConfigGroupItem(
                    icon = Icons.Default.Description,
                    iconColor = IconColorPresupuesto,
                    title = "Presupuestos",
                    subtitle = "Validez por defecto, nota legal y más",
                    colors = colors,
                    onClick = onNavigateToPresupuestoConfig
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = colors.divider.copy(alpha = 0.6f)
                )
                ConfigGroupItem(
                    icon = Icons.Default.CalendarMonth,
                    iconColor = IconColorCalendario,
                    title = "Horarios de atención",
                    subtitle = "Días, horarios y duración de turnos",
                    colors = colors,
                    onClick = onNavigateToCalendario
                )
            }

            // Preferencias
            ConfigSection(label = "Preferencias") {
                ConfigGroupItem(
                    icon = Icons.Default.Palette,
                    iconColor = IconColorApariencia,
                    title = "Apariencia",
                    subtitle = "Claro, oscuro o seguir el sistema",
                    colors = colors,
                    onClick = onNavigateToApariencia
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = colors.divider.copy(alpha = 0.6f)
                )
                ConfigGroupItem(
                    icon = Icons.Default.Notifications,
                    iconColor = IconColorNotificaciones,
                    title = "Notificaciones",
                    subtitle = "Mensajes, presupuestos y pedidos",
                    colors = colors,
                    onClick = onNavigateToNotificaciones
                )
            }

            // Mi perfil
            ConfigSection(label = "Mi perfil") {
                ConfigGroupToggle(
                    icon = Icons.Default.Business,
                    iconColor = IconColorPerfil,
                    title = "Modo empresa",
                    subtitle = if (priorizarEmpresa) "Perfil personal desactivado"
                               else if (tieneEmpresa) "Perfil personal activo"
                               else "Necesitás agregar una empresa primero",
                    checked = priorizarEmpresa,
                    enabled = tieneEmpresa,
                    colors = colors,
                    onToggle = { viewModel.toggleModoEmpresa(!priorizarEmpresa) }
                )
            }

            // Legal
            ConfigSection(label = "Legal") {
                ConfigGroupItem(
                    icon = Icons.Default.Gavel,
                    iconColor = IconColorLegal,
                    title = "Términos y condiciones",
                    subtitle = "Condiciones de uso de la plataforma",
                    colors = colors,
                    onClick = onNavigateToTerminos
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = colors.divider.copy(alpha = 0.6f)
                )
                ConfigGroupItem(
                    icon = Icons.Default.PrivacyTip,
                    iconColor = IconColorLegal,
                    title = "Política de privacidad",
                    subtitle = "Cómo usamos y protegemos tus datos",
                    colors = colors,
                    onClick = onNavigateToPrivacidad
                )
            }

            //Cuenta
            var mostrarDialogCerrarSesion by remember { mutableStateOf(false) }
            var mostrarDialogoEliminarCuenta by remember { mutableStateOf(false) }
            var mostrarSnackbarContrasena by remember { mutableStateOf(false) }

            ConfigSection(label = "Cuenta") {
                ConfigGroupItem(
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFF1565C0),
                    title = "Cambiar contraseña",
                    subtitle = "Te enviaremos un email con el enlace",
                    colors = colors,
                    onClick = {
                        val email = (profileState as? ProfileState.Success)?.provider?.email ?: ""
                        viewModel.sendPasswordResetEmail(email) {
                            mostrarSnackbarContrasena = true
                        }
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = colors.divider.copy(alpha = 0.6f)
                )
                ConfigGroupItem(
                    icon = Icons.Default.Logout,
                    iconColor = Color(0xFFF57C00),
                    title = "Cerrar sesión",
                    subtitle = "Salir de tu cuenta en este dispositivo",
                    colors = colors,
                    onClick = { mostrarDialogCerrarSesion = true}
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp),
                    color = colors.divider.copy(alpha = 0.6f)
                )
                ConfigGroupItem(
                    icon = Icons.Default.DeleteForever,
                    iconColor = Color(0xFFD32F2F),
                    title = "Eliminar cuenta",
                    subtitle = "Esta acción es permanente e irreversible",
                    colors = colors,
                    onClick = { mostrarDialogoEliminarCuenta = true}
                )
            }

            //Diálogo cerrar sesión
            if (mostrarDialogCerrarSesion) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogCerrarSesion = false},
                    icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = colors.primaryOrange) },
                    title = { Text("¿Cerrar sesión?") },
                    text = { Text("Vas a salir de tu cuenta en este dispositivo.") },
                    confirmButton = {
                        TextButton(onClick = {
                            mostrarDialogCerrarSesion = false
                            viewModel.signOut()
                            onSignOut()
                        }) { Text("Cerrar sesión", color = colors.primaryOrange) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogCerrarSesion = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo eliminar cuenta
            if (mostrarDialogoEliminarCuenta) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminarCuenta = false },
                    icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F)) },
                    title = { Text("¿Eliminar cuenta?") },
                    text = { Text("Esta acción borrará tu perfil, datos y configuración de forma permanente. No se puede deshacer.") },
                    confirmButton = {
                        TextButton(onClick = {
                            mostrarDialogoEliminarCuenta = false
                            viewModel.deleteAccount(
                                onSuccess = { onSignOut() },
                                onError = { /* podés mostrar un toast si querés */ }
                            )
                        }) { Text("Eliminar", color = Color(0xFFD32F2F)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoEliminarCuenta = false }) { Text("Cancelar") }
                    }
                )
            }

            // Snackbar cambiar contraseña
            if (mostrarSnackbarContrasena) {
                AlertDialog(
                    onDismissRequest = { mostrarSnackbarContrasena = false },
                    icon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1565C0)) },
                    title = { Text("Email enviado") },
                    text = { Text("Revisá tu correo para restablecer tu contraseña.") },
                    confirmButton = {
                        TextButton(onClick = { mostrarSnackbarContrasena = false }) { Text("OK") }
                    }
                )
            }

            // Información
            ConfigSection(label = "Información") {
                ConfigGroupItem(
                    icon = Icons.Default.Info,
                    iconColor = IconColorInfo,
                    title = "Acerca de",
                    subtitle = "Versión de la app, equipo y contacto",
                    colors = colors,
                    onClick = onNavigateToAcercaDe
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Sección con card agrupada ─────────────────────────────────────────────────
@Composable
private fun ConfigSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = getPrestadorColors()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

// ── Item de navegación dentro de una Card ────────────────────────────────────
@Composable
private fun ConfigGroupItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    colors: PrestadorColors,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
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
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                Text(subtitle, fontSize = 12.sp, color = colors.textSecondary, lineHeight = 17.sp)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Toggle dentro de una Card ─────────────────────────────────────────────────
@Composable
private fun ConfigGroupToggle(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    colors: PrestadorColors,
    onToggle: () -> Unit
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
                .background(
                    if (checked) iconColor.copy(alpha = 0.18f) else iconColor.copy(alpha = 0.10f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) iconColor else colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = if (checked) iconColor else colors.textSecondary,
                lineHeight = 17.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconColor,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.border
            )
        )
    }
}


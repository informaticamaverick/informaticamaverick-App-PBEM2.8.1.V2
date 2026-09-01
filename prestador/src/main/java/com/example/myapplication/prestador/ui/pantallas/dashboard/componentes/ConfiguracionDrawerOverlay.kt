package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun ConfiguracionDrawerOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onNavigateToPresupuestoConfig: () -> Unit,
    onNavigateToCalendarioConfig: () -> Unit,
    onNavigateToApariencia: () -> Unit,
    onNavigateToNotificaciones: () -> Unit,
    onNavigateToTerminos: () -> Unit,
    onNavigateToPrivacidad: () -> Unit,
    onNavigateToAcercaDe: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onSignOut: () -> Unit,
) {
    val colors = getPrestadorColors()
    
    // Simplificado para el reordenamiento, luego se reconecta la lógica de soberanía real
    val priorizarEmpresa = false
    val tieneEmpresa = false

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(220))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(260)) { -it } + fadeOut(tween(260))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.82f)
                    .background(colors.surfaceColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(colors.primaryOrange, colors.primaryOrange.copy(alpha = 0.7f))
                                )
                            )
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Text(
                                "Configuración",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    DrawerSectionLabel("Trabajo")
                    DrawerItem(Icons.Default.Description, Color(0xFF1976D2), "Presupuestos", "Validez, nota legal y más") {
                        onDismiss(); onNavigateToPresupuestoConfig()
                    }
                    DrawerDivider()
                    DrawerItem(Icons.Default.CalendarMonth, Color(0xFF388E3C), "Horarios de atención", "Días, horarios y duración") {
                        onDismiss(); onNavigateToCalendarioConfig()
                    }

                    Spacer(Modifier.height(4.dp))

                    DrawerSectionLabel("Preferencias")
                    DrawerItem(Icons.Default.Palette, Color(0xFF7B1FA2), "Apariencia", "Claro, oscuro o sistema") {
                        onDismiss(); onNavigateToApariencia()
                    }
                    DrawerDivider()
                    DrawerItem(Icons.Default.Notifications, Color(0xFFE53935), "Notificaciones", "Mensajes, presupuestos y pedidos") {
                        onDismiss(); onNavigateToNotificaciones()
                    }

                    Spacer(Modifier.height(4.dp))

                    DrawerSectionLabel("Mi perfil")
                    DrawerItem(Icons.Default.Person, Color(0xFFF57C00), "Editar perfil", "Nombre, foto y descripción") {
                        onDismiss(); onNavigateToEditProfile()
                    }
                    DrawerDivider()
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFF57C00).copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Business, null, tint = Color(0xFFF57C00), modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modo empresa", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text(
                                if (priorizarEmpresa) "Perfil personal desactivado"
                                else if (tieneEmpresa) "Perfil personal activo"
                                else "Agregá una empresa primero",
                                fontSize = 12.sp, color = colors.textSecondary
                            )
                        }
                        Switch(
                            checked = priorizarEmpresa,
                            onCheckedChange = { },
                            enabled = tieneEmpresa,
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.primaryOrange)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    DrawerSectionLabel("Legal")
                    DrawerItem(Icons.Default.Gavel, Color(0xFF455A64), "Términos y condiciones", "Condiciones de uso") {
                        onDismiss(); onNavigateToTerminos()
                    }
                    DrawerDivider()
                    DrawerItem(Icons.Default.PrivacyTip, Color(0xFF455A64), "Política de privacidad", "Cómo usamos tus datos") {
                        onDismiss(); onNavigateToPrivacidad()
                    }
                    DrawerDivider()
                    DrawerItem(Icons.Default.Info, Color(0xFF00796B), "Acerca de", "Versión y créditos") {
                        onDismiss(); onNavigateToAcercaDe()
                    }

                    Spacer(Modifier.height(4.dp))
                    
                    DrawerItem(Icons.Default.Logout, Color.Red, "Cerrar Sesión", "Salir de la cuenta") {
                        onDismiss(); onSignOut()
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionLabel(label: String) {
    val colors = getPrestadorColors()
    Text(
        label,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = colors.primaryOrange,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerDivider() {
    val colors = getPrestadorColors()
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp, end = 16.dp),
        color = colors.divider.copy(alpha = 0.5f)
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            Text(subtitle, fontSize = 12.sp, color = colors.textSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
    }
}


















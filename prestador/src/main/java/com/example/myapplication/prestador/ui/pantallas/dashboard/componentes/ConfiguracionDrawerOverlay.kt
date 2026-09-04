package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.ui.theme.LocalIsDarkTheme
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.profile.PerfilPrestadorDeepViewModel
import com.example.myapplication.uishared.ui.components.profile.parts.DialogoPriorizarEmpresa
import com.example.myapplication.uishared.ui.components.profile.parts.DialogoDesactivarEmpresa

// [ELITE]: paleta calcada de InicioComponents.ThemeColors — el drawer ahora comparte
// el mismo lenguaje visual que Inicio (tarjetas con borde, chips de ícono, fondo #030712).
private val DrawerDarkBg = Color(0xFF030712)
private val DrawerCardBg = Color(0xFF0F172A)
private val DrawerCardBorder = Color(0xFF334155).copy(alpha = 0.7f)
private val DrawerDivider = Color(0xFF1E293B)

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
    onNavigateToAyuda: () -> Unit,
    viewModel: PerfilPrestadorDeepViewModel = hiltViewModel()
) {
    // [ELITE]: el drawer, como Inicio, es siempre oscuro por diseño — antes seguía
    // getPrestadorColors() sin fijar el tema, que resuelve según el modo claro/oscuro
    // DEL DISPOSITIVO (quedaba blanco en dispositivos en modo claro, sin combinar con Inicio).
    CompositionLocalProvider(LocalIsDarkTheme provides true) {
    val colors = getPrestadorColors()

    val stateDeep by viewModel.state.collectAsStateWithLifecycle()
    val maestro = stateDeep.ecosistema
    val priorizarEmpresa = maestro?.cuenta?.priorizarEmpresa ?: false
    val tieneEmpresa = maestro?.empresas?.isNotEmpty() ?: false
    var mostrarConfirmarActivarEmpresa by remember { mutableStateOf(false) }
    var mostrarConfirmarDesactivarEmpresa by remember { mutableStateOf(false) }

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

        // [ELITE]: slide con resorte, de izquierda (fuera de pantalla) hacia la derecha
        // (su posición final) — resorte más "blando" (stiffness baja) para que el
        // movimiento se note en vez de sentirse como un aparecer instantáneo.
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(spring(dampingRatio = 0.82f, stiffness = 190f)) { -it } + fadeIn(tween(200)),
            exit = slideOutHorizontally(tween(260)) { -it } + fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.86f)
                    .background(DrawerDarkBg)
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
                    // --- TARJETA DE PERFIL: foto + nombre + PRO + rating, toca para ver detalles ---
                    val perfilPrestador = maestro?.prestador?.perfil
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                            .clickable { onDismiss(); onNavigateToEditProfile() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val fotoPerfil = com.example.myapplication.core.utilidades.ImageUtils.processImageSource(
                            perfilPrestador?.urlMiniatura ?: perfilPrestador?.urlFoto
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primaryOrange.copy(alpha = if (fotoPerfil != null) 0f else 1f))
                                .border(1.dp, DrawerDivider, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fotoPerfil != null) {
                                coil.compose.AsyncImage(
                                    model = fotoPerfil,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = perfilPrestador?.titulo?.trim()?.firstOrNull()?.uppercase() ?: "?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = perfilPrestador?.titulo?.ifBlank { "Mi perfil" } ?: "Mi perfil",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (perfilPrestador?.estaSuscrito == true) {
                                    Surface(color = colors.primaryOrange, shape = RoundedCornerShape(2.dp)) {
                                        Text(
                                            text = "PRO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Editar perfil",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    }
                    HorizontalDivider(color = DrawerDivider)

                    // --- HEADER: chip de ícono + título "Configuración" ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(colors.primaryOrange.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .border(1.dp, colors.primaryOrange.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, null, tint = colors.primaryOrange, modifier = Modifier.size(19.dp))
                        }
                        Text("Configuración", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                    }
                    HorizontalDivider(color = DrawerDivider)

                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var idx = 0

                        SeccionCard(indice = idx++, visible = visible, dot = Color(0xFF06B6D4), label = "Trabajo") {
                            DrawerItem(Icons.Default.Description, Color(0xFF1976D2), "Presupuestos", "Validez, nota legal y más") {
                                onDismiss(); onNavigateToPresupuestoConfig()
                            }
                            DrawerItemDivider()
                            DrawerItem(Icons.Default.CalendarMonth, Color(0xFF388E3C), "Horarios de atención", "Días, horarios y duración") {
                                onDismiss(); onNavigateToCalendarioConfig()
                            }
                        }

                        SeccionCard(indice = idx++, visible = visible, dot = Color(0xFFA855F7), label = "Preferencias") {
                            DrawerItem(Icons.Default.Palette, Color(0xFF7B1FA2), "Apariencia", "Claro, oscuro o sistema") {
                                onDismiss(); onNavigateToApariencia()
                            }
                            DrawerItemDivider()
                            DrawerItem(Icons.Default.Notifications, Color(0xFFE53935), "Notificaciones", "Mensajes, presupuestos y pedidos") {
                                onDismiss(); onNavigateToNotificaciones()
                            }
                        }

                        SeccionCard(indice = idx++, visible = visible, dot = Color(0xFFF59E0B), label = "Mi perfil") {
                            DrawerToggleItem(
                                icon = Icons.Default.Business,
                                iconColor = Color(0xFFF57C00),
                                title = "Modo empresa",
                                subtitle = if (priorizarEmpresa) "Perfil personal desactivado"
                                    else if (tieneEmpresa) "Perfil personal activo"
                                    else "Agregá una empresa primero",
                                checked = priorizarEmpresa,
                                enabled = tieneEmpresa,
                                accent = colors.primaryOrange,
                                onToggle = { enabled ->
                                    if (enabled) mostrarConfirmarActivarEmpresa = true else mostrarConfirmarDesactivarEmpresa = true
                                }
                            )
                        }

                        SeccionCard(indice = idx++, visible = visible, dot = Color(0xFF38BDF8), label = "Soporte") {
                            DrawerItem(Icons.AutoMirrored.Filled.HelpOutline, Color(0xFF0288D1), "Ayuda y soporte", "Preguntas frecuentes y contacto") {
                                onDismiss(); onNavigateToAyuda()
                            }
                        }

                        SeccionCard(indice = idx++, visible = visible, dot = Color(0xFF94A3B8), label = "Legal") {
                            DrawerItem(Icons.Default.Gavel, Color(0xFF455A64), "Términos y condiciones", "Condiciones de uso") {
                                onDismiss(); onNavigateToTerminos()
                            }
                            DrawerItemDivider()
                            DrawerItem(Icons.Default.PrivacyTip, Color(0xFF455A64), "Política de privacidad", "Cómo usamos tus datos") {
                                onDismiss(); onNavigateToPrivacidad()
                            }
                            DrawerItemDivider()
                            DrawerItem(Icons.Default.Info, Color(0xFF00796B), "Acerca de", "Versión y créditos") {
                                onDismiss(); onNavigateToAcercaDe()
                            }
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(280, delayMillis = 60 + idx * 45)) + slideInVertically(tween(280, delayMillis = 60 + idx * 45)) { it / 6 }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.07f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .clickable { onDismiss(); onSignOut() }
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                Text("Cerrar sesión", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                    }
                }
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
    }
    }
}

/**
 * [ELITE]: tarjeta de sección — mismo tratamiento que "Caja de Herramientas"/"Agenda de Hoy"
 * de Inicio (borde #334155, fondo #0F172A, radio 12dp, punto de color + label en mayúsculas).
 * Todas las secciones quedan siempre desplegadas (sin acordeón); cada una entra con un
 * pequeño desfasaje según `indice` para dar sensación de despliegue en cascada al abrir el drawer.
 */
@Composable
private fun SeccionCard(indice: Int, visible: Boolean, dot: Color, label: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = getPrestadorColors()
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, delayMillis = 60 + indice * 45)) + slideInVertically(tween(280, delayMillis = 60 + indice * 45)) { it / 6 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DrawerCardBg)
                .border(1.dp, DrawerCardBorder, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).background(dot, RoundedCornerShape(2.dp)))
                Text(
                    label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary,
                    letterSpacing = 0.7.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun DrawerItemDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 62.dp, end = 14.dp), color = DrawerDivider)
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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(9.dp))
                .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun DrawerToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    accent: Color,
    onToggle: (Boolean) -> Unit
) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(9.dp))
                .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(17.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = colors.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle(it) },
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
        )
    }
}

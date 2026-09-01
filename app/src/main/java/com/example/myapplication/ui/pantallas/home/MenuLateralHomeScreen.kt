package com.example.myapplication.ui.pantallas.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.core.dominio.modelos.UsuarioDominio
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.viewmodel.home.MenuHomeViewModel

/**
 * --- MENU LATERAL ELITE (SIDE SHEET v2026) ---
 * [REFACTOR]: Ya no es un NavigationDrawer. Ahora es una SideSheet inyectada bajo demanda.
 * Propósito: Eliminar conflictos de estado y garantizar paridad visual total.
 */
@Composable
fun MenuLateralHomeScreen(
    onNavigate: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: MenuHomeViewModel = hiltViewModel()
) {
    val usuario by viewModel.usuarioState.collectAsStateWithLifecycle()
    val cyberShape = CutCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .drawBehind {
                // 🔥 [ELITE] Dibujado Quirúrgico: Solo Derecha y Cortes (Paridad Total)
                val strokeWidth = 1.2.dp.toPx()
                val radio = 32.dp.toPx()
                val w = size.width
                val h = size.height
                
                val path = Path().apply {
                    moveTo(w - radio, 0f)
                    lineTo(w, radio)
                    lineTo(w, h - radio)
                    lineTo(w - radio, h)
                }

                val gradiente = Brush.verticalGradient(
                    listOf(
                        SharedPalette.ElectricCyan,
                        Color.Transparent,
                        SharedPalette.DeepRed
                    )
                )

                drawPath(
                    path = path,
                    brush = gradiente,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawPath(
                    path = path,
                    brush = gradiente,
                    style = Stroke(width = strokeWidth * 2f, cap = StrokeCap.Round),
                    alpha = 0.2f
                )
            },
        color = SharedPalette.V2DeepVoid,
        shape = cyberShape
    ) {
        MenuLateralContent(
            usuario = usuario,
            onNavigate = onNavigate,
            onLogout = { 
                viewModel.cerrarSesion { 
                    onClose()
                }
            }
        )
    }
}

@Composable
private fun MenuLateralContent(
    usuario: UsuarioDominio?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                UserHeaderCyber(
                    usuario = usuario,
                    onClick = { onNavigate("perfil_cliente") }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                MenuLateralSectionTitle("ACTIVIDAD", "📈")
                MenuLateralItem(Icons.Default.AttachMoney, "MIS PRESUPUESTOS", "Historial de ofertas", { onNavigate("concursos") })
                MenuLateralItem(Icons.Default.ShoppingBag, "MIS PEDIDOS", "Seguimiento activo", { 
                    // TODO: Implementar pantalla de pedidos
                })
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MenuLateralSectionTitle("IDENTIDAD", "👤")
                MenuLateralItem(Icons.Default.AccountCircle, "EDITAR PERFIL", "Datos de cuenta", { onNavigate("perfil_cliente") })
                MenuLateralItem(Icons.Default.LocationOn, "DIRECCIONES", "Puntos de entrega", { onNavigate("perfil_cliente") })
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MenuLateralSectionTitle("SISTEMA", "⚙️")
                MenuLateralItem(Icons.Default.Settings, "CONFIGURACIÓN", "Privacidad y Notificaciones", { onNavigate("config_user") })
                MenuLateralItem(Icons.Default.Palette, "APARIENCIA", "Temas y Estilo", { onNavigate("config_user") })
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MenuLateralSectionTitle("SOPORTE", "🛡️")
                MenuLateralItem(Icons.AutoMirrored.Filled.Help, "CENTRO DE AYUDA", "Preguntas y Soporte", { })
                MenuLateralItem(Icons.Default.Gavel, "LEGAL", "Términos y Privacidad", { })
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, SharedPalette.V2DeepVoid)
                    )
                )
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CutCornerShape(topStart = 0.dp, bottomEnd = 12.dp, topEnd = 0.dp, bottomStart = 4.dp),
                border = BorderStroke(1.2.dp, Color.Red.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red,
                    containerColor = Color.Red.copy(alpha = 0.05f)
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("CERRAR SESIÓN", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun UserHeaderCyber(
    usuario: UsuarioDominio?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                MoldeBurbujaPerfilV3(
                    perfil = PerfilIdentidadV3(
                        id = usuario?.id ?: "guest",
                        nombre = usuario?.nombreVisible ?: "Invitado",
                        iniciales = (usuario?.nombreVisible ?: "G").take(2).uppercase(),
                        photoUrl = usuario?.urlMiniatura ?: usuario?.urlFoto,
                        estaEnLinea = true,
                        estaVerificado = false // Usuarios suelen no estar verificados en este contexto
                    ),
                    tamanoBase = 54.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ID_OPERATIVA",
                        style = AppTypography.HeaderTitle.copy(fontSize = 9.sp, color = SharedPalette.ElectricCyan)
                    )
                    Text(
                        text = usuario?.nombreVisible?.uppercase() ?: "RECOLECTANDO...",
                        style = AppTypography.HeaderTitle.copy(fontSize = 16.sp, color = Color.White),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 🔥 [ELITE UX]: Flecha indicadora de navegación al perfil
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = usuario?.correo ?: "Sincronizando...",
            color = Color.Gray,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun MenuLateralSectionTitle(title: String, emoji: String) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 12.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = AppTypography.HeaderTitle.copy(
                fontSize = 10.sp, 
                color = Color.Gray,
                letterSpacing = 1.5.sp
            )
        )
    }
}

@Composable
private fun MenuLateralItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CutCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), CutCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Preview(name = "Menú SideSheet - Vista Elite", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMenuLateralHomeScreen() {
    com.example.myapplication.ui.estilos.PBEMTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            MenuLateralHomeScreen(
                onNavigate = {},
                onClose = {}
            )
        }
    }
}

package com.example.myapplication.ui.pantallas.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
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
import com.example.myapplication.ui.componentes.be.vm.BeCerebroViewModel
import com.example.myapplication.ui.componentes.be.modelos.ContextoHUD
import com.example.myapplication.viewmodel.profile.ArmadorUsuarioViewModel

import com.example.myapplication.ui.pantallas.profile.componentes.ConfigNotificacionSheet
import com.example.myapplication.ui.pantallas.profile.componentes.PermisosConfigSheet

/**
 * --- PANTALLA DE CONFIGURACIÓN (V2026) ---
 * Gestiona ajustes técnicos y baja de cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigUserScreen(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: ArmadorUsuarioViewModel = hiltViewModel(),
    beViewModel: BeCerebroViewModel = hiltViewModel()
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNotifSheet by remember { mutableStateOf(false) }
    var showPermisosSheet by remember { mutableStateOf(false) }
    val estaCargando by viewModel.estaCargando.collectAsStateWithLifecycle()

    val beConfig = remember { ContextoHUD.PERFIL.crearConfiguracionBase() }

    // 🔥 [ELITE]: Contrato de Pantalla Soberano mediante Mapa de Registros
    DisposableEffect(Unit) {
        beViewModel.navCoordinador.registrarPantalla(beConfig)
        onDispose {
            beViewModel.navCoordinador.removerPantalla(beConfig.id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CONFIGURACIÓN", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0E14))
            )
        },
        containerColor = Color(0xFF0A0E14)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            ConfigSectionTitle("APLICACIÓN")
            ConfigItem(Icons.Default.Notifications, "Notificaciones", "Gestión de alertas tácticas") { showNotifSheet = true }
            ConfigItem(Icons.Default.Palette, "Apariencia", "Temas y estilo visual")
            ConfigItem(Icons.Default.Language, "Idioma", "Español (Standard)")
            
            Spacer(modifier = Modifier.height(24.dp))

            ConfigSectionTitle("PRIVACIDAD Y PERMISOS")
            ConfigItem(Icons.Default.Shield, "Permisos del Sistema", "GPS, Cámara y Notificaciones") { 
                showPermisosSheet = true 
            }
            ConfigItem(Icons.Default.Lock, "Privacidad de Datos", "Quién puede ver tu perfil")

            Spacer(modifier = Modifier.height(24.dp))

            ConfigSectionTitle("CUENTA Y SEGURIDAD")
            ConfigItem(Icons.Default.Security, "Seguridad", "Autenticación y accesos")
            ConfigItem(Icons.Default.Link, "Redes Vinculadas", "Cuentas conectadas")

            Spacer(modifier = Modifier.height(24.dp))

            ConfigSectionTitle("SOPORTE")
            ConfigItem(Icons.AutoMirrored.Filled.Help, "Centro de Ayuda", "FAQ y contacto técnico")
            ConfigItem(Icons.Default.Description, "Términos y Condiciones", "Marco legal v2026")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ConfigSectionTitle("ZONA DE RIESGO")
            Surface(
                onClick = { showDeleteConfirm = true },
                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFEF4444))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Eliminar Cuenta", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        Text("Borrar datos permanentemente", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    ConfigNotificacionSheet(
        isVisible = showNotifSheet,
        onClose = { showNotifSheet = false }
    )

    PermisosConfigSheet(
        isVisible = showPermisosSheet,
        onClose = { showPermisosSheet = false }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿ELIMINAR CUENTA?") },
            text = { Text("Esta acción borrará todas tus identidades y empresas vinculadas.") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminarCuenta(onAccountDeleted) }) {
                    Text("BORRAR TODO", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    if (estaCargando) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
    }

   // ConfigNotificacionSheet(
   //     isVisible = showNotifSheet,
   //     onClose = { showNotifSheet = false }
    //)
}

@Composable
private fun ConfigSectionTitle(title: String) {
    Text(text = title, color = Color(0xFFA78BFA), fontWeight = FontWeight.Black, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun ConfigItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}



































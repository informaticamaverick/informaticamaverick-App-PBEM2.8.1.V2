package com.example.myapplication.presentation.features.profile

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
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme

/**
 * --- CONFIG USER SCREEN (V1.1) ---
 * Pantalla dedicada a configuraciones técnicas, notificaciones y ajustes de la aplicación.
 * [ACTUALIZADO]: Botón de eliminación de cuenta integrado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigUserScreen(
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("CONFIGURACIÓN", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold) },
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
            ConfigItem(Icons.Default.Notifications, "Notificaciones", "Gestionar alertas y avisos")
            ConfigItem(Icons.Default.Palette, "Apariencia", "Modo oscuro y temas")
            ConfigItem(Icons.Default.Language, "Idioma", "Español (Argentina)")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ConfigSectionTitle("SEGURIDAD")
            ConfigItem(Icons.Default.Lock, "Privacidad del Perfil", "Quién puede ver mis datos")
            ConfigItem(Icons.Default.Shield, "Verificación", "Estado de identidad Maverick")
            ConfigItem(Icons.Default.Key, "Cambiar Contraseña", "Actualizar credenciales")

            Spacer(modifier = Modifier.height(24.dp))
            
            ConfigSectionTitle("SOPORTE")
            ConfigItem(Icons.AutoMirrored.Filled.Help, "Centro de Ayuda", "Preguntas frecuentes")
            ConfigItem(Icons.Default.Description, "Términos y Condiciones", "Información legal")

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(48.dp))

            // --- ZONA PELIGROSA ---
            ConfigSectionTitle("ZONA DE RIESGO")
            Surface(
                onClick = { showDeleteConfirm = true },
                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Eliminar Cuenta", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Borrar permanentemente todos mis datos", color = Color(0xFFEF4444).copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1A1A24),
            title = { Text("¿ELIMINAR CUENTA?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción es irreversible. Se borrarán todos tus datos de perfil, direcciones y empresas.", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFullAccount(onAccountDeleted)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("BORRAR TODO", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ConfigSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFFA78BFA),
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun ConfigItem(icon: ImageVector, title: String, subtitle: String) {
    Surface(
        onClick = { /* TODO */ },
        color = Color.White.copy(alpha = 0.05f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

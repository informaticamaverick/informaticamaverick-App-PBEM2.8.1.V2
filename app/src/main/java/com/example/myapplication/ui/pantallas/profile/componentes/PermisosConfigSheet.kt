package com.example.myapplication.ui.pantallas.profile.componentes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEmergenteV3
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- HOJA DE GESTIÓN DE PERMISOS (v2026.ELITE) ---
 * [PROPÓSITO]: Transparencia y control sobre los datos del usuario.
 */
@Composable
fun PermisosConfigSheet(
    isVisible: Boolean,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    MoldeSheetEmergenteV3(
        estaVisible = isVisible,
        alCerrar = onClose,
        tituloCabecera = "PRIVACIDAD Y PERMISOS",
        subtituloCabecera = "Control de Acceso al Hardware",
        iconoCabecera = "🛡️",
        colorBordeAcento = Color(0xFFA78BFA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PermissionStatusItem(
                icon = Icons.Default.LocationOn,
                title = "Ubicación (GPS)",
                description = "Necesario para encontrar servicios cercanos",
                isGranted = true // Placeholder
            )
            PermissionStatusItem(
                icon = Icons.Default.CameraAlt,
                title = "Cámara",
                description = "Usada para enviar fotos en el chat",
                isGranted = true // Placeholder
            )
            PermissionStatusItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones",
                description = "Alertas tácticas y seguimiento",
                isGranted = true // Placeholder
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { openAppSettings(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA))
            ) {
                Icon(Icons.Default.Settings, null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text("GESTIONAR EN AJUSTES", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            
            Text(
                "Mav App no comparte tus datos de ubicación con terceros sin tu consentimiento explícito.",
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun PermissionStatusItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isGranted) SharedPalette.SuccessGreen.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (isGranted) SharedPalette.SuccessGreen else Color.Red, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(description, color = Color.Gray, fontSize = 11.sp)
        }
        
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, null, tint = SharedPalette.SuccessGreen, modifier = Modifier.size(16.dp))
        } else {
            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(16.dp))
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

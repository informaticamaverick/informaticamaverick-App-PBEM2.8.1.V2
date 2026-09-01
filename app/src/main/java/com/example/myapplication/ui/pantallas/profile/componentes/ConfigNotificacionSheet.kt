package com.example.myapplication.ui.pantallas.profile.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.componentes.sistema.lista.MoldeSheetEmergenteV3
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- HOJA DE CONFIGURACIÓN DE NOTIFICACIONES (v2026.ELITE) ---
 * [PROPÓSITO]: Control granular de alertas push.
 */
@Composable
fun ConfigNotificacionSheet(
    isVisible: Boolean,
    onClose: () -> Unit
) {
    MoldeSheetEmergenteV3(
        estaVisible = isVisible,
        alCerrar = onClose,
        tituloCabecera = "NOTIFICACIONES",
        subtituloCabecera = "Centro de Alertas Tácticas",
        iconoCabecera = "🔔",
        colorBordeAcento = SharedPalette.ElectricCyan
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NotificationToggleItem(
                title = "Mensajes de Chat",
                description = "Alertas de nuevos mensajes y archivos",
                initialState = true
            )
            NotificationToggleItem(
                title = "Promociones y Ofertas",
                description = "Novedades de servicios en tu zona",
                initialState = true
            )
            NotificationToggleItem(
                title = "Estados de Pedido",
                description = "Seguimiento en tiempo real de tus solicitudes",
                initialState = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Las notificaciones críticas de seguridad no pueden desactivarse.",
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    description: String,
    initialState: Boolean
) {
    var checked by remember { mutableStateOf(initialState) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = SharedPalette.ElectricCyan,
                checkedTrackColor = SharedPalette.ElectricCyan.copy(alpha = 0.3f)
            )
        )
    }
}

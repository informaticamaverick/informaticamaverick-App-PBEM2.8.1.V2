package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * --- MENÚ CONTEXTUAL DE MENSAJE MAVERICK (V2026.FINAL) ---
 * [ELITE]: Diálogo translúcido estilo Telegram para acciones de mensaje.
 */

@Composable
fun MenuContextualMensaje(
    alResponder: () -> Unit,
    alCopiar: () -> Unit,
    alReenviar: () -> Unit,
    alEliminar: () -> Unit,
    alCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = alCerrar) {
        Column(
            modifier = modifier
                .width(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.95f))
                .padding(8.dp)
        ) {
            ItemMenuElite("Responder", Icons.AutoMirrored.Filled.Reply, Color.White) { alResponder(); alCerrar() }
            ItemMenuElite("Copiar", Icons.Default.ContentCopy, Color.White) { alCopiar(); alCerrar() }
            ItemMenuElite("Reenviar", Icons.AutoMirrored.Filled.Forward, Color.White) { alReenviar(); alCerrar() }
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
            ItemMenuElite("Eliminar", Icons.Default.Delete, Color(0xFFEF4444)) { alEliminar(); alCerrar() }
        }
    }
}

@Composable
private fun ItemMenuElite(
    texto: String,
    icono: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = texto,
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


































package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- DIÁLOGOS DE CONFIRMACIÓN DEL PERFIL (Ley #10) ---
 */

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    textoConfirmar: String = "CONFIRMAR",
    textoCancelar: String = "CANCELAR",
    colorConfirmar: Color = Color(0xFFEF4444),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo, fontWeight = FontWeight.Black, fontSize = 18.sp) },
        text = { Text(mensaje, color = Color.Gray) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(textoConfirmar, color = colorConfirmar, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(textoCancelar, color = Color.Gray)
            }
        },
        containerColor = Color(0xFF1A1A24),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DialogoSuscripcion(
    onDismiss: () -> Unit,
    onSuscribirse: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MÉTRICAS ELITE", fontWeight = FontWeight.Black) },
        text = { Text("Suscríbete para acceder a estadísticas avanzadas y posicionamiento prioritario.") },
        confirmButton = {
            Button(onClick = onSuscribirse) {
                Text("SUSCRIBIRSE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        },
        containerColor = Color(0xFF1A1A24),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DialogoDescartarCambios(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogoConfirmacion(
        titulo = "¿DESCARTAR CAMBIOS?",
        mensaje = "Tienes cambios sin guardar. Si sales ahora, se perderán permanentemente.",
        textoConfirmar = "DESCARTAR",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

/**
 * --- DIÁLOGO DE SALIDA SEGURA (Elite v2026) ---
 * [PROPÓSITO]: Ofrecer al usuario la opción de Guardar, Descartar o Cancelar al salir.
 */
@Composable
fun DialogoSalidaSegura(
    onGuardar: () -> Unit,
    onDescartar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("¿GUARDAR CAMBIOS?", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White) },
        text = { 
            Text(
                "Tienes modificaciones pendientes en tu perfil. ¿Qué deseas hacer?",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            ) 
        },
        confirmButton = {
            Button(
                onClick = onGuardar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GUARDAR Y SALIR", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDescartar) {
                    Text("DESCARTAR", color = Color.Red.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onCancelar) {
                    Text("CANCELAR", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF1A1A24),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun DialogoPriorizarEmpresa(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogoConfirmacion(
        titulo = "¿ACTIVAR MODO EMPRESA?",
        mensaje = "Al activar una empresa, tu perfil personal se ocultará de las búsquedas para priorizar tu marca comercial. Podrás revertirlo en cualquier momento.",
        textoConfirmar = "ACTIVAR SOBERANÍA",
        colorConfirmar = Color(0xFF3B82F6),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DialogoDesactivarEmpresa(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogoConfirmacion(
        titulo = "¿REVERTIR A PERFIL PERSONAL?",
        mensaje = "Esto desactivará la soberanía de la empresa y volverá a mostrar tu perfil individual en los resultados.",
        textoConfirmar = "REVERTIR",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}


































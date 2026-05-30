package com.example.myapplication.prestador.ui.dashboard.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

private const val SOPORTE_WHATSAPP = "5491112345678" // reemplazar con número real
private const val SOPORTE_EMAIL = "soporte@pbem.com"  // reemplazar con email real

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSoporteSheet(onDismiss: () -> Unit) {
    val colors = getPrestadorColors()
    val accent = colors.primaryOrange
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.textSecondary.copy(alpha = 0.3f))
            )
            Spacer(Modifier.height(20.dp))

            // Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SupportAgent, null, tint = accent, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("Contactar soporte", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Estamos para ayudarte\nLunes a viernes de 9 a 18 hs",
                fontSize = 13.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(28.dp))

            // Opción WhatsApp
            ContactoOpcionCard(
                icon = Icons.Default.Chat,
                iconColor = Color(0xFF25D366),
                titulo = "WhatsApp",
                subtitulo = "Respuesta en menos de 1 hora",
                etiqueta = "Recomendado",
                etiquetaColor = Color(0xFF25D366),
                onClick = {
                    val url = "https://wa.me/$SOPORTE_WHATSAPP?text=Hola,%20necesito%20ayuda%20con%20la%20app%20PBEM"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )

            Spacer(Modifier.height(12.dp))

            // Opción Email
            ContactoOpcionCard(
                icon = Icons.Default.Email,
                iconColor = Color(0xFF4A90D9),
                titulo = "Correo electrónico",
                subtitulo = SOPORTE_EMAIL,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$SOPORTE_EMAIL")
                        putExtra(Intent.EXTRA_SUBJECT, "Consulta desde la app PBEM")
                    }
                    context.startActivity(Intent.createChooser(intent, "Enviar correo"))
                }
            )

            Spacer(Modifier.height(28.dp))

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar", color = colors.textSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ContactoOpcionCard(
    icon: ImageVector,
    iconColor: Color,
    titulo: String,
    subtitulo: String,
    etiqueta: String? = null,
    etiquetaColor: Color = Color.Green,
    onClick: () -> Unit
) {
    val colors = getPrestadorColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.backgroundColor.copy(alpha = 0.6f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    if (etiqueta != null) {
                        Box(
                            modifier = Modifier
                                .background(etiquetaColor.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(etiqueta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = etiquetaColor)
                        }
                    }
                }
                Text(subtitulo, fontSize = 12.sp, color = colors.textSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}

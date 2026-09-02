package com.example.myapplication.prestador.ui.pantallas.dashboard.componentes


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme


@Composable
fun MatriculaVerificadaDialog(onDismiss: () -> Unit) {
    val colors = GestionTurnosTheme
    val dorado = Color(0xFFFFC107)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.CardBg,
        titleContentColor = colors.TextPrimary,
        textContentColor = colors.TextSecondary,
        icon = {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = dorado,
                modifier = Modifier.size(48.dp)
            )
        },

        title = {
            Text(
                text = "¡Tu matrícula fue verificada!",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Ya tenés la insignia de verificado en tu perfil. Mostrale a tus clientes que sos un profesional confiable.",
                textAlign = TextAlign.Center,
                color = colors.TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("¡Genial!", color = colors.BrandOrange, fontWeight = FontWeight.Bold)
            }
        }
    )
}
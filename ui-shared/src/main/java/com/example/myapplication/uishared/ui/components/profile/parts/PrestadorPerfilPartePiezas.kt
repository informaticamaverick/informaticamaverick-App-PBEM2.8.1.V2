package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- PIEZAS ATÓMICAS DEL PERFIL (Ley #10) ---
 * [PROPÓSITO]: Unidades mínimas de UI reutilizables para el Perfil del Prestador.
 */

private val ColorOroMav = Color(0xFFFFD700)
private val ColorAcentoMav = Color(0xFFFF7043)

@Composable
fun EstrellasCalificacionMav(
    calificacion: Float,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < calificacion.toInt()) Icons.Default.Star else Icons.Default.StarOutline,
                contentDescription = null,
                tint = if (index < calificacion.toInt()) ColorOroMav else Color.Gray.copy(0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "%.1f".format(calificacion),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun InsigniaVerificadoMav(
    modifier: Modifier = Modifier,
    tamanio: androidx.compose.ui.unit.Dp = 20.dp
) {
    Icon(
        imageVector = Icons.Default.Verified,
        contentDescription = "Verificado",
        tint = ColorAcentoMav,
        modifier = modifier.size(tamanio)
    )
}

@Composable
fun CabeceraSeccionMav(
    titulo: String,
    emoji: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emoji.isNotBlank()) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun TarjetaMetricaMav(
    etiqueta: String,
    valor: String,
    icono: ImageVector,
    color: Color = ColorAcentoMav,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.height(60.dp).width(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.height(4.dp))
            Text(valor, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(etiqueta.uppercase(), color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun EtiquetaCapacidadMav(
    emoji: String, 
    etiqueta: String,
    estaActiva: Boolean = true
) {
    val alpha = if (estaActiva) 1f else 0.3f
    Surface(
        color = Color.White.copy(alpha = if (estaActiva) 0.05f else 0.02f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(if (estaActiva) 0.1f else 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 14.sp, modifier = Modifier.alpha(alpha))
            Spacer(Modifier.width(8.dp))
            Text(etiqueta, color = if (estaActiva) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun NotificacionGuardadoMav() {
    Surface(
        modifier = Modifier
            .padding(bottom = 100.dp)
            .height(40.dp)
            .widthIn(min = 150.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF4ADE80),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("CAMBIOS GUARDADOS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}


































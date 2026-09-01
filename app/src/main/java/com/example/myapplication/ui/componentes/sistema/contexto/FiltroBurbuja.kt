package com.example.myapplication.ui.componentes.sistema.contexto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.estilos.ClienteTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto

/**
 * --- 🫧 ÁTOMO: BURBUJA DE FILTRO ---
 * [LEY #10]: Representación visual de un filtro activo (Pill style).
 * [LEY #9]: Estándar Mav en Español.
 */

data class ModeloBurbujaFiltro(
    val id: String,
    val etiqueta: String,
    val emoji: String,
    val color: Color = SharedPalette.ElectricCyan
)

@Composable
fun BurbujaFiltroElite(
    modelo: ModeloBurbujaFiltro,
    alEliminar: () -> Unit
) {
    Surface(
        onClick = alEliminar,
        color = modelo.color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp), // 🔥 [ELITE]: Esquinas más rectas
        border = BorderStroke(1.dp, modelo.color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextCompacto(text = modelo.emoji, fontSize = 14.sp)
            TextCompacto(
                text = modelo.etiqueta.uppercase(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewBurbujaFiltroElite() {
    ClienteTheme {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BurbujaFiltroElite(
                modelo = ModeloBurbujaFiltro("1", "Urgente", "🔥"),
                alEliminar = {}
            )
            BurbujaFiltroElite(
                modelo = ModeloBurbujaFiltro("2", "Online", "📡", color = SharedPalette.NeonCyan),
                alEliminar = {}
            )
        }
    }
}


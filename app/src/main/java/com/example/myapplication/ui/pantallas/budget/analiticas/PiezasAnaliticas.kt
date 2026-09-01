package com.example.myapplication.ui.pantallas.budget.analiticas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.uishared.ui.components.TextCompacto
import java.util.Locale

/**
 * --- 🧩 PIEZAS ANALÍTICAS (ÁTOMOS - v2026.ELITE) ---
 * Título: Piezas Analíticas
 * Propósito: Componentes mínimos reutilizables para el panel de inteligencia de mercado.
 * Funcionamiento Interno: Implementa átomos visuales siguiendo la Ley #10 y Ley #11.
 * Relación: Consumidos por BloquesAnaliticos y SeccionesAnaliticas.
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun TarjetaKpiAnalitico(
    modifier: Modifier = Modifier,
    etiqueta: String,
    valor: Double,
    colorAcento: Color = Color.White
) {
    val locale = LocalConfiguration.current.locales[0]
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.03f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.06f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextCompacto(
                text = etiqueta.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = Color.Gray,
                style = TextStyle(letterSpacing = 1.sp)
            )
            TextCompacto(
                text = "$ ${String.format(locale, "%,.0f", valor)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = colorAcento
            )
        }
    }
}

@Composable
fun ChipCondicionAnalitica(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            TextCompacto(
                text = text,
                color = Color.LightGray,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(backgroundColor = 0xFF020408, showBackground = true)
@Composable
private fun PreviewPiezasAnaliticas() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TarjetaKpiAnalitico(etiqueta = "Promedio Mercado", valor = 45000.0)
        ChipCondicionAnalitica(icon = Icons.Default.Schedule, text = "24 Horas")
    }
}

package com.example.myapplication.ui.componentes

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- SEPARADOR DE FECHA PREMIUM (Elite v5.0) ---
 * Utilizado en listas de presupuestos y licitaciones para agrupar por fecha.
 * Permite expandir/colapsar el grupo.
 */
@Composable
fun SeparadorFechaPremium(
    fecha: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF00F0FF) // Cyan Elite

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = RoundedCornerShape(2.dp),
                color = if (isExpanded) accentColor else Color.Gray
            ) {}
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = fecha.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = if (isExpanded) Color.White else Color.Gray
            )
        }

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Colapsar" else "Expandir",
            tint = if (isExpanded) accentColor else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}


































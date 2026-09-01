package com.example.myapplication.ui.pantallas.budget.armador

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.estilos.PBEMTheme
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.uishared.ui.components.TextCompacto

/**
 * --- PIEZAS DEL ARMADOR DE CONCURSO (v2026.ELITE) ---
 * [LEY #10]: Bloques reutilizables para el rompecabezas de la UI.
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun ItemIndicadorPaso(
    titulo: String,
    activo: Boolean,
    completado: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (completado) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (activo || completado) SharedPalette.AcidGreen else Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        TextCompacto(
            text = titulo,
            color = if (activo) SharedPalette.AcidGreen else if (completado) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (activo) FontWeight.Black else FontWeight.Medium
        )
    }
}

@Composable
fun ClausulaSwitch(
    icono: ImageVector,
    etiqueta: String,
    marcado: Boolean,
    alCambiarMarcado: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alCambiarMarcado(!marcado) }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icono, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            TextCompacto(text = etiqueta, color = Color.White, fontSize = 11.sp)
        }
        Switch(
            checked = marcado,
            onCheckedChange = alCambiarMarcado,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SharedPalette.AcidGreen,
                checkedTrackColor = SharedPalette.AcidGreen.copy(alpha = 0.3f)
            ),
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
fun TacticalTextField(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    pista: String = "",
    lineaUnica: Boolean = true,
    modificador: Modifier = Modifier
) {
    Column(modifier = modificador.fillMaxWidth()) {
        TextCompacto(
            text = etiqueta.uppercase(),
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiarValor,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { TextCompacto(text = pista, color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
            leadingIcon = { Icon(icono, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SharedPalette.ElectricCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = SharedPalette.ElectricCyan
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = lineaUnica
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewPiezasArmador() {
    PBEMTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ItemIndicadorPaso(titulo = "Paso Activo", activo = true, completado = false)
            ItemIndicadorPaso(titulo = "Paso Completado", activo = false, completado = true)
            
            ClausulaSwitch(icono = Icons.Default.CheckCircle, etiqueta = "Cláusula de Ejemplo", marcado = true, alCambiarMarcado = {})
            
            TacticalTextField(
                valor = "Texto de ejemplo",
                alCambiarValor = {},
                etiqueta = "Nombre del Proyecto",
                icono = Icons.Default.Title,
                pista = "Ej: Pintura de Fachada"
            )
        }
    }
}

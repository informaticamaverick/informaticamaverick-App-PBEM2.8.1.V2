package com.example.myapplication.ui.componentes.sistema.cabecera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.AppTypography
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- 👤 PIEZA CABECERA: PERFIL ---
 * [PROPÓSITO]: Bloque de identidad del usuario/empresa.
 * [REFACTORED]: Usa el MoldeBurbujaPerfilV3 compartido.
 */

@Composable
fun MoldeCabeceraSuperiorPerfil(
    nombre: String,
    foto: Any?,
    esPersonal: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    estaVerificado: Boolean = false,
    esSuscripto: Boolean = false,
    conteoNoLeidos: Int = 0
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Transparent)
            .clickable { onClick() }
            .fillMaxHeight(), // 🔥 [RAÍZ]: Ocupa toda la altura para balancear
        verticalAlignment = Alignment.CenterVertically 
    ) {
        MoldeBurbujaPerfilV3(
            perfil = PerfilIdentidadV3(
                id = if (esPersonal) "personal" else "entidad",
                nombre = nombre,
                iniciales = nombre.take(2).uppercase(),
                photoUrl = foto,
                colorAcento = SharedPalette.ElectricCyan,
                estaVerificado = estaVerificado,
                esSuscripto = esSuscripto,
                conteoNoLeidos = conteoNoLeidos
            ),
            tamanoBase = 52.dp, 
            mostrarBadges = true
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.Center
        ) {
            TextoEtiquetaCabeceraV3(
                text = if (esPersonal) "BIENVENIDO 👋" else "ENTIDAD ACTIVA 💼"
            )
            
            val nombreFormateado = remember(nombre) {
                val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
                if (partes.size > 1) {
                    "${partes[0]}\n${partes.drop(1).joinToString(" ")}"
                } else nombre
            }

            // Usamos TextCompacto puro para forzar el respeto al \n
            TextCompacto(
                text = nombreFormateado.uppercase(),
                fontSize = 14.sp, 
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = AppTypography.HeaderTitle.copy(
                    letterSpacing = 0.5.sp,
                    lineHeight = 14.sp 
                ),
                maxLines = 2
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewMoldePerfilV3() {
    MoldeCabeceraSuperiorPerfil(
        nombre = "JUAN PEREZ",
        foto = null,
        esPersonal = true,
        onClick = {}
    )
}

package com.example.myapplication.ui.componentes.sistema.cabecera

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.ui.components.TextCompactoAutoFit
import com.example.myapplication.uishared.ui.components.TextCompacto
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.core.dominio.modelos.TipoDireccion
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- 📍 PIEZA CABECERA: UBICACIÓN (PROFESIONAL / AIRY) ---
 * [PROPÓSITO]: Bloque de localización activa con diseño transparente y espacioso.
 * [LEY #11]: Uso de Textos Elásticos para optimizar el espacio interno.
 */
@Composable
fun MoldeCabeceraSuperiorUbicacion(
    direccion: DireccionDominio?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Identificación de origen
    val isGps = direccion?.id == "gps_current"
    val isEmpresa = direccion?.tipo != TipoDireccion.PERFIL_USUARIO

    // 2. Definición de etiquetas
    val modeLabel = when {
        isGps -> "COORD_LIVE"
        isEmpresa -> "HQ_NETWORK"
        else -> "HOME_STATION"
    }

    // 3. Color de acento soberano (LEY #9: Cyan)
    val accentColor = SharedPalette.ElectricCyan

    // 4. Emoji dinámico
    val emojiIcon = when {
        isGps -> "🛰️"
        isEmpresa -> "💼"
        else -> "🏠"
    }

    // 5. Animación de pulso
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGps")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    // 6. Formateo de localidad y CP
    val subTexto = remember(direccion) {
        if (direccion == null) "BUSCANDO..."
        else {
            val cp = if (direccion.codigoPostal.isNotBlank()) " (${direccion.codigoPostal})" else ""
            "${direccion.localidad}$cp".uppercase()
        }
    }

    // CONTENEDOR TRANSPARENTE (PROFESIONAL / AIRY)
    Row(
        modifier = modifier
            .fillMaxHeight() // 🔥 [RAÍZ]: Ocupa todo el alto para balancear
            .clickable { onClick() }
            .padding(horizontal = 4.dp), 
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.Start
    ) {
        // --- CAJA DE EMOJI (TRANSPARENTE CON BORDE SUTIL) ---
        Box(
            modifier = Modifier
                .size(38.dp) 
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.04f)) 
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.12f), 
                    shape = RoundedCornerShape(4.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            TextCompacto(
                text = emojiIcon,
                fontSize = 22.sp 
            )
        }

        Spacer(Modifier.width(8.dp))

        // --- COLUMNA DE DETALLES DE DIRECCIÓN ---
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // LÍNEA 1: INDICADOR RADAR Y ETIQUETA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isGps) accentColor.copy(alpha = dotAlpha) else Color.White.copy(alpha = 0.3f))
                )

                Spacer(Modifier.width(5.dp))

                TextoEtiquetaCabeceraV3(
                    text = modeLabel,
                    color = if (isGps) accentColor else Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(0.dp))

            // LÍNEA 2: CALLE Y NÚMERO (PRINCIPAL)
            TextCompactoAutoFit(
                text = (direccion?.calleYNumero ?: "SIN UBICACIÓN").uppercase(),
                maxFontSize = 10.sp,
                minFontSize = 8.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier.offset(y = (1).dp)
            )

            // LÍNEA 3: LOCALIDAD Y CÓDIGO POSTAL
            TextCompactoAutoFit(
                text = subTexto,
                maxFontSize = 8.sp,
                minFontSize = 6.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier.offset(y = (2).dp)
            )
        }
    }
}

@Preview(name = "PROFESIONAL GPS", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewUbicacionProfesionalGps() {
    val mockGps = DireccionDominio(
        id = "gps_current",
        calle = "Av. San Martín",
        numero = "1420",
        localidad = "San Miguel de Tucumán",
        codigoPostal = "4000"
    )
    Box(modifier = Modifier.padding(10.dp)) {
        MoldeCabeceraSuperiorUbicacion(
            direccion = mockGps,
            onClick = {}
        )
    }
}

@Preview(name = "PROFESIONAL GUARDADA", showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewUbicacionProfesionalGuardada() {
    val mockGuardada = DireccionDominio(
        id = "home_01",
        calle = "Calle Las Heras",
        numero = "340",
        localidad = "Yerba Buena",
        codigoPostal = "4107",
        //esEmpresa = false
    )
    Box(modifier = Modifier.padding(10.dp)) {
        MoldeCabeceraSuperiorUbicacion(
            direccion = mockGuardada,
            onClick = {}
        )
    }
}

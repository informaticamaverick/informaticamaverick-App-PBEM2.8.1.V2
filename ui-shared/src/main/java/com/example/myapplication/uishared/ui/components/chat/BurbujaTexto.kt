package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE TEXTO MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaTexto(
    texto: String,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    nombreRespuesta: String? = null,
    contenidoRespuesta: String? = null,
    alHacerClick: (() -> Unit)? = null,
    alHacerSwipeRespuesta: (() -> Unit)? = null
) {
    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondo,
        colorContenido = colorContenido,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado,
        nombreRespuesta = nombreRespuesta,
        contenidoRespuesta = contenidoRespuesta,
        alHacerClick = alHacerClick,
        alHacerSwipeRespuesta = alHacerSwipeRespuesta
    ) {
        Text(
            text = texto,
            color = colorContenido,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}


































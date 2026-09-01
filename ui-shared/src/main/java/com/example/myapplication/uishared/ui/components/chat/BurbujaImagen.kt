package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

/**
 * --- BURBUJA DE IMAGEN MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaImagen(
    urlImagen: String?,
    texto: String? = null,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    alHacerClick: () -> Unit = {},
    alHacerSwipeRespuesta: (() -> Unit)? = null // 🔥 [NEW]
) {
    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondo,
        colorContenido = colorContenido,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado,
        alHacerClick = alHacerClick,
        alHacerSwipeRespuesta = alHacerSwipeRespuesta,
        margenInterno = PaddingValues(0.dp) // Imagen ocupa todo el espacio
    ) {
        Box {
            AsyncImage(
                model = urlImagen,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 400.dp),
                contentScale = ContentScale.Crop
            )
            
            // Texto si existe
            if (!texto.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(texto, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}


































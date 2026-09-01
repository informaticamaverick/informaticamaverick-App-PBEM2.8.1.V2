package com.example.myapplication.uishared.ui.components.chat

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- BURBUJA DE PRESUPUESTO ESTILO DOCUMENTO (v2026.ELITE) ---
 * PROPÓSITO: Mostrar una previsualización real del PDF al estilo WhatsApp/Telegram.
 */
@Composable
fun BurbujaPresupuestoDocumento(
    titulo: String,
    total: String,
    estado: String,
    esMio: Boolean,
    marcaTiempo: Long,
    colorFondo: Color,
    colorContenido: Color,
    estaLeido: Boolean = false,
    estaEntregado: Boolean = false,
    estaSincronizado: Boolean = true,
    miniaturaBase64: String? = null,
    alVer: () -> Unit = {},
    alGuardar: () -> Unit = {},
    alHacerSwipeRespuesta: (() -> Unit)? = null
) {
    val colorBurbuja = if (esMio) colorFondo else Color(0xFF1F2C34)
    val colorAcento = Color(0xFFF97316)
    
    val miniaturaBitmap = remember(miniaturaBase64) {
        if (miniaturaBase64.isNullOrBlank()) null else {
            try {
                val decodedString = Base64.decode(miniaturaBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        }
    }

    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorBurbuja,
        colorContenido = Color.White,
        estaLeido = estaLeido,
        estaEntregado = estaEntregado,
        estaSincronizado = estaSincronizado,
        alHacerSwipeRespuesta = alHacerSwipeRespuesta,
        margenInterno = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .width(260.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            // --- 🖼️ SECCIÓN 1: MINIATURA (Previsualización) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { alVer() },
                contentAlignment = Alignment.Center
            ) {
                if (miniaturaBitmap != null) {
                    Image(
                        bitmap = miniaturaBitmap,
                        contentDescription = "Vista previa presupuesto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                        Text("PRESUPUESTO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.3f))
                    }
                }
            }

            // --- 📄 SECCIÓN 2: INFO DEL ARCHIVO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE11D48), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("PDF", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titulo, 
                        color = Color.White, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Presupuesto • $total", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }

            // --- ⚡ SECCIÓN 3: ACCIONES ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { alVer() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, null, tint = colorAcento, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("VER", color = colorAcento, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
                Box(modifier = Modifier.width(0.5.dp).fillMaxHeight().background(Color.White.copy(alpha = 0.1f)))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { alGuardar() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("GUARDAR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF808080)
@Composable
private fun PreviewBurbujaPresupuestoDocumento() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BurbujaPresupuestoDocumento(
            titulo = "Renovación de Cocina - Materiales",
            total = "$ 125.000,00",
            estado = "PENDIENTE",
            esMio = true,
            marcaTiempo = System.currentTimeMillis(),
            colorFondo = Color(0xFF005C4B),
            colorContenido = Color.White,
            miniaturaBase64 = null
        )
        
        BurbujaPresupuestoDocumento(
            titulo = "Servicio de Pintura",
            total = "$ 45.000,00",
            estado = "ACEPTADO",
            esMio = false,
            marcaTiempo = System.currentTimeMillis(),
            colorFondo = Color(0xFF1F2C34),
            colorContenido = Color.White,
            miniaturaBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==" // Un pixel rojo
        )
    }
}

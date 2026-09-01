package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- MENU DE ADJUNTOS MAVERICK (V2026.7) ---
 * [ELITE]: Panel universal para adjuntar archivos en el chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuAdjuntos(
    alCerrar: () -> Unit,
    alHacerClickGaleria: () -> Unit,
    alHacerClickCamara: () -> Unit,
    alHacerClickPdf: () -> Unit = {}, 
    alHacerClickUbicacion: () -> Unit = {}, // 🔥 [NEW]
    alHacerClickArchiveroImagenes: () -> Unit = {}, 
    alHacerClickArchiveroPresupuestos: () -> Unit = {}, 
    alHacerClickArchiveroProductos: () -> Unit = {},
    alHacerClickArchiveroTurnos: () -> Unit = {},
    alHacerClickArchiveroVisitas: () -> Unit = {},
    alHacerClickArchiveroUbicaciones: () -> Unit = {},
    colorAcento: Color,
    modifier: Modifier = Modifier,
    mostrarSeccionArchivero: Boolean = true // 🔥 Control de visibilidad
) {
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        modifier = modifier,
        containerColor = Color(0xFF1E293B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            // --- SECCIÓN 1: ADJUNTAR ---
            Text(
                text = "ADJUNTAR ARCHIVO",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                color = colorAcento,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconoAdjunto(Icons.Default.Image, "Galería", Color(0xFF8B5CF6), alHacerClickGaleria)
                IconoAdjunto(Icons.Default.PictureAsPdf, "Documento", Color(0xFFF97316), alHacerClickPdf)
                IconoAdjunto(Icons.Default.CameraAlt, "Cámara", Color(0xFFEF4444), alHacerClickCamara)
                IconoAdjunto(Icons.Default.LocationOn, "Ubicación", Color(0xFF22C55E), alHacerClickUbicacion) // 🔥 [ELITE]
            }
            
            if (mostrarSeccionArchivero) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.05f))

                // --- SECCIÓN 2: ARCHIVERO ---
                Text(
                    text = "ARCHIVERO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                    color = colorAcento,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconoAdjunto(Icons.Default.Collections, "Imágenes", Color(0xFF8B5CF6), alHacerClickArchiveroImagenes)
                    IconoAdjunto(Icons.Default.Description, "Presupuestos", Color(0xFFF97316), alHacerClickArchiveroPresupuestos)
                    IconoAdjunto(Icons.Default.Inventory2, "Productos", Color(0xFFEC4899), alHacerClickArchiveroProductos)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    IconoAdjunto(Icons.Default.EventAvailable, "Turnos", Color(0xFFA855F7), alHacerClickArchiveroTurnos)
                    IconoAdjunto(Icons.Default.HomeRepairService, "Visitas", Color(0xFF22D3EE), alHacerClickArchiveroVisitas)
                    IconoAdjunto(Icons.Default.LocationOn, "Direcciones", Color(0xFF22C55E), alHacerClickArchiveroUbicaciones)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IconoAdjunto(
    icono: ImageVector,
    etiqueta: String,
    color: Color,
    alHacerClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { alHacerClick() }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = color, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}


































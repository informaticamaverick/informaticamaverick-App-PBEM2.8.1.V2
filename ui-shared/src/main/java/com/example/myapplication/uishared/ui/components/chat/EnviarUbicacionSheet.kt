package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.dominio.modelos.DireccionDominio

/**
 * --- HOJA DE ENVÍO DE UBICACIÓN (v2026.ELITE) ---
 * PROPÓSITO: Permitir al prestador seleccionar una dirección (GPS o guardada) para enviar por chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnviarUbicacionSheet(
    direccionesGuardadas: List<DireccionDominio>,
    ubicacionGps: DireccionDominio?,
    alCerrar: () -> Unit,
    alAlternarGps: () -> Unit,
    alSeleccionar: (Double, Double, String) -> Unit
) {
    val colorAcento = Color(0xFF10B981) // Verde Elite
    
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = Color(0xFF0F172A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.1f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "ENVIAR UBICACIÓN",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))

            // GPS Option
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { alAlternarGps() },
                shape = RoundedCornerShape(16.dp),
                color = if (ubicacionGps != null) colorAcento.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                border = BorderStroke(width = 1.dp, color = if (ubicacionGps != null) colorAcento else Color.White.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(if (ubicacionGps != null) colorAcento else Color.Gray.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MyLocation, null, tint = if (ubicacionGps != null) Color.Black else Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ubicación Actual (GPS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (ubicacionGps != null) ubicacionGps.aTextoCompleto() else "Toca para obtener ubicación precisa",
                            color = if (ubicacionGps != null) colorAcento else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    if (ubicacionGps != null) {
                        Button(
                            onClick = { alSeleccionar(ubicacionGps.latitud ?: 0.0, ubicacionGps.longitud ?: 0.0, ubicacionGps.aTextoCompleto()) },
                            colors = ButtonDefaults.buttonColors(containerColor = colorAcento, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("ENVIAR", fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("DIRECCIONES GUARDADAS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(direccionesGuardadas) { dir ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { alSeleccionar(dir.latitud ?: 0.0, dir.longitud ?: 0.0, dir.aTextoCompleto()) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = colorAcento.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(dir.aTextoCompleto(), color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewEnviarUbicacion() {
    EnviarUbicacionSheet(
        direccionesGuardadas = listOf(DireccionDominio(calle = "Serrano", numero = "1100", localidad = "Palermo")),
        ubicacionGps = null,
        alCerrar = {},
        alAlternarGps = {},
        alSeleccionar = { _, _, _ -> }
    )
}

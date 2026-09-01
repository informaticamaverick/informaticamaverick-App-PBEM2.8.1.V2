package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.MensajeEntity

/**
 * --- ARCHIVERO: UBICACIONES (v2026.ELITE) ---
 */
@Composable
fun ArchiveroUbicacionesSheet(
    ubicaciones: List<MensajeEntity>?, // 🔥 null = Cargando
    busqueda: String,
    alCambiarBusqueda: (String) -> Unit,
    alCerrar: () -> Unit,
    alSeleccionar: (MensajeEntity) -> Unit
) {
    val colorAcento = Color(0xFF22C55E) // Verde Elite
    
    ArchiveroMoldeSheet(
        titulo = "Historial de Ubicaciones",
        subtitulo = "Direcciones compartidas previamente",
        busqueda = busqueda,
        alCambiarBusqueda = alCambiarBusqueda,
        alCerrar = alCerrar,
        colorAcento = colorAcento
    ) {
        if (ubicaciones == null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { ItemUbicacionSkeletonMav() }
            }
        } else if (ubicaciones.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes ubicaciones en este chat", color = Color.White.copy(alpha = 0.3f))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ubicaciones) { m ->
                    Surface(
                        onClick = { alSeleccionar(m) },
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).background(colorAcento.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = colorAcento)
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = m.direccionTexto ?: "Ubicación compartida", 
                                color = Color.White, 
                                fontSize = 13.sp, 
                                fontWeight = FontWeight.Medium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

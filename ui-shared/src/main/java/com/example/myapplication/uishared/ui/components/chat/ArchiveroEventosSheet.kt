package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import com.example.myapplication.core.dominio.modelos.EventoDominio

/**
 * --- ARCHIVERO: EVENTOS (v2026.ELITE) ---
 */
@Composable
fun ArchiveroEventosSheet(
    eventos: List<EventoDominio>?, // 🔥 null = Cargando
    busqueda: String,
    alCambiarBusqueda: (String) -> Unit,
    alCerrar: () -> Unit,
    alSeleccionar: (EventoDominio) -> Unit,
    tituloOverride: String? = null,
    subtituloOverride: String? = null
) {
    ArchiveroMoldeSheet(
        titulo = tituloOverride ?: "Historial de Compromisos",
        subtitulo = subtituloOverride ?: "Turnos y Visitas técnicas",
        busqueda = busqueda,
        alCambiarBusqueda = alCambiarBusqueda,
        alCerrar = alCerrar,
        colorAcento = Color(0xFFA855F7) // Púrpura Elite
    ) {
        if (eventos == null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { ItemEventoSkeletonMav() }
            }
        } else if (eventos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes compromisos en este chat", color = Color.White.copy(alpha = 0.3f))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(eventos) { evento ->
                    val colorEvento = Color(evento.colorAcentoHex)
                    Surface(
                        onClick = { alSeleccionar(evento) },
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(50.dp).background(colorEvento.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (evento.tipo == TipoEvento.VISITA_TECNICA) Icons.Default.HomeRepairService else Icons.Default.Event,
                                    null,
                                    tint = colorEvento
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(evento.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${evento.fechaTexto} • ${evento.horaTexto}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

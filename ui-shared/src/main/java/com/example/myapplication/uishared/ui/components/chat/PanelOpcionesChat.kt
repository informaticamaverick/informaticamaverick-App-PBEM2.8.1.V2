package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.*
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
 * --- PANEL DE OPCIONES CHAT MAVERICK (V2026.8) ---
 * [ELITE]: Panel de adjuntos estilo WhatsApp 2026.
 * Aparece debajo de la barra de entrada con animaciones suaves.
 */
@Composable
fun PanelOpcionesChat(
    visible: Boolean,
    alSeleccionarOpcion: (TipoOpcionChat) -> Unit,
    colorAcento: Color = Color(0xFFF97316),
    esModoPrestador: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            if (esModoPrestador) {
                // Prestador: Turnos, Visitas, Productos, Presupuesto, Imágenes, Ubicación, Archivero
                FilaOpciones(
                    opciones = listOf(
                        OpcionConfig(TipoOpcionChat.TURNOS, Icons.Default.CalendarMonth, "Turnos", Color(0xFFA855F7)),
                        OpcionConfig(TipoOpcionChat.VISITAS, Icons.Default.Engineering, "Visitas", Color(0xFF22D3EE)),
                        OpcionConfig(TipoOpcionChat.PRODUCTOS, Icons.Default.Inventory2, "Productos", Color(0xFFFACC15))
                    ),
                    alSeleccionar = alSeleccionarOpcion
                )
                Spacer(modifier = Modifier.height(20.dp))
                FilaOpciones(
                    opciones = listOf(
                        OpcionConfig(TipoOpcionChat.PRESUPUESTO, Icons.Default.Description, "Presupuesto", Color(0xFFF97316)),
                        OpcionConfig(TipoOpcionChat.IMAGENES, Icons.Default.Image, "Imágenes", Color(0xFF8B5CF6)),
                        OpcionConfig(TipoOpcionChat.UBICACION, Icons.Default.LocationOn, "Ubicación", Color(0xFF10B981))
                    ),
                    alSeleccionar = alSeleccionarOpcion
                )
                Spacer(modifier = Modifier.height(20.dp))
                FilaOpciones(
                    opciones = listOf(
                        OpcionConfig(TipoOpcionChat.ARCHIVERO, Icons.Default.FolderZip, "Archivero", Color(0xFF64748B))
                    ),
                    alSeleccionar = alSeleccionarOpcion
                )
            } else {
                // Usuario: Archivero, Imágenes, Ubicación
                FilaOpciones(
                    opciones = listOf(
                        OpcionConfig(TipoOpcionChat.ARCHIVERO, Icons.Default.FolderZip, "Archivero", Color(0xFF64748B)),
                        OpcionConfig(TipoOpcionChat.IMAGENES, Icons.Default.Image, "Imágenes", Color(0xFF8B5CF6)),
                        OpcionConfig(TipoOpcionChat.UBICACION, Icons.Default.LocationOn, "Ubicación", Color(0xFF10B981))
                    ),
                    alSeleccionar = alSeleccionarOpcion
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private data class OpcionConfig(
    val tipo: TipoOpcionChat,
    val icono: ImageVector,
    val etiqueta: String,
    val color: Color
)

@Composable
private fun FilaOpciones(
    opciones: List<OpcionConfig>,
    alSeleccionar: (TipoOpcionChat) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        opciones.forEach { opcion ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                ItemOpcion(
                    icono = opcion.icono,
                    etiqueta = opcion.etiqueta,
                    color = opcion.color,
                    onClick = { alSeleccionar(opcion.tipo) }
                )
            }
        }
        // Rellenar espacios vacíos si la fila no está completa para mantener alineación
        if (opciones.size < 3) {
            repeat(3 - opciones.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ItemOpcion(
    icono: ImageVector,
    etiqueta: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp
            ),
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}

enum class TipoOpcionChat {
    TURNOS, VISITAS, PRODUCTOS, PRESUPUESTO, IMAGENES, UBICACION, ARCHIVERO
}



































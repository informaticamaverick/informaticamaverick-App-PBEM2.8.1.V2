package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.uishared.ui.components.shimmerApp

/**
 * --- ARCHIVERO: IMÁGENES (v2026.ELITE) ---
 * PROPÓSITO: Mostrar el historial de fotos enviadas en una cuadrícula.
 */
@Composable
fun ArchiveroImagenesSheet(
    imagenes: List<MensajeEntity>?,
    alCerrar: () -> Unit,
    alHacerClick: (String) -> Unit
) {
    ArchiveroMoldeSheet(
        titulo = "Historial de Fotos",
        subtitulo = "Imágenes compartidas previamente",
        busqueda = "",
        alCambiarBusqueda = {},
        alCerrar = alCerrar,
        colorAcento = Color(0xFF8B5CF6), // Violeta Galería
        mostrarBuscador = false // 🔥 No es necesario buscar imágenes por texto
    ) {
        if (imagenes == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(12) {
                    Box(modifier = Modifier.aspectRatio(1f).shimmerApp())
                }
            }
        } else if (imagenes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes fotos en este chat", color = Color.White.copy(alpha = 0.3f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(imagenes) { m ->
                    AsyncImage(
                        model = m.urlMedia ?: m.miniaturaBase64,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { alHacerClick(m.urlMedia ?: m.id) },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
private fun PreviewArchiveroImagenes() {
    ArchiveroImagenesSheet(
        imagenes = emptyList(),
        alCerrar = {},
        alHacerClick = {}
    )
}

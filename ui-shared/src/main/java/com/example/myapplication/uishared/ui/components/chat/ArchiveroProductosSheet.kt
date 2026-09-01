package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import java.util.Locale

/**
 * --- ARCHIVERO: PRODUCTOS (v2026.ELITE) ---
 */
@Composable
fun ArchiveroProductosSheet(
    productos: List<MensajeEntity>?, // 🔥 null = Cargando
    busqueda: String,
    alCambiarBusqueda: (String) -> Unit,
    alCerrar: () -> Unit,
    alSeleccionar: (MensajeEntity) -> Unit
) {
    val colorAcento = Color(0xFFEC4899) // Rosa Elite
    
    ArchiveroMoldeSheet(
        titulo = "Historial de Productos",
        subtitulo = "Productos y servicios compartidos",
        busqueda = busqueda,
        alCambiarBusqueda = alCambiarBusqueda,
        alCerrar = alCerrar,
        colorAcento = colorAcento
    ) {
        if (productos == null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(5) { ItemProductoSkeletonMav() }
            }
        } else if (productos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes productos en este chat", color = Color.White.copy(alpha = 0.3f))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productos) { m ->
                    Surface(
                        onClick = { alSeleccionar(m) },
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorAcento.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val miniatura = m.miniaturaBase64
                                val bitmap = remember(miniatura) {
                                    if (miniatura.isNullOrBlank()) null else {
                                        try {
                                            val decoded = Base64.decode(miniatura, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)?.asImageBitmap()
                                        } catch (_: Exception) { null }
                                    }
                                }
                                
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(
                                        imageVector = if (m.subtipoOperativo == "SERVICIO") Icons.Default.Handyman else Icons.Default.Storefront, 
                                        null, 
                                        tint = colorAcento
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.idCategoria?.uppercase() ?: "GENERAL",
                                    color = colorAcento,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = com.example.myapplication.core.utilidades.CompresorProductos.descomprimir(m.contenido)?.nombre ?: m.contenido, 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (m.subtipoOperativo == "SERVICIO") "Servicio Técnico" else "Artículo Comercial", 
                                    color = Color.White.copy(alpha = 0.5f), 
                                    fontSize = 10.sp
                                )
                            }
                            m.precioReferencia?.let { p ->
                                val formatted = String.format(Locale.getDefault(), "%,.2f", p)
                                Text(
                                    text = "$ $formatted",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

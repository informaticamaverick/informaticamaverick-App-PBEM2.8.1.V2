package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.PresupuestoResumenDominio
import com.example.myapplication.core.datos.local.entidades.MensajeEntity
import com.example.myapplication.core.dominio.modelos.EventoDominio
import com.example.myapplication.core.datos.local.entidades.EventoEntity
import com.example.myapplication.core.datos.local.entidades.TipoEvento
import java.util.Locale

/**
 * --- ARCHIVERO DE CHAT (V2026.FINAL) ---
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveroChatSheet(
    alCerrar: () -> Unit,
    listaPresupuestos: List<PresupuestoResumenDominio>?,
    listaImagenes: List<MensajeEntity>?,
    listaDirecciones: List<MensajeEntity>?,
    modifier: Modifier = Modifier,
    listaProductos: List<MensajeEntity>? = null,
    listaTurnos: List<EventoDominio>? = null,
    listaVisitas: List<EventoDominio>? = null,
    alSeleccionarPestana: (Int) -> Unit,
    alHacerClickPresupuesto: (String) -> Unit,
    alHacerClickMultimedia: (String) -> Unit,
    alHacerClickUbicacion: (String) -> Unit,
    alHacerClickProducto: (String) -> Unit = {},
    alHacerClickEvento: (EventoDominio) -> Unit = {},
    colorAcento: Color = Color(0xFFF97316)
) {
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    val titulos = listOf("PRESUPUESTOS", "IMÁGENES", "DIRECCIONES", "PRODUCTOS", "TURNOS", "VISITAS")

    LaunchedEffect(pestanaSeleccionada) {
        alSeleccionarPestana(pestanaSeleccionada)
    }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = Color(0xFF0F172A),
        dragHandle = null,
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ARCHIVERO DEL CHAT",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White
                )
                IconButton(
                    onClick = alCerrar,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            TabRow(
                selectedTabIndex = pestanaSeleccionada,
                containerColor = Color.Transparent,
                contentColor = colorAcento,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pestanaSeleccionada]),
                        color = colorAcento,
                        height = 3.dp
                    )
                },
                divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.05f)) }
            ) {
                titulos.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = pestanaSeleccionada == indice,
                        onClick = { pestanaSeleccionada = indice },
                        text = {
                            Text(
                                text = titulo,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (pestanaSeleccionada == indice) FontWeight.Black else FontWeight.Bold
                                )
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (pestanaSeleccionada) {
                    0 -> {
                        if (listaPresupuestos == null) EsqueletoArchivero(0)
                        else SeccionPresupuestosArchivero(listaPresupuestos, alHacerClickPresupuesto)
                    }
                    1 -> {
                        if (listaImagenes == null) EsqueletoArchivero(1)
                        else SeccionMultimediaArchivero(listaImagenes, alHacerClickMultimedia)
                    }
                    2 -> {
                        if (listaDirecciones == null) EsqueletoArchivero(2)
                        else SeccionUbicacionesArchivero(listaDirecciones, alHacerClickUbicacion)
                    }
                    3 -> {
                        if (listaProductos == null) EsqueletoArchivero(3)
                        else SeccionProductosArchivero(listaProductos, alHacerClickProducto)
                    }
                    4 -> {
                        if (listaTurnos == null) EsqueletoArchivero(4)
                        else SeccionEventosArchivero(
                            listaTurnos, 
                            alHacerClickEvento,
                            "No hay turnos registrados"
                        )
                    }
                    5 -> {
                        if (listaVisitas == null) EsqueletoArchivero(5)
                        else SeccionEventosArchivero(
                            listaVisitas, 
                            alHacerClickEvento,
                            "No hay visitas técnicas registradas"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionProductosArchivero(
    lista: List<MensajeEntity>,
    onClick: (String) -> Unit
) {
    if (lista.isEmpty()) {
        EstadoVacioArchivero("No hay productos compartidos")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista) { m ->
                Surface(
                    onClick = { onClick(m.idReferencia ?: m.id) },
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp).background(Color(0xFFEC4899).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = m.contenido,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = m.idCategoria ?: "Producto",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionPresupuestosArchivero(
    lista: List<PresupuestoResumenDominio>,
    onClick: (String) -> Unit
) {
    if (lista.isEmpty()) {
        EstadoVacioArchivero("No hay presupuestos en este chat")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista) { p ->
                Surface(
                    onClick = { onClick(p.idPresupuesto) },
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFF97316).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📄", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                p.tituloTrabajo ?: "Presupuesto sin título",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Total: $ " + String.format(Locale.getDefault(), "%,.2f", p.totalGeneral),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            p.estado.name,
                            color = Color(0xFFF97316),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionMultimediaArchivero(
    lista: List<MensajeEntity>,
    onClick: (String) -> Unit
) {
    if (lista.isEmpty()) {
        EstadoVacioArchivero("No hay imágenes compartidas")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(lista) { m ->
                AsyncImage(
                    model = m.urlMedia ?: m.miniaturaBase64,
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(1f).clickable { onClick(m.id) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun SeccionUbicacionesArchivero(
    lista: List<MensajeEntity>,
    onClick: (String) -> Unit
) {
    if (lista.isEmpty()) {
        EstadoVacioArchivero("No hay direcciones enviadas")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista) { m ->
                Surface(
                    onClick = { onClick("geo:" + m.latitud + "," + m.longitud + "?q=" + m.direccionTexto) },
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(Color(0xFF10B981).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📍", fontSize = 22.sp)
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

@Composable
private fun SeccionEventosArchivero(
    lista: List<EventoDominio>,
    onClick: (EventoDominio) -> Unit,
    mensajeVacio: String
) {
    if (lista.isEmpty()) {
        EstadoVacioArchivero(mensajeVacio)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista) { evento ->
                val colorEvento = Color(evento.colorAcentoHex)
                Surface(
                    onClick = { onClick(evento) },
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(colorEvento.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = if (evento.tipo == TipoEvento.VISITA_TECNICA) Icons.Default.HomeRepairService else Icons.Default.Event,
                                null,
                                tint = colorEvento,
                                modifier = Modifier.size(20.dp)
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

@Composable
private fun EsqueletoArchivero(tipo: Int) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White.copy(alpha = 0.2f))
    }
}

@Composable
private fun EstadoVacioArchivero(mensaje: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = mensaje,
            color = Color.White.copy(alpha = 0.3f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

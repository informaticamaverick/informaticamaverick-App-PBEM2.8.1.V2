package com.example.myapplication.prestador.ui.pantallas.market

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.PrestadorTheme
import com.example.myapplication.core.datos.local.entidades.ConcursoPublicoEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

/**
 * --- HOJA DE DETALLE DE CONCURSO (v2026.ELITE) ---
 * [PROPÓSITO]: Mostrar toda la información de una licitación para que el prestador decida postularse.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcursoDetalleSheet(
    concurso: ConcursoPublicoEntity,
    estaSuscrito: Boolean = false,
    onDismiss: () -> Unit,
    onPostularse: (String) -> Unit,
    onViewBudget: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    val colores = getPrestadorColors()
    val acento = Color(0xFFF97316)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colores.surfaceColor,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colores.divider) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. CABECERA: TÍTULO Y ESTADO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = concurso.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = colores.textPrimary
                    )
                    Text(
                        text = (concurso.idCategoria ?: "SERVICIO").uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = acento,
                        letterSpacing = 1.sp
                    )
                }
                
                Surface(
                    color = if (concurso.estado == "ABIERTA") Color(0xFF10B981).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = concurso.estado,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (concurso.estado == "ABIERTA") Color(0xFF10B981) else Color.Gray
                    )
                }
            }

            // --- 2. IMÁGENES DEL PROYECTO ---
            if (concurso.urlImagenes.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(concurso.urlImagenes) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Imagen del proyecto",
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colores.surfaceElevated),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // --- 3. DESCRIPCIÓN ---
            Column {
                Text(
                    text = "DESCRIPCIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = colores.textSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = concurso.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.textPrimary,
                    lineHeight = 20.sp
                )
            }

            // --- 4. REQUISITOS TÁCTICOS ---
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (concurso.exigeVisita) ItemRequisito(Icons.Default.Visibility, "Exige Visita")
                if (concurso.exigeGarantia) ItemRequisito(Icons.Default.Security, "Exige Garantía")
                if (concurso.exigeMetodoPago) ItemRequisito(Icons.Default.Payments, "Define Pago")
                if (concurso.exigeDocPrestador) ItemRequisito(Icons.Default.Description, "Exige Docs")
            }

            HorizontalDivider(color = colores.divider)

            // --- 5. DATOS DEL CLIENTE Y UBICACIÓN ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp)) {
                    if (concurso.miniaturaCliente != null) {
                        AsyncImage(
                            model = concurso.miniaturaCliente,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(acento.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(concurso.nombreCliente?.take(1)?.uppercase() ?: "C", fontWeight = FontWeight.Black, color = acento)
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = concurso.nombreCliente ?: "Cliente Maverick",
                        fontWeight = FontWeight.Bold,
                        color = colores.textPrimary
                    )
                    Text(
                        text = "PUBLICADO EL ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(concurso.marcaTiempo))}",
                        fontSize = 10.sp,
                        color = colores.textSecondary
                    )
                }
                TextButton(onClick = { onNavigateToUserProfile(concurso.idCliente) }) {
                    Text("VER PERFIL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = acento)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colores.surfaceElevated,
                border = BorderStroke(1.dp, colores.divider)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        val calle = if (concurso.direccionCalle != null) "${concurso.direccionCalle} ${concurso.direccionNumero ?: ""}" else "Ubicación del Proyecto"
                        Text(text = calle, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = colores.textPrimary)
                        Text(text = "${concurso.direccionLocalidad ?: "Zona"} - CP: ${concurso.direccionCodigoPostal ?: "----"}", fontSize = 10.sp, color = colores.textSecondary)
                    }
                }
            }

            // --- 6. ACCIONES FINALES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colores.divider)
                ) {
                    Text("CANCELAR", color = colores.textSecondary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { 
                        if (estaSuscrito) onPostularse(concurso.idConcurso)
                        else onNavigateToPaywall()
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = acento)
                ) {
                    Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("POSTULARME", fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun ItemRequisito(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    val colores = getPrestadorColors()
    Surface(
        color = colores.surfaceElevated,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colores.divider)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, null, tint = colores.textSecondary, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = texto, fontSize = 10.sp, color = colores.textSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

// ==========================================================================================
// ---------- SECCIÓN: PREVIEWS (V2026.ELITE) ----------------------------------------------
// ==========================================================================================

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
fun ConcursoDetalleSheetPreview() {
    PrestadorTheme(darkTheme = true) {
        ConcursoDetalleSheet(
            concurso = ConcursoPublicoEntity(
                idConcurso = "1",
                titulo = "Reparación de Aire Acondicionado",
                descripcion = "El equipo no enfría y hace un ruido extraño al encender. Necesito presupuesto urgente para el fin de semana.",
                idCategoria = "reparaciones",
                nombreCliente = "Juan Pérez",
                marcaTiempo = System.currentTimeMillis(),
                direccionCalle = "Av. Aconquija",
                direccionNumero = "2000",
                direccionLocalidad = "Yerba Buena",
                direccionCodigoPostal = "4107",
                exigeVisita = true,
                exigeGarantia = true
            ),
            estaSuscrito = true,
            onDismiss = {},
            onPostularse = {}
        )
    }
}


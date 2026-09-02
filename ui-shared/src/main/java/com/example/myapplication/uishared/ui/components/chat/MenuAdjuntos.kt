package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.ui.components.shakeClick

/**
 * --- MENU DE ADJUNTOS MAVERICK (V2026.8) ---
 * [31/08]: Archivero colapsado por defecto (menú chico al abrir) + Archivero
 * reagrupado por sentido, con etiquetas que cambian según el rol (prestador
 * ofrece, cliente recibe) en vez de un mismo texto simetrico para los dos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuAdjuntos(
    alCerrar: () -> Unit,
    alHacerClickGaleria: () -> Unit,
    alHacerClickCamara: () -> Unit,
    alHacerClickPdf: () -> Unit = {},
    alHacerClickUbicacion: () -> Unit = {},
    alHacerClickArchiveroImagenes: () -> Unit = {},
    alHacerClickArchiveroPresupuestos: () -> Unit = {},
    alHacerClickArchiveroProductos: () -> Unit = {},
    alHacerClickArchiveroTurnos: () -> Unit = {},
    alHacerClickArchiveroVisitas: () -> Unit = {},
    alHacerClickArchiveroUbicaciones: () -> Unit = {},
    colorAcento: Color,
    modifier: Modifier = Modifier,
    mostrarSeccionArchivero: Boolean = true,
    esPrestador: Boolean = true,
    nombreContacto: String? = null
) {
    var archiveroExpandido by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        modifier = modifier,
        containerColor = Color(0xFF1E293B),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            // --- SECCION1: ADJUNTAR (siempre visible) ---
            Text(
                text = "ADJUNTAR",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp),
                color = colorAcento,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconoAdjunto(Icons.Default.Image, "Galería", Color(0xFF38BDF8), alHacerClickGaleria)
                IconoAdjunto(Icons.Default.PictureAsPdf, "Documento", Color(0xFFF59E0B), alHacerClickPdf)
                IconoAdjunto(Icons.Default.CameraAlt, "Cámara", colorAcento, alHacerClickCamara, resaltado = true)
                IconoAdjunto(Icons.Default.LocationOn, "Ubicación", Color(0xFF22C55E), alHacerClickUbicacion)
            }

            if (mostrarSeccionArchivero) {
                HorizontalDivider(modifier = Modifier.padding(top = 22.dp, bottom = 8.dp), color = Color.White.copy(alpha = 0.05f))


                // --- DISCLOSURE: el Archivero arranca oculto ---

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { archiveroExpandido = !archiveroExpandido }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (archiveroExpandido) "Ocultar" else "Ver Archivero",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorAcento
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (archiveroExpandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = archiveroExpandido,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {

                        if (!esPrestador && nombreContacto != null) {
                            Text(
                                text = "CON ${nombreContacto.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.6.sp),
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // --- SUBGRUPO 1: enfoque distinto por rol ---
                        SubgrupoArchivero(titulo = if (esPrestador) "MI CATÁLOGO" else "RECIBIDO") {
                            IconoAdjunto(Icons.Default.Description, "Presupuestos", Color(0xFF6366F1), alHacerClickArchiveroPresupuestos)
                            IconoAdjunto(
                                Icons.Default.Inventory2,
                                if (esPrestador) "Productos" else "Catálogo",
                                Color(0xFFEC4899),
                                alHacerClickArchiveroProductos
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- SUBGRUPO 2: enfoque distinto por rol ---
                        SubgrupoArchivero(titulo = if (esPrestador) "GESTIÓN" else "COORDINADO") {
                            IconoAdjunto(
                                Icons.Default.EventAvailable,
                                if (esPrestador) "Turnos" else "Mis turnos",
                                Color(0xFFA78BFA),
                                alHacerClickArchiveroTurnos
                            )
                            IconoAdjunto(Icons.Default.HomeRepairService, "Visitas", Color(0xFF14B8A6), alHacerClickArchiveroVisitas)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // --- SUBGRUPO 3: neutral, igual para los dos roles ---
                        SubgrupoArchivero(titulo = "MULTIMEDIA Y LUGAR") {
                            IconoAdjunto(Icons.Default.Collections, "Imágenes", Color(0xFF84CC16), alHacerClickArchiveroImagenes)
                            IconoAdjunto(Icons.Default.LocationOn, "Direcciones", Color(0xFFF43F5E), alHacerClickArchiveroUbicaciones)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SubgrupoArchivero(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            contenido()
        }
    }
}

@Composable
private fun IconoAdjunto(
    icono: ImageVector,
    etiqueta: String,
    color: Color,
    alHacerClick: () -> Unit,
    resaltado: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp).clickable { alHacerClick() }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = color.copy(alpha = if (resaltado) 0.18f else 0.15f),
            border = BorderStroke(if (resaltado) 1.5.dp else 1.dp, color.copy(alpha = if (resaltado) 1f else 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icono, null, tint = color, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 1
        )
    }
}
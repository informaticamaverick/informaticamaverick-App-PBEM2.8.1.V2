package com.example.myapplication.prestador.ui.pantallas.chat.wizard.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.DireccionDominio
import com.example.myapplication.uishared.estilos.SharedPalette

@Composable
fun PasoIdentidad(
    nombrePrestador: String,
    categoria: String,
    iconoCategoria: String,
    direcciones: List<DireccionDominio>,
    direccionSeleccionada: DireccionDominio?,
    onDireccionSelect: (DireccionDominio) -> Unit,
    nombreCliente: String,
    urlFotoCliente: String?,
    onContinuar: () -> Unit
) {
    val colorAcento = Color(0xFFA855F7) // Púrpura Elite
    var expandedDirecciones by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- SECCIÓN 1: IDENTIDAD DEL PRESTADOR ---
        SeccionWizard(titulo = "QUIÉN ENVÍA LA PROPUESTA", icono = Icons.Default.Business, colorAcento = colorAcento) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(nombrePrestador.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(iconoCategoria, fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(categoria.uppercase(), color = colorAcento, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(Modifier.height(16.dp))

                // Selector de Dirección
                Text("DIRECCIÓN DE ATENCIÓN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                
                Box {
                    Surface(
                        onClick = { if (direcciones.size > 1) expandedDirecciones = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.03f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.LocationOn, null, tint = colorAcento, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = direccionSeleccionada?.let { it.etiqueta.ifBlank { it.aTextoCorto() } } ?: "Sin dirección configurada",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                            if (direcciones.size > 1) {
                                Icon(Icons.Default.ExpandMore, null, tint = Color.Gray)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDirecciones,
                        onDismissRequest = { expandedDirecciones = false },
                        modifier = Modifier.background(SharedPalette.EliteSurface).border(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        direcciones.forEach { dir ->
                            DropdownMenuItem(
                                text = { Text(dir.etiqueta.ifBlank { dir.aTextoCorto() }, color = Color.White) },
                                onClick = { onDireccionSelect(dir); expandedDirecciones = false },
                                leadingIcon = { Icon(Icons.Default.Place, null, tint = colorAcento) }
                            )
                        }
                    }
                }
            }
        }

        // --- SECCIÓN 2: DESTINATARIO (CLIENTE) ---
        SeccionWizard(titulo = "PARA QUIÉN ES EL TURNO", icono = Icons.Default.Person, colorAcento = Color(0xFF3B82F6)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!urlFotoCliente.isNullOrBlank()) {
                    AsyncImage(
                        model = urlFotoCliente,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(0.05f)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(44.dp).background(Color(0xFF3B82F6).copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Text(nombreCliente.take(1).uppercase(), color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(nombreCliente, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("Cliente Final", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinuar,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
        ) {
            Text("CONTINUAR A CONFIGURACIÓN", fontWeight = FontWeight.Black)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
        }
    }
}

@Composable
fun SeccionWizard(
    titulo: String,
    icono: ImageVector,
    colorAcento: Color,
    content: @Composable () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
            Icon(icono, null, tint = colorAcento.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text(titulo, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorAcento.copy(alpha = 0.8f), letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SharedPalette.EliteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
private fun PreviewPasoIdentidad() {
    PasoIdentidad(
        nombrePrestador = "Maverick Elite Services",
        categoria = "Informática (Técnico)",
        iconoCategoria = "💻",
        direcciones = listOf(
            DireccionDominio(etiqueta = "Oficina Central", calle = "Av. Libertador", numero = "1234"),
            DireccionDominio(etiqueta = "Sucursal Belgrano", calle = "Juramento", numero = "2500")
        ),
        direccionSeleccionada = DireccionDominio(etiqueta = "Oficina Central", calle = "Av. Libertador", numero = "1234"),
        onDireccionSelect = {},
        nombreCliente = "Maxi Nanterne",
        urlFotoCliente = null,
        onContinuar = {}
    )
}

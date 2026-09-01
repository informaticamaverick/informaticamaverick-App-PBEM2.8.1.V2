package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.RequestPage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE SOLICITUD DE PRESUPUESTO MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaSolicitudPresupuesto(
    descripcion: String?,
    direccion: String?,
    nombreCliente: String?,
    marcaTiempo: Long,
    esMio: Boolean,
    colorFondo: Color,
    colorContenido: Color,
    alHacerClickAccion: () -> Unit = {}
) {
    BurbujaBase(
        esMio = esMio,
        marcaTiempo = marcaTiempo,
        colorFondo = colorFondo,
        colorContenido = colorContenido
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .padding(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorContenido.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RequestPage,
                        contentDescription = null,
                        tint = colorContenido,
                        modifier = Modifier.padding(4.dp).size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SOLICITUD DE PRESUPUESTO",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = colorContenido
                    )
                    if (!nombreCliente.isNullOrBlank()) {
                        Text(
                            text = nombreCliente,
                            fontSize = 10.sp,
                            color = colorContenido.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = colorContenido.copy(alpha = 0.1f)
            )

            if (!descripcion.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = colorContenido.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = descripcion,
                        fontSize = 13.sp,
                        color = colorContenido,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!direccion.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = colorContenido.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = direccion,
                        fontSize = 12.sp,
                        color = colorContenido.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!esMio) {
                Button(
                    onClick = alHacerClickAccion,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorContenido),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colorFondo
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "CREAR PRESUPUESTO", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 12.sp,
                        color = colorFondo
                    )
                }
            }
        }
    }
}


































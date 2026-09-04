package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3

/**
 * --- BARRA DE CABECERA DE CHAT MAVERICK (V2026.7) ---
 * [ELITE]: Cabecera premium con soporte para estados de identidad.
 * Variables 100% en español.
 */
@Composable
fun BarraCabeceraChat(
    titulo: String,
    urlFoto: Any?,
    estaOnline: Boolean,
    alVolver: () -> Unit,
    colorAcento: Color,
    estaVerificado: Boolean = false,
    fraccionColapso: Float = 0f,
    alHacerClickInfo: () -> Unit = {},
    alHacerClickBuscar: () -> Unit = {},
    alHacerClickOpciones: () -> Unit = {}
) {
    // 🔥 [ELITE] Uso de lerp para transiciones matemáticas perfectas (Zero-Jank)
    val alturaContenido = lerp(80.dp, 64.dp, fraccionColapso) // Altura neta sin status bar
    val tamanoAvatar = lerp(48.dp, 36.dp, fraccionColapso)
    val tamanoTitulo = lerp(18.sp, 15.sp, fraccionColapso)
    val radioEsquina = lerp(0.dp, 28.dp, fraccionColapso)
    
    val alfaFondo = 1f - (0.05f * fraccionColapso) 

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(100f),
        color = Color(0xFF0B0E14).copy(alpha = alfaFondo),
        shape = RoundedCornerShape(bottomStart = radioEsquina, bottomEnd = radioEsquina),
        shadowElevation = lerp(0.dp, 8.dp, fraccionColapso)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // Integración inmersiva perfecta
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(alturaContenido)) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }

                    Spacer(Modifier.width(4.dp))

                    MoldeBurbujaPerfilV3(
                        perfil = PerfilIdentidadV3(
                            id = "remoto",
                            nombre = titulo,
                            iniciales = titulo.take(2).uppercase(),
                            photoUrl = urlFoto,
                            estaEnLinea = estaOnline,
                            estaVerificado = estaVerificado
                        ),
                        tamanoBase = tamanoAvatar,
                        modifier = Modifier.clickable { alHacerClickInfo() }
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .clickable { alHacerClickInfo() }
                    ) {
                        Text(
                            text = titulo.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = tamanoTitulo,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (estaOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4ADE80))
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = if (estaOnline) "EN LÍNEA" else "DESCONECTADO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (estaOnline) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = alHacerClickBuscar) {
                            Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = alHacerClickOpciones) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

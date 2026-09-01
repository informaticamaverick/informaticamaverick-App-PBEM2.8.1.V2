package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.ui.components.MoldeMultiSeleccion
import com.example.myapplication.uishared.ui.components.profile.MoldeBurbujaPerfilV3
import com.example.myapplication.uishared.ui.components.profile.PerfilIdentidadV3
import java.text.SimpleDateFormat
import java.util.*

/**
 * --- ELEMENTO DE LISTA DE CHAT MAVERICK (V2026.ELITE) ---
 * [ELITE SSOT]: Componente unificado para la bandeja de entrada (Inbox).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ElementoListaChat(
    idChat: String,
    nombreVisible: String,
    ultimoMensaje: String,
    marcaTiempo: Long,
    urlFoto: String?,
    urlMiniatura: String?,
    estaOnline: Boolean,
    estaVerificado: Boolean,
    conteoNoLeidos: Int,
    accionesPendientes: Int,
    estaSeleccionado: Boolean,
    modoMultiseleccion: Boolean,
    alHacerClick: () -> Unit,
    alHacerLongClick: () -> Unit,
    alHacerClickAvatar: () -> Unit,
    colorAcento: Color
) {
    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    val textoHora = remember(marcaTiempo) { formatoHora.format(Date(marcaTiempo)) }

    MoldeMultiSeleccion(
        estaSeleccionado = estaSeleccionado,
        modoMultiseleccionActivo = modoMultiseleccion,
        colorAcento = colorAcento,
        radioCurvatura = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = alHacerClick, 
                    onLongClick = alHacerLongClick
                ),
            color = Color.Transparent 
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- AVATAR CON ESTADOS TÁCTICOS ---
                MoldeBurbujaPerfilV3(
                    perfil = PerfilIdentidadV3(
                        id = idChat,
                        nombre = nombreVisible,
                        iniciales = nombreVisible.take(2).uppercase(),
                        photoUrl = ImageUtils.processImageSource(urlMiniatura ?: urlFoto),
                        estaEnLinea = estaOnline,
                        estaVerificado = estaVerificado,
                        conteoNoLeidos = 0 
                    ),
                    tamanoBase = 56.dp,
                    modifier = Modifier.clickable { alHacerClickAvatar() }
                )

                Spacer(Modifier.width(16.dp))

                // --- INFO CENTRAL ---
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nombreVisible.uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = ultimoMensaje,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = if (conteoNoLeidos > 0) Color.White.copy(alpha = 0.9f) else Color.Gray,
                            fontWeight = if (conteoNoLeidos > 0) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // --- INFO TIEMPO Y BADGES ---
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight().padding(vertical = 2.dp)
                ) {
                    Text(
                        text = textoHora,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = if (conteoNoLeidos > 0) colorAcento else Color.Gray,
                            fontWeight = if (conteoNoLeidos > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    
                    if (accionesPendientes > 0 || conteoNoLeidos > 0) {
                        Row(
                            modifier = Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (accionesPendientes > 0) {
                                Surface(
                                    color = Color(0xFF22D3EE),
                                    shape = CircleShape,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            if (conteoNoLeidos > 0) {
                                Surface(
                                    color = colorAcento,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = conteoNoLeidos.toString(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = TextStyle(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EsqueletoListaChat() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.05f)))
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.03f)))
        }
    }
}

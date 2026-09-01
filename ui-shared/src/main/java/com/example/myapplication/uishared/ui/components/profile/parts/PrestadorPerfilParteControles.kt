package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.core.dominio.modelos.PrestadorDominio

/**
 * --- CONTROLES Y FAB DEL PERFIL (Ley #10) ---
 * [PROPÓSITO]: Gestionar el selector de soberanía y acciones tácticas de sincronización.
 */

@Composable
fun FabInteraccionPerfil(
    paginaActual: Int,
    empresas: List<PrestadorDominio>,
    urlFotoPersonal: Any?,
    esMiPropioPerfil: Boolean,
    hayCambiosPendientes: Boolean,
    alSeleccionarPagina: (Int) -> Unit,
    alClickConfig: () -> Unit,
    alClickSync: () -> Unit,
    alClickAnadirEmpresa: () -> Unit,
    alClickChat: (String?) -> Unit
) {
    val colorAcento = Color(0xFF3B82F6)
    
    if (esMiPropioPerfil) {
        Surface(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .height(70.dp)
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(35.dp),
            color = Color(0xFF1A1A24).copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    BurbujaPerfilMini(
                        fotoUrl = urlFotoPersonal,
                        estaSeleccionado = paginaActual == 0,
                        onClick = { alSeleccionarPagina(0) }
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    empresas.forEachIndexed { index, empresa ->
                        BurbujaPerfilMini(
                            fotoUrl = empresa.urlFoto,
                            estaSeleccionado = paginaActual == index + 1,
                            onClick = { alSeleccionarPagina(index + 1) }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    
                    if (empresas.size < 3) {
                        IconButton(
                            onClick = alClickAnadirEmpresa,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, null, tint = colorAcento, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = alClickSync) {
                        BadgedBox(badge = { if(hayCambiosPendientes) Badge(containerColor = Color.Red) }) {
                            val colorNube = if(hayCambiosPendientes) Color(0xFF22D3EE) else Color.Gray // Electric Cyan
                            Icon(Icons.Default.CloudUpload, null, tint = colorNube)
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = alClickConfig,
                        containerColor = colorAcento,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                }
            }
        }
    } else {
        FloatingActionButton(
            onClick = { alClickChat(null) },
            containerColor = colorAcento,
            modifier = Modifier.padding(16.dp).navigationBarsPadding()
        ) {
            Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White)
        }
    }
}

@Composable
fun BurbujaPerfilMini(
    fotoUrl: Any?,
    estaSeleccionado: Boolean,
    onClick: () -> Unit
) {
    val escala by animateFloatAsState(if (estaSeleccionado) 1.2f else 1f, label = "escala")
    val colorBorde by animateColorAsState(
        if (estaSeleccionado) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.15f),
        label = "colorBorde"
    )

    Surface(
        modifier = Modifier
            .size(40.dp)
            .scale(escala)
            .clip(CircleShape)
            .clickable { onClick() },
        color = Color.Black,
        border = androidx.compose.foundation.BorderStroke(if (estaSeleccionado) 2.dp else 1.dp, colorBorde),
        shape = CircleShape
    ) {
        AsyncImage(
            model = fotoUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = android.R.drawable.ic_menu_gallery)
        )
    }
}


































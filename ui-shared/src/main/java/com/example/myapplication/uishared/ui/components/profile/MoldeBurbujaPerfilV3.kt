package com.example.myapplication.uishared.ui.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- 🛡️ MOLDE BURBUJA DE PERFIL TÁCTICA V3 ---
 * [PROPÓSITO]: Identidad visual unificada para el ecosistema Maverick (Cliente y Prestador).
 * [LEY #10]: Diseño de alto impacto con badges de estado (Online, Verificado, Suscripto).
 * [LEY #11]: Escala automática proporcional al tamaño base.
 */

data class PerfilIdentidadV3(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val photoUrl: Any? = null,
    val emoji: String? = null,
    val colorAcento: Color = SharedPalette.ElectricCyan,
    val estaEnLinea: Boolean = false,
    val estaVerificado: Boolean = false,
    val esSuscripto: Boolean = false,
    val conteoNoLeidos: Int = 0
)

@Composable
fun MoldeBurbujaPerfilV3(
    modifier: Modifier = Modifier,
    perfil: PerfilIdentidadV3,
    tamanoBase: Dp = 48.dp,
    mostrarBadges: Boolean = true,
    estaSeleccionado: Boolean = false
) {
    // --- CÁLCULOS DE ESCALA (Ley #11) ---
    val factorEscala = tamanoBase.value / 48f
    val bordeAncho = (if (perfil.esSuscripto || estaSeleccionado) 1.5f else 0.8f) * factorEscala
    val borderColor = when {
        estaSeleccionado -> perfil.colorAcento
        perfil.esSuscripto -> perfil.colorAcento.copy(alpha = 0.8f)
        else -> Color.White.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .size(tamanoBase)
            .graphicsLayer {
                if (estaSeleccionado) {
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // --- 1. CÍRCULO PRINCIPAL DEL AVATAR ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(0xFF1C1F2B))
                .border(bordeAncho.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val context = LocalContext.current
            
            // Simulación de procesamiento de imagen (Fidelidad con el original)
            if (perfil.photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(perfil.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else if (perfil.emoji != null) {
                Text(
                    text = perfil.emoji, 
                    fontSize = (18 * factorEscala).sp
                )
            } else {
                Text(
                    text = perfil.iniciales.uppercase(),
                    fontSize = (14 * factorEscala).sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        if (mostrarBadges) {
            // --- 2. DOCK LATERAL IZQUIERDO (Notificaciones y Online) ---
            
            // ARRIBA IZQUIERDA: CONTEO NO LEÍDOS
            if (perfil.conteoNoLeidos > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = ((-2) * factorEscala).dp, y = 0.dp)
                        .size((14 * factorEscala).dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .border((1.2 * factorEscala).dp, Color.Black, CircleShape)
                        .zIndex(3f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (perfil.conteoNoLeidos > 9) "+" else perfil.conteoNoLeidos.toString(),
                        fontSize = (8 * factorEscala).sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // MEDIO IZQUIERDA: PUNTO ONLINE
            if (perfil.estaEnLinea) {
                val topOffset = if (perfil.conteoNoLeidos > 0) (tamanoBase * 0.32f) else (tamanoBase * 0.10f)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = ((-3) * factorEscala).dp, y = topOffset)
                        .size((10 * factorEscala).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border((1.2 * factorEscala).dp, Color.Black, CircleShape)
                        .zIndex(2f)
                )
            }

            // --- 3. ETIQUETA INFERIOR UNIFICADA [ PRO ] ---
            if (perfil.esSuscripto) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (4 * factorEscala).dp)
                        .zIndex(4f),
                    shape = RoundedCornerShape((3 * factorEscala).dp),
                    color = perfil.colorAcento,
                    border = BorderStroke((1 * factorEscala).dp, Color.Black)
                ) {
                    Text(
                        text = "PRO",
                        style = TextStyle(
                            fontSize = (8 * factorEscala).sp, 
                            fontWeight = FontWeight.Black, 
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(horizontal = (5 * factorEscala).dp, vertical = (1 * factorEscala).dp)
                    )
                }
            }

            // --- 4. ICONO DE VERIFICADO (Bottom End Overlay) ---
            if (perfil.estaVerificado) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = "Verificado",
                    tint = SharedPalette.ElectricCyan, 
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (3 * factorEscala).dp, y = (3 * factorEscala).dp)
                        .size((18 * factorEscala).dp)
                        .background(SharedPalette.CyberBackground, CircleShape) 
                        .border((1 * factorEscala).dp, Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding((1 * factorEscala).dp)
                        .zIndex(5f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewBurbujaTacticaV3() {
    val mockPerfil = PerfilIdentidadV3(
        id = "1",
        nombre = "Maverick Hunter",
        iniciales = "MH",
        estaEnLinea = true,
        estaVerificado = true,
        esSuscripto = true,
        conteoNoLeidos = 5
    )

    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Diferentes estados
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MoldeBurbujaPerfilV3(perfil = mockPerfil, tamanoBase = 48.dp)
            MoldeBurbujaPerfilV3(perfil = mockPerfil.copy(esSuscripto = false), tamanoBase = 48.dp)
            MoldeBurbujaPerfilV3(perfil = mockPerfil.copy(estaEnLinea = false, conteoNoLeidos = 0), tamanoBase = 48.dp)
        }
        
        // Diferentes tamaños (Elasticidad)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Bottom) {
            MoldeBurbujaPerfilV3(perfil = mockPerfil, tamanoBase = 64.dp)
            MoldeBurbujaPerfilV3(perfil = mockPerfil, tamanoBase = 40.dp)
            MoldeBurbujaPerfilV3(perfil = mockPerfil, tamanoBase = 24.dp)
        }
    }
}

package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * --- CABECERA LIMPIA DEL PERFIL DE USUARIO (Elite v2026) ---
 * [PROPÓSITO]: Identidad visual para clientes, sin métricas profesionales.
 */

@Composable
fun CabeceraUsuarioPerfilMav(
    altura: Dp,
    fraccionColapso: Float,
    fotoUrl: Any?,
    miniaturaUrl: Any? = null,
    titulo: String,
    subtitulo: String = "Cliente Maverick",
    estaVerificado: Boolean = false,
    estaOnline: Boolean,
    esMiPropioPerfil: Boolean,
    estaSuscrito: Boolean = false,
    alVolver: () -> Unit,
    alEditarAvatar: () -> Unit,
    alCerrarSesion: () -> Unit,
    enModoEdicion: Boolean = false,
    distintivoPremium: @Composable () -> Unit = {}
) {
    val colorFondo = Color(0xFF0F0F0F)
    val colorAcento = Color(0xFF3B82F6) // Azul Maverick
    val colorNaranja = Color(0xFFF97316)
    
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    
    // --- LÓGICA DE TRANSFORMACIÓN (Morphing de Élite) ---
    val widthImagen = lerp(screenWidth.value, 42f, fraccionColapso).dp
    val heightImagen = lerp(altura.value, 42f, fraccionColapso).dp
    val radioEsquina = lerp(0f, 50f, fraccionColapso).dp
    
    // Margen de la imagen al colapsar (para dejar espacio a la flecha)
    val paddingStartImagen = lerp(0f, 56f, fraccionColapso).dp
    val paddingBottomImagen = lerp(0f, 8f, fraccionColapso).dp
    
    val alphaTextosExtra = (1f - fraccionColapso * 3f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(altura)
            .background(if (fraccionColapso > 0.9f) colorFondo else Color.Transparent)
            .zIndex(10f)
    ) {
        // --- 1. CAPA DE PROFUNDIDAD (Fondo con Blur fijo) ---
        val finalFoto = fotoUrl ?: miniaturaUrl
        if (finalFoto != null) {
            AsyncImage(
                model = finalFoto,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f * (1f - fraccionColapso))
                    .blur(25.dp),
                contentScale = ContentScale.Crop
            )
        }

    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = paddingStartImagen, bottom = paddingBottomImagen)
            .size(width = widthImagen, height = heightImagen)
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(radioEsquina)
                shadowElevation = if(fraccionColapso > 0.8f) 6f else 0f
            }
            .clickable(enabled = esMiPropioPerfil && enModoEdicion, onClick = alEditarAvatar)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(finalFoto)
                .crossfade(true)
                .build(),
            contentDescription = "Foto de Perfil",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop 
        )
    }

        // --- 3. SCRIMS DE PROTECCIÓN (Para textos inferiores) ---
        if (alphaTextosExtra > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alphaTextosExtra)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, colorFondo.copy(alpha = 0.85f))
                        )
                    )
            )
        }

        // --- 4. IDENTIDAD (Nombre, Status y Lápiz) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = lerp(20f, 106f, fraccionColapso).dp, 
                    bottom = lerp(40f, 12f, fraccionColapso).dp, // Menos bottom que el prestador por falta de métricas
                    end = 8.dp 
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subtitulo.uppercase(),
                    color = colorAcento,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(alphaTextosExtra)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titulo,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = lerp(24f, 17f, fraccionColapso).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (estaVerificado) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, null, tint = if(estaSuscrito) Color(0xFFFFD700) else colorAcento, modifier = Modifier.size(16.dp))
                    }
                }
                
                if (alphaTextosExtra > 0f) {
                    Text(
                        text = if (estaOnline) "en línea" else "últ. vez hace poco",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        modifier = Modifier.alpha(alphaTextosExtra)
                    )
                }
            }

            // BOTÓN LÁPIZ ELITE
            if (esMiPropioPerfil && enModoEdicion && alphaTextosExtra > 0.2f) {
                IconButton(
                    onClick = alEditarAvatar,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Editar Foto",
                        tint = colorNaranja, 
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // --- 5. TOOLBAR FIXED (Controles Superiores) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, start = 4.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = alVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            
            if (esMiPropioPerfil) {
                if (alphaTextosExtra > 0.4f) {
                    distintivoPremium()
                } else {
                    IconButton(onClick = alCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}


































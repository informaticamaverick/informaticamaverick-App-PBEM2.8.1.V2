package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
 * --- CABECERA DINÁMICA DEL PERFIL (Ley #10 - MORPHING TELEGRAM ELITE) ---
 * [PROPÓSITO]: Lograr una transición física donde la imagen principal se convierte en el avatar del toolbar.
 * [DISEÑO]: Se elimina la duplicidad visual. Solo existe UNA imagen de perfil.
 */

@Composable
fun CabeceraPerfilDinamica(
    altura: Dp,
    fraccionColapso: Float,
    fotoUrl: Any?,
    miniaturaUrl: Any? = null,
    titulo: String,
    subtitulo: String,
    calificacion: Float,
    estaVerificado: Boolean,
    estaOnline: Boolean,
    esMiPropioPerfil: Boolean,
    trabajosRealizados: Int,
    totalResenas: Int,
    estaSuscrito: Boolean,
    alVolver: () -> Unit,
    alEditarAvatar: () -> Unit,
    alCerrarSesion: () -> Unit,
    alVerReseñas: () -> Unit = {},
    distintivoPremium: @Composable () -> Unit = {}
) {
    val colorFondo = Color(0xFF0F0F0F)
    val colorAcento = Color(0xFFFF7043)
    val colorNaranja = Color(0xFFF97316)
    
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    
    // --- LÓGICA DE TRANSFORMACIÓN (Morphing de Élite) ---
    // [v2026.REDISEÑO]: el avatar ahora es circular desde el estado expandido (antes era la
    // foto full-bleed del ancho de pantalla) — solo cambia de tamaño al colapsar, no de forma.
    val avatarSizeExpandido = 88f
    val widthImagen = lerp(avatarSizeExpandido, 42f, fraccionColapso).dp
    val heightImagen = lerp(avatarSizeExpandido, 42f, fraccionColapso).dp
    val radioEsquina = 50.dp

    // Margen de la imagen al colapsar (para dejar espacio a la flecha)
    // [FIX]: 78f dejaba el avatar pisando la toolbar (flecha atrás) en el estado expandido —
    // subido a 90f para que quede debajo de la toolbar y arriba de las métricas.
    val paddingStartImagen = lerp(20f, 56f, fraccionColapso).dp
    val paddingBottomImagen = lerp(90f, 8f, fraccionColapso).dp
    
    val alphaTextosExtra = (1f - fraccionColapso * 3f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(altura)
            .background(if (fraccionColapso > 0.9f) colorFondo else Color.Transparent)
            .zIndex(10f)
    ) {
        // --- 1. CAPA DE PROFUNDIDAD (Fondo con Blur fijo) ---
        // Permanece de fondo para suavizar la transición
        val finalFoto = fotoUrl ?: miniaturaUrl
        if (finalFoto != null) {
            AsyncImage(
                model = finalFoto,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f)
                    .blur(25.dp),
                contentScale = ContentScale.Crop
            )
        }

    // --- 2. IMAGEN PRINCIPAL (MOTOR DEL MORPHING) ---
    // Esta es la imagen "Real" de Room que se convierte en círculo
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = paddingStartImagen, bottom = paddingBottomImagen)
            .size(width = widthImagen, height = heightImagen)
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(radioEsquina)
                // Elevación solo cuando es círculo para destacar en el Toolbar
                shadowElevation = if(fraccionColapso > 0.8f) 6f else 0f
            }
            // [FIX]: antes, sin foto cargada, el círculo quedaba transparente e invisible —
            // pasaba desapercibido cuando el avatar era el hero gigante, pero ahora que es un
            // círculo chico y definido la ausencia se nota. Fondo + ícono de respaldo.
            .background(Color(0xFF2A2A35))
            .clickable(enabled = esMiPropioPerfil, onClick = alEditarAvatar)
    ) {
        if (finalFoto != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(finalFoto) // Fallback a miniatura si la foto real falla (ej: ruta local inválida)
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de Perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "Foto de Perfil",
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxSize(0.55f).align(Alignment.Center)
            )
        }
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

        // --- 4. IDENTIDAD Y CONTROLES (Nombre, Status y Lápiz) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = lerp(124f, 106f, fraccionColapso).dp,
                    bottom = lerp(120f, 12f, fraccionColapso).dp,
                    end = 8.dp // Menos margen a la derecha para el lápiz
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

            // BOTÓN LÁPIZ ELITE (Solo el icono, como en las tarjetas)
            if (esMiPropioPerfil && alphaTextosExtra > 0.2f) {
                IconButton(
                    onClick = alEditarAvatar,
                    modifier = Modifier.size(48.dp) // Área de toque generosa
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
            
            // Botón Premium (Solo en modo extendido para evitar aplastamiento)
            if (esMiPropioPerfil && alphaTextosExtra > 0.4f) {
                distintivoPremium()
            } else {
                Spacer(Modifier.width(1.dp))
            }
        }

        // --- 6. MÉTRICAS GLASS (Solo abajo) ---
        if (alphaTextosExtra > 0.1f) {
            FilaMetricasGlass(
                trabajos = trabajosRealizados,
                rating = calificacion,
                resenas = totalResenas,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .alpha(alphaTextosExtra),
                onComentariosClick = alVerReseñas
            )
        }
    }
}

@Composable
fun FilaMetricasGlass(
    trabajos: Int,
    rating: Float,
    resenas: Int,
    modifier: Modifier = Modifier,
    onComentariosClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BotonMetricaGlass(
            valor = trabajos.toString(),
            etiqueta = "Trabajos",
            icono = Icons.Default.Handyman,
            modifier = Modifier.weight(1f)
        )
        BotonMetricaGlass(
            valor = "%.1f".format(rating),
            etiqueta = "Ranking",
            icono = Icons.Default.Star,
            colorIcono = Color(0xFFFFD700),
            modifier = Modifier.weight(1f)
        )
        BotonMetricaGlass(
            valor = resenas.toString(),
            etiqueta = "Comentarios",
            icono = Icons.AutoMirrored.Filled.Comment,
            modifier = Modifier.weight(1f),
            onClick = onComentariosClick
        )
    }
}

@Composable
private fun BotonMetricaGlass(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    colorIcono: Color = Color.White.copy(alpha = 0.7f),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icono, null, tint = colorIcono, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(6.dp))
                Text(valor, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            Text(etiqueta.uppercase(), color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return (1 - fraction) * start + fraction * stop
}


































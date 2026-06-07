package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.PrestadorColors

/**
 * --- COMPONENTE DE UI: ProfileBannerHeader ---
 * Implementa el header colapsable con efecto de banner y avatar.
 * Siguiendo el Maverick Elite Protocol v3.5 (SSOT en :core, sin banners).
 */

@Composable
internal fun ProfileBannerHeaderView(
    name: String,
    profesion: String,
    imageUrl: String?,
    rating: Float,
    isSubscribed: Boolean,
    isVerified: Boolean,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    companyAvatars: List<Pair<String?, () -> Unit>> = emptyList(),
    colors: PrestadorColors,
    isEditMode: Boolean = false,
    onEditPhoto: () -> Unit = {},
    onEditBanner: () -> Unit = {}, // Mantenido por compatibilidad pero no se usa
    collapseFraction: Float = 0f,
    avatarSizeDp: Dp = 90.dp
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundColor) // Fondo sólido sin banner
    ) {
        // Botón volver (esquina superior izquierda)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = colors.textPrimary.copy(alpha = 0.1f) // Color adaptado sin banner
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Avatar + Nombre + Profesión (centrado)
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp), // Ajustado para nuevo header sin banner
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de Perfil
            Box(
                modifier = Modifier
                    .size(avatarSizeDp)
                    .clip(CircleShape)
                    .border(3.dp, colors.primaryOrange, CircleShape)
                    .then(if (isEditMode) Modifier.clickable { onEditPhoto() } else Modifier)
            ) {
                ProfilePhoto(imageUrl = imageUrl, colors = colors, isCompany = false)
                
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // Datos del Prestador (se desvanecen al colapsar)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        alpha = (1f - collapseFraction * 2.5f).coerceIn(0f, 1f)
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isVerified) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verificado",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                if (profesion.isNotBlank()) {
                    Text(
                        text = profesion,
                        fontSize = 14.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                    Text(
                        text = " $rating",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (isSubscribed) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = colors.primaryOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "PREMIUM",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.primaryOrange
                            )
                        }
                    }
                }
            }
        }

        // TOGGLE AVATARS (Otras empresas) - Esquina inferior derecha
        if (companyAvatars.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                companyAvatars.forEach { (avatarUrl, onClick) ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable { onClick() }
                    ) {
                        ProfilePhoto(imageUrl = avatarUrl, colors = colors, isCompany = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePhoto(
    imageUrl: String?,
    colors: PrestadorColors,
    isCompany: Boolean
) {
    if (!imageUrl.isNullOrEmpty()) {
        val model: Any = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            remember(imageUrl) {
                try {
                    val decodedBytes = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ?: imageUrl
                } catch (e: Exception) {
                    imageUrl
                }
            }
        }
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.primaryOrange.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompany) Icons.Default.Business else Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colors.primaryOrange.copy(alpha = 0.6f)
            )
        }
    }
}


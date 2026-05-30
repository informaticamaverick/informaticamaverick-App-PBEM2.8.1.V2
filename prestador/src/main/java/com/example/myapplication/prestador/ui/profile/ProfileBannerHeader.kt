package com.example.myapplication.prestador.ui.profile

import android.R.attr.label
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.PrestadorColors

// ── HEADER CON BANNER ────────────────────────────────────────────────────────
@Composable
internal fun ProfileBannerHeaderView(
    name: String,
    profesion: String,
    imageUrl: String?,
    bannerImageUrl: String?,
    rating: Float,
    isSubscribed: Boolean,
    isVerified: Boolean,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    companyAvatars: List<Pair<String?, () -> Unit>> = emptyList(),
    colors: PrestadorColors,
    isEditMode: Boolean = false,
    onEditPhoto: () -> Unit = {},
    onEditBanner: () -> Unit = {},
    collapseFraction: Float = 0f,
    avatarSizeDp: Dp = 90.dp
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // Banner (ocupa todo el header)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isEditMode) Modifier.clickable { onEditBanner() } else Modifier)
        ) {
            val bannerModel: Any? = bannerImageUrl?.takeIf { it.isNotEmpty() }
            when {
                bannerModel != null && (bannerModel as String).startsWith("http") -> {
                    AsyncImage(
                        model = bannerModel,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                bannerModel != null -> {
                    val bmp = remember(bannerModel) {
                        try {
                            val b = android.util.Base64.decode(bannerModel as String, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bmp != null) {
                        Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        BannerGradient(colors)
                    }
                }
                else -> BannerGradient(colors)
            }
            // Fade inferior
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, colors.backgroundColor.copy(alpha = 0.85f)),
                        startY = 100f
                    )
                )
            )
            // Botón volver (solo back, sin lápiz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(36.dp), CircleShape, Color.Black.copy(alpha = 0.35f)) {
                    IconButton(onClick = onBack, Modifier.fillMaxSize()) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Avatar + Nombre + Profesión + Badges (centrado verticalmente en el header)
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSizeDp)
                    .clip(CircleShape)
                    .border(3.dp, colors.primaryOrange, CircleShape)
                    .then(if (isEditMode) Modifier.clickable { onEditPhoto() } else Modifier)
            ) {
                ProfilePhoto(imageUrl = imageUrl, colors = colors, isCompany = true)
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { alpha = (1f - collapseFraction * 2f).coerceIn(0f, 1f) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        name.ifEmpty { "Prestador" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isVerified) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, null, Modifier.size(18.dp), Color(0xFF10B981))
                    }
                }
                if (profesion.isNotEmpty()) {
                    Text(profesion, fontSize = 13.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.surfaceColor,
                        border = BorderStroke(1.dp, colors.textSecondary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, Modifier.size(12.dp), Color(0xFFFBBF24))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                String.format("%.1f", if (rating == 0f) 5.0f else rating),
                                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary
                            )
                        }
                    }
                    if (isSubscribed) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFBBF24).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.WorkspacePremium, null, Modifier.size(12.dp), Color(0xFFFBBF24))
                                Spacer(Modifier.width(3.dp))
                                Text("Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                            }
                        }
                    }
                }
            }
        }

        // Botones toggle empresa (bottom-end) — lista de avatares
        if (companyAvatars.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy((-12).dp)
            ) {
                companyAvatars.forEach { (avatarUrl, onClicck) ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.82F else 1f,
                        animationSpec = spring(dampingRatio = 0.4f, stiffness = 500f),
                        label = "avatarScale"
                    )
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .background(colors.surfaceColor)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onClicck() }
                    ) {
                        ProfilePhoto(imageUrl = avatarUrl, colors = colors, isCompany = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerGradient(colors: PrestadorColors) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(
                    colors.primaryOrange.copy(alpha = 0.8f),
                    Color(0xFFFF5722).copy(alpha = 0.5f),
                    colors.backgroundColor
                )
            )
        )
    )
}

@Composable
internal fun ProfilePhoto(imageUrl: String?, colors: PrestadorColors, isCompany: Boolean = false, name: String = "") {
    when {
        !imageUrl.isNullOrEmpty() && imageUrl.startsWith("http") -> {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        !imageUrl.isNullOrEmpty() -> {
            val bmp = remember(imageUrl) {
                try {
                    val b = android.util.Base64.decode(imageUrl, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
                } catch (e: Exception) { null }
            }
            if (bmp != null) {
                Image(bmp, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                PhotoPlaceholder(colors, isCompany, name)
            }
        }
        else -> PhotoPlaceholder(colors, isCompany)
    }
}

@Composable
private fun PhotoPlaceholder(colors: PrestadorColors, isCompany: Boolean = false, name: String = "") {
    val initials = name.trim().split("")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    Box(
        Modifier.fillMaxSize().background(colors.primaryOrange.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (initials.isNotEmpty() && !isCompany) {
            Text(
                text = initials,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryOrange
            )
        } else {
            Icon(
                if (isCompany) Icons.Default.Business else Icons.Default.Person,
                null,
                Modifier.size(28.dp),
                tint = colors.primaryOrange.copy(alpha = 0.5f)
            )
        }
    }
}

package com.example.myapplication.prestador.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

/**
 * Popup que aparece anclado en la esquina superior derecha (debajo del avatar)
 * con animación de escala desde esa esquina. No usa Dialog — es un overlay
 * posicionado dentro del Box raíz de InicioScreen.
 */
@Composable
fun AvatarProfilePopup(
    visible: Boolean,
    nombrePrestador: String,
    email: String,
    imageBase64: String?,
    onClose: () -> Unit,
    onEditProfile: () -> Unit
) {
    val colors = getPrestadorColors()

    // Scrim semitransparente para cerrar al tocar fuera
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClose() }
        )
    }

    // Card posicionada en la esquina superior derecha, casi pegada al avatar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 68.dp, end = 8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = tween(200),
                transformOrigin = TransformOrigin(1f, 0f)   // crece desde esquina sup-der
            ) + fadeIn(tween(200)),
            exit = scaleOut(
                animationSpec = tween(150),
                transformOrigin = TransformOrigin(1f, 0f)
            ) + fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .wrapContentHeight()
                    .shadow(12.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surfaceColor)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {}   // evita que el clic cierre el popup
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── HEADER CON GRADIENTE ──────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color(0xFFFF7043),
                                        0.5f to Color(0xFFFF9E80),
                                        1.0f to Color(0xFFFFCCBC)
                                    )
                                )
                            )
                            .padding(top = 16.dp, bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar clickeable
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .shadow(6.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(
                                    3.dp,
                                    Brush.sweepGradient(listOf(Color(0xFFFF7043), Color(0xFFFFAB40), Color(0xFFFF7043))),
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { onEditProfile(); onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarImageView(
                                imageBase64 = imageBase64,
                                nombre = nombrePrestador,
                                sizeDp = 64
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // ── NOMBRE ────────────────────────────────────────
                    Text(
                        text = nombrePrestador.ifBlank { "Prestador" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    if (email.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = email,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // ── BOTÓN EDITAR PERFIL ────────────────────────────
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 12.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF7043), Color(0xFFFF9E80))
                                )
                            )
                            .clickable { onEditProfile(); onClose() }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(
                                "Editar perfil",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarImageView(imageBase64: String?, nombre: String, sizeDp: Int) {
    val bitmap = remember(imageBase64) {
        if (imageBase64 != null && !imageBase64.startsWith("http")) {
            try {
                val bytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else null
    }
    when {
        bitmap != null -> Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(sizeDp.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        imageBase64 != null && imageBase64.startsWith("http") -> AsyncImage(
            model = imageBase64,
            contentDescription = null,
            modifier = Modifier.size(sizeDp.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        else -> Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFCCBC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.firstOrNull()?.uppercase() ?: "P",
                fontSize = (sizeDp / 2.2).sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D2000)
            )
        }
    }
}


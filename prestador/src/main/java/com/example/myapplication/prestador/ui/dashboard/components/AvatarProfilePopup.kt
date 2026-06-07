package com.example.myapplication.prestador.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

@Composable
fun AvatarProfilePopup(
    visible: Boolean,
    nombrePrestador: String,
    email: String,
    imageBase64: String?,
    profileCompletion: Int,
    isVerified: Boolean,
    onClose: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = getPrestadorColors()
    var mostrarDialogLogout by remember { mutableStateOf(false) }

    //Animación de la barra de progreso
    val animatedProgress by animateFloatAsState(
        targetValue = if (visible) profileCompletion / 100f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200),
        label = "profileProgress"
    )

    //Scrim para cerrar al tocar fuera
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
                transformOrigin = TransformOrigin(1f, 0f)
            ) + fadeIn(tween(200)),
            exit = scaleOut(
                animationSpec = tween(150),
                transformOrigin = TransformOrigin(1f, 0f)
            ) + fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .wrapContentHeight()
                    .shadow(16.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceColor)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {}
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    //Header con gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color(0xFFFF5722),
                                        0.6f to Color(0xFFFF7043),
                                        1.0f to Color(0xFFFF9E80)
                                    )
                                )
                            )
                            .padding(top = 20.dp, bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        //Avatar
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(8.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(
                                    3.dp,
                                    Brush.sweepGradient(
                                        listOf(Color(0xFFFF7043), Color(0xFFFFAB40), Color(0xFFFF7043))
                                    ),
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .clickable { onEditProfile(); onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarImageView(
                                imageBase64 = imageBase64,
                                nombre = nombrePrestador,
                                sizeDp = 72
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    //NOMBRE + BADGE VERIFICADO
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = nombrePrestador.ifBlank { "Prestador" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerified) {
                            Spacer(Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verificado",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (email.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = email,
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    //Barra de completitud
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Completitud del perfil",
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$profileCompletion%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    profileCompletion >= 80 -> Color(0xFF4CAF50)
                                    profileCompletion >= 50 -> Color(0xFFFF9800)
                                    else -> Color(0xFFFF5722)
                                }
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        //Barra de fondo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            //Barra animada de progreso
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(listOf(Color(0xFFFF5722), Color(0xFFFFAB40)))
                                    )
                            )
                        }

                        if (profileCompletion < 100) {
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = "Completá tu perfil para mayor visivilidad",
                                fontSize = 10.sp,
                                color = colors.textSecondary.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    //Botón editar perfil
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9E80)))
                            )
                            .clickable { onEditProfile(); onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Editar perfil",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    //Botón cerrar sesión
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable { mostrarDialogLogout = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Cerrar sesión",
                                color = Color(0xFFE53935),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
    //Dialog Confirmar Logout
    if (mostrarDialogLogout) {
        AlertDialog(
            onDismissRequest = { mostrarDialogLogout = false },
            containerColor = colors.surfaceColor,
            icon = { Icon(Icons.Default.Logout, null, tint = Color(0xFFE53935)) },
            title = { Text("vas a salir de tu cuenta en este dispositivo.", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { mostrarDialogLogout = false; onClose(); onLogout() }) {
                    Text("Cerrar sesión", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogLogout = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
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
        else -> Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFCCBC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nombre.firstOrNull()?.uppercase() ?: "p",
                fontSize = (sizeDp / 2.2).sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D2000)
            )
        }
    }
}
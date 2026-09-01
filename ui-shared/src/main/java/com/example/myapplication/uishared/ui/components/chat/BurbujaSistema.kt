package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE SISTEMA MAVERICK (TOAST TÁCTICO v2026.FINAL) ---
 * [PROPÓSITO]: Avisos automáticos que no forman parte de la conversación humana.
 * [ESTÉTICA]: Look inmersivo "Grandes Ligas" (Telegram/WhatsApp Style).
 */
@Composable
fun BurbujaSistema(
    texto: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    icon: ImageVector? = null,
    backgroundColor: Color = Color(0xFF0F172A).copy(alpha = 0.85f), // Más oscuro y táctico
    textColor: Color = Color.White.copy(alpha = 0.9f),
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(24.dp) // Más redondeado, estilo cápsula

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 40.dp), // Más margen para centrar visualmente
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 2.dp, shape = shape)
                .background(color = backgroundColor, shape = shape)
                .border(width = 0.8.dp, color = borderColor, shape = shape)
                .clip(shape)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = androidx.compose.material3.ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                            role = Role.Button,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClick()
                            }
                        )
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!emoji.isNullSensorOrEmpty()) {
                Text(
                    text = emoji!!,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 6.dp).size(14.dp)
                )
            }

            Text(
                text = texto,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.3.sp)
            )
        }
    }
}

private fun String?.isNullSensorOrEmpty(): Boolean = this.isNullOrEmpty()
/*
package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- BURBUJA DE SISTEMA MAVERICK (V2026.7) ---
 */
@Composable
fun BurbujaSistema(
    texto: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = texto,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

*/
































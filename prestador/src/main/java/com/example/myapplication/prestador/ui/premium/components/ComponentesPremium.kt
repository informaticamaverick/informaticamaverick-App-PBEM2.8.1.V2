package com.example.myapplication.prestador.ui.premium.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- COMPONENTES VISUALES PREMIUM (ESTILO CAMBA) ---
 * [LEY #9]: Estándar Mav en Español.
 */

@Composable
fun EtiquetaEliteMaestra() {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_alpha"
    )

    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)

    Surface(
        color = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            (1.5 * borderAlpha).dp, 
            Brush.linearGradient(listOf(gold, darkGold))
        ),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WorkspacePremium, 
                contentDescription = null, 
                tint = gold, 
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "MIEMBRO ELITE", 
                color = gold, 
                fontWeight = FontWeight.Black, 
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun BotonGoEliteTactico(onClick: () -> Unit) {
    val maverickOrange = Color(0xFFF97316)
    val maverickYellow = Color(0xFFFBBD23)

    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(listOf(maverickOrange, maverickYellow))
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Stars, 
                contentDescription = null, 
                tint = Color.White, 
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "GO ELITE", 
                color = Color.White, 
                fontWeight = FontWeight.Black, 
                fontSize = 12.sp
            )
        }
    }
}














































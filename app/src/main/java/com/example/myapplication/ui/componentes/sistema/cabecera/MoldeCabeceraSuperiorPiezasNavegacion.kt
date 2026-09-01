package com.example.myapplication.ui.componentes.sistema.cabecera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- 🚀 PIEZAS DE NAVEGACIÓN Y DECORACIÓN V3 ---
 * [PROPÓSITO]: Átomos funcionales para pantallas internas (Back, Emojis).
 */

@Composable
fun BotonBackCabeceraV3(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CutCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), CutCornerShape(6.dp))
            .clickable { 
                android.util.Log.d("MAV_NAV", "🎯 [BACK_BUTTON] Click detectado en BotonBackCabeceraV3")
                onClick() 
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Emoji de Alto Impacto (Dinámico)
 * [LEY #11]: Escala masiva para identidad de sección.
 */
@Composable
fun EmojiImpactoV3(
    emoji: String,
    fraccionColapso: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size((64 - (24 * fraccionColapso)).dp) // 🔥 [ELITE]: De 64dp a 40dp (Más presencia)
            .graphicsLayer {
                // Rotación sutil y escalado dinámico
                rotationZ = -8f * fraccionColapso
                val escalaExtra = 1f + (fraccionColapso * 0.1f)
                scaleX = escalaExtra
                scaleY = escalaExtra
                
                // Centrado dinámico al colapsar
                translationY = (4f * fraccionColapso)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (52 - (18 * fraccionColapso)).sp // 🔥 [MÁS GRANDE]: De 52sp a 34sp
        )
    }
}

@Composable
fun AtmoEmojiSeccionV3(
    emoji: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.03f))
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050508)
@Composable
fun PreviewPiezasNavegacionV3() {
    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonBackCabeceraV3(onClick = {})
            AtmoEmojiSeccionV3(emoji = "📋")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiImpactoV3(emoji = "💰", fraccionColapso = 0f)
            Spacer(Modifier.width(20.dp))
            EmojiImpactoV3(emoji = "💰", fraccionColapso = 1f)
        }
    }
}

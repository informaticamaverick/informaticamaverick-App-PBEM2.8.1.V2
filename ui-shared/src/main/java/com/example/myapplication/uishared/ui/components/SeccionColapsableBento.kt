package com.example.myapplication.uishared.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.uishared.estilos.SharedPalette

/**
 * --- SECCIÓN COLAPSABLE BENTO (v2026.ELITE) ---
 * [PROPÓSITO]: Componente de alta jerarquía visual para agrupar formularios.
 */
@Composable
fun SeccionColapsableBento(
    titulo: String,
    subtitulo: String? = null,
    icono: ImageVector,
    colorAcento: Color = Color(0xFFF97316),
    inicialmenteExpandida: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var expandida by remember { mutableStateOf(inicialmenteExpandida) }
    val rotacion by animateFloatAsState(if (expandida) 180f else 0f, label = "rotacion")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = if (expandida) colorAcento.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- CABECERA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandida = !expandida }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(colorAcento.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, null, tint = colorAcento, modifier = Modifier.size(24.dp))
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titulo.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    if (subtitulo != null) {
                        Text(
                            text = subtitulo,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.rotate(rotacion)
                )
            }

            // --- CONTENIDO ---
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    // Divisor sutil
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                    Spacer(Modifier.height(20.dp))
                    content()
                }
            }
        }
    }
}

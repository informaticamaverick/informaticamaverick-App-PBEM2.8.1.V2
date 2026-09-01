package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.uishared.ui.components.shimmerApp

/**
 * --- SHIMMER PERFIL ELITE (v2026.FINAL) ---
 * [PROPÓSITO]: Placeholder de carga de alta fidelidad para perfiles de usuario y prestador.
 * [LEY #10]: Estética inmersiva coincidente con el Morphing Header.
 */
@Composable
fun ShimmerPerfilElite() {
    val colorBase = Color(0xFF020408)
    val colorSkeleton = Color.White.copy(alpha = 0.05f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBase)
    ) {
        // 1. Cabecera Gigante (Placeholder de Imagen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .shimmerApp()
        )

        // 2. Sección de Información
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nombre y Título
            Box(modifier = Modifier.size(200.dp, 24.dp).clip(RoundedCornerShape(4.dp)).background(colorSkeleton))
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.size(120.dp, 14.dp).clip(RoundedCornerShape(4.dp)).background(colorSkeleton))
            
            Spacer(Modifier.height(32.dp))

            // Fila de Métricas (Glass Placeholder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colorSkeleton)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Secciones de Cuerpo
            repeat(3) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Box(modifier = Modifier.size(100.dp, 12.dp).clip(RoundedCornerShape(2.dp)).background(colorSkeleton))
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(8.dp)).background(colorSkeleton))
                }
            }
        }
    }
}

package com.example.myapplication.uishared.ui.components.profile.parts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * --- BLOQUEO DE MODO EMPRESA (Soberanía) ---
 * [PROPÓSITO]: Ocultar el perfil personal cuando la empresa tiene el mando.
 */
@Composable
fun CompanyModeLockedOverlay(
    onVolver: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F).copy(alpha = 0.85f))
            .zIndex(20f)
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Surface(
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(28.dp))
            
            Text(
                text = "MODO EMPRESA ACTIVO",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = "Tu identidad corporativa es ahora la preferida para búsquedas y contactos. Tu perfil personal está oculto para garantizar la soberanía de marca.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            
            Spacer(Modifier.height(40.dp))
            
            Surface(
                onClick = onVolver,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "VOLVER AL PERFIL PERSONAL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}


































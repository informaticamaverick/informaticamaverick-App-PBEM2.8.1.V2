package com.example.myapplication.prestador.ui.pantallas.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

private const val APP_VERSION = "1.0.0"

@Composable
fun AcercaDeScreen(onBack: () -> Unit = {}) {
    val colors = getPrestadorColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = colors.textPrimary)
            }
            Icon(Icons.Default.Info, contentDescription = null, tint = colors.primaryOrange, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Acerca de",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        HorizontalDivider(color = colors.divider)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo / nombre
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.primaryOrange),
                modifier = Modifier.size(80.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("M", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("app Prestador", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text("Versión $APP_VERSION", fontSize = 14.sp, color = colors.textSecondary)
            }

            HorizontalDivider(color = colors.divider)

            // Info del equipo
            AcercaDeSection(label = "Equipo de desarrollo") {
                AcercaDeRow(icon = Icons.Default.Code, iconColor = Color(0xFF1976D2), label = "Desarrollado por", value = "app Developers")
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = colors.divider.copy(alpha = 0.5f))
                AcercaDeRow(icon = Icons.Default.Email, iconColor = Color(0xFFE53935), label = "Contacto", value = "soporte@app.app")
            }

            // Info técnica
            AcercaDeSection(label = "Información técnica") {
                AcercaDeRow(icon = Icons.Default.Info, iconColor = Color(0xFF00796B), label = "Versión", value = APP_VERSION)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "© 2025 app Developers. Todos los derechos reservados.",
                fontSize = 11.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AcercaDeSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = getPrestadorColors()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary, modifier = Modifier.padding(horizontal = 4.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun AcercaDeRow(icon: ImageVector, iconColor: Color, label: String, value: String) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = colors.textSecondary)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
        }
    }
}

















































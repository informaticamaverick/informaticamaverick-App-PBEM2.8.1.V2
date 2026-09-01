package com.example.myapplication.ui.componentes.sistema.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.utilidades.ImageUtils
import com.example.myapplication.uishared.estilos.SharedPalette
import com.example.myapplication.ui.estilos.ClienteTheme

/**
 * --- SELECTOR DE REMITENTE (INTELIGENTE) ---
 * [ELITE]: Permite al usuario elegir bajo qué identidad enviar un mensaje.
 */

data class SenderProfile(
    val id: String, // "personal" or companyId
    val branchId: String? = null,
    val name: String,
    val subName: String? = null,
    val photoUrl: Any? = null
)

@Composable
fun SenderSelectionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    profiles: List<SenderProfile>,
    onProfileSelected: (SenderProfile) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(220.dp)
            .background(SharedPalette.V2TechSurface)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    ) {
        Text(
            text = "ENVIAR MENSAJE COMO:",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = SharedPalette.ElectricCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        )
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        
        profiles.forEach { profile ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.05f)
                        ) {
                            val processedPhoto = ImageUtils.processImageSource(profile.photoUrl)
                            if (processedPhoto != null) {
                                AsyncImage(
                                    model = processedPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = profile.name.uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (profile.subName != null) {
                                Text(
                                    text = profile.subName,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                onClick = { 
                    onProfileSelected(profile)
                    onDismissRequest()
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF05070A)
@Composable
fun PreviewSenderSelectionMenu() {
    val profiles = listOf(
        SenderProfile("personal", null, "Mi Perfil Personal", "Max Power"),
        SenderProfile("c1", "b1", "Sucursal Centro", "Maverick Tech")
    )
    ClienteTheme {
        Box(modifier = Modifier.padding(20.dp)) {
            SenderSelectionMenu(
                expanded = true,
                onDismissRequest = {},
                profiles = profiles,
                onProfileSelected = {}
            )
        }
    }
}

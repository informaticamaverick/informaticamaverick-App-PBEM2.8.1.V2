package com.example.myapplication.uishared.ui.components.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * --- MOLDE BASE PARA EL ARCHIVERO (v2026.ELITE) ---
 * PROPÓSITO: Proporcionar una estructura consistente para buscar en el historial del chat.
 * LEY #9: Nomenclatura en español.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveroMoldeSheet(
    titulo: String,
    subtitulo: String,
    busqueda: String,
    alCambiarBusqueda: (String) -> Unit,
    alCerrar: () -> Unit,
    colorAcento: Color,
    modifier: Modifier = Modifier,
    mostrarBuscador: Boolean = true, // 🔥 [NEW v2026]
    contenido: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = alCerrar,
        containerColor = Color(0xFF0F172A),
        dragHandle = null,
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- CABECERA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = titulo.uppercase(), 
                        color = Color.White, 
                        fontWeight = FontWeight.Black, 
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = subtitulo.uppercase(), 
                        color = colorAcento, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                IconButton(
                    onClick = alCerrar,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            // --- BUSCADOR ---
            if (mostrarBuscador) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = alCambiarBusqueda,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    placeholder = { Text("Buscar en el historial...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = colorAcento, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorAcento,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        cursorColor = colorAcento
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- CONTENIDO (Lista) ---
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    this@Column.contenido()
                }
            }
        }
    }
}

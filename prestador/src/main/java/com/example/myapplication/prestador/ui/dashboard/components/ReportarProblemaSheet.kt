package com.example.myapplication.prestador.ui.dashboard.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.theme.getPrestadorColors

private val categorias = listOf("Presupuestos", "Turnos", "Mi cuenta", "Catálogo", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarProblemaSheet(onDismiss: () -> Unit) {
    val colors = getPrestadorColors()
    val accent = colors.primaryOrange
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var categoriaSeleccionada by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var enviado by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imagenUri = uri }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.textSecondary.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            if (enviado) {
                // Estado de éxito
                EnviadoExitoContent(accentColor = accent, onDismiss = onDismiss)
            } else {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BugReport, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Reportar un problema", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text("Nos ayuda a mejorar la app", fontSize = 12.sp, color = colors.textSecondary)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Categoría
                Text("¿Con qué area tenés el problema?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(Modifier.height(10.dp))
                CategoriaChips(
                    categorias = categorias,
                    seleccionada = categoriaSeleccionada,
                    accentColor = accent,
                    onSeleccionar = { categoriaSeleccionada = it }
                )

                Spacer(Modifier.height(20.dp))

                // Descripción
                Text("Descripción del problema *", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { if (it.length <= 500) descripcion = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    placeholder = { Text("Describí lo que pasó, qué estabas haciendo y qué esperabas que ocurriera...", fontSize = 13.sp, color = colors.textSecondary) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = colors.textSecondary.copy(alpha = 0.3f),
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = accent,
                        focusedContainerColor = colors.backgroundColor.copy(alpha = 0.4f),
                        unfocusedContainerColor = colors.backgroundColor.copy(alpha = 0.4f)
                    ),
                    maxLines = 6
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("${descripcion.length}/500", fontSize = 11.sp, color = colors.textSecondary)
                }

                Spacer(Modifier.height(20.dp))

                // Captura de pantalla opcional
                Text("Captura de pantalla (opcional)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Spacer(Modifier.height(8.dp))

                if (imagenUri != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = imagenUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { imagenUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.backgroundColor.copy(alpha = 0.6f))
                            .border(1.dp, colors.textSecondary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                            .clickable {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = colors.textSecondary, modifier = Modifier.size(28.dp))
                            Text("Adjuntar imagen", fontSize = 12.sp, color = colors.textSecondary)
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Botón enviar
                Button(
                    onClick = {
                        enviando = true
                        enviado = true
                        enviando = false
                    },
                    enabled = descripcion.isNotBlank() && !enviando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        disabledContainerColor = accent.copy(alpha = 0.4f)
                    )
                ) {
                    if (enviando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar reporte", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaChips(
    categorias: List<String>,
    seleccionada: String,
    accentColor: Color,
    onSeleccionar: (String) -> Unit
) {
    val colors = getPrestadorColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categorias.take(3).forEach { cat ->
            val isSelected = cat == seleccionada
            FilterChip(
                selected = isSelected,
                onClick = { onSeleccionar(cat) },
                label = { Text(cat, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = colors.backgroundColor,
                    labelColor = colors.textPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = colors.textSecondary.copy(alpha = 0.3f),
                    selectedBorderColor = accentColor
                )
            )
        }
    }
    Spacer(Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categorias.drop(3).forEach { cat ->
            val isSelected = cat == seleccionada
            FilterChip(
                selected = isSelected,
                onClick = { onSeleccionar(cat) },
                label = { Text(cat, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = Color.White,
                    containerColor = colors.backgroundColor,
                    labelColor = colors.textPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = colors.textSecondary.copy(alpha = 0.3f),
                    selectedBorderColor = accentColor
                )
            )
        }
    }
}

@Composable
private fun EnviadoExitoContent(accentColor: Color, onDismiss: () -> Unit) {
    val colors = getPrestadorColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = accentColor, modifier = Modifier.size(48.dp))
        }
        Text("¡Reporte enviado!", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
        Text(
            "Gracias por ayudarnos a mejorar.\nNuestro equipo revisará tu reporte pronto.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Listo", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

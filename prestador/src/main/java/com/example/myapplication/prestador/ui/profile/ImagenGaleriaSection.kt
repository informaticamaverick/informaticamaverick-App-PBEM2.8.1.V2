package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.prestador.ui.profile.sections.ArchiveroSection
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.util.UUID

@Composable
fun ImagenGaleriaSection(
    imagenesJson: String,
    empresaId: String,
    onImagenesActualizadas: (String) -> Unit,
    expanded: Boolean = false,
    onExpandChange: () -> Unit = {}
) {
    val colors = getPrestadorColors()
    val scope = rememberCoroutineScope()

    var imagenes by remember(imagenesJson) {
        mutableStateOf(jsonArrayToList(imagenesJson))
    }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isUploading = true
                val nuevaUrl = subirImagenAStorage(selectedUri, empresaId)
                if (nuevaUrl != null) {
                    val nuevaLista = imagenes + nuevaUrl
                    imagenes = nuevaLista
                    onImagenesActualizadas(listToJsonArray(nuevaLista))
                }
                isUploading = false
            }
        }
    }

    ArchiveroSection(
        title = "Galería de imágenes",
        sectionId = "gallery",
        icon = Icons.Default.Photo,
        color = colors.primaryOrange,
        modifier = Modifier.padding(horizontal = 16.dp),
        expanded = expanded,
        onExpandChange = { onExpandChange() }
    ) {
        // Botón agregar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${imagenes.size} foto${if (imagenes.size != 1) "s" else ""}",
                fontSize = 13.sp,
                color = colors.textSecondary
            )
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = colors.primaryOrange,
                    strokeWidth = 2.dp
                )
            } else {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.primaryOrange
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primaryOrange),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar foto", fontSize = 13.sp)
                }
            }
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        if (imagenes.isEmpty()) {
            Text(
                text = "No hay fotos en la galería. Agregá imágenes de tus trabajos.",
                fontSize = 13.sp,
                color = colors.textSecondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                imagenes.forEach { url ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Imagen de galería",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        colors.primaryOrange.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Foto de trabajo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = url.substringAfterLast("/").take(30),
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            IconButton(
                                onClick = {
                                    val nuevaLista = imagenes.filter { img -> img != url }
                                    imagenes = nuevaLista
                                    onImagenesActualizadas(listToJsonArray(nuevaLista))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar imagen",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun jsonArrayToList(json: String): List<String> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { arr.getString(it) }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun listToJsonArray(list: List<String>): String {
    val arr = JSONArray()
    list.forEach { arr.put(it) }
    return arr.toString()
}

private suspend fun subirImagenAStorage(uri: Uri, empresaId: String): String? {
    return try {
        val ref = FirebaseStorage.getInstance()
            .reference
            .child("empresas/$empresaId/galeria/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        ref.downloadUrl.await().toString()
    } catch (e: Exception) {
        null
    }
}

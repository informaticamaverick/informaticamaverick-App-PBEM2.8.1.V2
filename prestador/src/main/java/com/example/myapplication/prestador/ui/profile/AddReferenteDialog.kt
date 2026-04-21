package com.example.myapplication.prestador.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.myapplication.prestador.data.local.entity.ReferenteEntity
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField

@Composable
fun AddReferenteDialog(
    referente: ReferenteEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, apellido: String, cargo: String, imageUri: Uri?) -> Unit
) {
    val colors = getPrestadorColors()
    val isEditMode = referente != null

    var nombre by remember { mutableStateOf(referente?.nombre ?: "") }
    var apellido by remember { mutableStateOf(referente?.apellido ?: "") }
    var cargo by remember { mutableStateOf(referente?.cargo ?: "") }
    var nombreError by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedImageUri = uri }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surfaceColor
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Editar Referente" else "Agregar Referente",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar picker
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.primaryOrange.copy(alpha = 0.1f))
                            .border(2.dp, colors.primaryOrange, CircleShape)
                            .clickable { imageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto del referente",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            referente?.imageUrl != null -> AsyncImage(
                                model = referente.imageUrl,
                                contentDescription = "Foto del referente",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            else -> Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primaryOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                FloatingLabelTextField(
                    value = nombre,
                    onValueChange = { nombre = it; nombreError = false },
                    label = "Nombre *",
                    leadingIcon = Icons.Default.Person
                )
                if (nombreError) {
                    Text("El nombre es requerido", color = Color.Red, fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                FloatingLabelTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = "Apellido",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(16.dp))

                FloatingLabelTextField(
                    value = cargo,
                    onValueChange = { cargo = it },
                    label = "Cargo (ej: Encargado, Gerente)",
                    leadingIcon = Icons.Default.Work
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            if (nombre.isBlank()) { nombreError = true; return@Button }
                            onConfirm(nombre.trim(), apellido.trim(), cargo.trim(), selectedImageUri)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange)
                    ) { Text(if (isEditMode) "Actualizar" else "Agregar") }
                }
            }
        }
    }
}
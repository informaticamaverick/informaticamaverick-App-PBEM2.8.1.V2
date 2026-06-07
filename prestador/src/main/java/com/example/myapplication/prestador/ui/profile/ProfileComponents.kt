package com.example.myapplication.prestador.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.prestador.ui.theme.PrestadorColors
import java.util.UUID

@Composable
fun ProfileSectionCard(
    icon: ImageVector,
    title: String,
    iconColor: Color,
    colors: PrestadorColors,
    actionButton: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(iconColor.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                if (actionButton != null) {
                    actionButton()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresaBottomSheet(
    colors: PrestadorColors,
    onDismiss: () -> Unit,
    onAceptar: (CompanyProvider) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var cuit by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorNombre by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF8B5CF6),
        unfocusedBorderColor = colors.border,
        focusedLabelColor = Color(0xFF8B5CF6),
        unfocusedLabelColor = colors.textSecondary,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        cursorColor = Color(0xFF8B5CF6)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Business, null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(22.dp))
                Text("Nueva Empresa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; errorNombre = null },
                label = { Text("Nombre Comercial *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                isError = errorNombre != null,
                supportingText = errorNombre?.let { { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 10.sp) } }
            )
            OutlinedTextField(
                value = razonSocial,
                onValueChange = { razonSocial = it },
                label = { Text("Razón Social") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = cuit,
                onValueChange = { cuit = it.filter { c -> c.isDigit() || c == '-' }.take(13) },
                label = { Text("CUIT") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Corporativo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            errorNombre = "El nombre es obligatorio"
                        } else {
                            onAceptar(
                                CompanyProvider(
                                    id = UUID.randomUUID().toString(),
                                    name = nombre.trim(),
                                    razonSocial = razonSocial.trim(),
                                    cuit = cuit.trim(),
                                    email = email.trim()
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) { Text("Crear Empresa") }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    emoji: String,
    label: String,
    value: String,
    colors: PrestadorColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = colors.textPrimary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun SavedToast(colors: PrestadorColors) {
    Surface(
        modifier = Modifier.zIndex(99f),
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceElevated,
        shadowElevation = 12.dp
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text("Cambios guardados", fontWeight = FontWeight.Bold, color = colors.textPrimary)
        }
    }
}

@Composable
fun DiscardChangesDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Descartar cambios?") },
        text = { Text("Tenés cambios sin guardar. ¿Deseas descartarlos?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Descartar", color = Color.Red) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Seguir editando") } }
    )
}

@Composable
fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar sesión") },
        text = { Text("¿Seguro que deseas salir?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Salir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun PriorizarEmpresaDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Perfil de empresa") },
        text = { Text("¿Deseas mostrar la empresa como tu perfil principal?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Sí") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("No por ahora") } }
    )
}

package com.example.myapplication.prestador.ui.profile.dialogs

import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import com.example.myapplication.prestador.data.model.ServiceType
import com.example.myapplication.prestador.ui.register.components.FloatingLabelTextField
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.example.myapplication.prestador.viewmodel.DireccionUiState
import com.example.myapplication.prestador.viewmodel.DireccionViewModel
import com.example.myapplication.prestador.viewmodel.EditProfileViewModel
import com.example.myapplication.prestador.viewmodel.ProfileState
import com.example.myapplication.prestador.viewmodel.UpdateState
import com.example.myapplication.prestador.viewmodel.ReferenteViewModel
import com.example.myapplication.prestador.viewmodel.ReferentesUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.prestador.viewmodel.PhotoUploadState
import com.example.myapplication.prestador.viewmodel.BusinessViewModel
import androidx.compose.runtime.collectAsState
import com.example.myapplication.prestador.data.model.ServicioFirebase
@Composable
fun CambiarEmailDialog(onDismiss: () -> Unit) {
    val colors = getPrestadorColors()
    var nuevoEmail by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var estado by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val esUsuarioGoogle = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance()
            .currentUser?.providerData
            ?.any { it.providerId == "google.com" } == true
    }

    AlertDialog(
        onDismissRequest = { if (!cargando) onDismiss() },
        containerColor = colors.surfaceColor,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = colors.primaryOrange,
                modifier = Modifier.size(28.dp))
        },
        title = {
            Text("Cambiar email", fontWeight = FontWeight.Bold, color = colors.textPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                if (esUsuarioGoogle) {
                    // Usuario Google — no puede cambiar email desde aquí
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = colors.primaryOrange,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Text(
                                "Tu cuenta usa Google como m\u00e9todo de inicio de sesi\u00f3n. " +
                                "El email est\u00e1 administrado por Google y no puede cambiarse desde aqu\u00ed.\n\n" +
                                "Para modificarlo, hac\u00e9lo desde tu cuenta de Google.",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Text(
                        "Ingres\u00e1 tu nuevo email y tu contrase\u00f1a actual para confirmar el cambio. " +
                        "Te enviaremos un email de verificaci\u00f3n.",
                        fontSize = 13.sp, color = colors.textSecondary
                    )

                    OutlinedTextField(
                        value = nuevoEmail,
                        onValueChange = { nuevoEmail = it; estado = null },
                        label = { Text("Nuevo email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            focusedLabelColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it; estado = null },
                        label = { Text("Contrase\u00f1a actual") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                                Icon(
                                    if (contrasenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (contrasenaVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primaryOrange,
                            focusedLabelColor = colors.primaryOrange,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    estado?.let { msg ->
                        val esError = msg.startsWith("Error") || msg.startsWith("La")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (esError) colors.error.copy(alpha = 0.1f)
                                    else colors.success.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (esError) colors.error else colors.success,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!esUsuarioGoogle) {
                Button(
                    onClick = {
                        if (nuevoEmail.isBlank() || contrasena.isBlank()) {
                            estado = "Complet\u00e1 todos los campos."
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(nuevoEmail).matches()) {
                            estado = "La direcci\u00f3n de email no es v\u00e1lida."
                            return@Button
                        }
                        cargando = true
                        scope.launch {
                            try {
                                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                    ?: throw Exception("Sesi\u00f3n no encontrada.")
                                val credential = com.google.firebase.auth.EmailAuthProvider
                                    .getCredential(user.email ?: "", contrasena)
                                user.reauthenticate(credential).await()
                                user.verifyBeforeUpdateEmail(nuevoEmail).await()
                                estado = "\u2705 Te enviamos un link de verificaci\u00f3n a $nuevoEmail. " +
                                         "El cambio se aplicar\u00e1 cuando confirmes."
                            } catch (e: Exception) {
                                estado = "Error: ${
                                    when {
                                        e.message?.contains("password") == true ||
                                        e.message?.contains("credential") == true ->
                                            "Contrase\u00f1a incorrecta."
                                        e.message?.contains("email") == true ->
                                            "Email inv\u00e1lido o ya en uso."
                                        else -> e.message ?: "Ocurri\u00f3 un error."
                                    }
                                }"
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = Color.White,
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Confirmar", color = Color.White)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!cargando) onDismiss() }) {
                Text("Cancelar", color = colors.textSecondary)
            }
        }
    )
}



package com.example.myapplication.prestador.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.prestador.R
import com.example.myapplication.prestador.ui.theme.getPrestadorColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun PrestadorLoginScreen(
    onLoginSuccess: (hasProfile: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: PrestadorLoginViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    viewModel.handleGoogleSignInResult(idToken)
                } ?: run {
                    errorMessage = "No se pudo obtener el token de Google. Verifica que el SHA-1 este registrado en Firebase Console."
                    viewModel.resetLoginState()
                }
            } catch (e: ApiException) {
                val detalle = when (e.statusCode) {
                    10    -> "Error de configuracion (codigo 10): registra el SHA-1 del certificado en Firebase Console."
                    7     -> "Sin conexion a internet (codigo 7)."
                    12500 -> "Inicio de sesion fallido (codigo 12500). Verifica google-services.json."
                    12501 -> null
                    else  -> "Error Google (codigo ${e.statusCode}): ${e.message}"
                }
                errorMessage = detalle
                viewModel.resetLoginState()
            }
        } else {
            viewModel.resetLoginState()
        }
    }

    val loginState by viewModel.loginState.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()
    val passwordResetEmailSent by viewModel.passwordResetEmailSent.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> onLoginSuccess(hasProfile)
            is LoginState.Error   -> { isLoading = false; errorMessage = (loginState as LoginState.Error).message }
            is LoginState.Loading -> { isLoading = true; errorMessage = null }
            is LoginState.Idle    -> isLoading = false
        }
    }

    LaunchedEffect(passwordResetEmailSent) {
        if (passwordResetEmailSent) showSuccessDialog = true
    }

    PrestadorLoginScreenContent(
        isLoading = isLoading,
        errorMessage = errorMessage,
        showSuccessDialog = showSuccessDialog,
        onLoginClick = { email, password ->
            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password)
            } else {
                errorMessage = "Por favor completa todos los campos"
            }
        },
        onGoogleSignInClick = {
            viewModel.signInWithGoogle()
            launcher.launch(googleSignInClient.signInIntent)
        },
        onNavigateToRegister = onNavigateToRegister,
        onResetPassword = { viewModel.resetPassword(it) },
        onDismissSuccessDialog = {
            showSuccessDialog = false
            viewModel.resetPasswordEmailSentFlag()
        }
    )
}

@Composable
fun PrestadorLoginScreenContent(
    isLoading: Boolean,
    errorMessage: String?,
    showSuccessDialog: Boolean,
    onLoginClick: (email: String, password: String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onResetPassword: (String) -> Unit,
    onDismissSuccessDialog: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val colors = getPrestadorColors()

    val headerGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFFF7043),
            0.45f to Color(0xFFFF9E80),
            1.0f to Color(0xFFFFCCBC)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── HERO HEADER ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(brush = headerGradient)
                    .statusBarsPadding()
                    .padding(top = 32.dp, bottom = 44.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Icono circular
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(8.dp, CircleShape)
                            .background(Color.White.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "\uD83D\uDD28", fontSize = 36.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "PBEM Prestador",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3D1100),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tu plataforma de servicios",
                        fontSize = 14.sp,
                        color = Color(0xFF5D2000).copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── FORMULARIO ──────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ingresa a tu cuenta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Spacer(Modifier.height(20.dp))

                    // Campo Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Correo electronico", color = colors.textSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = colors.textSecondary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated,
                            focusedBorderColor = Color(0xFFFF7043),
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = Color(0xFFFF7043)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Campo Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contrasena", color = colors.textSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password", tint = colors.textSecondary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                                    ),
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                    tint = if (passwordVisible) Color(0xFFFF7043) else colors.textSecondary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceElevated,
                            unfocusedContainerColor = colors.surfaceElevated,
                            focusedBorderColor = Color(0xFFFF7043),
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = Color(0xFFFF7043)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Boton Iniciar Sesion (gradiente)
                    Button(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFFFF7043), Color(0xFFFF9E80))
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = "Iniciar Sesion",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Olvide mi contrasena
                    TextButton(onClick = { showForgotPasswordDialog = true }) {
                        Text(
                            text = "Olvidaste tu contrasena?",
                            color = Color(0xFFFF7043),
                            fontSize = 14.sp
                        )
                    }

                    // Divisor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.divider)
                        Text(
                            text = " o ",
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.divider)
                    }

                    // Boton Google
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surfaceColor),
                        enabled = !isLoading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Continuar con Google",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Registrarse
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No tienes cuenta?",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = onNavigateToRegister) {
                            Text(
                                text = "Registrate",
                                color = Color(0xFFFF7043),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Dialog: olvide contrasena
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSendEmail = { emailToReset ->
                onResetPassword(emailToReset)
                showForgotPasswordDialog = false
            }
        )
    }

    // Dialog: correo enviado
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissSuccessDialog,
            title = {
                Text(
                    text = "Correo Enviado!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hemos enviado un correo de recuperacion a tu email.", fontSize = 14.sp)
                    Text("Revisa tu bandeja de entrada y sigue las instrucciones para restablecer tu contrasena.", fontSize = 14.sp)
                    Text("Revisa tambien la carpeta de SPAM si no lo encuentras.", fontSize = 12.sp, color = colors.textSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissSuccessDialog) {
                    Text("Entendido", color = Color(0xFFFF7043), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val colors = getPrestadorColors()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar Contrasena", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Ingresa tu correo electronico y te enviaremos un enlace para restablecer tu contrasena.",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electronico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF7043),
                        focusedLabelColor = Color(0xFFFF7043)
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (email.isNotEmpty()) onSendEmail(email) }
            ) {
                Text("Enviar", color = Color(0xFFFF7043), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = colors.textSecondary)
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PrestadorLoginScreenPreview() {
    PrestadorLoginScreenContent(
        isLoading = false,
        errorMessage = null,
        showSuccessDialog = false,
        onLoginClick = { _, _ -> },
        onGoogleSignInClick = {},
        onNavigateToRegister = {},
        onResetPassword = {},
        onDismissSuccessDialog = {}
    )
}

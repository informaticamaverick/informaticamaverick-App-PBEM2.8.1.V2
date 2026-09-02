package com.example.myapplication.prestador.ui.pantallas.login

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.prestador.R
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.ChatSoporteSheet
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.estadoInfo
import com.example.myapplication.prestador.ui.pantallas.dashboard.componentes.formatearFecha
import com.example.myapplication.prestador.viewmodel.login.ApelacionBaneoViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.Check
import com.example.myapplication.prestador.ui.pantallas.empresa.turnos.GestionTurnosTheme

@Composable
fun PrestadorLoginScreen(
    onLoginSuccess: (hasProfile: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: PrestadorLoginViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showBannedDialog by remember { mutableStateOf(false) }
    var motivoSuspension by remember { mutableStateOf<String?>(null) }
    var showSoporte by remember { mutableStateOf(false) }
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
        when (val estado = loginState) {
            is EstadoLogin.Exito -> onLoginSuccess(hasProfile)
            is EstadoLogin.Error   -> { isLoading = false; errorMessage = estado.mensaje }
            is EstadoLogin.Cargando -> { isLoading = true; errorMessage = null }
            is EstadoLogin.Inactivo    -> isLoading = false
            is EstadoLogin.Suspendido -> {
                isLoading = false
                motivoSuspension = estado.motivo
                showBannedDialog = true
            }
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
            // 🔥 [FIX v8.8] Google Stale Token Resolution
            // Forzamos el cierre de sesión del cliente de Google antes de pedir uno nuevo.
            // Esto garantiza que el selector de cuentas aparezca y se genere un ID Token fresco.
            googleSignInClient.signOut().addOnCompleteListener {
                Log.d("PrestadorLogin", "🔄 [GOOGLE_REFRESH] Sesión previa de Google limpiada. Abriendo selector...")
                launcher.launch(googleSignInClient.signInIntent)
            }
        },
        onNavigateToRegister = onNavigateToRegister,
        onResetPassword = { viewModel.resetPassword(it) },
        onDismissSuccessDialog = {
            showSuccessDialog = false
            viewModel.resetPasswordEmailSentFlag()
        }
    )

    if (showBannedDialog) {
        CuentaSuspendidaDialog(
            motivo = motivoSuspension,
            onDismiss = {
                showBannedDialog = false
                viewModel.cerrarSesionSuspendida()
                viewModel.resetLoginState()
            },
            onContactarSoporte = { showSoporte = true }
        )
    }

    if (showSoporte) {
        ChatSoporteSheet(onDismiss = { showSoporte = false })
    }
}

@Composable
private fun campoLoginColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = GestionTurnosTheme.TextPrimary,
    unfocusedTextColor = GestionTurnosTheme.TextPrimary,
    cursorColor = GestionTurnosTheme.BrandOrange,
    focusedBorderColor = GestionTurnosTheme.BrandOrange,
    unfocusedBorderColor = GestionTurnosTheme.BorderGlass,
    focusedContainerColor = Color(0xFF020617),
    unfocusedContainerColor = Color(0xFF020617),
    focusedLabelColor = GestionTurnosTheme.BrandOrange,
    unfocusedLabelColor = GestionTurnosTheme.TextMuted
)


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
    val colors = GestionTurnosTheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── BADGE + TÍTULO ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(12.dp, CircleShape, spotColor = colors.BrandOrange.copy(alpha = 0.5f))
                    .background(Brush.linearGradient(listOf(colors.BrandOrange, Color(0xFFFB923C))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83D\uDD28", fontSize = 36.sp)
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "PBEM PRESTADOR",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = colors.TextPrimary,
                letterSpacing = 0.3.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tu plataforma de servicios",
                fontSize = 13.sp,
                color = colors.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(32.dp))

            // ── FORMULARIO ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.CardBg,
                border = BorderStroke(1.dp, colors.BorderGlass)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Ingresá a tu cuenta",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.TextPrimary
                    )

                    Spacer(Modifier.height(16.dp))

                    // Campo Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Correo electrónico", color = colors.TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = colors.TextMuted)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = campoLoginColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña", color = colors.TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password", tint = colors.TextMuted)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                                    ),
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                    tint = if (passwordVisible) colors.BrandOrange else colors.TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = campoLoginColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFF87171),
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showForgotPasswordDialog = true }, contentPadding = PaddingValues(0.dp)) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                color = colors.BrandOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Botón Iniciar Sesión (gradiente)
                    Button(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(colors.BrandOrange, Color(0xFFFB923C))),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "INICIAR SESIÓN",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Divisor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.BorderGlass)
                        Text(
                            text = " o ",
                            color = colors.TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.BorderGlass)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Botón Google
                    OutlinedButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.BorderGlass),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Continuar con Google",
                                color = colors.TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
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
                            text = "¿No tenés cuenta?",
                            color = colors.TextSecondary,
                            fontSize = 13.sp
                        )
                        TextButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier.padding(start = 4.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Registrate",
                                color = colors.BrandOrange,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: olvidé contraseña
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
        Dialog(onDismissRequest = onDismissSuccessDialog) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = colors.CardBg,
                border = BorderStroke(1.dp, colors.BorderGlass)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).background(colors.AccentEmerald.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = colors.AccentEmerald, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "¡Correo enviado!",
                        fontWeight = FontWeight.Black,
                        color = colors.AccentEmerald,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Te enviamos un correo de recuperación.",
                        fontSize = 13.sp,
                        color = colors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Revisá tu bandeja de entrada (y la carpeta de SPAM) y seguí las instrucciones.",
                        fontSize = 12.sp,
                        color = colors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = onDismissSuccessDialog,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.BrandOrange)
                    ) {
                        Text("Entendido", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    val colors = GestionTurnosTheme

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.CardBg,
            border = BorderStroke(1.dp, colors.BorderGlass)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Recuperar Contraseña", fontWeight = FontWeight.Black, fontSize = 17.sp, color = colors.TextPrimary)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Ingresá tu correo electrónico y te envíaremos un enlace para restablecer tu contraseña.",
                    fontSize = 13.sp,
                    color = colors.TextSecondary
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Correo electronico", color = colors.TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = colors.TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = campoLoginColors()
                )
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = colors.TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { if (email.isNotEmpty()) onSendEmail(email) }) {
                        Text("Enviar", color = colors.BrandOrange, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun CuentaSuspendidaDialog(
    motivo: String?,
    onDismiss: () -> Unit,
    onContactarSoporte: () -> Unit = {},
    viewModel: ApelacionBaneoViewModel = hiltViewModel()
) {
    val colors = GestionTurnosTheme
    val rojo = Color(0xFFEF4444)
    val rojoOscuro = Color(0xFFDC2626)

    val infiniteAnim = rememberInfiniteTransition(label = "ban_ping")
    val ring1 by infiniteAnim.animateFloat(
        initialValue = 0.6f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "ring1"
    )
    val ring1Alpha by infiniteAnim.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "ring1Alpha"
    )
    val ring2 by infiniteAnim.animateFloat(
        initialValue = 0.6f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1600, delayMillis = 500, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "ring2"
    )
    val ring2Alpha by infiniteAnim.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600, delayMillis = 500, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "ring2Alpha"
    )
    val badgePulse by infiniteAnim.animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "badgePulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.CardBg),
            border = BorderStroke(1.dp, colors.BorderGlass)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(ring1)
                            .background(rojo.copy(alpha = ring1Alpha), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(ring2)
                            .background(rojo.copy(alpha = ring2Alpha), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(badgePulse)
                            .shadow(6.dp, CircleShape)
                            .background(Brush.verticalGradient(listOf(rojo, rojoOscuro)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Cuenta Suspendida",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Tu acceso a PBEM Prestador fue suspendido por el equipo de administración.",
                    fontSize = 14.sp,
                    color = colors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                if (!motivo.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = rojo.copy(alpha = 0.12f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Motivo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = motivo,
                                fontSize = 13.sp,
                                color = colors.TextPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                val ticketApelacion by viewModel.ticketApelacion.collectAsState()
                val enviandoApelacion by viewModel.enviando.collectAsState()
                val errorApelacion by viewModel.error.collectAsState()
                var comentarioApelacion by remember { mutableStateOf("") }

                if (ticketApelacion != null) {
                    val ticket = ticketApelacion!!
                    val info = estadoInfo(ticket.estado)
                    val ultimoMensaje = ticket.mensajes.lastOrNull()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = info.color.copy(alpha = 0.12f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tu apelación", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.TextSecondary)
                                Spacer(Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(info.color.copy(alpha = 0.20f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(info.etiqueta, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = info.color)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = ultimoMensaje?.texto ?: "Ya la enviamos, el equipo la está revisando.",
                                fontSize = 13.sp,
                                color = colors.TextPrimary
                            )
                            if (ultimoMensaje != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(formatearFecha(ultimoMensaje.fecha), fontSize = 10.sp, color = colors.TextMuted)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    if (ticket.estado == "resuelto") {
                        Text(
                            "Caso cerrado — el equipo dio esta consulta por resuelta.",
                            fontSize = 11.sp,
                            color = colors.TextSecondary
                        )
                    } else {
                        var textoRespuesta by remember(ticket.id) { mutableStateOf("") }
                        OutlinedTextField(
                            value = textoRespuesta,
                            onValueChange = { if (it.length <= 300) textoRespuesta = it },
                            placeholder = { Text("Escribir una respuesta...", fontSize = 13.sp, color = colors.TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = rojo,
                                unfocusedBorderColor = colors.BorderGlass,
                                focusedTextColor = colors.TextPrimary,
                                unfocusedTextColor = colors.TextPrimary,
                                cursorColor = rojo
                            ),
                            maxLines = 3
                        )
                        if (errorApelacion != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(errorApelacion ?: "", fontSize = 12.sp, color = rojo)
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val t = textoRespuesta.trim()
                                if (t.isNotEmpty()) {
                                    viewModel.responder(ticket.id, t)
                                    textoRespuesta = ""
                                }
                            },
                            enabled = !enviandoApelacion && textoRespuesta.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = rojoOscuro)
                        ) {
                            if (enviandoApelacion) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Responder", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = comentarioApelacion,
                        onValueChange = { if (it.length <= 300) comentarioApelacion = it },
                        placeholder = { Text("Contá tu versión (opcional)", fontSize = 13.sp, color = colors.TextMuted) },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = rojo,
                            unfocusedBorderColor = colors.BorderGlass,
                            focusedTextColor = colors.TextPrimary,
                            unfocusedTextColor = colors.TextPrimary,
                            cursorColor = rojo
                        ),
                        maxLines = 4
                    )
                    if (errorApelacion != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(errorApelacion ?: "", fontSize = 12.sp, color = rojo)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.enviarApelacion(motivo, comentarioApelacion) },
                        enabled = !enviandoApelacion,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = rojoOscuro)
                    ) {
                        if (enviandoApelacion) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Enviar apelación", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onContactarSoporte,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.BorderGlass),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.TextPrimary)
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Contactar a soporte", fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.BrandOrange)
                ) {
                    Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
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
















































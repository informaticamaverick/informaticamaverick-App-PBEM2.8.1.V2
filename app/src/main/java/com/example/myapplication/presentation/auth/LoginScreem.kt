package com.example.myapplication.presentation.auth

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.presentation.components.Utilidades.CustomTextField
import com.example.myapplication.presentation.components.PrimaryButton
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * ======================================================================================
 * PANTALLA DE AUTENTICACIÓN MAVERICK - REDISEÑO CIBERPUNK M3
 * ======================================================================================
 */

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (targetRoute: String?) -> Unit // 🔥 ACTUALIZADO MAVERICK V5
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationTarget by viewModel.navigationTarget.collectAsState() // 🔥 NUEVO
    val context = LocalContext.current

    // --------------------------------------------------------------------------------------
    // SECCIÓN 1: GESTIÓN DE PERMISOS
    // --------------------------------------------------------------------------------------
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> /* Permisos procesados */ }

    LaunchedEffect(Unit) {
        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // --------------------------------------------------------------------------------------
    // SECCIÓN 2: LÓGICA DE GOOGLE SIGN-IN
    // --------------------------------------------------------------------------------------
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.handleGoogleSignInResult(it) }
            } catch (e: ApiException) { viewModel.onSignInCancelled() }
        } else { viewModel.onSignInCancelled() }
    }

    // --- SECCIÓN: NAVEGACIÓN POST-LOGIN ---
    LaunchedEffect(uiState.isLoginSuccess, navigationTarget) {
        if (uiState.isLoginSuccess && navigationTarget != null) { 
            onLoginSuccess(navigationTarget) 
            viewModel.consumeNavigationTarget()
        }
    }

    LaunchedEffect(Unit) { viewModel.checkCurrentUser() }

    LoginScreenContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onGoogleSignInClick = {
            viewModel.signInWithGoogle()
            launcher.launch(googleSignInClient.signInIntent)
        },
        onForgotPasswordSubmit = viewModel::resetPassword,
        onToggleRegister = viewModel::toggleRegisterWithEmail,
        onSendVerification = viewModel::sendVerificationCode,
        onCodeChange = viewModel::onVerificationCodeChange,
        onVerifyAccept = viewModel::verifyCodeAndContinue
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordSubmit: (String) -> Unit,
    onToggleRegister: () -> Unit,
    onSendVerification: () -> Unit,
    onCodeChange: (String) -> Unit,
    onVerifyAccept: () -> Unit
) {
    // COLORES CYBERPUNK MAVERICK
    val cyberMagenta = Color(0xFFFF00FF)
    val cyberCyan = Color(0xFF00FFFF)
    val cyberDark = Color(0xFF0D0221)
    val cyberYellow = Color(0xFFFFF01F)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cyberDark)
            .drawBehind {
                // Efecto de rejilla digital Maverick
                val gridStep = 45.dp.toPx()
                for (x in 0..size.width.toInt() step gridStep.toInt()) {
                    drawLine(cyberCyan.copy(alpha = 0.08f), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), 1f)
                }
                for (y in 0..size.height.toInt() step gridStep.toInt()) {
                    drawLine(cyberCyan.copy(alpha = 0.08f), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), 1f)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --------------------------------------------------------------------------------------
            // SECCIÓN 3: CABECERA Y ASISTENTE BE (BUSCANDO BOTÓN GOOGLE)
            // --------------------------------------------------------------------------------------
            BeLoginAssistant(
                cyberCyan = cyberCyan,
                cyberMagenta = cyberMagenta
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MAVERICK INTERFACE",
                style = TextStyle(
                    color = cyberCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 5.sp,
                    shadow = Shadow(color = cyberCyan, blurRadius = 8f)
                )
            )

            Spacer(modifier = Modifier.height(60.dp))

            // --------------------------------------------------------------------------------------
            // SECCIÓN 4: BOTÓN ESTRELLA (GOOGLE)
            // --------------------------------------------------------------------------------------
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .border(2.dp, Brush.horizontalGradient(listOf(cyberCyan, cyberMagenta)), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                enabled = !uiState.isLoading
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "REGISTRAR CON GOOGLE",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --------------------------------------------------------------------------------------
            // SECCIÓN 5: REGISTRO MANUAL DESPLEGABLE ANIMADO
            // --------------------------------------------------------------------------------------
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onToggleRegister() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isRegisteringWithEmail) "USAR OTRA CUENTA" else "OTRO CORREO ELECTRÓNICO",
                        color = cyberYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (uiState.isRegisteringWithEmail) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = cyberYellow
                    )
                }

                AnimatedVisibility(
                    visible = uiState.isRegisteringWithEmail,
                    enter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            if (!uiState.isVerificationSent) {
                                // Formulario de Registro/Login
                                CustomTextField(
                                    value = uiState.email,
                                    onValueChange = onEmailChange,
                                    placeholder = "CORREO@MAVERICK.COM",
                                    icon = Icons.Default.AlternateEmail
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                CustomTextField(
                                    value = uiState.password,
                                    onValueChange = onPasswordChange,
                                    placeholder = "PASSWORD_ACCESS",
                                    icon = Icons.Default.VpnKey,
                                    isPassword = true
                                )

                                Text(
                                    text = "¿OLVIDASTE TU CONTRASEÑA?",
                                    color = cyberCyan,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 8.dp)
                                        .clickable { onForgotPasswordSubmit(uiState.email) },
                                    textDecoration = TextDecoration.Underline
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PrimaryButton(
                                    text = "ENVIAR CÓDIGO",
                                    onClick = onSendVerification,
                                    backgroundColor = cyberMagenta,
                                    enabled = !uiState.isLoading
                                )
                            } else {
                                // ----------------------------------------------------------------------------------
                                // SECCIÓN 6: VERIFICACIÓN Y TIMER (MAVERICK CLOCK)
                                // ----------------------------------------------------------------------------------
                                Text(
                                    text = "CÓDIGO DE SEGURIDAD ENVIADO",
                                    color = cyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val minutes = uiState.timerValue / 60
                                val seconds = uiState.timerValue % 60
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                    color = cyberYellow,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                CustomTextField(
                                    value = uiState.verificationCode,
                                    onValueChange = onCodeChange,
                                    placeholder = "INGRESE CÓDIGO",
                                    icon = Icons.Default.Shield
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                PrimaryButton(
                                    text = "ACEPTAR VERIFICACIÓN",
                                    onClick = onVerifyAccept,
                                    backgroundColor = cyberCyan,
                                    enabled = !uiState.isLoading
                                )
                            }
                        }
                    }
                }
            }

            // Manejo de Errores de Sistema
            uiState.error?.let { error ->
                Text(
                    text = "CRITICAL_ERROR: $error",
                    color = Color.Red,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(40.dp))

            // --------------------------------------------------------------------------------------
            // SECCIÓN 7: TÉRMINOS Y CONDICIONES (PIE DE PANTALLA)
            // --------------------------------------------------------------------------------------
            Text(
                text = "AL ACCEDER ACEPTAS LOS TÉRMINOS Y CONDICIONES\nDE MAVERICK SYSTEM PROTOCOL",
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // Overlay de Carga Neón
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = cyberMagenta, strokeWidth = 5.dp)
            }
        }
    }
}

/**
 * BeLoginAssistant - Animación especial para el Login
 * Be busca algo mirando a los lados y luego mira hacia abajo (al botón de Google)
 */
@Composable
fun BeLoginAssistant(
    modifier: Modifier = Modifier,
    cyberCyan: Color,
    cyberMagenta: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "be_login_anim")
    
    // Animación de flotación suave
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Estados de la mirada
    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            // 1. Buscando: Mira a la izquierda y derecha
            repeat(2) {
                targetPupilX = -6f; targetPupilY = -2f
                delay(1000)
                targetPupilX = 6f; targetPupilY = -2f
                delay(1000)
            }
            // 2. Encuentra el botón de Google (mira hacia abajo)
            targetPupilX = 0f; targetPupilY = 8f
            delay(3500)
            
            // 3. Pequeño re-centrado antes de reiniciar el ciclo
            targetPupilX = 0f; targetPupilY = 0f
            delay(800)
        }
    }

    val pupilX by animateFloatAsState(targetPupilX, tween(700, easing = FastOutSlowInEasing), label = "pupilX")
    val pupilY by animateFloatAsState(targetPupilY, tween(700, easing = FastOutSlowInEasing), label = "pupilY")

    // Animación de parpadeo
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..6000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(if (isBlinking) 0.1f else 1f, tween(120), label = "blink")

    Box(
        modifier = modifier
            .offset(y = floatY.dp)
            .size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Resplandor neón de fondo
        Canvas(modifier = Modifier.size(110.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cyberCyan.copy(alpha = 0.4f), Color.Transparent)
                ),
                radius = size.width / 1.2f
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = size.width / 100f
            scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
                // Casco Base de Be
                drawCircle(Color(0xFF0A0E14), 38f, Offset(50f, 50f))
                
                // Anillo Neón (Estilo ROG Maverick)
                drawCircle(
                    color = cyberCyan,
                    radius = 34f,
                    center = Offset(50f, 50f),
                    style = Stroke(width = 4f)
                )
                
                // Brillo del visor
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 3f, cap = StrokeCap.Round),
                    topLeft = Offset(20f, 20f),
                    size = Size(60f, 60f)
                )

                // Ojos
                drawOval(
                    color = Color.White,
                    topLeft = Offset(30f, 50f - (12f * eyeScaleY)),
                    size = Size(16f, 24f * eyeScaleY)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(54f, 50f - (12f * eyeScaleY)),
                    size = Size(16f, 24f * eyeScaleY)
                )
                
                // Pupilas móviles
                val pupilRadius = 5f
                drawCircle(
                    color = Color(0xFF05070A),
                    radius = pupilRadius * eyeScaleY,
                    center = Offset(38f + pupilX, 50f + pupilY)
                )
                drawCircle(
                    color = Color(0xFF05070A),
                    radius = pupilRadius * eyeScaleY,
                    center = Offset(62f + pupilX, 50f + pupilY)
                )
                
                // Reflejos en las pupilas
                drawCircle(
                    color = Color.White,
                    radius = 1.5f * eyeScaleY,
                    center = Offset(39f + pupilX, 48f + pupilY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.5f * eyeScaleY,
                    center = Offset(63f + pupilX, 48f + pupilY)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onGoogleSignInClick = {},
            onForgotPasswordSubmit = {},
            onToggleRegister = {},
            onSendVerification = {},
            onCodeChange = {},
            onVerifyAccept = {}
        )
    }
}

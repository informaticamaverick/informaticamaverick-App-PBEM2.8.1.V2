package com.example.myapplication.presentation.features.auth

import android.Manifest
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale as drawScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.presentation.components.BeEmotion
import com.example.myapplication.presentation.designsystem.components.CustomTextField
import com.example.myapplication.presentation.components.PrimaryButton
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.components.MaverickBackgroundStrix
import com.example.myapplication.presentation.designsystem.components.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.pager.*
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// === SECCIÓN: PANTALLA DE AUTENTICACIÓN MAVERICK (PANTALLA TONTA) ===

/**
 * LoginScreen: Puerta de entrada al sistema Maverick.
 * Sigue la Ley 1: Stateless Screen. Delega toda la lógica al LoginViewModel.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (targetRoute: String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationTarget by viewModel.navigationTarget.collectAsState()
    val context = LocalContext.current

    // --- SECCIÓN: GESTIÓN DE PERMISOS TÁCTICOS ---
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
    ) { results -> 
        // MAVERICK V5: Actualización selectiva para evitar sobreescritura de estados
        if (results.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) || 
            results.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            val locationGranted = results.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                                 results.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            viewModel.updateLocationPermissionStatus(locationGranted)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            results.containsKey(Manifest.permission.POST_NOTIFICATIONS)) {
            val notificationsGranted = results.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false)
            viewModel.updateNotificationsPermissionStatus(notificationsGranted)
        }
    }

    LaunchedEffect(Unit) {
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                         ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        val needsNotificationCheck = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val hasNotifications = if (needsNotificationCheck) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        viewModel.updateLocationPermissionStatus(hasLocation)
        viewModel.updateNotificationsPermissionStatus(hasNotifications)

        // Lanzamiento obligatorio al inicio si falta algún permiso crítico
        val shouldRequest = !hasLocation || (needsNotificationCheck && !hasNotifications)
        if (shouldRequest) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // --- SECCIÓN: GOOGLE SIGN-IN (CREDENTIAL MANAGER) ---
    val credentialManager = remember { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()
    val serverClientId = stringResource(id = R.string.default_web_client_id)

    val onGoogleSignInClick: () -> Unit = {
        scope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                android.util.Log.d("LoginScreen", "🟡 Credencial recibida: ${credential.type}")
                
                // MAVERICK V5: Extracción robusta del ID Token
                // Usamos el ID de tipo de la librería para evitar fallos de inicialización
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        android.util.Log.d("LoginScreen", "🟢 GoogleIdTokenCredential extraída con éxito")
                        viewModel.handleGoogleSignInResult(googleIdTokenCredential.idToken)
                    } catch (e: Exception) {
                        android.util.Log.e("LoginScreen", "🔴 Error al crear GoogleIdTokenCredential: ${e.message}")
                        viewModel.onSignInCancelled()
                    }
                } else {
                    android.util.Log.w("LoginScreen", "⚠️ Tipo de credencial no soportado: ${credential.type}")
                    viewModel.onSignInCancelled()
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginScreen", "🔴 Error en CredentialManager: ${e.message}")
                viewModel.onSignInCancelled()
            }
        }
    }

    // --- SECCIÓN: NAVEGACIÓN POST-LOGIN (LEY DE COSTO ZERO) ---
    LaunchedEffect(uiState.isLoginSuccess, navigationTarget) {
        if (uiState.isLoginSuccess && navigationTarget != null) { 
            onLoginSuccess(navigationTarget)
            viewModel.consumeNavigationTarget()
        }
    }

    LaunchedEffect(Unit) { viewModel.checkCurrentUser() }

    LoginScreenContent(
        uiState = uiState,
        isWifiEnabled = viewModel.isWifiEnabled.collectAsState().value,
        isGpsEnabled = viewModel.isGpsEnabled.collectAsState().value,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onGoogleSignInClick = onGoogleSignInClick,
        onForgotPasswordSubmit = viewModel::resetPassword,
        onToggleRegister = viewModel::toggleRegisterWithEmail,
        onSendVerification = viewModel::sendVerificationCode,
        onCodeChange = viewModel::onVerificationCodeChange,
        onVerifyAccept = viewModel::verifyCodeAndContinue,
        onRequestLocationPermission = {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        },
        onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    )
}

// === SECCIÓN: CONTENIDO VISUAL (CYBERPUNK M3) ===

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    isWifiEnabled: Boolean = false,
    isGpsEnabled: Boolean = false,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordSubmit: (String) -> Unit,
    onToggleRegister: () -> Unit,
    onSendVerification: () -> Unit,
    onCodeChange: (String) -> Unit,
    onVerifyAccept: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    // COLORES MAVERICK M3 (MATE / APAGADOS)
    val mutedPurple = Color(0xFF6750A4)
    val mutedCyan = Color(0xFF00838F)

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    MaverickBackgroundStrix {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- BLOQUE SUPERIOR: LOGIN Y MARCA ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // CABECERA BEM
                Text(
                    text = "BEM",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 16.sp,
                        shadow = Shadow(color = mutedCyan, blurRadius = 15f)
                    )
                )
                
                Text(
                    text = "CONECTANDO A PERSONAS",
                    style = TextStyle(
                        color = mutedCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 4.sp,
                        shadow = Shadow(color = mutedCyan.copy(alpha = 0.5f), blurRadius = 4f)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CABECERA Y ASISTENTE BE (COLORES VÍVIDOS - LA ESTRELLA)
                BeLoginAssistant(
                    cyberCyan = Color(0xFF00FFFF),
                    cyberMagenta = Color(0xFFFF00FF)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "BUSCAR SE ESCRIBE CON BE",
                    style = TextStyle(
                        color = mutedCyan,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(color = mutedCyan, blurRadius = 1f)
                    )
                )

                Spacer(modifier = Modifier.height(46.dp))

                // --- SECCIÓN: BUBBLE TABS (GOOGLE & EMAIL) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BubbleTabItem(
                        icon = R.drawable.ic_google_logo,
                        isSelected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                        accentColor = mutedCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BubbleTabItem(
                        icon = "✉️", // EMOJI SOBRE CERRADO
                        isSelected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                        accentColor = mutedPurple
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // --- SECCIÓN: PAGER DE CONTENIDO ---
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> GoogleTabContent(uiState, onGoogleSignInClick, onToggleRegister)
                        1 -> EmailTabContent(
                            uiState = uiState,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onForgotPasswordSubmit = onForgotPasswordSubmit,
                            onLoginClick = onVerifyAccept
                        )
                    }
                }
            }

            // --- BLOQUE INFERIOR: CONFIGURACIÓN Y LEGAL (CAE HACIA ABAJO) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp), // Aumentado para no tapar el log
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- SECCIÓN: ACCESOS (PERMISOS) ---
                Text(
                    text = "CONFIGURACIÓN DE PERMISOS SENSORES",
                    color = mutedCyan.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionAccessItem(
                        title = "GPS",
                        subtitle = "UBICACIÓN",
                        icon = Icons.Default.MyLocation,
                        accentColor = mutedCyan,
                        modifier = Modifier.weight(1f),
                        isGranted = uiState.hasLocationPermission,
                        onClick = onRequestLocationPermission
                    )
                    PermissionAccessItem(
                        title = "NOTIFICACIONES",
                        subtitle = "ALERTAS",
                        icon = Icons.Default.NotificationsActive,
                        accentColor = mutedPurple,
                        modifier = Modifier.weight(1f),
                        isGranted = uiState.hasNotificationsPermission,
                        onClick = onRequestNotificationPermission
                    )
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

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN: TÉRMINOS Y CONDICIONES
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = true,
                        onCheckedChange = { /* Obligatorio en True */ },
                        colors = CheckboxDefaults.colors(
                            checkedColor = mutedCyan,
                            uncheckedColor = Color.White.copy(alpha = 0.2f),
                            checkmarkColor = Color.Black
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                    Text(
                        text = "AL ACCEDER ACEPTAS LOS TÉRMINOS Y CONDICIONES DE MAVERICK",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // PIE DE PANTALLA: ATRIBUCIÓN CYBERPUNK
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MAVERICK DEVELOPERS",
                        style = TextStyle(
                            color = mutedPurple.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            shadow = Shadow(color = mutedPurple, blurRadius = 4f)
                        )
                    )
                    Text(
                        text = "PROYECTO BEM",
                        style = TextStyle(
                            color = mutedCyan.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            shadow = Shadow(color = mutedCyan, blurRadius = 8f)
                        )
                    )
                }
            }
        }

        // Overlay de Carga Neón
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = mutedPurple, strokeWidth = 5.dp)
            }
        }

        // --- SECCIÓN: LOG DE SISTEMA REACTIVO (BOTTOM-CENTER) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            SystemLoginLog(isWifiEnabled, isGpsEnabled)
        }
    }
}

/**
 * SystemLoginLog: Muestra el estado del sistema en una sola fila animada.
 */
@Composable
fun SystemLoginLog(isWifi: Boolean, isGps: Boolean) {
    val logText = remember(isWifi, isGps) {
        when {
            !isWifi -> "NETWORK_OFFLINE: WAITING_CONNECTION"
            !isGps -> "SENSORS_WARNING: GPS_DISABLED"
            else -> "SYSTEM_READY: ENCRYPTED_LINK_ACTIVE"
        }
    }

    AnimatedContent(
        targetState = logText,
        transitionSpec = {
            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
        },
        label = "log_anim"
    ) { text ->
        Text(
            text = "> $text",
            color = Color(0xFF00FFFF).copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

/**
 * PermissionAccessItem: Fila moderna para solicitar accesos críticos.
 */
@Composable
fun PermissionAccessItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isGranted: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isGranted) accentColor else Color.White.copy(alpha = 0.1f),
        tween(500), label = "border_color"
    )
    val containerAlpha by animateFloatAsState(
        if (isGranted) 0.15f else 0.05f,
        tween(500), label = "bg_alpha"
    )

    Surface(
        onClick = if (isGranted) ({}) else onClick, // Desactiva el click si ya está aceptado
        color = Color.White.copy(alpha = containerAlpha),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(60.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = if (isGranted) 0.2f else 0.1f), RoundedCornerShape(10.dp))
                    .border(1.dp, accentColor.copy(alpha = if (isGranted) 0.6f else 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else icon, 
                    contentDescription = null, 
                    tint = if (isGranted) Color.White else accentColor, 
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
                if (isGranted) {
                    // ETIQUETA DE ACEPTADO MAVERICK ELITE
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "ACEPTADO", 
                            color = accentColor, 
                            fontSize = 7.sp, 
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Text(
                        text = subtitle, 
                        color = Color.White.copy(alpha = 0.5f), 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!isGranted) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward, 
                    contentDescription = null, 
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * BeLoginAssistant: Animación especial para el Login.
 * Be busca algo mirando a los lados y luego mira hacia abajo (al botón de Google).
 */
@Composable
fun BeLoginAssistant(
    modifier: Modifier = Modifier,
    cyberCyan: Color,
    cyberMagenta: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "be_login_anim")
    
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }
    var currentEmotion by remember { mutableStateOf(BeEmotion.NORMAL) }

    LaunchedEffect(Unit) {
        while (true) {
            repeat(2) {
                targetPupilX = -6f; targetPupilY = -2f
                delay(1000)
                targetPupilX = 6f; targetPupilY = -2f
                delay(1000)
            }
            
            // Momento de felicidad Maverick
            currentEmotion = BeEmotion.HAPPY
            delay(1500)
            currentEmotion = BeEmotion.NORMAL

            targetPupilX = 0f; targetPupilY = 8f
            delay(3500)
            targetPupilX = 0f; targetPupilY = 0f
            delay(800)
        }
    }

    val pupilX by animateFloatAsState(targetPupilX, tween(700, easing = FastOutSlowInEasing), label = "pupilX")
    val pupilY by animateFloatAsState(targetPupilY, tween(700, easing = FastOutSlowInEasing), label = "pupilY")

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
            .offset { IntOffset(0, floatY.dp.roundToPx()) }
            .size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Resplandor neón dual Maverick
        Canvas(modifier = Modifier.size(110.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cyberCyan.copy(alpha = 0.4f), 
                        cyberMagenta.copy(alpha = 0.1f), 
                        Color.Transparent
                    )
                ),
                radius = size.width / 1.1f
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = size.width / 100f
            drawScale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
                // ==========================================================================================
                // --- SECCIÓN: MANGO DE LA LUPA (EL MANDO DE BE) ---
                // ==========================================================================================
                // Dibujamos el mando antes que la cabeza para que quede por detrás
                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                drawLine(cyberCyan, Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)
                drawLine(cyberCyan.copy(alpha = 0.4f), Offset(76f, 76f), Offset(89f, 89f), 6f, StrokeCap.Round)

                // ==========================================================================================
                // --- SECCIÓN: CABEZA DE ASISTENTE (LENTE) ---
                // ==========================================================================================
                drawCircle(Color(0xFF0A0E14), 38f, Offset(50f, 50f))
                drawCircle(
                    color = cyberCyan,
                    radius = 34f,
                    center = Offset(50f, 50f),
                    style = Stroke(width = 4f)
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 3f, cap = StrokeCap.Round),
                    topLeft = Offset(20f, 20f),
                    size = Size(60f, 60f)
                )

                if (currentEmotion == BeEmotion.HAPPY) {
                    // OJOS FELICES (ARCOS)
                    val happyPathLeft = Path().apply {
                        moveTo(30f, 55f)
                        quadraticTo(38f, 40f, 46f, 55f)
                    }
                    val happyPathRight = Path().apply {
                        moveTo(54f, 55f)
                        quadraticTo(62f, 40f, 70f, 55f)
                    }
                    drawPath(happyPathLeft, Color.White, style = Stroke(width = 4f, cap = StrokeCap.Round))
                    drawPath(happyPathRight, Color.White, style = Stroke(width = 4f, cap = StrokeCap.Round))
                } else {
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
}
/**
 * BubbleTabItem: Un botón circular animado para las pestañas de login.
 */
@Composable
fun BubbleTabItem(
    icon: Any, // R.drawable, ImageVector o String (emoji)
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, tween(300), label = "bubble_scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, tween(300), label = "bubble_alpha")

    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .scale(scale),
        shape = CircleShape,
        color = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.2.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.1f)),
        tonalElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (icon) {
                is Int -> Icon(painterResource(id = icon), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(18.dp).alpha(alpha))
                is androidx.compose.ui.graphics.vector.ImageVector -> Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) accentColor else Color.Gray, modifier = Modifier.size(24.dp).alpha(alpha))
                is String -> Text(text = icon, fontSize = 18.sp, modifier = Modifier.alpha(alpha))
            }
        }
    }
}

@Composable
fun GoogleTabContent(
    uiState: LoginUiState,
    onGoogleSignInClick: () -> Unit,
    onToggleRegister: () -> Unit
) {
    val cyberCyan = Color(0xFF00FFFF)
    val cyberMagenta = Color(0xFFFF00FF)

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth().height(65.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(cyberCyan, cyberMagenta))),
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "INICIAR SESIÓN CON GOOGLE",
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.5.sp,
                        shadow = Shadow(color = Color.Black, blurRadius = 4f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val registerText = buildAnnotatedString {
            append("¿ERES NUEVO? ")
            pushStringAnnotation(tag = "register", annotation = "register")
            withStyle(style = SpanStyle(
                color = Color.White,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.ExtraBold
            )) {
                append("REGÍSTRATE")
            }
            pop()
            append(" AHORA")
        }

        Text(
            text = registerText,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.clickable { onToggleRegister() }.padding(12.dp)
        )
    }
}

@Composable
fun EmailTabContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onForgotPasswordSubmit: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val mutedCyan = Color(0xFF00838F)

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("CORREO ELECTRÓNICO") },
            placeholder = { Text("agente@maverick.com") },
            leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = mutedCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = mutedCyan,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        var passwordVisible by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("PASSWORD_ACCESS") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                }
            },
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = mutedCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = mutedCyan,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿OLVIDASTE TU CONTRASEÑA?",
            style = TextStyle(
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ),
            modifier = Modifier.align(Alignment.End).clickable { onForgotPasswordSubmit(uiState.email) }.padding(4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = "ENTRAR AL SISTEMA",
            onClick = onLoginClick,
            backgroundColor = mutedCyan,
            enabled = !uiState.isLoading,
            modifier = Modifier.height(48.dp) // Más pequeño
        )
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
            onGoogleSignInClick = {},
            onForgotPasswordSubmit = {},
            onToggleRegister = {},
            onSendVerification = {},
            onCodeChange = {},
            onVerifyAccept = {},
            onRequestLocationPermission = {},
            onRequestNotificationPermission = {}
        )
    }
}

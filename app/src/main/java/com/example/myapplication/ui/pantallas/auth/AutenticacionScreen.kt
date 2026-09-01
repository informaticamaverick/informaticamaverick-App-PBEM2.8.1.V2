package com.example.myapplication.ui.pantallas.auth

import android.Manifest
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
import androidx.compose.ui.graphics.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.R
import com.example.myapplication.ui.componentes.PrimaryButton
import com.example.myapplication.ui.componentes.sistema.appBackgroundStrix
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.pager.*
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
//import com.example.myapplication.coordinadores.EmocionBe
import com.example.myapplication.ui.componentes.be.modelos.EmocionBe
import com.example.myapplication.viewmodel.auth.UsuarioLoginViewModel
import com.example.myapplication.viewmodel.auth.UsuarioRegisterViewModel
import com.example.myapplication.viewmodel.auth.EstadoUiLoginUsuario
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * --- PANTALLA DE AUTENTICACIÓN (ELITE v2026.FINAL) ---
 * Orquestador visual para el acceso soberano del cliente.
 * [LEY #9]: Estándar en Idioma Español.
 */
@Composable
fun AutenticacionScreen(
    loginViewModel: UsuarioLoginViewModel = hiltViewModel(),
    registerViewModel: UsuarioRegisterViewModel = hiltViewModel(),
    alExito: (rutaDestino: String?) -> Unit
) {
    val estadoUi by loginViewModel.uiState.collectAsState()
    val estadoReg by registerViewModel.uiState.collectAsState()
    val rutaNavegacion by loginViewModel.navigationTarget.collectAsState()
    val contexto = LocalContext.current

    // --- GESTIÓN DE PERMISOS TÁCTICOS ---
    val permisosARequerir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    val lanzadorPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados -> 
        if (resultados.containsKey(Manifest.permission.ACCESS_FINE_LOCATION) || 
            resultados.containsKey(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            val concedido = resultados.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                            resultados.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            loginViewModel.updateLocationPermissionStatus(concedido)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            resultados.containsKey(Manifest.permission.POST_NOTIFICATIONS)) {
            val notifConcedido = resultados.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false)
            loginViewModel.updateNotificationsPermissionStatus(notifConcedido)
        }
    }

    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        
        val hasLocation = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                         ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val needsNotificationCheck = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val hasNotifications = if (needsNotificationCheck) {
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true

        loginViewModel.updateLocationPermissionStatus(hasLocation)
        loginViewModel.updateNotificationsPermissionStatus(hasNotifications)

        if (!hasLocation || (needsNotificationCheck && !hasNotifications)) {
            lanzadorPermisos.launch(permisosARequerir)
        }
        
        loginViewModel.verificarUsuarioActual()
    }

    // --- SECCIÓN: GOOGLE SIGN-IN ---
    val gestorCredenciales = remember { CredentialManager.create(contexto) }
    val alcance = rememberCoroutineScope()
    val idClienteServidor = stringResource(id = R.string.default_web_client_id)

    val alHacerClickGoogle: () -> Unit = {
        alcance.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(idClienteServidor)
                    .setAutoSelectEnabled(false)
                    .build()

                val peticion = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val resultado = gestorCredenciales.getCredential(context = contexto, request = peticion)
                val credencial = resultado.credential
                
                if (credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val tokenGoogle = GoogleIdTokenCredential.createFrom(credencial.data)
                    loginViewModel.manejarResultadoLoginGoogle(tokenGoogle.idToken)
                }
            } catch (e: Exception) {
                loginViewModel.setCustomError("Error Google: ${e.message}")
            }
        }
    }

    // --- SECCIÓN: NAVEGACIÓN ---
    LaunchedEffect(estadoUi.exitoLogin, estadoReg.exitoRegistro, rutaNavegacion) {
        if (estadoUi.exitoLogin && rutaNavegacion != null) { 
            alExito(rutaNavegacion)
            loginViewModel.consumeNavigationTarget()
        } else if (estadoReg.exitoRegistro) {
            alExito("home")
        }
    }

    AutenticacionScreenContent(
        estadoUi = estadoUi,
        estadoRegCargando = estadoReg.estaCargando,
        alCambiarEmail = loginViewModel::onEmailChange,
        alCambiarPassword = loginViewModel::onPasswordChange,
        alClickGoogle = alHacerClickGoogle,
        alEnviarRecuperacion = loginViewModel::recuperarClave,
        alAlternarRegistro = loginViewModel::toggleRegisterWithEmail,
        alHacerClickAccion = {
            if (estadoUi.esRegistroEmail) {
                registerViewModel.registrarse(estadoUi.email, estadoUi.password)
            } else {
                loginViewModel.iniciarSesion()
            }
        },
        alCambiarCodigo = loginViewModel::onVerificationCodeChange,
        alSolicitarPermisoGps = {
            lanzadorPermisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        },
        alSolicitarPermisoNotif = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lanzadorPermisos.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutenticacionScreenContent(
    estadoUi: EstadoUiLoginUsuario,
    estadoRegCargando: Boolean,
    alCambiarEmail: (String) -> Unit,
    alCambiarPassword: (String) -> Unit,
    alClickGoogle: () -> Unit,
    alEnviarRecuperacion: (String) -> Unit,
    alAlternarRegistro: () -> Unit,
    alHacerClickAccion: () -> Unit,
    alCambiarCodigo: (String) -> Unit,
    alSolicitarPermisoGps: () -> Unit,
    alSolicitarPermisoNotif: () -> Unit
) {
    val purpuraMate = Color(0xFF6750A4)
    val cianMate = Color(0xFF00838F)

    val estadoPager = rememberPagerState(pageCount = { 2 })
    val alcance = rememberCoroutineScope()
    val estaCargando = estadoUi.estaCargando || estadoRegCargando

    appBackgroundStrix {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "PBEM",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 12.sp,
                        shadow = Shadow(color = cianMate, blurRadius = 15f)
                    )
                )
                
                Text(
                    text = "SISTEMA DE ACCESO ELITE",
                    style = TextStyle(
                        color = cianMate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 4.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                BeLoginAssistant(
                    cyberCyan = Color(0xFF00FFFF),
                    cyberMagenta = Color(0xFFFF00FF)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "BUSCAR SE ESCRIBE CON BE",
                    style = TextStyle(
                        color = cianMate,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )

                Spacer(modifier = Modifier.height(46.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BubbleTabItem(
                        icon = com.example.myapplication.core.R.drawable.icons8_logo_de_google_48,
                        isSelected = estadoPager.currentPage == 0,
                        onClick = { alcance.launch { estadoPager.animateScrollToPage(0) } },
                        accentColor = cianMate
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BubbleTabItem(
                        icon = "✉️",
                        isSelected = estadoPager.currentPage == 1,
                        onClick = { alcance.launch { estadoPager.animateScrollToPage(1) } },
                        accentColor = purpuraMate
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                HorizontalPager(
                    state = estadoPager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalAlignment = Alignment.Top
                ) { pagina ->
                    when (pagina) {
                        0 -> ContenidoTabGoogle(estadoUi, alClickGoogle, alAlternarRegistro)
                        1 -> ContenidoTabEmail(
                            estadoUi = estadoUi,
                            alCambiarEmail = alCambiarEmail,
                            alCambiarPassword = alCambiarPassword,
                            alEnviarRecuperacion = alEnviarRecuperacion,
                            alClickLogin = alHacerClickAccion,
                            alCambiarCodigo = alCambiarCodigo
                        )
                    }
                }
            }

            // BLOQUE INFERIOR: CONFIGURACIÓN Y PERMISOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CONFIGURACIÓN DE PERMISOS SENSORES",
                    color = cianMate.copy(alpha = 0.9f),
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
                        accentColor = cianMate,
                        modifier = Modifier.weight(1f),
                        isGranted = estadoUi.tienePermisoUbicacion,
                        onClick = alSolicitarPermisoGps
                    )
                    PermissionAccessItem(
                        title = "ALERTAS",
                        subtitle = "NOTIFICACIONES",
                        icon = Icons.Default.NotificationsActive,
                        accentColor = purpuraMate,
                        modifier = Modifier.weight(1f),
                        isGranted = estadoUi.tienePermisoNotificaciones,
                        onClick = alSolicitarPermisoNotif
                    )
                }

                if (estadoUi.error != null) {
                    Text(
                        text = "ERROR_SISTEMA: ${estadoUi.error}",
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = true,
                        onCheckedChange = { },
                        colors = CheckboxDefaults.colors(
                            checkedColor = cianMate,
                            checkmarkColor = Color.Black
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                    Text(
                        text = "AL ACCEDER ACEPTAS LOS TÉRMINOS Y CONDICIONES",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "PBEM 2026",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        if (estaCargando) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = purpuraMate, strokeWidth = 5.dp)
            }
        }
    }
}

@Composable
fun ContenidoTabGoogle(
    estadoUi: EstadoUiLoginUsuario,
    alClickGoogle: () -> Unit,
    alAlternarRegistro: () -> Unit
) {
    val cyberCyan = Color(0xFF00FFFF)
    val cyberMagenta = Color(0xFFFF00FF)

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = alClickGoogle,
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
                    painter = painterResource(id = com.example.myapplication.core.R.drawable.icons8_logo_de_google_48),
                    contentDescription = "Google",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "ACCEDER CON GOOGLE SOBERANO",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = buildAnnotatedString {
                append("¿ERES NUEVO? ")
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.ExtraBold)) {
                    append("REGÍSTRATE")
                }
                append(" AHORA")
            },
            color = Color.White,
            modifier = Modifier.clickable { alAlternarRegistro() }.padding(8.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ContenidoTabEmail(
    estadoUi: EstadoUiLoginUsuario,
    alCambiarEmail: (String) -> Unit,
    alCambiarPassword: (String) -> Unit,
    alEnviarRecuperacion: (String) -> Unit,
    alClickLogin: () -> Unit,
    alCambiarCodigo: (String) -> Unit
) {
    val cianMate = Color(0xFF00838F)

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!estadoUi.esVerificacionEnviada) {
            OutlinedTextField(
                value = estadoUi.email,
                onValueChange = alCambiarEmail,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("EMAIL") },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cianMate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = estadoUi.password,
                onValueChange = alCambiarPassword,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("PASSWORD") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, null, tint = Color.Gray)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cianMate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Text(
                text = "¿OLVIDASTE TU CLAVE?",
                modifier = Modifier.align(Alignment.End).clickable { alEnviarRecuperacion(estadoUi.email) }.padding(8.dp),
                fontSize = 11.sp,
                color = Color.Gray
            )
        } else {
            OutlinedTextField(
                value = estadoUi.verificationCode,
                onValueChange = alCambiarCodigo,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("CÓDIGO") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (estadoUi.esRegistroEmail) "REGISTRARSE" else "ENTRAR",
            onClick = alClickLogin,
            backgroundColor = cianMate,
            enabled = !estadoUi.estaCargando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        )
    }
}

@Composable
fun BubbleTabItem(
    icon: Any, 
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, tween(300), label = "scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, tween(300), label = "alpha")

    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp).scale(scale),
        shape = CircleShape,
        color = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.2.dp, if (isSelected) accentColor else Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (icon) {
                is Int -> Icon(painterResource(id = icon), null, tint = Color.Unspecified, modifier = Modifier.size(18.dp).alpha(alpha))
                is String -> Text(text = icon, fontSize = 18.sp, modifier = Modifier.alpha(alpha))
            }
        }
    }
}

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
    val borderColor by animateColorAsState(if (isGranted) accentColor else Color.White.copy(alpha = 0.1f), tween(500), label = "border")
    val containerAlpha by animateFloatAsState(if (isGranted) 0.15f else 0.05f, tween(500), label = "alpha")

    Surface(
        onClick = if (isGranted) ({}) else onClick,
        color = Color.White.copy(alpha = containerAlpha),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(60.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    null, 
                    tint = if (isGranted) Color.White else accentColor, 
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                if (isGranted) {
                    Surface(color = accentColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text("ACEPTADO", color = accentColor, fontSize = 7.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                } else {
                    Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun BeLoginAssistant(
    modifier: Modifier = Modifier,
    cyberCyan: Color,
    cyberMagenta: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "be_login_anim")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )

    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }
    var currentEmotion by remember { mutableStateOf(EmocionBe.NORMAL) }

    LaunchedEffect(Unit) {
        while (true) {
            repeat(2) {
                targetPupilX = -6f; targetPupilY = -2f; delay(1000.milliseconds)
                targetPupilX = 6f; targetPupilY = -2f; delay(1000.milliseconds)
            }
            currentEmotion = EmocionBe.FELIZ; delay(1500.milliseconds)
            currentEmotion = EmocionBe.NORMAL
            targetPupilX = 0f; targetPupilY = 8f; delay(3500.milliseconds)
            targetPupilX = 0f; targetPupilY = 0f; delay(800.milliseconds)
        }
    }

    val pupilX by animateFloatAsState(targetPupilX, tween(700, easing = FastOutSlowInEasing), label = "pupilX")
    val pupilY by animateFloatAsState(targetPupilY, tween(700, easing = FastOutSlowInEasing), label = "pupilY")

    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..6000).random().toLong().milliseconds)
            isBlinking = true; delay(150.milliseconds); isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(if (isBlinking) 0.1f else 1f, tween(120), label = "blink")

    Box(modifier = modifier.offset { IntOffset(0, floatY.dp.roundToPx()) }.size(140.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(110.dp)) {
            drawCircle(Brush.radialGradient(listOf(cyberCyan.copy(alpha = 0.4f), cyberMagenta.copy(alpha = 0.1f), Color.Transparent)), radius = size.width / 1.1f)
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = size.width / 100f
            drawScale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                drawLine(cyberCyan, Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)
                
                drawCircle(Color(0xFF0A0E14), 38f, Offset(50f, 50f))
                drawCircle(cyberCyan, 34f, Offset(50f, 50f), style = Stroke(width = 4f))
                
                if (currentEmotion == EmocionBe.FELIZ) {
                    val happyPathLeft = Path().apply { moveTo(30f, 55f); quadraticTo(38f, 40f, 46f, 55f) }
                    val happyPathRight = Path().apply { moveTo(54f, 55f); quadraticTo(62f, 40f, 70f, 55f) }
                    drawPath(happyPathLeft, Color.White, style = Stroke(width = 4f, cap = StrokeCap.Round))
                    drawPath(happyPathRight, Color.White, style = Stroke(width = 4f, cap = StrokeCap.Round))
                } else {
                    drawOval(Color.White, Offset(30f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                    drawOval(Color.White, Offset(54f, 50f - (12f * eyeScaleY)), Size(16f, 24f * eyeScaleY))
                    drawCircle(Color(0xFF05070A), 5f * eyeScaleY, Offset(38f + pupilX, 50f + pupilY))
                    drawCircle(Color(0xFF05070A), 5f * eyeScaleY, Offset(62f + pupilX, 50f + pupilY))
                }
            }
        }
    }
}

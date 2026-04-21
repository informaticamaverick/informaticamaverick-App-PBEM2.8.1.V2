package com.example.myapplication.presentation.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.presentation.client.BeBrainViewModel
import com.example.myapplication.presentation.client.InitialNavTarget
import com.example.myapplication.presentation.components.Utilidades.MaverickBackgroundStrix
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

/**
 * --- STARTUP SCREEN (PANTALLA DE PRE-CARGA INTELIGENTE) ---
 * Esta pantalla se encarga de:
 * 1. Pedir permisos de ubicación y notificaciones.
 * 2. Verificar el estado inicial antes de entrar al Login.
 * 3. Ofrecer una experiencia premium desde el primer segundo.
 * 
 * El "Cerebro" (BeBrainViewModel) ahora controla los estados de permisos y navegación,
 * asegurando consistencia en toda la aplicación.
 */
@Composable
fun StartupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToProfileEdit: () -> Unit, // NUEVO TARGET MAVERICK V5
    beBrainViewModel: BeBrainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationPermissionGranted by beBrainViewModel.locationPermissionGranted.collectAsState()
    val notificationPermissionGranted by beBrainViewModel.notificationPermissionGranted.collectAsState()
    val navTarget by beBrainViewModel.initialNavTarget.collectAsState()
    val userName by beBrainViewModel.targetUserName.collectAsState()
    val isFirstTime by beBrainViewModel.isFirstTime.collectAsState()

    // --- SINCRONIZACIÓN CON EL OBRERO TÉCNICO (LOGINVIEWMODEL) ---
    val loginViewModel: LoginViewModel = hiltViewModel()
    val isWifiEnabled by loginViewModel.isWifiEnabled.collectAsState()
    val isCellularEnabled by loginViewModel.isCellularEnabled.collectAsState()
    val isGpsEnabled by loginViewModel.isGpsEnabled.collectAsState()

    // Sincronizamos los estados técnicos del obrero al cerebro
    LaunchedEffect(isWifiEnabled, isCellularEnabled, isGpsEnabled) {
        beBrainViewModel.setWifiEnabled(isWifiEnabled)
        beBrainViewModel.setCellularEnabled(isCellularEnabled)
        beBrainViewModel.setGpsEnabled(isGpsEnabled)
        beBrainViewModel.setOfflineStatus(!isWifiEnabled && !isCellularEnabled)
    }

    // Actualización periódica de hardware mientras estemos en esta pantalla
    LaunchedEffect(Unit) {
        while(true) {
            loginViewModel.refreshHardwareStatus()
            delay(2000) // Polling suave para la consola
        }
    }

    // Lanzadores de permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                      permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        beBrainViewModel.setLocationPermissionGranted(granted)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        beBrainViewModel.setNotificationPermissionGranted(isGranted)
    }

    // 1. Verificación inicial de permisos ya otorgados y arranque del Cerebro
    LaunchedEffect(Unit) {
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                         ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        beBrainViewModel.setLocationPermissionGranted(hasLocation)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotifications = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            beBrainViewModel.setNotificationPermissionGranted(hasNotifications)
        } else {
            beBrainViewModel.setNotificationPermissionGranted(true)
        }

        // El cerebro empieza a trabajar inmediatamente
        beBrainViewModel.performInitialAuthCheck()
    }

    // 2. Navegación inteligente: Solo ocurre cuando los permisos mínimos están listos Y el cerebro decidió
    LaunchedEffect(locationPermissionGranted, navTarget) {
        if (locationPermissionGranted && navTarget != InitialNavTarget.CHECKING) {
            // Esperamos un poco más para que Be termine de "leer"
            delay(4500) 
            
            // Si llegamos aquí con éxito, marcamos que ya no es la primera vez
            if (isFirstTime) {
                beBrainViewModel.completeFirstTime()
            }

            when (navTarget) {
                InitialNavTarget.LOGIN -> onNavigateToLogin()
                InitialNavTarget.MAIN_SCREEN -> onNavigateToMain()
                InitialNavTarget.PROFILE_EDIT -> onNavigateToProfileEdit()
                else -> {}
            }
        }
    }

    StartupScreenContent(
        locationPermissionGranted = locationPermissionGranted,
        notificationPermissionGranted = notificationPermissionGranted,
        navTarget = navTarget,
        userName = userName,
        isFirstTime = isFirstTime,
        isWifiEnabled = isWifiEnabled,
        isCellularEnabled = isCellularEnabled,
        isGpsEnabled = isGpsEnabled,
        onLocationPermissionClick = {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        onNotificationPermissionClick = {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    )
}

@Composable
fun StartupScreenContent(
    locationPermissionGranted: Boolean,
    notificationPermissionGranted: Boolean,
    navTarget: InitialNavTarget,
    userName: String,
    isFirstTime: Boolean,
    isWifiEnabled: Boolean,
    isCellularEnabled: Boolean,
    isGpsEnabled: Boolean,
    onLocationPermissionClick: () -> Unit,
    onNotificationPermissionClick: () -> Unit
) {
    // --- SECCIÓN: COLORES Y ESTILOS CYBERPUNK ---
    val cyberMagenta = Color(0xFFFF00FF)
    val cyberCyan = Color(0xFF00FFFF)
    val cyberDark = Color(0xFF0D0221)
    val cyberYellow = Color(0xFFFFF01F)
    val errorRed = Color(0xFFEF4444)

    // --- SECCIÓN: LÓGICA DE CONSOLA DE CÓDIGO BEM ---
    val codeLines = remember(isWifiEnabled, isCellularEnabled, isGpsEnabled) {
        listOf(
            "> INITIALIZING BEM PROTOCOL...",
            "> NETWORK CHECK: WIFI -> ${if (isWifiEnabled) "CONNECTED" else "DISCONNECTED"}",
            "> NETWORK CHECK: 4G/3G -> ${if (isCellularEnabled) "ACTIVE" else "INACTIVE"}",
            "> SENSOR CHECK: GPS -> ${if (isGpsEnabled) "ENABLED" else "DISABLED"}",
            "> CONNECTING TO FIREBASE CORE...",
            "> FETCHING CLOUD SERVICES...",
            "> LOADING PROVIDER DATABASE...",
            "> SYNCING LOCAL CACHE (ROOM)...",
            "> DECRYPTING USER IDENTITY...",
            "> SYSTEMS READY. WELCOME AGENT."
        )
    }
    var visibleLinesCount by remember { mutableIntStateOf(0) }
    
    // Si no es la primera vez, la consola corre de inmediato. 
    // Si es la primera vez, espera a los permisos.
    LaunchedEffect(locationPermissionGranted, notificationPermissionGranted, isFirstTime) {
        if (!isFirstTime || (locationPermissionGranted && notificationPermissionGranted)) {
            while (visibleLinesCount < codeLines.size) {
                delay(400) // Velocidad de lectura para Be
                visibleLinesCount++
            }
        }
    }

    // --- SECCIÓN: ANIMACIÓN DEL LÁPIZ (WRITING EFFECT) ---
    val infiniteTransition = rememberInfiniteTransition(label = "pencil")
    val pencilX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pencilX"
    )

    MaverickBackgroundStrix {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp).fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // --- SECCIÓN: TÍTULO BEM ---
            Text(
                text = "BEM",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 16.sp,
                    shadow = Shadow(color = cyberCyan, blurRadius = 15f)
                )
            )
            
            Text(
                text = "CONECTANDO A PERSONAS",
                style = TextStyle(
                    color = cyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp,
                    shadow = Shadow(color = cyberCyan.copy(alpha = 0.5f), blurRadius = 4f)
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // --- SECCIÓN: ASISTENTE BE (ANIMACIÓN DE LECTURA) ---
            BeStartupAssistant(
                cyberCyan = cyberCyan,
                cyberMagenta = cyberMagenta,
                isReading = visibleLinesCount < codeLines.size
            )

            Spacer(modifier = Modifier.height(24.dp)) // Reducido para subir la consola

            // --- SECCIÓN: CONSOLA DE CARGA ESTILO CÓDIGO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // Título fuera de la caja
                Text(
                    text = "CONSOLE_MAVERICK V5",
                    color = cyberCyan.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                )

                val progress = if (codeLines.isEmpty()) 0f else visibleLinesCount.toFloat() / codeLines.size.toFloat()
                val totalSteps = 30
                val filledSteps = (progress * totalSteps).toInt()
                val loadingBarText = "Loading Sys " + "/".repeat(filledSteps) + ".".repeat(totalSteps - filledSteps) + " ${(progress * 100).toInt()}%"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(300))
                        .drawBehind {
                            // Fondo oscuro semitransparente
                            drawRect(color = Color(0xFF0D0221).copy(alpha = 0.85f))
                            
                            // Borde de neón sutil
                            val neonColor = cyberCyan.copy(alpha = 0.3f)
                            drawRect(
                                color = neonColor,
                                style = Stroke(width = 1.dp.toPx())
                            )
                            
                            // Esquinas reforzadas estilo CyberMaverickNeonBoxHeader
                            val L = 15.dp.toPx()
                            val sw = 3.dp.toPx()
                            // Top-Left
                            drawLine(cyberCyan, Offset(0f, 0f), Offset(L, 0f), sw)
                            drawLine(cyberCyan, Offset(0f, 0f), Offset(0f, L), sw)
                            // Bottom-Right
                            drawLine(cyberCyan, Offset(size.width, size.height), Offset(size.width - L, size.height), sw)
                            drawLine(cyberCyan, Offset(size.width, size.height), Offset(size.width, size.height - L), sw)
                            
                            // Detalle extra premium: línea de escaneo superior
                            drawLine(
                                brush = Brush.horizontalGradient(listOf(Color.Transparent, cyberCyan, Color.Transparent)),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(16.dp)
                ) {
                    Column {
                        // Barra de progreso interactiva
                        Text(
                            text = loadingBarText,
                            color = if (progress < 1f) cyberCyan else cyberYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Líneas de código
                        codeLines.take(visibleLinesCount).forEach { line ->
                            val isError = line.contains("DISCONNECTED") || line.contains("INACTIVE") || line.contains("DISABLED")
                            Text(
                                text = line,
                                color = when {
                                    isError -> errorRed
                                    line.contains("READY") -> cyberYellow
                                    else -> cyberCyan.copy(alpha = 0.9f)
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isError) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        if (visibleLinesCount < codeLines.size) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    text = "✍️",
                                    fontSize = 14.sp,
                                    modifier = Modifier.offset(x = pencilX.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System processing...",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // UI de Solicitud de Permisos (DEBAJO DE LA CONSOLA Y SOLO SI ES LA PRIMERA VEZ)
            if (isFirstTime) {
                Spacer(modifier = Modifier.height(24.dp))
                
                if (!locationPermissionGranted) {
                    StartupPermissionCard(
                        title = "Ubicación Táctica",
                        description = "Necesaria para el Radar BEM y servicios FAST.",
                        icon = Icons.Default.Place,
                        onClick = onLocationPermissionClick
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                    StartupPermissionCard(
                        title = "Notificaciones",
                        description = "Recibe alertas de presupuestos y mensajes en tiempo real.",
                        icon = Icons.Default.Notifications,
                        onClick = onNotificationPermissionClick
                    )
                }
            }
        }

        // --- SECCIÓN: FOOTER CIBERPUNK ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BUSCAR SE ESCRIBE CON BE",
                style = TextStyle(
                    color = cyberCyan.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MAVERICK PBEN 2.8.1",
                style = TextStyle(
                    color = cyberCyan.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

@Composable
fun BeStartupAssistant(
    modifier: Modifier = Modifier,
    cyberCyan: Color,
    cyberMagenta: Color,
    isReading: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "be_startup_anim")
    
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Estados de la mirada
    var targetPupilX by remember { mutableFloatStateOf(0f) }
    var targetPupilY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isReading) {
        while (true) {
            if (isReading) {
                // MODO LECTURA: Mira abajo (Y=8f) y escanea horizontalmente
                targetPupilY = 8f
                targetPupilX = -6f
                delay(600)
                targetPupilX = 6f
                delay(600)
            } else {
                // FIN DE CARGA: Mira al frente
                targetPupilY = 0f
                targetPupilX = 0f
                delay(2000)
            }
        }
    }

    val pupilX by animateFloatAsState(targetPupilX, tween(500, easing = FastOutSlowInEasing), label = "pupilX")
    val pupilY by animateFloatAsState(targetPupilY, tween(500, easing = FastOutSlowInEasing), label = "pupilY")

    // Animación de parpadeo
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2500..5000).random().toLong())
            isBlinking = true
            delay(150)
            isBlinking = false
        }
    }
    val eyeScaleY by animateFloatAsState(if (isBlinking) 0.1f else 1f, tween(120), label = "blink")

    Box(
        modifier = modifier
            .offset(y = floatY.dp)
            .size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        // Resplandor neón
        Canvas(modifier = Modifier.size(100.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cyberCyan.copy(alpha = 0.3f), Color.Transparent)
                ),
                radius = size.width / 1.2f
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = size.width / 100f
            scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
                // ==========================================================================================
                // --- SECCIÓN: MANGO DE LA LUPA (NUEVO) ---
                // ==========================================================================================
                // Dibujamos el mando antes que la cabeza para que quede por detrás
                drawLine(Color(0xFF020408).copy(alpha = 0.6f), Offset(70f, 70f), Offset(94.5f, 94.5f), 16f, StrokeCap.Round)
                drawLine(Color(0xFF1E293B), Offset(70f, 70f), Offset(95f, 95f), 14f, StrokeCap.Round)
                drawLine(cyberCyan, Offset(73f, 73f), Offset(92f, 92f), 10f, StrokeCap.Round)
                drawLine(cyberCyan.copy(alpha = 0.4f), Offset(76f, 76f), Offset(89f, 89f), 6f, StrokeCap.Round)

                // ==========================================================================================
                // --- SECCIÓN: CABEZA DE ASISTENTE (LENTE) ---
                // ==========================================================================================
                // Casco Base de Be
                drawCircle(Color(0xFF0A0E14), 38f, Offset(50f, 50f))
                
                // Anillo Neón
                drawCircle(
                    color = cyberCyan,
                    radius = 34f,
                    center = Offset(50f, 50f),
                    style = Stroke(width = 3.5f)
                )
                
                // Brillo visor
                drawArc(
                    color = Color.White.copy(alpha = 0.2f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round),
                    topLeft = Offset(22f, 22f),
                    size = Size(56f, 56f)
                )

                // Ojos
                drawOval(
                    color = Color.White,
                    topLeft = Offset(31f, 50f - (11f * eyeScaleY)),
                    size = Size(15f, 22f * eyeScaleY)
                )
                drawOval(
                    color = Color.White,
                    topLeft = Offset(54f, 50f - (11f * eyeScaleY)),
                    size = Size(15f, 22f * eyeScaleY)
                )
                
                // Pupilas móviles (LÓGICA DE LECTURA HACIA ABAJO)
                val pupilRadius = 4.5f
                drawCircle(
                    color = Color(0xFF05070A),
                    radius = pupilRadius * eyeScaleY,
                    center = Offset(38.5f + pupilX, 50f + pupilY)
                )
                drawCircle(
                    color = Color(0xFF05070A),
                    radius = pupilRadius * eyeScaleY,
                    center = Offset(61.5f + pupilX, 50f + pupilY)
                )
                
                // Brillo pupilas
                drawCircle(
                    color = Color.White,
                    radius = 1.2f * eyeScaleY,
                    center = Offset(39.5f + pupilX, 48.5f + pupilY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 1.2f * eyeScaleY,
                    center = Offset(62.5f + pupilX, 48.5f + pupilY)
                )
            }
        }
    }
}

@Composable
fun StartupPermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF22D3EE)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview_NoLocation() {
    MyApplicationTheme {
        StartupScreenContent(
            locationPermissionGranted = false,
            notificationPermissionGranted = false,
            navTarget = InitialNavTarget.CHECKING,
            userName = "Usuario Maverick",
            isFirstTime = true,
            isWifiEnabled = true,
            isCellularEnabled = true,
            isGpsEnabled = false,
            onLocationPermissionClick = {},
            onNotificationPermissionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview_NoNotification() {
    MyApplicationTheme {
        StartupScreenContent(
            locationPermissionGranted = true,
            notificationPermissionGranted = false,
            navTarget = InitialNavTarget.CHECKING,
            userName = "Usuario Maverick",
            isFirstTime = true,
            isWifiEnabled = true,
            isCellularEnabled = true,
            isGpsEnabled = true,
            onLocationPermissionClick = {},
            onNotificationPermissionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview_Syncing() {
    MyApplicationTheme {
        StartupScreenContent(
            locationPermissionGranted = true,
            notificationPermissionGranted = true,
            navTarget = InitialNavTarget.CHECKING,
            userName = "Usuario Maverick",
            isFirstTime = true,
            isWifiEnabled = false,
            isCellularEnabled = true,
            isGpsEnabled = true,
            onLocationPermissionClick = {},
            onNotificationPermissionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview_Ready() {
    MyApplicationTheme {
        StartupScreenContent(
            locationPermissionGranted = true,
            notificationPermissionGranted = true,
            navTarget = InitialNavTarget.MAIN_SCREEN,
            userName = "Usuario Maverick",
            isFirstTime = false,
            isWifiEnabled = true,
            isCellularEnabled = true,
            isGpsEnabled = true,
            onLocationPermissionClick = {},
            onNotificationPermissionClick = {}
        )
    }
}

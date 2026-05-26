package com.example.myapplication.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.myapplication.presentation.client.BeBrainViewModel
//import com.example.myapplication.presentation.client.BeInteractionViewModel
import com.example.myapplication.data.local.UserEntity
import com.example.myapplication.data.repository.ForecastDay
import com.example.myapplication.presentation.client.LocationOption
import com.example.myapplication.presentation.client.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.data.model.CompanyClient
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.content.Context
import com.example.myapplication.presentation.client.UbicacionClimaViewModel
import com.example.myapplication.presentation.components.Utilidades.CPCyberColors
import com.example.myapplication.presentation.components.Utilidades.GeminiCyberWrapper

// ==================================================================================
// --- SECCIÓN 1: COMPONENTES DE CABECERA PRINCIPAL ---
// ==================================================================================

@Composable
fun TopHeaderSection(
    navController: NavHostController,
    user: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    currentLocationState: LocationOption,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit,
    beViewModel: BeBrainViewModel,
    // interactionViewModel: BeInteractionViewModel, // SE DEJA FUERA SEGUN PLAN DE ACCION
    onResultClick: (Any) -> Unit = {}
) {
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    TopHeaderSectionContent(
        navController = navController,
        user = user,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        currentLocationState = currentLocationState,
        onWeatherClick = onWeatherClick,
        onRefreshLocation = onRefreshLocation,
        onLocationSelected = onLocationSelected,
        onLogout = onLogout,
        userFromBrain = userFromBrain,
        onResultClick = onResultClick
    )
}

@Composable
fun TopHeaderSectionContent(
    navController: NavHostController,
    user: UserEntity?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    currentLocationState: LocationOption,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    onLogout: () -> Unit,
    userFromBrain: UserEntity?,
    onResultClick: (Any) -> Unit = {}
) {
    val cardGradientBrush = Brush.verticalGradient(
        listOf(Color(0xFF1A1A24).copy(alpha = 0.9f), Color(0xFF0D0D12).copy(alpha = 1f))
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                .background(CPCyberColors.DeepVoid.copy(alpha = 0.98f))
                .statusBarsPadding() // Empuja el contenido hacia abajo de la muesca
                .height(95.dp) // Altura fija para el contenido (evita que se estire)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val cornerSize = 10.dp.toPx()
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(
                            CPCyberColors.ElectricPurple,
                            CPCyberColors.MaverickCyan,
                            CPCyberColors.ElectricPurple
                        )
                    )

                    // Path que define los bordes laterales y la base: Lateral Izq -> Corte Izq -> Base -> Corte Der -> Lateral Der
                    val path = Path().apply {
                        moveTo(0f, 200f)
                        lineTo(0f, size.height - cornerSize)
                        lineTo(cornerSize, size.height)
                        lineTo(size.width - cornerSize, size.height)
                        lineTo(size.width, size.height - cornerSize)
                        lineTo(size.width, 200f)
                    }

                    // 1. Efecto Glow (Resplandor central en la base)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(CPCyberColors.MaverickCyan.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(size.width / 2, size.height),
                            radius = size.width / 2
                        ),
                        alpha = 0.4f
                    )

                    // 2. Línea principal con el gradiente (Bordas laterales y base)
                    drawPath(
                        path = path,
                        brush = gradient,
                        style = Stroke(width = strokeWidth)
                    )
                }
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- TARJETA DE CLIMA (Izquierda) ---
                WeatherWidget(
                    temp = temperature,
                    emoji = weatherEmoji,
                    city = cityName,
                    onClick = onWeatherClick,
                    brush = cardGradientBrush,
                    modifier = Modifier.width(95.dp).fillMaxHeight()
                )

                // --- TARJETA DE DIRECCIONES (Centro - Adaptable) ---
                LocationSelector(
                    user = user,
                    currentLocation = currentLocationState,
                    onRefresh = onRefreshLocation,
                    onLocationSelected = onLocationSelected,
                    brush = cardGradientBrush,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                // --- TARJETA DE PERFIL (Derecha) ---
                ProfileSectionContent(
                    user = user,
                    userFromBrain = userFromBrain,
                    navController = navController,
                    onLogout = onLogout,
                    brush = cardGradientBrush,
                    onResultClick = onResultClick,
                    modifier = Modifier.width(95.dp).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun WeatherWidget(temp: String, emoji: String, city: String, onClick: () -> Unit, brush: Brush, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(temp, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
fun LocationSelector(
    user: UserEntity?,
    currentLocation: LocationOption,
    onRefresh: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    brush: Brush,
    modifier: Modifier = Modifier
) {
    var showPopup by remember { mutableStateOf(false) }

    // --- ANIMACIÓN DE ROTACIÓN (ESTILO M3 / ANDROID 16) ---
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GpsRotation"
    )

    // --- SECCIÓN: DESGLOSE DINÁMICO DE UBICACIÓN ---
    val (linea1, linea2, linea3) = when (currentLocation) {
        is LocationOption.Gps -> {
            Triple(
                "UBICACIÓN ACTUAL", 
                currentLocation.address.ifBlank { "Buscando..." }, 
                "${currentLocation.locality}, ${currentLocation.province}".trim().trim { it == ',' }
            )
        }
        is LocationOption.Personal -> Triple("MI CASA / PERSONAL", "${currentLocation.address} ${currentLocation.number}", currentLocation.locality)
        is LocationOption.Business -> Triple(currentLocation.companyName.uppercase(), currentLocation.branchName, "${currentLocation.address} ${currentLocation.number}")
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
                .clickable { showPopup = true }
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(start = 12.dp, top = 4.dp, bottom = 6.dp, end = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                Text(text = linea1, fontSize = 9.sp, fontWeight = FontWeight.Black, color = CPCyberColors.MaverickCyan, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = linea2, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = linea3, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        // Icono de GPS estilizado y sobresaliendo (Como en tu imagen)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (4).dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D1117).copy(alpha = 0.8f))
                .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f), CircleShape)
                .clickable { 
                    rotationAngle += 360f // Gira hacia la derecha
                    onRefresh() 
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation, 
                contentDescription = "Refresh GPS", 
                tint = Color(0xFF22D3EE), 
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation)
            )
        }
    }


    if (showPopup) {
        Dialog(
            onDismissRequest = { showPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch {
                    delay(300) // Tiempo para la animación de salida
                    showPopup = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.6f))
                    .clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 60.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LocationPopup(
                            user = user,
                            onClose = { closeWithAnimation() },
                            onRefresh = { onRefresh(); closeWithAnimation() },
                            onLocationSelected = { onLocationSelected(it); closeWithAnimation() },
                            currentLocation = currentLocation
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    user: UserEntity?,
    navController: NavHostController,
    onLogout: () -> Unit,
    brush: Brush,
    beViewModel: BeBrainViewModel,
    //interactionViewModel: BeInteractionViewModel,
    onResultClick: (Any) -> Unit = {}
) {
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    ProfileSectionContent(
        user = user,
        userFromBrain = userFromBrain,
        navController = navController,
        onLogout = onLogout,
        brush = brush,
        onResultClick = onResultClick
    )
}

@Composable
fun ProfileSectionContent(
    modifier: Modifier = Modifier,
    user: UserEntity?,
    userFromBrain: UserEntity?,
    navController: NavHostController,
    onLogout: () -> Unit,
    brush: Brush,
    onResultClick: (Any) -> Unit = {}
) {
    var showPopup by remember { mutableStateOf(false) }
    val finalUser = userFromBrain ?: user

    val displayName = remember(finalUser) {
        finalUser?.name?.ifBlank { null }?.trim()?.split(" ")?.firstOrNull()?.uppercase()
            ?: finalUser?.displayName?.split(" ")?.firstOrNull()?.uppercase()
            ?: "PERFIL"
    }



    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
                .clickable { showPopup = true }
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(40.dp)) {
                    if (finalUser?.photoUrl != null) {
                        AsyncImage(model = finalUser.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.5.dp, Color(0xFF22D3EE), CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.fillMaxSize().padding(1.dp))
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(text = displayName, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }


    if (showPopup && finalUser != null) {
        Dialog(
            onDismissRequest = { showPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch {
                    delay(300)
                    showPopup = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.6f))
                    .clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 60.dp, end = 16.dp)
                        // --- SECCIÓN: CONFIGURACIÓN DE ANCHO DEL POPUP ---
                        // Se aumenta de 340.dp a 380.dp para permitir visualización de 3 empresas simultáneas
                        .width(380.dp)
                        .clickable(enabled = false) {}
                ) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        UserProfilePopup(
                            user = finalUser,
                            onClose = { closeWithAnimation() },
                            onLogout = { closeWithAnimation(); onLogout() },
                            onProfileClick = { closeWithAnimation(); navController.navigate(Screen.PerfilCliente.route) }
                        )
                    }
                }
            }
        }
    }
}

// ==================================================================================
// --- SECCIÓN 2: POPUPS Y ÁRBOLES (MEJORADOS) ---
// ==================================================================================

@Composable
fun UserProfilePopup(user: UserEntity, onClose: () -> Unit, onLogout: () -> Unit, onProfileClick: () -> Unit) {
    val cyberCyan = Color(0xFF22D3EE)
    val cyberPurple = Color(0xFF9B51E0)
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f) // Fondo más oscuro y limpio

    GeminiCyberWrapper(modifier = Modifier.fillMaxWidth().padding(16.dp), cornerRadius = 8.dp, isAnimated = true, showGlow = true) {
        Column(
            modifier = Modifier.background(deepGlass).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header del Popup
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "SYS_ID // PROFILE", color = cyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(cyberCyan.copy(0.1f)).clickable { /* QR */ }.padding(6.dp)) {
                        Icon(Icons.Default.QrCode2, null, tint = cyberCyan, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Close, null, tint = Color.White.copy(0.5f)) }
                }
            }
            Spacer(Modifier.height(32.dp))

            // Avatar
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp).clickable { onProfileClick() }) {
                Box(modifier = Modifier.fillMaxSize().background(cyberCyan.copy(0.05f), CircleShape).border(1.dp, cyberCyan.copy(0.3f), CircleShape))
                AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(2.dp, cyberCyan, CircleShape), contentScale = ContentScale.Crop)
            }
            Spacer(Modifier.height(20.dp))
            Text("${user.name} ${user.lastName}".uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            Text(user.email, color = Color.White.copy(0.5f), fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = cyberCyan.copy(0.2f), thickness = 1.dp)
            Spacer(Modifier.height(24.dp))

            // ==========================================
            // --- SECCIÓN: NETWORK / EMPRESAS ASOCIADAS ---
            // ==========================================
            if (user.companies.isNotEmpty()) {
                Text("NETWORK / ENTIDADES ASOCIADAS", color = cyberPurple, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(16.dp))
                
                // Contenedor de empresas con ancho optimizado para visualizar hasta 3 elementos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    user.companies.forEach { company ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                            // Slot de Imagen de Empresa
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cyberPurple.copy(0.1f))
                                    .border(1.dp, cyberPurple.copy(0.4f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (company.photoUrl != null) { 
                                    AsyncImage(model = company.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop) 
                                } else { 
                                    Icon(Icons.Default.Business, null, tint = cyberPurple, modifier = Modifier.size(28.dp)) 
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            // Nombre de la Empresa
                            Text(text = company.name.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // Botón Desconectar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFF1744).copy(0.15f))
                    .border(1.dp, Color(0xFFFF1744).copy(0.5f), RoundedCornerShape(16.dp))
                    .clickable { onLogout() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, null, tint = Color(0xFFFF1744), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("DESCONECTAR SESIÓN", color = Color(0xFFFF1744), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.5.sp)
                }
            }
        }
    }
}

@Composable
fun LocationPopup(
    user: UserEntity?,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    currentLocation: LocationOption
) {
    val cyberCyan = Color(0xFF22D3EE)
    val cyberPurple = Color(0xFF9B51E0)
    var isRefreshing by remember { mutableStateOf(false) }

    // --- SECCIÓN: ANIMACIÓN DE ROTACIÓN GPS ---
    val rotationAnim by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(0)
        },
        label = "GpsRotation"
    )

    GeminiCyberWrapper(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        cornerRadius = 8.dp,
        isAnimated = true,
        showGlow = true
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFF0B0F19).copy(0.98f))
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .padding(24.dp)
        ) {

            // ======================================================================================
            // --- SECCIÓN 1: ENCABEZADO Y CONTROL GPS ---
            // ======================================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "DIRECCIÓN ACTUAL",
                        color = cyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (currentLocation is LocationOption.Gps) "Ubicación detectada vía Satélite" else "Dirección guardada en perfil",
                        color = Color.White.copy(0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // --- ICONO GPS CON ANIMACIÓN ---
                IconButton(
                    onClick = {
                        isRefreshing = true
                        onRefresh()
                        // Simulamos fin de carga para detener rotación (El Obrero lo hace real)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        null,
                        tint = cyberCyan,
                        modifier = Modifier.rotate(rotationAnim)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = cyberCyan.copy(0.2f), thickness = 1.dp)
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // ======================================================================================
                // --- SECCIÓN 2: DESGLOSE DE DIRECCIÓN ACTIVA (DINÁMICO) ---
                // ======================================================================================
                val (address, locality, province, country, cp) = when (currentLocation) {
                    is LocationOption.Gps -> Quintuple(
                        currentLocation.address.ifBlank { "Detectando..." },
                        currentLocation.locality,
                        currentLocation.province,
                        currentLocation.country,
                        currentLocation.postalCode
                    )
                    is LocationOption.Personal -> Quintuple(
                        "${currentLocation.address} ${currentLocation.number}",
                        currentLocation.locality,
                        currentLocation.province,
                        currentLocation.country,
                        currentLocation.postalCode
                    )
                    is LocationOption.Business -> Quintuple(
                        "${currentLocation.address} ${currentLocation.number}",
                        currentLocation.locality,
                        currentLocation.province,
                        currentLocation.country,
                        currentLocation.postalCode
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cyberCyan.copy(0.05f))
                        .border(1.dp, cyberCyan.copy(0.3f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    LocationInfoRow("DIR:", address, cyberCyan)
                    LocationInfoRow("LOC:", locality, cyberCyan)
                    LocationInfoRow("PRV:", province.ifBlank { "---" }, cyberCyan)
                    LocationInfoRow("PAÍS:", country.ifBlank { "ARGENTINA" }, cyberCyan)
                    LocationInfoRow("C.P:", cp.ifBlank { "---" }, cyberCyan)
                }

                Spacer(Modifier.height(32.dp))

                // ======================================================================================
                // --- SECCIÓN 3: DIRECTORIOS DE UBICACIÓN (PERSISTENCIA) ---
                // ======================================================================================
                if (user != null) {
                    var pExp by remember { mutableStateOf(true) }
                    var bExp by remember { mutableStateOf(true) }
                    Text(
                        "MIS LUGARES GUARDADOS",
                        color = Color.White.copy(0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    CyberTreeDirectory(
                        "DIRECCIONES PERSONALES",
                        Icons.Default.FolderOpen,
                        cyberCyan,
                        pExp,
                        onToggle = { pExp = !pExp }) {
                        user.personalAddresses.forEach { addr ->
                            CyberTreeLeaf(
                                Icons.Default.LocationOn,
                                "${addr.calle} ${addr.numero}",
                                "${addr.localidad}, ${addr.provincia}",
                                cyberCyan
                            ) {
                                // Sincronizamos con el Obrero usando el ID persistente
                                onLocationSelected(
                                    LocationOption.Personal(
                                        addr.calle,
                                        addr.numero,
                                        addr.localidad,
                                        addr.provincia,
                                        "Argentina",
                                        addr.codigoPostal,
                                        id = addr.id
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (user.companies.isNotEmpty()) {
                        CyberTreeDirectory(
                            "MIS EMPRESAS",
                            Icons.Default.Business,
                            cyberPurple,
                            bExp,
                            onToggle = { bExp = !bExp }) {
                            user.companies.forEach { company ->
                                var cExp by remember { mutableStateOf(false) }
                                CyberTreeDirectory(
                                    company.name.uppercase(),
                                    Icons.Default.Storefront,
                                    cyberPurple.copy(0.8f),
                                    cExp,
                                    isNested = true,
                                    onToggle = { cExp = !cExp }) {
                                    company.branches.forEach { branch ->
                                        CyberTreeLeaf(
                                            Icons.Default.Map,
                                            branch.name,
                                            "${branch.address.calle} ${branch.address.numero}",
                                            cyberPurple
                                        ) {
                                            // Sincronizamos con el Obrero usando el ID persistente de la sucursal
                                            onLocationSelected(
                                                LocationOption.Business(
                                                    company.name,
                                                    branch.name,
                                                    branch.address.calle,
                                                    branch.address.numero,
                                                    branch.address.localidad,
                                                    branch.address.provincia,
                                                    "Argentina",
                                                    branch.address.codigoPostal,
                                                    id = branch.id
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Efecto para resetear la animación cuando el Obrero termina (simulado aquí, real vía state)
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(2000)
            isRefreshing = false
        }
    }
}

// Helper data class for location breakdown
data class Quintuple<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)

@Composable
fun LocationInfoRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(45.dp))
        Text(text = value.uppercase(), color = Color.White.copy(0.9f), fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CyberTreeDirectory(title: String, icon: ImageVector, accentColor: Color, isExpanded: Boolean, isNested: Boolean = false, onToggle: () -> Unit, content: @Composable () -> Unit) {
    val rot by animateFloatAsState(if (isExpanded) 90f else 0f)
    Column(modifier = Modifier.padding(start = if (isNested) 20.dp else 0.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onToggle() }.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ChevronRight, null, tint = accentColor, modifier = Modifier.size(18.dp).rotate(rot))
            Spacer(Modifier.width(12.dp))
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        }
        AnimatedVisibility(visible = isExpanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Box(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp).drawWithCache { onDrawWithContent { drawLine(accentColor.copy(0.3f), Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx()); drawContent() } }) { Column { content() } }
        }
    }
}

@Composable
fun CyberTreeLeaf(icon: ImageVector, title: String, subtitle: String, accentColor: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp).drawWithCache { onDrawWithContent { drawLine(accentColor.copy(0.3f), Offset(0f, size.height/2), Offset(24.dp.toPx(), size.height/2), 1.dp.toPx()); drawContent() } }.padding(start = 32.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(accentColor.copy(0.1f), RoundedCornerShape(10.dp)).border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = Color.White.copy(0.5f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherExpandedCard(temperature: String, weatherEmoji: String, weatherDescription: String, cityName: String, forecastDays: List<ForecastDay>) {
    GeminiCyberWrapper(modifier = Modifier.fillMaxWidth().padding(16.dp), cornerRadius = 8.dp, isAnimated = true, showGlow = true) {
        Column(modifier = Modifier.background(Color(0xFF0B0F19).copy(0.98f)).fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SYS_WTHR // ATMÓSFERA", color = Color(0xFF22D3EE), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(24.dp))
            Text(cityName.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            Text(weatherDescription.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE).copy(0.8f), letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(weatherEmoji, fontSize = 72.sp)
                Spacer(modifier = Modifier.width(24.dp))
                Text(temperature, fontSize = 72.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            // Si deseas luego mapear `forecastDays` puedes usar una Row con cajas estilizadas aquí abajo.
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun TopHeaderSectionPreview() {
    val mockUser = UserEntity(
        id = "1",
        name = "Juan",
        lastName = "Pérez",
        displayName = "JUAN",
        email = "juan.perez@example.com",
        photoUrl = null,
        companies = listOf(
            CompanyClient(
                name = "Maverick Tech",
                photoUrl = null
            )
        )
    )
    val mockLocation = LocationOption.Gps(
        address = "Calle Falsa 123",
        locality = "Tucumán",
        province = "Tucumán",
        postalCode = "4000"
    )

    MyApplicationTheme {
        TopHeaderSectionContent(
            navController = rememberNavController(),
            user = mockUser,
            temperature = "24°C",
            weatherEmoji = "☀️",
            weatherDescription = "Despejado",
            cityName = "Tucumán",
            currentLocationState = mockLocation,
            onWeatherClick = {},
            onRefreshLocation = {},
            onLocationSelected = {},
            onLogout = {},
            userFromBrain = mockUser,
            onResultClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun UserProfilePopupPreview() {
    val mockUser = UserEntity(
        id = "1",
        name = "Juan",
        lastName = "Pérez",
        displayName = "JUAN",
        email = "juan.perez@example.com",
        photoUrl = null,
        companies = listOf(
            CompanyClient(
                name = "Maverick Tech",
                photoUrl = null
            )
        )
    )

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            UserProfilePopup(
                user = mockUser,
                onClose = {},
                onLogout = {},
                onProfileClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun LocationPopupPreview() {
    val mockUser = UserEntity(
        id = "1",
        name = "Juan",
        lastName = "Pérez",
        displayName = "JUAN",
        email = "juan.perez@example.com",
        photoUrl = null
    )
    val mockLocation = LocationOption.Gps(
        address = "Calle Falsa 123",
        locality = "Tucumán",
        province = "Tucumán",
        postalCode = "4000"
    )

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            LocationPopup(
                user = mockUser,
                onClose = {},
                onRefresh = {},
                onLocationSelected = {},
                currentLocation = mockLocation
            )
        }
    }
}


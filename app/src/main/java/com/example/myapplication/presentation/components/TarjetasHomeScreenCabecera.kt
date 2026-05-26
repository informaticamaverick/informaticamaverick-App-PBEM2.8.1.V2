package com.example.myapplication.presentation.components

import android.os.Build
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.core.data.local.entity.UserEntity
//import com.example.myapplication.core.data.repository.ForecastDay
import com.example.myapplication.presentation.features.home.LocationOption
import com.example.myapplication.presentation.features.home.Screen
import com.example.myapplication.presentation.features.home.UbicacionClimaViewModel
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.presentation.designsystem.components.GeminiCyberWrapper
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.core.common.QRUtils

// ==================================================================================
// --- SECCIÓN 1: ORQUESTADORES DE CABECERA (SMART) ---
// ==================================================================================

/**
 * TopHeaderSection: Componente inteligente que sincroniza el HUD superior.
 * Sigue la Regla de Oro: Recolecta el estado del Cerebro (BeBrain) y emite eventos.
 */
@Composable
fun TopHeaderSection(
    navController: NavHostController,
    beViewModel: BeBrainViewModel,
    ubicacionObrero: UbicacionClimaViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    
    // --- SUSCRIPCIÓN AL CEREBRO (Elite SSOT) ---
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val activeName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val activePhoto by beViewModel.activeProfilePhotoUrl.collectAsStateWithLifecycle()
    val temperature by beViewModel.temperature.collectAsStateWithLifecycle()
    val weatherEmoji by beViewModel.weatherEmoji.collectAsStateWithLifecycle()
    val weatherDescription by beViewModel.weatherDescription.collectAsStateWithLifecycle()
    val currentLocationState by beViewModel.selectedLocation.collectAsStateWithLifecycle()

    TopHeaderSectionContent(
        navController = navController,
        user = userFromBrain,
        activeName = activeName,
        activePhoto = activePhoto,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        currentLocationState = currentLocationState ?: LocationOption.Gps(address = "Buscando...", locality = "Detectando..."),
        onWeatherClick = { beViewModel.toggleWeatherDetails() },
        onRefreshLocation = { 
            if (ubicacionObrero.isGpsHabilitado(context)) {
                ubicacionObrero.ejecutarCalculoUbicacionGps(context) 
            } else {
                Toast.makeText(context, "Activa el GPS para actualizar", Toast.LENGTH_SHORT).show()
            }
        },
        onLocationSelected = { option -> 
            val id = when(option) {
                is LocationOption.Gps -> "gps_current"
                is LocationOption.Personal -> option.id
                is LocationOption.Business -> option.id
            }
            beViewModel.selectAddress(id) 
        },
        onLogout = onLogout,
        userFromBrain = userFromBrain
    )
}

/**
 * TopHeaderSectionV2: Versión evolucionada (Elite) de la cabecera.
 * Implementa una estética de cápsula flotante con Glassmorphism y diseño orgánico.
 */
@Composable
fun TopHeaderSectionV2(
    navController: NavHostController,
    beViewModel: BeBrainViewModel,
    ubicacionObrero: UbicacionClimaViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    
    // --- SUSCRIPCIÓN AL CEREBRO (Elite SSOT) ---
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val activeName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val activePhoto by beViewModel.activeProfilePhotoUrl.collectAsStateWithLifecycle()
    val selectedProfileId by beViewModel.selectedProfileId.collectAsStateWithLifecycle()
    val temperature by beViewModel.temperature.collectAsStateWithLifecycle()
    val weatherEmoji by beViewModel.weatherEmoji.collectAsStateWithLifecycle()
    val weatherDescription by beViewModel.weatherDescription.collectAsStateWithLifecycle()
    val currentLocationState by beViewModel.selectedLocation.collectAsStateWithLifecycle()

    TopHeaderSectionContentV2(
        navController = navController,
        user = userFromBrain,
        activeName = activeName,
        activePhoto = activePhoto,
        isPersonalProfile = selectedProfileId == null,
        selectedProfileId = selectedProfileId,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        currentLocationState = currentLocationState ?: LocationOption.Gps(address = "Buscando...", locality = "Detectando..."),
        onWeatherClick = { beViewModel.toggleWeatherDetails() },
        onRefreshLocation = { 
            if (ubicacionObrero.isGpsHabilitado(context)) {
                ubicacionObrero.ejecutarCalculoUbicacionGps(context) 
            } else {
                Toast.makeText(context, "Activa el GPS para actualizar", Toast.LENGTH_SHORT).show()
            }
        },
        onLocationSelected = { option: LocationOption ->
            val id = when(option) {
                is LocationOption.Gps -> "gps_current"
                is LocationOption.Personal -> option.id
                is LocationOption.Business -> option.id
            }
            beViewModel.selectAddress(id) 
        },
        onProfileSelected = { profileId -> beViewModel.selectProfile(profileId) },
        onLogout = onLogout,
        userFromBrain = userFromBrain
    )
}

/**
 * ProfileSection: Orquestador inteligente para el slot de perfil.
 */
@Composable
fun ProfileSection(
    navController: NavHostController,
    beViewModel: BeBrainViewModel,
    onLogout: () -> Unit,
    brush: Brush
) {
    val userFromBrain by beViewModel.userState.collectAsStateWithLifecycle()
    val activeName by beViewModel.activeProfileName.collectAsStateWithLifecycle()
    val activePhoto by beViewModel.activeProfilePhotoUrl.collectAsStateWithLifecycle()
    
    ProfileSectionContent(
        user = userFromBrain,
        userFromBrain = userFromBrain,
        activeName = activeName,
        activePhoto = activePhoto,
        navController = navController,
        onLogout = onLogout,
        brush = brush
    )
}

// ==================================================================================
// --- SECCIÓN 2: COMPONENTES VISUALES DE CABECERA (DUMB) ---
// ==================================================================================

/**
 * TopHeaderSectionContent: Representación visual sin estado de la cabecera superior.
 * Utiliza un diseño de 3 slots (Clima, Ubicación, Perfil) con estética Cyberpunk.
 */
@Composable
fun TopHeaderSectionContent(
    navController: NavHostController,
    user: UserEntity?,
    activeName: String,
    activePhoto: String?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
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
                .statusBarsPadding()
                .height(95.dp)
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

                    val path = Path().apply {
                        moveTo(0f, size.height - cornerSize)
                        lineTo(cornerSize, size.height)
                        lineTo(size.width - cornerSize, size.height)
                        lineTo(size.width, size.height - cornerSize)
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
                // --- SLOT 1: TARJETA DE CLIMA (Izquierda) ---
                WeatherWidget(
                    temp = temperature,
                    emoji = weatherEmoji,
                    description = weatherDescription,
                    onClick = onWeatherClick,
                    brush = cardGradientBrush,
                    modifier = Modifier.width(95.dp).fillMaxHeight()
                )

                // --- SLOT 2: TARJETA DE DIRECCIONES (Centro - Adaptable) ---
                LocationSelector(
                    user = user,
                    currentLocation = currentLocationState,
                    onRefresh = onRefreshLocation,
                    onLocationSelected = onLocationSelected,
                    brush = cardGradientBrush,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                // --- SLOT 3: TARJETA DE PERFIL (Derecha) ---
                ProfileSectionContent(
                    user = user,
                    userFromBrain = userFromBrain,
                    activeName = activeName,
                    activePhoto = activePhoto,
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

/**
 * TopHeaderSectionContentV2: Representación visual Elite Masterpiece (M3 / Future-Design).
 * Implementa un diseño minimalista de 3 slots con jerarquía optimizada:
 * Izquierda: Identidad (Avatar + Nombre Ultra-Bold)
 * Centro: Ubicación (Dirección centrada de alto impacto)
 * Derecha: Clima (Indicador técnico secundario)
 */
@Composable
fun TopHeaderSectionContentV2(
    navController: NavHostController,
    user: UserEntity?,
    activeName: String,
    activePhoto: String?,
    isPersonalProfile: Boolean,
    selectedProfileId: String?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    currentLocationState: LocationOption,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onLocationSelected: (LocationOption) -> Unit,
    onProfileSelected: (String?) -> Unit,
    onLogout: () -> Unit,
    userFromBrain: UserEntity?,
    onResultClick: (Any) -> Unit = {}
) {
    var showLocationPopup by remember { mutableStateOf(false) }
    var showProfilePopup by remember { mutableStateOf(false) }
    val finalUser = userFromBrain ?: user

    val displayName = activeName.uppercase()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(CPCyberColors.DeepVoid.copy(alpha = 0.95f))
            .drawBehind {
                // Línea de horizonte de neón en la base
                val strokeWidth = 1.2.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, size.height - 16.dp.toPx())
                    lineTo(16.dp.toPx(), size.height)
                    lineTo(size.width - 16.dp.toPx(), size.height)
                    lineTo(size.width, size.height - 16.dp.toPx())
                }
                
                val borderGradient = Brush.horizontalGradient(
                    0.0f to CPCyberColors.MaverickCyan.copy(alpha = 0.05f),
                    0.15f to CPCyberColors.MaverickCyan,
                    0.85f to CPCyberColors.MaverickCyan,
                    1.0f to CPCyberColors.MaverickCyan.copy(alpha = 0.05f)
                )

                // 1. Línea Principal
                drawPath(
                    path = path,
                    brush = borderGradient,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )

                // 2. Glow Tenue
                drawPath(
                    path = path,
                    brush = borderGradient,
                    style = Stroke(
                        width = strokeWidth * 2.5f,
                        cap = StrokeCap.Round
                    ),
                    alpha = 0.15f
                )

                // Resplandor base que emana de la identidad
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(CPCyberColors.MaverickCyan.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(40.dp.toPx(), size.height / 2),
                        radius = size.width / 2
                    )
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
                // --- SLOT 1: IDENTIDAD (Izquierda - Protagonista) ---
            Row(
                modifier = Modifier
                    .weight(1.3f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { showProfilePopup = true }
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.5.dp, Brush.sweepGradient(listOf(CPCyberColors.MaverickCyan, CPCyberColors.ElectricPurple, CPCyberColors.MaverickCyan)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (activePhoto != null) {
                        AsyncImage(
                            model = activePhoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isPersonalProfile) "HOLA," else "ENTIDAD ACTIVA:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = CPCyberColors.MaverickCyan,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black, // Ultra-Bold Impact
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // --- SLOT 2: UBICACIÓN (Centro - Núcleo de Datos) ---
            Column(
                modifier = Modifier
                    .weight(1.4f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showLocationPopup = true },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val (modeLabel, modeColor, modeIcon) = when (currentLocationState) {
                    is LocationOption.Gps -> Triple("GPS_LIVE", CPCyberColors.MaverickCyan, Icons.Default.GpsFixed)
                    is LocationOption.Personal -> Triple("STATION_HOME", Color.White.copy(alpha = 0.6f), Icons.Default.Home)
                    is LocationOption.Business -> Triple("NETWORK_HQ", CPCyberColors.ElectricPurple, Icons.Default.Business)
                }

                val locationMain = when (currentLocationState) {
                    is LocationOption.Gps -> currentLocationState.address.ifBlank { "SCANNING..." }
                    is LocationOption.Personal -> "${currentLocationState.address} ${currentLocationState.number}"
                    is LocationOption.Business -> currentLocationState.branchName
                }.uppercase()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(modeIcon, null, tint = modeColor, modifier = Modifier.size(8.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = modeLabel,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = modeColor,
                        letterSpacing = 2.sp
                    )
                }
                AutoSizeText(
                    text = locationMain,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = when(currentLocationState) {
                        is LocationOption.Gps -> currentLocationState.locality
                        is LocationOption.Personal -> currentLocationState.locality
                        is LocationOption.Business -> currentLocationState.address
                    }.uppercase(),
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // --- SLOT 3: CLIMA (Derecha - Indicador Técnico Secundario) ---
            Row(
                modifier = Modifier
                    .weight(0.9f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onWeatherClick() },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = temperature,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = weatherDescription.uppercase(),
                        fontSize = 6.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = weatherEmoji, fontSize = 20.sp)
            }
        }
    }

    // --- DIÁLOGOS ---
    LocationDialog(
        show = showLocationPopup,
        user = user,
        currentLocation = currentLocationState,
        onRefresh = { onRefreshLocation() },
        onLocationSelected = { onLocationSelected(it); showLocationPopup = false },
        onDismiss = { showLocationPopup = false }
    )

    if (finalUser != null) {
        ProfileDialog(
            show = showProfilePopup,
            user = finalUser,
            isPersonalProfile = isPersonalProfile,
            selectedProfileId = selectedProfileId,
            onProfileSelected = onProfileSelected,
            navController = navController,
            onLogout = { onLogout(); showProfilePopup = false },
            onDismiss = { showProfilePopup = false }
        )
    }
}

/**
 * WeatherWidget: Slot lateral para visualización de temperatura y estado climático.
 */
@Composable
fun WeatherWidget(temp: String, emoji: String, description: String, onClick: () -> Unit, brush: Brush, modifier: Modifier = Modifier) {
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
            // Detalle descriptivo sutil
            Text(text = description.uppercase(), fontSize = 6.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

/**
 * LocationSelector: Slot central adaptable para la ubicación activa.
 * Implementa la lógica de selección de direcciones personales y empresariales.
 */
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

        // Icono de GPS estilizado y sobresaliendo
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = (4).dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D1117).copy(alpha = 0.8f))
                .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.5f), CircleShape)
                .clickable { 
                    rotationAngle += 360f 
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
        LocationDialog(
            show = true,
            user = user,
            currentLocation = currentLocation,
            onRefresh = { onRefresh(); showPopup = false },
            onLocationSelected = { onLocationSelected(it); showPopup = false },
            onDismiss = { showPopup = false }
        )
    }
}

/**
 * ProfileSectionContent: Representación visual del acceso al perfil.
 */
@Composable
fun ProfileSectionContent(
    modifier: Modifier = Modifier,
    user: UserEntity?,
    userFromBrain: UserEntity?,
    activeName: String,
    activePhoto: String?,
    navController: NavHostController,
    onLogout: () -> Unit,
    brush: Brush,
    onResultClick: (Any) -> Unit = {}
) {
    var showPopup by remember { mutableStateOf(false) }
    val finalUser = userFromBrain ?: user

    val displayName = activeName.uppercase()

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
                    if (activePhoto != null) {
                        AsyncImage(model = activePhoto, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape).border(1.5.dp, Color(0xFF22D3EE), CircleShape), contentScale = ContentScale.Crop)
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
        ProfileDialog(
            show = true,
            user = finalUser,
            navController = navController,
            onLogout = { onLogout(); showPopup = false },
            onDismiss = { showPopup = false }
        )
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
        photoUrl = null
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
            activeName = "JUAN PEREZ",
            activePhoto = null,
            temperature = "24°C",
            weatherEmoji = "☀️",
            weatherDescription = "Despejado",
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
fun TopHeaderSectionV2Preview() {
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
        locality = "San Miguel de Tucumán",
        province = "Tucumán",
        postalCode = "4000"
    )

    MyApplicationTheme {
        TopHeaderSectionContentV2(
            navController = rememberNavController(),
            user = mockUser,
            activeName = "JUAN PEREZ",
            activePhoto = null,
            isPersonalProfile = true,
            selectedProfileId = null,
            temperature = "24°C",
            weatherEmoji = "☀️",
            weatherDescription = "Despejado",
            currentLocationState = mockLocation,
            onWeatherClick = {},
            onRefreshLocation = {},
            onLocationSelected = {},
            onProfileSelected = {},
            onLogout = {},
            userFromBrain = mockUser,
            onResultClick = {}
        )
    }
}

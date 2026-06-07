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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.myapplication.presentation.features.home.Screen
import com.example.myapplication.presentation.features.home.UbicacionClimaViewModel
import com.example.myapplication.presentation.global.BeBrainViewModel
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.core.domain.model.CompanyClient
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.presentation.designsystem.components.GeminiCyberWrapper
import com.example.myapplication.presentation.designsystem.components.AutoSizeText
import com.example.myapplication.core.common.QRUtils

// ==================================================================================
// --- SECCIÓN 1: ORQUESTADORES DE CABECERA (SMART) ---
// ==================================================================================

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
    activePhoto: Any?,
    isPersonalProfile: Boolean = true,
    selectedProfileId: String? = null,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    activeAddress: AddressUnico?,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onGpsToggle: () -> Unit = {},
    isGpsEnabled: Boolean = true,
    onLocationSelected: (AddressUnico) -> Unit,
    onProfileSelected: (String?) -> Unit = {},
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
                    activeAddress = activeAddress,
                    onRefresh = onRefreshLocation,
                    onGpsToggle = onGpsToggle,
                    isGpsEnabled = isGpsEnabled,
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
                    isPersonalProfile = isPersonalProfile,
                    selectedProfileId = selectedProfileId,
                    navController = navController,
                    onProfileSelected = onProfileSelected,
                    onLogout = onLogout,
                    brush = cardGradientBrush,
                    onResultClick = onResultClick,
                    modifier = Modifier.width(95.dp).fillMaxHeight()
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopHeaderSectionContentV2(
    navController: NavHostController,
    user: UserEntity?,
    activeName: String,
    activePhoto: Any?,
    isPersonalProfile: Boolean,
    selectedProfileId: String?,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    activeAddress: AddressUnico?,
    onWeatherClick: () -> Unit,
    onRefreshLocation: () -> Unit,
    onGpsToggle: () -> Unit = {},
    isGpsEnabled: Boolean = true,
    onLocationSelected: (AddressUnico) -> Unit,
    onProfileSelected: (String?) -> Unit,
    onLogout: () -> Unit,
    userFromBrain: UserEntity?,
    showWeatherDialog: Boolean = false,
    cityName: String = "",
    onSetWeatherDetailsVisible: (Boolean) -> Unit = {},
    onResultClick: (Any) -> Unit = {},
    showLocationPopupHoisted: Boolean? = null,
    showProfilePopupHoisted: Boolean? = null,
    onLocationPopupToggle: (Boolean) -> Unit = {},
    onProfilePopupToggle: (Boolean) -> Unit = {}
) {
    var showLocationPopupLocal by remember { mutableStateOf(false) }
    var showProfilePopupLocal by remember { mutableStateOf(false) }

    val showLocationPopup = showLocationPopupHoisted ?: showLocationPopupLocal
    val showProfilePopup = showProfilePopupHoisted ?: showProfilePopupLocal

    val setShowLocationPopup: (Boolean) -> Unit = { 
        if (showLocationPopupHoisted != null) onLocationPopupToggle(it) else showLocationPopupLocal = it 
    }
    val setShowProfilePopup: (Boolean) -> Unit = { 
        if (showProfilePopupHoisted != null) onProfilePopupToggle(it) else showProfilePopupLocal = it 
    }

    val finalUser = userFromBrain ?: user

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(CPCyberColors.DeepVoid.copy(alpha = 0.95f))
            .drawBehind { drawCyberHeaderBorder() }
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IdentitySlot(
                activeName = activeName,
                activePhoto = activePhoto,
                isPersonalProfile = isPersonalProfile,
                onProfileClick = { setShowProfilePopup(true) },
                modifier = Modifier.weight(1.1f)
            )

            LocationSlot(
                activeAddress = activeAddress,
                onLocationClick = { setShowLocationPopup(true) },
                modifier = Modifier.weight(1.8f)
            )

            WeatherSlot(
                temperature = temperature,
                weatherEmoji = weatherEmoji,
                weatherDescription = weatherDescription,
                onWeatherClick = onWeatherClick,
                modifier = Modifier.weight(1.1f)
            )
        }
    }

    HeaderDialogs(
        showLocationPopup = showLocationPopup,
        showProfilePopup = showProfilePopup,
        finalUser = finalUser,
        activeAddress = activeAddress,
        selectedProfileId = selectedProfileId,
        isGpsEnabled = isGpsEnabled,
        onRefreshLocation = onRefreshLocation,
        onGpsToggle = onGpsToggle,
        onLocationSelected = onLocationSelected,
        onProfileSelected = onProfileSelected,
        onLogout = onLogout,
        navController = navController,
        isPersonalProfile = isPersonalProfile,
        showWeatherDialog = showWeatherDialog,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        onSetWeatherDetailsVisible = onSetWeatherDetailsVisible,
        setShowLocationPopup = setShowLocationPopup,
        setShowProfilePopup = setShowProfilePopup
    )
}

/**
 * Fragmento de Identidad (Slot Izquierdo)
 */
@Composable
private fun IdentitySlot(
    activeName: String,
    activePhoto: Any?,
    isPersonalProfile: Boolean,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onProfileClick() })
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
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = if (isPersonalProfile) "HOLA !!! " else "ENTIDAD ACTIVA:",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = CPCyberColors.MaverickCyan,
                letterSpacing = 1.5.sp
            )
            Text(
                text = activeName.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Fragmento de Ubicación (Slot Central)
 */
@Composable
private fun LocationSlot(
    activeAddress: AddressUnico?,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onLocationClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val modeLabel = if (activeAddress?.id == "gps_current") "GPS_LIVE" else if (activeAddress?.isCompany == true) "NETWORK_HQ" else "STATION_HOME"
        val modeColor = if (activeAddress?.id == "gps_current") CPCyberColors.MaverickCyan else if (activeAddress?.isCompany == true) CPCyberColors.ElectricPurple else Color.White.copy(alpha = 0.6f)
        val modeIcon = if (activeAddress?.id == "gps_current") Icons.Default.GpsFixed else if (activeAddress?.isCompany == true) Icons.Default.Business else Icons.Default.Home

        val locationMain = activeAddress?.streetAndNumber ?: "SCANNING..."

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
            text = locationMain.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp
            ),
            maxLines = 1
        )
        Text(
            text = (activeAddress?.localidad ?: "Buscando...").uppercase(),
            fontSize = 8.sp,
            color = Color.White.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Fragmento de Clima (Slot Derecho)
 */
@Composable
private fun WeatherSlot(
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onWeatherClick() },
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = weatherEmoji,
                    fontSize = 40.sp,
                    modifier = Modifier
                        .graphicsLayer { alpha = 0.35f }
                        .offset(x = 6.dp)
                )
                Text(
                    text = temperature,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
            AutoSizeText(
                text = weatherDescription.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Orquestador de Diálogos para limpiar el componente principal
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HeaderDialogs(
    showLocationPopup: Boolean,
    showProfilePopup: Boolean,
    finalUser: UserEntity?,
    activeAddress: AddressUnico?,
    selectedProfileId: String?,
    isGpsEnabled: Boolean,
    onRefreshLocation: () -> Unit,
    onGpsToggle: () -> Unit,
    onLocationSelected: (AddressUnico) -> Unit,
    onProfileSelected: (String?) -> Unit,
    onLogout: () -> Unit,
    navController: NavHostController,
    isPersonalProfile: Boolean,
    showWeatherDialog: Boolean,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    onSetWeatherDetailsVisible: (Boolean) -> Unit,
    setShowLocationPopup: (Boolean) -> Unit,
    setShowProfilePopup: (Boolean) -> Unit
) {
    LocationDialog(
        show = showLocationPopup,
        availableAddresses = finalUser?.personalAddresses ?: emptyList(),
        activeAddress = activeAddress,
        selectedProfileId = selectedProfileId,
        isGpsSystemEnabled = isGpsEnabled,
        onRefresh = { onRefreshLocation() },
        onGpsToggle = onGpsToggle,
        onLocationSelected = { onLocationSelected(it); setShowLocationPopup(false) },
        onDismiss = { setShowLocationPopup(false) }
    )

    if (finalUser != null) {
        ProfileDialog(
            show = showProfilePopup,
            user = finalUser.toDomain(),
            isPersonalProfile = isPersonalProfile,
            selectedProfileId = selectedProfileId,
            onProfileSelected = onProfileSelected,
            navController = navController,
            onLogout = { onLogout(); setShowProfilePopup(false) },
            onDismiss = { setShowProfilePopup(false) }
        )
    }

    WeatherDialog(
        show = showWeatherDialog,
        temperature = temperature,
        weatherEmoji = weatherEmoji,
        weatherDescription = weatherDescription,
        cityName = cityName,
        onDismiss = { onSetWeatherDetailsVisible(false) }
    )
}

/**
 * Lógica de dibujo del borde Cyberpunk extraída
 */
private fun DrawScope.drawCyberHeaderBorder() {
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

    drawPath(
        path = path,
        brush = borderGradient,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    drawPath(
        path = path,
        brush = borderGradient,
        style = Stroke(width = strokeWidth * 2.5f, cap = StrokeCap.Round),
        alpha = 0.15f
    )

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(CPCyberColors.MaverickCyan.copy(alpha = 0.08f), Color.Transparent),
            center = Offset(40.dp.toPx(), size.height / 2),
            radius = size.width / 2
        )
    )
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
    activeAddress: AddressUnico?,
    onRefresh: () -> Unit,
    onLocationSelected: (AddressUnico) -> Unit,
    brush: Brush,
    modifier: Modifier = Modifier,
    onGpsToggle: () -> Unit = {},
    isGpsEnabled: Boolean = true
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
    val (linea1, linea2, linea3) = if (activeAddress?.id == "gps_current") {
        Triple("UBICACIÓN ACTUAL", activeAddress.streetAndNumber, activeAddress.localidad)
    } else {
        val label = if (activeAddress?.isCompany == true) (activeAddress.ownerName ?: "EMPRESA") else "MI CASA / PERSONAL"
        Triple(label, activeAddress?.label ?: activeAddress?.streetAndNumber ?: "SELECCIONAR", activeAddress?.streetAndNumber ?: "")
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
            availableAddresses = user?.personalAddresses ?: emptyList(),
            activeAddress = activeAddress,
            selectedProfileId = if (activeAddress?.isCompany == true) activeAddress.ownerId else null,
            isGpsSystemEnabled = isGpsEnabled,
            onRefresh = { onRefresh(); showPopup = false },
            onGpsToggle = { onGpsToggle(); showPopup = false },
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
    activePhoto: Any?,
    isPersonalProfile: Boolean = true,
    selectedProfileId: String? = null,
    navController: NavHostController,
    onProfileSelected: (String?) -> Unit = {},
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
            user = finalUser.toDomain(),
            isPersonalProfile = isPersonalProfile,
            selectedProfileId = selectedProfileId,
            onProfileSelected = onProfileSelected,
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
    val mockAddress = AddressUnico(
        id = "gps_current",
        ownerName = "Juan",
        label = "GPS",
        calle = "Calle Falsa",
        numero = "123",
        localidad = "San Miguel de Tucumán",
        provincia = "Tucumán",
        codigoPostal = "T4000",
        isCompany = false,
        latitude = -26.8,
        longitude = -65.2
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
            activeAddress = mockAddress,
            onWeatherClick = {},
            onRefreshLocation = {},
            onGpsToggle = {},
            isGpsEnabled = true,
            onLocationSelected = {},
            onLogout = {},
            userFromBrain = mockUser,
            onResultClick = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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
    val mockAddress = AddressUnico(
        id = "gps_current",
        ownerName = "Juan",
        label = "GPS",
        calle = "Calle Falsa",
        numero = "123",
        localidad = "San Miguel de Tucumán",
        provincia = "Tucumán",
        codigoPostal = "T4000",
        isCompany = false,
        latitude = -26.8,
        longitude = -65.2
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
            activeAddress = mockAddress,
            onWeatherClick = {},
            onRefreshLocation = {},
            onGpsToggle = {},
            isGpsEnabled = true,
            onLocationSelected = {},
            onProfileSelected = {},
            onLogout = {},
            userFromBrain = mockUser,
            onResultClick = {}
        )
    }
}


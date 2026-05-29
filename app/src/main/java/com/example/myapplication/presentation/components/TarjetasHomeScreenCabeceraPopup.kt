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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.myapplication.core.data.local.entity.UserEntity
import com.example.myapplication.core.domain.model.AddressInfo
import com.example.myapplication.core.domain.model.User
import com.example.myapplication.presentation.features.home.Screen
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.components.*
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.myapplication.core.common.QRUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE DIÁLOGOS Y POPUPS (ELITE) ---
// ==================================================================================

// --- COMPONENTES AUXILIARES ---

@Composable
fun MaverickCloseButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.Red.copy(alpha = 0.1f))
            .border(1.dp, Color.Red.copy(alpha = 0.3f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 12.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Cerrar",
            tint = Color.Red,
            modifier = Modifier.size(14.dp)
        )
    }
}

/**
 * MaverickQRDisplay: Pantalla completa estilo WhatsApp para mostrar el QR del usuario.
 */
@Composable
fun MaverickQRDisplay(
    user: User,
    activeProfileName: String,
    activeProfilePhoto: Any?,
    onClose: () -> Unit
) {
    val cyberCyan = CPCyberColors.MaverickCyan
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Cerrar", tint = Color.Black)
                }
                Text(
                    "MI IDENTIDAD MAVERICK",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color.Black
                )
                Spacer(Modifier.size(48.dp)) // Espaciador para equilibrar
            }

            Spacer(Modifier.weight(0.3f))

            // --- CÓDIGO QR CARD ---
            Card(
                modifier = Modifier
                    .size(320.dp)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Generamos el código basado en el UID (o ID de empresa si aplica)
                    val qrContent = QRUtils.generateUniqueMAVCode(user.uid)
                    
                    Icon(
                        Icons.Default.QrCode2, 
                        null, 
                        tint = Color.Black, 
                        modifier = Modifier.fillMaxSize().padding(24.dp)
                    )
                    
                    // Logo Maverick en el medio del QR
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(2.dp, cyberCyan)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("M", fontWeight = FontWeight.Black, color = cyberCyan, fontSize = 24.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- DATOS DEL PERFIL ---
            AsyncImage(
                model = activeProfilePhoto ?: user.profileImage,
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(2.dp, cyberCyan, CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.height(16.dp))

            Text(
                text = activeProfileName.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.Black
            )
            
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(cyberCyan.copy(alpha = 0.1f))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    "ESCANEA PARA AGREGAR CONTACTO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = cyberCyan,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

/**
 * UserProfilePopup: Ventana emergente con detalles del perfil y empresas asociadas.
 */
@Composable
fun UserProfilePopup(
    user: User, 
    isPersonalProfile: Boolean = true,
    selectedProfileId: String? = null,
    onProfileSelected: (String?) -> Unit = {},
    isScrollable: Boolean = true,
    onClose: () -> Unit, 
    onLogout: () -> Unit, 
    onProfileClick: () -> Unit
) {
    val cyberCyan = CPCyberColors.MaverickCyan
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)

    var showMyQR by remember { mutableStateOf(false) }

    if (showMyQR) {
        Dialog(
            onDismissRequest = { showMyQR = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val activeName = if (isPersonalProfile) user.fullName else user.companies.find { it.id == selectedProfileId }?.name ?: user.displayName
            val activePhoto = if (isPersonalProfile) user.profileImage else user.companies.find { it.id == selectedProfileId }?.profileImage ?: user.profileImage
            
            MaverickQRDisplay(
                user = user,
                activeProfileName = activeName,
                activeProfilePhoto = activePhoto,
                onClose = { showMyQR = false }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topEnd = 28.dp, bottomStart = 32.dp))
            .background(deepGlass)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(cyberCyan, Color.Transparent, cyberCyan)),
                shape = CutCornerShape(topEnd = 32.dp, bottomStart = 32.dp)
            )
            .then(if (isScrollable) Modifier.heightIn(max = 700.dp).verticalScroll(rememberScrollState()) else Modifier)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER DEL POPUP ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SYS_ID // PROFILE_V2",
                    color = cyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = " GESTIÓN DE IDENTIDAD DIGITAL",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            MaverickCloseButton(onClick = onClose)
        }
        
        Spacer(Modifier.height(24.dp))

        // --- LÓGICA DE DATOS DEL PERFIL ACTIVO (REACTIVE SSOT) ---
        val activeCompany = remember(user, selectedProfileId) { user.companies.find { it.id == selectedProfileId } }
        val activeName = remember(user, isPersonalProfile, activeCompany) { if (isPersonalProfile) user.fullName else activeCompany?.name ?: user.displayName }
        val activeEmail = remember(user, isPersonalProfile, activeCompany) { if (isPersonalProfile) user.email else activeCompany?.email ?: user.email }
        val activePhoto = remember(user, isPersonalProfile, activeCompany) { if (isPersonalProfile) user.profileImage else activeCompany?.profileImage ?: user.profileImage }

        // ==================================================================================
        // --- SECCIÓN 1: PERFIL ACTIVO (VISTA CENTRAL DINÁMICA) ---
        // ==================================================================================
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            Canvas(modifier = Modifier.size(100.dp)) {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(cyberCyan, Color.Transparent, cyberCyan)),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            AsyncImage(
                model = activePhoto,
                contentDescription = null,
                modifier = Modifier
                    .size(85.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    .clickable { onProfileClick() },
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = activeName.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
            if (isPersonalProfile && user.isVerified) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Verified, null, tint = cyberCyan, modifier = Modifier.size(14.dp))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = activeEmail,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
            if (isPersonalProfile && activeEmail.endsWith("@gmail.com")) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
        Spacer(Modifier.height(20.dp))

        // ==================================================================================
        // --- SECCIÓN 2: NETWORK // ENTIDADES (BURBUJAS INACTIVAS) ---
        // ==================================================================================
        Text(
            "🏢 NETWORK // CAMBIAR DE PERFIL",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(Modifier.height(16.dp))

        // Lista de perfiles INACTIVOS para las burbujas (Ocultamos el actual para evitar duplicidad)
        val inactiveProfiles = remember(user, selectedProfileId, isPersonalProfile) {
            val list = mutableListOf<ProfileBubbleData>()
            
            // 1. Si no estamos en el perfil personal, lo agregamos como opción
            if (!isPersonalProfile) {
                list.add(ProfileBubbleData(null, user.fullName, user.profileImage, false))
            }
            
            // 2. Agregamos todas las empresas EXCEPTO la que está activa
            user.companies.forEach { company ->
                if (isPersonalProfile || company.id != selectedProfileId) {
                    list.add(ProfileBubbleData(company.id, company.name, company.profileImage, false))
                }
            }
            list
        }

        if (inactiveProfiles.isEmpty()) {
            Text(
                "NO HAY OTROS PERFILES DISPONIBLES",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(inactiveProfiles.size) { index ->
                    val profile = inactiveProfiles[index]
                    ProfileBubbleSwitch(
                        data = profile,
                        onClick = { onProfileSelected(profile.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
        Spacer(Modifier.height(20.dp))

        // ==================================================================================
        // --- SECCIÓN 3: HERRAMIENTAS DE COMPARTIR ---
        // ==================================================================================
        Text(
            "🤝 INTERCAMBIO // COMPARTIR NODO",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SharingActionBotton(icon = Icons.Default.QrCodeScanner, label = "ESCANEAR", color = Color.White.copy(0.6f)) { /* TODO */ }
            SharingActionBotton(icon = Icons.Default.QrCode2, label = "MI QR", color = Color.White.copy(0.6f)) { showMyQR = true }
            SharingActionBotton(icon = Icons.Default.Share, label = "GOOGLE", color = Color(0xFF4285F4).copy(alpha = 0.8f)) { /* TODO: Share via System */ }
        }

        Spacer(Modifier.height(24.dp))
        DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
        Spacer(Modifier.height(20.dp))

        // --- BOTÓN DESCONECTAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(CutCornerShape(bottomStart = 16.dp))
                .background(Color(0xFFFF1744).copy(0.06f))
                .border(1.dp, Color(0xFFFF1744).copy(0.15f), CutCornerShape(bottomStart = 16.dp))
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color(0xFFFF1744), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text("TERMINAR SESIÓN SEGURA", color = Color(0xFFFF1744), fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun SharingActionBotton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(0.05f), CircleShape)
                .border(1.dp, color.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = Color.White.copy(0.4f), fontSize = 7.sp, fontWeight = FontWeight.Black)
    }
}

/**
 * Data class auxiliar para las burbujas de perfil.
 */
private data class ProfileBubbleData(
    val id: String?,
    val name: String,
    val photo: Any?,
    val isActive: Boolean
)

/**
 * Componente de Burbuja para cambio de perfil en el Popup.
 */
@Composable
private fun ProfileBubbleSwitch(
    data: ProfileBubbleData,
    onClick: () -> Unit
) {
    val cyberCyan = CPCyberColors.MaverickCyan
    val borderColor = if (data.isActive) cyberCyan else Color.White.copy(alpha = 0.2f)
    val borderWidth = if (data.isActive) 2.dp else 1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(borderWidth, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (data.photo != null) {
                AsyncImage(
                    model = data.photo,
                    contentDescription = data.name,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = if (data.id == null) Icons.Default.Person else Icons.Default.Business,
                    contentDescription = null,
                    tint = if (data.isActive) cyberCyan else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = data.name.split(" ").firstOrNull()?.uppercase() ?: "",
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            color = if (data.isActive) cyberCyan else Color.White.copy(alpha = 0.4f),
            letterSpacing = 0.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(54.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * LocationPopup: Ventana emergente para visualización y selección de ubicación.
 */
@Composable
fun LocationPopup(
    availableAddresses: List<AddressInfo>,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onLocationSelected: (AddressInfo) -> Unit,
    onGpsToggle: () -> Unit,
    activeAddress: AddressInfo?,
    selectedProfileId: String? = null,
    isGpsSystemEnabled: Boolean = true // Detecta si el GPS de Android está ON/OFF
) {
    val cyberCyan = CPCyberColors.MaverickCyan
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)
    var isRefreshing by remember { mutableStateOf(false) }

    val rotationAnim by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = if (isRefreshing) {
            infiniteRepeatable(animation = tween(1000, easing = LinearOutSlowInEasing))
        } else tween(0),
        label = "GpsRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topEnd = 32.dp, bottomStart = 32.dp))
            .background(deepGlass)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(cyberCyan, Color.Transparent, cyberCyan)),
                shape = CutCornerShape(topEnd = 32.dp, bottomStart = 32.dp)
            )
            .heightIn(max = 700.dp)
            .padding(24.dp)
    ) {
        // --- HEADER DEL POPUP ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GEO_HUD // LOCATOR_V3",
                    color = cyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = " CONFIGURACIÓN DE NODO GEOGRÁFICO",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            MaverickCloseButton(onClick = onClose)
        }

        Spacer(Modifier.height(24.dp))

        // ==================================================================================
        // --- SECCIÓN 1: NODO ACTIVO SELECCIONADO ---
        // ==================================================================================
        val isGpsActive = activeAddress?.id == "gps_current"
        
        Text(
            text = "📍 NODO SELECCIONADO",
            color = if (isGpsActive) cyberCyan else Color.White.copy(0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isGpsActive) cyberCyan.copy(0.05f) else Color.White.copy(alpha = 0.03f),
            shape = CutCornerShape(topStart = 16.dp),
            border = BorderStroke(1.dp, (if (isGpsActive) cyberCyan else Color.White).copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (activeAddress != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isGpsActive) Icons.Default.SatelliteAlt else if (activeAddress.isCompany) Icons.Default.Business else Icons.Default.Home,
                            contentDescription = null,
                            tint = if (isGpsActive) cyberCyan else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = activeAddress.streetAndNumber.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            val detailText = buildString {
                                if (activeAddress.isCompany) {
                                    append("${activeAddress.companyOrUserName} - ${activeAddress.branchName} | ")
                                }
                                append("${activeAddress.locality}, ${activeAddress.province} [CP: ${activeAddress.postalCode}]")
                            }
                            Text(
                                text = detailText.uppercase(),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text("NO SE HA DETECTADO UN NODO ACTIVO", color = Color.Red.copy(0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ==================================================================================
        // --- SECCIÓN 2: CONTROL DE ESTADO GPS ---
        // ==================================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(0.02f))
                .border(0.5.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val gpsText = when {
                    !isGpsSystemEnabled -> "GPS DESACTIVADO EN EL SISTEMA"
                    isGpsActive -> "USANDO DATOS DE GPS EN TIEMPO REAL"
                    else -> "USAR UBICACIÓN GPS"
                }
                Text(
                    text = gpsText,
                    color = if (!isGpsSystemEnabled) Color.Red.copy(0.7f) else if (isGpsActive) cyberCyan else Color.White.copy(0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                if (!isGpsSystemEnabled) {
                    Text("ACTIVA EL GPS DESDE AJUSTES", color = Color.White.copy(0.3f), fontSize = 7.sp)
                }
            }

            IconButton(
                onClick = {
                    isRefreshing = true
                    onGpsToggle()
                },
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isGpsActive) cyberCyan.copy(0.15f) else Color.White.copy(0.05f),
                        CircleShape
                    )
                    .border(1.dp, if (isGpsActive) cyberCyan.copy(alpha = 0.4f) else Color.White.copy(0.1f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    null,
                    tint = if (isGpsActive) cyberCyan else Color.White.copy(0.4f),
                    modifier = Modifier.rotate(rotationAnim).size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        DepthDividerHorizontal(shadowColor = Color.Black.copy(0.4f), highlightColor = Color.White.copy(0.05f))
        Spacer(Modifier.height(20.dp))

        // ==================================================================================
        // --- SECCIÓN 3: DIRECTORIO DE NODOS GUARDADOS (DIRECTORY TREE) ---
        // ==================================================================================
        Text(
            "📂 NODOS DE RED DISPONIBLES // DIR_TREE",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(Modifier.height(12.dp))

        // El arbol tiene su propio scroll interno
        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            val locationNodes = remember(availableAddresses, selectedProfileId) {
                val personal = availableAddresses.filter { !it.isCompany && it.id != "gps_current" }
                val companies = availableAddresses.filter { it.isCompany }

                val nodes = mutableListOf<FileNode>()
                
                if (personal.isNotEmpty()) {
                    val isPersonalActive = selectedProfileId == null
                    nodes.add(FileNode(
                        name = "Mis Direcciones",
                        isDirectory = true,
                        alpha = if (isPersonalActive) 1f else 0.5f,
                        children = personal.map { addr ->
                            FileNode(
                                name = "${addr.streetAndNumber} (${addr.locality})",
                                isDirectory = false,
                                icon = Icons.Default.Home,
                                tint = if (isPersonalActive) cyberCyan else Color.Gray
                            )
                        }
                    ))
                }
                
                if (companies.isNotEmpty()) {
                    nodes.add(FileNode(
                        name = "Business Network",
                        isDirectory = true,
                        children = companies.groupBy { it.companyOrUserName }.map { (companyName, branches) ->
                            val companyId = branches.firstOrNull()?.ownerId
                            val isCompanyActive = selectedProfileId != null && selectedProfileId == companyId
                            
                            FileNode(
                                name = companyName,
                                isDirectory = true,
                                alpha = if (isCompanyActive) 1f else 0.5f,
                                children = branches.map { branch ->
                                    FileNode(
                                        name = "${branch.branchName}: ${branch.streetAndNumber} CP:${branch.postalCode}",
                                        isDirectory = false,
                                        icon = Icons.Default.Business,
                                        tint = if (isCompanyActive) CPCyberColors.ElectricPurple else Color.Gray
                                    )
                                }
                            )
                        }
                    ))
                }
                nodes
            }

            DirectoryTree(
                nodes = locationNodes,
                onNodeClick = { node ->
                    if (!node.isDirectory) {
                        val selected = availableAddresses.find { addr ->
                            val personalName = "${addr.streetAndNumber} (${addr.locality})"
                            val companyName = "${addr.branchName}: ${addr.streetAndNumber} CP:${addr.postalCode}"
                            node.name == personalName || node.name == companyName
                        }
                        selected?.let { onLocationSelected(it) }
                    }
                }
            )
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1500)
            isRefreshing = false
        }
    }
}

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
    val rot by animateFloatAsState(if (isExpanded) 90f else 0f, label = "dir_rot")
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
fun CyberTreeLeaf(icon: ImageVector, title: String, subtitle: String, accentColor: Color, isActive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .drawWithCache { 
                onDrawWithContent { 
                    drawLine(accentColor.copy(0.3f), Offset(0f, size.height/2), Offset(24.dp.toPx(), size.height/2), 1.dp.toPx())
                    drawContent() 
                } 
            }
            .padding(start = 32.dp, end = 8.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isActive) accentColor.copy(0.2f) else accentColor.copy(0.1f), RoundedCornerShape(10.dp))
                .border(1.dp, if (isActive) accentColor else accentColor.copy(0.3f), RoundedCornerShape(10.dp)), 
            contentAlignment = Alignment.Center
        ) { 
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp)) 
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = if (isActive) Color.White else Color.White.copy(0.9f), fontSize = 13.sp, fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = Color.White.copy(0.5f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isActive) {
            Spacer(Modifier.weight(1f))
            Box(modifier = Modifier.size(6.dp).background(accentColor, CircleShape))
        }
    }
}

/**
 * WeatherExpandedCard: Ventana expandida para el clima.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherExpandedCard(temperature: String, weatherEmoji: String, weatherDescription: String, cityName: String, onDismiss: () -> Unit) {
    val cyberCyan = CPCyberColors.MaverickCyan
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)

    // --- LÓGICA DE MENSAJES CONTEXTUALES ---
    val contextMessage = remember(temperature, weatherDescription) {
        val tempValue = temperature.replace("°C", "").trim().toIntOrNull() ?: 20
        val isRainy = weatherDescription.contains("lluvia", ignoreCase = true) || weatherDescription.contains("tormenta", ignoreCase = true)
        
        when {
            isRainy -> "⚠️ PRECAUCIÓN: Lluvia detectada. Las visitas técnicas externas podrían reprogramarse por seguridad."
            tempValue > 30 -> "🔥 ALERTA TÉRMICA: Calor intenso. Asegúrate de tener hidratación disponible para recibir tus envíos."
            tempValue < 10 -> "❄️ CLIMA FRÍO: Ideal para turnos en locales físicos. Abrígate antes de salir a tu cita."
            else -> "✨ CLIMA ÓPTIMO: Condiciones ideales para visitas técnicas y recepciones de pedidos."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topEnd = 32.dp, bottomStart = 32.dp))
            .background(deepGlass)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(cyberCyan, Color.Transparent, cyberCyan)),
                shape = CutCornerShape(topEnd = 32.dp, bottomStart = 32.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SYS_WTHR // ATMÓSFERA_V2", 
                color = cyberCyan, 
                fontSize = 10.sp, 
                fontWeight = FontWeight.Black, 
                letterSpacing = 2.sp
            )
            
            MaverickCloseButton(onClick = onDismiss)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = cityName.uppercase(), 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Black, 
            color = Color.White, 
            letterSpacing = 1.sp
        )
        Text(
            text = weatherDescription.uppercase(), 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            color = cyberCyan.copy(alpha = 0.8f), 
            letterSpacing = 1.5.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = weatherEmoji, fontSize = 82.sp)
            Spacer(modifier = Modifier.width(32.dp))
            Text(
                text = temperature, 
                fontSize = 82.sp, 
                fontWeight = FontWeight.Black, 
                color = Color.White,
                letterSpacing = (-2).sp
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        // --- BLOQUE DE MENSAJE INTELIGENTE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = contextMessage,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // --- MICRO-INDICADOR DE ESTADO ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(cyberCyan, CircleShape))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "REAL_TIME_FEED // ONLINE",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

// ==================================================================================
// --- SECCIÓN: CONTENEDORES DE DIÁLOGOS (POPUPS) ---
// ==================================================================================

@Composable
fun ProfileDialog(
    show: Boolean,
    user: User,
    isPersonalProfile: Boolean = true,
    selectedProfileId: String? = null,
    onProfileSelected: (String?) -> Unit = {},
    navController: NavHostController,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch {
                    delay(300)
                    onDismiss()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Capa de Dimming que respeta la cabecera
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.statusBarsPadding().height(110.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.5f))
                            .clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() }
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 110.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400)),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    ) {
                        UserProfilePopup(
                            user = user,
                            isPersonalProfile = isPersonalProfile,
                            selectedProfileId = selectedProfileId,
                            onProfileSelected = { onProfileSelected(it); closeWithAnimation() },
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

@Composable
fun LocationDialog(
    show: Boolean,
    availableAddresses: List<AddressInfo>,
    activeAddress: AddressInfo?,
    selectedProfileId: String? = null,
    isGpsSystemEnabled: Boolean = true,
    onRefresh: () -> Unit,
    onGpsToggle: () -> Unit,
    onLocationSelected: (AddressInfo) -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch {
                    delay(300)
                    onDismiss()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Capa de Dimming que respeta la cabecera
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.statusBarsPadding().height(110.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.5f))
                            .clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() }
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 110.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400)),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    ) {
                        LocationPopup(
                            availableAddresses = availableAddresses,
                            onClose = { closeWithAnimation() },
                            onRefresh = { onRefresh(); closeWithAnimation() },
                            onGpsToggle = { onGpsToggle(); closeWithAnimation() },
                            onLocationSelected = { onLocationSelected(it); closeWithAnimation() },
                            activeAddress = activeAddress,
                            selectedProfileId = selectedProfileId,
                            isGpsSystemEnabled = isGpsSystemEnabled
                        )
                    }
                }
            }
        }
    }
}

/**
 * WeatherDialog: Contenedor independiente para el popup de clima.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDialog(
    show: Boolean,
    temperature: String,
    weatherEmoji: String,
    weatherDescription: String,
    cityName: String,
    onDismiss: () -> Unit
) {
    if (show) {
        Popup(
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            var animateIn by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) { animateIn = true }

            fun closeWithAnimation() {
                animateIn = false
                scope.launch {
                    delay(300)
                    onDismiss()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Capa de Dimming que respeta la cabecera
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.statusBarsPadding().height(110.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(0.5f))
                            .clickable(remember { MutableInteractionSource() }, null) { closeWithAnimation() }
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 110.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clickable(enabled = false) {}
                ) {
                    AnimatedVisibility(
                        visible = animateIn,
                        enter = fadeIn(animationSpec = tween(400)) + expandVertically(animationSpec = tween(400)),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    ) {
                        WeatherExpandedCard(
                            temperature = temperature,
                            weatherEmoji = weatherEmoji,
                            weatherDescription = weatherDescription,
                            cityName = cityName,
                            onDismiss = { closeWithAnimation() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun UserProfilePopupPreview() {
    val mockUser = User(
        uid = "1",
        name = "Juan",
        lastName = "Pérez",
        displayName = "JUAN",
        email = "juan.perez@gmail.com",
        isVerified = true,
        isSubscribed = true
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
    val mockAddress = AddressInfo(
        id = "gps_current",
        companyOrUserName = "Juan",
        branchName = "GPS",
        streetAndNumber = "Calle Falsa 123",
        locality = "San Miguel de Tucumán",
        province = "Tucumán",
        postalCode = "T4000",
        isCompany = false,
        lat = -26.8,
        lng = -65.2
    )

    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            LocationPopup(
                availableAddresses = listOf(mockAddress),
                onClose = {},
                onRefresh = {},
                onGpsToggle = {},
                onLocationSelected = {},
                activeAddress = mockAddress
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun WeatherExpandedCardPreview() {
    MyApplicationTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            WeatherExpandedCard(
                temperature = "32°C",
                weatherEmoji = "☀️",
                weatherDescription = "Despejado y caluroso",
                cityName = "San Miguel de Tucumán",
                onDismiss = {}
            )
        }
    }
}


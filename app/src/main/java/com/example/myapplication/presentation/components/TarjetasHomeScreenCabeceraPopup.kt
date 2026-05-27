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
import com.example.myapplication.presentation.features.home.Screen
import com.example.myapplication.presentation.designsystem.theme.MyApplicationTheme
import com.example.myapplication.presentation.designsystem.components.CPCyberColors
import com.example.myapplication.core.common.QRUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController

// ==================================================================================
// --- SECCIÓN: COMPONENTES DE DIÁLOGOS Y POPUPS (ELITE) ---
// ==================================================================================

/**
 * MaverickQRDisplay: Pantalla completa estilo WhatsApp para mostrar el QR del usuario.
 */
@Composable
fun MaverickQRDisplay(
    user: UserEntity,
    activeProfileName: String,
    activeProfilePhoto: String?,
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
                    val qrContent = QRUtils.generateUniqueMAVCode(user.id)
                    
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
                model = activeProfilePhoto ?: user.photoUrl,
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
    user: UserEntity, 
    isPersonalProfile: Boolean = true,
    selectedProfileId: String? = null,
    onProfileSelected: (String?) -> Unit = {},
    isScrollable: Boolean = true,
    onClose: () -> Unit, 
    onLogout: () -> Unit, 
    onProfileClick: () -> Unit
) {
    val cyberCyan = CPCyberColors.MaverickCyan
    val cyberPurple = CPCyberColors.ElectricPurple
    val deepGlass = Color(0xFF0B0F19).copy(alpha = 0.98f)

    var showMyQR by remember { mutableStateOf(false) }

    if (showMyQR) {
        Dialog(
            onDismissRequest = { showMyQR = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val activeName = if (isPersonalProfile) user.getFullName() else user.companies.find { it.id == selectedProfileId }?.name ?: user.displayName
            val activePhoto = if (isPersonalProfile) user.photoUrl else user.companies.find { it.id == selectedProfileId }?.photoUrl ?: user.photoUrl
            
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
            .clip(CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp))
            .background(deepGlass)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(cyberCyan, cyberPurple)),
                shape = CutCornerShape(topEnd = 24.dp, bottomStart = 24.dp)
            )
            .then(if (isScrollable) Modifier.heightIn(max = 750.dp).verticalScroll(rememberScrollState()) else Modifier)
            .padding(24.dp),
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (isPersonalProfile) "MODO: PERSONAL" else "MODO: EMPRESARIAL",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // BOTÓN: GENERAR QR (ID)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(cyberCyan.copy(0.1f))
                        .clickable { showMyQR = true }
                        .padding(6.dp)
                ) {
                    Icon(Icons.Default.QrCode2, "Generar ID QR", tint = cyberCyan, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(8.dp))
                // BOTÓN: ESCANEAR QR
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(cyberPurple.copy(0.1f))
                        .clickable { /* Logic Escaneo QR - TODO: Inyectar disparador de cámara */ }
                        .padding(6.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, "Escanear QR", tint = cyberPurple, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(16.dp))
                // BOTÓN DE CIERRE RESALTADO
                IconButton(
                    onClick = onClose, 
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))

        // --- SECCIÓN: AVATAR Y DATOS ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .clickable { onProfileClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cyberCyan.copy(0.05f), CircleShape)
                    .border(1.5.dp, Brush.sweepGradient(listOf(cyberCyan, cyberPurple, cyberCyan)), CircleShape)
            )
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "${user.name} ${user.lastName}".uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        // --- BURBUJAS DE EMPRESAS ---
        if (user.companies.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                user.companies.take(3).forEach { company ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(cyberPurple.copy(alpha = 0.15f))
                            .border(0.5.dp, cyberPurple.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = company.name.uppercase(),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = cyberPurple,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                if (user.companies.size > 3) {
                    Text("+${user.companies.size - 3}", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = user.email,
            color = Color.White.copy(0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        // --- ACCIÓN: REGRESAR A PERSONAL (Solo si está en modo empresa) ---
        if (!isPersonalProfile) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onProfileSelected(null) },
                colors = ButtonDefaults.buttonColors(containerColor = cyberCyan.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, cyberCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, null, tint = cyberCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("VOLVER AL PERFIL PERSONAL", color = cyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
        Spacer(Modifier.height(24.dp))

        // --- SECCIÓN: NETWORK / ENTIDADES ASOCIADAS ---
        if (user.companies.isNotEmpty()) {
            Text(
                "NETWORK // ENTIDADES VINCULADAS",
                color = cyberPurple,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(20.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                user.companies.forEach { company ->
                    val isActive = !isPersonalProfile && company.id == selectedProfileId
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CutCornerShape(topStart = 12.dp))
                            .background(if (isActive) cyberPurple.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
                            .border(1.dp, if (isActive) cyberPurple else Color.White.copy(alpha = 0.1f), CutCornerShape(topStart = 12.dp))
                            .clickable { onProfileSelected(company.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(cyberPurple.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (company.photoUrl != null) {
                                AsyncImage(model = company.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Business, null, tint = cyberPurple, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = company.name.uppercase(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            Text(text = "CONEXIÓN EMPRESARIAL", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = cyberPurple, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }

        // --- BOTÓN DESCONECTAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CutCornerShape(bottomStart = 16.dp)) // CORTE EN BORDE INFERIOR IZQUIERDO
                .background(Color(0xFFFF1744).copy(0.1f))
                .border(1.dp, Color(0xFFFF1744).copy(0.3f), CutCornerShape(bottomStart = 16.dp))
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color(0xFFFF1744), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("DESCONECTAR SESIÓN", color = Color(0xFFFF1744), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp)
            }
        }
    }
}

/**
 * LocationPopup: Ventana emergente para visualización y selección de ubicación.
 * Rediseñada bajo el estándar "Elite Masterpiece" con arquitectura de 3 secciones:
 * 1. RADAR SATELITAL (GPS Prioridad con Glow Activo)
 * 2. NODOS PERSONALES (Ubicaciones Guardadas)
 * 3. BUSINESS NETWORK (Sincronización de Perfil de Empresa)
 */
@Composable
fun LocationPopup(
    user: UserEntity?,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    onLocationSelected: (AddressInfo) -> Unit,
    activeAddress: AddressInfo?,
    isScrollable: Boolean = true
) {
    val cyberCyan = CPCyberColors.MaverickCyan
    val cyberPurple = CPCyberColors.ElectricPurple
    var isRefreshing by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(topStart = 24.dp, bottomEnd = 24.dp))
            .background(Color(0xFF0B0F19).copy(alpha = 0.98f))
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(cyberCyan, cyberPurple)),
                shape = CutCornerShape(topStart = 24.dp, bottomEnd = 24.dp)
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "CONFIGURACIÓN DE NODO ACTIVO",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // BOTÓN DE CIERRE RESALTADO
            IconButton(
                onClick = onClose, 
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- SECCIÓN 1: RADAR SATELITAL (GPS) ---
        val isGpsActive = activeAddress?.id == "gps_current"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CutCornerShape(topStart = 16.dp))
                .background(if (isGpsActive) cyberCyan.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f))
                .border(
                    width = 1.dp, 
                    color = if (isGpsActive) cyberCyan else Color.White.copy(alpha = 0.1f), 
                    shape = CutCornerShape(topStart = 16.dp)
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SatelliteAlt, 
                    contentDescription = null, 
                    tint = if (isGpsActive) cyberCyan else Color.White.copy(alpha = 0.3f), 
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "RADAR SATELITAL // GPS_LIVE", 
                    color = if (isGpsActive) Color.White else Color.White.copy(alpha = 0.4f), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black, 
                    letterSpacing = 1.sp
                )
                if (isGpsActive) {
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(6.dp).background(cyberCyan, CircleShape))
                }
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        isRefreshing = true
                        onRefresh()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(cyberCyan.copy(0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.MyLocation, null, tint = cyberCyan, modifier = Modifier.rotate(rotationAnim).size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            
            val address = if (isGpsActive) activeAddress.streetAndNumber.ifBlank { "DETECTANDO..." } else "MODO OFFLINE"
            val detail = if (isGpsActive) {
                listOfNotNull(activeAddress.locality, activeAddress.province, activeAddress.postalCode, activeAddress.country)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
            } else "TOCA EL ICONO PARA ACTIVAR"
            
            Text(text = address.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (isGpsActive) Color.White else Color.White.copy(alpha = 0.3f))
            Text(text = detail.uppercase(), fontSize = 9.sp, color = if (isGpsActive) cyberCyan else Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(32.dp))

        // --- SECCIÓN: DIRECTORIO DE NODOS ---
        Column(modifier = if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier) {
            if (user != null) {
                // --- SECCIÓN 2: NODOS PERSONALES ---
                Text(
                    "NODOS PERSONALES // GUARDADOS",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(16.dp))

                user.personalAddresses.forEach { addr ->
                    val isActive = !isGpsActive && activeAddress?.id == addr.id
                    val fullDetail = if (isActive) {
                        listOfNotNull(addr.localidad, addr.provincia, addr.codigoPostal, addr.pais)
                            .filter { it.isNotBlank() }
                            .joinToString(", ")
                    } else {
                        "${addr.localidad}, ${addr.provincia}"
                    }

                    CyberTreeLeaf(
                        icon = Icons.Default.Home,
                        title = "${addr.calle} ${addr.numero}",
                        subtitle = fullDetail,
                        accentColor = if (isActive) cyberCyan else Color.White.copy(alpha = 0.3f),
                        isActive = isActive
                    ) {
                        onLocationSelected(
                            AddressInfo(
                                id = addr.id,
                                ownerId = null,
                                companyOrUserName = user.displayName,
                                branchName = addr.label.ifEmpty { "Mi Domicilio" },
                                streetAndNumber = "${addr.calle} ${addr.numero}",
                                locality = addr.localidad,
                                province = addr.provincia,
                                country = addr.pais,
                                postalCode = addr.codigoPostal,
                                isCompany = false,
                                lat = addr.latitude,
                                lng = addr.longitude
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // --- SECCIÓN 3: NETWORK BUSINESS ---
                if (user.companies.isNotEmpty()) {
                    Text(
                        "BUSINESS NETWORK // SUCURSALES",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    user.companies.forEach { company ->
                        company.branches.forEach { branch ->
                            val isActive = !isGpsActive && activeAddress?.id == branch.id
                            val addr = branch.address
                            val fullDetail = if (isActive) {
                                listOfNotNull(addr.localidad, addr.provincia, addr.codigoPostal, addr.pais)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                            } else {
                                "${addr.localidad}, ${addr.provincia}"
                            }

                            CyberTreeLeaf(
                                icon = Icons.Default.Business,
                                title = "${company.name} - ${branch.name}",
                                subtitle = fullDetail,
                                accentColor = if (isActive) cyberPurple else Color.White.copy(alpha = 0.3f),
                                isActive = isActive
                            ) {
                                onLocationSelected(
                                    AddressInfo(
                                        id = branch.id,
                                        ownerId = company.id,
                                        companyOrUserName = company.name,
                                        branchName = branch.name,
                                        streetAndNumber = "${branch.address.calle} ${branch.address.numero}",
                                        locality = branch.address.localidad,
                                        province = branch.address.provincia,
                                        country = branch.address.pais,
                                        postalCode = branch.address.codigoPostal,
                                        isCompany = true,
                                        lat = branch.address.latitude,
                                        lng = branch.address.longitude
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(2000)
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
fun WeatherExpandedCard(temperature: String, weatherEmoji: String, weatherDescription: String, cityName: String) {
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
        Text(
            text = "SYS_WTHR // ATMÓSFERA_V2", 
            color = cyberCyan, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 2.sp, 
            modifier = Modifier.align(Alignment.Start)
        )
        
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
    user: UserEntity,
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
    user: UserEntity?,
    activeAddress: AddressInfo?,
    onRefresh: () -> Unit,
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
                            user = user,
                            onClose = { closeWithAnimation() },
                            onRefresh = { onRefresh(); closeWithAnimation() },
                            onLocationSelected = { onLocationSelected(it); closeWithAnimation() },
                            activeAddress = activeAddress
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
                            cityName = cityName
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
    val mockUser = UserEntity(
        id = "1",
        name = "Juan",
        lastName = "Pérez",
        displayName = "JUAN",
        email = "juan.perez@example.com",
        photoUrl = null
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
                user = mockUser,
                onClose = {},
                onRefresh = {},
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
                cityName = "San Miguel de Tucumán"
            )
        }
    }
}
